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

/**
 * Defines services to interact with the mouse print recorder.
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GeneralServices
 */
public interface MousePrintRecorderApi {
	
	/** Interface version. */
	String VERSION = "2.0";
	
	/**
	 * Displays the recorder frame which can be used to manually start/stop the recording and to display the recorded mouse print.
	 * <p>Note that the Mouse print recorder can record and save mouse prints without the frame being displayed.</p>
	 */
	void showFrame();
	
	/**
	 * Starts recording the mouse print.
	 * @see #stopRecording()
	 */
	void startRecording();
	
	/**
	 * Stops recording the mouse print.
	 * @see #startRecording()
	 */
	void stopRecording();
	
	/**
	 * Tells if a recording is in progress.
	 * @return true if a recording is in progress; false otherwise
	 */
	boolean isRecording();
	
	/**
	 * Tells if a recording is present.
	 * <p>Returns true if a recording is present either if it is still in progress or if it is stopped.</p> 
	 * @return true if a recording is present; false otherwise
	 */
	boolean isRecordingPresent();
	
	/**
	 * Saves the recording.
	 * <p>The recording can only be saved if a recording is not in progress.
	 * Does nothing if a recording is not present or a recording is in progress.</p>
	 * @see #isRecording()
	 * @see #isRecordingPresent()
	 */
	void saveRecording();
	
}
