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

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.EnumCache;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent;
import hu.belicza.andras.sc2gears.sc2replay.model.Details;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.BuildAction;
import hu.belicza.andras.sc2gears.sc2replay.model.InitData;
import hu.belicza.andras.sc2gears.sc2replay.model.InitData.Client;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gearspluginapi.api.enums.ReplayOrigin;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A utility class responsible to cache replays and to read replays from cache.
 * 
 * @author Andras Belicza
 */
public class ReplayCache {
	
	/** Version of the cache data format. */
	// If a changed/modified replay parser results in different parsed info (taking into consideration those that are stored in the cache files)
	// this version has to be changed/incremented (which will ensure that the outdated/wrong info from the cache will be omitted).
	public static final short CACHE_VERSION = 33;
	
	
	/** Persistent map storing the replay cache data (key: replay MD5; value: cache info. */
	private static final PersistentMap persistentMap;
	static {
		PersistentMap pm = null;
		try {
			pm = new PersistentMap( new File( Consts.FOLDER_REPLAY_CACHE ), CACHE_VERSION );
		} catch ( final IOException ie ) {
			System.err.println( "Failed to initialize the internal replay cache! Replay cache service is disabled!" );
			ie.printStackTrace();
			GuiUtils.showErrorDialog( Language.getText( "general.replayCacheInitError" ) );
		}
		persistentMap = pm;
	}
	
	/**
	 * Closes the replay cache.
	 */
	public static void closeCache() {
		if ( persistentMap != null )
			persistentMap.close();
	}
	
	/**
	 * No need to instantiate this class.
	 */
	private ReplayCache() {
	}
	
	/**
	 * Gets and returns a replay.<br>
	 * This method can parse the replay either from the cache, or if cache is not preferred or is not in the cache, will parse the replay
	 * using the {@link ReplayFactory}.<br>
	 * If replay is present in the cache and it is sufficient to the provided build order length and time exclusion for APM parameters,
	 * it will be returned from the cache.
	 * Else the replay will be read using the {@link ReplayFactory} with the supplied parseableContentSet,
	 * or if it is not supplied, {@link ReplayFactory#GENERAL_DATA_CONTENT} will be used
	 * (the replay parser takes the APM time exclusion limit from the Settings).
	 * If it is wanted, the parsed replay will be put into the cache.
	 * 
	 * @param replayFile          replay file to be loaded from the cache
	 * @param buildOrderLength    optional parameter, if provided: minimum build order length required in the parsed replay
	 * @param timeExclusionForApm optional parameter, if provided: initial time exclusion for calculated APMs
	 * @param useCache            tells if cache can be used
	 * @param saveToCache         tells if replay should be saved back to cache if it wasn't in it
	 * @param parseableContentSet optional replay content set to be parsed if cache does not contain the replay, if null, {@link ReplayFactory#GENERAL_DATA_CONTENT} will be used
	 * @return the parsed replay
	 */
	public static Replay getReplay( final File replayFile, final Integer buildOrderLength, final Integer timeExclusionForApm, final boolean useCache, final boolean saveToCache, final Set< ReplayContent > parseableContentSet ) {
		if ( !replayFile.exists() )
			return null;
		
		// MD5 digest of the content of the replay.
		String md5;
		
		if ( useCache || saveToCache ) {
			md5 = Settings.getBoolean( Settings.KEY_SETTINGS_MISC_USE_MD5_HASH_FROM_FILE_NAME ) ? peekFileMd5( replayFile ) : null;
			if ( md5 == null )
				md5 = GeneralUtils.calculateFileMd5( replayFile );
			if ( md5.length() == 0 )
				return null;
		}
		else
			md5 = null;
		
		if ( useCache ) {
			final CacheInfo cacheInfo = readCachedReplay( md5 );
			
			if ( cacheInfo != null ) {
				// Check if the settings affecting Multi-rep analysis match
				if ( ( buildOrderLength == null || cacheInfo.buildOrderLength >= buildOrderLength )
						&& ( timeExclusionForApm == null || cacheInfo.replay.excludedInitialFrames == timeExclusionForApm ) )
					return cacheInfo.replay;
			}
		}
		
		// Cache disabled, replay not cached or failed to read replay cache...
		final Replay replay = ReplayFactory.parseReplay( replayFile.getAbsolutePath(), parseableContentSet == null ? ReplayFactory.GENERAL_DATA_CONTENT : parseableContentSet );
		if ( replay != null && saveToCache )
			cacheReplay( replay, buildOrderLength, md5 );
		
		return replay;
	}
	
	/**
	 * Checks the file name if it contains the MD5.
	 * @param file file whose MD5 to peek
	 * @return the MD5 if it is found in the file name; <code>null</code> otherwise
	 */
	private static String peekFileMd5( final File file ) {
		final String name = GeneralUtils.getFileNameWithoutExt( file );
		
		for ( int index = name.indexOf( "MD5_" ); index >= 0; index = name.indexOf( "MD5_", index ) ) {
			index += 4;
			
			if ( name.length() - index < 22 )
				break;
			
			final String md5Candidate = decodeEncodedMd5( name.substring( index, index + 22 ) );
			if ( md5Candidate != null )
				return md5Candidate;
		}
		
		return null;
	}
	
	/**
	 * Contains the replay constructed from the cache and other related info.
	 * @author Andras Belicza
	 */
	private static class CacheInfo {
		/** Build order length saved in the cache file.  */
		public int    buildOrderLength;
		/** Replay constructed from the cache.           */
		public Replay replay;
	}
	
	/**
	 * Tries to read a replay from the cache and returns a {@link CacheInfo} object with the result.
	 * @param md5 MD5 digest of the content of the replay whose cached version to be read
	 * @return a {@link CacheInfo} object with the result; or <code>null</code> if the replay is not cached
	 */
	private static CacheInfo readCachedReplay( final String md5 ) {
		final byte[] cacheContent = persistentMap == null ? null : persistentMap.get( md5 );
		if ( cacheContent == null )
			return null;
		
		final CacheInfo cacheInfo = new CacheInfo();
		
		try ( final DataInputStream input = new DataInputStream( new ByteArrayInputStream( cacheContent ) ) ) {
			cacheInfo.buildOrderLength = input.readShort();
			
			final Replay replay = cacheInfo.replay = new Replay( ReplayOrigin.REPLAY_CACHE );
			replay.initData = new InitData();
			replay.details = new Details();
			
			final int[] buildNumbers = new int[ 4 ];
			for ( int i = 0; i < 4; i++ )
				buildNumbers[ i ] = input.readInt();
			replay.setBuildNumbers( buildNumbers );
			replay.setGameDuration( input.readInt() );
			replay.excludedInitialFrames = input.readInt();
			
			replay.initData.gateway = EnumCache.GATEWAYS[ input.readByte() ];
			replay.initData.format = EnumCache.FORMATS[ input.readByte() ];
			replay.initData.gameSpeed = EnumCache.GAME_SPEEDS[ input.readByte() ];
			replay.converterGameSpeed = Settings.getBoolean( Settings.KEY_SETTINGS_MISC_USE_REAL_TIME_MEASUREMENT ) ? replay.initData.gameSpeed : GameSpeed.NORMAL;			
			replay.initData.gameType = EnumCache.GAME_TYPES[ input.readByte() ];
			final byte competitiveByte = input.readByte();
			replay.initData.competitive = competitiveByte == 0 ? null : competitiveByte == 1 ? Boolean.TRUE : Boolean.FALSE;
			
			replay.details.saveTime = input.readLong();
			replay.details.originalMapName = input.readUTF();
			replay.details.mapName = Settings.getMapAliasGroupName( replay.details.originalMapName );
			replay.details.expansion = EnumCache.EXPANSIONS[ input.readByte() ];
			
			replay.details.players = new Player[ input.readByte() ];
			for ( int i = 0; i < replay.details.players.length; i++ ) {
				final Player player = replay.details.players[ i ] = new Player();
				player.playerId.name = input.readUTF();
				player.playerId.battleNetId = input.readInt();
				player.playerId.battleNetSubId = input.readInt();
				player.nameWithClan =  input.readUTF();
				player.client = new Client();
				player.client.name = player.nameWithClan;
				player.client.league = EnumCache.LEAGUES[ input.readByte() ];
				player.playerId.gateway = replay.initData.gateway;
				player.type = EnumCache.PLAYER_TYPES[ input.readByte() ];
				player.lastActionFrame = input.readInt();
				player.race = EnumCache.RACES[ input.readByte() ];
				player.finalRace = EnumCache.RACES[ input.readByte() ];
				player.team = input.readInt();
				if ( input.readBoolean() ) // Is winner known?
					player.isWinner = input.readBoolean();
				player.actionsCount = input.readInt();
				player.excludedActionsCount = input.readInt();
				player.effectiveActionsCount = input.readInt();
				player.excludedEffectiveActionsCount = input.readInt();
				player.totalHatchTime = input.readInt();
				player.totalHatchSpawnTime = input.readInt();
				player.totalInjectionGap = input.readInt();
				player.totalInjectionGapCount = input.readInt();
			}
			
			// Build actions
			replay.gameEvents = new GameEvents( replay, null );
			final int buildActionsCount = input.readShort();
			final List< Action > actionList = new ArrayList< Action >( buildActionsCount );
			for ( int i = buildActionsCount - 1; i >= 0; i-- ) {
				final BuildAction ba = replay.gameEvents.new BuildAction( EnumCache.BUILDINGS[ input.readByte() ] );
				ba.player = input.readByte();
				actionList.add( ba );
			}
			replay.gameEvents.actions = actionList.toArray( new Action[ buildActionsCount ] );
			
			// Chat words
			replay.messageEvents = new MessageEvents();
			@SuppressWarnings("unchecked")
			final Map< String, IntHolder >[] wordCountMaps = new Map[ input.readShort() ];
			for ( int i = 0; i < wordCountMaps.length; i++ ) {
				final int wordsCount = input.readShort() & 0xffff;
				if ( wordsCount > 0 ) {
					wordCountMaps[ i ] = new HashMap< String, IntHolder >( wordsCount );
					for ( int j = wordsCount - 1; j >= 0; j-- ) {
						final String word = input.readUTF();
						wordCountMaps[ i ].put( word, new IntHolder( input.readShort() & 0xffff ) );
					}
				}
			}
			replay.messageEvents.setWordCountMaps( wordCountMaps );
			
			return cacheInfo;
			
		} catch ( final Exception e ) {
			e.printStackTrace();
			return cacheInfo;
		}
	}
	
	/**
	 * Caches a replay: saves the replay in the cache.<br>
	 * If any error occurs, returns silently.
	 * @param replay            parsed {@link Replay} object of the replay to be cached
	 * @param buildOrderLength_ length of build order to be cached
	 * @param md5               md5 digest of the content of the replay file
	 */
	private static void cacheReplay( final Replay replay, final Integer buildOrderLength_, final String md5 ) {
		final ByteArrayOutputStream bout = new ByteArrayOutputStream( 1024 );
		try ( final DataOutputStream output = new DataOutputStream( bout ) ) {
			final int buildOrderLength = buildOrderLength_ == null ? Settings.getInt( Settings.KEY_SETTINGS_MISC_BUILD_ORDER_LENGTH ) : buildOrderLength_;
			output.writeShort( buildOrderLength );
			
			for ( final int buildNumber : replay.buildNumbers )
				output.writeInt( buildNumber );
			output.writeInt( replay.frames );
			output.writeInt( replay.excludedInitialFrames );
			
			output.writeByte( replay.initData.gateway.ordinal() );
			output.writeByte( replay.initData.format.ordinal() );
			output.writeByte( replay.initData.gameSpeed.ordinal() );
			output.writeByte( replay.initData.gameType.ordinal() );
			output.writeByte( replay.initData.competitive == null ? 0 : replay.initData.competitive ? 1 : 2 );
			
			output.writeLong( replay.details.saveTime );
			output.writeUTF( replay.details.originalMapName );
			output.writeByte( replay.details.expansion.ordinal() );
			
			output.writeByte( replay.details.players.length );
			for ( final Player player : replay.details.players ) {
				output.writeUTF( player.playerId.name );
				output.writeInt( player.playerId.battleNetId );
				output.writeInt( player.playerId.battleNetSubId );
				output.writeUTF( player.nameWithClan );
				output.writeByte( player.getLeague().ordinal() );
				output.writeByte( player.type.ordinal() );
				output.writeInt( player.lastActionFrame );
				output.writeByte( player.race.ordinal() );
				output.writeByte( player.finalRace.ordinal() );
				output.writeInt( player.team );
				output.writeBoolean( player.isWinner != null ); // Is winner known?
				if ( player.isWinner != null )
					output.writeBoolean( player.isWinner );
				output.writeInt( player.actionsCount );
				output.writeInt( player.excludedActionsCount );
				output.writeInt( player.effectiveActionsCount );
				output.writeInt( player.excludedEffectiveActionsCount );
				output.writeInt( player.totalHatchTime );
				output.writeInt( player.totalHatchSpawnTime );
				output.writeInt( player.totalInjectionGap );
				output.writeInt( player.totalInjectionGapCount );
			}
			
			// Build actions
			final int[] counters = new int[ replay.details.players.length ];
			int doneCounter = 0;
			final List< BuildAction > buildActionList = new ArrayList< BuildAction >( Math.min( 300, counters.length * buildOrderLength ) );
			for ( final Action action : replay.gameEvents.actions ) {
				if ( action instanceof BuildAction && counters[ action.player ] < buildOrderLength ) {
					buildActionList.add( (BuildAction) action );
					if ( ++counters[ action.player ] == buildOrderLength )
						if ( ++doneCounter == counters.length )
							break;
				}
			}
			output.writeShort( buildActionList.size() );
			for ( final BuildAction ba : buildActionList ) {
				output.writeByte( ba.building.ordinal() );
				output.writeByte( ba.player );
			}
			
			// Chat words
			final Map< String, IntHolder >[] wordCountMaps = replay.messageEvents.getWordCountMaps( replay.initData.clientNames.length );
			output.writeShort( wordCountMaps.length );
			for ( final Map< String, IntHolder > wordCountMap : wordCountMaps ) {
				output.writeShort( wordCountMap == null ? 0 : wordCountMap.size() );
				if ( wordCountMap != null )
					for ( final Entry< String, IntHolder > entry : wordCountMap.entrySet() ) {
						output.writeUTF( entry.getKey() );
						output.writeShort( entry.getValue().value );
					}
			}
			
			output.flush();
			
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
		
		if ( persistentMap != null )
			persistentMap.put( md5, bout.toByteArray() );
	}
	
	/**
	 * Empties the replay cache.
	 */
	public static void emptyCache() {
		if ( persistentMap != null )
			persistentMap.clear();
	}
	
	/** Char set to use to encode MD5 hashes. */
	private static final char[] ENCODED_MD5_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$#".toCharArray();
	
	/**
	 * Encodes an MD5 hash presented in hexadecimal string.<br>
	 * The purpose is to make it shorter while allowing it to use in file names. The algorithm is very similar to Base64 but doesn't use padding.<br>
	 * Shortens from 32 characters to 22 characters.
	 * 
	 * @param md5 MD5 hash to be encoded presented in hexadecimal string, must be 32 characters
	 * @return the encoded MD5 hash
	 * @see #decodeEncodedMd5(String) 
	 */
	public static String encodeMd5( final String md5 ) {
		// Encoded characters use 6 bits => 3 hexa digits (12 bits) results in 2 encoded chars
		// md5.length() = 32 => 21 encoded chars + an extra which holds only 2 bits (the last 4 "missing" bits will be unused/undetermined)
		
		final StringBuilder encodedBuilder = new StringBuilder( 22 );
		
		final int[] digits = new int[ 3 ];
		
		for ( int i = 0; i < 32; ) {
			// Process 3 hexa digits to produce 2 encoded chars
			for ( int j = 0; j < 3; j++ )
				if ( i < 32 ) {
					final char ch = md5.charAt( i++ );
					digits[ j ] = ch >= '0' && ch <= '9' ? ch - '0' : 10 + ch - 'a';
				}
			
			encodedBuilder.append( ENCODED_MD5_ALPHABET[ ( digits[ 0 ] << 2 ) | ( ( digits[ 1 ] & 0x0c ) >> 2 ) ] );
			encodedBuilder.append( ENCODED_MD5_ALPHABET[ ( ( digits[ 1 ] & 0x03 ) << 4 ) | digits[ 2 ] ] );
		}
		
		return encodedBuilder.toString();
	}
	
	/**
	 * Decodes an encoded MD5 hash value.
	 * @param encodedMd5 encoded MD5 hash value to be decoded
	 * @return the decoded MD5 hash value in hexadecimal string representation; or <code>null</code> if the input is invalid
	 * @see #encodeMd5(String)
	 */
	private static String decodeEncodedMd5( final String encodedMd5 ) {
		if ( encodedMd5.length() != 22 )
			return null;
		
		final char[] HEX_DIGITS = GeneralUtils.HEX_DIGITS;
		
		final StringBuilder md5Builder = new StringBuilder( 32 );
		
		final int[] digits = new int[ 2 ];
		
		for ( int i = 0; i < 22; ) {
			// Process 2 encoded chars to produce 3 hexa digits
			for ( int j = 0; j < 2; j++ ) {
				final char ch = encodedMd5.charAt( i++ );
				
				if ( ch >= 'a' && ch <= 'z' )
					digits[ j ] = ch - 'a' + 10;     // 'a' has a value of 10
				else if ( ch >= 'A' && ch <= 'Z' )
					digits[ j ] = ch - 'A' + 36;     // 'A' has a value of 36
				else if ( ch >= '0' && ch <= '9' )
					digits[ j ] = ch - '0';          // '0' has a value of 0
				else if ( ch == ENCODED_MD5_ALPHABET[ 62 ] )
					digits[ j ] = 62;
				else if ( ch == ENCODED_MD5_ALPHABET[ 63 ] )
					digits[ j ] = 63;
				else
					return null; // Invalid character!
			}
			
			md5Builder.append( HEX_DIGITS[ digits[ 0 ] >> 2 ] );
			md5Builder.append( HEX_DIGITS[ ( ( digits[ 0 ] & 0x03 ) << 2 ) | ( digits[ 1 ] >> 4 ) ] );
			if ( i < 22 )
				md5Builder.append( HEX_DIGITS[ digits[ 1 ] & 0x0f ] );
		}
		
		return md5Builder.toString();
	}
	
}
