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

import static hu.belicza.andras.sc2gearsdb.common.client.ClientUtils.DATE_FORMAT;
import static hu.belicza.andras.sc2gearsdb.common.client.ClientUtils.NUMBER_FORMAT;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.EntityListResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PageInfo;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PagingTable;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PagingTableConfig;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PagingTableHandler;
import hu.belicza.andras.sc2gearsdb.user.client.Permission;
import hu.belicza.andras.sc2gearsdb.user.client.beans.OtherFileFilters;
import hu.belicza.andras.sc2gearsdb.user.client.beans.OtherFileInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * Other files page.
 * 
 * @author Andras Belicza
 */
public class OtherFilesPage extends UserPage {
	
	/**
	 * Creates a new OtherFilesPage.
	 */
	public OtherFilesPage() {
		super( "Other Files", "user/otherFiles" );
	}
	
	@Override
	public void buildGUI() {
		if ( !checkPagePermission( Permission.VIEW_OTHER_FILES ) )
			return;
		
		final FlexTable filtersTable = new FlexTable();
		DOM.setStyleAttribute( filtersTable.getElement(), "border", "1px dashed #888888" );
		final HorizontalPanel filtersPanel = new HorizontalPanel();
		filtersPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		filtersPanel.add( new Label( "File extension:" ) );
		final TextBox extensionTextBox = new TextBox();
		extensionTextBox.setWidth( "85px" );
		filtersPanel.add( extensionTextBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		filtersPanel.add( new Label( "Date From:" ) );
		final DateBox fromDateBox = new DateBox();
		fromDateBox.setFormat( new DateBox.DefaultFormat( DATE_FORMAT ) );
		fromDateBox.setWidth( "85px" );
		filtersPanel.add( fromDateBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		filtersPanel.add( new Label( "Date To:" ) );
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
		
		final PagingTableHandler< OtherFileInfo > tableHandler = new PagingTableHandler< OtherFileInfo >() {
			private OtherFileFilters filters;
			private final ClickHandler extClickHandler = new ClickHandler() {
				@Override
				public void onClick( final ClickEvent event ) {
					extensionTextBox.setText( ( (Anchor) event.getSource() ).getText() );
					applyFiltersButton.click();
				}
			};
			@Override
			public String getRowStyleClass( final OtherFileInfo otherFileInfo, final int row ) {
				return null;
			}
			@Override
			public Widget createCellWidget( final OtherFileInfo otherFileInfo, final int column ) {
				switch ( column ) {
				case 0 : return ClientUtils.createTimestampWidget( otherFileInfo.getUploaded() );
				case 1 : {
					final int dotIndex = otherFileInfo.getFileName().lastIndexOf( '.' );
					if ( dotIndex < 0 || dotIndex == otherFileInfo.getFileName().length() - 1 )
						return new Label( otherFileInfo.getFileName() );
					final HorizontalPanel fileNamePanel = new HorizontalPanel();
					fileNamePanel.add( new Label( otherFileInfo.getFileName().substring( 0, dotIndex + 1 ) ) );
					fileNamePanel.add( ClientUtils.createAnchorWithHandler( otherFileInfo.getFileName().substring( dotIndex + 1 ), extClickHandler ) );
					return fileNamePanel;
				}
				case 2 : return new Label( NUMBER_FORMAT.format( new Long( otherFileInfo.getSize() ) ) + " Bytes" );
				case 3 : return ClientUtils.createTimestampWidget( otherFileInfo.getLastModified() );
				case 4 : return new Label( otherFileInfo.getComment() );
				case 5 : {
					final HorizontalPanel actionsPanel = new HorizontalPanel();
					actionsPanel.add( ClientUtils.createFileDownloadButton( userInfo, userInfo.getOtherFileType(), otherFileInfo.getSha1(), otherFileInfo.getFileName(), getSharedAccount() ) );
					return actionsPanel;
				}
				default : throw new RuntimeException( "Unhandled column: " + column );
				}
			}
			@Override
			public String getCursorNamespace() {
				filters = new OtherFileFilters();
				String s;
				if ( !( s = extensionTextBox.getText() ).isEmpty() ) filters.setExtension( s );
				filters.setFromDate( fromDateBox.getValue() );
				filters.setToDate  ( toDateBox  .getValue() );
				return filters.toString();
			}
			@Override
			public void getEntityListResult( final PageInfo pageInfo, final AsyncCallback< RpcResult< EntityListResult< OtherFileInfo > > > callback ) {
				SERVICE_ASYNC.getOtherFileInfoList( getSharedAccount(), pageInfo, filters, callback );
			}
			@Override
			public void deleteEntityList( final List< OtherFileInfo > entityList, final AsyncCallback< RpcResult< Integer > > callback ) {
				final List< String > sha1List = new ArrayList< String >( entityList.size() );
				for ( final OtherFileInfo otherFileInfo : entityList )
					sha1List.add( otherFileInfo.getSha1() );
				SERVICE_ASYNC.deleteFileList( getSharedAccount(), userInfo.getOtherFileType(), sha1List, callback );
			}
			@Override
			public void downloadEntityList( final List< OtherFileInfo > entityList ) {
				final StringBuilder sha1ListBuilder = new StringBuilder();
				for ( final OtherFileInfo otherFileInfo : entityList ) {
					if ( sha1ListBuilder.length() > 0 )
						sha1ListBuilder.append( ',' );
					sha1ListBuilder.append( otherFileInfo.getSha1() );
				}
				
				ClientUtils.initiateBatchDownload( userInfo, userInfo.getOtherFileType(), sha1ListBuilder.toString(), getSharedAccount() );
			}
		};
		
		final PagingTableConfig< OtherFileInfo > tableConfig = new PagingTableConfig< OtherFileInfo >();
		tableConfig.setInfoPanel       ( infoPanel );
		tableConfig.setEntityName      ( "other file" );
		tableConfig.setEntityNamePlural( "other files" );
		tableConfig.setColumnLabels    ( "Uploaded", "File name", "Size", "Last modified", "Comment", "Actions" );
		tableConfig.setColumnHorizontalAlignments( HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_LEFT );
		tableConfig.setTableHandler    ( tableHandler );
		tableConfig.setSortingColumn   ( 0 );
		tableConfig.setDeleteEnabled   ( checkPermission( Permission.DELETE_OTHER_FILES ) );
		
		final PagingTable< OtherFileInfo > pagingTable = new PagingTable< OtherFileInfo >( tableConfig );
		
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
				extensionTextBox.setText ( null );
				fromDateBox     .setValue( null );
				toDateBox       .setValue( null );
				pagingTable.reloadFirstPage();
			}
		} );
		
		extensionTextBox.addKeyPressHandler( new KeyPressHandler() {
			@Override
			public void onKeyPress( final KeyPressEvent event ) {
				if ( event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER )
					applyFiltersButton.click();
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
