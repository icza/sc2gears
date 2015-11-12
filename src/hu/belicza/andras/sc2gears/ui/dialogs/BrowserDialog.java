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

import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gears.util.NormalThread;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

/**
 * A browser dialog which displays the content of a web page.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class BrowserDialog extends BaseDialog {
	
	/**
	 * Creates a new BrowserDialog.
	 * @param owner         the Frame from which the dialog is displayed
	 * @param titleTextKey  text key of the dialog title
	 * @param icon          optional icon of the dialog
	 * @param url           URL of the page to be displayed
	 * @param preferredSize optional preferred size of the browser component
	 */
	public BrowserDialog( final Frame owner, final String titleTextKey, final ImageIcon icon, final String url, final Dimension preferredSize ) {
		super( owner, titleTextKey, icon );
		
		completeInit( url, preferredSize );
	}
	
	/**
	 * Creates a new BrowserDialog.
	 * @param owner         the Dialog from which the dialog is displayed
	 * @param titleTextKey  text key of the dialog title
	 * @param icon          optional icon of the dialog
	 * @param url           URL of the page to be displayed
	 * @param preferredSize optional preferred size of the browser component
	 */
	public BrowserDialog( final Dialog owner, final String titleTextKey, final ImageIcon icon, final String url, final Dimension preferredSize ) {
		super( owner, titleTextKey, icon );
		
		completeInit( url, preferredSize );
	}
	
	/**
	 * Creates a new BrowserDialog.
	 * @param owner         the Frame from which the dialog is displayed
	 * @param title         title of the dialog
	 * @param icon          optional icon of the dialog
	 * @param url           URL of the page to be displayed
	 * @param preferredSize optional preferred size of the browser component
	 */
	public BrowserDialog( final Frame owner, final Holder< String > title, final ImageIcon icon, final String url, final Dimension preferredSize ) {
		super( owner, title, icon );
		
		completeInit( url, preferredSize );
	}
	
	/**
	 * Creates a new BrowserDialog.
	 * @param owner         the Dialog from which the dialog is displayed
	 * @param title         title of the dialog
	 * @param icon          optional icon of the dialog
	 * @param url           URL of the page to be displayed
	 * @param preferredSize optional preferred size of the browser component
	 */
	public BrowserDialog( final Dialog owner, final Holder< String > title, final ImageIcon icon, final String url, final Dimension preferredSize ) {
		super( owner, title, icon );
		
		completeInit( url, preferredSize );
	}
	
	/**
	 * Completes the initialization of the dialog.
	 * @param url           URL of the page to be displayed
	 * @param preferredSize optional preferred size of the browser component
	 */
	private void completeInit( final String url, final Dimension preferredSize ) {
		final JEditorPane browserPane = GuiUtils.createEditorPane();
		browserPane.setPreferredSize( preferredSize );
		final JScrollPane scrollPane = new JScrollPane( browserPane );
		scrollPane.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		getContentPane().add( scrollPane, BorderLayout.CENTER );
		
		final JButton okButton = createCloseButton( "button.close" );
		getContentPane().add( GuiUtils.wrapInPanel( okButton ), BorderLayout.SOUTH );
		
		// Load page content in a new thread to not block the AWT event dispatcher thread
		new NormalThread( "Browser dialog content loader" ) {
			@Override
            public void run() {
				try {
					browserPane.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
			        browserPane.setPage( url );
		        } catch ( final IOException ie ) {
			        ie.printStackTrace();
			        browserPane.setContentType( "text/html" );
			        browserPane.setText( "<html><body style='font-family:arial;font-size:10px;font-style:italic;background:#ffffff;'>"
							+ "<p>This content is currently unavailable. Please try again later.</p></body></html>" );
		        } finally {
					browserPane.setCursor( null );
		        }
            }
		}.start();
		
		packAndShow( okButton, false );
	}
	
}
