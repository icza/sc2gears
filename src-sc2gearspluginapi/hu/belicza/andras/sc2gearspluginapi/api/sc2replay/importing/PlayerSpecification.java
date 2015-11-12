/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.sc2replay.importing;

/**
 * Specification of a player. Contains info that are needed to create a virtual player.
 * 
 * <p>Build order text format specification can be found
 * <a href="https://sites.google.com/site/sc2gears/features/build-order-import">here</a>.</p>
 * 
 * @author Andras Belicza
 * 
 * @see ReplaySpecification
 */
public class PlayerSpecification {
	
	/** Name of the player. */
	public String name;
	
	/** Team of the player. */
	public int    team;
	
	/**
	 * Build order text specifying the actions of the player.
	 * The build order text format specification can be found
	 * <a href="https://sites.google.com/site/sc2gears/features/build-order-import">here</a>.
	 */
	public String buildOrderText;
	
}
