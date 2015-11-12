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
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Key;

/**
 * Class representing an event.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>"v"</code> property.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class Event extends DataStoreObject {
	
	/**
	 * Event type.
	 * @author Andras Belicza
	 */
	public enum Type {
		// NEW ITEMS CAN ONLY BE ADDED TO THE END OF THE LIST (because ordinal is stored in the Event entity)!!!!!!
		/** Notification storage quota exceeded. */
		NOTIFICATION_STORAGE_QUOTA_EXCEEDED,
		/** Storage quota exceeded.              */
		STORAGE_QUOTA_EXCEEDED,
		/** Notification available Ops exceeded. */
		NOTIFICATION_AVAIL_OPS_EXCEEDED,
		/** Change authorization key.            */
		CHANGE_AUTHORIZATION_KEY,
		/** Change API key.                      */
		CHANGE_API_KEY,
		/** File stats recalculation requested.  */
		FILE_STATS_RECALC_TRIGGERED,
		/** Change Google account.               */
		CHANGE_GOOGLE_ACCOUNT,
		/** Change API Google account.           */
		CHANGE_API_GOOGLE_ACCOUNT
	}
	
	/** Key of the entity bound to the event. */
	@Persistent
	private Key entityKey;
	
	/** Type of the event */
	@Persistent
	private int type;
	
	/** Name of event. */
	@Persistent
	private String name;
	
	/** Comment. */
	@Persistent
	private String comment;
	
	/**
	 * Creates a new Event.
	 * @param entityKey key of the entity bound to the event
	 * @param type      type of the event
	 */
	public Event( final Key entityKey, final Type type ) {
		this( entityKey, type, null );
	}
	
	/**
	 * Creates a new Event.
	 * @param entityKey key of the entity bound to the event
	 * @param type      type of the event
	 * @param comment   comment
	 */
	public Event( final Key entityKey, final Type type, final String comment ) {
		this.entityKey = entityKey;
		this.type      = type.ordinal();
		this.name      = type.name();
		this.comment   = comment;
		setV( 1 );
	}
	
	public void setEntityKey( Key entityKey ) {
	    this.entityKey = entityKey;
    }

	public Key getEntityKey() {
	    return entityKey;
    }

	public void setType( int type ) {
	    this.type = type;
    }

	public int getType() {
	    return type;
    }

	public void setName( String name ) {
	    this.name = name;
    }

	public String getName() {
	    return name;
    }

	public void setComment( String comment ) {
	    this.comment = comment;
    }

	public String getComment() {
	    return comment;
    }
	
}
