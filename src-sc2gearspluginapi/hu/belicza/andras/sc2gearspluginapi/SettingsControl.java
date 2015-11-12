/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi;

import hu.belicza.andras.sc2gearspluginapi.api.SettingsApi;
import hu.belicza.andras.sc2gearspluginapi.impl.BaseSettingsControl;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * A control interface to display and edit the settings of a plugin.
 * 
 * <p>It is recommended to use the {@link SettingsApi} to store the plugin's settings.</p>
 * 
 * @since "2.1"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see Configurable#getSettingsControl()
 * @see BaseSettingsControl
 */
public interface SettingsControl {
	
	/** Interface version. */
	String VERSION = "2.1";
	
	/**
	 * Receives the reference to the settings dialog.
	 * 
	 * <p>The dialog is handled automatically, no need to do anything with it.
	 * The reference is handed here so it can serve as the parent dialog
	 * for optional child dialogs (for example file chooser dialogs).</p>
	 * 
	 * <p>This method will be called before {@link #getEditorPanel()} is invoked,
	 * but at the time of calling the GUI of the dialog is obviously not yet constructed.</p>
	 * 
	 * @param settingsDialog reference to the settings dialog
	 */
	void receiveSettingsDialog( JDialog settingsDialog );
	
	/**
	 * Returns a {@link Container} that wraps the editor components to display and edit the settings of the plugin.
	 * This can be typically a {@link JPanel} or a {@link Box}.
	 * 
	 * <p>The editor components have to be initialized with the current settings of the plugin.</p>
	 * 
	 * <p>This component will be added to the center ({@link BorderLayout#CENTER}) of the content pane of the settings dialog.</p>
	 * 
	 * @return a {@link Container} that wraps the editor components to display and edit the settings of the plugin
	 */
	Container getEditorPanel();
	
	/**
	 * Called when the OK button is pressed on the settings dialog.
	 * 
	 * <p>The implementation must get the settings from the editor components and store them.</p>
	 */
	void onOkButtonPressed();
	
	/**
	 * Called when the the plugin settings dialog is closed in any other way than pressign the OK button,
	 * typically by pressing the Cancel button or clicking on the window close icon or pressing ALT+F4.
	 * 
	 * <p>The implementation MUST NOT get and store the settings from the editor components.
	 * The implementation may use this to free resources that were allocated for the Settings control.
	 * A typical implementation does nothing here.</p>
	 */
	void onCancelButtonPressed();
	
}
