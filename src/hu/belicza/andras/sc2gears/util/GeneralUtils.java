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
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.shared.SharedUtils;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.ProgressDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog.SettingsTab;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gearsdbapi.FileServletApi;
import hu.belicza.andras.sc2gearsdbapi.InfoServletApi;
import hu.belicza.andras.sc2gearsdbapi.FileServletApi.FileType;
import hu.belicza.andras.smpd.SmpdUtil;
import hu.belicza.andras.smpd.SmpdUtil.SmpdVer;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.ToolTipManager;

/**
 * General utility methods.
 * 
 * @author Andras Belicza
 */
public class GeneralUtils extends SharedUtils {
	
	/** Number of max retries if an Sc2gears Database storing fails. */
	private static final int MAX_SC2GEARS_DATABASE_STORE_RETRIES = 3;
	
	/**
	 * An IO file filter that accepts all SC2Replay files and directories.
	 */
	public static final FileFilter SC2REPLAY_FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept( final File pathname ) {
			return pathname.isDirectory() || pathname.getName().toLowerCase().endsWith( ".sc2replay" );
		}
	};
	
	/**
	 * An IO file filter that accepts only SC2Replay files and no directories.
	 */
	public static final FileFilter SC2REPLAY_FILES_ONLY_FILTER = new FileFilter() {
		@Override
		public boolean accept( final File pathname ) {
			return !pathname.isDirectory() && pathname.getName().toLowerCase().endsWith( ".sc2replay" );
		}
	};
	
	/**
	 * No need to instantiate this class.
	 */
	private GeneralUtils() {
	}
	
	/**
	 * Returns the name of a file without path and extension.
	 * @param file file whose name to be returned
	 * @return the name of a file without path and extension
	 */
	public static String getFileNameWithoutExt( final File file ) {
		final String fileName = file.getName();
		final int lastDotIndex = fileName.lastIndexOf( '.' );
		return lastDotIndex < 0 ? fileName : fileName.substring( 0, lastDotIndex );
	}
	
	/**
	 * Returns the extension of a file.
	 * @param file file whose extension to be returned
	 * @return the extension of a file
	 */
	public static String getFileExtension( final File file ) {
		final String fileName = file.getName();
		final int lastDotIndex = fileName.lastIndexOf( '.' );
		return lastDotIndex < 0 ? "" : fileName.substring( lastDotIndex, fileName.length() );
	}
	
	/**
	 * Returns if <code>f2</code> is a descendant of <code>f1</code>.<br>
	 * Also returns true if f2 is equals to f1.<br>
	 * Works for both files and folders.
	 * @param f1 possible ancestor to test
	 * @param f2 possible descendant to test
	 * @return true if <code>f2</code> is a descendant of <code>f1</code>
	 */
	public static boolean isDescendant( File f1, File f2 ) {
		f1 = f1.getAbsoluteFile();
		f2 = f2.getAbsoluteFile();
		
		while ( f2 != null )
			if ( f1.equals( f2 ) )
				return true;
			else
				f2 = f2.getParentFile();
		
		return false;
	}
	
	/**
	 * Copies the specified file to the target folder.<br>
	 * If target folder does not exist, first an attempt will be made to create it (recursively).<br>
	 * This copy method reserves the last modification date of the file.
	 * 
	 * @param sourceFile   source file to be copied
	 * @param targetFolder target folder to copy to
	 * @param buffer       optional buffer, if given it will be used for the I/O operations (if not provided, a new one will be created)
	 * @param targetName   optional name of the target file; if not given, the name of the source file will be used
	 * @return true if copy was successful; false otherwise
	 */
	public static boolean copyFile( final File sourceFile, final File targetFolder, byte[] buffer, final String targetName ) {
		if ( !targetFolder.exists() )
			if ( !targetFolder.mkdirs() )
				return false;
		
		if ( targetFolder.exists() && !targetFolder.isDirectory() )
			return false;
		
		final File targetFile = new File( targetFolder, targetName == null ? sourceFile.getName() : targetName );
		if ( targetFile.exists() ) {
			System.out.println( "Copy failed: target file already exists!" );
			return false;
		}
		
		try ( final FileInputStream inputStream  = new FileInputStream ( sourceFile ); final FileOutputStream outputStream = new FileOutputStream( targetFile ) ) {
			if ( buffer == null )
				buffer = new byte[ 4096 ];
			
			int bytesRead;
			while ( ( bytesRead = inputStream.read( buffer ) ) > 0 )
				outputStream.write( buffer, 0, bytesRead );
			outputStream.flush();
			
		} catch ( final Exception e ) {
			e.printStackTrace();
			return false;
		}
		
		targetFile.setLastModified( sourceFile.lastModified() );
		
		return true;
	}
	
	/**
	 * Splits a text at semicolons, and returns an array of the parts. Text must be terminated with semicolon!
	 * 
	 * <p>If there are 2 semicolons next to each other, or the string starts with a semicolon an empty string is added.</p>
	 * 
	 * <p>For example: ";as;;fd;" would result in {"","as","","fd"}</p>
	 * 
	 * @param text text to be split
	 * @return an array of the parts of the text split at semicolons
	 */
	public static String[] splitBySemicolon( final String text ) {
		final List< String > partList = new ArrayList< String >();
		
		int lastIndex = 0;
		int index;
		while ( ( index = text.indexOf( ';', lastIndex ) ) >= 0 ) {
			partList.add( text.substring( lastIndex, index ) );
			lastIndex = index + 1;
		}
		
		return partList.toArray( new String[ partList.size() ] );
	}
	
	/**
	 * Returns the maximum value of an int array.<br>
	 * If the array has no elements, 0 is returned.
	 * @param array array whose max to be returned
	 * @return the maximum value of an int array
	 */
	public static int maxValue( final int[] array ) {
		if ( array.length == 0 )
			return 0;
		
		int max = array[ 0 ];
		
		for ( int i = array.length - 1; i > 0; i-- )
			if ( max < array[ i ] )
				max = array[ i ];
		
		return max;
	}
	
	/**
	 * Returns the maximum value of a float array.<br>
	 * If the array has no elements, 0 is returned.
	 * @param array array whose max to be returned
	 * @return the maximum value of a float array
	 */
	public static float maxValue( final float[] array ) {
		if ( array.length == 0 )
			return 0;
		
		float max = array[ 0 ];
		
		for ( int i = array.length - 1; i > 0; i-- )
			if ( max < array[ i ] )
				max = array[ i ];
		
		return max;
	}
	
	/**
	 * Returns if the operating system running the application is Windows 7.
	 * @return true if the operating system running the application is Windows 7
	 */
	public static boolean isWindows7() {
		return "Windows 7".equals( OS_NAME );
	}
	
	/**
	 * Returns if the operating system running the application is Windows Vista.
	 * @return true if the operating system running the application is Windows Vista
	 */
	public static boolean isWindowsVista() {
		return "Windows Vista".equals( OS_NAME );
	}
	
	/**
	 * Returns if the operating system running the application is Windows XP.
	 * @return true if the operating system running the application is Windows XP
	 */
	public static boolean isWindowsXp() {
		return "Windows XP".equals( OS_NAME );
	}
	
	/**
	 * Returns the occurrences count of <code>element</code> in the specified array.
	 * @param array array in which to count occurrences of <code>element</code>
	 * @return the occurrences count of <code>element</code>  in the specified array
	 */
	public static int countElements( final Object[] array, final Object element ) {
		int occurrencesCount = 0;
		for ( final Object element_ : array )
			if ( element_.equals( element ) )
				occurrencesCount++;
		return occurrencesCount;
	}
	
	/**
	 * Returns an array which contains elements from <code>elements</code> that are not part of <code>excludeSet</code>.
	 * Elements in the returned array are in the same order as in the input array. The input array must have at least 1 element which cannot be null!
	 * @param <T>        type of the elements
	 * @param elements   array of the input elements
	 * @param excludeSet set of elements to be excluded
	 * @return an array which contains elements from <code>elements</code> that are not part of <code>excludeSet</code>
	 */
	@SuppressWarnings("unchecked")
	public static < T > T[] remainingElements( final T[] elements, final Set< T > excludeSet ) {
		final List< T > remainingList = new ArrayList< T >( elements.length - excludeSet.size() ); // A good guess to the new size
		
		for ( final T element : elements )
			if ( !excludeSet.contains( element ) )
				remainingList.add( element );
		
		return remainingList.toArray( (T[]) Array.newInstance( elements[ 0 ].getClass(), remainingList.size() ) );
	}
	
	/**
	 * Returns the square of the distance of 2 points.
	 * @param x1 x coordinate of the first point
	 * @param y1 y coordinate of the first point
	 * @param x2 x coordinate of the second point
	 * @param y2 y coordinate of the second point
	 * @return the square of the distance of 2 points
	 */
	public static int distanceSquare( int x1, int y1, int x2, int y2 ) {
		x1 >>= 16;
		y1 >>= 16;
		x2 >>= 16;
		y2 >>= 16;
		return ( x1 - x2 ) * ( x1 - x2 ) + ( y1 - y2 ) * ( y1 - y2 );
	}
	
	/**
	 * Formats the specified amount of seconds.
	 * @param seconds seconds to be formatted
	 * @return the formatted seconds
	 */
	public static String formatLongSeconds( long seconds ) {
		final StringBuilder builder = new StringBuilder();
		final long hours = seconds / 3600;
		builder.append( hours ).append( ':' );
		
		seconds %= 3600;
		final long minutes = seconds / 60;
		if ( minutes < 10 )
			builder.append( 0 );
		builder.append( minutes ).append( ':' );
		
		seconds %= 60;
		if ( seconds < 10 )
			builder.append( 0 );
		builder.append( seconds );
		
		return builder.toString();
	}
	
	/**
	 * Returns the list of auto replay folders.
	 * @return the list of auto replay folders
	 */
	public static List< File > getAutoRepFolderList() {
		final List< File > autorepFolderList = new ArrayList< File >( Settings.MAX_SC2_AUTOREP_FOLDERS );
		
		for ( int i = 1; i <= Settings.MAX_SC2_AUTOREP_FOLDERS; i++ ) {
			final String folderName;
			
			if ( i == 1 )
				folderName = Settings.getString( Settings.KEY_SETTINGS_FOLDER_SC2_AUTO_REPLAY );
			else {
				if ( !Settings.getBoolean( Settings.KEY_SETTINGS_FOLDER_ENABLE_EXTRA_SC2_AUTO_REPLAY + i ) )
					continue;
				folderName = Settings.getString( Settings.KEY_SETTINGS_FOLDER_SC2_AUTO_REPLAY + i );
			}
			
			final File relativeAutorepFolder = new File( folderName );
			autorepFolderList.add( relativeAutorepFolder.exists() ? relativeAutorepFolder : new File( Consts.FOLDER_USER_HOME, folderName ) );
		}
		
		return autorepFolderList;
	}
	
	/**
	 * Returns the favored player list.
	 * @return the favored player list
	 */
	public static List< String > getFavoredPlayerList() {
		final String favoredPlayerListString = Settings.getString( Settings.KEY_SETTINGS_MISC_FAVORED_PLAYER_LIST ).trim();
		
		if ( !favoredPlayerListString.isEmpty() ) {
			final List< String >  favoredPlayerList = new ArrayList< String >( 4 );
			final StringTokenizer playerTokenizer   = new StringTokenizer( favoredPlayerListString, " ," );
			
			while ( playerTokenizer.hasMoreTokens() )
				favoredPlayerList.add( playerTokenizer.nextToken() );
			
			return favoredPlayerList;
		}
		else
			return new ArrayList< String >( 0 );
	}
	
	/**
	 * Creates and returns a HashSet with the arguments.
	 * @param <T>    type of the arguments
	 * @param values values to put in the set
	 * @return a HashSet with the argument
	 */
	public static < T > Set< T > assembleHashSet( @SuppressWarnings("unchecked") final T... values ) {
		final Set< T > set = new HashSet< T >( values.length );
		
		for ( final T value : values )
			set.add( value );
		
		return set;
	}
	
	/**
	 * Downloads the specified URL to the specified file.
	 * @param url    URL to be downloaded
	 * @param toFile file to write the downloaded content to
	 * @return true if the download was successful; false otherwise
	 */
	public static boolean downloadUrl( final String url, final File toFile ) {
		try ( final InputStream contentStream = new URL( url ).openStream(); final FileOutputStream output = new FileOutputStream( toFile ) ) {
			final byte[] buffer = new byte[ 16*1024 ];
			int bytesRead;
			
			while ( ( bytesRead = contentStream.read( buffer ) ) > 0 )
				output.write( buffer, 0, bytesRead );
			
			output.flush();
			return true;
		} catch ( final Exception e ) {
			e.printStackTrace();
			return false;
		}
	}
	
	/** Time parser to be used for parsing min:sec format.      */
	private static final DateFormat MIN_SEC_PARSER      = new SimpleDateFormat( "m:s" );
	/** Time parser to be used for parsing hour:min:sec format. */
	private static final DateFormat HOUR_MIN_SEC_PARSER = new SimpleDateFormat( "H:m:s" );
	
	/**
	 * Tries to parse seconds from the given text.<br>
	 * It has 3 possible formats:
	 * <ul><li>an integer specifying the seconds
	 * <li>min:sec specifying minutes and seconds
	 * <li>hour:min:sec specifying hours, minutes and seconds</ul>
	 * 
	 * @param text text to parse seconds from
	 * @return the parsed seconds or <code>null</code> if format is invalid
	 */
	public static Integer parseSeconds( final String text ) {
		// First try the simple number format as seconds
		try {
			return Integer.parseInt( text );
		} catch ( final Exception e ) {
		}
		
		Date time = null;
		try {
			// Now try the h:m:s format, because if this one is specified, it also fits to the m:s format!
			time = HOUR_MIN_SEC_PARSER.parse( text );
		} catch ( final Exception e ) {
			try {
				time = MIN_SEC_PARSER.parse( text );
			} catch ( final Exception e2 ) {
			}
		}
		
		if ( time == null )
			return null;
		
		final Calendar calendar = new GregorianCalendar();
		calendar.setTime( time );
		return calendar.get( Calendar.HOUR_OF_DAY ) * 3600 + calendar.get( Calendar.MINUTE ) * 60 + calendar.get( Calendar.SECOND );
	}
	
	/**
	 * Returns the last replay (the latest replay) in the specified folder (recursive). 
	 * @param startFolder    folder to start searching in
	 * @param lastReplayFile the best candidate for being the last replay so far
	 * @return the last replay (the latest replay) in the specified folder (recursive)
	 * @see #getLastReplayFile()
	 */
	public static File getLastReplay( final File startFolder, File lastReplayFile ) {
		long lastReplayTime = lastReplayFile == null ? 0 : lastReplayFile.lastModified();
		final File[] files = startFolder.listFiles( GeneralUtils.SC2REPLAY_FILE_FILTER );
		if ( files == null )
			return lastReplayFile;
		for ( int i = files.length - 1; i >= 0; i-- ) { // Prolly the last is the last, so in order to minimize assignments, go downwards...
			final File file = files[ i ];
			if ( file.isDirectory() ) {
				lastReplayFile = getLastReplay( file, lastReplayFile );
				lastReplayTime = lastReplayFile == null ? 0 : lastReplayFile.lastModified();
			}
			else
				if ( file.lastModified() > lastReplayTime ) {
					lastReplayFile = file;
					lastReplayTime = file.lastModified();
				}
		}
		return lastReplayFile;
	}
	
	/**
	 * Returns the last replay (the latest replay) specified by the auto-save folders
	 * (returned bye {@link #getAutoRepFolderList()}.
	 * @return the last replay specified by the auto-save folders; or null if no replays found in the auto-rep folders
	 * @see #getLastReplay(File, File)
	 */
	public static File getLastReplayFile() {
		File lastReplayFile = null;
		
		for ( final File autoRepFolder : getAutoRepFolderList() )
			lastReplayFile = GeneralUtils.getLastReplay( autoRepFolder, lastReplayFile );
		
		return lastReplayFile;
	}
	
	/**
	 * Returns the default replay folder that should be set when opening/choosing replay(s).
	 * @return the default replay folder that should be set when opening/choosing replay(s)
	 */
	public static String getDefaultReplayFolder() {
		final String defaultReplayFolder = Settings.getString( Settings.KEY_SETTINGS_FOLDER_DEFAULT_REPLAY );
		// Return the autorep folder if no default rep folder is provided
		return defaultReplayFolder == null || defaultReplayFolder.isEmpty() ? Settings.getString( Settings.KEY_SETTINGS_FOLDER_SC2_AUTO_REPLAY ) : defaultReplayFolder;
	}
	
	/**
	 * Sets the tool tip delays.
	 */
	public static void setToolTipDelays() {
		ToolTipManager.sharedInstance().setInitialDelay( Settings.getInt( Settings.KEY_SETTINGS_MISC_TOOL_TIP_INITIAL_DELAY ) );
		ToolTipManager.sharedInstance().setDismissDelay( Settings.getInt( Settings.KEY_SETTINGS_MISC_TOOL_TIP_DISMISS_DELAY ) );
	}
	
	/**
	 * Returns the inverted color of a color.
	 * @param color color whose inverted color to be returned
	 * @return the inverted color of a color
	 */
	public static Color getInvertedColor( final Color color ) {
		final float[] hsb = Color.RGBtoHSB( color.getRed(), color.getGreen(), color.getBlue(), null );
		return new Color( Color.HSBtoRGB( 1-hsb[ 0 ], hsb[ 1 ], 1-hsb[ 2 ] ) );
	}
	
	/**
	 * Launches an external program.<br>
	 * The external program is launched by calling {@link Runtime#exec(String[])},
	 * <code>cmdArray</code> will be passed if specified or <code>new String[] { file.getAbsolutePath() }</code> if not.<br>
	 * This is necessary because in case of commands like "StarCraft II Editor.exe" it would be interpreted as
	 * "StarCraft II" with a "Editor.exe" parameter => would launch Sc2 instead of the Editor.
	 * 
	 * @param file starter file of the external program; if specified, it will be checked if it exists before attempting to launch, and if not, an error message will be displayed
	 * @param cmdArray array containing the command to call and its arguments
	 */
	private static void launchExternalProgram( final File file, final String[] cmdArray ) {
		if ( file != null && !file.exists() ) {
			GuiUtils.showErrorDialog( new Object[] { Language.getText( "misc.applicationDoesNotExist" ), file.getAbsolutePath(), " ", Language.getText( "misc.isStarCraft2FolderConfiguredProperly" )," ", MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.FOLDERS ) } );
			return;
		}
		
		try {
			Runtime.getRuntime().exec( cmdArray == null ? new String[] { file.getAbsolutePath() } : cmdArray );
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		}
	}
	
	/**
	 * Starts StarCraft II.
	 */
	public static void startStarCraftII() {
		launchExternalProgram( new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_SC2_INSTALLATION ), isMac() ? "StarCraft II.app/Contents/MacOS/StarCraft II" : "StarCraft II.exe" ), null );
	}
	
	/**
	 * Starts StarCraft II Editor.
	 */
	public static void startStarCraftIIEditor() {
		launchExternalProgram( new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_SC2_INSTALLATION ), isMac() ? "StarCraft II Editor.app/Contents/MacOS/StarCraft II Editor" : "StarCraft II Editor.exe" ), null );
	}
	
	/**
	 * Starts playing a replay in StarCraft II.<br>
	 * Due to StarCraft II it only works if StarCraft II is not running at the time when this is called.
	 */
	public static void launchReplay( final File replayFile ) {
		// TODO Test it, also investigate MAC OS-X
		final File sc2LauncherFile = new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_SC2_INSTALLATION ), isMac() ? "StarCraft II.app/Contents/MacOS/StarCraft II" : "Support/SC2Switcher.exe" );
		//final File sc2LauncherFile = new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_SC2_INSTALLATION ), isMac() ? "StarCraft II.app/Contents/MacOS/StarCraft II" : "StarCraft II.exe" );
		launchExternalProgram( sc2LauncherFile, new String[] { sc2LauncherFile.getAbsolutePath(), replayFile.getAbsolutePath() } );
	}
	
	/**
	 * Creates a new array with an empty string (" ") as an extra element in the first place, and the copy of the specified array.
	 * @param values array of values to be prepended with an extra element
	 * @return a new array with an empty string (" ") as an extra element in the first place, and the copy of the specified array.
	 */
	public static Object[] prependOneElement( final Object[] values ) {
		final Object[] result = new Object[ values.length + 1 ];
		result[ 0 ] = " ";
		System.arraycopy( values, 0, result, 1, values.length );
		return result;
	}
	
	/**
	 * Copies a text to the system clipboard.
	 * @param textToCopy text to be copied to the system clipboard
	 */
	public static void copyToClipboard( final String textToCopy ) {
		try {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents( new StringSelection( textToCopy ), null );
		} catch ( final IllegalStateException ise ) {
			// Just to make sure: on some platforms if the clipboard is being accessed by another application, this might happen 
		}
	}
	
	/**
	 * Reverses the specified map.<br>
	 * The returned map will contain the values as keys mapped to objects that were keys in the original map.
	 * @param <K> key type of the input map
	 * @param <V> value type of the input map
	 * @param map map to be reversed
	 * @return the reversed map
	 */
	public static < K, V > Map< V, K > reverseMap( final Map< K, V > map ) {
		final HashMap< V, K > reversedMap = new HashMap< V, K >( map.size() );
		
		for ( final Entry< K, V > entry : map.entrySet() )
			reversedMap.put( entry.getValue(), entry.getKey() );
		
		return reversedMap;
	}
	
	/**
	 * Converts a Byte list to a byte array.
	 * @param list list to be converted
	 * @return a byte array containing the elements of the specified Byte list
	 */
	public static byte[] toByteArray( final List< Byte > list ) {
		final byte[] array = new byte[ list.size() ];
		for ( int i = array.length - 1; i >= 0; i-- )
			array[ i ] = list.get( i );
		return array;
	}
	
	/**
	 * Converts a Short list to a byte array.
	 * @param list list to be converted
	 * @return a short array containing the elements of the specified Short list
	 */
	public static short[] toShortArray( final List< Short > list ) {
		final short[] array = new short[ list.size() ];
		for ( int i = array.length - 1; i >= 0; i-- )
			array[ i ] = list.get( i );
		return array;
	}
	
	/**
	 * Converts an Integer list to a byte array.
	 * @param list list to be converted
	 * @return an int array containing the elements of the specified Integer list
	 */
	public static int[] toIntArray( final List< Integer > list ) {
		final int[] array = new int[ list.size() ];
		for ( int i = array.length - 1; i >= 0; i-- )
			array[ i ] = list.get( i );
		return array;
	}
	
	/**
	 * Generates and returns a unique file name that does not exist.<br>
	 * If the provided file does not exist, it is returned.<br>
	 * If it exists, <code>" (2)"</code> will be appended to the end of the name. If this does not exist, it will be returned.<br>
	 * If it exists, <code>" (3)"</code> will be appended to the end of the original name. This will go on until a unique name is found;
	 * and then it is returned.
	 * 
	 * @param file file to be checked and returned a unique name for
	 * @return a unique file name that does not exist
	 */
	public static File generateUniqueName( final File file ) {
		String parent    = null;
		String origName  = null;
		String extension = null;
		
		File candidate = file;
		int i = 2;
		while ( candidate.exists() ) {
			if ( i == 2 ) {
				parent    = file.getParent();
				origName  = getFileNameWithoutExt( file );
				extension = getFileExtension( file );
			}
			
			candidate = new File( parent, origName + " (" + ( i++ ) + ")" + extension );
		}
		
		return candidate;
	}
	
	/**
	 * Tells if a value is listed in a comma separated list. The items in the list are not allowed to contain spaces.
	 * 
	 * <p>Example: <code>"apple"</code> is contained in the list: <code>"banana, apple, peach"</code>.</p>
	 * 
	 * @param value value to be searched
	 * @param list  comma separated list to search in
	 * @return true if the value is listed in the comma separated list; false otherwise
	 */
	public static boolean isValueInCommaSeparatedList( final String value, final String list ) {
		final int listLength = list.length();
		int pos = 0;
		while ( pos < listLength && ( pos = list.indexOf( value, pos ) ) >= 0 ) {
			char prevCh;
			// Is this the start of an item?
			if ( pos == 0 || ( prevCh = list.charAt( pos-1 ) ) == ',' || prevCh == ' ' ) {
				int end = pos + value.length();
				char nextCh;
				// Is this the end of an item?
				if ( end == listLength || ( nextCh = list.charAt( end ) ) == ',' || nextCh == ' ' )
					return true;
			}
			
			pos++;
		}
		
		return false;
	}
	
	/**
	 * Parses RGB values from a a comma separated string.
	 * @param rgbText comma separated list of RGB values
	 * @return an integer array of the parsed RGB values; or <code>null</code> if the text does not contains a valid RGB list
	 */
	public static int[] parseRgbString( final String rgbText ) {
		final String[] rgbStrings = rgbText.split( "," );
		if ( rgbStrings.length != 3 )
			return null;
		
		final int[] rgb = new int[ rgbStrings.length ];
		
		for ( int i = rgb.length - 1; i >= 0; i-- ) {
			rgb[ i ] = Integer.parseInt( rgbStrings[ i ].trim() );
			if ( rgb[ i ] < 0 || rgb[ i ] > 255 )
				return null;
		}
		
		return rgb;
	}
	
	/**
	 * Returns the Color value specified by an RGB string.<br>
	 * If <code>rgbString</code> is specified first its value is attempted to converted to a color.
	 * If <code>rgbString</code> is not specified or is invalid, the value of the setting specified by <code>key</code> is tried.
	 * If that also fails, the default value of that setting will be parsed.
	 * @param rgbString optional RGB string to parse the color from
	 * @param key key of the setting to parse the color from
	 * @return the Color value specified by an RGB string
	 */
	public static Color getColorSetting( final String rgbString, final String key ) {
		int[] rgb = null;
		if ( rgbString != null )
			rgb = parseRgbString( rgbString );
		
		if ( rgb == null ) {
			rgb = parseRgbString( Settings.getString( key ) );
			if ( rgb == null )
				rgb = parseRgbString( Settings.getDefaultString( key ) );
		}
		
		return new Color( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] );
	}
	
	/**
	 * Creates and returns an {@link ExecutorService} which uses a thread pool
	 * with a size defined in the {@link Settings}.
	 * @return a multi-threaded {@link ExecutorService} with the proper thread pool size
	 */
	public static ExecutorService createMultiThreadedExecutorService() {
		int coresCount = Settings.getInt( Settings.KEY_SETTINGS_MISC_UTILIZED_CPU_CORES );
		
		if ( coresCount == 0 )
			coresCount = Runtime.getRuntime().availableProcessors();
		
		return Executors.newFixedThreadPool( coresCount );
	}
	
	/**
	 * Shuts down properly the specified executor service.
	 * @param executorService executor service to be shut down
	 * @return true if the executor service was shut down properly; false if it still has tasks running
	 */
	public static boolean shutdownExecutorService( final ExecutorService executorService ) {
		executorService.shutdown();
		
		try {
			return executorService.awaitTermination( 100l, TimeUnit.SECONDS ); // Give some time to finish running tasks (it should terminate below 1 second..)
        } catch ( final InterruptedException ie ) {
            ie.printStackTrace();
            // Preserve the interrupted status of the current thread
            Thread.currentThread().interrupt();
            return false;
        }
	}
	
	/**
	 * Checks if the specified authorization key is valid.<br>
	 * Connects to the Sc2gears Database to check the key.
	 * @param authorizationKey authorization key to be checked
	 * @return Boolean.TRUE if the specified authorization key is valid; Boolean.FALSE if the key is invalid; or <code>null</code> if the check failed
	 */
	public static Boolean checkAuthorizationKey( final String authorizationKey ) {
		HttpPost httpPost = null;
		try {
			// I use POST so the authorization key will not appear in the URL, because in case of errors the URL will appear in the log
			final Map< String, String > paramsMap = new HashMap< String, String >();
			paramsMap.put( InfoServletApi.PARAM_PROTOCOL_VERSION , InfoServletApi.PROTOCOL_VERSION_1  );
			paramsMap.put( InfoServletApi.PARAM_OPERATION        , InfoServletApi.OPERATION_CHECK_KEY );
			paramsMap.put( InfoServletApi.PARAM_AUTHORIZATION_KEY, authorizationKey                   );
			
			httpPost = new HttpPost( Consts.URL_SC2GEARS_DATABASE_INFO_SERVLET, paramsMap );
			if ( httpPost.connect() )
				if ( httpPost.doPost() ) {
					final List< String > response = httpPost.getResponseLines();
					if ( response != null ) {
						return Boolean.valueOf( response.get( 0 ) );
					}
				}
		} catch ( final Exception e ) {
			e.printStackTrace();
		} finally {
			if ( httpPost != null )
				httpPost.close();
		}
		return null;
	}
	
	/**
	 * Checks the Authorization key before storing or downloading files.
	 * @param owner owner optional frame to be used as parent for error dialogs
	 * @return the Authorization key if it is valid; <code>null</code> otherwise
	 */
	public static String checkKeyBeforeStoringOrDownloading( final Frame... owner ) {
		final String authorizationKey = Settings.getString( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY );
		Boolean keyValid = null;
		
		if ( !authorizationKey.isEmpty() ) {
			keyValid = GeneralUtils.checkAuthorizationKey( authorizationKey );
			if ( keyValid == null ) {
				GuiUtils.showErrorDialog( Language.getText( "replayops.failedAuthKeyCheck" ) );
				return null;
			}
		}
		
		if ( authorizationKey.isEmpty() || !keyValid ) {
			GuiUtils.showErrorDialog( new Object[] { Language.getText( authorizationKey.isEmpty() ? "replayops.missingAuthKey" : "replayops.invalidAuthKey" ), Language.getText( "replayops.authKeyRequired" ), " ", MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.SC2GEARS_DATABASE ) }, owner );
			return null;
		}
		
		return authorizationKey;
	}
	
	/**
	 * Stores a replay in the Sc2gears Database.
	 * @param file             replay file to store
	 * @param authorizationKey authorization key to use
	 * @return true if replay was stored successfully; false otherwise
	 */
	public static boolean storeReplay( final File file, final String authorizationKey ) {
		final Replay replay = ReplayFactory.parseReplay( file.getAbsolutePath(), ReplayFactory.GENERAL_DATA_CONTENT );
		if ( replay == null ) {
			System.out.println( "Failed to store file in the Sc2gears Database: failed to parse replay: " + file.getAbsolutePath() );
			return false;
		}
		
		if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_REARRANGE_PLAYERS_IN_REP_ANALYZER ) )
			ReplayUtils.applyFavoredPlayerListSetting( replay.details );
		
		final StringBuilder playerTeamsBuilder = new StringBuilder();
		final StringBuilder winnersBuilder     = new StringBuilder();
		for ( final int playerIndex : replay.details.getTeamOrderPlayerIndices() ) {
			final Player player = replay.details.players[ playerIndex ];
			if ( playerTeamsBuilder.length() > 0 )
				playerTeamsBuilder.append( ',' );
			playerTeamsBuilder.append( player.team );
			if ( player.isWinner != null && player.isWinner ) { 
				if ( winnersBuilder.length() > 0 )
					winnersBuilder.append( ',' );
				winnersBuilder.append( player.playerId.name );
			}
		}
		
		final Map< String, String > paramsMap = new HashMap< String, String >();
		paramsMap.put( FileServletApi.PARAM_VERSION       , replay.version                           );
		paramsMap.put( FileServletApi.PARAM_REPLAY_DATE   , Long.toString( replay.details.saveTime ) );
		paramsMap.put( FileServletApi.PARAM_GAME_LENGTH   , Integer.toString( replay.gameLengthSec ) );
		paramsMap.put( FileServletApi.PARAM_GATEWAY       , replay.initData.gateway.stringValue      );
		paramsMap.put( FileServletApi.PARAM_GAME_TYPE     , replay.initData.gameType.stringValue     );
		paramsMap.put( FileServletApi.PARAM_MAP_NAME      , FileServletApi.trimMapName( replay.details.mapName ) );
		paramsMap.put( FileServletApi.PARAM_MAP_FILE_NAME , replay.initData.mapFileName == null ? "" : FileServletApi.trimMapFileName( replay.initData.mapFileName ) );
		paramsMap.put( FileServletApi.PARAM_FORMAT        , replay.initData.format.stringValue       );
		paramsMap.put( FileServletApi.PARAM_LEAGUE_MATCHUP, replay.details.getLeagueMatchup()        );
		paramsMap.put( FileServletApi.PARAM_RACE_MATCHUP  , replay.details.getRaceMatchup()          );
		paramsMap.put( FileServletApi.PARAM_PLAYERS       , replay.details.getPlayerNames()          );
		paramsMap.put( FileServletApi.PARAM_PLAYER_TEAMS  , playerTeamsBuilder.toString()            );
		paramsMap.put( FileServletApi.PARAM_WINNERS       , winnersBuilder.toString()                );
		
		return storeFile( file, authorizationKey, FileType.SC2REPLAY, paramsMap );
	}
	
	/**
	 * Stores a mouse print in the Sc2gears Database.
	 * @param file             mouse print file to store
	 * @param authorizationKey authorization key to use
	 * @return true if mouse print was stored successfully; false otherwise
	 */
	public static boolean storeMousePrint( final File file, final String authorizationKey ) {
		try ( final DataInputStream dataInput = new DataInputStream( new FileInputStream( file ) ) ) {
			final byte[] magic = new byte[ 4 ];
			dataInput.read( magic );
			if ( !Arrays.equals( magic, SmpdUtil.SMPD_MAGIC ) )
				throw new Exception( "Invalid SMPD magic!" );
			
			final short   binaryVersion = dataInput.readShort();
			final SmpdVer smpdVer       = SmpdVer.fromBinaryValue( binaryVersion );
			if ( smpdVer == SmpdVer.UNKNOWN )
				throw new Exception( "Unsupported SMPD version: " + SmpdUtil.getVersionString( binaryVersion ) );
			
			final int headerLength = dataInput.readInt();
			if ( headerLength < smpdVer.minHeaderSize )
				throw new Exception( "Header length too small, should be at least " + smpdVer.minHeaderSize + "!" );
			
			final long startTime            = dataInput.readLong();
			final long endTime              = dataInput.readLong();
			final int  screenWidth          = dataInput.readInt();
			final int  screenHeight         = dataInput.readInt();
			final int  screenResolution     = dataInput.readInt();
			final int  samplingTime         = dataInput.readInt();
			final int  samplesCount         = dataInput.readInt();
			final int  uncompressedDataSize = dataInput.readInt();
			final int  savedWithCompression = dataInput.read();
			
			final Map< String, String > paramsMap = new HashMap< String, String >();
			paramsMap.put( FileServletApi.PARAM_VERSION               , Short  .toString( binaryVersion        ) );
			paramsMap.put( FileServletApi.PARAM_RECORDING_START       , Long   .toString( startTime            ) );
			paramsMap.put( FileServletApi.PARAM_RECORDING_END         , Long   .toString( endTime              ) );
			paramsMap.put( FileServletApi.PARAM_SCREEN_WIDTH          , Integer.toString( screenWidth          ) );
			paramsMap.put( FileServletApi.PARAM_SCREEN_HEIGHT         , Integer.toString( screenHeight         ) );
			paramsMap.put( FileServletApi.PARAM_SCREEN_RESOLUTION     , Integer.toString( screenResolution     ) );
			paramsMap.put( FileServletApi.PARAM_SAMPLING_TIME         , Integer.toString( samplingTime         ) );
			paramsMap.put( FileServletApi.PARAM_SAMPLES_COUNT         , Integer.toString( samplesCount         ) );
			paramsMap.put( FileServletApi.PARAM_UNCOMPRESSED_DATA_SIZE, Integer.toString( uncompressedDataSize ) );
			paramsMap.put( FileServletApi.PARAM_SAVED_WITH_COMPRESSION, Integer.toString( savedWithCompression ) );
			
			return storeFile( file, authorizationKey, FileType.MOUSE_PRINT, paramsMap );
			
		} catch ( final Exception e ) {
			e.printStackTrace();
			System.out.println( "Failed to store file in the Sc2gears Database: " + file.getAbsolutePath() );
		}
		
		return false;
	}
	
	/**
	 * Checks the authorization key from the settings, and if it's OK, stores the "other" file.<br>
	 * The file is stored in a new thread, and a progress dialog is displayed.
	 * @param file           other file to store
	 * @param comment        comment for the file
	 * @param dialogTitleKey text key of the progress dialog
	 * @return true if a valid authorization key is set in the settings and the other file is being stored; false otherwise
	 */
	public static boolean storeOtherFile( final File file, final String comment, final String dialogTitleKey ) {
		final String authorizationKey = GeneralUtils.checkKeyBeforeStoringOrDownloading();
		if ( authorizationKey == null )
			return false;
		
		final ProgressDialog progressDialog = new ProgressDialog( dialogTitleKey, Icons.SERVER_NETWORK, 1 );
		new NormalThread( "Other file storer" ) {
			@Override
			public void run() {
				final Map< String, String > paramsMap = new HashMap< String, String >();
				paramsMap.put( FileServletApi.PARAM_COMMENT, FileServletApi.trimComment( comment ) );
				
				if ( !GeneralUtils.storeFile( file, authorizationKey, FileType.OTHER, paramsMap ) )
					progressDialog.incrementFailed();
				progressDialog.incrementProcessed();
				progressDialog.updateProgressBar();
				progressDialog.taskFinished();
			}
		}.start();
		
		return false;
	}
	
	/**
	 * Stores a file in the Sc2gears Database.<br>
	 * 
	 * General file parameters will be added to the params map (like sha1, file name, last modified, file content).
	 * 
	 * @param file             file to store
	 * @param authorizationKey authorization key to use
	 * @param fileType         file type to be added to the params map, constants are defined in the {@link FileServletApi}
	 * @param paramsMap        params map to pass to {@link HttpPost} (file type specific parameters of the file)
	 * @return true if file was stored successfully; false otherwise
	 */
	private static boolean storeFile( final File file, final String authorizationKey, final FileType fileType, final Map< String, String > paramsMap ) {
		paramsMap.put( FileServletApi.PARAM_PROTOCOL_VERSION , FileServletApi.PROTOCOL_VERSION_1    );
		paramsMap.put( FileServletApi.PARAM_OPERATION        , FileServletApi.OPERATION_STORE       );
		paramsMap.put( FileServletApi.PARAM_AUTHORIZATION_KEY, authorizationKey                     );
		paramsMap.put( FileServletApi.PARAM_SHA1             , calculateFileSha1( file )            );
		paramsMap.put( FileServletApi.PARAM_FILE_NAME        , FileServletApi.trimFileName( file.getName() ) );
		paramsMap.put( FileServletApi.PARAM_LAST_MODIFIED    , Long.toString( file.lastModified() ) );
		paramsMap.put( FileServletApi.PARAM_FILE_TYPE        , fileType.longName                    );
		paramsMap.put( FileServletApi.PARAM_FILE_CONTENT     , Base64.encodeFile( file )            );
		
		for ( int retry = 0; retry < MAX_SC2GEARS_DATABASE_STORE_RETRIES; retry++ ) {
			if ( retry > 0 )
				System.out.println( "Retrying storing file (" + (retry+1) + "): " + file.getAbsolutePath() );
			
			HttpPost httpPost  = null;
			try {
				httpPost = new HttpPost( Consts.URL_SC2GEARS_DATABASE_FILE_SERVLET, paramsMap );
				if ( httpPost.connect() )
					if ( httpPost.doPost() ) {
						final List< String > response = httpPost.getResponseLines();
						if ( response != null )
							if ( Boolean.parseBoolean( response.get( 0 ) ) ) {
								System.out.println( "Successfully stored file in the Sc2gears Database: " + file.getAbsolutePath() );
								return true;
							}
					}
			} catch ( final Exception e ) {
				e.printStackTrace();
			} finally {
				if ( httpPost != null )
					httpPost.close();
			}
			
			System.out.println( "Failed to store file in the Sc2gears Database: " + file.getAbsolutePath() );
		}
		
		return false;
	}
	
	/**
	 * Gets the user quota info from the Sc2gears Database.<br>
	 * @param authorizationKey authorization key to use
	 * @return the user quota info; or <code>null</code> if some error occurs
	 */
	public static String getUserQuotaInfo( final String authorizationKey ) {
		HttpPost httpPost = null;
		try {
			// I use POST so the authorization key will not appear in the URL, because in case of errors the URL will appear in the log
			final Map< String, String > paramsMap = new HashMap< String, String >();
			paramsMap.put( InfoServletApi.PARAM_PROTOCOL_VERSION , InfoServletApi.PROTOCOL_VERSION_1       );
			paramsMap.put( InfoServletApi.PARAM_OPERATION        , InfoServletApi.OPERATION_GET_QUOTA_INFO );
			paramsMap.put( InfoServletApi.PARAM_AUTHORIZATION_KEY, authorizationKey                        );
			
			httpPost = new HttpPost( Consts.URL_SC2GEARS_DATABASE_INFO_SERVLET, paramsMap );
			if ( httpPost.connect() )
				if ( httpPost.doPost() )
					return httpPost.getResponse();
			
		} catch ( final Exception e ) {
			e.printStackTrace();
		} finally {
			if ( httpPost != null )
				httpPost.close();
		}
		return null;
	}
	
}
