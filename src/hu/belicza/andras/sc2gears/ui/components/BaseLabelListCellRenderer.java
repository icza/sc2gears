/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.components;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * A list cell renderer which uses a JLabel to render its cells.
 * 
 * @author Andras Belicza
 */
public abstract class BaseLabelListCellRenderer< T > implements ListCellRenderer< T > {
	
	/** The label used to render the cells. */
	protected final JLabel     label = new JLabel();
	
	/** Reference to the owner this cell renderer belongs to.<br>
	 * This is used for example to determine if the cell should be enabled/disabled. */
	protected final JComponent owner;
	
	/**
	 * Creates a new BaseLabelListCellRenderer.
	 */
	public BaseLabelListCellRenderer() {
		this( 0, null );
	}
	
	/**
	 * Creates a new BaseLabelListCellRenderer.
	 * @param verticalSpacing vertical space to use on top an on bottom of the items
	 */
	public BaseLabelListCellRenderer( final int verticalSpacing ) {
		this( verticalSpacing, null );
	}
	
	/**
	 * Creates a new BaseLabelListCellRenderer.
	 * @param verticalSpacing vertical space to use on top an on bottom of the items
	 * @param owner reference to the owner component this cell renderer belongs to
	 */
	public BaseLabelListCellRenderer( final int verticalSpacing, final JComponent owner ) {
		this.owner = owner;
		label.setBorder( BorderFactory.createEmptyBorder( verticalSpacing, 3, verticalSpacing, 1 ) );
		label.setOpaque( true ); // This is required for some LAF: else the focused item in the drop down list would not be visible (due to having background color)
	}
	
	@Override
	public Component getListCellRendererComponent( final JList< ? extends T > list, final T value, final int index, final boolean isSelected, final boolean cellHasFocus ) {
		if ( owner != null )
			label.setEnabled( owner.isEnabled() );
		label.setText( value.toString() );
		label.setIcon( getIcon( value ) );
		
		if ( isSelected ) {
			label.setBackground( list.getSelectionBackground() );
			label.setForeground( list.getSelectionForeground() );
		} else {
			label.setBackground( list.getBackground() );
			label.setForeground( list.getForeground() );
		}
		
		label.setFont( list.getFont() );
		
		return label;
	}
	
	/**
	 * Returns the icon for the specified value.<br>
	 * @param value value whose icon to be returned
	 * @return the icon for the specified value
	 */
	public abstract Icon getIcon( final T value );
	
} 
