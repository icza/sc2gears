/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.profile;

import hu.belicza.andras.sc2gearspluginapi.api.enums.League;

/**
 * Team rank: represents the rank of a team.
 * 
 * <p>This interface extends {@link Comparable} to define the following rank order:
 * <ol>
 * 		<li>higher leagues before lower ones
 * 		<li>lower ranks before the higher ones
 * </ol></p>
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see IProfile
 */
public interface ITeamRank extends Comparable< ITeamRank > {
	
	/** Interface version. */
	String VERSION = "3.0";
	
	/**
	 * Returns the league of the team.
	 * @return the league of the team
	 * @see League
	 */
	League getLeague();
	
	/**
	 * Returns the rank of the team's division.
	 * @return the rank of the team's division
	 */
	int getDivisionRank();
	
	/**
	 * Returns the names of the team members.
	 * 
	 * <p>The returned array always contains the full team including the player whose profile info references this team rank.</p>
	 * 
	 * @return the names of the team members
	 */
	String[] getTeamMembers();
	
	/**
	 * Returns a comma separated list of the names of the team members.
	 * 
	 * <p>The returned array always contains the full team including the player whose profile info references this team rank.</p>
	 * 
	 * @return the names of the team members
	 * 
	 * @since "3.0"
	 */
	String getTeamMembersString();
	
}
