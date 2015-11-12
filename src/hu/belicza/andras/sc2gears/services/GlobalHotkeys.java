/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.services;

import hu.belicza.andras.sc2gears.util.ControlledThread;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gearspluginapi.impl.Hotkey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.win32.StdCallLibrary;

/**
 * Utility class to setup or remove global hotkeys.<br>
 * 
 * Works only on Windows.
 * 
 * http://hanskeys.svn.sourceforge.net/viewvc/hanskeys/src/org/hanstool/hanskeys/HotkeyManager_win.java?revision=8&view=markup
 * http://stackoverflow.com/questions/696170/java-keylistener-without-window-being-open/4394398#4394398
 * 
 * @author Andras Belicza
 */
public class GlobalHotkeys extends ControlledThread {
	
	/** Tells if Global hotkeys are supported on the current machine. */
	public static final boolean supported = GeneralUtils.isWindows();
	
	/**
	 * Creates a GlobalHotkeys.
	 */
	private GlobalHotkeys() {
		super( "Global hotkey listener" );
	}
	
	/** List of global hotkeys.                     */
	private static final List< Hotkey >         hotkeyList          = new ArrayList< Hotkey >();
	/** Map of the successfully registered hotkeys. */
	private static final Map< Integer, Hotkey > registeredHotkeyMap = new HashMap< Integer, Hotkey >();
	
	/**
	 * Adds a hotkey to the global hotkey list.
	 * @param hotkey hotkey to be added
	 */
	public static synchronized void addHotkey( final Hotkey hotkey ) {
		final boolean active = isActive();
		if ( active )
			deactivate();
		
		hotkeyList.add( hotkey );
		
		if ( active )
			activate();
	}
	
	/**
	 * Removes a hotkey from the global hotkey list.
	 * @param hotkey hotkey to be removed
	 */
	public static synchronized void removeHotkey( final Hotkey hotkey ) {
		final boolean active = isActive();
		if ( active )
			deactivate();
		
		hotkeyList.remove( hotkey );
		
		if ( active )
			activate();
	}
	
	/**
	 * Activates the global hotkeys.
	 */
	public static synchronized void activate() {
		if ( !supported || hotkeyCheckerThread != null )
			return;
		
		// Start thread
		hotkeyCheckerThread = new GlobalHotkeys();
		hotkeyCheckerThread.start();
	}
	
	/**
	 * Deactivates the global hotkeys.
	 */
	public static synchronized void deactivate() {
		if ( hotkeyCheckerThread == null )
			return;
		
		// Stop thread
		hotkeyCheckerThread.shutdown();
		hotkeyCheckerThread = null;
	}
	
	/**
	 * Tells if "global hotkeys" is active.
	 * @return true if "global hotkeys" is active; false otherwise
	 */
	private static synchronized boolean isActive() {
		return hotkeyCheckerThread != null && !hotkeyCheckerThread.isCancelRequested();
	}
	
	/**
	 * User32.dll interface we're interested in
	 * @author Andras Belicza
	 */
	private static interface MyUser32 extends StdCallLibrary {
		boolean RegisterHotKey( HWND hWnd, int id, int fsModifiers, int vk );
		boolean UnregisterHotKey( HWND hWnd, int id );
	}
	
	/** Reference to the hotkey checker thread. */
	private static volatile ControlledThread hotkeyCheckerThread;
	/** Reference to the my user32 interface.   */
	private static MyUser32 myUser32;
	
	@Override
	public void run() {
		// Registration and message handling must be in the same thread (else no message would be delivered to the thread)!
		
		// User32 interface loaded on demand to prevent exception if it is not available and Global hotkeys are disabled
		synchronized ( MyUser32.class ) {
			if ( myUser32 == null )
				myUser32 = (MyUser32) Native.loadLibrary( "user32", MyUser32.class );
		}
		
		// First register hotkeys
		synchronized ( GlobalHotkeys.class ) {
			for ( int i = 0; i < hotkeyList.size(); i++ ) {
				final Integer hotkeyId = i + 1000;
				if ( registeredHotkeyMap.containsKey( hotkeyId ) )
					continue; // Already registered (could be due to unsuccessful unregistration)
				
				final Hotkey hotkey = hotkeyList.get( i );
				
				final boolean success = myUser32.RegisterHotKey( null, hotkeyId, hotkey.modifiers, hotkey.keyCode );
				
				if ( success )
					registeredHotkeyMap.put( hotkeyId, hotkey );
				else
					System.out.println( "Failed to register global hotkey: " + hotkey );
			}
		}
		
		final User32 user32 = User32.INSTANCE;
		
		// Message cycle
		final MSG msg = new MSG();
		while ( !requestedToCancel ) {
			while ( user32.PeekMessage( msg, null, 0, 0, 1 ) ) {
				final int hotkeyId = msg.wParam.intValue();
				if ( hotkeyId != 0 ) {
					final Hotkey hotkey = registeredHotkeyMap.get( hotkeyId );
					if ( hotkey != null )
						hotkey.run();
				}
			}
			
			try {
				sleep( 50l );
			} catch ( final InterruptedException ie ) {
				ie.printStackTrace();
			}
		}
		
		// Lastly unregister hotkeys
		for ( final Iterator< Entry< Integer, Hotkey > > iterator = registeredHotkeyMap.entrySet().iterator(); iterator.hasNext(); ) {
			final Entry< Integer, Hotkey > entry = iterator.next();
			final boolean success = myUser32.UnregisterHotKey( null, entry.getKey() );
			if ( success )
				iterator.remove();
		}
	}
	
}
