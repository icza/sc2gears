package hu.belicza.andras.util;

import hu.belicza.andras.sc2gears.util.GeneralUtils;

import java.io.File;
import java.io.FileFilter;

public class CalcChecksum {
	
	public static void main( final String[] args ) {
		final File[] releases = new File( "w:/" ).listFiles( new FileFilter() {
			@Override
			public boolean accept( final File pathname ) {
				return pathname.isFile() && pathname.getName().startsWith( "Sc2gears" ) && pathname.getName().endsWith( ".zip" );
			}
		} );
		
		if ( releases != null )
			for ( final File release : releases )
				System.out.println( release.getAbsolutePath() + " SHA-256:\n\t" + GeneralUtils.calculateFileSha256( release ) + "\n" );
		
		final File[] languages = new File( "w:/" ).listFiles( new FileFilter() {
			@Override
			public boolean accept( final File pathname ) {
				return pathname.isFile() && pathname.getName().endsWith( ".xml" );
			}
		} );
		
		if ( languages != null )
			for ( final File language : languages )
				System.out.println( language.getAbsolutePath() + " MD5:\n\t" + GeneralUtils.calculateFileMd5( language ) + "\n" );
	}
	
}
