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

import hu.belicza.andras.sc2gearspluginapi.api.enums.ReplayOrigin;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ExpansionLevel;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Format;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Gateway;

import java.io.File;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * An interface representing StarCraft II replays.
 * 
 * <p>Example usage:<br>
 * <blockquote><pre>
 * IReplay replay = generalServices.getReplayFactoryApi().parseReplay( "C:\\someReplay.SC2Replay", EnumSet.allOf( ReplayFactoryApi.ReplayContent.class ) );
 * 
 * if ( replay == null )
 *     System.out.println( "Failed to parse replay!" );
 * else {
 *     System.out.println( "Players: " + replay.getPlayerNamesGrouped() );
 *     System.out.println( "Race match-up: " + replay.getRaceMatchup() );
 *     System.out.println( "Map name: " + replay.getMapName() );
 *     if ( replay.getMapInfo() != null )
 *         System.out.println( "Map size: " + replay.getMapInfo().getSizeString() );
 * }
 * </pre></blockquote></p>
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see ReplayFactoryApi
 * @see ReplayUtilsApi
 */
public interface IReplay {
	
	/** Interface version. */
	String VERSION = "4.2";
	
	/**
	 * Returns the general services implementation version.
	 * @return the general services implementation version
	 */
	String getImplementationVersion();
	
	/**
	 * Returns the version of the replay, for example <code>"1.3.4.18701"</code>.<br>
	 * This is the version of StarCraft II that saved the replay.
	 * 
	 * @return the version of the replay
	 */
	String getReplayVersion();
	
	/**
	 * Returns the components of the replay version, for example <code>{1, 3, 4, 18701}</code>.
	 * @return the components of the replay version
	 */
	int[] getBuildNumbers();
	
	/**
	 * Returns the length of the game in half seconds.
	 * @return the length of the game in half seconds
	 * @see #getGameLengthSec()
	 * @see #getFrames()
	 */
	int getGameLength();
	
	/**
	 * Returns the length of the game in seconds.
	 * @return the length of the game in seconds
	 * @see #getGameLength()
	 * @see #getFrames()
	 */
	int getGameLengthSec();
	
	/**
	 * Returns the length of the game in frames.
	 * @return the length of the game in frames
	 * @see #getGameLength()
	 * @see #getGameLengthSec()
	 */
	int getFrames();
	
	/**
	 * Returns the excluded initial frames from the APM calculation.
	 * @return the excluded initial frames from the APM calculation
	 */
	int getExcludedInitialFrames();
	
	/**
	 * Returns the game speed object that is used to calculate time values (game-time or real-time).
	 * 
	 * <p>Initially it is set to reflect the {@link InfoApi#isRealTimeConversionEnabled()} setting.</p>
	 * 
	 * @return the game speed object that is used to calculate time values (game-time or real-time)
	 * 
	 * @see InfoApi#isRealTimeConversionEnabled()
	 * @see IGameEvents#isDisplayInSeconds()
	 */
	GameSpeed getConverterGameSpeed();
	
	/**
	 * Returns the origin of the replay which tells where/how the replay was assembled.
	 * @return the origin of the replay
	 * @since "2.7"
	 * @see ReplayOrigin
	 */
	ReplayOrigin getReplayOrigin();
	
	// ==================================================================================
	// **********************************  INIT DATA  ***********************************
	// ==================================================================================
	
	/**
	 * Returns the client names (player names and observers).
	 * 
	 * <p>Returns the names in the order they are recorded in the replay.</p>
	 * 
	 * @return the client names (player names and observers)
	 * @see #getArrangedClientNames()
	 */
	String[] getClientNames();
	
	/**
	 * Returns the game type.
	 * @return the game type
	 * @see GameType
	 */
	GameType getGameType();
	
	/**
	 * Returns the game speed.
	 * @return the game speed
	 * @see GameSpeed
	 */
	GameSpeed getGameSpeed();
	
	/**
	 * Returns the game format.
	 * @return the game format
	 * @see Format
	 */
	Format getFormat();
	
	/**
	 * Returns the name of the map file.
	 * 
	 * <p>The map file name is usually the SHA-256 hash value of the map file content.</p>
	 * 
	 * <p>The returned file name is relative to the SC2 maps folder.</p>
	 * 
	 * @return the name of the map file
	 * 
	 * @see InfoApi#getSc2MapsFolder()
	 * @see #getMapFile()
	 * @see #getMapName()
	 * @see #getOriginalMapName()
	 */
	String getMapFileName();
	
	/**
	 * Returns the map file.
	 * @return the map file
	 * 
	 * @see #getMapFileName()
	 * @see #getMapName()
	 * @see #getOriginalMapName()
	 */
	File getMapFile();
	
	/**
	 * Returns the gateway.
	 * @return the gateway
	 * @see Gateway
	 */
	Gateway getGateway();
	
	/**
	 * Tells if the game is a competitive game (ranked or unranked game).<br>
	 * AutoMM vs AI matches are not competitive.<br>
	 * This info is available only from replay version 2.0.
	 * @return if the game is a competitive game
	 * @since "4.2"
	 */
	Boolean isCompetitive();
	
	/**
	 * Returns the rearranged client names.
	 * 
	 * <p> Arranged: first names are the player names, then the client names in reversed order;
	 * this is the order used in chat client indices and "Leave game" action indices.</p>
	 * 
	 * @return the rearranged client names
	 * @see #getClientNames()
	 */
	String[] getArrangedClientNames();
	
	// ==================================================================================
	// ***********************************  DETAILS  ************************************
	// ==================================================================================
	
	/**
	 * Returns the players of the replay.
	 * @return the players of the replay
	 * @see IPlayer
	 */
	IPlayer[] getPlayers();
	
	/**
	 * Returns the name of the map.
	 * <p>The name might be a map alias defined by the user.</p>
	 * @return the name of the map.
	 * @see #getOriginalMapName()
	 * @see #getMapFileName()
	 * @see #getMapFile()
	 */
	String getMapName();
	
	/**
	 * Returns the original name of the map as it is recorded in the replay.
	 * @return the name of the map.
	 * @see #getMapName()
	 * @see #getMapFileName()
	 * @see #getMapFile()
	 */
	String getOriginalMapName();
	
	/**
	 * Returns the save time (and date) of the replay.
	 * @return the save time (and date) of the replay
	 */
	Date getSaveTime();
	
	/**
	 * Returns the time zone of the recorder of the replay in hours.
	 * @return the time zone of the recorder of the replay in hours
	 */
	float getSaveTimeZone();
	
	/**
	 * Returns the expansion level.
	 * @return the expansion level
	 * @since "4.1"
	 */
	ExpansionLevel getExpansion();
	
	/**
	 * Returns the game dependencies.
	 * @return the game dependencies
	 * @since "4.1"
	 */
	String[] getDependencies();
	
	/**
	 * Returns the comma separated list of player names.
	 * @return the comma separated list of player names
	 */
	String getPlayerNames();
	
	/**
	 * Returns the comma separated list of player names grouped by teams.
	 * @return the comma separated list of player names grouped by teams
	 */
	String getPlayerNamesGrouped();
	
	/**
	 * Returns the comma separated list of the names of the winners.
	 * @return the comma separated list of the names of the winners
	 * @since "2.2"
	 */
	String getWinnerNames();
	
	/**
	 * Returns the race match-up, for example: "ZTvPP".
	 * @return the race match-up
	 */
	String getRaceMatchup();
	
	/**
	 * Returns the league match-up, for example: "DI, PT vs PT, GO".
	 * @return the league match-up
	 * @since "4.2"
	 */
	String getLeagueMatchup();
	
	/**
	 * Returns player indices in team order.
	 * 
	 * <p>If there is already a stored teamOrderPlayerIndices, it will be returned. Else a new array will be created and stored as follows:<br> 
	 * Lower teams are put before higher ones, and players without a team will be listed in the end.<br>
	 * For example if player 1 and player 4 are in team 1, player 2 and player 3 are in team 2, and player 0 has no team, this returns:<br>
	 * <code>{ 1, 4, 2, 3, 0 }</code></p>
	 * 
	 * @return player indices in team order
	 * @see #getTeamOrderPlayerIndices(Comparator)
	 */
	int[] getTeamOrderPlayerIndices();
	
	/**
	 * Returns player indices in an order defined by the <code>teamIndexComparator</code>.<br>
	 * 
	 * <b>The teamIndexComparator might define a unique team order, but players in the same team must be next to each other!</b>
	 * 
	 * @return player indices in an order defined by the <code>teamIndexComparator</code>
	 * @see #getTeamOrderPlayerIndices()
	 */
	int[] getTeamOrderPlayerIndices( Comparator< int[] > teamIndexComparator );
	
	/**
	 * Rearranges the players based on a favored player list.
	 * 
	 * <p>Players with lower index in the list has higher priority. Moving a player also moves his/her team.<br>
	 * This method also sets the "teamOrderPlayerIndices" (see {@link #getTeamOrderPlayerIndices()})
	 * so that teams of (higher) favored players will be in the front of list.<br>
	 * Calling this method with an empty list will not change anything.
	 * 
	 * @param favoredPlayerList list of favored players to use when rearranging
	 * 
	 * @see InfoApi#getFavoredPlayerList()
	 */
	void rearrangePlayers( List< String > favoredPlayerList );	
	
	// ==================================================================================
	// *********************************  GAME EVENTS  **********************************
	// ==================================================================================
	
	/**
	 * Returns the Game events of the replay.
	 * 
	 * <p>This will return <code>null</code> if game events are not parsed
	 * (see {@link ReplayFactoryApi#parseReplay(String, java.util.Set)}).</p>
	 * 
	 * @return the Game events of the replay
	 * @see IGameEvents
	 * @see ReplayFactoryApi#parseReplay(String, java.util.Set)
	 */
	IGameEvents getGameEvents();
	
	// ==================================================================================
	// ********************************  MESSAGE EVENTS  ********************************
	// ==================================================================================
	
	/**
	 * Returns the Message events of the replay.
	 * 
	 * <p>This will return <code>null</code> if message events are not parsed
	 * (see {@link ReplayFactoryApi#parseReplay(String, java.util.Set)}).</p>
	 * 
	 * @return the Message events of the replay
	 * @see IMessageEvents
	 * @see ReplayFactoryApi#parseReplay(String, java.util.Set)
	 */
	IMessageEvents getMessageEvents();
	
}
