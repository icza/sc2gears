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

import hu.belicza.andras.sc2gearsdb.admin.client.Admin;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.AccountInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.NewAccountSuggestion;
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Create Account page.
 * 
 * @author Andras Belicza
 */
public class CreateAccountPage extends AdminPage {
	
    /**
     * Creates a new CreateAccountPage.
     */
    public CreateAccountPage() {
		super( "Create Account", "admin/createAccount" );
    }
    
    @Override
    public void buildGUI() {
		final FlexTable table = new FlexTable();
		int row = -1;
		table.setWidget( ++row, 0, new Label( "Suggestions:" ) );
		final ListBox suggestionListBox = new ListBox();
		suggestionListBox.addItem( "Loading suggestions..." );
		suggestionListBox.setEnabled( false );
		table.setWidget( row, 1, suggestionListBox );
		table.setWidget( ++row, 0, new Label( "Name:" ) );
		final TextBox nameTextBox = new TextBox();
		table.setWidget( row, 1, nameTextBox );
		table.setWidget( ++row, 0, new Label( "Google account:" ) );
		final TextBox googleAccountTextBox = new TextBox();
		table.setWidget( row, 1, googleAccountTextBox );
		table.setWidget( row, 2, new Label( "(required)" ) );
		table.setWidget( ++row, 0, new Label( "Contact email:" ) );
		final TextBox contactEmailTextBox = new TextBox();
		table.setWidget( row, 1, contactEmailTextBox );
		table.setWidget( ++row, 0, new Label( "Country:" ) );
		final TextBox countryTextBox = new TextBox();
		table.setWidget( row, 1, countryTextBox );
		for ( int i = table.getRowCount() - 1; i >= 0; i-- )
			table.getWidget( i, 1 ).setWidth( "220px" );
		table.setWidget( ++row, 0, new Label( "Comment:" ) );
		final TextArea commentTextArea = new TextArea();
		commentTextArea.setWidth( "100%" );
		commentTextArea.setHeight( "70px" );
		commentTextArea.setText( "UNUSED" );
		table.setWidget( row, 1, commentTextArea );
		table.getFlexCellFormatter().setColSpan( row, 1, 2 );
		ClientUtils.alignTableCells( table, HasHorizontalAlignment.ALIGN_LEFT );
		add( table );
		
		nameTextBox.setFocus( true );
		
		final Button createFreeAccountButton               = new Button( "Create Free Account" );
		final Button createAccountAndRegisterPaymentButton = new Button( "Create Account & Register Payment" );
		final Button createApiAccAndRegApiPaymentButton    = new Button( "Create API Acc & Register Payment" );
		final Button[] createButtons = { createFreeAccountButton, createAccountAndRegisterPaymentButton, createApiAccAndRegApiPaymentButton };
		
		final ClickHandler createButtonClickHandler = new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				setComponentsEnabled( false );
				nameTextBox         .setText( nameTextBox         .getText().trim() );
				googleAccountTextBox.setText( googleAccountTextBox.getText().trim() );
				contactEmailTextBox .setText( contactEmailTextBox .getText().trim() );
				countryTextBox      .setText( countryTextBox      .getText().trim() );
				commentTextArea     .setText( commentTextArea     .getText().trim() );
				
				final AccountInfo accountInfo = new AccountInfo();
				if ( !nameTextBox.getText().isEmpty() )
					accountInfo.setName( nameTextBox.getText() );
				accountInfo.setGoogleAccount( googleAccountTextBox.getText() );
				if ( !contactEmailTextBox.getText().isEmpty() )
					accountInfo.setContactEmail( contactEmailTextBox.getText() );
				if ( !countryTextBox.getText().isEmpty() )
					accountInfo.setCountry( countryTextBox.getText() );
				if ( !commentTextArea.getText().isEmpty() )
					accountInfo.setComment( commentTextArea.getText() );
				accountInfo.setFreeAccount( event.getSource() == createFreeAccountButton );
				accountInfo.setApiAccount( event.getSource() == createApiAccAndRegApiPaymentButton );
				SERVICE_ASYNC.createAccount( accountInfo, new AsyncCallbackAdapter< Void >( infoPanel ) {
					@Override
					public void customOnSuccess( final RpcResult< Void > rpcResult ) {
						if ( rpcResult.getErrorMsg() == null ) {
							if ( !accountInfo.isFreeAccount() )
								Admin.menu.setPage( new RegisterPaymentPage( accountInfo.getGoogleAccount(), accountInfo.isApiAccount() ) );
						}
					}
					@Override
					public void customOnEnd() {
						setComponentsEnabled( true );
					};
				} );
			}
			private void setComponentsEnabled( final boolean enabled ) {
				for ( final Button button : createButtons )
					button.setEnabled( enabled );
				suggestionListBox   .setEnabled( enabled && suggestionListBox.getItemCount() > 0 );
				nameTextBox         .setEnabled( enabled );
				googleAccountTextBox.setEnabled( enabled );
				contactEmailTextBox .setEnabled( enabled );
				countryTextBox      .setEnabled( enabled );
				commentTextArea     .setEnabled( enabled );
			}
		};
		
		for ( final Button button : createButtons )
			button.addClickHandler( createButtonClickHandler );
		
		final VerticalPanel buttonsPanel = new VerticalPanel();
		for ( final Button button : createButtons ) {
			button.setWidth( "100%" );
			button.setHeight( button == createFreeAccountButton ? "35px" : "30px" );
			buttonsPanel.add( button );
			buttonsPanel.add( ClientUtils.createVerticalEmptyWidget( 10 ) );
		}
		add( buttonsPanel );
		
		SERVICE_ASYNC.getNewAccountSuggestionList( new AsyncCallbackAdapter< List< NewAccountSuggestion > >( infoPanel ) {
			@Override
            public void customOnSuccess( final RpcResult< List< NewAccountSuggestion > > rpcResult ) {
				final List< NewAccountSuggestion > newAccSuggestionList = rpcResult.getValue();
				suggestionListBox.clear();
				if ( newAccSuggestionList == null || newAccSuggestionList.isEmpty() ) {
					suggestionListBox.addItem( "Failed to load suggestions!" );
					return;
				}
				
				for ( final NewAccountSuggestion newAccSuggestion : newAccSuggestionList )
					suggestionListBox.addItem( newAccSuggestion.getGoogleAccount() == null ? " " : newAccSuggestion.getCountryCode() + " - " + newAccSuggestion.getGoogleAccount() );
				
				suggestionListBox.addChangeHandler( new ChangeHandler() {
					@Override
					public void onChange( final ChangeEvent event ) {
						final int selectedSuggestionIdx = suggestionListBox.getSelectedIndex();
						if ( selectedSuggestionIdx < 0 )
							return;
						googleAccountTextBox.setText( newAccSuggestionList.get( selectedSuggestionIdx ).getGoogleAccount() );
						countryTextBox      .setText( newAccSuggestionList.get( selectedSuggestionIdx ).getCountryName  () );
						
						if ( nameTextBox.getText().isEmpty() )
							nameTextBox.setFocus( true );
						else
							contactEmailTextBox.setFocus( true );
					}
				} );
				
				suggestionListBox.setEnabled( true );
            }
		} );
    }
	
}
