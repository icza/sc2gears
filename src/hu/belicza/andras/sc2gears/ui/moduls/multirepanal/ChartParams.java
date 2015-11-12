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
import hu.belicza.andras.sc2gears.ui.charts.ChartUtils.GraphApproximation;
import hu.belicza.andras.sc2gears.ui.moduls.multirepanal.MultiRepAnalysis.ChartGranularity;
import hu.belicza.andras.sc2gears.ui.moduls.multirepanal.MultiRepAnalysis.ChartType;

/**
 * Common parameters for painting charts.
 * 
 * @author Andras Belicza
 */
public class ChartParams extends BaseChartParams {
	
	/** X coordinate of the Y axis.         */
	public static final int Y_AXIS_X_CONST = 30;
	
	/** The type of the chart.                     */
	public ChartType                chartType; 
	/** Granularity of the chart.                  */
	public ChartGranularity         chartGranularity;
	/** Approximation of the graph.                */
	public GraphApproximation       graphApproximation;
	
	/** Player statistics (needed for avg marker). */
	public PlayerStatistics         playerStatistics;
	/** Statistics of the segments of the chart.   */
	public ChartSegmentStatistics[] segmentStats; 
	
	/** Top boundary of usable chart space.        */
	public int chartY1;
	/** Bottom boundary of usable chart space.     */
	public int chartY2;
	
	/**
	 * Creates a new ChartParams.
	 */
	public ChartParams() {
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
	}
	
}
