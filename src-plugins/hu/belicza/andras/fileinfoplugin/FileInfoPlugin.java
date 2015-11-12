/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.fileinfoplugin;

import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.PluginDescriptor;
import hu.belicza.andras.sc2gearspluginapi.PluginServices;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayOpCallback;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayOpsPopupMenuItemListener;
import hu.belicza.andras.sc2gearspluginapi.impl.BasePlugin;

import java.io.File;
import java.util.Locale;

import javax.swing.ImageIcon;

/**
 * A test plugin that adds a new replay operations menu item which shows info of the selected replays in a dialog.
 * 
 * @author Andras Belicza
 */
public class FileInfoPlugin extends BasePlugin {
	
	/** Hander of the new replay ops popup menu item. */
	private Integer fileInfoItemHandler;
	
	@Override
	public void init( final PluginDescriptor pluginDescriptor, final PluginServices pluginServices, final GeneralServices generalServices ) {
		// Call the init() implementation of the BasePlugin:
		super.init( pluginDescriptor, pluginServices, generalServices );
		
		// Register the new replay ops popup menu item:
		final ImageIcon infoIcon = new ImageIcon( getClass().getResource( "information.png" ) );
		
		fileInfoItemHandler = generalServices.getCallbackApi().addReplayOpsPopupMenuItem( "Show selection info", infoIcon, new ReplayOpsPopupMenuItemListener() {
			@Override
			public void actionPerformed( final File[] files, final ReplayOpCallback replayOpCallback, final Integer handler ) {
				long totalSize = 0;
				
				for ( final File file : files )
					totalSize += file.length();
				
				generalServices.getGuiUtilsApi().showInfoDialog( new String[] {
						"Total selected replays: " + files.length,
						String.format( Locale.ENGLISH, "Total size: %,d Bytes", totalSize )
				} );
				
				// No need to call replayOpCallback here because we did not move, rename or delete replays
			}
		} );
	}
	
	@Override
	public void destroy() {
		// Remove the registered replay ops popup menu item
		generalServices.getCallbackApi().removeReplayOpsPopupMenuItem( fileInfoItemHandler );
	}
	
}
