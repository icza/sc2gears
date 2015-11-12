/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.util;

import hu.belicza.andras.sc2gearsdb.Consts;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.ApiCallStatInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.DlStatInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.FileStatInfo;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PageInfo;
import hu.belicza.andras.sc2gearsdb.common.server.CommonUtils.DbPackage;
import hu.belicza.andras.sc2gearsdb.datastore.Account;
import hu.belicza.andras.sc2gearsdb.datastore.ApiAccount;
import hu.belicza.andras.sc2gearsdb.datastore.ApiCallStat;
import hu.belicza.andras.sc2gearsdb.datastore.DataStoreObject;
import hu.belicza.andras.sc2gearsdb.datastore.DownloadStat;
import hu.belicza.andras.sc2gearsdb.datastore.Event;
import hu.belicza.andras.sc2gearsdb.datastore.FileMetaData;
import hu.belicza.andras.sc2gearsdb.datastore.Rep;
import hu.belicza.andras.sc2gearsdb.datastore.Event.Type;
import hu.belicza.andras.sc2gearsdb.user.client.Permission;
import hu.belicza.andras.sc2gearsdb.user.client.beans.ReplayInfo;
import hu.belicza.andras.sc2gearsdbapi.ServletApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.AppEngineFile.FileSystem;
import com.google.appengine.api.users.User;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

/**
 * General utilities for the Sc2gears Database server side.
 * 
 * @author Andras Belicza
 */
public class ServerUtils {
	
	private static final Logger LOGGER = Logger.getLogger( ServerUtils.class.getName() );
	
	/** Lazily initialized country code - country name map. */
	private static final HashMap< String, String > COUNTRY_CODE_NAME_MAP = new HashMap< String, String >();
	
	/**
	 * Converts a 3166-1-alpha-2 country code (2-char country code) to the country name.
	 * @param countryCode 2-char country code whose country name to return
	 * @return the country name specified by the country code
	 */
	public static String countryCodeToName( final String countryCode ) {
		if ( COUNTRY_CODE_NAME_MAP.isEmpty() ) {
			// Load country code map
			// Source: http://www.iso.org/iso/list-en1-semic-3.txt
			// More:   http://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
			try ( final BufferedReader reader = new BufferedReader( new InputStreamReader( ServerUtils.class.getResourceAsStream( "list-en1-semic-3_2012-03-20.txt" ), "ISO-8859-1" ) ) ) {
				reader.readLine(); // Info line
				reader.readLine(); // Empty line
				
				String line;
				final StringBuilder nameBuilder = new StringBuilder();
				while ( ( line = reader.readLine() ) != null ) {
					final int semicolonIdx = line.indexOf( ';' );
					if ( semicolonIdx < 0 )
						continue;
					
					String name = line.substring( 0, semicolonIdx );
					final String code = line.substring( semicolonIdx + 1 );
					
					// Lowercase all non-first letters:
					nameBuilder.setLength( 0 );
					for ( int i = 0; i < name.length(); i++ )
						if ( i == 0 || name.charAt( i-1 ) == ' ' )
							nameBuilder.append( name.charAt( i ) );
						else
							nameBuilder.append( Character.toLowerCase( name.charAt( i ) ) );
					
					// Some words must start with lower-case letter:
					name = nameBuilder.toString();
					if ( name.endsWith( " Of" ) )
						name = name.substring( 0, name.length() - 2 ) + "of";
					if ( name.endsWith( " The" ) )
						name = name.substring( 0, name.length() - 3 ) + "the";
					name = name.replace( " And ", " and " ).replace( " The ", " the " ).replace( " Of ", " of " );
					
					COUNTRY_CODE_NAME_MAP.put( code, name );
				}
			} catch ( Exception e ) {
				LOGGER.log( Level.SEVERE, "Failed to load country code map!", e );
			}
		}
		
		return COUNTRY_CODE_NAME_MAP.get( countryCode );
	}
	
	/** Number formatter to format numbers. */
	public static final DecimalFormat DECIMAL_FORMAT;
	static {
		final DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
		newSymbols.setGroupingSeparator( ',' );
		DECIMAL_FORMAT = new DecimalFormat( "#,###", newSymbols );
	}
	
	/** Digits used in the hexadecimal representation. */
	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	/** Admin Google Account email address. */
	public static final String ADMIN_EMAIL_STRING = "iczaaa@gmail.com";
	
	/** The admin's email address. */
	private static InternetAddress ADMIN_EMAIL;
	static {
		try {
			ADMIN_EMAIL = new InternetAddress( ADMIN_EMAIL_STRING, "Andras Belicza" );
		} catch ( final UnsupportedEncodingException uee ) {
			LOGGER.log( Level.SEVERE, "Failed to create admin email address!", uee );
			throw new RuntimeException( uee );
		}
	}
	
	/**
	 * Generates a "highly" random string key.<br>
	 * Key characters: capital letters and numbers.<br>
	 * Key format: 5 groups of letters, each contains 5 letters, groups are separated with dashes.<br>
	 * Example: D9OJF-YESQI-S9MKP-4CZXT-PWYJZ
	 * 
	 * @return a "highly" random authorization key
	 */
	public static String generateRandomStringKey() {
		final char[]        KEY_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		final Random        random      = new Random();
		final StringBuilder keyBuilder  = new StringBuilder();
		
		for ( int i = 0; i < 5; i++ ) { // 5 groups
			if ( i > 0 )
				keyBuilder.append( '-' );
			for ( int j = 0; j < 5; j++ ) { // 5 characters in each group
				// If we don't change the seed before (or after) each use, the key space (number of combinations) would not be greater that the number of Longs!
				random.setSeed( new Object().hashCode() + new Random().nextLong() + System.nanoTime() + Runtime.getRuntime().freeMemory() + ( ( (long) new Object().hashCode() ) << 32 ) );
				keyBuilder.append( KEY_CHARSET[ random.nextInt( KEY_CHARSET.length ) ] );
			}
		}
		
		return keyBuilder.toString();
	}
	
	/**
	 * Checks the storage quota of the specified account, and sends notification email if quota is near to 1 or if it is over 1.
	 * @param account         account whose quota to be checked
	 * @param storageUsed     storage used by the account
	 * @param sizeIncrement   storage size increment being registered
	 * @return true if storage quota is exceeded; false otherwise
	 */
	public static boolean checkAccountQuota( final Account account, final long storageUsed, final long sizeIncrement ) {
		final long storageAfter = storageUsed + sizeIncrement;
		
		if ( storageAfter > account.getPaidStorage() ) {
			// Check last sent message
			final Event lastEvent = getLastEvent( account.getKey(), Type.STORAGE_QUOTA_EXCEEDED );
			if ( lastEvent != null && lastEvent.getDate().getTime() + 480L * 60 * 1000 > System.currentTimeMillis() )
				return true; // If we sent this kind of message in the last 8 hours, do not send it again (to avoid spamming)
			
			logEvent( account.getKey(), Type.STORAGE_QUOTA_EXCEEDED );
			
			// Storage quota exceeded
			final String body = ServerUtils.concatenateLines(
					"Hi " + account.getAddressedBy() + ",", null,
					"Your Sc2gears Database account exceeded storage limit!", null,
					"You can check your storage limit here:",
					"https://sciigears.appspot.com/User.html", null, 
					"You can purchase additional storage. For price details visit your User page (above) or see:",
					"https://sites.google.com/site/sc2gears/sc2gears-database", null,
					"Regards,",
					"   Andras Belicza" );
			ServerUtils.sendEmail( account, "Account exceeded storage quota", body );
			
			return true;
		}
		
		// Notification quota level is checked in the task servlet, update file store stats task
		
		return false;
	}
	
	/**
	 * Sends an email from the admin's email address to the specified account.
	 * @return true if no error occurred, false otherwise
	 */
	public static boolean sendEmail( final Account toAccount, final String subject, final String body ) {
		try {
			final InternetAddress to = new InternetAddress( toAccount.getUser().getEmail() );
			final InternetAddress cc = toAccount.getContactEmail() == null ? null : new InternetAddress( toAccount.getContactEmail() );
			return sendEmail( ADMIN_EMAIL, to, cc, subject, body );
		} catch ( final AddressException e ) {
			LOGGER.log( Level.SEVERE, "", e );
		}
		return false;
	}
	
	/**
	 * Sends an email from the admin's email address to the specified API account.
	 * @return true if no error occurred, false otherwise
	 */
	public static boolean sendEmail( final ApiAccount toApiAccount, final String subject, final String body ) {
		try {
			final InternetAddress to = new InternetAddress( toApiAccount.getUser().getEmail() );
			final InternetAddress cc = toApiAccount.getContactEmail() == null ? null : new InternetAddress( toApiAccount.getContactEmail() );
			return sendEmail( ADMIN_EMAIL, to, cc, subject, body );
		} catch ( final AddressException e ) {
			LOGGER.log( Level.SEVERE, "", e );
		}
		return false;
	}
	
	/**
	 * Sends an email to the admin's email address.
	 * @return true if no error occurred, false otherwise
	 */
	public static boolean sendEmailToAdmin( final String subject, final String message ) {
		return sendEmail( ADMIN_EMAIL, ADMIN_EMAIL, null, subject,
				concatenateLines( "Hi iczaaa,", null, "This is an automated message to notify you that:", null ) + message );
	}
	
	/**
	 * Sends an email from the admin's email address.
	 * Calls {@link ServerUtils#sendEmail(InternetAddress, InternetAddress, InternetAddress, String, String)}
	 * @return true if no error occurred, false otherwise
	 */
	public static boolean sendEmail( final InternetAddress to, final InternetAddress cc, final String subject, final String body ) {
		return sendEmail( ADMIN_EMAIL, to, cc, subject, body );
	}
	
	/**
	 * Sends an email.
	 * @return true if no error occurred, false otherwise
	 */
	public static boolean sendEmail( final InternetAddress from, final InternetAddress to, final InternetAddress cc, final String subject, final String body ) {
		LOGGER.info( "Sending email to: " + to.toString() + ", subject: " + subject );
		final Session session = Session.getDefaultInstance( new Properties(), null );
		try {
			final Message message = new MimeMessage( session );
			message.setFrom( from );
			message.addRecipient( Message.RecipientType.TO, to );
			if ( cc != null )
				message.addRecipient( Message.RecipientType.CC, cc );
			message.addRecipient( Message.RecipientType.BCC, ADMIN_EMAIL );
			message.setSubject( "[Sc2gears Database] " + subject );
			message.setText( body );
			Transport.send( message );
			return true;
		} catch ( final Exception e ) {
			LOGGER.log( Level.SEVERE, "Failed to send email!", e );
			return false;
		}
	}
	
	/**
	 * Concatenates strings treated as lines. They are concatenated separated with a line terminator.
	 * @param lines lines to concatenate
	 * @return the concatenated lines as a string
	 */
	public static String concatenateLines( final String... lines ) {
		final StringWriter sw = new StringWriter();
		final PrintWriter  pw = new PrintWriter( sw );
		
		for ( final String line : lines )
			pw.println( line == null ? "" : line );
		
		pw.flush();
		return sw.toString();
	}
	
	/**
	 * Calculates the SHA-1 digest of the specified byte array.
	 * @param data byte array whose SHA-1 digest to be calculated
	 * @return the calculated SHA-1 digest of the specified byte array
	 */
	public static String calculateSha1( final byte[] data ) {
		return calculateDigest( data, "SHA-1" );
	}
	
	/**
	 * Calculates the digest of the specified byte array.
	 * @param data      data whose digest to be calculated
	 * @param algorithm algorithm to be used to calculate the digest
	 * @return the calculated digest of the specified byte array
	 */
	private static String calculateDigest( final byte[] data, final String algorithm ) {
		try {
			return convertToHexString( MessageDigest.getInstance( algorithm ).digest( data ) );
		} catch ( final NoSuchAlgorithmException nsae ) {
			// This should never happen
			LOGGER.log( Level.SEVERE, "", nsae );
			throw new RuntimeException( nsae );
		}
	}
	
	/**
	 * Converts the specified data to hex string.
	 * @param data data to be converted
	 * @return the specified data converted to hex string
	 */
	public static String convertToHexString( final byte[] data ) {
		final StringBuilder hexBuilder = new StringBuilder( data.length << 1 );
		
		for ( final byte b : data )
			hexBuilder.append( HEX_DIGITS[ ( b & 0xff ) >> 4 ] ).append( HEX_DIGITS[ b & 0x0f ] );
		
		return hexBuilder.toString();
	}
	
	/**
	 * Generates and returns a unique name that is not contained in the specified name set.<br>
	 * If the provided file name is not contained, it is returned.<br>
	 * If it is contained, <code>" (2)"</code> will be appended to the end of the name. If this does not exist, it will be returned.<br>
	 * If it is contained, <code>" (3)"</code> will be appended to the end of the original name. This will go on until a unique name is found;
	 * and then it is returned.
	 * 
	 * @param name    file name to be checked and returned a unique name for
	 * @param nameSet set of reserved
	 * 
	 * @return a unique file name that is not contained
	 */
	public static String generateUniqueName( final String fileName, final Set< String > nameSet ) {
		String nameOnly  = null;
		String extension = null;
		
		String candidate = fileName;
		int i = 2;
		while ( nameSet.contains( candidate ) ) {
			if ( i == 2 ) {
				final int lastDotIndex = fileName.lastIndexOf( '.' );
				nameOnly  = lastDotIndex < 0 ? fileName : fileName.substring( 0, lastDotIndex );
				extension = lastDotIndex < 0 ? "" : fileName.substring( lastDotIndex, fileName.length() );
			}
			
			candidate = nameOnly + " (" + ( i++ ) + ")" + extension;
		}
		
		return candidate;
	}
	
	/**
	 * Encodes an input string for HTML rendering.
	 * @param input        input string to be encoded
	 * @param newLinesToBr tells if new lines have to be converted to <code>&lt;BR&gt;</code> elements
	 * @return an encoded string for HTML rendering
	 */
	public static String encodeHtmlString( String input, final boolean newLinesToBr ) {
		final StringBuilder encodedHtml = new StringBuilder();
		
		input = input.replace( "\r\n", "\n" );
		
		final int length = input.length();
		
		for ( int i = 0; i < length; i++ ) {
			final char ch = input.charAt( i );
			
			if ( ch >= 'a' && ch <='z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' )
				encodedHtml.append( ch ); // safe
			else if ( ch == '\n' )
				encodedHtml.append( newLinesToBr ? "<br>" : "\n" );
			else if ( Character.isISOControl( ch ) )
				encodedHtml.append( "&middot;" ); // Not safe, substitute it in the output
			else
				encodedHtml.append( "&#" ).append( (int) ch ).append( ';' );
		}
		
		return encodedHtml.toString();
	}
	
	/**
	 * Guesses the format from the specified race match-up. 
	 * @param raceMatchup race match-up to guess the format from
	 * @return the guessed format or <code>null</code> if format could not be guessed
	 */
	public static String guessFormat( final String raceMatchup ) {
		// TODO Replace this with ReplayUtils.guessFormat() (it's included in the replay parsing engine jar)
		if ( raceMatchup == null || raceMatchup.isEmpty() )
			return null;
		
		int teamsCount = 1;
		for ( int i = raceMatchup.length() - 2; i > 0; i-- )
			if ( raceMatchup.charAt( i ) == 'v' )
				teamsCount++;
		
		if ( teamsCount < 2 )
			return null;
		
		if ( teamsCount == 2 )
			switch ( raceMatchup.length() ) {
			case 3 : if ( raceMatchup.charAt( 1 ) == 'v' ) return "1v1"; break; // PvZ
			case 5 : if ( raceMatchup.charAt( 2 ) == 'v' ) return "2v2"; break; // PPvZZ
			case 7 : if ( raceMatchup.charAt( 3 ) == 'v' ) return "3v3"; break; // PPPvZZZ
			case 9 : if ( raceMatchup.charAt( 4 ) == 'v' ) return "4v4"; break; // PPPPvZZZZ
			}
		else {
			// Check if FFA (examples: ZvZvP, PvPvPvP, PvPvPvPvPvPvPvP)
			boolean ffa = true;
			for ( int i = 1; i < raceMatchup.length(); i += 2 )
				if ( raceMatchup.charAt( i ) != 'v' ) {
					ffa = false;
					break;
				}
			if ( ffa )
				return "FFA";
		}
		
		return null;
	}
	
	/**
	 * Returns a normalized form of the specified match-up string (which can be either a race match-up or a league match-up).
	 * 
	 * <p>The gain of this normalization is that 2 different match-ups which are the permutations of each other
	 * have the same normalized form therefore match-up permutations can be queried based on a simple equality filter
	 * (where we compare the normalized forms of the match-up filter and the match-up of the entities).</p>
	 * 
	 * <p>Normalization consists of the following steps:
	 * <ol>
	 * 		<li>make match-up lower-cased (e.g. <code>"ZZPvTPZ"</code> => <code>"zzpvtpz"</code>)
	 * 		<li>sort letters inside teams (e.g. <code>"zzpvtpz"</code> => <code>"pzzvptz"</code>)
	 * 		<li>sort teams (e.g. <code>"pzzvptz"</code> => <code>"ptzvpzz"</code>)
	 * </ol></p>
	 * 
	 * @param matchup match-up to be normalized
	 * @return a normalized form of the specified match-up string
	 */
	public static String normalizeMatchupString( final String matchup ) {
		if ( matchup == null )
			return null;
		
		// Step 1: lower-case
		final String[] teams = matchup.toLowerCase().split( "v" );
		
		// Step 2: sort letters inside teams
		for ( int i = 0; i < teams.length; i++ ) {
			final char[] letters = teams[ i ].toCharArray();
			Arrays.sort( letters );
			teams[ i ] = new String( letters );
		}
		
		// Step 3: sort teams
		Arrays.sort( teams );
		
		// Concatenate teams and return the normalized form:
		final StringBuilder builder = new StringBuilder();
		for ( int i = 0; i < teams.length; i++ ) {
			if ( i > 0 )
				builder.append( 'v' );
			builder.append( teams[ i ] );
		}
		
		return builder.toString();
	}
	
	/**
	 * Copies the properties from the specified replay to the specified replay info.
	 * @param replay     replay to copy properties from
	 * @param replayInfo replay info to copy properties to
	 */
	public static void copyReplayToReplayInfo( final Rep replay, final ReplayInfo replayInfo ) {
		if ( replay.getLabels() != null )
			replayInfo.setLabels( cloneList( replay.getLabels() ) );
		
		replayInfo.setVersion      ( replay.getVer    () );
		replayInfo.setReplayDate   ( replay.getRepd   () );
		replayInfo.setGameLength   ( replay.getLength () );
		replayInfo.setGateway      ( replay.getGw     () );
		replayInfo.setGameType     ( replay.getType   () );
		replayInfo.setMapName      ( replay.getMap    () );
		replayInfo.setMapFileName  ( replay.getMapf   () );
		replayInfo.setLeagueMatchup( replay.getLeagues() );
		replayInfo.setRaceMatchup  ( replay.getMatchup() );
		replayInfo.setPlayers      ( cloneList( replay.getPlayers() ) );
		replayInfo.setPlayerTeams  ( cloneList( replay.getTeams  () ) );
		replayInfo.setWinners      ( cloneList( replay.getWinners() ) );
		replayInfo.setSha1         ( replay.getSha1   () );
		replayInfo.setFileName     ( replay.getFname  () );
	}
	
	/**
	 * Returns the label name list of an account.
	 * @param account account whose label name list to return
	 * @return the label name list of an account
	 */
	public static List< String > getLabelNames( final Account account ) {
		if ( account.getLabelNames() == null )
			return Consts.DEFAULT_REPLAY_LABEL_LIST;
		else {
			final List< String > savedNames = account.getLabelNames();
			final List< String > labelNames = new ArrayList< String >( Consts.DEFAULT_REPLAY_LABEL_LIST.size() );
			
			for ( int i = 0; i < Consts.DEFAULT_REPLAY_LABEL_LIST.size(); i++ )
				labelNames.add( i >= savedNames.size() || savedNames.get( i ).isEmpty() ? Consts.DEFAULT_REPLAY_LABEL_LIST.get( i ) : savedNames.get( i ) );
			
			return labelNames;
		}
	}
	
	/**
	 * Sets a cursor to a query so the next execute() method will return results after the last query
	 * @param query           query to set the cursor of
	 * @param lastQueryResult reference to the last query result list
	 */
	public static void setQueryCursor( final Query query, final List< ? > lastQueryResult ) {
		final Cursor cursor = JDOCursorHelper.getCursor( lastQueryResult );
		
		final Map< String, Object > extensionMap = new HashMap< String, Object >( 2 ); // initial size of 2 because 1 with default load factor=0.75 would result in 0-size internal cache...
		extensionMap.put( JDOCursorHelper.CURSOR_EXTENSION, cursor );
		
		query.setExtensions( extensionMap );
	}
	
	/**
	 * Sets the page info to a query.
	 * @param query    query to set the page info of
	 * @param pageInfo page info to be set
	 */
	public static void setQueryPageInfo( final Query query, final PageInfo pageInfo ) {
		if ( pageInfo.getCursorString() == null )
			query.setRange( pageInfo.getOffset(), pageInfo.getOffset() + pageInfo.getLimit() );
		else {
			query.setRange( 0, pageInfo.getLimit() );
			
			final Map< String, Object > extensionMap = new HashMap< String, Object >( 2 ); // initial size of 2 because 1 with default load factor=0.75 would result in 0-size internal cache...
			extensionMap.put( JDOCursorHelper.CURSOR_EXTENSION, Cursor.fromWebSafeString( pageInfo.getCursorString() ) );
			
			query.setExtensions( extensionMap );
		}
	}
	
	/**
	 * Returns the DB package icon (relative image URL) for the specified DB package and storage parameters.
	 * @param dbPackage   DB package to return the icon for
	 * @param paidStorage paid storage (it might be different than the one defined by the <code>dbPackage</code>)
	 * @param usedStorage used storage
	 * @return the package icon for the specified DB package and storage parameters
	 */
	public static String getDbPackageIcon( final DbPackage dbPackage, final long paidStorage, final long usedStorage ) {
		if ( dbPackage == null )
			return null;
		
		int saturationClass = paidStorage == 0 ? 1 : 1 + (int) ( 4 * usedStorage / paidStorage );
		// Must be in the range of 1..4:
		if ( saturationClass < 1 )
			saturationClass = 1;
		else if ( saturationClass > 4 )
			saturationClass = 4;
		
		return "/images/dbpackages/" + dbPackage.iconName + "-" + saturationClass + ".png";
	}
	
	/**
	 * Checks if the specified email is syntactically correct (according to <a href="http://www.ietf.org/rfc/rfc822.txt">RFC 822</a>.
	 * @param email email to be checked
	 * @return true if the specified email is valid; false otherwise
	 */
	public static boolean isEmailValid( final String email ) {
		try {
			new InternetAddress( email, true );
			return true;
		} catch ( final AddressException ae ) {
			return false;
		}
	}
	
	/**
	 * Checks the specified user agent string.<br>
	 * If it's longer than 500, it will be shortened to 500 characters.
	 * Also "Java/" fragments will be shortened to "J/".
	 * 
	 * @param userAgent user agent string to be checked
	 * @return a checked, shortened user agent string
	 */
	public static String checkUserAgent( String userAgent ) {
		if ( userAgent == null )
			return null;
		
		userAgent = ServletApi.trimStringLength( userAgent, 500 );
		
		return userAgent.replace( "Sc2gears/", "S/" ).replace( "Sc2gearsUpdater/", "SU/" ).replace( "Java/", "J/" );
	}
	
	/**
	 * Returns the location string of the client taken from the specified HTTP request.
	 * 
	 * <p>It has the form of <code>"country;region;city"</code> where:
	 * <ul>
	 * 	<li><code>"country"</code> is the <a href="http://en.wikipedia.org/wiki/ISO_3166-1_alpha-2">ISO 3166-1 alpha-2</a> country code as reported by AppEngine in the <code>X-AppEngine-Country</code> HTTP header field
	 * 	<li><code>"region"</code> is the <a href="http://en.wikipedia.org/wiki/ISO_3166-2">ISO 3166-2</a> country specific region code as reported by AppEngine in the <code>X-AppEngine-Region</code> HTTP header field
	 * 	<li><code>"city"</code> is the city as reported by AppEngine in the <code>X-AppEngine-City</code> HTTP header field
	 * </ul>
	 * </p>
	 * 
	 * @param request request to get the location info from
	 * @return the location string of the client
	 */
	public static String getLocationString( final HttpServletRequest request ) {
		final StringBuilder cBuilder = new StringBuilder();
		String s;
		
		if ( ( s = request.getHeader( "X-AppEngine-Country" ) ) != null )
			cBuilder.append( s );
		cBuilder.append( ';' );
		
		if ( ( s = request.getHeader( "X-AppEngine-Region" ) ) != null )
			cBuilder.append( s );
		cBuilder.append( ';' );
		
		if ( ( s = request.getHeader( "X-AppEngine-City" ) ) != null )
			cBuilder.append( s );
		
		return cBuilder.toString();
	}
	
	/**
	 * Counts the new entities of the specified kind since the specified date.
	 * 
	 * <p>Implementation performs a query on the keys of the specified entity.
	 * The entity must have a creation date property <code>"date"</code>
	 * (and since the entity class extends {@link DataStoreObject}, it is ensured it has).</p>
	 * 
	 * @param ds          reference to the datastore service
	 * @param entityClass class of the entity whose new entities to count 
	 * @param fromDate    start date to count new entities from
	 * @return the number of new entities
	 */
	public static int countNewEntities( final DatastoreService ds, final Class< ? extends DataStoreObject > entityClass, final Date fromDate ) {
		final com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query( entityClass.getSimpleName() );
		q.setFilter( new com.google.appengine.api.datastore.Query.FilterPredicate( "date", FilterOperator.GREATER_THAN, fromDate ) );
		return countEntities( ds, q );
	}
	
	/**
	 * Counts the entities returned by the specified query.
	 * @param ds    reference to the datastore service
	 * @param query query whose results to count
	 * @return the number of entities returned by the query
	 */
	public static int countEntities( final DatastoreService ds, final com.google.appengine.api.datastore.Query q ) {
		q.setKeysOnly();
		
		final int          batchSize    = 1000;
		final FetchOptions fetchOptions = FetchOptions.Builder.withLimit( batchSize );
		
		Cursor cursor = null;
		int    count  = 0;
		while ( true ) {
			if ( cursor != null )
				fetchOptions.startCursor( cursor );
			
			final QueryResultList< Entity > resultList = ds.prepare( q ).asQueryResultList( fetchOptions );
			
			count += resultList.size();
			
			if ( resultList.size() < batchSize )
				return count;
			
			cursor = resultList.getCursor();
		}
	}
	
	/**
	 * Tells if the query result is empty.
	 * @param query query whose results to test
	 * @return true if the query result is empty; false otherwise
	 */
	public static boolean isQueryResultEmpty( final com.google.appengine.api.datastore.Query q ) {
		q.setKeysOnly();
		return 0 == DatastoreServiceFactory.getDatastoreService().prepare( q ).countEntities( FetchOptions.Builder.withLimit( 1 ) );
	}
	
	/**
	 * Returns the key of the single entity result of a query
	 * constructed from the specified entity class and an equality filter with the specified property name and value.
	 * 
	 * @param q query whose single entity result's key to be returned
	 * @param entityClass entity class to construct the query
	 * @param propName    property name to construct an equality filter for
	 * @param propValue   value of the specified property to filter for
	 * 
	 * @return the key of the single entity result of the constructed query; or <code>null</code> if the query result is empty
	 */
	public static Key getSingleKeyQueryResult( final Class< ? > entityClass, final String propName, final Object propValue ) {
		final com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query( entityClass.getSimpleName() );
		q.setFilter( new com.google.appengine.api.datastore.Query.FilterPredicate( propName, FilterOperator.EQUAL, propValue ) );
		
		return getSingleKeyQueryResult( q );
	}
	
	/**
	 * Returns the key of the single entity result of a query
	 * constructed from the specified entity class and 2 equality filters with the specified property names and values,
	 * connected with logical AND.
	 * 
	 * @param q query whose single entity result's key to be returned
	 * @param entityClass entity class to construct the query
	 * @param propName1   first property name to construct an equality filter for
	 * @param propValue1  first value of the specified property to filter for
	 * @param propName2   second property name to construct an equality filter for
	 * @param propValue2  second value of the specified property to filter for
	 * 
	 * @return the key of the single entity result of the constructed query; or <code>null</code> if the query result is empty
	 */
	public static Key getSingleKeyQueryResult( final Class< ? > entityClass, final String propName1, final Object propValue1, final String propName2, final Object propValue2 ) {
		final com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query( entityClass.getSimpleName() );
		q.setFilter( CompositeFilterOperator.and(
				new com.google.appengine.api.datastore.Query.FilterPredicate( propName1, FilterOperator.EQUAL, propValue1 ),
				new com.google.appengine.api.datastore.Query.FilterPredicate( propName2, FilterOperator.EQUAL, propValue2 )
		) );
		
		return getSingleKeyQueryResult( q );
	}
	
	/**
	 * Returns the key of the single entity result of the specified query.
	 * @param q query whose single entity result's key to be returned 
	 * @return the key of the single entity result of the specified query; or <code>null</code> if the query result is empty
	 */
	public static Key getSingleKeyQueryResult( final com.google.appengine.api.datastore.Query q ) {
		q.setKeysOnly();
		final Iterator< Entity > iterator = DatastoreServiceFactory.getDatastoreService().prepare( q ).asIterable( FetchOptions.Builder.withLimit( 1 ) ).iterator();
		return iterator.hasNext() ? iterator.next().getKey() : null;
	}
	
	/**
	 * Counts the new entities of the specified kind since the specified date.
	 * 
	 * <p>Implementation performs a query on the keys of the specified entity.
	 * The entity must have the key property <code>"key"</code>.</p>
	 * 
	 * @param pm          reference to the persistence manager
	 * @param entityClass class of the entity whose new entities to count 
	 * @param fromDate    start date to count new entities from
	 * @return the number of new entities
	 * 
	 * @deprecated Use of this method does not count toward <b>"Datastore Small Ops"</b> but toward <b>"Datastore Read Ops"</b>. Use {@link #countNewEntities(DatastoreService, Class, Date)} instead.
	 */
	public static int countNewEntitiesJdo( final PersistenceManager pm, final Class< ? extends DataStoreObject > entityClass, final Date fromDate ) {
		return countEntitiesJdo( pm.newQuery( "select key from " + entityClass.getName() + " where date>:1" ), fromDate );
	}
	
	/**
	 * Counts the entities returned by the specified query.
	 * @param query  query whose results to count
	 * @param params optional parameters of the query
	 * @return the number of entities returned by the query
	 * 
	 * @deprecated Use of this method does not count toward <b>"Datastore Small Ops"</b> but toward <b>"Datastore Read Ops"</b>. Use {@link #countEntities(DatastoreService, com.google.appengine.api.datastore.Query)} instead.
	 */
	public static int countEntitiesJdo( final Query query, final Object... params ) {
		int count = 0;
		
		query.setRange( 0, 1000 );
		
		while ( true ) {
			final List< ? > resultList = (List< ? >) query.executeWithArray( params );
			count += resultList.size();
			
			if ( resultList.size() < 1000 )
				return count;
			
			setQueryCursor( query, resultList );
		}
	}
	
	/**
	 * Checks if the specified map file name is valid.<br>
	 * 
	 * <p>The file name is the SHA-256 digest of its content ended with an <code>".s2ma"</code> extension.<br> 
	 * Valid example: <code>"05db4c70e14bb9bc2702b1ffea3535bcfe3c80b69616631ded5adcd34062029c.s2ma"</code></p>
	 * 
	 * <p>Another form is also accepted:<br>
	 * Example: <code>"05/db/05db4c70e14bb9bc2702b1ffea3535bcfe3c80b69616631ded5adcd34062029c.s2ma"</code></p>
	 * 
	 * @param mapFileName map file name to be checked
	 * @return a potentially modified, non-null map file name to be used if the input map file name is valid; <code>null</code> otherwise 
	 */
	public static String checkMapFileName( String mapFileName ) {
		if ( !mapFileName.endsWith( ".s2ma" ) )
			return null;
		
		if ( mapFileName.length() == 75 ) {
			final String prefix = new String( new char[] { mapFileName.charAt( 6 ), mapFileName.charAt( 7 ), '/', mapFileName.charAt( 8 ), mapFileName.charAt( 9 ) } );
			if ( !mapFileName.startsWith( prefix ) )
				return null;
			mapFileName = mapFileName.substring( 6 ); // Cut off prefix
		}
		
		if ( mapFileName.length() != 69 )
			return null;
		
		for ( int i = 63; i >= 0; i-- ) {
			final char ch = mapFileName.charAt( i );
			if ( ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f' )
				continue;
			return null;
		}
		
		return mapFileName;
	}
	
	/**
	 * Decodes a base64 string.
	 * @param base64 base64 string to be decoded
	 * @return the decoded content of the base64 string; or <code>null</code> if <code>base64</code> is not a valid base64 string
	 */
	public static byte[] decodeBase64String( final String base64 ) {
		try {
			return javax.xml.bind.DatatypeConverter.parseBase64Binary( base64 );
			// Alternative: decodedFileContent = com.google.gwt.user.server.Base64Utils.fromBase64( fileContent );
		} catch ( final IllegalArgumentException iae ) {
			return null;
		}
	}
	
	/**
	 * Integrates an {@link ApiCallStat} into an {@link ApiCallStatInfo}.
	 * @param apiCallStat API call stat to be integrated
	 * @param info        info to integrate into
	 */
	public static void integrateApiCallStatIntoInfo( final ApiCallStatInfo info, final ApiCallStat apiCallStat ) {
		info.setCalls           ( info.getCalls           () + apiCallStat.getCalls           () );
		info.setUsedOps         ( info.getUsedOps         () + apiCallStat.getUsedOps         () );
		info.setExecTime        ( info.getExecTime        () + apiCallStat.getExecTime        () );
		info.setDeniedCalls     ( info.getDeniedCalls     () + apiCallStat.getDeniedCalls     () );
		info.setErrors          ( info.getErrors          () + apiCallStat.getErrors          () );
		info.setInfoCalls       ( info.getInfoCalls       () + apiCallStat.getInfoCalls       () );
		info.setMapInfoCalls    ( info.getMapInfoCalls    () + apiCallStat.getMapInfoCalls    () );
		info.setParseRepCalls   ( info.getParseRepCalls   () + apiCallStat.getParseRepCalls   () );
		info.setProfInfoCalls   ( info.getProfInfoCalls   () + apiCallStat.getProfInfoCalls   () );
		info.setInfoExecTime    ( info.getInfoExecTime    () + apiCallStat.getInfoExecTime    () );
		info.setMapInfoExecTime ( info.getMapInfoExecTime () + apiCallStat.getMapInfoExecTime () );
		info.setParseRepExecTime( info.getParseRepExecTime() + apiCallStat.getParseRepExecTime() );
		info.setProfInfoExecTime( info.getProfInfoExecTime() + apiCallStat.getProfInfoExecTime() );
	}
	
	/**
	 * Converts a {@link DownloadStat} to a {@link DlStatInfo}.
	 * @param downloadStat download stat to be converted
	 * @return the converted {@link DlStatInfo}
	 */
	public static DlStatInfo convertDownloadStatToDlInfo( final DownloadStat downloadStat ) {
		final DlStatInfo dlStatInfo = new DlStatInfo();
		
		dlStatInfo.setFileName  ( downloadStat.getFile      () );
		dlStatInfo.setCount     ( downloadStat.getCount     () );
		dlStatInfo.setJavaClient( downloadStat.getJavaClient() );
		dlStatInfo.setUnique    ( downloadStat.getUnique    () );
		dlStatInfo.setDate      ( downloadStat.getDate      () );
		
		return dlStatInfo;
	}
	
	/**
	 * Logs an event (saves it to the datastore).
	 * @param entityKey entity key bound to the event
	 * @param type      type of the event
	 */
	public static void logEvent( final Key entityKey, final Type type ) {
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			pm.makePersistent( new Event( entityKey, type ) );
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	/**
	 * Returns the last logged event specified by the entity key and type.
	 * @param entityKey entity key to search the event for
	 * @param type      type to search the event for
	 * @return the last logged event specified by the entity key and type or <code>null</code> if no such event is logged
	 */
	public static Event getLastEvent( final Key entityKey, final Type type ) {
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			final List< Event > eventList = new JQBuilder<>( pm, Event.class ).filter( "entityKey==p1 && type==p2", "KEY p1, Integer p2" ).desc( "date" ).range( 0, 1 ).get( entityKey, type.ordinal() );
			
			if ( eventList.isEmpty() )
				return null;
			
			return eventList.get( 0 );
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	/**
	 * Returns the Account being accessed.
	 * @param pm            reference to the persistence manager
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @param user          the authenticated Google account
	 * @return the account being accessed; or <code>null</code> if no account exists or is not granted for the specified user
	 */
	public static Account getAccount( final PersistenceManager pm, final String sharedAccount, final User user ) {
		final User accountUser = sharedAccount == null ? user : new User( sharedAccount, "gmail.com" );
		
		final JQBuilder< Account > q = new JQBuilder<>( pm, Account.class );
		
		final List< Account > accountList;
		if ( sharedAccount == null || ADMIN_EMAIL_STRING.equals( user.getEmail() ) )
			accountList = q.filter( "user==p1", "USER p1" ).get( accountUser );
		else
			accountList = q.filter( "user==p1 && grantedUsers==p2", "USER p1, USER p2" ).get( accountUser, user );
		
		return accountList.isEmpty() ? null : accountList.get( 0 );
	}
	
	/**
	 * Returns the Account key being accessed.
	 * @param pm            reference to the persistence manager
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @param user          the authenticated Google account
	 * @return the account key being accessed; or <code>null</code> if no account exists or is not granted for the specified user
	 */
	public static Key getAccountKey( final PersistenceManager pm, final String sharedAccount, final User user, final Permission requiredPermission ) {
		// Warning: This logic is duplicated in the method UserServiceImpl.getReplayInfoList() (for optimization reasons).
		// If changes are made, manual review and mirroring is required there.
		
		final User accountUser = sharedAccount == null ? user : new User( sharedAccount, "gmail.com" );
		
		// If we're accessing our own account, we can use the CachingService, keys are cached
		if ( sharedAccount == null || ADMIN_EMAIL_STRING.equals( user.getEmail() ) )
			return CachingService.getAccountKeyByUser( pm, accountUser );
		
		// Else we need to fetch the whole account to check the required permission
		final List< Account > accountList = new JQBuilder<>( pm, Account.class ).filter( "user==p1 && grantedUsers==p2", "USER p1, USER p2" ).get( accountUser, user );
		
		return accountList.isEmpty() || !accountList.get( 0 ).isPermissionGranted( user, requiredPermission ) ? null : accountList.get( 0 ).getKey();
	}
	
	public static FileStatInfo createFileStatInfoForAccount( final Account account ) {
		final DbPackage    dbPackage    = DbPackage.getFromStorage( account.getPaidStorage() );
		
		final FileStatInfo fileStatInfo = new FileStatInfo();
		
		fileStatInfo.setGoogleAccount ( account.getUser().getEmail() );
		fileStatInfo.setDbPackageName ( dbPackage.name );
		fileStatInfo.setPaidStorage   ( account.getPaidStorage() );
		fileStatInfo.setDbPackageIcon ( getDbPackageIcon( dbPackage, account.getPaidStorage(), 0 ) );
		
		fileStatInfo.setAddressedBy   ( account.getAddressedBy() );
		fileStatInfo.setCountry       ( account.getCountry    () );
		fileStatInfo.setComment       ( account.getComment    () );
		fileStatInfo.setAccountCreated( account.getDate       () );
		
		return fileStatInfo;
	}
	
	/**
	 * Returns the content of the specified file
	 * @param fileMetaData file meta data of the file
	 * @param fileService  optionally you can pass the file service reference if you already have it
	 * @return the content of the specified file or <code>null</code> if the file could not be found
	 * @throws IOException thrown if reading the file from from the Blobstore fails
	 */
	public static byte[] getFileContent( final FileMetaData fileMetaData, FileService fileService ) throws IOException {
		if (true) {
			return null;
		}
		
		if ( fileMetaData.getContent() != null ) {
			// File content is in the file meta data
			return fileMetaData.getContent().getBytes();
		}
		else {
			// Get content from the Blobstore
			if ( fileService == null )
				fileService = FileServiceFactory.getFileService();
			
			// final AppEngineFile   appeFile = fileService.getBlobFile( file.getBlobKey() ); // This code throws exception on migrated blobs!
			final AppEngineFile   appeFile = new AppEngineFile( FileSystem.BLOBSTORE, fileMetaData.getBlobKey().getKeyString() );
			final FileReadChannel channel  = fileService.openReadChannel( appeFile, false );
			final byte[]          content  = new byte[ (int) fileMetaData.getSize() ];
			final ByteBuffer      wrapper  = ByteBuffer.wrap( content );
			
			while ( channel.read( wrapper ) > 0 )
				;
			
			channel.close();
			
			return content;
		}
	}
	
	/**
	 * Initializes a new account, sets default values and settings.
	 * @param pm      reference to the persistent manager
	 * @param account new account to be initialized
	 */
	@SuppressWarnings( "unchecked" )
    public static void initializeNewAccount( final PersistenceManager pm, final Account account ) {
		account.setAuthorizationKey( generateRandomStringKey() );
		// We have to make sure the authorization key is unique:
		if ( !( (List< Key >) pm.newQuery( "select key from " + Account.class.getName() + " where authorizationKey==:1" ).execute( account.getAuthorizationKey() ) ).isEmpty() ) {
			// This will (likely) never happen, but just in case this will ensure that the same key will not be associated with multiple accounts.
			throw new RuntimeException( "Failed to save new key, please try again." );
		}
		
		account.setNotificationQuotaLevel( 90       ); // Default notification storage quota
		account.setConvertToRealTime     ( true     ); // Default time conversion preference
		account.setMapImageSize          ( 3        ); // Default map image size
		account.setDisplayWinners        ( 2        ); // Default display winners preference
		account.setComment               ( "UNUSED" );
	}
	
	/**
	 * Chooses an object by random, each object has its own weight in the choosing algorithm.
	 * @param <T> type of the objects to choose from and to return
	 * @param objs objects to choose one from
	 * @param weights weights of the objects
	 * @return a randomly chosen object
	 */
	public static < T > T chooseWeightedRandom( final T[] objs, final int[] weights ) {
		int sumWeights = 0;
		for ( final int weight : weights )
			sumWeights += weight;
		
    	final int random = (int) ( Math.random() * sumWeights );
		
    	sumWeights = 0;
    	for ( int i = 0; i < weights.length; i++ ) {
    		sumWeights += weights[ i ];
    		if ( random < sumWeights )
    			return objs[ i ];
    	}
    	
		return null;
	}
	
	/** Day format to be appended to file names.    */
	private static final DateFormat DAY_DATE_FORMAT   = new SimpleDateFormat( "yy-MM-dd" );
	/** Month format to be appended to file names. */
	private static final DateFormat MONTH_DATE_FORMAT = new SimpleDateFormat( "yy-MM" );
	
	/**
	 * Appends the current day to the specified file name.
	 * @param fileName file name to append the current day to
	 * @return the specified file name with the current day appended
	 */
	public static String appendDayToFileName( final String fileName ) {
		return appendDayToFileName( fileName, new Date() );
	}
	
	/**
	 * Appends the specified day to the specified file name.
	 * @param fileName file name to append the specified day to
	 * @return the specified file name with the specified day appended
	 */
	public static String appendDayToFileName( final String fileName, final Date day ) {
		return fileName + "@D" + DAY_DATE_FORMAT.format( day );
	}
	
	/**
	 * Appends the current month to the specified file name.
	 * @param fileName file name to append the current month to
	 * @return the specified file name with the current month appended
	 */
	public static String appendMonthToFileName( final String fileName ) {
		return appendMonthToFileName( fileName, new Date() );
	}
	
	/**
	 * Appends the specified month to the specified file name.
	 * @param fileName file name to append the specified month to
	 * @return the specified file name with the specified month appended
	 */
	public static String appendMonthToFileName( final String fileName, final Date month ) {
		return fileName + "@M" + MONTH_DATE_FORMAT.format( new Date() );
	}
	
	/**
	 * Clones a list, returns an empty list if list is <code>null</code>.
	 * 
	 * <p>Datanucleus returns its own implementation of list which is not serializable,
	 * we have to create a new one in order to make it transferable to the clients.</p>
	 * 
	 * @param list
	 * @return
	 */
	public static <T> List< T > cloneList( final List< T > list ) {
		if ( list == null )
			return new ArrayList<>();
		
		return new ArrayList<>( list );
	}
	
}
