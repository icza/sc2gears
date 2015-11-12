/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.sc2replay;

import hu.belicza.andras.sc2gears.Consts;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A parser utility class for Blizzard's SC2 binary data format.
 * 
 * <p>External links:
 * <ul>
 * 		<li><a href="https://github.com/GraylinKim/sc2reader/blob/master/sc2reader/utils.py#L264">https://github.com/GraylinKim/sc2reader/blob/master/sc2reader/utils.py#L264</a>
 * 		<li><a href="https://github.com/GraylinKim/sc2reader/wiki/Serialized-Data">https://github.com/GraylinKim/sc2reader/wiki/Serialized-Data</a>
 * 		<li><a href="http://www.teamliquid.net/forum/viewmessage.php?topic_id=117260&currentpage=3#45">http://www.teamliquid.net/forum/viewmessage.php?topic_id=117260&amp;currentpage=3#45</a>
 * </ul></p>
 * 
 * @author Andras Belicza
 */
class Parser {
	
	/** Source of the I/O operations. */
	protected ByteBuffer wrapper;
	
	/**
	 * Sets the wrapper of this parser.
	 * @param data source of the wrapper
	 */
	protected void setWrapper( final byte[] data, final ByteOrder byteOrder ) {
		wrapper = ByteBuffer.wrap( data ).order( byteOrder );
	}
	
	/**
	 * Reads a value from the wrapper.<br>
	 * The value can be "any" bytes. As long as the leftmost bit is 1, there are more bytes.
	 * The first byte is divided by 2 (rightmost bit is reserved for sign).
	 * <p><i>The implementation only works if the absolute value of the encoded value is not greater than Integer.MAX_VALUE/2!</i></p>
	 * @return the read value
	 * @see {@link #readLongValueStrut()}
	 * @see {@link #readOptimizedValueStrut()}
	 */
	protected int readValueStrut() {
		// The sign bit is taken out only at the end, therefore it only handles values having absolute value not greater than Integer.MAX_VALUE/2
		int data, value = 0;
		for ( int shift = 0; ; shift += 7 ) {
			data   = wrapper.get() & 0xff;
			value |= ( data & 0x7f ) << shift;
			if ( ( data & 0x80 ) == 0 )
				return ( value & 0x01 ) > 0 ? -( value >> 1 ) : value >> 1;
		}
	}
	
	/**
	 * Reads a long value from the wrapper.<br>
	 * The value can be "any" bytes. As long as the leftmost bit is 1, there are more bytes.
	 * The first byte is divided by 2 (rightmost bit is reserved).
	 * <p><i>The implementation only works if the absolute value of the encoded value is not greater than Long.MAX_VALUE/2!</i></p>
	 * @return the read value
	 * @see {@link #readValueStrut()}
	 * @see {@link #readOptimizedValueStrut()}
	 */
	protected long readLongValueStrut() {
		// The sign bit is taken out only at the end, therefore it only handles values having absolute value not greater than Long.MAX_VALUE/2
		long data, value = 0;
		for ( int shift = 0; ; shift += 7 ) {
			data   = wrapper.get() & 0xff;
			value |= ( data & 0x7f ) << shift;
			if ( ( data & 0x80 ) == 0 )
				return ( value & 0x01 ) > 0 ? -( value >> 1 ) : value >> 1;
		}
	}
	
	/**
	 * Reads a value from the wrapper.<br>
	 * The value can be "any" bytes. As long as the leftmost bit is 1, there are more bytes.
	 * The first byte is divided by 2 (rightmost bit is reserved).
	 * 
	 * <p>The implementation first tries to read the value as an int. If the number of bytes is greater than 3, reading it will be continued as a long.</p>
	 * 
	 * <p><i>The implementation only works if the absolute value of the encoded value is not greater than Long.MAX_VALUE/2!</i></p>
	 * @return the read value
	 * @see {@link #readValueStrut()}
	 * @see {@link #readLongValueStrut()}
	 */
	protected Number readOptimizedValueStrut() {
		// The sign bit is taken out only at the end, therefore it only handles values having absolute value not greater than Long.MAX_VALUE/2
		// First try to read it as int
		int data, value = 0;
		for ( int shift = 0; ; shift += 7 ) {
			data   = wrapper.get() & 0xff;
			value |= ( data & 0x7f ) << shift;
			if ( ( data & 0x80 ) == 0 )
				return ( value & 0x01 ) > 0 ? -( value >> 1 ) : value >> 1;
			else if ( shift == 14 ) {
				
				// Value is 4 bytes at least, continue reading as long
				long data2, value2 = value;
				for ( shift = shift + 7; ; shift += 7 ) {
					data2   = wrapper.get() & 0xff;
					value2 |= ( data2 & 0x7f ) << shift;
					if ( ( data2 & 0x80 ) == 0 )
						return ( value2 & 0x01 ) > 0 ? -( value2 >> 1 ) : value2 >> 1;
				}
				
			}
		}
	}
	
	/**
	 * Reads a whole structure.
	 * @return the read structure
	 */
	protected Object readStructure() {
		switch ( wrapper.get() ) {
		case 0x00 : {
			// An array. 00 is followed by the number of elements in the array, and elements follow.
			// Each element is a data type marker followed by data, all in same format.
			final Object[] data = new Object[ readValueStrut() ];
			for ( int i = 0; i < data.length; i++ )
				data[ i ] = readStructure();
			return data;
		}
		case 0x02 : {
			// Binary data. 02 is followed by size of binary data in VLF after that comes data itself;
			// in most cases it's text in utf8. 02 12 41 55 54 4F 4D 41 54 49 43 is a string "AUTOMATIC".
			final byte[] data = new byte[ readValueStrut() ];
			wrapper.get( data );
			return data;
		}
		case 0x03 : {
			wrapper.get(); // Unknown value
			return readStructure();
		}
		case 0x04 : {
			// First byte tells if there is a structure here or not:
			if ( wrapper.get() == 0 )
				return 0;
			else
				return readStructure();
		}
		case 0x05 : {
			// A Map (key-value pairs). Followed by number in VLF which indicates how many pairs will follow. 
			// A pair consists of array element index in VLF and element itself (data type marker followed by data).
			final Object[] data = new Object[ readValueStrut() ];
			for ( int i = 0; i < data.length; i++ ) {
				// Key might have some special meaning, maybe should be build a java.util.Map...
				// This is certainly not the index, its values are: 0, 1, 2, 4 (instead of 3!)...
				readValueStrut();
				data[ i ] = readStructure();
			}
			return data;
		}
		case 0x06 :
			// A number. Its value follows in one byte.
			return wrapper.get() & 0xff;
		case 0x07 :
			// A number. Its value follows in four bytes.
			return wrapper.getInt();
		case 0x09 :
			// A number. Its value follows in VLF.
			return readOptimizedValueStrut();
		}
		return null;
	}
	
	/**
	 * Prints a structure read by {@link #readStructure()} to the standard output.  
	 * @param data the structure to print
	 * @param path path to the data object; should be empty string at the top level
	 */
	public static void printStructure( final Object data, final String path ) {
		if ( data instanceof Number ) {
			
			System.out.println( path + " " + data.getClass().getSimpleName() + ": " + data );
			
		}
		else if ( data instanceof byte[] ) {
			
			System.out.print( path + " \"" + new String( (byte[]) data, Consts.UTF8 ).replace( "\r", "\\r" ).replace( "\n", "\\n" ) + "\"  Hex: " );
			for ( final byte b : (byte[]) data )
				System.out.printf( "%02x ", b & 0xff );
			System.out.println();
			
		}
		else if ( data instanceof Object[] ) {
			
			final Object[] dataArray = (Object[]) data;
			for ( int i = 0; i < dataArray.length; i++ )
				printStructure( dataArray[ i ], String.format( "%s%3d|", path, i ) );
			
		}
	}
	
	/**
	 * Converts a byte array to a String using UTF-8 encoding.
	 * @param input byte array input to be interpreted as string
	 * @return the converted string
	 */
	public static String byteArrToString( final Object input ) {
		return new String( (byte[]) input, Consts.UTF8 );
	}
	
	/**
	 * Reads a string from the wrapper.<br>
	 * First reads a byte which is the number of characters, then reads that amount bytes (1 byte=1 character).
	 * @return the read string
	 */
	protected String readString() {
		final byte[] buffer = new byte[ wrapper.get() & 0xff ];
		wrapper.get( buffer );
		return new String( buffer, Consts.UTF8 );
	}
	
	/**
	 * Reads a String with the specified length from the wrapper.<br>
	 * @param length  length of the string to be read
	 * @return the read string
	 */
	protected String readStringWithLength( final int length ) {
		final byte[] buffer = new byte[ length ];
		wrapper.get( buffer );
		return new String( buffer, Consts.UTF8 );
	}
	
	/**
	 * Positions the wrapper after the specified pattern.
	 * @param pattern pattern to search
	 * @return true if pattern was found and positioned after; false if end of buffer reached without finding the pattern
	 */
	protected boolean positionAfter( final byte[] pattern ) {
		final byte[] data = wrapper.array();
		int position = wrapper.position();
		
		int i;
		final int maxPos = data.length - pattern.length;
		while ( position <= maxPos ) {
			for ( i = 0; i < pattern.length; i++ )
				if ( data[ position + i ] != pattern[ i ] )
					break;
			if ( i == pattern.length )
				break;
			position++;
		}
		
		if ( position <= data.length - pattern.length ) {
			wrapper.position( position + pattern.length );
			return true;
		}
		else
			return false;
	}
	
}
