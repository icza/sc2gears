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
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.UpgradeAction;
import hu.belicza.andras.sc2gears.ui.icons.IconHandler;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;
import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Upgrade;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;

/**
 * Search field that filters by an upgrade that was issued.
 * 
 * @author Andras Belicza
 */
public class UpgradeSearchField extends ComboBoxSearchField {
	
	/** Vector of upgrades to be added to the combo box. */
	private static final Vector< Object > UPGRADE_VECTOR = createDataVector(  EnumSet.allOf( Upgrade.class ) );
	
	/**
	 * Creates a new UnitSearchField.
	 */
	public UpgradeSearchField() {
		super( Id.UPGRADE, UPGRADE_VECTOR );
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
					if ( ( action = actions[ i ] ).type == ActionType.UPGRADE && ( (UpgradeAction) action ).upgrade == selected )
						if ( --minOccurrence_ == 0 )
							return true;
				return false;
			}
		};
	}
	
	@Override
	public Icon getIcon( final Object value ) {
		return value instanceof Upgrade ? Icons.getUpgradeIcon( (Upgrade) value, IconSize.MEDIUM ) : IconHandler.NULL.get( IconSize.MEDIUM );
	}
	
}
