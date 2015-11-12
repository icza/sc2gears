/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.util;

/**
 * A generic reference holder object whose reference can be changed.
 * 
 * @author Andras Belicza
 *
 * @param <T> type of the "holded" object
 */
public class Holder< T > {
	
	/** Reference of the holded object */
	public T value;
	
	/**
	 * Creates a new Holder.
	 */
	public Holder() {
	}
	
	/**
	 * Creates a new Holder.
	 * @param value reference to the holded object
	 */
	public Holder( final T value ) {
		this.value = value;
	}
	
}
