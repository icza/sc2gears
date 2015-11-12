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

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BnetLanguage;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Gateway;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Region;

/**
 * Info that identifies a StarCraft II player.
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see IPlayer
 */
public interface IPlayerId {
	
	/** Interface version. */
	String VERSION = "2.0";
	
	/**
	 * Returns the name of the player.
	 * @return the name of the player
	 */
	String getName();
	
	/**
	 * Returns the unique ID assigned by Battle.net.
	 * @return the unique ID assigned by Battle.net
	 */
	int getBattleNetId();
	
	/**
	 * Returns the unique sub-id assigned by Battle.net (region identifier inside the gateway).
	 * @return the unique sub-id assigned by Battle.net (region identifier inside the gateway)
	 */
	int getBattleNetSubId();
	
	/**
	 * Returns the gateway of the player.
	 * @return the gateway of the player
	 * @see Gateway
	 */
	Gateway getGateway();
	
	/**
	 * Returns the Battle.net profile URL of the player.
	 * <p>Returns an empty string for computer (AI) players.</p>
	 * @param preferredBnetLanguage the preferred battle.net language to view the profile in
	 * @return the profile URL of the player
	 * @see BnetLanguage
	 */
	String getBattleNetProfileUrl( BnetLanguage preferredBnetLanguage );
	
	/**
	 * Returns the Sc2ranks.com profile URL of the player.
	 * <p>Returns an empty string for computer (AI) players.</p>
	 * @return the Sc2ranks.com profile URL of the player
	 */
	String getSc2ranksProfileUrl();
	
	/**
	 * Returns the region of the player.
	 * @return the region of the player
	 * @see Region
	 */
	Region getRegion();
	
	/**
	 * Returns the full name of the player: name/gateway/region/bnet_id, example: <code>"SCIIGears/EU/1/206154"</code>.
	 * @return the full name of the player: name/gateway/region/bnet_id
	 */
	String getFullName();
	
}
