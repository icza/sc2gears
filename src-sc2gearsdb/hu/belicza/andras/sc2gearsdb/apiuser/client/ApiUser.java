/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.apiuser.client;

import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.ApiUserInfo;
import hu.belicza.andras.sc2gearsdb.apiuser.client.ui.ApiCallStatsPage;
import hu.belicza.andras.sc2gearsdb.apiuser.client.ui.ApiKeyPage;
import hu.belicza.andras.sc2gearsdb.apiuser.client.ui.HomePage;
import hu.belicza.andras.sc2gearsdb.apiuser.client.ui.QuotaAndPaymentsPage;
import hu.belicza.andras.sc2gearsdb.apiuser.client.ui.SettingsPage;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.Menu;
import hu.belicza.andras.sc2gearsdb.common.client.Page;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * API user page entry point.
 * 
 * @author Andras Belicza
 */
public class ApiUser implements EntryPoint {
	
	public static final String ADMIN_EMAIL = new String( new char[] { 'i', 'c', 'z', 'a', 'a', 'a', '@', 'g', 'm', 'a', 'i', 'l', '.', 'c', 'o', 'm' } );
	
	public static final ApiUserServiceAsync SERVICE_ASYNC = GWT.create( ApiUserService.class );
	
	private static final VerticalPanel   mainPanel = new VerticalPanel();
	private static final HorizontalPanel menuPanel = new HorizontalPanel();
	
	private static Anchor LOGOUT_LINK;
	
	private static Menu menu;
	
	public static ApiUserInfo apiUserInfo;
	public static ApiUserInfo sharedAccountApiUserInfo;
	
	@Override
	public void onModuleLoad() {
		mainPanel.setWidth( "100%" );
		DOM.setStyleAttribute( mainPanel.getElement(), "marginBottom", "10px" );
		mainPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		mainPanel.add( ClientUtils.styledWidget( new Label( "Sc2gears™ Database API User Page" ), "h1" ) );
		final HorizontalPanel accountChooserPanel = new HorizontalPanel();
		accountChooserPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		mainPanel.add( accountChooserPanel );
		mainPanel.add( ClientUtils.createVerticalEmptyWidget( 5 ) );
		menuPanel.add( ClientUtils.createCheckingUserWidget() );
		mainPanel.add( menuPanel );
		RootPanel.get().add( mainPanel );
		
		SERVICE_ASYNC.getApiUserInfo( null, new AsyncCallback< RpcResult< ApiUserInfo > >() {
			@Override
			public void onSuccess( final RpcResult< ApiUserInfo > rpcResult ) {
				ApiUser.apiUserInfo = rpcResult.getValue();
				menuPanel.clear();
				if ( apiUserInfo.getUserName() == null ) {
					mainPanel.add( new HTML( "<p>You are not logged in. You can login <a href=\"" + apiUserInfo.getLoginUrl() + "\">here</a>.</p>" ) );
					return;
				}
				
				LOGOUT_LINK = ClientUtils.createAndSetupLogoutLink( "Logout " + apiUserInfo.getUserName(), apiUserInfo.getLogoutUrl() );
				
				if ( !apiUserInfo.isHasApiAccount() )
					mainPanel.add( new HTML( "<center><p><span style=\"color:red\">There is no Sc2gears Database API account associated with this Google account: <b>"
						+ apiUserInfo.getUserName()
						+ "</b></span><br/>(Google account is case sensitive!)</p>"
						+ "<p><br/>Read about how to get an API account <a href=\"https://sites.google.com/site/sc2gears/parsing-service\" target=\"_blank\">here</a>.</p></center>" ) );
				else {
					if ( apiUserInfo.isAdmin() ) {
						accountChooserPanel.add( new Label( "View API Account:" ) );
						final ListBox sharedAccountListBox = new ListBox();
						sharedAccountListBox.addStyleName( "ownAccount" );
						int idx = 0;
						for ( final String sharedAccount : apiUserInfo.getSharedAccounts() )
							sharedAccountListBox.addItem( ( idx++ == 0 ? "YOU - " : "SHARED - " ) + sharedAccount );
						accountChooserPanel.add( sharedAccountListBox );
						final ChangeHandler sharedAccountChangeHandler = new ChangeHandler() {
							@Override
							public void onChange( final ChangeEvent event ) {
								if ( sharedAccountListBox.getSelectedIndex() == 0 ) {
									sharedAccountListBox.removeStyleName( "sharedAccount" );
									sharedAccountListBox.addStyleName( "ownAccount" );
									sharedAccountApiUserInfo = null;
									// Go to home page
									menu.setPage( new HomePage() );
								}
								else {
									sharedAccountListBox.removeStyleName( "ownAccount" );
									sharedAccountListBox.addStyleName( "sharedAccount" );
									
									final DialogBox dialogBox = createWaitingDialog( "Switching API Account..." );
									SERVICE_ASYNC.getApiUserInfo( apiUserInfo.getSharedAccounts().get( sharedAccountListBox.getSelectedIndex() ), new AsyncCallback< RpcResult< ApiUserInfo > >() {
										@Override
										public void onSuccess( final RpcResult< ApiUserInfo > rpcResult ) {
											dialogBox.hide();
											sharedAccountApiUserInfo = rpcResult.getValue();
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
					}
					
					menu = new Menu( MenuItem.values(), mainPanel );
					menuPanel.add( menu );
					// Go to home page
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
		HOME              ( "Home"            , HomePage            .class ),
		API_CALL_STATS    ( "API Call Stats"  , ApiCallStatsPage    .class ),
		QUOTA_AND_PAYMENTS( "Quota & Payments", QuotaAndPaymentsPage.class ),
		API_KEY           ( "API Key"         , ApiKeyPage          .class ),
		SETTINGS          ( "Settings"        , SettingsPage        .class );
		
		/** Display label of the menu item.            */
		public String                  label;
		/** Page class associated with this menu item. */
		public Class< ? extends Page > pageClass;
		
        /**
         * Creates a new ApiUser.MenuItem.
         * @param label     display label of the menu item
         * @param pageClass page class associated with this menu item
         */
        private MenuItem( final String label, final Class< ? extends Page > pageClass ) {
        	this.label     = label;
        	this.pageClass = pageClass;
        }
        
        @Override
        public void onActivate( final Menu menu ) {
        	switch ( this ) {
        	case HOME               : menu.setPage( new HomePage            () ); break;
        	case API_CALL_STATS     : menu.setPage( new ApiCallStatsPage    () ); break;
        	case QUOTA_AND_PAYMENTS : menu.setPage( new QuotaAndPaymentsPage() ); break;
        	case API_KEY            : menu.setPage( new ApiKeyPage          () ); break;
        	case SETTINGS           : menu.setPage( new SettingsPage        () ); break;
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
	
	public static void refreshLogoutMenuLabel() {
		LOGOUT_LINK.setText( "Logout " + apiUserInfo.getUserName() );
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
