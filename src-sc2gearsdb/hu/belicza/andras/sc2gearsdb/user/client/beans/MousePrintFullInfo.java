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
public class MousePrintFullInfo extends MousePrintInfo {
	
	private Date uploadedDate;
	private int samplingTime;
	private Date fileLastModified;
	private short version;
	private int screenResolution;
	private int uncompressedDataSize;
	private int savedWithCompression;
	
	public Date getUploadedDate() {
		return uploadedDate;
	}
	public void setUploadedDate(Date uploadedDate) {
		this.uploadedDate = uploadedDate == null ? null : new Date( uploadedDate.getTime() );
	}
	public Date getFileLastModified() {
		return fileLastModified;
	}
	public void setFileLastModified(Date fileLastModified) {
		this.fileLastModified = fileLastModified == null ? null : new Date( fileLastModified.getTime() );
	}
	public short getVersion() {
		return version;
	}
	public void setVersion(short version) {
		this.version = version;
	}
	public int getScreenResolution() {
		return screenResolution;
	}
	public void setScreenResolution(int screenResolution) {
		this.screenResolution = screenResolution;
	}
	public int getUncompressedDataSize() {
		return uncompressedDataSize;
	}
	public void setUncompressedDataSize(int uncompressedDataSize) {
		this.uncompressedDataSize = uncompressedDataSize;
	}
	public int getSavedWithCompression() {
		return savedWithCompression;
	}
	public void setSavedWithCompression(int savedWithCompression) {
		this.savedWithCompression = savedWithCompression;
	}
	public void setSamplingTime(int samplingTime) {
		this.samplingTime = samplingTime;
	}
	public int getSamplingTime() {
		return samplingTime;
	}
	
}
