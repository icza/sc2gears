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

import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Format;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Statistics of a player-game participation.
 * 
 * @author Andras Belicza
 */
class PlayerGameParticipationStats implements Comparable< PlayerGameParticipationStats > {
	
	/** Date of the game.                               */
	public final Date      date;
	/** Start date of the game.                         */
	public final Date      startDate;
	/** Game format.                                    */
	public final Format    format;
	/** Time (seconds) in the game.                     */
	public final int       timeSecInGame;
	/** Time (seconds) in the game for APM calculation. */
	public final int       timeSecInGameForApm;
	/** Actions count of the player.                    */
	public final int       actions;
	/** Effective actions count of the player.          */
	public final int       effectiveActions;
	/** Tells if the player was a winner in the game.
	 * <code>null</code> means unknown.                 */
	public final Boolean   isWinner;
	/** Race of the player in the game.                 */
	public final Race      race;
	/** 1v1 opponent race.                              */
	public final Race      opponent1v1Race;
	/** Name of the map of the game.                    */
	public final String    mapName;
	/** Game speed.                                     */
	public final GameSpeed gameSpeed;
	
	/** Build order of the player in the game. */
	public String buildOrder;
	
	/** List of allies in the game.    */
	public final List< String > allyList;
	/** List of opponents in the game. */
	public final List< String > opponentList;
	
	/** Team league compositions. Each team has an element in the array.
	 * The first player of the first team is the player. */
	public final String[] teamLeagueCompositions;
	
	/** Team race compositions. Each team has an element in the array.
	 * The first player of the first team is the player. */
	public final String[] teamRaceCompositions;
	
	/** Game type. */
	public final GameType gameType;
	
	/** Total hatchery life time in frames in relevance to the average spawning ratio.                   */
	public final long totalHatchTime;
	/** Total time in frames when hatcheries were spawning larva.                                        */
	public final long totalHatchSpawnTime;
	/** Total injection gap time in frames between injections in relevance to the average injection gap. */
	public final long totalInjectionGap;
	/** Total number of injection gaps in relevance to the average injection gap.                        */
	public final int  totalInjectionGapCount;
	
	/** Word count map. */
	public final Map< String, IntHolder > wordCountMap;
	
	/**
	 * Creates a new PlayerGameParticipationStats.
	 * @param date                   date of the game
	 * @param date                   start date of the game
	 * @param format                 game format
	 * @param timeSecInGame          time in game
	 * @param timeSecInGameForApm    time in game for APM calculation
	 * @param player                 reference to the player
	 * @param opponent1v1            reference to the opponent in case of 1v1 format
	 * @param mapName                name of the map of the game
	 * @param teamLeagueCompositions team league compositions
	 * @param teamRaceCompositions   team race compositions
	 * @param gameType               game type
	 * @param totalInjectionGap      total injection gap (converted to real time if desired)
	 * @param wordCountMap           word count map
	 */
	public PlayerGameParticipationStats( final Date date, final Date startDate, final Format format, final int timeSecInGame, final int timeSecInGameForApm, final Player player, final Race opponent1v1Race, final String mapName, final GameSpeed gameSpeed, final String[] teamLeagueCompositions, final String[] teamRaceCompositions, final GameType gameType, final int totalInjectionGap, final Map< String, IntHolder > wordCountMap ) {
		this.date                   = date;
		this.startDate              = startDate;
		this.format                 = format;
		this.timeSecInGame          = timeSecInGame;
		this.timeSecInGameForApm    = timeSecInGameForApm;
		this.actions                = player.actionsCount - player.excludedActionsCount;
		this.effectiveActions       = player.effectiveActionsCount - player.excludedEffectiveActionsCount;
		this.isWinner               = player.isWinner;
		this.race                   = player.finalRace;
		this.opponent1v1Race        = opponent1v1Race;
		this.mapName                = mapName;
		this.gameSpeed              = gameSpeed;
		this.teamLeagueCompositions = teamLeagueCompositions;
		this.teamRaceCompositions   = teamRaceCompositions;
		this.gameType               = gameType;
		this.totalHatchTime         = player.totalHatchTime;
		this.totalHatchSpawnTime    = player.totalHatchSpawnTime;
		this.totalInjectionGap      = totalInjectionGap;
		this.totalInjectionGapCount = player.totalInjectionGapCount;
		this.wordCountMap           = wordCountMap;
		allyList                    = new ArrayList< String >( format.teamSize );
		opponentList                = new ArrayList< String >( format.teamSize );
	}
	
	@Override
	public int compareTo( final PlayerGameParticipationStats pgps ) {
		return date.compareTo( pgps.date );
	}
	
}
