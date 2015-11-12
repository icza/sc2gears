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

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.util.LeetTranslator;
import hu.belicza.andras.sc2gears.util.LeetTranslator.LeetnessLevel;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is the Language manager, the source of language specific formats, settings and messages (that are intended for the users).
 * 
 * <p>Messages are access via message keys. The message values are read from different language XML files.</p>
 * 
 * @author Andras Belicza
 */
public class Language {
	
	/** Name of the version attribute.               */
	public static final String VERSION_ATTRIBUTE_NAME               = "version";
	/** Name of the sub-version attribute.           */
	public static final String SUBVERSION_ATTRIBUTE_NAME            = "subversion";
	/** Name of the translator first name attribute. */
	public static final String TRANSLATOR_FIRST_NAME_ATTRIBUTE_NAME = "translatorFirstName";
	/** Name of the translator last name attribute.  */
	public static final String TRANSLATOR_LAST_NAME_ATTRIBUTE_NAME  = "translatorLastName";
	/** Name of the date format tag.                 */
	public static final String DATE_FORMAT_TAG_NAME                 = "dateFormat";
	/** Name of the date+time format tag.            */
	public static final String DATE_TIME_FORMAT_TAG_NAME            = "dateTimeFormat";
	/** Name of the time format tag.                 */
	public static final String TIME_FORMAT_TAG_NAME                 = "timeFormat";
	/** Name of the time format tag.                 */
	public static final String PERSON_NAME_FORMAT_TAG_NAME          = "personNameFormat";
	/** Name of the text group tag.                  */
	public static final String TEXT_GROUP_TAG_NAME                  = "g";
	/** Name of the text tag.                        */
	public static final String TEXT_TAG_NAME                        = "t";
	/** Name of the key attribute.                   */
	public static final String KEY_ATTRIBUTE_NAME                   = "k";
	/** Name of the comment attribute.               */
	public static final String COMMENT_ATTRIBUTE_NAME               = "c";
	
	/** <code>"firstName lastName"</code> person name format. */
	public static final String PERSON_NAME_FORMAT_FIRST_NAME_LAST_NAME = "firstName lastName";
	/** <code>"lastName firstName"</code> person name format. */
	public static final String PERSON_NAME_FORMAT_LAST_NAME_FISRT_NAME = "lastName firstName";
	
	/** Name of the language.                              */
	private final String languageName;
	
	/** Version of the language file.                      */
	public final String languageFileVersion;
	/** Subversion of the language file.                   */
	public final String languageFileSubversion;
	/** First name of the translator of the language file. */
	public final String translatorFirstName;
	/** Last name of the translator of the language file.  */
	public final String translatorLastName;
	
	/** Default pattern of the date format.      */
	public final String defaultDateFormatPattern;
	/** Default pattern of the time format.      */
	public final String defaultTimeFormatPattern;
	/** Default pattern of the date+time format. */
	public final String defaultDateTimeFormatPattern;
	
	/** Date format to be used to format dates.                    */
	private final SimpleDateFormat DATE_FORMAT;
	/** Date and time format to be used to format dates and times. */
	private final SimpleDateFormat DATE_TIME_FORMAT;
	/** Time format to be used to format dates and times.          */
	private final SimpleDateFormat TIME_FORMAT;
	
	/** Indicates if the person name format is that first name comes first. */
	public final boolean personNameFormatFirstNameFirst;
	
	/**
	 * Internal map to store the texts.<br>
	 * The <code>key</code> is a unique text key, the <code>value</code> is the translation of the text denoted by the text key. 
	 */
	public final Map< String, String > textMap = new HashMap< String, String >();
	
	// Note: comments for texts and text groups are stored in 2 distinct maps
	// because the same key can be assigned to a group and to a text (example: "menu.file" is a text key and is also a text group key)!
	
	/**
	 * Internal map to store the comments of the texts.<br>
	 * The <code>key</code> is a unique text key, the <code>value</code> is the comment.
	 * 
	 * <p>This comment map is only created and populated for the default language!</p>
	 */
	public final Map< String, String > textCommentsMap;
	
	/**
	 * Internal map to store the comments of the text groups.<br>
	 * The <code>key</code> is a unique text (group) key, the <code>value</code> is the comment.
	 * 
	 * <p>This comment map is only created and populated for the default language!</p>
	 */
	public final Map< String, String > textGroupCommentsMap;
	
	/** Leetness level to be applied to texts. */
	private static final LeetnessLevel LEETNESS_LEVEL;
	static {
		LeetnessLevel LEETNESS_LEVEL_;
		try {
			LEETNESS_LEVEL_ = LeetTranslator.LeetnessLevel.values()[ Settings.getInt( Settings.KEY_SETTINGS_LANGUAGE_LEETNESS_LEVEL ) ];
		} catch ( Exception e ) {
			LEETNESS_LEVEL_ = LeetTranslator.LeetnessLevel.values()[ Settings.getDefaultInt( Settings.KEY_SETTINGS_LANGUAGE_LEETNESS_LEVEL ) ];
		}
		LEETNESS_LEVEL = LEETNESS_LEVEL_;
	}
	
	/** The default language is only loaded once. */
	public static final Language DEFAULT_LANGUAGE;
	static {
		// Load the default language
		Language DEFAULT_LANGUAGE_ = null;
		try {
			DEFAULT_LANGUAGE_ = new Language( Settings.DEFAULT_APP_LANGUAGE, true );
		} catch ( final Exception e ) {
			System.err.println( "Failed to load the default language file, program will exit now!" );
			System.exit( 0 );
		}
		DEFAULT_LANGUAGE = DEFAULT_LANGUAGE_;
	}
	
	/** Reference to the loaded language, pointing to the default language for now. */
	private static Language loadedLanguage = DEFAULT_LANGUAGE;
	
	/**
	 * Creates a new Language.<br>
	 * It's private because it is only used internally.<br>
	 * Loads the texts of a language.<br>
	 * The method will look for a file called <code>&lt;language&gt;.xml</code> in the <code>Languages</code> folder.
	 * 
	 * <p>if <code>isLanguageToBeActivated</code> is true, the text map will be initialized from the default language first
	 * (if we are not currently loading the default language).</p>
	 *  
	 * @param languageName language whose texts to be loaded
	 * @param isLanguageToBeActivated tells if the loading purpose is to activate the language; in this case language version will be checked and the text map will be initialized from the default language
	 * @throws Exception if loading the language fails
	 */
	private Language( final String languageName, final boolean isLanguageToBeActivated ) throws Exception {
		try {
			final boolean isDefaultLanguage = Settings.DEFAULT_APP_LANGUAGE.equals( languageName );
			
			this.languageName = languageName;
			
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments( true );
			final Document document = factory.newDocumentBuilder().parse( getLanguageFile( languageName ) );
			
			final Element docElement = document.getDocumentElement();
			
			// Read language file meta information
			languageFileVersion = docElement.getAttribute( VERSION_ATTRIBUTE_NAME );
			if ( isLanguageToBeActivated )
    			if ( !Consts.APPLICATION_LANGUAGE_VERSION.equals( languageFileVersion ) ) {
    				System.out.println( "The language file (" + languageName + ") version (" + languageFileVersion + ") does not match the application language version (" + Consts.APPLICATION_LANGUAGE_VERSION + ")." );
    				if ( isDefaultLanguage ) {
    					System.out.println( "The default language file (" + Settings.DEFAULT_APP_LANGUAGE + ") version MUST match the application language version!" );
    					if ( !Consts.DEVELOPER_MODE )
    						System.exit( 0 );
    				}
    				else
    					System.out.println( "Some texts might be displayed in the default language." );
    			}
			languageFileSubversion = docElement.getAttribute( SUBVERSION_ATTRIBUTE_NAME            );
			translatorFirstName    = docElement.getAttribute( TRANSLATOR_FIRST_NAME_ATTRIBUTE_NAME );
			translatorLastName     = docElement.getAttribute( TRANSLATOR_LAST_NAME_ATTRIBUTE_NAME  );
			
			// Read date format
			NodeList nodeList = docElement.getElementsByTagName( DATE_FORMAT_TAG_NAME );
			DATE_FORMAT = new SimpleDateFormat( defaultDateFormatPattern = ( (Element) nodeList.item( 0 ) ).getTextContent() );
			
			// Read date time format
			nodeList = docElement.getElementsByTagName( DATE_TIME_FORMAT_TAG_NAME );
			DATE_TIME_FORMAT = new SimpleDateFormat( defaultDateTimeFormatPattern = ( (Element) nodeList.item( 0 ) ).getTextContent() );
			
			// Read time format
			nodeList = docElement.getElementsByTagName( TIME_FORMAT_TAG_NAME );
			TIME_FORMAT = new SimpleDateFormat( defaultTimeFormatPattern = ( (Element) nodeList.item( 0 ) ).getTextContent() );
			
			// Read person name format
			nodeList = docElement.getElementsByTagName( PERSON_NAME_FORMAT_TAG_NAME );
			final String personNameFormat = ( (Element) nodeList.item( 0 ) ).getTextContent();
			if ( !PERSON_NAME_FORMAT_FIRST_NAME_LAST_NAME.equals( personNameFormat ) && !PERSON_NAME_FORMAT_LAST_NAME_FISRT_NAME.equals( personNameFormat ) )
				throw new Exception( "Invalid person name format!" );
			personNameFormatFirstNameFirst = PERSON_NAME_FORMAT_FIRST_NAME_LAST_NAME.equals( personNameFormat );
			
			if ( isLanguageToBeActivated )
    			if ( !isDefaultLanguage ) // If this is not the default language...
    				textMap.putAll( DEFAULT_LANGUAGE.textMap );
			
			// Read texts
			textCommentsMap      = isDefaultLanguage ? new HashMap< String, String >() : null;
			textGroupCommentsMap = isDefaultLanguage ? new HashMap< String, String >() : null;
			readGroupsAndTexts( docElement, "" );
			
		} catch ( final Exception e ) {
			System.err.println( "Failed to load language file: " + languageName );
			e.printStackTrace( System.err );
			throw e;
		}
	}
	
	/**
	 * Returns the language file associated to the specified language name.
	 * @param languageName name of the language to return the language file for
	 * @return the language file associated to the specified language name
	 */
	public static File getLanguageFile( final String languageName ) {
		return new File( Consts.FOLDER_LANGUAGES, languageName + ".xml" );
	}
	
	/**
	 * Reads the content of a node (of the Language XML file).
	 * 
	 * @param parentElement element to start reading from
	 * @param keyPrefix     prefix of the keys to prepend to read keys
	 */
	private void readGroupsAndTexts( final Element parentElement, String keyPrefix ) {
		if ( !keyPrefix.isEmpty() )
			keyPrefix = keyPrefix + '.';
		
		final NodeList childNodeList = parentElement.getChildNodes();
		
		final int childerCount = childNodeList.getLength();
		for ( int i = 0; i < childerCount; i++ ) {
			final Node childNode = childNodeList.item( i );
			if ( childNode instanceof Element ) {
				final Element childElement = (Element) childNode;
				
				String key = null;
				switch ( childElement.getTagName() ) {
				case TEXT_TAG_NAME :
					textMap.put( key = keyPrefix + childElement.getAttribute( KEY_ATTRIBUTE_NAME ), childElement.getTextContent() );
					if ( textCommentsMap != null && key != null ) {
						final String comment = childElement.getAttribute( COMMENT_ATTRIBUTE_NAME ); // "" is returned if attribute "c" is not specified
						if ( !comment.isEmpty() )
							textCommentsMap.put( key, comment );
					}
					break;
				case TEXT_GROUP_TAG_NAME :
					readGroupsAndTexts( childElement, key = keyPrefix + childElement.getAttribute( KEY_ATTRIBUTE_NAME ) );
					if ( textGroupCommentsMap != null && key != null ) {
						final String comment = childElement.getAttribute( COMMENT_ATTRIBUTE_NAME ); // "" is returned if attribute "c" is not specified
						if ( !comment.isEmpty() )
							textGroupCommentsMap.put( key, comment );
					}
					break;
				}
			}
		}
	}
	
	/**
	 * Loads and activates a language.<br>
	 * The method will look for a file called <code>&lt;language&gt;.xml</code> in the <code>Languages</code> folder.<br>
	 * If loading the language fails, nothing will change.
	 * @param languageName name of the language to be loaded and activated
	 * @return true if loading and activating the language file was successful; false otherwise
	 */
	public static boolean loadAndActivateLanguage( final String languageName ) {
		try {
			loadedLanguage = DEFAULT_LANGUAGE.languageName.equals( languageName ) ? DEFAULT_LANGUAGE : new Language( languageName, true );
			
			updateUIManagerTexts();
			
			return true;
		} catch ( final Exception e ) {
			System.err.println( "Failed to load language file: " + languageName );
			return false;
		}
	}
	
	/**
	 * Loads and returns a language.<br>
	 * The method will look for a file called <code>&lt;language&gt;.xml</code> in the <code>Languages</code> folder.<br>
	 * Returns <code>null</code> if loading the language fails.
	 * @param languageName name of the language to be loaded
	 * @return the loaded language or <code>null</code> if loading the language failed
	 */
	public static Language loadLanguage( final String languageName ) {
		try {
			final Language language = new Language( languageName, false );
			return language;
		} catch ( final Exception e ) {
			System.err.println( "Failed to load language file: " + languageName );
			return null;
		}
	}
	
	/**
	 * Updates the UIManager's texts from the loaded language.<br>
	 * These properties are queried explicitly from the UI manager (and not from us).
	 */
	private static void updateUIManagerTexts() {
		// JInternalFrame texts
		UIManager.put( "InternalFrameTitlePane.closeButtonText"   , getText( "internalFrame.menu.close" ) );
        UIManager.put( "InternalFrameTitlePane.minimizeButtonText", getText( "internalFrame.menu.minimize" ) );
        UIManager.put( "InternalFrameTitlePane.restoreButtonText" , getText( "internalFrame.menu.restore" ) );
        UIManager.put( "InternalFrameTitlePane.maximizeButtonText", getText( "internalFrame.menu.maximize" ) );
        UIManager.put( "InternalFrameTitlePane.moveButtonText"    , getText( "internalFrame.menu.move" ) );
        UIManager.put( "InternalFrameTitlePane.sizeButtonText"    , getText( "internalFrame.menu.size" ) );
        UIManager.put( "InternalFrame.closeButtonToolTip"         , getText( "internalFrame.toolTip.close" ) );
        UIManager.put( "InternalFrame.iconButtonToolTip"          , getText( "internalFrame.toolTip.minimize" ) );
        UIManager.put( "InternalFrame.restoreButtonToolTip"       , getText( "internalFrame.toolTip.restore" ) );
        UIManager.put( "InternalFrame.maxButtonToolTip"           , getText( "internalFrame.toolTip.maximize" ) );
        
		// JFileChooser texts
        UIManager.put( "FileChooser.openDialogTitleText"          , getText( "fileChooser.openDialogTitle" ) );
        UIManager.put( "FileChooser.saveDialogTitleText"          , getText( "fileChooser.saveDialogTitle" ) );
        UIManager.put( "FileChooser.lookInLabelText"              , getText( "fileChooser.lookInLabel" ) );
        UIManager.put( "FileChooser.filesOfTypeLabelText"         , getText( "fileChooser.filesOfTypeLabel" ) );
        UIManager.put( "FileChooser.fileNameLabelText"            , getText( "fileChooser.fileNameLabel" ) );
        UIManager.put( "FileChooser.upFolderToolTipText"          , getText( "fileChooser.upFolderToolTip" ) );
        UIManager.put( "FileChooser.homeFolderToolTipText"        , getText( "fileChooser.homeFolderToolTip" ) );
        UIManager.put( "FileChooser.newFolderToolTipText"         , getText( "fileChooser.newFolderToolTip" ) );
        UIManager.put( "FileChooser.listViewButtonToolTipText"    , getText( "fileChooser.listViewButtonToolTip" ) );
        UIManager.put( "FileChooser.detailsViewButtonToolTipText" , getText( "fileChooser.detailsViewButtonToolTip" ) );
        UIManager.put( "FileChooser.saveButtonText"               , getText( "fileChooser.saveButton" ) );
        UIManager.put( "FileChooser.openButtonText"               , getText( "fileChooser.openButton" ) );
        UIManager.put( "FileChooser.cancelButtonText"             , getText( "fileChooser.cancelButton" ) );
        UIManager.put( "FileChooser.updateButtonText"             , getText( "fileChooser.updateButton" ) );
        UIManager.put( "FileChooser.helpButtonText"               , getText( "fileChooser.helpButton" ) );
        UIManager.put( "FileChooser.saveButtonToolTipText"        , getText( "fileChooser.saveButtonToolTip" ) );
        UIManager.put( "FileChooser.openButtonToolTipText"        , getText( "fileChooser.openButtonToolTip" ) );
        UIManager.put( "FileChooser.cancelButtonToolTipText"      , getText( "fileChooser.cancelButtonToolTip" ) );
        UIManager.put( "FileChooser.updateButtonToolTipText"      , getText( "fileChooser.updateButtonToolTip" ) );
        UIManager.put( "FileChooser.helpButtonToolTipText"        , getText( "fileChooser.helpButtonToolTip" ) );
        UIManager.put( "FileChooser.acceptAllFileFilterText"      , getText( "fileChooser.acceptAllFileFilter" ) );
        
        // JColorChooser texts
        UIManager.put( "ColorChooser.previewText"                 , getText( "colorChooser.preview" ) );
        UIManager.put( "ColorChooser.swatchesRecentText"          , getText( "colorChooser.recent" ) );
        UIManager.put( "ColorChooser.sampleText"                  , getText( "colorChooser.sampleText" ) );
	}
	
	/**
	 * Applies the date and time formats from {@link Settings} on the loaded Language.<br>
	 * If a pattern is an empty string or is an invalid pattern, the default one will be used.
	 */
	public static void applyDateTimeFormats() {
		final String dateFormatPattern     = Settings.getString( Settings.KEY_SETTINGS_MISC_CUSTOM_DATE_FORMAT      );
		final String timeFormatPattern     = Settings.getString( Settings.KEY_SETTINGS_MISC_CUSTOM_TIME_FORMAT      );
		final String dateTimeFormatPattern = Settings.getString( Settings.KEY_SETTINGS_MISC_CUSTOM_DATE_TIME_FORMAT );
		
		try {
			loadedLanguage.DATE_FORMAT.applyPattern( dateFormatPattern.isEmpty() ? loadedLanguage.defaultDateFormatPattern : dateFormatPattern );
		} catch ( final IllegalArgumentException iae ) {
			iae.printStackTrace();
		}
		
		try {
			loadedLanguage.TIME_FORMAT.applyPattern( timeFormatPattern.isEmpty() ? loadedLanguage.defaultTimeFormatPattern : timeFormatPattern );
		} catch ( final IllegalArgumentException iae ) {
			iae.printStackTrace();
		}
		
		try {
			loadedLanguage.DATE_TIME_FORMAT.applyPattern( dateTimeFormatPattern.isEmpty() ? loadedLanguage.defaultDateTimeFormatPattern : dateTimeFormatPattern );
		} catch ( final IllegalArgumentException iae ) {
			iae.printStackTrace();
		}
	}
	
	/**
	 * Returns the name of the loaded language file.
	 * @return the name of the loaded language file
	 */
	public static String getLanguageName() {
		return loadedLanguage.languageName;
	}
	
	/**
	 * Returns the name of the translator of the loaded language file.
	 * @return the name of the translator of the loaded language file
	 */
	public static String getTranslatorName() {
		return formatPersonName( loadedLanguage.translatorFirstName, loadedLanguage.translatorLastName );
	}
	
	/**
	 * Returns the version of the the loaded language file.
	 * @return the version of the the loaded language file
	 */
	public static String getLanguageFileVersion() {
		return loadedLanguage.languageFileVersion;
	}
	
	/**
	 * Returns the subversion of the the loaded language file.
	 * @return the subversion of the the loaded language file
	 */
	public static String getLanguageFileSubversion() {
		return loadedLanguage.languageFileSubversion;
	}
	
	/**
	 * Returns the version of the the default language file.
	 * @return the version of the the default language file
	 */
	public static String getDefaultLanguageFileVersion() {
		return DEFAULT_LANGUAGE.languageFileVersion;
	}
	
	/**
	 * Returns the subversion of the the default language file.
	 * @return the subversion of the the default language file
	 */
	public static String getDefaultLanguageFileSubversion() {
		return DEFAULT_LANGUAGE.languageFileSubversion;
	}
	
	/**
	 * Returns the available languages.<br>
	 * Languages will be sorted by name, and the default language will be in the first place.
	 * @return the available languages
	 */
	public static String[] getAvailableLanguages() {
		final String[] languageFileNames = new File( Consts.FOLDER_LANGUAGES ).list( new FilenameFilter() {
			@Override
			public boolean accept( final File dir, final String name ) {
				return name.endsWith( ".xml" );
			}
		} );
		
		final String[] languages = new String[ languageFileNames.length ];
		for ( int i = 0; i < languageFileNames.length; i++ )
			languages[ i ] = languageFileNames[ i ].substring( 0, languageFileNames[ i ].lastIndexOf( '.' ) );
		
		Arrays.sort( languages, new Comparator< String >() {
			@Override
			public int compare( final String s1, final String s2 ) {
				return Settings.DEFAULT_APP_LANGUAGE.equals( s1 ) ? -1 : Settings.DEFAULT_APP_LANGUAGE.equals( s2 ) ? 1 : s1.compareTo( s2 );
			}
		} );
		
		return languages;
	}
	
	/**
	 * Returns the text associated with the given key.
	 * @param key key of the text to be returned
	 * @return the text associated with the given key
	 */
	public static String getText( final String key ) {
		return LeetTranslator.translate( loadedLanguage.textMap.get( key ), LEETNESS_LEVEL );
	}
	
	/**
	 * Returns the original text associated with the given key.<br>
	 * Original means without the Leet translation.
	 * @param key key of the text to be returned
	 * @return the original text associated with the given key
	 */
	public static String getOriginalText( final String key ) {
		return loadedLanguage.textMap.get( key );
	}
	
	/**
	 * Returns the default text associated with the given key.<br>
	 * Default means the text of the default (English) language.
	 * @param key key of the text to be returned
	 * @return the default text associated with the given key
	 */
	public static String getDefaultText( final String key ) {
		return LeetTranslator.translate( DEFAULT_LANGUAGE.textMap.get( key ), LEETNESS_LEVEL );
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
		String text = loadedLanguage.textMap.get( key );
		
		for ( int i = 0; i < arguments.length; i++ ) 
			text = text.replace( "$" + i, arguments[ i ].toString() );
		
		return LeetTranslator.translate( text, LEETNESS_LEVEL );
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
		String text = loadedLanguage.textMap.get( key );
		
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
		
		return new Pair< String, Character >( LeetTranslator.translate( text, LEETNESS_LEVEL ), mnemonic );
	}
	
	/**
	 * Formats a date to the proper language format.
	 * @param date date to be formatted
	 * @return the formatted date
	 */
	public static String formatDate( final Date date ) {
		// SimpleDateFormat is not synchronized, so... (this caused problems in multi-thread searches for example)
		synchronized ( loadedLanguage.DATE_FORMAT ) {
			return loadedLanguage.DATE_FORMAT.format( date );
		}
	}
	
	/**
	 * Formats a date and time to the proper language format.
	 * @param dateTime date and time to be formatted
	 * @return the formatted date and time
	 */
	public static String formatDateTime( final Date dateTime ) {
		// SimpleDateFormat is not synchronized, so... (this caused problems in multi-thread searches for example)
		synchronized ( loadedLanguage.DATE_TIME_FORMAT ) {
			return loadedLanguage.DATE_TIME_FORMAT.format( dateTime );
		}
	}
	
	/**
	 * Formats a time to the proper language format.
	 * @param time time to be formatted
	 * @return the formatted time
	 */
	public static String formatTime( final Date time ) {
		// SimpleDateFormat is not synchronized, so... (this caused problems in multi-thread searches for example)
		synchronized ( loadedLanguage.TIME_FORMAT ) {
			return loadedLanguage.TIME_FORMAT.format( time );
		}
	}
	
	/**
	 * Concatenates a person's first and last name in the order of the language setting.
	 * @param firstName first name of the person
	 * @param lastName  last name of the person
	 * @return the properly formatted name of the person
	 */
	public static String formatPersonName( final String firstName, final String lastName ) {
		if ( firstName == null || firstName.isEmpty() )
			return lastName;
		if ( lastName  == null || lastName .isEmpty() )
			return firstName;
		if ( loadedLanguage.personNameFormatFirstNameFirst )
			return firstName + ' ' + lastName;
		else
			return lastName + ' ' + firstName;
	}
	
	/**
	 * Parses and returns a date.
	 * @param dateString date string to parse from
	 * @return the parsed date, or null if dateString is invalid
	 */
	public static Date parseDate( final String dateString ) {
		try {
			return loadedLanguage.DATE_FORMAT.parse( dateString );
		} catch ( final ParseException pe ) {
			pe.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Parses and returns a date and time.
	 * @param dateTimeString date time string to parse from
	 * @param silent         indicates if no stack trace should be printed in case of errors
	 * @return the parsed date and time, or null if dateTimeString is invalid
	 */
	public static Date parseDateTime( final String dateTimeString, final boolean silent ) {
		try {
			return loadedLanguage.DATE_TIME_FORMAT.parse( dateTimeString );
		} catch ( final ParseException pe ) {
			if ( !silent )
				pe.printStackTrace();
			return null;
		}
	}
	
}
