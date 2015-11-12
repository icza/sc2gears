/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.sc2replay;

import hu.belicza.andras.sc2gearspluginapi.api.ReplayFactoryApi;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.MapObject;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;

import java.awt.Point;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * Map info interface of StarCraft II replays.
 * 
 * <p>Map info is read from the StarCraft II map files not from StarCraft II replays.</p>
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see IReplay
 */
public interface IMapInfo {
	
	/** Interface version. */
	String VERSION = "2.7";
	
	/**
	 * Returns the width of the map.
	 * @return the width of the map
	 */
	int getWidth();
	
	/**
	 * Returns the height of the map.
	 * @return the height of the map
	 */
	int getHeight();
	
	/**
	 * Returns the left boundary of the playable area of the map.
	 * @return the left boundary of the playable area of the map
	 */
	int getBoundaryLeft();
	
	/**
	 * Returns the bottom boundary of the playable area of the map.
	 * @return the bottom boundary of the playable area of the map
	 */
	int getBoundaryBottom();
	
	/**
	 * Returns the right boundary of the playable area of the map.
	 * @return the right boundary of the playable area of the map
	 */
	int getBoundaryRight();
	
	/**
	 * Returns the top boundary of the playable area of the map.
	 * @return the top boundary of the playable area of the map
	 */
	int getBoundaryTop();
	
	/**
	 * Returns the map preview.
	 * @return the map preview
	 */
	ImageIcon getPreviewIcon();
	
	/**
	 * Returns the list of map objects.
	 * 
	 * <p>This will return <code>null</code> if extended map info is not parsed
	 * (see {@link ReplayFactoryApi#parseReplay(String, java.util.Set)}).</p>
	 * 
	 * @return the list of map objects
	 * @see MapObject
	 */
	List< Pair< MapObject, Point > > getMapObjectList();
	
	/**
	 * Returns the list of start locations of the map.
	 * 
	 * <p>This will return <code>null</code> if extended map info is not parsed
	 * (see {@link ReplayFactoryApi#parseReplay(String, java.util.Set)}).</p>
	 * 
	 * @return the list of start locations of the map
	 * @see IPlayer#getStartLocation()
	 */
	List< Point > getStartLocationList();
	
	/**
	 * Returns the size of the map in a format of WxH.
	 * @return the size of the map in a format of WxH
	 */
	String getSizeString();
	
	/**
	 * Returns the playable size of the map in a format of WxH.
	 * @return the playable size of the map in a format of WxH
	 */
	String getPlayableSizeString();	
	
	/**
	 * Returns the map attributes.
	 * 
	 * <p>Maps from locale to map of (attribute name=>attribute value) pairs.<br>
	 * <b>Locale examples:</b> <code>"US-en", "DE-de", "ES-es", "MX-es", "FR-fr", "IT-it", "KR-ko", "PL-pl", "BR-pt", "RU-ru", "CN-zh", "TW-zh"</code>. Note that not locales might be present in a map.<br>
	 * <b>Attribute examples (key=value):</b>
	 * <code><ul><li>"DocInfo/Author" = "Blizzard Entertainment"
	 * 	<li>"DocInfo/Name" = "Xel'Naga Caverns"
	 * 	<li>"DocInfo/DescShort" = "1v1"
	 * 	<li>"DocInfo/DescLong" = "The natural expansion is vulnerable to attacks. Further expansions are even more difficult to defend. Use of the Xel'Naga Towers provides vision over a large portion of the battlefield."
	 * </ul></code></p>
	 * 
	 * <p>This will return <code>null</code> if map attributes are not parsed
	 * (see {@link ReplayFactoryApi#parseReplay(String, java.util.Set)}).</p>
	 * 
	 * @return the map attributes
	 * @since "2.7"
	 */
	Map< String, Map< String, String > > getLocaleAttributeMapMap();
	
	/**
	 * Returns the value of the specified map attribute.
	 * 
	 * <p>This method is a shorthand for <code>getLocaleAttributeMapMap().get( locale ).get( name )</code>.</p>
	 * 
	 * <p>This will return <code>null</code> if map attributes are not parsed
	 * (see {@link ReplayFactoryApi#parseReplay(String, java.util.Set)})
	 * or <code>locale</code> is not contained in it.</p>
	 * 
	 * @param locale locale in which to return the value of the specified attribute
	 * @param name name of the attribute whose value to be returned
	 * @return the value of the specified map attribute
	 * @see #getLocaleAttributeMapMap()
	 * @since "2.7"
	 */
	String getAttribute( String locale, String name );
	
	/**
	 * Returns the map name attribute value.
	 * 
	 * <p>This method is a shorthand for <code>getAttribute( locale, "DocInfo/Name" )</code>.</p>
	 * 
	 * <p>This will return <code>null</code> if map attributes are not parsed
	 * (see {@link ReplayFactoryApi#parseReplay(String, java.util.Set)})
	 * or <code>locale</code> is not contained in it.</p>
	 * 
	 * @param locale locale in which to return the map name
	 * @return the map name attribute value
	 * @see #getAttribute(String, String)
	 * @see #getLocaleAttributeMapMap()
	 * @since "2.7"
	 */
	String getNameAttribute( String locale );
	
	/**
	 * Returns the map author attribute value.
	 * 
	 * <p>This method is a shorthand for <code>getAttribute( locale, "DocInfo/Author" )</code>.</p>
	 * 
	 * <p>This will return <code>null</code> if map attributes are not parsed
	 * (see {@link ReplayFactoryApi#parseReplay(String, java.util.Set)})
	 * or <code>locale</code> is not contained in it.</p>
	 * 
	 * @param locale locale in which to return the map author
	 * @return the map author attribute value
	 * @see #getAttribute(String, String)
	 * @see #getLocaleAttributeMapMap()
	 * @since "2.7"
	 */
	String getAuthorAttribute( String locale );
	
	/**
	 * Returns the map short description attribute value.
	 * 
	 * <p>This method is a shorthand for <code>getAttribute( locale, "DocInfo/DescShort" )</code>.</p>
	 * 
	 * <p>This will return <code>null</code> if map attributes are not parsed
	 * (see {@link ReplayFactoryApi#parseReplay(String, java.util.Set)})
	 * or <code>locale</code> is not contained in it.</p>
	 * 
	 * @param locale locale in which to return the map short description
	 * @return the map short description attribute value
	 * @see #getAttribute(String, String)
	 * @see #getLocaleAttributeMapMap()
	 * @since "2.7"
	 */
	String getShortDescAttribute( String locale );
	
	/**
	 * Returns the map long description attribute value.
	 * 
	 * <p>This method is a shorthand for <code>getAttribute( locale, "DocInfo/DescLong" )</code>.</p>
	 * 
	 * <p>This will return <code>null</code> if map attributes are not parsed
	 * (see {@link ReplayFactoryApi#parseReplay(String, java.util.Set)})
	 * or <code>locale</code> is not contained in it.</p>
	 * 
	 * @param locale locale in which to return the map long description
	 * @return the map long description attribute value
	 * @see #getAttribute(String, String)
	 * @see #getLocaleAttributeMapMap()
	 * @since "2.7"
	 */
	String getLongDescAttribute( String locale );
	
}
