/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.apiuser.client.ui;

import static hu.belicza.andras.sc2gearsdb.common.client.ClientUtils.DATE_FORMAT;
import static hu.belicza.andras.sc2gearsdb.common.client.ClientUtils.NUMBER_FORMAT;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.ApiCallStatFilters;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.ApiCallStatInfo;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.EntityListResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PageInfo;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PagingTable;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PagingTableConfig;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PagingTableHandler;

import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * API call stats page.
 * 
 * @author Andras Belicza
 */
public class ApiCallStatsPage extends ApiUserPage {
	
    /**
     * Creates a new ApiCallStatsPage.
     */
    public ApiCallStatsPage() {
    	super( "API Call Stats", "apiuser/apiCallStats" );
    }
    
    @Override
    public void buildGUI() {
		final FlexTable filtersTable = new FlexTable();
		DOM.setStyleAttribute( filtersTable.getElement(), "border", "1px dashed #888888" );
		final HorizontalPanel filtersPanel = new HorizontalPanel();
		filtersPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		filtersPanel.add( new Label( "Day From:" ) );
		final DateBox fromDateBox = new DateBox();
		fromDateBox.setFormat( new DateBox.DefaultFormat( DATE_FORMAT ) );
		fromDateBox.setWidth( "85px" );
		filtersPanel.add( fromDateBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		filtersPanel.add( new Label( "Day To:" ) );
		final DateBox toDateBox = new DateBox();
		toDateBox.setFormat( fromDateBox.getFormat() );
		toDateBox.setWidth( "85px" );
		filtersPanel.add( toDateBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		filtersTable.setWidget( 0, 0, filtersPanel );
		// Filter control buttons
		final Button applyFiltersButton = new Button( "Apply" );
		ClientUtils.styleSmallButton( applyFiltersButton );
		filtersTable.setWidget( 0, 1, applyFiltersButton );
		final Button clearFiltersButton = new Button( "Clear" );
		ClientUtils.styleSmallButton( clearFiltersButton );
		filtersTable.setWidget( 0, 2, clearFiltersButton );
		add( filtersTable );
		add( ClientUtils.createVerticalEmptyWidget( 7 ) );
    	
		final PagingTableHandler< ApiCallStatInfo > tableHandler = new PagingTableHandler< ApiCallStatInfo >() {
			private ApiCallStatFilters filters;
			@Override
			public String getRowStyleClass( final ApiCallStatInfo apiCallStatInfo, final int row ) {
				return null;
			}
			@Override
			public Widget createCellWidget( final ApiCallStatInfo apiCallStatInfo, final int column ) {
				switch ( column ) {
				case 0  : return new Label( apiCallStatInfo.getDay() );
				case 1  : return new Label( NUMBER_FORMAT.format( apiCallStatInfo.getCalls              () )         );
				case 2  : return new Label( NUMBER_FORMAT.format( apiCallStatInfo.getUsedOps            () )         );
				case 3  : return new Label( NUMBER_FORMAT.format( apiCallStatInfo.getAvgExecTime        () ) + " ms" );
				case 4  : return new Label( NUMBER_FORMAT.format( apiCallStatInfo.getDeniedCalls        () )         );
				case 5  : return new Label( NUMBER_FORMAT.format( apiCallStatInfo.getErrors             () )         );
				case 6  : return new Label( NUMBER_FORMAT.format( apiCallStatInfo.getInfoCalls          () )         );
				case 7  : return new Label( NUMBER_FORMAT.format( apiCallStatInfo.getAvgInfoExecTime    () ) + " ms" );
				case 8  : return new Label( NUMBER_FORMAT.format( apiCallStatInfo.getMapInfoCalls       () )         );
				case 9  : return new Label( NUMBER_FORMAT.format( apiCallStatInfo.getAvgMapInfoExecTime () ) + " ms" );
				case 10 : return new Label( NUMBER_FORMAT.format( apiCallStatInfo.getParseRepCalls      () )         );
				case 11 : return new Label( NUMBER_FORMAT.format( apiCallStatInfo.getAvgParseRepExecTime() ) + " ms" );
				case 12 : return new Label( NUMBER_FORMAT.format( apiCallStatInfo.getProfInfoCalls      () )         );
				case 13 : return new Label( NUMBER_FORMAT.format( apiCallStatInfo.getAvgProfInfoExecTime() ) + " ms" );
				default : throw new RuntimeException( "Unhandled column: " + column );
				}
			}
			@Override
			public String getCursorNamespace() {
				filters = new ApiCallStatFilters();
				String s;
				if ( !( s = fromDateBox.getTextBox().getText() ).isEmpty() ) filters.setFromDay( s );
				if ( !( s = toDateBox  .getTextBox().getText() ).isEmpty() ) filters.setToDay  ( s );
				return filters.toString();
			}
			@Override
			public void getEntityListResult( final PageInfo pageInfo, final AsyncCallback< RpcResult< EntityListResult< ApiCallStatInfo > > > callback ) {
				SERVICE_ASYNC.getApiCallStatList( getSharedApiAccount(), pageInfo, filters, callback );
			}
			@Override
			public void deleteEntityList( final List< ApiCallStatInfo > entityList, final AsyncCallback< RpcResult< Integer > > callback ) {
				// Stats cannot be deleted
			}
			@Override
			public void downloadEntityList( final List< ApiCallStatInfo > entityList ) {
				// Stats cannot be downloaded
			}
		};
		
		final PagingTableConfig< ApiCallStatInfo > tableConfig = new PagingTableConfig< ApiCallStatInfo >();
		tableConfig.setInfoPanel        ( infoPanel );
		tableConfig.setEntityName       ( "API call stat" );
		tableConfig.setEntityNamePlural ( "API call stats" );
		tableConfig.setColumnLabels     ( "Day", "∑Calls", "Used Ops", "Avg Time", "Denied", "Errors", "Info", "Avg Info T", "Map info", "Avg Map info T", "Parse rep", "Avg Parse rep T", "Prof info", "Avg Prof info T" );
		tableConfig.setColumnHorizontalAlignments( HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_RIGHT,  HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_RIGHT );
		tableConfig.setTableHandler     ( tableHandler );
		tableConfig.setSortingColumn    ( 0 );
		tableConfig.setAllowRowSelection( false );
		
		final PagingTable< ApiCallStatInfo > pagingTable = new PagingTable< ApiCallStatInfo >( tableConfig );
		
		add( pagingTable );
		
		applyFiltersButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				pagingTable.reloadFirstPage();
			}
		} );
		clearFiltersButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				fromDateBox.setValue( null );
				toDateBox  .setValue( null );
				pagingTable.reloadFirstPage();
			}
		} );
		
		final ValueChangeHandler< Date > dateBoxChangeHandler = new ValueChangeHandler< Date >() {
			@Override
			public void onValueChange( final ValueChangeEvent< Date > event ) {
				applyFiltersButton.click();
			}
		}; 
		fromDateBox.addValueChangeHandler( dateBoxChangeHandler );
		toDateBox  .addValueChangeHandler( dateBoxChangeHandler );
    }
	
}
