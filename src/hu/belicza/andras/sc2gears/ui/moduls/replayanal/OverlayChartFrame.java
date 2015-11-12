/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.replayanal;

import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ReplayAnalyzer.ChartType;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * An internal frame to display an overlay chart.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class OverlayChartFrame extends JInternalFrame {
	
	/** Type of chart displayed in this frame. */
	public final ChartType chartType;
	
	/** Chart params for the overlay chart.    */
	private ChartParams     chartParams;
	/** Width of the main chart.               */
	private int             mainChartWidth;
	/** Height of the main chart.              */
	private int             mainChartHeight;
	
	/** Tells if the main chart size has to be projected. */
	private boolean         projectMainChartSize;
	
	/** Reference to the canvas of this overlay chart frame. */
	public final JComponent chartCanvas;
	
	/**
	 * Creates a new OverlayChartFrame.
	 * @param chartType                 chart type displayed in this frame
	 * @param container                 reference to the container to add overlay charts to
	 * @param mainChartCanvasScrollPane reference to the scroll pane of the main chart canvas
	 */
	protected OverlayChartFrame( final ChartType chartType, final Container container, final JComponent mainChartCanvasScrollPane ) {
        super( chartType.originalStringValue, true, true, true  );
        
		this.chartType = chartType;
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		
        chartCanvas = new JPanel() {
			@Override
			public void paintComponent( final Graphics graphics ) {
				if ( chartParams != null ) {
					chartParams.g2 = (Graphics2D) graphics;
					
					// Map view overlay chart is drawn with a constant size
					if ( !projectMainChartSize ) {
						chartParams.zoom = 1;
						mainChartWidth  = chartParams.width  = mainChartCanvasScrollPane.getWidth ();
						mainChartHeight = chartParams.height = mainChartCanvasScrollPane.getHeight();
						if ( chartType == ChartType.MAP_VIEW )
							chartParams.visibleRectangle = new Rectangle( 0, 0, getWidth(), getHeight() );
						else
							chartParams.visibleRectangle = new Rectangle( 0, 0, mainChartCanvasScrollPane.getWidth(), mainChartCanvasScrollPane.getHeight() );
					}
					
					if ( chartType != ChartType.MAP_VIEW ) {
						chartParams.g2.scale(
								(double) getWidth() * chartParams.zoom / mainChartWidth,
								(double) getHeight() / mainChartHeight );
						chartParams.g2.translate( -chartParams.visibleRectangle.x, 0 );
					}
					
					new ChartPainter( chartParams ).paintChart();
				}
			}
		};
		
        getContentPane().add( chartCanvas, BorderLayout.CENTER );
        
        final int width  = mainChartCanvasScrollPane.getWidth () >> 1;
        final int height = mainChartCanvasScrollPane.getHeight() >> 1;
        final int shift  = -( ChartType.values().length << 3 ) + ( chartType.ordinal() << 4 );
        setBounds( shift + ( ( container.getWidth() - width ) >> 1 ), shift + ( ( container.getHeight() - height ) >> 1 ), width, height );
        container.add( this, JLayeredPane.PALETTE_LAYER );
        setVisible( true );
	}
	
	/**
	 * Sets the chart params.
	 * @param chartParams chart params to be set
	 */
	public void setChartParams( final ChartParams chartParams ) {
		this.chartParams = chartParams;
		
		// Map view has a unique scrolling and zoom, don't try to follow it
		if ( chartType == ChartType.MAP_VIEW || chartParams.chartType == ChartType.MAP_VIEW ) {
			// If the overlay chart or the main chart is the map view:
			projectMainChartSize = false;
			if ( chartType == ChartType.MAP_VIEW && chartParams.markerX != null ) {
				// Marker X frame would go "off the radar", easiest is to simulate a marker frame
				chartParams.calcualteDerivedData(); // Required before xToFrame()
				chartParams.markerFrame = chartParams.xToFrame( chartParams.markerX );
			}
		}
		else {
			projectMainChartSize = true;
			mainChartWidth       = chartParams.width;
			mainChartHeight      = chartParams.height;
		}
		
		chartParams.chartType = chartType;
	}
	
	/**
	 * Opens a frame for the specified chart type.<br>
	 * Does nothing if a frame for that chart type already exists.
	 * @param chartType                 chart type to open a frame for
	 * @param overlayChartFrameList     reference to the list of overlay chart frames
	 * @param container                 reference to the container to add overlay charts to
	 * @param mainChartCanvasScrollPane reference to the scroll pane of the main chart canvas
	 * @return true if the frame did not exist and was opened; false otherwise
	 */
	public static boolean openOverlayChart( final ChartType chartType, final List< OverlayChartFrame > overlayChartFrameList, final Container container, final JComponent mainChartCanvasScrollPane ) {
		// First check if there already exists the frame to be created
		for ( final OverlayChartFrame overlayChartFrame : overlayChartFrameList )
			if ( overlayChartFrame.chartType == chartType )
				return false; // Already exists, nothing to do
		
		// Have to create it
		final OverlayChartFrame overlayChartFrame = new OverlayChartFrame( chartType, container, mainChartCanvasScrollPane );
		
		overlayChartFrame.addInternalFrameListener( new InternalFrameAdapter() {
			@Override
			public void internalFrameActivated( final InternalFrameEvent event ) {
				// Bring the overlay chart to front that was selected
				container.setComponentZOrder( (OverlayChartFrame) event.getSource(), 0 );
			}
			@Override
			public void internalFrameClosed( final InternalFrameEvent event ) {
				// dispose() removes the overlay chart frame from its parent
				overlayChartFrameList.remove( event.getSource() );
				// If this frame was the focus owner, then focus is "lost" now... do something about that
				container.requestFocusInWindow();
			};
		} );
		
		overlayChartFrameList.add( overlayChartFrame );
		
		return true;
	}
	
	/**
	 * Closes the frame of the specified chart type.<br>
	 * Does nothing if the frame of that chart type does not exist.
	 * @param chartType             chart type to close the frame of
	 * @param overlayChartFrameList reference to the list of overlay chart frames
	 * @return true if the frame existed and was closed; false otherwise
	 */
	public static boolean closeOverlayChart( final ChartType chartType, final List< OverlayChartFrame > overlayChartFrameList ) {
		for ( final OverlayChartFrame overlayChartFrame : overlayChartFrameList )
			if ( overlayChartFrame.chartType == chartType ) {
				overlayChartFrame.dispose();
				return true;
			}
		
		return false;
	}
	
	/**
	 * Inverts the "state" of the frame of the specified chart type.<br>
	 * If the frame of that chart type already exists, closes it, else opens it.
	 * @param chartType                 chart type to invert the frame of
	 * @param overlayChartFrameList     reference to the list of overlay chart frames
	 * @param container                 reference to the container to add overlay charts to
	 * @param mainChartCanvasScrollPane reference to the scroll pane of the main chart canvas
	 */
	public static void invertOverlayChart( final ChartType chartType, final List< OverlayChartFrame > overlayChartFrameList, final Container container, final JComponent mainChartCanvasScrollPane ) {
		if ( !openOverlayChart( chartType, overlayChartFrameList, container, mainChartCanvasScrollPane ) )
			closeOverlayChart( chartType, overlayChartFrameList );
	}
	
}
