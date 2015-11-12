/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.sc2replay.importing;

import hu.belicza.andras.sc2gearspluginapi.api.GeneralUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;

/**
 * Specification of a replay. Contains info that are needed to create a virtual replay.
 * 
 * @author Andras Belicza
 * 
 * @see GameSpeed
 * @see PlayerSpecification
 * @see GeneralUtilsApi#openReplaySpecification(ReplaySpecification)
 */
public class ReplaySpecification {
	
	/** Version of the replay.                       */
	public int[]                 version;
	
	/** Game speed.                                  */
	public GameSpeed             gameSpeed;
	
	/** Specifications of the players of the replay. */
	public PlayerSpecification[] playerSpecifications;
	
}
