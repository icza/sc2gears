/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.user.client.beans;

import hu.belicza.andras.sc2gearsdb.common.client.beans.DateRangeFilters;

/**
 * Other file filters.
 * 
 * @author Andras Belicza
 */
public class OtherFileFilters extends DateRangeFilters {
	
	private String extension;

	@Override
	public String toString() {
	    final StringBuilder builder = new StringBuilder();
		
	    if ( extension != null )
	    	builder.append( ", extension: " ).append( extension );
		
		final String superToString = super.toString();
		if ( !superToString.isEmpty() )
			builder.append( ", " ).append( superToString );
		
		return builder.length() > 0 ? builder.substring( 2 ) : "";
	}
	
	public void setExtension( String extension ) {
	    this.extension = extension;
    }

	public String getExtension() {
	    return extension;
    }
	
}
