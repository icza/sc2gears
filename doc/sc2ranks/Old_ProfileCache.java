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
import hu.belicza.andras.sc2gears.sc2replay.ReplayConsts.Gateway;
import hu.belicza.andras.sc2gears.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gears.sc2replay.ReplayConsts.Region;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.PlayerId;
import hu.belicza.andras.sc2gears.services.Downloader;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.icons.Icons.League;
import hu.belicza.andras.sc2gears.util.ProfileCache.Profile.TeamRank;
import hu.belicza.andras.sc2gearspluginapi.api.listener.DownloaderCallback;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A utility class responsible to cache profile info and to read profile info from cache.
 * 
 * <p>Powered by <a href="http://sc2ranks.com">Sc2ranks.com</a>.<br>
 * API: <a href="http://sc2ranks.com/api">http://sc2ranks.com/api</a><p/>
 * 
 * @author Andras Belicza
 */
public class ProfileCache extends FileBasedCache {
	
	/** Version of the cache data format. */
	public static final short CACHE_VERSION = 1;
	
	/** Maps from Sc2ranks.com league string value to {@linkplain League}. */
	private static Map< String, League > SC2RANKS_LEAGE_LEAGE_MAP = new HashMap< String, League >();
	static {
		SC2RANKS_LEAGE_LEAGE_MAP.put( "bronze"     , League.BRONZE      );
		SC2RANKS_LEAGE_LEAGE_MAP.put( "silver"     , League.SILVER      );
		SC2RANKS_LEAGE_LEAGE_MAP.put( "gold"       , League.GOLD        );
		SC2RANKS_LEAGE_LEAGE_MAP.put( "platinum"   , League.PLATINUM    );
		SC2RANKS_LEAGE_LEAGE_MAP.put( "diamond"    , League.DIAMOND     );
		SC2RANKS_LEAGE_LEAGE_MAP.put( "master"     , League.MASTER      );
		SC2RANKS_LEAGE_LEAGE_MAP.put( "grandmaster", League.GRANDMASTER );
	}
	
	/** Maps from Sc2ranks.com race string value to {@linkplain Race}. */
	private static Map< String, Race > SC2RANKS_RACE_RACE_MAP = new HashMap< String, Race >();
	static {
		SC2RANKS_RACE_RACE_MAP.put( "protoss", Race.PROTOSS );
		SC2RANKS_RACE_RACE_MAP.put( "terran" , Race.TERRAN  );
		SC2RANKS_RACE_RACE_MAP.put( "zerg"   , Race.ZERG    );
		SC2RANKS_RACE_RACE_MAP.put( "random" , Race.RANDOM  );
	}
	
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
	 * Profile info of a player.
	 * 
	 * @author Andras Belicza
	 */
	public static class Profile {
		
		public static class TeamRank implements Comparable< TeamRank > {
			public League  league;
			public int     divisionRank;
			public boolean isRandom;
			public int     points;
			public int     wins;
			public int     losses;
			public Race    race;
			public int     worldRank;
			public int     regionRank;
			
			/**
			 * Defines a rank order:
			 * <ol>
			 * 	<li>higher leagues before lower ones
			 * 	<li>lower ranks before the higher ones
			 * 	<li>higher points before lower ones
			 * </ol>
			 */
			@Override
			public int compareTo( final TeamRank t ) {
				final int leagueComp = league.compareTo( t.league );
				if ( leagueComp != 0 )
					return leagueComp;
				
				if ( divisionRank != t.divisionRank )
					return divisionRank - t.divisionRank;
				
				return t.points - points;
			}
		}
		
		public final Date retrievedAt;
		public Date updatedAt;
		
		public int portraitGroup;
		public int portraitRow;
		public int portraitColumn;
		
		public int achievementPoints;
		
		// 1st index: bracket (1v1 => 0, 2v2 => 1, 3v3 => 2, 4v4 => 3)
		/** Team ranks are ordered in the same bracket, highest is the first. */
		public final TeamRank[][] teamRankss = new TeamRank[ 4 ][];
		
		public Profile() {
			this.retrievedAt = new Date();
		}
		
		public Profile( final Date retrievedAt ) {
			this.retrievedAt = retrievedAt;
		}
		
	}
	
	/**
	 * A profile listener for asynchronous profile requests.
	 * @author Andras Belicza
	 */
	public static interface ProfileListener {
		void profileReady( final Profile profile, final boolean isAnotherRetrievingInProgress );
	}
	
	/**
	 * A custom portrait listener for asynchronous custom portrait requests.
	 * @author Andras Belicza
	 */
	public static interface CustomPortraitListener {
		void customPortraitReady( final ImageIcon customPortrait );
	}
	
	/**
	 * No need to instantiate this class.
	 */
	private ProfileCache() {
	}
	
	/**
	 * Calls and returns {@link #queryProfile(PlayerId, ProfileListener, boolean)} with <code>forceRetrieve=false</code>.
	 */
	public static boolean queryProfile( final PlayerId playerId, final ProfileListener profileListener ) {
		return queryProfile( playerId, profileListener, false );
	}
	
	/**
	 * Asynchronous profile get method.<br>
	 * Queries the profile of the specified player. If the profile is cached, it will be passed to the profile listener immediately.
	 * If the profile is not cached, or is out-dated, it will be retrieved (again) and the profile listener will be called (again)
	 * with the new profile.<br>
	 * New profiles are retrieved asynchronously in a new thread. If the retrieval of a new profile fails and no cached, out-dated version is available,
	 * the profile listener will be called with a <code>null</code> value.
	 * 
	 * @param playerId        player identifier
	 * @param profileListener listener to be called if the profile is retrieved asynchronously
	 * @param forceRetrieve   forces retrieving the profile even if its validity time is not over yet; also only calls {@link ProfileListener#profileReady(Profile)} if the new profile is ready
	 * @return true if a profile info is available right away; false otherwise
	 */
	public static boolean queryProfile( final PlayerId playerId, final ProfileListener profileListener, final boolean forceRetrieve ) {
		if ( playerId.gateway == Gateway.UNKNOWN || playerId.gateway == Gateway.PUBLIC_TEST || playerId.battleNetId == 0 ) {
			profileListener.profileReady( null, false );
			return true;
		}
		
		final Profile profile = readCachedProfile( playerId );
		
		final boolean haveToRetrieve = profile == null || forceRetrieve || profile.retrievedAt.getTime() + Settings.getInt( Settings.KEY_SETTINGS_MISC_PROFILE_INFO_VALIDITY_TIME ) * 24L*60*60*1000 < System.currentTimeMillis();
		if ( profile != null && !forceRetrieve )
			profileListener.profileReady( profile, haveToRetrieve );
		
		if ( haveToRetrieve ) {
			new NormalThread( "Profile retriever" ) {
				@Override
				public void run() {
					final Profile newProfile = retreiveProfile( playerId );
					if ( newProfile != null )
						cacheProfile( newProfile, playerId );
					// If old profile is null, call profileReady() always
					// If old profile is not null, only call profileReady() if new profile is not null
					if ( newProfile != null || profile == null )
						profileListener.profileReady( newProfile, false );
				}
			}.start();
		}
		
		return profile != null && !forceRetrieve;
	}
	
	/**
	 * Retrieves a profile from Battle.net.
	 * 
	 * @param playerId player identifier
	 * @return the retrieved profile
	 */
	public static Profile retreiveProfile2( final PlayerId playerId ) {
		// TODO
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments( true );
		
		// By default implementation attempts to download the DTD which is not available (and not our intent), disable it: 
		final DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch ( final ParserConfigurationException pce ) {
			// Never to happen
			pce.printStackTrace();
			return null;
		}
		builder.setEntityResolver( new EntityResolver() {
			@Override
			public InputSource resolveEntity( String publicId, String systemId ) throws SAXException, IOException {
				return new InputSource( new StringReader( "" ) );
			}
		} );
		
		final Profile profile = new Profile();
		
		// First character info
		try {
			final Document document = builder.parse( new File( "i:/backup/workspace/Sc2gears/doc/bnet profiles/SCIIGears.htm" ) );
			//final Document document = builder.parse( new File( "i:/backup/workspace/Sc2gears/doc/bnet profiles/PlayerCraft.htm" ) );
			//final Document document = builder.parse( new File( "i:/backup/workspace/Sc2gears/doc/bnet profiles/Kukulza.htm" ) );
			
			//final Document document = builder.parse( new URL( "http://eu.battle.net/sc2/en/profile/206154/1/SCIIGears/").toURI().toString() );
			//final Document document = builder.parse( new URL( playerId.getBnetProfileUrl( null ) ).toURI().toString() );
			
			final NodeList divList   = document.getElementsByTagName( "div" );
			final int      divsCount = divList.getLength();
			for ( int i = 0; i < divsCount; i++ ) {
				final Element div   = (Element) divList.item( i );
				final String  divId = div.getAttribute( "id" );
				if ( divId.length() == 0 )
					continue;
				System.out.println( divId );
				
				if ( "portrait".equals( divId ) ) {
					final String portraitStyle = ( (Element) div.getElementsByTagName( "span" ).item( 0 ) ).getAttribute( "style" );
					// "url('/sc2/static/local-common/images/sc2/portraits/3-90.jpg?v21') -270px -270px no-repeat; width: 90px; height: 90px;"
					final int urlStartPos = portraitStyle.indexOf( "url('" );
					final int urlEndPos   = portraitStyle.indexOf( '\'', urlStartPos + 5 );
					
					profile.portraitGroup = portraitStyle.charAt( portraitStyle.lastIndexOf( '/', urlEndPos ) + 1 ) - '0';
					
					final int xoffsetPos = portraitStyle.indexOf( '-', urlEndPos );
					final int yoffsetPos = portraitStyle.indexOf( '-', xoffsetPos + 1 );
					
					profile.portraitColumn = Integer.parseInt( portraitStyle.substring( xoffsetPos + 1, portraitStyle.indexOf( "px", xoffsetPos ) ) ) / 90;
					profile.portraitRow    = Integer.parseInt( portraitStyle.substring( yoffsetPos + 1, portraitStyle.indexOf( "px", yoffsetPos ) ) ) / 90;
					
					System.out.println( "[portrait] " + profile.portraitGroup + ";" + profile.portraitColumn + ";" + profile.portraitRow );
				} else if ( "profile-header".equals( divId ) ) {
					profile.achievementPoints = Integer.parseInt( ( (Element) div.getElementsByTagName( "h3" ).item( 0 ) ).getTextContent().trim() );
					
					System.out.println( "[achievement points] " + profile.achievementPoints );
				} else if ( divId.startsWith( "best-team-" ) ) {
					final int team = divId.charAt( divId.length() - 1 ) - '1';
					profile.teamRankss[ team ] = new TeamRank[] { new TeamRank() }; // TODO only best leages are retrieved now
					final TeamRank teamRank = profile.teamRankss[ team ][ 0 ];
					teamRank.divisionRank = Integer.parseInt( ( (Element) div.getElementsByTagName( "div" ).item( 1 ) ).getLastChild().getTextContent().trim() );
					
					final Element ladder = (Element) div.getParentNode();
					
					// TODO check UNRANKED
					final String badgeClass = ( (Element) ( (Element) ladder.getElementsByTagName( "a" ).item( 0 ) ).getElementsByTagName( "span" ).item( 0 ) ).getAttribute( "class" );
					for ( final League league : League.values() ) // Order is important: "grandmaster" must be checked first (because "master" is contained in "grandmaster" too!)
						if ( badgeClass.contains( league.bnetString ) ) {
							teamRank.league = league;
							break;
						}
					System.out.println( teamRank.league );
					
					final Element snapshot = (Element) ladder.getParentNode();
					final Element bars     = (Element) snapshot.getElementsByTagName( "div" ).item( 2 ); // { ladder, division, bars, ... }
					final Element totals   = (Element) ( (Element) ( (Element) bars.getElementsByTagName( "div" ).item( 0 ) ).getElementsByTagName( "div" ).item( 0 ) ).getElementsByTagName( "span" ).item( 0 );
					// "5 Wins"
					
					teamRank.wins = 0;
					teamRank.losses = 0;
					teamRank.points = 0;
					//teamRank.race = Race.UNKNOWN;
					//teamRank.isRandom = false;
				}
				// TODO parse "League wins"
			}
			
		} catch ( final Exception e ) {
			e.printStackTrace();
			return null;
		}
		
		// Now team and league info
		// First character info
		try {
			final Document document = builder.parse( new File( "i:/backup/workspace/Sc2gears/doc/bnet profiles/SCIIGears leagues.htm" ) );
			//final Document document = builder.parse( new URL( playerId.getBnetProfileUrl( null ) + "/ladder/leagues" ).toURI().toString() );
			
		} catch ( final Exception e ) {
			e.printStackTrace();
			return null;
		}
		
		return profile;
	}
	
	/**
	 * Retrieves a profile from Sc2ranks.com.
	 * 
	 * @param playerId player identifier
	 * @return the retrieved profile
	 */
	private static Profile retreiveProfile( final PlayerId playerId ) {
		BufferedReader input = null;
		
		try {
			final String url = "http://sc2ranks.com/api/base/teams/" + Region.getFromGatewayAndSubId( playerId.gateway, playerId.battleNetSubid ).sc2ranksId
				+ "/" + URLEncoder.encode( playerId.name, "UTF-8" ) + "!" + playerId.battleNetId + ".json?appKey=sc2gears.application";
			
			final URLConnection connection = new URL( url ).openConnection();
			connection.setRequestProperty( "Accept-Charset", "UTF-8" );
			input = new BufferedReader( new InputStreamReader( connection.getInputStream(), "UTF-8" ) );
			
			final StringBuilder responseBuilder = new StringBuilder();
			
			String line;
			while ( ( line = input.readLine() ) != null )
				responseBuilder.append( line );
			
			/*
			 * Example:
			 * {"bnet_id":206154,"updated_at":"2011-05-10T07:28:24Z","achievement_points":1025,"region":"eu","portrait":{"icon_id":2,"column":2,"row":3},"name":"SCIIGears",
			 * "teams":[{"world_rank":18889,"division":"Division Duke Epsilon","division_rank":49,"ratio":"1.00","league":"platinum","fav_race":"random","updated_at":"2011-05-09T21:50:40Z","region_rank":5740,"bracket":3,"wins":2,"is_random":false,"losses":0,"points":20}
			 *     ,{"world_rank":37436,"division":"Division Void Ray Lambda","division_rank":95,"ratio":"1.00","league":"diamond","fav_race":"random","updated_at":"2011-05-10T07:13:14Z","region_rank":12097,"bracket":2,"wins":3,"is_random":false,"losses":0,"points":0},{"world_rank":11637,"division":"Division Khaydarin Charlie","division_rank":26,"ratio":"1.00","league":"diamond","fav_race":"random","updated_at":"2011-05-10T07:28:17Z","region_rank":3295,"bracket":2,"wins":25,"is_random":true,"losses":0,"points":189},{"world_rank":37436,"division":"Division Zeratul Gravity","division_rank":89,"ratio":"1.00","league":"diamond","fav_race":"random","updated_at":"2011-05-10T11:48:06Z","region_rank":12097,"bracket":2,"wins":1,"is_random":false,"losses":0,"points":0},{"world_rank":5512,"division":"Division Corsair Upsilon","division_rank":42,"ratio":"1.00","league":"master","fav_race":"random","updated_at":"2011-05-10T01:36:17Z","region_rank":1658,"bracket":4,"wins":9,"is_random":true,"losses":0,"points":139},{"world_rank":77416,"division":"Division Hanson Psi","division_rank":87,"ratio":"1.00","league":"diamond","fav_race":"random","updated_at":"2011-05-10T04:49:23Z","region_rank":22437,"bracket":1,"wins":3,"is_random":false,"losses":0,"points":0},{"world_rank":2678,"division":"Division Gantrithor Upsilon","division_rank":1,"ratio":"1.00","league":"diamond","fav_race":"random","updated_at":"2011-05-08T02:16:31Z","region_rank":725,"bracket":3,"wins":46,"is_random":true,"losses":0,"points":586},{"world_rank":37436,"division":"Division Pylon Mars","division_rank":90,"ratio":"1.00","league":"diamond","fav_race":"random","updated_at":"2011-05-09T17:47:50Z","region_rank":12097,"bracket":2,"wins":1,"is_random":false,"losses":0,"points":0}],
			 * "id":221850,"character_code":387}
			 */
			
			final JSONObject jsonObject = new JSONObject( responseBuilder.toString() );
			
			// Check if we got back valid info
			try {
				jsonObject.getString( "error" );
				// No exception: error value exists => error
				return null;
			} catch ( final JSONException je ) {
				// Everything is ok
			}
			
			final Profile profile = new Profile();
			
			// TODO probably Z means timezone=0, so it should be parsed that way
			profile.updatedAt = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" ).parse( jsonObject.getString( "updated_at" ) );
			profile.achievementPoints = jsonObject.getInt( "achievement_points" );
			
			final JSONObject portrait = jsonObject.optJSONObject( "portrait" );
			if ( portrait != null ) {
				profile.portraitGroup  = portrait.getInt( "icon_id" );
				profile.portraitColumn = portrait.getInt( "column" );
				profile.portraitRow    = portrait.getInt( "row" );
			}
			else {
				// Default portrait
				profile.portraitGroup  = 0;
				profile.portraitColumn = 0;
				profile.portraitRow    = 0;
			}
			
			final JSONArray teamsJson = jsonObject.getJSONArray( "teams" );
			
			@SuppressWarnings("unchecked")
			final List< TeamRank >[] teamRanksLists = new List[ profile.teamRankss.length ];
			for ( int i = 0; i < teamRanksLists.length; i++ )
				teamRanksLists[ i ]  = new ArrayList< TeamRank >();
			
			final int teamsCount = teamsJson.length();
			for ( int i = 0; i < teamsCount; i++ ) {
				final JSONObject teamJson = teamsJson.getJSONObject( i );
				
				final TeamRank teamRank = new TeamRank();
				teamRank.worldRank    = teamJson.getInt( "world_rank" );
				teamRank.divisionRank = teamJson.getInt( "division_rank" );
				teamRank.league       = SC2RANKS_LEAGE_LEAGE_MAP.get( teamJson.getString( "league" ) );
				if ( teamRank.league == null )
					teamRank.league = League.UNKNOWN;
				teamRank.race         = SC2RANKS_RACE_RACE_MAP.get( teamJson.getString( "fav_race" ) );
				if ( teamRank.race == null )
					teamRank.race = Race.UNKNOWN;
				
				teamRank.regionRank = teamJson.getInt( "region_rank" );
				
				final int bracketIdx = teamJson.getInt( "bracket" ) - 1;
				if ( bracketIdx >= teamRanksLists.length )
					continue; // For example 5v5...
				
				teamRank.wins         = teamJson.getInt( "wins" );
				teamRank.isRandom     = teamJson.getBoolean( "is_random" );
				teamRank.losses       = teamJson.getInt( "losses" );
				teamRank.points       = teamJson.getInt( "points" );
				
				teamRanksLists[ bracketIdx ].add( teamRank );
			}
			
			for ( int i = 0; i < teamRanksLists.length; i++ ) {
				profile.teamRankss[ i ] = teamRanksLists[ i ].toArray( new TeamRank[ teamRanksLists[ i ].size() ] );
				Arrays.sort( profile.teamRankss[ i ] );
			}
			
			return profile;
			
		} catch ( final Exception e ) {
			e.printStackTrace();
		} finally {
			if ( input != null )
				try { input.close(); } catch ( final Exception e ) {}
		}
		
		return null;
	}
	
	/**
	 * Tries to read a profile from the cache.
	 * @param playerId player identifier
	 * @return the profile if cached; or <code>null</code> if the profile is not cached
	 */
	private static Profile readCachedProfile( final PlayerId playerId ) {
		final File cacheFile = new File( getCacheFolder( playerId ), Integer.toHexString( playerId.battleNetId ) );
		if ( !cacheFile.exists() )
			return null;
		
		DataInputStream input = null;
		try {
			
			input = new DataInputStream( new FileInputStream( cacheFile ) );
			
			final short cacheFileVersion = input.readShort();
			if ( cacheFileVersion != CACHE_VERSION )
				return null;
			
			final Profile profile = new Profile( new Date( input.readLong() ) );
			
			profile.updatedAt = new Date( input.readLong() );
			
			profile.portraitGroup  = input.readShort();
			profile.portraitRow    = input.readShort();
			profile.portraitColumn = input.readShort();
			
			profile.achievementPoints = input.readInt();
			
			final League[] leagues = League.values();
			final Race  [] races   = Race  .values();
			for ( int i = 0; i < profile.teamRankss.length; i++ ) {
				final TeamRank[] teamRanks = profile.teamRankss[ i ] = new TeamRank[ input.readInt() ];
				for ( int j = 0; j < teamRanks.length; j++ ) {
					final TeamRank teamRank = teamRanks[ j ] = new TeamRank();
					teamRank.league       = leagues[ input.readByte() ];
					teamRank.divisionRank = input.readShort();
					teamRank.isRandom     = input.readBoolean();
					teamRank.points       = input.readInt();
					teamRank.wins         = input.readInt();
					teamRank.losses       = input.readInt();
					teamRank.race         = races[ input.readByte() ];
					teamRank.worldRank    = input.readInt();
					teamRank.regionRank   = input.readInt();
				}
			}
			
			return profile;
			
		} catch ( final Exception e ) {
			e.printStackTrace();
			return null;
		} finally {
			if ( input != null )
				try { input.close(); } catch ( final Exception e ) {}
		}
	}
	
	/**
	 * Caches a profile: saves the profile in the cache.<br>
	 * If any error occurs, returns silently.
	 * @param profile  profile to be cached
	 * @param playerId player identifier
	 */
	private static void cacheProfile( final Profile profile, final PlayerId playerId ) {
		final File cacheFolder = getCacheFolder( playerId );
		if ( !cacheFolder.exists() )
			if ( !cacheFolder.mkdirs() )
				return;
		
		final File cacheFile = new File( cacheFolder, Integer.toHexString( playerId.battleNetId ) );
		
		DataOutputStream output = null;
		try {
			
			output = new DataOutputStream( new FileOutputStream( cacheFile ) );
			
			output.writeShort( CACHE_VERSION );
			
			output.writeLong( profile.retrievedAt.getTime() );
			output.writeLong( profile.updatedAt.getTime() );
			
			output.writeShort( profile.portraitGroup  );
			output.writeShort( profile.portraitRow    );
			output.writeShort( profile.portraitColumn );
			
			output.writeInt( profile.achievementPoints );
			
			for ( int i = 0; i < profile.teamRankss.length; i++ ) {
				final TeamRank[] teamRanks = profile.teamRankss[ i ];
				output.writeInt( teamRanks.length );
				for ( int j = 0; j < teamRanks.length; j++ ) {
					final TeamRank teamRank = teamRanks[ j ];
					output.writeByte   ( teamRank.league.ordinal() );
					output.writeShort  ( teamRank.divisionRank     );
					output.writeBoolean( teamRank.isRandom         );
					output.writeInt    ( teamRank.points           );
					output.writeInt    ( teamRank.wins             );
					output.writeInt    ( teamRank.losses           );
					output.writeByte   ( teamRank.race.ordinal()   );
					output.writeInt    ( teamRank.worldRank        );
					output.writeInt    ( teamRank.regionRank       );
				}
			}
			
			output.flush();
			
		} catch ( final Exception e ) {
			e.printStackTrace();
		} finally {
			if ( output != null )
				try { output.close(); } catch ( final Exception e ) {}
		}
	}
	
	/**
	 * Queries the custom portrait for the specified player.<br>
	 * If a custom portrait is not defined for the specified player, the custom portrait listener will not be called.<br>
	 * If the custom portrait is cached, it will be passed to the custom portrait listener immediately.
	 * If the custom portrait is not cached, it will be retrieved and the custom profile listener will be called
	 * with the custom portrait.<br>
	 * Custom portraits are retrieved asynchronously in a new thread. If the retrieval of a custom portrait fails,
	 * the custom portrait listener will be called with a <code>null</code> value.
	 * 
	 * @param playerId               player identifier
	 * @param highRes                tells if high resolution version is required
	 * @param customPortraitListener custom portrait listener to be called with the results
	 * @return <code>Boolean.TRUE</code> if the custom portrait is available right away; <code>Boolean.FALSE</code> if it is being downloaded; <code>null</code> if no custom portrait is defined for the specified player
	 */
	public static Boolean queryCustomPortrait( final PlayerId playerId, final boolean highRes, final CustomPortraitListener customPortraitListener ) {
		// If custom portrait list is older than 1 day, attempt to re-download it
		if ( CUSTOM_PORTRAIT_LIST_UPDATED_AT.getTime() + 24L*60*60*1000 < System.currentTimeMillis() ) {
			if ( !downloadCustomPortraitList() ) // Reset the time if download fails, else all subsequent call will try to re-download it again
				CUSTOM_PORTRAIT_LIST_UPDATED_AT.setTime( System.currentTimeMillis() );
		}
		
		final CustomPortraitInfo customPortraitInfo = USER_ID_CUSTOM_PORTRAIT_MAP.get( playerId.gateway.binaryValue + "-" + playerId.battleNetSubid + "-" + playerId.battleNetId );
		
		if ( customPortraitInfo == null )
			return null;
		
		final File customPortraitFile = new File( getCacheFolder( playerId ), ( highRes ? "cp90-" : "cp45-" ) + customPortraitInfo.version + "-" + Integer.toHexString( playerId.battleNetId ) );
		
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
	private static File getCacheFolder( final PlayerId playerId ) {
		return new File( Consts.FOLDER_PROFILE_CACHE, playerId.gateway.binaryValue + "-" + playerId.battleNetSubid + "/" + getByteHashOfInt( playerId.battleNetId ) );
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
