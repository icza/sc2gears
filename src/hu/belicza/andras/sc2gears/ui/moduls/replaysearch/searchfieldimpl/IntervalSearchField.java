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
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.SearchField;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An interval search field which has 2 text inputs: a minimum and a maximum value, both are optional.
 * 
 * @author Andras Belicza
 */
public abstract class IntervalSearchField extends SearchField {
	
	/** Text field for the minimum input. */
	protected final JTextField minTextField  = new JTextField( 1 );
	/** Text field for the maximum input. */
	protected final JTextField maxTextField  = new JTextField( 1 );
	
	/**
	 * Creates a new IntervalSearchField.
	 * @param id id of the search field
	 */
	public IntervalSearchField( final Id id ) {
		super( id );
		
		uiComponent.add( Box.createHorizontalStrut( 5 ) );
		uiComponent.add( new JLabel( Language.getText( "module.repSearch.tab.filters.min" ) ) );
		uiComponent.add( minTextField );
		uiComponent.add( new JLabel( Language.getText( "module.repSearch.tab.filters.max" ) ) );
		uiComponent.add( maxTextField );
	}
	
	@Override
	public boolean validate() {
		final boolean validMin = customValidateMin();
		
		if ( validMin )
			restoreDefaultBackground( minTextField );
		else
			minTextField.setBackground( ERROR_COLOR );
		
		final boolean validMax = customValidateMax();
		if ( validMax )
			restoreDefaultBackground( maxTextField );
		else
			maxTextField.setBackground( ERROR_COLOR );
		
		return validMin && validMax;
	}
	
	/**
	 * This is the implementation of the validation logic for the min value.<br>
	 * The default implementation always returns true;
	 * @return true if the entered value is valid; false otherwise
	 */
	public boolean customValidateMin() {
		return true;
	}
	
	/**
	 * This is the implementation of the validation logic for the max value.<br>
	 * The default implementation always returns true;
	 * @return true if the entered value is valid; false otherwise
	 */
	public boolean customValidateMax() {
		return true;
	}
	
	@Override
	public void reset() {
		super.reset();
		
		minTextField.setText( "" );
		restoreDefaultBackground( minTextField );
		maxTextField.setText( "" );
		restoreDefaultBackground( maxTextField );
	}
	
	@Override
	public void saveValue( final Document document, final Element filterElement ) {
		super.saveValue( document, filterElement );
		
		appendDataElement( document, filterElement, "min", minTextField.getText() );
		appendDataElement( document, filterElement, "max", maxTextField.getText() );
	}
	
	@Override
	public void loadValue( final Element filterElement ) {
		super.loadValue( filterElement );
		
		minTextField.setText( getDataElementValue( filterElement, "min" ) );
		maxTextField.setText( getDataElementValue( filterElement, "max" ) );
	}
	
}
