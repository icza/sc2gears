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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFOHEADER;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.W32APIOptions;

/**
 * Windows specific screen capture using JNA.
 * 
 * Theory source: http://stackoverflow.com/questions/2912007/java-how-to-take-a-screenshot-fast
 * 
 * @author Andras Belicza
 */
public class JNAScreenshot extends ScreenshotProducer {
	
	private final HDC        windowDC;
	private final HBITMAP    outputBitmap;
	private final HDC        blitDC;
	private final BITMAPINFO bi;
	
	private ColorModel       cm;
	private DataBuffer       buffer;
	private WritableRaster   raster;
	private BufferedImage    resultBufferedImage;
	
    /**
     * Creates a new JNAScreenshot.
     * @param screen area to create screen capture from
     */
    public JNAScreenshot( final Rectangle screenArea ) {
    	super( screenArea );
    	
		windowDC     = GDI.GetDC( USER.GetDesktopWindow() );
		outputBitmap = GDI.CreateCompatibleBitmap( windowDC, screenArea.width, screenArea.height );
		blitDC       = GDI.CreateCompatibleDC( windowDC );
		
    	bi = new BITMAPINFO( 40 );
		bi.bmiHeader.biSize = 40;
    }
	
    @Override
	public BufferedImage getScreenshot() {
		final HANDLE oldBitmap = GDI.SelectObject( blitDC, outputBitmap );
		try {
			GDI.BitBlt( blitDC, 0, 0, screenArea.width, screenArea.height, windowDC, screenArea.x, screenArea.y, GDI32.SRCCOPY );
		} finally {
			GDI.SelectObject( blitDC, oldBitmap );
		}
		
		final boolean ok = GDI.GetDIBits( blitDC, outputBitmap, 0, screenArea.height, (byte[]) null, bi, WinGDI.DIB_RGB_COLORS );
		
		if ( ok ) {
			final BITMAPINFOHEADER bih = bi.bmiHeader;
			bih.biHeight      = -Math.abs( bih.biHeight );
			bih.biCompression = 0;
			
			return bufferedImageFromBitmap( blitDC, outputBitmap );
		}
		else
			return null;
	}
	
	private BufferedImage bufferedImageFromBitmap( final HDC blitDC, final HBITMAP outputBitmap ) {
		final BITMAPINFOHEADER bih   = bi.bmiHeader;
		
		final int height             = Math.abs( bih.biHeight );
		final int strideBits         = ( bih.biWidth * bih.biBitCount );
		final int strideBytesAligned = ( ( ( strideBits - 1 ) | 0x1F ) + 1 ) >> 3;
		final int strideElementsAligned;
		
		switch ( bih.biBitCount ) {
		case 16:
			strideElementsAligned = strideBytesAligned / 2;
			if ( buffer == null ) {
				cm     = new DirectColorModel( 16, 0x7C00, 0x3E0, 0x1F );
    			buffer = new DataBufferUShort( strideElementsAligned * height );
    			raster = Raster.createPackedRaster( buffer, bih.biWidth, height, strideElementsAligned, ( (DirectColorModel) cm ).getMasks(), null );
			}
			break;
		case 32:
			strideElementsAligned = strideBytesAligned / 4;
			if ( buffer == null ) {
				cm     = new DirectColorModel( 32, 0xFF0000, 0xFF00, 0xFF );
    			buffer = new DataBufferInt( strideElementsAligned * height );
    			raster = Raster.createPackedRaster( buffer, bih.biWidth, height, strideElementsAligned, ( (DirectColorModel) cm ).getMasks(), null );
			}
			break;
		default:
			throw new IllegalArgumentException( "Unsupported bit count: " + bih.biBitCount );
		}
		
		final boolean ok;
		switch ( buffer.getDataType() ) {
		case DataBuffer.TYPE_INT : {
			final int[] pixels = ( (DataBufferInt) buffer ).getData();
			ok = GDI.GetDIBits( blitDC, outputBitmap, 0, raster.getHeight(), pixels, bi, 0 );
			break;
		}
		case DataBuffer.TYPE_USHORT : {
			final short[] pixels = ( (DataBufferUShort) buffer ).getData();
			ok = GDI.GetDIBits( blitDC, outputBitmap, 0, raster.getHeight(), pixels, bi, 0 );
			break;
		}
		default:
			throw new AssertionError( "Unexpected buffer element type: " + buffer.getDataType() );
		}
		
		if ( ok ) {
			if ( resultBufferedImage == null )
				resultBufferedImage = new BufferedImage( cm, raster, false, null );
			return resultBufferedImage;
		}
		else
			return null;
	}
	
	@Override
	public void close() {
		if ( outputBitmap != null )
			GDI.DeleteObject( outputBitmap );
		if ( blitDC != null )
			GDI.DeleteObject( blitDC );
	}
	
	private static final User32 USER = User32.INSTANCE;
	private static final GDI32  GDI  = GDI32 .INSTANCE;
	
}

interface GDI32 extends com.sun.jna.platform.win32.GDI32 {
	
	final GDI32 INSTANCE = (GDI32) Native.loadLibrary( GDI32.class );
	
	boolean BitBlt( HDC hdcDest, int nXDest, int nYDest, int nWidth, int nHeight, HDC hdcSrc, int nXSrc, int nYSrc, int dwRop );
	
	HDC GetDC( HWND hWnd );
	
	boolean GetDIBits( HDC dc, HBITMAP bmp, int startScan, int scanLines, byte[] pixels, BITMAPINFO bi, int usage );
	
	boolean GetDIBits( HDC dc, HBITMAP bmp, int startScan, int scanLines, short[] pixels, BITMAPINFO bi, int usage );
	
	boolean GetDIBits( HDC dc, HBITMAP bmp, int startScan, int scanLines, int[] pixels, BITMAPINFO bi, int usage );
	
	final static int SRCCOPY = 0xCC0020;
}

interface User32 extends com.sun.jna.platform.win32.User32 {
	
	final User32 INSTANCE = (User32) Native.loadLibrary( User32.class, W32APIOptions.UNICODE_OPTIONS );
	
	HWND GetDesktopWindow();
	
}
