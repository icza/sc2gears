/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.user.client;

import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.Menu;
import hu.belicza.andras.sc2gearsdb.common.client.Page;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.user.client.beans.UserInfo;
import hu.belicza.andras.sc2gearsdb.user.client.ui.AuthorizationKeyPage;
import hu.belicza.andras.sc2gearsdb.user.client.ui.HomePage;
import hu.belicza.andras.sc2gearsdb.user.client.ui.MousePrintsPage;
import hu.belicza.andras.sc2gearsdb.user.client.ui.OtherFilesPage;
import hu.belicza.andras.sc2gearsdb.user.client.ui.PaymentsPage;
import hu.belicza.andras.sc2gearsdb.user.client.ui.QuotaPage;
import hu.belicza.andras.sc2gearsdb.user.client.ui.RegistrationPage;
import hu.belicza.andras.sc2gearsdb.user.client.ui.ReplaysPage;
import hu.belicza.andras.sc2gearsdb.user.client.ui.SettingsPage;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * User page entry point.
 * 
 * @author Andras Belicza
 */
public class User implements EntryPoint {
	
	public static final String   ADMIN_EMAIL           = new String( new char[] { 'i', 'c', 'z', 'a', 'a', 'a', '@', 'g', 'm', 'a', 'i', 'l', '.', 'c', 'o', 'm' } );
	
	public static final String[] MAP_IMAGE_SIZE_LABELS = { "Hidden", "Tiny (16 pixels)", "Small (24 pixels)", "Medium (32 pixels)", "Large (48 pixels)", "X-Large (64 pixels)" };
	public static final String[] MAP_IMAGE_SIZE_VALUES = { ""      , "16px"            , "24px"             , "32px"              , "48px"             , "64px"                };
	
	public static final String[] DISPLAY_WINNER_LABELS = { "Hide", "Icons", "Colored names", "Icons + Colored names" };
	
	public static final UserServiceAsync SERVICE_ASYNC = GWT.create( UserService.class );
	
	private static final VerticalPanel   mainPanel = new VerticalPanel();
	private static final HorizontalPanel menuPanel = new HorizontalPanel();
	
	public static Menu menu;
	
	private static Anchor LOGOUT_LINK;
	
	public static UserInfo userInfo;
	public static UserInfo sharedAccountUserInfo;
	
	@Override
	public void onModuleLoad() {
		mainPanel.setWidth( "100%" );
		DOM.setStyleAttribute( mainPanel.getElement(), "marginBottom", "10px" );
		mainPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		mainPanel.add( ClientUtils.styledWidget( new Label( "Sc2gears™ Database User Page" ), "h1" ) );
		mainPanel.add( new HTML( "<p style=\"color:red;font-weight:bold\">The Sc2gears™ Database is shutting down!</p>" ) );
		final HorizontalPanel accountChooserPanel = new HorizontalPanel();
		accountChooserPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		mainPanel.add( accountChooserPanel );
		mainPanel.add( ClientUtils.createVerticalEmptyWidget( 5 ) );
		menuPanel.add( ClientUtils.createCheckingUserWidget() );
		mainPanel.add( menuPanel );
		RootPanel.get().add( mainPanel );
		
		SERVICE_ASYNC.getUserInfo( null, new AsyncCallback< RpcResult< UserInfo > >() {
			@Override
			public void onSuccess( final RpcResult< UserInfo > rpcResult ) {
				User.userInfo = rpcResult.getValue();
				menuPanel.clear();
				if ( userInfo.getUserName() == null ) {
					mainPanel.add( new HTML( "<p>You are not logged in. You can login <a href=\"" + userInfo.getLoginUrl() + "\">here</a>.</p>" ) );
					return;
				}
				
				LOGOUT_LINK = ClientUtils.createAndSetupLogoutLink( "Logout " + userInfo.getUserName(), userInfo.getLogoutUrl() );
				
				if ( !userInfo.isHasAccount() ) {
					mainPanel.add( new HTML( "<p style=\"color:red\">There is no Sc2gears Database account associated with this Google account: <b>" + userInfo.getUserName() + "</b></p>" ) );
					mainPanel.add( new HTML( "<p>Note: if you are here to view a Database account shared with you, in order for you to access it" +
							"<br/>you also have to possess a Database account (which can be a free account or any of the paid accounts).</p>" ) );
					
					final RegistrationPage registrationPage = new RegistrationPage();
					mainPanel.add( registrationPage );
					registrationPage.buildGUI();
				}
				else {
					accountChooserPanel.add( new Label( "View Database Account:" ) );
					final ListBox sharedAccountListBox = new ListBox();
					sharedAccountListBox.addStyleName( "ownAccount" );
					int idx = 0;
					for ( final String sharedAccount : userInfo.getSharedAccounts() )
						sharedAccountListBox.addItem( ( idx++ == 0 ? "YOU - " : "SHARED - " ) + sharedAccount );
					accountChooserPanel.add( sharedAccountListBox );
					final ChangeHandler sharedAccountChangeHandler = new ChangeHandler() {
						@Override
						public void onChange( final ChangeEvent event ) {
							if ( sharedAccountListBox.getSelectedIndex() == 0 ) {
								sharedAccountListBox.removeStyleName( "sharedAccount" );
								sharedAccountListBox.addStyleName( "ownAccount" );
								sharedAccountUserInfo = null;
								updateMenuItemStates();
								// Go to home page
								menu.setPage( new HomePage() );
							}
							else {
								sharedAccountListBox.removeStyleName( "ownAccount" );
								sharedAccountListBox.addStyleName( "sharedAccount" );
								
								final DialogBox dialogBox = createWaitingDialog( "Switching Database Account..." );
								SERVICE_ASYNC.getUserInfo( userInfo.getSharedAccounts().get( sharedAccountListBox.getSelectedIndex() ), new AsyncCallback< RpcResult< UserInfo > >() {
									@Override
									public void onSuccess( final RpcResult< UserInfo > rpcResult ) {
										dialogBox.hide();
										sharedAccountUserInfo = rpcResult.getValue();
										updateMenuItemStates();
										// Go to home page
										menu.setPage( new HomePage() );
									}
									@Override
									public void onFailure( final Throwable caught ) {
										dialogBox.hide();
										Window.alert( "Failed to reach the server. Try refreshing the page." );
									}
								} );
							}
						}
					};
					sharedAccountListBox.addChangeHandler( sharedAccountChangeHandler );
					accountChooserPanel.add( ClientUtils.styledWidget( new Label( "List of DB accounts shared with you." ), "explanation" ) );
					if ( userInfo.isAdmin() ) {
						accountChooserPanel.add( ClientUtils.createHorizontalEmptyWidget( 3 ) );
    					final Button addNewDbAccountButton = new Button( "+" );
    					addNewDbAccountButton.addStyleName( "sharedAccount" );
    					DOM.setStyleAttribute( addNewDbAccountButton.getElement(), "paddingTop"   , "1px" );
    					DOM.setStyleAttribute( addNewDbAccountButton.getElement(), "paddingBottom", "1px" );
    					addNewDbAccountButton.setTitle( "Add a new Account to the list..." );
    					addNewDbAccountButton.addClickHandler( new ClickHandler() {
							@Override
							public void onClick( final ClickEvent event ) {
	    						final DialogBox dialogBox = new DialogBox( true );
	    						dialogBox.setText( "Add a new Account to the list" );
	    						dialogBox.setGlassEnabled( true );
	    						final VerticalPanel content = new VerticalPanel();
	    						content.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
	    						content.add( ClientUtils.createVerticalEmptyWidget( 10 ) );
	    						final HorizontalPanel rowPanel = new HorizontalPanel();
	    						rowPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
	    						rowPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
	    						rowPanel.add( new Label( "Google Account Email address to add:" ) );
	    						final TextBox googleAccountTextBox = new TextBox();
	    						googleAccountTextBox.setWidth( "260px" );
	    						rowPanel.add( googleAccountTextBox );
	    						rowPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
	    						content.add( rowPanel );
	    						content.add( ClientUtils.createVerticalEmptyWidget( 5 ) );
	    						final HorizontalPanel buttonsPanel = new HorizontalPanel();
	    						final Button okButton = new Button( "OK", new ClickHandler() {
	    							@Override
	    							public void onClick( final ClickEvent event ) {
	    								googleAccountTextBox.setText( googleAccountTextBox.getText().trim() );
	    								if ( googleAccountTextBox.getText().isEmpty() ) {
	    									googleAccountTextBox.setFocus( true );
	    									return;
	    								}
	    								dialogBox.hide();
	    								userInfo.getSharedAccounts().add( googleAccountTextBox.getText() );
	    								sharedAccountListBox.addItem( "[+] - " + googleAccountTextBox.getText() );
	    								// Select this new account:
	    								sharedAccountListBox.setSelectedIndex( sharedAccountListBox.getItemCount() - 1 );
	    								sharedAccountChangeHandler.onChange( null );
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
    					accountChooserPanel.add( addNewDbAccountButton );
					}
					menu = new Menu( MenuItem.values(), mainPanel );
					menuPanel.add( menu );
					boolean activateHomePage = true;
					final String accountToSelect = Window.Location.getParameter( "account" );
					if ( accountToSelect != null ) {
						idx = 0;
						for ( final String sharedAccount : userInfo.getSharedAccounts() ) {
							if ( accountToSelect.equals( sharedAccount ) ) {
								activateHomePage = false;
								sharedAccountListBox.setSelectedIndex( idx );
								sharedAccountChangeHandler.onChange( null );
								break;
							}
							idx++;
						}
					}
					if ( activateHomePage )
						menu.setPage( new HomePage() );
				}
			}
			@Override
			public void onFailure( final Throwable caught ) {
				mainPanel.add( new Label( "Failed to reach the server. Try refreshing the page." ) );
			}
		} );
	}
	
	/**
	 * Menu items.
	 * 
	 * @author Andras Belicza
	 */
	private static enum MenuItem implements Menu.MenuItem {
		HOME             ( null                             , "Home"             , HomePage            .class ),
		REPLAYS          ( Permission.VIEW_REPLAYS          , "Replays"          , ReplaysPage         .class ),
		MOUSE_PRINTS     ( Permission.VIEW_MOUSE_PRINTS     , "Mouse Prints"     , MousePrintsPage     .class ),
		OTHER_FILES      ( Permission.VIEW_OTHER_FILES      , "Other Files"      , OtherFilesPage      .class ),
		QUOTA            ( Permission.VIEW_QUOTA            , "Quota"            , QuotaPage           .class ),
		PAYMENTS         ( Permission.VIEW_PAYMENTS         , "Payments"         , PaymentsPage        .class ),
		AUTHORIZATION_KEY( Permission.VIEW_AUTHORIZATION_KEY, "Authorization Key", AuthorizationKeyPage.class ),
		SETTINGS         ( Permission.VIEW_SETTINGS         , "Settings"         , SettingsPage        .class );
		
		/** Permission required for the menu item.     */
		public final Permission        permission;
		/** Display label of the menu item.            */
		public String                  label;
		/** Page class associated with this menu item. */
		public Class< ? extends Page > pageClass;
		
        /**
         * Creates a new User.MenuItem.
         * @param permission permission required for the menu item
         * @param label      display label of the menu item
         * @param pageClass  page class associated with this menu item
         */
        private MenuItem( final Permission permission, final String label, final Class< ? extends Page > pageClass ) {
        	this.permission = permission;
        	this.label      = label;
        	this.pageClass  = pageClass;
        }
        
        @Override
        public void onActivate( final Menu menu ) {
        	switch ( this ) {
        	case HOME              : menu.setPage( new HomePage            ( true ) ); break;
        	case REPLAYS           : menu.setPage( new ReplaysPage         () ); break;
        	case MOUSE_PRINTS      : menu.setPage( new MousePrintsPage     () ); break;
        	case OTHER_FILES       : menu.setPage( new OtherFilesPage      () ); break;
        	case QUOTA             : menu.setPage( new QuotaPage           () ); break;
        	case PAYMENTS          : menu.setPage( new PaymentsPage        () ); break;
        	case AUTHORIZATION_KEY : menu.setPage( new AuthorizationKeyPage() ); break;
        	case SETTINGS          : menu.setPage( new SettingsPage        () ); break;
        	}
        }
        
        @Override
        public String getLabel() {
	        return label;
        }
        
        @Override
        public Class< ? extends Page > getPageClass() {
	        return pageClass;
        }
	}
	
	public static void refreshLogoutLinkText() {
		LOGOUT_LINK.setText( "Logout " + userInfo.getUserName() );
	}
	
	private static void updateMenuItemStates() {
		final long grantedPermissions = sharedAccountUserInfo == null ? 0 : sharedAccountUserInfo.getGrantedPermissions();
		for ( final MenuItem menuItem : MenuItem.values() )
			menu.setTabEnabled( menuItem.ordinal(), sharedAccountUserInfo == null || menuItem.permission == null || menuItem.permission.contained( grantedPermissions ) );
	}
	
	private static DialogBox createWaitingDialog( final String message ) {
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText( "Info" );
		
		final HorizontalPanel hp = new HorizontalPanel();
		DOM.setStyleAttribute( hp.getElement(), "padding", "20px" );
		hp.setHeight( "20px" );
		hp.add( new Image( "/images/loading.gif" ) );
		hp.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		hp.add( new Label( message ) );
		dialogBox.setWidget( hp );
		
		dialogBox.center();
		
		return dialogBox;
	}
	
}
