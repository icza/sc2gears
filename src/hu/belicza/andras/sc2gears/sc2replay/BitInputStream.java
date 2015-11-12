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

import java.nio.ByteBuffer;

/**
 * Bit input stream view of a {@link ByteBuffer}.
 * 
 * @author Andras Belicza
 */
public class BitInputStream {
	
	private static final int[] BITS_MASKS  = new int[] { 0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff };
	
	/** Reference to the underlying byte buffer. */
	private final ByteBuffer wrapper;
	
	/** Cache of the last read byte. */
	private int cache;
	/** Bits left in cache. */
	private int bitsLeftInCache;
	
	/**
	 * Creates a new BitBufferView.
	 * @param wrapper reference to the underlying byte buffer
	 */
	public BitInputStream( final ByteBuffer wrapper ) {
		this.wrapper = wrapper;
	}
	
	/**
	 * Reads the specified amount of bits and returns it as an int. 
	 * @param n number of bits to read
	 * @return the specified amount of bits as an int
	 */
	public int readBits( int n ) {
		int value = 0;
		
		while ( n > 0 ) {
			if ( bitsLeftInCache == 0 ) {
				cache           = wrapper.get() & 0xff;
				bitsLeftInCache = 8;
			}
			
			final int bitsToRead = bitsLeftInCache > n ? n : bitsLeftInCache;
			
			value = ( value << bitsToRead ) | ( cache & BITS_MASKS[ bitsToRead ] );
			
			if ( ( bitsLeftInCache -= bitsToRead ) > 0 ) // If nothing left in cache, no need to shift it (it will be overwritten before next read...)
				cache >>= bitsToRead;
			n -= bitsToRead;
		}
		
		return value;
	}
	
	/**
	 * Reads the next 8 bits and returns it as an int.
	 * @return the next 8 bits as an int
	 */
	public int read() {
		return readBits( 8 );
	}
	
	/**
	 * Reads the next 16 bits and returns it as a short.
	 * @return the next 16 bits as a short
	 */
	public short readShort() {
		return (short) readBits( 16 );
	}
	
	/**
	 * Reads the next 32 bits and returns it as an int.
	 * @return the next 32 bits as an int
	 */
	public int readInt() {
		return readBits( 32 );
	}
	
	/**
	 * Reads the next bit and returns it as a boolean (0 = false, 1 = true).
	 * @return the next bit as a boolean (0 = false, 1 = true)
	 */
	public boolean readBoolean() {
		return readBits( 1 ) == 1;
	}
	
	/**
	 * Clears the current byte, drops unread bits from it, so the next read
	 * will happen from the next byte. This is to byte align the bit input stream.
	 */
	public void clearByte() {
		bitsLeftInCache = 0;
	}
	
}
