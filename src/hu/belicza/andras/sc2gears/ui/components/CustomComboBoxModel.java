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

import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

/**
 * Custom combo box model which allows firing an event indicating that all contents might have changed.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class CustomComboBoxModel< T > extends DefaultComboBoxModel< T > {
	
	/**
	 * Creates a new CustomComboBoxModel.
	 */
	public CustomComboBoxModel() {
		super();
	}
	
	/**
	 * Creates a new CustomComboBoxModel.
	 * @param items initial items to be put into the model
	 */
	public CustomComboBoxModel( final T[] items ) {
		super( items );
	}
	
	/**
	 * Creates a new CustomComboBoxModel.
	 * @param vector vector of the model to be used
	 */
	public CustomComboBoxModel( final Vector< T > vector ) {
		super( vector );
	}
	
	/**
	 * Fires an event that all contents might have changed.
	 * @param source source of the event
	 */
	public void fireContentsChanged( final Object source ) {
		fireContentsChanged( source, 0, getSize() - 1 );
	}
	
}
