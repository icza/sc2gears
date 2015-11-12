/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.components;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.dialogs.WordCloudDialog;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.TextFilter;
import hu.belicza.andras.sc2gearspluginapi.api.ui.ITableBox;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;
import hu.belicza.andras.sc2gearspluginapi.impl.util.WordCloudTableInput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultRowSorter;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * A table box which contains a table filter component, the table header and the table body in a scroll pane.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class TableBox extends Box implements ITableBox {
	
	/** Reference to the filter components. */
	private final Container filterComponentsWrapper;
	/** Button to clear the filters.        */
	private final JButton   clearFiltersButton = new JButton();
	
	/** Task that updates the row filters.  */
	private Runnable        updateRowFilterTask;
	
	/** An optional, additional row filter.
	 * Will be connected with logical AND to the internal filter. */
	private RowFilter< ? super TableModel, ? super Integer > additionalRowFilter;
	
	/**
	 * Creates a new TableBox.
	 * @param table               table to be wrapped
	 * @param rootComponent       root component to register filter hotkeys at
	 * @param wordCloudTableInput optional parameter, if provided, a Word Cloud link will be added
	 * 		which will open a Word Cloud dialog taking its input from the table as specified by this object
	 */
	public TableBox( final JTable table, final JComponent rootComponent, final WordCloudTableInput wordCloudTableInput ) {
		super( BoxLayout.Y_AXIS );
		
		add( filterComponentsWrapper = createTableFilterComponent( table, rootComponent, wordCloudTableInput ) );
		
		add( table.getTableHeader() );
		add( new JScrollPane( table ) );
		
		table.getTableHeader().addMouseMotionListener( new MouseAdapter() {
			@Override
			public void mouseMoved( final MouseEvent event ) {
				final int column = table.getTableHeader().getColumnModel().getColumnIndexAtX( event.getX() );
				table.getTableHeader().setToolTipText( table.getColumnName( column ) );
			}
		} );
	}
	
	/**
	 * Creates a table filter component for the specified table.
	 * 
	 * @param table               table to create a filter component for
	 * @param rootComponent       root component to register filter hotkeys at
	 * @param wordCloudTableInput optional parameter, if provided, a Word Cloud link will be added
	 * 		which will open a Word Cloud dialog taking its input from the table as specified by this object
	 * 
	 * @return the created table filter component for the specified table
	 */
	private JComponent createTableFilterComponent( final JTable table, final JComponent rootComponent, final WordCloudTableInput wordCloudTableInput ) {
		final Box filterBox = Box.createHorizontalBox();
		
		filterBox.add( Box.createHorizontalStrut( 3 ) );
		final JLabel filterRowsLabel = new JLabel( Language.getText( "general.tableFilter.filterRows" ) );
		filterRowsLabel.setOpaque( true );
		filterBox.add( filterRowsLabel );
		filterBox.add( Box.createHorizontalStrut( 3 ) );
		final JTextField filterTextField = new JTextField( 15 );
		filterTextField.setToolTipText( Language.getText( "general.tableFilter.filterRowsToolTip" ) );
		filterBox.add( filterTextField );
		
		filterBox.add( Box.createHorizontalStrut( 3 ) );
		final JLabel filterOutRowsLabel = new JLabel( Language.getText( "general.tableFilter.filterOutRows" ) );
		filterOutRowsLabel.setOpaque( true );
		filterBox.add( filterOutRowsLabel );
		filterBox.add( Box.createHorizontalStrut( 3 ) );
		final JTextField filterOutTextField = new JTextField( 15 );
		filterOutTextField.setToolTipText( Language.getText( "general.tableFilter.filterOutRowsToolTip" ) );
		filterBox.add( filterOutTextField );
		filterBox.add( Box.createHorizontalStrut( 3 ) );
		
		GuiUtils.updateButtonText( clearFiltersButton, "general.tableFilter.clearFiltersButton" );
		clearFiltersButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				filterTextField   .setText( null );
				filterOutTextField.setText( null );
			}
		} );
		filterBox.add( clearFiltersButton );
		filterBox.add( Box.createHorizontalStrut( 3 ) );
		filterBox.add( new JLabel( Language.getText( "general.tableFilter.rows" ) ) );
		filterBox.add( Box.createHorizontalStrut( 3 ) );
		final JLabel rowsCountLabel = new JLabel();
		filterBox.add( rowsCountLabel );
		filterBox.add( Box.createHorizontalStrut( 3 ) );
		filterBox.add( new JPanel( new BorderLayout() ) );
		filterBox.add( new JLabel( "<html></html>" ) );
		filterBox.add( new JLabel( "<html></html>" ) );
		if ( wordCloudTableInput != null ) {
			final JLabel wordCloudLabel = GeneralUtils.createLinkStyledLabel( Language.getText( "general.tableFilter.wordCloud" ) );
			wordCloudLabel.setIcon( Icons.TAG_CLOUD );
			wordCloudLabel.addMouseListener( new MouseAdapter() {
				@Override
				public void mousePressed( final MouseEvent event ) {
					final Window windowAncestor = SwingUtilities.getWindowAncestor( TableBox.this );
					
					final TableModel model = table.getModel();
					final int rowCount = model.getRowCount();
					final List< Pair< String, Integer > > wordList = new ArrayList< Pair< String,Integer > >( rowCount );
					for ( int i = 0; i < rowCount; i++ )
						wordList.add( new Pair< String, Integer >( model.getValueAt( i, wordCloudTableInput.wordColumnIndex ).toString(), (Integer) model.getValueAt( i, wordCloudTableInput.frequencyColumnIndex ) ) );
					
					if ( windowAncestor instanceof Dialog )
						new WordCloudDialog( (Dialog) windowAncestor, wordCloudTableInput.title, wordList );
					else if ( windowAncestor instanceof Frame )
						new WordCloudDialog( (Frame) windowAncestor, wordCloudTableInput.title, wordList );
				}
			} );
			filterBox.add( wordCloudLabel );
		}
		filterBox.add( Box.createHorizontalStrut( 3 ) );
		final JLabel selectDeselectAllRowsLabel = GuiUtils.createIconLabelButton( Icons.TABLE_SELECT_ALL, "general.tableFilter.selectDeselectAllRowsToolTip" );
		selectDeselectAllRowsLabel.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked( final MouseEvent event ) {
				if ( table.getSelectedRow() == -1 ) 
					table.selectAll();
				else
					table.clearSelection();
			}
		} );
		filterBox.add( selectDeselectAllRowsLabel );
		filterBox.setMaximumSize( new Dimension( filterBox.getMaximumSize().width, 1 ) );
		
		final Runnable updateRowsCount = new Runnable() {
			@Override
			public void run() {
				rowsCountLabel.setText( table.getRowCount() + " / " + table.getModel().getRowCount() );
			}
		};
		@SuppressWarnings("unchecked")
		final DefaultRowSorter< TableModel, Integer > rowSorter  = (DefaultRowSorter< TableModel, Integer >) table.getRowSorter();
		rowSorter.addRowSorterListener( new RowSorterListener() {
			@Override
			public void sorterChanged( final RowSorterEvent event ) {
				updateRowsCount.run();
			}
		} );
		table.getModel().addTableModelListener( new TableModelListener() {
			@Override
			public void tableChanged( final TableModelEvent event ) {
				updateRowsCount.run();
			}
		} );
		updateRowsCount.run();
		
		updateRowFilterTask = new Runnable() {
			@Override
			public void run() {
				@SuppressWarnings("unchecked")
				final Vector< Vector< Object > > dataVector = ( (DefaultTableModel) table.getModel() ).getDataVector();
				
				final TextFilter textFilter = new TextFilter( filterTextField.getText().toLowerCase(), filterOutTextField.getText().toLowerCase() );
				
				filterRowsLabel   .setBackground( textFilter.isIncludeFilterActive() ? Color.GREEN : null );
				filterOutRowsLabel.setBackground( textFilter.isExcludeFilterActive() ? Color.GREEN : null );
				
				if ( textFilter.isFilterSpecified() )
					rowSorter.setRowFilter( new RowFilter< TableModel, Integer >() {
						private final StringBuilder rowStringBuilder = new StringBuilder(); // To reuse it
						@Override
						public boolean include( final Entry< ? extends TableModel, ? extends Integer > entry ) {
							// The additional row filter is most likely faster than our filter, so check that first:
							if ( additionalRowFilter != null && !additionalRowFilter.include( entry ) )
								return false;
							
							rowStringBuilder.setLength( 0 );
							
							for ( final Object data : dataVector.get( entry.getIdentifier() ) )
								if ( data != null )
									rowStringBuilder.append( data.toString() ).append( ' ' );
							
							return textFilter.isIncluded( rowStringBuilder.toString().toLowerCase() );
						}
					} );
				else
					rowSorter.setRowFilter( additionalRowFilter );
			}
		};
		
		final DocumentListener filterDocumentListener = new DocumentListener() {
			@Override public void removeUpdate ( final DocumentEvent event ) { updateRowFilterTask.run(); }
			@Override public void insertUpdate ( final DocumentEvent event ) { updateRowFilterTask.run(); }
			@Override public void changedUpdate( final DocumentEvent event ) { updateRowFilterTask.run(); }
		};
		
		filterTextField   .getDocument().addDocumentListener( filterDocumentListener );
		filterOutTextField.getDocument().addDocumentListener( filterDocumentListener );
		
		// Hotkeys for filters: CTRL+F, CTRL+T
		final InputMap  inputMap  = rootComponent.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		final ActionMap actionMap = rootComponent.getActionMap();
		
		Object actionKey;
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_F, InputEvent.CTRL_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				filterTextField.requestFocusInWindow();
			}
		} );
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_T, InputEvent.CTRL_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				filterOutTextField.requestFocusInWindow();
			}
		} );
		
		return filterBox;
	}
	
	@Override
	public void clearFilters() {
		clearFiltersButton.doClick( 0 );
	}
	
	@Override
	public JComponent getComponent() {
		return this;
	}
	
	@Override
	public Container getFilterComponentsWrapper() {
		return filterComponentsWrapper;
	}
	
	@Override
	public void setAdditionalRowFilter( final RowFilter< ? super TableModel, ? super Integer > rowFilter ) {
		additionalRowFilter = rowFilter;
		updateRowFilterTask.run();
	}
	
	@Override
    public void fireAdditionalRowFilterChanged() {
		updateRowFilterTask.run();
    }
	
}
