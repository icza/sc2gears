/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.util;

import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PageInfo;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;

/**
 * JDO Query Builder
 * 
 * <T> type of the queried entity
 * 
 * @author Andras Belicza
 */
public class JQBuilder< T > {
	
	public static final String KEY_TYPE  = Key.class.getName();
	public static final String USER_TYPE = User.class.getName();
	
	public Query q;
	
	/**
	 * Creates a new query builder.
	 * @return this for chaining
	 */
	public JQBuilder( final PersistenceManager pm, final Class< T > c ) {
		q = pm.newQuery( c );
	}
	
	/**
	 * Sets the query filter and the declared parameters.
	 * @return this for chaining
	 */
	public JQBuilder< T > filter( final String f, final String p ) {
		q.setFilter( f );
		if ( p != null )
			q.declareParameters( p.replace( "KEY", KEY_TYPE ).replace( "DATE", "java.util.Date" ).replace( "USER", USER_TYPE ) );
		return this;
	}
	
	/**
	 * Sets the query sorting property, ascending.
	 * @return this for chaining
	 */
	public JQBuilder< T > asc( final String s ) {
		q.setOrdering( s );
		return this;
	}
	
	/**
	 * Sets the query sorting property, descending.
	 * @return this for chaining
	 */
	public JQBuilder< T > desc( final String s ) {
		q.setOrdering( s + " desc" );
		return this;
	}
	
	/**
	 * Sets the query range.
	 * @return this for chaining
	 */
	public JQBuilder< T > range( final long first, final long last ) {
		q.setRange( first, last );
		return this;
	}
	
	/**
	 * Sets the query range.
	 * @return this for chaining
	 */
	public JQBuilder< T > cursor( final List< ? > lastQueryResult ) {
		ServerUtils.setQueryCursor( q, lastQueryResult );
		return this;
	}
	
	/**
	 * Sets the page info to the query.
	 * @return this for chaining
	 */
	public JQBuilder< T > pageInfo( final PageInfo pageInfo ) {
		ServerUtils.setQueryPageInfo( q, pageInfo);
		return this;
	}
	
	/**
	 * Executes the query and returns the result list.
	 * @return the query result
	 */
	@SuppressWarnings("unchecked")
	public List< T > get() {
		return (List<T>) q.execute();
	}
	
	/**
	 * Executes the query and returns the result list.
	 * @return the query result
	 */
	@SuppressWarnings("unchecked")
	public List< T > get( final Object p1 ) {
		return (List<T>) q.execute( p1 );
	}
	
	/**
	 * Executes the query and returns the result list.
	 * @return the query result
	 */
	@SuppressWarnings("unchecked")
	public List< T > get( final Object p1, final Object p2 ) {
		return (List<T>) q.execute( p1, p2 );
	}
	
	/**
	 * Executes the query and returns the result list.
	 * @return the query result
	 */
	@SuppressWarnings("unchecked")
	public List< T > get( final Object p1, final Object p2, final Object p3 ) {
		return (List<T>) q.execute( p1, p2, p3 );
	}
	
	/**
	 * Executes the query and returns the result list.
	 * @return the query result
	 */
	@SuppressWarnings("unchecked")
	public List< T > get( final Object... ps ) {
		return (List<T>) q.executeWithArray( ps );
	}
	
}
