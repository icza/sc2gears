/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.dialogs;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.Sc2gears;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.services.MousePrintRecorder;
import hu.belicza.andras.sc2gears.services.MousePrintRecorder.DataCompression;
import hu.belicza.andras.sc2gears.services.MousePrintRecorder.WhatToSave;
import hu.belicza.andras.sc2gears.services.ReplayAutoSaver;
import hu.belicza.andras.sc2gears.services.ReplayAutoSaver.NewFileDetectionMethod;
import hu.belicza.andras.sc2gears.services.Sc2RegMonitor;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.sound.Sounds;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.components.BaseLabelListCellRenderer;
import hu.belicza.andras.sc2gears.ui.components.CustomComboBoxModel;
import hu.belicza.andras.sc2gears.ui.components.StatusLabel;
import hu.belicza.andras.sc2gears.ui.dialogs.ProxyConfigDialog.ProxyConfig;
import hu.belicza.andras.sc2gears.ui.dialogs.ShareReplaysDialog.ReplayUploadSite;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ReplayAnalyzer.ShowProfileInfo;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gears.util.NormalThread;
import hu.belicza.andras.sc2gears.util.ProfileCache;
import hu.belicza.andras.sc2gears.util.ReplayCache;
import hu.belicza.andras.sc2gears.util.Task;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BnetLanguage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;


/**
 * Miscellaneous settings dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class MiscSettingsDialog extends BaseDialog {
	
	/**
	 * Miscellaneous Settings dialog tab.
	 * @author Andras Belicza
	 */
	public static enum SettingsTab {
		/** Replay auto-save settings tab.    */
		REPLAY_AUTO_SAVE   ( "miscSettings.tab.replayAutoSave"   , Icons.DATABASE_SAVE     ),
		/** APM Alert settings tab.           */
		APM_ALERT          ( "miscSettings.tab.apmAlert"         , Icons.ALARM_CLOCK       ),
		/** Mouse print settings tab.         */
		MOUSE_PRINT        ( "miscSettings.tab.mousePrint"       , Icons.FINGERPRINT       ),
		/** User interface settings tab.      */
		USER_INTERFACE     ( "miscSettings.tab.userInterface"    , Icons.UI_LAYOUT_PANEL   ),
		/** Replay parser settings tab.       */
		REPLAY_PARSER      ( "miscSettings.tab.replayParser"     , Icons.COMPILE           ),
		/** Replay analyzer settings tab.     */
		REPLAY_ANALYZER    ( "miscSettings.tab.replayAnalyzer"   , Icons.CHART             ),
		/** Multi-rep analysis settings tab.  */
		MULTI_REP_ANALYSIS ( "miscSettings.tab.multiRepAnalysis" , Icons.CHART_UP_COLOR    ),
		/** Internal settings tab.            */
		INTERNAL           ( "miscSettings.tab.internal"         , Icons.WRENCH            ),
		/** Mouse game rules settings tab.    */
		MOUSE_GAME_RULES   ( "miscSettings.tab.mouseGameRules"   , Icons.MOUSE             ),
		/** Aliases settings tab.             */
		ALIASES            ( "miscSettings.tab.aliases"          , Icons.USERS             ),
		/** Pre-defined lists settings tab.   */
		PREDEFINED_LISTS   ( "miscSettings.tab.predefinedLists"  , Icons.UI_COMBO_BOX_BLUE ),
		/** Folders settings tab.             */
		FOLDERS            ( "miscSettings.tab.folders"          , Icons.FOLDERS           ),
		/** Sc2gears Database settings tab.   */
		SC2GEARS_DATABASE  ( "miscSettings.tab.sc2gearsDatabase" , Icons.SERVER_NETWORK    ),
		/** Custom replay sites settings tab. */
		CUSTOM_REPLAY_SITES( "miscSettings.tab.customReplaySites", Icons.DOCUMENT_SHARE    );
		
		/** Text of the tab.              */
		public final String text;
		/** Icon associated with the tab. */
		public final Icon   icon;
		
		/**
		 * Creates a new Tab.
		 * @param tabTextKey text key of the tab
		 * @param icon       icon associated with the tab
		 */
		private SettingsTab( final String tabTextKey, final Icon icon ) {
			this.text = Language.getText( tabTextKey );
			this.icon = icon;
		}
	}
	
	/**
	 * A folder row in the Folders tab in the Miscellaneous settings dialog.
	 * 
	 * @author Andras Belicza
	 */
	private static class FolderRow {
		private final String     settingKeyForDefault; // This is different if multiplicationIndex is provided
		private final Integer    multiplicationIndex;
		private final String     checkBoxSettingKey;
		private final String     settingKey;
		public  final JComponent infoComponent;
		private final JTextField textField;
		private final boolean    isUserHomeRelative;
		
		public FolderRow( final String textKey, final String settingKey, final boolean isUserHomeRelative, final Integer multiplicationIndex, final String checkBoxSettingKey ) {
			settingKeyForDefault     = settingKey;
			this.multiplicationIndex = multiplicationIndex;
			this.checkBoxSettingKey  = checkBoxSettingKey;
			this.settingKey          = multiplicationIndex == null ? settingKey : settingKey + multiplicationIndex;
			if ( multiplicationIndex == null )
				infoComponent        = new JLabel( Language.getText( textKey ) );
			else
				infoComponent        = new JCheckBox( Language.getText( textKey ) + " #" + multiplicationIndex, Settings.getBoolean( checkBoxSettingKey + multiplicationIndex ) );
			textField                = new JTextField( Settings.getString( this.settingKey ), 25 );
			this.isUserHomeRelative  = isUserHomeRelative;
		}
		
		public JComponent getComponent() {
			final Box box = Box.createHorizontalBox();
			box.add( infoComponent );
			box.add( textField );
			final JButton chooseFolderButton = new JButton( Language.getText( "folderSettings.chooseFolder" ), Icons.FOLDER );
			chooseFolderButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					final String startFolder;
					if ( isUserHomeRelative ) {
						final File folder = new File( textField.getText() );
						if ( folder.exists() )
							startFolder = folder.getAbsolutePath();
						else
							startFolder = new File( Consts.FOLDER_USER_HOME, textField.getText() ).getAbsolutePath();
					}
					else
						startFolder = textField.getText();
					final JFileChooser fileChooser = new JFileChooser( startFolder );
					fileChooser.setDialogTitle( Language.getText( "folderSettings.chooseFolder" ) );
					fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
					if ( fileChooser.showOpenDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION ) {
						String selectedFilePath = fileChooser.getSelectedFile().getAbsolutePath();
						if ( isUserHomeRelative && selectedFilePath.toLowerCase().startsWith( Consts.FOLDER_USER_HOME.toLowerCase() ) ) {
							// Make it relative to user home
							selectedFilePath = selectedFilePath.substring( Consts.FOLDER_USER_HOME.length() );
						}
						textField.setText( selectedFilePath );
					}
				}
			} );
			box.add( chooseFolderButton );
			final JButton restoreDefaultFolderButton = new JButton( Language.getText( "folderSettings.restoreDefaultFolder" ), Icons.FOLDER_MINUS );
			restoreDefaultFolderButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					textField.setText( Settings.getDefaultString( settingKeyForDefault ) );
				}
			} );
			box.add( restoreDefaultFolderButton );
			if ( infoComponent instanceof JCheckBox ) {
				final ActionListener enablerActionListener = new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						final boolean enabled = ( (JCheckBox) infoComponent ).isSelected();
						textField                 .setEnabled( enabled );
						chooseFolderButton        .setEnabled( enabled );
						restoreDefaultFolderButton.setEnabled( enabled );
						// Required due to the Napkin LAF
						textField.updateUI();
						SwingUtilities.invokeLater( new Runnable() {
							@Override
							public void run() {
								chooseFolderButton        .updateUI();
								restoreDefaultFolderButton.updateUI();
							}
						} );
					}
				};
				enablerActionListener.actionPerformed( null );
				( (JCheckBox) infoComponent ).addActionListener( enablerActionListener );
			}
			return box;
		}
		
		public void storeSetting() {
			Settings.set( settingKey, textField.getText() );
			if ( infoComponent instanceof JCheckBox )
				Settings.set( checkBoxSettingKey + multiplicationIndex, ( (JCheckBox) infoComponent ).isSelected() );
		}
	}
	
	/**
	 * A setting row in the miscellaneous settings dialog.
	 * 
	 * @author Andras Belicza
	 */
	private static class SettingRow {
		private final String     settingKey;
		public  final JComponent component;
		public  final Box        box = Box.createHorizontalBox();
		
		public SettingRow( final String textKey, final String settingKey, final JComponent component, final JComponent extra ) {
			this.settingKey = settingKey;
			this.component  = component;
			
			// Build GUI
			if ( component instanceof JSpinner && ( (JSpinner) component ).getModel() instanceof SpinnerNumberModel ) {
				final SpinnerNumberModel model = (SpinnerNumberModel) ( (JSpinner) component ).getModel();
				component.setToolTipText( Language.getText( "miscSettings.validRangeAndDefaultToolTip", model.getMinimum(), model.getMaximum(), Settings.getDefaultInt( settingKey ) ) );
			}
			
			if ( component instanceof JComboBox ) {
				final JComboBox< ? > comboBox = (JComboBox< ? >) component;
				if ( comboBox.isEditable() ) {
					// It's a pre-defined list combo box
					comboBox.setSelectedItem( Settings.getString( settingKey ) );
				}
				else {
					// It's a normal combo box
					comboBox.setMaximumRowCount( comboBox.getModel().getSize() ); // Display all values
					try {
						comboBox.setSelectedIndex( Settings.getInt( settingKey ) );
					} catch ( final IllegalArgumentException iae ) {
						comboBox.setSelectedIndex( Settings.getDefaultInt( settingKey ) );
					}
				}
			}
			
			box.add( new JLabel( Language.getText( textKey ) ) );
			box.add( component instanceof JCheckBox ? GuiUtils.wrapInPanelLeftAligned( component ) : component );
			box.add( extra );
			
			final JButton restoreDefaultButton = new JButton();
			GuiUtils.updateButtonText( restoreDefaultButton, "miscSettings.restoreDefault" );
			restoreDefaultButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					if ( component instanceof JSpinner )
						( ( JSpinner) component ).setValue( Settings.getDefaultInt( settingKey ) );
					else if ( component instanceof JSlider )
						( ( JSlider) component ).setValue( Settings.getDefaultInt( settingKey ) );
					else if ( component instanceof JTextField )
						( ( JTextField) component ).setText( Settings.getDefaultString( settingKey ) );
					else if ( component instanceof JCheckBox )
						( ( JCheckBox) component ).setSelected( Settings.getDefaultBoolean( settingKey ) );
					else if ( component instanceof JComboBox ) {
						final JComboBox< ? > comboBox = (JComboBox< ? >) component;
						if ( comboBox.isEditable() ) // It's a pre-defined list combo box
							comboBox.setSelectedItem( Settings.getDefaultString( settingKey ) );
						else                         // Normal combo box
							comboBox.setSelectedIndex( Settings.getDefaultInt( settingKey ) );
					}
				}
			} );
			box.add( restoreDefaultButton );
		}
		
		public void storeSetting() {
			if ( component instanceof JSpinner )
				Settings.set( settingKey, ( (JSpinner) component ).getValue() );
			else if ( component instanceof JSlider )
				Settings.set( settingKey, ( (JSlider) component ).getValue() );
			else if ( component instanceof JTextField )
				Settings.set( settingKey, ( (JTextField) component ).getText() );
			else if ( component instanceof JCheckBox )
				Settings.set( settingKey, ( (JCheckBox) component ).isSelected() );
			else if ( component instanceof JComboBox ) {
				Settings.set( settingKey, ( (JComboBox< ? >) component ).getSelectedIndex() );
				final JComboBox< ? > comboBox = (JComboBox< ? >) component;
				if ( comboBox.isEditable() ) // It's a pre-defined list combo box
					Settings.set( settingKey, comboBox.getSelectedItem() );				
				else                         // Normal combo box
					Settings.set( settingKey, comboBox.getSelectedIndex() );				
			}
		}
	}
	
	/**
	 * Creates a new MiscSettingsDialog.<br>
	 * The MainFrame instance will be the owner of the dialog.
	 * @param tabToSelect           optional tab to select
	 */
	public MiscSettingsDialog( final SettingsTab tabToSelect ) {
		this( MainFrame.INSTANCE, tabToSelect, null );
	}
	
	/**
	 * Creates a new MiscSettingsDialog.
	 * @param owner                 the Frame from which the misc settings dialog is displayed
	 * @param tabToSelect           optional tab to select
	 * @param initialPredefinedList optional initial pre-defined list to select
	 */
	public MiscSettingsDialog( final Frame owner, final SettingsTab tabToSelect, final PredefinedList initialPredefinedList ) {
		super( owner, "miscSettings.title", Icons.EQUALIZER );
		
		final JTabbedPane tabbedPane = new JTabbedPane( JTabbedPane.LEFT );
		
		final JSlider soundVolumenSlider = new JSlider ( 0, 100, Settings.getInt( Settings.KEY_SETTINGS_MISC_SOUND_VOLUME ) );
		soundVolumenSlider.setMajorTickSpacing( 20 );
		soundVolumenSlider.setMinorTickSpacing( 10 );
		soundVolumenSlider.setPaintTicks( true );
		soundVolumenSlider.setPaintLabels( true );
		final JButton testSoundButton = new JButton( Icons.CONTROL );
		GuiUtils.updateButtonText( testSoundButton, "miscSettings.testSoundButton" );
		testSoundButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				Sounds.playSoundSample( Sounds.SAMPLE_SC2GEARS, false, soundVolumenSlider.getValue() );
			}
		} );
		
		final JComboBox< String > repAutoSaveNameTemplateComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.REP_AUTO_SAVE_TEMPLATE, false );
		repAutoSaveNameTemplateComboBox.setToolTipText( Language.getText( "miscSettings.repAutoSaveNameTemplateToolTip" ) );
		final JButton editTemplateButton = new JButton( Icons.UI_SCROLL_PANE_LIST );
		GuiUtils.updateButtonText( editTemplateButton, "miscSettings.editTemplateButton" );
		editTemplateButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				new AutoSaveNameTemplateEditorDialog( MiscSettingsDialog.this, repAutoSaveNameTemplateComboBox );
			}
		} );
		final JTextField favoredPlayerListTextField = new JTextField( Settings.getString( Settings.KEY_SETTINGS_MISC_FAVORED_PLAYER_LIST ) );
		favoredPlayerListTextField.setToolTipText( Language.getText( "miscSettings.favoredPlayerListToolTip" ) );
		
		final JTextField mousePrintBackgroundColorTextField = new JTextField( Settings.getString( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_BACKGROUND_COLOR ) );
		mousePrintBackgroundColorTextField.setToolTipText( Language.getText( "miscSettings.colorFieldToolTip" ) );
		final JButton chooseBackgroundColorButton = new JButton( Icons.COLOR );
		GuiUtils.updateButtonText( chooseBackgroundColorButton, "miscSettings.chooseColor" );
		chooseBackgroundColorButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final Color color = new ColorChooserDialog( MiscSettingsDialog.this, GeneralUtils.getColorSetting( mousePrintBackgroundColorTextField.getText(), Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_BACKGROUND_COLOR ) ).getColor();
				if ( color != null )
					mousePrintBackgroundColorTextField.setText( color.getRed() + "," + color.getGreen() + "," + color.getBlue() );
			}
		} );
		final JTextField mousePrintColorTextField = new JTextField( Settings.getString( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_COLOR ) );
		mousePrintColorTextField.setToolTipText( Language.getText( "miscSettings.colorFieldToolTip" ) );
		final JButton chooseColorButton = new JButton( Icons.COLOR );
		GuiUtils.updateButtonText( chooseColorButton, "miscSettings.chooseColor" );
		chooseColorButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final Color color = new ColorChooserDialog( MiscSettingsDialog.this, GeneralUtils.getColorSetting( mousePrintColorTextField.getText(), Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_COLOR ) ).getColor();
				if ( color != null )
					mousePrintColorTextField.setText( color.getRed() + "," + color.getGreen() + "," + color.getBlue() );
			}
		} );
		
		final JComboBox< DataCompression > binaryDataCompressionAlgorithmComboBox = new JComboBox<>( DataCompression.values() );
		binaryDataCompressionAlgorithmComboBox.setMaximumRowCount( binaryDataCompressionAlgorithmComboBox.getModel().getSize() ); // Display all output formats
		try {
			binaryDataCompressionAlgorithmComboBox.setSelectedIndex( Settings.getInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_DATA_COMPRESSION ) );
		} catch ( final IllegalArgumentException iae ) {
			binaryDataCompressionAlgorithmComboBox.setSelectedIndex( Settings.getDefaultInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_DATA_COMPRESSION ) );
		}
		
		final JComboBox< BnetLanguage > bnetLanguagesComboBox = new JComboBox<>( BnetLanguage.values() );
		bnetLanguagesComboBox.setMaximumRowCount( bnetLanguagesComboBox.getModel().getSize() ); // Display all bnet languages
		bnetLanguagesComboBox.setToolTipText( Language.getText( "miscSettings.preferredBnetLanguageToolTip" ) );
		bnetLanguagesComboBox.setRenderer( new BaseLabelListCellRenderer< BnetLanguage >() {
			@Override
			public Icon getIcon( final BnetLanguage value ) {
				return Icons.getLanguageIcon( Language.getDefaultText( value.textKey ) );
			}
		} );
		
		final JButton emptyProfileCacheButton = new JButton( Icons.CROSS );
		GuiUtils.updateButtonText( emptyProfileCacheButton, "miscSettings.emptyReplayCacheButton" );
		emptyProfileCacheButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				if ( GuiUtils.showConfirmDialog( Language.getText( "miscSettings.confirmEmptyProfileCache" ), true ) != 0 )
					return;
				ProfileCache.emptyCache( MiscSettingsDialog.this );
			}
		} );
		
		final JButton emptyReplayCacheButton = new JButton( Icons.CROSS );
		GuiUtils.updateButtonText( emptyReplayCacheButton, "miscSettings.emptyReplayCacheButton" );
		emptyReplayCacheButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				if ( GuiUtils.showConfirmDialog( Language.getText( "miscSettings.confirmEmptyReplayCache" ), true ) != 0 )
					return;
				ReplayCache.emptyCache();
			}
		} );
		
		final String customDateTimeFormatToolTip = Language.getText( "miscSettings.customDateTimeFormatToolTip" );
		final JComboBox< String > customDateFormatTextComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.CUSTOM_DATE_FORMAT, false );
		customDateFormatTextComboBox.setToolTipText( customDateTimeFormatToolTip );
		final JPanel customDateControlPanel = new JPanel( new BorderLayout() );
		final JButton testCustomDateButton = new JButton( Language.getText( "miscSettings.testFormatButton" ) );
		customDateControlPanel.add( testCustomDateButton, BorderLayout.CENTER );
		customDateControlPanel.add( GuiUtils.createDateTimeFormatHelpLinkLabel(), BorderLayout.EAST );
		final JComboBox< String > customTimeFormatTextComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.CUSTOM_TIME_FORMAT, false );
		customTimeFormatTextComboBox.setToolTipText( customDateTimeFormatToolTip );
		final JPanel customTimeControlPanel = new JPanel( new BorderLayout() );
		final JButton testCustomTimeButton = new JButton( Language.getText( "miscSettings.testFormatButton" ) );
		customTimeControlPanel.add( testCustomTimeButton, BorderLayout.CENTER );
		customTimeControlPanel.add( GuiUtils.createDateTimeFormatHelpLinkLabel(), BorderLayout.EAST );
		final JComboBox< String > customDateTimeFormatTextComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.CUSTOM_DATE_TIME_FORMAT, false );
		customDateTimeFormatTextComboBox.setToolTipText( customDateTimeFormatToolTip );
		final JPanel customDateTimeControlPanel = new JPanel( new BorderLayout() );
		final JButton testCustomDateTimeButton = new JButton( Language.getText( "miscSettings.testFormatButton" ) );
		customDateTimeControlPanel.add( testCustomDateTimeButton, BorderLayout.CENTER );
		customDateTimeControlPanel.add( GuiUtils.createDateTimeFormatHelpLinkLabel(), BorderLayout.EAST );
		final ActionListener testCustomDateTimeActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final String pattern = ( event.getSource() == testCustomDateButton ? customDateFormatTextComboBox
						: event.getSource() == testCustomTimeButton ? customTimeFormatTextComboBox : customDateTimeFormatTextComboBox ).getSelectedItem().toString();
				try {
					final String currentTime = new SimpleDateFormat( pattern ).format( new Date() );
					GuiUtils.showInfoDialog( new Object[] { Language.getText( "miscSettings.dateTimeFormatValid" ), " ", Language.getText( "miscSettings.currentDateTimeWithFormat" ), currentTime } );
				} catch ( IllegalArgumentException iae ) {
					iae.printStackTrace();
					GuiUtils.showErrorDialog( Language.getText( "miscSettings.dateTimeFormatInvalid" ) );
				}
			}
		};
		testCustomDateButton    .addActionListener( testCustomDateTimeActionListener );
		testCustomTimeButton    .addActionListener( testCustomDateTimeActionListener );
		testCustomDateTimeButton.addActionListener( testCustomDateTimeActionListener );
		
		final Holder< ProxyConfig > proxyConfigHolder = new Holder< ProxyConfig >();
		final JButton proxyConfigButton = new JButton( Icons.GLOBE_NETWORK );
		GuiUtils.updateButtonText( proxyConfigButton, "miscSettings.proxyConfigButton" );
		proxyConfigButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				if ( proxyConfigHolder.value == null )
					proxyConfigHolder.value = new ProxyConfig();
				new ProxyConfigDialog( proxyConfigHolder.value );
			}
		} );
		final JCheckBox enableProxyConfigCheckBox = new JCheckBox( Language.getText( "miscSettings.enableProxyConfig" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_ENABLE_PROXY_CONFIG ) );
		final ActionListener enableProxyConfigActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				proxyConfigButton.setEnabled( enableProxyConfigCheckBox.isSelected() );
			}
		};
		enableProxyConfigActionListener.actionPerformed( null );
		enableProxyConfigCheckBox.addActionListener( enableProxyConfigActionListener );
		
		final JTextField mouseGameRandomSeedTextField = new JTextField( Settings.getString( Settings.KEY_SETTINGS_MOUSE_GAME_RANDOM_SEED ) );
		mouseGameRandomSeedTextField.setToolTipText( Language.getText( "miscSettings.randomSeedToolTip" ) );
		
		final SettingRow[][] settingGroups = {
			// Replay auto-save
			{
				new SettingRow( "miscSettings.repAutoSaveNameTemplate", Settings.KEY_SETTINGS_MISC_REP_AUTO_SAVE_NAME_TEMPLATE,
					repAutoSaveNameTemplateComboBox, editTemplateButton ),
				new SettingRow( "miscSettings.newReplayDetectionMethod", Settings.KEY_SETTINGS_MISC_NEW_REPLAY_DETECTION_METHOD,
					new JComboBox<>( ReplayAutoSaver.fileMonitorSupported ? NewFileDetectionMethod.values() : new NewFileDetectionMethod[] { NewFileDetectionMethod.POLLING } ), new JLabel() ),
				new SettingRow( "miscSettings.newRepCheckIntervalSec", Settings.KEY_SETTINGS_MISC_NEW_REP_CHECK_INTERVAL_SEC,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_NEW_REP_CHECK_INTERVAL_SEC ), 1, 100, 1 ) ), new JLabel( Language.getText( "miscSettings.seconds" ) ) ),
				new SettingRow( "miscSettings.playReplaySavedVoiceWhenReplaysAreSaved", Settings.KEY_SETTINGS_MISC_PLAY_REPLAY_SAVED_VOICE,
					new JCheckBox( Language.getText( "miscSettings.playReplaySavedVoice" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_PLAY_REPLAY_SAVED_VOICE ) ), new JLabel() ),
				new SettingRow( "miscSettings.deleteSuccessfullyAutoSavedReplays", Settings.KEY_SETTINGS_MISC_DELETE_AUTO_SAVED_REPLAYS,
					new JCheckBox( Language.getText( "miscSettings.deleteAutoSavedReplays" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_DELETE_AUTO_SAVED_REPLAYS ) ), new JLabel() ),
				new SettingRow( "miscSettings.autoOpenNewReplaysInAnalyzer", Settings.KEY_SETTINGS_MISC_AUTO_OPEN_NEW_REPLAYS,
					new JCheckBox( Language.getText( "miscSettings.autoOpenNewReplays" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_AUTO_OPEN_NEW_REPLAYS ) ), new JLabel() ),
				new SettingRow( "miscSettings.showGameInfoDialogWhenNewReplayDetected", Settings.KEY_SETTINGS_MISC_SHOW_GAME_INFO_FOR_NEW_REPLAYS,
					new JCheckBox( Language.getText( "miscSettings.showOnTopGameInfoDialogForNewReplays" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_SHOW_GAME_INFO_FOR_NEW_REPLAYS ) ), new JLabel() ),
				new SettingRow( "miscSettings.autoStoreNewReplaysInDatabase", Settings.KEY_SETTINGS_MISC_AUTO_STORE_NEW_REPLAYS,
					new JCheckBox( Language.getText( "miscSettings.autoStoreNewReplays" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_AUTO_STORE_NEW_REPLAYS ) ), new JLabel() )
			},
			// APM alert
			{
				new SettingRow( "miscSettings.apmAlertLevel", Settings.KEY_SETTINGS_MISC_APM_ALERT_LEVEL,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_APM_ALERT_LEVEL ), 0, 1000, 1 ) ), new JLabel( Language.getText( "miscSettings.apm" ) ) ),
				new SettingRow( "miscSettings.apmWarmupTime", Settings.KEY_SETTINGS_MISC_APM_WARMUP_TIME,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_APM_WARMUP_TIME ), 0, 300, 1 ) ), new JLabel( Language.getText( "miscSettings.seconds" ) ) ),
				new SettingRow( "miscSettings.apmCheckIntervalSec", Settings.KEY_SETTINGS_MISC_APM_CHECK_INTERVAL_SEC,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_APM_CHECK_INTERVAL_SEC ), 1, 15, 1 ) ), new JLabel( Language.getText( "miscSettings.seconds" ) ) ),
				new SettingRow( "miscSettings.alertWhenApmIsAboveAlertLevelAgain", Settings.KEY_SETTINGS_MISC_ALERT_WHEN_APM_IS_BACK_TO_NORMAL,
					new JCheckBox( Language.getText( "miscSettings.alertWhenApmIsBackToNormal" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_ALERT_WHEN_APM_IS_BACK_TO_NORMAL ) ), new JLabel() ),
				new SettingRow( "miscSettings.alertRepetitionIntervalIfApmStaysLow", Settings.KEY_SETTINGS_MISC_APM_ALERT_REPETITION_INTERVAL_SEC,
					new JComboBox<>( Sc2RegMonitor.APM_ALERT_REPETITION_INTERVALS ), new JLabel( Language.getText( "miscSettings.seconds" ) ) ),
				new SettingRow( "miscSettings.alertOnGameStartEvents", Settings.KEY_SETTINGS_MISC_ALERT_ON_GAME_START,
					new JCheckBox( Language.getText( "miscSettings.alertWhenGamesStarts" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_ALERT_ON_GAME_START ) ), new JLabel() ),
				new SettingRow( "miscSettings.alertOnGameEndEvents", Settings.KEY_SETTINGS_MISC_ALERT_ON_GAME_END,
					new JCheckBox( Language.getText( "miscSettings.alertWhenGamesEnd" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_ALERT_ON_GAME_END ) ), new JLabel() ),
				new SettingRow( "miscSettings.autoShowHideApmDisplayDialog", Settings.KEY_SETTINGS_MISC_SHOW_HIDE_APM_DISPLAY_ON_START_END,
					new JCheckBox( Language.getText( "miscSettings.showHideOnTopApmDisplayDialog" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_SHOW_HIDE_APM_DISPLAY_ON_START_END ) ), new JLabel() ),
			},
			// Mouse print
			{
				new SettingRow( "miscSettings.saveMousePrintsOfAllGames", Settings.KEY_SETTINGS_MISC_SAVE_MOUSE_PRINTS,
					new JCheckBox( Language.getText( "miscSettings.saveMousePrints#2" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_SAVE_MOUSE_PRINTS ) ), new JLabel() ),
				new SettingRow( "miscSettings.storeMousePrintsOfAllGames", Settings.KEY_SETTINGS_MISC_STORE_MOUSE_PRINTS,
					new JCheckBox( Language.getText( "miscSettings.storeMousePrints" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_STORE_MOUSE_PRINTS ) ), new JLabel() ),
				new SettingRow( "miscSettings.mousePrintSamplingTime", Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_SAMPLING_TIME,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_SAMPLING_TIME ), 1, 20, 1 ) ), new JLabel( Language.getText( "miscSettings.ms" ) ) ),
				new SettingRow( "miscSettings.mousePrintWhatToSave", Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_WHAT_TO_SAVE,
					new JComboBox<>( WhatToSave.values() ), GeneralUtils.createLinkLabel( Language.getText( "miscSettings.smpdFormatSpec" ), Consts.URL_SMPD_FORMAT_SPECIFICATION ) ),
				new SettingRow( "miscSettings.binaryDataCompressionAlgorithm", Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_DATA_COMPRESSION,
					new JComboBox<>( DataCompression.values() ), new JLabel() ),
				new SettingRow( "miscSettings.mousePrintImageOutputFormat", Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_IMAGE_OUTPUT_FORMAT,
					new JComboBox<>( MousePrintRecorder.OUTPUT_FORMATS ), new JLabel() ),
				new SettingRow( "miscSettings.useAntialiasingEffect", Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_USE_ANTIALIASING,
					new JCheckBox( Language.getText( "miscSettings.useAntialiasing" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_USE_ANTIALIASING ) ), new JLabel() ),
				new SettingRow( "miscSettings.mousePrintBackgroundColorRGB", Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_BACKGROUND_COLOR,
					mousePrintBackgroundColorTextField, chooseBackgroundColorButton ),
				new SettingRow( "miscSettings.mousePrintColorRGB", Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_COLOR,
					mousePrintColorTextField, chooseColorButton ),
				new SettingRow( "miscSettings.pourInkWhenMouseIsIdleFor", Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_POUR_INK_IDLE_TIME,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_POUR_INK_IDLE_TIME ), 1, 30, 1 ) ), new JLabel( Language.getText( "miscSettings.seconds" ) ) ),
				new SettingRow( "miscSettings.idleInkFlowRate", Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_IDLE_INK_FLOW_RATE,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_IDLE_INK_FLOW_RATE ), 1, 1500, 1 ) ), new JLabel( Language.getText( "miscSettings.pixelPersec" ) ) ),
				new SettingRow( "miscSettings.mouseWarmupTime", Settings.KEY_SETTINGS_MISC_MOUSE_WARMUP_TIME,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_MOUSE_WARMUP_TIME ), 0, 300, 1 ) ), new JLabel( Language.getText( "miscSettings.seconds" ) ) )
			},
			// User interface
			{
				new SettingRow( "miscSettings.preferredBnetLanguage", Settings.KEY_SETTINGS_MISC_PREFERRED_BNET_LANGUAGE,
					bnetLanguagesComboBox, new JLabel() ),
				new SettingRow( "miscSettings.navBarInitialWidth", Settings.KEY_SETTINGS_MISC_NAV_BAR_INITIAL_WIDTH,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_NAV_BAR_INITIAL_WIDTH ), 0, 1000, 1 ) ), new JLabel( Language.getText( "miscSettings.pixels" ) ) ),
				new SettingRow( "miscSettings.chartsActionListPartition", Settings.KEY_SETTINGS_MISC_CHARTS_ACTION_LIST_PARTITIONING,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_CHARTS_ACTION_LIST_PARTITIONING ), 0, 100, 1 ) ), new JLabel( Language.getText( "miscSettings.percent" ) ) ),
				new SettingRow( "miscSettings.toolTipInitialDelay", Settings.KEY_SETTINGS_MISC_TOOL_TIP_INITIAL_DELAY,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_TOOL_TIP_INITIAL_DELAY ), 0, 10000, 1 ) ), new JLabel( Language.getText( "miscSettings.ms" ) ) ),
				new SettingRow( "miscSettings.toolTipDismissDelay", Settings.KEY_SETTINGS_MISC_TOOL_TIP_DISMISS_DELAY,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_TOOL_TIP_DISMISS_DELAY ), 0, 50000, 1 ) ), new JLabel( Language.getText( "miscSettings.ms" ) ) ),
				new SettingRow( "miscSettings.displayInfoBalloonWhenStartedMinimized", Settings.KEY_SETTINGS_MISC_DISPLAY_INFO_WHEN_STARTED_MINIMIZED,
					new JCheckBox( Language.getText( "miscSettings.displayInfoBalloonWhenStartedMinimized2" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_DISPLAY_INFO_WHEN_STARTED_MINIMIZED ) ), new JLabel() ),
			},
			// Replay parser
			{
				new SettingRow( "miscSettings.useRealTimeMeasurement", Settings.KEY_SETTINGS_MISC_USE_REAL_TIME_MEASUREMENT,
					new JCheckBox( Language.getText( "miscSettings.convertGameTimeToRealTime" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_USE_REAL_TIME_MEASUREMENT ) ), new JLabel() ),
				new SettingRow( "miscSettings.initialTimeToExcludeFromApm", Settings.KEY_SETTINGS_MISC_INITIAL_TIME_TO_EXCLUDE_FROM_APM,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_INITIAL_TIME_TO_EXCLUDE_FROM_APM ), 0, 1000, 1 ) ), new JLabel( Language.getText( "miscSettings.seconds" ) ) ),
				new SettingRow( "miscSettings.favoredPlayerList", Settings.KEY_SETTINGS_MISC_FAVORED_PLAYER_LIST,
					favoredPlayerListTextField, new JLabel() ),
				new SettingRow( "miscSettings.declareLargestWinner", Settings.KEY_SETTINGS_MISC_DECLARE_LARGEST_AS_WINNER,
					new JCheckBox( Language.getText( "miscSettings.largestRemainingTeamWins" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_DECLARE_LARGEST_AS_WINNER ) ), new JLabel() ),
				new SettingRow( "miscSettings.overrideDetectedFormatBasedOnMatchup", Settings.KEY_SETTINGS_MISC_OVERRIDE_FORMAT_BASED_ON_MATCHUP,
					new JCheckBox( Language.getText( "miscSettings.overrideFormatBasedOnMatchup" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_OVERRIDE_FORMAT_BASED_ON_MATCHUP ) ), new JLabel() ),
				new SettingRow( "miscSettings.enableInternalPreprocessedReplayCache", Settings.KEY_SETTINGS_MISC_CACHE_PREPROCESSED_REPLAYS,
					new JCheckBox( Language.getText( "miscSettings.cachePreprocessedReplays" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_CACHE_PREPROCESSED_REPLAYS ) ), emptyReplayCacheButton ),
				new SettingRow( "miscSettings.useMd5HashFromFilename", Settings.KEY_SETTINGS_MISC_USE_MD5_HASH_FROM_FILE_NAME,
					new JCheckBox( Language.getText( "miscSettings.useMd5HashIfFoundInFileNames" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_USE_MD5_HASH_FROM_FILE_NAME ) ), new JLabel() )
			},
			// Replay analyzer
			{
				new SettingRow( "miscSettings.showProfileInfo", Settings.KEY_REP_MISC_ANALYZER_SHOW_PROFILE_INFO,
					new JComboBox<>( ShowProfileInfo.values() ), emptyProfileCacheButton ),
    			new SettingRow( "miscSettings.autoRetrieveExtProfileInfo", Settings.KEY_REP_MISC_ANALYZER_AUTO_RETRIEVE_EXT_PROFILE_INFO,
    				new JCheckBox( Language.getText( "miscSettings.autoRetrieveExtProfileInfo2" ), Settings.getBoolean( Settings.KEY_REP_MISC_ANALYZER_AUTO_RETRIEVE_EXT_PROFILE_INFO ) ), new JLabel() ),
				new SettingRow( "miscSettings.profileInfoValidityTime", Settings.KEY_SETTINGS_MISC_PROFILE_INFO_VALIDITY_TIME,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_PROFILE_INFO_VALIDITY_TIME ), 1, 30, 1 ) ), new JLabel( Language.getText( "miscSettings.days" ) ) ),
				new SettingRow( "miscSettings.maxTableSizeInProfileToolTip", Settings.KEY_REP_MISC_ANALYZER_MAX_ROWS_IN_PROFILE_TOOL_TIP,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_REP_MISC_ANALYZER_MAX_ROWS_IN_PROFILE_TOOL_TIP ), 0, 100, 1 ) ), new JLabel( Language.getText( "miscSettings.rows" ) ) ),
				new SettingRow( "miscSettings.rearrangePlayersInRepAnalyzer", Settings.KEY_SETTINGS_MISC_REARRANGE_PLAYERS_IN_REP_ANALYZER,
					new JCheckBox( Language.getText( "miscSettings.applyFavoredListInRepAnalyzer" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_REARRANGE_PLAYERS_IN_REP_ANALYZER ) ), new JLabel() ),
				new SettingRow( "miscSettings.showWinners", Settings.KEY_SETTINGS_MISC_SHOW_WINNERS,
					new JCheckBox( Language.getText( "miscSettings.showWinners2" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_SHOW_WINNERS ) ), new JLabel() ),
				new SettingRow( "miscSettings.animateRefreshRate", Settings.KEY_SETTINGS_MISC_ANIMATOR_FPS,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_ANIMATOR_FPS ), 1, 30, 1 ) ), new JLabel( Language.getText( "miscSettings.fps" ) ) ),
				new SettingRow( "miscSettings.animatorJumpTime", Settings.KEY_SETTINGS_MISC_ANIMATOR_JUMP_TIME,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_ANIMATOR_JUMP_TIME ), 1, 1200, 1 ) ), new JLabel( Language.getText( "miscSettings.seconds" ) ) )
			},
			// Multi-rep analysis
			{
				new SettingRow( "miscSettings.timeLimitForMultiRepAnalysis", Settings.KEY_SETTINGS_MISC_TIME_LIMIT_FOR_MULTI_REP_ANALYSIS,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_TIME_LIMIT_FOR_MULTI_REP_ANALYSIS ), 0, 1000, 1 ) ), new JLabel( Language.getText( "miscSettings.seconds" ) ) ),
				new SettingRow( "miscSettings.buildOrderLength", Settings.KEY_SETTINGS_MISC_BUILD_ORDER_LENGTH,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_BUILD_ORDER_LENGTH ), 1, 100, 1 ) ), new JLabel( Language.getText( "miscSettings.buildings" ) ) ),
				new SettingRow( "miscSettings.gameLengthRecordsGranularity", Settings.KEY_SETTINGS_MISC_GAME_LENGTH_RECORDS_GRANULARITY,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_GAME_LENGTH_RECORDS_GRANULARITY ), 1, 120, 1 ) ), new JLabel( Language.getText( "miscSettings.minutes" ) ) ),
				new SettingRow( "miscSettings.maxGamingSessionBreak", Settings.KEY_SETTINGS_MISC_MAX_GAMING_SESSION_BREAK,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_MAX_GAMING_SESSION_BREAK ), 1, 600, 1 ) ), new JLabel( Language.getText( "miscSettings.minutes" ) ) )
			},
			// Internal
			{
				new SettingRow( "miscSettings.soundVolume", Settings.KEY_SETTINGS_MISC_SOUND_VOLUME,
					soundVolumenSlider, testSoundButton ),
				new SettingRow( "miscSettings.timeToKeepLogFiles", Settings.KEY_SETTINGS_MISC_TIME_TO_KEEP_LOG_FILES,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_TIME_TO_KEEP_LOG_FILES ), 0, 10000, 1 ) ), new JLabel( Language.getText( "miscSettings.days" ) ) ),
				new SettingRow( "miscSettings.preloadSc2IconsOnStartup", Settings.KEY_SETTINGS_MISC_PRELOAD_SC2_ICONS_ON_STARTUP,
					new JCheckBox( Language.getText( "miscSettings.preloadSc2IconsOnStartup2" ), Settings.getBoolean( Settings.KEY_SETTINGS_MISC_PRELOAD_SC2_ICONS_ON_STARTUP ) ), new JLabel() ),
				new SettingRow( "miscSettings.allowOnlyOneInstance", Settings.KEY_SETTINGS_ALLOW_ONLY_ONE_INSTANCE,
					new JCheckBox( Language.getText( "miscSettings.enableInstanceMonitor" ), Settings.getBoolean( Settings.KEY_SETTINGS_ALLOW_ONLY_ONE_INSTANCE ) ), new JLabel() ),
				new SettingRow( "miscSettings.accessTheInternetThroughProxy", Settings.KEY_SETTINGS_MISC_ENABLE_PROXY_CONFIG,
					enableProxyConfigCheckBox, proxyConfigButton ),
				new SettingRow( "miscSettings.utilizedCpuCores", Settings.KEY_SETTINGS_MISC_UTILIZED_CPU_CORES,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MISC_UTILIZED_CPU_CORES ), 0, 32, 1 ) ), new JLabel( "(" + Language.getText( "miscSettings.detectedCpuCores", Runtime.getRuntime().availableProcessors() ) + ")" ) ),
				new SettingRow( "miscSettings.maxReplaysToOpenForOpenInAnalyzer", Settings.KEY_SETTINGS_MAX_REPLAYS_TO_OPEN_FOR_OPEN_IN_ANALYZER,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MAX_REPLAYS_TO_OPEN_FOR_OPEN_IN_ANALYZER ), 1, 15, 1 ) ), new JLabel() ),
				new SettingRow( "miscSettings.customDateFormat", Settings.KEY_SETTINGS_MISC_CUSTOM_DATE_FORMAT,
					customDateFormatTextComboBox, customDateControlPanel ),
				new SettingRow( "miscSettings.customTimeFormat", Settings.KEY_SETTINGS_MISC_CUSTOM_TIME_FORMAT,
					customTimeFormatTextComboBox, customTimeControlPanel ),
				new SettingRow( "miscSettings.customDateTimeFormat", Settings.KEY_SETTINGS_MISC_CUSTOM_DATE_TIME_FORMAT,
					customDateTimeFormatTextComboBox, customDateTimeControlPanel )
			},
			// Mouse game rules
			{
				new SettingRow( "miscSettings.gameSpeedRefreshRate", Settings.KEY_SETTINGS_MOUSE_GAME_FPS,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_FPS ), 1, 50, 1 ) ), new JLabel( Language.getText( "miscSettings.fps" ) ) ),
				new SettingRow( "miscSettings.maxDiscRadius", Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISC_RADIUS,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISC_RADIUS ), 1, 200, 1 ) ), new JLabel( Language.getText( "miscSettings.pixels" ) ) ),
				new SettingRow( "miscSettings.maxDiscAge", Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISC_AGE,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISC_AGE ), 1, 10000, 1 ) ), new JLabel( Language.getText( "miscSettings.ms" ) ) ),
				new SettingRow( "miscSettings.maxDiscScore", Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISC_SCORE,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISC_SCORE ), 1, 1000, 1 ) ), new JLabel( Language.getText( "miscSettings.points" ) ) ),
				new SettingRow( "miscSettings.maxDiscsMissed", Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISCS_MISSED,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISCS_MISSED ), 1, 1000, 1 ) ), new JLabel() ),
				new SettingRow( "miscSettings.discMovementSpeed", Settings.KEY_SETTINGS_MOUSE_GAME_DISC_SPEED,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_DISC_SPEED ), 0, 500, 1 ) ), new JLabel( Language.getText( "miscSettings.pixelPersec" ) ) ),
				new SettingRow( "miscSettings.greenDiscProbability", Settings.KEY_SETTINGS_MOUSE_GAME_FRIENDLY_DISC_PROBABILITY,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_FRIENDLY_DISC_PROBABILITY ), 0, 100, 1 ) ), new JLabel( Language.getText( "miscSettings.percent" ) ) ),
				new SettingRow( "miscSettings.initialDelayForNewDisc", Settings.KEY_SETTINGS_MOUSE_GAME_INITIAL_DELAY_FOR_NEW_DISC,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_INITIAL_DELAY_FOR_NEW_DISC ), 1, 10000, 1 ) ), new JLabel( Language.getText( "miscSettings.ms" ) ) ),
				new SettingRow( "miscSettings.newDiscDelayDecrement", Settings.KEY_SETTINGS_MOUSE_GAME_NEW_DISC_DELAY_DECREMENT,
					new JSpinner( new SpinnerNumberModel( Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_NEW_DISC_DELAY_DECREMENT ), 0, 100, 1 ) ), new JLabel( Language.getText( "miscSettings.msPerDisc" ) ) ),
				new SettingRow( "miscSettings.randomSeed", Settings.KEY_SETTINGS_MOUSE_GAME_RANDOM_SEED,
					mouseGameRandomSeedTextField, new JLabel() ),
				new SettingRow( "miscSettings.paintCrossAtCenterOfDiscs", Settings.KEY_SETTINGS_MOUSE_GAME_PAINT_DISC_CENTER_CROSS,
					new JCheckBox( Language.getText( "miscSettings.paintDiscCenterCross" ), Settings.getBoolean( Settings.KEY_SETTINGS_MOUSE_GAME_PAINT_DISC_CENTER_CROSS ) ), new JLabel() ),
				new SettingRow( "miscSettings.paintCircleAroundDiscs", Settings.KEY_SETTINGS_MOUSE_GAME_PAINT_MAX_DISC_OUTLINE,
					new JCheckBox( Language.getText( "miscSettings.paintMaxDiscOutline" ), Settings.getBoolean( Settings.KEY_SETTINGS_MOUSE_GAME_PAINT_MAX_DISC_OUTLINE ) ), new JLabel() )
			}
		};
		
		final SettingsTab[] settingsTabs = new SettingsTab[] { SettingsTab.REPLAY_AUTO_SAVE, SettingsTab.APM_ALERT, SettingsTab.MOUSE_PRINT, SettingsTab.USER_INTERFACE, SettingsTab.REPLAY_PARSER, SettingsTab.REPLAY_ANALYZER, SettingsTab.MULTI_REP_ANALYSIS, SettingsTab.INTERNAL, SettingsTab.MOUSE_GAME_RULES };
		for ( int i = 0; i < settingGroups.length; i++ ) {
			final Box tabBox = Box.createVerticalBox();
			tabBox.setBorder( BorderFactory.createEmptyBorder( 5, 5, 0, 5 ) );
			
			GuiUtils.addNewTab( settingsTabs[ i ].text, settingsTabs[ i ].icon, false, tabbedPane, GuiUtils.wrapInPanel( tabBox ), null );
			if ( tabToSelect == settingsTabs[ i ] )
				tabbedPane.setSelectedIndex( tabbedPane.getTabCount() - 1 );
			
			final SettingRow[] settingGroup = settingGroups[ i ];
			
			for ( final SettingRow settingRow : settingGroup )
				tabBox.add( settingRow.box );
		}
		
		// Create a "form-style" (same width for all columns)
		for ( int column = 0; column < 3; column++ ) {
			// Find max column width
			int maxWidth = 0;
			for ( final SettingRow[] settingGroup : settingGroups )
				for ( final SettingRow settingRow : settingGroup )
					maxWidth = Math.max( settingRow.box.getComponent( column ).getPreferredSize().width, maxWidth );
			
			// Set same width for all labels
			for ( final SettingRow[] settingGroup : settingGroups )
				for ( final SettingRow settingRow : settingGroup )
					settingRow.box.getComponent( column ).setPreferredSize( new Dimension( maxWidth, settingRow.box.getComponent( column ).getPreferredSize().height ) );
		}
		
		// Aliases tab is unique
		final JTextArea playerAliasesTextArea = new JTextArea( Settings.getString( Settings.KEY_SETTINGS_MISC_PLAYER_ALIASES ), 1, 1 );
		final JTextArea mapAliasesTextArea    = new JTextArea( Settings.getString( Settings.KEY_SETTINGS_MISC_MAP_ALIASES    ), 1, 1 );
		{
			final JPanel panel = new JPanel( new BorderLayout() );
			panel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 0, 5 ) );
			Box box = Box.createVerticalBox();
			box.add( new JLabel( Language.getText( "miscSettings.aliasInfo" ) ) );
			box.add( new JLabel( Language.getText( "miscSettings.aliasInfo2" ) ) );
			box.add( new JLabel( Language.getText( "miscSettings.aliasInfo3" ) ) );
			panel.add( GuiUtils.wrapInPanel( box ), BorderLayout.NORTH );
			box = Box.createHorizontalBox();
			final JScrollPane playerAliasesScrollPane = new JScrollPane( playerAliasesTextArea );
			playerAliasesScrollPane.setBorder( BorderFactory.createTitledBorder( Language.getText( "miscSettings.playerAliases" ) ) );
			box.add( playerAliasesScrollPane );
			final JScrollPane mapAliasesScrollPane = new JScrollPane( mapAliasesTextArea );
			mapAliasesScrollPane.setBorder( BorderFactory.createTitledBorder( Language.getText( "miscSettings.mapAliases" ) ) );
			box.add( mapAliasesScrollPane );
			panel.add( box, BorderLayout.CENTER );
			GuiUtils.addNewTab( SettingsTab.ALIASES.text, SettingsTab.ALIASES.icon, false, tabbedPane, panel, null );
			if ( tabToSelect == SettingsTab.ALIASES )
				tabbedPane.setSelectedIndex( tabbedPane.getTabCount() - 1 );
		}
		
		// Pre-defined lists tab is unique
		final Map< PredefinedList, String > predefinedListCacheMap = new EnumMap< PredefinedList, String >( PredefinedList.class );
		final JComboBox< PredefinedList > predefinedListComboBox = new JComboBox<>( PredefinedList.values() );
		if ( initialPredefinedList != null )
			predefinedListComboBox.setSelectedItem( initialPredefinedList );
		predefinedListComboBox.setMaximumRowCount( predefinedListComboBox.getItemCount() ); // Display all values
		predefinedListComboBox.setRenderer( new BaseLabelListCellRenderer< PredefinedList >( 2 ) {
			@Override
			public Icon getIcon( final PredefinedList value ) {
				return value.icon;
			}
		} );
		final JTextArea predefinedListTextArea = new JTextArea( 18, 1 ); // I set the rows count to 18 because Easynth LAF has big tabs and it wouldn't fit in 1 column...
		final ActionListener predefinedListActionListener;
		{
			final JPanel panel = new JPanel( new BorderLayout() );
			panel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 0, 5 ) );
			final Box northBox = Box.createVerticalBox();
			final JPanel chooserPanel = new JPanel();
			chooserPanel.add( new JLabel( Language.getText( "miscSettings.selectPredefinedList" ) ) );
			predefinedListActionListener = new ActionListener() {
				private PredefinedList previousPredefinedList;
				@Override
				public void actionPerformed( final ActionEvent event ) {
					if ( previousPredefinedList != null )
						predefinedListCacheMap.put( previousPredefinedList, predefinedListTextArea.getText() );
					
					final PredefinedList predefinedList = (PredefinedList) predefinedListComboBox.getSelectedItem();
					// If it has been just edited, load the edited value...
					String predefinedListValue = predefinedListCacheMap.get( predefinedList );
					if ( predefinedListValue == null )
						predefinedListValue = Settings.getString( predefinedList.settingsKey );
					predefinedListTextArea.setText( predefinedListValue );
					predefinedListTextArea.setCaretPosition( 0 );
					previousPredefinedList = predefinedList;
				}
			};
			predefinedListActionListener.actionPerformed( null ); // Load the value of the selected pre-defined list
			predefinedListComboBox.addActionListener( predefinedListActionListener );
			chooserPanel.add( predefinedListComboBox );
			northBox.add( chooserPanel );
			final JLabel infoLabel = new JLabel( Language.getText( "miscSettings.predefinedList.info" ) );
			northBox.add( GuiUtils.wrapInPanel( infoLabel ) );
			panel.add( northBox, BorderLayout.NORTH );
			panel.add( new JScrollPane( predefinedListTextArea ), BorderLayout.CENTER );
			final JPanel buttonsPanel = new JPanel( new GridLayout( 2, 1 ) );
			final JButton sortButton = new JButton( Icons.SORT_ALPHABET );
			GuiUtils.updateButtonText( sortButton, "miscSettings.predefinedListSortButton" );
			sortButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					final List< String > lineVector = new ArrayList< String >();
					Settings.breakIntoLines( predefinedListTextArea.getText(), lineVector );
					Collections.sort( lineVector );
					predefinedListTextArea.setText( "" );
					for ( int i = 0; i < lineVector.size(); i++ ) {
						if ( i > 0 )
							predefinedListTextArea.append( "\n" );
						predefinedListTextArea.append( lineVector.get( i ) );
					}
					predefinedListTextArea.setCaretPosition( 0 );
				}
			} );
			buttonsPanel.add( sortButton );
			final JButton restoreDefaultButton = new JButton();
			GuiUtils.updateButtonText( restoreDefaultButton, "miscSettings.restoreDefault" );
			restoreDefaultButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					predefinedListTextArea.setText( Settings.getDefaultString( ( (PredefinedList) predefinedListComboBox.getSelectedItem() ).settingsKey ) );
					predefinedListTextArea.setCaretPosition( 0 );
				}
			} );
			buttonsPanel.add( restoreDefaultButton );
			panel.add( GuiUtils.wrapInPanel( buttonsPanel ), BorderLayout.EAST );
			GuiUtils.addNewTab( SettingsTab.PREDEFINED_LISTS.text, SettingsTab.PREDEFINED_LISTS.icon, false, tabbedPane, panel, null );
			if ( tabToSelect == SettingsTab.PREDEFINED_LISTS )
				tabbedPane.setSelectedIndex( tabbedPane.getTabCount() - 1 );
		}
		
		// Folders tab is unique
		final List< FolderRow > folderRowList = new ArrayList< FolderRow >();
		{
			final Box tabBox = Box.createVerticalBox();
			tabBox.setBorder( BorderFactory.createEmptyBorder( 5, 5, 0, 5 ) );
			
			folderRowList.add( new FolderRow( "folderSettings.sc2InstallationFolder" , Settings.KEY_SETTINGS_FOLDER_SC2_INSTALLATION  , false, null, null ) );
			folderRowList.add( new FolderRow( "folderSettings.sc2AutoReplay"         , Settings.KEY_SETTINGS_FOLDER_SC2_AUTO_REPLAY   , true , null, null ) );
			for ( int i = 2; i <= Settings.MAX_SC2_AUTOREP_FOLDERS; i++ )
				folderRowList.add( new FolderRow( "folderSettings.sc2AutoReplay"     , Settings.KEY_SETTINGS_FOLDER_SC2_AUTO_REPLAY   , true , i   , Settings.KEY_SETTINGS_FOLDER_ENABLE_EXTRA_SC2_AUTO_REPLAY ) );
			folderRowList.add( new FolderRow( "folderSettings.sc2Maps"               , Settings.KEY_SETTINGS_FOLDER_SC2_MAPS          , false, null, null ) );
			folderRowList.add( new FolderRow( "folderSettings.defaultFolder"         , Settings.KEY_SETTINGS_FOLDER_DEFAULT_REPLAY    , false, null, null ) );
			folderRowList.add( new FolderRow( "folderSettings.replayAutoSaveFolder"  , Settings.KEY_SETTINGS_FOLDER_REPLAY_AUTO_SAVE  , false, null, null ) );
			folderRowList.add( new FolderRow( "folderSettings.mousePrintOutputFolder", Settings.KEY_SETTINGS_FOLDER_MOUSE_PRINT_OUTPUT, false, null, null ) );
			
			// Create a "form-style" (same width for all columns)
			int maxWidth = 0;
			for ( final FolderRow folderRow : folderRowList ) {
				tabBox.add( folderRow.getComponent() );
				maxWidth = Math.max( folderRow.infoComponent.getPreferredSize().width, maxWidth );
			}
			// Set same width for all labels
			for ( final FolderRow folderRow : folderRowList )
				folderRow.infoComponent.setPreferredSize( new Dimension( maxWidth, folderRow.infoComponent.getPreferredSize().height ) );
			GuiUtils.addNewTab( SettingsTab.FOLDERS.text, SettingsTab.FOLDERS.icon, false, tabbedPane, GuiUtils.wrapInPanel( tabBox ), null );
			if ( tabToSelect == SettingsTab.FOLDERS )
				tabbedPane.setSelectedIndex( tabbedPane.getTabCount() - 1 );
		}
		
		// Sc2gears Database tab is unique
		final Holder< String > authorizationKeyHolder = new Holder< String >();
		{
			final Box tabBox = Box.createVerticalBox();
			JPanel row = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			row.add( new JLabel( Language.getText( "miscSettings.sc2gearsDatabaseInfo" ) ) );
			row.add( GeneralUtils.createLinkLabel( Language.getText( "miscSettings.sc2gearsDatabaseReadMore" ), Consts.URL_SC2GEARS_DATABASE ) );
			tabBox.add( row );
			row = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			row.add( new JLabel( Language.getText( "miscSettings.sc2gearsDatabaseInfo2" ) ) );
			tabBox.add( row );
			tabBox.add( Box.createVerticalStrut( 10 ) );
			row = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			row.add( new JLabel( Language.getText( "miscSettings.authorizationKey" ) ) );
			final JLabel authKeyLabel = new JLabel();
			GuiUtils.changeFontToBold( authKeyLabel );
			row.add( authKeyLabel );
			tabBox.add( row );
			tabBox.add( Box.createVerticalStrut( 10 ) );
			row = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			final StatusLabel keyStatusLabel = new StatusLabel();
			final JButton editKeyButton = new JButton( Icons.LICENSE_KEY );
			GuiUtils.updateButtonText( editKeyButton, "miscSettings.editKeyButton" );
			final JButton checkKeyButton = new JButton( Icons.SERVER_NETWORK );
			final JButton deleteKeyButton = new JButton( Icons.CROSS );
			final Task< String > setKeyTask = new Task< String >() {
				@Override
				public void execute( final String authorizationKey ) {
					authorizationKeyHolder.value = authorizationKey;
					final boolean keySet = authorizationKey.length() > 0;
					authKeyLabel.setText( keySet ? authorizationKey : "<" + Language.getText( "miscSettings.notSet" ) + ">" );
					deleteKeyButton.setEnabled( keySet );
					checkKeyButton .setEnabled( keySet );
				}
			};
			setKeyTask.execute( Settings.getString( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY ) );
			editKeyButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					final BaseDialog editKeyDialog = new BaseDialog( MiscSettingsDialog.this, "miscSettings.editKeyDialog.title" , Icons.LICENSE_KEY );
					editKeyDialog.setModal( true );
					final Box box = Box.createVerticalBox();
					box.setBorder( BorderFactory.createEmptyBorder( 15, 15, 5, 15 ) );
					Box drow = Box.createHorizontalBox();
					drow.add( new JLabel( Language.getText( "miscSettings.editKeyDialog.info" ) ) );
					drow.add( Box.createHorizontalStrut( 4 ) );
					drow.add( GeneralUtils.createLinkLabel( Language.getText( "miscSettings.editKeyDialog.sc2gearsDatabaseUserPage" ), Consts.URL_SC2GEARS_DATABASE_USER_PAGE ) );
					box.add( drow );
					box.add( Box.createVerticalStrut( 10 ) );
					drow = Box.createHorizontalBox();
					drow.add( new JLabel( Language.getText( "miscSettings.authorizationKey" ) ) );
					final JTextField keyTextField = new JTextField( authorizationKeyHolder.value );
					drow.add( keyTextField );
					box.add( drow );
					box.add( Box.createVerticalStrut( 5 ) );
					final JPanel buttonsPanel = new JPanel();
					final StatusLabel newKeyStatusLabel = new StatusLabel();
					final JButton cancelButton = editKeyDialog.createCloseButton( "button.cancel" );
					final JButton okButton = new JButton();
					GuiUtils.updateButtonText( okButton, "button.ok" );
					okButton.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed( final ActionEvent event ) {
							final String newAuthorizationKey = keyTextField.getText();
							if ( newAuthorizationKey.length() == 0 ) {
								// If key is not provided/deleted, no need to check it (must not check it)
								setKeyTask.execute( "" );
								keyStatusLabel.clearMessage();
								editKeyDialog.dispose();
								return;
							}
							editKeyDialog.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
							keyTextField.setEnabled( false );
							okButton    .setEnabled( false );
							cancelButton.setEnabled( false );
							newKeyStatusLabel.setProgressMessageKey( "miscSettings.checkingKey" );
							// Start a new thread to not block the UI
							new NormalThread( "Key checker" ) {
								@Override
								public void run() {
									final Boolean valid = GeneralUtils.checkAuthorizationKey( newAuthorizationKey );
									final String keyStatusLabelTextKey = valid == null ? "miscSettings.checkingFailed" : valid.booleanValue() ? "miscSettings.checkingValid" : "miscSettings.checkingInvalid";
									if ( valid == null || !valid )
										newKeyStatusLabel.setErrorMessageKey( keyStatusLabelTextKey );
									else
										newKeyStatusLabel.setInfoMessageKey( keyStatusLabelTextKey );
									if ( valid != null && valid.booleanValue() ) {
										setKeyTask    .execute( newAuthorizationKey );
										if ( valid == null || !valid )
											keyStatusLabel.setErrorMessageKey( keyStatusLabelTextKey );
										else
											keyStatusLabel.setInfoMessageKey( keyStatusLabelTextKey );
										editKeyDialog.dispose();
									}
									else {
										cancelButton.setEnabled( true );
										okButton    .setEnabled( true );
										keyTextField.setEnabled( true );
										editKeyDialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
									}
								}
							}.start();
						}
					} );
					buttonsPanel.add( okButton );
					buttonsPanel.add( cancelButton );
					box.add( buttonsPanel );
					box.add( GuiUtils.wrapInPanel( newKeyStatusLabel ) );
					editKeyDialog.add( box );
					editKeyDialog.packAndShow( keyTextField, false );
				}
			} );
			row.add( editKeyButton );
			deleteKeyButton.setEnabled( authorizationKeyHolder.value.length() > 0 );
			GuiUtils.updateButtonText( deleteKeyButton, "miscSettings.deleteKeyButton" );
			deleteKeyButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					setKeyTask.execute( "" );
					keyStatusLabel.clearMessage();
				}
			} );
			row.add( deleteKeyButton );
			checkKeyButton.setEnabled( authorizationKeyHolder.value.length() > 0 );
			GuiUtils.updateButtonText( checkKeyButton, "miscSettings.checkKeyButton" );
			checkKeyButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					editKeyButton  .setEnabled( false );
					deleteKeyButton.setEnabled( false );
					checkKeyButton .setEnabled( false );
					keyStatusLabel.setProgressMessageKey( "miscSettings.checkingKey" );
					// Start a new thread to not block the UI
					new NormalThread( "Key checker" ) {
						@Override
						public void run() {
							final Boolean valid = GeneralUtils.checkAuthorizationKey( authorizationKeyHolder.value );
							if ( valid == null || !valid )
								keyStatusLabel.setErrorMessageKey( valid == null ? "miscSettings.checkingFailed" : "miscSettings.checkingInvalid" );
							else
								keyStatusLabel.setInfoMessageKey( "miscSettings.checkingValid" );
							editKeyButton  .setEnabled( true );
							deleteKeyButton.setEnabled( true );
							checkKeyButton .setEnabled( true );
						}
					}.start();
				}
			} );
			row.add( checkKeyButton );
			row.add( keyStatusLabel );
			tabBox.add( row );
			
			GuiUtils.addNewTab( SettingsTab.SC2GEARS_DATABASE.text, SettingsTab.SC2GEARS_DATABASE.icon, false, tabbedPane, GuiUtils.wrapInPanel( tabBox ), null );
			if ( tabToSelect == SettingsTab.SC2GEARS_DATABASE )
				tabbedPane.setSelectedIndex( tabbedPane.getTabCount() - 1 );
		}
		
		// Custom replay sites tab is unique
		final Vector< ReplayUploadSite > customReplayUploadSiteVector = new Vector< ReplayUploadSite >( Settings.getCustomReplayUploadSiteList() );
		final ActionListener customReplaySiteActionListener;
		{
			final JPanel panel = new JPanel( new BorderLayout() );
			final Box northBox = Box.createVerticalBox();
			JLabel infoLabel = new JLabel( Language.getText( "miscSettings.customReplaySitesInfo" ) );
			GuiUtils.changeFontToBold( infoLabel );
			northBox.add(  GuiUtils.wrapInPanel( infoLabel ) );
			final JCheckBox acknowledgeCheckBox = GuiUtils.createCheckBox( "miscSettings.customReplaySitesAcknowledge", Settings.KEY_SETTINGS_MISC_CUSTOM_REPLAY_SITES_ACKNOWLEDGED );
			northBox.add( GuiUtils.wrapInPanel( acknowledgeCheckBox ) );
			panel.add( northBox, BorderLayout.NORTH );
			final JPanel editPanel = new JPanel( new BorderLayout() );
			editPanel.setBorder( BorderFactory.createTitledBorder( Language.getText( "miscSettings.customReplaySitesBorderTitle" ) ) );
			
			final JComboBox< ReplayUploadSite > customReplaySiteComboBox = new JComboBox<>( new CustomComboBoxModel<>( customReplayUploadSiteVector ) );
			final JTextField displayNameTextField    = new JTextField( 30 );
			final JTextField homePageTextField       = new JTextField( 30 );
			final JLabel     testHomePageLinkLabel   = GeneralUtils.createLinkStyledLabel( Language.getText( "miscSettings.testHomePageLink" ) );
			testHomePageLinkLabel.setIcon( Icons.APPLICATION_BROWSER );
			final JTextField uploadUrlTextField      = new JTextField( 30 );
			final JButton    deleteButton            = new JButton( Icons.CROSS );
			final String     testHomePageLinkActiveText = testHomePageLinkLabel.getText(); // HTML text 
			final Runnable updateTestHomePageLink = new Runnable() {
				@Override
				public void run() {
					final boolean uploadReplaySiteSelected = customReplaySiteComboBox.getSelectedIndex() >= 0;
					testHomePageLinkLabel.setText( acknowledgeCheckBox.isSelected() && uploadReplaySiteSelected ? testHomePageLinkActiveText : Language.getText( "miscSettings.testHomePageLink" ) );
				}
			};
			final Runnable updateFieldsButtons = new Runnable() {
				@Override
				public void run() {
					final boolean uploadReplaySiteSelected = customReplaySiteComboBox.getSelectedIndex() >= 0;
					displayNameTextField.setEnabled( uploadReplaySiteSelected );
					homePageTextField   .setEnabled( uploadReplaySiteSelected );
					uploadUrlTextField  .setEnabled( uploadReplaySiteSelected );
					deleteButton        .setEnabled( uploadReplaySiteSelected );
					updateTestHomePageLink.run();
				}
			};
			customReplaySiteActionListener = new ActionListener() {
				private ReplayUploadSite previousReplayUploadSite;
				@Override
				public void actionPerformed( final ActionEvent event ) {
					if ( previousReplayUploadSite != null ) {
						previousReplayUploadSite.displayName = displayNameTextField.getText();
						previousReplayUploadSite.homePage    = homePageTextField   .getText();
						previousReplayUploadSite.uploadUrl   = uploadUrlTextField  .getText();
						// Refresh the combo box (display name might have changed)
						( (CustomComboBoxModel< ? >) customReplaySiteComboBox.getModel() ).fireContentsChanged( customReplaySiteComboBox );
					}
					previousReplayUploadSite = (ReplayUploadSite) customReplaySiteComboBox.getSelectedItem();
					displayNameTextField.setText( previousReplayUploadSite == null ? null : previousReplayUploadSite.displayName );
					homePageTextField   .setText( previousReplayUploadSite == null ? null : previousReplayUploadSite.homePage    );
					uploadUrlTextField  .setText( previousReplayUploadSite == null ? null : previousReplayUploadSite.uploadUrl   );
					updateFieldsButtons.run();
				}
			};
			customReplaySiteComboBox.addActionListener( customReplaySiteActionListener );
			
			JPanel wrapper = new JPanel();
			wrapper.add( new JLabel( Language.getText( "miscSettings.selectCustomReplaySite" ) ) );
			wrapper.add( customReplaySiteComboBox );
			editPanel.add( wrapper, BorderLayout.NORTH );
			
			final Box fieldsBox = Box.createVerticalBox();
			Box row = Box.createHorizontalBox();
			row.add( new JLabel( Language.getText( "miscSettings.displayName" ) ) );
			row.add( displayNameTextField );
			fieldsBox.add( row );
			row = Box.createHorizontalBox();
			row.add( new JLabel( Language.getText( "miscSettings.homePage" ) ) );
			row.add( homePageTextField );
			fieldsBox.add( row );
			testHomePageLinkLabel.addMouseListener( new MouseAdapter() {
				public void mouseClicked( final MouseEvent event ) {
					if ( homePageTextField.isEnabled() && homePageTextField.getText().length() > 0 )
						GeneralUtils.showURLInBrowser( homePageTextField.getText() );
				};
			} );
			fieldsBox.add( GuiUtils.createRightAlignedInfoWrapperPanel( testHomePageLinkLabel, 0 ) );
			row = Box.createHorizontalBox();
			row.add( new JLabel( Language.getText( "miscSettings.uploadUrl" ) ) );
			row.add( uploadUrlTextField );
			fieldsBox.add( row );
			final JLabel uploadUrlInfoLabel = new JLabel( Language.getText( "miscSettings.uploadUrlInfo" ), JLabel.RIGHT );
			fieldsBox.add( GuiUtils.createRightAlignedInfoWrapperPanel( uploadUrlInfoLabel, 0 ) );
			editPanel.add( fieldsBox, BorderLayout.CENTER );
			GuiUtils.alignBox( fieldsBox, 2 );
			
			final JPanel buttonsPanel = new JPanel( new GridLayout( 2, 1 ) );
			final JButton addNewButton = new JButton( Icons.DOCUMENT );
			GuiUtils.updateButtonText( addNewButton, "miscSettings.addNewButton" );
			addNewButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					final ReplayUploadSite replayUploadSite = new ReplayUploadSite();
					replayUploadSite.displayName = Language.getText( "miscSettings.customReplaySiteDefaultName", customReplaySiteComboBox.getItemCount() + 1 );
					customReplaySiteComboBox.addItem( replayUploadSite );
					customReplaySiteComboBox.setSelectedIndex( customReplaySiteComboBox.getItemCount() - 1 );
					updateFieldsButtons.run();
				}
			} );
			buttonsPanel.add( addNewButton );
			GuiUtils.updateButtonText( deleteButton, "miscSettings.deleteButton" );
			deleteButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					customReplaySiteComboBox.removeItemAt( customReplaySiteComboBox.getSelectedIndex() );
					updateFieldsButtons.run();
				}
			} );
			buttonsPanel.add( deleteButton );
			editPanel.add( GuiUtils.wrapInPanel( buttonsPanel ), BorderLayout.EAST );
			
			// Load the selected replay upload site (if there's any)
			customReplaySiteActionListener.actionPerformed( null );
			
			final ActionListener acknowledgeActionListener = new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					final boolean acknowledged = acknowledgeCheckBox.isSelected();
					GuiUtils.setComponentTreeEnabled( editPanel, acknowledged );
					if ( acknowledged )
						updateFieldsButtons.run();
					else
						updateTestHomePageLink.run();
				}
			};
			acknowledgeActionListener.actionPerformed( null );
			acknowledgeCheckBox.addActionListener( acknowledgeActionListener );
			panel.add( editPanel, BorderLayout.CENTER );
			
			GuiUtils.addNewTab( SettingsTab.CUSTOM_REPLAY_SITES.text, SettingsTab.CUSTOM_REPLAY_SITES.icon, false, tabbedPane, GuiUtils.wrapInPanel( panel ), null );
			if ( tabToSelect == SettingsTab.CUSTOM_REPLAY_SITES )
				tabbedPane.setSelectedIndex( tabbedPane.getTabCount() - 1 );
		}
		
		getContentPane().add( tabbedPane, BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder( 1, 0, 0, 0, new JLabel().getForeground() ), BorderFactory.createEmptyBorder( 0, 0, 5, 0 ) ) );
		final JButton okButton = new JButton();
		GuiUtils.updateButtonText( okButton, "button.ok" );
		buttonsPanel.add( okButton );
		final JButton okAndRunDiagnosticTestButton = new JButton( Icons.SYSTEM_MONITOR );
		GuiUtils.updateButtonText( okAndRunDiagnosticTestButton, "miscSettings.okAndRunDiagnosticTestButton" );
		buttonsPanel.add( okAndRunDiagnosticTestButton );
		final JButton okAndStoreSettingsButton = new JButton( Icons.SERVER_NETWORK );
		GuiUtils.updateButtonText( okAndStoreSettingsButton, "miscSettings.okAndStoreSettingsButton" );
		buttonsPanel.add( okAndStoreSettingsButton );
		final JButton cancelButton = createCloseButton( "button.cancel" );
		buttonsPanel.add( cancelButton );
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		final ActionListener okActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				for ( final SettingRow[] settingGroup : settingGroups )
					for ( final SettingRow settingRow : settingGroup )
						settingRow.storeSetting();
				GeneralUtils.setToolTipDelays();
				Language.applyDateTimeFormats();
				
				if ( proxyConfigHolder.value != null ) {
					proxyConfigHolder.value.storeSettings();
					// Apply new proxy config
					Sc2gears.applyProxyConfig();
				}
				
				Settings.set( Settings.KEY_SETTINGS_MISC_PLAYER_ALIASES, playerAliasesTextArea.getText() );
				Settings.set( Settings.KEY_SETTINGS_MISC_MAP_ALIASES   , mapAliasesTextArea   .getText() );
				Settings.rebuildAliases();
				
				// Cache the currently displayed pre-defined list
				predefinedListActionListener.actionPerformed( null );
				for ( final Entry< PredefinedList, String > entry : predefinedListCacheMap.entrySet() )
					Settings.set( entry.getKey().settingsKey, entry.getValue() );
				Settings.rebuildPredefinedLists();
				
				for ( final FolderRow folderRow : folderRowList )
					folderRow.storeSetting();
				
				// Store settings from Sc2gears Database tab
				Settings.set( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY, authorizationKeyHolder.value );
				
				// Store the currently displayed custom replay site
				customReplaySiteActionListener.actionPerformed( null );
				Settings.setCustomReplayUploadSiteList( customReplayUploadSiteVector );
				
				// Recommended to restart the replay auto-saver: if file monitor is used, new auto-save folders have to be watched,
				// also detection method might have changed.
				MainFrame.INSTANCE.restartReplayAutoSaver();
				
				dispose();
				
				if ( event.getSource() == okAndRunDiagnosticTestButton )
					new DiagnosticToolDialog();
				else if ( event.getSource() == okAndStoreSettingsButton ) {
					MainFrame.INSTANCE.saveSettingsNow();
					GeneralUtils.storeOtherFile( Settings.SETTINGS_FILE, Language.getText( "miscSettings.settingsFileComment" ), "miscSettings.storingSettings" );
				}
			}
		};
		okButton                    .addActionListener( okActionListener );
		okAndRunDiagnosticTestButton.addActionListener( okActionListener );
		okAndStoreSettingsButton    .addActionListener( okActionListener );
		
		// I want tab titles to start exactly under each other (at one vertical line). By default they are center-aligned... 
		int maxWidth = 0;
		for ( int i = tabbedPane.getTabCount() - 1; i >= 0; i-- )
			maxWidth = Math.max( maxWidth, tabbedPane.getTabComponentAt( i ).getPreferredSize().width );
		for ( int i = tabbedPane.getTabCount() - 1; i >= 0; i-- )
			tabbedPane.getTabComponentAt( i ).setPreferredSize( new Dimension( maxWidth, tabbedPane.getTabComponentAt( i ).getPreferredSize().height ) );
		
		
		packAndShow( cancelButton, true );
	}
	
	/**
	 * Creates and returns a link label which opens the miscellaneous settings dialog as a child of the main frame,
	 * and selects the specified tab.
	 * @param tabToSelect tab to select
	 * @param owner       optional owner frame; it not provided the Main frame will be used
	 * @return a link label which opens the miscellaneous settings dialog and selects the specified tab
	 */
	public static JLabel createLinkLabelToSettings( final SettingsTab tabToSelect ) {
		return createLinkLabelToSettings( tabToSelect, MainFrame.INSTANCE, null );
	}
	
	/**
	 * Creates and returns a link label which opens the miscellaneous settings dialog as a child of the main frame,
	 * selects the Pre-defined lists tab and selects the specified pre-defined list.
	 * @param initialPredefinedList initial pre-definied list to select
	 * @return a link label which opens the miscellaneous settings dialog and selects the Pre-defined lists tab
	 */
	public static JLabel createLinkLabelToPredefinedListsSettings( final PredefinedList initialPredefinedList ) {
		return createLinkLabelToSettings( SettingsTab.PREDEFINED_LISTS, MainFrame.INSTANCE, initialPredefinedList );
	}
	
	/**
	 * Creates and returns a link label which opens the miscellaneous settings dialog and selects the specified tab.
	 * @param tabToSelect tab to select
	 * @param owner       optional owner frame; it not provided the Main frame will be used
	 * @return a link label which opens the miscellaneous settings dialog and selects the specified tab
	 */
	public static JLabel createLinkLabelToSettings( final SettingsTab tabToSelect, final Frame owner ) {
		return createLinkLabelToSettings( tabToSelect, owner, null );
	}
	
	/**
	 * Creates and returns a link label which opens the miscellaneous settings dialog and selects the specified tab.
	 * @param tabToSelect           tab to select
	 * @param owner                 optional owner frame; it not provided the Main frame will be used
	 * @param initialPredefinedList optional initial pre-definied list to select
	 * @return a link label which opens the miscellaneous settings dialog and selects the specified tab
	 */
	public static JLabel createLinkLabelToSettings( final SettingsTab tabToSelect, final Frame owner, final PredefinedList initialPredefinedList ) {
		final JLabel settingsLabel = GeneralUtils.createLinkStyledLabel( Language.getText( "misc.settingsOf", tabToSelect.text ) );
		settingsLabel.setIcon( tabToSelect.icon );
		
		settingsLabel.addMouseListener( new MouseAdapter() {
			public void mousePressed( final MouseEvent event ) {
				new MiscSettingsDialog( owner == null ? MainFrame.INSTANCE : owner, tabToSelect, initialPredefinedList );
			};
		} );
		
		return settingsLabel;
	}
	
}
