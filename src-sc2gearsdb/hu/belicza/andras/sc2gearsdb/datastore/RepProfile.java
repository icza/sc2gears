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
 * Class representing a replay profile.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>"v"</code> property.
 * 			<br>Made {@link #commentsCount}, {@link #ggsCount}, {@link #bgsCount} properties unindexed.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class RepProfile extends DataStoreObject {
	
	/** SHA-1 digest of the content of the file. */
	@Persistent
	private String sha1;
	
	/** Number of comments. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int commentsCount;
	
	/** Number of GG rates. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int ggsCount;
	
	/** Number of BG rates. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int bgsCount;
	
	/**
	 * Creates a new RepProfile.
	 * @param sha1 SHA-1 digest of the content of the file
	 */
	public RepProfile( final String sha1 ) {
		this.sha1 = sha1;
		setV( 1 );
	}
	
	/**
	 * Increments the comments count (by 1).
	 */
	public void incrementCommentsCount() {
		commentsCount++;
	}
	
	/**
	 * Increments the GG's count (by 1).
	 */
	public void incrementGgsCount() {
		ggsCount++;
	}
	
	/**
	 * Increments the BG's count (by 1).
	 */
	public void incrementBgsCount() {
		bgsCount++;
	}
	
	public String getSha1() {
		return sha1;
	}
	
	public void setSha1( String sha1 ) {
		this.sha1 = sha1;
	}
	
	public void setCommentsCount( int commentsCount ) {
	    this.commentsCount = commentsCount;
    }
	
	public int getCommentsCount() {
	    return commentsCount;
    }
	
	public void setGgsCount( int ggsCount ) {
	    this.ggsCount = ggsCount;
    }
	
	public int getGgsCount() {
	    return ggsCount;
    }
	
	public void setBgsCount( int bgsCount ) {
	    this.bgsCount = bgsCount;
    }
	
	public int getBgsCount() {
	    return bgsCount;
    }
	
}
