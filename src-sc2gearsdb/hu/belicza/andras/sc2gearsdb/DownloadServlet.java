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

import hu.belicza.andras.sc2gearsdb.apiuser.server.ApiUserServiceImpl;
import hu.belicza.andras.sc2gearsdb.datastore.D;
import hu.belicza.andras.sc2gearsdb.user.server.UserServiceImpl;
import hu.belicza.andras.sc2gearsdb.util.PMF;
import hu.belicza.andras.sc2gearsdb.util.ServerUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

/**
 * A tracking, public download servlet.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class DownloadServlet extends HttpServlet {
	
	private static final Logger LOGGER = Logger.getLogger( DownloadServlet.class.getName() );
	
	/**
	 * Download descriptor.
	 * @author Andras Belicza
	 */
	private static class DownloadDescriptor {
		/** The destination URL of the file. */
		public final String destinationdUrl;
		/** Tells how often the download counter has to be written to the datastore.<br>
		 * The download stat will be written to the datastore when the new (mem-cached) downloads count reaches this limit.<br>
		 * This mechanism is introduced to reduce the write OPs (including the index write ops),
		 * and it will also reduce read OPs (including queries). */
		public final int    counterPersistingCycle;
		
		/**
		 * Creates a new DownloadDescriptor.
		 * @param destinationUrl         the destination URL of the file
		 * @param counterPersistingCycle tells how often the download counter has to be written to the datastore
		 */
		public DownloadDescriptor( final String destinationUrl, final int counterPersistingCycle ) {
			this.destinationdUrl        = destinationUrl;
			this.counterPersistingCycle = counterPersistingCycle;
		}
	}
	
	/** File name of the latest release.            */
	public static final String LATEST_RELEASE_FILE_NAME            = "Sc2gears-14.3.3.zip";
	/** File name of the latest plugin API release. */
	public static final String LATEST_PLUGIN_API_RELEASE_FILE_NAME = "Sc2gears_Plugin_API_4.2.zip";
	
	/** Routing file map. Tells where downloadable files are available. */
	private static final Map< String, DownloadDescriptor > ROUTING_MAP = new HashMap< String, DownloadDescriptor >();
	static {
		// RELEASE
		// Main source of the latest release:
		ROUTING_MAP.put( LATEST_RELEASE_FILE_NAME            , new DownloadDescriptor( "/hosted/" + LATEST_RELEASE_FILE_NAME, 1 ) );
		//ROUTING_MAP.put( LATEST_RELEASE_FILE_NAME            , new DownloadDescriptor( "http://www.mediafire.com/download/0lpt41a0b3d534i/" + LATEST_RELEASE_FILE_NAME, 1 ) );
		//ROUTING_MAP.put( LATEST_RELEASE_FILE_NAME            , new DownloadDescriptor( "http://dl.dropbox.com/u/19063641/"              + LATEST_RELEASE_FILE_NAME, 1 ) );
		
		// Plugin API has to be here because the online plugin API javadoc is served from this file!
		ROUTING_MAP.put( LATEST_PLUGIN_API_RELEASE_FILE_NAME , new DownloadDescriptor( "/hosted/" + LATEST_PLUGIN_API_RELEASE_FILE_NAME, 1  ) );
		
		// HOSTED
		ROUTING_MAP.put( "latest_version.xml"                , new DownloadDescriptor( "/hosted/latest_version.xml"                , 20 ) );
		ROUTING_MAP.put( "custom_portraits.xml"              , new DownloadDescriptor( "/hosted/custom_portraits.xml"              , 20 ) );
		ROUTING_MAP.put( "start_page.html"                   , new DownloadDescriptor( "/hosted/start_page.html"                   , 20 ) );
		ROUTING_MAP.put( "start_page_all_sc2gears_news.html" , new DownloadDescriptor( "/hosted/start_page_all_sc2gears_news.html" , 1  ) );
		
		// LANGUAGES
		// TODO language files could always be here, dynamically inserting the latest version... (latest language version not app version!)
		//ROUTING_MAP.put( "lang/10.5/2/English.xml"           , new DownloadDescriptor( "/hosted/lang/English.xml"                  , 1  ) );
		//ROUTING_MAP.put( "lang/12.4/1/German.xml"            , new DownloadDescriptor( "/hosted/lang/German.xml"                   , 1  ) );
		//ROUTING_MAP.put( "lang/12.2/2/French.xml"            , new DownloadDescriptor( "/hosted/lang/French.xml"                   , 1  ) );
		//ROUTING_MAP.put( "lang/10.5/1/Polish.xml"            , new DownloadDescriptor( "/hosted/lang/Polish.xml"                   , 1  ) );
		//ROUTING_MAP.put( "lang/14.1/1/Italian.xml"           , new DownloadDescriptor( "/hosted/lang/Italian.xml"                  , 1  ) );
		//ROUTING_MAP.put( "lang/12.4/1/Russian.xml"           , new DownloadDescriptor( "/hosted/lang/Russian.xml"                  , 1  ) );
		//ROUTING_MAP.put( "lang/12.3/1/Portuguese-BR.xml"     , new DownloadDescriptor( "/hosted/lang/Portuguese-BR.xml"            , 1  ) );
		//ROUTING_MAP.put( "lang/12.3/1/Chinese.xml"           , new DownloadDescriptor( "/hosted/lang/Chinese.xml"                  , 1  ) );
		//ROUTING_MAP.put( "lang/12.3/1/ChineseTraditional.xml", new DownloadDescriptor( "/hosted/lang/ChineseTraditional.xml"       , 1  ) );
		
		// MISCELLANEOUS
		ROUTING_MAP.put( "custom_portrait.SC2Replay"         , new DownloadDescriptor( "/hosted/custom_portrait.SC2Replay", 1 ) );
		ROUTING_MAP.put( "portraits.zip"                     , new DownloadDescriptor( "/hosted/portraits.zip"            , 1 ) );
		//ROUTING_MAP.put( "custom_portrait.SC2Replay"         , new DownloadDescriptor( "http://www.mediafire.com/file/w0qzk4bg81jk55s/custom_portrait.SC2Replay", 1 ) );
		//ROUTING_MAP.put( "portraits.zip"                     , new DownloadDescriptor( "http://www.mediafire.com/file/sx2i64ja64hge48/portraits.zip"            , 1 ) );
	}
	
	/** Tells which static file names are not to be logged.
	 * (<code>"STATIC"</code> because there are dynamic language file names.) */
	private static final Set< String > UNLOGGABLE_STATIC_FILE_NAME_SET = new HashSet< String >();
	static {
		UNLOGGABLE_STATIC_FILE_NAME_SET.add( "custom_portraits.xml"              );
		UNLOGGABLE_STATIC_FILE_NAME_SET.add( "latest_version.xml"                );
		UNLOGGABLE_STATIC_FILE_NAME_SET.add( "start_page.html"                   );
		UNLOGGABLE_STATIC_FILE_NAME_SET.add( "start_page_all_sc2gears_news.html" );
	}
	
	/** Tells which file names are subject to have daily download stats. */
	public static final Set< String > DAILY_STATS_FILE_NAME_SET;
	static {
		final Set< String > fileNameSet = new HashSet< String >();
		
		fileNameSet.addAll( UNLOGGABLE_STATIC_FILE_NAME_SET );
		
		fileNameSet.add( UserServiceImpl   .VISIT_STATS_FILE_NAME     );
		fileNameSet.add( ApiUserServiceImpl.API_VISIT_STATS_FILE_NAME );
		
		DAILY_STATS_FILE_NAME_SET = Collections.unmodifiableSet( fileNameSet );
	}
	
	/**
	 * Tells if the specified file name is loggable.
	 * @param fileName file name to be tested
	 * @return true if the specified file name is loggable; false otherwise
	 */
	private static boolean isFileNameLoggable( final String fileName ) {
		if ( UNLOGGABLE_STATIC_FILE_NAME_SET.contains( fileName ) )
			return false;
		
		if ( fileName.startsWith( "lang/" ) )
			return false;
		
		return true;
	}
	
	/*
	 * doGet() is more common in case of this servlet, so doPost() calls doGet().
	 */
	@Override
	protected void doPost( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		doGet( request, response );
	}
	
	@Override
	protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		String fileName = request.getPathInfo();
		if ( fileName != null && !fileName.isEmpty() )
			fileName = fileName.substring( 1 ); // Cut off leading slash
		
		final DownloadDescriptor downloadDescriptor = fileName == null ? null : ROUTING_MAP.get( fileName );
		
		if ( downloadDescriptor == null ) {
			LOGGER.warning( "The requested file could not be found on the server!" );
			response.sendError( HttpServletResponse.SC_NOT_FOUND, "The requested file could not be found on the server!" );
			return;
		}
		
		response.sendRedirect( downloadDescriptor.destinationdUrl );
		
		PersistenceManager pm = null;
		try {
			final String ip = request.getRemoteAddr();
			
			boolean unique = false;
			
			if ( isFileNameLoggable( fileName ) ) {
				pm = PMF.get().getPersistenceManager();
				
				// Check if IP is unique
				// Using low-level implementation due to JDO keys-only query does not count toward the "Datastore Small Ops" but instead toward "Datastore Read Ops"
				final com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query( D.class.getSimpleName() );
				q.setFilter( CompositeFilterOperator.and(
					new FilterPredicate( "f", FilterOperator.EQUAL, fileName ),
					new FilterPredicate( "i", FilterOperator.EQUAL, ip       ) )
				);
				unique = ServerUtils.isQueryResultEmpty( q );
				
				// Log download
				final D d = new D();
				d.setF( fileName );
				d.setI( ip );
				d.setU( ServerUtils.checkUserAgent( request.getHeader( "User-Agent" ) ) );
				d.setC( ServerUtils.getLocationString( request ) );
				
				pm.makePersistent( d );
			}
			
			// Update download stats
			TaskServlet.register_updateDownloadStat( fileName, request.getHeader( "User-Agent" ), unique, downloadDescriptor.counterPersistingCycle );
			
		} catch ( final Exception e ) {
			// Don't let exceptions get away, still send back the redirect
			LOGGER.log( Level.SEVERE, "Exception logging the download, still proceeding...", e );
			e.printStackTrace();
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
}
