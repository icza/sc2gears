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

import hu.belicza.andras.sc2gearspluginapi.impl.BasePlugin;

/**
 * Interface that all Sc2gears plugins must implement. Moreover plugin implementations must provide a public no-argument constructor.
 * 
 * <p>If a plugin wants to take advantage of the <i>"Configure plugin"</i> button displayed in the Plugin manager,
 * the plugin has to implement the {@link Configurable} interface.</p>
 * 
 * <p>The plugin may store settings and persistent files which will be saved in the plugin's own context.
 * This plugin context is bound to the main class of the plugin, so the main class cannot be changed.
 * If the main class of the plugin is changed (the package name and/or the class name),
 * a new context will be assigned to the plugin.</p>
 * 
 * @version Plugin API version: {@value #API_VERSION}<br>Interface version: {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see BasePlugin
 * @see PluginDescriptor
 * @see PluginServices
 * @see GeneralServices
 * @see Configurable
 */
public interface Plugin {
	
	/** Plugin API version. */
	String API_VERSION = "4.2";
	
	/** Interface version. */
	String VERSION     = "1.0";
	
	/**
	 * Called when the plugin is initialized.<br>
	 * 
	 * <p>This method is only called once in the lifetime of a plugin.<br>
	 * The plugin may start a new {@link Thread} if the initialization takes long or if it intends to perform a background job.</p>
	 * 
	 * <p>If this method throws an {@link Exception}, the {@link #destroy()} method will be called and the plugin will be discarded.<br>
	 * If initialization fails, an {@link Exception} should be thrown to indicate the failure.</p>
	 * 
	 * @param pluginDescriptor the plugin descriptor; this reference should be stored by the plugin if the plugin needs it later 
	 * @param pluginServices   reference to the plugin services; this reference should be stored by the plugin if the plugin intends to use it later
	 * @param generalServices  reference to the general services; this reference should be stored by the plugin if the plugin intends to use it later
	 * 
	 * @see PluginDescriptor
	 * @see PluginServices
	 * @see GeneralServices
	 */
	void init( PluginDescriptor pluginDescriptor, PluginServices pluginServices, GeneralServices generalServices );
	
	/**
	 * Called when the plugin is being unloaded.<br>
	 * 
	 * <p>The plugin should release all allocated resources and stop all background jobs and threads created by it.<br>
	 * The plugin cannot perform any tasks after returning from this method because the application may be terminated at any time.</p>
	 * 
	 * <p>This method will be called even if the plugin is instantiated successfully (the constructor throws no exception)
	 * but the {@link #init(PluginDescriptor, PluginServices, GeneralServices)} throws an exception.</p>
	 * 
	 * <p>Any thrown {@link Exception} will be silently discarded.</p>
	 */
	void destroy();
	
}
