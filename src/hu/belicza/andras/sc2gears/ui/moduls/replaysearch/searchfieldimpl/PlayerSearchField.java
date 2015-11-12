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
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.components.BaseLabelListCellRenderer;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplayFilter;
import hu.belicza.andras.sc2gearspluginapi.api.enums.League;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.MatchResult;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Search field that filters by various attributes of players.<br>
 * Subfields include: player name, race, win, APM.
 * 
 * @author Andras Belicza
 */
public class PlayerSearchField extends TextSearchField {
	
	/** Race combo box.         */
	private final JComboBox< Object > raceComboBox        = new JComboBox<>( new Object[] { " ", Race.PROTOSS, Race.TERRAN, Race.ZERG, Race.RANDOM } );
	/** League combo box.       */
	private final JComboBox< Object > leagueComboBox      = new JComboBox<>( new Object[] { " ", League.GRANDMASTER, League.MASTER, League.DIAMOND, League.PLATINUM, League.GOLD, League.SILVER, League.BRONZE, League.UNRANKED, League.UNKNOWN } );
	/** Match result combo box. */
	private final JComboBox< Object > matchResultComboBox = new JComboBox<>( new Object[] { "", MatchResult.WIN, MatchResult.LOSS, MatchResult.UNKNOWN } );
	
	/** Min valid APM. */
	private static final int MIN_VALID_APM =    0;
	/** Max valid APM. */
	private static final int MAX_VALID_APM = 9999;
	
	/** Min APM spinner. */
	private final JSpinner minApmSpinner = new JSpinner( new SpinnerNumberModel( MIN_VALID_APM, MIN_VALID_APM, MAX_VALID_APM, 1 ) );
	/** Min APM spinner. */
	private final JSpinner maxApmSpinner = new JSpinner( new SpinnerNumberModel( MAX_VALID_APM, MIN_VALID_APM, MAX_VALID_APM, 1 ) );
	
	/**
	 * Creates a new PlayerNameSearchField.
	 */
	public PlayerSearchField() {
		super( Id.PLAYER, PredefinedList.REP_SEARCH_PLAYER_NAME );
		textField.setToolTipText( Language.getText( "module.repSearch.tab.filters.name.playerNameToolTip" ) );
		
		// The player combo box should use all the extra space:
		uiComponent.remove( textComboBox );
		uiComponent.add( GuiUtils.wrapInBorderPanel( textComboBox ), uiComponent.getComponentCount() - 1 );
		
		uiComponent.add( new JLabel( Language.getText( "module.repSearch.tab.filters.name.race" ) ), uiComponent.getComponentCount() - 1 );
		raceComboBox.setRenderer( new BaseLabelListCellRenderer< Object >() {
			@Override
			public Icon getIcon( final Object value ) {
				if ( value instanceof Race )
					return Icons.getRaceIcon( (Race) value );
				return null;
			}
		} );
		uiComponent.add( raceComboBox, uiComponent.getComponentCount() - 1 );
		
		uiComponent.add( new JLabel( Language.getText( "module.repSearch.tab.filters.name.league" ) ), uiComponent.getComponentCount() - 1 );
		leagueComboBox.setRenderer( new BaseLabelListCellRenderer< Object >() {
			@Override
			public Component getListCellRendererComponent( final JList< ? extends Object > list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus ) {
				super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
				if ( value instanceof League )
					label.setText( Character.toString( ( (League) value ).letter ) );
				return label;
			}
			@Override
			public Icon getIcon( final Object value ) {
				if ( value instanceof League )
					return ( (League) value ).getIconForRank( 200 );
				return null;
			}
		} );
		uiComponent.add( leagueComboBox, uiComponent.getComponentCount() - 1 );
		
		uiComponent.add( new JLabel( Language.getText( "module.repSearch.tab.filters.name.result" ) ), uiComponent.getComponentCount() - 1 );
		uiComponent.add( matchResultComboBox, uiComponent.getComponentCount() - 1 );
		
		uiComponent.add( new JLabel( Language.getText( "module.repSearch.tab.filters.name.apm" ) ), uiComponent.getComponentCount() - 1 );
		minApmSpinner.setEditor( new JSpinner.NumberEditor( minApmSpinner ) );
		minApmSpinner.setMaximumSize( new Dimension( 40, minApmSpinner.getPreferredSize().height ) );
		uiComponent.add( minApmSpinner, uiComponent.getComponentCount() - 1 );
		uiComponent.add( new JLabel( "-" ), uiComponent.getComponentCount() - 1 );
		maxApmSpinner.setEditor( new JSpinner.NumberEditor( maxApmSpinner ) );
		maxApmSpinner.setMaximumSize( new Dimension( 40, maxApmSpinner.getPreferredSize().height ) );
		uiComponent.add( maxApmSpinner, uiComponent.getComponentCount() - 1 );
	}
	
	@Override
	public boolean validate() {
		final boolean superValidateResult = super.validate();
		
		// Validate the APM fields: the min APM cannot be greater than the max APM
		// Note: some LAF honors the background of the editor, some only the background of the text field of the editor
		if ( ( (Integer) minApmSpinner.getValue() ).intValue() <= ( (Integer) maxApmSpinner.getValue() ).intValue() ) {
			minApmSpinner.getEditor().setBackground( null );
			maxApmSpinner.getEditor().setBackground( null );
			restoreDefaultBackground( ( (JSpinner.NumberEditor) minApmSpinner.getEditor() ).getTextField() );
			restoreDefaultBackground( ( (JSpinner.NumberEditor) maxApmSpinner.getEditor() ).getTextField() );
			return superValidateResult;
		}
		else {
			minApmSpinner.getEditor().setBackground( ERROR_COLOR );
			maxApmSpinner.getEditor().setBackground( ERROR_COLOR );
			( (JSpinner.NumberEditor) minApmSpinner.getEditor() ).getTextField().setBackground( ERROR_COLOR );
			( (JSpinner.NumberEditor) maxApmSpinner.getEditor() ).getTextField().setBackground( ERROR_COLOR );
			return false;
		}
	}
	
	@Override
	public boolean customValidate() {
		return textField.getText().length() == 0 || parseNames( textField.getText() ) != null;
	}
	
	@Override
	public ReplayFilter getFilter() {
		final Object      selectedRace        = raceComboBox.getSelectedItem();
		final Race        race                = selectedRace instanceof Race ? (Race) selectedRace : null;
		final Object      selectedLeague      = leagueComboBox.getSelectedItem();
		final League      league              = selectedLeague instanceof League ? (League) selectedLeague: null;
		final Object      selectedMatchResult = matchResultComboBox.getSelectedItem();
		final MatchResult matchResult         = selectedMatchResult instanceof MatchResult ? (MatchResult) selectedMatchResult : null;
		final int         minApm              = (Integer) minApmSpinner.getValue();
		final int         maxApm              = (Integer) maxApmSpinner.getValue();
		
		return textField.getText().length() == 0 && race == null && league == null && matchResult == null && minApm <= MIN_VALID_APM && maxApm >= MAX_VALID_APM ? null : new TextReplayFilter( this ) {
			private final String[] names  = parseNames( text );
			@Override
			public boolean customAccept( final File file, final Replay replay ) {
				if ( names.length < 2 ) {
					for ( final Player player : replay.details.players ) {
						if ( !isPlayerCandidate( replay, player ) )
							continue;
						if ( names.length == 0 )
							return true;
						final String playerName = player.playerId.name.toLowerCase();
						if ( exactMatch ? playerName.equals( names[ 0 ] ) : playerName.contains( names[ 0 ] ) ) // Use names[ 0 ] instead of text, cause it's trimmed, and optional leading and trailing commas are removed
							return true;
					}
					return false;
				}
				else {
					// Cannot use  Arrays.asList() here, because the list is modified (items are removed)
					final List< String > nameList = new ArrayList< String >( names.length );
					for ( final String name : names )
						nameList.add( name );
					for ( final Player player : replay.details.players ) {
						if ( !isPlayerCandidate( replay, player ) )
							continue;
						if ( names.length > 0 ) {
							final String playerName = player.playerId.name.toLowerCase();
							for ( int i = nameList.size() - 1; i >= 0; i-- )
								if ( exactMatch ? playerName.equals( nameList.get( i ) ) : playerName.contains( nameList.get( i ) ) ) {
									nameList.remove( i );
									break;
								}
						}
					}
					return nameList.isEmpty();
				}
			}
			/**
			 * Tells if the player is a candidate.<br>
			 * A player is a candidate if the race, league, match result and APM conditions are met.
			 * @param replay reference to the replay
			 * @param player the player to be tested
			 * @return true if the player is a candidate
			 */
			private boolean isPlayerCandidate( final Replay replay, final Player player ) {
				if ( race != null )
					if ( race == Race.RANDOM && player.race != Race.RANDOM || race != Race.RANDOM && race != player.finalRace )
						return false;
				if ( league != null )
					if ( league != player.getLeague() )
						return false;
				if ( matchResult != null ) {
					if ( matchResult == MatchResult.UNKNOWN ) {
						if ( player.isWinner != null )
							return false;
					}
					else {
						if ( player.isWinner == null || ( matchResult == MatchResult.WIN ^ player.isWinner ) ) // XOR :F; matchResult can only be WIN or LOSS here
							return false;
					}
				}
				if ( minApm > MIN_VALID_APM || maxApm < MAX_VALID_APM ) {
					final int apm = ReplayUtils.calculatePlayerApm( replay, player );
					if ( minApm > MIN_VALID_APM && apm < minApm || maxApm < MAX_VALID_APM && apm > maxApm )
						return false;
				}
				return true;
			}
		};
	}
	
	/**
	 * Parses names from the given text.
	 * Text is interpreted as a comma separated list. Tokens will be trimmed.
	 * @param text text to parse names from
	 * @return an array of names; or null if the format of text is invalid
	 */
	private static String[] parseNames( final String text ) {
		if ( text.length() == 0 )
			return new String[ 0 ];
		
		final String[] names = text.split( "," );
		if ( names.length > 0 )
			for ( int i = 0; i < names.length; i++ ) {
				names[ i ] = names[ i ].trim();
				if ( names[ i ].length() == 0 )
					return null;
			}
		return names;
	}
	
	@Override
	public void reset() {
		super.reset();
		
		raceComboBox       .setSelectedIndex( 0 );
		leagueComboBox     .setSelectedIndex( 0 );
		matchResultComboBox.setSelectedIndex( 0 );
		
		minApmSpinner.getEditor().setBackground( null );
		restoreDefaultBackground( ( (JSpinner.NumberEditor) minApmSpinner.getEditor() ).getTextField() );
		minApmSpinner.setValue( MIN_VALID_APM );
		
		maxApmSpinner.getEditor().setBackground( null );
		restoreDefaultBackground( ( (JSpinner.NumberEditor) maxApmSpinner.getEditor() ).getTextField() );
		maxApmSpinner.setValue( MAX_VALID_APM );
	}
	
	@Override
	public void saveValue( final Document document, final Element filterElement ) {
		super.saveValue( document, filterElement );
		
		appendDataElement( document, filterElement, "raceCombo"       , raceComboBox       .getSelectedIndex() );
		appendDataElement( document, filterElement, "leagueCombo"     , leagueComboBox     .getSelectedIndex() );
		appendDataElement( document, filterElement, "matchResultCombo", matchResultComboBox.getSelectedIndex() );
		appendDataElement( document, filterElement, "minApm"          , minApmSpinner      .getValue()         );
		appendDataElement( document, filterElement, "maxApm"          , maxApmSpinner      .getValue()         );
	}
	
	@Override
	public void loadValue( final Element filterElement ) {
		super.loadValue( filterElement );
		
		raceComboBox       .setSelectedIndex( Integer.parseInt( getDataElementValue( filterElement, "raceCombo"        ) ) );
		leagueComboBox     .setSelectedIndex( Integer.parseInt( getDataElementValue( filterElement, "leagueCombo"      ) ) );
		matchResultComboBox.setSelectedIndex( Integer.parseInt( getDataElementValue( filterElement, "matchResultCombo" ) ) );
		minApmSpinner      .setValue        ( Integer.valueOf ( getDataElementValue( filterElement, "minApm"           ) ) );
		maxApmSpinner      .setValue        ( Integer.valueOf ( getDataElementValue( filterElement, "maxApm"           ) ) );
	}
	
}
