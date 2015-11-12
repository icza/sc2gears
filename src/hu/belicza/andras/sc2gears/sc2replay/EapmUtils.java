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

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.BaseUseAbilityAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.BuildAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.HotkeyAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.ResearchAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.SelectAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.UpgradeAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.UseBuildingAbilityAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.UseUnitAbilityAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.AbilityGroup;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BuildingAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IAction;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of the EAPM algorithm.
 * 
 * <p>For details and algorithm description see <a href="https://sites.google.com/site/sc2gears/features/replay-analyzer/apm-types">APM Types</a>.</p>
 * 
 * @author Andras Belicza
 */
public class EapmUtils {
	
	/** Version of the EAPM algorithm. */
	public static final String ALGORITHM_VERSION = "1.4";
	
	/** Set of unit abilities that are ineffective if repeated fast. */
	private static final Set< UnitAbility > FAST_REPEAT_UNIT_ABILITY_SET = EnumSet.of(
		UnitAbility.TOGGLE_AUTO_REPAIR, UnitAbility.NEURAL_PARASITE,
		UnitAbility.TOGGLE_AUTO_HEAL, UnitAbility.CHARGE, UnitAbility.ATTACK_STRUCTURE,
		UnitAbility.TOGGLE_AUTO_ATTACK_STRUCTURE, UnitAbility.TOGGLE_AUTO_CHARGE, UnitAbility.TOGGLE_AUTO_SPAWN_LOCUSTS
	);
	
	/** Set of building abilities that are ineffective if repeated fast. */
	private static final Set< BuildingAbility > FAST_REPEAT_BUILDING_ABILITY_SET = EnumSet.of(
		BuildingAbility.SET_RALLY_POINT, BuildingAbility.SET_WORKER_RALLY_POINT, BuildingAbility.LAND
	);
	
	/** Set of action names that are ineffective if repeated fast. */
	private static final Set< String > FAST_REPEAT_ACTION_NAME_SET = new HashSet< String >();
	static {
		Collections.addAll( FAST_REPEAT_ACTION_NAME_SET,
			"Right click", "Stop", "Hold position", "Move", "Patrol", "Scan Move", "Attack", "Set rally point",
			"Hold fire", "Halt (on building)", "Halt (on unit)", "Attack (Bunker)", "Stop (Bunker)" );
	}
	
	/** Set of unit abilities that are ineffective if repeated without time restriction. */
	private static final Set< UnitAbility > REPEAT_UNIT_ABILITY_SET = EnumSet.of(
		UnitAbility.GATHER_RESOURCES_TERRAN, UnitAbility.GATHER_RESOURCES_PROTOSS, UnitAbility.GATHER_RESOURCES_ZERG,
		UnitAbility.RETURN_CARGO, UnitAbility.CLOAK, UnitAbility.DECLOAK, UnitAbility.SIEGE_MODE, UnitAbility.TANK_MODE,
		UnitAbility.ASSAULT_MODE, UnitAbility.FIGHTER_MODE, UnitAbility.BURROW, UnitAbility.UNBURROW,
		UnitAbility.PHASING_MODE, UnitAbility.TRANSPORT_MODE, UnitAbility.GENERATE_CREEP, UnitAbility.STOP_GENERATING_CREEP,
		UnitAbility.WEAPONS_FREE, UnitAbility.HELLION_MODE, UnitAbility.BATTLE_MODE,
		UnitAbility.HIGH_IMPACT_PAYLOAD, UnitAbility.EXPLOSIVE_PAYLOAD, UnitAbility.ACTIVATE_MINE, UnitAbility.DEACTIVATE_MINE
	);
	
	/** Set of building abilities that are ineffective if repeated without time restriction. */
	private static final Set< BuildingAbility > REPEAT_BUILDING_ABILITY_SET = EnumSet.of(
		BuildingAbility.CANCEL_AN_ADDON, BuildingAbility.MUTATE_INTO_LAIR, BuildingAbility.CANCEL_LAIR_UPGRADE,
		BuildingAbility.MUTATE_INTO_HIVE, BuildingAbility.CANCEL_HIVE_UPGRADE, BuildingAbility.MUTATE_INTO_GREATER_SPIRE,
		BuildingAbility.CANCEL_GREATER_SPIRE_UPGRADE, BuildingAbility.UPGRADE_TO_PLANETARY_FORTRESS, BuildingAbility.CANCEL_PLANETARY_FORTRESS_UPGRADE,
		BuildingAbility.UPGRADE_TO_ORBITAL_COMMAND, BuildingAbility.CANCEL_ORBITAL_COMMAND_UPGRADE, BuildingAbility.SALVAGE,
		BuildingAbility.LIFT_OFF, BuildingAbility.UPROOT, BuildingAbility.ROOT, BuildingAbility.LOWER, BuildingAbility.RAISE
	);
	
	/** Set of action names that are ineffective if repeated without time restriction. */
	private static final Set< String > REPEAT_ACTION_NAME_SET = new HashSet< String >();
	static {
		Collections.addAll( REPEAT_ACTION_NAME_SET,
			"Archon Warp (High Templar or Dark Templar)" );
	}
	
	/**
	 * Reason for classifying an action <i>ineffective</i>.
	 * 
	 * @author Andras Belicza
	 */
	public enum IneffectiveReason {
		ABILITY_FAILED              ( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.abilityFailed"            , "fugue/cross.png"                ),
		
		FAST_CANCEL                 ( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.fastCancel"               , ReplayConsts.AbilityGroup.CANCEL ),
		
		FAST_REPEAT_UNIT_ABILITY    ( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.fastRepeatUnitAbility"    , "fugue/target.png"               ),
		FAST_REPEAT_BUILDING_ABILITY( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.fastRepeatBuildingAbility", "fugue/building-hedge.png"       ),
		FAST_REPEAT_OTHER_ABILITY   ( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.fastRepeatOtherAbility"   , "fugue/universal.png"            ),
		
		FAST_REPEAT_SAME_HOTKEY     ( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.fastRepeatSameHotkey"     , "fugue/keyboard.png"             ),
		FAST_SELECTION_CHANGE       ( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.fastSelectionChange"      , "fugue/selection-select.png"     ),
		
		REPEAT_HOTKEY_ASSIGN        ( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.repeatHotkeyAssign"       , "fugue/keyboard.png"             ),
		
		REPEAT_UNIT_ABILITY         ( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.repeatUnitAbility"        , "fugue/target.png"               ),
		REPEAT_BUILDING_ABILITY     ( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.repeatBuildingAbility"    , "fugue/building-hedge.png"       ),
		REPEAT_OTHER_ABILITY        ( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.repeatOtherAbility"       , "fugue/universal.png"            ),
		REPEAT_RESEARCH             ( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.repeatResearch"           , "fugue/flask.png"                ),
		REPEAT_UPGRADE              ( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.repeatUpgrade"            , "fugue/hammer-screwdriver.png"   ),
		REPEAT_BUILDING             ( "module.repAnalyzer.tab.charts.apmRedundancyDistribution.ineffectiveReason.repeatBuilding"           , "fugue/building.png"             );
		
		/** Cache of the string value.  */
		public final String stringValue;
		/** Default entity icon object. */
		public final Object defaultIconEntity;
		
		/**
		 * Creates a new Tab.
		 * @param textKey           key of the text representation
		 * @param defaultIconEntity default icon entity icon object
		 */
		private IneffectiveReason( final String textKey, final Object defaultIconEntity ) {
			this.stringValue              = Language.getText( textKey );
			this.defaultIconEntity = defaultIconEntity;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Returns the reason for classifying the specified action <i>ineffective</i>.
	 * Ineffective actions must be excluded from EAPM calculation.
	 * 
	 * <p>See <a href="https://sites.google.com/site/sc2gears/features/replay-analyzer/apm-types">APM Types</a> for EAPM algorithm details.</p>
	 * 
	 * @param actions     reference to the actions array
	 * @param actionIndex index of the action to be tested
	 * 
	 * @return the reason for classifying the specified action <i>ineffective</i> <b>if</b> it is ineffective; <code>null</code> otherwise
	 */
	public static IneffectiveReason getActionIneffectiveReason( final IAction[] actions_, final int actionIndex ) {
		// Conversion from time limits to frames (time limit => frame limit):
		// Time values are real time values. The conversion applies to Faster game speed.
		// Seconds have to be multiplied by 36/26, and multiply the result by 64 to get frame values.
		// Example: 1 sec => 1 * 36/26 * 64 = 88.6 Frames (it's 1 second in real time but it's more than 1 second in game-time).
		
		final Action[] actions = (Action[]) actions_;
		final Action action = actions[ actionIndex ];
		
		if ( action instanceof BaseUseAbilityAction && ( (BaseUseAbilityAction) action ).isAbilityFailed() )
			return IneffectiveReason.ABILITY_FAILED;
		
		// Shortcut to the previous action
		Action tempAction;
		Action prevAction = null;
		int    prevActionIndex;
		for ( prevActionIndex = actionIndex - 1; prevActionIndex >= 0; prevActionIndex-- )
			if ( ( tempAction = actions[ prevActionIndex ] ).player == action.player && tempAction.type != ActionType.INACTION ) {
				prevAction = tempAction;
				break;
			}
		
		if ( prevAction == null )
			return null;
		
		// TODO Unit queue overflow?
		
		final int actionDelay = action.frame - prevAction.frame;
		
		// Too fast cancel (~0.83 sec => <74 frames): both the cancel action and the action that was canceled
		if ( actionDelay < 74 ) {
			// This action:
			if ( action instanceof BaseUseAbilityAction && ( (BaseUseAbilityAction) action ).abilityGroup == AbilityGroup.CANCEL
				&& !isActionSelect( prevAction ) )
				return IneffectiveReason.FAST_CANCEL;
		}
		// Next action:
		if ( !isActionSelect( action ) ) {
			for ( int nextActionIndex = actionIndex + 1; nextActionIndex < actions.length; nextActionIndex++ ) {
				if ( ( tempAction = actions[ nextActionIndex ] ).frame - action.frame >= 74 )
					break;
				if ( tempAction.player == action.player && tempAction.type != ActionType.INACTION ) {
					if ( tempAction instanceof BaseUseAbilityAction && ( (BaseUseAbilityAction) tempAction ).abilityGroup == AbilityGroup.CANCEL )
						return IneffectiveReason.FAST_CANCEL;
					break;
				}
			}
		}
		
		
		// Too fast repeat of commands in a short time (~0.42 sec => <37 frames)
		// (regardless of its destination, if destination is different/far, then the first one was useless)
		if ( actionDelay <= 37 && prevAction.getClass().equals( action.getClass() ) ) {
			// TODO quickly adding way points which are not too close should be effective
			if ( action instanceof UseUnitAbilityAction ) {
				final UseUnitAbilityAction ua = (UseUnitAbilityAction) action;
				if ( ( (UseUnitAbilityAction) prevAction ).unitAbility == ua.unitAbility
					&& FAST_REPEAT_UNIT_ABILITY_SET.contains( ua.unitAbility ) )
					return IneffectiveReason.FAST_REPEAT_UNIT_ABILITY;
			}
			
			if ( action instanceof UseBuildingAbilityAction ) {
				final UseBuildingAbilityAction ba = (UseBuildingAbilityAction) action;
				if ( ( (UseBuildingAbilityAction) prevAction ).buildingAbility == ba.buildingAbility
					&& FAST_REPEAT_BUILDING_ABILITY_SET.contains( ba.buildingAbility ) )
					return IneffectiveReason.FAST_REPEAT_BUILDING_ABILITY;
			}
			
			if ( action instanceof BaseUseAbilityAction ) {
				final BaseUseAbilityAction bua = (BaseUseAbilityAction) action; 				
				if ( bua.abilityName != null && bua.abilityName.equals( ( (BaseUseAbilityAction) prevAction ).abilityName ) && FAST_REPEAT_ACTION_NAME_SET.contains( bua.abilityName ) )
					return IneffectiveReason.FAST_REPEAT_OTHER_ABILITY;
			}
		}
		
		// Too fast switch away from or reselecting the same selected unit = no use of selecting it.
		// By too fast I mean it isn't even enough to check the object state
		// ~0.25 sec => <=22 Frames
		if ( actionDelay <= 22 ) {
			if ( isActionSelect( action ) )
				if ( isActionSelect( prevAction ) ) {
					if ( action instanceof HotkeyAction && prevAction instanceof HotkeyAction
						&& ( (HotkeyAction) action ).getNumber() == ( (HotkeyAction) prevAction ).getNumber() ) {
						// Exclude double tapping a hotkey, it's only ineffective if it was pressed more than 3 times
						// For this we need the previous-previous action:
						for ( int i = prevActionIndex - 1; i >= 0; i-- )
							if ( ( tempAction = actions[ prevActionIndex ] ).player == action.player && tempAction.type != ActionType.INACTION ) {
								if ( actions[ i ] instanceof HotkeyAction && ( (HotkeyAction) action ).getNumber() == ( (HotkeyAction) actions[ i ] ).getNumber() )
									return IneffectiveReason.FAST_REPEAT_SAME_HOTKEY;
								break;
							}
					}
					else
						return IneffectiveReason.FAST_SELECTION_CHANGE; // Different hotkeys or 2 select actions or (hotkey;select) pair
				}
		}
		
		// Repeat of commands without time restriction
		if ( prevAction.getClass().equals( action.getClass() ) ) {
			if ( action instanceof HotkeyAction && !( (HotkeyAction) action ).isSelect() && !( (HotkeyAction) prevAction ).isSelect() && ( (HotkeyAction) action ).getNumber() == ( (HotkeyAction) prevAction ).getNumber() )
				return IneffectiveReason.REPEAT_HOTKEY_ASSIGN;
			
			if ( action instanceof UseUnitAbilityAction ) {
				final UseUnitAbilityAction ua = (UseUnitAbilityAction) action;
				if ( ( (UseUnitAbilityAction) prevAction ).unitAbility == ua.unitAbility
					&& REPEAT_UNIT_ABILITY_SET.contains( ua.unitAbility ) )
					return IneffectiveReason.REPEAT_UNIT_ABILITY;
			}
			
			if ( action instanceof UseBuildingAbilityAction ) {
				final UseBuildingAbilityAction ba = (UseBuildingAbilityAction) action;
				if ( ( (UseBuildingAbilityAction) prevAction ).buildingAbility == ba.buildingAbility
					&& REPEAT_BUILDING_ABILITY_SET.contains( ba.buildingAbility ) )
					return IneffectiveReason.REPEAT_BUILDING_ABILITY;
			}
			
			if ( action instanceof BaseUseAbilityAction ) {
				final BaseUseAbilityAction bua = (BaseUseAbilityAction) action; 				
				if ( bua.abilityName != null && ( (BaseUseAbilityAction) prevAction ).abilityName == bua.abilityName && REPEAT_ACTION_NAME_SET.contains( bua.abilityName ) )
					return IneffectiveReason.REPEAT_OTHER_ABILITY;
			}
			
			// Only the same research/upgrade counts as ineffective, because different researches/upgrades can be queued
			if ( action instanceof ResearchAction && ( (ResearchAction) action ).research == ( (ResearchAction) prevAction ).research )
				return IneffectiveReason.REPEAT_RESEARCH;
			if ( action instanceof UpgradeAction  && ( (UpgradeAction ) action ).upgrade  == ( (UpgradeAction ) prevAction ).upgrade  )
				return IneffectiveReason.REPEAT_UPGRADE;
			
			if ( action instanceof BuildAction ) {
				final BuildAction ba           = (BuildAction) action;
				final Building    prevBuilding = ( (BuildAction) prevAction ).building;
				if ( ba.building == Building.LAIR && prevBuilding == ba.building )
					return IneffectiveReason.REPEAT_BUILDING;
				if ( ba.building == Building.HIVE && prevBuilding == ba.building )
					return IneffectiveReason.REPEAT_BUILDING;
			}
		}
		
		return null;
	}
	
	/**
	 * Tells if the specified action is some kind of select action.<br>
	 * Select actions are "Select" and "Hotkey select"
	 * @param action action to be examined
	 * @return true if the specified action is a select action; false otherwise
	 */
	private static boolean isActionSelect( final Action action ) {
		return action instanceof SelectAction || action instanceof HotkeyAction && ( (HotkeyAction) action ).isSelect();
	}
	
}
