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

import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerId;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Defines general information query services.
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GeneralServices
 */
public interface InfoApi {
	
	/** Interface version. */
	String VERSION = "2.7";
	
	/**
	 * Returns the application version.
	 * @return the application version
	 */
	String getApplicationVersion();
	
	/**
	 * Returns the release date of the application
	 * @return the release date of the application
	 */
	Date getApplicationReleaseDate();
	
	/**
	 * Returns the application language version.
	 * @return the application language version
	 */
	String getApplicationLanguageVersion();
	
	/**
	 * Returns the list of auto replay folders.
	 * @return the list of auto replay folders
	 */
	List< File > getAutoRepFolderList();
	
	/**
	 * Tells if replay auto-save is enabled.
	 * @return true if replay auto-save is enabled; false otherwise
	 * @see #getReplayAutoSaveFolder()
	 */
	boolean isReplayAutoSaveEnabled();
	
	/**
	 * Returns the replay auto-save folder.
	 * @return the replay auto-save folder
	 * @since "2.0"
	 * @see #isReplayAutoSaveEnabled()
	 */
	File getReplayAutoSaveFolder();
	
	/**
	 * Tells if APM alert is enabled.
	 * @return true if APM alert is enabled; false otherwise
	 */
	boolean isApmAlertEnabled();
	
	/**
	 * Tells if "global hotkeys" is enabled.
	 * @return true if "global hotkeys" is enabled; false otherwise
	 */
	boolean isGlobalHotkeysEnabled();
	
	/**
	 * Tells if "voice notifications" is enabled.
	 * @return true if "voice notifications" is enabled; false otherwise
	 */
	boolean isVoiceNotificationsEnabled();
	
	/**
	 * Tells if real time conversion is enabled.
	 * 
	 * <p>If real time conversion is enabled, all time and APM values in Sc2gears are converted from game time to real time.</p>
	 * 
	 * @return true if real time conversion is enabled; false otherwise
	 * @since "1.0.1"
	 */
	boolean isRealTimeConversionEnabled();
	
	/**
	 * Tells if save mouse print is enabled.
	 * 
	 * <p>If it is enabled, mouse prints are automatically saved.</p>
	 * 
	 * @return true if save mouse print is enabled
	 * @since "2.0"
	 * @see #getMousePrintOutputFolder()
	 */
	boolean isSaveMousePrintEnabled();
	
	/**
	 * Returns the mouse print output folder.
	 * @return the mouse print output folder
	 * @since "2.0"
	 * @see #isSaveMousePrintEnabled()
	 */
	File getMousePrintOutputFolder();
	
	/**
	 * Returns the SC2 maps folder.
	 * @return the SC2 maps folder
	 * @since "2.0"
	 */
	File getSc2MapsFolder();
	
	/**
	 * Returns the favored player list.
	 * @return the favored player list
	 * @since "2.0"
	 */
	List< String > getFavoredPlayerList();
	
	/**
	 * Returns the alias group player id for the specified player.<br>
	 * If the player is not part of any alias groups, the same reference is returned.
	 * @param playerId player identifier whose alias group player id to be returned
	 * @return the alias group player id for the specified player or <code>playerId</code> if the player is not part of any alias groups
	 * @since "2.7"
	 */
	IPlayerId getAliasGroupPlayerId( IPlayerId playerId );
	
	/**
	 * Returns the alias group name for the specified map.<br>
	 * If the map name is not part of any alias groups, the same reference is returned.
	 * @param mapName name of map whose alias group name to be returned
	 * @return the alias group name for the specified map or <code>mapName</code> if the map is not part of any alias groups
	 * @since "2.7"
	 */
	String getMapAliasGroupName( String mapName );
	
	/**
	 * Returns the profile info validity time in days.
	 * @return the profile info validity time in days
	 * @since "2.0"
	 */
	int getProfileInfoValidityTime();
	
	/**
	 * Returns the auto-retrieve extended profile info setting.
	 * @return the auto-retrieve extended profile info setting
	 * @since "2.0"
	 */
	boolean getAutoRetrieveExtProfileInfo();
	
	/**
	 * Returns if the operating system running the application is windows.
	 * @return true if the operating system running the application is windows
	 */
	boolean isWindows();
	
	/**
	 * Returns if the operating system running the application is MAC OS-X.
	 * @return true if the operating system running the application is windows
	 */
	boolean isMac();
	
	/**
	 * Returns if the operating system running the application is Unix (linux).
	 * @return true if the operating system running the application is windows
	 */
	boolean isUnix();
	
	/**
	 * Returns if the operating system running the application is Windows 7.
	 * @return true if the operating system running the application is Windows 7
	 */
	boolean isWindows7();
	
	/**
	 * Returns if the operating system running the application is Windows Vista.
	 * @return true if the operating system running the application is Windows Vista
	 */
	boolean isWindowsVista();
	
	/**
	 * Returns if the operating system running the application is Windows XP.
	 * @return true if the operating system running the application is Windows XP
	 */
	boolean isWindowsXp();
	
}
