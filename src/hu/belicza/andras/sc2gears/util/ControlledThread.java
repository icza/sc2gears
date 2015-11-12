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
 * A controlled thread with helper methods to stop it.
 * 
 * @author Andras Belicza
 */
public class ControlledThread extends NormalThread {
	
	/**
	 * Tells if a request has been made to cancel the execution of the thread.
	 * The subclasses are responsible to periodically check this variable in their overridden run() method
	 * whether they are allowed to continue their work or they have to return in order to end the thread.
	 */
	protected volatile boolean requestedToCancel;
	
	/**
	 * Creates a new ControlledThread.
	 * @param name name of the thread
	 */
	public ControlledThread( final String name ) {
		super( name );
	}
	
	/**
	 * Requests the cancel of the execution of the thread.
	 */
	public void requestToCancel() {
		// Volatile variables are synchronized internally, so no need external synchronization here.
		requestedToCancel = true;
	}
	
	/**
	 * Tells whether a cancel has been requested.
	 * @return true if a cancel has been requested; false otherwise
	 */
	public boolean isCancelRequested() {
		return requestedToCancel;
	}
	
	/**
	 * Shuts down this thread.<br>
	 * First calls {@link #requestToCancel()} and then waits for this thread to close by calling {@link #join()}.
	 */
	public void shutdown() {
		requestToCancel();
		
		try {
			join();
		} catch ( final InterruptedException ie ) {
			ie.printStackTrace();
		}
	}
	
}
