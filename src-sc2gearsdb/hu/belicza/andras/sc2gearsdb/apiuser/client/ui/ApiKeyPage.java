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

import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

/**
 * API Key page.
 * 
 * @author Andras Belicza
 */
public class ApiKeyPage extends ApiUserPage {
	
    /**
     * Creates a new ApiKeyPage.
     */
    public ApiKeyPage() {
    	super( "API Key", "apiuser/apiKey" );
    }
    
    @Override
    public void buildGUI() {
		SERVICE_ASYNC.getApiKey( getSharedApiAccount(), new AsyncCallbackAdapter< String >( infoPanel ) {
			@Override
			public void customOnSuccess( final RpcResult< String > rpcResult ) {
				final String apiKey = rpcResult.getValue();
				if ( apiKey == null )
					return;
				
				add( ClientUtils.createVerticalEmptyWidget( 5 ) );
				add( new Label( "Your current API key: " ) );
				add( ClientUtils.createVerticalEmptyWidget( 15 ) );
				final Label apiKeylabel = new Label( apiKey );
				apiKeylabel.addStyleName( "authKey" );
				add( apiKeylabel );
				add( ClientUtils.createVerticalEmptyWidget( 15 ) );
				
				final Button generateNewKeyButton = new Button( "Generate new API key" );
				ClientUtils.setWidgetIcon( generateNewKeyButton, "fugue/license-key.png", null, "#fa7" );
				generateNewKeyButton.addClickHandler( new ClickHandler() {
					@Override
					public void onClick( final ClickEvent event ) {
						generateNewKeyButton.setEnabled( false );
						SERVICE_ASYNC.generateNewApiKey( getSharedApiAccount(), new AsyncCallbackAdapter< String >( infoPanel ) {
							@Override
							public void customOnSuccess( final RpcResult< String > rpcResult ) {
								final String newApiKey = rpcResult.getValue();
								if ( newApiKey != null ) {
									infoPanel.setInfoMessage( "New API key successfully created." );
									apiKeylabel.setText( newApiKey );
								}
							}
							@Override
							public void customOnEnd() {
								generateNewKeyButton.setEnabled( true );
							};
						} );
					}
				} );
				add( generateNewKeyButton );
				
				add( ClientUtils.createVerticalEmptyWidget( 25 ) );
				final Label noteLabel = new Label( "Note: If you generate a new API key, you have to use the new API key in all your requests, else your requests will be rejected (with HTTP status code 403 - FORBIDDEN). By generating a new API key the old key becomes invalid." );
				noteLabel.setWidth( "600px" );
				add( noteLabel );
			}
		} );
    }
	
}
