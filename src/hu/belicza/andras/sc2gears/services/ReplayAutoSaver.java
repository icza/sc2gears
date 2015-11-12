/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.services;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.sound.Sounds;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.ontopdialogs.OnTopGameInfoDialog;
import hu.belicza.andras.sc2gears.util.ControlledThread;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.NormalThread;
import hu.belicza.andras.sc2gears.util.ObjectRegistry;
import hu.belicza.andras.sc2gears.util.TemplateEngine;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayAutosaveListener;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sun.jna.platform.FileMonitor;
import com.sun.jna.platform.FileMonitor.FileEvent;
import com.sun.jna.platform.FileMonitor.FileListener;

/**
 * Replay auto-saver.
 * 
 * @author Andras Belicza
 */
public class ReplayAutoSaver extends ControlledThread implements FileFilter {
	
	/** Registry to handle replay auto-save listeners.  */
	private static final ObjectRegistry< ReplayAutosaveListener > replayAutosaveListenerRegistry  = new ObjectRegistry< ReplayAutosaveListener >();
	
	/**
	 * Adds a {@link ReplayAutosaveListener}.
	 * @param replayAutosaveListener replay auto-save listener to be added
	 */
	public static void addReplayAutosaveListener( final ReplayAutosaveListener replayAutosaveListener ) {
		replayAutosaveListenerRegistry.add( replayAutosaveListener );
	}
	
	/**
	 * Removes a {@link ReplayAutosaveListener}.
	 * @param replayAutosaveListener replay auto-save listener to be removed
	 */
	public static void removeReplayAutosaveListener( final ReplayAutosaveListener replayAutosaveListener ) {
		replayAutosaveListenerRegistry.remove( replayAutosaveListener );
	}
	
	/** Tells if File monitor is supported on the current machine. */
	public static final boolean fileMonitorSupported = GeneralUtils.isWindows();
	
	/**
	 * File detection methods.
	 * @author Andras Belicza
	 */
	public static enum NewFileDetectionMethod {
		// Note: If order is changed, default value for the KEY_SETTINGS_MISC_NEW_REPLAY_DETECTION_METHOD setting must be revised!
		// The reason why the POLLING is the first is because it is available on all OS while the other is not
		/** Polling (periodically scanning files).          */
		POLLING    ( "miscSettings.newFileDetectionMethod.polling"    ),
		/** Event based file detection (using FileMonitor). */
		EVENT_BASED( "miscSettings.newFileDetectionMethod.eventBased" );
		
		/** Cache of the string value. */
		public final String stringValue;
		
		/**
		 * Creates a new NewFileDetectionMethod.
		 * @param textKey key of the text representation
		 */
		private NewFileDetectionMethod( final String textKey ) {
			stringValue = Language.getText( textKey );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/** Default format of the auto-saved replays in case the provided template fails. */
	private static final DateFormat DEFAULT_AUTO_REPLAY_NAME_FORMAT = new SimpleDateFormat( "yy-MM-dd HH-mm-ss" );
	
	/** Date and time of the previous archived replay. */
	private volatile long previousReplayDate = System.currentTimeMillis();
	
	/** New replay file reported by the file monitor. */
	private volatile File lastReplayFromMonitor;
	
	/**
	 * Creates a new ReplayAutoSaver.
	 */
	public ReplayAutoSaver() {
		super( "Replay auto-saver" );
	}
	
	/**
	 * This method contains the cycle which checks for new replays.
	 */
	@Override
	public void run() {
		if ( Settings.getInt( Settings.KEY_SETTINGS_MISC_NEW_REPLAY_DETECTION_METHOD ) == NewFileDetectionMethod.EVENT_BASED.ordinal() && fileMonitorSupported )
			setupFileMonitor();
		
		while ( !requestedToCancel )
			try {
				File lastReplayFile = null;
				
				// Check if a new replay was saved
				if ( fileMonitor == null ) {
					for ( final File autoRepFolder : GeneralUtils.getAutoRepFolderList() )
						if ( ( lastReplayFile = getLastReplay( autoRepFolder ) ) != null )
							break;
				}
				else {
					if ( lastReplayFromMonitor != null ) {
						lastReplayFile        = lastReplayFromMonitor;
						lastReplayFromMonitor = null;
					}
				}
				
				if ( lastReplayFile != null ) {					
					// Wait a little, let SC2 finish saving the game...
					sleep( 1500l );
					// Check again the date: FileListener is called at creation time which is the current time, lastModified of file is only set after that
					if ( lastReplayFile.lastModified() > previousReplayDate )
						handleNewReplay( lastReplayFile );
				}
				
				sleep( Settings.getInt( Settings.KEY_SETTINGS_MISC_NEW_REP_CHECK_INTERVAL_SEC ) * 1000 );
			} catch ( final Exception e ) {
				// Do not stop if we failed to archive 1 replay, because the reason might be temporal (disk full for example)!
				e.printStackTrace();
			}
	}
	
	/**
	 * Returns the last replay (one replay) that is newer than the one we last archived.
	 * This is part of the polling method.
	 * @param startFolder start folder to start the search in
	 * @return the last replay (one replay) that is after the last check time
	 */
	private File getLastReplay( final File startFolder ) {
		final File[] files = startFolder.listFiles( this );
		if ( files == null )
			return null;
		
		for ( int i = files.length - 1; i >= 0; i-- ) { // Prolly the last is the last, so in order to minimize assignments, go downwards...
			final File file = files[ i ];
			if ( file.isFile() )
				return file;
			else {
				final File lastReplay = getLastReplay( file );
				if ( lastReplay != null )
					return lastReplay;
			}
		}
		
		return null;
	}
	
	/**
	 * Handles the saving of a new replay.
	 * @param lastReplayFile a ne replay to be handled
	 */
	private void handleNewReplay( final File lastReplayFile ) {
		// Store the last modification date now
		// (as it changes at the end of saving, and it will differ in our case if saving finishes during our last sleep,
		//  also in case of file monitor it might be the creation (current) time, lastModified is only set later...)
		previousReplayDate = lastReplayFile.lastModified();
		
		// Archive it
		final File targetFolder = new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_REPLAY_AUTO_SAVE ) );
		
		if ( !targetFolder.exists() )
			if ( !targetFolder.mkdirs() ) {
				System.out.println( "Failed to create replay auto-save folder: " + targetFolder.getAbsolutePath() );
				if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
					Sounds.playSoundSample( Sounds.SAMPLE_REPLAY_SAVE_FAILED, false );
			}
		
		if ( targetFolder.exists() ) {
			// Apply the template stored in the Settings
			String newName = null;
			try {
				newName = new TemplateEngine( Settings.getString( Settings.KEY_SETTINGS_MISC_REP_AUTO_SAVE_NAME_TEMPLATE ) ).applyToReplay( lastReplayFile, targetFolder );
			} catch ( final Exception e ) {
				System.out.println( "Failed to apply auto-save name template: " + Settings.getString( Settings.KEY_SETTINGS_MISC_REP_AUTO_SAVE_NAME_TEMPLATE ) );
			}
			if ( newName == null || newName.length() == 0 )                  // If template failed, revert to the default naming
				newName = DEFAULT_AUTO_REPLAY_NAME_FORMAT.format( new Date( lastReplayFile.lastModified() ) ) + ".SC2Replay";
			if ( !newName.toLowerCase().endsWith( ".sc2replay" ) )           // Append SC2Replay extension if missing
				newName += ".SC2Replay";
			
			final File targetFile = GeneralUtils.generateUniqueName( new File( targetFolder, newName ) );
			newName = targetFile.getName();
			// Create potential sub-folders specified by the name template
			final File parentOfTargetFile = targetFile.getParentFile();
			if ( !parentOfTargetFile.exists() )
				if ( !parentOfTargetFile.mkdirs() )
					System.out.println( "Creating subfolders failed, auto-save will probably fail!" );
			
			final boolean successfullyCopied = GeneralUtils.copyFile( lastReplayFile, parentOfTargetFile, null, newName );
			
			final File replayFile = successfullyCopied ? targetFile : lastReplayFile;
			
			// Show On-Top Game info dialog
			if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_SHOW_GAME_INFO_FOR_NEW_REPLAYS ) )
				OnTopGameInfoDialog.open( replayFile );
			
			// Play sound
			if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
				if ( successfullyCopied ) {
					if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_PLAY_REPLAY_SAVED_VOICE ) )
						Sounds.playSoundSample( Sounds.SAMPLE_REPLAY_SAVED, false );
				}
				else
					Sounds.playSoundSample( Sounds.SAMPLE_REPLAY_SAVE_FAILED, false );
			
			// Call replay auto-save listeners
			synchronized ( replayAutosaveListenerRegistry ) {
				for ( final ReplayAutosaveListener replayAutosaveListener : replayAutosaveListenerRegistry )
					try {
						// Call listener in a try-catch block to stop uncaught exceptions 
						replayAutosaveListener.replayAutosaved( targetFile, lastReplayFile );
					} catch ( final Throwable t ) {
						t.printStackTrace();
					}
			}
			
			if ( successfullyCopied ) {
				boolean deletedAutoSavedReplay = false;
				if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_DELETE_AUTO_SAVED_REPLAYS ) )
					deletedAutoSavedReplay = lastReplayFile.delete();
				System.out.println( "Successfully " + ( deletedAutoSavedReplay ? "moved" : "copied" ) + " last replay to: " + targetFile.getAbsolutePath() );
			}
			else
				System.out.println( "Failed to copy last replay to: " + parentOfTargetFile.getAbsolutePath() );
			
			if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_AUTO_STORE_NEW_REPLAYS ) ) {
				final String authorizationKey = Settings.getString( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY );
				if ( authorizationKey.length() == 0 )
					System.out.println( "Failed to store replay in the Sc2gears Database: no Authorization key is set: " + replayFile.getAbsolutePath() );
				else
					new NormalThread( "Replay auto-storer" ) {
						@Override
						public void run() {
							try {
								MainFrame.registerBackgroundJob();
								if ( !GeneralUtils.storeReplay( replayFile, authorizationKey ) && Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
									Sounds.playSoundSample( Sounds.SAMPLE_FILE_STORE_FAILED, false );
							} finally {
								MainFrame.removeBackgroundJob();
							}
						}
					}.start();
			}
			if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_AUTO_OPEN_NEW_REPLAYS ) )
				MainFrame.INSTANCE.openReplayFile( replayFile );
		}
	}
	
	/** Reference to the file monitor.  */
	private FileMonitor  fileMonitor;
	/** List of watched files.          */
	private List< File > watchedFileList;
	/** Reference to the file listener. */
	private FileListener fileListener;
	
	/**
	 * Initializes the file monitor.
	 * @return true if the file monitor is initialized properly; false if some error occurred
	 */
	private boolean setupFileMonitor() {
		synchronized ( ReplayAutoSaver.class ) {
			if ( fileMonitor == null ) {
				fileMonitor     = FileMonitor.getInstance();
				watchedFileList = new ArrayList< File >( 5 );
			}
			
			try {
				// Add watches
				for ( final File autoRepFolder : GeneralUtils.getAutoRepFolderList() ) {
					// Note: FILE_DELETED mask has to be specified too in order to receive FILE_CREATED events. Bug?
					fileMonitor.addWatch( autoRepFolder, FileMonitor.FILE_CREATED | FileMonitor.FILE_DELETED, true );
					watchedFileList.add( autoRepFolder );
				}
				
				fileMonitor.addFileListener( fileListener = new FileListener() {
					@Override
					public void fileChanged( final FileEvent event ) {
						if ( event.getType() == FileMonitor.FILE_CREATED ) {
							final File file = event.getFile();
							if ( file.isFile() && accept( file ) )
								lastReplayFromMonitor = file;
						}
					}
				} );
				
				return true;
			} catch ( final IOException ie ) {
				System.out.println( "Failed to setup File monitor, reverting to polling..." );
				ie.printStackTrace();
				
				shutdownFileMonitor();
				return false;
			}
		}
	}
	
	/**
	 * Shuts down the file monitor.
	 */
	private void shutdownFileMonitor() {
		synchronized ( ReplayAutoSaver.class ) {
			if ( fileMonitor != null ) {
				if ( fileListener != null )
					fileMonitor.removeFileListener( fileListener );
				fileListener = null;
				
				for ( final File watchedFile : watchedFileList )
					fileMonitor.removeWatch( watchedFile );
				
				fileMonitor.dispose();
				
				fileMonitor     = null;
				watchedFileList = null;
			}
		}
	}
	
	/**
	 * An IO file filter that accepts all directories and SC2Replay files that are newer than the one we last archived.
	 * @param pathname the abstract pathname to be tested
	 * @return true if the pathname denotes a directory or an SC2Replay file that is newer than the one we last archived
	 */
	@Override
	public boolean accept( final File pathname ) {
		return pathname.isDirectory() || 
			pathname.lastModified() > previousReplayDate && pathname.getName().toLowerCase().endsWith( ".sc2replay" );
	}
	
	@Override
	public void requestToCancel() {
		shutdownFileMonitor();
		super.requestToCancel();
	}
	
}
