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

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class SettingsInfo implements IsSerializable {
	
	private String  googleAccount;
	private String  contactEmail;
	private String  userName;
	private int     notificationQuotaLevel;
	private boolean convertToRealTime;
	private int     mapImageSize;
	private int     displayWinners;
	
	private List< String > favoredPlayerList;
	
	private List< String > grantedUsers;
	private List< Long >   grantedPermissions;
	
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

	public void setNotificationQuotaLevel( int notificationQuotaLevel ) {
	    this.notificationQuotaLevel = notificationQuotaLevel;
    }

	public int getNotificationQuotaLevel() {
	    return notificationQuotaLevel;
    }

	public void setConvertToRealTime( boolean convertToRealTime ) {
	    this.convertToRealTime = convertToRealTime;
    }

	public boolean isConvertToRealTime() {
	    return convertToRealTime;
    }

	public void setMapImageSize( int mapImageSize ) {
	    this.mapImageSize = mapImageSize;
    }

	public int getMapImageSize() {
	    return mapImageSize;
    }

	public void setFavoredPlayerList( List< String > favoredPlayerList ) {
	    this.favoredPlayerList = favoredPlayerList;
    }

	public List< String > getFavoredPlayerList() {
	    return favoredPlayerList;
    }
	
	public void setGrantedUsers( List< String > grantedUsers ) {
	    this.grantedUsers = grantedUsers;
    }

	public List< String > getGrantedUsers() {
	    return grantedUsers;
    }

	public void setGrantedPermissions( List< Long > grantedPermissions ) {
	    this.grantedPermissions = grantedPermissions;
    }

	public List< Long > getGrantedPermissions() {
	    return grantedPermissions;
    }

	public void setDisplayWinners( int displayWinners ) {
	    this.displayWinners = displayWinners;
    }

	public int getDisplayWinners() {
	    return displayWinners;
    }

}
