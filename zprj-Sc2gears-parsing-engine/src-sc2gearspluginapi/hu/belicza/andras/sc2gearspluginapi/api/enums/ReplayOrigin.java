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

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IReplay;

/**
 * Replay origin. Tells where/how the replay was assembled.
 * 
 * @since "2.7"
 * 
 * @author Andras Belicza
 * 
 * @see IReplay#getReplayOrigin()
 */
public enum ReplayOrigin {
	
	/** The replay was assembled by parsing the replay file.  */
	REPLAY_PARSER,
	/** The replay was taken from the replay cache.           */
	REPLAY_CACHE,
	/**
	 * The replay was assembled from a replay specification.
	 * @see ReplaySpecification
	 */
	REPLAY_SPECIFICATION;
	
}
