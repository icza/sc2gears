package hu.belicza.andras.sc2gears.services;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Base HTTP client handler.
 * 
 * @author Andras Belicza
 */
public abstract class BaseHttpClientHandler implements Runnable {
	
	/** Date format of the HTTP header fields holding date+time values. */
	protected static final DateFormat HTTP_HEADER_DATE_FORMAT = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss zzz" );
	
	/**
	 * HTTP response codes.
	 * 
	 * @author Andras Belicza
	 */
	public enum HttpResponseCode {
		OK             ( 200 ),
		
		BAD_REQUEST    ( 400 ),
		NOT_FOUND      ( 403 ),
		NOT_IMPLEMENTED( 501 );
		
		public final int    code;
		public final String message;
		
		/**
		 * Creates a new HttpResponseCode.
		 * @param code numerical HTTP response code
		 */
		private HttpResponseCode( final int code ) {
			this.code    = code;
			this.message = ReplayConsts.convertConstNameToNormal( name(), true );
		}
	}
	
	/** The client socket. */
	protected final Socket   socket;
	/** The input reader.  */
	protected BufferedReader input; 
	/** The output stream. */
	protected OutputStream   output;
	
	/** First line of the request. */
	protected String requestLine;
	
	/**
	 * Creates a new BaseHttpClientHandler.
	 * @param socket socket of the HTTP client to handle
	 */
	public BaseHttpClientHandler( final Socket socket ) {
		this.socket = socket;
	}
	
	/**
	 * Processes the HTTP request<br>
	 * Should be executed in a new thread.
	 */
	@Override
	public final void run() {
		try {
			input  = new BufferedReader( new InputStreamReader( socket.getInputStream(), Consts.UTF8 ) );
			output = socket.getOutputStream();
			
			requestLine = input.readLine();
			
			if ( !requestLine.endsWith( " HTTP/1.0" ) && !requestLine.endsWith( " HTTP/1.1" ) )
				sendError( HttpResponseCode.BAD_REQUEST );
			
			if ( !requestLine.startsWith( "GET " ) )
				sendError( HttpResponseCode.NOT_IMPLEMENTED );
			
			handleResource( requestLine.substring( 4, requestLine.length() - 9 ) );
			
			output.flush();
		} catch ( Exception e ) {
			// Do not print stack trace...
		} finally {
			try {
	            socket.close();
            } catch ( IOException ie ) {
            }
		}
	}
	
	/**
	 * Handles the specified requested resource.
	 * @param resource resource as requested in the first line of the request
	 */
	protected abstract void handleResource( String resource ) throws IOException;
	
	/**
	 * Sends back an HTTP error.
	 * @param responseCode HTTP response code of the error
	 */
	protected void sendError( final HttpResponseCode responseCode ) throws IOException {
		sendError( responseCode.code, responseCode.message );
	}
	
	/**
	 * Sends back an HTTP error.
	 * @param responseCode    numerical HTTP response code of the error
	 * @param responseMessage response message associated with the error
	 */
	protected void sendError( final int responseCode, final String responseMessage ) throws IOException {
		initResponse( responseCode, responseMessage );
		printHeaderField( "Content-type", "text/html" );
		closeHeader();
		print( "<html><body><h3>" );
		print( responseMessage );
		print( "</h3></body></html>" );
	}
	
	/**
	 * Initializes the HTTP response by printing its first line with the HTTP response code and some common header fields.
	 * @param responseCode HTTP response code
	 */
	protected void initResponse( final HttpResponseCode responseCode ) throws IOException {
		initResponse( responseCode.code, responseCode.message );
	}
	
	/**
	 * Initializes the HTTP response by printing its first line with the HTTP response code and some common header fields.
	 * @param responseCode    numerical HTTP response code
	 * @param responseMessage response message associated with the response code
	 */
	protected void initResponse( final int responseCode, final String responseMessage ) throws IOException {
		output.write( ( "HTTP/1.0 " + responseCode + " " + responseMessage ).getBytes( Consts.UTF8 ) );
		println();
		
		printHeaderField( "Server", HttpServer.SERVER_ID_STRING );
		
		// Disable caching:
		printHeaderField( "Cache-Control", "no-cache" ); // For HTTP 1.1
		printHeaderField( "Pragma"       , "no-cache" ); // For HTTP 1.0
		printHeaderField( "Expires"      , HTTP_HEADER_DATE_FORMAT.format( new Date( 0 ) ) ); // For proxies
	}
	
	/**
	 * Prints a header field to the output.
	 * @param name  name of the header field
	 * @param value value of the header field
	 */
	protected void printHeaderField( final String name, final String value ) throws IOException {
		print( name );
		output.write( ':' );
		print( value );
		println();
	}
	
	/**
	 * Prints some text to the output.
	 * @param text text to be printed
	 */
	protected void print( final String text ) throws IOException {
		output.write( text.getBytes( Consts.UTF8 ) );
	}
	
	/**
	 * Prints all objects to the output.
	 * <p>The {@link Object#toString()} will be called for objects which are not instance of {@link String}.</p> 
	 * @param objects objects to be printed
	 */
	protected void print( final Object... objects ) throws IOException {
		for ( final Object object : objects )
			output.write( ( object instanceof String ? (String) object : object.toString() ).getBytes( Consts.UTF8 ) );
	}
	
	/**
	 * Closes the header section of the response.
	 */
	protected void closeHeader() throws IOException {
		println(); // An empty line indicates the end of header
		
		output.flush();
	}
	
	/**
	 * Prints line termination to the output.
	 */
	protected void println() throws IOException {
		output.write( '\r' );
		output.write( '\n' );
	}
	
}
