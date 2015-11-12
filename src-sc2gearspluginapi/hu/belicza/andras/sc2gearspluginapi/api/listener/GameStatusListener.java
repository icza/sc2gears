/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.listener;

import hu.belicza.andras.sc2gearspluginapi.api.CallbackApi;
import hu.belicza.andras.sc2gearspluginapi.api.InfoApi;
import hu.belicza.andras.sc2gearspluginapi.api.enums.GameStatus;

/**
 * Defines a listener interface that can be notified when game (StarCraft II) status changes.
 * 
 * @author Andras Belicza
 * 
 * @see CallbackApi
 */
public interface GameStatusListener {
	
	/**
	 * Called when the game status changes.<br>
	 * Game status changes can only be detected in Windows.<br>
	 * You can check if the operating system is windows with {@link InfoApi#isWindows()}.
	 * @param newGameStatus the new game status, one of {@link GameStatus#NO_GAME} or {@link GameStatus#STARTED}
	 */
	void gameStatusChanged( GameStatus newGameStatus );
	
}
