/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */

package hu.belicza.andras.sc2gears.services.streaming;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Screenshot producer.
 * 
 * @author Andras Belicza
 */
public abstract class ScreenshotProducer {
	
	/** Screen area to create screen capture from. */
	protected final Rectangle screenArea;
	
    /**
     * Creates a new ScreenshotProducer.
     * @param screenArea screen area to create screenshot from
     */
    public ScreenshotProducer( final Rectangle screenArea ) {
    	this.screenArea = screenArea;
    }
    
    /**
     * Returns a screenshot.<br>
     * The returned {@link BufferedImage} may be reused for future called by the implementation. 
     * @return a screenshot
     */
    public abstract BufferedImage getScreenshot();
    
    /**
     * Closes this screenshot producer, releases resources allocated and reserved for this screenshot provider.<br>
     * This is an empty implementation. 
     */
    public void close() {
    }
    
}
