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
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;

import java.io.File;

/**
 * Search field that filters by map name.
 * 
 * @author Andras Belicza
 */
public class MapNameSearchField extends TextSearchField {
	
	/**
	 * Creates a new MapNameSearchField.
	 */
	public MapNameSearchField() {
		super( Id.MAP, PredefinedList.REP_SEARCH_MAP_NAME );
	}
	
	@Override
	public ReplayFilter getFilter() {
		return textField.getText().length() == 0 ? null : new TextReplayFilter( this ) {
			@Override
			public boolean customAccept( final File file, final Replay replay ) {
				final String mapName = replay.details.mapName.toLowerCase();
				return exactMatch ? mapName.equals( text ) :  mapName.contains( text );
			}
		};
	}
	
}
