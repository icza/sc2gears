package hu.belicza.andras.sc2gearspluginapi.impl;

import java.awt.event.KeyEvent;

import hu.belicza.andras.sc2gearspluginapi.api.CallbackApi;

/**
 * Describes a global (system-wide) hotkey.
 * 
 * <p>Example: you can register the CTRL+ALT+Q hotkey with the following code:<br>
 * <blockquote><pre>
 * Hotkey myHotkey = new Hotkey( Hotkey.MOD_CTRL | Hotkey.MOD_ALT | Hotkey.MOD_NOREP, KeyEvent.VK_Q ) {
 *     public void run() {
 *         System.out.println( "Hotkey was pressed: " + this );  // Hotkey.toString() is properly overridden..
 *     }
 * };
 * 
 * pluginService.getCallbackApi().addGlobalHotkey( myHotkey );
 * </pre></blockquote></p>
 * 
 * @author Andras Belicza
 * 
 * @see CallbackApi#addGlobalHotkey(Hotkey)
 * @see CallbackApi#removeGlobalHotkey(Hotkey)
 */
public class Hotkey implements Runnable {
	
	/** ALT modifier mask.       */
	public static final int MOD_ALT   = 0x0001;
	/** CONTROL modifier mask.   */
	public static final int MOD_CTRL  = 0x0002;
	/** SHIFT modifier mask.     */
	public static final int MOD_SHIFT = 0x0004;
	/** WIN modifier mask.       */
	public static final int MOD_WIN   = 0x0008;
	/** NO-REPEAT modifier mask. */
	public static final int MOD_NOREP = 0x4000;
	
	/** Modifiers of the hotkey.                                  */
	public final int      modifiers;
	/** Key code of the hotkey.                                   */
	public final int      keyCode;
	/** Optional task to be performed when the hotkey is pressed. */
	public final Runnable onKeyPressTask;
	
	/**
	 * Creates a new Hotkey.
	 * This constructor is to be used if the {@link #run()} method is overridden.
	 * @param modifiers modifiers of the hotkey
	 * @param keyCode   key code of the hotkey; use the constants defined in {@link KeyEvent}
	 * @see KeyEvent
	 */
	public Hotkey( final int modifiers, final int keyCode ) {
		this( modifiers, keyCode, null );
	}
	
	/**
	 * Creates a new Hotkey.
	 * @param modifiers      modifiers of the hotkey
	 * @param keyCode        key code of the hotkey; use the constants defined in {@link KeyEvent}
	 * @param onKeyPressTask optional task to be perfomed when the hotkey is pressed
	 * @see KeyEvent
	 */
	public Hotkey( final int modifiers, final int keyCode, final Runnable onKeyPressTask ) {
		this.modifiers      = modifiers;
		this.keyCode        = keyCode;
		this.onKeyPressTask = onKeyPressTask;
	}
	
	/**
	 * Called when the hotkey is pressed.<br>
	 * Default implementation calls {@link #onKeyPressTask} if provided, else throws {@link RuntimeException}.
	 * @throws RuntimeException if {@link #onKeyPressTask} is not provided
	 */
	@Override
	public void run() {
		if ( onKeyPressTask == null )
			throw new RuntimeException( "onKeyPressTask is not provided and run() is not overridden!" );
		else
			onKeyPressTask.run();
	}
	
	/**
	 * This method is properly overridden to provide a human readable text representation of the hotkey,
	 * for example <code>"CTRL+ALT+T"</code>.
	 */
	@Override
	public String toString() {
		String value = "";
		
		if ( ( modifiers & MOD_CTRL ) != 0 )
			value += "CTRL+";
		if ( ( modifiers & MOD_SHIFT ) != 0 )
			value += "SHIFT+";
		if ( ( modifiers & MOD_ALT ) != 0 )
			value += "ALT+";
		if ( ( modifiers & MOD_WIN ) != 0 )
			value += "WIN+";
		value += (char) keyCode;
		
		return value;
	}
	
}

