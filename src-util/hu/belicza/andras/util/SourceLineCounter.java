/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Locale;

/**
 * Source line counter utility.
 * 
 * @author Andras Belicza
 */
public class SourceLineCounter {
	
	/**
	 * Custom file filter which accepts files of the specified extension.
	 * @author Andras Belicza
	 *
	 */
	private static class CustomFileFilter implements FileFilter {
		private final String[] acceptableFileExtensions;
		
		public CustomFileFilter( final String[] acceptableFileExtensions ) {
			this.acceptableFileExtensions = acceptableFileExtensions;
		}
		
		@Override
		public boolean accept( final File pathname ) {
			if ( pathname.isDirectory() )
				return true;
			
			final String fileName = pathname.getName();
			
			for ( final String acceptableFileExtension : acceptableFileExtensions )
				if ( fileName.toLowerCase().endsWith( acceptableFileExtension ) )
					return true;
			
			return false;
		}
	}
	
	/**
	 * The entry point of the program
	 * @param arguments used to take parameters from the running environment - not used here
	 */
	public static void main( final String[] arguments ) {
		System.out.printf( Locale.US, "Project            TLOC:  %,7d\n", countLinesInFile( "."                               , true ) );
		System.out.printf( Locale.US, "Sc2gears source    TLOC:  %,7d\n", countLinesInFile( "src/hu"                          , true )
																		+ countLinesInFile( "src-updater"                     , true )
																		+ countLinesInFile( "src-shared"                      , true )
																		+ countLinesInFile( "src-common"                      , true )
																		+ countLinesInFile( "src-sc2gearspluginapi"           , true )
																		+ countLinesInFile( "src-plugins"                     , true )
																		+ countLinesInFile( "app-folder/Languages/English.xml", true ) );
		System.out.printf( Locale.US, "Sc2gears DB source TLOC:  %,7d\n", countLinesInFile( "src-sc2gearsdb"                  , true )
																		+ countLinesInFile( "src-common/hu"                   , true ) );
		System.out.printf( Locale.US, "WAR                TLOC:  %,7d\n", countLinesInFile( "war"                             , true ) );
		System.out.printf( Locale.US, "Util-src           TLOC:  %,7d\n", countLinesInFile( "src-util"                        , true ) );
		
		System.out.printf( Locale.US, "---------------------------------\n" );
		
		System.out.printf( Locale.US, "Project Java       TLOC:  %,7d\n", countLinesInFile( "."                               , false ) );
		System.out.printf( Locale.US, "Sc2gears Java      TLOC:  %,7d\n", countLinesInFile( "src/hu"                          , false )
																		+ countLinesInFile( "src-updater"                     , false )
																		+ countLinesInFile( "src-shared"                      , false )
																		+ countLinesInFile( "src-common"                      , false )
																		+ countLinesInFile( "src-sc2gearspluginapi"           , false )
																		+ countLinesInFile( "src-plugins"                     , false ) );
		System.out.printf( Locale.US, "Sc2gears DB Java   TLOC:  %,7d\n", countLinesInFile( "src-sc2gearsdb"                  , false )
																		+ countLinesInFile( "src-common/hu"                   , false ) );
		System.out.printf( Locale.US, "Updater Java       TLOC:  %,7d\n", countLinesInFile( "src-updater"                     , false )
																		+ countLinesInFile( "src-shared"                      , false ) );
	}
	
	/**
	 * Counts the lines in the specified file.<br>
	 * If the file denotes a directory, then the sum of lines of files in side the directory will be returned recursively.
	 * @param pathname pathname to count lines in
	 * @param allCountableFileFilter tells if count in all countable files; only in *.java otherwise
	 * @return the number of lines in the specified pathname
	 */
	private static int countLinesInFile( final String pathname, final boolean allCountableFileFilter ) {
		return countLinesInFile( new File( pathname ),
				allCountableFileFilter ? new CustomFileFilter( new String[] { ".java", ".xml", ".html", ".css", ".properties", ".cmd", ".sh", ".5-" } )
				: new CustomFileFilter( new String[] { ".java" } ) );
	}
	
	/**
	 * Counts the lines in the specified file.<br>
	 * If the file denotes a directory, then the sum of lines of files in side the directory will be returned recursively.
	 * @param file file to count lines in
	 * @return the number of lines in the specified file
	 */
	private static int countLinesInFile( final File file, final FileFilter fileFilter ) {
		if ( file.isDirectory() ) {
			int linesInDirectory = 0;
			final File[] directoryContent = file.listFiles( fileFilter );
			if ( directoryContent == null )
				System.out.println( "Failed to list directory and therefore skipping: " + file.getAbsolutePath() );
			else
				for ( final File child : directoryContent )
					linesInDirectory += countLinesInFile( child, fileFilter );
			return linesInDirectory;
		}
		else {
			try ( final LineNumberReader input = new LineNumberReader( new FileReader( file ) ) ) {
				try { input.skip( Long.MAX_VALUE ); } catch ( final IOException ie ) { ie.printStackTrace(); }
				
				return input.getLineNumber();
			} catch ( final IOException fnfe ) {
				System.out.println( "Can't access and therefore skipping file: " + file.getAbsolutePath() );
				return 0;
			}
		}
	}
	
}
