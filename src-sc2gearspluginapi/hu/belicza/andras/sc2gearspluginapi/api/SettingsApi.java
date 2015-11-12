/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api;

import hu.belicza.andras.sc2gearspluginapi.PluginServices;

/**
 * Defines Settings services.
 * 
 * <p>Each plugin gets its own settings container, stored properties do not mix with properties of other plugins or Sc2gears itself.
 * The plugin container is bound to the main class, so if you would change the main class,
 * the modified plugin would see a new, empty settings storage.</p>
 * 
 * <p>The settings that are set through this interface are automatically saved when settings are saved in Sc2gears,
 * and are automatically loaded when the plugin accesses this service.</p>
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see PluginServices#getSettingsApi()
 */
public interface SettingsApi {
	
	/** Interface version. */
	String VERSION = "1.0";
	
	/**
	 * Sets the value of a property.<br>
	 * The string value returned by <code>value.toString()</code> will be set.
	 * @param key   key of the property
	 * @param value value of the property
	 */
	void set( String key, Object value );
	
	/**
	 * Returns the String value of a property.
	 * @param key key of the property
	 * @return the String value of the property
	 */
	String getString( String key );
	
	/**
	 * Returns the int value of a property.<br>
	 * Does not check if the value associated with the specified key is an int.
	 * @param key key of the property
	 * @return the int value of the property
	 */
	int getInt( String key );
	
	/**
	 * Returns the boolean value of a property.
	 * Does not check if the value associated with the specified key is a boolean.
	 * @param key key of the property
	 * @return the boolean value of the property
	 */
	boolean getBoolean( String key );
	
	/**
	 * Removes the value of a property.
	 * @param key key of the property to be removed
	 * @see #removeAll()
	 */
	void remove( String key );
	
	/**
	 * Removes all values.
	 */
	void removeAll();
	
	/**
	 * Loads the properties from the storage.
	 * <p>Note that properties are automatically loaded when the plugin accesses the {@link SettingsApi} service.
	 * This method forces an immediate load.</p>
	 * @return true if properties has been loaded successfully; false otherwise
	 * @see #saveProperties()
	 */
	boolean loadProperties();
	
	/**
	 * Saves the properties to the storage.
	 * <p>Note that properties are automatically saved when settings are saved in Sc2gears.
	 * This method forces an immediate save.</p>
	 * @return true if properties has been saved successfully; false otherwise
	 * @see #loadProperties()
	 */
	boolean saveProperties();
	
	/**
	 * Tells if a persistent storage for this plugin already exists.
	 * @return true if a persistent storage for this plugin already exists; false otherwise
	 */
	boolean isPersisted();
	
}
