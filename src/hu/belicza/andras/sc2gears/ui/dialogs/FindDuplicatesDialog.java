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
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.ReplaySearch;
import hu.belicza.andras.sc2gears.util.GeneralUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.table.TableModel;

/**
 * Find duplicates dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class FindDuplicatesDialog extends BaseDialog {
	
	/**
	 * Creates a new FindDuplicatesDialog.
	 * @param resultList   reference to the result list to find duplicates in
	 * @param resultsTable reference to the table displaying the results
	 */
	public FindDuplicatesDialog( final List< Object[] > resultList, final JTable resultsTable ) {
		super( "findDuplicates.title", Icons.DOCUMENTS_STACK );
		
		final Integer[][] identicalGroups = findDuplicates( resultList );
		
		final Box northBox = Box.createVerticalBox();
		northBox.setBorder( BorderFactory.createEmptyBorder( 5, 10, 5, 10 ) );
		final JLabel infoLabel = new JLabel( Language.getText( "findDuplicates.foundDuplicateReplays", identicalGroups.length ) );
		northBox.add( GuiUtils.wrapInPanel( infoLabel ) );
		getContentPane().add( northBox, BorderLayout.NORTH );
		
		final List< JCheckBox > checkBoxList = identicalGroups.length > 0 ? new ArrayList< JCheckBox >() : null;
		if ( identicalGroups.length > 0 ) {
			final JPanel controlPanel = new JPanel();
			final JButton markAllButton = new JButton();
			GuiUtils.updateButtonText( markAllButton, "findDuplicates.markAllButton" );
			controlPanel.add( markAllButton );
			final JButton markAllButFirstsButton = new JButton( "Mark all but firsts" );
			GuiUtils.updateButtonText( markAllButFirstsButton, "findDuplicates.markAllButFirstsButton" );
			controlPanel.add( markAllButFirstsButton );
			final JButton clearMarksButton = new JButton( "Clear marks" );
			GuiUtils.updateButtonText( clearMarksButton, "findDuplicates.clearMarks" );
			controlPanel.add( clearMarksButton );
			northBox.add( controlPanel );
			
			final ActionListener checkBoxHandlerListener = new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					if ( event.getSource() == markAllButFirstsButton ) {
						int counter = 0;
						for ( int i = 0; i < identicalGroups.length; i++ ) {
							final Integer[] identicalGroup = identicalGroups[ i ];
							for ( int j = 0; j < identicalGroup.length; j++ )
								checkBoxList.get( counter++ ).setSelected( j > 0 );
						}
					}
					else {
						final boolean select = event.getSource() == markAllButton;
						for ( final JCheckBox checkBox : checkBoxList )
							checkBox.setSelected( select );
					}
				}
			};
			markAllButton         .addActionListener( checkBoxHandlerListener );
			markAllButFirstsButton.addActionListener( checkBoxHandlerListener );
			clearMarksButton      .addActionListener( checkBoxHandlerListener );
			
			final Box duplicatesBox = Box.createVerticalBox();
			for ( int i = 0; i < identicalGroups.length; i++ ) {
				if ( i > 0 )
					duplicatesBox.add( Box.createVerticalStrut( 20 ) );
				final Integer[] identicalGroup = identicalGroups[ i ];
				final JLabel duplicatesOfReplayLabel = new JLabel( Language.getText( "findDuplicates.duplicatesOfReplay", identicalGroup.length, resultList.get( identicalGroup[ 0 ] )[ ReplaySearch.COLUMN_FILE_NAME ] ) );
				GuiUtils.changeFontToBold( duplicatesOfReplayLabel );
				duplicatesBox.add( duplicatesOfReplayLabel );
				for ( int j = 0; j < identicalGroup.length; j++ ) {
					final JCheckBox checkBox = new JCheckBox( (String) resultList.get( identicalGroup[ j ] )[ ReplaySearch.COLUMN_FILE_NAME ], j > 0 );
					checkBoxList.add( checkBox );
					duplicatesBox.add( checkBox );
				}
			}
			final JScrollPane duplicatesBoxWrapper = new JScrollPane( duplicatesBox );
			duplicatesBoxWrapper.setPreferredSize( new Dimension( 800, 400 ) );
			duplicatesBoxWrapper.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 10 ), duplicatesBoxWrapper.getBorder() ) );
			getContentPane().add( duplicatesBoxWrapper, BorderLayout.CENTER );
		}
		
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		final JButton selectMarkedReplaysButton = identicalGroups.length > 0 ? new JButton( Icons.TABLE_SELECT_ROW ) : null;
		if ( identicalGroups.length > 0 ) {
			GuiUtils.updateButtonText( selectMarkedReplaysButton, "findDuplicates.selectMarkedReplaysButton" );
			selectMarkedReplaysButton.setEnabled( identicalGroups.length > 0 );
			selectMarkedReplaysButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					final ListSelectionModel selectionModel = resultsTable.getSelectionModel();
					selectionModel.clearSelection();
					final RowSorter< ? extends TableModel > rowSorter = resultsTable.getRowSorter();
					Integer firstSelectedRow = null;
					int counter = 0;
					for ( int i = 0; i < identicalGroups.length; i++ ) {
						final Integer[] identicalGroup = identicalGroups[ i ];
						for ( int j = 0; j < identicalGroup.length; j++ ) 
							if ( checkBoxList.get( counter++ ).isSelected() ) {
								final int viewIndex = rowSorter.convertRowIndexToView( identicalGroup[ j ] );
								if ( firstSelectedRow == null )
									firstSelectedRow = viewIndex;
								selectionModel.addSelectionInterval( viewIndex, viewIndex );
							}
					}
					if ( firstSelectedRow != null ) // Scroll to the first selected row
						resultsTable.scrollRectToVisible( resultsTable.getCellRect( firstSelectedRow, 0, true ) );
					dispose();
				}
			} );
			buttonsPanel.add( selectMarkedReplaysButton );
		}
		final JButton cancelButton = createCloseButton( identicalGroups.length > 0 ? "button.cancel" : "button.close" );
		buttonsPanel.add( cancelButton );
		
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		packAndShow( identicalGroups.length > 0 ? selectMarkedReplaysButton : cancelButton, false );
	}
	
	/**
	 * File descriptor.
	 * @author Andras Belicza
	 */
	private static class FileDescriptor {
		/** Index of the file.              */
		public final int  index;
		/** The file.                       */
		public final File file;
		/** MD5 of the content of the file
		 * (calculated on demand).          */
		public String     md5;
		/**
		 * Creates a new FileDescriptor.
		 * @param index index of the file
		 * @param file  the file
		 */
		public FileDescriptor( final int  index, final File file ) {
			this.index = index;
			this.file  = file;
		}
		/**
		 * Checks if our file is equals to another file.
		 * @param fd file descriptor of the other file to compare to
		 * @return true if our file is identical to the another file; false otherwise
		 */
		public boolean equalsToFile( final FileDescriptor fd ) {
			if ( md5 == null )
				md5 = GeneralUtils.calculateFileMd5( file );
			if ( md5.length() == 0 )
				return false;
			
			if ( fd.md5 == null )
				fd.md5 = GeneralUtils.calculateFileMd5( fd.file );
			if ( fd.md5.length() == 0 )
				return false;
			
			return md5.equals( fd.md5 );
		}
	}
	
	/**
	 * Finds duplicate files and returns the identical groups (groups of indices of identical files).
	 * @param resultList reference to the result list to find duplicates in
	 * @return an array of indices arrays, groups of identical files
	 */
	private static Integer[][] findDuplicates( final List< Object[] > resultList ) {
		final Map< Integer, Object >         fileClassMap    = new HashMap< Integer, Object >( resultList.size() );
		final Map< String, List< Integer > > md5IdenticalMap = new HashMap< String, List< Integer > >();
		
		for ( int i = resultList.size() - 1; i >= 0; i-- ) {
			final Object[] result = resultList.get( i ); 
			final File file = new File( (String) result[ ReplaySearch.COLUMN_FILE_NAME ] );
			if ( file.exists() ) {
				final FileDescriptor fd = new FileDescriptor( i, file );
				final Integer sizeKey = new Integer( (int) file.length() ); // It's enough to handle it as int (even if it could be greater, we check file MD5 too!)
				// Only compare to those that have the same size (size key)
				final Object fdObject = fileClassMap.get( sizeKey );
				if ( fdObject == null )
					fileClassMap.put( sizeKey, fd ); // In most cases there will be only one file for a size key, just store the file descriptor (not in a list)
				else {
					if ( fdObject instanceof FileDescriptor ) {
						final FileDescriptor fd2 = (FileDescriptor) fdObject;
						if ( fd2.equalsToFile( fd ) ) {
							List< Integer > identicalList = md5IdenticalMap.get( fd2.md5 );
							if ( identicalList == null ) {
								md5IdenticalMap.put( fd2.md5, identicalList = new ArrayList< Integer >( 4 ) );
								identicalList.add( fd2.index );
							}
							identicalList.add( i );
						}
						else {
							final List< FileDescriptor > fdList = new ArrayList< FileDescriptor >( 4 );
							fdList.add( fd2 );
							fdList.add( fd );
							fileClassMap.put( sizeKey, fdList );
						}
					}
					else {
						// fdObject is a list of file descriptors
						@SuppressWarnings("unchecked")
						final List< FileDescriptor > fdList = (List< FileDescriptor >) fdObject;
						boolean foundIdentical = false;
						for ( final FileDescriptor fd2 : fdList ) {
							if ( fd2.equalsToFile( fd ) ) {
								List< Integer > identicalList = md5IdenticalMap.get( fd2.md5 );
								if ( identicalList == null ) {
									md5IdenticalMap.put( fd2.md5, identicalList = new ArrayList< Integer >( 4 ) );
									identicalList.add( fd2.index );
								}
								identicalList.add( i );
								foundIdentical = true;
								break;
							}
						}
						if ( !foundIdentical )
							fdList.add( fd );
					}
				}
			}
		}
		
		final Integer[][] identicalGroups = new Integer[ md5IdenticalMap.size() ][];
		int counter = 0;
		for ( final List< Integer > identicalList : md5IdenticalMap.values() ) {
			identicalGroups[ counter++ ] = identicalList.toArray( new Integer[ identicalList.size() ] );
		}
		
		return identicalGroups;
	}
	
}
