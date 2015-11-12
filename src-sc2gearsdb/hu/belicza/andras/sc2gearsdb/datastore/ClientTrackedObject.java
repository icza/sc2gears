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

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.servlet.http.HttpServletRequest;

/**
 * A Datastore object which tracks the client.
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
public abstract class ClientTrackedObject extends DataStoreObject {
	
	/** Client IP address. */
	@Persistent
	private String ip;
	
	/** Client user agent. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String userAgent;
	
	/**
	 * Client location. See {@link ServerUtils#getLocationString(javax.servlet.http.HttpServletRequest)} for format details.
	 * <p><i>Note: this field is missing from the oldest entities, and it contains only the country in some old entities
	 * (in the form of <code>"country"</code>). Region and city were added on 2012-04-25.</i></p>
	 */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String country;
	
	/**
	 * Fills the tracking properties from the specified HTTP request.
	 * @param request HTTP request to fill the tracking properties from
	 */
	public void fillTracking( final HttpServletRequest request ) {
		ip        = request.getRemoteAddr();
		userAgent = ServerUtils.checkUserAgent( request.getHeader( "User-Agent" ) );
		country   = ServerUtils.getLocationString( request );
	}
	
	/**
	 * Returns the <a href="http://en.wikipedia.org/wiki/ISO_3166-1_alpha-2">ISO 3166-1 alpha-2</a> country code part of the {@link #country} field.
	 * @return the country code part if available; <code>null</code> otherwise
	 */
	public String getCountryCode() {
		if ( country == null )
			return null;
		
		final String[] parts = country.split( ";" );
		
		return parts.length > 0 ? parts[ 0 ] : null;
	}
	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
    }

	public String getUserAgent() {
	    return userAgent;
    }
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public void setCountry( String country ) {
	    this.country = country;
    }

	public String getCountry() {
	    return country;
    }

}
