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

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;
import hu.belicza.andras.sc2gearspluginapi.api.enums.MiscObject;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.AbilityGroup;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Research;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Upgrade;

/**
 * Defines services to access icons of different objects.
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GeneralServices
 */
public interface IconsApi {
	
	/** Interface version. */
	String VERSION = "2.6";
	
	/**
	 * Returns the icon of the specified building in the specified size.
	 * @param building building whose icon to be returned
	 * @param size     size in which the icon to be returned
	 * @return the icon of the specified building in the specified size
	 * @see Building
	 * @see IconSize
	 */
	Icon getBuildingIcon( Building building, IconSize size );
	
	/**
	 * Returns the icon of the specified unit in the specified size.
	 * @param unit unit whose icon to be returned
	 * @param size size in which the icon to be returned
	 * @return the icon of the specified unit in the specified size
	 * @see Unit
	 * @see IconSize
	 */
	Icon getUnitIcon( Unit unit, IconSize size );
	
	/**
	 * Returns the icon of the specified upgrade in the specified size.
	 * @param upgrade upgrade whose icon to be returned
	 * @param size    size in which the icon to be returned
	 * @return the icon of the specified upgrade in the specified size
	 * @see Upgrade
	 * @see IconSize
	 */
	Icon getUpgradeIcon( Upgrade upgrade, IconSize size );
	
	/**
	 * Returns the icon of the specified research in the specified size.
	 * @param research research whose icon to be returned
	 * @param size     size in which the icon to be returned
	 * @return the icon of the specified research in the specified size
	 * @see Research
	 * @see IconSize
	 */
	Icon getResearchIcon( Research research, IconSize size );
	
	/**
	 * Returns the icon of the specified ability group in the specified size.
	 * @param abilityGroup ability group whose icon to be returned, can be <code>null</code>
	 * @param size         size in which the icon to be returned
	 * @return the icon of the specified ability group in the specified size
	 * @see AbilityGroup
	 * @see IconSize
	 */
	Icon getAbilityGroupIcon( AbilityGroup abilityGroup, IconSize size );
	
	/**
	 * Returns the icon of the specified misc object in the specified size.
	 * @param miscObject misc object whose icon to be returned, can be <code>null</code>
	 * @param size       size in which the icon to be returned
	 * @return the icon of the specified misc object in the specified size
	 * @see MiscObject
	 * @see IconSize
	 */
	Icon getMiscObjectIcon( MiscObject miscObject, IconSize size );
	
	/**
	 * Returns a custom enlarged icon.
	 * @param name name of the custom enlarged icon to be returned, can be <code>null</code>
	 * @param size size in which the icon to be returned
	 * @return a custom enlarged icon in the specified size
	 * @since "2.6"
	 * @see IconSize
	 */
	Icon getCustomEnlargedIcon( String name, IconSize size );
	
	/**
	 * Returns the icon of an entity in the specified size, be it either a building, unit, research, upgrade, ability group,
	 * misc object or a custom enlarged icon name.
	 * 
	 * @param entity entity whose icon to be returned
	 * @param size   size in which the icon to be returned
	 * @return the icon of an entity in the specified size
	 * @see IconSize
	 * @see #getBuildingIcon(ReplayConsts.Building, IconSize)
	 * @see #getUnitIcon(ReplayConsts.Unit, IconSize)
	 * @see #getResearchIcon(ReplayConsts.Research, IconSize)
	 * @see #getUpgradeIcon(ReplayConsts.Upgrade, IconSize)
	 * @see #getAbilityGroupIcon(ReplayConsts.AbilityGroup, IconSize)
	 * @see #getMiscObjectIcon(MiscObject, IconSize)
	 * @see #getCustomEnlargedIcon(String, IconSize)
	 */
	Icon getEntityIcon( Object entity, IconSize size );
	
	/**
	 * Returns the icon of the specified race.
	 * @param race race whose icon to be returned
	 * @return the icon of the specified race
	 * @see Race
	 */
	ImageIcon getRaceIcon( Race race );
	
	/**
	 * Returns the icon resource of the specified race.
	 * @param race race whose icon resource to be returned
	 * @return the icon resource of the specified race
	 * @see Race
	 */
	URL getRaceIconResource( Race race );
	
	/**
	 * Returns a null icon with the specified size.
	 * @param width  width of the null icon
	 * @param height height of the null icon
	 * @return a null icon with the specified size
	 */
	Icon getNullIcon( int width, int height );
	
	/**
	 * Returns an icon with custom size with another icon centered in it.
	 * @param anotherIcon another icon to be centered in it
	 * @param width       width of the icon
	 * @param height      height of the icon
	 * @param zoom        zoom factor of <code>anotherIcon</code> 
	 * @return an icon with custom size with another icon centered in it
	 */
	Icon getCustomIcon( ImageIcon anotherIcon, int width, int height, int zoom );
	
	/**
	 * Returns a portrait loading icon.
	 * @return a portrait loading icon
	 */
	Icon getPortraitLoadingIcon();
	
	/**
	 * Returns a portrait NA (not available) icon.
	 * @return a portrait NA (not available) icon
	 */
	Icon getPortraitNAIcon();
	
	/**
	 * Returns a computer portrait icon.
	 * @return a computer portrait icon
	 */
	Icon getPortraitComputerIcon();
	
	/**
	 * Returns a high-res portrait loading icon.
	 * @return a high-res portrait loading icon
	 */
	Icon getPortraitHighLoadingIcon();
	
	/**
	 * Returns a high-res portrait NA (not available) icon.
	 * @return a high-res portrait NA (not available) icon
	 */
	Icon getPortraitHighNAIcon();
	
	/**
	 * Returns a league loading icon.
	 * @return a league loading icon
	 */
	Icon getLeagueLoadingIcon();
	
	/**
	 * Returns a league NA (not available) icon.
	 * @return a league NA (not available) icon
	 */
	Icon getLeagueNAIcon();
	
	/**
	 * Returns the icon of the specified portrait.
	 * @param group  portrait group
	 * @param row    row in the portrait group
	 * @param column column in the portrait group
	 * @return the icon of the specified portrait; or a null portrait icon if no icon for the specified portrait
	 * @see #getPortraitHighIcon(int, int, int)
	 */
	Icon getPortraitIcon( int group, int row, int column );
	
	/**
	 * Returns the high-resolution icon of the specified portrait.
	 * @param group  portrait group
	 * @param row    row in the portrait group
	 * @param column column in the portrait group
	 * @return the high-resolution icon of the specified portrait; or a high-res null portrait icon if no icon for the specified portrait
	 * @see #getPortraitIcon(int, int, int)
	 */
	Icon getPortraitHighIcon( int group, int row, int column );
	
}
