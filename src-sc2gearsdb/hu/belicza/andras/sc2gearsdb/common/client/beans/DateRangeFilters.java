/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.common.client.beans;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Date range filters.
 * 
 * @author Andras Belicza
 */
public class DateRangeFilters implements IsSerializable {
	
	private Date fromDate;
	private Date toDate;
	
	@Override
	public String toString() {
	    final StringBuilder builder = new StringBuilder();
		
		if ( fromDate != null )
	    	builder.append( ", fromDate: " ).append( fromDate );
		if ( toDate != null )
	    	builder.append( ", toDate: " ).append( toDate );
		
		return builder.length() > 0 ? builder.substring( 2 ) : "";
	}
	
	public void setFromDate( Date fromDate ) {
	    this.fromDate = fromDate;
    }

	public Date getFromDate() {
	    return fromDate;
    }

	public void setToDate( Date toDate ) {
	    this.toDate = toDate;
    }

	public Date getToDate() {
	    return toDate;
    }

}
