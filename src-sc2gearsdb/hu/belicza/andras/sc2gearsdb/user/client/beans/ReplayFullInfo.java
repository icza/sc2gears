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

/**
 * 
 * @author Andras Belicza
 */
public class ReplayFullInfo extends ReplayInfo {
	
	private Date uploadedDate;
	private long fileSize;
	private Date fileLastModified;
	
	public Date getUploadedDate() {
		return uploadedDate;
	}
	public void setUploadedDate(Date uploadedDate) {
		this.uploadedDate =  uploadedDate == null ? null : new Date( uploadedDate.getTime() );
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public Date getFileLastModified() {
		return fileLastModified;
	}
	public void setFileLastModified(Date fileLastModified) {
		this.fileLastModified = fileLastModified == null ? null : new Date( fileLastModified.getTime() );
	}
	
}
