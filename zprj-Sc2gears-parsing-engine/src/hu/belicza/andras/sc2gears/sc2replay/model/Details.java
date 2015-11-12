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

import hu.belicza.andras.sc2gears.sc2replay.model.InitData.Client;
import hu.belicza.andras.sc2gearspluginapi.api.enums.League;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayer;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerId;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BnetLanguage;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Difficulty;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ExpansionLevel;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Gateway;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerColor;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Region;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import sjava.awt.Color;
import sjava.awt.Point;

/**
 * Contains replay header information.
 * 
 * @author Andras Belicza
 */
public class Details {
	
	/**
	 * Info that identifies a player.
	 * @author Andras Belicza
	 */
	public static class PlayerId implements IPlayerId {
		public String  name;
		/** Unique ID assigned by Battle.net.                                           */
		public int     battleNetId; 
		/** Unique subID assigned by Battle.net (region identifier inside the gateway). */
		public int     battleNetSubId;
		public Gateway gateway = Gateway.UNKNOWN;
		
		@Override
		public String getBattleNetProfileUrl( final BnetLanguage preferredBnetLanguage ) {
			if ( gateway == Gateway.UNKNOWN )
				return "";
			try {
				final BnetLanguage bnetLanguage = gateway.availableLanguageSet.contains( preferredBnetLanguage ) ? preferredBnetLanguage : gateway.defaultLanguage;
				
				// There is an exception with the Chinese (Taiwan) language:
				// it belongs to the Korean gateway, but the web is hosted under a different URL 
				final String bnetUrl = gateway == Gateway.ASIA && bnetLanguage == BnetLanguage.CHINESE_TRADITIONAL ? "http://tw.battle.net/" : gateway.bnetUrl;
				
				return bnetUrl + "sc2/" + bnetLanguage.languageCode + "/profile/"
					+ battleNetId + "/" + battleNetSubId + "/" + URLEncoder.encode( name, "UTF-8" ) + "/";
			} catch ( final UnsupportedEncodingException uee ) {
				// Never to happen
				uee.printStackTrace();
				return "";
			}
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public int getBattleNetId() {
			return battleNetId;
		}
		
		@Override
		public int getBattleNetSubId() {
			return battleNetSubId;
		}
		
		@Override
		public Gateway getGateway() {
			return gateway;
		}
		
		@Override
		public String getSc2ranksProfileUrl() {
			if ( gateway == Gateway.UNKNOWN )
				return "";
			try {
				return "http://sc2ranks.com/" + getRegion().sc2ranksId + "/" + battleNetId + "/" + URLEncoder.encode( name, "UTF-8" );
			} catch ( final UnsupportedEncodingException uee ) {
				// Never to happen
				uee.printStackTrace();
				return "";
			}
		}
		
		@Override
		public Region getRegion() {
			return Region.getFromGatewayAndSubId( gateway, battleNetSubId );
		}
		
		/**
		 * Returns the string representation of the player identifier.<br>
		 * This implementation returns {@link #getFullName()}.
		 * @return the string representation of the player identifier
		 * @see #getFullName()
		 */
		@Override
		public String toString() {
			return getFullName();
		}
		
		@Override
		public String getFullName() {
			return name + "/" + gateway.binaryValue + "/" + battleNetSubId + "/" + battleNetId;
		}
		
		/**
		 * Parses a player identifier from a full name string with a format: name/gateway/region/bnet_id, example: "SCIIGears/EU/1/206154".
		 * @param fullName full name of the player
		 * @return the parsed PlayerId; or null if the full name is invalid
		 */
		public static PlayerId parse( final String fullName ) {
			final String[] tokens = fullName.split( "/" );
			if ( tokens.length < 4 )
				return null;
			
			final PlayerId playerId = new PlayerId();
			playerId.name           = tokens[ 0 ];
			playerId.gateway        = Gateway.fromBinaryValue( tokens[ 1 ] );
			try {
				playerId.battleNetSubId = Integer.parseInt( tokens[ 2 ] );
				playerId.battleNetId    = Integer.parseInt( tokens[ 3 ] );
			} catch ( final NumberFormatException nfe ) {
				return null;
			}
			
			return playerId;
		}
		
		@Override
		public boolean equals( final Object playerId_ ) {
			if ( !( playerId_ instanceof PlayerId ) )
				return false;
			final PlayerId playerId = (PlayerId) playerId_;
			// Player is identified without the name, but if no bnet id is present (local game), then only the name can distinguish, so check all fields
			return battleNetId == playerId.battleNetId && battleNetSubId == playerId.battleNetSubId && gateway == playerId.gateway && name.equals( playerId.name );
		}
		
		@Override
		public int hashCode() {
			// Player is identified without the name, but if no bnet id is present (local game), then only the name can distinguish, so use all fields
			return ( battleNetId + "-" + battleNetSubId + "-" + gateway.ordinal() + "-" + name ).hashCode();
		}
	}
	
	public static class Player implements IPlayer {
		// Integer.MAX_VALUE is used for unknown teams for easier sorting
		public static final int TEAM_UNKNOWN = Integer.MAX_VALUE;
		public Client      client;
		public PlayerId    playerId = new PlayerId();
		public String      nameWithClan;
		public String      raceString;
		public final int[] argbColor = new int[ 4 ];
		public int         lastActionFrame;
		public int         actionsCount;
		public int         excludedActionsCount;          // Excluded from APM calculation
		public int         effectiveActionsCount;
		public int         excludedEffectiveActionsCount; // Excluded from EAPM calculation
		public PlayerType  type = PlayerType.UNKNOWN;
		public Race        race = Race.UNKNOWN;
		public Race        finalRace; // If race is null, UNKNOWN or RANDOM, we try to interpret the localized value
		public int         team = TEAM_UNKNOWN;
		public Point       startLocation;
		public Difficulty  difficulty = Difficulty.UNKNOWN;
		public PlayerColor playerColor = PlayerColor.UNKNOWN;
		public int         handicap = 100; // Default is 100, in case this info is missing from the replay
		/** <code>Boolean.TRUE</code> = winner, <code>Boolean.FALSE</code> = loser, <code>null</code> = unknown. */
		public Boolean     isWinner;
		public int         totalHatchTime;
		public int         totalHatchSpawnTime;
		public int         totalInjectionGap;
		public int         totalInjectionGapCount;
		
		@Override
		public IPlayerId getPlayerId() {
			return playerId;
		}
		
		@Override
		public String getNameWithClan() {
			return nameWithClan;
		}
		
		@Override
		public League getLeague() {
			return client == null ? League.UNKNOWN : client.league;
		}
		
		@Override
		public int getSwarmLevels() {
			return client == null ? 0 : client.swarmLevels;
		}
		
		@Override
		public String getLocalizedRaceString() {
			return raceString;
		}
		
		@Override
		public int[] getArgbColor() {
			return argbColor;
		}
		
		@Override
		public int getLastActionFrame() {
			return lastActionFrame;
		}
		
		@Override
		public int getActionsCount() {
			return actionsCount;
		}
		
		@Override
		public int getExcludedActionsCount() {
			return excludedActionsCount;
		}
		
		@Override
		public int getEffectiveActionsCount() {
			return effectiveActionsCount;
		}
		
		@Override
		public int getExcludedEffectiveActionsCount() {
			return excludedEffectiveActionsCount;
		}
		
		@Override
		public PlayerType getType() {
			return type;
		}
		
		@Override
		public Race getRace() {
			return race;
		}
		
		@Override
		public Race getFinalRace() {
			return finalRace;
		}
		
		@Override
		public int getTeam() {
			return team;
		}
		
		@Override
		public Point getStartLocation() {
			return startLocation;
		}
		
		@Override
		public Difficulty getDifficulty() {
			return difficulty;
		}
		
		@Override
		public PlayerColor getPlayerColor() {
			return playerColor;
		}
		
		@Override
		public int getHandicap() {
			return handicap;
		}
		
		@Override
		public Boolean isWinner() {
			return isWinner;
		}
		
		@Override
		public String getRaceString() {
			if ( race == Race.RANDOM )
				return Race.RANDOM + " (" + finalRace.stringValue + ")";
			return finalRace.stringValue;
		}
		
		@Override
		public char getRaceLetter() {
			return finalRace.isConcrete ? finalRace.letter : race == Race.RANDOM ? Race.RANDOM.letter : Race.UNKNOWN.letter;
		}
		
		@Override
		public Color getColor() {
			return new Color( argbColor[ 1 ],  argbColor[ 2 ], argbColor[ 3 ] );
		}
		
		@Override
		public Color getDarkerColor() {
			return new Color( argbColor[ 1 ] >> 1,  argbColor[ 2 ] >> 1, argbColor[ 3 ] >> 1 );
		}
		
		@Override
		public Color getBrighterColor() {
			return new Color( 127 + ( argbColor[ 1 ] >> 1 ), 127 + ( argbColor[ 2 ] >> 1 ), 127 + ( argbColor[ 3 ] >> 1 ) );
		}
		
		@Override
		public String getColorName() {
			return ( playerColor == PlayerColor.UNKNOWN ? "(" : playerColor.stringValue + " (" ) + argbColor[ 1 ] + "," + argbColor[ 2 ] + "," + argbColor[ 3 ] + ")";
		}
		
		@Override
		public int getTotalHatchTime() {
			return totalHatchTime;
		}
		
		@Override
		public int getTotalHatchSpawnTime() {
			return totalHatchSpawnTime;
		}
		
		@Override
		public int getTotalInjectionGap() {
			return totalInjectionGap;
		}
		
		@Override
		public int getTotalInjectionGapCount() {
			return totalInjectionGapCount;
		}
		
		@Override
		public Float getAverageSpawningRatio() {
			return totalHatchTime == 0 ? null : (float) totalHatchSpawnTime / totalHatchTime;
		}
		
		@Override
		public Float getAverageInjectionGap() {
			return totalInjectionGapCount == 0 ? null : (float) totalInjectionGap / totalInjectionGapCount;
		}
	}
	
	public Player[] players;
	
	public String   mapName; // Might be an alias
	public String   originalMapName;
	
	public String   mapPreviewFileName;
	
	public long     saveTime;
	public float    saveTimeZone; // In hours
	
	public ExpansionLevel expansion = ExpansionLevel.UNKNOWN;
	
	public String[] dependencies;
	
	/**
	 * Returns the comma separated list of player names.
	 * @return the comma separated list of player names
	 */
	public String getPlayerNames() {
		final int[] teamOrderPlayerIndices = getTeamOrderPlayerIndices();
		
		final StringBuilder playerNamesBuilder = new StringBuilder();
		
		for ( int i = 0; i < teamOrderPlayerIndices.length; i++ ) {
			if ( i > 0 )
				playerNamesBuilder.append( ", " );
			playerNamesBuilder.append( players[ teamOrderPlayerIndices[ i ] ].playerId.name );
		}
		
		return playerNamesBuilder.toString();
	}
	
	/**
	 * Returns the comma separated list of player names grouped by teams.
	 * @return the comma separated list of player names grouped by teams
	 */
	public String getPlayerNamesGrouped() {
		final int[] teamOrderPlayerIndices = getTeamOrderPlayerIndices();
		if ( teamOrderPlayerIndices.length == 0 )
			return "";
		
		final StringBuilder playerNamesBuilder = new StringBuilder();
		
		int lastTeam = players[ teamOrderPlayerIndices[ 0 ] ].team;
		boolean firstPlayerInTeam = true;
		for ( final int playerIndex : teamOrderPlayerIndices ) {
			final Player player = players[ playerIndex ];
			if ( player.team != lastTeam ) {
				playerNamesBuilder.append( " vs " );
				lastTeam = player.team;
				firstPlayerInTeam = true;
			}
			if ( firstPlayerInTeam )
				firstPlayerInTeam = false;
			else
				playerNamesBuilder.append( ", " );
			playerNamesBuilder.append( player.playerId.name );
		}
		
		return playerNamesBuilder.toString();
	}
	
	/**
	 * Returns the comma separated list of the names of the winners.
	 * @return the comma separated list of the names of the winners
	 */
	public String getWinnerNames() {
		final int[] teamOrderPlayerIndices = getTeamOrderPlayerIndices();
		
		final StringBuilder winnersBuilder = new StringBuilder();
		
		for ( int i = 0; i < teamOrderPlayerIndices.length; i++ ) {
			final Player player = players[ teamOrderPlayerIndices[ i ] ];
			if ( player.isWinner != null && player.isWinner ) {
				if ( winnersBuilder.length() > 0 )
					winnersBuilder.append( ", " );
				winnersBuilder.append( player.playerId.name );
			}
		}
		
		return winnersBuilder.toString();
	}
	
	/**
	 * Returns the race match-up, for example: "ZTvPP"
	 * @return the race match-up
	 */
	public String getRaceMatchup() {
		final int[] teamOrderPlayerIndices = getTeamOrderPlayerIndices();
		
		final StringBuilder playerRacesBuilder = new StringBuilder();
		
		int lastTeam = teamOrderPlayerIndices.length == 0 ? -1 : players[ teamOrderPlayerIndices[ 0 ] ].team;
		for ( int i = 0; i < teamOrderPlayerIndices.length; i++ ) {
			final int team = players[ teamOrderPlayerIndices[ i ] ].team;
			if ( team != lastTeam ) {
				playerRacesBuilder.append( 'v' );
				lastTeam = team;
			}
			playerRacesBuilder.append( players[ teamOrderPlayerIndices[ i ] ].getRaceLetter() );
		}
		
		return playerRacesBuilder.toString();
	}
	
	/**
	 * Returns the league match-up, for example: "DPvPG".
	 * @return the league match-up
	 */
	public String getLeagueMatchup() {
		final int[] teamOrderPlayerIndices = getTeamOrderPlayerIndices();
		
		final StringBuilder playerRacesBuilder = new StringBuilder();
		
		int lastTeam = teamOrderPlayerIndices.length == 0 ? -1 : players[ teamOrderPlayerIndices[ 0 ] ].team;
		for ( int i = 0; i < teamOrderPlayerIndices.length; i++ ) {
			final int team = players[ teamOrderPlayerIndices[ i ] ].team;
			if ( team != lastTeam ) {
				playerRacesBuilder.append( 'v' );
				lastTeam = team;
			}
			playerRacesBuilder.append( players[ teamOrderPlayerIndices[ i ] ].getLeague().letter );
		}
		
		return playerRacesBuilder.toString();
	}
	
	/** Cache of the team order player indices. */
	private int[] teamOrderPlayerIndices;
	
	/**
	 * Returns player indices in team order.
	 * 
	 * <p>If there is already a stored teamOrderPlayerIndices, it will be returned. Else a new array will be created and stored as follows:<br> 
	 * Lower teams are put before higher ones, and players without a team will be listed in the end.<br>
	 * For example if player 1 and player 4 are in team 1, player 2 and player 3 are in team 2, and player 0 has no team, this returns:<br>
	 * <code>{ 1, 4, 2, 3, 0 }</code></p>
	 * 
	 * @return player indices in team order
	 */
	public int[] getTeamOrderPlayerIndices() {
		return getTeamOrderPlayerIndices( new Comparator< int[] >() {
			@Override
			public int compare( final int[] pair1, final int[] pair2 ) {
				return pair1[ 0 ] - pair2[ 0 ];
			}
		} );
	}
	
	/**
	 * Returns player indices in an order defined by the <code>teamIndexComparator</code>.<br>
	 * 
	 * <b>The teamIndexComparator might define a unique team order, but players in the same team must be next to each other!</b>
	 * 
	 * @return player indices in an order defined by the <code>teamIndexComparator</code>
	 */
	public int[] getTeamOrderPlayerIndices( final Comparator< int[] > teamIndexComparator ) {
		if ( teamOrderPlayerIndices == null ) {
			final int[][] teamIndexPairs = new int[ players.length ][];
			for ( int i = 0; i < teamIndexPairs.length; i++ )
				teamIndexPairs[ i ] = new int[] { players[ i ].team, i };
			
			Arrays.sort( teamIndexPairs, teamIndexComparator );
			
			teamOrderPlayerIndices = new int[ players.length ];
			for ( int i = 0; i < teamIndexPairs.length; i++ )
				teamOrderPlayerIndices[ i ] = teamIndexPairs[ i ][ 1 ];
		}
		
		return teamOrderPlayerIndices;
	}
	
	/**
	 * Rearranges the Players array based on a favored player list.<br>
	 * Players with lower index in the list has higher priority. Moving a player also moves his/her team.<br>
	 * This method also sets the teamOrderPlayerIndices so that teams of (higher) favored players will be in the front of list.<br>
	 * Calling this method with an empty list will not change anything.
	 * @param favoredPlayerList list of favored players to use when rearranging
	 */
	public void rearrangePlayers( final List< String > favoredPlayerList ) {
		if ( favoredPlayerList.isEmpty() )
			return;
		
		final List< Integer > teamFavorIndexList = new ArrayList< Integer >( favoredPlayerList.size() );
		for ( final String playerName : favoredPlayerList ) {
			for ( final Player player : players )
				if ( player.playerId.name.equals( playerName ) ) {
					if ( !teamFavorIndexList.contains( player.team ) )
						teamFavorIndexList.add( player.team );
					break;
				}
		}
		
		// Force to recalculate teamOrderPlayerIndices:
		teamOrderPlayerIndices = null;
		
		getTeamOrderPlayerIndices( new Comparator< int[] >() {
			@Override
			public int compare( final int[] pair1, final int[] pair2 ) {
				if ( pair1[ 0 ] == pair2[ 0 ] ) { // Same team, player favor index decides...
					final int favoredPlayerIndex1 = favoredPlayerList.indexOf( players[ pair1[ 1 ] ].playerId.name );
					final int favoredPlayerIndex2 = favoredPlayerList.indexOf( players[ pair2[ 1 ] ].playerId.name );
					
					if ( favoredPlayerIndex1 < 0 && favoredPlayerIndex2 >= 0 )       // Only player 2 is favored, he/she must be earlier in the array
						return 1;
					else if ( favoredPlayerIndex1 >= 0 && favoredPlayerIndex2 < 0 )  // Only player 1 is favored, he/she must be earlier in the array
						return -1;
					else if ( favoredPlayerIndex1 >= 0 && favoredPlayerIndex2 >= 0 ) // Both players are favored...
						return favoredPlayerIndex1 - favoredPlayerIndex2;
					return 0;                                                        // None of the players are favored, leave them in replay order
				}
				else {
					final int favoredTeamIndex1 = teamFavorIndexList.indexOf( pair1[ 0 ] );
					final int favoredTeamIndex2 = teamFavorIndexList.indexOf( pair2[ 0 ] );
					
					if ( favoredTeamIndex1 < 0 && favoredTeamIndex2 >= 0 )           // Only team 2 is favored, it must be earlier in the array
						return 1;
					else if ( favoredTeamIndex1 >= 0 && favoredTeamIndex2 < 0 )      // Only team 1 is favored, it must be earlier in the array
						return -1;
					else if ( favoredTeamIndex1 >= 0 && favoredTeamIndex2 >= 0 ) {   // Both teams are favored...
						if ( favoredTeamIndex1 != favoredTeamIndex2 )                // ...but not equally
							return favoredTeamIndex1 - favoredTeamIndex2;
					}
				}
				
				return pair1[ 0 ] - pair2[ 0 ];                                      // Non-favored teams, return the default order
			}
		} );
	}
	
	/**
	 * Sets the isWinner property for a team.
	 * @param team     team whose isWinner property to set
	 * @param isWinner the value of isWinner
	 */
	public void setTeamIsWinner( final int team, final Boolean isWinner ) {
		for ( final Player player : players )
			if ( player.team == team && player.isWinner == null )
				player.isWinner = isWinner;
	}
	
}
