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
import hu.belicza.andras.sc2gears.util.GeneralUtils;

import java.io.File;

/**
 * Search field that filters by game length.
 * 
 * @author Andras Belicza
 */
public class GameLengthSearchField extends IntervalSearchField {
	
	/**
	 * Creates a new GameLengthSearchField.
	 */
	public GameLengthSearchField() {
		super( Id.GAME_LENGTH );
		minTextField.setToolTipText( Language.getText( "module.repSearch.tab.filters.timeFormatToolTip" ) );
		maxTextField.setToolTipText( Language.getText( "module.repSearch.tab.filters.timeFormatToolTip" ) );
	}
	
	@Override
	public boolean customValidateMin() {
		final String minText = minTextField.getText();
		
		if ( minText.length() == 0 )
			return true;
		
		final Integer min = GeneralUtils.parseSeconds( minText );
		if ( min == null || min < 0 )
			return false;
		else
			return true;
	}
	
	@Override
	public boolean customValidateMax() {
		final String maxText = maxTextField.getText();
		
		if ( maxText.length() == 0 )
			return true;
		
		final Integer max = GeneralUtils.parseSeconds( maxText );
		if ( max == null || max < 0 )
			return false;
		else
			return true;
	}
	
	/**
	 * A game length replay filter.<br>
	 * This implementation requires a named, separate class, because the implementation depends on a setting:
	 * the time measurement (whether it's game-time or real-time).
	 * 
	 * @author Andras Belicza
	 *
	 */
	public static class GameLengthReplayFilter extends IntervalReplayFilter {
		private final Integer minSeconds = minText.length() > 0 ? GeneralUtils.parseSeconds( minText ) : null;
		private final Integer maxSeconds = maxText.length() > 0 ? GeneralUtils.parseSeconds( maxText ) : null;
		private boolean useRealTime;
		public GameLengthReplayFilter( final IntervalSearchField intervalSearchField ) {
			super( intervalSearchField );
		}
		public void setUseRealTime( final boolean useRealTime ) {
			this.useRealTime = useRealTime;
		}
		@Override
		public boolean customAccept( final File file, final Replay replay ) {
			final int gameLengthSec = useRealTime ? replay.converterGameSpeed.convertToRealTime( replay.gameLengthSec ) : replay.gameLengthSec;
			if ( minSeconds != null && gameLengthSec < minSeconds )
				return false;
			if ( maxSeconds != null && gameLengthSec > maxSeconds )
				return false;
			return true;
		}
	}
	
	@Override
	public ReplayFilter getFilter() {
		return minTextField.getText().length() == 0 && maxTextField.getText().length() == 0 ? null : new GameLengthReplayFilter( this );
	}
	
}
