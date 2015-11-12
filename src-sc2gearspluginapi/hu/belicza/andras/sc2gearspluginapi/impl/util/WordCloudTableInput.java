/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.impl.util;

import hu.belicza.andras.sc2gearspluginapi.api.GuiUtilsApi;

/**
 * A wrapper class that holds the information required to create and show a Word Cloud dialog taking a table as the input.
 * 
 * @since "2.3"
 * 
 * @author Andras Belicza
 * 
 * @see GuiUtilsApi#createTableBox(javax.swing.JTable, javax.swing.JComponent, WordCloudTableInput)
 */
public class WordCloudTableInput {
	
	/** The sub-title of the Word Cloud dialog.                                      */
	public final String title;
	/** The column index to take the words from.
	 * {@link Object#toString()} will be used to obtain words from the table values. */
	public final int    wordColumnIndex;
	/** The column index to take the frequencies from.
	 * Must contain {@link Integer}s.                                                */
	public final int    frequencyColumnIndex;
	
	/**
	 * Creates a new WordCloudTableInput.
	 * @param title                the sub-title of the Word Cloud dialog
	 * @param wordColumnIndex      the column index to take the words from, {@link Object#toString()} will be used to obtain words from the table values
	 * @param frequencyColumnIndex the column index to take the frequencies from, must contain {@link Integer}s
	 */
	public WordCloudTableInput( final String title, final int wordColumnIndex, final int frequencyColumnIndex ) {
		this.title                = title;
		this.wordColumnIndex      = wordColumnIndex;
		this.frequencyColumnIndex = frequencyColumnIndex;
	}
	
}
