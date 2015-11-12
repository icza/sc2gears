/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.enums;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gearspluginapi.api.IconsApi;

/**
 * Icon size.
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see IconsApi
 */
public enum IconSize {
	
	/** Indicator of not showing the icon. */
	HIDDEN ( 8, "module.repAnalyzer.tab.charts.actions.iconSize.hidden" ),
	/** One fourth of the original size.   */
	SMALL  ( 2, "module.repAnalyzer.tab.charts.actions.iconSize.small"  ),
	/** Half of the original size.         */
	MEDIUM ( 1, "module.repAnalyzer.tab.charts.actions.iconSize.medium" ),
	/** Original size.                     */
	BIG    ( 0, "module.repAnalyzer.tab.charts.actions.iconSize.big"    );
	
	/** Bit shift count to calculate the new size. */
	public final int    sizeShift;
	/** Cache of the string value. */
	public final String stringValue;
	
	/**
	 * Creates a new Size.
	 * @param sizeShift bit shift count to calculate the new size
	 * @param textKey   key of the text representation
	 */
	private IconSize( final int sizeShift, final String textKey ) {
		this.sizeShift = sizeShift;
		stringValue    = Language.getText( textKey );
	}
	
	@Override
	public String toString() {
		return stringValue;
	};
	
}

