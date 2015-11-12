/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.EnumCache;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.BuildAction;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.components.BaseLabelListCellRenderer;
import hu.belicza.andras.sc2gears.ui.icons.IconHandler;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.SearchField;
import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Search field that filters by an initial build order.
 * 
 * @author Andras Belicza
 */
public class BuildOrderSearchField extends SearchField {
	
	/** Name of the any building pseudo-building. */
	private static final String ANY_BUILDING_NAME = "*";
	
	/** Vector of building to be added to the build order text field. */
	private static final Vector< Object > BUILDING_VECTOR;
	static {
		BUILDING_VECTOR = new Vector< Object >( ReplayUtils.CURRENT_ABILITY_CODES.BUILD_ABILITY_CODES.values() );
		
		Collections.sort( BUILDING_VECTOR, new Comparator< Object >() {
			@Override
			public int compare( final Object b1, final Object b2 ) {
				return b1.toString().compareTo( b2.toString() );
			}
		} );
		
		BUILDING_VECTOR.insertElementAt( ANY_BUILDING_NAME + " (" + Language.getText( "module.repSearch.tab.filters.name.buildOrderAnyBuilding" )+ ")", 0 );
	}
	
	/** Editable combo box for the text input.                           */
	protected final JComboBox< String >  textComboBox;
	/** Text field for the build order text input.                       */
	protected final JTextField           textField;
	/** Combo box for adding new building to the build order text input. */
	protected final JComboBox< Object >  addComboBox = new JComboBox<>( BUILDING_VECTOR );
	
	/**
	 * Creates a new BuildOrderSearchField.
	 */
	public BuildOrderSearchField() {
		super( Id.BUILD_ORDER );
		
		textComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.REP_SEARCH_BUILD_ORDER, true );
		textField    = (JTextField) textComboBox.getEditor().getEditorComponent();
		
		textField.setToolTipText( Language.getText( "module.repSearch.tab.filters.name.buildOrderTextToolTip" ) );
		uiComponent.add( GuiUtils.wrapInBorderPanel( textComboBox ) );
		final JButton addButton = new JButton( Icons.ARROW_180 );
		addButton.setToolTipText( Language.getText( "module.repSearch.tab.filters.name.buildOrderAddToolTip" ) );
		addButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final String currentText = textField.getText();
				textComboBox.setSelectedItem( currentText
						+ ( currentText.length() == 0 ? "" : ( currentText.trim().endsWith( "," ) ? "" : ", " ) )
						+ ( addComboBox.getSelectedIndex() == 0 ? ANY_BUILDING_NAME : addComboBox.getSelectedItem() ) );
			}
		} );
		uiComponent.add( addButton );
		addComboBox.setRenderer( new BaseLabelListCellRenderer< Object >() {
			@Override
			public Icon getIcon( final Object value ) {
				return value instanceof Building ? Icons.getBuildingIcon( (Building) value, IconSize.MEDIUM ) : IconHandler.NULL.get( IconSize.MEDIUM );
			}
		} );
		uiComponent.add( addComboBox );
	}
	
	@Override
	public boolean validate() {
		if ( parseBuildOrder( textField.getText() ) == null ) {
			textField.setBackground( ERROR_COLOR );
			return false;
		}
		else {
			restoreDefaultBackground( textField );
			return true;
		}
	}
	
	@Override
	public void reset() {
		super.reset();
		
		addComboBox.setSelectedIndex( 0 );
		textComboBox.setSelectedItem( "" );
		restoreDefaultBackground( textField );
	}
	
	@Override
	public ReplayFilter getFilter() {
		final Building[] buildings = parseBuildOrder( textField.getText() );
		
		if ( buildings == null || buildings.length == 0 )
			return null;
		
		return new ReplayFilter( this ) {
			@Override
			public Set< ReplayContent > getRequiredReplayContentSet() {
				return GAME_EVENTS_REPLAY_CONTENT_SET;
			}
			@Override
			public boolean customAccept( final File file, final Replay replay ) {
				final Action[] actions = replay.gameEvents.actions;
				final int actionsLength = actions.length;
				Action action;
				
				// Tells how many buildings matches with the build order from the player
				final int[] matchedBuildingCounts = new int[ replay.details.players.length ];
				// Tells how many players are still in race to match the build order
				int stillInRaceCount = matchedBuildingCounts.length; 
				Building boBuilding;
				for ( int i = 0; i < actionsLength; i++ )
					if ( ( action = actions[ i ] ).type == ActionType.BUILD ) {
						if ( matchedBuildingCounts[ action.player ] >= 0 ) { // If player is still in race...
							final Building building = ( (BuildAction) action ).building;
							if ( ( boBuilding = buildings[ matchedBuildingCounts[ action.player ] ] ) == null || boBuilding == building ) {
								if ( ++matchedBuildingCounts[ action.player ] == buildings.length ) {
									// Found a match!
									return true;
								}
							}
							else {
								// Player is out of race
								matchedBuildingCounts[ action.player ] = -1;
								if ( --stillInRaceCount == 0 )
									return false; // All player dropped out of race...
							}
						}
					}
				
				return false;
			}
		};
	}
	
	/**
	 * Parses a build order from a string.<br>
	 * A build order is a comma separated list of building names (case in-sensitive).<br>
	 * All building asterisk is indicated with a null element inside the returned array.
	 * @param text text to parse build order from
	 * @return an array of parsed building names; an empty array if text is empty; or <code>null</code> if text is invalid
	 */
	private static Building[] parseBuildOrder( final String text ) {
		if ( text.length() == 0 )
			return new Building[ 0 ];
		
		final String[] buildingNames = text.split( "," );
		
		final List< Building > buildingList = new ArrayList< Building >();
		for ( String buildingName : buildingNames ) {
			buildingName = buildingName.trim();
			
			if ( buildingName.length() == 0 )
				return null;
			
			if ( buildingName.equals( ANY_BUILDING_NAME ) )
				buildingList.add( null );
			else {
				boolean found = false;
				for ( final Building building : EnumCache.BUILDINGS )
					if ( building.toString().equalsIgnoreCase( buildingName ) ) {
						buildingList.add( building );
						found = true;
						break;
					}
				if ( !found )
					return null;
			}
		}
		
		return buildingList.toArray( new Building[ buildingList.size() ] );
	}
	
	@Override
	public void saveValue( final Document document, final Element filterElement ) {
		super.saveValue( document, filterElement );
		
		appendDataElement( document, filterElement, "text", textField.getText() );
	}
	
	@Override
	public void loadValue( final Element filterElement ) {
		super.loadValue( filterElement );
		textComboBox.setSelectedItem( getDataElementValue( filterElement, "text" ) );
	}
	
}
