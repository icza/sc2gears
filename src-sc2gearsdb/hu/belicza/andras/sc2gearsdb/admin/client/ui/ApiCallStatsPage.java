/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.admin.client.ui;

import hu.belicza.andras.sc2gearsdb.admin.client.Admin;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.ApiCallStatInfo;
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * API Call Stats page.
 * 
 * @author Andras Belicza
 */
public class ApiCallStatsPage extends HasApiCallStatInfoTablePage {
	
	/**
	 * A click handler which when clicked opens the google account acquired by the {@link Anchor#getText()} call.
	 * The {@link Object#toString()} or the handler returns a string which can be used as the tool tip for the target widget.
	 */
	public static final ClickHandler OPEN_GOOGLE_ACCOUNT_CLICK_HANDLER = new ClickHandler() {
		@Override
		public void onClick( final ClickEvent event ) {
			Admin.menu.setPage( new ApiCallStatsPage( ( (Anchor) event.getSource() ).getText() ) );
		}
		@Override
		public String toString() {
		    return "Show complete API call stats of this account.";
		}
	};
	
	/**
	 * Creates and returns a link with the text being the specified google account and which when clicked
	 * opens the complete API call stats of the specified account.
	 * @param googleAccount google account to create a link for
	 * @return the created new link
	 */
	public static Anchor createLinkForOpenGoogleAccount( final String googleAccount ) {
		final Anchor googleAccountAnchor = new Anchor( googleAccount );
		
		googleAccountAnchor.setTitle( OPEN_GOOGLE_ACCOUNT_CLICK_HANDLER.toString() );
		googleAccountAnchor.addClickHandler( OPEN_GOOGLE_ACCOUNT_CLICK_HANDLER );
		
		return googleAccountAnchor;
	}
	
	/** Google account to fill the Google account input field with. */
	private final String googleAccount;
	
    /**
     * Creates a new ApiCallStatsPage.
     */
    public ApiCallStatsPage() {
		this( null );
    }
    
    /**
     * Creates a new ApiCallStatsPage.
     * @param googleAccount Google account to fill the Google account input field with
     */
    public ApiCallStatsPage( final String googleAccount ) {
		super( "API Call Stats", "admin/apiCallStats" );
		this.googleAccount = googleAccount;
    }
    
    @Override
    public void buildGUI() {
		final HorizontalPanel filtersPanel = new HorizontalPanel();
		filtersPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		filtersPanel.add( new Label( "Google Account:" ) );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 2 ) );
		final TextBox googleAccountTextBox = new TextBox();
		googleAccountTextBox.setWidth( "220px" );
		if ( googleAccount != null )
			googleAccountTextBox.setText( googleAccount );
		filtersPanel.add( googleAccountTextBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 2 ) );
		final Button goButton = new Button( "Go" );
		ClientUtils.styleSmallButton( goButton );
		DOM.setStyleAttribute( goButton.getElement(), "fontWeight", "bold" );
		filtersPanel.add( goButton );
		ClientUtils.addEnterTarget( googleAccountTextBox, goButton );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 6 ) );
		final Button clearButton = new Button( "Clear" );
		ClientUtils.styleSmallButton( clearButton );
		clearButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				googleAccountTextBox.setText( "" );
			}
		} );
		filtersPanel.add( clearButton );
		add( filtersPanel );
		
		add( ClientUtils.createVerticalEmptyWidget( 7 ) );
		
		final FlexTable table = new FlexTable();
		
		goButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				googleAccountTextBox.setEnabled( false );
				goButton            .setEnabled( false );
				clearButton         .setEnabled( false );
				
				SERVICE_ASYNC.getApiCallStatInfoList( googleAccountTextBox.getText().trim(), new AsyncCallbackAdapter< List< ApiCallStatInfo > >( infoPanel ) {
					@Override
					public void customOnSuccess( final RpcResult< List< ApiCallStatInfo > > rpcResult ) {
						final List< ApiCallStatInfo > apiCallStatInfoList = rpcResult.getValue();
						if ( apiCallStatInfoList == null )
							return;
						
						buildApiCallStatInfoTable( apiCallStatInfoList, table, null );
					}
					@Override
					public void customOnEnd() {
						googleAccountTextBox.setEnabled( true );
						goButton            .setEnabled( true );
						clearButton         .setEnabled( true );
					}
				} );
			}
		} );
		
		if ( googleAccount != null )
			goButton.click();
    }
	
}
