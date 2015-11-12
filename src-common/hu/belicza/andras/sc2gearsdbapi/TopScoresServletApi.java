/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdbapi;

import hu.belicza.andras.sc2gearsdb.TopScoresServlet;

/**
 * API of the {@link TopScoresServlet}.
 * 
 * @author Andras Belicza
 */
public class TopScoresServletApi extends ServletApi {
	
	/** Protocol version 1. */
	public static final String PROTOCOL_VERSION_1 = "1";
	
	/** Check score operation.    */
	public static final String OPERATION_CHECK_SCORE    = "checkScore";
	/** Submit score operation.   */
	public static final String OPERATION_SUBMIT_SCORE   = "submitScore";
	/** Get top scores operation. */
	public static final String OPERATION_GET_TOP_SCORES = "getTopScores";
	
	// Common parameters
	/** Game parameter.         */
	public static final String PARAM_GAME         = "game";
	/** User name parameter.    */
	public static final String PARAM_USER_NAME    = "userName";
	/** Score parameter.        */
	public static final String PARAM_SCORE        = "score";
	/** Game version parameter. */
	public static final String PARAM_GAME_VERSION = "gameVer";
	
	// Mouse practice game parameters
	/** Accuracy parameter.          */
	public static final String PARAM_ACCURACY     = "accuracy";
	/** Hits parameter.              */
	public static final String PARAM_HITS         = "hits";
	/** Game length parameter.       */
	public static final String PARAM_GAME_LENGTH  = "gameLength";
	/** Start random seed parameter. */
	public static final String PARAM_RANDOM_SEED  = "randomSeed";
	
	/** Mouse practice game. */
	public static final String GAME_MOUSE_PRACTICE = "mousePractice";
	
	// Parameter restriction constants
	/** Max length of the user name parameter values. */
	public static final int MAX_USER_NAME_LENGTH = 50;
	
	/**
	 * Returns a trimmed user name.
	 * @param userName user name to be trimmed
	 * @return a trimmed user name
	 */
	public static String trimUserName( final String userName ) {
		return trimStringLength( userName, MAX_USER_NAME_LENGTH );
	}
	
}
