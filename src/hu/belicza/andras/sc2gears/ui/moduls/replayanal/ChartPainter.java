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

import static hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.FRAME_BITS_IN_SECOND;
import static hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.LARVA_SPAWNING_DURATION;
import static hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.CHRONO_BOOST_DURATION;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2map.MapParser;
import hu.belicza.andras.sc2gears.sc2replay.AbilityCodes;
import hu.belicza.andras.sc2gears.sc2replay.EapmUtils;
import hu.belicza.andras.sc2gears.sc2replay.PlayerSelectionTracker;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.EapmUtils.IneffectiveReason;
import hu.belicza.andras.sc2gears.sc2replay.model.MapInfo;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.BaseUseAbilityAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.BuildAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.HotkeyAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.LeaveGameAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.MoveScreenAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.ResearchAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.SelectAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.TrainAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.TrainHallucinatedAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.UpgradeAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.UseBuildingAbilityAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.UseUnitAbilityAction;
import hu.belicza.andras.sc2gears.ui.charts.BaseChartPainter;
import hu.belicza.andras.sc2gears.ui.charts.BaseChartParams;
import hu.belicza.andras.sc2gears.ui.charts.ChartUtils.GraphApproximation;
import hu.belicza.andras.sc2gears.ui.icons.IconHandler;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ChartPainter.MainBuildingUse.UseOnBuildingHistory;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ChartParams.DeferredToolTipProvider;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ChartParams.StaticToolTipProvider;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.GridParams.TimeUnit;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ReplayAnalyzer.ChartType;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ReplayAnalyzer.MapBackground;
import hu.belicza.andras.sc2gears.ui.moduls.replayanal.ReplayAnalyzer.ShowDuration;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.Producer;
import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.EntityParams;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.AbilityGroup;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BuildingAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.MapObject;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerColor;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Research;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitTier;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Upgrade;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * This class is responsible for painting the different charts.
 * 
 * @author Andras Belicza
 */
class ChartPainter extends BaseChartPainter {
	
	/** String values of the numbers used for hotkeys. */
	private static final String[] NUMBER_STRINGS = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	
	/** Color of the chart marker. */
	private static final Color COLOR_CHART_MARKER = new Color( 150, 150, 255 );
	/** Color of the grid.         */
	private static final Color COLOR_GRID         = COLOR_AXIS;
	
	/** Colors of the map objects. */
	private static final Map< MapObject, Color > MAP_OBJECT_COLOR_MAP = new EnumMap< MapObject, Color >( MapObject.class );
	static {
		MAP_OBJECT_COLOR_MAP.put( MapObject.MINERAL_FIELD                   , new Color( 130, 130, 255 ) );
		MAP_OBJECT_COLOR_MAP.put( MapObject.RICH_MINERAL_FIELD              , new Color( 180, 180, 100 ) );
		MAP_OBJECT_COLOR_MAP.put( MapObject.VESPENE_GEYSER                  , new Color( 100, 220, 100 ) );
		MAP_OBJECT_COLOR_MAP.put( MapObject.SPACE_PLATFORM_GEYSER           , new Color( 100, 220, 100 ) );
		MAP_OBJECT_COLOR_MAP.put( MapObject.XEL_NAGA_TOWER                  , new Color( 255, 255, 255 ) );
		MAP_OBJECT_COLOR_MAP.put( MapObject.DESTRUCTIBLE_ROCK_4X4           , new Color(  92,  51,  23 ) );
		MAP_OBJECT_COLOR_MAP.put( MapObject.DESTRUCTIBLE_ROCK_6X6           , new Color(  92,  51,  23 ) );
		MAP_OBJECT_COLOR_MAP.put( MapObject.DESTRUCTIBLE_ROCK_2X6_VERTICAL  , new Color(  92,  51,  23 ) );
		MAP_OBJECT_COLOR_MAP.put( MapObject.DESTRUCTIBLE_ROCK_2X6_HORIZONTAL, new Color(  92,  51,  23 ) );
		MAP_OBJECT_COLOR_MAP.put( MapObject.DESTRUCTIBLE_DEBRIS_4X4         , new Color(  92,  51,  23 ) );
		MAP_OBJECT_COLOR_MAP.put( MapObject.DESTRUCTIBLE_DEBRIS_6X6         , new Color(  92,  51,  23 ) );
	}
	
	/**
	 * Action category.
	 * @author Andras Belicza
	 */
	private static enum ActionCategory {
		SELECT         ( "fugue/selection-select.png"              ),
		HOTKEY         ( "fugue/keyboard.png"                      ),
		TRAIN          ( "fugue/target.png"                        ), // Excluding train hallucination
		BUILD          ( "fugue/building.png"                      ),
		RESEARCH       ( "fugue/flask.png"                         ),
		UPGRADE        ( "fugue/hammer-screwdriver.png"            ),
		RIGHT_CLICK    ( "fugue/mouse-select-right.png"            ),
		STOP           ( ReplayConsts.AbilityGroup.STOP            ),
		HOLD_POSITION  ( ReplayConsts.AbilityGroup.HOLD_POSITION   ),
		MOVE           ( ReplayConsts.AbilityGroup.MOVE            ),
		PATROL         ( ReplayConsts.AbilityGroup.PATROL          ),
		ATTACK         ( ReplayConsts.AbilityGroup.ATTACK          ), // Including attack structure
		SET_RALLY_POINT( ReplayConsts.AbilityGroup.SET_RALLY_POINT ), // Including set worker rally point
		CANCEL         ( ReplayConsts.AbilityGroup.CANCEL          ),
		OTHER          ( "fugue/universal.png" );
		
		/** Cache of the string value.                                                                                      */
		public final String stringValue;
		/** Entity the action category to function as an icon source through {@link Icons#getEntityIcon(Object, IconSize)}. */
		public final Object iconEntity;
		
		/**
		 * Creates a new ActionCategory.
		 * @param iconEntity entity the action category to function as an icon source
		 */
		private ActionCategory( final Object iconEntity ) {
			this.stringValue = ReplayConsts.convertConstNameToNormal( name(), false );
			this.iconEntity  = iconEntity;
		}
		
		/**
		 * Returns the icon of the action category.
		 * @param size size in which the icon to be returned
		 * @return
		 */
		public Icon getIcon( final IconSize size ) {
			return Icons.getEntityIcon( iconEntity, size );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	
	/** Builders for the chart descriptions. */
	private final StringBuilder[] descriptionBuilders; // Per chart
	
	/** Common parameters for painting charts.    */
	protected final ChartParams params;
	
	/**
	 * Creates a new ChartPainter.
	 * @param chartParams parameters for painting charts
	 */
	public ChartPainter( final ChartParams chartParams ) {
		super( chartParams );
		this.params = chartParams;
		
		params.calcualteDerivedData( COLOR_PLAYER_DEFAULT );
		
		descriptionBuilders = new StringBuilder[ params.chartPlayerIndices.length ];
	}
	
	@Override
	public void paintChart() {
		super.paintChart();
		
		if ( params.chartPlayerIndices.length == 0 || params.chartDX <= 0 || params.replay.frames == 0 )
			return;
		
		// Set the common chart descriptions
		if ( params.chartType != ChartType.MAP_VIEW ) {
			final Player[] players = params.replay.details.players;
			for ( int i = 0; i < descriptionBuilders.length; i++ ) {
				descriptionBuilders[ i ] = new StringBuilder();
				if ( params.groupByTeam ) {
					descriptionBuilders[ i ].append( Language.getText( "module.repAnalyzer.tab.charts.chartText.team", params.teamList.get( i ) ) ).append( " [" );
					
					int playersInTeam = 0;
					for ( final int playerIndex : params.playerIndices ) {
						if ( params.playerChartIndices[ playerIndex ] == i ) {
							final Player player = players[ playerIndex ];
							if ( playersInTeam++ > 0 )
								descriptionBuilders[ i ].append( ", " );
							descriptionBuilders[ i ].append( player.playerId.name ).append( " (" ).append( player.getRaceLetter() ).append( ')' );
						}
					}
					
					descriptionBuilders[ i ].append( "]" );
				}
				else {
					final Player player = players[ params.chartPlayerIndices[ i ] ];
					descriptionBuilders[ i ].append( player.playerId.name ).append( " (" ).append( player.getRaceLetter() ).append( ')' );
				}
			}
			
			if ( params.gridParams != null )
				paintGrid();
			
			paintAxis();
		}
		
		switch ( params.chartType ) {
		case APM                         : paintApmChart                      (); break;
		case HOTKEYS                     : paintHotkeysChart                  (); break;
		case BUILDS_TECH                 : paintBuildsTechChart               (); break;
		case BUILDS_TECH_STAT            : paintBuildsTechStatChart           (); break;
		case MAP_VIEW                    : paintMapViewChart                  (); break;
		case ACTION_DISTRIBUTION         : paintActionDistributionChart       (); break;
		case MAIN_BUILDING_CONTROL       : paintMainBuildingControlChart      (); break;
		case UNIT_TIERS                  : paintUnitTiersChart                (); break;
		case RESOURCE_SPENDING_RATE      : paintResourceSpendingRateChart     (); break;
		case RESOURCES_SPENT             : paintResourcesSpentChart           (); break;
		case PRODUCED_ARMY_SUPPLY        : paintProducedArmySupplyChart       (); break;
		case APM_REDUNDANCY_DISTRIBUTION : paintApmRedundancyDistributionChart(); break;
		case ACTION_SEQUENCES            : paintActionSequencesChart          (); break;
		case PRODUCTIONS                 : paintProductionsChart              (); break;
		case PLAYER_SELECTIONS           : paintPlayerSelectionsChart         (); break;
		}
		
		if ( params.chartType != ChartType.MAP_VIEW ) {
			paintChartDescriptions();
			// Draw marker
			if ( params.markerX == null && params.markerFrame != null )
				params.markerX = params.frameToX( params.markerFrame );
			if ( params.markerX != null ) {
				g2.setColor( COLOR_CHART_MARKER );
				g2.setStroke( STROKE_DASHED );
				g2.drawLine( params.markerX, 0, params.markerX, params.height - 1 );
				g2.setStroke( STROKE_DEFAULT );
				// Display marker time
				final Font oldFont = g2.getFont();
				g2.setFont( oldFont.deriveFont( Font.BOLD ) );
				final String markerTime = Language.getText( "module.repAnalyzer.tab.charts.chartText.marker", params.displayInSeconds ? ReplayUtils.formatFramesShort( params.xToFrame( params.markerX ), params.replay.converterGameSpeed ) : params.xToFrame( params.markerX ) );
				g2.drawString( markerTime, params.visibleRectangle.x + params.visibleRectangle.width - g2.getFontMetrics().stringWidth( markerTime ) - 1, g2.getFontMetrics().getAscent() - 1 );
				g2.setFont( oldFont );
			}
		}
	}
	
	/**
	 * Paints the grid.
	 */
	private void paintGrid() {
		Float firstFrame  = null;
		float repeatFrame = 0;
		switch (  params.gridParams.predefinedGrid ) {
		case SPAWN_LARVA :
			for ( final Action action : params.replay.gameEvents.actions )
				if ( action instanceof TrainAction && ( (TrainAction) action ).unit == Unit.QUEEN && params.playerChartIndices[ action.player ] >= 0 ) {
					// Queen starts with 25 energy when it is born, Spawn Larva costs 25 energy
					firstFrame  = (float) ( action.frame + ( params.replay.gameEvents.abilityCodes.UNIT_PARAMS.get( Unit.QUEEN ).time << FRAME_BITS_IN_SECOND ) );
					repeatFrame = GridParams.getFrame( 25, TimeUnit.ENERGY_REGENERATION, params.replay.converterGameSpeed );
					break;
				}
			break;
		case CALLDOWN_MULE :
			for ( final Action action : params.replay.gameEvents.actions )
				if ( action instanceof UseBuildingAbilityAction && ( (UseBuildingAbilityAction) action ).abilityGroup == AbilityGroup.UPGRADE_TO_ORBITAL_COMMAND && params.playerChartIndices[ action.player ] >= 0 ) {
					// Orbital Command starts with 50 energy when its ready, Calldown MULE costs 50 energy
					firstFrame  = (float) ( action.frame + ( params.replay.gameEvents.abilityCodes.BUILDING_ABILITY_PARAMS.get( BuildingAbility.UPGRADE_TO_ORBITAL_COMMAND ).time << FRAME_BITS_IN_SECOND ) );
					repeatFrame = GridParams.getFrame( 50, TimeUnit.ENERGY_REGENERATION, params.replay.converterGameSpeed );
					break;
				}
			break;
		case CHRONO_BOOST :
			// Nexus starts with 0 energy, Chrono Boost costs 25 energy
			firstFrame  = 0f;
			repeatFrame = GridParams.getFrame( 25, TimeUnit.ENERGY_REGENERATION, params.replay.converterGameSpeed );
			break;
		case CUSTOM :
			repeatFrame = GridParams.getFrame( params.gridParams.repeatMarker.value1.value, params.gridParams.repeatMarker.value2.value, params.replay.converterGameSpeed );
			firstFrame  = GridParams.getFrame( params.gridParams.firstMarker.value1.value, params.gridParams.firstMarker.value2.value, params.replay.converterGameSpeed );
			break;
		}
		
		if ( firstFrame != null ) {
			g2.setColor( COLOR_GRID );
			g2.setStroke( STROKE_DASHED );
			final float maxFrame = params.replay.frames;
			for ( float frame = firstFrame ; frame < maxFrame; frame += repeatFrame ) {
				final int x = params.frameToX( (int) frame );
				g2.drawLine( x, 0, x, params.height - 1 );
			}
			g2.setStroke( STROKE_DEFAULT );
		}
	}
	
	/**
	 * Paints the axis of the charts, and the time (x) axis labels.
	 */
	private void paintAxis() {
		g2.setColor( COLOR_AXIS );
		
		for ( int i = params.chartsCount - 1; i >= 0; i-- ) {
			g2.drawLine( ChartParams.Y_AXIS_X_CONST + params.visibleRectangle.x, params.chartY1s[ i ], ChartParams.Y_AXIS_X_CONST + params.visibleRectangle.x, params.chartY2s[ i ] );
			g2.drawLine( ChartParams.Y_AXIS_X_CONST, params.chartY2s[ i ] + 1, params.chartX2, params.chartY2s[ i ] + 1 );
		}
		
		// Time axis labels
		final int fontAscent = g2.getFontMetrics().getAscent();
		final int labelsCount = params.chartWidth < TIME_LABELS_MIN_DISTANCE ? 1 : params.chartWidth / TIME_LABELS_MIN_DISTANCE;
		for ( int i = params.chartsCount - 1; i >= 0; i-- ) {
			for ( int j = labelsCount; j >= 0; j-- ) {
				final int    x     = params.chartX1 + j * params.chartDX / labelsCount;
				final int    frame = j == 0 ? 0 : j == labelsCount ? params.replay.frames : params.xToFrame( x );
				final String label = params.displayInSeconds ? ReplayUtils.formatFramesShort( frame, params.replay.converterGameSpeed ) : Integer.toString( frame );
				g2.setColor( COLOR_AXIS );
				g2.drawLine( x, params.chartY2s[ i ] + 1, x, params.chartY2s[ i ] + 3 ); // Marker on axis
				g2.setColor( COLOR_AXIS_LABELS );
				g2.drawString( label, x - ( g2.getFontMetrics().stringWidth( label ) >> ( j == labelsCount ? 0 : 1 ) ), params.chartY2s[ i ] + fontAscent );
			}
		}
	}
	
	/**
	 * Paints the APM chart.
	 */
	private void paintApmChart() {
		final Action[] actions = params.actions;
		Action action;
		
		final int     chartPoints       = params.width / params.apmGranularity + ( params.width % params.apmGranularity != 0 ? 1 : 0 );
		// Last segment might be smaller than the other ones, APM for the last segment must be scaled using its actual width
		final int     lastSegmentWidth_ = params.width % params.apmGranularity;
		final int     lastSegmentWidth  = lastSegmentWidth_ == 0 ? params.apmGranularity : lastSegmentWidth_;
		
		// 1 array for each chart
		final int[][] yPointss      = new int[ params.chartPlayerIndices.length ][ chartPoints ];
		final int[][] yEapmPointss  = params.showEapm          ? new int[ params.chartPlayerIndices.length ][ chartPoints ] : null;
		final int[][] yMicroPointss = params.showMicroMacroApm ? new int[ params.chartPlayerIndices.length ][ chartPoints ] : null;
		final int[][] yXapmPointss  = params.showXapm          ? new int[ params.chartPlayerIndices.length ][ chartPoints ] : null;
		// Count actions
		final int[] playerActionsCount              =                            new int[ params.chartPlayerIndices.length ];
		final int[] excludedPlayerActionsCount      =                            new int[ params.chartPlayerIndices.length ];
		final int[] playerEapmActionsCount          = params.showEapm          ? new int[ params.chartPlayerIndices.length ] : null;
		final int[] excludedPlayerEapmActionsCount  = params.showEapm          ? new int[ params.chartPlayerIndices.length ] : null;
		final int[] playerMicroActionsCount         = params.showMicroMacroApm ? new int[ params.chartPlayerIndices.length ] : null;
		final int[] excludedPlayerMicroActionsCount = params.showMicroMacroApm ? new int[ params.chartPlayerIndices.length ] : null;
		final int[] playerXapmActionsCount          = params.showXapm          ? new int[ params.chartPlayerIndices.length ] : null;
		final int[] excludedPlayerXapmActionsCount  = params.showXapm          ? new int[ params.chartPlayerIndices.length ] : null;
		final int   excludedInitialFrames           = params.replay.excludedInitialFrames;
		// Last move screen actions for XAPM, must be tracked BY PLAYER NOT BY CHART!
		final MoveScreenAction[] lastMSAs = params.showXapm ? new MoveScreenAction[ params.playerChartIndices.length ] : null;
		int chartIndex, pointIndex;
		
		// Selection trackers per player (needed to decide whether select and hotkey actions are micro or macro)
		final PlayerSelectionTracker[] selectionTrackers = params.showMicroMacroApm ? new PlayerSelectionTracker[ params.playerColors.length ] : null;
		if ( selectionTrackers != null )
			for ( int i = 0; i < selectionTrackers.length; i++ )
				selectionTrackers[ i ] = params.playerChartIndices[ i ] < 0 ? null : new PlayerSelectionTracker();
		
		final AbilityCodes abilityCodes = params.replay.gameEvents.abilityCodes;
		final int actionsLength = actions.length;
		for ( int i = 0; i < actionsLength; i++ ) { // We go upward because if micro/macro APM or XAPM is to display, we have to calculate current selections or current screen location (chronological order is a must)
			if ( ( chartIndex = params.playerChartIndices[ ( action = actions[ i ] ).player ] ) >= 0 ) {
				if ( action.type == ActionType.INACTION ) {
					// XAPM: Include certain move screen actions
					if ( yXapmPointss != null && action instanceof MoveScreenAction ) {
						// Include move screen actions where move distance is greater than 15.
						// TODO If the move screen action is preceded by a hotkey double tapping, it should not be included
						// (since it was automatic and not done by the player), but since it is so rare, I just omit it for now.
						final MoveScreenAction msa = (MoveScreenAction) action;
						if ( lastMSAs[ action.player ] != null ) {
							// Distance square
							// Coordinates are map coordinates multiplied by 256; in the range of 0..256; max delta is 256.
							// Max distance square: ffff * ffff + ffff * ffff = 2*MAX_INT    => would require LONG; Instead I omit the fraction, gives little error (unsignificant)
							final int dx = ( msa.x >> 8 ) - ( lastMSAs[ action.player ].x >> 8 );
							final int dy = ( msa.y >> 8 ) - ( lastMSAs[ action.player ].y >> 8 );
							if ( dx * dx + dy * dy > 225 ) {
	    						pointIndex = ( params.frameToX( action.frame ) - ChartParams.Y_AXIS_X_CONST ) / params.apmGranularity;
	    						yXapmPointss[ chartIndex ][ pointIndex ]++;
	    						playerXapmActionsCount[ chartIndex ]++;
	    						if ( action.frame < excludedInitialFrames )
	    							excludedPlayerXapmActionsCount[ chartIndex ]++;
							}
						}
						lastMSAs[ action.player ] = msa;
					}
					continue; // No one else wants inactions...
				}
				
				// APM
				pointIndex = ( params.frameToX( action.frame ) - ChartParams.Y_AXIS_X_CONST ) / params.apmGranularity;
				yPointss[ chartIndex ][ pointIndex ]++;
				playerActionsCount[ chartIndex ]++;
				if ( action.frame < excludedInitialFrames )
					excludedPlayerActionsCount[ chartIndex ]++;
				// Update selection tracker
				if ( selectionTrackers != null ) {
    				if ( action instanceof SelectAction )
    					selectionTrackers[ action.player ].processSelectAction( (SelectAction) action );
    				else if ( action instanceof HotkeyAction )
    					selectionTrackers[ action.player ].processHotkeyAction( (HotkeyAction) action );
				}
				// EAPM
				if ( yEapmPointss != null ) {
					if ( EapmUtils.getActionIneffectiveReason( actions, i ) == null ) {
						yEapmPointss[ chartIndex ][ pointIndex ]++;
						playerEapmActionsCount[ chartIndex ]++;
						if ( action.frame < excludedInitialFrames )
							excludedPlayerEapmActionsCount[ chartIndex ]++;
					}
				}
				// Micro/macro APM
				if ( yMicroPointss != null ) {
					boolean macro;
					if ( action instanceof SelectAction )
						macro = PlayerSelectionTracker.isSelectionMacro( selectionTrackers[ action.player ].currentSelection, abilityCodes );
					else if ( action instanceof HotkeyAction )
						macro = PlayerSelectionTracker.isSelectionMacro( selectionTrackers[ action.player ].hotkeySelectionLists[ ( (HotkeyAction) action ).getNumber() ], abilityCodes );
					else
						macro = action.isMacro();
					if ( !macro ) {
						yMicroPointss[ chartIndex ][ pointIndex ]++;
						playerMicroActionsCount[ chartIndex ]++;
						if ( action.frame < excludedInitialFrames )
							excludedPlayerMicroActionsCount[ chartIndex ]++;
					}
				}
				// XAPM: normal actions count toward XAPM too!
				if ( yXapmPointss != null ) {
					yXapmPointss[ chartIndex ][ pointIndex ]++;
					playerXapmActionsCount[ chartIndex ]++;
					if ( action.frame < excludedInitialFrames )
						excludedPlayerXapmActionsCount[ chartIndex ]++;
				}
			}
		}
		
		for ( int i = 0; i < playerActionsCount.length; i++ ) {
			final int lastActionFrame        = getChartLastActionFrame( i );
			final int convertedCountedFrames = params.replay.converterGameSpeed.convertToRealTime( lastActionFrame - excludedInitialFrames );
			
			final int allActionsCount  = playerActionsCount[ i ] - excludedPlayerActionsCount[ i ];
			
			descriptionBuilders[ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.actions", playerActionsCount[ i ] ) )
				.append( ",        " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.apm", lastActionFrame == 0 ? 0 : ReplayUtils.calculateApm( allActionsCount, convertedCountedFrames ) ) );
			if ( yEapmPointss != null ) {
				final int eapmActionsCount = playerEapmActionsCount[ i ] - excludedPlayerEapmActionsCount[ i ];
				descriptionBuilders[ i ].append( ",        " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.eapm", lastActionFrame == 0 ? 0 : ReplayUtils.calculateApm( eapmActionsCount, convertedCountedFrames ) ) )
					.append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.redundancy", allActionsCount == 0 ? 0 : 100 * ( allActionsCount - eapmActionsCount ) / allActionsCount ) ).append( '%' );
			}
			if ( yMicroPointss != null ) {
				descriptionBuilders[ i ].append( ",        " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.microApm", lastActionFrame == 0 ? 0 : ReplayUtils.calculateApm( playerMicroActionsCount[ i ] - excludedPlayerMicroActionsCount[ i ], convertedCountedFrames ) ) )
					.append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.macroApm", lastActionFrame == 0 ? 0 : ReplayUtils.calculateApm( playerActionsCount[ i ] - playerMicroActionsCount[ i ] - ( excludedPlayerActionsCount[ i ] - excludedPlayerMicroActionsCount[ i ] ), convertedCountedFrames ) ) );
			}
			if ( yXapmPointss != null ) {
				final int xapmActionsCount = playerXapmActionsCount[ i ] - excludedPlayerXapmActionsCount[ i ];
				descriptionBuilders[ i ].append( ",        " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.xapm", lastActionFrame == 0 ? 0 : ReplayUtils.calculateApm( playerXapmActionsCount[ i ] - excludedPlayerXapmActionsCount[ i ], convertedCountedFrames ) ) )
					.append( " (+" ).append( allActionsCount == 0 ? 0 : 100 * ( xapmActionsCount - allActionsCount ) / allActionsCount ).append( "%)" );
			}
		}
		
		// Normalize y points
		// First count max actions (APM graph is the reference intentionally; should be XAPM?)
		final int[] maxActionCounts = new int[ params.chartPlayerIndices.length ];
		for ( int i = 0; i < yPointss.length; i++ )
			maxActionCounts[ i ] = GeneralUtils.maxValue( yPointss[ i ] );
		if ( params.allPlayersOnOneChart ) // We have a global maximum!
			Arrays.fill( maxActionCounts, GeneralUtils.maxValue( maxActionCounts ) );
		// Now scale the y points
		int[] yPoints;
		for ( int i = 0; i < yPointss.length; i++ ) {
			final int chartY2         = params.chartY2s[ i ];
			final int maxActionsCount = maxActionCounts[ i ];
			// Scale all APM types (normal APM, EAPM, micro APM, XAPM)
			for ( int apmType = 3; apmType >= 0; apmType-- ) {
				if ( apmType == 1 && !params.showEapm || apmType == 2 && !params.showMicroMacroApm || apmType == 3 && !params.showXapm )
					continue;
				yPoints = apmType == 0 ? yPointss[ i ] : apmType == 1 ? yEapmPointss[ i ] : apmType == 2 ? yMicroPointss[ i ] : yXapmPointss[ i ];
				if ( maxActionsCount > 0 ) {
					// Last segment might be smaller than the other ones, APM for the last segment must be scaled using its actual width
					yPoints[ yPoints.length - 1 ] = chartY2 - params.chartHeight * yPoints[ yPoints.length - 1 ] * params.apmGranularity / ( maxActionsCount * lastSegmentWidth );
					for ( int j = yPoints.length - 2; j >= 0; j-- )
						yPoints[ j ] = chartY2 - params.chartHeight * yPoints[ j ] / maxActionsCount;
				}
				else 
					Arrays.fill( yPoints, chartY2 );
			}
		}
		
		// Now draw the charts
		
		// Draw assist lines
		final int frameGranularity = params.xToFrame( ChartParams.Y_AXIS_X_CONST + params.apmGranularity - 1 );
		final int assistLinesCount = params.chartHeight < ASSIST_LINES_MIN_DISTANCE ? 1 : params.chartHeight / ASSIST_LINES_MIN_DISTANCE;
		g2.setStroke( STROKE_DASHED );
		for ( int i = params.chartsCount - 1; i >= 0; i-- ) {
			final int maxApm = params.replay.converterGameSpeed.convertToGameTime( (int) ( frameGranularity < 1 ? 1 : (long) maxActionCounts[ i ] * ( 60 << FRAME_BITS_IN_SECOND ) / frameGranularity ) );
			Font oldFont = null;
			if ( maxApm > 999 ) {
				// Max apm is 4 digit (at least), decrease the font size
				oldFont = g2.getFont();
				g2.setFont( oldFont.deriveFont( 10f ) );
			}
			final int fontAscent = g2.getFontMetrics().getAscent();
			for ( int j = assistLinesCount - 1; j >= 0; j-- ) {
				final int y = params.chartY1s[ i ] + j * params.chartDY / assistLinesCount;
				g2.setColor( COLOR_ASSIST_LINES );
				g2.drawLine( params.chartX1 + 1 + params.visibleRectangle.x, y, params.chartX2, y );
				g2.setColor( COLOR_AXIS_LABELS );
				// APM = actionsCount / minute = 60 * actionsCount / sec = 60 * actionsCount / (frame>>FRAME_BITS_IN_SECOND)
				int apm = (int) ( frameGranularity < 1 ? 1 :
					(long) ( assistLinesCount - j ) * maxActionCounts[ i ] * ( 60 << FRAME_BITS_IN_SECOND ) / ( assistLinesCount * frameGranularity )
					);
				apm = params.replay.converterGameSpeed.convertToGameTime( apm );
				g2.drawString( Integer.toString( apm ), params.visibleRectangle.x, y + ( fontAscent >> 1 ) - 1 );
			}
			if ( maxApm > 999 )
				g2.setFont( oldFont );
		}
		
		// Draw APM charts
		final int[] xPoints = new int[ chartPoints ];
		int x = ChartParams.Y_AXIS_X_CONST - params.apmGranularity;
		for ( int i = 0; i < xPoints.length; i++ )
			xPoints[ i ] = x += params.apmGranularity;
		for ( int i = 0; i < yPointss.length; i++ ) {
			// Draw only the visible part for efficiency
			final int j1 = Math.max( 0, ( params.visibleRectangle.x - ChartParams.Y_AXIS_X_CONST ) / params.apmGranularity );
			final int j2 = Math.min( xPoints.length - 1, ( params.visibleRectangle.x + params.visibleRectangle.width - 1 ) / params.apmGranularity + 1 );
			// Draw all APM types (normal APM, EAPM, micro APM, XAPM)
			for ( int apmType = 3; apmType >= 0; apmType-- ) { // Downward so normal APM will be on top followed by EAMP, followed by micro APM  and lastly XAPM chart
				if ( apmType == 1 && !params.showEapm || apmType == 2 && !params.showMicroMacroApm || apmType == 3 && !params.showXapm )
					continue;
				if ( apmType == 0 ) {      // Normal APM
					yPoints = yPointss[ i ];
					g2.setStroke( params.apmApproximation == GraphApproximation.LINEAR ? STROKE_DOUBLE_WIDTH_ROUNDED : STROKE_DOUBLE_WIDTH );
					g2.setColor( params.playerColors[ params.chartPlayerIndices[ i ] ] );
				}
				else if ( apmType == 1 ) { // EAPM
					yPoints = yEapmPointss[ i ];
					g2.setStroke( params.apmApproximation == GraphApproximation.LINEAR ? STROKE_SLIGHTLY_DASHED_ROUNDED : STROKE_SLIGHTLY_DASHED );
					g2.setColor( params.replay.details.players[ params.chartPlayerIndices[ i ] ].getBrighterColor() );
				}
				else if ( apmType == 2 ) { // Micro APM
					yPoints = yMicroPointss[ i ];
					g2.setStroke( params.apmApproximation == GraphApproximation.LINEAR ? STROKE_ROUNDED : STROKE_DEFAULT );
					g2.setColor( params.replay.details.players[ params.chartPlayerIndices[ i ] ].getBrighterColor() );
				}
				else {                     // XAPM
					yPoints = yXapmPointss[ i ];
					g2.setStroke( STROKE_DOTTED );
					g2.setColor( params.replay.details.players[ params.chartPlayerIndices[ i ] ].getBrighterColor() );
				}
				switch ( params.apmApproximation ) {
				case LINEAR :
					for ( int j = j1; j < j2; j++ )
						g2.drawLine( xPoints[ j ], yPoints[ j ], xPoints[ j+1 ], yPoints[ j+1 ] );
					break;
				case CUBIC : {
					// Cubic curve might go outside the chart area, clip it!
					// Moreover, if chart is zoomed, the default clip allows drawing outside of the canvas!!
					final Rectangle oldClipBounds = g2.getClipBounds();
					if ( oldClipBounds == null )
						g2.setClip( params.chartX1, params.chartY1s[ i ], params.chartWidth, params.chartHeight );
					else
						g2.setClip( oldClipBounds.intersection( new Rectangle( params.chartX1, params.chartY1s[ i ], params.chartWidth, params.chartHeight ) ) );
					drawCubicCurve2D( xPoints, yPoints, j1, j2 );
					g2.setClip( oldClipBounds );
					break;
				}
				}
			}
		}
		g2.setStroke( STROKE_DEFAULT );
	}
	
	/**
	 * Returns the frame of the last action of a chart.
	 * @param chartIndex index of chart to return last frame for
	 * @return the frame of the last action of a chart
	 */
	private int getChartLastActionFrame( final int chartIndex ) {
		if ( params.groupByTeam ) {
			// lastActionFrame is the max inside the team
			int lastActionFrame = 0;
			
			for ( int i = 0; i < params.playerChartIndices.length; i++ )
				if ( params.playerChartIndices[ i ] == chartIndex )
					lastActionFrame = Math.max( lastActionFrame, params.replay.details.players[ i ].lastActionFrame );
			
			return lastActionFrame;
		}
		else
			return params.replay.details.players[ params.chartPlayerIndices[ chartIndex ] ].lastActionFrame;
	}
	
	/**
	 * Paints the Hotkeys chart.
	 */
	private void paintHotkeysChart() {
		final int     fontAscent        = g2.getFontMetrics().getAscent();
		final int     numberWidth       = g2.getFontMetrics().charWidth( '8' );
		final boolean chartIsHighEnough = params.chartHeight >= 10 * fontAscent;
		
		// Calculate secondary colors for assign hotkeys
		final Color[] playerInvertedColors = new Color[ params.playerColors.length ];
		for ( int i = 0; i < playerInvertedColors.length; i++ ) {
			final Color playerColor = params.playerColors[ i ];
			if ( playerColor != null )
				playerInvertedColors[ i ] = GeneralUtils.getInvertedColor( playerColor );
		}
		
		final int[] playerHotkeysAssignCounts = new int[ params.chartPlayerIndices.length ];
		final int[] playerHotkeysSelectCounts = params.showSelectHotkeys ? new int[ params.chartPlayerIndices.length ] : null;
		
		final Action[] actions = params.actions;
		Action action;
		int chartIndex, number;
		boolean isSelect;
		
		// The reason why I don't use DeferredToopTipProvider here is because the tool tip data is a list that always changes.
		// Cloning the list for every entry would consume more memory, it's easier just to convert it to string right away.
		final List< Pair< Rectangle, String > > toolTipList = new ArrayList< Pair< Rectangle, String > >( 64 );
		final Map< Short, String > unitTypeNameMap = params.replay.gameEvents.abilityCodes.UNIT_TYPE_NAME;
		
		// Selection trackers per player
		final PlayerSelectionTracker[] selectionTrackers = new PlayerSelectionTracker[ params.playerColors.length ];
		for ( int i = 0; i < selectionTrackers.length; i++ )
			selectionTrackers[ i ] = params.playerChartIndices[ i ] < 0 ? null : new PlayerSelectionTracker();
		
		final int verticalShift = fontAscent + ( chartIsHighEnough ? ( params.chartHeight / 10 - fontAscent ) >> 1 : 0 ) - 2;
		final int actionsLength = actions.length;
		for ( int i = 0; i < actionsLength; i++ ) // Have to go upward because we calculate current selections (chronological order is a must)
			if ( ( chartIndex = params.playerChartIndices[ ( action = actions[ i ] ).player ] ) >= 0 ) {
				if ( action instanceof SelectAction )
					selectionTrackers[ action.player ].processSelectAction( (SelectAction) action );
				else if ( action instanceof HotkeyAction ) {
					selectionTrackers[ action.player ].processHotkeyAction( (HotkeyAction) action );
					isSelect = ( (HotkeyAction) action ).isSelect();
					number   = ( (HotkeyAction) action ).getNumber();
					final List< Short > hotkeySelectionList = selectionTrackers[ action.player ].hotkeySelectionLists[ number ];
					if ( params.showSelectHotkeys || !isSelect ) {
						g2.setColor( params.playerColors[ action.player ] );
						final int x = params.frameToX( action.frame );
						// 0 should be displayed after 9 and not before 1:
						final int num_pos = number == 0 ? 9 : number - 1;
						final int y = params.chartY1s[ chartIndex ] + ( chartIsHighEnough ? num_pos * params.chartHeight / 10 : num_pos * ( params.chartHeight - fontAscent ) / 9 ) + verticalShift;
						if ( isSelect )
							g2.setColor( params.replay.details.players[ action.player ].getBrighterColor() );
						else {
							g2.fillRect( x, y - fontAscent + 2, numberWidth, fontAscent );
							g2.setColor( playerInvertedColors[ action.player ]  );
							
							toolTipList.add( new Pair< Rectangle, String >( new Rectangle( x, y - fontAscent + 2, numberWidth, fontAscent ), PlayerSelectionTracker.getSelectionString( hotkeySelectionList, unitTypeNameMap ) ) );
						}
						g2.drawString( NUMBER_STRINGS[ number ], x, y );
						( isSelect ? playerHotkeysSelectCounts : playerHotkeysAssignCounts )[ chartIndex ]++;
					}
				}
			}
		
		for ( int i = 0; i < descriptionBuilders.length; i++ ) {
			if ( params.showSelectHotkeys )
				descriptionBuilders[ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.hotkeys", playerHotkeysAssignCounts[ i ] + playerHotkeysSelectCounts[ i ] ) )
					.append( " (" ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.assign", playerHotkeysAssignCounts[ i ] ) )
					.append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.select", playerHotkeysSelectCounts[ i ] ) )
					.append( ')' );
			else
				descriptionBuilders[ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.hotkeysAssign", playerHotkeysAssignCounts[ i ] ) );
		}
		
		params.toolTipProvider = new StaticToolTipProvider( toolTipList );
	}
	
	/**
	 * Paints the Builds/Tech chart.
	 */
	private void paintBuildsTechChart() {
		final Action[] actions = params.actions;
		
		// First determine what actions to display
		final Set< ActionType > displayActionTypeSet = EnumSet.noneOf( ActionType.class );
		if ( params.showBuilds )
			displayActionTypeSet.add( ActionType.BUILD );
		if ( params.showTrains )
			displayActionTypeSet.add( ActionType.TRAIN );
		if ( params.showResearches )
			displayActionTypeSet.add( ActionType.RESEARCH );
		if ( params.showUpgrades )
			displayActionTypeSet.add( ActionType.UPGRADE );
		if ( params.showAbilityGroups )
			displayActionTypeSet.add( ActionType.OTHER );
		
		final int entityHeight = params.iconSizes == IconSize.HIDDEN ? g2.getFontMetrics().getAscent() : Icons.getBuildingIcon( Building.NEXUS, params.iconSizes ).getIconHeight() + 2;
		final int MAX_LEVEL = Math.max( 1, ( params.chartDY - 7 ) / entityHeight );
		
		final int[] buildCounts        = params.showBuilds        ? new int[ params.chartPlayerIndices.length ] : null;
		final int[] trainCounts        = params.showTrains        ? new int[ params.chartPlayerIndices.length ] : null;
		final int[] workerCounts       = params.showWorkers       ? new int[ params.chartPlayerIndices.length ] : null;
		final int[] researchCounts     = params.showResearches    ? new int[ params.chartPlayerIndices.length ] : null;
		final int[] upgradeCounts      = params.showUpgrades      ? new int[ params.chartPlayerIndices.length ] : null;
		final int[] abilityGroupCounts = params.showAbilityGroups ? new int[ params.chartPlayerIndices.length ] : null;
		
		final List< Pair< Rectangle, String > > toolTipList = params.iconSizes == IconSize.HIDDEN ? null : new ArrayList< Pair< Rectangle, String > >( 64 );
		
		Action action;
		int chartIndex;
		final int[] chartLevelCounters = new int[ params.allPlayersOnOneChart ? 1 : params.chartsCount ];
		if ( MAX_LEVEL > 2 )
			Arrays.fill( chartLevelCounters, 1 );
		EntityParams entityParams;
		AbilityGroup abilityGroup = null;
		final int actionsLength = actions.length;
		for ( int i = 0; i < actionsLength; i++ ) // We go upward so later icons will overlap earlier ones
			if ( ( chartIndex = params.playerChartIndices[ ( action = actions[ i ] ).player ] ) >= 0 && displayActionTypeSet.contains( action.type ) ) {
				if ( action.type == ActionType.TRAIN ) {
					final Unit unit = ( (TrainAction) action ).unit;
					if ( unit == Unit.SCV || unit == Unit.DRONE || unit == Unit.PROBE )
						if ( params.showWorkers )
							workerCounts[ chartIndex ]++;
						else
							continue;
				} else if ( action.type == ActionType.OTHER ) {
					if ( !( action instanceof UseUnitAbilityAction ) && !( action instanceof UseBuildingAbilityAction ) || ( abilityGroup = ( (BaseUseAbilityAction) action ).abilityGroup ) == null )
						continue;
				}
				
				final Object entity = action.type == ActionType.TRAIN ? ( (TrainAction) action ).unit : action.type == ActionType.BUILD ? ( (BuildAction) action ).building : action.type == ActionType.RESEARCH ? ( (ResearchAction) action ).research : action.type == ActionType.UPGRADE ? ( (UpgradeAction) action ).upgrade : abilityGroup;
				final String text   = entity.toString();
				( action.type == ActionType.TRAIN ? trainCounts : action.type == ActionType.BUILD ? buildCounts : action.type == ActionType.RESEARCH ? researchCounts : action.type == ActionType.UPGRADE ? upgradeCounts : abilityGroupCounts )[ chartIndex ]++;
				
				g2.setColor( action.type == ActionType.RESEARCH || action.type == ActionType.UPGRADE ? params.replay.details.players[ action.player ].getBrighterColor() : action.type==ActionType.OTHER ? params.replay.details.players[ action.player ].getDarkerColor() : params.playerColors[ action.player ] );
				g2.setStroke( action.type == ActionType.TRAIN ? STROKE_DASHED : STROKE_DEFAULT );
				
				if ( params.allPlayersOnOneChart )
					chartIndex = 0;
				final int x = params.frameToX( action.frame );
				final int y = params.chartY2s[ chartIndex ] - 7 - chartLevelCounters[ chartIndex ] * entityHeight;
				if ( ++chartLevelCounters[ chartIndex ] == MAX_LEVEL )
					chartLevelCounters[ chartIndex ] = 0;
				
				g2.drawLine( x, y + 1, x, params.chartY2s[ chartIndex ] );
				if ( params.iconSizes == IconSize.HIDDEN )
					g2.drawString( text, x - ( g2.getFontMetrics().stringWidth( text ) >> 1 ), y );
				else {
					final Icon icon = Icons.getEntityIcon( entity, params.iconSizes );
					Icons.getEntityIcon( entity, params.iconSizes ).paintIcon( params.chartCanvas, g2, x - ( icon.getIconWidth() >> 1 ), y - icon.getIconHeight() );
					toolTipList.add( new Pair< Rectangle, String >( new Rectangle( x - ( icon.getIconWidth() >> 1 ), y - icon.getIconHeight(), icon.getIconWidth(), icon.getIconHeight() ), text ) );
				}
				
				// Duration lines
				if ( params.showDuration != ShowDuration.NONE && action instanceof BaseUseAbilityAction
						&& ( entityParams = ( (BaseUseAbilityAction) action ).getEntityParams() ) != null ) {
					g2.setStroke( STROKE_DEFAULT );
					g2.drawLine( x, y+1, x + entityParams.time * params.chartDX / ( params.replay.gameLengthSec == 0 ? 1 : params.replay.gameLengthSec ), y+1 );
				}
			}
		
		// Draw duration bars
		// We have to re-do the cycle to determine the proper level the entity is displayed on!
		if ( params.showDuration == ShowDuration.BARS ) {
			if ( MAX_LEVEL > 2 )
				Arrays.fill( chartLevelCounters, 1 );
			else
				Arrays.fill( chartLevelCounters, 0 );
			for ( int i = 0; i < actionsLength; i++ ) // We go upward so later icons will overlap earlier ones
				if ( ( chartIndex = params.playerChartIndices[ ( action = actions[ i ] ).player ] ) >= 0 && displayActionTypeSet.contains( action.type ) ) {
					if ( action.type == ActionType.TRAIN ) {
						final Unit unit = ( (TrainAction) action ).unit;
						if ( unit == Unit.SCV || unit == Unit.DRONE || unit == Unit.PROBE )
							if ( !params.showWorkers )
								continue;
					} else if ( action.type == ActionType.OTHER ) {
						if ( !( action instanceof UseUnitAbilityAction ) && !( action instanceof UseBuildingAbilityAction ) || ( (BaseUseAbilityAction) action ).abilityGroup == null )
							continue;
					}
					
					if ( params.allPlayersOnOneChart )
						chartIndex = 0;
					final int x = params.frameToX( action.frame );
					final int y = params.chartY2s[ chartIndex ] - 7 - chartLevelCounters[ chartIndex ] * entityHeight;
					if ( ++chartLevelCounters[ chartIndex ] == MAX_LEVEL )
						chartLevelCounters[ chartIndex ] = 0;
					
					if ( action instanceof BaseUseAbilityAction && ( entityParams = ( (BaseUseAbilityAction) action ).getEntityParams() ) != null ) {
						final Color color = action.type == ActionType.RESEARCH || action.type == ActionType.UPGRADE ? params.replay.details.players[ action.player ].getBrighterColor() : action.type==ActionType.OTHER ? params.replay.details.players[ action.player ].getDarkerColor() : params.playerColors[ action.player ];
						g2.setColor( new Color( color.getRed(), color.getGreen(),color.getBlue(), 50 ) );
						g2.fillRect( x, y+1, entityParams.time * params.chartDX / params.replay.gameLengthSec + 1, params.chartY2s[ chartIndex ] - y - 1 );
					}
				}
		}
		
		if ( toolTipList != null )
			params.toolTipProvider = new StaticToolTipProvider( toolTipList );
		
		if ( !displayActionTypeSet.isEmpty() )
			for ( int i = 0; i < descriptionBuilders.length; i++ ) {
				if ( params.showBuilds )
					descriptionBuilders    [ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.builds"       , buildCounts       [ i ] ) );
				if ( params.showTrains ) {
					descriptionBuilders    [ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.trains"       , trainCounts       [ i ] ) );
					if ( params.showWorkers )
						descriptionBuilders[ i ].append( " (" ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.workers"      , workerCounts      [ i ]  ) ).append( ')' );
				}
				if ( params.showResearches )
					descriptionBuilders    [ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.researches"   , researchCounts    [ i ] ) );
				if ( params.showUpgrades )
					descriptionBuilders    [ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.upgrades"     , upgradeCounts     [ i ] ) );
				if ( params.showAbilityGroups )
					descriptionBuilders    [ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.abilityGroups", abilityGroupCounts[ i ] ) );
			}
	}
	
	/**
	 * Paints the Builds/Tech Stat chart.
	 */
	void paintBuildsTechStatChart() {
		/**
		 * Holds the statistical information of a chart.
		 * @author Andras Belicza
		 */
		class ChartStat {
			public final Map< Unit        , IntHolder > unitCountMap         = params.showUnitsStat         ? new EnumMap< Unit        , IntHolder >( Unit        .class ) : null;
			public final Map< Building    , IntHolder > buildingCountMap     = params.showBuildingsStat     ? new EnumMap< Building    , IntHolder >( Building    .class ) : null;
			public final Map< Research    , IntHolder > researchCountMap     = params.showResearchesStat    ? new EnumMap< Research    , IntHolder >( Research    .class ) : null;
			public final Map< Upgrade     , IntHolder > upgradeCountMap      = params.showUpgradesStat      ? new EnumMap< Upgrade     , IntHolder >( Upgrade     .class ) : null;
			public final Map< AbilityGroup, IntHolder > abilityGroupCountMap = params.showAbilityGroupsStat ? new EnumMap< AbilityGroup, IntHolder >( AbilityGroup.class ) : null;
			
			public final List< Unit         > unitList         = new ArrayList< Unit         >();
			public final List< Building     > buildingList     = new ArrayList< Building     >();
			public final List< Research     > researchList     = new ArrayList< Research     >();
			public final List< Upgrade      > upgradeList      = new ArrayList< Upgrade      >();
			public final List< AbilityGroup > abilityGroupList = new ArrayList< AbilityGroup >();
		}
		
		final int maxFrame = params.markerFrame != null ? params.markerFrame : params.markerX != null ? params.xToFrame( params.markerX ) : params.replay.frames;
		final Action[] actions = params.actions;
		
		final ChartStat[] chartStats = new ChartStat[ params.chartPlayerIndices.length ];
		for ( int i = 0; i < chartStats.length; i++ )
			chartStats[ i ] = new ChartStat();
		
		// First gather statistics
		Action action;
		ChartStat cs;
		final int actionsLength = actions.length;
		int chartIndex;
		for ( int i = 0; i < actionsLength && ( action = actions[ i ] ).frame <= maxFrame; i++ )
			if ( ( chartIndex = params.playerChartIndices[ action.player ] ) >= 0 ) {
				if ( params.showAfterCompleted && action instanceof BaseUseAbilityAction ) {
					final EntityParams entityParams = ( (BaseUseAbilityAction) action ).getEntityParams();
					if ( entityParams != null && action.frame + ( entityParams.time << FRAME_BITS_IN_SECOND ) > maxFrame ) 
						continue;
				}
				cs = chartStats[ chartIndex ]; 
				IntHolder count = null;
				switch ( action.type ) {
				case TRAIN :
					if ( params.showUnitsStat ) {
						final Unit unit = ( (TrainAction) action ).unit;
						if ( ( count = cs.unitCountMap.get( unit ) ) == null ) {
							cs.unitCountMap.put( unit, count = new IntHolder() );
							cs.unitList.add( unit );
						}
					}
					break;
				case BUILD :
					if ( params.showBuildingsStat ) {
						final Building building = ( (BuildAction) action ).building;
						if ( ( count = cs.buildingCountMap.get( building ) ) == null ) {
							cs.buildingCountMap.put( building, count = new IntHolder() );
							cs.buildingList.add( building );
						}
					}
					break;
				case RESEARCH :
					if ( params.showResearchesStat ) {
						final Research research = ( (ResearchAction) action ).research;
						if ( ( count = cs.researchCountMap.get( research ) ) == null ) {
							cs.researchCountMap.put( research, count = new IntHolder() );
							cs.researchList.add( research );
						}
					}
					break;
				case UPGRADE :
					if ( params.showUpgradesStat ) {
						final Upgrade upgrade = ( (UpgradeAction) action ).upgrade;
						if ( ( count = cs.upgradeCountMap.get( upgrade ) ) == null ) {
							cs.upgradeCountMap.put( upgrade, count = new IntHolder() );
							cs.upgradeList.add( upgrade );
						}
					}
					break;
				default :
					if ( action instanceof BaseUseAbilityAction && params.showAbilityGroupsStat ) {
						final AbilityGroup abilityGroup = ( (BaseUseAbilityAction) action ).abilityGroup;
						if ( abilityGroup != null ) {
							if ( ( count = cs.abilityGroupCountMap.get( abilityGroup ) ) == null ) {
								cs.abilityGroupCountMap.put( abilityGroup, count = new IntHolder() );
								cs.abilityGroupList.add( abilityGroup );
							}
						}
					}
					break;
				}
				if ( count != null )
					count.value++;
			}
		
		// Now calculate max counts
		final int[][] maxCountss = new int[ 5 ][ chartStats.length ]; // per entity per chart
		for ( int i = 0; i < chartStats.length; i++ ) {
			cs = chartStats[ i ];
			maxCountss[ 0 ][ i ] = maxOfMap( cs.unitCountMap         );
			maxCountss[ 1 ][ i ] = maxOfMap( cs.buildingCountMap     );
			maxCountss[ 2 ][ i ] = maxOfMap( cs.researchCountMap     );
			maxCountss[ 3 ][ i ] = maxOfMap( cs.upgradeCountMap      );
			maxCountss[ 4 ][ i ] = maxOfMap( cs.abilityGroupCountMap );
		}
		if ( params.allPlayersOnOneChart ) {
			// In case of 1 chart we have a global maximum per entities
			for ( int i = 0; i < 5; i++ ) {
				maxCountss[ i ][ 0 ] = GeneralUtils.maxValue( maxCountss[ i ] );
				for ( int j = 1; j < maxCountss[ i ].length; j++ )
					maxCountss[ i ][ j ] = maxCountss[ i ][ 0 ];
			}
		}
		
		// And finally draw the stat bars
		final int fontAscent   = g2.getFontMetrics().getAscent();
		final int maxBarHeight = Math.max( 0, params.chartHeight - Icons.getBuildingIcon( Building.NEXUS, params.barSize ).getIconHeight() - fontAscent );
		// We draw statistics bars entity by entity, so if all players (teams) are on one chart, same entities will be next to each other
		final IntHolder[] xs = new IntHolder[ params.allPlayersOnOneChart ? 1 : chartStats.length ];
		for ( int i = 0; i < xs.length; i++ )
			xs[ i ] = new IntHolder( ChartParams.Y_AXIS_X_CONST + 1 );
		
		final List< Pair< Rectangle, String > > toolTipList = new ArrayList< Pair< Rectangle, String > >( 32 );
		
		final boolean[][] needSpaceAfterss = new boolean[ params.allPlayersOnOneChart ? 1 : chartStats.length ][ 5 ];
		for ( int entity = 0; entity < 5; entity++ ) {
			for ( int i = 0; i < chartStats.length; i++ ) {
				cs = chartStats[ i ];
				
				final int       y2              = params.chartY2s[ i ];
				final IntHolder x               = xs[ params.allPlayersOnOneChart ? 0 : i ];
				final boolean[] needSpaceAfters = needSpaceAfterss[ params.allPlayersOnOneChart ? 0 : i ];
				// If space is needed but was not used at the last entity (either because it was empty or it is disabled), have to use it now (or carry to the next entity)
				if ( entity > 0 )
					needSpaceAfters[ entity ] |= needSpaceAfters[ entity-1 ];
				g2.setColor( entity == 2 || entity == 3 ? params.replay.details.players[ params.chartPlayerIndices[ i ] ].getBrighterColor() : params.playerColors[ params.chartPlayerIndices[ i ] ] );
				boolean needSpaceAfter = false;
				if ( entity == 0 && cs.unitList.size() > 0 ) {
					needSpaceAfter = true;
					for ( final Unit unit : cs.unitList )
						drawStatBar( toolTipList, Icons.getUnitIcon( unit, params.barSize ), unit, x, maxBarHeight, cs.unitCountMap.get( unit ).value, maxCountss[ 0 ][ i ], y2, null );
				}
				if ( entity == 1 && cs.buildingList.size() > 0 ) {
					if ( needSpaceAfters[ entity-1 ] ) {
						x.value += 10;
						needSpaceAfters[ entity-1 ] = false; // We have put space, do not put again for this entity
					}
					needSpaceAfter = true;
					for ( final Building building : cs.buildingList )
						drawStatBar( toolTipList, Icons.getBuildingIcon( building, params.barSize ), building, x, maxBarHeight, cs.buildingCountMap.get( building ).value, maxCountss[ 1 ][ i ], y2, null );
				}
				if ( entity == 2 && cs.researchList.size() > 0 ) {
					if ( needSpaceAfters[ entity-1 ] ) {
						x.value += 10;
						needSpaceAfters[ entity-1 ] = false; // We have put space, do not put again for this entity
					}
					needSpaceAfter = true;
					for ( final Research research : cs.researchList )
						drawStatBar( toolTipList, Icons.getResearchIcon( research, params.barSize ), research, x, maxBarHeight, cs.researchCountMap.get( research ).value, maxCountss[ 2 ][ i ], y2, null );
				}
				if ( entity == 3 && cs.upgradeList.size() > 0 ) {
					if ( needSpaceAfters[ entity-1 ] ) {
						x.value += 10;
						needSpaceAfters[ entity-1 ] = false; // We have put space, do not put again for this entity
					}
					needSpaceAfter = true;
					for ( final Upgrade upgrade : cs.upgradeList )
						drawStatBar( toolTipList, Icons.getUpgradeIcon( upgrade, params.barSize ), upgrade, x, maxBarHeight, cs.upgradeCountMap.get( upgrade ).value, maxCountss[ 3 ][ i ], y2, null );
				}
				if ( entity == 4 && cs.abilityGroupList.size() > 0 ) {
					if ( needSpaceAfters[ entity-1 ] ) {
						x.value += 10;
						needSpaceAfters[ entity-1 ] = false; // We have put space, do not put again for this entity
					}
					needSpaceAfter = true;
					for ( final AbilityGroup abilityGroup : cs.abilityGroupList )
						drawStatBar( toolTipList, Icons.getAbilityGroupIcon( abilityGroup, params.barSize ), abilityGroup, x, maxBarHeight, cs.abilityGroupCountMap.get( abilityGroup ).value, maxCountss[ 4 ][ i ], y2, null );
				}
				
				needSpaceAfters[ entity ] |= needSpaceAfter;
			}
		}
		
		params.toolTipProvider = new StaticToolTipProvider( toolTipList );
	}
	
	/**
	 * Draws a statistics bar.
	 * @param toolTipList  reference to the list of tool tips (where to add new tool tip)
	 * @param icon         icon of the bar
	 * @param entity       entity to be drawn (will be used to create a tool tip text)
	 * @param x            x coordinate of the bar
	 * @param maxBarHeight maximum allowed height of the bar
	 * @param count        count to calculate and draw bar for
	 * @param maxCount     maximum count belonging to the maximum bar height
	 * @param y2           bottom y coordinate of the bar
	 * @param label        optional label, if provided this will be displayed on top of the bar, if <code>null</code>, the count will be displayed
	 */
	private void drawStatBar( final List< Pair< Rectangle, String > > toolTipList, final Icon icon, final Object entity, final IntHolder x, final int maxBarHeight, final int count, final int maxCount, final int y2, String label ) {
		final int barWidth = icon.getIconWidth();
		icon.paintIcon( params.chartCanvas, g2, x.value, y2 - icon.getIconHeight() + 1 );
		
		final int bottomY = y2 - icon.getIconHeight();
		final int height_ = maxBarHeight * count / maxCount;
		// Passing 0 as the height for the fill3dRect results in a 2 pixel height rectangle :S
		final int height = height_ < 1 ? 1 : height_;
		
		g2.fill3DRect( x.value, bottomY - height, barWidth, height, true );
		
		final Color storedColor = g2.getColor();
		g2.setColor( COLOR_PLAYER_DEFAULT );
		if ( label == null )
			label = Integer.toString( count );
		g2.drawString( label, x.value + ( ( barWidth - g2.getFontMetrics().stringWidth( label ) ) >> 1 ), bottomY - height - 1 );
		g2.setColor( storedColor );
		
		toolTipList.add( new Pair< Rectangle, String >( new Rectangle( x.value, bottomY - height, barWidth, y2 - bottomY + height ), entity.toString() ) );
		
		x.value += barWidth;
	}
	
	/**
	 * Returns the max count in the specified count map.
	 * @param countMap count map in which to determine the max
	 * @return the max count in the specified count map
	 */
	private static int maxOfMap( final Map< ?, IntHolder > countMap ) {
		int maxCount = 0;
		if ( countMap != null )
			for ( final IntHolder count : countMap.values() )
				if ( maxCount < count.value )
					maxCount = count.value;
		return maxCount;
	}
	
	/**
	 * Paints the Map view chart.
	 */
	private void paintMapViewChart() {
		g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, params.mapViewQuality.hintValue );		
		
		final MapInfo mapInfo = params.replay.mapInfo;
		if ( params.replay.mapInfo == null ) {
			g2.setColor( Color.WHITE );
			g2.drawString( Language.getText( "module.repAnalyzer.tab.charts.chartText.mapViewChartNotAvailable1" ), 10, 20 );
			g2.drawString( Language.getText( "module.repAnalyzer.tab.charts.chartText.mapViewChartNotAvailable2" ), 10, 20 + g2.getFontMetrics().getHeight() );
			params.toolTipProvider = new StaticToolTipProvider( new ArrayList< Pair< Rectangle, String > >( 0 ) );
			return;
		}
		
		if ( mapInfo.mapObjectList == null )
			MapParser.parseExtendedMapInfo( params.replay );
		
		final int maxFrame = params.markerFrame != null ? params.markerFrame : params.markerX != null ? params.xToFrame( params.markerX ) : params.replay.frames;
		final Action[] actions = params.actions;
		
		// Either BuildAction will be provided or a MapObject
		final List< Pair< Rectangle, Pair< BuildAction, MapObject > > > toolTipDataList = new ArrayList< Pair< Rectangle, Pair< BuildAction, MapObject > > >( 128 );
		
		final int zoom = params.zoom << 1;
		
		final int mapPixelWidth  = mapInfo.width  * zoom;
		final int mapPixelHeight = mapInfo.height * zoom;
		// Map border
		g2.setColor( Color.LIGHT_GRAY );
		g2.drawLine( mapPixelWidth, 0, mapPixelWidth, mapPixelHeight );
		g2.drawLine( 0, mapPixelHeight, mapPixelWidth, mapPixelHeight );
		if ( zoom <= 2 )
			g2.drawString( Language.getText( "module.repAnalyzer.tab.charts.chartText.mapViewZoomTip" ), mapPixelWidth + 5, 20 );
		
		// Draw map image
		if ( params.mapBackground.requiresMapImage )
			g2.drawImage( mapInfo.previewIcon.getImage(), mapInfo.boundaryLeft * zoom, ( mapInfo.height - mapInfo.boundaryTop ) * zoom,
					mapInfo.previewIcon.getIconWidth() * zoom, mapInfo.previewIcon.getIconHeight() * zoom, null );
		
		Action action;
		final int actionsLength = actions.length;
		
		final Action[] lastMSActions = new Action[ params.playerChartIndices.length ];
		
		if ( params.mapBackground.involvesAreaAggregation ) {
			final boolean hotAreas = params.mapBackground == MapBackground.HOT_AREAS || params.mapBackground == MapBackground.MAP_IMAGE_AND_HOT_AREAS;
			final int areaGranularity = params.areaGranularityCount;
			final int[][] areas = new int[ areaGranularity ][ areaGranularity ];
			for ( int i = 0; i < actionsLength && ( action = actions[ i ] ).frame <= maxFrame; i++ )
				if ( params.playerChartIndices[ action.player ] >= 0 )
					if ( hotAreas ) {
						// Hot areas
						if ( action instanceof BaseUseAbilityAction ) {
							final BaseUseAbilityAction bua = (BaseUseAbilityAction) action;
							if ( bua.targetX != 256 && bua.targetY != 256 )
								areas[ areaGranularity * ( mapInfo.height - ( bua.targetY >> 16 ) ) / mapInfo.height ][ areaGranularity * ( bua.targetX >> 16 ) / mapInfo.width ]++;
						}
					}
					else {
						// Camera areas
						if ( action instanceof MoveScreenAction ) {
							final MoveScreenAction lastMSA = (MoveScreenAction) lastMSActions[ action.player ]; 
							if ( lastMSA != null )
								areas[ areaGranularity * ( mapInfo.height - ( lastMSA.y >> 8 ) ) / mapInfo.height ][ areaGranularity * ( lastMSA.x >> 8 ) / mapInfo.width ] += action.frame - lastMSA.frame;
							lastMSActions[ action.player ] = action;
						} else if ( action instanceof LeaveGameAction )
							lastMSActions[ action.player ] = null; // The player left: the area is no longer being watched by him/her
					}
			if ( !hotAreas ) {
				// Finish the Camera areas: camera stays at where it was last moved to:
				for ( int i = 0; i < lastMSActions.length; i++ ) {
					final MoveScreenAction lastMSA = (MoveScreenAction) lastMSActions[ i ]; 
					if ( lastMSActions[ i ] != null )
						areas[ areaGranularity * ( mapInfo.height - ( lastMSA.y >> 8 ) ) / mapInfo.height ][ areaGranularity * ( lastMSA.x >> 8 ) / mapInfo.width ] += maxFrame - lastMSA.frame;
				}
			}
			
			int maxValue = 0;
			for ( final int[] rows : areas )
				for ( final int value : rows )
					if ( value > maxValue )
						maxValue = value;
			for ( int y = areas.length - 1; y >= 0; y-- ) {
				final int[] row = areas[ y ];
				for ( int x = row.length - 1; x >= 0; x-- ) {
					int value = maxValue == 0 ? 255 : row[ x ] * 255 / maxValue;
					g2.setColor( params.mapBackground.requiresMapImage ? new Color( value, value/2, value/2, 128 ) : new Color( value, value/2, value/2 ) );
					final int xCoord = x * mapPixelWidth  / areaGranularity;
					final int yCoord = y * mapPixelHeight / areaGranularity;
					g2.fillRect( xCoord, yCoord, ( x + 1 ) * mapPixelWidth / areaGranularity - xCoord, ( y + 1 ) * mapPixelHeight / areaGranularity - yCoord );
				}
			}
		}
		
		// Paint map objects
		if ( params.showMapObjects && mapInfo.mapObjectList != null ) { // mapInfo.mapObjectList can be null, if the user opens the replay (default chart is not Map view), changes the map folder (so map won't be available anymore) then switches to the Map view chart
			for ( final Pair< MapObject, Point > pair : mapInfo.mapObjectList ) {
				g2.setColor( MAP_OBJECT_COLOR_MAP.get( pair.value1 ) );
				final Rectangle r = new Rectangle( pair.value1.width * zoom, pair.value1.height * zoom );
				r.x = params.scaleMapCoord( pair.value2.x ) - ( r.width  >> 1 );
				r.y = mapPixelHeight - params.scaleMapCoord( pair.value2.y ) - ( r.height >> 1 );
				g2.fillRect( r.x, r.y, r.width, r.height );
				if ( pair.value1.toolTip != null )
					toolTipDataList.add( new Pair< Rectangle, Pair< BuildAction, MapObject > >( r, new Pair< BuildAction, MapObject >( null, pair.value1 ) ) );
			}
		}
		
		// We have to gather the displayable build actions; due to overlapping some might not be displayed eventually
		// Simply stepping downward on the actions is not sufficient: if B overlaps A, C overlaps B, then the result must be C only
		// (if C does not overlap A, by stepping downward both A and C would be visible) 
		final List< Pair< BuildAction, Rectangle > > displayableActionList = new ArrayList< Pair< BuildAction, Rectangle > >( 128 );
		
		// Paint start locations
		Player player;
		for ( int i = params.replay.details.players.length - 1; i >= 0; i-- )
			if ( params.playerChartIndices[ i ] >= 0 && ( player = params.replay.details.players[ i ] ).startLocation != null ) {
				Building mainBuilding = null;
				Race race = player.finalRace;
				if ( !race.isConcrete ) {
					// Try to find out the race based on the first train action
					for ( int j = 0; j < actionsLength; j++ )
						if ( ( action = actions[ j ] ).player == i && action instanceof TrainAction ) {
							race = ( (TrainAction) action ).unit.raceOfUnit();
							break;
						}
				}
				switch ( race ) {
				case PROTOSS : mainBuilding = Building.NEXUS;          break;
				case TERRAN  : mainBuilding = Building.COMMAND_CENTER; break;
				case ZERG    : mainBuilding = Building.HATCHERY;       break;
				default : continue; // Failed to determine race...
				}
				// A simulated build action:
				final BuildAction ba = params.replay.gameEvents.new BuildAction( mainBuilding );
				ba.player = (byte) i;
				final Rectangle r = new Rectangle( ba.building.width * zoom, ba.building.height * zoom );
				r.x = params.scaleMapCoord( player.startLocation.x ) - ( r.width >> 1 );
				r.y = mapPixelHeight - params.scaleMapCoord( player.startLocation.y ) - ( r.height >> 1 );
				displayableActionList.add( new Pair< BuildAction, Rectangle >( ba, r ) );
			}
		
		final boolean hideOverlappedBuildings = params.hideOverlappedBuildings;
		final boolean showHotPoints           = params.mapBackground == MapBackground.HOT_POINTS || params.mapBackground == MapBackground.MAP_IMAGE_AND_HOT_POINTS;
		final int     hotPointSize            = zoom << 1; // 2 * zoom
		for ( int i = 0; i < actionsLength && ( action = actions[ i ] ).frame <= maxFrame; i++ )
			if ( params.playerChartIndices[ action.player ] >= 0 ) {
				if ( action instanceof MoveScreenAction ) {
					lastMSActions[ action.player ] = action;
					continue;
				}
				if ( action instanceof BuildAction ) {
					final BuildAction ba = (BuildAction) action;
					if ( ba.hasTargetPoint() ) {
						final Rectangle r = new Rectangle( ba.building.width * zoom, ba.building.height * zoom );
						r.x = params.scaleMapCoord( ba.targetX ) - ( r.width >> 1 );
						r.y = mapPixelHeight - params.scaleMapCoord( ba.targetY ) - ( r.height >> 1 );
						if ( hideOverlappedBuildings )
							for ( int j = displayableActionList.size() - 1; j >= 0; j-- )
								if ( r.intersects( displayableActionList.get( j ).value2 ) )
									displayableActionList.remove( j );
						displayableActionList.add( new Pair< BuildAction, Rectangle >( ba, r ) );
					}
				}
				if ( showHotPoints && action instanceof BaseUseAbilityAction ) {
					final BaseUseAbilityAction bua = (BaseUseAbilityAction) action;
					if ( bua.targetX != 256 && bua.targetY != 256 ) {
						final int x = params.scaleMapCoord( bua.targetX ) - ( hotPointSize >> 1 );
						final int y = mapPixelHeight - params.scaleMapCoord( bua.targetY ) - ( hotPointSize >> 1 );
						g2.setColor( params.playerColors[ params.chartPlayerIndices[ params.playerChartIndices[ bua.player ] ] ] );
						g2.fillOval( x, y, hotPointSize, hotPointSize );
					}
				}
			}
		// Now we have the displayable action list, display them
		final GameSpeed converterGameSpeed = params.replay.converterGameSpeed;
		for ( final Pair< BuildAction, Rectangle > displayableAction : displayableActionList ) {
			final BuildAction ba = displayableAction.value1;
			g2.setColor( params.playerColors[ params.chartPlayerIndices[ params.playerChartIndices[ ba.player ] ] ] );
			final Rectangle r = displayableAction.value2;
			if ( params.fillBuildingIcons ) {
				g2.fillRect( r.x, r.y, r.width, r.height );
			}
			else {
				final Icon buildingIcon = Icons.getBuildingIcon( ba.building, IconSize.BIG );
				if ( buildingIcon instanceof ImageIcon ) {
					final Image image = ( (ImageIcon) buildingIcon ).getImage();
					g2.drawImage( image, r.x, r.y, r.width, r.height, null );
					g2.drawRect( r.x, r.y, r.width, r.height );
				}
			}
			toolTipDataList.add( new Pair< Rectangle, Pair< BuildAction, MapObject > >( r, new Pair< BuildAction, MapObject >( ba, null ) ) );
		}
		
		// Indicate player screens
		final int playerScreenPixelWidth  = 25 * zoom + (zoom>>1);
		final int playerScreenPixelHeight = 15 * zoom;
		g2.setStroke( STROKE_DOUBLE_WIDTH );
		for ( final Action lastMSAction : lastMSActions )
			if ( lastMSAction != null ) {
				g2.setColor( params.playerColors[ lastMSAction.player ] );
				final MoveScreenAction msa = (MoveScreenAction) lastMSAction;
				g2.drawRect( ( ( msa.x * zoom ) >> 8 ) - ( playerScreenPixelWidth >> 1 ), mapPixelHeight - ( ( msa.y * zoom ) >> 8 ) - ( playerScreenPixelHeight >> 1 ), playerScreenPixelWidth, playerScreenPixelHeight );
			}
		g2.setStroke( STROKE_DEFAULT );
		
		// Draw last action's marker
		if ( params.selectedAction instanceof BaseUseAbilityAction ) {
			final BaseUseAbilityAction buaa = (BaseUseAbilityAction) params.selectedAction;
			if ( buaa.hasTargetPoint() ) {
				final int tx = params.scaleMapCoord( buaa.targetX );
				final int ty = mapPixelHeight - params.scaleMapCoord( buaa.targetY );
				g2.setColor( Color.RED );
				g2.drawLine( tx, ty - zoom, tx, ty + zoom );
				g2.drawLine( tx - zoom, ty, tx + zoom, ty );
				g2.setColor( Color.WHITE );
				g2.drawRect( tx - zoom, ty - zoom, zoom*2, zoom*2 );
			}
		}
		
		params.toolTipProvider = new DeferredToolTipProvider< Pair< BuildAction, MapObject > >( toolTipDataList, new Producer< Pair< BuildAction, MapObject >, String >() {
			@Override
			public String produce( final Pair< BuildAction, MapObject > input ) {
				if ( input.value1 != null ) {
					final BuildAction ba = input.value1;
					return ba.building.stringValue + " (" + params.replay.details.players[ ba.player ].playerId.name
						+ ( ba.frame == 0 ? ")" : "); " + Language.getText( "module.repAnalyzer.tab.charts.chartTooltip.frameTime", ba.frame, ReplayUtils.formatFramesShort( ba.frame, converterGameSpeed ) ) );
				}
				if ( input.value2 != null )
					return input.value2.toolTip;
				return null;
			}
		} ) {
			@Override
			public String getToolTip( final Point point ) {
				final String toolTip = super.getToolTip( point );
				if ( toolTip != null )
					return toolTip;
				// If no tool tip specified, display the coordinates...
				if ( point.x < mapPixelWidth && point.y < mapPixelHeight ) {
					return "x=" + ReplayUtils.formatCoordinate( (int) ( point.x * 65536f * mapInfo.width / mapPixelWidth ) )
						+ ", y=" + ReplayUtils.formatCoordinate( (int) ( ( mapPixelHeight - point.y ) * 65536f * mapInfo.height / mapPixelHeight ) );
				}
				return null;
			}
		};
	}
	
	/**
	 * Paints the Action distribution chart.
	 */
	private void paintActionDistributionChart() {
		final int maxFrame = params.markerFrame != null ? params.markerFrame : params.markerX != null ? params.xToFrame( params.markerX ) : params.replay.frames;
		final Action[] actions = params.actions;
		
		@SuppressWarnings( "unchecked" )
		final Map< ActionCategory, IntHolder >[] actionCategoryCountMaps = new EnumMap[ params.chartPlayerIndices.length ];
		for ( int i = 0; i < actionCategoryCountMaps.length; i++ )
			actionCategoryCountMaps[ i ] = new EnumMap< ActionCategory, IntHolder >( ActionCategory.class );
		
		// First gather statistics
		Action action;
		Map< ActionCategory, IntHolder > actionCategoryCountMap;
		final int actionsLength = actions.length;
		int chartIndex;
		for ( int i = 0; i < actionsLength && ( action = actions[ i ] ).frame <= maxFrame; i++ )
			if ( ( chartIndex = params.playerChartIndices[ action.player ] ) >= 0 ) {
				ActionCategory category = null;
				switch ( action.type ) {
				case INACTION : continue;
				case SELECT   : category = action instanceof HotkeyAction ? ActionCategory.HOTKEY : ActionCategory.SELECT; break;
				case TRAIN    : category = action instanceof TrainHallucinatedAction ? ActionCategory.OTHER : ActionCategory.TRAIN; break;
				case BUILD    : category = ActionCategory.BUILD   ; break;
				case RESEARCH : category = ActionCategory.RESEARCH; break;
				case UPGRADE  : category = ActionCategory.UPGRADE ; break;
				}
				
				if ( category == null ) {
					if ( action instanceof HotkeyAction )
						category = ActionCategory.HOTKEY;
					else if ( action instanceof BaseUseAbilityAction ) {
						final BaseUseAbilityAction buaa = (BaseUseAbilityAction) action;
						if ( buaa.abilityGroup == null ) {
							if ( "Right click".equals( buaa.abilityName ) )
								category = ActionCategory.RIGHT_CLICK;
						}
						else
							switch ( buaa.abilityGroup ) {
							case STOP : case STOP_GENERATING_CREEP : category = ActionCategory.STOP; break;
							case HOLD_POSITION : category = ActionCategory.HOLD_POSITION; break;
							case MOVE : category = ActionCategory.MOVE; break;
							case PATROL : category = ActionCategory.PATROL; break;
							case ATTACK : case ATTACK_STRUCTURE : category = ActionCategory.ATTACK; break;
							case SET_RALLY_POINT : case SET_WORKER_RALLY_POINT : category = ActionCategory.SET_RALLY_POINT; break;
							case CANCEL : category = ActionCategory.CANCEL; break;
							}
					}
				}
				if ( category == null )
					category = ActionCategory.OTHER;
				
				actionCategoryCountMap = actionCategoryCountMaps[ chartIndex ];
				IntHolder count = actionCategoryCountMap.get( category );
				if ( count == null )
					actionCategoryCountMap.put( category, count = new IntHolder() );
				count.value++;
			}
		
		// Now calculate max counts and total counts
		final int[] maxCounts   = new int[ actionCategoryCountMaps.length ]; //per chart
		final int[] totalCounts = params.showPercent ? new int[ actionCategoryCountMaps.length ] : null; //per chart
		for ( int i = 0; i < actionCategoryCountMaps.length; i++ ) {
			maxCounts[ i ] = maxOfMap( actionCategoryCountMaps[ i ] );
			if ( params.showPercent )
				for ( final IntHolder count : actionCategoryCountMaps[ i ].values() )
					totalCounts[ i ] += count.value;
		}
		if ( params.allPlayersOnOneChart ) {
			// In case of 1 chart we have a global maximum
			maxCounts[ 0 ] = GeneralUtils.maxValue( maxCounts );
			for ( int i = 1; i < maxCounts.length; i++ )
				maxCounts[ i ] = maxCounts[ 0 ];
		}
		
		// And finally draw the stat bars
		final int fontAscent   = g2.getFontMetrics().getAscent();
		final int maxBarHeight = Math.max( 0, params.chartHeight - Icons.getBuildingIcon( Building.NEXUS, params.distributionBarSize ).getIconHeight() - fontAscent );
		
		final List< Pair< Rectangle, String > > toolTipList = new ArrayList< Pair< Rectangle, String > >( 32 );
		
		final ActionCategory[] actionCategories = ActionCategory.values();
		final IntHolder x = new IntHolder();
		for ( int i = 0; i < actionCategoryCountMaps.length; i++ ) {
			if ( params.allPlayersOnOneChart )
				x.value = i == 0 ? ChartParams.Y_AXIS_X_CONST + 1 : x.value + 10;
			else
				x.value = ChartParams.Y_AXIS_X_CONST + 1;
			actionCategoryCountMap = actionCategoryCountMaps[ i ];
			
			final int y2 = params.chartY2s[ i ];
			g2.setColor( params.playerColors[ params.chartPlayerIndices[ i ] ] );
			
			IntHolder count;
			for ( final ActionCategory actionCategory : actionCategories ) {
				if ( ( count = actionCategoryCountMap.get( actionCategory ) ) != null )
					drawStatBar( toolTipList, actionCategory.getIcon( params.distributionBarSize ), actionCategory, x, maxBarHeight, count.value, maxCounts[ i ], y2, params.showPercent ? ( totalCounts[ i ] == 0 ? "0%" : count.value * 100 / totalCounts[ i ] + "%" ) : null );
			}
		}
		
		params.toolTipProvider = new StaticToolTipProvider( toolTipList );
	}
	
	/**
	 * Main Building use related info for one chart.
	 * @author Andras Belicza
	 */
	public static class MainBuildingUse {
		/**
		 * Class representing the history of use of an ability on a building.
		 * @author Andras Belicza
		 */
		public static class UseOnBuildingHistory {
			public final int             buildingId;
			public final String          buildingName;
			public final List< Integer > frameList = new ArrayList< Integer >();
			public UseOnBuildingHistory( final int buildingId, final String buildingName ) {
				this.buildingId   = buildingId;
				this.buildingName = buildingName;
			}
		}
		
		/** Injection history of Hatcheries.                  */
		public List< UseOnBuildingHistory > injectionHistoryList;
		/** Chrono Boost history of Chrono Boosted buidlings. */
		public List< UseOnBuildingHistory > chronoBoostHistoryList;
		
		public List< UseOnBuildingHistory > getInjectionHistoryList() {
			if ( injectionHistoryList == null )
				injectionHistoryList = new ArrayList< UseOnBuildingHistory >();
			return injectionHistoryList;
		}
		
		public List< UseOnBuildingHistory > getChronoBoostHistoryList() {
			if ( chronoBoostHistoryList == null )
				chronoBoostHistoryList = new ArrayList< UseOnBuildingHistory >();
			return chronoBoostHistoryList;
		}
		
		/**
		 * Returns the number of different targeted building.
		 * @return the number of different targeted building
		 */
		public int size() {
			return ( injectionHistoryList == null ? 0 : injectionHistoryList.size() )
				+ ( chronoBoostHistoryList == null ? 0 : chronoBoostHistoryList.size() );
		}
	}
	
	/**
	 * Paints the Main building control chart.
	 */
	private void paintMainBuildingControlChart() {
		final int maxFrame;
		if ( params.calculateUntilMarker )
			maxFrame = params.markerFrame != null ? params.markerFrame : params.markerX != null ? params.xToFrame( params.markerX ) : params.replay.frames;
		else
			maxFrame = params.replay.frames;
		// First gather statistics
		
		// 1 for each chart.
		final MainBuildingUse[] mainBuildingUses = new MainBuildingUse[ params.chartPlayerIndices.length ];
		
		final Action[]             actions        = params.actions;
		final Map< Short, String > UNIT_TYPE_NAME = params.replay.gameEvents.abilityCodes.UNIT_TYPE_NAME;
		Action action;
		final int actionsLength = actions.length;
		int chartIndex;
		for ( int i = 0; i < actionsLength; i++ )
			if ( ( chartIndex = params.playerChartIndices[ ( action = actions[ i ] ).player ] ) >= 0 ) {
				// Zerg Spawn Larva
				if ( action instanceof UseUnitAbilityAction ) {
					final UseUnitAbilityAction uuaa = (UseUnitAbilityAction) action;
					if ( uuaa.unitAbility == UnitAbility.SPAWN_LARVA ) {
						if ( mainBuildingUses[ chartIndex ] == null )
							mainBuildingUses[ chartIndex ] = new MainBuildingUse();
						
						UseOnBuildingHistory injectionHistory = null;
						// Has this Hatchery been injected before?
						for ( final UseOnBuildingHistory injectionHistory_ : mainBuildingUses[ chartIndex ].getInjectionHistoryList() )
							if ( injectionHistory_.buildingId == uuaa.targetId ) {
								injectionHistory = injectionHistory_;
								break;
							}
						if ( injectionHistory == null )
							mainBuildingUses[ chartIndex ].injectionHistoryList.add( injectionHistory = new UseOnBuildingHistory( uuaa.targetId, Building.HATCHERY.stringValue ) );
						
						// Register spawn larva action
						if ( !injectionHistory.frameList.isEmpty()
								&& injectionHistory.frameList.get( injectionHistory.frameList.size() - 1 ) + LARVA_SPAWNING_DURATION > action.frame )
							injectionHistory.frameList.remove( injectionHistory.frameList.size() - 1 ); // The last Spawn larva overlaps the current one: that means the last one was not executed for sure, remove it
						injectionHistory.frameList.add( action.frame );
					}
				}
				// Protoss Chrono Boost
				if ( action instanceof UseBuildingAbilityAction ) {
					final UseBuildingAbilityAction ubaa = (UseBuildingAbilityAction) action;
					if ( ubaa.buildingAbility == BuildingAbility.CHRONO_BOOST ) {
						if ( mainBuildingUses[ chartIndex ] == null )
							mainBuildingUses[ chartIndex ] = new MainBuildingUse();
						
						UseOnBuildingHistory chronoBoostHistory = null;
						// Has this building been Chrono Boosted before?
						for ( final UseOnBuildingHistory chronoBoostHistory_ : mainBuildingUses[ chartIndex ].getChronoBoostHistoryList() )
							if ( chronoBoostHistory_.buildingId == ubaa.targetId ) {
								chronoBoostHistory = chronoBoostHistory_;
								break;
							}
						if ( chronoBoostHistory == null )
							mainBuildingUses[ chartIndex ].chronoBoostHistoryList.add( chronoBoostHistory = new UseOnBuildingHistory( ubaa.targetId, UNIT_TYPE_NAME.get( (short) ubaa.targetType ) ) );
						
						// Register chrono boost action
						chronoBoostHistory.frameList.add( action.frame );
					}
				}
			}
		
		// Now draw charts
		final Font oldFont = g2.getFont();
		g2.setFont( new Font( oldFont.getName(), Font.BOLD, oldFont.getSize() ) );
		for ( int i = 0; i < mainBuildingUses.length; i++ ) { // Charts cycle
			final MainBuildingUse mainBuildingUse = mainBuildingUses[ i ];
			if ( mainBuildingUse == null || mainBuildingUse.size() == 0 )
				continue;
			
			int x = params.chartX1 + 3;
			int y = params.chartY1s[ i ] + 1;
			
			int totalHatchTime         = 0;
			int totalHatchSpawnTime    = 0;
			int totalInjectionGap      = 0;
			int totalInjectionGapCount = 0;
			int totalInjectionCount    = 0;
			
			int totalChronoBoostCount  = 0;
			
			final int lastActionFrame = Math.min( maxFrame, getChartLastActionFrame( i ) );
			
			// 1 pixel gap between rows; mainBuildingUse.size() > 0
			final int rowHeight = ( params.chartHeight - mainBuildingUse.size() ) / mainBuildingUse.size();
			final int fontAscent = g2.getFontMetrics().getAscent();
			
			if ( mainBuildingUse.injectionHistoryList != null ) {
				for ( final UseOnBuildingHistory injectionHistory : mainBuildingUse.injectionHistoryList ) { // Hatcheries cycle
					final int hatchTime         = lastActionFrame > injectionHistory.frameList.get( 0 ) ? lastActionFrame - injectionHistory.frameList.get( 0 ) : 0;
					int       hatchSpawnTime    = 0;
					int       injectionGap      = 0;
					int       injectionGapCount = 0;
					int       injectionCount    = 0;
					// First the bars, then the text, so the text will be on top
					g2.setColor( params.playerColors[ params.chartPlayerIndices[ i ] ] );
					final int framesCount = injectionHistory.frameList.size();
					for ( int j = 0; j < framesCount; j++ ) { // Injections of a Hatchery cycle
						final int frame = injectionHistory.frameList.get( j );
						final int x1    = params.frameToX( frame );
						final int x2    = params.frameToX( frame + LARVA_SPAWNING_DURATION );
						
						g2.fillRect( x1, y, x2 - x1 + 1, rowHeight );
						
						if ( frame < lastActionFrame ) {
							injectionCount++;
							hatchSpawnTime += Math.min( LARVA_SPAWNING_DURATION, lastActionFrame - frame );
							if ( j > 0 ) {
								injectionGap += frame - injectionHistory.frameList.get( j-1 ) - LARVA_SPAWNING_DURATION;
								injectionGapCount++;
							}
						}
					}
					g2.setColor( params.replay.details.players[ params.chartPlayerIndices[ i ] ].getBrighterColor() );
					g2.drawString( injectionHistory.buildingName + " " + Integer.toHexString( injectionHistory.buildingId )
							+ " (" + ( hatchTime == 0 ? "N/A" : ( hatchSpawnTime * 100 / hatchTime ) + "%" )
							+ ", " + ( injectionGapCount == 0 ? "N/A" : ReplayUtils.formatFramesDecimal( injectionGap / injectionGapCount, params.replay.converterGameSpeed ) + " " + Language.getText( "module.repAnalyzer.tab.charts.chartText.sec" ) )
							+ ", " + injectionCount + ")",
							x + params.visibleRectangle.x, y + ( ( rowHeight + fontAscent ) >> 1 ) );
					
					totalHatchTime         += hatchTime;
					totalHatchSpawnTime    += hatchSpawnTime;
					totalInjectionGap      += injectionGap;
					totalInjectionGapCount += injectionGapCount;
					totalInjectionCount    += injectionCount;
					
					y += rowHeight + 1;
				}
				
				// Total averages to the chart descriptions:
				descriptionBuilders[ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.avgSpawningRatio", totalHatchTime == 0 ? "N/A" : ( totalHatchSpawnTime * 100 / totalHatchTime ) + "%"  ) )
					.append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.avgInjectionGap", totalInjectionGapCount == 0 ? "N/A" : ReplayUtils.formatFramesDecimal( totalInjectionGap / totalInjectionGapCount, params.replay.converterGameSpeed ) + " " + Language.getText( "module.repAnalyzer.tab.charts.chartText.sec" ) ) )
					.append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.injectionCount", totalInjectionCount ) );
			}
			
			if ( mainBuildingUse.chronoBoostHistoryList != null ) {
				for ( final UseOnBuildingHistory chronoBoostHistory : mainBuildingUse.chronoBoostHistoryList ) { // Chrono Boosted buildings cycle
					int chronoBoostCount = 0;
					// First the bars, then the text, so the text will be on top
					final Color color     = params.playerColors[ params.chartPlayerIndices[ i ] ];
					final Color fillColor = new Color( color.getRed(), color.getGreen(),color.getBlue(), 85 );
					final int framesCount = chronoBoostHistory.frameList.size();
					for ( int j = 0; j < framesCount; j++ ) { // Chrono Boosts of a building cycle
						final int frame = chronoBoostHistory.frameList.get( j );
						final int x1    = params.frameToX( frame );
						final int x2    = params.frameToX( frame + CHRONO_BOOST_DURATION );
						
						g2.setColor( fillColor );
						g2.fillRect( x1, y, x2 - x1 + 1, rowHeight   );
						g2.setColor( color );
						g2.drawRect( x1, y, x2 - x1 + 1, rowHeight-1 );
						
						if ( frame < lastActionFrame )
							chronoBoostCount++;
					}
					g2.setColor( params.replay.details.players[ params.chartPlayerIndices[ i ] ].getBrighterColor() );
					g2.drawString( chronoBoostHistory.buildingName + " " + Integer.toHexString( chronoBoostHistory.buildingId )
							+ "  (" + chronoBoostCount + ")",
							x + params.visibleRectangle.x, y + ( ( rowHeight + fontAscent ) >> 1 ) );
					
					totalChronoBoostCount += chronoBoostCount;
					
					y += rowHeight + 1;
				}
				
				// Total averages to the chart descriptions:
				descriptionBuilders[ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.chronoBoostCount", totalChronoBoostCount ) );
			}
		}
		g2.setFont( oldFont );
	}
	
	/**
	 * Paints the Unit Tiers chart.
	 */
	private void paintUnitTiersChart() {
		final UnitTier[] unitTiers = UnitTier.values();
		// 1 array for each chart
		final int       barsCount       = params.width / params.unitTiersGranularity + ( params.width % params.unitTiersGranularity != 0 ? 1 : 0 );
		final int[][][] tierUnitsss     = new int[ params.chartPlayerIndices.length ][ barsCount ][ unitTiers.length ];
		final int[][]   totalTierUnitss = new int[ params.chartPlayerIndices.length ][ barsCount ];
		
		@SuppressWarnings("unchecked")
		final Map< Unit, IntHolder >[][] unitCounterMapss = new Map[ params.chartPlayerIndices.length ][ barsCount ];
		
		final Action[] actions = params.actions;
		
		// First count units of the different tiers
		final int actionsLength = actions.length;
		Action action;
		int chartIndex;
		Map< Unit, IntHolder > unitCounterMap;
		IntHolder unitCounter;
		EntityParams entityParams;
		for ( int i = 0; i < actionsLength; i++ ) {
			if ( ( chartIndex = params.playerChartIndices[ ( action = actions[ i ] ).player ] ) >= 0 && action instanceof BaseUseAbilityAction ) {
				final UnitTier unitTier = ( (BaseUseAbilityAction) action ).getUnitTier();
				if ( unitTier != null ) {
					final int frame    = params.showAfterCompleted ? action.frame + ( ( entityParams = ( (BaseUseAbilityAction) action ).getEntityParams() ) == null ? 0 : ( entityParams.time << FRAME_BITS_IN_SECOND ) ) : action.frame;
					final int barIndex = ( params.frameToX( frame ) - ChartParams.Y_AXIS_X_CONST ) / params.unitTiersGranularity;
					if ( barIndex >= barsCount ) // If a unit does not complete before the end of game, bar index would be invalid 
						continue;
					
					tierUnitsss    [ chartIndex ][ barIndex ][ unitTier.ordinal() ]++;
					// We immediately count max values too
					totalTierUnitss[ chartIndex ][ barIndex ]++;
					
					// It's either a TrainAction or a UseUnitAbilityAction
					final Unit unit = action instanceof TrainAction ? ( (TrainAction) action ).unit : ( (UseUnitAbilityAction) action ).unitAbility.transformationTargetUnit;
					
					if ( ( unitCounterMap = unitCounterMapss[ chartIndex ][ barIndex ] ) == null )
						// I do not use EnumMap here because it would be extremely inefficient:
						// Number of units is close to 60, and EnumMap uses an internal array for all values
						// while we usually have less than 5 units per bar!
						// I use TreeMap because I will need the results in Tier order
						unitCounterMap = unitCounterMapss[ chartIndex ][ barIndex ] = new TreeMap< Unit, IntHolder >( ReplayUtils.UNIT_TIER_REVERSE_COMPARATOR );
					if ( ( unitCounter = unitCounterMap.get( unit ) ) == null )
						unitCounterMap.put( unit, new IntHolder( 1 ) );
					else
						unitCounter.value++;
				}
			}
		}
		
		// Count max bar heights
		final int[] maxTotalTierUnits = new int[ params.chartPlayerIndices.length ];
		for ( int i = 0; i < totalTierUnitss.length; i++ )
			maxTotalTierUnits[ i ] = GeneralUtils.maxValue( totalTierUnitss[ i ] );
		
		// Now draw the bars
		final List< Pair< Rectangle, Pair< Map< Unit, IntHolder >, int[] > > > toolTipDataList = new ArrayList< Pair< Rectangle, Pair< Map< Unit, IntHolder >, int[] > > >( params.chartPlayerIndices.length * barsCount ); // A maximum estimate
		final int fontAscent   = g2.getFontMetrics().getAscent();
		final int maxBarHeight = Math.max( 0, params.chartHeight - fontAscent );
		for ( int i = 0; i < tierUnitsss.length; i++ ) {
			final int maxTotalTierUnit = maxTotalTierUnits[ i ];
			if ( maxTotalTierUnit == 0 )
				continue;
			
			final int[][] tierUnitss     = tierUnitsss[ i ];
			final int[]   totalTierUnits = totalTierUnitss[ i ];
			final int     chartY2        = params.chartY2s[ i ] + 1;
			
			for ( int j = tierUnitss.length - 1; j >= 0; j-- ) {
				final int x = params.chartX1 + j * params.unitTiersGranularity + 1;
				final int tierUnitsCountToScale = params.stretchBars ? totalTierUnitss[ i ][ j ] : maxTotalTierUnit;
				
				final int[] tierUnits = tierUnitss[ j ];
				
				int partialTotal = 0;
				int y2           = chartY2;
				int y1           = 0;
				for ( int k = 0; k < tierUnits.length; k++ )
					if ( tierUnits[ k ] > 0 ) {
						g2.setColor( unitTiers[ k ].color );
						partialTotal += tierUnits[ k ];
						y1 = chartY2 - partialTotal * maxBarHeight / tierUnitsCountToScale;
						g2.fillRect( x, y1, params.unitTiersGranularity, y2 - y1 );
						y2 = y1;
					}
				
				// Indicate unit tiers count
				g2.setColor( COLOR_PLAYER_DEFAULT );
				final String label = Integer.toString( totalTierUnits[ j ] );
				int labelX = x + ( ( params.unitTiersGranularity - g2.getFontMetrics().stringWidth( label ) ) >> 1 );
				if ( labelX + g2.getFontMetrics().stringWidth( label ) > params.chartX2 )
					labelX = params.chartX2 - g2.getFontMetrics().stringWidth( label );
				g2.drawString( label, labelX, y1 - 1 );
				
				// Build tool tip table for details
				if ( ( unitCounterMap = unitCounterMapss[ i ][ j ] ) != null )
					toolTipDataList.add( new Pair< Rectangle, Pair< Map< Unit, IntHolder >, int[] > >( new Rectangle( x, y1, params.unitTiersGranularity, chartY2 - y1 ), new Pair< Map< Unit, IntHolder >, int[] >( unitCounterMap, tierUnits ) ) );
			}
		}
		
		params.toolTipProvider = new DeferredToolTipProvider< Pair< Map< Unit, IntHolder >, int[] > >( toolTipDataList, new Producer< Pair< Map< Unit, IntHolder >, int[] >, String >() {
			@Override
			public String produce( final Pair< Map< Unit, IntHolder >, int[] > input ) {
				final Map< Unit, IntHolder > unitCounterMap = input.value1;
				final int[]                  tierUnits      = input.value2;
				
				final StringBuilder toolTipBuilder = new StringBuilder( "<html><table border=1>" );
				UnitTier unitTier = null;
				// unitCounterMap is a TreeMap => entry set is sorted
				for ( final Entry< Unit, IntHolder > entry : unitCounterMap.entrySet() ) {
					if ( unitTier != entry.getKey().unitTier ) {
						unitTier = entry.getKey().unitTier;
						toolTipBuilder.append( "<tr><td style='background:#" ).append( unitTier.htmlColor ).append( "'>&nbsp;&nbsp;&nbsp;<td><b>" ).append( unitTier.stringValue ).append( "</b>" )
							.append( "<td>&sum;=" ).append( tierUnits[ unitTier.ordinal() ] ).append( "<td>" );
					}
					else
						toolTipBuilder.append( ", " );
					toolTipBuilder.append( entry.getKey().stringValue );
					if ( entry.getValue().value > 1 )
						toolTipBuilder.append( " x" ).append( entry.getValue().value );
				}
				toolTipBuilder.append( "</table></html>" );
				
				return toolTipBuilder.toString();
			}
		} );
	}
	
	/**
	 * Paints the Resource Spending Rate chart.
	 */
	private void paintResourceSpendingRateChart() {
		final Action[] actions = params.actions;
		Action action;
		
		final int     chartPoints       = params.width / params.rsrGranularity + ( params.width % params.rsrGranularity != 0 ? 1 : 0 );
		// Last segment might be smaller than the other ones, RSR for the last segment must be scaled using its actual width
		final int     lastSegmentWidth_ = params.width % params.rsrGranularity;
		final int     lastSegmentWidth  = lastSegmentWidth_ == 0 ? params.rsrGranularity : lastSegmentWidth_;
		
		// 1 array for each chart
		final int[][] yMnrlPointss = new int[ params.chartPlayerIndices.length ][ chartPoints ];
		final int[][] yGasPointss  = new int[ params.chartPlayerIndices.length ][ chartPoints ];
		int chartIndex, pointIndex;
		
		EntityParams entityParams;
		for ( int i = actions.length - 1; i >= 0; i-- ) {
			if ( ( action = actions[ i ] ) instanceof BaseUseAbilityAction && ( chartIndex = params.playerChartIndices[ action.player ] ) >= 0
				&& ( entityParams = ( (BaseUseAbilityAction) action ).getEntityParams() ) != null ) {
				if ( entityParams.minerals == 0 && entityParams.gas == 0 )
					continue;
				pointIndex = ( params.frameToX( action.frame ) - ChartParams.Y_AXIS_X_CONST ) / params.rsrGranularity;
				yMnrlPointss[ chartIndex ][ pointIndex ] += entityParams.minerals;
				yGasPointss [ chartIndex ][ pointIndex ] += entityParams.gas;
			}
		}
		
		// Normalize y points
		// First count max values
		final int[] maxValues = new int[ params.chartPlayerIndices.length ];
		for ( int i = 0; i < yMnrlPointss.length; i++ )
			maxValues[ i ] = Math.max( GeneralUtils.maxValue( yMnrlPointss[ i ] ), GeneralUtils.maxValue( yGasPointss[ i ] ) );
		if ( params.allPlayersOnOneChart ) // We have a global maximum!
			Arrays.fill( maxValues, GeneralUtils.maxValue( maxValues ) );
		// Now scale the y points
		int[] yPoints;
		for ( int i = 0; i < yMnrlPointss.length; i++ ) {
			final int chartY2  = params.chartY2s[ i ];
			final int maxValue = maxValues[ i ];
			// Scale all RSR types (Mineral RSR, Gas RSR)
			for ( int rsrType = 1; rsrType >= 0; rsrType-- ) {
				yPoints = rsrType == 0 ? yMnrlPointss[ i ] : yGasPointss[ i ];
				if ( maxValue > 0 ) {
					// Last segment might be smaller than the other ones, RSR for the last segment must be scaled using its actual width
					yPoints[ yPoints.length - 1 ] = chartY2 - params.chartHeight * yPoints[ yPoints.length - 1 ] * params.rsrGranularity / ( maxValue * lastSegmentWidth );
					for ( int j = yPoints.length - 2; j >= 0; j-- )
						yPoints[ j ] = chartY2 - params.chartHeight * yPoints[ j ] / maxValue;
				}
				else 
					Arrays.fill( yPoints, chartY2 );
			}
		}
		
		// Now draw the charts
		
		// Draw assist lines
		final int frameGranularity = params.xToFrame( ChartParams.Y_AXIS_X_CONST + params.rsrGranularity - 1 );
		final int assistLinesCount = params.chartHeight < ASSIST_LINES_MIN_DISTANCE ? 1 : params.chartHeight / ASSIST_LINES_MIN_DISTANCE;
		g2.setStroke( STROKE_DASHED );
		for ( int i = params.chartsCount - 1; i >= 0; i-- ) {
			final int maxValue = params.replay.converterGameSpeed.convertToGameTime( (int) ( frameGranularity < 1 ? 1 : (long) maxValues[ i ] * ( 60 << FRAME_BITS_IN_SECOND ) / frameGranularity ) );
			Font oldFont = null;
			if ( maxValue > 999 ) {
				// Max RSR is 4 digit (at least), decrease the font size
				oldFont = g2.getFont();
				g2.setFont( oldFont.deriveFont( 10f ) );
			}
			final int fontAscent = g2.getFontMetrics().getAscent();
			for ( int j = assistLinesCount - 1; j >= 0; j-- ) {
				final int y = params.chartY1s[ i ] + j * params.chartDY / assistLinesCount;
				g2.setColor( COLOR_ASSIST_LINES );
				g2.drawLine( params.chartX1 + 1 + params.visibleRectangle.x, y, params.chartX2, y );
				g2.setColor( COLOR_AXIS_LABELS );
				// value = spentResources / minute = 60 * spentResources / sec = 60 * spentResources / (frame>>FRAME_BITS_IN_SECOND)
				int value = (int) ( frameGranularity < 1 ? 1 :
					(long) ( assistLinesCount - j ) * maxValues[ i ] * ( 60 << FRAME_BITS_IN_SECOND ) / ( assistLinesCount * frameGranularity )
					);
				g2.drawString( Integer.toString( value ), params.visibleRectangle.x, y + ( fontAscent >> 1 ) - 1 );
			}
			if ( maxValue > 999 )
				g2.setFont( oldFont );
		}
		
		// Draw RSR charts
		final int[] xPoints = new int[ chartPoints ];
		int x = ChartParams.Y_AXIS_X_CONST - params.rsrGranularity;
		for ( int i = 0; i < xPoints.length; i++ )
			xPoints[ i ] = x += params.rsrGranularity;
		for ( int i = 0; i < yMnrlPointss.length; i++ ) {
			// Draw only the visible part for efficiency
			final int j1 = Math.max( 0, ( params.visibleRectangle.x - ChartParams.Y_AXIS_X_CONST ) / params.rsrGranularity );
			final int j2 = Math.min( xPoints.length - 1, ( params.visibleRectangle.x + params.visibleRectangle.width - 1 ) / params.rsrGranularity + 1 );
			// Draw all RSR types (Mineral RSR, Gas RSR)
			for ( int rsrType = 1; rsrType >= 0; rsrType-- ) { // Downward so mineral RSR will be on top followed by GAS rsr
				if ( rsrType == 0 ) { // Mineral RSR
					yPoints = yMnrlPointss[ i ];
					g2.setStroke( params.rsrApproximation == GraphApproximation.LINEAR ? STROKE_DOUBLE_WIDTH_ROUNDED : STROKE_DOUBLE_WIDTH );
					g2.setColor( params.playerColors[ params.chartPlayerIndices[ i ] ] );
				}
				else {                // Gas RSR
					yPoints = yGasPointss[ i ];
					g2.setStroke( params.rsrApproximation == GraphApproximation.LINEAR ? STROKE_DOUBLE_WIDTH_ROUNDED : STROKE_DOUBLE_WIDTH );
					g2.setColor( params.replay.details.players[ params.chartPlayerIndices[ i ] ].getBrighterColor() );
				}
				switch ( params.rsrApproximation ) {
				case LINEAR :
					for ( int j = j1; j < j2; j++ )
						g2.drawLine( xPoints[ j ], yPoints[ j ], xPoints[ j+1 ], yPoints[ j+1 ] );
					break;
				case CUBIC : {
					// Cubic curve might go outside the chart area, clip it!
					// Moreover, if chart is zoomed, the default clip allows drawing outside of the canvas!!
					final Rectangle oldClipBounds = g2.getClipBounds();
					if ( oldClipBounds == null )
						g2.setClip( params.chartX1, params.chartY1s[ i ], params.chartWidth, params.chartHeight );
					else
						g2.setClip( oldClipBounds.intersection( new Rectangle( params.chartX1, params.chartY1s[ i ], params.chartWidth, params.chartHeight ) ) );
					drawCubicCurve2D( xPoints, yPoints, j1, j2 );
					g2.setClip( oldClipBounds );
					break;
				}
				}
			}
		}
		g2.setStroke( STROKE_DEFAULT );
	}
	
	/**
	 * Paints the Resources spent chart.
	 */
	private void paintResourcesSpentChart() {
		// 1 list of points for each chart
		@SuppressWarnings("unchecked")
		final List< Point >[] minPointLists = new List[ params.chartPlayerIndices.length ];
		@SuppressWarnings("unchecked")
		final List< Point >[] gasPointLists = new List[ params.chartPlayerIndices.length ];
		
		int chartIndex;
		EntityParams entityParams;
		
		if ( params.rsCumulative ) {
			// Cumulative: step chart changes when an action is issued where resources are involved
			for ( chartIndex = 0; chartIndex < params.chartPlayerIndices.length; chartIndex++ ) {
				minPointLists[ chartIndex ] = new ArrayList< Point >( 256 );
				gasPointLists[ chartIndex ] = new ArrayList< Point >( 128 );
				minPointLists[ chartIndex ].add( new Point( ChartParams.Y_AXIS_X_CONST, 0 ) );
				gasPointLists[ chartIndex ].add( new Point( ChartParams.Y_AXIS_X_CONST, 0 ) );
			}
			
			// Count resources spent
			List< Point > pointList;
			Point point;
			for ( final Action action : params.actions )
				if ( action instanceof BaseUseAbilityAction && ( chartIndex = params.playerChartIndices[ action.player ] ) >= 0 && ( entityParams = ( (BaseUseAbilityAction) action ).getEntityParams() ) != null ) {
					final int x = params.frameToX( action.frame );
					if ( entityParams.minerals != 0 ) { // Can be negative!
						pointList = minPointLists[ chartIndex ];
						point     = pointList.get( pointList.size() - 1 );
						if ( point.x != x )
							pointList.add( point = new Point( x, point.y ) );
						point.y += entityParams.minerals;
					}
					if ( entityParams.gas != 0 ) { // Can be negative!
						pointList = gasPointLists[ chartIndex ];
						point     = pointList.get( pointList.size() - 1 );
						if ( point.x != x )
							pointList.add( point = new Point( x, point.y ) );
						point.y += entityParams.gas;
					}
				}
			
			for ( int i = 0; i < params.chartPlayerIndices.length; i++ )
				descriptionBuilders[ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.mineralsSpent", minPointLists[ i ].get( minPointLists[ i ].size() - 1 ).y ) )
					.append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.gasSpent", gasPointLists[ i ].get( gasPointLists[ i ].size() - 1 ).y ) );
		}
		else {
			// Non-cumulative: we sum resources for segments of the charts
			final int     chartPoints  = params.width / params.rsGranularity + ( params.width % params.rsGranularity != 0 ? 1 : 0 );
			// 1 array for each chart
			final int[][] yMnrlPointss = new int[ params.chartPlayerIndices.length ][ chartPoints ];
			final int[][] yGasPointss  = new int[ params.chartPlayerIndices.length ][ chartPoints ];
			
			final Action[] actions = params.actions;
			int pointIndex;
			Action action;
			for ( int i = actions.length - 1; i >= 0; i-- ) {
				if ( ( action = actions[ i ] ) instanceof BaseUseAbilityAction && ( chartIndex = params.playerChartIndices[ action.player ] ) >= 0
					&& ( entityParams = ( (BaseUseAbilityAction) action ).getEntityParams() ) != null ) {
					if ( entityParams.minerals == 0 && entityParams.gas == 0 )
						continue;
					pointIndex = ( params.frameToX( action.frame ) - ChartParams.Y_AXIS_X_CONST ) / params.rsGranularity;
					yMnrlPointss[ chartIndex ][ pointIndex ] += entityParams.minerals;
					yGasPointss [ chartIndex ][ pointIndex ] += entityParams.gas;
				}
			}
			
			// Now produce the points list for the step chart
			for ( int i = 0; i < yMnrlPointss.length; i++ ) {
				final List< Point > minPointList = minPointLists[ i ] = new ArrayList< Point >( chartPoints + 2 ); // +2: 1 extra first and 1 extra last point (added by the step chart painter method)
				final List< Point > gasPointList = gasPointLists[ i ] = new ArrayList< Point >( chartPoints + 2 ); // +2: 1 extra first and 1 extra last point (added by the step chart painter method)
				minPointList.add( new Point( ChartParams.Y_AXIS_X_CONST, 0 ) );
				gasPointList.add( new Point( ChartParams.Y_AXIS_X_CONST, 0 ) );
				final int[] yMnrlPoints = yMnrlPointss[ i ];
				final int[] yGasPoints  = yGasPointss [ i ];
				int x = ChartParams.Y_AXIS_X_CONST;
				for ( int j = 0; j < yMnrlPoints.length; j++, x += params.rsGranularity ) {
					minPointList.add( new Point( x, yMnrlPoints[ j ] ) );
					gasPointList.add( new Point( x, yGasPoints [ j ] ) );
				}
			}
		}
		
		paintStepCharts( minPointLists, gasPointLists );
	}
	
	/**
	 * Paints the Produced Army/Supply chart.
	 */
	private void paintProducedArmySupplyChart() {
		// 1 list of points for each chart
		@SuppressWarnings("unchecked")
		final List< Point >[] armyPointLists   = new List[ params.chartPlayerIndices.length ];
		@SuppressWarnings("unchecked")
		final List< Point >[] supplyPointLists = new List[ params.chartPlayerIndices.length ];
		
		int chartIndex;
		EntityParams entityParams;
		
		if ( params.pasCumulative ) {
			// Cumulative: step chart changes when an action is issued where supply is involved
    		for ( chartIndex = 0; chartIndex < params.chartPlayerIndices.length; chartIndex++ ) {
    			armyPointLists  [ chartIndex ] = new ArrayList< Point >( 256 );
    			supplyPointLists[ chartIndex ] = new ArrayList< Point >( 64 );
    			armyPointLists  [ chartIndex ].add( new Point( ChartParams.Y_AXIS_X_CONST, 0 ) );
    			supplyPointLists[ chartIndex ].add( new Point( ChartParams.Y_AXIS_X_CONST, 0 ) );
    		}
    		if ( params.includeInitialUnits ) {
    			final Player[] players = params.replay.details.players;
    			for ( int i = 0; i < players.length; i++ )
    				if ( ( chartIndex = params.playerChartIndices[ i ] ) >= 0 ) {
    					armyPointLists  [ chartIndex ].get( 0 ).y += 6; // Each race starts with 6 workers
    					supplyPointLists[ chartIndex ].get( 0 ).y += players[ i ].finalRace == Race.TERRAN ? 11 : 10;
    				}
    		}
    		
    		// Count produced army and supply
    		List< Point > pointList;
    		Point point;
    		for ( final Action action : params.actions )
    			if ( action instanceof BaseUseAbilityAction && ( chartIndex = params.playerChartIndices[ action.player ] ) >= 0 && ( entityParams = ( (BaseUseAbilityAction) action ).getEntityParams() ) != null ) {
    				if ( entityParams.supply == 0 )
    					continue;
					final int x = params.frameToX( params.showAfterCompleted ? action.frame + ( entityParams.time << FRAME_BITS_IN_SECOND ) : action.frame );
					if ( x > params.chartX2 ) // If "showAfterCompleted", the last actions might not finish before the end of replay
						continue;
					pointList = ( entityParams.supply > 0 ? armyPointLists : supplyPointLists )[ chartIndex ];
					for ( int i = pointList.size() - 1; i >= 0; i-- ) {
						point = pointList.get( i ); 
						if ( point.x < x )
							pointList.add( i + 1, point = new Point( x, point.y ) );
						point.y += entityParams.supply > 0 ? entityParams.supply : -entityParams.supply;
						if ( point.x <= x )
							break;
					}
    			}
    		
    		for ( int i = 0; i < params.chartPlayerIndices.length; i++ )
    			descriptionBuilders[ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.producedArmySize", armyPointLists[ i ].get( armyPointLists[ i ].size() - 1 ).y ) )
    				.append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.producedSupply", supplyPointLists[ i ].get( supplyPointLists[ i ].size() - 1 ).y ) );
		}
		else {
			// Non-cumulative: we sum supply/army for segments of the charts
			
			// Non-cumulative: we sum resources for segments of the charts
			final int     chartPoints    = params.width / params.pasGranularity + ( params.width % params.pasGranularity != 0 ? 1 : 0 );
			// 1 array for each chart
			final int[][] yArmyPointss   = new int[ params.chartPlayerIndices.length ][ chartPoints ];
			final int[][] ySupplyPointss = new int[ params.chartPlayerIndices.length ][ chartPoints ];
			
    		if ( params.includeInitialUnits ) {
    			final Player[] players = params.replay.details.players;
    			for ( int i = 0; i < players.length; i++ )
    				if ( ( chartIndex = params.playerChartIndices[ i ] ) >= 0 ) {
    					yArmyPointss  [ chartIndex ][ 0 ] += 6; // Each race starts with 6 workers
    					ySupplyPointss[ chartIndex ][ 0 ] += players[ i ].finalRace == Race.TERRAN ? 11 : 10;
    				}
    		}
			
			final Action[] actions = params.actions;
			int pointIndex;
			Action action;
			for ( int i = actions.length - 1; i >= 0; i-- ) {
				if ( ( action = actions[ i ] ) instanceof BaseUseAbilityAction && ( chartIndex = params.playerChartIndices[ action.player ] ) >= 0
					&& ( entityParams = ( (BaseUseAbilityAction) action ).getEntityParams() ) != null ) {
					if ( entityParams.supply == 0 )
						continue;
					pointIndex = ( params.frameToX( params.showAfterCompleted ? action.frame + ( entityParams.time << FRAME_BITS_IN_SECOND ) : action.frame ) - ChartParams.Y_AXIS_X_CONST ) / params.pasGranularity;
					if ( pointIndex >= chartPoints ) // If "showAfterCompleted", the last actions might not finish before the end of replay
						continue;
					( entityParams.supply > 0 ? yArmyPointss : ySupplyPointss )[ chartIndex ][ pointIndex ] += entityParams.supply > 0 ? entityParams.supply : -entityParams.supply;
				}
			}
			
			// Now produce the points list for the step chart
			for ( int i = 0; i < yArmyPointss.length; i++ ) {
				final List< Point > armyPointList   = armyPointLists  [ i ] = new ArrayList< Point >( chartPoints + 2 ); // +2: 1 extra first and 1 extra last point (added by the step chart painter method)
				final List< Point > supplyPointList = supplyPointLists[ i ] = new ArrayList< Point >( chartPoints + 2 ); // +2: 1 extra first and 1 extra last point (added by the step chart painter method)
				armyPointList  .add( new Point( ChartParams.Y_AXIS_X_CONST, 0 ) );
				supplyPointList.add( new Point( ChartParams.Y_AXIS_X_CONST, 0 ) );
				final int[] yArmyPoints   = yArmyPointss  [ i ];
				final int[] ySupplyPoints = ySupplyPointss[ i ];
				int x = ChartParams.Y_AXIS_X_CONST;
				for ( int j = 0; j < yArmyPoints.length; j++, x += params.pasGranularity ) {
					armyPointList  .add( new Point( x, yArmyPoints  [ j ] ) );
					supplyPointList.add( new Point( x, ySupplyPoints[ j ] ) );
				}
			}
		}
		
		paintStepCharts( armyPointLists, supplyPointLists );
	}
	
	/**
	 * Paints 2 step charts where the segment are horizontal lines which will be connected with vertical lines.<br>
	 * Also handles scaling. Also appends a last point with the last value so the step chart will fill the available width.
	 * @param point1Lists array (1 element for each chart) of list of the points of the first step chart
	 * @param point2Lists array (1 element for each chart) list of the points of the second step chart
	 */
	private void paintStepCharts( final List< Point >[] point1Lists, final List< Point >[] point2Lists ) {
		// First determine the max values (common for the 2 point list because both are presented on the same chart) 
		final int[] maxValues = new int[ params.chartPlayerIndices.length ];
		for ( int i = 0; i < maxValues.length; i++ ) {
			// Not necessarily the last one is the max/total (there are negative values like salvage bunker)
			// Initialize with the last value so this will minimize the swaps
			// (most likely the last one is the highest in case of cumulative step charts, or the one very close to it)
			final Point lastPoint1 = point1Lists[ i ].get( point1Lists[ i ].size() - 1 );
			final Point lastPoint2 = point2Lists[ i ].get( point2Lists[ i ].size() - 1 );
			
			int totalValue1 = lastPoint1.y;
			int totalValue2 = lastPoint2.y;
			for ( final Point point : point1Lists[ i ] )
				if ( point.y > totalValue1 )
					totalValue1 = point.y;
			for ( final Point point : point2Lists[ i ] )
				if ( point.y > totalValue2 )
					totalValue2 = point.y;
			
			maxValues[ i ] = Math.max( totalValue1, totalValue2 );
			// Put a last point with the same value so the chart will fill the whole chart space
			if ( lastPoint1.x != params.chartX2 )
				point1Lists[ i ].add( new Point( params.chartX2, lastPoint1.y ) );
			if ( lastPoint2.x != params.chartX2 )
				point2Lists[ i ].add( new Point( params.chartX2, lastPoint2.y ) );
		}
		if ( params.allPlayersOnOneChart ) // We have a global maximum!
			Arrays.fill( maxValues, GeneralUtils.maxValue( maxValues ) );
		
		// Now draw the charts
		
		// Draw assist lines
		final int assistLinesCount = params.chartHeight < ASSIST_LINES_MIN_DISTANCE ? 1 : params.chartHeight / ASSIST_LINES_MIN_DISTANCE;
		g2.setStroke( STROKE_DASHED );
		for ( int i = params.chartsCount - 1; i >= 0; i-- ) {
			final Float newFontSize = maxValues[ i ] > 9999 ? new Float( 9f ) : maxValues[ i ] > 999 ? new Float( 10f ) : null;
			Font oldFont = null;
			if ( newFontSize != null ) {
				oldFont = g2.getFont();
				g2.setFont( oldFont.deriveFont( 9f ) );
			}
			final int fontAscent       = g2.getFontMetrics().getAscent();
			for ( int j = assistLinesCount - 1; j >= 0; j-- ) {
				final int y = params.chartY1s[ i ] + j * params.chartDY / assistLinesCount;
				g2.setColor( COLOR_ASSIST_LINES );
				g2.drawLine( params.chartX1 + 1 + params.visibleRectangle.x, y, params.chartX2, y );
				g2.setColor( COLOR_AXIS_LABELS );
				final int value = ( assistLinesCount - j ) * maxValues[ i ] / assistLinesCount;
				g2.drawString( Integer.toString( value ), params.visibleRectangle.x, y + ( fontAscent >> 1 ) - 1 );
			}
			if ( newFontSize != null )
				g2.setFont( oldFont );
		}
		
		// Draw graphs
		g2.setStroke( STROKE_DOUBLE_WIDTH_ROUNDED );
		for ( int i = 0; i < point1Lists.length; i++ ) {
			final int y2 = params.chartY2s[ i ];
			final int maxValue = maxValues[ i ] == 0 ? 1 : maxValues[ i ]; // We divide by it, so it must not be zero
			
			for ( int data = 0; data < 2; data++ ) { // 2 rounds: 1 for point1 list, 1 for point2 list
				final List< Point > pointList = data == 0 ? point1Lists[ i ] : point2Lists[ i ];
				g2.setColor( data == 0 ? params.playerColors[ params.chartPlayerIndices[ i ] ] : params.replay.details.players[ params.chartPlayerIndices[ i ] ].getBrighterColor() );
				
				Point point = pointList.get( 0 );
				int y = y2 - point.y * params.chartDY / maxValue;
				
				final int maxj = pointList.size();
				for ( int j = 1; j < maxj; j++ ) {
					final Point nextPoint = pointList.get( j );
					final int   nextY     = y2 - nextPoint.y * params.chartDY / maxValue;
					g2.drawLine( point.x    , y, nextPoint.x, y     ); // Horizontal
					g2.drawLine( nextPoint.x, y, nextPoint.x, nextY ); // Vertical
					point = nextPoint;
					y     = nextY;
				}
			}
		}
		g2.setStroke( STROKE_DEFAULT );
	}
	
	/**
	 * Paints the APM redundancy distribution chart.
	 */
	private void paintApmRedundancyDistributionChart() {
		final int maxFrame = params.markerFrame != null ? params.markerFrame : params.markerX != null ? params.xToFrame( params.markerX ) : params.replay.frames;
		final Action[] actions = params.actions;
		
		@SuppressWarnings( "unchecked" )
		final Map< IneffectiveReason, Map< Object, IntHolder > >[] reasonDetailCountMapMaps = new EnumMap[ params.chartPlayerIndices.length ];
		for ( int i = 0; i < reasonDetailCountMapMaps.length; i++ )
			reasonDetailCountMapMaps[ i ] = new EnumMap< IneffectiveReason, Map< Object, IntHolder > >( IneffectiveReason.class );
		
		// First gather statistics
		Action action;
		Map< Object, IntHolder > detailCountMap;
		Object detail;
		final int actionsLength = actions.length;
		int chartIndex;
		for ( int i = 0; i < actionsLength && ( action = actions[ i ] ).frame <= maxFrame; i++ )
			if ( ( chartIndex = params.playerChartIndices[ action.player ] ) >= 0 ) {
				final IneffectiveReason reason = EapmUtils.getActionIneffectiveReason( actions, i );
				if ( reason == null )
					continue;
				
				detailCountMap = reasonDetailCountMapMaps[ chartIndex ].get( reason );
				if ( detailCountMap == null )
					reasonDetailCountMapMaps[ chartIndex ].put( reason, detailCountMap = new HashMap< Object, IntHolder >() );
				detail = null;
				switch ( reason ) {
				case ABILITY_FAILED               : detail = IneffectiveReason.ABILITY_FAILED                     ; break;
				case FAST_CANCEL                  : detail = IneffectiveReason.FAST_CANCEL                        ; break;
				case REPEAT_HOTKEY_ASSIGN         : detail = IneffectiveReason.REPEAT_HOTKEY_ASSIGN               ; break;
				case FAST_REPEAT_SAME_HOTKEY      : detail = IneffectiveReason.FAST_REPEAT_SAME_HOTKEY            ; break;
				case FAST_SELECTION_CHANGE        : detail = IneffectiveReason.FAST_SELECTION_CHANGE              ; break;
				case FAST_REPEAT_UNIT_ABILITY     :
				case REPEAT_UNIT_ABILITY          : detail = ( (UseUnitAbilityAction    ) action ).unitAbility    ; break;
				case FAST_REPEAT_BUILDING_ABILITY :
				case REPEAT_BUILDING_ABILITY      : detail = ( (UseBuildingAbilityAction) action ).buildingAbility; break;
				case FAST_REPEAT_OTHER_ABILITY    :
				case REPEAT_OTHER_ABILITY         : detail = ( (BaseUseAbilityAction    ) action ).abilityGroup;
					if ( detail == null )           detail = ( (BaseUseAbilityAction    ) action ).abilityName    ; break;
				case REPEAT_RESEARCH              : detail = ( (ResearchAction          ) action ).research       ; break;
				case REPEAT_UPGRADE               : detail = ( (UpgradeAction           ) action ).upgrade        ; break;
				case REPEAT_BUILDING              : detail = ( (BuildAction             ) action ).building       ; break;
				}
				if ( detail != null ) {
					IntHolder count = detailCountMap.get( detail );
					if ( count == null )
						detailCountMap.put( detail, count = new IntHolder() );
					count.value++;
				}
			}
		
		// Now calculate max counts and total counts
		final int[] maxCounts   = new int[ reasonDetailCountMapMaps.length ]; //per chart
		final int[] totalCounts = params.showRedPercent ? new int[ reasonDetailCountMapMaps.length ] : null; //per chart
		for ( int i = 0; i < reasonDetailCountMapMaps.length; i++ ) {
			final Map< IneffectiveReason, Map< Object, IntHolder > > reasonDetailCountMapMap = reasonDetailCountMapMaps[ i ];
			for ( final Entry< IneffectiveReason, Map< Object, IntHolder > > reasonDetailCountMapMapEntry : reasonDetailCountMapMap.entrySet() ) {
				maxCounts[ i ] = Math.max( maxCounts[ i ], maxOfMap( reasonDetailCountMapMapEntry.getValue() ) );
				if ( params.showRedPercent )
					for ( final IntHolder count : reasonDetailCountMapMapEntry.getValue().values() )
						totalCounts[ i ] += count.value;
			}
		}
		if ( params.allPlayersOnOneChart ) {
			// In case of 1 chart we have a global maximum
			maxCounts[ 0 ] = GeneralUtils.maxValue( maxCounts );
			for ( int i = 1; i < maxCounts.length; i++ )
				maxCounts[ i ] = maxCounts[ 0 ];
		}
		
		// And finally draw the stat bars
		final int fontAscent   = g2.getFontMetrics().getAscent();
		final int maxBarHeight = Math.max( 0, params.chartHeight - Icons.getBuildingIcon( Building.NEXUS, params.redDistributionBarSize ).getIconHeight() - fontAscent );
		
		final List< Pair< Rectangle, String > > toolTipList = new ArrayList< Pair< Rectangle, String > >( 32 );
		
		final IneffectiveReason[] reasons = IneffectiveReason.values();
		final IntHolder x = new IntHolder();
		for ( int i = 0; i < reasonDetailCountMapMaps.length; i++ ) {
			if ( params.allPlayersOnOneChart )
				x.value = i == 0 ? ChartParams.Y_AXIS_X_CONST + 1 : x.value + 10;
			else
				x.value = ChartParams.Y_AXIS_X_CONST + 1;
			final Map< IneffectiveReason, Map< Object, IntHolder > > reasonDetailCountMapMap = reasonDetailCountMapMaps[ i ];
			
			final int y2 = params.chartY2s[ i ];
			g2.setColor( params.playerColors[ params.chartPlayerIndices[ i ] ] );
			
			IntHolder count;
			String    name;
			for ( final IneffectiveReason reason : reasons ) {
				if ( ( detailCountMap = reasonDetailCountMapMap.get( reason ) ) == null )
					continue;
				
				for ( final Entry< Object, IntHolder > detailCountEntry : detailCountMap.entrySet() ) {
					detail = detailCountEntry.getKey();
					count  = detailCountEntry.getValue();
					name   = detail instanceof IneffectiveReason ? reason.stringValue : reason.stringValue + ": " + detail;
					// Entity object for the icon:
					if ( detail instanceof String )
						detail = "Right click".equals( detail ) ? "fugue/mouse-select-right.png" : reason;
					else
						detail = detail instanceof IneffectiveReason ? ( (IneffectiveReason) detail ).defaultIconEntity
							: detail instanceof UnitAbility     ? ( (UnitAbility    ) detail ).abilityGroup
							: detail instanceof BuildingAbility ? ( (BuildingAbility) detail ).abilityGroup : detail;
					drawStatBar( toolTipList, Icons.getEntityIcon( detail, params.redDistributionBarSize ), name, x, maxBarHeight, count.value, maxCounts[ i ], y2, params.showRedPercent ? ( totalCounts[ i ] == 0 ? "0%" : count.value * 100 / totalCounts[ i ] + "%" ) : null );
				}
				
				x.value += 10;
			}
		}
		
		params.toolTipProvider = new StaticToolTipProvider( toolTipList );
	}
	
	/**
	 * Paints the Productions chart.
	 */
	private void paintActionSequencesChart() {
		class ActionSequence {
			public final int     firstFrame;
			public final int     lastFrame;
			public final int     pairsCount;
			public final boolean isHotkey;
			public final int     duration;
			
			public ActionSequence( final int firstFrame, final int lastFrame, final int pairsCount, final boolean isHotkey ) {
				this.firstFrame = firstFrame;
				this.lastFrame  = lastFrame;
				this.pairsCount = pairsCount;
				this.isHotkey   = isHotkey;
				
				// Specify a meaningful minimum duration (else it would be infinite Pairs/sec
				duration = lastFrame == firstFrame ? 4 : lastFrame - firstFrame;
			}
			
			/**
			 * Returns the sequence execution speed in Pairs/Sec.
			 * @return the sequence execution speed in Pairs/Sec.
			 */
			public float getPairsPerSec() {
				return (float) ( pairsCount << ReplayConsts.FRAME_BITS_IN_SECOND ) / duration;
			}
		}
		
		final Action[] actions = params.actions;
		
		// First identify Action Sequences
		@SuppressWarnings( "unchecked" )
		final List< ActionSequence >[] actionSequenceLists = new List[ params.chartPlayerIndices.length ]; // An action sequence has 4 parameters: its first frame; its last frame; pairs count, and whether it is hotkey sequence (0) or not (1)
		for ( int i = 0; i < actionSequenceLists.length; i++ )
			actionSequenceLists[ i ] = new ArrayList< ActionSequence >();
		
		final float[] maxValues     = new float[ params.chartPlayerIndices.length ]; // We calculate max values for normalization in one step 
		final int[]   summaPairs    = new int  [ params.chartPlayerIndices.length ]; // For the average pairs/sec 
		final int[]   summaDuration = new int  [ params.chartPlayerIndices.length ]; // For the average pairs/sec
		
		// Sequences must be tracked PER PLAYER!! (It's not a sequence if a player selects, a teammate "moves", player select, teammate "moves" again...)
		@SuppressWarnings( "unchecked" )
		final List< Action >[] currentPlayerSequences = new List[ params.playerChartIndices.length ];
		for ( int i = 0; i < currentPlayerSequences.length; i++ )
			currentPlayerSequences[ i ] = new ArrayList< Action >();
		
		// Search for sequences...
		Action action;
		int chartIndex;
		List< Action > currentPlayerSequence;
		ActionSequence actionSequence;
		final int actionsLength = actions.length;
		for ( int i = 0; i < actionsLength; i++ )
			if ( ( action = actions[ i ] ).type != ActionType.INACTION && ( chartIndex = params.playerChartIndices[ action.player ] ) >= 0 ) {
				currentPlayerSequence = currentPlayerSequences[ action.player ];
				if ( isActionPartOfSequence( currentPlayerSequence, action ) ) {
					currentPlayerSequence.add( action );
				}
				else {
					// Process current sequence
					final int lastActionSequenceIdx = ( currentPlayerSequence.size() & 0xfffffffe ) - 1; // Make it "even"
					if ( lastActionSequenceIdx > 1 ) {
						// We have a good sequence!
						actionSequenceLists[ chartIndex ].add( actionSequence = new ActionSequence( currentPlayerSequence.get( 0 ).frame, currentPlayerSequence.get( lastActionSequenceIdx ).frame, lastActionSequenceIdx / 2 + 1, currentPlayerSequence.get( 0 ) instanceof HotkeyAction ) );
						summaPairs         [ chartIndex ] += actionSequence.pairsCount;
						summaDuration      [ chartIndex ] += actionSequence.duration;
						final float pairsPerSec = actionSequence.getPairsPerSec();
						if ( maxValues[ chartIndex ] < pairsPerSec )
							maxValues[ chartIndex ] = pairsPerSec;
					}
					// Start a new sequence
					currentPlayerSequence.clear();
					if ( action instanceof SelectAction || action instanceof HotkeyAction && ( (HotkeyAction) action ).isSelect() )
						currentPlayerSequence.add( action );
				}
			}
		
		// Process potential last sequences
		for ( int i = 0; i < currentPlayerSequences.length; i++ ) {
			currentPlayerSequence = currentPlayerSequences[ i ];
			final int lastActionSequenceIdx = ( currentPlayerSequence.size() & 0xfffffffe ) - 1; // Make it "even"
			if ( lastActionSequenceIdx > 1 ) {
				chartIndex = params.playerChartIndices[ i ];
				// We have a good sequence!
				actionSequenceLists[ chartIndex ].add( actionSequence = new ActionSequence( currentPlayerSequence.get( 0 ).frame, currentPlayerSequence.get( lastActionSequenceIdx ).frame, lastActionSequenceIdx / 2 + 1, currentPlayerSequence.get( 0 ) instanceof HotkeyAction ) );
				summaPairs         [ chartIndex ] += actionSequence.pairsCount;
				summaDuration      [ chartIndex ] += actionSequence.duration;
				final float pairsPerSec = actionSequence.getPairsPerSec();
				if ( maxValues[ chartIndex ] < pairsPerSec )
					maxValues[ chartIndex ] = pairsPerSec;
			}
		}
		
		if ( params.allPlayersOnOneChart ) // We have a global maximum!
			Arrays.fill( maxValues, GeneralUtils.maxValue( maxValues ) );
		
		// Add info to descriptions
		for ( int i = 0; i < actionSequenceLists.length; i++ )
			descriptionBuilders[ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.sequences", actionSequenceLists[ i ].size() ) )
				.append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.pairs", summaPairs[ i ] ) )
				.append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.avgSpeed", String.format( Locale.US, "%.1f", summaDuration[ i ] == 0 ? 0 : (float) ( summaPairs[ i ] << ReplayConsts.FRAME_BITS_IN_SECOND ) / summaDuration[ i ] ) ) );
		
		// Now draw the charts
		
		// Draw assist lines
		final int assistLinesCount = params.chartHeight < ASSIST_LINES_MIN_DISTANCE ? 1 : params.chartHeight / ASSIST_LINES_MIN_DISTANCE;
		g2.setStroke( STROKE_DASHED );
		for ( int i = params.chartsCount - 1; i >= 0; i-- ) {
			final float maxValue = maxValues[ i ];
			Font oldFont = null;
			if ( maxValue > 999 ) {
				// Max RSR is 4 digit (at least), decrease the font size
				oldFont = g2.getFont();
				g2.setFont( oldFont.deriveFont( 10f ) );
			}
			final int fontAscent = g2.getFontMetrics().getAscent();
			for ( int j = assistLinesCount - 1; j >= 0; j-- ) {
				final int y = params.chartY1s[ i ] + j * params.chartDY / assistLinesCount;
				g2.setColor( COLOR_ASSIST_LINES );
				g2.drawLine( params.chartX1 + 1 + params.visibleRectangle.x, y, params.chartX2, y );
				g2.setColor( COLOR_AXIS_LABELS );
				float value = ( assistLinesCount - j ) * maxValue / assistLinesCount;
				value = params.replay.converterGameSpeed.convertToGameTime( 1000 ) * value / 1000;
				g2.drawString( String.format( Locale.US, "%.1f", value ), params.visibleRectangle.x, y + ( fontAscent >> 1 ) - 1 );
			}
			if ( maxValue > 999 )
				g2.setFont( oldFont );
		}
		
		// Draw bars
		g2.setStroke( STROKE_DEFAULT );
		for ( int i = 0; i < actionSequenceLists.length; i++ ) {
			final Color selectColor = params.playerColors[ params.chartPlayerIndices[ i ] ];
			final Color hotkeyColor = params.replay.details.players[ params.chartPlayerIndices[ i ] ].getBrighterColor();
			final float maxValue    = maxValues[ i ];
			final int chartY2       = params.chartY2s[ i ];
			
			final List< ActionSequence > actionSequenceList = actionSequenceLists[ i ];
			for ( final ActionSequence actionSeq : actionSequenceList ) {
				g2.setColor( actionSeq.isHotkey ? hotkeyColor : selectColor );
				final int x1 = params.frameToX( actionSeq.firstFrame );
				final int x2 = params.frameToX( actionSeq.lastFrame );
				int height = (int) ( params.chartHeight * actionSeq.getPairsPerSec() / maxValue );
				g2.fillRect( x1, chartY2 - height + 1, x2 - x1 + 1, height );
			}
		}
	}
	
	/**
	 * Checks if the specified action is part of the sequence.
	 * @return true if the action is part of the sequence; false otherwise
	 */
	private boolean isActionPartOfSequence( final List< Action > sequence, final Action action ) {
		// Select is expected if we have even number of actions in the current sequence (a sequence always starts with a select)
		if ( ( sequence.size() & 0x01 ) == 0 ) {
			// Select is expected
			if ( sequence.isEmpty() )
				// Any select is good, it'll be the first of the sequence
				return action instanceof SelectAction || action instanceof HotkeyAction && ( (HotkeyAction) action ).isSelect();
			else
				// Must be the same type of select and must be within the allowed frame break
				return sequence.get( 0 ).getClass() == action.getClass()
					&& action.frame - sequence.get( sequence.size() - 1 ).frame <= params.maxSequenceFrameBreak;
		}
		else {
			// Command is expected
			if ( sequence.size() == 1 )
				// Cannot be select and must be within the allowed frame break
				return !( action instanceof SelectAction || action instanceof HotkeyAction && ( (HotkeyAction) action ).isSelect() )
					&& action.frame - sequence.get( sequence.size() - 1 ).frame <= params.maxSequenceFrameBreak;
			// Must be the same type of command and must be within the allowed frame break
			return sequence.get( 1 ).getClass() == action.getClass()
				&& action.frame - sequence.get( sequence.size() - 1 ).frame <= params.maxSequenceFrameBreak;
				
		}
	}
	
	/**
	 * Paints the Productions chart.
	 */
	private void paintProductionsChart() {
		/**
		 * Holds the statistical information of a production.
		 * @author Andras Belicza
		 */
		class ProductionStat {
			/** Entity of the production. It can be a unit, a building, a research, an upgrade or an ability group. */
			public final Object entity;
			/** Name of the production entity.                                                                      */
			public final String entityName;
			/** Count of the production entities if the same are grouped.                                           */
			public int          count = 1;
			/** Completion bar width in pixels. If the same are grouped, this is the max of completions.            */
			public int          completionBarWidth;
			
			/**
			 * Creates a new ProductionStat.
			 * @param keyEntity          entity of the production
			 * @param entityName         name of the production entity
			 * @param completionBarWidth completion bar width in pixels
			 */
			public ProductionStat( final Object entity, final String entityName, final int completionBarWidth ) {
				this.entity             = entity;
				this.entityName         = entityName;
				this.completionBarWidth = completionBarWidth;
			}
		}
		
		final int frame = params.markerFrame == null ? params.markerX == null ? params.replay.frames : params.xToFrame( params.markerX ) : params.markerFrame;
		final Action[] actions = params.actions;
		
		final boolean groupSameProductions = params.groupSameProductions;
		final Set< AbilityGroup > productionAbilityGroupSet = EnumSet.of( AbilityGroup.ARCHON_WARP, AbilityGroup.ARM_SILO_WITH_NUKE, AbilityGroup.MORPH_TO_BANELING, AbilityGroup.MORPH_TO_BROOD_LORD, AbilityGroup.MORPH_TO_OVERSEER, AbilityGroup.MUTATE_INTO_A_GATEWAY, AbilityGroup.MUTATE_INTO_GREATER_SPIRE, AbilityGroup.MUTATE_INTO_HIVE, AbilityGroup.MUTATE_INTO_LAIR, AbilityGroup.SPAWN_CREEP_TUMOR, AbilityGroup.TRAIN_AN_INTERCEPTOR, AbilityGroup.UPGRADE_TO_ORBITAL_COMMAND, AbilityGroup.UPGRADE_TO_PLANETARY_FORTRESS, AbilityGroup.UPGRADE_TO_WARP_GATE );
		
		final IconSize iconSize              = params.iconSizesP;
		final int  maxCompletionBarWidth = Icons.getBuildingIcon( Building.NEXUS, iconSize ).getIconWidth() - 2;
		
		// First gather statistics
		@SuppressWarnings("unchecked")
		final List< ProductionStat >[] productionStatLists = new List[ params.chartPlayerIndices.length ];
		for ( int i = productionStatLists.length - 1; i >= 0; i-- )
			productionStatLists[ i ] = new ArrayList< ProductionStat >( groupSameProductions ? 10 : 20 );
		
		Action action;
		EntityParams entityParams;
		final int actionsLength = actions.length;
		int chartIndex;
		for ( int i = 0; i < actionsLength && ( action = actions[ i ] ).frame <= frame; i++ )
			if ( ( chartIndex = params.playerChartIndices[ action.player ] ) >= 0 && action instanceof BaseUseAbilityAction
					&& ( entityParams = ( (BaseUseAbilityAction) action ).getEntityParams() ) != null && entityParams.time > 0
					&& action.frame + ( entityParams.time << FRAME_BITS_IN_SECOND ) >= frame ) {
				
				Object entity     = null;
				String entityName = null;
				
				switch ( action.type ) {
				case TRAIN:
					{
						final Unit unit = ( (TrainAction) action ).unit;
						entity     = unit;
						entityName = unit.stringValue;
					}
					break;
				case BUILD :
					{
						final Building building = ( (BuildAction) action ).building;
						entity     = building;
						entityName = building.stringValue;
					}
					break;
				case RESEARCH :
					{
						final Research research = ( (ResearchAction) action ).research;
						entity     = research;
						entityName = research.stringValue;
					}
					break;
				case UPGRADE :
					{
						final Upgrade upgrade = ( (UpgradeAction) action ).upgrade;
						entity     = upgrade;
						entityName = upgrade.stringValue;
					}
					break;
				default :
					{
						final AbilityGroup abilityGroup = ( (BaseUseAbilityAction) action ).abilityGroup;
						if ( productionAbilityGroupSet.contains( abilityGroup ) ) {
							entity     = abilityGroup;
							entityName = abilityGroup.stringValue;
						}
					}
					break;
				}
				
				if ( entity != null ) {
					final int completionBarWidth = maxCompletionBarWidth * ( frame - action.frame ) / ( entityParams.time << FRAME_BITS_IN_SECOND ); // entityParams.time > 0 is in the condition
					final List< ProductionStat > productionStatList = productionStatLists[ chartIndex ];
					boolean addNew = true;
					if ( groupSameProductions )
						for ( final ProductionStat productionStat : productionStatList )
							if ( productionStat.entity == entity ) {
								addNew = false;
								productionStat.count++;
								productionStat.completionBarWidth = Math.max( completionBarWidth, productionStat.completionBarWidth );
								break;
							}
					if ( addNew )
						productionStatList.add( new ProductionStat( entity, entityName, completionBarWidth ) );
				}
			}
		
		// Now draw productions
		final List< Pair< Rectangle, String > > toolTipList = new ArrayList< Pair< Rectangle, String > >( 128 );
		final Font oldFont = g2.getFont();
		g2.setFont( new Font( oldFont.getName(), Font.BOLD, oldFont.getSize() ) );
		final int completionBarHeight = Icons.getBuildingIcon( Building.NEXUS, iconSize ).getIconHeight() >> 2;
		for ( int i = 0; i < productionStatLists.length; i++ ) {
			final List< ProductionStat > productionStatList = productionStatLists[ i ];
			int x = params.chartX1 + 1;
			int y = params.chartY1s[ i ] + 1;
			
			for ( int j = 0; j < productionStatList.size(); j++ ) {
				final ProductionStat productionStat = productionStatList.get( j );
				final Icon           icon           = Icons.getEntityIcon( productionStat.entity, iconSize );
				final int            iconWidth      = icon.getIconWidth ();
				final int            iconHeight     = icon.getIconHeight();
				
				if ( x + iconWidth > params.chartX2 ) // End of chart reached...
					if ( y + ( ( iconHeight + completionBarHeight + 1 ) << 1 ) < params.chartY2s[ i ] ) { // Is there space for more rows?
						x = params.chartX1 + 1;
						y += iconHeight + completionBarHeight + 1;
					}
				
				icon.paintIcon( params.chartCanvas, g2, x, y );
				toolTipList.add( new Pair< Rectangle, String >( new Rectangle( x, y, iconWidth, iconHeight ), productionStat.count == 1 ? productionStat.entityName : productionStat.entityName + " x" + productionStat.count ) );
				
				// Paint production count
				if ( groupSameProductions ) {
					g2.setColor( Color.WHITE );
					final String countString = Integer.toString( productionStat.count );
					g2.drawString( countString, x + iconWidth - g2.getFontMetrics().stringWidth( countString ), y + iconHeight );
				}
				
				// Paint completion bar
				g2.setColor( PlayerColor.GREEN.color );
				g2.drawRect( x+1, y+1 + iconHeight, iconWidth-2, completionBarHeight );
				g2.setColor( PlayerColor.DARK_GREEN.color );
				g2.fillRect( x+2, y+1 + iconHeight + 1, maxCompletionBarWidth, completionBarHeight - 1 );
				g2.setColor( PlayerColor.LIGHT_GREEN.color );
				g2.fillRect( x+2, y+1 + iconHeight + 1, productionStat.completionBarWidth, completionBarHeight - 1 );
				
				x += iconWidth;
			}
		}
		g2.setFont( oldFont );
		
		params.toolTipProvider = new StaticToolTipProvider( toolTipList );
	}
	
	/**
	 * Paints the Player Selections chart.
	 */
	private void paintPlayerSelectionsChart() {
		// Selection trackers per player
		final PlayerSelectionTracker[] selectionTrackers = new PlayerSelectionTracker[ params.playerColors.length ]; // Per player
		for ( int i = 0; i < selectionTrackers.length; i++ )
			selectionTrackers[ i ] = params.playerChartIndices[ i ] < 0 ? null : new PlayerSelectionTracker();
		
		final Action[] actions = params.actions;
		
		final int maxFrame = params.markerFrame != null ? params.markerFrame : params.markerX != null ? params.xToFrame( params.markerX ) : params.replay.frames;
		
		// First determine player selections
		// It has to be tracked player by player (due to deselections)!
		Action action;
		final int actionsLength = actions.length;
		for ( int i = 0; i < actionsLength && ( action = actions[ i ] ).frame <= maxFrame; i++ )
			if ( selectionTrackers[ action.player ] != null ) {
				if ( action instanceof SelectAction )
					selectionTrackers[ action.player ].processSelectAction( (SelectAction) action );
				else if ( action instanceof HotkeyAction )
					selectionTrackers[ action.player ].processHotkeyAction( (HotkeyAction) action );
			}
		
		// Determine chart selections
		int chartIndex;
		@SuppressWarnings("unchecked")
		final List< Short >[] chartSelections = new List[ params.chartPlayerIndices.length ];
		// In case of "group by teams" team order is to be used when concatenating player selections
		for ( final int playerIndex : params.playerIndices ) {
				if ( selectionTrackers[ playerIndex ] != null ) {
					chartIndex = params.playerChartIndices[ playerIndex ];
					// If "group by teams", multiple players might belong to the same chart...
					if ( chartSelections[ chartIndex ] == null )
						chartSelections[ chartIndex ] = selectionTrackers[ playerIndex ].currentSelection;
					else
						chartSelections[ chartIndex ].addAll( selectionTrackers[ playerIndex ].currentSelection );
				}
		}
		
		// Now draw the selections
		final AbilityCodes abilityCodes = params.replay.gameEvents.abilityCodes;
		final Point[] chartXPoints = new Point[ params.chartPlayerIndices.length ];
		for ( int i = 0; i < chartSelections.length; i++ )
			chartXPoints[ i ] = new Point( params.chartX1 + 1, params.chartY1s[ i ] + 1 );
		final Icon nullGrayIcon = new Icon() {
			private final int width  = IconHandler.NULL.get( params.iconSizesPS ).getIconWidth ();
			private final int height = IconHandler.NULL.get( params.iconSizesPS ).getIconHeight();
			@Override public int getIconHeight() { return height; }
			@Override public int getIconWidth () { return width ; }
			@Override
			public void paintIcon( final Component c, final Graphics g, final int x, final int y ) {
				g.setColor( Color.gray );
				g.fillRect( x+1, y+1, width-1, height-1 );
			}
		};
		final List< Pair< Rectangle, String > > toolTipList = new ArrayList< Pair< Rectangle, String > >( 256 );
		for ( int i = 0; i < chartSelections.length; i++ ) {
			for ( final Short unitId : chartSelections[ i ] ) {
				final String unitName   = abilityCodes.UNIT_TYPE_NAME.get( unitId );
				final Object unitObject = AbilityCodes.UNIT_NAME_OBJECT_MAP.get( unitName );
				
				final Icon icon = unitObject == null ? nullGrayIcon
						: Icons.getEntityIcon( unitObject, params.iconSizesPS );
				
				final Point point = chartXPoints[ i ];
				if ( point.x + icon.getIconWidth() > params.chartX2 ) // End of chart reached...
					if ( point.y + ( icon.getIconHeight() << 1 ) < params.chartY2s[ i ] ) { // Is there space for more rows?
						point.x = params.chartX1 + 1;
						point.y += icon.getIconHeight();
					}
				icon.paintIcon( params.chartCanvas, g2, point.x, point.y );
				toolTipList.add( new Pair< Rectangle, String >( new Rectangle( point.x, point.y, icon.getIconWidth(), icon.getIconHeight() ), unitName ) );
				
				point.x += icon.getIconWidth();
			}
		}
		
		params.toolTipProvider = new StaticToolTipProvider( toolTipList );
		
		// Append the selection strings to the descriptions
		final Map< Short, String > unitTypeNameMap = params.replay.gameEvents.abilityCodes.UNIT_TYPE_NAME;
		for ( int i = 0; i < descriptionBuilders.length; i++ )
			descriptionBuilders[ i ].append( ", " ).append( Language.getText( "module.repAnalyzer.tab.charts.chartText.selection", PlayerSelectionTracker.getSelectionString( chartSelections[ i ], unitTypeNameMap ) ) );
	}
	
	/**
	 * Paints the chart descriptions.
	 */
	private void paintChartDescriptions() {
		final Font oldFont = g2.getFont();
		g2.setFont( new Font( oldFont.getName(), Font.BOLD, oldFont.getSize() ) );
		final int fontAscent = g2.getFontMetrics().getAscent();
		
		for ( int i = 0; i < descriptionBuilders.length; i++ ) {
			final Player player = params.replay.details.players[ params.chartPlayerIndices[ i ] ];
			final Color descriptionColor = player.getBrighterColor();
			g2.setColor( descriptionColor );
			
			final String playerDescription = descriptionBuilders[ i ].toString();
			int x = ChartParams.Y_AXIS_X_CONST + 3 + params.visibleRectangle.x;
			int y = params.chartY1s[ i ]-1 + ( params.allPlayersOnOneChart ? i * fontAscent + i : 0 );
			g2.drawString( playerDescription, x, y );
			
			// Chart legends
			List< Pair< Color, String > > legendPairList = null;
			switch ( params.chartType ) {
			case UNIT_TIERS :
				final UnitTier[] unitTiers = UnitTier.values();
				legendPairList = new ArrayList< Pair< Color,String > >( unitTiers.length );
				for ( final UnitTier unitTier : unitTiers )
					legendPairList.add( new Pair< Color, String >( unitTier.color, unitTier.textKey ) );
				break;
			case RESOURCE_SPENDING_RATE :
				legendPairList = new ArrayList< Pair< Color,String > >( 2 );
				legendPairList.add( new Pair< Color, String >( params.playerColors[ params.chartPlayerIndices[ i ] ], "module.repAnalyzer.tab.charts.chartText.mineralRsr" ) );
				legendPairList.add( new Pair< Color, String >( player.getBrighterColor()                            , "module.repAnalyzer.tab.charts.chartText.gasRsr"     ) );
				break;
			case RESOURCES_SPENT :
				legendPairList = new ArrayList< Pair< Color,String > >( 2 );
				legendPairList.add( new Pair< Color, String >( params.playerColors[ params.chartPlayerIndices[ i ] ], "module.repAnalyzer.tab.charts.chartText.minerals"   ) );
				legendPairList.add( new Pair< Color, String >( player.getBrighterColor()                            , "module.repAnalyzer.tab.charts.chartText.gas"        ) );
				break;
			case ACTION_SEQUENCES :
				legendPairList = new ArrayList< Pair< Color,String > >( 2 );
				legendPairList.add( new Pair< Color, String >( params.playerColors[ params.chartPlayerIndices[ i ] ], "module.repAnalyzer.tab.charts.chartText.nonHotkey"  ) );
				legendPairList.add( new Pair< Color, String >( player.getBrighterColor()                            , "module.repAnalyzer.tab.charts.chartText.hotkey"     ) );
				break;
			case PRODUCED_ARMY_SUPPLY :
				legendPairList = new ArrayList< Pair< Color,String > >( 2 );
				legendPairList.add( new Pair< Color, String >( params.playerColors[ params.chartPlayerIndices[ i ] ], "module.repAnalyzer.tab.charts.chartText.army"       ) );
				legendPairList.add( new Pair< Color, String >( player.getBrighterColor()                            , "module.repAnalyzer.tab.charts.chartText.supply"     ) );
				break;
			}
			if ( legendPairList != null ) {
				final FontMetrics fontMetrics = g2.getFontMetrics();
				x += fontMetrics.stringWidth( playerDescription ) + 10;
				g2.drawString( "(" , x, y );
				x += fontMetrics.stringWidth( "(" );
				final int legendSampleSize = BaseChartParams.CHART_TITLE_HEIGHT - 2;
				for ( int j = 0; j < legendPairList.size(); j++ ) {
					final Pair< Color, String > legendPair = legendPairList.get( j );
					g2.setColor( legendPair.value1 );
					g2.fillRoundRect( x, y - fontMetrics.getAscent() + 3, legendSampleSize, legendSampleSize, legendSampleSize >> 1, legendSampleSize >> 1 );
					x += legendSampleSize + 3;
					g2.setColor( descriptionColor );
					final String label = Language.getText( legendPair.value2 ) + ( j == legendPairList.size() - 1 ? ")" : ", " );
					g2.drawString( label, x, y );
					x += fontMetrics.stringWidth( label );
				}
			}
		}
		
		g2.setFont( oldFont );
	}
	
}
