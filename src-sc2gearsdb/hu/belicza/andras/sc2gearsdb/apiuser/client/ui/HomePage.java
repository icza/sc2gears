/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.apiuser.client.ui;

import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Home page.
 * 
 * @author Andras Belicza
 */
public class HomePage extends ApiUserPage {
	
    /**
     * Creates a new HomePage.
     */
    public HomePage() {
    	super( null, "apiuser/home" );
    }
    
    @Override
    public void buildGUI() {
		add( ClientUtils.createVerticalEmptyWidget( 30 ) );
		
		final HorizontalPanel greetingsPanel = new HorizontalPanel();
		add( greetingsPanel );
		
		if ( sharedApiAccountUserInfo == null ) {
			// Greeting
			if ( apiUserInfo.getLastVisit() == null ) {
				greetingsPanel.add( new Label( "Welcome " ) );
				greetingsPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
				greetingsPanel.add( ClientUtils.styledWidget( new Label( apiUserInfo.getUserName() ), "strong" ) );
				greetingsPanel.add( new Label( "!" ) );
			}
			else {
				greetingsPanel.add( new Label( "Welcome back" ) );
				greetingsPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
				greetingsPanel.add( ClientUtils.styledWidget( new Label( apiUserInfo.getUserName() ), "strong" ) );
				greetingsPanel.add( new Label( "! Your last visit was at:" ) );
				greetingsPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
				greetingsPanel.add( ClientUtils.createTimestampWidget( apiUserInfo.getLastVisit() ) );
			}
		}
		else {
			if ( sharedApiAccountUserInfo.isHasApiAccount() )
				greetingsPanel.add( new Label( "You are now viewing the" ) );
			else
				greetingsPanel.add( new Label( "You do NOT have access to the" ) );
			greetingsPanel.add( ClientUtils.createHorizontalEmptyWidget( 9 ) );
			greetingsPanel.add( ClientUtils.styledWidget( new Label( sharedApiAccountUserInfo.getSharedApiAccount() ), "strong" ) );
			greetingsPanel.add( ClientUtils.createHorizontalEmptyWidget( 9 ) );
			greetingsPanel.add( new Label( "API account!" ) );
		}
		
		// Replay parser engine version
		if ( apiUserInfo.getRepParserEngineVer() != null ) {
			add( ClientUtils.createVerticalEmptyWidget( 30 ) );
			final HorizontalPanel rowPanel = new HorizontalPanel();
			rowPanel.add( new Label( "Replay parser engine version:" ) );
			rowPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
			rowPanel.add( ClientUtils.styledWidget( new Label( apiUserInfo.getRepParserEngineVer() ), "strong" ) );
			add( rowPanel );
		}
		
		// Parsing service tester page
		add( ClientUtils.createVerticalEmptyWidget( 30 ) );
		add( new Anchor( "Parsing Service Tester page", "/parsing_service_tester.html", "_blank" ) );
		
		add( ClientUtils.createVerticalEmptyWidget( 70 ) );
		
		// Footer
		final HorizontalPanel linksPanel = new HorizontalPanel();
		linksPanel.setWidth( "500px" );
		DOM.setStyleAttribute( linksPanel.getElement(), "padding", "2px" );
		DOM.setStyleAttribute( linksPanel.getElement(), "borderTop", "1px solid #555555" );
		linksPanel.addStyleName( "noWrap" );
		linksPanel.add( new Anchor( "About the Service", "https://sites.google.com/site/sc2gears/parsing-service", "_blank" ) );
		linksPanel.add( ClientUtils.createHorizontalEmptyWidget( 10 ) );
		linksPanel.add( new Anchor( "Terms of Service", "https://sites.google.com/site/sc2gears/parsing-service#TOC-Terms-of-Service", "_blank" ) );
		linksPanel.add( ClientUtils.createHorizontalEmptyWidget( 10 ) );
		linksPanel.add( new Anchor( "Privacy Policy", "https://sites.google.com/site/sc2gears/parsing-service#TOC-Privacy-Policy", "_blank" ) );
		linksPanel.add( ClientUtils.createHorizontalEmptyWidget( 15 ) );
		final Label lastLabel = new Label( "© András Belicza, 2011-2014, Icons: Fugue" );
		DOM.setStyleAttribute( lastLabel.getElement(), "color", "#555555" );
		DOM.setStyleAttribute( lastLabel.getElement(), "paddingTop", "3px" );
		linksPanel.add( lastLabel );
		linksPanel.setCellWidth( lastLabel, "100%" );
		linksPanel.setCellHorizontalAlignment( lastLabel, HasHorizontalAlignment.ALIGN_RIGHT );
		for ( int i = linksPanel.getWidgetCount() - 1; i >= 0; i-- )
			DOM.setStyleAttribute( linksPanel.getWidget( i ).getElement(), "fontSize", "80%" );
		add( linksPanel );
    }
	
}
