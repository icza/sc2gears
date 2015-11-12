/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.apiuser.client.beans;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * API call stat filters.
 * 
 * @author Andras Belicza
 */
public class ApiCallStatFilters implements IsSerializable {
	
	private String fromDay;
	private String toDay;
	
	@Override
	public String toString() {
	    final StringBuilder builder = new StringBuilder();
		
		if ( fromDay != null )
	    	builder.append( ", fromDay: " ).append( fromDay );
		if ( toDay != null )
	    	builder.append( ", toDay: " ).append( toDay );
		
		return builder.length() > 0 ? builder.substring( 2 ) : "";
	}
	
	public void setFromDay( String fromDay ) {
	    this.fromDay = fromDay;
    }

	public String getFromDay() {
	    return fromDay;
    }

	public void setToDay( String toDay ) {
	    this.toDay = toDay;
    }

	public String getToDay() {
	    return toDay;
    }

}
