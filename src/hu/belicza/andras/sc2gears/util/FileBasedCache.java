/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.util;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.ui.GuiUtils;

import java.awt.Dialog;
import java.awt.Font;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 * Utilities for a file based cache manager.
 * 
 * @author Andras Belicza
 */
public class FileBasedCache {

	/**
	 * Empties the cache: deletes all files in the cache.<br>
	 * Displays a modal info dialog about the fact that the cache is being emptied.
	 * @param owner optional owner of the info dialog
	 */
	protected static void emptyCache_( final String cacheEntity, final Dialog owner, final String infoTextKey, final String cacheFolder ) {
		System.out.println( "Clearing " + cacheEntity + " cache..." );
		
		final JDialog infoDialog = owner == null ? new JDialog() : new JDialog( owner );
		infoDialog.setModal( true );
		infoDialog.setTitle( Language.getText( "general.infoTitle" ) );
		final JLabel infoLabel = new JLabel( Language.getText( infoTextKey ) );
		infoLabel.setFont( infoLabel.getFont().deriveFont( Font.ITALIC ) );
		infoLabel.setBorder( BorderFactory.createEmptyBorder( 25, 35, 25, 35 ) );
		infoDialog.getContentPane().add( infoLabel );
		infoDialog.pack();
		if ( owner == null )
			infoDialog.setLocationRelativeTo( null );
		else
			GuiUtils.centerWindowToWindow( infoDialog, owner );
		
		new NormalThread( cacheEntity + " cache emptier" ) {
			@Override
			public void run() {
				final File[] cacheFiles = new File( cacheFolder ).listFiles();
				if ( cacheFiles != null )
					for ( final File cacheFile : cacheFiles )
						deleteFile( cacheFile );
				
				infoDialog.dispose();
			}
		}.start();
		
		infoDialog.setVisible( true );
	}
	
	/**
	 * Deletes the specified file or directory recursively.
	 * @param file file to be deleted
	 */
	private static void deleteFile( final File file ) {
		if ( file.isDirectory() ) {
			final File[] files = file.listFiles();
			if ( files != null )
				for ( final File child : files )
					deleteFile( child );
		}
		file.delete();
	}
	
}
