/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.enums;

import hu.belicza.andras.sc2gearspluginapi.api.GeneralUtilsApi;

/**
 * Miscellaneous Settings dialog tab.
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see GeneralUtilsApi#createLinkLabelToSettings(SettingsTab)
 * @see GeneralUtilsApi#createLinkLabelToSettings(SettingsTab, java.awt.Frame)
 */
public enum SettingsTab {
	
	/** Replay auto-save settings tab.    */
	REPLAY_AUTO_SAVE,
	/** APM Alert settings tab.           */
	APM_ALERT,
	/** Mouse print settings tab.         */
	MOUSE_PRINT,
	/** User interface settings tab.      */
	USER_INTERFACE,
	/** Replay parser settings tab.       */
	REPLAY_PARSER,
	/** Replay analyzer settings tab.     */
	REPLAY_ANALYZER,
	/** Multi-rep analysis settings tab.  */
	MULTI_REP_ANALYSIS,
	/** Internal settings tab.            */
	INTERNAL,
	/** Mouse game rules settings tab.    */
	MOUSE_GAME_RULES,
	/** Aliases settings tab.             */
	ALIASES,
	/** Pre-defined lists settings tab.   */
	PREDEFINED_LISTS,
	/** Folders settings tab.             */
	FOLDERS,
	/** Sc2gears Database settings tab.   */
	SC2GEARS_DATABASE,
	/** Custom replay sites settings tab. */
	CUSTOM_REPLAY_SITES;
	
}
