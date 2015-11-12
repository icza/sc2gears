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

import hu.belicza.andras.sc2gearsdb.apiuser.client.ApiUser;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.SettingsInfo;
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;

/**
 * Settings page.
 * 
 * @author Andras Belicza
 */
public class SettingsPage extends ApiUserPage {
	
    /**
     * Creates a new SettingsPage.
     */
    public SettingsPage() {
    	super( "Settings", "apiuser/settings" );
    }
    
    @Override
    public void buildGUI() {
		SERVICE_ASYNC.getSettings( getSharedApiAccount(), new AsyncCallbackAdapter< SettingsInfo >( infoPanel ) {
			@Override
			public void customOnSuccess( final RpcResult< SettingsInfo > rpcResult ) {
				final SettingsInfo settingsInfo = rpcResult.getValue();
				if ( settingsInfo == null )
					return;
				
				final FlexTable table = new FlexTable();
				int row = -1;
				
				table.setWidget( ++row, 0, new Label( "Google account:" ) );
				final TextBox googleAccountTextBox = new TextBox();
				googleAccountTextBox.setText( settingsInfo.getGoogleAccount() );
				googleAccountTextBox.setReadOnly( true );
				table.setWidget( row, 1, googleAccountTextBox );
				table.setWidget( row, 2, ClientUtils.styledWidget( new Label( "Send an email to " + ApiUser.ADMIN_EMAIL + " if you want to change your Google account." ), "explanation" ) );
				
				table.setWidget( ++row, 0, new Label( "Contact email:" ) );
				final TextBox contactEmailTextBox = new TextBox();
				contactEmailTextBox.setText( settingsInfo.getContactEmail() );
				table.setWidget( row, 1, contactEmailTextBox );
				table.setWidget( row, 2, ClientUtils.styledWidget( new Label( "Optional. If provided, emails will be sent both to your Google account and to your contact email." ), "explanation" ) );
				
				table.setWidget( ++row, 0, new Label( "Name:" ) );
				final TextBox nameTextBox = new TextBox();
				nameTextBox.setText( settingsInfo.getUserName() );
				table.setWidget( row, 1, nameTextBox );
				table.setWidget( row, 2, ClientUtils.styledWidget( new Label( "Optional. If provided, you will be addressed by this name." ), "explanation" ) );
				
				table.setWidget( ++row, 0, new Label( "Notification available Ops:" ) );
				final TextBox quotaAvailOpsTextBox = new TextBox();
				quotaAvailOpsTextBox.setText( Long.toString( settingsInfo.getNotificationAvailOps() ) );
				quotaAvailOpsTextBox.setAlignment( TextAlignment.RIGHT );
				table.setWidget( row, 1, quotaAvailOpsTextBox );
				table.setWidget( row, 2, ClientUtils.styledWidget( new Label( "You will be notified via email if your available Ops decreases to this amount or below." ), "explanation" ) );
				
				ClientUtils.alignTableCells( table, HasHorizontalAlignment.ALIGN_LEFT );
				for ( int i = table.getRowCount() - 1; i >= 0; i-- )
					table.getRowFormatter().addStyleName( i, ( i & 0x01 ) == 0 ? "row0" : "row1"  );
				add( table );
				add( ClientUtils.createVerticalEmptyWidget( 3 ) );
				
				final Button saveButton = new Button( "Save settings" );
				saveButton.addStyleName( "saveButton" );
				saveButton.addClickHandler( new ClickHandler() {
					@Override
					public void onClick( final ClickEvent event ) {
						final SettingsInfo newSettingsInfo = new SettingsInfo();
						newSettingsInfo.setContactEmail( contactEmailTextBox.getText() );
						newSettingsInfo.setUserName    ( nameTextBox        .getText() );
						try {
							newSettingsInfo.setNotificationAvailOps( Long.parseLong( quotaAvailOpsTextBox.getText() ) );
							if ( newSettingsInfo.getNotificationAvailOps() < 0 )
								throw new Exception();
						} catch ( final Exception e ) {
							infoPanel.setErrorMessage( "Invalid notification available Ops! (Must be equal to or greater than 0!)" );
							quotaAvailOpsTextBox.setFocus( true );
							return;
						}
						
						setComponentsEnabled( false );
						SERVICE_ASYNC.saveSettings( getSharedApiAccount(), newSettingsInfo, new AsyncCallbackAdapter< Void >( infoPanel ) {
							@Override
							public void customOnSuccess( final RpcResult< Void > rpcResult ) {
								if ( rpcResult.getErrorMsg() != null )
									return;
								if ( newSettingsInfo.getUserName() == null || newSettingsInfo.getUserName().isEmpty() )
									apiUserInfo.setUserName( apiUserInfo.getUserNickname() );
								else
									apiUserInfo.setUserName( newSettingsInfo.getUserName() );
								ApiUser.refreshLogoutMenuLabel();
							}
							@Override
							public void customOnEnd() {
								setComponentsEnabled( true );
							};
						} );
					}
					
					private void setComponentsEnabled( final boolean enabled ) {
						googleAccountTextBox     .setEnabled( enabled );
						contactEmailTextBox      .setEnabled( enabled );
						nameTextBox              .setEnabled( enabled );
						quotaAvailOpsTextBox     .setEnabled( enabled );
						saveButton               .setEnabled( enabled );
					}
				} );
				add( saveButton );
			}
		} );
    }
	
}
