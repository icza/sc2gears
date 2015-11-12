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
 * Represents a save game action.
 * 
 * @since "2.7.2"
 * 
 * @author Andras Belicza
 * 
 * @see IGameEvents
 */
public interface ISaveGameAction extends IAction {
	
	/**
	 * Returns the saved game file name.
	 * @return the saved game file name
	 */
	String getFileName();
	
}
