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

import com.google.appengine.api.users.User;

/**
 * Class representing an API account.
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
public abstract class BaseAccount extends DataStoreObject {
	
	/** Google Account user associated with this account. */
	@Persistent
	private User user;
	
	/** Contact email if differs from the Google account. */
	@Persistent
	private String contactEmail;
	
	/** Name of the account's person. */
	@Persistent
	private String name;
	
	/** Country of the account's person. */
	@Persistent
	private String country;
	
	/** Comment to the account. */
	@Persistent
	private String comment;
	
	/**
	 * Creates a new BaseAccount.
	 * @param user user associated with this account
	 */
	public BaseAccount( final User user ) {
		this.user = user;
	}
	
	/**
	 * Returns the name this account should be addressed by.
	 * <p>If {@link #name} is provided, it will be returned. Else the nick name of the {@link #user}.</p>
	 * @return the name this account should be addressed by
	 */
	public String getAddressedBy() {
		return name != null && !name.isEmpty() ? name : user.getNickname();
	}
	
    public User getUser() {
    	return user;
    }
	
    public void setUser( User user ) {
    	this.user = user;
    }
	
    public String getContactEmail() {
    	return contactEmail;
    }
	
    public void setContactEmail( String contactEmail ) {
    	this.contactEmail = contactEmail;
    }
	
    public String getName() {
    	return name;
    }
	
    public void setName( String name ) {
    	this.name = name;
    }
	
    public String getCountry() {
    	return country;
    }
	
    public void setCountry( String country ) {
    	this.country = country;
    }
    
    public String getComment() {
    	return comment;
    }
	
    public void setComment( String comment ) {
    	this.comment = comment;
    }

}
