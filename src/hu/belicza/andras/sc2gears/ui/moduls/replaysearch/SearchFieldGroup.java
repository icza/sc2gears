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

import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents a search field groups.
 * 
 * <p>A search field groups is a group of the search fields of the same type.
 * The group provides access to add new search field or remove any parts of it, but ensures that there is always at least one.
 * </p>
 * 
 * @author Andras Belicza
 */
public class SearchFieldGroup {
	
	/** Name of the filter group tag used to save the values of this group. */
	public static final String FILTER_GROUP_TAG_NAME = "filterGroup";
	/** Name of the id attribute used to uniquely identify filter groups.   */
	public static final String ID_ATTRIBUTE_NAME     = "id";
	
	/** Parent to notify when search fields are added or removed. */
	public  final Container                      parentToNotify;
	/** Class of the search field that this group is made of.     */
	private final Class< ? extends SearchField > searchFieldType;
	
	/** The UI component of the search field group, this has to be added to a container. */
	public final Box uiComponent     = Box.createHorizontalBox();
	/** Box that stores the search fields of this group. */
	public final Box searchFieldsBox = Box.createVerticalBox();
	
	/** List of search fields of the group. */
	public final List< SearchField > searchFieldList = new ArrayList< SearchField >(); 
	
	/**
	 * Creates a new SearchFieldGroups.
	 * @param parentToNotify  parent to notify when search fields are added or removed
	 * @param searchFieldType class of the search field that this group is made of
	 */
	public SearchFieldGroup( final Container parentToNotify, final Class< ? extends SearchField > searchFieldType ) {
		this.parentToNotify  = parentToNotify;
		this.searchFieldType = searchFieldType;
		
		uiComponent.setBorder( BorderFactory.createRaisedBevelBorder() );
		uiComponent.add( searchFieldsBox );
		final JLabel addNewLabel = GuiUtils.createIconLabelButton( Icons.PLUS, "module.repSearch.tab.filters.addNewFieldToolTip" );
		addNewLabel.addMouseListener( new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				addNewSearchField();
			};
		} );
		uiComponent.add( addNewLabel );
		
		addNewSearchField();
	}
	
	/**
	 * Adds a new search field to this group.
	 */
	private void addNewSearchField() {
		try {
			final Box searchFieldBox = Box.createHorizontalBox();
			
			final SearchField newSearchField = searchFieldType.newInstance();
			if ( !searchFieldList.isEmpty() ) // If not the first, we have to set the size of display label
				newSearchField.displayLabel.setPreferredSize( new Dimension( searchFieldList.get( 0 ).displayLabel.getPreferredSize().width, newSearchField.displayLabel.getPreferredSize().height ) );
			searchFieldList.add( newSearchField );
			searchFieldBox.add( newSearchField.uiComponent );
			
			searchFieldBox.add( Box.createHorizontalStrut( 6 ) );
			
			final JLabel removeLabel = GuiUtils.createIconLabelButton( Icons.MINUS, "module.repSearch.tab.filters.removeFieldToolTip" );
			removeLabel.addMouseListener( new MouseAdapter() {
				public void mouseClicked( final MouseEvent event ) {
					if ( searchFieldsBox.getComponentCount() > 1 ) {
						searchFieldList.remove( newSearchField );
						searchFieldsBox.remove( searchFieldBox );
						updateRemoveLabels();
						parentToNotify.validate();
					}
				};
			} );
			searchFieldBox.add( removeLabel );
			
			searchFieldsBox.add( searchFieldBox );
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
		
		updateRemoveLabels();
		parentToNotify.validate();
	}
	
	/**
	 * Updates the remove labels so that if the group contains only 1 field, it will be disabled.
	 */
	private void updateRemoveLabels() {
		final boolean enabled = searchFieldsBox.getComponentCount() != 1;
		
		for ( int i = searchFieldsBox.getComponentCount() - 1; i >= 0; i-- ) {
			final Box searchFieldBox = (Box) searchFieldsBox.getComponent( i );
			searchFieldBox.getComponent( searchFieldBox.getComponentCount() - 1 ).setEnabled( enabled );
		}
	}
	
	/**
	 * Validates the value entered into the search fields of this group.<br>
	 * @return true if all the entered values are valid; false otherwise
	 */
	public boolean validateAll() {
		boolean validAll = true;
		
		for ( final SearchField searchField : searchFieldList )
			validAll &= searchField.validate();
		
		return validAll;
	}
	
	/**
	 * Resets all search fields of this group.
	 */
	public void resetAll() {
		for ( final SearchField searchField : searchFieldList )
			searchField.reset();
	}
	
	/**
	 * Returns the replay filters of the search fields of the group.
	 * @return the replay filters of the search fields of the group or <code>null</code> if none of the search fields in this group define a filter (contains initial value)
	 */
	public ReplayFilter[] getReplayFilters() {
		final List< ReplayFilter > replayFilterList = new ArrayList< ReplayFilter >( searchFieldList.size() );
		
		for ( final SearchField searchField : searchFieldList ) {
			final ReplayFilter replayFilter = searchField.getFilter();
			if ( replayFilter != null )
				replayFilterList.add( replayFilter );
		}
		
		return replayFilterList.isEmpty() ? null : replayFilterList.toArray( new ReplayFilter[ replayFilterList.size() ] );
	}
	
	/**
	 * Saves the values of the search fields of this group into the specified document. 
	 * @param document      document to save the value in
	 * @param parentElement parent element to append the created new elements to
	 */
	public void saveValues( final Document document, final Element parentElement ) {
		final Element filterGroupElement = document.createElement( FILTER_GROUP_TAG_NAME );
		filterGroupElement.setAttribute( ID_ATTRIBUTE_NAME, searchFieldList.get( 0 ).id.name() );
		
		for ( final SearchField searchField : searchFieldList ) {
			final Element filterElement = document.createElement( "filter" );
			searchField.saveValue( document, filterElement );
			filterGroupElement.appendChild( filterElement );
		}
		
		parentElement.appendChild( filterGroupElement );
	}
	
	/**
	 * Loads the saved value from the specified document.
	 * @param document document to load the value from
	 * @param subId    sub-id to differentiate between multiple instances of this search field
	 */
	public void loadValues( final Document document ) {
		final Element filterGroupElement = document.getElementById( searchFieldList.get( 0 ).id.name() );
		
		boolean filterGroupMissing = true;
		
		if ( filterGroupElement != null ) {
			final NodeList filterElementList = filterGroupElement.getElementsByTagName( "filter" );
			
			final int filtersCount = filterElementList.getLength();
			if ( filtersCount > 0 ) {
				ensureExactSearchFieldsCount( filtersCount );
				
				for ( int i = 0; i < filtersCount; i++ ) {
					final SearchField searchField = searchFieldList.get( i );
					searchField.reset(); // To clear previous errors
					searchField.loadValue( (Element) filterElementList.item( i ) );
				}
				
				filterGroupMissing = false;
			}
		}
		
		if ( filterGroupMissing ) {
			ensureExactSearchFieldsCount( 1 );
			searchFieldList.get( 0 ).reset(); // To clear previous errors
		}
	}
	
	/**
	 * Sets how many search fields to have exactly in this group.<br>
	 * If more is contained, they will be removed. If less, new ones will be added.
	 * @param searchFieldsCount search fields count
	 */
	private void ensureExactSearchFieldsCount( final int searchFieldsCount ) {
		int difference = searchFieldsCount - searchFieldList.size();
		
		if ( difference == 0 )
			return;
		
		if ( difference > 0 )
			while ( difference-- > 0 )
				addNewSearchField();
		else
			while ( difference++ < 0 ) {
				searchFieldList.remove( searchFieldList.size             () - 1 );
				searchFieldsBox.remove( searchFieldsBox.getComponentCount() - 1 );
			}
		
		updateRemoveLabels();
		parentToNotify.validate();
	}
	
}
