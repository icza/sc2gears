/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.common.client;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * An info panel which can display "work-in-progress" state and info and error messages in different styles.
 * 
 * @author Andras Belicza
 */
public class InfoPanel extends HorizontalPanel {
	
	private final Image loadingImage = new Image( "/images/loading.gif" );
	
	/** Label to display messages. */
	private final Label messageLabel = new Label( " " );
	
	/**
	 * Creates a new InfoPanel.
	 */
	public InfoPanel() {
		setHeight( "20px" );
		loadingImage.setVisible( false );
		add( loadingImage );
		add( messageLabel );
	}
	
	/**
	 * Sets the "work-in-progress" state.
	 * @param loading the "work-in-progress" state
	 */
	public void setLoading( final boolean loading ) {
		loadingImage.setVisible( loading );
		if ( loading )
			messageLabel.setText( " " );
	}
	
	/**
	 * Sets an info message.
	 * @param infoMessage info message to be set
	 */
	public void setInfoMessage( final String infoMessage ) {
		messageLabel.removeStyleName( "errorMsg" );
		messageLabel.addStyleName( "infoMsg" );
		messageLabel.setText( infoMessage );
	}
	
	/**
	 * Sets an error message.
	 * @param errorMessage error message to be set
	 */
	public void setErrorMessage( final String errorMessage ) {
		messageLabel.removeStyleName( "infoMsg" );
		messageLabel.addStyleName( "errorMsg" );
		messageLabel.setText( errorMessage );
	}
	
}
