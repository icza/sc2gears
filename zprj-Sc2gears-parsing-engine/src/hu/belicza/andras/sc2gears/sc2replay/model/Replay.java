/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.sc2replay.model;

import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gearspluginapi.api.enums.ReplayOrigin;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;

/**
 * This class represents a StarCraft 2 replay file.
 * 
 * <p>Replays are MPQ archives containing various pieces of data.</p>
 * 
 * <p>For format description see:
 * <ul><li><a href='http://code.google.com/p/starcraft2replay/'>Starcraft 2 Replay</a></ul></p>
 * 
 * <p>It appears that the replay.sync content (the content of the file) can be removed which will prevent SC2
 * to go out of sync if the game.events is modified (new actions are injected or removed).</p>
 * 
 * @author Andras Belicza
 */
public class Replay {
	
	/** Version of StarCraft II which saved the replay. */
	public String         version;
	/** Build numbers (version parts).                 */
	public int[]          buildNumbers;
	
	/** Length of the game in half seconds.    */
	public int            gameLength;
	
	/** Length of the game in seconds. */
	public int            gameLengthSec;
	
	/** The length of the game in frames.   */
	public int            frames;
	
	/** Excluded initial frames from the APM calculations. */
	public int            excludedInitialFrames;
	
	/** Initial data.                                                               */
	public InitData       initData;
	
	/** Details.                                                                    */
	public Details        details;
	
	/** Contains the game events (select, order, right click, etc).                 */
	public GameEvents     gameEvents;
	
	/** Contains information from chat messages and other non-game impact commands. */
	public MessageEvents  messageEvents;
	
	/** This game speed object will be used to calculate time values (game-time or real-time). */
	public GameSpeed      converterGameSpeed;
	
	/** The origin of the replay which tells where/how the replay was assembled.    */
	public final ReplayOrigin replayOrigin;
	
    /**
     * Creates a new Replay.
     * @param replayOrigin the origin of the replay
     */
    public Replay( final ReplayOrigin replayOrigin ) {
    	this.replayOrigin = replayOrigin;
    }
	
	/**
	 * Sets the attributes related to game duration: frames, gameLength, gameLength, gameLengthSec
	 * @param frames the length of game in frames
	 */
	public void setGameDuration( final int frames ) {
		this.frames   = frames;
		gameLength    = frames >> 5;
		gameLengthSec = gameLength >> 1;
	}
	
	/**
	 * Sets the build numbers and version of the replay.
	 * @param buildNumbers build numbers of the replay
	 */
	public void setBuildNumbers( final int[] buildNumbers ) {
		this.buildNumbers = buildNumbers;
		version = ReplayUtils.convertBuildNumbersToString( buildNumbers );
	}
	
}
