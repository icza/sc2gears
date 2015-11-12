/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.services.plugins;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2map.MapParser;
import hu.belicza.andras.sc2gears.sc2replay.EapmUtils;
import hu.belicza.andras.sc2gears.sc2replay.PlayerSelectionTracker;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.PlayerId;
import hu.belicza.andras.sc2gears.services.Sc2RegMonitor;
import hu.belicza.andras.sc2gears.services.Downloader;
import hu.belicza.andras.sc2gears.services.GlobalHotkeys;
import hu.belicza.andras.sc2gears.services.MousePrintRecorder;
import hu.belicza.andras.sc2gears.services.ReplayAutoSaver;
import hu.belicza.andras.sc2gears.services.plugins.sc2replay.IReplayImpl;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.sound.Sounds;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.components.PlayerPopupMenu;
import hu.belicza.andras.sc2gears.ui.components.ReplayOperationsPopupMenu;
import hu.belicza.andras.sc2gears.ui.components.TableBox;
import hu.belicza.andras.sc2gears.ui.dialogs.BrowserDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.ColorChooserDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.DiagnosticToolDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.ProgressDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.WordCloudDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog.SettingsTab;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.icons.Icons.XImageIcon;
import hu.belicza.andras.sc2gears.util.Base64;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gears.util.HttpPost;
import hu.belicza.andras.sc2gears.util.ProfileCache;
import hu.belicza.andras.sc2gears.util.ReplayCache;
import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.Plugin;
import hu.belicza.andras.sc2gearspluginapi.api.CallbackApi;
import hu.belicza.andras.sc2gearspluginapi.api.EapmUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.GeneralUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.GuiUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.IconsApi;
import hu.belicza.andras.sc2gearspluginapi.api.InfoApi;
import hu.belicza.andras.sc2gearspluginapi.api.LanguageApi;
import hu.belicza.andras.sc2gearspluginapi.api.MousePrintRecorderApi;
import hu.belicza.andras.sc2gearspluginapi.api.ProfileApi;
import hu.belicza.andras.sc2gearspluginapi.api.ReplayFactoryApi;
import hu.belicza.andras.sc2gearspluginapi.api.ReplayUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.SoundsApi;
import hu.belicza.andras.sc2gearspluginapi.api.StarCraftIIApi;
import hu.belicza.andras.sc2gearspluginapi.api.enums.GameStatus;
import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;
import hu.belicza.andras.sc2gearspluginapi.api.enums.MiscObject;
import hu.belicza.andras.sc2gearspluginapi.api.httpost.IHttpPost;
import hu.belicza.andras.sc2gearspluginapi.api.listener.CustomPortraitListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.DiagnosticTestFactory;
import hu.belicza.andras.sc2gearspluginapi.api.listener.DownloaderCallback;
import hu.belicza.andras.sc2gearspluginapi.api.listener.GameStatusListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.PlayerPopupMenuItemListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ProfileListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayAutosaveListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayOpsPopupMenuItemListener;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayer;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerId;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerSelectionTracker;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IReplay;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.AbilityGroup;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Research;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Upgrade;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.importing.ReplaySpecification;
import hu.belicza.andras.sc2gearspluginapi.api.ui.IDownloader;
import hu.belicza.andras.sc2gearspluginapi.api.ui.IProgressDialog;
import hu.belicza.andras.sc2gearspluginapi.api.ui.ITableBox;
import hu.belicza.andras.sc2gearspluginapi.impl.Hotkey;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;
import hu.belicza.andras.sc2gearspluginapi.impl.util.WordCloudTableInput;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

/**
 * A {@link GeneralServices}, {@link InfoApi}, {@link LanguageApi}, {@link CallbackApi}, {@link StarCraftIIApi},
 * {@link GeneralUtilsApi}, {@link GuiUtilsApi}, {@link SoundsApi}, {@link MousePrintRecorderApi},
 * {@link ReplayFactoryApi}, {@link ReplayUtilsApi}, {@link IconsApi}, {@link ProfileApi}, {@link EapmUtilsApi} provider.
 * 
 * <p>Most method implementation just delegates to the proper existing Sc2gears component
 * (this class acts as a bridge between the public Plugin API and the non-public Sc2gears internal classes).</p>
 * 
 * 
 * TODO -add new search field
 * 		-Leet translator api
 * 
 * @author Andras Belicza
 * 
 * @see Plugin
 */
public class GeneralServicesImpl
			implements GeneralServices, InfoApi, LanguageApi, CallbackApi, StarCraftIIApi, GeneralUtilsApi, GuiUtilsApi,
				SoundsApi, MousePrintRecorderApi, ReplayFactoryApi, ReplayUtilsApi, IconsApi, ProfileApi, EapmUtilsApi {
	
	/** Implementation version. */
	public static final String IMPL_VERSION = "4.1";
	
	/** A single instance. */
	private static final GeneralServices INSTANCE = new GeneralServicesImpl();
	
	/**
	 * Returns the general services instance.
	 * @return the general services instance
	 */
	public static GeneralServices getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Disable instantiation of this class.
	 */
	private GeneralServicesImpl() {
	}
	
	@Override
	public String getImplementationVersion() {
		return IMPL_VERSION;
	}
	
	// ==================================================================================
	// ***********************************  INFO API  ***********************************
	// ==================================================================================
	
	@Override
	public InfoApi getInfoApi() {
		return this;
	}
	
	@Override
	public String getApplicationVersion() {
		return Consts.APPLICATION_VERSION;
	}
	
	@Override
	public Date getApplicationReleaseDate() {
		return Consts.APPLICATION_RELEASE_DATE;
	}
	
	@Override
	public String getApplicationLanguageVersion() {
		return Consts.APPLICATION_LANGUAGE_VERSION;
	}
	
	@Override
	public List< File > getAutoRepFolderList() {
		return GeneralUtils.getAutoRepFolderList();
	}
	
	@Override
	public boolean isReplayAutoSaveEnabled() {
		return Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_REPLAY_AUTO_SAVE );
	}
	
	@Override
	public File getReplayAutoSaveFolder() {
		return new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_REPLAY_AUTO_SAVE ) );
	}
	
	@Override
	public boolean isApmAlertEnabled() {
		return Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_APM_ALERT );
	}
	
	@Override
	public boolean isGlobalHotkeysEnabled() {
		return Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_GLOBAL_HOTKEYS );
	}
	
	@Override
	public boolean isVoiceNotificationsEnabled() {
		return Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS );
	}
	
	@Override
	public boolean isRealTimeConversionEnabled() {
		return Settings.getBoolean( Settings.KEY_SETTINGS_MISC_USE_REAL_TIME_MEASUREMENT );
	}
	
	@Override
	public boolean isSaveMousePrintEnabled() {
		return Settings.getBoolean( Settings.KEY_SETTINGS_MISC_SAVE_MOUSE_PRINTS );
	}
	
	@Override
	public File getMousePrintOutputFolder() {
		return new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_MOUSE_PRINT_OUTPUT ) );
	}
	
	@Override
	public File getSc2MapsFolder() {
		return new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_SC2_MAPS ) );
	}
	
	@Override
	public List< String > getFavoredPlayerList() {
		return GeneralUtils.getFavoredPlayerList();
	}
	
	@Override
	public IPlayerId getAliasGroupPlayerId( final IPlayerId playerId ) {
		return Settings.getAliasGroupPlayerId( (PlayerId) playerId );
	}
	
	@Override
	public String getMapAliasGroupName( final String mapName ) {
		return Settings.getMapAliasGroupName( mapName );
	}
	
	@Override
	public int getProfileInfoValidityTime() {
		return Settings.getInt( Settings.KEY_SETTINGS_MISC_PROFILE_INFO_VALIDITY_TIME );
	}
	
	@Override
	public boolean getAutoRetrieveExtProfileInfo() {
		return Settings.getBoolean( Settings.KEY_REP_MISC_ANALYZER_AUTO_RETRIEVE_EXT_PROFILE_INFO );
	}
	
	@Override
	public boolean isWindows() {
		return GeneralUtils.isWindows();
	}
	
	@Override
	public boolean isMac() {
		return GeneralUtils.isMac();
	}
	
	@Override
	public boolean isUnix() {
		return GeneralUtils.isUnix();
	}
	
	@Override
	public boolean isWindows7() {
		return GeneralUtils.isWindows7();
	}
	
	@Override
	public boolean isWindowsVista() {
		return GeneralUtils.isWindowsVista();
	}
	
	@Override
	public boolean isWindowsXp() {
		return GeneralUtils.isWindowsXp();
	}
	
	// ==================================================================================
	// *********************************  Language API  *********************************
	// ==================================================================================
	
	@Override
	public LanguageApi getLanguageApi() {
		return this;
	}
	
	@Override
	public String getLanguage() {
		return Language.getLanguageName();
	}
	
	@Override
	public String formatDate( final Date date ) {
		return Language.formatDate( date );
	}
	
	@Override
	public String formatDateTime( final Date dateTime ) {
		return Language.formatDateTime( dateTime );
	}
	
	@Override
	public String formatTime( final Date time ) {
		return Language.formatTime( time );
	}
	
	@Override
	public String formatPersonName( final String firstName, final String lastName ) {
		return Language.formatPersonName( firstName, lastName );
	}
	
	@Override
	public Date parseDate( final String dateString ) {
		return Language.parseDate( dateString );
	}
	
	@Override
	public Date parseDateTime( final String dateTimeString, final boolean silent ) {
		return Language.parseDateTime( dateTimeString, silent );
	}
	
	@Override
	public String getText( final String key ) {
		return Language.getText( key );
	}
	
	@Override
	public String getOriginalText( final String key ) {
		return Language.getOriginalText( key );
	}
	
	@Override
	public String getDefaultText( final String key ) {
		return Language.getDefaultText( key );
	}
	
	@Override
	public String getText( final String key, final Object... arguments ) {
		return Language.getText( key, arguments );
	}
	
	@Override
	public Pair< String, Character > getTextAndMnemonic( final String key, final Object... arguments ) {
		return Language.getTextAndMnemonic( key, arguments );
	}
	
	// ==================================================================================
	// *********************************  Callback API  *********************************
	// ==================================================================================
	
	@Override
	public CallbackApi getCallbackApi() {
		return this;
	}
	
    @Override
    public void addReplayAutosaveListener( final ReplayAutosaveListener replayAutosaveListener ) {
    	ReplayAutoSaver.addReplayAutosaveListener( replayAutosaveListener );
    }
    
    @Override
    public void removeReplayAutosaveListener( final ReplayAutosaveListener replayAutosaveListener ) {
    	ReplayAutoSaver.removeReplayAutosaveListener( replayAutosaveListener );
    }
	
	@Override
	public void addGameStatusListener( final GameStatusListener gameStatusListener ) {
		Sc2RegMonitor.addGameStatusListener( gameStatusListener );
	}
	
	@Override
	public void removeGameStatusListener( final GameStatusListener gameStatusListener ) {
		Sc2RegMonitor.removeGameStatusListener( gameStatusListener );
	}
	
	@Override
	public Integer addPlayerPopupMenuItem( final String text, final Icon icon, final PlayerPopupMenuItemListener listener ) {
		return PlayerPopupMenu.addPlayerPopupMenuItem( text, icon, listener );
	}
	
	@Override
	public void removePlayerPopupMenuItem( final Integer handler ) {
		PlayerPopupMenu.removePlayerPopupMenuItem( handler );
	}
	
	@Override
	public Integer addReplayOpsPopupMenuItem( final String text, final Icon icon, final ReplayOpsPopupMenuItemListener listener ) {
		return ReplayOperationsPopupMenu.addReplayOpsPopupMenuItem( text, icon, listener );
	}
	
	@Override
	public void removeReplayOpsPopupMenuItem( final Integer handler ) {
		ReplayOperationsPopupMenu.removeReplayOpsPopupMenuItem( handler );
	}
	
	@Override
	public void addGlobalHotkey( final Hotkey hotkey ) {
		GlobalHotkeys.addHotkey( hotkey );
	}
	
	@Override
	public void removeGlobalHotkey( final Hotkey hotkey ) {
		GlobalHotkeys.removeHotkey( hotkey );
	}
	
	@Override
	public void addDiagnosticTestFactory( final DiagnosticTestFactory diagnosticTestFactory ) {
		DiagnosticToolDialog.addDiagnosticTestFactory( diagnosticTestFactory );
	}
	
	@Override
	public void removeDiagnosticTestFactory( final DiagnosticTestFactory diagnosticTestFactory ) {
		DiagnosticToolDialog.removeDiagnosticTestFactory( diagnosticTestFactory );
	}
	
	@Override
	public void addMenuItemToPluginsMenu( final JMenuItem menuItem ) {
		MainFrame.INSTANCE.addMenuItemToPluginsMenu( menuItem );
	}
	
	@Override
	public void removeMenuItemFromPluginsMenu( final JMenuItem menuItem ) {
		MainFrame.INSTANCE.removeMenuItemFromPluginsMenu( menuItem );
	}
	
	@Override
	public JInternalFrame createAndAddInternalFrame( final String title ) {
		return MainFrame.INSTANCE.createAndAddPluginInternalFrame( title );
	}
	
	// ==================================================================================
	// *******************************  StarCraft II API  *******************************
	// ==================================================================================
	
	@Override
	public StarCraftIIApi getStarCraftIIApi() {
		return this;
	}
	
	@Override
	public void startStarCraftII() {
		GeneralUtils.startStarCraftII();
	}
	
	@Override
	public void startStarCraftIIEditor() {
		GeneralUtils.startStarCraftIIEditor();
	}
	
	@Override
	public void launchReplay( final File replayFile ) {
		GeneralUtils.launchReplay( replayFile );
	}
	
	@Override
	public GameStatus getGameStatus() {
		final Integer gameStatus = Sc2RegMonitor.getGameStatus();
		if ( gameStatus == null )
			return null;
		
		// Translate game status
		switch ( Sc2RegMonitor.getGameStatus() ) {
		case Sc2RegMonitor.GAME_STATUS_UNKNOWN : return GameStatus.UNKNOWN;
		case Sc2RegMonitor.GAME_STATUS_NO_GAME : return GameStatus.NO_GAME;
		case Sc2RegMonitor.GAME_STATUS_STARTED : return GameStatus.STARTED;
		}
		
		return null;
	}
	
	@Override
	public Integer getApm() {
		return Sc2RegMonitor.getApm();
	}
	
	// ==================================================================================
	// ****************************  General Utilities API  *****************************
	// ==================================================================================
	
	@Override
	public GeneralUtilsApi getGeneralUtilsApi() {
		return this;
	}
	
	@Override
	public void restoreMainFrame() {
		MainFrame.INSTANCE.restoreMainFrame();
	}
	
	@Override
	public void hideMainFrame() {
		MainFrame.INSTANCE.hideMainFrame();
	}
	
	@Override
	public File getLastReplayFile() {
		return GeneralUtils.getLastReplayFile();
	}
	
	@Override
	public File getLastReplay( final File startFolder, File lastReplayFile ) {
		return GeneralUtils.getLastReplay( startFolder, lastReplayFile );
	}
	
	@Override
	public String getDefaultReplayFolder() {
		return GeneralUtils.getDefaultReplayFolder();
	}
	
	@Override
	public void openReplayFile( final File file ) {
		MainFrame.INSTANCE.openReplayFile( file );
	}
	
	@Override
	public void openReplaysInMultiRepAnalysis( final File[] files ) {
		MainFrame.INSTANCE.openReplaysInMultiRepAnalysis( files );
	}
	
	@Override
	public void openReplaySearch( final File[] files, final boolean performSearch ) {
		MainFrame.INSTANCE.openReplaySearch( files, performSearch );
	}
	
	@Override
	public void openReplaySpecification( final ReplaySpecification replaySpec ) {
		MainFrame.INSTANCE.openReplaySpecification( replaySpec );
	}
	
	@Override
	public JLabel createLinkLabelToSettings( final hu.belicza.andras.sc2gearspluginapi.api.enums.SettingsTab tabToSelect ) {
		return createLinkLabelToSettings( tabToSelect, null );
	}
	
	@Override
	public JLabel createLinkLabelToSettings( final hu.belicza.andras.sc2gearspluginapi.api.enums.SettingsTab tabToSelect, final Frame owner ) {
		// Translate SettingsTab
		return MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.valueOf( tabToSelect.name() ), owner );
	}
	
	@Override
	public void registerBackgroundJob() {
		MainFrame.registerBackgroundJob();
	}
	
	@Override
	public void removeBackgroundJob() {
		MainFrame.removeBackgroundJob();
	}
	
	@Override
	public void showURLInBrowser( final String url ) {
		GeneralUtils.showURLInBrowser( url );
	}
	
	@Override
	public File generateUniqueName( final File file ) {
		return GeneralUtils.generateUniqueName( file );
	}
	
	@Override
	public boolean copyFile( final File sourceFile, final File targetFolder, final byte[] buffer, final String targetName ) {
		return GeneralUtils.copyFile( sourceFile, targetFolder, buffer, targetName );
	}
	
	@Override
	public boolean downloadUrl( final String url, final File toFile ) {
		return GeneralUtils.downloadUrl( url, toFile );
	}
	
	@Override
	public IDownloader getDownloader( final String url, final File destination, final boolean useProgressBar, final DownloaderCallback callback ) {
		return new Downloader( url, destination, useProgressBar, callback );
	}
	
	@Override
	public String convertToHexString( final byte[] data ) {
		return GeneralUtils.convertToHexString( data );
	}
	
	@Override
	public String convertToHexString( final byte[] data, final int offset, final int length ) {
		return GeneralUtils.convertToHexString( data, offset, length );
	}
	
	@Override
	public String calculateFileMd5( final File file ) {
		return GeneralUtils.calculateFileMd5( file );
	}
	
	@Override
	public String calculateFileSha1( final File file ) {
		return GeneralUtils.calculateFileSha1( file );
	}
	
	@Override
	public String calculateFileSha256( final File file ) {
		return GeneralUtils.calculateFileSha256( file );
	}

	@Override
	public String encodeFileBase64( final File file ) {
		return Base64.encodeFile( file );
	}
	
	@Override
	public IHttpPost createHttpPost( final String urlString, final Map< String, String > paramsMap ) {
		return new HttpPost( urlString, paramsMap );
	}
	
	// ==================================================================================
	// ******************************  GUI Utilities API  *******************************
	// ==================================================================================
	
	@Override
	public GuiUtilsApi getGuiUtilsApi() {
		return this;
	}
	
	@Override
	public Frame getMainFrame() {
		return MainFrame.INSTANCE;
	}
	
	@Override
	public void updateButtonText( final AbstractButton button, final String textKey, final Object... arguments ) {
		GuiUtils.updateButtonText( button, textKey, arguments );
	}
	
	@Override
	public JLabel createLinkLabel( final String text, final String targetUrl) {
		return GeneralUtils.createLinkLabel( text, targetUrl );
	}
	
	@Override
	public JLabel createLinkStyledLabel( final String text ) {
		return GeneralUtils.createLinkStyledLabel( text );
	}
	
	@Override
	public void maximizeWindowWithMargin( final Window window, final int margin, final Dimension maxSize ) {
		GuiUtils.maximizeWindowWithMargin( window, margin, maxSize );
	}
	
	@Override
	public void centerWindow( final Window window ) {
		GeneralUtils.centerWindow( window );
	}
	
	@Override
	public void centerWindowToWindow( final Window window, final Window toWindow ) {
		GuiUtils.centerWindowToWindow( window, toWindow );
	}
	
	@Override
	public void packTable( final JTable table ) {
		GuiUtils.packTable( table );
	}
	
	@Override
	public void packTable( final JTable table, final int[] columns ) {
		GuiUtils.packTable( table, columns );
	}
	
	@Override
	public void addNewTab( final String title, final Icon icon, final boolean closeable, final JTabbedPane tabbedPane, final JComponent tab, final Runnable beforeCloseTask ) {
		GuiUtils.addNewTab( title, icon, closeable, tabbedPane, tab, beforeCloseTask );
	}
	
	@Override
	public void addNewTab( final String title, final Icon icon, final boolean closeable, final JTabbedPane tabbedPane, final JComponent tab, final boolean addTabMnemonic, final Runnable beforeCloseTask ) {
		GuiUtils.addNewTab( title, icon, closeable, tabbedPane, tab, addTabMnemonic, beforeCloseTask );
	}
	
	@Override
	public void appendNewLineWithTimestamp( final JTextArea textArea, final String line ) {
		GuiUtils.appendNewLineWithTimestamp( textArea, line );
	}
	
	@Override
	public void appendNewLine(JTextArea textArea, String line) {
		GuiUtils.appendNewLine( textArea, line );
	}
	
	@Override
	public JLabel createErrorDetailsLink() {
		return GuiUtils.createErrorDetailsLink();
	}
	
	@Override
	public void showErrorDialog( final Object message, final Frame... owner ) {
		GuiUtils.showErrorDialog( message, owner );
	}
	
	@Override
	public void showInfoDialog( final Object message, final Frame... owner ) {
		GuiUtils.showInfoDialog( message, owner );
	}
	
	@Override
	public int showConfirmDialog( final Object message, final boolean isWarning, final Frame... owner ) {
		return GuiUtils.showConfirmDialog( message, isWarning, owner );
	}
	
	@Override
	public JComponent createReplayFilePreviewAccessory( final JFileChooser fileChooser ) {
		return GuiUtils.createReplayFilePreviewAccessory( fileChooser );
	}
	
	@Override
	public void makeComponentDragScrollable( final JComponent component ) {
		GuiUtils.makeComponentDragScrollable( component );
	}
	
	@Override
	public JComponent changeFontToBold( final JComponent component ) {
		return GuiUtils.changeFontToBold( component );
	}
	
	@Override
	public JComponent changeFontToItalic( final JComponent component ) {
		return GuiUtils.changeFontToItalic( component );
	}
	
	@Override
	public JTable createNonEditableTable() {
		return GuiUtils.createNonEditableTable();
	}
	
	@Override
	public JButton createCloseButton( final JDialog dialog ) {
		return GuiUtils.createCloseButton( dialog );
	}
	
	@Override
	public JButton createCloseButton( final JDialog dialog, final String textKey ) {
		return GuiUtils.createCloseButton( dialog, textKey );
	}
	
	@Override
	public JLabel createRaceIconLabel( final Race race ) {
		return GuiUtils.createRaceIconLabel( race );
	}
	
	@Override
	public JLabel createIconLabelButton( final ImageIcon icon, final String toolTipTextKey ) {
		return GuiUtils.createIconLabelButton( icon, toolTipTextKey );
	}
	
	@Override
	public Icon getColorIcon( final Color color ) {
		return GuiUtils.getColorIcon( color );
	}
	
	@Override
	public void alignBox( final Box box, final int columns ) {
		GuiUtils.alignBox( box, columns );
	}
	
	@Override
	public void setComponentTreeEnabled( final Component component, final boolean enabled ) {
		GuiUtils.setComponentTreeEnabled( component, enabled );
	}
	
	@Override
	public ITableBox createTableBox( final JTable table, final JComponent rootComponent ) {
		return createTableBox( table, rootComponent, null );
	}
	
	@Override
	public ITableBox createTableBox( final JTable table, final JComponent rootComponent, final WordCloudTableInput wordCloudTableInput ) {
		return new TableBox( table, rootComponent, wordCloudTableInput );
	}
	
	@Override
	public IProgressDialog createProgressDialog( final String title, final ImageIcon icon, final int total ) {
		return new ProgressDialog( new Holder< String >( title ), icon, total );
	}
	
	@Override
	public IProgressDialog createProgressDialog( final Frame owner, final String title, final ImageIcon icon, final int total ) {
		return new ProgressDialog( owner, new Holder< String >( title ), icon, total );
	}
	
	@Override
	public IProgressDialog createProgressDialog( final Dialog owner, final String title, final ImageIcon icon, final int total ) {
		return new ProgressDialog( owner, new Holder< String >( title ), icon, total );
	}
	
	@Override
	public JEditorPane createJEditorPane() {
		return GuiUtils.createEditorPane();
	}
	
	@Override
	public void showWordCloudDialog( final Dialog parent, final String title, final List< Pair< String, Integer > > wordList ) {
		new WordCloudDialog( parent, title, wordList );
	}
	
	@Override
	public void showWordCloudDialog( final Frame parent, final String title, final List< Pair< String, Integer > > wordList ) {
		new WordCloudDialog( parent, title, wordList );
	}
	
	@Override
	public void showBrowserDialog( final Frame parent, final String title, final ImageIcon icon, final String url, final Dimension preferredSize ) {
		new BrowserDialog( parent, new Holder< String >( title ), icon, url, preferredSize );
	}
	
	@Override
	public void showBrowserDialog( final Dialog parent, final String title, final ImageIcon icon, final String url, final Dimension preferredSize ) {
		new BrowserDialog( parent, new Holder< String >( title ), icon, url, preferredSize );
	}
	
	@Override
	public Color showColorChooserDialog( final Dialog parent, final Color initialColor ) {
		return new ColorChooserDialog( parent, initialColor ).getColor();
	}
	
	@Override
	public Color showColorChooserDialog( final Frame parent, final Color initialColor ) {
		return new ColorChooserDialog( parent, initialColor ).getColor();
	}
	
	// ==================================================================================
	// **********************************  SOUNDS API  **********************************
	// ==================================================================================
	
	@Override
	public SoundsApi getSoundsApi() {
		return this;
	}
	
	@Override
	public boolean playSound( final InputStream dataStream, final boolean waitPlayEnd ) {
		return Sounds.playSound( dataStream, waitPlayEnd, Settings.getInt( Settings.KEY_SETTINGS_MISC_SOUND_VOLUME ) );
	}
	
	@Override
	public boolean playSound( final InputStream dataStream, final boolean waitPlayEnd, final int volume ) {
		return Sounds.playSound( dataStream, waitPlayEnd, volume );
	}
	
	// ==================================================================================
	// ***************************  MOUSE PRINT RECORDER API  ***************************
	// ==================================================================================
	
	@Override
	public MousePrintRecorderApi getMousePrintRecorderApi() {
		return this;
	}
	
	@Override
	public void showFrame() {
		MousePrintRecorder.showFrame();
	}
	
	@Override
	public void startRecording() {
		MousePrintRecorder.startRecording();
	}
	
	@Override
	public void stopRecording() {
		MousePrintRecorder.stopRecording();
	}
	
	@Override
	public boolean isRecording() {
		return MousePrintRecorder.isRecording();
	}
	
	@Override
	public boolean isRecordingPresent() {
		return MousePrintRecorder.isRecordingPresent();
	}
	
	@Override
	public void saveRecording() {
		MousePrintRecorder.saveRecording();
	}
	
	// ==================================================================================
	// ******************************  REPLAY FACTORY API  ******************************
	// ==================================================================================
	
	@Override
	public ReplayFactoryApi getReplayFactoryApi() {
		return this;
	}
	
	@Override
	public String getReplayParserEngineVersion() {
		return ReplayFactory.VERSION;
	}
	
	@Override
	public IReplay parseReplay( final String fileName, final Set< ReplayContent > contentToParseSet ) {
		final Set< hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent > contentToExtractSet = EnumSet.copyOf( ReplayFactory.GENERAL_INFO_CONTENT );
		
		if ( contentToParseSet.contains( ReplayContent.GAME_EVENTS ) )
			contentToExtractSet.add( hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent.GAME_EVENTS );
		if ( contentToParseSet.contains( ReplayContent.MESSAGE_EVENTS ) )
			contentToExtractSet.add( hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent.MESSAGE_EVENTS );
		if ( contentToParseSet.contains( ReplayContent.MAP_INFO ) )
			contentToExtractSet.add( hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent.MAP_INFO );
		if ( contentToParseSet.contains( ReplayContent.MAP_ATTRIBUTES ) )
			contentToExtractSet.add( hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent.MAP_ATTRIBUTES );
		
		final Replay replay = ReplayFactory.parseReplay( fileName, contentToExtractSet );
		
		if ( replay == null )
			return null;
		
		if ( contentToParseSet.contains( ReplayContent.MAP_INFO ) && contentToParseSet.contains( ReplayContent.EXTENDED_MAP_INFO ) )
			MapParser.parseExtendedMapInfo( replay );
		
		return new IReplayImpl( replay );
	}
	
	@Override
	public IReplay constructReplay( final ReplaySpecification replaySpec ) {
		return new IReplayImpl( ReplayFactory.constructReplay( replaySpec ) );
	}
	
	@Override
	public IReplay getReplay( final String fileName, final Set< ReplayContent > contentToParseSet ) {
		final Set< hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent > contentToExtractSet = EnumSet.copyOf( ReplayFactory.GENERAL_INFO_CONTENT );
		
		if ( contentToParseSet == null ) {
			contentToExtractSet.add( hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent.GAME_EVENTS );
			contentToExtractSet.add( hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent.MESSAGE_EVENTS );
		} else {
			if ( contentToParseSet.contains( ReplayContent.GAME_EVENTS ) )
				contentToExtractSet.add( hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent.GAME_EVENTS );
			if ( contentToParseSet.contains( ReplayContent.MESSAGE_EVENTS ) )
				contentToExtractSet.add( hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent.MESSAGE_EVENTS );
			// Map info cannot be parsed 
			if ( contentToParseSet.contains( ReplayContent.MAP_INFO ) )
				contentToExtractSet.add( hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent.MAP_INFO );
			if ( contentToParseSet.contains( ReplayContent.MAP_ATTRIBUTES ) )
				contentToExtractSet.add( hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent.MAP_ATTRIBUTES );
		}
		
		boolean saveToCache = contentToExtractSet.contains( hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent.GAME_EVENTS )
						   && contentToExtractSet.contains( hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent.MESSAGE_EVENTS );
		
		final Replay replay = ReplayCache.getReplay( new File( fileName ), null, null, true, saveToCache, contentToExtractSet );
		
		if ( replay == null )
			return null;
		
		if ( contentToParseSet != null && contentToParseSet.contains( ReplayContent.MAP_INFO ) && contentToParseSet.contains( ReplayContent.EXTENDED_MAP_INFO )
				&& replay.mapInfo != null ) // If replay is from the cache, mapInfo == null, if mapInfo == null, extended map info cannot be parsed!
			MapParser.parseExtendedMapInfo( replay );
		
		return new IReplayImpl( replay );
	}
	
	// ==================================================================================
	// *******************************  REPLAY UTILS API  *******************************
	// ==================================================================================
	
	@Override
	public ReplayUtilsApi getReplayUtilsApi() {
		return this;
	}
	
	@Override
	public Comparator< Unit > getUnitTierReverseComparator() {
		return ReplayUtils.UNIT_TIER_REVERSE_COMPARATOR;
	}
	
	@Override
	public int calculatePlayerApm( final IReplay replay, final IPlayer player ) {
		return ReplayUtils.calculateApm( player.getActionsCount() - player.getExcludedActionsCount(), replay.getConverterGameSpeed().convertToRealTime( player.getLastActionFrame() - replay.getExcludedInitialFrames() ) );
	}
	
	@Override
	public int calculatePlayerEapm( final IReplay replay, final IPlayer player ) {
		return ReplayUtils.calculateApm( player.getEffectiveActionsCount() - player.getExcludedEffectiveActionsCount(), replay.getConverterGameSpeed().convertToRealTime( player.getLastActionFrame() - replay.getExcludedInitialFrames() ) );
	}
	
	@Override
	public int calculateApm( final int actionsCount, final int frames ) {
		return ReplayUtils.calculateApm( actionsCount, frames );
	}
	
	@Override
	public String formatFrames( final int frames, final GameSpeed gameSpeed ) {
		return ReplayUtils.formatFrames( frames, gameSpeed );
	}
	
	@Override
	public String formatMs( final int ms, final GameSpeed gameSpeed ) {
		return ReplayUtils.formatMs( ms, gameSpeed );
	}
	
	@Override
	public String formatFramesShort( final int frames, final GameSpeed gameSpeed ) {
		return ReplayUtils.formatFramesShort( frames, gameSpeed );
	}
	
	@Override
	public String formatFramesDecimal( final int frames, final GameSpeed gameSpeed ) {
		return ReplayUtils.formatFramesDecimal( frames, gameSpeed );
	}
	
	@Override
	public String formatCoordinate( final int coord ) {
		return ReplayUtils.formatCoordinate( coord );
	}
	
	@Override
	public String convertBuildNumbersToString( final int[] buildNumbers ) {
		return ReplayUtils.convertBuildNumbersToString( buildNumbers );
	}
	
	@Override
	public int[] parseVersion( final String text ) {
		return ReplayUtils.parseVersion( text );
	}
	
	@Override
	public int compareVersions( final int[] v1, final int[] v2 ) {
		return ReplayUtils.compareVersions( v1, v2 );
	}
	
	@Override
	public int strictCompareVersions( final int[] v1, final int[] v2 ) {
		return ReplayUtils.strictCompareVersions( v1, v2 );
	}
	
	@Override
	public IPlayerSelectionTracker createPlayerSelectionTracker() {
		return new PlayerSelectionTracker();
	}
	
	// ==================================================================================
	// **********************************  ICONS API  ***********************************
	// ==================================================================================
	
	@Override
	public IconsApi getIconsApi() {
		return this;
	}
	
	@Override
	public Icon getBuildingIcon( final Building building, final IconSize size ) {
		return Icons.getBuildingIcon( building, size );
	}

	@Override
	public Icon getUnitIcon( final Unit unit, final IconSize size ) {
		return Icons.getUnitIcon( unit, size );
	}
	
	@Override
	public Icon getUpgradeIcon( final Upgrade upgrade, final IconSize size ) {
		return Icons.getUpgradeIcon( upgrade, size );
	}
	
	@Override
	public Icon getResearchIcon( final Research research, final IconSize size ) {
		return Icons.getResearchIcon( research, size );
	}
	
	@Override
	public Icon getAbilityGroupIcon( final AbilityGroup abilityGroup, final IconSize size ) {
		return Icons.getAbilityGroupIcon( abilityGroup, size );
	}
	
	@Override
	public Icon getMiscObjectIcon( final MiscObject miscObject, final IconSize size ) {
		return Icons.getMiscObjectIcon( miscObject, size );
	}
	
    @Override
    public Icon getCustomEnlargedIcon( final String name, final IconSize size ) {
		return Icons.getCustomEnlargedIcon( name, size );
    }
	
	@Override
	public Icon getEntityIcon( final Object entity, final IconSize size ) {
		return Icons.getEntityIcon( entity, size );
	}
	
	@Override
	public ImageIcon getRaceIcon( final Race race ) {
		return Icons.getRaceIcon( race );
	}
	
	@Override
	public URL getRaceIconResource( final Race race ) {
		final XImageIcon raceIcon = Icons.getRaceIcon( race );
		return raceIcon == null ? null : raceIcon.resource;
	}
	
	@Override
	public Icon getNullIcon( final int width, final int height ) {
		return Icons.getNullIcon( width, height );
	}
	
	@Override
	public Icon getCustomIcon( final ImageIcon anotherIcon, final int width, final int height, int zoom ) {
		return Icons.getCustomIcon( anotherIcon, width, height, zoom );
	}
	
	@Override
	public Icon getPortraitLoadingIcon() {
		return Icons.PORTRAIT_LOADING_ICON;
	}
	
	@Override
	public Icon getPortraitNAIcon() {
		return Icons.PORTRAIT_NA_ICON;
	}
	
	@Override
	public Icon getPortraitComputerIcon() {
		return Icons.PORTRAIT_COMPUTER_ICON;
	}
	
	@Override
	public Icon getPortraitHighLoadingIcon() {
		return Icons.PORTRAIT_HIGH_LOADING_ICON;
	}
	
	@Override
	public Icon getPortraitHighNAIcon() {
		return Icons.PORTRAIT_HIGH_NA_ICON;
	}
	
	@Override
	public Icon getLeagueLoadingIcon() {
		return Icons.LEAGUE_LOADING_ICON;
	}
	
	@Override
	public Icon getLeagueNAIcon() {
		return Icons.LEAGUE_NA_ICON;
	}
	
	@Override
	public Icon getPortraitIcon( final int group, final int row, final int column ) {
		return Icons.getPortraitIcon( group, row, column );
	}
	
	@Override
	public Icon getPortraitHighIcon( final int group, final int row, final int column ) {
		return Icons.getPortraitHighIcon( group, row, column );
	}
	
	// ==================================================================================
	// *********************************  PROFILE API  **********************************
	// ==================================================================================
	
	@Override
	public ProfileApi getProfileApi() {
		return this;
	}
	
	@Override
	public boolean queryProfile( final IPlayerId playerId, final ProfileListener profileListener, final boolean queryExtendedInfo, final boolean forceRetrieve ) {
		return ProfileCache.queryProfile( playerId, profileListener, queryExtendedInfo, forceRetrieve );
	}
	
	@Override
	public Boolean queryCustomPortrait( final IPlayerId playerId, final boolean highRes, final CustomPortraitListener customPortraitListener ) {
		return ProfileCache.queryCustomPortrait( playerId, highRes, customPortraitListener );
	}
	
	// ==================================================================================
	// ********************************  EAPM UTILS API  ********************************
	// ==================================================================================
	
	@Override
	public EapmUtilsApi getEapmUtilsApi() {
		return this;
	}
	
	@Override
	public String getAlgorithmVersion() {
		return EapmUtils.ALGORITHM_VERSION;
	}
	
	@Override
	public boolean isActionEffective( final IAction[] actions, final int actionIndex ) {
		return EapmUtils.getActionIneffectiveReason( actions, actionIndex ) == null;
	}
	
}
