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

import java.io.File;

import hu.belicza.andras.sc2gearspluginapi.api.SettingsApi;

/**
 * Defines services for Sc2gears plugins which are plugin specific.
 * 
 * <p>The plugin context is bound to the plugin main class, so that cannot be changed.
 * If the main class of the plugin is changed (the package name and/or the class name),
 * a new context will be assigned to the plugin.</p>
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see SettingsApi
 */
public interface PluginServices {
	
	/** Interface version. */
	String VERSION = "2.2";
	
	/**
	 * Returns the plugin services implementation version.
	 * @return the plugin services implementation version
	 */
	String getImplementationVersion();
	
	/**
	 * Opens the Plugin manager and selects this plugin.
	 */
	void openPluginDetailsInPluginManager();
	
	/**
	 * Returns the persistent settings API of the plugin.
	 * @return the persistent settings API of the plugin
	 * @see SettingsApi
	 */
	SettingsApi getSettingsApi();
	
	/**
	 * Returns the folder where the plugin may store persistent files, files that need to remain between launches.
	 * 
	 * <p>Typically these include resource files that the plugin does not contain initially (in its release),
	 * but downloads them later / "on-the-fly" and are required for its operation.
	 * These type of files should be saved in this folder.</p>
	 * 
	 * <p>The returned folder is already created (unless the user does not have access to create that folder).
	 * The returned folder is located under the <code>"User Content"</code> folder inside the Sc2gears folder.</p>
	 * 
	 * <p>Files stored in the returned folder are persistent, but the user may delete them manually.
	 * It is a good practice to check if files saved in this folder still exists when the plugin wants to use them.</p>
	 * 
	 * <p>The plugin may create and use sub-folders inside the returned file cached folder.</p>
	 * 
	 * <p>The returned folder is plugin-specific: each plugin has its own, unique file cache folder.</p>
	 * 
	 * @return the folder where the plugin may store persistent files
	 * 
	 * @since "2.2"
	 */
	File getPluginFileCacheFolder();
	
	/**
	 * The plugin may arbitrary call this method to report that it has failed and stops (or stopped) working.
	 * 
	 * <p>The plugin may request to be restarted by passing <code>requestRestart=true</code>.
	 * Restarting means the {@link Plugin#destroy()} will be called, and a new plugin instance will be created and initialized.</p>
	 * 
	 * @param requestRestart true if the plugin requests to be restarted; false otherwise
	 * @since "2.0"
	 */
	void reportFailure( boolean requestRestart );
	
}
