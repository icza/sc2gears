/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.impl;

import hu.belicza.andras.sc2gearspluginapi.SettingsControl;

import javax.swing.JDialog;

/**
 * A basic, abstract implementation of {@link SettingsControl}.
 * 
 * <p>Plugins that create {@link SettingsControl}s may choose to extend this class
 * (this class implements the {@link SettingsControl} interface).</p>
 * 
 * <p>Stores the reference of the settings dialog, and implements the {@link SettingsControl#onCancelButtonPressed()}
 * method (but does nothing).</p>
 * 
 * @since "2.1"
 * 
 * @version {@value #IMPL_VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see SettingsControl
 */
public abstract class BaseSettingsControl implements SettingsControl {
	
	/** Implementation version. */
	public static final String IMPL_VERSION = "2.1";
	
	/** Reference to store the settings dialog. */
	protected JDialog settingsDialog;
	
	@Override
	public void receiveSettingsDialog( final JDialog settingsDialog ) {
		this.settingsDialog = settingsDialog;
	}
	
	/**
	 * This implementation does nothing.
	 */
	@Override
	public void onCancelButtonPressed() {
	}
	
}
