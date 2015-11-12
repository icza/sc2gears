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
import hu.belicza.andras.sc2gears.services.plugins.PluginControl;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gearspluginapi.Configurable;
import hu.belicza.andras.sc2gearspluginapi.SettingsControl;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A dialog to display and edit the settings of a plugin.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class PluginSettingsDialog extends BaseDialog {
	
	/**
	 * Creates a new PluginSettingsDialog.
	 * @param owner                 owner of the dialog
	 * @param pluginControl         reference to the plugin control this settings dialog belongs to
	 * @param onOkButtonPressedTask taks to be executed when the OK button is pressed
	 */
	public PluginSettingsDialog( final Dialog owner, final PluginControl pluginControl, final Runnable onOkButtonPressedTask ) {
		super( owner, new Holder< String >( Language.getText( "pluginSettings.title", pluginControl.pluginDescriptor.getName() ) ), Icons.WRENCH );
		
		final SettingsControl settingsControl = ( (Configurable) pluginControl.plugin ).getSettingsControl();
		settingsControl.receiveSettingsDialog( this );
		
		// Call SettingsControl.onCancelButtonPressed() when window is closed
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing( final WindowEvent event ) {
				settingsControl.onCancelButtonPressed();
			}
		} );
		
		( (JComponent) getContentPane() ).setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		getContentPane().add( settingsControl.getEditorPanel(), BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		final JButton okButton = new JButton();
		GuiUtils.updateButtonText( okButton, "button.ok" );
		okButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				settingsControl.onOkButtonPressed();
				onOkButtonPressedTask.run();
				dispose();
			}
		} );
		buttonsPanel.add( okButton );
		final JButton cancelButton = createCloseButton( "button.cancel" );
		cancelButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				settingsControl.onCancelButtonPressed();
			}
		} );
		buttonsPanel.add( cancelButton );
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		packAndShow( cancelButton, false );
	}
	
}
