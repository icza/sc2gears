/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.admin.client.beans;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class AccountInfo implements IsSerializable {
	
	private String googleAccount;
	private String contactEmail;
	private String name;
	private String country;
	private String comment;
	
	private boolean freeAccount;
	private boolean apiAccount;
	
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
	
    public String getCountry() {
    	return country;
    }
	
    public void setCountry( String country ) {
    	this.country = country;
    }

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public void setFreeAccount( boolean freeAccount ) {
	    this.freeAccount = freeAccount;
    }

	public boolean isFreeAccount() {
	    return freeAccount;
    }

	public void setApiAccount( boolean apiAccount ) {
	    this.apiAccount = apiAccount;
    }

	public boolean isApiAccount() {
	    return apiAccount;
    }
	
}
