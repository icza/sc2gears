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

import hu.belicza.andras.sc2gearspluginapi.api.GeneralUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.listener.DownloaderCallback;
import hu.belicza.andras.sc2gearspluginapi.impl.util.SimpleFileProvider;

import java.net.HttpURLConnection;
import java.util.List;

/**
 * An interface that allows interaction with standard HTTP servers using the POST method.
 * 
 * <p>Parameters are sent as if they would be part of an HTML form. The content-type (<code>"Content-Type"</code> request property)
 * of the request will be set to <code>"application/x-www-form-urlencoded;charset"</code>.</p>
 * 
 * <p>Its usefulness includes -but is not limited to- uploading/downloading files without having to use Multi-part requests.
 * The following examples will demonstrate these.</p>
 * 
 * <p><b>Example #1: Upload a file encoded with Base64:</b><br>
 * <blockquote><pre>
 * IHttpPost httpPost = null;
 * try {
 *     String url = "http://some.site.com/upload";
 *     Map< String, String > paramsMap = new HashMap< String, String >();
 *     File file = new File( "c:/somefile.txt" );
 *     paramsMap.put( "fileName", file.getName() );
 *     paramsMap.put( "fileBase64", generalServices.getGeneralUtilsApi().encodeFileBase64( file ) );
 *     // Add other parameters you need...
 *     paramsMap.put( "someOtherThing", "some other value" );
 *     
 *     httpPost = generalServices.getGeneralUtilsApi().createHttpPost( url, paramsMap );
 *     if ( httpPost.connect() ) {
 *         if ( httpPost.doPost() )
 *             System.out.println( "File sent successfully, server response: " + httpPost.getResponse() );
 *         else
 *             System.out.println( "Failed to send file!" );
 *     }
 *     else
 *         System.out.println( "Failed to connect!" );
 * } catch ( Exception e ) {
 *     e.printStackTrace();
 * } finally {
 *     if ( httpPost != null )
 *         httpPost.close();
 * }
 * </pre></blockquote></p>
 * 
 * <p><b>Example #2: Download a file from a server:</b><br>
 * <blockquote><pre>
 * IHttpPost httpPost = null;
 * try {
 *     String url = "http://some.site.com/download";
 *     Map< String, String > paramsMap = new HashMap< String, String >();
 *     String fileName = "somefile.txt";
 *     paramsMap.put( "fileName", fileName );
 *     // Add other parameters you need...
 *     paramsMap.put( "userId", "someUserId" );
 *     
 *     httpPost = generalServices.getGeneralUtilsApi().createHttpPost( url, paramsMap );
 *     if ( httpPost.connect() ) {
 *         if ( httpPost.doPost() ) {
 *             // We could also use: new SimpleFileProvider( new File( "c:/downloads", fileName ), null )
 *             httpPost.saveAttachmentToFile( new FileProvider() {
 *                 public File getFile( HttpURLConnection httpUrlConnection ) {
 *                     File file = new File( "c:/downloads", fileName );
 *                     System.out.println( "Saving file to " + file.getAbsolutePath() );
 *                     return file;
 *                 }
 *                 public Long getLastModified( HttpURLConnection httpUrlConnection ) {
 *                     // We assume here that the server sends the file last modified value as a header field named "X-file-date":
 *                     String fileDateString = httpUrlConnection.getHeaderField( "X-file-date" );
 *                     return fileDateString == null ? null : Long.valueOf( fileDateString );
 *                 }
 *             } );
 *         }
 *         else
 *             System.out.println( "Failed to send request!" );
 *     }
 *     else
 *         System.out.println( "Failed to connect!" );
 * } catch ( Exception e ) {
 *     e.printStackTrace();
 * } finally {
 *     if ( httpPost != null )
 *         httpPost.close();
 * }
 * </pre></blockquote>
 * <i>Note that you can download a file using the GET method with {@link GeneralUtilsApi#downloadUrl(String, java.io.File)}
 * and {@link GeneralUtilsApi#getDownloader(String, java.io.File, boolean, DownloaderCallback)}.</i>
 * </p>
 * 
 * @since "2.2"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GeneralUtilsApi#createHttpPost(String, java.util.Map)
 * @see SimpleFileProvider
 */
public interface IHttpPost {
	
	/** Interface version. */
	String VERSION = "2.2";
	
	/**
	 * Connection/communication state.
	 * 
	 * @since "2.2"
	 * @author Andras Belicza
	 */
	public enum State {
		/** Not connected.                   */
		NOT_CONNECTED,
		/** Successfully connected.          */
		CONNECTED,
		/** Connect failed.                  */
		CONNECT_FAILED,
		/** Request sent successfully.       */
		REQUEST_SENT,
		/** Sending request failed.          */
		SENDING_REQUEST_FAILED,
		/** Response processed successfully. */
		RESPONSE_PROCESSED,
		/** Processing response failed.      */
		PROCESSING_RESPONSE_FAILED,
		/** Closed.                          */
		CLOSED;
		
	}
	
	/**
	 * Sets whether internal state checking should be performed.
	 * 
	 * <p>You may disable internal state checking if you tweak the {@link HttpURLConnection}.</p>
	 * 
	 * @param enabled the internal state checking value to be set
	 */
	void setInternalStateCheckingEnabled( boolean enabled );
	
	/**
	 * Tells if internal state checking is enabled.
	 * Internal state checking is enabled by default.
	 * @return true if if internal state checking is enabled; false otherwise
	 */
	boolean isInternalStateCheckingEnabled();
	
	/**
	 * Returns the internal state of the connection/communication.
	 * @return the internal state of the connection/communication
	 */
	State getState();
	
	/**
	 * Sets the charset of the request.
	 * 
	 * <p>This will set the request property <code>"Accept-Charset"</code> to <code>charset</code>.</p>
	 * 
	 * <p>It must be called before {@link #connect()}. If charset is not set, the default <code>UTF-8</code> will be used.</p>
	 * 
	 * @param charset charset of the request to be set
	 * 
	 * @throws IllegalStateException if internal state checking is enabled and the internal state is not {@link State#NOT_CONNECTED}
	 */
	void setRequestCharset( String charset ) throws IllegalStateException;
	
	/**
	 * Returns the charset of the request.
	 * The default charset of the request is "UTF-8".
	 * @return the charset of the request
	 */
	String getRequestCharset();
	
	/**
	 * Sets a request property.
	 * 
	 * <p>The properties will be passed to the underlying {@link HttpURLConnection}
	 * before it's <code>connect()</code> method is called.</p>
	 * 
	 * <p>It must be called before {@link #connect()}.</p>
	 * 
	 * @param key   the property key
	 * @param value the property value
	 * 
	 * @throws IllegalStateException if internal state checking is enabled and the internal state is not {@link State#NOT_CONNECTED}
	 */
	void setRequestProperty( String key, String value ) throws IllegalStateException;
	
	/**
	 * Returns the underlying {@link HttpURLConnection}.
	 * 
	 * <p>This method may return <code>null</code> if called before {@link #connect()} or if called when {@link #connect()} returned false.</p>
	 * 
	 * @return the underlying {@link HttpURLConnection}
	 */
	HttpURLConnection getConnection();
	
	/**
	 * Connects to the provided URL.
	 * 
	 * <p>Only one connect method ({@link #connect()} or {@link #connect(Runnable)}) can be called and only once.</p>
	 * 
	 * @return true if connection was successful; false otherwise
	 * 
	 * @throws IllegalStateException if internal state checking is enabled and the internal state is not {@link State#NOT_CONNECTED}
	 * 
	 * @see #connect(Runnable)
	 */
	boolean connect() throws IllegalStateException;
	
	/**
	 * Connects to the provided URL.
	 * 
	 * <p>Only one connect method ({@link #connect()} or {@link #connect(Runnable)}) can be called and only once.</p>
	 *
	 * @param beforeConnectionConnectTask task to be executed right before calling {@link HttpURLConnection#connect()} 
	 * @return true if connection was successful; false otherwise
	 * 
	 * @throws IllegalStateException if internal state checking is enabled and the internal state is not {@link State#NOT_CONNECTED}
	 * 
	 * @see #connect()
	 */
	boolean connect( Runnable beforeConnectionConnectTask ) throws IllegalStateException;
	
	/**
	 * Posts the parameters to the server.
	 * 
	 * <p>The parameters will be encoded using the charset set by {@link #setRequestCharset(String)} (defaults to UTF-8).</p>
	 * 
	 * <p>Can only be called if {@link #connect()} returned <code>true</code>.</p>
	 * 
	 * @return true if the operation was successful; false otherwise
	 * 
	 * @throws IllegalStateException if internal state checking is enabled and the internal state is not {@link State#CONNECTED}
	 */
	boolean doPost() throws IllegalStateException;
	
	/**
	 * Returns the HTTP response code of the server.
	 * 
	 * <p>Can only be called if {@link #doPost()} returned <code>true</code>.</p>
	 * 
	 * @return the HTTP response code of the server
	 * 
	 * @throws IllegalStateException if internal state checking is enabled and the internal state is not {@link State#REQUEST_SENT}
	 */
	int getServerResponseCode() throws IllegalStateException;
	
	/**
	 * Gets the response from the server.
	 * 
	 * <p>Can only be called if {@link #doPost()} returned <code>true</code>.</p>
	 * 
	 * @return the server response, or <code>null</code> if error occurred
	 * 
	 * @throws IllegalStateException if internal state checking is enabled and the internal state is not {@link State#REQUEST_SENT}
	 * 
	 * @see #getResponseLines()
	 * @see #saveAttachmentToFile(FileProvider, byte[][])
	 */
	String getResponse() throws IllegalStateException;
	
	/**
	 * Gets the response from the server as a list of lines.
	 * 
	 * <p>Can only be called if {@link #doPost()} returned <code>true</code>.</p>
	 * 
	 * @return the server response as a list of lines, or <code>null</code> if error occurred
	 * 
	 * @throws IllegalStateException if internal state checking is enabled and the internal state is not {@link State#REQUEST_SENT}
	 * 
	 * @see #getResponse()
	 * @see #saveAttachmentToFile(FileProvider, byte[][])
	 */
	List< String > getResponseLines() throws IllegalStateException;
	
	/**
	 * Saves the attachment of the response, the content is treated as <code>application/octet-stream</code>.
	 * 
	 * <p>Can only be called if {@link #doPost()} returned <code>true</code>.</p>
	 * 
	 * <p>A {@link FileProvider} is used to get a file to save the attachment to.</p>
	 * 
	 * @param fileProvider file provider to specify a file to save to
	 * @param buffer       optional buffer to use for IO read/write operations
	 * @return true if the attachment was saved successfully; false otherwise
	 * 
	 * @throws IllegalStateException if internal state checking is enabled and the internal state is not {@link State#REQUEST_SENT}
	 * 
	 * @see FileProvider
	 * @see #getResponse()
	 * @see #getResponseLines()
	 */
	boolean saveAttachmentToFile( FileProvider fileProvider, byte[]... buffer ) throws IllegalStateException;
	
	/**
	 * Closes this IHttpPost, releases all allocated resources.
	 */
	void close();
	
}
