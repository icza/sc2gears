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
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.components.CustomComboBoxModel;
import hu.belicza.andras.sc2gears.ui.components.ReplayInfoBox;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog.SettingsTab;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gears.util.TemplateEngine;
import hu.belicza.andras.sc2gears.util.TemplateEngine.Symbol;
import hu.belicza.andras.sc2gearspluginapi.api.enums.League;
import hu.belicza.andras.sc2gearspluginapi.api.profile.IProfile;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;
import hu.belicza.andras.smpd.SmpdUtil;
import hu.belicza.andras.util.BnetUtils.Profile;
import hu.belicza.andras.util.BnetUtils.Profile.BestTeamRank;
import hu.belicza.andras.util.BnetUtils.Profile.TeamRank;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ComboBoxEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileView;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;

/**
 * Utility methods related to GUI.
 * 
 * @author Andras Belicza
 */
public class GuiUtils {
	
	/**
	 * No need to instantiate this class.
	 */
	private GuiUtils() {
	}
	
	/** Constant for the left mouse button mask. */
	public static final int MOUSE_BUTTON_LEFT   = MouseEvent.BUTTON1;
	/** Constant for the right mouse button mask. */
	public static final int MOUSE_BUTTON_RIGHT  = MouseEvent.BUTTON3;
	/** Constant for the middle mouse button mask. */
	public static final int MOUSE_BUTTON_MIDDLE = MouseEvent.BUTTON2;
	
	/** StarCraft 2 replay file filter.<br>
	 * We cannot use final here, because Language is not yet initialized. */
	public static FileFilter SC2_REPLAY_FILTER;
	/** StarCraft 2 replay source filter.<br>
	 * We cannot use final here, because Language is not yet initialized. */
	public static FileFilter SC2_REPLAY_SOURCE_FILE_FILTER;
	/** StarCraft 2 replay list filter.<br>
	 * We cannot use final here, because Language is not yet initialized. */
	public static FileFilter SC2_REPLAY_LIST_FILE_FILTER;
	/** Zip file filter.<br>
	 * We cannot use final here, because Language is not yet initialized. */
	public static FileFilter ZIP_FILE_FILTER;
	/** Text file filter.<br>
	 * We cannot use final here, because Language is not yet initialized. */
	public static FileFilter TEXT_FILE_FILTER;
	/** Mouse print data file filter.<br>
	 * We cannot use final here, because Language is not yet initialized. */
	public static FileFilter MOUSE_PRINT_DATA_FILE_FILTER;
	/** Search filter list filter.<br>
	 * We cannot use final here, because Language is not yet initialized. */
	public static FileFilter SEARCH_FILTER_FILE_FILTER;
	
	/**
	 * Initializes the file filters.<br>
	 * This is a deferred initialization because file filters use the Language manager which is only initialized later.
	 */
	public static void initFileFilters() {
		SC2_REPLAY_FILTER             = new FileNameExtensionFilter( Language.getText( "fileChooser.sc2ReplayFiles"      ), "SC2Replay" );
		SC2_REPLAY_SOURCE_FILE_FILTER = new FileNameExtensionFilter( Language.getText( "fileChooser.sc2ReplaySources"    ), Consts.EXT_SC2REPLAY_SOURCE.substring( 1 ) );
		SC2_REPLAY_LIST_FILE_FILTER   = new FileNameExtensionFilter( Language.getText( "fileChooser.sc2ReplayLists"      ), Consts.EXT_SC2REPLAY_LIST.substring( 1 ) );
		ZIP_FILE_FILTER               = new FileNameExtensionFilter( Language.getText( "fileChooser.zipFiles"            ), "zip" );
		TEXT_FILE_FILTER              = new FileNameExtensionFilter( Language.getText( "fileChooser.textFiles"           ), "txt" );
		MOUSE_PRINT_DATA_FILE_FILTER  = new FileNameExtensionFilter( Language.getText( "fileChooser.mousePrintDataFiles" ), SmpdUtil.SMPD_FILE_EXTENSION );
		SEARCH_FILTER_FILE_FILTER     = new FileNameExtensionFilter( Language.getText( "fileChooser.searchFilterFiles"   ), Consts.EXT_SEARCH_FILTER.substring( 1 ) );
	}
	
	/** Sc2gears file view object which associates our icons to our file types. */
	public static final FileView SC2GEARS_FILE_VIEW = new FileView() {
		@Override
		public Icon getIcon( final File file ) {
			if ( file.isDirectory() )
				return null; // Default value will be used
			
			if ( SC2_REPLAY_FILTER.accept( file ) )
				return Icons.SC2;
			else if ( SC2_REPLAY_SOURCE_FILE_FILTER.accept( file ) )
				return Icons.FOLDERS_STACK;
			else if ( SC2_REPLAY_LIST_FILE_FILTER.accept( file ) )
				return Icons.TABLE_EXCEL;
			else if ( ZIP_FILE_FILTER.accept( file ) )
				return Icons.DOCUMENT_ZIPPER;
			else if ( MOUSE_PRINT_DATA_FILE_FILTER.accept( file ) )
				return Icons.FINGERPRINT;
			else if ( SEARCH_FILTER_FILE_FILTER.accept( file ) )
				return Icons.EDIT_COLUMN;
			
			return null; // Default value will be used
		}
	};
	
	/**
	 * Updates/sets the text of an abstract button based on the key of its text.
	 * 
	 * <p>The button's text will be taken from the {@link Language} specified by the <code>textKey</code>.<br>
	 * If the text of the button designates a mnemonic, it will be set properly.</p>
	 * 
	 * @param button  abstract button whose text to be updated
	 * @param textKey key of the button's text
	 */
	public static void updateButtonText( final AbstractButton button, final String textKey, Object... arguments ) {
		final Pair< String, Character > textAndMnemonic = Language.getTextAndMnemonic( textKey, arguments );
		
		button.setText( textAndMnemonic.value1 );
		if ( textAndMnemonic.value2 != null )
			button.setMnemonic( textAndMnemonic.value2 );
	}
	
	/**
	 * Wraps a component in a JPanel.
	 * @param component component to be wrapped
	 * @return a JPanel with the specified component added to it
	 */
	public static JPanel wrapInPanel( final JComponent component ) {
		final JPanel panel = new JPanel();
		panel.add( component );
		return panel;
	}
	
	/**
	 * Wraps a component in a left-aligned JPanel.
	 * @param component component to be wrapped
	 * @return a left-aligned JPanel with the specified component added to it
	 */
	public static JPanel wrapInPanelLeftAligned( final JComponent component ) {
		final JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		panel.add( component );
		return panel;
	}
	
	/**
	 * Wraps a component in a JPanel with {@link BorderLayout}.
	 * @param component component to be wrapped
	 * @return a JPanel with the specified component added to it
	 */
	public static JPanel wrapInBorderPanel( final JComponent component ) {
		final JPanel panel = new JPanel( new BorderLayout() );
		panel.add( component );
		return panel;
	}
	
	/**
	 * Resizes a window by setting its bounds to maximum that fits inside the default screen having the specified margin around it,
	 * and centers the window on the screen.
	 * 
	 * <p>The implementation takes the screen insets (for example space occupied by task bar) into account.</p>
	 * 
	 * @param window  window to be resized
	 * @param margin  margin to leave around the window
	 * @param maxSize optional parameter defining a maximum size
	 */
	public static void maximizeWindowWithMargin( final Window window, final int margin, final Dimension maxSize ) {
		final GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		
		final Rectangle bounds = defaultConfiguration.getBounds();
		final Insets    insets = Toolkit.getDefaultToolkit().getScreenInsets( defaultConfiguration );
		
		final int width  = bounds.width  - insets.left - insets.right  - ( margin << 1 );
		final int height = bounds.height - insets.top  - insets.bottom - ( margin << 1 );
		
		if ( maxSize == null )
			window.setSize( width, height );
		else
			window.setSize( Math.min( width, maxSize.width ), Math.min( height, maxSize.height ) );
		
		GeneralUtils.centerWindow( window );
	}
	
	/**
	 * Centers a window relative to another window.
	 * @param window   window to be centered
	 * @param toWindow reference window to be centered to
	 */
	public static void centerWindowToWindow( final Window window, final Window toWindow ) {
		window.setLocation(	toWindow.getX() + ( toWindow.getWidth() - window.getWidth() ) / 2, toWindow.getY() + ( toWindow.getHeight() - window.getHeight() ) / 2 );
	}
	
	/**
	 * Tries to set the specified Look and Feel.
	 * @param name name of the look and feel to be loaded
	 * @return true if the LAF was set successfully, false otherwise
	 */
	public static boolean setLAF( final String lafName ) {
		for ( final LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels() )
			if ( lookAndFeelInfo.getName().equals( lafName ) )
				try {
					UIManager.setLookAndFeel( lookAndFeelInfo.getClassName() );
					
					return true;
				} catch ( final Exception e ) {
					System.err.println( "Failed to set " + lookAndFeelInfo.getName() + " look and feel!" );
					e.printStackTrace( System.err );
					return false;
				}
		
		System.err.println( lafName + " look and feel was not found!" );
		
		return false;
	}
	
	/**
	 * Packs a table.<br>
	 * Resizes all columns to the maximum width of the values in each column.
	 * @param table table to be packed
	 */
	public static void packTable( final JTable table ) {
		for ( int column = table.getColumnCount() - 1; column >= 0 ; column-- ) {
			int maxWidth = 70; // A minimum width for the columns
			for ( int row = table.getRowCount() - 1; row >= 0 ; row-- )
				maxWidth = Math.max( maxWidth, table.getRowMargin() + table.getCellRenderer( row, column ).getTableCellRendererComponent( table, table.getValueAt( row, column ), false, false, row, column ).getPreferredSize().width );
			table.getColumnModel().getColumn( column ).setPreferredWidth( maxWidth );
		}
	}
	
	/**
	 * Packs some columns of a table.<br>
	 * Resizes the specified columns to exactly the maximum width of the values in each column.
	 * @param table table to be packed
	 * @param columns columns to be resized
	 */
	public static void packTable( final JTable table, final int[] columns ) {
		for ( final int column : columns ) {
			int maxWidth = 10; // A minimum width for the columns
			for ( int row = table.getRowCount() - 1; row >= 0 ; row-- )
				maxWidth = Math.max( maxWidth, table.getRowMargin() + table.getCellRenderer( row, column ).getTableCellRendererComponent( table, table.getValueAt( row, column ), false, false, row, column ).getPreferredSize().width );
			final TableColumn tableColumn = table.getColumnModel().getColumn( column );
			tableColumn.setMaxWidth( maxWidth );
			tableColumn.setMinWidth( maxWidth );
		}
	}
	
	/**
	 * Returns the installed LAF info array sorted by my preference.
	 * @return the installed LAF info array sorted by my preference
	 */
	public static LookAndFeelInfo[] getSortedInstalledLAFInfos() {
		final LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
		
		Arrays.sort( installedLookAndFeels, new Comparator< LookAndFeelInfo >() {
			// What we want to prioritize:
			final String[] lafNamesInOrder = new String[] {
					"Nimbus", "Metal", "Windows", "Squareness", "Office 2003", "Office XP", "Visual Studio 2005"
				};
			@Override
			public int compare( final LookAndFeelInfo l1, final LookAndFeelInfo l2 ) {
				for ( final String lafName : lafNamesInOrder ) {
					if ( lafName.equals( l1.getName() ) )
						return -1;
					if ( lafName.equals( l2.getName() ) )
						return 1;
				}
				
				return 0; // The rest is good as is.
			}
		} );
		
		return installedLookAndFeels;
	}
	
	/**
	 * Adds a new tab to a tabbed pane.<br>
	 * Calls the other {@link #addNewTab(String, Icon, boolean, JTabbedPane, JComponent, boolean, Runnable)}
	 * method with <code>addTabMnemonic=true</code> value.
	 * 
	 * @param title           title of the new tab
	 * @param icon            icon of the new tab
	 * @param tabbedPane      tabbed pane to add the tab to
	 * @param closeable       tells if the new tab is closeable (adds a close icon if it is)
	 * @param tab             tab component to be added
	 * @param beforeCloseTask optional task to be executed before close in case of closeable tabs
	 */
	public static void addNewTab( final String title, final Icon icon, final boolean closeable, final JTabbedPane tabbedPane, final JComponent tab, final Runnable beforeCloseTask ) {
		addNewTab( title, icon, closeable, tabbedPane, tab, true, beforeCloseTask );
	}
	
	/**
	 * Adds a new tab to a tabbed pane.
	 * 
	 * @param title           title of the new tab
	 * @param icon            icon of the new tab
	 * @param tabbedPane      tabbed pane to add the tab to
	 * @param closeable       tells if the new tab is closeable (adds a close icon if it is)
	 * @param tab             tab component to be added
	 * @param addTabMnemonic  tells if a tab mnemonic has to be added
	 * @param beforeCloseTask optional task to be executed before close in case of closeable tabs
	 */
	@SuppressWarnings("serial")
	public static void addNewTab( final String title, final Icon icon, final boolean closeable, final JTabbedPane tabbedPane, final JComponent tab, final boolean addTabMnemonic, final Runnable beforeCloseTask ) {
		tabbedPane.addTab( null, tab );
		
		final JLabel titleLabel;
		Character mnemonicChar = null;
		if( addTabMnemonic ) {
			final int tabCount = tabbedPane.getTabCount();
			if ( tabCount == 1 )
				mnemonicChar = '1';
			else {
				final int lastMnemonic = tabbedPane.getMnemonicAt( tabCount - 2 ) ;
				if ( lastMnemonic >= 0 && lastMnemonic < '9' )
					mnemonicChar = (char) ( lastMnemonic + 1 );
			}
		}
		if ( mnemonicChar != null ) {
			titleLabel = new JLabel( mnemonicChar + " " + title, icon, JLabel.LEFT );
			titleLabel.setDisplayedMnemonicIndex( 0 );
			tabbedPane.setMnemonicAt( tabbedPane.getTabCount() - 1, mnemonicChar );
		}
		else
			titleLabel = new JLabel( title, icon, JLabel.LEFT );
		
		if ( closeable ) {
			final Box titleBox = Box.createHorizontalBox();
			
			titleBox.add( titleLabel );
			
			final Holder< MouseListener > middleClickCloseListenerHolder = new Holder<MouseListener>();
			final Action closeTabAction = new AbstractAction() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					if ( beforeCloseTask != null )
						beforeCloseTask.run();
					tabbedPane.remove( tab );
					tabbedPane.removeMouseListener( middleClickCloseListenerHolder.value );
				}
			};
			
			final JLabel closeLabel = new JLabel( Icons.CROSS_SMALL );
			closeLabel.setToolTipText( Language.getText( "tab.close.tooltop" ) );
			closeLabel.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
			closeLabel.addMouseListener( new MouseAdapter() {
				@Override
				public void mousePressed( final MouseEvent event ) {
					closeTabAction.actionPerformed( null );
				};
			} );
			titleBox.add( closeLabel );
			
			// Middle click on tab title should close the tab
			middleClickCloseListenerHolder.value = new MouseAdapter() {
				@Override
				public void mousePressed( final MouseEvent event ) {
					if ( event.getButton() == MOUSE_BUTTON_MIDDLE && titleBox.contains( SwingUtilities.convertPoint( tabbedPane, event.getPoint(), titleBox ) ) )
						closeTabAction.actionPerformed( null );
				};
			};
			tabbedPane.addMouseListener( middleClickCloseListenerHolder.value );
			
			// Register CTRL+W for tab close
			final Object closeTabActionKey = new Object();
			tab.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( KeyEvent.VK_W, InputEvent.CTRL_MASK ), closeTabActionKey );
			tab.getActionMap().put( closeTabActionKey, closeTabAction );
			
			tabbedPane.setTabComponentAt( tabbedPane.getTabCount() - 1, titleBox );
		}
		else {
			tabbedPane.setTabComponentAt( tabbedPane.getTabCount() - 1, titleLabel );
		}
		
		// Move focus to the tab component, so hotkeys (key strokes) can be used right away
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				tab.requestFocusInWindow();
			}
		} );
	}
	
	/**
	 * Appends a timestamp, a line and a new line to a text area.
	 * @param textArea text area to append to
	 * @param line     line to be appended
	 */
	public static void appendNewLineWithTimestamp( final JTextArea textArea, final String line ) {
		textArea.append( Language.formatDateTime( new Date() ) );
		textArea.append( " - " );
		appendNewLine( textArea, line );
	}
	
	/**
	 * Appends a line and a new line to a text area.
	 * @param textArea text area to append to
	 * @param line     line to be appended
	 */
	public static void appendNewLine( final JTextArea textArea, final String line ) {
		textArea.append( line );
		textArea.append( "\n" );
		textArea.setCaretPosition( textArea.getDocument().getLength() );
	}
	
	/**
	 * Creates and returns a link to open error details (the system messages).
	 * @return a link to open error details (the system messages)
	 */
	public static JLabel createErrorDetailsLink() {
		final JLabel detailsLink = GeneralUtils.createLinkStyledLabel( Language.getText( "general.errorDetails" ) );
		detailsLink.addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				MainFrame.INSTANCE.viewSystemMessagesMenuItem.doClick();
			};
		} );
		return detailsLink;
	}
	
	/**
	 * Shows an error dialog with the specified message.
	 * @param message message to be displayed
	 * @param owner optional owner frame; if not provided the Main frame will be used
	 */
	public static void showErrorDialog( final Object message, final Frame... owner ) {
		final String buttonText = Language.getText( "general.okButton" );
		JOptionPane.showOptionDialog( owner.length == 0 ? MainFrame.INSTANCE : owner[ 0 ], new Object[] { message, new JLabel( " " ), createErrorDetailsLink() }, Language.getText( "general.errorTitle" ), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new Object[] { buttonText }, buttonText );
	}
	
	/**
	 * Shows an info dialog with the specified message.
	 * @param message message to be displayed
	 * @param owner optional owner frame; if not provided the Main frame will be used
	 */
	public static void showInfoDialog( final Object message, final Frame... owner ) {
		final String buttonText = Language.getText( "general.okButton" );
		JOptionPane.showOptionDialog( owner.length == 0 ? MainFrame.INSTANCE : owner[ 0 ], message, Language.getText( "general.infoTitle" ), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new Object[] { buttonText }, buttonText );
	}
	
	/**
	 * Shows confirmation dialog with options to confirm or reject options.<br>
	 * It has 2 types: a warning (with yes and cancel options) and a question (with yes and no options).
	 * @param message message to be displayed
	 * @param isWarning tells if this is a warning, otherwise it's treated as a question
	 * @param owner optional owner frame; if not provided the Main frame will be used
	 * @return 0 if yes/ok is selected, false otherwise
	 */
	public static int showConfirmDialog( final Object message, final boolean isWarning, final Frame... owner ) {
		final String yesButtonText = Language.getText( "general.yesButton" );
		return JOptionPane.showOptionDialog( owner.length == 0 ? MainFrame.INSTANCE : owner[ 0 ], message, Language.getText( "general.confirmationTitle" ), JOptionPane.DEFAULT_OPTION, isWarning ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE, null, new Object[] { yesButtonText, Language.getText( isWarning ? "general.cancelButton" : "general.noButton" ) }, yesButtonText );
	}
	
	/**
	 * Creates a replay file preview accessory for a JFileChooser.
	 * @param fileChooser file chooser to create the accessory for
	 * @return a replay file preview accessory for JFileChoosers
	 */
	public static JComponent createReplayFilePreviewAccessory( final JFileChooser fileChooser ) {
		final ReplayInfoBox replayInfoBox = new ReplayInfoBox();
		
		fileChooser.addPropertyChangeListener( new PropertyChangeListener() {
			@Override
			public void propertyChange( final PropertyChangeEvent event ) {
				if ( JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals( event.getPropertyName() ) ) {
					final File file = (File) event.getNewValue();
					if ( file == null || file.isDirectory() )
						return;
					replayInfoBox.setReplayFile( file );
				}
			}
		} );
		
		return replayInfoBox;
	}
	
	/**
	 * Creates a check box which text is specified by a textKey, its value is bound to the specified settingKey.<br>
	 * The initial value of the check box will be taken from the {@link Settings},
	 * and an action listener will be added to the check box to register changes at the {@link Settings}.
	 * 
	 * @param textKey    key of the text of the check box
	 * @param settingKey key of the settings its value is bound to
	 * @return the created check box
	 */
	public static JCheckBox createCheckBox( final String textKey, final String settingKey ) {
		final JCheckBox checkBox = new JCheckBox( Language.getText( textKey ), Settings.getBoolean( settingKey ) );
		
		checkBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				Settings.set( settingKey, checkBox.isSelected() );
			}
		} );
		
		return checkBox;
	}
	
	/**
	 * Creates a check box menu item which text is specified by a textKey, its value is bound to the specified settingKey.<br>
	 * The text will be set by calling the {@link GuiUtils#updateButtonText(AbstractButton, String, Object...)}.<br>
	 * The initial selection value of the check box menu item will be taken from the {@link Settings},
	 * and an action listener will be added to the menu item to register changes at the {@link Settings}.
	 * 
	 * @param textKey    key of the text of the check box menu item
	 * @param settingKey key of the settings its value is bound to
	 * @param icon       optional icon of the menu item
	 * @return the created check box menu item
	 */
	public static JCheckBoxMenuItem createCheckBoxMenuItem( final String textKey, final String settingKey, final Icon icon ) {
		final JCheckBoxMenuItem checkBoxMenuItem = new JCheckBoxMenuItem();
		
		GuiUtils.updateButtonText( checkBoxMenuItem, textKey );
		if ( icon != null )
			checkBoxMenuItem.setIcon( icon );
		checkBoxMenuItem.setSelected( Settings.getBoolean( settingKey ) );
		
		checkBoxMenuItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				Settings.set( settingKey, checkBoxMenuItem.isSelected() );
			}
		} );
		
		return checkBoxMenuItem;
	}
	
	/**
	 * Creates a combo box whose value is bound to the specified settingKey.<br>
	 * Calls the other {@link #createComboBox(Object[], String, boolean)} with <code>true</code> for the missing parameter value.
	 * 
	 * @param values     initial values of the combo box
	 * @param settingKey key of the settings its value is bound to
	 * @return the created combo box
	 * @see #createComboBox(Object[], String, boolean)
	 */
	public static <T> JComboBox< T > createComboBox( final T[] values, final String settingKey ) {
		final Vector< T > valueVector = new Vector< T >( values.length );
		for ( final T value : values )
			valueVector.add( value );
		
		return createComboBox( valueVector, settingKey, true );
	}
	
	/**
	 * Creates a combo box whose value is bound to the specified settingKey.<br>
	 * The initial value of the combo box will be taken from the {@link Settings},
	 * and an action listener will be added to the combo box to register changes at the {@link Settings}.
	 * 
	 * @param valueVector         vector of initial values of the combo box
	 * @param settingKey          key of the settings its value is bound to
	 * @param useSmallSizeVariant tells is small size variant feature of the Nimbus should be used
	 * @return the created combo box
	 */
	public static <T> JComboBox< T > createComboBox( final Vector< T > valueVector, final String settingKey, final boolean useSmallSizeVariant ) {
		final JComboBox< T > comboBox = new JComboBox<>( new CustomComboBoxModel< T >( valueVector ) ); 
		
		try {
			comboBox.setSelectedIndex( Settings.getInt( settingKey ) );
		} catch ( final IllegalArgumentException iae ) {
			comboBox.setSelectedIndex( Settings.getDefaultInt( settingKey ) );
		}
		
		comboBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				Settings.set( settingKey, comboBox.getSelectedIndex() );
			}
		} );
		
		if ( useSmallSizeVariant ) {
			comboBox.putClientProperty( "JComponent.sizeVariant", "small" );
			comboBox.updateUI();
		}
		
		return comboBox;
	}
	
	/**
	 * Creates a combo box for a pre-defined list.<br>
	 * The default editor component will be used.
	 * @param predefinedList pre-defined list to create a combo box for
	 * @return a combo box for a pre-defined list
	 */
	public static JComboBox< String > createPredefinedListComboBox( final PredefinedList predefinedList ) {
		return createPredefinedListComboBox( predefinedList, true );
	}
	
	/**
	 * Creates a combo box for a pre-defined list.
	 * @param predefinedList pre-defined list to create a combo box for
	 * @param addEditLink    tells if an edit link has to be added to the popup of the combo box
	 * @return a combo box for a pre-defined list
	 */
	public static JComboBox< String > createPredefinedListComboBox( final PredefinedList predefinedList, final boolean addEditLink ) {
		final JComboBox< String > comboBox = new JComboBox<>( new CustomComboBoxModel<>( Settings.getPredefinedListValues( predefinedList ) ) );
		comboBox.setEditable( true );
		comboBox.setMaximumRowCount( 20 );
		
		// The default one does not support changing background
		// Also it takes up more vertical space...
		comboBox.setEditor( new ComboBoxEditor() {
			private final JTextField editorTextField = new JTextField( 1 );
			{
				editorTextField.setBorder( BorderFactory.createEmptyBorder( 0, 3, 0, 3 ) );
			}
			@Override
			public void addActionListener( final ActionListener actionListener ) {
				editorTextField.addActionListener( actionListener );
			}
			@Override
			public Component getEditorComponent() {
				return editorTextField;
			}
			@Override
			public Object getItem() {
				return editorTextField.getText();
			}
			@Override
			public void removeActionListener( final ActionListener actionListener ) {
				editorTextField.removeActionListener( actionListener );
			}
			@Override
			public void selectAll() {
				editorTextField.selectAll();
			}
			@Override
			public void setItem( final Object item ) {
				editorTextField.setText( item == null ? null : item.toString() );
			}
		} );
		
		if ( addEditLink )
			addEditListLinkToComboBoxPopup( comboBox, new Runnable() {
				@Override
				public void run() {
					new MiscSettingsDialog( MainFrame.INSTANCE, SettingsTab.PREDEFINED_LISTS, predefinedList );
					// Refresh the list in the combo box. The vector reference is shared, so the following is enough:
					if ( comboBox.getModel() instanceof CustomComboBoxModel )
						( (CustomComboBoxModel<?>) comboBox.getModel() ).fireContentsChanged( comboBox );
				}
			} );
		
		return comboBox;
	}
	
	/**
	 * Creates a spinner whose value is bound to the specified settingKey.<br>
	 * The spinner will be created with a {@link SpinnerNumberModel}.
	 * The initial value of the spinner will be taken from the {@link Settings},
	 * and a change listener will be added to the spinner to register changes at the {@link Settings}.<br>
	 * Also sets a tool tip displaying the valid range.
	 * 
	 * @param settingKey key of the setting its value is bound to
	 * @param minimum    the minimum allowed number
	 * @param maximum    the maximum allowed number
	 * @param stepSize   step size of the spinner
	 * @return the created spinner
	 */
	public static JSpinner createSpinner( final String settingKey, final int minimum, final int maximum, final int stepSize ) {
		final JSpinner spinner = new JSpinner( new SpinnerNumberModel( Settings.getInt( settingKey ), minimum, maximum, stepSize ) );
		
		spinner.setToolTipText( Language.getText( "miscSettings.validRangeAndDefaultToolTip", minimum, maximum, Settings.getDefaultInt( settingKey ) ) );
		
		spinner.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged( final ChangeEvent event ) {
				Settings.set( settingKey, spinner.getValue() );
			}
		} );
		
		return spinner;
	}
	
	/**
	 * Creates a slider whose value is bound to the specified settingKey.<br>
	 * The initial value of the slider will be taken from the {@link Settings},
	 * and a change listener will be added to the slider to register changes at the {@link Settings}.<br>
	 * 
	 * @param settingKey key of the setting its value is bound to
	 * @param minValue   the minimum value
	 * @param maxValue   the maximum value
	 * @return the created slider
	 */
	public static JSlider createSlider( final String settingKey, final int minValue, final int maxValue ) {
		final JSlider slider = new JSlider( minValue, maxValue, Settings.getInt( settingKey ) );
		
		slider.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged( final ChangeEvent event ) {
				if ( slider.getValueIsAdjusting() )
					return;
				
				Settings.set( settingKey, slider.getValue() );
			}
		} );
		
		return slider;
	}
	
	/**
	 * Creates a text field whose value is bound to the specified settingKey.<br>
	 * The initial value of the text field will be taken from the {@link Settings},
	 * and a document listener will be added to the text field to register changes at the {@link Settings}.<br>
	 * 
	 * @param settingKey key of the setting its value is bound to
	 * @return the created text field
	 */
	public static JTextField createTextField( final String settingKey ) {
		final JTextField textField = new JTextField( Settings.getString( settingKey ) );
		
		textField.getDocument().addDocumentListener( new DocumentListener() {
			@Override public void removeUpdate ( final DocumentEvent event ) { updateSetting(); }
			@Override public void insertUpdate ( final DocumentEvent event ) { updateSetting(); }
			@Override public void changedUpdate( final DocumentEvent event ) { updateSetting(); }
			private void updateSetting() {
				Settings.set( settingKey, textField.getText() );
			}
		} );
		
		return textField;
	}
	
	/**
	 * Creates a text area whose value is bound to the specified settingKey.<br>
	 * The initial value of the text area will be taken from the {@link Settings},
	 * and a document listener will be added to the text area to register changes at the {@link Settings}.<br>
	 * 
	 * @param settingKey key of the setting its value is bound to
	 * @return the created text area
	 */
	public static JTextArea createTextArea( final String settingKey ) {
		final JTextArea textArea = new JTextArea( Settings.getString( settingKey ) );
		
		textArea.getDocument().addDocumentListener( new DocumentListener() {
			@Override public void removeUpdate ( final DocumentEvent event ) { updateSetting(); }
			@Override public void insertUpdate ( final DocumentEvent event ) { updateSetting(); }
			@Override public void changedUpdate( final DocumentEvent event ) { updateSetting(); }
			private void updateSetting() {
				Settings.set( settingKey, textArea.getText() );
			}
		} );
		
		return textArea;
	}
	
	/**
	 * Adds a link-style edit list label to the bottom of the popup of the specified combo box.<br>
	 * When the link is activated (clicked), the editTask will be run.
	 * @param comboBox combo box to add the edit link to
	 * @param editTask edit task to be performed when the link is activated
	 */
	public static void addEditListLinkToComboBoxPopup( final JComboBox< ? > comboBox, final Runnable editTask ) {
		final Object child = comboBox.getUI().getAccessibleChild( comboBox, 0 );
		if ( child instanceof JPopupMenu ) {
			final JPanel editPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 3, 1 ) );
			final JLabel editLinkLabel = GeneralUtils.createLinkStyledLabel( Language.getText( "general.editList" ) );
			editLinkLabel.addMouseListener( new MouseAdapter() {
				public void mousePressed( final MouseEvent event ) {
					comboBox.hidePopup();
					editTask.run();
				};
			} );
			editPanel.add( editLinkLabel );
			( (JPopupMenu) child ).add( editPanel );
		}
	}
	
	/**
	 * Makes a component drag-scrollable. So if the user tries to drag the component,
	 * it will be scrolled if it is added to a scroll pane. Also changes the mouse cursor to "MOVE" over this component.
	 * @param component component to be made drag-scrollable
	 */
	public static void makeComponentDragScrollable( final JComponent component ) {
		final MouseAdapter dragHandler = new MouseAdapter() {
			int dragStartX, dragStartY;
			@Override
			public void mousePressed( final MouseEvent event ) {
				if ( ( event.getModifiers() & InputEvent.BUTTON1_MASK ) != 0 ) {
					dragStartX = event.getXOnScreen();
					dragStartY = event.getYOnScreen();
				}
			};
			@Override
			public void mouseDragged( final MouseEvent event ) {
				if ( ( event.getModifiers() & InputEvent.BUTTON1_MASK ) != 0 ) {
					final Rectangle visibleRect = component.getVisibleRect();
					visibleRect.x += ( dragStartX - event.getXOnScreen() ) * 2; // No shifting as this can be negative!
					visibleRect.y += ( dragStartY - event.getYOnScreen() ) * 2; // No shifting as this can be negative!
					component.scrollRectToVisible( visibleRect );
					dragStartX = event.getXOnScreen();
					dragStartY = event.getYOnScreen();
				}
			}
		};
		
		component.addMouseListener( dragHandler );
		component.addMouseMotionListener( dragHandler );
		
		component.setCursor( Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR ) );
	}
	
	/**
	 * Changes the font of a component to BOLD.
	 * @param component component whose font to be changed to BOLD
	 * @return the component
	 */
	public static JComponent changeFontToBold( final JComponent component ) {
		component.setFont( component.getFont().deriveFont( Font.BOLD ) );
		return component;
	}
	
	/**
	 * Changes the font of a component to ITALIC.
	 * @param component component whose font to be changed to ITALIC
	 * @return the component
	 */
	public static JComponent changeFontToItalic( final JComponent component ) {
		component.setFont( component.getFont().deriveFont( Font.ITALIC ) );
		return component;
	}
	
	/**
	 * Creates and returns a non-editable table.
	 * @return and returns a non-editable table
	 */
	@SuppressWarnings("serial")
	public static JTable createNonEditableTable() {
		return new JTable() {
			@Override
			public boolean isCellEditable( final int row, final int column ) {
				return false;
			}
		};
	}
	
	/**
	 * Creates a close button for a dialog.
	 * The button disposes the dialog when it is pressed.
	 * @param dialog dialog to be closed when the button is presses
	 * @return a close button for the specified dialog
	 */
	public static JButton createCloseButton( final JDialog dialog ) {
		return createCloseButton( dialog, "button.close" );
	}
	
	/**
	 * Creates a close button for a dialog.
	 * The button disposes the dialog when it is pressed.
	 * @param dialog dialog to be closed when the button is presses
	 * @param textKey text key of the button
	 * @return a close button for the specified dialog
	 */
	public static JButton createCloseButton( final JDialog dialog, final String textKey ) {
		final JButton closeButton = new JButton();
		
		GuiUtils.updateButtonText( closeButton, textKey );
		
		closeButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				dialog.dispose();
			}
		} );
		
		return closeButton;
	}
	
	/**
	 * Creates and returns an icon label for the specified race.<br>
	 * The tool tip of the returned icon label will be the name of the race.
	 * @param race race whose icon label to be returned
	 * @return returns an icon label for the specified race
	 */
	public static JLabel createRaceIconLabel( final Race race ) {
		final JLabel label = new JLabel( Icons.getRaceIcon( race ) );
		label.setToolTipText( race == null ? "" : race.stringValue );
		return label;
	}
	
	/**
	 * Logical Symbol group.
	 * @author Andras Belicza
	 */
	private enum SymbolGroup {
		REPLAY_INFO( "replayops.renameDialog.tab.replayInfo", Icons.SC2             , Symbol.FORMAT, Symbol.MATCHUP, Symbol.DATE, Symbol.DATE_TIME, Symbol.GAME_TYPE, Symbol.GATEWAY, Symbol.GAME_LENGTH, Symbol.GAME_LENGTH_LONG, Symbol.LADDER_SEASON, Symbol.REPLAY_VERSION, Symbol.BUILD_NUMBER ),
		PLAYERS    ( "replayops.renameDialog.tab.players"   , Icons.USERS           , Symbol.PLAYER_INFO_BLOCK, Symbol.PLAYER_NAME, Symbol.RACE_FIRST_LETTER, Symbol.RACE, Symbol.LEAGUE, Symbol.APM, Symbol.EAPM, Symbol.RESULT, Symbol.ALL_PLAYER_NAMES, Symbol.ALL_PLAYER_NAMES_GROUPED, Symbol.WINNERS ),
		MAP        ( "replayops.renameDialog.tab.map"       , Icons.MAP             , Symbol.MAP_NAME, Symbol.MAP_FIRST_WORDS, Symbol.MAP_FIRST_LETTERS, Symbol.MAP_NAME_ACRONYM ),
		COUNTERS   ( "replayops.renameDialog.tab.counters"  , Icons.COUNTER_COUNT_UP, Symbol.COUNTER, Symbol.COUNTER2, Symbol.REPLAY_COUNTER ),
		GENERAL    ( "replayops.renameDialog.tab.general"   , null                  , Symbol.SUBFOLDER_SEPARATOR, Symbol.ORIGINAL_NAME, Symbol.EXTENSION, Symbol.MD5 );
		
		/** Cache of the string value. */
		public final String   stringValue;
		/** Icon of the symbol group.  */
		public final Icon     icon;
		/** Symbols of the group.      */
		public final Symbol[] symbols;
		
		/**
		 * Creates a new SymbolGroup.
		 * @param textKey key of the text representation
		 * @param icon    icon of the symbol group
		 */
		private SymbolGroup( final String textKey, final Icon icon, final Symbol... symbols ) {
			stringValue  = Language.getText( textKey );
			this.icon    = icon;
			this.symbols = symbols;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Creates and returns a name template editor component.
	 * @param nameTemplateComboBox editable combo box to write the template to
	 * @param symbolsToHide        optional list of symbols to hide
	 * @return a name template editor component
	 */
	public static JComponent createNameTemplateEditor( final JComboBox< ? > nameTemplateComboBox, final Symbol... symbolsToHide ) {
		final Box box = Box.createVerticalBox();
		
		box.add( GuiUtils.wrapInBorderPanel( new JLabel( Language.getText( "replayops.renameDialog.description1" ) ) ) );
		box.add( GuiUtils.wrapInBorderPanel( new JLabel( Language.getText( "replayops.renameDialog.description2" ) + " " + Language.getText( "replayops.renameDialog.description3" ) ) ) );
		box.add( Box.createVerticalStrut( 10 ) );
		box.add( GuiUtils.wrapInBorderPanel( new JLabel( Language.getText( "replayops.renameDialog.description4" ) ) ) );
		
		final JTabbedPane tabbedPane = new JTabbedPane();
		
		final List< JButton > symbolButtonList = new ArrayList< JButton >( 32 );
		
		final JTextField nameTemplateTextField = (JTextField) nameTemplateComboBox.getEditor().getEditorComponent();
		
		for ( final SymbolGroup symbolGroup : SymbolGroup.values() ) {
			final Box symbolsBox = Box.createVerticalBox();
			
			symbolCycle:
			for ( final Symbol symbol : symbolGroup.symbols ) {
				for ( final Symbol symbolToHide : symbolsToHide )
					if ( symbol == symbolToHide )
						continue symbolCycle;
				
				final Box row = Box.createHorizontalBox();
				row.setAlignmentX( 0.0f );
				final JButton symbolButton = new JButton( symbol.fragment, symbol.icon );
				symbolButtonList.add( symbolButton ); 
				symbolButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						try {
							nameTemplateTextField.getDocument().insertString( nameTemplateTextField.getCaretPosition(), ( (JButton) event.getSource() ).getText(), null );
						} catch ( final BadLocationException ble ) {
							ble.printStackTrace();
						}
						nameTemplateComboBox.requestFocusInWindow();
					}
				} );
				row.add( symbolButton );
				row.add( Box.createHorizontalStrut( 3 ) );
				row.add( new JLabel( symbol.toString() ) );
				symbolsBox.add( row );
			}
			
			GuiUtils.addNewTab( symbolGroup.stringValue, symbolGroup.icon, false, tabbedPane, wrapInPanelLeftAligned( symbolsBox ), null );
		}
		
		// Same width for all buttons
		int maxWidth = 0;
		for ( final JButton button : symbolButtonList )
			maxWidth = Math.max( maxWidth, button.getPreferredSize().width );
		for ( final JButton button : symbolButtonList ) {
			final Dimension newDimension = new Dimension( maxWidth, button.getPreferredSize().height );
			button.setPreferredSize( newDimension );
			button.setMaximumSize  ( newDimension );
		}
		
		box.add( tabbedPane );
		
		Box row = Box.createHorizontalBox();
		final JLabel templateLabel = new JLabel( Language.getText( "replayops.renameDialog.template" ) );
		row.add( templateLabel );
		row.add( nameTemplateComboBox );
		box.add( row );
		row = Box.createHorizontalBox();
		final JLabel explainedLabel = new JLabel( Language.getText( "replayops.renameDialog.explained" ) );
		row.add( explainedLabel );
		final JTextField explainedTextField = new JTextField();
		explainedTextField.setEditable( false );
		row.add( explainedTextField );
		box.add( row );
		// Align "Template:" and "Explained:" labels
		maxWidth = Math.max( templateLabel.getPreferredSize().width, explainedLabel.getPreferredSize().width );
		templateLabel .setPreferredSize( new Dimension( maxWidth, templateLabel .getPreferredSize().height ) );
		explainedLabel.setPreferredSize( new Dimension( maxWidth, explainedLabel.getPreferredSize().height ) );
		
		nameTemplateTextField.getDocument().addDocumentListener( new DocumentListener() {
			{
				changedUpdate( null ); // Initialize
			}
			@Override
			public void changedUpdate( final DocumentEvent event ) {
				try {
					explainedTextField.setText( new TemplateEngine( nameTemplateTextField.getText() ).explain() );
					explainedTextField.setBackground( UIManager.getColor( "TextField.background" ) );
				} catch ( final Exception e ) {
					explainedTextField.setText( Language.getText( "replayops.renameDialog.invalidTemplate", e.getMessage() ) );
					explainedTextField.setBackground( new Color( 255, 150, 150 ) );
				}
			}
			@Override
			public void removeUpdate( final DocumentEvent event ) {
				changedUpdate( event );
			}
			@Override
			public void insertUpdate( final DocumentEvent event ) {
				changedUpdate( event );
			}
		} );
		
		return box;
	}
	
	/**
	 * Creates a label-button with the specified icon and tool tip.
	 * @param icon           icon of the label-button
	 * @param toolTipTextKey text key of the tool tip of the label-button
	 * @return the created label-button
	 */
	public static JLabel createIconLabelButton( final ImageIcon icon, final String toolTipTextKey ) {
		final JLabel label = new JLabel( icon );
		label.setToolTipText( Language.getText( toolTipTextKey ) );
		label.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		label.setBorder( BorderFactory.createRaisedBevelBorder() );
		
		label.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseEntered( final MouseEvent event ) {
				label.setBorder( BorderFactory.createLoweredBevelBorder() );
			};
			@Override
			public void mouseExited( final MouseEvent event ) {
				label.setBorder( BorderFactory.createRaisedBevelBorder() );
			};
		} );
		
		return label;
	}
	
	/**
	 * Returns an icon with a dimension of 10x16 which indicates a color.<br>
	 * A rounded rectangle will be filled with the specified color, and a border will be drawn around it with the inverted color of the color.
	 * @param color color which to be represented by the returned icon
	 * @return an icon with a dimension of 10x16 which indicates a color
	 */
	public static Icon getColorIcon( final Color color ) {
		final Color invertedColor = GeneralUtils.getInvertedColor( color );
		return new Icon() {
			@Override
			public void paintIcon( final Component c, final Graphics g, final int x, final int y ) {
				final Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
				g2.setColor( color );
				g2.fillRoundRect( x+1, y, 8, 15, 8, 8 );
				g2.setColor( invertedColor );
				g2.drawRoundRect( x+1, y, 8, 15, 8, 8 );
			}
			@Override
			public int getIconWidth() {
				return 10;
			}
			@Override
			public int getIconHeight() {
				return 16;
			}
		};
	}
	
	/**
	 * Creates a "form style": same width for components that are in the same column.<br>
	 * The elements of the box must be containers (for example {@link Box} in order for this to work).
	 * @param box     reference to the box whose content to be aligned
	 * @param columns number of columns to align
	 */
	public static void alignBox( final Box box, final int columns ) {
		Box row;
		for ( int column = 0; column < columns; column++ ) {
			int maxWidth = 0;
			for ( int rowIndex = box.getComponentCount() - 1; rowIndex >= 0; rowIndex-- )
				if ( box.getComponent( rowIndex ) instanceof Box ) {
					row = (Box) box.getComponent( rowIndex );
					if ( row.getComponentCount() > column )
						maxWidth = Math.max( maxWidth, row.getComponent( column ).getPreferredSize().width );
				}
			
			for ( int rowIndex = box.getComponentCount() - 1; rowIndex >= 0; rowIndex-- )
				if ( box.getComponent( rowIndex ) instanceof Box ) {
					row = (Box) box.getComponent( rowIndex );
					if ( row.getComponentCount() > column )
						row.getComponent( column ).setPreferredSize( new Dimension( maxWidth, row.getComponent( column ).getPreferredSize().height ) );
				}
		}
	}
	
	/**
	 * Sets the enabled property of a component tree recursively.
	 * @param component component parent of a component tree whose enabled property to be set
	 * @param enabled value of the enabled property to be set
	 */
	public static void setComponentTreeEnabled( final Component component, final boolean enabled ) {
		component.setEnabled( enabled );
		
		if ( component instanceof Container ) {
			final Container container = (Container) component;
			for ( int i = container.getComponentCount() - 1; i >= 0; i-- )
				setComponentTreeEnabled( container.getComponent( i ), enabled );
		}
	}
	
	/**
	 * Applies some formatting on the specified info label, creates a right aligned wrapper panel for it, adds it and returns the panel. 
	 * @param infoLabel info label to be wrapped
	 * @param vgap      vgap of the wrapper panel (passed to {@link FlowLayout#FlowLayout(int, int, int)}
	 * @return the wrapper panel
	 */
	public static JPanel createRightAlignedInfoWrapperPanel( final JLabel infoLabel, final int vgap ) {
		GuiUtils.changeFontToItalic( infoLabel );
		infoLabel.setFont( infoLabel.getFont().deriveFont( (float) ( infoLabel.getFont().getSize() - 2 ) ) );
		
		final JPanel wrapper = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, vgap ) );
		wrapper.add( infoLabel );
		return wrapper;
	}
	
	/**
	 * Creates an editor pane.
	 * 
	 * <p>The returned editor pane is set to be non-editable, handles hyperlinks (opens them in the default browser),
	 * and removes the CTRL+SHIFT+O hotkey which is used by Sc2gears (it is associated with opening last replay).</p>
	 * 
	 * @return the created editor pane
	 */
	public static JEditorPane createEditorPane() {
		final JEditorPane editorPane = new JEditorPane();
		
		editorPane.setEditable( false );
		
		editorPane.addHyperlinkListener( new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate( final HyperlinkEvent event ) {
				if ( event.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
					GeneralUtils.showURLInBrowser( event.getURL().toExternalForm() );
			}
		} );
		
		// Remove CTRL+SHIFT+O
		Object actionKey;
		editorPane.getInputMap( JComponent.WHEN_FOCUSED ).put( KeyStroke.getKeyStroke( KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK ), actionKey = new Object() );
		editorPane.getActionMap().put( actionKey, null );
		
		return editorPane;
	}
	
	/**
	 * Updates the league labels (that display best leagues) based on the profile, and returns
	 * a profile tool tip which is also set for the league labels.
	 * 
	 * <p>Updating a league label means set the icon of the respective best league and add the profile tool tip
	 * returned by {@link #buildProfileToolTip(Profile, int)}.</p>
	 * 
	 * @param profile_     profile info to update the league labels with
	 * @param leagueLabels best league labels to be updated
	 * @param isAnotherRetrievingInProgress true if another retrieving is in progress; false otherwise<br>
	 * 		If the value is true, this method will be called again when updated or extended info is available.
	 * @return the tool tip of the profile
	 */
	public static String updateLeagueLabels( final IProfile profile_, final JLabel[] leagueLabels, final boolean isAnotherRetrievingInProgress ) {
		final Profile profile = (Profile) profile_;
		final String toolTip;
		final int maxRowsCount = Settings.getInt( Settings.KEY_REP_MISC_ANALYZER_MAX_ROWS_IN_PROFILE_TOOL_TIP ); 
		
		if ( profile == null || maxRowsCount == 0 )
			toolTip = null;
		else
			toolTip = buildProfileToolTip( profile, maxRowsCount );
		
		for ( int j = 0; j < 4; j++ )
			if ( profile == null || profile.bestRanks == null ) {
				if ( !isAnotherRetrievingInProgress )
					leagueLabels[ j ].setIcon( Icons.LEAGUE_NA_ICON );
			}
			else {
				if ( profile.bestRanks[ j ] != null )
					leagueLabels[ j ].setIcon( profile.bestRanks[ j ].league.getIconForClass( profile.bestRanks[ j ].leagueClass ) );
				else
					leagueLabels[ j ].setIcon( League.UNRANKED.getIconForRank( 0 ) );
				if ( toolTip != null )
					leagueLabels[ j ].setToolTipText( toolTip );
			}
		
		return toolTip;
	}
	
	/** Cache of the portrait tool tip leagues table header HTML value.  */
	private static String PORTRAIT_TOOL_TIP_LEAGUES_TABLE_HEADER;
	
	/**
	 * Builds a profile tool tip.
	 * @param profile profile to build tool tip for
	 * @param maxRowsCount max table rows to show in the tool tip
	 * @return the profile tool tip
	 */
	public static String buildProfileToolTip( final Profile profile, int maxRowsCount ) {
		final StringBuilder toolTipBuilder = new StringBuilder( "<html>" );
		
		// General info
		toolTipBuilder.append( "&nbsp;&nbsp;<b><img src=\"" ).append( Icons.ACHIEVEMENT.resource ).append( "\" border=0>" );
		toolTipBuilder.append( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.achievementPoints", profile.achievementPoints ) ).append( ",&nbsp;&nbsp;"    );
		toolTipBuilder.append( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.allGames"         , profile.totalCareerGames  ) ).append( ",&nbsp;&nbsp;"    );
		toolTipBuilder.append( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.gamesThisSeason"  , profile.gamesThisSeason   ) ).append( "<br>&nbsp;&nbsp;" );
		toolTipBuilder.append( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.terranWins"       , profile.terranWins        ) ).append( ",&nbsp;&nbsp;"    );
		toolTipBuilder.append( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.zergWins"         , profile.zergWins          ) ).append( ",&nbsp;&nbsp;"    );
		toolTipBuilder.append( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.protossWins"      , profile.protossWins       ) ).append( "<br>&nbsp;&nbsp;" );
		toolTipBuilder.append( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.highestFinishLeague" ) ).append( " " );
		toolTipBuilder.append( "<img src=\"" ).append( profile.highestSoloFinishLeague.getIconResourceForRank( 200 ) ).append( "\" width=16 height=16 border=0> 1v1" );
		if ( profile.highestSoloFinishTimes > 0 )
			toolTipBuilder.append( " (x" ).append( profile.highestSoloFinishTimes ).append( ')' );
		toolTipBuilder.append( ",&nbsp;&nbsp;" );
		toolTipBuilder.append( "<img src=\"" ).append( profile.highestTeamFinishLeague.getIconResourceForRank( 200 ) ).append( "\" width=16 height=16 border=0> " );
		toolTipBuilder.append( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.team" ) );
		if ( profile.highestTeamFinishTimes > 0 )
			toolTipBuilder.append( " (x" ).append( profile.highestTeamFinishTimes ).append( ')' );
		toolTipBuilder.append( "</b><br><br>" );
		
		// Leagues tables
		if ( PORTRAIT_TOOL_TIP_LEAGUES_TABLE_HEADER == null ) {
			PORTRAIT_TOOL_TIP_LEAGUES_TABLE_HEADER = "<tr><th>" + Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.format" )
				+ "<th>" + Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.league" )
				+ "<th>" + Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.rank" )
				+ "<th>" + Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.teamMembers" );
		}
		
		// Best leagues
		toolTipBuilder.append( "<table border=1 width='100%'>" );
		toolTipBuilder.append( "<tr><td colspan=6 align=center style='color:red;font-weight:bold'>" ).append( Language.getText( "playerProfile.currentBestLeagues" ) );
		toolTipBuilder.append( PORTRAIT_TOOL_TIP_LEAGUES_TABLE_HEADER );
		toolTipBuilder.append( "<th>" ).append( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.games" ) );
		toolTipBuilder.append( "<th>" ).append( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.wins"  ) );
		for ( int bracket = 0; bracket < profile.bestRanks.length; bracket++ ) {
			final BestTeamRank bestRank = profile.bestRanks[ bracket ];
			if ( bestRank == null )
				continue;
			--maxRowsCount;
			toolTipBuilder.append( "<tr>" )
				.append( "<td>" ).append( bracket+1 ).append( 'v' ).append( bracket+1 )
				.append( "<td><img src=\"" ).append( bestRank.league.getIconResourceForClass( bestRank.leagueClass ) ).append( "\" width=16 height=16 border=0>").append( bestRank.league.stringValue )
				.append( "<td align=right style='background:#60e060;color:blue;font-weight:bold'>" ).append( bestRank.divisionRank == 0 ? "NA" : bestRank.divisionRank )
				.append( "<td>" ).append( bestRank.getTeamMembersString() )
				.append( "<td align=right>" ).append( bestRank.games ).append( "&nbsp;&nbsp;(Σ " ).append( bestRank.gamesOfFormat ).append( ")<td align=right>" ).append( bestRank.wins );
		}
		
		// All leagues
		toolTipBuilder.append( "<tr><td colspan=6 align=center style='color:red;font-weight:bold'>" ).append( Language.getText( "playerProfile.allLeagues" ) );
		int hiddenRows = 0;
		toolTipBuilder.append( PORTRAIT_TOOL_TIP_LEAGUES_TABLE_HEADER );
		for ( int bracket = 0; bracket < profile.allRankss.length; bracket++ ) {
			final TeamRank[] teamRanks = profile.allRankss[ bracket ];
			if ( teamRanks == null )
				continue;
			
			for ( final TeamRank teamRank : teamRanks ) {
				if ( --maxRowsCount < 0 ) {
					hiddenRows++;
					continue;
				}
				toolTipBuilder.append( "<tr align=left>" )
					.append( "<td>" ).append( bracket+1 ).append( 'v' ).append( bracket+1 )
					.append( "<td><img src=\"" ).append( teamRank.league.getIconResourceForRank( teamRank.divisionRank ) ).append( "\" width=16 height=16 border=0>").append( teamRank.league.stringValue )
					.append( "<td align=right style='background:#60e060;color:blue;font-weight:bold'>" ).append( teamRank.divisionRank )
					.append( "<td>" ).append( teamRank.getTeamMembersString() );
			}
		}
		toolTipBuilder.append( "</table>" );
		if ( hiddenRows > 0 )
			toolTipBuilder.append( "<p style='font-weight:bold;font-style:italic'><br>" ).append( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.thereAreMoreRows", hiddenRows ) ).append( "</p>" );
		toolTipBuilder.append( "</html>" );
		
		return toolTipBuilder.toString();
	}
	
	/**
	 * Creates and returns a date time format help link label.
	 * @return a date time format help link label
	 */
	public static JLabel createDateTimeFormatHelpLinkLabel() {
		final JLabel dateTimeFormatHelpLinkLabel = GeneralUtils.createLinkLabel( null, Consts.URL_DATE_TIME_FORMAT_SPEC );
		dateTimeFormatHelpLinkLabel.setIcon( Icons.QUESTION );
		dateTimeFormatHelpLinkLabel.setToolTipText( Language.getText( "miscSettings.dateTimeFormatHelpLinkToolTip" ) );
		return dateTimeFormatHelpLinkLabel;
	}
	
	/**
	 * Concatenates 2 icons next to each other (in 1 row).
	 * @param icon1 first icon to be concatenated (this will be on the left side)
	 * @param icon2 second icon to be concatenated (this will be on the right side)
	 * @return an icon that is the result of the concatenation of the 2 specified icons
	 */
	public static Icon concatenateIcons( final Icon icon1, final Icon icon2 ) {
		return new Icon() {
			@Override
			public void paintIcon( final Component c, final Graphics g, final int x, final int y ) {
				icon1.paintIcon( c, g, x, y );
				icon2.paintIcon( c, g, x + icon1.getIconWidth(), y );
			}
			@Override
			public int getIconWidth() {
				return icon1.getIconWidth() + icon2.getIconWidth();
			}
			@Override
			public int getIconHeight() {
				return Math.max( icon1.getIconHeight(), icon2.getIconHeight() );
			}
		};
	}
	
	/**
	 * Creates and returns a link label which opens the Sc2gears Database User Page.
	 * @return a link label which opens the Sc2gears Database User Page
	 */
	public static JLabel createOpenUserPageLinkLabel() {
		final JLabel openUserPageLabel = GeneralUtils.createLinkLabel( Language.getText( "module.startPage.openUserPage" ), Consts.URL_SC2GEARS_DATABASE_USER_PAGE );
		openUserPageLabel.setIcon( Icons.SERVER_NETWORK );
		openUserPageLabel.setToolTipText( Language.getText( "module.startPage.openUserPageToolTip" ) );
		return openUserPageLabel;
	}
	
	/**
	 * Creates and returns a scroll panel which wraps the specified view component.<br>
	 * The returned scroll panel disables vertical scroll bar, and only displays the horizontal scroll bar when the view does not fit
	 * into the size of the view port. When the view fits into the view port, the scroll pane will not claim the space of the scroll bar.
	 * 
	 * @param view               view to wrap in the scroll pane
	 * @param parentToRevalidate parent to revalidate when the scroll pane decides to change its size
	 * 
	 * @return the created self managed scroll pane
	 */
	public static JScrollPane createSelfManagedScrollPane( final Component view, final JComponent parentToRevalidate ) {
		final JScrollPane scrollPane = new JScrollPane( view );
		
		scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_NEVER );
		scrollPane.getHorizontalScrollBar().setPreferredSize( new Dimension( 0, 12 ) ); // Only want to restrict the height, width doesn't matter (it takes up whole width)
		scrollPane.getHorizontalScrollBar().setUnitIncrement( 10 );
		
		final ComponentListener scrollPaneComponentListener = new ComponentAdapter() {
			@Override
			public void componentResized( final ComponentEvent event ) {
				scrollPane.setHorizontalScrollBarPolicy( view.getWidth() < scrollPane.getWidth() ? JScrollPane.HORIZONTAL_SCROLLBAR_NEVER : JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
				scrollPane.setPreferredSize( null );
				scrollPane.setPreferredSize( new Dimension( 10, scrollPane.getPreferredSize().height ) );
				parentToRevalidate.revalidate();
			}
		};
		scrollPane.addComponentListener( scrollPaneComponentListener );
		
		return scrollPane;
	}
	
}
