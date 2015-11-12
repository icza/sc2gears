/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.impl.util;

/**
 * Represents a pair of objects with generic types.
 * 
 * @param <T1> type of the first object
 * @param <T2> type of the second object
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 */
public class Pair< T1, T2 > {
	
	/** Reference to the first object.  */
	public final T1 value1;
	/** Reference to the second object. */
	public final T2 value2;
	
	/**
	 * Creates a new Pair.
	 * @param value1 reference to the first object
	 * @param value2 reference to the second object
	 */
	public Pair( final T1 value1, final T2 value2 ) {
		this.value1 = value1;
		this.value2 = value2;
	}
	
	/**
	 * Returns <code>true</code> if both values of the pairs in this object and in the other one are equal.
	 * <p>If a value is <code>null</code>, then the value in the other pair must also be <code>null</code>
	 * in order to be equal.</p>
	 */
	@Override
	public boolean equals( final Object o ) {
		if ( o == null || !( o instanceof Pair ) )
			return false;
		
		final Pair< ?, ? > pair2 = (Pair< ?, ? >) o;
		
		if ( value1 == null ^ pair2.value1 == null ) // If only one of the first values is null...
			return false;
		
		if ( value2 == null ^ pair2.value2 == null ) // If only one of the second values is null...
			return false;
		
		if ( value1 != null && !value1.equals( pair2.value1 ) )
			return false;
		
		if ( value2 != null && !value2.equals( pair2.value2 ) )
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return ( value1 == null ? 0 : value1.hashCode() ) ^ ( value2 == null ? 0 : value2.hashCode() );
	}
	
}
