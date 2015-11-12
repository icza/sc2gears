/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb;

import hu.belicza.andras.draw.BmpImage;
import hu.belicza.andras.sc2gearsdb.datastore.Smpd;
import hu.belicza.andras.sc2gearsdb.user.client.Permission;
import hu.belicza.andras.sc2gearsdb.util.JQBuilder;
import hu.belicza.andras.sc2gearsdb.util.PMF;
import hu.belicza.andras.sc2gearsdb.util.ServerUtils;
import hu.belicza.andras.smpd.SmpdUtil;
import hu.belicza.andras.smpd.SmpdUtil.SmpdVer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.InflaterInputStream;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tools.bzip2.CBZip2InputStream;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesService.OutputEncoding;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * A servlet that generates and serves mouse print preview images.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class SmpdImageServlet extends BaseServlet {
	
	private static final Logger LOGGER = Logger.getLogger( SmpdImageServlet.class.getName() );
	
	private enum DataCompression {
		/** No compression.      */
		NO_COMPRESSION,
		/** Deflate compression. */
		DEFLATE,
		/** BZip2 compression.   */
		BZIP2;
	}
	
	private static final int COLOR              = BmpImage.color(   0,   0,   0      );
	private static final int SHADOW_COLOR       = BmpImage.color( 255, 255, 255, 128 );
	private static final int POUR_INK_IDLE_TIME = 3000;   // ms
	private static final int IDLE_INK_FLOW_RATE = 200;    // pixel/sec
	private static final int MOUSE_WARMUP_TIME  = 50_000; // ms 
	
	
	/*
	 * doGet() is more common in case of this servlet, so doPost() calls doGet().
	 */
	@Override
	protected void doPost( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		doGet( request, response );
	}
	
    @Override
	protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		if (true) {
			response.sendError(HttpServletResponse.SC_GONE, "This URL is gone and will not be available anymore!");
			return;
		}
		
		final String sharedAccount = request.getParameter( "sharedAccount" );
		LOGGER.fine( sharedAccount == null ? "" : "Shared account: " + sharedAccount + ", " );
		
    	final UserService userService = UserServiceFactory.getUserService();
		final User        user        = userService.getCurrentUser();
		if ( user == null ) {
			LOGGER.warning( "Unauthorized access, not logged in!" );
			response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized access, you are not logged in!" );
			return;
		}
		
		String smpdSha1 = request.getPathInfo();
		if ( smpdSha1 != null && !smpdSha1.isEmpty() )
			smpdSha1 = smpdSha1.substring( 1 ); // Cut off leading slash
		
		if ( smpdSha1 == null ) {
			LOGGER.warning( "Missing mouse print SHA-1!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Missing mouse print SHA-1!" );
			return;
		}
		if ( smpdSha1.length() != 40 + 4 ) { // 40 chars sha-1 and 4 chars for extension: ".png"
			LOGGER.warning( "Invalid mouse print SHA-1!" );
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid mouse print SHA-1!" );
			return;
		}
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			final Key accountKey = ServerUtils.getAccountKey( pm, sharedAccount, user, Permission.VIEW_MOUSE_PRINTS );
			if ( accountKey == null ) {
				LOGGER.warning( "Unauthorized access!" );
				response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized access!" );
				return;
			}
			
			final List< Smpd > mousePrintList = new JQBuilder<>( pm, Smpd.class ).filter( "ownerk==p1 && sha1==p2", "KEY p1, String p2" ).get( accountKey, smpdSha1.substring( 0, 40 ) );
			if ( mousePrintList.isEmpty() ) {
				LOGGER.warning( "Unauthorized access!" );
				response.sendError( HttpServletResponse.SC_FORBIDDEN, "Unauthorized access!" );
				return;
			}
			
			byte[] content = null;
			try {
				content = ServerUtils.getFileContent( mousePrintList.get( 0 ), null );
				if ( content == null ) {
					LOGGER.warning( "File not found!" );
					response.sendError( HttpServletResponse.SC_NOT_FOUND, "File not found!" );
					return;
				}
			} catch ( final IOException ie ) {
				LOGGER.log( Level.SEVERE, "Some error occured getting the file from the Blobstore!", ie );
				response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Some error occured during generating mouse print preview image!" );
				return;
			}
			
			final BmpImage bmp;
			
			// Parse SMPD file and generate preview image
			try ( final DataInputStream dataInput = new DataInputStream( new ByteArrayInputStream( content ) ) ) {
				final byte[] magic = new byte[ 4 ];
				dataInput.read( magic );
				if ( !Arrays.equals( magic, SmpdUtil.SMPD_MAGIC ) )
					throw new Exception( "Invalid SMPD magic!" );
				
				final short   binaryVersion = dataInput.readShort();
				final SmpdVer smpdVer       = SmpdVer.fromBinaryValue( binaryVersion );
				if ( smpdVer == SmpdVer.UNKNOWN )
					throw new Exception( "Unsupported SMPD version: " + SmpdUtil.getVersionString( binaryVersion ) );
				
				final int headerLength = dataInput.readInt();
				if ( headerLength < smpdVer.minHeaderSize )
					throw new Exception( "Header length too small, should be at least " + smpdVer.minHeaderSize + "!" );
				
				dataInput.readLong(); // Start time
				dataInput.readLong(); // End time
				final int width  = dataInput.readInt();
				final int height = dataInput.readInt();
				bmp = new BmpImage( width >> 1, height >> 1 ); // ZOOM TO HALF
				bmp.clear( 0xff );
				dataInput.readInt(); // Screen resolution
				dataInput.readInt(); // Sampling time
				int samplesCount = dataInput.readInt();
				dataInput.readInt(); // Uncompressed data size
				final DataCompression savedWithCompression = DataCompression.values()[ dataInput.read() ];
				
				int extraHeaderBytes = 0;  // Extra header bytes over the SmpdVer.minHeaderSize
				
				// Version 1.1 additions
				if ( smpdVer.compareTo( SmpdVer.V11 ) <= 0 ) {
					final int appNameLength = dataInput.read();
					extraHeaderBytes += appNameLength;
					dataInput.skipBytes( appNameLength );
				}
				
				if ( headerLength != smpdVer.minHeaderSize + extraHeaderBytes ) // Skip custom header bytes
					dataInput.skipBytes( headerLength - ( smpdVer.minHeaderSize + extraHeaderBytes ) );
				
				// Samples
				InputStream inputStream = null;
				switch ( savedWithCompression ) {
				case NO_COMPRESSION :
					inputStream = dataInput;
					break;
				case DEFLATE :
					inputStream = new InflaterInputStream( dataInput );
					break;
				case BZIP2 :
					inputStream = new CBZip2InputStream( dataInput );
					break;
				}
				
				bmp.setColor( COLOR );
				
				// available() cannot be used here:
				// InflaterInputStream might return >0 even if there are no more, CBZip2InputStream might return 0 even if there are more...
				int x = 0, y = 0, lastx = 0, lasty = 0;
				int elapsedTime = 0;
				boolean first = true;
				while ( samplesCount-- > 0 ) {
					// InflaterInputStream might return available()>0 even if there are no more...
					final int dt = SmpdUtil.readEncodedValue( inputStream );
					if ( dt == Integer.MAX_VALUE )
						break;
					elapsedTime += dt;
					final int dx = SmpdUtil.readEncodedValue( inputStream );
					final int dy = SmpdUtil.readEncodedValue( inputStream );
					if ( first ) {
						first = false;
						x = dx; y = dy;
						lastx = x; lasty = y;
					}
					else {
						x += dx;
						y += dy;
						// handle sample
						// If mouse is idle, "pour ink" on the buffer based on dt
						if ( dt > POUR_INK_IDLE_TIME && elapsedTime > MOUSE_WARMUP_TIME ) {
							// Area is the number of pixels to pour: T = flow_rate * dt = r*r*PI = d*d*PI/4
							final int d = (int) Math.sqrt( 0.004 / Math.PI * ( dt - POUR_INK_IDLE_TIME ) * IDLE_INK_FLOW_RATE ); // flow rate is in pixel/sec, dt is in ms!
							bmp.setColor( SHADOW_COLOR );
							bmp.fillCircle( lastx >> 1, lasty >> 1, d >> 1 );
							bmp.setColor( COLOR );
							bmp.fillCircle( lastx >> 1, lasty >> 1, d >> 2 );
						}
						
						// Draw movement
						bmp.drawLine( lastx >> 1, lasty >> 1, x >> 1, y >> 1 );
						
						lastx = x; lasty = y;
					}
				}
				
			} catch ( final Exception e ) {
				e.printStackTrace();
				LOGGER.log( Level.SEVERE, "Failed to parse SMPD file!", e );
				response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Some error occured during generating mouse print preview image!" );
				return;
			}
			
			// Convert and serve image
			final ImagesService imagesService = ImagesServiceFactory.getImagesService();
			final Image  image       = ImagesServiceFactory.makeImage( bmp.bmpData );
			// Convert the BMP to PNG
			final Image  outputImage = imagesService.applyTransform( ImagesServiceFactory.makeRotate( 0 ), image, OutputEncoding.PNG );
			
			// Serve the image
			response.setContentType( "image/png" );
			response.setDateHeader( "Expires", new Date().getTime() + 60L*24*60*60*1000 ); // 60 days cache time
			response.addHeader( "Cache-Control", "private, max-age=5184000" ); // 5_184_000 sec = 60 days cache time
			response.getOutputStream().write( outputImage.getImageData() );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
    
}
