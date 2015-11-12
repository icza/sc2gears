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
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.OverlayChartFrame;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ReplayAnalyzer.ChartType;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Dialog to set overlay charts.
 * TODO consider settings for in-line overlay charts besides popup overlay charts
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class OverlayChartsDialog extends BaseDialog {
	
	/**
	 * Creates a new OverlayChartsDialog.
	 * @param overlayChartFrameList        reference to the list of overlay chart frames
	 * @param container                    reference to the container to add overlay charts to
	 * @param chartCanvasRepainterListener chart canvas repainter listener
	 * @param mainChartCanvasScrollPane    reference to the scroll pane of the main chart canvas
	 */
	public OverlayChartsDialog( final List< OverlayChartFrame > overlayChartFrameList, final Container container, final ActionListener chartCanvasRepainterListener, final JComponent mainChartCanvasScrollPane ) {
		super( "overlayCharts.title", Icons.CHART_PLUS );
		
		final Box box = Box.createVerticalBox();
		box.setBorder( BorderFactory.createEmptyBorder( 15, 25, 5, 25 ) );
		box.add( new JLabel( Language.getText( "overlayCharts.selectOverlayCharts" ) ) );
		box.add( Box.createVerticalStrut( 10 ) );
		
		final ChartType[] chartTypes = ChartType.values();
		final JCheckBox[] chartCheckBoxes = new JCheckBox[ chartTypes.length ];
		for ( int i = 0; i < chartTypes.length; i++ )
			box.add( chartCheckBoxes[ i ] = new JCheckBox( chartTypes[ i ].originalStringValue ) );
		for ( final OverlayChartFrame overlayChartFrame : overlayChartFrameList )
			chartCheckBoxes[ overlayChartFrame.chartType.ordinal() ].setSelected( true );
			
		getContentPane().add( box, BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 5, 25, 5, 25 ) );
		final JButton okButton = new JButton();
		GuiUtils.updateButtonText( okButton, "button.ok" );
		okButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				for ( int i = 0; i < chartCheckBoxes.length; i++ )
					if ( chartCheckBoxes[ i ].isSelected() )
						OverlayChartFrame.openOverlayChart( chartTypes[ i ], overlayChartFrameList, container, mainChartCanvasScrollPane );
					else
						OverlayChartFrame.closeOverlayChart( chartTypes[ i ], overlayChartFrameList );
				
				dispose();
				
				chartCanvasRepainterListener.actionPerformed( null );
			}
		} );
		buttonsPanel.add( okButton );
		final JButton cancelButton = createCloseButton( "button.cancel" );
		buttonsPanel.add( cancelButton );
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		packAndShow( okButton, false );
	}
	
}
