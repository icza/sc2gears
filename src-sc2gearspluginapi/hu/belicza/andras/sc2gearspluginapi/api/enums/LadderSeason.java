/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.enums;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Gateway;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Ladder season.
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 */
public enum LadderSeason {
	
	/** Unknown ladder season.                   */
	UNKNOWN ( "1970-01-01", true ),
	/** Beta testing.
	 * Beta testing opened on 17, February 2010. */
	BETA    ( "2010-02-16", true ),
	/** Season #1.                               */
	// First season: unlike other seasons, started at the same day local time (July 27), -2 days to cover world-wide release
	// This does not cause errors, because there was a 2-week silent period before the launch (no games).
	SEASON1 ( "2010-07-25", true ),
	/** Season #2.                               */
	SEASON2 ( "2011-03-29", true ),
	/** Season #3.                               */
	SEASON3 ( "2011-07-26", true ),
	/** Season #4.                               */
	SEASON4 ( "2011-10-25", true ),
	/** Season #5.                               */
	SEASON5 ( "2011-12-20", true ),
	/** Season #6.                               */
	SEASON6 ( "2012-02-14", true ),
	/** Season #7.                               */
	SEASON7 ( "2012-04-10", true ),
	/** Season #8.                               */
	SEASON8 ( "2012-06-12", true ),
	/** Season #9.                               */
	SEASON9 ( "2012-09-11", true ),
	/** Season #10.                              */
	SEASON10( "2012-11-01", false ),
	/** Season #11.                              */
	SEASON11( "2013-01-02", true ),
	/** Season #12. HotS launch.                 */
	SEASON12( "2013-03-12", true ),
	/** Season #13.                              */
	SEASON13( "2013-04-30", true ),
	/** Season #14.                              */
	SEASON14( "2013-06-11", true ),
	/** Season #15.                              */
	SEASON15( "2013-08-27", true ),
	/** Season #16.                              */
	SEASON16( "2013-11-12", true ),
	/** Season #17.                              */
	SEASON17( "2014-01-02", true ),
	/** Season #18.                              */
	SEASON18( "2014-03-11", true ),
	/** Season #18: Unknown start date.          */
	SEASON19( "2099-01-01", false );
	
	/**
	 * Start date of the season.
	 * Interpretation:
	 * <ul>
	 *  <li>When <b>applyStartTimeOffset=true:</b> Seasons start slightly at different times on different servers (gateways).
	 *      This date is a reference day of the season start day: Tuesday GMT (time is set to zero).
	 *      Gateway's real season start time is off by {@link Gateway#seasonStartTimeOffset}.
	 *  <li>When <b>applyStartTimeOffset=false:</b> This date is the world-wide season start date (specified in GMT time zone, time is set to zero).
	 *      Gateway's real season start date is off by {@link Gateway#timeZoneOffset}. Start time is specified to be 3 AM on this date.
	 * </ul>
	 */
	public final Date   startDate;
	/** Cache of the string value.   */
	public final String stringValue;
	/** Number string of the season. */
	public final String seasonNumber;
	/**
	 * Tells if gateway-specific general season start time offset has to be applied in this season.
	 * If this value is false, season starts on <code>startDate</code> in the gateway's local time zone.
	 * @since "3.0.1"
	 */
	public final boolean applyStartTimeOffset;
	
	/**
	 * Creates a new LadderSeason.
	 * @param startDateString      start date of the season in string format
	 * @param applyStartTimeOffset tells if gateway-specific general season start time offset has to be applied in this season
	 */
	private LadderSeason( final String startDateString, final boolean applyStartTimeOffset ) {
		try {
			// Start date string must be parsed using a fixed time zone! Start time offsets are now handled!
			final SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd" );
			format.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
			startDate = format.parse( startDateString );
		} catch ( final ParseException pe ) {
			// This should never happen
			throw new RuntimeException( "Fix the ladder season dates!", pe );
		}
		
		final int ordinal = ordinal();
		stringValue = ordinal > 1 ? Language.getText( "module.multiRepAnal.tab.player.tab.charts.granularity.ladderSeason.season", ordinal - 1 )
				: ordinal == 1 ? Language.getText( "module.multiRepAnal.tab.player.tab.charts.granularity.ladderSeason.beta" )
				: Language.getText( "general.unknown" );
		
		seasonNumber = ordinal > 1 ? Integer.toString( ordinal - 1 ) : stringValue.substring( 0, 1 );
		this.applyStartTimeOffset = applyStartTimeOffset;
	}
	
	/** Cache of the values. */
	private final static LadderSeason[] VALUES = values();
	
	/**
	 * Returns the ladder season specified by its the start date.
	 * 
	 * <p>The implementation allows a little uncertainty: shooting within 4 days before or after the exact start date will be accepted,
	 * for example if you pass <code>startDate="2010-07-28"</code>, {@link #SEASON1} will be returned.</p>
	 * 
	 * @param startDate start date of the ladder season to be returned
	 * @return the ladder season specified by its start date
	 */
	public static LadderSeason getByStartDate( final Date startDate ) {
		for ( final LadderSeason ls : VALUES )
			if ( Math.abs( ls.startDate.getTime() - startDate.getTime() ) < 4L * 24*60*60*1000 )
				return ls;
		
		return UNKNOWN;
	}
	
	/**
	 * Returns the ladder season for the specified date.
	 * 
	 * @deprecated Deprecated as of plugin API version "2.7.3". Since seasons start slightly at different times on different servers (gateways),
	 * 		determining the season for a date might be ambiguous. Use the {@link #getByDate(Date, Gateway)} method instead.
	 * 
	 * @param date date to return ladder season for
	 * @return the ladder season for the specified date
	 */
	public static LadderSeason getByDate( final Date date ) {
		for ( int i = VALUES.length - 1; i >= 0; i-- )
			if ( !date.before( VALUES[ i ].startDate ) )
				return VALUES[ i ];
		
		return UNKNOWN;
	}
	
	/**
	 * Returns the ladder season for the specified date on the specified gateway.
	 * 
	 * @param date    date to return ladder season for
	 * @param gateway gateway where to determine the season for the specified date
	 * 
	 * @return the ladder season for the specified date on the specified gateway
	 * 
	 * @since "2.7.3"
	 */
	public static LadderSeason getByDate( final Date date, final Gateway gateway ) {
		final long startOffsetTimeToCheck = date.getTime() - gateway.seasonStartTimeOffset;
		final long timeZoneTimeToCheck    = date.getTime() + gateway.timeZoneOffset - 3*60*60*1000; // After midnight there's usually a few hours service maintenance, after that new season starts
		
		for ( int i = VALUES.length - 1; i >= 0; i-- ) {
			if ( ( VALUES[ i ].applyStartTimeOffset ? startOffsetTimeToCheck : timeZoneTimeToCheck ) >= VALUES[ i ].startDate.getTime() )
				return VALUES[ i ];
		}
		
		return UNKNOWN;
	}
	
	/**
	 * Returns the next season after the specified season.
	 * @param season season to return the next one to
	 * @return the next season after the specified season; or <code>null</code> if the specified season is the last
	 */
	public static LadderSeason getNextSeason( final LadderSeason season ) {
		if ( season.ordinal() == VALUES.length - 1 )
			return null;
		
		return VALUES[ season.ordinal() + 1 ];
	}
	
	/**
	 * Returns the previous season before the specified season.
	 * @param season season to return the previous one to
	 * @return the previous season before the specified season; or <code>null</code> if the specified season is the first
	 */
	public static LadderSeason getPreviousSeason( final LadderSeason season ) {
		if ( season.ordinal() == 0 )
			return null;
		
		return VALUES[ season.ordinal() - 1 ];
	}
	
	@Override
	public String toString() {
		return stringValue;
	};
	
}
