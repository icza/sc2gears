package hu.belicza.andras.sc2gears.ui.moduls.multirepanal;

import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.NullAwareComparable;


/**
 * Statistics of the player with a playmate.
 * 
 * @author Andras Belicza
 */
public class PlaymateStatistics extends DateRangeStatistics {
	
	/** Full name of the playmate.     */
	public final String playmate;
	
	/** Total time (seconds) in games. */
	public long totalTimeSecInGames;
	
	/** Record as allies.    */
	public final Record recordAsAllies    = new Record();
	/** Record as opponents. */
	public final Record recordAsOpponents = new Record();
	
	/**
	 * Creates a new PlaymateStatistics.
	 * @param playmate name of the playmate
	 */
	public PlaymateStatistics( final String playmate ) {
		this.playmate = playmate;
	}
	
	/**
	 * Builds in the specified player game participation stats.
	 * @param pgps reference to the player game participation stats to build in
	 */
	public void buildInPlayerGameParticipation( final PlayerGameParticipationStats pgps, final boolean isAlly ) {
		registerDate( pgps.date );
		totalTimeSecInGames += pgps.timeSecInGame;
		final Record record = isAlly ? recordAsAllies : recordAsOpponents;
		record.totalGames++;
		if ( pgps.isWinner != null )
			if ( pgps.isWinner )
				record.wins++;
			else
				record.losses++;
	}
	
	/**
	 * Returns the formatted total time in games.
	 * @return the formatted total time in games
	 */
	public NullAwareComparable< Long > getFormattedTotalTimeInGames() {
		return new NullAwareComparable< Long >( totalTimeSecInGames ) {
			@Override
			public String toString() {
				return GeneralUtils.formatLongSeconds( value );
			}
		};
	}
	
}
