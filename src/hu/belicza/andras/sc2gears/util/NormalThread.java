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
 * A normal thread which does not take the inherited priority but instead sets NORM_PRIORITY.
 * 
 * @author Andras Belicza
 */
public class NormalThread extends Thread {
	
	/**
	 * Creates a new NormalThread.
	 * @param name name of the thread
	 */
	public NormalThread( final String name ) {
		super( name );
		setPriority( NORM_PRIORITY );
	}
	
}
