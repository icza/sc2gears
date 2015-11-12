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
import hu.belicza.andras.sc2gearsdb.admin.client.beans.FileStatInfo;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.Filter;

import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;

/**
 * A page which has a file stat info table.
 * 
 * @author Andras Belicza
 */
abstract class HasFileStatInfoTablePage extends AdminPage {
	
	/**
	 * Creates a new HasFileStatInfoTablePage.
	 * @param title    displayable title of the screen; can be <code>null</code>
	 * @param pageName page name to be logged
	 */
	public HasFileStatInfoTablePage( final String title, final String pageName ) {
		super( title, pageName );
	}
	
	/** Helper attributes to render the table. */
	private int row, userCounter;
	
	/**
	 * Builds the file stat info table.
	 * 
	 * <p>On first call the table is initialized, on later calls its content is first removed.</p>
	 * 
	 * @param fileStatInfoList          file stat info list to build the table from
	 * @param table                     table to build
	 * @param googleAccountClickHandler optional, if provided, google accounts will be rendered as links (@link {@link Anchor}),
	 * 		and clicking on them will call this handler; the tool tip of links will be <code>googleAccountClickHandler.toString()</code>
	 * @param filter                    optional, if provided, only file stat info accepted by this filter will be displayed
	 */
	protected void buildFileStatInfoTable( final List< FileStatInfo > fileStatInfoList, final FlexTable table, final ClickHandler googleAccountClickHandler, final Filter< FileStatInfo > filter ) {
		if ( table.getRowCount() == 0 ) {
			// Init table
			table.setBorderWidth( 1 );
			table.setCellSpacing( 0 );
			table.setCellPadding( 3 );
			add( table );
		}
		else
			table.removeAllRows();
		
		int column = 0;
		table.setWidget( 0, column++, new Label( "#" ) );
		table.setWidget( 0, column++, new Label( "User" ) );
		table.setWidget( 0, column++, new Label( "Pkg" ) );
		table.setWidget( 0, column++, new Label( "FType" ) );
		table.setWidget( 0, column++, new Label( "Files" ) );
		table.setWidget( 0, column++, new Label( "Storage used" ) );
		table.setWidget( 0, column++, new Label( "Avg. file size" ) );
		table.setWidget( 0, column++, new Label( "Share" ) );
		table.getRowFormatter().addStyleName( 0, "headerRow" );
		
		row         = 1;
		userCounter = 0;
		
		// Calculate total. Calculate with local variables to make it faster (it's noticeable faster).
		long paidStorage = 0, allStorage = 0, repStorage = 0, smpdStorage = 0, otherStorage = 0;
		int                   allCount   = 0, repCount   = 0, smpdCount   = 0, otherCount   = 0;
		Date updated = new Date( 0 );
		for ( final FileStatInfo info : fileStatInfoList ) {
			if ( filter != null && !filter.accept( info ) )
				continue;
			
			paidStorage  += info.getPaidStorage ();
			allCount     += info.getAllCount    ();
			allStorage   += info.getAllStorage  ();
			repCount     += info.getRepCount    ();
			repStorage   += info.getRepStorage  ();
			smpdCount    += info.getSmpdCount   ();
			smpdStorage  += info.getSmpdStorage ();
			otherCount   += info.getOtherCount  ();
			otherStorage += info.getOtherStorage();
			if ( updated.before( info.getUpdated() ) )
				updated = info.getUpdated();
		}
		final FileStatInfo totalInfo = new FileStatInfo();
		totalInfo.setGoogleAccount( "TOTAL: ∑ ALL" );
		totalInfo.setDbPackageName( "N/A"          );
		totalInfo.setPaidStorage  ( paidStorage    );
		totalInfo.setAllCount     ( allCount       );
		totalInfo.setAllStorage   ( allStorage     );
		totalInfo.setRepCount     ( repCount       );
		totalInfo.setRepStorage   ( repStorage     );
		totalInfo.setSmpdCount    ( smpdCount      );
		totalInfo.setSmpdStorage  ( smpdStorage    );
		totalInfo.setOtherCount   ( otherCount     );
		totalInfo.setOtherStorage ( otherStorage   );
		totalInfo.setUpdated      ( updated        );
		
		renderInfoRow( totalInfo, table, googleAccountClickHandler );
		
		for ( final FileStatInfo info : fileStatInfoList )
			if ( filter == null || filter.accept( info ) )
				renderInfoRow( info, table, googleAccountClickHandler );
	}
	
	private void renderInfoRow( final FileStatInfo info, final FlexTable table, final ClickHandler googleAccountClickHandler ) {
		final CellFormatter cellFormatter = table.getCellFormatter();
		final RowFormatter  rowFormatter  = table.getRowFormatter();
		
		int column = 0;
		
		table.setWidget( row, column++, new Label( userCounter + "." ) );
		cellFormatter.setHorizontalAlignment( row, column-1, HasHorizontalAlignment.ALIGN_RIGHT );
		
		final VerticalPanel userPanel = new VerticalPanel();
		if ( info.getAddressedBy() != null )
			userPanel.add( ClientUtils.styledWidget( new Label( info.getAddressedBy() + ", "
				+ ( info.getCountry() == null ? "-" : info.getCountry() ) ), "explanation" ) );
		if ( googleAccountClickHandler == null || userCounter == 0 )
			userPanel.add( new Label( info.getGoogleAccount() ) );
		else {
			final Anchor googleAccountAnchor = new Anchor( info.getGoogleAccount() );
			googleAccountAnchor.setTitle( googleAccountClickHandler.toString() );
			googleAccountAnchor.addClickHandler( googleAccountClickHandler );
			userPanel.add( googleAccountAnchor );
		}
		userPanel.add( ClientUtils.styledWidget( ClientUtils.createTimestampWidget( "Updated: ", info.getUpdated() ), "explanation" ) );
		userPanel.add( ClientUtils.styledWidget( new Label(
			( info.getAccountCreated() == null ? "" : "Created: " + ClientUtils.DATE_FORMAT.format( info.getAccountCreated() ) + ", " )
			+ "Pkg: " + info.getDbPackageName() + ";" ), "explanation" ) );
		if ( info.getComment() != null ) {
			final Label commentLabel = new Label();
			if ( info.getComment().length() <= 40 )
				commentLabel.setText( info.getComment() );
			else {
				commentLabel.setText( info.getComment().substring( 0, 40 ) + "..." );
				commentLabel.setTitle( info.getComment() );
			}
			userPanel.add( ClientUtils.styledWidget( commentLabel, "explanation" ) );
		}
		table.setWidget( row, column++, userPanel );
		
		final Widget dbPackageWidget = info.getDbPackageIcon() == null ? new Label( info.getDbPackageName() ) : new Image( info.getDbPackageIcon() );
		dbPackageWidget.setTitle( "Available storage: " + NUMBER_FORMAT.format( info.getPaidStorage() ) + " bytes" );
		table.setWidget( row, column++, dbPackageWidget );
		int rowSpan = 1;
		if ( info.getRepCount  () > 0 ) rowSpan++;
		if ( info.getSmpdCount () > 0 ) rowSpan++;
		if ( info.getOtherCount() > 0 ) rowSpan++;
		if ( rowSpan > 1 )
			for ( int i = column - 1; i >= 0; i-- )
				table.getFlexCellFormatter().setRowSpan( row, i, rowSpan );
		userCounter++;
		
		for ( int type = 0; type < 4; type++ ) {
			String fileType = null;
			int    count    = 0;
			long   storage  = 0;
			switch ( type ) {
			case 0 : count = info.getAllCount(); storage = info.getAllStorage(); fileType = "<all>"; break;
			case 1 : if ( ( count = info.getRepCount  () ) == 0 ) continue; storage = info.getRepStorage  (); fileType = "rep"  ; column = 0; break;
			case 2 : if ( ( count = info.getSmpdCount () ) == 0 ) continue; storage = info.getSmpdStorage (); fileType = "smpd" ; column = 0; break;
			case 3 : if ( ( count = info.getOtherCount() ) == 0 ) continue; storage = info.getOtherStorage(); fileType = "other"; column = 0; break;
			}
			table.setWidget( row, column++, new Label( fileType ) );
			final int firstNumberColumn = column;
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( count ) ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( storage ) + " bytes" ) );
			table.setWidget( row, column++, new Label( NUMBER_FORMAT.format( count == 0 ? 0 : storage / count ) + " bytes" ) );
			final int usedPercent = info.getPaidStorage() == 0 ? 0 : (int) ( storage * 100 / info.getPaidStorage() );
			table.setWidget( row, column++, new Label( usedPercent + "%" ) );
			
			for ( int i = column - 1; i >= firstNumberColumn; i-- )
				cellFormatter.setHorizontalAlignment( row, i, HasHorizontalAlignment.ALIGN_RIGHT );
			
			rowFormatter.addStyleName( row, userCounter == 1 ? "gold" : ( userCounter & 0x01 ) == 0 ? "row0" : "row1" );
			
			row++;
		}
	}
	
}
