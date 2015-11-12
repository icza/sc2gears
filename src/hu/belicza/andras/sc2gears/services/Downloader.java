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
import hu.belicza.andras.sc2gears.util.ControlledThread;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gearspluginapi.api.listener.DownloaderCallback;
import hu.belicza.andras.sc2gearspluginapi.api.ui.IDownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JProgressBar;

/**
 * This is an automated downloader.
 * 
 * <p>Downloads a file from a specified URL in a separate thread.
 * First writes it to a temporary file, and copies it to the destination if downloading was successful.</p>
 * 
 * @author Andras Belicza
 */
public class Downloader extends ControlledThread implements IDownloader {
	
	/** URL to be downloaded.                               */
	private final String             url;
	/** Destination file to write to.                       */
	private final File               destination;
	/** Callback to be notified when the download finishes. */
	private final DownloaderCallback callback;
	
	/** Progress bar to indicate the download status.       */
	private final JProgressBar       progressBar;
	
	/**
	 * Creates a new Downloader.
	 * @param url         url to be downloaded
	 * @param destination destination file to write to
	 * @param callback    callback to be notified when the download finishes
	 */
	public Downloader( final String url, final File destination, final boolean useProgressBar, final DownloaderCallback callback ) {
		super( "Downloader" );
		
		this.url         = url;
		this.destination = destination;
		this.callback    = callback;
		
		progressBar      = useProgressBar ? new JProgressBar() : null;
		
		if ( progressBar != null ) {
			progressBar.setString( "" ); // Do not show anything until we know the size (or the lack of size info) 
			progressBar.setStringPainted( true );
		}
	}
	
	/**
	 * We do our job in a new thread.
	 */
	public void run() {
		InputStream  input    = null;
		File         tempFile = null; 
		OutputStream output   = null;
		
		try {
			final URLConnection archiveUrlConnection = new URL( url ).openConnection();
			final int length = archiveUrlConnection.getContentLength();
			if ( progressBar != null ) {
				if ( length < 0 )
					progressBar.setString( Language.getText( "general.downloadingSizeUnknown" ) );
				else {
					progressBar.setString( null ); // This turns on the default percent string
					progressBar.setMaximum( length );
				}
			}
			
			input    = archiveUrlConnection.getInputStream();
			tempFile = File.createTempFile( "Sc2gears", null );
			tempFile.deleteOnExit();
			output   = new FileOutputStream( tempFile );
			
			int totalBytesRead = 0;
			int bytesRead;
			final byte[] buffer = new byte[ 8192 ];
			while ( !requestedToCancel && ( bytesRead = input.read( buffer ) ) > 0 ) {
				output.write( buffer, 0, bytesRead );
				totalBytesRead += bytesRead;
				if ( progressBar != null && length > 0 )
					progressBar.setValue( totalBytesRead );
			}
			output.flush();
			if ( progressBar != null && !requestedToCancel && length < 0 ) {
				progressBar.setValue( progressBar.getMaximum() );
				progressBar.setString( null ); // This turns on the default percent string
			}
			
			if ( requestedToCancel ) {
				callback.downloadFinished( false );
				return;
			}
			
			// Now copy the file to its destination
			callback.downloadFinished( GeneralUtils.copyFile( tempFile, destination.getParentFile(), buffer, destination.getName() ) );
		}
		catch ( final Exception e ) {
			e.printStackTrace();
			callback.downloadFinished( false );
		}
		finally {
			if ( output   != null )
				try { output.close(); } catch ( final IOException ie ) {}
			if ( tempFile != null )
				tempFile.delete();
			if ( input    != null )
				try { input .close(); } catch ( final IOException ie ) {}
		}
	}
	
	@Override
	public JProgressBar getProgressBar() {
		return progressBar;
	}
	
	@Override
	public void startDownload() {
		start();
	}
	
}
