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
public class DbPackageInfo implements IsSerializable {
	
	private String dbPackageIcon;
	private String dbPackageName;
	private int priceUSD;
	private long storage;
	private int numOfReplays;
	
	public void setDbPackageIcon( String packageIcon ) {
	    this.dbPackageIcon = packageIcon;
    }
	public String getDbPackageIcon() {
	    return dbPackageIcon;
    }
	public void setDbPackageName( String dbPackageName ) {
	    this.dbPackageName = dbPackageName;
    }
	public String getDbPackageName() {
	    return dbPackageName;
    }
	public void setPriceUSD( int priceUSD ) {
	    this.priceUSD = priceUSD;
    }
	public int getPriceUSD() {
	    return priceUSD;
    }
	public void setStorage( long storage ) {
	    this.storage = storage;
    }
	public long getStorage() {
	    return storage;
    }
	public void setNumOfReplays( int numOfReplays ) {
	    this.numOfReplays = numOfReplays;
    }
	public int getNumOfReplays() {
	    return numOfReplays;
    }
	
}
