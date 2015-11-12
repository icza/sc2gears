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

import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gearspluginapi.api.enums.League;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Format;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Gateway;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains replay header information.
 * 
 * <p>For format description see:
 * <ul><li><a href='http://code.google.com/p/starcraft2replay/'>Starcraft 2 Replay</a></ul></p>
 * 
 * @author Andras Belicza
 */
public class InitData {
	
	public static class Client {
		public String name;
		public League league = League.UNKNOWN;
		public int    swarmLevels;
	}
	
	public Client[] clients;
	
	public String[]  clientNames;
	
	public GameType  gameType  = GameType.UNKNOWN;
	
	public GameSpeed gameSpeed = GameSpeed.UNKNOWN;
	
	public Format    format    = Format.UNKNOWN;
	
	/** It is usually the SHA-256 hash value of the map file content. */
	public String    mapFileName;
	
	public Gateway   gateway   = Gateway.UNKNOWN;
	
	/** True => ranked or unranked; False => vs AI (but still AutoMM). */
	public Boolean   competitive;
	
	/**
	 * Arranged: first names are the player names, then the client names in reversed order;
	 * this is the order used in chat client indices and "Leave game" action indices.
	 */
	private String[] arrangedClientNames;
	
	/**
	 * Returns the arranged client names.
	 * @param players players (to be put in front)
	 * @return the arranged client names
	 * @see #arrangedClientNames
	 */
	public String[] getArrangedClientNames( final Player[] players ) {
		if ( arrangedClientNames == null ) {
			final List< String > clientNameList = new ArrayList< String >( clientNames.length );
			
			// First the players
			for ( final Player player : players )
				clientNameList.add( player.nameWithClan );
			
			// Then the clients, in reversed order
			clientsCycle:
			for ( int i = clientNames.length - 1; i >= 0; i-- )
				if ( clientNames[ i ].length() > 0 ) {
					// A simple check if the client name list already contains clientNames[ i ] is not sufficient
					// Multiple observers with the same name can be in the game!
					for ( final Player player : players )
						if ( player.nameWithClan.equals( clientNames[ i ] ) )
							continue clientsCycle; // This client is a player, already added
					
					clientNameList.add( clientNames[ i ] );
				}
			
			arrangedClientNames = clientNameList.toArray( new String[ clientNameList.size() ] );
		}
		
		return arrangedClientNames;
	}
	
}
