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
 * Defines a task which has 2 parameters and a return value.
 * 
 * @param <I1> type of the first argument of the execution
 * @param <I2> type of the second argument of the execution
 * @param <O>  return type of the execution
 * 
 * @author Andras Belicza
 */
public interface AdvancedTask< I1, I2, O > {
	
	/**
	 * Executes the task.
	 * @param argument1 first argument of the execution
	 * @param argument2 second argument of the execution
	 */
	O execute( I1 argument1, I2 argument2 );
	
}
