/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.dialogs;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplaySearch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Dialog to view/edit the column settings of the Replay list table (Results tab of the Replay search).
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class ReplayListColumnSetupDialog extends BaseDialog {
	
	/**
	 * Components of a column.
	 * @author Andras Belicza
	 */
	private static class ColumnDef implements Comparable< ColumnDef > {
		public final int       modelIndex;
		public final JLabel    positionLabel;
		public final JLabel    nameLabel;
		public final JButton   moveUpButton    = new JButton( Icons.ARROW_090 );
		public final JButton   moveDownButton  = new JButton( Icons.ARROW_270 );
		public final JCheckBox visibleCheckBox = new JCheckBox( Language.getText( "replayListColumnSetup.visible" ) );
		
		public final Box wrapperBox = Box.createHorizontalBox();
		
		public ColumnDef( final int modelIndex ) {
			this.modelIndex = modelIndex;
			positionLabel = new JLabel( "", JLabel.RIGHT );
			positionLabel.setPreferredSize( new Dimension( 40, 0 ) );
			nameLabel     = new JLabel( ReplaySearch.RESULT_HEADER_NAMES[ modelIndex ], JLabel.LEFT );
			GuiUtils.changeFontToBold( nameLabel );
			
			wrapperBox.add( positionLabel );
			wrapperBox.add( Box.createHorizontalStrut( 10 ) );
			wrapperBox.add( Box.createHorizontalGlue() );
			wrapperBox.add( nameLabel );
			wrapperBox.add( Box.createHorizontalStrut( 20 ) );
			wrapperBox.add( moveUpButton );
			wrapperBox.add( moveDownButton );
			wrapperBox.add( Box.createHorizontalStrut( 20 ) );
			wrapperBox.add( visibleCheckBox );
		}

		/**
		 * Defines a model index order.
		 */
		@Override
		public int compareTo( final ColumnDef o ) {
			return modelIndex - o.modelIndex;
		}
	}
	
	/**
	 * Creates a new ReplayListColumnSetupDialog.
	 * @param onChangeTask task to be executed if the column setup is changed
	 */
	public ReplayListColumnSetupDialog( final Runnable onChangeTask ) {
		super( "replayListColumnSetup.title", Icons.EDIT_COLUMN );
		
		final ColumnDef[] columnDefs = new ColumnDef[ ReplaySearch.RESULT_HEADER_NAMES.length ];
		{
			// Initial column def order to reflect the visible column list order
			final List< Integer > allColumnIndexList = new ArrayList< Integer >( ReplaySearch.RESULT_HEADER_NAMES.length );
			
			final ColumnDef[] tempColumnDefs = new ColumnDef[ ReplaySearch.RESULT_HEADER_NAMES.length ];
			for ( int i = 0; i < tempColumnDefs.length; i++ ) {
				tempColumnDefs[ i ] = new ColumnDef( i );
				allColumnIndexList.add( i );
			}
			
			final int[] visibleReplayListColumnIndices = Settings.getVisibleReplayListColumnIndices().clone();
			
			// First the visible columns
			int displayIndex;
			for ( displayIndex = 0; displayIndex < visibleReplayListColumnIndices.length; displayIndex++ ) {
				allColumnIndexList.remove( new Integer( visibleReplayListColumnIndices[ displayIndex ] ) );
				columnDefs[ displayIndex ] = tempColumnDefs[ visibleReplayListColumnIndices[ displayIndex ] ];
				columnDefs[ displayIndex ].visibleCheckBox.setSelected( true );
			}
			
			// The rest in their default order
			for ( int i = 0; displayIndex < tempColumnDefs.length; displayIndex++, i++ )
				columnDefs[ displayIndex ] = tempColumnDefs[ allColumnIndexList.get( i ) ];
		}
		
		final Box columnsBox = Box.createVerticalBox();
		columnsBox.setBorder( BorderFactory.createEmptyBorder( 15, 0, 15, 25 ) );
		
		final Runnable columnsBoxRebuilderTask = new Runnable() {
			@Override
			public void run() {
				columnsBox.removeAll();
				
				int visibleColumns = 0;
				for ( final ColumnDef columnDef : columnDefs )
					if ( columnDef.visibleCheckBox.isSelected() )
						visibleColumns++;
				
				for ( int i = 0; i < columnDefs.length; i++ ) {
					final ColumnDef columnDef = columnDefs[ i ];
					final boolean   isVisible = columnDef.visibleCheckBox.isSelected();
					
					columnDef.positionLabel .setText( isVisible ? (i+1) + "." : "-  " );
					columnDef.moveUpButton  .setEnabled( isVisible && i > 0 );
					columnDef.moveDownButton.setEnabled( isVisible && i < visibleColumns - 1 );
					
					columnsBox.add( columnDef.wrapperBox );
				}
				
				( (JComponent) getContentPane() ).updateUI();
			}
		};
		
		// Register listeners
		for ( int i = 0; i < columnDefs.length; i++ ) {
			final ColumnDef columnDef = columnDefs[ i ];
			columnDef.moveUpButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					// Move column up
					// Find column index (it changes when columns are moved)
					for ( int i = 0; i < columnDefs.length; i++ )
						if ( columnDefs[ i ] == columnDef ) {
							swap( columnDefs, i, i - 1 );
							break;
						}
					
					columnsBoxRebuilderTask.run();
				}
			} );
			columnDef.moveDownButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					// Move column down
					// Find column index (it changes when columns are moved)
					for ( int i = 0; i < columnDefs.length; i++ )
						if ( columnDefs[ i ] == columnDef ) {
							swap( columnDefs, i, i + 1 );
							break;
						}
					
					columnsBoxRebuilderTask.run();
				}
			} );
			columnDef.visibleCheckBox.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					// Visible columns are above non-visible columns
					// Find column index (it changes when columns are moved)
					for ( int i = 0; i < columnDefs.length; i++ )
						if ( columnDefs[ i ] == columnDef ) {
							if ( columnDef.visibleCheckBox.isSelected() ) {
								// Move up until it's above all non-visible columns
								while ( i > 0 && !columnDefs[ i - 1 ].visibleCheckBox.isSelected() ) {
									swap( columnDefs, i, i - 1 );
									i--;
								}
							}
							else {
								// Move down until it's below all visible columns
								while ( i < columnDefs.length - 1 && columnDefs[ i + 1 ].visibleCheckBox.isSelected() ) {
									swap( columnDefs, i, i + 1 );
									i++;
								}
							}
							
							break;
						}
					
					columnsBoxRebuilderTask.run();
				}
			} );
		}
		
		columnsBoxRebuilderTask.run();
		// Only have to align once:
		GuiUtils.alignBox( columnsBox, 4 );
		getContentPane().add( columnsBox, BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 0, 20, 10, 20 ) );
		final JButton okButton = new JButton();
		GuiUtils.updateButtonText( okButton, "button.ok" );
		okButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				// Do not accept column setup if no visible columns
				int visibleColumns = 0;
				for ( visibleColumns = 0; visibleColumns < columnDefs.length; visibleColumns++ )
					if ( !columnDefs[ visibleColumns ].visibleCheckBox.isSelected() )
						break;
				
				if ( visibleColumns > 0 ) {
					final int[] visibleReplayListColumnIndices = new int[ visibleColumns ];
					for ( int i = 0; i < visibleColumns; i++ )
						visibleReplayListColumnIndices[ i ] = columnDefs[ i ].modelIndex;
					
					Settings.setVisibleReplayListColumnIndices( visibleReplayListColumnIndices );
				}
				
				dispose();
				
				if ( visibleColumns > 0 )
					onChangeTask.run();
			}
		} );
		buttonsPanel.add( okButton );
		final JButton cancelButton = createCloseButton( "button.cancel" );
		buttonsPanel.add( cancelButton );
		final JButton restoreDefaultsColumnSetupButton = new JButton();
		GuiUtils.updateButtonText( restoreDefaultsColumnSetupButton, "button.restoreDefaults" );
		restoreDefaultsColumnSetupButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				Arrays.sort( columnDefs );
				for ( final ColumnDef columnDef : columnDefs )
					columnDef.visibleCheckBox.setSelected( true );
				columnsBoxRebuilderTask.run();
			}
		} );
		buttonsPanel.add( restoreDefaultsColumnSetupButton );
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		packAndShow( cancelButton, false );
	}
	
	/**
	 * Swaps 2 elements in the column defs array.
	 * @param columnDefs reference to the column defs array
	 * @param i          first index to swap
	 * @param j          second index to swap
	 */
	private static void swap( final ColumnDef[] columnDefs, final int i, final int j ) {
		final ColumnDef stored = columnDefs[ i ];
		
		columnDefs[ i ] = columnDefs[ j ];
		columnDefs[ j ] = stored;
	}
	
}
