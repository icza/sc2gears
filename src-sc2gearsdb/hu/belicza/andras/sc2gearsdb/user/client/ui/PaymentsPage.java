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
import hu.belicza.andras.sc2gearsdb.user.client.beans.DbPackageInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.PaymentInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.PaymentsInfo;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;

/**
 * Payments page.
 * 
 * @author Andras Belicza
 */
public class PaymentsPage extends UserPage {
	
	/**
	 * Creates a new QuotaPage.
	 */
	public PaymentsPage() {
		super( "Payments", "user/payments" );
	}
	
	@Override
	public void buildGUI() {
		if ( !checkPagePermission( Permission.VIEW_PAYMENTS ) )
			return;
		
		SERVICE_ASYNC.getPaymentsInfo( getSharedAccount(), new AsyncCallbackAdapter< PaymentsInfo >( infoPanel ) {
			@Override
			public void customOnSuccess( final RpcResult< PaymentsInfo > rpcResult ) {
				final PaymentsInfo paymentsInfo = rpcResult.getValue();
				if ( paymentsInfo == null )
					return;
				
				final NumberFormat CURRENCY_FORMAT = NumberFormat.getFormat( "#,###.##" );
				
				float totalPayments = 0;
				for ( final PaymentInfo paymentInfo : paymentsInfo.getPaymentInfoList() )
					totalPayments += paymentInfo.getPayment();
				
				// Payments
				add( ClientUtils.styledWidget( new Label( "Payment history:" ), "h3" ) );
				if ( paymentsInfo.getPaymentInfoList().isEmpty() ) {
					add( new Label( "You have no payments." ) );
				}
				else {
					final FlexTable table = new FlexTable();
					final CellFormatter cellFormatter = table.getCellFormatter();
					final RowFormatter  rowFormatter  = table.getRowFormatter ();
					table.setBorderWidth( 1 );
					table.setCellSpacing( 0 );
					table.setCellPadding( 3 );
					int row = 0;
					table.setWidget( row, 0, new Label( "Date" ) );
					table.setWidget( row, 1, new Label( "Payment sender" ) );
					table.setWidget( row, 2, new Label( "Payment" ) );
					table.getRowFormatter().addStyleName( row, "headerRow" );
					// Total payments:
					row++;
					table.setWidget( row, 1, new Label( "TOTAL: ∑ ALL" ) );
					table.setWidget( row, 2, new Label( CURRENCY_FORMAT.format( totalPayments ) + " USD" ) );
					cellFormatter.setHorizontalAlignment( row, 2, HasHorizontalAlignment.ALIGN_RIGHT );
					rowFormatter.addStyleName( row, "gold" );
					for ( final PaymentInfo paymentInfo : paymentsInfo.getPaymentInfoList() ) {
						row++;
						table.setWidget( row, 0, ClientUtils.createTimestampWidget( paymentInfo.getDate() ) );
						table.setWidget( row, 1, new Label( paymentInfo.getPaymentSender() == null || paymentInfo.getPaymentSender().isEmpty() ? "N/A" : paymentInfo.getPaymentSender() ) );
						table.setWidget( row, 2, new Label( CURRENCY_FORMAT.format( paymentInfo.getPayment() ) + " USD" ) );
						cellFormatter.setHorizontalAlignment( row, 2, HasHorizontalAlignment.ALIGN_RIGHT );
						rowFormatter.addStyleName( row, ( row & 0x01 ) == 0 ? "row0" : "row1" );
					}
					add( table );
				}
				
				// Current package price table
				add( ClientUtils.styledWidget( new Label( "Current Package prices:" ), "h3" ) );
				final FlexTable table = new FlexTable();
				final CellFormatter cellFormatter = table.getCellFormatter();
				final RowFormatter  rowFormatter  = table.getRowFormatter ();
				table.setBorderWidth( 1 );
				table.setCellSpacing( 0 );
				table.setCellPadding( 3 );
				int row = 0;
				table.setWidget( row, 0, new Label( "Package" ) );
				table.setWidget( row, 1, new HTML ( "Price <sup>(1)</sup>" ) );
				table.setWidget( row, 2, new HTML ( "Storage <sup>(2)</sup>" ) );
				table.setWidget( row, 3, new HTML ( "# of replays <sup>(3)</sup>" ) );
				table.setWidget( row, 4, new HTML ( "Replays stored<br>for 1 USD" ) );
				table.setWidget( row, 5, new HTML ( "Payment required<br>to upgrade to" ) );
				table.getRowFormatter().addStyleName( row, "headerRow" );
				row++;
				final int firstBuyableDbPackageInfoIdx = paymentsInfo.getFirstBuyableDbPackageInfoIdx() == 0 ? Integer.MAX_VALUE : paymentsInfo.getFirstBuyableDbPackageInfoIdx();
				int idx = 0;
				for ( final DbPackageInfo dbPackageInfo : paymentsInfo.getDbPackageInfoList() ) {
					row++;
					final HorizontalPanel packagePanel = new HorizontalPanel();
					packagePanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
					packagePanel.add( new Image( dbPackageInfo.getDbPackageIcon() ) );
					packagePanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
					packagePanel.add( new Label( dbPackageInfo.getDbPackageName() ) );
					table.setWidget( row, 0, packagePanel );
					table.setWidget( row, 1, new Label( CURRENCY_FORMAT.format( dbPackageInfo.getPriceUSD() ) + " USD" ) );
					table.setWidget( row, 2, new Label( dbPackageInfo.getStorage() >= 1000000000L ? ( dbPackageInfo.getStorage() / 1000000000L ) + " GB" : ( dbPackageInfo.getStorage() / 1000000L ) + " MB" ) );
					table.setWidget( row, 3, new Label( NUMBER_FORMAT.format( dbPackageInfo.getNumOfReplays() ) ) );
					table.setWidget( row, 4, new Label( dbPackageInfo.getPriceUSD() == 0 ? "-" : NUMBER_FORMAT.format( dbPackageInfo.getNumOfReplays() / dbPackageInfo.getPriceUSD() ) ) );
					table.setWidget( row, 5, new Label( idx >= firstBuyableDbPackageInfoIdx ? CURRENCY_FORMAT.format( dbPackageInfo.getPriceUSD() - totalPayments ) + " USD" : "-" ) );
					cellFormatter.setHorizontalAlignment( row, 2, HasHorizontalAlignment.ALIGN_RIGHT );
					rowFormatter.addStyleName( row, paymentsInfo.getCurrentDbPackageInfoIdx() == idx ? "winRow0" : ( row & 0x01 ) == 0 ? "row0" : "row1" );
					for ( int i = 1; i < 6; i++ )
						cellFormatter.setHorizontalAlignment( row, i, HasHorizontalAlignment.ALIGN_RIGHT );
					idx++;
				}
				add( table );
				
				final VerticalPanel notesPanel = new VerticalPanel();
				notesPanel.setWidth( "600px" );
				final String[] notes = new String[] {
					"(1) Prices indicate a one-time fee, there are no recurring monthly costs. Prices include the costs of all necessary resources (storage, incoming and outgoing bandwidth, CPU usage).",
					"(2) Storage conversion: 1 MB = 1,000,000 bytes, 1 GB = 1,000,000,000 bytes",
					"(3) The number of replays is an approximation. Size of replays depends on their length, number of players, number of actions."
				};
				for ( final String note : notes ) {
    				final Label noteLabel = ClientUtils.styledWidget( new Label( note ), "explanation" );
    				notesPanel.add( noteLabel );
				}
				add( notesPanel );
			}
		} );
		
	}
	
}
