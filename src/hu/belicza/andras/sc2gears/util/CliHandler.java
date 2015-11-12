/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.util;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory.ReplayContent;
import hu.belicza.andras.sc2gears.sc2replay.model.MessageEvents;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gearspluginapi.api.enums.LadderSeason;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.ActionType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Command line interface handler.
 * 
 * @author Andras Belicza
 */
public class CliHandler {
	
	/**
	 * Modifier parameter.
	 * @author Andras Belicza
	 */
	private static enum ModifierParameter {
		/** XML output modifier parameter.    */
		XML_OUTPUT   ( "--xml-output"   , "Uses XML output format instead of plain text" ),
		/** No inaction modifier parameter.   */
		NO_INACTION  ( "--no-inaction"  , "Excludes the Inaction action types" ),
		/** Use frames modifier parameter.    */
		USE_FRAMES   ( "--use-frames"   , "Overrides the setting and uses frames to display time" ),
		/** Use seconds modifier parameter.   */
		USE_SECONDS  ( "--use-seconds"  , "Overrides the setting and uses seconds to display time" ),
		/** Print blinks modifier parameter.  */
		PRINT_BLINKS ( "--print-blinks" , "Overrides the setting and includes the minimap pings" ),
		/** No blinks modifier parameter.     */
		NO_BLINKS    ( "--no-blinks"    , "Overrides the setting and excludes the minimap pings" ),
		/** Print targets modifier parameter. */
		PRINT_TARGETS( "--print-targets", "Overrides the setting and includes the message targets" ),
		/** No targets modifier parameter.    */
		NO_TARGETS   ( "--no-targets"   , "Overrides the setting and excludes the message targets" );
		
		/** String value of the modifier parameter; the form it has to be defined in the command line. */
		public final String stringValue;
		/** Description of the modifier parameter.                                                     */
		public final String description;
		
		/**
		 * Creates a new ModifierParameter.
		 * @param stringValue string value of the modifier parameter
		 * @param description description of the modifier parameter
		 */
		private ModifierParameter( final String stringValue, final String description ) {
			this.stringValue = stringValue;
			this.description = description;
		}
		
	}
	
	/**
	 * Action parameter.
	 * @author Andras Belicza
	 */
	private static enum ActionParameter {
		/** Show game info action parameter.     */
		PRINT_GAME_INFO  ( "--print-game-info"   , true , "Prints general info about the specified replay", ModifierParameter.XML_OUTPUT ),
		/** Print action list action parameter.  */
		PRINT_ACTION_LIST( "--print-action-list" , true , "Prints the complete action list of the specified replay", ModifierParameter.NO_INACTION, ModifierParameter.USE_FRAMES, ModifierParameter.USE_SECONDS ),
		/** Print in-game chat action parameter. */
		PRINT_INGAME_CHAT( "--print-in-game-chat", true , "Prints the in-game chat of the specified replay", ModifierParameter.PRINT_BLINKS, ModifierParameter.NO_BLINKS, ModifierParameter.PRINT_TARGETS, ModifierParameter.NO_TARGETS ),
		/** Help action parameter.               */
		HELP             ( "--help"              , false, "Displays this help" );
		
		/** String value of the action parameter; the form it has to be defined in the command line. */
		public final String              stringValue;
		/** Tells if this action parameter requires a replay file.                                   */
		public final boolean             requiresReplayFile;
		/** Description of the action parameter.                                                     */
		public final String              description;
		/** Optional modifier parameters associated with this action parameter.                      */
		public final ModifierParameter[] modifierParameters;
		
		/**
		 * Creates a new ActionParameter.
		 * @param stringValue        string value of the action parameter
		 * @param description        description of the action parameter
		 * @param modifierParameters optional modifier parameters associated with this action parameter
		 */
		private ActionParameter( final String stringValue, final boolean requiresReplayFile, final String description, final ModifierParameter... modifierParameters ) {
			this.stringValue        = stringValue;
			this.requiresReplayFile = requiresReplayFile;
			this.description        = description;
			this.modifierParameters = modifierParameters.length == 0 ? null : modifierParameters;
		}
		
		/**
		 * Returns the action parameter defined by its string value.
		 * @param stringValue string value of the action parameter
		 * @return the action parameter defined by its string value; or <code>null</code> if the specified string value does not match any action parameter
		 */
		public static ActionParameter fromStringValue( final String stringValue ) {
			for ( final ActionParameter actionParameter : values() )
				if ( actionParameter.stringValue.equals( stringValue ) )
					return actionParameter;
			
			return null;
		}
		
		/**
		 * Returns the modifier parameter defined by its string value.
		 * @param stringValue string value of the modifier parameter
		 * @return the modifier parameter defined by its string value; or <code>null</code> if the specified string value does not match any modifier parameter
		 * @return the modifier parameter defined by its string value
		 */
		public ModifierParameter modifierFromStringValue( final String stringValue ) {
			for ( final ModifierParameter modifierParameter : modifierParameters )
				if ( modifierParameter.stringValue.equals( stringValue ) )
					return modifierParameter;
			
			return null;
		}
		
	}
	
	/**
	 * Checks the arguments if command line mode is to be activated.
	 * @param arguments arguments of the application
	 * @return true if command line mode is to be activated; false otherwise
	 */
	public static boolean checkCliMode( final String[] arguments ) {
		return arguments.length > 0 && arguments[ 0 ].startsWith( "--" );
	}
	
	/**
	 * Handles the arguments.
	 * @param arguments arguments of the application
	 * @return 0 in case of success; or a positive error code if something went bad
	 */
	public static int handleArguments( final String[] arguments ) {
		// Wrap the output to specify character encoding and ensure proper display of all characters
		try {
			System.setOut( new PrintStream( System.out, true, "UTF-8" ) );
		} catch ( final UnsupportedEncodingException uee ) {
			uee.printStackTrace();
		}
		
		System.out.println( Consts.APPLICATION_NAME + " " + Consts.APPLICATION_VERSION + " Command Line Interface" );
		
		int argsCounter = 0;
		
		// Parse action parameter
		final ActionParameter actionParameter = ActionParameter.fromStringValue( arguments[ argsCounter ] );
		
		if ( actionParameter == null ) {
			System.out.println( "Unrecognized ACTION: " + arguments[ argsCounter ] );
			printUsageWarning();
			return 1;
		}
		
		// Parse optional modifier parameters
		argsCounter++;
		final Set< ModifierParameter > modifierParameterSet;
		if ( actionParameter.modifierParameters != null ) {
			modifierParameterSet = EnumSet.noneOf( ModifierParameter.class );
			for ( ; argsCounter < arguments.length; argsCounter++ ) {
				ModifierParameter mp = actionParameter.modifierFromStringValue( arguments[ argsCounter ] );
				if ( mp == null )
					break;
				else
					modifierParameterSet.add( mp );
			}
		}
		else
			modifierParameterSet = null;
		
		// Parse optional replay file parameter
		final String replayFileName;
		if ( actionParameter.requiresReplayFile ) {
			if ( argsCounter >= arguments.length ) {
				System.out.println( "Missing replay file argument!" );
				printUsageWarning();
				return 2;
			}
			replayFileName = arguments[ argsCounter++ ];
		}
		else
			replayFileName = null;
		
		// Do the job
		int result = 0;
		switch ( actionParameter ) {
		case PRINT_GAME_INFO   : result = printGameInfo  ( replayFileName, modifierParameterSet ); break;
		case PRINT_ACTION_LIST : result = printActionList( replayFileName, modifierParameterSet ); break;
		case PRINT_INGAME_CHAT : result = printIngameChat( replayFileName, modifierParameterSet ); break;
		case HELP              : result = printHelp      ( replayFileName, modifierParameterSet ); break;
		}
		
		return result == 0 ? 0 : result + ( actionParameter.ordinal() + 1 ) * 100; // Shift the error code so we can distinguish them from error codes originating from this method.
	}
	
	/**
	 * Prints a usage warning message; instructions how-to print usage info.
	 */
	private static void printUsageWarning() {
		System.out.println( "Type \"" + GeneralUtils.getSc2gearsStartCommand() + " --help\" for usage info." );
	}
	
	/**
	 * Prints the game info.
	 * @param replayFileName       optional replay file name
	 * @param modifierParameterSet set of the provided modifier parameters 
	 * @return 0 in case of success; or a positive error code if something went bad
	 */
	private static int printGameInfo( final String replayFileName, final Set< ModifierParameter > modifierParameterSet ) {
		final Replay replay = ReplayFactory.parseReplay( replayFileName, EnumSet.of( ReplayContent.INIT_DATA, ReplayContent.DETAILS, ReplayContent.ATTRIBUTES_EVENTS, ReplayContent.GAME_EVENTS, ReplayContent.MAP_INFO ) );
		if ( replay == null ) {
			System.out.println( "Could not parse replay: " + replayFileName );
			return 1;
		}
		
		System.out.println( "-----" + ActionParameter.PRINT_GAME_INFO.stringValue );
		
		final StringBuilder clientNamesBuilder = new StringBuilder();
		final String[] arrangedClientNames = replay.initData.getArrangedClientNames( replay.details.players );
		for ( final String clientName : arrangedClientNames )
			if ( clientName.length() > 0 ) { // There might be empty client names 
				if ( clientNamesBuilder.length() > 0 )
					clientNamesBuilder.append( ", " );
				clientNamesBuilder.append( clientName );
			}
		
		final File replayFile = new File( replayFileName );
		
		if ( modifierParameterSet.contains( ModifierParameter.XML_OUTPUT ) ) {
			// XML output
			try {
				final Document document    = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				final Element  rootElement = document.createElement( "gameInfo" );
				
				rootElement.setAttribute( "Sc2gearsVersion", Consts.APPLICATION_VERSION );
				rootElement.setAttribute( "docVersion", "1.0" );
				
				final Holder< Element > parent = new Holder< Element >();
				Element newElement;
				// A task to add a new element to the parent
				final AdvancedTask< String, Object, Element > add = new AdvancedTask< String, Object, Element >() {
					/**
					 * Creates a new XML tag, sets its text content and appends it to the parent.
					 * @param tagName name of the new XML tag to be created
					 * @param content content of the new XML tag to be set; its {@link Object#toString} method will be called and set as the text content
					 * @return the created and added new XML tag
					 */
					@Override
					public Element execute( final String tagName, final Object content ) {
						final Element element = document.createElement( tagName );
						element.setTextContent( content.toString() );
						parent.value.appendChild( element );
						return element;
					}
				};
				
				parent.value = rootElement;
				
				add.execute( "fileName"        , replayFile.getAbsolutePath() );
				add.execute( "fileSize"        , replayFile.length() );
				add.execute( "expansion"       , replay.details.expansion );
				add.execute( "version"         , replay.version );
				add.execute( "date"            , Language.formatDateTime( new Date( replay.details.saveTime ) ) );
				add.execute( "recorderTimeZone", String.format( Locale.US, "%+.2f", replay.details.saveTimeZone ) );
				add.execute( "gameLength"      , ReplayUtils.formatMs( replay.gameLength * 500, replay.converterGameSpeed ) );
				add.execute( "gameSpeed"       , replay.initData.gameSpeed );
				add.execute( "gameType"        , replay.initData.gameType );
				if ( replay.initData.competitive != null )
					add.execute( "isCompetitive", Language.getText( replay.initData.competitive ? "general.yes" : "general.no" ) );
				if ( replay.initData.gameType == GameType.AMM )
					add.execute( "ladderSeason", LadderSeason.getByDate( new Date( replay.details.saveTime ), replay.initData.gateway ) );
				add.execute( "format"          , replay.initData.format );
				add.execute( "gateway"         , replay.initData.gateway );
				
				newElement = add.execute( "clients", clientNamesBuilder );
				newElement.setAttribute( "clientsCount", Integer.toString( arrangedClientNames.length ) );
				
				newElement = add.execute( "mapName", replay.details.mapName );
				if ( replay.details.mapName != replay.details.originalMapName )
					newElement.setAttribute( "originalMapName", replay.details.originalMapName );
	    		if ( replay.initData.mapFileName != null )
	    			add.execute( "mapFile", replay.initData.mapFileName );
				
	    		if ( replay.mapInfo != null ) {
	    			newElement = add.execute( "mapSize"        , replay.mapInfo.getSizeString() );
	    			newElement.setAttribute( "width" , Integer.toString( replay.mapInfo.width  ) );
	    			newElement.setAttribute( "height", Integer.toString( replay.mapInfo.height ) );
	    			newElement = add.execute( "mapPlayableSize", replay.mapInfo.getPlayableSizeString() );
	    			newElement.setAttribute( "width" , Integer.toString( replay.mapInfo.boundaryRight - replay.mapInfo.boundaryLeft   ) );
	    			newElement.setAttribute( "height", Integer.toString( replay.mapInfo.boundaryTop   - replay.mapInfo.boundaryBottom ) );
	    		}
	    		
	    		// Players
				final Element playersElement = document.createElement( "players" );
	    		int lastTeam = -1;
	    		Element teamElement = null;
	    		playersElement.setAttribute( "playersCount", Integer.toString( replay.details.players.length ) );
	    		int teamsCount = 0;
	    		for ( final int playerIndex : replay.details.getTeamOrderPlayerIndices() ) {
	    			final Player player = replay.details.players[ playerIndex ];
	    			if ( player.team != lastTeam ) {
	    				teamsCount++;
	    				if ( teamElement != null )
	    		    		playersElement.appendChild( teamElement );
	    				teamElement = document.createElement( "team" );
	    				teamElement.setAttribute( "team", player.team == Player.TEAM_UNKNOWN ? Language.getText( "general.unknown" ) : Integer.toString( player.team ) );
	    				lastTeam = player.team;
	    			}
					final Element playerElement = document.createElement( "player" );
					parent.value = playerElement;
					add.execute( "name", player.playerId.name );
					add.execute( "league" , player.getLeague().stringValue );
					add.execute( "levels" , player.getSwarmLevels() );
					newElement = add.execute( "race", player.race.stringValue );
					if ( player.race == Race.RANDOM )
						newElement.setAttribute( "finalRace", player.finalRace.stringValue );
					add.execute( "apm" , ReplayUtils.calculatePlayerApm ( replay, player ) );
					add.execute( "eapm", ReplayUtils.calculatePlayerEapm ( replay, player ) );
					newElement = add.execute( "type", player.type.stringValue );
					if (  player.type == PlayerType.COMPUTER )
						newElement.setAttribute( "difficulty", player.difficulty.stringValue );
					else {
						newElement.setAttribute( "fullName"   , player.playerId.getFullName() );
						newElement.setAttribute( "gateway"    , player.playerId.getGateway().stringValue );
						newElement.setAttribute( "region"     , player.playerId.getRegion().stringValue );
						newElement.setAttribute( "battleNetId", Integer.toString( player.playerId.battleNetId ) );
					}
					newElement = add.execute( "color", player.getColorName() );
					newElement.setAttribute( "name" , player.playerColor.stringValue );
					newElement.setAttribute( "red"  , Integer.toString( player.argbColor[ 1 ] ) );
					newElement.setAttribute( "green", Integer.toString( player.argbColor[ 2 ] ) );
					newElement.setAttribute( "blue" , Integer.toString( player.argbColor[ 3 ] ) );
					if ( player.handicap < 100 )
						add.execute( "handicap", player.handicap );
					teamElement.appendChild( playerElement );
	    		}
				if ( teamElement != null )
		    		playersElement.appendChild( teamElement );
				playersElement.setAttribute( "teamsCount", Integer.toString( teamsCount ) );
	    		rootElement.appendChild( playersElement );
	    		
				parent.value = rootElement;
				newElement = add.execute( "winners", replay.details.getWinnerNames() );
				int winnersCount = 0;
				for ( final Player player : replay.details.players )
					if ( player.isWinner != null && player.isWinner )
						winnersCount++;
				newElement.setAttribute( "winnersCount", Integer.toString( winnersCount ) );
				
				document.appendChild( rootElement );
				
				final Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
				transformer.transform( new DOMSource( document ), new StreamResult( System.out ) );
				
            } catch ( final Exception e ) {
            	System.out.println( e );
            	return 2;
            }
		}
		else {
			// Plain text output
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.fileName", replayFile.getAbsolutePath() ) );
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.fileSize", String.format( Locale.ENGLISH, "%,d", replayFile.length() ) ) );
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.expansion", replay.details.expansion.fullStringValue ) );
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.version", replay.version ) );
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.date", Language.formatDateTime( new Date( replay.details.saveTime ) ) ) );
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.recorderTimeZone", String.format( Locale.US, "%+.2f", replay.details.saveTimeZone ) ) );
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.gameLength", ReplayUtils.formatMs( replay.gameLength * 500, replay.converterGameSpeed ) ) );
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.gameSpeed", replay.initData.gameSpeed ) );
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.gameType", replay.initData.gameType ) );
			if ( replay.initData.competitive != null )
				System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.isCompetitive", Language.getText( replay.initData.competitive ? "general.yes" : "general.no" ) ) );
			if ( replay.initData.gameType == GameType.AMM )
				System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.ladderSeason", LadderSeason.getByDate( new Date( replay.details.saveTime ), replay.initData.gateway ) ) );
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.format", replay.initData.format ) );
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.gateway", replay.initData.gateway ) );
    		
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.clients", clientNamesBuilder ) );
    		
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.mapName", replay.details.mapName == replay.details.originalMapName ? replay.details.mapName : replay.details.mapName + " (" + replay.details.originalMapName + ")" ) );
    		if ( replay.initData.mapFileName != null )
    			System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.mapFile", replay.initData.mapFileName ) );
    		if ( replay.mapInfo != null ) {
    			System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.mapSize", replay.mapInfo.getSizeString() ) );
    			System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.mapPlayableSize", replay.mapInfo.getPlayableSizeString() ) );
    		}
    		
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.players" ) );
    		
    		// Players
    		int lastTeam = -1;
    		for ( final int playerIndex : replay.details.getTeamOrderPlayerIndices() ) {
    			final Player player = replay.details.players[ playerIndex ];
    			if ( player.team != lastTeam ) {
    				System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.team", player.team == Player.TEAM_UNKNOWN ? Language.getText( "general.unknown" ) : player.team ) );
    				lastTeam = player.team;
    			}
    			System.out.println( player.playerId.name
    					+ ", " + player.getLeague().stringValue
    					+ ", " + Language.getText( "module.repAnalyzer.tab.gameInfo.levels" , player.getSwarmLevels() )
    					+ ", " + player.getRaceString()
    					+ ", " + Language.getText( "module.repAnalyzer.tab.charts.chartText.apm" , ReplayUtils.calculatePlayerApm ( replay, player ) )
    					+ ", " + Language.getText( "module.repAnalyzer.tab.charts.chartText.eapm", ReplayUtils.calculatePlayerEapm( replay, player ) )
    					+ ", " + player.type.stringValue + " (" + ( player.type == PlayerType.COMPUTER ? player.difficulty.stringValue : player.playerId.getFullName() ) + ")"
    					+ ", " + player.getColorName()
    					+ ( player.handicap < 100 ? ", " + Language.getText( "module.repAnalyzer.tab.gameInfo.handicap", player.handicap ) : "" ) );
    		}
    		
    		// Winners
    		System.out.println( Language.getText( "module.repAnalyzer.tab.gameInfo.winners", replay.details.getWinnerNames() ) );
		}
		
		return 0;
	}
	
	/**
	 * Prints the action list.
	 * @param replayFileName       optional replay file name
	 * @param modifierParameterSet set of the provided modifier parameters 
	 * @return 0 in case of success; or a positive error code if something went bad
	 */
	private static int printActionList( final String replayFileName, final Set< ModifierParameter > modifierParameterSet ) {
		final Replay replay = ReplayFactory.parseReplay( replayFileName, ReplayFactory.GENERAL_DATA_CONTENT );
		if ( replay == null ) {
			System.out.println( "Could not parse replay: " + replayFileName );
			return 1;
		}
		
		System.out.println( "-----" + ActionParameter.PRINT_ACTION_LIST.stringValue );
		
		final boolean printInactions = !modifierParameterSet.contains( ModifierParameter.NO_INACTION );
		
		if ( modifierParameterSet.contains( ModifierParameter.USE_FRAMES ) )
			replay.gameEvents.displayInSeconds = false;
		else if ( modifierParameterSet.contains( ModifierParameter.USE_SECONDS ) )
			replay.gameEvents.displayInSeconds = true;
		else
			replay.gameEvents.displayInSeconds = Settings.getBoolean( Settings.KEY_REP_ANALYZER_CHARTS_DISPLAY_IN_SECONDS );
		
		for ( final Action action : replay.gameEvents.actions )
			if ( printInactions || action.type != ActionType.INACTION )
				System.out.println( action.toString() );
		
		return 0;
	}
	
	/**
	 * Prints the in-game chat.
	 * @param replayFileName       optional replay file name
	 * @param modifierParameterSet set of the provided modifier parameters 
	 * @return 0 in case of success; or a positive error code if something went bad
	 */
	private static int printIngameChat( final String replayFileName, final Set< ModifierParameter > modifierParameterSet ) {
		final Replay replay = ReplayFactory.parseReplay( replayFileName, EnumSet.of( ReplayContent.INIT_DATA, ReplayContent.DETAILS, ReplayContent.ATTRIBUTES_EVENTS, ReplayContent.MESSAGE_EVENTS ) );
		if ( replay == null ) {
			System.out.println( "Could not parse replay: " + replayFileName );
			return 1;
		}
		
		System.out.println( "-----" + ActionParameter.PRINT_INGAME_CHAT.stringValue );
		
		boolean printBlinks;
		if ( modifierParameterSet.contains( ModifierParameter.PRINT_BLINKS ) )
			printBlinks = true;
		else if ( modifierParameterSet.contains( ModifierParameter.NO_BLINKS ) )
			printBlinks = false;
		else
			printBlinks = Settings.getBoolean( Settings.KEY_REP_ANALYZER_IN_GAME_CHAT_SHOW_BLINKS );
		
		boolean hideMessageTargets;
		if ( modifierParameterSet.contains( ModifierParameter.PRINT_TARGETS ) )
			hideMessageTargets = false;
		else if ( modifierParameterSet.contains( ModifierParameter.NO_TARGETS ) )
			hideMessageTargets = true;
		else
			hideMessageTargets = Settings.getBoolean( Settings.KEY_REP_ANALYZER_IN_GAME_CHAT_HIDE_MESSAGE_TARGETS );
		
		final String allText     = hideMessageTargets ? null : Language.getText( "module.repAnalyzer.tab.inGameChat.messageTargetAll"       );
		final String alliesText  = hideMessageTargets ? null : Language.getText( "module.repAnalyzer.tab.inGameChat.messageTargetAllies"    );
		final String obsText     = hideMessageTargets ? null : Language.getText( "module.repAnalyzer.tab.inGameChat.messageTargetObservers" );
		final String unknownText = hideMessageTargets ? null : Language.getText( "module.repAnalyzer.tab.inGameChat.messageTargetUnknown"   );
		
		final String[] arrangedClientNames = replay.initData.getArrangedClientNames( replay.details.players );
		
		int ms = 0;
		for ( final MessageEvents.Message message : replay.messageEvents.messages ) {
			String outputMessage = null;
			
			final String playerName = arrangedClientNames[ message.client ];
			ms += message.time;
			
			if ( message instanceof MessageEvents.Text ) {
				final MessageEvents.Text textMessage = (MessageEvents.Text) message;
				final String text = textMessage.text;
				if ( hideMessageTargets )
					outputMessage = Language.getText( "module.repAnalyzer.tab.inGameChat.chatMessageNoTarget", ReplayUtils.formatMs( ms, replay.converterGameSpeed ), playerName, text );
				else
					outputMessage = Language.getText( "module.repAnalyzer.tab.inGameChat.chatMessageFull", ReplayUtils.formatMs( ms, replay.converterGameSpeed ), playerName, textMessage.opCode == MessageEvents.OP_CODE_CHAT_TO_ALL ? allText : textMessage.opCode == MessageEvents.OP_CODE_CHAT_TO_ALLIES ? alliesText : textMessage.opCode == MessageEvents.OP_CODE_CHAT_TO_OBSERVERS ? obsText : unknownText, text );
			}
			else if ( message instanceof MessageEvents.Blink ) {
				if ( printBlinks ) {
					final MessageEvents.Blink blinkMessage = (MessageEvents.Blink) message;
					outputMessage = Language.getText( "module.repAnalyzer.tab.inGameChat.blinkMessage", ReplayUtils.formatMs( ms, replay.converterGameSpeed ), playerName, ReplayUtils.formatCoordinate( blinkMessage.x ), ReplayUtils.formatCoordinate( blinkMessage.y ) );
				}
			}
			
			if ( outputMessage != null )
				System.out.println( outputMessage );
		}
		
		return 0;
	}
	
	/**
	 * Prints the help text (including the command line usage).
	 * @param replayFileName       optional replay file name
	 * @param modifierParameterSet set of the provided modifier parameters 
	 * @return 0 in case of success; or a positive error code if something went bad
	 */
	private static int printHelp( final String replayFileName, final Set< ModifierParameter > modifierParameterSet ) {
		System.out.println( "Usage:" );
		System.out.println();
		System.out.println( ( GeneralUtils.isWindows() ? Consts.EXECUTABLE_NAME_WIN_BATCH : GeneralUtils.getSc2gearsStartCommand() ) + " ACTION [ACTION_ARGS1] [ACTION_ARGS2] [...] [replayFile]" );
		System.out.println();
		
		final ActionParameter[] parameters = ActionParameter.values();
		int maxActionParameterLength = 0;
		for ( final ActionParameter actionParameter : parameters )
			maxActionParameterLength = Math.max( maxActionParameterLength, actionParameter.stringValue.length() );
		
		int maxModifierParameterLength = 0;
		for ( final ModifierParameter modifierParameter : ModifierParameter.values() )
			maxModifierParameterLength = Math.max( maxModifierParameterLength, modifierParameter.stringValue.length() );
		
		System.out.println( "Possible values of ACTION:" );
		for ( final ActionParameter actionParameter : parameters ) {
			System.out.println();
			System.out.printf( "   %-" + maxActionParameterLength + "s  %s\n", actionParameter.stringValue, actionParameter.description );
			if ( actionParameter.modifierParameters != null ) {
				System.out.printf( "   %-" + maxActionParameterLength + "s  ACTION_ARGS:\n", "" );
				for ( final ModifierParameter modifierParameter : actionParameter.modifierParameters )
					System.out.printf( "   %-" + maxActionParameterLength + "s      %-" + maxModifierParameterLength + "s  %s\n", "", modifierParameter.stringValue, modifierParameter.description );
			}
		}
		
		System.out.println();
		System.out.println( "For more information please visit " + Consts.URL_COMMAND_LINE_INTERFACE_HELP );
		return 0;
	}
	
}
