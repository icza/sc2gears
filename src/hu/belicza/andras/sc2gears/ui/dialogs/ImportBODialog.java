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
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.importing.PlayerSpecification;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.importing.ReplaySpecification;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

/**
 * Dialog to import Build orders.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class ImportBODialog extends BaseDialog {
	
	/** Maximum number of Build orders. */
	private static final int MAX_BUILD_ORDERS = 8;
	
	/**
	 * Replay version to simulate.
	 * @author Andras Belicza
	 */
	private static enum SimulatedVersion {
		/** Version 2.0.x.x.          */
		V_2_0  ( "2.0"  , new int[] { 2, 0, 7, 25604 } ),
		/** Version 1.5.x.x.          */
		V_1_5  ( "1.5"  , new int[] { 1, 5, 4, 24540 } ),
		/** Version 1.4.4.x.          */
		V_1_4_4( "1.4.4", new int[] { 1, 4, 4, 22418 } ),
		/** Version 1.4.2.x.          */
		V_1_4_2( "1.4.2", new int[] { 1, 4, 2, 20141 } ),
		/** Version 1.4.1.x.          */
		V_1_4_1( "1.4.1", new int[] { 1, 4, 1, 19776 } ),
		/** Version 1.3.6.x.          */
		V_1_3_6( "1.3.6", new int[] { 1, 3, 6, 19269 } ),
		/** Version 1.3.0.x - 1.3.2.x */
		V_1_3_2( "1.3.2", new int[] { 1, 3, 2, 18317 } ),
		/** Version 1.2.x.x.          */
		V_1_2  ( "1.2"  , new int[] { 1, 2, 0, 17811 } ),
		/** Version 1.1.x.x.          */
		V_1_1  ( "1.1"  , new int[] { 1, 1, 3, 16939 } ),
		/** Version 1.0.x.x.          */
		V_1_0  ( "1.0"  , new int[] { 1, 0, 3, 16291 } );
		
		/** String representation of the simulated version.        */
		public final String stringValue;
		/** Corresponding replay version of the simulated version. */
		public final int[]  version;
		
		/**
		 * Creates a new SimulatedVersion.
		 * @param stringValue
		 * @param version
		 */
		private SimulatedVersion( final String stringValue, final int[] version ) {
			this.stringValue = stringValue;
			this.version     = version;
		}
		
		@Override
		public String toString() {
			return stringValue;
		}
		
	}
	
	/**
	 * A panel to import a BO.
	 * @author Andras Belicza
	 */
	private static class ImportPanel extends JPanel {
		
		/** Text area for the BO text import. */
		private final JTextArea buildOrderTextArea = new JTextArea( 1, 14 );
		/** Combo box to select a team.       */
		private final JComboBox< Integer > teamComboBox = new JComboBox<>();
		
		/**
		 * Creates a new ImportPanel.
		 * @param n id of the import panel
		 */
		public ImportPanel( int id ) {
			super( new BorderLayout() );
			setBorder( BorderFactory.createTitledBorder( Language.getText( "importBuildOrders.importPanelTitle", id ) ) );
			
			add( new JScrollPane( buildOrderTextArea ), BorderLayout.CENTER );
			
			final JPanel controlsBox = new JPanel( new GridLayout( 3, 1 ) );
			
			final JButton loadFromFileButton = new JButton( Language.getText( "importBuildOrders.loadFromFileButton" ) );
			loadFromFileButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					final JFileChooser fileChooser = new JFileChooser( Settings.getString( Settings.KEY_IMPORT_BO_LATEST_IMPORT_FOLDER ) );
					fileChooser.setDialogTitle( Language.getText( "importBuildOrders.loadFromFileTitle" ) );
					fileChooser.setFileFilter( GuiUtils.TEXT_FILE_FILTER );
					if ( fileChooser.showOpenDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION ) {
						final File file = fileChooser.getSelectedFile();
						final String parent = file.getParent();
						if ( parent != null )
							Settings.set( Settings.KEY_IMPORT_BO_LATEST_IMPORT_FOLDER, file.getParent() );
						try ( final FileInputStream input = new FileInputStream( fileChooser.getSelectedFile() ) ) {
							final byte[] content = new byte[ (int) file.length() ];
							if ( input.read( content ) > 0 ) {
								// If \r\n are used in the source file, manually editing the text (appending text to the end of a line)
								// would insert the text between the \r and \n!! To eliminate this effect, replace line end sequences with a single char: \n
								buildOrderTextArea.setText( new String( content, Consts.UTF8 ).replace( "\r\n", "\n" ) );
							}
						} catch ( final IOException ie ) {
							ie.printStackTrace();
						}
					}
				}
			} );
			controlsBox.add( loadFromFileButton );
			
			final JButton clearButton = new JButton( Language.getText( "importBuildOrders.clearButton" ) );
			clearButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					buildOrderTextArea.setText( "" );
				}
			} );
			controlsBox.add( clearButton );
			
			final Box teamBox = Box.createHorizontalBox();
			teamBox.add( new JLabel( Language.getText( "importBuildOrders.team" ) ) );
			for ( int i = 1; i <= MAX_BUILD_ORDERS; i++ )
				teamComboBox.addItem( i );
			teamComboBox.setSelectedIndex( id - 1 );
			teamBox.add( teamComboBox );
			
			controlsBox.add( teamBox );
			
			add( controlsBox, BorderLayout.EAST );
		}
		
	}
	
	/**
	 * Creates a new ImportBODialog.
	 */
	public ImportBODialog() {
		super( "importBuildOrders.title", Icons.BLOCK_ARROW );
		
		setModal( false );
		
		final Box northBox = Box.createVerticalBox();
		northBox.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		
		final JLabel infoLabel = new JLabel( Language.getText( "importBuildOrders.info", Consts.APPLICATION_NAME ) );
		GuiUtils.changeFontToBold( infoLabel );
		northBox.add( GuiUtils.wrapInPanel( infoLabel ) );
		JPanel wrapper = new JPanel();
		wrapper.add( new JLabel( Language.getText( "importBuildOrders.info2" ) ) );
		wrapper.add( GeneralUtils.createLinkLabel( Language.getText( "importBuildOrders.boTextFormatSpecification" ), Consts.URL_BO_TEXT_SPECIFICATION ) );
		northBox.add( wrapper );
		
		wrapper = new JPanel();
		northBox.add( wrapper );
		northBox.add( new JSeparator() );
		
		final Box globalDataBox = Box.createVerticalBox();
		Box row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "importBuildOrders.replayVersionToSimulate" ) ) );
		final JComboBox< SimulatedVersion > versionComboBox = new JComboBox<>( SimulatedVersion.values() );
		row.add( versionComboBox );
		globalDataBox.add( row );
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "importBuildOrders.gameSpeed" ) ) );
		final Vector< GameSpeed > gameSpeedVector = new Vector< GameSpeed >( EnumSet.complementOf( EnumSet.of( GameSpeed.UNKNOWN ) ) );
		Collections.sort( gameSpeedVector, Collections.reverseOrder() );
		final JComboBox< GameSpeed > gameSpeedComboBox = new JComboBox<>( gameSpeedVector );
		row.add( gameSpeedComboBox );
		globalDataBox.add( row );
		GuiUtils.alignBox( globalDataBox, 2 );
		northBox.add( GuiUtils.wrapInPanel( globalDataBox ) );
		add( northBox, BorderLayout.NORTH );
		
		final ImportPanel[] importPanels = new ImportPanel[ MAX_BUILD_ORDERS ];
		final JPanel importPanelsWrapper = new JPanel( new GridLayout( 3, 3 ) );
		importPanelsWrapper.setBorder( BorderFactory.createEmptyBorder( 0, 10, 5, 10 ) );
		for ( int i = 0; i < importPanels.length; i++ )
			importPanelsWrapper.add( importPanels[ i ] = new ImportPanel( i + 1 ) );
		getContentPane().add( importPanelsWrapper, BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 5, 5 ) );
		final JButton openBuildOrdersButton = new JButton( Icons.CHART );
		GuiUtils.updateButtonText( openBuildOrdersButton, "importBuildOrders.openBuildOrdersButton" );
		openBuildOrdersButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final List< PlayerSpecification > playerSpecList = new ArrayList< PlayerSpecification >( MAX_BUILD_ORDERS );
				for ( int i = 0; i < importPanels.length; i++ ) {
					final ImportPanel importPanel = importPanels[ i ];
					final String buildOrderText = importPanel.buildOrderTextArea.getText();
					if ( buildOrderText.length() > 0 ) {
						final PlayerSpecification playerSpec = new PlayerSpecification();
						playerSpec.name           = "BO" + ( i + 1 );
						playerSpec.team           = (Integer) importPanel.teamComboBox.getSelectedItem();
						playerSpec.buildOrderText = buildOrderText;
						playerSpecList.add( playerSpec );
					}
				}
				final ReplaySpecification replaySpec = new ReplaySpecification();
				replaySpec.version   = ( (SimulatedVersion) versionComboBox.getSelectedItem() ).version;
				replaySpec.gameSpeed = (GameSpeed) gameSpeedComboBox.getSelectedItem();
				replaySpec.playerSpecifications = playerSpecList.toArray( new PlayerSpecification[ playerSpecList.size() ] );
				MainFrame.INSTANCE.openReplaySpecification( replaySpec );
				dispose();
			}
		} );
		buttonsPanel.add( openBuildOrdersButton );
		final JButton closeButton = createCloseButton( "button.cancel" );
		buttonsPanel.add( closeButton );
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		packAndShow( openBuildOrdersButton, false );
	}
	
}
