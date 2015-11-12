/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.sc2replay;

import hu.belicza.andras.sc2gearspluginapi.api.ReplayFactoryApi;
import hu.belicza.andras.sc2gearspluginapi.api.enums.League;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Difficulty;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerColor;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;

import java.awt.Color;
import java.awt.Point;

/**
 * Player interface of StarCraft II replays.
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see IReplay
 */
public interface IPlayer {
	
	/** Interface version. */
	String VERSION = "4.2";
	
	/** Indication of unknown team. */
	int TEAM_UNKNOWN = Integer.MAX_VALUE;
	
	/**
	 * Returns the player identifier.
	 * @return the player identifier
	 * @see IPlayerId
	 */
	IPlayerId getPlayerId();
	
	/**
	 * Returns the name with clan name included.
	 * @return the name with clan name included
	 * @since "3.2"
	 */
	String getNameWithClan();
	
	/**
	 * Returns the 1v1 league of the player <i>at the time of the game</i>.<br>
	 * This info is only available from replay version 2.0.
	 * @return the 1v1 league of the player
	 * @since "4.2"
	 */
	League getLeague();
	
	/**
	 * Returns the swarm levels of the player <i>at the time of the game</i>.<br>
	 * This info is only available from replay version 2.0.
	 * @return the swarm levels of the player
	 * @since "4.2"
	 */
	int getSwarmLevels();
	
	/**
	 * Returns the localised race string as found in the replay file.
	 * @return the race string as found in the replay file
	 * @see #getRace()
	 * @see #getFinalRace()
	 * @see #getRaceString()
	 */
	String getLocalizedRaceString();
	
	/**
	 * Returns the ARGB (Alpha, Red, Green, Blue) components of the color of the player.
	 * @return the ARGB (Alpha, Red, Green, Blue) components of the color of the player
	 * @see #getPlayerColor()
	 * @see #getColor()
	 */
	int[] getArgbColor();
	
	/**
	 * Returns the frame of the last action of the player.
	 * @return the frame of the last action of the player
	 */
	int getLastActionFrame();
	
	/**
	 * Returns the number of actions of the player.
	 * @return the number of actions of the player
	 */
	int getActionsCount();
	
	/**
	 * Returns the number of excluded actions from APM calculation.
	 * @return the number of excluded actions from APM calculation
	 */
	int getExcludedActionsCount();
	
	/**
	 * Returns the number of effective actions of the player.
	 * 
	 * <p>See <a href="https://sites.google.com/site/sc2gears/features/replay-analyzer/apm-types">APM Types</a> for EAPM algorithm details.</p>
	 * 
	 * @return the number of effective actions of the player
	 * @since "2.5"
	 */
	int getEffectiveActionsCount();
	
	/**
	 * Returns the number of excluded effective actions from EAPM calculation.
	 * 
	 * <p>See <a href="https://sites.google.com/site/sc2gears/features/replay-analyzer/apm-types">APM Types</a> for EAPM algorithm details.</p>
	 * 
	 * @return the number of excluded effective actions from EAPM calculation
	 * @since "2.5"
	 */
	int getExcludedEffectiveActionsCount();
	
	/**
	 * Returns the type of the player.
	 * @return the type of the player
	 * @see PlayerType
	 */
	PlayerType getType();
	
	/**
	 * Returns the race of the player.<br>
	 * If the player chose Random, this will return {@link Race#RANDOM}.
	 * @return the race of the player
	 * @see Race
	 * @see #getFinalRace()
	 * @see #getLocalizedRaceString()
	 * @see #getRaceString()
	 */
	Race getRace();
	
	/**
	 * Returns the final race of the player.<br>
	 * If race is <code>null</code>, {@link Race#UNKNOWN} or {@link Race#RANDOM}, we try to interpret the localized value.
	 * @return the final race of the player
	 * @see Race
	 * @see #getRace()
	 * @see #getLocalizedRaceString()
	 * @see #getRaceString()
	 */
	Race getFinalRace();
	
	/**
	 * Returns the team of the player.<br>
	 * If team is unknown, {@link #TEAM_UNKNOWN} will be returned.
	 * @return the team of the player
	 */
	int getTeam();
	
	/**
	 * Returns the start location of the player.
	 * 
	 * <p>This will return <code>null</code> if extended map info is not parsed
	 * (see {@link ReplayFactoryApi#parseReplay(String, java.util.Set)}).</p>
	 * 
	 * @return the start location of the player
	 * @see ReplayFactoryApi#parseReplay(String, java.util.Set)
	 * @see IMapInfo#getStartLocationList()
	 */
	Point getStartLocation();
	
	/**
	 * Returns the difficulty in case of of computer (AI) players.
	 * @return the difficulty in case of of computer (AI) players
	 * @see Difficulty
	 */
	Difficulty getDifficulty();
	
	/**
	 * Returns the color of the player.
	 * @return the color of the player
	 * @see PlayerColor
	 * @see #getArgbColor()
	 * @see #getColor()
	 */
	PlayerColor getPlayerColor();
	
	/**
	 * Returns the handicap of the player.
	 * @return the handicap of the player
	 */
	int getHandicap();
	
	/**
	 * Returns whether this player is a winner in the replay.
	 * 
	 * <p><i>Note: specifying to parse the game events increases the probability to determine the proper winners
	 * (see {@link ReplayFactoryApi#parseReplay(String, java.util.Set)}).</i></p>
	 * 
	 * @return <code>Boolean.TRUE</code> if player is a winner, <code>Boolean.FALSE</code> if player is a loser, <code>null</code> if unknown
	 */
	Boolean isWinner();
	
	/**
	 * Returns the race as a string.
	 * 
	 * <p>If the player chose {@link Race#RANDOM}, the returned value will start with random and the final race in parenthesis;
	 * else only the final race.</p>
	 * 
	 * @return the race as a string
	 * @see #getLocalizedRaceString()
	 * @see #getRace()
	 * @see #getFinalRace()
	 */
	String getRaceString();
	
	/**
	 * Returns the letter of the player's race.
	 * 
	 * <p>If final race is concrete, its letter will be returned. Else if final race is {@link Race#RANDOM},
	 * then the letter of random is returned. Else the letter of {@link Race#UNKNOWN}.</p>
	 * 
	 * @return the letter of the player's race
	 */
	char getRaceLetter();
	
	/**
	 * Returns the color of the player.
	 * @return the color of the player
	 * @see #getPlayerColor()
	 * @see #getArgbColor()
	 */
	Color getColor();
	
	/**
	 * Returns a darker version of the player's color.
	 * @return a darker version of the player's color
	 * @see #getColor()
	 */
	Color getDarkerColor();
	
	/**
	 * Returns a brighter version of the player's color.
	 * @return a brighter version of the player's color
	 * @see #getColor()
	 */
	Color getBrighterColor();
	
	/**
	 * Returns the name of the player's color.
	 * 
	 * <p>The name contains the name of the color and the comma separated decimal RGB values enumerated in parenthesis after the name,
	 * for example: <code>"Red (180,20,30)"</code>. In case of {@link PlayerColor#UNKNOWN} the player name is left out.</p>
	 * 
	 * @return the name of the player's color
	 * @see #getPlayerColor()
	 */
	String getColorName();
	
	/**
	 * Returns the total hatchery life time in frames in relevance to the average spawning ratio.
	 * @return the total hatchery life time in frames in relevance to the average spawning ratio
	 * @since "2.6"
	 * @see #getTotalHatchSpawnTime()
	 */
	int getTotalHatchTime();
	
	/**
	 * Returns the total time in frames when hatcheries were spawning larva.
	 * @return the total time in frames when hatcheries were spawning larva
	 * @since "2.6"
	 * @see #getTotalHatchTime()
	 * @see #getAverageSpawningRatio()
	 */
	int getTotalHatchSpawnTime();
	
	/**
	 * Returns the total injection gap time in frames between injections in relevance to the average injection gap.<br>
	 * A gap is the time window between the end of a spawn larva and the start of the next spawn larva.
	 * @return the total injection gap time in frames between injections in relevance to average injection gap
	 * @since "2.6"
	 * @see #getTotalInjectionGapCount()
	 */
	int getTotalInjectionGap();
	
	/**
	 * Returns the total number of injection gaps in relevance to the average injection gap.
	 * @return the total number of injection gaps in relevance to the average injection gap
	 * @since "2.6"
	 * @see #getTotalInjectionGap()
	 */
	int getTotalInjectionGapCount();
	
	/**
	 * Returns the average spawning ratio.
	 * <p>avg. spawning ratio = total hatch spawn time / total hatch time</p>
	 * <p>The returned value is in the range of 0..1 where 0 is the worst, 1 is the best.</p>
	 * 
	 * @return the average spawning ratio; or <code>null</code> if the average spawning ratio cannot be calculated
	 * @since "2.6"
	 * @see #getTotalHatchTime()
	 * @see #getTotalHatchSpawnTime()
	 */
	Float getAverageSpawningRatio();
	
	/**
	 * Returns the average injection gap in frames.
	 * <p>avg. injection gap = total injection gap / total injection gap count</p>
	 * <p>0 return value is the optimal best.</p>
	 * 
	 * @return the average injection gap in frames; or <code>null</code> if the average injection gap cannot be calculated
	 * @since "2.6"
	 * @see #getTotalInjectionGap()
	 * @see #getTotalInjectionGapCount()
	 */
	Float getAverageInjectionGap();
	
}
