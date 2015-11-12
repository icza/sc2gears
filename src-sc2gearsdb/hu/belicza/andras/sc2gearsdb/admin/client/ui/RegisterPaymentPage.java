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

import hu.belicza.andras.sc2gearsdb.admin.client.beans.PaymentInfo;
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;

/**
 * Register Payment page.
 * 
 * @author Andras Belicza
 */
public class RegisterPaymentPage extends AdminPage {
	
	/** Google account to fill the Google account input field with. */
	private final String  googleAccount;
	/** Tells if API payment is being registered.                   */
	private final Boolean apiPayment;
	
    /**
     * Creates a new RegisterPaymentPage.
     */
    public RegisterPaymentPage() {
    	this( null, null );
    }
    
    /**
     * Creates a new RegisterPaymentPage.
     * @param googleAccount Google account to fill the Google account input field with
     * @param apiPayment    tells if API payment is being registered
     */
    public RegisterPaymentPage( final String googleAccount, final Boolean apiPayment ) {
		super( "Register Payment", "admin/registerPayment" );
		
		this.googleAccount = googleAccount;
		this.apiPayment    = apiPayment;
    }
    
    @Override
    public void buildGUI() {
		final FlexTable table = new FlexTable();
		int row = -1;
		table.setWidget( ++row, 0, new Label( "API payment:" ) );
		final CheckBox apiPaymentCheckBox = new CheckBox( "API payment" );
		table.setWidget( row, 1, apiPaymentCheckBox );
		table.setWidget( ++row, 0, new Label( "Google account:" ) );
		final TextBox googleAccountTextBox = new TextBox();
		table.setWidget( row, 1, googleAccountTextBox );
		table.setWidget( row, 2, new Label( "(required)" ) );
		table.setWidget( ++row, 0, new Label( "PayPal account:" ) );
		final TextBox paypalAccountTextBox = new TextBox();
		table.setWidget( row, 1, paypalAccountTextBox );
		table.setWidget( ++row, 0, new Label( "Bank account:" ) );
		final TextBox bankAccountTextBox = new TextBox();
		table.setWidget( row, 1, bankAccountTextBox );
		table.setWidget( ++row, 0, new Label( "Virtual payment:" ) );
		final TextBox virtualPaymentTextBox = new TextBox();
		virtualPaymentTextBox.setAlignment( TextAlignment.RIGHT );
		table.setWidget( row, 1, virtualPaymentTextBox );
		table.setWidget( row, 2, new Label( "$ (USD)" ) );
		table.setWidget( ++row, 0, new Label( "Real payment:" ) );
		final TextBox realPaymentTextBox = new TextBox();
		realPaymentTextBox.setAlignment( TextAlignment.RIGHT );
		table.setWidget( row, 1, realPaymentTextBox );
		table.setWidget( row, 2, new Label( "$ (USD)" ) );
		
		table.setWidget( ++row, 0, new Label( "API Gross payment:" ) );
		final TextBox apiGrossPaymentTextBox = new TextBox();
		apiGrossPaymentTextBox.setAlignment( TextAlignment.RIGHT );
		table.setWidget( row, 1, apiGrossPaymentTextBox );
		table.setWidget( row, 2, new Label( "¢ (cents)" ) );
		table.setWidget( ++row, 0, new Label( "API Net payment:" ) );
		final TextBox apiNetPaymentTextBox = new TextBox();
		apiNetPaymentTextBox.setAlignment( TextAlignment.RIGHT );
		table.setWidget( row, 1, apiNetPaymentTextBox );
		table.setWidget( row, 2, new Label( "¢ (cents)" ) );
		
		for ( int i = table.getRowCount() - 1; i >= 0; i-- )
			table.getWidget( i, 1 ).setWidth( "220px" );
		table.setWidget( ++row, 0, new Label( "Comment:" ) );
		final TextArea commentTextArea = new TextArea();
		commentTextArea.setWidth( "100%" );
		commentTextArea.setHeight( "70px" );
		table.setWidget( row, 1, commentTextArea );
		table.getFlexCellFormatter().setColSpan( row, 1, 2 );
		ClientUtils.alignTableCells( table, HasHorizontalAlignment.ALIGN_LEFT );
		add( table );
		
		final ClickHandler apiPaymentClickHandler = new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				final boolean apiPayment = apiPaymentCheckBox.getValue();
				virtualPaymentTextBox .setEnabled( !apiPayment );
				realPaymentTextBox    .setEnabled( !apiPayment );
				apiGrossPaymentTextBox.setEnabled(  apiPayment );
				apiNetPaymentTextBox  .setEnabled(  apiPayment );
			}
		};
		apiPaymentCheckBox.addClickHandler( apiPaymentClickHandler );
		if ( apiPayment != null )
			apiPaymentCheckBox.setValue( apiPayment, true );
		apiPaymentClickHandler.onClick( null ); // Events are not fired? Need to call this explicitly
		if ( apiPayment != null )
			apiPaymentCheckBox.setEnabled( false );
		
		if ( googleAccount != null ) {
			googleAccountTextBox.setText( googleAccount );
			googleAccountTextBox.setReadOnly( true );
			paypalAccountTextBox.setFocus( true );
		}
		else
			googleAccountTextBox.setFocus( true );
		
		final Button registerPaymentButton = new Button( "Register Payment" );
		registerPaymentButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				Float   virtualPayment  = null;
				Float   realPayment     = null;
				Integer apiGrossPayment = null;
				Integer apiNetPayment   = null;
				
				final boolean apiPayment = apiPaymentCheckBox.getValue();
				
				if ( apiPayment ) {
					apiGrossPaymentTextBox.setText( apiGrossPaymentTextBox.getText().trim() );
					apiNetPaymentTextBox  .setText( apiNetPaymentTextBox  .getText().trim() );
					if ( !apiGrossPaymentTextBox.getText().isEmpty() ) {
						try {
							apiGrossPayment = Integer.valueOf( apiGrossPaymentTextBox.getText() );
							if ( apiGrossPayment < 0 )
								throw new Exception();
						} catch ( final Exception e ) {
							infoPanel.setErrorMessage( "Invalid API Gross Payment!" );
							return;
						}
					}
					if ( !apiNetPaymentTextBox.getText().isEmpty() ) {
						try {
							apiNetPayment = Integer.valueOf( apiNetPaymentTextBox.getText() );
							if ( apiNetPayment < 0 )
								throw new Exception();
						} catch ( final Exception e ) {
							infoPanel.setErrorMessage( "Invalid API Net Payment!" );
							return;
						}
					}
				}
				else {
					virtualPaymentTextBox.setText( virtualPaymentTextBox.getText().trim() );
					realPaymentTextBox   .setText( realPaymentTextBox   .getText().trim() );
					if ( !virtualPaymentTextBox.getText().isEmpty() ) {
						try {
							virtualPayment = Float.valueOf( virtualPaymentTextBox.getText() );
							if ( virtualPayment.floatValue() < 0f )
								throw new Exception();
						} catch ( final Exception e ) {
							infoPanel.setErrorMessage( "Invalid Virtual Payment!" );
							return;
						}
					}
					if ( !realPaymentTextBox.getText().isEmpty() ) {
						try {
							realPayment = Float.valueOf( realPaymentTextBox.getText() );
							if ( realPayment.floatValue() < 0f )
								throw new Exception();
						} catch ( final Exception e ) {
							infoPanel.setErrorMessage( "Invalid Real Payment!" );
							return;
						}
					}
				}
				
				googleAccountTextBox.setText( googleAccountTextBox.getText().trim() );
				paypalAccountTextBox.setText( paypalAccountTextBox.getText().trim() );
				bankAccountTextBox  .setText( bankAccountTextBox  .getText().trim() );
				commentTextArea     .setText( commentTextArea     .getText().trim() );
				
				setComponentsEnabled( false );
				registerPaymentButton.setEnabled( false );
				final PaymentInfo paymentInfo = new PaymentInfo();
				paymentInfo.setApiPayment( apiPayment );
				paymentInfo.setGoogleAccount( googleAccountTextBox.getText() );
				if ( !paypalAccountTextBox.getText().isEmpty() )
					paymentInfo.setPaypalAccount( paypalAccountTextBox.getText() );
				if ( !bankAccountTextBox.getText().isEmpty() )
					paymentInfo.setBankAccount( bankAccountTextBox.getText() );
				if ( virtualPayment != null )
					paymentInfo.setVirtualPayment( virtualPayment );
				if ( realPayment != null )
					paymentInfo.setRealPayment( realPayment );
				if ( apiGrossPayment != null )
					paymentInfo.setApiGrossPayment( apiGrossPayment );
				if ( apiNetPayment != null )
					paymentInfo.setApiNetPayment( apiNetPayment );
				final String comment = commentTextArea.getText();
				if ( !comment.isEmpty() )
					paymentInfo.setComment( comment );
				SERVICE_ASYNC.registerPayment( paymentInfo, new AsyncCallbackAdapter< Void >( infoPanel ) {
					@Override
					public void customOnSuccess( final RpcResult< Void > rpcResult ) {
						if ( rpcResult.getErrorMsg() != null )
							registerPaymentButton.setEnabled( true );
					}
					@Override
					public void customOnEnd() {
						setComponentsEnabled( true );
						registerPaymentButton.setEnabled( true );
					};
				} );
			}
			private void setComponentsEnabled( final boolean enabled ) {
				apiPaymentCheckBox  .setEnabled( apiPayment == null && enabled );
				googleAccountTextBox.setEnabled( googleAccount == null && enabled );
				paypalAccountTextBox.setEnabled( enabled );
				bankAccountTextBox  .setEnabled( enabled );
				commentTextArea     .setEnabled( enabled );
				if ( enabled )
					apiPaymentClickHandler.onClick( null );
				else {
					virtualPaymentTextBox .setEnabled( false );
					realPaymentTextBox    .setEnabled( false );
					apiGrossPaymentTextBox.setEnabled( false );
					apiNetPaymentTextBox  .setEnabled( false );
				}
			}
		} );
		add( registerPaymentButton );
    }
	
}
