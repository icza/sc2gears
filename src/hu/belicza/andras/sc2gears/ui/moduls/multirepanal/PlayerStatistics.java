/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.multirepanal;

import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.PlayerId;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.NullAwareComparable;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Statistics of a player.
 * 
 * @author Andras Belicza
 */
class PlayerStatistics extends DateRangeStatistics {
	
	/** Name that is displayed in tables. */
	public final String     playerDisplayName;
	/** Id of the player.                 */
	public final PlayerId   playerId;
	/** Type of player.                   */
	public final PlayerType playerType;
	
	/** Total time (seconds) in games.    */
	public long totalTimeSecInGames;
	/** Total time (seconds) in games for APM calculation. */
	public long totalTimeSecInGamesForApm;
	/** Total actions in games.           */
	public long totalActions;
	/** Total effective actions in games. */
	public long totalEffectiveActions;
	
	/** Record (games played).            */
	public final Record record = new Record();
	
	/** Race distribution map (counts of different races).          */
	public final Map< Race  , IntHolder > raceDistributionMap = new EnumMap< Race  , IntHolder >( Race.class );
	/** Map distribution map (counts of different maps).            */
	public final Map< String, IntHolder > mapDistributionMap  = new HashMap< String, IntHolder >( 8 );
	
	// For advanced statistics
	/** The list of game participation stats of the player. */
	public final List< PlayerGameParticipationStats > playerGameParticipationStatsList = new ArrayList< PlayerGameParticipationStats >();
	
	/**
	 * Creates a new PlayerStatistics.
	 * @param playerDisplayName name that is displayed in tables
	 * @param player            player
	 */
	public PlayerStatistics( final String playerDisplayName, final Player player ) {
		this.playerDisplayName = playerDisplayName;
		if ( player != null ) {
			playerId   = player.playerId;
			playerType = player.type;
		}
		else {
			playerId   = new PlayerId();
			playerType = null;
		}
	}
	
	/**
	 * Builds in the specified player game participation stats.
	 * @param pgps reference to the player game participation stats to build in
	 */
	public void buildInPlayerGameParticipation( final PlayerGameParticipationStats pgps ) {
		registerDate( pgps.date );
		totalTimeSecInGames       += pgps.timeSecInGame;
		totalTimeSecInGamesForApm += pgps.timeSecInGameForApm;
		record.totalGames++;
		totalActions += pgps.actions;
		totalEffectiveActions += pgps.effectiveActions;
		if ( pgps.isWinner != null )
			if ( pgps.isWinner )
				record.wins++;
			else
				record.losses++;
		
		// Add a new occurrence of the race
		final IntHolder raceCounter = raceDistributionMap.get( pgps.race );
		if ( raceCounter == null )
			raceDistributionMap.put( pgps.race, new IntHolder( 1 ) );
		else
			raceCounter.value++;
		
		// Add a new occurrence of the map
		final IntHolder mapCounter = mapDistributionMap.get( pgps.mapName );
		if ( mapCounter == null )
			mapDistributionMap.put( pgps.mapName, new IntHolder( 1 ) );
		else
			mapCounter.value++;
	}
	
	/**
	 * Returns a string representation of the race distribution.
	 * @return a string representation of the race distribution
	 */
	public String getRaceDistributionString() {
		final StringBuilder builder = new StringBuilder();
		for ( final Entry< Race, IntHolder > entry : raceDistributionMap.entrySet() ) {
			if ( builder.length() > 0 )
				builder.append( ", " );
			builder.append( entry.getKey().letter ).append( ":" ).append( entry.getValue().value * 100 / record.totalGames ).append( '%' );
		}
		
		return builder.toString();
	}
	
	/**
	 * Returns the average games per day metric.
	 * @return the average games per day metric
	 */
	public float getAvgGamesPerDay() {
		return (float) record.totalGames / getPresence(); // Presence is at least 1 day!
	}
	
	/**
	 * Returns the average game length in seconds.
	 * @return the average game length in seconds
	 */
	public int getAvgGameLength() {
		return (int) ( totalTimeSecInGames / record.totalGames ); // totalGames is at least 1!
	}
	
	/**
	 * Returns the average APM.
	 * @return the average APM
	 */
	public int getAvgApm() {
		return totalTimeSecInGamesForApm == 0 ? 0 : (int) ( totalActions * 60L / totalTimeSecInGamesForApm );
	}
	
	/**
	 * Returns the average EAPM.
	 * @return the average EAPM
	 */
	public int getAvgEapm() {
		return totalTimeSecInGamesForApm == 0 ? 0 : (int) ( totalEffectiveActions * 60L / totalTimeSecInGamesForApm );
	}
	
	/**
	 * Returns the average EAPM.
	 * @return the average EAPM
	 */
	public NullAwareComparable< Integer > getAvgApmRedundancy() {
		return NullAwareComparable.getPercent( totalActions == 0 ? null : new Integer( (int) ( ( totalActions - totalEffectiveActions ) * 100L / totalActions ) ) );
	}
	
	/**
	 * Returns the formatted total time in games.
	 * @return the formatted total time in games
	 */
	public NullAwareComparable< Long > getFormattedTotalTimeInGames() {
		return new NullAwareComparable< Long >( totalTimeSecInGames ) {
			@Override
			public String toString() {
				return GeneralUtils.formatLongSeconds( value );
			}
		};
	}
	
}
