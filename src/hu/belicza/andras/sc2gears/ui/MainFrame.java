/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.EnumCache;
import hu.belicza.andras.sc2gears.services.GlobalHotkeys;
import hu.belicza.andras.sc2gears.services.MousePrintRecorder;
import hu.belicza.andras.sc2gears.services.ReplayAutoSaver;
import hu.belicza.andras.sc2gears.services.Sc2RegMonitor;
import hu.belicza.andras.sc2gears.services.UpdateChecker;
import hu.belicza.andras.sc2gears.services.plugins.PluginManager;
import hu.belicza.andras.sc2gears.services.streaming.PrivateVideoStreaming;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.sound.Sounds;
import hu.belicza.andras.sc2gears.sound.Sounds.VoiceDescription;
import hu.belicza.andras.sc2gears.ui.dialogs.AboutDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.DiagnosticToolDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.GameTimeRealTimeConverterDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.ImportBODialog;
import hu.belicza.andras.sc2gears.ui.dialogs.KeyboardShortcutsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog.SettingsTab;
import hu.belicza.andras.sc2gears.ui.dialogs.MySharedReplaysDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.NewToSc2gearsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.PluginManagerDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.RenameItemDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.Sc2gearsDatabaseDownloaderDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.ShareReplaysDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.SystemMessagesDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.TipsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.TranslationToolDialog;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.ModuleFrame;
import hu.belicza.andras.sc2gears.ui.moduls.multirepanal.MultiRepAnalysis;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ReplayAnalyzer;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplaySearch;
import hu.belicza.andras.sc2gears.ui.moduls.startpage.StartPage;
import hu.belicza.andras.sc2gears.ui.mousepracticegame.MousePracticeGameFrame;
import hu.belicza.andras.sc2gears.ui.ontopdialogs.OnTopApmDisplayDialog;
import hu.belicza.andras.sc2gears.ui.ontopdialogs.OnTopGameInfoDialog;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.LeetTranslator;
import hu.belicza.andras.sc2gears.util.LeetTranslator.LeetnessLevel;
import hu.belicza.andras.sc2gears.util.ProfileCache;
import hu.belicza.andras.sc2gears.util.ReplayCache;
import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.AbilityGroup;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerColor;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Research;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Upgrade;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.importing.ReplaySpecification;
import hu.belicza.andras.sc2gearspluginapi.impl.Hotkey;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * The main frame of the application.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	
	/** Start time of the application. */
	public static final Date APPLICATION_START_TIME = new Date();
	
	/** Maximum number of recent replays. */
	private static final int MAX_RECENT_REPLAYS = 15;
	
	/** Reference to the only instance of the main frame.*/
	public static MainFrame INSTANCE;
	
	/**
	 * Reference to the tray icon of the application.
	 * Can be null if system tray is not supported or it failed to install the tray icon. */
	private TrayIcon trayIcon;
	/** Reference to the popup menu of the tray icon. */
	private JPopupMenu trayIconPopupMenu;
	
	/** The split pane in the main frame.                   */
	private final JSplitPane mainSplitPane                           = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, true );
	
	/** Reference to the enable replay auto-save tray menu item.  */
	private final JCheckBoxMenuItem enableReplayAutoSaveTrayMenuItem = new JCheckBoxMenuItem( Language.getText( "tray.enableReplayAutoSave" ), Icons.DATABASE_SAVE, Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_REPLAY_AUTO_SAVE ) );
	/** Reference to the enable APM alert tray menu item.         */
	private final JCheckBoxMenuItem enableApmAlertTrayMenuItem       = new JCheckBoxMenuItem( Language.getText( "tray.enableApmAlert" ), Icons.ALARM_CLOCK, Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_APM_ALERT ) );
	
	/** Reference to the open last replay menu item.          */
	private final JMenuItem  openLastReplayMenuItem                 = new JMenuItem( Icons.CHART );
	/** Reference to the recent replays menu.                 */
	private final JMenu      recentReplaysMenu                      = new JMenu();
	/** Reference to the enable replay auto-save menu item.   */
	private final JCheckBoxMenuItem enableReplayAutoSaveMenuItem    = GuiUtils.createCheckBoxMenuItem( "menu.settings.enableReplayAutoSave", Settings.KEY_SETTINGS_ENABLE_REPLAY_AUTO_SAVE, Icons.DATABASE_SAVE );
	/** Reference to the enable APM alert menu item.          */
	private final JCheckBoxMenuItem enableApmAlertMenuItem          = GuiUtils.createCheckBoxMenuItem( "menu.settings.enableApmAlert", Settings.KEY_SETTINGS_ENABLE_APM_ALERT, Icons.ALARM_CLOCK );
	/** Reference to the plugins menu.                        */
	private final JMenu      pluginsMenu                            = new JMenu();
	/** Reference to the minimize to tray menu item.          */
	private final JMenuItem  minimizeToTrayMenuItem                 = new JMenuItem( Icons.APPLICATION_DOCK_TAB );
	/** Reference to the view system messages menu item.      */
	private final JMenuItem  startMinimizedToTrayMenuItem           = GuiUtils.createCheckBoxMenuItem( "menu.window.startMinimimizedToTray", Settings.KEY_WINDOW_START_MINIMIZED_TO_TRAY, Icons.APPLICATION_DOCK_TAB );
	/** Reference to the view system messages menu item.      */
	public  final JMenuItem  viewSystemMessagesMenuItem             = new JMenuItem( Icons.REPORT_EXCLAMATION );
	/** Reference to the Check Updates menu item.             */
	private final JMenuItem  checkUpdatesMenuItem                   = new JMenuItem( Icons.ARROW_CIRCLE_DOUBLE );
	
	/** The navigation box.                                   */
	private final Box                    navigationBox              = Box.createVerticalBox();
	/** The navigation bar.                                   */
	private final JScrollPane            navigationBar              = new JScrollPane();
	/** Reference to the navigation tree.                     */
	private final JTree                  navigationTree             = new JTree();
	/** Reference to the Start page node.                     */
	private final DefaultMutableTreeNode startPageNode              = new DefaultMutableTreeNode( Language.getText( "module.startPage.title" ) );
	/** Reference to the rep analyzer node.                   */
	private final DefaultMutableTreeNode repAnalNode                = new DefaultMutableTreeNode( Language.getText( "module.repAnalyzer.name" ) );
	/** Reference to the new rep analyzer node.               */
	private final DefaultMutableTreeNode newRepAnalNode             = new DefaultMutableTreeNode( '<' + Language.getText( "navigationTree.new" ) + '>' );
	/** Reference to the multi-rep analysis node.             */
	private final DefaultMutableTreeNode multiRepAnalNode           = new DefaultMutableTreeNode( Language.getText( "module.multiRepAnal.name" ) );
	/** Reference to the new multi-rep analysis node.         */
	private final DefaultMutableTreeNode newMultiRepAnalNode        = new DefaultMutableTreeNode( '<' + Language.getText( "navigationTree.new" ) + '>' );
	/** Reference to the rep search node.                     */
	private final DefaultMutableTreeNode repSearchNode              = new DefaultMutableTreeNode( Language.getText( "module.repSearch.name" ) );
	/** Reference to the new rep search node.                 */
	private final DefaultMutableTreeNode newRepSearchNode           = new DefaultMutableTreeNode( '<' + Language.getText( "navigationTree.new" ) + '>' );
	/** Reference to the rep sources node.                    */
	private final DefaultMutableTreeNode repSourcesNode             = new DefaultMutableTreeNode( Language.getText( "navigationTree.repSources.name" ) );
	/** Reference to the sc2 auto-reps rep source node.       */
	public  final DefaultMutableTreeNode sc2AutoRepsRepSourceNode   = new DefaultMutableTreeNode( Language.getText( "navigationTree.repSources.sc2AutoReps.name" ) );
	/** Reference to the auto-saved reps rep source node.     */
	public  final DefaultMutableTreeNode autoSavedRepsRepSourceNode = new DefaultMutableTreeNode( Language.getText( "navigationTree.repSources.autoSavedReps.name" ) );
	/** Reference to the empty rep source node.               */
	public  final DefaultMutableTreeNode emptyRepSourceNode         = new DefaultMutableTreeNode( '<' + Language.getText( "navigationTree.empty" ) + '>' );
	/** Reference to the rep lists node.                      */
	private final DefaultMutableTreeNode repListsNode               = new DefaultMutableTreeNode( Language.getText( "navigationTree.repLists.name" ) );
	/** Reference to the empty rep list node.                 */
	public  final DefaultMutableTreeNode emptyRepListNode           = new DefaultMutableTreeNode( '<' + Language.getText( "navigationTree.empty" ) + '>' );
	/** Reference to the Plugins node.                        */
	private final DefaultMutableTreeNode pluginsNode                = new DefaultMutableTreeNode( Language.getText( "navigationTree.plugins.name" ) );
	/** Reference to the plugins info node.                   */
	public  final DefaultMutableTreeNode pluginsInfoNode            = new DefaultMutableTreeNode( Language.getText( "navigationTree.plugins.info.name" ) );
	/** The desktop pane holding the internal frames.         */
	private final JDesktopPane           desktopPane                = new JDesktopPane();
	/** SC2 game status label.                                */
	private final JLabel                 sc2GameStatusLabel         = new JLabel();
	/** Mouse print recorder status label.                    */
	private final JLabel                 mousePrintRecorderStatus   = new JLabel();
	/** Private streaming status label.                       */
	private final JLabel                 privateStreamingStatus     = new JLabel();
	
	/** Reference to the hide tray menu item.    */
	private JMenuItem hideTrayMenuItem;
	/** Reference to the restore tray menu item. */
	private JMenuItem restoreTrayMenuItem;
	
	/** Tells if the window size has been initialized. */
	private boolean windowSizeInitialized;
	
	/** Reference to the start page. */
	private StartPage startPage;
	
	/** Reference to the replay auto-saver.    */
	private ReplayAutoSaver replayAutoSaver;
	/** Reference to the SC2 registry monitor. */
	private Sc2RegMonitor   sc2RegMonitor;
	
	/**
	 * Creates the MainFrame.
	 * @param arguments if provided, they will be treated as SC2 replays, and they will opened in analyzers
	 */
	public MainFrame( final String[] arguments ) {
		super( Consts.APPLICATION_NAME + " " + Consts.APPLICATION_VERSION );
		if ( INSTANCE != null )
			throw new RuntimeException( "Only one instance of MainFrame is allowed!" );
		INSTANCE = this;
		
		setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing( final WindowEvent event ) {
				if ( Settings.getBoolean( Settings.KEY_WINDOW_MINIMIZE_TO_TRAY_ON_CLOSE ) )
					minimizeToTrayMenuItem.doClick( 0 );
				else
					exit( false );
			};
			public void windowIconified( final WindowEvent event ) {
				if ( Settings.getBoolean( Settings.KEY_WINDOW_MINIMIZE_TO_TRAY_ON_MINIMIZE ) )
					minimizeToTrayMenuItem.doClick( 0 );
			}
		} );
		
		setIconImage( Icons.SC2GEARS.getImage() );
		
		installTrayIcon();
		
		setSc2GameStatus           ( null  );
		setMousePrintRecorderStatus( false );
		setPrivateStreamingStatus  ( false );
		
		buildGUI();
		
		// Register other global hotkeys
		GlobalHotkeys.addHotkey( new Hotkey( Hotkey.MOD_CTRL | Hotkey.MOD_ALT | Hotkey.MOD_NOREP, KeyEvent.VK_T ) {
			@Override
			public void run() {
				restoreMainFrame();
			}
		} );
		GlobalHotkeys.addHotkey( new Hotkey( Hotkey.MOD_CTRL | Hotkey.MOD_ALT | Hotkey.MOD_NOREP, KeyEvent.VK_M ) {
			@Override
			public void run() {
				if ( MousePrintRecorder.isRecording() ) {
					MousePrintRecorder.stopRecording();
					if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
						Sounds.playSoundSample( Sounds.SAMPLE_RECORDING_STOPPED, false );
				}
				else {
					MousePrintRecorder.startRecording();
					if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
						Sounds.playSoundSample( Sounds.SAMPLE_RECORDING_STARTED, false );
				}
			}
		} );
		GlobalHotkeys.addHotkey( new Hotkey( Hotkey.MOD_CTRL | Hotkey.MOD_ALT | Hotkey.MOD_NOREP, KeyEvent.VK_I ) {
			@Override
			public void run() {
				if ( OnTopGameInfoDialog.isOpened() )
					OnTopGameInfoDialog.close();
				else
					OnTopGameInfoDialog.open( GeneralUtils.getLastReplayFile() );
			}
		} );
		if ( Sc2RegMonitor.supported )
			GlobalHotkeys.addHotkey( new Hotkey( Hotkey.MOD_CTRL | Hotkey.MOD_ALT | Hotkey.MOD_NOREP, KeyEvent.VK_U ) {
				@Override
				public void run() {
					if ( OnTopApmDisplayDialog.isOpened() )
						OnTopApmDisplayDialog.close();
					else
						OnTopApmDisplayDialog.open();
				}
			} );
		
		GeneralUtils.setToolTipDelays();
		
		if ( arguments.length == 0 && trayIcon != null && startMinimizedToTrayMenuItem.isSelected() ) {
			hideMainFrame();
			if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_DISPLAY_INFO_WHEN_STARTED_MINIMIZED ) )			
				trayIcon.displayMessage( null, Language.getText( "trayIcon.startedMinimized", getTitle() ), MessageType.INFO );
		}
		else
			restoreMainFrame();
		
		if ( Settings.getBoolean( Settings.KEY_SETTINGS_CHECK_UPDATES_ON_STARTUP ) )
			checkUpdatesMenuItem.doClick( 0 );
		
		rebuildRecentReplaysMenu();
		
		if ( Settings.getBoolean( Settings.KEY_SETTINGS_SHOW_START_PAGE_ON_STARTUP ) )
			showStartPage( false );
		
		// Start APM alert always for game status detection
		if ( Sc2RegMonitor.supported ) {
			sc2RegMonitor = new Sc2RegMonitor();
			sc2RegMonitor.start();
		}
		
		// Initialize ProfileCache so the Custom portrait list will be downloaded
		// Initialize before opening arguments (so proper custom portraits will be visible)
		try {
			Class.forName( ProfileCache.class.getName() );
		} catch ( final ClassNotFoundException cfe ) {
			// Never to happen
			cfe.printStackTrace();
		}
		
		// Start plugins
		PluginManager.startEnabledPlugins();
		
		if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_GLOBAL_HOTKEYS ) )
			GlobalHotkeys.activate();
		
		// Open arguments if provided
		if ( arguments.length > 0 )
			openArguments( arguments );
		
		if ( startPage != null )
			startPage.refresh();
		
		if ( Settings.getBoolean( Settings.KEY_SHOW_NEW_TO_SC2GEARS_DIALOG_ON_STARTUP ) )
			new NewToSc2gearsDialog();
		
		// Initialize ReplayCache so the replay cache will be ready if needed
		// Initialize after opening arguments (so replay will be opened right away (without having to wait for cache initialization, and if argument triggers a search, it will initialize replay cache anyway)
		try {
			Class.forName( ReplayCache.class.getName() );
		} catch ( final ClassNotFoundException cfe ) {
			// Never to happen
			cfe.printStackTrace();
		}
		
		// Application has started, main frame is visible (unless the app is started minimized)
		// Do some background initialization (current thread now runs in the background)
		
		setupDropTarget();
		
		// Pre-load Sc2 icons
		if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_PRELOAD_SC2_ICONS_ON_STARTUP ) )
			preloadSc2Icons();
	}
	
	/**
	 * If a Start page window is already created, makes it visible. Else creates a new one.
	 * @param refresh tells if the content has to be refreshed in case the internal frame was just created
	 */
	private void showStartPage( final boolean refreshIfNew ) {
		if ( startPage == null ) {
			addNewInternalFrame( startPage = new StartPage() );
			startPageNode.setUserObject( startPage );
			final TreePath startPageNodePath = new TreePath( startPageNode.getPath() );
			navigationTree.setSelectionPath( startPageNodePath );
			startPage.addInternalFrameListener( new InternalFrameAdapter() {
				@Override
				public void internalFrameClosing( final InternalFrameEvent event ) {
					startPage = null;
					startPageNode.setUserObject( Language.getText( "module.startPage.title" ) );
				}
				@Override
				public void internalFrameActivated( final InternalFrameEvent event ) {
					if ( !startPage.isIcon() ) {
						navigationTree.setSelectionPath( startPageNodePath );
						navigationTree.scrollPathToVisible( startPageNodePath );
					}
				}
			} );
			if ( refreshIfNew )
				startPage.refresh();
		}
		else {
			selectFrame( startPage );
		}
	}
	
	/**
	 * Opens the arguments.
	 * @param arguments arguments of the program
	 */
	public void openArguments( final String[] arguments ) {
		// The invokeLater is required else the navigation tree would not be updated properly (on startup)
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				for ( final String argument : arguments ) {
					final File file = new File( argument );
					if ( GuiUtils.SC2_REPLAY_LIST_FILE_FILTER.accept( file ) )
						createNewInternalFrame( newRepSearchNode, null, file );
					else if ( GuiUtils.SC2_REPLAY_SOURCE_FILE_FILTER.accept( file ) )
						createNewInternalFrame( newRepSearchNode, file, null );
					else
						openReplayFile( new File( argument ) ); // Else try it as an SC2Replay file even if extension does not match
				}
			}
		} );
	}
	
	/**
	 * Pre-load Sc2 icons.
	 */
	private void preloadSc2Icons() {
		for ( final Building building : EnumCache.BUILDINGS )
			Icons.getBuildingIcon( building, IconSize.BIG );
		for ( final Unit unit : Unit.values() )
			Icons.getUnitIcon( unit, IconSize.BIG );
		for ( final AbilityGroup abilityGroup : AbilityGroup.values() )
			Icons.getAbilityGroupIcon( abilityGroup, IconSize.BIG );
		for ( final Research research : Research.values() )
			Icons.getResearchIcon( research, IconSize.BIG );
		for ( final Upgrade upgrade : Upgrade.values() )
			Icons.getUpgradeIcon( upgrade, IconSize.BIG );
	}
	
	/**
	 * Enables dragging and dropping files onto Sc2gears.<br>
	 * If the dropped file list contains only a single replay (amongst other files), that will be opened in the Replay analyzer.<br>
	 * If the dropped file list contains multiple replays and/or folders, those will be opened in a replay search.<br>
	 * If the dropped file list contains replay sources and/or replay lists, those will be opened respectively in replay searches.<br>
	 */
	private void setupDropTarget() {
		new DropTarget( this, new DropTargetAdapter() {
			@Override
			public void drop( final DropTargetDropEvent event ) {
				final Transferable transferable = event.getTransferable();
				for ( final DataFlavor flavor : transferable.getTransferDataFlavors() ) {
					if ( flavor.isFlavorJavaFileListType() ) {
						// It's a file list, accept it!
						event.acceptDrop( DnDConstants.ACTION_COPY_OR_MOVE );
						try {
							@SuppressWarnings("unchecked")
							final List< File > fileList       = (List< File >) transferable.getTransferData( flavor );
							final List< File > replayFileList = new ArrayList< File >();
							
							for ( final File file : fileList ) {
								if ( file.isDirectory() )
									replayFileList.add( file );
								else {
									if ( GuiUtils.SC2_REPLAY_LIST_FILE_FILTER.accept( file ) )
										createNewInternalFrame( newRepSearchNode, null, file );
									else if ( GuiUtils.SC2_REPLAY_SOURCE_FILE_FILTER.accept( file ) )
										createNewInternalFrame( newRepSearchNode, file, null );
									else
										replayFileList.add( file );
								}
							}
							
							if ( replayFileList.size() == 1 && replayFileList.get( 0 ).isFile() )
								openReplayFile( replayFileList.get( 0 ) );
							else if ( replayFileList.size() > 0 ) // It might contain 1 folder only...
								createNewInternalFrame( newRepSearchNode, null, null, replayFileList.toArray( new File[ replayFileList.size() ] ) );
							
							event.dropComplete( true );
							
						} catch ( final Exception e ) {
							e.printStackTrace();
							event.rejectDrop();
						}
						break;
					}
				}
			}
		} );
	}
	
	/**
	 * Installs a system tray icon if supported.
	 */
	private void installTrayIcon() {
		if ( SystemTray.isSupported() ) {
			final TrayIcon trayIcon = new TrayIcon( Icons.SC2GEARS.getImage(), Language.getText( "trayIcon.running", getTitle() ) );
			trayIcon.setImageAutoSize( true );
			
			trayIconPopupMenu = new JPopupMenu();
			
			hideTrayMenuItem = new JMenuItem( Language.getText( "tray.hide" ), Icons.APPLICATION_DOCK_TAB );
			hideTrayMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					hideMainFrame();
				}
			} );
			trayIconPopupMenu.add( hideTrayMenuItem );
			restoreTrayMenuItem = new JMenuItem( Language.getText( "tray.restore" ), Icons.APPLICATION_RESIZE );
			restoreTrayMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					restoreMainFrame();
				}
			} );
			trayIconPopupMenu.add( restoreTrayMenuItem );
			trayIconPopupMenu.addSeparator();
			enableReplayAutoSaveTrayMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					enableReplayAutoSaveMenuItem.doClick();
				}
			} );
			trayIconPopupMenu.add( enableReplayAutoSaveTrayMenuItem );
			if ( Sc2RegMonitor.supported )
				enableApmAlertTrayMenuItem.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						enableApmAlertMenuItem.doClick();
					}
				} );
			else
				enableApmAlertTrayMenuItem.setEnabled( false );
			trayIconPopupMenu.add( enableApmAlertTrayMenuItem );
			trayIconPopupMenu.addSeparator();
			final JMenuItem openLastReplayMenuItem = new JMenuItem( Language.getText( "tray.openLastReplay" ), Icons.CHART );
			openLastReplayMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					restoreMainFrame();
					openLastReplayFile();
				}
			} );
			trayIconPopupMenu.add( openLastReplayMenuItem );
			trayIconPopupMenu.addSeparator();
			final JMenuItem startStarCraft2MenuItem = new JMenuItem( Language.getText( "tray.startStarCraft2" ), Icons.SC2 );
			startStarCraft2MenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.startStarCraftII();
				}
			} );
			trayIconPopupMenu.add( startStarCraft2MenuItem );
			final JMenuItem startStarCraft2EditorMenuItem = new JMenuItem( Language.getText( "tray.startStarCraft2Editor" ), Icons.SC2_EDITOR );
			startStarCraft2EditorMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.startStarCraftIIEditor();
				}
			} );
			trayIconPopupMenu.add( startStarCraft2EditorMenuItem );
			trayIconPopupMenu.addSeparator();
			final JMenuItem exitMenuItem = new JMenuItem( Language.getText( "tray.exit" ), Icons.DOOR_OPEN_IN );
			exitMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					exit( false );
				}
			} );
			trayIconPopupMenu.add( exitMenuItem );
			
			try {
				SystemTray.getSystemTray().add( trayIcon );
				this.trayIcon = trayIcon;
				
				trayIcon.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						restoreMainFrame();
					}
				} );
				
				final JDialog helperDialog = new JDialog();
				helperDialog.setUndecorated( true );
				helperDialog.setAlwaysOnTop( true );
				trayIconPopupMenu.addPopupMenuListener( new PopupMenuListener() {
					@Override
					public void popupMenuWillBecomeVisible( final PopupMenuEvent event ) {
					}
					@Override
					public void popupMenuWillBecomeInvisible( final PopupMenuEvent event ) {
						helperDialog.setVisible( false );
					}
					@Override
					public void popupMenuCanceled( final PopupMenuEvent event ) {
						helperDialog.setVisible( false );
					}
				} );
				
				trayIcon.addMouseListener( new MouseAdapter() {
					@Override
					public void mouseReleased( final MouseEvent event ) {
						if ( event.isPopupTrigger() ) {
							helperDialog.setLocation( event.getX(), event.getY() - trayIconPopupMenu.getPreferredSize().height );
							helperDialog.setVisible( true );
							trayIconPopupMenu.show( helperDialog.getContentPane(), 0, 0 );
							helperDialog.toFront();
						}
					}
				} );
			} catch ( final AWTException ae ) {
			}
		}
	}
	
	/**
	 * Builds the GUI of the frame.
	 */
	private void buildGUI() {
		buildNavigationBar();
		
		mainSplitPane.setDividerSize( 6 );
		getContentPane().add( mainSplitPane );
		arrangeContent();
		
		buildMenuBar();
	}
	
	/**
	 * Builds the navigation bar.
	 */
	private void buildNavigationBar() {
		final DefaultMutableTreeNode navigationRoot = new DefaultMutableTreeNode();
		
		navigationRoot.add( startPageNode );
		
		repAnalNode.add( newRepAnalNode );
		navigationRoot.add( repAnalNode );
		multiRepAnalNode.add( newMultiRepAnalNode );
		navigationRoot.add( multiRepAnalNode );
		repSearchNode.add( newRepSearchNode );
		navigationRoot.add( repSearchNode );
		navigationRoot.add( repSourcesNode );
		navigationRoot.add( repListsNode );
		pluginsNode.add( pluginsInfoNode );
		navigationRoot.add( pluginsNode );
		refreshNavigationTree();
		
		( (DefaultTreeModel) navigationTree.getModel() ).setRoot( navigationRoot );
		navigationTree.setRootVisible( false );
		navigationTree.setShowsRootHandles( true );
		navigationTree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
		
		navigationTree.setCellRenderer( createTreeCellRenderer() );
		
		if ( Settings.getBoolean( Settings.KEY_NAVIGATION_USE_SMALL_FONT ) )
			navigationTree.setFont( new Font( navigationTree.getFont().getName(), Font.PLAIN, 10 ) );
		expandAllNavigationRow();
		
		navigationTree.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked( final MouseEvent event ) {
				if ( event.getButton() == GuiUtils.MOUSE_BUTTON_RIGHT || event.getClickCount() == 2 ) {
					final TreePath path = navigationTree.getPathForLocation( event.getX(), event.getY() );
					if ( path == null )
						return;
					
					final DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
					final DefaultMutableTreeNode parentNode  = (DefaultMutableTreeNode) clickedNode.getParent();
					
					if ( parentNode == repSourcesNode || parentNode == repListsNode ) {
						if ( clickedNode == emptyRepSourceNode )
							GuiUtils.showInfoDialog( Language.getText( "navigationTree.info.newRepSource" ) );
						else if ( clickedNode == emptyRepListNode )
							GuiUtils.showInfoDialog( Language.getText( "navigationTree.info.newRepList" ) );
						else {
							final File   file;
							final File[] files;
							if ( parentNode == repSourcesNode ) {
								if ( clickedNode == sc2AutoRepsRepSourceNode ) {
									files = GeneralUtils.getAutoRepFolderList().toArray( new File[ 0 ] );
									file  = null;
								} else if ( clickedNode == autoSavedRepsRepSourceNode ) {
									file  = new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_REPLAY_AUTO_SAVE ) );
									files = null;
								} else {
									file  = new File( Consts.FOLDER_REPLAY_SOURCES, clickedNode.getUserObject().toString() + Consts.EXT_SC2REPLAY_SOURCE );
									files = null;
								}
							}
							else {
								file  = new File( Consts.FOLDER_REPLAY_LISTS, clickedNode.getUserObject().toString() + Consts.EXT_SC2REPLAY_LIST );
								files = null;
							}
							if ( event.getButton() == GuiUtils.MOUSE_BUTTON_RIGHT ) {
								navigationTree.setSelectionPath( path );
								final JPopupMenu popup = new JPopupMenu();
								final JMenuItem openInMultiRepAnalysisMenuItem = new JMenuItem( Icons.CHART_UP_COLOR );
								GuiUtils.updateButtonText( openInMultiRepAnalysisMenuItem, "navigationTree.popup.openInMultiRepAnalysis" );
								openInMultiRepAnalysisMenuItem.addActionListener( new ActionListener() {
									@Override
									public void actionPerformed( final ActionEvent event ) {
										if ( clickedNode == sc2AutoRepsRepSourceNode || clickedNode == autoSavedRepsRepSourceNode )
											createNewInternalFrame( newMultiRepAnalNode, null, null, clickedNode == sc2AutoRepsRepSourceNode ? files : new File[] { file } );
										else if ( parentNode == repSourcesNode )
											createNewInternalFrame( newMultiRepAnalNode, file, null, null );
										else
											createNewInternalFrame( newMultiRepAnalNode, null, file, null );
									}
								} );
								popup.add( openInMultiRepAnalysisMenuItem );
								final JMenu applyFilterMenu = new JMenu( Language.getText( "navigationTree.popup.applyFilter" ) );
								applyFilterMenu.setIcon( Icons.BINOCULAR_ARROW );
								final String[] filterFileNames = new File( Consts.FOLDER_SEARCH_FILTERS ).list( new FilenameFilter() {
									@Override
									public boolean accept( final File dir, final String name ) {
										return name.toLowerCase().endsWith( Consts.EXT_SEARCH_FILTER );
									}
								} );
								if ( filterFileNames != null ) {
									Arrays.sort( filterFileNames, String.CASE_INSENSITIVE_ORDER );
									for ( final String filterFileName : filterFileNames ) {
										final JMenuItem filterMenuItem = new JMenuItem( filterFileName.substring( 0, filterFileName.lastIndexOf( '.' ) ), Icons.EDIT_COLUMN );
										filterMenuItem.addActionListener( new ActionListener() {
											@Override
											public void actionPerformed( final ActionEvent event ) {
												final File filtersFile = new File( Consts.FOLDER_SEARCH_FILTERS, filterFileName );
												if ( parentNode == repSourcesNode ) {
													if ( clickedNode == sc2AutoRepsRepSourceNode || clickedNode == autoSavedRepsRepSourceNode )
														createNewInternalFrame( newRepSearchNode, null, null, clickedNode == sc2AutoRepsRepSourceNode ? files : new File[] { file }, Boolean.TRUE, filtersFile );
													else
														createNewInternalFrame( newRepSearchNode, file, null, null, Boolean.TRUE, filtersFile );
												}
												else
													createNewInternalFrame( newRepSearchNode, null, file, null, Boolean.TRUE, filtersFile );
											}
										} );
										applyFilterMenu.add( filterMenuItem );
									}
								}
								popup.add( applyFilterMenu );
								final JMenuItem listAllReplaysMenuItem = new JMenuItem( Icons.BINOCULAR_ARROW );
								GuiUtils.updateButtonText( listAllReplaysMenuItem, "navigationTree.popup.listAllReplays" );
								listAllReplaysMenuItem.addActionListener( new ActionListener() {
									@Override
									public void actionPerformed( final ActionEvent event ) {
										if ( parentNode == repSourcesNode ) {
											if ( clickedNode == sc2AutoRepsRepSourceNode || clickedNode == autoSavedRepsRepSourceNode )
												createNewInternalFrame( newRepSearchNode, null, null, clickedNode == sc2AutoRepsRepSourceNode ? files : new File[] { file } );
											else
												createNewInternalFrame( newRepSearchNode, file, null );
										}
										else
											createNewInternalFrame( newRepSearchNode, null, file );
									}
								} );
								popup.add( listAllReplaysMenuItem );
								final JMenuItem openInRepSearchForFilteringMenuItem = new JMenuItem( Icons.EDIT_COLUMN );
								GuiUtils.updateButtonText( openInRepSearchForFilteringMenuItem, "navigationTree.popup.openInRepSearchForFiltering" );
								openInRepSearchForFilteringMenuItem.addActionListener( new ActionListener() {
									@Override
									public void actionPerformed( final ActionEvent event ) {
										if ( parentNode == repSourcesNode ) {
											if ( clickedNode == sc2AutoRepsRepSourceNode || clickedNode == autoSavedRepsRepSourceNode )
												createNewInternalFrame( newRepSearchNode, null, null, clickedNode == sc2AutoRepsRepSourceNode ? files : new File[] { file }, Boolean.FALSE );
											else
												createNewInternalFrame( newRepSearchNode, file, null, null, Boolean.FALSE );
										}
										else
											createNewInternalFrame( newRepSearchNode, null, file, null, Boolean.FALSE );
									}
								} );
								popup.add( openInRepSearchForFilteringMenuItem );
								popup.addSeparator();
								final JMenuItem renameItemMenuItem = new JMenuItem( Icons.DOCUMENT_RENAME );
								if ( clickedNode == sc2AutoRepsRepSourceNode || clickedNode == autoSavedRepsRepSourceNode )
									renameItemMenuItem.setEnabled( false );
								GuiUtils.updateButtonText( renameItemMenuItem, "navigationTree.popup.renameItem" );
								renameItemMenuItem.addActionListener( new ActionListener() {
									@Override
									public void actionPerformed( final ActionEvent event ) {
										new RenameItemDialog( clickedNode, file );
									}
								} );
								popup.add( renameItemMenuItem );
								final JMenuItem deleteItemMenuItem = new JMenuItem( Icons.CROSS );
								if ( clickedNode == sc2AutoRepsRepSourceNode || clickedNode == autoSavedRepsRepSourceNode )
									deleteItemMenuItem.setEnabled( false );
								GuiUtils.updateButtonText( deleteItemMenuItem, "navigationTree.popup.deleteItem" );
								deleteItemMenuItem.addActionListener( new ActionListener() {
									@Override
									public void actionPerformed( final ActionEvent event ) {
										if ( GuiUtils.showConfirmDialog( Language.getText( "navigationTree.popup.areYouSureToDelete", GeneralUtils.getFileNameWithoutExt( file ) ), true ) == 0 )
											if ( file.delete() )
												refreshNavigationTree();
									}
								} );
								popup.add( deleteItemMenuItem );
								popup.addSeparator();
								final JMenuItem storeItemMenuItem = new JMenuItem( Icons.SERVER_NETWORK );
								if ( clickedNode == sc2AutoRepsRepSourceNode || clickedNode == autoSavedRepsRepSourceNode )
									storeItemMenuItem.setEnabled( false );
								GuiUtils.updateButtonText( storeItemMenuItem, "navigationTree.popup.storeItem" );
								storeItemMenuItem.addActionListener( new ActionListener() {
									@Override
									public void actionPerformed( final ActionEvent event ) {
										 final String comment = parentNode == repSourcesNode ? Language.getText( "navigationTree.popup.replaySourceComment" )
											 : parentNode == repListsNode ? Language.getText( "navigationTree.popup.replayListComment" ) : "";
										GeneralUtils.storeOtherFile( file, comment, "navigationTree.popup.storingItem" );
									}
								} );
								popup.add( storeItemMenuItem );
								popup.show( navigationTree, event.getX(), event.getY() );
							}
							else {
								if ( parentNode == repSourcesNode ) {
									if ( clickedNode == sc2AutoRepsRepSourceNode || clickedNode == autoSavedRepsRepSourceNode )
										createNewInternalFrame( newRepSearchNode, null, null, clickedNode == sc2AutoRepsRepSourceNode ? files : new File[] { file } );
									else
										createNewInternalFrame( newRepSearchNode, file, null );
								}
								else
									createNewInternalFrame( newRepSearchNode, null, file );
							}
						}
					}
					else
						if ( parentNode != pluginsNode && event.getClickCount() == 2 )
							createNewInternalFrame( clickedNode );
				}
			};
		} );
		
		navigationTree.addTreeSelectionListener( new TreeSelectionListener() {
			@Override
			public void valueChanged( final TreeSelectionEvent event ) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
				if ( node.getUserObject() instanceof JInternalFrame )
					selectFrame( (JInternalFrame) node.getUserObject() );
				else if ( node == startPageNode )
					showStartPage( true );// Start page is closed (else user object would be a JInternalFrame)
			}
		} );
		
		navigationBar.setViewportView( navigationTree );
		
		navigationBox.add( GuiUtils.wrapInBorderPanel( navigationBar ) );
		for ( final JLabel statusLabel : new JLabel[] { sc2GameStatusLabel, mousePrintRecorderStatus, privateStreamingStatus } ) {
			if ( statusLabel == sc2GameStatusLabel && !Sc2RegMonitor.supported )
				continue;
			statusLabel.setMinimumSize( new Dimension( 0, statusLabel.getMinimumSize().height ) );
			statusLabel.setAlignmentX( 0.5f );
			final JPanel wrapper = new JPanel( new FlowLayout( FlowLayout.LEFT, 2, 2 ) );
			wrapper.add( statusLabel );
			navigationBox.add( wrapper );
			if ( statusLabel != sc2GameStatusLabel )
				statusLabel.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		}
		mousePrintRecorderStatus.addMouseListener( new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				MousePrintRecorder.showFrame();
			};
		} );
		privateStreamingStatus.addMouseListener( new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				PrivateVideoStreaming.showFrame();
			};
		} );
		navigationBox.add( Box.createVerticalStrut( 12 ) );
		final JPanel wrapper = new JPanel( new FlowLayout( FlowLayout.LEFT, 2, 2 ) );
		wrapper.add( new JLabel( Language.getText( "general.memoryUsage" ), Icons.MEMORY, JLabel.LEFT ) );
		navigationBox.add( wrapper );
		final JComponent memorySatusComponent;
		navigationBox.add( memorySatusComponent = new JComponent() {
			private static final int HEIGHT = 20;
			private long allocated, free, max;
			@Override public Dimension getPreferredSize() { return new Dimension( 1                , HEIGHT ); }
			@Override public Dimension getMaximumSize  () { return new Dimension( Integer.MAX_VALUE, HEIGHT ); }
			@Override public Dimension getMinimumSize  () { return new Dimension( 0                , HEIGHT ); }
			@Override
			protected void paintComponent( final Graphics g ) {
				final Runtime runtime = Runtime.getRuntime();
				allocated   = runtime.totalMemory();
				free        = runtime.freeMemory();
				max         = runtime.maxMemory();
				
				final int width  = getWidth ();
				final int height = getHeight();
				if ( width == 0 || height == 0 )
					return;
				g.setColor( PlayerColor.GREEN.color );
				g.fillRect( 0, 0, width, height );
				g.setColor( PlayerColor.ORANGE.color );
				g.fillRect( 1, 1, (int) ( allocated * width / max ), height-2 );
				g.setColor( PlayerColor.RED.color );
				g.fillRect( 1, 1, (int) ( ( allocated - free ) * width / max ), height-2 );
				
				// Display used / allocated
				g.setFont( g.getFont().deriveFont( Font.BOLD ) );
				final FontMetrics fontMetrics = g.getFontMetrics();
				final String      memString   = ( ( allocated - free ) >> 20 ) + " MB / " + ( allocated >> 20 ) + " MB";
				final int         x           = width > fontMetrics.stringWidth( memString ) ? ( width - fontMetrics.stringWidth( memString ) ) >> 1 : 0;
				final int         y           = fontMetrics.getAscent();
				g.setColor( Color.BLACK );
				g.drawString( memString, x + 1, y + 1 );
				g.setColor( Color.WHITE );
				g.drawString( memString, x, y );
			}
			@Override
			public String getToolTipText( final MouseEvent event ) {
				final long totalFreeMB = ( free + ( max - allocated ) ) >> 20;
				return Language.getText( "general.memoryUsage" ) + " " + Language.getText( "diagnosticTool.test.checkMemory.memoryValues", allocated >> 20, free >> 20, max >> 20, totalFreeMB );
			}
		} );
		memorySatusComponent.setToolTipText( "" ); // To turn on tool tips
		new Timer( "Memory Status Timer" ).schedule( new TimerTask() {
			@Override
			public void run() {
				memorySatusComponent.repaint();
			}
		}, 0, 1500 );
		
		navigationBox.setVisible( Settings.getBoolean( Settings.KEY_NAVIGATION_SHOW_BAR ) );
	}
	
	/**
	 * Sets the SC2 game status.
	 * @param started SC2 game status to be set
	 */
	public void setSc2GameStatus( final Boolean started ) {
		sc2GameStatusLabel.setText( Language.getText( started == null ? "general.statusIndicator.sc2NoGame" : started ? "general.statusIndicator.sc2GameStarted" : "general.statusIndicator.sc2GameEnded" ) );
		// TODO different icons (at least a grayed version)
		sc2GameStatusLabel.setIcon( Icons.SC2 );
	}
	
	/**
	 * Sets the mouse print recorder status.
	 * @param on mouse print recorder status to be set
	 */
	public void setMousePrintRecorderStatus( final boolean on ) {
		mousePrintRecorderStatus.setText( GeneralUtils.createHtmlLink( Language.getText( on ? "general.statusIndicator.mousePrintRecorderOn" : "general.statusIndicator.mousePrintRecorderOff" ) ) );
		mousePrintRecorderStatus.setIcon( on ? Icons.FINGERPRINT_RECOGNITION : Icons.FINGERPRINT  );
	}
	
	/**
	 * Sets the private streaming status.
	 * @param on private streaming status to be set
	 */
	public void setPrivateStreamingStatus( final boolean on ) {
		privateStreamingStatus.setText( GeneralUtils.createHtmlLink( Language.getText( on ? "general.statusIndicator.privateVideoStreamingOn" : "general.statusIndicator.privateVideoStreamingOff" ) ) );
		privateStreamingStatus.setIcon( on ? Icons.MONITOR_CAST : Icons.MONITOR_MEDIUM );
	}
	
	/**
	 * Selects the specified internal frame. If it is iconified, first it will be de-iconified.
	 * @param iframe
	 */
	private void selectFrame( final JInternalFrame iframe ) {
		try {
			if ( iframe.isIcon() )
				iframe.setIcon( false);
			iframe.setSelected( true );
		} catch ( final PropertyVetoException pve) {
			pve.printStackTrace();
		}
	}
	
	/**
	 * Creates a custom tree cell renderer for the navigation tree which sets custom leaf icons.
	 * @return a custom tree cell renderer
	 */
	private TreeCellRenderer createTreeCellRenderer() {
		return new DefaultTreeCellRenderer() {
			public Component getTreeCellRendererComponent( final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus ) {
				super.getTreeCellRendererComponent( tree, value, selected, expanded, leaf, row, hasFocus );
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				if ( node != newRepAnalNode && node != newMultiRepAnalNode && node != newRepSearchNode && node != emptyRepSourceNode && node != emptyRepListNode && node != pluginsInfoNode ) {
					if ( node == sc2AutoRepsRepSourceNode || node == autoSavedRepsRepSourceNode )
						setIcon( Icons.FOLDER_BOOKMARK );
					else if ( node == startPageNode )
						setIcon( Icons.NEWSPAPER );
					else {
						final TreeNode parentNode = node.getParent();
						if ( parentNode == repAnalNode )
							setIcon( Icons.CHART );
						else if ( parentNode == multiRepAnalNode )
							setIcon( Icons.CHART_UP_COLOR );
						else if ( parentNode == repSearchNode )
							setIcon( Icons.BINOCULAR );
						else if ( parentNode == repSourcesNode )
							setIcon( Icons.FOLDERS_STACK );
						else if ( parentNode == repListsNode )
							setIcon( Icons.TABLE_EXCEL );
						else if ( parentNode == pluginsNode )
							setIcon( Icons.PUZZLE );
					}
				}
				return this;
			};
		};
	}
	
	/**
	 * Expands all navigation row.
	 */
	private void expandAllNavigationRow() {
		for ( int i = 0; i < navigationTree.getRowCount(); i++ )
			navigationTree.expandRow( i );
	}
	
	/**
	 * Refreshes the navigation tree.
	 */
	public void refreshNavigationTree() {
		// Only need to refresh the rep sources and rep lists node, the rest are always up-to-date
		for ( int i = 0; i < 2; i++ ) { // 2 nodes to rebuild
			final DefaultMutableTreeNode node = i == 0 ? repSourcesNode : repListsNode;
			node.removeAllChildren();
			
			if ( node == repSourcesNode ) {
				node.add( sc2AutoRepsRepSourceNode   );
				node.add( autoSavedRepsRepSourceNode );
			}
			
			final String folderName = i == 0 ? Consts.FOLDER_REPLAY_SOURCES : Consts.FOLDER_REPLAY_LISTS;
			final String extension  = i == 0 ? Consts.EXT_SC2REPLAY_SOURCE  : Consts.EXT_SC2REPLAY_LIST;
			
			final String[] fileNames = new File( folderName ).list( new FilenameFilter() {
				@Override
				public boolean accept( final File dir, final String name ) {
					return name.toLowerCase().endsWith( extension );
				}
			} );
			
			if ( fileNames == null || fileNames.length == 0 ) {
				node.add( node == repSourcesNode ? emptyRepSourceNode : emptyRepListNode );
			}
			else {
				Arrays.sort( fileNames, String.CASE_INSENSITIVE_ORDER );
				for ( final String fileName : fileNames )
					node.add( new DefaultMutableTreeNode( fileName.substring( 0, fileName.lastIndexOf( '.' ) ) ) );
			}
			
			( (DefaultTreeModel) navigationTree.getModel() ).reload( node );
		}
	}
	
	/**
	 * Refreshes the Replay analyzer node in the navigation tree.
	 */
	public void refreshRepAnalNavigationNode() {
		( (DefaultTreeModel) navigationTree.getModel() ).reload( repAnalNode );
	}
	
	/**
	 * Builds the menu bar.
	 */
	private void buildMenuBar() {
		final JMenuBar menuBar = new JMenuBar();
		
		final JMenu fileMenu = new JMenu();
		fileMenu.setIcon( Icons.DISK );
		GuiUtils.updateButtonText( fileMenu, "menu.file" );
		{
			final JMenuItem openReplayMenuItem = new JMenuItem( Icons.CHART );
			GuiUtils.updateButtonText( openReplayMenuItem, "menu.file.openReplay" );
			openReplayMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK ) );
			openReplayMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					createNewInternalFrame( newRepAnalNode );
				}
			} );
			fileMenu.add( openReplayMenuItem );
			
			GuiUtils.updateButtonText( openLastReplayMenuItem, "menu.file.openLastReplay" );
			openLastReplayMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK ) );
			openLastReplayMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					openLastReplayFile();
				}
			} );
			fileMenu.add( openLastReplayMenuItem );
			
			final JMenuItem shareReplayMenuItem = new JMenuItem( Icons.DOCUMENT_SHARE );
			GuiUtils.updateButtonText( shareReplayMenuItem, "menu.file.shareReplays" );
			shareReplayMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new ShareReplaysDialog( null );
				}
			} );
			fileMenu.add( shareReplayMenuItem );
			
			final JMenuItem myShareReplaysMenuItem = new JMenuItem( Icons.DOCUMENT_SHARE );
			GuiUtils.updateButtonText( myShareReplaysMenuItem, "menu.file.mySharedReplays" );
			myShareReplaysMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new MySharedReplaysDialog();
				}
			} );
			fileMenu.add( myShareReplaysMenuItem );
			
			final JMenuItem importBOMenuItem = new JMenuItem( Icons.BLOCK_ARROW );
			GuiUtils.updateButtonText( importBOMenuItem, "menu.file.importBuildOrders" );
			importBOMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new ImportBODialog();
				}
			} );
			fileMenu.add( importBOMenuItem );
			
			fileMenu.addSeparator();
			
			GuiUtils.updateButtonText( recentReplaysMenu, "menu.file.recentReplays" );
			fileMenu.add( recentReplaysMenu );
			
			fileMenu.addSeparator();
			
			final JMenuItem startStarCraft2MenuItem = new JMenuItem( Icons.SC2 );
			GuiUtils.updateButtonText( startStarCraft2MenuItem, "menu.file.startStarCraft2" );
			startStarCraft2MenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.startStarCraftII();
				}
			} );
			fileMenu.add( startStarCraft2MenuItem );
			
			final JMenuItem startStarCraft2MapEditorMenuItem = new JMenuItem( Icons.SC2_EDITOR );
			GuiUtils.updateButtonText( startStarCraft2MapEditorMenuItem, "menu.file.startStarCraft2Editor" );
			startStarCraft2MapEditorMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.startStarCraftIIEditor();
				}
			} );
			fileMenu.add( startStarCraft2MapEditorMenuItem );
			
			fileMenu.addSeparator();
			
			final JMenuItem exitMenuItem = new JMenuItem( Icons.DOOR_OPEN_IN );
			GuiUtils.updateButtonText( exitMenuItem, "menu.file.exit" );
			exitMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X, InputEvent.ALT_DOWN_MASK ) );
			exitMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					exit( false );
				}
			} );
			fileMenu.add( exitMenuItem );
		}
		menuBar.add( fileMenu );
		
		final JMenu navigationMenu = new JMenu();
		navigationMenu.setIcon( Icons.APPLICATION_SIDEBAR );
		GuiUtils.updateButtonText( navigationMenu, "menu.navigation" );
		{
			final JMenuItem expandAllMenuItem = new JMenuItem( Icons.TOGGLE_EXPAND );
			GuiUtils.updateButtonText( expandAllMenuItem, "menu.navigation.expandAll" );
			expandAllMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					expandAllNavigationRow();
				}
			} );
			navigationMenu.add( expandAllMenuItem );
			final JMenuItem collapseAllMenuItem = new JMenuItem( Icons.TOGGLE );
			GuiUtils.updateButtonText( collapseAllMenuItem, "menu.navigation.collapseAll" );
			collapseAllMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					for ( int i = 0; i < navigationTree.getRowCount(); i++ )
						navigationTree.collapseRow( i );
				}
			} );
			navigationMenu.add( collapseAllMenuItem );
			final JMenuItem refreshTreeMenuItem = new JMenuItem( Icons.ARROW_CIRCLE_315 );
			GuiUtils.updateButtonText( refreshTreeMenuItem, "menu.navigation.refreshTree" );
			refreshTreeMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					refreshNavigationTree();
				}
			} );
			navigationMenu.add( refreshTreeMenuItem );
			
			navigationMenu.addSeparator();
			
			final JCheckBoxMenuItem useSmallFontMenuItem = GuiUtils.createCheckBoxMenuItem( "menu.navigation.useSmallFont", Settings.KEY_NAVIGATION_USE_SMALL_FONT, Icons.APPLICATION_SIDEBAR_LIST );
			final JMenu navigationBarPositionMenu = new JMenu();
			final JCheckBoxMenuItem showNavigationBarMenuItem = GuiUtils.createCheckBoxMenuItem( "menu.navigation.showNavbar", Settings.KEY_NAVIGATION_SHOW_BAR, Icons.APPLICATION_SIDEBAR );
			showNavigationBarMenuItem.addActionListener( new ActionListener() {
				private int storedLeftComponentWidth = Settings.getInt( Settings.KEY_SETTINGS_MISC_NAV_BAR_INITIAL_WIDTH );
				@Override
				public void actionPerformed( final ActionEvent event ) {
					if ( !showNavigationBarMenuItem.isSelected() )
						storedLeftComponentWidth = mainSplitPane.getLeftComponent().getWidth();
					navigationBox.setVisible( showNavigationBarMenuItem.isSelected() );
					useSmallFontMenuItem.setEnabled( showNavigationBarMenuItem.isSelected() );
					navigationBarPositionMenu.setEnabled( showNavigationBarMenuItem.isSelected() );
					if ( showNavigationBarMenuItem.isSelected() )
						mainSplitPane.setDividerLocation( storedLeftComponentWidth );
					mainSplitPane.validate();
				}
			} );
			navigationMenu.add( showNavigationBarMenuItem );
			
			useSmallFontMenuItem.setEnabled( Settings.getBoolean( Settings.KEY_NAVIGATION_SHOW_BAR ) );
			useSmallFontMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					navigationTree.setFont( new Font( navigationTree.getFont().getName(), Font.PLAIN, useSmallFontMenuItem.isSelected() ? 10 : 12 ) );
				}
			} );
			navigationMenu.add( useSmallFontMenuItem );
			
			navigationBarPositionMenu.setEnabled( Settings.getBoolean( Settings.KEY_NAVIGATION_SHOW_BAR ) );
			GuiUtils.updateButtonText( navigationBarPositionMenu, "menu.navigation.navbarPosition" );
			{
				final JRadioButtonMenuItem[] navigationBarPositionMenuItems = new JRadioButtonMenuItem[] { new JRadioButtonMenuItem( Icons.APPLICATION_DOCK_180 ), new JRadioButtonMenuItem( Icons.APPLICATION_DOCK ) } ;
				GuiUtils.updateButtonText( navigationBarPositionMenuItems[ 0 ], "menu.navigation.navbarPosition.left" );
				GuiUtils.updateButtonText( navigationBarPositionMenuItems[ 1 ], "menu.navigation.navbarPosition.right" );
				navigationBarPositionMenuItems[ Settings.getInt( Settings.KEY_NAVIGATION_BAR_POSITION ) ].setSelected( true );
				
				final ButtonGroup buttonGroup = new ButtonGroup();
				for ( int i = 0; i < navigationBarPositionMenuItems.length; i++ ) {
					final JRadioButtonMenuItem navigationBarPositionMenuItem = navigationBarPositionMenuItems[ i ];
					buttonGroup.add( navigationBarPositionMenuItem );
					final int i_ = i; // Final required for the inner class
					navigationBarPositionMenuItem.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed( final ActionEvent event ) {
							Settings.set( Settings.KEY_NAVIGATION_BAR_POSITION, i_ );
							arrangeContent();
						}
					} );
					navigationBarPositionMenu.add( navigationBarPositionMenuItem );
				}
			}
			navigationMenu.add( navigationBarPositionMenu );
		}
		menuBar.add( navigationMenu );
		
		final JMenu settingsMenu = new JMenu();
		settingsMenu.setIcon( Icons.WRENCH_SCREWDRIVER );
		GuiUtils.updateButtonText( settingsMenu, "menu.settings" );
		{
			final JMenuItem lafMenu = new JMenu();
			GuiUtils.updateButtonText( lafMenu, "menu.settings.laf" );
			lafMenu.setIcon( Icons.UI_FLOW );
			{
				final ButtonGroup buttonGroup = new ButtonGroup();
				for ( final LookAndFeelInfo lookAndFeelInfo : GuiUtils.getSortedInstalledLAFInfos() ) {
					final JRadioButtonMenuItem lafMenuItem = new JRadioButtonMenuItem( lookAndFeelInfo.getName(), lookAndFeelInfo.getName().equals( UIManager.getLookAndFeel().getName() ) );
					buttonGroup.add( lafMenuItem );
					lafMenuItem.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed( final ActionEvent event ) {
							if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) && !Settings.getString( Settings.KEY_SETTINGS_LAF ).equals( lookAndFeelInfo.getName() ) )
								Sounds.playSoundSample( Sounds.SAMPLE_CHANGING_THEME, false );
							
							if ( GuiUtils.setLAF( lookAndFeelInfo.getName() ) ) {
								// TODO also call OnTopDialogs
								for ( final JFrame frame : new JFrame[] { MainFrame.this, MousePrintRecorder.getRecorderFrame(), PrivateVideoStreaming.getStreamerFrame() } ) {
									if ( frame == null )
										continue;
									SwingUtilities.updateComponentTreeUI( frame );
									for ( final Window childWindow : frame.getOwnedWindows() )
										SwingUtilities.updateComponentTreeUI( childWindow );
								}
								if ( trayIconPopupMenu != null )
									SwingUtilities.updateComponentTreeUI( trayIconPopupMenu );
								// We have to reset the tree cell renderer once LAF is changed
								navigationTree.setCellRenderer( createTreeCellRenderer() );
								Settings.set( Settings.KEY_SETTINGS_LAF, lookAndFeelInfo.getName() );
							}
						}
					} );
					lafMenu.add( lafMenuItem );
				}
			}
			settingsMenu.add( lafMenu );
			
			final JMenu languageMenu = new JMenu();
			GuiUtils.updateButtonText( languageMenu, "menu.settings.language" );
			languageMenu.setIcon( Icons.LOCALE );
			{
				ButtonGroup buttonGroup = new ButtonGroup();
				final String currentLanguage = Settings.getString( Settings.KEY_SETTINGS_LANGUAGE );
				for ( final String language : Language.getAvailableLanguages() ) {
					final boolean isCurrentLanguage = language.equals( currentLanguage );
					final JRadioButtonMenuItem languageMenuItem = new JRadioButtonMenuItem( isCurrentLanguage ? '*' + language + '*' : language, Icons.getLanguageIcon( language ), isCurrentLanguage );
					buttonGroup.add( languageMenuItem );
					languageMenuItem.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed( final ActionEvent event ) {
							if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) && !Settings.getString( Settings.KEY_SETTINGS_LANGUAGE ).equals( language ) )
								Sounds.playSoundSample( Sounds.SAMPLE_CHANGING_LANGUAGE, false );
							Settings.set( Settings.KEY_SETTINGS_LANGUAGE, language );
						}
					} );
					languageMenu.add( languageMenuItem );
				}
				
				languageMenu.addSeparator();
				
				final JMenu leetTranslationMenu = new JMenu();
				GuiUtils.updateButtonText( leetTranslationMenu, "menu.settings.language.leetTranslation" );
				buttonGroup = new ButtonGroup();
				LeetnessLevel currentLeetnessLevel;
				try {
					currentLeetnessLevel = LeetTranslator.LeetnessLevel.values()[ Settings.getInt( Settings.KEY_SETTINGS_LANGUAGE_LEETNESS_LEVEL ) ];
				} catch ( Exception e ) {
					currentLeetnessLevel = LeetTranslator.LeetnessLevel.values()[ Settings.getDefaultInt( Settings.KEY_SETTINGS_LANGUAGE_LEETNESS_LEVEL ) ];
				}
				final Font monoSpacedFont = new Font( Font.MONOSPACED, leetTranslationMenu.getFont().getStyle(), leetTranslationMenu.getFont().getSize() );
				// Align leetness levels
				int maxNameLength = 0;
				for ( final LeetnessLevel leetnessLevel : LeetTranslator.LeetnessLevel.values() )
					maxNameLength = Math.max( maxNameLength, leetnessLevel.toString().length() );
				maxNameLength += 2; // +2 for the leading and padding character
				for ( final LeetnessLevel leetnessLevel : LeetTranslator.LeetnessLevel.values() ) {
					final boolean isCurrentLeetnessLevel = leetnessLevel == currentLeetnessLevel;
					final String leetnessLevelName = String.format( "%-" + maxNameLength + "s", isCurrentLeetnessLevel ? '*' + leetnessLevel.toString() + '*' : ' ' + leetnessLevel.toString() + ' ' );
					final JRadioButtonMenuItem leetnessLevelMenuItem = new JRadioButtonMenuItem( leetnessLevelName + " - " + leetnessLevel.getSampleText(), isCurrentLeetnessLevel );
					leetnessLevelMenuItem.setFont( monoSpacedFont );
					buttonGroup.add( leetnessLevelMenuItem );
					leetnessLevelMenuItem.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed( final ActionEvent event ) {
							if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) && Settings.getInt( Settings.KEY_SETTINGS_LANGUAGE_LEETNESS_LEVEL ) != leetnessLevel.ordinal() )
								Sounds.playSoundSample( Sounds.SAMPLE_CHANGING_LANGUAGE, false );
							Settings.set( Settings.KEY_SETTINGS_LANGUAGE_LEETNESS_LEVEL, leetnessLevel.ordinal() );
						}
					} );
					leetTranslationMenu.add( leetnessLevelMenuItem );
				}
				languageMenu.add( leetTranslationMenu );
			}
			settingsMenu.add( languageMenu );
			
			final JMenuItem voiceMenu = new JMenu();
			GuiUtils.updateButtonText( voiceMenu, "menu.settings.voice" );
			voiceMenu.setIcon( Icons.MICROPHONE );
			{
				final String currentVoiceName = Settings.getString( Settings.KEY_SETTINGS_VOICE );
				final ButtonGroup buttonGroup = new ButtonGroup();
				for ( final VoiceDescription voiceDesc : Sounds.VOICE_DESCRIPTIONS ) {
					final JRadioButtonMenuItem voiceMenuItem = new JRadioButtonMenuItem( voiceDesc.displayName, Icons.getLanguageIcon( voiceDesc.language ), voiceDesc.name.equals( currentVoiceName ) );
					buttonGroup.add( voiceMenuItem );
					voiceMenuItem.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed( final ActionEvent event ) {
							Settings.set( Settings.KEY_SETTINGS_VOICE, voiceDesc.name );
							if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
								Sounds.playSoundSample( Sounds.SAMPLE_VOICE_NAME, false );
						}
					} );
					voiceMenu.add( voiceMenuItem );
				}
			}
			settingsMenu.add( voiceMenu );
			
			settingsMenu.addSeparator();
			
			final JMenuItem miscSettingsMenuItem = new JMenuItem( Icons.EQUALIZER );
			GuiUtils.updateButtonText( miscSettingsMenuItem, "menu.settings.miscellaneous" );
			miscSettingsMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK ) );
			miscSettingsMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new MiscSettingsDialog( null );
				}
			} );
			settingsMenu.add( miscSettingsMenuItem );
			
			settingsMenu.addSeparator();
			
			enableReplayAutoSaveMenuItem.addActionListener( new ActionListener() {
				{ actionPerformed( null ); } // Handle initial thread start on Sc2gears startup.
				@Override
				public void actionPerformed( final ActionEvent event ) {
					if ( enableReplayAutoSaveMenuItem.isSelected() ) {
						replayAutoSaver = new ReplayAutoSaver();
						replayAutoSaver.start();
					}
					else {
						if ( replayAutoSaver != null )
							replayAutoSaver.requestToCancel();
					}
					if ( trayIcon != null )
						enableReplayAutoSaveTrayMenuItem.setSelected( enableReplayAutoSaveMenuItem.isSelected() );
					if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) && event != null  )
						Sounds.playSoundSample( enableReplayAutoSaveMenuItem.isSelected() ? Sounds.SAMPLE_AUTO_SAVE_ON : Sounds.SAMPLE_AUTO_SAVE_OFF, false );
				}
			} );
			GlobalHotkeys.addHotkey( new Hotkey( Hotkey.MOD_CTRL | Hotkey.MOD_ALT | Hotkey.MOD_NOREP, KeyEvent.VK_R ) {
				@Override
				public void run() {
					enableReplayAutoSaveTrayMenuItem.doClick( 0 );
				}
			} );
			settingsMenu.add( enableReplayAutoSaveMenuItem );
			
			if ( Sc2RegMonitor.supported ) {
				enableApmAlertMenuItem.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						if ( trayIcon != null )
							enableApmAlertTrayMenuItem.setSelected( enableApmAlertMenuItem.isSelected() );
						if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) && event != null )
							Sounds.playSoundSample( enableApmAlertMenuItem.isSelected() ? Sounds.SAMPLE_APM_ALERT_ON : Sounds.SAMPLE_APM_ALERT_OFF, false );
					}
				} );
				GlobalHotkeys.addHotkey( new Hotkey( Hotkey.MOD_CTRL | Hotkey.MOD_ALT | Hotkey.MOD_NOREP, KeyEvent.VK_A ) {
					@Override
					public void run() {
						enableApmAlertMenuItem.doClick( 0 );
					}
				} );
			}
			else
				enableApmAlertMenuItem.setEnabled( false );
			settingsMenu.add( enableApmAlertMenuItem );
			
			final JCheckBoxMenuItem enableVoiceNotificationsMenuItem = GuiUtils.createCheckBoxMenuItem( "menu.settings.enableVoiceNotifications", Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS, Icons.SPEAKER_VOLUME );
			settingsMenu.add( enableVoiceNotificationsMenuItem );
			
			final JCheckBoxMenuItem enableGlobalHotkeysMenuItem = GuiUtils.createCheckBoxMenuItem( "menu.settings.enableGlobalHotkeys", Settings.KEY_SETTINGS_ENABLE_GLOBAL_HOTKEYS, Icons.KEYBOARD );
			if ( GlobalHotkeys.supported ) {
				// Do not activate GlobalHotkeys here, hotkeys may be added after this. It will be started after the GUI is built.
				enableGlobalHotkeysMenuItem.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						if ( enableGlobalHotkeysMenuItem.isSelected() )
							GlobalHotkeys.activate();
						else
							GlobalHotkeys.deactivate();
					}
				} );
			}
			else
				enableGlobalHotkeysMenuItem.setEnabled( false );
			settingsMenu.add( enableGlobalHotkeysMenuItem );
			
			final JCheckBoxMenuItem showStartPageOnStartupMenuItem = GuiUtils.createCheckBoxMenuItem( "menu.settings.showStartPageOnStartup", Settings.KEY_SETTINGS_SHOW_START_PAGE_ON_STARTUP, Icons.NEWSPAPER );
			settingsMenu.add( showStartPageOnStartupMenuItem );
			
			final JCheckBoxMenuItem checkUpdatesOnStartupMenuItem = GuiUtils.createCheckBoxMenuItem( "menu.settings.checkUpdatesOnStartup", Settings.KEY_SETTINGS_CHECK_UPDATES_ON_STARTUP, Icons.ARROW_CIRCLE_DOUBLE );
			settingsMenu.add( checkUpdatesOnStartupMenuItem );
			
			settingsMenu.addSeparator();
			
			final JMenuItem saveSettingsNowMenuItem = new JMenuItem( Icons.DISK );
			GuiUtils.updateButtonText( saveSettingsNowMenuItem, "menu.settings.saveNow" );
			saveSettingsNowMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					saveSettingsNow();
				}
			} );
			settingsMenu.add( saveSettingsNowMenuItem );
			
			final JCheckBoxMenuItem saveSettingsOnExitMenuItem = GuiUtils.createCheckBoxMenuItem( "menu.settings.saveOnExit", Settings.KEY_SETTINGS_SAVE_ON_EXIT, Icons.DISK_ARROW );
			settingsMenu.add( saveSettingsOnExitMenuItem );
		}
		menuBar.add( settingsMenu );
		
		final JMenu toolsMenu = new JMenu();
		toolsMenu.setIcon( Icons.TOOLBOX );
		GuiUtils.updateButtonText( toolsMenu, "menu.tools" );
		{
			final JMenuItem mousePrintRecorderMenuItem = new JMenuItem( Icons.FINGERPRINT );
			GuiUtils.updateButtonText( mousePrintRecorderMenuItem, "menu.tools.mousePrintRecorder" );
			mousePrintRecorderMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F1, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK ) );
			mousePrintRecorderMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					MousePrintRecorder.showFrame();
				}
			} );
			toolsMenu.add( mousePrintRecorderMenuItem );
			
			final JMenuItem privateVideoStreamingMenuItem = new JMenuItem( Icons.MONITOR_CAST );
			GuiUtils.updateButtonText( privateVideoStreamingMenuItem, "menu.tools.privateVideoStreaming" );
			privateVideoStreamingMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F2, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK ) );
			privateVideoStreamingMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					PrivateVideoStreaming.showFrame();
				}
			} );
			toolsMenu.add( privateVideoStreamingMenuItem );
			
			final JMenuItem gameTimeRealTimeConverterMenuItem = new JMenuItem( Icons.CALCULATOR );
			GuiUtils.updateButtonText( gameTimeRealTimeConverterMenuItem, "menu.tools.gameTimeRealTimeConverter" );
			gameTimeRealTimeConverterMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F3, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK ) );
			gameTimeRealTimeConverterMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new GameTimeRealTimeConverterDialog();
				}
			} );
			toolsMenu.add( gameTimeRealTimeConverterMenuItem );
			
			toolsMenu.addSeparator();
			
			final JMenuItem diagnosticToolMenuItem = new JMenuItem( Icons.SYSTEM_MONITOR );
			GuiUtils.updateButtonText( diagnosticToolMenuItem, "menu.tools.diagnosticTool" );
			diagnosticToolMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F4, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK ) );
			diagnosticToolMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new DiagnosticToolDialog();
				}
			} );
			toolsMenu.add( diagnosticToolMenuItem );
			
			final JMenuItem pluginManagerMenuItem = new JMenuItem( Icons.PUZZLE );
			GuiUtils.updateButtonText( pluginManagerMenuItem, "menu.tools.pluginManager" );
			pluginManagerMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F5, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK ) );
			pluginManagerMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new PluginManagerDialog();
				}
			} );
			toolsMenu.add( pluginManagerMenuItem );
			
			final JMenuItem translationToolMenuItem = new JMenuItem( Icons.LOCALE );
			GuiUtils.updateButtonText( translationToolMenuItem, "menu.tools.translationTool" );
			translationToolMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F6, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK ) );
			translationToolMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new TranslationToolDialog();
				}
			} );
			toolsMenu.add( translationToolMenuItem );
			
			toolsMenu.addSeparator();
			
			final JMenuItem onTopLastGameInfoMenuItem = new JMenuItem( Icons.INFORMATION_BALLOON );
			GuiUtils.updateButtonText( onTopLastGameInfoMenuItem, "menu.tools.onTopLastGameInfo" );
			onTopLastGameInfoMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F7, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK ) );
			onTopLastGameInfoMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					OnTopGameInfoDialog.open( GeneralUtils.getLastReplayFile() );
				}
			} );
			toolsMenu.add( onTopLastGameInfoMenuItem );
			
			final JMenuItem onTopApmDisplayMenuItem = new JMenuItem( Icons.COUNTER );
			GuiUtils.updateButtonText( onTopApmDisplayMenuItem, "menu.tools.onTopApmDisplay" );
			onTopApmDisplayMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F8, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK ) );
			if ( Sc2RegMonitor.supported )
				onTopApmDisplayMenuItem.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						OnTopApmDisplayDialog.open();
					}
				} );
			else
				onTopApmDisplayMenuItem.setEnabled( false );
			toolsMenu.add( onTopApmDisplayMenuItem );
			
			toolsMenu.addSeparator();
			
			final JMenuItem mousePracticeGameMenuItem = new JMenuItem( Icons.MOUSE );
			GuiUtils.updateButtonText( mousePracticeGameMenuItem, "menu.tools.mousePracticeGame" );
			mousePracticeGameMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F9, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK ) );
			mousePracticeGameMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					MousePracticeGameFrame.open();
				}
			} );
			toolsMenu.add( mousePracticeGameMenuItem );
			
			toolsMenu.addSeparator();
			
			final JMenuItem sc2gearsDbDownloaderMenuItem = new JMenuItem( Icons.SERVER_NETWORK );
			GuiUtils.updateButtonText( sc2gearsDbDownloaderMenuItem, "menu.tools.sc2earsDatabaseDownloader" );
			sc2gearsDbDownloaderMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F10, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK ) );
			sc2gearsDbDownloaderMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new Sc2gearsDatabaseDownloaderDialog();
				}
			} );
			toolsMenu.add( sc2gearsDbDownloaderMenuItem );
		}
		menuBar.add( toolsMenu );
		
		pluginsMenu.setIcon( Icons.PUZZLE );
		GuiUtils.updateButtonText( pluginsMenu, "menu.plugins" );
		{
			final JPanel infoPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 10, 1 ) );
			infoPanel.add( GuiUtils.changeFontToBold( new JLabel( Language.getText( "menu.plugins.info" ) ) ) );
			pluginsMenu.add( infoPanel );
		}
		menuBar.add( pluginsMenu );
		
		final JMenu windowMenu = new JMenu();
		windowMenu.setIcon( Icons.APPLICATION_BLUE );
		GuiUtils.updateButtonText( windowMenu, "menu.window" );
		{
			final JCheckBoxMenuItem fullscreenMenuItem = new JCheckBoxMenuItem( Icons.APPLICATION_RESIZE_FULL );
			GuiUtils.updateButtonText( fullscreenMenuItem, "menu.window.fullscreen" );
			fullscreenMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F11, 0 ) );
			fullscreenMenuItem.addActionListener( new ActionListener() {
				private Rectangle storedBounds;
				@Override
				public void actionPerformed( final ActionEvent event ) {
					if ( Toolkit.getDefaultToolkit().isFrameStateSupported( MAXIMIZED_BOTH ) ) {
						if ( fullscreenMenuItem.isSelected() )
							storedBounds = getBounds();
						// We have to dispose first, setUndecorated() cannot be called while the frame is displayable
						dispose();
						setUndecorated( fullscreenMenuItem.isSelected() );
						setExtendedState( fullscreenMenuItem.isSelected() ? MAXIMIZED_BOTH : NORMAL );
						if ( !fullscreenMenuItem.isSelected() )
							setBounds( storedBounds );
						setVisible( true );
						fullscreenMenuItem.setIcon( fullscreenMenuItem.isSelected() ? Icons.APPLICATION_RESIZE_ACTUAL : Icons.APPLICATION_RESIZE_FULL );
					}
				}
			} );
			windowMenu.add( fullscreenMenuItem );
			
			windowMenu.addSeparator();
			
			final JMenuItem startMaximizedMenuItem = GuiUtils.createCheckBoxMenuItem( "menu.window.startMaximized", Settings.KEY_WINDOW_START_MAXIMIZED, Icons.APPLICATION_RESIZE );
			windowMenu.add( startMaximizedMenuItem );
			
			final JMenuItem restoreLastPositionOnStartupMenuItem = GuiUtils.createCheckBoxMenuItem( "menu.window.restoreLastWindowPositionOnStartup", Settings.KEY_WINDOW_RESTORE_LAST_POSITION_ON_STARTUP, Icons.APPLICATION_SMALL_BLUE );
			windowMenu.add( restoreLastPositionOnStartupMenuItem );
			
			windowMenu.add( startMinimizedToTrayMenuItem );
			
			windowMenu.addSeparator();
			
			GuiUtils.updateButtonText( minimizeToTrayMenuItem, "menu.window.mimimizeToTray" );
			minimizeToTrayMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F9, 0 ) );
			minimizeToTrayMenuItem.setEnabled( trayIcon != null );
			minimizeToTrayMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					if ( trayIcon != null )
						hideMainFrame();
				}
			} );
			windowMenu.add( minimizeToTrayMenuItem );
			
			final JMenuItem minimizeToTrayOnMinimizeMenuItem = GuiUtils.createCheckBoxMenuItem( "menu.window.minimizeToTrayOnMinimize", Settings.KEY_WINDOW_MINIMIZE_TO_TRAY_ON_MINIMIZE, Icons.APPLICATION_DOCK_TAB );
			windowMenu.add( minimizeToTrayOnMinimizeMenuItem );
			
			final JMenuItem minimizeToTrayOnCloseMenuItem = GuiUtils.createCheckBoxMenuItem( "menu.window.minimizeToTrayOnClose", Settings.KEY_WINDOW_MINIMIZE_TO_TRAY_ON_CLOSE, Icons.APPLICATION_DOCK_TAB );
			windowMenu.add( minimizeToTrayOnCloseMenuItem );
			
			windowMenu.addSeparator();
			
			final JMenuItem tileAllWindowsMenuItem        = new JMenuItem( Icons.APPLICATION_SPLIT_TILE );
			final JMenuItem cascadeAllWindowsMenuItem     = new JMenuItem( Icons.APPLICATIONS_STACK );
			final JMenuItem tileVisibleWindowsMenuItem    = new JMenuItem( Icons.APPLICATION_SPLIT_TILE );
			final JMenuItem cascadeVisibleWindowsMenuItem = new JMenuItem( Icons.APPLICATIONS_STACK );
			
			final int WINDOW_SHIFT_SIZE = 25;
			final ActionListener windowArrangeActionListener = new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					final boolean arrangeTile = event.getSource() == tileAllWindowsMenuItem || event.getSource() == tileVisibleWindowsMenuItem;
					final boolean arrangeAll  = event.getSource() == tileAllWindowsMenuItem || event.getSource() == cascadeAllWindowsMenuItem;
					
					final JInternalFrame[] allFrames = desktopPane.getAllFrames();
					
					int visibleFramesCount = allFrames.length;
					if ( !arrangeAll )
						for ( final JInternalFrame iframe : allFrames )
							if ( iframe.isIcon() )
								visibleFramesCount--;
					
					int columns = 1, rows = 1;
					if ( arrangeTile ) {
						final boolean useHorizontalTileStrategy = Settings.getBoolean( Settings.KEY_WINDOW_VERTICAL_TILE_STRATEGY );
						while ( columns * rows < visibleFramesCount )
							if ( useHorizontalTileStrategy )
								if ( columns == rows )
									rows++;
								else
									columns++;
							else
								if ( columns == rows )
									columns++;
								else
									rows++;
					}
					
					int width, height;
					if ( arrangeTile ) {
						width  =   desktopPane.getWidth () / columns;
						height = ( desktopPane.getHeight() - ( arrangeAll ? 0 : WINDOW_SHIFT_SIZE ) ) / rows;
					}
					else {
						width  = desktopPane.getWidth () - visibleFramesCount * WINDOW_SHIFT_SIZE;
						height = desktopPane.getHeight() - visibleFramesCount * WINDOW_SHIFT_SIZE;
					}
					
					for ( int i = 0, wId = 0; i < allFrames.length; i++ ) {
						try {
							final JInternalFrame iframe = allFrames[ arrangeTile ? i : allFrames.length - i - 1 ];
							if ( arrangeAll && iframe.isIcon() )
								iframe.setIcon( false );
							if ( iframe.isMaximum() )
								iframe.setMaximum( false );
							if ( arrangeAll || !iframe.isIcon() ) {
								if ( arrangeTile )
									iframe.setBounds( wId % columns * width, wId / columns * height, width, height );
								else
									iframe.setBounds( wId * WINDOW_SHIFT_SIZE, wId * WINDOW_SHIFT_SIZE, width, height );
								wId++;
							}
						} catch ( final PropertyVetoException pve ) {
							pve.printStackTrace();
						}
					}
				}
			};
			
			GuiUtils.updateButtonText( tileAllWindowsMenuItem, "menu.window.tileAllWindows" );
			tileAllWindowsMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F2, 0 ) );
			tileAllWindowsMenuItem.addActionListener( windowArrangeActionListener );
			windowMenu.add( tileAllWindowsMenuItem );
			
			GuiUtils.updateButtonText( cascadeAllWindowsMenuItem, "menu.window.cascadeAllWindows" );
			cascadeAllWindowsMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F3, 0 ) );
			cascadeAllWindowsMenuItem.addActionListener( windowArrangeActionListener );
			windowMenu.add( cascadeAllWindowsMenuItem );
			
			GuiUtils.updateButtonText( tileVisibleWindowsMenuItem, "menu.window.tileVisibleWindows" );
			tileVisibleWindowsMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F2, InputEvent.CTRL_DOWN_MASK ) );
			tileVisibleWindowsMenuItem.addActionListener( windowArrangeActionListener );
			windowMenu.add( tileVisibleWindowsMenuItem );
			
			GuiUtils.updateButtonText( cascadeVisibleWindowsMenuItem, "menu.window.cascadeVisibleWindows" );
			cascadeVisibleWindowsMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F3, InputEvent.CTRL_DOWN_MASK ) );
			cascadeVisibleWindowsMenuItem.addActionListener( windowArrangeActionListener );
			windowMenu.add( cascadeVisibleWindowsMenuItem );
			
			final JMenuItem minimizeAllMenuItem = new JMenuItem( Icons.APPLICATION_DIALOG );
			GuiUtils.updateButtonText( minimizeAllMenuItem, "menu.window.minimizeAll" );
			minimizeAllMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					for ( final JInternalFrame iframe : desktopPane.getAllFrames() )
						try {
							iframe.setIcon( true );
						} catch ( final PropertyVetoException pve ) {
							pve.printStackTrace();
						}
				}
			} );
			windowMenu.add( minimizeAllMenuItem );
			
			final JMenuItem closeAllMenuItem = new JMenuItem( Icons.CROSS_BUTTON );
			GuiUtils.updateButtonText( closeAllMenuItem, "menu.window.closeAll" );
			closeAllMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					for ( final JInternalFrame iframe : desktopPane.getAllFrames() )
						try {
							iframe.setClosed( true );
						} catch ( final PropertyVetoException pve ) {
							pve.printStackTrace();
						}
				}
			} );
			windowMenu.add( closeAllMenuItem );
			
			windowMenu.addSeparator();
			
			final JCheckBoxMenuItem useVerticalTileStrategyMenuItem = GuiUtils.createCheckBoxMenuItem( "menu.window.useVerticalTileStrategy", Settings.KEY_WINDOW_VERTICAL_TILE_STRATEGY, Icons.APPLICATION_TILE_VERTICAL );
			windowMenu.add( useVerticalTileStrategyMenuItem );
		}
		menuBar.add( windowMenu );
		
		final JMenu helpMenu = new JMenu();
		helpMenu.setIcon( Icons.QUESTION );
		GuiUtils.updateButtonText( helpMenu, "menu.help" );
		{
			final JMenuItem visitHomePageMenuItem = new JMenuItem( Icons.HOME_ARROW );
			GuiUtils.updateButtonText( visitHomePageMenuItem, "menu.help.visitHomePage" );
			visitHomePageMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F1, KeyEvent.CTRL_DOWN_MASK ) );
			visitHomePageMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.showURLInBrowser( Consts.URL_HOME_PAGE );
				}
			} );
			helpMenu.add( visitHomePageMenuItem );
			
			final JMenuItem visitScelightMenuItem = new JMenuItem( "Visit Scelight™, the successor to " + Consts.APPLICATION_NAME, Icons.SCELIGHT );
			visitScelightMenuItem.setMnemonic( 'e' );
			visitScelightMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.showURLInBrowser( Consts.URL_SCELIGHT_HOME_PAGE );
				}
			} );
			helpMenu.add( visitScelightMenuItem );
			
			final JMenuItem faqMenuItem = new JMenuItem( Icons.APPLICATION_BROWSER );
			GuiUtils.updateButtonText( faqMenuItem, "menu.help.viewFaq" );
			faqMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.showURLInBrowser( Consts.URL_FAQ );
				}
			} );
			helpMenu.add( faqMenuItem );
			
			final JMenuItem gloassaryMenuItem = new JMenuItem( Icons.APPLICATION_BROWSER );
			GuiUtils.updateButtonText( gloassaryMenuItem, "menu.help.viewGlossary" );
			gloassaryMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.showURLInBrowser( Consts.URL_GLOSSARY );
				}
			} );
			helpMenu.add( gloassaryMenuItem );
			
			final JMenuItem visitForumMenuItem = new JMenuItem( Icons.APPLICATION_BROWSER );
			GuiUtils.updateButtonText( visitForumMenuItem, "menu.help.visitForum" );
			visitForumMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.showURLInBrowser( Consts.URL_FORUM );
				}
			} );
			helpMenu.add( visitForumMenuItem );
			
			final JMenuItem viewVersionHistoryMenuItem = new JMenuItem( Icons.APPLICATION_BROWSER );
			GuiUtils.updateButtonText( viewVersionHistoryMenuItem, "menu.help.viewVersionHistory" );
			viewVersionHistoryMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.showURLInBrowser( Consts.URL_VERSION_HISTORY );
				}
			} );
			helpMenu.add( viewVersionHistoryMenuItem );
			
			final JMenuItem sc2gearsDatabaseMenuItem = new JMenuItem( Icons.APPLICATION_BROWSER );
			GuiUtils.updateButtonText( sc2gearsDatabaseMenuItem, "menu.help.sc2gearsDatabase" );
			sc2gearsDatabaseMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.showURLInBrowser( Consts.URL_SC2GEARS_DATABASE );
				}
			} );
			helpMenu.add( sc2gearsDatabaseMenuItem );
			
			final JMenuItem sc2gearsDatabaseUserPageMenuItem = new JMenuItem( Icons.APPLICATION_BROWSER );
			GuiUtils.updateButtonText( sc2gearsDatabaseUserPageMenuItem, "menu.help.sc2gearsDatabaseUserPage" );
			sc2gearsDatabaseUserPageMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.showURLInBrowser( Consts.URL_SC2GEARS_DATABASE_USER_PAGE );
				}
			} );
			helpMenu.add( sc2gearsDatabaseUserPageMenuItem );
			
			final JMenuItem donateMenuItem = new JMenuItem( Icons.APPLICATION_BROWSER );
			GuiUtils.updateButtonText( donateMenuItem, "menu.help.donate" );
			donateMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.showURLInBrowser( Consts.URL_DONATE );
				}
			} );
			helpMenu.add( donateMenuItem );
			
			helpMenu.addSeparator();
			
			GuiUtils.updateButtonText( viewSystemMessagesMenuItem, "menu.help.viewSystemMessages" );
			viewSystemMessagesMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new SystemMessagesDialog();
				}
			} );
			helpMenu.add( viewSystemMessagesMenuItem );
			
			helpMenu.addSeparator();
			
			GuiUtils.updateButtonText( checkUpdatesMenuItem, "menu.help.checkUpdates" );
			checkUpdatesMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new UpdateChecker( checkUpdatesMenuItem );
				}
			} );
			helpMenu.add( checkUpdatesMenuItem );
			
			helpMenu.addSeparator();
			
			final JMenuItem showStartPageMenuItem = new JMenuItem( Icons.NEWSPAPER );
			GuiUtils.updateButtonText( showStartPageMenuItem, "menu.help.showStartPage" );
			showStartPageMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F1, 0 ) );
			showStartPageMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					showStartPage( true );
				}
			} );
			helpMenu.add( showStartPageMenuItem );
			
			final JMenuItem showNewToSc2gearsDialogMenuItem = new JMenuItem( Icons.LIGHT_BULB );
			GuiUtils.updateButtonText( showNewToSc2gearsDialogMenuItem, "menu.help.showNewToSc2gearsDialog", Consts.APPLICATION_NAME );
			showNewToSc2gearsDialogMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new NewToSc2gearsDialog();
				}
			} );
			helpMenu.add( showNewToSc2gearsDialogMenuItem );
			
			final JMenuItem showTipsDialogMenuItem = new JMenuItem( Icons.LIGHT_BULB );
			GuiUtils.updateButtonText( showTipsDialogMenuItem, "menu.help.showTips" );
			showTipsDialogMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new TipsDialog();
				}
			} );
			helpMenu.add( showTipsDialogMenuItem );
			
			final JMenuItem keyboardShortcutsMenuItem = new JMenuItem( Icons.KEYBOARD );
			GuiUtils.updateButtonText( keyboardShortcutsMenuItem, "menu.help.keyboardShortcuts" );
			keyboardShortcutsMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new KeyboardShortcutsDialog();
				}
			} );
			helpMenu.add( keyboardShortcutsMenuItem );
			
			helpMenu.addSeparator();
			
			final JMenuItem aboutMenuItem = new JMenuItem( Icons.INFORMATION );
			GuiUtils.updateButtonText( aboutMenuItem, "menu.help.about", Consts.APPLICATION_NAME );
			aboutMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					new AboutDialog();
				}
			} );
			helpMenu.add( aboutMenuItem );
		}
		menuBar.add( helpMenu );
		
		setJMenuBar( menuBar );
	}
	
	/**
	 * Creates a new internal frame based on its reference tree node.
	 * @param referenceNewTreeNode reference tree node to create new internal frame for
	 * @param arguments optional arguments that will be passed to the new ModuleFrames
	 * @return the new created internal frame
	 */
	private ModuleFrame createNewInternalFrame( final MutableTreeNode referenceNewTreeNode, final Object... arguments ) {
		ModuleFrame moduleFrame = null;
		if ( referenceNewTreeNode == newRepAnalNode )
			moduleFrame = new ReplayAnalyzer( arguments );
		else if ( referenceNewTreeNode == newMultiRepAnalNode )
			moduleFrame = new MultiRepAnalysis( arguments );
		else if ( referenceNewTreeNode == newRepSearchNode )
			moduleFrame = new ReplaySearch( arguments );
		else if ( referenceNewTreeNode == pluginsInfoNode )
			moduleFrame = new ModuleFrame( (String) arguments[ 0 ] );
		
		if ( moduleFrame != null && !moduleFrame.isClosed() ) {
			final DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode( moduleFrame );
			( (DefaultTreeModel) navigationTree.getModel() ).insertNodeInto( newTreeNode, (DefaultMutableTreeNode) referenceNewTreeNode.getParent(), 1 );
			final TreePath newPath = new TreePath( newTreeNode.getPath() );
			navigationTree.setSelectionPath( newPath );
			moduleFrame.addInternalFrameListener( new InternalFrameAdapter() {
				@Override
				public void internalFrameClosing( final InternalFrameEvent event ) {
					( (DefaultTreeModel) navigationTree.getModel() ).removeNodeFromParent( newTreeNode );
				}
				@Override
				public void internalFrameActivated( final InternalFrameEvent event ) {
					if ( !( (JInternalFrame) event.getSource() ).isIcon() ) {
						navigationTree.setSelectionPath( newPath );
						navigationTree.scrollPathToVisible( newPath );
					}
				}
			} );
			
			addNewInternalFrame( moduleFrame );
		}
		
		return moduleFrame;
	}
	
	/**
	 * Adds an internal frame to the internal desktop pane. 
	 * @param iFrame internal frame to be added
	 */
	private void addNewInternalFrame( final JInternalFrame iFrame ) {
		iFrame.setDefaultCloseOperation( JInternalFrame.DISPOSE_ON_CLOSE );
		
		desktopPane.add( iFrame );
		
		// We call setMaximum() twice: first so that the internal window will appear maximized (so no visual resize)!
		// The next one is required because on some LAF (native Windows dependant) the call does not work if the internal frame is not visible!
		
		try {
			iFrame.setMaximum( true );
		} catch ( final PropertyVetoException pve ) {
			pve.printStackTrace();
		}
		
		iFrame.setVisible( true );
		
		try {
			iFrame.setMaximum( true );
		} catch ( final PropertyVetoException pve ) {
			pve.printStackTrace();
		}
	}
	
	/**
	 * Restarts the replay auto-saver if it is running.
	 */
	public void restartReplayAutoSaver() {
		if ( replayAutoSaver != null && !replayAutoSaver.isCancelRequested() ) {
			replayAutoSaver.requestToCancel();
			replayAutoSaver = new ReplayAutoSaver();
			replayAutoSaver.start();
		}
	}
	
	/**
	 * Opens the last replay in the Replay analyzer.<br>
	 * If a last replay cannot be obtained, displays an error dialog.
	 */
	public void openLastReplayFile() {
		final List< File > autoRepFolderList = GeneralUtils.getAutoRepFolderList();
		File lastReplayFile = null;
		boolean exists = false;
		for ( final File autoRepFolder : autoRepFolderList )
			if ( autoRepFolder.exists() ) {
				exists = true;
				lastReplayFile = GeneralUtils.getLastReplay( autoRepFolder, lastReplayFile );
			}
		if ( lastReplayFile == null ) {
			GuiUtils.showErrorDialog( new Object[] { Language.getText( exists ? "misc.autorepFolderEmpty" : "misc.autorepFolderNotExists" ), autoRepFolderList.get( 0 ).getAbsolutePath(), " ", MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.FOLDERS ) } );
			return;
		}
		openReplayFile( lastReplayFile );
	}
	
	/**
	 * Opens a replay file in the Replay analyzer.
	 * @param file replay file to be opened
	 */
	public void openReplayFile( final File file ) {
		createNewInternalFrame( newRepAnalNode, file );
	}
	
	/**
	 * Opens a replay specification in the Replay analyzer.
	 * @param replaySpec replay specification to be opened
	 */
	public void openReplaySpecification( final ReplaySpecification replaySpec ) {
		createNewInternalFrame( newRepAnalNode, null, replaySpec );
	}
	
	/**
	 * Updates the recent replays list with the specified one.
	 * @param file replay file to be added to the recent list
	 */
	public void updateRecentReplays( final File file ) {
		// Update recent list
		int recentReplaysCount = Settings.getInt( Settings.KEY_RECENT_REPLAYS_COUNT );
		final List< String > recentReplayList = new ArrayList< String >( recentReplaysCount + 1 );
		final String fileAbsolutePath = file.getAbsolutePath();
		recentReplayList.add( fileAbsolutePath );
		for ( int i = 0; i < recentReplaysCount; i++ ) {
			final String recentFileEntry = Settings.getString( Settings.KEY_RECENT_REPLAY_ENTRIES + i );
			if ( !fileAbsolutePath.equals( recentFileEntry ) ) // This removes duplicates
				recentReplayList.add( recentFileEntry );
		}
		while ( recentReplayList.size() > MAX_RECENT_REPLAYS )
			recentReplayList.remove( recentReplayList.size() - 1 );
		
		// Set the new recent list at the Settings
		if ( recentReplayList.size() != recentReplaysCount ) {
			recentReplaysCount = recentReplayList.size();
			Settings.set( Settings.KEY_RECENT_REPLAYS_COUNT, recentReplaysCount );
		}
		for ( int i = 0; i < recentReplaysCount; i++ )
			Settings.set( Settings.KEY_RECENT_REPLAY_ENTRIES + i, recentReplayList.get( i ) );
		
		rebuildRecentReplaysMenu();
	}
	
	/**
	 * Opens replays in multi-replay analysis.
	 * @param files replay files to be opened
	 */
	public void openReplaysInMultiRepAnalysis( final File[] files ) {
		createNewInternalFrame( newMultiRepAnalNode, null, null, files );
	}
	
	/**
	 * Opens files and folders in a Replay search.
	 * @param files         files and folders to be searched (these files will be added to the search source)
	 * @param performSearch tells whether the search should be performed, or only the activation of the filters tab is required
	 */
	public void openReplaySearch( final File[] files, final boolean performSearch ) {
		createNewInternalFrame( newRepSearchNode, null, null, files, performSearch );
	}
	
	/**
	 * Creates and adds a new plugin internal frame.
	 * @param title title of the internal frame to be created
	 * @return the new created internal frame
	 */
	public JInternalFrame createAndAddPluginInternalFrame( final String title ) {
		return createNewInternalFrame( pluginsInfoNode, title );
	}
	
	/**
	 * Rebuilds the recent replays menu.
	 */
	private void rebuildRecentReplaysMenu() {
		recentReplaysMenu.removeAll();
		
		final int recentReplaysCount = Settings.getInt( Settings.KEY_RECENT_REPLAYS_COUNT );
		
		for ( int i = 0; i < recentReplaysCount; i++ ) {
			final File file = new File( Settings.getString( Settings.KEY_RECENT_REPLAY_ENTRIES + i ) );
			
			final JMenuItem recentReplayMenuItem = new JMenuItem( (i+1) + " " + file.getName(), Icons.CHART );
			if ( i < 9 )
				recentReplayMenuItem.setMnemonic( '1' + i );
			recentReplayMenuItem.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					openReplayFile( file );
				}
			} );
			
			recentReplaysMenu.add( recentReplayMenuItem );
		}
	}
	
	/**
	 * Arranges the content of the main frame.
	 */
	private void arrangeContent() {
		mainSplitPane.setLeftComponent ( null );
		mainSplitPane.setRightComponent( null );
		
		if ( Settings.getInt( Settings.KEY_NAVIGATION_BAR_POSITION ) == 0 ) {
			if ( navigationBar.getWidth() != 0 )
				mainSplitPane.setDividerLocation( navigationBar.getWidth() );
			mainSplitPane.setLeftComponent ( navigationBox );
			mainSplitPane.setRightComponent( desktopPane   );
		}
		else {
			if ( desktopPane.getWidth() != 0 )
				mainSplitPane.setDividerLocation( desktopPane.getWidth() );
			mainSplitPane.setLeftComponent ( desktopPane   );
			mainSplitPane.setRightComponent( navigationBox );
		}
	}
	
	/**
	 * Hides the main frame.<br>
	 * Minimizes the application to tray. 
	 */
	public void hideMainFrame() {
		setVisible( false );
		
		hideTrayMenuItem   .setEnabled( false );
		restoreTrayMenuItem.setEnabled( true  );
	}
	
	/**
	 * Restores the state of the main frame.<br>
	 * Called when the window is minimized to tray.
	 */
	public void restoreMainFrame() {
		if ( windowSizeInitialized ) {
			if ( !isVisible() )
				setVisible( true );
			if ( getExtendedState() == ICONIFIED )
				setExtendedState( NORMAL );
			setState( NORMAL ); // Restores minimized window
			toFront();
		}
		else {
			windowSizeInitialized = true;
			if ( Settings.getBoolean( Settings.KEY_WINDOW_RESTORE_LAST_POSITION_ON_STARTUP ) ) {
				// Restore saved window position
				try {
					final String positionString = Settings.getString( Settings.KEY_WINDOW_POSITION );
					final StringTokenizer tokenizer = new java.util.StringTokenizer( positionString, "," );
					final int x      = Integer.parseInt( tokenizer.nextToken() );
					final int y      = Integer.parseInt( tokenizer.nextToken() );
					final int width  = Integer.parseInt( tokenizer.nextToken() );
					final int height = Integer.parseInt( tokenizer.nextToken() );
					setBounds( x, y, width, height );
				} catch ( final Exception e ) {
					GuiUtils.maximizeWindowWithMargin( this, 10, null );
				}
			}
			else
				GuiUtils.maximizeWindowWithMargin( this, 10, null );
			if ( Settings.getBoolean( Settings.KEY_WINDOW_START_MAXIMIZED ) )
				setExtendedState( MAXIMIZED_BOTH );
			setVisible( true );
			toFront();
			
			double dividerLoc = (double) Settings.getInt( Settings.KEY_SETTINGS_MISC_NAV_BAR_INITIAL_WIDTH ) / getWidth();
			if ( dividerLoc > 1.0 )
				dividerLoc = 1.0;
			final double dividerLoc_ = dividerLoc;
			// If not run "later", it's ineffective sometimes...
			SwingUtilities.invokeLater( new Runnable() {
				@Override
				public void run() {
					mainSplitPane.setDividerLocation( Settings.getInt( Settings.KEY_NAVIGATION_BAR_POSITION ) == 0 ? dividerLoc_ : 1.0 - dividerLoc_ );
				}
			} );
		}
		
		if ( trayIcon != null ) {
			hideTrayMenuItem   .setEnabled( true  );
			restoreTrayMenuItem.setEnabled( false );
		}
	}
	
	/** Background jobs count. */
	private static final AtomicInteger backgroundJobsCount = new AtomicInteger();
	
	/**
	 * Registers a background job.
	 */
	public static void registerBackgroundJob() {
		backgroundJobsCount.incrementAndGet();
	}
	
	/**
	 * Removes a background job.
	 */
	public static void removeBackgroundJob() {
		backgroundJobsCount.decrementAndGet();
	}
	
	/**
	 * Adds the specified menu item to the Plugins menu.
	 * @param menuItem menu item to be added
	 */
	public void addMenuItemToPluginsMenu( final JMenuItem menuItem ) {
		pluginsMenu.add( menuItem );
	}
	
	/**
	 * Removes the specified menu item from the Plugins menu.
	 * @param menuItem menu item to be added
	 */
	public void removeMenuItemFromPluginsMenu( final JMenuItem menuItem ) {
		pluginsMenu.remove( menuItem );
	}
	
	/**
	 * Saves settings now.
	 */
	public void saveSettingsNow() {
		if ( Settings.getBoolean( Settings.KEY_WINDOW_RESTORE_LAST_POSITION_ON_STARTUP ) && getExtendedState() != MAXIMIZED_BOTH ) {
			// Save window position
			Settings.set( Settings.KEY_WINDOW_POSITION, getX() + "," + getY() + "," + getWidth() + "," + getHeight() );
		}
		Settings.saveProperties();
		PluginManager.savePluginSettings();
	}
	
	/**
	 * Closes the application.<br>
	 * First checks and ask for confirmation if there are active background jobs.<br>
	 * Saves settings before exit if have to.
	 * @param startUpdater tells if updater has to be started
	 * @param arguments    optional arguments to be passed on to the Updater; they cannot contain white-space characters!
	 */
	public void exit( final boolean startUpdater, final String... arguments ) {
		final int backgroundJobs = backgroundJobsCount.get();
		if ( backgroundJobs > 0 )
			if ( GuiUtils.showConfirmDialog( Language.getText( "general.activeJobsExitConfirmation", backgroundJobs ), true ) != 0 )
				return;
		
		PluginManager.disposePlugins();
		
		// Shut down all threads that optionally use native resources!
		
		if ( replayAutoSaver != null )
			replayAutoSaver.requestToCancel();
		
		if ( sc2RegMonitor != null )
			sc2RegMonitor.requestToCancel();
		
		PrivateVideoStreaming.stopStreaming();
		
		if ( sc2RegMonitor != null )
			sc2RegMonitor.shutdown();
		
		if ( Settings.getBoolean( Settings.KEY_SETTINGS_SAVE_ON_EXIT ) )
			saveSettingsNow();
		
		GlobalHotkeys.deactivate();
		
		// Give some time for native codes to clean up...
		try {
			Thread.sleep( 50l );
		} catch ( final InterruptedException ie ) {
			ie.printStackTrace();
		}
		
		ReplayCache.closeCache();
		
		if ( startUpdater )
			try {
				final StringBuilder updaterStarterBuilder = new StringBuilder( "java" );
				
				if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_ENABLE_PROXY_CONFIG ) && Settings.getBoolean( Settings.KEY_SETTINGS_MISC_USE_PROXY_WHEN_DOWNLOADING_UPDATES ) ) {
					updaterStarterBuilder.append( " -Dhttp.proxyHost=\""  ).append( Settings.getString( Settings.KEY_SETTINGS_MISC_HTTP_PROXY_HOST  ) ).append( '"' );
					updaterStarterBuilder.append( " -Dhttp.proxyPort="    ).append( Settings.getString( Settings.KEY_SETTINGS_MISC_HTTP_PROXY_PORT  ) );
					
					updaterStarterBuilder.append( " -Dhttps.proxyHost=\"" ).append( Settings.getString( Settings.KEY_SETTINGS_MISC_HTTPS_PROXY_HOST ) ).append( '"' );
					updaterStarterBuilder.append( " -Dhttps.proxyHost="   ).append( Settings.getString( Settings.KEY_SETTINGS_MISC_HTTPS_PROXY_PORT ) );
					
					updaterStarterBuilder.append( " -DsocksProxyHost=\""  ).append( Settings.getString( Settings.KEY_SETTINGS_MISC_SOCKS_PROXY_HOST ) ).append( '"' );
					updaterStarterBuilder.append( " -DsocksProxyPort="    ).append( Settings.getString( Settings.KEY_SETTINGS_MISC_SOCKS_PROXY_PORT ) );
				}
				
				updaterStarterBuilder.append( " -cp lib-updater/" ).append( Consts.LIB_UPDATER_NAME )
					.append( " hu/belicza/andras/sc2gearsupdater/Updater" );
				
				for ( final String argument : arguments )
					updaterStarterBuilder.append( ' ' ).append( argument );
				
				Runtime.getRuntime().exec( updaterStarterBuilder.toString() );
			} catch ( final IOException ie ) {
				System.err.println( "Failed to start" + Consts.UPDATER_NAME + "!" );
				ie.printStackTrace();
			}
		
		System.exit( 0 );
	}
	
}
