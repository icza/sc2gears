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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility class to provide text filtering based on filter criteria.<br>
 * Supports applying include and exclude filters where the exclude filters have higher priority.<br>
 * The filter texts can contain logical operators: "OR" and "AND". Multiple words are connected with AND if not specified.
 * 
 * @author Andras Belicza
 */
public class TextFilter {
	
	/** Filter groups to apply.     */
	private final String[][] filterGroups;
	/** Filter out groups to apply. */
	private final String[][] filterOutGroups;
	
	/**
	 * Creates a new TextFilter.
	 * @param filterText    filter text
	 * @param filterOutText filter out text
	 */
	public TextFilter( final String filterText, final String filterOutText ) {
		filterGroups    = createFilterGroups( filterText    );
		filterOutGroups = createFilterGroups( filterOutText );
	}
	
	/**
	 * Tells if filters were specified (the include filter or the exclude filter is active).
	 * @return true if filters were specified; false otherwise 
	 */
	public boolean isFilterSpecified() {
		return filterGroups != null || filterOutGroups != null;
	}
	
	/**
	 * Tells if the include filter is active.
	 * @return true if the include filter is active; false otherwise 
	 */
	public boolean isIncludeFilterActive() {
		return filterGroups != null;
	}
	
	/**
	 * Tells if the exclude filter is active.
	 * @return true if the exclude filter is active; false otherwise 
	 */
	public boolean isExcludeFilterActive() {
		return filterOutGroups != null;
	}
	
	/**
	 * Tests if the specified text is included by the filters.
	 * @param text text to be tested; it should (must) be lower cased
	 * @return true if the specified text is included by the filters; false otherwise
	 */
	public boolean isIncluded( final String text ) {
		// First check the exclude filter
		if ( filterOutGroups != null ) {
			for ( final String[] filterOutGroup : filterOutGroups ) {
				boolean filterGroupApplies = true;
				
				for ( final String filter : filterOutGroup )
					if ( text.indexOf( filter, 0 ) < 0 ) { // Originally "!text.contains( filter )" was here which calls the current code after lots of other "general" unnecessary code
						filterGroupApplies = false;
						break;
					}
				
				if ( filterGroupApplies )
					return false;
			}
		}
		
		// If still in business, check the filter include filter
		if ( filterGroups != null ) {
			for ( final String[] filterGroup : filterGroups ) {
				boolean filterGroupApplies = true;
				
				for ( final String filter : filterGroup )
					if ( text.indexOf( filter, 0 ) < 0 ) { // Originally "!text.contains( filter )" was here which calls the current code after lots of other "general" unnecessary code
						filterGroupApplies = false;
						break;
					}
				
				if ( filterGroupApplies )
					return true;
			}
			
			// None of the filters applied:
			return false;
		}
		
		// None of the filters is active:
		return true;
	}
	
	/**
	 * Creates the text filter groups from the specified filter text.<br>
	 * <p>Words in the filter text are connected with logical AND condition by default even it it's not written explicitly.
	 * Logical "and" and "or" can be provided (case in-sensitive).</p>
	 * 
	 * <p>Filters in a group are connected with logical AND condition, and the groups are connected
	 * with logical OR condition.</p>
	 * 
	 * @param filterText filter text to create filter groups from
	 * @return the filter group created from <code>filterText</code>; or null if no filter is specified by the filter text
	 */
	private static String[][] createFilterGroups( String filterText ) {
		if ( filterText == null )
			return null;
		filterText = filterText.trim();
		if ( filterText.length() == 0 )
			return null;
		
		final List< List< String > > filterGroupList = new ArrayList< List< String > >();
		
		final StringTokenizer filterTokenizer = new StringTokenizer( filterText.toLowerCase(), " \t" );
		
		List< String > filterGroup = null;
		while ( filterTokenizer.hasMoreTokens() ) {
			final String filterToken = filterTokenizer.nextToken();
			if ( filterToken.equals( "or" ) ) {
				// Next token is in a new filter group
				filterGroup = null;
			}
			else if ( filterToken.equals( "and" ) ) {
				// Do nothing, next token is in the same group
			}
			else {
				if ( filterGroup == null ) {
					filterGroup = new ArrayList< String >( 2 );
					filterGroupList.add( filterGroup );
				}
				filterGroup.add( filterToken );
			}
		}
		
		final String[][] filterGroups = new String[ filterGroupList.size() ][];
		for ( int i = 0; i < filterGroups.length; i++ )
			filterGroups[ i ] = filterGroupList.get( i ).toArray( new String[ 0 ] );
		
		return filterGroups;
	}
	
}
