/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.dialogs;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.TemplateEngine;
import hu.belicza.andras.sc2gears.util.TemplateEngine.Symbol;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayOpCallback;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Replay rename dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class ReplayRenameDialog extends BaseDialog {
	
	/**
	 * Creates a new ReplayRenameDialog.
	 * @param replayOpCallback optional callback that has to be called upon file changes
	 * @param files            files to operate on
	 */
	public ReplayRenameDialog( final ReplayOpCallback replayOpCallback, final File[] files ) {
		super( "replayops.renameDialog.title", Icons.DOCUMENT_RENAME );
		
		final JPanel editorPanel = new JPanel( new BorderLayout() );
		editorPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		
		final JComboBox< String > nameTemplateComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.REP_RENAME_TEMPLATE );
		nameTemplateComboBox.setSelectedItem( Settings.getString( Settings.KEY_REP_SEARCH_RESULTS_RENAME_TEMPLATE ) );
		editorPanel.add( GuiUtils.createNameTemplateEditor( nameTemplateComboBox, Symbol.REPLAY_COUNTER ), BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		final JButton previewButton = new JButton();
		GuiUtils.updateButtonText( previewButton, "replayops.renameDialog.previewButton" );
		buttonsPanel.add( previewButton );
		final JButton renameButton = new JButton();
		GuiUtils.updateButtonText( renameButton, "replayops.renameDialog.renameButton" );
		buttonsPanel.add( renameButton );
		final JButton cancelButton = createCloseButton( "button.cancel" );
		buttonsPanel.add( cancelButton );
		editorPanel.add( buttonsPanel, BorderLayout.SOUTH );
		
		getContentPane().add( editorPanel, BorderLayout.NORTH );
		
		final JTextArea previewTextArea = new JTextArea( 10, 50 );
		previewTextArea.setEditable( false );
		getContentPane().add( new JScrollPane( previewTextArea ), BorderLayout.CENTER );
		
		final ActionListener renameActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final boolean preview = event.getSource() == previewButton;
				
				if ( preview )
					previewTextArea.setText( "" );
				
				int errorCounter = 0;
				try {
					final TemplateEngine templatEngine = new TemplateEngine( nameTemplateComboBox.getSelectedItem().toString() );
					for ( int fileIndex = 0; fileIndex < files.length; fileIndex++ ) {
						final File   file    = files[ fileIndex ];
						final String newName = templatEngine.applyToReplay( file, file.getParentFile() );
						if ( newName == null ) {
							errorCounter++;
							continue;
						}
						
						if ( preview )
							previewTextArea.append( newName + "\n" );
						else {
							if ( newName.equals( file.getName() ) )
								continue; // Same name, no need to rename (and if we would proceed, we would wrongly append something like " (2)" at the end of it...
							final File destinationFile = GeneralUtils.generateUniqueName( new File( file.getParent(), newName ) );
							// Create potential sub-folders specified by the name template
							final File parentOfDestinationFile = destinationFile.getParentFile();
							boolean failedToCreateSubfolders = false;
							if ( !parentOfDestinationFile.exists() )
								failedToCreateSubfolders = !parentOfDestinationFile.mkdirs();
							if ( file.renameTo( destinationFile ) ) {
								if ( replayOpCallback != null )
									replayOpCallback.replayRenamed( file, destinationFile, fileIndex );
							}
							else {
								System.out.println( "Failed to rename replay file" + ( destinationFile.exists() ? " (target file already exists)" : failedToCreateSubfolders ? " (failed to create sub-folders)" : "" ) + "!" );
								errorCounter++;
							}
						}
					}
				} catch ( final Exception e ) {
					e.printStackTrace();
					GuiUtils.showErrorDialog( Language.getText( "replayops.renameDialog.invalidTemplate", e.getMessage() ) );
					return;
				} finally {
					nameTemplateComboBox.requestFocusInWindow();
				}
				
				if ( preview )
					previewTextArea.setCaretPosition( 0 );
				else {
					if ( replayOpCallback != null )
						replayOpCallback.moveRenameDeleteEnded();
					// Template is valid here, so we can store it
					Settings.set( Settings.KEY_REP_SEARCH_RESULTS_RENAME_TEMPLATE, nameTemplateComboBox.getSelectedItem().toString() );
					if ( errorCounter == 0 )
						GuiUtils.showInfoDialog( Language.getText( "replayops.renameDialog.successfullyRenamed", files.length ) );
					else
						GuiUtils.showErrorDialog( new Object[] { Language.getText( "replayops.renameDialog.successfullyRenamed", files.length - errorCounter ), Language.getText( "replayops.renameDialog.failedToRename", errorCounter ) } );
					dispose();
				}
			}
		};
		previewButton.addActionListener( renameActionListener );
		renameButton .addActionListener( renameActionListener );
		
		previewButton.doClick();
		packAndShow( nameTemplateComboBox, false );
	}
	
}
