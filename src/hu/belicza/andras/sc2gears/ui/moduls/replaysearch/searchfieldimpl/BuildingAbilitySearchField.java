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

import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.ui.icons.IconHandler;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;
import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BuildingAbility;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;

/**
 * Search field that filters by a unit ability that was used.
 * 
 * @author Andras Belicza
 */
public class BuildingAbilitySearchField extends ComboBoxSearchField {
	
	/** Vector of building abilities to be added to the combo box. */
	private static final Vector< Object > BUILDING_ABILITY_VECTOR = createDataVector( EnumSet.allOf( BuildingAbility.class ) );
	
	/**
	 * Creates a new BuildingAbilitySearchField.
	 */
	public BuildingAbilitySearchField() {
		super( Id.BUILDING_ABILITY, BUILDING_ABILITY_VECTOR );
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
					if ( ( action = actions[ i ] ) instanceof GameEvents.UseBuildingAbilityAction && ( (GameEvents.UseBuildingAbilityAction) action ).buildingAbility == selected )
						if ( --minOccurrence_ == 0 )
							return true;
				return false;
			}
		};
	}
	
	@Override
	public Icon getIcon( final Object value ) {
		return value instanceof BuildingAbility ? Icons.getAbilityGroupIcon( ( (BuildingAbility) value ).abilityGroup, IconSize.MEDIUM ) : IconHandler.NULL.get( IconSize.MEDIUM );
	}
	
}
