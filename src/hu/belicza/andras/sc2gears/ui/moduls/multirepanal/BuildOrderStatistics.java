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

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Format;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;

import java.util.EnumMap;
import java.util.Map;


/**
 * Statistics of a build order.
 * 
 * @author Andras Belicza
 */
class BuildOrderStatistics extends DateRangeStatistics {
	
	/** The build order. */
	public final String buildOrder;
	
	/** Race where this BO belongs to. */
	public final Race race;
	
	/** Record (using this BO). */
	public final Record record = new Record();
	
	/** Record vs race map (records vs different races). */
	public final Map< Race, Record > recordVsRaceMap;
	
	/**
	 * Creates a new BuildOrderStatistics.
	 * @param buildOrder the build order
	 * @param race       race where this BO belongs to
	 * @param format     format of the game this BO is taken from
	 */
	public BuildOrderStatistics( final String buildOrder, final Race race, final Format format ) {
		this.buildOrder = buildOrder;
		this.race       = race;
		recordVsRaceMap = format == Format.ONE_VS_ONE ? new EnumMap< Race, Record >( Race.class ) : null;
	}
	
	/**
	 * Builds in the specified player game participation stats.
	 * @param pgps reference to the player game participation stats to build in
	 */
	public void buildInPlayerGameParticipation( final PlayerGameParticipationStats pgps ) {
		registerDate( pgps.date );
		record.totalGames++;
		
		Record recordVsRace;
		if ( recordVsRaceMap == null )
			recordVsRace = null;
		else {
			recordVsRace = recordVsRaceMap.get( pgps.opponent1v1Race );
			if ( recordVsRace == null )
				recordVsRaceMap.put( pgps.opponent1v1Race, recordVsRace = new Record() );
			recordVsRace.totalGames++;
		}
		
		if ( pgps.isWinner != null ) {
			if ( pgps.isWinner ) {
				record.wins++;
				if ( recordVsRace != null )
					recordVsRace.wins++;
			}
			else {
				record.losses++;
				if ( recordVsRace != null )
					recordVsRace.losses++;
			}
		}
	}
	
}
