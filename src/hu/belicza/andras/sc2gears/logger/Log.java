/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.logger;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.settings.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Log Manager controls the console redirection to a log file and provides access to it.
 * 
 * @author Andras Belicza
 */
public class Log {
	
	/** Name of the log file name. */
	private static final String LOG_FILE_NAME = "system_messages";
	/** The log file.              */
	private static final File   LOG_FILE      = new File( Consts.FOLDER_LOGS, LOG_FILE_NAME + Consts.LOG_FILE_EXT );
	
	/**
	 * No need to instantiate this class.
	 */
	private Log() {
	}
	
	/**
	 * Initializes the log manager.
	 */
	public static void init() {
		try {
			// Backup (rename) last log file
			if ( LOG_FILE.exists() && LOG_FILE.length() > 0 )
				LOG_FILE.renameTo( new File( Consts.FOLDER_LOGS, LOG_FILE_NAME + new SimpleDateFormat( " yyyy-MM-dd HH-mm-ss" ).format( new Date( LOG_FILE.lastModified() ) ) + Consts.LOG_FILE_EXT ) );
			
			// Delete old log files
			final File[] logFiles = new File( Consts.FOLDER_LOGS ).listFiles();
			if ( logFiles != null ) {
				final long oldestAllowed = System.currentTimeMillis() - Settings.getInt( Settings.KEY_SETTINGS_MISC_TIME_TO_KEEP_LOG_FILES ) * 24l * 60 * 60 * 1000;
				for ( final File logFile : logFiles )
					if ( logFile.lastModified() < oldestAllowed )
						logFile.delete();
			}
			
			// Create new log file
			if ( !Consts.DEVELOPER_MODE ) {
				final PrintStream logStream = new PrintStream( new FileOutputStream( LOG_FILE ), true );
				System.setOut( logStream );
				System.setErr( logStream );
			}
		} catch ( final FileNotFoundException fnfe ) {
			fnfe.printStackTrace();
		}
	}
	
	/**
	 * Returns the content of the log file.
	 * @return the content of the log file; or <code>null</code> if the log file cannot be read
	 */
	public static String getLog() {
		try ( final FileInputStream logInput = new FileInputStream( LOG_FILE ) ) {
			final byte[] logContent = new byte[ (int) LOG_FILE.length() ];
			logInput.read( logContent );
			
			return new String( logContent );
		} catch ( final Exception e ) {
			return null;
		}
	}
	
}
