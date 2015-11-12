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

import hu.belicza.andras.sc2gearspluginapi.api.enums.MiscObject;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.EntityParams;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.AbilityGroup;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BuildingAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Research;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Upgrade;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Ability codes used in replay files.
 * 
 * @author Andras Belicza
 */
public class AbilityCodes {
	
	/** Ability codes of train actions mapped to the trained unit.               */
	public final Map< Integer, Unit     > TRAIN_ABILITY_CODES       = new HashMap< Integer, Unit     >();
	/** Ability codes of warp actions mapped to the warped unit.                 */
	public final Map< Integer, Unit     > WARP_ABILITY_CODES        = new HashMap< Integer, Unit     >();
	/** Ability codes of train hallucination actions mapped to the trained unit. */
	public final Map< Integer, Unit     > TRAIN_HALLU_ABILITY_CODES = new HashMap< Integer, Unit     >();
	/** Ability codes of build actions mapped to the building.                   */
	public final Map< Integer, Building > BUILD_ABILITY_CODES       = new HashMap< Integer, Building >();
	/** Ability codes of upgrade actions mapped to the upgrades.                 */
	public final Map< Integer, Upgrade  > UPGRADE_ABILITY_CODES     = new HashMap< Integer, Upgrade  >();
	/** Ability codes of research actions mapped to the researches.              */
	public final Map< Integer, Research > RESEARCH_ABILITY_CODES    = new HashMap< Integer, Research >();
	/**
	 * Ability codes of common use ability actions mapped to the ability name.
	 * Ability parameters are: { abilityName, AbilityGroup, isMacro }
	 */
	public final Map< Integer, Object[] > COMMON_BASE_ABILITY_CODES = new HashMap< Integer, Object[] >();
	/**
	 * Ability codes of use unit ability actions mapped to the ability parameters.
	 * Ability parameters are: { UnitAbility, Unit }
	 */
	public final Map< Integer, Object[] > USE_UNIT_ABILITY          = new HashMap< Integer, Object[] >();
	/**
	 * Ability codes of use building ability actions mapped to the ability parameters.
	 * Ability parameters are: { BuildingAbility, building }
	 */
	public final Map< Integer, Object[] > USE_BUILDING_ABILITY      = new HashMap< Integer, Object[] >();
	
	/**
	 * Unit (and building) type names (used in select actions) mapped to their names.
	 */
	public final Map< Short, String     > UNIT_TYPE_NAME            = new HashMap< Short, String >();
	
	/** Set of unit types that are considered macro units (buildings). */
	public final Set< Short             > MACRO_UNIT_TYPE_SET       = new HashSet< Short >();
	
	/** Unit parameters.        */
	public final Map< Unit           , EntityParams > UNIT_PARAMS             = new EnumMap< Unit           , EntityParams >( Unit           .class );
	/** Warped unit parameters. */
	public final Map< Unit           , EntityParams > WARPED_UNIT_PARAMS      = new EnumMap< Unit           , EntityParams >( Unit           .class );
	/** Unit parameters.        */
	public final Map< Building       , EntityParams > BUILDING_PARAMS         = new EnumMap< Building       , EntityParams >( Building       .class );
	/** Unit parameters.        */
	public final Map< Research       , EntityParams > RESEARCH_PARAMS         = new EnumMap< Research       , EntityParams >( Research       .class );
	/** Unit parameters.        */
	public final Map< Upgrade        , EntityParams > UPGRADE_PARAMS          = new EnumMap< Upgrade        , EntityParams >( Upgrade        .class );
	/** Unit parameters.        */
	public final Map< UnitAbility    , EntityParams > UNIT_ABILITY_PARAMS     = new EnumMap< UnitAbility    , EntityParams >( UnitAbility    .class );
	/** Unit parameters.        */
	public final Map< BuildingAbility, EntityParams > BUILDING_ABILITY_PARAMS = new EnumMap< BuildingAbility, EntityParams >( BuildingAbility.class );
	/** Common ability parameters (for example Archon warp). */
	public final Map< Integer        , EntityParams > COMMON_ABILITY_PARAMS   = new HashMap< Integer        , EntityParams >();
	
	/** Maps the unit names to the corresponding unit object (Unit, Building, AbilityGroup or MiscObject).
	 * This is used to get the icon of a selected unit (selected unit name is obtained through {@link #UNIT_TYPE_NAME},
	 * so those strings are the keys here). */
	public static final Map< String, Object > UNIT_NAME_OBJECT_MAP = new HashMap< String, Object >();
	static {
		UNIT_NAME_OBJECT_MAP.put( "Colossus".intern(), Unit.COLOSSUS );
		UNIT_NAME_OBJECT_MAP.put( "Reactor".intern(), Building.REACTOR_BARRACKS );
		UNIT_NAME_OBJECT_MAP.put( "Reactor (Barracks)".intern(), Building.REACTOR_BARRACKS );
		UNIT_NAME_OBJECT_MAP.put( "Reactor (Factory)".intern(), Building.REACTOR_FACTORY );
		UNIT_NAME_OBJECT_MAP.put( "Reactor (Starport)".intern(), Building.REACTOR_STARPORT );
		UNIT_NAME_OBJECT_MAP.put( "Infested Terran".intern(), AbilityGroup.INFESTED_TERRAN );
		UNIT_NAME_OBJECT_MAP.put( "Baneling Cocoon".intern(), MiscObject.BANELING_COCOON );
		UNIT_NAME_OBJECT_MAP.put( "Baneling".intern(), AbilityGroup.MORPH_TO_BANELING );
		UNIT_NAME_OBJECT_MAP.put( "Mothership".intern(), Unit.MOTHERSHIP );
		UNIT_NAME_OBJECT_MAP.put( "Point Defense Drone".intern(), AbilityGroup.BUILD_POINT_DEFENSE_DRONE );
		UNIT_NAME_OBJECT_MAP.put( "Changeling".intern(), Unit.CHANGELING );
		UNIT_NAME_OBJECT_MAP.put( "Command Center".intern(), Building.COMMAND_CENTER );
		UNIT_NAME_OBJECT_MAP.put( "Supply Depot".intern(), Building.SUPPLY_DEPOT );
		UNIT_NAME_OBJECT_MAP.put( "Refinery".intern(), Building.REFINERY );
		UNIT_NAME_OBJECT_MAP.put( "Barracks".intern(), Building.BARRACKS );
		UNIT_NAME_OBJECT_MAP.put( "Engineering Bay".intern(), Building.ENGINEERING_BAY );
		UNIT_NAME_OBJECT_MAP.put( "Missile Turret".intern(), Building.MISSILE_TURRET );
		UNIT_NAME_OBJECT_MAP.put( "Bunker".intern(), Building.BUNKER );
		UNIT_NAME_OBJECT_MAP.put( "Sensor Tower".intern(), Building.SENSOR_TOWER );
		UNIT_NAME_OBJECT_MAP.put( "Ghost Academy".intern(), Building.GHOST_ACADEMY );
		UNIT_NAME_OBJECT_MAP.put( "Factory".intern(), Building.FACTORY );
		UNIT_NAME_OBJECT_MAP.put( "Starport".intern(), Building.STARPORT );
		UNIT_NAME_OBJECT_MAP.put( "Armory".intern(), Building.ARMORY );
		UNIT_NAME_OBJECT_MAP.put( "Fusion Core".intern(), Building.FUSION_CORE );
		UNIT_NAME_OBJECT_MAP.put( "Auto-Turret".intern(), AbilityGroup.BUILD_AUTO_TURRET );
		UNIT_NAME_OBJECT_MAP.put( "Siege Tank".intern(), Unit.SIEGE_TANK );
		UNIT_NAME_OBJECT_MAP.put( "Viking".intern(), Unit.VIKING );
		UNIT_NAME_OBJECT_MAP.put( "Viking (Assault Mode)".intern(), Unit.VIKING );
		UNIT_NAME_OBJECT_MAP.put( "Viking (Fighter Mode)".intern(), Unit.VIKING );
		UNIT_NAME_OBJECT_MAP.put( "Command Center (Flying)".intern(), Building.COMMAND_CENTER );
		UNIT_NAME_OBJECT_MAP.put( "Tech Lab (Barracks)".intern(), Building.TECH_LAB_BARRACKS );
		UNIT_NAME_OBJECT_MAP.put( "Tech Lab (Factory)".intern(), Building.TECH_LAB_FACTORY );
		UNIT_NAME_OBJECT_MAP.put( "Tech Lab (Starport)".intern(), Building.TECH_LAB_STARPORT );
		UNIT_NAME_OBJECT_MAP.put( "Factory (Flying)".intern(), Building.FACTORY );
		UNIT_NAME_OBJECT_MAP.put( "Starport (Flying)".intern(), Building.STARPORT );
		UNIT_NAME_OBJECT_MAP.put( "SCV".intern(), Unit.SCV );
		UNIT_NAME_OBJECT_MAP.put( "Barracks (Flying)".intern(), Building.BARRACKS );
		UNIT_NAME_OBJECT_MAP.put( "Supply Depot (Lowered)".intern(), Building.SUPPLY_DEPOT );
		UNIT_NAME_OBJECT_MAP.put( "Marine".intern(), Unit.MARINE );
		UNIT_NAME_OBJECT_MAP.put( "Reaper".intern(), Unit.REAPER );
		UNIT_NAME_OBJECT_MAP.put( "Ghost".intern(), Unit.GHOST );
		UNIT_NAME_OBJECT_MAP.put( "Marauder".intern(), Unit.MARAUDER );
		UNIT_NAME_OBJECT_MAP.put( "Thor".intern(), Unit.THOR );
		UNIT_NAME_OBJECT_MAP.put( "Hellion".intern(), Unit.HELLION );
		UNIT_NAME_OBJECT_MAP.put( "Medivac".intern(), Unit.MEDIVAC );
		UNIT_NAME_OBJECT_MAP.put( "Banshee".intern(), Unit.BANSHEE );
		UNIT_NAME_OBJECT_MAP.put( "Raven".intern(), Unit.RAVEN );
		UNIT_NAME_OBJECT_MAP.put( "Battlecruiser".intern(), Unit.BATTLECRUISER );
		UNIT_NAME_OBJECT_MAP.put( "Nexus".intern(), Building.NEXUS );
		UNIT_NAME_OBJECT_MAP.put( "Pylon".intern(), Building.PYLON );
		UNIT_NAME_OBJECT_MAP.put( "Assimilator".intern(), Building.ASSIMILATOR );
		UNIT_NAME_OBJECT_MAP.put( "Gateway".intern(), Building.GATEWAY );
		UNIT_NAME_OBJECT_MAP.put( "Forge".intern(), Building.FORGE );
		UNIT_NAME_OBJECT_MAP.put( "Fleet Beacon".intern(), Building.FLEET_BEACON );
		UNIT_NAME_OBJECT_MAP.put( "Twilight Council".intern(), Building.TWILIGHT_COUNCIL );
		UNIT_NAME_OBJECT_MAP.put( "Photon Cannon".intern(), Building.PHOTON_CANNON );
		UNIT_NAME_OBJECT_MAP.put( "Stargate".intern(), Building.STARGATE );
		UNIT_NAME_OBJECT_MAP.put( "Templar Archives".intern(), Building.TEMPLAR_ARCHIVES );
		UNIT_NAME_OBJECT_MAP.put( "Dark Shrine".intern(), Building.DARK_SHRINE );
		UNIT_NAME_OBJECT_MAP.put( "Robotics Bay".intern(), Building.ROBOTICS_BAY );
		UNIT_NAME_OBJECT_MAP.put( "Robotics Facility".intern(), Building.ROBOTICS_FACILITY );
		UNIT_NAME_OBJECT_MAP.put( "Cybernetics Core".intern(), Building.CYBERNETICS_CORE );
		UNIT_NAME_OBJECT_MAP.put( "Zealot".intern(), Unit.ZEALOT );
		UNIT_NAME_OBJECT_MAP.put( "Stalker".intern(), Unit.STALKER );
		UNIT_NAME_OBJECT_MAP.put( "High Templar".intern(), Unit.HIGH_TEMPLAR );
		UNIT_NAME_OBJECT_MAP.put( "Dark Templar".intern(), Unit.DARK_TEMPLAR );
		UNIT_NAME_OBJECT_MAP.put( "Sentry".intern(), Unit.SENTRY );
		UNIT_NAME_OBJECT_MAP.put( "Phoenix".intern(), Unit.PHOENIX );
		UNIT_NAME_OBJECT_MAP.put( "Carrier".intern(), Unit.CARRIER );
		UNIT_NAME_OBJECT_MAP.put( "Void Ray".intern(), Unit.VOID_RAY );
		UNIT_NAME_OBJECT_MAP.put( "Warp Prism".intern(), Unit.WARP_PRISM );
		UNIT_NAME_OBJECT_MAP.put( "Observer".intern(), Unit.OBSERVER );
		UNIT_NAME_OBJECT_MAP.put( "Immortal".intern(), Unit.IMMORTAL );
		UNIT_NAME_OBJECT_MAP.put( "Probe".intern(), Unit.PROBE );
		UNIT_NAME_OBJECT_MAP.put( "Hatchery".intern(), Building.HATCHERY );
		UNIT_NAME_OBJECT_MAP.put( "Creep Tumor".intern(), Building.CREEP_TUMOR );
		UNIT_NAME_OBJECT_MAP.put( "Extractor".intern(), Building.EXTRACTOR );
		UNIT_NAME_OBJECT_MAP.put( "Spawning Pool".intern(), Building.SPAWNING_POOL );
		UNIT_NAME_OBJECT_MAP.put( "Evolution Chamber".intern(), Building.EVOLUTION_CHAMBER );
		UNIT_NAME_OBJECT_MAP.put( "Hydralisk Den".intern(), Building.HYDRALISK_DEN );
		UNIT_NAME_OBJECT_MAP.put( "Spire".intern(), Building.SPIRE );
		UNIT_NAME_OBJECT_MAP.put( "Ultralisk Cavern".intern(), Building.ULTRALISK_CAVERN );
		UNIT_NAME_OBJECT_MAP.put( "Infestation Pit".intern(), Building.INFESTATION_PIT );
		UNIT_NAME_OBJECT_MAP.put( "Nydus Network".intern(), Building.NYDUS_NETWORK );
		UNIT_NAME_OBJECT_MAP.put( "Baneling Nest".intern(), Building.BANELING_NEST );
		UNIT_NAME_OBJECT_MAP.put( "Roach Warren".intern(), Building.ROACH_WARREN );
		UNIT_NAME_OBJECT_MAP.put( "Spine Crawler".intern(), Building.SPINE_CRAWLER );
		UNIT_NAME_OBJECT_MAP.put( "Spore Crawler".intern(), Building.SPORE_CRAWLER );
		UNIT_NAME_OBJECT_MAP.put( "Lair".intern(), Building.LAIR );
		UNIT_NAME_OBJECT_MAP.put( "Hive".intern(), Building.HIVE );
		UNIT_NAME_OBJECT_MAP.put( "Greater Spire".intern(), AbilityGroup.MUTATE_INTO_GREATER_SPIRE );
		UNIT_NAME_OBJECT_MAP.put( "Egg".intern(), Unit.EGG );
		UNIT_NAME_OBJECT_MAP.put( "Drone".intern(), Unit.DRONE );
		UNIT_NAME_OBJECT_MAP.put( "Zergling".intern(), Unit.ZERGLING );
		UNIT_NAME_OBJECT_MAP.put( "Overlord".intern(), Unit.OVERLORD );
		UNIT_NAME_OBJECT_MAP.put( "Hydralisk".intern(), Unit.HYDRALISK );
		UNIT_NAME_OBJECT_MAP.put( "Mutalisk".intern(), Unit.MUTALISK );
		UNIT_NAME_OBJECT_MAP.put( "Ultralisk".intern(), Unit.ULTRALISK );
		UNIT_NAME_OBJECT_MAP.put( "Roach".intern(), Unit.ROACH );
		UNIT_NAME_OBJECT_MAP.put( "Infestor".intern(), Unit.INFESTOR );
		UNIT_NAME_OBJECT_MAP.put( "Corruptor".intern(), Unit.CORRUPTOR );
		UNIT_NAME_OBJECT_MAP.put( "Brood Lord Cocoon".intern(), MiscObject.BROOD_LORD_COCOON );
		UNIT_NAME_OBJECT_MAP.put( "Brood Lord".intern(), Unit.BROOD_LORD );
		UNIT_NAME_OBJECT_MAP.put( "Baneling (Burrowed)".intern(), Unit.BANELING );
		UNIT_NAME_OBJECT_MAP.put( "Drone (Burrowed)".intern(), Unit.DRONE );
		UNIT_NAME_OBJECT_MAP.put( "Hydralisk (Burrowed)".intern(), Unit.HYDRALISK );
		UNIT_NAME_OBJECT_MAP.put( "Roach (Burrowed)".intern(), Unit.ROACH );
		UNIT_NAME_OBJECT_MAP.put( "Zergling (Burrowed)".intern(), Unit.ZERGLING );
		UNIT_NAME_OBJECT_MAP.put( "Infested Terran (Burrowed)".intern(), AbilityGroup.INFESTED_TERRAN );
		UNIT_NAME_OBJECT_MAP.put( "Queen (Burrowed)".intern(), Unit.QUEEN );
		UNIT_NAME_OBJECT_MAP.put( "Queen".intern(), Unit.QUEEN );
		UNIT_NAME_OBJECT_MAP.put( "Infestor (Burrowed)".intern(), Unit.INFESTOR );
		UNIT_NAME_OBJECT_MAP.put( "Overseer Cocoon".intern(), MiscObject.OVERSEER_COCOON );
		UNIT_NAME_OBJECT_MAP.put( "Overseer".intern(), Unit.OVERSEER );
		UNIT_NAME_OBJECT_MAP.put( "Planetary Fortress".intern(), AbilityGroup.UPGRADE_TO_PLANETARY_FORTRESS );
		UNIT_NAME_OBJECT_MAP.put( "Ultralisk (Burrowed)".intern(), Unit.ULTRALISK );
		UNIT_NAME_OBJECT_MAP.put( "Orbital Command".intern(), AbilityGroup.UPGRADE_TO_ORBITAL_COMMAND );
		UNIT_NAME_OBJECT_MAP.put( "Warp Gate".intern(), AbilityGroup.UPGRADE_TO_WARP_GATE );
		UNIT_NAME_OBJECT_MAP.put( "Orbital Command (Flying)".intern(), AbilityGroup.UPGRADE_TO_ORBITAL_COMMAND );
		UNIT_NAME_OBJECT_MAP.put( "Warp Prism (Phasing Mode)".intern(), Unit.WARP_PRISM );
		UNIT_NAME_OBJECT_MAP.put( "Creep Tumor (Burrowed)".intern(), Building.CREEP_TUMOR );
		UNIT_NAME_OBJECT_MAP.put( "Spine Crawler (Uprooted)".intern(), Building.SPINE_CRAWLER );
		UNIT_NAME_OBJECT_MAP.put( "Spore Crawler (Uprooted)".intern(), Building.SPORE_CRAWLER );
		UNIT_NAME_OBJECT_MAP.put( "Archon".intern(), Unit.ARCHON );
		UNIT_NAME_OBJECT_MAP.put( "Nydus Worm (Canal)".intern(), Building.NYDUS_WORM );
		UNIT_NAME_OBJECT_MAP.put( "Larva".intern(), AbilityGroup.SPAWN_LARVA );
		UNIT_NAME_OBJECT_MAP.put( "Rich Mineral Field".intern(), MiscObject.RICH_MINERAL_FIELD );
		UNIT_NAME_OBJECT_MAP.put( "Xel'Naga Tower".intern(), MiscObject.XEL_NAGA_TOWER );
		UNIT_NAME_OBJECT_MAP.put( "Infested Swarm Egg".intern(), MiscObject.INFESTED_SWARM_EGG );
		UNIT_NAME_OBJECT_MAP.put( "MULE".intern(), Unit.MULE );
		UNIT_NAME_OBJECT_MAP.put( "Broodling".intern(), Unit.BROODLING );
		UNIT_NAME_OBJECT_MAP.put( "Lyote".intern(), MiscObject.LYOTE );
		UNIT_NAME_OBJECT_MAP.put( "Urubu".intern(), MiscObject.URUBU );
		UNIT_NAME_OBJECT_MAP.put( "Male Karak".intern(), MiscObject.MALE_KARAK );
		UNIT_NAME_OBJECT_MAP.put( "Female Karak".intern(), MiscObject.FEMALE_KARAK );
		UNIT_NAME_OBJECT_MAP.put( "Male Ursadak".intern(), MiscObject.MALE_URSADAK );
		UNIT_NAME_OBJECT_MAP.put( "Female Ursadak".intern(), MiscObject.FEMALE_URSADAK );
		UNIT_NAME_OBJECT_MAP.put( "Ursadak Calf".intern(), MiscObject.FEMALE_URSADAK );
		UNIT_NAME_OBJECT_MAP.put( "Male Ursadak (Exotic)".intern(), MiscObject.MALE_URSADAK );
		UNIT_NAME_OBJECT_MAP.put( "Female Ursadak (Exotic)".intern(), MiscObject.FEMALE_URSADAK );
		UNIT_NAME_OBJECT_MAP.put( "Automaton 2000".intern(), MiscObject.AUTOMATON_2000 );
		UNIT_NAME_OBJECT_MAP.put( "Artosis Bot 2000".intern(), MiscObject.AUTOMATON_2000 );
		UNIT_NAME_OBJECT_MAP.put( "Tasteless Bot 2000".intern(), MiscObject.AUTOMATON_2000 );
		UNIT_NAME_OBJECT_MAP.put( "Scantipede".intern(), MiscObject.SCANTIPEDE );
		UNIT_NAME_OBJECT_MAP.put( "Mineral Field".intern(), MiscObject.MINERAL_FIELD );
		UNIT_NAME_OBJECT_MAP.put( "Vespene Geyser".intern(), MiscObject.VESPENE_GEYSER );
		UNIT_NAME_OBJECT_MAP.put( "Space Platform Geyser".intern(), MiscObject.SPACE_PLATFORM_GEYSER );
		UNIT_NAME_OBJECT_MAP.put( "Rich Vespene Geyser".intern(), MiscObject.VESPENE_GEYSER );
		UNIT_NAME_OBJECT_MAP.put( "Braxis Alpha - Destructible Debris".intern(), MiscObject.DESTRUCTIBLE_DEBRIS );
		UNIT_NAME_OBJECT_MAP.put( "Destructible Debris".intern(), MiscObject.DESTRUCTIBLE_DEBRIS );
		UNIT_NAME_OBJECT_MAP.put( "Destructible Rock".intern(), MiscObject.DESTRUCTIBLE_ROCK );
		UNIT_NAME_OBJECT_MAP.put( "The Gift of Freedom (Mengsk Statue Alone)".intern(), MiscObject.MENGSK_STATUE_ALONE );
		UNIT_NAME_OBJECT_MAP.put( "Glory of the Dominion (Mengsk Statue)".intern(), MiscObject.MENGSK_STATUE );
		UNIT_NAME_OBJECT_MAP.put( "The Wolves of Korhal (Wolf Statue)".intern(), MiscObject.WOLF_STATUE );
		UNIT_NAME_OBJECT_MAP.put( "Capitol Statue (Globe Statue)".intern(), MiscObject.GLOBE_STATUE );
		
		// HotS additions
		UNIT_NAME_OBJECT_MAP.put( "Unbuildable Destructible Rocks".intern(), MiscObject.UNBUILDABLE_DESTRUCTIBLE_ROCKS );
		UNIT_NAME_OBJECT_MAP.put( "Unbuildable Destructible Bricks".intern(), MiscObject.UNBUILDABLE_DESTRUCTIBLE_BRICKS );
		UNIT_NAME_OBJECT_MAP.put( "Unbuildable Destructible Plates".intern(), MiscObject.UNBUILDABLE_DESTRUCTIBLE_PLATES );
		UNIT_NAME_OBJECT_MAP.put( "Oracle".intern(), Unit.ORACLE );
		UNIT_NAME_OBJECT_MAP.put( "Tempest".intern(), Unit.TEMPEST );
		UNIT_NAME_OBJECT_MAP.put( "Widow Mine".intern(), Unit.WIDOW_MINE );
		UNIT_NAME_OBJECT_MAP.put( "Hellbat".intern(), Unit.HELLBAT );
		UNIT_NAME_OBJECT_MAP.put( "Swarm Host".intern(), Unit.SWARM_HOST );
		UNIT_NAME_OBJECT_MAP.put( "Swarm Host (Burrowed)".intern(), Unit.SWARM_HOST );
		UNIT_NAME_OBJECT_MAP.put( "Viper".intern(), Unit.VIPER );
		UNIT_NAME_OBJECT_MAP.put( "Mothership Core".intern(), Unit.MOTHERSHIP_CORE );
		UNIT_NAME_OBJECT_MAP.put( "Locust".intern(), Unit.LOCUST );
		
		// TODO: Garbage?
	}
	
}
