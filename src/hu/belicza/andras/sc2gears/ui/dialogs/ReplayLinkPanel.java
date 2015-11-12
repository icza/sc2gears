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

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A panel displaying a replay link and buttons to perform various copy to clipboard and visit link operations.<br>
 * The buttons are disabled until an URL is set.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
class ReplayLinkPanel extends JPanel {
	
	/** Info label.                                            */
	private final JLabel     replayLinkLabel       = new JLabel( Language.getText( "shareReplay.replayLink" ) );
	/** Text field displaying the URL.                         */
	private final JTextField replayLinkTextField   = new JTextField( 10 );
	/** Button to copy the URL to the clipboard.               */
	private final JButton    copyUrlButton         = new JButton( Icons.CLIPBOARD_SIGN );
	/** Button to copy a forum style URL tag to the clipboard. */
	private final JButton    copyForumLinkButton   = new JButton( Icons.CLIPBOARD_SIGN );
	/** Button to copy an HTML link to the clipboard.          */
	private final JButton    copyHtmlLinkButton    = new JButton( Icons.CLIPBOARD_SIGN );
	/** Button to visit the URL.                               */
	private final JButton    visitReplayLinkButton = new JButton( Icons.APPLICATION_BROWSER );
	
	/**
	 * Creates a new ReplayLinkPanel.
	 */
	public ReplayLinkPanel() {
		super( new BorderLayout() );
		
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		replayLinkLabel.setOpaque( true );
		add( replayLinkLabel, BorderLayout.WEST );
		replayLinkTextField.setEditable( false );
		add( replayLinkTextField, BorderLayout.CENTER );
		final Box replayLinkButtonsBox = Box.createHorizontalBox();
		final ActionListener copyActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				String textToCopy = replayLinkTextField.getText();
				if ( event.getSource() == copyForumLinkButton )
					textToCopy = "[url=" + textToCopy + "]Replay[/url]";
				else if ( event.getSource() == copyHtmlLinkButton )
					textToCopy = "<a href=\"" + textToCopy + "\">Replay</a>";
				GeneralUtils.copyToClipboard( textToCopy );
			}
		};
		copyUrlButton.setEnabled( false );
		GuiUtils.updateButtonText( copyUrlButton, "shareReplay.copyUrlButton" );
		copyUrlButton.addActionListener( copyActionListener );
		replayLinkButtonsBox.add( copyUrlButton );
		
		copyForumLinkButton.setEnabled( false );
		GuiUtils.updateButtonText( copyForumLinkButton, "shareReplay.copyForumLinkButton" );
		copyForumLinkButton.addActionListener( copyActionListener );
		replayLinkButtonsBox.add( copyForumLinkButton );
		
		copyHtmlLinkButton.setEnabled( false );
		GuiUtils.updateButtonText( copyHtmlLinkButton, "shareReplay.copyHtmlLinkButton" );
		copyHtmlLinkButton.addActionListener( copyActionListener );
		replayLinkButtonsBox.add( copyHtmlLinkButton );
		
		visitReplayLinkButton.setEnabled( false );
		GuiUtils.updateButtonText( visitReplayLinkButton, "shareReplay.visitReplayLinkButton" );
		visitReplayLinkButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				GeneralUtils.showURLInBrowser( replayLinkTextField.getText() );
			}
		} );
		replayLinkButtonsBox.add( visitReplayLinkButton );
		add( replayLinkButtonsBox, BorderLayout.EAST );
	}
	
	/**
	 * Sets the URL displayed on this panel.<br>
	 * Also enables the buttons to perform various operations with/on it.
	 * @param url       URL to be set
	 * @param highlight tells if the info label before the URL's text field has to be highlighted
	 */
	public void setUrl( final String url, final boolean highlight, final boolean focusCopyButton ) {
		if ( highlight )
			replayLinkLabel.setBackground( Color.GREEN );
		
		replayLinkTextField.setText( url );
		replayLinkTextField.selectAll();
		
		copyUrlButton        .setEnabled( true );
		copyForumLinkButton  .setEnabled( true );
		copyHtmlLinkButton   .setEnabled( true );
		visitReplayLinkButton.setEnabled( true );
		
		if ( focusCopyButton )
			copyUrlButton.requestFocusInWindow();
	}
	
}
