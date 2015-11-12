package hu.belicza.andras;

import java.io.FileOutputStream;

import hu.belicza.andras.draw.BmpImage;

public class A {

	private static final int COLOR              = BmpImage.color(   0,   0,   0      );
	private static final int SHADOW_COLOR       = BmpImage.color( 255, 255, 255, 128 );
	
	public static void main(String[] args) throws Exception {
		
		final BmpImage bmp = new BmpImage( 800,  600 );
		bmp.clear( 0xff );
		
		bmp.setColor( COLOR );
		
		for ( int i = 0; i < 100; i += 5 )
			bmp.drawLine( 0, 0 + i, 800, 600 );
		
		bmp.setColor( SHADOW_COLOR );
		bmp.fillCircle( 400, 300, 50 );
		
		try ( final FileOutputStream out = new FileOutputStream("w:/a.bmp" ) ) {
			out.write( bmp.bmpData );
		}
	}
	
}
