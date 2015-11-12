/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.logger.Log;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.services.InstanceMonitor;
import hu.belicza.andras.sc2gears.services.plugins.PluginManager;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.WelcomeFrame;
import hu.belicza.andras.sc2gears.util.CliHandler;
import hu.belicza.andras.sc2gears.util.GeneralUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.UIManager;

import com.sun.jna.Native;

/**
 * This is the main class of Sc2gears.
 * 
 * <p>Application starts in developer mode if the <code>"developer-mode"</code> environment variable is present (see {@link Consts#DEVELOPER_MODE} for details).
 * You can achieve this by passing the <code>-Ddev-mode</code> VM argument to the <code>java.exe</code> or <code>javaw.exe</code> process.</p>
 * 
 * @author Andras Belicza
 */
public class Sc2gears {
	
	/**
	 * No need to instantiate this class.
	 */
	private Sc2gears() {
	}
	
	/**
	 * The entry point of the program.
	 * 
	 * <p>Checks for running instances, and passes arguments if have to.
	 * Else loads the settings, language files etc. and then instantiates the main frame.</p>
	 * 
	 * @param arguments if command line mode is desired, they will be handled by the {@link CliHandler};
	 * 					else they will be treated as files (like replays, replay lists, replay sources) and will be opened properly 
	 * @throws MalformedURLException 
	 */
	public static void main( final String[] arguments ) {
		// Add Sc2gears version and OS info to the User-Agent HTTP request property.
		// The final user agent string will be the value of this property + the default (which is the Java version).
		System.setProperty( "http.agent", Consts.APPLICATION_NAME + "/" + Consts.APPLICATION_VERSION
				+ " (" + System.getProperty( "os.name" ) + "; " + System.getProperty( "os.version" ) + "; " + System.getProperty( "os.arch" ) + ")" );
		
		checkFolders();
		
		Settings.loadProperties();
		
		final boolean cliMode = CliHandler.checkCliMode( arguments );
		
		if ( !cliMode ) {
			InstanceMonitor.checkRunningInstance( arguments );
			Log.init();
		}
		
		if ( !cliMode ) {
			installExtraLAFs();
			GuiUtils.setLAF( Settings.getString( Settings.KEY_SETTINGS_LAF ) );
		}
		
		if ( Settings.doesSettingsFileExist() ) {
			Language.loadAndActivateLanguage( Settings.getString( Settings.KEY_SETTINGS_LANGUAGE ) );
		}
		else {
			// Only show the welcome frame if we're not in CLI mode
			if ( !cliMode ) {
				final WelcomeFrame welcomeFrame = new WelcomeFrame();
				synchronized ( welcomeFrame ) {
					try {
						welcomeFrame.wait(); // Wait until the welcome frame is closed.
					} catch ( final InterruptedException ie ) {
						// This should never happen.
					}
				}
			}
		}
		
		// Now language is loaded, initialize codes that build on it.
		Language.applyDateTimeFormats();
		Settings.completeDefaultPropertiesInitialization();
		GuiUtils.initFileFilters();
		
		// Must be after completeDefaultPropertiesInitialization()!
		checkAndPerformPostUpdate();
		
		// Ensure ReplayUtils and AbilityCodesRepository is initialized
		// (else opening a replay from the file menu or by the CilHandler would fail!)
		try {
			Class.forName( ReplayUtils.class.getName() );
		} catch ( final ClassNotFoundException cfe ) {
			// Never to happen
			cfe.printStackTrace();
		}
		
		if ( cliMode )
			System.exit( CliHandler.handleArguments( arguments ) );
		else {
			// Load the Native class now, else later it might hang (dead lock?)
			if ( GeneralUtils.isWindows() ) // Currently JNA is only used on windows
				try {
					Class.forName( Native.class.getName() );
				} catch ( final ClassNotFoundException cfe ) {
					// Never to happen
					cfe.printStackTrace();
				}
			
			// Apply proxy config
			applyProxyConfig();
			
			// Load plugins
			PluginManager.loadPlugins();
			
			// Now instantiate the main frame
			new MainFrame( arguments );
		}
	}
	
	/**
	 * Checks some internal folders and creates them if they don't exist.
	 */
	private static void checkFolders() {
		for ( final String folder : Consts.USER_CONTENT_FOLDERS ) {
			final File file = new File( folder );
			if ( !file.exists() )
				file.mkdirs();
		}
	}
	
	/**
	 * Checks if post-update is required to complete the update process, and if so, performs it.
	 * @return true if post-update is not required, or required but was performed successfully; false otherwise
	 */
	public static boolean checkAndPerformPostUpdate() {
		final File newUpdaterJar = new File( Consts.LIB_UPDATER_FOLDER, Consts.NEW_LIB_UPDATER_NAME ).getAbsoluteFile();
		if ( newUpdaterJar.exists() ) {
			System.out.println( "Performing post-update..." );
			// Replace the Updater jar with the new one
			final File updaterJar = new File( Consts.LIB_UPDATER_FOLDER, Consts.LIB_UPDATER_NAME ).getAbsoluteFile();
			
			for ( int i = 0; i < 20; i++ ) { // Try a few times, Updater might have not yet shut down
				if ( !updaterJar.exists() || updaterJar.delete() ) {
					if ( newUpdaterJar.renameTo( updaterJar ) ) {
						// Now do additional tasks
						performPostUpdateTasks();
						
						System.out.println( "Post-update completed." );
						return true;
					}
					else {
						System.err.println( "Could not rename file: " + newUpdaterJar.getAbsolutePath() );
						System.err.println( "\tto: " + updaterJar.getAbsolutePath() );
						System.err.println( "Post-update could not be performed!" );
						return false;
					}
				}
				else
					try { Thread.sleep( 100l ); } catch ( final InterruptedException ie ) { ie.printStackTrace(); }
			}
			
			System.err.println( "Could not delete file: " + updaterJar.getAbsolutePath() );
			System.err.println( "Post-update could not be performed!" );
			
			return false;
		}
		else
			return true;
	}
	
	/**
	 * Performs post update tasks.
	 */
	private static void performPostUpdateTasks() {
		// TODO review on each release!
		
		// Set default enabled plugins: add the Build Orders Table plugin (but does not disable other ones...)
		final Set< String > enabledPluginSet = new HashSet< String >( Settings.getEnabledPluginSet() );
		enabledPluginSet.add( "hu.belicza.andras.buildorderstableplugin.BuildOrdersTablePlugin" );
		Settings.setEnabledPluginSet( enabledPluginSet );
		
		// MD5 hash symbol has been introduced:
		{
			final String autoSaveNameTemplateSetting = Settings.getString( Settings.KEY_SETTINGS_MISC_REP_AUTO_SAVE_NAME_TEMPLATE );
			if ( "/D./e".equals( autoSaveNameTemplateSetting ) ) // The old default value
				Settings.set( Settings.KEY_SETTINGS_MISC_REP_AUTO_SAVE_NAME_TEMPLATE, Settings.getDefaultString( Settings.KEY_SETTINGS_MISC_REP_AUTO_SAVE_NAME_TEMPLATE ) ); // Set the new default
		}
		
		// Player full name has been removed:
		{
			final String autoSaveNameTemplateSetting = Settings.getString( Settings.KEY_SETTINGS_MISC_REP_AUTO_SAVE_NAME_TEMPLATE );
			if ( autoSaveNameTemplateSetting != null )
				Settings.set( Settings.KEY_SETTINGS_MISC_REP_AUTO_SAVE_NAME_TEMPLATE, autoSaveNameTemplateSetting.replace( "/P", "/p" ).replace( "/Q", "/q" ) );
			final String replayRenameTemplateSetting = Settings.getString( Settings.KEY_REP_SEARCH_RESULTS_RENAME_TEMPLATE );
			if ( replayRenameTemplateSetting != null )
				Settings.set( Settings.KEY_REP_SEARCH_RESULTS_RENAME_TEMPLATE, replayRenameTemplateSetting.replace( "/P", "/p" ).replace( "/Q", "/q" ) );
			final String predefinedAutoSaveNameTemplatesSetting = Settings.getString( PredefinedList.REP_AUTO_SAVE_TEMPLATE.settingsKey );
			if ( predefinedAutoSaveNameTemplatesSetting != null )
				Settings.set( PredefinedList.REP_AUTO_SAVE_TEMPLATE.settingsKey, predefinedAutoSaveNameTemplatesSetting.replace( "/P", "/p" ).replace( "/Q", "/q" ) );
			final String repRenameTemplatesSetting = Settings.getString( PredefinedList.REP_RENAME_TEMPLATE.settingsKey );
			if ( repRenameTemplatesSetting != null )
				Settings.set( PredefinedList.REP_RENAME_TEMPLATE.settingsKey, repRenameTemplatesSetting.replace( "/P", "/p" ).replace( "/Q", "/q" ) );
			Settings.rebuildPredefinedLists();
		}
		
		// Remove keys of changed/removed settings
		Settings.remove( "settings.misc.useInMemoryReplayCache" );
		Settings.remove( "module.repAnalyzer.charts.mapView.hotAreasCount" );
		Settings.remove( "module.repAnalyzer.charts.mapView.iconSizesCS" );
		
		// If replay site list has changed/rearranged:
		Settings.remove( Settings.KEY_SHARE_REP_REPLAY_SITE );
		
		// The initial default value of the pre-defined replay list had a typo: "battlecuriser" instead of "battlecruiser"
		final String repAnalFilterSetting = Settings.getString( PredefinedList.REP_ANAL_FILTER.settingsKey );
		if ( repAnalFilterSetting != null ) {
			if ( repAnalFilterSetting.contains( "battlecuriser" ) )
				Settings.set( PredefinedList.REP_ANAL_FILTER.settingsKey, repAnalFilterSetting.replace( "battlecuriser", "battlecruiser" ) );
			Settings.rebuildPredefinedLists();
		}
		
		// Removed LaFs: Napkin and EaSynthLookAndFeel
		if ( "Napkin".equals( Settings.getString( Settings.KEY_SETTINGS_LAF ) ) )
			Settings.remove( Settings.KEY_SETTINGS_LAF );
		if ( "EaSynthLookAndFeel".equals( Settings.getString( Settings.KEY_SETTINGS_LAF ) ) )
			Settings.remove( Settings.KEY_SETTINGS_LAF );
		
		// Save changes
		Settings.saveProperties();
		
		// If profile cache version changed:
		hu.belicza.andras.sc2gears.util.ProfileCache.emptyCache( null );
	}
	
	/**
	 * Installs extra LAFs.
	 */
	private static void installExtraLAFs() {
		UIManager.installLookAndFeel( "Office 2003"       , "org.fife.plaf.Office2003.Office2003LookAndFeel" );
		UIManager.installLookAndFeel( "Office XP"         , "org.fife.plaf.OfficeXP.OfficeXPLookAndFeel" );
		UIManager.installLookAndFeel( "Visual Studio 2005", "org.fife.plaf.VisualStudio2005.VisualStudio2005LookAndFeel" );
		UIManager.installLookAndFeel( "Squareness"        , "net.beeger.squareness.SquarenessLookAndFeel" );
	}
	
	/**
	 * Applies the proxy configuration from the {@link Settings}.
	 */
	public static void applyProxyConfig() {
		// Proxy guide:
		// http://download.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
		if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_ENABLE_PROXY_CONFIG ) ) {
			System.setProperty( "http.proxyHost" , Settings.getString( Settings.KEY_SETTINGS_MISC_HTTP_PROXY_HOST  ) );
			System.setProperty( "http.proxyPort" , Settings.getString( Settings.KEY_SETTINGS_MISC_HTTP_PROXY_PORT  ) );
			
			System.setProperty( "https.proxyHost", Settings.getString( Settings.KEY_SETTINGS_MISC_HTTPS_PROXY_HOST ) );
			System.setProperty( "https.proxyPort", Settings.getString( Settings.KEY_SETTINGS_MISC_HTTPS_PROXY_PORT ) );
			
			System.setProperty( "socksProxyHost" , Settings.getString( Settings.KEY_SETTINGS_MISC_SOCKS_PROXY_HOST ) );
			System.setProperty( "socksProxyPort" , Settings.getString( Settings.KEY_SETTINGS_MISC_SOCKS_PROXY_PORT ) );
		} else {
			System.setProperty( "http.proxyHost" , "" );
			System.setProperty( "http.proxyPort" , "" );
			
			System.setProperty( "https.proxyHost", "" );
			System.setProperty( "https.proxyPort", "" );
			
			System.setProperty( "socksProxyHost" , "" );
			System.setProperty( "socksProxyPort" , "" );
		}
	}
	
}
