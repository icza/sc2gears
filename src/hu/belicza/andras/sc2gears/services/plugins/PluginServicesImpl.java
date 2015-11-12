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

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.services.plugins.PluginControl;
import hu.belicza.andras.sc2gears.ui.dialogs.PluginManagerDialog;
import hu.belicza.andras.sc2gearspluginapi.PluginServices;
import hu.belicza.andras.sc2gearspluginapi.api.SettingsApi;

/**
 * A {@link PluginServices} provider.
 * 
 * @author Andras Belicza
 */
public class PluginServicesImpl implements PluginServices {
	
	/** Implementation version. */
	public static final String IMPL_VERSION = "2.2";
	
	/** Reference to the plugin control. */
	private final PluginControl pluginControl;
	
	/** The persistent settings of the plugin.  */
	private SettingsApi settings;
	
	/**
	 * Creates a new PluginServicesImpl.
	 * @param pluginControl reference to the plugin control
	 */
	public PluginServicesImpl( final PluginControl pluginControl ) {
		this.pluginControl = pluginControl;
	}
	
	@Override
	public String getImplementationVersion() {
		return IMPL_VERSION;
	}
	
	@Override
	public void openPluginDetailsInPluginManager() {
		new PluginManagerDialog( pluginControl );
	}
	
	@Override
	public synchronized SettingsApi getSettingsApi() {
		if ( settings == null )
			settings = new SettingsApiImpl( new File( Consts.FOLDER_PLUGIN_SETTINGS, pluginControl.pluginDescriptor.getMainClass() + ".xml" ) );
		
		return settings;
	}
	
	/**
	 * Returns the persistent settings of the plugin without creating it if it is not yet created.
	 * @return the persistent settings of the plugin without creating it if it is not yet created
	 */
	public synchronized SettingsApi getSettingsWithoutCreating() {
		return settings;
	}
	
	@Override
	public File getPluginFileCacheFolder() {
		final File plguinFileCacheFolder = new File( Consts.FOLDER_PLUGIN_FILE_CACHE_BASE, pluginControl.pluginDescriptor.getMainClass() );
		
		if ( !plguinFileCacheFolder.exists() )
			plguinFileCacheFolder.mkdirs();
		
		return plguinFileCacheFolder;
	}
	
	@Override
	public void reportFailure( final boolean requestRestart ) {
		pluginControl.reportFailure( requestRestart );
	}
	
}
