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
 * Exception indicating that the message is intended for the user.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings( "serial" )
public class MessageException extends RuntimeException {
	
    /**
     * Creates a new MessageException.
     * @param message message for the user
     */
	public MessageException( final String message ) {
		super( message );
    }
	
    /**
     * Creates a new MessageException.
     * @param message message for the user
     * @param cause the cause
     */
	public MessageException( final String message, final Throwable cause ) {
		super( message, cause );
    }
	
}
