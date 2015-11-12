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

import static hu.belicza.andras.sc2gearsdb.FileServlet.FILE_TYPE_CLASS_MAP;
import static hu.belicza.andras.sc2gearsdb.FileServlet.FILE_TYPE_MIME_TYPE_MAP;
import hu.belicza.andras.mpq.MpqParser;
import hu.belicza.andras.sc2gearsdb.beans.DownloadStatInfo;
import hu.belicza.andras.sc2gearsdb.common.server.CommonUtils.DbPackage;
import hu.belicza.andras.sc2gearsdb.datastore.Account;
import hu.belicza.andras.sc2gearsdb.datastore.ApiAccount;
import hu.belicza.andras.sc2gearsdb.datastore.ApiCallStat;
import hu.belicza.andras.sc2gearsdb.datastore.DownloadStat;
import hu.belicza.andras.sc2gearsdb.datastore.Event.Type;
import hu.belicza.andras.sc2gearsdb.datastore.FileMetaData;
import hu.belicza.andras.sc2gearsdb.datastore.FileStat;
import hu.belicza.andras.sc2gearsdb.datastore.Map;
import hu.belicza.andras.sc2gearsdb.datastore.Payment;
import hu.belicza.andras.sc2gearsdb.datastore.RepProfile;
import hu.belicza.andras.sc2gearsdb.util.ByteArrayMpqDataInput;
import hu.belicza.andras.sc2gearsdb.util.CachingService;
import hu.belicza.andras.sc2gearsdb.util.JQBuilder;
import hu.belicza.andras.sc2gearsdb.util.PMF;
import hu.belicza.andras.sc2gearsdb.util.ServerUtils;
import hu.belicza.andras.sc2gearsdbapi.FileServletApi.FileType;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.AppEngineFile.FileSystem;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesService.OutputEncoding;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

/**
 * A general task servlet.<br>
 * <br>
 * Although the maximum rate for a queue is 100/s, it cannot be reached if the max concurrent task is 1.
 * In practice I experienced something like 200 tasks/min. To overcome this, I classify concurrent entity updates.<br>
 * <br>
 * Entities cannot affect each other, so only the tasks affecting the same entity have to be put in the same queue.
 * Obviously I cannot make a queue for each entity, I make a queue for all classes of the entities.<br>
 * <br>
 * The classification is simply:
 * <pre>class( Entity ) = Entity.Key.Id % (# of entity queues)</pre>
 * If the number of queues reserved for entities is a power of 2:
 * <pre>class( Entity ) = Entity.Key.Id & MASK</pre>
 * where <code>MASK</code> is the number of queues reserved for entities -1.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class TaskServlet extends HttpServlet {
	
	private static final Logger LOGGER = Logger.getLogger( TaskServlet.class.getName() );
	
	/** Day format used in {@link ApiCallStat}. */
	private static final DateFormat DAY_DATE_FORMAT = new SimpleDateFormat( ApiCallStat.DAY_PATTERN );
	
	/** URL of this task servlet. */
	private static final String TASK_SERVLET_URL = "/task";
	
	/** Name of the admin queue.                     */
	private static final String QUEUE_NAME_ADMIN                  = "admin";
	/** Name of the entity update queues.            */
	private static final String QUEUE_NAME_ENTITY_UPDATE          = "entityUpdate";
	/** Name of the process file with content queue. */
	private static final String QUEUE_NAME_PROC_FILE_WITH_CONTENT = "procFileWithContent";
	/** Name of the map processor queue.             */
	private static final String QUEUE_NAME_MAP_PROCESSOR          = "mapProcessor";
	/** Name of the custom queue.                    */
	private static final String QUEUE_NAME_CUSTOM                 = "custom";
	
	/** Update download stat operation.                   */
	private static final String OPERATION_UPDATE_DOWNLOAD_STATS   = "updateDwnlStat";
	/** Update API call stat operation.                   */
	private static final String OPERATION_UPDATE_API_CALL_STATS   = "updateApiCallStat";
	/** Update package operation.                         */
	private static final String OPERATION_UPDATE_PACKAGE          = "updatePackage";
	/** Update statistics for file storing operation.     */
	private static final String OPERATION_UPDATE_FILE_STORE_STATS = "fileStoreSat";
	/** Update statistics for file downloading operation. */
	private static final String OPERATION_UPDATE_FILE_DOWNL_STATS = "fileDownlStat";
	/** Update statistics for file deletion operation.    */
	private static final String OPERATION_UPDATE_FILE_DEL_STATS   = "fileDelStat";
	/** Update replay profile operation.                  */
	private static final String OPERATION_UPDATE_REP_PROFILE      = "updateRepProfile";
	/** Recalculate file stats operation.                 */
	private static final String OPERATION_RECALC_FILE_STATS       = "recalcFileStats";
	/** Process file with content operation.              */
	private static final String OPERATION_PROC_FILE_WITH_CONTENT  = "procFileWithContent";
	/** Process map operation.                            */
	private static final String OPERATION_PROCESS_MAP             = "processMap";
	/** Custom operation.                                 */
	private static final String OPERATION_CUSTOM                  = "custom";
	
	/** Operation parameter name.                    */
	private static final String PARAM_OPERATION                = "op";
	/** API account key parameter name.              */
	private static final String PARAM_API_ACCOUNT_KEY          = "apiAccKey";
	/** Paid ops parameter name.                     */
	private static final String PARAM_PAID_OPS                 = "paidOps";
	/** Notification available Ops parameter name.   */
	private static final String PARAM_NOTIFICATION_AVAIL_OPS   = "notificAvailOps";
	/** Used ops parameter name.                     */
	private static final String PARAM_USED_OPS                 = "usedOps";
	/** Exec time parameter name.                    */
	private static final String PARAM_EXEC_TIME                = "execTime";
	/** Denied parameter name.                       */
	private static final String PARAM_DENIED                   = "denied";
	/** Error parameter name.                        */
	private static final String PARAM_ERROR                    = "error";
	/** Call operation parameter name.               */
	private static final String PARAM_CALL_OPERATION           = "callOp";
	/** File name parameter name.                    */
	private static final String PARAM_FILE_NAME                = "fileName";
	/** User agent parameter name.                   */
	private static final String PARAM_USER_AGENT               = "userAgent";
	/** Unique parameter name.                       */
	private static final String PARAM_UNIQUE                   = "unique";
	/** Counter persisting cycle parameter name.     */
	private static final String PARAM_COUNTER_PERSISTING_CYCLE = "countPersCycle";
	/** Account key parameter name.                  */
	private static final String PARAM_ACCOUNT_KEY              = "accountKey";
	/** Paid storage parameter name.                 */
	private static final String PARAM_PAID_STORAGE             = "paidStorage";
	/** Notification quota level parameter name.     */
	private static final String PARAM_NOTIFICATION_QUOTA_LEVEL = "notificQuotaLev";
	/** Incoming bandwidth increment parameter name. */
	private static final String PARAM_IN_BANDWIDTH_INCREMENT   = "inBandInc";
	/** File size parameter name.                    */
	private static final String PARAM_FILE_SIZE                = "fileSize";
	/** File type parameter name.                    */
	private static final String PARAM_FILE_TYPE                = "fileType";
	/** SHA-1 parameter name.                        */
	private static final String PARAM_SHA1                     = "sha1";
	/** Increment comments count parameter name.     */
	private static final String PARAM_INC_COMMENTS_COUNT       = "incComCount";
	/** Increment GG's count parameter name.         */
	private static final String PARAM_INC_GGS_COUNT            = "incGgsCount";
	/** Increment BG's count parameter name.         */
	private static final String PARAM_INC_BGS_COUNT            = "incBgsCount";
	/** Map file name parameter name.                */
	private static final String PARAM_MAP_FILE_NAME            = "mapFname";
	/** File key parameter name.                     */
	private static final String PARAM_FILE_KEY                 = "fileKey";
	
	/** Number of queues reserved for entity updates.
	 * It has to be a power of 2! */
	private static final int    ENTITY_UPDATE_QUEUES_COUNT = 32;
	
	/** Entity queue mask, determined by the number of queue masks.
	 * If the number of queues reserved for entities is a power of 2,
	 * then the mask is the number of queues - 1. */
	private static final int    QUEUE_ENTITY_MASK          = ENTITY_UPDATE_QUEUES_COUNT - 1;
	
	/**
	 * Registers an update-download-stat task.
	 * @param fileName               downloaded file name
	 * @param userAgent              user agent header field sent in the download request
	 * @param unique                 tells if the download is unique (based on IP and download history)
	 * @param counterPersistingCycle tells how often the download counter has to be written to the datastore
	 */
	public static void register_updateDownloadStat( final String fileName, final String userAgent, final boolean unique, final int counterPersistingCycle ) {
		QueueFactory.getQueue( getEntityUpdateQueueName( fileName.hashCode() ) ).add(
			buildTask( OPERATION_UPDATE_DOWNLOAD_STATS )
				.param( PARAM_FILE_NAME               , fileName                                   )
				.param( PARAM_USER_AGENT              , userAgent == null ? "" : userAgent         )
				.param( PARAM_UNIQUE                  , Boolean.toString( unique                 ) )
				.param( PARAM_COUNTER_PERSISTING_CYCLE, Integer.toString( counterPersistingCycle ) )
		);
	}
	
	/**
	 * Registers an update-API-call-stat task.
	 * @param apiAccountKey        API account key
	 * @param paidOps              number of paid ops of the API account
	 * @param notificationAvailOps notification available Ops of the API account
	 * @param usedOps              number of used ops
	 * @param execTime             API call execution time in milliseconds
	 * @param denied               tells if the API call was denied due to no available Ops
	 * @param error                tells if the API call returned error
	 * @param callOperation        call operation
	 */
	public static void register_updateApiCallStat( final Key apiAccountKey, final long paidOps, final long notificationAvailOps, final long usedOps, final long execTime, final boolean denied, final boolean error, final String callOperation ) {
		QueueFactory.getQueue( getEntityUpdateQueueName( apiAccountKey ) ).add(
			buildTask( OPERATION_UPDATE_API_CALL_STATS )
				.param( PARAM_API_ACCOUNT_KEY       , KeyFactory.keyToString( apiAccountKey  ) )
				.param( PARAM_PAID_OPS              , Long   .toString( paidOps              ) )
				.param( PARAM_NOTIFICATION_AVAIL_OPS, Long   .toString( notificationAvailOps ) )
				.param( PARAM_USED_OPS              , Long   .toString( usedOps              ) )
				.param( PARAM_EXEC_TIME             , Long   .toString( execTime             ) )
				.param( PARAM_DENIED                , Boolean.toString( denied               ) )
				.param( PARAM_ERROR                 , Boolean.toString( error                ) )
				.param( PARAM_CALL_OPERATION        , callOperation                            )
		);
	}
	
	/**
	 * Registers an update-package task.
	 * @param accountKey account key
	 */
	public static void register_updatePackageTask( final Key accountKey ) {
		QueueFactory.getQueue( QUEUE_NAME_ADMIN ).add(
			buildTask( OPERATION_UPDATE_PACKAGE )
				.param( PARAM_ACCOUNT_KEY, KeyFactory.keyToString( accountKey ) )
		);
	}
	
	/**
	 * Registers an update-file-store-stats task.
	 * @param accountKey             account key
	 * @param paidStorage            paid storage of the account
	 * @param notificationQuotaLevel notification quota level of the account
	 * @param inBandwidthIncrement   incoming bandwidth increment
	 * @param fileSize               file size
	 * @param fileType               file type
	 */
	public static void register_updateFileStoreStatsTask( final Key accountKey, final long paidStorage, final int notificationQuotaLevel, final int inBandwidthIncrement, final int fileSize, final String fileType ) {
		QueueFactory.getQueue( getEntityUpdateQueueName( accountKey ) ).add(
			buildTask( OPERATION_UPDATE_FILE_STORE_STATS )
				.param( PARAM_ACCOUNT_KEY             , KeyFactory.keyToString( accountKey       ) )
				.param( PARAM_PAID_STORAGE            , Long   .toString( paidStorage            ) )
				.param( PARAM_NOTIFICATION_QUOTA_LEVEL, Integer.toString( notificationQuotaLevel ) )
				.param( PARAM_IN_BANDWIDTH_INCREMENT  , Integer.toString( inBandwidthIncrement   ) )
				.param( PARAM_FILE_SIZE               , Integer.toString( fileSize               ) )
				.param( PARAM_FILE_TYPE               , fileType                                   )
		);
	}
	
	/**
	 * Registers an update-file-dl-stats task options.
	 * @param accountKey account key
	 * @param fileSize   file size
	 * @return an update-file-dl-stats task options
	 */
	public static void register_updateFileDownlStatsTask( final Key accountKey, final long fileSize, final String fileType ) {
		QueueFactory.getQueue( getEntityUpdateQueueName( accountKey ) ).add(
			buildTask( OPERATION_UPDATE_FILE_DOWNL_STATS )
				.param( PARAM_ACCOUNT_KEY, KeyFactory.keyToString( accountKey ) )
				.param( PARAM_FILE_SIZE  , Long.toString( fileSize            ) )
				.param( PARAM_FILE_TYPE  , fileType                             )
		);
	}
	
	/**
	 * Registers an update-file-delete-stats task.
	 * @param accountKey account key
	 * @param fileSize   file size
	 * @param fileType   file type
	 * @param sha1       SHA-1 of the file to be deleted after
	 */
	public static void register_updateFileDelStatsTask( final Key accountKey, final long fileSize, final String fileType ) {
		QueueFactory.getQueue( getEntityUpdateQueueName( accountKey ) ).add(
			buildTask( OPERATION_UPDATE_FILE_DEL_STATS )
				.param( PARAM_ACCOUNT_KEY, KeyFactory.keyToString( accountKey ) )
				.param( PARAM_FILE_SIZE  , Long.toString( fileSize            ) )
				.param( PARAM_FILE_TYPE  , fileType                             )
		);
	}
	
	/**
	 * Registers an update-replay-profile task.
	 * @param sha1 SHA-1 of the file
	 * @param incCommentsCount tells if comments count has to be incremented
	 * @param incGgsCount      tells if GG count has to be incremented
	 * @param incBgsCount      tells if BG count has to be incremented
	 */
	public static void register_updateRepProfileTask( final String sha1, final boolean incCommentsCount, final boolean incGgsCount, final boolean incBgsCount ) {
		int entitySelector;
		try {
			// SHA-1 algorithm is designed so every bit of SHA-1 depends on the input,
			// so the first byte depends on the input (and changes) just like all of it
			// It's enough to decide/assign the queue based on the first byte
			entitySelector = Integer.parseInt( sha1.substring( 0, 2 ), 16 );
		} catch ( final Exception e ) { // NumberFormatException is not enough, IndexOutOfBoundsException is raised if sha1 is shorter than 2 chars
			// This should never happen, but since sha1 is not checked syntactically,
			// it might happen someone sends an invalid sha1
			entitySelector = sha1.hashCode();
		}
		
		QueueFactory.getQueue( getEntityUpdateQueueName( entitySelector ) ).add(
			buildTask( OPERATION_UPDATE_REP_PROFILE )
				.param( PARAM_SHA1              , sha1 )
				.param( PARAM_INC_COMMENTS_COUNT, Boolean.toString( incCommentsCount ) )
				.param( PARAM_INC_GGS_COUNT     , Boolean.toString( incGgsCount      ) )
				.param( PARAM_INC_BGS_COUNT     , Boolean.toString( incBgsCount      ) )
		);
	}
	
	/**
	 * Registers a recalc-file-stats task.
	 * @param accountKey account key
	 */
	public static void register_recalcFileStatsTask( final Key accountKey ) {
		QueueFactory.getQueue( getEntityUpdateQueueName( accountKey ) ).add(
			buildTask( OPERATION_RECALC_FILE_STATS )
				.param( PARAM_ACCOUNT_KEY, KeyFactory.keyToString( accountKey ) )
		);
	}
	
	/**
	 * Registers a process-file-with-content task.
	 * @param fileKey  key of file to process
	 * @param fileType file type
	 */
	public static void register_procFileWithContentTask( final Key fileKey, final String fileType ) {
		QueueFactory.getQueue( QUEUE_NAME_PROC_FILE_WITH_CONTENT ).add(
			buildTask( OPERATION_PROC_FILE_WITH_CONTENT )
				.param( PARAM_FILE_KEY , KeyFactory.keyToString( fileKey ) )
				.param( PARAM_FILE_TYPE, fileType )
		);
	}
	
	/**
	 * Registers a process-map task.
	 * @param mapFileName name of the map file to process (it's the SHA-256 of the content of the map file plus ".s2ma")
	 */
	public static void register_processMapTask( final String mapFileName ) {
		QueueFactory.getQueue( QUEUE_NAME_MAP_PROCESSOR ).add(
			buildTask( OPERATION_PROCESS_MAP )
				.param( PARAM_MAP_FILE_NAME, mapFileName )
		);
	}
	
	/**
	 * Registers a custom task in the custom queue.
	 * @param paramMap map of parameters to store
	 */
	public static void register_customTask( final java.util.Map< String, String > paramMap ) {
		final TaskOptions taskOptions = buildTask( OPERATION_CUSTOM );
		
		for ( final Entry< String, String > paramEntry : paramMap.entrySet() )
			taskOptions.param( paramEntry.getKey(), paramEntry.getValue() );
		
		QueueFactory.getQueue( QUEUE_NAME_CUSTOM ).add( taskOptions );
	}
	
	/**
	 * Returns the name of the queue associated for the specified entity key.
	 * @param entityKey entity key to return the queue name for
	 * @return the name of the queue associated for the specified entity key
	 */
	private static String getEntityUpdateQueueName( final Key entityKey ) {
		return QUEUE_NAME_ENTITY_UPDATE + ( entityKey.getId() & QUEUE_ENTITY_MASK );
	}
	
	/**
	 * Returns the name of the queue associated with the specified entity selector.
	 * @param entitySelector a numerical value derived from a property which selects/identifies the entity
	 * @return the name of the queue associated for the specified entity selector
	 */
	private static String getEntityUpdateQueueName( final int entitySelector ) {
		return QUEUE_NAME_ENTITY_UPDATE + ( entitySelector & QUEUE_ENTITY_MASK );
	}
	
	
	/**
	 * Builds the basic task options suitable for extension for specific tasks.
	 * @param operation operation, task to be performed
	 * @return the basic task options suitable for extension for specific tasks
	 */
	private static TaskOptions buildTask( final String operation ) {
		return TaskOptions.Builder.withUrl( TASK_SERVLET_URL ).param( PARAM_OPERATION, operation );
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
		// The task servlet is restricted to admin security role. No custom authentication or authorization required
		// (and there isn't any info like authorization key or Google account provided)
		final String operation = request.getParameter( PARAM_OPERATION );
		if ( operation == null ) {
			LOGGER.warning( "Missing Operation!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing Operation!" );
			return;
		}
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			switch ( operation ) {
			case OPERATION_UPDATE_DOWNLOAD_STATS :
				updateDownloadStat( pm, request.getParameter( PARAM_FILE_NAME ), request.getParameter( PARAM_USER_AGENT ), Boolean.parseBoolean( request.getParameter( PARAM_UNIQUE ) ), Integer.parseInt( request.getParameter( PARAM_COUNTER_PERSISTING_CYCLE ) ) );
				break;
			case OPERATION_UPDATE_API_CALL_STATS :
				updateApiCallStat( pm, KeyFactory.stringToKey( request.getParameter( PARAM_API_ACCOUNT_KEY ) ), Long.parseLong( request.getParameter( PARAM_PAID_OPS ) ), Long.parseLong( request.getParameter( PARAM_NOTIFICATION_AVAIL_OPS ) ), Long.parseLong( request.getParameter( PARAM_USED_OPS ) ), Long.parseLong( request.getParameter( PARAM_EXEC_TIME ) ), Boolean.parseBoolean( request.getParameter( PARAM_DENIED ) ), Boolean.parseBoolean( request.getParameter( PARAM_ERROR ) ), request.getParameter( PARAM_CALL_OPERATION ) );
				break;
			case OPERATION_UPDATE_FILE_STORE_STATS : 
				updateFileStoreStats( pm, KeyFactory.stringToKey( request.getParameter( PARAM_ACCOUNT_KEY ) ), Long.parseLong( request.getParameter( PARAM_PAID_STORAGE ) ), Integer.parseInt( request.getParameter( PARAM_NOTIFICATION_QUOTA_LEVEL ) ), Integer.parseInt( request.getParameter( PARAM_IN_BANDWIDTH_INCREMENT ) ), Integer.parseInt( request.getParameter( PARAM_FILE_SIZE ) ), request.getParameter( PARAM_FILE_TYPE ) );
				break;
			case OPERATION_CUSTOM : 
				customTask( request, pm );
				break;
			case OPERATION_UPDATE_FILE_DOWNL_STATS : 
				updateFileDownlStats( pm, KeyFactory.stringToKey( request.getParameter( PARAM_ACCOUNT_KEY ) ), Long.parseLong( request.getParameter( PARAM_FILE_SIZE ) ), request.getParameter( PARAM_FILE_TYPE ) );
				break;
			case OPERATION_UPDATE_FILE_DEL_STATS : 
				updateFileDelStats( pm, KeyFactory.stringToKey( request.getParameter( PARAM_ACCOUNT_KEY ) ), Long.parseLong( request.getParameter( PARAM_FILE_SIZE ) ), request.getParameter( PARAM_FILE_TYPE ) );
				break;
			case OPERATION_UPDATE_REP_PROFILE : 
				updateRepProfile( pm, request.getParameter( PARAM_SHA1 ), Boolean.parseBoolean( request.getParameter( PARAM_INC_COMMENTS_COUNT ) ), Boolean.parseBoolean( request.getParameter( PARAM_INC_GGS_COUNT ) ), Boolean.parseBoolean( request.getParameter( PARAM_INC_BGS_COUNT ) ) );
				break;
			case OPERATION_UPDATE_PACKAGE : 
				updatePackage( pm, KeyFactory.stringToKey( request.getParameter( PARAM_ACCOUNT_KEY ) ) );
				break;
			case OPERATION_RECALC_FILE_STATS : 
				recalcFileStats( pm, KeyFactory.stringToKey( request.getParameter( PARAM_ACCOUNT_KEY ) ) );
				break;
			case OPERATION_PROC_FILE_WITH_CONTENT : 
				processFileWithContent( pm, KeyFactory.stringToKey( request.getParameter( PARAM_FILE_KEY ) ), request.getParameter( PARAM_FILE_TYPE ) );
				break;
			case OPERATION_PROCESS_MAP : 
				processMap( pm, request.getParameter( PARAM_MAP_FILE_NAME ) );
				break;
			default: 
				LOGGER.warning( "Invalid Operation: " + operation );
				response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid Operation!" );
				return;
			}
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	private void updateDownloadStat( final PersistenceManager pm, String origFileName, final String userAgent, final boolean unique, final int counterPersistingCycle ) {
		final String fileName = DownloadServlet.DAILY_STATS_FILE_NAME_SET.contains( origFileName )
				? ServerUtils.appendDayToFileName( origFileName ) : origFileName; 
		
		LOGGER.fine( "File name: " + fileName );
		
		if ( counterPersistingCycle <= 1 ) {
			// No caching, persist every stat change immediately
			final Key          dsKey        = ServerUtils.getSingleKeyQueryResult( DownloadStat.class, "file", fileName );
			final DownloadStat downloadStat = dsKey == null ? new DownloadStat( fileName ) : pm.getObjectById( DownloadStat.class, dsKey );
			
			// Update stat fields
			downloadStat.incrementCount();
			if ( userAgent.contains( "Java" ) )
				downloadStat.incrementJavaClient();
			if ( unique )
				downloadStat.incrementUnique();
			
			if ( downloadStat.getKey() == null )
				pm.makePersistent( downloadStat );
		}
		else {
			// Cache changes, persist the results of multiple changes in one step
			DownloadStatInfo downloadStatInfo = CachingService.getDownloadStatByFile( fileName );
			if ( downloadStatInfo == null ) {
				downloadStatInfo = new DownloadStatInfo();
				final Key          dsKey        = ServerUtils.getSingleKeyQueryResult( DownloadStat.class, "file", fileName );
				final DownloadStat downloadStat = dsKey == null ? new DownloadStat( fileName ) : pm.getObjectById( DownloadStat.class, dsKey );
				
				if ( downloadStat.getKey() == null ) {
					pm.makePersistent( downloadStat );
					if ( DownloadServlet.DAILY_STATS_FILE_NAME_SET.contains( origFileName ) ) {
						// We save daily stats, and the current dl stats is new, this means it's the first for today,
						// Yesterday's stats have just been abandoned. Persist memcached data for yesterday!
						final Calendar cal = Calendar.getInstance();
						cal.add( Calendar.DATE, -1 );
						final String yesterdayFileName = ServerUtils.appendDayToFileName( origFileName, cal.getTime() );
						final DownloadStatInfo yesterdayDlStatInfo = CachingService.getDownloadStatByFile( yesterdayFileName );
						if ( yesterdayDlStatInfo != null ) {
							final Key ydsKey = ServerUtils.getSingleKeyQueryResult( DownloadStat.class, "file", yesterdayFileName );
							if ( ydsKey != null ) {
								final DownloadStat yesterdayDlStat = pm.getObjectById( DownloadStat.class, ydsKey );
								// Copy stat fields to stat info fields
								yesterdayDlStat.setCount     ( yesterdayDlStatInfo.getCount     () );
								yesterdayDlStat.setJavaClient( yesterdayDlStatInfo.getJavaClient() );
								yesterdayDlStat.setUnique    ( yesterdayDlStatInfo.getUnique    () );
							}
						}
					}
				}
				
				// Copy stat fields to stat info fields
				// TODO can ServerUtils.convertDownloadStatToDlInfo() be used here?
				downloadStatInfo.setCount     ( downloadStat.getCount     () );
				downloadStatInfo.setJavaClient( downloadStat.getJavaClient() );
				downloadStatInfo.setUnique    ( downloadStat.getUnique    () );
			}
			
			// Update stat info fields
			downloadStatInfo.incrementPersistingCycleCounter();
			downloadStatInfo.incrementCount();
			if ( userAgent.contains( "Java" ) )
				downloadStatInfo.incrementJavaClient();
			if ( unique )
				downloadStatInfo.incrementUnique();
			
			// Need persisting now?
			if ( downloadStatInfo.getPersistingCycleCounter() >= counterPersistingCycle ) {
				// We have to persist now 
				final Key          dsKey        = ServerUtils.getSingleKeyQueryResult( DownloadStat.class, "file", fileName );
				// DownloadStat should always exist, but just to be sure...
				final DownloadStat downloadStat = dsKey == null ? new DownloadStat( fileName ) : pm.getObjectById( DownloadStat.class, dsKey );
				
				// Copy stat info fields to stat fields
				downloadStat.setCount     ( downloadStatInfo.getCount     () );
				downloadStat.setJavaClient( downloadStatInfo.getJavaClient() );
				downloadStat.setUnique    ( downloadStatInfo.getUnique    () );
				
				if ( downloadStat.getKey() == null ) // DownloadStat should always exist, but just to be sure...
					pm.makePersistent( downloadStat );
				
				// Reset persisting cycle counter
				downloadStatInfo.setPersistingCycleCounter( 0 );
			}
			
			// Cache the updated stat info
			CachingService.putFileDownloadStat( fileName, downloadStatInfo );
		}
	}
	
	private void updateApiCallStat( final PersistenceManager pm, final Key apiAccountKey, final long paidOps, final long notificationAvailOps, final long usedOps, final long execTime, final boolean denied, final boolean error, final String callOperation ) {
		LOGGER.fine( "API account key: " + apiAccountKey + ", used Ops: " + usedOps + ", call operation: " + callOperation );
		
		// Update in 2 cycles: first TOTAL, second today's
		for ( int i = 0; i < 2; i++ ) {
			final String dayToUpdate = i == 0 ? ApiCallStat.DAY_TOTAL : DAY_DATE_FORMAT.format( new Date() );
			final Key         acsKey      = ServerUtils.getSingleKeyQueryResult( ApiCallStat.class, "ownerKey", apiAccountKey, "day", dayToUpdate );
			final ApiCallStat apiCallStat = acsKey == null ? new ApiCallStat( apiAccountKey, dayToUpdate ) : pm.getObjectById( ApiCallStat.class, acsKey );
			
			if ( i == 0 && usedOps > 0 ) {
				// Check notification available Ops level
				final long availableOpsBefore = paidOps - apiCallStat.getUsedOps();
				final long availableOpsAfter  = availableOpsBefore - usedOps;
				if ( availableOpsBefore > notificationAvailOps && availableOpsAfter <= notificationAvailOps ) {
					ServerUtils.logEvent( apiAccountKey, Type.NOTIFICATION_AVAIL_OPS_EXCEEDED );
					
					final ApiAccount apiAccount = pm.getObjectById( ApiAccount.class, apiAccountKey );
					final String body = ServerUtils.concatenateLines(
							"Hi " + apiAccount.getAddressedBy() + ",", null,
							"Your Sc2gears Database API account exceeded the notification available Ops level!", null,
							"Regards,",
							"   Andras Belicza" );
					ServerUtils.sendEmail( apiAccount, "API Account exceeded notification available Ops level", body );
				}
			}
			
    		// Update stat fields
    		apiCallStat.registerApiCall( usedOps, execTime, denied, error, callOperation );
    		
    		if ( apiCallStat.getKey() == null )
    			pm.makePersistent( apiCallStat );
		}
	}
	
	private void updateRepProfile( final PersistenceManager pm, final String sha1, final boolean incCommentsCount, final boolean incGgsCount, final boolean incBgsCount ) {
		LOGGER.fine( "sha1: " + sha1 );
		
		final Key         rpKey      = ServerUtils.getSingleKeyQueryResult( RepProfile.class, "sha1", sha1 );
		final RepProfile  repProfile = rpKey == null ? new RepProfile( sha1 ) : pm.getObjectById( RepProfile.class, rpKey );
		
		if ( incCommentsCount )
			repProfile.incrementCommentsCount();
		if ( incGgsCount )
			repProfile.incrementGgsCount();
		if ( incBgsCount )
			repProfile.incrementBgsCount();
		
		if ( repProfile.getKey() == null )
			pm.makePersistent( repProfile );
	}
	
	private void updateFileStoreStats( final PersistenceManager pm, final Key accountKey, final long paidStorage, final int notificationQuotaLevel, final int inBandwidthIncrement, final int fileSize, final String fileTypeString ) {
		LOGGER.fine( "Account key: " + accountKey );
		
		final FileType fileType = FileType.fromString( fileTypeString );
		final Key      fsKey    = ServerUtils.getSingleKeyQueryResult( FileStat.class, "ownerKey", accountKey );
		final FileStat fileStat = fsKey == null ? new FileStat( accountKey ) : pm.getObjectById( FileStat.class, fsKey );
		
		if ( fileSize > 0 ) {
			// Check notification storage quota level
			final long quotaLevel = paidStorage * notificationQuotaLevel / 100;
			if ( fileStat.getStorage() < quotaLevel && fileStat.getStorage() + fileSize >= quotaLevel ) {
				ServerUtils.logEvent( accountKey, Type.NOTIFICATION_STORAGE_QUOTA_EXCEEDED );
				
				final Account account = pm.getObjectById( Account.class, accountKey );
				final String body = ServerUtils.concatenateLines(
						"Hi " + account.getAddressedBy() + ",", null,
						"Your Sc2gears Database account exceeded the notification storage quota level!", null,
						"You can check your storage limit here:",
						"https://sciigears.appspot.com/User.html", null,
						"You can purchase additional storage. For price details visit your User page (above) or see:",
						"https://sites.google.com/site/sc2gears/sc2gears-database", null,
						"Regards,",
						"   Andras Belicza" );
				ServerUtils.sendEmail( account, "Account exceeded notification storage quota level", body );
			}
		}
		
		if ( fileSize > 0 )
			fileStat.registerFile( fileType, fileSize );
		fileStat.increaseInbw( fileType, inBandwidthIncrement );
		
		if ( fileStat.getKey() == null )
			pm.makePersistent( fileStat );
	}
	
	private void updateFileDownlStats( final PersistenceManager pm, final Key accountKey, final long fileSize, final String fileTypeString ) {
		LOGGER.fine( "Account key: " + accountKey );
		
		final FileType fileType = FileType.fromString( fileTypeString );
		final Key      fsKey    = ServerUtils.getSingleKeyQueryResult( FileStat.class, "ownerKey", accountKey );
		final FileStat fileStat = fsKey == null ? new FileStat( accountKey ) : pm.getObjectById( FileStat.class, fsKey );
		
		fileStat.increaseOutbw( fileType, fileSize );
		
		if ( fileStat.getKey() == null )
			pm.makePersistent( fileStat );
	}
	
	private void updateFileDelStats( final PersistenceManager pm, final Key accountKey, final long fileSize, final String fileTypeString ) {
		LOGGER.fine( "Account key: " + accountKey );
		
		final FileType fileType = FileType.fromString( fileTypeString );
		final Key fsKey = ServerUtils.getSingleKeyQueryResult( FileStat.class, "ownerKey", accountKey );
		if ( fsKey != null ) {
			final FileStat fileStat = pm.getObjectById( FileStat.class, fsKey );
			fileStat.deregisterFile( fileType, fileSize );
			// fileStat must not be deleted: it stores the bandwidth info and the last updated info
		}
	}
	
	private void updatePackage( final PersistenceManager pm, final Key accountKey ) {
		LOGGER.fine( "Account key: " + accountKey );
		
		final Account account = pm.getObjectById( Account.class, accountKey );
		
		final List< Payment > paymentList = new JQBuilder<>( pm, Payment.class ).filter( "accountKey==p1", "KEY p1" ).get( accountKey );
		float totalPayment = 0.1f; // To get rid of rounding problems
		for ( final Payment payment : paymentList )
			totalPayment += payment.getVirtualPayment();
		
		final DbPackage dbPackage = DbPackage.getFromPayment( totalPayment );
		if ( dbPackage != null && dbPackage.buyable )
			if ( account.getPaidStorage() != dbPackage.storage ) {
				if ( account.getPaidStorage() == 0l ) {
					// Account registration, first payment
					final String body = ServerUtils.concatenateLines(
							"Hi " + account.getAddressedBy() + ",", null,
							"Your Sc2gears Database account is ready.", null,
							"This is your Sc2gears Database Authorization key:", null,
							account.getAuthorizationKey(), null,
							"You can view, change your account and view your uploaded content here:",
							"https://sciigears.appspot.com/User.html", null,
							"Should you have any questions, hit \"Reply\" to this email.", null,
							"Regards,",
							"   Andras Belicza" );
					if ( !ServerUtils.sendEmail( account, "Account info", body ) )
						ServerUtils.sendEmailToAdmin( "Failed to notify client",
								ServerUtils.concatenateLines( "Payment registered successfully, but failed to send email notification about account creation!", "Google account: " + account.getUser().getEmail() ) );
				}
				else {
					// A package upgrade
					final String body = ServerUtils.concatenateLines(
							"Hi " + account.getAddressedBy() + ",", null,
							"This email is sent to notify you that your Sc2gears Database package has been upgraded.", null,
							"You can view, change your account here:",
							"https://sciigears.appspot.com/User.html", null,
							"Should you have any questions, hit \"Reply\" to this email.", null,
							"Regards,",
							"   Andras Belicza" );
					if ( !ServerUtils.sendEmail( account, "Successful package upgrade", body ) )
						ServerUtils.sendEmailToAdmin( "Failed to notify client",
								ServerUtils.concatenateLines( "Payment registered successfully, but failed to send email notification about package upgrade!", "Google account: " + account.getUser().getEmail() ) );
				}
				account.setPaidStorage( dbPackage.storage );
			}
	}
	
	private void recalcFileStats( final PersistenceManager pm, final Key accountKey ) {
		LOGGER.fine( "Account key: " + accountKey );
		
		final Key fsKey = ServerUtils.getSingleKeyQueryResult( FileStat.class, "ownerKey", accountKey );
		if ( fsKey == null )
			return;
		
		final FileStat fileStat = pm.getObjectById( FileStat.class, fsKey );
		
		// First perform a fast check: count the different file types (keys only). If this differs, then perform a recalculation.
		final DatastoreService ds     = DatastoreServiceFactory.getDatastoreService();
		final Filter           filter = new FilterPredicate( "ownerk", FilterOperator.EQUAL, accountKey );
		
		for ( final FileType fileType : new FileType[] { FileType.SC2REPLAY, FileType.MOUSE_PRINT, FileType.OTHER } ) {
			final Query q = new Query( FILE_TYPE_CLASS_MAP.get( fileType ).getSimpleName() ).setFilter( filter );
			int count = ServerUtils.countEntities( ds, q );
			if ( fileStat.getCount( fileType ) != count ) {
				count = 0;
				int storage = 0;
				final JQBuilder< ? extends FileMetaData > qb = new JQBuilder<>( pm, FILE_TYPE_CLASS_MAP.get( fileType ) ).filter( "ownerk==p1", "KEY p1" ).range( 0, 1000 );
				while ( true ) {
					List< ? extends FileMetaData > fileMetaDataList = qb.get( accountKey );
					for ( final FileMetaData fileMetaData : fileMetaDataList ) {
						count   ++;
						storage += fileMetaData.getSize();
					}
					
					if ( fileMetaDataList.size() < 1000 )
						break;
					
					qb.cursor( fileMetaDataList );
				}
				switch ( fileType ) {
				case SC2REPLAY :
					fileStat.setRepCount    ( count   );
					fileStat.setRepStorage  ( storage );
					break;
				case MOUSE_PRINT :
					fileStat.setSmpdCount   ( count   );
					fileStat.setSmpdStorage ( storage );
					break;
				case OTHER :
					fileStat.setOtherCount  ( count   );
					fileStat.setOtherStorage( storage );
					break;
				}
			}
		}
		
		// Totals
		fileStat.setCount  ( fileStat.getRepCount  () + fileStat.getSmpdCount  () + fileStat.getOtherCount  () );
		fileStat.setStorage( fileStat.getRepStorage() + fileStat.getSmpdStorage() + fileStat.getOtherStorage() );
		
		fileStat.setRecalced( new Date() );
	}
	
	private void processFileWithContent( final PersistenceManager pm, final Key fileKey, final String fileTypeString ) throws IOException {
		LOGGER.fine( "File key: " + fileKey + ", file type: " + fileTypeString );
		
		final FileType fileType = FileType.fromString( fileTypeString );
		
		final FileMetaData fmd;
		try {
			fmd =  (FileMetaData) pm.getObjectById( FILE_TYPE_CLASS_MAP.get( fileType ), fileKey );
		} catch ( final JDOObjectNotFoundException jonfe ) {
			LOGGER.warning( "File not found! (Deleted?)" );
			return;
		}
		LOGGER.fine( "sha1: " + fmd.getSha1() );
		if ( fmd.getBlobKey() != null && fmd.getContent() == null ) {
			LOGGER.warning( "This file is already processed!" );
			return;
		}
		if ( fmd.getContent() == null ) {
			LOGGER.warning( "File does not have content!" );
			return;
		}
		
		// Store content in the Blobstore
		final FileService      fileService = FileServiceFactory.getFileService();
		final AppEngineFile    appeFile    = fileService.createNewBlobFile( FILE_TYPE_MIME_TYPE_MAP.get( fileType ), fmd.getSha1() );
		final FileWriteChannel channel     = fileService.openWriteChannel( appeFile, true );
		final ByteBuffer       bb          = ByteBuffer.wrap( fmd.getContent().getBytes() );
		while ( bb.hasRemaining() )
			channel.write( bb );
		channel.closeFinally();
		
		fmd.setBlobKey( fileService.getBlobKey( appeFile ) );
		fmd.setContent( null );
		
		// I do not catch exceptions (so the task will be retried)
	}
	
	
	private static enum DepotServer {
		US ( "http://usb.depot.battle.net:1119/" ),
		EU ( "http://eub.depot.battle.net:1119/" ),
		KR ( "http://krb.depot.battle.net:1119/" ),
		SEA( "http://sg.depot.battle.net:1119/"  ),
		CN ( "http://cnb.depot.battle.net:1119/" ),
		PT ( "http://xx.depot.battle.net:1119/"  ); // Public test
		public final String url;
		private DepotServer( final String url ) {
			this.url = url;
		}
		public static DepotServer[] DEPOT_SERVERS = DepotServer.values();
	}
	private void processMap( final PersistenceManager pm, final String mapFileName ) {
		LOGGER.fine( "Map file name: " + mapFileName );
		
		// Check if map exists 
		final List< Map > mapList = new JQBuilder<>( pm, Map.class ).filter( "fname==p1", "String p1" ).get( mapFileName );
		final Map map = mapList.isEmpty() ? new Map( mapFileName ) : mapList.get( 0 );
		
		if ( map.getStatus() == Map.STATUS_READY )
			return;
		
		byte[] content = null;
		
		// Fetch map
		final URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
        // Not all maps are stored on all depot servers, try them all (until we get the map)
		for ( final DepotServer depotServer : DepotServer.DEPOT_SERVERS )
			try {
				// Default deadline: 5 seconds... Maps are a little bigger to fetch in 5 seconds...
				final HTTPRequest  request  = new HTTPRequest( new URL( depotServer.url + mapFileName ) );
				request.getFetchOptions().setDeadline( 540.0 ); // 9-minute deadline (leaving 1 minute to process...)
				final HTTPResponse response = urlFetchService.fetch( request );
		        
				if ( response.getResponseCode() == HttpServletResponse.SC_OK ) {
			        content = response.getContent();
			        if ( content != null ) {
			        	map.setFsize( content.length );
			        	map.setFsource( depotServer.name() );
			        	// Content here could be stored in the Blobstore...
			        	break;
			        }
		        }
	        } catch ( final Exception e ) {
	        	// Do not stop, try other depot servers...
	        }
    	if ( content == null ) {
        	map.setStatus( Map.STATUS_DL_ERROR );
	        
			if ( map.getKey() == null )
				pm.makePersistent( map );
			
			return;
    	}
    	
        // Parse map
    	MpqParser mpqParser = null;
        try {
        	mpqParser = new MpqParser( new ByteArrayMpqDataInput( content ) );
        	
        	// Pre-calculated hash values of file name: "Minimap.tga"
        	final byte[] tgaImageData = mpqParser.getFile( -1658863222, -1379586317, 1671265210 );
        	
			final ImagesService imagesService = ImagesServiceFactory.getImagesService();
			// TGA image is not supported, BMP is supported. We convert it to BMP
			final Image  image           = ImagesServiceFactory.makeImage( convertTgaToBmp( tgaImageData ) );
			// Now convert the BMP to JPEG
			final Image  outputImage     = imagesService.applyTransform( ImagesServiceFactory.makeRotate( 0 ), image, OutputEncoding.JPEG );
			final byte[] outputImageData = outputImage.getImageData();
			
			map.setSize  ( outputImageData.length      );
			map.setWidth ( outputImage.getWidth()      );
			map.setHeight( outputImage.getHeight()     );
			map.setImage ( new Blob( outputImageData ) );
			
	        map.setStatus( Map.STATUS_READY );
	        
        } catch ( final Exception e ) {
        	map.setStatus( Map.STATUS_PARSING_ERROR );
			LOGGER.log( Level.SEVERE, "", e );
        }
        
        // Map image ready, try to parse other data
        if ( mpqParser != null )
            try {
            	// Pre-calculated hash values of file name: "MapInfo"
        		final byte[] mapInfoData = mpqParser.getFile( 456326858, 2000504491, 1514959542 );
        		ByteBuffer wrapper = ByteBuffer.wrap( mapInfoData ).order( ByteOrder.LITTLE_ENDIAN );
        		if ( wrapper.getInt() == 0x4d617049 ) { // "IpaM" ("MapI" reversed)
        			final int version = wrapper.getInt();
        			if ( version > 0x17 )
        				wrapper.position( wrapper.position() + 8 ); // 2x unknown int
	        		map.setMwidth( wrapper.getInt() );
	        		map.setMheight( wrapper.getInt() );
	    			int value;
	    			do {
	    				value = wrapper.getInt();
	    				switch ( value ) {
	    				case 0x001 : break; // do nothing
	    				case 0x002 : while ( wrapper.get() != 0 ); break; // 0 char terminated string
	    				case 0x100 : wrapper.get(); break; // 1 extra byte? seen in version 0x1f
	    				case 0x400 : wrapper.get(); break; // 1 extra byte? seen in version 0x20
	    				case 0x000 :
	    					while ( wrapper.get() != 0 ) ; // Theme
	    					while ( wrapper.get() != 0 ) ; // Planet
	    					break;
	    				}
	    			} while ( value != 0 );
	    			StringBuilder mboundariesBuilder = new StringBuilder();
	        		mboundariesBuilder.append( wrapper.getInt() ).append( ',' )  // Boundary left
	        						  .append( wrapper.getInt() ).append( ',' )  // Boundary bottom
	        						  .append( wrapper.getInt() ).append( ',' )  // Boundary right
	        						  .append( wrapper.getInt() );               // Boundary top
	        		map.setMboundaries( mboundariesBuilder.toString() );
        		}
        		
            	// Pre-calculated hash values of file name: "DocumentHeader"
        		final byte[] docHeaderData = mpqParser.getFile( 967573924, 1586069117, 1498525374 );
        		wrapper = ByteBuffer.wrap( docHeaderData ).order( ByteOrder.LITTLE_ENDIAN );
        		wrapper.position( 44 );
        		final int dependenciesCount = wrapper.getInt();
        		for ( int i = 0; i < dependenciesCount; i++ )
	    			while ( wrapper.get() != 0 ) ;  // Dependency strings (0-char terminated)
        		final int pairsCount = wrapper.getInt();
        		
        		final int US_EN_LOCALE = 0x656e5553; // "SUne" => "USen"
        		byte[] buffer;
        		for ( int i = 0; i < pairsCount; i++ ) {
        			wrapper.get( buffer = new byte[ wrapper.getShort() ] ); // Key length and value
        			final String key = new String( buffer, "UTF-8" );
        			final int locale = wrapper.getInt();
        			wrapper.get( buffer = new byte[ wrapper.getShort() ] ); // Value length and value
        			final String value = new String( buffer, "UTF-8" );
        			if ( "DocInfo/Name".equals( key ) )
        				// We want the English name if available in the file
        				if ( map.getName() == null || locale == US_EN_LOCALE )
        					map.setName( value );
        		}
        		
            } catch ( final Exception e ) {
    			LOGGER.log( Level.SEVERE, "", e );
            } finally {
            	if ( mpqParser != null )
            		mpqParser.close();
            }
        
		if ( map.getKey() == null )
			pm.makePersistent( map );
	}
	
	/**
	 * Converts a TGA image to BMP image.<br>
	 * The TGA image must be uncompressed true color image (type=2).
	 * This is the format of the map previews stored in SC2 map files.
	 * 
	 * @param tgaData TGA image binary data to be converted
	 * @return BMP image binary data
	 */
	private static byte[] convertTgaToBmp( final byte[] tgaData ) {
		final int width  = ( tgaData[ 12 ] & 0xff ) | ( tgaData[ 13 ] & 0xff ) << 8;
		final int height = ( tgaData[ 14 ] & 0xff ) | ( tgaData[ 15 ] & 0xff ) << 8;
		
		final int TGA_IMAGE_DATA_OFFSET = 18;
		// First determine the useful area of the image (there is a black border around it => black border is unnecessary)
		int tgaPos = TGA_IMAGE_DATA_OFFSET;
		int x1 = Integer.MAX_VALUE, y1 = Integer.MAX_VALUE, x2 = Integer.MIN_VALUE, y2 = Integer.MIN_VALUE;
		byte b, g, r;
		for ( int y = 0; y < height; y++ )
			for ( int x = 0; x < width; x++ ) {
				b = tgaData[ tgaPos++ ];
				g = tgaData[ tgaPos++ ];
				r = tgaData[ tgaPos++ ];
				if ( b != 0 || g != 0 || r != 0 ) {
					if ( x < x1 ) x1 = x;
					if ( x > x2 ) x2 = x;
					if ( y < y1 ) y1 = y;
					if ( y > y2 ) y2 = y;
				}
			}
		
		final int bmpWidth  = x2 - x1 + 1;
		final int bmpHeight = y2 - y1 + 1;
		
		final int BMP_IMAGE_DATA_OFFSET = 54;
		// In BMP lines are aligned to 4 bytes
		final int        bmpLineSize = ( bmpWidth * 3 ) % 4 == 0 ? bmpWidth * 3 : bmpWidth * 3 + 4 - ( bmpWidth * 3 ) % 4;
		final byte[]     bmpData     = new byte[ BMP_IMAGE_DATA_OFFSET + bmpLineSize * bmpHeight ];
		final ByteBuffer bmpBuffer   = ByteBuffer.wrap( bmpData ).order( ByteOrder.LITTLE_ENDIAN );
		
		// BMP header (14 bytes)
		bmpBuffer.put( (byte) 0x42 );                // 'B'
		bmpBuffer.put( (byte) 0x4d );                // 'M'
		bmpBuffer.putInt( bmpData.length );          // Complete file (data) size
		bmpBuffer.putInt( 0 );                       // Reserved
		bmpBuffer.putInt( BMP_IMAGE_DATA_OFFSET );   // Offset to the image data bytes
		// BMP image info (40 bytes)
		bmpBuffer.putInt( 40 );                      // Image info header size (this section)
		bmpBuffer.putInt( bmpWidth );                // Obvious...
		bmpBuffer.putInt( bmpHeight );               // Obvious...
		bmpBuffer.putShort( (short) 1 );             // Number of color planes
		bmpBuffer.putShort( (short) 24 );            // Number of bits per pixel
		bmpBuffer.putInt( 0 );                       // Compression (0 => no compression)
		bmpBuffer.putInt( bmpLineSize * bmpHeight ); // Image data size (this is equal to bmpData.length - BMP_IMAGE_DATA_OFFSET)
		bmpBuffer.putInt( 0 );                       // x resolution (pixels/meter)
		bmpBuffer.putInt( 0 );                       // y resolution (pixels/meter)
		bmpBuffer.putInt( 0 );                       // Number of colors
		bmpBuffer.putInt( 0 );                       // Important colors
		
		// Now copy image data from TGA's buffer to BMP's buffer
		for ( int y = y1; y <= y2; y++ ) {
			// BMP picture is upside-down: lines are stored from bottom to up (and from left to right),
			// length padded with zeros to be a multiple of 4 (which padding zero bytes are "automatically" there) 
			bmpBuffer.position( BMP_IMAGE_DATA_OFFSET + ( y2 - y ) * bmpLineSize );
			bmpBuffer.put( tgaData, TGA_IMAGE_DATA_OFFSET + width * 3 * y + x1 * 3, bmpWidth * 3 );
		}
		
		return bmpData;
	}
	
	private void customTask( final HttpServletRequest request, final PersistenceManager pm ) throws IOException {
		LOGGER.fine( "Key: " + request.getParameter( "key" ) + ", file type: " + request.getParameter( "fileType" ) );
		
		final FileType fileType = FileType.fromString( request.getParameter( "fileType" ) );
		if ( fileType == null ) {
			LOGGER.severe( "Invalid File type!" );
			return;
		}
		
		final Key key = KeyFactory.stringToKey( request.getParameter( "key" ) );
		
		final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        final Entity e;
		try {
	        e = ds.get( key );
        } catch ( final EntityNotFoundException enfe ) {
			LOGGER.log( Level.WARNING, "Entity not found!", enfe );
	        return;
        }
		
        if ( !e.getKind().equals( "Rep" ) && !e.getKind().equals( "Smpd" ) && !e.getKind().equals( "OtherFile" ) ) {
			LOGGER.severe( "Invalid Entity kind:" + e.getKind() );
			return;
        }
        
        if ( (Long) e.getProperty( "v" ) == 4 ) {
			LOGGER.warning( "Entity is already v4!" );
			return;
		}
        
        // Update common properties:
        // TODO
        final int size = ( (Long) e.getProperty( "size" ) ).intValue();
        if ( size < FileServlet.DATASTORE_CONTENT_STORE_LIMIT ) {
            final FileService fileService = FileServiceFactory.getFileService();
    		final AppEngineFile   appeFile = new AppEngineFile( FileSystem.BLOBSTORE, ( (BlobKey) e.getProperty( "blobKey" ) ).getKeyString() );
    		final FileReadChannel channel  = fileService.openReadChannel( appeFile, false );
    		final byte[]          content  = new byte[ size ];
    		final ByteBuffer      wrapper  = ByteBuffer.wrap( content );
    		while ( channel.read( wrapper ) > 0 )
    			;
    		channel.close();
    		
    		e.setProperty( "content", new Blob( content ) );
    		e.setProperty( "blobKey", null );
    		fileService.delete( appeFile );
        }
        e.setUnindexedProperty( "blobKey", e.getProperty( "blobKey" ) );
        e.setUnindexedProperty( "content", e.getProperty( "content" ) );
        
        switch ( fileType ) {
        case SC2REPLAY :
            e.setUnindexedProperty( "matchup", e.getProperty( "matchup" ) );
        	break;
        case MOUSE_PRINT :
        	break;
        case OTHER :
        	break;
        default:
        	throw new RuntimeException( "Invalid file type!" );
        }
        
        // UPGRADE COMPLETE!
		e.setProperty( "v", 4 );
		ds.put( e );
	}
	
}
