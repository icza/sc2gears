/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.smpd;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * SMPD utility class.<br>
 * Provides utility methods to handle the SMPD format.
 * 
 * <p>Mouse Print Data file specification can be found <a href="http://sites.google.com/site/sc2gears/features/mouse-print-recorder">here</a>.</p>
 * 
 * @author Andras Belicza
 */
public class SmpdUtil {
	
	/** Extension of the SMPD files. */
	public static final String SMPD_FILE_EXTENSION = "smpd";
	
	/** SMPD magic bytes.           */
	public static final byte[] SMPD_MAGIC = "SMPD".getBytes();
	
	/**
	 * SMPD version.
	 * @author Andras Belicza
	 */
	public enum SmpdVer {
		/** Version 1.1. */
		V11    ( (short) 0x0101, 42 ),
		/** Version 1.0. */
		V10    ( (short) 0x0100, 41 ),
		/** Invalid version. */
		UNKNOWN( (short) 0     , 0  );
		
		/** Binary value of the version. */
		public final short binaryValue;
		/** Min SMPD header size of the version.
		 * Header size may vary based on some variable-length header field (e.g. app name), this is the minimum length. */
		public final int   minHeaderSize;
		
		/**
		 * Creates a new SmpdVer.
		 * @param binaryValue   binary value of the version
		 * @param minHeaderSize min SMPD header size
		 */
		private SmpdVer( final short binaryValue, final int minHeaderSize ) {
			this.binaryValue   = binaryValue;
			this.minHeaderSize = minHeaderSize;
		}
		
		/**
		 * Returns the SMPD version specified by its binary value.
		 * @param binaryValue binary value to return the the SMPD version for
		 * @return the SMPD version specified by its binary value or <code>UNKNOWN</code> if the binary value is invalid/unknown
		 */
		public static SmpdVer fromBinaryValue( final short binaryValue ) {
			for ( final SmpdVer smpdVer : SmpdVer.values() )
				if ( smpdVer.binaryValue == binaryValue )
					return smpdVer;
			return UNKNOWN;
		}
	}
	
	/**
	 * Writes an integer number in the encoded form to the output.
	 * <p><i>The implementation only works if the absolute value of <code>n</code> is not greater than Integer.MAX_VALUE/2!</i></p>
	 * @param n      number to be encoded and written
	 * @param output output to write to
	 * @throws IOException if an I/O error occurs
	 */
	public static void writeEncodedValue( int n, final OutputStream output ) throws IOException {
		// The sign bit is added to n, therefore it only handles values having absolute value not greater than Integer.MAX_VALUE/2
		n = n > 0 ? n << 1 : ( ( -n ) << 1 ) | 0x01;
		
		do {
			if ( n < 0x80 ) {
				output.write( n );
				return;
			}
			else {
				output.write( ( n & 0x7f ) | 0x80 );
				n >>= 7;
			}
		} while ( true );
	}
	
	/**
	 * Reads an encoded value from the specified input and decodes it to an integer.
	 * <p><i>The implementation only works if the absolute value of the encoded value is not greater than Integer.MAX_VALUE/2!</i></p>
	 * @return the read value; or {@link Integer#MAX_VALUE} if end of stream reached and a value could not be read
	 * @throws EOFException if the input stream reaches the end before reading all bytes of the encoded value
	 * @throws IOException  if an I/O error occurs
	 */
	public static int readEncodedValue( final InputStream input ) throws EOFException, IOException {
		// The sign bit is taken out only at the end, therefore it only handles values having absolute value not greater than Integer.MAX_VALUE/2
		int data, value = 0;
		
		for ( int shift = 0; ; shift += 7 ) {
			if ( ( data = input.read() ) < 0 )
				if ( shift == 0 )
					return Integer.MAX_VALUE;
				else
					throw new EOFException();
			
			value |= ( data & 0x7f ) << shift;
			if ( ( data & 0x80 ) == 0 )
				return ( value & 0x01 ) > 0 ? -( value >> 1 ) : value >> 1;
		}
	}
	
	/**
	 * Returns a string representation of the version specified by its binary value.
	 * @param binaryValue binary value of the version whose string representation to be returned
	 * @return a string representation of the version specified by its binary value
	 */
	public static String getVersionString( final short binaryValue ) {
		return ( binaryValue >> 8 ) + "." + ( binaryValue & 0xff );
	}
	
}
