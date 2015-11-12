/*
/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.common.client;

/**
 * A general/generic filter.
 * 
 * @param T type of the filterable object
 * 
 * @author Andras Belicza
 */
public interface Filter< T > {
	
	/**
	 * Tests if the specified object is accepted by the filter.
	 * @param value value to be tested
	 * @return true of the specified object is accepted by the filter; false otherwise
	 */
	boolean accept( T value );
	
}
