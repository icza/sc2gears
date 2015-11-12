/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.ui;

import java.awt.Container;

import hu.belicza.andras.sc2gearspluginapi.api.GuiUtilsApi;

import javax.swing.JComponent;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;

/**
 * An interface representing a table box which contains a table filter component, the table header and the table body in a scroll pane.
 * 
 * <p>The table box also adds tool tips to the table header which will be the name of the column.</p>
 * 
 * <p>Example usage:<br>
 * <blockquote><pre>
 * JDialog dialog = new JDialog();
 * 
 * JTable table = new JTable();
 * // Build the table...
 * 
 * ITableBox tableBox = generalServices.getGeneralUtilsApi().createTableBox( table, dialog.getLayeredPane() );
 * 
 * dialog.getContentPane().add( tableBox.getComponent() );
 * </pre></blockquote></p>
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see GuiUtilsApi#createTableBox(javax.swing.JTable, JComponent)
 */
public interface ITableBox {
	
	/** Interface version. */
	String VERSION = "2.4";
	
	/**
	 * Clears the table filters.
	 */
	void clearFilters();
	
	/**
	 * Returns the component of the table box.
	 * @return the component of the table box
	 */
	JComponent getComponent(); 
	
	/**
	 * Returns the wrapper which contains the filter components.
	 * @return the wrapper which contains the filter components
	 */
	Container getFilterComponentsWrapper();
	
	/**
	 * Sets an optional, additional row filter.
	 * 
	 * <p>The specified row filter will be connected with logical AND to the internal filter.
	 * To clear a previously set additional row filter, specify <code>null</code>.</p>
	 * 
	 * @param rowFilter optional, additional row filter
	 * 
	 * @since "2.4"
	 * @see #fireAdditionalRowFilterChanged()
	 */
	void setAdditionalRowFilter( RowFilter< ? super TableModel, ? super Integer > rowFilter );
	
	/**
	 * This method lets the table box know that the additional row filter might have changed,
	 * and re-filtering the table is required.
	 * 
	 * <p>For example if the additional row filter filters the rows based on a condition
	 * which changed, this method has to be called so that the table can be filtered again
	 * to only show rows that the changed condition includes.</p>
	 * 
	 * @since "2.4"
	 * @see #setAdditionalRowFilter(RowFilter)
	 */
	void fireAdditionalRowFilterChanged();
	
}
