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

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.components.TableBox;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * Dialog to list the previously shared replays.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class MySharedReplaysDialog extends BaseDialog {
	
	/** File that stores the My shared replays. */
	private static final File MY_SHARED_REPLAYS_FILE = new File( Consts.FOLDER_USER_CONTENT, "My shared replays.csv" );
	
	/** Header keys of the shared replays table.<br>
	 * The order of the header fields MUST NOT be changed: saved a new entry does not check the order.
	 * The list can only be extended by new columns. */
	private static final String[] SHARED_REPLAYS_HEADER_KEYS = new String[] {
		"mySharedReplays.header.uploadedAt",
		"mySharedReplays.header.replaySite",
		"mySharedReplays.header.userName",
		"mySharedReplays.header.reportedName",
		"mySharedReplays.header.description",
		"mySharedReplays.header.replayUrl"
	};
	
	/** Column model index of the uploaded at column. */
	private static final int COLUMN_UPLOADED_AT;
	/** Column model index of the replay URL column.  */
	private static final int COLUMN_REPLAY_URL;
	/** Header names of the shared replays table.     */
	private static final Vector< String > SHARED_REPLAYS_HEADER_NAME_VECTOR = new Vector< String >( SHARED_REPLAYS_HEADER_KEYS.length );
	static {
		int COLUMN_UPLOADED_AT_ = 0;
		int COLUMN_REPLAY_URL_  = 0;
		for ( int i = 0; i < SHARED_REPLAYS_HEADER_KEYS.length; i++ ) {
			SHARED_REPLAYS_HEADER_NAME_VECTOR.addElement( Language.getText( SHARED_REPLAYS_HEADER_KEYS[ i ] ) );
			if ( "mySharedReplays.header.uploadedAt".equals( SHARED_REPLAYS_HEADER_KEYS[ i ] ) )
				COLUMN_UPLOADED_AT_ = i;
			else if ( "mySharedReplays.header.replayUrl".equals( SHARED_REPLAYS_HEADER_KEYS[ i ] ) )
				COLUMN_REPLAY_URL_ = i;
		}
		COLUMN_UPLOADED_AT = COLUMN_UPLOADED_AT_;
		COLUMN_REPLAY_URL  = COLUMN_REPLAY_URL_;
	}
	
	/**
	 * Creates a new MySharedReplaysDialog.
	 */
	public MySharedReplaysDialog() {
		super( "mySharedReplays.title", Icons.DOCUMENT_SHARE );
		
		final Vector< Vector< String > > sharedRepInfo = getSharedRepInfo();
		if ( sharedRepInfo == null )
			return;
		
		setModal( false );
		
		final JPanel northPanel = new JPanel( new BorderLayout() );
		final JPanel wrapper = new JPanel();
		final JLabel infoLabel = new JLabel( Language.getText( "mySharedReplays.numberOfSharedReplays", sharedRepInfo.size() ), JLabel.CENTER );
		GuiUtils.changeFontToBold( infoLabel );
		wrapper.add( infoLabel );
		final JButton storeListButton = new JButton( Icons.SERVER_NETWORK );
		GuiUtils.updateButtonText( storeListButton, "mySharedReplays.storeListButton" );
		storeListButton.setEnabled( !sharedRepInfo.isEmpty() );
		storeListButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				GeneralUtils.storeOtherFile( MY_SHARED_REPLAYS_FILE, Language.getText( "mySharedReplays.mySharedReplaysFileComment" ), "mySharedReplays.storingMySharedReplayList" );
			}
		} );
		wrapper.add( storeListButton );
		northPanel.add( wrapper, BorderLayout.NORTH );
		final ReplayLinkPanel replayLinkPanel = new ReplayLinkPanel();
		northPanel.add( replayLinkPanel, BorderLayout.CENTER );
		getContentPane().add( northPanel, BorderLayout.NORTH );
		
		final JTable sharedReplaysTable = GuiUtils.createNonEditableTable();
		( (DefaultTableModel) sharedReplaysTable.getModel() ).setDataVector( sharedRepInfo, SHARED_REPLAYS_HEADER_NAME_VECTOR );
		sharedReplaysTable.setAutoCreateRowSorter( true );
		sharedReplaysTable.getRowSorter().setSortKeys( Arrays.asList( new SortKey( COLUMN_UPLOADED_AT, SortOrder.DESCENDING ) ) );
		sharedReplaysTable.setPreferredScrollableViewportSize( new Dimension( 950, 500 ) );
		sharedReplaysTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			@Override
			public void valueChanged( final ListSelectionEvent event ) {
				if ( !event.getValueIsAdjusting() && sharedReplaysTable.getSelectedRow() >= 0 ) {
					// If table is sorted, model and view indices are different:
					final RowSorter< ? extends TableModel > rowSorter = sharedReplaysTable.getRowSorter();
					replayLinkPanel.setUrl( sharedRepInfo.get( rowSorter.convertRowIndexToModel( sharedReplaysTable.getSelectedRow() ) ).get( COLUMN_REPLAY_URL ), false, false );
				}
			}
		} );
		final TableBox tableBox = new TableBox( sharedReplaysTable, getLayeredPane(), null );
		final JScrollPane tableScrollPane =  new JScrollPane( tableBox );
		tableScrollPane.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 10 ) );
		getContentPane().add( tableScrollPane, BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		final JButton closeButton = createCloseButton( "button.close" );
		buttonsPanel.add( closeButton );
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		packAndShow( closeButton, false );
	}
	
	/**
	 * Parses the My shared replay list file, and returns the result.
	 * @return a vector of vectors; each vector describes one shared replay which will be one line in the shared replay table
	 */
	private static Vector< Vector< String > > getSharedRepInfo() {
		if ( !MY_SHARED_REPLAYS_FILE.exists() )
			return new Vector< Vector< String > >( 0 );
		
		try ( final BufferedReader input = new BufferedReader( new InputStreamReader( new FileInputStream( MY_SHARED_REPLAYS_FILE ), Consts.UTF8 ) ) ) {
			input.readLine(); // Header names
			final String[] headerKeys = input.readLine().split( ";" );
			// Tells that a column in our model is at which index in the file.
			// A value of -1 indicates that the column is not present in the file.
			final int[] modelFromFileIndices = new int[ SHARED_REPLAYS_HEADER_KEYS.length ];
			for ( int i = 0; i < modelFromFileIndices.length; i++ ) {
				int index = -1;
				for ( int j = 0; j < headerKeys.length; j++ )
					if ( SHARED_REPLAYS_HEADER_KEYS[ i ].equals( headerKeys[ j ] ) ) {
						index = j;
						break;
					}
				modelFromFileIndices[ i ] = index;
			}
			
			final Vector< Vector< String > > dataVector = new Vector< Vector< String > >();
			String line;
			while ( ( line = input.readLine() ) != null ) {
				final Vector< String > replayData = new Vector< String >( SHARED_REPLAYS_HEADER_KEYS.length );
				final String[] fileData = GeneralUtils.splitBySemicolon( line );
				
				for ( int i = 0; i < SHARED_REPLAYS_HEADER_KEYS.length; i++ )
					replayData.addElement( modelFromFileIndices[ i ] < 0 ? "" : fileData[ modelFromFileIndices[ i ] ] );
				
				dataVector.addElement( replayData );
			}
			
			return dataVector;
		} catch ( final Exception e ) {
			e.printStackTrace();
			GuiUtils.showErrorDialog( Language.getText( "mySharedReplays.failedToLoadSharedReplayList" ) );
			return null;
		}
	}
	
	/**
	 * Saves the info of a shared replay into the My shared replay list file.
	 * @param replaySite   replay site the replay was uploaded to
	 * @param userName     user name that was used to upload
	 * @param reportedName reported name of the replay
	 * @param description  description of the replay
	 * @param replayUrl    replay URL
	 */
	public static synchronized void saveSharedRepInfo( final String replaySite, final String userName, final String reportedName, final String description, final String replayUrl ) {
		final boolean existed = MY_SHARED_REPLAYS_FILE.exists();
		
		try ( final PrintWriter output = new PrintWriter( new OutputStreamWriter( new FileOutputStream( MY_SHARED_REPLAYS_FILE, true ), Consts.UTF8 ) ) ) {
			if ( !existed ) {
				// Write the header names info for external applications
				for ( final String headerName : SHARED_REPLAYS_HEADER_NAME_VECTOR ) {
					output.print( headerName.replace( ';', '_' ) );
					output.print( ';' );
				}
				output.println();
				// Write the header keys so we can identify columns
				for ( final String headerKey : SHARED_REPLAYS_HEADER_KEYS ) {
					output.print( headerKey );
					output.print( ';' );
				}
				output.println();
			}
			
			// And finally the data
			for ( final String data : new String[] { Language.formatDateTime( new Date() ), replaySite, userName, reportedName, description, replayUrl } ) {
				output.print( data.replace( "\n\r", " " ).replace( '\n', ' ' ).replace( '\r', ' ' ).replace( ';', '_' ) );
				output.print( ';' );
			}
			output.println();
			
			output.flush();
			
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
	}
	
}
