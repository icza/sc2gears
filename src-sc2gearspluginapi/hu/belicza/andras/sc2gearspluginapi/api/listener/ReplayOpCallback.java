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

import java.io.File;

/**
 * Defines an interface for callback methods that can be used to get notified of file changes.
 * 
 * @author Andras Belicza
 * 
 * @see ReplayOpsPopupMenuItemListener#actionPerformed(File[], ReplayOpCallback, Integer)
 */
public interface ReplayOpCallback {
	
	/**
	 * Called when a replay is moved successfully.
	 * @param file         file that was moved
	 * @param targetFolder target folder where it was moved to
	 * @param fileIndex    index of the file in the array on which the operation is performed
	 */
	void replayMoved( File file, File targetFolder, int fileIndex );
	
	/**
	 * Called when a replay is deleted successfully.
	 * @param file         file that was deleted
	 * @param fileIndex    index of the file in the array on which the operation is performed
	 */
	void replayDeleted( File file, int fileIndex );
	
	/**
	 * Called when a replay is renamed successfully.
	 * @param file      file that was renamed
	 * @param newFile   file that the replay was renamed to
	 * @param fileIndex index of the file in the array passed on which the operation is performed
	 */
	void replayRenamed( File file, File newFile, int fileIndex );
	
	/**
	 * Called when either a move, rename or delete operation ended.<br>
	 * Should be called only once when all the files are processed.<br>
	 * This method is not called if the operation was aborted before file changes were made.
	 */
	void moveRenameDeleteEnded();
	
}
