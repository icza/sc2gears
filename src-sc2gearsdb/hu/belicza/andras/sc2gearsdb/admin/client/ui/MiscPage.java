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

import hu.belicza.andras.sc2gearsdb.admin.client.beans.MiscFunctionInfo;
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Misc page.
 * 
 * @author Andras Belicza
 */
public class MiscPage extends AdminPage {
	
	/**
	 * Creates a new MiscPage.
	 */
	public MiscPage() {
		super( "Misc", "admin/misc" );
	}
	
	@Override
	public void buildGUI() {
		final FlexTable table = new FlexTable();
		int row = 0;
		table.setWidget( row, 0, new Label( "Use auto transactions:" ) );
		final CheckBox useAutoTxCheckBox = new CheckBox( "Auto-create transactions" );
		table.setWidget( row, 1, useAutoTxCheckBox );
		row++;
		table.setWidget( row, 0, new Label( "Function:" ) );
		final ListBox functionListBox = new ListBox();
		functionListBox.setWidth( "100%" );
		functionListBox.addItem( "Loading function list..." );
		functionListBox.setEnabled( false );
		table.setWidget( row, 1, functionListBox );
		row++;
		ClientUtils.alignTableCells( table, HasHorizontalAlignment.ALIGN_LEFT );
		add( table );
		
		final int firstParamRow = row;
		
		SERVICE_ASYNC.getMiscFunctionInfoList( new AsyncCallbackAdapter< List< MiscFunctionInfo > >( infoPanel ) {
			@Override
			public void customOnSuccess( final RpcResult< List< MiscFunctionInfo > > rpcResult ) {
				final List< MiscFunctionInfo > functionInfoList = rpcResult.getValue();
				
				functionListBox.clear();
				if ( functionInfoList == null ) {
					functionListBox.addItem( "Failed to load function list!" );
					return;
				}
				
				functionListBox.addItem( "Select a function..." );
				for ( final MiscFunctionInfo functionInfo : functionInfoList )
					functionListBox.addItem( functionInfo.getName() );
				
				add( ClientUtils.createVerticalEmptyWidget( 4 ) );
				final Button executeButton = new Button( "Execute" );
				executeButton.setEnabled( false );
				add( executeButton );
				
				functionListBox.addChangeHandler( new ChangeHandler() {
					@Override
					public void onChange( final ChangeEvent event ) {
						while ( firstParamRow < table.getRowCount() )
							table.removeRow( firstParamRow );
						
						final MiscFunctionInfo functionInfo = functionListBox.getSelectedIndex() == 0 ? null : functionInfoList.get( functionListBox.getSelectedIndex() - 1 );
						executeButton.setEnabled( functionInfo != null );
						
						if ( functionInfo == null )
							return;
						
						int row = firstParamRow;
						for ( final String paramName : functionInfo.getParamNames() ) {
							table.setWidget( row, 0, new Label( paramName + ":" ) );
							table.setWidget( row, 1, new TextBox() );
							table.getWidget( row, 1 ).setWidth( "100%" );
							row++;
						}
						ClientUtils.alignTableCells( table, HasHorizontalAlignment.ALIGN_LEFT );
					}
				} );
				
				executeButton.addClickHandler( new ClickHandler() {
					@Override
					public void onClick( ClickEvent event ) {
						useAutoTxCheckBox.setEnabled( false );
						executeButton    .setEnabled( false );
						functionListBox  .setEnabled( false );
						
						final MiscFunctionInfo functionInfo = functionInfoList.get( functionListBox.getSelectedIndex() - 1 );
						
						final String[] params = new String[ functionInfo.getParamNames().length ];
						for ( int i = 0; i < params.length; i++ ) {
							final TextBox paramTextBox = (TextBox) table.getWidget( firstParamRow + i, 1 );
							params[ i ] = paramTextBox.getText();
							paramTextBox.setEnabled( false );
						}
						SERVICE_ASYNC.executeMiscFunction( useAutoTxCheckBox.getValue(), functionInfo.getName(), params, new AsyncCallbackAdapter< String >( infoPanel ) {
							@Override
							public void customOnSuccess( final RpcResult< String > rpcResult ) {
								if ( rpcResult.getValue() != null )
									infoPanel.setInfoMessage( rpcResult.getValue() );
							}
							@Override
							public void customOnEnd() {
								useAutoTxCheckBox.setEnabled( true );
								executeButton    .setEnabled( true );
								functionListBox  .setEnabled( true );
								
								for ( int i = 0; i < params.length; i++ )
									( (TextBox) table.getWidget( firstParamRow + i, 1 ) ).setEnabled( true );
							};
						} );
					}
				} );
				
				functionListBox.setEnabled( true );
			}
		} );
	}
	
}
