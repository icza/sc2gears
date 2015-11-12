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

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.services.Sc2RegMonitor;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.Task;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Always On Top APM display dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class OnTopApmDisplayDialog extends BaseOnTopDialog {
	
	/** Min title font size. */
	private static final int MIN_FONT_SIZE = 8;
	/** Max title font size. */
	private static final int MAX_FONT_SIZE = 70;
	
	/** Timer for the APM display refresh. */
	private final Timer timer = new Timer( "APM display refresher", false );
	
	/** Task to change the font size of the title label (label displaying the current APM).
	 * Parameter: tells if font size has to be increased ({@link Boolean#TRUE}) or decreased ({@link Boolean#FALSE}). */
	private final Task< Boolean > changeFontSizeTask;
	
	/**
	 * Creates a new OnTopApmDisplayDialog.
	 */
	public OnTopApmDisplayDialog() {
		super( null, null, Settings.KEY_ON_TOP_APM_DISPLAY_LOCATION, Settings.KEY_ON_TOP_APM_DISPLAY_LOCKED );
		
		final String defaultApmText = Language.getText( "onTopGameInfo.apm", 1000 ); // Default APM text for 4-digits
		
		titleLabel.setFont( titleLabel.getFont().deriveFont( Font.BOLD, Settings.getInt( Settings.KEY_ON_TOP_APM_DISPLAY_FONT_SIZE ) ) );
		
		changeFontSizeTask = new Task< Boolean >() {
			@Override
			public void execute( final Boolean increase ) {
				int fontSize = titleLabel.getFont().getSize();
				
				if ( increase ) {
					if ( fontSize < MAX_FONT_SIZE )
						fontSize++;
				}
				else {
					if ( fontSize > MIN_FONT_SIZE )
						fontSize--;
				}
				
				Settings.set( Settings.KEY_ON_TOP_APM_DISPLAY_FONT_SIZE, fontSize );
				titleLabel.setFont( titleLabel.getFont().deriveFont( Font.BOLD, fontSize ) );
				// When resizing, it should be resized to be able to hold a 4-digit APM value...
				final String savedText = titleLabel.getText();
				titleLabel.setText( defaultApmText );
				packAndPosition();
				titleLabel.setText( savedText );
			}
		};
		
		addKeyListener( new KeyAdapter() {
			@Override
			public void keyTyped( final KeyEvent event ) {
				final char ch = event.getKeyChar();
				if ( ch =='-' || ch == '+' ) {
					if ( ch == '-' )
						changeFontSizeTask.execute( Boolean.FALSE );
					if ( ch == '+' )
						changeFontSizeTask.execute( Boolean.TRUE );
				}
			}
		} );
		
		titleLabel.setText( defaultApmText );
		packAndPosition();
		
		// Text in case APM value cannot be obtained
		titleLabel.setText( Language.getText( "onTopGameInfo.apm", 0 ) );
		
		timer.scheduleAtFixedRate( new TimerTask() {
			@Override
			public void run() {
				final Integer apm = Sc2RegMonitor.getApm();
				if ( apm != null ) {
					titleLabel.setText( Language.getText( "onTopGameInfo.apm", apm ) );
					
					// If APM alert is enabled and APM is less than the alert level, change the color to RED
					if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_APM_ALERT ) )
						titleLabel.setForeground( apm.intValue() < Settings.getInt( Settings.KEY_SETTINGS_MISC_APM_ALERT_LEVEL ) ? Color.RED : null );
					else
						titleLabel.setForeground( null );
				}
			}
		}, 0, 500 );
		
		setVisible( true );
	}
	
	/**
	 * Closes the APM display dialog.
	 */
	@Override
	protected void internalClose() {
		timer.cancel();
		
		super.internalClose();
		
		if ( INSTANCE == this )
			INSTANCE = null;
	}
	
	/**
	 * Reference to the shared instance handled by the {@link #open()} and {@link #close()} methods.
	 */
	private static OnTopApmDisplayDialog INSTANCE;
	
	/**
	 * Opens an internally managed APM display dialog.
	 * If an internally managed APM display dialog is already opened, it will be closed first.
	 */
	public static synchronized void open() {
		if ( INSTANCE != null )
			close();
		
		INSTANCE = new OnTopApmDisplayDialog();
	}
	
	/**
	 * Tells if the internally managed APM display dialog is opened.
	 * @return true if the internally managed APM display dialog is opened; false otherwise
	 */
	public static synchronized boolean isOpened() {
		return INSTANCE != null;
	}
	
	/**
	 * Closes the internally managed APM display dialog.
	 */
	public static synchronized void close() {
		if ( INSTANCE != null ) {
			INSTANCE.internalClose();
			INSTANCE = null;
		}
	}
	
	@Override
	protected void onBeforePopup( final JPopupMenu popupMenu ) {
		final int fontSize = titleLabel.getFont().getSize();
		
		popupMenu.addSeparator();
		
		final JMenuItem decreaseFontSizeMenuItem = new JMenuItem( Language.getText( "onTopGameInfo.popup.decreaseFontSize" ), Icons.EDIT_SIZE_DOWN );
		if ( fontSize == MIN_FONT_SIZE )
			decreaseFontSizeMenuItem.setEnabled( false );
		popupMenu.add( decreaseFontSizeMenuItem );
		
		final JMenuItem increaseFontSizeMenuItem = new JMenuItem( Language.getText( "onTopGameInfo.popup.increaseFontSize" ), Icons.EDIT_SIZE_UP );
		if ( fontSize == MAX_FONT_SIZE )
			increaseFontSizeMenuItem.setEnabled( false );
		popupMenu.add( increaseFontSizeMenuItem );
		
		final ActionListener changeFontSizeActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				changeFontSizeTask.execute( event.getSource() == increaseFontSizeMenuItem ? Boolean.TRUE : Boolean.FALSE );
			}
		};
		decreaseFontSizeMenuItem.addActionListener( changeFontSizeActionListener );
		increaseFontSizeMenuItem.addActionListener( changeFontSizeActionListener );
	}
	
}
