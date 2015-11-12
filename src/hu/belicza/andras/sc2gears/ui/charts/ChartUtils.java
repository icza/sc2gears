/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.charts;

import hu.belicza.andras.sc2gears.language.Language;

/**
 * Utility class for charts.
 * 
 * @author Andras Belicza
 */
public class ChartUtils {
	
	/**
	 * Graph approximation.
	 * @author Andras Belicza
	 */
	public static enum GraphApproximation {
		LINEAR( "charts.graphApproximation.linear" ),
		CUBIC ( "charts.graphApproximation.cubic"  );
		
		/** Cache of the string value. */
		public final String stringValue;
		
		/**
		 * Creates a new GraphApproximation.
		 * @param textKey key of the text representation
		 */
		private GraphApproximation( final String textKey ) {
			stringValue = Language.getText( textKey );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
}
