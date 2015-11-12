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

import hu.belicza.andras.sc2gearspluginapi.Plugin;
import hu.belicza.andras.sc2gearspluginapi.PluginDescriptor;
import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.PluginServices;

/**
 * A basic, abstract implementation of {@link Plugin}.
 * 
 * <p>Plugin implementations may choose to extend this class (this class implements the {@link Plugin} interface).</p>
 * 
 * <p>Stores the plugin descriptor, plugin services and general services in attributes for later use.</p>
 * 
 * @version {@value #IMPL_VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see Plugin
 */
public abstract class BasePlugin implements Plugin {
	
	/** Implementation version. */
	public static final String IMPL_VERSION = "1.0";
	
	/** Reference to the plugin descriptor. */
	protected PluginDescriptor pluginDescriptor;
	/** Reference to the plugin services.   */
	protected PluginServices   pluginServices;
	/** Reference to the general services.   */
	protected GeneralServices  generalServices;
	
	/**
	 * Stores the plugin descriptor, plugin services and general services in attributes for later use.
	 */
	@Override
	public void init( final PluginDescriptor pluginDescriptor, final PluginServices pluginServices, final GeneralServices generalServices ) {
		this.pluginDescriptor = pluginDescriptor;
		this.pluginServices   = pluginServices;
		this.generalServices  = generalServices;
	}
	
}
