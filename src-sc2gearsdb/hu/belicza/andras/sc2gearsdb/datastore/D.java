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

import hu.belicza.andras.sc2gearsdb.util.ServerUtils;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 * Class representing a download.
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class D {
	
	/** Key of the account. */
	@PrimaryKey
	@Persistent( valueStrategy = IdGeneratorStrategy.IDENTITY )
	protected Key k;
	
	/** Object creation date. */
	@Persistent
	protected Date d;
	
	/** Requested file. */
	@Persistent
	private String f;
	
	/** Client IP address. */
	@Persistent
	private String i;
	
	/** Client user agent. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String u;
	
	/**
	 * Client location. See {@link ServerUtils#getLocationString(javax.servlet.http.HttpServletRequest)} for format details.
	 * <p><i>Note: this field is missing from the oldest entities, and it contains only the country in some old entities
	 * (in the form of <code>"country"</code>). Region and city were added on 2012-04-25.</i></p>
	 */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String c;
	
	/**
	 * Creates a new D.
	 */
	public D() {
		d = new Date();
	}

	public Key getK() {
		return k;
	}

	public void setK(Key k) {
		this.k = k;
	}

	public Date getD() {
		return d;
	}

	public String getF() {
		return f;
	}

	public void setF(String f) {
		this.f = f;
	}

	public String getI() {
		return i;
	}

	public void setI(String i) {
		this.i = i;
	}

	public String getU() {
		return u;
	}

	public void setU(String u) {
		this.u = u;
	}

	public void setC( String c ) {
	    this.c = c;
    }

	public String getC() {
	    return c;
    }
	
}
