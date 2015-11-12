import java.io.File;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;


public class Test {

	private static void init() {
		Language.getText( "general.unknown" );
		// Ensure ability codes repository is initialized when it is needed:
		try {
			Class.forName( ReplayUtils.class.getName() );
			
			final ActionType[] actionTypeValues = ActionType.values();
			for ( int i = 0; i < actionTypeValues.length; i++ )
				actionTypeValues[ i ].stringValue.charAt( 0 );
			
		} catch ( final ClassNotFoundException cnfe ) {
			cnfe.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		
		final File repFolder = new File( "testrep" );
		
		for ( final File repFile : repFolder.listFiles() ) {
			System.out.println( repFile );
			final Replay rep = ReplayFactory.parseReplay( repFile.getAbsolutePath(), ReplayFactory.ALL_CONTENT );
			
			for ( final Player player : rep.details.players )
				System.out.println( player.playerId.getBattleNetProfileUrl( null ) );
			
			System.out.println( rep.details.getPlayerNamesGrouped() );
			
			System.out.println();
		}
	}

}
