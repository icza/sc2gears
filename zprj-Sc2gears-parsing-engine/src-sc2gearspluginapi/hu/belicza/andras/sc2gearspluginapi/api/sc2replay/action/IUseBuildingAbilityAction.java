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
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BuildingAbility;

/**
 * Represents a Use building ability action.
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see IGameEvents
 */
public interface IUseBuildingAbilityAction extends IBaseUseAbilityAction {
	
	/**
	 * Returns the building whose ability is used.
	 * @return the building whose ability is used
	 * @see Building
	 */
	Building getBuilding();
	
	/**
	 * Returns the issued building ability.
	 * @return the issued building ability
	 * @see BuildingAbility
	 */
	BuildingAbility getBuildingAbility();
	
}
