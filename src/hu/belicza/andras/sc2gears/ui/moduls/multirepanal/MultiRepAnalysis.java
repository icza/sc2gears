/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.multirepanal;

import static hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.FRAME_BITS_IN_SECOND;
import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.EnumCache;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.model.Details;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.PlayerId;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.BuildAction;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.charts.ChartUtils.GraphApproximation;
import hu.belicza.andras.sc2gears.ui.components.FirstShownListener;
import hu.belicza.andras.sc2gears.ui.components.PlayerPopupMenu;
import hu.belicza.andras.sc2gears.ui.components.TableBox;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog.SettingsTab;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.ModuleFrame;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplaySearch;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gears.util.NormalThread;
import hu.belicza.andras.sc2gears.util.NullAwareComparable;
import hu.belicza.andras.sc2gears.util.ReplayCache;
import hu.belicza.andras.sc2gearspluginapi.api.enums.League;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Format;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;
import hu.belicza.andras.sc2gearspluginapi.impl.util.WordCloudTableInput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Multi-replay analysis.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class MultiRepAnalysis extends ModuleFrame {
	
	/** Text keys of the days of the week. */
	private static final String[] DAY_TEXT_KEYS = {
		"general.day.sunday",
		"general.day.monday",
		"general.day.tuesday",
		"general.day.wednesday",
		"general.day.thursday",
		"general.day.friday",
		"general.day.saturday"
	};
	
	/** Text keys of the months. */
	private static final String[] MONTH_TEXT_KEYS = {
		"general.month.january",
		"general.month.february",
		"general.month.march",
		"general.month.april",
		"general.month.may",
		"general.month.june",
		"general.month.july",
		"general.month.august",
		"general.month.september",
		"general.month.october",
		"general.month.november",
		"general.month.december"
	};
	
	/**
	 * Chart type.
	 * @author Andras Belicza
	 */
	public static enum ChartType {
		ACTIVITY               ( "module.multiRepAnal.tab.player.tab.charts.chartType.activity"             , "module.multiRepAnal.tab.player.tab.charts.chartType.activity.yAxisLabel"              ),
		APM_DEVELOPMENT        ( "module.multiRepAnal.tab.player.tab.charts.chartType.apmDevelopment"       , "module.multiRepAnal.tab.player.tab.charts.chartType.apmDevelopment.yAxisLabel"        ),
		WIN_RATIO_DEVELOPMENT  ( "module.multiRepAnal.tab.player.tab.charts.chartType.winRatioDevelopment"  , "module.multiRepAnal.tab.player.tab.charts.chartType.winRatioDevelopment.yAxisLabel"   ),
		SPAWN_LARVA_DEVELOPMENT( "module.multiRepAnal.tab.player.tab.charts.chartType.spawnLarvaDevelopment", "module.multiRepAnal.tab.player.tab.charts.chartType.spawnLarvaDevelopment.yAxisLabel" ),
		RACE_DISTRIBUTION      ( "module.multiRepAnal.tab.player.tab.charts.chartType.raceDistribution"     , "module.multiRepAnal.tab.player.tab.charts.chartType.raceDistribution.yAxisLabel#2"    ),
		GAME_TYPE_DISTRIBUTION ( "module.multiRepAnal.tab.player.tab.charts.chartType.gameTypeDistribution" , "module.multiRepAnal.tab.player.tab.charts.chartType.gameTypeDistribution.yAxisLabel"  ),
		FORMAT_DISTRIBUTION    ( "module.multiRepAnal.tab.player.tab.charts.chartType.formatDistribution"   , "module.multiRepAnal.tab.player.tab.charts.chartType.formatDistribution.yAxisLabel"    );
		
		/** Cache of the string value.                     */
		public final String    stringValue;
		/** Key of Y axis label of this chart type.        */
		public final String    yAxisLabelKey;
		/** Key stroke for fast accessing this chart type. */
		public final KeyStroke keyStroke;
		
		/**
		 * Creates a new ChartType.
		 * @param textKey       key of the text representation
		 * @param yAxisLabelKey key of Y axis label of this chart type
		 */
		private ChartType( final String textKey, final String yAxisLabelKey ) {
			this.yAxisLabelKey    = yAxisLabelKey;
			final int chartNumber = ordinal() + 1;
			keyStroke             = chartNumber > 10 ? null : KeyStroke.getKeyStroke( KeyEvent.VK_0 + chartNumber, InputEvent.CTRL_MASK ); // CTRL+1 for chart type 1, CTRL+2 select chart type 2 etc.
			if ( keyStroke == null )
				stringValue = Language.getText( textKey );
			else
				stringValue = "<html>" + Language.getText( textKey ) + "&nbsp;&nbsp;<font color=#777777>Ctrl+" + ( chartNumber == 10 ? 0 : chartNumber ) + "</font></html>";
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Chart type.
	 * @author Andras Belicza
	 */
	public static enum ChartGranularity {
		DAY          ( "module.multiRepAnal.tab.player.tab.charts.granularity.day"          ),
		WEEK         ( "module.multiRepAnal.tab.player.tab.charts.granularity.week"         ),
		MONTH        ( "module.multiRepAnal.tab.player.tab.charts.granularity.month"        ),
		YEAR         ( "module.multiRepAnal.tab.player.tab.charts.granularity.year"         ),
		LADDER_SEASON( "module.multiRepAnal.tab.player.tab.charts.granularity.ladderSeason" );
		
		/** Cache of the string value. */
		private final String stringValue;
		
		/**
		 * Creates a new ChartType.
		 * @param textKey key of the text representation
		 */
		private ChartGranularity( final String textKey ) {
			stringValue = Language.getText( textKey );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Trend type.
	 * @author Andras Belicza
	 */
	public static enum TrendType {
		HOURLY ( "module.multiRepAnal.tab.player.tab.trends.hourly"  ),
		DAILY  ( "module.multiRepAnal.tab.player.tab.trends.daily"   ),
		MONTHLY( "module.multiRepAnal.tab.player.tab.trends.monthly" );
		
		/** Cache of the string value.                     */
		public final String    stringValue;
		/** Key stroke for fast accessing this chart type. */
		public final KeyStroke keyStroke;
		/** Labels of the different values of the trend.   */
		public final String[]  labels;
		
		/**
		 * Creates a new ChartType.
		 * @param textKey       key of the text representation
		 * @param yAxisLabelKey key of Y axis label of this chart type
		 */
		private TrendType( final String textKey ) {
			stringValue = Language.getText( textKey );
			keyStroke   = KeyStroke.getKeyStroke( KeyEvent.VK_1 + ordinal(), InputEvent.CTRL_MASK ); // CTRL+1 for chart type 1, CTRL+2 select chart type 2 etc.
			
			if ( "module.multiRepAnal.tab.player.tab.trends.hourly".equals( textKey ) ) {
				labels = new String[ 24 ];
				for ( int i = 0; i < 24; i++ )
					labels[ i ] = Integer.toString( i );
			} else if ( "module.multiRepAnal.tab.player.tab.trends.daily".equals( textKey ) ) {
				labels = new String[ DAY_TEXT_KEYS.length ];
				for ( int i = 0; i < DAY_TEXT_KEYS.length; i++ )
					labels[ i ] = Language.getText( DAY_TEXT_KEYS[ i ] );
			} else if ( "module.multiRepAnal.tab.player.tab.trends.monthly".equals( textKey ) ) {
				labels = new String[ MONTH_TEXT_KEYS.length ];
				for ( int i = 0; i < MONTH_TEXT_KEYS.length; i++ )
					labels[ i ] = Language.getText( MONTH_TEXT_KEYS[ i ] );
			} else
				throw new RuntimeException( "Set the labels for the trend: " + stringValue );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/** Header keys of the players table. */
	private static final String[] PLAYERS_HEADER_KEYS = new String[] {
		"module.multiRepAnal.tab.players.header.playerName",
		"module.multiRepAnal.tab.players.header.replays",
		"module.multiRepAnal.tab.players.header.avgApm",
		"module.multiRepAnal.tab.players.header.avgEapm",
		"module.multiRepAnal.tab.players.header.avgApmRedundancy",
		"module.multiRepAnal.tab.players.header.record",
		"module.multiRepAnal.tab.players.header.winRatio",
		"module.multiRepAnal.tab.players.header.raceDistribution",
		"module.multiRepAnal.tab.players.header.totalTimeInGames",
		"module.multiRepAnal.tab.players.header.avgGameLength",
		"module.multiRepAnal.tab.players.header.presence",
		"module.multiRepAnal.tab.players.header.avgGamesPerDay",
		"module.multiRepAnal.tab.players.header.firstGame",
		"module.multiRepAnal.tab.players.header.lastGame"
	};
	/** Header keys of the player maps table. */
	private static final String[] PLAYER_MAPS_HEADER_KEYS = new String[] {
		"module.multiRepAnal.tab.maps.header.mapName",
		"module.multiRepAnal.tab.maps.header.replays",
		"module.multiRepAnal.tab.maps.header.replaysRatio",
		"module.multiRepAnal.tab.maps.header.record",
		"module.multiRepAnal.tab.maps.header.winRatio",
		"module.multiRepAnal.tab.maps.header.1v1PWinRatio",
		"module.multiRepAnal.tab.maps.header.1v1TWinRatio",
		"module.multiRepAnal.tab.maps.header.1v1ZWinRatio",
		"module.multiRepAnal.tab.maps.header.avgGameLength",
		"module.multiRepAnal.tab.maps.header.firstPlayed",
		"module.multiRepAnal.tab.maps.header.lastPlayed"
	};
	/** Header keys of the 1v1 build orders table. */
	private static final String[] BUILD_ORDERS_1V1_HEADER_KEYS = new String[] {
		"module.multiRepAnal.tab.1v1BuildOrders.header.race",
		"module.multiRepAnal.tab.1v1BuildOrders.header.buildOrder",
		"module.multiRepAnal.tab.1v1BuildOrders.header.occurrences",
		"module.multiRepAnal.tab.1v1BuildOrders.header.record",
		"module.multiRepAnal.tab.1v1BuildOrders.header.winRatio",
		"module.multiRepAnal.tab.1v1BuildOrders.header.recordVsP",
		"module.multiRepAnal.tab.1v1BuildOrders.header.recordVsT",
		"module.multiRepAnal.tab.1v1BuildOrders.header.recordVsZ",
		"module.multiRepAnal.tab.1v1BuildOrders.header.firstUsed",
		"module.multiRepAnal.tab.1v1BuildOrders.header.lastUsed"
	};
	/** Header keys of the Chat words table. */
	private static final String[] CHAT_WORDS_HEADER_KEYS = new String[] {
		"module.multiRepAnal.tab.chatWords.header.word",
		"module.multiRepAnal.tab.1v1BuildOrders.header.occurrences",
		"module.multiRepAnal.tab.maps.header.replays",
		"module.multiRepAnal.tab.maps.header.replaysRatio",
		"module.multiRepAnal.tab.1v1BuildOrders.header.firstUsed",
		"module.multiRepAnal.tab.1v1BuildOrders.header.lastUsed"
	};
	/** Header keys of the playmates table. */
	private static final String[] PLAYMATES_HEADER_KEYS = new String[] {
		"module.multiRepAnal.tab.player.tab.playmates.header.playmate",
		"module.multiRepAnal.tab.player.tab.playmates.header.commonGames",
		"module.multiRepAnal.tab.player.tab.playmates.header.gamesAsAllies",
		"module.multiRepAnal.tab.player.tab.playmates.header.gamesAsOpponents",
		"module.multiRepAnal.tab.player.tab.playmates.header.recordAsAllies",
		"module.multiRepAnal.tab.player.tab.playmates.header.winRatioAsAllies",
		"module.multiRepAnal.tab.player.tab.playmates.header.recordAsOpponents",
		"module.multiRepAnal.tab.player.tab.playmates.header.winRatioAsOpponents",
		"module.multiRepAnal.tab.player.tab.playmates.header.totalTimeTogether",
		"module.multiRepAnal.tab.player.tab.playmates.header.firstCommonGame",
		"module.multiRepAnal.tab.player.tab.playmates.header.lastCommonGame"
	};
	/** Header names of the players table.                  */
	private static final Vector< String > PLAYERS_HEADER_NAME_VECTOR              = new Vector< String >( PLAYERS_HEADER_KEYS         .length );
	/** Header names of the maps table.                     */
	private static final Vector< String > MAPS_HEADER_NAME_VECTOR                 = new Vector< String >( PLAYER_MAPS_HEADER_KEYS     .length-2 ); // 2 columns are not in the general maps table
	/** Header names of the 1v1 build orders table.         */
	private static final Vector< String > BUILD_ORDERS_1V1_HEADER_NAME_VECTOR     = new Vector< String >( BUILD_ORDERS_1V1_HEADER_KEYS.length );
	/** Header names of the non 1v1 build orders table.     */
	private static final Vector< String > BUILD_ORDERS_NON_1V1_HEADER_NAME_VECTOR = new Vector< String >( BUILD_ORDERS_1V1_HEADER_KEYS.length-3 ); // 3 columns are not in the non 1v1 build orders table
	/** Header names of the Chat words table.               */
	private static final Vector< String > CHAT_WORDS_HEADER_NAME_VECTOR           = new Vector< String >( CHAT_WORDS_HEADER_KEYS      .length );
	/** Header names of the type records table.             */
	private static final Vector< String > TYPE_RECORDS_HEADER_NAME_VECTOR;
	/** Header names of the format records table.           */
	private static final Vector< String > FORMAT_RECORDS_HEADER_NAME_VECTOR;
	/** Header names of the league match-up records table.  */
	private static final Vector< String > LEAGUE_MATCHUP_RECORDS_HEADER_NAME_VECTOR;
	/** Header names of the match-up records table.         */
	private static final Vector< String > MATCHUP_RECORDS_HEADER_NAME_VECTOR;
	/** Header names of the match-up by maps records table. */
	private static final Vector< String > MATCHUP_BY_MAPS_RECORDS_HEADER_NAME_VECTOR;
	/** Header names of the game length records table.      */
	private static final Vector< String > GAME_LENGTH_RECORDS_HEADER_NAME_VECTOR;
	/** Header names of the day records table.              */
	private static final Vector< String > DAY_RECORDS_HEADER_NAME_VECTOR;
	/** Header names of the hour records table.             */
	private static final Vector< String > HOUR_RECORDS_HEADER_NAME_VECTOR;
	/** Header names of the player maps table.              */
	private static final Vector< String > PLAYER_MAPS_HEADER_NAME_VECTOR          = new Vector< String >( PLAYER_MAPS_HEADER_KEYS     .length );
	/** Header names of the playmates table.                */
	private static final Vector< String > PLAYMATES_HEADER_NAME_VECTOR            = new Vector< String >( PLAYMATES_HEADER_KEYS       .length );
	/** Header names of the gaming sessions table.          */
	private static final Vector< String > GAMING_SESSIONS_HEADER_NAME_VECTOR;
	static {
		for ( int i = 0; i < PLAYERS_HEADER_KEYS.length; i++ )
			PLAYERS_HEADER_NAME_VECTOR.add( Language.getText( PLAYERS_HEADER_KEYS[ i ] ) );
		for ( int i = 0; i < BUILD_ORDERS_1V1_HEADER_KEYS.length; i++ )
			BUILD_ORDERS_1V1_HEADER_NAME_VECTOR.add( Language.getText( BUILD_ORDERS_1V1_HEADER_KEYS[ i ] ) );
		for ( int i = 0; i < BUILD_ORDERS_1V1_HEADER_NAME_VECTOR.size(); i++ )
			if ( !"module.multiRepAnal.tab.1v1BuildOrders.header.recordVsP".equals( BUILD_ORDERS_1V1_HEADER_KEYS[ i ] ) && !"module.multiRepAnal.tab.1v1BuildOrders.header.recordVsT".equals( BUILD_ORDERS_1V1_HEADER_KEYS[ i ] ) && !"module.multiRepAnal.tab.1v1BuildOrders.header.recordVsZ".equals( BUILD_ORDERS_1V1_HEADER_KEYS[ i ] ) )
			BUILD_ORDERS_NON_1V1_HEADER_NAME_VECTOR.add( BUILD_ORDERS_1V1_HEADER_NAME_VECTOR.get( i ) );
		
		for ( int i = 0; i < CHAT_WORDS_HEADER_KEYS.length; i++ )
			CHAT_WORDS_HEADER_NAME_VECTOR.add( Language.getText( CHAT_WORDS_HEADER_KEYS[ i ] ) );
		
		@SuppressWarnings("unchecked")
		final Vector< String > TYPE_RECORDS_HEADER_NAME_VECTOR_ = (Vector< String >) PLAYERS_HEADER_NAME_VECTOR.clone(); // To suppress "unchecked" warning
		TYPE_RECORDS_HEADER_NAME_VECTOR = TYPE_RECORDS_HEADER_NAME_VECTOR_;
		TYPE_RECORDS_HEADER_NAME_VECTOR.set( 0, Language.getText( "module.multiRepAnal.tab.player.tab.typeRecords.header.type" ) );
		
		@SuppressWarnings("unchecked")
		final Vector< String > FORMAT_RECORDS_HEADER_NAME_VECTOR_ = (Vector< String >) PLAYERS_HEADER_NAME_VECTOR.clone();
		FORMAT_RECORDS_HEADER_NAME_VECTOR = FORMAT_RECORDS_HEADER_NAME_VECTOR_;
		FORMAT_RECORDS_HEADER_NAME_VECTOR.set( 0, Language.getText( "module.multiRepAnal.tab.player.tab.formatRecords.header.format" ) );
		
		@SuppressWarnings("unchecked")
		final Vector< String > GAME_LENGTH_RECORDS_HEADER_NAME_VECTOR_ = (Vector< String >) PLAYERS_HEADER_NAME_VECTOR.clone();
		GAME_LENGTH_RECORDS_HEADER_NAME_VECTOR = GAME_LENGTH_RECORDS_HEADER_NAME_VECTOR_;
		GAME_LENGTH_RECORDS_HEADER_NAME_VECTOR.set( 0, Language.getText( "module.multiRepAnal.tab.player.tab.gameLengthRecords.header.gameLength" ) );
		
		@SuppressWarnings("unchecked")
		final Vector< String > DAY_RECORDS_HEADER_NAME_VECTOR_ = (Vector< String >) PLAYERS_HEADER_NAME_VECTOR.clone();
		DAY_RECORDS_HEADER_NAME_VECTOR = DAY_RECORDS_HEADER_NAME_VECTOR_;
		DAY_RECORDS_HEADER_NAME_VECTOR.set( 0, Language.getText( "module.multiRepAnal.tab.player.tab.dayRecords.header.day" ) );
		
		@SuppressWarnings("unchecked")
		final Vector< String > HOUR_RECORDS_HEADER_NAME_VECTOR_ = (Vector< String >) PLAYERS_HEADER_NAME_VECTOR.clone();
		HOUR_RECORDS_HEADER_NAME_VECTOR = HOUR_RECORDS_HEADER_NAME_VECTOR_;
		HOUR_RECORDS_HEADER_NAME_VECTOR.set( 0, Language.getText( "module.multiRepAnal.tab.player.tab.hourRecords.header.hour" ) );
		
		LEAGUE_MATCHUP_RECORDS_HEADER_NAME_VECTOR = new Vector<>( PLAYERS_HEADER_KEYS.length );
		LEAGUE_MATCHUP_RECORDS_HEADER_NAME_VECTOR.add( Language.getText( "module.multiRepAnal.tab.player.tab.leagueMatchupRecords.header.leagues" ) );
		for ( int i = 1; i < PLAYERS_HEADER_NAME_VECTOR.size(); i++ )
			LEAGUE_MATCHUP_RECORDS_HEADER_NAME_VECTOR.add( Language.getText( PLAYERS_HEADER_KEYS[ i ] ) );
		
		MATCHUP_RECORDS_HEADER_NAME_VECTOR = new Vector<>( PLAYERS_HEADER_KEYS.length - 1 ); // The Races % column is not shown because each line is for 1 race (would always be "X:100%"
		MATCHUP_RECORDS_HEADER_NAME_VECTOR.add( Language.getText( "module.multiRepAnal.tab.player.tab.matchupRecords.header.matchup" ) );
		for ( int i = 1; i < PLAYERS_HEADER_NAME_VECTOR.size(); i++ )
			if ( !"module.multiRepAnal.tab.players.header.raceDistribution".equals( PLAYERS_HEADER_KEYS[ i ] ) )
				MATCHUP_RECORDS_HEADER_NAME_VECTOR.add( Language.getText( PLAYERS_HEADER_KEYS[ i ] ) );
		for ( int i = 0; i < PLAYER_MAPS_HEADER_KEYS.length; i++ )
			PLAYER_MAPS_HEADER_NAME_VECTOR.add( Language.getText( PLAYER_MAPS_HEADER_KEYS[ i ] ) );
		@SuppressWarnings("unchecked")
		final Vector< String > MATCHUP_BY_MAPS_RECORDS_HEADER_NAME_VECTOR_ = (Vector< String >) MATCHUP_RECORDS_HEADER_NAME_VECTOR.clone();
		MATCHUP_BY_MAPS_RECORDS_HEADER_NAME_VECTOR = MATCHUP_BY_MAPS_RECORDS_HEADER_NAME_VECTOR_;
		MATCHUP_BY_MAPS_RECORDS_HEADER_NAME_VECTOR.add( 0, Language.getText( "module.multiRepAnal.tab.maps.header.mapName" ) );
		// 2 columns are not in the general maps table
		for ( int i = 0; i < PLAYER_MAPS_HEADER_NAME_VECTOR.size(); i++ )
			if ( !"module.multiRepAnal.tab.maps.header.record".equals( PLAYER_MAPS_HEADER_KEYS[ i ] ) && !"module.multiRepAnal.tab.maps.header.winRatio".equals( PLAYER_MAPS_HEADER_KEYS[ i ] ) )
				MAPS_HEADER_NAME_VECTOR.add( PLAYER_MAPS_HEADER_NAME_VECTOR.get( i ) );
		for ( int i = 0; i < PLAYMATES_HEADER_KEYS.length; i++ )
			PLAYMATES_HEADER_NAME_VECTOR.add( Language.getText( PLAYMATES_HEADER_KEYS[ i ] ) );
		
		GAMING_SESSIONS_HEADER_NAME_VECTOR = new Vector<>( PLAYERS_HEADER_NAME_VECTOR.size() + 1 );
		GAMING_SESSIONS_HEADER_NAME_VECTOR.add( 0, Language.getText( "module.multiRepAnal.tab.player.tab.gamingSessions.header.gameInSessions" ) );
		GAMING_SESSIONS_HEADER_NAME_VECTOR.addAll( PLAYERS_HEADER_NAME_VECTOR );
		GAMING_SESSIONS_HEADER_NAME_VECTOR.set( 1, Language.getText( "module.multiRepAnal.tab.player.tab.gamingSessions.header.sessionsEndingHere" ) );
	}
	
	/** Simple counter. */
	private static final AtomicInteger counter = new AtomicInteger();
	
	/** Files to be analyzed. */
	private final File[] files;
	
	/** Check box to tell if the first player should be auto-opened. */
	private final JCheckBox autoOpenFirstPlayerCheckBox = GuiUtils.createCheckBox( "module.multiRepAnal.autoOpenFirstPlayer", Settings.KEY_MULTI_REP_ANAL_AUTO_OPEN_FIRST_PLAYER );
	/** Check box to tell if tables have to be stretched to window.  */
	private final JCheckBox stretchToWindowCheckBox     = GuiUtils.createCheckBox( "module.multiRepAnal.stretchToWindow", Settings.KEY_MULTI_REP_ANAL_STRETCH_TO_WINDOW );
	
	/** Reference to the tabbed pane of this internal frame. */
	private final JTabbedPane tabbedPane = new JTabbedPane();
	
	/** Number of replays that are included in the analysis. */
	private int replaysIncludedInAnalysis;
	
	/** The setting that tells whether to use real time.     */
	private final boolean useRealTime    = Settings.getBoolean( Settings.KEY_SETTINGS_MISC_USE_REAL_TIME_MEASUREMENT );
	
	/** Map of player statistics. Maps from player name (full name) to the statistics.         */
	private final Map< String, PlayerStatistics     > playerStatisticsMap        = new HashMap< String, PlayerStatistics     >();
	/** Map of map statistics. Maps from map name to the statistics.                           */
	private final Map< String, MapStatistics        > mapStatisticsMap           = new HashMap< String, MapStatistics        >();
	/** Build order statistics for all formats. Value maps from build order to the statistics. */
	private final Map< Format, Map< String, BuildOrderStatistics > > formatBuildOrderStatisticsMap = new EnumMap< Format, Map< String, BuildOrderStatistics > >( Format.class );
	/** Map of words statistics. Maps from word to the statistics.                             */
	private final Map< String, WordStatistics       > chatWordsStatisticsMap     = new HashMap< String, WordStatistics       >();
	
	/** Name of the first player (with the most games). */
	private String firstPlayerName;
	
	/** Mouse listener for player tables which handles opening player for double click, and shows the player menu for right click. */
	private final MouseListener playersTableMouseListener = new MouseAdapter() {
		@Override
		public void mouseClicked( final MouseEvent event ) {
			final JTable table = (JTable) event.getSource();
			if ( event.getButton() == GuiUtils.MOUSE_BUTTON_LEFT ) {
				if ( table.getSelectedRow() >= 0 && event.getClickCount() == 2 ) {
					final String playerName = (String) table.getValueAt( table.rowAtPoint( event.getPoint() ), table.convertColumnIndexToView( 0 ) );
					openPlayer( playerName );
				}
			} else if ( event.getButton() == GuiUtils.MOUSE_BUTTON_RIGHT ) {
				final int row = table.rowAtPoint( event.getPoint() );
				table.getSelectionModel().setSelectionInterval( row, row ); // Select only 1 player
				final String playerName = (String) table.getValueAt( row, table.convertColumnIndexToView( 0 ) );
				final PlayerStatistics ps = playerStatisticsMap.get( playerName );
				if ( ps != null ) { // It can be null (computer playmates for example)
					final JPopupMenu playerPopupMenu = new PlayerPopupMenu( ps.playerId, ps.playerType );
					playerPopupMenu.addSeparator();
					final JMenuItem openPlayerMenuItem = new JMenuItem( Language.getText( "module.multiRepAnal.playerMenu.openPlayer" ), Icons.CHART_UP );
					openPlayerMenuItem.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed( final ActionEvent event ) {
							openPlayer( playerName );
						}
					} );
					playerPopupMenu.add( openPlayerMenuItem );
					playerPopupMenu.show( event.getComponent(), event.getX(), event.getY() );
				}
			}
		}
	};
	
	/**
	 * Creates a new MultiRepAnalysis
	 * @param arguments optional arguments to define the files and folders to analyze<br>
	 * 		the <b>first</b>  element can be an optional replay source to load<br>
	 * 		the <b>second</b> element can be an optional replay list to load<br>
	 * 		the <b>third</b>  element can be a File array to perform the Multi-rep analysis on
	 */
	public MultiRepAnalysis( final Object... arguments ) {
		super( arguments.length == 0 ? Language.getText( "module.multiRepAnal.opening" ) : null ); // This title does not have a role as this internal frame is not displayed until replays are chosen, and then title is changed anyway
		
		setFrameIcon( Icons.CHART_UP_COLOR );
		
		if ( arguments.length == 0 ) {
			final JFileChooser fileChooser = new JFileChooser( GeneralUtils.getDefaultReplayFolder() );
			fileChooser.setDialogTitle( Language.getText( "module.multiRepAnal.openTitle" ) );
			fileChooser.setFileFilter( GuiUtils.SC2_REPLAY_FILTER );
			fileChooser.setAccessory( GuiUtils.createReplayFilePreviewAccessory( fileChooser ) );
			fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
			fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
			fileChooser.setMultiSelectionEnabled( true );
			if ( fileChooser.showOpenDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION )
				this.files = fileChooser.getSelectedFiles();
			else {
				dispose();
				this.files = null;
				return;
			}
		}
		else {
			if ( arguments.length > 0 && arguments[ 0 ] != null ) {
				// Replay source
				this.files = loadReplaySourceFile( (File) arguments[ 0 ] );
			}
			else if ( arguments.length > 1 && arguments[ 1 ] != null ) {
				// Replay list
				// TODO this can be sped up by reading the replay list by hand and only use the file name!
				final List< Object[] > dataList = ReplaySearch.loadReplayListFile( (File) arguments[ 1 ] );
				this.files = new File[ dataList.size() ];
				for ( int i = dataList.size() - 1; i >= 0; i-- )
					this.files[ i ] = new File( (String) dataList.get( i )[ ReplaySearch.COLUMN_FILE_NAME ] );
			}
			else if ( arguments.length > 2 && arguments[ 2 ] != null ) {
				// Replays to open
				this.files = (File[]) arguments[ 2 ];
			}
			else
				throw new RuntimeException( "The source for Multi-rep analysis is incorrectly specified!" );
		}
		
		setTitle( Language.getText( "module.multiRepAnal.title", counter.incrementAndGet() ) );
		
		buildGUI();
	}
	
	/**
	 * Loads the specified replay source file.
	 * @param replaySource replay source file to be loaded
	 * @return the files denoted by the replay source
	 */
	private static File[] loadReplaySourceFile( final File replaySource ) {
		try ( final BufferedReader input = new BufferedReader( new InputStreamReader( new FileInputStream( replaySource ), Consts.UTF8 ) ) ) {
			final List< File > replayList = new ArrayList< File >();
			
			while ( input.ready() )
				replayList.add( new File( input.readLine() ) );
			
			return replayList.toArray( new File[ replayList.size() ] );
		} catch ( final Exception e ) {
			e.printStackTrace();
			GuiUtils.showErrorDialog( Language.getText( "module.repSearch.tab.source.failedToLoadRepSource" ) );
			return new File[ 0 ];
		}
	}
	
	/**
	 * Builds the GUI of the frame.
	 */
	private void buildGUI() {
		final Box northBox = Box.createVerticalBox();
		final Box buttonsBox = Box.createHorizontalBox();
		final JButton abortButton = new JButton( Icons.CROSS_OCTAGON );
		GuiUtils.updateButtonText( abortButton, "module.multiRepAnal.abortAnalysisButton" );
		buttonsBox.add( abortButton );
		autoOpenFirstPlayerCheckBox.setToolTipText( Language.getText( "module.multiRepAnal.autoOpenFirstPlayerToolTip" ) );
		buttonsBox.add( autoOpenFirstPlayerCheckBox );
		buttonsBox.add( Box.createHorizontalStrut( 5 ) );
		buttonsBox.add( stretchToWindowCheckBox );
		buttonsBox.add( Box.createHorizontalStrut( 15 ) );
		buttonsBox.add( MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.MULTI_REP_ANALYSIS ) );
		buttonsBox.add( Box.createHorizontalStrut( 15 ) );
		buttonsBox.add( MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.ALIASES ) );
		northBox.add( GuiUtils.wrapInPanel( buttonsBox ) ); // If not wrapped, it doesn't get center-aligned
		final JProgressBar progressBar = new JProgressBar();
		northBox.add( progressBar );
		getContentPane().add( northBox, BorderLayout.NORTH );
		getContentPane().add( tabbedPane, BorderLayout.CENTER );
		
		final Thread analysisThread = new NormalThread( "Multi-replay analysis" ) {
			private volatile boolean aborted;
			private final ExecutorService executorService = GeneralUtils.createMultiThreadedExecutorService();
			private int          replaysCount;
			private volatile int analyzedCount;
			private volatile int skippedCount;
			// We have to use the same settings for all replays
			private final int     timeLimitToBeIncluded = Settings.getInt    ( Settings.KEY_SETTINGS_MISC_TIME_LIMIT_FOR_MULTI_REP_ANALYSIS );
			private final int     buildOrderLength      = Settings.getInt    ( Settings.KEY_SETTINGS_MISC_BUILD_ORDER_LENGTH                );
			private final int     timeExclusionForApm   = Settings.getInt    ( Settings.KEY_SETTINGS_MISC_INITIAL_TIME_TO_EXCLUDE_FROM_APM ) << FRAME_BITS_IN_SECOND;
			private final boolean cacheEnabled          = Settings.getBoolean( Settings.KEY_SETTINGS_MISC_CACHE_PREPROCESSED_REPLAYS        );
			@Override
			public void run() {
				final InternalFrameListener abortListener = new InternalFrameAdapter() {
					@Override
					public void internalFrameClosing( final InternalFrameEvent event ) {
						// If the Multi-replay analysis is closed, we want to stop the analysis
						abortButton.doClick();
					}
				};
				addInternalFrameListener( abortListener );
				
				abortButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						buttonsBox.remove( abortButton );
						getContentPane().validate();
						aborted = true;
					}
				} );
				progressBar.setStringPainted( true );
				progressBar.setString( Language.getText( "module.multiRepAnal.countingReplays" ) );
				
				for ( final File file : files ) {
					if ( aborted )
						break;
					replaysCount += countReplays( file );
				}
				
				if ( aborted ) {
					GeneralUtils.shutdownExecutorService( executorService );
					progressBar.setString( Language.getText( "module.multiRepAnal.analysisAborted" ) + " [" + progressBar.getString() + "]" );
				}
				else {
					progressBar.setMaximum( replaysCount );
					updateProgressBar();
					
					for ( final File file : files ) {
						if ( aborted )
							break;
						analyzeReplay( file );
					}
					GeneralUtils.shutdownExecutorService( executorService );
					
					if ( aborted ) {
						progressBar.setString( Language.getText( "module.multiRepAnal.analysisAborted" ) + " [" + progressBar.getString() + "]" );
					}
					else {
						buttonsBox.remove( abortButton );
						getContentPane().validate();
					}
					
					replaysIncludedInAnalysis = analyzedCount - skippedCount;
					
					// Now we have statistical info, build the rest of the GUI
					// Swing is single-threaded. We cannot update the GUI directly, else the Nimbus LaF thows ClassCastExceptions inconsistently.
					// (java.lang.ClassCastException: javax.swing.plaf.BorderUIResource cannot be cast to java.awt.Font)
					try {
						SwingUtilities.invokeAndWait( new Runnable() {
							@Override
							public void run() {
								GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.players.title" ), Icons.USERS, false, tabbedPane, createPlayersTab(), false, null );
								
								final JTabbedPane globalStatsTabbedPane = new JTabbedPane();
								GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.maps.title"        ), Icons.MAPS_STACK, false, globalStatsTabbedPane, createMapsTab       (), false, null );
								GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.buildOrders.title" ), Icons.BLOCK     , false, globalStatsTabbedPane, createBuildOrdersTab(), false, null );
								GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.chatWords.title"   ), Icons.BALLOONS  , false, globalStatsTabbedPane, createChatWordsTab  (), false, null );
								GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.globalStats.title" ), Icons.SUM, false, tabbedPane, globalStatsTabbedPane, false, null );
								
								// Auto-open first player if it's required
								if ( autoOpenFirstPlayerCheckBox.isSelected() && firstPlayerName != null )
									openPlayer( firstPlayerName );
							}
						} );
					} catch ( final InterruptedException ie ) {
						ie.printStackTrace();
					} catch ( final InvocationTargetException ite ) {
						ite.printStackTrace();
					}
				}
				
				removeInternalFrameListener( abortListener );
			}
			
			private int countReplays( final File file ) {
				if ( file.isFile() )
					return GuiUtils.SC2_REPLAY_FILTER.accept( file ) ? 1 : 0;
				else {
					final File[] children = file.listFiles();
					int replaysCount = 0;
					if ( children != null )
						for ( final File child : children ) {
							if ( aborted )
								return 0;
							replaysCount += countReplays( child );
						}
					return replaysCount;
				}
			}
			
			/** This is here to distinguish different players with the same name.<br>
			 * If only one occurrence found for a name, the value is the player identifier.<br>
			 * If more occurrences were found, the value will be a list of player identifiers.*/
			private final Map< String, Object > playerNameClonesMap = new HashMap< String, Object >();
			
			/** Lock to be used not to use same instance variables concurrently. */
			private final Object multiThreadLock = playerNameClonesMap;
			
			private void analyzeReplay( final File file ) {
				if ( file.isFile() ) {
					if ( GuiUtils.SC2_REPLAY_FILTER.accept( file ) ) // TODO: this will exclude explicitly specified files having different extension!
						executorService.execute( new Runnable() {
							@Override
							public void run() {
        						if ( aborted ) // ExecutorService.shutdown() continues to execute queued tasks, so check if aborted
        							return;
        						analyzedCount++;
        						
        						final Replay replay = ReplayCache.getReplay( file, buildOrderLength, timeExclusionForApm, cacheEnabled, cacheEnabled, null );
        						
            					if ( replay != null && replay.gameLength >= ( timeLimitToBeIncluded << 1 ) ) // To exclude short games
            						synchronized ( multiThreadLock ) {
            							final Date     replayDate = new Date( replay.details.saveTime );
            							final Date     startDate  = new Date( replay.details.saveTime - 1000L*replay.initData.gameSpeed.convertToRealTime( replay.gameLengthSec ) );
            							final Player[] players    = replay.details.players;
            							final Format   format     = replay.initData.format;
            							
            							// Distinguish different players with same name, apply aliases
            							final String[] playerDisplayNames = new String[ players.length ];
            							for ( int playerIndex = 0; playerIndex < players.length; playerIndex++ ) {
            								final Player   player   = players[ playerIndex ];
            								// Apply player aliases
            								player.playerId = Settings.getAliasGroupPlayerId( player.playerId );
            								
            								final PlayerId playerId = player.playerId;
            								
            								int cloneId = -1;
            								final Object clones = playerNameClonesMap.get( playerId.name );
            								if ( clones == null ) {
            									// First occurrence
            									playerNameClonesMap.put( player.playerId.name, playerId );
            									cloneId = 0;
            								}
            								else {
            									if ( clones instanceof List< ? > ) {
            										// At least 2 occurrence before
            										@SuppressWarnings("unchecked")
            										final List< PlayerId > cloneList = (List< PlayerId >) clones;
            										for ( int i = 0; i < cloneList.size(); i++ )
            											if ( playerId.equals( cloneList.get( i ) ) ) {
            												cloneId = i;
            												break;
            											}
            										if ( cloneId < 0 ) {
            											// New occurrence
            											cloneId = cloneList.size();
            											cloneList.add( playerId );
            										}
            									}
            									else {
            										if ( playerId.equals( (PlayerId) clones ) )
            											cloneId = 0; // The same occurrence as before
            										else {
            											// 2nd occurrence
            											final List< PlayerId > cloneList = new ArrayList< PlayerId >( 2 );
            											cloneList.add( (PlayerId) clones );
            											cloneList.add( playerId );
            											cloneId = 1;
            											playerNameClonesMap.put( playerId.name, cloneList );
            										}
            									}
            								}
            								
            								playerDisplayNames[ playerIndex ] = cloneId == 0 ? playerId.name : playerId.name + " (#" + (cloneId+1) + ")";
            							}
            							
            							final Map< String, IntHolder >[] wordCountMaps = replay.messageEvents.getWordCountMaps( replay.initData.clientNames == null ? players.length : replay.initData.clientNames.length );
            							
            							// Player statistics
            							Race winner1v1Race = null;
            							Race loser1v1Race  = null;
            							final PlayerGameParticipationStats[] pgpss = new PlayerGameParticipationStats[ players.length ];
            							final Set< String > replayWordSet = new HashSet< String >();
            							for ( int playerIndex = 0; playerIndex < players.length; playerIndex++ ) {
            								final Player player = players[ playerIndex ];
            								
            								int secondsInGame = player.lastActionFrame >> FRAME_BITS_IN_SECOND;
            								if ( useRealTime )
            									secondsInGame = replay.initData.gameSpeed.convertToRealTime( secondsInGame );
            								if ( secondsInGame < timeLimitToBeIncluded )
            									continue;
            								
            								int secondsInGameForApm = Math.max( 0, player.lastActionFrame - replay.excludedInitialFrames ) >> FRAME_BITS_IN_SECOND;
            								if ( useRealTime )
            									secondsInGameForApm = replay.initData.gameSpeed.convertToRealTime( secondsInGameForApm ); 
            								
            								int totalInjectionGap = player.totalInjectionGap;
            								if ( useRealTime )
            									totalInjectionGap = replay.initData.gameSpeed.convertToRealTime( totalInjectionGap );
            								
            								PlayerStatistics ps = playerStatisticsMap.get( playerDisplayNames[ playerIndex ] );
            								if ( ps == null )
            									playerStatisticsMap.put( playerDisplayNames[ playerIndex ], ps = new PlayerStatistics( playerDisplayNames[ playerIndex ], player ) );
            								
            								pgpss[ playerIndex ] = new PlayerGameParticipationStats(
            										replayDate, startDate, format, secondsInGame, secondsInGameForApm, player,
            										players.length > 1 ? players[ ( players[ 0 ] == player ? 1 : 0 ) ].finalRace : Race.UNKNOWN,
            										replay.details.mapName, replay.initData.gameSpeed,
            										getTeamLeagueCompositions( replay.details, player, format ),
            										getTeamRaceCompositions  ( replay.details, player, format ),
            										replay.initData.gameType, totalInjectionGap, wordCountMaps[ playerIndex ]
            								);
            								ps.buildInPlayerGameParticipation( pgpss[ playerIndex ] );
            								
            								// This is for the map statistics:
            								if ( format == Format.ONE_VS_ONE )
            									if ( player.isWinner != null )
            										if ( player.isWinner )
            											winner1v1Race = player.finalRace;
            										else
            											loser1v1Race = player.finalRace;
            								
            								for ( int playMateIndex = 0; playMateIndex < players.length; playMateIndex++ )
            									if ( playMateIndex != playerIndex )
            										( player.team != Player.TEAM_UNKNOWN && player.team == players[ playMateIndex ].team ? pgpss[ playerIndex ].allyList : pgpss[ playerIndex ].opponentList ).add( playerDisplayNames[ playMateIndex ] );
            								
            								ps.playerGameParticipationStatsList.add( pgpss[ playerIndex ] );
            							}
            							
            							// Map statistics
            							MapStatistics ms = mapStatisticsMap.get( replay.details.mapName );
            							if ( ms == null )
            								mapStatisticsMap.put( replay.details.mapName, ms = new MapStatistics( replay.details.mapName ) );
            							ms.record.totalGames++;
            							ms.totalTimeSecInGames += useRealTime ? replay.initData.gameSpeed.convertToRealTime( replay.gameLengthSec ) : replay.gameLengthSec;
            							ms.registerDate( replayDate );
            							if ( winner1v1Race != null ) {
            								final Record record = ms.getRaceRecord( winner1v1Race );
            								record.totalGames++;
            								record.wins++;
            							}
            							if ( loser1v1Race != null ) {
            								final Record record = ms.getRaceRecord( loser1v1Race );
            								record.totalGames++;
            								record.losses++;
            							}
            							
            							// Build order statistics
            							final int[] counters = new int[ players.length ];
            							int doneCounter = 0;
            							for ( int i = 0; i < pgpss.length; i++ )
            								if ( pgpss[ i ] == null ) { // Player left before 2 minutes, do not parse build orders for him/her
            									counters[ i ] = buildOrderLength;
            									doneCounter++;
            								}
            							final Building[][] buildingss = new Building[ players.length ][ buildOrderLength ];
            							final Action[] actions = replay.gameEvents.actions;
            							final int actionsLength = actions.length;
            							Action action;
            							for ( int i = 0; i < actionsLength; i++ ) {
            								if ( ( action = actions[ i ] ) instanceof BuildAction && counters[ action.player ] < buildOrderLength ) {
            									buildingss[ action.player ][ counters[ action.player ] ] = ( (BuildAction) action ).building;
            									if ( ++counters[ action.player ] == buildOrderLength )
            										if ( ++doneCounter == counters.length )
            											break;
            								}
            							}
            							for ( int i = buildingss.length - 1; i >= 0; i-- ) {
            								if ( pgpss[ i ] == null )
            									continue;
            								final Building[] buildings = buildingss[ i ];
            								final int buildingsLength = counters[ i ];
            								if ( buildingsLength == 0 )
            									continue;
            								final StringBuilder builder = new StringBuilder();
            								for ( int j = 0; j < buildingsLength; j++ ) {
            									if ( builder.length() > 0 )
            										builder.append( ", " );
            									builder.append( buildings[ j ].stringValue );
            								}
            								final String buildOrder = builder.toString();
            								pgpss[ i ].buildOrder   = buildOrder;
            								Map< String, BuildOrderStatistics > buildOrderStatisticsMap = formatBuildOrderStatisticsMap.get( replay.initData.format );
            								if ( buildOrderStatisticsMap == null )
            									formatBuildOrderStatisticsMap.put( replay.initData.format, buildOrderStatisticsMap = new HashMap< String, BuildOrderStatistics >() );
            								BuildOrderStatistics bs = buildOrderStatisticsMap.get( buildOrder );
            								if ( bs == null )
            									buildOrderStatisticsMap.put( buildOrder, bs = new BuildOrderStatistics( buildOrder, pgpss[ i ].race, replay.initData.format ) );
            								bs.buildInPlayerGameParticipation( pgpss[ i ] );
            							}
            							
            							// Chat words statistics
            							for ( final Map< String, IntHolder > wordCountMap : wordCountMaps ) {
            								if ( wordCountMap != null )
            									for ( final Entry< String, IntHolder > entry : wordCountMap.entrySet() ) {
            										final String word = entry.getKey();
            										WordStatistics wordStatistics = chatWordsStatisticsMap.get( word );
            										if ( wordStatistics == null )
            											chatWordsStatisticsMap.put( word, wordStatistics = new WordStatistics( entry.getKey() ) );
            										wordStatistics.registerDate( replayDate );
            										wordStatistics.count += entry.getValue().value;
            										if ( replayWordSet.add( word ) )
            											wordStatistics.replays++;
            									}
            							}
            						}
        						else
        							skippedCount++;
        						
        						updateProgressBar();
							}
						} );
				}
				else {
					final File[] children = file.listFiles();
					if ( children != null )
						for ( final File child : children ) {
							if ( aborted )
								return;
							analyzeReplay( child );
						}
				}
			}
			
			// Re-used objects...
			final List< String    > helperPlayerList       = Arrays.asList( "" );
			final List< Character > letterList             = new ArrayList< Character >( 4 );
			final StringBuilder     teamCompositionBuilder = new StringBuilder( 5 );
			
			private String[] getTeamLeagueCompositions( final Details details, final Player player, final Format format ) {
				if ( details.players.length == 0 || details.players[ 0 ].getLeague() == League.UNKNOWN ) // League is only available from replay version 2.0.
					return new String[ 0 ];
				if ( format == Format.ONE_VS_ONE )
					return new String[] { Character.toString( player.getLeague().letter ), Character.toString( ( details.players.length > 1 ? details.players[ ( details.players[ 0 ] == player ? 1 : 0 ) ].getLeague() : League.UNKNOWN ).letter ) };
				
				helperPlayerList.set( 0, player.playerId.name );
				details.rearrangePlayers( helperPlayerList );
				final int[] teamOrderPlayerIndices = details.getTeamOrderPlayerIndices();
				
				letterList.clear();
				
				final String[] teamCompositions;
				
				if ( format == Format.FREE_FOR_ALL ) {
					teamCompositions = new String[ teamOrderPlayerIndices.length ];
					// 1 player in each team, many teams, so we have to sort the teams to get the race letters sorted
					for ( int i = 0; i < teamOrderPlayerIndices.length; i++ )
						letterList.add( details.players[ teamOrderPlayerIndices[ i ] ].getLeague().letter );
					Collections.sort( letterList );
					for ( int i = teamOrderPlayerIndices.length - 1; i >= 0; i-- )
						teamCompositions[ i ] = Character.toString( letterList.get( i ) );
				}
				else {
					int teamsCount = 1;
					int lastTeam = details.players[ teamOrderPlayerIndices[ 0 ] ].team;
					for ( int i = 1; i < teamOrderPlayerIndices.length; i++ ) {
						final int team = details.players[ teamOrderPlayerIndices[ i ] ].team;
						if ( team != lastTeam ) {
							lastTeam = team;
							teamsCount++;
						}
					}
					teamCompositions = new String[ teamsCount ];
					
					teamCompositionBuilder.setLength( 0 );
					teamCompositionBuilder.append( details.players[ teamOrderPlayerIndices[ 0 ] ].getLeague().letter );
					
					teamCompositionBuilder.append( '+' );
					teamsCount = 0;
					lastTeam = details.players[ teamOrderPlayerIndices[ 0 ] ].team;
					for ( int i = 1; i < teamOrderPlayerIndices.length; i++ ) {
						final int team = details.players[ teamOrderPlayerIndices[ i ] ].team;
						if ( team != lastTeam ) {
							Collections.sort( letterList );
							for ( final Character leagueLetter : letterList )
								teamCompositionBuilder.append( leagueLetter );
							teamCompositions[ teamsCount++ ] = teamCompositionBuilder.toString();
							teamCompositionBuilder.setLength( 0 );
							letterList.clear();
							lastTeam = team;
						}
						letterList.add( details.players[ teamOrderPlayerIndices[ i ] ].getLeague().letter );
					}
					Collections.sort( letterList );
					for ( final Character leagueLetter : letterList )
						teamCompositionBuilder.append( leagueLetter );
					teamCompositions[ teamCompositions.length - 1 ] = teamCompositionBuilder.toString();
				}
				
				return teamCompositions;
			}
			
			private String[] getTeamRaceCompositions( final Details details, final Player player, final Format format ) {
				if ( details.players.length == 0 )
					return new String[ 0 ];
				if ( format == Format.ONE_VS_ONE )
					return new String[] { Character.toString( player.finalRace.letter ), Character.toString( ( details.players.length > 1 ? details.players[ ( details.players[ 0 ] == player ? 1 : 0 ) ].finalRace : Race.UNKNOWN ).letter ) };
				
				helperPlayerList.set( 0, player.playerId.name );
				details.rearrangePlayers( helperPlayerList );
				final int[] teamOrderPlayerIndices = details.getTeamOrderPlayerIndices();
				
				letterList.clear();
				
				final String[] teamCompositions;
				
				if ( format == Format.FREE_FOR_ALL ) {
					teamCompositions = new String[ teamOrderPlayerIndices.length ];
					// 1 player in each team, many teams, so we have to sort the teams to get the race letters sorted
					for ( int i = 0; i < teamOrderPlayerIndices.length; i++ )
						letterList.add( details.players[ teamOrderPlayerIndices[ i ] ].getRaceLetter() );
					Collections.sort( letterList );
					for ( int i = teamOrderPlayerIndices.length - 1; i >= 0; i-- )
						teamCompositions[ i ] = Character.toString( letterList.get( i ) );
				}
				else {
					int teamsCount = 1;
					int lastTeam = details.players[ teamOrderPlayerIndices[ 0 ] ].team;
					for ( int i = 1; i < teamOrderPlayerIndices.length; i++ ) {
						final int team = details.players[ teamOrderPlayerIndices[ i ] ].team;
						if ( team != lastTeam ) {
							lastTeam = team;
							teamsCount++;
						}
					}
					teamCompositions = new String[ teamsCount ];
					
					teamCompositionBuilder.setLength( 0 );
					teamCompositionBuilder.append( details.players[ teamOrderPlayerIndices[ 0 ] ].getRaceLetter() );
					
					teamCompositionBuilder.append( '+' );
					teamsCount = 0;
					lastTeam = details.players[ teamOrderPlayerIndices[ 0 ] ].team;
					for ( int i = 1; i < teamOrderPlayerIndices.length; i++ ) {
						final int team = details.players[ teamOrderPlayerIndices[ i ] ].team;
						if ( team != lastTeam ) {
							Collections.sort( letterList );
							for ( final Character raceLetter : letterList )
								teamCompositionBuilder.append( raceLetter );
							teamCompositions[ teamsCount++ ] = teamCompositionBuilder.toString();
							teamCompositionBuilder.setLength( 0 );
							letterList.clear();
							lastTeam = team;
						}
						letterList.add( details.players[ teamOrderPlayerIndices[ i ] ].getRaceLetter() );
					}
					Collections.sort( letterList );
					for ( final Character raceLetter : letterList )
						teamCompositionBuilder.append( raceLetter );
					teamCompositions[ teamCompositions.length - 1 ] = teamCompositionBuilder.toString();
				}
				
				return teamCompositions;
			}
			
			private void updateProgressBar() {
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						progressBar.setValue( analyzedCount );
						progressBar.setString( Language.getText( "module.multiRepAnal.analysisStatus", analyzedCount, skippedCount, replaysCount, replaysCount == 0 ? 100 : 100 * analyzedCount / replaysCount ) );
					}
				} );
			}
		};
		
		// We have to start the analysis thread "later", else there some kind of blocking occurs (while this internal frame is active, CTRL+F4 and ALT+F4 are not working (and probably amongst others) )
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				analysisThread.start();
			}
		} );
	}
	
	/**
	 * Creates and returns the players tab.
	 * @return the players tab
	 */
	private JComponent createPlayersTab() {
		final JPanel panel = new JPanel( new BorderLayout() );
		final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( playerStatisticsMap.size() );
		
		for ( final PlayerStatistics ps : playerStatisticsMap.values() ) {
			final Vector< Object > row = new Vector< Object >( 15 );
			row.add( ps.playerDisplayName );
			row.add( ps.record.totalGames );
			row.add( ps.getAvgApm() );
			row.add( ps.getAvgEapm() );
			row.add( ps.getAvgApmRedundancy() );
			row.add( ps.record );
			row.add( ps.record.getWinRatio() );
			row.add( ps.getRaceDistributionString() );
			row.add( ps.getFormattedTotalTimeInGames() );
			row.add( ReplayUtils.formatMs( ps.getAvgGameLength() * 1000, GameSpeed.NORMAL ) ); // Passing GameSpeed.NORMAL because it has already been converted
			row.add( ps.getPresence() );
			row.add( ps.getAvgGamesPerDay() );
			row.add( Language.formatDate( ps.firstDate ) );
			row.add( Language.formatDate( ps.lastDate )  );
			row.add( dataVector.add( row ) );
		}
		
		if ( autoOpenFirstPlayerCheckBox.isSelected() ) {
			int maxTotalGames = 0;
			for ( final Vector< Object > row : dataVector )
				if ( maxTotalGames < ( (Integer) row.get( 1 ) ).intValue() ) {
					firstPlayerName = (String) row.get( 0 );
					maxTotalGames   = ( (Integer) row.get( 1 ) ).intValue();
				}
		}
		
		final Holder< JTable > tableHolder = new Holder< JTable >();
		createStatisticsTableTab( panel, "module.multiRepAnal.tab.players.info", new Object[] { playerStatisticsMap.size() }, 0, new int[] { 1, 0 }, dataVector, PLAYERS_HEADER_NAME_VECTOR, new WordCloudTableInput( Language.getText( "module.multiRepAnal.tab.players.title" ), 0, 1 ), tableHolder, null );
		final JLabel infoLabel = (JLabel) panel.getComponent( 0 );
		infoLabel.setOpaque( true ); // Needed for the background color to take effect
		infoLabel.setBackground( Color.GREEN );
		tableHolder.value.addMouseListener( playersTableMouseListener );
		registerEnterToOpenPlayer( tableHolder.value );
		
		return panel;
	}
	
	/**
	 * Registers the Enter and Shift+Enter keystrokes to the specified players table to open the selected player.
	 * @param table players table to register keystrokes to
	 */
	private void registerEnterToOpenPlayer( final JTable table ) {
		Object actionKey;
		// Add Enter and Shift+Enter keystroke to open selected player
		table.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), actionKey = new Object() );
		table.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK ), actionKey );
		table.getActionMap().put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				if ( table.getSelectedRow() >= 0 ) {
					final String playerName = (String) table.getValueAt( table.getSelectedRow(), table.convertColumnIndexToView( 0 ) );
					openPlayer( playerName );
				}
			}
		} );
	}
	
	/**
	 * Opens the detailed statistics of a player.
	 * @param playerName name of the player whose detailed statistics to be opened
	 */
	private void openPlayer( final String playerName ) {
		final PlayerStatistics playerStatistics = playerStatisticsMap.get( playerName );
		if ( playerStatistics == null ) // It can be null if the player did not have any "long" games, but was listed as a playmate
			return;
		final Holder< JComponent > chartsPanelHolder = new Holder< JComponent >();
		// Action listeners to stretch to window have to be removed if player tab is closed.
		final List< ActionListener > stretchToWindowActionListenerList = new ArrayList< ActionListener >();		
		GuiUtils.addNewTab( playerName, Icons.USER, true, tabbedPane, createPlayerTab( playerStatistics, chartsPanelHolder, stretchToWindowActionListenerList ), false, new Runnable() {
			@Override
			public void run() {
				for ( final ActionListener actionListener : stretchToWindowActionListenerList )
					stretchToWindowCheckBox.removeActionListener( actionListener );
				tabbedPane.setSelectedIndex( 0 ); // Select the Players tab when this player is closed (that's where we go to this tab from)
			}
		} );
		tabbedPane.setSelectedIndex( tabbedPane.getTabCount() - 1 );
		
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				chartsPanelHolder.value.requestFocusInWindow();
			}
		} );
	}
	
	/**
	 * Creates and returns the maps tab.
	 * @return the maps tab
	 */
	private JComponent createMapsTab() {
		final JPanel panel = new JPanel( new BorderLayout() );
		panel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( mapStatisticsMap.size() );
        		final NullAwareComparable< Integer > nullWinRatio = NullAwareComparable.getPercent( null );
        		
        		for ( final MapStatistics ms : mapStatisticsMap.values() ) {
        			final Record pRecord = ms.raceRecordMap.get( Race.PROTOSS );
        			final Record tRecord = ms.raceRecordMap.get( Race.TERRAN  );
        			final Record zRecord = ms.raceRecordMap.get( Race.ZERG    );
        			final Vector< Object > row = new Vector< Object >( 9 );
        			row.add( ms.name );
        			row.add( ms.record.totalGames );
        			row.add( NullAwareComparable.getPercent( ms.record.totalGames * 100 / replaysIncludedInAnalysis ) );
        			row.add( pRecord == null ? nullWinRatio : pRecord.getWinRatio() );
        			row.add( tRecord == null ? nullWinRatio : tRecord.getWinRatio() );
        			row.add( zRecord == null ? nullWinRatio : zRecord.getWinRatio() );
        			row.add( ReplayUtils.formatMs( ms.getAvgGameLength() * 1000, GameSpeed.NORMAL ) ); // Passing GameSpeed.NORMAL because it has already been converted
        			row.add( Language.formatDate( ms.firstDate ) );
        			row.add( Language.formatDate( ms.lastDate ) );
        			dataVector.add( row );
        		}
        		
        		createStatisticsTableTab( panel, "module.multiRepAnal.tab.maps.info", new Object[] { mapStatisticsMap.size() }, 0, new int[] { 1, 0 }, dataVector, MAPS_HEADER_NAME_VECTOR, new WordCloudTableInput( Language.getText( "module.multiRepAnal.tab.maps.title" ), 0, 1 ), null, null );
			}
		} );
		
		return panel;
	}
	
	/**
	 * Creates and returns the Build orders tab.
	 * @return the Build orders tab
	 */
	private JComponent createBuildOrdersTab() {
		final JPanel panel = new JPanel( new BorderLayout() );
		panel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		final JTabbedPane buildOrdersTabbedPane = new JTabbedPane();
        		for ( final Format format : EnumCache.FORMATS ) {
        			final Map< String, BuildOrderStatistics > buildOrderStatisticsMap = formatBuildOrderStatisticsMap.get( format );
        			if ( buildOrderStatisticsMap == null )
        				continue;
        			final JPanel buildOrdersPanel = new JPanel( new BorderLayout() );
        			buildOrdersPanel.addComponentListener( new FirstShownListener() {
        				@Override
        				public void firstShown( final ComponentEvent event ) {
                			final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( buildOrderStatisticsMap.size() );
                			
                			for ( final BuildOrderStatistics bs : buildOrderStatisticsMap.values() ) {
                				if ( format == Format.ONE_VS_ONE ) {
                					final Record recordVsP = bs.recordVsRaceMap.get( Race.PROTOSS );
                					final Record recordVsT = bs.recordVsRaceMap.get( Race.TERRAN  );
                					final Record recordVsZ = bs.recordVsRaceMap.get( Race.ZERG    );
                					final Vector< Object > row = new Vector< Object >( 10 );
                					row.add( bs.race );
                					row.add( bs.buildOrder );
                					row.add( bs.record.totalGames );
                					row.add( bs.record );
                					row.add( bs.record.getWinRatio() );
                					row.add( recordVsP == null ? new Record() : recordVsP );
                					row.add( recordVsT == null ? new Record() : recordVsT );
                					row.add( recordVsZ == null ? new Record() : recordVsZ );
                					row.add( Language.formatDate( bs.firstDate ) );
                					row.add( Language.formatDate( bs.lastDate ) );
                					dataVector.add( row );
                				}
                				else {
                					final Vector< Object > row = new Vector< Object >( 7 );
                					row.add( bs.race );
                					row.add( bs.buildOrder );
                					row.add( bs.record.totalGames );
                					row.add( bs.record );
                					row.add( bs.record.getWinRatio() );
                					row.add( Language.formatDate( bs.firstDate ) );
                					row.add( Language.formatDate( bs.lastDate ) );
                					dataVector.add( row );
                				}
                			}
            			
                			createStatisticsTableTab( buildOrdersPanel, "module.multiRepAnal.tab.buildOrders.info", new Object[] { buildOrderStatisticsMap.size() }, 1, new int[] { 2, 0, 4 }, dataVector, format == Format.ONE_VS_ONE ? BUILD_ORDERS_1V1_HEADER_NAME_VECTOR : BUILD_ORDERS_NON_1V1_HEADER_NAME_VECTOR, null, null, null );
        				}
        			} );
        			GuiUtils.addNewTab( format.stringValue, null, false, buildOrdersTabbedPane, buildOrdersPanel, false, null );
        		}
        		panel.add( buildOrdersTabbedPane );
			}
		} );
		
		return panel;
	}
	
	/**
	 * Creates and returns the Chat words tab.
	 * @return the Chat words tab
	 */
	private JComponent createChatWordsTab() {
		final JPanel panel = new JPanel( new BorderLayout() );
		panel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
    		final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( chatWordsStatisticsMap.size() );
    		
    		for ( final WordStatistics ws : chatWordsStatisticsMap.values() ) {
    			final Vector< Object > row = new Vector< Object >( 6 );
    			row.add( ws.word );
    			row.add( ws.count );
    			row.add( ws.replays );
    			row.add( NullAwareComparable.getPercent( ws.replays * 100 / replaysIncludedInAnalysis ) );
    			row.add( Language.formatDate( ws.firstDate ) );
    			row.add( Language.formatDate( ws.lastDate ) );
    			dataVector.add( row );
    		}
    		
    		createStatisticsTableTab( panel, "module.multiRepAnal.tab.chatWords.info", new Object[] { chatWordsStatisticsMap.size() }, 0, new int[] { 1, 2, 0 }, dataVector, CHAT_WORDS_HEADER_NAME_VECTOR, new WordCloudTableInput( Language.getText( "module.multiRepAnal.tab.chatWords.title" ), 0, 1 ), null, null );
			}
		} );
		
		return panel;
	}
	
	/**
	 * Creates and returns the player tab for the specified player.
	 * @param ps                player statistics of the player to create tab for
	 * @param chartsPanelHolder outgoing reference holder to the charts panel, will be used to request focus on it
	 * @param stretchToWindowActionListenerList the tables' action listeners to stretch to window will be added to this
	 * @return the player tab for the specified player
	 */
	private JComponent createPlayerTab( final PlayerStatistics ps, final Holder< JComponent > chartsPanelHolder, final List< ActionListener > stretchToWindowActionListenerList ) {
		final JPanel panel = new JPanel( new BorderLayout() );
		
		// Player game participation stat list must be in chronological order for the gaming sessions.
		// Development chart also builds on this list being sorted by date
		Collections.sort( ps.playerGameParticipationStatsList );
		
		final int gameLengthRecordsGranularityMin = Settings.getInt( Settings.KEY_SETTINGS_MISC_GAME_LENGTH_RECORDS_GRANULARITY );
		final int gameLengthRecordsGranularitySec = gameLengthRecordsGranularityMin * 60;
		final long maxGamingSessionBreakMs        = Settings.getInt( Settings.KEY_SETTINGS_MISC_MAX_GAMING_SESSION_BREAK ) * 60L*1000;
		
		// Calculate advanced player statistics
		final Map< Format, PlayerStatistics     > formatPlayerStatsMap        = new EnumMap< Format  , PlayerStatistics   >( Format.class );
		final Map< GameType, PlayerStatistics   > typePlayerStatsMap          = new EnumMap< GameType, PlayerStatistics   >( GameType.class );
		// League match-up stats for all formats
		final Map< Format, Map< String, PlayerStatistics > > formatLeagueMatchupPlayerStatsMapMap = new EnumMap< Format, Map< String, PlayerStatistics > >( Format.class );
		// Match-up stats for all formats
		final Map< Format, Map< String, PlayerStatistics > > formatMatchupPlayerStatsMapMap = new EnumMap< Format, Map< String, PlayerStatistics > >( Format.class );
		// Match-up stats by maps (key: pair of map and match-up)
		final Map< Pair< String, String >, PlayerStatistics > matchupByMapsPlayerStatsMap = new HashMap< Pair< String, String >, PlayerStatistics >();
		// Key is the game length interval index: if granularity is 5 min: "0-5 min" => 0, "5-10 min" => 1 ...
		final Map< Integer, PlayerStatistics    > gameLengthPlayerStatsMap    = new HashMap< Integer, PlayerStatistics    >();
		final PlayerStatistics[] dayPlayerStats  = new PlayerStatistics[ DAY_TEXT_KEYS.length ];
		final PlayerStatistics[] hourPlayerStats = new PlayerStatistics[ 24                   ];
		final Map< String, MapStatistics        > mapStatisticsMap            = new HashMap< String , MapStatistics       >();
		// Build order stats for all formats
		final Map< Format, Map< String, BuildOrderStatistics > > formatBuildOrderStatisticsMap = new EnumMap< Format, Map< String, BuildOrderStatistics > >( Format.class );
		final Map< String, PlaymateStatistics   > playmateStatisticsMap       = new HashMap< String , PlaymateStatistics  >();
		// Game in session stats; it's a list, the index is the game number in the gaming session (no need for map where the index would be the key...)
		final List< PlayerStatistics            > gameInSessionStatsList      = new ArrayList< PlayerStatistics           >();
		// Chat word stats
		final Map< String, WordStatistics       > chatWordsStatisticsMap      = new HashMap< String , WordStatistics      >();
		
		final GregorianCalendar gc = new GregorianCalendar();
		final int[] hourlyActivities  = new int[ 24 ];
		final int[] dailyActivities   = new int[  7 ];
		final int[] monthlyActivities = new int[ 12 ];
		
		int currentGamingSessionLength = 0;
		
		PlayerGameParticipationStats lastPgps = null;
		for ( final PlayerGameParticipationStats pgps : ps.playerGameParticipationStatsList ) {
			// Activity trend data
			gc.setTime( pgps.date );
			hourlyActivities [ gc.get( Calendar.HOUR_OF_DAY )   ]++;
			dailyActivities  [ gc.get( Calendar.DAY_OF_WEEK )-1 ]++;
			monthlyActivities[ gc.get( Calendar.MONTH       )   ]++;
			
			PlayerStatistics vps; // A virtual Player Statistics
			
			// Game type statistics (Game type is never null)
			vps = typePlayerStatsMap.get( pgps.gameType );
			if ( vps == null )
				typePlayerStatsMap.put( pgps.gameType, vps = new PlayerStatistics( null, null ) );
			vps.buildInPlayerGameParticipation( pgps );
			
			// Format statistics (Format is never null)
			vps = formatPlayerStatsMap.get( pgps.format );
			if ( vps == null )
				formatPlayerStatsMap.put( pgps.format, vps = new PlayerStatistics( null, null ) );
			vps.buildInPlayerGameParticipation( pgps );
			
			// League match-up statistics
			if ( pgps.teamLeagueCompositions.length > 0 ) {
				Map< String, PlayerStatistics > leagueMatchupPlayerStatsMap = formatLeagueMatchupPlayerStatsMapMap.get( pgps.format );
				if ( leagueMatchupPlayerStatsMap == null )
					formatLeagueMatchupPlayerStatsMapMap.put( pgps.format, leagueMatchupPlayerStatsMap = new HashMap< String, PlayerStatistics >() );
				if ( pgps.format == Format.FREE_FOR_ALL || pgps.format == Format.UNKNOWN ) {
					final StringBuilder leagueMatchupBuilder = new StringBuilder();
					for ( final String teamComposition : pgps.teamLeagueCompositions ) {
						if ( leagueMatchupBuilder.length() > 0 )
							leagueMatchupBuilder.append( 'v' );
						leagueMatchupBuilder.append( teamComposition );
					}
					String leagueMatchup = leagueMatchupBuilder.toString();
					vps = leagueMatchupPlayerStatsMap.get( leagueMatchup );
					if ( vps == null )
						leagueMatchupPlayerStatsMap.put( leagueMatchup, vps = new PlayerStatistics( null, null ) );
					vps.buildInPlayerGameParticipation( pgps );
					if ( pgps.format == Format.FREE_FOR_ALL ) {
						leagueMatchup = pgps.teamLeagueCompositions[ 0 ] + "v" + League.ANY.letter;
						vps = leagueMatchupPlayerStatsMap.get( leagueMatchup );
						if ( vps == null )
							leagueMatchupPlayerStatsMap.put( leagueMatchup, vps = new PlayerStatistics( null, null ) );
						vps.buildInPlayerGameParticipation( pgps );
					}
				}
				else {
					final String teamSeparator = pgps.format == Format.ONE_VS_ONE ? "v" : " vs ";
					final int cyclesCount = pgps.format == Format.ONE_VS_ONE ? 3 : 4;
					for ( int i = 0; i < cyclesCount; i++ ) {
						// 3 cycles: one for normal match-up, one for summarized opponent ANY (*) and one for summarized own ANY (*)
						// 4th     : league + summarized teammates ANY (*) (which is for example: D+* vs *, P+* vs *, G+* vs *)
						final String leagueMatchup;
						if ( pgps.teamLeagueCompositions.length > 1 )
							switch ( i ) {
							case 0 : leagueMatchup = pgps.teamLeagueCompositions[ 0 ] + teamSeparator + pgps.teamLeagueCompositions[ 1 ]; break;
							case 1 : leagueMatchup = pgps.teamLeagueCompositions[ 0 ] + teamSeparator + League.ANY.letter;                break;
							case 2 : leagueMatchup = League.ANY.letter                + teamSeparator + pgps.teamLeagueCompositions[ 1 ]; break;
							case 3 : {
								if ( pgps.teamLeagueCompositions[ 0 ].length() < 3 || pgps.teamLeagueCompositions[ 0 ].charAt( 1 ) != '+' )
									continue;
								leagueMatchup = pgps.teamLeagueCompositions[ 0 ].charAt( 0 ) + "+" + League.ANY.letter + teamSeparator + League.ANY.letter; break;
							}
							default : throw new RuntimeException( "Fix the cycle!" );
							}
						else {
							if ( i > 0 ) // League match-ups where there is no opponent should only be counted once
								break;
							leagueMatchup = pgps.teamLeagueCompositions.length == 1 ? pgps.teamLeagueCompositions[ 0 ] : Language.getText( "general.unknown" );
						}
						vps = leagueMatchupPlayerStatsMap.get( leagueMatchup );
						if ( vps == null )
							leagueMatchupPlayerStatsMap.put( leagueMatchup, vps = new PlayerStatistics( null, null ) );
						vps.buildInPlayerGameParticipation( pgps );
					}
				}
			}

			
			// Match-up statistics; and Match-up by maps statistics
			Map< String, PlayerStatistics > matchupPlayerStatsMap = formatMatchupPlayerStatsMapMap.get( pgps.format );
			if ( matchupPlayerStatsMap == null )
				formatMatchupPlayerStatsMapMap.put( pgps.format, matchupPlayerStatsMap = new HashMap< String, PlayerStatistics >() );
			if ( pgps.format == Format.FREE_FOR_ALL || pgps.format == Format.UNKNOWN ) {
				final StringBuilder matchupBuilder = new StringBuilder();
				for ( final String teamComposition : pgps.teamRaceCompositions ) {
					if ( matchupBuilder.length() > 0 )
						matchupBuilder.append( 'v' );
					matchupBuilder.append( teamComposition );
				}
				String matchup = matchupBuilder.toString();
				vps = matchupPlayerStatsMap.get( matchup );
				if ( vps == null )
					matchupPlayerStatsMap.put( matchup, vps = new PlayerStatistics( null, null ) );
				vps.buildInPlayerGameParticipation( pgps );
				if ( pgps.format == Format.FREE_FOR_ALL ) {
					matchup = pgps.teamRaceCompositions[ 0 ] + "v" + Race.ANY.letter;
					vps = matchupPlayerStatsMap.get( matchup );
					if ( vps == null )
						matchupPlayerStatsMap.put( matchup, vps = new PlayerStatistics( null, null ) );
					vps.buildInPlayerGameParticipation( pgps );
				}
			}
			else {
				final String teamSeparator = pgps.format == Format.ONE_VS_ONE ? "v" : " vs ";
				final int cyclesCount = pgps.format == Format.ONE_VS_ONE ? 3 : 4;
				for ( int i = 0; i < cyclesCount; i++ ) {
					// 3 cycles: one for normal match-up, one for summarized opponent ANY (*) and one for summarized own ANY (*)
					// 4th     : race + summarized teammates ANY (*) (which is for example: P+* vs *, T+* vs *, Z+* vs *)
					final String matchup;
					if ( pgps.teamRaceCompositions.length > 1 )
						switch ( i ) {
						case 0 : matchup = pgps.teamRaceCompositions[ 0 ] + teamSeparator + pgps.teamRaceCompositions[ 1 ]; break;
						case 1 : matchup = pgps.teamRaceCompositions[ 0 ] + teamSeparator + Race.ANY.letter;            break;
						case 2 : matchup = Race.ANY.letter            + teamSeparator + pgps.teamRaceCompositions[ 1 ]; break;
						case 3 : {
							if ( pgps.teamRaceCompositions[ 0 ].length() < 3 || pgps.teamRaceCompositions[ 0 ].charAt( 1 ) != '+' )
								continue;
							matchup = pgps.teamRaceCompositions[ 0 ].charAt( 0 ) + "+" + Race.ANY.letter + teamSeparator + Race.ANY.letter; break;
						}
						default : throw new RuntimeException( "Fix the cycle!" );
						}
					else {
						if ( i > 0 ) // Match-ups where there is no opponent should only be counted once
							break;
						matchup = pgps.teamRaceCompositions.length == 1 ? pgps.teamRaceCompositions[ 0 ] : Language.getText( "general.unknown" );
					}
					vps = matchupPlayerStatsMap.get( matchup );
					if ( vps == null )
						matchupPlayerStatsMap.put( matchup, vps = new PlayerStatistics( null, null ) );
					vps.buildInPlayerGameParticipation( pgps );
					// Match-ups by maps:
					if ( pgps.format == Format.ONE_VS_ONE ) {
						final Pair< String, String > key = new Pair< String, String >( pgps.mapName, matchup );
						vps = matchupByMapsPlayerStatsMap.get( key );
						if ( vps == null )
							matchupByMapsPlayerStatsMap.put( key, vps = new PlayerStatistics( null, null ) );
						vps.buildInPlayerGameParticipation( pgps );
					}
				}
			}
			
			// Game length statistics
			final Integer gameLengthIntervalIndex = pgps.timeSecInGame / gameLengthRecordsGranularitySec;
			vps = gameLengthPlayerStatsMap.get( gameLengthIntervalIndex );
			if ( vps == null )
				gameLengthPlayerStatsMap.put( gameLengthIntervalIndex, vps = new PlayerStatistics( null, null ) );
			vps.buildInPlayerGameParticipation( pgps );
			
			// Day statistics
			vps = dayPlayerStats[ gc.get( Calendar.DAY_OF_WEEK )-1 ];
			if ( vps == null )
				vps = dayPlayerStats[ gc.get( Calendar.DAY_OF_WEEK )-1 ] = new PlayerStatistics( null, null );
			vps.buildInPlayerGameParticipation( pgps );
			
			// Hour statistics
			vps = hourPlayerStats[ gc.get( Calendar.HOUR_OF_DAY ) ];
			if ( vps == null )
				vps = hourPlayerStats[ gc.get( Calendar.HOUR_OF_DAY ) ] = new PlayerStatistics( null, null );
			vps.buildInPlayerGameParticipation( pgps );
			
			// Map statistics
			MapStatistics ms = mapStatisticsMap.get( pgps.mapName );
			if ( ms == null )
				mapStatisticsMap.put( pgps.mapName, ms = new MapStatistics( pgps.mapName ) );
			ms.record.totalGames++;
			ms.totalTimeSecInGames += pgps.timeSecInGame; // No conversion (with game speed) because this time value has already been converted
			if ( pgps.isWinner != null )
				if ( pgps.isWinner )
					ms.record.wins++;
				else
					ms.record.losses++;
			ms.registerDate( pgps.date );
			if ( pgps.format == Format.ONE_VS_ONE && pgps.isWinner != null ) {
				final Record record = ms.getRaceRecord( pgps.race );
				record.totalGames++;
				if ( pgps.isWinner )
					record.wins++;
				else
					record.losses++;
			}
			
			// Build orders
			Map< String, BuildOrderStatistics > buildOrderStatisticsMap = formatBuildOrderStatisticsMap.get( pgps.format );
			if ( buildOrderStatisticsMap == null )
				formatBuildOrderStatisticsMap.put( pgps.format, buildOrderStatisticsMap = new HashMap< String, BuildOrderStatistics >() );
			BuildOrderStatistics bs = buildOrderStatisticsMap.get( pgps.buildOrder );
			if ( bs == null )
				buildOrderStatisticsMap.put( pgps.buildOrder, bs = new BuildOrderStatistics( pgps.buildOrder, pgps.race, pgps.format ) );
			bs.buildInPlayerGameParticipation( pgps );
			
			// Playmate statistics
			for ( final String playmate : pgps.allyList ) {
				PlaymateStatistics playmateStatistics = playmateStatisticsMap.get( playmate );
				if ( playmateStatistics == null )
					playmateStatisticsMap.put( playmate, playmateStatistics = new PlaymateStatistics( playmate ) );
				playmateStatistics.buildInPlayerGameParticipation( pgps, true );
			}
			for ( final String playmate : pgps.opponentList ) {
				PlaymateStatistics playmateStatistics = playmateStatisticsMap.get( playmate );
				if ( playmateStatistics == null )
					playmateStatisticsMap.put( playmate, playmateStatistics = new PlaymateStatistics( playmate ) );
				playmateStatistics.buildInPlayerGameParticipation( pgps, false );
			}
			
			// Gaming Sessions statistics
			if ( lastPgps == null || pgps.startDate.getTime() - maxGamingSessionBreakMs > lastPgps.startDate.getTime()
						+ ( useRealTime ? lastPgps.timeSecInGame : lastPgps.gameSpeed.convertToRealTime( lastPgps.timeSecInGame ) )*1000L ) // Real time in last game
				currentGamingSessionLength = 1; // Too big time break, start a new session
			else
				currentGamingSessionLength++;
			if ( gameInSessionStatsList.size() < currentGamingSessionLength )
				gameInSessionStatsList.add( vps = new PlayerStatistics( null, null ) );
			else
				vps = gameInSessionStatsList.get( currentGamingSessionLength - 1 );
			vps.buildInPlayerGameParticipation( pgps );
			
			// Chat words statistics
			if ( pgps.wordCountMap != null )
				for ( final Entry< String, IntHolder > entry : pgps.wordCountMap.entrySet() ) {
					WordStatistics wordStatistics = chatWordsStatisticsMap.get( entry.getKey() );
					if ( wordStatistics == null )
						chatWordsStatisticsMap.put( entry.getKey(), wordStatistics = new WordStatistics( entry.getKey() ) );
					wordStatistics.buildInCount( entry.getValue().value, pgps.date );
				}
			
			lastPgps = pgps;
		}
		
		// Advanced statistics ready. Now build the GUI.
		final JLabel infoLabel = new JLabel( Language.getText( "module.multiRepAnal.tab.player.info", ps.playerDisplayName, ps.record.totalGames ) );
		GuiUtils.changeFontToBold( infoLabel );
		panel.add( infoLabel, BorderLayout.NORTH );
		final JTabbedPane tabbedPane = new JTabbedPane();
		
		// Development charts tab
		{
			final JPanel chartsPanel = new JPanel( new BorderLayout() );
			chartsPanelHolder.value = chartsPanel;
			// Pgps list is sorted...
			final Holder< JComponent > chartCanvasHolder = new Holder< JComponent >();
			final ActionListener chartCanvasRepaintListener = new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					chartCanvasHolder.value.repaint();
				}
			};
			final Box chartOptionsBox = Box.createHorizontalBox();
			chartOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBoxes
			chartOptionsBox.add( new JLabel( Language.getText( "charts.chartType" ) ) );
			final JComboBox< ChartType > chartTypeComboBox = GuiUtils.createComboBox( ChartType.values(), Settings.KEY_MULTI_REP_ANAL_CHARTS_CHART_TYPE );
			chartTypeComboBox.setToolTipText( Language.getText( "charts.chartTypeToolTip" ) ); 
			chartTypeComboBox.addActionListener( chartCanvasRepaintListener );
			chartOptionsBox.add( chartTypeComboBox );
			chartOptionsBox.add( Box.createHorizontalStrut( 5 ) );
			chartOptionsBox.add( new JLabel( Language.getText( "module.multiRepAnal.tab.player.tab.charts.granularity" ) ) );
			final JComboBox< ChartGranularity > chartGranularityComboBox = GuiUtils.createComboBox( ChartGranularity.values(), Settings.KEY_MULTI_REP_ANAL_CHARTS_CHART_GRANULARITY );
			final Holder< ChartData > chartDataHolder = new Holder< ChartData >();
			final ActionListener chartDataCreatorListener = new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					final ChartGranularity chartGranularity = (ChartGranularity) chartGranularityComboBox.getSelectedItem();
					if ( chartDataHolder.value == null || chartDataHolder.value.chartGranularity != chartGranularity ) {
						chartDataHolder.value = new ChartData( ps, chartGranularity );
					}
				}
			};
			chartGranularityComboBox.addActionListener( chartDataCreatorListener );
			chartGranularityComboBox.addActionListener( chartCanvasRepaintListener );
			chartOptionsBox.add( chartGranularityComboBox );
			chartOptionsBox.add( Box.createHorizontalStrut( 5 ) );
			chartOptionsBox.add( new JLabel( Language.getText( "charts.graphApproximation" ) ) );
			final JComboBox< GraphApproximation > graphApproximationComboBox  = GuiUtils.createComboBox( GraphApproximation.values(), Settings.KEY_MULTI_REP_ANAL_CHARTS_GRAPH_APPROXIMATION );
			graphApproximationComboBox.addActionListener( chartCanvasRepaintListener );
			chartOptionsBox.add( graphApproximationComboBox );
			chartDataCreatorListener.actionPerformed( null );
			chartOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBoxes
			chartsPanel.add( chartOptionsBox, BorderLayout.NORTH );
			final JComponent chartCanvas = chartCanvasHolder.value = new JComponent() {
				protected void paintComponent( final Graphics graphics ) {
					// Create the chart params
					final ChartParams params = new ChartParams();
					params.chartCanvas        = chartCanvasHolder.value;
					params.g2                 = (Graphics2D) graphics;
					params.playerStatistics   = ps;
					params.segmentStats       = chartDataHolder.value.segmentStats;
					params.chartType          = (ChartType) chartTypeComboBox.getSelectedItem();
					params.chartGranularity   = (ChartGranularity  ) chartGranularityComboBox  .getSelectedItem();
					params.graphApproximation = (GraphApproximation) graphApproximationComboBox.getSelectedItem();
					params.width              = getWidth();
					params.height             = getHeight();
					
					new ChartPainter( params ).paintChart();
				}
			};
			chartCanvas.setFocusable( true );
			chartsPanel.add( chartCanvas, BorderLayout.CENTER );
			// Register hotkeys for chart types
			Object actionKey = new Object();
			for ( final ChartType chartType : ChartType.values() ) {
				chartsPanel.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( chartType.keyStroke, actionKey = new Object() );
				chartsPanel.getActionMap().put( actionKey, new AbstractAction() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						chartTypeComboBox.setSelectedItem( chartType );
					}
				} );
			}
			GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.player.tab.charts.title" ), Icons.CHART_UP_COLOR, false, tabbedPane, chartsPanel, null );
		}
		
		// Activity trend charts
		final JPanel trendsPanel = new JPanel( new BorderLayout() );
		trendsPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
    			final Holder< JComponent > chartCanvasHolder = new Holder< JComponent >();
    			final ActionListener chartCanvasRepaintListener = new ActionListener() {
    				@Override
    				public void actionPerformed( final ActionEvent event ) {
    					chartCanvasHolder.value.repaint();
    				}
    			};
    			final Box chartOptionsBox = Box.createHorizontalBox();
    			chartOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
    			chartOptionsBox.add( new JLabel( Language.getText( "module.multiRepAnal.tab.player.tab.trends.type" ) ) );
    			final JComboBox< TrendType > trendTypeComboBox = GuiUtils.createComboBox( TrendType.values(), Settings.KEY_MULTI_REP_ANAL_TRENDS_TREND_TYPE );
    			trendTypeComboBox.setToolTipText( Language.getText( "charts.chartTypeToolTip" ) ); 
    			trendTypeComboBox.addActionListener( chartCanvasRepaintListener );
    			chartOptionsBox.add( trendTypeComboBox );
    			chartOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
    			trendsPanel.add( chartOptionsBox, BorderLayout.NORTH );
    			final JComponent chartCanvas = chartCanvasHolder.value = new JComponent() {
    				protected void paintComponent( final Graphics graphics ) {
    					// Create the trend params
    					final TrendParams params = new TrendParams();
    					params.chartCanvas        = chartCanvasHolder.value;
    					params.g2                 = (Graphics2D) graphics;
    					params.trendType          = (TrendType) trendTypeComboBox.getSelectedItem();
    					switch ( params.trendType ) {
    					case HOURLY  : params.activityData = hourlyActivities ; break;
    					case DAILY   : params.activityData = dailyActivities  ; break;
    					case MONTHLY : params.activityData = monthlyActivities; break;
    					default      : throw new RuntimeException( "Insert proper activity data here!" );
    					}
    					params.width              = getWidth();
    					params.height             = getHeight();
    					
    					new TrendPainter( params ).paintChart();
    				}
    			};
    			trendsPanel.add( chartCanvas, BorderLayout.CENTER );
    			// Register hotkeys for chart types
    			Object actionKey = new Object();
    			for ( final TrendType trendType : TrendType.values() ) {
    				trendsPanel.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( trendType.keyStroke, actionKey = new Object() );
    				trendsPanel.getActionMap().put( actionKey, new AbstractAction() {
    					@Override
    					public void actionPerformed( final ActionEvent event ) {
    						trendTypeComboBox.setSelectedItem( trendType );
    					}
    				} );
    			}
			}
		} );
		
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.player.tab.trends.title" ), Icons.CHART, false, tabbedPane, trendsPanel, null );
		
		// Playmates tab
		final JPanel playmatesPanel = new JPanel( new BorderLayout() );
		playmatesPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( playmateStatisticsMap.size() );
        		for ( final Entry< String, PlaymateStatistics > entry : playmateStatisticsMap.entrySet() ) {
        			final PlaymateStatistics vps = entry.getValue();
        			final Vector< Object > row = new Vector< Object >( 11 );
        			row.add( entry.getKey() );
        			row.add( vps.recordAsAllies.totalGames + vps.recordAsOpponents.totalGames );
        			row.add( vps.recordAsAllies.totalGames );
        			row.add( vps.recordAsOpponents.totalGames );
        			row.add( vps.recordAsAllies );
        			row.add( vps.recordAsAllies.getWinRatio() );
        			row.add( vps.recordAsOpponents );
        			row.add( vps.recordAsOpponents.getWinRatio() );
        			row.add( vps.getFormattedTotalTimeInGames() );
        			row.add( Language.formatDate( vps.firstDate ) );
        			row.add( Language.formatDate( vps.lastDate ) );
        			dataVector.add( row );
        		}
        		final Holder< JTable > tableHolder = new Holder< JTable >();
        		createStatisticsTableTab( playmatesPanel, "module.multiRepAnal.tab.players.info", new Object[] { playmateStatisticsMap.size() }, 0, new int[] { 1, 0 }, dataVector, PLAYMATES_HEADER_NAME_VECTOR, new WordCloudTableInput( Language.getText( "module.multiRepAnal.tab.player.tab.playmates.title" ) + " - " + ps.playerDisplayName, 0, 1 ), tableHolder, stretchToWindowActionListenerList );
        		tableHolder.value.addMouseListener( playersTableMouseListener );
        		registerEnterToOpenPlayer( tableHolder.value );
			}
		} );
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.player.tab.playmates.title" ), Icons.GROUP_LINK, false, tabbedPane, playmatesPanel, null );
		
		// Game type records tab
		final JPanel gameTypeRecordsPanel = new JPanel( new BorderLayout() );
		gameTypeRecordsPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( typePlayerStatsMap.size() );
        		for ( final Entry< GameType, PlayerStatistics > entry : typePlayerStatsMap.entrySet() )
        			dataVector.add( createGeneralPlayerStatTableRow( entry.getValue(), entry.getKey() ) );
        		createStatisticsTableTab( gameTypeRecordsPanel, null, null, 0, new int[] { 1, 0 }, dataVector, TYPE_RECORDS_HEADER_NAME_VECTOR, new WordCloudTableInput( Language.getText( "module.multiRepAnal.tab.player.tab.typeRecords.title" ) + " - " + ps.playerDisplayName, 0, 1 ), null, stretchToWindowActionListenerList );
			}
		} );
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.player.tab.typeRecords.title" ), null, false, tabbedPane, gameTypeRecordsPanel, null );
		
		// Format records tab
		final JPanel formatRecordsPanel = new JPanel( new BorderLayout() );
		formatRecordsPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( formatPlayerStatsMap.size() );
        		for ( final Entry< Format, PlayerStatistics > entry : formatPlayerStatsMap.entrySet() )
        			dataVector.add( createGeneralPlayerStatTableRow( entry.getValue(), entry.getKey() ) );
        		createStatisticsTableTab( formatRecordsPanel, null, null, 0, new int[] { 1, 0 }, dataVector, FORMAT_RECORDS_HEADER_NAME_VECTOR, new WordCloudTableInput( Language.getText( "module.multiRepAnal.tab.player.tab.formatRecords.title" ) + " - " + ps.playerDisplayName, 0, 1 ), null, stretchToWindowActionListenerList );
			}
		} );
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.player.tab.formatRecords.title" ), Icons.FORMAT, false, tabbedPane, formatRecordsPanel, null );
		
		// League match-ups records tab
		final JTabbedPane leagueMatchupsTabbedPane = new JTabbedPane();
		leagueMatchupsTabbedPane.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		for ( final Format format : EnumCache.FORMATS ) {
        			final Map< String, PlayerStatistics > leagueMatchupPlayerStatsMap = formatLeagueMatchupPlayerStatsMapMap.get( format );
        			if ( leagueMatchupPlayerStatsMap == null )
        				continue;
        			final JPanel leagueMatchupRecordsPanel = new JPanel( new BorderLayout() );
        			leagueMatchupRecordsPanel.addComponentListener( new FirstShownListener() {
        				@Override
        				public void firstShown( final ComponentEvent event ) {
                			final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( leagueMatchupPlayerStatsMap.size() );
                			// League match-up records
                			for ( final Entry< String, PlayerStatistics > entry : leagueMatchupPlayerStatsMap.entrySet() ) {
                				final PlayerStatistics vps = entry.getValue();
                				final Vector< Object > row = new Vector< Object >( 13 );
                				row.add( entry.getKey() );
                				row.add( vps.record.totalGames );
                				row.add( vps.getAvgApm() );
                				row.add( vps.getAvgEapm() );
                				row.add( vps.getAvgApmRedundancy() );
                				row.add( vps.record );
                				row.add( vps.record.getWinRatio() );
                				row.add( vps.getRaceDistributionString() );
                				row.add( vps.getFormattedTotalTimeInGames() );
                				row.add( ReplayUtils.formatMs( vps.getAvgGameLength() * 1000, GameSpeed.NORMAL ) ); // Passing GameSpeed.NORMAL because it has already been converted
                				row.add( vps.getPresence() );
                				row.add( vps.getAvgGamesPerDay() );
                				row.add( Language.formatDate( vps.firstDate ) );
                				row.add( Language.formatDate( vps.lastDate ) );
                				dataVector.add( row );
                			}
                			createStatisticsTableTab( leagueMatchupRecordsPanel, null, null, 0, new int[] { 1, 0 }, dataVector, LEAGUE_MATCHUP_RECORDS_HEADER_NAME_VECTOR, new WordCloudTableInput( Language.getText( "module.multiRepAnal.tab.player.tab.leagueMatchupRecords.title" ) + " - " + format.stringValue + " - " + ps.playerDisplayName, 0, 1 ), null, stretchToWindowActionListenerList );
        				}
        			} );
        			GuiUtils.addNewTab( format.stringValue, null, false, leagueMatchupsTabbedPane, leagueMatchupRecordsPanel, false, null );
        		}
			}
		} );
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.player.tab.leagueMatchupRecords.title" ), Icons.SC2LEAGUE, false, tabbedPane, leagueMatchupsTabbedPane, null );
		
		// Match-up records tab
		final JTabbedPane matchupsTabbedPane = new JTabbedPane();
		matchupsTabbedPane.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		for ( final Format format : EnumCache.FORMATS ) {
        			final Map< String, PlayerStatistics > matchupPlayerStatsMap = formatMatchupPlayerStatsMapMap.get( format );
        			if ( matchupPlayerStatsMap == null )
        				continue;
        			final JPanel matchupRecordsPanel = new JPanel( new BorderLayout() );
        			matchupRecordsPanel.addComponentListener( new FirstShownListener() {
        				@Override
        				public void firstShown( final ComponentEvent event ) {
                			final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( matchupPlayerStatsMap.size() );
                			// Match-up records
                			for ( final Entry< String, PlayerStatistics > entry : matchupPlayerStatsMap.entrySet() ) {
                				final PlayerStatistics vps = entry.getValue();
                				final Vector< Object > row = new Vector< Object >( 13 );
                				row.add( entry.getKey() );
                				row.add( vps.record.totalGames );
                				row.add( vps.getAvgApm() );
                				row.add( vps.getAvgEapm() );
                				row.add( vps.getAvgApmRedundancy() );
                				row.add( vps.record );
                				row.add( vps.record.getWinRatio() );
                				row.add( vps.getFormattedTotalTimeInGames() );
                				row.add( ReplayUtils.formatMs( vps.getAvgGameLength() * 1000, GameSpeed.NORMAL ) ); // Passing GameSpeed.NORMAL because it has already been converted
                				row.add( vps.getPresence() );
                				row.add( vps.getAvgGamesPerDay() );
                				row.add( Language.formatDate( vps.firstDate ) );
                				row.add( Language.formatDate( vps.lastDate ) );
                				dataVector.add( row );
                			}
                			createStatisticsTableTab( matchupRecordsPanel, null, null, 0, new int[] { 1, 0 }, dataVector, MATCHUP_RECORDS_HEADER_NAME_VECTOR, new WordCloudTableInput( Language.getText( "module.multiRepAnal.tab.player.tab.matchupRecords.title" ) + " - " + format.stringValue + " - " + ps.playerDisplayName, 0, 1 ), null, stretchToWindowActionListenerList );
        				}
        			} );
        			GuiUtils.addNewTab( format.stringValue, null, false, matchupsTabbedPane, matchupRecordsPanel, false, null );
        		}
			}
		} );
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.player.tab.matchupRecords.title" ), Icons.RACE_ANY, false, tabbedPane, matchupsTabbedPane, null );
		
		// Match-up records by maps tab
		final JPanel matchupByMapsRecordsPanel = new JPanel( new BorderLayout() );
		matchupByMapsRecordsPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( matchupByMapsPlayerStatsMap.size() );
        		for ( final Entry< Pair< String, String >, PlayerStatistics > entry : matchupByMapsPlayerStatsMap.entrySet() ) {
        			final PlayerStatistics vps = entry.getValue();
        			final Vector< Object > row = new Vector< Object >( 14 );
        			row.add( entry.getKey().value1 );
        			row.add( entry.getKey().value2 );
        			row.add( vps.record.totalGames );
        			row.add( vps.getAvgApm() );
        			row.add( vps.getAvgEapm() );
        			row.add( vps.getAvgApmRedundancy() );
        			row.add( vps.record );
        			row.add( vps.record.getWinRatio() );
        			row.add( vps.getFormattedTotalTimeInGames() );
        			row.add( ReplayUtils.formatMs( vps.getAvgGameLength() * 1000, GameSpeed.NORMAL ) ); // Passing GameSpeed.NORMAL because it has already been converted
        			row.add( vps.getPresence() );
        			row.add( vps.getAvgGamesPerDay() );
        			row.add( Language.formatDate( vps.firstDate ) );
        			row.add( Language.formatDate( vps.lastDate ) );
        			dataVector.add( row );
        		}
        		createStatisticsTableTab( matchupByMapsRecordsPanel, null, null, 0, new int[] { 2, 1, 0 }, dataVector, MATCHUP_BY_MAPS_RECORDS_HEADER_NAME_VECTOR, null, null, stretchToWindowActionListenerList );
			}
		} );
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.player.tab.matchupByMapsRecords.title" ), Icons.RACE_ANY, false, tabbedPane, matchupByMapsRecordsPanel, null );
		
		// Game length records tab
		final JPanel gameLengthRecordsPanel = new JPanel( new BorderLayout() );
		gameLengthRecordsPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( gameLengthPlayerStatsMap.size() );
        		for ( final Entry< Integer, PlayerStatistics > entry : gameLengthPlayerStatsMap.entrySet() ) {
        			final int gameLengthIntervalIndex = entry.getKey();
        			final IntHolder interval = new IntHolder( gameLengthIntervalIndex ) {
        				final String intervalString = Language.getText( "module.multiRepAnal.tab.player.tab.gameLengthRecords.gameLengthIntervalMin", ( gameLengthIntervalIndex * gameLengthRecordsGranularityMin ) + "-" + ( (gameLengthIntervalIndex+1) * gameLengthRecordsGranularityMin ) );
        				@Override public String toString() { return intervalString; }
        			};
        			dataVector.add( createGeneralPlayerStatTableRow( entry.getValue(), interval ) );
        		}
        		createStatisticsTableTab( gameLengthRecordsPanel, null, null, 0, new int[] { 0 }, dataVector, GAME_LENGTH_RECORDS_HEADER_NAME_VECTOR, new WordCloudTableInput( Language.getText( "module.multiRepAnal.tab.player.tab.gameLengthRecords.title" ) + " - " + ps.playerDisplayName, 0, 1 ), null, stretchToWindowActionListenerList );
			}
		} );
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.player.tab.gameLengthRecords.title" ), Icons.CLOCK, false, tabbedPane, gameLengthRecordsPanel, null );
		
		// Day records tab
		final JPanel dayRecordsPanel = new JPanel( new BorderLayout() );
		dayRecordsPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( dayPlayerStats.length );
        		for ( int i = 0; i < dayPlayerStats.length; i++ ) {
        			if ( dayPlayerStats[ i ] == null )
        				continue;
        			final int dayIndex = i;
        			final IntHolder day = new IntHolder( dayIndex ) {
        				@Override public String toString() { return TrendType.DAILY.labels[ dayIndex ]; }
        			};
        			dataVector.add( createGeneralPlayerStatTableRow( dayPlayerStats[ dayIndex ], day ) );
        		}
        		createStatisticsTableTab( dayRecordsPanel, null, null, 0, new int[] { 0 }, dataVector, DAY_RECORDS_HEADER_NAME_VECTOR, new WordCloudTableInput( Language.getText( "module.multiRepAnal.tab.player.tab.dayRecords.title" ) + " - " + ps.playerDisplayName, 0, 1 ), null, stretchToWindowActionListenerList );
			}
		} );
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.player.tab.dayRecords.title" ), Icons.CALENDAR_SELECT_WEEK, false, tabbedPane, dayRecordsPanel, null );
		
		// Hour records tab
		final JPanel hourRecordsPanel = new JPanel( new BorderLayout() );
		hourRecordsPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
    			final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( hourPlayerStats.length );
    			for ( int i = 0; i < hourPlayerStats.length; i++ )
    				if ( hourPlayerStats[ i ] != null )
    					dataVector.add( createGeneralPlayerStatTableRow( hourPlayerStats[ i ], i < 10 ? "0" + i : Integer.toString( i ) ) );
    			createStatisticsTableTab( hourRecordsPanel, null, null, 0, new int[] { 0 }, dataVector, HOUR_RECORDS_HEADER_NAME_VECTOR, new WordCloudTableInput( Language.getText( "module.multiRepAnal.tab.player.tab.hourRecords.title" ) + " - " + ps.playerDisplayName, 0, 1 ), null, stretchToWindowActionListenerList );
    		}
    	} );
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.player.tab.hourRecords.title" ), Icons.CLOCK_MOON_PHASE, false, tabbedPane, hourRecordsPanel, null );
		
		// Maps tab
		final JPanel mapsPanel = new JPanel( new BorderLayout() );
		mapsPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( mapStatisticsMap.size() );
        		final NullAwareComparable< Integer > nullWinRatio = NullAwareComparable.getPercent( null );
        		for ( final MapStatistics ms : mapStatisticsMap.values() ) {
        			final Record pRecord = ms.raceRecordMap.get( Race.PROTOSS );
        			final Record tRecord = ms.raceRecordMap.get( Race.TERRAN  );
        			final Record zRecord = ms.raceRecordMap.get( Race.ZERG    );
        			final Vector< Object > row = new Vector< Object >( 11 );
        			row.add( ms.name );
        			row.add( ms.record.totalGames );
        			row.add( NullAwareComparable.getPercent( ms.record.totalGames * 100 / ps.playerGameParticipationStatsList.size() ) );
        			row.add( ms.record );
        			row.add( ms.record.getWinRatio() );
        			row.add( pRecord == null ? nullWinRatio : pRecord.getWinRatio() );
        			row.add( tRecord == null ? nullWinRatio : tRecord.getWinRatio() );
        			row.add( zRecord == null ? nullWinRatio : zRecord.getWinRatio() );
        			row.add( ReplayUtils.formatMs( ms.getAvgGameLength() * 1000, GameSpeed.NORMAL ) ); // Passing GameSpeed.NORMAL because it has already been converted
        			row.add( Language.formatDate( ms.firstDate ) );
        			row.add( Language.formatDate( ms.lastDate ) );
        			dataVector.add( row );
        		}
        		createStatisticsTableTab( mapsPanel, "module.multiRepAnal.tab.maps.info", new Object[] { mapStatisticsMap.size() }, 0, new int[] { 1, 0 }, dataVector, PLAYER_MAPS_HEADER_NAME_VECTOR, new WordCloudTableInput( Language.getText( "module.multiRepAnal.tab.maps.title" ) + " - " + ps.playerDisplayName, 0, 1 ), null, stretchToWindowActionListenerList );
    		}
    	} );
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.maps.title" ), Icons.MAPS_STACK, false, tabbedPane, mapsPanel, null );
		
		// Build orders tab
		final JTabbedPane buildOrdersTabbedPane = new JTabbedPane();
		buildOrdersTabbedPane.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		for ( final Format format : EnumCache.FORMATS ) {
        			final Map< String, BuildOrderStatistics > buildOrderStatisticsMap = formatBuildOrderStatisticsMap.get( format );
        			if ( buildOrderStatisticsMap == null )
        				continue;
        			final JPanel buildOrdersPanel = new JPanel( new BorderLayout() );
        			buildOrdersPanel.addComponentListener( new FirstShownListener() {
        				@Override
        				public void firstShown( final ComponentEvent event ) {
                			final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( buildOrderStatisticsMap.size() );
                			
                			for ( final BuildOrderStatistics bs : buildOrderStatisticsMap.values() ) {
                				if ( format == Format.ONE_VS_ONE ) {
                					final Record recordVsP = bs.recordVsRaceMap.get( Race.PROTOSS );
                					final Record recordVsT = bs.recordVsRaceMap.get( Race.TERRAN  );
                					final Record recordVsZ = bs.recordVsRaceMap.get( Race.ZERG    );
                					final Vector< Object > row = new Vector< Object >( 10 );
                					row.add( bs.race );
                					row.add( bs.buildOrder );
                					row.add( bs.record.totalGames );
                					row.add( bs.record );
                					row.add( bs.record.getWinRatio() );
                					row.add( recordVsP == null ? new Record() : recordVsP );
                					row.add( recordVsT == null ? new Record() : recordVsT );
                					row.add( recordVsZ == null ? new Record() : recordVsZ );
                					row.add( Language.formatDate( bs.firstDate ) );
                					row.add( Language.formatDate( bs.lastDate ) );
                					dataVector.add( row );
                				}
                				else {
                					final Vector< Object > row = new Vector< Object >( 7 );
                					row.add( bs.race );
                					row.add( bs.buildOrder );
                					row.add( bs.record.totalGames );
                					row.add( bs.record );
                					row.add( bs.record.getWinRatio() );
                					row.add( Language.formatDate( bs.firstDate ) );
                					row.add( Language.formatDate( bs.lastDate ) );
                					dataVector.add( row );
                				}
                			}
                			
                			createStatisticsTableTab( buildOrdersPanel, "module.multiRepAnal.tab.buildOrders.info", new Object[] { buildOrderStatisticsMap.size() }, 1, new int[] { 2, 0, 4 }, dataVector, format == Format.ONE_VS_ONE ? BUILD_ORDERS_1V1_HEADER_NAME_VECTOR : BUILD_ORDERS_NON_1V1_HEADER_NAME_VECTOR, null, null, null );
        	    		}
        	    	} );
        			GuiUtils.addNewTab( format.stringValue, null, false, buildOrdersTabbedPane, buildOrdersPanel, false, null );
        		}
    		}
    	} );
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.buildOrders.title" ), Icons.BLOCK, false, tabbedPane, buildOrdersTabbedPane, null );
		
		// Gaming Sessions records tab
		final JPanel gamingSessionsRecordsPanel = new JPanel( new BorderLayout() );
		gamingSessionsRecordsPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		final int[] sessionsCountByLength = new int[ gameInSessionStatsList.size() ];
        		int sumSessions = 0;
				for ( int i = gameInSessionStatsList.size() - 1; i >= 0; i-- ) {
					sessionsCountByLength[ i ] = gameInSessionStatsList.get( i ).record.totalGames - sumSessions;
					sumSessions += sessionsCountByLength[ i ];
				}
        		
        		final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( gameInSessionStatsList.size() );
        		int gameInSession = 1;
        		for ( final PlayerStatistics ps : gameInSessionStatsList ) {
        			final Vector< Object > generalPlayerStatTableRow = createGeneralPlayerStatTableRow( ps, gameInSession );
        			final Vector< Object > sessionTableRow           = new Vector< Object >( generalPlayerStatTableRow.size() + 1 );
        			sessionTableRow.addAll( generalPlayerStatTableRow );
        			sessionTableRow.insertElementAt( sessionsCountByLength[ gameInSession - 1 ], 1 );
        			dataVector.add( sessionTableRow );
        			gameInSession++;
        		}
        		createStatisticsTableTab( gamingSessionsRecordsPanel, null, null, 0, new int[] { 0 }, dataVector, GAMING_SESSIONS_HEADER_NAME_VECTOR, null, null, stretchToWindowActionListenerList );
			}
		} );
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.player.tab.gamingSessions.title" ), Icons.CHAIN, false, tabbedPane, gamingSessionsRecordsPanel, null );
		
		// Chat words tab
		final JPanel chatWordsPanel = new JPanel( new BorderLayout() );
		chatWordsPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
        		final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >( chatWordsStatisticsMap.size() );
        		for ( final WordStatistics ws : chatWordsStatisticsMap.values() ) {
        			final Vector< Object > row = new Vector< Object >( 6 );
        			row.add( ws.word );
        			row.add( ws.count );
        			row.add( ws.replays );
        			row.add( NullAwareComparable.getPercent( ws.replays * 100 / ps.playerGameParticipationStatsList.size() ) );
        			row.add( Language.formatDate( ws.firstDate ) );
        			row.add( Language.formatDate( ws.lastDate ) );
        			dataVector.add( row );
        		}
        		createStatisticsTableTab( chatWordsPanel, "module.multiRepAnal.tab.chatWords.info", new Object[] { chatWordsStatisticsMap.size() }, 0, new int[] { 1, 2, 0 }, dataVector, CHAT_WORDS_HEADER_NAME_VECTOR, new WordCloudTableInput( Language.getText( "module.multiRepAnal.tab.chatWords.title" ) + " - " + ps.playerDisplayName, 0, 1 ), null, stretchToWindowActionListenerList );
    		}
    	} );
		GuiUtils.addNewTab( Language.getText( "module.multiRepAnal.tab.chatWords.title" ), Icons.BALLOONS, false, tabbedPane, chatWordsPanel, null );
		
		panel.add( tabbedPane, BorderLayout.CENTER );
		
		return panel;
	}
	
	/**
	 * Creates a general player stats table row. 
	 * @param ps        player statistics to create a row from
	 * @param mainValue main value to add as the first value
	 * @return the created general player stats table row
	 */
	private static Vector< Object > createGeneralPlayerStatTableRow( final PlayerStatistics ps, final Object mainValue ) {
		final Vector< Object > row = new Vector< Object >( 14 );
		
		row.add( mainValue );
		row.add( ps.record.totalGames );
		row.add( ps.getAvgApm() );
		row.add( ps.getAvgEapm() );
		row.add( ps.getAvgApmRedundancy() );
		row.add( ps.record );
		row.add( ps.record.getWinRatio() );
		row.add( ps.getRaceDistributionString() );
		row.add( ps.getFormattedTotalTimeInGames() );
		row.add( ReplayUtils.formatMs( ps.getAvgGameLength() * 1000, GameSpeed.NORMAL ) ); // Passing GameSpeed.NORMAL because it has already been converted
		row.add( ps.getPresence() );
		row.add( ps.getAvgGamesPerDay() );
		row.add( Language.formatDate( ps.firstDate ) );
		row.add( Language.formatDate( ps.lastDate ) );
		
		return row;
	}
	
	/**
	 * Creates a statistics table tab.
	 * @param panel               panel to build the tab on
	 * @param infoTextKey         optional key of the info text on the top of the tab
	 * @param infoTextArguments   arguments of the info text
	 * @param nameColumn          name column (this will be sorted ascending by default)
	 * @param defaultSortColumns  default sort columns
	 * @param dataVector          data vector of the table
	 * @param headerNameVector    header names of the table
	 * @param wordCloudTableInput optional word cloud table input
	 * @param tableHolder         an optional holder, if provided, a reference to the table will be set to this
	 * @param stretchToWindowActionListenerList optional list, if provided, the table's action listener to stretch to window will be added to this
	 */
	private void createStatisticsTableTab( final JPanel panel, final String infoTextKey, final Object[] infoTextArguments, final int nameColumn, final int[] defaultSortColumns, final Vector< Vector< Object > > dataVector, final Vector< String > headerNameVector, final WordCloudTableInput wordCloudTableInput, final Holder< JTable > tableHolder, final List< ActionListener > stretchToWindowActionListenerList ) {
		if ( infoTextKey != null )
			panel.add( new JLabel( Language.getText( infoTextKey, infoTextArguments ) ), BorderLayout.NORTH );
		
		final JTable table = GuiUtils.createNonEditableTable();
		
		// To have a proper sorting, we need a table model which returns proper classes for columns
		final DefaultTableModel model = new DefaultTableModel() {
			@Override
			public Class<?> getColumnClass( final int columnIndex ) {
				int maxTries = getRowCount();
				if ( maxTries == 0 )
					return super.getColumnClass( columnIndex );
				else {
					if ( maxTries > 10 )
						maxTries = 10;
					for ( int i = 0; i < maxTries; i++ ) {
						final Object value = getValueAt( i, columnIndex );
						if ( value != null )
							return value.getClass();
					}
					return Object.class;
				}
			}
		};
		model.setDataVector( dataVector, headerNameVector );
		table.setModel( model );
		// I want default descending sorting in all columns but the name column
		table.setRowSorter( new TableRowSorter< TableModel >( model ) {
			{
				// By default sort by the 2nd column (replays)
				final List< SortKey > sortKeys = new ArrayList< SortKey >();
				for ( final int column : defaultSortColumns )
					sortKeys.add( new SortKey( column, column == nameColumn ? SortOrder.ASCENDING : SortOrder.DESCENDING ) );
				setSortKeys( sortKeys );
				setMaxSortKeys( 3 );
			}
			@Override
			public void toggleSortOrder( int column ) {
				if ( column != nameColumn ) {
					final List< SortKey > sortKeys = new ArrayList< SortKey >( getSortKeys() );
					if ( sortKeys.isEmpty() || sortKeys.get( 0 ).getColumn() != column ) {
						sortKeys.add( 0, new SortKey( column, SortOrder.DESCENDING ) );
						if ( sortKeys.size() > getMaxSortKeys() )
							sortKeys.remove( getMaxSortKeys() );
						setSortKeys( sortKeys );
						return;
					}
				}
				super.toggleSortOrder( column );
			};
		} );
		
		final TableBox tableBox = new TableBox( table, panel, wordCloudTableInput );
		panel.add( tableBox, BorderLayout.CENTER );
		
		final ActionListener stretchToWindowActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				table.setAutoResizeMode( stretchToWindowCheckBox.isSelected() ? JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF );
			}
		};
		stretchToWindowActionListener.actionPerformed( null );
		stretchToWindowCheckBox.addActionListener( stretchToWindowActionListener );
		if ( stretchToWindowActionListenerList != null )
			stretchToWindowActionListenerList.add( stretchToWindowActionListener );
		
		if ( tableHolder != null )
			tableHolder.value = table;
		
		// We have to invoke this later, because table might be being updated right now...
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				GuiUtils.packTable( table );
			}
		} );
	}
	
}
