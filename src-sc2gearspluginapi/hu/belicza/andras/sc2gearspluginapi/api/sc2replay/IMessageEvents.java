/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.sc2replay;

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.importing.ReplaySpecification;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;

import java.util.Map;

/**
 * Message events interface of StarCraft II replays.
 * 
 * <p>Message events contains the in-game chat and minimap pings.</p>
 * 
 * <p>Example usage:<br>
 * <blockquote><pre>
 * IReplay replay = generalServices.getReplayFactoryApi().parseReplay( "C:\\someReplay.SC2Replay", EnumSet.of( ReplayFactoryApi.ReplayContent.MESSAGE_EVENTS ) );
 * 
 * // Don't forget to check if replay or message events are null!
 * int ms = 0;
 * String[] arrangedClientNames = replay.getArrangedClientNames();
 * 
 * // Print in-game chat along with minimap pings:
 * for ( IMessageEvents.IMessage message : replay.getMessageEvents().getMessages() ) {
 *     ms += message.getDeltaTime();
 *     String playerName = arrangedClientNames[ message.getClient() ];
 *     
 *     if ( message instanceof IMessageEvents.IText )
 *             System.out.println( playerName + " at " + (ms/1000) + " sec said: " + ( (IMessageEvents.IText) message ).getText() );
 *         else if ( message instanceof IMessageEvents.IBlink )
 *             System.out.printf( "%s at %d sec pinged: x=%.2f,y=%.2f\n", playerName, ms/1000, ( (IMessageEvents.IBlink) message ).getNormalizedX(), ( (IMessageEvents.IBlink) message ).getNormalizedY() );
 * }
 * </pre></blockquote></p>
 * 
 * @since "2.0"
 * 
 * @version {@value #VERSION}
 * 
 * @author Andras Belicza
 * 
 * @see IReplay
 */
public interface IMessageEvents {
	
	/** Interface version. */
	String VERSION = "2.0";
	
	/**
	 * Represents a general message.
	 * @author Andras Belicza
	 */
	public static interface IMessage {
		
		/**
		 * Returns the <b>delta</b> time since the last message (in milliseconds).
		 * @return the <b>delta</b> time since the last message (in milliseconds)
		 */
		int getDeltaTime();
		
		/**
		 * Returns the client index of the message (who does the message originates from).
		 * 
		 * <p>The returned client index is valid for the array {@link IReplay#getArrangedClientNames()}.<br>
		 * If the returned client index is less than the number of players (length of {@link IReplay#getPlayers()}), then it is equivalent to the player index.</p>
		 * 
		 * @return the client index of the message
		 * @see IReplay#getArrangedClientNames()
		 */
		int getClient();
		
	}
	
	/**
	 * Represents a text message.
	 * @author Andras Belicza
	 */
	public static interface IText extends IMessage {
		
		/**
		 * Returns the text of the message.
		 * @return the text of the message
		 */
		String getText();
		
	}
	
	/**
	 * Represents a blink (minimap ping).
	 * @author Andras Belicza
	 */
	public static interface IBlink extends IMessage {
		
		/**
		 * Returns the x coordinate of the blink.
		 * <p>The coordinate is the map location multiplied by 65536.</p>
		 * @return the x coordinate of the blink
		 * @see #getNormalizedX()
		 */
		int getX();
		
		/**
		 * Returns the y coordinate of the blink.
		 * <p>The coordinate is the map location multiplied by 65536.</p>
		 * @return the y coordinate of the blink
		 * @see #getNormalizedY()
		 */
		int getY();
		
		/**
		 * Returns the normalized x coordinate of the blink.
		 * <p>The normalized coordinate is the map location.</p>
		 * @return the normalized x coordinate of the blink
		 * @see #getX()
		 */
		float getNormalizedX();
		
		/**
		 * Returns the normalized y coordinate of the blink.
		 * <p>The normalized coordinate is the map location.</p>
		 * @return the normalized y coordinate of the blink
		 * @see #getY()
		 */
		float getNormalizedY();
		
	}
	
	/**
	 * Returns the messages.
	 * 
	 * <p>The return value can be <code>null</code> if the replay was constructed from a {@link ReplaySpecification}
	 * or was loaded from the cache.</p>
	 * 
	 * @return the messages
	 */
	IMessage[] getMessages();
	
	/**
	 * Returns the array of word count maps.
	 * 
	 * <p>The index is the player (client) index, the map key is the word, the value is the word count.</p>
	 * 
	 * <p>The use of <code>clientsCount</code>: if you only want to calculate and get statistics for players
	 * (excluding observers), you can pass the players count (length of the array: {@link IReplay#getPlayers()}).</p>
	 * 
	 * @param clientsCount number of clients to count words for
	 * @return the word count maps
	 */
	Map< String, IntHolder >[] getWordCountMaps( int clientsCount );
	
}
