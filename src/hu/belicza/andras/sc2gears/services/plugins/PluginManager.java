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

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.services.plugins.PluginControl.Status;
import hu.belicza.andras.sc2gearspluginapi.Plugin;
import hu.belicza.andras.sc2gearspluginapi.api.SettingsApi;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Plugin manager responsible for handling plugins (finding, loading, starting, disposing etc.).
 * 
 * @author Andras Belicza
 */
public class PluginManager {
	
	/** Name of the plugin descriptor files. */
	private static final String PLUGIN_DESCRIPTOR_NAME = "Sc2gears-plugin.xml";
	
	/** Plugin API verison. */
	private static final int[] PLUGIN_API_VERSION = ReplayUtils.parseVersion( Plugin.API_VERSION );
	
	/** The map of loaded plugins. The key is the main class of the plugin. */
	private static final Map< String, PluginControl > pluginMap = new HashMap< String, PluginControl >();
	
	/**
	 * Searches for plugins and loads them.
	 */
	public static void loadPlugins() {
		// First look for plugins
		final File   pluginsFolder = new File( Consts.FOLDER_PLUGINS );
		final File[] pluginFolders = pluginsFolder.listFiles( new FileFilter() {
			@Override
			public boolean accept( final File pathname ) {
				return pathname.isDirectory();
			}
		} );
		
		if ( pluginFolders == null ) {
			System.out.println( "Failed to load plugins, plugins folder does not exists (" + pluginsFolder.getAbsolutePath() + ") !" );
			return;
		}
		
		for ( final File pluginFolder : pluginFolders ) {
			final File pluginDescriptorFile = new File( pluginFolder, PLUGIN_DESCRIPTOR_NAME );
			
			if ( !pluginDescriptorFile.exists() )
				continue;
			final PluginDescriptorImpl pluginDescriptor = loadPluginDescriptor( new File( pluginFolder, PLUGIN_DESCRIPTOR_NAME ) );
			if ( pluginDescriptor == null )
				continue;
			
			final String name = pluginDescriptor.getName();
			
			final int[] implPluginVersion = ReplayUtils.parseVersion( pluginDescriptor.getApiVersion() );
			if ( implPluginVersion == null ) {
				System.out.println( "Plugin \"" + name + "\" cannot be loaded, invalid API version specification!" );
				continue;
			}
			
			// Check main class restrictions:
			final String mainClass = pluginDescriptor.getMainClass();
			final PluginControl anotherPluginControl = pluginMap.get( mainClass );
			if ( anotherPluginControl != null ) {
				System.out.println( "Plugin \"" + name + "\" cannot be loaded, main class is already associated with plugin: \"" + anotherPluginControl.pluginDescriptor.getName() + "\"" );
				continue;
			}
			
			// Detect plugin libs
			final File[] libs = pluginFolder.listFiles( new FileFilter() {
				@Override
				public boolean accept( final File pathname ) {
					return pathname.getName().endsWith( ".jar" );
				}
			} );
			if ( libs == null || libs.length == 0 ) {
				System.out.println( "Plugin \"" + name + "\" will not be loaded, no libraries (*.jar) were found in the plugin folder!" );
				continue;
			}
			pluginDescriptor.setPluginLibs( libs );
			
			// Everything OK, add the plugin
			final PluginControl pluginControl = new PluginControl( pluginDescriptor, isVersionCompatible( implPluginVersion ) );
			pluginMap.put( mainClass, pluginControl );
		}
	}
	
	/**
	 * Checks if the specified plugin implementation version is compatible with our plugin API version.
	 * @param implPluginVersion version to be checked
	 * @return true if the specified plugin implementation version is compatible with our plugin API version; false otherwise
	 */
	private static boolean isVersionCompatible( final int[] implPluginVersion ) {
		// TODO review compatibility condition on each release
		
		// Future plugin versions are not supported:
		if ( ReplayUtils.strictCompareVersions( implPluginVersion, PLUGIN_API_VERSION ) > 0 )
			return false;
		
		// Versions before "2.0" are not supported:
		if ( ReplayUtils.strictCompareVersions( implPluginVersion, new int[] { 2 } ) < 0  )
			return false;
		
		return true;
	}
	
	/**
	 * Loads the plugin descriptor from the specified file.
	 * @param pluginDescriptorFile plugin descriptor XML file to load from
	 * @return the loaded plugin descriptor; or <code>null</code> if the descriptor is invalid or some other error occurs
	 */
	private static PluginDescriptorImpl loadPluginDescriptor( final File pluginDescriptorFile ) {
		final DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
		try {
			final Document pluginDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( pluginDescriptorFile );
			final Element  rootElement    = pluginDocument.getDocumentElement();
			
			final PluginDescriptorImpl pluginDescriptorImpl = new PluginDescriptorImpl( pluginDescriptorFile );
			
			pluginDescriptorImpl.setName           ( ( (Element) rootElement.getElementsByTagName( "name"            ).item( 0 ) ).getTextContent() );
			pluginDescriptorImpl.setAuthorFirstName( ( (Element) rootElement.getElementsByTagName( "authorFirstName" ).item( 0 ) ).getTextContent() );
			pluginDescriptorImpl.setAuthorLastName ( ( (Element) rootElement.getElementsByTagName( "authorLastName"  ).item( 0 ) ).getTextContent() );
			pluginDescriptorImpl.setAuthorEmail    ( ( (Element) rootElement.getElementsByTagName( "authorEmail"     ).item( 0 ) ).getTextContent() );
			pluginDescriptorImpl.setVersion        ( ( (Element) rootElement.getElementsByTagName( "version"         ).item( 0 ) ).getTextContent() );
			pluginDescriptorImpl.setReleaseDate    ( dateFormat.parse( ( (Element) rootElement.getElementsByTagName( "releaseDate" ).item( 0 ) ).getTextContent() ) );
			pluginDescriptorImpl.setHomePage       ( ( (Element) rootElement.getElementsByTagName( "homePage"        ).item( 0 ) ).getTextContent() );
			pluginDescriptorImpl.setApiVersion     ( ( (Element) rootElement.getElementsByTagName( "apiVersion"      ).item( 0 ) ).getTextContent() );
			final Element descriptionElement = (Element) rootElement.getElementsByTagName( "description" ).item( 0 );
			pluginDescriptorImpl.setIsHtmlDescription( "true".equals( descriptionElement.getAttribute( "isHtml" ) ) );
			pluginDescriptorImpl.setDescription    ( descriptionElement.getTextContent() );
			pluginDescriptorImpl.setMainClass      ( ( (Element) rootElement.getElementsByTagName( "mainClass"       ).item( 0 ) ).getTextContent() );
			
			return pluginDescriptorImpl;
		} catch ( final Exception e ) {
			System.out.println( "Failed to load plugin descriptor from file: " + pluginDescriptorFile.getAbsolutePath() );
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Starts all enabled plugins.
	 */
	public static void startEnabledPlugins() {
		for ( final PluginControl pluginControl : pluginMap.values() )
			if ( pluginControl.status == Status.ENABLED_STOPPED )
				pluginControl.startPlugin();
	}
	
	/**
	 * Disposes all plugins.
	 */
	public static void disposePlugins() {
		for ( final PluginControl pluginControl : pluginMap.values() )
			pluginControl.disposePlugin();
	}
	
	/**
	 * Saves the settings of all plugins.
	 */
	public static void savePluginSettings() {
		for ( final PluginControl pluginControl : pluginMap.values() ) {
			// If settings were not created, that means the plugin did not load them (and could not change them).
			// No point loading it and saving the same...
			final SettingsApi settingsWithoutCreating = pluginControl.pluginServices == null ? null : pluginControl.pluginServices.getSettingsWithoutCreating();
			if ( settingsWithoutCreating != null )
				settingsWithoutCreating.saveProperties();
		}
	}
	
	/**
	 * Returns the vector of plugin controls.
	 * @return the vector of plugin controls
	 */
	public static Vector< PluginControl > getPluginControlVector() {
		return new Vector< PluginControl >( pluginMap.values() );
	}
	
}
