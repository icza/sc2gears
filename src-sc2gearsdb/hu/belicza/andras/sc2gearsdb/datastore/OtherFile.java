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

import hu.belicza.andras.sc2gearsdb.FileServlet;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Key;

/**
 * Class representing other files.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>v</code> property.
 * 		<li>Added <code>blobKey</code>, <code>content</code> properties (as part of {@link FileMetaData}) to dispose of the <code>File</code> type.
 * 		<li>Made <code>fname</code>, <code>lastmod</code>, <code>size</code> properties (as part of {@link FileMetaData}) unindexed.
 * 			<br>Made {@link #comment} property unindexed.
 * 		<li>File <code>content</code> is now stored locally (as part of {@link FileMetaData}) if size is less than a specified limit (details at {@link FileServlet}).
 * 			<br>Made <code>blobKey</code>, <code>content</code> properties (as part of {@link FileMetaData}) unindexed.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class OtherFile extends FileMetaData {
	
	/** Comment. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String comment;
	
	/** Extension (derived from the file name). */
	@Persistent
	private String ext;
	
	/**
	 * Creates a new OtherFile.
	 * @param ownerk  key of the owner account
	 * @param fname   name of the mouse print
	 * @param lastmod last modified date of the file
	 * @param sha1    SHA-1 digest of the content of the mouse print
	 * @param size    size of the file
	 */
	public OtherFile( final Key ownerk, final String fname, final Date lastmod, final String sha1, final long size ) {
		super( ownerk, fname, lastmod, sha1, size );
		setV( 4 );
	}

	public void setComment( String comment ) {
	    this.comment = comment;
    }

	public String getComment() {
	    return comment;
    }
	
	public void setExt( String ext ) {
	    this.ext = ext;
    }

	public String getExt() {
	    return ext;
    }

}
