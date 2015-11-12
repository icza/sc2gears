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
 * Defines a task which has a parameter.
 * 
 * @param <T> type of the argument of the execution
 * 
 * @author Andras Belicza
 */
public interface Task< T > {
	
	/**
	 * Executes the task.
	 * @param argument argument of the execution
	 */
	void execute( T argument );
	
}
