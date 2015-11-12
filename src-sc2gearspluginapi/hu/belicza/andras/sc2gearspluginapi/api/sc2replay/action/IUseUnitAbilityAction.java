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
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitAbility;

/**
 * Represents a Use unit ability action.
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see IGameEvents
 */
public interface IUseUnitAbilityAction extends IBaseUseAbilityAction {
	
	/**
	 * Returns the unit whose ability is used.
	 * @return the unit whose ability is used
	 * @see Unit
	 */
	Unit getUnit();
	
	/**
	 * Returns the issued unit ability.
	 * @return the issued unit ability
	 * @see UnitAbility
	 */
	UnitAbility getUnitAbility();
	
}
