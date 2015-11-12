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

import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.HEADER_X_FILE_DATE;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.HEADER_X_FILE_NAME;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.OPERATION_BATCH_DOWNLOAD;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.OPERATION_DOWNLOAD;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.OPERATION_RETRIEVE_FILE_LIST;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.OPERATION_STORE;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_AFTER_DATE;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_BEFORE_DATE;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_COMMENT;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_FILE_CONTENT;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_FILE_NAME;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_FILE_TYPE;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_FORMAT;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_GAME_LENGTH;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_GAME_TYPE;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_GATEWAY;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_LAST_MODIFIED;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_MAP_FILE_NAME;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_MAP_NAME;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_PLAYERS;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_PLAYER_TEAMS;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_LEAGUE_MATCHUP;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_RACE_MATCHUP;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_RECORDING_END;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_RECORDING_START;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_REPLAY_DATE;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_SAMPLES_COUNT;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_SAMPLING_TIME;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_SAVED_WITH_COMPRESSION;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_SCREEN_HEIGHT;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_SCREEN_RESOLUTION;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_SCREEN_WIDTH;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_SHA1;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_SHA1_LIST;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_SHARED_ACCOUNT;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_UNCOMPRESSED_DATA_SIZE;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_VERSION;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_WINNERS;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PROTOCOL_VERSION_1;
import static hu.belicza.andras.sc2gearsdbapi.ServletApi.PARAM_AUTHORIZATION_KEY;
import hu.belicza.andras.sc2gearsdb.datastore.Account;
import hu.belicza.andras.sc2gearsdb.datastore.FileMetaData;
import hu.belicza.andras.sc2gearsdb.datastore.FileStat;
import hu.belicza.andras.sc2gearsdb.datastore.OtherFile;
import hu.belicza.andras.sc2gearsdb.datastore.Rep;
import hu.belicza.andras.sc2gearsdb.datastore.Smpd;
import hu.belicza.andras.sc2gearsdb.user.client.Permission;
import hu.belicza.andras.sc2gearsdb.util.JQBuilder;
import hu.belicza.andras.sc2gearsdb.util.PMF;
import hu.belicza.andras.sc2gearsdb.util.ServerUtils;
import hu.belicza.andras.sc2gearsdbapi.FileServletApi;
import hu.belicza.andras.sc2gearsdbapi.FileServletApi.FileType;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * A servlet that handles storing and downloading (serving) files.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class FileServlet extends BaseServlet {
	
	private static final Logger LOGGER = Logger.getLogger( FileServlet.class.getName() );
	
	/**
	 * Size limit to store file content in the Datastore.
	 * If the file size is bigger than or equals to this, it will be stored in the Blobstore.
	 * <p>This limit is calculated based on:
	 * <ul>
	 * 	<li><code>__BlobInfo__</code> entity size: <code>7,822 Bytes</code>
	 * 	<li>A <code>blobKey</code> reference size: <code>190 Bytes</code>
	 * 	<li>A <code>blobKey</code> index size: <code>460 Bytes</code>
	 * 	<li>A <code>NULL</code> value for the <code>content</code> property: <code>1 Byte</code>
	 * 	<li><code>NULL</code> index size for the <code>content</code> property: <code>15 Bytes</code>
	 * 	<li>Total Datastore size required for a Blobstore file: <code>c = 7822 + 190 + 460 + 15 + 1 = 8,488 Bytes</code>
	 * 	<li>Total Datastore size required if <code>blobKey</code> and <code>content</code> are unindexed: <code>c = 7822 + 190 + 1 = 8,013 Bytes</code>
	 * </ul>
	 * This is the constant data size in the Datastore required to store a file in the Blobstore,
	 * this is what we gain if we store the content in the Datastore instead of the Blobstore. If a file is smaller than this,
	 * no question it is cheaper to place it in the Datastore. If the file is bigger than this, then the cost increases based on the file size
	 * because Datastore is more expensive than the Blobstore. Datastore cost: 0.008 GB-day; Blobstore cost: 0.0043 GB-day<br>
	 * <br>
	 * Equation to calculate the turning point (when storing the file would cost the same in case of the Datastore and Blobstore):
	 * <pre>
	 * Datastore constant cost + Blobstore cost    = Datastore only cost
	 * 0.008 * c               + 0.0043 * fileSize = 0.008 * fileSize
	 * => fileSize                                      = 2.162162162 * c = 18,352 Bytes
	 * => fileSize if blobKey and content are unindexed = 2.162162162 * c = 17,325 Bytes
	 * </pre>
	 * 
	 * <b>On May 24, 2013 Datastore cost has been decreased to 0.006 USD/GB-day!</b> (down from 0.008 USD/GB-day)
	 * <pre>
	 * Datastore constant cost + Blobstore cost    = Datastore only cost
	 * 0.006 * c               + 0.0043 * fileSize = 0.006 * fileSize
	 * => fileSize                                      = 3.529411764 * c = 29,957 Bytes
	 * => fileSize if blobKey and content are unindexed = 3.529411764 * c = 28,281 Bytes
	 * </pre>
	 * 
	 * <b>On April 1, 2014 Blobstore cost has been decreased to 0.0009 USD/GB-day!</b> (down from 0.0043 USD/GB-day)
	 * <pre>
	 * Datastore constant cost + Blobstore cost    = Datastore only cost
	 * 0.006 * c               + 0.0009 * fileSize = 0.006 * fileSize
	 * => fileSize                                      = 1.176470588 * c =  9,986 Bytes
	 * => fileSize if blobKey and content are unindexed = 1.176470588 * c =  9,427 Bytes
	 * </pre>
	 * </p>
	 */
	//public static final int DATASTORE_CONTENT_STORE_LIMIT = 17325;
	//public static final int DATASTORE_CONTENT_STORE_LIMIT = 28281; // Effective May 27, 2013
	public static final int DATASTORE_CONTENT_STORE_LIMIT = 10_000; // Effective April 2, 2014; I used a little bigger (instead of 9427) to reflect the cost of write ops
	
	
	/** MIME types of the file types. */
	public static final Map< FileType, String >                          FILE_TYPE_MIME_TYPE_MAP = new EnumMap< FileType, String >                         ( FileType.class );
	public static final Map< FileType, Class< ? extends FileMetaData > > FILE_TYPE_CLASS_MAP     = new EnumMap< FileType, Class< ? extends FileMetaData > >( FileType.class );
	static {
		FILE_TYPE_MIME_TYPE_MAP.put( FileType.SC2REPLAY  , "application/x-sc2replay" );
		FILE_TYPE_MIME_TYPE_MAP.put( FileType.MOUSE_PRINT, "application/x-smpd"      );
		FILE_TYPE_MIME_TYPE_MAP.put( FileType.OTHER      , "application/x-other"     );
		
		FILE_TYPE_CLASS_MAP.put( FileType.SC2REPLAY  , Rep      .class );
		FILE_TYPE_CLASS_MAP.put( FileType.MOUSE_PRINT, Smpd     .class );
		FILE_TYPE_CLASS_MAP.put( FileType.OTHER      , OtherFile.class );
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
		if (true) {
			response.sendError(HttpServletResponse.SC_GONE, "This URL is gone and will not be available anymore!");
			return;
		}
		
		final String operation = checkProtVerAndGetOperation( PROTOCOL_VERSION_1, request, response );
		if ( operation == null )
			return;
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			final String authorizationKey = request.getParameter( PARAM_AUTHORIZATION_KEY );
			
			// AUTHENTICATION FIRST
			// Either authorization key must be provided or a valid Google account must be authenticated!
			final Account account;
			final User    user;
			final String sharedAccount = request.getParameter( PARAM_SHARED_ACCOUNT );
			if ( authorizationKey == null || authorizationKey.isEmpty() ) {
				// Google account authentication
				final UserService userService = UserServiceFactory.getUserService();
				user = userService.getCurrentUser();
				
				if ( user == null ) {
					LOGGER.warning( "Unauthorized access, not logged in!" );
					response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized access, you are not logged in!" );
					return;
				}
				account = ServerUtils.getAccount( pm, sharedAccount, user );
				if ( account == null ) {
					LOGGER.warning( "Unauthorized access: Google account: " + user.getEmail() + ( sharedAccount == null ? "" : ", shared account: " + sharedAccount ) );
					response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized access!" );
					return;
				}
			}
			else {
				// Authorization key authentication
				final List< Account > accountList = new JQBuilder<>( pm, Account.class ).filter( "authorizationKey==p1", "String p1" ).get( authorizationKey );
				if ( accountList.isEmpty() ) {
					LOGGER.warning( "Unauthorized access, invalid Authorization Key: " + authorizationKey );
					response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized access, invalid Authorization Key!" );
					return;
				}
				account = accountList.get( 0 );
				user    = null;
			}
			
			// ...Authentication OK
			
			switch ( operation ) {
			case OPERATION_STORE :
				storeFile( request, response, pm, account, sharedAccount );
				break;
			case OPERATION_DOWNLOAD :
				serveFile( request, response, pm, account, sharedAccount, user );
				break;
			case OPERATION_BATCH_DOWNLOAD :
				serveFileBatch( request, response, pm, account, sharedAccount, user );
				break;
			case OPERATION_RETRIEVE_FILE_LIST :
				serveFileList( request, response, pm, account, sharedAccount );
				break;
			default:
				LOGGER.warning( "Invalid Operation! (Account: " + account.getUser().getEmail() + ")" );
				response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid Operation!" );
				return;
			}
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	/**
	 * Stores a file from the request.<br>
	 * Sends back a plain text response with a line "true" if the file is stored successfully.
	 */
	private static void storeFile( final HttpServletRequest request, final HttpServletResponse response, final PersistenceManager pm, final Account account, final String sharedAccount ) throws IOException {
		if ( sharedAccount != null ) {
			LOGGER.warning( "Unauthorized Access! Shared account: " + sharedAccount );
			response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized Access!" );
			return;
		}
		
		// General parameters for all file types
		final String sha1           = request.getParameter( PARAM_SHA1           );
		final String fileName       = FileServletApi.trimFileName( request.getParameter( PARAM_FILE_NAME ) );
		final Date   lastModified   = getDateParam( request, PARAM_LAST_MODIFIED );
		final String fileTypeString = request.getParameter( PARAM_FILE_TYPE      );
		final String fileContent    = request.getParameter( PARAM_FILE_CONTENT   );
		
		LOGGER.fine( "Account: " + account.getUser().getEmail() + ", sha1: " + sha1 + ", est. size: " + ( fileContent == null ? 0 : fileContent.length() * 3 / 4 ) + ", file name: " + fileName );
		
		int inBandwidthIncrement = 0;
		int fileSize             = 0;
		
		try {
			// Increase used incoming bandwidth
			if ( fileContent != null )
				inBandwidthIncrement = fileContent.length();
			
			if ( sha1 == null || fileName == null || lastModified == null || fileTypeString == null || fileContent == null ) {
				LOGGER.warning( "Missing parameters!" );
				response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
				return;
			}
			
			final FileType fileType = FileType.fromString( fileTypeString );
			
			if ( fileType == null || fileType == FileType.ALL ) {
				LOGGER.warning( "Invalid file type: " + fileType );
				response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid file type!" );
				return;
			}
			
			final byte[] decodedFileContent = ServerUtils.decodeBase64String( fileContent );
			if ( decodedFileContent == null ) {
				LOGGER.warning( "Invalid base64 encoded file content!" );
				response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid base64 encoded file content!" );
				return;
			}
			final String calculatedSha1 = ServerUtils.calculateSha1( decodedFileContent );
			if ( !sha1.equals( calculatedSha1 ) ) {
				LOGGER.warning( "SHA-1 parameter does not match the SHA-1 digest of the file content: " + sha1 + " != " + calculatedSha1 );
				response.sendError( HttpServletResponse.SC_BAD_REQUEST, "SHA-1 parameter does not match the SHA-1 digest of the file content!" );
				return;
			}
			
			// Check if file is already stored
			final List< ? extends FileMetaData > fmdList = new JQBuilder<>( pm, FILE_TYPE_CLASS_MAP.get( fileType ) ).filter( "ownerk==p1 && sha1==p2", "KEY p1, String p2" ).get( account.getKey(), sha1 );
			
			final FileMetaData fileMetaData = !fmdList.isEmpty() ? fmdList.get( 0 )
				: fileType == FileType.SC2REPLAY   ? new Rep      ( account.getKey(), fileName, lastModified, sha1, decodedFileContent.length )
				: fileType == FileType.MOUSE_PRINT ? new Smpd     ( account.getKey(), fileName, lastModified, sha1, decodedFileContent.length )
				: fileType == FileType.OTHER       ? new OtherFile( account.getKey(), fileName, lastModified, sha1, decodedFileContent.length )
				: null;
			if ( fileMetaData == null )
				throw new RuntimeException( "Invalid file type, this case should have already been handled!" );
			
			if ( fmdList.isEmpty() ) {
				// It is a new file
				// Check quota
				final List< FileStat > fileStatList = new JQBuilder<>( pm, FileStat.class ).filter( "ownerKey==p1", "KEY p1" ).get( account.getKey() );
				// fileStatList might be empty if payment is not yet registered or not yet processed
				final long storageUsed = fileStatList.isEmpty() ? 0 : fileStatList.get( 0 ).getStorage();
				if ( ServerUtils.checkAccountQuota( account, storageUsed, decodedFileContent.length ) ) {
					// Quota exceeded, we deny storing the file
					LOGGER.warning( "Quota exceeded, storing denied!" );
					response.setContentType( "text/plain" );
					response.getWriter().println( false );
					return;
				}
				
				// Store content
				if ( decodedFileContent.length < DATASTORE_CONTENT_STORE_LIMIT ) {
					// Small file, more profitable to store it right here, right now (in the file meta data)
					fileMetaData.setContent( new Blob( decodedFileContent ) );
				}
				else {
    				// Store content in the Blobstore
    				try {
    					final FileService      fileService = FileServiceFactory.getFileService();
    					final AppEngineFile    appeFile    = fileService.createNewBlobFile( FILE_TYPE_MIME_TYPE_MAP.get( fileType ), sha1 );
    					final FileWriteChannel channel     = fileService.openWriteChannel( appeFile, true );
    					final ByteBuffer       bb          = ByteBuffer.wrap( decodedFileContent );
    					while ( bb.hasRemaining() )
    						channel.write( bb );
    					channel.closeFinally();
    					fileMetaData.setBlobKey( fileService.getBlobKey( appeFile ) );
    				} catch ( final Exception e ) {
    					// Blobstore is not available at the moment, we'll try to save content with (in) the file meta data
    					if ( decodedFileContent.length > 1000*1024 ) { // Leave a few KBs for other properties...
    						// Cannot store larger than 1 MB in the Datastore...
    						LOGGER.log( Level.SEVERE, "Blobstore is not available at the moment, and file content is too big (> 1 MB) to be saved with (in) the file meta data!", e );
    						response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Some error occured during storing the file!" );
    						return;
    					}
    					LOGGER.log( Level.WARNING, "Blobstore is not available at the moment, saving content with (in) the file meta data!", e );
    					fileMetaData.setContent( new Blob( decodedFileContent ) );
    					// process-file-with-content task will be registered after calling pm.makePersistent() because we need the key...
    				}
				}
			}
			
			// File type specific parameters
			switch ( fileType ) {
			case SC2REPLAY : {
				final Rep replay = (Rep) fileMetaData;
				
				// Update properties of the replay
				replay.setVer    ( request       .getParameter( PARAM_VERSION        ) );
				replay.setRepd   ( getDateParam      ( request, PARAM_REPLAY_DATE    ) );
				replay.setLength ( getIntParam       ( request, PARAM_GAME_LENGTH    ) );
				replay.setGw     ( request       .getParameter( PARAM_GATEWAY        ) );
				replay.setType   ( request       .getParameter( PARAM_GAME_TYPE      ) );
				replay.setMap    ( FileServletApi.trimMapName    ( request.getParameter( PARAM_MAP_NAME      ) ) );
				replay.setMapf   ( FileServletApi.trimMapFileName( request.getParameter( PARAM_MAP_FILE_NAME ) ) );
				replay.setFormat ( request       .getParameter( PARAM_FORMAT         ) );
				replay.setLeagues( request       .getParameter( PARAM_LEAGUE_MATCHUP ) );
				replay.setMatchup( request       .getParameter( PARAM_RACE_MATCHUP   ) );
				replay.setPlayers( getStringListParam( request, PARAM_PLAYERS        ) );
				replay.setTeams  ( getIntListParam   ( request, PARAM_PLAYER_TEAMS   ) );
				replay.setWinners( getStringListParam( request, PARAM_WINNERS        ) );
				
				// Old Sc2gears versions (prior to 8.3) don't send the format:
				if ( replay.getFormat() == null )
					replay.setFormat( ServerUtils.guessFormat( replay.getMatchup() ) );
				
				break;
			}
			case MOUSE_PRINT : {
				final Smpd mousePrint = (Smpd) fileMetaData;
				
				// Update properties of the mouse print
				mousePrint.setVer   ( getShortParam( request, PARAM_VERSION                ) );
				mousePrint.setStart ( getDateParam ( request, PARAM_RECORDING_START        ) );
				mousePrint.setEnd   ( getDateParam ( request, PARAM_RECORDING_END          ) );
				mousePrint.setWidth ( getIntParam  ( request, PARAM_SCREEN_WIDTH           ) );
				mousePrint.setHeight( getIntParam  ( request, PARAM_SCREEN_HEIGHT          ) );
				mousePrint.setRes   ( getIntParam  ( request, PARAM_SCREEN_RESOLUTION      ) );
				mousePrint.setTime  ( getIntParam  ( request, PARAM_SAMPLING_TIME          ) );
				mousePrint.setCount ( getIntParam  ( request, PARAM_SAMPLES_COUNT          ) );
				mousePrint.setUdsize( getIntParam  ( request, PARAM_UNCOMPRESSED_DATA_SIZE ) );
				mousePrint.setCompr ( getIntParam  ( request, PARAM_SAVED_WITH_COMPRESSION ) );
				
				break;
			}
			case OTHER : {
				final OtherFile otherFile = (OtherFile) fileMetaData;
				
				// Update properties of the Other file
				otherFile.setComment( FileServletApi.trimComment( request.getParameter( PARAM_COMMENT ) ) );
				final int dotIndex = otherFile.getFname().lastIndexOf( '.' );
				otherFile.setExt( dotIndex >= 0 && dotIndex < otherFile.getFname().length() - 1 ? otherFile.getFname().substring( dotIndex + 1 ) : null );
				
				break;
			}
			default :
				throw new RuntimeException( "Invalid file type, this case should have already been handled!" );
			}
			
			if ( fileMetaData.getKey() == null ) {
				pm.makePersistent( fileMetaData );
				fileSize = decodedFileContent.length;
				if ( fileMetaData.getBlobKey() == null && decodedFileContent.length >= DATASTORE_CONTENT_STORE_LIMIT ) 
					TaskServlet.register_procFileWithContentTask( fileMetaData.getKey(), fileType.longName );
			}
			
		}
		finally {
			// Increase used incoming bandwidth, update file counts and used storage statistics
			if ( inBandwidthIncrement > 0 || fileSize > 0 )
				TaskServlet.register_updateFileStoreStatsTask( account.getKey(), account.getPaidStorage(), account.getNotificationQuotaLevel(), inBandwidthIncrement, fileSize, fileTypeString );
		}
		
		// Everything went ok, send back success
		response.setContentType( "text/plain" );
		response.getWriter().println( true );
	}
	
	/**
	 * Serves a file specified by the request.
	 */
	private static void serveFile( final HttpServletRequest request, final HttpServletResponse response, final PersistenceManager pm, final Account account, final String sharedAccount, final User user ) throws IOException {
		// General parameters for all file types
		final String sha1           = request.getParameter( PARAM_SHA1      );
		final String fileTypeString = request.getParameter( PARAM_FILE_TYPE );
		
		final Key accountKey = account.getKey();
		LOGGER.fine( "Account key: " + accountKey + ", sha1: " + sha1 + ", file type: " + fileTypeString + ( sharedAccount == null ? "" : ", shared account: " + sharedAccount ) );
		
		if ( sha1 == null || fileTypeString == null ) {
			LOGGER.warning( "Missing parameters!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
			return;
		}
		
		final FileType fileType = FileType.fromString( fileTypeString );
		if ( fileType == null || fileType == FileType.ALL ) {
			LOGGER.warning( "Invalid file type!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid file type!" );
			return;
		}
		
		// AUTHORIZATION
		// Check if the specified file is owned by the user
		boolean hasPermission = true;
		switch ( fileType ) {
		case SC2REPLAY :
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.VIEW_REPLAYS ) )
				hasPermission = false;
			break;
		case MOUSE_PRINT :
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.VIEW_MOUSE_PRINTS ) )
				hasPermission = false;
			break;
		case OTHER :
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.VIEW_OTHER_FILES ) )
				hasPermission = false;
			break;
		default :
			LOGGER.warning( "Invalid file type: " + fileType );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid file type!" );
			return;
		}
		
		final List< ? extends FileMetaData > fileMetaDataList = hasPermission ? new JQBuilder<>( pm, FILE_TYPE_CLASS_MAP.get( fileType ) ).filter( "ownerk==p1 && sha1==p2", "KEY p1, String p2" ).get( accountKey, sha1 ) : new ArrayList< FileMetaData >( 0 );
		
		if ( fileMetaDataList.isEmpty() ) {
			LOGGER.warning( "Unauthorized access!" );
			response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized access!" );
			return;
		}
		// ...Authorization OK
		
		final FileMetaData fileMetaData = fileMetaDataList.get( 0 );
		
		byte[] content = null;
		try {
			content = ServerUtils.getFileContent( fileMetaData, null );
			if ( content == null ) {
				LOGGER.warning( "File not found!" );
				response.sendError( HttpServletResponse.SC_NOT_FOUND, "File not found!" );
				return;
			}
		} catch ( final IOException ie ) {
			LOGGER.log( Level.SEVERE, "Some error occured getting the file from the Blobstore!", ie );
			response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Some error occured during serving the file!" );
			return;
		}
		
		// Increase used outgoing bandwidth
		TaskServlet.register_updateFileDownlStatsTask( accountKey, content.length, fileTypeString );
		
		response.setContentType( "application/octet-stream" );
		// No standard way to provide UTF-8 file names, so I don't do so here (like "attachment;filename=somerep.SC2Replay").
		// Instead I append the file name to the end of the URL like "/file/somerep.SC2Replay" which is handled by all browsers well.
		response.setHeader( "Content-Disposition", "attachment" );
		// Set no-cache
		setNoCache( response );
		
		response.setContentLength( content.length );
		response.setHeader( HEADER_X_FILE_NAME, URLEncoder.encode( fileMetaData.getFname(), "UTF-8" ) );
		response.setHeader( HEADER_X_FILE_DATE, Long.toString( fileMetaData.getLastmod().getTime() ) );
		
		response.getOutputStream().write( content );
	}
	
	/**
	 * Serves a batch of files specified by the request.
	 */
	private static void serveFileBatch( final HttpServletRequest request, final HttpServletResponse response, final PersistenceManager pm, final Account account, final String sharedAccount, final User user ) throws IOException {
		// General parameters for all file types
		final String sha1List       = request.getParameter( PARAM_SHA1_LIST );
		final String fileTypeString = request.getParameter( PARAM_FILE_TYPE );
		
		final Key accountKey = account.getKey();
		LOGGER.fine( "Account key: " + accountKey + ", file type: " + fileTypeString + ( sharedAccount == null ? "" : ", shared account: " + sharedAccount ) );
		
		if ( sha1List == null || fileTypeString == null ) {
			LOGGER.warning( "Missing parameters!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
			return;
		}
		
		final FileType fileType = FileType.fromString( fileTypeString );
		if ( fileType == null || fileType == FileType.ALL ) {
			LOGGER.warning( "Invalid file type!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid file type!" );
			return;
		}
		
		final String[] sha1s = sha1List.split( "," );
		LOGGER.fine( "sha1 count: " + sha1s.length );
		
		// TODO sha1s.length should be compared to MAX_PAGE_SIZE just as in UserServiceImpl.java...
		
		// AUTHORIZATION
		// Check if the specified files are owned by the user
		boolean hasAccess = true;
		switch ( fileType ) {
		case SC2REPLAY :
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.VIEW_REPLAYS ) )
				hasAccess = false;
			break;
		case MOUSE_PRINT :
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.VIEW_MOUSE_PRINTS ) )
				hasAccess = false;
			break;
		case OTHER :
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.VIEW_OTHER_FILES ) )
				hasAccess = false;
			break;
		default :
			LOGGER.warning( "Invalid file type: " + fileType );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid file type!" );
			return;
		}
		
		if ( !hasAccess ) {
			LOGGER.warning( "Unauthorized access!" );
			response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized access!" );
			return;
		}
		
		final JQBuilder<? extends FileMetaData> q = new JQBuilder<>( pm, FILE_TYPE_CLASS_MAP.get( fileType ) ).filter( "ownerk==p1 && sha1==p2", "KEY p1, String p2" );
		
		final FileMetaData[] fileMetaDatas = new FileMetaData[ sha1s.length ];
		for ( int i = 0; i < sha1s.length; i++ ) {
			final List< ? extends FileMetaData > fileMetaDataList = q.get( accountKey, sha1s[ i ] );
			if ( fileMetaDataList.isEmpty() ) {
				LOGGER.warning( "Unauthorized access!" );
				response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized access!" );
				return;
			}
			
			fileMetaDatas[ i ] = fileMetaDataList.get( 0 );
		}
		
		// ...Authorization OK
		
		response.setContentType( "application/zip" );
		// No standard way to provide UTF-8 file names, so I don't do so here (like "attachment;filename=somerep.SC2Replay").
		// Instead I append the file name to the end of the URL like "/file/SC2Replay_pack.zip" which is handled by all browsers well.
		response.setHeader( "Content-Disposition", "attachment" );
		// Set no-cache
		setNoCache( response );
		
		long zipSize = 0;
		
		final ZipOutputStream zipOutputStream = new ZipOutputStream( response.getOutputStream() );
		// SC2Replays and SMPD files are BZip2 compressed... don't waste any time trying to compress them more with a weaker algorithm (Deflate):
		zipOutputStream.setLevel( 0 );
		
		// Each file/entry must have a unique name. We store "already used" names here:
		final Set< String > usedNameSet = new HashSet< String >( fileMetaDatas.length );
		
		final FileService fileService = FileServiceFactory.getFileService();
		
		for ( int i = 0; i < fileMetaDatas.length; i++ ) {
			final FileMetaData fileMetaData = fileMetaDatas[ i ];
			
			byte[] content = null;
			try {
				content = ServerUtils.getFileContent( fileMetaData, fileService );
			} catch ( IOException ie ) {
				LOGGER.log( Level.SEVERE, "Some error occured getting the file from the Blobstore!", ie );
			}
			if ( content == null )
				continue; // File not found
			
			final String entryName = ServerUtils.generateUniqueName( fileMetaData.getFname(), usedNameSet );
			usedNameSet.add( entryName );
			final ZipEntry zipEntry = new ZipEntry( entryName );
			zipEntry.setTime( fileMetaData.getLastmod().getTime() ); // TODO have to add client's time zone offset?
			zipOutputStream.putNextEntry( zipEntry );
			zipOutputStream.write( content );
			zipOutputStream.closeEntry();
			
			if ( zipEntry.getCompressedSize() > 0 )
				zipSize += zipEntry.getCompressedSize();  // This will give a good estimation, but will not include entry headers...
			else
				zipSize += content.length;                // Since we're not compressing, this will give the same...
		}
		
		// Increase used outgoing bandwidth
		TaskServlet.register_updateFileDownlStatsTask( accountKey, zipSize, fileTypeString );
		
		zipOutputStream.finish();
		zipOutputStream.flush();
	}
	
	/**
	 * Serves a file list specified by the request.
	 */
	private static void serveFileList( final HttpServletRequest request, final HttpServletResponse response, final PersistenceManager pm, final Account account, final String sharedAccount ) throws IOException {
		if ( sharedAccount != null ) {
			// This method is called from Sc2gears clients where no shared account can be specified
			// so if shared account is not null, it surely is a hack!
			LOGGER.warning( "Unauthorized Access! Shared account: " + sharedAccount );
			response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized Access!" );
			return;
		}
		
		final Date   afterDate      = getDateParam( request, PARAM_AFTER_DATE  );
		final Date   beforeDate     = getDateParam( request, PARAM_BEFORE_DATE ); // Only sent from version 8.9
		final String fileTypeString = request.getParameter( PARAM_FILE_TYPE    );
		
		final Key accountKey = account.getKey();
		LOGGER.fine( "Account key: " + accountKey + ", after date: " + afterDate + ", before date: " + beforeDate + ", file type: " + fileTypeString );
		
		if ( afterDate == null || fileTypeString == null ) {
			LOGGER.warning( "Missing parameters!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
			return;
		}
		
		final FileType fileType = FileType.fromString( fileTypeString );
		if ( fileType == null || fileType == FileType.ALL ) {
			LOGGER.warning( "Invalid file type!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid file type!" );
			return;
		}
		
		if ( beforeDate != null )
			beforeDate.setTime( beforeDate.getTime() + 24L*60*60*1000 ); // Include all files from the last day
		
		final JQBuilder< ? extends FileMetaData > q = new JQBuilder<>( pm, FILE_TYPE_CLASS_MAP.get( fileType ) );
		switch ( fileType ) {
		case SC2REPLAY :
			if ( beforeDate== null )
				q.filter( "ownerk==p1 && repd>=p2", "KEY p1, DATE p2" );
			else
				q.filter( "ownerk==p1 && repd>=p2 && repd<=p3", "KEY p1, DATE p2, DATE p3" );
			q.desc( "repd" );
			break;
		case MOUSE_PRINT :
			if ( beforeDate== null )
				q.filter( "ownerk==p1 && end>=p2", "KEY p1, DATE p2" );
			else
				q.filter( "ownerk==p1 && end>=p2 && end<=p3", "KEY p1, DATE p2, DATE p3" );
			q.desc( "end" );
			break;
		case OTHER :
			if ( beforeDate== null )
				q.filter( "ownerk==p1 && date>=p2", "KEY p1, DATE p2" );
			else
				q.filter( "ownerk==p1 && date>=p2 && date<=p3", "KEY p1, DATE p2, DATE p3" );
			q.desc( "date" );
			break;
		default :
			LOGGER.warning( "Invalid file type: " + fileType );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid file type!" );
			return;
		}
		q.range( 0, 1000 );
		
		response.setContentType( "text/plain;charset=UTF-8" );
		response.setCharacterEncoding( "UTF-8" );
		final PrintWriter writer = response.getWriter();
		
		while ( true ) {
			List< ? extends FileMetaData > fileMetaDataList = beforeDate == null ? q.get( accountKey, afterDate ) : q.get( accountKey, afterDate, beforeDate );
			for ( final FileMetaData fileMetaData : fileMetaDataList )
				writer.println( fileMetaData.getSha1() );
			
			if ( fileMetaDataList.size() < 1000 )
				break;
			
			q.cursor( fileMetaDataList );
		}
	}
	
	/**
	 * Serves dedicated data.
	 * TODO change back to private
	 */
	public static void serveDedicatedData( final HttpServletRequest request, final HttpServletResponse response, final PersistenceManager pm, final Account account, final String sharedAccount, final User user ) throws IOException {
		// TODO
		try {
			final Document document    = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			final Element  rootElement = document.createElement( "dedicated" );
			rootElement.setAttribute( "docVersion", "1.0" );
			
			final Element nameElement = document.createElement( "personName" );
			nameElement.setAttribute( "name", "Name" );
			rootElement.appendChild( nameElement );
			
			final Element googleAccountElement = document.createElement( "googleAccount" );
			googleAccountElement.setAttribute( "email", "googleaccount@something.com" );
			rootElement.appendChild( googleAccountElement );
			
			final Element quoteElement = document.createElement( "quote" );
			quoteElement.setTextContent( "Personal quote" );
			rootElement.appendChild( quoteElement );
			
			final Element timeElement = document.createElement( "dedicatedAt" );
			timeElement.setAttribute( "time", Long.toString( System.currentTimeMillis() ) );
			rootElement.appendChild( timeElement );
			
			// Encode using the privkey.rsa and send to the output
			
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
	}
	
}
