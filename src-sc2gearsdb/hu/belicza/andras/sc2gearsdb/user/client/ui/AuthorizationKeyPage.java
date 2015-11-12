/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.user.client.ui;

import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.user.client.Permission;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

/**
 * Authorization Key page.
 * 
 * @author Andras Belicza
 */
public class AuthorizationKeyPage extends UserPage {
	
	/**
	 * Creates a new AuthorizationKeyPage.
	 */
	public AuthorizationKeyPage() {
		super( "Authorization Key", "user/authorizationKey" );
	}
	
	@Override
	public void buildGUI() {
		if ( !checkPagePermission( Permission.VIEW_AUTHORIZATION_KEY ) )
			return;
		
		SERVICE_ASYNC.getAuthorizationKey( getSharedAccount(), new AsyncCallbackAdapter< String >( infoPanel ) {
			@Override
			public void customOnSuccess( final RpcResult< String > rpcResult ) {
				final String authorizationKey = rpcResult.getValue();
				if ( authorizationKey == null )
					return;
				
				add( ClientUtils.createVerticalEmptyWidget( 5 ) );
				add( new Label( "Your current Authorization key: " ) );
				add( ClientUtils.createVerticalEmptyWidget( 15 ) );
				final Label authorizationKeylabel = new Label( authorizationKey );
				authorizationKeylabel.addStyleName( "authKey" );
				add( authorizationKeylabel );
				add( ClientUtils.createVerticalEmptyWidget( 15 ) );
				
				final Button generateNewKeyButton = new Button( "Generate new Authorization key" );
				ClientUtils.setWidgetIcon( generateNewKeyButton, "fugue/license-key.png", null, "#ffaa77" );
				if ( !checkPermission( Permission.CHANGE_AUTHORIZATION_KEY ) )
					generateNewKeyButton.setEnabled( false );
				else
					generateNewKeyButton.addClickHandler( new ClickHandler() {
						@Override
						public void onClick( final ClickEvent event ) {
							generateNewKeyButton.setEnabled( false );
							SERVICE_ASYNC.generateNewAuthorizationKey( getSharedAccount(), new AsyncCallbackAdapter< String >( infoPanel ) {
								@Override
								public void customOnSuccess( final RpcResult< String > rpcResult ) {
									final String newAuthorizationKey = rpcResult.getValue();
									if ( newAuthorizationKey != null )
										authorizationKeylabel.setText( newAuthorizationKey );
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
				final Label noteLabel = new Label( "Note: If you generate a new Authorization key, you have to set the new key in your Sc2gears, else calls that require an Authorization key (like storing replays) will be rejected. By generating a new Authorization key the old key becomes invalid." );
				noteLabel.setWidth( "600px" );
				add( noteLabel );
			}
		} );
	}
	
}
