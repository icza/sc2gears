/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.user.client.beans;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class FreeAccountInfo implements IsSerializable {
	
	private String googleAccount;
	private String name;
	private String contactEmail;
	
	public void setGoogleAccount(String googleAccount) {
		this.googleAccount = googleAccount;
	}

	public String getGoogleAccount() {
		return googleAccount;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

    public String getName() {
    	return name;
    }

    public void setName( String name ) {
    	this.name = name;
    }
	
}
