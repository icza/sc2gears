/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.sc2replay;

import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.VersionCompatibility;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.BaseUseAbilityAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.HotkeyAction;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.SelectAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.AbilityGroup;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BuildingAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Research;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Upgrade;

/**
 * A {@link VersionCompatibility#V_1_1} compatible replay parser.
 * 
 * <p>Info source: <a href="https://github.com/Mischanix/sc2replay-csharp">Mischanix's sc2replay-csharp project</a></p>
 * 
 * @author Andras Belicza
 */
class ReplayParserV11 extends ReplayParser {
	
	/**
	 * Creates a new ReplayParserV11.
	 * @param fileName             name of the replay file
	 * @param replay               reference to the replay
	 * @param versionCompatibility version compatibility of the replay
	 */
	public ReplayParserV11( final String fileName, final Replay replay, final VersionCompatibility versionCompatibility ) {
		super( fileName, replay, versionCompatibility );
	}
	
	@Override
	protected HotkeyAction readHotkeyAction() {
		final HotkeyAction ha = replay.gameEvents.new HotkeyAction();
		
		final BitInputStream bitin = new BitInputStream( wrapper );
		
		ha.flag = (byte) bitin.readBits( 2 );
		if ( ha.flag == HotkeyAction.FLAG_SELECT_GROUP )
			ha.type = ActionType.SELECT;
		
		final int updateType = bitin.readBits( 2 );
		switch ( updateType ) {
		case 0x01 : {
			// Remove from control group by flags: deselection map follows
			ha.removalBitsCount  = bitin.readBits( idxTypeBitLength );
			ha.removalUnitBitmap = new byte[ ( ha.removalBitsCount + 7 ) >> 3 ];
			
			for ( int i = 0; i < ha.removalUnitBitmap.length - 1; i++ )
				ha.removalUnitBitmap[ i ] = (byte) bitin.read();
			// Last bitmap byte, might not be "full" 8 bits:
			ha.removalUnitBitmap[ ha.removalUnitBitmap.length - 1 ] = (byte) ( ( ha.removalBitsCount & 0x07 ) == 0 ? bitin.read() : bitin.readBits( ha.removalBitsCount & 0x07 ) << ( 8 - ( ha.removalBitsCount & 0x07 ) ) );
			
			break;
		}
		case 0x02 : {
			// To indiciate "Remove all":
			ha.removalBitsCount = -1;
			// Some units removed from the control group
			final int count = bitin.readBits( idxTypeBitLength );
			if ( count > 0 ) {
    			ha.removeIndices = new short[ count ];
    			// Removed unit indices
    			for ( int i = 0; i < count; i++ )
    				ha.removeIndices[ i ] = (short) bitin.readBits( idxTypeBitLength );
			}
			break;
		}
		case 0x03 : {
			// To indiciate "Remove all":
			ha.removalBitsCount = -1;
			// Replace control group with portion of the control group
			final int count = bitin.readBits( idxTypeBitLength );
			if ( count > 0 ) {
    			ha.retainIndices = new short[ count ];
    			// Remaining unit indices
    			for ( int i = 0; i < count; i++ )
    				ha.retainIndices[ i ] = (short) bitin.readBits( idxTypeBitLength );
			}
			break;
		}
		}
		
		return ha;
	}
	
	@Override
	protected void readCustomSelectAction( final SelectAction sa ) {
		final BitInputStream bitin = new BitInputStream( wrapper );
		
		//final int wireframeIdx         = bitin.getBits( 4 ); // This is part of the opcode
		@SuppressWarnings( "unused" )
        final int wireframeSubgroupIdx = bitin.readBits( idxTypeBitLength );
		
		final int updateFlags = bitin.readBits( 2 );
		switch ( updateFlags ) {
		case 0x01 : {
			sa.deselectionBitsCount  = bitin.readBits( idxTypeBitLength );
			sa.deselectionUnitBitmap = new byte[ ( sa.deselectionBitsCount + 7 ) >> 3 ];
			
			for ( int i = 0; i < sa.deselectionUnitBitmap.length - 1; i++ )
				sa.deselectionUnitBitmap[ i ] = (byte) bitin.read();
			// Last bitmap byte, might not be "full" 8 bits:
			sa.deselectionUnitBitmap[ sa.deselectionUnitBitmap.length - 1 ] = (byte) ( ( sa.deselectionBitsCount & 0x07 ) == 0 ? bitin.read() : bitin.readBits( sa.deselectionBitsCount & 0x07 ) << ( 8 - ( sa.deselectionBitsCount & 0x07 ) ) );
			
			break;
		}
		case 0x02 : {
			// To indiciate "Deselect all":
			sa.deselectionBitsCount = -1;
			// Units are removed
			final int count = bitin.readBits( idxTypeBitLength );
			if ( count > 0 ) {
    			sa.removeIndices = new short[ count ];
    			// Deselected unit indices
    			for ( int i = 0; i < count; i++ )
    				sa.removeIndices[ i ] = (short) bitin.readBits( idxTypeBitLength );
			}
			break;
		}
		case 0x03 : {
			// To indiciate "Deselect all":
			sa.deselectionBitsCount = -1;
			// Units are retained
			final int count = bitin.readBits( idxTypeBitLength );
			if ( count > 0 ) {
    			sa.retainIndices = new short[ count ];
    			// Retained unit indices
    			for ( int i = 0; i < count; i++ )
    				sa.retainIndices[ i ] = (short) bitin.readBits( idxTypeBitLength );
			}
			break;
		}
		}
		
		final int unitTypesCount = bitin.readBits( idxTypeBitLength );
		
		if ( unitTypesCount > 0 ) {
			sa.unitTypes         = new short[ unitTypesCount ];
			sa.unitsOfTypeCounts = new short[ unitTypesCount ];
			final boolean belowVer20  = versionCompatibility.compareTo( VersionCompatibility.V_2_0 ) > 0;
			for ( int i = 0; i < unitTypesCount; i++ ) {
				sa.unitTypes        [ i ] = bitin.readShort();
				if ( belowVer20 )
					bitin.read(); // unknown
				else
					bitin.readShort(); // from 2.0 2 bytes unknown
				sa.unitsOfTypeCounts[ i ] = (short) bitin.readBits( idxTypeBitLength );
			}
		}
		final int totalUnitsCount = bitin.readBits( idxTypeBitLength );
		if ( totalUnitsCount > 0 ) {
			sa.unitIds = new int[ totalUnitsCount ];
			for ( int i = 0; i < totalUnitsCount; i++ )
				sa.unitIds[ i ] = twistId( bitin.readInt() );
		}
	}
	
	@Override
	protected BaseUseAbilityAction readUseAbilityAction( final AbilityCodes abilityCodes ) {
		
		// First determine how the "old" ability code looked because the ability code repository contains the old codes:
		wrapper.get(); wrapper.get();
		final int abilityCode = ( ( wrapper.get() & 0xff ) << 16 ) + ( ( wrapper.get() & 0xff ) << 8 ) + ( wrapper.get() & ( versionCompatibility.compareTo( VersionCompatibility.V_1_3_3 ) <= 0 ? 0x7f : 0x3f ) );
		wrapper.position( wrapper.position() - 5 ); // Properly rewind...
		
		BaseUseAbilityAction bua = null;
		
		final BitInputStream bitin = new BitInputStream( wrapper );
		
		final int flags = bitin.readBits( useAbilityFlagsLength );
		
		//final boolean queued          = ( flags & 0x000002 ) != 0;
		final boolean rightClick      = ( flags & 0x000008 ) != 0;
		//final boolean wireframeClick  = ( flags & 0x000020 ) != 0;
		//final boolean toggleAbility   = ( flags & 0x000040 ) != 0;
		//final boolean enableAutoCast  = ( flags & 0x000080 ) != 0;
		//final boolean abilityUsed     = ( flags & 0x000100 ) != 0;
		//final boolean wireframeUnload = ( flags & 0x000200 ) != 0;
		//final boolean wireframeCancel = ( flags & 0x000400 ) != 0;
		// The next 2 is part of the Opcode, and is not yet merged into flags!
		//final boolean minimapClick    = ( flags & 0x010000 ) != 0;
		//final boolean abilityFailed   = ( flags & 0x020000 ) != 0;  // Could this be what I dream about? That the ability failed due to insufficient resources and such??! 		
		
		final boolean defaultAbility = bitin.readBoolean();
		if ( defaultAbility ) {
			// 16+5 bits, I read them as one
			//bitin.getShort();    // type id
			//bitin.getBits( 5 );  // button id (on command card)
			@SuppressWarnings( "unused" )
			int abilityCode_ = bitin.readBits( 21 );
			final boolean defaultActor = bitin.readBoolean();
			if ( defaultActor ) {
				// Should something come here
			}
			
			Unit     unit     = null;
			Building building = null;
			Upgrade  upgrade  = null;
			Research research = null;
			Object[] abilityParams;
			final GameEvents gameEvents = replay.gameEvents;
			// Try to check in the frequency order...
			if      ( ( abilityParams = abilityCodes.COMMON_BASE_ABILITY_CODES.get( abilityCode ) ) != null )
				bua = gameEvents.new BaseUseAbilityAction( (String) abilityParams[ 0 ], (AbilityGroup) abilityParams[ 1 ], (Boolean) abilityParams[ 2 ] );
			else if ( ( unit          = abilityCodes.TRAIN_ABILITY_CODES      .get( abilityCode ) ) != null )
				bua = gameEvents.new TrainAction( unit );
			else if ( ( building      = abilityCodes.BUILD_ABILITY_CODES      .get( abilityCode ) ) != null )
				bua = gameEvents.new BuildAction( building );
			else if ( ( abilityParams = abilityCodes.USE_UNIT_ABILITY         .get( abilityCode ) ) != null )
				bua = gameEvents.new UseUnitAbilityAction( (UnitAbility) abilityParams[ 0 ], (Unit) abilityParams[ 1 ] );
			else if ( ( abilityParams = abilityCodes.USE_BUILDING_ABILITY     .get( abilityCode ) ) != null )
				bua = gameEvents.new UseBuildingAbilityAction( (BuildingAbility) abilityParams[ 0 ], (Building) abilityParams[ 1 ] );
			else if ( ( unit          = abilityCodes.WARP_ABILITY_CODES       .get( abilityCode ) ) != null )
				bua = gameEvents.new WarpAction( unit );
			else if ( ( unit          = abilityCodes.TRAIN_HALLU_ABILITY_CODES.get( abilityCode ) ) != null )
				bua = gameEvents.new TrainHallucinatedAction( unit );
			else if ( ( upgrade       = abilityCodes.UPGRADE_ABILITY_CODES    .get( abilityCode ) ) != null )
				bua = gameEvents.new UpgradeAction( upgrade );
			else if ( ( research      = abilityCodes.RESEARCH_ABILITY_CODES   .get( abilityCode ) ) != null )
				bua = gameEvents.new ResearchAction( research );
			else
				bua = gameEvents.new BaseUseAbilityAction();
			
			( (BaseUseAbilityAction) bua ).abilityCode = abilityCode;
			
		}
		else {
			if ( rightClick )
				bua = replay.gameEvents.new BaseUseAbilityAction( "Right click", null, Boolean.FALSE );
			else
				bua = replay.gameEvents.new BaseUseAbilityAction();
		}
		
		bua.flags = ( opCode & 0xf0 ) << ( useAbilityFlagsLength - 4 ) | flags;
		
		final int targetType = bitin.readBits( 2 );
		switch ( targetType ) {
		case 0x01 : {
			// Location target
			bua.targetX = bitin.readBits( 20 ) << 4;
			bua.targetY = bitin.readBits( 20 ) << 4;
			bitin.readInt(); // z?
			break;
		}
		case 0x02 : {
			// Unit + Location target
			@SuppressWarnings( "unused" )
			final int targetFlags  = bitin.read();
			@SuppressWarnings( "unused" )
			final int wireframeIdx = bitin.read();
			bua.targetId   = twistId( bitin.readInt() );
			bua.targetType = bitin.readShort();
			final boolean targetHasPlayer = bitin.readBoolean();
			if ( targetHasPlayer )
				bitin.readBits( 4 ); // target player
			// From version 1.4 extra data:
			if ( versionCompatibility.compareTo( VersionCompatibility.V_1_4 ) <= 0 ) {
    			final boolean targetHasTeam = bitin.readBoolean();
    			if ( targetHasTeam )
    				bitin.readBits( 4 ); // target team
			}
			bua.targetX = bitin.readBits( 20 ) << 4;
			bua.targetY = bitin.readBits( 20 ) << 4;
			bitin.readInt(); // z?
			break;
		}
		case 0x03 : {
			// Unit target
			bua.targetId = twistId( bitin.readInt() );
			break;
		}
		}
		
		bitin.readBoolean(); // Last bit, should be 0
		
		return bua;
	}
	
	/**
	 * There are usually 3 zeros inside the hex representation of unit ids.
	 * This method twists the bytes of the id so those zeros goes to the front and therefore the hex representation will be much shorter.
	 * 
	 * @param id id to twist
	 * @return the twisted id
	 */
	protected static int twistId( final int id ) {
		return ( id & 0x0000ffff ) << 16 | ( id & 0xffff0000 ) >>> 16; // Unsigned shift in case the sign bit is 1
	}
	
}
