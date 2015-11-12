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

import static hu.belicza.andras.sc2gearsdb.util.ServerUtils.DECIMAL_FORMAT;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.MAX_LIMIT_VALUE;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.OPERATION_CHECK_KEY;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.OPERATION_GET_PRIVATE_REP_DATA;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.OPERATION_GET_PUBLIC_REP_COMMENTS;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.OPERATION_GET_PUBLIC_REP_PROFILE;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.OPERATION_GET_QUOTA_INFO;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.OPERATION_SAVE_PRIVATE_REP_DATA;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.OPERATION_SAVE_PUBLIC_REP_COMMENT;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.PARAM_COMMENT;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.PARAM_LABELS;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.PARAM_LIMIT;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.PARAM_OFFSET;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.PARAM_RATE_BG;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.PARAM_RATE_GG;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.PARAM_SHA1;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.PARAM_TIME_ZONE;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.PARAM_USER_NAME;
import static hu.belicza.andras.sc2gearsdbapi.InfoServletApi.PROTOCOL_VERSION_1;
import static hu.belicza.andras.sc2gearsdbapi.ServletApi.PARAM_AUTHORIZATION_KEY;
import hu.belicza.andras.sc2gearsdb.common.server.CommonUtils.DbPackage;
import hu.belicza.andras.sc2gearsdb.datastore.Account;
import hu.belicza.andras.sc2gearsdb.datastore.FileStat;
import hu.belicza.andras.sc2gearsdb.datastore.Rep;
import hu.belicza.andras.sc2gearsdb.datastore.RepComment;
import hu.belicza.andras.sc2gearsdb.datastore.RepProfile;
import hu.belicza.andras.sc2gearsdb.datastore.RepRate;
import hu.belicza.andras.sc2gearsdb.util.CachingService;
import hu.belicza.andras.sc2gearsdb.util.JQBuilder;
import hu.belicza.andras.sc2gearsdb.util.PMF;
import hu.belicza.andras.sc2gearsdb.util.ServerUtils;
import hu.belicza.andras.sc2gearsdbapi.FileServletApi.FileType;
import hu.belicza.andras.sc2gearsdbapi.InfoServletApi;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

/**
 * A general information provider servlet.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class InfoServlet extends BaseServlet {
	
	private static final Logger LOGGER = Logger.getLogger( InfoServlet.class.getName() );
	
	/** Date-time format. */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
	
	/*
	 * doPost() is more common in case of this servlet, so doGet() calls doPost().
	 */
	@Override
	protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		doPost( request, response );
	}
	
	@Override
	protected void doPost( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		final String operation = checkProtVerAndGetOperation( PROTOCOL_VERSION_1, request, response );
		if ( operation == null )
			return;
		
		switch ( operation ) {
		case OPERATION_GET_PUBLIC_REP_COMMENTS :
			getPublicRepComments( request, response );
			break;
		case OPERATION_GET_PUBLIC_REP_PROFILE :
			getPublicRepProfile( request, response );
			break;
		case OPERATION_SAVE_PUBLIC_REP_COMMENT :
			savePublicRepComment( request, response );
			break;
		default:
    		// The rest requires an Authorization key
			final String authorizationKey = request.getParameter( PARAM_AUTHORIZATION_KEY );
			if ( authorizationKey == null || authorizationKey.isEmpty() ) {
				LOGGER.warning( "Missing Authorization Key!" );
				response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing Authorization Key!" );
				return;
			}
			
			switch ( operation ) {
			case OPERATION_CHECK_KEY :
				checkKey( authorizationKey, request, response );
				break;
			case OPERATION_GET_QUOTA_INFO :
				getQuotaInfo( authorizationKey, request, response );
				break;
			case OPERATION_GET_PRIVATE_REP_DATA :
				getPrivateData( authorizationKey, request, response);
				break;
			case OPERATION_SAVE_PRIVATE_REP_DATA :
				savePrivateData( authorizationKey, request, response);
				break;
			default:
				LOGGER.warning( "Invalid Operation! (Authorization key: " + authorizationKey + ")" );
				response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid Operation!" );
				return;
			}
			break;
		}
	}
	
	/**
	 * Returns public comments of a replay.
	 * 
	 * <p>
	 * Example GET url:<br>
	 * https://sciigears.appspot.com/info?protVer=1&op=getPubRepCom&sha1=fc9f84e5caac72ed8899483508809632b07f5d57&offset=0&limit=10&tzone=Europe%2fBudapest
	 * </p>
	 */
	private void getPublicRepComments( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		final String  sha1     = request.getParameter( PARAM_SHA1 );
		final Integer offset   = request.getParameter( PARAM_OFFSET ) == null ? 0  : getIntParam( request, PARAM_OFFSET );
		final Integer limit    = request.getParameter( PARAM_LIMIT  ) == null ? 10 : getIntParam( request, PARAM_LIMIT  );
		final String  timeZone = request.getParameter( PARAM_TIME_ZONE );
		
		if ( sha1 == null || sha1.isEmpty() ) {
			LOGGER.warning( "Missing parameters!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
			return;
		}
		if ( offset == null || offset < 0 ) {
			LOGGER.warning( "Invalid Offset: " + offset );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid Offset!" );
			return;
		}
		if ( limit == null || limit < 0 || limit > MAX_LIMIT_VALUE ) {
			LOGGER.warning( "Invalid Limit: " + limit );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid Limit!" );
			return;
		}
		
		LOGGER.fine( "sha1: " + sha1 + ", offset: " + offset );
		
		final DateFormat dateFormat = (DateFormat) DATE_FORMAT.clone();
		if ( timeZone != null )
			dateFormat.setTimeZone( TimeZone.getTimeZone( timeZone ) );
		
		final String  userAgent     = request.getHeader( "User-Agent" );
		final boolean nonJavaClient = userAgent != null && !userAgent.contains( "Java" );
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			final List< RepComment > repCommentList = new JQBuilder<>( pm, RepComment.class ).filter( "sha1==p1", "String p1" ).desc( "date" ).range( offset, offset + limit ).get( sha1 );
			
			response.setContentType( "text/html" );
			// Set no-cache
			setNoCache( response );
			final PrintWriter out = response.getWriter();
			out.println( "<html><head>" );
			if ( nonJavaClient )
				out.println( "<title>Public replay comments - Sc2gears&trade; Database</title>" );
			includeDefaultCss( out );
			// Include GA tracker code for browsers
			if ( nonJavaClient )
				out.println( GA_TRACKER_HTML_SCRIPT );
			out.println( "</head><body>" );
			
			final String range = repCommentList.isEmpty() ? "No more comments." : ( offset + 1 ) + ".." + ( offset + repCommentList.size() );
			out.println( "<h3 class=\"h3\">Public replay comments: " + range + "</h3>" );
			
			int counter = offset;
			for ( final RepComment repComment : repCommentList ) {
				final boolean hidden = repComment.isHidden();
				out.print  ( "<br><div class='" );
				out.print  ( hidden ? "hiddenHeader" : "commentHeader" );
				out.print  ( "'>&nbsp;#" );
				out.print  ( ++counter );
				out.print  ( " &#9658; " );
				out.print  ( dateFormat.format( repComment.getDate() ) );
				out.print  ( " &#9658; " );
				out.print  ( hidden ? "*REMOVED*" : ServerUtils.encodeHtmlString( repComment.getUserName(), false ) );
				out.println( "</div><p>" );
				out.println( hidden ? "*REMOVED*" : ServerUtils.encodeHtmlString( repComment.getComment().getValue(), true  ) );
				out.println( "</p>" );
			}
			
			// Include Footer for browsers
			if ( nonJavaClient )
				out.println( FOOTER_COPYRIGHT_HTML );
			out.println( "</body></html>" );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	/**
	 * Returns a public replay profile.<br>
	 * Sends back an XML with the requested data.
	 * 
	 * <p>Example XML response:<br>
     * <blockquote><pre>
	 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
	 * &lt;publicRepProfile docVersion="1.0"&gt;
	 *     &lt;commentsCount&gt;12&lt;/commentsCount&gt;
	 *     &lt;ggsCount&gt;8&lt;/ggsCount&gt;
	 *     &lt;bgsCount&gt;2&lt;/bgsCount&gt;
	 * &lt;/publicRepProfile&gt;
     * </pre></blockquote></p>
     * 
	 */
	private static void getPublicRepProfile( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		final String sha1 = request.getParameter( PARAM_SHA1 );
		
		if ( sha1 == null || sha1.isEmpty() ) {
			LOGGER.warning( "Missing parameters!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
			return;
		}
		
		LOGGER.fine( "sha1: " + sha1 );
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			final List< RepProfile > repProfileList = new JQBuilder<>( pm, RepProfile.class ).filter( "sha1==p1", "String p1" ).get( sha1 );
			final RepProfile         repProfile     = repProfileList.isEmpty() ? null : repProfileList.get( 0 );
			
			response.setContentType( "text/xml" );
			response.setCharacterEncoding( "UTF-8" );
			// Set no-cache
			setNoCache( response );
			
			// This method is called frequently, produce XML manually which is faster, and profile does not contain strings (which would have to be encoded) 
			
			final PrintWriter writer = response.getWriter();
			writer.print( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
			writer.print( "<publicRepProfile docVersion=\"1.0\">" );
			writer.print( "<commentsCount>" );
			writer.print( repProfile == null ? 0 : repProfile.getCommentsCount() );
			writer.print( "</commentsCount>" );
			writer.print( "<ggsCount>" );
			writer.print( repProfile == null ? 0 : repProfile.getGgsCount() );
			writer.print( "</ggsCount>" );
			writer.print( "<bgsCount>" );
			writer.print( repProfile == null ? 0 : repProfile.getBgsCount() );
			writer.print( "</bgsCount>" );
			writer.print( "</publicRepProfile>" );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	/**
	 * Saves public replay comment for a replay.
	 * Sends back a plain text response: 1 line exactly, true if comment saved successfully; false otherwise.
	 */
	private static void savePublicRepComment( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		final String sha1     = request.getParameter( PARAM_SHA1      );
		String       userName = request.getParameter( PARAM_USER_NAME );
		final String rateGg   = request.getParameter( PARAM_RATE_GG   );
		final String rateBg   = request.getParameter( PARAM_RATE_BG   );
		String       comment  = request.getParameter( PARAM_COMMENT   );
		// Optional parameter:
		final String authorizationKey = request.getParameter( PARAM_AUTHORIZATION_KEY );
		
		LOGGER.fine( "Authorization key: " + authorizationKey + ", sha1: " + sha1 + ", user name: " + userName );
		
		if ( sha1 == null || sha1.isEmpty() || userName == null || userName.isEmpty() ) {
			LOGGER.warning( "Missing parameters!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
			return;
		}
		if ( comment == null && rateGg == null && rateBg == null ) {
			LOGGER.warning( "Missing parameters!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
			return;
		}
		
		userName = InfoServletApi.trimUserName( userName );
		comment  = InfoServletApi.trimPublicRepComment( comment );
		if ( userName.isEmpty() || comment != null && comment.isEmpty() ) {
			LOGGER.warning( "Missing parameters!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
			return;
		}
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			final Key accountKey = authorizationKey == null ? null : CachingService.getAccountKeyByAuthKey( pm, authorizationKey );
			
			// First register the updater task because if it fails, we do not want comment and rate to be saved!
			// Update replay profile
			TaskServlet.register_updateRepProfileTask( sha1, comment != null, rateGg != null, rateBg != null );
			
			if ( rateGg != null || rateBg != null ) {
				// Save rate
				final RepRate repRate = new RepRate( sha1 );
				if ( accountKey != null )
					repRate.setAccountKey( accountKey );
				repRate.setUserName( userName );
				repRate.fillTracking( request );
				repRate.setBg( rateBg != null );
				repRate.setGg( rateGg != null );
				pm.makePersistent( repRate );
			}
			
			if ( comment != null ) {
				// Save comment
				final RepComment repComment = new RepComment( sha1 );
				if ( accountKey != null )
					repComment.setAccountKey( accountKey );
				repComment.setUserName( userName );
				repComment.fillTracking( request );
				repComment.setComment( new Text( comment ) );
				pm.makePersistent( repComment );
			}
			
			response.setContentType( "text/plain" );
			// Set no-cache
			setNoCache( response );
			
			response.getWriter().println( Boolean.TRUE );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	/**
	 * Checks the authorization key.<br>
	 * Sends back a plain text response: 1 line exactly, the boolean result of the key being valid (true or false).
	 * @param authorizationKey authorization key to check
	 */
	private static void checkKey( final String authorizationKey, final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		LOGGER.fine( "Authorization key: " + authorizationKey );
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			response.setContentType( "text/plain" );
			// Set no-cache
			setNoCache( response );
			response.getWriter().println( CachingService.getAccountKeyByAuthKey( pm, authorizationKey ) != null );
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	/**
	 * Returns the quota info to the client.
	 * @param authorizationKey authorization key of the user
	 */
	private void getQuotaInfo( final String authorizationKey, final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		LOGGER.fine( "Authorization key: " + authorizationKey );
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			response.setContentType( "text/html" );
			// Set no-cache
			setNoCache( response );
			final PrintWriter out = response.getWriter();
			out.println( "<html><head>" );
			includeDefaultCss( out );
			out.println( "</head><body>" );
			
			// Query data from the datastore
			final List< Account > accountList = new JQBuilder<>( pm, Account.class ).filter( "authorizationKey==p1", "String p1" ).get( authorizationKey );
			if ( accountList.isEmpty() ) {
				LOGGER.warning( "Invalid Authorization key!" );
				out.println( "<p class=\"note\">You provided an invalid Authorization key!</p>" );
				out.println( "</body></html>" );
				return;
			}
			
			final Account   account   = accountList.get( 0 );
			final DbPackage dbPackage = DbPackage.getFromStorage( account.getPaidStorage() );
			
			// File stats
			final List< FileStat > fileStatList = new JQBuilder<>( pm, FileStat.class ).filter( "ownerKey==p1", "KEY p1" ).get( account.getKey() );
			final FileStat fileStat = fileStatList.isEmpty() ? null : fileStatList.get( 0 );
			
			final long storage     = fileStat == null ? 0 : fileStat.getStorage();
			final int  usedPercent = account.getPaidStorage() == 0 ? 0 : (int) ( storage * 100 / account.getPaidStorage() );
			final int  freePercent = 100 - usedPercent;
			
			// Render output HTML
			
			out.println( "<h1 class=\"h1\">Sc2gears&trade; Database User quota</h1>" );
			out.println( "<table border=0><tr valign=middle><td>Your current package:&nbsp;<td>" );
			if ( dbPackage == null )
				out.println( "Unknown" );
			else {
				// ABSOLUTE URL is required because this page is set with "setText()", so relative URLs won't work
				out.println( "<img src=\"https://sciigears.appspot.com/" + ServerUtils.getDbPackageIcon( dbPackage, account.getPaidStorage(), storage ) + "\" width=27 height=27 border=0><td>&nbsp;" + dbPackage.name );
			}
			out.println( "</table><h2 class=\"h2\">Storage usage:</h2>" );
			// Chart
			final int usedPercentData = usedPercent > 100 ? 100 : usedPercent;
			final int freePercentData = usedPercent > 100 ?   0 : freePercent; 
			out.println( "<img src=\"https://chart.googleapis.com/chart?chs=380x130&cht=p3&chco=7777CC|76A4FB&chd=t:" + freePercentData + "," + usedPercentData + "&chl=Free+" + freePercent + "%25|Used+" + usedPercent + "%25&chma=0,0,0,5|0,5\" width=380 height=130 border=0>" );
			out.println( "<h2 class=\"h2\">Storage details:</h2><table border=1 cellpadding=3 cellspacing=0>" );
			out.println( "<tr align=right><td class=\"headerRow\">Available storage:<td>" + DECIMAL_FORMAT.format( account.getPaidStorage() ) + " bytes<td>100%" );
			out.println( "<tr align=right><td class=\"headerRow\">Used storage:<td>"      + DECIMAL_FORMAT.format( storage ) + " bytes<td>" + usedPercent + "%" );
			out.println( "<tr align=right><td class=\"headerRow\">Free storage:<td>"      + DECIMAL_FORMAT.format( account.getPaidStorage() - storage ) + " bytes<td>" + freePercent + "%" );
			out.println( "</table><h2 class=\"h2\">Used storage details:</h2>" );
			if ( fileStat == null || fileStat.getCount() == 0 ) {
				out.println( "There are no uploaded files." );
			}
			else {
				out.println( "<table border=1 cellpadding=3 cellspacing=0>" );
				out.println( "<tr class=\"headerRow\"><td>File type<td>Files<td>Storage used<td>Avg. file size<td>Share" );
				for ( final FileType fileType : FileType.values() ) {
					out.println( "<tr align=right><td align=left>" + ServerUtils.encodeHtmlString( fileType.longName, false )
							+ "<td>" + DECIMAL_FORMAT.format( fileStat.getCount( fileType ) )
							+ "<td>" + DECIMAL_FORMAT.format( fileStat.getStorage( fileType ) ) + " bytes"
							+ "<td>" + DECIMAL_FORMAT.format( fileStat.getCount( fileType ) == 0 ? 0 : fileStat.getStorage( fileType ) / fileStat.getCount( fileType ) ) + " bytes"
							+ "<td>" + ( storage == 0 ? 0 : (int) ( fileStat.getStorage( fileType ) * 100 / storage ) ) + "%" );
				}
				out.println( "</table>" );
			}
			
			out.println( "</body></html>" );
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	/**
	 * Returns private data for a replay.<br>
	 * Sends back an XML with the requested data.
	 * 
	 * <p>Example XML response:<br>
     * <blockquote><pre>
	 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
	 * &lt;privateData docVersion="1.0"&gt;
	 *     &lt;authKeyValid&gt;true&lt;/authKeyValid&gt;
	 *     &lt;replayStored&gt;true&lt;/replayStored&gt;
	 *     &lt;labels&gt;
	 *         &lt;label color="ffffff" bgColor="d4343e" checked="true"&gt;GG&lt;/label&gt;
	 *         &lt;label color="ffffff" bgColor="0042ff" checked="false"&gt;Funny&lt;/label&gt;
	 *     &lt;/labels&gt;
	 *     &lt;comment&gt;Some comment.&lt;/comment&gt;
	 * &lt;/privateData&gt;
     * </pre></blockquote></p>
     * 
	 * @param authorizationKey authorization key of the user
	 */
	private static void getPrivateData( final String authorizationKey, final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		final String sha1 = request.getParameter( PARAM_SHA1 );
		
		LOGGER.fine( "Authorization key: " + authorizationKey + "sha1: " + sha1 );
		
		if ( sha1 == null || sha1.isEmpty() ) {
			LOGGER.warning( "Missing parameters!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
			return;
		}
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			response.setContentType( "text/xml" );
			response.setCharacterEncoding( "UTF-8" );
			// Set no-cache
			setNoCache( response );
			
			// Output contains strings, let's produce XML using a document builder (this will ensure proper encoding of strings)...
			final Document document    = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			final Element  rootElement = document.createElement( "privateData" );
			rootElement.setAttribute( "docVersion", "1.0" );
			document.appendChild( rootElement );
			
			final List< Account > accountList = new JQBuilder<>( pm, Account.class ).filter( "authorizationKey==p1", "String p1" ).get( authorizationKey );
			
			Element element = document.createElement( "authKeyValid" );
			element.setTextContent( Boolean.toString( !accountList.isEmpty() ) );
			rootElement.appendChild( element );
			
			if ( !accountList.isEmpty() ) {
				final Account account = accountList.get( 0 );
				final List< Rep > replayList = new JQBuilder<>( pm, Rep.class ).filter( "ownerk==p1 && sha1==p2", "KEY p1, String p2" ).get( account.getKey(), sha1 );
				
				element = document.createElement( "replayStored" );
				element.setTextContent( Boolean.toString( !replayList.isEmpty() ) );
				rootElement.appendChild( element );
				
				if ( !replayList.isEmpty() ) {
					final Rep            replay = replayList.get( 0 );
					final List< Integer > labels = replay.getLabels();
					
					// Labels
					final Element labelsElement = document.createElement( "labels" );
					final List< String > labelNames = ServerUtils.getLabelNames( account );
					for ( int i = 0; i < labelNames.size(); i++ ) {
						element = document.createElement( "label" );
						element.setTextContent( labelNames.get( i ) );
						element.setAttribute( "checked", Boolean.toString( labels != null && labels.contains( i ) ) );
						element.setAttribute( "color"  , i < Consts.DEFAULT_REPLAY_LABEL_COLORS   .length ? Consts.DEFAULT_REPLAY_LABEL_COLORS   [ i ] : "ffffff" );
						element.setAttribute( "bgColor", i < Consts.DEFAULT_REPLAY_LABEL_BG_COLORS.length ? Consts.DEFAULT_REPLAY_LABEL_BG_COLORS[ i ] : "000000" );
						labelsElement.appendChild( element );
					}
					rootElement.appendChild( labelsElement );
					
					// Comment
					element = document.createElement( "comment" );
					element.setTextContent( replay.getComment() );
					rootElement.appendChild( element );
				}
			}
			
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform( new DOMSource( document ), new StreamResult( response.getOutputStream() ) );
			
		} catch ( final TransformerException te ) {
			LOGGER.log( Level.SEVERE, "", te );
			response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error!" );
        } catch ( final ParserConfigurationException pce ) {
			LOGGER.log( Level.SEVERE, "", pce );
			response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error!" );
        } finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	/**
	 * Saves private data for a replay.
	 * Sends back a plain text response: 1 line exactly, true if data saved successfully; false otherwise.
	 * @param authorizationKey authorization key of the user
	 */
	private static void savePrivateData( final String authorizationKey, final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		final String          sha1    = request.getParameter( PARAM_SHA1       );
		final List< Integer > labels  = getIntListParam( request, PARAM_LABELS );
		String                comment = request.getParameter( PARAM_COMMENT    );
		
		LOGGER.fine( "Authorization key: " + authorizationKey + "sha1: " + sha1 );
		
		if ( sha1 == null || sha1.isEmpty() || labels == null || comment == null ) {
			LOGGER.warning( "Missing parameters!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
			return;
		}
		
		comment = InfoServletApi.trimPrivateRepComment( comment );
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			response.setContentType( "text/plain" );
			// Set no-cache
			setNoCache( response );
			
			final Key accountKey = CachingService.getAccountKeyByAuthKey( pm, authorizationKey );
			if ( accountKey == null ) {
				LOGGER.warning( "Invalid Authorization key!" );
				response.getWriter().println( Boolean.FALSE );
				return;
			}
			
			final List< Rep > replayList = new JQBuilder<>( pm, Rep.class ).filter( "ownerk==p1 && sha1==p2", "KEY p1, String p2" ).get( accountKey, sha1 );
			if ( replayList.isEmpty() ) {
				response.getWriter().println( Boolean.FALSE );
				return;
			}
			
			final Rep replay = replayList.get( 0 );
			replay.setLabels( labels );
			replay.setComment( comment.isEmpty() ? null : comment );
			
			response.getWriter().println( Boolean.TRUE );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
}
