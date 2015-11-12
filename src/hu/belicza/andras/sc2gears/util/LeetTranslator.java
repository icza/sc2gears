/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.util;

import hu.belicza.andras.sc2gears.language.Language;


/**
 * Text to Leet Translator.
 * 
 * <p>Based on: <a href="http://www.albinoblacksheep.com/text/leet">http://www.albinoblacksheep.com/text/leet</a></p>
 * 
 * @author Andras Belicza
 */
public class LeetTranslator {
	
	/**
	 * Leetness level.
	 * @author Andras Belicza
	 */
	public static enum LeetnessLevel {
		/** Disabled, leetless.                         */
		DISABLED    ( "menu.settings.language.leetTranslation.leetnessLevel.disabled"     ),
		/** Basic, 4 characters are translated.         */
		BASIC       ( "menu.settings.language.leetTranslation.leetnessLevel.basic"        ),
		/** Intermediate, 10 characters are translated. */
		INTERMEDIATE( "menu.settings.language.leetTranslation.leetnessLevel.intermediate" ),
		/** Advanced, 15 characters are translated.     */
		ADVANCED    ( "menu.settings.language.leetTranslation.leetnessLevel.advanced"     ),
		/** Expert, 26 characters are translated.       */
		EXPERT      ( "menu.settings.language.leetTranslation.leetnessLevel.expert"       );
		
		/** key of the text representation.    */
		private final String textKey;
		/** Sample text of the leetness level. */
		private       String sampleText;
		/** Cache of the string value.         */
		private String       stringValue;
		
		/**
		 * Creates a new LeetnessLevel.
		 * @param textKey key of the text representation
		 */
		private LeetnessLevel( final String textKey ) {
			// stringValue cannot be initialized here, because Language references this class, Language is not yet initialized!
			this.textKey = textKey;
		}
		
		public String getSampleText() {
			if ( sampleText == null )
				sampleText = translate( Language.getOriginalText( "menu.settings.language.leetTranslation.leetnessLevel.sampleText" ), this );
			
			return sampleText;
		}
		
		@Override
		public String toString() {
			if ( stringValue == null )
				stringValue = Language.getText( textKey );
			
			return stringValue;
		}
	}
	
	/**
	 * Translates a text into Leet.
	 * @param text  text to be translated
	 * @param level Leetness level
	 * @return the translated Leet text
	 */
	public static String translate( final String text, final LeetnessLevel level ) {
		if ( text == null || level == LeetnessLevel.DISABLED )
			return text;
		
		final StringBuilder t3xtBuilder = new StringBuilder();
		
		final int n = text.length();
		for ( int i = 0; i < n; i++ ) {
			final char ch = text.charAt( i );
			boolean original = false;
			
			switch ( ch ) {
			// BASIC translation
			case 'a' : case 'A' : t3xtBuilder.append( '4' ); break;
			case 'e' : case 'E' : t3xtBuilder.append( '3' ); break;
			case 'i' : case 'I' : t3xtBuilder.append( '1' ); break;
			case 'o' : case 'O' : t3xtBuilder.append( '0' ); break;
			default :
				if ( level.compareTo( LeetnessLevel.INTERMEDIATE ) >= 0 ) {
					switch ( ch ) {
					case 'c' : case 'C' : t3xtBuilder.append( '(' ); break;
					case 'd' : case 'D' : t3xtBuilder.append( 'Ð' ); break;
					case 'l' : case 'L' : t3xtBuilder.append( '£' ); break;
					case 's' : case 'S' : t3xtBuilder.append( '$' ); break;
					case 'u' : case 'U' : t3xtBuilder.append( 'µ' ); break;
					case 'y' : case 'Y' : t3xtBuilder.append( '¥' ); break;
					default :
						if ( level.compareTo( LeetnessLevel.ADVANCED ) >= 0 ) {
							switch ( ch ) {
							case 'f' : case 'F' : t3xtBuilder.append( 'ƒ'  ); break;
							case 'g' : case 'G' : t3xtBuilder.append( '9'  ); break;
							case 'k' : case 'K' : t3xtBuilder.append( "|{" ); break;
							case 't' : case 'T' : t3xtBuilder.append( '7'  ); break;
							case 'z' : case 'Z' : t3xtBuilder.append( '2'  ); break;
							default :
								if ( level.compareTo( LeetnessLevel.EXPERT ) >= 0 ) {
									switch ( ch ) {
									case 'b' : case 'B' : t3xtBuilder.append( 'ß'      ); break;
									case 'h' : case 'H' : t3xtBuilder.append( "|-|"    ); break;
									case 'j' : case 'J' : t3xtBuilder.append( "_|"     ); break;
									case 'n' : case 'N' : t3xtBuilder.append( "|\\|"   ); break;
									case 'm' : case 'M' : t3xtBuilder.append( "|\\/|"  ); break;
									case 'p' : case 'P' : t3xtBuilder.append( "|°"     ); break;
									case 'q' :            t3xtBuilder.append( '¶'      ); break;
									case 'Q' :            t3xtBuilder.append( "¶¸"     ); break;
									case 'r' : case 'R' : t3xtBuilder.append( '®'      ); break;
									case 'v' : case 'V' : t3xtBuilder.append( "\\/"    ); break;
									case 'w' : case 'W' : t3xtBuilder.append( "\\/\\/" ); break;
									case 'x' : case 'X' : t3xtBuilder.append( ")("     ); break;
									default  : original = true; break;
									}
								}
								else
									original = true;
								break;
							}
						}
						else
							original = true;
						break;
					}
				}
				else
					original = true;
				break;
			}
			
			if ( original )
				t3xtBuilder.append( ch );
		}
		
		return t3xtBuilder.toString();
	}
	
}
