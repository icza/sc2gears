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

import java.awt.Color;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.SearchField;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A text search field which has a text input and a checkbox to specify exact or partial match.
 * 
 * @author Andras Belicza
 */
public abstract class TextSearchField extends SearchField {
	
	/** Editable combo box for the text input.           */
	protected final JComboBox< String > textComboBox;
	/** Text field for the text input.                   */
	protected final JTextField          textField;
	/** Checkbox to indicate if exact match is required. */
	protected final JCheckBox           exactMatch = new JCheckBox( Language.getText( "module.repSearch.tab.filters.exactMatch" ) );
	
	/**
	 * Creates a new TextSearchField.
	 * @param id id of the search field
	 */
	public TextSearchField( final Id id, final PredefinedList predefinedList ) {
		super( id );
		
		textComboBox = GuiUtils.createPredefinedListComboBox( predefinedList, true );
		textField    = (JTextField) textComboBox.getEditor().getEditorComponent();
		
		uiComponent.add( textComboBox );
		uiComponent.add( exactMatch );
	}
	
	@Override
	public boolean validate() {
		if ( customValidate() ) {
			restoreDefaultBackground( textField );
			return true;
		}
		else {
			textField.setBackground( new Color( 255, 150, 150 ) );
			return false;
		}
	}
	
	/**
	 * This is the implementation of the validation logic.<br>
	 * The default implementation always returns true.
	 * @return true if the entered value is valid; false otherwise
	 */
	public boolean customValidate() {
		return true;
	}
	
	@Override
	public void reset() {
		super.reset();
		
		textComboBox.setSelectedItem( "" );
		restoreDefaultBackground( textField );
		exactMatch.setSelected( false );
	}
	
	@Override
	public void saveValue( final Document document, final Element filterElement ) {
		super.saveValue( document, filterElement );
		
		appendDataElement( document, filterElement, "text"      , textField .getText()    );
		appendDataElement( document, filterElement, "exactMatch", exactMatch.isSelected() );
	}
	
	@Override
	public void loadValue( final Element filterElement ) {
		super.loadValue( filterElement );
		
		textComboBox.setSelectedItem( getDataElementValue( filterElement, "text" ) );
		exactMatch  .setSelected( Boolean.parseBoolean( getDataElementValue( filterElement, "exactMatch" ) ) );
	}
	
}
