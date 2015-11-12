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
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;

import java.io.File;
import java.util.Date;

/**
 * Search field that filters by date.
 * 
 * @author Andras Belicza
 */
public class DateSearchField extends IntervalSearchField {
	
	/** Ms in a day. */
	private static final long MS_IN_A_DAY = 24l*60*60*1000;
	
	/**
	 * Creates a new VersionSearchField.
	 */
	public DateSearchField() {
		super( Id.DATE );
		final Date currentDate = new Date();
		final String toolTipText = Language.getText( "module.repSearch.tab.filters.name.dateToolTip", Language.formatDate( currentDate ), Language.formatDateTime( currentDate ) );
		minTextField.setToolTipText( toolTipText );
		maxTextField.setToolTipText( toolTipText );
	}
	
	@Override
	public boolean customValidateMin() {
		final String minText = minTextField.getText();
		
		if ( minText.length() == 0 )
			return true;
		
		final Date minDate = parseDate( minText );
		if ( minDate == null )
			return false;
		else
			return true;
	}
	
	@Override
	public boolean customValidateMax() {
		final String maxText = maxTextField.getText();
		
		if ( maxText.length() == 0 )
			return true;
		
		final Date maxDate = parseDate( maxText );
		if ( maxDate == null )
			return false;
		else
			return true;
	}
	
	@Override
	public ReplayFilter getFilter() {
		return minTextField.getText().length() == 0 && maxTextField.getText().length() == 0 ? null : new IntervalReplayFilter( this ) {
			private final long minDate = minText.length() > 0 ? parseDate( minText ).getTime() : 0;
			private final long maxDate = maxText.length() > 0 ? parseDate( maxText ).getTime() + MS_IN_A_DAY : 0;
			@Override
			public boolean customAccept( final File file, final Replay replay ) {
				if ( minText.length() > 0 && replay.details.saveTime < minDate )
					return false;
				if ( maxText.length() > 0 && replay.details.saveTime > maxDate )
					return false;
				return true;
			}
		};
	}
	
	/**
	 * 
	 * @param dateString
	 * @return
	 */
	private static Date parseDate( final String dateString ) {
		Date date = Language.parseDateTime( dateString, true );
		if ( date == null )
			date = Language.parseDate( dateString );
		return date;
	}
	
}
