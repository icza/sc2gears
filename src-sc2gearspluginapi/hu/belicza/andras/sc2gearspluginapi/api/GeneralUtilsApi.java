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
import hu.belicza.andras.sc2gearspluginapi.api.enums.SettingsTab;
import hu.belicza.andras.sc2gearspluginapi.api.httpost.IHttpPost;
import hu.belicza.andras.sc2gearspluginapi.api.listener.DownloaderCallback;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.importing.ReplaySpecification;
import hu.belicza.andras.sc2gearspluginapi.api.ui.IDownloader;

import java.awt.Frame;
import java.io.File;
import java.util.Map;

import javax.swing.JLabel;

/**
 * General utility services.
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GeneralServices
 */
public interface GeneralUtilsApi {
	
	/** Interface version. */
	String VERSION = "4.1";
	
	/**
	 * Restores the state of the main frame.<br>
	 * Restores the main frame if it is minimized to tray, and brings it to front.
	 * @see #hideMainFrame()
	 */
	void restoreMainFrame();
	
	/**
	 * Hides the main frame.<br>
	 * Minimizes the application to tray. 
	 * @see #restoreMainFrame()
	 */
	void hideMainFrame();
	
	/**
	 * Returns the last replay (the latest replay) specified by the auto-save folders
	 * (returned bye {@link InfoApi#getAutoRepFolderList()}.
	 * @return the last replay specified by the auto-save folders; or null if no replays found in the auto-rep folders
	 * @see InfoApi#getAutoRepFolderList()
	 * @see #getLastReplay(File, File)
	 */
	File getLastReplayFile();
	
	/**
	 * Returns the last replay (the latest replay) in the specified folder (recursive). 
	 * @param startFolder    folder to start searching in
	 * @param lastReplayFile the best candidate for being the last replay so far; optional (can be <code>null</code>)
	 * @return the last replay (the latest replay) in the specified folder (recursive) if there are newer file than <code>lastReplayFile</code>; else <code>lastReplayFile</code> is returned
	 * @see #getLastReplayFile()
	 */
	File getLastReplay( File startFolder, File lastReplayFile );
	
	/**
	 * Returns the default replay folder that should be set when opening/choosing replay(s).
	 * @return the default replay folder that should be set when opening/choosing replay(s)
	 */
	String getDefaultReplayFolder();
	
	/**
	 * Opens a replay file in the Replay analyzer.
	 * @param file replay file to be opened
	 * @see #openReplaysInMultiRepAnalysis(File[])
	 */
	void openReplayFile( File file );
	
	/**
	 * Opens replays in multi-replay analysis.
	 * @param files replay files to be opened
	 * @see #openReplayFile(File)
	 */
	void openReplaysInMultiRepAnalysis( File[] files );
	
	/**
	 * Opens files and folders in a Replay search.
	 * @param files         files and folders to be searched (these files will be added to the search source)
	 * @param performSearch tells whether the search should be performed, or only the activation of the filters tab is required
	 */
	void openReplaySearch( File[] files, boolean performSearch );
	
	/**
	 * Opens a replay specification in the Replay analyzer.
	 * @param replaySpec replay specification to be opened
	 * @see ReplaySpecification
	 */
	void openReplaySpecification( ReplaySpecification replaySpec );
	
	/**
	 * Creates and returns a link label which opens the miscellaneous settings dialog as a child of the main frame,
	 * and selects the specified tab.
	 * @param tabToSelect tab to select
	 * @return a link label which opens the miscellaneous settings dialog and selects the specified tab
	 * @since "2.0"
	 * @see #createLinkLabelToSettings(SettingsTab, Frame)
	 * @see SettingsTab
	 */
	JLabel createLinkLabelToSettings( SettingsTab tabToSelect );
	
	/**
	 * Creates and returns a link label which opens the miscellaneous settings dialog and selects the specified tab.
	 * @param tabToSelect tab to select
	 * @param owner       optional owner frame; it not provided the Main frame will be used
	 * @return a link label which opens the miscellaneous settings dialog and selects the specified tab
	 * @since "2.0"
	 * @see #createLinkLabelToSettings(SettingsTab)
	 * @see SettingsTab
	 */
	JLabel createLinkLabelToSettings( SettingsTab tabToSelect, Frame owner );
	
	/**
	 * Registers a background job.<br>
	 * Background jobs are checked before close and a confirmation window is shown if there are background jobs.<br>
	 * <br>
	 * If a background job is registered via this method, it should be inside a try-finally block,
	 * and {@link #removeBackgroundJob()} should be called in the finally block.
	 * @see #removeBackgroundJob()
	 */
	void registerBackgroundJob();
	
	/**
	 * Removes a background job.<br>
	 * Background jobs are checked before close and a confirmation window is shown if there are background jobs.
	 * @see #registerBackgroundJob()
	 */
	void removeBackgroundJob();
	
	/**
	 * Opens the web page specified by the URL in the system's default browser.
	 * @param url URL to be opened
	 */
	void showURLInBrowser( String url );
	
	/**
	 * Generates and returns a unique file name that does not exist.<br>
	 * If the provided file does not exist, it is returned.<br>
	 * If it exists, <code>" (2)"</code> will be appended to the end of the name. If this does not exist, it will be returned.<br>
	 * If it exists, <code>" (3)"</code> will be appended to the end of the original name. This will go on until a unique name is found;
	 * and then it is returned.
	 * 
	 * @param file file to be checked and returned a unique name for
	 * @return a unique file name that does not exist
	 */
	File generateUniqueName( File file );
	
	/**
	 * Copies the specified file to the target folder.<br>
	 * If target folder does not exist, first an attempt will be made to create it (recursively).<br>
	 * This copy method reserves the last modification date of the file.
	 * 
	 * @param sourceFile   source file to be copied
	 * @param targetFolder target folder to copy to
	 * @param buffer       optional buffer, if given it will be used for the I/O operations (if not provided, a new one will be created)
	 * @param targetName   optional name of the target file; if not given, the name of the source file will be used
	 * @return true if copy was successful; false otherwise
	 */
	boolean copyFile( File sourceFile, File targetFolder, byte[] buffer, String targetName );
	
	/**
	 * Downloads the specified URL to the specified file synchronously.
	 * @param url    URL to be downloaded
	 * @param toFile file to write the downloaded content to
	 * @return true if the download was successful; false otherwise
	 * @since "2.2"
	 * @see #getDownloader(String, File, boolean, DownloaderCallback)
	 */
	boolean downloadUrl( String url, File toFile );
	
	/**
	 * Returns a downloader which can be used to download a file asynchronously.
	 * @param url            URL of the file to be downloaded
	 * @param destination    destination file to download to
	 * @param useProgressBar tells if a progress bar should be created and updated
	 * @param callback       callback to be called when the download is finished/failed
	 * @return a downloader
	 * @see IDownloader
	 * @see DownloaderCallback
	 * @see #downloadUrl(String, File)
	 */
	IDownloader getDownloader( String url, File destination, boolean useProgressBar, DownloaderCallback callback );
	
	/**
	 * Converts the specified data to hex string.
	 * @param data data to be converted
	 * @return the specified data converted to hex string
	 */
	String convertToHexString( byte[] data );
	
	/**
	 * Converts the specified data to hex string.
	 * @param data   data to be converted
	 * @param offset offset of first byte to convert 
	 * @param length number of bytes to convert
	 * @return the specified data converted to hex string
	 * @since "4.1"
	 */
	String convertToHexString( byte[] data, int offset, int length );
	
	/**
	 * Calculates the MD5 digest of a file.
	 * @param file file whose MD5 digest to be calculated
	 * @return the calculated MD5 digest of the file
	 */
	String calculateFileMd5( File file );
	
	/**
	 * Calculates the SHA-1 digest of a file.
	 * @param file file whose SHA-1 digest to be calculated
	 * @return the calculated SHA-1 digest of the file
	 */
	String calculateFileSha1( File file );
	
	/**
	 * Calculates the SHA-256 digest of a file.
	 * @param file file whose SHA-256 to be calculated
	 * @return the calculated SHA-256 digest of the file
	 */
	String calculateFileSha256( File file );
	
	/**
	 * Returns the base64 encoded form of the specified file.
	 * 
	 * <p>This implementation uses the equal sign (<code>'='</code>) as the padding character.</p>
	 * 
	 * <p>More info about Base64: <a href="http://en.wikipedia.org/wiki/Base64">Base64 on Wikipedia</a></p>
	 * 
	 * @param file file to be encoded
	 * @return the base64 encoded form of the specified file; or <code>null</code> if some error occurs
	 * @since "2.2"
	 */
	String encodeFileBase64( File file );
	
	/**
	 * Creates a new {@link IHttpPost}.
	 * @param urlString URL string to post to
	 * @param paramsMap map of parameters to be sent
	 * @return an {@link IHttpPost} to interact with the HTTP POST request
	 * @since "2.2"
	 * @see IHttpPost
	 */
	IHttpPost createHttpPost( String urlString, Map< String, String > paramsMap );
	
}
