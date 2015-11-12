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

/**
 * Common ancestor of servlet API classes.
 * 
 * @author Andras Belicza
 */
public class ServletApi {
	
	/** Protocol version parameter.                       */
	public static final String PARAM_PROTOCOL_VERSION  = "protVer";
	/** Operation parameter.                              */
	public static final String PARAM_OPERATION         = "op";
	/** Authorization key parameter.                      */
	public static final String PARAM_AUTHORIZATION_KEY = "authKey";
	
	/**
	 * Returns a trimmed string.
	 * @param s         string to be trimmed
	 * @param maxLength max allowed length of the string
	 * @return a trimmed string
	 */
	public static String trimStringLength( String s, final int maxLength ) {
		if ( s == null )
			return null;
		
		s = s.trim();
		if ( s.length() > maxLength )
			s = s.substring( 0, maxLength );
		
		return s;
	}
	
}
