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
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Search field that filters by race match-up.
 * 
 * @author Andras Belicza
 */
public class RaceMatchupSearchField extends TextSearchField {
	
	/** Tool tip of the race match-up text field. */
	private static String raceMatchupToolTip;
	
	/**
	 * Creates a new RaceMatchupSearchField.
	 */
	public RaceMatchupSearchField() {
		super( Id.RACE_MATCHUP, PredefinedList.REP_SEARCH_RACE_MATCHUP );
		
		if ( raceMatchupToolTip == null )
			raceMatchupToolTip = Language.getText( "module.repSearch.tab.filters.name.matchupToolTip",
					Race.PROTOSS.letter, Race.PROTOSS.toString(),
					Race.TERRAN .letter, Race.TERRAN .toString(),
					Race.ZERG   .letter, Race.ZERG   .toString(),
					Race.RANDOM .letter, Race.RANDOM .toString(),
					Race.ZERG.letter + "v" + Race.RANDOM.letter + ", " + Race.PROTOSS.letter + Race.TERRAN.letter + "v" + Race.ZERG.letter + "*, " + Race.PROTOSS.letter + Race.TERRAN.letter + Race.ZERG.letter );
		
		textField.setToolTipText( raceMatchupToolTip );
	}
	
	@Override
	public boolean customValidate() {
		final String raceMatchupText = textField.getText();
		if ( raceMatchupText.length() == 0 )
			return true;
		return parseRaceMatchups( raceMatchupText ) != null;
	}
	
	@Override
	public ReplayFilter getFilter() {
		return textField.getText().length() == 0 ? null : new TextReplayFilter( this ) {
			private final Race[][] teamRacess;
			{
				teamRacess = parseRaceMatchups( text );
				// It's much easier to find matches if races are in a specific order:
				// Randoms must be first, then concrete races, and ANY races in the end
				for ( final Race[] teamRaces : teamRacess )
					Arrays.sort( teamRaces );
				// Take match-ups with ANY races to the end as they can match more patters 
				Arrays.sort( teamRacess, new Comparator< Race[] >() {
					@Override
					public int compare( final Race[] teamRaces1, final Race[] teamRaces2 ) {
						return GeneralUtils.countElements( teamRaces1, Race.ANY ) - GeneralUtils.countElements( teamRaces2, Race.ANY );
					}
				} );
			}
			@Override
			public boolean customAccept( final File file, final Replay replay ) {
				if ( replay.details.players.length == 0 )
					return false;
				if ( teamRacess.length == 1 ) {
					// Simply just look for used races
					final List< Player > playerList = new ArrayList< Player >( replay.details.players.length );
					for ( final Player player : replay.details.players )
						playerList.add( player );
					return patternMatchesTeam( teamRacess[ 0 ], playerList, exactMatch );
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
					// Team order matters!! Counter example: a replay with RvZ would match "RvZ" pattern, but would fail on "ZvR" pattern!
					// So we check all team orders by generating all permutations!
					permutationCycle:
					for ( final int[] permutationIndices : getPermutationIndices( teamList.size() ) ) {
						final List< List< Player > > teamList_ = new ArrayList< List< Player > >( teamList.size() );
						for ( final int permutationIndex : permutationIndices )
							teamList_.add( teamList.get( permutationIndex ) );
						for ( final Race[] racePattern : teamRacess ) {
							boolean found = false;
							for ( final List< Player > team_ : teamList_ )
								if ( patternMatchesTeam( racePattern, new ArrayList< Player > ( team_ ), exactMatch ) ) { // Have to pass a copy of the team!
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
	 * Returns true if the specified race pattern matches the specified team.<br>
	 * The passed team list will be modified, if it is not intended, a copy should be passed.
	 * @param pattern    race pattern to be matched
	 * @param team       team to be checked if matched by the race pattern
	 * @param exactMatch tells if exact match is required => no extra team mates without a race in the race pattern
	 * @return true if the specified race pattern matches the specified team
	 */
	private static boolean patternMatchesTeam( final Race[] pattern, final List< Player > team, final boolean exactMatch ) {
		for ( final Race race : pattern ) {
			if ( team.isEmpty() )
				return false;
			
			if ( race == Race.ANY )
				team.remove( team.size() - 1 ); // All important ones have been taken out, doesn't matter which we take out now (pattern is sorted)
			else {
				boolean found = false;
				for ( final Player player : team )
					if ( player.finalRace == race || player.race == Race.RANDOM && race == Race.RANDOM ) {
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
	 * Parses the race match-ups and returns them in arrays respectively to teams.
	 * @param raceMatchupText text of race match-ups to be parsed
	 * @return the race match-ups respectively to teams; or <code>null</code> if raceMatchupText is invalid
	 */
	private static Race[][] parseRaceMatchups( final String raceMatchupText ) {
		final List< Race[] > teamRacesList = new ArrayList< Race[] >( 4 );
		
		List< Race > teamRaceList = new ArrayList< Race >( 2 );
		for ( int i = 0; i < raceMatchupText.length(); i++ ) {
			final char ch = Character.toUpperCase( raceMatchupText.charAt( i ) );
			
			if ( ch == 'V' ) {
				if ( teamRaceList.isEmpty() )
					return null;
				else {
					teamRacesList.add( teamRaceList.toArray( new Race[ teamRaceList.size() ] ) );
					teamRaceList = new ArrayList< Race >( 2 );
				}
			}
			else {
				final Race race = Race.fromLetter( ch );
				if ( race == Race.UNKNOWN )
					return null;
				teamRaceList.add( race );
			}
		}
		
		if ( teamRaceList.isEmpty() )
			return null;
		else
			teamRacesList.add( teamRaceList.toArray( new Race[ teamRaceList.size() ] ) );
		
		return teamRacesList.toArray( new Race[ teamRacesList.size() ][] );
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
