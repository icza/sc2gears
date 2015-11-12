/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.util;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gearspluginapi.api.enums.LadderSeason;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.MatchResult;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.Icon;

/**
 * Template engine to rename replays based on a template.<br>
 * 
 * The implementation is designed for 1-thread-use only.
 * 
 * @author Andras Belicza
 */
public class TemplateEngine {
	
	/** Version of the template engine. */
	public static final String ENGINE_VERSION = "2.5";
	
	/** Set of not allowed characters in file name. */
	private static final Set< Character > NOT_ALLOWED_FILE_NAME_CHAR_SET = new HashSet< Character >();
	static {
		for ( int i = 0; i < 32; i++ )
			NOT_ALLOWED_FILE_NAME_CHAR_SET.add( (char) i );
		
		final String notAllowedChars;
		if ( GeneralUtils.isWindows() )
			notAllowedChars = "?*:|\"";
		else if ( GeneralUtils.isMac() )
			notAllowedChars = ":";
		else if ( GeneralUtils.isUnix() )
			notAllowedChars = "?%*:|\""; // For Ext3 only '/' and the NULL is forbidden, but linux also handles NTFS and FAT file systems, so let's not allow what Windows doesn't allow...
		else
			notAllowedChars = "?%*:|\"";
		
		for ( final char nach : notAllowedChars.toCharArray() )
			NOT_ALLOWED_FILE_NAME_CHAR_SET.add( nach );
	}
	
	/**
	 * Template symbol.
	 * @author Andras Belicza
	 */
	public enum Symbol {
		// Not yet used letters: a H i I j J k K o O P Q t u U V x X y z Z
		
		// Replay info
		FORMAT                  ( "/f"  , 'f' , "replayops.renameDialog.symbol.format"               , "replayops.renameDialog.symbolExplained.format"               , Icons.FORMAT               ),
		MATCHUP                 ( "/T"  , 'T' , "replayops.renameDialog.symbol.raceMatchup"          , "replayops.renameDialog.symbolExplained.raceMatchup"          , Icons.RACE_ANY             ),
		DATE                    ( "/d"  , 'd' , "replayops.renameDialog.symbol.date"                 , "replayops.renameDialog.symbolExplained.date"                 , Icons.CALENDAR_SELECT      ),
		DATE_TIME               ( "/D"  , 'D' , "replayops.renameDialog.symbol.dateTime"             , "replayops.renameDialog.symbolExplained.dateTime"             , Icons.CALENDAR_BLUE        ),
		GAME_TYPE               ( "/Y"  , 'Y' , "replayops.renameDialog.symbol.gameType"             , "replayops.renameDialog.symbolExplained.gameType"             , null                       ),
		GATEWAY                 ( "/G"  , 'G' , "replayops.renameDialog.symbol.gateway"              , "replayops.renameDialog.symbolExplained.gateway"              , Icons.SERVER               ),
		GAME_LENGTH             ( "/l"  , 'l' , "replayops.renameDialog.symbol.gameLength"           , "replayops.renameDialog.symbolExplained.gameLength"           , Icons.CLOCK                ),
		GAME_LENGTH_LONG        ( "/L"  , 'L' , "replayops.renameDialog.symbol.gameLengthLong"       , "replayops.renameDialog.symbolExplained.gameLengthLong"       , Icons.CLOCK                ),
		LADDER_SEASON           ( "/s"  , 's' , "replayops.renameDialog.symbol.ladderSeason"         , "replayops.renameDialog.symbolExplained.ladderSeason"         , Icons.LADDER               ),
		REPLAY_VERSION          ( "/v"  , 'v' , "replayops.renameDialog.symbol.replayVersion"        , "replayops.renameDialog.symbolExplained.replayVersion"        , Icons.DOCUMENT_ATTRIBUTE_V ),
		BUILD_NUMBER            ( "/b"  , 'b' , "replayops.renameDialog.symbol.buildNumber"          , "replayops.renameDialog.symbolExplained.buildNumber"          , Icons.DOCUMENT_ATTRIBUTE_B ),
		// Players
		PLAYER_INFO_BLOCK       ( "<>"  , '<' , "replayops.renameDialog.symbol.playerInfoBlock"      , null                                                          , Icons.USERS                ),
		PLAYER_NAME             ( "/px" , 'p' , "replayops.renameDialog.symbol.playerName"           , "replayops.renameDialog.symbolExplained.playerName"           , Icons.USER                 ),
		RACE_FIRST_LETTER       ( "/rx" , 'r' , "replayops.renameDialog.symbol.playerRaceFirstLetter", "replayops.renameDialog.symbolExplained.playerRaceFirstLetter", Icons.RACE_ALL             ),
		RACE                    ( "/Rx" , 'R' , "replayops.renameDialog.symbol.playerRace"           , "replayops.renameDialog.symbolExplained.playerRace"           , Icons.RACE_ALL             ),
		LEAGUE                  ( "/Bx" , 'B' , "replayops.renameDialog.symbol.league"               , "replayops.renameDialog.symbolExplained.league"               , Icons.SC2LEAGUE            ),
		APM                     ( "/Ax" , 'A' , "replayops.renameDialog.symbol.playerApm"            , "replayops.renameDialog.symbolExplained.playerApm"            , null                       ),
		EAPM                    ( "/Ex" , 'E' , "replayops.renameDialog.symbol.playerEapm"           , "replayops.renameDialog.symbolExplained.playerEapm"           , null                       ),
		RESULT                  ( "/Wx" , 'W' , "replayops.renameDialog.symbol.playerResult"         , "replayops.renameDialog.symbolExplained.playerResult"         , Icons.WIN                  ),
		ALL_PLAYER_NAMES        ( "/q"  , 'q' , "replayops.renameDialog.symbol.allPlayerNames"       , "replayops.renameDialog.symbolExplained.allPlayerNames"       , Icons.USERS                ),
		ALL_PLAYER_NAMES_GROUPED( "/g"  , 'g' , "replayops.renameDialog.symbol.allPlayerNamesGrouped", "replayops.renameDialog.symbolExplained.allPlayerNamesGrouped", Icons.USERS                ),
		WINNERS                 ( "/w"  , 'w' , "replayops.renameDialog.symbol.winners"              , "replayops.renameDialog.symbolExplained.winners"              , Icons.WIN                  ),
		// Map
		MAP_NAME                ( "/m"  , 'm' , "replayops.renameDialog.symbol.mapName"              , "replayops.renameDialog.symbolExplained.mapName"              , Icons.MAP                  ),
		MAP_FIRST_WORDS         ( "/Mx" , 'M' , "replayops.renameDialog.symbol.firstWordsOfMapName"  , "replayops.renameDialog.symbolExplained.firstWordsOfMapName"  , Icons.MAP_PENCIL           ),
		MAP_FIRST_LETTERS       ( "/Nxy", 'N' , "replayops.renameDialog.symbol.firstLettersOfMapName", "replayops.renameDialog.symbolExplained.firstLettersOfMapName", Icons.MAP_PENCIL           ),
		MAP_NAME_ACRONYM        ( "/S"  , 'S' , "replayops.renameDialog.symbol.mapNameAcronym"       , "replayops.renameDialog.symbolExplained.mapNameAcronym"       , Icons.MAP_PENCIL           ),
		// Counters
		COUNTER                 ( "/c"  , 'c' , "replayops.renameDialog.symbol.counter"              , "replayops.renameDialog.symbolExplained.counter"              , Icons.COUNTER_RESET        ),
		COUNTER2                ( "/Cx" , 'C' , "replayops.renameDialog.symbol.counter2"             , "replayops.renameDialog.symbolExplained.counter2"             , Icons.COUNTER_RESET        ),
		REPLAY_COUNTER          ( "/Fx" , 'F' , "replayops.renameDialog.symbol.replayCounter"        , "replayops.renameDialog.symbolExplained.replayCounter"        , Icons.COUNTER_COUNT_UP     ),
		// General
		SUBFOLDER_SEPARATOR     ( "\\"  , '\\', "replayops.renameDialog.symbol.subfolderSeparator"   , "replayops.renameDialog.symbolExplained.subfolderSeparator"   , Icons.FOLDER_TREE          ),
		ORIGINAL_NAME           ( "/n"  , 'n' , "replayops.renameDialog.symbol.name"                 , "replayops.renameDialog.symbolExplained.name"                 , Icons.CARD                 ),
		EXTENSION               ( "/e"  , 'e' , "replayops.renameDialog.symbol.extension"            , "replayops.renameDialog.symbolExplained.extension"            , Icons.SC2                  ),
		MD5                     ( "/h"  , 'h' , "replayops.renameDialog.symbol.md5"                  , "replayops.renameDialog.symbolExplained.md5"                  , Icons.TAG_HASH             );
		
		
		/** Template fragment.              */
		public final String  fragment;
		/** Template fragment, short.       */
		public final String  shortFragment;
		/** Symbol character.               */
		public final char    symbolChar;
		/** Key of the text representation. */
		private final String textKey;
		/** Key of the explained text.      */
		public  final String explainedTextKey;
		/** Icon of the symbol group.       */
		public final Icon    icon;
		
		/**
		 * Creates a new Symbol.
		 * @param fragment         template fragment
		 * @param textKey          key of the text representation
		 * @param explainedTextKey key of the explained text
		 * @param icon             icon of the symbol
		 */
		private Symbol( final String fragment, final char symbolChar, final String textKey, final String explainedTextKey, final Icon icon ) {
			this.fragment         = fragment;
			this.symbolChar       = symbolChar;
			this.textKey          = textKey;
			this.explainedTextKey = explainedTextKey;
			this.icon             = icon;
			
			shortFragment         = "/" + symbolChar;
		}
		
		@Override
		public String toString() {
			if ( this == PLAYER_INFO_BLOCK ) {
				final StringBuilder builder = new StringBuilder();
				for ( final Symbol symbol : PLAYER_INFO_SYMBOLS ) {
					if ( builder.length() > 0 )
						builder.append( ", " );
					builder.append( symbol.shortFragment );
				}
				return Language.getText( textKey, builder.toString() );
			}
			else if ( this == RESULT )
				return Language.getText( textKey, MatchResult.getDescription() );
			else
				return Language.getText( textKey );
		};
	}
	
	
	/** Player info symbols (these can be used inside player info blocks). */
	private static final Symbol[] PLAYER_INFO_SYMBOLS = { Symbol.PLAYER_NAME, Symbol.RACE_FIRST_LETTER, Symbol.RACE, Symbol.LEAGUE, Symbol.APM, Symbol.EAPM, Symbol.RESULT };
	
	/** Symbol char - symbol map. */
	private static final Map< Character, Symbol > CHAR_SYMBOL_MAP = new HashMap< Character, Symbol >();
	static {
		for ( final Symbol symbol : Symbol.values() )
			CHAR_SYMBOL_MAP.put( symbol.symbolChar, symbol );
	}
	
	/** Tells if replay cache is enabled. */
	private final boolean cacheEnabled = Settings.getBoolean( Settings.KEY_SETTINGS_MISC_CACHE_PREPROCESSED_REPLAYS );
	
	/** The template as a string. */
	private final String template;
	/** Char array of the template. */
	private final char[] templateCharArray;
	
	/** Tells if applying the template requires to parse the game events too. */
	private boolean requiresGameEvent;
	
	/** Tells if replay could not be parsed. */
	private boolean replayParseError;
	
	/** Counter value for the /c symbol. */
	private int counter  = 1;
	/** Counter value for the /C symbol. */
	private int counter2 = 1;
	
	/** Reference to the name builder.                                    */
	private final StringBuilder nameBuilder = new StringBuilder();
	/** Replay file to apply the template to.                             */
	private File   file;
	/** Target folder to for the replay.                                  */
	private File   targetFolder;
	/** Target folder with sub-folders to count files in (in case of /F). */
	private File   targetFolderWithSubfolders;
	/** Reference to the replay if it has already been parsed.            */
	private Replay replay;
	
	/**
	 * Creates a new TemplateEngine.
	 * @param template the template
	 * @throws Exception if the template is invalid
	 */
	public TemplateEngine( final String template ) throws Exception {
		if ( template.length() == 0 )
			throw new IllegalArgumentException( Language.getText( "replayops.renameDialog.templateEngineError.templateCannotBeEmpty" ) );
		
		this.template     = template;
		templateCharArray = template.toCharArray();
		requiresGameEvent = checkNameTemplate( templateCharArray );
	}
	
	/**
	 * Applies a name template to a replay file.
	 * @param file         replay file to apply to
	 * @param targetFolder target folder of the replay
	 * @return the result of the template applied to the replay file; or null if the replay cannot be parsed but the template requires it 
	 */
	public String applyToReplay( final File file, final File targetFolder ) {
		this.file         = file;
		targetFolderWithSubfolders = this.targetFolder = targetFolder;
		replay            = null;
		replayParseError  = false;
		
		nameBuilder.setLength( 0 );
		applyToReplay_( templateCharArray );
		
		if ( replayParseError )
			return null;
		else {
			// Replace characters that are not allowed in file names: (required because map names may contain * for example!)
			for ( int i = nameBuilder.length() - 1; i >= 0; i-- )
				if ( NOT_ALLOWED_FILE_NAME_CHAR_SET.contains( nameBuilder.charAt( i ) ) )
					nameBuilder.setCharAt( i, '_' );
			
			final String newName = nameBuilder.toString();
			return newName.toLowerCase().endsWith( ".sc2replay" ) ? newName : newName + ".SC2Replay";		
		}
	}
	
	/**
	 * Applies a name template to a replay file.
	 * @param templateCharArray char array of template to be applied
	 */
	private void applyToReplay_( final char[] templateCharArray ) {
		// Many checks are omitted which are handled in the checkNameTemplate().
		// If error is detected, the constructor throws an exception...
		
		final int length = templateCharArray.length;
		for ( int i = 0; i < length; i++ ) {
			if ( templateCharArray[ i ] == '/' ) {
				final char   templateChar = templateCharArray[ ++i ];
				final Symbol symbol       = CHAR_SYMBOL_MAP.get( templateChar );
				
				switch ( symbol ) {
				case ORIGINAL_NAME : nameBuilder.append( GeneralUtils.getFileNameWithoutExt( file ) ); break;
				case EXTENSION : nameBuilder.append( "SC2Replay" ); break;
				case MD5 : nameBuilder.append( "MD5_" + ReplayCache.encodeMd5( GeneralUtils.calculateFileMd5( file ) ) ); break;
				case COUNTER : nameBuilder.append( Integer.toString( counter++ ) ); break;
				case COUNTER2 : case REPLAY_COUNTER : {
					final int count = templateCharArray[ ++i ] - '0';
					switch ( symbol ) {
					case COUNTER2 : nameBuilder.append( String.format( "%0" + count + "d", counter2++ ) ); break;
					case REPLAY_COUNTER : {
						final File[] replayFiles = targetFolderWithSubfolders.listFiles( GeneralUtils.SC2REPLAY_FILES_ONLY_FILTER );
						nameBuilder.append( String.format( "%0" + count + "d", ( replayFiles == null ? 0 : replayFiles.length ) + 1 ) ); break;
					}
					}
					break;
				}
				case MAP_NAME : case MAP_FIRST_WORDS : case MAP_FIRST_LETTERS : case MAP_NAME_ACRONYM : case PLAYER_NAME : case ALL_PLAYER_NAMES : case ALL_PLAYER_NAMES_GROUPED : case WINNERS : case RACE_FIRST_LETTER : case RACE : case LEAGUE : case FORMAT : case MATCHUP : case DATE : case DATE_TIME : case GAME_LENGTH : case GAME_LENGTH_LONG : case LADDER_SEASON : case GAME_TYPE : case GATEWAY : case APM : case EAPM : case RESULT : case REPLAY_VERSION : case BUILD_NUMBER : {
					if ( replay == null )
						parseReplay();
					if ( replay == null ) {
						System.out.println( "Could not parse replay!" );
						replayParseError = true;
						return;
					}
					final int[] teamOrderPlayerIndices = replay.details.getTeamOrderPlayerIndices();
					switch ( symbol ) {
					case DATE : nameBuilder.append( Language.formatDate( new Date( replay.details.saveTime ) ).replace( '/', '-' ).replace( '\\', '-' ).replace( ':', '-' ) ); break;
					case DATE_TIME : nameBuilder.append( Language.formatDateTime( new Date( replay.details.saveTime ) ).replace( '/', '-' ).replace( '\\', '-' ).replace( ':', '-' ) ); break;
					case GAME_LENGTH : nameBuilder.append( ReplayUtils.formatFramesShort( replay.gameLength << ( ReplayConsts.FRAME_BITS_IN_SECOND-1 ), replay.converterGameSpeed ).replace( ':', '-' ) ); break;
					case GAME_LENGTH_LONG : nameBuilder.append( ReplayUtils.formatMs( replay.gameLength * 500, replay.converterGameSpeed ).replace( ':', '-' ) ); break;
					case LADDER_SEASON : nameBuilder.append( LadderSeason.getByDate( new Date( replay.details.saveTime ), replay.initData.gateway ).seasonNumber ); break;
					case REPLAY_VERSION : nameBuilder.append( replay.buildNumbers[ 0 ] ).append( '.' ).append( replay.buildNumbers[ 1 ] ).append( '.' ).append( replay.buildNumbers[ 2 ] ); break;
					case BUILD_NUMBER : nameBuilder.append( replay.buildNumbers[ 3 ] ); break;
					case GAME_TYPE : nameBuilder.append( replay.initData.gameType ); break;
					case GATEWAY : nameBuilder.append( replay.initData.gateway ); break;
					case MAP_NAME : nameBuilder.append( replay.details.mapName ); break;
					case ALL_PLAYER_NAMES : nameBuilder.append( replay.details.getPlayerNames() ); break;
					case ALL_PLAYER_NAMES_GROUPED : nameBuilder.append( replay.details.getPlayerNamesGrouped() ); break;
					case WINNERS : nameBuilder.append( replay.details.getWinnerNames() ); break;
					case FORMAT : nameBuilder.append( replay.initData.format ); break;
					case MATCHUP : nameBuilder.append( replay.details.getRaceMatchup() ); break;
					case MAP_NAME_ACRONYM : {
						final StringTokenizer mapNameTokenizer = new StringTokenizer( replay.details.mapName );
						while ( mapNameTokenizer.hasMoreTokens() )
							nameBuilder.append( mapNameTokenizer.nextToken().charAt( 0 ) );
						break;
					}
					case PLAYER_NAME : case RACE : case RACE_FIRST_LETTER : case LEAGUE : case APM : case EAPM : case RESULT : {
						final int index = templateCharArray[ ++i ] - '1';
						if ( index >= replay.details.players.length )
							continue;
						switch ( symbol ) {
						case PLAYER_NAME : nameBuilder.append( replay.details.players[ teamOrderPlayerIndices[ index ] ].playerId.name ); break;
						case RACE : nameBuilder.append( replay.details.players[ teamOrderPlayerIndices[ index ] ].finalRace ); break;
						case RACE_FIRST_LETTER : nameBuilder.append( replay.details.players[ teamOrderPlayerIndices[ index ] ].getRaceLetter() ); break;
						case LEAGUE : nameBuilder.append( replay.details.players[ teamOrderPlayerIndices[ index ] ].getLeague().letter ); break;
						case APM : nameBuilder.append( ReplayUtils.calculatePlayerApm( replay, replay.details.players[ teamOrderPlayerIndices[ index ] ] ) ); break;
						case EAPM : nameBuilder.append( ReplayUtils.calculatePlayerEapm( replay, replay.details.players[ teamOrderPlayerIndices[ index ] ] ) ); break;
						case RESULT : {
							final Boolean isWinner = replay.details.players[ teamOrderPlayerIndices[ index ] ].isWinner;
							nameBuilder.append( isWinner == null ? MatchResult.UNKNOWN.letter : isWinner ? MatchResult.WIN.letter : MatchResult.LOSS.letter ); break;
						}
						}
						break;
					}
					case MAP_FIRST_WORDS : {
						final int count = templateCharArray[ ++i ] - '0';
						{
							final StringTokenizer mapNameTokenizer = new StringTokenizer( replay.details.mapName );
							for ( int j = 0; j < count && mapNameTokenizer.hasMoreElements(); j++ ) {
								final String word = mapNameTokenizer.nextToken();
								if ( j > 0 )
									nameBuilder.append( ' ' );
								else
									// Skip the first word if it's "The" or "A"
									if ( "The".equalsIgnoreCase( word ) || "A".equalsIgnoreCase( word ) ) {
										j--;
										continue;
									}
								nameBuilder.append( word );
							}
						}
						break;
					}
					case MAP_FIRST_LETTERS : {
						final int count1 = templateCharArray[ ++i ] - '0';
						final int count2 = templateCharArray[ ++i ] - '0';
						final int count = count1 * 10 + count2;
						nameBuilder.append( replay.details.mapName.substring( 0, Math.min( count, replay.details.mapName.length() ) ) );
						break;
					}
					}
					break;
				}
				}
			}
			else if ( templateCharArray[ i ] == Symbol.SUBFOLDER_SEPARATOR.symbolChar ) {
				targetFolderWithSubfolders = new File( targetFolder, nameBuilder.toString() );
				nameBuilder.append( File.separatorChar );
			}
			else if ( templateCharArray[ i ] == Symbol.PLAYER_INFO_BLOCK.symbolChar ) {
				i++;
				final int blockEnd = template.indexOf( '>', i );
				final String playerInfoBlockTemplate = template.substring( i, blockEnd );
				if ( replay == null )
					parseReplay();
				if ( replay == null ) {
					System.out.println( "Could not parse replay!" );
					replayParseError = true;
					return;
				}
				// Iterate over players
				final int[] teamOrderPlayerIndices = replay.details.getTeamOrderPlayerIndices();
				if ( teamOrderPlayerIndices.length > 0 ) {
					int lastTeam = replay.details.players[ teamOrderPlayerIndices[ 0 ] ].team;
					boolean firstPlayerInTeam = true;
					for ( int j = 0; j < teamOrderPlayerIndices.length; j++ ) {
						final Player player = replay.details.players[ teamOrderPlayerIndices[ j ] ];
						if ( player.team != lastTeam ) {
							nameBuilder.append( " vs " );
							lastTeam = player.team;
							firstPlayerInTeam = true;
						}
						if ( firstPlayerInTeam )
							firstPlayerInTeam = false;
						else
							nameBuilder.append( ", " );
						// Make new template by substitution
						String modifiedBlockTemplate = playerInfoBlockTemplate;
						for ( final Symbol symbol : PLAYER_INFO_SYMBOLS )
							modifiedBlockTemplate = modifiedBlockTemplate.replace( symbol.shortFragment, symbol.shortFragment + ( j + 1 ) );
						applyToReplay_( modifiedBlockTemplate.toCharArray() );
					}
				}
				
				i = blockEnd;
			}
			else
				nameBuilder.append( templateCharArray[ i ] );
		}
	}
	
	/**
	 * Parses the replay.
	 */
	private void parseReplay() {
		if ( requiresGameEvent )
			replay = ReplayCache.getReplay( file, null, null, cacheEnabled, cacheEnabled, ReplayFactory.GENERAL_DATA_CONTENT );
		else
			replay = ReplayCache.getReplay( file, null, null, cacheEnabled, false, ReplayFactory.GENERAL_INFO_CONTENT );
		
		// Rearrange the players
		if ( replay != null )
			ReplayUtils.applyFavoredPlayerListSetting( replay.details );
	}
	
	/**
	 * Explains the template.
	 * @return a string explaining the template
	 */
	public String explain() {
		nameBuilder.setLength( 0 );
		explain_( templateCharArray );
		
		return nameBuilder.toString();
	}
	
	/**
	 * Explains the template. This is the implementation.
	 * @param templateCharArray char array of template to be applied
	 */
	private void explain_( final char[] templateCharArray ) {
		// Many checks are omitted which are handled in the checkNameTemplate().
		// If error is detected, the constructor throws an exception...
		
		final int length = templateCharArray.length;
		for ( int i = 0; i < length; i++ ) {
			if ( templateCharArray[ i ] == '/' ) {
				final char   templateChar = templateCharArray[ ++i ];
				final Symbol symbol       = CHAR_SYMBOL_MAP.get( templateChar );
				
				switch ( symbol ) {
				case ORIGINAL_NAME : case EXTENSION : case MD5 : case COUNTER : case DATE : case DATE_TIME : case GAME_LENGTH : case GAME_LENGTH_LONG : case LADDER_SEASON : case REPLAY_VERSION : case BUILD_NUMBER : case GAME_TYPE : case GATEWAY : case MAP_NAME : case ALL_PLAYER_NAMES : case ALL_PLAYER_NAMES_GROUPED : case WINNERS : case FORMAT : case MATCHUP : case MAP_NAME_ACRONYM : case SUBFOLDER_SEPARATOR :
					nameBuilder.append( Language.getText( symbol.explainedTextKey ) ); break;
				case COUNTER2 : case REPLAY_COUNTER : case PLAYER_NAME : case RACE : case RACE_FIRST_LETTER : case LEAGUE : case APM : case EAPM : case RESULT : case MAP_FIRST_WORDS : {
					final int param = templateCharArray[ ++i ] - '0';
					nameBuilder.append( Language.getText( symbol.explainedTextKey, param ) ); break;
				}
				case MAP_FIRST_LETTERS : {
					final int count1 = templateCharArray[ ++i ] - '0';
					final int count2 = templateCharArray[ ++i ] - '0';
					final int count = count1 * 10 + count2;
					nameBuilder.append( Language.getText( symbol.explainedTextKey, count ) ); break;
				}
				}
			}
			else if ( templateCharArray[ i ] == Symbol.PLAYER_INFO_BLOCK.symbolChar ) {
				i++;
				final int blockEnd = template.indexOf( '>', i );
				final String playerInfoBlockTemplate = template.substring( i, blockEnd );
				// Iterate over players (2 in case of explain)
				for ( int j = 0; j < 2; j++ ) {
					if ( j > 0 )
						nameBuilder.append( " vs " );
					// Make new template by substitution
					String modifiedBlockTemplate = playerInfoBlockTemplate;
					for ( final Symbol symbol : PLAYER_INFO_SYMBOLS )
						modifiedBlockTemplate = modifiedBlockTemplate.replace( symbol.shortFragment, symbol.shortFragment + ( j + 1 ) );
					explain_( modifiedBlockTemplate.toCharArray() );
				}
				
				i = blockEnd;
			}
			else
				nameBuilder.append( templateCharArray[ i ] );
		}
	}
	
	/**
	 * Checks a name template if it's valid.
	 * @param templateCharArray char array of the template to be checked
	 * @return true if applying the name template requires parsing game events too; false otherwise
	 * @throws Exception if the template is invalid
	 */
	private boolean checkNameTemplate( final char[] templateCharArray ) throws Exception {
		boolean requiresGameEvent = false;
		
		int index;
		final int length = templateCharArray.length;
		for ( int i = 0; i < length; i++ ) {
			if ( templateCharArray[ i ] == '/' ) {
				if ( i == length-1 )
        			throw new Exception( Language.getText( "replayops.renameDialog.templateEngineError.missingSymbolCharAtEnd" ) );
				
				final char   templateChar = templateCharArray[ ++i ];
				final Symbol symbol       = CHAR_SYMBOL_MAP.get( templateChar );
        		if ( symbol == null ) 
        			throw new Exception( Language.getText( "replayops.renameDialog.templateEngineError.invalidSymbol", templateChar ) );
        		
				switch ( symbol ) {
				case ORIGINAL_NAME : case MAP_NAME_ACRONYM : case EXTENSION : case MD5 : case COUNTER : case DATE : case DATE_TIME : case GAME_LENGTH : case GAME_LENGTH_LONG : case LADDER_SEASON : case REPLAY_VERSION : case BUILD_NUMBER : case GAME_TYPE : case GATEWAY : case MAP_NAME : case ALL_PLAYER_NAMES : case ALL_PLAYER_NAMES_GROUPED : case WINNERS : case FORMAT : case MATCHUP :
					break;
				case PLAYER_NAME : case RACE : case RACE_FIRST_LETTER : case LEAGUE : case APM : case EAPM : case RESULT :
					requiresGameEvent |= symbol == Symbol.APM || symbol == Symbol.EAPM || symbol == Symbol.RESULT;
					if ( i == length-1 )
	        			throw new Exception( Language.getText( "replayops.renameDialog.templateEngineError.incompleteSymbolAtEnd" ) );
					index = templateCharArray[ ++i ] - '1';
					if ( index < 0 || index > 7 )
	        			throw new Exception( Language.getText( "replayops.renameDialog.templateEngineError.invalidPlayerNumber", templateCharArray[ i ] ) );
					break;
				 case MAP_FIRST_WORDS : case COUNTER2 : case REPLAY_COUNTER :
					if ( i == length-1 )
	        			throw new Exception( Language.getText( "replayops.renameDialog.templateEngineError.incompleteSymbolAtEnd" ) );
					index = templateCharArray[ ++i ] - '0';
					if ( index < 1 || index > 9 )
						throw new Exception( Language.getText( symbol == Symbol.MAP_FIRST_WORDS ? "replayops.renameDialog.templateEngineError.invalidWordCount" : "replayops.renameDialog.templateEngineError.invalidDigitCount", templateCharArray[ i ] ) );
					break;
				case MAP_FIRST_LETTERS : {
					if ( i == length-1 )
	        			throw new Exception( Language.getText( "replayops.renameDialog.templateEngineError.incompleteSymbolAtEnd" ) );
					final int count1 = templateCharArray[ ++i ] - '0';
					if ( i == length-1 )
	        			throw new Exception( Language.getText( "replayops.renameDialog.templateEngineError.incompleteSymbolAtEnd" ) );
					final int count2 = templateCharArray[ ++i ] - '0';
					if ( count1 < 0 || count1 > 9 || count2 < 0 || count2 > 9 )
	        			throw new Exception( Language.getText( "replayops.renameDialog.templateEngineError.invalidLetterCount", templateCharArray[ i-1 ] + "" + templateCharArray[ i ] ) );
					break;
				}
				}
			}
			else if ( templateCharArray[ i ] == '>' )
				throw new Exception( Language.getText( "replayops.renameDialog.templateEngineError.invalidPlayerInfoBlockClosing" ) );
			else if ( templateCharArray[ i ] == Symbol.PLAYER_INFO_BLOCK.symbolChar ) {
				i++;
				final int blockEnd = template.indexOf( '>', i );
				if ( blockEnd < 0 )
					throw new Exception( Language.getText( "replayops.renameDialog.templateEngineError.playerInfoBlockNotClosedProperly" ) );
				else {
					String playerInfoBlockTemplate = template.substring( i, blockEnd );
					if ( playerInfoBlockTemplate.indexOf( Symbol.PLAYER_INFO_BLOCK.symbolChar ) >= 0 )
						throw new Exception( Language.getText( "replayops.renameDialog.templateEngineError.playerInfoBlockCannotBeEmbedded" ) );
					String modifiedBlockTemplate = playerInfoBlockTemplate;
					for ( final Symbol symbol : PLAYER_INFO_SYMBOLS )
						modifiedBlockTemplate = modifiedBlockTemplate.replace( symbol.shortFragment, symbol.shortFragment + 1 );
					requiresGameEvent |= checkNameTemplate( modifiedBlockTemplate.toCharArray() );
				}
				i = blockEnd;
			}
			else if ( NOT_ALLOWED_FILE_NAME_CHAR_SET.contains( templateCharArray[ i ] ) )
				throw new Exception( Language.getText( "replayops.renameDialog.templateEngineError.notAllowedCharInFileName", templateCharArray[ i ] ) );
		}
		
		return requiresGameEvent;
	}
	
}
