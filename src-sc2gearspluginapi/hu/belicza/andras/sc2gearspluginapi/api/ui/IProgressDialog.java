/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.ui;

import hu.belicza.andras.sc2gearspluginapi.api.GuiUtilsApi;

/**
 * A dialog displaying a progress bar and control buttons to abort/close.<br>
 * The progress dialog registers a background job, and in order to clear that,
 * {@link #taskFinished()} must be called after the use of this dialog.
 * 
 * <p>Example usage:<br>
 * <blockquote><pre>
 * ImageIcon dialogIcon = new ImageIcon( "images/copy.png" );
 * int total = 200;
 * IProgressDialog progressDialog = pluginService.getGuiUtilsApi().createProgressDialog( "Copying files...", dialogIcon, total );
 * 
 * for ( int i = 0; i &lt; total; i++ ) {
 *     if ( progressDialog.isAborted() )
 *         break;
 *     
 *     // Execute a subtask either here or call your method that does that:
 *     boolean success = executeSubtask( i );
 *     
 *     progressDialog.incrementProcessed();
 *     if ( !success )
 *         progressDialog.incrementFailed();
 *     
 *     progressDialog.updateProgressBar();
 * }
 * 
 * // The registered background job must be cleared:
 * progressDialog.taskFinished();
 * </pre></blockquote></p>
 * 
 * @author Andras Belicza
 * 
 * @see GuiUtilsApi#createProgressDialog(String, javax.swing.ImageIcon, int)
 * @see GuiUtilsApi#createProgressDialog(java.awt.Frame, String, javax.swing.ImageIcon, int)
 * @see GuiUtilsApi#createProgressDialog(java.awt.Dialog, String, javax.swing.ImageIcon, int)
 */
public interface IProgressDialog {
	
	/**
	 * Increments the number of processed subtasks.
	 */
	void incrementProcessed();
	
	/**
	 * Increments the number of failed subtasks.
	 */
	void incrementFailed();
	
	/**
	 * Returns the number of processed subtasks.
	 * @return the number of processed subtasks
	 */
	int getProcessed();
	
	/**
	 * Returns the number of failed subtasks.
	 * @return the number of failed subtasks
	 */
	int getFailed();
	
	/**
	 * Returns the total number of subtasks.
	 * @return the total number of subtasks
	 */
	int getTotal();
	
	/**
	 * Tells if abort has been requested.
	 * @return true if abort has been requested; false otherwise
	 */
	boolean isAborted();
	
	/**
	 * Updates the progress bar and the info texts.
	 */
	void updateProgressBar();
	
	/**
	 * Registers that the task has been finished.
	 */
	void taskFinished();
	
	/**
	 * Sets a custom message.
	 * @param message custom message to be set
	 */
	void setCustomMessage( final String message );
	
}
