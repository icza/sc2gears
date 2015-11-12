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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * S2mh file parser (SC2 Map Header).
 * 
 * @author Andras Belicza
 */
public class S2mhParser extends Parser {
	
	public static void main( final String... arguments ) throws IOException {
		
		//final File f = new File( "w:/replay.server.battlelobby" );
		final File f = new File( "w:/ec8c308a1726708523a8b4cc6b56ae5c701ac2bcb65528ae07c1e3efa9cff6e7.s2mh" );
		final byte[] buff = new byte[ (int) f.length() ];
		
		try ( final InputStream in = new FileInputStream( f ) ) {
			in.read( buff );
			
			final Parser p = new Parser();
			p.wrapper = ByteBuffer.wrap( buff );
	
			Parser.printStructure( p.readStructure(), "" );
		}
	}
	
}
