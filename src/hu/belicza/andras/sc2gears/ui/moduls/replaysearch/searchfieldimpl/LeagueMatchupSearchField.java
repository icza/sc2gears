/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.EnumCache;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gearspluginapi.api.enums.League;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Search field that filters by league match-up.
 * 
 * @author Andras Belicza
 */
public class LeagueMatchupSearchField extends TextSearchField {
	
	/** Tool tip of the league match-up text field. */
	private static String leagueMatchupToolTip;
	
	/**
	 * Creates a new LeagueMatchupSearchField.
	 */
	public LeagueMatchupSearchField() {
		super( Id.LEAGUE_MATCHUP, PredefinedList.REP_SEARCH_LEAGUE_MATCHUP );
		
		if ( leagueMatchupToolTip == null ) {
			final StringBuilder table = new StringBuilder( "<table border=1>" );
			for ( final League league : EnumCache.LEAGUES )
				if ( league != League.ANY )
					table.append( "<tr><th>" ).append( league.letter ).append( "<td><img src=\"" )
						.append( league.getIconResourceForRank( 200 ) ).append( "\" width=16 height=16 border=0>" ).append( league.stringValue );
			table.append( "</table>" );
			leagueMatchupToolTip = Language.getText( "module.repSearch.tab.filters.name.leagueMatchupToolTip",
					League.PLATINUM.letter + "v" + League.GOLD.letter + ", " + League.PLATINUM.letter + League.MASTER.letter + "v" + League.DIAMOND.letter + "*, " + League.BRONZE.letter + League.SILVER.letter + League.GOLD.letter,
					table.toString() );
		}
		
		textField.setToolTipText( leagueMatchupToolTip );
	}
	
	@Override
	public boolean customValidate() {
		final String leagueMatchupText = textField.getText();
		if ( leagueMatchupText.length() == 0 )
			return true;
		return parseLeagueMatchups( leagueMatchupText ) != null;
	}
	
	@Override
	public ReplayFilter getFilter() {
		return textField.getText().length() == 0 ? null : new TextReplayFilter( this ) {
			private final League[][] teamLeaguess;
			{
				teamLeaguess = parseLeagueMatchups( text );
				// It's much easier to find matches if leagues are in a specific order:
				// Concrete leagues and then ANY league in the end.
				for ( final League[] teamLeagues : teamLeaguess )
					Arrays.sort( teamLeagues );
				// Take match-ups with ANY leagues to the end as they can match more patters 
				Arrays.sort( teamLeaguess, new Comparator< League[] >() {
					@Override
					public int compare( final League[] teamLeagues1, final League[] teamLeagues2 ) {
						return GeneralUtils.countElements( teamLeagues1, League.ANY ) - GeneralUtils.countElements( teamLeagues2, League.ANY );
					}
				} );
			}
			@Override
			public boolean customAccept( final File file, final Replay replay ) {
				if ( replay.details.players.length == 0 )
					return false;
				if ( teamLeaguess.length == 1 ) {
					// Simply just look for used leagues
					final List< Player > playerList = new ArrayList< Player >( replay.details.players.length );
					for ( final Player player : replay.details.players )
						playerList.add( player );
					return patternMatchesTeam( teamLeaguess[ 0 ], playerList, exactMatch );
				}
				else {
					// First assemble teams (which are player lists)
					final List< List< Player > > teamList = new ArrayList< List< Player > >( 2 );
					final int[] teamOrderPlayerIndices = replay.details.getTeamOrderPlayerIndices();
					List< Player > team = null;
					int lastTeam = -1;
					for ( final int playerIndex : teamOrderPlayerIndices ) {
						final Player player = replay.details.players[ playerIndex ];
						if ( player.team != lastTeam ) {
							if ( team != null )
								teamList.add( team );
							team = new ArrayList< Player >( 4 );
							lastTeam = player.team;
						}
						team.add( player );
					}
					teamList.add( team );
					// Now try to match the patterns
					// Team order matters!! Counter example: a replay with MvD would match "MvD" pattern, but would fail on "DvM" pattern!
					// So we check all team orders by generating all permutations!
					permutationCycle:
					for ( final int[] permutationIndices : getPermutationIndices( teamList.size() ) ) {
						final List< List< Player > > teamList_ = new ArrayList< List< Player > >( teamList.size() );
						for ( final int permutationIndex : permutationIndices )
							teamList_.add( teamList.get( permutationIndex ) );
						for ( final League[] leaguePattern : teamLeaguess ) {
							boolean found = false;
							for ( final List< Player > team_ : teamList_ )
								if ( patternMatchesTeam( leaguePattern, new ArrayList< Player > ( team_ ), exactMatch ) ) { // Have to pass a copy of the team!
									found = true;
									teamList_.remove( team_ ); // Will not throw ConcurrentModificationException because we "break" in the next line
									break;
								}
							if ( !found )
								continue permutationCycle;
						}
						if ( exactMatch && !teamList_.isEmpty() )
							continue permutationCycle;
						return true;
					}
					return false; // None of the permutations were good
				}
			}
		};
	}
	
	/**
	 * Returns true if the specified league pattern matches the specified team.<br>
	 * The passed team list will be modified, if it is not intended, a copy should be passed.
	 * @param pattern    league pattern to be matched
	 * @param team       team to be checked if matched by the league pattern
	 * @param exactMatch tells if exact match is required => no extra team mates without a league in the league pattern
	 * @return true if the specified league pattern matches the specified team
	 */
	private static boolean patternMatchesTeam( final League[] pattern, final List< Player > team, final boolean exactMatch ) {
		for ( final League league : pattern ) {
			if ( team.isEmpty() )
				return false;
			
			if ( league == League.ANY )
				team.remove( team.size() - 1 ); // All important ones have been taken out, doesn't matter which we take out now (pattern is sorted)
			else {
				boolean found = false;
				for ( final Player player : team )
					if ( player.getLeague() == league ) {
						found = true;
						team.remove( player ); // Will not throw ConcurrentModificationException because we "break" in the next line
						break;
					}
				if ( !found )
					return false;
			}
		}
		if ( exactMatch && !team.isEmpty() )
			return false;
		return true;
	}
	
	/**
	 * Parses the league match-ups and returns them in arrays respectively to teams.
	 * @param leagueMatchupText text of league match-ups to be parsed
	 * @return the league match-ups respectively to teams; or <code>null</code> if leagueMatchupText is invalid
	 */
	private static League[][] parseLeagueMatchups( final String leagueMatchupText ) {
		final List< League[] > teamLeaguesList = new ArrayList< League[] >( 4 );
		
		List< League > teamLeagueList = new ArrayList< League >( 2 );
		for ( int i = 0; i < leagueMatchupText.length(); i++ ) {
			final char ch = Character.toUpperCase( leagueMatchupText.charAt( i ) );
			
			if ( ch == 'V' ) {
				if ( teamLeagueList.isEmpty() )
					return null;
				else {
					teamLeaguesList.add( teamLeagueList.toArray( new League[ teamLeagueList.size() ] ) );
					teamLeagueList = new ArrayList< League >( 2 );
				}
			}
			else {
				final League league = League.fromLetter( ch );
				if ( league == League.UNKNOWN && ch != League.UNKNOWN.letter )
					return null;
				teamLeagueList.add( league );
			}
		}
		
		if ( teamLeagueList.isEmpty() )
			return null;
		else
			teamLeaguesList.add( teamLeagueList.toArray( new League[ teamLeagueList.size() ] ) );
		
		return teamLeaguesList.toArray( new League[ teamLeaguesList.size() ][] );
	}
	
	/**
	 * Returns the permutations of indices from 0 up to <code>teamsCount</code>.
	 * @param teamsCount number of teams (max index) to generate permutations to
	 * @return the permutations of indices from 0 up to <code>teamsCount</code>
	 */
	private static int[][] getPermutationIndices( final int teamsCount ) {
		switch ( teamsCount ) {
		
		case 2 : { return new int[][] { { 0, 1 }, { 1, 0 } }; }
		
		case 3 : { return new int[][] { { 0, 1, 2 }, { 0, 2, 1 }, { 1, 0, 2 }, { 1, 2, 1 }, { 2, 0, 1 }, { 2, 1, 0 } }; }
		
		case 4 : { return new int[][] {
			{ 0, 1, 2, 3 }, { 0, 1, 3, 2 }, { 0, 2, 1, 3 }, { 0, 2, 3, 1 }, { 0, 3, 1, 2 }, { 0, 3, 2, 1 },
			{ 1, 0, 2, 3 }, { 1, 0, 3, 2 }, { 1, 2, 0, 3 }, { 1, 2, 3, 0 }, { 1, 3, 0, 2 }, { 1, 3, 2, 0 },
			{ 2, 0, 1, 3 }, { 2, 0, 3, 1 }, { 2, 1, 0, 3 }, { 2, 1, 3, 0 }, { 2, 3, 0, 1 }, { 2, 3, 1, 0 },
			{ 3, 0, 1, 2 }, { 3, 0, 2, 1 }, { 3, 1, 0, 2 }, { 3, 1, 2, 0 }, { 3, 2, 0, 1 }, { 3, 2, 1, 0 }
		}; }
		
		default: {
			// This is a very rare case (or won't even be used), we just give the default order and do not care about permutations
			final int[] indices = new int[ teamsCount ];
			for ( int i = 0; i < teamsCount; i++ )
				indices[ i ] = i;
			return new int[][] { indices };
		}
		}
		
	}
	
}
