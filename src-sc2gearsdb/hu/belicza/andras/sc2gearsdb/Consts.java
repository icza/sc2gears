/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb;

import java.util.Arrays;
import java.util.List;

/**
 * Common consts used from multiple servlets/service implementations.
 * 
 * @author Andras Belicza
 */
public class Consts {
	
	/** List of default replay label names. */
	public static final List< String > DEFAULT_REPLAY_LABEL_LIST = Arrays.asList( "GG", "LOL", "Owned", "BG", "WTF", "Close", "Epic", "Comeback", "Base-trade", "L#10", "L#11", "L#12", "L#13" );
	
	/** Default colors of the replay labels.            */
	public static final String[] DEFAULT_REPLAY_LABEL_COLORS    = { "ffffff", "ffffff", "ffffff", "ffffff", "ffffff", "ffffff", "ffffff", "ffffff", "ffffff", "ffffff", "ffffff", "ffffff", "ffffff" };
	/** Default background colors of the replay labels. */
	public static final String[] DEFAULT_REPLAY_LABEL_BG_COLORS = { "d4343e", "0042ff", "1ca7ea", "540081", "e77b0b", "1b9000", "af31e9", "106246", "7e410f", "888800", "008888", "666666", "202020" };
	
}
