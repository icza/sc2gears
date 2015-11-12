/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api;

import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayer;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerSelectionTracker;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IReplay;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitTier;

import java.util.Comparator;

/**
 * Defines utility services to work with replays.
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GeneralServices
 * @see ReplayFactoryApi
 * @see IReplay
 */
public interface ReplayUtilsApi {
	
	/** Interface version. */
	String VERSION = "2.5";
	
	/**
	 * Returns a {@link Unit} comparator which defines a Tier reversed order.<br>
	 * In case of identical tier units the comparator reverts to the Unit order.
	 * @return a {@link Unit} comparator which defines a Tier reversed order
	 * @see Unit
	 * @see UnitTier
	 */
	Comparator< Unit > getUnitTierReverseComparator();
	
	/**
	 * Calculates the APM of a player.
	 * @param replay reference to the replay
	 * @param player reference to the player whose APM to be calculated
	 * @return the calculated APM of a player
	 */
	int calculatePlayerApm( IReplay replay, IPlayer player );
	
	/**
	 * Calculates the EAPM of a player.
	 * 
	 * <p>See <a href="https://sites.google.com/site/sc2gears/features/replay-analyzer/apm-types">APM Types</a> for EAPM algorithm details.</p>
	 * 
	 * @param replay reference to the replay
	 * @param player reference to the player whose EAPM to be calculated
	 * @return the calculated EAPM of a player
	 * @since "2.5"
	 * @see EapmUtilsApi
	 */
	int calculatePlayerEapm( IReplay replay, IPlayer player );
	
	/**
	 * Calculates and returns the APM from the specified actions count and frames
	 * @param actionsCount actions count
	 * @param frames       frames, it must be pre-converted to real-time if intended so
	 * @return the APM for the specified actions count and frames
	 */
	int calculateApm( int actionsCount, int frames );
	
	/**
	 * Formats the specified amount of frames.
	 * @param frames frames to be formatted
	 * @param gameSpeed game speed to be used for real time conversion
	 * @return the formatted frames
	 */
	String formatFrames( int frames, GameSpeed gameSpeed );
	
	/**
	 * Formats the specified amount of milliseconds.
	 * @param ms milliseconds to be formatted
	 * @param gameSpeed game speed to be used for real time conversion
	 * @return the formatted milliseconds
	 */
	String formatMs( int ms, GameSpeed gameSpeed );
	
	/**
	 * Formats the specified amount of frames to a short format (for example <code>"6:32"</code>).
	 * @param frames    frames to be formatted
	 * @param gameSpeed game speed to be used for real time conversion
	 * @return the formatted frames in short format
	 */
	String formatFramesShort( int frames, GameSpeed gameSpeed );
	
	/**
	 * Formats the specified amount of frames to a digit decimal format, with 2 fraction digits,
	 * for example <code>"38.63"</code>.
	 * 
	 * <p>The implementation is fast because it does not use format string or floating point arithmetic.</p>
	 * 
	 * @param frames    frames to be formatted
	 * @param gameSpeed game speed to be used for real time conversion
	 * @return the formatted frames in decimal format, with 2 fraction digits
	 */
	String formatFramesDecimal( int frames, GameSpeed gameSpeed );
	
	/**
	 * Formats a coordinate to a digit decimal format, with 2 fraction digits,
	 * for example <code>"38.63"</code>.
	 * 
	 * <p>The implementation is fast because it does not use format string or floating point arithmetic.</p>
	 * 
	 * @param coord coordinate to be formatted, must be the map location multiplied by 65536
	 * @return the formatted coordinate
	 */
	String formatCoordinate( int coord );
	
	/**
	 * Returns the build numbers as a string.
	 * @param buildNumbers build numbers
	 */
	String convertBuildNumbersToString( int[] buildNumbers );
	
	/**
	 * Tries to parse the version from the given text.
	 * @param text text to parse the version from
	 * @return the parsed version or <code>null</code> if text is not a valid version
	 */
	int[] parseVersion( String text );
	
	/**
	 * Compares 2 versions.
	 * 
	 * <p>This method performs a "soft" check: declares <code>"2.0"</code> equal to <code>"2.0.1"</code> for example!</p>
	 * 
	 * @param v1 v1 to be compared
	 * @param v2 v2 to compare to
	 * @return a positive int if v1 > v2, 0 if v1 == v2, a negative int if v1 < v2
	 * 
	 * @see #strictCompareVersions(int[], int[])
	 */
	int compareVersions( int[] v1, int[] v2 );
	
	/**
	 * Compares 2 versions.
	 * 
	 * <p>This method performs a "hard" check: declares <code>"2.0"</code> less than <code>"2.0.1"</code> for example!</p>
	 * 
	 * @param v1 v1 to be compared
	 * @param v2 v2 to compare to
	 * @return a positive int if v1 > v2, 0 if v1 == v2, a negative int if v1 < v2
	 * 
	 * @see #compareVersions(int[], int[])
	 */
	int strictCompareVersions( int[] v1, int[] v2 );
	
	/**
	 * Creates and returns a player selection tracker which can be used to track the selection of a player.
	 * @return a player selection tracker which can be used to track the selection of a player
	 * @see IPlayerSelectionTracker
	 */
	IPlayerSelectionTracker createPlayerSelectionTracker();
	
}
