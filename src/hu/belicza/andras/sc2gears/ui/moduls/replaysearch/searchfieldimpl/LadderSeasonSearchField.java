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

import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;
import hu.belicza.andras.sc2gearspluginapi.api.enums.LadderSeason;

import java.io.File;
import java.util.Date;
import java.util.EnumSet;
import java.util.Vector;

/**
 * Search field that filters by ladder season.
 * 
 * @author Andras Belicza
 */
public class LadderSeasonSearchField extends ComboBoxSearchField {
	
	/** Vector of ladder seasons to be added to the combo box. */
	public static final Vector< Object > LADDER_SEASON_VECTOR = createDataVector( EnumSet.allOf( LadderSeason.class ) );
	
	/**
	 * Creates a new LadderSeasonSearchField.
	 */
	public LadderSeasonSearchField() {
		super( Id.LADDER_SEASON, LADDER_SEASON_VECTOR, false );
		comboBox.setMaximumRowCount( comboBox.getItemCount() );
	}
	
	@Override
	public ReplayFilter getFilter() {
		return comboBox.getSelectedIndex() == 0 ? null : new ComboBoxReplayFilter( this ) {
			@Override
			public boolean customAccept( final File file, final Replay replay ) {
				return selected.equals( LadderSeason.getByDate( new Date( replay.details.saveTime ), replay.initData.gateway ) );
			}
		};
	}
	
}
