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

import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.ui.charts.BaseChartParams;
import hu.belicza.andras.sc2gears.ui.charts.ChartUtils.GraphApproximation;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ReplayAnalyzer.ChartType;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ReplayAnalyzer.MapBackground;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ReplayAnalyzer.MapViewQuality;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ReplayAnalyzer.ShowDuration;
import hu.belicza.andras.sc2gears.util.Producer;
import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;

/**
 * Common parameters for painting charts.
 * 
 * @author Andras Belicza
 */
public class ChartParams extends BaseChartParams implements Cloneable {
	
	/** X coordinate of the Y axis.         */
	public static final int Y_AXIS_X_CONST = 25;
	
	/** The frame where marker should be painted if not null.           */
	public Integer    markerFrame;
	/** X coordinate where marker should be painted if not null.        */
	public Integer    markerX;
	/** Reference to the selected action.                               */
	public Action     selectedAction;
	/** Visible rectangle of the chart canvas.                          */
	public Rectangle  visibleRectangle;
	/** Replay that is being charted.                                   */
	public Replay     replay; 
	/** Array of actions to be used as input data for charts.           */
	public Action[]   actions; 
	/** The type of the chart.                                          */
	public ChartType  chartType; 
	/** Tells if charts should be aggregated by teams.                  */
	public boolean    groupByTeam;
	/** Tells if all players should be displayed on 1 chart.            */
	public boolean    allPlayersOnOneChart;
	/** Tells if charts should be painted with players' in-game colors. */
	public boolean    usePlayersInGameColors;
	/** Tells if time information should be displayed in seconds.       */
	public boolean    displayInSeconds;
	/** Chart zoom value.                                               */
	public int        zoom;
	/** Grid parameters.                                                */
	public GridParams gridParams;
	
	// Chart type specific parameters
	/** Granularity of the APM chart.                      */
	public int                   apmGranularity;
	/** Approximation of the APM chart.                    */
	public GraphApproximation    apmApproximation;
	/** Show EAPM property.                                */
	public boolean               showEapm;
	/** Show micro/macro APM property.                     */
	public boolean               showMicroMacroApm;
	/** Show XAPM property.                                */
	public boolean               showXapm;
	/** Show select hotkeys property.                      */
	public boolean               showSelectHotkeys;
	/** Show builds property.                              */
	public boolean               showBuilds;
	/** Show trains property.                              */
	public boolean               showTrains;
	/** Show workers property.                             */
	public boolean               showWorkers;
	/** Show researches property.                          */
	public boolean               showResearches;
	/** Show upgrades property.                            */
	public boolean               showUpgrades;
	/** Show ability groups property.                      */
	public boolean               showAbilityGroups;
	/** Show duration property.                            */
	public ShowDuration          showDuration;
	/** Icon sizes property.                               */
	public IconSize              iconSizes;
	/** Show units stat property.                          */
	public boolean               showUnitsStat;
	/** Show buildings property.                           */
	public boolean               showBuildingsStat;
	/** Show researches stat property.                     */
	public boolean               showResearchesStat;
	/** Show upgrades stat property.                       */
	public boolean               showUpgradesStat;
	/** Show ability groups stat property.                 */
	public boolean               showAbilityGroupsStat;
	/** Bar size property.                                 */
	public IconSize              barSize;
	/** Show after completed property.                     */
	public boolean               showAfterCompleted;
	/** Map view quality property.                         */
	public MapViewQuality        mapViewQuality;
	/** Map background property.                           */
	public MapBackground         mapBackground;
	/** Area granularity property.                         */
	public int                   areaGranularityCount;
	/** Hide overlapped buildings property.                */
	public boolean               hideOverlappedBuildings;
	/** Fill building icons property.                      */
	public boolean               fillBuildingIcons;
	/** Show map objects property.                         */
	public boolean               showMapObjects;
	/** Show percent property.                             */
	public boolean               showPercent;
	/** Distribution bar size property.                    */
	public IconSize              distributionBarSize;
	/** Calculate until time marker property.              */
	public boolean               calculateUntilMarker;
	/** Granularity of the Unit Tiers chart.               */
	public int                   unitTiersGranularity;
	/** Stretch bars property.                             */
	public boolean               stretchBars;
	/** Granularity of the Resource Spending Rate chart.   */
	public int                   rsrGranularity;
	/** Approximation of the Resource Spending Rate chart. */
	public GraphApproximation    rsrApproximation;
	/** Resources spent cumulative.                        */
	public boolean               rsCumulative;
	/** Granularity of the resources spent chart.          */
	public int                   rsGranularity;
	/** Produced Army/Supply cumulative.                   */
	public boolean               pasCumulative;
	/** Granularity of the Produced Army/Supply chart.     */
	public int                   pasGranularity;
	/** Include initial units property.                    */
	public boolean               includeInitialUnits;
	/** Show percent property of the Red. distr. chart.    */
	public boolean               showRedPercent;
	/** Distribution bar size property of the R. d. chart. */
	public IconSize              redDistributionBarSize;
	/** Max frame break in action sequences.               */
	public int                   maxSequenceFrameBreak;
	/** Group the same productions property.               */
	public boolean               groupSameProductions;
	/** Icon sizes (Productions) property.                 */
	public IconSize              iconSizesP;
	/** Icon sizes (Player Selec.) property.               */
	public IconSize              iconSizesPS;
	
	/** The visible teams in case of group by teams. */
	public List< Integer > teamList = null;
	/** Indices of players belonging to the check boxes. This is the display order of the players. */
	public int[] playerIndices;
	/** Tells the chart index of a player, less than 0 if player is not visible (basically a player index to chart index map). */
	public int[] playerChartIndices; // Per player
	/** Tells the player index of a chart index (basically a chart index to player index map). */
	public int[] chartPlayerIndices; // Per chart
	
	// Derived data
	/** Number of visible charts.
	 * This is equal to playersCount if players are on different charts, and is equal to 1 if all players are on the same chart. */
	public int     chartsCount;
	/** Top boundaries of usable chart spaces.        */
	public int[]   chartY1s;     // Per chart
	/** Bottom boundaries of usable chart spaces.     */
	public int[]   chartY2s;     // Per chart
	/** Colors of players to be used to paint charts. */
	public Color[] playerColors; // Per player
	
	/**
	 * Creates a new ChartParams.
	 */
	public ChartParams() {
		super( Y_AXIS_X_CONST );
	}
	
	/**
	 * Specifies a tool tip provider which can define different tool tips for different points/locations.
	 * @author Andras Belicza
	 */
	static interface ToolTipProvider {
		/**
		 * Returns a tool tip for the specified point
		 * @param point point to return a tool tip for
		 * @return a tool tip for the specified point
		 */
		String getToolTip( Point point );
	}
	
	/**
	 * A deferred, location dependent tool tip provider.
	 * @param <V> the type of custom objects storing information to determine tool tips
	 * @author Andras Belicza
	 */
	static class DeferredToolTipProvider< V > implements ToolTipProvider {
		
		/** Reference to the tool tip data list.                              */
		private final List< Pair< Rectangle, V > > toolTipDataList;
		/** A producer which produces a string tool tip from a tool tip data. */
		private final Producer< V, String >        toolTipProducer;
		
		/**
		 * Creates a new ToolTipProvider.
		 * @param toolTipList     reference to the tool tip data list
		 * @param toolTipProducer producer which produces tool tips from a data
		 */
		public DeferredToolTipProvider( final List< Pair< Rectangle, V > > toolTipDataList, final Producer< V, String > toolTipProducer ) {
			this.toolTipDataList = toolTipDataList;
			this.toolTipProducer = toolTipProducer;
		}
		
		@Override
		public String getToolTip( final Point point ) {
			// Since most list is populated in a way that items displayed earlier are added earlier,
			// overlapping items that are visible will be at a higher index, so we get a better result if we
			// walk through the list backwards
			for ( int i = toolTipDataList.size() - 1; i >= 0; i-- )
				if ( toolTipDataList.get( i ).value1.contains( point ) )
					return toolTipProducer.produce( toolTipDataList.get( i ).value2 );
			
			return null;
		}
	}
	
	/**
	 * A static, location dependent tool tip provider.<br>
	 * It's static because the tool tip list is already determined at the time of constructor call.
	 * @author Andras Belicza
	 */
	static class StaticToolTipProvider extends DeferredToolTipProvider< String > {
		
		/**
		 * A producer that simply returns its input argument.
		 */
		private static Producer< String, String > RETURNER_PRODUCER = new Producer< String, String >() {
			@Override
			public String produce( final String input) {
				return input;
			}
		};
		
		/**
		 * Creates a new StaticToolTipProvider.
		 * @param toolTipList reference to the tool tip list
		 */
		public StaticToolTipProvider( final List< Pair< Rectangle, String > > toolTipList ) {
			super( toolTipList, RETURNER_PRODUCER );
		}
	}
	
	/** Optional tool tip provider for custom tool tip for different charts at different locations. */
	public ToolTipProvider toolTipProvider;
	
	/**
	 * Sets the visible players.
	 * @param playerCheckBoxes check boxes of the players
	 * @param playerIndices    indices of players belonging to the check boxes
	 */
	public void setVisiblePlayers( final JCheckBox[] playerCheckBoxes, final int[] playerIndices ) {
		this.playerIndices = playerIndices;
		
		int playersCount = 0;
		playerChartIndices = new int[ playerCheckBoxes.length ];
		if ( groupByTeam ) {
			teamList = new ArrayList< Integer >( playerCheckBoxes.length ); // We need this because players of an entire team might be disabled => whole teams might not be visible
			int lastTeam = -1;
			for ( int i = 0; i < playerIndices.length; i++ )
				if ( playerCheckBoxes[ i ].isSelected() ) {
					final int team = replay.details.players[ playerIndices[ i ] ].team;
					if ( team != lastTeam ) {
						teamList.add( team );
						lastTeam = team;
					}
				}
			
			for ( int i = 0; i < playerCheckBoxes.length; i++ )
				playerChartIndices[ playerIndices[ i ] ] = playerCheckBoxes[ i ].isSelected() ? teamList.indexOf( replay.details.players[ playerIndices[ i ] ].team ) : -1;
		}
		else {
			for ( int i = 0; i < playerCheckBoxes.length; i++ )
				playerChartIndices[ playerIndices[ i ] ] = playerCheckBoxes[ i ].isSelected() ? playersCount++ : -1;
		}
		
		chartPlayerIndices = new int[ groupByTeam ? teamList.size() : playersCount ];
		for ( int i = playerIndices.length - 1; i >= 0; i-- ) { // Downward, so in case of group by team, this will point to the first player in the team
			final int playerIndex = playerIndices[ i ];
			if ( playerChartIndices[ playerIndex ] >= 0 )
				chartPlayerIndices[ playerChartIndices[ playerIndex ] ] = playerIndex;
		}
	}
	
	/**
	 * Calculates the derived data.
	 * @param defaultPlayerColor default player color
	 */
	public void calcualteDerivedData( final Color defaultPlayerColor ) {
		calcualteDerivedData();
		
		chartsCount = allPlayersOnOneChart ? 1 : chartPlayerIndices.length;
		
		if ( chartPlayerIndices.length > 0 )
			chartHeight = ( height - chartsCount * ( CHART_TITLE_HEIGHT + X_AXIS_LABELS_HEIGHT ) ) / chartsCount;
		if ( chartHeight < 0 )
			chartHeight = 0;
		chartDY = chartHeight - 1;
		
		final int chartHeightWithDecoration = CHART_TITLE_HEIGHT + chartHeight + X_AXIS_LABELS_HEIGHT;
		
		chartY1s = new int[ chartPlayerIndices.length ];
		chartY2s = new int[ chartPlayerIndices.length ];
		for ( int i = chartPlayerIndices.length - 1; i >= 0; i-- ) {
			chartY1s[ i ] = ( allPlayersOnOneChart ? 0 : i * chartHeightWithDecoration ) + CHART_TITLE_HEIGHT;
			chartY2s[ i ] = chartY1s[ i ] + chartHeight - 1;
		}
		
		playerColors = new Color[ playerChartIndices.length ];
		for ( int i = 0; i < playerColors.length; i++ ) {
			if ( playerChartIndices[ i ] < 0 )
				playerColors[ i ] = null;
			else
				playerColors[ i ] = allPlayersOnOneChart || usePlayersInGameColors ? replay.details.players[ i ].getColor() : defaultPlayerColor;
		}
	}
	
	/**
	 * Converts an X coordinate to frame.
	 * @param x x coordinate to be converted to frame
	 * @return the frame for the x coordinate
	 */
	public int xToFrame( final int x ) {
		// chartDX is not zero (else we don't paint charts)
		return (int) ( (long) replay.frames * ( x - Y_AXIS_X_CONST + 1 ) / chartDX ); // There's real chance to overflow so we work with longs
	}
	
	/**
	 * Converts a frame number to X coordinate.
	 * @param frame frame number to be converted to x coordinate
	 * @return the x coordinate for the frame number
	 */
	public int frameToX( final int frame ) {
		// replay.frames is not zero (else we don't paint charts)
		return Y_AXIS_X_CONST + (int) ( (long) frame * chartDX / replay.frames ); // There's real chance to overflow so we work with longs
	}
	
	/**
	 * Scales and converts a coordinate into pixel coordinate. 
	 * @param mapCoord map coordinate to be scaled
	 * @return the coordinate scaled into pixel coordinate
	 */
	public int scaleMapCoord( final int mapCoord ) {
		// Should be divided by 65536, and multiplied by (zoom*2) which is equal to: / 32768 * zoom
		return ( mapCoord >>> 15 ) * zoom;
	}
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch ( final CloneNotSupportedException cnse ) {
			// Never to happen...
			cnse.printStackTrace();
		}
		
		return null;
	}
	
}
