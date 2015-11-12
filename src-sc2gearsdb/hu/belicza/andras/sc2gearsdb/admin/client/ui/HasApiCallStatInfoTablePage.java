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

import static hu.belicza.andras.sc2gearsdb.common.client.ClientUtils.NUMBER_FORMAT;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.ApiCallStatInfo;

import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;

/**
 * A page which has an API call stat info table.
 * 
 * @author Andras Belicza
 */
abstract class HasApiCallStatInfoTablePage extends AdminPage {
	
	/**
	 * Creates a new HasApiCallStatInfoTablePage.
	 * @param title    displayable title of the screen; can be <code>null</code>
	 * @param pageName page name to be logged
	 */
	public HasApiCallStatInfoTablePage( final String title, final String pageName ) {
		super( title, pageName );
	}
	
	/**
	 * Builds the API call stat info table.
	 * 
	 * <p>On first call the table is initialized, on later calls its content is first removed.</p>
	 * 
	 * @param apiCallStatInfoList       API call stat info list to build the table from
	 * @param table                     table to build
	 * @param googleAccountClickHandler if provided, google accounts will be rendered as links (@link {@link Anchor}),
	 * 		and clicking on them will call this handler; the tool tip of links will be <code>googleAccountClickHandler.toString()</code>
	 */
	protected void buildApiCallStatInfoTable( final List< ApiCallStatInfo > apiCallStatInfoList, final FlexTable table, final ClickHandler googleAccountClickHandler ) {
		// Build table
		if ( table.getRowCount() == 0 ) {
			// Init table
			table.setBorderWidth( 1 );
			table.setCellSpacing( 0 );
			table.setCellPadding( 3 );
			add( table );
		}
		else
			table.removeAllRows();
		
		final RowFormatter rowFormatter = table.getRowFormatter();
		
		int column = 0;
		table.setWidget( 0, column++, new Label( "#"               ) );
		table.setWidget( 0, column++, new Label( "User"            ) );
		table.setWidget( 0, column++, new Label( "Paid Ops"        ) );
		table.setWidget( 0, column++, new Label( "∑Calls"          ) );
		table.setWidget( 0, column++, new Label( "Used Ops"        ) );
		table.setWidget( 0, column++, new Label( "Avg Time"        ) );
		table.setWidget( 0, column++, new Label( "Denied"          ) );
		table.setWidget( 0, column++, new Label( "Errors"          ) );
		table.setWidget( 0, column++, new Label( "Info"            ) );
		table.setWidget( 0, column++, new Label( "Avg Info T"      ) );
		table.setWidget( 0, column++, new Label( "Map info"        ) );
		table.setWidget( 0, column++, new Label( "Avg Map info T"  ) );
		table.setWidget( 0, column++, new Label( "Parse rep"       ) );
		table.setWidget( 0, column++, new Label( "Avg Parse rep T" ) );
		table.setWidget( 0, column++, new Label( "Prof info"       ) );
		table.setWidget( 0, column++, new Label( "Avg Prof info T" ) );
		table.setWidget( 0, column++, new Label( "Share"           ) );
		rowFormatter.addStyleName( 0, "headerRow" );
		
		final CellFormatter cellFormatter = table.getCellFormatter();
		
		final int rowsCount = apiCallStatInfoList.size();
		int userCounter = 0;
		for ( int row = 1; row <= rowsCount; row++ ) {
			final ApiCallStatInfo info = apiCallStatInfoList.get( row - 1 );
			column = 0;
			
			table.setWidget( row, column++, new Label( userCounter + "." ) );
			cellFormatter.setHorizontalAlignment( row, column-1, HasHorizontalAlignment.ALIGN_RIGHT );
			if ( googleAccountClickHandler == null || userCounter == 0 )
				table.setWidget( row, column++, new Label( info.getGoogleAccount() ) );
			else {
				final Anchor googleAccountAnchor = new Anchor( info.getGoogleAccount() );
				googleAccountAnchor.setTitle( googleAccountClickHandler.toString() );
				googleAccountAnchor.addClickHandler( googleAccountClickHandler );
				table.setWidget( row, column++, googleAccountAnchor );
			}
			userCounter++;
			
			final int firstNumberColumn = column;
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getPaidOps            () )         ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getCalls              () )         ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getUsedOps            () )         ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getAvgExecTime        () ) + " ms" ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getDeniedCalls        () )         ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getErrors             () )         ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getInfoCalls          () )         ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getAvgInfoExecTime    () ) + " ms" ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getMapInfoCalls       () )         ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getAvgMapInfoExecTime () ) + " ms" ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getParseRepCalls      () )         ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getAvgParseRepExecTime() ) + " ms" ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getProfInfoCalls      () )         ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( info.getAvgProfInfoExecTime() ) + " ms" ) );
			table.setWidget( row, column++, new Label( ( info.getPaidOps() == 0 ? 0 : 100 * info.getUsedOps() / info.getPaidOps() ) + "%" ) );
			
			for ( int i = column - 1; i >= firstNumberColumn; i-- )
				cellFormatter.setHorizontalAlignment( row, i, HasHorizontalAlignment.ALIGN_RIGHT );
			
			rowFormatter.addStyleName( row, userCounter == 1 ? "gold" : ( userCounter & 0x01 ) == 0 ? "row0" : "row1" );
		}
	}
	
}
