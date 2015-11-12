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

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Text;

/**
 * Comment for a replay.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>"v"</code> property.
 * 			<br>Made <code>userAgent</code>, <code>country</code> properties unindexed (as part of {@link ClientTrackedObject}).  
 * 			<br>Made <code>userName</code> property unindexed (as part of {@link RepUserData}).  
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class RepComment extends RepUserData {
	
	/** The comment. */
	@Persistent
	private Text comment;
	
	/** Tells if the comment is hidden (censured). */
	@Persistent
	private boolean hidden;
	
	/**
	 * Creates a new RepComment.
	 * @param sha1 SHA-1 digest of the content of the replay
	 */
	public RepComment( final String sha1 ) {
		super( sha1 );
    	setV( 1 );
	}
	
	public void setComment( Text comment ) {
		this.comment = comment;
	}
	
	public Text getComment() {
		return comment;
	}
	
	public void setHidden( boolean hidden ) {
	    this.hidden = hidden;
    }
	
	public boolean isHidden() {
	    return hidden;
    }
	
	public void setDate( final Date date ) {
		this.date = date;
	}
	
}
