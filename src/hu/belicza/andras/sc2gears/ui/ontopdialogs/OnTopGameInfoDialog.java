/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.ontopdialogs;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.ProfileCache;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ProfileListener;
import hu.belicza.andras.sc2gearspluginapi.api.profile.IProfile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.EnumSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Always On Top last game info dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class OnTopGameInfoDialog extends BaseOnTopDialog {
	
	/** Timer for the auto-close count down. */
	private final Timer timer = new Timer( "Auto-close down-counter", false );
	/** Count down timer to auto-close.      */
	private int         autoCloseSec;
	
	/**
	 * Creates a new OnTopGameInfoDialog.
	 * @param file replay file to be opened
	 */
	public OnTopGameInfoDialog( final File file ) {
		super( Language.getText( "onTopGameInfo.title", Consts.APPLICATION_NAME ), Icons.INFORMATION_BALLOON, Settings.KEY_ON_TOP_GAME_INFO_LOCATION, Settings.KEY_ON_TOP_GAME_INFO_LOCKED );
		
		// Cached replays are not good: player color is not cached
		final Replay replay = file == null ? null : ReplayFactory.parseReplay( file.getAbsolutePath(), EnumSet.of( ReplayContent.INIT_DATA, ReplayContent.DETAILS, ReplayContent.ATTRIBUTES_EVENTS, ReplayContent.GAME_EVENTS ) );
		
		autoCloseSec = 12;
		
		final JLabel closingInLabel = new JLabel( "", JLabel.RIGHT );
		closingInLabel.setBorder( BorderFactory.createEmptyBorder( 0, 3, 0, 3 ) );
		GuiUtils.changeFontToItalic( closingInLabel );
		
		if ( replay == null ) {
			
			final JLabel errorLabel = new JLabel( Language.getText( file == null ? "onTopGameInfo.noReplay" : "fileChooser.failedToParseRepaly" ), JLabel.CENTER );
			errorLabel.setForeground( Color.RED );
			errorLabel.setFont( errorLabel.getFont().deriveFont( Font.BOLD | Font.ITALIC ) );
			errorLabel.setBorder( BorderFactory.createEmptyBorder( 30, 20, 30, 20 ) );
			getContentPane().add( errorLabel, BorderLayout.CENTER );
			
			getContentPane().add( closingInLabel, BorderLayout.SOUTH );
			
		}
		else {
			
			// The more players in the replay the more time we give by default
			autoCloseSec += replay.details.players.length;
			
			final Box playersBox = Box.createVerticalBox();
			
			ReplayUtils.applyFavoredPlayerListSetting( replay.details );
			
			// This has to be Vector (or a synchronized list) because the profile cache calls back while we might adding new players
			final Vector< Box > infoBoxVector = new Vector< Box >( replay.details.players.length );
			
			Integer lastTeam = null;
			for ( final int i : replay.details.getTeamOrderPlayerIndices() ) {
				final Player player = replay.details.players[ i ];
				
				if ( lastTeam == null )
					lastTeam = player.team;
				else {
					if ( lastTeam.intValue() != player.team ) {
						playersBox.add( GuiUtils.changeFontToBold( new JLabel( "VS." ) ) );
						lastTeam = player.team;
					}
				}
				
				final Box playerBox = Box.createHorizontalBox();
				playerBox.setBorder( BorderFactory.createEmptyBorder( 3, 0, 3, 0 ) );
				
				final Box infoBox = Box.createVerticalBox();
				infoBoxVector.add( infoBox );
				// Name row
				Box row = Box.createHorizontalBox();
				if ( player.isWinner != null )
					row.add( new JLabel( player.isWinner ? Icons.WIN : Icons.LOSS ) );
				row.add( GuiUtils.changeFontToBold( new JLabel( player.playerId.name, GuiUtils.getColorIcon( player.getColor() ), JLabel.LEFT ) ) );
				row.add( new JLabel( "", Icons.getRaceIcon( player.finalRace.isConcrete ? player.finalRace : player.race ), JLabel.LEFT ) );
				infoBox.add( row );
				row.setAlignmentX( 0 );
				// Other info (APM, EAPM, League wins)
				row = Box.createHorizontalBox();
				row.add( new JLabel( Language.getText( "onTopGameInfo.apm" , ReplayUtils.calculatePlayerApm( replay, player ) ) ) );
				row.add( new JLabel( ", " ) );
				row.add( new JLabel( Language.getText( "onTopGameInfo.eapm", ReplayUtils.calculatePlayerEapm( replay, player ) ) ) );
				row.add( new JLabel( ", " ) );
				final JLabel allGamesLabel = new JLabel( Language.getText( "onTopGameInfo.allGames", ".    " ) );
				row.add( allGamesLabel );
				infoBox.add( row );
				row.setAlignmentX( 0 );
				playerBox.add( infoBox );
				
				playerBox.add( Box.createHorizontalStrut( 3 ) );
				playerBox.add( Box.createHorizontalGlue() );
				
				final Box leagueBox = Box.createHorizontalBox();
				final JLabel[] leagueLabels = new JLabel[ 4 ];
				for ( int j = 0; j < 4; j++ ) {
					leagueBox.add( leagueLabels[ j ] = new JLabel( Icons.LEAGUE_LOADING_ICON ) );
					leagueLabels[ j ].setCursor( Cursor.getDefaultCursor() );
				}
				playerBox.add( leagueBox );
				
				// Get profile info
				ProfileCache.queryProfile( player.playerId, new ProfileListener() {
					@Override
					public void profileReady( final IProfile profile, final boolean isAnotherRetrievingInProgress ) {
						GuiUtils.updateLeagueLabels( profile, leagueLabels, isAnotherRetrievingInProgress );
						for ( final Box infoBox : infoBoxVector )
							infoBox.setPreferredSize( null );
						allGamesLabel.setText( Language.getText( "onTopGameInfo.allGames", profile == null ? "-" : profile.getTotalCareerGames() ) );
						allGamesLabel.invalidate();
						playersBox.validate();
						GuiUtils.alignBox( playersBox, 1 );
						packAndPosition();
					}
				}, Settings.getBoolean( Settings.KEY_REP_MISC_ANALYZER_AUTO_RETRIEVE_EXT_PROFILE_INFO ) );
				
				playersBox.add( playerBox );
			}
			GuiUtils.alignBox( playersBox, 1 );
			
			getContentPane().add( GuiUtils.wrapInPanel( playersBox ), BorderLayout.CENTER );
			
			
			final JPanel southPanel = new JPanel( new BorderLayout() );
			southPanel.add( closingInLabel, BorderLayout.CENTER );
			final JButton openInAnalyzerButton = new JButton( Icons.CHART );
			GuiUtils.updateButtonText( openInAnalyzerButton, "onTopGameInfo.openInAnalyzerButton" );
			openInAnalyzerButton.setCursor( Cursor.getDefaultCursor() );
			openInAnalyzerButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					internalClose();
					MainFrame.INSTANCE.restoreMainFrame();
					MainFrame.INSTANCE.openReplayFile( file );
				}
			} );
			southPanel.add( openInAnalyzerButton, BorderLayout.EAST );
			getContentPane().add( southPanel, BorderLayout.SOUTH );
			
		}
		
		closingInLabel.setText( Language.getText( "onTopGameInfo.closingIn", autoCloseSec ) );
		
		packAndPosition();
		
		timer.scheduleAtFixedRate( new TimerTask() {
			@Override
			public void run() {
				autoCloseSec--;
				closingInLabel.setText( Language.getText( "onTopGameInfo.closingIn", autoCloseSec ) );
				if ( autoCloseSec == 0 )
					internalClose();
			}
		}, 1000, 1000 );
		
		// If clicked on, stop timer (and remove text)
		addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				timer.cancel();
				closingInLabel.setText( " " );
				removeMouseListener( this );
			}
		} );
		
		setVisible( true );
	}
	
	/**
	 * Closes the game info dialog.
	 */
	@Override
	protected void internalClose() {
		timer.cancel();
		
		super.internalClose();
		
		if ( INSTANCE == this )
			INSTANCE = null;
	}
	
	/**
	 * Reference to the shared instance handled by the {@link #open(File)} and {@link #close()} methods.
	 */
	private static OnTopGameInfoDialog INSTANCE;
	
	/**
	 * Opens an internally managed game info dialog of the specified replay.<br>
	 * If an internally managed game info dialog is already opened, it will be closed first.
	 * @param file replay file to be opened
	 */
	public static synchronized void open( final File file ) {
		if ( INSTANCE != null )
			close();
		
		INSTANCE = new OnTopGameInfoDialog( file );
	}
	
	/**
	 * Tells if the internally managed game info dialog is opened.
	 * @return true if the internally managed game info dialog is opened; false otherwise
	 */
	public static synchronized boolean isOpened() {
		return INSTANCE != null;
	}
	
	/**
	 * Closes the internally managed game info dialog.
	 */
	public static synchronized void close() {
		if ( INSTANCE != null ) {
			INSTANCE.internalClose();
			INSTANCE = null;
		}
	}
	
}
