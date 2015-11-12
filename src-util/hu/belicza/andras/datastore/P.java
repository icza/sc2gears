/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.datastore;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Example Person entity class.
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class P extends _ClientObject {
	
	/**
	 * Version history and changes:
	 * <ol>
	 * 	<li value="2">Added the birthday ("bd") property
	 * 	<li value="1">Initial version
	 * </ol>
	 */
    @Override
    public int getCurrentVersion() {
	    return 2;
    }
	
	/** Name of the person. */
	@Persistent
	private String n;
	
	/** Birthday of the person. */
	@Persistent
	private Date bd;
	
	public void setN( final String n ) {
		this.n = n;
	}
	
	public String getN() {
		return n;
	}

	public void setBd( Date bd ) {
	    this.bd = bd;
    }

	public Date getBd() {
	    return bd;
    }
	
}
