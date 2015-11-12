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

import static hu.belicza.andras.sc2gearsdb.common.client.ClientUtils.DATE_TIME_FORMAT;
import static hu.belicza.andras.sc2gearsdb.common.client.ClientUtils.NUMBER_FORMAT;
import hu.belicza.andras.sc2gearsdb.admin.client.AdminService.DlStatType;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.DlStatInfo;
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;

/**
 * Download Stats page.
 * 
 * @author Andras Belicza
 */
public class DlStatsPage extends AdminPage {
	
	/** Previous stat info map (to calculate delta). Static: it will retain state between pages.           */
	private static final Map< String, DlStatInfo > previousStatInfoMap = new HashMap< String, DlStatInfo >();
	/** Date of the previous stat info (to calculate delta t). Static: it will retain state between pages. */
	private static final Date                      previousTime        = new Date( 0 );
	
    /**
     * Creates a new MiscPage.
     */
    public DlStatsPage() {
		super( "Download Stats", "admin/dlStats" );
    }
    
    @Override
    public void buildGUI() {
		final HorizontalPanel filtersPanel = new HorizontalPanel();
		filtersPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
    	
    	filtersPanel.add( new Label( "Filter:" ) );
		final ListBox typeListBox = new ListBox();
    	for ( final DlStatType dlStatType : DlStatType.values() )
    		typeListBox.addItem( dlStatType.stringValue );
    	filtersPanel.add( typeListBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
    	final Button refreshButton = new Button( "Refresh" );
		ClientUtils.styleSmallButton( refreshButton );
		DOM.setStyleAttribute( refreshButton.getElement(), "fontWeight", "bold" );
    	filtersPanel.add( refreshButton );
    	add( filtersPanel );
    	
    	add( ClientUtils.createVerticalEmptyWidget( 7 ) );
    	
		final HorizontalPanel timePanel = new HorizontalPanel();
		timePanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		final Label timeLabel = new Label();
		timePanel.add( timeLabel );
		timePanel.add( ClientUtils.createHorizontalEmptyWidget( 15 ) );
		final Label deltaTimeLabel = new Label();
		deltaTimeLabel.addStyleName( "delta" );
		timePanel.add( deltaTimeLabel );
    	add( timePanel );
    	
    	add( ClientUtils.createVerticalEmptyWidget( 7 ) );
		
    	final FlexTable table = new FlexTable();
    	
    	refreshButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				typeListBox  .setEnabled( false );
				refreshButton.setEnabled( false );
				
				SERVICE_ASYNC.getDlStatInfoList( DlStatType.values()[ typeListBox.getSelectedIndex() ], new AsyncCallbackAdapter< List< DlStatInfo > >( infoPanel ) {
					@Override
					public void customOnSuccess( final RpcResult< List< DlStatInfo > > rpcResult ) {
						final List< DlStatInfo > dlStatInfoList = rpcResult.getValue();
						if ( dlStatInfoList == null )
							return;
						
						final Date now = new Date();
						if ( previousTime.getTime() == 0 )
							previousTime.setTime( now.getTime() );
						
						if ( table.getRowCount() == 0 ) {
							// Init table
							table.setBorderWidth( 1 );
							table.setCellSpacing( 0 );
							table.setCellPadding( 3 );
							add( table );
							add( ClientUtils.createVerticalEmptyWidget( 10 ) );
						}
						else
							table.removeAllRows();
						
						timeLabel.setText( "Time: " + DATE_TIME_FORMAT.format( now ) );
						long dt = now.getTime() - previousTime.getTime();
						final long hours = dt / (60*60*1000);
						final long mins  = ( dt %= 60*60*1000 ) / (60*1000);
						final long sec   = ( dt %= 60*1000 ) / 1000;
						deltaTimeLabel.setText( "Δt: " + hours + ":" + ( mins < 10 ? "0" + mins : mins ) + ":" + ( sec < 10 ? "0" + sec : sec ) );
						previousTime.setTime( now.getTime() );
						
						final RowFormatter rowFormatter = table.getRowFormatter();
						
						int column = 0;
						table.setWidget( 0, column++, new Label( "#" ) );
						table.setWidget( 0, column++, new Label( "File name" ) );
						table.setWidget( 0, column++, new Label( "Count" ) );
						table.setWidget( 0, column++, new Label( "Δ" ) );
						table.setWidget( 0, column++, new Label( "Java client" ) );
						table.setWidget( 0, column++, new Label( "Δ" ) );
						table.setWidget( 0, column++, new Label( "Unique" ) );
						table.setWidget( 0, column++, new Label( "Δ" ) );
						table.setWidget( 0, column++, new Label( "Date" ) );
						rowFormatter.addStyleName( 0, "headerRow" );
						
						int row = 0;
						final CellFormatter cellFormatter = table.getCellFormatter();
						for ( final DlStatInfo dlStatInfo : dlStatInfoList ) {
							column = 0;
							row++;
							
							DlStatInfo prevInfo = previousStatInfoMap.get( dlStatInfo.getFileName() );
							if ( prevInfo == null )
								prevInfo = dlStatInfo;
							
							table.setWidget( row, column++, new Label( row + "." ) );
							cellFormatter.setHorizontalAlignment( row, column-1, HasHorizontalAlignment.ALIGN_RIGHT );
							table.setWidget( row, column++, new Label( dlStatInfo.getFileName() ) );
							table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( dlStatInfo.getCount() ) ) );
							table.setWidget( row, column++, ClientUtils.styledWidget( new Label( "+" + NUMBER_FORMAT.format( dlStatInfo.getCount() - prevInfo.getCount() ) ), "delta" ) );
							table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( dlStatInfo.getJavaClient() ) ) );
							table.setWidget( row, column++, ClientUtils.styledWidget( new Label( "+" + NUMBER_FORMAT.format( dlStatInfo.getJavaClient() - prevInfo.getJavaClient() ) ), "delta" ) );
							table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( dlStatInfo.getUnique() ) ) );
							table.setWidget( row, column++, ClientUtils.styledWidget( new Label( "+" + NUMBER_FORMAT.format( dlStatInfo.getUnique() - prevInfo.getUnique() ) ), "delta" ) );
							table.setWidget( row, column++, ClientUtils.createTimestampWidget( dlStatInfo.getDate() ) );
							
			    			for ( int i = column - 1; i >= 2; i-- )
		    					cellFormatter.setHorizontalAlignment( row, i, HasHorizontalAlignment.ALIGN_RIGHT );
			    			
			    			rowFormatter.addStyleName( row, ( row & 0x01 ) == 0 ? "row0" : "row1" );
						}
						
						previousStatInfoMap.clear();
						for ( final DlStatInfo dlStatInfo : dlStatInfoList )
							previousStatInfoMap.put( dlStatInfo.getFileName(), dlStatInfo );
					}
					@Override
					public void customOnEnd() {
						typeListBox  .setEnabled( true );
						refreshButton.setEnabled( true );
					}
				} );
			}
		} );
    	
    	typeListBox.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange( final ChangeEvent event ) {
				refreshButton.click();
			}
		} );
    	
    	refreshButton.click();
    }
	
}
