/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.thesoundofvictoryplugin;

import hu.belicza.andras.sc2gearspluginapi.Configurable;
import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.PluginDescriptor;
import hu.belicza.andras.sc2gearspluginapi.PluginServices;
import hu.belicza.andras.sc2gearspluginapi.SettingsControl;
import hu.belicza.andras.sc2gearspluginapi.api.LanguageApi;
import hu.belicza.andras.sc2gearspluginapi.api.SettingsApi;
import hu.belicza.andras.sc2gearspluginapi.api.listener.DiagnosticTestFactory;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayAutosaveListener;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayer;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IReplay;
import hu.belicza.andras.sc2gearspluginapi.impl.BasePlugin;
import hu.belicza.andras.sc2gearspluginapi.impl.BaseSettingsControl;
import hu.belicza.andras.sc2gearspluginapi.impl.DiagnosticTest;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A plugin that plays an audio file after each of the user's games based on the match result (win or loss).
 * The user can choose his/her own Victory and Defeat audio files.
 * 
 * <p>The plugin also registers a diagnostic test to check the pre-conditions of the plugin.</p>
 * 
 * @author Andras Belicza
 */
public class TheSoundOfVictoryPlugin extends BasePlugin implements Configurable {
	
	/** Key of the Victory audio file setting. */
	private static final String KEY_AUDIO_FILE_VICTORY = "audioFile.victory";
	/** Key of the Defeat audio file setting.  */
	private static final String KEY_AUDIO_FILE_DEFEAT  = "audioFile.defeat";
	/** Key of watched players setting.        */
	private static final String KEY_WATCHED_PLAYERS    = "watchedPlayers";
	
	/** A lazily loaded icon for the "Test" button.        */
	private static ImageIcon ICON_CONTROL;
	/** A lazily loaded icon for the "Select file" button. */
	private static ImageIcon ICON_FOLDER_OPEN;
	
	/** Reference to our replay auto-save listener.        */
	private ReplayAutosaveListener replayAutosaveListener;
	
	/** Reference to our diagnostic test factory. */
	private DiagnosticTestFactory  diagnosticTestFactory;
	
	/** Set containing the full names of the watched players.
	 * This is a cache of the setting so we can faster detect watched players. */
	private final Set< String >    watchedPlayerSet = new HashSet< String >( 4 );
	
	
	@Override
	public void init( final PluginDescriptor pluginDescriptor, final PluginServices pluginServices, final GeneralServices generalServices ) {
		// Call the init() implementation of the BasePlugin:
		super.init( pluginDescriptor, pluginServices, generalServices );
		
		// Load saved settings
		assembleWatchedPlayerSet();
		
		// Register a diagnostic test to check the pre-conditions of this plugin:
		generalServices.getCallbackApi().addDiagnosticTestFactory( diagnosticTestFactory = new DiagnosticTestFactory() {
			@Override
			public DiagnosticTest createDiagnosticTest() {
				return new DiagnosticTest( generalServices.getLanguageApi().getText( "tsovplugin.diagnosticTest.name" ) ) {
					@Override
					public void execute() {
						if ( isActionRequired() ) {
							result  = Result.WARNING;
							details = generalServices.getLanguageApi().getText( "tsovplugin.diagnosticTest.warning.missingSettings" );
						}
						else if ( !generalServices.getInfoApi().isReplayAutoSaveEnabled() ) {
							result  = Result.WARNING;
							details = generalServices.getLanguageApi().getText( "tsovplugin.diagnosticTest.warning.autoSaveDisabled" );
						}
						else {
							result  = Result.OK;
						}
					}
				};
			}
		} );
		
		// Register a replay auto-save listener
		generalServices.getCallbackApi().addReplayAutosaveListener( replayAutosaveListener = new ReplayAutosaveListener() {
			@Override
            public void replayAutosaved( File autosavedReplayFile, File originalReplayFile ) {
				// Respect the user's setting about voice notifications:
				if ( !generalServices.getInfoApi().isVoiceNotificationsEnabled() )
					return;
				
				// A quick check if there are watched players:
				if ( watchedPlayerSet.isEmpty() )
					return;
				
				// Cached info is enough for us: we acquire replay with ReplayFactory.getReplay()
				final IReplay replay = generalServices.getReplayFactoryApi().getReplay( autosavedReplayFile.getAbsolutePath(), null );
				if ( replay == null )
					return; // Failed to parse replay
				
				boolean victory = false;
				boolean defeat  = false;
				for ( final IPlayer player : replay.getPlayers() )
					if ( watchedPlayerSet.contains( player.getPlayerId().getFullName() ) )
						if ( player.isWinner() != null ) {
							if ( player.isWinner() ) {
								victory = true;
								break; // One winner is enough...
							}
							else
								defeat  = true; // Winner has higher priority, so don't stop looking...
						}
				
				// Winners have higher priority: if we found a winner, play the Victory audio file
				// If no winner and we found a loser, play the Defeat audio file
				try {
					final String audioFile;
					if ( victory )
						audioFile = pluginServices.getSettingsApi().getString( KEY_AUDIO_FILE_VICTORY );
					else if ( defeat )
						audioFile = pluginServices.getSettingsApi().getString( KEY_AUDIO_FILE_DEFEAT  );
					else
						audioFile = null;
					
					if ( audioFile != null )
						generalServices.getSoundsApi().playSound( new FileInputStream( audioFile ), false );
				} catch ( final IOException ie ) {
					ie.printStackTrace();
				}
			}
		} );
	}
	
	/**
	 * Assembles the set of full names of the watched players from the stored setting.
	 */
	private void assembleWatchedPlayerSet() {
		watchedPlayerSet.clear();
		
		final String watchedPlayersString = pluginServices.getSettingsApi().getString( KEY_WATCHED_PLAYERS );
		if ( watchedPlayersString == null )
			return; // No players have been added to watch
		
		for ( String watchedPlayer : watchedPlayersString.split( "," ) ) {
			watchedPlayer = watchedPlayer.trim();
			if ( watchedPlayer.length() > 0 )
				watchedPlayerSet.add( watchedPlayer );
		}
	}
	
	@Override
	public void destroy() {
		// Remove our diagnostic test factory
		generalServices.getCallbackApi().removeDiagnosticTestFactory ( diagnosticTestFactory  );
		// Remove our replay auto-save listener
		generalServices.getCallbackApi().removeReplayAutosaveListener( replayAutosaveListener );
	}
	
	@Override
	public boolean isActionRequired() {
		// Action is NOT required if watched players is provided and at least one of the victory or defeat audio files is set
		return isStringSettingMissing( KEY_WATCHED_PLAYERS )
			|| isStringSettingMissing( KEY_AUDIO_FILE_VICTORY ) && isStringSettingMissing( KEY_AUDIO_FILE_DEFEAT );
	}
	
	/**
	 * Checks if the string setting specified by its key is missing.
	 * A string setting is missing if its value is null or its length is zero.
	 * @param key key of the setting to check
	 * @return true if the setting specified by its key is missing; false otherwise
	 */
	private boolean isStringSettingMissing( final String key ) {
		final String value = pluginServices.getSettingsApi().getString( key );
		return value == null || value.length() == 0;
	}
	
	@Override
	public SettingsControl getSettingsControl() {
		return new BaseSettingsControl() {
			/** Store the settings API reference. */
			private final SettingsApi settings = pluginServices.getSettingsApi();
			/** Store the language API reference. */
			private final LanguageApi language = generalServices.getLanguageApi();
			
			/** Text field to view/edit watched players.        */
			private final JTextField watchedPlayersTextField   = new JTextField( settings.getString( KEY_WATCHED_PLAYERS    ) );
			/** Text field to view/edit the Victory audio file. */
			private final JTextField victoryAudioFileTextField = new JTextField( settings.getString( KEY_AUDIO_FILE_VICTORY ) );
			/** Text field to view/edit the Defeat audio file.  */
			private final JTextField defeatAudioFileTextField  = new JTextField( settings.getString( KEY_AUDIO_FILE_DEFEAT  ) );
			
			@Override
			public Container getEditorPanel() {
				final JPanel panel = new JPanel( new BorderLayout() );
				
				final JPanel infoPanel = new JPanel( new GridLayout( 2, 1, 0, 3 ) );
				infoPanel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 10, 0 ) );
				infoPanel.add( new JLabel( language.getText( "miscSettings.aliasInfo3" ) ) );
				infoPanel.add( new JLabel( language.getText( "tsovplugin.supportedAudioFormats" ) ) );
				panel.add( infoPanel, BorderLayout.NORTH );
				
				final Box box = Box.createVerticalBox();
				
				Box row = Box.createHorizontalBox();
				row.add( new JLabel( language.getText( "tsovplugin.watchedPlayerList" ) ) );
				watchedPlayersTextField.setToolTipText( language.getText( "tsovplugin.watchedPlayerListToolTip" ) );
				row.add( watchedPlayersTextField );
				row.add( new JLabel() );
				row.add( new JLabel() );
				box.add( row );
				
				row = Box.createHorizontalBox();
				row.add( new JLabel( language.getText( "tsovplugin.victoryAudioFile" ) ) );
				row.add( victoryAudioFileTextField );
				row.add( createFileChooserButton( victoryAudioFileTextField ) );
				row.add( createTestAudioButton  ( victoryAudioFileTextField ) );
				box.add( row );
				
				row = Box.createHorizontalBox();
				row.add( new JLabel( language.getText( "tsovplugin.defeatAudioFile" ) ) );
				row.add( defeatAudioFileTextField );
				row.add( createFileChooserButton( defeatAudioFileTextField ) );
				row.add( createTestAudioButton  ( defeatAudioFileTextField ) );
				box.add( row );
				
				generalServices.getGuiUtilsApi().alignBox( box, 4 );
				
				panel.add( box, BorderLayout.CENTER );
				
				return panel;
			}
			
			/**
			 * Creates a file chooser button associated with a text field.
			 * @param textField text field to get from and set to the chosen folder
			 * @return a folder chooser button associated with the specified text field
			 */
			private JButton createFileChooserButton( final JTextField textField ) {
				if ( ICON_FOLDER_OPEN == null )
					ICON_FOLDER_OPEN = new ImageIcon( TheSoundOfVictoryPlugin.class.getResource( "folder-open.png" ) );
				
				final JButton button = new JButton( language.getText( "tsovplugin.selectFileButton" ), ICON_FOLDER_OPEN );
				button.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( ActionEvent event ) {
						final JFileChooser fileChooser = new JFileChooser( textField.getText() );
						fileChooser.setFileFilter( new FileNameExtensionFilter( language.getText( "fileChooser.audioFiles" ), "mp3", "wav", "aiff", "aif", "au" ) );
						fileChooser.setDialogTitle( generalServices.getLanguageApi().getText( "tsovplugin.selectAudioFile" ) );
						if ( fileChooser.showOpenDialog( settingsDialog ) == JFileChooser.APPROVE_OPTION )
							textField.setText( fileChooser.getSelectedFile().getAbsolutePath() );
					}
				} );
				return button;
			}
			
			/**
			 * Creates a test audio button associated with a text field.
			 * @param textField text field to get the name of the audio file from
			 * @return a test audio button associated with the specified text field
			 */
			private JButton createTestAudioButton( final JTextField textField ) {
				if ( ICON_CONTROL == null )
					ICON_CONTROL = new ImageIcon( TheSoundOfVictoryPlugin.class.getResource( "control.png" ) );
				
				final JButton button = new JButton( language.getText( "tsovplugin.testButton" ), ICON_CONTROL );
				button.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( ActionEvent event ) {
						try {
							generalServices.getSoundsApi().playSound( new FileInputStream( textField.getText() ), false );
						} catch ( final IOException ie ) {
						}
					}
				} );
				return button;
			}
			
			@Override
			public void onOkButtonPressed() {
				settings.set( KEY_WATCHED_PLAYERS   , watchedPlayersTextField  .getText() );
				settings.set( KEY_AUDIO_FILE_VICTORY, victoryAudioFileTextField.getText() );
				settings.set( KEY_AUDIO_FILE_DEFEAT , defeatAudioFileTextField .getText() );
				
				// Rebuild our watched player set cache
				assembleWatchedPlayerSet();
			}
		};
	}
	
}
