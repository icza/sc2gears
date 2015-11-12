/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.sc2replay;

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BuildingAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IBaseUseAbilityAction;

/**
 * Parameters of different entities (like units, buildings, researches, upgrades, abilities).
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see IBaseUseAbilityAction
 */
public class EntityParams {
	
	/** Mineral cost.                 */
	public int minerals;
	/** Gast cost.                    */
	public int gas;
	/** Build or duration time (sec). */
	public int time;
	/** Required supply.<br>
	 * If the entity specifies a supply providing unit or ability (for example {@link Building#PYLON}, {@link Unit#OVERLORD},
	 * {@link Building#HATCHERY}, {@link Building#NEXUS}, {@link Building#COMMAND_CENTER}, {@link BuildingAbility#CALLDOWN_EXTRA_SUPPLIES}),
	 * this value is negative. */
	public int supply;
	
}
