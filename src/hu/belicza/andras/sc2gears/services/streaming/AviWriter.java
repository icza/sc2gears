/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */

package hu.belicza.andras.sc2gears.services.streaming;

import hu.belicza.andras.sc2gears.Consts;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AVI writer.<br>
 * The video codec is MJPEG.
 * 
 * Info source: http://rsb.info.nih.gov/ij/developer/source/
 * 				http://rsb.info.nih.gov/ij/developer/source/ij/plugin/filter/AVI_Writer.java.html
 * 
 * @author Andras Belicza
 */
class AviWriter {
	
	/** File to write the AVI video to.                          */
	private final File             aviFile;
	/** Random access file to read and write the AVI file.       */
	private final RandomAccessFile raf;
	/** File to write temporary AVI index data.                  */
	private final File             indexFile;
	/** Random access file to read and write the AVI index data. */
	private final RandomAccessFile rafIdx;
	
	/** Saved length field positions. */
	private final List< Long > lengthFieldList = new ArrayList< Long >( 5 );
	
	/** Position of the frames count fields. */
	private final long framesCountFieldPos, framesCountFieldPos2;
	/** Position of the MOVI chunk.          */
	private final int  moviPos;
	
	/** Number of frames written to the AVI file. */
	private volatile int framesWritten;
	
	/**
	 * Creates a new AviWriter.<br>
	 * Opens the AVI file and writes the AVI header fields.
	 * @param aviFile file to write the AVI video to
	 * @param width   width of the video in pixels
	 * @param height  height of the video in pixels
	 * @param fps     video FPS
	 */
	public AviWriter( final File aviFile, final int width, final int height, final int fps ) throws IOException {
		this.aviFile = aviFile;
		indexFile    = new File( aviFile.getAbsolutePath() + ".idx_" );
		
		RandomAccessFile raf_ = null, rafIdx_ = null;
		
		try {
			
			raf_ = raf = new RandomAccessFile( aviFile, "rw" );
			raf.setLength( 0 );
			
			rafIdx_ = rafIdx = new RandomAccessFile( indexFile, "rw" );
			rafIdx.setLength( 0 );
			
			// Write AVI header
			writeString( "RIFF"         ); // RIFF type
			writeLengthField();            // File length (remaining bytes after this field) (nesting level 0)
			writeString( "AVI "         ); // AVI signature
			writeString( "LIST"         ); // LIST chunk: data encoding
			writeLengthField();            // Chunk length (nesting level 1)
			writeString( "hdrl"         ); // LIST chunk type
			writeString( "avih"         ); // avih sub-chunk
			writeInt   ( 0x38           ); // Sub-chunk length excluding the first 8 bytes of avih signature and size
			writeInt   ( 1000000/fps    ); // Frame delay time in microsec
			writeInt   ( 0              ); // dwMaxBytesPerSec (maximum data rate of the file in bytes per second)
			writeInt   ( 0              ); // Reserved
			writeInt   ( 0x10           ); // dwFlags, 0x10 bit: AVIF_HASINDEX (the AVI file has an index chunk at the end of the file - for good performance); Windows Media Player can't even play it if index is missing!
			framesCountFieldPos = raf.getFilePointer();
			writeInt   ( 0              ); // Number of frames
			writeInt   ( 0              ); // Initial frame for non-interleaved files; non interleaved files should set this to 0
			writeInt   ( 1              ); // Number of streams in the video; here 1 video, no audio
			writeInt   ( 0              ); // dwSuggestedBufferSize
			writeInt   ( width          ); // Image width in pixels
			writeInt   ( height         ); // Image height in pixels
			writeInt   ( 0              ); // Reserved
			writeInt   ( 0              );
			writeInt   ( 0              );
			writeInt   ( 0              );
			
			// Write stream information
			writeString( "LIST"         ); // LIST chunk: stream headers
			writeLengthField();            // Chunk size (nesting level 2)
			writeString( "strl"         ); // LIST chunk type: stream list
			writeString( "strh"         ); // Stream header
			writeInt   ( 56             ); // Length of the strh sub-chunk
			writeString( "vids"         ); // fccType - type of data stream - here 'vids' for video stream
			writeString( "MJPG"         ); // MJPG for Motion JPEG
			writeInt   ( 0              ); // dwFlags
			writeInt   ( 0              ); // wPriority, wLanguage
			writeInt   ( 0              ); // dwInitialFrames
			writeInt   ( 1              ); // dwScale
			writeInt   ( fps            ); // dwRate, Frame rate for video streams (the actual FPS is calculated by dividing this by dwScale)
			writeInt   ( 0              ); // usually zero
			framesCountFieldPos2 = raf.getFilePointer();
			writeInt   ( 0              ); // dwLength, playing time of AVI file as defined by scale and rate (set equal to the number of frames)
			writeInt   ( 0              ); // dwSuggestedBufferSize for reading the stream (typically, this contains a value corresponding to the largest chunk in a stream)
			writeInt   ( -1             ); // dwQuality, encoding quality given by an integer between (0 and 10,000.  If set to -1, drivers use the default quality value)
			writeInt   ( 0              ); // dwSampleSize, 0 means that each frame is in its own chunk
			writeShort ( (short) 0      ); // left of rcFrame if stream has a different size than dwWidth*dwHeight(unused)
			writeShort ( (short) 0      ); //   ..top
			writeShort ( (short) 0      ); //   ..right
			writeShort ( (short) 0      ); //   ..bottom
			// end of 'strh' chunk, stream format follows
			writeString( "strf"         ); // stream format chunk
			writeLengthField();            // Chunk size (nesting level 3)
			writeInt   ( 40             ); // biSize, write header size of BITMAPINFO header structure; applications should use this size to determine which BITMAPINFO header structure is being used, this size includes this biSize field
			writeInt   ( width          ); // biWidth, width in pixels
			writeInt   ( height         ); // biWidth, height in pixels (may be negative for uncompressed video to indicate vertical flip)
			writeShort ( (short) 1      ); // biPlanes, number of color planes in which the data is stored
			writeShort ( (short) 24     ); // biBitCount, number of bits per pixel #
			writeString( "MJPG"         ); // biCompression, type of compression used (uncompressed: NO_COMPRESSION=0)
			writeInt   ( width*height*3 ); // biSizeImage (buffer size for decompressed mage) may be 0 for uncompressed data
			writeInt   ( 0              ); // biXPelsPerMeter, horizontal resolution in pixels per meter
			writeInt   ( 0              ); // biYPelsPerMeter, vertical resolution in pixels per meter
			writeInt   ( 0              ); // biClrUsed (color table size; for 8-bit only)
			writeInt   ( 0              ); // biClrImportant, specifies that the first x colors of the color table (0: all the colors are important, or, rather, their relative importance has not been computed)
			finalizeLengthField();         //'strf' chunk finished (nesting level 3)
			
			writeString( "strn"         ); // Use 'strn' to provide a zero terminated text string describing the stream
			final DateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" );
			String streamName = "Recorded with " + Consts.APPLICATION_NAME + " " + Consts.APPLICATION_VERSION + " (" + Consts.URL_HOME_PAGE + ")"
				+ " at " + df.format( new Date() );
			// Stream name must be 0-terminated and stream name length (the length of the chunk) must be even
			if ( ( streamName.length() & 0x01 ) == 0 )
				streamName = streamName + " \0"; // padding space plus terminating 0
			else
				streamName = streamName + "\0";  // terminating 0
			final byte[] aviStreamName;
			aviStreamName = streamName.getBytes( Consts.UTF8 );
			writeInt ( aviStreamName.length ); // Length of the strn sub-CHUNK (must be even)
			raf.write( aviStreamName );
			finalizeLengthField();         // LIST 'strl' finished (nesting level 2)
			finalizeLengthField();         // LIST 'hdrl' finished (nesting level 1)
			
			writeString( "LIST"         ); // The second LIST chunk, which contains the actual data
			writeLengthField();            // Chunk length (nesting level 1)
			moviPos = (int) raf.getFilePointer();
			writeString( "movi"         ); // LIST chunk type: 'movi'
			
		} catch ( final IOException ie ) {
			if ( raf_ != null )
				try { raf_.close(); } catch ( final IOException ie2 ) {}
			aviFile.delete();
			
			if ( rafIdx_ != null )
				try { rafIdx_.close(); } catch ( final IOException ie2 ) {}
			indexFile.delete();
				
			throw ie;
		}
	}
	
	/**
	 * Writes a string to the file.
	 * @param s string to be written
	 */
	private void writeString( final String s ) throws IOException {
		raf.write( s.getBytes( Consts.UTF8 ) );
	}
	
	/**
	 * Writes an int to the file.
	 * @param i int to be written
	 */
	private void writeInt( final int i ) throws IOException {
		raf.write(   i         & 0xff );
		raf.write( ( i >>  8 ) & 0xff );
		raf.write( ( i >> 16 ) & 0xff );
		raf.write( ( i >> 24 ) & 0xff );
	}
	
	/**
	 * Writes an int to the index file.
	 * @param i int to be written
	 */
	private void writeIntToIdx( final int i ) throws IOException {
		rafIdx.write(   i         & 0xff );
		rafIdx.write( ( i >>  8 ) & 0xff );
		rafIdx.write( ( i >> 16 ) & 0xff );
		rafIdx.write( ( i >> 24 ) & 0xff );
	}
	
	/**
	 * Writes a short to the file.
	 * @param i short to be written
	 */
	private void writeShort( final short i ) throws IOException {
		raf.write(   i         & 0xff );
		raf.write( ( i >>  8 ) & 0xff );
	}
	
	/**
	 * Saves the current file pointer and writes an int length field to the output to be written later on when the actual size is known.
	 */
	private void writeLengthField() throws IOException {
		lengthFieldList.add( raf.getFilePointer() );
		writeInt( 0 ); // Write 0 to reserve space for the length field (we will fill it later when we know its actual size)
	}
	
	/**
	 * Finalizes the last length field.
	 */
	private void finalizeLengthField() throws IOException {
		final long position = raf.getFilePointer();
		
		raf.seek( lengthFieldList.remove( lengthFieldList.size() - 1 ) );
		writeInt( (int) ( position - raf.getFilePointer() - 4 ) );
		
		// Seek "back" but align to a 2-byte boundary
		raf.seek( ( position & 0x01 ) == 0 ? position : position + 1 );
	}
	
	/**
	 * Writes a frame to the AVI stream.
	 * @param jpegData the frame in JPG encoded format
	 */
	public void writeFrame( final byte[] jpegData ) throws IOException {
		final long framePosLong = raf.getFilePointer();
		// Pointers in AVI are 32 bit. Do not write beyond that else the whole AVI file will be corrupted (not playable).
		// Index entry size: 16 bytes (for each frame)
		if ( framePosLong + jpegData.length + ( framesWritten << 4 ) > 4294000000L ) // 2^32 = 4 294 967 296
			return;
		
		final int framePos = (int) framePosLong;
		
		framesWritten++;
		
		writeInt   ( 0x63643030     ); // "00dc" compressed frame
		writeLengthField();            // Chunk length (nesting level 2)
		raf.write( jpegData );
		finalizeLengthField();         // "00dc" chunk finished (nesting level 2)
		
		// Write index data
		writeIntToIdx( 0x63643030    ); // "00dc" compressed frame
		writeIntToIdx( 0x10          ); // flags: select AVIIF_KEYFRAME (The flag indicates key frames in the video sequence. Key frames do not need previous video information to be decompressed.)
		writeIntToIdx( framePos - moviPos ); // offset to the chunk, offset can be relative to file start or 'movi'
		writeIntToIdx( jpegData.length    ); // length of the chunk
	}
	
	/**
	 * Returns the current size of the AVI file.
	 * @return the current size of the AVI file; or -1 if some error occurs
	 */
	public long getAviFileSize() {
		return aviFile.length();
	}
	
	/**
	 * Closes the AVI writer.<br>
	 * Finalizes the AVI file and closes all resources allocated to write the AVI file.
	 */
	public void close() throws IOException {
		try {
			
			finalizeLengthField();         // LIST 'movi' finished (nesting level 1)
			
			// Write index
			writeString( "idx1"         ); // idx1 chunk
			final int idxLength = (int) rafIdx.length();
			writeInt   ( idxLength      ); // Chunk length (we know its size, no need to use writeLengthField() and finalizeLengthField() pair)
			// Copy temporary index data
			rafIdx.seek( 0 );
			final byte[] buffer = new byte[ 1024 ];
			int bytesRead;
			while ( ( bytesRead = rafIdx.read( buffer ) ) != -1 )
				raf.write( buffer, 0, bytesRead );
			
			raf.seek( framesCountFieldPos );
			writeInt( framesWritten );
			raf.seek( framesCountFieldPos2 );
			writeInt( framesWritten );
			
			finalizeLengthField();         // 'RIFF' File finished (nesting level 0)
			
		} catch ( final IOException ie ) {
			throw ie;
		} finally {
			try { rafIdx.close(); } catch ( final IOException ie ) {}
			indexFile.delete();
			
			try { raf.close(); } catch ( final IOException ie ) {}
		}
		
	}
	
}
