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

import hu.belicza.andras.sc2gearsdb.common.client.InfoPanel;

import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * Paging table configuration.
 * 
 * @param <T> type of the entity displayed in the table
 * 
 * @author Andras Belicza
 */
public class PagingTableConfig < T > {
	
	/** Reference to the info panel to display activity, error and info messages. */
	private InfoPanel               infoPanel;
	
	/** Name of the entities displayed in the table.        */
	private String                  entityName;
	/** Plural name of the entities displayed in the table. */
	private String                  entityNamePlural;
	
	/** Labels of the columns of the table. */
	private String[]                columnLabels;
	
	/** Horizontal alignments of the columns of the table.  */
	private HorizontalAlignmentConstant[] columnHorizontalAlignments;
	
	/** Data source, renderer and action implementation for selected rows. */
	private PagingTableHandler< T > tableHandler;
	
	/**
	 * Tells which is the sorting column of the table data (null=not sorted).
	 * Default value: 1
	 */
	private Integer                 sortingColumn     = 1;
	/**
	 * Tells if sorting is ascending (false=descending).
	 * Default value: false
	 */
	private boolean                 ascendingSort     = false;
	
	/** Tells if row selection should be allowed.
	 * <p>If row selection is allowed, a check box will be displayed in the first column of each row,
	 * and "Download selected" and "Delete selected" buttons will be displayed above the table.</p>
	 * Default value: true
	 */
	private boolean                 allowRowSelection = true;
	
	/**
	 * Tells if delete button should be enabled.
	 * Default value: true
	 */
	private boolean                 deleteEnabled    = true;
	
    /**
     * Creates a new PagingTableConfig with the default settings.
     */
    public PagingTableConfig() {
    }

	public void setInfoPanel( InfoPanel infoPanel ) {
	    this.infoPanel = infoPanel;
    }

	public InfoPanel getInfoPanel() {
	    return infoPanel;
    }

	public void setEntityName( String entityName ) {
	    this.entityName = entityName;
    }

	public String getEntityName() {
	    return entityName;
    }

	public void setEntityNamePlural( String entityNamePlural ) {
	    this.entityNamePlural = entityNamePlural;
    }

	public String getEntityNamePlural() {
	    return entityNamePlural;
    }

	public void setColumnLabels( String... columnLabels ) {
	    this.columnLabels = columnLabels;
    }

	public String[] getColumnLabels() {
	    return columnLabels;
    }

	public void setColumnHorizontalAlignments( HorizontalAlignmentConstant... columnHorizontalAlignments ) {
	    this.columnHorizontalAlignments = columnHorizontalAlignments;
    }

	public HorizontalAlignmentConstant[] getColumnHorizontalAlignments() {
	    return columnHorizontalAlignments;
    }
	
	public void setTableHandler( PagingTableHandler< T > tableHandler ) {
	    this.tableHandler = tableHandler;
    }

	public PagingTableHandler< T > getTableHandler() {
	    return tableHandler;
    }

	public void setSortingColumn( Integer sortingColumn ) {
	    this.sortingColumn = sortingColumn;
    }

	public Integer getSortingColumn() {
	    return sortingColumn;
    }

	public void setAscendingSort( boolean ascendingSort ) {
	    this.ascendingSort = ascendingSort;
    }

	public boolean isAscendingSort() {
	    return ascendingSort;
    }

	public void setAllowRowSelection( boolean allowRowSelection ) {
	    this.allowRowSelection = allowRowSelection;
    }

	public boolean isAllowRowSelection() {
	    return allowRowSelection;
    }

	public void setDeleteEnabled( boolean deleteEnabled ) {
	    this.deleteEnabled = deleteEnabled;
    }

	public boolean isDeleteEnabled() {
	    return deleteEnabled;
    }

}
