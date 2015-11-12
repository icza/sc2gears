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
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ChartParams;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.GridParams;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.GridParams.PredefinedGrid;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.GridParams.TimeUnit;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Custom grid settings for the charts of the Replay analyzer.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class GridSettingsDialog extends BaseDialog {
	
	/**
	 * Creates a new GridSettingsDialog.
	 * @param gridParams                   reference to the grid params
	 * @param chartParams                  reference to the chart parameters
	 * @param chartCanvasRepainterListener chart canvas repainter listener
	 */
	public GridSettingsDialog( final GridParams gridParams, final ChartParams chartParams, final ActionListener chartCanvasRepainterListener ) {
		super( "gridSettings.title", Icons.GRID );
		
		final Box customBox = Box.createVerticalBox();
		
		final Box northBox = Box.createVerticalBox();
		northBox.setBorder( BorderFactory.createEmptyBorder( 15, 15, 0, 15 ) );
		final ButtonGroup      buttonGroup                = new ButtonGroup();
		final PredefinedGrid[] predefinedGridValues       = PredefinedGrid.values();
		final JRadioButton[]   predefinedGridRadioButtons = new JRadioButton[ predefinedGridValues.length ];
		final ChangeListener   customBoxEnablerListener   = new ChangeListener() {
			@Override
			public void stateChanged( final ChangeEvent event ) {
				GuiUtils.setComponentTreeEnabled( customBox, predefinedGridRadioButtons[ PredefinedGrid.CUSTOM.ordinal() ].isSelected() );
			}
		};
		for ( int i = 0; i < predefinedGridValues.length; i++ ) {
			final PredefinedGrid predefinedGrid = predefinedGridValues[ i ];
			final JRadioButton   radioButton    = predefinedGridRadioButtons[ i ] = new JRadioButton( predefinedGrid.stringValue, predefinedGrid == gridParams.predefinedGrid );
			buttonGroup.add( radioButton );
			northBox.add( radioButton );
			if ( predefinedGrid == PredefinedGrid.CUSTOM )
				radioButton.addChangeListener( customBoxEnablerListener );
		}
		getContentPane().add( northBox, BorderLayout.NORTH );
		
		customBox.setBorder( BorderFactory.createEmptyBorder( 0, 15, 15, 15 ) );
		Box row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "gridSettings.firstMarker" ) ) );
		final JSpinner firstMarkerValueSpinner = new JSpinner( new SpinnerNumberModel( gridParams.firstMarker.value1.value, 0, 10000000, 1 ) );
		row.add( firstMarkerValueSpinner );
		final JComboBox< TimeUnit > firstMarkerUnitComboBox = new JComboBox<>( TimeUnit.values() );
		firstMarkerUnitComboBox.setSelectedItem( gridParams.firstMarker.value2.value );
		row.add( firstMarkerUnitComboBox );
		customBox.add( row );
		final JButton insertCurrentPositionButton = new JButton();
		GuiUtils.updateButtonText( insertCurrentPositionButton, "gridSettings.insertCurrentPositionButton" );
		insertCurrentPositionButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				if ( chartParams != null ) {
					Integer frame = null;
					if ( chartParams.markerFrame != null )
						frame = chartParams.markerFrame;
					else if ( chartParams.markerX != null )
						frame = chartParams.xToFrame( chartParams.markerX );
					if ( frame != null ) {
						firstMarkerValueSpinner.setValue( frame );
						firstMarkerUnitComboBox.setSelectedItem( TimeUnit.FRAMES );
					}
				}
			}
		} );
		row.add( insertCurrentPositionButton );
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "gridSettings.repeatMarker" ) ) );
		final JSpinner repeatMarkerValueSpinner = new JSpinner( new SpinnerNumberModel( gridParams.repeatMarker.value1.value, 0, 10000000, 1 ) );
		row.add( repeatMarkerValueSpinner );
		final JComboBox< TimeUnit > repeatMarkerUnitComboBox = new JComboBox<>( TimeUnit.values() );
		repeatMarkerUnitComboBox.setSelectedItem( gridParams.repeatMarker.value2.value );
		row.add( repeatMarkerUnitComboBox );
		row.add( new JLabel() );
		customBox.add( row );
		
		GuiUtils.alignBox( customBox, 4 );
		getContentPane().add( customBox, BorderLayout.CENTER );
		
		// Initialize current enabled state
		customBoxEnablerListener.stateChanged( null );
		
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 0, 15, 5, 15 ) );
		final JButton okButton = new JButton();
		GuiUtils.updateButtonText( okButton, "button.ok" );
		okButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				for ( int i = 0; i < predefinedGridValues.length; i++ ) {
					if ( predefinedGridRadioButtons[ i ].isSelected() )
						gridParams.predefinedGrid = predefinedGridValues[ i ];
				}
				
				gridParams.firstMarker .value1.value = (Integer ) firstMarkerValueSpinner .getValue();
				gridParams.firstMarker .value2.value = (TimeUnit) firstMarkerUnitComboBox .getSelectedItem();
				gridParams.repeatMarker.value1.value = (Integer ) repeatMarkerValueSpinner.getValue();
				gridParams.repeatMarker.value2.value = (TimeUnit) repeatMarkerUnitComboBox.getSelectedItem();
				
				gridParams.storeSettings();
				dispose();
				chartCanvasRepainterListener.actionPerformed( null );
			}
		} );
		buttonsPanel.add( okButton );
		final JButton cancelButton = createCloseButton( "button.cancel" );
		buttonsPanel.add( cancelButton );
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		packAndShow( cancelButton, false );
	}
	
}
