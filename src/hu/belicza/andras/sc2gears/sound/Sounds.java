/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.sound;

import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.util.NormalThread;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

/**
 * The Sound Manager responsible to manage and play sounds.
 * 
 * @author Andras Belicza
 */
public class Sounds {
	
	/**
	 * The description of a voice.
	 * @author Andras Belicza
	 */
	public static class VoiceDescription {
		/** Name of the voice.                     */
		public final String name;
		/** Display name of the voice.             */
		public final String displayName;
		/** First name of the author of the voice. */
		public final String authorFirstName;
		/** Last name of the author of the voice.  */
		public final String authorLastName;
		/** Language of the voice.                 */
		public final String language;
		
		/**
		 * Creates a new VoiceDescription.
		 * @param name            name of the voice
		 * @param displayName     display name of the voice
		 * @param authorFirstName first name of the author of the voice
		 * @param authorLastName  last name of the author of the voice
		 * @param language        language of the voice
		 */
		public VoiceDescription( final String name, final String displayName, final String authorFirstName, final String authorLastName, final String language ) {
			this.name            = name;
			this.displayName     = displayName;
			this.authorFirstName = authorFirstName;
			this.authorLastName  = authorLastName;
			this.language        = language;
		}
		
		@Override
		public String toString() {
			return displayName;
		}
	}
	
	/** Descriptions of the available voices. The default voice is the first in the array. */
	public static final VoiceDescription[] VOICE_DESCRIPTIONS = {
		new VoiceDescription( "smix"      , "Smix"       , "Smix"      , ""     , "English"   ),
		new VoiceDescription( "smixkorean", "Smix 한국어", "Smix"      , ""     , "Korean"    ),
		new VoiceDescription( "smuck"     , "Smuck"      , "Andrea"    , "Smuck", "Hungarian" ),
		new VoiceDescription( "google"    , "Google"     , "Google TTS", ""     , "English"   )   // http://translate.google.com/translate_tts?tl=en&q=Hello
	};
	
	/**
	 * Returns the description of a voice specified by its name.
	 * @param voiceName name of the voice whose description to be returned
	 * @return the description of a voice specified by its name
	 */
	public static VoiceDescription getVoiceDescription( final String voiceName ) {
		for ( final VoiceDescription voiceDesc : VOICE_DESCRIPTIONS )
			if ( voiceDesc.name.equals( voiceName ) )
				return voiceDesc;
		return null;
	}
	
	/** Default theme which contains all samples. */
	private static final String DEFAULT_THEME = "google";
	
	/** Name of voice sample.                 */
	public static final String SAMPLE_VOICE_NAME         = "voice_name";         // <voice name>
	/** Name of the changing language sample. */
	public static final String SAMPLE_CHANGING_LANGUAGE  = "changing_language";  // "Restart is required when changing language."
	/** Name of the changing theme sample.    */
	public static final String SAMPLE_CHANGING_THEME     = "changing_theme";     // "Restart is advised when changing theme."
	/** Name of the replay saved sample.      */
	public static final String SAMPLE_REPLAY_SAVED       = "replay_saved";       // "Replay saved"
	/** Name of the save failed sample.       */
	public static final String SAMPLE_REPLAY_SAVE_FAILED = "replay_save_failed"; // "Failed to save replay"
	/** Name of the Sc2gears sample.          */
	public static final String SAMPLE_SC2GEARS           = "sc2gears";           // "Sc2gears"
	/** Name of the thank you sample.         */
	public static final String SAMPLE_THANK_YOU          = "thank_you";          // "Thank You for choosing Sc2gears."
	/** Name of the updates available sample. */
	public static final String SAMPLE_UPDATES_AVAILABLE  = "updates_available";  // "Updates available!"
	/** Name of the welcome sample.           */
	public static final String SAMPLE_WELCOME            = "welcome";            // "Welcome!"
	/** Name of the language updated sample.  */
	public static final String SAMPLE_LANGUAGE_UPDATED   = "language_updated";   // "Language file updated. Please restart Sc2gears."
	/** Name of the low APM sample.           */
	public static final String SAMPLE_LOW_APM            = "low_apm";            // "Low APM!"
	/** Name of the APM OK sample.            */
	public static final String SAMPLE_APM_OK             = "apm_ok";             // "APM OK."
	/** Name of the game started sample.      */
	public static final String SAMPLE_GAME_STARTED       = "game_started";       // "Game started!"
	/** Name of the game ended sample.        */
	public static final String SAMPLE_GAME_ENDED         = "game_ended";         // "Game ended!"
	/** Name of the auto-save on sample.      */
	public static final String SAMPLE_AUTO_SAVE_ON       = "auto_save_on";       // "Auto-save on."
	/** Name of the auto-save off sample.     */
	public static final String SAMPLE_AUTO_SAVE_OFF      = "auto_save_off";      // "Auto-save off."
	/** Name of the APM Alert on sample.      */
	public static final String SAMPLE_APM_ALERT_ON       = "apm_alert_on";       // "APM Alert on."
	/** Name of the APM Alert off sample.     */
	public static final String SAMPLE_APM_ALERT_OFF      = "apm_alert_off";      // "APM Alert off."
	/** Name of the recording started sample. */
	public static final String SAMPLE_RECORDING_STARTED  = "recording_started";  // "Recording started."
	/** Name of the recording stopped sample. */
	public static final String SAMPLE_RECORDING_STOPPED  = "recording_stopped";  // "Recording stopped."
	/** Name of the file store failed sample. */
	public static final String SAMPLE_FILE_STORE_FAILED  = "file_store_failed";  // "Failed to store file!"
	
	/**
	 * Plays a sound sample.<br>
	 * The sound sample will be played on a new thread.
	 * 
	 * @param sampleName  name of the sample to play
	 * @param waitPlayEnd tells if have to wait the end of the play or return immediately
	 * @return true if the sound sample was started playing; false if error occurred
	 */
	public static boolean playSoundSample( final String sampleName, final boolean waitPlayEnd ) {
		return playSoundSample( sampleName, waitPlayEnd, Settings.getInt( Settings.KEY_SETTINGS_MISC_SOUND_VOLUME ) );
	}
	
	/**
	 * Plays a sound sample.<br>
	 * The sound will be played in a new thread.
	 * 
	 * @param sampleName  name of the sample to play
	 * @param waitPlayEnd tells if have to wait the end of the play or return immediately
	 * @param volume      volume to use (valid range: 0..100)
	 * @return true if the file was started playing; false if error occurred
	 */
	public static boolean playSoundSample( final String sampleName, final boolean waitPlayEnd, final int volume ) {
		InputStream dataStream = Sounds.class.getResourceAsStream( Settings.getString( Settings.KEY_SETTINGS_VOICE ) + "/" + sampleName + ".mp3" );
		
		if ( dataStream == null ) {
			if ( !DEFAULT_THEME.equals( Settings.getString( Settings.KEY_SETTINGS_VOICE ) ) ) {
				// Try the default theme
				dataStream = Sounds.class.getResourceAsStream( DEFAULT_THEME + "/" + sampleName + ".mp3" );
			}
			if ( dataStream == null )
				return false;
		}
		
		return playSound( dataStream, waitPlayEnd, volume );
	}
	
	/**
	 * Plays a sound.<br>
	 * The sound will be played in a new thread. The <code>dataStream</code> will be closed at the end of play.
	 * 
	 * @param dataStream  data stream to read the sound data from
	 * @param waitPlayEnd tells if have to wait the end of the play or return immediately
	 * @param volume      volume to use (valid range: 0..100)
	 * @return true if the file was started playing; false if error occurred
	 */
	public static boolean playSound( InputStream dataStream, final boolean waitPlayEnd, final int volume ) {
		try {
			// AudioSystem requires the stream to support marks:
			if ( !dataStream.markSupported() )
				dataStream = new BufferedInputStream( dataStream );
			
			final AudioInputStream audioInputStream        = AudioSystem.getAudioInputStream( dataStream );
			final AudioFormat      audioFormat             = audioInputStream.getFormat();
			// We have to create a target format as the MP3 format (variable bit rate) is not supported by the audio system 
			final AudioFormat      targetFormat            = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16, audioFormat.getChannels(), audioFormat.getChannels() * 2, audioFormat.getSampleRate(), false );
			final AudioInputStream wrappedAudioInputStream = AudioSystem.getAudioInputStream( targetFormat, audioInputStream );
			final SourceDataLine   audioLine               = (SourceDataLine) AudioSystem.getLine( new DataLine.Info( SourceDataLine.class, targetFormat ) );
			
			audioLine.open( targetFormat );
			
			if ( audioLine.isControlSupported( FloatControl.Type.MASTER_GAIN ) ) {
				final FloatControl volumeControl = (FloatControl) audioLine.getControl( FloatControl.Type.MASTER_GAIN );
	            volumeControl.setValue( 20.0f * (float) Math.log10( volume / 100.0 ) );
			}
			
			final Thread soundPlayerThread = new NormalThread( "Sound player" ) {
				@Override
				public void run() {
					try {
						audioLine.start();
						
						final byte[] buffer = new byte[ 64*1024 ];
						int          bytesRead;
						
						while ( ( bytesRead = wrappedAudioInputStream.read( buffer ) ) > 0 )
							audioLine.write( buffer, 0, bytesRead );
					}
					catch ( final Exception e  ) {
						e.printStackTrace();
					}
					finally {
						audioLine.drain();
						audioLine.stop ();
						audioLine.close();
						try { wrappedAudioInputStream.close(); } catch ( final IOException ie ) {}
						try { audioInputStream       .close(); } catch ( final IOException ie ) {}
					}
				}
			};
			soundPlayerThread.start();
			
			if ( waitPlayEnd )
				soundPlayerThread.join();
			
			return true;
		}
		catch ( final Exception e ) {
			e.printStackTrace();
			return false;
		}
	}
	
}
