/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api;

import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IReplay;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.importing.ReplaySpecification;

import java.util.Set;

/**
 * Defines services to parse StarCraft II replays and to construct replays from replay specifications.
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GeneralServices
 * @see ReplayUtilsApi
 */
public interface ReplayFactoryApi {
	
	/** Interface version. */
	String VERSION = "2.7";
	
	/**
	 * Contents of a replay.
	 * @author Andras Belicza
	 */
	enum ReplayContent {
		/** Game events (actions of players).       */
		GAME_EVENTS,
		/** Message events (in-game chat).          */
		MESSAGE_EVENTS,
		/** Basic map info, read from SC2 map file. */
		MAP_INFO,
		/** Map attributes, read from SC2 map file. */
		MAP_ATTRIBUTES,
		/** Extended map info (including map objects and start locations), read from SC2 map file.
		 * Only has effect with {@link #MAP_INFO}.  */
		EXTENDED_MAP_INFO;
	}
	
	/**
	 * Returns the version of the replay parser engine.
	 * @return the version of the replay parser engine
	 */
	String getReplayParserEngineVersion();
	
	/**
	 * Parses a replay from a file.
	 * 
	 * @param fileName          name of the replay file
	 * @param contentToParseSet set of replay content to parse; general replay info is always parsed besides the content denoted by this set
	 * @return the parsed replay or <code>null</code> if replay parse failed
	 * 
	 * @see IReplay
	 * @see ReplayContent
	 * @see #getReplay(String, Set)
	 */
	IReplay parseReplay( String fileName, Set< ReplayContent > contentToParseSet );
	
	/**
	 * Constructs a replay from the given replay specification.
	 * 
	 * @param replaySpec replay specification
	 * @return the constructed replay from the given replay specification
	 * 
	 * @see ReplaySpecification
	 * @see IReplay
	 * @see #getReplay(String, Set)
	 */
	IReplay constructReplay( ReplaySpecification replaySpec );
	
	/**
	 * 
	 * @param fileName
	 * @param contentToParseSet
	 * @return
	 */
	/**
	 * Gets and returns a replay.
	 * 
	 * <p>This method first tries to get the replay from the Replay cache. If the replay is in the cache, it will be returned.
	 * If the replay is not in the cache, the replay will be parsed with the specified optional <code>contentToParseSet</code>
	 * similarly to {@link #parseReplay(String, Set)}. If <code>contentToParseSet</code> is specified and is "wide" enough to parse
	 * enough information required by the cache, the parsed replay will be cached after the parsing and will be available
	 * from the cache next time.</p>
	 * 
	 * <p>You can use {@link IReplay#getReplayOrigin()} to tell where the returned replay was taken from
	 * (e.g. from the replay cache, or it was parsed from the replay file).</p>
	 * 
	 * <p><b>Attention!</b> The replay cache contains limited information about a replay. Only use cached replays if the cached
	 * information is sufficient for you. What you gain in return is <i>much-much</i> faster replay data access.<br>
	 * Here is an incomplete list of information that is stored in the replay cache:
	 * <ul>
	 * 		<li><b>Basic info:</b> expansion level, replay version, duration, gateway, is competitive, format, game speed, converter game speed,
	 * 			game type, save time, map name, original map name 
	 * 		<li><b>Players:</b> name, battle.net id, battle.net sub-id, name with clan tag, league, gateway, type, last action frame, race, final race,
	 * 			team, is winner, actions count, excluded actions count, effective actions count, excluded effective actions count,
	 * 			total hatch time, total hatch spawn time, total injection gap, total injection gap count
	 * 		<li><b>Game events:</b> just the first couple build actions for all players, nothing more!
	 * 			(And even these build actions are quite bare: they only contain the player and the building properties, not even the frame
	 * 			- although the order of these actions is chronological.)
	 * 		<li><b>Message events:</b> just the word count maps, nothing more!
	 * </ul></p>
	 * 
	 * @param fileName          name of the replay file to be loaded/parsed
	 * @param contentToParseSet optional replay content set to be parsed <b>if</b> cache does not contain the replay
	 * 		(if the replay is cached, the cached info will be returned),
	 * 		if not specified (<code>null</code>), a set of {@link ReplayContent#GAME_EVENTS} and {@link ReplayContent#MESSAGE_EVENTS} will be used
	 * @return the loaded/parsed replay or <code>null</code> if replay is not in the cache (or failed to read the cache) and replay parse failed
	 * 
	 * @see IReplay
	 * @see ReplayContent
	 * @see #parseReplay(String, Set)
	 */
	IReplay getReplay( String fileName, Set< ReplayContent > contentToParseSet );
	
}
