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
 * A {@link VersionCompatibility#V_1_0} compatible replay parser.
 * 
 * @author Andras Belicza
 */
class ReplayParserV10 extends ReplayParser {
	
	/**
	 * Creates a new ReplayParserV10.
	 * @param fileName             name of the replay file
	 * @param replay               reference to the replay
	 * @param versionCompatibility version compatibility of the replay
	 */
	public ReplayParserV10( final String fileName, final Replay replay, final VersionCompatibility versionCompatibility ) {
		super( fileName, replay, versionCompatibility );
	}
	
	@Override
	protected HotkeyAction readHotkeyAction() {
		final HotkeyAction ha = replay.gameEvents.new HotkeyAction();
		
		final int flag = wrapper.get() & 0xff;
		ha.flag = (byte) ( flag & 0x03 );
		if ( ha.flag == HotkeyAction.FLAG_SELECT_GROUP )
			ha.type = ActionType.SELECT;
		// Size of rest           : flag >> 3
		// Consider extra byte    : 0x04 mask on the flag
		// There is an extra-extra: if 0x04 mask on the flag and 0x06 mask on the next byte
		// Examples:                     -- Next action:
		// 1d 16 f8 ff 07                -- 00 24 0b
		// 1d 0e 80 00                   -- 00 23 0b
		// 1d 36 06 00 00 00 00 00 10 00 -- 00 22 ac
		if ( flag > 3 )
			// It means we surely have extra bytes, so it's safe to read the next
			// which is required to properly determine the number of extra bytes
			wrapper.position( wrapper.position() + ( flag >> 3 ) + ( ( flag & 0x04 ) == 0x04 ? ( wrapper.get( wrapper.position() ) & 0x06 ) == 0x06 ? 2 : 1 : 0 ) ); // Notice the absolute get()!
		
		return ha;
	}
	
	@Override
	protected void readCustomSelectAction( final SelectAction sa ) {
		wrapper.get(); // Flags, sometimes its the length of the action (eventDataLength): wrapper.position( wrapper.position() + eventDataLength ); 
		sa.deselectionBitsCount = wrapper.get() & 0xff;
		if ( ( sa.deselectionBitsCount & 0x07 ) == 0 ) {
			// Event is byte aligned
			// Read deselection unit bitmap
			if ( sa.deselectionBitsCount > 0 ) {
				sa.deselectionUnitBitmap = new byte[ sa.deselectionBitsCount >> 3 ];
				for ( int i = 0; i < sa.deselectionUnitBitmap.length; i++ )
					sa.deselectionUnitBitmap[ i ] = wrapper.get();
			}
			final int unitTypesCount = wrapper.get() & 0xff;
			sa.unitTypes         = new short[ unitTypesCount ];
			sa.unitsOfTypeCounts = new short[ unitTypesCount ];
			for ( int i = 0; i < unitTypesCount; i++ ) {
				sa.unitTypes        [ i ] = (short) ( ( ( wrapper.get() & 0xff ) << 8 ) + ( wrapper.get() & 0xff ) );
				wrapper.get(); // unknown
				sa.unitsOfTypeCounts[ i ] = (short) ( wrapper.get() & 0xff );
			}
			final int totalUnitsCount = wrapper.get() & 0xff;
			sa.unitIds = new int[ totalUnitsCount ];
			for ( int i = 0; i < totalUnitsCount; i++ ) {
				sa.unitIds[ i ] = ( wrapper.get() & 0xff ) << 8 | ( wrapper.get() & 0xff ) | ( wrapper.get() & 0xff ) << 24 | ( wrapper.get() & 0xff ) << 16;
			}
		}
		else {
			// Real bitstream (not byte aligned)
			// Read deselection unit bitmap
			sa.deselectionUnitBitmap = new byte[ ( sa.deselectionBitsCount + 7 ) >> 3 ];
			for ( int i = 0; i < sa.deselectionUnitBitmap.length - 1; i++ )
				sa.deselectionUnitBitmap[ i ] = wrapper.get();
			final BitBufferView bitBuffer = new BitBufferView( wrapper, sa.deselectionBitsCount & 0x07 );
			// Last bits of the deselection unit bitmap: bitBuffer.initialBits
			sa.deselectionUnitBitmap[ sa.deselectionUnitBitmap.length - 1 ] = bitBuffer.initialBits;
			
			final int unitTypesCount = bitBuffer.get();
			if ( unitTypesCount > 0 ) {
				sa.unitTypes         = new short[ unitTypesCount ];
				sa.unitsOfTypeCounts = new short[ unitTypesCount ];
				for ( int i = 0; i < unitTypesCount; i++ ) {
					sa.unitTypes        [ i ] = bitBuffer.get2Bytes();
					bitBuffer.get(); // unknown
					sa.unitsOfTypeCounts[ i ] = (short) bitBuffer.get();
				}
			}
			final int totalUnitsCount = bitBuffer.get();
			if ( totalUnitsCount > 0 ) {
				sa.unitIds = new int[ totalUnitsCount ];
				for ( int i = 0; i < totalUnitsCount; i++ ) {
					// Id is an int, but it cannot be read byte-by-byte due to Blizzard's bit stream format
					sa.unitIds[ i ] = bitBuffer.getInt();
					sa.unitIds[ i ] = ( sa.unitIds[ i ] & 0xff00 ) >> 8 | ( sa.unitIds[ i ] & 0xff ) << 8 | ( sa.unitIds[ i ] & 0xff000000 ) >>> 8 | ( sa.unitIds[ i ] & 0xff0000 ) << 8;
				}
			}
			final int remainder = bitBuffer.getRemainder();
			// If there are unused bits in the last byte, they tell us how many padding bytes follow
			if ( remainder > 0 )
				wrapper.position( wrapper.position() + remainder );
		}
	}
	
	@Override
	protected BaseUseAbilityAction readUseAbilityAction( final AbilityCodes abilityCodes ) {
		wrapper.getInt(); // 2nd byte: unitCountToSubstract?, 3rd byte: unitCountToAdd?
		final int abilityCode = ( ( wrapper.get() & 0xff ) << 16 ) + ( ( wrapper.get() & 0xff ) << 8 ) + ( wrapper.get() & 0xff );
		Unit     unit     = null;
		Building building = null;
		Upgrade  upgrade  = null;
		Research research = null;
		Object[] abilityParams;
		final BaseUseAbilityAction bua;
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
		
		final boolean targetIsUnit = ( wrapper.get() & 0x10 ) != 0;
		
		if ( targetIsUnit ) {
			// Here comes a unit target (25 more bytes)
			wrapper.position( wrapper.position() + 6 );
			// Unit id
			bua.targetId = wrapper.getInt();
			// It contains some bit twist, and we want to bring it to our representation (format).
			// The following line is the concatenation of the 2 operations:
			bua.targetId = ( bua.targetId & 0xfff ) << 4 | ( bua.targetId & 0xf000 ) >> 12 | ( bua.targetId & 0xff000000 ) >>> 8 | ( bua.targetId & 0xff0000 ) << 8;
			// Unit type
			bua.targetType = (short) ( ( ( wrapper.get() & 0xff ) << 4 ) | ( wrapper.get() & 0x0f ) );
			wrapper.position( wrapper.position() + 13 );
		}
		else {
			// Here comes a point target (24 more bytes)
			wrapper.position( wrapper.position() + 12 );
			bua.targetX = Integer.reverseBytes( wrapper.getInt() ) << 1; // x; Half of the "usual" coordinate formats
			bua.targetY = Integer.reverseBytes( wrapper.getInt() ) << 1; // y; Half of the "usual" coordinate formats
			wrapper.position( wrapper.position() + 4 );
		}
		
		return bua;
	}
	
}
