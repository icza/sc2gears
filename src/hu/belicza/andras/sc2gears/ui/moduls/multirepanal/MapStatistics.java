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

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;

import java.util.EnumMap;
import java.util.Map;

/**
 * Statistics of a map.
 * 
 * @author Andras Belicza
 */
class MapStatistics extends DateRangeStatistics {
	
	/** Name of the map. */
	public final String name;
	
	/** Record on this map. */
	public final Record record = new Record();
	
	/** Total time (seconds) in games. */
	public long totalTimeSecInGames;
	
	/** Race record map (records for different races). */
	public final Map< Race, Record > raceRecordMap = new EnumMap< Race, Record >( Race.class );
	
	/**
	 * Creates a new MapStatistics.
	 * @param name name of the map
	 */
	public MapStatistics( final String name ) {
		this.name = name;
	}
	
	/**
	 * Returns the record for the race.<br>
	 * If a record does not yet exist, a new will be created.
	 * @param race race to return record for
	 * @return the record for the race
	 */
	public Record getRaceRecord( final Race race ) {
		Record record = raceRecordMap.get( race );
		if ( record == null )
			raceRecordMap.put( race, record = new Record() );
		return record;
	}
	
	/**
	 * Returns the average game length in seconds.
	 * @return the average game length in seconds
	 */
	public int getAvgGameLength() {
		return (int) ( totalTimeSecInGames / record.totalGames ); // totalGames is at least 1!
	}
	
}
