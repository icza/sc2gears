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

/**
 * The NullAwareComparable allows either of the comparable objects to be null with the following behavior:
 * <ul><li>if both objects are <code>null</code>s, <code>compareTo()</code> returns 0
 * <li><code>null</code> "objects" are smaller than non-nulls
 * <li>if both objects are non-nulls, their <code>compareTo()</code> will be called</ul>
 * 
 * @author Andras Belicza
 * 
 * @param < T > type of the comparable objects
 */
public class NullAwareComparable< T extends Comparable< T > > implements Comparable< NullAwareComparable< T > > {
	
	/** Reference to the comparable object. */
	public final T value;
	
	/**
	 * Creates a new NullAwareComparable.
	 * @param value reference to the comparable object
	 */
	public NullAwareComparable( final T value ) {
		this.value = value;
	}

	@Override
	public int compareTo( final NullAwareComparable< T > otherNAC ) {
		if ( value == null && otherNAC.value == null )
			return 0;
		else if ( value == null && otherNAC.value != null )
			return -1;
		else if ( value != null && otherNAC.value == null )
			return 1;
		return value.compareTo( otherNAC.value );
	}
	
	/**
	 * Returns a NullAwareComparable that toString() formats the value as percent.
	 * @param percent the percent value
	 * @return a NullAwareComparable that toString() formats the value as percent
	 */
	public static NullAwareComparable< Integer > getPercent( final Integer percent ) {
		// If all is unknown, wins+losses=0!
		return new NullAwareComparable< Integer >( percent ) {
			@Override
			public String toString() {
				return value == null ? "-" : value + "%";
			}
		};
	}
	
}
