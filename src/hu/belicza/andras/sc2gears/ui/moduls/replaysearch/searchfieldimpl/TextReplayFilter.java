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

import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;


/**
 * Text replay filter for {@link TextSearchField}s.
 * 
 * @author Andras Belicza
 */
public abstract class TextReplayFilter extends ReplayFilter {
	
	/** The lower-cased filter text.       */
	protected final String  text;
	/** Tells if exact match is required. */
	protected final boolean exactMatch;
	
	/**
	 * Creates a new TextReplayFilter.
	 * @param textSearchField text search field which this filter is created for
	 */
	public TextReplayFilter( final TextSearchField textSearchField ) {
		super( textSearchField );
		
		text       = textSearchField.textField.getText().toLowerCase();
		exactMatch = textSearchField.exactMatch.isSelected();
	}
	
}
