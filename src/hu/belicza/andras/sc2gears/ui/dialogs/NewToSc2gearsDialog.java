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

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog.SettingsTab;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * "New to Sc2gears?" information dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class NewToSc2gearsDialog extends BaseDialog {
	
	/**
	 * Creates a new AboutDialog.
	 */
	public NewToSc2gearsDialog() {
		super( "newToSc2gears.title", new Object[] { Consts.APPLICATION_NAME }, Icons.LIGHT_BULB );
		
		final JLabel infoLabel = new JLabel( Language.getText( "newToSc2gears.info", Consts.APPLICATION_NAME + "™" ) );
		GuiUtils.changeFontToBold( infoLabel );
		infoLabel.setFont( infoLabel.getFont().deriveFont( (float) ( infoLabel.getFont().getSize() + 16 ) ) );
		infoLabel.setBorder( BorderFactory.createEmptyBorder( 15, 20, 15, 20 ) );
		getContentPane().add( infoLabel, BorderLayout.NORTH );
		
		final Box box = Box.createVerticalBox();
		box.setBorder( BorderFactory.createEmptyBorder( 10, 20, 15, 20 ) );
		addBoxRow( box, Language.getText( "newToSc2gears.ensureCorrectFolders" ), MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.FOLDERS ) );
		addBoxRow( box, Language.getText( "newToSc2gears.autoSaveNameTemplate" ), MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.REPLAY_AUTO_SAVE ) );
		addBoxRow( box, Language.getText( "newToSc2gears.favoredPlayerList"    ), MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.REPLAY_PARSER ) );
		addBoxRow( box, Language.getText( "newToSc2gears.realTimeGameTime"     ), MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.REPLAY_PARSER ) );
		addBoxRow( box, Language.getText( "newToSc2gears.autoStoreReplays", Consts.APPLICATION_NAME ), MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.SC2GEARS_DATABASE ) );
		final JLabel runDiagnosticTestLabel = GeneralUtils.createLinkStyledLabel( Language.getText( "newToSc2gears.runDiagnosticTestLink" ) );
		runDiagnosticTestLabel.setIcon( Icons.SYSTEM_MONITOR );
		runDiagnosticTestLabel.addMouseListener( new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				new DiagnosticToolDialog();
			};
		} );
		addBoxRow( box, Language.getText( "newToSc2gears.runDiagnostic" ), runDiagnosticTestLabel );
		addBoxRow( box, Language.getText( "newToSc2gears.openTipsDialog" ), TipsDialog.createOpenLinkLabel( "newToSc2gears.openTipsDialogLink", true ) );
		addBoxRow( box, Language.getText( "newToSc2gears.limitation", Consts.APPLICATION_NAME ), new JLabel( " ", Icons.EXCLAMATION_BIG, JLabel.LEFT ) );
		addBoxRow( box, Language.getText( "newToSc2gears.legit"     , Consts.APPLICATION_NAME ), new JLabel( " ", Icons.TICK_BIG, JLabel.LEFT ) );
		// TODO In some cases the dialog is stretched (some HTML components have invalid preferred size?) try to call this "later"?
		GuiUtils.alignBox( box, 7 );
		getContentPane().add( box, BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 0, 20, 15, 20 ) );
		final JButton closeButton = createCloseButton( "button.close" );
		closeButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				Settings.set( Settings.KEY_SHOW_NEW_TO_SC2GEARS_DIALOG_ON_STARTUP, Boolean.FALSE );
			}
		} );
		buttonsPanel.add( closeButton );
		final JButton remindMeNextTimeButton = new JButton();
		GuiUtils.updateButtonText( remindMeNextTimeButton, "newToSc2gears.remindMeNextTimeButton" );
		remindMeNextTimeButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				Settings.set( Settings.KEY_SHOW_NEW_TO_SC2GEARS_DIALOG_ON_STARTUP, Boolean.TRUE );
				dispose();
			}
		} );
		buttonsPanel.add( remindMeNextTimeButton );
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		packAndShow( closeButton, false );
	}
	
	/** Row counter for adding new rows. */
	private int rowCounter = 1;
	
	/**
	 * Adds a new row to a box.
	 * @param box           box to add to
	 * @param text          text to add
	 * @param linkComponent associated link component to add
	 */
	private void addBoxRow( final Box box, final String text, final Component linkComponent ) {
		Box row = Box.createHorizontalBox();
		
		row.add( new JLabel( rowCounter++ + "." ) );
		row.add( Box.createHorizontalGlue() );
		row.add( Box.createHorizontalStrut( 10 ) );
		row.add( new JLabel( text ) );
		row.add( Box.createHorizontalGlue() );
		row.add( Box.createHorizontalStrut( 10 ) );
		row.add( linkComponent );
		
		box.add( row );
		box.add( Box.createVerticalStrut( 15 ) );
	}
	
}
