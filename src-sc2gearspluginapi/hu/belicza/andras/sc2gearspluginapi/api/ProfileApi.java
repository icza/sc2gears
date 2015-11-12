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
import hu.belicza.andras.sc2gearspluginapi.api.listener.CustomPortraitListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ProfileListener;
import hu.belicza.andras.sc2gearspluginapi.api.profile.IProfile;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerId;

/**
 * Defines services to access player profile info.
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GeneralServices
 */
public interface ProfileApi {
	
	/** Interface version. */
	String VERSION = "2.0";
	
	/**
	 * Asynchronous profile get method.
	 * 
	 * <p>Queries the profile of the specified player. If the profile is cached, it will be passed to the profile listener immediately.
	 * If the profile is not cached, or is out-dated, it will be retrieved (again) and the profile listener will be called (again)
	 * with the new profile.</p>
	 * 
	 * <p>New profiles are retrieved asynchronously in a new thread. If the retrieval of a new profile fails and no cached,
	 * out-dated version is available, the profile listener will be called with a <code>null</code> value.</p>
	 * 
	 * <p>If extended info is required but is not available right away, the base profile info will be passed to the listener,
	 * and the extended info will be retrieved after that, and when it is ready, the listener will be called again.</p>
	 * 
	 * @param playerId          player identifier
	 * @param profileListener   listener to be called when the profile is available
	 * @param queryExtendedInfo tells if extended profile info has to be retrieved too
	 * @param forceRetrieve     forces retrieving the profile even if its validity time is not over yet; also only calls {@link ProfileListener#profileReady(IProfile, boolean)} if the new profile is ready
	 * @return true if a profile info is available right away; false otherwise
	 * 
	 * @see IPlayerId
	 * @see ProfileListener
	 * @see InfoApi#getProfileInfoValidityTime()
	 * @see InfoApi#getAutoRetrieveExtProfileInfo()
	 */
	boolean queryProfile( IPlayerId playerId, ProfileListener profileListener, boolean queryExtendedInfo, boolean forceRetrieve );
	
	/**
	 * Queries the custom portrait for the specified player.
	 * 
	 * <p>Custom portraits are retrieved asynchronously in a new thread. If the retrieval of a custom portrait fails,
	 * the custom portrait listener will be called with a <code>null</code> value.</p>
	 * 
	 * <p>If a custom portrait is not defined for the specified player, the custom portrait listener will not be called.<br>
	 * If the custom portrait is cached, it will be passed to the custom portrait listener immediately.<br>
	 * If the custom portrait is not cached, it will be retrieved and the custom profile listener will be called
	 * with the custom portrait.</p>
	 * 
	 * <p>You can read more about the custom portraits: <a href="https://sites.google.com/site/sc2gears/custom-portraits">Custom portraits</a></p>
	 * 
	 * @param playerId               player identifier
	 * @param highRes                tells if high resolution version is required; normal resolution is 45x45, high resolution is 90x90
	 * @param customPortraitListener custom portrait listener to be called with the results
	 * @return {@link Boolean#TRUE} if the custom portrait is available right away; {@link Boolean#FALSE} if it is being downloaded; <code>null</code> if no custom portrait is defined for the specified player
	 */
	Boolean queryCustomPortrait( IPlayerId playerId, boolean highRes, CustomPortraitListener customPortraitListener );
	
}
