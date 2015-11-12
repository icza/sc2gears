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

import java.util.Date;

/**
 * Player profile interface.
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see ProfileApi
 */
public interface IProfile {
	
	/** Interface version. */
	String VERSION = "3.1";
	
	/**
	 * Returns the time when the profile was updated at.
	 * @return the time when the profile was updated at
	 * @see InfoApi#getProfileInfoValidityTime()
	 */
	Date getUpdatedAt();
	
	/**
	 * Returns the time when all ranks info was updated at.<br>
	 * A 0 value ({@link Date#getTime()}) indicates that all ranks info is not retrieved.
	 * @return the time when all ranks info was updated at
	 * @see InfoApi#getProfileInfoValidityTime()
	 */
	Date getAllRanksUpdatedAt();
	
	/**
	 * Returns the group of the portrait.
	 * 
	 * <p>The portrait of a player is defined by a group, a row and a column.</p>
	 * 
	 * @return the group of the portrait
	 * @see #getPortraitRow()
	 * @see #getPortraitColumn()
	 */
	int getPortraitGroup();
	
	/**
	 * Returns the row of the portrait.
	 * 
	 * <p>The portrait of a player is defined by a group, a row and a column.</p>
	 * 
	 * @return the row of the portrait
	 * @see #getPortraitGroup()
	 * @see #getPortraitColumn()
	 */
	int getPortraitRow();
	
	/**
	 * Returns the column of the portrait.
	 * 
	 * <p>The portrait of a player is defined by a group, a row and a column.</p>
	 * 
	 * @return the column of the portrait
	 * @see #getPortraitGroup()
	 * @see #getPortraitRow()
	 */
	int getPortraitColumn();
	
	/**
	 * Returns the achievement points of the player.
	 * @return the achievement points of the player
	 */
	int getAchievementPoints();
	
	/**
	 * Returns the number of terran wins.
	 * @return the number of terran wins
	 * @since "3.1"
	 */
	int getTerranWins();
	
	/**
	 * Returns the number of zerg wins.
	 * @return the number of zerg wins
	 * @since "3.1"
	 */
	int getZergWins();
	
	/**
	 * Returns the number of protoss wins.
	 * @return the number of protoss wins
	 * @since "3.1"
	 */
	int getProtossWins();
	
	/**
	 * Returns the number of games from this season.
	 * @return the number of games from this season
	 * @since "3.0"
	 */
	int getGamesThisSeason();
	
	/**
	 * Returns the number of total career games.
	 * @return the number of total career games
	 * @since "3.0"
	 */
	int getTotalCareerGames();
	
	/**
	 * Returns the highest solo finish league.
	 * @return the highest solo finish league
	 * @since "3.0"
	 * @see League
	 */
	League getHighestSoloFinishLeague();
	
	/**
	 * Returns the highest team finish league.
	 * @return the highest team finish league
	 * @since "3.0"
	 * @see League
	 */
	League getHighestTeamFinishLeague();
	
	/**
	 * Returns how many times the highest solo finish league has been achieved.
	 * @return how many times the highest solo finish league has been achieved
	 * @since "3.0"
	 */
	int getHighestSoloFinishTimes();
	
	/**
	 * Returns how many times the highest team finish league has been achieved.
	 * @return how many times the highest team finish league has been achieved
	 * @since "3.0"
	 */
	int getHighestTeamFinishTimes();
	
	/**
	 * Returns the best ranks of the player.
	 * 
	 * <p>The index is the bracket:
	 * <ul>
	 * 		<li>0 => 1v1
	 * 		<li>1 => 2v2
	 * 		<li>2 => 3v3
	 * 		<li>3 => 4v4
	 * </ul>
	 * 
	 * @return the best ranks of the player
	 * @see IBestTeamRank
	 */
	IBestTeamRank[] getBestRanks();
	
	/**
	 * Returns all ranks of the player.
	 * 
	 * <p>The first index is the bracket:
	 * <ul>
	 * 		<li>0 => 1v1
	 * 		<li>1 => 2v2
	 * 		<li>2 => 3v3
	 * 		<li>3 => 4v4
	 * </ul>
	 * Team ranks inside a bracket are ordered by the ranks: highest ranks are in the beginning of the array.
	 * The exact order is specified by the <code>ITeamRank.compareTo(ITeamRank)</code>.</p>
	 * 
	 * <p>This method always returns a non-null array even if all ranks info is not available,
	 * but if all ranks info is not available, the elements of the array will be <code>null</code>.
	 * If all ranks info is available but there are no ranks for a bracket (1v1 for example),
	 * the respective array element can either be <code>null</code> or a 0-length array.<br>
	 * Whether all ranks info is available can be checked with the {@link IProfile#getAllRanksUpdatedAt()}.</p>
	 * 
	 * <p>The name <code>getAllRank<b>ss</b></code> with the double plural ending is intentional:
	 * indicates that the returned value is a 2-dimensional array.</p>
	 * 
	 * @return all ranks of the player
	 * @see ITeamRank
	 * @see #getAllRanksUpdatedAt()
	 */
	ITeamRank[][] getAllRankss();
	
}
