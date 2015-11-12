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
 * API of the common module.
 * 
 * @author Andras Belicza
 */
public class CommonApi {
	
	/** Page sizes for the data tables. */
	public static final int[] PAGE_SIZES    = { 20, 50, 100 };
	
	/** Max page size for the data tables. */
	public static final int   MAX_PAGE_SIZE = PAGE_SIZES[ PAGE_SIZES.length-1 ];
	
}
