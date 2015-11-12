/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An object registry that can add and remove objects.<br>
 * The implementation is thread safe, and instantiates the internal structure on-demand.<br>
 * The {@link #add(Object)} and {@link #remove(Object)} methods are synchronized with the <code>this</code> reference.
 * 
 * @param <T> the type of objects to be handled
 * 
 * @author Andras Belicza
 */
public class ObjectRegistry< T > implements Iterable< T > {
	
	/** A list that stores the object. */
	private List< T > objectList;
	
	/**
	 * Adds an object to this registry.
	 * @param object object to be added
	 */
	public synchronized void add( final T object ) {
		if ( objectList == null )
			objectList = new ArrayList< T >();
		
		objectList.add( object );
	}
	
	/**
	 * Removes an object from this registry.<br>
	 * If the object is not in the registry, does nothing.
	 * @param object object to be removed
	 */
	public synchronized void remove( final T object ) {
		if ( objectList != null )
			objectList.remove( object );
	}
	
	/**
	 * Returns if this object registry is empty.
	 * @return true if this object registry is empty; false otherwise
	 */
	public boolean isEmpty() {
		return objectList == null ? true : objectList.isEmpty();
	}
	
	@Override
	public Iterator< T > iterator() {
		if ( objectList == null )
			return new Iterator< T >() {
				@Override
				public boolean hasNext() {
					return false;
				}
				@Override
				public T next() {
					return null;
				}
				@Override
				public void remove() {
				}
			};
		else
			return objectList.iterator();
	}
	
}
