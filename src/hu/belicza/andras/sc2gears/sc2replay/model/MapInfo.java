/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.sc2replay.model;

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IMapInfo;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.MapObject;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;

import java.awt.Point;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * Map info of the replay.
 * 
 * <p>For format description see:
 * <ul><li><a href='http://code.google.com/p/vgce/source/browse/trunk/docs/Blizzard/Starcraft%20II/MapInfo.txt'>MapInfo.txt</a></ul></p>
 * 
 * @author Andras Belicza
 */
public class MapInfo implements IMapInfo {
	
	/** Width of the map.  */
	public int width;
	/** Height of the map. */
	public int height;
	
	/** Left boundary of the playable size of the map. */
	public int boundaryLeft;
	/** Bottom boundary of the playable size of the map. */
	public int boundaryBottom;
	/** Right boundary of the playable size of the map. */
	public int boundaryRight;
	/** Top boundary of the playable size of the map. */
	public int boundaryTop;
	
	/** A preview of the map. */
	public ImageIcon previewIcon;
	
	/** Objects on the map. */
	public List< Pair< MapObject, Point > > mapObjectList;
	
	/** Start locations on the map. */
	public List< Point > startLocationList;
	
	/** Map attributes. Maps from locale to map of (attribute name=>attribute value) pairs. */
	public Map< String, Map< String, String > > localeAttributeMapMap;
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	public int getBoundaryLeft() {
		return boundaryLeft;
	}
	
	@Override
	public int getBoundaryBottom() {
		return boundaryBottom;
	}
	
	@Override
	public int getBoundaryRight() {
		return boundaryRight;
	}
	
	@Override
	public int getBoundaryTop() {
		return boundaryTop;
	}
	
	@Override
	public ImageIcon getPreviewIcon() {
		return previewIcon;
	}
	
	@Override
	public List< Pair< MapObject, Point > > getMapObjectList() {
		return mapObjectList;
	}
	
	@Override
	public List< Point > getStartLocationList() {
		return startLocationList;
	}
	
    @Override
	public String getSizeString() {
		return width + "x" + height;
	}
	
    @Override
	public String getPlayableSizeString() {
		return ( boundaryRight - boundaryLeft ) + "x" + ( boundaryTop - boundaryBottom );
	}
    
    @Override
    public Map< String, Map< String, String > > getLocaleAttributeMapMap() {
	    return localeAttributeMapMap;
    }
    
    @Override
    public String getAttribute( final String locale, final String name ) {
    	if ( localeAttributeMapMap == null )
    		return null;
    	
    	final Map< String, String > attributeMap = getLocaleAttributeMapMap().get( locale );
    	if ( attributeMap == null )
    		return null;
    	
	    return attributeMap.get( name );
    }
    
    @Override
    public String getNameAttribute( final String locale ) {
	    return getAttribute( locale, "DocInfo/Name" );
    }
    
    @Override
    public String getAuthorAttribute( final String locale ) {
	    return getAttribute( locale, "DocInfo/Author" );
    }
    
    @Override
    public String getShortDescAttribute( final String locale ) {
	    return getAttribute( locale, "DocInfo/DescShort" );
    }
	
    @Override
    public String getLongDescAttribute( final String locale ) {
	    return getAttribute( locale, "DocInfo/DescLong" );
    }
    
}
