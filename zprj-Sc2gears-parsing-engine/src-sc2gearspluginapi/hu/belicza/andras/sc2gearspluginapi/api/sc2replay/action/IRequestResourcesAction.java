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
 * Represents a Request resources action.
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see IGameEvents
 */
public interface IRequestResourcesAction extends IAction {
	
	/**
	 * Returns the requested minerals amount.
	 * @return the requested minerals amount
	 */
	int getMineralsRequested();
	
	/**
	 * Returns the requested gas amount.
	 * @return the requested gas amount
	 */
	int getGasRequested();
	
}
