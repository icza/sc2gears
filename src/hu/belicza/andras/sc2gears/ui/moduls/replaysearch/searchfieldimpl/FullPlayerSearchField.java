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
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.PlayerId;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.SearchField;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Search field that filters by full player names and defined aliases.
 * 
 * @author Andras Belicza
 */
public class FullPlayerSearchField extends SearchField {
	
	/** Editable combo box for the text input.           */
	private final JComboBox< String > textComboBox;
	/** Text field for the text input.                   */
	private final JTextField          textField;
	
	/** Checkbox to indicate if defined aliases are also accepted. */
	private final JCheckBox  includeAliases = new JCheckBox( Language.getText( "module.repSearch.tab.filters.includeAliases" ) );
	
	/**
     * Creates a new FullPlayerSearchField.
     */
    public FullPlayerSearchField() {
	    super( Id.FULL_PLAYER );
	    
		textComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.REP_SEARCH_FULL_PLAYER_NAME, true );
		textField    = (JTextField) textComboBox.getEditor().getEditorComponent();
		textField.setToolTipText( Language.getText( "module.repSearch.tab.filters.name.playerNameToolTip" ) );
		
	    uiComponent.add( textComboBox );
	    uiComponent.add( includeAliases );
    }
    
	@Override
	public boolean validate() {
		if ( textField.getText().isEmpty() || parsePlayerIds( textField.getText() ) != null ) {
			restoreDefaultBackground( textField );
			return true;
		}
		else {
			textField.setBackground( ERROR_COLOR );
			return false;
		}
	}
	
	@Override
	public void reset() {
		super.reset();
		
		textComboBox.setSelectedItem( "" );
		restoreDefaultBackground( textField );
		includeAliases.setSelected( false );
	}
	
	@Override
	public ReplayFilter getFilter() {
		final PlayerId[] playerIds = parsePlayerIds( textField.getText() );
		
		if ( playerIds == null || playerIds.length == 0 )
			return null;
		
		final boolean acceptAliases = this.includeAliases.isSelected();
		if ( acceptAliases ) {
			for ( int i = playerIds.length - 1; i >= 0; i-- )
				playerIds[ i ] = Settings.getAliasGroupPlayerId( playerIds[ i ] );
		}
		
		return new ReplayFilter( this ) {
			@Override
            public boolean customAccept( final File file, final Replay replay ) {
				if ( playerIds.length == 1 ) {
					for ( final Player player : replay.details.players )
						if ( acceptAliases ) {
							if ( Settings.getAliasGroupPlayerId( player.playerId ).equals( playerIds[ 0 ] ) )
    							return true;
						}
    					else {
    						if ( player.playerId.equals( playerIds[ 0 ] ) )
								return true;
    					}
					return false;
				}
   				else {
    				final Set< PlayerId > playerIdSet = new HashSet< PlayerId >( playerIds.length );
    				for ( final PlayerId playerId : playerIds )
    					playerIdSet.add( playerId );
    				
    				for ( final Player player : replay.details.players )
    					playerIdSet.remove( acceptAliases ? Settings.getAliasGroupPlayerId( player.playerId ) : player.playerId );
    	            return playerIdSet.isEmpty();
				}
            }
		};
	}
	
	/**
	 * Parses player id's (full names) from the given text.
	 * Text is interpreted as a comma separated list. Tokens will be trimmed.
	 * @param text text to parse player ids from
	 * @return an array of player ids; or null if the format of text is invalid
	 */
	private static PlayerId[] parsePlayerIds( final String text ) {
		if ( text.length() == 0 )
			return new PlayerId[ 0 ];
		
		final String[]   fullNames = text.split( "," );
		final PlayerId[] playerIds = new PlayerId[ fullNames.length ];
		if ( fullNames.length > 0 )
			for ( int i = 0; i < fullNames.length; i++ ) {
				fullNames[ i ] = fullNames[ i ].trim();
				if ( ( playerIds[ i ] = PlayerId.parse( fullNames[ i ] ) ) == null )
					return null;
			}
		
		return playerIds;
	}
	
	@Override
	public void saveValue( final Document document, final Element filterElement ) {
		super.saveValue( document, filterElement );
		
		appendDataElement( document, filterElement, "text"          , textField     .getText() );
		appendDataElement( document, filterElement, "includeAliases", includeAliases.isSelected() );
	}
	
	@Override
	public void loadValue( final Element filterElement ) {
		super.loadValue( filterElement );
		
		textComboBox  .setSelectedItem( getDataElementValue( filterElement, "text" ) );
		includeAliases.setSelected( Boolean.parseBoolean( getDataElementValue( filterElement, "includeAliases" ) ) );
	}
	
}
