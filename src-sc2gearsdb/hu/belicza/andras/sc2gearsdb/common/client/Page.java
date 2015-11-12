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

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Represents a page.
 * 
 * @author Andras Belicza
 */
public abstract class Page extends VerticalPanel {
	
	/** Info panel for displaying messages and status of the page. */
	protected final InfoPanel infoPanel = new InfoPanel();
	
    /**
     * Creates a new Page.
     * @param title    displayable title of the page; can be <code>null</code>
     * @param pageName page name to be logged
     */
    public Page( final String title, final String pageName ) {
		setWidth( "100%" );
		setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		
		if ( title != null )
			add( ClientUtils.styledWidget( new Label( title ), "h2" ) );
		
		add( infoPanel );
		
		ClientUtils.trackAnalyticsPageView( pageName );
    }
	
    /**
     * Builds the GUI of the page.
     */
    public abstract void buildGUI();
    
}
