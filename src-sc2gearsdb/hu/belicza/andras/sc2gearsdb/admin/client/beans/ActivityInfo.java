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

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class ActivityInfo implements IsSerializable {
	
	private int visitsCount;
	private int accountsCount;
	private int mousePracticeGameScoresCount;
	private int mapsCount;
	private int repProfilesCount;
	private int repCommentsCount;
	private int repRatesCount;
	private int releaseDownloadsCount;
	private int eventsCount;
	
	/** A sorted list of the latest file type stat info. */
	private List< FileStatInfo > latestFileStatInfoList;
	
    public int getVisitsCount() {
    	return visitsCount;
    }
	
    public void setVisitsCount( int visitsCount ) {
    	this.visitsCount = visitsCount;
    }
	
    public int getAccountsCount() {
    	return accountsCount;
    }

    public void setAccountsCount( int accountsCount ) {
    	this.accountsCount = accountsCount;
    }
	
    public int getMousePracticeGameScoresCount() {
    	return mousePracticeGameScoresCount;
    }
	
    public void setMousePracticeGameScoresCount( int mousePracticeGameScoresCount ) {
    	this.mousePracticeGameScoresCount = mousePracticeGameScoresCount;
    }

	public void setMapsCount( int mapsCount ) {
	    this.mapsCount = mapsCount;
    }

	public int getMapsCount() {
	    return mapsCount;
    }
	
    public int getRepProfilesCount() {
    	return repProfilesCount;
    }
	
    public void setRepProfilesCount( int repProfilesCount ) {
    	this.repProfilesCount = repProfilesCount;
    }
	
    public int getRepCommentsCount() {
    	return repCommentsCount;
    }

    public void setRepCommentsCount( int repCommentsCount ) {
    	this.repCommentsCount = repCommentsCount;
    }
	
    public int getRepRatesCount() {
    	return repRatesCount;
    }

    public void setRepRatesCount( int repRatesCount ) {
    	this.repRatesCount = repRatesCount;
    }

	public void setReleaseDownloadsCount( int releaseDownloadsCount ) {
	    this.releaseDownloadsCount = releaseDownloadsCount;
    }

	public int getReleaseDownloadsCount() {
	    return releaseDownloadsCount;
    }

	public void setEventsCount( int eventsCount ) {
	    this.eventsCount = eventsCount;
    }

	public int getEventsCount() {
	    return eventsCount;
    }

    public List< FileStatInfo > getLatestFileStatInfoList() {
    	return latestFileStatInfoList;
    }

    public void setLatestFileStatInfoList( List< FileStatInfo > latestFileStatInfoList ) {
    	this.latestFileStatInfoList = latestFileStatInfoList;
    }

}
