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
 * Represents a select action.
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see IGameEvents
 */
public interface IMoveScreenAction extends IAction {
	
	/** Interface version. */
	String VERSION = "4.0";
	
	/**
	 * Tells if the action has location info.
	 * @return true if the action has location info; false otherwise
	 * @since "4.0"
	 * @see #getX()
	 * @see #getY()
	 */
	boolean hasLocation();
	
	/**
	 * Returns the x coordinate of the screen position.
	 * <p>The coordinate is the map location multiplied by 256.</p>
	 * @return the x coordinate of the screen position
	 * @see #hasLocation()
	 */
	int getX();
	
	/**
	 * Returns the y coordinate of the screen position.
	 * <p>The coordinate is the map location multiplied by 256.</p>
	 * @return the y coordinate of the screen position
	 * @see #hasLocation()
	 */
	int getY();
	
	/**
	 * Tells if the action has distance info.
	 * @return true if the action has distance info; false otherwise
	 * @since "3.0"
	 * @see #getDistance()
	 */
	boolean hasDistance();
	
	/**
	 * Returns the distance info.
	 * <p>The distance is a map distance multiplied by 256.</p>
	 * @return the distance info
	 * @since "3.0"
	 * @see #hasDistance()
	 */
	int getDistance();
	
	/**
	 * Tells if the action has pitch info.
	 * @return true if the action has pitch info; false otherwise
	 * @since "3.0"
	 * @see #getPitch()
	 */
	boolean hasPitch();
	
	/**
	 * Returns the pitch info.
	 * <p>The pitch is a degree value multiplied by 256.</p>
	 * @return the pitch info
	 * @since "3.0"
	 * @see #hasPitch()
	 */
	int getPitch();
	
	/**
	 * Tells if the action has yaw info.
	 * @return true if the action has yaw info; false otherwise
	 * @since "3.0"
	 * @see #getYaw()
	 */
	boolean hasYaw();
	
	/**
	 * Returns the yaw info.
	 * <p>The yaw is a degree value multiplied by 256.</p>
	 * @return the yaw info
	 * @since "3.0"
	 * @see #hasYaw()
	 */
	int getYaw();
	
	/**
	 * Tells if the action has height offset info.
	 * @return true if the action has height offset info; false otherwise
	 * @since "3.0"
	 * @see #getHeightOffset()
	 */
	boolean hasHeightOffset();
	
	/**
	 * Returns the height offset info.
	 * <p>The height offset is a map distance multiplied by 256.</p>
	 * @return the height offset info
	 * @since "3.0"
	 * @see #hasHeightOffset()
	 */
	int getHeightOffset();
	
}
