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

import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IMessageEvents;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Contains information from chat messages and other non-game impact commands.
 * 
 * <p>For format description see:
 * <ul><li><a href='http://code.google.com/p/starcraft2replay/'>Starcraft 2 Replay</a></ul></p>
 * 
 * @author Andras Belicza
 */
public class MessageEvents implements IMessageEvents {
	
	/** Op code indicating a chat message to all.       */
	public static final byte OP_CODE_CHAT_TO_ALL       = (byte) 0x00;
	/** Op code indicating a chat message to allies.    */
	public static final byte OP_CODE_CHAT_TO_ALLIES    = (byte) 0x02;
	/** Op code indicating a chat message to observers. */
	public static final byte OP_CODE_CHAT_TO_OBSERVERS = (byte) 0x04;
	/** Op code indicating a sync message.              */
	public static final byte OP_CODE_SYNC              = (byte) 0x80; // Maybe this is System message with parameters: short, short messageID
	/** Op code indicating a blink (minimap ping).      */
	public static final byte OP_CODE_BLINK             = (byte) 0x83;
	
	/**
	 * Represents a general message.
	 * @author Andras Belicza
	 */
	public static class Message implements IMessage {
		public final int  time;  // time since the last message in ms
		public final int  client;
		public final byte opCode;
		
		public Message( final int time, final int client, final byte opCode ) {
			this.time   = time;
			this.client = client;
			this.opCode = opCode;
		}
		
		@Override
		public int getDeltaTime() {
			return time;
		}
		
		@Override
		public int getClient() {
			return client;
		}
	}
	
	/**
	 * Represents a text message.
	 * @author Andras Belicza
	 */
	public static class Text extends Message implements IText {
		public String text;
		
		public Text( final int time, final int client, final byte opCode ) {
			super( time, client, opCode );
		}
		
		@Override
		public String toString() {
			return "Player " + client + " at " + time + " to " + ( opCode == OP_CODE_CHAT_TO_ALL ? "all: " : opCode == OP_CODE_CHAT_TO_ALLIES ? "allies: " : opCode == OP_CODE_CHAT_TO_OBSERVERS ? "observers: " : "unknown: " ) + text;
		}
		
		@Override
		public String getText() {
			return text;
		}
	}
	
	/**
	 * Represents a blink (minimap ping).
	 * @author Andras Belicza
	 */
	public static class Blink extends Message implements IBlink {
		public int x;
		public int y;
		
		public Blink( final int time, final int client ) {
			super( time, client, OP_CODE_BLINK );
		}
		
		@Override
		public String toString() {
			return String.format( Locale.ENGLISH, "Player %s pinged at %d: %.1f,%.1f", client, time, getNormalizedX(), getNormalizedY() );
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
		public float getNormalizedX() {
			return x / 65536;
		}
		
		@Override
		public float getNormalizedY() {
			return y / 65536f;
		}
	}
	
	public Message[] messages;
	
	private Map< String, IntHolder >[] wordCountMaps;
	
	/**
	 * Returns the array of word count maps.<br>
	 * The index is the player (client) index, the map key is the word, the value is the word count.
	 * @param clientsCount number of clients to count words for
	 * @return the word count maps
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Map< String, IntHolder >[] getWordCountMaps( final int clientsCount ) {
		if ( ( wordCountMaps == null || wordCountMaps.length != clientsCount ) && messages != null ) { // messages can be null if replay was constructed from specification or loaded from cache
			wordCountMaps = new Map[ clientsCount ];
			
			IntHolder count;
			for ( final Message message : messages )
				if ( message instanceof Text )
					for ( final StringTokenizer stringTokenizer = new StringTokenizer( ( (Text) message ).text ); stringTokenizer.hasMoreTokens(); ) {
						final String word = stringTokenizer.nextToken().toLowerCase();
						
						if ( wordCountMaps[ message.client ] == null )
							wordCountMaps[ message.client ] = new HashMap< String, IntHolder >();
						
						if ( ( count = wordCountMaps[ message.client ].get( word ) ) == null )
							wordCountMaps[ message.client ].put( word, count = new IntHolder( 1 ) );
						else
							count.value++;
					}
		}
		
		return wordCountMaps;
	}
	
	/**
	 * Sets the word count maps.
	 * @param wordCountMaps word count maps to be set
	 */
	public void setWordCountMaps( final Map< String, IntHolder >[] wordCountMaps ) {
		this.wordCountMaps = wordCountMaps;
	}
	
	@Override
	public IMessage[] getMessages() {
		return messages;
	}
	
}
