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

import hu.belicza.andras.sc2gears.ui.moduls.multirepanal.MultiRepAnalysis.ChartGranularity;
import hu.belicza.andras.sc2gearspluginapi.api.enums.LadderSeason;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Input data for the charts.
 * 
 * @author Andras Belicza
 */
class ChartData {
	
	/** Granularity of the chart data. */
	public final ChartGranularity chartGranularity;
	
	/** Statistics of the segments of the chart. */
	public final ChartSegmentStatistics[] segmentStats; 
	
	/**
	 * Creates a new ChartData.
	 * @param ps               playerStatistics this data is for
	 * @param chartGranularity granularity of the chart data
	 */
	public ChartData( final PlayerStatistics ps, final ChartGranularity chartGranularity ) {
		this.chartGranularity = chartGranularity;
		
		final GregorianCalendar gc = new GregorianCalendar();
		gc.setTime( ps.firstDate );
		// time part is already 0
		
		final int presence = ps.getPresence();
		int estimatedMaxSegmentsCount;
		LadderSeason season = null;
		switch ( chartGranularity ) {
		default            :
		case DAY           : estimatedMaxSegmentsCount = presence          ; break;
		// Cannot use "gc.set( Calendar.DAY_OF_WEEK , Calendar.MONDAY )"
		// Because in the USA week starts with Sunday, and in case of SUNDAY the mentioned call
		// would set the next day instead of the past Monday!  
		case WEEK          : estimatedMaxSegmentsCount = 2 + presence / 7  ; gc.add( Calendar.DATE, -( ( gc.get( Calendar.DAY_OF_WEEK ) + 5 ) % 7 ) ); break; // Monday of the week
		case MONTH         : estimatedMaxSegmentsCount = 2 + presence / 31 ; gc.set( Calendar.DAY_OF_MONTH, 1 ); break;
		case YEAR          : estimatedMaxSegmentsCount = 2 + presence / 366; gc.set( Calendar.DAY_OF_YEAR , 1 ); break;
		case LADDER_SEASON : estimatedMaxSegmentsCount = LadderSeason.values().length; gc.setTime( ( season = LadderSeason.getByDate( ps.firstDate, ps.playerId.gateway ) ).startDate ); break;
		}
		if ( estimatedMaxSegmentsCount == 1 ) // If only 1 segment, we will add supplementaries
			estimatedMaxSegmentsCount = 3;
		
		// Lower dates of the segments
		final List< Date > segmentDateList = new ArrayList< Date >( estimatedMaxSegmentsCount );
		segmentCycle:
		while ( !gc.getTime().after( ps.lastDate ) ) {
			segmentDateList.add( gc.getTime() );
			switch ( chartGranularity ) {
			case DAY           : gc.add( Calendar.DATE , 1 ); break;
			case WEEK          : gc.add( Calendar.DATE , 7 ); break;
			case MONTH         : gc.add( Calendar.MONTH, 1 ); break;
			case YEAR          : gc.add( Calendar.YEAR , 1 ); break;
			case LADDER_SEASON : {
				season = LadderSeason.getNextSeason( season );
				if ( season == null ) // last season
					break segmentCycle;
				gc.setTime( season.startDate );
				break;
			}
			}
		}
		
		// If only 1 chart point, add dummy chart points before that and after that to make it look nicer (and more meaningful)
		if ( segmentDateList.size() == 1 ) {
			if ( chartGranularity == ChartGranularity.LADDER_SEASON ) {
				if ( season == null ) {
					// The only season is the last season!
					final LadderSeason[] values = LadderSeason.values();
					for ( int i = 2; i <= 3; i++ )
						segmentDateList.add( 0, values[ values.length - i ].startDate );
				}
				else {
					// The only season is not the last season!
					segmentDateList.add( season.startDate );
					if ( season.ordinal() > 1 ) // If season is not the first, there is a previous season to add before
						segmentDateList.add( 0, LadderSeason.getPreviousSeason( LadderSeason.getPreviousSeason( season ) ).startDate );
					else // If it was the first, no previous, add a next to the end instead 
						segmentDateList.add( LadderSeason.getNextSeason( season ).startDate );
				}
			}
			else {
				segmentDateList.add( gc.getTime() );
				switch ( chartGranularity ) {
				case DAY   : gc.add( Calendar.DATE , -2  ); break;
				case WEEK  : gc.add( Calendar.DATE , -14 ); break;
				case MONTH : gc.add( Calendar.MONTH, -2  ); break;
				case YEAR  : gc.add( Calendar.YEAR , -2  ); break;
				}
				segmentDateList.add( 0, gc.getTime() );
			}
		}
		
		segmentStats = new ChartSegmentStatistics[ segmentDateList.size() ];
		for ( int i = segmentStats.length - 1; i >= 0; i-- )
			segmentStats[ i ] = new ChartSegmentStatistics( segmentDateList.get( i ) );
		
		final int maxSegmentIndex = segmentStats.length - 1;
		int segment = 0;
		for ( final PlayerGameParticipationStats pgps : ps.playerGameParticipationStatsList ) {
			while ( segment < maxSegmentIndex && !segmentStats[ segment + 1 ].lowerDate.after( pgps.date ) )
				segment++;
			final ChartSegmentStatistics css = segmentStats[ segment ];
			
			css.record.totalGames++;
			if ( pgps.isWinner != null )
				if ( pgps.isWinner )
					css.record.wins++;
				else
					css.record.losses++;
			css.timeSecInGameForApm   += pgps.timeSecInGameForApm;
			css.totalActions          += pgps.actions;
			css.totalEffectiveActions += pgps.effectiveActions;
			
			// Average spawning ratio and average injection gap
			css.totalHatchTime         += pgps.totalHatchTime;
			css.totalHatchSpawnTime    += pgps.totalHatchSpawnTime;
			css.totalInjectionGap      += pgps.totalInjectionGap;
			css.totalInjectionGapCount += pgps.totalInjectionGapCount;
			
			// Race distribution data
			final IntHolder raceCounter = css.raceDistributionMap.get( pgps.race );
			if ( raceCounter == null )
				css.raceDistributionMap.put( pgps.race, new IntHolder( 1 ) );
			else
				raceCounter.value++;
			// Game type distribution data
			final IntHolder gameTypeCounter = css.gameTypeDistributionMap.get( pgps.gameType );
			if ( gameTypeCounter == null )
				css.gameTypeDistributionMap.put( pgps.gameType, new IntHolder( 1 ) );
			else
				gameTypeCounter.value++;
			// Format distribution data
			final IntHolder formatCounter = css.formatDistributionMap.get( pgps.format );
			if ( formatCounter == null )
				css.formatDistributionMap.put( pgps.format, new IntHolder( 1 ) );
			else
				formatCounter.value++;
		}
	}
	
}
