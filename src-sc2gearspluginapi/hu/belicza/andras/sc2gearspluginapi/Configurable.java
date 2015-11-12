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

/**
 * Interface that defines a configurable plugin.
 * 
 * <p>If a plugin implements this interface, the <i>"Configure plugin"</i> button will be enabled in the Plugin manager.
 * It is recommended to use the {@link SettingsApi} to store the plugin's settings.</p>
 * 
 * @since "2.1"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see Plugin
 * @see SettingsControl
 */
public interface Configurable {
	
	/** Interface version. */
	String VERSION = "2.1";
	
	/**
	 * Returns if action is required from the user's part.
	 * 
	 * <p>The application can use this method to indicate that an action is required from the user's part,
	 * for example the user has to set/fill some basic settings which are required for the operation of the plugin.<br>
	 * If this method returns true, Sc2gears will attract the user's attention to the <i>"Configure plugin"</i> button.</p>
	 * 
	 * <p>Typically this method should return true if some mandatory settings has to be provided by the user.
	 * This method should return false if mandatory settings are already provided by the user
	 * or if there are no required settings.</p>
	 * 
	 * @return true if action is required from the user; false otherwise
	 */
	boolean isActionRequired();
	
	/**
	 * Returns a settings control which will be used to display and edit the settings of the plugin.
	 * 
	 * <p>Since this can be called multiple times, a new instance have to be returned on each call.</p>
	 * 
	 * @return a settings control which will be used to display and edit the settings of the plugin
	 * 
	 * @see SettingsControl
	 */
	SettingsControl getSettingsControl();
	
}
