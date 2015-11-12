/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.apiuser.client.beans;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class SettingsInfo implements IsSerializable {
	
	private String  googleAccount;
	private String  contactEmail;
	private String  userName;
	private long    notificationAvailOps;
	
	public void setGoogleAccount( String googleAccount ) {
	    this.googleAccount = googleAccount;
    }
	
	public String getGoogleAccount() {
	    return googleAccount;
    }
	
	public void setContactEmail( String contactEmail ) {
	    this.contactEmail = contactEmail;
    }
	
	public String getContactEmail() {
	    return contactEmail;
    }
	
	public void setUserName( String userName ) {
	    this.userName = userName;
    }
	
	public String getUserName() {
	    return userName;
    }

	public void setNotificationAvailOps( long notificationAvailOps ) {
	    this.notificationAvailOps = notificationAvailOps;
    }

	public long getNotificationAvailOps() {
	    return notificationAvailOps;
    }

}
