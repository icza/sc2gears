/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.settings;

import java.util.Properties;

/**
 * Settings face.
 * 
 * @author Andras Belicza
 */
public class Settings {
	
	public  static final String KEY_SETTINGS_MISC_INITIAL_TIME_TO_EXCLUDE_FROM_APM    = "settings.misc.initialTimeToExcludeFromApm";
	public  static final String KEY_SETTINGS_MISC_USE_REAL_TIME_MEASUREMENT           = "settings.misc.useRealTimeMeasurement";
	public  static final String KEY_SETTINGS_MISC_DECLARE_LARGEST_AS_WINNER           = "settings.misc.declareLargestAsWinner";
	public  static final String KEY_SETTINGS_MISC_OVERRIDE_FORMAT_BASED_ON_MATCHUP    = "settings.misc.overrideFormatBasedOnMatchup";
	
	/** Properties holding the default settings. */
	private static final Properties DEFAULT_PROPERTIES = new Properties();
	static {
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_INITIAL_TIME_TO_EXCLUDE_FROM_APM   , "110" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_USE_REAL_TIME_MEASUREMENT          , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_DECLARE_LARGEST_AS_WINNER          , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_OVERRIDE_FORMAT_BASED_ON_MATCHUP   , "true" );
	}
	
	/** The actual properties. */
	private static Properties properties = new Properties( DEFAULT_PROPERTIES );
	
	/**
	 * Returns the int value of a property.
	 * @param key key of the property
	 * @return the int value of the property
	 */
	public static int getInt( final String key ) {
		return Integer.parseInt( properties.getProperty( key ) );
	}
	
	/**
	 * Returns the boolean value of a property.
	 * @param key key of the property
	 * @return the boolean value of the property
	 */
	public static boolean getBoolean( final String key ) {
		return Boolean.parseBoolean( properties.getProperty( key ) );
	}
	
	/**
	 * Returns the alias group name for the specified map.<br>
	 * If the map name is not part of any alias groups, the same reference is returned.
	 * @param mapName name of map whose alias group name to be returned
	 * @return the alias group name for the specified map or <code>mapName</code> if the map is not part of any alias groups
	 */
	public static String getMapAliasGroupName( final String mapName ) {
		return mapName;
	}
	
}
