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

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.NormalThread;
import hu.belicza.andras.sc2gearspluginapi.Plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.Icon;

/**
 * Class storing and controlling a plugin.
 * @author Andras Belicza
 */
public class PluginControl implements Comparable< PluginControl > {
	
	/**
	 * Plugin status.
	 * @author Andras Belicza
	 */
	public static enum Status {
		/** Plugin is disabled, compatible.                                */
		DISABLED                       ( Icons.EXCLAMATION_OCTAGON_FRAME, "pluginManager.status.disabled" ),
		/** Plugin is disabled, incompatible.                              */
		DISABLED_INCOMPATIBLE          ( Icons.EXCLAMATION_SHIELD_FRAME , "pluginManager.status.disabled" ),
		/** Plugin is enabled, but denied to start due to incompatibility. */
		ENABLED_STOPPED                ( Icons.CROSS_SHIELD             , "pluginManager.status.enabledStopped" ),
		/** Plugin is enabled, but denied to start due to incompatibility. */
		ENABLED_DENIED_INCOMPATIBLE    ( Icons.CROSS_SHIELD             , "pluginManager.status.enabledDeniedIncompatible" ),
		/** Plugin is enabled, but failed to start.                        */
		ENABLED_FAILED_TO_START        ( Icons.CROSS                    , "pluginManager.status.enabledFailedToStart" ),
		/** Plugin is enabled, but failed during execution.                */
		ENABLED_FAILED_DURING_EXECUTION( Icons.CROSS_OCTAGON            , "pluginManager.status.enabledFailedDuringExecution" ),
		/** Plugin is enabled, running.                                    */
		ENABLED_RUNNING                ( Icons.TICK                     , "pluginManager.status.enabledRunning" );
		
		/** Icon of the status. */
		public final Icon icon;
		/** Cache of the string representation of the status. */
		public final String stringValue;
		
		/**
		 * Creates a new Status.
		 * @param icon icon of the status
		 * @param textKey key of the text representation of the status
		 */
		private Status( final Icon icon, final String textKey ) {
			this.icon   = icon;
			stringValue = Language.getText( textKey );
		}
		
		@Override
		public String toString() {
			return stringValue;
		}
		
	}
	
	/** The plugin descriptor. */
	public final PluginDescriptorImpl pluginDescriptor;
	
	/** Tells if the plugin is compatible with the current plugin implementation. */
	public final boolean isCompatible;
	
	/** Reference to the main {@link Class} of the plugin if loaded. */
	public Class< ? extends Plugin > loadedMainClass;
	
	/** Reference to the plugin services implementation. */
	public PluginServicesImpl pluginServices;
	
	/** Reference to the plugin if it is running. */
	public Plugin plugin;
	
	/** Status of the plugin. */
	public Status status;
	
	/**
	 * Creates a new PluginControl.
	 * @param pluginDescriptor the plugin descriptor
	 * @param isCompatible     tells if the plugin is compatible with the current plugin implementation 
	 */
	public PluginControl( final PluginDescriptorImpl pluginDescriptor, final boolean isCompatible ) {
		this.pluginDescriptor = pluginDescriptor;
		this.isCompatible     = isCompatible;
		status = Settings.getEnabledPluginSet().contains( pluginDescriptor.getMainClass() ) ? Status.ENABLED_STOPPED
				: isCompatible ? Status.DISABLED : Status.DISABLED_INCOMPATIBLE;
	}
	
	/**
	 * Tells if the plugin is disabled.
	 * @return true if the plugin is disabled
	 */
	public boolean isDisabled() {
		return status == Status.DISABLED || status == Status.DISABLED_INCOMPATIBLE;
	}
	
	/**
	 * Tells if there were errors starting/running the plugin.
	 * @return true if there were errors starting/running the plugin; false otherwise
	 */
	public boolean isError() {
		return status == Status.ENABLED_FAILED_TO_START || status == Status.ENABLED_DENIED_INCOMPATIBLE || status == Status.ENABLED_FAILED_DURING_EXECUTION;
	}
	
	/**
	 * Starts the plugin.
	 * @return true if the plugin was successfully started or was already running; false otherwise 
	 */
	public synchronized boolean startPlugin() {
		if ( plugin != null ) // Plugin already running
			return true;
		
		if ( !isCompatible && !Settings.getBoolean( Settings.KEY_PLUGIN_MANAGER_ALLOW_INCOMPATIBLE_PLUGINS ) ) {
			System.out.println( "Denying plugin \"" + pluginDescriptor.getName() + "\" to start because you did not allow incompatible plugins!" );
			status = Status.ENABLED_DENIED_INCOMPATIBLE;
			return false;
		}
		
		try {
			// Load main class of the plugin if not yet loaded
			if ( loadedMainClass == null ) {
				final File[] pluginLibs = pluginDescriptor.getPluginLibs();
				final URL[]  libUrls    = new URL[ pluginLibs.length ];
				for ( int i = 0; i < pluginLibs.length; i++ )
					libUrls[ i ] = pluginLibs[ i ].toURI().toURL();
				
				// We don't close the class loader here because when it is running, might require to load new classes...
				// TODO we could close it when the plugin is stopped!
				@SuppressWarnings("resource")
				final URLClassLoader urlClassLoader = new URLClassLoader( libUrls );
				loadedMainClass = urlClassLoader.loadClass( pluginDescriptor.getMainClass() ).asSubclass( Plugin.class );
			}
			
			// Instantiate plugin
			plugin = loadedMainClass.newInstance();
			
			// Initialize/start plugin
			// Plugin initialization is called from a new thread with normal priority so if the plugin starts new threads,
			// they will inherit the proper normal priority by default.
			final Thread pluginStarterThread = new NormalThread( "Plugin starter" ) {
				@Override
				public void run() {
					try {
						if ( pluginServices == null )
							pluginServices = new PluginServicesImpl( PluginControl.this );
						plugin.init( pluginDescriptor, pluginServices, GeneralServicesImpl.getInstance() );
					} catch ( final Throwable t ) {
						System.out.println( "Failed to start plugin \"" + pluginDescriptor.getName() + "\"!" );
						t.printStackTrace();
						
						// Plugin is already instantiated, destroy it
						disposePlugin();
						status = Status.ENABLED_FAILED_TO_START; // Must be set after disposePlugin()
					}						
				}
			};
			pluginStarterThread.start();
			pluginStarterThread.join();
			
			status = Status.ENABLED_RUNNING;
		} catch ( final Throwable t ) {
			status = Status.ENABLED_FAILED_TO_START;
			
			System.out.println( "Failed to start plugin \"" + pluginDescriptor.getName() + "\"!" );
			t.printStackTrace();
		}
		
		return status == Status.ENABLED_RUNNING;
	}
	
	/**
	 * Disposes the plugin.
	 */
	public synchronized void disposePlugin() {
		try {
			if ( plugin == null ) // Plugin not running / already disposed
				return;
			
			try {
				plugin.destroy();
			} catch ( final Throwable t ) {
				// silently ignore
			} finally {
				plugin = null;
			}
		} finally {
			status = Status.ENABLED_STOPPED;
		}
	}
	
	/**
	 * The plugin is reporting that it has failed and stops working.
	 * @param requestRestart true if the plugin request to be restarted
	 */
	public void reportFailure( final boolean requestRestart ) {
		status = Status.ENABLED_FAILED_DURING_EXECUTION;
		
		System.out.println( "Plugin \"" + pluginDescriptor.getName() + "\" has failed" + ( requestRestart ? " and is being restarted..." : "."  ) );
		
		if ( requestRestart ) {
			disposePlugin();
			startPlugin();
		}
	}
	
	/**
	 * Defines an order based by the plugin name.
	 */
	@Override
	public int compareTo( final PluginControl pluginSatte ) {
		return pluginDescriptor.getName().compareTo( pluginSatte.pluginDescriptor.getName() );
	}
	
	/**
	 * Overrides {@link Object#toString()} to return the name of the plugin
	 */
	@Override
	public String toString() {
		return pluginDescriptor.getName();
	}
	
}

