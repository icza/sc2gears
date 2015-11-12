/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action;

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IGameEvents;

/**
 * Represents a Send resources action.
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see IGameEvents
 */
public interface ISendResourcesAction extends IAction {
	
	/**
	 * Returns the target player (who the resources were sent to).
	 * @return the target player (who the resources were sent to)
	 */
	int getTargetPlayer();
	
	/**
	 * Returns the sent minerals amount.
	 * @return the sent minerals amount
	 */
	int getMineralsSent();
	
	/**
	 * Returns the gas minerals amount.
	 * @return the gas minerals amount
	 */
	int getGasSent();
	
}
