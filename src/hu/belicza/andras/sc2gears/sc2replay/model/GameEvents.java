/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.sc2replay.model;

import java.util.List;

import hu.belicza.andras.sc2gears.sc2replay.AbilityCodes;
import hu.belicza.andras.sc2gears.sc2replay.PlayerSelectionTracker;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.EntityParams;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IGameEvents;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.AbilityGroup;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BuildingAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Research;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitTier;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Upgrade;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IAllianceAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IBaseUseAbilityAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IBuildAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.ICancelResRequestAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.ICustomAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IDecreaseGameSpeedAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IHotkeyAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IIncreaseGameSpeedAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.ILeaveGameAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IMoveScreenAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IRequestResourcesAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IResearchAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.ISaveGameAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.ISelectAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.ISendResourcesAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.ITrainAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.ITrainHallucinatedAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IUpgradeAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IUseBuildingAbilityAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IUseUnitAbilityAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IWarpAction;

/**
 * Contains the game events of a replay (select, order, right click, etc.).
 * 
 * <p>For format description see:
 * <ul><li><a href='http://code.google.com/p/phpsc2replay/'>PHP SC2Replay</a>
 * <li><a href='http://code.google.com/p/starcraft2replay/'>Starcraft 2 Replay</a>
 * </ul></p>
 * 
 * @author Andras Belicza
 */
public class GameEvents implements IGameEvents {
	
	/** Required for string representations.
	 * The names here have the same length (which is the max), left aligned, space-padded. */
	public String[] playerNames;
	/** Required for string representations. */
	public boolean  displayInSeconds;
	
	/** The actions of the game. */
	public Action[] actions;
	
	/** Reference to the ability codes applying to this replay. */
	public final AbilityCodes abilityCodes;
	
	/** Reference to the replay. */
	private final Replay replay;
	
	/**
	 * Tells if there were errors parsing the game events.<br>
	 * If replay was constructed from a replay specification, this tells if there were lines that could not be parsed.<br>
	 * If replay was taken from the replay cache, this is always <code>false</code>.
	 */
	public boolean errorParsing;
	
	/**
	 * Creates a new GameEvents.
	 * @param replay reference to the replay
	 */
	public GameEvents( final Replay replay, final AbilityCodes abilityCodes ) {
		this.replay = replay;
		final Player[] players = replay.details.players;
		int maxLength = 0;
		for ( final Player player : players )
			if ( maxLength < player.playerId.name.length() )
				maxLength = player.playerId.name.length();
		
		playerNames = new String[ players.length ];
		for ( int i = 0; i < playerNames.length; i++ )
			playerNames[ i ] = String.format( "  %-" + maxLength + "s  ", players[ i ].playerId.name );
		
		this.abilityCodes = abilityCodes;
	}
	
	/**
	 * Represents an Action.
	 * 
	 * @author Andras Belicza
	 */
	public class Action implements IAction {
		/** The frame when the action happened. */
		public int  frame;
		/** The player who issued the action.   */
		public byte player;
		/** Op code of the action.              */
		public byte opCode;
		/** Type of the action.                 */
		public ActionType type = ActionType.OTHER;
		/** Tells if this action is effective.  */
		public boolean effective;
		
		protected String toStringTemplate() {
			return ( displayInSeconds ? ReplayUtils.formatFramesShort( frame, replay.converterGameSpeed ) : String.format( "%6d", frame ) ) + playerNames[ player ];
		}
		
		@Override
		public final String toString() {
			final StringBuilder builder = new StringBuilder( displayInSeconds ? ReplayUtils.formatFramesShort( frame, replay.converterGameSpeed ) : String.format( "%6d", frame ) );
			builder.append( playerNames[ player ] );
			/*if ( this instanceof BaseUseAbilityAction ) // For debugging purposes
				builder.append( String.format( "{%6x} " , ( (BaseUseAbilityAction) this ).abilityCode ) );*/
			customToString( builder );
			return builder.toString();
		}
		
		public void customToString( final StringBuilder builder ) {
			builder.append( "0x" ).append( Integer.toHexString( opCode & 0xff ) );
		}
		
		@Override
		public int getFrame() {
			return frame;
		}
		
		@Override
		public int getPlayer() {
			return player;
		}
		
		@Override
		public byte getOpCode() {
			return opCode;
		}
		
		@Override
		public ActionType getActionType() {
			return type;
		}
		
		@Override
		public boolean isMacro() {
			return false;
		}
		
	}
	
	///////////////////////////////////////
	/////////// GENERAL ACTIONS ///////////
	///////////////////////////////////////
	
	public class HotkeyAction extends Action implements IHotkeyAction {
		public static final byte FLAG_OVERWRITE_SELECTION = 0x00;
		public static final byte FLAG_ADD_SELECTION       = 0x01;
		public static final byte FLAG_SELECT_GROUP        = 0x02;
		/** Flag indicating what kind of hotkey action. */
		public byte flag;
		// Units to remove from the control group if specified by a bitmap:
		public int     removalBitsCount;
		public byte [] removalUnitBitmap;
		public short[] removeIndices;
		public short[] retainIndices;
		
		@Override
		public void customToString( final StringBuilder builder ) {
			// The hotkey number is the upper 4 bits of the opcode.
			builder.append( "Hotkey " ).append( flag == FLAG_SELECT_GROUP ? "Select " : "Assign " ).append( ( opCode & 0xff ) >> 4 );
			if ( flag == FLAG_ADD_SELECTION )
				builder.append( " (add selection)" );
			if ( removeIndices != null )
				builder.append( " (remove " ).append( removeIndices.length ).append( removeIndices.length == 1 ? " unit)" : " units)" );
			if ( retainIndices != null )
				builder.append( " (retain " ).append( retainIndices.length ).append( retainIndices.length == 1 ? " unit)" : " units)" );
		}
		
		@Override
		public int getNumber() {
			return ( opCode & 0xff ) >> 4;
		}
		
		@Override
		public boolean isSelect() {
			return flag == HotkeyAction.FLAG_SELECT_GROUP;
		}
		
		@Override
		public boolean isMacro() {
			throw new UnsupportedOperationException( "Whether a hotkey action is macro depends on previous actions!" );
		}
		
		@Override
		public boolean isHotkeyAssignAdd() {
			return flag == HotkeyAction.FLAG_ADD_SELECTION;
		}
		
		@Override
		public boolean isHotkeyAssignOverwrite() {
			return flag == HotkeyAction.FLAG_OVERWRITE_SELECTION;
		}
		
		@Override
		public int getRemovalBitsCount() {
			return removalBitsCount;
		}
		
		@Override
		public byte[] getRemovalUnitBitmap() {
			return removalUnitBitmap;
		}
		
        @Override
        public short[] getRemoveIndices() {
	        return removeIndices;
        }
        
        @Override
        public short[] getRetainIndices() {
	        return retainIndices;
        }
		
	}
	
	public class SelectAction extends Action implements ISelectAction {
		public boolean automatic;
		public int     deselectionBitsCount;
		public byte [] deselectionUnitBitmap;
		public short[] unitTypes;
		public short[] unitsOfTypeCounts;
		public int  [] unitIds;
		public short[] removeIndices;
		public short[] retainIndices;
		
		public Short   deselectedUnitsCount;
		
		public SelectAction() {
			type = ActionType.SELECT;
		}
		
		@Override
		public void customToString( final StringBuilder builder ) {
			if ( automatic )
				builder.append( "Selection auto-update " );
			int unitIdIdx = 0;
			if ( unitTypes != null ) for ( int i = 0; i < unitTypes.length; i++ ) {
				builder.append( i == 0 ? "Select " : ", " );
				
				final String unitName = abilityCodes.UNIT_TYPE_NAME.get( unitTypes[ i ] );
				if ( unitName == null )
					builder.append( "Unknown[" ).append( Integer.toHexString( unitTypes[ i ] ) ).append( ']' );
				else
					builder.append( unitName );
				
				final int unitsOfTypeCount = unitsOfTypeCounts[ i ];
				if ( unitsOfTypeCount > 1 )
					builder.append( " x" ).append( unitsOfTypeCount );
				builder.append( " (" );
				for ( int j = unitsOfTypeCount; j > 0; j--, unitIdIdx++ )
					builder.append( Integer.toHexString( unitIds[ unitIdIdx ] ) ).append( ',' );
				builder.setCharAt( builder.length() - 1, ')' );
			}
			if ( deselectionBitsCount < 0 ) {
				if ( unitTypes != null && unitTypes.length > 0 )
					builder.append( ", " );
				builder.append( "Deselect all" );
			}
			else if ( getDeselectedUnitsCount() > 0 ) {
				if ( unitTypes != null && unitTypes.length > 0 )
					builder.append( ", " );
				builder.append( "Deselect " ).append( deselectedUnitsCount ).append( deselectedUnitsCount == 1 ? " unit" : " units" );
			}
			else if ( retainIndices != null ) {
				if ( unitTypes != null && unitTypes.length > 0 )
					builder.append( ", " );
				builder.append( "Retain " ).append( retainIndices.length ).append( retainIndices.length == 1 ? " unit" : " units" );
			}
		}
		
		@Override
		public boolean isMacro() {
			throw new UnsupportedOperationException( "Whether a select action is macro depends on previous actions!" );
		}
		
		@Override
		public boolean isAutomatic() {
			return automatic;
		}
		
		@Override
		public int getDeselectionBitsCount() {
			return deselectionBitsCount;
		}
		
		@Override
		public byte[] getDeselectionUnitBitmap() {
			return deselectionUnitBitmap;
		}
		
		@Override
		public short[] getUnitTypes() {
			return unitTypes;
		}
		
		@Override
		public short[] getUnitsOfTypeCounts() {
			return unitsOfTypeCounts;
		}
		
		@Override
		public int[] getUnitIds() {
			return unitIds;
		}
		
		@Override
		public short[] getRemoveIndices() {
			return removeIndices;
		}
		
		@Override
		public short[] getRetainIndices() {
			return retainIndices;
		}
		
		@Override
		public short getDeselectedUnitsCount() {
			if ( deselectedUnitsCount == null ) {
				if ( deselectionBitsCount > 0 ) {
					int removedCounter = 0;
					int bitmapElement  = 0;
					for ( int bitIndex = 0; bitIndex < deselectionBitsCount; bitIndex++ ) {
						if ( ( bitIndex & 0x07 ) == 0 )
							bitmapElement = deselectionUnitBitmap[ bitIndex >> 3 ] & 0xff;
						if ( ( bitmapElement & 0x80 ) != 0 )
							removedCounter++;
						bitmapElement <<= 1;
					}
					deselectedUnitsCount = (short) removedCounter;
				}
				else if ( removeIndices != null )
					deselectedUnitsCount = (short) removeIndices.length;
				else
					deselectedUnitsCount = 0;
			}
			return deselectedUnitsCount;
		}
		
	}
	
	public class MoveScreenAction extends Action implements IMoveScreenAction {
		public static final int FLAG_MASK_HAS_LOCATION      = 0x01;
		public static final int FLAG_MASK_HAS_DISTANCE      = 0x02;
		public static final int FLAG_MASK_HAS_PITCH         = 0x04;
		public static final int FLAG_MASK_HAS_YAW           = 0x08;
		public static final int FLAG_MASK_HAS_HEIGHT_OFFSET = 0x10;
		
		public int  x;
		public int  y;
		public byte flags;
		public int  distance;
		public int  pitch;         // This angle is relative to the horizontal plane, but the editor shows the angle relative to the vertical plane. Subtract from 90 degrees to convert
		public int  yaw;           // This angle is the vector from the camera head to the camera target projected on to the x-y plane in positive coordinates. So, default is 90 degrees, while insert and delete produce 45 and 135 degrees by default.
		public int  heightOffset;
		
		public MoveScreenAction() {
			type = ActionType.INACTION;
		}
		
		@Override
		public void customToString( final StringBuilder builder ) {
			builder.append( "Move screen " );
			if ( hasLocation() )
				builder.append( "x=" ).append( x >> 8 ).append( '.' ).append( ReplayUtils.DECIMAL_TABLE[ x & 0xff ] )
					  .append( ",y=" ).append( y >> 8 ).append( '.' ).append( ReplayUtils.DECIMAL_TABLE[ y & 0xff ] );
			if ( hasDistance() )
				builder.append( "; distance=" ).append( distance >> 8 ).append( '.' ).append( ReplayUtils.DECIMAL_TABLE[ distance & 0xff ] );
			if ( hasPitch() )
				builder.append( "; pitch=" ).append( pitch >> 8 ).append( '.' ).append( ReplayUtils.DECIMAL_TABLE[ pitch & 0xff ] ).append( '°' );
			if ( hasYaw() )
				builder.append( "; yaw=" ).append( yaw >> 8 ).append( '.' ).append( ReplayUtils.DECIMAL_TABLE[ yaw & 0xff ] ).append( '°' );
			if ( hasHeightOffset() )
				builder.append( "; height offset=" ).append( heightOffset >> 8 ).append( '.' ).append( ReplayUtils.DECIMAL_TABLE[ heightOffset & 0xff ] );
		}
		
		@Override
		public boolean hasLocation() {
			return ( flags & FLAG_MASK_HAS_LOCATION ) != 0;
		}
		
		@Override
		public int getX() {
			return x;
		}
		
		@Override
		public int getY() {
			return y;
		}
		
		@Override
		public boolean hasDistance() {
			return ( flags & FLAG_MASK_HAS_DISTANCE ) != 0;
		}
		
		@Override
		public int getDistance() {
			return distance;
		}
		
		@Override
		public boolean hasPitch() {
			return ( flags & FLAG_MASK_HAS_PITCH ) != 0;
		}
		
		@Override
		public int getPitch() {
			return pitch;
		}
		
		@Override
		public boolean hasYaw() {
			return ( flags & FLAG_MASK_HAS_YAW ) != 0;
		}
		
		@Override
		public int getYaw() {
			return yaw;
		}
		
		@Override
		public boolean hasHeightOffset() {
			return ( flags & FLAG_MASK_HAS_HEIGHT_OFFSET ) != 0;
		}
		
		@Override
		public int getHeightOffset() {
			return heightOffset;
		}
		
	}
	
	public class RequestResoucesAction extends Action implements IRequestResourcesAction {
		public int minRequested;
		public int gasRequested;
		
		@Override
		public void customToString( final StringBuilder builder ) {
			builder.append( "Request resources; minerals: " ).append( minRequested ).append( ", gas: " ).append( gasRequested );
		}
		
		@Override
		public boolean isMacro() {
			return true;
		}
		
		@Override
		public int getMineralsRequested() {
			return minRequested;
		}
		
		@Override
		public int getGasRequested() {
			return gasRequested;
		}
		
	}
	
	public class SendResourcesAction extends Action implements ISendResourcesAction {
		public int targetPlayer;
		public int minSent;
		public int gasSent;
		
		@Override
		public void customToString( final StringBuilder builder ) {
			builder.append( "Send resources to " ).append( replay.details.players[ targetPlayer ].playerId.name )
				.append( "; minerals: " ).append( minSent ).append( ", gas: " ).append( gasSent );
		}
		
		@Override
		public boolean isMacro() {
			return true;
		}
		
		@Override
		public int getTargetPlayer() {
			return targetPlayer;
		}
		
		@Override
		public int getGasSent() {
			return gasSent;
		}
		
		@Override
		public int getMineralsSent() {
			return minSent;
		}
		
	}
	
	public class CancelResRequestAction extends Action implements ICancelResRequestAction {
		
		@Override
		public void customToString( final StringBuilder builder ) {
			builder.append( "Cancel resources request" );
		}
		
		@Override
		public boolean isMacro() {
			return true;
		}
		
	}
	
	public class LeaveGameAction extends Action implements ILeaveGameAction {
		
		@Override
		public void customToString( final StringBuilder builder ) {
			builder.append( "Leave game" );
		}
		
	}
	
	public class SaveGameAction extends Action implements ISaveGameAction {
		public String fileName;
		
		public SaveGameAction() {
			type = ActionType.INACTION;
		}
		
		@Override
		public void customToString( final StringBuilder builder ) {
			builder.append( "Save game; file: \"" ).append( fileName ).append( '"' );
		}
		
        @Override
        public String getFileName() {
	        return fileName;
        }
		
	}
	
	public class AllianceAction extends Action implements IAllianceAction {
		
		@Override
		public void customToString( final StringBuilder builder ) {
			builder.append( "Set Alliance" );
		}
		
	}
	
	public class IncreaseGameSpeedAction extends Action implements IIncreaseGameSpeedAction {
		
		public IncreaseGameSpeedAction() {
			type = ActionType.INACTION;
		}
		
		@Override
		public void customToString( final StringBuilder builder ) {
			builder.append( "Increase Game Speed" );
		}
		
	}
	
	public class DecreaseGameSpeedAction extends Action implements IDecreaseGameSpeedAction {
		
		public DecreaseGameSpeedAction() {
			type = ActionType.INACTION;
		}
		
		@Override
		public void customToString( final StringBuilder builder ) {
			builder.append( "Decrease Game Speed" );
		}
	}
	
	///////////////////////////////////////
	/////////// USE ABILITY ACTIONS ///////
	///////////////////////////////////////
	
	public class BaseUseAbilityAction extends Action implements IBaseUseAbilityAction {
		public static final int FLAG_MASK_QUEUED           = 0x000002;
		public static final int FLAG_MASK_RIGHT_CLICK      = 0x000008;
		public static final int FLAG_MASK_WIREFRAME_CLICK  = 0x000020;
		public static final int FLAG_MASK_TOGGLE_ABILITY   = 0x000040;
		public static final int FLAG_MASK_AUTOCAST         = 0x000080;
		public static final int FLAG_MASK_ABILITY_USED     = 0x000100;
		public static final int FLAG_MASK_WIREFRAME_UNLOAD = 0x000200;
		public static final int FLAG_MASK_WIREFRAME_CANCEL = 0x000400;
		public static final int FLAG_MASK_MINIMAP_CLICK    = 0x010000;
		public static final int FLAG_MASK_ABILITY_FAILED   = 0x020000;
		
		public int           abilityCode;
		public String        abilityName;
		public AbilityGroup  abilityGroup;
		public int   flags;
		public int   targetId   = -1;
		public short targetType = -1;
		public int   targetX    = 256; // Compatibility reason (with very old versions) 
		public int   targetY    = 256; // Compatibility reason (with very old versions)
		public final Boolean macro;
		
		public BaseUseAbilityAction() {
			macro = null;
		}
		
		public BaseUseAbilityAction( final String abilityName, final AbilityGroup abilityGroup, final Boolean macro ) {
			this.abilityName  = abilityName;
			this.abilityGroup = abilityGroup;
			this.macro        = macro;
		}
		
		@Override
		public final void customToString( final StringBuilder builder ) {
			if ( isQueued() )
				builder.append( "[Queued] " );
			if ( isWireframeClick() )
				builder.append( "[WireframeClick] " );
			if ( isToggleAbility() )
				builder.append( "[Toggle] " );
			if ( isAutocast() )
				builder.append( "[Autocast] " );
			if ( isWireframeUnload() )
				builder.append( "[WireframeUnload] " );
			if ( isWireframeCancel() )
				builder.append( "[WireframeCancel] " );
			if ( isMinimapClick() )
				builder.append( "[MinimapClick] " );
			if ( isAbilityFailed() )
				builder.append( "[Failed] " );
			customAbilToString( builder );
			if ( targetId != -1 ) {
				builder.append( "; target: " );
				final String unitName = abilityCodes.UNIT_TYPE_NAME.get( (short) targetType );
				if ( unitName == null )
					builder.append( "Unknown[" ).append( targetType == -1 ? "-1" : Integer.toHexString( targetType ) ).append( ']' );
				else
					builder.append( unitName );
				
				builder.append( " (" ).append( Integer.toHexString( targetId ) ).append( ')' );
			}
			if ( targetX != 256 || targetY != 256 ) { 
				builder.append( "; target: " );
				builder.append( "x=" ).append( ReplayUtils.formatCoordinate( targetX ) ).append( ",y=" ).append( ReplayUtils.formatCoordinate( targetY ) );
			}
		}
		
		protected void customAbilToString( final StringBuilder builder ) {
			// For debugging:
			// builder.append( String.format( "[%6x] " , abilityCode ) );
			if ( abilityName == null )
				builder.append( "Use ability " ).append( String.format( "%6x" , abilityCode ) );
			else
				builder.append( abilityName );
		}
		
		@Override
		public boolean isQueued() {
			return ( flags & FLAG_MASK_QUEUED ) != 0;
		}
		
		@Override
		public boolean isRightClick() {
			return ( flags & FLAG_MASK_RIGHT_CLICK ) != 0;
		}
		
		@Override
		public boolean isWireframeClick() {
			return ( flags & FLAG_MASK_WIREFRAME_CLICK ) != 0;
		}
		
		@Override
		public boolean isToggleAbility() {
			return ( flags & FLAG_MASK_TOGGLE_ABILITY ) != 0;
		}
		
		@Override
		public boolean isAutocast() {
			return ( flags & FLAG_MASK_AUTOCAST ) != 0;
		}
		
		@Override
		public boolean isWireframeUnload() {
			return ( flags & FLAG_MASK_WIREFRAME_UNLOAD ) != 0;
		}
		
		@Override
		public boolean isWireframeCancel() {
			return ( flags & FLAG_MASK_WIREFRAME_CANCEL ) != 0;
		}
		
		@Override
		public boolean isMinimapClick() {
			return ( flags & FLAG_MASK_MINIMAP_CLICK ) != 0;
		}
		
		@Override
		public boolean isAbilityFailed() {
			return ( flags & FLAG_MASK_ABILITY_FAILED ) != 0;
		}
		
		@Override
		public boolean hasTargetUnit() {
			return targetId != -1; 
		}
		
		@Override
		public boolean hasTargetPoint() {
			return targetX != 256 || targetY != 256; 
		}
		
		@Override
		public EntityParams getEntityParams() {
			return abilityCodes.COMMON_ABILITY_PARAMS.get( abilityCode );
		}
		
		@Override
		public boolean isMacro() {
			if ( macro != null )
				return macro; // If it is a common ability, the macro property is specified externally
			
			final EntityParams entityParams = getEntityParams();
			if ( entityParams == null )
				return false;
			
			return entityParams.minerals != 0 || entityParams.gas != 0; // Can be negative too!
		}
		
		public UnitTier getUnitTier() {
			return null;
		}
		
		@Override
		public int getAbilityCode() {
			return abilityCode;
		}
		
		@Override
		public String getAbilityName() {
			return abilityName;
		}
		
		@Override
		public AbilityGroup getAbilityGroup() {
			return abilityGroup;
		}
		
		@Override
		public int getTargetId() {
			return targetId;
		}
		
		@Override
		public int getTargetType() {
			return targetType;
		}
		
		@Override
		public int getTargetX() {
			return targetX;
		}
		
		@Override
		public int getTargetY() {
			return targetY;
		}
		
	}
	
	public class TrainAction extends BaseUseAbilityAction implements ITrainAction {
		public final Unit unit;
		
		public TrainAction( final Unit unit ) {
			type      = ActionType.TRAIN;
			this.unit = unit;
		}
		
		@Override
		protected void customAbilToString( final StringBuilder builder ) {
			builder.append( "Train " ).append( unit.stringValue );
		}
		
		@Override
		public EntityParams getEntityParams() {
			return abilityCodes.UNIT_PARAMS.get( unit );
		}
		
		@Override
		public UnitTier getUnitTier() {
			return unit.unitTier;
		}
		
		@Override
		public Unit getUnit() {
			return unit;
		}
	}
	
	public class WarpAction extends TrainAction implements IWarpAction {
		
		public WarpAction( final Unit unit ) {
			super( unit );
		}
		
		@Override
		protected void customAbilToString( final StringBuilder builder ) {
			builder.append( "Train " ).append( unit.stringValue ).append( " (Warp gate)" );
		}
		
		@Override
		public EntityParams getEntityParams() {
			return abilityCodes.WARPED_UNIT_PARAMS.get( unit );
		}
		
	}
	
	public class TrainHallucinatedAction extends TrainAction implements ITrainHallucinatedAction {
		
		public TrainHallucinatedAction( final Unit unit ) {
			super( unit );
		}
		
		@Override
		protected void customAbilToString( final StringBuilder builder ) {
			builder.append( "Train " ).append( unit.stringValue ).append( " Hallucination (Sentry)" );
		}
		
		@Override
		public EntityParams getEntityParams() {
			return null;
		}
		
		@Override
		public UnitTier getUnitTier() {
			return null;
		}
		
	}
	
	public class BuildAction extends BaseUseAbilityAction implements IBuildAction {
		public final Building building;
		
		public BuildAction( final Building building ) {
			type          = ActionType.BUILD;
			this.building = building;
		}
		
		@Override
		protected void customAbilToString( final StringBuilder builder ) {
			builder.append( "Build " ).append( building.stringValue );
		}
		
		@Override
		public EntityParams getEntityParams() {
			return abilityCodes.BUILDING_PARAMS.get( building );
		}
		
		@Override
		public Building getBuilding() {
			return building;
		}
		
	}
	
	public class UpgradeAction extends BaseUseAbilityAction implements IUpgradeAction {
		public final Upgrade upgrade;
		
		public UpgradeAction( final Upgrade upgrade ) {
			type         = ActionType.UPGRADE;
			this.upgrade = upgrade;
		}
		
		@Override
		protected void customAbilToString( final StringBuilder builder ) {
			builder.append( "Upgrade " ).append( upgrade.stringValue );
		}
		
		@Override
		public EntityParams getEntityParams() {
			return abilityCodes.UPGRADE_PARAMS.get( upgrade );
		}
		
		@Override
		public Upgrade getUpgrade() {
			return upgrade;
		}
	}
	
	public class ResearchAction extends BaseUseAbilityAction implements IResearchAction {
		public final Research research;
		
		public ResearchAction( final Research research ) {
			type          = ActionType.RESEARCH;
			this.research = research;
		}
		
		@Override
		protected void customAbilToString( final StringBuilder builder ) {
			builder.append( "Research " ).append( research.stringValue );
		}
		
		@Override
		public EntityParams getEntityParams() {
			return abilityCodes.RESEARCH_PARAMS.get( research );
		}
		
		@Override
		public Research getResearch() {
			return research;
		}
		
	}
	
	public class UseUnitAbilityAction extends BaseUseAbilityAction implements IUseUnitAbilityAction {
		public final UnitAbility unitAbility;
		public final Unit        unit;
		
		public UseUnitAbilityAction( final UnitAbility unitAbility, final Unit unit ) {
			this.unitAbility = unitAbility;
			this.unit        = unit;
			abilityGroup     = unitAbility.abilityGroup;
		}
		
		@Override
		protected void customAbilToString( final StringBuilder builder ) {
			builder.append( unitAbility ).append( " (" ).append( unit.stringValue ).append( ')' );
		}
		
		@Override
		public EntityParams getEntityParams() {
			return abilityCodes.UNIT_ABILITY_PARAMS.get( unitAbility );
		}
		
		@Override
		public UnitTier getUnitTier() {
			return unitAbility.transformationTargetUnit == null ? null : unitAbility.transformationTargetUnit.unitTier;
		}
		
		@Override
		public Unit getUnit() {
			return unit;
		}
		
		@Override
		public UnitAbility getUnitAbility() {
			return unitAbility;
		}
		
	}
	
	public class UseBuildingAbilityAction extends BaseUseAbilityAction implements IUseBuildingAbilityAction {
		public final BuildingAbility buildingAbility;
		public final Building        building;
		
		public UseBuildingAbilityAction( final BuildingAbility buildingAbility, final Building building ) {
			this.buildingAbility = buildingAbility;
			this.building        = building;
			abilityGroup         = buildingAbility.abilityGroup;
		}
		
		@Override
		protected void customAbilToString( final StringBuilder builder ) {
			builder.append( buildingAbility.stringValue ).append( " (" ).append( building.stringValue ).append( ')' );
		}
		
		@Override
		public EntityParams getEntityParams() {
			return abilityCodes.BUILDING_ABILITY_PARAMS.get( buildingAbility );
		}
		
		@Override
		public Building getBuilding() {
			return building;
		}
		
		@Override
		public BuildingAbility getBuildingAbility() {
			return buildingAbility;
		}
		
	}
	
	public class CustomAction extends Action implements ICustomAction {
		public final String actionName;
		
		public CustomAction( final String actionName ) {
			this.actionName = actionName;
			type = ActionType.INACTION;
		}
		
		@Override
		public void customToString( final StringBuilder builder ) {
			builder.append( actionName );
		}
		
		@Override
		public String getActionName() {
			return actionName;
		}
	}
	
	@Override
	public boolean isDisplayInSeconds() {
		return displayInSeconds;
	}
	
	@Override
	public void setDisplayInSeconds( final boolean displayInSeconds ) {
		this.displayInSeconds = displayInSeconds;
	}
	
    @Override
    public boolean isErrorParsing() {
	    return errorParsing;
    }
	
	@Override
	public IAction[] getActions() {
		return actions;
	}
	
	@Override
	public String getUnitName( final short unitType ) {
		return abilityCodes.UNIT_TYPE_NAME.get( unitType );
	}
	
	@Override
	public String getSelectionString( final List< Short > selection ) {
		return PlayerSelectionTracker.getSelectionString( selection, abilityCodes.UNIT_TYPE_NAME );
	}
	
	@Override
	public boolean isSelectionMacro( final List< Short > selection ) {
		return PlayerSelectionTracker.isSelectionMacro( selection, abilityCodes );
	}
	
}
