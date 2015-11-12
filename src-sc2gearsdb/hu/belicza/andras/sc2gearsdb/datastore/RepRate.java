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
 * Rate of a replay.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>"v"</code> property.
 * 			<br>Made <code>userAgent</code>, <code>country</code> properties unindexed (as part of {@link ClientTrackedObject}).  
 * 			<br>Made <code>userName</code> property unindexed (as part of {@link RepUserData}).  
 * 			<br>Made {@link #gg}, {@link #bg} properties unindexed.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class RepRate extends RepUserData {
	
	/** Is GG?. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private boolean gg;
	
	/** Is BG?. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private boolean bg;
	
	/**
	 * Creates a new RepRate.
	 * @param sha1 SHA-1 digest of the content of the replay
	 */
	public RepRate( final String sha1 ) {
		super( sha1 );
    	setV( 1 );
	}
	
	public void setGg( boolean gg ) {
	    this.gg = gg;
    }
	
	public boolean isGg() {
	    return gg;
    }
	
	public void setBg( boolean bg ) {
	    this.bg = bg;
    }
	
	public boolean isBg() {
	    return bg;
    }
	
}
