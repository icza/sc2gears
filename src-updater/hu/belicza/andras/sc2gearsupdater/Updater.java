/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsupdater;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.shared.SharedConsts;
import hu.belicza.andras.sc2gears.shared.SharedUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This is an updater utility for Sc2gears.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class Updater extends JFrame {
	
	/** URL of the Downloads page. */
	public static final String URL_DOWNLOADS          = "https://sites.google.com/site/sc2gears/downloads";
	
	
	/** Lib folder.                */
	private static final File FOLDER_LIB              = new File( "lib" ).getAbsoluteFile();
	/** Current folder.            */
	private static final File FOLDER_CURRENT          = FOLDER_LIB.getParentFile(); // If System.getProperty( "user.dir" ) is used, File.equals() doesn't work for virtual (subst) drives
	/** Update cache folder.       */
	private static final File FOLDER_UPDATE_CACHE     = new File( "_Update cache" ).getAbsoluteFile();
	/** User content folder.       */
	private static final File FOLDER_USER_CONTENT     = new File( SharedConsts.FOLDER_USER_CONTENT ).getAbsoluteFile();
	/** Sc2gears jar.              */
	private static final File SC2GEARS_JAR            = new File( FOLDER_LIB, "Sc2gears.jar" ).getAbsoluteFile();
	/** Archive Sc2gears folder.   */
	private static final File FOLDER_ARCHIVE_SC2GEARS = new File( FOLDER_UPDATE_CACHE, "Sc2gears" );
	/** Archive updater lib file.  */
	private static final File ARCHIVE_UPDATER_LIB     = new File( FOLDER_ARCHIVE_SC2GEARS, SharedConsts.LIB_UPDATER_FOLDER.getName() + "/" + SharedConsts.LIB_UPDATER_NAME );
	
	/** Files of Sc2gears. Only these can be patched/deleted. */
	private static final String[] SC2GEARS_FILE_NAMES = {
		"lib/jl1.0.1.jar",
		"lib/jna.jar",
		"lib/mp3spi1.9.4.jar",
		"lib/OfficeLnFs_2.7.jar",
		"lib/platform.jar",
		"lib/Sc2gears.jar",
		"lib/squareness.jar",
		"lib/tritonus_share.jar",
		"lib",
		"Languages/Chinese.xml",
		"Languages/ChineseTraditional.xml",
		"Languages/Dutch.xml",
		"Languages/English.xml",
		"Languages/French.xml",
		"Languages/German.xml",
		"Languages/Hungarian.xml",
		"Languages/Italian.xml",
		"Languages/Japanese.xml",
		"Languages/Korean.xml",
		"Languages/Polish.xml",
		"Languages/Portuguese-BR.xml",
		"Languages/Russian.xml",
		"Languages/Spanish.xml",
		"Languages/Swedish.xml",
		"Languages",
		"Plugins/The Sound of Victory/the-sound-of-victory-plugin.jar",
		"Plugins/The Sound of Victory/Sc2gears-plugin.xml",
		"Plugins/The Sound of Victory",
		"Plugins/Build Orders Table/build-orders-table-plugin.jar",
		"Plugins/Build Orders Table/Sc2gears-plugin.xml",
		"Plugins/Build Orders Table",
		"Plugins/readme.txt",
		"Plugins",
		SharedConsts.EXECUTABLE_NAME_WIN_BATCH,
		SharedConsts.EXECUTABLE_NAME_OS_X,
		SharedConsts.EXECUTABLE_NAME_WIN,
		SharedConsts.EXECUTABLE_NAME_UNIX,
		"LICENSE.txt"
	};
	
	/** Updater log file. */
	private static final File LOG_FILE = new File( SharedConsts.FOLDER_LOGS, "Updater " + new SimpleDateFormat( "yyyy-MM-dd HH-mm-ss" ).format( new Date() ) + SharedConsts.LOG_FILE_EXT );
	
	/** Date time format used for logging. */
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss - " );
	
	/**
	 * Entry point of the program.
	 * 
	 * @param arguments the first argument must be the current version of Sc2gears
	 */
	public static void main( final String[] arguments ) {
		if ( arguments.length < 4 ) { // 4 arguments at least: latest version, required min updater version, archive SHA-256 and at least 1 archive URL
			// Wrong argument count is assumed to be the result of an attempt to manually start the Updater.
			InfoMainClass.main( null );
			System.exit( 0 );
		}
		
		// Add Sc2gears Updater and Sc2gears version to the User-Agent HTTP request property.
		// The final user agent string will be the value of this property + the default (which is the Java version).
		// The format of the user agent string is a list of "product/version" pairs separated with a space
		System.setProperty( "http.agent", "Sc2gearsUpdater/" + SharedConsts.UPDATER_VERSION
				+ " (" + System.getProperty( "os.name" ) + "; " + System.getProperty( "os.version" ) + "; " + System.getProperty( "os.arch" ) + ")"
				+ " " + Consts.APPLICATION_NAME + "/" + Consts.APPLICATION_VERSION + "" ); // Consts is outside of the src-updater, but these consts will be inlided!
		
		// Init logging
		try {
			// Make sure log folder exists
			final File logFolder = LOG_FILE.getParentFile();
			if ( !logFolder.exists() )
				logFolder.mkdirs();
			
			final PrintStream logStream = new PrintStream( new FileOutputStream( LOG_FILE ), true );
			System.setOut( logStream );
			System.setErr( logStream );
		} catch ( final Exception e ) {
			// Proceed even if failed to init logging
		}
		
		System.out.println( TIME_FORMAT.format( new Date() )
			+ "Starting " + SharedConsts.UPDATER_NAME + " " + SharedConsts.UPDATER_VERSION ); // This is redirected to the updater log
		
		InfoMainClass.setLaf();
		
		
		new Updater( arguments[ 0 ], arguments[ 1 ], arguments[ 2 ], Arrays.copyOfRange( arguments, 3, arguments.length ) );
	}
	
	/** Log text area.                              */
	private final JTextArea    logTextArea                 = new JTextArea();
	/** Progress bar showing the download progress. */
	private final JProgressBar dlProgressBar               = new JProgressBar();
	/** The control buttons panel.                  */
	private final JPanel       controlButtonsPanel         = new JPanel();
	/** Close and start Sc2gears button.            */
	private final JButton      closeAndStartSc2gearsButton = new JButton( "Close and Start " + SharedConsts.APPLICATION_NAME, new ImageIcon( SharedUtils.SC2GEARS.getImage().getScaledInstance( 16, 16, Image.SCALE_SMOOTH ) ) );
	/** Close button.                               */
	private final JButton      closeButton                 = new JButton( "Close", SharedUtils.DOOR_OPEN_IN );
	/** Abort button.                               */
	private final JButton      abortButton                 = new JButton( "Abort", SharedUtils.CROSS_OCTAGON );
	
	/** Indicates that the update is in progress.    */
	private volatile boolean updateInProgress;
	/** Indicates that the update cannot be aborted. */
	private volatile boolean abortAllowed;
	
	/** Work buffer. */
	private final byte[] buffer = new byte[ 8192 ];
	
	/**
	 * Creates a new Updater.
	 * @param latestVersion         latest Sc2gears version
	 * @param requiredMinUpdaterVer required minimum updater version to update to the latest version
	 * @param arhciveSha256         SHA-256 digest of the archive
	 * @param archiveUrls           URLs of the latest archive file
	 */
	public Updater( final String latestVersion, final String requiredMinUpdaterVer, final String archiveSha256, final String[] archiveUrls ) {
		super( SharedConsts.UPDATER_NAME + " " + SharedConsts.UPDATER_VERSION );
		
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing( final WindowEvent event ) {
				exit();
			}
		} );
		
		setIconImage( SharedUtils.SC2GEARS.getImage() );
		
		buildGUI();
		
		pack();
		SharedUtils.centerWindow( this );
		setVisible( true );
		
		startUpdate( latestVersion, requiredMinUpdaterVer, archiveSha256, archiveUrls );
	}
	
	/**
	 * Builds the GUI of the frame.
	 */
	private void buildGUI() {
		final JPanel contentPane = new JPanel( new BorderLayout() );
		contentPane.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		setContentPane( contentPane );
		
		final JPanel northPanel = new JPanel( new BorderLayout() );
		JPanel wrapper = new JPanel();
		wrapper.add( SharedUtils.createAnimatedLogoLabel() );
		northPanel.add( wrapper, BorderLayout.CENTER );
		wrapper = new JPanel();
		wrapper.setBorder( BorderFactory.createEmptyBorder( 10, 0, 10, 0 ) );
		final JLabel infoLabel = new JLabel( SharedConsts.APPLICATION_NAME + "™ © " + SharedConsts.AUTHOR_FIRST_NAME + " " + SharedConsts.AUTHOR_LAST_NAME + ", 2010-2014", JLabel.CENTER );
		infoLabel.setFont( infoLabel.getFont().deriveFont( Font.ITALIC ) );
		wrapper.add( infoLabel );
		northPanel.add( wrapper, BorderLayout.SOUTH );
		contentPane.add( northPanel, BorderLayout.NORTH );
		
		logTextArea.setEditable( false );
		final JScrollPane logScrollPane = new JScrollPane( logTextArea );
		logScrollPane.setPreferredSize( new Dimension( 700, 310 ) );
		contentPane.add( logScrollPane, BorderLayout.CENTER );
		
		final Box southBox = Box.createVerticalBox();
		
		final JPanel dlStatusPanel = new JPanel();
		dlStatusPanel.add( new JLabel( "Download status:" ) );
		dlProgressBar.setString( "" ); // Does not show anything until we know the size (or the lack of size info)
		dlProgressBar.setStringPainted( true );
		dlProgressBar.setPreferredSize( new Dimension( 300, 23 ) );
		dlStatusPanel.add( dlProgressBar );
		southBox.add( dlStatusPanel );
		
		final JPanel linksPanel = new JPanel();
		linksPanel.add( SharedUtils.createLinkLabel( "View version history", SharedConsts.URL_VERSION_HISTORY ) );
		linksPanel.add( Box.createHorizontalStrut( 10 ) );
		linksPanel.add( SharedUtils.createLinkLabel( "View download page"  , URL_DOWNLOADS                    ) );
		linksPanel.add( Box.createHorizontalStrut( 10 ) );
		linksPanel.add( SharedUtils.createLinkLabel( "Visit home page"     , SharedConsts.URL_HOME_PAGE       ) );
		southBox.add( linksPanel );
		
		closeAndStartSc2gearsButton.setEnabled( false );
		closeAndStartSc2gearsButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				if ( SharedUtils.isWindows() || SharedUtils.isMac() || SharedUtils.isUnix() ) {
					if ( SharedUtils.isMac() || SharedUtils.isUnix() )
						try {
							// Set executable permission on the starter script
							final Process chmodProcess = Runtime.getRuntime().exec( SharedUtils.isMac() ? "chmod +x " + SharedConsts.EXECUTABLE_NAME_OS_X : SharedUtils.isUnix() ? "chmod +x " + SharedConsts.EXECUTABLE_NAME_UNIX : "" );
							// Wait for the chmod command to be executed, else starting Sc2gears with this script might fail with something like:
							// java.io.IOException: Cannot run proram "./Sc2gears-linux.sh": java.io.IOException: error=13, Permission denied
							chmodProcess.waitFor();
						} catch ( final IOException ie ) {
							ie.printStackTrace();
						} catch ( InterruptedException e ) {
	                        e.printStackTrace();
                        }
					try {
						Runtime.getRuntime().exec( SharedUtils.getSc2gearsStartCommand() );
					} catch ( final Exception e ) {
						JOptionPane.showMessageDialog( null, e );
						e.printStackTrace();
					}
				}
				exit();
			}
		} );
		controlButtonsPanel.add( closeAndStartSc2gearsButton );
		
		closeButton.setEnabled( false );
		closeButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				exit();
			}
		} );
		controlButtonsPanel.add( closeButton );
		
		abortButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				exit();
			}
		} );
		controlButtonsPanel.add( abortButton );
		
		southBox.add( controlButtonsPanel );
		
		contentPane.add( southBox, BorderLayout.SOUTH );
	}
	
	/**
	 * Asks confirmation for exit, and exits if confirmed and allowed.
	 */
	private void exit() {
		if ( updateInProgress && abortAllowed )
			if ( JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog( Updater.this, new String[] { "Are you sure you want to abort the update?", "Hit 'No' to continue." }, "Warning!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) )
				return;
		
		// We check again because the user response might take some time while the abortAllowed status changed
		if ( updateInProgress && !abortAllowed ) {
			JOptionPane.showMessageDialog( this, new String[] { "The Update process cannot be aborted at this time without damaging " + SharedConsts.APPLICATION_NAME + "!" }, "Error!", JOptionPane.ERROR_MESSAGE );
			return;
		}
		
		System.exit( 0 );
	}
	
	/**
	 * Starts the update process.
	 * 
	 * @param latestVersion         latest Sc2gears version
	 * @param requiredMinUpdaterVer required minimum updater version to update to the latest version
	 * @param arhciveSha256         SHA-256 digest of the archive
	 * @param archiveUrls           URLs of the latest archive file
	 */
	private void startUpdate( final String latestVersion, final String requiredMinUpdaterVer, final String archiveSha256, final String[] archiveUrls ) {
		updateInProgress = true;
		abortAllowed     = true;
		
		try {
			logTextArea.append( "If the update fails and you cannot start " + SharedConsts.APPLICATION_NAME + ", visit the home page and download it manually.\n" );
			logTextArea.append( "A detailed log is saved to the file:\n     " + LOG_FILE.getAbsolutePath() + "\n\n" );
			
			logMessage( "Available latest version: " + latestVersion );
			
			if ( strictCompareVersions( parseVersion( SharedConsts.UPDATER_VERSION ), parseVersion( requiredMinUpdaterVer ) ) < 0 ) {
				logMessage( "ERROR! The update cannot be performed!" );
				logMessage( "This update requires a newer version of the " + SharedConsts.UPDATER_NAME + " (" + requiredMinUpdaterVer + ")!" );
				logMessage( "Please download the latest version manually." );
				throw new Exception();
			}
			
			logMessage( "Preparing for update..." );
			if ( !SC2GEARS_JAR.exists() ) {
				logMessage( "It appears you did not start Sc2gears from its folder, aborting update!" );
				throw new Exception();
			}
			while ( !deleteFile( FOLDER_UPDATE_CACHE, true ) ) {
				if ( JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog( this, new String[] { "Could not delete folder:", FOLDER_UPDATE_CACHE.getAbsolutePath(), " ", "Retry?" }, "Error!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE ) ) {
					logMessage( "Failed to delete file: " + FOLDER_UPDATE_CACHE.getAbsolutePath() );
					throw new Exception();
				}
			}
			while ( !FOLDER_UPDATE_CACHE.mkdir() ) {
				if ( JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog( this, new String[] { "Could not create folder:", FOLDER_UPDATE_CACHE.getAbsolutePath(), " ", "Retry?" }, "Error!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE ) ) {
					logMessage( "Could not create update cache folder!" );
					throw new Exception();
				}
			}
			
			final File ARCHIVE_FILE = new File( FOLDER_UPDATE_CACHE, "archive.zip" );
			for ( int i = 0; i < archiveUrls.length; i++ ) {
				final String  archiveSource  = i == 0 ? "main source" : " mirror #" + i;
				final boolean lastArchiveUrl = i == archiveUrls.length - 1;
				
				logMessage( "Downloading archive from " + archiveSource + "..." );
				
				boolean downloadOk = false;
				InputStream  input  = null;
				OutputStream output = null;
				try {
					dlProgressBar.setString( "Connecting..." );
					dlProgressBar.setValue( 0 );
					
					final URLConnection archiveUrlConnection = new URL( archiveUrls[ i ] ).openConnection();
					final int length = archiveUrlConnection.getContentLength();
					if ( length < 0 )
						dlProgressBar.setString( "Downloading... (size unknown)" );
					else {
						dlProgressBar.setString( null ); // This turns on the default percent string
						dlProgressBar.setMaximum( length );
					}
					
					input  = archiveUrlConnection.getInputStream();
					output = new FileOutputStream( ARCHIVE_FILE );
					
					int totalBytesRead = 0;
					int bytesRead;
					while ( ( bytesRead = input.read( buffer ) ) > 0 ) {
						output.write( buffer, 0, bytesRead );
						totalBytesRead += bytesRead;
						if ( length > 0 )
							dlProgressBar.setValue( totalBytesRead );
					}
					if ( length < 0 ) {
						dlProgressBar.setValue( dlProgressBar.getMaximum() );
						dlProgressBar.setString( null ); // This turns on the default percent string
					}
					output.flush();
					
					downloadOk = true;
					logMessage( "Download complete." );
				}
				catch ( final Exception e ) {
					logMessage( "Failed to download archive from " + archiveSource + "!" + ( lastArchiveUrl ? "" : " Proceeding to the next source." ) );
					e.printStackTrace();
					if ( lastArchiveUrl )
						throw new Exception( "None of the archives are available!" );
				} finally {
					if ( input != null ) {
						try { input.close(); } catch ( final IOException ie ) {}
						input = null;
					}
					if ( output != null ) {
						try { output.close(); } catch ( final IOException ie ) {}
						output = null;
					}
				}
				
				if ( downloadOk ) {
					logMessage( "Checking SHA-256 checksum of the archive..." );
					if ( archiveSha256.equals( SharedUtils.calculateFileSha256( ARCHIVE_FILE ) ) ) {
						logMessage( "SHA-256 checksum OK." );
						break; // Break archive URLs cycle
					}
					else {
						logMessage( "SHA-256 checksums do not match!" );
						if ( lastArchiveUrl ) {
							logMessage( "To protect your computer, the update process has been aborted!" );
							logMessage( "You can start the update process again if you think this is due to a download error." );
							logMessage( "If the SHA-256 checksum test fails repeatedly, please contact the author." );
							throw new Exception( "SHA-256 checksum test failed, aborting update!" );
						}
						else {
							logMessage( "To protect your computer, the downloaded archive is discarded. Proceeding to the next source." );
						}
					}
				}
			}
			
			logMessage( "Extracting archive..." );
			try ( final ZipInputStream zipInput = new ZipInputStream( new FileInputStream( ARCHIVE_FILE ) ) ) {
				ZipEntry zipEntry;
				while ( ( zipEntry = zipInput.getNextEntry() ) != null ) {
					final File entryFile = new File( FOLDER_UPDATE_CACHE, zipEntry.getName() );
					if ( zipEntry.isDirectory() ) {
						entryFile.mkdirs();
					}
					else {
						long size = zipEntry.getSize();
						try ( final OutputStream output = new FileOutputStream( entryFile ) ) {
							while ( size > 0 ) {
								final int bytesRead = zipInput.read( buffer );
								output.write( buffer, 0, bytesRead );
								size -= bytesRead;
							}
							output.flush();
						}
					}
				}
				logMessage( "Extracting done." );
				if ( !FOLDER_ARCHIVE_SC2GEARS.exists() ) {
					logMessage( "Error: the extracted archive does not seem to be a valid archive! Aborting update!" );
					throw new Exception();
				}
			} catch ( final Exception e ) {
				logMessage( "Failed to extract archive!" );
				throw e;
			}
			
			logMessage( "Replacing/patching files..." );
			abortAllowed = false;
			abortButton.setEnabled( false );
			// First delete old files
			for ( final String sc2gearsFileName : SC2GEARS_FILE_NAMES ) {
				final File file = new File( FOLDER_CURRENT, sc2gearsFileName );
				while ( !deleteFile( file, false ) ) {
					if ( JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog( this, new String[] { "Could not delete file or folder:", file.getAbsolutePath(), " ", "At this point your Sc2gears is probably damaged.", "Retry?" }, "Error!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE ) ) {
						logMessage( "Failed to delete file or folder: " + file.getAbsolutePath() );
						logMessage( "Your " + SharedConsts.APPLICATION_NAME + " is probably damaged now." );
						throw new Exception();
					}
				}
			}
			// Next rename the new updater
			final File newArchiveUpdaterLib = new File( ARCHIVE_UPDATER_LIB.getParent(), SharedConsts.NEW_LIB_UPDATER_NAME );
			while ( !ARCHIVE_UPDATER_LIB.renameTo( newArchiveUpdaterLib ) ) {
				if ( JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog( this, new String[] { "Could not rename file:", ARCHIVE_UPDATER_LIB.getAbsolutePath(), "To:", newArchiveUpdaterLib.getAbsolutePath(), " ", "At this point your " + SharedConsts.APPLICATION_NAME + " is probably damaged.", "Retry?" }, "Error!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE ) ) {
					logMessage( "Failed to rename file: " + ARCHIVE_UPDATER_LIB.getAbsolutePath() );
					logMessage( "Your " + SharedConsts.APPLICATION_NAME + " is probably damaged now." );
					throw new Exception();
				}
			}
			// Now copy the new ones
			for ( final File sourceFile : FOLDER_ARCHIVE_SC2GEARS.listFiles() ) {
				if ( !sourceFile.getName().equals( FOLDER_USER_CONTENT.getName() ) )
					while ( !copyFile( sourceFile, FOLDER_CURRENT ) ) {
						if ( JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog( this, new String[] { "Could not copy file or folder from:", sourceFile.getAbsolutePath(), "To:", FOLDER_CURRENT.getAbsolutePath(), " ", "At this point your " + SharedConsts.APPLICATION_NAME + " is probably damaged.", "Retry?" }, "Error!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE ) ) {
							logMessage( "Failed to copy file or folder: " + sourceFile.getAbsolutePath() );
							logMessage( "Your " + SharedConsts.APPLICATION_NAME + " is probably damaged now." );
							throw new Exception();
						}
					}
			}
			abortAllowed = true;
			logMessage( "Replace/patch complete." );
			
			logMessage( "Cleaning up..." );
			if ( !deleteFile( FOLDER_UPDATE_CACHE, true ) )
				logMessage( "Warning: there were some errors during cleanup." );
			else
				logMessage( "Cleanup complete." );
			
			logMessage( SharedConsts.APPLICATION_NAME + " has been successfully updated to version: " + latestVersion );
		} catch ( final Exception e ) {
			e.printStackTrace();
			logMessage( "Update FAILED! Check the log file for details." );
		} finally {
			closeAndStartSc2gearsButton.setEnabled( true );
			closeAndStartSc2gearsButton.requestFocusInWindow();
			closeButton.setEnabled( true );
			abortButton.setEnabled( false );
			
			updateInProgress = false;
		}
	}
	
	/**
	 * Deletes the specified file or directory.<br>
	 * If the file denotes a directory which is not empty, it will not be deleted.
	 * @param file file to be removed
	 * @param recursive tells if deletion should be performed recursively
	 * @return true if the file or directory tree does not exist, or was successfully deleted, or if file denotes a directory but it contains custom files; false otherwise
	 */
	private static boolean deleteFile( final File file, final boolean recursive ) {
		if ( !file.exists() )
			return true;
		
		if ( file.isFile() )
			return file.delete();
		else {
			final File[] list = file.listFiles();
			if ( recursive ) {
				// It's a folder. First delete its content
				for ( final File childFile : list ) {
					if ( !deleteFile( childFile, true ) )
						return false;
				}
				// Now the folder
				return file.delete();
			}
			else {
				if ( list.length == 0 )
					return file.delete();
				else
					return true; // There are other custom files in it, do not delete the folder
			}
			
		}
	}
	
	/**
	 * Recursively copies the specified file or directory.
	 * @param sourceFile        source file or folder to be copied
	 * @param destinationFolder destination folder to copy the specified file or folder to; it must exist
	 * @return true if the file or directory tree was successfully copied; false otherwise
	 */
	private boolean copyFile( final File sourceFile, final File destinationFolder ) {
		final File targetFile = new File( destinationFolder, sourceFile.getName() ).getAbsoluteFile();
		if ( sourceFile.isDirectory() ) {
			if ( !targetFile.exists() ) {
				if ( !targetFile.mkdir() )
					return false;
			}
			else
				if ( targetFile.isFile() )
					return false;
			
			for ( final File file : sourceFile.listFiles() ) {
				if ( !copyFile( file, targetFile ) )
					return false;
			}
			return true;
		}
		else {
			// It is a file
			try ( final OutputStream output = new FileOutputStream( targetFile ); final InputStream  input  = new FileInputStream ( sourceFile ) ) {
				int readBytes;
				while ( ( readBytes = input.read( buffer ) ) > 0 )
					output.write( buffer, 0, readBytes );
				output.flush();
				
				return true;
			} catch ( final Exception e ) {
				return false;
			}
		}
	}
	
	/**
	 * Tries to parse the version from the given text.
	 * @param text text to parse the version from
	 * @return the parsed version or <code>null</code> if text is not a valid version
	 */
	private static int[] parseVersion( final String text ) {
		try {
			final StringTokenizer tokenizer  = new StringTokenizer( text, "." );
			final List< Integer > numberList = new ArrayList< Integer >( 4 );
			
			while ( tokenizer.hasMoreTokens() ) {
				final int n = Integer.parseInt( tokenizer.nextToken() );
				if ( n < 0 )
					return null;
				numberList.add( n );
			}
			
			final int[] version = new int[ numberList.size() ];
			for ( int i = 0; i < version.length; i++ )
				version[ i ] = numberList.get( i );
			
			return version;
		} catch ( final Exception e ) {
			return null;
		}
	}
	
	/**
	 * Compares 2 versions.
	 * 
	 * <p>This method performs a "hard" check: declares <code>"2.0"</code> less than <code>"2.0.1"</code> for example!</p>
	 * 
	 * @param v1 v1 to be compared
	 * @param v2 v2 to compare to
	 * @return a positive int if v1 > v2, 0 if v1 == v2, a negative int if v1 < v2
	 * 
	 * @see #compareVersions(int[], int[])
	 */
	private static int strictCompareVersions( final int[] v1, final int[] v2 ) {
		for ( int i = 0; i < v1.length && i < v2.length; i++ )
			if ( v1[ i ] != v2[ i ] )
				return v1[ i ] - v2[ i ];
		
		// If any of the versions has more components, that one is the greater
		return v1.length - v2.length;
	}
	
	/**
	 * Logs a message.
	 * @param message message to be logged
	 */
	private void logMessage( String message ) {
		message = TIME_FORMAT.format( new Date() ) + message;
		logTextArea.append( message + "\n" );
		logTextArea.setCaretPosition( logTextArea.getDocument().getLength() );
		System.out.println( message ); // This is redirected to the updater log
	}
	
}
