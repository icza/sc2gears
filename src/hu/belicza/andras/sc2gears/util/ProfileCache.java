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
import hu.belicza.andras.sc2gears.services.Downloader;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gearspluginapi.api.enums.League;
import hu.belicza.andras.sc2gearspluginapi.api.listener.CustomPortraitListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.DownloaderCallback;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ProfileListener;
import hu.belicza.andras.sc2gearspluginapi.api.profile.IProfile;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerId;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Gateway;
import hu.belicza.andras.util.BnetUtils;
import hu.belicza.andras.util.BnetUtils.Profile;
import hu.belicza.andras.util.BnetUtils.Profile.BestTeamRank;
import hu.belicza.andras.util.BnetUtils.Profile.TeamRank;

import java.awt.Dialog;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A utility class responsible to cache profile info and to read profile info from cache.
 * 
 * <p>Profile info is taken and parsed from Battle.net profile pages.</p>
 * 
 * @author Andras Belicza
 */
public class ProfileCache extends FileBasedCache {
	
	/** Version of the cache data format. */
	public static final short CACHE_VERSION = 9;
	
	/**
	 * Descriptor of a custom portrait.
	 * @author Andras Belicza
	 */
	private static class CustomPortraitInfo {
		/** Version of the custom portrait.                    */
		public final String version;
		/** URL of the low resolution portrait image (45x45).  */
		public final String url45;
		/** URL of the high resolution portrait image (90x90). */
		public final String url90;
		
		/**
		 * Creates a new CustomPortraitInfo. 
		 * @param version version of the custom portrait
		 * @param url45   URL of the low resolution portrait image (45x45)
		 * @param url90   URL of the high resolution portrait image (90x90)
		 */
		public CustomPortraitInfo( final String version, final String url45, final String url90 ) {
			this.version = version;
			this.url45   = url45;
			this.url90   = url90;
		}
		
		/**
		 * Returns the URL of the custom portrait.
		 * @param highRes tells if URL of the high resolution is to be returned 
		 * @return the URL of the custom portrait
		 */
		public String getCustomPortraitUrl( final boolean highRes ) {
			return highRes ? url90 : url45;
		}
		
	}
	
	/** Custom portrait map. Maps from user id to custom portrait info.<br>
	 * User id: <code>"[gateway id]-[bnet subid]-[bnet id]"</code>; example: <code>"EU-1-206154"</code><br> */
	private static final Map< String, CustomPortraitInfo > USER_ID_CUSTOM_PORTRAIT_MAP = new HashMap< String, CustomPortraitInfo >();
	/** Time when the custom portrait list was updated at. */
	private static final Date CUSTOM_PORTRAIT_LIST_UPDATED_AT = new Date(); // Set it to the current time because if we can't download it, we won't try it again each time it is queried
	static {
		for ( int errorCounter = 0; errorCounter < 3; errorCounter++ )
			if ( downloadCustomPortraitList() )
				break;
	}
	
	/**
	 * Downloads and sets the custom portrait list.
	 * @return true if download was successful; false otherwise
	 */
	private static boolean downloadCustomPortraitList() {
		if ( Consts.DEVELOPER_MODE )
			return true;
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments( true );
			final Document latestVersionDocument = factory.newDocumentBuilder().parse( new URL( Consts.URL_CUSTOM_PORTRAITS_LIST ).toURI().toString() );
			final Element rootElement = latestVersionDocument.getDocumentElement();
			
			final NodeList customPortraitList = rootElement.getElementsByTagName( "p" );
			
			final int length = customPortraitList.getLength();
			final Map< String, CustomPortraitInfo > TEMP_USER_ID_CUSTOM_PORTRAIT_MAP = new HashMap< String, CustomPortraitInfo >( length );
			for ( int i = 0; i < length; i++ ) {
				final Element customPortrait = (Element) customPortraitList.item( i );
				TEMP_USER_ID_CUSTOM_PORTRAIT_MAP.put( customPortrait.getAttribute( "uid" ),
						new CustomPortraitInfo( customPortrait.getAttribute( "ver" ), customPortrait.getAttribute( "url45" ), customPortrait.getAttribute( "url90" ) ) );
			}
			
			USER_ID_CUSTOM_PORTRAIT_MAP.clear();
			USER_ID_CUSTOM_PORTRAIT_MAP.putAll( TEMP_USER_ID_CUSTOM_PORTRAIT_MAP );
			CUSTOM_PORTRAIT_LIST_UPDATED_AT.setTime( System.currentTimeMillis() );
			
			return true;
		} catch ( final Exception e ) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * No need to instantiate this class.
	 */
	private ProfileCache() {
	}
	
	/**
	 * Calls and returns {@link #queryProfile(IPlayerId, ProfileListener, boolean, boolean)} with <code>forceRetrieve=false</code>.
	 */
	public static boolean queryProfile( final IPlayerId playerId, final ProfileListener profileListener, final boolean queryExtendedInfo ) {
		return queryProfile( playerId, profileListener, queryExtendedInfo, false );
	}
	
	/**
	 * Asynchronous profile get method.
	 * 
	 * <p>Queries the profile of the specified player. If the profile is cached, it will be passed to the profile listener immediately.
	 * If the profile is not cached, or is out-dated, it will be retrieved (again) and the profile listener will be called (again)
	 * with the new profile.</p>
	 * 
	 * <p>New profiles are retrieved asynchronously in a new thread. If the retrieval of a new profile fails and no cached,
	 * out-dated version is available, the profile listener will be called with a <code>null</code> value.</p>
	 * 
	 * <p>If extended info is required but is not available right away, the base profile info will be passed to the listener,
	 * and the extended info will be retrieved after that, and when it is ready, the listener will be called again.</p>
	 * 
	 * @param playerId          player identifier
	 * @param profileListener   listener to be called when the profile is available
	 * @param queryExtendedInfo tells if extended profile info has to be retrieved too
	 * @param forceRetrieve     forces retrieving the profile even if its validity time is not over yet; also only calls {@link ProfileListener#profileReady(IProfile, boolean)} if the new profile is ready
	 * @return true if a profile info is available right away; false otherwise
	 */
	public static boolean queryProfile( final IPlayerId playerId, final ProfileListener profileListener, final boolean queryExtendedInfo, final boolean forceRetrieve ) {
		if ( playerId.getGateway() == Gateway.UNKNOWN || playerId.getGateway() == Gateway.PUBLIC_TEST || playerId.getBattleNetId() == 0 ) {
			profileListener.profileReady( null, false );
			return true;
		}
		
		final Profile profile = readCachedProfile( playerId );
		
		final boolean haveToRetrieve    = profile == null || forceRetrieve || profile.updatedAt.getTime() + Settings.getInt( Settings.KEY_SETTINGS_MISC_PROFILE_INFO_VALIDITY_TIME ) * 24L*60*60*1000 < System.currentTimeMillis();
		final boolean haveToRetrieveExt = queryExtendedInfo &&
			( profile == null || forceRetrieve || profile.allRanksUpdatedAt.getTime() + Settings.getInt( Settings.KEY_SETTINGS_MISC_PROFILE_INFO_VALIDITY_TIME ) * 24L*60*60*1000 < System.currentTimeMillis() );
		
		if ( profile != null && !forceRetrieve )
			profileListener.profileReady( profile, haveToRetrieve || haveToRetrieveExt );
		
		if ( haveToRetrieve || haveToRetrieveExt ) {
			new NormalThread( "Profile retriever" ) {
				@Override
				public void run() {
					
					final Profile newProfile;
					if ( haveToRetrieve ) {
						newProfile = BnetUtils.retrieveProfile( playerId, null );
						if ( newProfile != null )
							cacheProfile( newProfile, playerId );
						// Have to call profileReady() even if new profile is null to let the listener know that there may be no more profileReady() (in which we could let the listener know that no more retrieving is in progress)
						profileListener.profileReady( newProfile != null ? newProfile : profile, haveToRetrieveExt && ( newProfile != null || profile != null ) ); // If both newProfile and profile are nulls, an attempt to retrieve extended profile will not be made!
					}
					else
						newProfile = null;
					
					if ( haveToRetrieveExt ) {
						Profile extProfile = newProfile == null ? profile : newProfile;
						if ( extProfile != null ) {
							extProfile = BnetUtils.retrieveExtProfile( playerId, null, extProfile );
							if ( extProfile != null )
								cacheProfile( extProfile, playerId );
							// Have to call profileReady() even if null is returned to let the listener know that no more retrieving is in progress
							profileListener.profileReady( extProfile, false );
						}
					}
				}
			}.start();
		}
		
		return profile != null && !forceRetrieve;
	}
	
	/**
	 * Tries to read a profile from the cache.
	 * @param playerId player identifier
	 * @return the profile if cached; or <code>null</code> if the profile is not cached
	 */
	private static Profile readCachedProfile( final IPlayerId playerId ) {
		final File cacheFile = new File( getCacheFolder( playerId ), Integer.toHexString( playerId.getBattleNetId() ) );
		if ( !cacheFile.exists() )
			return null;
		
		try ( final DataInputStream input = new DataInputStream( new FileInputStream( cacheFile ) ) ) {
			final short cacheFileVersion = input.readShort();
			if ( cacheFileVersion != CACHE_VERSION )
				return null;
			
			final Profile profile = new Profile( new Date( input.readLong() ) );
			profile.allRanksUpdatedAt       = new Date( input.readLong() );
			
			profile.portraitGroup           = input.readShort();
			profile.portraitRow             = input.readShort();
			profile.portraitColumn          = input.readShort();
			
			final League[] leagues = League.values();
			
			profile.achievementPoints       = input.readInt();
			profile.terranWins              = input.readInt();
			profile.zergWins                = input.readInt();
			profile.protossWins             = input.readInt();
			profile.gamesThisSeason         = input.readInt();
			profile.totalCareerGames        = input.readInt();
			profile.highestSoloFinishLeague = leagues[ input.readByte() ]; 
			profile.highestTeamFinishLeague = leagues[ input.readByte() ]; 
			profile.highestSoloFinishTimes  = input.readInt();
			profile.highestTeamFinishTimes  = input.readInt();
			
			// Best ranks
			for ( int i = 0; i < profile.bestRanks.length; i++ ) {
				if ( input.readBoolean() ) {
					final BestTeamRank rank = profile.bestRanks[ i ] = new BestTeamRank();
					rank.league = leagues[ input.readByte() ];
					rank.divisionRank = input.readShort();
					final int teamMembersCount = input.readByte();
					if ( teamMembersCount > 0 ) {
						rank.teamMembers = new String[ teamMembersCount ];
						for ( int k = 0; k < teamMembersCount; k++ )
							rank.teamMembers[ k ] = input.readUTF();
					}
					rank.leagueClass   = input.readByte();
					rank.games         = input.readInt();
					rank.wins          = input.readInt();
					rank.gamesOfFormat = input.readInt();
				}
				else {
					profile.bestRanks[ i ] = null;
				}
			}
			
			// All ranks
			for ( int i = 0; i < profile.allRankss.length; i++ ) {
				final int count = input.readInt();
				if ( count > 0 ) {
					profile.allRankss[ i ] = new TeamRank[ count ];
					for ( int j = 0; j < count; j++ ) {
						final TeamRank rank = profile.allRankss[ i ][ j ] = new TeamRank();
						rank.league = leagues[ input.readByte() ];
						rank.divisionRank = input.readShort();
						rank.teamMembers = new String[ input.readByte() ];
						for ( int k = 0; k < rank.teamMembers.length; k++ )
							rank.teamMembers[ k ] = input.readUTF();
					}
				}
			}
			
			return profile;
			
		} catch ( final Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Caches a profile: saves the profile in the cache.<br>
	 * If any error occurs, returns silently.
	 * @param profile  profile to be cached
	 * @param playerId player identifier
	 */
	private static void cacheProfile( final Profile profile, final IPlayerId playerId ) {
		final File cacheFolder = getCacheFolder( playerId );
		if ( !cacheFolder.exists() )
			if ( !cacheFolder.mkdirs() )
				return;
		
		final File cacheFile = new File( cacheFolder, Integer.toHexString( playerId.getBattleNetId() ) );
		
		try ( final DataOutputStream output = new DataOutputStream( new FileOutputStream( cacheFile ) ) ) {
			output.writeShort( CACHE_VERSION                             );
			
			output.writeLong ( profile.updatedAt              .getTime() );
			output.writeLong ( profile.allRanksUpdatedAt      .getTime() );
			
			output.writeShort( profile.portraitGroup                     );
			output.writeShort( profile.portraitRow                       );
			output.writeShort( profile.portraitColumn                    );
			
			output.writeInt  ( profile.achievementPoints                 );
			output.writeInt  ( profile.terranWins                        );
			output.writeInt  ( profile.zergWins                          );
			output.writeInt  ( profile.protossWins                       );
			output.writeInt  ( profile.gamesThisSeason                   );
			output.writeInt  ( profile.totalCareerGames                  );
			output.writeByte ( profile.highestSoloFinishLeague.ordinal() );
			output.writeByte ( profile.highestTeamFinishLeague.ordinal() );
			output.writeInt  ( profile.highestSoloFinishTimes            );
			output.writeInt  ( profile.highestTeamFinishTimes            );
			
			// Best ranks
			for ( final BestTeamRank rank : profile.bestRanks ) {
				output.writeBoolean( rank != null );
				if ( rank != null ) {
					output.writeByte ( rank.league.ordinal() );
					output.writeShort( rank.divisionRank     );
					output.writeByte ( rank.teamMembers == null ? 0 : rank.teamMembers.length );
					if ( rank.teamMembers != null )
						for ( final String teamMate : rank.teamMembers )
							output.writeUTF( teamMate );
					output.writeByte( rank.leagueClass   );
					output.writeInt ( rank.games         );
					output.writeInt ( rank.wins          );
					output.writeInt ( rank.gamesOfFormat );
				}
			}
			
			// All ranks
			for ( final TeamRank[] ranks : profile.allRankss ) {
				if ( ranks == null ) {
					output.writeInt( 0 );
					continue;
				}
				output.writeInt( ranks.length );
				for ( final TeamRank rank : ranks ) {
					output.writeByte ( rank.league.ordinal() );
					output.writeShort( rank.divisionRank     );
					output.writeByte ( rank.teamMembers.length );
					for ( final String teamMate : rank.teamMembers )
						output.writeUTF( teamMate );
				}
			}
			
			output.flush();
			
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Queries the custom portrait for the specified player.
	 * 
	 * <p>Custom portraits are retrieved asynchronously in a new thread. If the retrieval of a custom portrait fails,
	 * the custom portrait listener will be called with a <code>null</code> value.</p>
	 * 
	 * <p>If a custom portrait is not defined for the specified player, the custom portrait listener will not be called.<br>
	 * If the custom portrait is cached, it will be passed to the custom portrait listener immediately.<br>
	 * If the custom portrait is not cached, it will be retrieved and the custom profile listener will be called
	 * with the custom portrait.</p>
	 * 
	 * <p>You can read more about the custom portraits: <a href="https://sites.google.com/site/sc2gears/custom-portraits">Custom portraits</a></p>
	 * 
	 * @param playerId               player identifier
	 * @param highRes                tells if high resolution version is required; normal resolution is 45x45, high resolution is 90x90
	 * @param customPortraitListener custom portrait listener to be called with the results
	 * @return {@link Boolean#TRUE} if the custom portrait is available right away; {@link Boolean#FALSE} if it is being downloaded; <code>null</code> if no custom portrait is defined for the specified player
	 */
	public static Boolean queryCustomPortrait( final IPlayerId playerId, final boolean highRes, final CustomPortraitListener customPortraitListener ) {
		// If custom portrait list is older than 1 day, attempt to re-download it
		if ( CUSTOM_PORTRAIT_LIST_UPDATED_AT.getTime() + 24L*60*60*1000 < System.currentTimeMillis() ) {
			if ( !downloadCustomPortraitList() ) // Reset the time if download fails, else all subsequent call will try to re-download it again
				CUSTOM_PORTRAIT_LIST_UPDATED_AT.setTime( System.currentTimeMillis() );
		}
		
		final CustomPortraitInfo customPortraitInfo = USER_ID_CUSTOM_PORTRAIT_MAP.get( playerId.getGateway().binaryValue + "-" + playerId.getBattleNetSubId() + "-" + playerId.getBattleNetId() );
		
		if ( customPortraitInfo == null )
			return null;
		
		final File customPortraitFile = new File( getCacheFolder( playerId ), ( highRes ? "cp90-" : "cp45-" ) + customPortraitInfo.version + "-" + Integer.toHexString( playerId.getBattleNetId() ) );
		
		if ( customPortraitFile.exists() ) {
			customPortraitListener.customPortraitReady( new ImageIcon( customPortraitFile.getAbsolutePath() ) );
			return Boolean.TRUE;
		}
		else {
			new Downloader( customPortraitInfo.getCustomPortraitUrl( highRes ), customPortraitFile, false, new DownloaderCallback() {
				@Override
				public void downloadFinished( final boolean success ) {
					customPortraitListener.customPortraitReady( success ? new ImageIcon( customPortraitFile.getAbsolutePath() ) : null );
				}
			} ).start();
			return Boolean.FALSE;
		}
	}
	
	/**
	 * Returns the cache folder for the specified player.
	 * @param playerId player identifier
	 * @return the cache folder for the specified player
	 */
	private static File getCacheFolder( final IPlayerId playerId ) {
		return new File( Consts.FOLDER_PROFILE_CACHE, playerId.getGateway().binaryValue + "-" + playerId.getBattleNetSubId() + "/" + getByteHashOfInt( playerId.getBattleNetId() ) );
	}
	
	/**
	 * Returns the hexadecimal string representation of a one-byte hash of the specified int number.<br>
	 * This implementation calculates the XOR of the bytes of the specified int.
	 * @param n number whose one-byte hash to be returned
	 * @return the hexadecimal string representation of a one-byte hash of the specified int number.
	 */
	private static String getByteHashOfInt( int n ) {
		int hash = n & 0xff;
		for ( int i = 0; i < 3; i++ ) // 3 more bytes in an int
			hash ^= ( n >>= 8 ) & 0xff;
		
		return String.format( "%02x", hash );
	}
	
	/**
	 * Empties the profile cache: deletes all cached profiles.<br>
	 * Displays a modal info dialog about the fact that the cache is being emptied.
	 * @param owner optional owner of the info dialog
	 */
	public static void emptyCache( final Dialog owner ) {
		emptyCache_( "Profile", owner, "miscSettings.clearingProfileCache", Consts.FOLDER_PROFILE_CACHE );
	}
	
}
