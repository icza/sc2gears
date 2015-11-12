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
import hu.belicza.andras.sc2gearsdb.admin.client.beans.FileStatInfo;
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.Filter;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.common.client.Task;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * File Stats page.
 * 
 * @author Andras Belicza
 */
public class FileStatsPage extends HasFileStatInfoTablePage {
	
	/**
	 * A click handler which when clicked opens the google account acquired by the {@link Anchor#getText()} call.
	 * The {@link Object#toString()} or the handler returns a string which can be used as the tool tip for the target widget.
	 */
	public static final ClickHandler OPEN_GOOGLE_ACCOUNT_CLICK_HANDLER = new ClickHandler() {
		@Override
		public void onClick( final ClickEvent event ) {
			Admin.menu.setPage( new FileStatsPage( ( (Anchor) event.getSource() ).getText() ) );
		}
		@Override
		public String toString() {
		    return "Show complete file stats of this account.";
		}
	};
	
	/**
	 * Creates and returns a link with the text being the specified google account and which when clicked
	 * opens the complete file stats of the specified account.
	 * @param googleAccount google account to create a link for
	 * @return the created new link
	 */
	public static Anchor createLinkForOpenGoogleAccount( final String googleAccount ) {
		final Anchor googleAccountAnchor = new Anchor( googleAccount );
		
		googleAccountAnchor.setTitle( OPEN_GOOGLE_ACCOUNT_CLICK_HANDLER.toString() );
		googleAccountAnchor.addClickHandler( OPEN_GOOGLE_ACCOUNT_CLICK_HANDLER );
		
		return googleAccountAnchor;
	}
	
	/** Google account to fill the Google account input field with. */
	private final String googleAccount;
	
	/**
	 * Creates a new FileStatsPage.
	 */
	public FileStatsPage() {
		this( null );
	}
	
	/**
	 * Creates a new FileStatsPage.
	 * @param googleAccount Google account to fill the Google account input field with
	 */
	public FileStatsPage( final String googleAccount ) {
		super( "File Stats", "admin/fileStats" );
		this.googleAccount = googleAccount;
	}
	
	@Override
	public void buildGUI() {
		HorizontalPanel filtersPanel = new HorizontalPanel();
		filtersPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		filtersPanel.add( new Label( "Google Account:" ) );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 2 ) );
		final TextBox googleAccountTextBox = new TextBox();
		googleAccountTextBox.setWidth( "210px" );
		if ( googleAccount != null )
			googleAccountTextBox.setText( googleAccount );
		filtersPanel.add( googleAccountTextBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 2 ) );
		final Button goButton = new Button( "Go" );
		ClientUtils.styleSmallButton( goButton );
		DOM.setStyleAttribute( goButton.getElement(), "fontWeight", "bold" );
		filtersPanel.add( goButton );
		ClientUtils.addEnterTarget( googleAccountTextBox, goButton );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 6 ) );
		final Button clearButton = new Button( "Clear" );
		ClientUtils.styleSmallButton( clearButton );
		filtersPanel.add( clearButton );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 20 ) );
		filtersPanel.add( new Label( "Display Filter:" ) );
		final ListBox displayFilterListBox = new ListBox();
		displayFilterListBox.addItem( "All" );
		displayFilterListBox.addItem( "Paid" );
		displayFilterListBox.addItem( "Free" );
		filtersPanel.add( displayFilterListBox );
		add( filtersPanel );
		
		filtersPanel = new HorizontalPanel();
		filtersPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		filtersPanel.add( new Label( "Active in last:" ) );
		final TextBox activeLastTextBox = new TextBox();
		activeLastTextBox.setWidth( "50px" );
		filtersPanel.add( activeLastTextBox );
		final ListBox activeLastUnitListBox = new ListBox();
		activeLastUnitListBox.addItem( "min"  , "1"      );
		activeLastUnitListBox.addItem( "hour" , "60"     );
		activeLastUnitListBox.addItem( "day"  , "1440"   );
		activeLastUnitListBox.addItem( "week" , "10080"  );
		activeLastUnitListBox.addItem( "month", "43200"  );
		activeLastUnitListBox.addItem( "year" , "525600" );
		activeLastUnitListBox.setSelectedIndex( 2 );
		filtersPanel.add( activeLastUnitListBox );
		filtersPanel.add( ClientUtils.createHorizontalEmptyWidget( 10 ) );
		filtersPanel.add( new Label( "Min stored:" ) );
		final TextBox minStoredTextBox = new TextBox();
		minStoredTextBox.setWidth( "50px" );
		minStoredTextBox.setText( "1" );
		filtersPanel.add( minStoredTextBox );
		final ListBox minStoredUnitListBox = new ListBox();
		minStoredUnitListBox.addItem( "Bytes", "1"          );
		minStoredUnitListBox.addItem( "KB"   , "1024"       );
		minStoredUnitListBox.addItem( "MB"   , "1048576"    );
		minStoredUnitListBox.addItem( "GB"   , "1073741824" );
		minStoredUnitListBox.setSelectedIndex( 2 );
		filtersPanel.add( minStoredUnitListBox );
		add( filtersPanel );
		
		clearButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				googleAccountTextBox.setText( "" );
				activeLastTextBox   .setText( "" );
				minStoredTextBox    .setText( "" );
			}
		} );
		
		add( ClientUtils.createVerticalEmptyWidget( 7 ) );
		
		final FlexTable fileStatTable = new FlexTable();
		
		final ClickHandler googleAccountClickHandler = new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				final String googleAccount = ( (Anchor) event.getSource() ).getText();
				if ( !Window.confirm( "Recalculate file info stats for " + googleAccount + "?" ) )
					return;
				// Recalculate file info stats of the clicked google account
				SERVICE_ASYNC.recalculateFileInfoStats( googleAccount, new AsyncCallbackAdapter< Void >( infoPanel ) {
					@Override
					public void customOnSuccess( final RpcResult< Void > rpcResult ) {
						// Nothing to do here
					}
				} );
			}
			@Override
			public String toString() {
			    return "Recalculate file info stats of this account";
			}
		};
		
		// List returned by the server is stored here
		final List< FileStatInfo > fileStatInfoList = new ArrayList< FileStatInfo >();
		
		final Task refreshTableTask = new Task() {
			@Override
			public void execute() {
				final Filter< FileStatInfo > filter;
				switch ( displayFilterListBox.getSelectedIndex() ) {
				case 0 : default : filter = null; break;
				case 1 :
					filter = new Filter< FileStatInfo >() {
    					@Override
                        public boolean accept( final FileStatInfo value ) {
    	                    return value.getPaidStorage() > 5_000_000;
                        }
    				};
    				break;
				case 2 :
					filter = new Filter< FileStatInfo >() {
    					@Override
                        public boolean accept( final FileStatInfo value ) {
    	                    return value.getPaidStorage() == 5_000_000;
                        }
    				};
				}
				buildFileStatInfoTable( fileStatInfoList, fileStatTable, googleAccountClickHandler, filter );
			}
		};
		
		goButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				String  googleAccount  = null;
				Integer activeLastMins = null;
				Long    minStoredBytes = null;
				
				if ( !googleAccountTextBox.getText().trim().isEmpty() ) {
					googleAccount = googleAccountTextBox.getText().trim();
				}
				else if ( !activeLastTextBox.getText().isEmpty() ) {
    				try {
    					activeLastMins  = Integer.parseInt( activeLastTextBox.getText() );
    					activeLastMins *= Integer.parseInt( activeLastUnitListBox.getValue( activeLastUnitListBox.getSelectedIndex() ) );
    				} catch ( final NumberFormatException nfe ) {
    					activeLastTextBox.setFocus( true );
    					infoPanel.setErrorMessage( "Invalid active last minutes!" );
    					return;
    				}
				}
				else if ( !minStoredTextBox.getText().isEmpty() ) {
    				try {
    					minStoredBytes  = Long.parseLong( minStoredTextBox.getText() );
    					minStoredBytes *= Long.parseLong( minStoredUnitListBox.getValue( minStoredUnitListBox.getSelectedIndex() ) );
    				} catch ( final NumberFormatException nfe ) {
    					minStoredTextBox.setFocus( true );
    					infoPanel.setErrorMessage( "Invalid min stored bytes!" );
    					return;
    				}
				}
				
				googleAccountTextBox .setEnabled( false );
				activeLastTextBox    .setEnabled( false );
				activeLastUnitListBox.setEnabled( false );
				minStoredTextBox     .setEnabled( false );
				minStoredUnitListBox .setEnabled( false );
				goButton             .setEnabled( false );
				clearButton          .setEnabled( false );
				displayFilterListBox .setEnabled( false );
				
				SERVICE_ASYNC.getFileStatInfoList( googleAccount, activeLastMins, minStoredBytes, new AsyncCallbackAdapter< List< FileStatInfo > >( infoPanel ) {
					@Override
					public void customOnSuccess( final RpcResult< List< FileStatInfo > > rpcResult ) {
						final List< FileStatInfo > result = rpcResult.getValue();
						if ( result == null )
							return;
						fileStatInfoList.clear();
						fileStatInfoList.addAll( result );
						refreshTableTask.execute();
					}
					@Override
					public void customOnEnd() {
						googleAccountTextBox .setEnabled( true );
						activeLastTextBox    .setEnabled( true );
						activeLastUnitListBox.setEnabled( true );
						minStoredTextBox     .setEnabled( true );
						minStoredUnitListBox .setEnabled( true );
						goButton             .setEnabled( true );
						clearButton          .setEnabled( true );
						displayFilterListBox .setEnabled( true );
					}
				} );
			}
		} );
		
		displayFilterListBox.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange( final ChangeEvent event ) {
				refreshTableTask.execute();
			}
		} );
		
		if ( googleAccount != null )
			goButton.click();
	}
	
}
