/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.listener;

import hu.belicza.andras.sc2gearspluginapi.api.ProfileApi;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerId;

import javax.swing.ImageIcon;

/**
 * A custom portrait listener for asynchronous custom portrait requests.
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see ProfileApi#queryCustomPortrait(IPlayerId, boolean, CustomPortraitListener)
 */
public interface CustomPortraitListener {
	
	/**
	 * Called when a custom portrait is ready.
	 * @param customPortrait image icon of the custom portrait
	 */
	void customPortraitReady( ImageIcon customPortrait );
	
}
