/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.util;

import hu.belicza.andras.sc2gears.sc2replay.EnumCache;
import hu.belicza.andras.sc2gearspluginapi.api.enums.League;
import hu.belicza.andras.sc2gearspluginapi.api.profile.IBestTeamRank;
import hu.belicza.andras.sc2gearspluginapi.api.profile.IProfile;
import hu.belicza.andras.sc2gearspluginapi.api.profile.ITeamRank;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerId;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BnetLanguage;
import hu.belicza.andras.util.BnetUtils.Profile.BestTeamRank;
import hu.belicza.andras.util.BnetUtils.Profile.TeamRank;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Battle.net parsing utility.
 * 
 * @author Andras Belicza
 */
public class BnetUtils {
	
	/**
	 * Profile info of a player.
	 * 
	 * @author Andras Belicza
	 */
	public static class Profile implements IProfile {
		
		public static class TeamRank implements ITeamRank {
			public League   league;
			public int      divisionRank;
			public String[] teamMembers;
			
			@Override
			public League getLeague() {
				return league;
			}
			
			@Override
			public int getDivisionRank() {
				return divisionRank;
			}
			
			@Override
			public String[] getTeamMembers() {
				return teamMembers;
			}
			
			/**
			 * Defines a rank order:
			 * <ol>
			 * 	<li>higher leagues before lower ones
			 * 	<li>lower ranks before the higher ones
			 * </ol>
			 */
			@Override
			public int compareTo( final ITeamRank t ) {
				final int leagueComp = league.compareTo( t.getLeague() );
				if ( leagueComp != 0 )
					return leagueComp;
				
				return divisionRank - t.getDivisionRank();
			}
			
			@Override
			public String getTeamMembersString() {
				if ( teamMembers == null )
					return "";
				final StringBuilder builder = new StringBuilder();
				for ( final String member : teamMembers ) {
					if ( builder.length() > 0 )
						builder.append( ", " );
					builder.append( member );
				}
				return builder.toString();
			}
		}
		
		public static class BestTeamRank extends TeamRank implements IBestTeamRank {
			public int leagueClass;
			public int games;
			public int wins;
			public int gamesOfFormat;
			
			@Override
			public int getLeagueClass() {
				return leagueClass;
			}
			
			@Override
			public int getGames() {
				return games;
			}
			
			@Override
			public int getWins() {
				return wins;
			}
			
			@Override
			public int getGamesOfFormat() {
				return gamesOfFormat;
			}
		}
		
		public Date updatedAt;
		public Date allRanksUpdatedAt; // 0 time value indicates that all ranks are not retrieved
		
		public int portraitGroup;
		public int portraitRow;
		public int portraitColumn;
		
		public int achievementPoints;
		public int terranWins;
		public int zergWins;
		public int protossWins;
		public int gamesThisSeason;
		public int totalCareerGames;
		
		public League highestSoloFinishLeague;
		public League highestTeamFinishLeague;
		public int    highestSoloFinishTimes;
		public int    highestTeamFinishTimes;
		
		/** Best ranks. */
		// 1st index: bracket (1v1 => 0, 2v2 => 1, 3v3 => 2, 4v4 => 3)
		public final BestTeamRank[] bestRanks = new BestTeamRank[ 4 ];
		
		/** Ranks are ordered in the same bracket, highest is the first. */
		// 1st index: bracket (1v1 => 0, 2v2 => 1, 3v3 => 2, 4v4 => 3)
		public final TeamRank[][] allRankss = new TeamRank[ 4 ][];
		
		public Profile() {
			updatedAt               = new Date();
			allRanksUpdatedAt       = new Date( 0 );
			highestSoloFinishLeague = League.UNKNOWN;
			highestTeamFinishLeague = League.UNKNOWN;
		}
		
		public Profile( final Date updatedAt ) {
			this.updatedAt = updatedAt;
		}
		
		@Override
		public Date getUpdatedAt() {
			return updatedAt;
		}
		
		@Override
		public Date getAllRanksUpdatedAt() {
			return allRanksUpdatedAt;
		}
		
		@Override
		public int getPortraitGroup() {
			return portraitGroup;
		}
		
		@Override
		public int getPortraitRow() {
			return portraitRow;
		}
		
		@Override
		public int getPortraitColumn() {
			return portraitColumn;
		}
		
		@Override
		public int getAchievementPoints() {
			return achievementPoints;
		}
		
		@Override
		public int getTerranWins() {
			return terranWins;
		}
		
		@Override
		public int getZergWins() {
			return zergWins;
		}
		
		@Override
		public int getProtossWins() {
			return protossWins;
		}
		
		@Override
		public int getGamesThisSeason() {
			return gamesThisSeason;
		}
		
		@Override
		public int getTotalCareerGames() {
			return totalCareerGames;
		}
		
		@Override
		public League getHighestSoloFinishLeague() {
			return highestSoloFinishLeague;
		}
		
		@Override
		public League getHighestTeamFinishLeague() {
			return highestTeamFinishLeague;
		}
		
		@Override
		public int getHighestSoloFinishTimes() {
			return highestSoloFinishTimes;
		}
		
		@Override
		public int getHighestTeamFinishTimes() {
			return highestTeamFinishTimes;
		}
		
		@Override
		public IBestTeamRank[] getBestRanks() {
			return bestRanks;
		}
		
		@Override
		public ITeamRank[][] getAllRankss() {
			return allRankss;
		}
		
	}
	
	/**
	 * Retrieves a profile from Battle.net.
	 * 
	 * <p>Either <code>playerId</code> or <code>inputStream</code> must be provided.</p>
	 * 
	 * @param playerId    optional player identifier
	 * @param inputStream optional input stream of the player profile XHTML document
	 * @return the retrieved profile; or <code>null</code> if retrieval failed
	 */
	public static Profile retrieveProfile( final IPlayerId playerId, final InputStream inputStream ) {
		final Profile profile = new Profile();
		
		// First character info
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments( true );
			
			// By default implementation attempts to download the DTD which is not available (and not our intent), disable it: 
			final DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver( new EntityResolver() {
				@Override
				public InputSource resolveEntity( String publicId, String systemId ) throws SAXException, IOException {
					return new InputSource( new StringReader( "" ) );
				}
			} );
			
			//final Document document = builder.parse( new File( "i:/backup/workspace/Sc2gears/doc/bnet profiles/SCIIGears.htm" ) );
			final Document document = playerId == null ? builder.parse( inputStream )
				: builder.parse( new URL( playerId.getBattleNetProfileUrl( BnetLanguage.ENGLISH ) ).toURI().toString() );
			
			// getElementById() cannot be used, always returns null,
			// because schema is not present which would define that the "id" is the ID attribute
			final NodeList divList   = document.getElementsByTagName( "div" );
			final int      divsCount = divList.getLength();
			for ( int i = 0; i < divsCount; i++ ) {
				final Element div   = (Element) divList.item( i );
				final String  divId = div.getAttribute( "id" );
				if ( divId.length() == 0 )
					continue;
				
				if ( "portrait".equals( divId ) ) {
					
					// Sometimes this span element is null, no portrait is displayed on the web page.
					// In this case we skip parsing portrait (default portrait will be displayed inside Sc2gears)
					// Example (at 2012-09-24: http://eu.battle.net/sc2/en/profile/2211317/1/Snesnigel/
					final Element spanElement = (Element) div.getElementsByTagName( "span" ).item( 0 );
					if ( spanElement !=null ) {
    					final String portraitStyle = spanElement.getAttribute( "style" );
    					
    					// Example: "background: url('/sc2/static/local-common/images/sc2/portraits/2-90.jpg?v42') -360px 0px no-repeat; width: 90px; height: 90px;"
    					final int urlStartPos = portraitStyle.indexOf( "url('" );
    					final int urlEndPos   = portraitStyle.indexOf( '\'', urlStartPos + 5 );
    					
    					profile.portraitGroup = portraitStyle.charAt( portraitStyle.lastIndexOf( '/', urlEndPos ) + 1 ) - '0';
    					
    					// Offset can also be "0px" (non-negative)!
    					final int xoffsetPos = portraitStyle.indexOf( ' ', urlEndPos  + 1);
    					final int yoffsetPos = portraitStyle.indexOf( ' ', xoffsetPos + 1 );
    					
    					profile.portraitColumn = Math.abs( Integer.parseInt( portraitStyle.substring( xoffsetPos + 1, portraitStyle.indexOf( "px", xoffsetPos ) ) ) ) / 90;
    					profile.portraitRow    = Math.abs( Integer.parseInt( portraitStyle.substring( yoffsetPos + 1, portraitStyle.indexOf( "px", yoffsetPos ) ) ) ) / 90;
					}
					
				} else if ( "profile-header".equals( divId ) ) {
					
					profile.achievementPoints = Integer.parseInt( ( (Element) div.getElementsByTagName( "h3" ).item( 0 ) ).getTextContent().trim() );
					
				} else if ( "season-snapshot".equals( divId ) ) {
					
					final NodeList seasonSnapshotSpans = div.getElementsByTagName( "span" );
					for ( int mode = 0; mode < 4; mode++ ) // Mode (1v1, 2v2, 3v3, 4v4)
						if ( seasonSnapshotSpans.getLength() > mode*3 + 2 ) {
							final String badgeClass = ( (Element) seasonSnapshotSpans.item( mode*3 + 1 ) ).getAttribute( "class" );
							final League league = getLeagueFromBadgeClass( badgeClass );
							if ( league != League.UNKNOWN && league != League.UNRANKED ) {
								profile.bestRanks[ mode ] = new BestTeamRank();
								profile.bestRanks[ mode ].league = league;
								profile.bestRanks[ mode ].leagueClass = badgeClass.charAt( badgeClass.length() - 1 ) - '0';
							}
						}
					
					int modeStatCount        = 0;
					int profileProgressCount = 0;
					final NodeList seasonSnapshotDivs = div.getElementsByTagName( "div" );
					for ( int j = 0; j < seasonSnapshotDivs.getLength(); j++ ) {
						final Element div_   = (Element) seasonSnapshotDivs.item( j );
						if ( "mode-stat".equals( div_.getAttribute( "class" ) ) ) {
							if ( profile.bestRanks[ modeStatCount ] != null ) { // Not unranked
								// 2 integer number here: games and wins.
								// Text content example: "&#160; - 2 Games | 1 Wins" or "&#160; – Игр: 2 | Побед: 1"
								int found = 0;
								for ( final StringTokenizer st = new StringTokenizer( div_.getTextContent() ); st.hasMoreTokens(); )
									try {
										final int num = Integer.parseInt( st.nextToken() );
										if ( found == 0 )
											profile.bestRanks[ modeStatCount ].games = num;
										else if ( found == 1 )
											profile.bestRanks[ modeStatCount ].wins = num;
										if ( ++found == 2 )
											break;
									} catch ( final NumberFormatException nfe ) {}
							}
							if ( ++modeStatCount == 4 && profileProgressCount == 4 ) {
								break;
							}
						}
						if ( "profile-progress".equals( div_.getAttribute( "class" ) ) ) {
							if ( profile.bestRanks[ profileProgressCount ] != null ) { // Not unranked
								// Total ladder games in this mode (not just the best team) / All ladder games
								// Example: "50 / 137"
								final String dataTooltip = div_.getAttribute( "data-tooltip" );
								// Try-catch because: a player may be GM ranked with no games played!
								try {
									profile.bestRanks[ profileProgressCount ].gamesOfFormat = Integer.parseInt( dataTooltip.substring( 0, dataTooltip.indexOf(' ' ) ) );
								} catch ( final Exception e ) {}
							}
							if ( ++profileProgressCount == 4 && modeStatCount == 4 ) {
								break;
							}
						}
					}
					
				} else if ( "career-stats".equals( divId ) ) {
					
					final NodeList careerStatsSpans = div.getElementsByTagName( "span" );
					if ( careerStatsSpans.getLength() > 1 )
						try {
							profile.terranWins = Integer.parseInt( careerStatsSpans.item( 1 ).getTextContent().trim() );
						} catch ( final NumberFormatException nfe ) {} // Not a number if no games have been played
					if ( careerStatsSpans.getLength() >= 4 )
						try {
							profile.zergWins = Integer.parseInt( careerStatsSpans.item( 4 ).getTextContent().trim() );
						} catch ( final NumberFormatException nfe ) {} // Not a number if no games have been played
					if ( careerStatsSpans.getLength() >= 7 )
						try {
							profile.protossWins = Integer.parseInt( careerStatsSpans.item( 7 ).getTextContent().trim() );
						} catch ( final NumberFormatException nfe ) {} // Not a number if no games have been played
					if ( careerStatsSpans.getLength() >= 10 )
						try {
							profile.totalCareerGames = Integer.parseInt( careerStatsSpans.item( 10 ).getTextContent().trim() );
						} catch ( final NumberFormatException nfe ) {} // Not a number if no games have been played
					if ( careerStatsSpans.getLength() >= 13 )
						try {
							profile.gamesThisSeason = Integer.parseInt( careerStatsSpans.item( 13 ).getTextContent().trim() );
						} catch ( final NumberFormatException nfe ) {} // Not a number if no games have been played
					
					if ( careerStatsSpans.getLength() >= 15 ) {
						final String badgeClass = ( (Element) careerStatsSpans.item( 15 ) ).getAttribute( "class" );
						profile.highestSoloFinishLeague = getLeagueFromBadgeClass( badgeClass );
					}
					if ( careerStatsSpans.getLength() >= 16 ) {
						final String badgeClass = ( (Element) careerStatsSpans.item( 16 ) ).getAttribute( "class" );
						profile.highestTeamFinishLeague = getLeagueFromBadgeClass( badgeClass );
					}
					
					final NodeList careerStatsDivs = div.getElementsByTagName( "div" );
					for ( int j = careerStatsDivs.getLength() - 1; j >= 0; j-- ) {
						final Element div_   = (Element) careerStatsDivs.item( j );
						if ( "best-finish-SOLO".equals( div_.getAttribute( "id" ) ) ) {
							final NodeList bfStrongs = div_.getElementsByTagName( "strong" );
							if ( bfStrongs.getLength() > 1 ) {
								try {
									profile.highestSoloFinishTimes = Integer.parseInt( bfStrongs.item( 1 ).getNextSibling().getTextContent().trim() );
								} catch ( final NumberFormatException nfe ) {
								}
							}
						} else if ( "best-finish-TEAM".equals( div_.getAttribute( "id" ) ) ) {
							final NodeList bfStrongs = div_.getElementsByTagName( "strong" );
							if ( bfStrongs.getLength() > 1 ) {
								try {
									profile.highestTeamFinishTimes = Integer.parseInt( bfStrongs.item( 1 ).getNextSibling().getTextContent().trim() );
								} catch ( final NumberFormatException nfe ) {
								}
							}
						}
					}
					
				}
			}
			
		} catch ( final Exception e ) {
			e.printStackTrace();
			return null;
		}
		
		return profile;
	}
	
	/**
	 * Retrieves extended profile info from Battle.net.
	 * 
	 * <p>Either <code>playerId</code> or <code>inputStream</code> must be provided.</p>
	 * 
	 * @param playerId    optional player identifier
	 * @param inputStream optional input stream of the player extended profile XHTML document
	 * @param profile     reference to the profile to add extended info to
	 * @return the profile; or <code>null</code> if retrieval failed
	 */
	public static Profile retrieveExtProfile( final IPlayerId playerId, final InputStream inputStream, final Profile profile ) {
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments( true );
			
			// By default implementation attempts to download the DTD which is not available (and not our intent), disable it: 
			final DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver( new EntityResolver() {
				@Override
				public InputSource resolveEntity( String publicId, String systemId ) throws SAXException, IOException {
					return new InputSource( new StringReader( "" ) );
				}
			} );
			
			//final Document document = builder.parse( new File( "i:/backup/workspace/Sc2gears/doc/bnet profiles/SCIIGears leagues.htm" ) );
			final Document document = playerId == null ? builder.parse( inputStream )
				: builder.parse( new URL( playerId.getBattleNetProfileUrl( BnetLanguage.ENGLISH ) + "ladder/leagues" ).toURI().toString() );
			
			@SuppressWarnings("unchecked")
			final List< TeamRank >[] teamRanksLists = new List[ profile.allRankss.length ];
			for ( int i = 0; i < teamRanksLists.length; i++ )
				teamRanksLists[ i ]  = new ArrayList< TeamRank >();
			
			final List< Integer > bracketList = new ArrayList< Integer >();
			
			final NodeList divList   = document.getElementsByTagName( "div" );
			final int      divsCount = divList.getLength();
			for ( int i = 0; i < divsCount; i++ ) {
				final Element div   = (Element) divList.item( i );
				final String  divId = div.getAttribute( "id" );
				if ( divId.length() == 0 )
					continue;
				
				if ( "profile-left".equals( divId ) ) {
					
					// This is where bracket info is stored
					final NodeList menuLinks = ( (Element) div.getElementsByTagName( "ul" ).item( 0 ) ).getElementsByTagName( "a" );
					final int menuItemsCount = menuLinks.getLength();
					for ( int j = 0; j < menuItemsCount; j++ ) {
						final Element menuLink = (Element) menuLinks.item( j );
						if ( menuLink.getAttribute( "data-tooltip" ).length() > 0 )
							bracketList.add( menuLink.getTextContent().trim().charAt( 0 ) - '1' );
					}
					
				} else if ( "profile-right".equals( divId ) ) {
					
					// This is where rank and team members info is stored
					
					// Bracket list needs to be sorted, because if a player has both HotS and Wol leagues, they are in this order:
					Collections.sort( bracketList );
					
					int teamCounter = 0;
					final NodeList teamInfoList = div.getElementsByTagName( "div" );
					final int nodesCount = teamInfoList.getLength();
					for ( int j = 0; j < nodesCount; j++ ) {
						final Element teamInfo = (Element) teamInfoList.item( j );
						if ( "ladder-tooltip".equals( teamInfo.getAttribute( "class" ) ) ) {
							final int bracket = bracketList.get( teamCounter++ );
							final TeamRank teamRank = new TeamRank();
							
							final String badgeClass = ( (Element) teamInfo.getElementsByTagName( "span" ).item( 0 ) ).getAttribute( "class" );
							teamRank.league = getLeagueFromBadgeClass( badgeClass );
							
							// Example: "Rank 63"; Chinese: "排名54" (no space!), Korean: "79 순위" (number comes first!)
							final String rankString = teamInfo.getElementsByTagName( "strong" ).item( 0 ).getTextContent().trim();
							// First try at the end (most common)
							int chPos = rankString.length() - 1;
							for ( char ch = rankString.charAt( chPos ); ch >= '0' && ch <= '9'; ch = rankString.charAt( --chPos ) )
								;
							if ( chPos < rankString.length() - 1 ) {
								// Found rank number in the end
								teamRank.divisionRank = Integer.parseInt( rankString.substring( chPos + 1 ) );
							}
							else {
								// Try at the beginning
								chPos = 0;
								for ( char ch = rankString.charAt( chPos ); ch >= '0' && ch <= '9'; ch = rankString.charAt( ++chPos ) )
									;
								if ( chPos > 0 ) {
									// Found rank number in the beginning
									teamRank.divisionRank = Integer.parseInt( rankString.substring( 0, chPos ) );
								}
							}
							
							final StringTokenizer membersTokenizer = new StringTokenizer( teamInfo.getLastChild().getTextContent(), "," );
							final List< String > memberList = new ArrayList< String >( bracket );
							while ( membersTokenizer.hasMoreTokens() )
								memberList.add( membersTokenizer.nextToken().trim() );
							teamRank.teamMembers = memberList.toArray( new String[ memberList.size() ] );
							
							teamRanksLists[ bracket ].add( teamRank );
						}
					}
					
				}
			}
			
			for ( int i = 0; i < teamRanksLists.length; i++ ) {
				profile.allRankss[ i ] = teamRanksLists[ i ].toArray( new TeamRank[ teamRanksLists[ i ].size() ] );
				Arrays.sort( profile.allRankss[ i ] );
			}
			
			// Copy division rank and team members properties to best ranks:
			for ( int bracket = 0; bracket < 4; bracket++ )
				if ( profile.bestRanks[ bracket ] != null && profile.allRankss[ bracket ] != null && profile.allRankss[ bracket ].length > 0 ) {
					// allRankss is sorted, so the first is the best
					profile.bestRanks[ bracket ].divisionRank = profile.allRankss[ bracket ][ 0 ].divisionRank;
					profile.bestRanks[ bracket ].teamMembers = profile.allRankss[ bracket ][ 0 ].teamMembers;
				}
			
			profile.allRanksUpdatedAt = new Date();
			
		} catch ( final Exception e ) {
			e.printStackTrace();
			return null;
		}
		
		return profile;
	}
	
	/**
	 * Returns the {@link League} from the badge class style string.
	 * @param badgeClass badge class style string to parse league from
	 * @return the {@link League} from the badge class style string
	 */
	private static League getLeagueFromBadgeClass( final String badgeClass ) {
		// example: class="badge badge-diamond badge-medium-1"
		for ( final League league : EnumCache.LEAGUES ) // Order is important: "grandmaster" must be checked first (because "master" is contained in "grandmaster" too!)
			if ( league != League.ANY && badgeClass.contains( league.bnetString ) )
				return league;
		
		return League.UNKNOWN;
	}
	
}
