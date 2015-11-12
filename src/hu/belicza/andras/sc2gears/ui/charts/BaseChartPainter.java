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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;

/**
 * This class is responsible for painting the different charts.
 * 
 * @author Andras Belicza
 */
public class BaseChartPainter {

	/** Background color.                        */
	public static final Color COLOR_BACKGROUND     = Color.BLACK;
	/** Color of the axis lines and axis titles. */
	public static final Color COLOR_AXIS           = Color.YELLOW;
	/** Color of the assist lines.               */
	public static final Color COLOR_ASSIST_LINES   = new Color( 60, 60, 60 );
	/** Color of the axis labels.                */
	public static final Color COLOR_AXIS_LABELS    = Color.CYAN;
	/** Default color of the players.            */
	public static final Color COLOR_PLAYER_DEFAULT = Color.WHITE;
	
	/** Minimal distance between assist lines. */
	public static final int ASSIST_LINES_MIN_DISTANCE = 20;
	/** Minimal distance between time labels.  */
	public static final int TIME_LABELS_MIN_DISTANCE  = 100;
	
	/** Default stroke.                                                          */
	protected static final Stroke STROKE_DEFAULT                  = new BasicStroke();
	/** Dashed stroke (stroke of the of the marker).                             */
	protected static final Stroke STROKE_DASHED                   = new BasicStroke( 1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] { 5 }, 1 );
	/** Dashed stroke with double width (for missing chart data, estimates).     */
	protected static final Stroke STROKE_DASHED_DOUBLE            = new BasicStroke( 2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] { 5 }, 1 );
	/** Double width stroke (for curves).                                        */
	protected static final Stroke STROKE_DOUBLE_WIDTH             = new BasicStroke( 2.0f );
	/** Double width stroke with rounded joins (for polygons).                   */
	protected static final Stroke STROKE_DOUBLE_WIDTH_ROUNDED     = new BasicStroke( 2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND );
	/** Stroke with rounded joins (for polygons).                                */
	protected static final Stroke STROKE_ROUNDED                  = new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND );
	/** Slightly dashed stroke (stroke of the of EAPM chart).                    */
	protected static final Stroke STROKE_SLIGHTLY_DASHED          = new BasicStroke( 1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] { 5, 2 }, 1 );
	/** Slightly dashed stroke with rounded joins (stroke of the of EAPM chart). */
	protected static final Stroke STROKE_SLIGHTLY_DASHED_ROUNDED  = new BasicStroke( 1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[] { 5, 2 }, 1 );
	/** Dotted stroke (stroke of the of XAPM chart).                    */
	protected static final Stroke STROKE_DOTTED                   = new BasicStroke( 1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0f, new float[] { 1, 2f }, 0.5f );
	
	/** Base parameters for painting charts.    */
	protected final BaseChartParams params;
	/** Graphics context to be used for painting (a short-cut reference as it is in the chart params). */
	protected final Graphics2D  g2;
	
	/**
	 * Creates a new ChartPainter.
	 * @param chartParams common parameters for painting charts
	 */
	public BaseChartPainter( final BaseChartParams chartParams ) {
		this.g2     = chartParams.g2;
		this.params = chartParams;
	}
	
	/**
	 * Paints the specified chart type.
	 * @param arguments arguments for the specific chart 
	 */
	public void paintChart() {
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		
		g2.setBackground( COLOR_BACKGROUND );
		g2.clearRect( 0, 0, params.width, params.height );
	}
	
	/**
	 * Draws a cubic curve with the current settings.<br>
	 * <code>xPoints</code> and <code>yPoints</code> must contain at least 4 points!
	 * @param xPoints    x coordinates of the points of the curve
	 * @param yPoints    y coordinates of the points of the curve
	 * @param startIndex start index to draw segments from
	 * @param endIndex   end index to draw segments up to
	 */
	public void drawCubicCurve2D( final int[] xPoints, final int[] yPoints, final int startIndex, final int endIndex ) {
		if ( xPoints.length < 2 )
			return;
		final CubicCurve2D.Float cubicCurve = new CubicCurve2D.Float();
		this.xPoints = xPoints;
		this.yPoints = yPoints;
		Point2D.Float p = new Point2D.Float(), p1 = new Point2D.Float(), p2 = new Point2D.Float(), p3 = new Point2D.Float(), pTemp;
		final float s = 0.25f;
		getCP( startIndex, p ); getCP( startIndex+1, p1 ); getCP( startIndex+2, p2 );
		for ( int j = startIndex; j < endIndex; j++ ) {
			getCP( j+3, p3 );
			if ( yPoints[ j ] < 0 || yPoints[ j+1 ] < 0 )
				g2.setStroke( STROKE_DASHED_DOUBLE );
			cubicCurve.setCurve(
					p1.x, p1.y,
					-s*p.x + p1.x + s*p2.x, -s*p.y + p1.y + s*p2.y,
					s*p1.x + p2.x - s*p3.x, s*p1.y + p2.y - s*p3.y,
					p2.x, p2.y );
			g2.draw( cubicCurve );
			if ( yPoints[ j ] < 0 || yPoints[ j+1 ] < 0 )
				g2.setStroke( STROKE_DOUBLE_WIDTH );
			pTemp = p; p = p1; p1 = p2; p2 = p3; p3 = pTemp;
		}
	}
	
	// Helper properties for painting cubic curves
	/** Reference to the x coordinates of the chart points.
	 * Might contain negative values which indicates the point is a missing data,
	 * it is an estimate to make the chart better/more informative. Multiply this by -1 to get the proper data. 
	 * Segments which contain a point having negative value should be drawn with dash line/curve. */
	private int[] xPoints;
	/** Reference to the y coordinates of the chart points.
	 * Might contain negative values which indicates the point is a missing data,
	 * it is an estimate to make the chart better/more informative. Multiply this by -1 to get the proper data. 
	 * Segments which contain a point having negative value should be drawn with dash line/curve. */
	private int[] yPoints;
	
	/**
	 * Helper method for drawing cubic curves.<br>
	 * <code>xPoints</code> and <code>yPoints</code> must contain at least 2 points!
	 * @param i index of the curve segment
	 * @param p point to store the control point location
	 */
	private void getCP( final int i, final Point2D.Float p ) {
		int l;
		if ( i == 0 ) {
			p.setLocation( -0.5f * xPoints[ 0 ] + 1.5f * xPoints[ 1 ],
						   -0.5f * Math.abs( yPoints[ 0 ] ) + 1.5f * Math.abs( yPoints[ 1 ] ) );
		} else if ( i == ( l = xPoints.length ) + 1 ) {
			p.setLocation( -0.5f * xPoints[ l - 1 ] + 1.5f * xPoints[ l - 2 ] ,
						   -0.5f * Math.abs( yPoints[ l - 1 ] ) + 1.5f * Math.abs( yPoints[ l - 2 ] ) );
		}
		else
			p.setLocation( xPoints[ i - 1 ], Math.abs( yPoints[ i - 1 ] ) );
	}
	
}
