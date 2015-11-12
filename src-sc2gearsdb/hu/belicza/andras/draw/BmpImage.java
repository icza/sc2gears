package hu.belicza.andras.draw;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Represents a true color (3-byte-pixel) image on which drawing is supported.
 * 
 * <p><b>About alpha channel:</b><br>
 * <br>
 * Drawing a pixel with alpha channel (0..1 range) results in a new pixel of (<a href="http://en.wikipedia.org/wiki/Alpha_compositing">alpha-blending</a>):<br>
 * <blockquote><pre>outRGB = colorRGB*colorAlpha + backgrRGB*(1-colorAlpha)</pre></blockquote>
 * 
 * Using <code>alphaMul = 1-colorAlpha</code> and pre-multiplied color (<code>colorRGB</code>): 
 * <blockquote><pre>outRGB = colorRGBPre + backgrRGB*alphaMul</pre></blockquote>
 * 
 * Alpha range of 0..255 would result in alphaMul of range 255..0
 * requiring to multiply with alphaMul AND divide by 255.
 * For performance boost, I add 1 to it, so I can multiply with
 * alphaMul and divide by 256 (using bit right shift of 8). 
 * </p>
 * 
 * @author Andras Belicza
 */
public class BmpImage {
	
	public static final int BMP_IMAGE_DATA_OFFSET = 54;
	
	/** Width of the image, in pixels.  */
	public final int width;
	/** Height of the image, in pixels. */
	public final int height;
	
	/** Max x coordinate. */
	public final int maxX;
	/** Max y coordinate. */
	public final int maxY;
	
	// Image data is stored in BMP format
	
	/** Size of a line in bytes, including the padding (4-byte align). */
	public final int    bmpLineSize; 
	/** Byte array holding the BMP image. */
	public final byte[] bmpData;
	
	/**
	 * Creates a new image
	 * @param width  width of the image, in pixels
	 * @param height height of the image, in pixels
	 */
	public BmpImage( final int width, final int height ) {
		if ( width < 1 )
			throw new IllegalArgumentException( "Width must be positive!" );
		if ( height < 1 )
			throw new IllegalArgumentException( "Height must be positive!" );
		
		this.width  = width;
		this.height = height;
		
		maxX = width  - 1;
		maxY = height - 1;
		
		final ByteBuffer bmpBuffer;
		
		// In BMP lines are aligned to 4 bytes
		bmpLineSize = ( width * 3 ) % 4 == 0 ? width * 3 : width * 3 + 4 - ( width * 3 ) % 4;
		bmpData     = new byte[ BMP_IMAGE_DATA_OFFSET + bmpLineSize * height ];
		bmpBuffer   = ByteBuffer.wrap( bmpData ).order( ByteOrder.LITTLE_ENDIAN );
		
		// BMP header (14 bytes)
		bmpBuffer.put( (byte) 0x42 );              // 'B'
		bmpBuffer.put( (byte) 0x4d );              // 'M'
		bmpBuffer.putInt( bmpData.length );        // Complete file (data) size
		bmpBuffer.putInt( 0 );                     // Reserved
		bmpBuffer.putInt( BMP_IMAGE_DATA_OFFSET ); // Offset to the image data bytes
		// BMP image info (40 bytes)
		bmpBuffer.putInt( 40 );                    // Image info header size (this section)
		bmpBuffer.putInt( width );                 // Obvious...
		bmpBuffer.putInt( height );                // Obvious...
		bmpBuffer.putShort( (short) 1 );           // Number of color planes
		bmpBuffer.putShort( (short) 24 );          // Number of bits per pixel
		bmpBuffer.putInt( 0 );                     // Compression (0 => no compression)
		bmpBuffer.putInt( bmpLineSize * height );  // Image data size (this is equal to bmpData.length - BMP_IMAGE_DATA_OFFSET)
		bmpBuffer.putInt( 0 );                     // x resolution (pixels/meter)
		bmpBuffer.putInt( 0 );                     // y resolution (pixels/meter)
		bmpBuffer.putInt( 0 );                     // Number of colors
		bmpBuffer.putInt( 0 );                     // Important colors
	}
	
	/**
	 * Assembles a color from RGB components specified in the range of 0..255.
	 * The color will be opaque.
	 * @param red   red   component in the range of 0..255
	 * @param green green component in the range of 0..255
	 * @param blue  blue  component in the range of 0..255
	 * @return the opaque RGB color
	 */
	public static int color( final int red, final int green, final int blue ) {
		return 0xff000000 | ( red << 16 ) | ( green << 8 ) | blue;
	}
	
	/**
	 * Assembles a color from RGB components specified in the range of 0..255
	 * and with be pre-multiplied with the specified alpha.
	 * 
	 * @param red   red   component in the range of 0..255
	 * @param green green component in the range of 0..255
	 * @param blue  blue  component in the range of 0..255
	 * @param alpha alpha component in the range of 0..255; 0 means transparent, 255 means opaque 
	 * @return the pre-multiplied RGBA color
	 */
	public static int color( final int red, final int green, final int blue, int alpha ) {
		alpha++;
		
		return    ( ( alpha - 1 ) << 24 )
				| ( ( red   * alpha & 0xff00 ) << 8 )
				|   ( green * alpha & 0xff00 )
				|   ( blue  * alpha >> 8 );
	}
	
	
	
	
	
	/** Red component of current drawing color. Pre-multiplied.          */
	private byte    blue;
	/** Green component of current drawing color. Pre-multiplied.        */
	private byte    green;
	/** Blue component of current drawing color. Pre-multiplied.         */
	private byte    red;
	/** Red component of current drawing color as int. Pre-multiplied.   */
	private int     blueInt;
	/** Green component of current drawing color as int. Pre-multiplied. */
	private int     greenInt;
	/** Blue component of current drawing color as int. Pre-multiplied.  */
	private int     redInt;
	/** Tells if there is no drawing alpha.                              */
	private boolean noAlpha;
	/** Multiplier of current drawing alpha (256-alpha).                 */
	private int     alphaMul;
	
	/**
	 * Sets the drawing color.
	 * @param color drawing color to be set
	 */
	public void setColor( final int color ) {
		blue  = (byte)   color;
		green = (byte) ( color >>  8 );
		red   = (byte) ( color >> 16 );
		
		noAlpha = ( color & 0xff000000 ) == 0xff000000;
		
		if ( !noAlpha ) {
			// These are only used if there is alpha, no need to set them otherwise
			alphaMul = 256 - ( color >>> 24 );
			blueInt  = blue  & 0xff; // byte => int conversion
			greenInt = green & 0xff; // byte => int conversion
			redInt   = red   & 0xff; // byte => int conversion
		}
	}
	
	
	/**
	 * Returns the byte position of the specified pixel.
	 * @param x x coordinate of the pixel whose byte position to return
	 * @param y y coordinate of the pixel whose byte position to return
	 * @return the byte position of the specified pixel
	 */
	public int pos( final int x, final int y ) {
		return BMP_IMAGE_DATA_OFFSET + ( maxY - y ) * bmpLineSize + x * 3;
	}
	
	/**
	 * Clears the whole image.
	 * 
	 * <p>Brightness of 255 will fill the whole image white,<br>
	 * Brightness of 128 will fill the whole image gray,<br>
	 * and a value of 0 will fill the whole image black.</p>
	 * 
	 * @param brightness brightness to use when clearing, in the range of 0..255
	 */
	public void clear( final int brightness ) {
		Arrays.fill( bmpData, BMP_IMAGE_DATA_OFFSET, BMP_IMAGE_DATA_OFFSET + bmpLineSize * height, (byte) brightness );
	}
	
	/**
	 * Draws a pixel at the specified coordinates with the specified color.
	 * @param x x coordinate of the pixel
	 * @param y y coordinate of the pixel
	 */
	public void drawPixel( final int x, final int y ) {
		if ( x < 0 || y < 0 || x > maxX || y > maxY )
			return;
		
		final int pos = pos( x, y );
		
		if ( noAlpha ) {
			bmpData[ pos     ] = blue;
			bmpData[ pos + 1 ] = green;
			bmpData[ pos + 2 ] = red;
		}
		else {
			bmpData[ pos     ] = (byte) ( blueInt  + ( ( bmpData[ pos     ] & 0xff ) * alphaMul >> 8 ) );
			bmpData[ pos + 1 ] = (byte) ( greenInt + ( ( bmpData[ pos + 1 ] & 0xff ) * alphaMul >> 8 ) );
			bmpData[ pos + 2 ] = (byte) ( redInt   + ( ( bmpData[ pos + 2 ] & 0xff ) * alphaMul >> 8 ) );
		}
	}
	
	/**
	 * Draws a horizontal line.
	 * @param x1 x coordinate of the first pixel
	 * @param y1 y coordinate of the first pixel
	 * @param x2 x coordinate of the last pixel
	 */
	public void drawHLine( int x1, int y1, int x2 ) {
		if ( y1 < 0 || y1 > maxY ) // Outside of image
			return;
		
		if ( x2 < x1 ) {
			final int temp = x1; x1 = x2; x2 = temp; // swap x1 and x2
		}
		
		if ( x2 < 0 || x1 > maxX ) // Outside of image
			return;
		
		// Clip
		if ( x1 < 0 )
			x1 = 0;
		if ( x2 > maxX )
			x2 = maxX;
		
		// Fill the line:
		final byte[] bmpData = this.bmpData; // Local copy for performance
		if ( noAlpha ) {
			final byte blue = this.blue, green = this.green, red = this.red; // Local copies for performance
			for ( int pos = pos( x1, y1 ), i = x2 - x1; i >= 0; i-- ) {
				bmpData[ pos++ ] = blue;
				bmpData[ pos++ ] = green;
				bmpData[ pos++ ] = red;
			}
		}
		else {
			final int blueInt = this.blueInt, greenInt = this.greenInt, redInt = this.redInt, alphaMul = this.alphaMul; // Local copies for performance
			for ( int pos = pos( x1, y1 ), i = x2 - x1; i >= 0; i-- ) {
				bmpData[ pos ] = (byte) ( blueInt  + ( ( bmpData[ pos ] & 0xff ) * alphaMul >> 8 ) ); pos++;
				bmpData[ pos ] = (byte) ( greenInt + ( ( bmpData[ pos ] & 0xff ) * alphaMul >> 8 ) ); pos++;
				bmpData[ pos ] = (byte) ( redInt   + ( ( bmpData[ pos ] & 0xff ) * alphaMul >> 8 ) ); pos++;
			}
		}
	}
	
	/**
	 * Draws a vertical line.
	 * @param x1 x coordinate of the first pixel
	 * @param y1 y coordinate of the first pixel
	 * @param y2 y coordinate of the last pixel
	 */
	public void drawVLine( int x1, int y1, int y2 ) {
		if ( x1 < 0 || x1 > maxX ) // Outside of image
			return;
		
		if ( y2 < y1 ) {
			final int temp = y1; y1 = y2; y2 = temp; // swap y1 and y2
		}
		
		if ( y2 < 0 || y1 > maxY ) // Outside of image
			return;
		
		// Clip
		if ( y1 < 0 )
			y1 = 0;
		if ( y2 > maxY )
			y2 = maxY;
		
		// BMP stores image upside-down
		final byte[] bmpData = this.bmpData; // Local copy for performance
		if ( noAlpha ) {
			final byte blue = this.blue, green = this.green, red = this.red; // Local copies for performance
			for ( int pos = pos( x1, y2 ), i = y2 - y1; i >= 0; i--, pos += bmpLineSize ) {
				bmpData[ pos     ] = blue;
				bmpData[ pos + 1 ] = green;
				bmpData[ pos + 2 ] = red;
			}
		}
		else {
			final int blueInt = this.blueInt, greenInt = this.greenInt, redInt = this.redInt, alphaMul = this.alphaMul; // Local copies for performance
			for ( int pos = pos( x1, y2 ), i = y2 - y1; i >= 0; i--, pos += bmpLineSize ) {
				bmpData[ pos     ] = (byte) ( blueInt  + ( ( bmpData[ pos     ] & 0xff ) * alphaMul >> 8 ) );
				bmpData[ pos + 1 ] = (byte) ( greenInt + ( ( bmpData[ pos + 1 ] & 0xff ) * alphaMul >> 8 ) );
				bmpData[ pos + 2 ] = (byte) ( redInt   + ( ( bmpData[ pos + 2 ] & 0xff ) * alphaMul >> 8 ) );
			}
		}
	}
	
	/**
	 * Draws a line.
	 * @param x1 x coordinate of the first pixel
	 * @param y1 y coordinate of the first pixel
	 * @param x2 x coordinate of the last pixel
	 * @param y2 y coordinate of the last pixel
	 */
	public void drawLine( int x1, int y1, int x2, int y2 ) {
		// Special cases:
		if ( y1 == y2 ) {
			drawHLine( x1, y1, x2 );
			return;
		} else if ( x1 == x2 ) {
			drawVLine( x1, y1, y2 );
			return;
		}
		
		// Clip is done by using drawPixel()
		
		// Bresenham's line algorithm
		// http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
		final boolean steep = Math.abs( y2 - y1 ) > Math.abs( x2 - x1 );
		if ( steep ) {
			int temp;
			temp = x1; x1 = y1; y1 = temp; // swap x1 and y1
			temp = x2; x2 = y2; y2 = temp; // swap x2 and y2
		}
		if ( x1 > x2 ) {
			int temp;
			temp = x1; x1 = x2; x2 = temp; // swap x1 and x2
			temp = y1; y1 = y2; y2 = temp; // swap y1 and y2
		}
		
		final int dx = x2 - x1;
		final int dy = Math.abs( y2 - y1 );
		final int ystep = y1 < y2 ? 1 : -1;
		int err = dx >> 1;
		int y = y1;
		
		for ( int x = x1; x <= x2; x++ ) {
			if ( steep )
				drawPixel( y, x );
			else
				drawPixel( x, y );
			if ( ( err -= dy ) < 0 ) {
				y += ystep;
				err += dx;
			}
		}
	}
	
	/**
	 * Fills a rectangle.
	 * @param x x coordinate of the top left corner of the rectangle
	 * @param y y coordinate of the top left corner of the rectangle
	 * @param w width of the rectangle
	 * @param h height of the rectangle
	 */
	public void fillRect( int x, int y, final int w, final int h ) {
		if ( x > maxX || y > maxY || w <= 0 || h <= 0 ) // Outside of image or no real dimension
			return;
		
		int x2 = x + w - 1;
		int y2 = y + h - 1;
		
		if ( x2 < 0 || y2 < 0 ) // Outside of image
			return;
		
		// Clip
		if ( x < 0 )
			x = 0;
		if ( y < 0 )
			y = 0;
		if ( x2 > maxX )
			x2 = maxX;
		if ( y2 > maxY )
			y2 = maxY;
		
		if ( x2 < 0 || y2 < 0 ) // Outside of image
			return;
		
		// BMP stores image upside-down
		if ( noAlpha ) {
			final int firstLinePos = pos( x, y2 );
			int linePos = firstLinePos;
			
			final byte[] bmpData = this.bmpData; // Local copy for performance
			
			// Fill the first line:
			final byte blue = this.blue, green = this.green, red = this.red; // Local copies for performance
			for ( int pos = linePos, i = x2 - x; i >= 0; i-- ) {
				bmpData[ pos++ ] = blue;
				bmpData[ pos++ ] = green;
				bmpData[ pos++ ] = red;
			}
			
			// ...and copy this line to the rest
			final int lineBytes = ( x2 - x + 1 ) * 3;
			for ( int i = y2 - y; i > 0; i-- ) {
				linePos += bmpLineSize;
				System.arraycopy( bmpData, firstLinePos, bmpData, linePos, lineBytes );
			}
		}
		else {
			for ( ; y <= y2; y++ )
				drawHLine( x, y, x2 );
		}
	}
	
	/**
	 * Fills a circle.
	 * @param x x coordinate of the center point of the circle 
	 * @param y y coordinate of the center point of the circle 
	 * @param r radius of the circle
	 */
	public void fillCircle( final int x, final int y, final int r ) {
		// Quick check (for the enclosing rectangle)
		if ( x + r <= 0 || y + r <= 0 || x - r >= maxX || y - r >= maxY || r <= 0 ) // Outside of image or no real radius
			return;
		
		// Stepping on y axis from center to border, using circle equation:
		// x*x + y*y = r*r
		// and drawing horizontal lines, mirroring top and bottom half.
		
		// Center line
		drawHLine( x - r, y, x + r );
		
		final int sqr_r = r * r;
		
		for ( int dy = 1, sqr_dy = 1; dy <= r; dy++ ) {
			final int dx = (int) Math.sqrt( sqr_r - sqr_dy );
			
			drawHLine( x - dx, y - dy, x + dx ); // Top half
			drawHLine( x - dx, y + dy, x + dx ); // Bottom half
			
			// sqr(n) = sqr(n-1) + n*2-1        (n+1)*(n+1) = n*n + 2*n + 1 = sqr(n) + (n<<1) + 1
			sqr_dy += ( dy << 1 ) + 1;
		}
	}
	
}
