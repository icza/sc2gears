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

import hu.belicza.andras.sc2gearsdb.datastore.Map;
import hu.belicza.andras.sc2gearsdb.util.JQBuilder;
import hu.belicza.andras.sc2gearsdb.util.PMF;
import hu.belicza.andras.sc2gearsdb.util.ServerUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * A servlet that serves map images.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class MapImageServlet extends BaseServlet {
	
	private static final Logger LOGGER = Logger.getLogger( MapImageServlet.class.getName() );
	
	/*
	 * doGet() is more common in case of this servlet, so doPost() calls doGet().
	 */
	@Override
	protected void doPost( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		doGet( request, response );
	}
	
    @Override
	protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		// Restrict map image download for logged in Google accounts
    	// This simple check does not require datastore read but will prevent
    	// images to get "stolen" by simple downloader apps...
    	final UserService userService = UserServiceFactory.getUserService();
		final User        user        = userService.getCurrentUser();
		if ( user == null ) {
			LOGGER.warning( "Unauthorized access, not logged in!" );
			response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized access, you are not logged in!" );
			return;
		}
		
		String mapFileName = request.getPathInfo();
		if ( mapFileName != null && !mapFileName.isEmpty() )
			mapFileName = mapFileName.substring( 1 ); // Cut off leading slash
		
		if ( mapFileName == null ) {
			LOGGER.warning( "Missing map file name!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing map file name!" );
			return;
		}
		if ( ( mapFileName = ServerUtils.checkMapFileName( mapFileName ) ) == null ) {
			LOGGER.warning( "Invalid map file name!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid map file name!" );
			return;
		}
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			final List< Map > mapList = new JQBuilder<>( pm, Map.class ).filter( "fname==p1", "String p1" ).get( mapFileName );
			
			if ( mapList.isEmpty() ) {
				// Register a task to process the map
				TaskServlet.register_processMapTask( mapFileName );
				
				// Serve "processing" image
				LOGGER.fine( "Response: Processing image..." );
				setNoCache( response );
				getServletContext().getRequestDispatcher( "/images/fugue/hourglass.png" ).forward( request, response );
			}
			else {
				final Map map = mapList.get( 0 );
				
				switch ( map.getStatus() ) {
				case Map.STATUS_READY :
					// Serve the image
					response.setContentType( "image/jpeg" );
					response.setDateHeader( "Expires", new Date().getTime() + 60L*24*60*60*1000 ); // 60 days cache time
					response.addHeader( "Cache-Control", "private, max-age=5184000" ); // 5_184_000 sec = 60 days cache time
					response.getOutputStream().write( map.getImage().getBytes() );
					break;
				case Map.STATUS_PROCESSING :
					// Serve "processing" image
					LOGGER.fine( "Response: Processing image..." );
					setNoCache( response );
					getServletContext().getRequestDispatcher( "/images/fugue/hourglass.png" ).forward( request, response );
					break;
				default :
					// Serve "N/A" image
					LOGGER.fine( "Response: N/A" );
					setNoCache( response );
					getServletContext().getRequestDispatcher( "/images/fugue/na.png"        ).forward( request, response );
					break;
				}
			}
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
    
}
