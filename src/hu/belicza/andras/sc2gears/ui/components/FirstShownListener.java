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

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;

/**
 * A {@link ComponentListener} which can be used to detect when a component is first shown.<br>
 * 
 * A typical use of this is to implement deferred content building.
 * 
 * @author Andras Belicza
 */
public abstract class FirstShownListener extends ComponentAdapter {
	
	/** Tells if the component was already shown. */
	private boolean shown;
	
	@Override
	public void componentShown( final ComponentEvent event ) {
		if ( !shown) {
			shown = true;
			firstShown( event );
			( (JComponent) event.getSource() ).validate();
		}
	}
	
	/**
	 * Called when the component is first shown.
	 * @param event the component event
	 */
	public abstract void firstShown( ComponentEvent event );
	
}
