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

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Format;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

/**
 * Statistics of a chart segment.
 * 
 * @author Andras Belicza
 */
class ChartSegmentStatistics {
	
	/** Lower (first) date of the segment.                   */
	public final Date lowerDate;
	
	/** Record of the period belonging to the chart segment. */
	public final Record record = new Record();
	
	/** Total time (seconds) in games for APM calculation.   */
	public long timeSecInGameForApm;
	/** Total actions in games.                              */
	public long totalActions;
	/** Total effective actions in games.                    */
	public long totalEffectiveActions;
	
	/** Total hatchery life time in frames in relevance to the average spawning ratio.                   */
	public long totalHatchTime;
	/** Total time in frames when hatcheries were spawning larva.                                        */
	public long totalHatchSpawnTime;
	/** Total injection gap time in frames between injections in relevance to the average injection gap. */
	public long totalInjectionGap;
	/** Total number of injection gaps in relevance to the average injection gap.                        */
	public int  totalInjectionGapCount;
	
	/** Race distribution map (counts of different races).           */
	public final Map< Race    , IntHolder > raceDistributionMap     = new EnumMap< Race    , IntHolder >( Race    .class );
	/** Game type distribution map (counts of different game types). */
	public final Map< GameType, IntHolder > gameTypeDistributionMap = new EnumMap< GameType, IntHolder >( GameType.class );
	/** Format distribution map (counts of different formats).       */
	public final Map< Format  , IntHolder > formatDistributionMap   = new EnumMap< Format  , IntHolder >( Format  .class );
	
	/**
	 * Creates a new ChartSegmentStatistics.
	 * @param lowerDate lower (first) date of the segment
	 */
	public ChartSegmentStatistics( final Date lowerDate ) {
		this.lowerDate = lowerDate;
	}
	
	/**
	 * Returns the average APM.
	 * @return the average APM; or -1 if average APM is not available
	 */
	public int getAvgApm() {
		return timeSecInGameForApm == 0 ? -1 : (int) ( totalActions * 60L / timeSecInGameForApm );
	}
	
	/**
	 * Returns the average EAPM.
	 * @return the average EAPM; or -1 if average EAPM is not available
	 */
	public int getAvgEapm() {
		return timeSecInGameForApm == 0 ? -1 : (int) ( totalEffectiveActions * 60L / timeSecInGameForApm );
	}
	
	/**
	 * Returns the average spawning ratio in percent.
	 * @return the average spawning ratio in percent; or -1 if average spawning ratio is not available
	 */
	public int getAvgSpawningRatio() {
		return totalHatchTime == 0 ? -1 : (int) ( totalHatchSpawnTime * 100L / totalHatchTime );
	}
	
	/**
	 * Returns the average injection gap in seconds multiplied by 10.
	 * @return the average injection gap in seconds multiplied by 10; or -1 if average injection gap is not available
	 */
	public int getAvgInjectionGap() {
		return totalInjectionGapCount == 0 ? -1 : (int) ( 10L * totalInjectionGap / totalInjectionGapCount / ReplayConsts.FRAMES_IN_SECOND );
	}
	
}
