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

import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * Callback tasks for the paging table.
 * 
 * <p>The callback tasks provide functionality to produce data for the table, to render the cell widgets
 * and to implement actions for selected rows (like download, delete).</p>
 * 
 * @param <T> type of the entity displayed in the table
 * 
 * @author Andras Belicza
 */
public interface PagingTableHandler< T > {
	
	/**
	 * Returns the current cursor namespace.
	 * 
	 * <p>This method is called right before {@link #getEntityListResult(PageInfo, AsyncCallback)}.
	 * The namespace is used to create different cursor caches.
	 * The namespace is usually a unique string representation of custom table filters:
	 * if custom filters change, cursors are no longer valid for the new changed filters.</p>
	 * 
	 * <p>If the implementation does not need this,
	 * consistently the same string (any string - preferable an empty string) should be returned.</p>
	 * 
	 * @return the current namespace
	 */
	String getCursorNamespace();
	
	/**
	 * The data source for the table.<br>
	 * Returns the entity list result for a page.
	 * @param pageInfo page info to return
	 * @param callback callback to be called with the list result of entities
	 */
	void getEntityListResult( PageInfo pageInfo, AsyncCallback< RpcResult< EntityListResult< T > > > callback );
	
	/**
	 * Returns the row style class name.<br>
	 * The implementation may return <code>null</code> in which case default row styles will be set.
	 * @param entity entity of the row
	 * @param row    row number
	 * @return the style class name for the row
	 */
	String getRowStyleClass( T entity, int row );
	
	/**
	 * Creates a widget for the specified column for the specified entity.
	 * @param entity entity whose property widget to return
	 * @param column column to return the widget for
	 * @return the widget for the specified column for the specified entity
	 */
	Widget createCellWidget( T entity, int column );
	
	/**
	 * Deletes the specified entity list.
	 * @param entityList list of entities to delete
	 * @param callback   callback to be called with the result of deletion
	 */
	void deleteEntityList( List< T > entityList, AsyncCallback< RpcResult< Integer > > callback );
	
	/**
	 * Downloads the specified entity list.
	 * @param entityList list of entities to download
	 */
	void downloadEntityList( List< T > entityList );
	
}
