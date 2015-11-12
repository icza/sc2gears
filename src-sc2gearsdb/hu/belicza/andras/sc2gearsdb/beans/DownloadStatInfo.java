/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.beans;

import java.io.Serializable;

/**
 * Download Stat bean class.
 * 
 * @author Andras Belicza
 */
public class DownloadStatInfo implements Serializable {
	
	private static final long serialVersionUID = -7795953931888078830L;

	private int persistingCycleCounter;
	
	private int count;
	private int javaClient;
	private int unique;
	
	public void incrementPersistingCycleCounter() {
		persistingCycleCounter++;
	}
	
	public void incrementCount() {
		count++;
	}
	
	public void incrementJavaClient() {
		javaClient++;
	}
	
	public void incrementUnique() {
		unique++;
	}
	
	public void setPersistingCycleCounter( int persistingCycleCounter ) {
		this.persistingCycleCounter = persistingCycleCounter;
	}
	
	public int getPersistingCycleCounter() {
		return persistingCycleCounter;
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
	
}
