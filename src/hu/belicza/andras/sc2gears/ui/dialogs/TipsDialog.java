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
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.startpage.StartPage;
import hu.belicza.andras.sc2gears.util.GeneralUtils;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Tips dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class TipsDialog extends BaseDialog {
	
	/**
	 * Creates a new TipsDialog.
	 */
	public TipsDialog() {
		super( "tips.title", Icons.LIGHT_BULB );
		
		final JTextArea tipsTextArea = new JTextArea( 25, 65 );
		tipsTextArea.setEditable( false );
		tipsTextArea.setLineWrap( true );
		tipsTextArea.setWrapStyleWord( true );
		// Set tips as text
		for ( int i = 1; i <= StartPage.TIPS_COUNT; i++ )
			tipsTextArea.append( "\n" + i + ". " + Language.getText( "module.startPage.tip." + i ) + "\n" );
		tipsTextArea.setCaretPosition( 0 );
		final JScrollPane scrollPane = new JScrollPane( tipsTextArea );
		scrollPane.setBorder( BorderFactory.createEmptyBorder( 15, 15, 15, 15 ) );
		getContentPane().add( scrollPane, BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 0, 15, 10, 15 ) );
		final JButton closeButton = createCloseButton( "button.close" );
		buttonsPanel.add( closeButton );
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		packAndShow( closeButton, false );
	}
	
	/**
	 * Creates and returns a link label which opens this Tips dialog.
	 * @param textKey text key of the label to create
	 * @param setIcon tells whether to set an appropriate icon to the label
	 * @return a link label which opens this Tips dialog.
	 */
	public static JLabel createOpenLinkLabel( final String textKey, final boolean setIcon ) {
		final JLabel openLinkLabel = GeneralUtils.createLinkStyledLabel( Language.getText( textKey ) );
		if ( setIcon )
			openLinkLabel.setIcon( Icons.LIGHT_BULB );
		
		openLinkLabel.addMouseListener( new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				new TipsDialog();
			};
		} );
		
		return openLinkLabel;
	}
	
}
