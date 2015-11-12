/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.settings;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.PlayerId;
import hu.belicza.andras.sc2gears.shared.SharedConsts;
import hu.belicza.andras.sc2gears.ui.dialogs.ShareReplaysDialog.ReplayUploadSite;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplaySearch;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Icon;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This is the Settings manager. It is responsible to hold, serve, save and load the settings of the program.
 * 
 * @author Andras Belicza
 */
public class Settings {
	
	/** The the persistent file storing the settings. */
	public static final File SETTINGS_FILE = new File( Consts.FOLDER_USER_CONTENT, "settings.xml" );
	
	/** Maximum number of SC2 auto-rep folders. */
	public  static final int MAX_SC2_AUTOREP_FOLDERS = 5;
	
	/** Saved with version key.                         */
	public  static final String KEY_META_SAVED_WITH_VERSION                           = "metaData.savedWithVersion";
	/** Save time key.                                  */
	public  static final String KEY_META_SAVE_TIME                                    = "metaData.saveTime";
	
	/** Recent replays count key.                       */
	public  static final String KEY_RECENT_REPLAYS_COUNT                              = "recentReplays.count"; 
	/** Recent replay entries key.                      */
	public  static final String KEY_RECENT_REPLAY_ENTRIES                             = "recentReplays.entry."; 
	/** Show navigation bar key.                        */
	public  static final String KEY_NAVIGATION_SHOW_BAR                               = "navigation.showNavbar"; 
	/** Show navigation labels key.                     */
	public  static final String KEY_NAVIGATION_USE_SMALL_FONT                         = "navigation.useSmallFont"; 
	/** Navigation bar position key.                    */
	public  static final String KEY_NAVIGATION_BAR_POSITION                           = "navigation.position"; 
	/** The application language key.                   */
	public  static final String KEY_SETTINGS_LANGUAGE                                 = "settings.language"; 
	/** Leetness level key.                             */
	public  static final String KEY_SETTINGS_LANGUAGE_LEETNESS_LEVEL                  = "settings.language.leetnessLevel"; 
	/** The application Look and Feel key.              */
	public  static final String KEY_SETTINGS_LAF                                      = "settings.laf"; 
	/** Voice key.                                      */
	public  static final String KEY_SETTINGS_VOICE                                    = "settings.voice"; 
	/** Sc2 installation folder key.                    */
	public  static final String KEY_SETTINGS_FOLDER_SC2_INSTALLATION                  = "settings.folders.sc2Installation"; 
	/** Sc2 auto replay folder key.                     */
	public  static final String KEY_SETTINGS_FOLDER_SC2_AUTO_REPLAY                   = "settings.folders.sc2AutoReplay"; 
	/** Sc2 auto replay folder key.                     */
	public  static final String KEY_SETTINGS_FOLDER_ENABLE_EXTRA_SC2_AUTO_REPLAY      = "settings.folders.enableExtraSc2AutoReplay"; 
	/** Sc2 maps folder key.                            */
	public  static final String KEY_SETTINGS_FOLDER_SC2_MAPS                          = "settings.folders.sc2Maps"; 
	/** Default replay folder key.                      */
	public  static final String KEY_SETTINGS_FOLDER_DEFAULT_REPLAY                    = "settings.folders.defaultReplay"; 
	/** Replay auto-save folder key.                    */
	public  static final String KEY_SETTINGS_FOLDER_REPLAY_AUTO_SAVE                  = "settings.folders.replayAutoSave"; 
	/** Mouse print output folder key.                  */
	public  static final String KEY_SETTINGS_FOLDER_MOUSE_PRINT_OUTPUT                = "settings.folders.mousePrintOutput"; 
	/** Sound volume key.                               */
	public  static final String KEY_SETTINGS_MISC_SOUND_VOLUME                        = "settings.misc.soundVolume";
	/** APM alert level key.                            */
	public  static final String KEY_SETTINGS_MISC_APM_ALERT_LEVEL                     = "settings.misc.apmAlertLevel";
	/** APM warm-up time key.                           */
	public  static final String KEY_SETTINGS_MISC_APM_WARMUP_TIME                     = "settings.misc.apmWarmupTime";
	/** APM check interval (sec) key.                   */
	public  static final String KEY_SETTINGS_MISC_APM_CHECK_INTERVAL_SEC              = "settings.misc.apmCheckIntervalSec"; 
	/** Alert when APM is back to normal key.           */
	public  static final String KEY_SETTINGS_MISC_ALERT_WHEN_APM_IS_BACK_TO_NORMAL    = "settings.misc.alertWhenApmIsBackToNormal";
	/** APM alert repetition interval (sec) key.        */
	public  static final String KEY_SETTINGS_MISC_APM_ALERT_REPETITION_INTERVAL_SEC   = "settings.misc.apmAlertRepetitionIntervalSec";
	/** Alert on game start key.                        */
	public  static final String KEY_SETTINGS_MISC_ALERT_ON_GAME_START                 = "settings.misc.alertOnGameStart"; 
	/** Alert on game end key.                          */
	public  static final String KEY_SETTINGS_MISC_ALERT_ON_GAME_END                   = "settings.misc.alertOnGameEnd"; 
	/** Show/Hide APM display on start/end key.         */
	public  static final String KEY_SETTINGS_MISC_SHOW_HIDE_APM_DISPLAY_ON_START_END  = "settings.misc.showHideApmDisplayOnStartEnd"; 
	/** Save mouse prints key.                          */
	public  static final String KEY_SETTINGS_MISC_SAVE_MOUSE_PRINTS                   = "settings.misc.saveMousePrints"; 
	/** Store mouse prints key.                         */
	public  static final String KEY_SETTINGS_MISC_STORE_MOUSE_PRINTS                  = "settings.misc.storeMousePrints"; 
	/** Mouse print sampling time color key.            */
	public  static final String KEY_SETTINGS_MISC_MOUSE_PRINT_SAMPLING_TIME           = "settings.misc.mousePrintSamplingTime"; 
	/** Mouse print what to save key.                   */
	public  static final String KEY_SETTINGS_MISC_MOUSE_PRINT_WHAT_TO_SAVE            = "settings.misc.mousePrintWhatToSave"; 
	/** Mouse print data compression key.               */
	public  static final String KEY_SETTINGS_MISC_MOUSE_PRINT_DATA_COMPRESSION        = "settings.misc.mousePrintDataCompression"; 
	/** Mouse print output format key.                  */
	public  static final String KEY_SETTINGS_MISC_MOUSE_PRINT_IMAGE_OUTPUT_FORMAT     = "settings.misc.mousePrintImageOutputFormat"; 
	/** Mouse print use anti-aliasing key.              */
	public  static final String KEY_SETTINGS_MISC_MOUSE_PRINT_USE_ANTIALIASING        = "settings.misc.mousePrintUseAntialiasing"; 
	/** Mouse print background color key.               */
	public  static final String KEY_SETTINGS_MISC_MOUSE_PRINT_BACKGROUND_COLOR        = "settings.misc.mousePrintBackgroundColor"; 
	/** Mouse print color key.                          */
	public  static final String KEY_SETTINGS_MISC_MOUSE_PRINT_COLOR                   = "settings.misc.mousePrintColor"; 
	/** Mouse print pour ink idle time key.             */
	public  static final String KEY_SETTINGS_MISC_MOUSE_PRINT_POUR_INK_IDLE_TIME      = "settings.misc.mousePrintPourInkIdleTime"; 
	/** Mouse print idle ink flow rate (pixel/sec) key. */
	public  static final String KEY_SETTINGS_MISC_MOUSE_PRINT_IDLE_INK_FLOW_RATE      = "settings.misc.mousePrintIdleInkFlowRate"; 
	/** Mouse warm-up time key.                         */
	public  static final String KEY_SETTINGS_MISC_MOUSE_WARMUP_TIME                   = "settings.misc.mouseWarmupTime"; 
	/** New rep check interval (sec) key.               */
	public  static final String KEY_SETTINGS_MISC_NEW_REP_CHECK_INTERVAL_SEC          = "settings.misc.newRepCheckIntervalSec"; 
	/** Navigation bar initial width key.               */
	public  static final String KEY_SETTINGS_MISC_NAV_BAR_INITIAL_WIDTH               = "settings.misc.navBarInitialWidth"; 
	/** Tool tip initial delay key.                     */
	public  static final String KEY_SETTINGS_MISC_TOOL_TIP_INITIAL_DELAY              = "settings.misc.toolTipInitialDelay"; 
	/** Tool tip dismiss delay key.                     */
	public  static final String KEY_SETTINGS_MISC_TOOL_TIP_DISMISS_DELAY              = "settings.misc.toolTipDismissDelay";
	/** Display info when started minimized key.        */
	public  static final String KEY_SETTINGS_MISC_DISPLAY_INFO_WHEN_STARTED_MINIMIZED = "settings.misc.displayInfoWhenStartedMinimized";
	/** Time to keep log files key.                     */
	public  static final String KEY_SETTINGS_MISC_TIME_TO_KEEP_LOG_FILES              = "settings.misc.timeToKeepLogFiles";
	/** Time limit for multi-rep analysis key.          */
	public  static final String KEY_SETTINGS_MISC_TIME_LIMIT_FOR_MULTI_REP_ANALYSIS   = "settings.misc.timeLimitForMultiRepAnalysis";
	/** Build order length key.                         */
	public  static final String KEY_SETTINGS_MISC_BUILD_ORDER_LENGTH                  = "settings.misc.buildOrderLength";
	/** Charts-action list partitioning key.            */
	public  static final String KEY_SETTINGS_MISC_CHARTS_ACTION_LIST_PARTITIONING     = "settings.misc.chartsActionListPartitioning";
	/** Initial time to exclude from APM key.           */
	public  static final String KEY_SETTINGS_MISC_INITIAL_TIME_TO_EXCLUDE_FROM_APM    = "settings.misc.initialTimeToExcludeFromApm";
	/** Game length records granularity key.            */
	public  static final String KEY_SETTINGS_MISC_GAME_LENGTH_RECORDS_GRANULARITY     = "settings.misc.gameLengthRecordsGranularity";
	/** Max gaming session break granularity key.       */
	public  static final String KEY_SETTINGS_MISC_MAX_GAMING_SESSION_BREAK            = "settings.misc.maxGamingSessionBreak";
	/** Replay auto-save name template key.             */
	public  static final String KEY_SETTINGS_MISC_REP_AUTO_SAVE_NAME_TEMPLATE         = "settings.misc.repAutoSaveNameTemplate";
	/** Favored player list key.                        */
	public  static final String KEY_SETTINGS_MISC_FAVORED_PLAYER_LIST                 = "settings.misc.favoredPlayerList";
	/** Rearrange players in rep analyzer key.          */
	public  static final String KEY_SETTINGS_MISC_REARRANGE_PLAYERS_IN_REP_ANALYZER   = "settings.misc.rearrangePlayersInRepAnalyzer";
	/** Preferred battle.net language key.              */
	public  static final String KEY_SETTINGS_MISC_PREFERRED_BNET_LANGUAGE             = "settings.misc.preferredBnetLanguage";
	/** Show profile info key.                          */
	public  static final String KEY_REP_MISC_ANALYZER_SHOW_PROFILE_INFO               = "settings.misc.showProfileInfo"; 
	/** Auto retrieve extended profile info key.        */
	public  static final String KEY_REP_MISC_ANALYZER_AUTO_RETRIEVE_EXT_PROFILE_INFO  = "settings.misc.autoRetrieveExtProfileInfo"; 
	/** Max rows in profile tool tip key.               */
	public  static final String KEY_REP_MISC_ANALYZER_MAX_ROWS_IN_PROFILE_TOOL_TIP    = "settings.misc.maxRowsInProfileToolTip"; 
	/** Use real-time measurement key.                  */
	public  static final String KEY_SETTINGS_MISC_USE_REAL_TIME_MEASUREMENT           = "settings.misc.useRealTimeMeasurement";
	/** Show winners key.                               */
	public  static final String KEY_SETTINGS_MISC_SHOW_WINNERS                        = "settings.misc.showWinners";
	/** Declare largest as winner key.                  */
	public  static final String KEY_SETTINGS_MISC_DECLARE_LARGEST_AS_WINNER           = "settings.misc.declareLargestAsWinner";
	/** Override game format based on match-up key.     */
	public  static final String KEY_SETTINGS_MISC_OVERRIDE_FORMAT_BASED_ON_MATCHUP    = "settings.misc.overrideFormatBasedOnMatchup";
	/** Animator FPS key.                               */
	public  static final String KEY_SETTINGS_MISC_ANIMATOR_FPS                        = "settings.misc.animatorFps";
	/** Animator jump time key.                         */
	public  static final String KEY_SETTINGS_MISC_ANIMATOR_JUMP_TIME                  = "settings.misc.animatorJumpTime";
	/** Animator speed key.                             */
	public  static final String KEY_SETTINGS_MISC_ANIMATOR_SPEED                      = "settings.misc.animatorSpeed";
	/** New replay detection method key.                */
	public  static final String KEY_SETTINGS_MISC_NEW_REPLAY_DETECTION_METHOD         = "settings.misc.newReplayDetectionMethod";
	/** Play "Replay saved" voice key.                  */
	public  static final String KEY_SETTINGS_MISC_PLAY_REPLAY_SAVED_VOICE             = "settings.misc.playReplaySavedVoice";
	/** Delete auto-saved replays key.                  */
	public  static final String KEY_SETTINGS_MISC_DELETE_AUTO_SAVED_REPLAYS           = "settings.misc.deleteAutoSavedReplays";
	/** Auto-open new replays key.                      */
	public  static final String KEY_SETTINGS_MISC_AUTO_OPEN_NEW_REPLAYS               = "settings.misc.autoOpenNewReplays";
	/** Show Game info for new replays key.             */
	public  static final String KEY_SETTINGS_MISC_SHOW_GAME_INFO_FOR_NEW_REPLAYS      = "settings.misc.showGameInfoForNewReplays";
	/** Auto-store new replays key.                     */
	public  static final String KEY_SETTINGS_MISC_AUTO_STORE_NEW_REPLAYS              = "settings.misc.autoStoreNewReplays";
	/** Cache preprocessed replays key.                 */
	public  static final String KEY_SETTINGS_MISC_CACHE_PREPROCESSED_REPLAYS          = "settings.misc.cachePreprocessedReplays";
	/** Use MD5 hash from file name key.                  */
	public  static final String KEY_SETTINGS_MISC_USE_MD5_HASH_FROM_FILE_NAME         = "settings.misc.useMd5HashFromFileName";
	/** Profile info validity time key.                 */
	public  static final String KEY_SETTINGS_MISC_PROFILE_INFO_VALIDITY_TIME          = "settings.misc.profileInfoValidityTime";
	/** Pre-load SC2 icons on startup key.              */
	public  static final String KEY_SETTINGS_MISC_PRELOAD_SC2_ICONS_ON_STARTUP        = "settings.misc.preloadSc2IconsOnStartup";
	/** Custom date format key.                         */
	public  static final String KEY_SETTINGS_MISC_CUSTOM_DATE_FORMAT                  = "settings.misc.customDateFormat";
	/** Custom time format key.                         */
	public  static final String KEY_SETTINGS_MISC_CUSTOM_TIME_FORMAT                  = "settings.misc.customTimeFormat";
	/** Custom date+time format key.                    */
	public  static final String KEY_SETTINGS_MISC_CUSTOM_DATE_TIME_FORMAT             = "settings.misc.customDateTimeFormat";
	/** Enable proxy config key.                        */
	public  static final String KEY_SETTINGS_MISC_ENABLE_PROXY_CONFIG                 = "settings.misc.enableProxyConfig";
	/** HTTP proxy host key.                            */
	public  static final String KEY_SETTINGS_MISC_HTTP_PROXY_HOST                     = "settings.misc.httpProxyHost";
	/** HTTP proxy port key.                            */
	public  static final String KEY_SETTINGS_MISC_HTTP_PROXY_PORT                     = "settings.misc.httpProxyPort";
	/** HTTPS proxy host key.                           */
	public  static final String KEY_SETTINGS_MISC_HTTPS_PROXY_HOST                    = "settings.misc.httpsProxyHost";
	/** HTTPS proxy port key.                           */
	public  static final String KEY_SETTINGS_MISC_HTTPS_PROXY_PORT                    = "settings.misc.httpsProxyPort";
	/** SOCKS proxy host key.                           */
	public  static final String KEY_SETTINGS_MISC_SOCKS_PROXY_HOST                    = "settings.misc.socksProxyHost";
	/** SOCKS proxy port key.                           */
	public  static final String KEY_SETTINGS_MISC_SOCKS_PROXY_PORT                    = "settings.misc.socksProxyPort";
	/** Use Proxy when downloading updates key.         */
	public  static final String KEY_SETTINGS_MISC_USE_PROXY_WHEN_DOWNLOADING_UPDATES  = "settings.misc.useProxyWhenDownloadingUpdates";
	/** Utilized CPU cores key.                         */
	public  static final String KEY_SETTINGS_MISC_UTILIZED_CPU_CORES                  = "settings.misc.utilizedCpuCores";
	/** Player aliases key.                             */
	public  static final String KEY_SETTINGS_MISC_PLAYER_ALIASES                      = "settings.misc.playerAliases";
	/** Map aliases key.                                */
	public  static final String KEY_SETTINGS_MISC_MAP_ALIASES                         = "settings.misc.mapAliases";
	/** Pre-defined lists key.                          */
	public  static final String KEY_SETTINGS_MISC_PREDEFINED_LISTS                    = "settings.misc.predefinedList.";
	/** Authorization key.                              */
	public  static final String KEY_SETTINGS_MISC_AUTHORIZATION_KEY                   = "settings.misc.authorizationKey";
	/** Custom replay sites acknowledged key.           */
	public  static final String KEY_SETTINGS_MISC_CUSTOM_REPLAY_SITES_ACKNOWLEDGED    = "settings.misc.customReplaySitesAcknowledged";
	/** Custom replay sites key.                        */
	private static final String KEY_SETTINGS_MISC_CUSTOM_REPLAY_SITES                 = "settings.misc.customReplaySites";
	/** The application Look and Feel key.              */
	public  static final String KEY_SETTINGS_SAVE_ON_EXIT                             = "settings.saveOnExit"; 
	/** Enable voice notifications key.                 */
	public  static final String KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS               = "settings.enableVoiceNotifications";
	/** Allow only one instance key.                    */
	public  static final String KEY_SETTINGS_ALLOW_ONLY_ONE_INSTANCE                  = "settings.allowOnlyOneInstance";
	/** Allow only one instance key.                    */
	public  static final String KEY_SETTINGS_MAX_REPLAYS_TO_OPEN_FOR_OPEN_IN_ANALYZER = "settings.maxReplaysToOpenForOpenInAnalyzer";
	/** Show Start page on startup key.                 */
	public  static final String KEY_SETTINGS_SHOW_START_PAGE_ON_STARTUP               = "settings.showStartPageOnStartup";
	/** Enable replay auto-save key.                    */
	public  static final String KEY_SETTINGS_ENABLE_REPLAY_AUTO_SAVE                  = "settings.enableReplayAutoSave";
	/** Enable global hotkeys key.                      */
	public  static final String KEY_SETTINGS_ENABLE_GLOBAL_HOTKEYS                    = "settings.enableGlobalHotkeys";
	/** Enable APM alert key.                           */
	public  static final String KEY_SETTINGS_ENABLE_APM_ALERT                         = "settings.enableApmAlert";
	/** Check updates on startup key.                   */
	public  static final String KEY_SETTINGS_CHECK_UPDATES_ON_STARTUP                 = "settings.checkUpdatesOnStartup"; 
	/** Start maximized key.                            */
	public  static final String KEY_WINDOW_START_MAXIMIZED                            = "window.startMaximized"; 
	/** Restore last position on startup key.           */
	public  static final String KEY_WINDOW_RESTORE_LAST_POSITION_ON_STARTUP           = "window.restoreLastPositionOnStartup"; 
	/** Window position key.                            */
	public  static final String KEY_WINDOW_POSITION                                   = "window.position"; 
	/** Start minimized to tray key.                    */
	public  static final String KEY_WINDOW_START_MINIMIZED_TO_TRAY                    = "window.startMinimizedToTray"; 
	/** Minimize to tray on Minimize key.               */
	public  static final String KEY_WINDOW_MINIMIZE_TO_TRAY_ON_MINIMIZE               = "window.minimizeToTrayOnMinimize"; 
	/** Minimize to tray on Close key.                  */
	public  static final String KEY_WINDOW_MINIMIZE_TO_TRAY_ON_CLOSE                  = "window.minimizeToTrayOnClose"; 
	/** The vertical tile strategy key.                 */
	public  static final String KEY_WINDOW_VERTICAL_TILE_STRATEGY                     = "window.verticalTileStrategy"; 
	/** Chart type key.                                 */
	public  static final String KEY_REP_ANALYZER_CHARTS_CHART_TYPE                    = "module.repAnalyzer.charts.chartType"; 
	/** Group by teams key.                             */
	public  static final String KEY_REP_ANALYZER_CHARTS_GROUP_BY_TEAMS                = "module.repAnalyzer.charts.groupByTeams"; 
	/** All players on 1 chart key.                     */
	public  static final String KEY_REP_ANALYZER_CHARTS_ALL_PLAYERS_ON_1_CHART        = "module.repAnalyzer.charts.allPlayersOn1Chart"; 
	/** Use players' colors key.                        */
	public  static final String KEY_REP_ANALYZER_CHARTS_USE_PLAYERS_COLORS            = "module.repAnalyzer.charts.usePlayersColors"; 
	/** Display in seconds key.                         */
	public  static final String KEY_REP_ANALYZER_CHARTS_DISPLAY_IN_SECONDS            = "module.repAnalyzer.charts.displayInSeconds"; 
	/** Chart zoom key.                                 */
	public  static final String KEY_REP_ANALYZER_CHARTS_ZOOM                          = "module.repAnalyzer.charts.zoom"; 
	/** Grid key.                                       */
	public  static final String KEY_REP_ANALYZER_CHARTS_GRID                          = "module.repAnalyzer.charts.grid"; 
	/** Predefined grid key.                            */
	public  static final String KEY_REP_ANALYZER_CHARTS_PREDEFINED_GRID               = "module.repAnalyzer.charts.grid.predefined";  
	/** Grid first marker key.                          */
	public  static final String KEY_REP_ANALYZER_CHARTS_GRID_FIRST_MARKER             = "module.repAnalyzer.charts.grid.firstMarker"; 
	/** Grid repeat marker key.                         */
	public  static final String KEY_REP_ANALYZER_CHARTS_GRID_REPEAT_MARKER            = "module.repAnalyzer.charts.grid.repeatMarker"; 
	/** APM granularity key.                            */
	public  static final String KEY_REP_ANALYZER_CHARTS_APM_GRANULARITY               = "module.repAnalyzer.charts.apm.granularity"; 
	/** APM graph approximation key.                    */
	public  static final String KEY_REP_ANALYZER_CHARTS_APM_GRAPH_APPROXIMATION       = "module.repAnalyzer.charts.apm.graphApproximation"; 
	/** Show EAPM key.                                  */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_EAPM                     = "module.repAnalyzer.charts.apm.showEapm"; 
	/** Show micro/macro APM key.                       */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_MICRO_MACRO_APM          = "module.repAnalyzer.charts.apm.showMicroMacroApm"; 
	/** Show XAPM key.                                  */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_XAPM                     = "module.repAnalyzer.charts.apm.showXapm"; 
	/** Show select hotkeys key.                        */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_SELECT_HOTKEYS           = "module.repAnalyzer.charts.hotkeys.showSelectHotkeys"; 
	/** Show builds key.                                */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_BUILDS                   = "module.repAnalyzer.charts.buildsTech.showBuilds"; 
	/** Show trains key.                                */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_TRAINS                   = "module.repAnalyzer.charts.buildsTech.showTrains"; 
	/** Show workers key.                               */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_WORKERS                  = "module.repAnalyzer.charts.buildsTech.showWorkers"; 
	/** Show researches key.                            */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_RESEARCHES               = "module.repAnalyzer.charts.buildsTech.showResearches"; 
	/** Show upgrades key.                              */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_UPGRADES                 = "module.repAnalyzer.charts.buildsTech.showUpgrades"; 
	/** Show ability groups key.                        */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_ABILITY_GROUPS           = "module.repAnalyzer.charts.buildsTech.showAbilityGroups"; 
	/** Show duration key.                              */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_DURATION                 = "module.repAnalyzer.charts.buildsTech.showDuration"; 
	/** Icon sizes key.                                 */
	public  static final String KEY_REP_ANALYZER_CHARTS_ICON_SIZES                    = "module.repAnalyzer.charts.buildsTech.iconSizes"; 
	/** Show units stat key.                            */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_UNITS_STAT               = "module.repAnalyzer.charts.buildsTechStat.showUnits"; 
	/** Show buildings stat key.                        */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_BUILDINGS_STAT           = "module.repAnalyzer.charts.buildsTechStat.showBuildings"; 
	/** Show researches stat key.                       */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_RESEARCHES_STAT          = "module.repAnalyzer.charts.buildsTechStat.showResearches"; 
	/** Show upgrades stat key.                         */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_UPGRADES_STAT            = "module.repAnalyzer.charts.buildsTechStat.showUpgrades"; 
	/** Show ability groups stat key.                   */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_ABILITY_GROUPS_STAT      = "module.repAnalyzer.charts.buildsTechStat.showAbilityGroups"; 
	/** Bar size key.                                   */
	public  static final String KEY_REP_ANALYZER_CHARTS_BAR_SIZE                      = "module.repAnalyzer.charts.buildsTechStat.barSize"; 
	/** Show after completed key.                       */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_AFTER_COMPLETED          = "module.repAnalyzer.charts.showAfterCompleted"; 
	/** Map view quality key.                           */
	public  static final String KEY_REP_ANALYZER_CHARTS_MAP_VIEW_QUALITY              = "module.repAnalyzer.charts.mapView.quality"; 
	/** Map background key.                             */
	public  static final String KEY_REP_ANALYZER_CHARTS_MAP_BACKGROUND                = "module.repAnalyzer.charts.mapView.background"; 
	/** Area granularity key.                           */
	public  static final String KEY_REP_ANALYZER_CHARTS_AREA_GRANULARITY              = "module.repAnalyzer.charts.mapView.areaGranularity"; 
	/** Hide overlapped buildings key.                  */
	public  static final String KEY_REP_ANALYZER_CHARTS_HIDE_OVERLAPPED_BUILDINGS     = "module.repAnalyzer.charts.mapView.hideOverlappedBuildings"; 
	/** Fill building icons key.                        */
	public  static final String KEY_REP_ANALYZER_CHARTS_FILL_BUILDING_ICONS           = "module.repAnalyzer.charts.mapView.fillBuildingIcons"; 
	/** Show map objects key.                           */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_MAP_OBJECTS              = "module.repAnalyzer.charts.mapView.showMapObjects"; 
	/** Show percent key.                               */
	public  static final String KEY_REP_ANALYZER_CHARTS_SHOW_PERCENT                  = "module.repAnalyzer.charts.actionDistribution.showPercent"; 
	/** Distribution bar size key.                      */
	public  static final String KEY_REP_ANALYZER_CHARTS_DISTRIBUTION_BAR_SIZE         = "module.repAnalyzer.charts.actionDistribution.barSize"; 
	/** Unit Tiers granularity key.                     */
	public  static final String KEY_REP_ANALYZER_CHARTS_UNIT_TIERS_GRANULARITY        = "module.repAnalyzer.charts.unitTiersGranularity"; 
	/** Stretch bars key.                               */
	public  static final String KEY_REP_ANALYZER_CHARTS_STRETCH_BARS                  = "module.repAnalyzer.charts.stretchBars"; 
	/** Calculate until time marker key.                */
	public  static final String KEY_REP_ANALYZER_CHARTS_CALCULATE_UNTIL_TIME_MARKER   = "module.repAnalyzer.charts.mainBuildingControl.calculateUntilTimeMarker";
	/** Resource Spending Rate granularity key.         */
	public  static final String KEY_REP_ANALYZER_CHARTS_RSR_GRANULARITY               = "module.repAnalyzer.charts.resourceSpendingRate.granularity"; 
	/** Resource Spending Rate graph approximation key. */
	public  static final String KEY_REP_ANALYZER_CHARTS_RSR_GRAPH_APPROXIMATION       = "module.repAnalyzer.charts.resourceSpendingRate.graphApproximation";
	/** Resources spent cumulative key.                 */
	public  static final String KEY_REP_ANALYZER_CHARTS_RS_CUMULATIVE                 = "module.repAnalyzer.charts.resourcesSpent.cumulative";
	/** Resources spent granularity key.                */
	public  static final String KEY_REP_ANALYZER_CHARTS_RS_GRANULARITY                = "module.repAnalyzer.charts.resourcesSpent.granularity";
	/** Produced Army/Supply cumulative key.            */
	public  static final String KEY_REP_ANALYZER_CHARTS_PAS_CUMULATIVE                = "module.repAnalyzer.charts.producedArmySupply.cumulative";
	/** Produced Army/Supply granularity key.                */
	public  static final String KEY_REP_ANALYZER_CHARTS_PAS_GRANULARITY               = "module.repAnalyzer.charts.producedArmySupply.granularity";
	/** Include initial units key.                      */
	public  static final String KEY_REP_ANALYZER_CHARTS_INCLUDE_INITIAL_UNITS         = "module.repAnalyzer.charts.producedArmySupply.includeInitialUnits"; 
	/** Red. show percent key.                          */
	public  static final String KEY_REP_ANALYZER_CHARTS_RED_SHOW_PERCENT              = "module.repAnalyzer.charts.redundancyDistribution.showPercent"; 
	/** Red. distribution bar size key.                 */
	public  static final String KEY_REP_ANALYZER_CHARTS_RED_DISTRIBUTION_BAR_SIZE     = "module.repAnalyzer.charts.redundancyDistribution.barSize"; 
	/** Max frame break key.                            */
	public  static final String KEY_REP_ANALYZER_CHARTS_MAX_FRAME_BREAK               = "module.repAnalyzer.charts.actionSequences.maxFrameBreak"; 
	/** Group same productions key.                     */
	public  static final String KEY_REP_ANALYZER_CHARTS_GROUP_SAME_PRODUCTIONS        = "module.repAnalyzer.charts.productions.groupSameProductions"; 
	/** Icon sizes (Productions) key.                   */
	public  static final String KEY_REP_ANALYZER_CHARTS_ICON_SIZES_P                  = "module.repAnalyzer.charts.productions.iconSizes"; 
	/** Icon sizes (Player Selections) key.             */
	public  static final String KEY_REP_ANALYZER_CHARTS_ICON_SIZES_PS                 = "module.repAnalyzer.charts.playerSelections.iconSizes"; 
	/** Action type keys.                               */
	public  static final String KEY_REP_ANALYZER_CHARTS_ACTIONS_ACTION_TYPES          = "module.repAnalyzer.charts.actions.actionType."; 
	/** Invert action list colors key.                  */
	public  static final String KEY_REP_ANALYZER_CHARTS_ACTIONS_INVERT_COLORS         = "module.repAnalyzer.charts.actions.invertColors"; 
	/** Action list icon size key.                      */
	public  static final String KEY_REP_ANALYZER_CHARTS_ACTIONS_ICON_SIZE             = "module.repAnalyzer.charts.actions.iconSize"; 
	/** Use listed actions as chart input data key.     */
	public  static final String KEY_REP_ANALYZER_CHARTS_ACTIONS_USE_LISTED_AS_INPUT   = "module.repAnalyzer.charts.actions.useListedAsInput"; 
	/** In-game chat hide message targets key.          */
	public  static final String KEY_REP_ANALYZER_IN_GAME_CHAT_HIDE_MESSAGE_TARGETS    = "module.repAnalyzer.inGameChat.hideMessageTargets"; 
	/** In-game chat show blinks key.                   */
	public  static final String KEY_REP_ANALYZER_IN_GAME_CHAT_SHOW_BLINKS             = "module.repAnalyzer.inGameChat.showBlinks"; 
	/** In-game chat format into paragraphs key.        */
	public  static final String KEY_REP_ANALYZER_IN_GAME_CHAT_FORMAT_INTO_PARAGRAPHS  = "module.repAnalyzer.inGameChat.formatIntoParagraphs"; 
	/** In-game chat target language key.               */
	public  static final String KEY_REP_ANALYZER_IN_GAME_CHAT_TARGET_LANGUAGE         = "module.repAnalyzer.inGameChat.targetLanguage"; 
	/** In-game chat source language key.               */
	public  static final String KEY_REP_ANALYZER_IN_GAME_CHAT_SOURCE_LANGUAGE         = "module.repAnalyzer.inGameChat.sourceLanguage"; 
	/** Download from gateway key.                      */
	public  static final String KEY_REP_ANALYZER_MAP_PREVIEW_DOWNLOAD_FROM_GATEWAY    = "module.repAnalyzer.mapPreview.downloadFromGateway"; 
	/** Map preview zoom key.                           */
	public  static final String KEY_REP_ANALYZER_MAP_PREVIEW_ZOOM                     = "module.repAnalyzer.mapPreview.zoom"; 
	/** Map attributes locale key.                      */
	public  static final String KEY_REP_ANALYZER_MAP_PREVIEW_LOCALE                   = "module.repAnalyzer.mapPreview.locale"; 
	/** Public comments user name key.                  */
	public  static final String KEY_REP_ANALYZER_PUBLIC_COMMENTS_USER_NAME            = "module.repAnalyzer.publicComments.userName"; 
	/** Auto-open first player key.                     */
	public  static final String KEY_MULTI_REP_ANAL_AUTO_OPEN_FIRST_PLAYER             = "module.multiRepAnal.autoOpenFirstPlayer"; 
	/** Stretch tables to window key.                   */
	public  static final String KEY_MULTI_REP_ANAL_STRETCH_TO_WINDOW                  = "module.multiRepAnal.stretchToWindow"; 
	/** Multi-rep anal chart type key.                  */
	public  static final String KEY_MULTI_REP_ANAL_CHARTS_CHART_TYPE                  = "module.multiRepAnal.charts.chartType"; 
	/** Multi-rep anal chart granularity key.           */
	public  static final String KEY_MULTI_REP_ANAL_CHARTS_CHART_GRANULARITY           = "module.multiRepAnal.charts.chartGranularity"; 
	/** Multi-rep anal graph approximation key.         */
	public  static final String KEY_MULTI_REP_ANAL_CHARTS_GRAPH_APPROXIMATION         = "module.multiRepAnal.charts.graphApproximation";
	/** Activity trend type key.                        */
	public  static final String KEY_MULTI_REP_ANAL_TRENDS_TREND_TYPE                  = "module.multiRepAnal.trends.trendType";
	/** Source auto sort key.                           */
	public  static final String KEY_REP_SEARCH_SOURCE_AUTO_SORT                       = "module.repSearch.source.autoSort"; 
	/** Stretch results table to window key.            */
	public  static final String KEY_REP_SEARCH_RESULTS_STRETCH_TO_WINDOW              = "module.repSearch.results.stretchToWindow"; 
	/** Color Win-Loss key.                             */
	public  static final String KEY_REP_SEARCH_RESULTS_COLOR_WIN_LOSS                 = "module.repSearch.results.colorWinLoss"; 
	/** Visible Replay list column keys key.            */
	private static final String KEY_REP_SEARCH_VISIBLE_REPLAY_LIST_COLUMN_KEYS        = "module.repSearch.results.visibleReplayListColumnkeys"; 
	/** Include path in zip key.                        */
	public  static final String KEY_REP_SEARCH_RESULTS_INCLUDE_PATH_IN_ZIP            = "module.repSearch.results.includePathInZip"; 
	/** Replay rename template key.                     */
	public  static final String KEY_REP_SEARCH_RESULTS_RENAME_TEMPLATE                = "module.repSearch.results.renameTemplate"; 
	/** Replay site key.                                */
	public  static final String KEY_SHARE_REP_REPLAY_SITE                             = "shareRep.replaySite"; 
	/** Replay site key.                                */
	public  static final String KEY_SHARE_REP_USER_NAME                               = "shareRep.userName"; 
	/** Latest import folder key.                       */
	public  static final String KEY_IMPORT_BO_LATEST_IMPORT_FOLDER                    = "importBO.latestImportFolder"; 
	/** Alert when done key.                            */
	public  static final String KEY_PROGRESS_DIALOG_ALERT_WHEN_DONE                   = "progressDialog.alertWhenDone"; 
	/** Mouse print refresh rate key.                   */
	public  static final String KEY_MOUSE_PRINT_REFRESH_RATE                          = "mousePrint.refreshRate"; 
	/** Mouse print image zoom key.                     */
	public  static final String KEY_MOUSE_PRINT_IMAGE_ZOOM                            = "mousePrint.imageZoom"; 
	/** Show new to Sc2gears dialog on startup key.     */
	public  static final String KEY_SHOW_NEW_TO_SC2GEARS_DIALOG_ON_STARTUP            = "showNewToSc2gearsDialogOnStartup"; 
	/** Start page channel key.                         */
	public  static final String KEY_START_PAGE_CHANNEL                                = "startPageChannel"; 
	/** Sc2gears Database downloader file type key.     */
	public  static final String KEY_SC2GEARS_DATABASE_DOWNLOADER_FILE_TYPE            = "sc2gearsDatabaseDownloader.fileType"; 
	/** Sc2gears Database downloader file type key.     */
	public  static final String KEY_SC2GEARS_DATABASE_DOWNLOADER_TARGET_FOLDER        = "sc2gearsDatabaseDownloader.targetFolder"; 
	/** Allow incompatible plugins key.                 */
	public  static final String KEY_PLUGIN_MANAGER_ALLOW_INCOMPATIBLE_PLUGINS         = "pluginManager.allowIncompatiblePlugins"; 
	/** Plugin manager enabled plugins key.             */
	private static final String KEY_PLUGIN_MANAGER_ENABLED_PLUGINS                    = "pluginManager.enabledPlugins"; 
	/** On top game info location key.                  */
	public  static final String KEY_ON_TOP_GAME_INFO_LOCATION                         = "onTopGameInfo.location"; 
	/** On top game info locked key.                    */
	public  static final String KEY_ON_TOP_GAME_INFO_LOCKED                           = "onTopGameInfo.locked"; 
	/** On top APM display location key.                */
	public  static final String KEY_ON_TOP_APM_DISPLAY_LOCATION                       = "onTopApmDisplay.location";
	/** On top APM display locked key.                  */
	public  static final String KEY_ON_TOP_APM_DISPLAY_LOCKED                         = "onTopApmDisplay.locked";
	/** On top APM display font size key.               */
	public  static final String KEY_ON_TOP_APM_DISPLAY_FONT_SIZE                      = "onTopApmDisplay.fontSize";
	/** Word cloud font key.                            */
	public  static final String KEY_WORD_CLOUD_FONT                                   = "wordCloud.font";
	/** Word cloud min font size key.                   */
	public  static final String KEY_WORD_CLOUD_MIN_FONT_SIZE                          = "wordCloud.minFontSize";
	/** Word cloud max font size key.                   */
	public  static final String KEY_WORD_CLOUD_MAX_FONT_SIZE                          = "wordCloud.maxFontSize";
	/** Word cloud max words key.                       */
	public  static final String KEY_WORD_CLOUD_MAX_WORDS                              = "wordCloud.maxWords";
	/** Word cloud use colors key.                      */
	public  static final String KEY_WORD_CLOUD_USE_COLORS                             = "wordCloud.useColors";
	/** Mouse practice game check user score key.       */
	public  static final String KEY_MOUSE_PRACTICE_CHECK_USER_SCORE                   = "mousePractice.checkUserScore";
	/** Mouse practice game enable custom rules key.    */
	public  static final String KEY_MOUSE_PRACTICE_ENABLE_CUSTOM_RULES                = "mousePractice.enableCustomRules";
	/** Mouse practice game color blind key.            */
	public  static final String KEY_MOUSE_PRACTICE_COLOR_BLIND                        = "mousePractice.colorBlind";
	/** Mouse practice game user name key.              */
	public  static final String KEY_MOUSE_PRACTICE_USER_NAME                          = "mousePractice.userName";
	/** Mouse game FPS key.                             */
	public  static final String KEY_SETTINGS_MOUSE_GAME_FPS                           = "settings.misc.mouseGameFps";
	/** Mouse game max disc radius key.                 */
	public  static final String KEY_SETTINGS_MOUSE_GAME_MAX_DISC_RADIUS               = "settings.misc.mouseGameMaxDiscRadius";
	/** Mouse game max disc age key.                    */
	public  static final String KEY_SETTINGS_MOUSE_GAME_MAX_DISC_AGE                  = "settings.misc.mouseGameMaxDiscAge";
	/** Mouse game max disc score key.                  */
	public  static final String KEY_SETTINGS_MOUSE_GAME_MAX_DISC_SCORE                = "settings.misc.mouseGameMaxDiscScore";
	/** Mouse game max disc score key.                  */
	public  static final String KEY_SETTINGS_MOUSE_GAME_MAX_DISCS_MISSED              = "settings.misc.mouseGameMaxDiscsMissed";
	/** Mouse game disc speed key.                      */
	public  static final String KEY_SETTINGS_MOUSE_GAME_DISC_SPEED                    = "settings.misc.mouseGameDiscSpeed";
	/** Mouse game friendly disc probability key.       */
	public  static final String KEY_SETTINGS_MOUSE_GAME_FRIENDLY_DISC_PROBABILITY     = "settings.misc.mouseGameFriendlyDiscProbability";
	/** Mouse game initial delay for new disc key.      */
	public  static final String KEY_SETTINGS_MOUSE_GAME_INITIAL_DELAY_FOR_NEW_DISC    = "settings.misc.mouseGameInitialDelayForNewDisc";
	/** Mouse game new disc delay decrement key.        */
	public  static final String KEY_SETTINGS_MOUSE_GAME_NEW_DISC_DELAY_DECREMENT      = "settings.misc.mouseGameNewDiscDelayDecrement";
	/** Mouse game random seed key.                     */
	public  static final String KEY_SETTINGS_MOUSE_GAME_RANDOM_SEED                   = "settings.misc.mouseGameRandomSeed";
	/** Mouse game paint disc center cross key.         */
	public  static final String KEY_SETTINGS_MOUSE_GAME_PAINT_DISC_CENTER_CROSS       = "settings.misc.mouseGamePaintDiscCenterCross";
	/** Mouse game paint max disc outline key.          */
	public  static final String KEY_SETTINGS_MOUSE_GAME_PAINT_MAX_DISC_OUTLINE        = "settings.misc.mouseGamePaintMaxDiscOutline";
	/** Stream name key.                                */
	public  static final String KEY_PRIVATE_STREAMING_STREAM_NAME                     = "privateStreaming.streamName";
	/** Stream name key.                                */
	public  static final String KEY_PRIVATE_STREAMING_STREAM_PASSWORD                 = "privateStreaming.streamPassword";
	/** Stream name key.                                */
	public  static final String KEY_PRIVATE_STREAMING_STREAM_DESCRIPTION              = "privateStreaming.streamDescription";
	/** Screen capture method key.                      */
	public  static final String KEY_PRIVATE_STREAMING_SCREEN_CAPTURE_METHOD           = "privateStreaming.screenCaptureMethod";
	/** Screen area to stream key.                      */
	public  static final String KEY_PRIVATE_STREAMING_SCREEN_AREA_TO_STREAM           = "privateStreaming.screenAreaToStream";
	/** Custom area left key.                           */
	public  static final String KEY_PRIVATE_STREAMING_CUSTOM_AREA_LEFT                = "privateStreaming.customAreaLeft";
	/** Custom area top key.                            */
	public  static final String KEY_PRIVATE_STREAMING_CUSTOM_AREA_TOP                 = "privateStreaming.customAreaTop";
	/** Custom area right key.                          */
	public  static final String KEY_PRIVATE_STREAMING_CUSTOM_AREA_RIGHT               = "privateStreaming.customAreaRight";
	/** Custom area bottom key.                         */
	public  static final String KEY_PRIVATE_STREAMING_CUSTOM_AREA_BOTTOM              = "privateStreaming.customAreaBottom";
	/** Output image size key.                          */
	public  static final String KEY_PRIVATE_STREAMING_OUTPUT_VIDEO_SIZE               = "privateStreaming.outputVideoSize";
	/** Custom resize width key.                        */
	public  static final String KEY_PRIVATE_STREAMING_CUSTOM_RESIZE_WIDTH             = "privateStreaming.customResizeWidth";
	/** Custom resize height key.                       */
	public  static final String KEY_PRIVATE_STREAMING_CUSTOM_RESIZE_HEIGHT            = "privateStreaming.customResizeHeight";
	/** Refresh rate FPS key.                           */
	public  static final String KEY_PRIVATE_STREAMING_REFRESH_RATE_FPS                = "privateStreaming.refreshRateFps";
	/** Server port key.                                */
	public  static final String KEY_PRIVATE_STREAMING_SERVER_PORT                     = "privateStreaming.serverPort";
	/** Image quality key.                              */
	public  static final String KEY_PRIVATE_STREAMING_IMAGE_QUALITY                   = "privateStreaming.imageQuality";
	/** Save video as AVI key.                          */
	public  static final String KEY_PRIVATE_STREAMING_SAVE_VIDEO_AS_AVI               = "privateStreaming.saveVideoAsAvi";
	/** Save video folder key.                          */
	public  static final String KEY_PRIVATE_STREAMING_SAVE_VIDEO_FOLDER               = "privateStreaming.saveVideoFolder";
	/** Fix AVI FPS key.                                */
	public  static final String KEY_PRIVATE_STREAMING_FIX_AVI_FPS                     = "privateStreaming.fixAviFps";
	
	/** The default application language.            */
	public static final String DEFAULT_APP_LANGUAGE  = "English";
	/** The default application look and feel.       */
	public static final String DEFAULT_APP_LAF       = "Nimbus";
	// MAC specific default LAF is disabled for now: it displays vertical tabs wrong
	/*static {
		String DEFAULT_APP_LAF_ = null;
		// MAC users only like the MAC GUI, for them set the system LAF
		if ( GeneralUtils.isOsMac() ) {
			final String systemLafClassName = UIManager.getSystemLookAndFeelClassName();
			for ( final LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels() )
				if ( systemLafClassName.equals( lafInfo.getClassName() ) ) {
					DEFAULT_APP_LAF_ = lafInfo.getName();
					break;
				}
		}
		DEFAULT_APP_LAF = DEFAULT_APP_LAF_ == null ? "Nimbus" : DEFAULT_APP_LAF_;
	}*/
	
	/**
	 * Pre-defined lists.
	 * @author Andras Belicza
	 */
	public static enum PredefinedList {
		/** Replay analyzer search pre-defined list. */
		REP_ANAL_SEARCH( Icons.CHART, "miscSettings.predefinedList.repAnalSearch", "repAnalSearch",
				  "\nleave game"
				+ "\nresources"
				+ "\ntoggle" ),
		/** Replay analyzer filter pre-defined list. */
		REP_ANAL_FILTER( Icons.CHART, "miscSettings.predefinedList.repAnalFilter", "repAnalFilter",
				  "\n[WORKERS]     OR train probe OR train scv OR train drone OR calldown mule"
				+ "\n[SUPPLY]     OR build pylon OR build supply depot OR train overlord OR calldown extra supplies"
				+ "\n[EXPAND]     OR build nexus OR build command center OR build hatchery"
				+ "\n[DEFENSE]     OR build cannon OR build bunker OR build missile turret OR build spine crawler OR build spore crawler"
				+ "\n[TRADE]     OR resources"
				+ "\n[TIER 1]     OR train zergling OR train marine OR train zealot"
				+ "\n[TIER 1.5]     OR train roach OR morph baneling OR train marauder OR train reaper OR train stalker OR train sentry"
				+ "\n[TIER 2]     OR train hydralisk OR train infestor OR train mutalisk OR train corruptor OR morph overseer OR train ghost OR train hellion OR train immortal OR train phoenix OR train void ray OR train observer OR train warp prism"
				+ "\n[TIER 2.5]     OR train siege tank OR train medivac OR train viking OR train banshee"
				+ "\n[TIER 3]     OR train ultralisk OR morph brood lord OR train raven OR train battlecruiser OR train thor OR train colossus OR train mothership OR train carrier OR train high templar OR train dark templar" ),
		/** Replay analyzer filter out pre-defined list. */
		REP_ANAL_FILTER_OUT( Icons.CHART, "miscSettings.predefinedList.repAnalFilterOut", "repAnalFilterOut", "\nright click" ),
		/** Replay auto-save template. */
		REP_AUTO_SAVE_TEMPLATE( Icons.UI_SCROLL_PANE_LIST, "miscSettings.predefinedList.repAutoSaveTemplate", "repAutoSaveTemplate",
				  "\n/D  /h./e"
				+ "\n/D./e"
				+ "\n/F4 </p (/r)>./e"
				+ "\n/f\\/F4 </p (/r)>./e"
				+ "\n/Y\\/F4 </p (/r)>./e"
				+ "\n/m\\/F4 </p (/r)>./e"
				+ "\n/G\\</p (/r)>./e"
				+ "\n/G\\/T - </p>./e"
				+ "\n/D </p>./e"
				+ "\n/D [/m] </p>./e"
				+ "\n/m - </p (/r)>./e"
				+ "\n/m - /T - </p (/r)>./e"
				+ "\n/m\\/T - </p (/r)>./e" ),
		/** Replay rename template pre-defined list. */
		REP_RENAME_TEMPLATE( Icons.DOCUMENT_RENAME, "miscSettings.predefinedList.repRenameTemplate", "repRenameTemplate",
				  "\n/D  /h./e"
				+ "\n/D./e"
				+ "\n/D - /m./e"
				+ "\n/T - /m - /q./e"
				+ "\n/C4 - /m./e"
				+ "\n/C4 - </p (/r)>./e"
				+ "\n/G\\</p (/r)>./e"
				+ "\n/G\\/T - </p>./e" ),
		/** Replay search player name pre-defined list. */
		REP_SEARCH_PLAYER_NAME( Icons.BINOCULAR, "miscSettings.predefinedList.repSearchPlayerName", "repSearchPlayerName", "" ),
		/** Replay search player name pre-defined list. */
		REP_SEARCH_FULL_PLAYER_NAME( Icons.BINOCULAR, "miscSettings.predefinedList.repSearchFullPlayerName", "repSearchFullPlayerName", "" ),
		/** Replay search map name pre-defined list. */
		REP_SEARCH_MAP_NAME( Icons.BINOCULAR, "miscSettings.predefinedList.repSearchMapName", "repSearchMapName",
				  "\nAbyss\nAgria Valley\nArakan Citadel\nArid Wastes\nBlistering Sands\nCoalition\nColony 426\nDecena\nDelta Quadrant\nDesert Oasis"
				+ "\nDig Site\nDiscord IV\nExtinction\nFrontier\nHigh Ground\nHigh Orbit\nIncineration Zone\nJungle Basin\nKulas Ravine\nLava Flow"
				+ "\nLost Temple\nMegaton\nMetalopolis\nMonlyth Ridge\nMonsoon\nOutpost\nQuicksand\nSand Canyon\nScorched Haven\nScrap Station"
				+ "\nShakuras Plateau\nSteppes of War\nTarsonis Assault\nTempest\nThe Bio Lab\nToxic Slums\nTwilight Fortress\nTyphon\nUlaan Deeps\nWar Zone\nXel'Naga Caverns" ),
		/** Replay search race match-up pre-defined list. */
		REP_SEARCH_RACE_MATCHUP( Icons.BINOCULAR, "miscSettings.predefinedList.repSearchRaceMatchup", "repSearchRaceMatchup",
				  "\n*vP"
				+ "\n*vT"
				+ "\n*vZ"
				+ "\n*vR"
				+ "\nPv*"
				+ "\nTv*"
				+ "\nZv*"
				+ "\nRv*" ),
		/** Replay search race match-up pre-defined list. */
		REP_SEARCH_LEAGUE_MATCHUP( Icons.BINOCULAR, "miscSettings.predefinedList.repSearchLeagueMatchup", "repSearchLeagueMatchup",
				  "\n*vR"
				+ "\n*vM"
				+ "\n*vD"
				+ "\n*vP"
				+ "\n*vG"
				+ "\n*vS"
				+ "\n*vB" ),
		/** Replay search file name pre-defined list. */
		REP_SEARCH_FILE_NAME( Icons.BINOCULAR, "miscSettings.predefinedList.repSearchFileName", "repSearchFileName", "" ),
		/** Replay search chat message pre-defined list. */
		REP_SEARCH_CHAT_MESSAGE( Icons.BINOCULAR, "miscSettings.predefinedList.repSearchChatMessage", "repSearchChatMessage", "\ngg\ngl hf" ),
		/** Replay search build order pre-defined list. */
		REP_SEARCH_BUILD_ORDER( Icons.BINOCULAR, "miscSettings.predefinedList.repSearchBuildOrder", "repSearchBuildOrder",
				  "\nPylon, Gateway, Assimilator, Pylon, Cybernetics Core"
				+ "\nPylon, Gateway, Pylon, Gateway"
				+ "\nPylon, Forge"
				+ "\nPylon, Gateway, Pylon, Nexus"
				+ "\nPylon, Nexus"
				+ "\nSupply Depot, Barracks, Refinery, Supply Depot, Tech Lab (Barracks)"
				+ "\nSupply Depot, Barracks, Barracks, Supply Depot"
				+ "\nSupply Depot, Barracks, Refinery, Supply Depot, Factory"
				+ "\nSupply Depot, Barracks, Supply Depot, Command Center"
				+ "\nSupply Depot, Refinery, Barracks, Supply Depot"
				+ "\nSpawning Pool, Extractor, Lair"
				+ "\nSpawning Pool, Extractor, Roach Warren"
				+ "\nSpawning Pool, Extractor, Baneling Nest"
				+ "\nSpawning Pool, Hatchery, Extractor"
				+ "\nSpawning Pool, Extractor, Hatchery" ),
		/** Replay share user name pre-defined list. */
		REP_SHARE_USER_NAME( Icons.DOCUMENT_SHARE, "miscSettings.predefinedList.repShareUserName", "repShareUserName", "" ),
		/** Custom date format pre-defined list. */
		CUSTOM_DATE_FORMAT( Icons.CALENDAR_BLUE, "miscSettings.predefinedList.customDateFormat", "customDateFormat",
				  "\nyyyy-MM-dd"
				+ "\nyyyy.MM.dd"
				+ "\nyyyy/MM/dd"
				+ "\nyy-MM-dd"
				+ "\nyy.MM.dd"
				+ "\nyy/MM/dd" ),
		/** Custom time format pre-defined list. */
		CUSTOM_TIME_FORMAT( Icons.CALENDAR_BLUE, "miscSettings.predefinedList.customTimeFormat", "customTimeFormat",
				  "\nHH:mm:ss"
				+ "\nHH-mm-ss" ),
		/** Custom date+time format pre-defined list. */
		CUSTOM_DATE_TIME_FORMAT( Icons.CALENDAR_BLUE, "miscSettings.predefinedList.customDateTimeFormat", "customDateTimeFormat",
				  "\nyyyy-MM-dd HH:mm:ss"
				+ "\nyyyy.MM.dd HH:mm:ss"
				+ "\nyyyy/MM/dd HH:mm:ss"
				+ "\nyy-MM-dd HH:mm:ss"
				+ "\nyy.MM.dd HH:mm:ss"
				+ "\nyy/MM/dd HH:mm:ss" );
		
		/** The icon associated with this pre-defined list. */
		public final Icon   icon;
		/** Cache of the string value.                      */
		public final String stringValue;
		/** Settings key of this pre-defined list.          */
		public final String settingsKey;
		/** Default value of the pre-defined list.          */
		public final String defaultValue;
		
		/**
		 * Creates a new PredefinedList.
		 * 
		 * @param icon               the icon associated with this pre-defined list
		 * @param textKey            key of the text representation
		 * @param settingsKeyPostTag post tag of the key of this pre-defined list in the settings
		 * @param defaultValue       default value of the pre-defined list
		 */
		private PredefinedList( final Icon icon, final String textKey, final String settingsKeyPostTag, final String defaultValue ) {
			this.icon         = icon;
			this.stringValue  = Language.getText( textKey );
			this.settingsKey  = KEY_SETTINGS_MISC_PREDEFINED_LISTS + settingsKeyPostTag;
			this.defaultValue = defaultValue;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/** Properties holding the default settings. */
	private static final Properties DEFAULT_PROPERTIES = new Properties();
	static {
		DEFAULT_PROPERTIES.setProperty( KEY_RECENT_REPLAYS_COUNT                             , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_NAVIGATION_SHOW_BAR                              , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_NAVIGATION_USE_SMALL_FONT                        , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_NAVIGATION_BAR_POSITION                          , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_LANGUAGE                                , DEFAULT_APP_LANGUAGE );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_LANGUAGE_LEETNESS_LEVEL                 , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_LAF                                     , DEFAULT_APP_LAF );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_VOICE                                   , "smix" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_SOUND_VOLUME                       , "70" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_APM_ALERT_LEVEL                    , "40" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_APM_WARMUP_TIME                    , "90" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_APM_CHECK_INTERVAL_SEC             , "2" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_ALERT_WHEN_APM_IS_BACK_TO_NORMAL   , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_APM_ALERT_REPETITION_INTERVAL_SEC  , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_ALERT_ON_GAME_START                , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_ALERT_ON_GAME_END                  , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_SHOW_HIDE_APM_DISPLAY_ON_START_END , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_SAVE_MOUSE_PRINTS                  , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_STORE_MOUSE_PRINTS                 , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_MOUSE_PRINT_SAMPLING_TIME          , "1" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_MOUSE_PRINT_WHAT_TO_SAVE           , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_MOUSE_PRINT_DATA_COMPRESSION       , "2" ); // BZip2
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_MOUSE_PRINT_IMAGE_OUTPUT_FORMAT    , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_MOUSE_PRINT_USE_ANTIALIASING       , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_MOUSE_PRINT_BACKGROUND_COLOR       , "255,255,255" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_MOUSE_PRINT_COLOR                  , "0,0,0" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_MOUSE_PRINT_POUR_INK_IDLE_TIME     , "3" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_MOUSE_PRINT_IDLE_INK_FLOW_RATE     , "200" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_MOUSE_WARMUP_TIME                  , "50" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_NEW_REP_CHECK_INTERVAL_SEC         , "3" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_NAV_BAR_INITIAL_WIDTH              , "250" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_TOOL_TIP_INITIAL_DELAY             , "200" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_TOOL_TIP_DISMISS_DELAY             , "10000" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_DISPLAY_INFO_WHEN_STARTED_MINIMIZED, "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_TIME_TO_KEEP_LOG_FILES             , "14" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_TIME_LIMIT_FOR_MULTI_REP_ANALYSIS  , "120" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_BUILD_ORDER_LENGTH                 , "6" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_CHARTS_ACTION_LIST_PARTITIONING    , "70" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_INITIAL_TIME_TO_EXCLUDE_FROM_APM   , "110" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_GAME_LENGTH_RECORDS_GRANULARITY    , "5" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_MAX_GAMING_SESSION_BREAK           , "60" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_REP_AUTO_SAVE_NAME_TEMPLATE        , "/D  /h./e" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_FAVORED_PLAYER_LIST                , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_REARRANGE_PLAYERS_IN_REP_ANALYZER  , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_PREFERRED_BNET_LANGUAGE            , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_MISC_ANALYZER_SHOW_PROFILE_INFO              , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_MISC_ANALYZER_AUTO_RETRIEVE_EXT_PROFILE_INFO , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_MISC_ANALYZER_MAX_ROWS_IN_PROFILE_TOOL_TIP   , "16" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_USE_REAL_TIME_MEASUREMENT          , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_SHOW_WINNERS                       , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_DECLARE_LARGEST_AS_WINNER          , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_OVERRIDE_FORMAT_BASED_ON_MATCHUP   , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_ANIMATOR_FPS                       , "16" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_ANIMATOR_JUMP_TIME                 , "60" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_ANIMATOR_SPEED                     , "5" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_NEW_REPLAY_DETECTION_METHOD        , GeneralUtils.isWindows() ? "1" : "0" ); // Default Event based for Windows, default Polling for other OS
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_PLAY_REPLAY_SAVED_VOICE            , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_DELETE_AUTO_SAVED_REPLAYS          , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_AUTO_OPEN_NEW_REPLAYS              , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_SHOW_GAME_INFO_FOR_NEW_REPLAYS     , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_AUTO_STORE_NEW_REPLAYS             , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_CACHE_PREPROCESSED_REPLAYS         , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_USE_MD5_HASH_FROM_FILE_NAME        , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_PROFILE_INFO_VALIDITY_TIME         , "3" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_PRELOAD_SC2_ICONS_ON_STARTUP       , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_CUSTOM_DATE_FORMAT                 , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_CUSTOM_TIME_FORMAT                 , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_CUSTOM_DATE_TIME_FORMAT            , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_ENABLE_PROXY_CONFIG                , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_HTTP_PROXY_HOST                    , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_HTTP_PROXY_PORT                    , "80" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_HTTPS_PROXY_HOST                   , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_HTTPS_PROXY_PORT                   , "443" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_SOCKS_PROXY_HOST                   , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_SOCKS_PROXY_PORT                   , "1080" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_USE_PROXY_WHEN_DOWNLOADING_UPDATES , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_UTILIZED_CPU_CORES                 , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_PLAYER_ALIASES                     , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_MAP_ALIASES                        , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_AUTHORIZATION_KEY                  , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_CUSTOM_REPLAY_SITES_ACKNOWLEDGED   , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MISC_CUSTOM_REPLAY_SITES                , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_SAVE_ON_EXIT                            , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS              , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_ALLOW_ONLY_ONE_INSTANCE                 , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MAX_REPLAYS_TO_OPEN_FOR_OPEN_IN_ANALYZER, "5" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_SHOW_START_PAGE_ON_STARTUP              , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_ENABLE_REPLAY_AUTO_SAVE                 , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_ENABLE_GLOBAL_HOTKEYS                   , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_ENABLE_APM_ALERT                        , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_CHECK_UPDATES_ON_STARTUP                , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_WINDOW_START_MAXIMIZED                           , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_WINDOW_RESTORE_LAST_POSITION_ON_STARTUP          , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_WINDOW_POSITION                                  , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_WINDOW_START_MINIMIZED_TO_TRAY                   , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_WINDOW_MINIMIZE_TO_TRAY_ON_MINIMIZE              , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_WINDOW_MINIMIZE_TO_TRAY_ON_CLOSE                 , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_WINDOW_VERTICAL_TILE_STRATEGY                    , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_CHART_TYPE                   , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_GROUP_BY_TEAMS               , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_ALL_PLAYERS_ON_1_CHART       , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_USE_PLAYERS_COLORS           , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_DISPLAY_IN_SECONDS           , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_ZOOM                         , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_GRID                         , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_PREDEFINED_GRID              , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_GRID_FIRST_MARKER            , "180,0" ); // 180 seconds
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_GRID_REPEAT_MARKER           , "25,2" );  // 25 energy (regeneration)
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_APM_GRANULARITY              , "5" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_APM_GRAPH_APPROXIMATION      , "1" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_EAPM                    , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_MICRO_MACRO_APM         , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_XAPM                    , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_SELECT_HOTKEYS          , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_BUILDS                  , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_TRAINS                  , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_WORKERS                 , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_RESEARCHES              , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_UPGRADES                , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_ABILITY_GROUPS          , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_DURATION                , "2" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_ICON_SIZES                   , "1" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_UNITS_STAT              , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_BUILDINGS_STAT          , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_RESEARCHES_STAT         , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_UPGRADES_STAT           , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_ABILITY_GROUPS_STAT     , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_BAR_SIZE                     , "1" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_AFTER_COMPLETED         , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_UNIT_TIERS_GRANULARITY       , "8" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_STRETCH_BARS                 , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_MAP_VIEW_QUALITY             , "1" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_MAP_BACKGROUND               , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_AREA_GRANULARITY             , "6" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_HIDE_OVERLAPPED_BUILDINGS    , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_FILL_BUILDING_ICONS          , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_MAP_OBJECTS             , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_SHOW_PERCENT                 , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_DISTRIBUTION_BAR_SIZE        , "1" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_CALCULATE_UNTIL_TIME_MARKER  , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_RSR_GRANULARITY              , "7" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_RSR_GRAPH_APPROXIMATION      , "1" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_RS_CUMULATIVE                , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_RS_GRANULARITY               , "7" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_PAS_CUMULATIVE               , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_PAS_GRANULARITY              , "7" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_INCLUDE_INITIAL_UNITS        , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_RED_SHOW_PERCENT             , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_RED_DISTRIBUTION_BAR_SIZE    , "1" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_MAX_FRAME_BREAK              , "14" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_GROUP_SAME_PRODUCTIONS       , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_ICON_SIZES_P                 , "1" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_ICON_SIZES_PS                , "1" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_ACTIONS_INVERT_COLORS        , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_ACTIONS_ICON_SIZE            , "1" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_ACTIONS_USE_LISTED_AS_INPUT  , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_MAP_PREVIEW_DOWNLOAD_FROM_GATEWAY   , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_MAP_PREVIEW_ZOOM                    , "2" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_MAP_PREVIEW_LOCALE                  , "US-en" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_PUBLIC_COMMENTS_USER_NAME           , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_IN_GAME_CHAT_HIDE_MESSAGE_TARGETS   , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_IN_GAME_CHAT_SHOW_BLINKS            , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_IN_GAME_CHAT_FORMAT_INTO_PARAGRAPHS , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_IN_GAME_CHAT_TARGET_LANGUAGE        , "0" ); // The first is the default
		DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_IN_GAME_CHAT_SOURCE_LANGUAGE        , "0" ); // The first is the default
		DEFAULT_PROPERTIES.setProperty( KEY_MULTI_REP_ANAL_AUTO_OPEN_FIRST_PLAYER            , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_MULTI_REP_ANAL_STRETCH_TO_WINDOW                 , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_MULTI_REP_ANAL_CHARTS_CHART_TYPE                 , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_MULTI_REP_ANAL_CHARTS_CHART_GRANULARITY          , "1" );
		DEFAULT_PROPERTIES.setProperty( KEY_MULTI_REP_ANAL_CHARTS_GRAPH_APPROXIMATION        , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_MULTI_REP_ANAL_TRENDS_TREND_TYPE                 , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_SEARCH_SOURCE_AUTO_SORT                      , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_SEARCH_RESULTS_STRETCH_TO_WINDOW             , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_SEARCH_RESULTS_COLOR_WIN_LOSS                , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_SEARCH_VISIBLE_REPLAY_LIST_COLUMN_KEYS       , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_SEARCH_RESULTS_INCLUDE_PATH_IN_ZIP           , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_REP_SEARCH_RESULTS_RENAME_TEMPLATE               , "/T - /m - /q./e" );
		DEFAULT_PROPERTIES.setProperty( KEY_SHARE_REP_REPLAY_SITE                            , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_SHARE_REP_USER_NAME                              , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_IMPORT_BO_LATEST_IMPORT_FOLDER                   , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_PROGRESS_DIALOG_ALERT_WHEN_DONE                  , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_MOUSE_PRINT_REFRESH_RATE                         , "2" );
		DEFAULT_PROPERTIES.setProperty( KEY_MOUSE_PRINT_IMAGE_ZOOM                           , "1" );
		DEFAULT_PROPERTIES.setProperty( KEY_SHOW_NEW_TO_SC2GEARS_DIALOG_ON_STARTUP           , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_START_PAGE_CHANNEL                               , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_SC2GEARS_DATABASE_DOWNLOADER_FILE_TYPE           , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_SC2GEARS_DATABASE_DOWNLOADER_TARGET_FOLDER       , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_PLUGIN_MANAGER_ALLOW_INCOMPATIBLE_PLUGINS        , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_PLUGIN_MANAGER_ENABLED_PLUGINS                   , "hu.belicza.andras.buildorderstableplugin.BuildOrdersTablePlugin" );
		DEFAULT_PROPERTIES.setProperty( KEY_ON_TOP_GAME_INFO_LOCATION                        , "150,400" );
		DEFAULT_PROPERTIES.setProperty( KEY_ON_TOP_GAME_INFO_LOCKED                          , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_ON_TOP_APM_DISPLAY_LOCATION                      , "600,50" );
		DEFAULT_PROPERTIES.setProperty( KEY_ON_TOP_APM_DISPLAY_LOCKED                        , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_ON_TOP_APM_DISPLAY_FONT_SIZE                     , "18" );
		DEFAULT_PROPERTIES.setProperty( KEY_WORD_CLOUD_FONT                                  , "SansSerif" );
		DEFAULT_PROPERTIES.setProperty( KEY_WORD_CLOUD_MIN_FONT_SIZE                         , "11" );
		DEFAULT_PROPERTIES.setProperty( KEY_WORD_CLOUD_MAX_FONT_SIZE                         , "60" );
		DEFAULT_PROPERTIES.setProperty( KEY_WORD_CLOUD_MAX_WORDS                             , "800" );
		DEFAULT_PROPERTIES.setProperty( KEY_WORD_CLOUD_USE_COLORS                            , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_MOUSE_PRACTICE_CHECK_USER_SCORE                  , "true" );
		DEFAULT_PROPERTIES.setProperty( KEY_MOUSE_PRACTICE_ENABLE_CUSTOM_RULES               , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_MOUSE_PRACTICE_COLOR_BLIND                       , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_MOUSE_PRACTICE_USER_NAME                         , "Anonymous" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MOUSE_GAME_FPS                          , "20" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MOUSE_GAME_MAX_DISC_RADIUS              , "30" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MOUSE_GAME_MAX_DISC_AGE                 , "2500" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MOUSE_GAME_MAX_DISC_SCORE               , "100" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MOUSE_GAME_MAX_DISCS_MISSED             , "10" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MOUSE_GAME_DISC_SPEED                   , "50" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MOUSE_GAME_FRIENDLY_DISC_PROBABILITY    , "83" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MOUSE_GAME_INITIAL_DELAY_FOR_NEW_DISC   , "800" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MOUSE_GAME_NEW_DISC_DELAY_DECREMENT     , "3" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MOUSE_GAME_RANDOM_SEED                  , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MOUSE_GAME_PAINT_DISC_CENTER_CROSS      , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_MOUSE_GAME_PAINT_MAX_DISC_OUTLINE       , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_STREAM_NAME                    , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_STREAM_PASSWORD                , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_STREAM_DESCRIPTION             , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_SCREEN_CAPTURE_METHOD          , GeneralUtils.isWindows() ? "1" : "0" ); // Windows Native for Windows, standard for other OS 
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_SCREEN_AREA_TO_STREAM          , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_CUSTOM_AREA_LEFT               , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_CUSTOM_AREA_TOP                , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_CUSTOM_AREA_RIGHT              , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_CUSTOM_AREA_BOTTOM             , "0" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_OUTPUT_VIDEO_SIZE              , "6" ); // LowD 360p (640 x 360) 16:9
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_CUSTOM_RESIZE_WIDTH            , "640" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_CUSTOM_RESIZE_HEIGHT           , "400" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_REFRESH_RATE_FPS               , "10" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_SERVER_PORT                    , "80" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_IMAGE_QUALITY                  , "50" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_SAVE_VIDEO_AS_AVI              , "false" );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_FIX_AVI_FPS                    , "10" );
		
		final String programFilesFolder, baseAutoReplayFolder, baseMapsFolder, baseCustomContentFolder;
		if ( GeneralUtils.isWindows7() || GeneralUtils.isWindowsVista()
			|| ( GeneralUtils.isWindows() && !GeneralUtils.isWindowsXp() ) ) { // This is to handle Windows 8 (which is reported as Windows NT due to a bug)
			programFilesFolder         = new File( "C:/Program Files (x86)" ).exists() ? "C:/Program Files (x86)" : "C:/Program Files";
			baseAutoReplayFolder       = "/Documents";          // User home relative!
			baseMapsFolder             = "C:/ProgramData/Blizzard Entertainment";
			baseCustomContentFolder    = "C:";
		}
		else if ( GeneralUtils.isWindowsXp() ) {
			programFilesFolder         = new File( "C:/Program Files (x86)" ).exists() ? "C:/Program Files (x86)" : "C:/Program Files";
			baseAutoReplayFolder       = "/My Documents";
			baseMapsFolder             = "C:/Documents and Settings/All Users/Application Data/Blizzard Entertainment";
			baseCustomContentFolder    = "C:";
		}
		else if ( GeneralUtils.isMac() ) {
			programFilesFolder         = "/Applications";
			baseAutoReplayFolder       = "/Library/Application Support/Blizzard"; // User home relative!
			baseMapsFolder             = "/Users/Shared/Blizzard";
			baseCustomContentFolder    = "/Users/Shared";
		}
		else {
			programFilesFolder         = "/Applications";
			baseAutoReplayFolder       = "/Documents";          // User home relative!
			baseMapsFolder             = "/Documents/Blizzard Entertainment";
			baseCustomContentFolder    = "";
		}
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_FOLDER_SC2_INSTALLATION   , programFilesFolder      + "/StarCraft II"           );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_FOLDER_SC2_AUTO_REPLAY    , baseAutoReplayFolder    + "/StarCraft II/Accounts"  );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_FOLDER_SC2_MAPS           , baseMapsFolder          + "/Battle.net/Cache"       );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_FOLDER_DEFAULT_REPLAY     , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_FOLDER_REPLAY_AUTO_SAVE   , baseCustomContentFolder + "/SC2Replay Archive"      );
		DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_FOLDER_MOUSE_PRINT_OUTPUT , baseCustomContentFolder + "/Sc2gears Mouse prints"  );
		DEFAULT_PROPERTIES.setProperty( KEY_PRIVATE_STREAMING_SAVE_VIDEO_FOLDER, baseCustomContentFolder + "/Sc2gears Stream videos" );
		final String sc2AutoRepFolder = DEFAULT_PROPERTIES.getProperty( KEY_SETTINGS_FOLDER_SC2_AUTO_REPLAY );
		for ( int i = 2; i <= MAX_SC2_AUTOREP_FOLDERS; i++ ) {
			DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_FOLDER_SC2_AUTO_REPLAY + i, sc2AutoRepFolder );
			DEFAULT_PROPERTIES.setProperty( KEY_SETTINGS_FOLDER_ENABLE_EXTRA_SC2_AUTO_REPLAY + i, "false" );
		}
	}
	
	/** The actual properties. */
	private static Properties properties = new Properties( DEFAULT_PROPERTIES );
	
	/** Player alias group map. Key: player identifier. Value: alias group player identifier (main player identifier). */
	private static final Map< PlayerId, PlayerId > playerAliasGroupMap = new HashMap< PlayerId, PlayerId >();
	/** Map alias group map. Key: map name. Value: alias group name.                                                   */
	private static final Map< String, String     > mapAliasGroupMap    = new HashMap< String, String     >();
	
	/** Values of the pre-defined lists mapped from their keys. */
	private static Map< PredefinedList, Vector< String > > predefinedListMap; // Unfortunately this cannot be final because then it would have to been initialized which would trigger codes that build on Language which is not yet loaded! 
	
	/** List of custom replay sites. */
	private static volatile List< ReplayUploadSite > customReplayUploadSiteList;
	
	/** Set of enabled plugins (their main classes). */
	private static volatile Set< String > enabledPluginSet;
	
	/** Indices of the visible columns of the Replay list table. */
	private static volatile int[] visibleReplayListColumnIndices;
	
	/**
	 * No need to instantiate this class.
	 */
	private Settings() {
	}
	
	/**
	 * Checks if the settings file exists.
	 * @return true if the settings file exists; false otherwise
	 */
	public static boolean doesSettingsFileExist() {
		return SETTINGS_FILE.exists();
	}
	
	/**
	 * Loads the properties from its persistent file.<br>
	 * If loading fails, errors are silently discarded, the default settings remain.
	 */
	public static void loadProperties() {
		if ( SETTINGS_FILE.exists() )
			try {
				properties.loadFromXML( new FileInputStream( SETTINGS_FILE ) );
			} catch ( final Exception e ) {
				System.err.println( "Failed to load properties!" );
				e.printStackTrace( System.err );
			}
		else
			System.err.println( "Warning: settings file does not exist, the default settings will be used." );
		
		rebuildAliases();
		// rebuildPredefinedLists() will be called from completeDefaultPropertiesInitialization()!
	}
	
	/**
	 * Completes the initialization of the default properties.
	 * Can only be called after the Settings and the Languages have been loaded
	 * because this method initializes default properties which trigger
	 * the loading of classes which use texts from the {@link Language} (for example {@link ActionType}).
	 */
	public static void completeDefaultPropertiesInitialization () {
		for ( final ActionType actionType : ActionType.values() )
			DEFAULT_PROPERTIES.setProperty( KEY_REP_ANALYZER_CHARTS_ACTIONS_ACTION_TYPES + actionType.ordinal(), actionType == ActionType.INACTION ? "false" : "true" );
		
		for ( final PredefinedList predefinedList : PredefinedList.values() )
			DEFAULT_PROPERTIES.setProperty( predefinedList.settingsKey, predefinedList.defaultValue );
		
		predefinedListMap = new EnumMap< PredefinedList, Vector< String > >( PredefinedList.class );
		for ( final PredefinedList predefinedList : PredefinedList.values() )
			predefinedListMap.put( predefinedList, new Vector< String >( 5 ) );
		
		rebuildPredefinedLists();
	}
	
	/**
	 * Rebuilds the internal alias structures.
	 */
	public static void rebuildAliases() {
		playerAliasGroupMap.clear();
		mapAliasGroupMap   .clear();
		
		for ( int i = 0; i < 2; i++ ) { // 2 iterations: one for players and one for maps
			final String aliases = Settings.getString( i == 0 ? KEY_SETTINGS_MISC_PLAYER_ALIASES : KEY_SETTINGS_MISC_MAP_ALIASES );
			String line;
			try ( final BufferedReader aliasesReader = new BufferedReader( new StringReader( aliases ) ) ) {
				while ( ( line = aliasesReader.readLine() ) != null ) {
					final StringTokenizer tokenizer = new StringTokenizer( line, "," );
					
					if ( !tokenizer.hasMoreTokens() )
						continue; // Empty line
					
					final String aliasGroupName = tokenizer.nextToken().trim();
					if ( aliasGroupName.isEmpty() )
						continue;
					final PlayerId aliasGroupPlayerId = i == 0 ? PlayerId.parse( aliasGroupName ) : null;
					if ( i == 0 && aliasGroupPlayerId == null )
						continue;
					
					while ( tokenizer.hasMoreTokens() ) {
						final String alias = tokenizer.nextToken().trim();
						if ( !alias.isEmpty() ) {
							if ( i == 0 ) {
								final PlayerId aliasPlayerId = PlayerId.parse( alias );
								if ( aliasPlayerId != null )
									playerAliasGroupMap.put( aliasPlayerId, aliasGroupPlayerId );
							}
							else
								mapAliasGroupMap.put( alias, aliasGroupName );
						}
					}
				}
			} catch ( final IOException ie ) {
				// Never to happen because we read from a string...
				ie.printStackTrace();
			}
		}
	}
	
	/**
	 * Rebuilds the internal pre-defined lists structures.
	 */
	public static void rebuildPredefinedLists() {
		for ( final PredefinedList predefinedList : PredefinedList.values() ) {
			final Vector< String > predefinedListVector = predefinedListMap.get( predefinedList );
			predefinedListVector.clear();
			breakIntoLines( Settings.getString( predefinedList.settingsKey ), predefinedListVector );
		}
	}
	
	/**
	 * Breaks a text into lines and stores the lines in the specified collection.
	 * @param text           text to be broken into lines
	 * @param lineCollection collection to put the lines into
	 */
	public static void breakIntoLines( final String text, final Collection< String > lineCollection ) {
		String line;
		try ( final BufferedReader textReader = new BufferedReader( new StringReader( text ) ) ) {
			while ( ( line = textReader.readLine() ) != null )
				lineCollection.add( line );
		} catch ( final IOException ie ) {
			// Never to happen because we read from a string...
			ie.printStackTrace();
		}
	}
	
	/**
	 * Saves the properties to its persistent file.
	 */
	public static void saveProperties() {
		try ( final FileOutputStream output = new FileOutputStream( SETTINGS_FILE ) ) {
			// Set meta data
			set( KEY_META_SAVED_WITH_VERSION, Consts.APPLICATION_VERSION );
			set( KEY_META_SAVE_TIME         , System.currentTimeMillis() );
			
			properties.storeToXML( output, "This settings file is managed by " + SharedConsts.APPLICATION_NAME + " automatically. Do not edit it unless you know what you're doing!" );
			
		} catch ( final Exception e ) {
			System.err.println( "Failed to save properties!" );
			e.printStackTrace( System.err );
		}
	}
	
	/**
	 * Sets the value of a property.<br>
	 * The string value returned by <code>value.toString()</code> will be set.
	 * @param key   key of the property
	 * @param value value of the property
	 */
	public static void set( final String key, final Object value ) {
		properties.setProperty( key, value.toString() );
	}
	
	/**
	 * Returns the String value of a property.
	 * @param key key of the property
	 * @return the String value of the property
	 */
	public static String getString( final String key ) {
		return properties.getProperty( key );
	}
	
	/**
	 * Returns the int value of a property.
	 * @param key key of the property
	 * @return the int value of the property
	 */
	public static int getInt( final String key ) {
		return Integer.parseInt( properties.getProperty( key ) );
	}
	
	/**
	 * Returns the boolean value of a property.
	 * @param key key of the property
	 * @return the boolean value of the property
	 */
	public static boolean getBoolean( final String key ) {
		return Boolean.parseBoolean( properties.getProperty( key ) );
	}
	
	/**
	 * Removes the value of a property.
	 * @param key key of the property to be removed
	 */
	public static void remove( final String key ) {
		properties.remove( key );
	}
	
	/**
	 * Returns the default string value of a property.
	 * @param key key of the property
	 * @return the default string value of the property
	 */
	public static String getDefaultString( final String key ) {
		return DEFAULT_PROPERTIES.getProperty( key );
	}
	
	/**
	 * Returns the default int value of a property.
	 * @param key key of the property
	 * @return the default int value of the property
	 */
	public static int getDefaultInt( final String key ) {
		return Integer.parseInt( DEFAULT_PROPERTIES.getProperty( key ) );
	}
	
	/**
	 * Returns the default boolean value of a property.
	 * @param key key of the property
	 * @return the default boolean value of the property
	 */
	public static boolean getDefaultBoolean( final String key ) {
		return Boolean.parseBoolean( DEFAULT_PROPERTIES.getProperty( key ) );
	}
	
	/**
	 * Returns the alias group player id for the specified player.<br>
	 * If the player is not part of any alias groups, the same reference is returned.
	 * @param playerId player identifier whose alias group player id to be returned
	 * @return the alias group player id for the specified player or <code>playerId</code> if the player is not part of any alias groups
	 */
	public static PlayerId getAliasGroupPlayerId( final PlayerId playerId ) {
		final PlayerId aliasGroupPlayerId = playerAliasGroupMap.get( playerId );
		return aliasGroupPlayerId == null ? playerId : aliasGroupPlayerId;
	}
	
	/**
	 * Returns the alias group name for the specified map.<br>
	 * If the map name is not part of any alias groups, the same reference is returned.
	 * @param mapName name of map whose alias group name to be returned
	 * @return the alias group name for the specified map or <code>mapName</code> if the map is not part of any alias groups
	 */
	public static String getMapAliasGroupName( final String mapName ) {
		final String aliasGroupName = mapAliasGroupMap.get( mapName );
		return aliasGroupName == null ? mapName : aliasGroupName;
	}
	
	/**
	 * Returns the values of a pre-defined list.
	 * @param predefinedList predefined list whose values to be returned
	 * @return the values of a pre-defined list
	 */
	public static Vector< String > getPredefinedListValues( final PredefinedList predefinedList ) {
		return predefinedListMap.get( predefinedList );
	}
	
	/**
	 * Returns the list of custom replay upload sites.
	 * @return the list of custom replay upload sites
	 */
	public static List< ReplayUploadSite > getCustomReplayUploadSiteList() {
		if ( customReplayUploadSiteList == null ) {
			final List< ReplayUploadSite > customReplayUploadSiteList = new ArrayList< ReplayUploadSite >( 4 );
			
			final String customReplaySitesString = getString( KEY_SETTINGS_MISC_CUSTOM_REPLAY_SITES );
			if ( customReplaySitesString.length() > 0 )
				try {
					final Document document     = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new InputSource( new StringReader( customReplaySitesString ) ) );
					final Element  rootElement  = document.getDocumentElement();
					final NodeList siteNodeList = rootElement.getElementsByTagName( "site" );
					
					final int sitesCount = siteNodeList.getLength();
					for ( int i = 0; i < sitesCount; i++ ) {
						Element siteElement = (Element) siteNodeList.item( i );
						
						final ReplayUploadSite replayUploadSite = new ReplayUploadSite();
						replayUploadSite.displayName = siteElement.getElementsByTagName( "displayName" ).item( 0 ).getTextContent();
						replayUploadSite.homePage    = siteElement.getElementsByTagName( "homePage"    ).item( 0 ).getTextContent();
						replayUploadSite.uploadUrl   = siteElement.getElementsByTagName( "uploadUrl"   ).item( 0 ).getTextContent();
						
						customReplayUploadSiteList.add( replayUploadSite );
					}
				}
				catch ( final Exception e ) {
					e.printStackTrace();
				}
			
			Settings.customReplayUploadSiteList = customReplayUploadSiteList;
		}
		
		return customReplayUploadSiteList;
	}
	
	/**
	 * Sets the list of custom replay upload sites.
	 * @param customReplayUploadSiteList the list of custom replay upload sites
	 */
	public static void setCustomReplayUploadSiteList( final List< ReplayUploadSite > customReplayUploadSiteList ) {
		try {
			final Document document    = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			final Element  rootElement = document.createElement( "customReplaySites" );
			rootElement.setAttribute( "version", "1.0" ); // To keep the possibility for future changes
			
			for ( final ReplayUploadSite replayUploadSite : customReplayUploadSiteList ) {
				final Element siteElement = document.createElement( "site" );
				
				Element element = document.createElement( "displayName" );
				element.setTextContent( replayUploadSite.displayName );
				siteElement.appendChild( element );
				
				element = document.createElement( "homePage" );
				element.setTextContent( replayUploadSite.homePage );
				siteElement.appendChild( element );
				
				element = document.createElement( "uploadUrl" );
				element.setTextContent( replayUploadSite.uploadUrl );
				siteElement.appendChild( element );
				
				rootElement.appendChild( siteElement );
			}
			
			document.appendChild( rootElement );
			
			final StringWriter output = new StringWriter();
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform( new DOMSource( document ), new StreamResult( output ) );
			
			set( KEY_SETTINGS_MISC_CUSTOM_REPLAY_SITES, output.getBuffer().toString() );
			Settings.customReplayUploadSiteList = null;
			
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the set of enabled plugins (their main classes).
	 * @return the set of enabled plugins (their main classes)
	 */
	public static Set< String > getEnabledPluginSet() {
		if ( enabledPluginSet == null ) {
			final Set< String > enabledPluginSet = new HashSet< String >();
			
			for ( final String enabledPlugin : getString( KEY_PLUGIN_MANAGER_ENABLED_PLUGINS ).split( "," ) )
				enabledPluginSet.add( enabledPlugin );
			
			Settings.enabledPluginSet = enabledPluginSet;
		}
		
		return enabledPluginSet;
	}
	
	/**
	 * Sets the list of enabled plugins (their main classes).
	 * @param enabledPluginSet the set of enabled plugins (their main classes) to be set
	 */
	public static void setEnabledPluginSet( final Set< String > enabledPluginSet ) {
		final StringBuilder enabledPluginsBuilder = new StringBuilder();
		
		for ( final String enabledPlugin : enabledPluginSet ) {
			if ( enabledPluginsBuilder.length() > 0 )
				enabledPluginsBuilder.append( ',' );
			enabledPluginsBuilder.append( enabledPlugin );
		}
		
		set( KEY_PLUGIN_MANAGER_ENABLED_PLUGINS, enabledPluginsBuilder.toString() );
		Settings.enabledPluginSet = null;
	}
	
	/**
	 * Returns the array of the visible columns in the Replay list table.
	 * @return the array of the visible columns in the Replay list table
	 */
	public static int[] getVisibleReplayListColumnIndices() {
		if ( visibleReplayListColumnIndices == null ) {
			final String columnKeysString = getString( KEY_REP_SEARCH_VISIBLE_REPLAY_LIST_COLUMN_KEYS );
			
			boolean settingIsGood = false;
			if ( columnKeysString.length() > 0 ) {
				
				// Parse replay list column indices setting
				final String[] columnKeys = columnKeysString.split( "," );
				
				// The same column cannot be listed twice:
				final Set< String > columnKeySet = new HashSet< String >( columnKeys.length );
				for ( final String columnKey : columnKeys )
					columnKeySet.add( columnKey );
				
				if ( columnKeys.length > 0 && columnKeySet.size() == columnKeys.length ) { // 0 visible column: not good
					settingIsGood = true;
					final int[] indices = new int[ columnKeys.length ];
					for ( int i = 0; i < indices.length; i++ ) {
						final String columnKey = columnKeys[ i ];
						int index = -1;
						for ( int j = 0; j < ReplaySearch.RESULT_HEADER_KEYS.length; j++ )
							if ( columnKey.equals( ReplaySearch.RESULT_HEADER_KEYS[ j ] ) ) {
								index = j;
								break;
							}
						
						if ( index < 0 ) {
							settingIsGood = false;
							break;
						}
						
						indices[ i ] = index;
					}
					
					if ( settingIsGood )
						visibleReplayListColumnIndices = indices;
				}
			}
			
			if ( !settingIsGood ) {
				// Column setup has not yet been changed or invalid setting, return default order
				final int[] indices = new int[ ReplaySearch.RESULT_HEADER_KEYS.length ];
				
				for ( int i = 0; i < indices.length; i++ )
					indices[ i ] = i;
				
				visibleReplayListColumnIndices = indices;
			}
		}
		
		return visibleReplayListColumnIndices;
	}
	
	/**
	 * Sets the array of the visible columns in the Replay list table.
	 * @param visibleReplayListColumnIndices visible columns of the replay list table to be set
	 */
	public static void setVisibleReplayListColumnIndices( final int[] visibleReplayListColumnIndices ) {
		final StringBuilder columnKeysStringBuilder = new StringBuilder();
		
		for ( int i = 0; i < visibleReplayListColumnIndices.length; i++ ) {
			if ( columnKeysStringBuilder.length() > 0 )
				columnKeysStringBuilder.append( ',' );
			
			columnKeysStringBuilder.append( ReplaySearch.RESULT_HEADER_KEYS[ visibleReplayListColumnIndices[ i ] ] );
		}
		
		set( KEY_REP_SEARCH_VISIBLE_REPLAY_LIST_COLUMN_KEYS, columnKeysStringBuilder.toString() );
		Settings.visibleReplayListColumnIndices = visibleReplayListColumnIndices;
	}
	
}
