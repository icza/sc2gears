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
public class MousePrintInfo implements IsSerializable {
	
	private Date recordingStart;
	private Date recordingEnd;
	private int screenWidth;
	private int screenHeight;
	private long fileSize;
	private int samplesCount;
	private String sha1;
	private String fileName;
	
	public Date getRecordingStart() {
		return recordingStart;
	}
	public void setRecordingStart(Date recordingStart) {
		this.recordingStart = recordingStart == null ? null : new Date( recordingStart.getTime() );
	}
	public Date getRecordingEnd() {
		return recordingEnd;
	}
	public void setRecordingEnd(Date recordingEnd) {
		this.recordingEnd = recordingEnd == null ? null : new Date( recordingEnd.getTime() );
	}
	public int getScreenWidth() {
		return screenWidth;
	}
	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}
	public int getScreenHeight() {
		return screenHeight;
	}
	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}
	public int getSamplesCount() {
		return samplesCount;
	}
	public void setSamplesCount(int samplesCount) {
		this.samplesCount = samplesCount;
	}
	public String getSha1() {
		return sha1;
	}
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileName() {
		return fileName;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	
}
