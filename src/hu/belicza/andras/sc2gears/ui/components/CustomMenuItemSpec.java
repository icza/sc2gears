/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.components;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Icon;

/**
 * A custom menu item specification that has a handler,
 * which is also used to implement the {@link #equals(Object)} and {@link #hashCode()} functions.
 * 
 * @author Andras Belicza
 */
class CustomMenuItemSpec {
	
	/** Used to generate unique handlers. */
	private static AtomicInteger handlerGenerator = new AtomicInteger();
	
	/** Handler of the custom menu item spec.       */
	public final Integer handler;
	/** Text of the custom menu item spec.          */
	public final String text;
	/** Optional icon of the custom menu item spec. */
	public final Icon icon;
	
	/**
	 * Creates a new CustomMenuItemSpec.
	 * @param text    text of the custom menu item spec
	 * @param icon    optional icon of the custom menu item spec
	 */
	public CustomMenuItemSpec( final String text, final Icon icon ) {
		this.handler = handlerGenerator.incrementAndGet();
		this.text    = text;
		this.icon    = icon;
	}
	
	/**
	 * 2 custom menu items will be declared equals if their handlers are equal.
	 */
	@Override
	public boolean equals( final Object obj ) {
		if ( !( obj instanceof CustomMenuItemSpec ) )
			return false;
		
		return handler.intValue() == ( (CustomMenuItemSpec) obj ).handler.intValue();
	}
	
	/**
	 * Returns the handler as the hash code.
	 */
	@Override
	public int hashCode() {
		return handler.intValue();
	}
	
}
