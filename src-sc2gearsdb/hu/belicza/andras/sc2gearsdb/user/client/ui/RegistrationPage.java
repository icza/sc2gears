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

import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.user.client.beans.FreeAccountInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Registration page.
 * 
 * @author Andras Belicza
 */
public class RegistrationPage extends UserPage {
	
	/**
	 * Creates a new RegistrationPage.
	 */
	public RegistrationPage() {
		super( "Create your account in 30 seconds! It is Free!", "user/registration" );
	}
	
	@Override
	public void buildGUI() {
		// Color title to green
		DOM.setStyleAttribute( getWidget( 0 ).getElement(), "color", "green" );
		
		final FlexTable table = new FlexTable();
		int row = -1;
		
		table.setWidget( ++row, 0, new Label( "Google account:" ) );
		final TextBox googleAccountTextBox = new TextBox();
		googleAccountTextBox.setWidth( "200px" );
		googleAccountTextBox.setText( userInfo.getUserName() );
		googleAccountTextBox.setReadOnly( true );
		table.setWidget( row, 1, googleAccountTextBox );
		table.setWidget( row, 2, ClientUtils.styledWidget( new Label( "This is what you are logged in with. You can request to change your Google account later on." ), "explanation" ) );
		
		table.setWidget( ++row, 0, new Label( "Name:" ) );
		final TextBox nameTextBox = new TextBox();
		nameTextBox.setWidth( "200px" );
		table.setWidget( row, 1, nameTextBox );
		table.setWidget( row, 2, ClientUtils.styledWidget( new Label( "Optional. If provided, you will be addressed by this name." ), "explanation" ) );
		
		table.setWidget( ++row, 0, new Label( "Contact email:" ) );
		final TextBox contactEmailTextBox = new TextBox();
		contactEmailTextBox.setWidth( "200px" );
		table.setWidget( row, 1, contactEmailTextBox );
		table.setWidget( row, 2, ClientUtils.styledWidget( new Label( "Optional. If provided, emails will be sent both to your Google account and to your contact email." ), "explanation" ) );
		
		ClientUtils.alignTableCells( table, HasHorizontalAlignment.ALIGN_LEFT );
		for ( int i = table.getRowCount() - 1; i >= 0; i-- )
			table.getRowFormatter().addStyleName( i, ( i & 0x01 ) == 0 ? "row0" : "row1"  );
		add( table );
		nameTextBox.setFocus( true );
		
		add( ClientUtils.createVerticalEmptyWidget( 10 ) );
		final Button createFreeAccountButton = ClientUtils.styledWidget( new Button( "Create Free Account!" ), "strong" );
		DOM.setStyleAttribute( createFreeAccountButton.getElement(), "padding", "10px 30px" );
		createFreeAccountButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				Window.alert("Registration is disabled!");
			}
			public void onClick2( final ClickEvent event ) {
				if (true) {
					return; // Registration disabled
				}
				setComponentsEnabled( false );
				
				googleAccountTextBox.setText( googleAccountTextBox.getText().trim() );
				nameTextBox         .setText( nameTextBox         .getText().trim() );
				contactEmailTextBox .setText( contactEmailTextBox .getText().trim() );
				
				final FreeAccountInfo freeAccountInfo = new FreeAccountInfo();
				freeAccountInfo.setGoogleAccount( googleAccountTextBox.getText() );
				if ( !nameTextBox.getText().isEmpty() )
					freeAccountInfo.setName( nameTextBox.getText() );
				if ( !contactEmailTextBox.getText().isEmpty() )
					freeAccountInfo.setContactEmail( contactEmailTextBox.getText() );
				
				SERVICE_ASYNC.register( freeAccountInfo, new AsyncCallbackAdapter< Void >( infoPanel ) {
					@Override
                    public void customOnSuccess( final RpcResult< Void > rpcResult ) {
						if ( rpcResult.getErrorMsg() != null )
							return;
						
						final DialogBox dialogBox = new DialogBox();
						dialogBox.setGlassEnabled( true );
						dialogBox.setText( "Congratulations!" );
						final VerticalPanel content = new VerticalPanel();
						DOM.setStyleAttribute( content.getElement(), "padding", "20px" );
						content.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
						content.add( ClientUtils.styledWidget( new Label( "Congratulations! Your Sc2gears Database Account is ready!" ), "strong" ) );
						content.add( ClientUtils.createVerticalEmptyWidget( 15 ) );
						content.add( new Label( "A notification email has been sent to your email address" + ( freeAccountInfo.getContactEmail() == null ? "." : "es." ) ) );
						content.add( ClientUtils.createVerticalEmptyWidget( 15 ) );
						final Button proceedToAccountButton = new Button( "Proceed to your Account" );
						proceedToAccountButton.addClickHandler( new ClickHandler() {
							@Override
							public void onClick( final ClickEvent event ) {
								Window.Location.reload();
							}
						} );
						content.add( proceedToAccountButton );
						dialogBox.setWidget( content );
						
						dialogBox.center();
						proceedToAccountButton.setFocus( true );
                    }
					@Override
					public void customOnEnd() {
						setComponentsEnabled( true );
					};
				} );
			}
			private void setComponentsEnabled( final boolean enabled ) {
				createFreeAccountButton.setEnabled( enabled );
				googleAccountTextBox   .setEnabled( enabled );
				nameTextBox            .setEnabled( enabled );
				contactEmailTextBox    .setEnabled( enabled );
			}
		} );
		add( createFreeAccountButton );
		
		add( ClientUtils.createVerticalEmptyWidget( 10 ) );
		add( new HTML( "<p>You can read more about the Sc2gears Database <a href=\"https://sites.google.com/site/sc2gears/sc2gears-database\" target=\"_blank\">here</a>." +
				"<br/>By creating an account you agree to the <a href=\"https://sites.google.com/site/sc2gears/sc2gears-database#TOC-Terms-of-Service\" target=\"_blank\">Terms of Service</a> and <a href=\"https://sites.google.com/site/sc2gears/sc2gears-database#TOC-Privacy-Policy\" target=\"_blank\">Privacy Policy</a>.</p>" ) );
	}
	
}
