/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.shared;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.security.MessageDigest;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Shared utility methods for the updater and Sc2gears.
 * 
 * @author Andras Belicza
 */
public class SharedUtils {
	
	/** Digits used in the hexadecimal representation. */
	public static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	/** This icon is defined here (and not in the <code>Icons</code> class because this class is part of the Updater too. */
	public static final ImageIcon SC2GEARS            = new ImageIcon( SharedUtils.class.getResource( "sc2gears.png" ) );
	/** This icon is defined here (and not in the <code>Icons</code> class because this class is part of the Updater too. */
	public static final ImageIcon APPLICATION_BROWSER = new ImageIcon( SharedUtils.class.getResource( "application-browser.png" ) );
	/** This icon is defined here (and not in the <code>Icons</code> class because this class is part of the Updater too. */
	public static final ImageIcon DOOR_OPEN_IN        = new ImageIcon( SharedUtils.class.getResource( "door-open-in.png" ) );
	/** This icon is defined here (and not in the <code>Icons</code> class because this class is part of the Updater too. */
	public static final ImageIcon CROSS_OCTAGON       = new ImageIcon( SharedUtils.class.getResource( "cross-octagon.png" ) );
	
	/**
	 * No need to instantiate this class.
	 */
	protected SharedUtils() {
	}
	
	/** Operating system name property.             */
	public static final String OS_NAME = System.getProperty( "os.name" );
	/** Operating system name property lower cased. */
	public static final String OS_NAME_LOWERED = OS_NAME.toLowerCase();
	
	/**
	 * Returns if the operating system running the application is windows.
	 * @return true if the operating system running the application is windows
	 */
	public static boolean isWindows() {
		return OS_NAME_LOWERED.indexOf( "win" ) >= 0;
	}
	
	/**
	 * Returns if the operating system running the application is MAC OS or MAC OS-X.
	 * @return true if the operating system running the application is windows
	 */
	/**
	 * Returns if the operating system running the application is MAC OS or MAC OS-X.
	 * @return true if the operating system running the application is MAC OS or MAC OS-X
	 */
	public static boolean isMac() {
		return OS_NAME_LOWERED.indexOf( "mac" ) >= 0;
	}
	
	/**
	 * Returns if the operating system running the application is Unix (linux).
	 * @return true if the operating system running the application is windows
	 */
	public static boolean isUnix() {
		return OS_NAME_LOWERED.indexOf( "nix" ) >= 0 || OS_NAME_LOWERED.indexOf( "nux" ) >= 0;
	}
	
	/**
	 * Opens the web page specified by the URL in the system's default browser.
	 * @param url URL to be opened
	 */
	public static void showURLInBrowser( final String url ) {
		try {
			if ( Desktop.isDesktopSupported() )
				try {
					Desktop.getDesktop().browse( new URI( url ) );
					return;
				} catch ( final Exception e ) {
				}
			
			// Desktop failed, try our own method
			String[] cmdArray = null;
			if ( isWindows() ) {
				cmdArray = new String[] { "rundll32", "url.dll,FileProtocolHandler", url };
			}
			else {
				// Linux
				final String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
				for ( final String browser : browsers )
					if ( Runtime.getRuntime().exec( new String[] { "which", browser } ).waitFor() == 0 ) {
						cmdArray = new String[] { browser, url };
						break;
					}
			}
			
			if ( cmdArray != null )
				Runtime.getRuntime().exec( cmdArray );
		} catch ( final Exception e ) {
		}
	}
	
	/**
	 * Centers a window on its screen.
	 * @param window window to be centered
	 */
	public static void centerWindow( final Window window ) {
		window.setLocationRelativeTo( null );
	}
	
	/**
	 * Creates a label which operates as a link.
	 * The link will only be opened if the label is not disabled.
	 * The target URL is set as the tool of the label.
	 * @param text      text of the label (link)
	 * @param targetUrl URL to be opened when the user clicks on the label
	 * @return the label which operates as a link
	 */
	public static JLabel createLinkLabel( final String text, final String targetUrl ) {
		final JLabel label = createLinkStyledLabel( text );
		label.setIcon( APPLICATION_BROWSER );
		label.setToolTipText( targetUrl );
		
		label.addMouseListener( new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				showURLInBrowser( targetUrl );
			};
		} );
		
		return label;
	}
	
	/**
	 * Creates and returns an animated logo label.
	 * <p>The logo label is also set to open the home page if clicked.</p>
	 * @return an animated logo label
	 */
	public static JLabel createAnimatedLogoLabel() {
		final JLabel logoLabel = createLinkLabel( null, SharedConsts.URL_HOME_PAGE );
		
		logoLabel.setBorder( BorderFactory.createRaisedBevelBorder() );
		logoLabel.setIcon( new ImageIcon( SharedUtils.class.getResource( "sc2gears_logo_anim_fire.gif" ) ) );
		
		return logoLabel;
	}
	
	/**
	 * Creates a label which looks like a link and has a hand mouse cursor.
	 * @param text text of the label (link)
	 * @return a label which looks like a link
	 */
	public static JLabel createLinkStyledLabel( final String text ) {
		final JLabel label = new JLabel( createHtmlLink( text ) );
		label.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		return label;
	}
	
	/**
	 * Creates an HTML link with the specified text. 
	 * @param text text to be displayed for the link
	 * @return an HTML link
	 */
	public static String createHtmlLink( final String text ) {
		return text == null ? null : "<html><a href='#'>" + text.replace( " ", "&nbsp;" ).replace( "<", "&lt;" ).replace( ">", "&gt;" ) + "</a></html>";
	}
	
	/**
	 * Calculates the SHA-256 digest of a file.
	 * @param file file whose SHA-256 to be calculated
	 * @return the calculated SHA-256 digest of the file
	 */
	public static String calculateFileSha256( final File file ) {
		return calculateFileDigest( file, "SHA-256" );
	}
	
	/**
	 * Calculates the MD5 digest of a file.
	 * @param file file whose MD5 digest to be calculated
	 * @return the calculated MD5 digest of the file
	 */
	public static String calculateFileMd5( final File file ) {
		return calculateFileDigest( file, "MD5" );
	}
	
	/**
	 * Calculates the SHA-1 digest of a file.
	 * @param file file whose SHA-1 digest to be calculated
	 * @return the calculated SHA-1 digest of the file
	 */
	public static String calculateFileSha1( final File file ) {
		return calculateFileDigest( file, "SHA-1" );
	}
	
	/**
	 * Calculates the digest of a file.
	 * @param file      file whose digest to be calculated
	 * @param algorithm algorithm to be used to calculate the digest
	 * @return the calculated digest of the file
	 */
	public static String calculateFileDigest( final File file, final String algorithm ) {
		try ( final FileInputStream input = new FileInputStream( file ) ) {
			final MessageDigest md = MessageDigest.getInstance( algorithm );
			
			final byte[] buffer = new byte[ 16*1024 ];
			
			int bytesRead;
			while ( ( bytesRead = input.read( buffer ) ) > 0 )
				md.update( buffer, 0, bytesRead );
			
			return convertToHexString( md.digest() );
		}
		catch ( final Exception e ) {
			return "";
		}
	}
	
	/**
	 * Converts the specified data to hex string.
	 * @param data data to be converted
	 * @return the specified data converted to hex string
	 */
	public static String convertToHexString( final byte[] data ) {
		return convertToHexString( data, 0, data.length );
	}
	
	/**
	 * Converts the specified data to hex string.
	 * @param data   data to be converted
	 * @param offset offset of first byte to convert 
	 * @param length number of bytes to convert
	 * @return the specified data converted to hex string
	 */
	public static String convertToHexString( final byte[] data, int offset, int length ) {
		final StringBuilder hexBuilder = new StringBuilder( length << 1 );
		
		length += offset;
		for ( ; offset < length; offset++ ) {
			final byte b = data[ offset ];
			hexBuilder.append( HEX_DIGITS[ ( b & 0xff ) >> 4 ] ).append( HEX_DIGITS[ b & 0x0f ] );
		}
		
		return hexBuilder.toString();
	}
	
	/**
	 * Returns the OS dependent command to start Sc2gears. 
	 * @return the OS dependent command to start Sc2gears
	 */
	public static String getSc2gearsStartCommand() {
		return isWindows() ? SharedConsts.SC2GEARS_START_COMMAND_WIN : isMac() ? SharedConsts.SC2GEARS_START_COMMAND_OS_X : isUnix() ? SharedConsts.SC2GEARS_START_COMMAND_UNIX : "";
	}
	
}
