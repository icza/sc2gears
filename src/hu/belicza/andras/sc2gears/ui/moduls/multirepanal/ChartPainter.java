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

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.EnumCache;
import hu.belicza.andras.sc2gears.ui.charts.BaseChartPainter;
import hu.belicza.andras.sc2gears.ui.charts.BaseChartParams;
import hu.belicza.andras.sc2gears.ui.charts.ChartUtils.GraphApproximation;
import hu.belicza.andras.sc2gears.ui.moduls.multirepanal.MultiRepAnalysis.ChartGranularity;
import hu.belicza.andras.sc2gears.ui.moduls.multirepanal.MultiRepAnalysis.ChartType;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gearspluginapi.api.enums.LadderSeason;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Format;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerColor;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * This class is responsible for painting the different charts.
 * 
 * @author Andras Belicza
 */
class ChartPainter extends BaseChartPainter {
	
	/**
	 * Y axis label position.
	 * @author Andras Belicza
	 */
	private static enum YAxisLabelPos {
		/** On the left of the Y axis.          */
		LEFT  ( 0 ),
		/** On the right of the Y axis.         */
		RIGHT ( ChartParams.Y_AXIS_X_CONST + 1 ),
		/** Hidden labels (do not draw labels). */
		HIDDEN( 0 );
		
		/** X coordinate of the labels associated with this AxisLabelPos. */
		public final int x;
		
        /**
         * Creates a new ChartPainter.AxisLabelPos.
         * @param x x coordinate of the labels associated with this AxisLabelPos
         */
        private YAxisLabelPos( final int x ) {
        	this.x = x;
        }
	}
	
	/**
	 * APM types displayed in the APM development chart.
	 * @author Andras Belicza
	 */
	private static enum ApmType {
		APM ( "module.multiRepAnal.tab.player.tab.charts.chartType.apmDevelopment.apm"  ),
		EAPM( "module.multiRepAnal.tab.player.tab.charts.chartType.apmDevelopment.eapm" );
		
		/** Cache of the string value. */
		public  final String stringValue;
		
		/**
		 * Creates a new ActionType.
		 * @param textKey key of the text representation
		 */
		private ApmType( final String textKey ) {
			stringValue = Language.getText( textKey );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Sub-charts displayed in the Spawn Larva development chart.
	 * @author Andras Belicza
	 */
	private static enum SpawnLarvaType {
		AVG_SPAWNING_RATIO( "module.multiRepAnal.tab.player.tab.charts.chartType.spawnLarvaDevelopment.avgSpawningRatio"  ),
		AVG_INJECTION_GAP ( "module.multiRepAnal.tab.player.tab.charts.chartType.spawnLarvaDevelopment.avgInjectionGap" );
		
		/** Cache of the string value. */
		public  final String stringValue;
		
		/**
		 * Creates a new ActionType.
		 * @param textKey key of the text representation
		 */
		private SpawnLarvaType( final String textKey ) {
			stringValue = Language.getText( textKey );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/** Color to be used if the proper map does not contain a color for a value. */
	private static final Color MISSING_COLOR = Color.DARK_GRAY;
	
	/** General chart color.         */
	private static final Color COLOR_GENERAL_CHART  = PlayerColor.RED  .color;
	/** General chart color #2.      */
	private static final Color COLOR_GENERAL_CHART2 = PlayerColor.GREEN.color;
	/** Color of the average marker. */
	private static final Color COLOR_AVERAGE_MARKER = new Color( 40, 100, 255 );
	
	/** Colors of the APM types.         */
	private static final Map< ApmType       , Color > APM_TYPE_COLOR_MAP         = new EnumMap< ApmType        , Color >( ApmType       .class );
	/** Colors of the Spawn Larva types. */
	private static final Map< SpawnLarvaType, Color > SPAWN_LARVA_TYPE_COLOR_MAP = new EnumMap< SpawnLarvaType , Color >( SpawnLarvaType.class );
	/** Colors of the races.             */
	private static final Map< Race          , Color > RACE_COLOR_MAP             = new EnumMap< Race           , Color >( Race          .class );
	/** Colors of the game types.        */
	private static final Map< GameType      , Color > GAME_TYPE_COLOR_MAP        = new EnumMap< GameType       , Color >( GameType      .class );
	/** Colors of the formats.           */
	private static final Map< Format        , Color > FORMAT_COLOR_MAP           = new EnumMap< Format         , Color >( Format        .class );
	static {
		APM_TYPE_COLOR_MAP.put( ApmType.APM , COLOR_GENERAL_CHART  );
		APM_TYPE_COLOR_MAP.put( ApmType.EAPM, COLOR_GENERAL_CHART2 );
		
		SPAWN_LARVA_TYPE_COLOR_MAP.put( SpawnLarvaType.AVG_SPAWNING_RATIO, COLOR_GENERAL_CHART  );
		SPAWN_LARVA_TYPE_COLOR_MAP.put( SpawnLarvaType.AVG_INJECTION_GAP , COLOR_GENERAL_CHART2 );
		
		RACE_COLOR_MAP.put( Race.PROTOSS, PlayerColor.BLUE  .color );
		RACE_COLOR_MAP.put( Race.TERRAN , PlayerColor.GREEN .color );
		RACE_COLOR_MAP.put( Race.ZERG   , PlayerColor.ORANGE.color );
		RACE_COLOR_MAP.put( Race.UNKNOWN, Color.LIGHT_GRAY         );
		
		GAME_TYPE_COLOR_MAP.put( GameType.AMM          , PlayerColor.GREEN     .color );
		GAME_TYPE_COLOR_MAP.put( GameType.PRIVATE      , PlayerColor.BLUE      .color );
		GAME_TYPE_COLOR_MAP.put( GameType.PUBLIC       , PlayerColor.ORANGE    .color );
		GAME_TYPE_COLOR_MAP.put( GameType.SINGLE_PLAYER, PlayerColor.LIGHT_PINK.color );
		GAME_TYPE_COLOR_MAP.put( GameType.UNKNOWN      , Color.LIGHT_GRAY             );
		
		FORMAT_COLOR_MAP.put( Format.ONE_VS_ONE    , PlayerColor.GREEN      .color );
		FORMAT_COLOR_MAP.put( Format.TWO_VS_TWO    , PlayerColor.BLUE       .color );
		FORMAT_COLOR_MAP.put( Format.THREE_VS_THREE, PlayerColor.ORANGE     .color );
		FORMAT_COLOR_MAP.put( Format.FOUR_VS_FOUR  , PlayerColor.LIGHT_PINK .color );
		FORMAT_COLOR_MAP.put( Format.FREE_FOR_ALL  , PlayerColor.TEAL       .color );
		FORMAT_COLOR_MAP.put( Format.CUSTOM        , PlayerColor.LIGHT_GREEN.color );
		FORMAT_COLOR_MAP.put( Format.UNKNOWN       , Color.LIGHT_GRAY             );
	}
	
	/** Common parameters for painting charts.    */
	protected final ChartParams params;
	
	/** X coordinate of the end of the y axis label. */
	private int yAxisLabelEndPos;
	
	/**
	 * Creates a new ChartPainter.
	 * @param chartParams parameters for painting charts
	 */
	public ChartPainter( final ChartParams chartParams ) {
		super( chartParams );
		this.params = chartParams;
		
		params.calcualteDerivedData();
	}
	
	@Override
	public void paintChart() {
		super.paintChart();
		
		if ( params.chartDX <= 0 )
			return;
		
		paintAxis();
		
		switch ( params.chartType ) {
		case ACTIVITY                : paintActivityChart             (); break;
		case APM_DEVELOPMENT         : paintApmDevelopmentChart       (); break;
		case WIN_RATIO_DEVELOPMENT   : paintWinRatioDevelopmentChart  (); break;
		case SPAWN_LARVA_DEVELOPMENT : paintSpawnLarvaDevelopmentChart(); break;
		case RACE_DISTRIBUTION       : case GAME_TYPE_DISTRIBUTION : case FORMAT_DISTRIBUTION : paintDistributionChart(); break;
		}
	}
	
	/**
	 * Paints the axis of the charts, and the time (x) axis labels.
	 */
	private void paintAxis() {
		g2.setColor( COLOR_AXIS );
		
		final Font oldFont = g2.getFont();
		g2.setFont( new Font( oldFont.getName(), Font.BOLD, oldFont.getSize() ) );
		final String yAxisLabel = Language.getText( params.chartType.yAxisLabelKey, params.chartGranularity );
		g2.drawString( yAxisLabel, ChartParams.Y_AXIS_X_CONST + 1, g2.getFontMetrics().getAscent() - 2 );
		yAxisLabelEndPos = ChartParams.Y_AXIS_X_CONST + 1 + g2.getFontMetrics().stringWidth( yAxisLabel );
		g2.setFont( oldFont );
		
		g2.drawLine( ChartParams.Y_AXIS_X_CONST, params.chartY1, ChartParams.Y_AXIS_X_CONST, params.chartY2 );
		g2.drawLine( ChartParams.Y_AXIS_X_CONST, params.chartY2 + 1, params.chartX2, params.chartY2 + 1 );
		
		// Time axis labels
		final int maxIndex = params.segmentStats.length - 1;
		final int fontAscent = g2.getFontMetrics().getAscent();
		final int maxLabelsCount = params.chartWidth < TIME_LABELS_MIN_DISTANCE ? 1 : params.chartWidth / TIME_LABELS_MIN_DISTANCE;
		int step;
		if ( maxLabelsCount == 1 )
			step = maxIndex;
		else {
			step = 1;
			int labelsCount = maxIndex;
			while ( labelsCount > maxLabelsCount )
				labelsCount = maxIndex / ++step;
		}
		for ( int j = maxIndex; j >= 0; j -= step ) {
			if ( j - step < 0 ) // If there's no space for the first label, skip the 2nd and display the first
				j = 0;
			final int    x     = params.chartX1 + j * params.chartDX / maxIndex;
			final String label = params.chartGranularity == ChartGranularity.LADDER_SEASON ? LadderSeason.getByStartDate( params.segmentStats[ j ].lowerDate ).stringValue
					: Language.formatDate( params.segmentStats[ j ].lowerDate );
			g2.setColor( COLOR_AXIS );
			g2.drawLine( x, params.chartY2 + 1, x, params.chartY2 + 3 ); // Marker on axis
			g2.setColor( COLOR_AXIS_LABELS );
			g2.drawString( label, x - ( g2.getFontMetrics().stringWidth( label ) >> ( j == maxIndex ? 0 : j == 0 ? 2 : 1 ) ), params.chartY2 + fontAscent );
			if ( j > 0 ) {
				// Vertical assist line
				g2.setColor( COLOR_ASSIST_LINES );
				g2.setStroke( STROKE_DASHED );
				g2.drawLine( x, params.chartY1, x, params.chartY2 );
				g2.setStroke( STROKE_DEFAULT );
			}
		}
	}
	
	/**
	 * Paints the APM chart.
	 */
	private void paintActivityChart() {
		final ChartSegmentStatistics[] segmentStats = params.segmentStats;
		
		final int[] yPoints = new int[ segmentStats.length ];
		for ( int i = yPoints.length - 1; i >= 0; i-- ) {
			final int totalGames = segmentStats[ i ].record.totalGames;
			yPoints[ i ] = totalGames;
		}
		
		float avgActivity;
		if ( params.chartGranularity == ChartGranularity.LADDER_SEASON ) { 
			// How many segments (seasons)?
			int nonZeroSegmentsCount = 0;
			for ( final ChartSegmentStatistics segmentStat : segmentStats )
				if ( segmentStat.record.totalGames > 0 )
					nonZeroSegmentsCount++;
			
			// if there's only 1 non-zero segment => only 1 season, the rest were just "beautifier addons"
			int seasonsCount = nonZeroSegmentsCount == 1 ? 1 : segmentStats.length == 0 ? 1 : segmentStats.length;
			avgActivity = params.playerStatistics.record.totalGames / seasonsCount;
		}
		else {
			avgActivity = params.playerStatistics.getAvgGamesPerDay();
			switch ( params.chartGranularity ) {
			case WEEK  : avgActivity *= 7; break;
			case MONTH : avgActivity *= 365.25f / 12; break;
			case YEAR  : avgActivity *= 365.25f; break;
			}
		}
		
		paintGraphChart( yPoints, avgActivity, YAxisLabelPos.LEFT, null, 0 );
	}
	
	/**
	 * Paints the APM development chart.
	 */
	private void paintApmDevelopmentChart() {
		final ChartSegmentStatistics[] segmentStats = params.segmentStats;
		
		final int[] yApmPoints  = new int[ segmentStats.length ];
		final int[] yEapmPoints = new int[ segmentStats.length ];
		for ( int i = yApmPoints.length - 1; i >= 0; i-- ) {
			yApmPoints [ i ] = segmentStats[ i ].getAvgApm ();
			yEapmPoints[ i ] = segmentStats[ i ].getAvgEapm();
		}
		
		final int referenceMaxValue = GeneralUtils.maxValue( yApmPoints );
		// Draw APM first (else the assist lines of the APM chart would overlap the EAPM chart)
		paintGraphChart( yApmPoints , params.playerStatistics.getAvgApm (), YAxisLabelPos.LEFT  , null             , 0 );
		paintGraphChart( yEapmPoints, params.playerStatistics.getAvgEapm(), YAxisLabelPos.HIDDEN, referenceMaxValue, 0 );
		
		// Draw legend
		paintChartLegend( ApmType.values(), APM_TYPE_COLOR_MAP );
	}
	
	/**
	 * Paints a distribution chart.
	 */
	private void paintDistributionChart() {
		final int                                iType, assistLinesCount; 
		final Enum< ? extends Enum< ? > >[]      values;
		final Map< ? extends Enum< ? > , Color > valueColorMap;
		
		switch ( params.chartType ) {
		case RACE_DISTRIBUTION      : iType = 0; assistLinesCount = 3; values = EnumCache.RACES     ; valueColorMap = RACE_COLOR_MAP     ; break;
		case GAME_TYPE_DISTRIBUTION : iType = 1; assistLinesCount = 4; values = EnumCache.GAME_TYPES; valueColorMap = GAME_TYPE_COLOR_MAP; break;
		case FORMAT_DISTRIBUTION    : iType = 2; assistLinesCount = 4; values = EnumCache.FORMATS   ; valueColorMap = FORMAT_COLOR_MAP   ; break;
		default : throw new RuntimeException( "Invalid type!" );
		}
		
		final ChartSegmentStatistics[] segmentStats = params.segmentStats;
		
		if ( params.chartWidth < 3 * segmentStats.length ) {
			// Bars width would be less than 1 pixel wide
			g2.setColor( Color.WHITE );
			final Font savedFont = g2.getFont();
			g2.setFont( savedFont.deriveFont( Font.BOLD ) );
			for ( int i = 0; i < 2; i++ ) { // 2 info lines
				final String message = Language.getText( i == 0 ? "module.multiRepAnal.tab.player.tab.charts.notEnoughSpace" : "module.multiRepAnal.tab.player.tab.charts.notEnoughSpaceTip" );
				int x = params.chartX1 + ( params.chartDX - g2.getFontMetrics().stringWidth( message ) ) / 2;
				if ( x < ChartParams.Y_AXIS_X_CONST + 3 )
					x = ChartParams.Y_AXIS_X_CONST + 3;
				g2.drawString( message, x, params.chartY1 + params.chartDY / 2 - ( i == 0 ? g2.getFontMetrics().getHeight() : 0 ) );
			}
			g2.setFont( savedFont );
			return;
		}
		
		// Draw assist lines
		final int fontAscent = g2.getFontMetrics().getAscent();
		g2.setStroke( STROKE_DASHED );
		for ( int j = assistLinesCount - 1; j >= 0; j-- ) {
			final int y = params.chartY1 + j * params.chartDY / assistLinesCount;
			g2.setColor( COLOR_ASSIST_LINES );
			g2.drawLine( params.chartX1 - 5, y, params.chartX2, y );
			g2.setColor( COLOR_AXIS_LABELS );
			final int value = ( assistLinesCount - j ) * 100 / assistLinesCount;
			g2.drawString( Integer.toString( value ), 0, y + ( fontAscent >> 1 ) - 1 );
		}
		g2.setStroke( STROKE_DEFAULT );
		
		// Draw the distribution bars
		final IntHolder[] counters = new IntHolder[ values.length ];
		final int         barWidth = ( params.chartWidth - segmentStats.length - 3 ) / segmentStats.length;
		for ( int i = segmentStats.length - 1; i >= 0; i-- ) {
			final Map< ? extends Enum< ? >, IntHolder > distributionMap;
			switch ( iType ) {
			case 0 : distributionMap = segmentStats[ i ].raceDistributionMap    ; break;
			case 1 : distributionMap = segmentStats[ i ].gameTypeDistributionMap; break;
			case 2 : distributionMap = segmentStats[ i ].formatDistributionMap  ; break;
			default: throw new RuntimeException( "Invalid type!" );
			}
			for ( int j = values.length-1; j >= 0; j-- )
				counters[ j ] = distributionMap.get( values[ j ] );
			int sum = 0;
			for ( final IntHolder counter : counters )
				if ( counter != null )
					sum += counter.value;
			
			if ( sum == 0 )
				continue;
			
			final int x = 2 + params.chartX1 + i * params.chartDX / segmentStats.length;
			
			int partialSum = 0;
			int y = params.chartY1;
			IntHolder counter;
			for ( int j = counters.length - 1; j >= 0; j-- )
				if ( ( counter = counters[ j ] ) != null ) {
					final Color color = valueColorMap.get( values[ j ] );
					g2.setColor( color == null ? MISSING_COLOR : color );
					partialSum += counter.value;
					final int y2 = params.chartY1 + partialSum * params.chartHeight / sum;
					g2.fillRect( x, y, barWidth, y2 - y );
					y = y2;
				}
		}
		
		// Draw legend
		paintChartLegend( values, valueColorMap );
	}
	
	/**
	 * Paints the Win ratio development chart.
	 */
	private void paintWinRatioDevelopmentChart() {
		final ChartSegmentStatistics[] segmentStats = params.segmentStats;
		
		final int[] yPoints = new int[ segmentStats.length ];
		for ( int i = yPoints.length - 1; i >= 0; i-- ) {
			final Integer percent = segmentStats[ i ].record.getWinRatio().value;
			yPoints[ i ] = percent == null ? -1 : percent;
		}
		
		final Integer winRatio = params.playerStatistics.record.getWinRatio().value;
		paintGraphChart( yPoints, winRatio == null ? 0 : winRatio, YAxisLabelPos.LEFT, null, 0 );
	}
	
	/**
	 * Paints the Spawn Larva development chart.
	 */
	private void paintSpawnLarvaDevelopmentChart() {
		final ChartSegmentStatistics[] segmentStats = params.segmentStats;
		
		// To calculate averages:
		long totalHatchTime         = 0l;
		long totalHatchSpawnTime    = 0l;
		long totalInjectionGap      = 0l;
		int  totalInjectionGapCount = 0;
		
		final int[] yAvgSpawnRatioPoints   = new int[ segmentStats.length ];
		final int[] yAvgInjectionGapPoints = new int[ segmentStats.length ];
		for ( int i = yAvgSpawnRatioPoints.length - 1; i >= 0; i-- ) {
			final ChartSegmentStatistics segmentStat = segmentStats[ i ];
			
			yAvgSpawnRatioPoints  [ i ] = segmentStat.getAvgSpawningRatio();
			yAvgInjectionGapPoints[ i ] = segmentStat.getAvgInjectionGap ();
			
			totalHatchTime         += segmentStat.totalHatchTime;
			totalHatchSpawnTime    += segmentStat.totalHatchSpawnTime;
			totalInjectionGap      += segmentStat.totalInjectionGap;
			totalInjectionGapCount += segmentStat.totalInjectionGapCount;
		}
		
		final float avgSpawningRatio = totalHatchTime == 0 ? -1 : (int) ( totalHatchSpawnTime * 100L / totalHatchTime );
		final float avgInjectionGap  = totalInjectionGapCount == 0 ? -1 : (int) ( 10L * totalInjectionGap / totalInjectionGapCount / ReplayConsts.FRAMES_IN_SECOND );
		
		if ( avgSpawningRatio >= 0 )
			paintGraphChart( yAvgSpawnRatioPoints  , avgSpawningRatio, YAxisLabelPos.LEFT , null             , 0 );
		if ( avgInjectionGap >= 0 ) {
    		final int referenceMaxValue = GeneralUtils.maxValue( yAvgInjectionGapPoints ); // Passing this will tell paintGraphChart() to use secondary chart color!
    		paintGraphChart( yAvgInjectionGapPoints, avgInjectionGap, YAxisLabelPos.RIGHT , referenceMaxValue, 1 );
		}
		
		// Draw legend
		paintChartLegend( SpawnLarvaType.values(), SPAWN_LARVA_TYPE_COLOR_MAP );
	}
	
	/**
	 * Paints a graph chart specified by the data values
	 * @param yPoints            data values of the graph
	 * @param avgValue           average value to put the marker on
	 * @param yAxisLabelPos      tells where to draw Y axis labels
	 * @param referenceMaxValue  if provided, chart will be scaled to this max value
	 * @param fractionDigitCount if greater than 0, it tells the number of fraction digits included in values which needs to be displayed in the axis labels
	 */
	private void paintGraphChart( final int[] yPoints, final float avgValue, final YAxisLabelPos yAxisLabelPos, final Integer referenceMaxValue, final int fractionDigitCount ) {
		// Normalize y points
		final int maxValue_ = referenceMaxValue == null ? GeneralUtils.maxValue( yPoints ) : referenceMaxValue; // Max value might be 0!!
		final int maxValue  = maxValue_ == 0 ? 1 : maxValue_;
		for ( int i = 0; i < yPoints.length; i++ ) {
			final int end = yPoints[ i ] = yPoints[ i ] < 0 ? -1 : params.chartY2 - params.chartHeight * yPoints[ i ] / maxValue;
			// Fill missing points with linear interpolation
			if ( end > 0 ) {
				int j = i - 1;
				while ( j >= 0 && yPoints[ j ] < 0 )
					j--;
				if ( i > j + 1 ) {
					if ( j < 0 )
						Arrays.fill( yPoints, j+1, i, -end ); // Estimation is the first known point
					else {
						final int start = yPoints[ j ];
						for ( int j2 = j + 1; j2 < i; j2++ )
							yPoints[ j2 ] = -( start + ( end - start ) * ( j2 - j ) / ( i - j ) );
					}
				}
			}
		}
		// Finish filling the missing points at the end
		if ( yPoints[ yPoints.length - 1 ] < 0 ) {
			int j = yPoints.length - 2;
			while ( j >= 0 && yPoints[ j ] < 0 )
				j--;
			if ( j < 0 )
				Arrays.fill( yPoints, -params.chartY2 ); // The whole chart is missing
			else
				Arrays.fill( yPoints, j + 1, yPoints.length, -yPoints[ j ] ); // Estimation is the last known point
		}
		
		final int fontAscent = g2.getFontMetrics().getAscent();
		
		final float valueDivider = 10 * fractionDigitCount;
		
		if ( yAxisLabelPos != YAxisLabelPos.HIDDEN ) {
    		// Draw assist lines
    		final int assistLinesCount = params.chartHeight < ASSIST_LINES_MIN_DISTANCE ? 1 : params.chartHeight / ASSIST_LINES_MIN_DISTANCE;
    		g2.setStroke( STROKE_DASHED );
    		for ( int j = assistLinesCount - 1; j >= 0; j-- ) {
    			final int y = params.chartY1 + j * params.chartDY / assistLinesCount;
    			g2.setColor( COLOR_ASSIST_LINES );
    			g2.drawLine( params.chartX1 + 1, y, params.chartX2, y );
    			g2.setColor( COLOR_AXIS_LABELS );
    			final int value = ( assistLinesCount - j ) * maxValue / assistLinesCount;
    			g2.drawString( fractionDigitCount == 0 ? Integer.toString( value ) : String.format( Locale.US, "%.1f", value / valueDivider ), yAxisLabelPos.x, y + ( fontAscent >> 1 ) - 1 );
    		}
    		g2.setStroke( STROKE_DEFAULT );
    		
		}
		
		// Average marker
		g2.setColor( COLOR_AVERAGE_MARKER );
		final int avgY = params.chartY2 - (int) ( params.chartDY * avgValue / maxValue );
		g2.drawLine( params.chartX1 + 1, avgY, params.chartX2, avgY );
		
		// And the graph
		g2.setColor( referenceMaxValue == null ? COLOR_GENERAL_CHART : COLOR_GENERAL_CHART2 );
		g2.setStroke( params.graphApproximation == GraphApproximation.LINEAR ? STROKE_DOUBLE_WIDTH_ROUNDED : STROKE_DOUBLE_WIDTH );
		final int maxPointIndex = yPoints.length - 1;
		switch ( params.graphApproximation ) {
		case LINEAR : {
			int x = params.chartX2, lastX;
			for ( int i = maxPointIndex - 1; i >= 0; i-- ) {
				lastX = x;
				x = params.chartX1 + i * params.chartDX / maxPointIndex;
				if ( yPoints[ i ] < 0 || yPoints[ i+1 ] < 0 ) {
					// Missing chart data, draw interpolation as estimates
					g2.setStroke( STROKE_DASHED_DOUBLE );
					g2.drawLine( x, Math.abs( yPoints[ i ] ), lastX, Math.abs( yPoints[ i+1 ] ) );
					g2.setStroke( STROKE_DOUBLE_WIDTH_ROUNDED );
				}
				else
					g2.drawLine( x, yPoints[ i ], lastX, yPoints[ i+1 ] );
			}
			break;
		}
		case CUBIC : {
			// Cubic curve might go outside the chart area, clip it!
			final Rectangle oldClipBounds = g2.getClipBounds();
			if ( oldClipBounds == null )
				g2.setClip( params.chartX1, params.chartY1, params.chartWidth, params.chartHeight );
			else
				g2.setClip( oldClipBounds.intersection( new Rectangle( params.chartX1, params.chartY1, params.chartWidth, params.chartHeight ) ) );
			final int[] xPoints = new int[ yPoints.length ];
			for ( int i = maxPointIndex; i >= 0; i-- )
				xPoints[ i ] = params.chartX1 + i * params.chartDX / maxPointIndex;
			drawCubicCurve2D( xPoints, yPoints, 0, maxPointIndex );
			g2.setClip( oldClipBounds );
			break;
		}
		}
		g2.setStroke( STROKE_DEFAULT );
		
		// Avg value
		g2.setColor( COLOR_AVERAGE_MARKER );
		final Font oldFont = g2.getFont();
		g2.setFont( new Font( oldFont.getName(), Font.BOLD, oldFont.getSize() ) );
		Object avgValueObject;
		if ( params.chartType == ChartType.ACTIVITY )
			avgValueObject = String.format( Locale.US, "%.3f", avgValue );
		else if ( params.chartType == ChartType.SPAWN_LARVA_DEVELOPMENT && yAxisLabelPos == YAxisLabelPos.RIGHT )
			avgValueObject = String.format( Locale.US, "%.1f", fractionDigitCount == 0 ? avgValue : avgValue / valueDivider );
		else
			avgValueObject = (int) avgValue;
		final String avgString = Language.getText( "module.multiRepAnal.tab.player.tab.charts.averageMarker", avgValueObject );
		g2.drawString( avgString, params.chartX2 - g2.getFontMetrics().stringWidth( avgString ) - 5, referenceMaxValue == null ? avgY - 3 : avgY + fontAscent );
		g2.setFont( oldFont );
	}
	
	/**
	 * Paints a chart legend: filled (rounded) rectangles with the colors of the entities associated with the names of the entities.
	 * @param values        values to be painted in the legend
	 * @param valueColorMap colors of the values (mapped from the values)
	 */
	private void paintChartLegend( final Enum< ? extends Enum< ? > >[] values, final Map< ? extends Enum< ? > , Color > valueColorMap ) {
		// Draw legend
		final Font oldFont = g2.getFont();
		g2.setFont( new Font( oldFont.getName(), Font.BOLD, oldFont.getSize() ) );
		int x = yAxisLabelEndPos + 15;
		final int legendSampleSize = BaseChartParams.CHART_TITLE_HEIGHT - 2;
		for ( final Enum< ? extends Enum< ? > > value : values ) {
			final Color color = valueColorMap.get( value );
			if ( color == null )
				continue;
			g2.setColor( color );
			g2.fillRoundRect( x, 1, legendSampleSize, legendSampleSize, legendSampleSize >> 1, legendSampleSize >> 1 );
			x += BaseChartParams.CHART_TITLE_HEIGHT;
			g2.setColor( COLOR_AXIS );
			final String text = value.toString();
			g2.drawString( text, x + 1, g2.getFontMetrics().getAscent() - 2 );
			x += g2.getFontMetrics().stringWidth( text ) + 15;
		}
		g2.setFont( oldFont );
	}
	
}
