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

import hu.belicza.andras.sc2gears.Consts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Google Translator interface.
 * 
 * @author Andras Belicza
 */
public class GoogleTranslator {
	
	/**
	 * Available languages in the translator.
	 * @author Andras Belicza
	 */
	public enum Language {
		AUTO_DETECT( "" ),
		ENGLISH( "en" ),
		AFRIKAANS( "af" ),
		ALBANIAN( "sq" ),
		AMHARIC( "am" ),
		ARABIC( "ar" ),
		ARMENIAN( "hy" ),
		AZERBAIJANI( "az" ),
		BASQUE( "eu" ),
		BELARUSIAN( "be" ),
		BENGALI( "bn" ),
		BIHARI( "bh" ),
		BRETON( "br" ),
		BULGARIAN( "bg" ),
		BURMESE( "my" ),
		CATALAN( "ca" ),
		CHEROKEE( "chr" ),
		CHINESE( "zh" ),
		CHINESE_SIMPLIFIED( "zh-CN" ),
		CHINESE_TRADITIONAL( "zh-TW" ),
		CORSICAN( "co" ),
		CROATIAN( "hr" ),
		CZECH( "cs" ),
		DANISH( "da" ),
		DHIVEHI( "dv" ),
		DUTCH( "nl" ),  
		ESPERANTO( "eo" ),
		ESTONIAN( "et" ),
		FAROESE( "fo" ),
		FILIPINO( "tl" ),
		FINNISH( "fi" ),
		FRENCH( "fr" ),
		FRISIAN( "fy" ),
		GALICIAN( "gl" ),
		GEORGIAN( "ka" ),
		GERMAN( "de" ),
		GREEK( "el" ),
		GUJARATI( "gu" ),
		HAITIAN_CREOLE( "ht" ),
		HEBREW( "iw" ),
		HINDI( "hi" ),
		HUNGARIAN( "hu" ),
		ICELANDIC( "is" ),
		INDONESIAN( "id" ),
		INUKTITUT( "iu" ),
		IRISH( "ga" ),
		ITALIAN( "it" ),
		JAPANESE( "ja" ),
		JAVANESE( "jw" ),
		KANNADA( "kn" ),
		KAZAKH( "kk" ),
		KHMER( "km" ),
		KOREAN( "ko" ),
		KURDISH( "ku" ),
		KYRGYZ( "ky" ),
		LAO( "lo" ),
		LATIN( "la" ),
		LATVIAN( "lv" ),
		LITHUANIAN( "lt" ),
		LUXEMBOURGISH( "lb" ),
		MACEDONIAN( "mk" ),
		MALAY( "ms" ),
		MALAYALAM( "ml" ),
		MALTESE( "mt" ),
		MAORI( "mi" ),
		MARATHI( "mr" ),
		MONGOLIAN( "mn" ),
		NEPALI( "ne" ),
		NORWEGIAN( "no" ),
		OCCITAN( "oc" ),
		ORIYA( "or" ),
		PASHTO( "ps" ),
		PERSIAN( "fa" ),
		POLISH( "pl" ),
		PORTUGUESE( "pt" ),
		PORTUGUESE_PORTUGAL( "pt-PT" ),
		PUNJABI( "pa" ),
		QUECHUA( "qu" ),
		ROMANIAN( "ro" ),
		RUSSIAN( "ru" ),
		SANSKRIT( "sa" ),
		SCOTS_GAELIC( "gd" ),
		SERBIAN( "sr" ),
		SINDHI( "sd" ),
		SINHALESE( "si" ),
		SLOVAK( "sk" ),
		SLOVENIAN( "sl" ),
		SPANISH( "es" ),
		SUNDANESE( "su" ),
		SWAHILI( "sw" ),
		SWEDISH( "sv" ),
		SYRIAC( "syr" ),
		TAJIK( "tg" ),
		TAMIL( "ta" ),
		TATAR( "tt" ),
		TELUGU( "te" ),
		THAI( "th" ),
		TIBETAN( "bo" ),
		TONGA( "to" ),
		TURKISH( "tr" ),
		UKRAINIAN( "uk" ),
		URDU( "ur" ),
		UZBEK( "uz" ),
		UIGHUR( "ug" ),
		VIETNAMESE( "vi" ),
		WELSH( "cy" ),
		YIDDISH( "yi" ),
		YORUBA( "yo" ),
		UNKNOWN( "" );
		
		/** Cache of the string value. */
		public final String stringValue;
		/** Language code.             */
		public final String code;
		
		/**
		 * Creates a new Language.
		 * @param code language code
		 */
		private Language( final String code ) {
			stringValue = convertConstNameToNormal( name() );
			this.code   = code;
		}
		
		@Override
		public String toString() {
			return stringValue;
		}
		
		/**
		 * Converts a const name to a normal, human readable format.
		 * @param text text to be converted
		 * @return the converted text
		 */
		private static String convertConstNameToNormal( final String text ) {
			final StringBuilder sb = new StringBuilder();
			
			boolean nextCharIsUpper = true;
			for ( final char ch : text.toCharArray() ) {
				if ( ch == '_' ) {
					sb.append( ' ' );
					nextCharIsUpper = true;
				}
				else if ( nextCharIsUpper ) {
					sb.append( ch );
					nextCharIsUpper = false;
				}
				else
					sb.append( Character.toLowerCase( ch ) );
			}
			
			return sb.toString();
		};
		
	}
	
	/** URL of the translation service. */
	private static final String SERVICE_URL = "http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&langpair=";
	
	/** Response data key.   */
	private static final String KEY_RESPONSE_DATA   = "responseData";
	/** Translated text key. */
	private static final String KEY_TRANSLATED_TEXT = "translatedText";
	
	/**
	 * Translates the specified texts.
	 * @param texts          texts to be translated
	 * @param sourceLanguage source language of the texts
	 * @param targetLanguage target language of the texts
	 * @return the translated texts; or <code>null</code> if some error occurred
	 */
	public static String[] translateTexts( final String[] texts, final Language sourceLanguage, final Language targetLanguage ) {
		if ( texts.length == 0 )
			return new String[ 0 ];
		
		final StringBuilder urlBuilder = new StringBuilder( SERVICE_URL );
		urlBuilder.append( sourceLanguage.code ).append( "%7C" ).append( targetLanguage.code ); // %7C is the separator |
		
		BufferedReader input = null;
		try {
			
			for ( final String text : texts )
				urlBuilder.append( "&q=" ).append( URLEncoder.encode( text, "UTF-8" ) );
			
			// URL done. Call the translation and download it.
			final URLConnection connection = new URL( urlBuilder.toString() ).openConnection();
			connection.setRequestProperty( "Accept-Charset", "UTF-8" );
			input = new BufferedReader( new InputStreamReader( connection.getInputStream(), Consts.UTF8 ) );
			
			urlBuilder.setLength( 0 ); // Reuse the string builder
			final StringBuilder responseBuilder = urlBuilder;
			
			String line;
			while ( ( line = input.readLine() ) != null )
				responseBuilder.append( line );
			
			// Process the JSON result.
			// Examples:
			// {"responseData": {"translatedText":"Hi!","detectedSourceLanguage":"hu"}, "responseDetails": null, "responseStatus": 200}
			// {"responseData": {"translatedText":"Hi!"}, "responseDetails": null, "responseStatus": 200}
			// {"responseData": {"translatedText":"Szia!"}, "responseDetails": null, "responseStatus": 200}
			// {"responseData": [{"responseData":{"translatedText":"Hello world!","detectedSourceLanguage":"hu"},"responseDetails":null,"responseStatus":200},{"responseData":{"translatedText":"... And goodbye.","detectedSourceLanguage":"hu"},"responseDetails":null,"responseStatus":200}], "responseDetails": null, "responseStatus": 200}
			final JSONObject jsonObject = new JSONObject( responseBuilder.toString() );
			// I don't check if the response status is OK (200) because some text might still got translated properly (for example response status 206)
			String[] translatedTexts = null;
			final Object data = jsonObject.get( KEY_RESPONSE_DATA );
			if ( data instanceof JSONArray ) {
				translatedTexts = new String[ texts.length ];
				final JSONArray array = (JSONArray) data;
				for ( int i = Math.min( texts.length, array.length() ) - 1; i >= 0; i-- ) {
					final Object dataElement = array.getJSONObject( i ).get( KEY_RESPONSE_DATA );
					if ( dataElement instanceof JSONObject ) // Translation OK
						translatedTexts[ i ] = ( (JSONObject) dataElement ).getString( KEY_TRANSLATED_TEXT );
					else
						translatedTexts[ i ] = texts[ i ]; // Could not translate (example: "could not reliably detect source language") 
				}
			}
			else if ( data instanceof JSONObject ) {
				translatedTexts = new String[ texts.length ];
				translatedTexts[ 0 ] = ( (JSONObject) data ).getString( KEY_TRANSLATED_TEXT );
			}
			
			return translatedTexts;
			
		} catch ( final Exception e ) {
			e.printStackTrace();
		} finally {
			if ( input != null )
				try { input.close(); } catch ( final Exception e ) {}
		}
		return null;
	}
	
}
