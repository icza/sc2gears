package hu.belicza.andras.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class UnitIdDecToHexConverter {

	/**
	 * Entry point of the program.
	 * @param arguments used to take parameters from the running environment - not used here
	 */
	public static void main( final String[] arguments ) throws Exception {
		try ( final BufferedReader input = new BufferedReader( new FileReader( "w:/ab.txt" ) ); final PrintWriter output = new PrintWriter( new FileWriter( "w:/ab-out.txt"  ) ) ) {
			String line;
			while ( ( line = input.readLine() ) != null ) {
				final int commaIdx = line.indexOf( ',' );
				
				Integer unitId = Integer.valueOf( line.substring( 0, commaIdx ) );
				output.print( String.format( "%04x", unitId ) );
				output.println( line.substring( commaIdx ) );
			}
			
			output.flush();
		}
	}
	
}
