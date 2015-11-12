/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.util;

import hu.belicza.andras.sc2gearspluginapi.api.httpost.FileProvider;
import hu.belicza.andras.sc2gearspluginapi.api.httpost.IHttpPost;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class to perform an HTTP POST.
 * 
 * <p>Info: <a href="http://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests">Stackoverflow.com</a>
 * 
 * @author Andras Belicza
 */
public class HttpPost implements IHttpPost {
	
	/** Charset to be used. */
	private static final String DEFAULT_CHARSET = "UTF-8";
	
	/** Internal state of the connection/communication. */
	private State state = State.NOT_CONNECTED;
	
	/** Tells if internal state checking is enabled.    */
	private boolean internalStateCheckingEnabled = true;
	
	/** Map of parameters to be sent.           */
	private final Map< String, String > paramsMap;
	/** URL string to post to.                  */
	private final String                urlString;
	/** Charset to use to send the request.     */
	private String                      requestCharset = DEFAULT_CHARSET;
	/** Optional additional request properties. */
	private Map< String, String >       requestPropertyMap;
	
	/** HttpUrlConnection to perform the POST.  */
	private HttpURLConnection           httpUrlConnection;
	
	/**
	 * Creates a new HttpPost.
	 * @param urlString URL string to post to
	 * @param paramsMap map of parameters to be sent
	 */
	public HttpPost( final String urlString, final Map< String, String > paramsMap ) {
		this.paramsMap = paramsMap;
		this.urlString = urlString;
	}
	
	@Override
	public void setInternalStateCheckingEnabled( final boolean enabled ) {
		internalStateCheckingEnabled = enabled;
	}
	
	@Override
	public boolean isInternalStateCheckingEnabled() {
		return internalStateCheckingEnabled;
	}
	
	@Override
	public State getState() {
		return state;
	}
	
	@Override
	public void setRequestCharset( final String requestCharset ) {
		if ( internalStateCheckingEnabled && state != State.NOT_CONNECTED )
			throw new IllegalStateException( "setRequestCharset() can only be called in NOT_CONNECTED state!" );
		
		this.requestCharset = requestCharset;
	}
	
	@Override
	public String getRequestCharset() {
		return requestCharset;
	}
	
	@Override
	public void setRequestProperty( final String key, final String value ) {
		if ( internalStateCheckingEnabled && state != State.NOT_CONNECTED )
			throw new IllegalStateException( "setRequestProperty() can only be called in NOT_CONNECTED state!" );
		
		if ( requestPropertyMap == null )
			requestPropertyMap = new HashMap< String, String >();
		
		requestPropertyMap.put( key, value );
	}
	
	@Override
	public HttpURLConnection getConnection() {
		return httpUrlConnection;
	}
	
	@Override
	public boolean connect() {
		return connect( null );
	}
	
	@Override
	public boolean connect( final Runnable beforeConnectionConnectTaks ) {
		if ( internalStateCheckingEnabled && state != State.NOT_CONNECTED )
			throw new IllegalStateException( "connect() can only be called in NOT_CONNECTED state!" );
		
		try {
			httpUrlConnection = (HttpURLConnection) new URL( urlString ).openConnection();
			
			httpUrlConnection.setDoOutput( true );
			
			if ( requestPropertyMap != null )
				for ( final Entry< String, String > entry : requestPropertyMap.entrySet() )
					httpUrlConnection.setRequestProperty( entry.getKey(), entry.getValue() );
			
			httpUrlConnection.setRequestProperty( "Accept-Charset", requestCharset );
			httpUrlConnection.setRequestProperty( "Content-Type"  , "application/x-www-form-urlencoded;charset=" + requestCharset );
			
			if ( beforeConnectionConnectTaks != null )
				beforeConnectionConnectTaks.run();
			
			httpUrlConnection.connect();
		} catch ( final IOException ie ) {
			state = State.CONNECT_FAILED;
			ie.printStackTrace();
			return false;
		}
		
		state = State.CONNECTED;
		return true;
	}
	
	@Override
	public boolean doPost() {
		if ( internalStateCheckingEnabled && state != State.CONNECTED )
			throw new IllegalStateException( "doPost() can only be called in CONNECTED state!" );
		
		try ( final OutputStream output = httpUrlConnection.getOutputStream() ) {
			final StringBuilder paramsBuilder = new StringBuilder();
			for ( final Entry< String, String > entry : paramsMap.entrySet() ) {
				if ( paramsBuilder.length() > 0 )
					paramsBuilder.append( '&' );
				paramsBuilder.append( entry.getKey() ).append( '=' ).append( URLEncoder.encode( entry.getValue(), DEFAULT_CHARSET ) );
			}
			
			output.write( paramsBuilder.toString().getBytes( requestCharset ) );
			output.flush();
			
			state = State.REQUEST_SENT;
		} catch ( final IOException ie ) {
			state = State.SENDING_REQUEST_FAILED;
			ie.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	@Override
	public int getServerResponseCode() {
		if ( internalStateCheckingEnabled && state != State.REQUEST_SENT )
			throw new IllegalStateException( "getServerResponseCode() can only be called in REQUEST_SENT state!" );
		
		try {
			return httpUrlConnection.getResponseCode();
		} catch ( final IOException ie ) {
			ie.printStackTrace();
			return -1;
		}
	}
	
	@Override
	public String getResponse() {
		if ( internalStateCheckingEnabled && state != State.REQUEST_SENT )
			throw new IllegalStateException( "getResponse() can only be called in REQUEST_SENT state!" );
		
		return (String) readResponse( false );
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List< String > getResponseLines() {
		if ( internalStateCheckingEnabled && state != State.REQUEST_SENT )
			throw new IllegalStateException( "getResponseLines() can only be called in REQUEST_SENT state!" );
		
		return (List< String >) readResponse( true );
	}
	
	/**
	 * Reads the response from the server.
	 * @param asLineList tells response should be returned as a list of lines or as 1 string
	 * @return the server response, or null if error occurred
	 */
	private Object readResponse( final boolean asLineList ) {
		try ( final InputStream input = httpUrlConnection.getInputStream() ) {
			final int status = httpUrlConnection.getResponseCode();
			if ( status == HttpURLConnection.HTTP_OK ) {
				
				String responseCharset = DEFAULT_CHARSET;
				final String contentType = httpUrlConnection.getHeaderField( "Content-Type" );
				if ( contentType != null ) {
					for ( final String token : contentType.replace( " ", "" ).split( ";" ) ) {
						if ( token.startsWith( "charset=" ) ) {
							responseCharset = token.split( "=", 2 )[ 1 ];
							break;
						}
					}
				}
				
				final BufferedReader reader = new BufferedReader( new InputStreamReader( input, responseCharset ) );
				if ( asLineList ) {
					String line;
					final List< String > lineList = new ArrayList< String >();
					while ( ( line = reader.readLine() ) != null )
						lineList.add( line );
					
					state = State.RESPONSE_PROCESSED;
					return lineList;
				}
				else {
					final StringBuilder responseBuilder = new StringBuilder();
					final char[] buffer = new char[ 64 ];
					int charsRead;
					while ( ( charsRead = reader.read( buffer ) ) > 0 )
						responseBuilder.append( buffer, 0, charsRead );
					
					state = State.RESPONSE_PROCESSED;
					return responseBuilder.toString();
				}
			}
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		}
		
		state = State.PROCESSING_RESPONSE_FAILED;
		return null;
	}
	
	@Override
	public boolean saveAttachmentToFile( final FileProvider fileProvider, final byte[]... buffer_ ) {
		if ( internalStateCheckingEnabled && state != State.REQUEST_SENT )
			throw new IllegalStateException( "saveAttachmentToFile() can only be called in REQUEST_SENT state!" );
		
		FileOutputStream output = null;
		try ( final InputStream input = httpUrlConnection.getInputStream() ) {
			final int status = httpUrlConnection.getResponseCode();
			if ( status == HttpURLConnection.HTTP_OK ) {
				final File toFile = fileProvider.getFile( httpUrlConnection );
				output            = new FileOutputStream( toFile );
				
				final byte[] buffer = buffer_.length == 0 ? new byte[ 16*1024 ] : buffer_[ 0 ];
				int bytesRead;
				while ( ( bytesRead = input.read( buffer ) ) > 0 )
					output.write( buffer, 0, bytesRead );
				
				output.flush();
				output.close();
				output = null;
				
				final Long lastModified = fileProvider.getLastModified( httpUrlConnection );
				if ( lastModified != null )
					toFile.setLastModified( lastModified );
				
				state = State.RESPONSE_PROCESSED;
				return true;
			}
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		} finally {
			if ( output != null )
				try { output.close(); } catch ( final IOException ie ) {}
		}
		
		state = State.PROCESSING_RESPONSE_FAILED;
		return false;
	}
	
	@Override
	public void close() {
		if ( httpUrlConnection != null )
			httpUrlConnection.disconnect();
		
		state = State.CLOSED;
	}
	
}
