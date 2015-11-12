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
 * Class representing a mouse print.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>v</code> property.
 * 			<br>Made {@link #compr} property unindexed.
 * 		<li>Added <code>blobKey</code>, <code>content</code> properties (as part of {@link FileMetaData}) to dispose of the <code>File</code> type.
 * 		<li>Made <code>fname</code>, <code>lastmod</code>, <code>size</code> properties (as part of {@link FileMetaData}) unindexed.
 * 			<br>Made {@link #ver}, {@link #start}, {@link #width}, {@link #height}, {@link #res}, {@link #time},
 * 			{@link #count}, {@link #udsize} properties unindexed.
 * 		<li>File <code>content</code> is now stored locally (as part of {@link FileMetaData}) if size is less than a specified limit (details at {@link FileServlet}).
 * 			<br>Made <code>blobKey</code>, <code>content</code> properties (as part of {@link FileMetaData}) unindexed.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class Smpd extends FileMetaData {
	
	/** Smpd file version. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private short ver;
	
	/** Recording start time. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private Date start;
	
	/** Recording end time. */
	@Persistent
	private Date end;
	
	/** Screen width in pixels. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int width;
	
	/** Screen height in pixels. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int height;
	
	/** Screen resolution in in dots/inch. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int res;
	
	/** Mouse sampling time in ms. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int time;
	
	/** Number of samples. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int count;
	
	/** Uncompressed data size. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int udsize;
	
	/** Compression used when saving. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int compr;
	
	/**
	 * Creates a new Smpd.
	 * @param ownerk  key of the owner account
	 * @param fname   name of the mouse print
	 * @param lastmod last modified date of the file
	 * @param sha1    SHA-1 digest of the content of the mouse print
	 * @param size    size of the file
	 */
	public Smpd( final Key ownerk, final String fname, final Date lastmod, final String sha1, final long size ) {
		super( ownerk, fname, lastmod, sha1, size );
		setV( 4 );
	}
	
    public short getVer() {
    	return ver;
    }
	
    public void setVer( short ver ) {
    	this.ver = ver;
    }
	
    public Date getStart() {
    	return start;
    }
	
    public void setStart( Date start ) {
    	this.start = start;
    }

    public Date getEnd() {
    	return end;
    }
	
    public void setEnd( Date end ) {
    	this.end = end;
    }
	
    public int getWidth() {
    	return width;
    }
	
    public void setWidth( int width ) {
    	this.width = width;
    }

    public int getHeight() {
    	return height;
    }
	
    public void setHeight( int height ) {
    	this.height = height;
    }
	
    public int getRes() {
    	return res;
    }
	
    public void setRes( int res ) {
    	this.res = res;
    }
	
    public int getTime() {
    	return time;
    }
	
    public void setTime( int time ) {
    	this.time = time;
    }
	
    public int getCount() {
    	return count;
    }
	
    public void setCount( int count ) {
    	this.count = count;
    }
	
    public int getUdsize() {
    	return udsize;
    }
	
    public void setUdsize( int udsize ) {
    	this.udsize = udsize;
    }
	
    public int getCompr() {
    	return compr;
    }
	
    public void setCompr( int compr ) {
    	this.compr = compr;
    }

}
