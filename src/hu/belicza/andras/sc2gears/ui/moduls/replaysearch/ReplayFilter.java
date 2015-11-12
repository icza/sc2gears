/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.replaysearch;

import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;

/**
 * Defines a replay filter. 
 * 
 * @author Andras Belicza
 */
public abstract class ReplayFilter {
	
	/** Frequently used game_events replay content set. */
	protected static final Set< ReplayContent > GAME_EVENTS_REPLAY_CONTENT_SET = EnumSet.of( ReplayContent.GAME_EVENTS );
	
	/** Tells if the accept condition has to be inverted. */
	private final boolean invert;
	
	/**
	 * Creates a new ReplayFilter.
	 * @param searchField search field which this filter is created for
	 */
	public ReplayFilter( final SearchField searchField ) {
		invert = searchField.invertCheckBox.isSelected();
	}
	
	/**
	 * Returns the replay content set that is required to apply this filter.<br>
	 * Contents from {@link ReplayFactory#GENERAL_INFO_CONTENT} should not be included (as they will be parsed anyway).<br>
	 * This default implementation returns null. 
	 * @return the replay content set that is required to apply this filter, can be <code>null</code>
	 */
	public Set< ReplayContent > getRequiredReplayContentSet() {
		return null;
	}
	
	/**
	 * Checks if this filter applies to a replay file.<br>
	 * Calls the {@link #customAccept} method and applies the state of the invert check box on it. 
	 * @param file   replay file
	 * @param replay the parsed replay
	 * @return true if this filter accepts the specified replay; false otherwise
	 */
	public boolean accept( final File file, final Replay replay ) {
		return invert ^ customAccept( file, replay );  // XOR :F
	}
	
	/**
	 * Custom implementation for checking if this filter applies to a replay file.
	 * @param file   replay file
	 * @param replay the parsed replay
	 * @return true if this filter accepts the specified replay; false otherwise
	 */
	public abstract boolean customAccept( final File file, final Replay replay );
	
}
