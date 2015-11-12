/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.datastore;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 * Common ancestor for objects stored in the data store based on best-practices.
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
public abstract class _DatastoreObject {
	
	/** Key of the entity. */
	@PrimaryKey
	@Persistent( valueStrategy = IdGeneratorStrategy.IDENTITY )
	private Key k;
	
	/** Entity creation date. */
	@Persistent
	protected Date c;
	
	/**
	 * Entity class format version.
	 * 
	 * <p>When the format of the entity changes, this should be incremented.
	 * 0 value which is the field's default should not be used so potential errors can be detected (if this value would remain zero).
	 * 1 should be used as the first version.</p>
	 * 
	 * <p>At least the following changes should make you increase the format version:
	 * <ul>
	 * 	<li>A new property is added to the class
	 * 	<li>An existing property is renamed or removed from the class, or its type changes in an incompatible way (for example from Date to String)
	 * 	<li>A property is made indexed/unindexed (this may make certain entities disappear from queries even though matching the query condition)
	 * 	<li>The meaning of the data stored in a property changes (for example a property <code>"r"</code> stored the revision info but from now on stores the rating info)
	 * </ul></p>
	 */
	@Persistent
	private int v;
	
	/**
	 * Creates a new _DatastoreObject.
	 */
	public _DatastoreObject() {
		c = new Date();
		v = getCurrentVersion();
	}
	
	/**
	 * Returns the current format version of the class.
	 * @return the current format version of the class
	 */
	public abstract int getCurrentVersion();
	
	/**
	 * Actualizes the version of the entity.
	 * 
	 * <p>This method should be called if an entity with a previous version is re-saved to reflect that its format is now the current format.</p>
	 * 
	 * <p>It's a good practice to always call this method if a loaded entity is modified and re-saved
	 * because we might never know if a loaded entity is not the current version.</p>
	 */
	public void actualizeVersion() {
		v = getCurrentVersion();
	}
	
	/**
	 * Returns the creation date.
	 * @return the creation date
	 */
	public Date getC() {
		return c;
	}
	
	public void setK( final Key k ) {
		this.k = k;
	}
	
	public Key getK() {
		return k;
	}
	
	public void setV( final int v ) {
		this.v = v;
	}
	
	public int getV() {
		return v;
	}
	
	/**
	 * Cuts a string if it's too long so it can be stored in the Datastore as a string.<br>
	 * The string size limit is 500 in the datastore.
	 * @param s string to be cut if too long
	 * @return the same string if it's not long; a cut string otherwise
	 */
	public static String cutString( final String s ) {
		if ( s != null && s.length() > 500 )
			return s.substring( 0, 500 );
		return s;
	}
	
}
