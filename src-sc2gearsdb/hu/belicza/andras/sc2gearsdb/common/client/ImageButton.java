/*
/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.common.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;

/**
 * An image button.
 * 
 * @author Andras Belicza
 */
public class ImageButton extends Button {
	
	/**
	 * Creates a new ImageButton with a size of 16x16.
	 * @param imageName name of the button image file, relative to the <code>/images/fugue/</code> folder, example: <code>"arrow.png"</code>
	 * @param toolTip   optional tool tip of the button
	 */
	public ImageButton( final String imageName, final String toolTip ) {
		this( imageName, toolTip, 16, 16 );
	}
	
	/**
	 * Creates a new ImageButton.
	 * @param imageName name of the button image file, relative to the <code>/images/fugue/</code> folder, example: <code>"arrow.png"</code>
	 * @param toolTip   optional tool tip of the button
	 * @param width     width of the button in pixels
	 * @param height    height of the button in pixels
	 * @return the created image button
	 */
	public ImageButton( final String imageName, final String toolTip, final int width, final int height ) {
		if ( toolTip != null )
			setTitle( toolTip );
		
		// TODO image should be grayed out when button is disabled!
		setPixelSize( width, height );
		setImage( imageName );
		
		DOM.setStyleAttribute( getElement(), "border", "0" );
		DOM.setStyleAttribute( getElement(), "padding", "0px" );
		DOM.setStyleAttribute( getElement(), "margin", "0px" );
		
		addStyleName( "pointerMouse" );
	}
	
	/**
	 * Sets the image of this image button.
	 * @param imageName name of the button image file, relative to the <code>/images/fugue/</code> folder, example: <code>"arrow.png"</code>
	 */
	public void setImage( final String imageName ) {
		DOM.setStyleAttribute( getElement(), "background", "transparent url('/images/fugue/" + imageName + "')" );
	}
	
}
