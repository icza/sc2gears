/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.icons;

import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;

import java.awt.Image;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Manages the different scaled instances of an ImageIcon.
 *  
 * @author Andras Belicza
 */
public class IconHandler {
	
	/**
	 * Null icon map in which icons do not draw anything.
	 */
	public static final Map< IconSize, Icon > NULL = new EnumMap< IconSize, Icon >( IconSize.class );
	static {
		for ( final IconSize size : IconSize.values() ) {
			final int pixelSize = 64 >> size.sizeShift;
			NULL.put( size, Icons.getNullIcon( pixelSize, pixelSize ) );
		}
	}
	
	/** Reference to the original icon. */
	private final ImageIcon originalIcon;
	
	/** Map from the size to the scaled instance. */
	private final Map< IconSize, ImageIcon > sizeIconMap;
	
	/**
	 * Creates a new IconHandler with the specified imageIcon.
	 * @param imageIcon reference to the original image icon (can be null)
	 */
	public IconHandler( final ImageIcon imageIcon ) {
		originalIcon = imageIcon;
		sizeIconMap  = imageIcon == null ? null : new EnumMap< IconSize, ImageIcon >( IconSize.class );
	}
	
	/**
	 * Returns the image icon scaled to the specified size.<br>
	 * Should not be called with HIDDEN size, behavior is undefined!
	 * 
	 * @param size size of the requested image icon
	 * @return the image icon scaled to the specified size
	 */
	public Icon get( final IconSize size ) {
		if ( originalIcon == null )
			return NULL.get( size );
		
		if ( size == IconSize.BIG )
			return originalIcon;
		
		ImageIcon scaled = sizeIconMap.get( size );
		if ( scaled == null )
			sizeIconMap.put( size, scaled = new ImageIcon( originalIcon.getImage().getScaledInstance( originalIcon.getIconWidth() >> size.sizeShift, originalIcon.getIconHeight() >> size.sizeShift, Image.SCALE_SMOOTH ) ) );
		
		return scaled;
	}
	
}
