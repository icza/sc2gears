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

import hu.belicza.andras.sc2gearspluginapi.api.ProfileApi;
import hu.belicza.andras.sc2gearspluginapi.api.profile.IProfile;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerId;

/**
 * A profile listener for asynchronous profile requests.
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see ProfileApi#queryProfile(IPlayerId, ProfileListener, boolean, boolean)
 */
public interface ProfileListener {
	
	/**
	 * Called when a queried profile is ready, even if just partially.
	 * 
	 * @param profile profile that was queried and is now ready; can be <code>null</code> if there is no profile for the profile query or if profile retrieval failed 
	 * @param isAnotherRetrievingInProgress true if another retrieving is in progress; false otherwise<br>
	 * 		If the value is true, this method will be called again when updated or extended info is available.
	 * 
	 * @see IProfile
	 */
	void profileReady( IProfile profile, boolean isAnotherRetrievingInProgress );
	
}
