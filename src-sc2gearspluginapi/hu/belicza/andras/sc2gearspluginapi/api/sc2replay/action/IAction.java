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
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerSelectionTracker;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;

/**
 * Represents a player action.
 * 
 * <p>The actions override {@link Object#toString()} to have a nice string representation.
 * The string representation may use seconds or frames to display time values
 * depending on the value returned by {@link IGameEvents#isDisplayInSeconds()}.</p>
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see IGameEvents
 */
public interface IAction {
	
	/**
	 * Returns the frame when the action happened.
	 * @return the frame when the action happened
	 */
	int getFrame();
	
	/**
	 * Returns the index of the player who issued the action.
	 * @return the index of the player who issued the action
	 */
	int getPlayer();
	
	/**
	 * Returns the code of the action.
	 * @return the code of the action
	 */
	byte getOpCode();
	
	/**
	 * Returns the type (category) of the action.
	 * @return the type (category) of the action
	 */
	ActionType getActionType();
	
	/**
	 * Tells if this action is a macro action.
	 * 
	 * <p>Whether an action is macro or micro action cannot be always determined just by looking at the action,
	 * it may depend on other actions. In these cases this method throws an {@link UnsupportedOperationException}.
	 * These actions are: {@link ISelectAction}, {@link IHotkeyAction}.
	 * To determine if a select or hotkey action is a macro action, you can use an {@link IPlayerSelectionTracker}
	 * along with {@link IGameEvents#isSelectionMacro(java.util.List)}.</p>
	 * 
	 * <p>You can find more info about micro-macro actions and their classification
	 * on the <a href="https://sites.google.com/site/sc2gears/features/replay-analyzer/apm-types">APM Types</a> page.</p>
	 * 
	 * @return true if this action is a macro action; false if this action is a micro action
	 */
	boolean isMacro();
	
}
