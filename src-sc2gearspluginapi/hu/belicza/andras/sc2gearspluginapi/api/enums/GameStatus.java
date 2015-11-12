/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.enums;

import hu.belicza.andras.sc2gearspluginapi.api.StarCraftIIApi;
import hu.belicza.andras.sc2gearspluginapi.api.listener.GameStatusListener;

/**
 * Game status.
 * 
 * @author Andras Belicza
 * 
 * @see StarCraftIIApi#getGameStatus()
 * @see GameStatusListener#gameStatusChanged(GameStatus)
 */
public enum GameStatus {
	
	/** Unknown. */
	UNKNOWN,
	/** No game. */
	NO_GAME,
	/** Started. */
	STARTED;
	
}
