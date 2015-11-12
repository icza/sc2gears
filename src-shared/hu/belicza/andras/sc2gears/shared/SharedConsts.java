/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.shared;

import java.io.File;

/**
 * Shared consts for the updater and Sc2gears.
 * 
 * @author Andras Belicza
 */
public class SharedConsts {
	
	/** Name of the application.         */
	public static final String APPLICATION_NAME    = "Sc2gears";
	/** Authors first name.              */
	public static final String AUTHOR_FIRST_NAME   = "András";
	/** Authors first name.              */
	public static final String AUTHOR_LAST_NAME    = "Belicza";
	
	/** Name of the Updater.             */
	public static final String UPDATER_NAME        = "Sc2gears Updater";
	/** Version of the Sc2gears Updater. */
	public static final String UPDATER_VERSION     = "1.5";
	
	/** URL of the home page.          */
	public static final String URL_HOME_PAGE         = "https://sites.google.com/site/sc2gears/";
	/** URL of the version history.    */
	public static final String URL_VERSION_HISTORY   = URL_HOME_PAGE + "version-history";
	
	/** Name of the folder where user contents reside. */
	public static final String FOLDER_USER_CONTENT   = "User Content";
	/** Name of the folder where logs are saved.                         */
	public static final String FOLDER_LOGS           = FOLDER_USER_CONTENT + "/Logs";
	/** Lib updater folder.                            */
	public static final File   LIB_UPDATER_FOLDER    = new File( "lib-updater" ).getAbsoluteFile();
	/** Name of the updater lib.                       */
	public static final String LIB_UPDATER_NAME      = "Sc2gearsUpdater.jar";
	/** Name of the new updater lib.                   */
	public static final String NEW_LIB_UPDATER_NAME  = "Sc2gearsUpdater2.jar";
	
	/** Extension of the log files.                    */
	public static final String LOG_FILE_EXT          = ".log";
	
	/** Name of the executable Sc2gears file for windows. */
	public static final String EXECUTABLE_NAME_WIN       = "Sc2gears.exe";
	/** Name of the executable Sc2gears file for windows. */
	public static final String EXECUTABLE_NAME_WIN_BATCH = "Sc2gears-win.cmd";
	/** Name of the executable Sc2gears file for OS-X.    */
	public static final String EXECUTABLE_NAME_OS_X      = "Sc2gears-os-x.command";
	/** Name of the executable Sc2gears file for Unix.    */
	public static final String EXECUTABLE_NAME_UNIX      = "Sc2gears-linux.sh";
	
	/** Command that starts Sc2gears on windows. */
	public static final String SC2GEARS_START_COMMAND_WIN  = EXECUTABLE_NAME_WIN;
	/** Command that starts Sc2gears on OS-X.    */
	public static final String SC2GEARS_START_COMMAND_OS_X = "./" + EXECUTABLE_NAME_OS_X;
	/** Command that starts Sc2gears on Unix.    */
	public static final String SC2GEARS_START_COMMAND_UNIX = "./" + EXECUTABLE_NAME_UNIX;
	
}
