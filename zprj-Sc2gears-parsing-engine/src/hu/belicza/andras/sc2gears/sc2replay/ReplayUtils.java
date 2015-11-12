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

import static hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.FRAMES_IN_SECOND;
import static hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.FRAME_BITS_IN_SECOND;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.model.Details;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.EntityParams;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.AbilityGroup;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BuildingAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Format;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Research;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Upgrade;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility methods for replays.
 * 
 * @author Andras Belicza
 */
public class ReplayUtils {
	
	/**
	 * Tells the decimal representation of a hexadecimal fraction value in the range of 0..ff mapped to 00..90.
	 * This is a helper table to avoid floating point arithmetics.
	 */
	public static final String[] DECIMAL_TABLE = new String[ 256 ];
	static {
		for ( int i = 0; i < 255; i++ ) {
			final int fraction = Math.round( i * ( 100.0f / 256 ) );
			DECIMAL_TABLE[ i ] = fraction < 10 ? "0" + Integer.toString( fraction ) : Integer.toString( fraction );
		}
		// The rounding method would result in "100" for the last value, manually set it to "99"
		// In this case the integer value of the number should be increased by 1 and fraction digits set to "00",
		// but this solution is much more simple...
		DECIMAL_TABLE[ 255 ] = "99";
	}
	
	/**
	 * An {@link Action} comparator which defines a time (frame) order.
	 */
	public static final Comparator< Action > ACTION_FRAME_COMPARATOR = new Comparator< Action >() {
		@Override
		public int compare( final Action a1, final Action a2 ) {
			return a1.frame - a2.frame;
		}
	};
	
	/**
	 * A {@link Unit} comparator which defines a Tier reversed order.<br>
	 * In case of identical tier units the comparator reverts to the Unit order.
	 */
	public static final Comparator< Unit > UNIT_TIER_REVERSE_COMPARATOR = new Comparator< Unit >() {
		@Override
		public int compare( final Unit u1, final Unit u2 ) {
			if ( u1.unitTier == null ^ u2.unitTier == null ) // XOR :F
				return u1.unitTier == null ? -1 : 1;
			
			if ( u1.unitTier == null && u2.unitTier == null )
				return u2.compareTo( u1 );
			
			final int tierOrder = u2.unitTier.compareTo( u1.unitTier );
			return tierOrder == 0 ? u2.compareTo( u1 ) : tierOrder;
		}
	};
	
	/**
	 * Calculates the APM of a player
	 * @param replay reference to the replay
	 * @param player reference to the player whose APM to be calculated
	 * @return the calculated APM of a player
	 */
	public static int calculatePlayerApm( final Replay replay, final Player player ) {
		return calculateApm( player.actionsCount - player.excludedActionsCount, replay.converterGameSpeed.convertToRealTime( player.lastActionFrame - replay.excludedInitialFrames ) );
	}
	
	/**
	 * Calculates the EAPM of a player
	 * @param replay reference to the replay
	 * @param player reference to the player whose EAPM to be calculated
	 * @return the calculated EAPM of a player
	 */
	public static int calculatePlayerEapm( final Replay replay, final Player player ) {
		return calculateApm( player.effectiveActionsCount - player.excludedEffectiveActionsCount, replay.converterGameSpeed.convertToRealTime( player.lastActionFrame - replay.excludedInitialFrames ) );
	}
	
	/**
	 * Calculates and returns the APM from the specified actions count and frames
	 * @param actionsCount actions count
	 * @param frames       frames, it must be pre-converted to real-time if intended so
	 * @return the APM for the specified actions count and frames
	 */
	public static int calculateApm( final int actionsCount, final int frames ) {
		if ( frames <= 0 )
			return 0;
		return (int) ( actionsCount * ( 60l << FRAME_BITS_IN_SECOND ) / frames );
	}
	
	/**
	 * Formats the specified amount of frames.
	 * @param frames frames to be formatted
	 * @param gameSpeed game speed to be used for real time conversion
	 * @return the formatted frames
	 */
	public static String formatFrames( final int frames, final GameSpeed gameSpeed ) {
		return formatMs( ( frames * 125 ) >> ( FRAME_BITS_IN_SECOND - 3 ), gameSpeed );
	}
	
	/**
	 * Formats the specified amount of milliseconds.
	 * @param ms milliseconds to be formatted
	 * @param gameSpeed game speed to be used for real time conversion
	 * @return the formatted milliseconds
	 */
	public static String formatMs( int ms, final GameSpeed gameSpeed ) {
		ms = gameSpeed.convertToRealTime( ms );
		final Calendar calendar = new GregorianCalendar( 0, 0, 0, 0, 0, 0 );
		calendar.add( Calendar.MILLISECOND, ms );
		return Language.formatTime( calendar.getTime() ); 
	}
	
	/**
	 * Formats the specified amount of frames to a short format (for example <code>"6:32"</code>).
	 * @param frames    frames to be formatted
	 * @param gameSpeed game speed to be used for real time conversion
	 * @return the formatted frames in short format
	 */
	public static String formatFramesShort( int frames, final GameSpeed gameSpeed ) {
		frames = gameSpeed.convertToRealTime( frames );
		int sec = frames >> FRAME_BITS_IN_SECOND;
		int hour = 0, min = 0;
		
		if ( sec >= 3600 ) {
			hour = sec / 3600;
			sec  = sec % 3600;
		}
		if ( sec >= 60 ) {
			min = sec / 60;
			sec = sec % 60;
		}
		
		return hour > 0 ? hour + ( min < 10 ? ":0" : ":" ) + min + ( sec < 10 ? ":0" : ":" ) + sec
				: min + ( sec < 10 ? ":0" : ":" ) + sec;
	}
	
	/**
	 * Formats the specified amount of frames to a digit decimal format, with 2 fraction digits,
	 * for example <code>"38.63"</code>.
	 * 
	 * <p>The implementation is fast because it does not use format string or floating point arithmetic.</p>
	 * 
	 * @param frames    frames to be formatted
	 * @param gameSpeed game speed to be used for real time conversion
	 * @return the formatted frames in decimal format, with 2 fraction digits
	 */
	public static String formatFramesDecimal( int frames, final GameSpeed gameSpeed ) {
		frames = gameSpeed.convertToRealTime( frames );
		return ( frames >> FRAME_BITS_IN_SECOND ) + "." + DECIMAL_TABLE[ ( frames & ( FRAMES_IN_SECOND - 1 ) ) << ( 8 - FRAME_BITS_IN_SECOND ) ];
	}
	
	/**
	 * Formats a coordinate to a digit decimal format, with 2 fraction digits,
	 * for example <code>"38.63"</code>.
	 * 
	 * <p>The implementation is fast because it does not use format string or floating point arithmetic.</p>
	 * 
	 * @param coord coordinate to be formatted, must be the map location multiplied by 65536
	 * @return the formatted coordinate
	 */
	public static String formatCoordinate( final int coord ) {
		return ( coord >> 16 ) + "." + DECIMAL_TABLE[ ( coord >> 8 ) & 0xff ]; 
	}
	
	/**
	 * Returns the build numbers as a string.
	 * @param buildNumbers build numbers
	 */
	public static String convertBuildNumbersToString( final int[] buildNumbers ) {
		return buildNumbers[ 0 ] + "." + buildNumbers[ 1 ] + "." + buildNumbers[ 2 ] + "." + buildNumbers[ 3 ];
	}
	
	/**
	 * Tries to parse the version from the given text.
	 * @param text text to parse the version from
	 * @return the parsed version or <code>null</code> if text is not a valid version
	 */
	public static int[] parseVersion( final String text ) {
		try {
			final StringTokenizer tokenizer  = new StringTokenizer( text, "." );
			final List< Integer > numberList = new ArrayList< Integer >( 4 );
			
			while ( tokenizer.hasMoreTokens() ) {
				final int n = Integer.parseInt( tokenizer.nextToken() );
				if ( n < 0 )
					return null;
				numberList.add( n );
			}
			
			final int[] version = new int[ numberList.size() ];
			for ( int i = 0; i < version.length; i++ )
				version[ i ] = numberList.get( i );
			
			return version;
		} catch ( final Exception e ) {
			return null;
		}
	}
	
	/**
	 * Compares 2 versions.
	 * 
	 * <p>This method performs a "soft" check: declares <code>"2.0"</code> equal to <code>"2.0.1"</code> for example!</p>
	 * 
	 * @param v1 v1 to be compared
	 * @param v2 v2 to compare to
	 * @return a positive int if v1 > v2, 0 if v1 == v2, a negative int if v1 < v2
	 * 
	 * @see #strictCompareVersions(int[], int[])
	 */
	public static int compareVersions( final int[] v1, final int[] v2 ) {
		for ( int i = 0; i < v1.length && i < v2.length; i++ )
			if ( v1[ i ] != v2[ i ] )
				return v1[ i ] - v2[ i ];
		
		// Skip the rest, let for example 0.6 be equal to 0.6.0.14259
		return 0;
	}
	
	/**
	 * Compares 2 versions.
	 * 
	 * <p>This method performs a "hard" check: declares <code>"2.0"</code> less than <code>"2.0.1"</code> for example!</p>
	 * 
	 * @param v1 v1 to be compared
	 * @param v2 v2 to compare to
	 * @return a positive int if v1 > v2, 0 if v1 == v2, a negative int if v1 < v2
	 * 
	 * @see #compareVersions(int[], int[])
	 */
	public static int strictCompareVersions( final int[] v1, final int[] v2 ) {
		for ( int i = 0; i < v1.length && i < v2.length; i++ )
			if ( v1[ i ] != v2[ i ] )
				return v1[ i ] - v2[ i ];
		
		// If any of the versions has more components, that one is the greater
		return v1.length - v2.length;
	}
	
    /**
     * Guesses the format from the specified race match-up. 
     * @param raceMatchup race match-up to guess the format from
     * @return the guessed format; {@link Format#UNKNOWN} if format cannot be guessed
     */
    public static Format guessFormat( final String raceMatchup ) {
    	if ( raceMatchup == null || raceMatchup.isEmpty() )
    		return Format.UNKNOWN;
    	
		int teamsCount = 1;
		for ( int i = raceMatchup.length() - 2; i > 0; i-- )
			if ( raceMatchup.charAt( i ) == 'v' )
				teamsCount++;
    	
		if ( teamsCount < 2 )
    		return Format.UNKNOWN;
		
    	Format format = Format.CUSTOM;                                                            // PPvZZZ
    	if ( teamsCount == 2 )
    		switch ( raceMatchup.length() ) {
    		case 3 : if ( raceMatchup.charAt( 1 ) == 'v' ) format = Format.ONE_VS_ONE    ; break; // PvZ
    		case 5 : if ( raceMatchup.charAt( 2 ) == 'v' ) format = Format.TWO_VS_TWO    ; break; // PPvZZ
    		case 7 : if ( raceMatchup.charAt( 3 ) == 'v' ) format = Format.THREE_VS_THREE; break; // PPPvZZZ
    		case 9 : if ( raceMatchup.charAt( 4 ) == 'v' ) format = Format.FOUR_VS_FOUR  ; break; // PPPPvZZZZ
    		}
    	else {
    		// Check if FFA (examples: ZvZvP, PvPvPvP, PvPvPvPvPvPvPvP)
    		boolean ffa = true;
    		for ( int i = 1; i < raceMatchup.length(); i += 2 )
    			if ( raceMatchup.charAt( i ) != 'v' ) {
    				ffa = false;
    				break;
    			}
    		if ( ffa )
				format = Format.FREE_FOR_ALL;
    	}
		
		return format;
    }
    
	/**
	 * Repository of the available ability codes for different replay versions.
	 * 
	 * <p>An ability codes repository defines static parameters (ability codes and their entity params, unit codes)
	 * for a set of replay versions which use the same static parameters.</p>
	 * 
	 * @author Andras Belicza
	 */
	public static enum AbilityCodesRepository {
		// The order is important: it must be sorted downwards by the version the ability codes apply to.
		// The current (newest) version must always be the first
		V_2_0_3       ( "ability_codes_2.0.3-"      ),
		V_1_5__1_5_4  ( "ability_codes_1.5-1.5.4"   ),
		V_1_4_3__1_4_4( "ability_codes_1.4.3-1.4.4" ), // Only change introduced compared to 1.4.2: observer build time decreased from 40 to 30 sec, and only from May 10, 2012 (USA)) - replay version did not change, so this is inaccurate for replays with this version but date before May 10!
		V_1_4_2__1_4_2( "ability_codes_1.4.2-1.4.2" ),
		V_1_4__1_4_1  ( "ability_codes_1.4-1.4.1"   ),
		V_1_3_3__1_3_6( "ability_codes_1.3.3-1.3.6" ),
		V_1_3__1_3_2  ( "ability_codes_1.3-1.3.2"   ),
		V_1_2__1_2    ( "ability_codes_1.2-1.2"     ),
		V_1_1__1_1    ( "ability_codes_1.1-1.1"     ),
		V_0_19__1_0   ( "ability_codes_0.19-1.0"    );
		
		/** Name of the file that contains the ability codes.     */
		public final String  fileName;
		/** Min replay version these ability codes apply to.      */
		public final int[]   minVersion;
		/** Max replay version these ability codes apply to.      */
		public final int[]   maxVersion;
		/** Reference to the loaded ability codes. Lazily loaded. */
		private AbilityCodes abilityCodes;
		
		/**
		 * Creates a new AbilityCodesRepository.
		 * @param fileName name of the file that contains the ability codes; it must be named like this:<br>
		 * 					<code>something_[min_version]-[max_version]</code><br>
		 * 					where the max_version is optional, examples:<br>
		 * 					<code>ability_codes_0.19-1.0</code><br>
		 * 					<code>ability_codes_1.1-</code><br>
		 */
		private AbilityCodesRepository( final String fileName ) {
			this.fileName = fileName;
			
			final String minVersionString = fileName.substring( fileName.lastIndexOf( '_' ) + 1, fileName.indexOf( '-' ) );
			final String maxVersionString = fileName.substring( fileName.indexOf( '-' ) + 1 );
			
			minVersion = parseVersion( minVersionString );
			maxVersion = parseVersion( maxVersionString );
		}
		
		/**
		 * Returns the ability codes.
		 * @return the ability codes
		 */
		public AbilityCodes getAbilityCodes() {
			if ( abilityCodes == null )
				abilityCodes = loadAbilityCodes( fileName );
			return abilityCodes;
		}
		
		/**
		 * Returns the ability codes for the specified version.
		 * @param version version to return ability codes for
		 * @return the ability codes for the specified version; or <code>null</code> if no ability codes for the specified version
		 */
		public static AbilityCodes getForVersion( final int[] version ) {
			for ( final AbilityCodesRepository abilityCodesRepository : values() )
				if ( compareVersions( version, abilityCodesRepository.minVersion ) >= 0 && ( abilityCodesRepository.maxVersion.length == 0 || compareVersions( version, abilityCodesRepository.maxVersion ) <= 0 ) )
					return abilityCodesRepository.getAbilityCodes();
			
			return null;
		}
		
		/**
		 * Returns the ability codes for the current version.
		 * @return the ability codes for the current version
		 */
		public static AbilityCodes getCurrentAbilityCodes() {
			return values()[ 0 ].getAbilityCodes();
		}
		
	}
	
	/** Ability codes of the current version. */
	public static final AbilityCodes CURRENT_ABILITY_CODES = AbilityCodesRepository.getCurrentAbilityCodes();
	
	/**
	 * Loads ability codes from the specified file.
	 * @param fileName name of the file to load ability codes from
	 */
	private static AbilityCodes loadAbilityCodes( final String fileName ) {
		final AbilityCodes abilityCodes = new AbilityCodes();
		
		BufferedReader input = null;
		try {
			input = new BufferedReader( new InputStreamReader( ReplayUtils.class.getResourceAsStream( fileName ) ) );
			
			EntityParams entityParams;
			String line;
			while ( ( line = input.readLine() ) != null ) {
				if ( line.isEmpty() )
					continue;      // Skip empty lines
				final StringTokenizer tokenizer = new StringTokenizer( line, "," );
				final String code = tokenizer.nextToken();
				if ( code.length() == 4 ) {
					// It's a unit type
					final Short unitType = Short.valueOf( code, 16 );
					abilityCodes.UNIT_TYPE_NAME.put( unitType, tokenizer.nextToken().intern() ); // Use intern() because these names are the same across all ability_codes_xxx files
					if ( "A".equals( tokenizer.nextToken() ) )
						abilityCodes.MACRO_UNIT_TYPE_SET.add( unitType );
				}
				else {
					// It's an ability code
					final Integer abilityCode = Integer.valueOf( code, 16 );
					final String token = tokenizer.nextToken();
					if ( token.charAt( 0 ) == '"' ) {
						final String abilityGroupName = tokenizer.nextToken();
						final String microMacroValue  = tokenizer.nextToken(); // "I" => micro, "A" => macro
						abilityCodes.COMMON_BASE_ABILITY_CODES.put( abilityCode, new Object[] {
							// Ability name:
							token.substring( 1, token.length() - 1 ),
							// Ability group:
							"null".equals( abilityGroupName ) ? null : AbilityGroup.valueOf( abilityGroupName.substring( abilityGroupName.indexOf( '.' ) + 1 ) ),
							"A".equals( microMacroValue )
						} );
						if ( ( entityParams = readEntityParams( tokenizer ) ) != null )
							abilityCodes.COMMON_ABILITY_PARAMS.put( abilityCode, entityParams );
					}
					else {
						if ( token.startsWith( "U." ) ) {
							final Unit unit = Unit.valueOf( token.substring( 2 ) );
							abilityCodes.TRAIN_ABILITY_CODES.put( abilityCode, unit );
							if ( ( entityParams = readEntityParams( tokenizer ) ) != null )
								abilityCodes.UNIT_PARAMS.put( unit, entityParams );
						} else if ( token.startsWith( "B." ) ) {
							final Building building = Building.valueOf( token.substring( 2 ) );
							abilityCodes.BUILD_ABILITY_CODES.put( abilityCode, building );
							if ( ( entityParams = readEntityParams( tokenizer ) ) != null )
								abilityCodes.BUILDING_PARAMS.put( building, entityParams );
						} else if ( token.startsWith( "UA." ) ) {
							final UnitAbility unitAbility = UnitAbility.valueOf( token.substring( 3 ) );
							abilityCodes.USE_UNIT_ABILITY.put( abilityCode, new Object[] { unitAbility, Unit.valueOf( tokenizer.nextToken().substring( 2 ) ) } );
							if ( ( entityParams = readEntityParams( tokenizer ) ) != null )
								abilityCodes.UNIT_ABILITY_PARAMS.put( unitAbility, entityParams );
						} else if ( token.startsWith( "BA." ) ) {
							final BuildingAbility buildingAbility = BuildingAbility.valueOf( token.substring( 3 ) );
							abilityCodes.USE_BUILDING_ABILITY.put( abilityCode, new Object[] { buildingAbility, Building.valueOf( tokenizer.nextToken().substring( 2 ) ) } );
							if ( ( entityParams = readEntityParams( tokenizer ) ) != null )
								abilityCodes.BUILDING_ABILITY_PARAMS.put( buildingAbility, entityParams );
						} else if ( token.startsWith( "R." ) ) {
							final Research research = Research.valueOf( token.substring( 2 ) );
							abilityCodes.RESEARCH_ABILITY_CODES.put( abilityCode, research );
							if ( ( entityParams = readEntityParams( tokenizer ) ) != null )
								abilityCodes.RESEARCH_PARAMS.put( research, entityParams );
						} else if ( token.startsWith( "UP." ) ) {
							final Upgrade upgrade = Upgrade.valueOf( token.substring( 3 ) );
							abilityCodes.UPGRADE_ABILITY_CODES.put( abilityCode, upgrade );
							if ( ( entityParams = readEntityParams( tokenizer ) ) != null )
								abilityCodes.UPGRADE_PARAMS.put( upgrade, entityParams );
						} else if ( token.startsWith( "HU." ) ) {
							final Unit unit = Unit.valueOf( token.substring( 3 ) );
							abilityCodes.TRAIN_HALLU_ABILITY_CODES.put( abilityCode, unit );
						} else if ( token.startsWith( "WU." ) ) {
							final Unit unit = Unit.valueOf( token.substring( 3 ) );
							abilityCodes.WARP_ABILITY_CODES.put( abilityCode, unit );
							if ( ( entityParams = readEntityParams( tokenizer ) ) != null )
								abilityCodes.WARPED_UNIT_PARAMS.put( unit, entityParams );
						}
					}
				}
			}
			
			return abilityCodes;
		} catch ( final IOException ie ) {
			ie.printStackTrace();
			throw new RuntimeException( "Internal error: failed to load ability codes!" );
		} finally {
			if ( input != null )
				try { input.close(); } catch ( final IOException ie ) {}
		}
	}
	
	/**
	 * Reads entity parameters from the tokenizer.
	 * @param tokenizer tokenizer to read entity parameters from
	 * @return the read entity params
	 */
	private static EntityParams readEntityParams( final StringTokenizer tokenizer ) {
		if ( tokenizer.hasMoreTokens() ) {
			final EntityParams entityParams = new EntityParams();
			
			entityParams.minerals = Integer.parseInt( tokenizer.nextToken() );
			entityParams.gas      = Integer.parseInt( tokenizer.nextToken() );
			if ( tokenizer.hasMoreTokens() ) {
				entityParams.time       = Integer.parseInt( tokenizer.nextToken() );
				if ( tokenizer.hasMoreTokens() )
					entityParams.supply = Integer.parseInt( tokenizer.nextToken() );
			}
			
			return entityParams;
		}
		return null;
	}
	
	/**
	 * Applies the favored player list setting on the specified details of a replay.
	 * @param details details of a replay to apply the favored player list setting on
	 */
	public static void applyFavoredPlayerListSetting( final Details details ) {
		final List< String > favoredPlayerList = GeneralUtils.getFavoredPlayerList();
		details.rearrangePlayers( favoredPlayerList );
	}
	
}
