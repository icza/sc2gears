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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Base64 encoding utility.
 * <p>Info about Base64: <a href="http://en.wikipedia.org/wiki/Base64">Base64 on Wikipedia</a></p>
 * 
 * @author Andras Belicza
 */
public class Base64 {
	
	/** Symbols used in the base64 format. */
	private static char[] BASE64_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	/** Base64 padding character.          */
	private static char   BASE64_PADDING = '=';
	
	/**
	 * Returns the base64 encoded form of the specified file.
	 * @param file file to be encoded
	 * @return the base64 encoded form of the specified file; or <code>null</code> if some error occurs
	 */
	public static String encodeFile( final File file ) {
		if ( !file.exists() )
			return null;
		
		// 3 bytes results in 4: charCount = RoundUp( size / 3 ) * 4
		int bytesLeft = (int) file.length();
		final char[] encoded = new char[ ( (bytesLeft+2) / 3 ) * 4 ];
		
		int charPos = 0;
		try ( final InputStream input = new FileInputStream( file ) ) {
			while ( bytesLeft > 0 ) {
				final int byte1 = input.read();
				final int byte2 = bytesLeft > 1 ? input.read() : 0;
				final int byte3 = bytesLeft > 2 ? input.read() : 0;
				
				encoded[ charPos++ ] = BASE64_SYMBOLS[ byte1 >> 2 ];
				encoded[ charPos++ ] = BASE64_SYMBOLS[ ( byte1 & 0x03 ) << 4 | ( byte2 & 0xf0 ) >> 4 ];
				
				if ( bytesLeft > 1 ) {
					encoded[ charPos++ ] = BASE64_SYMBOLS[ ( byte2 & 0x0f ) << 2 | ( byte3 & 0xc0 ) >> 6 ];
					
					if ( bytesLeft > 2 )
						encoded[ charPos++ ] = BASE64_SYMBOLS[ byte3 &0x3f ];
					else
						// 1 padding byte
						encoded[ charPos++ ] = BASE64_PADDING;
				}
				else {
					// 2 padding bytes
					encoded[ charPos++ ] = BASE64_PADDING;
					encoded[ charPos++ ] = BASE64_PADDING;
				}
				
				bytesLeft -= 3;
			}
		} catch ( final Exception e ) {
			e.printStackTrace();
			return null;
		}
		
		return new String( encoded );
	}
	
}
