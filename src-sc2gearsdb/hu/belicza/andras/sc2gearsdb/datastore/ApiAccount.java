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

import com.google.appengine.api.users.User;

/**
 * Class representing an API account.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>"v"</code> property.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class ApiAccount extends BaseAccount {
	
	/** API key used to make API calls. */
	@Persistent
	private String apiKey;
	
	/** Ops the user paid for.*/
	@Persistent
	private long paidOps;
	
	/** Notification level for available ops. */
	@Persistent
	private long notificationAvailOps;
	
	/**
	 * Increases the paid Ops with the specified amount.
	 * @param paidOps amount to increase the paid Ops with
	 */
	public void increasePaidOps( final long paidOps) {
		this.paidOps += paidOps;
	}
	
	/**
	 * Creates a new ApiAccount.
	 * @param user user associated with this API account
	 */
	public ApiAccount( final User user ) {
		super( user );
		setV( 1 );
	}
	
    public String getApiKey() {
    	return apiKey;
    }
	
    public void setApiKey( String apiKey ) {
    	this.apiKey = apiKey;
    }
	
    public long getPaidOps() {
    	return paidOps;
    }
	
    public void setPaidOps( long paidOps ) {
    	this.paidOps = paidOps;
    }
	
    public long getNotificationAvailOps() {
    	return notificationAvailOps;
    }
	
    public void setNotificationAvailOps( long notificationAvailOps ) {
    	this.notificationAvailOps = notificationAvailOps;
    }
	
}
