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
 * Defines a Producer which can produce an output value from an input value.
 * 
 * @param <I> type of the input parameter
 * @param <O> type of the output parameter
 * 
 * @author Andras Belicza
 */
public interface Producer< I, O > {
	
	/**
	 * Produces an output value from an input value.
	 * @param input the input argument 
	 */
	O produce( I input );
	
}
