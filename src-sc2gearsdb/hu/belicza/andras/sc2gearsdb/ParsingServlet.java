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

import static hu.belicza.andras.sc2gearsdb.ParsingServletApi.*;
import hu.belicza.andras.mpq.InvalidMpqArchiveException;
import hu.belicza.andras.mpq.MpqParser;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.EnumCache;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.PlayerId;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents.Blink;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents.Message;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents.Text;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gearsdb.ParsingServletApi.IResult;
import hu.belicza.andras.sc2gearsdb.ParsingServletApi.InfoResult;
import hu.belicza.andras.sc2gearsdb.ParsingServletApi.MapInfoResult;
import hu.belicza.andras.sc2gearsdb.ParsingServletApi.ParseRepResult;
import hu.belicza.andras.sc2gearsdb.ParsingServletApi.ProfInfoResult;
import hu.belicza.andras.sc2gearsdb.datastore.ApiAccount;
import hu.belicza.andras.sc2gearsdb.datastore.ApiCallStat;
import hu.belicza.andras.sc2gearsdb.datastore.Map;
import hu.belicza.andras.sc2gearsdb.util.ByteArrayMpqDataInput;
import hu.belicza.andras.sc2gearsdb.util.JQBuilder;
import hu.belicza.andras.sc2gearsdb.util.PMF;
import hu.belicza.andras.sc2gearsdb.util.ServerUtils;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BnetLanguage;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Gateway;
import hu.belicza.andras.util.BnetUtils;
import hu.belicza.andras.util.BnetUtils.Profile;
import hu.belicza.andras.util.BnetUtils.Profile.TeamRank;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

/**
 * The Parsing service servlet.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class ParsingServlet extends BaseServlet {
	
	private static final Logger LOGGER = Logger.getLogger( ParsingServlet.class.getName() );
	
	private static final String     DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private static final DateFormat DATE_TIME_FORMAT         = new SimpleDateFormat( DATE_TIME_FORMAT_PATTERN );
	private static final String     TEXT_UNKNOWN             = Language.getText( "general.unknown" );
	private static String[]         ACTION_TYPE_STRINGS;
	
	static {
		loadResources();
	}
	
	private static class ResponseWrapper extends HttpServletResponseWrapper {
		
		private int status = HttpServletResponse.SC_OK;
		
		/**
		 * Creates a new ResponseWrapper.
		 * @param response
		 */
		public ResponseWrapper( final HttpServletResponse response ) {
			super( response );
		}
		
		@Override
		public void setStatus( final int sc ) {
			status = sc;
			super.setStatus( sc );
		}
		
		@SuppressWarnings( "deprecation" ) // This is due to Tomcat 7 (not applicable to AppEngine)
        @Override
		public void setStatus( final int sc, final String sm ) {
			status = sc;
			super.setStatus( sc, sm );
		}
		
		@Override
		public void sendError( final int sc ) throws IOException {
			status = sc;
			super.sendError( sc );
		}
		
		@Override
		public void sendError( final int sc, final String msg ) throws IOException {
			status = sc;
			super.sendError( sc, msg );
		}
		
		/**
		 * Tells if error is returned by comparing the {@link #status} to {@link HttpServletResponse#SC_OK}.
		 * @return true if error is returned; false otherwise
		 */
		public boolean isError() {
			return status != HttpServletResponse.SC_OK;
		}
		
	}
	
	public static void loadResources() {
		if ( ACTION_TYPE_STRINGS != null )
			return; // Already loaded
		
		// Ensure ability codes repository is initialized when it is needed:
		try {
			Class.forName( ReplayUtils.class.getName() );
			
			final ActionType[] actionTypeValues = ActionType.values();
			ACTION_TYPE_STRINGS = new String[ actionTypeValues.length ];
			for ( int i = 0; i < ACTION_TYPE_STRINGS.length; i++ )
				ACTION_TYPE_STRINGS[ i ] = Character.toString( actionTypeValues[ i ].stringValue.charAt( 0 ) );
			
		} catch ( final ClassNotFoundException cnfe ) {
			LOGGER.log( Level.SEVERE, "Exception during initialization (processing ReplayUtils class)!", cnfe );
		}
	}
	
	/*
	 * doPost() is more common in case of this servlet, so doGet() calls doPost().
	 */
	@Override
	protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		doPost( request, response );
	}
	
	@Override
	protected void doPost( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		final long startNanoTime = System.nanoTime();
		
		final String operation = checkProtVerAndGetOperation( PROTOCOL_VERSION_1, request, response );
		if ( operation == null )
			return;
		
		final String apiKey = request.getParameter( PARAM_API_KEY );
		if ( apiKey == null || apiKey.isEmpty() ) {
			LOGGER.warning( "Missing API key!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing API key!" );
			return;
		}
		
		long               opsCharged      = 0;
		ApiAccount         apiAccount      = null;
		PersistenceManager pm              = null;
		ResponseWrapper    responseWrapper = null;
		boolean            denied          = false;
		
		try {
			pm = PMF.get().getPersistenceManager();
			
			// Check API key
			final List< ApiAccount > apiAccountList = new JQBuilder<>( pm, ApiAccount.class ).filter( "apiKey==p1", "String p1" ).get( apiKey );
			if ( apiAccountList.isEmpty() ) {
				LOGGER.warning( "Unauthorized access, invalid API Key: " + apiKey );
				response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized access, invalid API Key!" );
				return;
			}
			apiAccount = apiAccountList.get( 0 );
			
			responseWrapper = new ResponseWrapper( response );
			
			// Check Ops quota
			final List< ApiCallStat > totalApiCallStatList = new JQBuilder<>( pm, ApiCallStat.class ).filter( "ownerKey==p1 && day==p2", "KEY p1, String p2" ).get( apiAccount.getKey(), ApiCallStat.DAY_TOTAL );
			final long totalUsedOps = totalApiCallStatList.isEmpty() ? 0 : totalApiCallStatList.get( 0 ).getUsedOps();
			if ( !OPERATION_INFO.equals( operation ) && totalUsedOps >= apiAccount.getPaidOps() ) {
				denied = true;
				LOGGER.warning( "Ops quota have been exceeded, serving denied! (API account: " + apiAccount.getUser().getEmail() + ")" );
				responseWrapper.sendError( HttpServletResponse.SC_PAYMENT_REQUIRED, "Ops quota have been exceeded, serving denied!" );
				return;
			}
			
			switch ( operation ) {
			case OPERATION_INFO :
				opsCharged = infoOp( request, responseWrapper, pm, apiAccount );
				break;
			case OPERATION_MAP_INFO :
				opsCharged = mapInfoOp( request, responseWrapper, pm, apiAccount );
				break;
			case OPERATION_PARSE_REPLAY :
				opsCharged = parseRepOp( request, responseWrapper, pm, apiAccount );
				break;
			case OPERATION_PROFILE_INFO :
				opsCharged = profInfoOp( request, responseWrapper, pm, apiAccount );
				break;
			default:
				LOGGER.warning( "Invalid Operation! (API account: " + apiAccount.getUser().getEmail() + ")" );
				responseWrapper.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid Operation!" );
				return;
			}
			
			// Notification available Ops will be checked in the task servlet, update API call stat task
			
		} finally {
			if ( apiAccount != null )
				TaskServlet.register_updateApiCallStat( apiAccount.getKey(), apiAccount.getPaidOps(), apiAccount.getNotificationAvailOps(), opsCharged, ( System.nanoTime() - startNanoTime ) / 1000000l, denied, responseWrapper == null ? true : responseWrapper.isError(), operation );
			if ( pm != null )
				pm.close();
		}
	}
	
	/**
	 * XML builder.
	 * @author Andras Belicza
	 */
	private static class XmlBuilder {
		
		/** The document we build.                             */
		private final Document document;
		/** Parent element where new elements are attached to. */
		private Element        parentElement;
		
		/**
		 * Creates a new XmlBuilder.
		 * @param docVersion document version to set as the {@link ParsingServletApi#XATTR_DOC_VERSION} value for the {@link ParsingServletApi#XTAG_RESPONSE} root element
		 */
		public XmlBuilder( final String docVersion ) throws ParserConfigurationException {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			// Create root element
			parentElement = document.createElement( XTAG_RESPONSE );
			parentElement.setAttribute( XATTR_DOC_VERSION, docVersion );
			document.appendChild( parentElement );
		}
		
		/**
		 * Sets the parent element where new elements are attached to.
		 * @param parentElement parent element to be set; can be <code>null</code> in which case the root element will be set
		 * @return the parent element
		 */
		public Element setParentElement( final Element parentElement ) {
			return this.parentElement = parentElement == null ? document.getDocumentElement() : parentElement;
		}
		
		/**
		 * Returns the parent element where new elements are attached to.
		 * @return the parent element where new elements are attached to
		 */
		public Element getParentElement() { 
			return parentElement;
		}
		
		/**
		 * Creates and attaches a new element to the parent element.
		 * @param elementName name of the new element to be created and attached
		 * @return the created and attached new element
		 */
		public Element createElement( final String elementName ) {
			return createElement( elementName, null, null );
		}
		
		/**
		 * Creates and attaches a new element to the parent element with a {@link ParsingServletApi#XATTR_VALUE} attribute having the specified value.
		 * @param elementName    name of the new element to be created and attached
		 * @param valueAttrValue value of {@link ParsingServletApi#XATTR_VALUE} attribute to be set for the new element
		 * @return the created and attached new element
		 */
		public Element createElement( final String elementName, final Object valueAttrValue ) {
			return createElement( elementName, XATTR_VALUE, valueAttrValue );
		}
		
		/**
		 * Creates and attaches a new element to the parent element with an attribute.
		 * @param elementName name of the new element to be created and attached
		 * @param attrName    name of the attribute to be set
		 * @param attrValue   value of the attribute to set; can be <code>null</code> in which case no attribute will be set
		 * @return the created and attached new element
		 */
		public Element createElement( final String elementName, final String attrName, final Object attrValue ) {
			final Element element = document.createElement( elementName );
			if ( attrValue != null )
				element.setAttribute( attrName, attrValue.toString() );
			parentElement.appendChild( element );
			return element;
		}
		
		/**
		 * Creates a {@link ParsingServletApi#XTAG_RESULT} tag from the specified result and attaches it to the parent element .
		 * @param result result to create an XML tag from
		 */
		public void createResultElement( final IResult result ) {
			createElement( XTAG_RESULT, XATTR_CODE, result.getCode() ).setTextContent( result.getMessage() );
		}
		
		/**
		 * Creates and attaches a new element to the parent element with a value attribute from a {@link Date}.
		 * Also attaches a {@link ParsingServletApi#XATTR_PATTERN} describing the date-time pattern used in the value attribute. 
		 * @param result result to create an XML tag from
		 * @param elementName
		 */
		public void createDateTimeElement( final String elementName, final Date date ) {
			createElement( elementName, DATE_TIME_FORMAT.format( date ) ).setAttribute( XATTR_PATTERN, DATE_TIME_FORMAT_PATTERN );
		}
		
		/**
		 * Prints the document to the specified HTTP servlet response.
		 * @param response response to print the document to
		 */
		public void printDocument( final HttpServletResponse response ) throws TransformerFactoryConfigurationError, TransformerException, IOException {
			response.setContentType( "text/xml" );
			response.setCharacterEncoding( "UTF-8" );
			setNoCache( response );
			
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
			transformer.transform( new DOMSource( document ), new StreamResult( response.getOutputStream() ) );
		}
	}
	
	/**
	 * Info operation.
	 */
	private long infoOp( final HttpServletRequest request, final HttpServletResponse response, final PersistenceManager pm, final ApiAccount apiAccount ) throws IOException {
		Integer daysCount = getIntParam( request, PARAM_DAYS_COUNT  );
		LOGGER.fine( "API account: " + apiAccount.getUser().getEmail() + ", days count: " + daysCount );
		
		if ( daysCount != null ) {
			if ( daysCount < 0 || daysCount > 14 ) {
				LOGGER.warning( "Invalid days count, must be between 0 and 14!" );
				response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid days count, must be between 0 and 14!" );
				return 0;
			}
		}
		else
			daysCount = 2;
		
		try {
			// Total + days count (last days count)
			final List< ApiCallStat > apiCallStatList = new JQBuilder<>( pm, ApiCallStat.class ).filter( "ownerKey==p1 && day==p2", "KEY p1" )
					.range( 0, daysCount + 1 ).desc( "day" ).get( apiAccount.getKey() );
			
			final XmlBuilder xb = new XmlBuilder( "1.0" );
			
			xb.createResultElement( InfoResult.OK );
			xb.createElement( XTAG_ENGINE_VER , ReplayFactory.getVersion() ); // DO NOT USE ReplayFactory.VERSION because the compiler replaces the actual ReplayFactory.VERSION and in live environment not the value from sc2gears-parsing-engine.jar will be used!
			xb.createDateTimeElement( XTAG_SERVER_TIME, new Date() );
			
			xb.createElement( XTAG_PAID_OPS   , apiAccount.getPaidOps() );
			xb.createElement( XTAG_AVAIL_OPS  , apiCallStatList.isEmpty() ? apiAccount.getPaidOps() : apiAccount.getPaidOps() - apiCallStatList.get( 0 ).getUsedOps() );
			
			final Element callStatsElement = xb.createElement( XTAG_CALL_STATS, XATTR_COUNT, apiCallStatList.size() );
			callStatsElement.setAttribute( XATTR_PATTERN, ApiCallStat.DAY_PATTERN );
			for ( final ApiCallStat apiCallStat : apiCallStatList ) {
				xb.setParentElement( callStatsElement );
				xb.setParentElement( xb.createElement( XTAG_CALL_STAT, XATTR_DAY, apiCallStat.getDay() ) );
				xb.createElement( XTAG_API_CALLS    , apiCallStat.getCalls() );
				xb.createElement( XTAG_USED_OPS     , apiCallStat.getUsedOps() );
				xb.createElement( XTAG_AVG_EXEC_TIME, apiCallStat.getAvgExecTime() ).setAttribute( XATTR_UNIT, "ms" );
				xb.createElement( XTAG_DENIED_CALLS , apiCallStat.getDeniedCalls() );
				xb.createElement( XTAG_ERRORS       , apiCallStat.getErrors() );
				
				xb.createElement( XTAG_INFO_CALLS             , apiCallStat.getInfoCalls() );
				xb.createElement( XTAG_AVG_INFO_EXEC_TIME     , apiCallStat.getAvgInfoExecTime() ).setAttribute( XATTR_UNIT, "ms" );
				xb.createElement( XTAG_MAP_INFO_CALLS         , apiCallStat.getMapInfoCalls() );
				xb.createElement( XTAG_AVG_MAP_INFO_EXEC_TIME , apiCallStat.getAvgMapInfoExecTime() ).setAttribute( XATTR_UNIT, "ms" );
				xb.createElement( XTAG_PARSE_REP_CALLS        , apiCallStat.getParseRepCalls() );
				xb.createElement( XTAG_AVG_PARSE_REP_EXEC_TIME, apiCallStat.getAvgParseRepExecTime() ).setAttribute( XATTR_UNIT, "ms" );
				xb.createElement( XTAG_PROF_INFO_CALLS        , apiCallStat.getProfInfoCalls() );
				xb.createElement( XTAG_AVG_PROF_INFO_EXEC_TIME, apiCallStat.getAvgProfInfoExecTime() ).setAttribute( XATTR_UNIT, "ms" );
			}
			
			xb.printDocument( response );
			
			return 0;
		} catch ( final Exception e ) {
			LOGGER.log( Level.SEVERE, "", e );
			response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
			return 0;
		}
	}
	
	/**
	 * Get map info operation.
	 */
	private long mapInfoOp( final HttpServletRequest request, final HttpServletResponse response, final PersistenceManager pm, final ApiAccount apiAccount ) throws IOException {
		String mapFileName = request.getParameter( PARAM_MAP_FILE_NAME );
		
		LOGGER.fine( "API account: " + apiAccount.getUser().getEmail() + ", map file name: " + mapFileName );
		
		if ( mapFileName == null || mapFileName.isEmpty() ) {
			LOGGER.warning( "Missing map file name!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing map file name!" );
			return 0;
		}
		
		if ( ( mapFileName = ServerUtils.checkMapFileName( mapFileName ) ) == null ) {
			LOGGER.warning( "Invalid map file name: " + mapFileName );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid map file name!" );
			return 0;
		}
		
		try {
			final XmlBuilder xb = new XmlBuilder( "1.0" );
			
			long opsCharged = 0;
			
			final List< Map > mapList = new JQBuilder<>( pm, Map.class ).filter( "fname==p1", "String p1" ).get( mapFileName );
			if ( mapList.isEmpty() ) {
				// Register a task to process the map
				TaskServlet.register_processMapTask( mapFileName );
				xb.createResultElement( MapInfoResult.PROCESSING );
			}
			else {
				final Map map = mapList.get( 0 );
				switch ( map.getStatus() ) {
				case Map.STATUS_PROCESSING    : xb.createResultElement( MapInfoResult.PROCESSING     ); break;
				case Map.STATUS_PARSING_ERROR : xb.createResultElement( MapInfoResult.PARSING_ERROR  ); break;
				case Map.STATUS_DL_ERROR      : xb.createResultElement( MapInfoResult.DOWNLOAD_ERROR ); break;
				case Map.STATUS_READY         : {
					opsCharged = 1;
					xb.createResultElement( MapInfoResult.OK );
					Element element = xb.createElement( XTAG_MAP );
					element.setAttribute( XATTR_NAME  , map.getName() );
					element.setAttribute( XATTR_WIDTH , Integer.toString( map.getMwidth () ) );
					element.setAttribute( XATTR_HEIGHT, Integer.toString( map.getMheight() ) );
					element = xb.createElement( XTAG_MAP_IMAGE );
					element.setAttribute( XATTR_FORMAT, "JPEG" );
					element.setAttribute( XATTR_SIZE  , Integer.toString( map.getSize  () ) );
					element.setAttribute( XATTR_WIDTH , Integer.toString( map.getWidth () ) );
					element.setAttribute( XATTR_HEIGHT, Integer.toString( map.getHeight() ) );
					element.setTextContent( javax.xml.bind.DatatypeConverter.printBase64Binary( map.getImage().getBytes() ) );
					break;
				}
				}
			}
			
			xb.printDocument( response );
			
			return opsCharged;
		} catch ( final Exception e ) {
			LOGGER.log( Level.SEVERE, "", e );
			response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
			return 0;
		}
	}
	
	/**
	 * Parse replay operation.
	 */
	private long parseRepOp( final HttpServletRequest request, final HttpServletResponse response, final PersistenceManager pm, final ApiAccount apiAccount ) throws IOException {
		final String  fileContent = request.getParameter( PARAM_FILE_CONTENT );
		final Integer fileLength  = getIntParam( request, PARAM_FILE_LENGTH  );
		LOGGER.fine( "API account: " + apiAccount.getUser().getEmail() + ", file length: " + fileLength );
		if ( fileContent == null || fileContent.isEmpty() || fileLength == null ) {
			LOGGER.warning( "Missing parameters!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
			return 0;
		}
		
		final byte[] decodedFileContent = ServerUtils.decodeBase64String( fileContent );
		if ( decodedFileContent == null ) {
			LOGGER.warning( "Invalid Base64 encoded file content!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid Base64 encoded file content!" );
			return 0;
		}
		if ( decodedFileContent.length != fileLength ) {
			LOGGER.warning( "Supplied file length does not match decoded file content length: " + fileLength + " != " + decodedFileContent.length );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Supplied file length does not match decoded file content length!" );
			return 0;
		}
		
		try {
			long opsCharged = 1;
			
			final Boolean parseMessagesParam = getBooleanParam( request, PARAM_PARSE_MESSAGES );
			final Boolean parseActionsParam  = getBooleanParam( request, PARAM_PARSE_ACTIONS  );
			
			// Default or requested values:
			final boolean parseMessages = parseMessagesParam == null ? true : parseMessagesParam;
			final boolean parseActions  = parseActionsParam  == null ? true : parseActionsParam ;
			
			final Set< ReplayContent > contentToExtractSet = EnumSet.copyOf( ReplayFactory.GENERAL_INFO_CONTENT );
			if ( parseMessages )
				contentToExtractSet.add( ReplayContent.MESSAGE_EVENTS );
			if ( parseActions ) {
				contentToExtractSet.add( ReplayContent.GAME_EVENTS );
				opsCharged++;
			}
			
			final Replay replay = ReplayFactory.parseReplay( "attachedFile.SC2Replay", new MpqParser( new ByteArrayMpqDataInput( decodedFileContent ) ), contentToExtractSet );
			
			final XmlBuilder xb = new XmlBuilder( "1.0" );
			xb.createResultElement( replay == null ? ParseRepResult.PARSING_ERROR : ParseRepResult.OK );
			xb.createElement( XTAG_ENGINE_VER , ReplayFactory.getVersion() );
			if ( replay == null ) {
				LOGGER.fine( "Replay parsing error!" );
				xb.printDocument( response );
				return opsCharged;
			}
			
			final Element repInfoElement = xb.setParentElement( xb.createElement( XTAG_REP_INFO ) );
			xb.createElement( XTAG_VERSION       , replay.version );
			xb.createElement( XTAG_EXPANSION     , replay.details.expansion );
			final Element gameLengthSecElement = xb.createElement( XTAG_GAME_LENGTH, replay.converterGameSpeed.convertToRealTime( replay.gameLengthSec ) );
			gameLengthSecElement.setAttribute( XATTR_UNIT, "sec" );
			gameLengthSecElement.setAttribute( XATTR_GAME_TIME_VALUE, Integer.toString( replay.gameLengthSec ) );
			xb.createElement( XTAG_GAME_LENGTH   , replay.frames ).setAttribute( XATTR_UNIT, "frame" );
			xb.createElement( XTAG_GAME_TYPE     , replay.initData.gameType );
			if ( replay.initData.competitive != null )
				xb.createElement( XTAG_IS_COMPETITIVE, replay.initData.competitive );
			xb.createElement( XTAG_GAME_SPEED    , replay.initData.gameSpeed );
			xb.createElement( XTAG_FORMAT        , replay.initData.format );
			xb.createElement( XTAG_GATEWAY       , replay.initData.gateway );
			xb.createElement( XTAG_MAP_FILE_NAME , replay.initData.mapFileName );
			xb.setParentElement( xb.createElement( XTAG_CLIENTS ) );
			final Player[] players = replay.details.players;
			final String[] arrangedClientNames = replay.initData.getArrangedClientNames( players );
			xb.getParentElement().setAttribute( XATTR_COUNT, Integer.toString( arrangedClientNames.length ) );
			for ( int i = 0; i < arrangedClientNames.length; i++ )
				xb.createElement( XTAG_CLIENT       , arrangedClientNames[ i ] ).setAttribute( XATTR_INDEX, Integer.toString( i ) );
			xb.setParentElement( repInfoElement );
			xb.createElement( XTAG_MAP_NAME      , replay.details.originalMapName );
			xb.createDateTimeElement( XTAG_SAVE_TIME, new Date( replay.details.saveTime ) );
			xb.createElement( XTAG_SAVE_TIME_ZONE, String.format( Locale.US, "%+.2f", replay.details.saveTimeZone ) );
			final Element playersElement = xb.createElement( XTAG_PLAYERS );
			xb.setParentElement( playersElement );
			playersElement.setAttribute( XATTR_COUNT, Integer.toString( players.length ) );
			for ( int i = 0; i < players.length; i++ ) {
				final Player player = players[ i ];
				xb.setParentElement( playersElement );
				xb.setParentElement( xb.createElement( XTAG_PLAYER, XATTR_INDEX, Integer.toString( i ) ) );
				final Element playerElement = xb.createElement( XTAG_PLAYER_ID, XATTR_NAME, player.playerId.name );
				playerElement.setAttribute( XATTR_BNET_ID    , Integer.toString( player.playerId.battleNetId ) );
				playerElement.setAttribute( XATTR_BNET_SUBID , Integer.toString( player.playerId.battleNetSubId ) );
				playerElement.setAttribute( XATTR_GATEWAY    , player.playerId.gateway.toString() );
				playerElement.setAttribute( XATTR_GW_CODE    , player.playerId.gateway.binaryValue );
				playerElement.setAttribute( XATTR_REGION     , player.playerId.getRegion().toString() );
				playerElement.setAttribute( XATTR_PROFILE_URL, player.playerId.getBattleNetProfileUrl( player.playerId.gateway.defaultLanguage ) );
				xb.createElement( XTAG_TEAM, player.team == Player.TEAM_UNKNOWN ? TEXT_UNKNOWN : player.team );
				xb.createElement( XTAG_RACE, player.race );
				xb.createElement( XTAG_FINAL_RACE, player.finalRace );
				xb.createElement( XTAG_LEAGUE, player.getLeague() );
				xb.createElement( XTAG_SWARM_LEVELS, player.getSwarmLevels() );
				final Element colorElement = xb.createElement( XTAG_COLOR, XATTR_NAME, player.playerColor );
				colorElement.setAttribute( XATTR_RED  , Integer.toString( player.argbColor[ 1 ] ) );
				colorElement.setAttribute( XATTR_GREEN, Integer.toString( player.argbColor[ 2 ] ) );
				colorElement.setAttribute( XATTR_BLUE , Integer.toString( player.argbColor[ 3 ] ) );
				xb.createElement( XTAG_TYPE, player.type );
				xb.createElement( XTAG_DIFFICULTY, player.difficulty );
				xb.createElement( XTAG_HANDICAP, player.handicap );
				xb.createElement( XTAG_IS_WINNER, player.isWinner == null ? TEXT_UNKNOWN : player.isWinner );
				
				if ( parseActions ) {
					xb.createElement( XTAG_ACTIONS_COUNT, player.actionsCount );
					xb.createElement( XTAG_EFFECTIVE_ACTIONS_COUNT, player.effectiveActionsCount );
					xb.createElement( XTAG_LAST_ACTION_FRAME, player.lastActionFrame );
					xb.createElement( XTAG_APM , ReplayUtils.calculatePlayerApm ( replay, player ) ).setAttribute( XATTR_EXCLUDED_ACTIONS_COUNT, Integer.toString( player.excludedActionsCount ) );
					xb.createElement( XTAG_EAPM, ReplayUtils.calculatePlayerEapm( replay, player ) ).setAttribute( XATTR_EXCLUDED_ACTIONS_COUNT, Integer.toString( player.excludedEffectiveActionsCount ) );
					Float fvalue;
					xb.createElement( XTAG_AVG_SPAWNING_RATIO, ( fvalue = player.getAverageSpawningRatio() ) == null ? TEXT_UNKNOWN : (int) ( fvalue * 100 ) ).setAttribute( XATTR_UNIT, "%" );
					xb.createElement( XTAG_AVG_INJECTION_GAP , ( fvalue = player.getAverageInjectionGap () ) == null ? TEXT_UNKNOWN : ReplayUtils.formatFramesDecimal( fvalue.intValue(), replay.converterGameSpeed ) ).setAttribute( XATTR_UNIT, "sec" );
				}
			}
			
			if ( parseMessages ) {
				xb.setParentElement( null ); // Root element
				final Element inGameChatElement = xb.createElement( XTAG_IN_GAME_CHAT, XATTR_COUNT, replay.messageEvents.messages.length );
				inGameChatElement.setAttribute( XATTR_PATTERN, "HH:mm:ss" );
				xb.setParentElement( inGameChatElement );
				int ms = 0;
				for ( final Message message : replay.messageEvents.messages ) {
					ms += message.time;
					final Element messageElement = xb.createElement( message instanceof Text ? XTAG_TEXT : XTAG_PING, XATTR_CLIENT_INDEX, message.client );
					messageElement.setAttribute( XATTR_CLIENT, arrangedClientNames[ message.client ] );
					messageElement.setAttribute( XATTR_TIME, ReplayUtils.formatMs( ms, replay.converterGameSpeed ) );
					if ( message instanceof Text ) {
						messageElement.setAttribute( XATTR_VALUE , ( (Text) message ).text );
						final byte opCode = ( (Text) message ).opCode;
						messageElement.setAttribute( XATTR_TARGET, opCode == MessageEvents.OP_CODE_CHAT_TO_ALL ? "all" : opCode == MessageEvents.OP_CODE_CHAT_TO_ALLIES ? "allies" : opCode == MessageEvents.OP_CODE_CHAT_TO_OBSERVERS ? "observers" : "unknown" );
					}
					else if ( message instanceof Blink ) {
						messageElement.setAttribute( XATTR_X, ReplayUtils.formatCoordinate( ( (Blink) message ).x ) );
						messageElement.setAttribute( XATTR_Y, ReplayUtils.formatCoordinate( ( (Blink) message ).y ) );
					}
				}
			}
			
			if ( parseActions ) {
				final Boolean sendActionsSelectParam   = getBooleanParam( request, PARAM_SEND_ACTIONS_SELECT   );
				final Boolean sendActionsBuildParam    = getBooleanParam( request, PARAM_SEND_ACTIONS_BUILD    );
				final Boolean sendActionsTrainParam    = getBooleanParam( request, PARAM_SEND_ACTIONS_TRAIN    );
				final Boolean sendActionsResearchParam = getBooleanParam( request, PARAM_SEND_ACTIONS_RESEARCH );
				final Boolean sendActionsUpgradeParam  = getBooleanParam( request, PARAM_SEND_ACTIONS_UPGRADE  );
				final Boolean sendActionsOtherParam    = getBooleanParam( request, PARAM_SEND_ACTIONS_OTHER    );
				final Boolean sendActionsInactionParam = getBooleanParam( request, PARAM_SEND_ACTIONS_INACTION );
				
				// Default or requested values:
				final boolean sendActionsSelect   = sendActionsSelectParam   == null ? false : sendActionsSelectParam  ;
				final boolean sendActionsBuild    = sendActionsBuildParam    == null ? true  : sendActionsBuildParam   ;
				final boolean sendActionsTrain    = sendActionsTrainParam    == null ? true  : sendActionsTrainParam   ;
				final boolean sendActionsResearch = sendActionsResearchParam == null ? true  : sendActionsResearchParam;
				final boolean sendActionsUpgrade  = sendActionsUpgradeParam  == null ? true  : sendActionsUpgradeParam ;
				final boolean sendActionsOther    = sendActionsOtherParam    == null ? false : sendActionsOtherParam   ;
				final boolean sendActionsInaction = sendActionsInactionParam == null ? false : sendActionsInactionParam;
				
				final Set< ActionType > sendActionTypeSet = EnumSet.noneOf( ActionType.class );
				if ( sendActionsSelect   ) sendActionTypeSet.add( ActionType.SELECT   );
				if ( sendActionsBuild    ) sendActionTypeSet.add( ActionType.BUILD    );
				if ( sendActionsTrain    ) sendActionTypeSet.add( ActionType.TRAIN    );
				if ( sendActionsResearch ) sendActionTypeSet.add( ActionType.RESEARCH );
				if ( sendActionsUpgrade  ) sendActionTypeSet.add( ActionType.UPGRADE  );
				if ( sendActionsOther    ) sendActionTypeSet.add( ActionType.OTHER    );
				if ( sendActionsInaction ) sendActionTypeSet.add( ActionType.INACTION );
				
				if ( sendActionsSelect || sendActionsOther )
					opsCharged++;
				if ( sendActionsInaction )
					opsCharged++;
				
				xb.setParentElement( null ); // Root element
				final Element actionsElement = xb.createElement( XTAG_ACTIONS, XATTR_ALL_ACTIONS_COUNT, replay.gameEvents.actions.length );
				actionsElement.setAttribute( XATTR_ERROR_PARSING, Boolean.toString( replay.gameEvents.errorParsing ) );
				xb.setParentElement( actionsElement );
				
				int count = 0; // Sent actions count
				if ( !sendActionTypeSet.isEmpty() ) {
					final StringBuilder actionStringBuilder = new StringBuilder();
					for ( final Action action : replay.gameEvents.actions )
						if ( sendActionTypeSet.contains( action.type ) ) {
							count++;
							final Element actionElement = xb.createElement( XTAG_ACTION_, XATTR_PLAYER_, action.player );
							actionElement.setAttribute( XATTR_TYPE_ , ACTION_TYPE_STRINGS[ action.type.ordinal() ] );
							actionElement.setAttribute( XATTR_FRAME_, Integer.toString( action.frame ) );
							actionStringBuilder.setLength( 0 );
							action.customToString( actionStringBuilder );
							actionElement.setAttribute( XATTR_STRING_, actionStringBuilder.toString() );
						}
				}
				actionsElement.setAttribute( XATTR_COUNT, Integer.toString( count ) );
			}
			
			xb.printDocument( response );
			
			return opsCharged;
		} catch ( final InvalidMpqArchiveException imae ) {
			LOGGER.log( Level.WARNING, "", imae );
			response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid SC2Replay file!" );
			return 0;
		} catch ( final Exception e ) {
			LOGGER.log( Level.SEVERE, "", e );
			response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
			return 0;
		}
	}
	
	/**
	 * Profile info operation.
	 */
    private long profInfoOp( final HttpServletRequest request, final HttpServletResponse response, final PersistenceManager pm, final ApiAccount apiAccount ) throws IOException {
		LOGGER.fine( "API account: " + apiAccount.getUser().getEmail() );
		
		final Integer bnetId;
		final Integer bnetSubId;
		final String  gatewayString;
		final String  playerName;
		final Gateway gateway;
		
		final String  bnetProfileUrlParam = request.getParameter( PARAM_BNET_PROFILE_URL );
    	if ( bnetProfileUrlParam == null || bnetProfileUrlParam.isEmpty() ) {
    		// Player id is provided explicitly
    		bnetId        = getIntParam( request, PARAM_BNET_ID     );
    		bnetSubId     = getIntParam( request, PARAM_BNET_SUBID  );
    		gatewayString = request.getParameter( PARAM_GATEWAY     );
    		playerName    = request.getParameter( PARAM_PLAYER_NAME );
    		if ( bnetId == null || bnetSubId == null || gatewayString == null || gatewayString.isEmpty() || playerName == null || playerName.isEmpty() ) {
    			LOGGER.warning( "Missing parameters!" );
    			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
    			return 0;
    		}
    		gateway = Gateway.fromBinaryValue( gatewayString );
    		if ( gateway == Gateway.UNKNOWN || gateway == Gateway.PUBLIC_TEST ) {
    			LOGGER.warning( "Invalid gateway parameter: " + gatewayString );
    			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid gateway parameter!" );
    			return 0;
    		}
    	}
    	else {
    		// Player id is provided through his/her Battle.net profile URL
    		try {
        		Gateway foundGateway = null;
        		for ( final Gateway gateway_ : EnumCache.GATEWAYS )
        			if ( bnetProfileUrlParam.startsWith( gateway_.bnetUrl ) ) {
        				foundGateway = gateway_;
        				break;
        			}
        		if ( foundGateway == null || foundGateway == Gateway.UNKNOWN || foundGateway == Gateway.PUBLIC_TEST )
        			throw new Exception( "No matching gateway!" );
        		gateway = foundGateway;
        		
    			final String[] urlParts = bnetProfileUrlParam.split( "/" );
    			if ( urlParts.length < 3 )
    				throw new Exception( "Not enough parts in URL!" );
    			
    			playerName = URLDecoder.decode ( urlParts[ urlParts.length-1 ], "UTF-8" );
    			bnetSubId  = Integer   .valueOf( urlParts[ urlParts.length-2 ]          );
    			bnetId     = Integer   .valueOf( urlParts[ urlParts.length-3 ]          );
    			
    		} catch ( final Exception e ) {
    			LOGGER.log( Level.SEVERE, "Invalid Battle.net profile URL: " + bnetProfileUrlParam, e );
    			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid Battle.net profile URL!" );
    			return 0;
    		}
    	}
		
		long opsCharged = 1;
		
		final Boolean retrieveExtInfoParam = getBooleanParam( request, PARAM_RETRIEVE_EXT_INFO );
		// Default or requested values:
		final boolean retrieveExtInfo = retrieveExtInfoParam == null ? false : retrieveExtInfoParam;
		
		try {
			final PlayerId playerId = new PlayerId();
			playerId.battleNetId    = bnetId;
			playerId.battleNetSubId = bnetSubId;
			playerId.gateway        = gateway;
			playerId.name           = playerName;
			
			final String bnetProfileUrl = playerId.getBattleNetProfileUrl( BnetLanguage.ENGLISH );
			
			LOGGER.fine( "Bnet profile URL: " + bnetProfileUrl );
			
			final URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
			
			// Default deadline: 5 seconds... increase it!
			final HTTPRequest profileRequest = new HTTPRequest( new URL( bnetProfileUrl ) );
			profileRequest.getFetchOptions().setDeadline( 50.0 ); // 50-sec deadline (leaving 10 seconds to process...
			final Future< HTTPResponse > profileFetchAsync = urlFetchService.fetchAsync( profileRequest );
			
			final Future< HTTPResponse > extProfileFetchAsync; 
			if ( retrieveExtInfo ) {
				opsCharged++;
				// Start retrieving extended profile info in parallel
				final HTTPRequest extProfileRequest = new HTTPRequest( new URL( bnetProfileUrl + "ladder/leagues" ) );
				extProfileRequest.getFetchOptions().setDeadline( 50.0 ); // 50-sec deadline (leaving 10 seconds to process...
				extProfileFetchAsync = urlFetchService.fetchAsync( extProfileRequest );
			}
			else
				extProfileFetchAsync = null;
			
			final XmlBuilder xb = new XmlBuilder( "1.1" );
			
			HTTPResponse profileResponse = null;
			Profile      profile         = null;
			Element      profInfoElement = null;
			try {
				profileResponse = profileFetchAsync.get();
				
				switch ( profileResponse.getResponseCode() ) {
				case HttpServletResponse.SC_OK : {
					final byte[] content = profileResponse.getContent();
					if ( content.length == 0 )
						throw new Exception( "Content length = 0!" );
					profile = BnetUtils.retrieveProfile( null, new ByteArrayInputStream( content ) );
					if ( profile != null ) {
						LOGGER.fine( "Parse OK" );
						
						xb.createResultElement( ProfInfoResult.OK );
						opsCharged += 2;
						
						profInfoElement = xb.setParentElement( xb.createElement( XTAG_PROFILE_INFO ) );
						// Re-include player id
						final Element playerElement = xb.createElement( XTAG_PLAYER_ID, XATTR_NAME, playerName );
						playerElement.setAttribute( XATTR_BNET_ID    , bnetId.toString() );
						playerElement.setAttribute( XATTR_BNET_SUBID , bnetSubId.toString() );
						playerElement.setAttribute( XATTR_GATEWAY    , gateway.toString() );
						playerElement.setAttribute( XATTR_GW_CODE    , gateway.binaryValue );
						playerElement.setAttribute( XATTR_REGION     , playerId.getRegion().toString() );
						playerElement.setAttribute( XATTR_PROFILE_URL, bnetProfileUrl );
						final Element portraitElement = xb.createElement( XTAG_PORTRAIT, XATTR_GROUP, profile.portraitGroup );
						portraitElement.setAttribute( XATTR_ROW, Integer.toString( profile.portraitRow ) );
						portraitElement.setAttribute( XATTR_COLUMN, Integer.toString( profile.portraitColumn ) );
						xb.createElement( XTAG_ACHIEVEMENT_POINTS, profile.achievementPoints       );
						xb.createElement( XTAG_TOTAL_CAREER_GAMES, profile.totalCareerGames        );
						xb.createElement( XTAG_GAMES_THIS_SEASON , profile.gamesThisSeason         );
						xb.createElement( XTAG_TERRAN_WINS       , profile.terranWins              );
						xb.createElement( XTAG_ZERG_WINS         , profile.zergWins                );
						xb.createElement( XTAG_PROTOSS_WINS      , profile.protossWins             );
						final Element highestSoloFlElement = xb.createElement( XTAG_HIGHEST_SOLO_FL, profile.highestSoloFinishLeague );
						if ( profile.highestSoloFinishTimes > 0 )
							highestSoloFlElement.setAttribute( XATTR_TIMES_ACHIEVED, Integer.toString( profile.highestSoloFinishTimes ) );
						final Element highestTeamFlElement = xb.createElement( XTAG_HIGHEST_TEAM_FL, profile.highestTeamFinishLeague );
						if ( profile.highestTeamFinishTimes > 0 )
							highestTeamFlElement.setAttribute( XATTR_TIMES_ACHIEVED, Integer.toString( profile.highestTeamFinishTimes ) );
						
						break;
					}
					else {
						LOGGER.fine( "Parse error!" );
						xb.createResultElement( ProfInfoResult.PARSING_ERROR ); // Parse fails
					}
				}
				case HttpServletResponse.SC_NOT_FOUND :
					LOGGER.fine( "Invalid player!" );
					xb.createResultElement( ProfInfoResult.INVALID_PLAYER );
					break;
				default :
					// Treat other response HTTP status codes as BNET_ERROR
					throw new Exception( "Response code: " + profileResponse.getResponseCode() );
				}
				
			} catch ( final Exception e ) {
				LOGGER.log( Level.SEVERE, "", e );
				xb.createResultElement( ProfInfoResult.BNET_ERROR );
			} finally {
				if ( retrieveExtInfo && profile == null )
					extProfileFetchAsync.cancel( true );
			}
			
			if ( retrieveExtInfo && profile != null ) {
				try {
					profileResponse = extProfileFetchAsync.get();
					
					final byte[] content;
					if ( profileResponse.getResponseCode() == HttpServletResponse.SC_OK && ( content = profileResponse.getContent() ).length > 0 ) {
						profile = BnetUtils.retrieveExtProfile( null, new ByteArrayInputStream( content ), profile );
						if ( profile != null ) {
							LOGGER.fine( "Parse extended OK" );
							
							opsCharged += 2;
							xb.setParentElement( profInfoElement );
							final Element allRankGroupsElement = xb.setParentElement( xb.createElement( XTAG_ALL_RANK_GROUPS ) );
							int allRankGroupsCount = 0;
							for ( int bracket = 0; bracket < profile.allRankss.length; bracket++ ) {
								final TeamRank[] allRanks = profile.allRankss[ bracket ];
								if ( allRanks != null && allRanks.length > 0 ) {
									allRankGroupsCount++;
    								xb.setParentElement( allRankGroupsElement );
    								final Element allRankGroupElement = xb.createElement( XTAG_ALL_RANK_GROUP, XATTR_COUNT, allRanks.length );
    								allRankGroupElement.setAttribute( XATTR_FORMAT, (bracket+1) + "v" + (bracket+1) );
    								
    								for ( int i = 0; i < allRanks.length; i++ ) {
    									xb.setParentElement( allRankGroupElement );
    									final Element teamRankElement = xb.setParentElement( xb.createElement( XTAG_TEAM_RANK, XATTR_LEAGUE, allRanks[ i ].league.stringValue ) );
    									teamRankElement.setAttribute( XATTR_DIVISION_RANK, Integer.toString( allRanks[ i ].divisionRank ) );
    									// Team members
    									xb.setParentElement( xb.createElement( XTAG_TEAM_MEMBERS, XATTR_COUNT, allRanks[ i ].teamMembers.length ) );
    									for ( final String memberName :  allRanks[ i ].teamMembers )
    										xb.createElement( XTAG_TEAM_MEMBER, XATTR_NAME, memberName );
    								}
								}
							}
							allRankGroupsElement.setAttribute( XATTR_COUNT, Integer.toString( allRankGroupsCount ) );
						}
						else
							LOGGER.fine( "Parse extended error!" );
					}
				} catch ( final Exception e ) {
					LOGGER.log( Level.SEVERE, "Failed to get extended profile info, we return the basic profile info silently.", e );
					// Failed to get extended profile info, we return the basic profile info silently
				}
			}
			
			xb.printDocument( response );
			
			return opsCharged;
			
		} catch ( final Exception e ) {
			LOGGER.log( Level.SEVERE, "", e );
			response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
			return 0;
		}
	}
	
}
