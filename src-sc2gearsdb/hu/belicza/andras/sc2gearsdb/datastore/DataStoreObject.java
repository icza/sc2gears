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

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 * Common ancestor for objects stored in the data store.
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
public abstract class DataStoreObject {
	
	/** Key of the entity. */
	@PrimaryKey
	@Persistent( valueStrategy = IdGeneratorStrategy.IDENTITY )
	protected Key key;
	
	/** Object creation date. */
	@Persistent
	protected Date date;
	
	/** Entity class format version. */
	@Persistent
	protected int v;
	
	/**
	 * Creates a new DataStoreObject.
	 */
	public DataStoreObject() {
		date = new Date();
	}
	
	/**
	 * Sets the key.
	 * @param key key to set
	 */
	public void setKey( final Key key ) {
		this.key = key;
	}
	
	/**
	 * Returns the key.
	 * @return the key
	 */
	public Key getKey() {
		return key;
	}
	
	/**
	 * Returns the creation date.
	 * @return the creation date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Sets the entity class format version.
	 * @param v the entity class format version to be set
	 */
	public void setV( int v ) {
		this.v = v;
	}

	/**
	 * Returns the entity class format version.
	 * @return the entity class format version
	 */
	public int getV() {
		return v;
	}
	
}
