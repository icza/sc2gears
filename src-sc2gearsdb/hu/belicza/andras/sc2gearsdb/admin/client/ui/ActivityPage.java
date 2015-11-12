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

import hu.belicza.andras.sc2gearsdb.admin.client.beans.ActivityInfo;
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;

/**
 * Activity page.
 * 
 * @author Andras Belicza
 */
public class ActivityPage extends HasFileStatInfoTablePage {
	
    /**
     * Creates a new ActivityPage.
     */
    public ActivityPage() {
		super( "Activity", "admin/activity" );
    }
    
    @Override
    public void buildGUI() {
		final int MAX_MINUTES = 10080;
		
		final HorizontalPanel filtersPanel = new HorizontalPanel();
		filtersPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		filtersPanel.add( new Label( "In the last" ) );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 3 ) );
		final TextBox minutesTextBox = new TextBox();
		minutesTextBox.setAlignment( TextAlignment.RIGHT );
		minutesTextBox.setText( "60" );
		minutesTextBox.setWidth( "40px" );
		minutesTextBox.setTitle( "Max " + MAX_MINUTES + " min" );
		filtersPanel.add( minutesTextBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 3 ) );
		filtersPanel.add( new Label( "minutes" ) );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		final Button goButton = new Button( "Go" );
		ClientUtils.styleSmallButton( goButton );
		DOM.setStyleAttribute( goButton.getElement(), "fontWeight", "bold" );
		filtersPanel.add( goButton );
		ClientUtils.addEnterTarget( minutesTextBox, goButton );
		
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 15 ) );
		final String[][] predefinedTimeLabels = { { "10 min", "10" }, { "30 min", "30" }, { "Hour", "60" }, { "2 Hours", "120" }, { "4 Hours", "240" }, { "8 Hours", "480" }, { "Day", "1440" } };
		final Button[] predefinedTimeButtons = new Button[ predefinedTimeLabels.length ];
		final ClickHandler predefinedMinutesClickHandler = new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				final Object source = event.getSource();
				for ( int i = 0; i < predefinedTimeButtons.length; i++ )
					if ( source == predefinedTimeButtons[ i ] ) {
						minutesTextBox.setText( predefinedTimeLabels[ i ][ 1 ] );
						goButton.click();
					}
			}
		};
		for ( int i = 0; i < predefinedTimeButtons.length; i++ ) {
			predefinedTimeButtons[ i ] = new Button( predefinedTimeLabels[ i ][ 0 ] );
			predefinedTimeButtons[ i ].addClickHandler( predefinedMinutesClickHandler );
			ClientUtils.styleSmallButton( predefinedTimeButtons[ i ] );
			filtersPanel.add( predefinedTimeButtons[ i ] );
		}
		add( filtersPanel );
		
		add( ClientUtils.createVerticalEmptyWidget( 7 ) );
		
		final FlexTable infoTable = new FlexTable();
		
		final FlexTable fileStatTable = new FlexTable();
		
		goButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				int minutes;
				try {
    				minutes = Integer.parseInt( minutesTextBox.getText() );
    				if ( minutes < 1 || minutes > MAX_MINUTES )
    					throw new NumberFormatException();
				} catch ( final NumberFormatException nfe ) {
					minutesTextBox.setFocus( true );
					infoPanel.setErrorMessage( "Invalid entered minutes (must be between 1 and " + MAX_MINUTES + ")!" );
					return;
				}
				
				minutesTextBox.setEnabled( false );
				goButton.setEnabled( false );
				for ( final Button button : predefinedTimeButtons )
					button.setEnabled( false );
				
				SERVICE_ASYNC.getActivityInfo( minutes, new AsyncCallbackAdapter< ActivityInfo >( infoPanel ) {
					@Override
					public void customOnSuccess( final RpcResult< ActivityInfo > rpcResult ) {
						final ActivityInfo activityInfo = rpcResult.getValue();
						if ( activityInfo == null )
							return;
						
						// Build info table
						if ( infoTable.getRowCount() == 0 ) {
							// Init table
							infoTable.setBorderWidth( 1 );
							infoTable.setCellSpacing( 0 );
							infoTable.setCellPadding( 3 );
							int col = 0, row = 0;
							infoTable.setWidget( row++, col, new Label( "Visits:" ) );
							infoTable.setWidget( row++, col, new Label( "Release dls:" ) );
							infoTable.setWidget( row++, col, new Label( "Events:" ) );
							col += 2; row = 0;
							infoTable.setWidget( row++, col, new Label( "Accounts:" ) );
							infoTable.setWidget( row++, col, new Label( "Maps:" ) );
							infoTable.setWidget( row++, col, new Label( "M.G. scores:" ) );
							col += 2; row = 0;
							infoTable.setWidget( row++, col, new Label( "Rep profs:" ) );
							infoTable.setWidget( row++, col, new Label( "Rep coms:" ) );
							infoTable.setWidget( row++, col, new Label( "Rep rates:" ) );
							for ( col = 0; col <= infoTable.getCellCount( 0 ); col += 2 )
								for ( row = infoTable.getRowCount() - 1; row >= 0; row-- )
									infoTable.getFlexCellFormatter().addStyleName( row, col, "headerRow" );
							add( infoTable );
							add( ClientUtils.createVerticalEmptyWidget( 7 ) );
						}
						
						int col = 1, row = 0;
						infoTable.setWidget( row++, col, new Label( Integer.toString( activityInfo.getVisitsCount() ) ) );
						infoTable.setWidget( row++, col, new Label( Integer.toString( activityInfo.getReleaseDownloadsCount() ) ) );
						infoTable.setWidget( row++, col, new Label( Integer.toString( activityInfo.getEventsCount() ) ) );
						col += 2; row = 0;
						infoTable.setWidget( row++, col, new Label( Integer.toString( activityInfo.getAccountsCount() ) ) );
						infoTable.setWidget( row++, col, new Label( Integer.toString( activityInfo.getMapsCount() ) ) );
						infoTable.setWidget( row++, col, new Label( Integer.toString( activityInfo.getMousePracticeGameScoresCount() ) ) );
						col += 2; row = 0;
						infoTable.setWidget( row++, col, new Label( Integer.toString( activityInfo.getRepProfilesCount() ) ) );
						infoTable.setWidget( row++, col, new Label( Integer.toString( activityInfo.getRepCommentsCount() ) ) );
						infoTable.setWidget( row++, col, new Label( Integer.toString( activityInfo.getRepRatesCount() ) ) );
						for ( col = 1; col < infoTable.getCellCount( 0 ); col += 2 ) {
							infoTable.getColumnFormatter().setWidth( col, "50px" );
    						for ( row = infoTable.getRowCount() - 1; row >= 0; row-- ) {
    							infoTable.getFlexCellFormatter().setHorizontalAlignment( row, col, HasHorizontalAlignment.ALIGN_RIGHT );
    							infoTable.getFlexCellFormatter().addStyleName( row, col, ( row & 0x01 ) == 0 ? "row0" : "row1"  );
    						}
						}
						
						buildFileStatInfoTable( activityInfo.getLatestFileStatInfoList(), fileStatTable, FileStatsPage.OPEN_GOOGLE_ACCOUNT_CLICK_HANDLER, null );
					}
					@Override
					public void customOnEnd() {
						minutesTextBox.setEnabled( true );
						goButton.setEnabled( true );
						for ( final Button button : predefinedTimeButtons )
							button.setEnabled( true );
					}
				} );
			}
		} );
		
		goButton.click();
    }
	
}
