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
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * Bases of Always On Top dialogs in Sc2gears.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
class BaseOnTopDialog extends JDialog {
	
	/** Optional key of the setting storing the location of the dialog. */
	protected final String locationSettingKey;
	
	/** Title panel to function as the title bar. */
	protected JPanel titlePanel = new JPanel( new BorderLayout() );
	
	/** The title label. */
	protected JLabel titleLabel;
	
	/**
	 * Creates a new BaseOnTopDialog.
	 * The created dialog will not be made visible.
	 * 
	 * @param title              title of the dialog
	 * @param icon               optional icon of the dialog
	 * @param locationSettingKey key of the setting storing the location of the dialog
	 * @param lockedSettingKey   key of the setting storing if the dialog is locked
	 */
	public BaseOnTopDialog( final String title, final ImageIcon icon, final String locationSettingKey, final String lockedSettingKey ) {
		
		this.locationSettingKey = locationSettingKey;
		
		setAlwaysOnTop( true );
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter() {
			public void windowClosing( final WindowEvent event ) {
				internalClose();
			}
		} );
		
		setUndecorated( true );
		( (JPanel) getContentPane() ).setBorder( BorderFactory.createEtchedBorder() );
		
		// Assemble title panel
		titleLabel = new JLabel( title, icon, JLabel.LEFT );
		GuiUtils.changeFontToBold( titleLabel );
		titlePanel.add( titleLabel, BorderLayout.WEST );
		
		final JLabel closeLabel = new JLabel( Icons.CROSS );
		closeLabel.setToolTipText( Language.getText( "icon.close.toolTip" ) );
		closeLabel.setEnabled( !Settings.getBoolean( lockedSettingKey ) );
		closeLabel.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		closeLabel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
		closeLabel.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseEntered( final MouseEvent event ) {
				if ( closeLabel.isEnabled() )
					closeLabel.setBorder( BorderFactory.createEtchedBorder() );
			};
			@Override
			public void mouseExited( final MouseEvent event ) {
				if ( closeLabel.isEnabled() )
					closeLabel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
			};
			@Override
			public void mousePressed( final MouseEvent event ) {
				if ( closeLabel.isEnabled() )
					closeLabel.setBorder( BorderFactory.createLoweredBevelBorder() );
			}
			@Override
			public void mouseClicked( final MouseEvent event ) {
				if ( closeLabel.isEnabled() )
					internalClose();
			}
		} );
		titlePanel.add( closeLabel, BorderLayout.EAST );
		
		getContentPane().add( titlePanel, BorderLayout.NORTH );
		
		// Add support for mouse dragging
		final MouseAdapter mouseAdapter = new MouseAdapter() {
			private boolean dragging;
			private boolean locked = Settings.getBoolean( lockedSettingKey );
			private int refX;
			private int refY;
			{
				setMouseCursor();
			}
			private void setMouseCursor() {
				getLayeredPane().setCursor( locked ? null : Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR ) );
			}
			@Override
			public void mousePressed( final MouseEvent event ) {
				// Always On Top property sometimes loses effect due to modal dialogs... 
				setAlwaysOnTop( false );
				setAlwaysOnTop( true );
				
				if ( event.getButton() == GuiUtils.MOUSE_BUTTON_LEFT ) {
					
					if ( !locked ) {
						refX     = event.getX();
						refY     = event.getY();
						dragging = true;
					}
					
				} else if ( event.getButton() == GuiUtils.MOUSE_BUTTON_RIGHT ) {
					
					final JPopupMenu popupMenu = new JPopupMenu();
					popupMenu.setCursor( Cursor.getDefaultCursor() );
					final JCheckBoxMenuItem lockMenuItem =  new JCheckBoxMenuItem( Language.getText( "general.lock" ), locked ? Icons.LOCK_UNLOCK : Icons.LOCK, locked );
					lockMenuItem.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed( final ActionEvent event ) {
							locked = lockMenuItem.isSelected();
							closeLabel.setEnabled( !locked );
							Settings.set( lockedSettingKey, locked );
							setMouseCursor();
						}
					} );
					popupMenu.add( lockMenuItem );
					popupMenu.addSeparator();
					final JMenuItem closeMenuItem =  new JMenuItem( Language.getText( "general.close" ), Icons.CROSS );
					closeMenuItem.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed( final ActionEvent event ) {
							internalClose();
						}
					} );
					popupMenu.add( closeMenuItem );
					onBeforePopup( popupMenu );
					popupMenu.show( BaseOnTopDialog.this, event.getX(), event.getY() );
					
				}
			}
			@Override
			public void mouseDragged( final MouseEvent event ) {
				if ( dragging ) {
					final Point location = getLocation();
					location.x += event.getX() - refX;
					location.y += event.getY() - refY;
					setLocation( location );
					Settings.set( locationSettingKey, ( location.x + ( getWidth() >> 1 ) ) + "," + ( location.y + ( getHeight() >> 1 ) ) );
				}
			}
			@Override
			public void mouseReleased( final MouseEvent event ) {
				if ( event.getButton() == GuiUtils.MOUSE_BUTTON_LEFT )
					dragging = false;
			}
		};
		addMouseListener      ( mouseAdapter );
		addMouseMotionListener( mouseAdapter );
	}
	
	/**
	 * Closes the dialog.
	 */
	protected void internalClose() {
		dispose();
	}
	
	/**
	 * Packs and positions the dialog to its saved location.
	 */
	public void packAndPosition() {
		pack();
		
		String[] coordsString = Settings.getString( locationSettingKey ).split( "," );
		if ( coordsString.length != 2 )
			coordsString = Settings.getDefaultString( locationSettingKey ).split( "," );
		
		final int cx = Integer.parseInt( coordsString[ 0 ] );
		final int cy = Integer.parseInt( coordsString[ 1 ] );
		
		setLocation( cx - ( getWidth() >> 1 ), cy - ( getHeight() >> 1 ) );
		
		ensureVisibleBounds();
	}
	
	/**
	 * Ensures that all parts of the dialog are visible (inside the screen).<br>
	 * If some parts of the dialog would fall outside of the screen, the dialog will be moved to still just fit in.
	 */
	private void ensureVisibleBounds() {
		final Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
		final Rectangle bounds       = getBounds();
		
		boolean moved = false;
		
		// Condition check order: ensure that the top right corner is visible (the close button)
		// even if the dialog has bigger size than the screen
		
		if ( bounds.x < screenBounds.x ) {
			bounds.x = screenBounds.x;
			moved = true;
		}
		
		if ( bounds.x + bounds.width > screenBounds.x + screenBounds.width ) {
			bounds.x = screenBounds.x + screenBounds.width - bounds.width;
			moved = true;
		}
		
		if ( bounds.y + bounds.height > screenBounds.y + screenBounds.height ) {
			bounds.y = screenBounds.y + screenBounds.height - bounds.height;
			moved = true;
		}
		
		if ( bounds.y < screenBounds.y ) {
			bounds.y = screenBounds.y;
			moved = true;
		}
		
		if ( moved )
			setBounds( bounds );
	}
	
	/**
	 * Called before a popup menu of the On-Top dialog is displayed.
	 * 
	 * <p>Sub-classes can override this method to add custom menu items.</p>
	 * 
	 * <p>This implementation does nothing.</p>
	 * 
	 * @param popupMenu reference to the popup menu about to be displayed
	 */
	protected void onBeforePopup( final JPopupMenu popupMenu ) {
	}
	
}
