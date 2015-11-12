/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb;

import hu.belicza.andras.sc2gearsdbapi.ServletApi;

/**
 * API of the {@link ParsingServlet}.
 * 
 * @author Andras Belicza
 */
public class ParsingServletApi extends ServletApi {
	
	/**
	 * Interface describing an API call result. 
	 * @author Andras Belicza
	 */
	public static interface IResult {
		/**
		 * Returns the result code.
		 * @return the result code
		 */
		int getCode();
		/**
		 * Returns the result message.
		 * @return the result message
		 */
		String getMessage();
	}
	
	/** Protocol version 1. */
	public static final String PROTOCOL_VERSION_1 = "1";
	
	// Operations
	
	/** Info operation.         */
	public static final String OPERATION_INFO              = "info";
	/** Map info operation.     */
	public static final String OPERATION_MAP_INFO          = "mapInfo";
	/** Parse replay operation. */
	public static final String OPERATION_PARSE_REPLAY      = "parseRep";
	/** Profile info operation. */
	public static final String OPERATION_PROFILE_INFO      = "profInfo";
	
	
	// Common parameters
	
	/** API key parameter.                                */
	public static final String PARAM_API_KEY               = "apiKey";
	
	// Info operation parameters
	/** Days count parameter.                             */
	public static final String PARAM_DAYS_COUNT            = "daysCount";
	
	// Map info operation parameters
	/** Map file name parameter.                          */
	public static final String PARAM_MAP_FILE_NAME         = "mapFileName";
	
	// Parse replay operation parameters
	/** File content (in Base-64 encoded form) parameter. */
	public static final String PARAM_FILE_CONTENT          = "fileContent";
	/** File length parameter.                            */
	public static final String PARAM_FILE_LENGTH           = "fileLength";
	/** Parse chat parameter.                             */
	public static final String PARAM_PARSE_MESSAGES        = "parseMessages";
	/** Parse chat parameter.                             */
	public static final String PARAM_PARSE_ACTIONS         = "parseActions";
	/** Send Select actions parameter.                    */
	public static final String PARAM_SEND_ACTIONS_SELECT   = "sendActionsSelect";
	/** Send Build actions parameter.                     */
	public static final String PARAM_SEND_ACTIONS_BUILD    = "sendActionsBuild";
	/** Send Train actions parameter.                     */
	public static final String PARAM_SEND_ACTIONS_TRAIN    = "sendActionsTrain";
	/** Send Research actions parameter.                  */
	public static final String PARAM_SEND_ACTIONS_RESEARCH = "sendActionsResearch";
	/** Send Upgrade actions parameter.                   */
	public static final String PARAM_SEND_ACTIONS_UPGRADE  = "sendActionsUpgrade";
	/** Send Other actions parameter.                     */
	public static final String PARAM_SEND_ACTIONS_OTHER    = "sendActionsOther";
	/** Send Inaction actions parameter.                  */
	public static final String PARAM_SEND_ACTIONS_INACTION = "sendActionsInaction";
	
	// Profile info operation parameters
	/** Battle.net profile URL parameter.                 */
	public static final String PARAM_BNET_PROFILE_URL      = "bnetProfileUrl";
	/** Battle.net id parameter.                          */
	public static final String PARAM_BNET_ID               = "bnetId";
	/** Battle.net sub-id parameter.                      */
	public static final String PARAM_BNET_SUBID            = "bnetSubid";
	/** Gateway parameter.                                */
	public static final String PARAM_GATEWAY               = "gateway";
	/** Player name parameter.                            */
	public static final String PARAM_PLAYER_NAME           = "playerName";
	/** Retrieve extended profile info parameter.         */
	public static final String PARAM_RETRIEVE_EXT_INFO     = "retrieveExtInf";
	
	
	
	// Common XML constants
	
	public static final String XTAG_RESPONSE     = "response";
	public static final String XTAG_RESULT       = "result";
	public static final String XTAG_ENGINE_VER   = "engineVer";
	public static final String XTAG_PLAYER_ID    = "playerId";
	
	public static final String XATTR_DOC_VERSION = "docVer";
	public static final String XATTR_CODE        = "code";
	public static final String XATTR_VALUE       = "value";
	public static final String XATTR_FORMAT      = "format";
	public static final String XATTR_UNIT        = "unit";
	public static final String XATTR_COUNT       = "count";
	public static final String XATTR_NAME        = "name";
	public static final String XATTR_BNET_ID     = "bnetId";
	public static final String XATTR_BNET_SUBID  = "bnetSubid";
	public static final String XATTR_GATEWAY     = "gateway";
	public static final String XATTR_GW_CODE     = "gwCode";
	public static final String XATTR_REGION      = "region";
	public static final String XATTR_PROFILE_URL = "profileUrl";
	
	// Info operation XML constants
	
	public static final String XTAG_SERVER_TIME             = "serverTime";
	
	public static final String XTAG_PAID_OPS                = "paidOps";
	public static final String XTAG_AVAIL_OPS               = "availOps";
	
	public static final String XTAG_CALL_STATS              = "callStats";
	public static final String XTAG_CALL_STAT               = "callStat";
	public static final String XTAG_API_CALLS               = "apiCalls";
	public static final String XTAG_USED_OPS                = "usedOps";
	public static final String XTAG_AVG_EXEC_TIME           = "avgExecTime";
	public static final String XTAG_DENIED_CALLS            = "deniedCalls";
	public static final String XTAG_ERRORS                  = "errors";
	public static final String XTAG_INFO_CALLS              = "infoCalls";
	public static final String XTAG_AVG_INFO_EXEC_TIME      = "avgInfoExecTime";
	public static final String XTAG_MAP_INFO_CALLS          = "mapInfoCalls";
	public static final String XTAG_AVG_MAP_INFO_EXEC_TIME  = "avgMapInfoExecTime";
	public static final String XTAG_PARSE_REP_CALLS         = "parseRepCalls";
	public static final String XTAG_AVG_PARSE_REP_EXEC_TIME = "avgParseRepExecTime";
	public static final String XTAG_PROF_INFO_CALLS         = "profInfoCalls";
	public static final String XTAG_AVG_PROF_INFO_EXEC_TIME = "avgProfInfoExecTime";
	
	public static final String XATTR_DAY                    = "day";
	public static final String XATTR_PATTERN                = "pattern";
	
	/**
	 * Info result: result code and message of the info op API call.
	 * @author Andras Belicza
	 */
	public static enum InfoResult implements IResult {
		OK( 0, "OK" );
		
		public final int    code;
		public final String message;
		
		private InfoResult( final int code, final String message ) {
			this.code    = code;
			this.message = message;
		}
		
		@Override
		public int getCode() {
			return code;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
	}
	
	// Map info operation XML constants
	
	public static final String XTAG_MAP       = "map";
	public static final String XTAG_MAP_IMAGE = "mapImage";
	
	public static final String XATTR_WIDTH    = "width";
	public static final String XATTR_HEIGHT   = "height";
	public static final String XATTR_SIZE     = "size";
	
	/**
	 * Map info result: result code and message of the map info op API call.
	 * @author Andras Belicza
	 */
	public static enum MapInfoResult implements IResult {
		OK            ( 0, "OK"                                           ),
		PROCESSING    ( 1, "Map is being processed."                      ),
		DOWNLOAD_ERROR( 2, "Could not acquire map file."                  ),
		PARSING_ERROR ( 3, "Could not parse map file (non-standard map)." );
		
		public final int    code;
		public final String message;
		
		private MapInfoResult( final int code, final String message ) {
			this.code    = code;
			this.message = message;
		}
		
		@Override
		public int getCode() {
			return code;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
	}
	
	// Replay parsing operation XML constants
	
	public static final String XTAG_REP_INFO                = "repInfo";
	public static final String XTAG_VERSION                 = "version";
	public static final String XTAG_GAME_LENGTH             = "gameLength";
	public static final String XTAG_EXPANSION               = "expansion";
	public static final String XTAG_GAME_TYPE               = "gameType";
	public static final String XTAG_IS_COMPETITIVE          = "isCompetitive";
	public static final String XTAG_GAME_SPEED              = "gameSpeed";
	public static final String XTAG_FORMAT                  = "format";
	public static final String XTAG_GATEWAY                 = "gateway";
	public static final String XTAG_MAP_FILE_NAME           = "mapFile";
	public static final String XTAG_CLIENTS                 = "clients";
	public static final String XTAG_CLIENT                  = "client";
	public static final String XTAG_MAP_NAME                = "mapName";
	public static final String XTAG_SAVE_TIME               = "saveTime";
	public static final String XTAG_SAVE_TIME_ZONE          = "saveTimeZone";
	public static final String XTAG_PLAYERS                 = "players";
	public static final String XTAG_PLAYER                  = "player";
	public static final String XTAG_TEAM                    = "team";
	public static final String XTAG_RACE                    = "race";
	public static final String XTAG_FINAL_RACE              = "finalRace";
	public static final String XTAG_LEAGUE                  = "league";
	public static final String XTAG_SWARM_LEVELS            = "swarmLevels";
	public static final String XTAG_COLOR                   = "color";
	public static final String XTAG_TYPE                    = "type";
	public static final String XTAG_DIFFICULTY              = "difficulty";
	public static final String XTAG_HANDICAP                = "handicap";
	public static final String XTAG_IS_WINNER               = "isWinner";
	public static final String XTAG_ACTIONS_COUNT           = "actionsCount";
	public static final String XTAG_EFFECTIVE_ACTIONS_COUNT = "effectiveActionsCount";
	public static final String XTAG_LAST_ACTION_FRAME       = "lastActionFrame";
	public static final String XTAG_APM                     = "apm";
	public static final String XTAG_EAPM                    = "eapm";
	public static final String XTAG_AVG_SPAWNING_RATIO      = "avgSpawningRatio";
	public static final String XTAG_AVG_INJECTION_GAP       = "avgInjectionGap";
	public static final String XTAG_IN_GAME_CHAT            = "inGameChat";
	public static final String XTAG_TEXT                    = "text";
	public static final String XTAG_PING                    = "ping";
	public static final String XTAG_ACTIONS                 = "actions";
	public static final String XTAG_ACTION_                 = "a";
	
	public static final String XATTR_GAME_TIME_VALUE        = "gameTimeValue";
	public static final String XATTR_INDEX                  = "index";
	public static final String XATTR_RED                    = "red";
	public static final String XATTR_GREEN                  = "green";
	public static final String XATTR_BLUE                   = "blue";
	public static final String XATTR_EXCLUDED_ACTIONS_COUNT = "excludedActionsCount";
	public static final String XATTR_TIME                   = "time";
	public static final String XATTR_CLIENT_INDEX           = "clientIndex";
	public static final String XATTR_CLIENT                 = "client";
	public static final String XATTR_TARGET                 = "target";
	public static final String XATTR_X                      = "x";
	public static final String XATTR_Y                      = "y";
	public static final String XATTR_ALL_ACTIONS_COUNT      = "allActionsCount";
	public static final String XATTR_ERROR_PARSING          = "errorParsing";
	public static final String XATTR_PLAYER_                = "p";
	public static final String XATTR_TYPE_                  = "t";
	public static final String XATTR_FRAME_                 = "f";
	public static final String XATTR_STRING_                = "s";
	
	/**
	 * Parse replay result: result code and message of the parse replay op API call.
	 * @author Andras Belicza
	 */
	public static enum ParseRepResult implements IResult {
		OK            ( 0, "OK"                           ),
		PARSING_ERROR ( 1, "Could not parse replay file." );
		
		public final int    code;
		public final String message;
		
		private ParseRepResult( final int code, final String message ) {
			this.code    = code;
			this.message = message;
		}
		
		@Override
		public int getCode() {
			return code;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
	}
	
	// Profile info operation XML constants
	
	public static final String XTAG_PROFILE_INFO       = "profInfo";
	public static final String XTAG_PORTRAIT           = "portrait";
	public static final String XTAG_ACHIEVEMENT_POINTS = "achievementPoints";
	public static final String XTAG_TOTAL_CAREER_GAMES = "totalCareerGames";
	public static final String XTAG_GAMES_THIS_SEASON  = "gamesThisSeason";
	public static final String XTAG_TERRAN_WINS        = "terranWins";
	public static final String XTAG_ZERG_WINS          = "zergWins";
	public static final String XTAG_PROTOSS_WINS       = "protossWins";
	public static final String XTAG_HIGHEST_SOLO_FL    = "highestSoloFinishLeague";
	public static final String XTAG_HIGHEST_TEAM_FL    = "highestTeamFinishLeague";
	
	public static final String XTAG_TEAM_RANK          = "teamRank";
	public static final String XTAG_ALL_RANK_GROUPS    = "allRankGroups";
	public static final String XTAG_ALL_RANK_GROUP     = "allRankGroup";
	public static final String XTAG_TEAM_MEMBERS       = "teamMembers";
	public static final String XTAG_TEAM_MEMBER        = "teamMember";
	
	public static final String XATTR_GROUP          = "group";
	public static final String XATTR_ROW            = "row";
	public static final String XATTR_COLUMN         = "column";
	public static final String XATTR_LEAGUE         = "league";
	public static final String XATTR_DIVISION_RANK  = "divisionRank";
	public static final String XATTR_TIMES_ACHIEVED = "timesAchieved";
	
	/**
	 * Profile info result: result code and message of the profile info op API call.
	 * @author Andras Belicza
	 */
	public static enum ProfInfoResult implements IResult {
		OK            ( 0, "OK"                                    ),
		BNET_ERROR    ( 1, "Could not connect to Battle.net."      ),
		INVALID_PLAYER( 2, "Invalid player specified."             ),
		PARSING_ERROR ( 3, "Could not parse data from Battle.net." );
		
		public final int    code;
		public final String message;
		
		private ProfInfoResult( final int code, final String message ) {
			this.code    = code;
			this.message = message;
		}
		
		@Override
		public int getCode() {
			return code;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
	}
	
}
