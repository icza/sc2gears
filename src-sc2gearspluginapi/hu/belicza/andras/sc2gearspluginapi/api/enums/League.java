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
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gearspluginapi.api.profile.ITeamRank;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

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
	GRANDMASTER ( "module.repAnalyzer.tab.charts.players.league.grandmaster", "grandmaster", "module.repAnalyzer.tab.charts.players.league.grandmaster.letter" ),
	MASTER      ( "module.repAnalyzer.tab.charts.players.league.master"     , "master"     , "module.repAnalyzer.tab.charts.players.league.master.letter"      ),
	DIAMOND     ( "module.repAnalyzer.tab.charts.players.league.diamond"    , "diamond"    , "module.repAnalyzer.tab.charts.players.league.diamond.letter"     ),
	PLATINUM    ( "module.repAnalyzer.tab.charts.players.league.platinum"   , "platinum"   , "module.repAnalyzer.tab.charts.players.league.platinum.letter"    ),
	GOLD        ( "module.repAnalyzer.tab.charts.players.league.gold"       , "gold"       , "module.repAnalyzer.tab.charts.players.league.gold.letter"        ),
	SILVER      ( "module.repAnalyzer.tab.charts.players.league.silver"     , "silver"     , "module.repAnalyzer.tab.charts.players.league.silver.letter"      ),
	BRONZE      ( "module.repAnalyzer.tab.charts.players.league.bronze"     , "bronze"     , "module.repAnalyzer.tab.charts.players.league.bronze.letter"      ),
	UNRANKED    ( "module.repAnalyzer.tab.charts.players.league.unranked"   , ""           , "module.repAnalyzer.tab.charts.players.league.unranked.letter"    ),
	ANY         ( null                                                      , ""           , "*" ),
	UNKNOWN     ( "general.unknown"                                         , ""           , "-" );
	
	/** Cache of the string value.                */
	public  final String stringValue;
	/** League letter (usually the first letter). */
	public  final char   letter;
	/** String used by Battle.net.                */
	public  final String bnetString;
	/** Icons of the league.                      */
	private final Icon[] icons         = new Icon[ 4 ];
	/** Icon resources of the league.             */
	private final URL[]  iconResources = new URL [ 4 ];
	
	/**
	 * Creates a new League.
	 * @param textKey       key of the text representation
	 * @param bnetString    string used by Battle.net
	 * @param letterTextKey key of the letter representation
	 */
	private League( final String textKey, final String bnetString, final String letterTextKey ) {
		stringValue      = textKey == null ? null : Language.getText( textKey );
		letter           = Character.toUpperCase( letterTextKey.length() == 1 ? letterTextKey.charAt( 0 ) : Language.getText( letterTextKey ).charAt( 0 ) );
		this.bnetString  = bnetString;
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
	
	/**
	 * Returns the icon of the league for the specified rank.
	 * @param rank rank to return the league icon for
	 * @return the icon of the league for the specified rank
	 */
	public Icon getIconForRank( final int rank ) {
		return icons[ getIconIndexForRank( rank ) ];
	}
	
	/**
	 * Returns the icon resource of the league for the specified rank.
	 * @param rank rank to return the league icon resource for
	 * @return the icon resource of the league for the specified rank
	 */
	public URL getIconResourceForRank( final int rank ) {
		return iconResources[ getIconIndexForRank( rank ) ];
	}
	
	/**
	 * Returns the icon of the league for the specified rank.
	 * Class is in the range of 1..4 where 1 is the bottom of the ladder, 4 is the top.
	 * 
	 * @param class_ class to return the league icon for
	 * @return the icon of the league for the specified rank
	 * 
	 * @since "3.1"
	 */
	public Icon getIconForClass( final int class_ ) {
		return icons[ getIconIndexForClass( class_ ) ];
	}
	
	/**
	 * Returns the icon resource of the league for the specified class.
	 * Class is in the range of 1..4 where 1 is the bottom of the ladder, 4 is the top.
	 * 
	 * @param class_ class to return the league icon resource for
	 * @return the icon resource of the league for the specified class
	 * 
	 * @since "3.1"
	 */
	public URL getIconResourceForClass( final int class_ ) {
		return iconResources[ getIconIndexForClass( class_) ];
	}
	
	/**
	 * Returns the icon index of the league for the specified rank.
	 * @param rank rank to return the league icon index for
	 * @return the icon index of the league for the specified rank
	 */
	private int getIconIndexForRank( int rank ) {
		int i;
		if ( this == UNRANKED || this == UNKNOWN )
			i = 0;
		else {
			if ( this != GRANDMASTER )
				rank <<= 1;
			if ( rank > 100 )      // Top 200 or Top 100
				i = 0;
			else if ( rank > 50 )  // Top 100 or Top 50
				i = 1;
			else if ( rank > 16 )  // Top 50 or Top 25
				i = 2;
			else                   // Top 16 or Top 8
				i = 3;
		}
		
		ensureIconLoaded( i );
		
		return i;
	}
	
	/**
	 * Returns the icon of the league for the specified class.
	 * Class is in the range of 1..4 where 1 is the bottom of the ladder, 4 is the top.
	 * 
	 * @param class_ class to return the league icon for
	 * @return the icon of the league for the specified class
	 * 
	 * @since "3.1"
	 */
	private int getIconIndexForClass( int class_ ) {
		// Class is nothing more than iconIdx + 1
		int iconIdx;
		
		if ( this == UNRANKED || this == UNKNOWN )
			iconIdx = 0;
		else {
			if ( class_ < 1 )
				class_ = 1;
			if ( class_ > 4 )
				class_ = 4;
			iconIdx = class_ - 1;
		}
		
		ensureIconLoaded( iconIdx );
		
		return iconIdx;
	}
	
	/**
	 * Ensures that the icon for the specified index is loaded.
	 * @param iconIdx index of the icon to be checked
	 */
	private void ensureIconLoaded( final int iconIdx ) {
		if ( icons[ iconIdx ] == null ) {
			iconResources[ iconIdx ] = Icons.class.getResource( "sc2/leagues/" + name() + "-" + (iconIdx+1) + ".png" );
			icons[ iconIdx ] = new ImageIcon( iconResources[ iconIdx ] );
		}
	}
	
	@Override
	public String toString() {
		return stringValue;
	};
	
}
