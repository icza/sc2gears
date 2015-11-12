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

import hu.belicza.andras.sc2gears.ui.charts.BaseChartPainter;
import hu.belicza.andras.sc2gears.util.GeneralUtils;

import java.awt.Color;

/**
 * This class is responsible for painting the different activity trend charts.
 * 
 * @author Andras Belicza
 */
class TrendPainter extends BaseChartPainter {
	
	/** Color of the bars. */
	private static final Color COLOR_BAR  = new Color( 200, 200, 200 );
	
	/** Common parameters for painting charts.    */
	protected final TrendParams params;
	
	/**
	 * Creates a new TrendPainter.
	 * @param trendParams common parameters for painting charts
	 */
	public TrendPainter( final TrendParams trendParams ) {
		super( trendParams );
		this.params = trendParams;
		
		params.calcualteDerivedData();
	}
	
	@Override
	public void paintChart() {
		super.paintChart();
		
		if ( params.chartDX <= 0 )
			return;
		
		final int maxActivity = GeneralUtils.maxValue( params.activityData );
		
		g2.setColor( COLOR_AXIS );
		g2.drawLine( TrendParams.Y_AXIS_X_CONST, params.chartY2 + 1, params.chartX2, params.chartY2 + 1 );
		
		final int fontAscent = g2.getFontMetrics().getAscent();
		final String[] labels = params.trendType.labels;
		for ( int i = 0; i < labels.length; i++ ) {
			final int x       = i * params.barWidth + ( ( i - 1 ) << 1 ) + 3; // 2 pixels between bars
			final int centerX = x + ( params.barWidth >> 1 );
			
			// Paint marker on axis
			g2.setColor( COLOR_AXIS );
			g2.drawLine( centerX, params.chartY2 + 1, centerX, params.chartY2 + 3 );
			// Paint axis label
			g2.setColor( COLOR_AXIS_LABELS );
			g2.drawString( labels[ i ], centerX - ( g2.getFontMetrics().stringWidth( labels[ i ] ) >> 1 ), params.chartY2 + fontAscent );
			
			// Paint activity bar
			g2.setColor( COLOR_BAR );
			final int height = params.activityData[ i ] * params.chartHeight / maxActivity;
			if ( height > 0 ) // Do not call fill3DRect() if it's 0, because it renders a non-zero bar 
				g2.fill3DRect( x, params.chartY2 - height + 1, params.barWidth, height, true );
			// Display activity value
			g2.setColor( COLOR_PLAYER_DEFAULT );
			final String value = Integer.toString( params.activityData[ i ] );
			g2.drawString( value, centerX - ( g2.getFontMetrics().stringWidth( value ) >> 1 ), params.chartY2 - height - 1 );
		}
	}
	
}
