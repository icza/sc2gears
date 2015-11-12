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

import hu.belicza.andras.mpq.MpqParser;
import hu.belicza.andras.mpq.model.UserData;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils.AbilityCodesRepository;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gearspluginapi.api.enums.ReplayOrigin;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Format;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.Set;

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
		MESSAGE_EVENTS   ( "replay.message.events"   ,  1089231967,   831857289,  1784674979 );
		
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
		MpqParser mpqParser = null;
		try {
			mpqParser = new MpqParser( fileName );
			return parseReplay( fileName, mpqParser, contentToExtractSet );
		} catch ( final Exception e ) {
			System.out.println( "Error parsing replay: " + fileName );
			e.printStackTrace();
			return null;
		} finally {
			if ( mpqParser != null )
				mpqParser.close();
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
			
			return replay;
			
		} catch ( final Exception e ) {
			System.out.println( "Error parsing replay: " + fileName );
			e.printStackTrace();
			return null;
		}
	}
	
}
