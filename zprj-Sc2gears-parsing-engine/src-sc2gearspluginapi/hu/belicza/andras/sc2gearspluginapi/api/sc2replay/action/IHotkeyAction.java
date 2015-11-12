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

/**
 * Represents a hotkey action.
 * 
 * <p>The action models the hotkey selection changes rather than the current hotkey selections.
 * To determine/track the current hotkey selections, use the {@link IPlayerSelectionTracker}
 * returned by {@link ReplayUtilsApi#createPlayerSelectionTracker()}.</p>
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see IGameEvents
 * @see IPlayerSelectionTracker
 */
public interface IHotkeyAction extends IAction {
	
	/** Interface version. */
	String VERSION = "3.0";
	
	/**
	 * Returns the hotkey number (the hotkey group).
	 * @return the hotkey number (the hotkey group)
	 */
	int getNumber();
	
	/**
	 * Tells if the action is a hotkey select.
	 * @return true if the action is a hotkey select; false if it's a hotkey assign
	 */
	boolean isSelect();
	
	/**
	 * Tells if the action is an ADD operation in case of hotkey assigns.
	 * <p>An ADD operation adds the current selection to the hotkey group.</p>
	 * @return true if the action is an ADD operation in case of hotkey assigns; false otherwise
	 */
	boolean isHotkeyAssignAdd();
	
	/**
	 * Tells if the action is an OVERWRITE operation in case of hotkey assigns.
	 * <p>An OVERWRITE operation overwrites the hotkey group assigning the current selection to the hotkey group.</p>
	 * @return true if the action is an OVERWRITE operation in case of hotkey assigns; false otherwise
	 */
	boolean isHotkeyAssignOverwrite();
	
	/**
	 * Returns the number of bits that have to be interpreted as the removal bits.
	 * 
	 * @return the number of bits that have to be interpreted as the removal bits
	 * @since "3.0"
	 * @see #getRemovalUnitBitmap()
	 */
	int getRemovalBitsCount();
	
	/**
	 * Returns the removal unit bitmap: unit.
	 * 
	 * <p>The removal unit bitmap is a series of bits grouped and stored in bytes (contained in the returned byte array).
	 * Each "1" bit means that the unit being at the same position as the "1" bit is removed.</p>
	 * 
	 * @return the removal unit bitmap
	 * @since "3.0"
	 * @see #getRemovalBitsCount()
	 */
	byte[] getRemovalUnitBitmap();
	
	/**
	 * Returns the array of indices to remove from the control group, may be <code>null</code>.
	 * 
	 * @return the array of indices to remove from the control group
	 * @since "3.0"
	 */
	short[] getRemoveIndices();
	
	/**
	 * Returns the array of indices to retain in the control group, may be <code>null</code>.
	 * 
	 * @return the array of indices to retain in the control group
	 * @since "3.0"
	 */
	short[] getRetainIndices();
	
}
