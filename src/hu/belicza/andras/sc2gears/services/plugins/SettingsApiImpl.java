/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.services.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Properties;

import hu.belicza.andras.sc2gearspluginapi.api.SettingsApi;

/**
 * {@link SettingsApi} implementation.
 * 
 * @author Andras Belicza
 */
class SettingsApiImpl implements SettingsApi {
	
	/** Settings file for persistent storage. */
	private final File settingsFile;
	/** The {@link Properties} instance to store the properties. */
	private Properties properties = new Properties();
	
	/**
	 * Creates a new SettingsApiImpl.
	 * @param settingsFile settings file for persistent storage
	 */
	public SettingsApiImpl( final File settingsFile ) {
		this.settingsFile = settingsFile;
		loadProperties();
	}
	
	@Override
	public void set( final String key, final Object value ) {
		properties.setProperty( key, value.toString() );
	}
	
	@Override
	public String getString( final String key ) {
		return properties.getProperty( key );
	}
	
	@Override
	public int getInt( final String key ) {
		return Integer.parseInt( properties.getProperty( key ) );
	}
	
	@Override
	public boolean getBoolean( final String key ) {
		return Boolean.parseBoolean( properties.getProperty( key ) );
	}
	
	@Override
	public void remove( final String key ) {
		properties.remove( key );
	}
	
	@Override
	public void removeAll() {
		properties.clear();
	}
	
	@Override
	public boolean loadProperties() {
		if ( settingsFile.exists() )
			try {
				properties.loadFromXML( new FileInputStream( settingsFile ) );
				return true;
			} catch ( final Exception e ) {
				System.err.println( "Failed to load plugin properties!" );
				e.printStackTrace( System.err );
			}
		return false;
	}
	
	@Override
	public boolean saveProperties() {
		// If no property is set...
		if ( properties.isEmpty() ) {
			if ( settingsFile.exists() )
				// ...and there are persisted properties, delete them
				return settingsFile.delete();
			else
				// ...and there are no persisted properties, we're done
				return true;
		}
		
		try ( final FileOutputStream output = new FileOutputStream( settingsFile ) ) {
			properties.storeToXML( output, "Saved at " + new Date() );
			return true;
		} catch ( final Exception e ) {
			System.err.println( "Failed to save plugin properties!" );
			e.printStackTrace( System.err );
			return false;
		}
	}
	
	@Override
	public boolean isPersisted() {
		return settingsFile.exists();
	}
	
}

