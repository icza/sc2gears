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
public class QuotaStatus implements IsSerializable {
	
	private long paidStorage;
	private long usedStorage;
	private int notificationQuotaLevel;
	
	public void setPaidStorage( long paidStorage ) {
	    this.paidStorage = paidStorage;
    }
	public long getPaidStorage() {
	    return paidStorage;
    }
	public void setUsedStorage( long usedStorage ) {
	    this.usedStorage = usedStorage;
    }
	public long getUsedStorage() {
	    return usedStorage;
    }
	public void setNotificationQuotaLevel( int notificationQuotaLevel ) {
	    this.notificationQuotaLevel = notificationQuotaLevel;
    }
	public int getNotificationQuotaLevel() {
	    return notificationQuotaLevel;
    }
	
}
