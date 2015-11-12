/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.charts;

import java.awt.Graphics2D;

import javax.swing.JComponent;

/**
 * Basic Common parameters for painting charts.
 * 
 * @author Andras Belicza
 */
public class BaseChartParams {
	
	/** X coordinate of the Y axis.         */
	private final int Y_AXIS_X;
	/** Height of the chart titles.         */
	public static final int CHART_TITLE_HEIGHT   = 13;
	/** Height of the labels on the x axis. */
	public static final int X_AXIS_LABELS_HEIGHT = 13;
	
	/** Width of the chart component.  */
	public int width;
	/** Height of the chart component. */
	public int height;
	
	/** Reference to the chart canvas.            */
	public JComponent chartCanvas;
	
	/** Graphics context to be used for painting  */
	public Graphics2D g2;
	
	/** Left boundary of usable chart spaces.     */
	public int chartX1;
	/** Right boundary of usable chart spaces.    */
	public int chartX2;
	/** Chart delta x = chartX2 - chartX2.        */
	public int chartDX;
	/** Width of the usable chart space.          */
	public int chartWidth;
	/** Chart delta y = y2 - y1.                  */
	public int chartDY;
	/** Height of the usable chart space.         */
	public int chartHeight;
	
	/**
	 * Creates a new BaseChartParams.
	 * @param Y_AXIS_X X coordinate of the Y axis
	 */
	public BaseChartParams( final int Y_AXIS_X ) {
		this.Y_AXIS_X = Y_AXIS_X;
	}
	
	/**
	 * Calculates the derived data.
	 */
	public void calcualteDerivedData() {
		chartX1      = Y_AXIS_X;
		chartX2      = width - 1;
		if ( chartX2 < chartX1 )
			chartX2 = chartX1;
		chartDX      = chartX2 - chartX1;
		chartWidth   = chartDX + 1;
	}
	
}
