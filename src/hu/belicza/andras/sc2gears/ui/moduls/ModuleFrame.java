/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls;

import javax.swing.JInternalFrame;

/**
 * Provides a common frame for modules.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class ModuleFrame extends JInternalFrame {
	
	/**
	 * Creates a new ModulFrame.
	 * @param title title of the frame
	 */
	public ModuleFrame( final String title ) {
		super( title, true, true, true, true );
		setBounds( 10, 10, 500, 300 );
	}
	
	@Override
	public String toString() {
		return getTitle();
	}
	
}
