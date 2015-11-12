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

import java.io.InputStream;

/**
 * Sounds utility services.
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GeneralServices
 */
public interface SoundsApi {
	
	/** Interface version. */
	String VERSION = "1.0";
	
	/**
	 * Plays a sound sample. Several formats are supported: <b>MP3</b>, WAV, AIFF, AU etc.<br>
	 * The sound will be played in a new thread. The <code>dataStream</code> will be closed at the end of play.<br>
	 * The current volume user setting will be used as the volume.
	 * 
	 * <p><i>Note: plugins should respect whether the user enabled/disabled voice notifications,
	 * see {@link InfoApi#isVoiceNotificationsEnabled()}.</i></p>
	 * 
	 * @param dataStream  data stream to read the sound data from
	 * @param waitPlayEnd tells if have to wait the end of the play or return immediately
	 * @return true if the file was started playing; false if error occurred
	 * @see #playSound(InputStream, boolean, int)
	 */
	boolean playSound( InputStream dataStream, boolean waitPlayEnd );
	
	/**
	 * Plays a sound sample. Several formats are supported: <b>MP3</b>, WAV, AIFF, AU etc.<br>
	 * The sound will be played in a new thread. The <code>dataStream</code> will be closed at the end of play.
	 * 
	 * <p><i>Note: plugins should respect whether the user enabled/disabled voice notifications,
	 * see {@link InfoApi#isVoiceNotificationsEnabled()}.</i></p>
	 * 
	 * <p><i>Note #2: plugins should respect the volume setting of the user, the need of specifying
	 * a different volume should be a rare case (for example a volume setting test).</i></p>
	 * 
	 * @param dataStream  data stream to read the sound data from
	 * @param waitPlayEnd tells if have to wait the end of the play or return immediately
	 * @param volume      volume to use (valid range: 0..100)
	 * @return true if the file was started playing; false if error occurred
	 * @see #playSound(InputStream, boolean)
	 */
	boolean playSound( InputStream dataStream, boolean waitPlayEnd, int volume );
	
}
