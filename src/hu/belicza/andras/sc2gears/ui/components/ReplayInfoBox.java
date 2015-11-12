package hu.belicza.andras.sc2gears.ui.components;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;

import java.awt.Dimension;
import java.io.File;
import java.util.Date;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * A replay info box.
 * A simple component which displays basic info about a replay.
 * 
 * @author Andras Belicza.
 */
@SuppressWarnings("serial")
public class ReplayInfoBox extends JScrollPane {
	
	/** Label to display the version.     */
	private final JLabel versionLabel    = new JLabel();
	/** Label to display the date.        */
	private final JLabel dateLabel       = new JLabel();
	/** Label to display the length.      */
	private final JLabel lengthLabel     = new JLabel();
	/** Label to display the speed.       */
	private final JLabel speedLabel      = new JLabel();
	/** Label to display the type.        */
	private final JLabel typeLabel       = new JLabel();
	/** Label to display the format.      */
	private final JLabel formatLabel     = new JLabel();
	/** Label to display the gateway.     */
	private final JLabel gatewayLabel    = new JLabel();
	/** Label to display the map.         */
	private final JLabel mapLabel        = new JLabel();
	/** Label to display the players.     */
	private final JLabel playersLabel    = new JLabel();
	/** Label to display the map preview. */
	private final JLabel mapPreviewLabel = new JLabel();
	
	/**
	 * Creates a new ReplayInfoBox.
	 */
	public ReplayInfoBox() {
		this( null );
	}
	
	/**
	 * Creates a new ReplayInfoBox.
	 * @param file replay file to be initially set.
	 */
	public ReplayInfoBox( final File file ) {
		final Box box = Box.createVerticalBox();
		box.setBorder( BorderFactory.createTitledBorder( Language.getText( "module.repAnalyzer.tab.gameInfo.title" ) ) );
		
		box.add( versionLabel );
		box.add( dateLabel );
		box.add( lengthLabel );
		box.add( speedLabel );
		box.add( typeLabel );
		box.add( formatLabel );
		box.add( gatewayLabel );
		box.add( mapLabel );
		box.add( playersLabel );
		box.add( mapPreviewLabel );
		
		box.setPreferredSize( new Dimension( 300, 440 ) );
		
		setViewportView( box );
		
		if ( file != null )
			setReplayFile( file );
	}
	
	/**
	 * Sets the replay file to display info about.
	 * @param file file to be set
	 */
	public void setReplayFile( final File file ) {
		final Replay replay = ReplayFactory.parseReplay( file.getAbsolutePath(), EnumSet.of( ReplayContent.INIT_DATA, ReplayContent.DETAILS, ReplayContent.ATTRIBUTES_EVENTS, ReplayContent.MAP_INFO ) );
		if ( replay == null ) {
			versionLabel.setText( Language.getText( "fileChooser.failedToParseRepaly" ) );
			dateLabel   .setText( "" );
			lengthLabel .setText( "" );
			speedLabel  .setText( "" );
			typeLabel   .setText( "" );
			formatLabel .setText( "" );
			gatewayLabel.setText( "" );
			playersLabel.setText( "" );
			mapLabel    .setText( "" );
			mapPreviewLabel.setIcon( null );
		}
		else {
			versionLabel.setText( Language.getText( "module.repAnalyzer.tab.gameInfo.version"   , replay.version ) );
			dateLabel   .setText( Language.getText( "module.repAnalyzer.tab.gameInfo.date"      , Language.formatDateTime( new Date( replay.details.saveTime ) ) ) );
			lengthLabel .setText( Language.getText( "module.repAnalyzer.tab.gameInfo.gameLength", ReplayUtils.formatMs( replay.gameLength * 500, replay.converterGameSpeed ) ) );
			speedLabel  .setText( Language.getText( "module.repAnalyzer.tab.gameInfo.gameSpeed" , replay.initData.gameSpeed ) );
			typeLabel   .setText( Language.getText( "module.repAnalyzer.tab.gameInfo.gameType"  , replay.initData.gameType ) );
			formatLabel .setText( replay.initData.format == null ? "" : Language.getText( "module.repAnalyzer.tab.gameInfo.format", replay.initData.format ) );
			gatewayLabel.setText( replay.initData.gateway == null ? "" : Language.getText( "module.repAnalyzer.tab.gameInfo.gateway", replay.initData.gateway ) );
			playersLabel.setText( Language.getText( "module.repAnalyzer.tab.gameInfo.players" ) + " " + replay.details.getPlayerNames() );
			mapLabel    .setText( Language.getText( "module.repAnalyzer.tab.gameInfo.mapName"   , replay.details.mapName ) );
			mapPreviewLabel.setIcon( replay.mapInfo == null ? null : replay.mapInfo.previewIcon );
		}
	}
	
}
