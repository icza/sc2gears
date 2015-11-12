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

import hu.belicza.andras.sc2gears.util.NullAwareComparable;

/**
 * Game record.
 * 
 * @author Andras Belicza
 */
class Record implements Comparable< Record > {
	
	/** Number of total games. */
	public int totalGames;
	/** Number of wins.        */
	public int wins;
	/** Number of losses.      */
	public int losses;
	
	/**
	 * Returns a string representation of the record in the form of:
	 * <pre>W-L-U</pre>
	 * where W = wins, L = losses, U = unknown games.
	 */
	@Override
	public String toString() {
		return wins + "-" + losses + "-" + ( totalGames - wins - losses );
	}
	
	/**
	 * Returns the win ratio of this record:
	 * <pre>wins / (wins+losses)</pre>
	 * The returned value is in percent.
	 * @return the win ratio of this record
	 */
	public NullAwareComparable< Integer > getWinRatio() {
		// If all is unknown, wins+losses=0!
		return NullAwareComparable.getPercent( wins == 0 && losses == 0 ? null : new Integer( wins * 100 / ( wins + losses ) ) );
	}
	
	/**
	 * Implements an order of (wins-losses).
	 * <p>This logic takes the number of played games into consideration contrary to the Win % column.
	 * The result order can be used to decide which maps to veto down for example.</p>
	 * 
	 * <p><i>Example:</i> a record of 4-36-0 (win rate = 11%) will be after a record of 1-10-0 (win rate = 10%) reflecting that a map of 4-36 record gives you more lost points than a map with a record of 1-10.<br>
	 * <i>Another example:</i> a record of 10-100-0 will be after a record of 1-10-0 even though having the same 10% win rate reflecting that 10-100 record would give you a lot more lost points.
	 * </p>
	 */
	@Override
    public int compareTo( final Record r ) {
	    return ( wins - losses ) - ( r.wins - r.losses );
    }
	
}
