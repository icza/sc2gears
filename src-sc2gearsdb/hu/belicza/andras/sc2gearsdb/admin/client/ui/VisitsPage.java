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

import hu.belicza.andras.sc2gearsdb.admin.client.AdminService.VisitType;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.VisitInfo;
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;

/**
 * Visits page.
 * 
 * @author Andras Belicza
 */
public class VisitsPage extends AdminPage {
	
    /**
     * Creates a new ActivityPage.
     */
    public VisitsPage() {
		super( "Visits", "admin/visits" );
    }
    
    @Override
    public void buildGUI() {
		final int MAX_HOURS = 336;
		
		final HorizontalPanel filtersPanel = new HorizontalPanel();
		filtersPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		filtersPanel.add( new Label( "Type:" ) );
		final ListBox typeListBox = new ListBox();
    	for ( final VisitType visitType : VisitType.values() )
    		typeListBox.addItem( visitType.stringValue );
		filtersPanel.add( typeListBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		filtersPanel.add( new Label( "Filter:" ) );
		final ListBox filterListBox = new ListBox();
		filterListBox.addItem( "All" );
		filterListBox.addItem( "Has Account" );
		filterListBox.addItem( "No Account" );
		filtersPanel.add( filterListBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		filtersPanel.add( new Label( "In the last" ) );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 3 ) );
		final TextBox hoursTextBox = new TextBox();
		hoursTextBox.setAlignment( TextAlignment.RIGHT );
		hoursTextBox.setText( "2" );
		hoursTextBox.setWidth( "30px" );
		hoursTextBox.setTitle( "Max " + MAX_HOURS + " hours" );
		filtersPanel.add( hoursTextBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 3 ) );
		filtersPanel.add( new Label( "hours" ) );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		final Button goButton = new Button( "Go" );
		ClientUtils.styleSmallButton( goButton );
		DOM.setStyleAttribute( goButton.getElement(), "fontWeight", "bold" );
		filtersPanel.add( goButton );
		ClientUtils.addEnterTarget( hoursTextBox, goButton );
		
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 10 ) );
		final String[][] predefinedTimeLabels = { { "1 Hour", "1" }, { "2 Hours", "2" }, { "4 Hours", "4" }, { "8 Hours", "8" }, { "Day", "24" }, { "2 Days", "48" }, { "3 Days ", "72" } };
		final Button[] predefinedTimeButtons = new Button[ predefinedTimeLabels.length ];
		final ClickHandler predefinedHoursClickHandler = new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				final Object source = event.getSource();
				for ( int i = 0; i < predefinedTimeButtons.length; i++ )
					if ( source == predefinedTimeButtons[ i ] ) {
						hoursTextBox.setText( predefinedTimeLabels[ i ][ 1 ] );
						goButton.click();
					}
			}
		};
		for ( int i = 0; i < predefinedTimeButtons.length; i++ ) {
			predefinedTimeButtons[ i ] = new Button( predefinedTimeLabels[ i ][ 0 ] );
			predefinedTimeButtons[ i ].addClickHandler( predefinedHoursClickHandler );
			ClientUtils.styleSmallButton( predefinedTimeButtons[ i ] );
			filtersPanel.add( predefinedTimeButtons[ i ] );
		}
		add( filtersPanel );
		
		add( ClientUtils.createVerticalEmptyWidget( 7 ) );
		
		final FlexTable visitsTable = new FlexTable();
		
		goButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				int hours;
				try {
    				hours = Integer.parseInt( hoursTextBox.getText() );
    				if ( hours < 1 || hours > MAX_HOURS )
    					throw new NumberFormatException();
				} catch ( final NumberFormatException nfe ) {
					hoursTextBox.setFocus( true );
					infoPanel.setErrorMessage( "Invalid entered hours (must be between 1 and " + MAX_HOURS + ")!" );
					return;
				}
				
				typeListBox  .setEnabled( false );
				filterListBox.setEnabled( false );
				hoursTextBox .setEnabled( false );
				goButton     .setEnabled( false );
				for ( final Button button : predefinedTimeButtons )
					button.setEnabled( false );
				
				final Boolean hasAccountFilter = filterListBox.getSelectedIndex() == 0 ? null : filterListBox.getSelectedIndex() == 1 ? Boolean.TRUE : Boolean.FALSE;
				final VisitType visitType = VisitType.values()[ typeListBox.getSelectedIndex() ];
				SERVICE_ASYNC.getVisitInfoList( visitType, hours, hasAccountFilter, new AsyncCallbackAdapter< List< VisitInfo > >( infoPanel ) {
					@Override
					public void customOnSuccess( final RpcResult< List< VisitInfo > > rpcResult ) {
						final List< VisitInfo > visitInfoList = rpcResult.getValue();
						if ( visitInfoList == null )
							return;
						
						// Build info table
						if ( visitsTable.getRowCount() == 0 ) {
							// Init table
							visitsTable.setBorderWidth( 1 );
							visitsTable.setCellSpacing( 0 );
							visitsTable.setCellPadding( 3 );
							add( visitsTable );
						}
						else
							visitsTable.removeAllRows();
						
						final RowFormatter rowFormatter = visitsTable.getRowFormatter();
						
						int column = 0;
						visitsTable.setWidget( 0, column++, new Label( "#"          ) );
						visitsTable.setWidget( 0, column++, new Label( "Date"       ) );
						visitsTable.setWidget( 0, column++, new Label( "User"       ) );
						visitsTable.setWidget( 0, column++, new Label( "Location"   ) );
						visitsTable.setWidget( 0, column++, new Label( "IP"         ) );
						visitsTable.setWidget( 0, column++, new Label( "User Agent" ) );
						rowFormatter.addStyleName( 0, "headerRow" );
						
						int row = 1;
						for ( final VisitInfo visitInfo : visitInfoList ) {
							column = 0;
							
							visitsTable.setWidget( row, column++, new Label( row + "."                                      ) );
							visitsTable.setWidget( row, column++, ClientUtils.styledWidget( ClientUtils.createTimestampWidget( visitInfo.getDate() ), "noWrap" ) );
							if ( visitInfo.isHasAccount() ) {
								final Anchor googleAccountAnchor;
								switch ( visitType ) {
								case VISIT     : googleAccountAnchor = FileStatsPage   .createLinkForOpenGoogleAccount( visitInfo.getGoogleAccount() ); break;
								case API_VISIT : googleAccountAnchor = ApiCallStatsPage.createLinkForOpenGoogleAccount( visitInfo.getGoogleAccount() ); break;
								default        : googleAccountAnchor = null;
								}
								visitsTable.setWidget( row, column++, googleAccountAnchor );
							}
							else
								visitsTable.setWidget( row, column++, new Label( visitInfo.getGoogleAccount() ) );
							visitsTable.setWidget( row, column++, new Label( visitInfo.getLocation() ) );
							final Label ipLabel = new Label( visitInfo.getIp() );
							if ( visitInfo.getIp() != null && visitInfo.getIp().indexOf( ':' ) >= 0 ) {
								// IPv6, longer, make font size smaller...
								ClientUtils.setWidgetFontSize( ipLabel, "70%" );
								// IPv6 is 16 bytes, 8 groups, groups separated by a colon
								// Insert a space after the 4th colon so the label can be broken into lines...
								int idx = 0;
								for ( int i = 0; i < 4 && idx >= 0; i++ )
									idx = visitInfo.getIp().indexOf( ':', idx ) + 1;
								if ( idx >= 0 )
									ipLabel.setText( new StringBuilder( visitInfo.getIp() ).insert( idx, ' ' ).toString() );
							}
							visitsTable.setWidget( row, column++, ipLabel );
							final Label userAgentLabel = ClientUtils.setWidgetFontSize( new Label( visitInfo.getUserAgent() ), "70%" );
							userAgentLabel.setTitle( visitInfo.getUserAgent() );
							visitsTable.setWidget( row, column++, userAgentLabel );
							
							visitsTable.getFlexCellFormatter().setHorizontalAlignment( row, 0, HasHorizontalAlignment.ALIGN_RIGHT );
							rowFormatter.addStyleName( row, ( row & 0x01 ) == 0 ? "row0" : "row1" );
							
							row++;
						}
					}
					@Override
					public void customOnEnd() {
						typeListBox  .setEnabled( true );
						filterListBox.setEnabled( true );
						hoursTextBox .setEnabled( true );
						goButton     .setEnabled( true );
						for ( final Button button : predefinedTimeButtons )
							button.setEnabled( true );
					}
				} );
			}
		} );
		
		final ChangeHandler goChangeHandler = new ChangeHandler() {
			@Override
			public void onChange( final ChangeEvent event ) {
				goButton.click();
			}
		};
		typeListBox  .addChangeHandler( goChangeHandler );
		filterListBox.addChangeHandler( goChangeHandler );
		
		goButton.click();
    }
	
}
