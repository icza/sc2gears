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
 * A holder of a boolean. This is to fill in a need of a modifiable Boolean.
 * 
 * @author Andras Belicza
 */
public class BoolHolder {
	
	/** The stored integer value. */
	public boolean value;
	
	/**
	 * Default constructor, creates a new BoolHolder.
	 */
	public BoolHolder() {
	}
	
	/**
	 * Creates a new BoolHolder with an initial value.
	 * @param value initial stored value
	 */
	public BoolHolder( final boolean value ) {
		this.value = value;
	}
	
}
