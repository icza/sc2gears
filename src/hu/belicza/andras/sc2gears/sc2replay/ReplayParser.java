/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.sc2replay;

import static hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.LARVA_SPAWNING_DURATION;
import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.VersionCompatibility;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils.AbilityCodesRepository;
import hu.belicza.andras.sc2gears.sc2replay.model.Details;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.BaseUseAbilityAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.HotkeyAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.MoveScreenAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.RequestResoucesAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.SaveGameAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.SelectAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.SendResourcesAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.UseUnitAbilityAction;
import hu.belicza.andras.sc2gears.sc2replay.model.InitData;
import hu.belicza.andras.sc2gears.sc2replay.model.InitData.Client;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents.Blink;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents.Message;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents.Text;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gearspluginapi.api.enums.League;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Difficulty;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ExpansionLevel;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Format;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Gateway;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerColor;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitAbility;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Replay parser.
 * 
 * <p>Version independent parsing logic is here.</p>
 * 
 * <p>This parser is single-threaded: you cannot call multiple parse methods from different threads simultaneously.</p>
 * 
 * @author Andras Belicza
 */
abstract class ReplayParser extends Parser {
	
	/** "s2ma" byte array.                              */
	private static final byte[] S2MA = new byte[] { 0x73, 0x32, 0x6d, 0x61 };
	
	/** Initialization event type. */
	private   static final int EVENT_TYPE_INITIALIZATION    = 0x00;
	/** Action event type.         */
	private   static final int EVENT_TYPE_ACTION            = 0x01;
	/** "Unnamed" event type.      */
	private   static final int EVENT_TYPE_UNNAMED           = 0x02;
	/** Replay event type.         */
	private   static final int EVENT_TYPE_REPLAY            = 0x03;
	/** Inaction event type.       */
	private   static final int EVENT_TYPE_INACTION          = 0x04;
	/** System event type.         */
	private   static final int EVENT_TYPE_SYSTEM            = 0x05;
	
	/** Op code indicating auto sync.                       */
	private   static final byte OP_CODE_AUTO_SYNC           = 0x00;
	/** Op code indicating game start.                      */
	private   static final byte OP_CODE_GAME_START          = 0x05;
	/** Op code indicating setting alliance.                */
	private   static final byte OP_CODE_ALLIANCE            = 0x06;
	/** Op code indicating game save.                      */
	private   static final byte OP_CODE_SAVE_GAME           = 0x06;
	/** Op code indicating player leave.                    */
	private   static final byte OP_CODE_PLAYER_LEAVE        = 0x09;
	/** Op code indicating usage of any kind of ability.
	 * Includes constructing buildings, training units, moving, attacking, gathering resources, and so on.
	 * High part of the op code for use ability may vary (for example 0x0b, 0x1b, 0x3b...) . */
	protected static final byte OP_CODE_USE_ABILITY         = 0x0b;
	/** Op code indicating follow-up on select.             */
	private   static final byte OP_CODE_SELECT_FOLLOW_UP    = (byte) 0x89;
	/** Op code indicating selection or deselection.        */
	protected static final byte OP_CODE_SELECT_DESELECT     = (byte) 0xac;
	/** Op code indicating increase game speed.             */
	private   static final byte OP_CODE_INCREASE_GAME_SPEED = (byte) 0x83;
	/** Op code indicating request resources.               */
	private   static final byte OP_CODE_REQUEST_RESOURCES   = (byte) 0xc6;
	/** Op code indicating increase game speed.             */
	private   static final byte OP_CODE_DECREASE_GAME_SPEED = 0x73;
	
	/** Name of the replay file (for debug purposes only).  */
	private final String   fileName;
	/** Reference to the replay.                            */
	protected final Replay replay;
	/** Op code of the action being parsed.                 */
	protected byte opCode;
	/** Game events contain the slot number not the player number, so we need this. */
	protected final int[] slotPlayerIndices = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
	
	/** Version compatibility of the replay. */
	protected final VersionCompatibility versionCompatibility;
	
	/**
	 * Creates a new ReplayParser.
	 * @param fileName             name of the replay file
	 * @param replay               reference to the replay
	 * @param versionCompatibility version compatibility of the replay
	 */
	public ReplayParser( final String fileName, final Replay replay, final VersionCompatibility versionCompatibility ) {
		this.fileName             = fileName;
		this.replay               = replay;
		this.versionCompatibility = versionCompatibility;
	}
	
	/**
	 * Parses replay init data from the given data.
	 * @param data data of the replay init data
	 */
	public void parseInitData( final byte[] data ) {
		final InitData initData = replay.initData = new InitData();
		
		setWrapper( data, ByteOrder.LITTLE_ENDIAN );
		
		// Clients init data
		
		final int maxClientsCount = wrapper.get() & 0xff;
		final List< Client > clientList = new ArrayList< Client >();
		// From version 2.0 structure changed
		if ( versionCompatibility.compareTo( VersionCompatibility.V_2_0 ) <= 0 ) {
			// From 2.0
			final BitInputStream bitin = new BitInputStream( wrapper );
			for ( int i = 0; i < maxClientsCount; i++ ) {
				final Client client = new Client();
				final int nameLength = bitin.read();
				// Names are always byte aligned so align to the next byte
				bitin.clearByte();
				// We can simply read from the underlying wrapper, bitin will not contain cached bits.
				client.name = readStringWithLength( nameLength );
				if ( bitin.readBoolean() ) {
					final int clanLength = bitin.read();
					bitin.clearByte();
					final String clanName = readStringWithLength( clanLength );
					if ( !clanName.isEmpty() )
						client.name = '[' + clanName + ']' + client.name;
				}
				// Clan logo introduced in 2.1:
				if ( versionCompatibility.compareTo( VersionCompatibility.V_2_1 ) <= 0 ) {
					if ( bitin.readBoolean() ) {
						bitin.clearByte();
						wrapper.position( wrapper.position() + 40 ); // Clan logo depot file
					}
				}
				if ( bitin.readBoolean() ) {
					final int league = bitin.read();
					if ( league == 8 )
						client.league = League.UNRANKED;
					else if ( league <= 0 ) 
						client.league = League.UNKNOWN;
					else
						client.league = EnumCache.LEAGUES[ League.UNRANKED.ordinal() - league ];
				}
				if ( bitin.readBoolean() )
					client.swarmLevels = bitin.readInt();
				
				bitin.readInt(); // Random seed
				if ( bitin.readBoolean() )
					bitin.read(); // Race preference
				if ( bitin.readBoolean() )
					bitin.read(); // Team preference
				bitin.readBoolean(); // Test map
				bitin.readBoolean(); // Test auto
				bitin.readBoolean(); // Examine
				bitin.readBoolean(); // Custom interface
				bitin.readBits( 2 ); // Observe
				
				clientList.add( client );
			}
			
			// Game init data
			
			bitin.readInt(); // Random value
			final int gameCacheNameLength = bitin.readBits( 10 );
			bitin.clearByte();
			readStringWithLength( gameCacheNameLength ); // Usually "Dflt" (default)
			bitin.readBoolean(); // Lock teams
			bitin.readBoolean(); // Teams together
			bitin.readBoolean(); // Advanced shared control
			bitin.readBoolean(); // Random races
			bitin.readBoolean(); // Battle.net
			bitin.readBoolean(); // Amm
			initData.competitive = Boolean.valueOf( bitin.readBoolean() );
			bitin.readBoolean(); // No victory or defeat
			bitin.readBits( 2 ); // Fog
			bitin.readBits( 2 ); // Observers
			bitin.readBits( 2 ); // User difficulty
			bitin.readInt(); bitin.readInt(); // Client debug flags
			bitin.readBits( 3 ); // Game speed
			bitin.readBits( 3 ); // Game type
		}
		else {
			// Before 2.0
			for ( int i = 0; i < maxClientsCount; i++ ) {
				final Client client = new Client();
				client.name = readString();
				// In case of client names there might be empty ones which are followed further real names, so we have to read all 16
				clientList.add( client );
				wrapper.position( wrapper.position() + 5 ); // Unknown, always seems to be zeros
			}
		}
		initData.clients = clientList.toArray( new Client[ clientList.size() ] );
		final List< String > clientNameList = new ArrayList< String >();
		for ( final Client client : clientList )
			clientNameList.add( client.name );
		initData.clientNames = clientNameList.toArray( new String[ clientNameList.size() ] );
		
		// Dependencies follow. The last one is the "real" map (containing the map preview image).
		boolean foundMapHash = false;
		int lastS2maPos = wrapper.position();
		while ( positionAfter( S2MA ) ) {
			lastS2maPos = wrapper.position();
			foundMapHash = true;
		}
		
		if ( foundMapHash ) {
			wrapper.position( lastS2maPos );
			wrapper.getShort();
			initData.gateway = Gateway.fromBinaryValue( readStringWithLength( 2 ) );
			final String mapFileHashString = GeneralUtils.convertToHexString( data, wrapper.position(), 32 );
			initData.mapFileName = new StringBuilder().append( mapFileHashString.charAt( 0 ) ).append( mapFileHashString.charAt( 1 ) ).append( '/' )
				.append( mapFileHashString.charAt( 2 ) ).append( mapFileHashString.charAt( 3 ) ).append( '/' )
				.append( mapFileHashString ).append( ".s2ma" ).toString();
		}
		else {
			// TODO read map name in case of single player type
			//initData.mapFileName = "";
			initData.gameType = GameType.SINGLE_PLAYER;
		}
	}
	
	/**
	 * Parses replay details from the given data.
	 * @param data data of the replay details
	 */
	public void parseDetails( final byte[] data ) {
		final Details details = replay.details = new Details();
		
		setWrapper( data, ByteOrder.LITTLE_ENDIAN );
		
		final Object[] dtls = (Object[]) readStructure();
		//printStructure( dtls, "" );
		
		final Object[] playersArr = (Object[]) dtls[ 0 ];
		final int playersCount = playersArr.length;
		final List< Player > playerList = new ArrayList< Player >( playersCount );
		for ( int i = 0; i < playersCount; i++ ) {
			final Player player = new Player();
			
			final Object[] playerArr = (Object[]) playersArr[ i ];
			
			String name = byteArrToString( playerArr[ 0 ] );
			if ( name.length() == 0 ) {
				player.playerId.name = "";
				break;
			}
			
			// The read player name contains the clan tag and optional formatting tags (in mark-up format).
			// Example: "[RA]<sp/>SvnthSyn"
			// Strip these off
			int start;
			while ( ( start = name.indexOf( '<' ) ) >= 0 )
				name = name.substring( 0, start ) + name.substring( name.indexOf( '>', start ) + 1 );
			player.nameWithClan = name;
			if ( ( start = name.indexOf( '[' ) ) >= 0 )
				name = name.substring( 0, start ) + name.substring( name.indexOf( ']', start ) + 1 );
			player.playerId.name = name;
			
			for ( final Client client : replay.initData.clients )
				if ( client.name.equals( player.nameWithClan ) ) {
					player.client = client;
					break;
				}
			
			final Object[] playerIdArr = (Object[]) playerArr[ 1 ];
			player.playerId.battleNetSubId = ( (Number) playerIdArr[ 2 ] ).intValue();
			player.playerId.battleNetId    = ( (Number) playerIdArr[ 3 ] ).intValue();
			
			if ( player.playerId.battleNetSubId != 0 && player.playerId.battleNetId != 0 )
				player.playerId.gateway = replay.initData.gateway; // Copy gateway from initData
			
			player.raceString = byteArrToString( playerArr[ 2 ] );
			
			final Object[] colorArr = (Object[]) playerArr[ 3 ];
			for ( int j = 0; j < 4; j++ )
				player.argbColor[ j ] = ( (Number) colorArr[ j ] ).intValue();
			
			// playerArr[ 6 ] is handicap (currently taken from attributes)
			// playerArr[ 7 ] is team     (currently taken from attributes)
			// playerArr[ 8 ] is the match result
			
			switch ( (Integer) playerArr[ 8 ] ) { // 1=>win, 2=>loss, 0=>unknown
			case 1 : player.isWinner = Boolean.TRUE ; break;
			case 2 : player.isWinner = Boolean.FALSE; break;
			}
			
			playerList.add( player );
		}
		
		details.players = playerList.toArray( new Player[ playerList.size() ] );
		
		details.originalMapName = byteArrToString( dtls[ 1 ] );
		details.mapName = Settings.getMapAliasGroupName( details.originalMapName );
		
		details.mapPreviewFileName = byteArrToString( ( (Object[]) dtls[ 3 ] )[ 0 ] );
		details.saveTime = ( ( (Number) dtls[ 5 ] ).longValue() - 116444736000000000L ) / 10000L;
		details.saveTimeZone = ( (Number) dtls[ 6 ] ).longValue() / ( 10000f * 1000 * 60 * 60 );
		
		final Object[] dependencies = (Object[]) dtls[ 10 ];
		details.dependencies = new String[ dependencies.length ];
		final byte[] ext = new byte[ 4];
		for ( int i = 0; i < dependencies.length; i++ ) {
			// Dependency: 4 byte extension ("s2ma"), 4 byte gw code ("\0\0EU") and the rest is the SHA-256 data.
			final byte[] dep = (byte[]) dependencies[ i ];
			System.arraycopy( dep, 0, ext, 0, 4 );
			details.dependencies[ i ] = GeneralUtils.convertToHexString( dep, 8, dep.length - 8 ) + "." + new String( ext, Consts.UTF8 );
		}
		// Order is important because HotS also requires WoL!
		expansionCycle:
		for ( final ExpansionLevel expansion : EnumCache.EXPANSIONS ) {
			for ( final String dependency : details.dependencies ) {
				if ( expansion.dependency.equals( dependency ) ) {
					details.expansion = expansion;
					break expansionCycle;
				}
			}
		}
	}
	
	/**
	 * Parses replay attributes events from the given data.
	 * @param data data of the replay details
	 */
	public void parseAttributesEvents( final byte[] data ) {
		if ( data.length == 0 )
			return;
		setWrapper( data, ByteOrder.LITTLE_ENDIAN );
		wrapper.getInt(); // Header (zeros)
		// From version 1.2 there is an extra zero here!
		if ( versionCompatibility.compareTo( VersionCompatibility.V_1_2 ) <= 0 )
			wrapper.get();
		
		// Game format will tell what attribute id holds the proper team info
		// Since there is no guarantee what the attribute order is (and it is the last anyway),
		// we have to cache all team info
		final List< Pair< Integer, String > > teamInfo1v1List = new ArrayList< Pair< Integer,String > >( 4 );
		final List< Pair< Integer, String > > teamInfo2v2List = new ArrayList< Pair< Integer,String > >( 4 );
		final List< Pair< Integer, String > > teamInfo3v3List = new ArrayList< Pair< Integer,String > >( 6 );
		final List< Pair< Integer, String > > teamInfo4v4List = new ArrayList< Pair< Integer,String > >( 8 );
		final List< Pair< Integer, String > > teamInfoFfaList = new ArrayList< Pair< Integer,String > >( 8 );
		
		final Player[] players = replay.details.players;
		
		final boolean[] slotOpenStates = new boolean[ 8 ];
		final int attributesCount = wrapper.getInt();
		for ( int i = 0; i < attributesCount; i++ ) {
			wrapper.getInt(); // attribute header
			final short attributeId = wrapper.getShort();
			wrapper.getShort(); // zeros
			final int slot = ( wrapper.get() & 0xff ) - 1;
			final int player = slotPlayerIndices[ slot < 8 ? slot : 0 ];
			final String value = readStringWithLength( 4 );
			
			// Even if a slot is open, info might be stored for that "player"; but we don't want to store those info (negative player would cause exception too)
			// Moreover if game type is 4v4 with 4 players in it and with 4 open slots, the info in those 4 open slots
			// might overlap/override the info of the 4 real players with false information!
			if ( slot < 8 && player < 0 || slot < slotOpenStates.length && slotOpenStates[ slot ] )
				continue;
			
			switch ( attributeId ) {
			case (short) 0x01f4 : {
				final PlayerType type = PlayerType.fromBinaryValue( value );
				if ( type == PlayerType.OPEN ) { // An open slot, shift the player indices
					for ( int j = slotPlayerIndices.length - 1; j >= slot; j-- )
						slotPlayerIndices[ j ]--;
					if ( slot < slotOpenStates.length )
						slotOpenStates[ slot ] = true;
				}
				else 
					players[ player ].type = type;
				break;
			}
			case (short) 0x0bb9 : players[ player ].race = Race.fromBinaryValue( value ); break;
			case (short) 0x07d1 : replay.initData.format = Format.fromBinaryValue( value.indexOf( 0 ) < 0 ? value : value.substring( 0, value.indexOf( 0 ) ) ); break;
			case (short) 0x07d2 : teamInfo1v1List.add( new Pair< Integer, String >( player, value ) ); break; // 1v1 team info
			case (short) 0x07d3 : teamInfo2v2List.add( new Pair< Integer, String >( player, value ) ); break; // 2v2 team info
			case (short) 0x07d4 : teamInfo3v3List.add( new Pair< Integer, String >( player, value ) ); break; // 3v3 team info
			case (short) 0x07d5 : teamInfo4v4List.add( new Pair< Integer, String >( player, value ) ); break; // 4v4 team info
			case (short) 0x07d6 : teamInfoFfaList.add( new Pair< Integer, String >( player, value ) ); break; // FFA team info
			//case (short) 0x07d7 : // supposedly this is 5v5 team info
			//case (short) 0x07d8 : // supposedly this is 6v6 team info
			case (short) 0x0bb8 : replay.initData.gameSpeed = GameSpeed.fromBinaryValue( value ); break;
			case (short) 0x0bc1 : replay.initData.gameType = GameType.fromBinaryValue( value.indexOf( 0 ) < 0 ? value : value.substring( 0, value.indexOf( 0 ) ) ); break;
			//case (short) 0x0bbf : break; // Value is "traP", tells if the player is a participant
			case (short) 0x0bbc : players[ player ].difficulty = Difficulty.fromBinaryValue( value ); break;
			case (short) 0x0bba : players[ player ].playerColor = PlayerColor.fromBinaryValue( value ); break;
			case (short) 0x0bbb : { // Handicap
				int handicap = 0, ch;
				for ( int idx = 3; idx >= 0; idx-- )
					if ( ( ch = value.charAt( idx ) ) != ' ' )
						handicap = handicap * 10 + ( ch - '0' );
				players[ player ].handicap = handicap;
				break;
			}
			//case (short) 0x0bbe : break; // Value here is always 7
			}
		}
		
		// Now process team info
		List< Pair< Integer, String > > teamInfoList = null;
		switch ( replay.initData.format ) {
		case ONE_VS_ONE     : teamInfoList = teamInfo1v1List; break;
		case TWO_VS_TWO     : teamInfoList = teamInfo2v2List; break;
		case THREE_VS_THREE : teamInfoList = teamInfo3v3List; break;
		case FOUR_VS_FOUR   : teamInfoList = teamInfo4v4List; break;
		case FREE_FOR_ALL   : teamInfoList = teamInfoFfaList; break;
		}
		if ( teamInfoList != null )
			for ( final Pair< Integer, String > teamInfo : teamInfoList )
				if ( teamInfo.value1 < players.length )
					players[ teamInfo.value1 ].team = teamInfo.value2.charAt( 0 ) - '0';
		// TODO maybe the team info in the player struct (in details) are more precise (sometimes this info states the same team in 1v1)
		// TODO the to-do just above needs revise, team detection has greatly improved when slotOpenStates was added!
		// TODO Another note: in custom games format of '1v1' might be recorded in attributes in case of team games which will make team info inaccurate!
		
		// The rest of the replay parsing uses this slot array.
		// From 2.0 this must be the client index.
		if ( versionCompatibility.compareTo( VersionCompatibility.V_2_0 ) <= 0 ) {
			final String[] clientNames         = replay.initData.clientNames;
			final String[] arrangedClientNames = replay.initData.getArrangedClientNames( replay.details.players );
			for ( int i = 0; i < clientNames.length; i++ ) {
				final String clientName = clientNames[ i ];
				for ( int j = 0; j < arrangedClientNames.length; j++ )
					if ( clientName.equals( arrangedClientNames[ j ] ) ) {
						slotPlayerIndices[ i ] = j;
						break;
					}
			}
			
		}
	}
	
	/**
	 * Parses replay message events from the given data.
	 * @param data data of the message events
	 */
	public void parseMessageEvents( final byte[] data ) {
		final MessageEvents messageEvents = replay.messageEvents = new MessageEvents();
		
		// From version 1.5.3 message length specification slightly differs
		final boolean belowVer153 = versionCompatibility.compareTo( VersionCompatibility.V_1_5_3 ) > 0;
		// Below version 2.0 client index is stored as +1
		final boolean belowVer20  = versionCompatibility.compareTo( VersionCompatibility.V_2_0 ) > 0;
		
		setWrapper( data, ByteOrder.LITTLE_ENDIAN );
		
		final List< Message > messageList = new ArrayList< Message >();
		
		while ( wrapper.hasRemaining() ) {
			// Parse time
			final int indicator = wrapper.get() & 0xff;
			int time = indicator >> 2;
			if ( ( indicator & 0x01 ) != 0 )
				time = ( time << 8 ) + ( wrapper.get() & 0xff );
			else if ( ( indicator & 0x02 ) != 0 )
				time = ( time << 16 ) + ( ( wrapper.get() & 0xff ) << 8 ) + ( wrapper.get() & 0xff );
			
			int clientIdx = wrapper.get() & 0x0f;
			if ( belowVer20 )
				clientIdx--;
			opCode = wrapper.get();
			
			switch ( opCode ) {
			case MessageEvents.OP_CODE_SYNC :
				// Some kind of sync messages (usually at the beginning of the message events file
				wrapper.getInt();
				break;
			case MessageEvents.OP_CODE_BLINK : {
				time = time * 125 / 2; // Convert to ms
				final Blink blinkMessage = new Blink( time, slotPlayerIndices[ clientIdx ] );
				blinkMessage.x = Integer.reverseBytes( wrapper.getInt() ) >> 1; // Double of the "usual" coordinate format
				blinkMessage.y = Integer.reverseBytes( wrapper.getInt() ) >> 1; // Double of the "usual" coordinate format
				messageList.add( blinkMessage );
				break;
				}
			default :
				if ( ( opCode & 0x80 ) == 0 ) {
					time = time * 125 / 2; // Convert to ms
					final Text textMessage = new Text( time, slotPlayerIndices[ clientIdx ], (byte) ( opCode & 0x07 ) );
					int length = wrapper.get() & 0xff;
					// From version 1.5.3 this bit does not mean we have to add 64 to the length
					if ( belowVer153 && ( opCode & 0x08 ) != 0 )
						length += 64;
					if ( ( opCode & 0x10 ) != 0 )
						length += 128;
					if ( ( opCode & 0x20 ) != 0 )
						length += 256;
					if ( ( opCode & 0x40 ) != 0 )
						length += 512;
					textMessage.text = readStringWithLength( length );
					messageList.add( textMessage );
				}
				break;
			}
		}
		
		messageEvents.messages = messageList.toArray( new Message[ messageList.size() ] );
	}
	
	private boolean unrecognizedActionFound = false;
	
	// Version specific field lengths (bit sizes)
	/** Number of bits an index is represented with. It's 9 bits from version 1.5, and it's 8 bits before 1.5. */
	protected int idxTypeBitLength;
	/** Number of bits of the use ability flags field. 13 bits before 1.3.3, 14 bits from 1.3.3 up to 1.5.0 (exclusive); 16 bits from 1.5.0.
	 * This value excludes the 4 bits taken from the opCodeHigh (mask 0xf0).  */
	protected int useAbilityFlagsLength;
	
	/**
	 * Parses game events from the given data.
	 * @param data data of the game events
	 */
	public void parseGameEvents( final byte[] data ) {
		idxTypeBitLength      = ReplayUtils.compareVersions( replay.buildNumbers, new int[] { 1, 5 } ) >= 0 ? 9 : 8;
		useAbilityFlagsLength = ReplayUtils.compareVersions( replay.buildNumbers, new int[] { 1, 5 } ) >= 0 ? 16
			: ReplayUtils.compareVersions( replay.buildNumbers, new int[] { 1, 3, 3 } ) >= 0 ? 14 : 13;
		
		final AbilityCodes abilityCodes = AbilityCodesRepository.getForVersion( replay.buildNumbers );
		
		final Player[] players = replay.details.players;
		final byte playersCount = (byte) players.length;
		
		// For tracking teams (for Winner determination)
		// Maps from teams to team members.
		final Map< Integer, List< Integer > > teamMemberListMap = new HashMap< Integer, List< Integer > >();
		for ( int i = 0; i < players.length; i++ ) {
			final int team = players[ i ].team;
			List< Integer > memberList = teamMemberListMap.get( team );
			if ( memberList == null )
				teamMemberListMap.put( team, memberList = new ArrayList< Integer >( 4 ) );
			memberList.add( i );
		}
		
		replay.gameEvents = new GameEvents( replay, abilityCodes );
		
		setWrapper( data, ByteOrder.LITTLE_ENDIAN );
		
		// Assume there will be errors parsing the events. If everything goes OK, we will set this to false.
		replay.gameEvents.errorParsing = true;
		
		// Below version 2.0 client index is stored as +1
		final boolean belowVer20  = versionCompatibility.compareTo( VersionCompatibility.V_2_0 ) > 0;
		
		final List< Action > actionList = new ArrayList< Action >( 5000 );
		int accumulatedFrames = 0;
		Action action;
		final int excludedInitialFrames = replay.excludedInitialFrames = Settings.getInt( Settings.KEY_SETTINGS_MISC_INITIAL_TIME_TO_EXCLUDE_FROM_APM ) << ReplayConsts.FRAME_BITS_IN_SECOND;
		int lastActionStart = 0, actionStart = 0; byte lastOpCode; opCode = 0; // Remove this line in production
		
		while ( wrapper.hasRemaining() ) {
			lastActionStart = actionStart; lastOpCode = opCode;
			actionStart = wrapper.position();
			
			// Little different time parsing than at the message events
			final int indicator = wrapper.get() & 0xff;
			int time = indicator >> 2;
			if ( ( indicator & 0x01 ) != 0 )
				time = ( time << 8 ) + ( wrapper.get() & 0xff );
			else if ( ( indicator & 0x02 ) != 0 )
				time = ( wrapper.get() & 0xff ) >> 2;
			accumulatedFrames += time << 2;
			
			final byte eventDesc = wrapper.get();
			// Event description format: [event type 3 bits][global event flag 1 bit][slot id 4 bits]
			final byte player;
			if ( belowVer20 )
				player = (byte) ( ( eventDesc & 0x0f ) == 0 ? -1 : slotPlayerIndices[ ( eventDesc & 0x0f ) - 1 ] );
			else
				player = (byte) slotPlayerIndices[ eventDesc & 0x0f ];
			
			opCode = wrapper.get();
			final int opCodeHigh = opCode & 0xf0, opCodeLow = opCode & 0x0f;
			//System.out.println( String.format( "0x%02x" , opCode ) + " (pos: " + String.format( "0x%04x", actionStart ) + ", event desc: " + String.format( "0x%02x", eventDesc ) + ")" );
			
			action = null;
			
			switch ( ( eventDesc & 0xe0 ) >> 5 ) { // event type
			
			case EVENT_TYPE_INITIALIZATION : {
				if ( opCodeLow == 0x0b || opCodeLow == 0x0c || opCodeLow == 0x07 ) { // 0x07 from version 2.0; it ends with 0x0c from version 1.2 (and 0x0b before that)
					// Player init, player joins the game
					// No parameters in old versions, 1 byte from 1.5.0, 5 bytes from 2.0
					// VC's are listed version-descending order...
					if ( !belowVer20 )
						wrapper.getInt();
					if ( versionCompatibility.compareTo( VersionCompatibility.V_1_5 ) <= 0 ) // VC's are listed version-descending order...
						wrapper.get();
				} else if ( opCodeLow == 0x0e ) { // Introduced in 2.0.8
					wrapper.position( wrapper.position() + 4 ); // Unknown
				}
				else switch ( opCode ) {
				case OP_CODE_GAME_START :
					// No parameters
					break;
				/*case 0x17 :
					// TODO Experienced with custom games, some kind of stored stats (from previous games? from battle.net server?)
					break;*/
				default:
					handleUnrecognizedAction( eventDesc, "INITIALIZATION", lastOpCode, actionList.size(), lastActionStart );
					break;
				}
				break;
			}
			
			case EVENT_TYPE_ACTION : case 0x06 : { // 0x06 from 2.0
				if ( opCode == OP_CODE_SELECT_DESELECT || opCodeLow == 0x0c && opCodeHigh < 0xa0 )
					action = readSelectAction();
				else if ( opCodeLow == 0x0b ) // Op code indicating usage of any kind of ability. Includes constructing buildings, training units, moving, attacking, gathering resources, and so on.
					action = readUseAbilityAction( abilityCodes );
				else if ( opCodeLow == 0x0d && opCodeHigh < 0xa0 )
					action = readHotkeyAction();
				else if ( opCodeLow == 0x0f && opCodeHigh > 0x00 && opCodeHigh < 0x90 )
					action = readSendResourcesAction();
				else switch ( opCode ) {
				case OP_CODE_PLAYER_LEAVE : case 0x05 : { // 0x05 from 2.0
					if ( player < playersCount ) { // if player is higher, it's an observer leaving...
						action = replay.gameEvents.new LeaveGameAction();
						trackPlayerLeave( teamMemberListMap, player ); 
					}
					// No parameters
					break;
				}
				case OP_CODE_SAVE_GAME : {
					action = replay.gameEvents.new SaveGameAction();
					( (SaveGameAction) action ).fileName = readString();
					wrapper.getInt(); // Unknown
					break;
				}
				default:
					handleUnrecognizedAction( eventDesc, "ACTION", lastOpCode, actionList.size(), lastActionStart );
					break;
				}
				break;
			}
			
			case EVENT_TYPE_UNNAMED : {
				switch ( opCode ) {
				case OP_CODE_DECREASE_GAME_SPEED :
					action = replay.gameEvents.new DecreaseGameSpeedAction();
					wrapper.get();
					break;
				case OP_CODE_INCREASE_GAME_SPEED :
					action = replay.gameEvents.new IncreaseGameSpeedAction();
					wrapper.get();
					break;
				case OP_CODE_ALLIANCE :
					action = replay.gameEvents.new AllianceAction();
					wrapper.position( wrapper.position() + 8 );
					break;
				case 0x07 :
					// Unknown, 4 bytes data
					wrapper.getInt();
					break;
				case 0x0e :
					// Unknown, 4 bytes data
					wrapper.getInt();
					break;
				case (byte) 0x8f :
					// Unknown, 4 bytes data
					wrapper.getInt();
					break;
				case (byte) 0x42 :
					// Unknown, no data
					break;
				default:
					handleUnrecognizedAction( eventDesc, "UNNAMED", lastOpCode, actionList.size(), lastActionStart );
					break;
				}
				break;
			}
			
			case EVENT_TYPE_REPLAY : {
				if ( opCodeLow == 0x01 )
					action = readMoveScreenAction();
				else switch ( opCode ) {
				case 0x08 : {
					// Unknown
					// There are 2 bytes data + n*4 where first byte gives the length:
					// n = ( firstByte & 0x0f ) << 1;
					final short d = wrapper.getShort();
					wrapper.position( wrapper.position() + ( ( d & 0x0f ) << 1 ) * 4 );
					break;
				}
				case (byte) 0x87 :
					// Unknown, parameters are 8 bytes (2 integers?)
					wrapper.position( wrapper.position() + 8 );
					// From version 2.0 its 12 bytes
					if ( versionCompatibility.compareTo( VersionCompatibility.V_2_0 ) <= 0 ) {
						wrapper.position( wrapper.position() + 4 );
					}
					break;
				case 0x18 :
					// TODO: one replay with this action, was 250 bytes payload, has to be verified
					wrapper.position( wrapper.position() + 250 );
					break;
				case (byte) 0x80 :
					// Unknown, 4 bytes data
					wrapper.getInt();
					break;
				case (byte) 0x88 :
					// TODO: this seems some kind of spam hack action
					wrapper.position( wrapper.position() + 514 );
					break;
				case 0x0c :
					wrapper.getShort(); // Unknown
					break;
				default:
					handleUnrecognizedAction( eventDesc, "REPLAY", lastOpCode, actionList.size(), lastActionStart );
					break;
				}
				break;
			}
			
			case EVENT_TYPE_INACTION : {
				if ( opCodeLow == 0x08 && opCodeHigh > 0x00 && opCodeHigh < 0x90 ) {
					// Cancel resource request
					action = replay.gameEvents.new CancelResRequestAction();
					wrapper.getInt();
				} else if ( opCodeLow == 0x0c ) {
					// There are some system generated events with no data
				} else switch ( opCode ) {
				case OP_CODE_REQUEST_RESOURCES :
					action = readRequestResourcesAction();
					break;
				case OP_CODE_AUTO_SYNC :
					wrapper.getInt();
					break;
				case (byte) 0x87 :
					// Unknown, parameters are 4 bytes
					wrapper.getInt();
					break;
				case (byte) 0x82 :
					// Unknown, parameters are 2 bytes
					wrapper.getShort();
					break;
				default:
					handleUnrecognizedAction( eventDesc, "INACTION", lastOpCode, actionList.size(), lastActionStart );
					break;
				}
				break;
			}
			
			case EVENT_TYPE_SYSTEM : {
				switch ( opCode ) {
				case OP_CODE_SELECT_FOLLOW_UP :
					wrapper.getInt();
					break;
				case (byte) 0x8c :
					wrapper.get(); // Unknown
					break;
				default:
					handleUnrecognizedAction( eventDesc, "SYSTEM", lastOpCode, actionList.size(), lastActionStart );
					break;
				}
				break;
			}
			
			default:
				handleUnrecognizedAction( eventDesc, null, lastOpCode, actionList.size(), lastActionStart );
				break;
			
			}
			
			if ( action != null && player >= 0 && player < playersCount ) {
				action.player = player;
				action.opCode = opCode;
				action.frame  = accumulatedFrames;
				if ( action.type != ActionType.INACTION ) {
					players[ player ].lastActionFrame = accumulatedFrames;
					players[ player ].actionsCount++;
					if ( accumulatedFrames < excludedInitialFrames )
						players[ player ].excludedActionsCount++;
				}
				
				actionList.add( action );
			}
		}
		
		if ( !unrecognizedActionFound )
			replay.gameEvents.errorParsing = false;
		
		completeWinnerDetection( teamMemberListMap );
		
		
		// POST PROCESSING ACTIONS
		
		final Action[] actions = replay.gameEvents.actions = actionList.toArray( new Action[ actionList.size() ] );
		final int actionsLength = actions.length;
		int player;
		
		// For Avg spawning ratio and Avg injection gap:
		// 1 for each chart. Elements: pairs: unit id, frame list when spawn larvas occur
		@SuppressWarnings("unchecked")
		final List< Pair< Integer, List< Integer > > >[] hatchSpawnTimesLists = new List[ players.length ];
		
		for ( int i = 0; i < actionsLength; i++ ) {
			action = actions[ i ];
			player = action.player;
			
			// Count effective actions
			if ( action.type != ActionType.INACTION && EapmUtils.getActionIneffectiveReason( actions, i ) == null ) {
				players[ player ].effectiveActionsCount++;
				if ( action.frame < excludedInitialFrames )
					players[ player ].excludedEffectiveActionsCount++;
			}
			
			// For Avg spawning ratio and Avg injection gap: first gather injections
			// Zerg spawn larva
			if ( action instanceof UseUnitAbilityAction && !( (BaseUseAbilityAction) action ).isAbilityFailed() ) {
				final UseUnitAbilityAction uuaa = (UseUnitAbilityAction) action;
				if ( uuaa.unitAbility == UnitAbility.SPAWN_LARVA ) {
					if ( hatchSpawnTimesLists[ player ] == null )
						hatchSpawnTimesLists[ player ] = new ArrayList< Pair< Integer, List< Integer > > >();
					
					Pair< Integer, List< Integer > > hatchSpawnTimes = null;
					// Has this Hatchery been injected before?
					for ( final Pair< Integer, List< Integer > > hatchSpawnTimes_ : hatchSpawnTimesLists[ player ] )
						if ( hatchSpawnTimes_.value1.intValue() == uuaa.targetId ) {
							hatchSpawnTimes = hatchSpawnTimes_;
							break;
						}
					if ( hatchSpawnTimes == null )
						hatchSpawnTimesLists[ player ].add( hatchSpawnTimes = new Pair< Integer, List< Integer > >( uuaa.targetId, new ArrayList< Integer >() ) );
					
					// Register spawn larva action
					if ( !hatchSpawnTimes.value2.isEmpty()
							&& hatchSpawnTimes.value2.get( hatchSpawnTimes.value2.size() - 1 ) + LARVA_SPAWNING_DURATION > action.frame )
						hatchSpawnTimes.value2.remove( hatchSpawnTimes.value2.size() - 1 ); // The last Spawn larva overlaps the current one: that means the last one was not executed for sure, remove it
					hatchSpawnTimes.value2.add( action.frame );
				}
			}
		}
		
		// For Avg spawning ratio and Avg injection gap: now calculate averages
		for ( int i = 0; i < hatchSpawnTimesLists.length; i++ ) { // Player cycle
			final List< Pair< Integer, List< Integer > > > hatchSpawnTimesList = hatchSpawnTimesLists[ i ];
			if ( hatchSpawnTimesList == null || hatchSpawnTimesList.isEmpty() )
				continue;
			
			final Player player_         = players[ i ];
			final int    lastActionFrame = player_.lastActionFrame;
			
			for ( final Pair< Integer, List< Integer > > hatchSpawnTimes : hatchSpawnTimesList ) { // Hatcheries cycle
				player_.totalHatchTime += lastActionFrame > hatchSpawnTimes.value2.get( 0 ) ? lastActionFrame - hatchSpawnTimes.value2.get( 0 ) : 0;
				final int hatchSpawnTimesCount = hatchSpawnTimes.value2.size();
				for ( int j = 0; j < hatchSpawnTimesCount; j++ ) { // Injections of a Hatchery cycle
					final int spawnTime = hatchSpawnTimes.value2.get( j );
					player_.totalHatchSpawnTime += Math.min( LARVA_SPAWNING_DURATION, lastActionFrame - spawnTime );
					if ( j > 0 ) {
						player_.totalInjectionGap += spawnTime - hatchSpawnTimes.value2.get( j-1 ) - LARVA_SPAWNING_DURATION;
						player_.totalInjectionGapCount++;
					}
				}
			}
		}
	}
	
	/**
	 * Handles the occurrence of an unrecognized action:
	 * prints some debug info and positions to the end of the underlying buffer. 
	 */
	private void handleUnrecognizedAction( final byte eventDesc, final String eventDescName, final byte lastOpCode, final int parsedActionsCount, final int lastActionStart ) {
		if ( eventDescName == null )
			System.out.println( "Unrecognized event desc: 0x" + String.format( "%02x", eventDesc )
				+ ", aborting parse. (" + fileName
				+ ")\n\t(last opcode: 0x" + String.format( "%02x", lastOpCode )
				+ ", total parsed: " + parsedActionsCount + ", stream pos: 0x"
				+ String.format( "%04x", wrapper.position() ) + ", last action startPos: 0x"
				+ String.format( "%04x", lastActionStart ) + ")" );
		else
    		System.out.println( "Unrecognized action (opCode=0x" + String.format( "%02x", opCode )
    			+ ", event desc=0x" + String.format( "%02x", eventDesc )
    			+ ": " + eventDescName + "), aborting parse. (" + fileName
    			+ ")\n\t(last opcode: 0x"
    			+ String.format( "%02x", lastOpCode ) + ", total parsed: " + parsedActionsCount + ", stream pos: 0x" + String.format( "%04x", wrapper.position() ) + ", last action startPos: 0x" + String.format( "%04x", lastActionStart ) + ")" );
		
		// Unrecognized action => we don't know the length of the action => have to abort parsing
		wrapper.position( wrapper.limit() );
		unrecognizedActionFound = true;
	}
	
	/**
	 * Reads a request resources action
	 * @return the read request resources action
	 */
	private Action readRequestResourcesAction() {
		final RequestResoucesAction rra = replay.gameEvents.new RequestResoucesAction();
		
		int res = Integer.reverseBytes( wrapper.getInt() );
		rra.minRequested = ( ( res & 0xffffff00 ) >> 1 ) + ( res & 0x7f );
		res = Integer.reverseBytes( wrapper.getInt() );
		rra.gasRequested = ( ( res & 0xffffff00 ) >> 1 ) + ( res & 0x7f );
		wrapper.getInt();
		wrapper.getInt();
		
		return rra;
	}
	
	/**
	 * Reads a send resources action
	 * @return the read send resources action
	 */
	private Action readSendResourcesAction() {
		final SendResourcesAction sra = replay.gameEvents.new SendResourcesAction();
		
		sra.targetPlayer = ( ( opCode & 0xf0 ) >> 4 ) - 1;
		wrapper.get(); // 0x84
		int res = Integer.reverseBytes( wrapper.getInt() );
		sra.minSent = ( res >> 8 << 3 ) + ( res & 0x0f );
		res = Integer.reverseBytes( wrapper.getInt() );
		sra.gasSent = ( res >> 8 << 3 ) + ( res & 0x0f );
		wrapper.position( wrapper.position() + 8 ); // Rest is unknown
		
		return sra;
	}
	
	/**
	 * Reads a move screen action.
	 * @return the read move screen action
	 */
	protected MoveScreenAction readMoveScreenAction() {
		final BitInputStream bitin = new BitInputStream( wrapper );
		
		final MoveScreenAction msa = replay.gameEvents.new MoveScreenAction();
		
		msa.flags = MoveScreenAction.FLAG_MASK_HAS_LOCATION;
		msa.x = ( opCode & 0xf0 ) << 8 | bitin.readBits( 12 );
		msa.y = bitin.readBits( 16 );
		
		final boolean hasDistance = bitin.readBoolean();
		if ( hasDistance ) {
			msa.flags |= MoveScreenAction.FLAG_MASK_HAS_DISTANCE;
			msa.distance = bitin.readBits( 16 );
		}
		
		final boolean hasPitch = bitin.readBoolean();
		if ( hasPitch ) {
			msa.flags |= MoveScreenAction.FLAG_MASK_HAS_PITCH;
			msa.pitch = bitin.readBits( 16 );
			// Convert to degrees and discard the last 4 bit in order for fast transformation
			msa.pitch = ( 45 * ( ( ( ( ( ( msa.pitch << 5 ) - 0x2000 ) << 17 ) - 1 ) >> 17 ) + 1 ) ) >> 4;
		}
		
		final boolean hasYaw = bitin.readBoolean();
		if ( hasYaw ) {
			msa.flags |= MoveScreenAction.FLAG_MASK_HAS_YAW;
			msa.yaw = bitin.readBits( 16 );
			// Convert to degrees and discard the last 4 bit in order for fast transformation
			msa.yaw = ( 45 * ( ( ( ( ( ( msa.yaw << 5 ) - 0x2000 ) << 17 ) - 1 ) >> 17 ) + 1 ) ) >> 4;
		}
		
		final boolean hasHeightOffset = bitin.readBoolean();
		if ( hasHeightOffset ) {
			msa.flags |= MoveScreenAction.FLAG_MASK_HAS_HEIGHT_OFFSET;
			msa.heightOffset = bitin.readBits( 16 );
		}
		
		return msa;
	}
	
	/**
	 * Reads a hotkey action.
	 * @return the read hotkey action
	 */
	protected abstract HotkeyAction readHotkeyAction();
	
	/**
	 * Reads the basics of a select action.
	 * @return the read select action
	 */
	private SelectAction readSelectAction() {
		final SelectAction sa = replay.gameEvents.new SelectAction();
		
		// Automatic hotkey transformation (for example a hotkeyed Hatchery transforms into a Lair)					
		sa.automatic = opCode != OP_CODE_SELECT_DESELECT;
		if ( sa.automatic )
			sa.type = ActionType.INACTION;
		
		readCustomSelectAction( sa );
		
		return sa;
	}
	
	/**
	 * Reads a custom (version specific) select action.
	 * @param sa reference to the select action
	 */
	protected abstract void readCustomSelectAction( final SelectAction sa );
	
	/**
	 * Reads a use ability action.
	 * @param abilityCodes ability codes to be used
	 * @return the read base use ability action
	 */
	protected abstract BaseUseAbilityAction readUseAbilityAction( final AbilityCodes abilityCodes );
	
	/**
	 * Tracks a player leave for the winner determination.
	 * @param teamMemberListMap map of the teams and their members still in "race"
	 * @param player player who just left
	 */
	private void trackPlayerLeave( final Map< Integer, List< Integer > > teamMemberListMap, final byte player ) {
		if ( teamMemberListMap.size() > 1 ) { // Leavers of the last team are not losers
			final int team = replay.details.players[ player ].team;
			final List< Integer > memberList = teamMemberListMap.get( team );
			memberList.remove( new Integer( player ) );
			if ( memberList.isEmpty() ) { // Loser team
				teamMemberListMap.remove( team );
				replay.details.setTeamIsWinner( team, Boolean.FALSE );
				if ( teamMemberListMap.size() == 1 ) { // One team remained: it is the winner team
					final int winnerTeam = teamMemberListMap.keySet().iterator().next();
					teamMemberListMap.remove( winnerTeam );
					replay.details.setTeamIsWinner( winnerTeam, Boolean.TRUE );
				}
			}
		}
	}
	
	/**
	 * Completes the winner detection based on the teams and members still in "race".
	 * @param teamMemberListMap map of the teams and their members still in "race"
	 */
	private void completeWinnerDetection( final Map< Integer, List< Integer > > teamMemberListMap ) {
		// If there is only one team left with human(s) in it and the rest are computer teams, then the human team wins (computers don't leave)
		final Player[] players = replay.details.players;
		
		final List< Integer > humanTeamList = new ArrayList< Integer >( teamMemberListMap.size() );
		final List< Integer > compTeamList  = new ArrayList< Integer >( teamMemberListMap.size() );
		int unknownTeamCounter = 0;
		for ( final Entry< Integer, List< Integer > > entry : teamMemberListMap.entrySet() ) {
			final int team = entry.getKey();
			// Since the type can be UNKNOWN, we have to store the only-computer and found-human test results too
			boolean computerOnly = true;
			boolean foundHuman   = false;
			for ( final Integer playerId : entry.getValue() )
				if ( players[ playerId ].type == PlayerType.HUMAN ) {
					foundHuman = true;
					break;
				}
				else if ( players[ playerId ].type == PlayerType.UNKNOWN ) {
					computerOnly = false;
					// We don't "break" here, because if we find a human, the team will "qualify" as human team 
				}
			if ( foundHuman )
				humanTeamList.add( team );
			else if ( computerOnly )
				compTeamList.add( team );
			else
				unknownTeamCounter++;
		}
		if ( unknownTeamCounter == 0 && humanTeamList.size() == 1 ) {
			replay.details.setTeamIsWinner( humanTeamList.get( 0 ), Boolean.TRUE );
			for ( final int compTeam : compTeamList )
				replay.details.setTeamIsWinner( compTeam, Boolean.FALSE );
		}
		// The largest remaining team wins
		// If there are multiple remaining teams with the same highest players count, it still remains unknown
		else if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_DECLARE_LARGEST_AS_WINNER ) && teamMemberListMap.size() > 1 ) { // If 1 team would've remained, it would've already been claimed winner and removed
			int maxTeamSize = 0;
			for ( final Entry< Integer, List< Integer > > entry : teamMemberListMap.entrySet() )
				if ( maxTeamSize < entry.getValue().size() )
					maxTeamSize = entry.getValue().size();
			
			// First handle losers
			Integer loserTeam;
			do {
				loserTeam = null;
				for ( final Entry< Integer, List< Integer > > entry : teamMemberListMap.entrySet() )
					if ( entry.getValue().size() < maxTeamSize ) {
						loserTeam = entry.getKey();
						teamMemberListMap.remove( loserTeam );
						replay.details.setTeamIsWinner( loserTeam, Boolean.FALSE );
						break; // We have to break to avoid ConcurrentModificationException
					}
			} while ( loserTeam != null );
			
			// Now handle the optional 1 winner team
			if ( teamMemberListMap.size() == 1 ) { // One team remained: it is the winner team
				final int winnerTeam = teamMemberListMap.keySet().iterator().next();
				teamMemberListMap.remove( winnerTeam );
				replay.details.setTeamIsWinner( winnerTeam, Boolean.TRUE );
			}
		}
	}
	
}
