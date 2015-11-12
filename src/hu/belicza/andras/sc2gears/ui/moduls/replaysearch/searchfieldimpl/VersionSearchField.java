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
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;

import java.io.File;

/**
 * Search field that filters by version.
 * 
 * @author Andras Belicza
 */
public class VersionSearchField extends IntervalSearchField {
	
	/**
	 * Creates a new VersionSearchField.
	 */
	public VersionSearchField() {
		super( Id.VERSION );
	}
	
	@Override
	public boolean customValidateMin() {
		final String minText = minTextField.getText();
		
		if ( minText.length() == 0 )
			return true;
		
		final int[] min = ReplayUtils.parseVersion( minText );
		if ( min == null )
			return false;
		else
			return true;
	}
	
	@Override
	public boolean customValidateMax() {
		final String maxText = maxTextField.getText();
		
		if ( maxText.length() == 0 )
			return true;
		
		final int[] max = ReplayUtils.parseVersion( maxText );
		if ( max == null )
			return false;
		else
			return true;
	}
	
	@Override
	public ReplayFilter getFilter() {
		return minTextField.getText().length() == 0 && maxTextField.getText().length() == 0 ? null : new IntervalReplayFilter( this ) {
			private final int[] minVersion = minText.length() > 0 ? ReplayUtils.parseVersion( minText ) : null;
			private final int[] maxVersion = maxText.length() > 0 ? ReplayUtils.parseVersion( maxText ) : null;
			@Override
			public boolean customAccept( final File file, final Replay replay ) {
				if ( minText.length() > 0 && ReplayUtils.compareVersions( replay.buildNumbers, minVersion ) < 0 )
					return false;
				if ( maxText.length() > 0 && ReplayUtils.compareVersions( replay.buildNumbers, maxVersion ) > 0 )
					return false;
				return true;
			}
		};
	}
	
}
