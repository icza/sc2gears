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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 * A file-persistent map-like utility.
 * 
 * <p>The implementation is thread-safe.<br>
 * The implementation is also close-state-tolerant: every method can be called after a {@link PersistentMap#close()},
 * but they will not return any valid result.</p>
 * 
 * @author Andras Belicza
 */
public class PersistentMap {
	
	/** Index file. */
	private final RandomAccessFile indexFile;
	/** Data file.  */
	private final RandomAccessFile dataFile;   
	
	/** The version of the data stored in the persistent map. */
	private final short version;
	
	/**
	 * Info about a value in the persistent map.
	 * @author Andras Belicza
	 */
	private static class ValueInfo {
		public int position;
		public int size;
	}
	
	/** Index map: map of the persistent map keys and value info set. */
	private final Map< String, ValueInfo > indexMap = new HashMap< String, ValueInfo >();
	
	/** Tells if the persistent map has been closed. */
	private boolean closed;
	
	/**
	 * Creates a new PersistentMap.
	 * @param rootFolder root folder to read/write files to
	 * @param version    tells the version of the data stored in the persistent map; if it does not equal to the version of the persistent file, it will be cleared automatically
	 * @throws IOException if the persistent map could not be initialized
	 */
	public PersistentMap( final File rootFolder, final short version ) throws IOException {
		indexFile = new RandomAccessFile( new File( rootFolder, "index" ), "rw" );
		if ( indexFile.getChannel().tryLock() == null )
			throw new IOException( "Index file is already in use!" );
		
		dataFile = new RandomAccessFile( new File( rootFolder, "data"  ), "rw" );
		if ( dataFile.getChannel().tryLock() == null )
			throw new IOException( "Data file is already in use!" );
		
		this.version = version;
		final long indexSize = indexFile.length();
		if ( indexSize == 0 || indexFile.readShort() != version ) // New file or old version?
			clear();
		else {
			// Read the index file into memory
			String    key;
			ValueInfo valueInfo;
			while ( indexFile.getFilePointer() < indexSize ) {
				key = indexFile.readUTF();
				valueInfo = new ValueInfo();
				valueInfo.position = indexFile.readInt();
				valueInfo.size     = indexFile.readInt();
				indexMap.put( key, valueInfo );
			}
		}
	}
	
	/**
	 * Puts a new entry into the persistent map.
	 * @param key   key of the new entry
	 * @param value value of the new entry
	 */
	public synchronized void put( final String key, final byte[] value ) {
		if ( closed )
			return;
		
		if ( !indexMap.containsKey( key ) ) {
			try {
				final ValueInfo valueInfo = new ValueInfo();
				valueInfo.position = (int) dataFile.length();
				valueInfo.size     = value.length;
				
				dataFile.setLength( valueInfo.position + value.length );
				dataFile.seek( valueInfo.position );
				dataFile.write( value );
				
				indexFile.writeUTF( key                );
				indexFile.writeInt( valueInfo.position );
				indexFile.writeInt( valueInfo.size     );
				
				indexMap.put( key, valueInfo );
			} catch ( final IOException ie ) {
				ie.printStackTrace();
			}
		}
	}
	
	/**
	 * Reads a value from the persistent map.
	 * @param key key whose value pair to be read
	 * @return the value associated with the specified key; or <code>null</code> if there is no value associated with the specified key
	 */
	public synchronized byte[] get( final String key ) {
		if ( closed )
			return null;
		
		final ValueInfo valueInfo = indexMap.get( key );
		
		if ( valueInfo != null )
			try {
				dataFile.seek( valueInfo.position );
				final byte[] value = new byte[ valueInfo.size ];
				dataFile.readFully( value );
				return value;
			} catch ( final IOException ie ) {
				ie.printStackTrace();
			}
		
		return null;
	}
	
	/**
	 * Clears the persistent map.
	 */
	public synchronized void clear() {
		if ( closed )
			return;
		
		try {
			indexFile.setLength( 0L );
			indexFile.writeShort( version );
			dataFile.setLength( 0L );
			indexMap.clear();
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		}
	}
	
	/**
	 * Closes the persistent map.
	 */
	public synchronized void close() {
		closed = true;
		
		try {
			indexFile.close();
		} catch ( final IOException ie ) {}
		try {
			dataFile.close();
		} catch ( final IOException ie ) {}
	}
	
	/**
	 * Tells if the persistent map has been closed.
	 * @return true if the persistent map has been closed; false otherwise
	 */
	public synchronized boolean isClosed() {
		return closed;
	}
	
}
