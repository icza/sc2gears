package hu.belicza.andras.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AbilityCodesShifter {

	/**
	 * Entry point of the program.
	 * @param arguments used to take parameters from the running environment - not used here
	 */
	public static void main( final String[] arguments ) throws Exception {
		try ( final BufferedReader input = new BufferedReader( new FileReader( "w:/ab.txt" ) ); final PrintWriter    output = new PrintWriter( new FileWriter( "w:/ab-out.txt"  ) ) ) {
			String line;
			while ( ( line = input.readLine() ) != null ) {
				Integer abilityCode = Integer.valueOf( line.substring( 0, 6 ), 16 );
				abilityCode += 0x0100;
				/*if ( ( abilityCode & 0x0f00 ) < 0x0e00 ) 
					abilityCode += 0x0200;
				else
					abilityCode += 0xf200;*/
				output.print( String.format( "%06x,", abilityCode ) );
				output.println( line.substring( 7 ) );
			}
			
			output.flush();
		}
	}
	
}
