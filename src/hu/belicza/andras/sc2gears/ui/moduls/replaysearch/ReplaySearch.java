/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.replaysearch;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.components.ReplayOperationsPopupMenu;
import hu.belicza.andras.sc2gears.ui.components.TableBox;
import hu.belicza.andras.sc2gears.ui.dialogs.FindDuplicatesDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.ReplayListColumnSetupDialog;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.ModuleFrame;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.BuildOrderSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.BuildingAbilitySearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.BuildingSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.ChatMessageSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.DateSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.ExpansionSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.FileNameSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.FormatSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.FullPlayerSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.GameLengthSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.GameLengthSearchField.GameLengthReplayFilter;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.GameTypeSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.GatewaySearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.LadderSeasonSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.LeagueMatchupSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.MapNameSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.PlayerSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.RaceMatchupSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.ResearchSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.UnitAbilitySearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.UnitSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.UpgradeSearchField;
import hu.belicza.andras.sc2gears.ui.moduls.replaysearch.searchfieldimpl.VersionSearchField;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.NormalThread;
import hu.belicza.andras.sc2gears.util.ReplayCache;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayOpCallback;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.PanelUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The Replay Search.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class ReplaySearch extends ModuleFrame {
	
	/** Header keys of the results table. */
	public static final String[] RESULT_HEADER_KEYS = new String[] {
		"module.repSearch.tab.results.header.version",
		"module.repSearch.tab.results.header.type",
		"module.repSearch.tab.results.header.gateway",
		"module.repSearch.tab.results.header.fileDate",
		"module.repSearch.tab.results.header.map",
		"module.repSearch.tab.results.header.races",
		"module.repSearch.tab.results.header.leagues",
		"module.repSearch.tab.results.header.apms",
		"module.repSearch.tab.results.header.length",
		"module.repSearch.tab.results.header.players",
		"module.repSearch.tab.results.header.format",
		"module.repSearch.tab.results.header.winners",
		"module.repSearch.tab.results.header.eapms",
		"module.repSearch.tab.results.header.file",
		"module.repSearch.tab.results.header.comment",
	};
	/** Column model index of the file name column. */
	public static final int  COLUMN_FILE_NAME;
	/** Column model index of the comment column.   */
	private static final int COLUMN_COMMENT;
	/** Column model index of the save time column. */
	private static final int COLUMN_SAVE_TIME;
	/** Column model index of the players column.   */
	private static final int COLUMN_PLAYERS;
	/** Column model index of the winners column.   */
	private static final int COLUMN_WINNERS;
	/** Header names of the results table. */
	public static final String[] RESULT_HEADER_NAMES = new String[ RESULT_HEADER_KEYS.length ];
	static {
		int FILE_NAME_COLUMN_INDEX_ = 0;
		int COMMENT_COLUMN_INDEX_   = 0;
		int COLUMN_SAVE_TIME_       = 0;
		int COLUMN_PLAYERS_         = 0;
		int COLUMN_WINNERS_         = 0;
		for ( int i = 0; i < RESULT_HEADER_KEYS.length; i++ ) {
			RESULT_HEADER_NAMES[ i ] = Language.getText( RESULT_HEADER_KEYS[ i ] );
			if ( "module.repSearch.tab.results.header.file"         .equals( RESULT_HEADER_KEYS[ i ] ) )
				FILE_NAME_COLUMN_INDEX_ = i;
			else if ( "module.repSearch.tab.results.header.comment" .equals( RESULT_HEADER_KEYS[ i ] ) )
				COMMENT_COLUMN_INDEX_   = i;
			else if ( "module.repSearch.tab.results.header.fileDate".equals( RESULT_HEADER_KEYS[ i ] ) )
				COLUMN_SAVE_TIME_       = i;
			else if ( "module.repSearch.tab.results.header.players" .equals( RESULT_HEADER_KEYS[ i ] ) )
				COLUMN_PLAYERS_         = i;
			else if ( "module.repSearch.tab.results.header.winners" .equals( RESULT_HEADER_KEYS[ i ] ) )
				COLUMN_WINNERS_         = i;
		}
		COLUMN_FILE_NAME = FILE_NAME_COLUMN_INDEX_;
		COLUMN_COMMENT   = COMMENT_COLUMN_INDEX_;
		COLUMN_SAVE_TIME = COLUMN_SAVE_TIME_;
		COLUMN_PLAYERS   = COLUMN_PLAYERS_;
		COLUMN_WINNERS   = COLUMN_WINNERS_;
	}
	
	/** Simple counter. */
	private static final AtomicInteger counter = new AtomicInteger();
	
	/** Indicates that filters should be omitted during the next search. */
	private volatile boolean    temporarilyDisableFilters;
	
	/** Replay source of the search.                    */
	private final JList< File > sourceList              = new JList<>( new DefaultListModel< File >() );
	/** Tells if the source list has to be kept sorted. */
	private final JCheckBox     autoSortSourcesCheckBox = GuiUtils.createCheckBox( "module.repSearch.tab.source.autoSort", Settings.KEY_REP_SEARCH_SOURCE_AUTO_SORT );
	/** Performs a search.                              */
	private final JButton       performSearchButton     = new JButton( Icons.BINOCULAR_ARROW );
	
	/** Reference to the search field groups. */
	private SearchFieldGroup[] searchFieldGroups;
	
	/** Initial replay list to load. */
	private File initialReplayList;
	
	/** Model indices of the visible columns. */
	private int[] visibleColumnIndices = Settings.getVisibleReplayListColumnIndices();
	
	/**
	 * Creates a new ReplaySearch.
	 * @param arguments optional arguments to define initial replay source and/or initial replay list<br>
	 * 		the <b>first</b>  element can be an optional replay source to load<br>
	 * 		the <b>second</b> element can be an optional replay list to load<br>
	 * 		the <b>third</b>  element can be optional arrays of file to be added to the replay source<br>
	 * 		the <b>fourth</b> element can be an optional boolean indicating whether the search should be performed, or only the activation of the filters tab is required<br>
	 * 		the <b>fifth</b> element can be an optional filter file to be loaded and applied<br>
	 */
	public ReplaySearch( final Object... arguments ) {
		super( Language.getText( "module.repSearch.title", counter.incrementAndGet() ) );
		
		setFrameIcon( Icons.BINOCULAR );
		
		// Optional boolean to tell if search should be performed
		final Boolean performSearch = arguments.length > 3 ? (Boolean) arguments[ 3 ] : null;
		
		final boolean openingForFurtherFiltering = performSearch != null && !performSearch;
		
		buildGUI( openingForFurtherFiltering );
		
		final boolean repSourceSpecified         = arguments.length > 0 && arguments[ 0 ] != null;
		final boolean repListSpecified           = arguments.length > 1 && arguments[ 1 ] != null;
		final boolean repSourceElementsSpecified = arguments.length > 2 && arguments[ 2 ] != null;
		final boolean filterFileSpecified        = arguments.length > 4 && arguments[ 4 ] != null;
		
		// Optional replay source
		if ( repSourceSpecified )
			loadReplaySourceFile( (File) arguments[ 0 ] );
		
		// Optional replay list
		if ( repListSpecified ) {
			//initialReplayList = (File) arguments[ 1 ];
			final File replayListFile = (File) arguments[ 1 ];
			if ( filterFileSpecified || openingForFurtherFiltering ) {
				// Set the elements of the replay list as source 
				final List< Object[] > loadedResultList = loadReplayListFile( replayListFile );
				
				final DefaultListModel< File > model = (DefaultListModel< File >) sourceList.getModel();
				for ( final Object[] result : loadedResultList )
					model.addElement( new File( (String) result[ COLUMN_FILE_NAME ] ) );
			}
			else
				initialReplayList = replayListFile;
		}
		
		// Optional files element
		if ( repSourceElementsSpecified ) {
			final DefaultListModel< File > model = (DefaultListModel< File >) sourceList.getModel();
			for ( final File file : (File[]) arguments[ 2 ] )
				model.addElement( file );
		}
		
		// Optional filter file element
		if ( filterFileSpecified )
			loadSearchFiltersFile( (File) arguments[ 4 ] );
		
		if ( performSearch == null || performSearch ) // If there is a need to perform a search automatically...
			// ...and there is something specified to search...
			if ( repSourceSpecified || repListSpecified || repSourceElementsSpecified || filterFileSpecified ) {
				// .. then search!
				if ( !filterFileSpecified )
					temporarilyDisableFilters = true;
				performSearchButton.doClick();
			}
	}
	
	/**
	 * Builds the GUI of the frame.
	 * @param selectFiltersTab tells if the filters tab has to be selected
	 */
	private void buildGUI( final boolean selectFiltersTab ) {
		final JTabbedPane tabbedPane = new JTabbedPane();
		
		// We want the sourceList to display folder and replay icons
		sourceList.setCellRenderer( new ListCellRenderer< File >() {
			private final JLabel label = new JLabel();
			@Override
			public Component getListCellRendererComponent( final JList< ? extends File > list, final File file, final int index, final boolean isSelected, final boolean cellHasFocus ) {
				label.setText( file.getAbsolutePath() );
				label.setIcon( file.isDirectory() ? Icons.FOLDER : GuiUtils.SC2_REPLAY_FILTER.accept( file ) ? Icons.SC2 : Icons.DOCUMENT );
				
				if ( isSelected ) {
					label.setBackground( list.getSelectionBackground() );
					label.setForeground( list.getSelectionForeground() );
				} else {
					label.setBackground( list.getBackground() );
					label.setForeground( list.getForeground() );
				}
				
				label.setFont( list.getFont() );
				label.setOpaque( true );
				
				return label;
			}
		} );
		
		GuiUtils.addNewTab( Language.getText( "module.repSearch.tab.source.title" ), Icons.FOLDERS_STACK, false, tabbedPane, createSourceTab(), null );
		
		GuiUtils.addNewTab( Language.getText( "module.repSearch.tab.filters.title" ), Icons.FUNNEL, false, tabbedPane, createFiltersTab( tabbedPane ), null );
		if ( selectFiltersTab )
			tabbedPane.setSelectedIndex( 1 );
		
		getContentPane().add( tabbedPane );
	}
	
	/**
	 * Creates and returns the search source tab.
	 * @return the search source tab
	 */
	private JComponent createSourceTab() {
		final JPanel sourcePanel = new JPanel( new BorderLayout() );
		
		Box buttonsBox = Box.createVerticalBox();
		
		final JPanel buttonsMatrix = new JPanel( new GridLayout( 2, 2 ) );
		final JButton loadReplaySourceButton = new JButton( Icons.FOLDER_OPEN );
		GuiUtils.updateButtonText( loadReplaySourceButton, "module.repSearch.tab.source.loadReplaySourceButton" );
		loadReplaySourceButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( Consts.FOLDER_REPLAY_SOURCES );
				fileChooser.setDialogTitle( Language.getText( "module.repSearch.tab.source.selectLoadReplaySource" ) );
				fileChooser.setFileFilter( GuiUtils.SC2_REPLAY_SOURCE_FILE_FILTER );
				fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
				if ( fileChooser.showOpenDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION )
					loadReplaySourceFile( fileChooser.getSelectedFile() );
			}
		} );
		buttonsMatrix.add( loadReplaySourceButton );
		final JButton saveReplaySourceButton = new JButton( Icons.DISK );
		GuiUtils.updateButtonText( saveReplaySourceButton, "module.repSearch.tab.source.saveReplaySourceButton" );
		saveReplaySourceButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( Consts.FOLDER_REPLAY_SOURCES );
				fileChooser.setDialogTitle( Language.getText( "module.repSearch.tab.source.selectSaveReplaySource" ) );
				fileChooser.setFileFilter( GuiUtils.SC2_REPLAY_SOURCE_FILE_FILTER );
				fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
				if ( fileChooser.showSaveDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION ) {
					File sourceFile = fileChooser.getSelectedFile();
					// Append the extension if not provided
					if ( !GuiUtils.SC2_REPLAY_SOURCE_FILE_FILTER.accept( sourceFile ) )
						sourceFile = new File( sourceFile.getAbsolutePath() + Consts.EXT_SC2REPLAY_SOURCE );
					
					saveReplaySourceFile( sourceFile );
				}
			}
		} );
		buttonsMatrix.add( saveReplaySourceButton );
		
		final JButton addFoldersButton = new JButton( Icons.FOLDERS );
		GuiUtils.updateButtonText( addFoldersButton, "module.repSearch.tab.source.addFoldersButton" );
		addFoldersButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( GeneralUtils.getDefaultReplayFolder() );
				fileChooser.setDialogTitle( Language.getText( "module.repSearch.tab.source.selectFoldersToAdd" ) );
				fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
				fileChooser.setMultiSelectionEnabled( true );
				if ( fileChooser.showOpenDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION ) {
					final DefaultListModel< File > sourceListModel = (DefaultListModel< File >) sourceList.getModel();
					for ( final File file : fileChooser.getSelectedFiles() )
						sourceListModel.addElement( file );
					if ( autoSortSourcesCheckBox.isSelected() )
						sortSources();
				}
			}
		} );
		buttonsMatrix.add( addFoldersButton );
		final JButton addFilesButton = new JButton( Icons.SC2 );
		GuiUtils.updateButtonText( addFilesButton, "module.repSearch.tab.source.addFilesButton" );
		addFilesButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( GeneralUtils.getDefaultReplayFolder() );
				fileChooser.setDialogTitle( Language.getText( "module.repSearch.tab.source.selectReplaysToAdd" ) );
				fileChooser.setMultiSelectionEnabled( true );
				fileChooser.setFileFilter( GuiUtils.SC2_REPLAY_FILTER );
				fileChooser.setAccessory( GuiUtils.createReplayFilePreviewAccessory( fileChooser ) );
				fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
				if ( fileChooser.showOpenDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION ) {
					for ( final File file : fileChooser.getSelectedFiles() )
						( (DefaultListModel< File >) sourceList.getModel() ).addElement( file );
					if ( autoSortSourcesCheckBox.isSelected() )
						sortSources();
				}
			}
		} );
		buttonsMatrix.add( addFilesButton );
		buttonsBox.add( buttonsMatrix );
		
		final JButton listAllReplaysButton = new JButton( Icons.BINOCULAR_ARROW );
		GuiUtils.updateButtonText( listAllReplaysButton, "module.repSearch.tab.source.listAllReplays" );
		listAllReplaysButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				temporarilyDisableFilters = true;
				performSearchButton.doClick();
			}
		} );
		buttonsBox.add( GuiUtils.wrapInPanel( listAllReplaysButton ) );
		
		sourcePanel.add( GuiUtils.wrapInPanel( buttonsBox ), BorderLayout.NORTH );
		
		sourcePanel.add( new JScrollPane( sourceList ), BorderLayout.CENTER );
		sourceList.setOpaque( false );
		
		final JPanel operationsPanel = new JPanel( new GridLayout( 5, 1 ) );
		final JButton sortButton = new JButton( Icons.SORT_ALPHABET );
		autoSortSourcesCheckBox.addActionListener( new ActionListener() {
			{ actionPerformed( null ); } // Initialize
			@Override
			public void actionPerformed( final ActionEvent event ) {
				sortButton.setEnabled( !autoSortSourcesCheckBox.isSelected() );
				if ( autoSortSourcesCheckBox.isSelected() )
					sortSources();
				sourcePanel.updateUI(); // Required due to the Napkin LAF:
			}
		} );
		operationsPanel.add( autoSortSourcesCheckBox );
		GuiUtils.updateButtonText( sortButton, "module.repSearch.tab.source.sortButton" );
		sortButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				sortSources();
			}
		} );
		operationsPanel.add( sortButton );
		final JButton removeSelectedButton = new JButton( Icons.TABLE_DELETE_ROW );
		GuiUtils.updateButtonText( removeSelectedButton, "module.repSearch.tab.source.removeSelectedButton" );
		removeSelectedButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final DefaultListModel< File > model = (DefaultListModel< File >) sourceList.getModel();
				final int[] selectedIndices = sourceList.getSelectedIndices();
				for ( int i = selectedIndices.length - 1; i >= 0; i-- )
					model.remove( selectedIndices[ i ] );
			}
		} );
		operationsPanel.add( removeSelectedButton );
		final JButton purgeButton = new JButton( Icons.BROOM );
		GuiUtils.updateButtonText( purgeButton, "module.repSearch.tab.source.purgeButton" );
		purgeButton.setToolTipText( Language.getText( "module.repSearch.tab.source.purgeButtonToopTip" ) );
		purgeButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final DefaultListModel< File > model = (DefaultListModel< File >) sourceList.getModel();
				for ( int i = model.getSize() - 1; i >= 0; i-- ) {
					final File file = model.get( i );
					
					if ( file.exists() ) {
						// Is within another source?
						for ( int j = model.getSize() - 1; j >= 0; j-- )
							if ( j != i && GeneralUtils.isDescendant( model.get( j ), file ) ) {
								model.remove( i );
								break;
							}
					}
					else
						model.remove( i );
				}
			}
		} );
		operationsPanel.add( purgeButton );
		sourcePanel.add( GuiUtils.wrapInPanel( operationsPanel ), BorderLayout.EAST );
		
		// Setup drop target
		new DropTarget( sourceList, new DropTargetAdapter() {
			@Override
			public void drop( final DropTargetDropEvent event ) {
				final Transferable transferable = event.getTransferable();
				for ( final DataFlavor flavor : transferable.getTransferDataFlavors() ) {
					if ( flavor.isFlavorJavaFileListType() ) {
						// It's a file list, accept it!
						event.acceptDrop( DnDConstants.ACTION_COPY_OR_MOVE );
						try {
							@SuppressWarnings("unchecked")
							final List< File > fileList = (List< File >) transferable.getTransferData( flavor );
							
							final DefaultListModel< File > sourceListModel = (DefaultListModel< File >) sourceList.getModel();
							for ( final File file : fileList )
								sourceListModel.addElement( file );
							if ( autoSortSourcesCheckBox.isSelected() )
								sortSources();
							
							event.dropComplete( true );
							
						} catch ( final Exception e ) {
							e.printStackTrace();
							event.rejectDrop();
						}
						break;
					}
				}
			}
		} );
		
		return sourcePanel;
	}
	
	/**
	 * Loads the specified replay source file.
	 * @param replaySource replay source file to be loaded
	 */
	private void loadReplaySourceFile( final File replaySource ) {
		try ( final BufferedReader input = new BufferedReader( new InputStreamReader( new FileInputStream( replaySource ), Consts.UTF8 ) ) ) {
			final DefaultListModel< File > model = (DefaultListModel< File >) sourceList.getModel();
			model.removeAllElements();
			
			while ( input.ready() )
				model.addElement( new File( input.readLine() ) );
		} catch ( final Exception e ) {
			e.printStackTrace();
			GuiUtils.showErrorDialog( Language.getText( "module.repSearch.tab.source.failedToLoadRepSource" ) );
		}
	}
	
	/**
	 * Saves the replay sources to the specified replay source file.
	 * @param replaySource replay source file to save to
	 */
	private void saveReplaySourceFile( final File replaySource ) {
		try ( final PrintWriter output = new PrintWriter( replaySource, "UTF-8" ) ) {
			final ListModel< File > model = sourceList.getModel();
			final int size = model.getSize();
			
			for ( int i = 0; i < size; i++ )
				output.println( model.getElementAt( i ) );
			
			output.flush();
			
			MainFrame.INSTANCE.refreshNavigationTree();
			
			GuiUtils.showInfoDialog( Language.getText( "module.repSearch.tab.source.repSourceSaved" ) );
		} catch ( final Exception e ) {
			e.printStackTrace();
			GuiUtils.showErrorDialog( Language.getText( "module.repSearch.tab.source.failedToSaveRepSource" ) );
		}
	}
	
	/**
	 * Loads the specified replay list file.
	 * @param replayList replay list file to be loaded
	 * @return the list of loaded data of the replays
	 */
	public static List< Object[] > loadReplayListFile( final File replayList ) {
		try ( final BufferedReader input = new BufferedReader( new InputStreamReader( new FileInputStream( replayList ), Consts.UTF8 ) ) ) {
			input.readLine(); // Header names
			final String[] headerKeys = input.readLine().split( ";" );
			// Tells that a column in our model is at which index in the file.
			// A value of -1 indicates that the column is not present in the file.
			final int[] modelFromFileIndices = new int[ RESULT_HEADER_KEYS.length ];
			for ( int i = 0; i < modelFromFileIndices.length; i++ ) {
				int index = -1;
				for ( int j = 0; j < headerKeys.length; j++ )
					if ( RESULT_HEADER_KEYS[ i ].equals( headerKeys[ j ] ) ) {
						index = j;
						break;
					}
				modelFromFileIndices[ i ] = index;
			}
			
			final List< Object[] > dataList = new ArrayList< Object[] >();
			String line;
			while ( ( line = input.readLine() ) != null ) {
				final Object[] replayData = new Object[ RESULT_HEADER_KEYS.length ];
				final String[] fileData   = GeneralUtils.splitBySemicolon( line );
				
				for ( int i = RESULT_HEADER_KEYS.length - 1; i >= 0; i-- )
					replayData[ i ] = modelFromFileIndices[ i ] < 0 ? "" : fileData[ modelFromFileIndices[ i ] ];
				
				dataList.add( replayData );
			}
			
			return dataList;
		} catch ( final Exception e ) {
			e.printStackTrace();
			GuiUtils.showErrorDialog( Language.getText( "module.repSearch.tab.results.failedToLoadRepList" ) );
			return null;
		}
	}
	
	/**
	 * Saves the replay list to the specified replay list file.
	 * @param replayList replay list file to save to
	 * @param dataList   list of data of the replays to be saved
	 */
	private static void saveReplayListFile( final File replayList, final List< Object[] > dataList ) {
		try ( final PrintWriter output = new PrintWriter( replayList, "UTF-8" ) ) {
			// Write the header names info for external applications
			for ( final String headerName : RESULT_HEADER_NAMES ) {
				output.print( headerName.replace( ';', '_' ) );
				output.print( ';' );
			}
			output.println();
			// Write the header keys so we can identify columns
			for ( final String headerKey : RESULT_HEADER_KEYS ) {
				output.print( headerKey );
				output.print( ';' );
			}
			output.println();
			
			// And finally the data
			for ( final Object[] replayData : dataList ) {
				for ( final Object data : replayData ) {
					output.print( ( (String) data ).replace( ';', '_' ) );
					output.print( ';' );
				}
				output.println();
			}
			
			output.flush();
			
			MainFrame.INSTANCE.refreshNavigationTree();
			
			GuiUtils.showInfoDialog( Language.getText( "module.repSearch.tab.results.repListSaved" ) );
		} catch ( final Exception e ) {
			e.printStackTrace();
			GuiUtils.showErrorDialog( Language.getText( "module.repSearch.tab.results.failedToSaveRepList" ) );
		}
	}
	
	/**
	 * Sorts the model of the sourcesList.<br>
	 * A new DefaultListModel is created and set to sourcesList.
	 */
	private void sortSources() {
		final ListModel< File > model   = sourceList.getModel();
		final File[]            sources = new File[ model.getSize() ];
		
		for ( int i = sources.length - 1; i >= 0 ; i-- )
			sources[ i ] = model.getElementAt( i );
		
		Arrays.sort( sources, new Comparator< File >() {
			@Override
			public int compare( final File f1, final File f2 ) {
				// Move directories up
				if ( f1.isDirectory() && !f2.isDirectory() )
					return -1;
				if ( !f1.isDirectory() && f2.isDirectory() )
					return 1;
				
				return f1.compareTo( f2 );
			}
		} );
		
		final DefaultListModel< File > newModel = new DefaultListModel<>();
		for ( final File file : sources )
			newModel.addElement( file );
		
		sourceList.setModel( newModel );
	}
	
	/**
	 * Creates and returns the search filters tab.
	 * @return the search filters tab
	 */
	private JComponent createFiltersTab( final JTabbedPane tabbedPane ) {
		final JPanel filtersPanel = new JPanel( new BorderLayout() );
		
		final JPanel fieldsPanel = new JPanel( new BorderLayout() ) {
			@Override
			public void setUI( final PanelUI ui ) {
				// Called when LAF is changed
				super.setUI( ui );
				if ( searchFieldGroups != null )
					packSearchFields();
			}
		};
		final JScrollPane fieldsScrollPane = new JScrollPane( fieldsPanel );
		searchFieldGroups = new SearchFieldGroup[] { // fieldsPanel is not enough for parent (tested it, if no scroll is needed without new row, but it is needed with the new row, scroll bar would not appear)
				new SearchFieldGroup( fieldsScrollPane, PlayerSearchField         .class ),
				new SearchFieldGroup( fieldsScrollPane, FullPlayerSearchField     .class ),
				new SearchFieldGroup( fieldsScrollPane, MapNameSearchField        .class ),
				new SearchFieldGroup( fieldsScrollPane, RaceMatchupSearchField    .class ),
				new SearchFieldGroup( fieldsScrollPane, LeagueMatchupSearchField  .class ),
				new SearchFieldGroup( fieldsScrollPane, FileNameSearchField       .class ),
				new SearchFieldGroup( fieldsScrollPane, ChatMessageSearchField    .class ),
				new SearchFieldGroup( fieldsScrollPane, ExpansionSearchField      .class ),
				new SearchFieldGroup( fieldsScrollPane, FormatSearchField         .class ),
				new SearchFieldGroup( fieldsScrollPane, GameTypeSearchField       .class ),
				new SearchFieldGroup( fieldsScrollPane, GatewaySearchField        .class ),
				new SearchFieldGroup( fieldsScrollPane, LadderSeasonSearchField   .class ),
				new SearchFieldGroup( fieldsScrollPane, DateSearchField           .class ),
				new SearchFieldGroup( fieldsScrollPane, GameLengthSearchField     .class ),
				new SearchFieldGroup( fieldsScrollPane, VersionSearchField        .class ),
				new SearchFieldGroup( fieldsScrollPane, BuildOrderSearchField     .class ),
				new SearchFieldGroup( fieldsScrollPane, BuildingSearchField       .class ),
				new SearchFieldGroup( fieldsScrollPane, UnitSearchField           .class ),
				new SearchFieldGroup( fieldsScrollPane, ResearchSearchField       .class ),
				new SearchFieldGroup( fieldsScrollPane, UpgradeSearchField        .class ),
				new SearchFieldGroup( fieldsScrollPane, UnitAbilitySearchField    .class ),
				new SearchFieldGroup( fieldsScrollPane, BuildingAbilitySearchField.class )
		};
		
		final Box buttonsBox = Box.createHorizontalBox();
		GuiUtils.updateButtonText( performSearchButton, "module.repSearch.tab.filters.performSearchButton" );
		performSearchButton.addActionListener( new ActionListener() {
			private int resultsTabCounter = 1;
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final Object[] resultsTabObjects;
				if ( temporarilyDisableFilters ) {
					temporarilyDisableFilters = false;
					resultsTabObjects = createResultsTab( new SearchFieldGroup[ 0 ] );
				}
				else {
					boolean validAll = true;
					for ( final SearchFieldGroup searchFieldGroup : searchFieldGroups )
						validAll &= searchFieldGroup.validateAll();
					if ( !validAll ) {
						GuiUtils.showErrorDialog( Language.getText( "module.repSearch.tab.filters.invalidFieldsError" ) );
						return;
					}
					resultsTabObjects = createResultsTab( searchFieldGroups );
				}
				GuiUtils.addNewTab( Language.getText( "module.repSearch.tab.results.title", resultsTabCounter++ ), Icons.TABLE, true, tabbedPane, (JComponent) resultsTabObjects[ 0 ], (Runnable) resultsTabObjects[ 1 ] );
				tabbedPane.setSelectedIndex( tabbedPane.getTabCount() - 1 );
			}
		} );
		buttonsBox.add( performSearchButton );
		final JButton resetFieldsButton = new JButton( Icons.CROSS_WHITE );
		GuiUtils.updateButtonText( resetFieldsButton, "module.repSearch.tab.filters.resetFieldsButton" );
		resetFieldsButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				for ( final SearchFieldGroup searchFieldGroup : searchFieldGroups )
					searchFieldGroup.resetAll();
			}
		} );
		buttonsBox.add( resetFieldsButton );
		buttonsBox.add( Box.createHorizontalStrut( 20 ) );
		final JButton loadFiltersButton = new JButton( Icons.FOLDER_OPEN );
		GuiUtils.updateButtonText( loadFiltersButton, "module.repSearch.tab.filters.loadFiltersButton" );
		loadFiltersButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( Consts.FOLDER_SEARCH_FILTERS );
				fileChooser.setDialogTitle( Language.getText( "module.repSearch.tab.filters.selectLoadFiltersFile" ) );
				fileChooser.setFileFilter( GuiUtils.SEARCH_FILTER_FILE_FILTER );
				fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
				if ( fileChooser.showOpenDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION )
					loadSearchFiltersFile( fileChooser.getSelectedFile() );
			}
		} );
		buttonsBox.add( loadFiltersButton );
		final JButton saveFiltersButton = new JButton( Icons.DISK );
		GuiUtils.updateButtonText( saveFiltersButton, "module.repSearch.tab.filters.saveFiltersButton" );
		saveFiltersButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( Consts.FOLDER_SEARCH_FILTERS );
				fileChooser.setDialogTitle( Language.getText( "module.repSearch.tab.filters.selectSaveFiltersFile" ) );
				fileChooser.setFileFilter( GuiUtils.SEARCH_FILTER_FILE_FILTER );
				fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
				if ( fileChooser.showSaveDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION ) {
					File filtersFile = fileChooser.getSelectedFile();
					// Append the extension if not provided
					if ( !GuiUtils.SEARCH_FILTER_FILE_FILTER.accept( filtersFile ) )
						filtersFile = new File( filtersFile.getAbsolutePath() + Consts.EXT_SEARCH_FILTER );
					
					saveSearchFiltersFile( filtersFile );
				}
			}
		} );
		buttonsBox.add( saveFiltersButton );
		buttonsBox.add( Box.createHorizontalStrut( 20 ) );
		buttonsBox.add( MiscSettingsDialog.createLinkLabelToPredefinedListsSettings( PredefinedList.REP_SEARCH_PLAYER_NAME ) );
		filtersPanel.add( GuiUtils.wrapInPanel( buttonsBox ), BorderLayout.NORTH );
		
		final Box fieldsBox = Box.createVerticalBox();
		for ( final SearchFieldGroup searchFieldGroup : searchFieldGroups )
			fieldsBox.add( searchFieldGroup.uiComponent );
		packSearchFields();
		fieldsPanel.add( fieldsBox, BorderLayout.NORTH );
		
		filtersPanel.add( fieldsScrollPane, BorderLayout.CENTER );
		
		return filtersPanel;
	}
	
	/**
	 * Packs the search field display labels so all will have the same width (the maximum).<br>
	 * It's necessary to call this when LAF (UI) changes.
	 */
	private void packSearchFields() {
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				int maxWidth = 0;
				for ( final SearchFieldGroup searchFieldGroup : searchFieldGroups )
					for ( final SearchField searchField : searchFieldGroup.searchFieldList ) {
						searchField.displayLabel.setPreferredSize( null );
						maxWidth = Math.max( maxWidth, searchField.displayLabel.getPreferredSize().width );
					}
				
				for ( final SearchFieldGroup searchFieldGroup : searchFieldGroups ) {
					for ( final SearchField searchField : searchFieldGroup.searchFieldList ) {
						searchField.displayLabel.setPreferredSize( new Dimension( maxWidth, searchField.displayLabel.getPreferredSize().height ) );
						searchField.displayLabel.invalidate();
					}
					searchFieldGroup.parentToNotify.validate();
				}
			}
		} );
	}
	
	/**
	 * Loads the specified search filters file.
	 * @param filtersFile filters file to load
	 */
	private void loadSearchFiltersFile( final File filtersFile ) {
		try {
			final Document document    = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( filtersFile );
			final Element  rootElement = document.getDocumentElement();
			
			// Declare ID attributes
			final NodeList filterGroupElementList = rootElement.getElementsByTagName( SearchFieldGroup.FILTER_GROUP_TAG_NAME );
			final int filtersCount = filterGroupElementList.getLength();
			for ( int i = 0; i < filtersCount; i++ )
				( (Element) filterGroupElementList.item( i ) ).setIdAttribute( SearchFieldGroup.ID_ATTRIBUTE_NAME, true );
			
			for ( final SearchFieldGroup searchFieldGroup : searchFieldGroups )
				for ( int i = 0; i < searchFieldGroup.searchFieldList.size(); i++ )
					searchFieldGroup.loadValues( document );
		} catch ( final Exception e ) {
			e.printStackTrace();
			GuiUtils.showErrorDialog( Language.getText( "module.repSearch.tab.filters.failedToLoadFilters" ) );
		}
	}
	
	/**
	 * Saves the search filters to the specified search filters file.
	 * @param filtersFile filters file to save to
	 */
	private void saveSearchFiltersFile( final File filtersFile ) {
		try {
			final Document document    = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			final Element  rootElement = document.createElement( "filters" );
			rootElement.setAttribute( "version", "1.0" ); // To keep the possibility for future changes
			
			for ( final SearchFieldGroup searchFieldGroup : searchFieldGroups )
				searchFieldGroup.saveValues( document, rootElement );
			
			document.appendChild( rootElement );
			
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
			transformer.transform( new DOMSource( document ), new StreamResult( filtersFile ) );
			
			GuiUtils.showInfoDialog( Language.getText( "module.repSearch.tab.filters.filtersSaved" ) );
		} catch ( final Exception e ) {
			e.printStackTrace();
			GuiUtils.showErrorDialog( Language.getText( "module.repSearch.tab.filters.failedToSaveFilters" ) );
		}
	}
	
	/**
	 * Creates and returns a search results tab.
	 * @param searchFieldGroups reference to the array of search field groups
	 * @return an array of the search results tab and a close task
	 */
	private Object[] createResultsTab( final SearchFieldGroup[] searchFieldGroups ) {
		// Make a copy of the sources because it can be modified during the search
		final ListModel< File > model = sourceList.getModel();
		final File[] sources = new File[ model.getSize() ];
		for ( int i = sources.length - 1; i >= 0; i-- )
			sources[ i ] = model.getElementAt( i );
		
		// We have to use the same time measurement for all replays
		final boolean useRealTime = Settings.getBoolean( Settings.KEY_SETTINGS_MISC_USE_REAL_TIME_MEASUREMENT );
		
		// Determine and cache the required replay content now
		final Set< ReplayContent > requiredReplayContentSetForSearch = EnumSet.copyOf( ReplayFactory.GENERAL_INFO_CONTENT );
		final List< ReplayFilter[] > replayFiltersList = new ArrayList< ReplayFilter[] >();
		for ( final SearchFieldGroup searchFieldGroup : searchFieldGroups ) {
			final ReplayFilter[] replayFilters = searchFieldGroup.getReplayFilters();
			if ( replayFilters != null ) {
				replayFiltersList.add( replayFilters );
				final Set< ReplayContent > requiredReplayContentSet = replayFilters[ 0 ].getRequiredReplayContentSet();
				if ( requiredReplayContentSet != null )
					requiredReplayContentSetForSearch.addAll( requiredReplayContentSet );
				if ( replayFilters[ 0 ] instanceof GameLengthReplayFilter )
					for ( final ReplayFilter replayFilter : replayFilters )
						( (GameLengthReplayFilter) replayFilter ).setUseRealTime( useRealTime );
			}
		}
		
		final JPanel resultsPanel = new JPanel( new BorderLayout() );
		
		final Box northBox = Box.createVerticalBox();
		
		final List< Object[] > resultList = new ArrayList< Object[] >();
		
		final JCheckBox colorWinLossCheckBox = GuiUtils.createCheckBox( "module.repSearch.tab.results.colorWinLoss", Settings.KEY_REP_SEARCH_RESULTS_COLOR_WIN_LOSS );
		colorWinLossCheckBox.setToolTipText( Language.getText( "module.repSearch.tab.results.colorWinLossToolTip" ) );
		
		final JTable resultsTable = new JTable() {
			private final TableCellRenderer coloredRowRenderer = new DefaultTableCellRenderer() {
				private final Color  WIN_COLOR         = new Color( 170, 255, 170 );
				private final Color  LOSS_COLOR        = new Color( 255, 170, 170 );
				final List< String > favoredPlayerList = GeneralUtils.getFavoredPlayerList();
				@Override
				public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column ) {
					setBackground( null );
					super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
					
					if ( !isSelected && colorWinLossCheckBox.isSelected() ) { // Do not change selection background
						Boolean isWinner = null;
						final Object[] rowObjects = resultList.get( convertRowIndexToModel( row ) );
						final String winners = (String) rowObjects[ COLUMN_WINNERS ];
						if ( winners != null && !winners.isEmpty() ) {
							final String players = (String) rowObjects[ COLUMN_PLAYERS ];
							for ( final String favoredPlayer : favoredPlayerList ) {
								if ( GeneralUtils.isValueInCommaSeparatedList( favoredPlayer, winners ) ) {
									// Winner
									isWinner = Boolean.TRUE;
									break;
								}
								else if ( GeneralUtils.isValueInCommaSeparatedList( favoredPlayer, players ) ) {
									// Loser
									isWinner = Boolean.FALSE;
									break;
								}
							}
						}
   						setBackground( isWinner == null ? null : isWinner ? WIN_COLOR : LOSS_COLOR );
					}
					
					return this;
				}
			};
			@Override
			public boolean isCellEditable( final int row, final int column ) {
				// Only the comment column is editable
				return getColumnModel().getColumn( column ).getModelIndex() == COLUMN_COMMENT;
			}
			@Override
			public TableCellRenderer getDefaultRenderer( final Class< ? > columnClass ) {
			    return coloredRowRenderer;
			}
		};
		
		colorWinLossCheckBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				resultsTable.repaint();
			}
		} );
		
		final Box buttonsBox = Box.createHorizontalBox();
		final JButton abortButton = new JButton( Icons.CROSS_OCTAGON );
		GuiUtils.updateButtonText( abortButton, "module.repSearch.tab.results.abortButton" );
		buttonsBox.add( abortButton );
		final JButton saveReplayListButton = new JButton( Icons.DISK );
		GuiUtils.updateButtonText( saveReplayListButton, "module.repSearch.tab.results.saveButton" );
		saveReplayListButton.setEnabled( false );
		buttonsBox.add( saveReplayListButton );
		final JButton loadReplayListButton = new JButton( Icons.FOLDER_OPEN );
		GuiUtils.updateButtonText( loadReplayListButton, "module.repSearch.tab.results.loadButton" );
		loadReplayListButton.setEnabled( false );
		buttonsBox.add( loadReplayListButton );
		final JButton setAsSourceButton = new JButton( Icons.TABLE_EXPORT );
		GuiUtils.updateButtonText( setAsSourceButton, "module.repSearch.tab.results.setAsSourceButton" );
		setAsSourceButton.setToolTipText( Language.getText( "module.repSearch.tab.results.setAsSourceToolTip" ) );
		setAsSourceButton.setEnabled( false );
		buttonsBox.add( setAsSourceButton );
		final JButton multiRepAnalysisButton = new JButton( Icons.CHART_UP_COLOR );
		GuiUtils.updateButtonText( multiRepAnalysisButton, "module.repSearch.tab.results.multiRepAnalysisButton" );
		multiRepAnalysisButton.setToolTipText( Language.getText( "module.repSearch.tab.results.multiRepAnalysisToolTip" ) );
		multiRepAnalysisButton.setEnabled( false );
		buttonsBox.add( multiRepAnalysisButton );
		final JCheckBox stretchToWindowCheckBox = GuiUtils.createCheckBox( "module.repSearch.tab.results.stretchToWindow", Settings.KEY_REP_SEARCH_RESULTS_STRETCH_TO_WINDOW );
		stretchToWindowCheckBox.addActionListener( new ActionListener() {
			{ actionPerformed( null ); } // Initialize
			@Override
			public void actionPerformed( final ActionEvent event ) {
				resultsTable.setAutoResizeMode( stretchToWindowCheckBox.isSelected() ? JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF );
			}
		} );
		buttonsBox.add( stretchToWindowCheckBox );
		northBox.add( buttonsBox );
		
		final JProgressBar progressBar = new JProgressBar();
		northBox.add( progressBar );
		
		resultsPanel.add( northBox, BorderLayout.NORTH );
		
		( (DefaultTableModel) resultsTable.getModel() ).setColumnIdentifiers( RESULT_HEADER_NAMES );
		resultsTable.setAutoCreateRowSorter( true );
		resultsTable.getRowSorter().setSortKeys( Arrays.asList( new SortKey( COLUMN_SAVE_TIME, SortOrder.DESCENDING ) ) );
		
		final TableBox tableBox = new TableBox( resultsTable, getLayeredPane(), null );
		tableBox.getFilterComponentsWrapper().add( Box.createHorizontalStrut( 5 ) );
		tableBox.getFilterComponentsWrapper().add( colorWinLossCheckBox );
		tableBox.getFilterComponentsWrapper().add( Box.createHorizontalStrut( 10 ) );
		final JLabel columnSetupLabel = GeneralUtils.createLinkStyledLabel( Language.getText( "module.repSearch.tab.results.columnSetup" ) );
		columnSetupLabel.setIcon( Icons.EDIT_COLUMN );
		tableBox.getFilterComponentsWrapper().add( columnSetupLabel );
		
		final Thread searchThread = new NormalThread( "Replay search" ) {
			private final boolean cacheEnabled         = Settings.getBoolean( Settings.KEY_SETTINGS_MISC_CACHE_PREPROCESSED_REPLAYS );
			private final boolean cachedReplaysAreGood = !requiredReplayContentSetForSearch.contains( ReplayContent.GAME_EVENTS ) && !requiredReplayContentSetForSearch.contains( ReplayContent.MESSAGE_EVENTS );
			private final int     timeExclusionForApm  = Settings.getInt    ( Settings.KEY_SETTINGS_MISC_INITIAL_TIME_TO_EXCLUDE_FROM_APM ) << ReplayConsts.FRAME_BITS_IN_SECOND;
			private final Set< ReplayContent > contentSetForSearchAndCache = EnumSet.copyOf( ReplayFactory.GENERAL_DATA_CONTENT );
			{
				contentSetForSearchAndCache.addAll( requiredReplayContentSetForSearch );
				
				columnSetupLabel.addMouseListener( new MouseAdapter() {
					@Override
					public void mouseClicked( final MouseEvent event ) {
						// When changes are made and the column setup dialog is closed, columns should be refreshed:
						new ReplayListColumnSetupDialog( new Runnable() {
							@Override
							public void run() {
								visibleColumnIndices = Settings.getVisibleReplayListColumnIndices();
								refreshTableFromResultList();
							}
						} );
					};
				} );
			}
			private final ExecutorService executorService = GeneralUtils.createMultiThreadedExecutorService();
			private volatile boolean aborted;
			private int          replaysCount;
			private volatile int searchedCount;
			private volatile int skippedCount;
			@Override
			public void run() {
				final InternalFrameListener abortListener = new InternalFrameAdapter() {
					@Override
					public void internalFrameClosing( final InternalFrameEvent event ) {
						// If the Replay search is closed, we want to stop the search
						abortButton.doClick();
					}
				};
				addInternalFrameListener( abortListener );
				
				abortButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						buttonsBox.remove( abortButton );
						resultsPanel.validate();
						aborted = true;
					}
				} );
				resultsTable.getModel().addTableModelListener( new TableModelListener() {
					@Override
					public void tableChanged( final TableModelEvent event ) {
						// event.getColumn() returns the column model index
						if ( event.getColumn() >= 0 && event.getColumn() == COLUMN_COMMENT ) {
							// There seems to be having inconsistency with event.getFirstRow()! resultsTable.getEditingRow() works.
							final RowSorter< ? extends TableModel > rowSorter = resultsTable.getRowSorter();
							final int row = rowSorter.convertRowIndexToModel( resultsTable.getEditingRow() );
							resultList.get( row )[ COLUMN_COMMENT ] = resultsTable.getValueAt( resultsTable.getEditingRow(), resultsTable.getColumnModel().getColumnIndex( RESULT_HEADER_NAMES[ event.getColumn() ] ) );
						}
					}
				} );
				saveReplayListButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						final JFileChooser fileChooser = new JFileChooser( Consts.FOLDER_REPLAY_LISTS );
						fileChooser.setDialogTitle( Language.getText( "module.repSearch.tab.results.selectSaveReplayList" ) );
						fileChooser.setFileFilter( GuiUtils.SC2_REPLAY_LIST_FILE_FILTER );
						fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
						if ( fileChooser.showSaveDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION ) {
							File listFile = fileChooser.getSelectedFile();
							// Append the extension if not provided
							if ( !GuiUtils.SC2_REPLAY_LIST_FILE_FILTER.accept( listFile ) )
								listFile = new File( listFile.getAbsolutePath() + Consts.EXT_SC2REPLAY_LIST );
							
							saveReplayListFile( listFile, resultList );
						}
					}
				} );
				final ActionListener loadReplayListActionListener = new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						File listFile = null;
						if ( event == null ) {
							listFile = initialReplayList;
							initialReplayList = null; // It is used only once.
						}
						else {
							final JFileChooser fileChooser = new JFileChooser( Consts.FOLDER_REPLAY_LISTS );
							fileChooser.setDialogTitle( Language.getText( "module.repSearch.tab.results.selectLoadReplayList" ) );
							fileChooser.setFileFilter( GuiUtils.SC2_REPLAY_LIST_FILE_FILTER );
							fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
							if ( fileChooser.showOpenDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION )
								listFile = fileChooser.getSelectedFile();
						}
						
						if ( listFile != null ) {
							buttonsBox.remove( abortButton );
							resultsPanel.validate();
							progressBar.setString( Language.getText( "module.repSearch.tab.results.loadingReplayList", listFile.getName() ) );
							
							final List< Object[] > loadedResultList = loadReplayListFile( listFile );
							if ( loadedResultList != null ) {
								// Table filter have to be cleared (else when the table model changed, the filter will be called for invalid indices)
								tableBox.clearFilters();
								resultList.clear();
								resultList.addAll( loadedResultList );
								refreshTableFromResultList();
							}
							
							progressBar.setMaximum( resultList.size() );
							progressBar.setValue( resultList.size() );
							progressBar.setString( Language.getText( "module.repSearch.tab.results.loadResult", resultList.size() ) );
							saveReplayListButton  .setEnabled( true );
							loadReplayListButton  .setEnabled( true );
							setAsSourceButton     .setEnabled( true );
							multiRepAnalysisButton.setEnabled( true );
						}
					}
				};
				loadReplayListButton.addActionListener( loadReplayListActionListener );				
				setAsSourceButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						final DefaultListModel< File > model = (DefaultListModel< File >) sourceList.getModel();
						model.removeAllElements();
						
						for ( final Object[] result : resultList )
							model.addElement( new File( (String) result[ COLUMN_FILE_NAME ] ) );
					}
				} );
				multiRepAnalysisButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						final File[] files = new File[ resultList.size() ];
						int i = 0;
						for ( final Object[] result : resultList )
							files[ i++ ] = new File( (String) result[ COLUMN_FILE_NAME ] );
						MainFrame.INSTANCE.openReplaysInMultiRepAnalysis( files );
					}
				} );
				progressBar.setStringPainted( true );
				
				if ( initialReplayList != null ) {
					loadReplayListActionListener.actionPerformed( null );
					resultsPanel.add( tableBox, BorderLayout.CENTER );
					resultsPanel.validate();
				}
				else {
					progressBar.setString( Language.getText( "module.repSearch.tab.results.countingReplays" ) );
					
					for ( final File source : sources ) {
						if ( aborted )
							break;
						replaysCount += countReplays( source );
					}
					
					if ( aborted ) {
						GeneralUtils.shutdownExecutorService( executorService );
						progressBar.setString( Language.getText( "module.repSearch.tab.results.searchAborted" ) + " [" + progressBar.getString() + "]" );
						loadReplayListButton.setEnabled( true );
					}
					else {
						progressBar.setMaximum( replaysCount );
						updateProgressBar();
						for ( final File source : sources ) {
							if ( aborted )
								break;
							searchReplays( source );
						}
						GeneralUtils.shutdownExecutorService( executorService );
						
						if ( aborted )
							progressBar.setString( Language.getText( "module.repSearch.tab.results.searchAborted" ) + " [" + progressBar.getString() + "]" );
						else
							buttonsBox.remove( abortButton );
						
						resultsPanel.add( tableBox, BorderLayout.CENTER );
						resultsPanel.validate();
						
						saveReplayListButton  .setEnabled( true );
						loadReplayListButton  .setEnabled( true );
						setAsSourceButton     .setEnabled( true );
						multiRepAnalysisButton.setEnabled( true );
						
						refreshTableFromResultList();
					}
				}
				resultsTable.addMouseListener( new MouseAdapter() {
					@Override
					public void mouseClicked( final MouseEvent event ) {
						if ( event.getButton() == GuiUtils.MOUSE_BUTTON_RIGHT ) {
							if ( resultsTable.getSelectedRow() < 0 ) {
								final int row = resultsTable.rowAtPoint( event.getPoint() );
								resultsTable.getSelectionModel().setSelectionInterval( row, row );
							}
							final int[] selectedRows = resultsTable.getSelectedRows();
							if ( selectedRows.length > 0 ) {
								final File[] files = getFilesForRows( selectedRows );
								
								final ReplayOperationsPopupMenu repOpPopup = new ReplayOperationsPopupMenu( files, new ReplayOpCallback() {
									final List< Object[] > removedList = new ArrayList< Object[] >();
									@Override
									public void replayRenamed( final File file, final File newFile, final int fileIndex ) {
										resultList.get( selectedRows[ fileIndex ] )[ COLUMN_FILE_NAME ] = newFile.getAbsolutePath();
									}
									@Override
									public void replayMoved( final File file, final File targetFolder, final int fileIndex ) {
										resultList.get( selectedRows[ fileIndex ] )[ COLUMN_FILE_NAME ] = new File( targetFolder, file.getName() ).getAbsolutePath();
									}
									@Override
									public void replayDeleted( final File file, final int fileIndex ) {
										removedList.add( resultList.get( selectedRows[ fileIndex ] ) );
									}
									@Override
									public void moveRenameDeleteEnded() {
										if ( !removedList.isEmpty() )
											resultList.removeAll( removedList );
										refreshTableFromResultList();
									}
								} );
								repOpPopup.addSeparator();
								final JMenuItem removeSelectedFromTableMenuItem = new JMenuItem( Icons.TABLE_DELETE_ROW );
								removeSelectedFromTableMenuItem.setText( Language.getText( "module.repSearch.tab.results.removeSelectedMenuItem" ) );
								removeSelectedFromTableMenuItem.addActionListener( new ActionListener() {
									@Override
									public void actionPerformed( final ActionEvent event ) {
										Arrays.sort( selectedRows );
										for ( int i = selectedRows.length - 1; i >= 0; i-- )
											resultList.remove( selectedRows[ i ] );
										refreshTableFromResultList();
									}
								} );
								repOpPopup.add( removeSelectedFromTableMenuItem );
								final JMenuItem setSelectedAsSourceMenuItem = new JMenuItem( Icons.TABLE_EXPORT );
								setSelectedAsSourceMenuItem.setText( Language.getText( "module.repSearch.tab.results.setSelectedAsSourceMenuItem" ) );
								setSelectedAsSourceMenuItem.addActionListener( new ActionListener() {
									@Override
									public void actionPerformed( final ActionEvent event ) {
										final DefaultListModel< File > model = (DefaultListModel< File >) sourceList.getModel();
										model.removeAllElements();
										for ( final File file : files )
											model.addElement( file );
									}
								} );
								repOpPopup.add( setSelectedAsSourceMenuItem );
								final JMenuItem findDuplicatesMenuItem = new JMenuItem( Icons.DOCUMENTS_STACK );
								findDuplicatesMenuItem.setText( Language.getText( "module.repSearch.tab.results.findDuplicatesMenuItem" ) );
								findDuplicatesMenuItem.addActionListener( new ActionListener() {
									@Override
									public void actionPerformed( final ActionEvent event ) {
										new FindDuplicatesDialog( resultList, resultsTable );
									}
								} );
								repOpPopup.add( findDuplicatesMenuItem );
								repOpPopup.show( event.getComponent(), event.getX(), event.getY() );
							}
						}
						if ( event.getButton() == GuiUtils.MOUSE_BUTTON_LEFT )
							if ( resultsTable.getSelectedRow() >= 0 && event.getClickCount() == 2 )
								MainFrame.INSTANCE.openReplayFile( new File( (String) resultList.get( resultsTable.getRowSorter().convertRowIndexToModel( resultsTable.rowAtPoint( event.getPoint() ) ) )[ COLUMN_FILE_NAME ] ) );
					}
				} );
				
				// Register hotkeys for the results table
				Object actionKey;
				// Enter and Shift+Enter to open selected replays
				resultsTable.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), actionKey = new Object() );
				resultsTable.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK ), actionKey );
				resultsTable.getActionMap().put( actionKey, new AbstractAction() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						// If 1 replay is selected, open in analyzer, if multiple replays are selected, do a multi-rep analysis
						final int[] selectedRows = resultsTable.getSelectedRows();
						if ( selectedRows.length == 0)
							return;
						final File[] files = getFilesForRows( selectedRows );
						if ( selectedRows.length == 1 )
							MainFrame.INSTANCE.openReplayFile( files[ 0 ] );
						else
							MainFrame.INSTANCE.openReplaysInMultiRepAnalysis( files );
					}
				} );
				// Delete to delete selected replays
				resultsTable.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0 ), actionKey = new Object() );
				resultsTable.getActionMap().put( actionKey, new AbstractAction() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						final int[] selectedRows = resultsTable.getSelectedRows();
						if ( selectedRows.length > 0 ) {
							final File[] files = getFilesForRows( selectedRows );
							final ReplayOperationsPopupMenu repOpPopup = new ReplayOperationsPopupMenu( files, new ReplayOpCallback() {
								final List< Object[] > removedList = new ArrayList< Object[] >();
								@Override public void replayRenamed( final File file, final File newFile     , final int fileIndex ) {}
								@Override public void replayMoved  ( final File file, final File targetFolder, final int fileIndex ) {}
								@Override
								public void replayDeleted( final File file, final int fileIndex ) {
									removedList.add( resultList.get( selectedRows[ fileIndex ] ) );
								}
								@Override
								public void moveRenameDeleteEnded() {
									if ( !removedList.isEmpty() )
										resultList.removeAll( removedList );
									refreshTableFromResultList();
								}
							} );
							repOpPopup.deleteReplaysMenuItem.doClick();
						}
					}
				} );
				
				removeInternalFrameListener( abortListener );
			}
			
			private File[] getFilesForRows( final int[] rowIndices ) {
				final File[] files = new File[ rowIndices.length ];
				// If table is sorted, model and view indices are different:
				final RowSorter< ? extends TableModel > rowSorter = resultsTable.getRowSorter();
				for ( int i = rowIndices.length - 1; i >= 0; i-- ) {
					rowIndices[ i ] = rowSorter.convertRowIndexToModel( rowIndices[ i ] );
					files     [ i ] = new File( (String) resultList.get( rowIndices[ i ] )[ COLUMN_FILE_NAME ] );
				}
				return files;
			}
			
			private int countReplays( final File file ) {
				if ( file.isFile() )
					return GuiUtils.SC2_REPLAY_FILTER.accept( file ) ? 1 : 0;
				else {
					final File[] children = file.listFiles();
					int replaysCount = 0;
					if ( children != null )
						for ( final File child : children ) {
							if ( aborted )
								return 0;
							replaysCount += countReplays( child );
						}
					return replaysCount;
				}
			}
			
			/** Lock to be used not to use same instance variables concurrently. */
			private final Object multiThreadLock = resultList;
			
			private void searchReplays( final File file ) {
				if ( file.isFile() ) {
					if ( GuiUtils.SC2_REPLAY_FILTER.accept( file ) )
						executorService.execute( new Runnable() {
							@Override
							public void run() {
        						if ( aborted ) // ExecutorService.shutdown() continues to execute queued tasks, so check if aborted
        							return;
        						searchedCount++;
        						
        						// We could do a check: if cacheEnabled is false, we could load the replay using
        						// ReplayParser.parseReplay( file.getAbsolutePath(), requiredReplayContentSetForSearch )
        						// (if no game events are required, this would be much faster)
        						// But that way the APMs column would contain full zeros!
        						final Replay replay = ReplayCache.getReplay( file, null, timeExclusionForApm, cacheEnabled && cachedReplaysAreGood, cacheEnabled, cachedReplaysAreGood ? null : contentSetForSearchAndCache );
        						
        						if ( replay != null ) {
        							// Groups are connected with logical AND. Inside a group filters are connected with logical OR.
        							boolean accepted = true;
        							for ( final ReplayFilter[] replayFilters : replayFiltersList ) {
        								boolean acceptedByGroup = false;
        								for ( final ReplayFilter replayFilter : replayFilters )
        									if ( replayFilter.accept( file, replay ) ) {
        										acceptedByGroup = true;
        										break;
        									}
        								if ( !acceptedByGroup ) {
        									accepted = false;
        									break;
        								}
        							}
        							
        							if ( accepted ) {
        								ReplayUtils.applyFavoredPlayerListSetting( replay.details );
        								final StringBuilder apmBuilder  = new StringBuilder();
        								final StringBuilder eapmBuilder = new StringBuilder();
        								for ( final int i : replay.details.getTeamOrderPlayerIndices() ) {
        									final Player player = replay.details.players[ i ];
        									if ( apmBuilder.length() > 0 ) {
        										apmBuilder .append( ", " );
        										eapmBuilder.append( ", " );
        									}
        									apmBuilder .append( ReplayUtils.calculatePlayerApm ( replay, player ) );
        									eapmBuilder.append( ReplayUtils.calculatePlayerEapm( replay, player ) );
        								}
        								final Object[] rowData = new Object[] { replay.version, replay.initData.gameType.stringValue,
        										replay.initData.gateway == null ? "" : replay.initData.gateway.stringValue,
        										Language.formatDateTime( new Date( replay.details.saveTime ) ), replay.details.mapName,
        										replay.details.getRaceMatchup(), replay.details.getLeagueMatchup(),
        										apmBuilder.toString(),
        										ReplayUtils.formatMs( replay.gameLength * 500, useRealTime ? replay.initData.gameSpeed : GameSpeed.NORMAL ),
        										replay.details.getPlayerNamesGrouped(), replay.initData.format.stringValue, replay.details.getWinnerNames(),
        										eapmBuilder.toString(), file.getAbsolutePath(), "" };
        								synchronized ( multiThreadLock ) {
        									resultList.add( rowData );
        								}
        							}
        						}
        						else
        							skippedCount++;
        						
        						updateProgressBar();
							}
						} );
				}
				else {
					final File[] children = file.listFiles();
					if ( children != null )
						for ( final File child : children ) {
							if ( aborted )
								return;
							searchReplays( child );
						}
				}
			}
			
			private void updateProgressBar() {
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						progressBar.setValue( searchedCount );
						progressBar.setString( Language.getText( "module.repSearch.tab.results.searchStatus", resultList.size(), searchedCount, skippedCount, replaysCount, replaysCount == 0 ? 100 : 100 * searchedCount / replaysCount ) );
					}
				} );
			}
			
			private void refreshTableFromResultList() {
				// We have to refresh the table "later" to make sure the UI update will not happen while we refresh it!
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						// Store the sorting keys to restore it after the model is changed
						final List< ? extends SortKey > storedSortKeys = resultsTable.getRowSorter().getSortKeys();
						( (DefaultTableModel) resultsTable.getModel() ).setDataVector( resultList.toArray( new Object[ resultList.size() ][] ), RESULT_HEADER_NAMES );
						
						// Restore sorting keys; sortedSortKeys cannot be null because we set the initial sorting...
						resultsTable.getRowSorter().setSortKeys( storedSortKeys );
						
						// Restore column order
						final TableColumnModel columnModel = resultsTable.getColumnModel();
						for ( int i = 0; i < visibleColumnIndices.length; i++ )
							columnModel.moveColumn( resultsTable.getColumnModel().getColumnIndex( RESULT_HEADER_NAMES[ visibleColumnIndices[ i ] ] ), i );
						// Remove invisible columns
						for ( int i = visibleColumnIndices.length; i < RESULT_HEADER_NAMES.length; i++ )
							columnModel.removeColumn( columnModel.getColumn( columnModel.getColumnCount() - 1 ) );
						
						GuiUtils.packTable( resultsTable );
					}
				} );
			}
			
		};
		
		searchThread.start();
		
		return new Object[] { resultsPanel, new Runnable() {
			@Override
			public void run() {
				abortButton.doClick();
			}
		} };
	}
	
}
