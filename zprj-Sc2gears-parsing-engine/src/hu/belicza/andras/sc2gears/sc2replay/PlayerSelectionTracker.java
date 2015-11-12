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

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerSelectionTracker;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IHotkeyAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.ISelectAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class to keep track of a player's current selection and hotkey selections.
 * 
 * @author Andras Belicza
 */
public class PlayerSelectionTracker implements IPlayerSelectionTracker {
	
	/** List of unit types of the current selection.
	 * Multiple units of the same type are listed multiple times.       */
	public final List< Short > currentSelection       = new ArrayList< Short >();
	
	/** Unit types of current hotkey selections, 10 for the 10 hotkeys. */
	@SuppressWarnings("unchecked")
	public final List< Short >[] hotkeySelectionLists = new List[ 10 ];
	
	/**
	 * Creates a new SelectionTracker.
	 */
	public PlayerSelectionTracker() {
		for ( int i = hotkeySelectionLists.length - 1; i >= 0; i-- )
			hotkeySelectionLists[ i ] = new ArrayList< Short >();
	}
	
	/**
	 * Processes and updates the current selection based on the specified select action.
	 * @param sa select action to be processed
	 */
	public void processSelectAction( final ISelectAction sa ) {
		// Deselected units by bitmap
		final int deselectionBitsCount = sa.getDeselectionBitsCount();
		if ( deselectionBitsCount > 0 ) {
			final byte[] deselectionUnitBitmap = sa.getDeselectionUnitBitmap();
			int removedCounter = 0;
			int bitmapElement  = 0;
			for ( int bitIndex = 0; bitIndex < deselectionBitsCount; bitIndex++ ) {
				if ( ( bitIndex & 0x07 ) == 0 )
					bitmapElement = deselectionUnitBitmap[ bitIndex >> 3 ] & 0xff;
				if ( ( bitmapElement & 0x80 ) != 0 )
					if ( bitIndex - removedCounter >= currentSelection.size() )
						break;
					else	
						currentSelection.remove( bitIndex - (removedCounter++) );
				bitmapElement <<= 1;
			}
		}
		
		// Deselected units by index
		// TODO
		/*final short[] removeIndices = sa.getRemoveIndices();
		if ( removeIndices != null ) {
			for ( int i = removeIndices.length - 1; i >= 0; i-- ) // Descending order is a must: when a unit is removed, the rest is shifted!
				currentSelection.remove( removeIndices[ i ] );
		}
		else*/ if ( deselectionBitsCount < 0 )
			currentSelection.clear();
		
		// Retained units by index
		// TODO
		/*final short[] reatinIndices = sa.getRetainIndices();
		if ( reatinIndices != null ) {
			final List< Short > newCurrentSelection = new ArrayList< Short >( reatinIndices.length );
			for ( int i = 0; i < reatinIndices.length; i++ ) // Ascending order is a must: unit order is important!
				newCurrentSelection.add( currentSelection.get( reatinIndices[ i ] ) );
			currentSelection.clear();
			currentSelection.addAll( newCurrentSelection );
		}
		else*/ if ( deselectionBitsCount < 0 )
			currentSelection.clear();
		
		// Added units
		final short[] unitTypes = sa.getUnitTypes();
		if ( unitTypes != null ) {
			final short[] unitsOfTypeCounts = sa.getUnitsOfTypeCounts();
			for ( int i = 0; i < unitTypes.length; i++ ) { // Upward because the order is important if we have to deselect units later
				final short unitType = unitTypes[ i ];
				for ( int j = unitsOfTypeCounts[ i ]; j > 0; j-- )
					currentSelection.add( unitType );
			}
		}
		
		// TODO Must be selected by unit id now? Then unit ids must also be stored...
	}
	
	/**
	 * Processes and updates the current selection or the hotkey selections based on the specified hotkey action.
	 * @param ha hotkey action to be processed
	 */
	public void processHotkeyAction( final IHotkeyAction ha ) {
		final List< Short > hotkeySelectionList = hotkeySelectionLists[ ha.getNumber() ];
		
		if ( ha.isSelect() ) {
			// Hotkey selection will become current selection
			currentSelection.clear();
			currentSelection.addAll( hotkeySelectionList );
			
			final int removalBitsCount = ha.getRemovalBitsCount();
			if ( removalBitsCount > 0 ) {
				final byte[] removalUnitBitmap = ha.getRemovalUnitBitmap();
				int removedCounter = 0;
				int bitmapElement  = 0;
				for ( int bitIndex = 0; bitIndex < removalBitsCount; bitIndex++ ) {
					if ( ( bitIndex & 0x07 ) == 0 )
						bitmapElement = removalUnitBitmap[ bitIndex >> 3 ] & 0xff;
					if ( ( bitmapElement & 0x80 ) != 0 )
						if ( bitIndex - removedCounter >= currentSelection.size() )
							break;
						else	
							currentSelection.remove( bitIndex - (removedCounter++) );
					bitmapElement <<= 1;
				}
			}
			
			// TODO
			/*final short[] removeIndices = ha.getRemoveIndices();
			if ( removeIndices != null )
				for ( int i = removeIndices.length - 1; i >= 0; i-- ) // Descending order is a must: when a unit is removed, the rest is shifted!
					currentSelection.remove( removeIndices[ i ] );*/
			
			// TODO
			/*final short[] reatinIndices = ha.getRetainIndices();
			if ( reatinIndices != null ) {
				final List< Short > newSelectionList = new ArrayList< Short >( reatinIndices.length );
				for ( int i = 0; i < reatinIndices.length; i++ ) // Ascending order is a must: unit order is important!
					newSelectionList.add( currentSelection.get( reatinIndices[ i ] ) );
				currentSelection.clear();
				currentSelection.addAll( newSelectionList );
			}*/
		}
		else if ( ha.isHotkeyAssignOverwrite() ) {
			hotkeySelectionList.clear();
			hotkeySelectionList.addAll( currentSelection );
		}
		else if ( ha.isHotkeyAssignAdd() ) {
    		// TODO PROBLEM:
    		// "Hotkey Assign (add selection)" would require to handle the unit IDs too!
    		// For example: "Hotkey Select 1" followed by "Hotkey Assign 1 (add selection)" would double the selection, but should remain the same!
    		// So the best we can do is use the player's current selection for the new hotkey selection
    		// Current selection will become or will be added to hotkey selection
    		// The next line could only be uncommented, if unit IDs were handled! 
    		//if ( ha.flag == HotkeyAction.FLAG_OVERWRITE_SELECTION )
    			hotkeySelectionList.clear();
			
    		hotkeySelectionList.addAll( currentSelection );
    		// TODO Must be selected by unit id now? Then unit ids must also be stored...
		}
	}
	
	@Override
	public List< Short > getCurrentSelection() {
		return currentSelection;
	}
	
	@Override
	public List< Short >[] getHotkeySelectionLists() {
		return hotkeySelectionLists;
	}
	
	/**
	 * Tells if the specified selection is a macro selection.
	 * 
	 * <p>A selection is considered to be a macro selection if it only includes buildings.</p>
	 * 
	 * @param selection    selection to be tested
	 * @param abilityCodes reference to the ability codes of the replay (this defines what unit types are macro units)
	 * @return true if the specified selection is a macro selection; false otherwise
	 */
	public static boolean isSelectionMacro( final List< Short > selection, final AbilityCodes abilityCodes ) {
		return abilityCodes.MACRO_UNIT_TYPE_SET.containsAll( selection );
	}
	
	/**
	 * Converts the specified selection to a string.
	 * @param selection       selection to be converted
	 * @param unitTypeNameMap map to get the unit names from
	 * @return the specified selection as a string
	 */
	public static String getSelectionString( final List< Short > selection, final Map< Short, String > unitTypeNameMap ) {
		final StringBuilder selectionStringBuilder = new StringBuilder();
		
		final int selectionListSize = selection.size();
		for ( int index = 0; index < selectionListSize; ) {
			if ( selectionStringBuilder.length() > 0 )
				selectionStringBuilder.append( ", " );
			final short unitType = selection.get( index++ );
			int count;
			for ( count = 1; index < selectionListSize && selection.get( index ) == unitType; index++ )
				count++;
			final String unitName = unitTypeNameMap.get( unitType );
			if ( unitName == null )
				selectionStringBuilder.append( "Unknown[" ).append( Integer.toHexString( unitType ) ).append( ']' );
			else
				selectionStringBuilder.append( unitName );
			if ( count > 1 )
				selectionStringBuilder.append( " x" ).append( count );
		};
		
		return selectionStringBuilder.toString();
	}
	
}
