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
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.MoveScreenAction;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;

/**
 * A {@link VersionCompatibility#V_2_0} compatible replay parser.
 * 
 * @author Andras Belicza
 */
class ReplayParserV20 extends ReplayParserV15 {
	
	/**
	 * Creates a new ReplayParserV20.
	 * @param fileName             name of the replay file
	 * @param replay               reference to the replay
	 * @param versionCompatibility version compatibility of the replay
	 */
	public ReplayParserV20( final String fileName, final Replay replay, final VersionCompatibility versionCompatibility ) {
		super( fileName, replay, versionCompatibility );
	}
	
	/**
	 * Reads a move screen action.
	 * @return the read move screen action
	 */
	protected MoveScreenAction readMoveScreenAction() {
		wrapper.position( wrapper.position() - 1 );
		final BitInputStream bitin = new BitInputStream( wrapper );
		bitin.readBits( 4 );
		
		final MoveScreenAction msa = replay.gameEvents.new MoveScreenAction();
		
		final boolean hasLocation = bitin.readBoolean();
		if ( hasLocation ) {
			msa.flags |= MoveScreenAction.FLAG_MASK_HAS_LOCATION;
			msa.x = bitin.readBits( 16 );
			msa.y = bitin.readBits( 16 );
		}
		
		final boolean hasDistance = bitin.readBoolean();
		if ( hasDistance ) {
			msa.flags |= MoveScreenAction.FLAG_MASK_HAS_DISTANCE;
			msa.distance = bitin.readBits( 16 );
		}
		
		final boolean hasPitch = bitin.readBoolean();
		if ( hasPitch ) {
			msa.flags |= MoveScreenAction.FLAG_MASK_HAS_PITCH;
			msa.pitch = bitin.readBits( 16 );
			// Convert to degrees and discard the last 4 bit in order for fast transformation
			msa.pitch = ( 45 * ( ( ( ( ( ( msa.pitch << 5 ) - 0x2000 ) << 17 ) - 1 ) >> 17 ) + 1 ) ) >> 4;
		}
		
		final boolean hasYaw = bitin.readBoolean();
		if ( hasYaw ) {
			msa.flags |= MoveScreenAction.FLAG_MASK_HAS_YAW;
			msa.yaw = bitin.readBits( 16 );
			// Convert to degrees and discard the last 4 bit in order for fast transformation
			msa.yaw = ( 45 * ( ( ( ( ( ( msa.yaw << 5 ) - 0x2000 ) << 17 ) - 1 ) >> 17 ) + 1 ) ) >> 4;
		}
		
		// Reason introduced in 2.1:
		if ( versionCompatibility.compareTo( VersionCompatibility.V_2_1 ) <= 0 ) {
			if ( bitin.readBoolean() )
				bitin.readBits( 8 ); // Reason
		}
		
		return msa;
	}
	
}
