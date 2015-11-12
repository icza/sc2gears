/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api;

import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.api.ui.IProgressDialog;
import hu.belicza.andras.sc2gearspluginapi.api.ui.ITableBox;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;
import hu.belicza.andras.sc2gearspluginapi.impl.util.WordCloudTableInput;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

/**
 * GUI (Graphical User Interface) utility services.
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GeneralServices
 */
public interface GuiUtilsApi {
	
	/** Interface version. */
	String VERSION = "2.4";
	
	/**
	 * Returns a reference to the main frame which can be used/set as the parent for child dialogs.
	 * @return a reference to the main frame
	 */
	Frame getMainFrame();
	
	/**
	 * Updates/sets the text of an abstract button based on the key of its text.
	 * 
	 * <p>The button's text is specified by the <code>textKey</code>.<br>
	 * If the text of the button designates a mnemonic, it will be set properly.</p>
	 * 
	 * @param button  abstract button whose text to be updated
	 * @param textKey key of the button's text
	 * @since "2.0"
	 */
	void updateButtonText( AbstractButton button, String textKey, Object... arguments );
	
	/**
	 * Creates a label which operates as a link.
	 * The link will only be opened if the label is not disabled.
	 * @param text      text of the label (link)
	 * @param targetUrl URL to be opened when the user clicks on the label
	 * @return the label which operates as a link
	 */
	JLabel createLinkLabel( String text, String targetUrl );
	
	/**
	 * Creates a label which looks like a link and has a hand mouse cursor.
	 * @param text text of the label (link)
	 * @return a label which looks like a link
	 */
	JLabel createLinkStyledLabel( String text );
	
	/**
	 * Resizes a window by setting its bounds to maximum that fits inside the default screen having the specified margin around it,
	 * and centers the window on the screen.
	 * 
	 * <p>The implementation takes the screen insets (for example space occupied by task bar) into account.</p>
	 * 
	 * @param window  window to be resized
	 * @param margin  margin to leave around the window
	 * @param maxSize optional parameter defining a maximum size
	 * 
	 * @since "2.0"
	 * 
	 * @see #centerWindow(Window)
	 * @see #centerWindowToWindow(Window, Window)
	 */
	void maximizeWindowWithMargin( Window window, int margin, Dimension maxSize );
	
	/**
	 * Centers a window on its screen.
	 * @param window window to be centered
	 * @see #centerWindowToWindow(Window, Window)
	 * @see #maximizeWindowWithMargin(Window, int, Dimension)
	 */
	void centerWindow( Window window );
	
	/**
	 * Centers a window relative to another window.
	 * @param window   window to be centered
	 * @param toWindow reference window to be centered to
	 * @see #centerWindow(Window)
	 * @see #maximizeWindowWithMargin(Window, int, Dimension)
	 */
	void centerWindowToWindow( Window window, Window toWindow );
	
	/**
	 * Packs a table.<br>
	 * Resizes all columns to the maximum width of the values in each column.
	 * @param table table to be packed
	 */
	void packTable( JTable table );
	
	/**
	 * Packs some columns of a table.<br>
	 * Resizes the specified columns to exactly the maximum width of the values in each column.
	 * @param table table to be packed
	 */
	void packTable( JTable table, int[] columns );
	
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
	void addNewTab( String title, Icon icon, boolean closeable, JTabbedPane tabbedPane, JComponent tab, Runnable beforeCloseTask );
	
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
	void addNewTab( String title, Icon icon, boolean closeable, JTabbedPane tabbedPane, JComponent tab, boolean addTabMnemonic, Runnable beforeCloseTask );
	
	/**
	 * Appends a time stamp, a line and a new line to a text area.
	 * @param textArea text area to append to
	 * @param line     line to be appended
	 */
	void appendNewLineWithTimestamp( JTextArea textArea, String line );
	
	/**
	 * Appends a line and a new line to a text area.
	 * @param textArea text area to append to
	 * @param line     line to be appended
	 */
	void appendNewLine( JTextArea textArea, String line );
	
	/**
	 * Creates and returns a link to open error details (the system messages).
	 * @return a link to open error details (the system messages)
	 */
	JLabel createErrorDetailsLink();
	
	/**
	 * Shows an error dialog with the specified message.
	 * @param message message to be displayed
	 * @param owner optional owner frame; if not provided the Main frame will be used
	 */
	void showErrorDialog( Object message, Frame... owner );
	
	/**
	 * Shows an info dialog with the specified message.
	 * @param message message to be displayed
	 * @param owner optional owner frame; if not provided the Main frame will be used
	 */
	void showInfoDialog( Object message, Frame... owner );
	
	/**
	 * Shows confirmation dialog with options to confirm or reject options.<br>
	 * It has 2 types: a warning (with yes and cancel options) and a question (with yes and no options).
	 * @param message message to be displayed
	 * @param isWarning tells if this is a warning, otherwise it's treated as a question
	 * @param owner optional owner frame; if not provided the Main frame will be used
	 * @return 0 if yes/ok is selected, false otherwise
	 */
	int showConfirmDialog( Object message, boolean isWarning, Frame... owner );
	
	/**
	 * Creates a replay file preview accessory for a JFileChooser.
	 * @param fileChooser file chooser to create the accessory for
	 * @return a replay file preview accessory for JFileChoosers
	 */
	JComponent createReplayFilePreviewAccessory( JFileChooser fileChooser );
	
	/**
	 * Makes a component drag-scrollable. So if the user tries to drag the component,
	 * it will be scrolled if it is added to a scroll pane. Also changes the mouse cursor to "MOVE" over this component.
	 * @param component component to be made drag-scrollable
	 */
	void makeComponentDragScrollable( JComponent component );
	
	/**
	 * Changes the font of a component to BOLD.
	 * @param component component whose font to be changed to BOLD
	 * @return the component
	 */
	JComponent changeFontToBold( JComponent component );
	
	/**
	 * Changes the font of a component to ITALIC.
	 * @param component component whose font to be changed to ITALIC
	 * @return the component
	 */
	JComponent changeFontToItalic( JComponent component );
	
	/**
	 * Creates and returns a non-editable table.
	 * @return and returns a non-editable table
	 */
	JTable createNonEditableTable();
	
	/**
	 * Creates a close button for a dialog.
	 * The button disposes the dialog when it is pressed.
	 * @param dialog dialog to be closed when the button is presses
	 * @return a close button for the specified dialog
	 * @since "2.0"
	 * @see #createCloseButton(JDialog, String)
	 */
	JButton createCloseButton( JDialog dialog );
	
	/**
	 * Creates a close button for a dialog.
	 * The button disposes the dialog when it is pressed.
	 * @param dialog dialog to be closed when the button is presses
	 * @param textKey text key of the button
	 * @return a close button for the specified dialog
	 * @since "2.0"
	 * @see #createCloseButton(JDialog)
	 */
	JButton createCloseButton( JDialog dialog, String textKey );
	
	/**
	 * Creates and returns an icon label for the specified race.<br>
	 * The tool tip of the returned icon label will be the name of the race.
	 * @param race race whose icon label to be returned
	 * @return returns an icon label for the specified race
	 * @see Race
	 * @since "2.0"
	 */
	JLabel createRaceIconLabel( Race race );
	
	/**
	 * Creates a label-button with the specified icon and tool tip.
	 * @param icon           icon of the label-button
	 * @param toolTipTextKey text key of the tool tip of the label-button
	 * @return the created label-button
	 * @since "2.0"
	 */
	JLabel createIconLabelButton( ImageIcon icon, String toolTipTextKey );
	
	/**
	 * Returns an icon with a dimension of 10x16 which indicates a color.<br>
	 * A rounded rectangle will be filled with the specified color, and a border will be drawn around it with the inverted color of the color.
	 * @param color color which to be represented by the returned icon
	 * @return an icon with a dimension of 10x16 which indicates a color
	 */
	Icon getColorIcon( Color color );
	
	/**
	 * Creates a "form style": same width for components that are in the same column.<br>
	 * The elements of the box must be containers (for example {@link Box} in order for this to work).
	 * 
	 * <p>Example usage:<br>
	 * <blockquote><pre>
	 * Box box = Box.createVerticalBox();
	 * 
	 * // First row
	 * Box row = Box.createHorizontalBox();
	 * row.add( new JLabel( "User name:" ) );
	 * row.add( new JTextField( 10 ) );
	 * row.add( new JButton( "Clear" ) );
	 * box.add( row );
	 * 
	 * // Second row
	 * row = Box.createHorizontalBox();
	 * row.add( new JLabel( "Password:" ) );
	 * row.add( new JPasswordField() );
	 * row.add( new JButton( "Login" ) );
	 * box.add( row );
	 * 
	 * alignBox( box, 3 );
	 * </pre></blockquote></p>
	 * 
	 * @param box     reference to the box whose content to be aligned
	 * @param columns number of columns to align
	 */
	void alignBox( Box box, int columns );
	
	/**
	 * Sets the enabled property of a component tree recursively.
	 * @param component component parent of a component tree whose enabled property to be set
	 * @param enabled value of the enabled property to be set
	 */
	void setComponentTreeEnabled( Component component, boolean enabled );
	
	/**
	 * Creates a table box.
	 * 
	 * @param table         table to be wrapped
	 * @param rootComponent root component to register filter hotkeys at;<br>
	 * 		usually this is the layered pane ({@link JLayeredPane}) of the window or dialog the table is displayed in
	 * 
	 * @return the created table box
	 * @see ITableBox
	 * @see #createTableBox(JTable, JComponent, WordCloudTableInput)
	 */
	ITableBox createTableBox( JTable table, JComponent rootComponent );
	
	/**
	 * Creates a table box.
	 * 
	 * @param table               table to be wrapped
	 * @param rootComponent       root component to register filter hotkeys at;<br>
	 * 		usually this is the layered pane ({@link JLayeredPane}) of the window or dialog the table is displayed in
	 * @param wordCloudTableInput optional parameter, if provided, a Word Cloud link will be added
	 * 		which will open a Word Cloud dialog taking its input from the table as specified by this object
	 * 
	 * @return the created table box
	 * @since "2.3"
	 * @see ITableBox
	 * @see WordCloudTableInput
	 * @see #createTableBox(JTable, JComponent)
	 */
	ITableBox createTableBox( JTable table, JComponent rootComponent, WordCloudTableInput wordCloudTableInput );
	
	/**
	 * Creates and returns a progress dialog.<br>
	 * The returned dialog will already be visible.<br>
	 * The main frame is used as the owner of the returned dialog.
	 * 
	 * @param title title of the dialog
	 * @param icon  optional icon of the dialog
	 * @param total total number of the subtasks to be performed
	 * @return the created and displayed progress dialog
	 * @see IProgressDialog
	 * @see #createProgressDialog(Frame, String, ImageIcon, int)
	 * @see #createProgressDialog(Dialog, String, ImageIcon, int)
	 */
	IProgressDialog createProgressDialog( String title, ImageIcon icon, int total );
	
	/**
	 * Creates and returns a progress dialog.<br>
	 * The returned dialog will already be visible.
	 * 
	 * @param owner the Frame from which the dialog is displayed
	 * @param title title of the dialog
	 * @param icon  optional icon of the dialog
	 * @param total total number of the subtasks to be performed
	 * @return the created and displayed progress dialog
	 * @see IProgressDialog
	 * @see #createProgressDialog(String, ImageIcon, int)
	 * @see #createProgressDialog(Dialog, String, ImageIcon, int)
	 */
	IProgressDialog createProgressDialog( Frame owner, String title, ImageIcon icon, int total );
	
	/**
	 * Creates and returns a progress dialog.<br>
	 * The returned dialog will already be visible.
	 * 
	 * @param owner the Dialog from which the dialog is displayed
	 * @param title title of the dialog
	 * @param icon  optional icon of the dialog
	 * @param total total number of the subtasks to be performed
	 * @return the created and displayed progress dialog
	 * @see IProgressDialog
	 * @see #createProgressDialog(String, ImageIcon, int)
	 * @see #createProgressDialog(Frame, String, ImageIcon, int)
	 */
	IProgressDialog createProgressDialog( Dialog owner, String title, ImageIcon icon, int total );
	
	/**
	 * Creates an editor pane.
	 * 
	 * <p>The returned editor pane is set to be non-editable, handles hyperlinks (opens them in the default browser),
	 * and removes the CTRL+SHIFT+O hotkey which is used by Sc2gears (it is associated with opening last replay).</p>
	 * 
	 * @return the created editor pane.
	 */
	JEditorPane createJEditorPane();
	
	/**
	 * Creates and shows a new Word Cloud dialog.
	 * @param parent   parent of the dialog to show
	 * @param title    sub-title of the dialog
	 * @param wordList list of words (and their frequency) to build the word cloud from.
	 * @since "2.3"
	 * @see #showWordCloudDialog(Frame, String, List)
	 */
	void showWordCloudDialog( Dialog parent, String title, List< Pair< String, Integer > > wordList );
	
	/**
	 * Creates and shows a new Word Cloud dialog.
	 * @param parent   parent of the dialog to show
	 * @param title    sub-title of the dialog
	 * @param wordList list of words (and their frequency) to build the word cloud from.
	 * @since "2.3"
	 * @see #showWordCloudDialog(Dialog, String, List)
	 */
	void showWordCloudDialog( Frame parent, String title, List< Pair< String, Integer > > wordList );
	
	/**
	 * Creates and shows a new browser dialog.
	 * @param parent        parent of the dialog to show
	 * @param title         title of the dialog
	 * @param icon          optional icon of the dialog
	 * @param url           URL of the page to be displayed
	 * @param preferredSize optional preferred size of the browser component
	 * @since "2.4"
	 * @see #showBrowserDialog(Dialog, String, ImageIcon, String, Dimension)
	 */
	void showBrowserDialog( Frame parent, String title, ImageIcon icon, String url, Dimension preferredSize );
	
	/**
	 * Creates and shows a new browser dialog.
	 * @param parent        parent of the dialog to show
	 * @param title         title of the dialog
	 * @param icon          optional icon of the dialog
	 * @param url           URL of the page to be displayed
	 * @param preferredSize optional preferred size of the browser component
	 * @since "2.4"
	 * @see #showBrowserDialog(Frame, String, ImageIcon, String, Dimension)
	 */
	void showBrowserDialog( Dialog parent, String title, ImageIcon icon, String url, Dimension preferredSize );
	
	/**
	 * Shows a color chooser dialog.
	 * @param parent       parent of the dialog to show
	 * @param initialColor optional initial color to be selected
	 * @return the selected color or <code>null</code> if no color was selected (cancel was pressed)
	 * @since "2.0"
	 * @see #showColorChooserDialog(Frame, Color)
	 */
	Color showColorChooserDialog( Dialog parent, Color initialColor );
	
	/**
	 * Shows a color chooser dialog.
	 * @param parent       parent of the dialog to show
	 * @param initialColor optional initial color to be selected
	 * @return the selected color or <code>null</code> if no color was selected (cancel was pressed)
	 * @since "2.0"
	 * @see #showColorChooserDialog(Dialog, Color)
	 */
	Color showColorChooserDialog( Frame parent, Color initialColor );
	
}
