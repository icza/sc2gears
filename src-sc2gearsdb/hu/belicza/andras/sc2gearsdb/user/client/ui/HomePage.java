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
import hu.belicza.andras.sc2gearsdb.user.client.beans.QuotaStatus;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
public class HomePage extends UserPage {
	
	private static String[] TIPS = {
		"You can change how winners of replays are displayed (or not displayed) on the Settings page.",
		"You can click on the map images on the Replays page to see a bigger version of them.",
		"You can change the size of map images of the Replays page or completely hide them on the Settings page.",
		"You don't have to refresh the page in your browser to see new uploaded files, just click on the page name again in the menu bar.",
		"You can enter multiple player names separated with commas in the 'Players' filter on the Replays page, and only replays containing all the listed players will be displayed.",
		"You can share your Database Account with yourself, and you can test what others will see and be able to do with your Database Account if you grant the same permissions to them.",
		"You can append an 'account' URL parameter to the address specifying an account, and it will be automatically selected. Example: '/User.html?account=BigJoe@gmail.com'",
		"On the Replays page you can color replays to green and red based on your wins and losses if you add your name to the Favored player list on the Settings page.",
		"You can also view and edit replay comments and labels directly from Sc2gears on the 'Private data' tab of the Replay analyzer.",
		"You don't have to worry about duplicate uploads. If a file you're uploading (e.g. a replay) is already stored, the Sc2gears Database will recognize it and will not store it duplicated.",
		"You can also download your stored files directly from Sc2gears using the 'Sc2gears Database downloader' tool in the Tools menu.",
		"You can check your quota directly from Sc2gears by selecting the 'Sc2gears Database User quota' channel on the Start page window."
	};
	
	private final boolean refreshQuotaStatus;
	
	/**
	 * Creates a new HomePage.
	 */
	public HomePage() {
		this( false );
	}
	
	/**
	 * Creates a new HomePage.
	 * @param refreshQuotaStatus tells if quota status has to be refreshed
	 */
	public HomePage( final boolean refreshQuotaStatus ) {
		super( null, "user/home" );
		
		this.refreshQuotaStatus = refreshQuotaStatus;
	}
	
	@Override
	public void buildGUI() {
		// No page title is displayed, insert a filler widget with height having the padding of page titles  
		insert( ClientUtils.createVerticalEmptyWidget( 7 ), 0 );
		
		add( ClientUtils.createVerticalEmptyWidget( 30 ) );
		
		final HorizontalPanel greetingsPanel = new HorizontalPanel();
		add( greetingsPanel );
		
		if ( userInfo.getUserName() == null ) {
			greetingsPanel.add( ClientUtils.styledWidget( new Label( "You are not logged in!" ), "strong" ) );
			return;
		}
		
		if ( sharedAccountUserInfo == null ) {
			// Greeting
			if ( userInfo.getLastVisit() == null ) {
				greetingsPanel.add( new Label( "Welcome " ) );
				greetingsPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
				greetingsPanel.add( ClientUtils.styledWidget( new Label( userInfo.getUserName() ), "strong" ) );
				greetingsPanel.add( new Label( "!" ) );
			}
			else {
				greetingsPanel.add( new Label( "Welcome back" ) );
				greetingsPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
				greetingsPanel.add( ClientUtils.styledWidget( new Label( userInfo.getUserName() ), "strong" ) );
				greetingsPanel.add( new Label( "! Your last visit was at:" ) );
				greetingsPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
				greetingsPanel.add( ClientUtils.createTimestampWidget( userInfo.getLastVisit() ) );
			}
		}
		else {
			if ( sharedAccountUserInfo.isHasAccount() )
				greetingsPanel.add( new Label( "You are now viewing the" ) );
			else
				greetingsPanel.add( new Label( "You do NOT have access to the" ) );
			greetingsPanel.add( ClientUtils.createHorizontalEmptyWidget( 9 ) );
			greetingsPanel.add( ClientUtils.styledWidget( new Label( sharedAccountUserInfo.getSharedAccount() ), "strong" ) );
			greetingsPanel.add( ClientUtils.createHorizontalEmptyWidget( 9 ) );
			greetingsPanel.add( new Label( "Database account!" ) );
		}
		
		if ( checkPermission( Permission.VIEW_QUOTA ) ) {
			// Storage quota status
			add( ClientUtils.createVerticalEmptyWidget( 30 ) );
			final HorizontalPanel storageStatusPanel = new HorizontalPanel();
			add( storageStatusPanel );
			final Task displayQuotaStatusTask = new Task() {
				@Override
				public void execute() {
					final QuotaStatus quotaStatus = sharedAccountUserInfo == null ? userInfo.getQuotaStatus() : sharedAccountUserInfo.getQuotaStatus();
					storageStatusPanel.clear();
					storageStatusPanel.add( new Label( "Storage quota status:" ) );
					storageStatusPanel.add( ClientUtils.createHorizontalEmptyWidget( 7 ) );
					if ( quotaStatus == null ) {
						storageStatusPanel.add( new Label( "-" ) );
						return;
					}
					final long quotaLevel  = quotaStatus.getPaidStorage() * quotaStatus.getNotificationQuotaLevel() / 100;
					final int  usedPercent = quotaStatus.getPaidStorage() == 0 ? 0 : (int) ( quotaStatus.getUsedStorage() * 100 / quotaStatus.getPaidStorage() );
					final Anchor quotaStatusLink;
					if ( quotaStatus.getUsedStorage() < quotaLevel ) {
						quotaStatusLink = new Anchor( "OK (" + usedPercent + "% used)" );
						ClientUtils.setWidgetIcon( quotaStatusLink, "fugue/tick.png", 19, null );
					} else if ( quotaStatus.getPaidStorage() - quotaStatus.getUsedStorage() < 1000000 ) {
						quotaStatusLink = ClientUtils.styledWidget( new Anchor( "WARNING - Exceeded storage level! (" + usedPercent + "% used)" ), "errorMsg" );
						ClientUtils.setWidgetIcon( quotaStatusLink, "fugue/exclamation-red.png", 19, null );
					} else {
						quotaStatusLink = ClientUtils.styledWidget( new Anchor( "WARNING - Above notification level! (" + usedPercent + "% used)" ), "errorMsg" );
						ClientUtils.setWidgetIcon( quotaStatusLink, "fugue/exclamation.png", 19, null );
					}
					quotaStatusLink.addClickHandler( new ClickHandler() {
						@Override
						public void onClick( final ClickEvent event ) {
							User.menu.setPage( new QuotaPage() );
						}
					} );
					storageStatusPanel.add( quotaStatusLink );
				}
			};
			displayQuotaStatusTask.execute();
			if ( refreshQuotaStatus ) {
				final String sharedAccount = getSharedAccount();
				SERVICE_ASYNC.getStorageQuotaStatus( sharedAccount, new AsyncCallbackAdapter< QuotaStatus >( infoPanel ) {
					@Override
					public void customOnSuccess( final RpcResult< QuotaStatus > rpcResult ) {
						final QuotaStatus quotaStatus = rpcResult.getValue();
						// Refresh even if no quota status arrived back due to an error...
						( sharedAccount == null ? userInfo : sharedAccountUserInfo ).setQuotaStatus( quotaStatus );
						displayQuotaStatusTask.execute();
					}
				} );
			}
		}
		
		// Random tip
		add( ClientUtils.createVerticalEmptyWidget( 50 ) );
		final Anchor randomTipLink = new Anchor( "Random Tip" );
		ClientUtils.setWidgetIcon( randomTipLink, "fugue/light-bulb.png", null, null );
		final Label tipLabel = new Label();
		tipLabel.setWidth( "600px" );
		add( randomTipLink );
		add( tipLabel );
		randomTipLink.addClickHandler( new ClickHandler() {
			private int tipIdx = (int) ( Math.random() * TIPS.length );
			{ onClick( null ); } // Initialize tip labels
			@Override
			public void onClick( final ClickEvent event ) {
				randomTipLink.setText( "Random Tip (" + (tipIdx+1) + "/" + TIPS.length + ")" );
				tipLabel.setText( TIPS[ tipIdx ] );
				if ( ++tipIdx == TIPS.length )
					tipIdx = 0;
			}
		} );
		
		add( ClientUtils.createVerticalEmptyWidget( 70 ) );
		
		// Footer
		final HorizontalPanel linksPanel = new HorizontalPanel();
		linksPanel.setWidth( "670px" );
		DOM.setStyleAttribute( linksPanel.getElement(), "padding", "2px" );
		DOM.setStyleAttribute( linksPanel.getElement(), "borderTop", "1px solid #555555" );
		linksPanel.addStyleName( "noWrap" );
		linksPanel.add( new Anchor( "About the Service", "https://sites.google.com/site/sc2gears/sc2gears-database", "_blank" ) );
		linksPanel.add( ClientUtils.createHorizontalEmptyWidget( 10 ) );
		linksPanel.add( new Anchor( "FAQ", "https://sites.google.com/site/sc2gears/faq#TOC-Sc2gears-Database-FAQ", "_blank" ) );
		linksPanel.add( ClientUtils.createHorizontalEmptyWidget( 10 ) );
		linksPanel.add( new Anchor( "Terms of Service", "https://sites.google.com/site/sc2gears/sc2gears-database#TOC-Terms-of-Service", "_blank" ) );
		linksPanel.add( ClientUtils.createHorizontalEmptyWidget( 10 ) );
		linksPanel.add( new Anchor( "Privacy Policy", "https://sites.google.com/site/sc2gears/sc2gears-database#TOC-Privacy-Policy", "_blank" ) );
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
