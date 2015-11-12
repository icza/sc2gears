/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.common.client.pagingtable;

import static hu.belicza.andras.sc2gearsdb.common.client.CommonApi.PAGE_SIZES;

import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.ImageButton;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * A paging table widget.
 * 
 * <p>This table widget renders table paging components (like Jump to first page, Previous page, Next page,
 * page size list box), and utility buttons like Download selected, Delete selected.<br>
 * Also renders a table with a check box column allowing the user to select rows,
 * and an action column providing different actions to individual rows.</p>
 * 
 * <p>The data source and the renderer of the table is a {@link PagingTableHandler} object which has to be specified in the constructor.</p>
 * 
 * @param <T> type of the entity displayed in the table
 * 
 * @author Andras Belicza
 */
public class PagingTable < T > extends Composite {
	
	/** Table configuration. */
	private final PagingTableConfig< T > tableConfig;
	
	/** Panel containing the widgets of the paging table. */
	private final VerticalPanel contentPanel = new VerticalPanel();
	
	/** Index of the default page size. */
	private static final int DEFAULT_PAGE_SIZE_INDEX = 0;
	
	/** Offset of the first displayed row. Initialized with -1 to indicate that no rows have been displayed. */
	private int offset    = -1;
	/** When paging, the new offset is stored in this variable. If the table rendered properly,
	 * this new offset will be assigned to offset. */
	private int newOffset = 0;
	/** Max number of rows to display in the table. */
	private int limit     = PAGE_SIZES[ DEFAULT_PAGE_SIZE_INDEX ];
	
	/** Button to jump to the first page.             */
	private final Button  firstButton     = new ImageButton( "arrow-stop-180.png", "First page" );
	/** Button to jump to the previous page.          */
	private final Button  prevButton      = new ImageButton( "arrow-180.png", "Previous page" );
	/** Label to indicate the current page.           */
	private final Label   pageLabel       = new Label( "Page:" );
	/** List box to display and change the page size. */
	private final ListBox pageSizeListBox = new ListBox();
	/** Button to jump to the next page.              */
	private final Button  nextButton      = new ImageButton( "arrow.png", "Next page" );
	
	/** The table widget. */
	private final FlexTable table = new FlexTable();
	
	/** Callback to be used to refresh the table data (the current page). */
	private AsyncCallbackAdapter< EntityListResult< T > > tableRefresherCallback;
	
	/** Entity list currently displayed in the table. */
	private List< T > entityList;
	
	/**
	 * Cursors are cached in this map.
	 * Key is the name space (might be a unique string generated from custom filters),
	 * value is another map which stores cursors mapped from the offset (which specifies the page).
	 */
	private final Map< String, Map< Integer, String > > cursorNamespaceOffsetCursorMapMap = new HashMap< String, Map< Integer,String > >();
	
	/** The current cursor namespace. */
	private String cursorNamespace;
	
	/**
	 * Creates a new PagingTable.
	 * @param tableconfig table configuration
	 */
	public PagingTable( final PagingTableConfig< T > tableConfig ) {
		this.tableConfig = tableConfig;
		
		table.setBorderWidth( 1 );
		table.setCellSpacing( 0 );
		table.setCellPadding( 3 );
		
		contentPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		
		initWidget( contentPanel );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the paging table.
	 * @param columnLabels labels of the columns of the table
	 */
	private void buildGUI() {
		tableRefresherCallback = new AsyncCallbackAdapter< EntityListResult< T > >( tableConfig.getInfoPanel(), false ) {
			@Override
			public void customOnSuccess( final RpcResult< EntityListResult< T > > rpcResult ) {
				final EntityListResult< T > entityListResult = rpcResult.getValue();
				if ( entityListResult == null )
					return;
				final List< T > entityList = entityListResult.getEntityList();
				if ( entityList.size() == 0 ) {
					tableConfig.getInfoPanel().setInfoMessage( "No more " + tableConfig.getEntityNamePlural() + "." );
					return;
				}
				
				if ( offset < 0 ) // First call
					contentPanel.add( table );
				
				offset = newOffset;
				pageLabel.setText( "Page " + ( offset / limit + 1 ) );
				
				table.removeAllRows();
				
				// Table header
				final boolean rowSelectionAllowed = tableConfig.isAllowRowSelection();
				if ( rowSelectionAllowed ) {
    				final CheckBox selectAllCheckBox = new CheckBox( "#" );
    				selectAllCheckBox.addClickHandler( new ClickHandler() {
    					@Override
    					public void onClick( final ClickEvent event ) {
    						final boolean selectall = selectAllCheckBox.getValue();
    						final int     rowCount  = table.getRowCount();
    						for ( int i = 1; i < rowCount; i++ )
    							( (CheckBox) table.getWidget( i, 0 ) ).setValue( selectall );
    					}
    				} );
    				table.setWidget( 0, 0, selectAllCheckBox );
				}
				else
    				table.setWidget( 0, 0, new Label( "#" ) );
				final RowFormatter rowFormatter = table.getRowFormatter();
				final String[] columnLabels = tableConfig.getColumnLabels();
				for ( int i = 0; i < columnLabels.length; i++ )
					table.setWidget( 0, i+1, new Label( tableConfig.getSortingColumn() == null || tableConfig.getSortingColumn().intValue() != i ? columnLabels[ i ] : columnLabels[ i ] + ( tableConfig.isAscendingSort() ? " ▲" : " ▼" ) ) );
				rowFormatter.addStyleName( 0, "headerRow" );
				
				// Table data
				int row = 0;
				final PagingTableHandler< T > tableHandler = tableConfig.getTableHandler();
				final CellFormatter cellFormatter = table.getCellFormatter();
				final HorizontalAlignmentConstant[] columnHorizontalAlignments = tableConfig.getColumnHorizontalAlignments();
				for ( final T entity : entityList ) {
					row++;
					final String rowStyleClass = tableHandler.getRowStyleClass( entity, row );
					rowFormatter.addStyleName( row, rowStyleClass == null ? "row" + ( row & 0x01 ) : rowStyleClass );
					table.setWidget( row, 0, rowSelectionAllowed ? new CheckBox( ( offset + row ) + "." ) : new Label( ( offset + row ) + "." ) );
					for ( int i = 0; i < columnLabels.length; i++ ) {
						table.setWidget( row, 1 + i, tableHandler.createCellWidget( entity, i ) );
						if ( columnHorizontalAlignments != null && columnHorizontalAlignments.length > i )
							cellFormatter.setHorizontalAlignment( row, 1 + i, columnHorizontalAlignments[ i ] );
					}
				}
				PagingTable.this.entityList = entityList;
				
				// Cache cursor
				// TODO if last page is not full...
				Map< Integer, String > offsetCursorMap = cursorNamespaceOffsetCursorMapMap.get( cursorNamespace );
				if ( offsetCursorMap == null )
					cursorNamespaceOffsetCursorMapMap.put( cursorNamespace, offsetCursorMap = new HashMap< Integer, String >() );
				offsetCursorMap.put( offset + limit, entityListResult.getCursorString() );
			}
			@Override
			public void customOnEnd() {
				setPagingButtonsEnabled( true );
			}
		};
		
		final ClickHandler pagingHandler = new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				setPagingButtonsEnabled( false );
				
				if ( event.getSource() == nextButton )
					newOffset = offset + limit;
				else if ( event.getSource() == prevButton ) {
					if ( ( newOffset = offset - limit ) < 0 )
						newOffset = 0;
				} else if ( event.getSource() == firstButton )
					newOffset = 0;
				
				reloadFromNewOffset();
			}
		};
		
		final HorizontalPanel controlPanel = new HorizontalPanel();
		controlPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		
		firstButton.setEnabled( false );
		firstButton.addClickHandler( pagingHandler );
		controlPanel.add( firstButton );
		
		controlPanel.add( ClientUtils.createHorizontalEmptyWidget( 15 ) );
		prevButton.setEnabled( false );
		prevButton.addClickHandler( pagingHandler );
		controlPanel.add( prevButton );
		
		controlPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		controlPanel.add( pageLabel );
		
		controlPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		nextButton.setEnabled( false );
		nextButton.addClickHandler( pagingHandler );
		controlPanel.add( nextButton );
		
		controlPanel.add( ClientUtils.createHorizontalEmptyWidget( 10 ) );
		controlPanel.add( new Label( "Page size:" ) );
		
		for ( final int pageSize : PAGE_SIZES )
			pageSizeListBox.addItem( Integer.toString( pageSize ) );
		pageSizeListBox.setSelectedIndex( DEFAULT_PAGE_SIZE_INDEX );
		pageSizeListBox.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange( final ChangeEvent event ) {
				limit = PAGE_SIZES[ pageSizeListBox.getSelectedIndex() ];
				reloadFirstPage();
			}
		} );
		
		controlPanel.add( pageSizeListBox );
		
		if ( tableConfig.isAllowRowSelection() ) {
			final Label spaceConsumer = new Label();
			spaceConsumer.setWidth( "40" );
			controlPanel.add( spaceConsumer );
			controlPanel.setCellWidth( spaceConsumer, "40" );
			
    		final AsyncCallbackAdapter< Integer > deleteResultHandlerCallback = new AsyncCallbackAdapter< Integer >( tableConfig.getInfoPanel(), false ) {
    			@Override
    			public void customOnSuccess( final RpcResult< Integer > rpcResult ) {
    				final Integer deletedCount = rpcResult.getValue();
    				if ( deletedCount == null )
    					return;
    				if ( deletedCount == 0 ) {
    					tableConfig.getInfoPanel().setErrorMessage( "Failed to delete any " + tableConfig.getEntityNamePlural() + "!" );
    					return;
    				}
    				
    				tableConfig.getInfoPanel().setInfoMessage( "Successfully deleted " + deletedCount + ( deletedCount.intValue() == 1 ? tableConfig.getEntityName() : tableConfig.getEntityNamePlural() ) + "." );
    				// Clear table here because if no more results (or no more on the current page), table would not be cleared
    				table.removeAllRows();
    				// Refresh table
    				tableRefresherCallback.setLoading( true );
    				tableConfig.getTableHandler().getEntityListResult( new PageInfo( newOffset, limit, null ), tableRefresherCallback );
    			}
    			@Override
    			public void customOnEnd() {
    				setPagingButtonsEnabled( true );
    			}
    		};
    		final Button downloadSelectedButton = new Button( "Download selected" );
    		final Button deleteSelectedButton   = new Button( "Delete selected" );
    		final ClickHandler batchClickHandler = new ClickHandler() {
    			@Override
    			public void onClick( final ClickEvent event ) {
    				final List< T > selectedEntityList = new ArrayList< T >();
    				for ( int i = table.getRowCount() - 1; i > 0; i-- )
    					if ( ( (CheckBox) table.getWidget( i, 0 ) ).getValue() )
    						selectedEntityList.add( entityList.get( i-1 ) );
    				
    				final int selectedCount = selectedEntityList.size();
    				if ( selectedCount == 0 ) {
    					Window.alert( "You have to select " + tableConfig.getEntityNamePlural() + " first!" );
    					return;
    				}
    				
    				if ( event.getSource() == downloadSelectedButton ) {
    					tableConfig.getTableHandler().downloadEntityList( selectedEntityList );
    					return;
    				} else if ( Window.confirm( "Are you sure you want to delete " + selectedCount + " " + ( selectedCount == 1 ? tableConfig.getEntityName() : tableConfig.getEntityNamePlural() ) + "?" ) ) {
    					setPagingButtonsEnabled( false );
    					tableRefresherCallback.setLoading( true );
    					tableConfig.getTableHandler().deleteEntityList( selectedEntityList, deleteResultHandlerCallback );
    					return;
    				}
    			}
    		};
    		downloadSelectedButton.addClickHandler( batchClickHandler );
    		downloadSelectedButton.setTitle( "Download selected " + tableConfig.getEntityNamePlural() );
    		downloadSelectedButton.addStyleName( "dlButton"  );
   			if ( tableConfig.isDeleteEnabled() ) {
   				deleteSelectedButton.addClickHandler( batchClickHandler );
   				deleteSelectedButton.setTitle( "Delete selected " + tableConfig.getEntityNamePlural() );
   			}
   			else
   				deleteSelectedButton.setEnabled( false );
    		deleteSelectedButton.addStyleName( "delButton" );
    		controlPanel.add( downloadSelectedButton );
    		controlPanel.add( ClientUtils.createHorizontalEmptyWidget( 3 ) );
    		controlPanel.add( deleteSelectedButton );
		}
		
		contentPanel.add( controlPanel );
		
		// Get the first page of entities
		reloadFirstPage();
	}
	
	/**
	 * Sets the enabled status of the paging buttons.
	 * @param enabled the enabled status to be set
	 */
	private void setPagingButtonsEnabled( final boolean enabled ) {
		if ( enabled ) {
			firstButton    .setEnabled( offset > 0 );
			prevButton     .setEnabled( offset > 0 );
			nextButton     .setEnabled( true );
			pageSizeListBox.setEnabled( true );
		}
		else {
			firstButton    .setEnabled( false );
			prevButton     .setEnabled( false );
			nextButton     .setEnabled( false );
			pageSizeListBox.setEnabled( false );
		}
	}
	
	/**
	 * Jumps to the first page.
	 */
	public void reloadFirstPage() {
		newOffset = 0;
		reloadFromNewOffset();
	}
	
	/**
	 * Reloads the current page.
	 */
	public void reloadCurrentPage() {
		reloadFromNewOffset();
	}
	
	/**
	 * Reloads the table from the {@link #newOffset}.
	 */
	private void reloadFromNewOffset() {
		tableRefresherCallback.setLoading( true );
		
		cursorNamespace = tableConfig.getTableHandler().getCursorNamespace();
		final Map< Integer, String > offsetCursorMap = cursorNamespaceOffsetCursorMapMap.get( cursorNamespace );
		
		tableConfig.getTableHandler().getEntityListResult( new PageInfo( newOffset, limit, offsetCursorMap == null ? null : offsetCursorMap.get( newOffset ) ), tableRefresherCallback );
	}
	
	/**
	 * Returns the table configuration.
	 * @return the table configuration
	 */
	public PagingTableConfig< T > getTableConfig() {
	    return tableConfig;
    }
	
}
