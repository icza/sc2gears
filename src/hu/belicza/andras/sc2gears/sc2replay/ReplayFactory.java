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

import static hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.FRAME_BITS_IN_SECOND;
import hu.belicza.andras.mpq.MpqParser;
import hu.belicza.andras.mpq.model.UserData;
import hu.belicza.andras.sc2gears.sc2map.MapParser;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils.AbilityCodesRepository;
import hu.belicza.andras.sc2gears.sc2replay.model.Details;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.BaseUseAbilityAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.SelectAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.TrainAction;
import hu.belicza.andras.sc2gears.sc2replay.model.InitData;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents.Message;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gearspluginapi.api.enums.ReplayOrigin;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.EntityParams;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.AbilityGroup;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BuildingAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Format;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerColor;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Research;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Upgrade;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.importing.PlayerSpecification;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.importing.ReplaySpecification;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * StarCraft 2 replay factory.
 * 
 * <p>Able to parse an SC2Replay MPQ archive replay file.</p>
 * 
 * <p>Also able to construct a replay from a {@link ReplaySpecification}.</p>
 * 
 * @author Andras Belicza
 */
public class ReplayFactory {
	
	/** Version of the replay parser engine. */
	public static final String VERSION = "2.1";
	
	/**
	 * Returns the replay parser engine version.
	 * @return the replay parser engine version
	 */
	public static String getVersion() {
		return VERSION;
	}
	
	/**
	 * Contents of a replay.<br>
	 * INIT_DATA, DETAILS and ATTRIBUTES_EVENTS should always be parsed!
	 */
	public static enum ReplayContent {
		/** Info section.            */
		INIT_DATA        ( "replay.initData"         ,  -750801643,  1518242780,   -14336164 ),
		/** Info section.            */
		DETAILS          ( "replay.details"          ,   620083690,  -746339684,  -281006446 ),
		/** Info section.            */
		ATTRIBUTES_EVENTS( "replay.attributes.events",  1306016990,   497594575, -1563492568 ),
		/** Game events section.     */
		GAME_EVENTS      ( "replay.game.events"      ,   496563520, -1430084277,  -193582187 ),
		/** Message events section.  */
		MESSAGE_EVENTS   ( "replay.message.events"   ,  1089231967,   831857289,  1784674979 ),
		
		// Contents part of map files:
		/** Map info. This is not part of the replay but the map files.       */
		MAP_INFO         ( "MapInfo"                 ,   456326858,  2000504491,  1514959542 ),
		/** Map attributes. This is not part of the replay but the map files. */
		MAP_ATTRIBUTES   ( "DocumentHeader"          ,   967573924,  1586069117,  1498525374 ),
		/** Map objects. This is not part of the replay but the map files.    */
		MAP_OBJECTS      ( "Objects"                 ,   602486196,  -842158972,  1775018687 ),
		/** Minimap.tga, the most common name of the map preview file. This is not part of the replay but the map files. */
		MINIMAP_TGA      ( "Minimap.tga"             , -1658863222, -1379586317,  1671265210 );
		
		/** Name of the file inside the MPQ archive.                        */
		public final String fileName;
		/** MpqHashType.TABLE_OFFSET hash value of the Content's file name. */
		public final int hash1;
		/** MpqHashType.NAME_A hash value of the Content's file name.       */
		public final int hash2;
		/** MpqHashType.NAME_B hash value of the Content's file name.       */
		public final int hash3;
		
		/**
		 * Creates a new Content.
		 * @param fileName name of the file inside the MPQ archive.
		 * @param hash1    MpqHashType.TABLE_OFFSET hash value of the Content's file name
		 * @param hash2    MpqHashType.NAME_A hash value of the Content's file name
		 * @param hash3    MpqHashType.NAME_B hash value of the Content's file name
		 */
		private ReplayContent( final String fileName, final int hash1, final int hash2, final int hash3 ) {
			this.fileName = fileName;
			this.hash1    = hash1;
			this.hash2    = hash2;
			this.hash3    = hash3;
		}
	}
	
	/**
	 * Replay format version compatibility.
	 * 
	 * <p>A version compatibility class consists of replay versions that can be parsed with the same code/algorithms.</p>
	 * 
	 * @author Andras Belicza
	 */
	public static enum VersionCompatibility {
		// The order is important: it must be sorted downwards by the version.
		
		/** Replay format from 2.1. */
		V_2_1  ( new int[] { 2, 1    }, new int[] {          } ),
		/** Replay format from 2.0. */
		V_2_0  ( new int[] { 2, 0    }, new int[] { 2, 0, 11 } ),
		/** Replay format from 1.5.3. */
		V_1_5_3( new int[] { 1, 5, 3 }, new int[] { 1, 5,  4 } ),
		/** Replay format from 1.5.   */
		V_1_5  ( new int[] { 1, 5    }, new int[] { 1, 5,  2 } ),
		/** Replay format from 1.4.   */
		V_1_4  ( new int[] { 1, 4    }, new int[] { 1, 4,  4 } ),
		/** Replay format from 1.3.3.  */
		V_1_3_3( new int[] { 1, 3, 3 }, new int[] { 1, 3,  6 } ),
		/** Replay format from 1.2.   */
		V_1_2  ( new int[] { 1, 2    }, new int[] { 1, 3,  2 } ),
		/** Replay format 1.1.        */
		V_1_1  ( new int[] { 1, 1    }, new int[] { 1, 1     } ),
		/** Replay format from 1.0 up to 1.1 (exclusive).
		 * Also covers the beta (0.19 and newer) formats (except the coordinate formats in the move screen actions). */
		V_1_0  ( new int[] { 0, 19   }, new int[] { 1, 0    } );
		
		/** Min replay version. */
		public final int[] minVersion;
		/** Max replay version. */
		public final int[] maxVersion;
		
		/**
		 * Creates a new VersionCompatibility.
		 * @param minVersion min replay version
		 * @param maxVersion max replay version
		 */
		private VersionCompatibility( final int[] minVersion, final int[] maxVersion ) {
			this.minVersion = minVersion;
			this.maxVersion = maxVersion;
		}
		
		/**
		 * Returns the version compatibility from the build numbers.
		 * @return the version compatibility from the build numbers
		 */
		public static VersionCompatibility fromBuildNumbers( final int[] buildNumbers ) {
			for ( final VersionCompatibility versionCompatibility : values() )
				if ( ReplayUtils.compareVersions( buildNumbers, versionCompatibility.minVersion ) >= 0 && ( versionCompatibility.maxVersion.length == 0 || ReplayUtils.compareVersions( buildNumbers, versionCompatibility.maxVersion ) <= 0 ) )
					return versionCompatibility;
			
			return null;
		}
	}
	
	/** A set of all replay contents.  */
	public static final Set< ReplayContent > ALL_CONTENT          = EnumSet.allOf( ReplayContent.class );
	/** A set of general info content. */
	public static final Set< ReplayContent > GENERAL_INFO_CONTENT = EnumSet.of( ReplayContent.INIT_DATA, ReplayContent.DETAILS, ReplayContent.ATTRIBUTES_EVENTS );
	/** A set of general data content.
	 * This content is sufficient to save the parsed replay to cache. */
	public static final Set< ReplayContent > GENERAL_DATA_CONTENT = EnumSet.of( ReplayContent.INIT_DATA, ReplayContent.DETAILS, ReplayContent.ATTRIBUTES_EVENTS, ReplayContent.GAME_EVENTS, ReplayContent.MESSAGE_EVENTS );
	
	/**
	 * No need to instantiate this class.
	 */
	private ReplayFactory() {
	}
	
	/**
	 * Parses a replay from a file.
	 * 
	 * @param fileName            name of the replay file
	 * @param contentToExtractSet set of content to extract
	 * @return the replay parsed from the file or <code>null</code> if the specified file is not an SC2Replay file or some error occurred 
	 */
	public static Replay parseReplay( final String fileName, final Set< ReplayContent > contentToExtractSet ) {
		try ( final MpqParser mpqParser = new MpqParser( fileName ) ) {
			return parseReplay( fileName, mpqParser, contentToExtractSet );
		} catch ( final Exception e ) {
			System.out.println( "Error parsing replay: " + fileName );
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Parses a replay using the specified MPQ parser.
	 * 
	 * <p>This method does not closes the passed MPQ parser. It's the callers responsibility to close it.</p>
	 * 
	 * @param mpqParser           MPQ parser opened for the content of the replay
	 * @param fileName            name of the replay file
	 * @param contentToExtractSet set of content to extract
	 * @return the replay parsed from the file or <code>null</code> if the specified file is not an SC2Replay file or some error occurred 
	 */
	public static Replay parseReplay( final String fileName, final MpqParser mpqParser, final Set< ReplayContent > contentToExtractSet ) {
		try {
			// Handle user data first
			final UserData userData       = mpqParser.getUserData();
			final Parser   userDataParser = new Parser();
			userDataParser.setWrapper( userData.userData, ByteOrder.BIG_ENDIAN ); // NOTICE THE BIG ENDIAN ORDER!
			final ByteBuffer udWrapper = userDataParser.wrapper; 
			udWrapper.getInt(); // 3c 00 00 00
			udWrapper.getInt(); // 05 (array) 08 (length) 00 (fieldId) 02 (opcode)
			userDataParser.readStringWithLength( userDataParser.readValueStrut() ); // "StarCraft II replay←11"
			udWrapper.getShort(); // 02 (fieldId) 05 (array)
			udWrapper.get(); // 0c (length)
			
			udWrapper.getShort(); // 00 (fieldId) 09 (opcode)
			userDataParser.readValueStrut();
			final int[] buildNumbers = new int[ 4 ];
			// Major, minor, maintenance version and build number parts of the replay version
			for ( int i = 0; i < 4; i++ ) {
				udWrapper.getShort(); // xy (fieldId) 09 (opcode)
				buildNumbers[ i ] = userDataParser.readValueStrut();
			}
			if ( AbilityCodesRepository.getForVersion( buildNumbers ) == null ) {
				System.out.println( "Unsupported replay version: " + ReplayUtils.convertBuildNumbersToString( buildNumbers ) + " (" + fileName + ")!" );
				return null;
			}
			// Replay version is supported
			final Replay replay = new Replay( ReplayOrigin.REPLAY_PARSER );
			replay.setBuildNumbers( buildNumbers );
			
			udWrapper.getShort(); // 0a (fieldId) 09 (opcode)
			userDataParser.readValueStrut(); // Build number again
			udWrapper.getShort(); // 04 (fieldId) 09 (opcode)
			userDataParser.readValueStrut(); // Unknown
			udWrapper.getShort(); // 06 (fieldId) 09 (opcode)
			replay.setGameDuration( userDataParser.readValueStrut() << 2 );
			
			final VersionCompatibility versionCompatibility = VersionCompatibility.fromBuildNumbers( replay.buildNumbers );
			final ReplayParser replayParser;
			switch ( versionCompatibility ) {
			case V_2_1   :
			case V_2_0   : replayParser = new ReplayParserV20 ( fileName, replay, versionCompatibility ); break;
			case V_1_5_3 : 
			case V_1_5   : replayParser = new ReplayParserV15 ( fileName, replay, versionCompatibility ); break;
			case V_1_4   : 
			case V_1_3_3 : 
			case V_1_2   : 
			case V_1_1   : replayParser = new ReplayParserV11 ( fileName, replay, versionCompatibility ); break;
			case V_1_0   : replayParser = new ReplayParserV10 ( fileName, replay, versionCompatibility ); break;
			default      : throw new RuntimeException( "Unhandled version compatibility: " + versionCompatibility );
			}
			
			// Map content from MPQ is parsed in a specific order because some data may rely on others...
			
			// First parse general info data
			if ( contentToExtractSet.contains( ReplayContent.INIT_DATA ) )
				replayParser.parseInitData( mpqParser.getFile( ReplayContent.INIT_DATA.hash1, ReplayContent.INIT_DATA.hash2, ReplayContent.INIT_DATA.hash3 ) );
			if ( contentToExtractSet.contains( ReplayContent.DETAILS ) )
				replayParser.parseDetails( mpqParser.getFile( ReplayContent.DETAILS.hash1, ReplayContent.DETAILS.hash2, ReplayContent.DETAILS.hash3 ) );
			if ( contentToExtractSet.contains( ReplayContent.ATTRIBUTES_EVENTS ) )
				replayParser.parseAttributesEvents( mpqParser.getFile( ReplayContent.ATTRIBUTES_EVENTS.hash1, ReplayContent.ATTRIBUTES_EVENTS.hash2, ReplayContent.ATTRIBUTES_EVENTS.hash3 ) );
			
			if ( replay.details != null )
				for ( final Player player : replay.details.players ) {
					if ( player.race.isConcrete )
						player.finalRace = player.race;
					else
						player.finalRace = Race.fromLocalizedValue( player.raceString );
				}
			
			if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_OVERRIDE_FORMAT_BASED_ON_MATCHUP ) ) {
				final Format guessedFormat = ReplayUtils.guessFormat( replay.details.getRaceMatchup() );
				if ( guessedFormat != Format.UNKNOWN )
					replay.initData.format = guessedFormat;
			}
			
			if ( replay.initData != null )
				replay.converterGameSpeed = Settings.getBoolean( Settings.KEY_SETTINGS_MISC_USE_REAL_TIME_MEASUREMENT ) ? replay.initData.gameSpeed : GameSpeed.NORMAL;			
			
			if ( contentToExtractSet.contains( ReplayContent.GAME_EVENTS ) )
				replayParser.parseGameEvents( mpqParser.getFile( ReplayContent.GAME_EVENTS.hash1, ReplayContent.GAME_EVENTS.hash2, ReplayContent.GAME_EVENTS.hash3 ) );
			if ( contentToExtractSet.contains( ReplayContent.MESSAGE_EVENTS ) )
				replayParser.parseMessageEvents( mpqParser.getFile( ReplayContent.MESSAGE_EVENTS.hash1, ReplayContent.MESSAGE_EVENTS.hash2, ReplayContent.MESSAGE_EVENTS.hash3 ) );
			if ( contentToExtractSet.contains( ReplayContent.MAP_INFO ) )
				MapParser.parseMapInfo( replay );
			if ( contentToExtractSet.contains( ReplayContent.MAP_ATTRIBUTES ) )
				MapParser.parseMapAttributes( replay );
			
			return replay;
			
		} catch ( final Exception e ) {
			System.out.println( "Error parsing replay: " + fileName );
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Constructs a replay from the given replay specification.
	 * 
	 * @param replaySpec replay specification
	 * @return the constructed replay from the given replay specification 
	 */
	public static Replay constructReplay( final ReplaySpecification replaySpec ) {
		final Replay replay = new Replay( ReplayOrigin.REPLAY_SPECIFICATION );
		
		// General replay data
		replay.setBuildNumbers( replaySpec.version );
		final int excludedInitialFrames = replay.excludedInitialFrames = Settings.getInt( Settings.KEY_SETTINGS_MISC_INITIAL_TIME_TO_EXCLUDE_FROM_APM ) << FRAME_BITS_IN_SECOND;
		
		// Init data
		final InitData initData = replay.initData = new InitData();
		initData.gameSpeed   = replaySpec.gameSpeed;
		initData.clientNames = new String[ replaySpec.playerSpecifications.length ];
		for ( int i = 0; i < replaySpec.playerSpecifications.length; i++ )
			initData.clientNames[ i ] = replaySpec.playerSpecifications[ i ].name;
		
		// Details
		final Details details = replay.details = new Details();
		final Player[] players = details.players = new Player[ replaySpec.playerSpecifications.length ];
		for ( int i = 0; i < replaySpec.playerSpecifications.length; i++ ) {
			final PlayerSpecification playerSpec = replaySpec.playerSpecifications[ i ];
			final Player player = new Player();
			player.playerId.name  = playerSpec.name;
			player.team           = playerSpec.team;
			player.type           = PlayerType.HUMAN;
			player.finalRace      = player.race = Race.UNKNOWN;
			player.raceString     = player.finalRace.stringValue;
			player.playerColor    = PlayerColor.values()[ i ];
			player.argbColor[ 0 ] = player.playerColor.color.getAlpha();
			player.argbColor[ 1 ] = player.playerColor.color.getRed();
			player.argbColor[ 2 ] = player.playerColor.color.getGreen();
			player.argbColor[ 3 ] = player.playerColor.color.getBlue();
			players[ i ] = player;
		}
		details.mapName = details.originalMapName = "";
		details.saveTime = System.currentTimeMillis();
		
		replay.converterGameSpeed = Settings.getBoolean( Settings.KEY_SETTINGS_MISC_USE_REAL_TIME_MEASUREMENT ) ? replay.initData.gameSpeed : GameSpeed.NORMAL;
		
		// Game events
		final AbilityCodes abilityCodes = AbilityCodesRepository.getForVersion( replay.buildNumbers );
		
		// A map to easily/fast find the descriptor of a common ability by name
		final Map< String, Entry< Integer, Object[] > > commonAbilityNameEntryMap = new HashMap< String, Entry< Integer,Object[] > >();
		for ( final Entry< Integer, Object[] > entry : abilityCodes.COMMON_BASE_ABILITY_CODES.entrySet() )
			commonAbilityNameEntryMap.put( (String) entry.getValue()[ 0 ], entry );
		
		final Map< String, Short > unitNameTypeMap = GeneralUtils.reverseMap( abilityCodes.UNIT_TYPE_NAME );
		
		final GameEvents gameEvents = replay.gameEvents = new GameEvents( replay, abilityCodes );
		// Parse BOs, create actions
		int gameDurationFrames = 0; // To keep track of when the actions take place in time 
		final List< Action > actionList = new ArrayList< Action >();
		for ( int i = 0; i < replaySpec.playerSpecifications.length; i++ ) {
			final PlayerSpecification playerSpec = replaySpec.playerSpecifications[ i ];
			final Player              player     = players[ i ];
			int frame = 0, actionEndFrame = 0;
			try ( final LineNumberReader boReader = new LineNumberReader( new StringReader( playerSpec.buildOrderText ) ) ) {
				String line;
				while ( ( line = boReader.readLine() ) != null ) {
					if ( line.length() > 0 ) {
						Action action = null;
						try {
							final StringTokenizer tokenizer = new StringTokenizer( line, "," );
							
							if ( !tokenizer.hasMoreTokens() )
								continue; // Empty line
							
							actionEndFrame = frame = Integer.parseInt( tokenizer.nextToken() );
							
							byte opCode = ReplayParser.OP_CODE_USE_ABILITY; // Most of the actions are using an ability; if not, opCode will be re-assigned
							
							final String token = tokenizer.nextToken();
							if ( token.startsWith( "U." ) ) {
								final Unit unit = Unit.valueOf( token.substring( 2 ) );
								action = gameEvents.new TrainAction( unit );
							} else if ( token.startsWith( "B." ) ) {
								final Building building = Building.valueOf( token.substring( 2 ) );
								action = gameEvents.new BuildAction( building );
							} else if ( token.startsWith( "UA." ) ) {
								final UnitAbility unitAbility = UnitAbility.valueOf( token.substring( 3 ) );
								final Unit unit = Unit.valueOf( tokenizer.nextToken().substring( 2 ) );
								action = gameEvents.new UseUnitAbilityAction( unitAbility, unit );
							} else if ( token.startsWith( "BA." ) ) {
								final BuildingAbility buildingAbility = BuildingAbility.valueOf( token.substring( 3 ) );
								final Building building = Building.valueOf( tokenizer.nextToken().substring( 2 ) );
								action = gameEvents.new UseBuildingAbilityAction( buildingAbility, building );
							} else if ( token.startsWith( "R." ) ) {
								final Research research = Research.valueOf( token.substring( 2 ) );
								action = gameEvents.new ResearchAction( research );
							} else if ( token.startsWith( "UP." ) ) {
								final Upgrade upgrade = Upgrade.valueOf( token.substring( 3 ) );
								action = gameEvents.new UpgradeAction( upgrade );
							} else if ( token.startsWith( "HU." ) ) {
								final Unit unit = Unit.valueOf( token.substring( 3 ) );
								action = gameEvents.new TrainHallucinatedAction( unit );
							} else if ( token.startsWith( "WU." ) ) {
								final Unit unit = Unit.valueOf( token.substring( 3 ) );
								action = gameEvents.new WarpAction( unit );
							} else if ( token.equals( "SELECT" ) ) {
								// Select action with Deselect all implied
								// Example: "345,SELECT,3*SCV[1;2;3],2*Marines,Marauder[6]"
								final List< Short   > unitTypeList         = new ArrayList< Short   >( 4 );
								final List< Short   > unitsOfTypeCountList = new ArrayList< Short   >( 4 );
								final List< Integer > unitIdList           = new ArrayList< Integer >( 4 );
								while ( tokenizer.hasMoreTokens() ) {
									final String unitDesc = tokenizer.nextToken();
									final int starIndex = unitDesc.indexOf( '*' );
									final short unitsCount;
									String unitName;
									if ( starIndex < 0 ) {
										unitsCount = 1;
										unitName = unitDesc;
									}
									else {
										unitsCount = Short.parseShort( unitDesc.substring( 0, starIndex ) );
										unitName = unitDesc.substring( starIndex + 1 );
									}
									final int squareBracketIndex = unitName.indexOf( '[' );
									final StringTokenizer unitIdsTokenizer;
									if ( squareBracketIndex >= 0 ) {
										unitIdsTokenizer = new StringTokenizer( unitName.substring( squareBracketIndex + 1, unitName.length() - 1 ), ";" );
										unitName = unitName.substring( 0, squareBracketIndex );
									}
									else {
										unitIdsTokenizer = null;
									}
									final Short unitType = unitNameTypeMap.get( unitName );
									unitTypeList.add( unitType == null ? 0 : unitType );
									unitsOfTypeCountList.add( unitsCount );
									for ( int j = 0; j < unitsCount; j++ )
										if ( unitIdsTokenizer == null )
											unitIdList.add( 0 );
										else
											unitIdList.add( Integer.parseInt( unitIdsTokenizer.nextToken() ) );
								}
								final SelectAction sa;
								action = sa = gameEvents.new SelectAction();
								sa.deselectionBitsCount = -1; // Deselect all
								sa.unitTypes            = GeneralUtils.toShortArray( unitTypeList         );
								sa.unitsOfTypeCounts    = GeneralUtils.toShortArray( unitsOfTypeCountList );
								sa.unitIds              = GeneralUtils.toIntArray  ( unitIdList           );
								opCode = ReplayParser.OP_CODE_SELECT_DESELECT;
							} else if ( token.startsWith( "\"" ) ) {
								final String commonBaseAbilityName = token.substring( 1, token.length() - 1 );
								final Entry< Integer, Object[] > commonAbilityEntry = commonAbilityNameEntryMap.get( commonBaseAbilityName );
								if ( commonAbilityEntry != null )
									action = gameEvents.new BaseUseAbilityAction( (String) commonAbilityEntry.getValue()[ 0 ], (AbilityGroup) commonAbilityEntry.getValue()[ 1 ], (Boolean) commonAbilityEntry.getValue()[ 2 ] );
								else if ( "Right click".equals( commonBaseAbilityName ) ) // Right click does not have a constant ability code in all versions...
									action = gameEvents.new BaseUseAbilityAction( "Right click", null, Boolean.FALSE );
							}
							
							if ( action != null ) {
								action.opCode = opCode;
								if ( action instanceof BaseUseAbilityAction ) {
									final BaseUseAbilityAction bua = (BaseUseAbilityAction) action;
									if ( tokenizer.hasMoreTokens() ) {
										final String targetIndicatorToken = tokenizer.nextToken();
										if ( "TU".equals( targetIndicatorToken ) ) {
											// Unit target
											String unitName = tokenizer.nextToken();
											final int squareBracketIndex = unitName.indexOf( '[' );
											if ( squareBracketIndex >= 0 ) {
												// Unit id:
												bua.targetId = Integer.parseInt( unitName.substring( squareBracketIndex + 1, unitName.length() - 1 ) );
												unitName = unitName.substring( 0, squareBracketIndex );
											}
											final Short unitType = unitNameTypeMap.get( unitName );
											// Unit type:
											bua.targetType = unitType == null ? 0 : unitType;
										} if ( "TL".equals( targetIndicatorToken ) ) {
											// Location target
											bua.targetX = (int) ( Float.parseFloat( tokenizer.nextToken() ) * 65536 );
											bua.targetY = (int) ( Float.parseFloat( tokenizer.nextToken() ) * 65536 );
										}
									}
									// else No target specified: do nothing
									
									final EntityParams entityParams = bua.getEntityParams();
									if ( entityParams != null )
										actionEndFrame += entityParams.time << FRAME_BITS_IN_SECOND;
								}
							}
							
						} catch ( final Exception e ) {
							// Parse error in line, don't stop, we will continue with the next line
						}
						
						if ( action == null ) {
							action = gameEvents.new CustomAction( "<Parse error in line: " + boReader.getLineNumber() + ">" );
							action.opCode = 0;
							gameEvents.errorParsing = true;
						}
						
						action.player = (byte) i;
						action.frame  = frame;
						if ( action.type != ActionType.INACTION ) {
							player.lastActionFrame = frame;
							player.actionsCount++;
							if ( frame < excludedInitialFrames )
								player.excludedActionsCount++;
						}
						actionList.add( action );
						
						if ( gameDurationFrames < actionEndFrame )
							gameDurationFrames = actionEndFrame;
					}
				}
			} catch ( final IOException ie ) {
				// Never to happen because we read from a string...
				ie.printStackTrace();
			}
		}
		final Action[] actions = gameEvents.actions = actionList.toArray( new Action[ actionList.size() ] );
		Arrays.sort( gameEvents.actions, ReplayUtils.ACTION_FRAME_COMPARATOR );
		// Try to find out the races based on the first train actions
		final int actionsLength = actions.length;
		Action action;
		for ( int i = 0; i < players.length; i++ ) {
			for ( int j = 0; j < actionsLength; j++ )
				if ( ( action = actions[ j ] ).player == i && action instanceof TrainAction ) {
					players[ i ].finalRace  = players[ i ].race = ( (TrainAction) action ).unit.raceOfUnit();
					players[ i ].raceString = players[ i ].finalRace.stringValue;
					break;
				}
		}
		
		replay.setGameDuration( gameDurationFrames + ( 10 << FRAME_BITS_IN_SECOND ) ); // +10 extra seconds
		
		// Message events
		final MessageEvents messageEvents = replay.messageEvents = new MessageEvents();
		messageEvents.messages = new Message[ 0 ];
		
		return replay;
	}
	
}
