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
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.shared.SharedUtils;
import hu.belicza.andras.sc2gears.sound.Sounds;
import hu.belicza.andras.sc2gears.sound.Sounds.VoiceDescription;
import hu.belicza.andras.sc2gears.ui.components.BaseLabelListCellRenderer;
import hu.belicza.andras.sc2gears.ui.icons.Icons;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This is the welcome frame.
 * 
 * <p>Shows a welcome message, and offers a language selection.</p>
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class WelcomeFrame extends JFrame {
	
	private final JLabel welcomeLabel  = new JLabel();
	private final JLabel firstRunLabel = new JLabel();
	private final JLabel chooseLabel   = new JLabel();
	private final JLabel thankYouLabel = new JLabel();
	private final JLabel languageLabel = new JLabel();
	private final JLabel voiceLabel    = new JLabel();
	
	private final JButton okButton     = new JButton();
	private final JButton cancelButton = new JButton();
	
	/**
	 * Creates a new WelcomeFrame and makes it visible.
	 */
	public WelcomeFrame() {
		setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter() {
			public void windowClosing( final WindowEvent event ) {
				cancel();
			};
		} );
		
		setIconImage( Icons.SC2GEARS.getImage() );
		
		final Box box = Box.createVerticalBox();
		box.add( Box.createVerticalStrut( 5 ) );
		box.add( GuiUtils.wrapInPanel( SharedUtils.createAnimatedLogoLabel() ) );
		box.add( Box.createVerticalStrut( 10 ) );
		GuiUtils.changeFontToBold( welcomeLabel );
		box.add( Box.createVerticalStrut( 15 ) );
		box.add( GuiUtils.wrapInPanel( welcomeLabel ) );
		box.add( Box.createVerticalStrut( 15 ) );
		box.add( GuiUtils.wrapInPanel( firstRunLabel ) );
		box.add( GuiUtils.wrapInPanel( chooseLabel ) );
		box.add( Box.createVerticalStrut( 15 ) );
		box.add( GuiUtils.wrapInPanel( thankYouLabel ) );
		box.add( Box.createVerticalStrut( 15 ) );
		
		final JPanel languagePanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 0, 1 ) );
		languagePanel.add( languageLabel );
		final JComboBox< String > languagesComboBox = new JComboBox<>( Language.getAvailableLanguages() );
		languagesComboBox.setMaximumRowCount( languagesComboBox.getModel().getSize() ); // Display all languages
		languagesComboBox.setRenderer( new BaseLabelListCellRenderer< String >() {
			@Override
			public Icon getIcon( final String value ) {
				return Icons.getLanguageIcon( value );
			}
		} );
		languagesComboBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final String language = (String) languagesComboBox.getSelectedItem();
				Language.loadAndActivateLanguage( language );
				
				reassignTexts();
			}
		} );
		languagePanel.add( languagesComboBox );
		box.add( languagePanel );
		
		final JPanel voicePanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 0, 1 ) );
		voicePanel.add( voiceLabel );
		final JComboBox< VoiceDescription > voiceComboBox = new JComboBox<>( Sounds.VOICE_DESCRIPTIONS );
		voiceComboBox.setMaximumRowCount( 15 ); // Not too many languages, display them all
		voiceComboBox.setRenderer( new BaseLabelListCellRenderer< VoiceDescription >() {
			@Override
			public Icon getIcon( final VoiceDescription value ) {
				return Icons.getLanguageIcon( value.language );
			}
		} );
		voiceComboBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final VoiceDescription voiceDescription = (VoiceDescription) voiceComboBox.getSelectedItem();
				Settings.set( Settings.KEY_SETTINGS_VOICE, voiceDescription.name );
				Sounds.playSoundSample( Sounds.SAMPLE_WELCOME, false );
			}
		} );
		voicePanel.add( voiceComboBox );
		box.add( voicePanel );
		
		int maxWidth = Math.max( languagesComboBox.getPreferredSize().width, voiceComboBox.getPreferredSize().width );
		maxWidth += 5;
		languagesComboBox.setPreferredSize( new Dimension( maxWidth, languagesComboBox.getPreferredSize().height ) );
		voiceComboBox    .setPreferredSize( new Dimension( maxWidth, voiceComboBox    .getPreferredSize().height ) );
		
		box.add( Box.createVerticalStrut( 15 ) );
		
		final JPanel buttonsPanel = new JPanel();
		okButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				dispose();
				Settings.set( Settings.KEY_SETTINGS_LANGUAGE, (String) languagesComboBox.getSelectedItem() );
				Settings.saveProperties();
				synchronized ( WelcomeFrame.this ) {
					Sounds.playSoundSample( Sounds.SAMPLE_THANK_YOU, false );
					WelcomeFrame.this.notify(); // Notify the main thread to continue starting the application
				}
			}
		} );
		buttonsPanel.add( okButton );
		cancelButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				cancel();
			}
		} );
		buttonsPanel.add( cancelButton );
		box.add( buttonsPanel );
		
		box.add( Box.createVerticalStrut( 15 ) );
		
		final JPanel panel = new JPanel();
		panel.add( Box.createHorizontalStrut( 15 ) );
		panel.add( box );
		panel.add( Box.createHorizontalStrut( 15 ) );
		getContentPane().add( panel );
		
		setResizable( false );
		
		reassignTexts();
		setVisible( true );
		
		okButton.requestFocusInWindow();
		Sounds.playSoundSample( Sounds.SAMPLE_WELCOME, false );
	}
	
	/**
	 * Gets current text values and reassigns them to the GUI components.
	 */
	private void reassignTexts() {
		setTitle( Language.getText( "welcome.welcome" ) + " - " + Consts.APPLICATION_NAME );
		
		welcomeLabel .setText( Language.getText( "welcome.welcome" ) + '!' );
		firstRunLabel.setText( Language.getText( "welcome.firstRun", Consts.APPLICATION_NAME + "™" ) );
		chooseLabel  .setText( Language.getText( "welcome.selectLanguage" ) );
		thankYouLabel.setText( Language.getText( "welcome.thankYou", Consts.APPLICATION_NAME + "™" ) );
		languageLabel.setText( Language.getText( "welcome.language" ) );
		voiceLabel   .setText( Language.getText( "welcome.voice" ) );
		
		languageLabel.setPreferredSize( null );
		voiceLabel   .setPreferredSize( null );
		final int maxWidth = Math.max( languageLabel.getPreferredSize().width, voiceLabel.getPreferredSize().width );
		languageLabel.setPreferredSize( new Dimension( maxWidth, languageLabel.getPreferredSize().height ) );
		voiceLabel   .setPreferredSize( new Dimension( maxWidth, voiceLabel   .getPreferredSize().height ) );
		
		GuiUtils.updateButtonText( okButton    , "button.ok"     );
		GuiUtils.updateButtonText( cancelButton, "button.cancel" );
		
		pack();
		SharedUtils.centerWindow( this );
	}
	
	/**
	 * Cancels the language selection.
	 */
	private void cancel() {
		dispose();
		// Cancel was pressed, revert to the default language
		Language.loadAndActivateLanguage( Settings.DEFAULT_APP_LANGUAGE );
		Settings.remove( Settings.KEY_SETTINGS_VOICE );
		
		synchronized ( WelcomeFrame.this ) {
			Sounds.playSoundSample( Sounds.SAMPLE_THANK_YOU, false );
			WelcomeFrame.this.notify(); // Notify the main thread to continue starting the application
		}
	}
	
}
