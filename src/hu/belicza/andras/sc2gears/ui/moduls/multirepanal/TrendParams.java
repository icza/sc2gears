/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.multirepanal;

import hu.belicza.andras.sc2gears.ui.charts.BaseChartParams;
import hu.belicza.andras.sc2gears.ui.moduls.multirepanal.MultiRepAnalysis.TrendType;

/**
 * Common parameters for painting charts.
 * 
 * @author Andras Belicza
 */
public class TrendParams extends BaseChartParams {
	
	/** X coordinate of the Y axis: no Y axis. */
	public static final int Y_AXIS_X_CONST       = 0;
	
	/** Height of the labels on the x axis.    */
	public static final int X_AXIS_LABELS_HEIGHT = 16;
	
	/** The type of the trend.                 */
	public TrendType trendType; 
	
	/** Activity data. */
	public int[] activityData;
	
	/** Top boundary of usable chart space.    */
	public int chartY1;
	/** Bottom boundary of usable chart space. */
	public int chartY2;
	
	/** Width of the bars of the trend. */
	public int barWidth;
	
	/**
	 * Creates a new ChartParams.
	 */
	public TrendParams() {
		super( Y_AXIS_X_CONST );
	}
	
	/**
	 * Calculates the derived data.
	 */
	public void calcualteDerivedData() {
		super.calcualteDerivedData();
		
		chartHeight = height - ( CHART_TITLE_HEIGHT + X_AXIS_LABELS_HEIGHT );
		if ( chartHeight < 0 )
			chartHeight = 0;
		chartDY = chartHeight - 1;
		
		chartY1 = CHART_TITLE_HEIGHT;
		chartY2 = chartY1 + chartHeight - 1;
		
		barWidth = ( chartWidth - ( ( trendType.labels.length ) << 1 ) ) / trendType.labels.length; // 2 pixels between bars
	}
	
}
