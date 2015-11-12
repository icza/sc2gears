/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.datastore;

import javax.jdo.annotations.PersistenceCapable;

import com.google.appengine.api.users.User;

/**
 * Class representing a visit to the User module.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>"v"</code> property.
 * 			<br>Made <code>userAgent</code>, <code>country</code> properties unindexed (as part of {@link ClientTrackedObject}).  
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class Visit extends BaseVisit {
	
	/**
	 * Creates a new Visit.
	 * @param user the visitor's Google Account
	 */
	public Visit( final User user ) {
		super( user );
		setV( 1 );
	}
	
}
