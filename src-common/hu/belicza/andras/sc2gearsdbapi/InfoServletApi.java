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

import hu.belicza.andras.sc2gearsdb.InfoServlet;

/**
 * API of the {@link InfoServlet}.
 * 
 * @author Andras Belicza
 */
public class InfoServletApi extends ServletApi {
	
	/** Protocol version 1. */
	public static final String PROTOCOL_VERSION_1 = "1";
	
	/** Get public replay comments operation. */
	public static final String OPERATION_GET_PUBLIC_REP_COMMENTS = "getPubRepCom";
	/** Get public replay profile operation.  */
	public static final String OPERATION_GET_PUBLIC_REP_PROFILE  = "getPubRepProf";
	/** Save public replay comment operation. */
	public static final String OPERATION_SAVE_PUBLIC_REP_COMMENT = "savePubRepCom";
	/** Check authorization key operation.    */
	public static final String OPERATION_CHECK_KEY               = "checkKey";
	/** Get quota info operation.             */
	public static final String OPERATION_GET_QUOTA_INFO          = "getQtInf";
	/** Get private replay data operation.    */
	public static final String OPERATION_GET_PRIVATE_REP_DATA    = "getPrivDat";
	/** Save private replay data operation.   */
	public static final String OPERATION_SAVE_PRIVATE_REP_DATA   = "savePrivDat";
	
	// Common parameters
	/** SHA-1 of the replay parameter. */
	public static final String PARAM_SHA1      = "sha1";
	/** Replay comment parameter. */
	public static final String PARAM_COMMENT   = "comment";
	
	// Get public rep comments parameters
	/** Offset parameter.         */
	public static final String PARAM_OFFSET    = "offset";
	/** Limit parameter.          */
	public static final String PARAM_LIMIT     = "limit";
	/** Time zone parameter.      */
	public static final String PARAM_TIME_ZONE = "tzone";
	
	// Save public rep comment parameters
	/** Replay comment parameter. */
	public static final String PARAM_USER_NAME = "userName";
	/** GG replay rate parameter. */
	public static final String PARAM_RATE_GG   = "rateGg";
	/** GG replay rate parameter. */
	public static final String PARAM_RATE_BG   = "rateBg";
	
	// Save private rep data parameters
	/** Labels parameter.         */
	public static final String PARAM_LABELS    = "labels";
	
	// Parameter restriction constants
	/** Max value of the limit parameter value.            */
	public static final int MAX_LIMIT_VALUE            = 20;
	/** Max length of the user name parameter value.       */
	public static final int MAX_USER_NAME_LENGTH       = 50;
	/** Max length of the public comment parameter value.  */
	public static final int MAX_PUBLIC_COMMENT_LENGTH  = 65536;
	/** Max length of the private comment parameter value. */
	public static final int MAX_PRIVATE_COMMENT_LENGTH = 500;
	
	/**
	 * Returns a trimmed user name.
	 * @param userName user name to be trimmed
	 * @return a trimmed user name
	 */
	public static String trimUserName( String userName ) {
		return trimStringLength( userName, MAX_USER_NAME_LENGTH );
	}
	
	/**
	 * Returns a trimmed public replay comment.
	 * @param comment public comment to be trimmed
	 * @return a trimmed public replay comment
	 */
	public static String trimPublicRepComment( String comment ) {
		return trimStringLength( comment, MAX_PUBLIC_COMMENT_LENGTH );
	}
	
	/**
	 * Returns a trimmed private replay comment.
	 * @param comment private comment to be trimmed
	 * @return a trimmed private replay comment
	 */
	public static String trimPrivateRepComment( String comment ) {
		return trimStringLength( comment, MAX_PRIVATE_COMMENT_LENGTH );
	}
	
}
