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

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IAction;

import java.util.List;

/**
 * Game events interface of StarCraft II replays.
 * 
 * <p>Game events contains the actions of the players.</p>
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see IReplay
 */
public interface IGameEvents {
	
	/** Interface version. */
	String VERSION = "2.7";
	
	/**
	 * Tells if the string representation of actions uses seconds to display time values.
	 * 
	 * <p>If seconds are used, game time - real time conversion will be controlled
	 * by {@link IReplay#getConverterGameSpeed()}.<p>
	 * 
	 * @return true if the string representation of actions uses seconds to display time values; false if frame values are used
	 * 
	 * @see IReplay#getConverterGameSpeed()
	 * @see InfoApi#isRealTimeConversionEnabled()
	 */
	boolean isDisplayInSeconds();
	
	/**
	 * Sets if seconds is to be used for time values by string representation of actions. 
	 * @param displayInSeconds true if seconds is to be used for time values by string representation of actions; false if frame values is to be used
	 * @see #isDisplayInSeconds()
	 */
	void setDisplayInSeconds( boolean displayInSeconds );
	
	/**
	 * Tells if there were errors parsing the game events.<br>
	 * 
	 * If replay was constructed from a replay specification, this tells if there were lines that could not be parsed.<br>
	 * If replay was taken from the replay cache, this is always <code>false</code>.
	 * 
	 * @return true if there were errors parsing the game events; false otherwise
	 * @since "2.7"
	 */
	boolean isErrorParsing();
	
	/**
	 * Returns the actions of the replay.
	 * @return the actions of the replay
	 */
	IAction[] getActions();
	
	/**
	 * Returns the name of a unit specified by its type.
	 * @param unitType type of unit whose name to be returned
	 * @return the name of a unit specified by its type
	 */
	String getUnitName( short unitType );
	
	/**
	 * Converts the specified selection to a string.
	 * @param selection       selection to be converted
	 * @return the specified selection as a string
	 * 
	 * @see IPlayerSelectionTracker
	 * @see IPlayerSelectionTracker#getCurrentSelection()
	 * @see IPlayerSelectionTracker#getHotkeySelectionLists()
	 */
	String getSelectionString( List< Short > selection );
	
	/**
	 * Tells if the specified selection is a macro selection.
	 * 
	 * <p>A selection is considered to be a macro selection if it only includes buildings.</p>
	 * 
	 * @param selection    selection to be tested
	 * @return true if the specified selection is a macro selection; false otherwise
	 * 
	 * @see IPlayerSelectionTracker
	 * @see IPlayerSelectionTracker#getCurrentSelection()
	 * @see IPlayerSelectionTracker#getHotkeySelectionLists()
	 */
	boolean isSelectionMacro( List< Short > selection );
	
}
