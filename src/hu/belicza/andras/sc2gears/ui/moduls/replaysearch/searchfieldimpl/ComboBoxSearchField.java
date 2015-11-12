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
import hu.belicza.andras.sc2gears.ui.components.BaseLabelListCellRenderer;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.SearchField;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Search field which displays a combo box to choose 1 element from a set,
 * and optionally a spinner to specify a minimum occurrence count.
 * 
 * @author Andras Belicza
 */
public abstract class ComboBoxSearchField extends SearchField {
	
	/** Combo box for the input.                   */
	protected final JComboBox< Object > comboBox;
	/** Spinner to specify a min occurrence count. */
	protected JSpinner                  minOccurrenceSpinner;
	
	/**
	 * Creates a new ComboBoxSearchField.
	 * @param id id of the search field
	 * @param valueVector  vector of values to add to the combo box (null values are not allowed!)
	 */
	public ComboBoxSearchField( final Id id, final Vector< Object > valueVector ) {
		this( id, valueVector, true );
	}
	
	/**
	 * Creates a new ComboBoxSearchField.
	 * @param id id of the search field
	 * @param valueVector  vector of values to add to the combo box (null values are not allowed!)
	 */
	public ComboBoxSearchField( final Id id, final Vector< Object > valueVector, final boolean showMinOccurence ) {
		super( id );
		
		comboBox = new JComboBox<>( valueVector );
		comboBox.setRenderer( new BaseLabelListCellRenderer< Object >() {
			@Override
			public Icon getIcon( final Object value ) {
				return ComboBoxSearchField.this.getIcon( value );
			}
		} );
		comboBox.setPreferredSize( new Dimension( 100, comboBox.getMinimumSize().height ) );
		uiComponent.add( comboBox );
		
		if ( showMinOccurence ) {
			minOccurrenceSpinner = new JSpinner( new SpinnerNumberModel( 1, 1, 999, 1 ) );
			uiComponent.add( new JLabel( Language.getText( "module.repSearch.tab.filters.name.minOccurrenceText" ) ) );
			minOccurrenceSpinner.setEditor( new JSpinner.NumberEditor( minOccurrenceSpinner ) );
			minOccurrenceSpinner.setMaximumSize( new Dimension( 50, minOccurrenceSpinner.getPreferredSize().height ) );
			uiComponent.add( minOccurrenceSpinner );
		}
	}
	
	/**
	 * Returns the icon for the specified value.<br>
	 * This implementation always returns <code>null</code>.
	 * @param value value whose icon to be returned
	 * @return the icon for the specified value
	 */
	protected Icon getIcon( final Object value ) {
		return null;
	}
	
	@Override
	public boolean validate() {
		return true; // Always true
	}
	
	@Override
	public void reset() {
		super.reset();
		
		comboBox.setSelectedIndex( 0 );
		if ( minOccurrenceSpinner != null )
			minOccurrenceSpinner.setValue( 1 );
	}
	
	/**
	 * Creates a vector that can be used to set for the combo box.<br>
	 * First sorts the values by their toString() method, then inserts an empty string to the first place.
	 * 
	 * @param valueCollection collection of values to be added
	 * @return a vector that can be used to set for the combo box
	 */
	protected static Vector< Object > createDataVector( final Collection< ? > valueCollection ) {
		final Vector< Object > valueVector = new Vector< Object >( valueCollection );
		
		Collections.sort( valueVector, new Comparator< Object >() {
			@Override
			public int compare( final Object o1, final Object o2 ) {
				return o1.toString().compareTo( o2.toString() ); // This will not allow null-s!
			}
		} );
		
		valueVector.insertElementAt( " ", 0 ); // Empty String is not good, the Label cell rendered would not display anything...
		
		return valueVector;
	}
	
	@Override
	public void saveValue( final Document document, final Element filterElement ) {
		super.saveValue( document, filterElement );
		
		appendDataElement( document, filterElement, "combo", comboBox.getSelectedIndex() );
		if ( minOccurrenceSpinner != null )
			appendDataElement( document, filterElement, "minOccurance", minOccurrenceSpinner.getValue() );
	}
	
	@Override
	public void loadValue( final Element filterElement ) {
		super.loadValue( filterElement );
		
		comboBox.setSelectedIndex( Integer.parseInt( getDataElementValue( filterElement, "combo" ) ) );
		if ( minOccurrenceSpinner != null )
			minOccurrenceSpinner.setValue( Integer.valueOf ( getDataElementValue( filterElement, "minOccurance" ) ) );
	}
	
}
