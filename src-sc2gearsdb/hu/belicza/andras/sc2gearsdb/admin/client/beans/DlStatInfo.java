/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.admin.client.beans;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class DlStatInfo implements IsSerializable {
	
	private String fileName;
	private int count;
	private int javaClient;
	private int unique;
	private Date date;
	
	public void setFileName( String fileName ) {
	    this.fileName = fileName;
    }
	public String getFileName() {
	    return fileName;
    }
	public void setCount( int count ) {
	    this.count = count;
    }
	public int getCount() {
	    return count;
    }
	public void setJavaClient( int javaClient ) {
	    this.javaClient = javaClient;
    }
	public int getJavaClient() {
	    return javaClient;
    }
	public void setUnique( int unique ) {
	    this.unique = unique;
    }
	public int getUnique() {
	    return unique;
    }
	public void setDate( Date date ) {
	    this.date = date == null ? null : new Date( date.getTime() );
    }
	public Date getDate() {
	    return date;
    }
	
}
