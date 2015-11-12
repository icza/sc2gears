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

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.sound.Sounds;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.ontopdialogs.OnTopApmDisplayDialog;
import hu.belicza.andras.sc2gears.util.ControlledThread;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.ObjectRegistry;
import hu.belicza.andras.sc2gearspluginapi.api.enums.GameStatus;
import hu.belicza.andras.sc2gearspluginapi.api.listener.GameStatusListener;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

/**
 * Monitor of SC2's windows registry values..
 * 
 * Possible replacement: http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 * 
 * @author Andras Belicza
 */
public class Sc2RegMonitor extends ControlledThread {
	
	/** Registry to handle game status listeners. */
	private static final ObjectRegistry< GameStatusListener > gameStatusListenerRegistry = new ObjectRegistry< GameStatusListener >();
	
	/**
	 * Adds a {@link GameStatusListener}.
	 * @param gameStatusListener game status listener to be added
	 */
	public static void addGameStatusListener( final GameStatusListener gameStatusListener ) {
		gameStatusListenerRegistry.add( gameStatusListener );
	}
	
	/**
	 * Removes a {@link GameStatusListener}.
	 * @param gameStatusListener game status listener to be removed
	 */
	public static void removeGameStatusListener( final GameStatusListener gameStatusListener ) {
		gameStatusListenerRegistry.remove( gameStatusListener );
	}
	
	/** Tells if SC2 Reg Monitor is supported on the current machine. */
	public static final boolean supported = GeneralUtils.isWindows();
	
	/** APM alert repetition intervals (sec). */
	public static final Object[] APM_ALERT_REPETITION_INTERVALS = new Object[] { Language.getText( "miscSettings.never" ), 3, 5, 10, 15, 20, 30, 60, 90, 120 };
	
	/** Unknown game status. */
	public static final int GAME_STATUS_UNKNOWN = -1;
	/** No game status.      */
	public static final int GAME_STATUS_NO_GAME = 0;
	/** Game started status. */
	public static final int GAME_STATUS_STARTED = 1;
	
	/** Game status.                                       */
	private int     gameStatus = GAME_STATUS_UNKNOWN;
	/** Start of the warm up time if not null.             */
	private Long    warmUpTimeStart;
	/** Tells if actual APM is OK (above the alert level). */
	private boolean apmOk;
	/** Time of the last APM alert.                        */
	private long    lastApmAlertTime;
	
	/**
	 * Creates a new Sc2RegMonitor.
	 */
	public Sc2RegMonitor() {
		super( "Sc2 Reg Monitor" );
	}
	
	/**
	 * This method contains the cycle which checks the actual APM value.
	 */
	@Override
	public void run() {
		long lastApmCheckTime = 0;
		while ( !requestedToCancel )
			try {
				
				// Check game status
				final Integer newGameStatus = getGameStatus();
				if ( newGameStatus != null ) {
					if ( gameStatus != GAME_STATUS_STARTED && newGameStatus == GAME_STATUS_STARTED ) {
						// Register that game is in progress
						MainFrame.INSTANCE.setSc2GameStatus( true );
						if ( gameStatus == GAME_STATUS_NO_GAME ) {
							// The game just started now
							warmUpTimeStart = System.currentTimeMillis();
							if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_ALERT_ON_GAME_START ) && Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
								Sounds.playSoundSample( Sounds.SAMPLE_GAME_STARTED, false );
							try {
								// We cannot let errors of the MousePrintRecorder interfere with us (exception would prevent changing game status after this
								MousePrintRecorder.onGameStart();
							} catch ( final Exception e ) {
								e.printStackTrace();
							}
						}
						else {
							// The game might be going on for some time now, skip the warm up time
							// (This is the case if APM alert was enabled during game, or Sc2gears was started during the game.) 
							warmUpTimeStart = null;
						}
						apmOk = true;
					}
					
					if ( gameStatus == GAME_STATUS_STARTED && newGameStatus == GAME_STATUS_NO_GAME ) {
						// A game just ended now
						MainFrame.INSTANCE.setSc2GameStatus( false );
						if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_ALERT_ON_GAME_END ) && Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
							Sounds.playSoundSample( Sounds.SAMPLE_GAME_ENDED, false );
						try {
							// We cannot let errors of the MousePrintRecorder interfere with us (exception would prevent changing game status after this
							MousePrintRecorder.onGameEnd();
						} catch ( final Exception e ) {
							e.printStackTrace();
						}
					}
					
					if ( gameStatus != newGameStatus ) {
						// Check On-Top APM display dialog
						if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_SHOW_HIDE_APM_DISPLAY_ON_START_END ) ) {
							if ( newGameStatus == GAME_STATUS_STARTED ) {
								if ( !OnTopApmDisplayDialog.isOpened() )
									OnTopApmDisplayDialog.open();
							}
							else if ( newGameStatus == GAME_STATUS_NO_GAME ) {
								if ( OnTopApmDisplayDialog.isOpened() )
									OnTopApmDisplayDialog.close();
							}
						}
						
						// Call game status listeners
						synchronized ( gameStatusListenerRegistry ) {
							for ( final GameStatusListener gameStatusListener : gameStatusListenerRegistry )
								try {
									// Call listener in a try-catch block to stop uncaught exceptions 
									final GameStatus listenerGameStatus;
									switch ( newGameStatus ) {
									case GAME_STATUS_NO_GAME : listenerGameStatus = GameStatus.NO_GAME; break;
									case GAME_STATUS_STARTED : listenerGameStatus = GameStatus.STARTED; break;
									default                  : listenerGameStatus = GameStatus.UNKNOWN; break;
									}
									gameStatusListener.gameStatusChanged( listenerGameStatus );
								} catch ( final Throwable t ) {
									t.printStackTrace();
								}
						}
					}
					
					gameStatus = newGameStatus;
				}
				
				// Check actual APM
				final long currentTime = System.currentTimeMillis();
				if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_APM_ALERT ) && gameStatus == GAME_STATUS_STARTED
					&& currentTime > lastApmCheckTime + Settings.getInt( Settings.KEY_SETTINGS_MISC_APM_CHECK_INTERVAL_SEC ) * 1000
					&& ( warmUpTimeStart == null || currentTime > warmUpTimeStart + Settings.getInt( Settings.KEY_SETTINGS_MISC_APM_WARMUP_TIME ) * 1000 ) ) {
					lastApmCheckTime = currentTime;
					
					Integer actualApm = getApm();
					
					if ( actualApm != null ) {
						if ( actualApm >= Settings.getInt( Settings.KEY_SETTINGS_MISC_APM_ALERT_LEVEL ) ) {
							// APM is now OK
							if ( !apmOk ) { // If it was low before
								if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_ALERT_WHEN_APM_IS_BACK_TO_NORMAL ) && Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
									Sounds.playSoundSample( Sounds.SAMPLE_APM_OK, false );
								apmOk = true;
							}
						}
						else {
							// APM is low
							if ( apmOk ) { // If it was OK before
								lastApmAlertTime = currentTime; 
								if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
									Sounds.playSoundSample( Sounds.SAMPLE_LOW_APM, false );
								apmOk = false;
							}
							else { // It was low before too
								final int apmAlertRepetitionInterval = Settings.getInt( Settings.KEY_SETTINGS_MISC_APM_ALERT_REPETITION_INTERVAL_SEC );
								if ( apmAlertRepetitionInterval > 0 && currentTime > lastApmAlertTime + (Integer) APM_ALERT_REPETITION_INTERVALS[ apmAlertRepetitionInterval ] * 1000 ) {
									lastApmAlertTime = currentTime; 
									if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
										Sounds.playSoundSample( Sounds.SAMPLE_LOW_APM, false );
								}
							}
						}
					}
				}
				
				sleep( 1000 );
				
			} catch ( final Exception e ) {
				e.printStackTrace();
			}
	}
	
	/**
	 * Returns the current game status from the Windows Registry.
	 * @return the current game status or <code>null</code> if some error occurs 
	 */
	public static Integer getGameStatus() {
		try {
			return Advapi32Util.registryGetIntValue( WinReg.HKEY_CURRENT_USER, "Software\\Razer\\Starcraft2", "StartModule" );
		} catch ( final Exception e ) {
			// Silently ignore, do not print stack trace
			return null;
		}
	}
	
	/**
	 * Returns the current APM originating from the Windows Registry.
	 * 
	 * Note: since 2.0 SC2 outputs real-time APM instead of game-time APM,
	 * so no conversion is performed to convert it anymore.
	 * 
	 * <p><b>How to interpret the values in the registry?</b><br>
	 * The digits of the result: 5 has to be subtracted from the first digit (in decimal representation), 4 has to be subtracted from the second digit,
	 * 3 from the third etc.
	 * If the result of a subtraction is negative, 10 has to be added.
	 * Examples: 64 => 10 APM; 40 => 96 APM; 8768 => 3336 APM; 38 => 84 APM</p>
	 * 
	 * @return the current APM or <code>null</code> if some error occurs 
	 */
	public static Integer getApm() {
		try {
			final String apmString = Advapi32Util.registryGetStringValue( WinReg.HKEY_CURRENT_USER, "Software\\Razer\\Starcraft2", "APMValue" );
			int apm = 0;
			
			for ( int idx = 0, delta = 5, digit; idx < apmString.length(); idx++, delta-- ) {
				digit = apmString.charAt( idx ) - '0' - delta;
				if ( digit < 0 )
					digit += 10;
				apm = apm * 10 + digit;
			}
			
			return apm;
		} catch ( final Exception e ) {
			// Silently ignore, do not print stack trace
			return null;
		}
	}
	
}
