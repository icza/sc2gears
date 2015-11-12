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
 * Combo box replay filter for {@link ComboBoxReplayFilter}s.
 * 
 * @author Andras Belicza
 */
public abstract class ComboBoxReplayFilter extends ReplayFilter {
	
	/** The selected filter object. */
	protected final Object selected;
	/** Minimum occurrence count.   */
	protected final int    minOccurrence;
	
	/**
	 * Creates a new ComboBoxReplayFilter.
	 * @param comboBoxSearchField combo box search field which this filter is created for
	 */
	public ComboBoxReplayFilter( final ComboBoxSearchField comboBoxSearchField ) {
		super( comboBoxSearchField );
		
		selected      = comboBoxSearchField.comboBox.getSelectedItem();
		minOccurrence = comboBoxSearchField.minOccurrenceSpinner == null ? 0 : (Integer) comboBoxSearchField.minOccurrenceSpinner.getValue();
	}
	
}
