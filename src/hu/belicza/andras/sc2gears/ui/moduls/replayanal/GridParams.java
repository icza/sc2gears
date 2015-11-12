/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.replayanal;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;

/**
 * Custom grid parameters.
 * 
 * @author Andras Belicza
 */
public class GridParams {
	
	public static enum PredefinedGrid {
		SPAWN_LARVA  ( "gridSettings.autoConfigure.spawnLarva"   ),
		CALLDOWN_MULE( "gridSettings.autoConfigure.calldownMule" ),
		CHRONO_BOOST ( "gridSettings.autoConfigure.chronoBoost"  ),
		CUSTOM       ( "gridSettings.autoConfigure.custom"       );
		
		/** Cache of the string value. */
		public final String stringValue;
		
		/**
		 * Creates a new PredefinedGrid.
		 * @param textKey key of the text representation
		 */
		private PredefinedGrid( final String textKey ) {
			stringValue = Language.getText( textKey );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Time unit.
	 * @author Andras Belicza
	 */
	public static enum TimeUnit {
		/** Seconds.                                                               */
		SECONDS            ( "module.repAnalyzer.tab.charts.timeUnit.seconds"            ),
		/** Frames.                                                                */
		FRAMES             ( "module.repAnalyzer.tab.charts.timeUnit.frames"             ),
		/** Energy regeneration: the amount of time needed to regenerate 1 energy. */
		ENERGY_REGENERATION( "module.repAnalyzer.tab.charts.timeUnit.energyRegeneration" );
		
		/** Cache of the string value. */
		public final String stringValue;
		
		/**
		 * Creates a new MapViewQuality.
		 * @param textKey key of the text representation
		 */
		private TimeUnit( final String textKey ) {
			stringValue = Language.getText( textKey );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/** What predefined grid is selected. */
	public PredefinedGrid predefinedGrid;
	
	/** First marker.  */
	public Pair< IntHolder, Holder< TimeUnit > > firstMarker;
	/** Repeat marker. */
	public Pair< IntHolder, Holder< TimeUnit > > repeatMarker;
	
	/**
	 * Creates a new GridParams.<br>
	 * Initializes the values from the {@link Settings}.
	 */
	public GridParams() {
		try {
			predefinedGrid = PredefinedGrid.values()[ Settings.getInt( Settings.KEY_REP_ANALYZER_CHARTS_PREDEFINED_GRID ) ];
		} catch ( final ArrayIndexOutOfBoundsException aiobe ) {
			predefinedGrid = PredefinedGrid.values()[ 0 ];
		}
		
		firstMarker  = getTimeSetting( Settings.KEY_REP_ANALYZER_CHARTS_GRID_FIRST_MARKER  );
		repeatMarker = getTimeSetting( Settings.KEY_REP_ANALYZER_CHARTS_GRID_REPEAT_MARKER );
	}
	
	/**
	 * Returns a time stored in the {@link Settings}<br>
	 * The time is stored in a string as a comma separated int pair
	 * where the first int is the time value, the second is the ordinal of the {@link TimeUnit}.
	 * @param settingKey key of the setting whose value to be parsed
	 * @return the parsed time
	 * @see {@link #storeSettings()}
	 */
	private static Pair< IntHolder, Holder< TimeUnit > > getTimeSetting( final String settingKey ) {
		final String timeString = Settings.getString( settingKey );
		final int    commaIndex = timeString.indexOf( ',' );
		
		final int timeValue = Integer.parseInt( timeString.substring( 0, commaIndex ) );
		TimeUnit timeUnit;
		try {
			timeUnit = TimeUnit.values()[ Integer.parseInt( timeString.substring( commaIndex + 1 ) ) ];
		} catch ( final ArrayIndexOutOfBoundsException aiobe ) {
			timeUnit = TimeUnit.SECONDS;
		}
		
		return new Pair< IntHolder, Holder< TimeUnit > >( new IntHolder( timeValue ), new Holder< TimeUnit >( timeUnit ) );
	}
	
	/**
	 * Stores the parameter values to the {@link Settings}.
	 */
	public void storeSettings() {
		Settings.set( Settings.KEY_REP_ANALYZER_CHARTS_PREDEFINED_GRID, predefinedGrid.ordinal() );
		
		storeTime( Settings.KEY_REP_ANALYZER_CHARTS_GRID_FIRST_MARKER , firstMarker  );
		storeTime( Settings.KEY_REP_ANALYZER_CHARTS_GRID_REPEAT_MARKER, repeatMarker );
	}
	
	/**
	 * Stores a time to the {@link Settings}.
	 * @param settingKey key of the setting to store with
	 * @param time time to be stored
	 * @see for the store format see {@link #getTimeSetting(String)
	 */
	private static void storeTime( final String settingKey, final Pair< IntHolder, Holder< TimeUnit > > time ) {
		Settings.set( settingKey, time.value1.value + "," + time.value2.value.ordinal() );
	}
	
	/**
	 * Returns the value of the specified time as frame.
	 * @param timeValue          value of the time
	 * @param timeUnit           unit of the time
	 * @param converterGameSpeed converter game speed to use for game time <=> real time conversion
	 * @return the value of the specified time as frame
	 */
	public static float getFrame( final int timeValue, final TimeUnit timeUnit, final GameSpeed converterGameSpeed ) {
		switch ( timeUnit ) {
		case SECONDS             : return converterGameSpeed.convertToGameTime( timeValue ) << ReplayConsts.FRAME_BITS_IN_SECOND;
		case FRAMES              : return timeValue;
		case ENERGY_REGENERATION : return timeValue / ReplayConsts.ENERGY_REGENERATION_FRAME_RATE;
		default                  : throw new RuntimeException( "Unhandled time unit." );
		}
	}
	
}
