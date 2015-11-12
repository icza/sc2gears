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

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.servlet.http.HttpServletRequest;

/**
 * A Datastore object which has an associated HTTP client.
 * 
 * <p>This can be used to store the HTTP client information who resulted this entity to be created.</p>
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
public abstract class _ClientObject extends _DatastoreObject {
	
	/** Client IP address. */
	@Persistent
	private String ip;
	
	/** Client user agent string. */
	@Persistent
	private String ua;
	
	/**
	 * Location of the client.
	 * 
	 * <p>It has the form of <code>"country;region;city"</code> where:
	 * <ul>
	 * 	<li><code>"country"</code> is the <a href="http://en.wikipedia.org/wiki/ISO_3166-1_alpha-2">ISO 3166-1 alpha-2</a> country code as reported by AppEngine in the <code>X-AppEngine-Country</code> HTTP header field
	 * 	<li><code>"region"</code> is the <a href="http://en.wikipedia.org/wiki/ISO_3166-2">ISO 3166-2</a> country specific region code as reported by AppEngine in the <code>X-AppEngine-Region</code> HTTP header field
	 * 	<li><code>"city"</code> is the city as reported by AppEngine in the <code>X-AppEngine-City</code> HTTP header field
	 * </ul>
	 * </p>
	 */
	@Persistent
	private String lo;
	
	/**
	 * Creates a new _ClientObject.
	 */
	public _ClientObject() {
	}
	
	/**
	 * Creates a new _ClientObject.
	 * The HTTP client info of the new object will be automatically filled from the specified HTTP request.
	 * @param request HTTP request to fill the associated HTTP client from
	 */
	public _ClientObject( final HttpServletRequest request ) {
		fillFromRequest( request );
	}
	
	public void setIp( final String ip ) {
		this.ip = ip;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setUa( final String ua ) {
		this.ua = ua;
	}
	
	public String getUa() {
		return ua;
	}
	
	public void setLo( final String lo ) {
		this.lo = lo;
	}
	
	public String getLo() {
		return lo;
	}
	
	/**
	 * Fills the associated HTTP client info from the specified HTTP request.
	 * @param request HTTP request to fill the associated HTTP client from
	 */
	public void fillFromRequest( final HttpServletRequest request ) {
		ip = request.getRemoteAddr();
		
		ua = cutString( request.getHeader( "User-Agent" ) );
		
		final StringBuilder locBuilder = new StringBuilder();
		String s;
		if ( ( s = request.getHeader( "X-AppEngine-Country" ) ) != null )
			locBuilder.append( s );
		locBuilder.append( ';' );
		if ( ( s = request.getHeader( "X-AppEngine-Region" ) ) != null )
			locBuilder.append( s );
		locBuilder.append( ';' );
		if ( ( s = request.getHeader( "X-AppEngine-City" ) ) != null )
			locBuilder.append( s );
		lo = locBuilder.toString();
	}
	
}
