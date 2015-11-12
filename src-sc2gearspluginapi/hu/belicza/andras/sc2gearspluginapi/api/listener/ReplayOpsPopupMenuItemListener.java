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

import hu.belicza.andras.sc2gearspluginapi.api.CallbackApi;

import java.io.File;

/**
 * Defines a listener interface that can be notified when the associated replay operations popup menu item is activated.
 * 
 * @author Andras Belicza
 * 
 * @see CallbackApi
 */
public interface ReplayOpsPopupMenuItemListener {
	
	/**
	 * Called when the associated replay operations popup menu item is activated.
	 * @param files            replay files to perform the operation on
	 * @param replayOpCallback an optional replay operations callback; if provided, it must be notified if the implementation performs actions that the replay operations callback needs to be notified of
	 * @param handler          handler of the menu item that was activated
	 * @see ReplayOpCallback
	 */
	void actionPerformed( File[] files, ReplayOpCallback replayOpCallback, Integer handler );
	
}
