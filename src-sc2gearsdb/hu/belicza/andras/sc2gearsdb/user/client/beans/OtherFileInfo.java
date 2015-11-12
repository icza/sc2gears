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
public class OtherFileInfo implements IsSerializable {
	
	private Date uploaded;
	private String fileName;
	private long size;
	private Date lastModified;
	private String comment;
	private String sha1;
	
	public void setUploaded( Date uploaded ) {
	    this.uploaded = uploaded == null ? null : new Date( uploaded.getTime() );
    }
	public Date getUploaded() {
	    return uploaded;
    }
	public void setFileName( String fileName ) {
	    this.fileName = fileName;
    }
	public String getFileName() {
	    return fileName;
    }
	public void setSize( long size ) {
	    this.size = size;
    }
	public long getSize() {
	    return size;
    }
	public void setLastModified( Date lastModified ) {
	    this.lastModified = lastModified == null ? null : new Date( lastModified.getTime() );
    }
	public Date getLastModified() {
	    return lastModified;
    }
	public void setComment( String comment ) {
		this.comment = comment;
	}
	public String getComment() {
		return comment;
	}
	public void setSha1( String sha1 ) {
	    this.sha1 = sha1;
    }
	public String getSha1() {
	    return sha1;
    }
	
}
