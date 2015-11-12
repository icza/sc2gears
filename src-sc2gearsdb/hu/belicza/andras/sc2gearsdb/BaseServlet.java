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

import static hu.belicza.andras.sc2gearsdbapi.ServletApi.PARAM_OPERATION;
import static hu.belicza.andras.sc2gearsdbapi.ServletApi.PARAM_PROTOCOL_VERSION;
import hu.belicza.andras.sc2gearsdbapi.ServletApi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A base servlet with request processing and response building utilities.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet {
	
	private static final Logger LOGGER = Logger.getLogger( BaseServlet.class.getName() );
	
	/** Google Analytics code to be included immediately before the closing <code>&lt;/head&gt;</code> tag. */
	protected static final String GA_TRACKER_HTML_SCRIPT = "<script type=\"text/javascript\">"
		+ "var _gaq = _gaq || [];"
		+ "_gaq.push(['_setAccount', 'UA-4884955-25']);"
		+ "_gaq.push(['_trackPageview']);"
		+ "(function() {"
		+ "var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;"
		+ "ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';"
		+ "var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);"
		+ "})();"
		+ "</script>";
	
	/** Google Adsense code, Header ad 728x90. */
	protected static final String HEADER_AD_728_90_HTML_SCRIPT = "<script type=\"text/javascript\">"
		+ "google_ad_client = \"ca-pub-4479321142068297\";"
		+ "google_ad_slot = \"4124263936\";"
		+ "google_ad_width = 728;"
		+ "google_ad_height = 90;"
		+ "</script>"
		+ "<script type=\"text/javascript\""
		+ "src=\"https://pagead2.googlesyndication.com/pagead/show_ads.js\">"
		+ "</script>";
	
	/** Footer copyright HTML code. */
	protected static final String FOOTER_COPYRIGHT_HTML = "<hr/><div style=\"text-align:right;font-style:italic\">&copy; Andr&aacute;s Belicza, 2010-2014</div>";
	
	/** Default CSS HTML code. */
	protected static String DEFAULT_CSS_HTML;
	
	@Override
	public void init() throws ServletException {
		if ( DEFAULT_CSS_HTML == null ) {
    		final StringBuilder cssBuilder = new StringBuilder( "<style type=\"text/css\">" );
    		
    		try ( final InputStreamReader cssReader = new InputStreamReader( getServletConfig().getServletContext().getResourceAsStream( "/sc2gearsdb.css" ), "UTF-8" ) ) {
    			final char[] buffer = new char[ 256 ];
    			int charsRead;
    	        while ( ( charsRead = cssReader.read( buffer ) ) >= 0 )
    	        	cssBuilder.append( buffer, 0, charsRead );
    	        
            } catch ( final IOException ie ) {
            	LOGGER.log( Level.SEVERE, "Failed to load sc2gearsdb.css!", ie );
            } finally {
                cssBuilder.append( "</style>" );
                DEFAULT_CSS_HTML = cssBuilder.toString();
            }
		}
	}
	
	/**
	 * Checks the protocol version supplied as a request parameter ({@link ServletApi#PARAM_PROTOCOL_VERSION})
	 * and returns the operation supplied as a request parameter ({@link ServletApi#PARAM_OPERATION}).
	 * 
	 * <p>{@link HttpServletResponse#SC_BAD_REQUEST} is sent back if the protocol version is missing or invalid, or if the operation is missing.</p>
	 * 
	 * @param validProtocolVersion valid protocol version to accept
	 * @param request              the HTTP servlet request
	 * @param response             the HTTP servlet response
	 * @return the operation, or <code>null</code> if the protocol version is missing or invalid or the operation is missing
	 * @throws IOException
	 */
	protected static String checkProtVerAndGetOperation( final String validProtocolVersion, final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		final String protocolVersion = request.getParameter( PARAM_PROTOCOL_VERSION );
		if ( protocolVersion == null ) {
        	LOGGER.warning( "Missing Protocol version!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing Protocol version!" );
			return null;
		}
		if ( !validProtocolVersion.equals( protocolVersion ) ) {
        	LOGGER.warning( "Invalid Protocol version: " + protocolVersion + " (valid: " + validProtocolVersion + ")" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid Protocol version! (Must be: " + validProtocolVersion + ")" );
			return null;
		}
		
		final String operation = request.getParameter( PARAM_OPERATION );
		if ( operation == null ) {
        	LOGGER.warning( "Missing Operation!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing Operation!" );
			return null;
		}
    	LOGGER.fine( "Operation: " + operation );
		
		return operation;
	}
	
	/**
	 * Returns the value of a parameter as a {@link Boolean}.
	 */
	protected static Boolean getBooleanParam( final HttpServletRequest request, final String paramName ) {
		try {
			return Boolean.valueOf( request.getParameter( paramName ) );
		} catch ( final Exception e ) {
			return null;
		}
	}
	
	/**
	 * Returns the value of a parameter as a {@link Short}.
	 */
	protected static Short getShortParam( final HttpServletRequest request, final String paramName ) {
		try {
			return Short.valueOf( request.getParameter( paramName ) );
		} catch ( final Exception e ) {
			return null;
		}
	}
	
	/**
	 * Returns the value of a parameter as an {@link Integer}.
	 */
	protected static Integer getIntParam( final HttpServletRequest request, final String paramName ) {
		try {
			return Integer.valueOf( request.getParameter( paramName ) );
		} catch ( final Exception e ) {
			return null;
		}
	}
	
	/**
	 * Returns the value of a parameter as an {@link Long}.
	 */
	protected static Long getLongParam( final HttpServletRequest request, final String paramName ) {
		try {
			return Long.valueOf( request.getParameter( paramName ) );
		} catch ( final Exception e ) {
			return null;
		}
	}
	
	/**
	 * Returns the value of a parameter as a {@link Float}.
	 */
	protected static Float getFloatParam( final HttpServletRequest request, final String paramName ) {
		try {
			return Float.valueOf( request.getParameter( paramName ) );
		} catch ( final Exception e ) {
			return null;
		}
	}
	
	/**
	 * Returns the value of a parameter as a {@link Date}.
	 */
	protected static Date getDateParam( final HttpServletRequest request, final String paramName ) {
		try {
			return new Date( Long.valueOf( request.getParameter( paramName ) ) );
		} catch ( final Exception e ) {
			return null;
		}
	}
	
	/**
	 * Returns a String list parsed from the value of the specified parameter.<br>
	 * The value is treated as a comma separated list of strings.
	 * Elements of this comma separated lists are trimmed by {@link String#trim()}.
	 */
	protected static List< String > getStringListParam( final HttpServletRequest request, final String paramName ) {
		try {
			
			final String[] values = request.getParameter( paramName ).split( "," );
			final List< String > valueList = new ArrayList< String >();
			
			for ( String value : values ) {
				value = value.trim();
				if ( value.length() > 0 )
					valueList.add( value );
			}
			
			return valueList;
			
		} catch ( final Exception e ) {
			return null;
		}
	}
	
	/**
	 * Returns an integer list parsed from the value of the specified parameter.<br>
	 * The value is treated as a comma separated list of integers.
	 * Elements of this comma separated lists are trimmed by {@link String#trim()}.
	 */
	protected static List< Integer > getIntListParam( final HttpServletRequest request, final String paramName ) {
		try {
			
			final String[] values = request.getParameter( paramName ).split( "," );
			final List< Integer > valueList = new ArrayList< Integer >();
			
			for ( String value : values ) {
				value = value.trim();
				if ( value.length() > 0 )
					valueList.add( Integer.valueOf( value ) );
			}
			
			return valueList;
			
		} catch ( final Exception e ) {
			return null;
		}
	}
	
	/**
	 * Configures the response never to cache the data.
	 * @param response reference to the response
	 */
	protected static void setNoCache( final HttpServletResponse response ) {
		response.setHeader    ( "Cache-Control", "no-cache" ); // For HTTP 1.1
		response.setHeader    ( "Pragma"       , "no-cache" ); // For HTTP 1.0
		response.setDateHeader( "Expires"      , 0          ); // For proxies
	}
	
	/**
	 * Included the default CSS into the specified output writer.
	 * <p>This method is to be called when the output HTML document is inside the <code>&lt;head&gt;</code> tag.
	 * This method will insert a <code>&lt;style&gt;</code> tag and inlines the CSS content.</p>
	 * @param out writer to write the default CSS code
	 */
	protected static void includeDefaultCss( final PrintWriter out ) {
		out.println( DEFAULT_CSS_HTML );
	}
	
}
