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

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Key;

/**
 * A class that stores user data for a relay.
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
public abstract class RepUserData extends ClientTrackedObject {
	
	/** SHA-1 digest of the content of the replay. */
	@Persistent
	private String sha1;
	
	/** Key of the account who posted the data. */
	@Persistent
	private Key accountKey;
	
	/** Name of the user who posted the data. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String userName;
	
	/**
	 * Creates a new RepComment.
	 * @param sha1 SHA-1 digest of the content of the replay
	 */
	public RepUserData( final String sha1 ) {
		this.sha1 = sha1;
	}
	
	public String getSha1() {
		return sha1;
	}
	
	public void setSha1( String sha1 ) {
		this.sha1 = sha1;
	}
	
	public Key getAccountKey() {
		return accountKey;
	}
	
	public void setAccountKey( Key accountKey ) {
		this.accountKey = accountKey;
	}
	
	public void setUserName( String userName ) {
		this.userName = userName;
	}
	
	public String getUserName() {
		return userName;
	}
	
}
