/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet that serves requests from a zip file.
 * 
 * <p>The servlet uses init parameters specified in the <code>web.xml</code> descriptor.<br/>
 * The following parameters are used:
 * <ul>
 * 	<li><b><code>zipFile</code>:</b> Zip file to serve content from</li>
 * 	<li><b><code>baseDir</code>:</b> Base directory in the zip file to use as root. Optional, if not specified, nothing will be pre-pended to requested files.</li>
 * 	<li><b><code>welcomeFile</code>:</b> Optional welcome file to serve if no file name is specified; relative to <code>baseDir</code></li>
 * </ul></p>
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class UnzipperServlet extends BaseServlet {
	
	private static final Logger LOGGER = Logger.getLogger( UnzipperServlet.class.getName() );
	
	/** File name map to to find out MIME types of files.                                          */
	private FileNameMap fileNameMap;
	/** Zip file to serve content from.                                                            */
	private ZipFile     zipFile;
	/** Base directory in the zip file to use as root.                                             */
	private String      baseDir;
	/** Optional welcome file to serve if no file name is specified; relative to {@link #baseDir}. */
	private String      welcomeFile;
	
	@Override
	public void init() throws ServletException {
		final String zipFileName = getInitParameter( "zipFile"     );
		baseDir                  = getInitParameter( "baseDir"     );
		welcomeFile              = getInitParameter( "welcomeFile" );
		
		if ( baseDir != null && !baseDir.isEmpty() && baseDir.charAt( baseDir.length() - 1 ) != '/' )
			baseDir += '/';
		
		try {
	        zipFile     = new ZipFile( new File( getServletContext().getResource( zipFileName ).toURI() ) );
        	fileNameMap = URLConnection.getFileNameMap();
        } catch ( final Exception e ) {
			LOGGER.log( Level.SEVERE, "Exception during initialization, error processing: " + zipFileName, e );
        }
	}
	
	@Override
	public void destroy() {
		if ( zipFile != null )
	        try {
	            zipFile.close();
            } catch ( final IOException ie ) {
    			LOGGER.log( Level.SEVERE, "Exception during destroy!", ie );
            }
		
	    super.destroy();
	}
	
	@Override
	protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		if ( zipFile == null ) {
			LOGGER.log( Level.SEVERE, "The requested file is temporarily unavailable: No internal zip file!" );
			response.sendError( HttpServletResponse.SC_NOT_FOUND, "The requested file is temporarily unavailable!" );
			return;
		}
		
		String fileName = request.getPathInfo();
		if ( fileName != null && !fileName.isEmpty() )
			fileName = fileName.substring( 1 ); // Cut off leading slash
		
		if ( fileName == null || fileName.isEmpty() )
			fileName = welcomeFile;
		
		final ZipEntry zipEntry = fileName == null || fileName.isEmpty() ? null : zipFile.getEntry( baseDir == null ? fileName : baseDir + fileName );
		if ( zipEntry == null ) {
			LOGGER.warning( "The requested file could not be found on the server!" );
			response.sendError( HttpServletResponse.SC_NOT_FOUND, "The requested file could not be found on the server!" );
			return;
		}
		
		if ( zipEntry.isDirectory() ) {
			LOGGER.warning( "You cannot list directory content!" );
			response.sendError( HttpServletResponse.SC_FORBIDDEN, "You cannot list directory content!" );
			return;
		}
		
		// css is not handled properly by the file name map returned by URLConnection.getFileNameMap():
		if ( fileName.toLowerCase().endsWith(".css" ) )
			response.setContentType( "text/css" );
		else
			response.setContentType( fileNameMap.getContentTypeFor( fileName ) );
		response.setContentLength( (int) zipEntry.getSize() );
		
		setNoCache( response );
		
		try ( final InputStream input = zipFile.getInputStream( zipEntry ) ) {
			if ( input == null ) {
				// This is the case if fileName denotes a directory but does not end with a slash ('/').
				// ZipEntry.isDirectory() only reports true if it ends with a slash.
				LOGGER.warning( "The requested resource is not available!" );
				response.sendError( HttpServletResponse.SC_FORBIDDEN, "The requested resource is not available!" );
				return;
			}
			
			final OutputStream output = response.getOutputStream();
			
			int data;
			while ( ( data = input.read() ) != -1 )
				output.write( data );
		}
	}
	
}
