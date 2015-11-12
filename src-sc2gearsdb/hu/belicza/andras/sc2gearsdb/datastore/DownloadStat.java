/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.datastore;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * A class that stores statistics for a download.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>"v"</code> property.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class DownloadStat extends DataStoreObject {
	
	/** Requested file. */
	@Persistent
	private String file;
	
	/** Download count. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int count;
	
	/** Java client count. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int javaClient;
	
	/** Unique download count. */
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int unique;
	
    /**
     * Creates a new DownloadStat.
     * @param file requested file
     */
    public DownloadStat( final String file ) {
    	this.file = file;
    	setV( 1 );
    }
	
	/**
	 * Increments the download count (by 1).
	 */
	public void incrementCount() {
		count++;
	}
	
	/**
	 * Increments the java client count (by 1).
	 */
	public void incrementJavaClient() {
		javaClient++;
	}
	
	/**
	 * Increments the unique download count (by 1).
	 */
	public void incrementUnique() {
		unique++;
	}
	
	public void setFile( String file ) {
	    this.file = file;
    }

	public String getFile() {
	    return file;
    }

	public void setCount( int count ) {
	    this.count = count;
    }

	public int getCount() {
	    return count;
    }

	public void setJavaClient( int javaClient ) {
	    this.javaClient = javaClient;
    }

	public int getJavaClient() {
	    return javaClient;
    }

	public void setUnique( int unique ) {
	    this.unique = unique;
    }

	public int getUnique() {
	    return unique;
    }

}
