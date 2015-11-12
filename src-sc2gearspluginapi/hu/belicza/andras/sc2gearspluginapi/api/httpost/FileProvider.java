/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.httpost;

import java.io.File;
import java.net.HttpURLConnection;

/**
 * A file provider.
 * 
 * @since "2.2"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see IHttpPost#saveAttachmentToFile(FileProvider, byte[][])
 */
public interface FileProvider {
	
	/** Interface version. */
	String VERSION = "2.2";
	
	/**
	 * Provides a file for an open {@link HttpURLConnection} to save the attachment to.
	 * @param httpUrlConnection an open {@link HttpURLConnection} which can be used to get information from (the file name for example)
	 * @return a file that will be used to save the attachment to
	 * */
	File getFile( HttpURLConnection httpUrlConnection );
	
	/**
	 * Provides a last modified date for an open {@link HttpURLConnection} to save the attachment.
	 * 
	 * <p>This method may returned <code>null</code> in which case the last modified property of the file will not be set.</p>
	 * 
	 * @param httpUrlConnection an open {@link HttpURLConnection} which can be used to get information from (the file name for example)
	 * @return last modified to be set for the saved attachment
	 */
	Long getLastModified( HttpURLConnection httpUrlConnection );
	
}
