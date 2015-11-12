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

import hu.belicza.andras.sc2gears.logger.Log;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * System messages dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class SystemMessagesDialog extends BaseDialog {
	
	/**
	 * Creates a new SystemMessages.
	 */
	public SystemMessagesDialog() {
		super( "dialog.systemMessages.title", Icons.REPORT_EXCLAMATION );
		
		final JTextArea logTextArea = new JTextArea( Log.getLog(), 30, 68 );
		logTextArea.setEditable( false );
		getContentPane().add( new JScrollPane( logTextArea ), BorderLayout.CENTER );
		
		final JButton closeButton = createCloseButton( "button.close" );
		getContentPane().add( GuiUtils.wrapInPanel( closeButton ), BorderLayout.SOUTH );
		
		packAndShow( closeButton, false );
	}
	
}
