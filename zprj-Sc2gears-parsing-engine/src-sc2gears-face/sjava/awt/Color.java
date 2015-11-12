/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package sjava.awt;

/**
 * java.awt.Color face.
 * 
 * @author Andras Belicza
 */
public class Color {
	
	private final int r;
	private final int g;
	private final int b;
	
    /**
     * Creates a new Color.
     */
    public Color( final int r, final int g, final int b ) {
    	this.r = r;
    	this.g = g;
    	this.b = b;
    }
    
    public int getRed() {
    	return r;
    }
    
    public int getGreen() {
    	return g;
    }
    
    public int getBlue() {
    	return b;
    }
    
}
