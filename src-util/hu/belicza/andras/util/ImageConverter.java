package hu.belicza.andras.util;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

public class ImageConverter {
	
	/**
	 * Entry point of the program.
	 * @param arguments used to take parameters from the running environment - not used here
	 */
	public static void main( final String[] arguments ) throws Exception {
		convertImages( new File( "w:/pic" ) );
	}
	
	private static void convertImages( final File file ) throws Exception {
		if ( file.isDirectory() ) {
			for ( final File childFile : file.listFiles() )
				convertImages( childFile );
		}
		else {
			final BufferedImage bi = ImageIO.read( file );
			
			// Default quality is not sufficient!
			// ImageIO.write( bi, "JPG", new File( file.getPath().replace( ".png", ".jpg" ) ) );
			
			final ImageWriter     iw  = ImageIO.getImageWritersByFormatName( "jpg" ).next();
			final ImageWriteParam iwp = iw.getDefaultWriteParam();
			
			iwp.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );
			iwp.setCompressionQuality( 0.9f );
			
			iw.setOutput( new FileImageOutputStream( new File( file.getPath().replace( ".png", ".jpg" ) ) ) );
			iw.write( null, new IIOImage( bi , null, null ), iwp );
			iw.dispose();
		}
	}
	
}
