package hu.belicza.andras.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Application which counts files and lines, and writes the result to a file.
 * 
 * @author Andras Belicza
 */
public class CountSourceLines {
	
	/** Set of binary file extensions. */
	private static final Set< String > BINARY_EXT_SET = new HashSet<>( Arrays.asList( "gif", "png", "jpg", "rsa", "mp3", "jar",
	                                                          "ico", "txt_" ) );
	
	/**
	 * Line count statistics.
	 * 
	 * @author Andras Belicza
	 */
	private static class Stat implements Comparable< Stat > {
		
		/** File extension. */
		public final String ext;
		
		/** Total files. */
		public int          files;
		
		/** Total size. */
		public long         size;
		
		/** Total lines. */
		public int          lines;
		
		
		/**
		 * Creates a new {@link Stat}.
		 * 
		 * @param ext file extension
		 */
		public Stat( final String ext ) {
			this.ext = ext;
		}
		
		/**
		 * Adds the specified stat into this.
		 * 
		 * @param s stats to be added
		 */
		public void add( final Stat s ) {
			files += s.files;
			size += s.size;
			lines += s.lines;
		}
		
		@Override
		public int compareTo( final Stat s ) {
			// We want descendant order
			int result = s.lines - lines;
			if ( result == 0 )
				result = s.files - files;
			if ( result == 0 )
				result = Long.compare( s.size, size );
			if ( result == 0 )
				result = s.ext.compareTo( ext );
			return result;
		}
		
	}
	
	/**
	 * @param args used to take arguments from the running environment - not used here
	 * @throws Exception if any error occurs
	 */
	public static void main( final String[] args ) throws Exception {
		final String outputFile = "dev-data/source-stats/" + new SimpleDateFormat( "yyyy-MM-dd HH_mm_ss" ).format( new Date() )
		        + ".txt";
		System.out.println( "Writing stats to file: " + outputFile );
		
		try ( final PrintStream out = new PrintStream( Files.newOutputStream( Paths.get( outputFile ) ) ) ) {
			System.setOut( out );
			
			System.out.println( "File: " + outputFile );
			
			main2( args );
			
			out.flush();
		}
	}
	
	/**
	 * @param args used to take arguments from the running environment - not used here
	 * @throws Exception if any error occurs
	 */
	public static void main2( final String[] args ) throws Exception {
		// Global statistics map
		final Map< String, Stat > globalExtStatMap = new TreeMap<>();
		
		final String[] folders = { "src", "src-common", "src-updater", "src-shared", "src-sc2gearspluginapi",
				"app-folder/Languages", "app-folder/lib",
				"src-util", "src-sc2gearsdb", "src-plugins", "create-release.cmd", "war/index.html", "war/bnet_profile_loader.html",
				"war/parsing_service_tester.html", "war/sc2gearsdb.css" };
		
		for ( final String folder : folders ) {
			// Statistics map
			final Map< String, Stat > extStatMap = new TreeMap<>();
			
			countFolder( Paths.get( folder ), extStatMap );
			
			// Calculate folder ALL
			final Stat folderAllStat = new Stat( "<ALL>" );
			for ( final Stat stat : extStatMap.values() )
				folderAllStat.add( stat );
			extStatMap.put( folderAllStat.ext, folderAllStat );
			
			printStats( folder, extStatMap );
			
			// Add to global stats
			for ( final Stat stat : extStatMap.values() ) {
				Stat globalStat = globalExtStatMap.get( stat.ext );
				if ( globalStat == null )
					globalExtStatMap.put( stat.ext, globalStat = new Stat( stat.ext ) );
				globalStat.add( stat );
			}
			
			if ( "app-folder/lib".equals( folder ) )
				printStats( "SC2GEARS STATS (so far)", globalExtStatMap );
		}
		
		printStats( "PROJECT STATS", globalExtStatMap );
	}
	
	/**
	 * Prints the specified stats.
	 * 
	 * @param title title to be printed first
	 * @param extStatMap stats map to be printed
	 */
	private static void printStats( final String title, final Map< String, Stat > extStatMap ) {
		System.out.println( "\n_______________________________________________" );
		System.out.println( title );
		
		final List< Stat > statList = new ArrayList<>( extStatMap.values() );
		Collections.sort( statList );
		
		System.out.printf( "%13s  %9s%12s%11s\n", "", "Files", "Size", "Lines" );
		for ( final Stat stat : statList ) {
			System.out.printf( Locale.US, "%13s: %,9d%,12d%,11d\n", stat.ext, stat.files, stat.size, stat.lines );
		}
	}
	
	/**
	 * Counts lines in the specified path.
	 * 
	 * @param folder folder to count in
	 * @param extStatMap statistics map
	 * @throws Exception if any error occurs
	 */
	private static void countFolder( final Path folder, final Map< String, Stat > extStatMap ) throws Exception {
		Files.walkFileTree( folder, new SimpleFileVisitor< Path >() {
			@Override
			public FileVisitResult visitFile( final Path file, final BasicFileAttributes attrs ) throws IOException {
				final String name = file.getFileName().toString();
				final String ext = name.lastIndexOf( '.' ) < 0 ? "<no-ext>" : name.substring( name.lastIndexOf( '.' ) + 1 );
				
				Stat stat = extStatMap.get( ext );
				if ( stat == null )
					extStatMap.put( ext, stat = new Stat( ext ) );
				
				stat.files++;
				stat.size += attrs.size();
				
				if ( BINARY_EXT_SET.contains( ext ) )
					return FileVisitResult.CONTINUE;
				
				try ( final LineNumberReader in = new LineNumberReader( Files.newBufferedReader( file, StandardCharsets.UTF_8 ) ) ) {
					in.skip( Long.MAX_VALUE );
					stat.lines += in.getLineNumber();
				} catch ( final Exception e ) {
					System.err.println( "ERROR IN FILE: " + file );
					throw e;
				}
				
				return FileVisitResult.CONTINUE;
			}
		} );
	}
	
}
