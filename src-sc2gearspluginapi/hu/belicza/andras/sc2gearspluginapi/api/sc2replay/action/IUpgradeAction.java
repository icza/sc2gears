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
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Upgrade;

/**
 * Represents a Upgrade action.
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see IGameEvents
 */
public interface IUpgradeAction extends IBaseUseAbilityAction {
	
	/**
	 * Returns the issued upgrade.
	 * @return the issued upgrade
	 * @see Upgrade
	 */
	Upgrade getUpgrade();
	
}
