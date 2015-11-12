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

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class FileStatInfo implements IsSerializable {
	
	private String googleAccount;
	private String dbPackageName;
	private String dbPackageIcon;
	private long   paidStorage;
	
	private boolean hasFileStat;
	private int  allCount;
	private long allStorage;
	private int  repCount;
	private long repStorage;
	private int  smpdCount;
	private long smpdStorage;
	private int  otherCount;
	private long otherStorage;
	
	private Date recalced;
	
	public void setGoogleAccount( String googleAccount ) {
	    this.googleAccount = googleAccount;
    }

	public String getGoogleAccount() {
	    return googleAccount;
    }

	public void setDbPackageName( String dbPackageName ) {
	    this.dbPackageName = dbPackageName;
    }

	public String getDbPackageName() {
	    return dbPackageName;
    }

	public void setDbPackageIcon( String dbPackageIcon ) {
	    this.dbPackageIcon = dbPackageIcon;
    }

	public String getDbPackageIcon() {
	    return dbPackageIcon;
    }

	public void setPaidStorage( long paidStorage ) {
	    this.paidStorage = paidStorage;
    }

	public long getPaidStorage() {
	    return paidStorage;
    }
	
	public void setHasFileStat( boolean hasFileStat ) {
	    this.hasFileStat = hasFileStat;
    }

	public boolean isHasFileStat() {
	    return hasFileStat;
    }

	public void setAllCount( int allCount ) {
	    this.allCount = allCount;
    }

	public int getAllCount() {
	    return allCount;
    }

	public void setAllStorage( long allStorage ) {
	    this.allStorage = allStorage;
    }

	public long getAllStorage() {
	    return allStorage;
    }

	public void setRepCount( int repCount ) {
	    this.repCount = repCount;
    }

	public int getRepCount() {
	    return repCount;
    }

	public void setRepStorage( long repStorage ) {
	    this.repStorage = repStorage;
    }

	public long getRepStorage() {
	    return repStorage;
    }

	public void setSmpdCount( int smpdCount ) {
	    this.smpdCount = smpdCount;
    }

	public int getSmpdCount() {
	    return smpdCount;
    }

	public void setSmpdStorage( long smpdStorage ) {
	    this.smpdStorage = smpdStorage;
    }

	public long getSmpdStorage() {
	    return smpdStorage;
    }

	public void setOtherCount( int otherCount ) {
	    this.otherCount = otherCount;
    }

	public int getOtherCount() {
	    return otherCount;
    }

	public void setOtherStorage( long otherStorage ) {
	    this.otherStorage = otherStorage;
    }

	public long getOtherStorage() {
	    return otherStorage;
    }

	public void setRecalced( Date recalced ) {
	    this.recalced = recalced == null ? null : new Date( recalced.getTime() );
    }

	public Date getRecalced() {
	    return recalced;
    }

}
