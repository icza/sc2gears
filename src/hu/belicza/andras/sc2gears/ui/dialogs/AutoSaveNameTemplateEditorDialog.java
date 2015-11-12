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
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.TemplateEngine;
import hu.belicza.andras.sc2gears.util.TemplateEngine.Symbol;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Auto-save name template editor dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class AutoSaveNameTemplateEditorDialog extends BaseDialog {
	
	/**
	 * Creates a new AutoSaveNameTemplateEditorDialog.
	 * @param owner                           the Frame from which the dialog is displayed
	 * @param repAutoSaveNameTemplateComboBox source and target editable combo box of the edited template
	 */
	public AutoSaveNameTemplateEditorDialog( final Dialog owner, final JComboBox< String > repAutoSaveNameTemplateComboBox ) {
		super( owner, "miscSettings.templateDialog.title", Icons.UI_SCROLL_PANE_LIST );
		
		final JPanel editorPanel = new JPanel( new BorderLayout() );
		editorPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		final JComboBox< String > nameTemplateComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.REP_AUTO_SAVE_TEMPLATE );
		nameTemplateComboBox.setSelectedItem( repAutoSaveNameTemplateComboBox.getSelectedItem().toString() );
		editorPanel.add( GuiUtils.createNameTemplateEditor( nameTemplateComboBox, Symbol.COUNTER, Symbol.COUNTER2 ), BorderLayout.CENTER );
		final JPanel buttonsPanel = new JPanel();
		final JTextField testOutputTextField = new JTextField();
		testOutputTextField.setEditable( false );
		final JButton testOnLastReplayButton = new JButton();
		GuiUtils.updateButtonText( testOnLastReplayButton, "miscSettings.templateDialog.testOnLastReplayButton" );
		testOnLastReplayButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				try {
					File lastReplayFile = null;
					for ( final File autoRepFolder : GeneralUtils.getAutoRepFolderList() )
						lastReplayFile = GeneralUtils.getLastReplay( autoRepFolder, lastReplayFile );
					testOutputTextField.setText( lastReplayFile == null ? "" : new TemplateEngine( nameTemplateComboBox.getSelectedItem().toString() ).applyToReplay( lastReplayFile, lastReplayFile.getParentFile() ) );
				} catch ( final Exception e ) {
					e.printStackTrace();
					GuiUtils.showErrorDialog( Language.getText( "replayops.renameDialog.invalidTemplate", e.getMessage() ) );
				}
				nameTemplateComboBox.requestFocusInWindow();
			}
		} );
		buttonsPanel.add( testOnLastReplayButton );
		final JButton okButton = new JButton();
		GuiUtils.updateButtonText( okButton, "button.ok" );
		okButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				repAutoSaveNameTemplateComboBox.setSelectedItem( nameTemplateComboBox.getSelectedItem().toString() );
				dispose();
			}
		} );
		buttonsPanel.add( okButton );
		final JButton cancelButton = createCloseButton( "button.cancel" );
		buttonsPanel.add( cancelButton );
		
		final JPanel wrapper = new JPanel( new BorderLayout() );
		wrapper.add( buttonsPanel, BorderLayout.CENTER );
		wrapper.add( testOutputTextField, BorderLayout.SOUTH );
		editorPanel.add( wrapper, BorderLayout.SOUTH );
		
		getContentPane().add( editorPanel, BorderLayout.NORTH );
		
		packAndShow( nameTemplateComboBox, false );
	}
	
}
