/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.sc2map;

import hu.belicza.andras.mpq.MpqParser;
import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent;
import hu.belicza.andras.sc2gears.sc2replay.model.MapInfo;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.MoveScreenAction;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.MapObject;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Sc2 map parser.
 * 
 * @author Andras Belicza
 */
public class MapParser {
	
	/**
	 * Parses the map info ("MapInfo") of a map.
	 * @param replay reference to the replay
	 */
	public static void parseMapInfo( final Replay replay ) {
		final File mapFile = getMapFile( replay );
		if ( mapFile== null || !mapFile.exists() )
			return;
		
		try ( final MpqParser mpqParser = new MpqParser( mapFile.getAbsolutePath() ) ) {
			final byte[] mapInfoData = mpqParser.getFile( ReplayContent.MAP_INFO.hash1, ReplayContent.MAP_INFO.hash2, ReplayContent.MAP_INFO.hash3 );
			final ByteBuffer wrapper = ByteBuffer.wrap( mapInfoData ).order( ByteOrder.LITTLE_ENDIAN );
			if ( wrapper.getInt() != 0x4d617049 ) // "IpaM" ("MapI" reversed)
				return;
			
			final MapInfo mapInfo = new MapInfo();
			
			final int version = wrapper.getInt();
			if ( version > 0x17 )
				wrapper.position( wrapper.position() + 8 ); // 2x unknown int
			mapInfo.width  = wrapper.getInt();
			mapInfo.height = wrapper.getInt();
			int value;
			do {
				value = wrapper.getInt();
				switch ( value ) {
				case 0x001 : break; // do nothing
				case 0x002 : while ( wrapper.get() != 0 ); break; // 0 char terminated string
				case 0x100 : wrapper.get(); break; // 1 extra byte? seen in version 0x1f
				case 0x400 : wrapper.get(); break; // 1 extra byte? seen in version 0x20
				case 0x000 :
					while ( wrapper.get() != 0 ) ; // Theme
					while ( wrapper.get() != 0 ) ; // Planet
					break;
				}
			} while ( value != 0 );
			
			mapInfo.boundaryLeft   = wrapper.getInt();
			mapInfo.boundaryBottom = wrapper.getInt();
			mapInfo.boundaryRight  = wrapper.getInt();
			mapInfo.boundaryTop    = wrapper.getInt();
			replay.details.mapPreviewFileName = "Minimap.tga";
			
			// Most common map preview name is "Minimap.tga", we already cached its hashes
			if ( replay.details.mapPreviewFileName.equals( ReplayContent.MINIMAP_TGA.fileName ) )
				mapInfo.previewIcon = ReplayUtils.parseTgaImage( mpqParser.getFile( ReplayContent.MINIMAP_TGA.hash1, ReplayContent.MINIMAP_TGA.hash2, ReplayContent.MINIMAP_TGA.hash3 ) );
			else
				mapInfo.previewIcon = ReplayUtils.parseTgaImage( mpqParser.getFile( replay.details.mapPreviewFileName ) );
			
			// Map parsing was successful, set it to the replay
			replay.mapInfo = mapInfo;
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses the map attributes ("DocumentHeader") of a map.
	 * 
	 * <p><i>This method can only be called if {@link ReplayContent#MAP_INFO} has already been parsed.</i></p>
	 * 
	 * @param replay reference to the replay
	 */
	public static void parseMapAttributes( final Replay replay ) {
		final File mapFile = getMapFile( replay );
		if ( mapFile== null || !mapFile.exists() )
			return;
		
		try ( final MpqParser mpqParser = new MpqParser( mapFile.getAbsolutePath() ) ) {
			final Map< String, Map< String, String > > localeAttributeMapMap = new HashMap< String, Map<String,String> >();
			
			final byte[] docHeaderData = mpqParser.getFile( ReplayContent.MAP_ATTRIBUTES.hash1, ReplayContent.MAP_ATTRIBUTES.hash2, ReplayContent.MAP_ATTRIBUTES.hash3 );
			final ByteBuffer wrapper = ByteBuffer.wrap( docHeaderData ).order( ByteOrder.LITTLE_ENDIAN );
			
    		wrapper.position( 44 ); // 44 byte header
    		final int dependenciesCount = wrapper.getInt();
    		for ( int i = 0; i < dependenciesCount; i++ )
    			while ( wrapper.get() != 0 )
    				;  // Dependency strings (0-char terminated)
    		
    		final int pairsCount = wrapper.getInt();
    		
    		byte[] buffer;
			final char[] localeChars = new char[ 5 ];
			localeChars[ 2 ] = '-';
    		for ( int i = 0; i < pairsCount; i++ ) {
    			wrapper.get( buffer = new byte[ wrapper.getShort() ] ); // Key length and value
    			final String key = new String( buffer, Consts.UTF8 );
    			// Locale (country+locale) reversed, example: "SUne" => "USen"
    			localeChars[ 1 ] = (char) wrapper.get();
    			localeChars[ 0 ] = (char) wrapper.get();
    			localeChars[ 4 ] = (char) wrapper.get();
    			localeChars[ 3 ] = (char) wrapper.get();
    			final String locale = new String( localeChars );
    			wrapper.get( buffer = new byte[ wrapper.getShort() ] ); // Value length and value
    			final String value = new String( buffer, Consts.UTF8 ).replace( "<n/>", "\n" );
    			Map< String, String > attributeMap = localeAttributeMapMap.get( locale );
    			if ( attributeMap == null )
    				localeAttributeMapMap.put( locale, attributeMap = new HashMap< String, String >() );
    			attributeMap.put( key, value );
    		}
    		
    		// Attribute parsing was successful, set it to the replay
    		replay.mapInfo.localeAttributeMapMap = localeAttributeMapMap;
    		
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses extended map info required for the Map view chart.
	 * 
	 * <p><i>This method can only be called if {@link ReplayContent#MAP_INFO} has already been parsed.</i></p>
	 * 
	 * @param replay reference to the replay
	 */
	public static void parseExtendedMapInfo( final Replay replay ) {
		final File mapFile = getMapFile( replay );
		if ( mapFile == null || !mapFile.exists() )
			return;
		
		try ( final MpqParser mpqParser = new MpqParser( mapFile.getAbsolutePath() ) ) {
			final byte[] mapObjectsData = mpqParser.getFile( ReplayContent.MAP_OBJECTS.hash1, ReplayContent.MAP_OBJECTS.hash2, ReplayContent.MAP_OBJECTS.hash3 );
			
			final Document mapObjectsDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new ByteArrayInputStream( mapObjectsData ) );
			final Element docElement = mapObjectsDocument.getDocumentElement();
			
			// Objects of the map
			final List< Pair< MapObject, Point > > mapObjectList = new ArrayList< Pair< MapObject,Point > >( 128 );
			final NodeList unitList = docElement.getElementsByTagName( "ObjectUnit" );
			for ( int i = unitList.getLength() - 1; i >= 0; i-- ) {
				final Element unit = (Element) unitList.item( i );
				final MapObject mapObject = MapObject.fromBinaryValue( unit.getAttribute( "UnitType" ) );
				if ( mapObject != null )
					mapObjectList.add( new Pair< MapObject, Point >( mapObject, parsePoint( unit.getAttribute( "Position" ) ) ) );
			}
			// Object parsing was successful, set it to the replay
			replay.mapInfo.mapObjectList = mapObjectList;
			
			// Start locations on the map
			final List< Point > startLocationList = new ArrayList< Point >();
			final NodeList startLocList = docElement.getElementsByTagName( "ObjectPoint" );
			for ( int i = startLocList.getLength() - 1; i >= 0; i-- ) {
				final Element startLoc = (Element) startLocList.item( i );
				if ( "StartLoc".equals( startLoc.getAttribute( "Type" ) ) )
					startLocationList.add( parsePoint( startLoc.getAttribute( "Position" ) ) );
			}
			// Object parsing was successful, set it to the replay
			replay.mapInfo.startLocationList = startLocationList;
			
			// Figure out players' start locations based on their first move screen action (assuming the closest start location)
			if ( startLocationList.size() > 0 ) {
				final MoveScreenAction[] firstMSActions = new MoveScreenAction[ replay.details.players.length ];
				final Action[] actions = replay.gameEvents.actions;
				final int actionsLength = actions.length;
				Action action;
				int foundCounter = 0;
				for ( int i = 0; i < actionsLength; i++ )
					if ( ( action = actions[ i ] ) instanceof MoveScreenAction && firstMSActions[ action.player ] == null ) {
						firstMSActions[ action.player ] = (MoveScreenAction) action;
						if ( ++foundCounter == firstMSActions.length )
							break;
					}
				MoveScreenAction msa;
				for ( int i = firstMSActions.length - 1; i >= 0; i-- )
					if ( ( msa = firstMSActions[ i ] ) != null ) {
						int distanceSquare = Integer.MAX_VALUE;
						Point startLoc = null;
						for ( final Point startLoc_ : startLocationList ) {
							final int distanceSquare_ = GeneralUtils.distanceSquare( msa.x << 8, msa.y << 8, startLoc_.x, startLoc_.y );
							if ( distanceSquare_ < distanceSquare ) {
								distanceSquare = distanceSquare_;
								startLoc       = startLoc_;
							}
						}
						replay.details.players[ i ].startLocation = startLoc;
					}
			}
			
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses a point in the sc2 map specified by it's x, y coordinates.
	 * @param positionString string containing x, y coordinates of the point
	 * @return the parsed point scaled to the sc2 coordinate space (multiplied by 65536)
	 */
	private static Point parsePoint( final String positionString ) {
		final int commaIndex = positionString.indexOf( ',' );
		return new Point( (int) ( Float.parseFloat( positionString.substring( 0, commaIndex ) ) * 65536 ),
				(int) ( ( Float.parseFloat( positionString.substring( commaIndex + 1, positionString.indexOf( ',', commaIndex + 1 ) ) ) ) * 65536 ) );
	}
	
	/**
	 * Returns a Java file object representing the map file of the specified replay.
	 * @param replay replay whose map file to be returned
	 * @return a Java file object representing the map file of the specified replay
	 */
	public static File getMapFile( final Replay replay ) {
		if ( replay.initData.mapFileName == null )
			return null;
		return new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_SC2_MAPS ), replay.initData.mapFileName );
	}
	
}
