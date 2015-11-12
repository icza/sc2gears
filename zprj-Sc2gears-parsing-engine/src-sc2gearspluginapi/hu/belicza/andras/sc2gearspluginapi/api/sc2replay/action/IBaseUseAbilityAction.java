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

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.EntityParams;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IGameEvents;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.AbilityGroup;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitTier;

/**
 * Represents a Base use ability action.
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see IGameEvents
 */
public interface IBaseUseAbilityAction extends IAction {
	
	/** Interface version. */
	String VERSION = "3.0";
	
	/**
	 * Returns the ability code.
	 * @return the ability code
	 */
	int getAbilityCode();
	
	/**
	 * Returns the name of the ability if known.
	 * @return the name of the ability if known; <code>null</code> otherwise
	 */
	String getAbilityName();
	
	/**
	 * Returns the ability group describing the action.
	 * @return the ability group describing the action
	 * @see AbilityGroup
	 */
	AbilityGroup getAbilityGroup();
	
	/**
	 * Returns if the action is queued (issued while SHIFT pressed).
	 * <p>Works only from replay version 1.1.</p>
	 * @return true if the action is queued (issued while SHIFT pressed); false otherwise
	 * @since "3.0"
	 */
	boolean isQueued();
	
	/**
	 * Returns if the action is a right click.
	 * <p>Works only from replay version 1.1.</p>
	 * @return true if the action is a right click; false otherwise
	 * @since "3.0"
	 */
	boolean isRightClick();
	
	/**
	 * Returns if the action is a click on the wireframe.
	 * <p>Works only from replay version 1.1.</p>
	 * @return true if the action is a click on the wireframe; false otherwise
	 * @since "3.0"
	 */
	boolean isWireframeClick();
	
	/**
	 * Returns if the action is a toggle ability (toggle auto-cast).
	 * <p>Works only from replay version 1.1.</p>
	 * @return true if the action is a toggle ability; false otherwise
	 * @since "3.0"
	 */
	boolean isToggleAbility();
	
	/**
	 * Returns if the action is an autocast (autocast=on).
	 * <p>Works only from replay version 1.1.</p>
	 * @return true if the action is an autocast; false otherwise
	 * @since "3.0"
	 */
	boolean isAutocast();
	
	/**
	 * Returns if the action is wireframe unload (clicking on a unit in the wireframe).
	 * <p>Works only from replay version 1.1.</p>
	 * @return true if the action is wireframe unload; false otherwise
	 * @since "3.0"
	 */
	boolean isWireframeUnload();
	
	/**
	 * Returns if the action is wireframe cancel (clicking on a queued unit for example in the wireframe).
	 * <p>Works only from replay version 1.1.</p>
	 * @return true if the action is wireframe cancel; false otherwise
	 * @since "3.0"
	 */
	boolean isWireframeCancel();
	
	/**
	 * Returns if the action is a minimap click.
	 * <p>Works only from replay version 1.1.</p>
	 * @return true if the action is a minimap click; false otherwise
	 * @since "3.0"
	 */
	boolean isMinimapClick();
	
	/**
	 * Returns if the action is an ability failed.
	 * <p>Works only from replay version 1.3.3.</p>
	 * @return true if the action is an ability failed; false otherwise
	 * @since "3.0"
	 */
	boolean isAbilityFailed();
	
	/**
	 * Returns the id of the unit target.
	 * 
	 * <p>If the action has no unit target, it has a value of -1.</p>
	 * 
	 * @return the id of the unit target
	 * 
	 * @since "3.0"
	 * 
	 * @see #getTargetType()
	 * @see #hasTargetUnit()
	 * @see IGameEvents#getUnitName(short)
	 */
	int getTargetId();
	
	/**
	 * Returns the type of the unit target.
	 * 
	 * <p>If the action has no unit target, it has a value of -1.</p>
	 * 
	 * @return the type of the unit target
	 * 
	 * @since "3.0"
	 * 
	 * @see #getTargetId()
	 * @see #hasTargetUnit()
	 * @see IGameEvents#getUnitName(short)
	 */
	int getTargetType();
	
	/**
	 * Returns the x coordinate of the target location.
	 * 
	 * <p>If the action has no location target, it has a value of 256.<br>
	 * Coordinate values are the map location multiplied by 65536.</p>
	 * 
	 * @return the x coordinate the target location
	 * 
	 * @since "3.0"
	 * 
	 * @see #getTargetY()
	 * @see #hasTargetPoint()
	 */
	int getTargetX();
	
	/**
	 * Returns the y coordinate of the target location.
	 * 
	 * <p>If the action has no location target, it has a value of 256.<br>
	 * Coordinate values are the map location multiplied by 65536.</p>
	 * 
	 * @return the y coordinate the target location
	 * 
	 * @since "3.0"
	 * 
	 * @see #getTargetX()
	 * @see #hasTargetPoint()
	 */
	int getTargetY();
	
	/**
	 * Tells if the action has a unit target.
	 * @return true if the action has a unit target; false otherwise
	 * 
	 * @since "3.0"
	 * 
	 * @see #hasTargetPoint()
	 */
	boolean hasTargetUnit();
	
	/**
	 * Tells if the action has a point target.
	 * @return true if the action has a target point; false otherwise
	 * @see #hasTargetUnit()
	 */
	boolean hasTargetPoint();
	
	/**
	 * Returns the entity parameters of the action.
	 * <p>Some actions do not have entity params in which case <code>null</code> is returned.</p>
	 * @return the entity parameters of the action
	 * @see EntityParams
	 */
	EntityParams getEntityParams();
	
	/**
	 * If the action has a unit parameter (or a target unit in case of a "transformation" action), returns the tier of that unit.
	 * @return the tier of the unit parameter (or the target unit in case of a "transformation" action) if available; <code>null</code> otherwise
	 * @see UnitTier
	 */
	UnitTier getUnitTier();
	
}
