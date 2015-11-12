/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears;

import hu.belicza.andras.sc2gears.shared.SharedConsts;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class holds application wide constants.
 * 
 * @author Andras Belicza
 */
public class Consts extends SharedConsts {
	
	/** Version of the application.                          */
	public  static final String APPLICATION_VERSION          = "14.3.3";
	/** String value of the release date of the application. */
	private static final String APPLICATION_RELEASE_DATE_ST  = "2014-01-22" ;
	/** Language version of the application.
	 * (required language file version).                     */
	public  static final String APPLICATION_LANGUAGE_VERSION = "14.1";
	/** Release date of the application.                     */
	public static final Date    APPLICATION_RELEASE_DATE;
	static {
		Date tempDate = null;
		try {
			tempDate = new SimpleDateFormat( "yyyy-MM-dd" ).parse( APPLICATION_RELEASE_DATE_ST );
		} catch ( final ParseException pe ) {
			throw new RuntimeException( "Yo! Fix the release date!!", pe );
		}
		APPLICATION_RELEASE_DATE = tempDate;
	}
	/** Author's email. */
	public static final String AUTHOR_EMAIL = new String( new char[] { 'i', 'c', 'z', 'a', 'a', 'a', '@', 'g', 'm', 'a', 'i', 'l', '.', 'c', 'o', 'm' } );
	
	/** UTF-8 charset to be used application wide. */
	public static final Charset UTF8 = Charset.forName( "UTF-8" );
	
	/** Tells if application runs in developer mode. */
	public static final boolean DEVELOPER_MODE = System.getProperty( "developer-mode" ) != null;
	
	/** Name of the folder where plugin settings are saved.              */
	public static final String FOLDER_PLUGIN_SETTINGS        = FOLDER_USER_CONTENT + "/Plugin settings";
	/** Name of the base folder where plugins may save persistent files. */
	public static final String FOLDER_PLUGIN_FILE_CACHE_BASE = FOLDER_USER_CONTENT + "/Plugin file cache";
	/** Name of the folder where replay sources are saved.               */
	public static final String FOLDER_REPLAY_SOURCES         = FOLDER_USER_CONTENT + "/Replay sources";
	/** Name of the folder where replay lists are saved.                 */
	public static final String FOLDER_REPLAY_LISTS           = FOLDER_USER_CONTENT + "/Replay lists";
	/** Name of the folder where preprocessed replays are saved.         */
	public static final String FOLDER_REPLAY_CACHE           = FOLDER_USER_CONTENT + "/Replay cache";
	/** Name of the folder where profiles are saved.                     */
	public static final String FOLDER_PROFILE_CACHE          = FOLDER_USER_CONTENT + "/Profile cache";
	/** Name of the folder where search filters are saved.               */
	public static final String FOLDER_SEARCH_FILTERS         = FOLDER_USER_CONTENT + "/Search filters";
	/** Name of the folder of the language files.                        */
	public static final String FOLDER_LANGUAGES              = "Languages";
	/** Name of the folder where plugins are placed.                     */
	public static final String FOLDER_PLUGINS                = "Plugins";
	/** User home folder.                                                */
	public static final String FOLDER_USER_HOME              = System.getProperty( "user.home" );
	
	/** User content folders.                */
	public static final String[] USER_CONTENT_FOLDERS = { FOLDER_USER_CONTENT, FOLDER_LOGS, FOLDER_REPLAY_SOURCES, FOLDER_REPLAY_LISTS, FOLDER_REPLAY_CACHE, FOLDER_PROFILE_CACHE, FOLDER_SEARCH_FILTERS, FOLDER_PLUGIN_SETTINGS, FOLDER_PLUGIN_FILE_CACHE_BASE };
	
	/** Extension of Sc2replay source files. */
	public static final String EXT_SC2REPLAY_SOURCE   = ".sc2repsrc";
	/** Extension of search filter files.    */
	public static final String EXT_SEARCH_FILTER      = ".filters";
	/** Extension of Sc2replay list files.   */
	public static final String EXT_SC2REPLAY_LIST     = ".csv";
	
	/** URL of the Scelight home page.                    */
	public static final String URL_SCELIGHT_HOME_PAGE                   = "https://sites.google.com/site/scelight/";
	/** URL of the FAQ.                                   */
	public static final String URL_FAQ                                  = URL_HOME_PAGE + "faq";
	/** URL of the Glossary.                              */
	public static final String URL_GLOSSARY                             = URL_HOME_PAGE + "glossary";
	/** URL of the Sc2gears database.                     */
	public static final String URL_SC2GEARS_DATABASE                    = URL_HOME_PAGE + "sc2gears-database";
	/** URL of the donate page.                           */
	public static final String URL_DONATE                               = URL_HOME_PAGE + "donate";
	/** URL of custom portraits.                          */
	public static final String URL_CUSTOM_PORTRAITS                     = URL_HOME_PAGE + "custom-portraits";
	/** URL of the replay sharing page.                   */
	public static final String URL_REPLAY_SHARING                       = URL_HOME_PAGE + "features/replay-sharing";
	/** URL of the BO text format specification.          */
	public static final String URL_BO_TEXT_SPECIFICATION                = URL_HOME_PAGE + "features/build-order-import";
	/** URL of SMPD format specification.                 */
	public static final String URL_SMPD_FORMAT_SPECIFICATION            = URL_HOME_PAGE + "features/mouse-print-recorder";
	/** URL of the Command Line Interface help.           */
	public static final String URL_COMMAND_LINE_INTERFACE_HELP          = URL_HOME_PAGE + "features/command-line-interface";
	/** URL of the Plugin interface.                      */
	public static final String URL_PLUGIN_INTERFACE                     = URL_HOME_PAGE + "features/plugin-interface";
	/** URL of the APM types help page.                   */
	public static final String URL_APM_TYPES                            = URL_HOME_PAGE + "features/replay-analyzer/apm-types";
	/** URL of the Main Building Control Chart help page. */
	public static final String URL_MAIN_BUILDING_CONTROL_CHART          = URL_HOME_PAGE + "features/replay-analyzer/main-building-control-chart";
	/** URL of the Mouse practice game.                   */
	public static final String URL_MOUSE_PRACTICE_GAME                  = URL_HOME_PAGE + "features/mouse-practice-game";
	/** URL of the Public replay commenting.              */
	public static final String URL_PUBLIC_REPLAY_COMMENTING             = URL_HOME_PAGE + "public-replay-commenting";
	/** URL of the Private Video Streaming help page.     */
	public static final String URL_PRIVATE_VIDEO_STREAMING              = URL_HOME_PAGE + "features/private-video-streaming";
	/** URL of the forum.                                 */
	public static final String URL_FORUM                                = "https://groups.google.com/d/forum/sc2gears";
	/** URL of the date/time format specification.        */
	public static final String URL_DATE_TIME_FORMAT_SPEC                = "http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html";
	
	public static final String URL_SC2GEARS_DATABASE_ROOT               = DEVELOPER_MODE ? "https://dev-dot-sciigears.appspot.com/" : "https://sciigears.appspot.com/";
	/** URL of the latest version.                        */
	public static final String URL_LATEST_VERSION                       = URL_SC2GEARS_DATABASE_ROOT + "download/latest_version.xml";
	/** URL of custom portraits list.                     */
	public static final String URL_CUSTOM_PORTRAITS_LIST                = URL_SC2GEARS_DATABASE_ROOT + "download/custom_portraits.xml";
	/** URL of the Latest Sc2gears news.                  */
	public static final String URL_LATEST_SC2GEARS_NEWS                 = URL_SC2GEARS_DATABASE_ROOT + "download/start_page.html";
	/** URL of the All Sc2gears news.                     */
	public static final String URL_ALL_SC2GEARS_NEWS                    = URL_SC2GEARS_DATABASE_ROOT + "download/start_page_all_sc2gears_news.html";
	/** URL of the Sc2gears Database User Page.           */
	public static final String URL_SC2GEARS_DATABASE_USER_PAGE          = URL_SC2GEARS_DATABASE_ROOT + "User.html";
	/** URL of the Sc2gears Database Info servlet.        */
	public static final String URL_SC2GEARS_DATABASE_INFO_SERVLET       = URL_SC2GEARS_DATABASE_ROOT + "info";
	/** URL of the Sc2gears Database File servlet.        */
	public static final String URL_SC2GEARS_DATABASE_FILE_SERVLET       = URL_SC2GEARS_DATABASE_ROOT + "file";
	/** URL of the Sc2gears Database Top Scores servlet.  */
	public static final String URL_SC2GEARS_DATABASE_TOP_SCORES_SERVLET = URL_SC2GEARS_DATABASE_ROOT + "topScores";
	
	/**
	 * No need to instantiate this class.
	 */
	private Consts() {
	}
	
}
