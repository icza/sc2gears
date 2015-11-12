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

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class VisitInfo implements IsSerializable {
	
	private Date date;
	private String googleAccount;
	private String location;
	private String ip;
	private boolean hasAccount;
	private String userAgent;
	
    public Date getDate() {
    	return date;
    }
	
    public void setDate( Date date ) {
    	this.date = date == null ? null : new Date( date.getTime() );
    }
	
    public String getGoogleAccount() {
    	return googleAccount;
    }
	
    public void setGoogleAccount( String googleAccount ) {
    	this.googleAccount = googleAccount;
    }
	
    public String getLocation() {
    	return location;
    }
	
    public void setLocation( String location ) {
    	this.location = location;
    }
	
    public String getIp() {
    	return ip;
    }
	
    public void setIp( String ip ) {
    	this.ip = ip;
    }
	
    public boolean isHasAccount() {
    	return hasAccount;
    }
	
    public void setHasAccount( boolean hasAccount ) {
    	this.hasAccount = hasAccount;
    }

	public void setUserAgent( String userAgent ) {
	    this.userAgent = userAgent;
    }

	public String getUserAgent() {
	    return userAgent;
    }
	
}
