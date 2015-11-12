/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.mousepracticegame;

/**
 * Rules of the game.
 * 
 * @author Andras Belicza
 */
class Rules {
	
	public final int     sleepBetweenIterations;
	public final int     maxDiscRadius;
	public final int     maxDiscAge;
	public final int     maxDiscScore;
	public final int     maxDiscsMissed;
	public final float   discSpeed; // pixel/iteration
	public final int     friendlyDiscProbability;
	public final int     initialDelayForNewDisc;
	public final int     newDiscDelayDecrement; // ms/disc
	public final Long    randomSeed;
	public final boolean paintDiscCenterCross;
	public final boolean paintMaxDiscOutline;
	
	/**
	 * Creates a new Rules.
	 */
	public Rules( final int fps, final int maxDiscRadius, final int maxDiscAge, final int maxDiscScore,
				  final int maxDiscsMissed, final int discSpeed, final int friendlyDiscProbability,
				  final int initialDelayForNewDisc, final int newDiscDelayDecrement, final String randomSeed,
				  final boolean paintDiscCenterCross, final boolean paintMaxDiscOutline ) {
		
		sleepBetweenIterations       = 1000 / fps;
		this.maxDiscRadius           = maxDiscRadius;
		this.maxDiscAge              = maxDiscAge;
		this.maxDiscScore            = maxDiscScore;
		this.maxDiscsMissed          = maxDiscsMissed;
		this.discSpeed               = (float) discSpeed * sleepBetweenIterations / 1000;
		this.friendlyDiscProbability = friendlyDiscProbability;
		this.initialDelayForNewDisc  = initialDelayForNewDisc;
		this.newDiscDelayDecrement   = newDiscDelayDecrement;
		
		this.paintDiscCenterCross    = paintDiscCenterCross;
		this.paintMaxDiscOutline     = paintMaxDiscOutline;
		
		if ( randomSeed == null )
			this.randomSeed = null;
		else {
			Long randomSeed_ = null;
			try {
				randomSeed_ = Long.parseLong( randomSeed );
			} catch ( final NumberFormatException nfe ) {
			}
			this.randomSeed = randomSeed_;
		}
	}
	
}
