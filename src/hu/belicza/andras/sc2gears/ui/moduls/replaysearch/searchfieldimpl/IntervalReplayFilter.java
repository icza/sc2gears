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
 * Interval replay filter for {@link IntervalSearchField}s.
 * 
 * @author Andras Belicza
 */
public abstract class IntervalReplayFilter extends ReplayFilter {
	
	/** The filter text of min. */
	protected final String minText;
	/** The filter text of max. */
	protected final String maxText;
	
	/**
	 * Creates a new IntervalReplayFilter.
	 * @param textSearchField text search field which this filter is created for
	 */
	public IntervalReplayFilter( final IntervalSearchField intervalSearchField ) {
		super( intervalSearchField );
		
		minText = intervalSearchField.minTextField.getText();
		maxText = intervalSearchField.maxTextField.getText();
	}
	
}
