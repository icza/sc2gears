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

import java.util.Date;

/**
 * Statistics of words.
 * 
 * @author Andras Belicza
 */
class WordStatistics extends DateRangeStatistics {
	
	/** Word. */
	public final String word;
	
	/** Count of the word. */
	public int count;
	
	/** Number of replays the word occurred in. */
	public int replays;
	
	/**
	 * Creates a new WordStatistics.
	 * @param word word
	 */
	public WordStatistics( final String word ) {
		this.word = word;;
	}
	
	/**
	 * Builds in the specified count and date.
	 * @param count count to be added
	 * @param date  date to be included
	 */
	public void buildInCount( final int count, final Date date ) {
		registerDate( date );
		this.count += count;
		replays++;
	}
	
}
