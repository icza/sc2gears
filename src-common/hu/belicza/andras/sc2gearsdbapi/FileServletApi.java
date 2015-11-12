/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdbapi;

import hu.belicza.andras.sc2gearsdb.FileServlet;

/**
 * API of the {@link FileServlet}.
 * 
 * @author Andras Belicza
 */
public class FileServletApi extends ServletApi {
	
	/** Shared account parameter.                         */
	public static final String PARAM_SHARED_ACCOUNT         = "sharedAccount";
	
	/** SHA-1 of the file content parameter.              */
	public static final String PARAM_SHA1                   = "sha1";
	/** List of SHA-1 of the file contents parameter.     */
	public static final String PARAM_SHA1_LIST              = "sha1List";
	/** File name parameter.                              */
	public static final String PARAM_FILE_NAME              = "fileName";
	/** Last modified parameter.                          */
	public static final String PARAM_LAST_MODIFIED          = "lastMod";
	/** File type parameter.                              */
	public static final String PARAM_FILE_TYPE              = "fileType";
	/** File content (in Base-64 encoded form) parameter. */
	public static final String PARAM_FILE_CONTENT           = "fileContent";
	
	// Common parameters
	/** Version parameter.                                */
	public static final String PARAM_VERSION                = "version";
	
	// Replay parameters
	/** Replay date parameter.                            */
	public static final String PARAM_REPLAY_DATE            = "repDate";
	/** Game length parameter.                            */
	public static final String PARAM_GAME_LENGTH            = "gameLength";
	/** Gateway parameter.                                */
	public static final String PARAM_GATEWAY                = "gateway";
	/** Game type parameter.                              */
	public static final String PARAM_GAME_TYPE              = "gameType";
	/** Map name parameter.                               */
	public static final String PARAM_MAP_NAME               = "mapName";
	/** Map file name parameter.                          */
	public static final String PARAM_MAP_FILE_NAME          = "mapFileName";
	/** Format parameter.                                 */
	public static final String PARAM_FORMAT                 = "format";
	/** League match-up parameter.                        */
	public static final String PARAM_LEAGUE_MATCHUP         = "leagueMtchp";
	/** Race match-up parameter.                          */
	public static final String PARAM_RACE_MATCHUP           = "raceMtchp";
	/** Players parameter.                                */
	public static final String PARAM_PLAYERS                = "players";
	/** Teams of players parameter.                                */
	public static final String PARAM_PLAYER_TEAMS           = "playerTeams";
	/** Winners parameter.                                */
	public static final String PARAM_WINNERS                = "winners";
	
	// Mouse print parameters
	/** Recording start parameter.                        */
	public static final String PARAM_RECORDING_START        = "recStart";
	/** Recording end parameter.                          */
	public static final String PARAM_RECORDING_END          = "recEnd";
	/** Screen width parameter.                           */
	public static final String PARAM_SCREEN_WIDTH           = "scrWidth";
	/** Screen height parameter.                          */
	public static final String PARAM_SCREEN_HEIGHT          = "scrHeight";
	/** Screen resolution parameter.                      */
	public static final String PARAM_SCREEN_RESOLUTION      = "scrRes";
	/** Sampling time parameter.                          */
	public static final String PARAM_SAMPLING_TIME          = "samplingTime";
	/** Samples count parameter.                          */
	public static final String PARAM_SAMPLES_COUNT          = "samplesCount";
	/** Uncompressed data size parameter.                 */
	public static final String PARAM_UNCOMPRESSED_DATA_SIZE = "uncmpDataSize";
	/** Saved with compression parameter.                 */
	public static final String PARAM_SAVED_WITH_COMPRESSION = "savedWithComp";
	
	// Other file parameters
	/** Recording start parameter.                        */
	public static final String PARAM_COMMENT                = "comment";
	
	// Retrieve file list parameters
	/** After date parameter.                             */
	public static final String PARAM_AFTER_DATE             = "afterDate";
	/** Before date parameter.                            */
	public static final String PARAM_BEFORE_DATE            = "beforeDate";
	
	/** File name header field.                           */
	public static final String HEADER_X_FILE_NAME           = "X-Sc2gears-file-name";
	/** File date header field.                           */
	public static final String HEADER_X_FILE_DATE           = "X-Sc2gears-file-date";
	
	/** Protocol version 1. */
	public static final String PROTOCOL_VERSION_1 = "1";
	
	/** Store operation.              */
	public static final String OPERATION_STORE              = "st";
	/** Download operation.           */
	public static final String OPERATION_DOWNLOAD           = "dl";
	/** Batch download operation.     */
	public static final String OPERATION_BATCH_DOWNLOAD     = "bdl";
	/** Retrieve file list operation. */
	public static final String OPERATION_RETRIEVE_FILE_LIST = "rfl";
	
	/**
	 * File types.
	 * @author Andras Belicza
	 */
	public enum FileType {
		/** File type representing all types. */
		ALL        ( "<all>"    , "<all>" ),
		/** SC2Replay file type.              */
		SC2REPLAY  ( "SC2Replay", "rep"   ),
		/** Mouse print file type.            */
		MOUSE_PRINT( "smpd"     , "smpd"  ),
		/** Other file type.                  */
		OTHER      ( "Other"    , "Other" );
		
		/** Long name of the file type.  */
		public final String longName;
		/** Short name of the file type. */
		public final String shortName;
		
        /**
         * Creates a new FileType.
         * @param longName  long name of the file type
         * @param shortName short name of the file type
         */
		private FileType( final String longName, final String shortName ) {
        	this.longName  = longName;
        	this.shortName = shortName;
        }
		
		@Override
		public String toString() {
		    return longName;
		}
		
		/**
		 * Returns the file type whose long name equals to the specified string.
		 * @param fileTypeString file type string to return the file type for
		 * @return the file type whose long name equals to the specified string, <code>null</code> otherwise
		 */
		public static FileType fromString( final String fileTypeString ) {
			for ( final FileType fileType : values() )
				if ( fileType.longName.equals( fileTypeString ) )
					return fileType;
			
			return null;
		}
	}
	
	/**
	 * Returns a trimmed file name.
	 * @param fileName file name to be trimmed
	 * @return a trimmed file name
	 */
	public static String trimFileName( final String fileName ) {
		return trimStringLength( fileName, 500 );
	}
	
	/**
	 * Returns a trimmed map name.
	 * @param mapName map name to be trimmed
	 * @return a trimmed file name
	 */
	public static String trimMapName( final String mapName ) {
		return trimStringLength( mapName, 500 );
	}
	
	/**
	 * Returns a trimmed map file name.
	 * @param mapFileName map name to be trimmed
	 * @return a trimmed file name
	 */
	public static String trimMapFileName( final String mapFileName ) {
		return trimStringLength( mapFileName, 500 );
	}
	
	/**
	 * Returns a trimmed comment.
	 * @param comment comment to be trimmed
	 * @return a trimmed comment
	 */
	public static String trimComment( final String comment ) {
		return trimStringLength( comment, 500 );
	}
	
}
