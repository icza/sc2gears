/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * General utils face.
 * 
 * @author Andras Belicza
 */
public class GeneralUtils {
	
	/** Digits used in the hexadecimal representation. */
	public static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	private static final List< String > FAVORED_PLAYER_LIST = new ArrayList< String >( 0 );
	
	/**
	 * Converts the specified data to hex string.
	 * @param data data to be converted
	 * @return the specified data converted to hex string
	 */
	public static String convertToHexString( final byte[] data ) {
		return convertToHexString( data, 0, data.length );
	}
	
	/**
	 * Converts the specified data to hex string.
	 * @param data   data to be converted
	 * @param offset offset of first byte to convert 
	 * @param length number of bytes to convert
	 * @return the specified data converted to hex string
	 */
	public static String convertToHexString( final byte[] data, int offset, int length ) {
		final StringBuilder hexBuilder = new StringBuilder( data.length << 1 );
		
		length += offset;
		for ( ; offset < length; offset++ ) {
			final byte b = data[ offset ];
			hexBuilder.append( HEX_DIGITS[ ( b & 0xff ) >> 4 ] ).append( HEX_DIGITS[ b & 0x0f ] );
		}
		
		return hexBuilder.toString();
	}
	
	/**
	 * Creates and returns a HashSet with the arguments.
	 * @param <T>    type of the arguments
	 * @param values values to put in the set
	 * @return a HashSet with the argument
	 */
	public static < T > Set< T > assembleHashSet( /*@SuppressWarnings("unchecked")*/ final T... values ) {
		final Set< T > set = new HashSet< T >( values.length );
		
		for ( final T value : values )
			set.add( value );
		
		return set;
	}
	
	/**
	 * Returns the favored player list.
	 * @return the favored player list
	 */
	public static List< String > getFavoredPlayerList() {
		return FAVORED_PLAYER_LIST;
	}
	
}
