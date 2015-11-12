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

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.EnumCache;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IBaseUseAbilityAction;

import sjava.awt.Color;
import java.util.EnumSet;
import java.util.Set;

/*
 * Unit tiers:
 * http://gaming.stackexchange.com/questions/3890/what-do-the-unit-tiers-mean-in-starcraft-2
 * http://www.teamliquid.net/forum/viewmessage.php?topic_id=150739&currentpage=5#82
 */

/**
 * General constants for StarCraft II replays.
 * 
 * <p>This class contains many powerful enumerations which usually have a <code>stingValue</code> public attribute which contains the
 * name of the object in the current language of Sc2gears.<br>
 * Many listed enumeration also provides utility methods and attributes associated with them, to mention a few examples:
 * <ul>
 * 		<li>{@link GameSpeed#convertToRealTime(int)}
 * 		<li>{@link Race#fromLocalizedValue(String)}
 * 		<li>{@link PlayerColor#color}
 * 		<li>{@link Unit#unitTier}
 * 		<li>{@link Unit#raceOfUnit()}
 * 		<li>{@link Building#raceOfBuilding()}
 * </ul></p>
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 */
public class ReplayConsts {
	
	/**
	 * No need to instantiate this class.
	 */
	private ReplayConsts() {
	}
	
	/** Max valid players in a replay.                          */
	public static final byte  MAX_PLAYERS                    = 0x0f;
	
	/** Number of bits for frames in 1 second.                  */
	public static final int   FRAME_BITS_IN_SECOND           = 6;
	/** Number of frames in 1 second.                           */
	public static final int   FRAMES_IN_SECOND               = 1 << FRAME_BITS_IN_SECOND;
	/** Energy regeneration rate (per second).                  */
	public static final float ENERGY_REGENERATION_RATE       = 0.5625f;
	/** Energy regeneration rate (per frame).                   */
	public static final float ENERGY_REGENERATION_FRAME_RATE = 0.5625f / FRAMES_IN_SECOND;
	/** Duration of a larva spawning on a Hatchery (in frames). */
	public static final int   LARVA_SPAWNING_DURATION        = 40 << FRAME_BITS_IN_SECOND; // 40 seconds...
	/** Duration of a Chrono Boost (in frames).                 */
	public static final int   CHRONO_BOOST_DURATION          = 20 << FRAME_BITS_IN_SECOND; // 20 seconds...
	
	
	/**
	 * Expansion level.
	 * @author Andras Belicza
	 * @see IReplay
	 */
	public static enum ExpansionLevel {
		// Order is important, this is the order we check them...
		// TODO icon
		/** Heart of the Swarm. */
		HOTS   ( "66093832128453efffbb787c80b7d3eec1ad81bde55c83c930dea79c4e505a04.s2ma", "sc2.expansion.hots", "sc2.expansion.hots.full" ), // "Standard Data: Swarm.SC2Mod"
		/** Wings of Liberty.   */
		WOL    ( "421c8aa0f3619b652d23a2735dfee812ab644228235e7a797edecfe8b67da30e.s2ma", "sc2.expansion.wol" , "sc2.expansion.wol.full"  ), // "Standard Data: Liberty.SC2Mod"
		/** Unknown.     */
		UNKNOWN( "", "general.unknown", "general.unknown" );
		
		/** Required dependency for the expansion level. */
		public final String dependency;
		/** Cache of the string value.                   */
		public final String stringValue;
		/** Cache of the full string value.              */
		public final String fullStringValue;
		
		/**
		 * Creates a new ExpansionLevel.
		 * @param dependency  required dependency for the expansion level
		 * @param textKey     key of the text representation
		 * @param fullTextKey key of the full text representation
		 */
		private ExpansionLevel( final String dependency, final String textKey, final String fullTextKey  ) {
			this.dependency = dependency;
			stringValue     = Language.getText( textKey );
			fullStringValue = Language.getText( fullTextKey );
		}
		
		/**
		 * Returns the expansion level specified by its dependency.
		 * @param dependency dependency value of the expansion level
		 * @return the expansion level specified by its dependency; or <code>UNKNOWN</code> if the dependency is invalid
		 */
		public static ExpansionLevel fromDependency( final String dependency ) {
			for ( final ExpansionLevel expansion : values() )
				if ( expansion.dependency.equals( dependency ) )
					return expansion;
			
			return UNKNOWN;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Game speed.
	 * @author Andras Belicza
	 * @see ReplaySpecification
	 * @see IReplay
	 */
	public static enum GameSpeed {
		/** Slower.  */
		SLOWER ( "rolS", "sc2.gameSpeed.slower", 60  ),
		/** Slow.    */
		SLOW   ( "wolS", "sc2.gameSpeed.slow"  , 45  ),
		/** Normal.  */
		NORMAL ( "mroN", "sc2.gameSpeed.normal", 36  ),
		/** Fast.    */
		FAST   ( "tsaF", "sc2.gameSpeed.fast"  , 30  ),
		/** Faster.  */
		FASTER ( "rsaF", "sc2.gameSpeed.faster", 26  ),
		/** Unknown. */
		UNKNOWN( ""    , "general.unknown"     , 36  );
		
		/** The binary value (stored in the replay file) of the game speed. */
		private final String binaryValue;
		/** Cache of the string value.                                      */
		public  final String stringValue;
		/** The relative time speed value (relative to other game speeds).  */
		private final int    relativeSpeed;
		
		/**
		 * Creates a new GameType.
		 * @param binaryValue   the binary value of the game type (stored in the replay file)
		 * @param textKey       key of the text representation
		 * @param relativeSpeed the relative time speed value
		 */
		private GameSpeed( final String binaryValue, final String textKey, final int relativeSpeed ) {
			this.binaryValue   = binaryValue;
			stringValue        = Language.getText( textKey );
			this.relativeSpeed = relativeSpeed;
		}
		
		/**
		 * Returns the game speed specified by its binary value.
		 * @param binaryValue the binary value of the game speed (stored in the replay)
		 * @return the game speed specified by its binary value; or <code>UNKNOWN</code> if the binary value is invalid
		 */
		public static GameSpeed fromBinaryValue( final String binaryValue ) {
			for ( final GameSpeed gameSpeed : values() )
				if ( gameSpeed.binaryValue.equals( binaryValue ) )
					return gameSpeed;
			
			// Looks replay is a version of 0.11 .. 0.14, game speed is a number: 0 .. 4
			final int ordinal = binaryValue.charAt( 0 ) - '0';
			if ( ordinal >= 0 && ordinal <= 4 )
				return values()[ ordinal ];
			
			return UNKNOWN;
		}
		
		/**
		 * Converts a game-time value to real-time value.
		 * @param gameTime game-time value to be converted
		 * @return the game-time converted to real-time
		 */
		public int convertToRealTime( final int gameTime ) {
			// Game-time is deprecated, but NORMAL is used many times in Multi-rep analysis (pre-converted time values)
			// So we perform a check for NORMAL (when no conversion needed)
			return this == NORMAL ? gameTime : gameTime * relativeSpeed / NORMAL.relativeSpeed;
		}
		
		/**
		 * Converts a real-time value to game-time value.
		 * @param realTime real-time value to be converted
		 * @return the real-time converted to game-time
		 */
		public int convertToGameTime( final int realTime ) {
			// Game-time is deprecated, but NORMAL is used many times in Multi-rep analysis (pre-converted time values)
			// So we perform a check for NORMAL (when no conversion needed)
			return this == NORMAL ? realTime : realTime * NORMAL.relativeSpeed / relativeSpeed;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Game type.
	 * @author Andras Belicza
	 * @see IReplay
	 */
	public static enum GameType {
		/** Private.                                       */
		PRIVATE      ( "virP", "sc2.gameType.private"      ),
		/** Public.                                        */
		PUBLIC       ( "buP" , "sc2.gameType.public"       ),
		/** Automatic/Anonymous Matchmaking (Ladder game). */
		AMM          ( "mmA" , "sc2.gameType.amm"          ),
		/** Single player.                                 */
		SINGLE_PLAYER( ""    , "sc2.gameType.singlePlayer" ),
		/** Unknown.                                       */
		UNKNOWN      ( ""    , "general.unknown"           );
		
		/** The binary value (stored in the replay file) of the game type. */
		private final String binaryValue;
		/** Cache of the string value. */
		public  final String stringValue;
		
		/**
		 * Creates a new GameType.
		 * @param binaryValue the binary value of the game type (stored in the replay file)
		 * @param textKey     key of the text representation
		 */
		private GameType( final String binaryValue, final String textKey ) {
			this.binaryValue = binaryValue;
			stringValue      = Language.getText( textKey );
		}
		
		/**
		 * Returns the game type specified by its binary value.
		 * @param binaryValue the binary value of the game type (stored in the replay)
		 * @return the game type specified by its binary value; or <code>UNKNOWN</code> if the binary value is invalid
		 */
		public static GameType fromBinaryValue( final String binaryValue ) {
			for ( final GameType gameType : values() )
				if ( gameType.binaryValue.equals( binaryValue ) )
					return gameType;
			return UNKNOWN;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Match result.
	 * @author Andras Belicza
	 */
	public static enum MatchResult {
		/** Win.     */
		WIN     ( "sc2.matchResult.win" , "sc2.matchResult.win.letter"     ),
		/** Win.     */
		LOSS    ( "sc2.matchResult.loss", "sc2.matchResult.loss.letter"    ),
		/** Unknown. */
		UNKNOWN ( "general.unknown"     , "sc2.matchResult.unknown.letter" );
		
		/** Cache of the string value.                                                             */
		public final String stringValue;
		/** Letter of the match result. It's the first character of the string value, upper-cased. */
		public final char   letter;
		
		/**
		 * Creates a new GameType.
		 * @param textKey       key of the text representation
		 * @param letterTextKey key of the letter of the match result
		 */
		private MatchResult( final String textKey, final String letterTextKey ) {
			stringValue = Language.getText( textKey );
			letter      = Character.toUpperCase( Language.getText( letterTextKey ).charAt( 0 ) );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
		
		/**
		 * Returns a description string enumerating the letters and the texts.
		 * @return a description string enumerating the letters and the texts
		 */
		public static String getDescription() {
			final StringBuilder descriptionBuilder = new StringBuilder();
			for ( final MatchResult matchResult : MatchResult.values() ) {
				if ( descriptionBuilder.length() > 0 )
					descriptionBuilder.append( ", " );
				descriptionBuilder.append( matchResult.letter ).append( " = " ).append( matchResult.stringValue );
			}
			return descriptionBuilder.toString();
		}
	}
	
	/**
	 * Battle.net language available on the web.
	 * @author Andras Belicza
	 * @see IPlayerId
	 */
	public static enum BnetLanguage {
		// TODO add SPANISH_ES (with flag) for Europe (instead of SPANISH_MX)
		
		/** English.    */
		ENGLISH            ( "general.language.english"     , "en" ),
		/** Chinese.    */
		CHINESE_TRADITIONAL( "general.language.chineseTw"   , "zh" ),
		/** French.     */
		FRENCH             ( "general.language.french"      , "fr" ),
		/** German.     */
		GERMAN             ( "general.language.german"      , "de" ),
		/** Italian.    */
		ITALIAN            ( "general.language.italian"     , "it" ),
		/** Korean.     */
		KOREAN             ( "general.language.korean"      , "ko" ),
		/** Polish.     */
		POLISH             ( "general.language.polish"      , "pl" ),
		/** Portuguese. */
		PORTUGUESE_BR      ( "general.language.portugueseBr", "pt" ),
		/** Russian.    */
		RUSSIAN            ( "general.language.russian"     , "ru" ),
		/** Spanish.    */
		SPANISH_MX         ( "general.language.spanishMx"   , "es" );
		
		/** Text key of the language.                 */
		public final String textKey;
		/** Cache of the string value.                */
		public final String stringValue;
		/** Language code how it appears in the URLs. */
		public final String languageCode;
		
		/**
		 * Creates a new BnetLanguage
		 * @param textKey      key of the text representation
		 * @param languageCode 
		 */
		private BnetLanguage( final String textKey, final String languageCode ) {
			this.textKey      = textKey;
			stringValue       = Language.getText( textKey );
			this.languageCode = languageCode;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Gateway.
	 * @author Andras Belicza
	 * @see IPlayerId
	 * @see IReplay
	 */
	public static enum Gateway {
		// Season start reference: Tuesday 0 AM, PDT => Monday 16, GMT
		/** America.        */  // Season start: Tuesday 5 AM, PDT = Tuesday 12 PM, GMT
		AMERICA( "US", "sc2.gateway.america", 12L*60*60*1000, "http://usb.depot.battle.net:1119/", "http://us.battle.net/",
				BnetLanguage.ENGLISH, EnumSet.of( BnetLanguage.ENGLISH, BnetLanguage.SPANISH_MX, BnetLanguage.PORTUGUESE_BR ) ),
		/** Asia.           */  // Season start: Thursday 5 AM, local time = Wednesday 8 PM, GMT
		ASIA( "KR", "sc2.gateway.asia", 44L*60*60*1000, "http://krb.depot.battle.net:1119/", "http://kr.battle.net/",
				BnetLanguage.KOREAN, EnumSet.of( BnetLanguage.KOREAN, BnetLanguage.CHINESE_TRADITIONAL ) ),
		/** China.          */  // Season start: Wednesday 5 AM local time = Tuesday 9 PM, GMT
		CHINA( "CN", "sc2.gateway.china", 21L*60*60*1000, "http://cnb.depot.battle.net:1119/", "http://www.battlenet.com.cn/",
				BnetLanguage.CHINESE_TRADITIONAL, EnumSet.of( BnetLanguage.CHINESE_TRADITIONAL ) ),
		/** Europe.         */  // Season start: Wednesday 5 AM, CEST = Wednesday 4 AM, GMT
		EUROPE( "EU", "sc2.gateway.europe", 28L*60*60*1000, "http://eub.depot.battle.net:1119/", "http://eu.battle.net/",
				BnetLanguage.ENGLISH, EnumSet.of( BnetLanguage.ENGLISH, BnetLanguage.GERMAN, BnetLanguage.FRENCH, BnetLanguage.SPANISH_MX, BnetLanguage.RUSSIAN, BnetLanguage.ITALIAN, BnetLanguage.POLISH ) ),
		/** Southeast Asia. */  // Season start: Tuesday 5 AM, local time = Monday 9 PM, GMT
		SOUTHEAST_ASIA( "SG", "sc2.gateway.southeastAsia", -3L*60*60*1000, "http://sg.depot.battle.net:1119/", "http://sea.battle.net/",
				BnetLanguage.ENGLISH, EnumSet.of( BnetLanguage.ENGLISH ) ),
		/** Public test.    */
		PUBLIC_TEST( "XX", "sc2.gateway.publicTest", 12L*60*60*1000, "http://xx.depot.battle.net:1119/", "http://us.battle.net/",
				BnetLanguage.ENGLISH, EnumSet.of( BnetLanguage.ENGLISH ) ),
		/** Unknown.        */
		UNKNOWN( "", "general.unknown", 0L, "" , "" , null, EnumSet.noneOf( BnetLanguage.class ) );
		
		/** The binary value (stored in the replay file) of the gateway. */
		public final String              binaryValue;
		/** Cache of the string value.                                   */
		public final String              stringValue;
		/** Season start time offset in milliseconds compared to the reference date specified in the LadderSeason.startDate(). */
		public final long                seasonStartTimeOffset;
		/** URL of the gateway's depot server.                           */
		public final String              depotServerUrl;
		/** URL of the gateway's battle.net.                             */
		public final String              bnetUrl;
		/** Default language of the gateway's battle.net.                */
		public final BnetLanguage        defaultLanguage;
		/** Set of available languages on this gateway's battle.net.     */
		public final Set< BnetLanguage > availableLanguageSet;
		
		/**
		 * Creates a new Gateway.
		 * @param binaryValue                  the binary value of the gateway (stored in the replay file)
		 * @param textKey                      key of the text representation
		 * @param seasonStartTimeOffset        season start time offset in milliseconds compared to the reference date specified in the {@link LadderSeason#startDate}
		 * @param depotServerUrl               URL of the gateway's depot server
		 * @param battleNetUrl                 default language code of the gateway's battle.net
		 * @param battleNetDefaultLanguageCode URL of the gateway's battle.net
		 */
		private Gateway( final String binaryValue, final String textKey, final long seasonStartTimeOffset, final String depotServerUrl, final String battleNetUrl, final BnetLanguage defaultLanguage, final Set< BnetLanguage > availableLanguageSet ) {
			this.binaryValue           = binaryValue;
			stringValue                = Language.getText( textKey );
			this.seasonStartTimeOffset = seasonStartTimeOffset;
			this.depotServerUrl        = depotServerUrl;
			this.bnetUrl               = battleNetUrl;
			this.defaultLanguage       = defaultLanguage;
			this.availableLanguageSet  = availableLanguageSet;
		}
		
		/**
		 * Returns the gateway specified by its binary value.
		 * @param binaryValue the binary value of the gateway (stored in the replay)
		 * @return the gateway specified by its binary value; or <code>UNKNOWN</code> if the binary value is invalid
		 */
		public static Gateway fromBinaryValue( final String binaryValue ) {
			for ( final Gateway gateway : values() )
				if ( gateway.binaryValue.equals( binaryValue ) )
					return gateway;
			return UNKNOWN;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * StarCraft II region.
	 * @author Andras Belicza
	 * @see PlayerInfo
	 * @see IPlayerId
	 */
	public static enum Region {
		/** North America.  */
		NORTH_AMERICA ( "sc2.region.northAmerica" , "us"  ),
		/** Latin America.  */
		LATIN_AMERICA ( "sc2.region.latinAmerica" , "la"  ),
		/** China.          */
		CHINA         ( "sc2.region.china"        , "cn"  ),
		/** Europe.         */
		EUROPE        ( "sc2.region.europe"       , "eu"  ),
		/** Russia.         */
		RUSSIA        ( "sc2.region.russia"       , "ru"  ),
		/** Korea.          */
		KOREA         ( "sc2.region.korea"        , "kr"  ),
		/** Taiwan.         */
		TAIWAN        ( "sc2.region.taiwan"       , "tw"  ),
		/** Southeast Asia. */
		SOUTHEAST_ASIA( "sc2.region.southeastAsia", "sea" ),
		/** Unknown.        */
		UNKNOWN       ( "general.unknown"         , ""    );
		
		/** Cache of the string value.     */
		public final String stringValue;
		/** Sc2ranks.com id of the region. */
		public final String sc2ranksId;
		
		/**
		 * Creates a new region.
		 * @param sc2ranksId sc2ranks.com id of the region
		 */
		private Region( final String textKey, final String sc2ranksId ) {
			stringValue     = Language.getText( textKey );
			this.sc2ranksId = sc2ranksId;
		}
		
		/**
		 * Returns the region specified by the gateway and the battle.net sub id.
		 * @param gateway        gateway
		 * @param battleNetSubid battle.net sub id
		 * @return the region specified by the gateway and the battle.net sub id
		 */
		public static Region getFromGatewayAndSubId( final Gateway gateway, final int battleNetSubid ) {
			switch ( gateway ) {
			case AMERICA        : return battleNetSubid == 2 ? LATIN_AMERICA : NORTH_AMERICA;
			case CHINA          : return CHINA;
			case EUROPE         : return battleNetSubid == 2 ? RUSSIA : EUROPE;
			case ASIA           : return battleNetSubid == 2 ? TAIWAN : KOREA;
			case SOUTHEAST_ASIA : return SOUTHEAST_ASIA;
			default             : return UNKNOWN;
			}
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Player type.
	 * @author Andras Belicza
	 * @see PlayerInfo
	 * @see IPlayerId
	 */
	public static enum PlayerType {
		/** Human.                   */
		HUMAN   ( "nmuH", "sc2.playerType.human"    ),
		/** Computer.                */
		COMPUTER( "pmoC", "sc2.playerType.computer" ),
		/** Represents an open slot. */
		OPEN    ( "nepO", ""                        ),
		/** Unknown.                 */
		UNKNOWN ( ""    , "general.unknown"         );
		
		/** The binary value (stored in the replay file) of the player type. */
		private final String binaryValue;
		/** Cache of the string value. */
		public  final String stringValue;
		
		/**
		 * Creates a new PlayerType.
		 * @param binaryValue the binary value (stored in the replay file) of the player type
		 * @param textKey     key of the text representation
		 */
		private PlayerType( final String binaryValue, final String textKey ) {
			this.binaryValue = binaryValue;
			stringValue      = Language.getText( textKey );
		}
		
		/**
		 * Returns the player type specified by its binary value.
		 * @param binaryValue the binary value of the gateway (stored in the replay)
		 * @return the player type specified by its binary value; or <code>UNKNOWN</code> if the binary value is invalid
		 */
		public static PlayerType fromBinaryValue( final String binaryValue ) {
			for ( final PlayerType playerType : values() )
				if ( playerType.binaryValue.equals( binaryValue ) )
					return playerType;
			return UNKNOWN;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Race.
	 * @author Andras Belicza
	 * @see IPlayer
	 */
	public static enum Race {
		/** Protoss.  */
		PROTOSS( "protoss", "torP", "sc2.race.protoss", "sc2.race.protoss.letter", true , GeneralUtils.assembleHashSet( "Protoss", "프로토스", "神族", "Протосс", "Protosi", "星灵" ) ), // English, Korean, Chinese, Russian, Polish, Mandarin (Chinese)
		/** Terran.   */
		TERRAN ( "terran" , "rreT", "sc2.race.terran" , "sc2.race.terran.letter" , true , GeneralUtils.assembleHashSet( "Terran", "Terraner", "Terrano", "테란", "人類", "Терран", "Terrani", "人类" ) ), // English, German, Portuguese, Korean, Chinese, Russian, Polish, Mandarin (Chinese)
		/** Zerg.     */
		ZERG   ( "zerg"   , "greZ", "sc2.race.zerg"   , "sc2.race.zerg.letter"   , true , GeneralUtils.assembleHashSet( "Zerg", "저그", "蟲族", "Зерг", "Zergi", "异虫" ) ), // English, Korean, Chinese, Russian, Polish, Mandarin (Chinese)
		/** Random    */
		RANDOM ( "random" , "DNAR", "sc2.race.random" , "sc2.race.random.letter" , false, null ),
		/** Any race. */
		ANY    ( ""       , "*"   , null              , "sc2.race.any.letter"    , false, null ),
		/** Unknown.  */
		UNKNOWN( ""       , ""    , "general.unknown" , "sc2.race.unknown.letter", false, null );
		
		/** String used by Battle.net.                                              */
		public  final String        bnetString;
		/** The binary value (stored in the replay file) of the race.               */
		private final String        binaryValue;
		/** Cache of the string value.                                              */
		public  final String        stringValue;
		/** Race letter. It's the first character of the string value, upper-cased. */
		public  final char          letter;
		/** Tells if the race is a concrete, known race.                            */
		public  final boolean       isConcrete;
		/** Localized values of the race name.                                      */
		private final Set< String > localizedValueSet;
		
		/**
		 * Creates a new PlayerType.
		 * @param bnetString  string used by Battle.net
		 * @param binaryValue the binary value (stored in the replay file) of the player type
		 * @param textKey           key of the text representation
		 * @param letterTextKey     race letter; it's the first character of the string value, upper-cased
		 * @param isConcrete        tells if the race is a concrete, known race
		 * @param localizedValueSet localized values of the race name
		 */
		private Race( final String bnetString, final String binaryValue, final String textKey, final String letterTextKey, final boolean isConcrete, final Set< String > localizedValueSet ) {
			this.bnetString        = bnetString;
			this.binaryValue       = binaryValue;
			stringValue            = textKey == null ? null : Language.getText( textKey );
			letter                 = Character.toUpperCase( Language.getText( letterTextKey ).charAt( 0 ) );
			this.isConcrete        = isConcrete;
			this.localizedValueSet = localizedValueSet;
		}
		
		/**
		 * Returns the race specified by its letter.
		 * @param letter letter of the race
		 * @return the race specified by its letter; or <code>UNKNOWN</code> if the letter is invalid
		 */
		public static Race fromLetter( final char letter ) {
			for ( final Race race : EnumCache.RACES )
				if ( race.letter == letter )
					return race;
			return UNKNOWN;
		}
		
		/**
		 * Returns the race specified by its binary value.
		 * @param binaryValue the binary value of the gateway (stored in the replay)
		 * @return the race specified by its binary value; or <code>UNKNOWN</code> if the binary value is invalid
		 */
		public static Race fromBinaryValue( final String binaryValue ) {
			for ( final Race race : EnumCache.RACES )
				if ( race.binaryValue.equals( binaryValue ) )
					return race;
			return UNKNOWN;
		}
		
		/**
		 * Returns the race specified by its localized value.
		 * @param localizedValue the localized value of the race
		 * @return the race specified by its localized value; or <code>UNKNOWN</code> if the localized value was not recognized
		 */
		public static Race fromLocalizedValue( final String localizedValue ) {
			// Protoss and Zerg are the most common name, so first try those; "Zerg" is shorter, so start with that
			if ( ZERG.localizedValueSet.contains( localizedValue ) )
				return ZERG;
			if ( PROTOSS.localizedValueSet.contains( localizedValue ) )
				return PROTOSS;
			if ( TERRAN.localizedValueSet.contains( localizedValue ) )
				return TERRAN;
			
			// Could not find the localized value, let's try to find out
			if ( localizedValue.startsWith( "Pr" ) )
				return PROTOSS;
			else if ( localizedValue.startsWith( "Te" ) )
				return TERRAN;
			else if ( localizedValue.startsWith( "Ze" ) )
				return ZERG;
			
			return UNKNOWN;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Computer (AI) player difficulty (difficulty level of computers).
	 * @author Andras Belicza
	 * @see IReplay
	 */
	public static enum Difficulty {
		/** Very easy. */
		VERY_EASY( "yEyV", "sc2.difficulty.veryEasy" ),
		/** Easy.      */
		EASY     ( "ysaE", "sc2.difficulty.easy"     ),
		/** Medium.    */
		MEDIUM   ( "ideM", "sc2.difficulty.medium"   ),
		/** Hard.      */
		HARD     ( "draH", "sc2.difficulty.hard"     ),
		/** Very hard. */
		VERY_HARD( "dHyV", "sc2.difficulty.veryHard" ),
		/** Insane.    */
		INSANE   ( "asnI", "sc2.difficulty.insane"   ),
		/** Unknown.   */
		UNKNOWN  ( ""    , "general.unknown"         );
		
		/** The binary value (stored in the replay file) of the difficulty. */
		private final String binaryValue;
		/** Cache of the string value.                                      */
		public  final String stringValue;
		
		/**
		 * Creates a new Difficulty.
		 * @param binaryValue the binary value of the game type (stored in the replay file)
		 * @param textKey     key of the text representation
		 */
		private Difficulty( final String binaryValue, final String textKey ) {
			this.binaryValue = binaryValue;
			stringValue      = Language.getText( textKey );
		}
		
		/**
		 * Returns the difficulty specified by its binary value.
		 * @param binaryValue the binary value of the difficulty (stored in the replay)
		 * @return the difficulty specified by its binary value; or <code>UNKNOWN</code> if the binary value is invalid
		 */
		public static Difficulty fromBinaryValue( final String binaryValue ) {
			for ( final Difficulty difficulty : values() )
				if ( difficulty.binaryValue.equals( binaryValue ) )
					return difficulty;
			return UNKNOWN;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Player color.
	 * @author Andras Belicza
	 * @see IReplay
	 */
	public static enum PlayerColor {
		/** Red.         */
		RED        ( "10ct", "sc2.color.red"       , new Color( 180,  20,  30 ) ),
		/** Blue.        */
		BLUE       ( "20ct", "sc2.color.blue"      , new Color(   0,  66, 255 ) ),
		/** Teal.        */
		TEAL       ( "30ct", "sc2.color.teal"      , new Color(  28, 167, 234 ) ),
		/** Purple.      */
		PURPLE     ( "40ct", "sc2.color.purple"    , new Color(  84,   0, 129 ) ),
		/** Yellow.      */
		YELLOW     ( "50ct", "sc2.color.yellow"    , new Color( 235, 225,  41 ) ),
		/** Orange.      */
		ORANGE     ( "60ct", "sc2.color.orange"    , new Color( 254, 138,  14 ) ),
		/** Green.       */
		GREEN      ( "70ct", "sc2.color.green"     , new Color(  22, 128,   0 ) ),
		/** Light pink.  */
		LIGHT_PINK ( "80ct", "sc2.color.lightPink" , new Color( 204, 166, 252 ) ),
		/** Violet.      */
		VIOLET     ( "90ct", "sc2.color.violet"    , new Color(  31,   1, 201 ) ),
		/** Light gray.  */
		LIGHT_GRAY ( "01ct", "sc2.color.lightGray" , new Color(  82,  84, 148 ) ),
		/** Dark green.  */
		DARK_GREEN ( "11ct", "sc2.color.darkGreen" , new Color(  16,  98,  70 ) ),
		/** Brown.       */
		BROWN      ( "21ct", "sc2.color.brown"     , new Color(  78,  42,   4 ) ),
		/** Light green. */
		LIGHT_GREEN( "31ct", "sc2.color.lightGreen", new Color( 150, 255, 145 ) ),
		/** Dark gray.   */
		DARK_GRAY  ( "41ct", "sc2.color.darkGray"  , new Color(  35,  35,  35 ) ),
		/** Pink.        */
		PINK       ( "51ct", "sc2.color.pink"      , new Color( 229,  91, 176 ) ),
		/** Unknown.     */
		UNKNOWN    ( ""    , "general.unknown"     , null );
		
		/** The binary value (stored in the replay file) of the player color. */
		private final String binaryValue;
		/** Cache of the string value.                                        */
		public  final String stringValue;
		/** Color value of this player color.                                 */
		public  final Color  color;
		
		/**
		 * Creates a new PlayerColor.
		 * @param binaryValue the binary value of the game type (stored in the replay file)
		 * @param textKey     key of the text representation
		 * @param color       color value of this player color
		 */
		private PlayerColor( final String binaryValue, final String textKey, final Color color ) {
			this.binaryValue = binaryValue;
			stringValue      = Language.getText( textKey );
			this.color       = color;
		}
		
		/**
		 * Returns the color specified by its binary value.
		 * @param binaryValue the binary value of the color (stored in the replay)
		 * @return the color specified by its binary value; or <code>UNKNOWN</code> if the binary value is invalid
		 */
		public static PlayerColor fromBinaryValue( final String binaryValue ) {
			for ( final PlayerColor color : values() )
				if ( color.binaryValue.equals( binaryValue ) )
					return color;
			return UNKNOWN;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Format.
	 * @author Andras Belicza
	 * @see IReplay
	 */
	public static enum Format {
		/** 1v1.                                  */
		ONE_VS_ONE    ( "1v1", "1v1"            , 1 ),
		/** 2v2.                                  */
		TWO_VS_TWO    ( "2v2", "2v2"            , 2 ),
		/** 3v3.                                  */
		THREE_VS_THREE( "3v3", "3v3"            , 3 ),
		/** 4v4.                                  */
		FOUR_VS_FOUR  ( "4v4", "4v4"            , 4 ),
		/** Free for all.                         */
		FREE_FOR_ALL  ( "AFF", "FFA"            , 1 ),
		/** Non-standard format, for example 2v3. */
		CUSTOM        ( ""   , "general.custom" , 1 ),
		/** Unknown.                              */
		UNKNOWN       ( ""   , "general.unknown", 1 );
		
		/** The binary value (stored in the replay file) of the format.              */
		private final String binaryValue;
		/** Cache of the string value.                                               */
		public  final String stringValue;
		/** Size of team of the game format (defaults to 1 if team size is unknown). */
		public  final int    teamSize;
		
		/**
		 * Creates a new Format.
		 * @param binaryValue the binary value of the game type (stored in the replay file)
		 * @param stringValue text representation; if binaryValue is empty string, this is the key of the text representation
		 * @param teamSize    size of team of the game format
		 */
		private Format( final String binaryValue, final String stringValue, final int teamSize ) {
			this.binaryValue = binaryValue;
			this.stringValue = binaryValue.isEmpty() ? Language.getText( stringValue ) : stringValue;
			this.teamSize    = teamSize;
		}
		
		/**
		 * Returns the race specified by its binary value.
		 * @param binaryValue the binary value of the gateway (stored in the replay)
		 * @return the race specified by its binary value; or <code>UNKNOWN</code> if the binary value is invalid
		 */
		public static Format fromBinaryValue( final String binaryValue ) {
			for ( final Format format : values() )
				if ( format.binaryValue.equals( binaryValue ) )
					return format;
			return UNKNOWN;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Action type.
	 * @author Andras Belicza
	 * @see IAction
	 */
	public static enum ActionType {
		/** Select action type.   */
		SELECT  ( "sc2.actionType.select"   ),
		/** Build action type.    */
		BUILD   ( "sc2.actionType.build"    ),
		/** Train action type.    */
		TRAIN   ( "sc2.actionType.train"    ),
		/** Research action type. */
		RESEARCH( "sc2.actionType.research" ),
		/** Upgrade action type.  */
		UPGRADE ( "sc2.actionType.upgrade"  ),
		/** Other action type.    */
		OTHER   ( "sc2.actionType.other"    ),
		/** Inaction action type. */
		INACTION( "sc2.actionType.inaction" );
		
		/** Key of the text of this action type. */
		public final String textKey;
		/** Cache of the string value.           */
		public final String stringValue;
		
		/**
		 * Creates a new ActionType.
		 * @param textKey key of the text representation
		 */
		private ActionType( final String textKey ) {
			this.textKey = textKey;
			stringValue  = Language.getText( textKey );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Unit Tier.
	 * @author Andras Belicza
	 */
	public static enum UnitTier {
		/** Tier 1.   */
		TIER_1  ( "sc2.unitTier.tier1"   ),
		/** Tier 1.5. */
		TIER_1_5( "sc2.unitTier.tier1.5" ),
		/** Tier 2.   */
		TIER_2  ( "sc2.unitTier.tier2"   ),
		/** Tier 2.5. */
		TIER_2_5( "sc2.unitTier.tier2.5" ),
		/** Tier 3.   */
		TIER_3  ( "sc2.unitTier.tier3"   );
		
		/** Key of the text representation.     */
		public final String textKey;
		/** Cache of the string value.          */
		public final String stringValue;
		/** Color of the unit tier.             */
		public final Color  color;
		/** HTML color string of the unit tier. */
		public final String htmlColor;
		
		/**
		 * Creates a new UnitTier.
		 * @param textKey key of the text representation
		 */
		private UnitTier( final String textKey ) {
			this.textKey = textKey;
			stringValue  = Language.getText( textKey );
			color        = PlayerColor.values()[ new int[] { 1, 6, 4, 5, 0 }[ ordinal() ] ].color;
			htmlColor    = toHtmlColorString( color );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Ability group.
	 * Abilities inside a group have the same icon.
	 * @author Andras Belicza
	 * @see IBaseUseAbilityAction
	 */
	public static enum AbilityGroup {
		STOP,
		MOVE,
		PATROL,
		HOLD_POSITION,
		RETURN_CARGO,
		ATTACK,
		CANCEL,
		GATHER_TERRAN,
		GATHER_ZERG,
		GATHER_PROTOSS,
		BURROW,
		UNBURROW,
		ROOT,
		UPROOT,
		SET_RALLY_POINT,
		SET_WORKER_RALLY_POINT,
		CALLDOWN_MULE( "Calldown MULE" ),
		CALLDOWN_EXTRA_SUPPLIES,
		LIFT_OFF,
		LAND,
		SCANNER_SWEEP,
		SPAWN_CREEP_TUMOR,
		GENERATE_CREEP,
		STOP_GENERATING_CREEP,
		REPAIR,
		LOAD,
		UNLOAD_ALL,
		CHRONO_BOOST,
		LOWER,
		RAISE,
		TANK_MODE,
		SIEGE_MODE,
		TRANSFUSION,
		FORCE_FIELD,
		GUARDIAN_SHIELD,
		UPGRADE_TO_WARP_GATE,
		MUTATE_INTO_A_GATEWAY,
		SALVAGE,
		UPGRADE_TO_ORBITAL_COMMAND,
		CHARGE,
		BLINK,
		SPAWN_CHANGELING,
		FEEDBACK,
		PSIONIC_STORM,
		MORPH_TO_BANELING,
		EXPLODE,
		HEAL,
		CORRUPTION,
		MORPH_TO_BROOD_LORD,
		TRAIN_AN_INTERCEPTOR,
		T250MM_STRIKE_CANNONS,
		CLOAK,
		DECLOAK,
		INFESTED_TERRAN,
		NEURAL_PARASITE,
		UPGRADE_TO_PLANETARY_FORTRESS,
		MUTATE_INTO_HIVE,
		FUNGAL_GROWTH,
		MORPH_TO_OVERSEER,
		STIMPACK,
		MUTATE_INTO_GREATER_SPIRE,
		MUTATE_INTO_LAIR,
		ARM_SILO_WITH_NUKE,
		PHASING_MODE,
		TRANSPORT_MODE,
		ARCHON_WARP,
		TAC_NUCLEAR_STRIKE,
		HOLD_FIRE,
		EMP_ROUND,
		SNIPER_ROUND,
		BUILD_AUTO_TURRET,
		BUILD_POINT_DEFENSE_DRONE,
		SEEKER_MISSILE,
		ASSAULT_MODE,
		FIGHTER_MODE,
		YAMATO_CANNON,
		VORTEX,
		GRAVITON_BEAM,
		MASS_RECALL, // Mothership's (target: Mothership's location)
		SPAWN_LARVA,
		ATTACK_STRUCTURE,
		CONTAMINATE,
		WEAPONS_FREE,
		ENABLE_BUILDING_ATTACK,
		DISABLE_BUILDING_ATTACK,
		
		// HotS additions
		HELLION_MODE,
		BATTLE_MODE,
		ACTIVATE_MINE,
		DEACTIVATE_MINE,
		IGNITE_AFTERBURNERS,
		HIGH_IMPACT_PAYLOAD,
		EXPLOSIVE_PAYLOAD,
		SPAWN_LOCUSTS,
		CONSUME,
		BLIDING_CLOUD,
		ABDUCT,
		PHOTON_OVERCHARGE,
		MASS_RECALL2( "Mass Recall" ), // Mothership Core's (target: a selected Nexus)
		UPGRADE_TO_MOTHERSHIP,
		ENVISION,
		REVELATION,
		TIME_WARP,
		ACTIVATE_PULSAR_BEAM,
		DEACTIVATE_PULSAR_BEAM;
		
		/** Cache of the string value. */
		public final String stringValue;
		
		/**
		 * Creates a new AbilityGroup whose string value will be generated from the name of the enum
		 * ({@link GeneralUtils#convertConstNameToNormal(String)}).
		 */
		private AbilityGroup() {
			stringValue = convertConstNameToNormal( name(), true );
		}
		
		/**
		 * Creates a new AbilityGroup with the specified string value.
		 * @param stringValue string value of the ability group
		 */
		private AbilityGroup( final String stringValue ) {
			this.stringValue = stringValue;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Units of StarCraft II.
	 * @author Andras Belicza
	 */
	public static enum Unit {
		// Terran units
		SCV            ( null, "SCV"       ),
		MARINE         ( UnitTier.TIER_1   ),
		REAPER         ( UnitTier.TIER_1_5 ),
		GHOST          ( UnitTier.TIER_2   ),
		MARAUDER       ( UnitTier.TIER_1_5 ),
		SIEGE_TANK     ( UnitTier.TIER_2_5 ),
		THOR           ( UnitTier.TIER_3   ),
		HELLION        ( UnitTier.TIER_2   ),
		MEDIVAC        ( UnitTier.TIER_2_5 ),
		BANSHEE        ( UnitTier.TIER_2_5 ),
		RAVEN          ( UnitTier.TIER_3   ),
		BATTLECRUISER  ( UnitTier.TIER_3   ),
		VIKING         ( UnitTier.TIER_2_5 ),
		MULE           ( null, "MULE"      ),
		NUCLEAR_MISSILE( null              ),
		HELLBAT        ( UnitTier.TIER_3   ),
		WIDOW_MINE     ( UnitTier.TIER_2   ),
		// Zerg units
		EGG            ( null              ),
		DRONE          ( null              ),
		ZERGLING       ( UnitTier.TIER_1   ),
		OVERLORD       ( null ),
		HYDRALISK      ( UnitTier.TIER_2   ),
		MUTALISK       ( UnitTier.TIER_2   ),
		ULTRALISK      ( UnitTier.TIER_3   ),
		ROACH          ( UnitTier.TIER_1_5 ),
		INFESTOR       ( UnitTier.TIER_2   ),
		CORRUPTOR      ( UnitTier.TIER_2   ),
		BROOD_LORD     ( UnitTier.TIER_3   ),
		BROODLING      ( null              ),
		QUEEN          ( null              ),
		BANELING       ( UnitTier.TIER_1_5 ),
		OVERSEER       ( UnitTier.TIER_2   ),
		CHANGELING     ( null              ),
		INFESTED_TERRAN( null              ),
		SWARM_HOST     ( UnitTier.TIER_2   ),
		LOCUST         ( null              ),
		VIPER          ( UnitTier.TIER_3   ),
		// Protoss units
		ZEALOT         ( UnitTier.TIER_1   ),
		STALKER        ( UnitTier.TIER_1_5 ),
		HIGH_TEMPLAR   ( UnitTier.TIER_3   ),
		DARK_TEMPLAR   ( UnitTier.TIER_3   ),
		SENTRY         ( UnitTier.TIER_1_5 ),
		PHOENIX        ( UnitTier.TIER_2   ),
		CARRIER        ( UnitTier.TIER_3   ),
		VOID_RAY       ( UnitTier.TIER_2   ),
		WARP_PRISM     ( UnitTier.TIER_2   ),
		OBSERVER       ( UnitTier.TIER_2   ),
		COLOSSUS       ( UnitTier.TIER_3   ),
		IMMORTAL       ( UnitTier.TIER_2   ),
		PROBE          ( null              ),
		MOTHERSHIP     ( UnitTier.TIER_3   ),
		ARCHON         ( null              ),
		MOTHERSHIP_CORE( UnitTier.TIER_1_5 ),
		ORACLE         ( UnitTier.TIER_2   ),
		TEMPEST        ( UnitTier.TIER_3   );
		
		/** Tier of the unit.          */
		public final UnitTier unitTier;
		/** Cache of the string value. */
		public final String   stringValue;
		
		/**
		 * Creates a new Unit whose string value will be generated from the name of the enum
		 * ({@link GeneralUtils#convertConstNameToNormal(String)}).
		 * @param unitTier tier of the unit
		 */
		private Unit( final UnitTier unitTier ) {
			this.unitTier = unitTier;
			stringValue   = convertConstNameToNormal( name(), true );
		}
		
		/**
		 * Creates a new Unit with the specified string value.
		 * @param unitTier    tier of this unit
		 * @param stringValue string value of the unit
		 */
		private Unit( final UnitTier unitTier, final String stringValue ) {
			this.unitTier    = unitTier;
			this.stringValue = stringValue;
		}
		
		/**
		 * Returns the owner race of the unit.
		 * @return the owner race of the unit
		 */
		public Race raceOfUnit() {
			if ( this.compareTo( EGG ) < 0 )
				return Race.TERRAN;
			else if ( this.compareTo( ZEALOT ) >= 0 )
				return Race.PROTOSS;
			else return Race.ZERG;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Unit abilities in StarCraft II.
	 * @author Andras Belicza
	 */
	public static enum UnitAbility {
		CORRUPTION( AbilityGroup.CORRUPTION ),
		EXPLODE( AbilityGroup.EXPLODE ),
		FUNGAL_GROWTH( AbilityGroup.FUNGAL_GROWTH ),
		GUARDIAN_SHIELD( AbilityGroup.GUARDIAN_SHIELD ),
		REPAIR( AbilityGroup.REPAIR ),
		TOGGLE_AUTO_REPAIR( AbilityGroup.REPAIR, "Toggle Auto-repair" ),
		FEEDBACK( AbilityGroup.FEEDBACK ),
		MASS_RECALL( AbilityGroup.MASS_RECALL ),
		BUILD_POINT_DEFENSE_DRONE( AbilityGroup.BUILD_POINT_DEFENSE_DRONE ),
		SEEKER_MISSILE( AbilityGroup.SEEKER_MISSILE ),
		GRAVITON_BEAM( AbilityGroup.GRAVITON_BEAM ),
		SPAWN_CHANGELING( AbilityGroup.SPAWN_CHANGELING ),
		DISGUISE( null ),
		DISGUISE_AS_ZEALOT( null ),
		DISGUISE_AS_MARINE_WITH_SHIELD( null ),
		DISGUISE_AS_MARINE_WITHOUT_SHIELD( null ),
		DISGUISE_AS_ZERGLING_WITH_WINGS( null ),
		DISGUISE_AS_ZERGLING_WITHOUT_WINGS( null ),
		INFESTED_TERRAN( AbilityGroup.INFESTED_TERRAN ),
		NEURAL_PARASITE( AbilityGroup.NEURAL_PARASITE ),
		SPAWN_LARVA( AbilityGroup.SPAWN_LARVA ),
		STIMPACK( AbilityGroup.STIMPACK ),
		T250MM_STRIKE_CANNONS( AbilityGroup.T250MM_STRIKE_CANNONS, "250mm Strike Cannons" ),
		GATHER_RESOURCES_TERRAN( AbilityGroup.GATHER_TERRAN ),
		GATHER_RESOURCES_PROTOSS( AbilityGroup.GATHER_PROTOSS ),
		GATHER_RESOURCES_ZERG( AbilityGroup.GATHER_ZERG ),
		RETURN_CARGO( AbilityGroup.RETURN_CARGO ),
		BUILD_AUTO_TURRET( AbilityGroup.BUILD_AUTO_TURRET ),
		CLOAK( AbilityGroup.CLOAK ),
		DECLOAK( AbilityGroup.DECLOAK ),
		SNIPER_ROUND( AbilityGroup.SNIPER_ROUND ),
		HEAL( AbilityGroup.HEAL ),
		TOGGLE_AUTO_HEAL( AbilityGroup.HEAL, "Toggle Auto-heal" ),
		SIEGE_MODE( AbilityGroup.SIEGE_MODE ),
		TANK_MODE( AbilityGroup.TANK_MODE ),
		LOAD( AbilityGroup.LOAD ),
		UNLOAD_ALL( AbilityGroup.UNLOAD_ALL ),
		UNLOAD_ALL_AT( AbilityGroup.UNLOAD_ALL ),
		UNLOAD_UNIT( null ),
		LOAD_ALL( null ),
		YAMATO_CANNON( AbilityGroup.YAMATO_CANNON ),
		ASSAULT_MODE( AbilityGroup.ASSAULT_MODE ),
		FIGHTER_MODE( AbilityGroup.FIGHTER_MODE ),
		PSIONIC_STORM( AbilityGroup.PSIONIC_STORM ),
		TRAIN_AN_INTERCEPTOR( AbilityGroup.TRAIN_AN_INTERCEPTOR, "Train an Interceptor" ),
		MORPH_TO_BROOD_LORD( AbilityGroup.MORPH_TO_BROOD_LORD, "Morph to Brood Lord", Unit.BROOD_LORD ),
		CANCEL_BROOD_LORD_MORPHING( AbilityGroup.CANCEL ),
		BURROW( AbilityGroup.BURROW ),
		UNBURROW( AbilityGroup.UNBURROW ),
		MORPH_TO_BANELING( AbilityGroup.MORPH_TO_BANELING, "Morph to Baneling", Unit.BANELING ),
		CANCEL_BANELING_MORPHING( AbilityGroup.CANCEL ),
		BLINK( AbilityGroup.BLINK ),
		MORPH_TO_OVERSEER( AbilityGroup.MORPH_TO_OVERSEER, "Morph to Overseer", Unit.OVERSEER ),
		CANCEL_OVERSEER_MORPHING( AbilityGroup.CANCEL ),
		FORCE_FIELD( AbilityGroup.FORCE_FIELD ),
		PHASING_MODE( AbilityGroup.PHASING_MODE ),
		TRANSPORT_MODE( AbilityGroup.TRANSPORT_MODE ),
		TAC_NUCLEAR_STRIKE( AbilityGroup.TAC_NUCLEAR_STRIKE ),
		CANCEL_TAC_NUCLEAR_STRIKE( AbilityGroup.CANCEL ),
		EMP_ROUND( AbilityGroup.EMP_ROUND, "EMP Round" ),
		VORTEX( AbilityGroup.VORTEX ),
		TRANSFUSION( AbilityGroup.TRANSFUSION ),
		GENERATE_CREEP( AbilityGroup.GENERATE_CREEP ),
		STOP_GENERATING_CREEP( AbilityGroup.STOP_GENERATING_CREEP ),
		SPAWN_CREEP_TUMOR( AbilityGroup.SPAWN_CREEP_TUMOR ),
		CHARGE( AbilityGroup.CHARGE ),
		ATTACK_STRUCTURE( AbilityGroup.ATTACK_STRUCTURE ),
		TOGGLE_AUTO_ATTACK_STRUCTURE( AbilityGroup.ATTACK_STRUCTURE, "Toggle Auto-attack Structure" ), // This was removed (replaced) in 1.4.0
		CONTAMINATE( AbilityGroup.CONTAMINATE ),
		WEAPONS_FREE( AbilityGroup.WEAPONS_FREE ),
		ENABLE_BUILDING_ATTACK( AbilityGroup.ENABLE_BUILDING_ATTACK ),
		DISABLE_BUILDING_ATTACK( AbilityGroup.DISABLE_BUILDING_ATTACK ),
		TOGGLE_AUTO_CHARGE( AbilityGroup.CHARGE, "Toggle Auto-charge" ),
		TOGGLE_AUTO_TRAIN_INTERCEPTOR( AbilityGroup.TRAIN_AN_INTERCEPTOR, "Toggle Auto-train Interceptor" ),
		
		// HotS additions
		ACTIVATE_MINE( AbilityGroup.ACTIVATE_MINE ),
		DEACTIVATE_MINE( AbilityGroup.DEACTIVATE_MINE ),
		SPAWN_LOCUSTS( AbilityGroup.SPAWN_LOCUSTS ),
		TOGGLE_AUTO_SPAWN_LOCUSTS( AbilityGroup.SPAWN_LOCUSTS, "Toggle Auto-spawn Locusts" ),
		HELLION_MODE( AbilityGroup.HELLION_MODE ),
		BATTLE_MODE( AbilityGroup.BATTLE_MODE ),
		IGNITE_AFTERBURNERS( AbilityGroup.IGNITE_AFTERBURNERS ),
		HIGH_IMPACT_PAYLOAD( AbilityGroup.HIGH_IMPACT_PAYLOAD ),
		CANCEL_HIGH_IMPACT_PAYLOAD( AbilityGroup.CANCEL ),
		EXPLOSIVE_PAYLOAD( AbilityGroup.EXPLOSIVE_PAYLOAD ),
		CANCEL_EXPLOSIVE_PAYLOAD( AbilityGroup.CANCEL ),
		CONSUME( AbilityGroup.CONSUME ),
		BLIDING_CLOUD( AbilityGroup.BLIDING_CLOUD ),
		ABDUCT( AbilityGroup.ABDUCT ),
		PHOTON_OVERCHARGE( AbilityGroup.PHOTON_OVERCHARGE ),
		MASS_RECALL2( AbilityGroup.MASS_RECALL2, "Mass Recall" ), // Mothership Core's (target: a selected Nexus)
		UPGRADE_TO_MOTHERSHIP( AbilityGroup.UPGRADE_TO_MOTHERSHIP ),
		CANCEL_UPGRADE_TO_MOTHERSHIP( AbilityGroup.CANCEL ),
		ENVISION( AbilityGroup.ENVISION ),
		REVELATION( AbilityGroup.REVELATION ),
		TIME_WARP( AbilityGroup.TIME_WARP ),
		ACTIVATE_PULSAR_BEAM( AbilityGroup.ACTIVATE_PULSAR_BEAM ),
		DEACTIVATE_PULSAR_BEAM( AbilityGroup.DEACTIVATE_PULSAR_BEAM );
		
		/** Cache of the string value.                                                    */
		public final String       stringValue;
		/** Ability group.                                                                */
		public final AbilityGroup abilityGroup;
		/** Target unit of the transformation (if this unit ability is a transformation). */
		public final Unit         transformationTargetUnit;
		
		/**
		 * Creates a new UnitAbility whose string value will be generated from the name of the enum
		 * ({@link GeneralUtils#convertConstNameToNormal(String)}).
		 * @param abilityGroup ability group
		 */
		private UnitAbility( final AbilityGroup abilityGroup ) {
			this.abilityGroup        = abilityGroup;
			stringValue              = convertConstNameToNormal( name(), true );
			transformationTargetUnit = null;
		}
		
		/**
		 * Creates a new UnitAbility with the specified string value.
		 * @param abilityGroup ability group
		 * @param stringValue  string value of the unit ability
		 */
		private UnitAbility( final AbilityGroup abilityGroup, final String stringValue ) {
			this.abilityGroup        = abilityGroup;
			this.stringValue         = stringValue;
			transformationTargetUnit = null;
		}
		
		/**
		 * Creates a new UnitAbility with the specified string value.
		 * @param abilityGroup             ability group
		 * @param stringValue              string value of the unit ability
		 * @param transformationTargetUnit target unit of the transformation (if this unit ability is a transformation)
		 */
		private UnitAbility( final AbilityGroup abilityGroup, final String stringValue, final Unit transformationTargetUnit ) {
			this.abilityGroup             = abilityGroup;
			this.stringValue              = stringValue;
			this.transformationTargetUnit = transformationTargetUnit;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Buildings of StarCraft II.<br>
	 * Listing order counts: it is used in determining the race of the building.
	 * @author Andras Belicza
	 */
	public static enum Building {
		// Terran buildings
		COMMAND_CENTER( 5, 5 ),
		SUPPLY_DEPOT( 2, 2 ),
		REFINERY( 3, 3 ),
		BARRACKS( 3, 3 ),
		ENGINEERING_BAY( 3, 3 ),
		MISSILE_TURRET( 2, 2 ),
		BUNKER( 3, 3 ),
		SENSOR_TOWER( 1, 1 ),
		GHOST_ACADEMY( 3, 3 ),
		FACTORY( 3, 3 ),
		STARPORT( 3, 3 ),
		ARMORY( 3, 3 ),
		FUSION_CORE( 3, 3 ),
		TECH_LAB_BARRACKS( 2, 2, "Tech Lab (Barracks)"),
		REACTOR_BARRACKS( 2, 2, "Reactor (Barracks)" ),
		TECH_LAB_FACTORY( 2, 2, "Tech Lab (Factory)"),
		TECH_LAB_STARPORT( 2, 2, "Tech Lab (Starport)"),
		REACTOR_FACTORY( 2, 2, "Reactor (Factory)" ),
		REACTOR_STARPORT( 2, 2, "Reactor (Starport)" ),
		ORBITAL_COMMAND( 5, 5 ),
		PLANETARY_FORTRESS( 5, 5 ),
		// Zerg buildings
		HATCHERY( 5, 5 ),
		CREEP_TUMOR( 1, 1 ),
		EXTRACTOR( 3, 3 ),
		SPAWNING_POOL( 3, 3 ),
		EVOLUTION_CHAMBER( 3, 3 ),
		HYDRALISK_DEN( 3, 3 ),
		SPIRE( 2, 2 ),
		ULTRALISK_CAVERN( 3, 3 ),
		INFESTATION_PIT( 3, 3 ),
		NYDUS_NETWORK( 3, 3 ),
		BANELING_NEST( 3, 3 ),
		ROACH_WARREN( 3, 3 ),
		SPINE_CRAWLER( 2, 2 ),
		SPORE_CRAWLER( 2, 2 ),
		LAIR( 5, 5 ),
		HIVE( 5, 5 ),
		NYDUS_WORM( 3, 3 ),
		// Protoss buildings
		NEXUS( 5, 5 ),
		PYLON( 2, 2 ),
		ASSIMILATOR( 3, 3 ),
		GATEWAY( 3, 3 ),
		FORGE( 3, 3 ),
		FLEET_BEACON( 3, 3 ),
		TWILIGHT_COUNCIL( 3, 3 ),
		PHOTON_CANNON( 2, 2 ),
		STARGATE( 3, 3 ),
		TEMPLAR_ARCHIVES( 3, 3 ),
		DARK_SHRINE( 2, 2 ),
		ROBOTICS_BAY( 3, 3 ),
		ROBOTICS_FACILITY( 3, 3 ),
		CYBERNETICS_CORE( 3, 3 ),
		WARP_GATE( 3, 3 ),
		OBELISK( 2, 2 );
		
		/** Width of the building.     */
		public final int    width;
		/** Height of the building.    */
		public final int    height;
		/** Cache of the string value. */
		public final String stringValue;
		
		/**
		 * Creates a new Building whose string value will be generated from the name of the enum
		 * ({@link GeneralUtils#convertConstNameToNormal(String)}).
		 * @param width  width of the building
		 * @param height height of the building
		 */
		private Building( final int width, final int height ) {
			this.width  = width;
			this.height = height;
			stringValue = convertConstNameToNormal( name(), true );
		}
		
		/**
		 * Creates a new Building with the specified string value.
		 * @param width       width of the building
		 * @param height      height of the building
		 * @param stringValue string value of the building
		 */
		private Building( final int width, final int height, final String stringValue ) {
			this.width       = width;
			this.height      = height;
			this.stringValue = stringValue;
		}
		
		/**
		 * Returns the owner race of the building.
		 * @return the owner race of the building
		 */
		public Race raceOfBuilding() {
			if ( this.compareTo( PLANETARY_FORTRESS ) < 1 )
				return Race.TERRAN;
			else if ( this.compareTo( NEXUS ) > -1 )
				return Race.PROTOSS;
			else return Race.ZERG;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Building abilities in StarCraft II.
	 * @author Andras Belicza
	 */
	public static enum BuildingAbility {
		CALLDOWN_MULE( AbilityGroup.CALLDOWN_MULE, "Calldown MULE" ),
		SET_RALLY_POINT( AbilityGroup.SET_RALLY_POINT ),
		SET_WORKER_RALLY_POINT( AbilityGroup.SET_WORKER_RALLY_POINT ),
		CALLDOWN_EXTRA_SUPPLIES( AbilityGroup.CALLDOWN_EXTRA_SUPPLIES ),
		CHRONO_BOOST( AbilityGroup.CHRONO_BOOST ),
		SCANNER_SWEEP( AbilityGroup.SCANNER_SWEEP ),
		CANCEL_AN_ADDON( AbilityGroup.CANCEL, "Cancel an Addon" ),
		MUTATE_INTO_LAIR( AbilityGroup.MUTATE_INTO_LAIR, "Mutate into Lair" ),
		CANCEL_LAIR_UPGRADE( AbilityGroup.CANCEL ),
		MUTATE_INTO_HIVE( AbilityGroup.MUTATE_INTO_HIVE, "Mutate into Hive" ),
		CANCEL_HIVE_UPGRADE( AbilityGroup.CANCEL ),
		MUTATE_INTO_GREATER_SPIRE( AbilityGroup.MUTATE_INTO_GREATER_SPIRE, "Mutate into Greater Spire" ),
		CANCEL_GREATER_SPIRE_UPGRADE( AbilityGroup.CANCEL ),
		UPGRADE_TO_PLANETARY_FORTRESS( AbilityGroup.UPGRADE_TO_PLANETARY_FORTRESS, "Upgrade to Planetary Fortress" ),
		CANCEL_PLANETARY_FORTRESS_UPGRADE( AbilityGroup.CANCEL ),
		UPGRADE_TO_ORBITAL_COMMAND( AbilityGroup.UPGRADE_TO_ORBITAL_COMMAND, "Upgrade to Orbital Command" ),
		CANCEL_ORBITAL_COMMAND_UPGRADE( AbilityGroup.CANCEL ),
		UPGRADE_TO_WARP_GATE( AbilityGroup.UPGRADE_TO_WARP_GATE, "Upgrade to Warp Gate" ),
		CANCEL_WARP_GATE_UPGRADE( AbilityGroup.CANCEL ),
		MUTATE_INTO_A_GATEWAY( AbilityGroup.MUTATE_INTO_A_GATEWAY, "Mutate into a Gateway" ),
		CANCEL_GATEWAY_MUTATION( AbilityGroup.CANCEL ),
		LOAD( AbilityGroup.LOAD ),
		UNLOAD_ALL( AbilityGroup.UNLOAD_ALL ),
		UNLOAD_ALL_AT( AbilityGroup.UNLOAD_ALL ),
		UNLOAD_UNIT( null ),
		LOAD_ALL( null ),
		LIFT_OFF( AbilityGroup.LIFT_OFF ),
		LAND( AbilityGroup.LAND ),
		SALVAGE( AbilityGroup.SALVAGE ),
		UPROOT( AbilityGroup.UPROOT ),
		ROOT( AbilityGroup.ROOT ),
		BUILD_CREEP_TUMOR( AbilityGroup.SPAWN_CREEP_TUMOR ),
		LOWER( AbilityGroup.LOWER, "Lower Supply Depot" ),
		RAISE( AbilityGroup.RAISE, "Raiser Supply Depot" ),
		ARM_SILO_WITH_NUKE( AbilityGroup.ARM_SILO_WITH_NUKE, "Arm Silo with Nuke" );
		
		/** Cache of the string value. */
		public final String       stringValue;
		/** Ability group. */
		public final AbilityGroup abilityGroup; 
		
		/**
		 * Creates a new BuildingAbility whose string value will be generated from the name of the enum
		 * ({@link GeneralUtils#convertConstNameToNormal(String)}).
		 * @param abilityGroup ability group
		 */
		private BuildingAbility( final AbilityGroup abilityGroup ) {
			this.abilityGroup = abilityGroup;
			stringValue       = convertConstNameToNormal( name(), true );
		}
		
		/**
		 * Creates a new BuildingAbility with the specified string value.
		 * @param abilityGroup ability group
		 * @param stringValue  string value of the building ability
		 */
		private BuildingAbility( final AbilityGroup abilityGroup, final String stringValue ) {
			this.abilityGroup = abilityGroup;
			this.stringValue  = stringValue;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Upgrades of StarCraft II.
	 * @author Andras Belicza
	 */
	public static enum Upgrade {
		// Terran upgrades
		TERRAN_HI_SEC_AUTO_TRACKING( "Terran Hi-sec Auto Tracking" ),
		TERRAN_BUILDING_ARMOR,
		TERRAN_INFANTRY_WEAPONS_1,
		TERRAN_INFANTRY_WEAPONS_2,
		TERRAN_INFANTRY_WEAPONS_3,
		TERRAN_INFANTRY_ARMOR_1,
		TERRAN_INFANTRY_ARMOR_2,
		TERRAN_INFANTRY_ARMOR_3,
		TERRAN_NEOSTEEL_FRAME,
		TERRAN_VEHICLE_PLATING_1,
		TERRAN_VEHICLE_PLATING_2,
		TERRAN_VEHICLE_PLATING_3,
		TERRAN_VEHICLE_WEAPONS_1,
		TERRAN_VEHICLE_WEAPONS_2,
		TERRAN_VEHICLE_WEAPONS_3,
		TERRAN_SHIP_PLATING_1,
		TERRAN_SHIP_PLATING_2,
		TERRAN_SHIP_PLATING_3,
		TERRAN_SHIP_WEAPONS_1,
		TERRAN_SHIP_WEAPONS_2,
		TERRAN_SHIP_WEAPONS_3,
		// Hots
		TERRAN_VEHICLE_AND_SHIP_PLATING_1( "Terran Vehicle and Ship Plating 1" ),
		TERRAN_VEHICLE_AND_SHIP_PLATING_2( "Terran Vehicle and Ship Plating 2" ),
		TERRAN_VEHICLE_AND_SHIP_PLATING_3( "Terran Vehicle and Ship Plating 3" ),
		// Zerg upgrades
		ZERG_MELEE_ATTACKS_1,
		ZERG_MELEE_ATTACKS_2,
		ZERG_MELEE_ATTACKS_3,
		ZERG_GROUND_CARAPACE_1,
		ZERG_GROUND_CARAPACE_2,
		ZERG_GROUND_CARAPACE_3,
		ZERG_MISSILE_ATTACKS_1,
		ZERG_MISSILE_ATTACKS_2,
		ZERG_MISSILE_ATTACKS_3,
		ZERG_FLYER_ATTACKS_1,
		ZERG_FLYER_ATTACKS_2,
		ZERG_FLYER_ATTACKS_3,
		ZERG_FLYER_CARAPACE_1,
		ZERG_FLYER_CARAPACE_2,
		ZERG_FLYER_CARAPACE_3,
		// Protoss upgrades
		PROTOSS_GROUND_WEAPONS_1,
		PROTOSS_GROUND_WEAPONS_2,
		PROTOSS_GROUND_WEAPONS_3,
		PROTOSS_GROUND_ARMOR_1,
		PROTOSS_GROUND_ARMOR_2,
		PROTOSS_GROUND_ARMOR_3,
		PROTOSS_SHIELD_1,
		PROTOSS_SHIELD_2,
		PROTOSS_SHIELD_3,
		PROTOSS_AIR_WEAPONS_1,
		PROTOSS_AIR_WEAPONS_2,
		PROTOSS_AIR_WEAPONS_3,
		PROTOSS_AIR_ARMOR_1,
		PROTOSS_AIR_ARMOR_2,
		PROTOSS_AIR_ARMOR_3;
		
		/** Cache of the string value. */
		public final String stringValue;
		
		/**
		 * Creates a new Upgrade whose string value will be generated from the name of the enum
		 * ({@link GeneralUtils#convertConstNameToNormal(String)}).
		 */
		private Upgrade() {
			stringValue = convertConstNameToNormal( name(), true );
		}
		
		/**
		 * Creates a new Upgrade with the specified string value.
		 * @param stringValue string value of the upgrade
		 */
		private Upgrade( final String stringValue ) {
			this.stringValue = stringValue;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Researches of StarCraft II.
	 * @author Andras Belicza
	 */
	public static enum Research {
		// Terran researches
		TERRAN_NITRO_PACKS,
		TERRAN_STIMPACK,
		TERRAN_COMBAT_SHIELD,
		TERRAN_CONCUSSIVE_SHELLS,
		TERRAN_SIEGE_TECH,
		TERRAN_INFERNAL_PRE_IGNITER( "Terran Infernal Pre-Igniter" ),
		TERRAN_CLOAKING_FIELD,
		TERRAN_CADUCEUS_REACTOR,
		TERRAN_CORVID_REACTOR,
		TERRAN_SEEKER_MISSILE,
		TERRAN_DURABLE_MATERIALS,
		TERRAN_PERSONAL_CLOAKING,
		TERRAN_MOEBIUS_REACTOR,
		TERRAN_WEAPON_REFIT,
		TERRAN_BEHEMOTH_REACTOR,
		TERRAN_250MM_STRIKE_CANNONS,
		TERRAN_DRILLING_CLAWS,
		TERRAN_TRANSFORMATION_SERVOS,
		// Zerg researches
		ZERG_BURROW,
		ZERG_PNEUMATIZED_CARAPACE,
		ZERG_VENTRAL_SACS,
		ZERG_ADRENAL_GLANDS,
		ZERG_METABOLIC_BOOST,
		ZERG_GROOVED_SNIPES,
		ZERG_PATHOGEN_GLANDS,
		ZERG_NEURAL_PARASITE,
		ZERG_CENTRIFUGAL_HOOKS,
		ZERG_GLIAL_RECONSTITUTION,
		ZERG_TUNNELING_CLAWS,
		ZERG_CHITINOUS_PLATING,
		ZERG_ENDURING_LOCUSTS,
		ZERG_MUSCULAR_AUGMENTS,
		// Protoss researches
		PROTOSS_GRAVITIC_BOOSTER,
		PROTOSS_GRAVITIC_DRIVE,
		PROTOSS_EXTENDED_THERMAL_LANCE,
		PROTOSS_KHAYDARIN_AMULET,
		PROTOSS_PSIONIC_STORM,
		PROTOSS_WARP_GATE,
		PROTOSS_HALLUCINATION,
		PROTOSS_CHARGE,
		PROTOSS_BLINK,
		PROTOSS_FLUX_VANES,
		PROTOSS_GRAVITON_CATAPULT,
		PROTOSS_ANION_PULSE_CRYSTALS( "Protoss Anion Pulse-Crystals" );
		
		/** Cache of the string value. */
		public final String stringValue;
		
		/**
		 * Creates a new Research whose string value will be generated from the name of the enum
		 * ({@link GeneralUtils#convertConstNameToNormal(String)}).
		 */
		private Research() {
			stringValue = convertConstNameToNormal( name(), true );
		}
		
		/**
		 * Creates a new Research with the specified string value.
		 * @param stringValue string value of the research
		 */
		private Research( final String stringValue ) {
			this.stringValue = stringValue;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Map objects stored in map files.
	 * @author Andras Belicza
	 * @see IMapInfo
	 */
	public static enum MapObject {
		MINERAL_FIELD                   ( 1, 1, "MineralField"                 , null                  ),
		RICH_MINERAL_FIELD              ( 1, 1, "RichMineralField"             , null                  ),
		VESPENE_GEYSER                  ( 3, 3, "VespeneGeyser"                , null                  ),
		SPACE_PLATFORM_GEYSER           ( 3, 3, "SpacePlatformGeyser"          , null                  ),
		XEL_NAGA_TOWER                  ( 5, 5, "XelNagaTower"                 , "Xel'Naga Tower"      ),
		DESTRUCTIBLE_ROCK_4X4           ( 4, 4, "DestructibleRock4x4"          , "Destructible Rock"   ),
		DESTRUCTIBLE_ROCK_6X6           ( 4, 4, "DestructibleRock6x6"          , "Destructible Rock"   ),
		DESTRUCTIBLE_ROCK_2X6_VERTICAL  ( 2, 6, "DestructibleRock2x6Vertical"  , "Destructible Rock"   ),
		DESTRUCTIBLE_ROCK_2X6_HORIZONTAL( 6, 2, "DestructibleRock2x6Horizontal", "Destructible Rock"   ),
		DESTRUCTIBLE_DEBRIS_4X4         ( 4, 4, "DestructibleDebris4x4"        , "Destructible Debris" ),
		DESTRUCTIBLE_DEBRIS_6X6         ( 6, 6, "DestructibleDebris6x6"        , "Destructible Debris" );
		
		/** Width of the object.  */
		public  final int    width;
		/** Height of the object. */
		public  final int    height;
		/** The binary value (stored in the replay file) of the game type. */
		private final String binaryValue;
		/** Optional tool tip to display for this object. */
		public  final String toolTip;
		
		/**
		 * Creates a new MapObject.
		 * @param width       width of the map object
		 * @param height      height of the map object
		 * @param binaryValue the binary value of the map object (stored in the map file)
		 * @param toolTip     optional tool tip of the map object
		 */
		private MapObject( final int width, final int height, final String binaryValue, final String toolTip ) {
			this.width       = width;
			this.height      = height;
			this.binaryValue = binaryValue;
			this.toolTip     = toolTip;
		}
		
		/**
		 * Returns the map object specified by its binary value.
		 * @param binaryValue the binary value of the game speed (stored in the replay)
		 * @return the map object specified by its binary value; or <code>null</code> if the binary value does not match any map object
		 */
		public static MapObject fromBinaryValue( final String binaryValue ) {
			for ( final MapObject mapObject : values() )
				if ( mapObject.binaryValue.equals( binaryValue ) )
					return mapObject;
			
			return null;
		}
	}
	
	/**
	 * Converts a const name to a normal, human readable format.
	 * @param text text to be converted
	 * @param capitalWordStart if true, all words will start with a capital letter
	 * @return the converted text
	 */
	public static String convertConstNameToNormal( final String text, final boolean capitalWordStart ) {
		final StringBuilder sb = new StringBuilder();
		
		boolean nextCharIsUpper = true;
		for ( final char ch : text.toCharArray() ) {
			if ( ch == '_' ) {
				sb.append( ' ' );
				nextCharIsUpper = capitalWordStart;
			}
			else if ( nextCharIsUpper ) {
				sb.append( ch );
				nextCharIsUpper = false;
			}
			else
				sb.append( Character.toLowerCase( ch ) );
		}
		
		return sb.toString();
	};
	
	/**
	 * Returns the HTML string representation of a color.
	 * @param color color to be converted
	 * @return the HTML string representation of a color
	 */
	private static String toHtmlColorString( final Color color ) {
		return String.format( "%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue() );
	}
	
}
