/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.listener;

import hu.belicza.andras.sc2gearspluginapi.api.InfoApi;

import java.io.File;

/**
 * Defines a listener interface that can be notified when new replays are detected and they are auto-saved.
 * 
 * @since "2.7"
 * 
 * @author Andras Belicza
 */
public interface ReplayAutosaveListener {
	
	/**
	 * Called when a new replay file is detected and auto-saved by Sc2gears.<br>
	 * New replays are only detected and auto-saved if replay auto-save is enabled in Sc2gears.<br>
	 * Replay auto-save status can be checked with {@link InfoApi#isReplayAutoSaveEnabled()}.
	 * 
	 * <p>Note: If Sc2gears fails to auto-save a replay, this method will still be called,
	 * and <code>autosavedReplayFile</code> will be equal to <code>originalReplayFile</code>.</p>
	 * 
	 * @param autosavedReplayFile the auto-saved replay file
	 * @param originalReplayFile  the original new replay file
	 */
	void replayAutosaved( File autosavedReplayFile, File originalReplayFile );
	
}
