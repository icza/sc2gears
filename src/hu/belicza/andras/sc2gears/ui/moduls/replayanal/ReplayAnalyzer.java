/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.replayanal;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2map.MapParser;
import hu.belicza.andras.sc2gears.sc2replay.EnumCache;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.BaseUseAbilityAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.BuildAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.HotkeyAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.MoveScreenAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.RequestResoucesAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.ResearchAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.SelectAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.SendResourcesAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.TrainAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.UpgradeAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.UseBuildingAbilityAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.UseUnitAbilityAction;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.services.Downloader;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.charts.ChartUtils.GraphApproximation;
import hu.belicza.andras.sc2gears.ui.components.FirstShownListener;
import hu.belicza.andras.sc2gears.ui.components.PlayerPopupMenu;
import hu.belicza.andras.sc2gears.ui.components.ReplayOperationsPopupMenu;
import hu.belicza.andras.sc2gears.ui.components.StatusLabel;
import hu.belicza.andras.sc2gears.ui.dialogs.GridSettingsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog.SettingsTab;
import hu.belicza.andras.sc2gears.ui.dialogs.OverlayChartsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.PlayerProfileDialog;
import hu.belicza.andras.sc2gears.ui.icons.IconHandler;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.ModuleFrame;
import hu.belicza.andras.sc2gears.util.BoolHolder;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.GoogleTranslator;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gears.util.HttpPost;
import hu.belicza.andras.sc2gears.util.NormalThread;
import hu.belicza.andras.sc2gears.util.ProfileCache;
import hu.belicza.andras.sc2gears.util.Task;
import hu.belicza.andras.sc2gears.util.TextFilter;
import hu.belicza.andras.sc2gearsdbapi.InfoServletApi;
import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;
import hu.belicza.andras.sc2gearspluginapi.api.enums.LadderSeason;
import hu.belicza.andras.sc2gearspluginapi.api.enums.League;
import hu.belicza.andras.sc2gearspluginapi.api.listener.CustomPortraitListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.DownloaderCallback;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ProfileListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayOpCallback;
import hu.belicza.andras.sc2gearspluginapi.api.profile.IProfile;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Gateway;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerColor;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.importing.ReplaySpecification;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The Replay Analyzer.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class ReplayAnalyzer extends ModuleFrame {
	
	/**
	 * Show profile info.
	 * @author Andras Belicza
	 */
	public static enum ShowProfileInfo {
		PORTRAIT_AND_LEAGUES( "module.repAnalyzer.tab.charts.showProfileInfo.portraitAndLeagues" ),
		LEAGUES_ONLY        ( "module.repAnalyzer.tab.charts.showProfileInfo.leaguesOnly"        ),
		NONE                ( "module.repAnalyzer.tab.charts.showProfileInfo.none"               );
		
		/** Cache of the string value. */
		public final String stringValue;
		
		/**
		 * Creates a new ShowProfileInfo.
		 * @param textKey key of the text representation
		 */
		private ShowProfileInfo( final String textKey ) {
			stringValue = Language.getText( textKey );
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
	public static enum ChartType {
		APM                        ( "module.repAnalyzer.tab.charts.chartType.apm"                      , true  ),
		HOTKEYS                    ( "module.repAnalyzer.tab.charts.chartType.hotkeys"                  , true  ),
		BUILDS_TECH                ( "module.repAnalyzer.tab.charts.chartType.buildsTech"               , true  ),
		BUILDS_TECH_STAT           ( "module.repAnalyzer.tab.charts.chartType.buildsTechStat"           , true  ),
		MAIN_BUILDING_CONTROL      ( "module.repAnalyzer.tab.charts.chartType.mainBuildingControl"      , false ),
		MAP_VIEW                   ( "module.repAnalyzer.tab.charts.chartType.mapView"                  , true  ),
		ACTION_DISTRIBUTION        ( "module.repAnalyzer.tab.charts.chartType.actionDistribution"       , true  ),
		UNIT_TIERS                 ( "module.repAnalyzer.tab.charts.chartType.unitTiers"                , false ),
		RESOURCE_SPENDING_RATE     ( "module.repAnalyzer.tab.charts.chartType.resourceSpendingRate"     , true  ),
		RESOURCES_SPENT            ( "module.repAnalyzer.tab.charts.chartType.resourcesSpent"           , true  ),
		PRODUCED_ARMY_SUPPLY       ( "module.repAnalyzer.tab.charts.chartType.producedArmySupply"       , true  ),
		APM_REDUNDANCY_DISTRIBUTION( "module.repAnalyzer.tab.charts.chartType.apmRedundancyDistribution", true  ),
		ACTION_SEQUENCES           ( "module.repAnalyzer.tab.charts.chartType.actionSequences"          , true  ),
		PRODUCTIONS                ( "module.repAnalyzer.tab.charts.chartType.productions"              , false ),
		PLAYER_SELECTIONS          ( "module.repAnalyzer.tab.charts.chartType.playerSelections"         , false );
		
		/** Cache of the original string value (without HTML formatting). */
		public final String    originalStringValue;
		/** Cache of the string value (with optional HTML formatting).    */
		public final String    stringValue;
		/** Key stroke for fast accessing this chart type.                */
		public final KeyStroke keyStroke;
		/** Tells if this chart type supports all on one chart option.    */
		public final boolean   supportsAllOnOneChart;
		
		/**
		 * Creates a new ChartType.
		 * @param textKey key of the text representation
		 */
		private ChartType( final String textKey, final boolean supportsAllOnOneChart ) {
			final int chartNumber      = ordinal() + 1;
			keyStroke                  = chartNumber > 10 ? null : KeyStroke.getKeyStroke( KeyEvent.VK_0 + ( chartNumber == 10 ? 0 : chartNumber ), InputEvent.CTRL_MASK ); // CTRL+1 for chart type 1, CTRL+2 select chart type 2, ... CTRL+0 to select chart type 10, the rest is null
			this.supportsAllOnOneChart = supportsAllOnOneChart;
			originalStringValue        = Language.getText( textKey );
			if ( keyStroke == null )
				stringValue = originalStringValue;
			else
				stringValue = "<html>" + originalStringValue + "&nbsp;&nbsp;<font color=#777777>Ctrl+" + ( chartNumber == 10 ? 0 : chartNumber ) + "</font></html>";
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Show duration.
	 * @author Andras Belicza
	 */
	public static enum ShowDuration {
		NONE ( "module.repAnalyzer.tab.charts.buildsTech.showDuration.none"  ),
		LINES( "module.repAnalyzer.tab.charts.buildsTech.showDuration.lines" ),
		BARS ( "module.repAnalyzer.tab.charts.buildsTech.showDuration.bars"  );
		
		/** Cache of the string value. */
		public final String stringValue;
		
		/**
		 * Creates a new ShowDuration.
		 * @param textKey key of the text representation
		 */
		private ShowDuration( final String textKey ) {
			stringValue = Language.getText( textKey );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Map background.
	 * @author Andras Belicza
	 */
	public static enum MapBackground {
		MAP_IMAGE                 ( "module.repAnalyzer.tab.charts.mapView.background.mapImage"              , true , false ),
		HOT_POINTS                ( "module.repAnalyzer.tab.charts.mapView.background.hotPoints"             , false, false ),
		MAP_IMAGE_AND_HOT_POINTS  ( "module.repAnalyzer.tab.charts.mapView.background.mapImageAndHotPoints"  , true , false ),
		HOT_AREAS                 ( "module.repAnalyzer.tab.charts.mapView.background.hotAreas"              , false, true  ),
		MAP_IMAGE_AND_HOT_AREAS   ( "module.repAnalyzer.tab.charts.mapView.background.mapImageAndHotAreas"   , true , true  ),
		CAMERA_AREAS              ( "module.repAnalyzer.tab.charts.mapView.background.cameraAreas"           , false, true  ),
		MAP_IMAGE_AND_CAMERA_AREAS( "module.repAnalyzer.tab.charts.mapView.background.mapImageAndCameraAreas", true , true  ),
		BLANK                     ( "module.repAnalyzer.tab.charts.mapView.background.blank"                 , false, false );
		
		/** Cache of the string value.                             */
		public final String  stringValue;
		/** Tells if the map background requires the map image.    */
		public final boolean requiresMapImage;
		/** Tells if the map background involves area aggregation. */
		public final boolean involvesAreaAggregation;
		
		/**
		 * Creates a new MapBackground.
		 * @param textKey key of the text representation
		 */
		private MapBackground( final String textKey, final boolean requiresMapImage, final boolean involvesAreaAggregation ) {
			stringValue                  = Language.getText( textKey );
			this.requiresMapImage        = requiresMapImage;
			this.involvesAreaAggregation = involvesAreaAggregation;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Map view quality.
	 * @author Andras Belicza
	 */
	public static enum MapViewQuality {
		LOW   ( "module.repAnalyzer.tab.charts.mapView.quality.low"   , RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR ),
		MEDIUM( "module.repAnalyzer.tab.charts.mapView.quality.medium", RenderingHints.VALUE_INTERPOLATION_BILINEAR         ),
		HIGH  ( "module.repAnalyzer.tab.charts.mapView.quality.high"  , RenderingHints.VALUE_INTERPOLATION_BICUBIC          );
		
		/** Cache of the string value.                                                                   */
		public final String stringValue;
		/** Hint value of the {@link RenderingHints#KEY_INTERPOLATION} key associated with this quality. */
		public final Object hintValue;
		
		/**
		 * Creates a new MapViewQuality.
		 * @param textKey   key of the text representation
		 * @param hintValue hint value of the {@link RenderingHints#KEY_INTERPOLATION} key associated with this quality
		 */
		private MapViewQuality( final String textKey, final Object hintValue ) {
			stringValue    = Language.getText( textKey );
			this.hintValue = hintValue;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Special symbols.
	 * See <a href="http://text-symbols.com/cool/">http://text-symbols.com/cool/</a> for more examples.
	 */
	private static final String[] SYMBOLS = {
		"\u263a", // Happy
		"\u263b", // Black Happy
		"\u30c4", // Happy #2
		"\u2605", // Black Star
		"\u2606", // Star
		"\u273f", // Black Flower
		"\u2740", // Flower
		"\u2665", // Heart
		"\u2666", // Diamond
		"\u2660", // Spade
		"\u2663", // Club
		"\u2654", // White Chess King
		"\u2655", // White Chess Queen
		"\u2656", // White Chess Rook
		"\u2657", // White Chess Bishop
		"\u2658", // White Chess Knight
		"\u2659", // White Chess Pawn
		"\u2668", // Hot Spring
		"\u2714", // Checked
		"\u2716", // Unchecked
		"\u27a8", // Right Arrow Black
		"\u21d0", // Left arrow
		"\u21d1", // Up arrow
		"\u21d2", // Right arrow
		"\u21d3", // Down arrow
		"\u21d4", // Left-Right arrow
		"\u21d5", // Up-Down arrow
		"\u2260", // Not equal
		"\u262e", // Peace
		"\u262f", // Yin and yang
		"\u266a", // Eighth Note
		"\u266c", // Beamed Sixteenth Notes
		"\u263c", // Sun
		"\u2600", // Black Sun
		"\u2601", // Cloud
		"\u263e", // Last Quarter Moon
		"\u263d", // First Quarter Moon
		"\u2604", // Comet
		"\u2602", // Umbrella
		"\u2603", // Snowman
		"\u2744", // Snowflake
		"\u2704", // Scissors
		"\u2709", // Message
		"\u270d", // Writing Hand
		"\u260e", // Black Telephone
		"\u25ba", // Black Right-pointing Pointer
		"\u25c4", // Black Left-pointing Pointer
		"\u25b2", // Black Up-pointing Pointer
		"\u25bc", // Black Down-pointing Pointer
		"\u06e9", // Arabic place of Sajdah
		"\u2708", // Airplane
		"\u2622", // Radioactive
		"\u2623", // Biohazard
		"\u2620"  // Skull
	};
	
	/** Max map preview zoom value. */
	private static final int MAX_MAP_PREVIEW_ZOOM = 6;
	
	/** Label dictionary for the map preview zoom slider. */
	private static final Dictionary< Integer, JComponent > LABEL_DICTIONARY = new Hashtable< Integer, JComponent >();
	static {
		for ( int i = 0; i <= MAX_MAP_PREVIEW_ZOOM; i++ )
			LABEL_DICTIONARY.put( i, new JLabel( i == 0 ?  "1/2x" : i + "x" ) );
	}
	
	/** Selected player borders of the different leagues. */
	private static final Map< League, Border > LEAGUE_SEL_BORDER_MAP   = new EnumMap<>( League.class );
	/** Unselected player borders of the different leagues. */
	private static final Map< League, Border > LEAGUE_UNSEL_BORDER_MAP = new EnumMap<>( League.class );
	static {
		for ( final League league : EnumCache.LEAGUES ) {
			Color c = null;
			switch ( league ) {
			case GRANDMASTER : c = new Color( 255, 220,  10 ); break;
			case MASTER      : c = new Color(  45, 200, 255 ); break;
			case DIAMOND     : c = new Color(  34, 135, 220 ); break;
			case PLATINUM    : c = new Color( 255, 255, 255 ); break;
			case GOLD        : c = new Color( 165, 120,  44 ); break;
			case SILVER      : c = new Color( 150, 150, 150 ); break;
			case BRONZE      : c = new Color( 100,  64,  37 ); break;
			case UNRANKED    : c = new Color( 220, 255, 220 ); break;
			case UNKNOWN     : c = new Color( 255, 210, 210 ); break;
			case ANY         : continue;
			}
			final Color highlight = c, shadow = c.darker();
			LEAGUE_SEL_BORDER_MAP  .put( league, BorderFactory.createCompoundBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED, highlight, highlight, shadow, shadow ), BorderFactory.createBevelBorder( BevelBorder.LOWERED, highlight, highlight, shadow, shadow ) ) );
			LEAGUE_UNSEL_BORDER_MAP.put( league, BorderFactory.createCompoundBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED , highlight, highlight, shadow, shadow ), BorderFactory.createBevelBorder( BevelBorder.RAISED , highlight, highlight, shadow, shadow ) ) );
		}
	}
	
	/** The analyzed replay file. */
	private File         replayFile;
	/** The parsed replay.        */
	private final Replay replay;
	/** SHA-1 checksum of the replay. */
	private String       replaySha1;
	
	/** Reference to the layered pane. */
	private final JLayeredPane layeredPane = getLayeredPane();
	
	/** Simple counter to generate unique names for Build order analysis. */
	private static final AtomicInteger counter = new AtomicInteger();
	
	/**
	 * Creates a new ReplayAnalyzer.
	 * @param arguments optional arguments to define the replay to open<br>
	 * 		the <b>first</b>  element can be a File to open in the replay analyzer<br>
	 *      the <b>second</b> element can be a replay specification
	 */
	public ReplayAnalyzer( final Object... arguments ) {
		super( arguments.length < 1 ? Language.getText( "module.replayAnalyzer.opening" ) : null ); // This title does not have a role as this internal frame is not displayed until a replay is chosen, and then title is changed anyway
		
		ReplaySpecification replaySpec = null;
		
		if ( arguments.length == 0 ) {
			final JFileChooser fileChooser = new JFileChooser( GeneralUtils.getDefaultReplayFolder() );
			fileChooser.setDialogTitle( Language.getText( "module.repAnalyzer.openTitle" ) );
			fileChooser.setFileFilter( GuiUtils.SC2_REPLAY_FILTER );
			fileChooser.setAccessory( GuiUtils.createReplayFilePreviewAccessory( fileChooser ) );
			fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
			if ( fileChooser.showOpenDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION )
				replayFile = fileChooser.getSelectedFile();
			else {
				dispose();
				replayFile = null;
				replay     = null;
				return;
			}
		}
		else if ( arguments.length == 1 ) {
			replayFile = (File) arguments[ 0 ];
		}
		else if ( arguments.length == 2 ) {
			replaySpec = (ReplaySpecification) arguments[ 1 ];
		}
		
		if ( replayFile != null ) {
			replay = ReplayFactory.parseReplay( replayFile.getAbsolutePath(), ReplayFactory.ALL_CONTENT );
			if ( replay == null ) {
				// We show error in a new thread, because the error dialog blocks the thread,
				// and if auto-open option is enabled and user doesn't close the error dialog,
				// it would block the replay auto-saver thread too (would not save new replays until this dialog is closed)!
				new NormalThread( "Non-blocking executor thread" ) {
					public void run() {
						GuiUtils.showErrorDialog( Language.getText( "module.repAnalyzer.openError" ) );
					}
				}.start();
				dispose();
				return;
			}
			
			if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_REARRANGE_PLAYERS_IN_REP_ANALYZER ) )
				ReplayUtils.applyFavoredPlayerListSetting( replay.details );
			
			MainFrame.INSTANCE.updateRecentReplays( replayFile );
			setTitle( GeneralUtils.getFileNameWithoutExt( replayFile ) );
			setFrameIcon( Icons.SC2 );
		}
		else {
			replay = ReplayFactory.constructReplay( replaySpec );
			setTitle( Language.getText( "module.repAnalyzer.buildOrderAnalysisTitle", counter.incrementAndGet() ) );
			setFrameIcon( Icons.BLOCK );
		}
		
		buildGui();
	}
	
	/**
	 * Builds the GUI of the frame.
	 */
	private void buildGui() {
		final JTabbedPane tabbedPane = new JTabbedPane();
		
		GuiUtils.addNewTab( Language.getText( "module.repAnalyzer.tab.charts.title" ), Icons.CHART, false, tabbedPane, createChartsTab(), null );
		
		GuiUtils.addNewTab( Language.getText( "module.repAnalyzer.tab.gameInfo.title" ), Icons.INFORMATION_BALLOON, false, tabbedPane, createGameInfoTab(), null );
		
		GuiUtils.addNewTab( Language.getText( "module.repAnalyzer.tab.inGameChat.title" ), Icons.BALLOONS, false, tabbedPane, createInGameChatTab(), null );
		
		GuiUtils.addNewTab( Language.getText( "module.repAnalyzer.tab.mapPreview.title" ), Icons.MAP, false, tabbedPane, createMapPreviewTab(), null );
		
		GuiUtils.addNewTab( Language.getText( "module.repAnalyzer.tab.publicComments.title" ), Icons.GLOBE_PENCIL, false, tabbedPane, createPublicCommentsTab(), null );
		
		GuiUtils.addNewTab( Language.getText( "module.repAnalyzer.tab.privateData.title" ), Icons.SERVER_NETWORK, false, tabbedPane, createPrivateDataTab(), null );
		
		getContentPane().add( tabbedPane );
	}
	
	/**
	 * Creates and returns the charts tab.
	 * @return the charts tab
	 */
	private JComponent createChartsTab() {
		final Holder< Runnable > updateChartSpecificComponentsUITask = new Holder< Runnable >();
		
		final JPanel chartsPanel = new JPanel( new BorderLayout() ) {
			@Override
			public void updateUI() {
				super.updateUI();
				// Update components that are not currently added to the chart specific options box
				if ( updateChartSpecificComponentsUITask.value != null )
					updateChartSpecificComponentsUITask.value.run();
			}
		};
		
		// List component to display actions.
		final JList< Action > actionsList = new JList<>();
		// A check box for each action type to specify if action type is displayable.
		final Map< ActionType, JCheckBox > displayActionTypeCheckboxMap = new EnumMap< ActionType, JCheckBox >( ActionType.class );
		
		final JCheckBox useListedActionsAsInputCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.actions.useListedActionsAsInput", Settings.KEY_REP_ANALYZER_CHARTS_ACTIONS_USE_LISTED_AS_INPUT );
		
		final List< OverlayChartFrame > overlayChartFrameList = new ArrayList< OverlayChartFrame >();
		addInternalFrameListener( new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing( final InternalFrameEvent event ) {
				for ( int i = overlayChartFrameList.size() - 1; i >= 0; i-- ) // Downward is a must, because frames will be removed from this list; also we cannot use enhanced for or iterator due to ConcurrentModificationException
					overlayChartFrameList.get( i ).dispose();
			}
		} );
		
		final JScrollPane chartCanvasScrollPane = new JScrollPane( JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		chartCanvasScrollPane.getHorizontalScrollBar().setUnitIncrement( 10 );
		chartCanvasScrollPane.getVerticalScrollBar  ().setUnitIncrement( 10 );
		final Holder< JComponent > chartCanvasHolder = new Holder< JComponent >();
		final ActionListener chartCanvasRepainterListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				chartCanvasHolder.value.repaint();
				for ( final OverlayChartFrame overlayChartFrame : overlayChartFrameList )
					overlayChartFrame.chartCanvas.repaint();
			}
		};
		
		final Holder< Action[] >   displayableActionsHolder           = new Holder< Action[] >();
		final JCheckBox[]          playerCheckBoxes                   = new JCheckBox[ replay.details.players.length ];
		final int[]                playerCheckBoxToPlayerIndices      = replay.details.getTeamOrderPlayerIndices(); // Tells which players belong to the checkboxes
		final Holder< TextFilter > actionListFilterHolder             = new Holder< TextFilter >( new TextFilter( "", "" ) );
		final JLabel               listedActionsCountLabel            = new JLabel();
		final ActionListener       actionsListRebuilderActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				// First determine what action-types to display
				final Set< ActionType > displayActionTypeSet = EnumSet.noneOf( ActionType.class );
				for ( final ActionType actionType : ActionType.values() )
					if ( displayActionTypeCheckboxMap.get( actionType ).isSelected() )
						displayActionTypeSet.add( actionType );
				
				// Now filter actions
				final TextFilter actionListFilter   = actionListFilterHolder.value;
				final boolean    filtersUnspecified = !actionListFilter.isFilterSpecified();
				final boolean[]  playerDisplayable  = new boolean[ playerCheckBoxes.length ];
				for ( int i = 0; i < playerDisplayable.length; i++ )
					playerDisplayable[ playerCheckBoxToPlayerIndices[ i ] ] = playerCheckBoxes[ i ].isSelected();
				final List< Action > displayableActionList = new ArrayList< Action >();
				for ( final Action action : replay.gameEvents.actions )
					if ( playerDisplayable[ action.player ] && displayActionTypeSet.contains( action.type ) && ( filtersUnspecified || actionListFilter.isIncluded( action.toString().toLowerCase() ) ) )
						displayableActionList.add( action );
				
				// And finally make them visible
				actionsList.setListData( displayableActionsHolder.value = displayableActionList.toArray( new Action[ displayableActionList.size() ] ) );
				listedActionsCountLabel.setText( Language.getText( "module.repAnalyzer.tab.charts.actions.listedActionsCount", displayableActionsHolder.value.length ) );
				if ( useListedActionsAsInputCheckBox.isSelected() )
					chartCanvasRepainterListener.actionPerformed( null );
			}
		};
		
		final Box topBox = Box.createVerticalBox();
		
		final Box generalChartOptionsBox = Box.createHorizontalBox();
		generalChartOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBoxes
		generalChartOptionsBox.add( new JLabel( Language.getText( "charts.chartType" ) ) );
		final JComboBox< ChartType > chartTypeComboBox = GuiUtils.createComboBox( ChartType.values(), Settings.KEY_REP_ANALYZER_CHARTS_CHART_TYPE );
		chartTypeComboBox.setToolTipText( Language.getText( "charts.chartTypeToolTip" ) );
		chartTypeComboBox.setMaximumRowCount( chartTypeComboBox.getItemCount() );
		chartTypeComboBox.addActionListener( chartCanvasRepainterListener );
		generalChartOptionsBox.add( chartTypeComboBox );
		generalChartOptionsBox.add( Box.createHorizontalStrut( 5 ) );
		final JCheckBox groupByTeamCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.groupByTeams", Settings.KEY_REP_ANALYZER_CHARTS_GROUP_BY_TEAMS );
		groupByTeamCheckBox.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.groupByTeamToolTip" ) );
		groupByTeamCheckBox.addActionListener( chartCanvasRepainterListener );
		generalChartOptionsBox.add( groupByTeamCheckBox );
		generalChartOptionsBox.add( Box.createHorizontalStrut( 5 ) );
		final JCheckBox allPlayersOnOneChartCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.allPlayersOn1Chart", Settings.KEY_REP_ANALYZER_CHARTS_ALL_PLAYERS_ON_1_CHART );
		chartTypeComboBox.addActionListener( new ActionListener() {
			{ actionPerformed( null ); } // Initialize
			@Override
			public void actionPerformed( final ActionEvent event ) {
				allPlayersOnOneChartCheckBox.setEnabled( ( (ChartType) chartTypeComboBox.getSelectedItem() ).supportsAllOnOneChart );
			}
		} );
		allPlayersOnOneChartCheckBox.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.allPlayersOn1ChartToolTip" ) );
		allPlayersOnOneChartCheckBox.addActionListener( chartCanvasRepainterListener );
		generalChartOptionsBox.add( allPlayersOnOneChartCheckBox );
		generalChartOptionsBox.add( Box.createHorizontalStrut( 8 ) );
		final JCheckBox usePlayersColorsCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.usePlayersColors", Settings.KEY_REP_ANALYZER_CHARTS_USE_PLAYERS_COLORS );
		usePlayersColorsCheckBox.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.usePlayersColorsToolTip" ) );
		usePlayersColorsCheckBox.addActionListener( chartCanvasRepainterListener );
		final ActionListener actionsListRepainterListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				actionsList.invalidate();
				actionsList.getParent().repaint(); // Parent has to be repainted (else all action-labels get white color)
				if ( useListedActionsAsInputCheckBox.isSelected() )
					chartCanvasRepainterListener.actionPerformed( null );
			}
		};
		usePlayersColorsCheckBox.addActionListener( actionsListRepainterListener );
		generalChartOptionsBox.add( usePlayersColorsCheckBox );
		allPlayersOnOneChartCheckBox.addActionListener( new ActionListener() {
			{ actionPerformed( null ); } // Initialize
			@Override
			public void actionPerformed( final ActionEvent event ) {
				usePlayersColorsCheckBox.setEnabled( !allPlayersOnOneChartCheckBox.isSelected() );
				usePlayersColorsCheckBox.updateUI(); // Required due to the Napkin LAF
			}
		} );
		generalChartOptionsBox.add( Box.createHorizontalStrut( 8 ) );
		final JCheckBox displayInSecondsCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.displayInSeconds", Settings.KEY_REP_ANALYZER_CHARTS_DISPLAY_IN_SECONDS );
		displayInSecondsCheckBox.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.displayInSecondsToolTip" ) );
		displayInSecondsCheckBox.addActionListener( new ActionListener() {
			{ actionPerformed( null ); } // Initialize
			@Override
			public void actionPerformed( final ActionEvent event ) {
				replay.gameEvents.displayInSeconds = displayInSecondsCheckBox.isSelected();
			}
		} );
		displayInSecondsCheckBox.addActionListener( actionsListRebuilderActionListener );
		displayInSecondsCheckBox.addActionListener( chartCanvasRepainterListener );
		generalChartOptionsBox.add( displayInSecondsCheckBox );
		generalChartOptionsBox.add( Box.createHorizontalStrut( 9 ) );
		final JLabel zoomLabel = new JLabel( Language.getText( "module.repAnalyzer.tab.charts.zoom" ) );
		zoomLabel.setOpaque( true ); // Needed for the background color to take effect
		generalChartOptionsBox.add( zoomLabel );
		final JComboBox< String > zoomComboBox = GuiUtils.createComboBox( new String[] { "x1", "x2", "x4", "x8", "x16", "x32" }, Settings.KEY_REP_ANALYZER_CHARTS_ZOOM );
		// zoomComboBox does not require explicit chartCanvasRepaint listener, it will invalidate the chartCanvas thus it will be repainted.
		zoomComboBox.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.zoomToolTip" ) ); 
		generalChartOptionsBox.add( zoomComboBox );
		generalChartOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBoxes
		topBox.add( generalChartOptionsBox );
		
		final Box chartSpecificOptionsBox = Box.createHorizontalBox();
		topBox.add( chartSpecificOptionsBox );
		
		final Holder< ChartParams > chartParamsHolder = new Holder< ChartParams >();
		
		topBox.add( Box.createVerticalStrut( 2 ) );
		final Box playersBox = Box.createHorizontalBox();
		playersBox.add( Box.createHorizontalStrut( 3 ) );
		final JLabel overlayChartsLabel = GeneralUtils.createLinkStyledLabel( Language.getText( "module.repAnalyzer.tab.charts.overlayCharts" ) );
		overlayChartsLabel.setIcon( Icons.CHART_PLUS );
		overlayChartsLabel.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked( final MouseEvent event ) {
				new OverlayChartsDialog( overlayChartFrameList, layeredPane, chartCanvasRepainterListener, chartCanvasScrollPane );
			};
		} );
		playersBox.add( overlayChartsLabel );
		playersBox.add( Box.createHorizontalStrut( 5 ) );
		playersBox.add( new JPanel( new BorderLayout() ) );
		final JLabel playersTextLabel = new JLabel( Language.getText( "module.repAnalyzer.tab.charts.players" ) );
		playersBox.add( playersTextLabel );
		playersBox.add( Box.createHorizontalStrut( 5 ) );
		Integer lastTeam = null;
		for ( int i = 0; i <  replay.details.players.length; i++ ) {
			final Player player = replay.details.players[ playerCheckBoxToPlayerIndices[ i ] ];
			final JCheckBox playerCheckBox = playerCheckBoxes[ i ] = new JCheckBox( player.nameWithClan, player.lastActionFrame > 0 );
			playerCheckBox.addActionListener( actionsListRebuilderActionListener );
			playerCheckBox.addActionListener( chartCanvasRepainterListener       );
			if ( lastTeam == null )
				lastTeam = player.team;
			else {
				if ( lastTeam.intValue() != player.team ) {
					playersBox.add( Box.createHorizontalStrut( 7 ) );
					final JLabel vsLabel = new JLabel( "VS." );
					GuiUtils.changeFontToBold( vsLabel );
					playersBox.add( vsLabel );
					playersBox.add( Box.createHorizontalStrut( 12 ) );
					lastTeam = player.team;
			}
			}
			final Box playerMainBox = Box.createHorizontalBox();
			ShowProfileInfo showProfileInfo_;
			try {
				showProfileInfo_ = ShowProfileInfo.values()[ Settings.getInt( Settings.KEY_REP_MISC_ANALYZER_SHOW_PROFILE_INFO ) ];
			} catch ( final IllegalArgumentException iae ) {
				showProfileInfo_ = ShowProfileInfo.values()[ Settings.getDefaultInt( Settings.KEY_REP_MISC_ANALYZER_SHOW_PROFILE_INFO ) ];
			}
			final ShowProfileInfo showProfileInfo = showProfileInfo_;
			try {
				Settings.getInt( Settings.KEY_REP_MISC_ANALYZER_SHOW_PROFILE_INFO );
			} catch ( final Exception e ) {
				
			}
			final Box playerWrapperBox = showProfileInfo == ShowProfileInfo.PORTRAIT_AND_LEAGUES ? Box.createHorizontalBox() : showProfileInfo == ShowProfileInfo.LEAGUES_ONLY ? Box.createVerticalBox() : playerMainBox;
			final List< Object > componentToOpenProfileDialogList = new ArrayList< Object >( 5 ); // Portrait label and the 4 leagues labels
			final MouseListener playerContextMenuMouseListener = new MouseAdapter() {
				@Override
				public void mousePressed( final MouseEvent event ) {
					if ( event.getButton() == GuiUtils.MOUSE_BUTTON_RIGHT ) {
						final JPopupMenu playerPopupMenu;
						if ( replayFile != null ) {
							playerPopupMenu = new PlayerPopupMenu( player.playerId, player.type );
							playerPopupMenu.addSeparator();
						}
						else
							playerPopupMenu = new JPopupMenu();
						final JMenuItem showOnlyThisPlayerMenuItem = new JMenuItem( Language.getText( "module.repAnalyzer.tab.charts.playerMenu.showOnlyThisPlayer" ), Icons.USER_ARROW );
						showOnlyThisPlayerMenuItem.addActionListener( new ActionListener() {
							@Override
							public void actionPerformed( final ActionEvent event ) {
								for ( final JCheckBox otherCheckBox : playerCheckBoxes )
									if ( playerCheckBox != otherCheckBox && otherCheckBox.isSelected() || playerCheckBox == otherCheckBox && !otherCheckBox.isSelected() )
										otherCheckBox.doClick( 0 );
							}
						} );
						playerPopupMenu.add( showOnlyThisPlayerMenuItem );
						final JMenuItem showAllPlayersMenuItem = new JMenuItem( Language.getText( "module.repAnalyzer.tab.charts.playerMenu.showAllPlayers" ), Icons.USERS );
						showAllPlayersMenuItem.addActionListener( new ActionListener() {
							@Override
							public void actionPerformed( final ActionEvent event ) {
								for ( final JCheckBox playerCheckBox : playerCheckBoxes )
									if ( !playerCheckBox.isSelected() )
										playerCheckBox.doClick( 0 );
							}
						} );
						playerPopupMenu.add( showAllPlayersMenuItem );
						playerPopupMenu.show( event.getComponent(), event.getX(), event.getY() );
					}
					else if ( event.getButton() == GuiUtils.MOUSE_BUTTON_LEFT ) {
						final boolean noProfile = replay.initData.gateway == Gateway.UNKNOWN || replay.initData.gateway == Gateway.PUBLIC_TEST || player.type != PlayerType.HUMAN || player.playerId.battleNetId == 0;
						if ( noProfile || !componentToOpenProfileDialogList.contains( event.getSource() ) )
							playerCheckBox.doClick( 100 );
						else
							new PlayerProfileDialog( player.playerId, player.type );
					}
				}
			};
			if ( player.handicap < 100 ) {
				final JLabel handicapIconLabel = new JLabel( Icons.HANDICAP );
				handicapIconLabel.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.players.handicapToolTip", player.handicap ) );
				handicapIconLabel.addMouseListener( playerContextMenuMouseListener );
				playerMainBox.add( handicapIconLabel );
			}
			if ( player.isWinner != null && Settings.getBoolean( Settings.KEY_SETTINGS_MISC_SHOW_WINNERS ) ) {
				final JLabel resultIconLabel = new JLabel( player.isWinner ? Icons.WIN : Icons.LOSS );
				resultIconLabel.setToolTipText( Language.getText( player.isWinner ? "module.repAnalyzer.tab.charts.players.winnerToolTip" : "module.repAnalyzer.tab.charts.players.loserToolTip" ) );
				resultIconLabel.addMouseListener( playerContextMenuMouseListener );
				playerMainBox.add( resultIconLabel );
			}
			final JLabel playerColorLabel = new JLabel( GuiUtils.getColorIcon( player.getColor() ) );
			playerColorLabel.setToolTipText( player.playerColor.stringValue );
			playerColorLabel.addMouseListener( playerContextMenuMouseListener );
			playerMainBox.add( playerColorLabel );
			playerMainBox.add( playerCheckBox );
			playerCheckBox.addActionListener( new ActionListener() {
				{ actionPerformed( null ); } // Set initial border
				@Override
				public void actionPerformed( final ActionEvent event ) {
					playerWrapperBox.setBorder( ( playerCheckBox.isSelected() ? LEAGUE_SEL_BORDER_MAP : LEAGUE_UNSEL_BORDER_MAP ).get( player.getLeague() ) );
				}
			} );
			final JLabel raceLabel = GuiUtils.createRaceIconLabel( player.race == null ? player.finalRace : player.race );
			raceLabel.addMouseListener( playerContextMenuMouseListener );
			playerMainBox.add( raceLabel );
			playerCheckBox.addMouseListener( playerContextMenuMouseListener );
			// Profile info (profile, league, rank, wins)
			if ( showProfileInfo != ShowProfileInfo.NONE ) {
				// Portrait
				final Box verticalPlayerBox;
				final JLabel portraitLabel;
				final Boolean customPortraitReady;
				if ( showProfileInfo == ShowProfileInfo.PORTRAIT_AND_LEAGUES ) {
					playerWrapperBox.add( portraitLabel = new JLabel( player.type == PlayerType.COMPUTER ? Icons.PORTRAIT_COMPUTER_ICON : Icons.PORTRAIT_LOADING_ICON ) );
					componentToOpenProfileDialogList.add( portraitLabel );
					portraitLabel.addMouseListener( playerContextMenuMouseListener );
					verticalPlayerBox = Box.createVerticalBox();
					playerWrapperBox.add( verticalPlayerBox );
					// Start downloading custom portrait now (if defined), it's independent from profile info
					customPortraitReady = ProfileCache.queryCustomPortrait( player.playerId, false, new CustomPortraitListener() {
						@Override
						public void customPortraitReady( final ImageIcon customPortrait ) {
							portraitLabel.setIcon( customPortrait == null ? Icons.PORTRAIT_NA_ICON : customPortrait );
						}
					} );
				}
				else {
					portraitLabel       = null;
					verticalPlayerBox   = playerWrapperBox;
					customPortraitReady = null;
				}
				verticalPlayerBox.add( playerMainBox );
				final Box leaguesBox = Box.createHorizontalBox();
				verticalPlayerBox.add( leaguesBox );
				if ( player.type == PlayerType.COMPUTER ) {
					final JLabel difficultyLabel = new JLabel( Language.getText( "module.repAnalyzer.tab.charts.players.difficulty", player.difficulty.stringValue ) );
					difficultyLabel.addMouseListener( playerContextMenuMouseListener );
					leaguesBox.add( difficultyLabel );
				}
				else {
					final JLabel[] leagueLabels = new JLabel[ 4 ];
					for ( int j = 0; j < 4; j++ ) {
						leaguesBox.add( leagueLabels[ j ] = new JLabel( Icons.LEAGUE_LOADING_ICON ) );
						componentToOpenProfileDialogList.add( leagueLabels[ j ] );
						leagueLabels[ j ].addMouseListener( playerContextMenuMouseListener );
					}
					ProfileCache.queryProfile( player.playerId, new ProfileListener() {
						@Override
						public void profileReady( final IProfile profile, final boolean isAnotherRetrievingInProgress ) {
							final String toolTip = GuiUtils.updateLeagueLabels( profile, leagueLabels, isAnotherRetrievingInProgress );
							
							if ( showProfileInfo == ShowProfileInfo.PORTRAIT_AND_LEAGUES ) {
								if ( customPortraitReady == null )
									portraitLabel.setIcon( profile == null ? Icons.PORTRAIT_NA_ICON : Icons.getPortraitIcon( profile.getPortraitGroup(), profile.getPortraitRow(), profile.getPortraitColumn() ) );
								if ( toolTip != null )
									portraitLabel.setToolTipText( toolTip );
							}
						}
					}, Settings.getBoolean( Settings.KEY_REP_MISC_ANALYZER_AUTO_RETRIEVE_EXT_PROFILE_INFO ) );
				}
			}
			playerWrapperBox.addMouseListener( playerContextMenuMouseListener );
			playersBox.add( playerWrapperBox );
			if ( i < replay.details.players.length - 1 )
				playersBox.add( Box.createHorizontalStrut( 5 ) );
		}
		playersBox.add( new JPanel( new BorderLayout() ) );
		playersBox.add( Box.createHorizontalStrut( 5 ) );
		// To position players in center, we need the "same type of component" that is before them: a link styled label
		playersBox.add( GeneralUtils.createLinkStyledLabel( "" ) );
		// Custom grid handler
		final GridParams gridParams = new GridParams(); // We create it now so it will store the current grid param settings
		final JCheckBox gridCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.grid", Settings.KEY_REP_ANALYZER_CHARTS_GRID );
		gridCheckBox.setOpaque( true ); // Needed for the background color to take effect
		gridCheckBox.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.gridToolTip" ) );
		gridCheckBox.addActionListener( new ActionListener() {
			{ actionPerformed( null ); } // Set background based on current value
			@Override
			public void actionPerformed( final ActionEvent event ) {
				gridCheckBox.setBackground( gridCheckBox.isSelected() ? Color.GREEN : null );
			}
		} );
		gridCheckBox.addActionListener( chartCanvasRepainterListener );
		playersBox.add( gridCheckBox );
		playersBox.add( Box.createHorizontalStrut( 5 ) );
		final JLabel gridSettingsLabel = GuiUtils.createIconLabelButton( Icons.GRID, "module.repAnalyzer.tab.charts.gridSettingsToolTip" );
		gridSettingsLabel.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked( final MouseEvent event ) {
				new GridSettingsDialog( gridParams, chartParamsHolder.value, chartCanvasRepainterListener );
			};
		} );
		playersBox.add( gridSettingsLabel );
		playersBox.add( Box.createHorizontalStrut( 3 ) );
		topBox.add( GuiUtils.createSelfManagedScrollPane( playersBox, chartsPanel ) );
		chartsPanel.add( topBox, BorderLayout.NORTH );
		
		final JSplitPane chartSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, true );
		chartSplitPane.setDividerSize( 6 );
		
		// APM chart options
		final JComboBox< Integer > apmGranularityComboBox = GuiUtils.createComboBox( new Integer[] { 1, 2, 3, 4, 5, 10, 15, 20, 30, 50, 100, 200 }, Settings.KEY_REP_ANALYZER_CHARTS_APM_GRANULARITY );
		apmGranularityComboBox.setMaximumRowCount( apmGranularityComboBox.getItemCount() );
		apmGranularityComboBox.addActionListener( chartCanvasRepainterListener );
		final JComboBox< GraphApproximation > apmApproximationComboBox = GuiUtils.createComboBox( GraphApproximation.values(), Settings.KEY_REP_ANALYZER_CHARTS_APM_GRAPH_APPROXIMATION );
		apmApproximationComboBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showEapmCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.apm.showEapm", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_EAPM );
		showEapmCheckBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showMicroMacroApmCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.apm.showMicroMacroApm", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_MICRO_MACRO_APM );
		showMicroMacroApmCheckBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showXapmCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.apm.showXapm", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_XAPM );
		showXapmCheckBox.addActionListener( chartCanvasRepainterListener );
		final JLabel apmTypesHelpLinkLabel = GeneralUtils.createLinkLabel( null, Consts.URL_APM_TYPES );
		apmTypesHelpLinkLabel.setIcon( Icons.QUESTION );
		apmTypesHelpLinkLabel.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.apm.apmTypesHelpToolTip" ) );
		// Hotkeys chart options
		final JCheckBox showSelectHotkeysCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.hotkeys.showSelectHotkeys", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_SELECT_HOTKEYS );
		showSelectHotkeysCheckBox.addActionListener( chartCanvasRepainterListener );
		// Builds/Tech chart options
		final JCheckBox showBuildsCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.buildsTech.showBuilds", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_BUILDS );
		showBuildsCheckBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showTrainsCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.buildsTech.showTrains", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_TRAINS );
		showTrainsCheckBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showWorkersCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.buildsTech.showWorkers", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_WORKERS );
		showTrainsCheckBox.addActionListener( new ActionListener() {
			{ actionPerformed( null ); } // Initialize
			@Override
			public void actionPerformed( final ActionEvent event ) {
				showWorkersCheckBox.setEnabled( showTrainsCheckBox.isSelected() );
				showWorkersCheckBox.updateUI(); // Required due to the Napkin LAF
			}
		} );
		showWorkersCheckBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showResearchesCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.buildsTech.showResearches", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_RESEARCHES );
		showResearchesCheckBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showUpgradesCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.buildsTech.showUpgrades", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_UPGRADES );
		showUpgradesCheckBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showAbilityGroupsCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.buildsTech.showAbilityGroups", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_ABILITY_GROUPS );
		showAbilityGroupsCheckBox.addActionListener( chartCanvasRepainterListener );
		final JComboBox< ShowDuration > showDurationComboBox = GuiUtils.createComboBox( ShowDuration.values(), Settings.KEY_REP_ANALYZER_CHARTS_SHOW_DURATION );
		showDurationComboBox.addActionListener( chartCanvasRepainterListener );
		final JComboBox< IconSize > iconSizesComboBox = GuiUtils.createComboBox( EnumCache.ICON_SIZES, Settings.KEY_REP_ANALYZER_CHARTS_ICON_SIZES );
		iconSizesComboBox.addActionListener( chartCanvasRepainterListener );
		// Builds/Tech Stat chart options
		final JCheckBox showUnitsStatCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.buildsTechStat.showUnits", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_UNITS_STAT );
		showUnitsStatCheckBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showBuildingsStatCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.buildsTechStat.showBuildings", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_BUILDINGS_STAT );
		showBuildingsStatCheckBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showResearchesStatCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.buildsTechStat.showResearches", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_RESEARCHES_STAT );
		showResearchesStatCheckBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showUpgradesStatCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.buildsTechStat.showUpgrades", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_UPGRADES );
		showUpgradesStatCheckBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showAbilityGroupsStatCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.buildsTechStat.showAbilityGroups", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_ABILITY_GROUPS_STAT );
		showAbilityGroupsStatCheckBox.addActionListener( chartCanvasRepainterListener );
		final JComboBox< IconSize > barSizeComboBox = GuiUtils.createComboBox( GeneralUtils.remainingElements( EnumCache.ICON_SIZES, EnumSet.of( IconSize.HIDDEN ) ), Settings.KEY_REP_ANALYZER_CHARTS_BAR_SIZE );
		barSizeComboBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showAfterCompletedCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.showAfterCompleted", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_AFTER_COMPLETED );
		showAfterCompletedCheckBox.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.showAfterCompletedToolTip" ) );
		showAfterCompletedCheckBox.addActionListener( chartCanvasRepainterListener );
		// Map view chart options
		final JComboBox< MapViewQuality > mapViewQualityComboBox = GuiUtils.createComboBox( MapViewQuality.values(), Settings.KEY_REP_ANALYZER_CHARTS_MAP_VIEW_QUALITY );
		mapViewQualityComboBox.addActionListener( chartCanvasRepainterListener );
		final JComboBox< String > areaGranularityCountComboBox = GuiUtils.createComboBox( new String[] { "2x2", "3x3", "4x4", "6x6", "8x8", "12x12", "16x16", "32x32", "64x64", "128x128", "256x256" }, Settings.KEY_REP_ANALYZER_CHARTS_AREA_GRANULARITY );
		final JComboBox< MapBackground > mapBackgroundComboBox = GuiUtils.createComboBox( MapBackground.values(), Settings.KEY_REP_ANALYZER_CHARTS_MAP_BACKGROUND );
		mapBackgroundComboBox.setMaximumRowCount( mapBackgroundComboBox.getItemCount() );
		mapBackgroundComboBox.addActionListener( chartCanvasRepainterListener );
		mapBackgroundComboBox.addActionListener( new ActionListener() {
			{ actionPerformed( null ); } // Initialize
			@Override
			public void actionPerformed( final ActionEvent event ) {
				areaGranularityCountComboBox.setEnabled( ( (MapBackground) mapBackgroundComboBox.getSelectedItem() ).involvesAreaAggregation );
			}
		} );
		areaGranularityCountComboBox.setMaximumRowCount( areaGranularityCountComboBox.getModel().getSize() ); // Display all values
		areaGranularityCountComboBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox hideOverlappedBuildingsCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.mapView.hideOverlappedBuildings", Settings.KEY_REP_ANALYZER_CHARTS_HIDE_OVERLAPPED_BUILDINGS );
		hideOverlappedBuildingsCheckBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox fillBuildingIconsCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.mapView.fillBuildingIcons", Settings.KEY_REP_ANALYZER_CHARTS_FILL_BUILDING_ICONS );
		fillBuildingIconsCheckBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox showMapObjectsCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.mapView.showMapObjects", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_MAP_OBJECTS );
		showMapObjectsCheckBox.addActionListener( chartCanvasRepainterListener );
		// Action distribution chart options
		final JCheckBox showPercentCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.actionDistribution.showPercent", Settings.KEY_REP_ANALYZER_CHARTS_SHOW_PERCENT );
		showPercentCheckBox.addActionListener( chartCanvasRepainterListener );
		final JComboBox< IconSize > distributionBarSizeComboBox = GuiUtils.createComboBox( GeneralUtils.remainingElements( EnumCache.ICON_SIZES, EnumSet.of( IconSize.HIDDEN ) ), Settings.KEY_REP_ANALYZER_CHARTS_DISTRIBUTION_BAR_SIZE );
		distributionBarSizeComboBox.addActionListener( chartCanvasRepainterListener );
		// Main building control chart options
		final JCheckBox calculateUntilMarkerCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.mainBuildingControl.calculateUntilTimeMarker", Settings.KEY_REP_ANALYZER_CHARTS_CALCULATE_UNTIL_TIME_MARKER );
		calculateUntilMarkerCheckBox.addActionListener( chartCanvasRepainterListener );
		final JLabel mainBuildingControlHelpLinkLabel = GeneralUtils.createLinkLabel( null, Consts.URL_MAIN_BUILDING_CONTROL_CHART );
		mainBuildingControlHelpLinkLabel.setIcon( Icons.QUESTION );
		mainBuildingControlHelpLinkLabel.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.mainBuildingControl.mainBuildingControlHelpToolTip" ) );
		// Unit Tiers chart options
		final JComboBox< Integer > unitTiersGranularityComboBox = GuiUtils.createComboBox( new Integer[] { 1, 2, 3, 4, 5, 10, 15, 20, 30, 50, 100, 200, 300, 500, 1000, 9999 }, Settings.KEY_REP_ANALYZER_CHARTS_UNIT_TIERS_GRANULARITY );
		unitTiersGranularityComboBox.setMaximumRowCount( unitTiersGranularityComboBox.getItemCount() );
		unitTiersGranularityComboBox.addActionListener( chartCanvasRepainterListener );
		final JCheckBox stretchBarsCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.unitTiers.stretchBars", Settings.KEY_REP_ANALYZER_CHARTS_STRETCH_BARS );
		stretchBarsCheckBox.addActionListener( chartCanvasRepainterListener );
		// Resource Spending Rate chart options
		final JComboBox< Integer > rsrGranularityComboBox = GuiUtils.createComboBox( new Integer[] { 1, 2, 3, 4, 5, 10, 15, 20, 30, 50, 100, 200 }, Settings.KEY_REP_ANALYZER_CHARTS_RSR_GRANULARITY );
		rsrGranularityComboBox.setMaximumRowCount( rsrGranularityComboBox.getItemCount() );
		rsrGranularityComboBox.addActionListener( chartCanvasRepainterListener );
		final JComboBox< GraphApproximation > rsrApproximationComboBox = GuiUtils.createComboBox( GraphApproximation.values(), Settings.KEY_REP_ANALYZER_CHARTS_RSR_GRAPH_APPROXIMATION );
		rsrApproximationComboBox.addActionListener( chartCanvasRepainterListener );
		// Resources spent chart options
		final JCheckBox rsCumulativeCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.resourcesSpent.cumulative", Settings.KEY_REP_ANALYZER_CHARTS_RS_CUMULATIVE );
		rsCumulativeCheckBox.addActionListener( chartCanvasRepainterListener );
		final JComboBox< Integer > rsGranularityComboBox = GuiUtils.createComboBox( new Integer[] { 1, 2, 3, 4, 5, 10, 15, 20, 30, 50, 100, 200, 300, 500 }, Settings.KEY_REP_ANALYZER_CHARTS_RS_GRANULARITY );
		rsGranularityComboBox.setMaximumRowCount( rsGranularityComboBox.getItemCount() );
		rsGranularityComboBox.addActionListener( chartCanvasRepainterListener );
		rsCumulativeCheckBox.addActionListener( new ActionListener() {
			{ actionPerformed( null ); } // Initialize
			@Override
			public void actionPerformed( final ActionEvent event ) {
				rsGranularityComboBox.setEnabled( !rsCumulativeCheckBox.isSelected() );
			}
		} );
		// Produced Army/Supply chart options
		final JCheckBox pasCumulativeCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.resourcesSpent.cumulative", Settings.KEY_REP_ANALYZER_CHARTS_PAS_CUMULATIVE );
		pasCumulativeCheckBox.addActionListener( chartCanvasRepainterListener );
		final JComboBox< Integer > pasGranularityComboBox = GuiUtils.createComboBox( new Integer[] { 1, 2, 3, 4, 5, 10, 15, 20, 30, 50, 100, 200, 300, 500 }, Settings.KEY_REP_ANALYZER_CHARTS_PAS_GRANULARITY );
		pasGranularityComboBox.setMaximumRowCount( pasGranularityComboBox.getItemCount() );
		pasGranularityComboBox.addActionListener( chartCanvasRepainterListener );
		pasCumulativeCheckBox.addActionListener( new ActionListener() {
			{ actionPerformed( null ); } // Initialize
			@Override
			public void actionPerformed( final ActionEvent event ) {
				pasGranularityComboBox.setEnabled( !pasCumulativeCheckBox.isSelected() );
			}
		} );
		final JCheckBox includeInitialUnitsCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.producedArmySupply.includeInitialUnits", Settings.KEY_REP_ANALYZER_CHARTS_INCLUDE_INITIAL_UNITS );
		includeInitialUnitsCheckBox.addActionListener( chartCanvasRepainterListener );
		// Redundancy distribution chart options
		final JCheckBox showRedPercentCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.actionDistribution.showPercent", Settings.KEY_REP_ANALYZER_CHARTS_RED_SHOW_PERCENT );
		showRedPercentCheckBox.addActionListener( chartCanvasRepainterListener );
		final JComboBox< IconSize > redDistributionBarSizeComboBox = GuiUtils.createComboBox( GeneralUtils.remainingElements( EnumCache.ICON_SIZES, EnumSet.of( IconSize.HIDDEN ) ), Settings.KEY_REP_ANALYZER_CHARTS_RED_DISTRIBUTION_BAR_SIZE );
		redDistributionBarSizeComboBox.addActionListener( chartCanvasRepainterListener );
		// Action sequences chart options
		final JComboBox< Integer > maxFrameBreakComboBox = GuiUtils.createComboBox( new Integer[] { 0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 48, 56, 64, 72, 84, 96, 112, 128, 196, 256, 512 }, Settings.KEY_REP_ANALYZER_CHARTS_MAX_FRAME_BREAK );
		maxFrameBreakComboBox.setMaximumRowCount( maxFrameBreakComboBox.getItemCount() );
		maxFrameBreakComboBox.addActionListener( chartCanvasRepainterListener );
		// Productions chart options
		final JCheckBox groupSameProductionsCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.productions.groupSameProductions", Settings.KEY_REP_ANALYZER_CHARTS_GROUP_SAME_PRODUCTIONS );
		groupSameProductionsCheckBox.addActionListener( chartCanvasRepainterListener );
		final JComboBox< IconSize > iconSizesPComboBox = GuiUtils.createComboBox( GeneralUtils.remainingElements( EnumCache.ICON_SIZES, EnumSet.of( IconSize.HIDDEN ) ), Settings.KEY_REP_ANALYZER_CHARTS_ICON_SIZES_P );
		iconSizesPComboBox.addActionListener( chartCanvasRepainterListener );
		// Current Selections chart options
		final JComboBox< IconSize > iconSizesPSComboBox = GuiUtils.createComboBox( GeneralUtils.remainingElements( EnumCache.ICON_SIZES, EnumSet.of( IconSize.HIDDEN ) ), Settings.KEY_REP_ANALYZER_CHARTS_ICON_SIZES_PS );
		iconSizesPSComboBox.addActionListener( chartCanvasRepainterListener );
		// ...End of chart option components
		updateChartSpecificComponentsUITask.value = new Runnable() {
			@Override
			public void run() {
				final JComponent[] chartSpecificComponents = {
					apmGranularityComboBox, apmApproximationComboBox, showEapmCheckBox, showMicroMacroApmCheckBox, showXapmCheckBox, apmTypesHelpLinkLabel,
					showSelectHotkeysCheckBox,
					showBuildsCheckBox, showTrainsCheckBox, showWorkersCheckBox, showResearchesCheckBox, showUpgradesCheckBox, showAbilityGroupsCheckBox, showDurationComboBox, iconSizesComboBox,
					showUnitsStatCheckBox, showBuildingsStatCheckBox, showResearchesStatCheckBox, showUpgradesStatCheckBox, showAbilityGroupsStatCheckBox, barSizeComboBox, showAfterCompletedCheckBox,
					mapViewQualityComboBox, areaGranularityCountComboBox, mapBackgroundComboBox, hideOverlappedBuildingsCheckBox, fillBuildingIconsCheckBox, showMapObjectsCheckBox,
					showPercentCheckBox, distributionBarSizeComboBox,
					calculateUntilMarkerCheckBox, mainBuildingControlHelpLinkLabel,
					unitTiersGranularityComboBox, stretchBarsCheckBox,
					rsrGranularityComboBox, rsrApproximationComboBox,
					rsCumulativeCheckBox, rsGranularityComboBox,
					pasCumulativeCheckBox, pasGranularityComboBox, includeInitialUnitsCheckBox,
					showRedPercentCheckBox, redDistributionBarSizeComboBox,
					maxFrameBreakComboBox,
					groupSameProductionsCheckBox,
					iconSizesPComboBox,
					iconSizesPSComboBox
				};
				for ( final JComponent chartSpecificOptionComponent : chartSpecificComponents )
					chartSpecificOptionComponent.updateUI();
			}
		};
		chartTypeComboBox.addActionListener( new ActionListener() {
			{ actionPerformed( null ); } // Initialize
			@Override
			public void actionPerformed( final ActionEvent event ) {
				chartSpecificOptionsBox.removeAll();
				
				switch ( (ChartType) chartTypeComboBox.getSelectedItem() ) {
				case APM :
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBoxes
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "charts.granularity" ) ) );
					chartSpecificOptionsBox.add( apmGranularityComboBox );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.apm.pixels" ) ) );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 6 ) );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "charts.graphApproximation" ) ) );
					chartSpecificOptionsBox.add( apmApproximationComboBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 5 ) );
					chartSpecificOptionsBox.add( showEapmCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showMicroMacroApmCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showXapmCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 20 ) );
					chartSpecificOptionsBox.add( apmTypesHelpLinkLabel );
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBoxes
					break;
				case HOTKEYS :
					chartSpecificOptionsBox.add( showSelectHotkeysCheckBox );
					break;
				case BUILDS_TECH :
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					chartSpecificOptionsBox.add( showBuildsCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showTrainsCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showWorkersCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showResearchesCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showUpgradesCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showAbilityGroupsCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 7 ) );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.buildsTech.iconSizes" ) ) );
					chartSpecificOptionsBox.add( iconSizesComboBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.buildsTech.showDuration" ) ) );
					chartSpecificOptionsBox.add( showDurationComboBox );
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					break;
				case BUILDS_TECH_STAT :
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					chartSpecificOptionsBox.add( showUnitsStatCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showBuildingsStatCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showResearchesStatCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showUpgradesStatCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showAbilityGroupsStatCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 7 ) );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.buildsTechStat.barSize" ) ) );
					chartSpecificOptionsBox.add( barSizeComboBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showAfterCompletedCheckBox );
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					break;
				case MAP_VIEW :
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.mapView.quality" ) ) );
					chartSpecificOptionsBox.add( mapViewQualityComboBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.mapView.background" ) ) );
					chartSpecificOptionsBox.add( mapBackgroundComboBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.mapView.areaGranularity" ) ) );
					chartSpecificOptionsBox.add( areaGranularityCountComboBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( hideOverlappedBuildingsCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( fillBuildingIconsCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showMapObjectsCheckBox );
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					break;
				case ACTION_DISTRIBUTION :
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					chartSpecificOptionsBox.add( showPercentCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 10 ) );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.buildsTechStat.barSize" ) ) );
					chartSpecificOptionsBox.add( distributionBarSizeComboBox );
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					break;
				case MAIN_BUILDING_CONTROL :
					chartSpecificOptionsBox.add( calculateUntilMarkerCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 20 ) );
					chartSpecificOptionsBox.add( mainBuildingControlHelpLinkLabel );
					break;
				case UNIT_TIERS :
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "charts.granularity" ) ) );
					chartSpecificOptionsBox.add( unitTiersGranularityComboBox );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.apm.pixels" ) ) );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( stretchBarsCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showAfterCompletedCheckBox );
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					break;
				case RESOURCE_SPENDING_RATE :
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBoxes
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "charts.granularity" ) ) );
					chartSpecificOptionsBox.add( rsrGranularityComboBox );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.apm.pixels" ) ) );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 6 ) );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "charts.graphApproximation" ) ) );
					chartSpecificOptionsBox.add( rsrApproximationComboBox );
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBoxes
					break;
				case RESOURCES_SPENT :
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					chartSpecificOptionsBox.add( rsCumulativeCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 8 ) );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "charts.granularity" ) ) );
					chartSpecificOptionsBox.add( rsGranularityComboBox );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.apm.pixels" ) ) );
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					break;
				case PRODUCED_ARMY_SUPPLY :
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					chartSpecificOptionsBox.add( pasCumulativeCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 8 ) );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "charts.granularity" ) ) );
					chartSpecificOptionsBox.add( pasGranularityComboBox );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.apm.pixels" ) ) );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( includeInitialUnitsCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 3 ) );
					chartSpecificOptionsBox.add( showAfterCompletedCheckBox );
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					break;
				case APM_REDUNDANCY_DISTRIBUTION :
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					chartSpecificOptionsBox.add( showRedPercentCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 10 ) );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.buildsTechStat.barSize" ) ) );
					chartSpecificOptionsBox.add( redDistributionBarSizeComboBox );
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					break;
				case ACTION_SEQUENCES :
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.actionSequences.maxSequenceTimeBreak" ) ) );
					chartSpecificOptionsBox.add( maxFrameBreakComboBox );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.actionSequences.frames" ) ) );
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					break;
				case PRODUCTIONS :
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					chartSpecificOptionsBox.add( groupSameProductionsCheckBox );
					chartSpecificOptionsBox.add( Box.createHorizontalStrut( 10 ) );
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.productions.iconSizes" ) ) );
					chartSpecificOptionsBox.add( iconSizesPComboBox );
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					break;
				case PLAYER_SELECTIONS :
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					chartSpecificOptionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.playerSelections.iconSizes" ) ) );
					chartSpecificOptionsBox.add( iconSizesPSComboBox );
					chartSpecificOptionsBox.add( new JPanel( new BorderLayout() ) ); // Need to fill up space due to JComboBox
					break;
				}
				
				chartsPanel.validate();
				chartsPanel.repaint(); // Needed: chartSpecificOptionsBox does not get repainted in some cases when its size (height) does not change 
			}
		} );
		
		final JPanel bottomPanel = new JPanel( new BorderLayout() );
		final Holder< Integer > markerFrameHolder    = new Holder< Integer     >();
		final Holder< Integer > markerXHolder        = new Holder< Integer     >();
		final Holder< Action  > selectedActionHolder = new Holder< Action      >();
		
		final JComponent chartCanvas = chartCanvasHolder.value = new JComponent() {
			@Override
			protected void paintComponent( final Graphics graphics ) {
				// Create the chart params
				ChartParams params            = chartParamsHolder   .value = new ChartParams();
				params.chartCanvas            = chartCanvasHolder   .value;
				params.markerFrame            = markerFrameHolder   .value;
				params.markerX                = markerXHolder       .value;
				params.selectedAction         = selectedActionHolder.value;
				params.visibleRectangle       = getVisibleRect();
				params.g2                     = (Graphics2D) graphics;
				params.replay                 = replay;
				params.actions                = useListedActionsAsInputCheckBox.isSelected() ? displayableActionsHolder.value : replay.gameEvents.actions;
				params.chartType              = (ChartType) chartTypeComboBox.getSelectedItem();
				params.groupByTeam            = groupByTeamCheckBox.isSelected();
				params.usePlayersInGameColors = usePlayersColorsCheckBox.isSelected();
				params.displayInSeconds       = displayInSecondsCheckBox.isSelected();
				params.zoom                   = Integer.parseInt( ( (String) zoomComboBox.getSelectedItem() ).substring( 1 ) );
				if ( gridCheckBox.isSelected() )
					params.gridParams         = gridParams;
				params.setVisiblePlayers( playerCheckBoxes, playerCheckBoxToPlayerIndices );
				params.width  = getWidth();
				params.height = getHeight();
				
				for ( int i = -1; i < overlayChartFrameList.size(); i++ ) { // -1 for the main chart
					if ( i >= 0 )
						overlayChartFrameList.get( i ).setChartParams( params = (ChartParams) chartParamsHolder.value.clone() );
					
					// This parameter/property has to be set here, it depends on the chart type!
					params.allPlayersOnOneChart = params.chartType.supportsAllOnOneChart && allPlayersOnOneChartCheckBox.isSelected();
					
					switch ( params.chartType ) {
					case APM :
						params.apmGranularity    = (Integer) apmGranularityComboBox.getSelectedItem();
						params.apmApproximation  = (GraphApproximation) apmApproximationComboBox.getSelectedItem();
						params.showEapm          = showEapmCheckBox         .isSelected();
						params.showMicroMacroApm = showMicroMacroApmCheckBox.isSelected();
						params.showXapm          = showXapmCheckBox         .isSelected();
						break;
					case HOTKEYS :
						params.showSelectHotkeys = showSelectHotkeysCheckBox.isSelected();
						break;
					case BUILDS_TECH :
						params.showBuilds            = showBuildsCheckBox       .isSelected();
						params.showTrains            = showTrainsCheckBox       .isSelected();
						params.showWorkers           = showWorkersCheckBox      .isSelected();
						params.showResearches        = showResearchesCheckBox   .isSelected();
						params.showUpgrades          = showUpgradesCheckBox     .isSelected();
						params.showAbilityGroups     = showAbilityGroupsCheckBox.isSelected();
						params.showDuration          = (ShowDuration) showDurationComboBox.getSelectedItem();
						params.iconSizes             = (IconSize        ) iconSizesComboBox   .getSelectedItem();
						break;
					case BUILDS_TECH_STAT :
						params.showUnitsStat         = showUnitsStatCheckBox        .isSelected();
						params.showBuildingsStat     = showBuildingsStatCheckBox    .isSelected();
						params.showResearchesStat    = showResearchesStatCheckBox   .isSelected();
						params.showUpgradesStat      = showUpgradesStatCheckBox     .isSelected();
						params.showAbilityGroupsStat = showAbilityGroupsStatCheckBox.isSelected();
						params.barSize               = (IconSize) barSizeComboBox.getSelectedItem();
						params.showAfterCompleted    = showAfterCompletedCheckBox   .isSelected();
						break;
					case MAP_VIEW :
						params.mapViewQuality          = (MapViewQuality) mapViewQualityComboBox.getSelectedItem();
						params.mapBackground           = (MapBackground ) mapBackgroundComboBox .getSelectedItem();
						final String selectedAreaGranularityCount = (String) areaGranularityCountComboBox.getSelectedItem();
						params.areaGranularityCount    = Integer.parseInt( selectedAreaGranularityCount.substring( 0, selectedAreaGranularityCount.indexOf( 'x' ) ) );
						params.hideOverlappedBuildings = hideOverlappedBuildingsCheckBox.isSelected();
						params.fillBuildingIcons       = fillBuildingIconsCheckBox      .isSelected();
						params.showMapObjects          = showMapObjectsCheckBox         .isSelected();
						break;
					case ACTION_DISTRIBUTION :
						params.showPercent         = showPercentCheckBox.isSelected();
						params.distributionBarSize = (IconSize) distributionBarSizeComboBox.getSelectedItem();
						break;
					case MAIN_BUILDING_CONTROL :
						params.calculateUntilMarker = calculateUntilMarkerCheckBox.isSelected();
						break;
					case UNIT_TIERS :
						params.unitTiersGranularity = (Integer) unitTiersGranularityComboBox.getSelectedItem();
						params.stretchBars          = stretchBarsCheckBox.isSelected();
						params.showAfterCompleted   = showAfterCompletedCheckBox.isSelected();
						break;
					case RESOURCE_SPENDING_RATE :
						params.rsrGranularity   = (Integer) rsrGranularityComboBox.getSelectedItem();
						params.rsrApproximation = (GraphApproximation) rsrApproximationComboBox.getSelectedItem();
						break;
					case RESOURCES_SPENT :
						params.rsCumulative  = rsCumulativeCheckBox.isSelected();
						params.rsGranularity = (Integer) rsGranularityComboBox.getSelectedItem();
						break;
					case PRODUCED_ARMY_SUPPLY :
						params.pasCumulative       = pasCumulativeCheckBox.isSelected();
						params.pasGranularity      = (Integer) pasGranularityComboBox.getSelectedItem();
						params.includeInitialUnits = includeInitialUnitsCheckBox.isSelected();
						params.showAfterCompleted  = showAfterCompletedCheckBox .isSelected();
						break;
					case APM_REDUNDANCY_DISTRIBUTION :
						params.showRedPercent         = showRedPercentCheckBox.isSelected();
						params.redDistributionBarSize = (IconSize) redDistributionBarSizeComboBox.getSelectedItem();
						break;
					case ACTION_SEQUENCES :
						params.maxSequenceFrameBreak = (Integer) maxFrameBreakComboBox.getSelectedItem();
						break;
					case PRODUCTIONS :
						params.groupSameProductions = groupSameProductionsCheckBox.isSelected();
						params.iconSizesP           = (IconSize) iconSizesPComboBox.getSelectedItem();
						break;
					case PLAYER_SELECTIONS :
						params.iconSizesPS = (IconSize) iconSizesPSComboBox.getSelectedItem();
						break;
					}
					
					if ( i < 0 )
						new ChartPainter( params ).paintChart();
					// Overlay charts are painted after this (their repaint() is called)
				}
			}
			@Override
			public Dimension getPreferredSize() {
				if ( chartTypeComboBox.getSelectedItem() == ChartType.MAP_VIEW && replay.mapInfo != null ) {
					final int zoom = Integer.parseInt( ( (String) zoomComboBox.getSelectedItem() ).substring( 1 ) );
					return new Dimension( ( replay.mapInfo.width * zoom ) << 1, ( replay.mapInfo.height * zoom ) << 1 );
				}
				else {
					final Dimension d = chartCanvasScrollPane.getViewport().getExtentSize();
					d.width *= Integer.parseInt( ( (String) zoomComboBox.getSelectedItem() ).substring( 1 ) );
					return d;
				}
			}
			@Override
			public String getToolTipText( final MouseEvent event ) {
				final ChartParams params = chartParamsHolder.value;
				if ( params == null )
					return null;
				else {
					if ( params.toolTipProvider == null ) {
						// Display frame and time info as tool tip
						final int frame = params.xToFrame( event.getX() );
						return frame < 0 ? null : Language.getText( "module.repAnalyzer.tab.charts.chartTooltip.frameTime", frame, ReplayUtils.formatFrames( frame, replay.converterGameSpeed ) );
					}
					else // Custom tool tip
						return params.toolTipProvider.getToolTip( event.getPoint() );
				}
			}
		};
		chartCanvas.setToolTipText( "" ); // This will enable tool tip
		chartCanvas.setFocusable( true );
		GuiUtils.makeComponentDragScrollable( chartCanvas );
		chartCanvas.setCursor( null ); // Remove the cursor set by GuiUtils.makeComponentDragScrollable()
		final BoolHolder disableSyncFromListToChartHolder = new BoolHolder();
		final Task< Integer > syncActionListToFrameTask = new Task< Integer >() {
			@Override
			public void execute( final Integer frame ) {
				if ( chartParamsHolder.value != null && displayableActionsHolder.value.length > 0 ) {
					final Action testAction = replay.gameEvents.new Action();
					testAction.frame = frame;
					final int position = Arrays.binarySearch( displayableActionsHolder.value, testAction, ReplayUtils.ACTION_FRAME_COMPARATOR );
					final int index    = position >= 0 ? position : -position <= displayableActionsHolder.value.length ? -position - 1 : -position - 2;
					disableSyncFromListToChartHolder.value = true;
					actionsList.setSelectedIndex( index );
					actionsList.ensureIndexIsVisible( index );
					disableSyncFromListToChartHolder.value = false;
				}
			}
		};
		final Animator animator = new Animator( replay, chartCanvasRepainterListener, layeredPane, chartParamsHolder, markerXHolder, markerFrameHolder, syncActionListToFrameTask );
		addInternalFrameListener( new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing( final InternalFrameEvent event ) {
				// If the analyzer is closed, Animator has to be stopped
				animator.requestToCancel();
			}
		} );
		chartCanvas.addMouseListener( new MouseAdapter() {
			private boolean enlarged = false;
			private double  storedDividerLocation;
			public void mousePressed( final MouseEvent event ) {
				if ( event.getButton() == GuiUtils.MOUSE_BUTTON_RIGHT ) {
					// Replay operations popup menu
					final JPopupMenu replayOperationsPopupMenu;
					if ( replayFile != null ) {
						replayOperationsPopupMenu = new ReplayOperationsPopupMenu( new File[] { replayFile }, new ReplayOpCallback() {
							@Override
							public void replayRenamed( final File file, final File newFile, final int fileIndex ) {
								replayFile = newFile;
							}
							@Override
							public void replayMoved( final File file, final File targetFolder, final int fileIndex ) {
								replayFile = new File( targetFolder, file.getName() );
							}
							@Override
							public void replayDeleted( final File file, final int fileIndex ) {
							}
							@Override
							public void moveRenameDeleteEnded() {
								setTitle( GeneralUtils.getFileNameWithoutExt( replayFile ) );
								MainFrame.INSTANCE.refreshRepAnalNavigationNode();
							}
						} );
						replayOperationsPopupMenu.addSeparator();
					}
					else
						replayOperationsPopupMenu = new JPopupMenu();
					final JCheckBoxMenuItem enlargeChartMenuItem = new JCheckBoxMenuItem( Language.getText( "module.repAnalyzer.tab.charts.chartMenu.enlargeChart" ), enlarged ? Icons.APPLICATION_RESIZE_ACTUAL : Icons.APPLICATION_RESIZE_FULL, enlarged );
					enlargeChartMenuItem.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed( final ActionEvent event ) {
							handleChartEnlarge();
						}
					} );
					replayOperationsPopupMenu.add( enlargeChartMenuItem );
					replayOperationsPopupMenu.show( event.getComponent(), event.getX(), event.getY() );
				}
				else if ( event.getButton() == GuiUtils.MOUSE_BUTTON_LEFT ) {
					if ( chartTypeComboBox.getSelectedItem() != ChartType.MAP_VIEW && chartParamsHolder.value != null ) {
						markerXHolder    .value = Math.max( ChartParams.Y_AXIS_X_CONST, event.getX() );
						markerFrameHolder.value = null;
						syncActionListToFrameTask.execute( chartParamsHolder.value.xToFrame( markerXHolder.value ) );
						chartCanvasRepainterListener.actionPerformed( null );
						animator.setCurrentFrame( chartParamsHolder.value.xToFrame( markerXHolder.value ) );
					}
				}
				else if ( event.getButton() == GuiUtils.MOUSE_BUTTON_MIDDLE ) {
					handleChartEnlarge();
				}
			}
			private void handleChartEnlarge() {
				enlarged = !enlarged;
				if ( enlarged )
					storedDividerLocation = chartSplitPane.getHeight() == 0 ? 1 : (double) chartSplitPane.getDividerLocation() / chartSplitPane.getHeight();
				topBox.setVisible( !enlarged );
				topBox.getParent().validate();
				bottomPanel.setVisible( !enlarged );
				if ( !enlarged )
					chartSplitPane.setDividerLocation( (int) ( chartSplitPane.getHeight() * storedDividerLocation ) );
			}
		} );
		
		chartCanvasScrollPane.setViewportView( chartCanvas );
		chartSplitPane.setTopComponent( chartCanvasScrollPane );
		
		final ActionListener zoomComboBoxActionListener =  new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final float hPos = (float) chartCanvasScrollPane.getHorizontalScrollBar().getValue() / chartCanvasScrollPane.getHorizontalScrollBar().getMaximum();
				final float vPos = (float) chartCanvasScrollPane.getVerticalScrollBar  ().getValue() / chartCanvasScrollPane.getVerticalScrollBar  ().getMaximum();
				
				final int zoom = Integer.parseInt( ( (String) zoomComboBox.getSelectedItem() ).substring( 1 ) );
				zoomLabel.setBackground( zoom == 1 ? null : Color.GREEN );
				if ( chartTypeComboBox.getSelectedItem() == ChartType.MAP_VIEW ) {
					chartCanvasScrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
					chartCanvasScrollPane.setVerticalScrollBarPolicy  ( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS   );
				}
				else {
					chartCanvasScrollPane.setHorizontalScrollBarPolicy( zoom > 1 ? JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS : JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
					chartCanvasScrollPane.setVerticalScrollBarPolicy  ( JScrollPane.VERTICAL_SCROLLBAR_NEVER );
				}
				chartCanvas.invalidate();
				chartCanvasScrollPane.validate();
				
				chartCanvasScrollPane.getHorizontalScrollBar().setValue( (int) (chartCanvasScrollPane.getHorizontalScrollBar().getMaximum() * hPos) );
				chartCanvasScrollPane.getVerticalScrollBar  ().setValue( (int) (chartCanvasScrollPane.getVerticalScrollBar  ().getMaximum() * vPos) );
			}
		};
		zoomComboBoxActionListener.actionPerformed( null );
		zoomComboBox.addActionListener( zoomComboBoxActionListener );
		chartTypeComboBox.addActionListener( new ActionListener() {
			private boolean lastWasMapView = chartTypeComboBox.getSelectedItem() == ChartType.MAP_VIEW;
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final boolean currentIsMapView = chartTypeComboBox.getSelectedItem() == ChartType.MAP_VIEW;
				// Map view chart has a different scroll bar policy. If we change to or from Map view, we have to update that.
				if ( currentIsMapView ^ lastWasMapView )
					zoomComboBoxActionListener.actionPerformed( null );
				lastWasMapView = currentIsMapView;
			}
		} );
		
		// Zoom in and out with CTRL+wheel scroll:
		chartCanvas.addMouseWheelListener( new MouseWheelListener() {
			@Override
			public void mouseWheelMoved( final MouseWheelEvent event ) {
				if ( event.isControlDown() ) {
					int newZoomIndex = zoomComboBox.getSelectedIndex() - event.getWheelRotation();
					if ( newZoomIndex < 0 )
						newZoomIndex = 0;
					if ( newZoomIndex >= zoomComboBox.getItemCount() )
						newZoomIndex = zoomComboBox.getItemCount() - 1;
					zoomComboBox.setSelectedIndex( newZoomIndex );
				}
			}
		} );
		
		Box hbox = Box.createHorizontalBox();
		if ( replay.gameEvents == null || replay.gameEvents.errorParsing ) {
			final JLabel errorParsingLabel = new JLabel( Language.getText( "module.repAnalyzer.tab.charts.parsingError" ), Icons.COMPILE_ERROR, SwingConstants.LEFT );
			errorParsingLabel.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.parsingErrorToolTip" ) );
			GuiUtils.changeFontToBold( errorParsingLabel );
			errorParsingLabel.setForeground( Color.RED );
			hbox.add( errorParsingLabel );
			hbox.add( Box.createHorizontalStrut( 7 ) );
		}
		hbox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.displayActionTypes" ) ) );
		hbox.add( Box.createHorizontalStrut( 5 ) );
		for ( final ActionType actionType : ActionType.values() ) {
			final JCheckBox actionTypeCheckBox = GuiUtils.createCheckBox( actionType.textKey, Settings.KEY_REP_ANALYZER_CHARTS_ACTIONS_ACTION_TYPES + actionType.ordinal() );
			actionTypeCheckBox.addActionListener( actionsListRebuilderActionListener );
			displayActionTypeCheckboxMap.put( actionType, actionTypeCheckBox );
			hbox.add( actionTypeCheckBox );
			hbox.add( Box.createHorizontalStrut( 5 ) );
		}
		hbox.add( new JPanel( new BorderLayout() ) );
		hbox.add( animator.getControlUIComponent() );
		bottomPanel.add( hbox, BorderLayout.NORTH );
		actionsList.addListSelectionListener( new ListSelectionListener() {
			@Override
			public void valueChanged( final ListSelectionEvent event ) {
				if ( !event.getValueIsAdjusting() && actionsList.getSelectedIndex() >= 0 && !disableSyncFromListToChartHolder.value ) {
					markerXHolder.value = null;
					final int x = markerFrameHolder.value = ( selectedActionHolder.value = displayableActionsHolder.value[ actionsList.getSelectedIndex() ] ).frame;
					animator.setCurrentFrame( markerFrameHolder.value );
					// If marker is not visible on chart, scroll to it
					if ( chartParamsHolder.value != null ) {
						final Rectangle visibleRectangle = chartParamsHolder.value.visibleRectangle;
						if ( chartParamsHolder.value.chartType == ChartType.MAP_VIEW ) {
							final Action selectedAction = (Action) actionsList.getSelectedValue();
							if ( selectedAction instanceof BaseUseAbilityAction ) {
								final BaseUseAbilityAction buaa = (BaseUseAbilityAction) selectedAction;
								if ( buaa.hasTargetPoint() ) {
									final int tx = chartParamsHolder.value.scaleMapCoord( buaa.targetX );
									final int ty = ( replay.mapInfo.height * chartParamsHolder.value.zoom << 1 ) - chartParamsHolder.value.scaleMapCoord( buaa.targetY );
									if ( !visibleRectangle.contains( tx, ty ) ) {
										final int radius = Math.min( Math.min( 10 * chartParamsHolder.value.zoom, visibleRectangle.width ), visibleRectangle.height );
										chartCanvas.scrollRectToVisible( new Rectangle( tx - radius, ty - radius, radius << 1, radius << 1 ) );
										chartCanvasScrollPane.repaint();
									}
								}
							}
						}
						else {
							if ( ( x < visibleRectangle.x || x >= visibleRectangle.x + visibleRectangle.width ) )
								chartCanvas.scrollRectToVisible( new Rectangle( chartParamsHolder.value.frameToX( x ) - ( visibleRectangle.width >> 2 ), 0, visibleRectangle.width >> 1, 1 ) );
						}
					}
					chartCanvasRepainterListener.actionPerformed( null );
				}
			}
		} );
		actionsList.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 12 ) ); // Set a fixed-width font
		final JCheckBox invertListColorsCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.charts.actions.invertColors", Settings.KEY_REP_ANALYZER_CHARTS_ACTIONS_INVERT_COLORS );
		final JComboBox< IconSize > actionIconSizesComboBox = GuiUtils.createComboBox( EnumCache.ICON_SIZES, Settings.KEY_REP_ANALYZER_CHARTS_ACTIONS_ICON_SIZE );
		actionsList.setCellRenderer( new ListCellRenderer< Action >() {
			private final JLabel label = new JLabel();
			{ label.setOpaque( true ); }
			@Override
			public Component getListCellRendererComponent( final JList< ? extends Action > list, final Action action, final int index, final boolean isSelected, final boolean cellHasFocus ) {
				final IconSize size = (IconSize) actionIconSizesComboBox.getSelectedItem();
				
				if ( size == IconSize.HIDDEN )
					label.setIcon( null );
				else if ( action instanceof SelectAction )
					label.setIcon( Icons.getCustomEnlargedIcon( "fugue/selection-select.png", size ) );
				else if ( action instanceof HotkeyAction )
					label.setIcon( Icons.getCustomEnlargedIcon( ( (HotkeyAction) action).isSelect() ? "fugue/selection-select.png" : "fugue/keyboard.png", size ) );
				else if ( action instanceof MoveScreenAction )
					label.setIcon( Icons.getCustomEnlargedIcon( "fugue/camera-lens.png", size ) );
				else if ( action instanceof UseUnitAbilityAction )
					label.setIcon( Icons.getAbilityGroupIcon( ( ( (UseUnitAbilityAction) action ).unitAbility ).abilityGroup, size ) );
				else if ( action instanceof TrainAction )
					label.setIcon( Icons.getUnitIcon( ( (TrainAction) action ).unit, size ) );
				else if ( action instanceof UseBuildingAbilityAction )
					label.setIcon( Icons.getAbilityGroupIcon( ( ( (UseBuildingAbilityAction) action ).buildingAbility ).abilityGroup, size ) );
				else if ( action instanceof BuildAction )
					label.setIcon( Icons.getBuildingIcon( ( (BuildAction) action ).building, size ) );
				else if ( action instanceof UpgradeAction )
					label.setIcon( Icons.getUpgradeIcon( ( (UpgradeAction) action ).upgrade, size ) );
				else if ( action instanceof ResearchAction )
					label.setIcon( Icons.getResearchIcon( ( (ResearchAction) action ).research, size ) );
				else if ( action instanceof BaseUseAbilityAction && ( (BaseUseAbilityAction) action ).abilityGroup != null )
					label.setIcon( Icons.getAbilityGroupIcon( ( (BaseUseAbilityAction) action ).abilityGroup, size ) );
				else if ( action instanceof BaseUseAbilityAction && "Right click".equals( ( (BaseUseAbilityAction) action ).abilityName ) )
					label.setIcon( Icons.getCustomEnlargedIcon( "fugue/mouse-select-right.png", size ) );
				else if ( action instanceof RequestResoucesAction || action instanceof SendResourcesAction )
					label.setIcon( Icons.getCustomEnlargedIcon( "sc2/misc/resources.png", size ) );
				else
					label.setIcon( IconHandler.NULL.get( size ) );
				
				label.setText( action.toString() );
				
				if ( isSelected ^ invertListColorsCheckBox.isSelected() ) {
					label.setBackground( usePlayersColorsCheckBox.isSelected() ? replay.details.players[ action.player ].getDarkerColor() : list.getSelectionBackground() );
					label.setForeground( usePlayersColorsCheckBox.isSelected() ? Color.WHITE : list.getSelectionForeground() );
				}
				else {
					label.setBackground( list.getBackground() );
					label.setForeground( usePlayersColorsCheckBox.isSelected() ? replay.details.players[ action.player ].getDarkerColor() : list.getForeground() );
				}
				label.setEnabled( list.isEnabled() );
				label.setFont( list.getFont() );
				return label;
			}
		} );
		bottomPanel.add( new JScrollPane( actionsList ), BorderLayout.CENTER );
		final Box optionsWrapperBox = Box.createVerticalBox();
		JPanel actionsOpsPanel = new JPanel( new GridLayout( 4, 2, 0, 0 ) );
		actionsOpsPanel.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.actionOps.jumpToFrame" ) ) );
		final JTextField jumpToFrameTextField = new JTextField( 1 );
		jumpToFrameTextField.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.actionOps.jumpToFrameToolTip" ) );
		jumpToFrameTextField.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				if ( displayableActionsHolder.value.length > 0 )
					try {
						final int toFrame = Integer.parseInt( jumpToFrameTextField.getText() );
						final Action testAction = replay.gameEvents.new Action();
						testAction.frame = toFrame;
						final int position = Arrays.binarySearch( displayableActionsHolder.value, testAction, ReplayUtils.ACTION_FRAME_COMPARATOR );
						final int index    = position >= 0 ? position : -position <= displayableActionsHolder.value.length ? -position - 1 : -position - 2;
						actionsList.setSelectedIndex( index ); // This will also scroll and update the chart!
						actionsList.ensureIndexIsVisible( index );
						chartCanvasRepainterListener.actionPerformed( null );
					} catch ( final Exception e ) {
					}
			}
		} );
		actionsOpsPanel.add( jumpToFrameTextField );
		actionsOpsPanel.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.actionOps.searchText" ) ) );
		final JComboBox< String > searchTextComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.REP_ANAL_SEARCH );
		searchTextComboBox.setPreferredSize( new Dimension( 1, 1 ) );
		searchTextComboBox.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.actionOps.searchTextToolTip" ) );
		searchTextComboBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final String searchText = searchTextComboBox.getSelectedItem() == null ? "" : searchTextComboBox.getSelectedItem().toString().trim().toLowerCase();
				if ( displayableActionsHolder.value.length > 0 && !searchText.isEmpty() ) {
					final Action[] actionList = displayableActionsHolder.value;
					final int startIndex = actionsList.getSelectedIndex() < 0 ? actionList.length - 1 : actionsList.getSelectedIndex();
					int i = startIndex;
					do {
						if ( ++i == actionList.length )
							i = 0;
						if ( actionList[ i ].toString().toLowerCase().contains( searchText ) ) {
							actionsList.setSelectedIndex( i ); // This will also scroll and update the chart!
							actionsList.ensureIndexIsVisible( i );
							chartCanvasRepainterListener.actionPerformed( null );
							break;
						}
					} while ( i != startIndex );
				}
			}
		} );
		actionsOpsPanel.add( searchTextComboBox );
		final JLabel filterActionsLabel = new JLabel( Language.getText( "module.repAnalyzer.tab.charts.actionOps.filterActions" ) );
		filterActionsLabel.setOpaque( true ); // Needed for the background color to take effect
		actionsOpsPanel.add( filterActionsLabel  );
		final JComboBox< String > filterActionsTextComboBox    = GuiUtils.createPredefinedListComboBox( PredefinedList.REP_ANAL_FILTER     );
		final JComboBox< String > filterOutActionsTextComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.REP_ANAL_FILTER_OUT );
		filterActionsTextComboBox.setPreferredSize( new Dimension( 1, 1 ) );
		filterActionsTextComboBox.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.actionOps.filterActionsToolTip" ) );
		final ActionListener filterActionsComboBoxActionListener =  new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				actionListFilterHolder.value = new TextFilter( (String) filterActionsTextComboBox.getSelectedItem(), (String) filterOutActionsTextComboBox.getSelectedItem() );
				filterActionsLabel.setBackground( actionListFilterHolder.value.isIncludeFilterActive() ? Color.GREEN : null );
				if ( event != null )
					actionsListRebuilderActionListener.actionPerformed( null );
			}
		};
		filterActionsTextComboBox.addActionListener( filterActionsComboBoxActionListener );
		actionsOpsPanel.add( filterActionsTextComboBox );
		final JLabel filterOutActionsLabel = new JLabel( Language.getText( "module.repAnalyzer.tab.charts.actionOps.filterOutActions" ) );
		filterOutActionsLabel.setOpaque( true ); // Needed for the background color to take effect
		actionsOpsPanel.add( filterOutActionsLabel  );
		filterOutActionsTextComboBox.setPreferredSize( new Dimension( 1, 1 ) );
		filterOutActionsTextComboBox.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.actionOps.filterOutActionsToolTip" ) );
		final ActionListener filterOutActionsTextFieldActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				actionListFilterHolder.value = new TextFilter( (String) filterActionsTextComboBox.getSelectedItem(), (String) filterOutActionsTextComboBox.getSelectedItem() );
				filterOutActionsLabel.setBackground( actionListFilterHolder.value.isExcludeFilterActive() ? Color.GREEN : null );
				if ( event != null )
					actionsListRebuilderActionListener.actionPerformed( null );
			}
		};
		filterOutActionsTextComboBox.addActionListener( filterOutActionsTextFieldActionListener );
		actionsOpsPanel.add( filterOutActionsTextComboBox );
		optionsWrapperBox.add( actionsOpsPanel );
		// I put the clear filter button on another panel, because the button's height is significantly bigger than the text field's.
		actionsOpsPanel = new JPanel( new GridLayout( 1, 2, 0, 0 ) );
		actionsOpsPanel.add( new JLabel() );
		final JButton clearFiltersButton = new JButton();
		GuiUtils.updateButtonText( clearFiltersButton, "module.repAnalyzer.tab.charts.actionOps.clearFiltersButton" );
		clearFiltersButton.putClientProperty( "JComponent.sizeVariant", "small" );
		clearFiltersButton.updateUI();
		clearFiltersButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				filterActionsTextComboBox              .setSelectedItem( "" );
				filterOutActionsTextComboBox           .setSelectedItem( "" );
				filterActionsComboBoxActionListener    .actionPerformed( null );
				filterOutActionsTextFieldActionListener.actionPerformed( null );
				actionsListRebuilderActionListener     .actionPerformed( null );
			}
		} );
		actionsOpsPanel.add( clearFiltersButton );
		optionsWrapperBox.add( actionsOpsPanel );
		optionsWrapperBox.add( new JPanel( new BorderLayout() ) ); // To consume the remaining space
		optionsWrapperBox.setMinimumSize( new Dimension( 10, 10 ) );
		bottomPanel.add( optionsWrapperBox, BorderLayout.EAST );
		final Box listSettingsBox = Box.createHorizontalBox();
		listSettingsBox.add( listedActionsCountLabel );
		listSettingsBox.add( Box.createHorizontalStrut( 12 ) );
		invertListColorsCheckBox.addActionListener( actionsListRepainterListener );
		listSettingsBox.add( invertListColorsCheckBox );
		listSettingsBox.add( Box.createHorizontalStrut( 12 ) );
		listSettingsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.actions.iconSizes" ) ) );
		actionIconSizesComboBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				actionsList.updateUI();
			}
		} );
		listSettingsBox.add( actionIconSizesComboBox );
		listSettingsBox.add( Box.createHorizontalStrut( 12 ) );
		useListedActionsAsInputCheckBox.setOpaque( true );
		useListedActionsAsInputCheckBox.addActionListener( new ActionListener() {
			{ actionPerformed( null ); } // Set the initial background color
			@Override
			public void actionPerformed( final ActionEvent event ) {
				useListedActionsAsInputCheckBox.setBackground( useListedActionsAsInputCheckBox.isSelected() ? Color.GREEN : null );
			}
		} );
		useListedActionsAsInputCheckBox.addActionListener( chartCanvasRepainterListener );
		listSettingsBox.add( useListedActionsAsInputCheckBox );
		listSettingsBox.add( new JPanel( new BorderLayout() ) ); // To consume the remaining space
		listSettingsBox.add( Box.createHorizontalStrut( 5 ) );
		final JLabel analyzerSettingsLabel = MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.REPLAY_ANALYZER );
		analyzerSettingsLabel.setHorizontalAlignment( SwingConstants.RIGHT );
		listSettingsBox.add( analyzerSettingsLabel );
		final JLabel predefinedListsSettingsLabel = MiscSettingsDialog.createLinkLabelToPredefinedListsSettings( PredefinedList.REP_ANAL_SEARCH );
		predefinedListsSettingsLabel.setHorizontalAlignment( SwingConstants.RIGHT );
		listSettingsBox.add( Box.createHorizontalStrut( 5 ) );
		listSettingsBox.add( predefinedListsSettingsLabel );
		bottomPanel.add( listSettingsBox, BorderLayout.SOUTH );
		chartSplitPane.setBottomComponent( bottomPanel );
		
		chartsPanel.add( chartSplitPane );
		final double resizeWeight = Settings.getInt( Settings.KEY_SETTINGS_MISC_CHARTS_ACTION_LIST_PARTITIONING ) / 100.0;
		chartSplitPane.setResizeWeight( resizeWeight );
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				chartSplitPane.setDividerLocation( resizeWeight );
			}
		} );
		
		actionsListRebuilderActionListener.actionPerformed( null );
		
		// Register hotkeys for chart types
		final InputMap  inputMap  = layeredPane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		final ActionMap actionMap = layeredPane.getActionMap();
		Object actionKey;
		// Register chart hotkeys
		for ( final ChartType chartType : ChartType.values() )
			if ( chartType.keyStroke != null ) {
				// Activate chart hotkeys
				inputMap .put( chartType.keyStroke, actionKey = new Object() );
				actionMap.put( actionKey, new AbstractAction() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						chartTypeComboBox.setSelectedItem( chartType );
					}
				} );
				// Open overlay chart hotkeys
				inputMap .put( KeyStroke.getKeyStroke( chartType.keyStroke.getKeyCode(), chartType.keyStroke.getModifiers() | InputEvent.SHIFT_MASK ), actionKey = new Object() );
				actionMap.put( actionKey, new AbstractAction() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						OverlayChartFrame.invertOverlayChart( chartType, overlayChartFrameList, layeredPane, chartCanvasScrollPane );
					}
				} );
			}
		// Register hotkeys for changing zoom
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_I, InputEvent.CTRL_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				if ( zoomComboBox.getSelectedIndex() < zoomComboBox.getItemCount() - 1 )
					zoomComboBox.setSelectedIndex( zoomComboBox.getSelectedIndex() + 1 );
			}
		} );
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_U, InputEvent.CTRL_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				if ( zoomComboBox.getSelectedIndex() > 0 )
					zoomComboBox.setSelectedIndex( zoomComboBox.getSelectedIndex() - 1 );
			}
		} );
		// Register hotkeys for turning the Grid on/off
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_G, InputEvent.CTRL_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				gridCheckBox.doClick( 0 );
			}
		} );
		// Register hotkeys for opening the Grid settings dialog
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_G, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				new GridSettingsDialog( gridParams, chartParamsHolder.value, chartCanvasRepainterListener );
			}
		} );
		// Register hotkeys for action operations
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_J, InputEvent.CTRL_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				jumpToFrameTextField.requestFocusInWindow();
			}
		} );
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				searchTextComboBox.requestFocusInWindow();
			}
		} );
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_F, InputEvent.CTRL_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				filterActionsTextComboBox.requestFocusInWindow();
			}
		} );
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_T, InputEvent.CTRL_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				filterOutActionsTextComboBox.requestFocusInWindow();
			}
		} );
		
		return chartsPanel;
	}
	
	/**
	 * Creates and returns the game info tab.
	 * @return the game info tab
	 */
	private JComponent createGameInfoTab() {
		final JPanel gameInfoPanel = new JPanel( new BorderLayout() );
		
		gameInfoPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
				final JTextArea gameInfoTextArea = new JTextArea();
				gameInfoTextArea.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 12 ) ); // Set a fixed-width font
				gameInfoTextArea.setEditable( false );
				gameInfoTextArea.setTabSize( 4 );
				
				final List< String > lineList = new  ArrayList< String >( 16 );
				if ( replayFile != null ) {
					lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.fileName", replayFile.getAbsolutePath() ) );
					lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.fileSize", String.format( Locale.ENGLISH, "%,d", replayFile.length() ) ) );
				}
				lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.expansion", replay.details.expansion.fullStringValue ) );
				lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.version", replay.version ) );
				lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.date", Language.formatDateTime( new Date( replay.details.saveTime ) ) ) );
				lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.recorderTimeZone", String.format( Locale.US, "%+.2f", replay.details.saveTimeZone ) ) );
				lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.gameLength", ReplayUtils.formatMs( replay.gameLength * 500, replay.converterGameSpeed ) ) );
				lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.gameSpeed", replay.initData.gameSpeed ) );
				lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.gameType", replay.initData.gameType ) );
				if ( replay.initData.competitive != null )
					lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.isCompetitive", Language.getText( replay.initData.competitive ? "general.yes" : "general.no" ) ) );
				if ( replay.initData.gameType == GameType.AMM )
					lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.ladderSeason", LadderSeason.getByDate( new Date( replay.details.saveTime ), replay.initData.gateway ) ) );
				lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.format", replay.initData.format ) );
				lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.gateway", replay.initData.gateway ) );
				
				final StringBuilder clientNamesBuilder = new StringBuilder();
				for ( final String clientName : replay.initData.getArrangedClientNames( replay.details.players ) )
					if ( !clientName.isEmpty() ) { // There might be empty client names 
						if ( clientNamesBuilder.length() > 0 )
							clientNamesBuilder.append( ", " );
						clientNamesBuilder.append( clientName );
					}
				lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.clients", clientNamesBuilder ) );
				
				lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.mapName", replay.details.mapName == replay.details.originalMapName ? replay.details.mapName : replay.details.mapName + " (" + replay.details.originalMapName + ")" ) );
				
				if ( replay.initData.mapFileName != null )
					lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.mapFile", replay.initData.mapFileName ) );
				if ( replay.mapInfo != null ) {
					lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.mapSize", replay.mapInfo.getSizeString() ) );
					lineList.add( Language.getText( "module.repAnalyzer.tab.gameInfo.mapPlayableSize", replay.mapInfo.getPlayableSizeString() ) );
				}
				
				// Align lines to the max pos of ':'
				int alignPos = 0, colonPos;
				for ( final String line : lineList )
					if ( ( colonPos = line.indexOf( ':' ) ) > alignPos )
						alignPos = colonPos;
				for ( final String line : lineList ) {
					colonPos = line.indexOf( ':' ) + 1;
					GuiUtils.appendNewLine( gameInfoTextArea, String.format( "%-" + (alignPos+1) + "s%s", line.substring( 0, colonPos ), line.substring( colonPos ) ) );
				}
				
				gameInfoTextArea.append( "\n" );
				GuiUtils.appendNewLine( gameInfoTextArea, Language.getText( "module.repAnalyzer.tab.gameInfo.players" ) );
				int lastTeam = -1;
				final List< List< String[] > > teamList = new ArrayList< List< String[] > >( 2 );
				List< String[] > playerDetailsList = null;
				for ( final int playerIndex : replay.details.getTeamOrderPlayerIndices() ) {
					final Player player = replay.details.players[ playerIndex ];
					if ( player.team != lastTeam ) {
						teamList.add( playerDetailsList = new ArrayList< String[] >( 4 ) );
						playerDetailsList.add( new String[] { Language.getText( "module.repAnalyzer.tab.gameInfo.team", player.team == Player.TEAM_UNKNOWN ? Language.getText( "general.unknown" ) : player.team ) } );
						lastTeam = player.team;
					}
					final String[] playerDetails = new String[ player.handicap < 100 ? 9 : 8 ];
					playerDetailsList.add( playerDetails );
					int i = 0;
					playerDetails[ i++ ] = player.playerId.name;
					playerDetails[ i++ ] = player.getLeague().stringValue;
					playerDetails[ i++ ] = Language.getText( "module.repAnalyzer.tab.gameInfo.levels"      , player.getSwarmLevels() );
					playerDetails[ i++ ] = player.getRaceString();
					playerDetails[ i++ ] = Language.getText( "module.repAnalyzer.tab.charts.chartText.apm" , ReplayUtils.calculatePlayerApm ( replay, player ) );
					playerDetails[ i++ ] = Language.getText( "module.repAnalyzer.tab.charts.chartText.eapm", ReplayUtils.calculatePlayerEapm( replay, player ) );
					playerDetails[ i   ] = player.type.stringValue;
					if ( player.type == PlayerType.COMPUTER )
						playerDetails[ i++ ] += " (" + player.difficulty.stringValue + ")";
					else
						playerDetails[ i++ ] += " (" + player.playerId.getFullName() + ")";
					playerDetails[ i++ ] = player.getColorName();
					if ( player.handicap < 100 ) {
						playerDetails[ i++ ] = Language.getText( "module.repAnalyzer.tab.gameInfo.handicap", player.handicap );
					}
				}
				final int[] columnWidths = new int[ 8 ];
				for ( final List< String[] > team : teamList )
					for ( int i = 1; i < team.size(); i++ ) {
						final String[] playerDetails = team.get( i );
						for ( int j = columnWidths.length - 1; j >= 0; j-- )
							if ( playerDetails[ j ].length() > columnWidths[ j ] )
								columnWidths[ j ] = playerDetails[ j ].length();
					}
				for ( final List< String[] > team : teamList ) {
					gameInfoTextArea.append( "\n\t" );
					GuiUtils.appendNewLine( gameInfoTextArea, team.get( 0 )[ 0 ] );
					for ( int i = 1; i < team.size(); i++ ) {
						gameInfoTextArea.append( "\t\t" );
						final String[] playerDetails = team.get( i );
						for ( int j = 0; j < playerDetails.length; j++ )
							if ( j == playerDetails.length - 1 )
								GuiUtils.appendNewLine( gameInfoTextArea, playerDetails[ j ] );
							else
								gameInfoTextArea.append( String.format( "%-" + columnWidths[ j ] + "s, ", playerDetails[ j ] ) );
					}
				}
				
				if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_SHOW_WINNERS ) ) {
					gameInfoTextArea.append( "\n" );
					GuiUtils.appendNewLine( gameInfoTextArea, Language.getText( "module.repAnalyzer.tab.gameInfo.winners", replay.details.getWinnerNames() ) );
				}
				
				gameInfoTextArea.setCaretPosition( 0 );
				gameInfoPanel.add( new JScrollPane( gameInfoTextArea ) );
			}
		} );
		
		return gameInfoPanel;
	}
	
	/**
	 * Creates and returns the in-game chat tab.
	 * @return the in-game chat tab
	 */
	private JComponent createInGameChatTab() {
		final JPanel chatPanel = new JPanel( new BorderLayout() );
		
		chatPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
				final StyleContext STYLE_CONTEX = new StyleContext();
				final Style defaultStyle = STYLE_CONTEX.addStyle( "default", StyleContext.getDefaultStyleContext().getStyle( StyleContext.DEFAULT_STYLE ) );
				
				int counter = 0;
				for ( final Player player : replay.details.players ) {
					final Style style = STYLE_CONTEX.addStyle( "p" + counter, defaultStyle );
					StyleConstants.setForeground( style, player.getDarkerColor() );
					counter++;
				}
				
				
				final Box optionsBox = Box.createVerticalBox();
				
				final Box generalOptionsBox = Box.createHorizontalBox();
				final JTextPane chatTextPane = new JTextPane() {
					@Override
					public void paint( final Graphics graphics ) {
						Component comp = this;
						while ( !( comp instanceof JViewport ) )
							comp = comp.getParent();
						final Rectangle viewRect = ( (JViewport) comp ).getViewRect();
						graphics.setColor( new Color( 240, 240, 240 ) );
						graphics.fillRect( viewRect.x, viewRect.y, viewRect.width, viewRect.height );
						
						super.paint( graphics );
					}
				};
				// Nimbus LAF ignores the component.getBackground()
				// Needed for the custom background
				chatTextPane.setOpaque( false );
				chatTextPane.setBackground( new Color( 0, 0, 0, 0 ) );
				
				final JCheckBox hideMessageTargetsCheckBox   = GuiUtils.createCheckBox( "module.repAnalyzer.tab.inGameChat.hideMessageTarget"   , Settings.KEY_REP_ANALYZER_IN_GAME_CHAT_HIDE_MESSAGE_TARGETS );
				final JCheckBox showBlinksCheckBox	         = GuiUtils.createCheckBox( "module.repAnalyzer.tab.inGameChat.showBlinks"		  , Settings.KEY_REP_ANALYZER_IN_GAME_CHAT_SHOW_BLINKS		  );
				final JCheckBox formatIntoParagraphsCheckBox = GuiUtils.createCheckBox( "module.repAnalyzer.tab.inGameChat.formatIntoParagraphs", Settings.KEY_REP_ANALYZER_IN_GAME_CHAT_FORMAT_INTO_PARAGRAPHS    );
				
				final Holder< String[] > translatedMessagesHolder = new Holder< String[] >();
				
				final ActionListener chatActionListener = new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						final boolean hideMessageTargets  = hideMessageTargetsCheckBox.isSelected();
						final boolean showBlinks		  = showBlinksCheckBox.isSelected();
						final boolean emptyLinesForBreaks = formatIntoParagraphsCheckBox.isSelected();
						
						final String allText     = hideMessageTargets ? null : Language.getText( "module.repAnalyzer.tab.inGameChat.messageTargetAll"       );
						final String alliesText  = hideMessageTargets ? null : Language.getText( "module.repAnalyzer.tab.inGameChat.messageTargetAllies"    );
						final String obsText     = hideMessageTargets ? null : Language.getText( "module.repAnalyzer.tab.inGameChat.messageTargetObservers" );
						final String unknownText = hideMessageTargets ? null : Language.getText( "module.repAnalyzer.tab.inGameChat.messageTargetUnknown"   );
						
						chatTextPane.setText( "" );
						
						int ms = 0, lastMs;
						final Document document = chatTextPane.getDocument();
						
						final String[] arrangedClientNames = replay.initData.getArrangedClientNames( replay.details.players );
						
						int textMessageCounter = 0;
						for ( final MessageEvents.Message message : replay.messageEvents.messages ) {
							lastMs = ms;
							String outputMessage = null;
							final String playerName = arrangedClientNames[ message.client ];
							
							ms += message.time;
							if ( message instanceof MessageEvents.Text ) {
								final MessageEvents.Text textMessage = (MessageEvents.Text) message;
								final String text = translatedMessagesHolder.value == null ? textMessage.text : translatedMessagesHolder.value[ textMessageCounter++ ];
								if ( hideMessageTargets )
									outputMessage = Language.getText( "module.repAnalyzer.tab.inGameChat.chatMessageNoTarget", ReplayUtils.formatMs( ms, replay.converterGameSpeed ), playerName, text );
								else
									outputMessage = Language.getText( "module.repAnalyzer.tab.inGameChat.chatMessageFull", ReplayUtils.formatMs( ms, replay.converterGameSpeed ), playerName, textMessage.opCode == MessageEvents.OP_CODE_CHAT_TO_ALL ? allText : textMessage.opCode == MessageEvents.OP_CODE_CHAT_TO_ALLIES ? alliesText : textMessage.opCode == MessageEvents.OP_CODE_CHAT_TO_OBSERVERS ? obsText : unknownText, text );
							}
							else if ( message instanceof MessageEvents.Blink ) {
								if ( showBlinks ) {
									final MessageEvents.Blink blinkMessage = (MessageEvents.Blink) message;
									outputMessage = Language.getText( "module.repAnalyzer.tab.inGameChat.blinkMessage", ReplayUtils.formatMs( ms, replay.converterGameSpeed ), playerName, ReplayUtils.formatCoordinate( blinkMessage.x ), ReplayUtils.formatCoordinate( blinkMessage.y ) );
								}
							}
							
							if ( outputMessage != null ) {
								try {
									document.insertString( document.getLength(), 
											emptyLinesForBreaks && ms - lastMs > 6000 && lastMs > 0 ? "\n" + outputMessage + "\n" : outputMessage + "\n",
											STYLE_CONTEX.getStyle( "p" + message.client ) );
								} catch ( final BadLocationException be ) {
									be.printStackTrace();
								}
							}
						}
						chatTextPane.setCaretPosition( 0 );
					}
				};
				
				hideMessageTargetsCheckBox  .addActionListener( chatActionListener );
				showBlinksCheckBox		  .addActionListener( chatActionListener );
				formatIntoParagraphsCheckBox.addActionListener( chatActionListener );
				
				chatActionListener.actionPerformed( null );
				
				generalOptionsBox.add( hideMessageTargetsCheckBox );
				generalOptionsBox.add( Box.createHorizontalStrut( 10 ) );
				generalOptionsBox.add( showBlinksCheckBox );
				generalOptionsBox.add( Box.createHorizontalStrut( 10 ) );
				generalOptionsBox.add( formatIntoParagraphsCheckBox );
				
				optionsBox.add( generalOptionsBox );
				
				final Box translateBox = Box.createHorizontalBox();
				final JButton restoreOriginalButton = new JButton( Icons.ARROW_CURVE_180 );
				GuiUtils.updateButtonText( restoreOriginalButton, "module.repAnalyzer.tab.inGameChat.restoreOriginalButton" );
				restoreOriginalButton.setEnabled( false );
				restoreOriginalButton.updateUI();
				restoreOriginalButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						translatedMessagesHolder.value = null;
						restoreOriginalButton.setEnabled( false );
						// Required due to the Napkin LAF:
						SwingUtilities.invokeLater( new Runnable() {
							@Override
							public void run() {
								restoreOriginalButton.updateUI();
							}
						} );
						chatActionListener.actionPerformed( null );
					}
				} );
				translateBox.add( restoreOriginalButton );
				final JComboBox< hu.belicza.andras.sc2gears.util.GoogleTranslator.Language > targetLanguageComboBox = GuiUtils.createComboBox( GeneralUtils.remainingElements( GoogleTranslator.Language.values(), EnumSet.of( GoogleTranslator.Language.AUTO_DETECT, GoogleTranslator.Language.UNKNOWN ) ), Settings.KEY_REP_ANALYZER_IN_GAME_CHAT_TARGET_LANGUAGE );
				final JComboBox< hu.belicza.andras.sc2gears.util.GoogleTranslator.Language > sourceLanguageComboBox = GuiUtils.createComboBox( GoogleTranslator.Language.values(), Settings.KEY_REP_ANALYZER_IN_GAME_CHAT_SOURCE_LANGUAGE );
				final JButton translateButton = new JButton( Icons.LOCALE );
				GuiUtils.updateButtonText( translateButton, "module.repAnalyzer.tab.inGameChat.translateButton" );
				translateButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						translateButton.setEnabled( false );
						translateButton.updateUI(); // Required due to the Napkin LAF
						// We don't want to block the event dispatcher:
						new NormalThread( "Translator" ) {
							@Override
							public void run() {
								final List< String > textList = new ArrayList< String >();
								for ( MessageEvents.Message message : replay.messageEvents.messages )
									if ( message instanceof MessageEvents.Text )
										textList.add( ( (MessageEvents.Text) message ).text );
								
								translatedMessagesHolder.value = GoogleTranslator.translateTexts( textList.toArray( new String[ textList.size() ] ), (GoogleTranslator.Language) sourceLanguageComboBox.getSelectedItem(), (GoogleTranslator.Language) targetLanguageComboBox.getSelectedItem() );
								restoreOriginalButton.setEnabled( translatedMessagesHolder.value != null );
								translateButton.setEnabled( true );
								chatActionListener.actionPerformed( null );
								// Required due to the Napkin LAF:
								SwingUtilities.invokeLater( new Runnable() {
									@Override
									public void run() {
										restoreOriginalButton.updateUI();
										translateButton.updateUI();
									}
								} );
							}
						}.start();
					}
				} );
				translateBox.add( translateButton );
				translateBox.add( Box.createHorizontalStrut( 5 ) );
				translateBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.inGameChat.targetLanguage" ) ) );
				targetLanguageComboBox.setMaximumRowCount( 12 );
				translateBox.add( targetLanguageComboBox );
				translateBox.add( Box.createHorizontalStrut( 5 ) );
				translateBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.inGameChat.sourceLanguage" ) ) );
				sourceLanguageComboBox.setMaximumRowCount( 12 );
				translateBox.add( sourceLanguageComboBox );
				optionsBox.add( translateBox );
				
				chatPanel.add( GuiUtils.wrapInPanel( optionsBox ), BorderLayout.NORTH );
				chatTextPane.setEditable( false );
				chatPanel.add( new JScrollPane( chatTextPane ), BorderLayout.CENTER );
			}
		} );
		
		return chatPanel;
	}
	
	/**
	 * Creates and returns the map preview tab.
	 * @return the map preview tab
	 */
	private JComponent createMapPreviewTab() {
		final JPanel mapPreviewPanel = new JPanel( new BorderLayout() );
		
		mapPreviewPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
				final JLabel mapPreviewLabel = new JLabel();
				
				final Box northBox = Box.createVerticalBox();
				final JLabel mapInfoLabel = new JLabel( Language.getText( "module.repAnalyzer.tab.gameInfo.mapName", replay.mapInfo == null ? replay.details.mapName : replay.details.mapName + " " + replay.mapInfo.getPlayableSizeString() + " (" + replay.mapInfo.getSizeString() + ")" ), JLabel.CENTER );
				GuiUtils.changeFontToBold( mapInfoLabel );
				northBox.add( GuiUtils.wrapInPanel( mapInfoLabel ) );
				if ( replay.mapInfo == null ) {
					final File   mapFile = MapParser.getMapFile( replay );
					final String mapFileName;
					// If it exists but we failed to open it, no need to download (again)...
					if ( mapFile != null && !mapFile.exists() && ( mapFileName = mapFile.getName() ).length() == 69 ) { // map name might not be a hash value, then do not offer downloading
						final Box downloadBox = Box.createVerticalBox();
						final Box controlBox = Box.createHorizontalBox();
						final JLabel downloadInfoLabel = new JLabel( Language.getText( "module.repAnalyzer.tab.mapPreview.mapFileNotExists" ), JLabel.CENTER );
						controlBox.add( downloadInfoLabel );
						controlBox.add( Box.createHorizontalStrut( 5 ) );
						final Box downloadSourceBox = Box.createHorizontalBox();
						final JComboBox< Gateway > downloadFromGatewayComboBox = GuiUtils.createComboBox( GeneralUtils.remainingElements( EnumCache.GATEWAYS, EnumSet.of( Gateway.UNKNOWN ) ), Settings.KEY_REP_ANALYZER_MAP_PREVIEW_DOWNLOAD_FROM_GATEWAY );
						final JButton downloadButton = new JButton( Icons.DRIVE_DOWNLOAD );
						GuiUtils.updateButtonText( downloadButton, "module.repAnalyzer.tab.mapPreview.downloadMapButton" );
						downloadButton.addActionListener( new ActionListener() {
							@Override
							public void actionPerformed( final ActionEvent event ) {
								downloadBox.remove( downloadSourceBox );
								downloadButton.setEnabled( false );
								downloadInfoLabel.setText( Language.getText( "module.repAnalyzer.tab.mapPreview.downloadingMap" ) );
								// First try the selected gateway, but if map is not available on that gateway, try the replay's gateway!
								final Gateway gateway = event != null ? (Gateway) downloadFromGatewayComboBox.getSelectedItem() : replay.initData.gateway;
								final String mapUrl = gateway.depotServerUrl + mapFileName;
								final Downloader[] downloaderHolder = new Downloader[ 1 ];
								final Downloader downloader = downloaderHolder[ 0 ] = new Downloader( mapUrl, mapFile, true, new DownloaderCallback() {
									private final InternalFrameListener downloadStopperListener = new InternalFrameAdapter() {
										@Override
										public void internalFrameClosing( final InternalFrameEvent event ) {
											// If the analyzer is closed, we want to stop the map download
											downloaderHolder[ 0 ].requestToCancel();
										}
									};
									{
										addInternalFrameListener( downloadStopperListener );
									}
									@Override
									public void downloadFinished( final boolean success ) {
										if ( !success && event != null ) {
											// Failed to download from selected gateway, try the replay's gateway!
											downloadBox.remove( downloaderHolder[ 0 ].getProgressBar() );
											mapPreviewPanel.validate();
											actionPerformed( null );
											return;
										}
										removeInternalFrameListener( downloadStopperListener );
										if ( !ReplayAnalyzer.this.isClosed() ) {
											downloadInfoLabel.setText( Language.getText( success ? "module.repAnalyzer.tab.mapPreview.mapDownloaded" : "module.repAnalyzer.tab.mapPreview.mapDownloadFailed" ) );
											controlBox.remove( downloadButton );
											final JButton reopenButton = new JButton( Icons.ARROW_CIRCLE_DOUBLE );
											GuiUtils.updateButtonText( reopenButton, "module.repAnalyzer.tab.mapPreview.reopenReplayButton" );
											reopenButton.addActionListener( new ActionListener() {
												@Override
												public void actionPerformed( final ActionEvent event ) {
													try {
														ReplayAnalyzer.this.setClosed( true );
													} catch ( final PropertyVetoException pe ) {
														// This will never happen
													}
													MainFrame.INSTANCE.openReplayFile( replayFile );
												}
											} );
											controlBox.add( reopenButton );
											mapPreviewPanel.validate();
										}
									}
								} );
								downloadBox.add( downloader.getProgressBar() );
								mapPreviewPanel.validate();
								downloader.start();
							}
						} );
						controlBox.add( downloadButton );
						downloadBox.add( controlBox );
						downloadSourceBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.mapPreview.downloadFromGateway" ) ) );
						downloadSourceBox.add( downloadFromGatewayComboBox );
						downloadBox.add( downloadSourceBox );
						northBox.add( downloadBox );
					}
				}
				final Box optionsBox = Box.createHorizontalBox();
				optionsBox.add( new JLabel( Language.getText( "module.repAnalyzer.tab.mapPreview.zoom" ) ) );
				final JSlider zoomSlider = GuiUtils.createSlider( Settings.KEY_REP_ANALYZER_MAP_PREVIEW_ZOOM, 0, MAX_MAP_PREVIEW_ZOOM );
				zoomSlider.setPaintLabels( true );
				zoomSlider.setPaintTicks( true );
				zoomSlider.setSnapToTicks( true );
				zoomSlider.setMajorTickSpacing( 1 );
				zoomSlider.setLabelTable( LABEL_DICTIONARY );
				final ChangeListener zoomChangeListener = new ChangeListener() {
					@Override
					public void stateChanged( final ChangeEvent event ) {
						if ( zoomSlider.getValueIsAdjusting() )
							return;
						final int zoom   = zoomSlider.getValue();
						if ( replay.mapInfo == null )
							mapPreviewLabel.setText( Language.getText( "module.repAnalyzer.tab.mapPreview.previewNotAvailable" ) );
						else {
							final int width  = replay.mapInfo.previewIcon.getIconWidth ();
							final int height = replay.mapInfo.previewIcon.getIconHeight();
							mapPreviewLabel.setIcon( new ImageIcon( replay.mapInfo.previewIcon.getImage().getScaledInstance( zoom == 0 ? width / 2 : width * zoom, zoom == 0 ? height / 2 : height * zoom, Image.SCALE_SMOOTH ) ) );
						}
					}
				};
				zoomChangeListener.stateChanged( null );
				zoomSlider.addChangeListener( zoomChangeListener );
				optionsBox.add( zoomSlider );
				northBox.add( optionsBox );
				mapPreviewPanel.add( GuiUtils.wrapInPanel( northBox ), BorderLayout.NORTH );
				mapPreviewLabel.setHorizontalAlignment( JLabel.CENTER );
				GuiUtils.makeComponentDragScrollable( mapPreviewLabel );
				mapPreviewPanel.add( new JScrollPane( mapPreviewLabel ), BorderLayout.CENTER );
				// Map attributes
				final Box attributesBox = Box.createVerticalBox();
				attributesBox.setBorder( BorderFactory.createTitledBorder( Language.getText( "module.repAnalyzer.tab.mapPreview.attributes.title" ) ) );
				Box row = Box.createHorizontalBox();
				row.add( new JLabel( Language.getText( "module.repAnalyzer.tab.mapPreview.attributes.chooseLocale" ) ) );
				final Vector< String > localeVector = replay.mapInfo == null || replay.mapInfo.localeAttributeMapMap == null ? new Vector< String >( 0 ) : new Vector< String >( replay.mapInfo.localeAttributeMapMap.keySet() );
				Collections.sort( localeVector );
				final JComboBox< String > localeComboBox = new JComboBox<>( localeVector );
				localeComboBox.setSelectedItem( Settings.getString( Settings.KEY_REP_ANALYZER_MAP_PREVIEW_LOCALE ) );
				localeComboBox.setMaximumRowCount( localeComboBox.getItemCount() ); // Display all available locales
				row.add( localeComboBox );
				attributesBox.add( row );
				row = Box.createHorizontalBox();
				row.add( new JLabel( Language.getText( "module.repAnalyzer.tab.mapPreview.attributes.name" ) ) );
				final JTextField nameTextField = new JTextField();
				nameTextField.setEditable( false );
				row.add( nameTextField );
				attributesBox.add( row );
				row = Box.createHorizontalBox();
				row.add( new JLabel( Language.getText( "module.repAnalyzer.tab.mapPreview.attributes.author" ) ) );
				final JTextField authorTextField = new JTextField();
				authorTextField.setEditable( false );
				row.add( authorTextField );
				attributesBox.add( row );
				row = Box.createHorizontalBox();
				row.add( new JLabel( Language.getText( "module.repAnalyzer.tab.mapPreview.attributes.shortDesc" ) ) );
				final JTextField shortDescTextField = new JTextField();
				shortDescTextField.setEditable( false );
				row.add( shortDescTextField );
				attributesBox.add( row );
				row = Box.createHorizontalBox();
				row.add( new JLabel( Language.getText( "module.repAnalyzer.tab.mapPreview.attributes.longDesc" ) ) );
				final JTextArea longDescTextArea = new JTextArea( 3, 1 );
				longDescTextArea.setEditable( false );
				longDescTextArea.setLineWrap( true );
				longDescTextArea.setWrapStyleWord( true );
				row.add( new JScrollPane( longDescTextArea ) );
				attributesBox.add( row );
				if ( replay.mapInfo != null && replay.mapInfo.localeAttributeMapMap != null )
    				localeComboBox.addActionListener( new ActionListener() {
						{ actionPerformed( null ); } // Initialize
    					@Override
    					public void actionPerformed( final ActionEvent event ) {
    						final String locale = (String) localeComboBox.getSelectedItem();
    						if ( event != null)
    							Settings.set( Settings.KEY_REP_ANALYZER_MAP_PREVIEW_LOCALE, locale );
    						nameTextField     .setText( replay.mapInfo.getNameAttribute     ( locale ) );
    						authorTextField   .setText( replay.mapInfo.getAuthorAttribute   ( locale ) );
    						shortDescTextField.setText( replay.mapInfo.getShortDescAttribute( locale ) );
    						longDescTextArea  .setText( replay.mapInfo.getLongDescAttribute ( locale ) );
    					}
    				} );
				GuiUtils.alignBox( attributesBox, 2 );
				mapPreviewPanel.add( attributesBox, BorderLayout.SOUTH );
			}
		} );
		
		return mapPreviewPanel;
	}
	
	/**
	 * Creates and returns the public comments tab.
	 * @return the public comments tab
	 */
	private JComponent createPublicCommentsTab() {
		final JPanel      publicCommentsPanel = new JPanel( new BorderLayout() );
		final JScrollPane scrollPane      = new JScrollPane( publicCommentsPanel );
		
		scrollPane.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
				final Box contentBox = Box.createVerticalBox();
				final StatusLabel statusLabel = new StatusLabel();
				contentBox.add( GuiUtils.wrapInPanel( statusLabel ) );
				publicCommentsPanel.add( contentBox );
				
				final JLabel viewTermsAndNotesLinkLabel = GeneralUtils.createLinkLabel( Language.getText( "module.repAnalyzer.tab.publicComments.viewTermsAndNotes" ), Consts.URL_PUBLIC_REPLAY_COMMENTING );
				GuiUtils.changeFontToBold( viewTermsAndNotesLinkLabel );
				final JPanel linkPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
				linkPanel.add( viewTermsAndNotesLinkLabel );
				contentBox.add( linkPanel );
				
				final JPanel profilePanel = new JPanel( new BorderLayout() );
				final JPanel summaryPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
				final JLabel commentsCountLabel = new JLabel( Language.getText( "module.repAnalyzer.tab.publicComments.profileComments", '-' ) );
				commentsCountLabel.setFont( commentsCountLabel.getFont().deriveFont( Font.BOLD, commentsCountLabel.getFont().getSize() + 5f ) );
				summaryPanel.add( commentsCountLabel );
				summaryPanel.add( Box.createHorizontalStrut( 20 ) );
				final JLabel ggsCountLabel = new JLabel( Language.getText( "module.repAnalyzer.tab.publicComments.profileGgs", '-' ) );
				ggsCountLabel.setFont( ggsCountLabel.getFont().deriveFont( Font.BOLD, ggsCountLabel.getFont().getSize() + 5f ) );
				summaryPanel.add( ggsCountLabel );
				summaryPanel.add( Box.createHorizontalStrut( 20 ) );
				final JLabel bgsCountLabel = new JLabel( Language.getText( "module.repAnalyzer.tab.publicComments.profileBgs", '-' ) );
				bgsCountLabel.setFont( bgsCountLabel.getFont().deriveFont( Font.BOLD, bgsCountLabel.getFont().getSize() + 5f ) );
				summaryPanel.add( bgsCountLabel );
				summaryPanel.add( Box.createHorizontalStrut( 20 ) );
				final JLabel refreshLinkLabel = GeneralUtils.createLinkStyledLabel( Language.getText( "module.repAnalyzer.tab.publicComments.refresh" ) );
				final String refreshLinkLabelHtml = refreshLinkLabel.getText();
				refreshLinkLabel.setIcon( Icons.ARROW_CIRCLE_315 );
				summaryPanel.add( refreshLinkLabel );
				profilePanel.add( summaryPanel, BorderLayout.NORTH );
				final IntHolder ggsCountHolder = new IntHolder( -1 );
				final IntHolder bgsCountHolder = new IntHolder( -1 );
				final JComponent rateBarComponent = new JComponent() {
					private static final int HEIGHT = 20;
					@Override public Dimension getPreferredSize() { return new Dimension( 1                , HEIGHT ); }
					@Override public Dimension getMaximumSize  () { return new Dimension( Integer.MAX_VALUE, HEIGHT ); }
					@Override public Dimension getMinimumSize  () { return new Dimension( 0                , HEIGHT ); }
					@Override
					protected void paintComponent( final Graphics g ) {
						int width  = getWidth ();
						int height = getHeight();
						if ( width == 0 || height == 0 )
							return;
						g.setColor( PlayerColor.LIGHT_GRAY.color );
						g.drawRect( 0, 0, width-1, height-1 );
						if ( ggsCountHolder.value > 0 || bgsCountHolder.value > 0 ) {
							width  -= 2;
							height -= 2;
    						int ggsWidth = ggsCountHolder.value * width / Math.max( ggsCountHolder.value + bgsCountHolder.value, 1 );
    						// Min 1 pixel if at least 1 rate:
    						if ( ggsWidth == 0 && ggsCountHolder.value > 0 )
    							ggsWidth = 1;
    						if ( ggsWidth == width && bgsCountHolder.value > 0 )
    							ggsWidth--;
    						g.setColor( PlayerColor.GREEN.color );
    						g.fillRect( 1, 1, ggsWidth, height );
    						g.setColor( PlayerColor.RED.color );
    						g.fillRect( 1 + ggsWidth, 1, width - ggsWidth, height );
						}
					}
					@Override
					public String getToolTipText( final MouseEvent event ) {
						if ( ggsCountHolder.value < 0 || bgsCountHolder.value < 0 )
							return Language.getText( "module.repAnalyzer.tab.publicComments.rateBarNoRateInfoAvailableToolTip" );
						else if ( ggsCountHolder.value > 0 || bgsCountHolder.value > 0 ) {
							final int sumRates = ggsCountHolder.value + bgsCountHolder.value;
							return Language.getText( "module.repAnalyzer.tab.publicComments.rateBarToolTip", ggsCountHolder.value, ggsCountHolder.value * 100 / sumRates, bgsCountHolder.value, bgsCountHolder.value * 100 / sumRates );
						}
						else
							return Language.getText( "module.repAnalyzer.tab.publicComments.rateBarNoOneRatedToolTip" );
					}
				};
				rateBarComponent.setToolTipText( "" ); // To turn on tool tips
				profilePanel.add( rateBarComponent, BorderLayout.CENTER );
				contentBox.add( GuiUtils.wrapInPanel( profilePanel ) );
				
				final JSplitPane postsSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, true );
				
				final JPanel newPostPanel = new JPanel( new BorderLayout() );
				newPostPanel.setBorder( BorderFactory.createTitledBorder( Language.getText( "module.repAnalyzer.tab.publicComments.newComment" ) ) );
				final JPanel newPostInputPanel = new JPanel( new BorderLayout() );
				final JPanel generalDataPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 3, 0 ) );
				generalDataPanel.add( new JLabel( Language.getText( "module.repAnalyzer.tab.publicComments.userName" ) ) );
				final JTextField userNameTextField = new JTextField( Settings.getString( Settings.KEY_REP_ANALYZER_PUBLIC_COMMENTS_USER_NAME ), 15 );
				generalDataPanel.add( userNameTextField );
				generalDataPanel.add( Box.createHorizontalStrut( 10 ) );
				generalDataPanel.add( new JLabel( Language.getText( "module.repAnalyzer.tab.publicComments.rate" ) ) );
				final ButtonGroup rateButtonGroup = new ButtonGroup();
				final JRadioButton ggRadioButton = new JRadioButton( Language.getText( "module.repAnalyzer.tab.publicComments.goodGame" ) );
				final JRadioButton bgRadioButton = new JRadioButton( Language.getText( "module.repAnalyzer.tab.publicComments.badGame" ) );
				rateButtonGroup.add( ggRadioButton );
				rateButtonGroup.add( bgRadioButton );
				generalDataPanel.add( ggRadioButton );
				generalDataPanel.add( bgRadioButton );
				newPostInputPanel.add( generalDataPanel, BorderLayout.NORTH );
				final JPanel commentPanel = new JPanel( new BorderLayout() );
				final JTextArea commentTextArea = new JTextArea( 2, 1 );
				//final JPanel symbolsPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 2, 0 ) );
				final Box symbolsPanel = Box.createHorizontalBox();
				for ( final String symbol : SYMBOLS ) {
					final JLabel symbolLabel = new JLabel( symbol );
					symbolLabel.setFont( symbolLabel.getFont().deriveFont( symbolLabel.getFont().getSize2D() + 6 ) );
					symbolLabel.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
					symbolLabel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
					symbolLabel.addMouseListener( new MouseAdapter() {
						@Override
						public void mouseEntered( final MouseEvent event ) {
							symbolLabel.setBorder( BorderFactory.createRaisedBevelBorder() );
						};
						@Override
						public void mouseExited( final MouseEvent event ) {
							symbolLabel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
						};
						@Override
						public void mousePressed( final MouseEvent event ) {
							try {
	                            commentTextArea.getDocument().insertString( commentTextArea.getCaretPosition(), symbol, null );
                            } catch ( final BadLocationException ble ) {
                            	ble.printStackTrace();
                            }
						};
					} );
					symbolsPanel.add( symbolLabel );
				}
				commentPanel.add( GuiUtils.createSelfManagedScrollPane( symbolsPanel, publicCommentsPanel ), BorderLayout.NORTH );
				commentPanel.add( new JLabel( Language.getText( "module.repAnalyzer.tab.publicComments.comment" ) ), BorderLayout.WEST );
				commentTextArea.setLineWrap( true );
				commentTextArea.setWrapStyleWord( true );
				commentPanel.add( new JScrollPane( commentTextArea ), BorderLayout.CENTER );
				newPostInputPanel.add( commentPanel, BorderLayout.CENTER );
				newPostPanel.add( newPostInputPanel, BorderLayout.CENTER );
				final JButton postButton = new JButton( Icons.DISK_SHARE );
				GuiUtils.updateButtonText( postButton, "module.repAnalyzer.tab.publicComments.postButton" );
				newPostPanel.add( postButton, BorderLayout.EAST );
				postsSplitPane.setTopComponent( newPostPanel );
				
				final JPanel postsPanel = new JPanel( new BorderLayout() );
				postsPanel.setBorder( BorderFactory.createTitledBorder( Language.getText( "module.repAnalyzer.tab.publicComments.comments" ) ) );
				final JPanel controlsPanel = new JPanel();
				final JButton firstPageButton = new JButton( Icons.ARROW_STOP_180 );
				firstPageButton.setEnabled( false );
				GuiUtils.updateButtonText( firstPageButton, "module.repAnalyzer.tab.publicComments.firstPageButton" );
				controlsPanel.add( firstPageButton );
				final JButton newerCommentsButton = new JButton( Icons.ARROW_180 );
				newerCommentsButton.setEnabled( false );
				GuiUtils.updateButtonText( newerCommentsButton, "module.repAnalyzer.tab.publicComments.newerCommentsButton" );
				controlsPanel.add( newerCommentsButton );
				final JLabel currentPagelabel = new JLabel( Language.getText( "module.repAnalyzer.tab.publicComments.page", 1 ) );
				currentPagelabel.setIcon( Icons.DEFAULT_NULL_ICON );
				GuiUtils.changeFontToBold( currentPagelabel );
				controlsPanel.add( currentPagelabel );
				controlsPanel.add( Box.createHorizontalStrut( 10 ) );
				final JButton olderCommentsButton = new JButton( Icons.ARROW );
				olderCommentsButton.setHorizontalTextPosition( SwingConstants.LEFT );
				GuiUtils.updateButtonText( olderCommentsButton, "module.repAnalyzer.tab.publicComments.olderCommentsButton" );
				controlsPanel.add( olderCommentsButton );
				postsPanel.add( controlsPanel, BorderLayout.NORTH );
				final JEditorPane postsBrowserPane = GuiUtils.createEditorPane();
				postsBrowserPane.setContentType( "text/html" );
				final JScrollPane postsBrowserScrollPane = new JScrollPane( postsBrowserPane );
				postsBrowserScrollPane.setPreferredSize( new Dimension( 10, 10 ) );
				postsPanel.add( postsBrowserScrollPane, BorderLayout.CENTER );
				postsSplitPane.setBottomComponent( postsPanel );
				
				contentBox.add( GuiUtils.wrapInBorderPanel( postsSplitPane ) );
				
				final Runnable profileRefresherTask = new Runnable() {
					@Override
                    public void run() {
						refreshLinkLabel.setEnabled( false );
						refreshLinkLabel.setText( Language.getText( "module.repAnalyzer.tab.publicComments.refresh" ) );
						statusLabel.setProgressMessageKey( "module.repAnalyzer.tab.publicComments.retrievingInProgress" );
						new NormalThread( "Public replay profile retriever" ) {
							@Override
		                    public void run() {
								HttpPost httpPost = null;
								try {
									final String sha1 = getReplaySha1();
									if ( sha1.isEmpty() ) {
										statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.replayNotAvailableError" );
										return;
									}
									// I use POST so the replay sha1 will not appear in the URL, because in case of errors the URL will appear in the log
									final Map< String, String > paramsMap = new HashMap< String, String >();
									paramsMap.put( InfoServletApi.PARAM_PROTOCOL_VERSION , InfoServletApi.PROTOCOL_VERSION_1               );
									paramsMap.put( InfoServletApi.PARAM_OPERATION        , InfoServletApi.OPERATION_GET_PUBLIC_REP_PROFILE );
									paramsMap.put( InfoServletApi.PARAM_SHA1             , sha1                                            );
									
									httpPost = new HttpPost( Consts.URL_SC2GEARS_DATABASE_INFO_SERVLET, paramsMap );
									if ( !httpPost.connect() ) {
										statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToConnect" );
										return;
									}
									if ( !httpPost.doPost() ) {
										statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToSend" );
										return;
									}
									final String response = httpPost.getResponse();
									if ( response == null ) {
										statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToParse" );
										return;
									}
									
									// Example response:
									/*
									 * <?xml version="1.0" encoding="UTF-8"?>
									 * <publicRepProfile docVersion="1.0">
									 *     <commentsCount>12</commentsCount>
									 *     <ggsCount>8</ggsCount>
									 *     <bgsCount>2</bgsCount>
									 * </publicRepProfile>
									 */
									final org.w3c.dom.Document responseDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new ByteArrayInputStream( response.getBytes( Consts.UTF8 ) ) );
									final Element docElement = responseDocument.getDocumentElement();
									final NodeList commentsCountNodeList = (NodeList) docElement.getElementsByTagName( "commentsCount" );
									commentsCountLabel.setText( Language.getText( "module.repAnalyzer.tab.publicComments.profileComments", commentsCountNodeList == null ? '-' : ( (Element) commentsCountNodeList.item( 0 ) ).getTextContent() ) );
									final NodeList ggsCountNodeList = (NodeList) docElement.getElementsByTagName( "ggsCount" );
									ggsCountHolder.value = ggsCountNodeList == null ? -1 : Integer.parseInt( ( (Element) ggsCountNodeList.item( 0 ) ).getTextContent() );
									ggsCountLabel.setText( Language.getText( "module.repAnalyzer.tab.publicComments.profileGgs", ggsCountNodeList == null ? '-' : ggsCountHolder.value ) );
									final NodeList bgsCountNodeList = (NodeList) docElement.getElementsByTagName( "bgsCount" );
									bgsCountHolder.value = bgsCountNodeList == null ? -1 : Integer.parseInt( ( (Element) bgsCountNodeList.item( 0 ) ).getTextContent() );
									bgsCountLabel.setText( Language.getText( "module.repAnalyzer.tab.publicComments.profileBgs", bgsCountNodeList == null ? '-' : bgsCountHolder.value ) );
									rateBarComponent.repaint();
									
									statusLabel.clearMessage();
									
								} catch ( final Exception e ) {
									statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToRetrieve" );
									e.printStackTrace();
								} finally {
									if ( httpPost != null )
										httpPost.close();
									refreshLinkLabel.setEnabled( true );
									refreshLinkLabel.setText( refreshLinkLabelHtml );
									GuiUtils.setComponentTreeEnabled( newPostPanel, true );
								}
							}
						}.start();
					}
				};
				profileRefresherTask.run();
				
				refreshLinkLabel.addMouseListener( new MouseAdapter() {
					@Override
					public void mousePressed( final MouseEvent event ) {
						profileRefresherTask.run();
					}
				} );
				
				final IntHolder pageHolder = new IntHolder( 1 );
				final int pageSize = 10;
				
				final Runnable postsRefresherTask = new Runnable() {
					@Override
                    public void run() {
						firstPageButton    .setEnabled( false );
						olderCommentsButton.setEnabled( false );
						newerCommentsButton.setEnabled( false );
						currentPagelabel.setIcon( Icons.LOADING );
						// This is called from the event dispatch thread, so we refresh in a new thread
						new NormalThread( "Public replay comment retriever" ) {
							@Override
							public void run() {
								HttpPost httpPost = null;
								try {
									final String sha1 = getReplaySha1();
									if ( sha1.isEmpty() ) {
										statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.replayNotAvailableError" );
										throw new Exception( "Replay unavailable!" );
									}
									postsBrowserPane.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
									// I use POST so the replay sha1 will not appear in the URL, because in case of errors the URL will appear in the log
									final Map< String, String > paramsMap = new HashMap< String, String >();
									paramsMap.put( InfoServletApi.PARAM_PROTOCOL_VERSION, InfoServletApi.PROTOCOL_VERSION_1                       );
									paramsMap.put( InfoServletApi.PARAM_OPERATION       , InfoServletApi.OPERATION_GET_PUBLIC_REP_COMMENTS        );
									paramsMap.put( InfoServletApi.PARAM_SHA1            , sha1                                                    );
									paramsMap.put( InfoServletApi.PARAM_OFFSET          , Integer.toString( ( pageHolder.value - 1 ) * pageSize ) );
									paramsMap.put( InfoServletApi.PARAM_LIMIT           , Integer.toString( pageSize                            ) );
									paramsMap.put( InfoServletApi.PARAM_TIME_ZONE       , TimeZone.getDefault().getID()                           );
									
									httpPost = new HttpPost( Consts.URL_SC2GEARS_DATABASE_INFO_SERVLET, paramsMap );
									if ( !httpPost.connect() )
										throw new Exception( Language.getText( "module.repAnalyzer.dbaseErrors.failedToConnect" ) );
									if ( !httpPost.doPost() )
										throw new Exception( Language.getText( "module.repAnalyzer.dbaseErrors.failedToSend" ) );
									final String response = httpPost.getResponse();
									if ( response == null )
										throw new Exception( Language.getText( "module.repAnalyzer.dbaseErrors.failedToParse" ) );
									
									postsBrowserPane.setText( response );
								} catch ( final Exception e ) {
									e.printStackTrace();
									postsBrowserPane.setText( "<html><body style='font-family:arial;font-size:10px;font-style:italic;background:#ffffff;'>"
										+ "<p>This content is currently unavailable. Please try again later.</p></body></html>" );
								} finally {
									if ( httpPost != null )
										httpPost.close();
									postsBrowserPane.setCursor( null );
									firstPageButton    .setEnabled( pageHolder.value > 1 );
									newerCommentsButton.setEnabled( pageHolder.value > 1 );
									olderCommentsButton.setEnabled( true );
									currentPagelabel.setIcon( Icons.DEFAULT_NULL_ICON );
									currentPagelabel.setText( Language.getText( "module.repAnalyzer.tab.publicComments.page", pageHolder.value ) );
								}
							}
						}.start();
                    }
				};
				postsRefresherTask.run();
				
				final ActionListener pagingActionListener = new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						if ( event.getSource() == olderCommentsButton ) {
							pageHolder.value++;
						} else if ( pageHolder.value > 1 ) {
							if ( event.getSource() == firstPageButton )
								pageHolder.value = 1;
							else
								pageHolder.value--;
							firstPageButton    .setEnabled( pageHolder.value > 1 );
							newerCommentsButton.setEnabled( pageHolder.value > 1 );
						} else
							return;
						postsRefresherTask.run();
					}
				};
				firstPageButton    .addActionListener( pagingActionListener );
				newerCommentsButton.addActionListener( pagingActionListener );
				olderCommentsButton.addActionListener( pagingActionListener );
				
				postButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						final String userName = InfoServletApi.trimUserName( userNameTextField.getText() );
						if ( userName.isEmpty() ) {
							statusLabel.setErrorMessageKey( "module.repAnalyzer.tab.publicComments.userNameRequired" );
							userNameTextField.requestFocusInWindow();
							return;
						}
						final boolean isGg = ggRadioButton.isSelected();
						final boolean isBg = bgRadioButton.isSelected();
						final String comment = InfoServletApi.trimPublicRepComment( commentTextArea.getText() );
						if ( comment.isEmpty() && !isGg && !isBg ) {
							statusLabel.setErrorMessageKey( "module.repAnalyzer.tab.publicComments.commentOrRatingRequired" );
							commentTextArea.requestFocusInWindow();
							return;
						}
						
						Settings.set( Settings.KEY_REP_ANALYZER_PUBLIC_COMMENTS_USER_NAME, userName );
						statusLabel.setProgressMessageKey( "module.repAnalyzer.tab.publicComments.postingComment" );
						postButton.setEnabled( false );
						GuiUtils.setComponentTreeEnabled( newPostPanel, false );
						new NormalThread( "Public comment poster" ) {
							@Override
		                    public void run() {
								HttpPost httpPost = null;
								try {
									final String sha1 = getReplaySha1();
									if ( sha1.isEmpty() ) {
										statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.replayNotAvailableError" );
										return;
									}
									// I use POST so the authorization key will not appear in the URL, because in case of errors the URL will appear in the log
									final Map< String, String > paramsMap = new HashMap< String, String >();
									paramsMap.put( InfoServletApi.PARAM_PROTOCOL_VERSION , InfoServletApi.PROTOCOL_VERSION_1                );
									paramsMap.put( InfoServletApi.PARAM_OPERATION        , InfoServletApi.OPERATION_SAVE_PUBLIC_REP_COMMENT );
									paramsMap.put( InfoServletApi.PARAM_SHA1             , sha1                                             );
									paramsMap.put( InfoServletApi.PARAM_USER_NAME        , userName                                         );
									if ( isGg )
										paramsMap.put( InfoServletApi.PARAM_RATE_GG, Boolean.TRUE.toString() );
									if ( isBg )
										paramsMap.put( InfoServletApi.PARAM_RATE_BG, Boolean.TRUE.toString() );
									if ( !comment.isEmpty() )
										paramsMap.put( InfoServletApi.PARAM_COMMENT, comment );
									final String authorizationKey = Settings.getString( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY );
									if ( !authorizationKey.isEmpty() )
										paramsMap.put( InfoServletApi.PARAM_AUTHORIZATION_KEY, authorizationKey );
									
									httpPost = new HttpPost( Consts.URL_SC2GEARS_DATABASE_INFO_SERVLET, paramsMap );
									if ( !httpPost.connect() ) {
										statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToConnect" );
										return;
									}
									if ( !httpPost.doPost() ) {
										statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToSend" );
										return;
									}
									final List< String > response = httpPost.getResponseLines();
									if ( response == null ) {
										statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToParse" );
										return;
									}
									
									if ( response.size() > 0 && Boolean.parseBoolean( response.get( 0 ) ) ) {
										commentTextArea.setText( "" );
										rateButtonGroup.clearSelection();
										statusLabel.setInfoMessageKey( "module.repAnalyzer.tab.publicComments.postedSuccessfully" );
										if ( !comment.isEmpty() && pageHolder.value == 1 )
											postsRefresherTask.run();
										// Rep profile is not refreshed here (profile is updated asynchronously (via Tasks) so it might be no use (we would get the same data back..). The Refresh link can be used to refresh the profile
									}
									else
										statusLabel.setErrorMessageKey( "module.repAnalyzer.tab.publicComments.failedToPost" );
								} catch ( final Exception e ) {
									statusLabel.setErrorMessageKey( "module.repAnalyzer.tab.publicComments.failedToPost" );
									e.printStackTrace();
								} finally {
									if ( httpPost != null )
										httpPost.close();
									postButton.setEnabled( true );
									GuiUtils.setComponentTreeEnabled( newPostPanel, true );
								}
							}
						}.start();
					}
				} );
				
				publicCommentsPanel.validate();
				publicCommentsPanel.repaint();
				
				postsSplitPane.setDividerLocation( 0.25 );
			}
		} );
		
		return scrollPane;
	}
	
	/**
	 * Creates and returns the private data tab.
	 * @return the private data tab
	 */
	private JComponent createPrivateDataTab() {
		final JPanel privateDataPanel = new JPanel( new BorderLayout() );
		
		privateDataPanel.addComponentListener( new FirstShownListener() {
			@Override
			public void firstShown( final ComponentEvent event ) {
				final Box contentBox = Box.createVerticalBox();
				final StatusLabel statusLabel = new StatusLabel();
				contentBox.add( GuiUtils.wrapInPanel( statusLabel ) );
				privateDataPanel.add( contentBox );
				
				final JPanel fillerPanel = new JPanel( new BorderLayout() );
				
				final String authorizationKey = Settings.getString( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY );
				if ( authorizationKey.isEmpty() ) {
					statusLabel.setErrorMessageKey( "module.repAnalyzer.tab.privateData.missingKey" );
					contentBox.add( GuiUtils.wrapInPanel( MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.SC2GEARS_DATABASE ) ) );
					contentBox.add( fillerPanel );
					privateDataPanel.validate();
					return;
				}
				
				contentBox.add( GuiUtils.wrapInPanel( GuiUtils.createOpenUserPageLinkLabel() ) );
				
				contentBox.add( fillerPanel );
				
				statusLabel.setProgressMessageKey( "module.repAnalyzer.tab.privateData.retrievingInProgress" );
				new NormalThread( "Private data retreiver" ) {
					@Override
                    public void run() {
						HttpPost httpPost = null;
						try {
							final String sha1 = getReplaySha1();
							if ( sha1.isEmpty() ) {
								statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.replayNotAvailableError" );
								return;
							}
							// I use POST so the authorization key will not appear in the URL, because in case of errors the URL will appear in the log
							final Map< String, String > paramsMap = new HashMap< String, String >();
							paramsMap.put( InfoServletApi.PARAM_PROTOCOL_VERSION , InfoServletApi.PROTOCOL_VERSION_1             );
							paramsMap.put( InfoServletApi.PARAM_OPERATION        , InfoServletApi.OPERATION_GET_PRIVATE_REP_DATA );
							paramsMap.put( InfoServletApi.PARAM_AUTHORIZATION_KEY, authorizationKey                              );
							paramsMap.put( InfoServletApi.PARAM_SHA1             , sha1                                          );
							
							httpPost = new HttpPost( Consts.URL_SC2GEARS_DATABASE_INFO_SERVLET, paramsMap );
							if ( !httpPost.connect() ) {
								statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToConnect" );
								return;
							}
							if ( !httpPost.doPost() ) {
								statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToSend" );
								return;
							}
							final String response = httpPost.getResponse();
							if ( response == null ) {
								statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToParse" );
								return;
							}
							// Example response:
							/*
							 * <?xml version="1.0" encoding="UTF-8"?>
							 * <privateData docVersion="1.0">
							 *     <authKeyValid>true</authKeyValid>
							 *     <replayStored>true</replayStored>
							 *     <labels>
							 *         <label color="ffffff" bgColor="d4343e" checked="true">GG</label>
							 *         <label color="ffffff" bgColor="0042ff" checked="false">Funny</label>
							 *     </labels>
							 *     <comment>Some comment.</comment>
							 * </privateData>
							 */
							final org.w3c.dom.Document responseDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new ByteArrayInputStream( response.getBytes( Consts.UTF8 ) ) );
							final Element docElement = responseDocument.getDocumentElement();
							if ( !Boolean.parseBoolean( ( (Element) docElement.getElementsByTagName( "authKeyValid" ).item( 0 ) ).getTextContent() ) ) {
								statusLabel.setErrorMessage( Language.getText( "module.repAnalyzer.dbaseErrors.invalidAuthKey" ) );
								contentBox.remove( fillerPanel );
								contentBox.add( GuiUtils.wrapInPanel( MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.SC2GEARS_DATABASE ) ) );
								contentBox.add( fillerPanel );
								privateDataPanel.validate();
								return;
							}
							if ( !Boolean.parseBoolean( ( (Element) docElement.getElementsByTagName( "replayStored" ).item( 0 ) ).getTextContent() ) ) {
								statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.replayNotStored" );
								return;
							}
							contentBox.remove( fillerPanel );
							final JPanel buttonsPanel = new JPanel();
							contentBox.add( buttonsPanel );
							final NodeList labelNodes = ( (Element) docElement.getElementsByTagName( "labels" ).item( 0 ) ).getChildNodes();
							final JCheckBox[] labelCheckBoxes = new JCheckBox[ labelNodes.getLength() ];
							final JPanel labelsPanel = new JPanel();
							for ( int i = 0; i < labelCheckBoxes.length; i++ ) {
								final Element labelElement = (Element) labelNodes.item( i );
								labelsPanel.add( labelCheckBoxes[ i ] = new JCheckBox( labelElement.getTextContent(), Boolean.parseBoolean( labelElement.getAttribute( "checked" ) ) ) );
								labelCheckBoxes[ i ].setOpaque( true );
								labelCheckBoxes[ i ].setForeground( new Color( Integer.parseInt( labelElement.getAttribute( "color"   ), 16 ) ) );
								labelCheckBoxes[ i ].setBackground( new Color( Integer.parseInt( labelElement.getAttribute( "bgColor" ), 16 ) ) );
							}
							final JScrollPane labelsScrollPane = new JScrollPane( labelsPanel );
							labelsScrollPane.setBorder( BorderFactory.createTitledBorder( Language.getText( "module.repAnalyzer.tab.privateData.labels" ) ) );
							labelsScrollPane.getViewport().setPreferredSize( new Dimension( 10, 30 ) );
							contentBox.add( labelsScrollPane );
							
							contentBox.add( Box.createVerticalStrut( 3 ) );
							final JPanel charsWrapper = new JPanel( new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
							final JLabel maxAndRemainingCharsLabel = new JLabel();
							GuiUtils.changeFontToBold( maxAndRemainingCharsLabel );
							charsWrapper.add( maxAndRemainingCharsLabel );
							contentBox.add( charsWrapper );
							final JTextArea commentTextArea = new JTextArea( ( (Element) docElement.getElementsByTagName( "comment" ).item( 0 ) ).getTextContent(), 1, 1 );
							commentTextArea.setLineWrap( true );
							commentTextArea.setWrapStyleWord( true );
							commentTextArea.getDocument().addDocumentListener( new DocumentListener() {
								{ changedUpdate( null ); } // Initialize
								@Override
								public void removeUpdate( final DocumentEvent event ) {
									changedUpdate( event );
								}
								@Override
								public void insertUpdate( final DocumentEvent event ) {
									changedUpdate( event );
								}
								@Override
								public void changedUpdate( final DocumentEvent event ) {
									maxAndRemainingCharsLabel.setText( Language.getText( "module.repAnalyzer.tab.privateData.maxAndRemainingChars", InfoServletApi.MAX_PRIVATE_COMMENT_LENGTH, InfoServletApi.MAX_PRIVATE_COMMENT_LENGTH - commentTextArea.getDocument().getLength() ) );
								}
							} );
							final JScrollPane commentScrollPane = new JScrollPane( commentTextArea );
							commentScrollPane.setBorder( BorderFactory.createTitledBorder( Language.getText( "module.repAnalyzer.tab.privateData.comment" ) ) );
							contentBox.add( GuiUtils.wrapInBorderPanel( commentScrollPane ) );
							
							final JButton saveButton = new JButton( Icons.DISK );
							GuiUtils.updateButtonText( saveButton, "module.repAnalyzer.tab.privateData.saveButton" );
							saveButton.addActionListener( new ActionListener() {
								@Override
								public void actionPerformed( final ActionEvent event ) {
									saveButton.setEnabled( false );
									GuiUtils.setComponentTreeEnabled( labelsScrollPane , false );
									GuiUtils.setComponentTreeEnabled( commentScrollPane, false );
									statusLabel.setProgressMessageKey( "module.repAnalyzer.tab.privateData.saving" );
									new NormalThread( "Private data saver" ) {
										@Override
				                        public void run() {
											HttpPost httpPost = null;
											try {
												commentTextArea.setText( InfoServletApi.trimPrivateRepComment( commentTextArea.getText() ) );
												final StringBuilder labelsBuilder = new StringBuilder();
												for ( int i = 0; i < labelCheckBoxes.length; i++ )
													if ( labelCheckBoxes[ i ].isSelected() ) {
														if ( labelsBuilder.length() > 0 )
															labelsBuilder.append( ',' );
														labelsBuilder.append( i );
													}
												paramsMap.put( InfoServletApi.PARAM_OPERATION, InfoServletApi.OPERATION_SAVE_PRIVATE_REP_DATA                    );
												paramsMap.put( InfoServletApi.PARAM_LABELS   , labelsBuilder  .toString()                                        );
												paramsMap.put( InfoServletApi.PARAM_COMMENT  , InfoServletApi.trimPrivateRepComment( commentTextArea.getText() ) );
												
												httpPost = new HttpPost( Consts.URL_SC2GEARS_DATABASE_INFO_SERVLET, paramsMap );
												if ( !httpPost.connect() ) {
													statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToConnect" );
													return;
												}
												if ( !httpPost.doPost() ) {
													statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToSend" );
													return;
												}
												final List< String > response = httpPost.getResponseLines();
												if ( response == null ) {
													statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToParse" );
													return;
												}
												
												if ( response.size() > 0 && Boolean.parseBoolean( response.get( 0 ) ) )
													statusLabel.setInfoMessageKey( "module.repAnalyzer.tab.privateData.savedSuccessfully" );
												else
													statusLabel.setErrorMessageKey( "module.repAnalyzer.tab.privateData.failedToSave" );
											} catch ( final Exception e ) {
												statusLabel.setErrorMessageKey( "module.repAnalyzer.tab.privateData.failedToSave" );
												e.printStackTrace();
											} finally {
												if ( httpPost != null )
													httpPost.close();
												saveButton.setEnabled( true );
												GuiUtils.setComponentTreeEnabled( labelsScrollPane, true );
												GuiUtils.setComponentTreeEnabled( commentScrollPane, true );
											}
										}
									}.start();
								}
							} );
							buttonsPanel.add( saveButton );
							
							statusLabel.clearMessage();
							
							privateDataPanel.validate();
						} catch ( final Exception e ) {
							if ( fillerPanel.getParent() == null ) // Re-add filler if it was already removed
								contentBox.add( fillerPanel );
							statusLabel.setErrorMessageKey( "module.repAnalyzer.dbaseErrors.failedToRetrieve" );
							e.printStackTrace();
						} finally {
							if ( httpPost != null )
								httpPost.close();
						}
					}
				}.start();
				
				privateDataPanel.validate();
			}
		} );
		
		return privateDataPanel;
	}
	
	/**
	 * Returns the SHA-1 checksum of the replay file.
	 * @return the SHA-1 checksum of the replay file
	 */
	private String getReplaySha1() {
		if ( replaySha1 == null || replaySha1.isEmpty() )
			replaySha1 = GeneralUtils.calculateFileSha1( replayFile );
		return replaySha1;
	}
	
}
