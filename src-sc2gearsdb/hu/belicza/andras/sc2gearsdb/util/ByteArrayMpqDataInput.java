/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.util;

import java.io.IOException;
import java.nio.ByteBuffer;

import hu.belicza.andras.mpq.MpqDataInput;

/**
 * {@link MpqDataInput} which gets data from a byte array.
 * 
 * @author Andras Belicza
 */
public class ByteArrayMpqDataInput implements MpqDataInput {
	
	/** The byte array to get data from. */
	private final byte[] content;
	
	/** Current position in the byte array. */
	private int position;
	
    /**
     * Creates a new ByteArrayMpqDataInput.
     * @param content byte array to get the data from
     */
    public ByteArrayMpqDataInput( final byte[] content ) {
    	this.content = content;
    }
	
	@Override
	public long read( final ByteBuffer destination ) throws IOException {
		final int count = destination.remaining();
		try {
			destination.put( content, position, count );
		} catch ( final Exception e ) {
			throw new IOException( e );
		}
		position += count;
		
		return count;
	}
	
	@Override
	public void position( final long newPosition ) throws IOException {
		position = (int) newPosition;
	}
	
	@Override
	public long position() throws IOException {
		return position;
	}
	
	@Override
	public void close() throws IOException {
		// Nothing to do
	}
	
}
