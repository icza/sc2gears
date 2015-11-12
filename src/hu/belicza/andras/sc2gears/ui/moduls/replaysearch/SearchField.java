/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.replaysearch;

import hu.belicza.andras.sc2gears.language.Language;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents a search field.
 * 
 * @author Andras Belicza
 */
public abstract class SearchField implements ActionListener {
	
	/**
	 * Filter id used to identify search fields when saving/loading.
	 * @author Andras Belicza
	 */
	protected static enum Id {
		/** Player.           */
		PLAYER          ( "module.repSearch.tab.filters.name.playerName"      ),
		/** Full player.      */
		FULL_PLAYER     ( "module.repSearch.tab.filters.name.fullPlayerName"  ),
		/** Map.              */
		MAP             ( "module.repSearch.tab.filters.name.mapName"         ),
		/** Race match-up.    */
		RACE_MATCHUP    ( "module.repSearch.tab.filters.name.raceMatchup"     ),
		/** League match-up.  */
		LEAGUE_MATCHUP  ( "module.repSearch.tab.filters.name.leagueMatchup"   ),
		/** File.             */
		FILE            ( "module.repSearch.tab.filters.name.fileName"        ),
		/** Chat message.     */
		CHAT_MESSAGE    ( "module.repSearch.tab.filters.name.chatMessage"     ),
		/** Expansion level.  */
		EXPANSION       ( "module.repSearch.tab.filters.name.expansion"       ),
		/** Format.           */
		FORMAT          ( "module.repSearch.tab.filters.name.format"          ),
		/** Game type.        */
		GAME_TYPE       ( "module.repSearch.tab.filters.name.gameType"        ),
		/** Gateway.          */
		GATEWAY         ( "module.repSearch.tab.filters.name.gateway"         ),
		/** Ladder Season.    */
		LADDER_SEASON   ( "module.repSearch.tab.filters.name.ladderSeason"    ),
		/** Date.             */
		DATE            ( "module.repSearch.tab.filters.name.date"            ),
		/** Game length.      */
		GAME_LENGTH     ( "module.repSearch.tab.filters.name.gameLength"      ),
		/** Version.          */
		VERSION         ( "module.repSearch.tab.filters.name.version"         ),
		/** Build order.      */
		BUILD_ORDER     ( "module.repSearch.tab.filters.name.buildOrder"      ),
		/** Building.         */
		BUILDING        ( "module.repSearch.tab.filters.name.building"        ),
		/** Unit.             */
		UNIT            ( "module.repSearch.tab.filters.name.unit"            ),
		/** Research.         */
		RESEARCH        ( "module.repSearch.tab.filters.name.research"        ),
		/** Upgrade.          */
		UPGRADE         ( "module.repSearch.tab.filters.name.upgrade"         ),
		/** Unit ability.     */
		UNIT_ABILITY    ( "module.repSearch.tab.filters.name.unitAbility"     ),
		/** Building ability. */
		BUILDING_ABILITY( "module.repSearch.tab.filters.name.buildingAbility" );
		
		/** Cached value of the display text of the search field. */
		public final String displayText;
		
		/**
		 * Creates a new Id.
		 * @param displayKey key of the search field
		 */
		private Id( final String displayTextKey ) {
			displayText = Language.getText( displayTextKey );
		}
	}
	
	/** Error color to be used if validation fails. */
	protected static final Color ERROR_COLOR = new Color( 255, 150, 150 );
	
	/** Id of the search field. */
	protected final Id id;
	
	/** The UI component of the search field, this has to be added to a container. */
	public final Box uiComponent = Box.createHorizontalBox();
	
	/** Check box to invert the filter.              */
	protected final JCheckBox invertCheckBox = new JCheckBox( Language.getText( "module.repSearch.tab.filters.invert" ) );
	/** The label displayed before the search field. */
	public    final JLabel    displayLabel;
	
	/**
	 * Creates a new SearchField.
	 * @param id id of the search field
	 */
	public SearchField( final Id id ) {
		this.id      = id;
		displayLabel = new JLabel( id.displayText );
		
		invertCheckBox.setToolTipText( Language.getText( "module.repSearch.tab.filters.invertToolTip" ) );
		invertCheckBox.setHorizontalTextPosition( SwingConstants.LEFT );
		invertCheckBox.addActionListener( this );
		invertCheckBox.setOpaque( true );
		uiComponent.add( invertCheckBox );
		
		uiComponent.add( Box.createHorizontalStrut( 5 ) );
		uiComponent.add( displayLabel );
	}
	
	/**
	 * Validates the value entered into the search field.<br>
	 * If the value is invalid, it should be marked with red background for example.
	 * @return true if the entered value is valid; false otherwise
	 */
	public abstract boolean validate();
	
	/**
	 * Returns a replay filter defined by this search field.
	 * @return a replay filter defined by this search field or <code>null</code> if the search fields does not define a filter (contains initial value)
	 */
	public abstract ReplayFilter getFilter();
	
	/**
	 * Resets this search field's value to its default value.
	 */
	public void reset() {
		invertCheckBox.setBackground( null );
		invertCheckBox.setSelected( false );
	}
	
	@Override
	public void actionPerformed( final ActionEvent event ) {
		invertCheckBox.setBackground( invertCheckBox.isSelected() ? Color.GREEN : null );
	}
	
	/**
	 * Restores the default background color to the specified text field.
	 * @param textField text field whose default background to be restored
	 */
	public static void restoreDefaultBackground( final JTextField textField ) {
		textField.setBackground( UIManager.getColor( "TextField.background" ) );
	}
	
	/**
	 * Saves the value of the search field into the specified filter element. 
	 * @param document      reference to the document
	 * @param filterElement filter element to save value in
	 */
	public void saveValue( final Document document, final Element filterElement ) {
		appendDataElement( document, filterElement, "not", invertCheckBox.isSelected() );
	}
	
	/**
	 * Appends a data element holding a value.
	 * @param document      reference to the document
	 * @param filterElement filter element to append to
	 * @param tagName       name of the data element to append
	 * @param value         value of the data element to append
	 */
	protected static void appendDataElement( final Document document, final Element filterElement, final String tagName, final Object value ) {
		final Element element = document.createElement( tagName );
		element.setTextContent( value.toString() );
		filterElement.appendChild( element );
	}
	
	/**
	 * Loads the saved value from the specified filter element.
	 * @param filterElement filter element to load the value from
	 */
	public void loadValue( final Element filterElement ) {
		invertCheckBox.setSelected( Boolean.parseBoolean( getDataElementValue( filterElement, "not" ) ) );
		// The invert check box changes color if selected:
		actionPerformed( null );
	}
	
	/**
	 * Returns the value of the specified data element.
	 * @param parentElement parent element to search in
	 * @param tagName       name of the data element whose value to return
	 * @return the value of the specified data element; or <code>null</code> if the data element is not found
	 */
	protected static String getDataElementValue( final Element parentElement, final String tagName ) {
		final NodeList dataElementList = parentElement.getElementsByTagName( tagName );
		return dataElementList.getLength() == 0 ? null : dataElementList.item( 0 ).getTextContent();
	}
	
}
