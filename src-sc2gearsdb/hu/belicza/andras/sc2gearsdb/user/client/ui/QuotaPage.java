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

import static hu.belicza.andras.sc2gearsdb.common.client.ClientUtils.NUMBER_FORMAT;
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.user.client.Permission;
import hu.belicza.andras.sc2gearsdb.user.client.beans.FileStatInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

/**
 * Quota page.
 * 
 * @author Andras Belicza
 */
public class QuotaPage extends UserPage {
	
	/**
	 * Creates a new QuotaPage.
	 */
	public QuotaPage() {
		super( "Quota", "user/quota" );
	}
	
	@Override
	public void buildGUI() {
		if ( !checkPagePermission( Permission.VIEW_QUOTA ) )
			return;
		
		SERVICE_ASYNC.getFileStatInfo( getSharedAccount(), new AsyncCallbackAdapter< FileStatInfo >( infoPanel ) {
			@Override
			public void customOnSuccess( final RpcResult< FileStatInfo > rpcResult ) {
				final FileStatInfo fileStatInfo = rpcResult.getValue();
				if ( fileStatInfo == null )
					return;
				
				final HorizontalPanel packagePanel = new HorizontalPanel();
				packagePanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
				packagePanel.add( new Label( "Your current package:" ) );
				packagePanel.add( ClientUtils.createHorizontalEmptyWidget( 10 ) );
				if ( fileStatInfo.getDbPackageName() != null ) {
					packagePanel.add( new Image( fileStatInfo.getDbPackageIcon() ) );
					packagePanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
					packagePanel.add( new Label( fileStatInfo.getDbPackageName() ) );
				}
				else
					packagePanel.add( new Label( "<Unknown>" ) );
				add( packagePanel );
				
				
				final long allStorage  = fileStatInfo.getAllStorage();
				final int  usedPercent = fileStatInfo.getPaidStorage() == 0 ? 0 : (int) ( fileStatInfo.getAllStorage() * 100 / fileStatInfo.getPaidStorage() );
				final int  freePercent = 100 - usedPercent;
				
				// 3D pie chart to show storage usage
				// Online chart editor: http://imagecharteditor.appspot.com/
				add( ClientUtils.styledWidget( new Label( "Storage usage:" ), "h3" ) );
				final int usedPercentData = usedPercent > 100 ? 100 : usedPercent;
				final int freePercentData = usedPercent > 100 ?   0 : freePercent; 
				final Image chartImage = new Image( "https://chart.googleapis.com/chart?chs=380x130&cht=p3&chco=7777CC|76A4FB&chd=t:" + freePercentData + "," + usedPercentData + "&chl=Free+" + freePercent + "%25|Used+" + usedPercent + "%25&chma=0,0,0,5|0,5" );
				chartImage.setPixelSize( 380, 130 );
				add( chartImage );
				
				// Storage usage details
				add( ClientUtils.styledWidget( new Label( "Storage details:" ), "h3" ) );
				FlexTable table = new FlexTable();
				table.setBorderWidth( 1 );
				table.setCellSpacing( 0 );
				table.setCellPadding( 3 );
				table.setWidget( 0, 0, new Label( "Available storage:" ) );
				table.setWidget( 0, 1, new Label( NUMBER_FORMAT.format( fileStatInfo.getPaidStorage() ) + " bytes" ) );
				table.setWidget( 0, 2, new Label( "100%" ) );
				table.setWidget( 1, 0, new Label( "Used storage:" ) );
				table.setWidget( 1, 1, new Label( NUMBER_FORMAT.format( allStorage ) + " bytes" ) );
				table.setWidget( 1, 2, new Label( usedPercent + "%" ) );
				table.setWidget( 2, 0, new Label( "Free storage:" ) );
				table.setWidget( 2, 1, new Label( NUMBER_FORMAT.format( fileStatInfo.getPaidStorage() - allStorage ) + " bytes" ) );
				table.setWidget( 2, 2, new Label( freePercent + "%" ) );
				CellFormatter cellFormatter = table.getCellFormatter();
				for ( int i = table.getRowCount() - 1; i >= 0; i-- )
					cellFormatter.addStyleName( i, 0, "headerRow" );
				add( table );
				for ( int i = table.getRowCount() - 1; i >= 0; i-- )
					for ( int j = table.getCellCount( i ) - 1; j >= 1; j-- )
						cellFormatter.setHorizontalAlignment( i, j, HasHorizontalAlignment.ALIGN_RIGHT );
				
				// Used storage details
				add( ClientUtils.styledWidget( new Label( "Used storage details:" ), "h3" ) );
				if ( fileStatInfo.getAllCount() == 0 ) {
					add( new Label( "There are no uploaded files." ) );
				}
				else {
					table = new FlexTable();
					cellFormatter = table.getCellFormatter();
					table.setBorderWidth( 1 );
					table.setCellSpacing( 0 );
					table.setCellPadding( 3 );
					int row = 0;
					table.setWidget( row, 0, new Label( "File type" ) );
					table.setWidget( row, 1, new Label( "Files" ) );
					table.setWidget( row, 2, new Label( "Storage used" ) );
					table.setWidget( row, 3, new Label( "Avg. file size" ) );
					table.setWidget( row, 4, new Label( "Share" ) );
					table.getRowFormatter().addStyleName( row, "headerRow" );
					for ( int typeIdx = 0; typeIdx < 4; typeIdx++ ) {
						int    count    = 0;
						long   storage  = 0;
						String typeName = "";
						switch ( typeIdx ) {
						case 0 : count = fileStatInfo.getAllCount  (); storage = fileStatInfo.getAllStorage  (); typeName = "<all>"    ; break;
						case 1 : count = fileStatInfo.getRepCount  (); storage = fileStatInfo.getRepStorage  (); typeName = "SC2Replay"; break;
						case 2 : count = fileStatInfo.getSmpdCount (); storage = fileStatInfo.getSmpdStorage (); typeName = "smpd"     ; break;
						case 3 : count = fileStatInfo.getOtherCount(); storage = fileStatInfo.getOtherStorage(); typeName = "Other"    ; break;
						}
						row++;
						final int filePercent = allStorage == 0 ? 0 : (int) ( storage * 100 / allStorage );
						table.setWidget( row, 0, new Label( typeName ) );
						cellFormatter.setHorizontalAlignment( row, 0, HasHorizontalAlignment.ALIGN_LEFT );
						table.setWidget( row, 1, new Label( NUMBER_FORMAT.format( count ) ) );
						table.setWidget( row, 2, new Label( NUMBER_FORMAT.format( storage ) + " bytes" ) );
						table.setWidget( row, 3, new Label( NUMBER_FORMAT.format( count == 0 ? 0 : storage / count ) + " bytes" ) );
						table.setWidget( row, 4, new Label( filePercent + "%" ) );
						for ( int i = 1; i < 5; i++ )
							cellFormatter.setHorizontalAlignment( row, i, HasHorizontalAlignment.ALIGN_RIGHT );
					}
					add( table );
				}
				
				// Recalculate file stats
				add( ClientUtils.createVerticalEmptyWidget( 10 ) );
				add( ClientUtils.styledWidget( new Label( "Recalculate statistics:" ), "h3" ) );
				add( new Label( "File statistics are calculated automatically but asynchronously." ) );
				add( new Label( "If you think your stats are inaccurate, you can trigger a recalculation." ) );
				add( new Label( "Recalculation can only be requested once per 24 hours." ) );
				
				add( ClientUtils.createVerticalEmptyWidget( 7 ) );
				add( fileStatInfo.getRecalced() == null ? new Label( "Last recalculation finished at: N/A" )
					: ClientUtils.createTimestampWidget( "Last recalculation finished at: ", fileStatInfo.getRecalced() ) );
				add( ClientUtils.createVerticalEmptyWidget( 9 ) );
				final Button recalculateFileStatsButton = new Button( "Recalculate file stats" );
				ClientUtils.setWidgetIcon( recalculateFileStatsButton, "fugue/calculator.png", null, "#ffaa77" );
				recalculateFileStatsButton.setEnabled( fileStatInfo.isHasFileStat() );
				recalculateFileStatsButton.addClickHandler( new ClickHandler() {
					@Override
					public void onClick( final ClickEvent event ) {
						recalculateFileStatsButton.setEnabled( false );
						SERVICE_ASYNC.recalcFileStats( getSharedAccount(), new AsyncCallbackAdapter< Void >( infoPanel ) {
							@Override
							public void customOnSuccess( final RpcResult< Void > rpcResult ) {
								// Nothing to do here...
							}
							@Override
							public void customOnEnd() {
								recalculateFileStatsButton.setEnabled( true );
							};
						} );
					}
				} );
				add( recalculateFileStatsButton );
			}
		} );
		
	}
	
}
