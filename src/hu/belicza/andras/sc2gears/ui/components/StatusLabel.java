/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.components;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

/**
 * A status label which can display a message with different styles.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class StatusLabel extends JLabel {
	
    /**
     * Creates a new StatusLabel.
     */
    public StatusLabel() {
		GuiUtils.changeFontToItalic( this );
    	clearMessage();
    }
	
	/**
	 * Sets a progress message.
	 * @param progressMessageKey text key of the progress message to be set
	 * @see #setProgressMessage(String)
	 */
	public void setProgressMessageKey( final String progressMessageKey ) {
		setProgressMessage( Language.getText( progressMessageKey ) );
	}
	
	/**
	 * Sets a progress message.
	 * @param progressMessage progress message to be set
	 * @see #setProgressMessageKey(String)
	 */
	public void setProgressMessage( final String progressMessage ) {
		setIcon( Icons.LOADING );
		setFont( getFont().deriveFont( Font.ITALIC ) );
		setText( progressMessage );
		setForeground( null );
	}
	
	/**
	 * Sets an info message.
	 * @param infoMessageKey text key of the info message to be set
	 * @see #setInfoMessage(String)
	 */
	public void setInfoMessageKey( final String infoMessageKey ) {
		setInfoMessage( Language.getText( infoMessageKey ) );
	}
	
	/**
	 * Sets an info message.
	 * @param infoMessage info message to be set
	 * @see #setInfoMessageKey(String)
	 */
	public void setInfoMessage( final String infoMessage ) {
		setIcon( null );
		setFont( getFont().deriveFont( Font.PLAIN ) );
		setText( infoMessage );
		setForeground( new Color( 0, 128, 0 ) );
	}
	
	/**
	 * Sets an error message.
	 * @param errorMessageKey text key of the error message to be set
	 * @see #setErrorMessage(String)
	 */
	public void setErrorMessageKey( final String errorMessageKey ) {
		setErrorMessage( Language.getText( errorMessageKey ) );
	}
	
	/**
	 * Sets an error message.
	 * @param errorMessage error message to be set
	 * @see #setErrorMessageKey(String)
	 */
	public void setErrorMessage( final String errorMessage ) {
		setIcon( null );
		setFont( getFont().deriveFont( Font.BOLD ) );
		setText( errorMessage );
		setForeground( Color.RED );
	}
	
	/**
	 * Clears the status message.
	 */
	public void clearMessage() {
		setIcon( null );
		setText( " " );
		setForeground( null );
	}
	
}
