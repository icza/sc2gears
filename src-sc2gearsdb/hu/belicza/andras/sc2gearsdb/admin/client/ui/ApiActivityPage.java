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

import hu.belicza.andras.sc2gearsdb.admin.client.beans.ApiCallStatInfo;
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * API Activity page.
 * 
 * @author Andras Belicza
 */
public class ApiActivityPage extends HasApiCallStatInfoTablePage {
	
	private static final DateTimeFormat DAY_FORMAT = DateTimeFormat.getFormat( "yyyy-MM-dd" );
	
    /**
     * Creates a new ApiActivityPage.
     */
    public ApiActivityPage() {
		super( "API Activity", "admin/apiActivity" );
    }
    
    @Override
    public void buildGUI() {
		final HorizontalPanel filtersPanel = new HorizontalPanel();
		filtersPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		filtersPanel.add( new Label( "First day:" ) );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 2 ) );
		final TextBox firstDayTextBox = new TextBox();
		firstDayTextBox.setWidth( "75px" );
		filtersPanel.add( firstDayTextBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		filtersPanel.add( new Label( "Last day:" ) );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 2 ) );
		final TextBox lastDayTextBox = new TextBox();
		lastDayTextBox.setWidth( "75px" );
		filtersPanel.add( lastDayTextBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		final Button goButton = new Button( "Go" );
		ClientUtils.styleSmallButton( goButton );
		DOM.setStyleAttribute( goButton.getElement(), "fontWeight", "bold" );
		filtersPanel.add( goButton );
		ClientUtils.addEnterTarget( lastDayTextBox, goButton );
		
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 15 ) );
		final String[][] predefinedPeriodLabels = { { "Yesterday", "1", "1" }, { "Today", "0", "0" }, { "Last 2 Days", "0", "1" }, { "Last 3 Days", "0", "2" }, { "Last 7 Days", "0", "6" } };
		final Button[] predefinedPeridoButtons = new Button[ predefinedPeriodLabels.length ];
		final ClickHandler predefinedPeriodClickHandler = new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				final Object source = event.getSource();
				for ( int i = 0; i < predefinedPeridoButtons.length; i++ )
					if ( source == predefinedPeridoButtons[ i ] ) {
						lastDayTextBox .setText( DAY_FORMAT.format( new Date( System.currentTimeMillis() - 24L*60*60*1000 * Integer.parseInt( predefinedPeriodLabels[ i ][ 1 ] ) ) ) );
						firstDayTextBox.setText( DAY_FORMAT.format( new Date( System.currentTimeMillis() - 24L*60*60*1000 * Integer.parseInt( predefinedPeriodLabels[ i ][ 2 ] ) ) ) );
						goButton.click();
					}
			}
		};
		for ( int i = 0; i < predefinedPeridoButtons.length; i++ ) {
			predefinedPeridoButtons[ i ] = new Button( predefinedPeriodLabels[ i ][ 0 ] );
			predefinedPeridoButtons[ i ].addClickHandler( predefinedPeriodClickHandler );
			ClientUtils.styleSmallButton( predefinedPeridoButtons[ i ] );
			filtersPanel.add( predefinedPeridoButtons[ i ] );
		}
		add( filtersPanel );
		
		add( ClientUtils.createVerticalEmptyWidget( 7 ) );
		
		final FlexTable apiCallStatTable = new FlexTable();
		
		goButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				final String firstDay = firstDayTextBox.getText();
				try {
					DAY_FORMAT.parse( firstDay );
				} catch ( final IllegalArgumentException iae ) {
					infoPanel.setErrorMessage( "Invalid First day!" );
					firstDayTextBox.setFocus( true );
					return;
				}
				final String lastDay = lastDayTextBox.getText();
				try {
					DAY_FORMAT.parse( lastDay );
				} catch ( final IllegalArgumentException iae ) {
					lastDayTextBox.setFocus( true );
					infoPanel.setErrorMessage( "Invalid Last day!" );
					return;
				}
				
				firstDayTextBox.setEnabled( false );
				lastDayTextBox .setEnabled( false );
				goButton.setEnabled( false );
				for ( final Button button : predefinedPeridoButtons )
					button.setEnabled( false );
				
				SERVICE_ASYNC.getApiActivity( firstDay, lastDay, new AsyncCallbackAdapter< List< ApiCallStatInfo > >( infoPanel ) {
					@Override
					public void customOnSuccess( final RpcResult< List< ApiCallStatInfo > > rpcResult ) {
						final List< ApiCallStatInfo > apiCallStatInfoList = rpcResult.getValue();
						if ( apiCallStatInfoList == null )
							return;
						
						buildApiCallStatInfoTable( apiCallStatInfoList, apiCallStatTable, ApiCallStatsPage.OPEN_GOOGLE_ACCOUNT_CLICK_HANDLER );
					}
					@Override
					public void customOnEnd() {
						firstDayTextBox.setEnabled( true );
						lastDayTextBox .setEnabled( true );
						goButton.setEnabled( true );
						for ( final Button button : predefinedPeridoButtons )
							button.setEnabled( true );
					}
				} );
			}
		} );
		
		// Load today:
		predefinedPeridoButtons[ 1 ].click();
    }
	
}
