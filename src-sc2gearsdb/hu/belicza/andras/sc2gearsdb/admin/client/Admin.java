/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.admin.client;

import hu.belicza.andras.sc2gearsdb.admin.client.beans.UserInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.ui.ActivityPage;
import hu.belicza.andras.sc2gearsdb.admin.client.ui.ApiActivityPage;
import hu.belicza.andras.sc2gearsdb.admin.client.ui.ApiCallStatsPage;
import hu.belicza.andras.sc2gearsdb.admin.client.ui.CreateAccountPage;
import hu.belicza.andras.sc2gearsdb.admin.client.ui.DlStatsPage;
import hu.belicza.andras.sc2gearsdb.admin.client.ui.FileStatsPage;
import hu.belicza.andras.sc2gearsdb.admin.client.ui.HomePage;
import hu.belicza.andras.sc2gearsdb.admin.client.ui.MiscPage;
import hu.belicza.andras.sc2gearsdb.admin.client.ui.RegisterPaymentPage;
import hu.belicza.andras.sc2gearsdb.admin.client.ui.VisitsPage;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.Menu;
import hu.belicza.andras.sc2gearsdb.common.client.Page;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Admin page entry point.
 * 
 * @author Andras Belicza
 */
public class Admin implements EntryPoint {
	
	public static final AdminServiceAsync SERVICE_ASYNC = GWT.create( AdminService.class );
	
	private static final VerticalPanel   mainPanel = new VerticalPanel();
	private static final HorizontalPanel menuPanel = new HorizontalPanel();
	
	public static Menu menu;
	
	private static UserInfo userInfo;
	
	@Override
	public void onModuleLoad() {
		mainPanel.setWidth( "100%" );
		DOM.setStyleAttribute( mainPanel.getElement(), "marginBottom", "10px" );
		mainPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		mainPanel.add( ClientUtils.styledWidget( new Label( "Sc2gears™ Database Admin Page" ), "h1" ) );
		menuPanel.add( ClientUtils.createCheckingUserWidget() );
		mainPanel.add( menuPanel );
		RootPanel.get().add( mainPanel );
		
		SERVICE_ASYNC.getUserInfo( new AsyncCallback< RpcResult< UserInfo > >() {
			@Override
			public void onSuccess( final RpcResult< UserInfo > rpcResult ) {
				Admin.userInfo = rpcResult.getValue();
				menuPanel.clear();
				if ( userInfo.getUserName() == null )
					menuPanel.add( new HTML( "You are not logged in. You can login <a href=\"" + userInfo.getLoginUrl() + "\">here</a>." ) );
				else if ( !userInfo.isAdmin() )
					menuPanel.add( new HTML( "You are not authorized to view this page. You can logout <a href=\"" + userInfo.getLogoutUrl() + "\">here</a>." ) );
				else {
					ClientUtils.createAndSetupLogoutLink( "Logout " + userInfo.getUserName(), userInfo.getLogoutUrl() );
					final MenuItem[] MENU_ITEMS = MenuItem.values();
					menu = new Menu( MENU_ITEMS, mainPanel );
					// Too many menu items, menu is too big, make it smaller:
					for ( int i = 0; i < MENU_ITEMS.length; i++ )
						menu.setTabHTML( i, "<span style='font-size:80%'>" + MENU_ITEMS[ i ].label + "</span>" );
					menuPanel.add( menu );
					// Go to home page
					menu.setPage( new HomePage() );
				}
			}
			@Override
			public void onFailure( final Throwable caught ) {
				menuPanel.clear();
				menuPanel.add( new Label( "Failed to reach the server. Try refreshing the page." ) );
			}
		} );
	}
	
	/**
	 * Menu items.
	 * 
	 * @author Andras Belicza
	 */
	private static enum MenuItem implements Menu.MenuItem {
		CREATE_ACCOUNT  ( "Create Acc."   , CreateAccountPage  .class ),
		REGISTER_PAYMENT( "Reg. Payment"  , RegisterPaymentPage.class ),
		ACTIVITY        ( "Activity"      , ActivityPage       .class ),
		VISITS          ( "Visits"        , VisitsPage         .class ),
		FILE_STATS      ( "File Stats"    , FileStatsPage      .class ),
		DL_STATS        ( "DL Stats"      , DlStatsPage        .class ),
		API_ACTIVITY    ( "API Activity"  , ApiActivityPage    .class ),
		API_CALL_STATS  ( "API Call Stats", ApiCallStatsPage   .class ),
		MISC            ( "Misc"          , MiscPage           .class );
		
		/** Display label of the menu item.            */
		public String                  label;
		/** Page class associated with this menu item. */
		public Class< ? extends Page > pageClass;
		
        /**
         * Creates a new Admin.MenuItem.
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
        	case CREATE_ACCOUNT   : menu.setPage( new CreateAccountPage  () ); break;
        	case REGISTER_PAYMENT : menu.setPage( new RegisterPaymentPage() ); break;
        	case ACTIVITY         : menu.setPage( new ActivityPage       () ); break;
        	case VISITS           : menu.setPage( new VisitsPage         () ); break;
        	case FILE_STATS       : menu.setPage( new FileStatsPage      () ); break;
        	case DL_STATS         : menu.setPage( new DlStatsPage        () ); break;
        	case API_ACTIVITY     : menu.setPage( new ApiActivityPage    () ); break;
        	case API_CALL_STATS   : menu.setPage( new ApiCallStatsPage   () ); break;
        	case MISC             : menu.setPage( new MiscPage           () ); break;
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
	
}
