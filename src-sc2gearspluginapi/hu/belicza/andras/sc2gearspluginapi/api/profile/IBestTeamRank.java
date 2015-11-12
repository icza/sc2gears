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

/**
 * Best team rank: represents the best team rank.
 * 
 * Note: the division rank and team members properties are only populated if all team ranks
 * info of the profile is available.
 * 
 * @since "3.1"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see IProfile
 */
public interface IBestTeamRank extends ITeamRank {
	
	/** Interface version. */
	String VERSION = "3.1";
	
	/**
	 * Returns the class of the league.
	 * Class is in the range of 1..4 where 1 is the bottom of the ladder, 4 is the top.
	 * @return the number of games
	 */
	int getLeagueClass();
	
	/**
	 * Returns the number of games.
	 * @return the number of games
	 */
	int getGames();
	
	/**
	 * Returns the number of wins.
	 * @return the number of wins
	 */
	int getWins();
	
	/**
	 * Returns the number of all games of this format (e.g. 2v2) not just the games of the best ranked team.
	 * @return the number of all games of this format (e.g. 2v2) not just the games of the best ranked team.
	 */
	int getGamesOfFormat();
	
}
