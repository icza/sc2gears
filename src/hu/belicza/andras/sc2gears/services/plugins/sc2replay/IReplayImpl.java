/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.services.plugins.sc2replay;

import hu.belicza.andras.sc2gears.sc2map.MapParser;
import hu.belicza.andras.sc2gears.sc2replay.model.Details;
import hu.belicza.andras.sc2gears.sc2replay.model.InitData;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gearspluginapi.api.enums.ReplayOrigin;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IGameEvents;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IMapInfo;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IMessageEvents;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayer;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IReplay;
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
 * {@link IReplay} implementation.
 * 
 * @author Andras Belicza
 * 
 * @see IReplay
 */
public class IReplayImpl implements IReplay {
	
	/** Implementation version. */
	public static final String IMPL_VERSION = "2.7";
	
	/** Replay that is "interfaced".              */
	private final Replay   replay;
	/** Reference to the Init data of the replay. */
	private final InitData initData;
	/** Reference to the Details of the replay.   */
	private final Details  details;
	
	/**
	 * Creates a new IReplayImpl.
	 * @param replay replay that is "interfaced"
	 */
	public IReplayImpl( final Replay replay ) {
		this.replay = replay;
		initData    = replay.initData;
		details     = replay.details;
	}
	
	@Override
	public String getImplementationVersion() {
		return IMPL_VERSION;
	}
	
	@Override
	public String getReplayVersion() {
		return replay.version;
	}
	
	@Override
	public int[] getBuildNumbers() {
		return replay.buildNumbers;
	}
	
	@Override
	public int getGameLength() {
		return replay.gameLength;
	}
	
	@Override
	public int getGameLengthSec() {
		return replay.gameLengthSec;
	}
	
	@Override
	public int getFrames() {
		return replay.frames;
	}
	
	@Override
	public int getExcludedInitialFrames() {
		return replay.excludedInitialFrames;
	}
	
	@Override
	public GameSpeed getConverterGameSpeed() {
		return replay.converterGameSpeed;
	}
	
	@Override
	public ReplayOrigin getReplayOrigin() {
		return replay.replayOrigin;
	}
	
	// ==================================================================================
	// **********************************  INIT DATA  ***********************************
	// ==================================================================================
	
	@Override
	public String[] getClientNames() {
		return initData.clientNames;
	}
	
	@Override
	public GameType getGameType() {
		return initData.gameType;
	}
	
	@Override
	public GameSpeed getGameSpeed() {
		return initData.gameSpeed;
	}
	
	@Override
	public Format getFormat() {
		return initData.format;
	}
	
	@Override
	public String getMapFileName() {
		return initData.mapFileName;
	}
	
	@Override
	public File getMapFile() {
		return MapParser.getMapFile( replay );
	}
	
	@Override
	public Gateway getGateway() {
		return initData.gateway;
	}
	
	@Override
	public Boolean isCompetitive() {
		return initData.competitive;
	}
	
	@Override
	public String[] getArrangedClientNames() {
		return initData.getArrangedClientNames( details.players );
	}
	
	// ==================================================================================
	// ***********************************  DETAILS  ************************************
	// ==================================================================================
	
	@Override
	public IPlayer[] getPlayers() {
		return details.players;
	}
	
	@Override
	public String getMapName() {
		return details.mapName;
	}
	
	@Override
	public String getOriginalMapName() {
		return details.originalMapName;
	}
	
	@Override
	public Date getSaveTime() {
		return new Date( details.saveTime );
	}
	
	@Override
	public float getSaveTimeZone() {
		return details.saveTimeZone;
	}
	
	@Override
	public ExpansionLevel getExpansion() {
		return details.expansion;
	}
	
	@Override
	public String[] getDependencies() {
		return details.dependencies;
	}
	
	@Override
	public String getPlayerNames() {
		return details.getPlayerNames();
	}
	
	@Override
	public String getPlayerNamesGrouped() {
		return details.getPlayerNamesGrouped();
	}
	
	@Override
	public String getWinnerNames() {
		return details.getWinnerNames();
	}
	
	@Override
	public String getRaceMatchup() {
		return details.getRaceMatchup();
	}
	
	@Override
	public String getLeagueMatchup() {
		return details.getLeagueMatchup();
	}
	
	@Override
	public int[] getTeamOrderPlayerIndices() {
		return details.getTeamOrderPlayerIndices();
	}
	
	@Override
	public int[] getTeamOrderPlayerIndices( final Comparator< int[] > teamIndexComparator ) {
		return details.getTeamOrderPlayerIndices( teamIndexComparator );
	}
	
	@Override
	public void rearrangePlayers( final List< String > favoredPlayerList ) {
		details.rearrangePlayers( favoredPlayerList );
	}
	
	// ==================================================================================
	// *********************************  GAME EVENTS  **********************************
	// ==================================================================================
	
	@Override
	public IGameEvents getGameEvents() {
		return replay.gameEvents;
	}
	
	// ==================================================================================
	// ********************************  MESSAGE EVENTS  ********************************
	// ==================================================================================
	
	@Override
	public IMessageEvents getMessageEvents() {
		return replay.messageEvents;
	}
	
	// ==================================================================================
	// ***********************************  MAP INFO  ***********************************
	// ==================================================================================
	
	@Override
	public IMapInfo getMapInfo() {
		return replay.mapInfo;
	}
	
}
