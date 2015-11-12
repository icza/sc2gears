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
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;

import java.util.Date;

/**
 * Defines language or locale specific services.
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GeneralServices
 */
public interface LanguageApi {
	
	/** Interface version. */
	String VERSION = "2.0";
	
	/**
	 * Returns the current language.
	 * @return the current language
	 */
	String getLanguage();
	
	/**
	 * Formats a date to the proper language format.
	 * @param date date to be formatted
	 * @return the formatted date
	 * @see #formatDateTime(Date)
	 * @see #formatTime(Date)
	 */
	String formatDate( Date date );
	
	/**
	 * Formats a date and time to the proper language format.
	 * @param dateTime date and time to be formatted
	 * @return the formatted date and time
	 * @see #formatDate(Date)
	 * @see #formatTime(Date)
	 */
	String formatDateTime( Date dateTime );
	
	/**
	 * Formats a time to the proper language format.
	 * @param time time to be formatted
	 * @return the formatted time
	 * @see #formatDate(Date)
	 * @see #formatDateTime(Date)
	 */
	String formatTime( Date time );
	
	/**
	 * Concatenates a person's first and last name in the order of the language setting.
	 * @param firstName first name of the person
	 * @param lastName  last name of the person
	 * @return the properly formatted name of the person
	 */
	String formatPersonName( String firstName, String lastName );
	
	/**
	 * Parses and returns a date.
	 * @param dateString date string to parse from
	 * @return the parsed date, or null if dateString is invalid
	 * @see #parseDateTime(String, boolean)
	 */
	Date parseDate( String dateString );
	
	/**
	 * Parses and returns a date and time
	 * @param dateTimeString date time string to parse from
	 * @param silent         indicates if no stack trace should be printed in case of errors
	 * @return the parsed date and time, or null if dateTimeString is invalid
	 * @see #parseDate(String)
	 */
	Date parseDateTime( String dateTimeString, boolean silent );
	
	/**
	 * Returns the text associated with the given key.
	 * @param key key of the text to be returned
	 * @return the text associated with the given key
	 * @since "2.0"
	 * @see #getText(String, Object...)
	 */
	String getText( String key );
	
	/**
	 * Returns the original text associated with the given key.<br>
	 * Original means without the Leet translation.
	 * @param key key of the text to be returned
	 * @return the original text associated with the given key
	 * @since "2.0"
	 * @see #getText(String)
	 */
	String getOriginalText( String key );
	
	/**
	 * Returns the default text associated with the given key.<br>
	 * Default means the text of the default (English) language.
	 * @param key key of the text to be returned
	 * @return the default text associated with the given key
	 * @since "2.0"
	 * @see #getText(String)
	 */
	String getDefaultText( String key );
	
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
	 * 
	 * @since "2.0"
	 * @see #getText(String)
	 */
	String getText( String key, Object... arguments );
	
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
	 * @since "2.0"
	 * @see #getText(String, Object...)
	 */
	Pair< String, Character > getTextAndMnemonic( String key, Object... arguments );
	
}
