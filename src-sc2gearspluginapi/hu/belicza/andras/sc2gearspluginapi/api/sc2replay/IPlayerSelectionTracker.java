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

import hu.belicza.andras.sc2gearspluginapi.api.ReplayUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IHotkeyAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.ISelectAction;

import java.util.List;

/**
 * Interface to keep track of a player's current selection and hotkey selections.
 * 
 * <p>Example usage:<br>
 * <blockquote><pre>
 * // Track and display the last selections of the players:
 * 
 * IReplay replay = generalServices.getReplayFactoryApi().parseReplay( "C:\\someReplay.SC2Replay",
 *         EnumSet.of( ReplayFactoryApi.ReplayContent.GAME_EVENTS ) );
 * 
 * if ( replay == null )
 *     System.out.println( "Failed to parse replay!" );
 * else {
 *     IPlayer[] players = replay.getPlayers();
 *     IPlayerSelectionTracker[] selectionTrackers = new IPlayerSelectionTracker[ players.length ];
 *     
 *     for ( int i = 0; i < selectionTrackers.length; i++ )
 *         selectionTrackers[ i ] = generalServices.getReplayUtilsApi().createPlayerSelectionTracker();
 *     
 *     IGameEvents gameEvents = replay.getGameEvents();
 *     for ( final IAction action : gameEvents.getActions() ) {
 *         if ( action instanceof ISelectAction )
 *             selectionTrackers[ action.getPlayer() ].processSelectAction( (ISelectAction) action );
 *         else if ( action instanceof IHotkeyAction )
 *             selectionTrackers[ action.getPlayer() ].processHotkeyAction( (IHotkeyAction) action );
 *     }
 *     
 *     for ( int i = 0; i < selectionTrackers.length; i++ )
 *         System.out.println( players[ i ].getPlayerId().getName() + " selection: "
 *                 + gameEvents.getSelectionString( selectionTrackers[ i ].getCurrentSelection() ) );
 * }
 * </pre></blockquote></p>
 * 
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see ReplayUtilsApi#createPlayerSelectionTracker()
 * @see IGameEvents#isSelectionMacro(List)
 * @see IGameEvents#getSelectionString(List)
 */
public interface IPlayerSelectionTracker {
	
	/** Interface version. */
	String VERSION = "2.0";
	
	/**
	 * Processes and updates the current selection based on the specified select action.
	 * @param sa select action to be processed
	 */
	void processSelectAction( ISelectAction sa );
	
	/**
	 * Processes and updates the current selection or the hotkey selections based on the specified hotkey action.
	 * @param ha hotkey action to be processed
	 */
	void processHotkeyAction( IHotkeyAction ha );
	
	/**
	 * Returns the current selection of the player.
	 * 
	 * <p>The selection is the list of unit types of the current selection.
	 * Multiple units of the same type are listed multiple times.<p>
	 * 
	 * @return the current selection of the player
	 */
	List< Short > getCurrentSelection();
	
	/**
	 * Returns the hotkey selections for each number/group.
	 * 
	 * <p>The selection is the list of unit types of the current selection.
	 * Multiple units of the same type are listed multiple times.<p>
	 * 
	 * @return the hotkey selections for each number/group
	 */
	List< Short >[] getHotkeySelectionLists();
	
}
