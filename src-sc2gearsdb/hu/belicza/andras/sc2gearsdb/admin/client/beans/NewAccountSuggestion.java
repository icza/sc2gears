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
public class NewAccountSuggestion implements IsSerializable {
	
	private String googleAccount;
	private String countryCode;
	private String countryName;
	
	public void setGoogleAccount( String googleAccount ) {
	    this.googleAccount = googleAccount;
    }
	public String getGoogleAccount() {
	    return googleAccount;
    }
	public void setCountryCode( String countryCode ) {
	    this.countryCode = countryCode;
    }
	public String getCountryCode() {
	    return countryCode;
    }
	public void setCountryName( String countryName ) {
	    this.countryName = countryName;
    }
	public String getCountryName() {
	    return countryName;
    }
	
}
