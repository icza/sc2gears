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

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;

/**
 * Class representing a visit to the site.
 * 
 * <p>The {@link InheritanceStrategy#SUBCLASS_TABLE} strategy will tell to store properties of this class
 * on the tables of the subclasses.</p>
 * 
 * <p>This class is abstract because we do not want to save instances of this class.</p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
@Inheritance( strategy = InheritanceStrategy.SUBCLASS_TABLE )
public abstract class BaseVisit extends ClientTrackedObject {
	
	/** Key of the visitor account. */
	@Persistent
	private Key visitorKey;
	
	/** The visitor's Google Account. */
	@Persistent
	private User user;
	
	/**
	 * Creates a new Visit.
	 * @param user the visitor's Google Account
	 */
	public BaseVisit( final User user ) {
		this.user = user;
	}
	
	public Key getVisitorKey() {
		return visitorKey;
	}

	public void setVisitorKey(Key visitorKey) {
		this.visitorKey = visitorKey;
	}

	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
}
