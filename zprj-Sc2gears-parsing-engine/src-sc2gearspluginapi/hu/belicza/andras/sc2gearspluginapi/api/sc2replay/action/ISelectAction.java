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
 * Represents a select action.
 * 
 * <p>The action models the selection changes rather than the current selection.
 * To determine/track the current selection, use the {@link IPlayerSelectionTracker}
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
public interface ISelectAction extends IAction {
	
	/** Interface version. */
	String VERSION = "3.0";
	
	/**
	 * Tells if the select action is automatic.
	 * 
	 * <p>An automatic select action is for example when a selected Hatchery being morphed into a Lair finishes morphing,
	 * and the selection of the Hatchery changes to selection of the Lair.</p>
	 * 
	 * @return true if the select action is automatic; false otherwise
	 */
	boolean isAutomatic();
	
	/**
	 * Returns the number of bits that have to be interpreted as the deselection bits.
	 * @return the number of bits that have to be interpreted as the deselection bits
	 * @see #getDeselectionUnitBitmap()
	 */
	int getDeselectionBitsCount();
	
	/**
	 * Returns the deselection unit bitmap: units to remove from the control group.
	 * 
	 * <p>The deselection unit bitmap is a series of bits grouped and stored in bytes (contained in the returned byte array).
	 * Each "1" bit means that the unit being at the same position as the "1" bit is removed from the control group.</p>
	 * 
	 * @return the deselection unit bitmap
	 * @see #getDeselectionBitsCount()
	 */
	byte[] getDeselectionUnitBitmap();
	
	/**
	 * Returns the unit types of the new units being involved in the select action.
	 * <p>Contains exactly the same element as {@link #getUnitsOfTypeCounts()}.</p>
	 * @return the unit types of the new units being involved in the select action
	 * @see #getUnitsOfTypeCounts()
	 * @see IGameEvents#getUnitName(short)
	 */
	short[] getUnitTypes();
	
	/**
	 * Returns the number of each unit type being involved in the select action.
	 * 
	 * <p>Contains exactly the same element as {@link #getUnitTypes()}.</p>
	 * 
	 * @return the number of each unit type being involved in the select action
	 * @see #getUnitTypes()
	 */
	short[] getUnitsOfTypeCounts();
	
	/**
	 * Returns the new unit IDs being involved in the select action.
	 * <p>The length of this array is the sum of the elements of the {@link #getUnitsOfTypeCounts()}.</p>
	 * @return the new unit IDs being involved in the select action
	 * @see #getUnitsOfTypeCounts()
	 */
	int[] getUnitIds();
	
	/**
	 * Returns the array of indices to remove from the current selection, may be <code>null</code>.
	 * 
	 * @return the array of indices to remove from the current selection
	 * @since "3.0"
	 */
	short[] getRemoveIndices();
	
	/**
	 * Returns the array of indices to retain in the current selection, may be <code>null</code>.
	 * 
	 * @return the array of indices to retain in the current selection
	 * @since "3.0"
	 */
	short[] getRetainIndices();
	
	/**
	 * Returns the deselected units count.
	 * 
	 * @return the deselected units count
	 * @since "3.0"
	 */
	short getDeselectedUnitsCount();
	
}
