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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Represents a statistics with a date range: first and last date. Operates on dates only (cuts off time).
 * 
 * @author Andras Belicza
 */
class DateRangeStatistics {

	/** Milliseconds in a day. */
	private static final long MILLIS_IN_A_DAY = 24L*60*60*1000;
	
	/** First date. */
	public Date firstDate;
	/** Last date.  */
	public Date lastDate;
	
	/**
	 * Registers a new date.
	 * @param date new date to be registered
	 */
	public void registerDate( final Date date ) {
		GregorianCalendar gc = null;
		if ( firstDate == null || firstDate.after( date ) ) {
			gc = new GregorianCalendar();
			gc.setTime( date );
			gc.set( Calendar.HOUR_OF_DAY, 0 );
			gc.set( Calendar.MINUTE     , 0 );
			gc.set( Calendar.SECOND     , 0 );
			gc.set( Calendar.MILLISECOND, 0 );
			firstDate = gc.getTime();
		}
		if ( lastDate == null || lastDate.before( date ) ) {
			if ( gc == null ) {
				gc = new GregorianCalendar();
				gc.setTime( date );
			}
			gc.set( Calendar.HOUR_OF_DAY, 23  );
			gc.set( Calendar.MINUTE     , 59  );
			gc.set( Calendar.SECOND     , 59  );
			gc.set( Calendar.MILLISECOND, 999 );
			lastDate = gc.getTime();
		}
	}
	
	/**
	 * Returns the presence in days (difference between firstDate and lastDate).<br>
	 * Presence is at least 1 day.
	 * @return the presence in days (difference between firstDate and lastDate)
	 */
	public int getPresence() {
		return 1 + (int) ( ( lastDate.getTime() - firstDate.getTime() ) / MILLIS_IN_A_DAY );
	}
	
}
