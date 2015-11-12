/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.sc2replay;

import hu.belicza.andras.sc2gears.sc2replay.S2gsParser.SSPlayer.BOEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.InflaterInputStream;

/**
 * S2gs file parser (Score Screen data file parser).
 * 
 * @author Andras Belicza
 */
public class S2gsParser extends Parser {
	
	private static final PrintStream DEFAULT_SYSOUT = System.out;
	
	public static void main( final String... arguments ) throws IOException {
		int mode = 1;
		
		switch ( mode ) {
		
		case 0 : {
			final File folder = new File( arguments.length == 0 ? "i:/s2gs/my" : arguments [ 0 ] );
    		for ( final File file : folder.listFiles() )
    			if ( file.isDirectory() )
    				main( file.getAbsolutePath() );
    			else
        			if ( file.getName().endsWith( ".s2gs" ) ) {
        				System.setOut( DEFAULT_SYSOUT );
        				System.out.println( file.getAbsolutePath() );
        				final byte[] decompressed = extractS2gsData( file );
        				
        				try ( final FileOutputStream fos = new FileOutputStream( new File( file.getParent(), file.getName() + ".bin" ) ) ) {
        					fos.write( decompressed );
        				}
        				
        				try ( final PrintStream fow = new PrintStream( new File( file.getParent(), file.getName() + ".txt" ) ) ) {
	        				System.setOut( fow );
	        				final S2gsParser s2gsParser = new S2gsParser();
	        				s2gsParser.parseData( decompressed );
        				}
        			}
    		break;
		}
		
		case 1 : {
			// 1v1:
			//final byte[] decompressed = extractS2gsData( new File( "i:/s2gs/my/1a1e0e6716acb65d1e987c8882c4067898bbab7855198be5e94760fb46688943/1a1e0e6716acb65d1e987c8882c4067898bbab7855198be5e94760fb46688943.s2gs" ) );
			// 2v2:
			final byte[] decompressed = extractS2gsData( new File( "i:/s2gs/my/3bea7103ba1e2c71c73867b518f529a7d1f997b388be9ad8cdafeba5b235e91b/3bea7103ba1e2c71c73867b518f529a7d1f997b388be9ad8cdafeba5b235e91b.s2gs" ) );
			
			final S2gsParser s2gsParser = new S2gsParser();
			s2gsParser.parseData( decompressed );
			break;
		}
		
		}
	}
	
	private static byte[] extractS2gsData( final File s2gsFile ) throws IOException {
		final byte[] inBuffer = new byte[ 16 ];
		
		try ( final FileInputStream fis = new FileInputStream( s2gsFile ) ) {
			fis.read( inBuffer );
			
			final ByteBuffer header = ByteBuffer.wrap( inBuffer ).order( ByteOrder.LITTLE_ENDIAN );
			header.getInt();                   // Magic word: "ZmpC" ("CmpZ" backward meaning: compressed with Zlib)
			header.getInt();                   // Zeros (reserved? version?)
			final int size = header.getInt() ; // uncompressed data size
			header.getInt();                   // Zeros (reserved? version?)
			
			try ( final InflaterInputStream compressedInputStream = new InflaterInputStream( fis ) ) {
				final byte[] decompressed = new byte[ size ];
				int pos = 0;
				while ( pos < size )
					pos += compressedInputStream.read( decompressed, pos, size - pos );
				
				return decompressed;
			}
		}
	}
	
	public static class SSPlayer {
		// Player info
		public String id;
		public String race;
		public int team;
		
		// Score summary
		public int resources;
		public int units;
		public int structures;
		public int overview;
		
		// Economy breakdown
		public int avgUnspentRes;
		public int resCollectionRate;
		public int workersCreated;
		
		// Units
		public int unitsTrained;
		public int killedUnitCount;
		public int structuresBuilt;
		public int stucturesRazedCount;
		
		// Resource Collection Rate Graph (rcr, seconds)
		public int[][] rcrGraph;
		
		// Army value graph (army, seconds)
		public int[][] armyValueGraph;
		
		// Build order (time, action, supply)
		public static class BOEntry implements Comparable< BOEntry > {
			public int frame;
			public String action;
			public int supply;
			public int maxSupply;
			public int index;
			
            @Override
            public int compareTo( final BOEntry e ) {
	            return index - e.index;
            }
		}
		
		public List< BOEntry > boEntryList = new ArrayList< BOEntry >();
		
	}
	
	public void parseData( final byte[] data ) {
		setWrapper( data, ByteOrder.LITTLE_ENDIAN );
		
		// https://github.com/GraylinKim/sc2reader/blob/s2gs/sc2reader/resources.py
		
		// S2gs data consists of 8 (or 7) structures:
		final List< Object > structureList = new ArrayList< Object >();
		while ( wrapper.hasRemaining() )
			structureList.add( readStructure() );
		
		final Object[] structures = structureList.toArray();
		
		Object st, o;
		Object[] a;
		
		st = pathElement( structures, 0 );
		
		// General info:
		System.out.println( "Game speed:  " + intToString( pathElement( st, 0, 1 ) ) );
		System.out.println( "Date:        " + new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( new Date( (Long) pathElement( st, 8 ) * 1000 ) ) );
		System.out.println( "Game length: " + pathElement( st, 7 ) + " sec" );
		
		// Players:
		System.out.println();
		final List< SSPlayer > playerList = new ArrayList< SSPlayer >();
		for ( int i = 0; i < 16; i++ ) {
			o = pathElement( st, 3, i, 0, 1 );
			if ( o instanceof Object[] ) {
				final SSPlayer player = new SSPlayer();
				a = pathElement( o, 0 );
				player.id   = "gw_" + a[ 0 ] + "/" + intToString( a[ 1 ] ) + "/reg_" + a[ 2 ] + "/" + a[ 3 ];
				player.race = intToString( pathElement( st, 3, i, 2 ) );
				player.team = ( (Integer) pathElement( st, 3, i, 1, 0 ) + 1 ); // TODO Maybe this is the "is_winner" property
				playerList.add( player );
				
				System.out.println( "Player " + i + ":" );
				System.out.println( "\tId  : " + player.id   );
				System.out.println( "\tRace: " + player.race );
				System.out.println( "\tTeam: " + player.team );
			}
			else {
				// else no player (TODO or it might be a computer)
				break;
			}
		}
		
		final SSPlayer[] players = playerList.toArray( new SSPlayer[ playerList.size() ] );
		SSPlayer player;
		
		st = pathElement( structures, 3 );
		
		// Points
		System.out.println( "SCORE SUMMARY" );
		for ( int i = 0; i < players.length; i++ ) {
			player = players[ i ];
			player.resources  = pathElement( st, 0, 0, 1, i, 0, 0 );
			player.units      = pathElement( st, 0, 1, 1, i, 0, 0 );
			player.structures = pathElement( st, 0, 2, 1, i, 0, 0 );
			player.overview   = pathElement( st, 0, 3, 1, i, 0, 0 );
			System.out.println( "\tPlayer " + i
				+ ": Resources: "  + player.resources
				+ ", Units: "      + player.units
				+ ", Structures: " + player.structures
				+ ", Overview: "   + player.overview );
		}
		
		System.out.println( "ECONOMY BREAKDOWN" );
		for ( int i = 0; i < players.length; i++ ) {
			player = players[ i ];
			player.avgUnspentRes     = pathElement( st, 0, 4, 1, i, 0, 0 );
			player.resCollectionRate = pathElement( st, 0, 5, 1, i, 0, 0 );
			player.workersCreated    = pathElement( st, 0, 6, 1, i, 0, 0 );
			System.out.println( "\tPlayer " + i
				+ ": Avg Unspent Res: "     + player.avgUnspentRes
				+ ", Res Collection Rate: " + player.resCollectionRate
				+ ", Workers Created: "     + player.workersCreated
				+ ", Resources: "           + player.resources );
		}
		
		System.out.println( "UNITS" );
		for ( int i = 0; i < players.length; i++ ) {
			player = players[ i ];
			player.unitsTrained    = pathElement( st, 0, 7, 1, i, 0, 0 );
			player.killedUnitCount = pathElement( st, 0, 8, 1, i, 0, 0 );
			player.structuresBuilt = pathElement( st, 0, 9, 1, i, 0, 0 );
		}
		
		st = pathElement( structures, 4 );
		
		// Points continued...
		System.out.println( "...UNITS" );
		for ( int i = 0; i < players.length; i++ ) {
			player = players[ i ];
			player.stucturesRazedCount = pathElement( st, 0, 0, 1, i, 0, 0 );
			System.out.println( "\tPlayer " + i
				+ ": Units Trained: " + player.unitsTrained
				+ ", Killed Unit Count: " + player.killedUnitCount
				+ ", Structures Built: " + player.structuresBuilt
				+ ", Structures Razed Count: " + player.stucturesRazedCount );
		}
		
		System.out.println( "RESOURCE COLLECTION RATE GRAPH (rcr, seconds)" );
		for ( int i = 0; i < players.length; i++ ) {
			player = players[ i ];
			a = pathElement( st, 0, 1, 1, i );
			player.rcrGraph = new int[ a.length ][ 2 ];
			System.out.print( "\tPlayer " + i + ":" );
			for ( int j = 0; j < a.length; j++ ) {
				player.rcrGraph[ j ][ 0 ] = pathElement( a, j, 0 );
				player.rcrGraph[ j ][ 1 ] = pathElement( a, j, 2 );
				System.out.print( " (" + player.rcrGraph[ j ][ 0 ] + "," + player.rcrGraph[ j ][ 1 ] + ")" );
			}
			System.out.println();
		}
		
		System.out.println( "ARMY VALUE GRAPH (army, seconds)" );
		for ( int i = 0; i < players.length; i++ ) {
			player = players[ i ];
			a = pathElement( st, 0, 2, 1, i );
			player.armyValueGraph = new int[ a.length ][ 2 ];
			System.out.print( "\tPlayer " + i + ":" );
			for ( int j = 0; j < a.length; j++ ) {
				player.armyValueGraph[ j ][ 0 ] = pathElement( a, j, 0 );
				player.armyValueGraph[ j ][ 1 ] = pathElement( a, j, 2 );
				System.out.print( " (" + player.armyValueGraph[ j ][ 0 ] + "," + player.armyValueGraph[ j ][ 1 ] + ")" );
			}
			System.out.println();
		}
		
		
		System.out.println( "BUILD ORDERS (time, action, supply)" );
		// The rest of this structure are build order structures...
		a = pathElement( st, 0 );
		for ( int i = 3; i < a.length; i++ )
			processBuildOrderStruct( a[ i ], players );
		
		// And all of the remaining structures consists of build order structures...
		for ( int i = 5; i < structures.length; i++ ) {
			a = pathElement( structures, i, 0 );
			for ( int j = 0; j < a.length; j++ )
				processBuildOrderStruct( a[ j ], players );
		}
		
		// We now have all the BO entries...
		for ( int i = 0; i < players.length; i++ ) {
			Collections.sort( players[ i ].boEntryList );
			System.out.print( "\tPlayer " + i + ":" );
			for ( final BOEntry boEntry : players[ i ].boEntryList )
				System.out.print( " ("
					+ boEntry.frame / 64 
					+ "," + boEntry.action
					+ "," + boEntry.supply + "/" + boEntry.maxSupply + ")" );
			System.out.println();
		}
		
		
		/*int counter = 0;
		while ( wrapper.hasRemaining() ) {
			System.out.println( "STRUCTURE: #" + ++counter );
			printStructure( readStructure(), "" );
			System.out.printf( "\nBuffer pos: " + wrapper.position() + " (0x%x)\n", wrapper.position() );
		}*/
	}
	
	private static void processBuildOrderStruct( Object st, final SSPlayer[] players ) {
		final long actionFlag = ( (Number) pathElement( st, 0, 1 ) ).longValue();
		String action;
		if ( ( actionFlag >> 24 ) == 0x01 )
			action = "U:" + ( actionFlag & 0xffffff );
		else if ( ( actionFlag >> 24 ) == 0x02 )
			action = "R:" + ( actionFlag & 0xffffff );
		else
			action = "Unknown";
		
		st = pathElement( st, 1 );
		for ( int i = 0; i < players.length; i++ ) {
    		final Object[] a = pathElement( st, i );
    		if ( a.length == 0 )
    			continue;
    		final SSPlayer player = players[ i ];
    		for ( int j = 0; j < a.length; j++ ) {
    			final Number o0 = pathElement( a, j, 0 ), o1 = pathElement( a, j, 1 ), o2 = pathElement( a, j, 2 );
    			final BOEntry boEntry = new BOEntry();
    			boEntry.frame     = (int) ( o2.longValue() >> 8 ) << 2;  // ( (o2 >> 8) holds time value in unit of 1/16 sec)
    			boEntry.supply    = o0.intValue();
    			boEntry.maxSupply = o1.intValue() & 0xff;
    			boEntry.action    = action;
    			boEntry.index     = o1.intValue() >> 16;
    			player.boEntryList.add( boEntry );
    		}
		}
	}
	
	/**
	 * Returns an element of the specified structure specified by the indices.
	 * Each structure element on the path is assumed and casted to <code>Object[]</code>.
	 * @param indices indices to step forward in the "array" tree
	 * @return the element specified by the indicies path
	 */
	@SuppressWarnings( "unchecked" )
    private static <T> T pathElement( final Object structure, final int... indices ) {
		Object element = structure;
		
		for ( int idx : indices )
			element = ( (Object[]) element )[ idx ];
		
		return (T) element;
	}
	
	private static String intToString( final Object o ) {
		final int n = (Integer) o;
		byte[] b = { (byte) n, (byte) ( n >> 8 ), (byte) ( n >> 16 ), (byte) ( n >> 24 ) };
		//byte[] b = { (byte) ( n >> 24 ), (byte) ( n >> 16 ), (byte) ( n >> 8 ), (byte) n };
		
		for ( int i = 0; i < 4; i++ )
			if ( b[ i ] != 0 )
				return new String( b, i, 4 - i );
		
		return "";
	}
	
}
