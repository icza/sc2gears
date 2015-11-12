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
import hu.belicza.andras.sc2gearspluginapi.api.enums.GameStatus;

import java.io.File;

/**
 * StarCraft II services.
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GeneralServices
 */
public interface StarCraftIIApi {
	
	/** Interface version. */
	String VERSION = "1.0";
	
	/**
	 * Starts StarCraft II.
	 */
	void startStarCraftII();
	
	/**
	 * Starts StarCraft II Editor.
	 */
	void startStarCraftIIEditor();
	
	/**
	 * Starts playing a replay in StarCraft II.<br>
	 * Due to StarCraft II it only works if StarCraft II is not running at the time when this is called.
	 */
	void launchReplay( File replayFile );
	
	/**
	 * Returns the current game status.<br>
	 * The game status is only available on Windows.<br>
	 * Whether the operating system is Windows can be tested with {@link InfoApi#isWindows()}.
	 * @return the current game status or <code>null</code> if some error occurs 
	 */
	GameStatus getGameStatus();
	
	/**
	 * Returns the current APM.
	 * 
	 * <p>The current APM only changes during a game (when gameStatus={@link GameStatus#STARTED}),
	 * else this method will return the last recorded APM from the last game.</p>
	 * 
	 * <p>The returned value may be measured in real time or in game time.
	 * To tell if the value is in game time, call {@link InfoApi#isRealTimeConversionEnabled()}.
	 * 
	 * <p>The current APM is only available on Windows.
	 * Whether the operating system is Windows can be tested with {@link InfoApi#isWindows()}.</p>
	 * 
	 * @return the current APM or <code>null</code> if some error occurs 
	 */
	Integer getApm();
	
}
