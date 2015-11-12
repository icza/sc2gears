/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.language;

import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Language face.
 * 
 * @author Andras Belicza
 */
public class Language {
	
	/** Time format to be used to format dates and times.          */
	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat( "HH:mm:ss" );
	
	/** Name of the text group tag.                  */
	public static final String TEXT_GROUP_TAG_NAME                  = "g";
	/** Name of the text tag.                        */
	public static final String TEXT_TAG_NAME                        = "t";
	/** Name of the key attribute.                   */
	public static final String KEY_ATTRIBUTE_NAME                   = "k";
	
	/**
	 * Internal map to store the texts.<br>
	 * The <code>key</code> is a unique text key, the <code>value</code> is the translation of the text denoted by the text key. 
	 */
	public static final Map< String, String > textMap = new HashMap< String, String >();
	
	static {
		// Load the default language
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments( true );
			final Document document = factory.newDocumentBuilder().parse( Language.class.getResourceAsStream( "English.xml" ) );
			
			final Element docElement = document.getDocumentElement();
			
			// Read texts
			readGroupsAndTexts( docElement, "" );
			
		} catch ( final Exception e ) {
			System.err.println( "Failed to load language file!" );
			e.printStackTrace( System.err );
		}
	}
	
	/**
	 * Reads the content of a node (of the Language XML file).
	 * 
	 * @param parentElement element to start reading from
	 * @param keyPrefix     prefix of the keys to prepend to read keys
	 */
	private static void readGroupsAndTexts( final Element parentElement, String keyPrefix ) {
		if ( keyPrefix.length() > 0 )
			keyPrefix = keyPrefix + '.';
		
		final NodeList childNodeList = parentElement.getChildNodes();
		
		final int childerCount = childNodeList.getLength();
		for ( int i = 0; i < childerCount; i++ ) {
			final Node childNode = childNodeList.item( i );
			if ( childNode instanceof Element ) {
				final Element childElement = (Element) childNode;
				
				if ( TEXT_TAG_NAME.equals( childElement.getTagName() ) )
					textMap.put( keyPrefix + childElement.getAttribute( KEY_ATTRIBUTE_NAME ), childElement.getTextContent() );
				else if ( TEXT_GROUP_TAG_NAME.equals( childElement.getTagName() ) )
					readGroupsAndTexts( childElement, keyPrefix + childElement.getAttribute( KEY_ATTRIBUTE_NAME ) );
			}
		}
	}
	
	/**
	 * Returns the text associated with the given key.
	 * @param key key of the text to be returned
	 * @return the text associated with the given key
	 */
	public static String getText( final String key ) {
		return textMap.get( key );
	}
	
	/**
	 * Returns the text associated with the given key.
	 * 
	 * <p>The returned text might contain parameters which first will be substituted with the values specified.<br>
	 * Parameters are marked with <code>$x</code> where <code>x</code> is the number of the parameter to be inserted (starting from 0).<br>
	 * The value to be inserted is determined by the <code>toString()</code> method of the argument.<br>
	 * If you want to have a <code>$</code> sign in the returned text, you have to pass a character parameter with a value of <code>'$'</code> (or as a string).</p>
	 * 
	 * <p>The method only handles a maximum of 10 parameters (0..9)!</p>
	 * 
	 * <p><b>Example</b><br>
	 * Let's assume we have a text with a key of <code>"somekey"</code> and with a value of<br>
	 * <code>"Hello $1, this is a $0 day! You have $2$3."</code>.<br>
	 * The following call:<br>
	 * <code>getText( "somekey", "fine", "Mr. Hunter", '$', 10 )</code><br>
	 * will return the following text:<br>
	 * <code>"Hello Mr. Hunter, this is a fine day! You have $10."</code></p>
	 * 
	 * @param key key of the text to be returned
	 * @param arguments substitutable parameters
	 * @return the text associated with the given key
	 */
	public static String getText( final String key, final Object... arguments ) {
		String text = textMap.get( key );
		
		for ( int i = 0; i < arguments.length; i++ ) 
			text = text.replace( "$" + i, arguments[ i ].toString() );
		
		return text;
	}
	
	/**
	 * Returns the text associated with the given key and an optional mnemonic defined by the text.
	 * 
	 * <p>If the text has a mnemonic marked by the '_' char in its text,
	 * it will be removed and the next character returned as the mnemonic character.</p>
	 * 
	 * @param key key of the text to be returned
	 * @param arguments substitutable parameters
	 * @return the text associated with the given key
	 * 
	 * @see #getText(String, Object...)
	 */
	public static Pair< String, Character > getTextAndMnemonic( final String key, final Object... arguments ) {
		String text = textMap.get( key );
		
		final int mnemonicIndex = text.indexOf( '_' );
		Character mnemonic;
		if ( mnemonicIndex < 0 )
			mnemonic = null;
		else {
			text = text.replace( "_", "" );
			mnemonic = text.charAt( mnemonicIndex );
		}
		
		for ( int i = 0; i < arguments.length; i++ ) 
			text = text.replace( "$" + i, arguments[ i ].toString() );
		
		return new Pair< String, Character >( text, mnemonic );
	}
	
	/**
	 * Formats a time to the proper language format.
	 * @param time time to be formatted
	 * @return the formatted time
	 */
	public static String formatTime( final Date time ) {
		return TIME_FORMAT.format( time );
	}
	
}
