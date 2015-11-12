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

import static hu.belicza.andras.sc2gearsdbapi.ServletApi.PARAM_AUTHORIZATION_KEY;
import static hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi.GAME_MOUSE_PRACTICE;
import static hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi.OPERATION_CHECK_SCORE;
import static hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi.OPERATION_GET_TOP_SCORES;
import static hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi.OPERATION_SUBMIT_SCORE;
import static hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi.PARAM_ACCURACY;
import static hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi.PARAM_GAME;
import static hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi.PARAM_GAME_LENGTH;
import static hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi.PARAM_GAME_VERSION;
import static hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi.PARAM_HITS;
import static hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi.PARAM_RANDOM_SEED;
import static hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi.PARAM_SCORE;
import static hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi.PARAM_USER_NAME;
import static hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi.PROTOCOL_VERSION_1;
import hu.belicza.andras.sc2gearsdb.beans.MousePracticeGameScoreInfo;
import hu.belicza.andras.sc2gearsdb.datastore.MousePracticeGameScore;
import hu.belicza.andras.sc2gearsdb.util.CachingService;
import hu.belicza.andras.sc2gearsdb.util.PMF;
import hu.belicza.andras.sc2gearsdb.util.ServerUtils;
import hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;

/**
 * A general information provider servlet.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class TopScoresServlet extends BaseServlet {
	
	private static final Logger LOGGER = Logger.getLogger( TopScoresServlet.class.getName() );
	
	/** Number of entries in top score table. */
	public static final int TOP_SCORE_TABLE_SIZE = 500;
	
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
		
		final String game = request.getParameter( PARAM_GAME );
		if ( game == null ) {
			LOGGER.warning( "Missing Game parameter!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing Game parameter!" );
			return;
		}
		
		// Check possible game values (currently only 1 game: Mouse practice)
		switch ( game ) {
		case GAME_MOUSE_PRACTICE :
			break;
		default :
			LOGGER.warning( "Invalid Game parameter!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid Game parameter!" );
			return;
		}
		
		switch ( operation ) {
		case OPERATION_GET_TOP_SCORES :
			if ( GAME_MOUSE_PRACTICE.equals( game ) )
				getMousePracticeTopScores( request, response );
			break;
		case OPERATION_CHECK_SCORE :
			if ( GAME_MOUSE_PRACTICE.equals( game ) )
				checkMousePracticeScore( request, response );
			break;
		default :
    		// The rest requires an Authorization key
    		final String authorizationKey = request.getParameter( PARAM_AUTHORIZATION_KEY );
    		if ( authorizationKey == null || authorizationKey.isEmpty() ) {
    			LOGGER.warning( "Missing Authorization Key!" );
    			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing Authorization Key!" );
    			return;
    		}
    		
    		switch ( operation ) {
    		case OPERATION_SUBMIT_SCORE :
    			if ( GAME_MOUSE_PRACTICE.equals( game ) )
    				submitMousePracticeScore( authorizationKey, request, response );
    			break;
    		default :
    			LOGGER.warning( "Invalid Operation! (Authorization key: " + authorizationKey + ")" );
    			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid Operation!" );
    			return;
    		}
		}
	}
	
	/**
	 * Serves the Mouse practice game top scores table.
	 * 
	 * <p>GET url:<br>
	 * https://sciigears.appspot.com/topScores?protVer=1&game=mousePractice&op=getTopScores
	 * </p>
	 */
	private void getMousePracticeTopScores( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		LOGGER.fine( "" );
		
		final String  userAgent     = request.getHeader( "user-agent" );
		final boolean nonJavaClient = userAgent != null && !userAgent.contains( "Java" );
		
		final List< MousePracticeGameScoreInfo > scoreList = CachingService.getMousePracticeTopScores();
		
		// Render output HTML
		
		response.setContentType( "text/html" );
		// Never cache high-score table:
		setNoCache( response );
		
		final PrintWriter out = response.getWriter();
		out.println( "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" );
		if ( nonJavaClient )
			out.println( "<title>Mouse Practice Game Top Scores - Sc2gears Database</title>" );
		includeDefaultCss( out );
		// Include GA tracker code for browsers
		if ( nonJavaClient )
			out.println( GA_TRACKER_HTML_SCRIPT );
		out.println( "</head><body><center>" );
		
		out.println( "<h1 class=\"h1\">Mouse Practice Game Top Scores</h1>" );
		
		if ( nonJavaClient ) {
			out.println( HEADER_AD_728_90_HTML_SCRIPT );
			out.println( "<br>" );
		}
		
		out.println( "<table border=1 cellpadding=3 cellspacing=0>" );
		out.println( "<tr class=\"headerRow\"><td>Score<br>Rank<td>Person<br>Rank<td>Name<td>Score<td>Accuracy<td>Hits" );
		int rank = 1;
		for ( final MousePracticeGameScoreInfo score : scoreList ) {
			out.println( "<tr class=\"" + ( rank > 3 ? "row" + ( rank & 0x01 ) : rank == 3 ? "bronze" : rank == 2 ? "silver" : "gold" ) + "\" align=right><td>" + rank
					+ ".<td>" + score.getPersonRank()
					+ ".<td align=left>" + ServerUtils.encodeHtmlString( score.getUserName(), false )
					+ "<td>" + score.getScore()
					+ "<td>" + (int) ( 100 * score.getAccuracy() )
					+ "%<td>" + score.getHits() );
			rank++;
		}
		out.println( "</table>" );
		
		out.println( "</center>" );
		
		// Include Footer for browsers
		if ( nonJavaClient )
			out.println( FOOTER_COPYRIGHT_HTML );
		out.println( "</body></html>" );
	}
	
	/**
	 * Checks if a Mouse practice game score made it to the top score table.<br>
	 * Sends back a plain text response: 1 line exactly, a number of the place of the score; -1 if the score did not make it to the top score table
	 */
	private static void checkMousePracticeScore( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		final Integer userScore = getIntParam( request, PARAM_SCORE );
		if ( userScore == null ) {
			LOGGER.warning( "Missing parameters!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
			return;
		}
		
		LOGGER.fine( "User score: " + userScore );
		
		final List< MousePracticeGameScoreInfo > scoreList = CachingService.getMousePracticeTopScores();
		int i = scoreList.size() - 1;
		while ( i >= 0 && scoreList.get( i ).getScore() < userScore )
			i--;
		
		// The placement:
		i += 2;
		
		response.setContentType( "text/plain" );
		response.getWriter().println( i <= TOP_SCORE_TABLE_SIZE ? i : -1 );
	}
	
	/**
	 * Stores the submitted Mouse practice game score.
	 * @param authorizationKey authorization key of the user
	 */
	private static void submitMousePracticeScore( final String authorizationKey, final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		final String  userName    = request.getParameter( PARAM_USER_NAME     );
		final String  gameVersion = request.getParameter( PARAM_GAME_VERSION  );
		final Integer userScore   = getIntParam  ( request, PARAM_SCORE       );
		final Float   accuracy    = getFloatParam( request, PARAM_ACCURACY    );
		final Integer hits        = getIntParam  ( request, PARAM_HITS        );
		final Integer gameLength  = getIntParam  ( request, PARAM_GAME_LENGTH );
		final Long    randomSeed  = getLongParam ( request, PARAM_RANDOM_SEED );
		
		if ( userName == null || gameVersion == null || userScore == null || accuracy == null || hits == null || gameLength == null ) {
			LOGGER.warning( "Missing parameters!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing parameters!" );
			return;
		}
		
		LOGGER.fine( "Authorization key: " + authorizationKey + ", user score: " + userScore );
		
		if ( userScore <= 0 ) {
			LOGGER.warning( "Invalid parameter values!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter values!" );
			return;
		}
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			// Account
			final Key accountKey = CachingService.getAccountKeyByAuthKey( pm, authorizationKey );
			if ( accountKey == null ) {
				LOGGER.warning( "Unauthorized access, invalid Authorization Key!" );
				response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized access, invalid Authorization Key!" );
				return;
			}
			
			final MousePracticeGameScore score = new MousePracticeGameScore();
			
			score.setAccountKey ( accountKey                                   );
			score.fillTracking  ( request                                      );
			score.setUserName   ( TopScoresServletApi.trimUserName( userName ) );
			score.setGameVersion( gameVersion                                  );
			score.setScore      ( userScore                                    );
			score.setAccuracy   ( accuracy                                     );
			score.setHits       ( hits                                         );
			score.setGameLength ( gameLength                                   );
			score.setRandomSeed ( randomSeed                                   );
			
			pm.makePersistent( score );
			
	    	// Invalidate the cached top scores table
			CachingService.removeMousePracticeTopScores();
			
			// I do not delete scores beyond the table size, this will allow viewing the historical score table (score table at any given time) 
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
}
