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
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.AbilityGroup;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BuildingAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Research;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.UnitAbility;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Upgrade;

/**
 * A {@link VersionCompatibility#V_1_5} compatible replay parser.
 * 
 * @author Andras Belicza
 */
class ReplayParserV15 extends ReplayParserV11 {
	
	/**
	 * Creates a new ReplayParserV15.
	 * @param fileName             name of the replay file
	 * @param replay               reference to the replay
	 * @param versionCompatibility version compatibility of the replay
	 */
	public ReplayParserV15( final String fileName, final Replay replay, final VersionCompatibility versionCompatibility ) {
		super( fileName, replay, versionCompatibility );
	}
	
	@Override
	protected BaseUseAbilityAction readUseAbilityAction( final AbilityCodes abilityCodes ) {
		
		BaseUseAbilityAction bua = null;
		
		final BitInputStream bitin = new BitInputStream( wrapper );
		
		final int flags = bitin.readBits( useAbilityFlagsLength ); // 13 bits before 1.3.3, 14 bits from 1.3.3 up to 1.5.0; 16 bits from 1.5.0
		
		//final boolean queued          = ( flags & 0x000002 ) != 0;
		final boolean rightClick      = ( flags & 0x000008 ) != 0;
		//final boolean wireframeClick  = ( flags & 0x000020 ) != 0;
		final boolean toggleAbility   = ( flags & 0x000040 ) != 0;
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
			int abilityCode = bitin.readBits( 21 );
			if ( toggleAbility )
				abilityCode |= 0x100000; // Include toggle bit in ability code
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
			// From version 1.4 extra data: 1 bit boolean and optionally 4 team bits
			final boolean targetHasTeam = bitin.readBoolean();
			if ( targetHasTeam )
				bitin.readBits( 4 ); // target team
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
	
}
