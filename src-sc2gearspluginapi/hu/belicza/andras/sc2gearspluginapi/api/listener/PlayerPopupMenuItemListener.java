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
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerId;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerType;

/**
 * Defines a listener interface that can be notified when the associated player popup menu item is activated.
 * 
 * @author Andras Belicza
 * 
 * @see CallbackApi
 */
public interface PlayerPopupMenuItemListener {
	
	/**
	 * Provides information about a player (these uniquely identify a player).
	 * @author Andras Belicza
	 */
	public interface PlayerInfo {
		
		/**
		 * Returns the player identifier.
		 * @return the player identifier
		 * @see IPlayerId
		 */
		IPlayerId getPlayerId();
		
		/**
		 * Returns the type of the player.
		 * @return the type of the player
		 */
		PlayerType getPlayerType();
		
	}
	
	/**
	 * Called when the associated player popup menu item is activated.
	 * @param playerInfo information of the player belonging to the player popup menu
	 * @param handler    handler of the menu item that was activated
	 */
	void actionPerformed( PlayerInfo playerInfo, Integer handler );
	
}
