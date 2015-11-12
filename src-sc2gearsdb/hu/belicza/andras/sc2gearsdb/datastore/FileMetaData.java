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

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;

/**
 * A class that stores meta data of a file.
 * 
 * Primarily this is just the representation of the file, its content is stored in the Blobstore,
 * and the blob key is stored here.
 * 
 * <p>If Blobstore was not available when needed to store the file, the content is stored here temporarily.
 * In these cases <code>blobKey=null</code> and <code>content</code> holds the file content.<br>
 * These are to be placed in the Blobstore, it is timed and processed by a cron job.</p>
 * 
 * <p><i>This is part of the double-caching mechanism I developed because the Blobstore is nowhere near reliable
 * at the moment (2012-03-08). On the doc page it is also stated that the file API is Use is experimental
 * (<a href="http://code.google.com/appengine/docs/java/blobstore/overview.html">Blobstore overview</a>).</i></p>
 * 
 * <p>The {@link InheritanceStrategy#SUBCLASS_TABLE} strategy will tell to store properties of this class
 * on the tables of the subclasses.</p>
 * 
 * <p>This class is abstract because we do not want to save instances of this class.</p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
@Inheritance( strategy = InheritanceStrategy.SUBCLASS_TABLE )
public abstract class FileMetaData extends DataStoreObject {
	
	/** Key of the owner account. */
	@Persistent
	private Key ownerk;
	
	/** Name of the file. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String fname;
	
	/** Last modified date of the file. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private Date lastmod;
	
	/** SHA-1 digest of the content of the file. */
	@Persistent
	private String sha1;
	
	/** Size of the file in bytes. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long size;
	
	/** Blob key of the content of the file in the Blobstore. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private BlobKey blobKey;
	
	/** Content of the file if it failed to store it in the Blobstore. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private Blob content;
	
	/**
	 * Creates a new FileMetaData.
	 * @param ownerk  key of the owner account
	 * @param fname   name of the file
	 * @param lastmod last modified date of the file
	 * @param sha1    SHA-1 digest of the content of the file
	 * @param size    size of the file
	 */
	public FileMetaData( final Key ownerk, final String fname, final Date lastmod, final String sha1, final long size ) {
		this.ownerk  = ownerk;
		this.fname   = fname;
		this.lastmod = lastmod;
		this.sha1    = sha1;
		this.size    = size;
	}
	
    public Key getOwnerk() {
    	return ownerk;
    }
	
    public void setOwnerk( Key ownerk ) {
    	this.ownerk = ownerk;
    }
	
    public String getFname() {
    	return fname;
    }
	
    public void setFname( String fname ) {
    	this.fname = fname;
    }
	
    public Date getLastmod() {
    	return lastmod;
    }
	
    public void setLastmod( Date lastmod ) {
    	this.lastmod = lastmod;
    }
	
    public String getSha1() {
    	return sha1;
    }
	
    public void setSha1( String sha1 ) {
    	this.sha1 = sha1;
    }
	
    public long getSize() {
    	return size;
    }

    public void setSize( long size ) {
    	this.size = size;
    }

	public void setBlobKey( BlobKey blobKey ) {
	    this.blobKey = blobKey;
    }

	public BlobKey getBlobKey() {
	    return blobKey;
    }

	public void setContent( Blob content ) {
	    this.content = content;
    }

	public Blob getContent() {
	    return content;
    }

}
