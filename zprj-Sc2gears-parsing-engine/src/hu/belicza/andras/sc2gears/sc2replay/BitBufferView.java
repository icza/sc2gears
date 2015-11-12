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
 * Bit buffer view of a {@link ByteBuffer}.
 * 
 * @author Andras Belicza
 */
public class BitBufferView {
	
	private static final int[] LOW_BIT_MASKS  = new int[] { 0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff };
	
	/** Reference to the underlying byte buffer. */
	private final ByteBuffer wrapper;
	/** Cache of the last read byte. */
	private int cache;
	
	/** The initial bits defined by byteBoundary. */
	public  final byte initialBits;
	
	private final int lowBitMask;
	private final int highBitMask;
	
	private final int byteBoundary;
	private final int byteBoundarySupplement;
	
	/**
	 * Creates a new BitBufferView.
	 * @param wrapper reference to the underlying byte buffer
	 * @param byteBoundary tells how many bits are shifted between bytes; must be between 0..8 (inclusive)
	 */
	public BitBufferView( final ByteBuffer wrapper, final int byteBoundary ) {
		this.wrapper                = wrapper;
		this.byteBoundary           = byteBoundary;
		this.byteBoundarySupplement = 8 - byteBoundary;
		
		lowBitMask  = LOW_BIT_MASKS[ byteBoundary ];
		highBitMask = 0xff - lowBitMask;
		
		cache = wrapper.get() & 0xff;
		
		initialBits = (byte) ( ( cache & lowBitMask ) << byteBoundarySupplement );
	}
	
	/**
	 * Returns the next 8 bits as an int.
	 * @return the next 8 bits as an int
	 */
	public int get() {
		final int retVal = cache & highBitMask;
		cache = wrapper.get() & 0xff;
		return retVal | ( cache & lowBitMask );
	}
	
	/**
	 * Returns the next 16 bits stored in a short.
	 * @return the next 16 bits stored in a short
	 */
	public short get2Bytes() {
		int retVal = ( ( cache & highBitMask ) << byteBoundarySupplement ) | ( wrapper.get() & 0xff );
		cache = wrapper.get() & 0xff;
		return (short) ( ( retVal << byteBoundary ) | ( cache & lowBitMask ) );
	}
	
	/**
	 * Returns the next 32 bits stored in an int.
	 * @return the next 32 bits stored in an int
	 */
	public int getInt() {
		int retVal = ( ( cache & highBitMask ) << byteBoundarySupplement ) | ( wrapper.get() & 0xff );
		retVal = ( retVal << 8 ) | ( wrapper.get() & 0xff );
		retVal = ( retVal << 8 ) | ( wrapper.get() & 0xff );
		cache = wrapper.get() & 0xff;
		return Integer.reverseBytes( ( retVal << byteBoundary ) | ( cache & lowBitMask ) );
	}
	
	/**
	 * Returns the remainder without reading another byte.<br>
	 * Returns the cache value holding the unused bits from the last read.
	 * @return the cache value holding the unused bits from the last read
	 */
	public int getRemainder() {
		return cache & highBitMask;
	}
	
}
