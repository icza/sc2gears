/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.sc2replay;

import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;
import hu.belicza.andras.sc2gearspluginapi.api.enums.League;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ExpansionLevel;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Format;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Gateway;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;

/**
 * Enum value array caches.
 * 
 * @author Andras Belicza
 */
public class EnumCache {
	
	public static final League[]         LEAGUES      = League.values();
	
	public static final ExpansionLevel[] EXPANSIONS   = ExpansionLevel.values();
	
	public static final Race[]           RACES        = Race.values();
	
	public static final PlayerType[]     PLAYER_TYPES = PlayerType.values();
	
	public static final Gateway[]        GATEWAYS     = Gateway.values();
	
	public static final Format[]         FORMATS      = Format.values();
	
	public static final GameSpeed[]      GAME_SPEEDS  = GameSpeed.values();
	
	public static final GameType[]       GAME_TYPES   = GameType.values();
	
	public static final Building[]       BUILDINGS    = Building.values();
	
	
	public static final IconSize[]       ICON_SIZES   = IconSize.values();
	
}
