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

import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.TrainAction;
import hu.belicza.andras.sc2gears.ui.icons.IconHandler;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;
import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;

/**
 * Search field that filters by a unit that was trained.
 * 
 * @author Andras Belicza
 */
public class UnitSearchField extends ComboBoxSearchField {
	
	/** Vector of units to be added to the combo box. */
	public static final Vector< Object > UNIT_VECTOR;
	static {
		// List only the trainable units (as this search field operates on TRAIN action types)
		final Set< Unit > trainableUnitSet = EnumSet.noneOf( Unit.class );
		trainableUnitSet.addAll( ReplayUtils.CURRENT_ABILITY_CODES.TRAIN_ABILITY_CODES      .values() );
		trainableUnitSet.addAll( ReplayUtils.CURRENT_ABILITY_CODES.WARP_ABILITY_CODES       .values() );
		trainableUnitSet.addAll( ReplayUtils.CURRENT_ABILITY_CODES.TRAIN_HALLU_ABILITY_CODES.values() );
		UNIT_VECTOR = createDataVector( trainableUnitSet );
	}
	
	/**
	 * Creates a new UnitSearchField.
	 */
	public UnitSearchField() {
		super( Id.UNIT, UNIT_VECTOR );
	}
	
	@Override
	public ReplayFilter getFilter() {
		return comboBox.getSelectedIndex() == 0 ? null : new ComboBoxReplayFilter( this ) {
			@Override
			public Set< ReplayContent > getRequiredReplayContentSet() {
				return GAME_EVENTS_REPLAY_CONTENT_SET;
			}
			@Override
			public boolean customAccept( final File file, final Replay replay ) {
				final Action[] actions = replay.gameEvents.actions;
				Action action;
				int minOccurrence_ = minOccurrence;
				for ( int i = actions.length - 1; i >= 0; i-- )
					if ( ( action = actions[ i ] ).type == ActionType.TRAIN && ( (TrainAction) action ).unit == selected )
						if ( --minOccurrence_ == 0 )
							return true;
				return false;
			}
		};
	}
	
	@Override
	public Icon getIcon( final Object value ) {
		return value instanceof Unit ? Icons.getUnitIcon( (Unit) value, IconSize.MEDIUM ) : IconHandler.NULL.get( IconSize.MEDIUM );
	}
	
}
