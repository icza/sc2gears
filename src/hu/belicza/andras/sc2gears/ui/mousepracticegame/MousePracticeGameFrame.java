/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.mousepracticegame;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.dialogs.BrowserDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog.SettingsTab;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.HttpPost;
import hu.belicza.andras.sc2gears.util.NormalThread;
import hu.belicza.andras.sc2gearsdbapi.TopScoresServletApi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Mouse practice game frame.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class MousePracticeGameFrame extends JFrame {
	
	/** Version of the game (engine). */
	public static final String GAME_VERSION = "1.1";
	
	/** Reference to the only instance. */
	public static volatile MousePracticeGameFrame INSTANCE;
	
	/** Reference to the game controller. */
	private Controller controller;
	
	/**
	 * Creates a new MousePracticeGameFrame.
	 */
	private MousePracticeGameFrame() {
		super( Language.getText( "mousePracticeGame.title" ) );
		
		setIconImage( Icons.MOUSE.getImage() );
		
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing( final WindowEvent event ) {
				close();
			}
		} );
		
		final Box northBox = Box.createVerticalBox();
		northBox.add( Box.createVerticalStrut( 5 ) );
		
		JPanel optionsPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 15, 2 ) );
		final JCheckBox enableCustomRulesCheckBox = GuiUtils.createCheckBox( "mousePracticeGame.enableCustomRules", Settings.KEY_MOUSE_PRACTICE_ENABLE_CUSTOM_RULES );
		enableCustomRulesCheckBox.setToolTipText( Language.getText( "mousePracticeGame.customRulesWarning" ) );
		optionsPanel.add( enableCustomRulesCheckBox );
		optionsPanel.add( MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.MOUSE_GAME_RULES, this ) );
		optionsPanel.add( Box.createHorizontalStrut( 15 ) );
		optionsPanel.add( GeneralUtils.createLinkLabel( Language.getText( "mousePracticeGame.gameAndCustomRulesDescription" ), Consts.URL_MOUSE_PRACTICE_GAME ) );
		northBox.add( optionsPanel );
		
		optionsPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 15, 2 ) );
		final JCheckBox checkUserScoreCheckBox = GuiUtils.createCheckBox( "mousePracticeGame.checkUserScore", Settings.KEY_MOUSE_PRACTICE_CHECK_USER_SCORE );
		checkUserScoreCheckBox.setEnabled( Settings.getString( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY ).length() == 0 );
		optionsPanel.add( checkUserScoreCheckBox );
		final JButton startStopButton = new JButton();
		GuiUtils.updateButtonText( startStopButton, "mousePracticeGame.startButton" );
		optionsPanel.add( startStopButton );
		final JLabel viewTopListLinkLabel = GeneralUtils.createLinkStyledLabel( Language.getText( "mousePracticeGame.viewTopScores" ) );
		viewTopListLinkLabel.addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				final StringBuilder urlBuilder = new StringBuilder( Consts.URL_SC2GEARS_DATABASE_TOP_SCORES_SERVLET );
				urlBuilder
					.append( '?' ).append( TopScoresServletApi.PARAM_PROTOCOL_VERSION ).append( '=' ).append( TopScoresServletApi.PROTOCOL_VERSION_1       )
					.append( '&' ).append( TopScoresServletApi.PARAM_GAME             ).append( '=' ).append( TopScoresServletApi.GAME_MOUSE_PRACTICE      )
					.append( '&' ).append( TopScoresServletApi.PARAM_OPERATION        ).append( '=' ).append( TopScoresServletApi.OPERATION_GET_TOP_SCORES );
				new BrowserDialog( MousePracticeGameFrame.this, "mousePracticeGame.topScoresTitle", Icons.MOUSE, urlBuilder.toString(), new Dimension( 800, 650 ) );
			}
		} );
		optionsPanel.add( viewTopListLinkLabel );
		final JCheckBox colorBlindCheckBox = GuiUtils.createCheckBox( "mousePracticeGame.colorBlind", Settings.KEY_MOUSE_PRACTICE_COLOR_BLIND );
		colorBlindCheckBox.setToolTipText( Language.getText( "mousePracticeGame.colorBlindToolTip" ) );
		optionsPanel.add( colorBlindCheckBox );
		northBox.add( optionsPanel );
		getContentPane().add( northBox, BorderLayout.NORTH );
		
		final View view = new View();
		
		final JPanel viewWrapper = GuiUtils.wrapInPanel( view );
		viewWrapper.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) ); 
		getContentPane().add( viewWrapper, BorderLayout.CENTER );
		
		startStopButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				if ( controller == null ) {
					GuiUtils.updateButtonText( startStopButton, "mousePracticeGame.stopButton" );
					// Start game
    				enableCustomRulesCheckBox.setEnabled( false );
    				controller = new Controller( view, enableCustomRulesCheckBox.isSelected(), new Runnable() {
    					@Override
    					public void run() {
    						// This run() method is called in the AWT dispatching thread...
    						startStopButton.setEnabled( false );
    						final Model model = controller.getModel();
    						boolean checkingScore = false;
    						if ( !controller.enableCustomRules && model.gameOver && model.score > 0 ) {
    							final String authorizationKey = Settings.getString( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY );
    							if ( authorizationKey.length() > 0 || Settings.getBoolean( Settings.KEY_MOUSE_PRACTICE_CHECK_USER_SCORE ) ) {
    								checkingScore = true;
        							// Check user score in a new thread...
        							new NormalThread( "Mouse practice game User score checker" ) {
        								@Override
        					            public void run() {
        									GuiUtils.updateButtonText( startStopButton, "mousePracticeGame.startButtonCheckingScore" );
        									checkAndSubmitScore( model );
        					    			// To ensure proper synchronization, execute in the AWT dispatching thread:
        					    			// (we need this because the game could end at the "same" exact moment when a user would hit the Stop button)
        					    			SwingUtilities.invokeLater( new Runnable() {
        					    				@Override
        					    				public void run() {
                									endGame();
        					    				}
        					    			} );
        					            }
        							}.start();
    							}
    						}
    						controller = null;
    						enableCustomRulesCheckBox.setEnabled( true );
    						if ( !checkingScore )
    							endGame();
    					}
    				} );
    				controller.start();
				}
				else {
					startStopButton.setEnabled( false ); // To prevent clicking on it again...
					controller.requestToCancel();
				}
			}
			private void endGame() {
				GuiUtils.updateButtonText( startStopButton, "mousePracticeGame.startButton" );
				startStopButton.setEnabled( true );
			}
		} );
		
		pack();
		GeneralUtils.centerWindow( this );
		
		setVisible( true );
	}
	
	/**
	 * Checks the user score and submits it if the user score made it to the top scores table
	 * and the user has entered an authorization key.
	 * 
	 * @param model reference to the game model
	 */
	private void checkAndSubmitScore( final Model model ) {
		// Check user score if it made to the top scores table
		final Map< String, String > paramsMap = new HashMap< String, String >();
		paramsMap.put( TopScoresServletApi.PARAM_PROTOCOL_VERSION, TopScoresServletApi.PROTOCOL_VERSION_1    );
		paramsMap.put( TopScoresServletApi.PARAM_GAME            , TopScoresServletApi.GAME_MOUSE_PRACTICE   );
		paramsMap.put( TopScoresServletApi.PARAM_OPERATION       , TopScoresServletApi.OPERATION_CHECK_SCORE );
		paramsMap.put( TopScoresServletApi.PARAM_SCORE           , Integer.toString( model.score )           );
		
		int rank = -1;
		HttpPost httpPost  = null;
		try {
			httpPost = new HttpPost( Consts.URL_SC2GEARS_DATABASE_TOP_SCORES_SERVLET, paramsMap );
			if ( httpPost.connect() )
				if ( httpPost.doPost() ) {
					final List< String > response = httpPost.getResponseLines();
					if ( response != null )
						rank = Integer.parseInt( response.get( 0 ) );
				}
		} catch ( final Exception e ) {
			e.printStackTrace();
		} finally {
			if ( httpPost != null )
				httpPost.close();
		}
		
		if ( rank >= 0 ) {
			// User score made it to the top scores table!
			final String authorizationKey = Settings.getString( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY );
			if ( authorizationKey.length() > 0 ) {
				// Query user name to submit score
				final JTextField userNameTextField = new JTextField( Settings.getString( Settings.KEY_MOUSE_PRACTICE_USER_NAME ) );
				GuiUtils.showInfoDialog(
					new Object[] {
						Language.getText( "mousePracticeGame.topScoreInfo1" ),
						" ",
						Language.getText( "mousePracticeGame.topScoreInfo2", rank ),
						" ",
						Language.getText( "mousePracticeGame.topScoreInfo5" ),
						userNameTextField,
						" "
					}, this );
				
				final String userName = TopScoresServletApi.trimUserName( userNameTextField.getText() );
				Settings.set( Settings.KEY_MOUSE_PRACTICE_USER_NAME, userName );
				
				paramsMap.put( TopScoresServletApi.PARAM_AUTHORIZATION_KEY, authorizationKey                                            );
				paramsMap.put( TopScoresServletApi.PARAM_OPERATION        , TopScoresServletApi.OPERATION_SUBMIT_SCORE                  );
				paramsMap.put( TopScoresServletApi.PARAM_GAME_VERSION     , GAME_VERSION                                                );
				paramsMap.put( TopScoresServletApi.PARAM_USER_NAME        , userName                                                    );
				paramsMap.put( TopScoresServletApi.PARAM_ACCURACY         , Float  .toString( model.accuracySum / model.accuracyCount ) );
				paramsMap.put( TopScoresServletApi.PARAM_HITS             , Integer.toString( model.hits                              ) );
				paramsMap.put( TopScoresServletApi.PARAM_GAME_LENGTH      , Integer.toString( model.ageMs                             ) );
				paramsMap.put( TopScoresServletApi.PARAM_RANDOM_SEED      , Long   .toString( model.startRandomSeed                   ) );
				
				// Submit user score
				httpPost = null; // Clear previous reference
				try {
					httpPost = new HttpPost( Consts.URL_SC2GEARS_DATABASE_TOP_SCORES_SERVLET, paramsMap );
					if ( httpPost.connect() )
						if ( httpPost.doPost() )
							httpPost.getResponse(); // Response has to be read (else the servlet might get terminated)
				} catch ( final Exception e ) {
					e.printStackTrace();
				} finally {
					if ( httpPost != null )
						httpPost.close();
				}
			}
			else {
				// Display a message saying score cannot be submitted
				GuiUtils.showInfoDialog(
					new Object[] {
						Language.getText( "mousePracticeGame.topScoreInfo1" ),
						" ",
						Language.getText( "mousePracticeGame.topScoreInfo2", rank ),
						" ",
						Language.getText( "mousePracticeGame.topScoreInfo3" ),
						Language.getText( "mousePracticeGame.topScoreInfo4" ),
						" ",
						MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.SC2GEARS_DATABASE )
					}, this );
			}
		}
	}
	
	/**
	 * Opens a new Mouse practice game frame if one does not already exist; else brings the existing one to front.
	 */
	public static synchronized void open() {
		final MousePracticeGameFrame instance = INSTANCE;
		
		if ( instance == null )
			INSTANCE = new MousePracticeGameFrame();
		else {
			if ( instance.getExtendedState() == ICONIFIED )
				instance.setExtendedState( NORMAL );
			instance.toFront();
		}
	}
	
	/**
	 * Closes the Mouse practice game frame if there is one opened.
	 */
	public static synchronized void close() {
		final MousePracticeGameFrame instance = INSTANCE;
		
		if ( instance != null ) {
			if ( instance.controller != null )
				instance.controller.requestToCancel();
			
			instance.dispose();
			INSTANCE = null;
		}
	}
	
}
