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
import hu.belicza.andras.sc2gearsdb.common.client.Task;
import hu.belicza.andras.sc2gearsdb.user.client.Permission;
import hu.belicza.andras.sc2gearsdb.user.client.User;
import hu.belicza.andras.sc2gearsdb.user.client.beans.SettingsInfo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;

/**
 * Settings page.
 * 
 * @author Andras Belicza
 */
public class SettingsPage extends UserPage {
	
	/**
	 * Creates a new SettingsPage.
	 */
	public SettingsPage() {
		super( "Settings", "user/settings" );
	}
	
	@Override
	public void buildGUI() {
		if ( !checkPagePermission( Permission.VIEW_SETTINGS ) )
			return;
		
		SERVICE_ASYNC.getSettings( getSharedAccount(), new AsyncCallbackAdapter< SettingsInfo >( infoPanel ) {
			@Override
			public void customOnSuccess( final RpcResult< SettingsInfo > rpcResult ) {
				final SettingsInfo settingsInfo = rpcResult.getValue();
				if ( settingsInfo == null )
					return;
				
				add( ClientUtils.styledWidget( new Label( "General Settings" ), "h3" ) );
				
				final FlexTable table = new FlexTable();
				int row = -1;
				
				table.setWidget( ++row, 0, new Label( "Google account:" ) );
				final TextBox googleAccountTextBox = new TextBox();
				googleAccountTextBox.setText( settingsInfo.getGoogleAccount() );
				googleAccountTextBox.setReadOnly( true );
				table.setWidget( row, 1, googleAccountTextBox );
				table.setWidget( row, 2, ClientUtils.styledWidget( new Label( "Send an email to " + User.ADMIN_EMAIL + " if you want to change your Google account." ), "explanation" ) );
				
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
				
				table.setWidget( ++row, 0, new Label( "Notification quota level:" ) );
				final TextBox quotaLevelTextBox = new TextBox();
				quotaLevelTextBox.setText( Integer.toString( settingsInfo.getNotificationQuotaLevel() ) );
				quotaLevelTextBox.setAlignment( TextAlignment.RIGHT );
				table.setWidget( row, 1, quotaLevelTextBox );
				final HorizontalPanel descPanel = new HorizontalPanel();
				descPanel.add( new Label( "%" ) );
				descPanel.add( ClientUtils.createHorizontalEmptyWidget( 10 ) );
				descPanel.add( ClientUtils.styledWidget( new Label( "You will be notified via email if your storage quota exceeds this level. Valid range: 50..100%" ), "explanation" ) );
				table.setWidget( row, 2, descPanel );
				
				table.setWidget( ++row, 0, new Label( "Replay length display:" ) );
				final CheckBox convertToRealTimeCheckBox = new CheckBox( "Convert to real-time" );
				convertToRealTimeCheckBox.setValue( settingsInfo.isConvertToRealTime() );
				table.setWidget( row, 1, convertToRealTimeCheckBox );
				table.setWidget( row, 2, ClientUtils.styledWidget( new Label( "If checked, replay length divided by 1.38 will be displayed to reflect real-time." ), "explanation" ) );
				
				table.setWidget( ++row, 0, new Label( "Map image size:" ) );
				final ListBox mapImageSizeListBox = new ListBox();
				mapImageSizeListBox.setWidth( "100%" );
				for ( final String mapImageSizeString : User.MAP_IMAGE_SIZE_LABELS )
					mapImageSizeListBox.addItem( mapImageSizeString );
				mapImageSizeListBox.setSelectedIndex( settingsInfo.getMapImageSize() );
				table.setWidget( row, 1, mapImageSizeListBox );
				table.setWidget( row, 2, ClientUtils.styledWidget( new Label( "Specifies the size of map images on the Replays page." ), "explanation" ) );
				
				table.setWidget( ++row, 0, new Label( "Display replay winners:" ) );
				final ListBox displayWinnersListBox = new ListBox();
				displayWinnersListBox.setWidth( "100%" );
				for ( final String mapImageSizeString : User.DISPLAY_WINNER_LABELS )
					displayWinnersListBox.addItem( mapImageSizeString );
				displayWinnersListBox.setSelectedIndex( settingsInfo.getDisplayWinners() );
				table.setWidget( row, 1, displayWinnersListBox );
				table.setWidget( row, 2, ClientUtils.styledWidget( new Label( "Specifies how winners of replays are indicated on the Replays page." ), "explanation" ) );
				
				table.setWidget( ++row, 0, new Label( "Favored player list:" ) );
				final TextBox favoredPlayerListTextBox = new TextBox();
				favoredPlayerListTextBox.setTitle( "A comma separated list of player names. Case sensitive!" );
				if ( settingsInfo.getFavoredPlayerList() != null ) {
					final StringBuilder sb = new StringBuilder();
					for ( final String favoredPlayer : settingsInfo.getFavoredPlayerList() ) {
						if ( sb.length() > 0 )
							sb.append( ", " );
						sb.append( favoredPlayer );
					}
					favoredPlayerListTextBox.setText( sb.toString() );
				}
				table.setWidget( row, 1, favoredPlayerListTextBox );
				table.setWidget( row, 2, ClientUtils.styledWidget( new Label( "Optional. If provided, wins of these players will be colored to green and losses to red on the Replays page." ), "explanation" ) );
				
				ClientUtils.alignTableCells( table, HasHorizontalAlignment.ALIGN_LEFT );
				for ( int i = table.getRowCount() - 1; i >= 0; i-- )
					table.getRowFormatter().addStyleName( i, ( i & 0x01 ) == 0 ? "row0" : "row1"  );
				add( table );
				add( ClientUtils.createVerticalEmptyWidget( 5 ) );
				
				add( ClientUtils.styledWidget( new Label( "Database Account Sharing" ), "h3" ) );
				HorizontalPanel rowPanel = new HorizontalPanel();
				rowPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
				rowPanel.add( new Label( "Control who has access to your Database Account. Granted users:" ) );
				rowPanel.add( ClientUtils.createHorizontalEmptyWidget( 50 ) );
				final Button addGoogleAccountButton = new Button( "Add a Google Account..." );
				ClientUtils.setWidgetIcon( addGoogleAccountButton, "fugue/user--plus.png", 20, "#fa7" );
				rowPanel.add( addGoogleAccountButton );
				add( rowPanel );
				final FlexTable grantedUsersTable = new FlexTable();
				add( grantedUsersTable );
				rebuildGrantedUsersTable( grantedUsersTable, settingsInfo );
				if ( !checkPermission( Permission.CHANGE_SETTINGS ) )
					addGoogleAccountButton.setEnabled( false );
				else
    				addGoogleAccountButton.addClickHandler( new ClickHandler() {
    					@Override
    					public void onClick( final ClickEvent event ) {
    						final DialogBox dialogBox = new DialogBox( true );
    						dialogBox.setText( "Add a Google Account" );
    						dialogBox.setGlassEnabled( true );
    						final VerticalPanel content = new VerticalPanel();
    						content.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
    						content.add( ClientUtils.createVerticalEmptyWidget( 10 ) );
    						content.add( new Label( "Enter the Google Account you wish to grant access to your Database Account." ) );
    						content.add( new Label( "The granted user must possess a Database Account to access your Database Account." ) );
    						content.add( new Label( "Sharing will take effect when you save Settings. You can customize permissions on the next dialog." ) );
    						content.add( new Label( "The granted user will see your Database Account after he/she refreshes the page in his/her browser." ) );
    						content.add( ClientUtils.createVerticalEmptyWidget( 15 ) );
    						final HorizontalPanel rowPanel = new HorizontalPanel();
    						rowPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
    						rowPanel.add( ClientUtils.styledWidget( new Label( "Google Account Email address:" ), "strong" ) );
    						final TextBox googleAccountTextBox = new TextBox();
    						googleAccountTextBox.setWidth( "230px" );
    						rowPanel.add( googleAccountTextBox );
    						content.add( rowPanel );
    						content.add( ClientUtils.createVerticalEmptyWidget( 15 ) );
    						content.add( new Label( "Warning! Google Account is case sensitive! Ask the person how his/her Google Account is written!" ) );
    						content.add( ClientUtils.createVerticalEmptyWidget( 10 ) );
    						final HorizontalPanel buttonsPanel = new HorizontalPanel();
    						final Button okButton = new Button( "OK", new ClickHandler() {
    							@Override
    							public void onClick( final ClickEvent event ) {
    								googleAccountTextBox.setText( googleAccountTextBox.getText().trim() );
    								if ( googleAccountTextBox.getText().isEmpty() ) {
    									googleAccountTextBox.setFocus( true );
    									return;
    								}
    								settingsInfo.getGrantedUsers().add( googleAccountTextBox.getText() );
    								final long defaultPermission = Permission.permissionsOf( EnumSet.of( Permission.VIEW_REPLAYS, Permission.VIEW_MOUSE_PRINTS, Permission.VIEW_QUOTA ) );
    								settingsInfo.getGrantedPermissions().add( defaultPermission );
    								dialogBox.hide();
    								rebuildGrantedUsersTable( grantedUsersTable, settingsInfo );
    								// Launch permission dialog for the new user:
    								( (Button) grantedUsersTable.getWidget( settingsInfo.getGrantedUsers().size() - 1, 2 ) ).click();
    							}
    						} );
    						buttonsPanel.add( okButton );
    						ClientUtils.addEnterTarget( googleAccountTextBox, okButton );
    						buttonsPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
    						buttonsPanel.add( ClientUtils.createDialogCloseButton( dialogBox, "Cancel" ) );
    						content.add( buttonsPanel );
    						content.add( ClientUtils.createVerticalEmptyWidget( 10 ) );
    						dialogBox.setWidget( content );
    						dialogBox.center();
    						googleAccountTextBox.setFocus( true );
    					}
    				} );
				add( ClientUtils.createVerticalEmptyWidget( 10 ) );
				
				final Button saveButton = new Button( "Save Settings" );
				saveButton.addStyleName( "saveButton" );
				if ( !checkPermission( Permission.CHANGE_SETTINGS ) ) {
					googleAccountTextBox     .setEnabled( false );
					contactEmailTextBox      .setEnabled( false );
					nameTextBox              .setEnabled( false );
					quotaLevelTextBox        .setEnabled( false );
					convertToRealTimeCheckBox.setEnabled( false );
					mapImageSizeListBox      .setEnabled( false );
					displayWinnersListBox    .setEnabled( false );
					favoredPlayerListTextBox .setEnabled( false );
					saveButton               .setEnabled( false );
					for ( int i = grantedUsersTable.getRowCount() - 1; i >= 0; i-- )
						( (Button) grantedUsersTable.getWidget( i, 3 ) ).setEnabled( false );
				}
				else
    				saveButton.addClickHandler( new ClickHandler() {
    					@Override
    					public void onClick( final ClickEvent event ) {
    						contactEmailTextBox.setText( contactEmailTextBox.getText().trim() );
    						nameTextBox        .setText( nameTextBox        .getText().trim() );
    						quotaLevelTextBox  .setText( quotaLevelTextBox  .getText().trim() );
							
    						final SettingsInfo newSettingsInfo = new SettingsInfo();
    						newSettingsInfo.setContactEmail( contactEmailTextBox.getText() );
    						newSettingsInfo.setUserName    ( nameTextBox        .getText() );
    						try {
    							newSettingsInfo.setNotificationQuotaLevel( Integer.parseInt( quotaLevelTextBox.getText() ) );
    							if ( newSettingsInfo.getNotificationQuotaLevel() < 50 || newSettingsInfo.getNotificationQuotaLevel() > 100 )
    								throw new Exception();
    						} catch ( final Exception e ) {
    							infoPanel.setErrorMessage( "Invalid notification quota level! (Must be between 50 and 100!)" );
    							quotaLevelTextBox.setFocus( true );
    							return;
    						}
    						newSettingsInfo.setConvertToRealTime( convertToRealTimeCheckBox.getValue    () );
    						newSettingsInfo.setMapImageSize     ( mapImageSizeListBox.getSelectedIndex  () );
    						newSettingsInfo.setDisplayWinners   ( displayWinnersListBox.getSelectedIndex() );
    						
    						final String[] favoredPlayerTokens = favoredPlayerListTextBox.getText().split( "," );
    						if ( favoredPlayerTokens.length > 0 ) {
    							final List< String > favoredPlayerList = new ArrayList< String >();
    							for ( String favoredPlayer : favoredPlayerTokens ) {
    								favoredPlayer = favoredPlayer.trim();
    								if ( !favoredPlayer.isEmpty() )
    									favoredPlayerList.add( favoredPlayer );
    							}
    							if ( !favoredPlayerList.isEmpty() )
    								newSettingsInfo.setFavoredPlayerList( favoredPlayerList );
    						}
    						
    						newSettingsInfo.setGrantedUsers( settingsInfo.getGrantedUsers() );
    						newSettingsInfo.setGrantedPermissions( settingsInfo.getGrantedPermissions() );
    						
    						setComponentsEnabled( false );
    						SERVICE_ASYNC.saveSettings( getSharedAccount(), newSettingsInfo, new AsyncCallbackAdapter< Void >( infoPanel ) {
    							@Override
    							public void customOnSuccess( final RpcResult< Void > rpcResult ) {
    								if ( rpcResult.getErrorMsg() != null )
    									return;
									if ( getSharedAccount() != null )
										return;
									// Refresh cached settings...
									if ( newSettingsInfo.getUserName() == null || newSettingsInfo.getUserName().isEmpty() )
										userInfo.setUserName( userInfo.getUserNickname() );
									else
										userInfo.setUserName( newSettingsInfo.getUserName() );
									User.refreshLogoutLinkText();
									userInfo.setConvertToRealTime( newSettingsInfo.isConvertToRealTime () );
									userInfo.setMapImageSize     ( newSettingsInfo.getMapImageSize     () );
									userInfo.setDisplayWinners   ( newSettingsInfo.getDisplayWinners   () );
									userInfo.setFavoredPlayerList( newSettingsInfo.getFavoredPlayerList() );
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
    						quotaLevelTextBox        .setEnabled( enabled );
    						convertToRealTimeCheckBox.setEnabled( enabled );
    						mapImageSizeListBox      .setEnabled( enabled );
    						displayWinnersListBox    .setEnabled( enabled );
    						favoredPlayerListTextBox .setEnabled( enabled );
    						saveButton               .setEnabled( enabled );
    						addGoogleAccountButton   .setEnabled( enabled );
    						for ( int i = grantedUsersTable.getRowCount() - 1; i >= 0; i-- ) {
    							( (Button) grantedUsersTable.getWidget( i, 2 ) ).setEnabled( enabled );
    							( (Button) grantedUsersTable.getWidget( i, 3 ) ).setEnabled( enabled );
    						}
    					}
    				} );
				add( saveButton );
			}
		} );
	}
	
	private void rebuildGrantedUsersTable( final FlexTable grantedUsersTable, final SettingsInfo settingsInfo ) {
		grantedUsersTable.removeAllRows();
		
		final List< String > grantedUsers       = settingsInfo.getGrantedUsers();
		final List< Long >   grantedPermissions = settingsInfo.getGrantedPermissions();
		
		final boolean haveChangeSettingsPermission = checkPermission( Permission.CHANGE_SETTINGS );
			
		final Permission[] PERMISSIONS = Permission.values();
		
		for ( int i = 0; i < grantedUsers.size(); i++ ) {
			final int row = i;
			grantedUsersTable.setWidget( i, 0, new Label( (i+1) + "." ) );
			grantedUsersTable.setWidget( i, 1, ClientUtils.styledWidget( new Label( grantedUsers.get( i ) ), "strong" ) );
			final Button permissionsButton = new Button();
			permissionsButton.setWidth( "100%" );
			final Task refreshPermissionButtonLabelTask = new Task() {
				{ execute(); /* Set initial button text*/ }
				@Override
				public void execute() {
					final Long grantedPermission = grantedPermissions.get( row );
					int grantedPermissionsCount = 0;
					for ( final Permission permission : PERMISSIONS )
						if ( permission.contained( grantedPermission ) )
							grantedPermissionsCount++;
					permissionsButton.setText( "Permissions: " + grantedPermissionsCount + "/" + PERMISSIONS.length );
				}
			};
			permissionsButton.addClickHandler( new ClickHandler() {
				@Override
				public void onClick( final ClickEvent event ) {
					final DialogBox dialogBox = new DialogBox( true );
					dialogBox.setText( "Permissions for " + grantedUsers.get( row ) );
					dialogBox.setGlassEnabled( true );
					final VerticalPanel content = new VerticalPanel();
					final CheckBox[] permissionCheckboxes = new CheckBox[ PERMISSIONS.length ];
					final Long grantedPermission = grantedPermissions.get( row );
					for ( int i = 0; i < permissionCheckboxes.length; i++ ) {
						if ( i == 0 || !PERMISSIONS[ i ].group.equals( PERMISSIONS[ i - 1 ].group ) ) {
							content.add( ClientUtils.createVerticalEmptyWidget( 8 ) );
							content.add( ClientUtils.styledWidget( new Label( PERMISSIONS[ i ].group ), "strong" ) );
						}
						content.add( permissionCheckboxes[ i ] = new CheckBox( PERMISSIONS[ i ].displayName ) );
						permissionCheckboxes[ i ].setValue( PERMISSIONS[ i ].contained( grantedPermission ) );
						if ( !haveChangeSettingsPermission )
							permissionCheckboxes[ i ].setEnabled( false );
					}
					content.add( ClientUtils.createVerticalEmptyWidget( 10 ) );
					
					final HorizontalPanel buttonsPanel = new HorizontalPanel();
					final Button okButton = new Button( "OK", new ClickHandler() {
						@Override
						public void onClick( final ClickEvent event ) {
							if ( !haveChangeSettingsPermission )
								return; // Just for safety
							long grantedPermission = 0;
							for ( int i = 0; i < permissionCheckboxes.length; i++ )
								if ( permissionCheckboxes[ i ].getValue() )
									grantedPermission = PERMISSIONS[ i ].addTo( grantedPermission );
							grantedPermissions.set( row, grantedPermission );
							dialogBox.hide();
							refreshPermissionButtonLabelTask.execute();
						}
					} );
					if ( !haveChangeSettingsPermission )
						okButton.setEnabled( false );
					buttonsPanel.add( okButton );
					buttonsPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
					buttonsPanel.add( ClientUtils.createDialogCloseButton( dialogBox, "Cancel" ) );
					content.add( buttonsPanel );
					content.setCellHorizontalAlignment( buttonsPanel, HasHorizontalAlignment.ALIGN_CENTER );
					content.add( ClientUtils.createVerticalEmptyWidget( 5 ) );
					dialogBox.setWidget( content );
					dialogBox.center();
				}
			} );
			permissionsButton.addStyleName( "permButton" );
			grantedUsersTable.setWidget( i, 2, permissionsButton );
			final Button removeButton = new Button( "Remove" );
			removeButton.addClickHandler(  new ClickHandler() {
				@Override
				public void onClick( final ClickEvent event ) {
					grantedUsers.remove( row );
					grantedPermissions.remove( row );
					// Rebuild table because first column (index) changes.
					rebuildGrantedUsersTable( grantedUsersTable, settingsInfo );
				}
			} );
			removeButton.addStyleName( "delButton" );
			grantedUsersTable.setWidget( i, 3, removeButton );
		}
	}
	
}
