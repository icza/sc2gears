/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.impl.util;

/**
 * A holder of an integer. This is to fill in a need of a modifiable Integer.
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 */
public class IntHolder implements Comparable< IntHolder > {
	
	/** The stored integer value. */
	public int value;
	
	/**
	 * Default constructor, creates a new IntHolder with a stored value of 0.
	 */
	public IntHolder() {
	}
	
	/**
	 * Creates a new IntHolder with an initial value.
	 * @param value initial stored value
	 */
	public IntHolder( final int value ) {
		this.value = value;
	}
	
	/**
	 * Defines a natural order of the stored values. 
	 */
	@Override
	public int compareTo( final IntHolder o ) {
		return value - o.value;
	}
	
}
