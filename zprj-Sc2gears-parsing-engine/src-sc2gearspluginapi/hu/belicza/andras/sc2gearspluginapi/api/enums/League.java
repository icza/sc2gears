/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.enums;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.EnumCache;

/**
 * Battle.net League.
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see ITeamRank
 */
public enum League {
	
	// Order is important, it has to reflect the "skill" or "difficulty" order
	GRANDMASTER ( "module.repAnalyzer.tab.charts.players.league.grandmaster", "grandmaster", 'R' ),
	MASTER      ( "module.repAnalyzer.tab.charts.players.league.master"     , "master"     , 'M' ),
	DIAMOND     ( "module.repAnalyzer.tab.charts.players.league.diamond"    , "diamond"    , 'D' ),
	PLATINUM    ( "module.repAnalyzer.tab.charts.players.league.platinum"   , "platinum"   , 'P' ),
	GOLD        ( "module.repAnalyzer.tab.charts.players.league.gold"       , "gold"       , 'G' ),
	SILVER      ( "module.repAnalyzer.tab.charts.players.league.silver"     , "silver"     , 'S' ),
	BRONZE      ( "module.repAnalyzer.tab.charts.players.league.bronze"     , "bronze"     , 'B' ),
	UNRANKED    ( "module.repAnalyzer.tab.charts.players.league.unranked"   , ""           , 'U' ),
	ANY         ( null                                                      , ""           , '*' ),
	UNKNOWN     ( "general.unknown"                                         , ""           , '-' );
	
	/** Cache of the string value.                */
	public  final String stringValue;
	/** League letter (usually the first letter). */
	public  final char   letter;
	/** String used by Battle.net.                */
	public  final String bnetString;
	
	/**
	 * Creates a new League.
	 * @param textKey    key of the text representation
	 * @param bnetString string used by Battle.net
	 * @param letter     league letter
	 */
	private League( final String textKey, final String bnetString, final char letter ) {
		stringValue     = textKey == null ? null : Language.getText( textKey );
		this.bnetString = bnetString;
		this.letter     = letter;
	}
	
	/**
	 * Returns the league specified by its letter.
	 * @param letter letter of the league 
	 * @return the league specified by its letter; or <code>UNKNOWN</code> if the letter is invalid
	 * @since "4.2"
	 */
	public static League fromLetter( final char letter ) {
		for ( final League league : EnumCache.LEAGUES )
			if ( league.letter == letter )
				return league;
		return UNKNOWN;
	}
	
	@Override
	public String toString() {
		return stringValue;
	};
	
}
