/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi;

import hu.belicza.andras.sc2gearspluginapi.api.CallbackApi;
import hu.belicza.andras.sc2gearspluginapi.api.EapmUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.GeneralUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.GuiUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.IconsApi;
import hu.belicza.andras.sc2gearspluginapi.api.InfoApi;
import hu.belicza.andras.sc2gearspluginapi.api.LanguageApi;
import hu.belicza.andras.sc2gearspluginapi.api.MousePrintRecorderApi;
import hu.belicza.andras.sc2gearspluginapi.api.ProfileApi;
import hu.belicza.andras.sc2gearspluginapi.api.ReplayFactoryApi;
import hu.belicza.andras.sc2gearspluginapi.api.ReplayUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.SoundsApi;
import hu.belicza.andras.sc2gearspluginapi.api.StarCraftIIApi;

/**
 * Defines general services provided for Sc2gears plugins.
 * 
 * <p>The provided services are logically grouped by their functions into smaller ones.</p>
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see InfoApi
 * @see LanguageApi
 * @see CallbackApi
 * @see StarCraftIIApi
 * @see GeneralUtilsApi
 * @see GuiUtilsApi
 * @see SoundsApi
 * @see MousePrintRecorderApi
 * @see ReplayFactoryApi
 * @see ReplayUtilsApi
 * @see IconsApi
 * @see ProfileApi
 * @see EapmUtilsApi
 */
public interface GeneralServices {
	
	/** Interface version. */
	String VERSION = "2.5";
	
	/**
	 * Returns the general services implementation version.
	 * @return the general services implementation version
	 */
	String getImplementationVersion();
	
	/**
	 * Returns an {@link InfoApi} reference.
	 * @return an {@link InfoApi} reference
	 */
	InfoApi getInfoApi();
	
	/**
	 * Returns a {@link LanguageApi} reference.
	 * @return a {@link LanguageApi} reference
	 */
	LanguageApi getLanguageApi();
	
	/**
	 * Returns a {@link CallbackApi} reference.
	 * @return a {@link CallbackApi} reference
	 */
	CallbackApi getCallbackApi();
	
	/**
	 * Returns a {@link StarCraftIIApi} reference.
	 * @return a {@link StarCraftIIApi} reference
	 */
	StarCraftIIApi getStarCraftIIApi();
	
	/**
	 * Returns a {@link GeneralUtilsApi} reference.
	 * @return a {@link GeneralUtilsApi} reference
	 */
	GeneralUtilsApi getGeneralUtilsApi();
	
	/**
	 * Returns a {@link GuiUtilsApi} reference.
	 * @return a {@link GuiUtilsApi} reference
	 */
	GuiUtilsApi getGuiUtilsApi();
	
	/**
	 * Returns a {@link SoundsApi} reference.
	 * @return a {@link SoundsApi} reference
	 */
	SoundsApi getSoundsApi();
	
	/**
	 * Returns a {@link MousePrintRecorderApi} reference.
	 * @return a {@link MousePrintRecorderApi} reference
	 * @since "2.0"
	 */
	MousePrintRecorderApi getMousePrintRecorderApi();
	
	/**
	 * Returns a {@link ReplayFactoryApi} reference.
	 * @return a {@link ReplayFactoryApi} reference
	 * @since "2.0"
	 */
	ReplayFactoryApi getReplayFactoryApi();
	
	/**
	 * Returns a {@link ReplayUtilsApi} reference.
	 * @return a {@link ReplayUtilsApi} reference
	 * @since "2.0"
	 */
	ReplayUtilsApi getReplayUtilsApi();
	
	/**
	 * Returns an {@link IconsApi} reference.
	 * @return an {@link IconsApi} reference
	 * @since "2.0"
	 */
	IconsApi getIconsApi();
	
	/**
	 * Returns an {@link ProfileApi} reference.
	 * @return an {@link ProfileApi} reference
	 * @since "2.0"
	 */
	ProfileApi getProfileApi();
	
	/**
	 * Returns an {@link EapmUtilsApi} reference.
	 * @return an {@link EapmUtilsApi} reference
	 * @since "2.5"
	 */
	EapmUtilsApi getEapmUtilsApi();
	
}
