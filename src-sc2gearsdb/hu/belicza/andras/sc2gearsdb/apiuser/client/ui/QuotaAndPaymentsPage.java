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

import static hu.belicza.andras.sc2gearsdb.common.client.ClientUtils.NUMBER_FORMAT;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.ApiPaymentInfo;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.QuotaAndPaymentsInfo;
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

/**
 * Quota & Payments page.
 * 
 * @author Andras Belicza
 */
public class QuotaAndPaymentsPage extends ApiUserPage {
	
	/**
	 * Creates a new QuotaPage.
	 */
	public QuotaAndPaymentsPage() {
		super( "Quota & Payments", "apiuser/quotaAndPayments" );
	}
	
	@Override
	public void buildGUI() {
		SERVICE_ASYNC.getQuotaAndPaymentsInfo( getSharedApiAccount(), new AsyncCallbackAdapter< QuotaAndPaymentsInfo >( infoPanel ) {
			@Override
			public void customOnSuccess( final RpcResult< QuotaAndPaymentsInfo > rpcResult ) {
				final QuotaAndPaymentsInfo quotaAndPaymentsInfo = rpcResult.getValue();
				if ( quotaAndPaymentsInfo == null )
					return;
				
				final int usedPercent  = quotaAndPaymentsInfo.getPaidOps() == 0 ? 0 : (int) ( quotaAndPaymentsInfo.getUsedOps() * 100 / quotaAndPaymentsInfo.getPaidOps() );
				final int availPercent = 100 - usedPercent;
				
				// 3D pie chart to show Ops usage
				// Online chart editor: http://imagecharteditor.appspot.com/
				add( ClientUtils.styledWidget( new Label( "Ops usage:" ), "h3" ) );
				final int usedPercentData  = usedPercent > 100 ? 100 : usedPercent;
				final int availPercentData = usedPercent > 100 ?   0 : availPercent; 
				final Image chartImage = new Image( "https://chart.googleapis.com/chart?chs=380x130&cht=p3&chco=7777CC|76A4FB&chd=t:" + availPercentData + "," + usedPercentData + "&chl=Avail+" + availPercent + "%25|Used+" + usedPercent + "%25&chma=0,0,0,5|0,5" );
				chartImage.setPixelSize( 380, 130 );
				add( chartImage );
				
				// Ops usage details
				add( ClientUtils.styledWidget( new Label( "Ops details:" ), "h3" ) );
				FlexTable table = new FlexTable();
				table.setBorderWidth( 1 );
				table.setCellSpacing( 0 );
				table.setCellPadding( 3 );
				table.setWidget( 0, 0, new Label( "Paid Ops:" ) );
				table.setWidget( 0, 1, new Label( NUMBER_FORMAT.format( quotaAndPaymentsInfo.getPaidOps() ) ) );
				table.setWidget( 0, 2, new Label( "100%" ) );
				table.setWidget( 1, 0, new Label( "Used Ops:" ) );
				table.setWidget( 1, 1, new Label( NUMBER_FORMAT.format( quotaAndPaymentsInfo.getUsedOps() ) ) );
				table.setWidget( 1, 2, new Label( usedPercent + "%" ) );
				table.setWidget( 2, 0, new Label( "Available Ops:" ) );
				table.setWidget( 2, 1, new Label( NUMBER_FORMAT.format( quotaAndPaymentsInfo.getPaidOps() - quotaAndPaymentsInfo.getUsedOps() ) ) );
				table.setWidget( 2, 2, new Label( availPercent + "%" ) );
				CellFormatter cellFormatter = table.getCellFormatter();
				for ( int i = table.getRowCount() - 1; i >= 0; i-- )
					cellFormatter.addStyleName( i, 0, "headerRow" );
				add( table );
				for ( int i = table.getRowCount() - 1; i >= 0; i-- )
					for ( int j = table.getCellCount( i ) - 1; j >= 1; j-- )
						cellFormatter.setHorizontalAlignment( i, j, HasHorizontalAlignment.ALIGN_RIGHT );
				
				// Payments
				add( ClientUtils.styledWidget( new Label( "Payments:" ), "h3" ) );
				if ( quotaAndPaymentsInfo.getApiPaymentInfoList().isEmpty() ) {
					add( new Label( "There are no payments." ) );
				}
				else {
					table = new FlexTable();
					cellFormatter = table.getCellFormatter();
					table.setBorderWidth( 1 );
					table.setCellSpacing( 0 );
					table.setCellPadding( 3 );
					int row = 0;
					table.setWidget( row, 0, new Label( "Date" ) );
					table.setWidget( row, 1, new Label( "Sender" ) );
					table.setWidget( row, 2, new Label( "Gross payment" ) );
					table.setWidget( row, 3, new Label( "Net payment" ) );
					table.setWidget( row, 4, new Label( "Conversion rate" ) );
					table.setWidget( row, 5, new Label( "Paid Ops" ) );
					table.getRowFormatter().addStyleName( row, "headerRow" );
					for ( final ApiPaymentInfo apiPaymentInfo : quotaAndPaymentsInfo.getApiPaymentInfoList() ) {
						row++;
						table.setWidget( row, 0, ClientUtils.createTimestampWidget( apiPaymentInfo.getDate() ) );
						table.setWidget( row, 1, new Label( apiPaymentInfo.getPaymentSender()               ) );
						table.setWidget( row, 2, new Label( formatCents( apiPaymentInfo.getGrossPayment() ) ) );
						table.setWidget( row, 3, new Label( formatCents( apiPaymentInfo.getNetPayment  () ) ) );
						table.setWidget( row, 4, new Label( apiPaymentInfo.getNetPayment() == 0 ? "N/A" : NUMBER_FORMAT.format( apiPaymentInfo.getPaidOps() * 100 / apiPaymentInfo.getNetPayment() ) + " Ops/USD" ) );
						table.setWidget( row, 5, new Label( NUMBER_FORMAT.format( apiPaymentInfo.getPaidOps() ) ) );
						for ( int i = 2; i < 6; i++ )
							cellFormatter.setHorizontalAlignment( row, i, HasHorizontalAlignment.ALIGN_RIGHT );
					}
					add( table );
				}
			}
		} );
		
	}
	
	private static String formatCents( final int cents ) {
		final int centsPart = cents % 100;
		return NUMBER_FORMAT.format( cents / 100 ) + "." + ( centsPart < 10 ? "0" + centsPart : centsPart ) + " USD";
	}
	
}
