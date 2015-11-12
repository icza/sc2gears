/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api;

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayer;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IReplay;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IAction;

/**
 * Interface of the EAPM algorithm.
 * 
 * <p>For details and algorithm description see <a href="https://sites.google.com/site/sc2gears/features/replay-analyzer/apm-types">APM Types</a>.</p>
 * 
 * @since "2.5"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see ReplayUtilsApi#calculatePlayerEapm(IReplay, IPlayer)
 */
public interface EapmUtilsApi {
	
	/** Interface version. */
	String VERSION = "2.5";
	
	/**
	 * Returns the version of the EAPM algorithm.
	 * @return the version of the EAPM algorithm
	 */
	String getAlgorithmVersion();
	
	/**
	 * Tells if an action is <i>effective</i> so it can be included in EAPM calculation.
	 * 
	 * <p>See <a href="https://sites.google.com/site/sc2gears/features/replay-analyzer/apm-types">APM Types</a> for EAPM algorithm details.</p>
	 * 
	 * @param actions     reference to the actions array
	 * @param actionIndex index of the action to be tested
	 * 
	 * @return true if the action is considered <i>effective</i>; false otherwise
	 */
	boolean isActionEffective( IAction[] actions, int actionIndex );
	
}
