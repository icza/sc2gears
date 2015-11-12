/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api;

import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.Plugin;
import hu.belicza.andras.sc2gearspluginapi.PluginDescriptor;
import hu.belicza.andras.sc2gearspluginapi.PluginServices;
import hu.belicza.andras.sc2gearspluginapi.api.listener.DiagnosticTestFactory;
import hu.belicza.andras.sc2gearspluginapi.api.listener.GameStatusListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.PlayerPopupMenuItemListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayAutosaveListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayOpsPopupMenuItemListener;
import hu.belicza.andras.sc2gearspluginapi.impl.Hotkey;

import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Defines callback services.
 * 
 * <p>This feature makes it possible for plugins to be notified when certain events occur.</p>
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GeneralServices
 */
public interface CallbackApi {
	
	/** Interface version. */
	String VERSION = "2.9";
	
	/**
	 * Adds a {@link ReplayAutosaveListener}.
	 * 
	 * <p>Every added listener must be removed by the plugin with {@link #removeReplayAutosaveListener(ReplayAutosaveListener)}
	 * before the plugin is destroyed (in the {@link Plugin#destroy()} for example).</p>
	 * 
	 * @param replayAutosaveListener replay auto-save listener to be added
	 * 
	 * @since "2.7"
	 * 
	 * @see #removeReplayAutosaveListener(ReplayAutosaveListener)
	 * @see ReplayAutosaveListener
	 */
    void addReplayAutosaveListener( ReplayAutosaveListener replayAutosaveListener );
	
	/**
	 * Removes a {@link ReplayAutosaveListener}.
	 * 
	 * @param replayAutosaveListener replay auto-save listener to be removed
	 * 
	 * @since "2.7"
	 * 
	 * @see #addReplayAutosaveListener(ReplayAutosaveListener)
	 * @see ReplayAutosaveListener
	 */
	void removeReplayAutosaveListener( ReplayAutosaveListener replayAutosaveListener );
	
	/**
	 * Adds a {@link GameStatusListener}.
	 * 
	 * <p>Every added listener must be removed by the plugin with {@link #removeGameStatusListener(GameStatusListener)}
	 * before the plugin is destroyed (in the {@link Plugin#destroy()} for example).</p>
	 * 
	 * @param gameStatusListener game status listener to be added
	 * 
	 * @see #removeGameStatusListener(GameStatusListener)
	 * @see GameStatusListener
	 */
	void addGameStatusListener( GameStatusListener gameStatusListener );
	
	/**
	 * Removes a {@link GameStatusListener}.
	 * 
	 * @param gameStatusListener game status listener to be removed
	 * 
	 * @see #addGameStatusListener(GameStatusListener)
	 * @see GameStatusListener
	 */
	void removeGameStatusListener( GameStatusListener gameStatusListener );
	
	/**
	 * Adds a new menu item to the player popup menu.
	 * 
	 * <p>Every added listener must be removed by the plugin with {@link #removePlayerPopupMenuItem(Integer)}
	 * before the plugin is destroyed (in the {@link Plugin#destroy()} for example).</p>
	 * 
	 * @param text     text of the new menu item
	 * @param icon     optional icon of the new menu item
	 * @param listener listener to be called when the menu item is activated
	 * 
	 * @return a handler that can be used to remove the registered menu item
	 * 
	 * @see #removePlayerPopupMenuItem(Integer)
	 * @see PlayerPopupMenuItemListener
	 */
	Integer addPlayerPopupMenuItem( String text, Icon icon, PlayerPopupMenuItemListener listener );
	
	/**
	 * Removes a player popup menu item specified by its handler.
	 * 
	 * @param handler handler of the popup menu item to be removed
	 * 
	 * @see #addPlayerPopupMenuItem(String, Icon, PlayerPopupMenuItemListener)
	 * @see PlayerPopupMenuItemListener
	 */
	void removePlayerPopupMenuItem( Integer handler );
	
	/**
	 * Adds a new menu item to the replay operations popup menu.
	 * 
	 * <p>Every added listener must be removed by the plugin with {@link #removeReplayOpsPopupMenuItem(Integer)}
	 * before the plugin is destroyed (in the {@link Plugin#destroy()} for example).</p>
	 * 
	 * @param text     text of the new menu item
	 * @param icon     optional icon of the new menu item
	 * @param listener listener to be called when the menu item is activated
	 * 
	 * @return a handler that can be used to remove the registered menu item
	 * 
	 * @see #removeReplayOpsPopupMenuItem(Integer)
	 * @see ReplayOpsPopupMenuItemListener
	 */
	Integer addReplayOpsPopupMenuItem( String text, Icon icon, ReplayOpsPopupMenuItemListener listener );
	
	/**
	 * Removes a replay operations popup menu item specified by its handler.
	 * 
	 * @param handler handler of the popup menu item to be removed
	 * 
	 * @see #addReplayOpsPopupMenuItem(String, Icon, ReplayOpsPopupMenuItemListener)
	 * @see ReplayOpsPopupMenuItemListener
	 */
	void removeReplayOpsPopupMenuItem( Integer handler );
	
	/**
	 * Adds a global (system-wide) hotkey.
	 * 
	 * <p>Adding/removing hotkeys after the plugin has been started costs a little overhead.
	 * It's recommended to add all global hotkeys when the plugin is initialized
	 * in the {@link Plugin#init(PluginDescriptor, PluginServices, GeneralServices)}.</p>
	 * 
	 * <p>Every added hotkey must be removed by the plugin with {@link #removeGlobalHotkey(Hotkey)}
	 * before the plugin is destroyed (in the {@link Plugin#destroy()} for example).</p>
	 * 
	 * @param hotkey hotkey to be added
	 * 
	 * @see #removeGlobalHotkey(Hotkey)
	 * @see Hotkey
	 */
	void addGlobalHotkey( Hotkey hotkey );
	
	/**
	 * Removes a global (system-wide) hotkey.
	 * 
	 * @param hotkey hotkey to be removed
	 * @see #addGlobalHotkey(Hotkey)
	 * @see Hotkey
	 */
	void removeGlobalHotkey( Hotkey hotkey );
	
	/**
	 * Adds a diagnostic test factory whose diagnostic tests will be executed by the Diagnostic tool.
	 * 
	 * <p>Every added diagnostic test factories must be removed by the plugin with {@link #removeDiagnosticTestFactory(DiagnosticTestFactory)}
	 * before the plugin is destroyed (in the {@link Plugin#destroy()} for example).</p>
	 * 
	 * @param diagnosticTestFactory diagnostic test factory to be added
	 * 
	 * @since "2.0"
	 * 
	 * @see #removeDiagnosticTestFactory(DiagnosticTestFactory)
	 * @see DiagnosticTestFactory
	 */
	void addDiagnosticTestFactory( DiagnosticTestFactory diagnosticTestFactory );
	
	/**
	 * Removes a diagnostic test factory.
	 * 
	 * @param diagnosticTestFactory diagnostic test factory to be removed
	 * 
	 * @since "2.0"
	 * 
	 * @see #addDiagnosticTestFactory(DiagnosticTestFactory)
	 * @see DiagnosticTestFactory
	 */
	void removeDiagnosticTestFactory( DiagnosticTestFactory diagnosticTestFactory );
	
	/**
	 * Adds the specified menu item to the Plugins menu of Sc2gears.
	 * 
	 * <p>The menu item can be a single {@link JMenuItem} or any subclass, a {@link JMenu} for example which can be used
	 * to add a sub-menu if the plugin has multiple menu items.</p>
	 * 
	 * <p>Every added menu items must be removed by the plugin with {@link #removeMenuItemFromPluginsMenu(JMenuItem)}
	 * before the plugin is destroyed (in the {@link Plugin#destroy()} for example).</p>
	 * 
	 * @param menuItem menu item to be added
	 * 
	 * @since "2.0"
	 * 
	 * @see #removeMenuItemFromPluginsMenu(JMenuItem)
	 */
	void addMenuItemToPluginsMenu( JMenuItem menuItem );
	
	/**
	 * Removes the specified menu item from the Plugins menu of Sc2gears.
	 * 
	 * @param menuItem menu item to be added
	 * 
	 * @since "2.0"
	 * 
	 * @see #addMenuItemToPluginsMenu(JMenuItem)
	 */
	void removeMenuItemFromPluginsMenu( JMenuItem menuItem );
	
	/**
	 * Creates and adds a new {@link JInternalFrame} to the main frame.
	 * 
	 * <p>The new internal frame will be automatically listed in the navigation tree under the Plugins node.
	 * When the internal frame is closed, it will be automatically removed from the navigation tree.</p>
	 * 
	 * <p>The returned internal frame should be closed by the plugin when the plugin is destroyed.</p>
	 * 
	 * @param title title of the internal frame to be created
	 * 
	 * @return a new internal frame that was created and added to the main frame
	 * 
	 * @since "2.9"
	 */
	JInternalFrame createAndAddInternalFrame( String title );
	
}
