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
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.ImageButton;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.EntityListResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PageInfo;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PagingTable;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PagingTableConfig;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PagingTableHandler;
import hu.belicza.andras.sc2gearsdb.user.client.Permission;
import hu.belicza.andras.sc2gearsdb.user.client.beans.MousePrintFilters;
import hu.belicza.andras.sc2gearsdb.user.client.beans.MousePrintFullInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.MousePrintInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * Mouse prints page.
 * 
 * @author Andras Belicza
 */
public class MousePrintsPage extends UserPage {
	
	/**
	 * Creates a new MousePrintsPage.
	 */
	public MousePrintsPage() {
		super( "Mouse Prints", "user/mousePrints" );
	}
	
	@Override
	public void buildGUI() {
		if ( !checkPagePermission( Permission.VIEW_MOUSE_PRINTS ) )
			return;
		
		final FlexTable filtersTable = new FlexTable();
		DOM.setStyleAttribute( filtersTable.getElement(), "border", "1px dashed #888888" );
		final HorizontalPanel filtersPanel = new HorizontalPanel();
		filtersPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
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
		
		final PagingTableHandler< MousePrintInfo > tableHandler = new PagingTableHandler< MousePrintInfo >() {
			private MousePrintFilters filters;
			@Override
			public String getRowStyleClass( final MousePrintInfo mousePrintInfo, final int row ) {
				return null;
			}
			@Override
			public Widget createCellWidget( final MousePrintInfo mousePrintInfo, final int column ) {
				switch ( column ) {
				case 0 : return ClientUtils.createTimestampWidget( mousePrintInfo.getRecordingStart() );
				case 1 : return ClientUtils.createTimestampWidget( mousePrintInfo.getRecordingEnd  () );
				case 2 : return new Label( ClientUtils.formatSeconds( (int) ( mousePrintInfo.getRecordingEnd().getTime() - mousePrintInfo.getRecordingStart().getTime() ) / 1000 ) );
				case 3 : return new Label( mousePrintInfo.getScreenWidth() + "x" + mousePrintInfo.getScreenHeight() );
				case 4 : return new Label( NUMBER_FORMAT.format( new Long( mousePrintInfo.getFileSize() ) ) + " Bytes" );
				case 5 : return new Label( NUMBER_FORMAT.format( mousePrintInfo.getSamplesCount() ) );
				case 6 : {
					final HorizontalPanel actionsPanel = new HorizontalPanel();
					final Button detailsButton = new ImageButton( "edit-column.png", "Details" );
					detailsButton.addClickHandler( new ClickHandler() {
						@Override
						public void onClick( final ClickEvent event ) {
							SERVICE_ASYNC.getMousePrintFullInfo( getSharedAccount(), mousePrintInfo.getSha1(), new AsyncCallbackAdapter< MousePrintFullInfo >( infoPanel ) {
								@Override
								public void customOnSuccess( final RpcResult< MousePrintFullInfo > rpcResult ) {
									final MousePrintFullInfo mousePrintFullInfo = rpcResult.getValue();
									final int swc = mousePrintFullInfo.getSavedWithCompression();
									final String savedWithCompression = swc == 0 ? "No compression" : swc == 1 ? "Deflate" : swc == 2 ? "BZip2" : "Unknown";
									ClientUtils.displayDetailsDialog( "Mouse print details", new Object[][] {
										{ "File name:"             , mousePrintFullInfo.getFileName() },
										{ "Uploaded at:"           , mousePrintFullInfo.getUploadedDate() },
										{ "File size:"             , NUMBER_FORMAT.format( mousePrintFullInfo.getFileSize() ) + " bytes" },
										{ "File last modified:"    , mousePrintFullInfo.getFileLastModified() },
										{ "File SHA-1:"            , mousePrintFullInfo.getSha1() },
										{ "Version:"               , ( ( mousePrintFullInfo.getVersion() & 0xff00 ) >> 8 ) + "." + ( mousePrintFullInfo.getVersion() & 0xff ) },
										{ "Recording start:"       , mousePrintFullInfo.getRecordingStart() },
										{ "Recording end:"         , mousePrintFullInfo.getRecordingEnd() },
										{ "Length:"                , ClientUtils.formatSeconds( (int) ( mousePrintFullInfo.getRecordingEnd().getTime() - mousePrintFullInfo.getRecordingStart().getTime() ) / 1000 ) },
										{ "Screen size:"           , mousePrintFullInfo.getScreenWidth() + "x" + mousePrintFullInfo.getScreenHeight() },
										{ "Screen resolution:"     , mousePrintFullInfo.getScreenResolution() + " dots/inch" },
										{ "Sampling time:"         , mousePrintFullInfo.getSamplingTime() + " ms" },
										{ "Samples:"               , mousePrintFullInfo.getSamplesCount() },
										{ "Uncompressed data size:", NUMBER_FORMAT.format( mousePrintFullInfo.getUncompressedDataSize() ) + " bytes" },
										{ "Saved with compression:", savedWithCompression }
									} );
								}
							} );
						}
					} );
					actionsPanel.add( detailsButton );
					actionsPanel.add( ClientUtils.createHorizontalEmptyWidget( 3 ) );
					final Button previewButton = new ImageButton( "picture.png", "Preview mouse print image" );
					previewButton.addClickHandler( new ClickHandler() {
						@Override
						public void onClick( final ClickEvent event ) {
							final DialogBox dialogBox = new DialogBox( true );
							dialogBox.setText( mousePrintInfo.getFileName() + " (" + mousePrintInfo.getScreenWidth() + "x" + mousePrintInfo.getScreenHeight() + ")" );
							dialogBox.setGlassEnabled( true );
							final VerticalPanel vp = new VerticalPanel();
							vp.setWidth( "100%" );
							vp.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
							final Image prevImage = new Image( "/smpdimage/" + mousePrintInfo.getSha1() + ".png" + ( getSharedAccount() == null ? "" : "?sharedAccount=" + getSharedAccount() ) );
							prevImage.addStyleName( "pointerMouse" );
							prevImage.setSize( "1px", "1px" );
							prevImage.addLoadHandler( new LoadHandler() {
								@Override
								public void onLoad( final LoadEvent event ) {
									prevImage.setSize( null, null );
									DOM.setStyleAttribute( prevImage.getElement(), "border", "2px solid rgb(255,169,76)" );
									dialogBox.setWidget( prevImage ); // Set only image
									// Image loaded, re-center dialog
									dialogBox.center();
								}
							} );
							prevImage.addClickHandler( new ClickHandler() {
								@Override
								public void onClick( final ClickEvent event ) {
									dialogBox.hide();
								}
							} );
							vp.add( prevImage );
							final Label noteLabel = ClientUtils.styledWidget( new Label( "Generating preview image..." ), "note" );
							vp.add( noteLabel );
							vp.add( new HTML( "&nbsp;" ) );
							prevImage.addErrorHandler( new ErrorHandler() {
								@Override
								public void onError( final ErrorEvent event ) {
									noteLabel.removeStyleName( "note" );
									noteLabel.addStyleName( "errorMsg" );
									noteLabel.setText( " An error occured while generating mouse print preview. " );
									dialogBox.center(); // Different text length, re-center dialog
								}
							} );
							dialogBox.setWidget( vp );
							dialogBox.center();
						}
					} );
					actionsPanel.add( previewButton );
					actionsPanel.add( ClientUtils.createHorizontalEmptyWidget( 3 ) );
					actionsPanel.add( ClientUtils.createFileDownloadButton( userInfo, userInfo.getMousePrintFileType(), mousePrintInfo.getSha1(), mousePrintInfo.getFileName(), getSharedAccount() ) );
					return actionsPanel;
				}
				default : throw new RuntimeException( "Unhandled column: " + column );
				}
			}
			@Override
			public String getCursorNamespace() {
				filters = new MousePrintFilters();
				filters.setFromDate( fromDateBox.getValue() );
				filters.setToDate  ( toDateBox  .getValue() );
				return filters.toString();
			}			
			@Override
			public void getEntityListResult( final PageInfo pageInfo, final AsyncCallback< RpcResult< EntityListResult< MousePrintInfo > > > callback ) {
				SERVICE_ASYNC.getMousePrintInfoList( getSharedAccount(), pageInfo, filters, callback );
			}
			@Override
			public void deleteEntityList( final List< MousePrintInfo > entityList, final AsyncCallback< RpcResult< Integer > > callback ) {
				final List< String > sha1List = new ArrayList< String >( entityList.size() );
				for ( final MousePrintInfo mousePrintInfo : entityList )
					sha1List.add( mousePrintInfo.getSha1() );
				SERVICE_ASYNC.deleteFileList( getSharedAccount(), userInfo.getMousePrintFileType(), sha1List, callback );
			}
			@Override
			public void downloadEntityList( final List< MousePrintInfo > entityList ) {
				final StringBuilder sha1ListBuilder = new StringBuilder();
				for ( final MousePrintInfo mousePrintInfo : entityList ) {
					if ( sha1ListBuilder.length() > 0 )
						sha1ListBuilder.append( ',' );
					sha1ListBuilder.append( mousePrintInfo.getSha1() );
				}
				
				ClientUtils.initiateBatchDownload( userInfo, userInfo.getMousePrintFileType(), sha1ListBuilder.toString(), getSharedAccount() );
			}
		};
		
		final PagingTableConfig< MousePrintInfo > tableConfig = new PagingTableConfig< MousePrintInfo >();
		tableConfig.setInfoPanel       ( infoPanel );
		tableConfig.setEntityName      ( "mouse print" );
		tableConfig.setEntityNamePlural( "mouse prints" );
		tableConfig.setColumnLabels    ( "Recording start", "Recording end", "Length", "Screen size", "File size", "Samples", "Actions" );
		tableConfig.setColumnHorizontalAlignments( HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_LEFT );
		tableConfig.setTableHandler    ( tableHandler );
		tableConfig.setDeleteEnabled   ( checkPermission( Permission.DELETE_MOUSE_PRINTS ) );
		
		final PagingTable< MousePrintInfo > pagingTable = new PagingTable< MousePrintInfo >( tableConfig );
		
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
