/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.ui;

import java.io.File;

import hu.belicza.andras.sc2gearspluginapi.api.GeneralUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.listener.DownloaderCallback;

import javax.swing.JProgressBar;

/**
 * A downloader which can be used to download a file asynchronously.
 * 
 * <p>Provides a {@link JProgressBar} to visualize the status, and makes a callback when download finishes/fails.</p>
 * 
 * <p>Example usage:<br>
 * <blockquote><pre>
 * String url = "http://some.site.com/files/something.zip";
 * File file = new File( "c:\\downloads\\something.zip" );
 * 
 * IDownloader downloader = generalServices.getGeneralUtilsApi().getDownloader( url, file, true, new DownloaderCallback() {
 *     public void downloadFinished( boolean success ) {
 *         if ( success ) {
 *             // Do something with the file
 *         }
 *         else
 *             System.out.println( "Failed to download file: http://some.site.com/files/something.zip" );
 *     }
 * } );
 * 
 * // Add the progress bar to your GUI:
 * getContentPane().add( downloader.getProgressBar(), BorderLayout.SOUTH );
 * 
 * downloader.startDownload();
 * 
 * // downloader uses a separate thread, you can do something else here...
 * </pre></blockquote></p>
 * 
 * @author Andras Belicza
 * 
 * @see GeneralUtilsApi#getDownloader(String, File, boolean, DownloaderCallback)
 * @see DownloaderCallback
 */
public interface IDownloader {
	
	/**
	 * Returns the progress bar that shows the download status.
	 * @return the progress bar that shows the download status
	 */
	JProgressBar getProgressBar();
	
	/**
	 * Starts the download.
	 */
	void startDownload();
	
	/**
	 * Requests the cancel of the download.
	 */
	void requestToCancel();
	
}
