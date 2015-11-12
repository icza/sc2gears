/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.startpage;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.dialogs.TipsDialog;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.ModuleFrame;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.NormalThread;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * Start page of Sc2gears to display news.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class StartPage extends ModuleFrame {
	
	// TIPS_COUNT is defined here because this class loads every time (if is not disabled), so the class TipsDialog will only be loaded if needed
	/** Number of available tips. */
	public static final int TIPS_COUNT = 58;
	
	/**
	 * Start page channels.
	 * @author Andras Belicza
	 */
	private static enum Channel {
		/** Latest Sc2gears news.          */
		LATEST_SC2GEARS_NEWS         ( "module.startPage.channel.latestSc2gearsNews"       , Consts.URL_LATEST_SC2GEARS_NEWS, Consts.APPLICATION_NAME ),
		/** Sc2gears Database User quota.  */
		SC2GEARS_DATABASE_USER_QUOTA ( "module.startPage.channel.sc2gearsDatabaseUserQuota", null ),
		/** All Sc2gears news.             */
		ALL_SC2GEARS_NEWS            ( "module.startPage.channel.allSc2gearsNews"          , Consts.URL_ALL_SC2GEARS_NEWS   , Consts.APPLICATION_NAME );
		
		/** Cache of the string value. */
		public final String stringValue;
		/** URL of the channel;
		 * <code>null</code> value indicates custom content creation. */
		public final String url;
		
		/**
		 * Creates a new Channel.
		 * @param textKey key of the text representation
		 * @param url     URL of the channel
		 */
		private Channel( final String textKey, final String url, final Object... textArguments ) {
			stringValue = Language.getText( textKey, textArguments );
			this.url    = url;
		}
		
		/**
		 * Returns the custom content of the channel.
		 * @return the custom content of the channel.
		 */
		public String getContent() {
			if ( this == SC2GEARS_DATABASE_USER_QUOTA ) {
				final String authorizationKey = Settings.getString( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY );
				if ( authorizationKey.length() == 0 )
					return "<html><body style='font-family:arial;font-size:10px;font-style:italic;background:#ffffff;'>"
						+ "<p>You did not set an Authorization key. This channel requires a valid Authorization key.</p>"
						+ "</body></html>";
				
				return GeneralUtils.getUserQuotaInfo( authorizationKey );
			}
			return null;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/** The number of the current tip.      */
	private int currentTip;
	
	/** Label displaying tips.              */
	private final JLabel               tipLabel        = new JLabel( Icons.LIGHT_BULB );
	
	/** The channel combo box.              */
	private final JComboBox< Channel > channelComboBox = GuiUtils.createComboBox( Channel.values(), Settings.KEY_START_PAGE_CHANNEL );
	/** Content refresher label.            */
	private final JLabel               refreshLabel    = GeneralUtils.createLinkStyledLabel( Language.getText( "module.startPage.refresh" ) );
	
	/** The browser pane of the Start page. */
	private final JEditorPane          browserPane     = GuiUtils.createEditorPane();
	
	/**
	 * Creates a new StartPage.
	 */
	public StartPage() {
		super( Language.getText( "module.startPage.title" ) );
		
		setFrameIcon( Icons.NEWSPAPER );
		buildGUI();
		
		// Randomly change tip when some events occur
		addInternalFrameListener( new InternalFrameAdapter() {
			@Override
			public void internalFrameActivated( final InternalFrameEvent event ) {
				currentTip = (int) ( Math.random() * TIPS_COUNT ) + 1;
				updateTip();
			}
			@Override
			public void internalFrameOpened( final InternalFrameEvent event ) {
				internalFrameActivated( event );
			}
		} );
	}
	
	/**
	 * Builds the GUI of the frame.
	 */
	private void buildGUI() {
		final Box northBox = Box.createVerticalBox();
		
		final Box tipBox = Box.createHorizontalBox();
		final JLabel previousTipLabel = new JLabel( Icons.ARROW_180 );
		previousTipLabel.setToolTipText( Language.getText( "module.startPage.previousTipToolTip" ) );
		previousTipLabel.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		previousTipLabel.addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				if ( --currentTip < 1 )
					currentTip = TIPS_COUNT;
				updateTip();
			}
		} );
		tipBox.add( previousTipLabel );
		tipBox.add( TipsDialog.createOpenLinkLabel( "module.startPage.tips", false ) );
		final JLabel nextTipLabel = new JLabel( Icons.ARROW );
		nextTipLabel.setToolTipText( Language.getText( "module.startPage.nextTipToolTip" ) );
		nextTipLabel.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		nextTipLabel.addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				if ( ++currentTip > TIPS_COUNT )
					currentTip = 1;
				updateTip();
			}
		} );
		tipBox.add( nextTipLabel );
		tipBox.add( Box.createHorizontalStrut( 10 ) );
		tipBox.add( tipLabel );
		final JPanel tipBoxWrapper = GuiUtils.wrapInPanelLeftAligned( tipBox );
		tipBoxWrapper.setBorder( BorderFactory.createMatteBorder( 0, 0, 1, 0, tipLabel.getForeground() ) );
		northBox.add( tipBoxWrapper );
		
		final Box controlBox = Box.createHorizontalBox();
		controlBox.add( new JLabel( Language.getText( "module.startPage.channel" ) ) );
		channelComboBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				refresh();
			}
		} );
		controlBox.add( channelComboBox );
		controlBox.add( Box.createHorizontalStrut( 5 ) );
		refreshLabel.setIcon( Icons.ARROW_CIRCLE_315 );
		refreshLabel.setToolTipText( Language.getText( "module.startPage.refreshToolTip" ) );
		refreshLabel.addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				refresh();
			}
		} );
		controlBox.add( refreshLabel );
		controlBox.add( Box.createHorizontalStrut( 10 ) );
		final JLabel visitHomePageLabel = GeneralUtils.createLinkLabel( Language.getText( "module.startPage.visitHomePage" ), Consts.URL_HOME_PAGE );
		visitHomePageLabel.setIcon( Icons.HOME_ARROW );
		controlBox.add( visitHomePageLabel );
		controlBox.add( Box.createHorizontalStrut( 10 ) );
		final JLabel visitScelightLabel = GeneralUtils.createLinkLabel( "Visit Scelight™", Consts.URL_SCELIGHT_HOME_PAGE );
		visitScelightLabel.setIcon( Icons.SCELIGHT );
		visitScelightLabel.setToolTipText( "<html>Visit <b>Scelight&trade;</b>, the successor to " + Consts.APPLICATION_NAME + "</html>" );
		controlBox.add( visitScelightLabel );
		controlBox.add( Box.createHorizontalStrut( 20 ) );
		final JLabel openLastReplayLabel = GeneralUtils.createLinkStyledLabel( Language.getText( "module.startPage.openLastReplay" ) );
		openLastReplayLabel.setIcon( Icons.CHART );
		openLastReplayLabel.addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				MainFrame.INSTANCE.openLastReplayFile();
			}
		} );
		controlBox.add( openLastReplayLabel );
		controlBox.add( Box.createHorizontalStrut( 20 ) );
		final JLabel startStarCraft2Label = GeneralUtils.createLinkStyledLabel( Language.getText( "module.startPage.startStarCraft2" ) );
		startStarCraft2Label.setIcon( Icons.SC2 );
		startStarCraft2Label.addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				GeneralUtils.startStarCraftII();
			}
		} );
		controlBox.add( startStarCraft2Label );
		controlBox.add( Box.createHorizontalStrut( 10 ) );
		final JLabel startStarCraft2EditorLabel = GeneralUtils.createLinkStyledLabel( Language.getText( "module.startPage.startStarCraft2Editor" ) );
		startStarCraft2EditorLabel.setIcon( Icons.SC2_EDITOR );
		startStarCraft2EditorLabel.addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				GeneralUtils.startStarCraftIIEditor();
			}
		} );
		controlBox.add( startStarCraft2EditorLabel );
		northBox.add( GuiUtils.wrapInPanelLeftAligned( controlBox ) );
		controlBox.add( Box.createHorizontalStrut( 20 ) );
		controlBox.add( GuiUtils.createOpenUserPageLinkLabel() );
		
		getContentPane().add( northBox, BorderLayout.NORTH );
		
		// Do not yet load the page!
		
		// Remove CTRL+SHIFT+O (it's associated with open last replay)
		// Register F5 to refresh
		Object actionKey;
		getRootPane().getLayeredPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( KeyEvent.VK_F5, 0 ), actionKey = new Object() );
		getRootPane().getLayeredPane().getActionMap().put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				refresh();
			}
		} );
		getContentPane().add( new JScrollPane( browserPane ), BorderLayout.CENTER );
	}
	
	/**
	 * Updates the tip label with the current tip.
	 */
	private void updateTip() {
		tipLabel.setText( Language.getText( "module.startPage.tip." + currentTip ) );
	}
	
	/**
	 * Refreshes the content of the Start page.
	 */
	public void refresh() {
		final String refreshHtmlText = refreshLabel.getText();
		channelComboBox.setEnabled( false );
		refreshLabel   .setEnabled( false );
		refreshLabel   .setText( Language.getText( "module.startPage.refresh" ) );
		// This is called from the event dispatch thread, so we refresh in a new thread
		new NormalThread( "Start page Refresher" ) {
			@Override
			public void run() {
				try {
					browserPane.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
					
					final Channel channel = (Channel) channelComboBox.getSelectedItem();
					if ( channel.url == null ) {
						browserPane.setContentType( "text/html" );
						
						final String content = channel.getContent();
						if ( content == null )
							throw new IOException( "Failed to retrieve channel content." );
						
						browserPane.setText( content );
						return;
					}
					if ( Consts.DEVELOPER_MODE ) {
						final StringBuilder  startPageContentBuilder = new StringBuilder();
						String line;
						try ( final BufferedReader input = new BufferedReader( new FileReader( "../war/hosted/" + channel.url.substring( channel.url.lastIndexOf( '/' ) + 1 ) ) ) ) {
							while ( ( line = input.readLine() ) != null )
								startPageContentBuilder.append( line );
						}
						
						browserPane.setContentType( "text/html" );
						browserPane.setText( startPageContentBuilder.toString() );
					}
					else
						browserPane.setPage( channel.url + "?t=" + System.currentTimeMillis() ); // JEditorPane does not refresh if URL is the same...
				} catch ( final IOException ie ) {
					ie.printStackTrace();
					browserPane.setContentType( "text/html" );
					browserPane.setText( "<html><body style='font-family:arial;font-size:10px;font-style:italic;background:#ffffff;'>"
							+ "<p>The channel is currently unavailable. Please try again later.</p>"
							+ "<p style='font-size:9px;'>If the problem remains, you should check the <a href='" + Consts.URL_HOME_PAGE + "'>home page</a> and download the latest version as the back-end server might have been moved.</p>"
							+ "</body></html>" );
				} finally {
					browserPane.setCursor( null );
					
					refreshLabel   .setText( refreshHtmlText );
					refreshLabel   .setEnabled( true );
					channelComboBox.setEnabled( true );
				}
			}
		}.start();
	}
	
}
