/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.services;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog.SettingsTab;
import hu.belicza.andras.sc2gears.ui.dialogs.ProgressDialog;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.ControlledThread;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.NormalThread;
import hu.belicza.andras.smpd.SmpdUtil;
import hu.belicza.andras.smpd.SmpdUtil.SmpdVer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;

/**
 * Mouse Print recorder.
 * 
 * http://www.teamliquid.net/forum/viewmessage.php?topic_id=180852
 * 
 * <p>Mouse Print Data file specification can be found <a href="http://sites.google.com/site/sc2gears/features/mouse-print-recorder">here</a>.</p>
 * 
 * ------------
 * Notes to me:
 *     -smallest mouse sampling time (1 ms) results in about 100 samples/sec (delta time will be 10 ms...)
 *     -that means maximum 100 samples*3 bytes=300 bytes/sec=18KB/min => maximum 200 KB / avg. game 
 * 
 * @author Andras Belicza
 */
public class MousePrintRecorder {
	
	/**
	 * Data compressions that can be used to compress samples.
	 * @author Andras Belicza
	 */
	public enum DataCompression {
		/** No compression.      */
		NO_COMPRESSION( "miscSettings.binaryDataCompressionAlgorithm.noCompression" ),
		/** Deflate compression. */
		DEFLATE       ( "miscSettings.binaryDataCompressionAlgorithm.deflate"       ),
		/** BZip2 compression.   */
		BZIP2         ( "miscSettings.binaryDataCompressionAlgorithm.bzip2"         );
		
		/** Cache of the string value. */
		public final String stringValue;
		
		/**
		 * Creates a new DataCompression.
		 * @param textKey key of the text representation
		 */
		private DataCompression( final String textKey ) {
			stringValue = Language.getText( textKey );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * What to save for the mouse print.
	 * @author Andras Belicza
	 */
	public static enum WhatToSave {
		BINARY_DATA_AND_IMAGE( "miscSettings.mousePrintWhatToSave.binaryAndImage", true , true  ),
		BINARY_DATA_ONLY     ( "miscSettings.mousePrintWhatToSave.binaryOnly"    , true , false ),
		IMAGE_ONLY           ( "miscSettings.mousePrintWhatToSave.imageOnly"     , false, true  );
		
		/** Cache of the string value.            */
		public final String  stringValue;
		/** Tells if binary data has to be saved. */
		public final boolean saveBinaryData;
		/** Tells if image has to be saved.       */
		public final boolean saveImage;
		
		/**
		 * Creates a new WhatToSave.
		 * @param textKey key of the text representation
		 */
		private WhatToSave( final String textKey, final boolean saveBinaryData, final boolean saveImage ) {
			stringValue         = Language.getText( textKey );
			this.saveBinaryData = saveBinaryData;
			this.saveImage      = saveImage;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
		
	}
	
	/** Output formats of the mouse print image. */
	public static final String[] OUTPUT_FORMATS = { "PNG", "JPG", "GIF" };
	
	/**
	 * The recorder thread.
	 * @author Andras Belicza
	 */
	private static class Recorder extends ControlledThread {
		
		/** Recorder refres rates. */
		public static final Integer[] RECORDER_REFRESH_RATES = new Integer[] { 1, 2, 4, 8, 16 };
		
		/** Recorder frame refresh period time in ms. */
		private volatile int recorderFrameRefreshPeriod = 200;
		
		/** Data compression used to save with. */
		private DataCompression  savedWithCompression;
		/** Tells what to save. */
		private final WhatToSave whatToSave;
		
		/** Name of the application that saved the SMPD file. */
		private String savedWithApp;
		
		/** Screen resolution in dots/inch.        */
		private final int           screenResolution;
		/** Background color.                      */
		private final Color         backgroundColor      = GeneralUtils.getColorSetting( null, Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_BACKGROUND_COLOR );
		/** Color.                                 */
		private final Color         color                = GeneralUtils.getColorSetting( null, Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_COLOR            );
		/** Shadow color drawn around blobs.       */
		private final Color         shadowColor          = new Color( backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 128 );
		/** The buffer to record the mouse print.  */
		private final BufferedImage buffer;
		/** Start time of the recording.           */
		private Date                startTime;
		/** End time of the recording.             */
		private volatile Date       endTime;
		/** Sampling time in ms.                   */
		private final long          samplingTime;
		/** Tells if anti-aliasing should be used. */
		private final boolean       useAntialiasing      = Settings.getBoolean( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_USE_ANTIALIASING );
		/** Pour ink after mouse idle time ms.     */
		private final int           pourInkMouseIdleTime = Settings.getInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_POUR_INK_IDLE_TIME ) * 1000;
		/** Ink flow rate in pixel/sec.            */
		private final int           inkFlowRate          = Settings.getInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_IDLE_INK_FLOW_RATE );
		/** Mouse warm-up time ms.                 */
		private final int           mouseWarmupTime      = Settings.getInt( Settings.KEY_SETTINGS_MISC_MOUSE_WARMUP_TIME ) * 1000;
		
		/** Number of samples.                     */
		private volatile int        samplesCount;
		/** Total mouse distance in pixels.        */
		private volatile double     totalMouseDistance;
		
		/** The mouse print binary data will be stored here if it has to be saved. */
		private final ByteArrayOutputStream dataStream;
		
		/** Tells if the recording has been saved. */
		private volatile Boolean saved;
		/** Tells if the recording was loaded.     */
		private volatile boolean loaded;
		
		/** File name of the mouse print (if saved or loaded). */
		private String sourceFileName;
		
		/**
		 * Creates a new Recorder.
		 */
		public Recorder() {
			super( "Mouse Print Recorder" );
			
			WhatToSave whatToSave_;
			try {
				whatToSave_ = WhatToSave.values()[ Settings.getInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_WHAT_TO_SAVE ) ];
			} catch ( final IllegalArgumentException iae ) {
				whatToSave_ = WhatToSave.values()[ Settings.getDefaultInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_WHAT_TO_SAVE ) ];
			}
			whatToSave = whatToSave_;
			
			samplingTime = Settings.getInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_SAMPLING_TIME );
			
			final Toolkit toolkit = Toolkit.getDefaultToolkit();
			screenResolution = toolkit.getScreenResolution();
			final Dimension screenSize = toolkit.getScreenSize();
			buffer = new BufferedImage( screenSize.width, screenSize.height, BufferedImage.TYPE_INT_RGB );
			dataStream = whatToSave.saveBinaryData ? new ByteArrayOutputStream( 100000 ) : null;
			
			setRecorderFrameRefreshRate( RECORDER_REFRESH_RATES[ Settings.getInt( Settings.KEY_MOUSE_PRINT_REFRESH_RATE ) ] );
		}
		
		/**
		 * Creates a new Recorder.<br>
		 * Data will be loaded from the specified mouse print data file.
		 * @param smpdFile SMPD file to load
		 */
		public Recorder( final File smpdFile ) {
			super( "Mouse Print Recorder" );
			
			WhatToSave whatToSave_;
			try {
				whatToSave_ = WhatToSave.values()[ Settings.getInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_WHAT_TO_SAVE ) ];
			} catch ( final IllegalArgumentException iae ) {
				whatToSave_ = WhatToSave.values()[ Settings.getDefaultInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_WHAT_TO_SAVE ) ];
			}
			whatToSave = whatToSave_;
			
			Graphics2D      g2        = null;
			try ( final DataInputStream dataInput = new DataInputStream( new FileInputStream( smpdFile ) ) ) {
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
				
				startTime              = new Date( dataInput.readLong() );
				endTime                = new Date( dataInput.readLong() );
				final int screenWidth  = dataInput.readInt();
				final int screenHeight = dataInput.readInt();
				buffer                 = new BufferedImage( screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB );
				screenResolution       = dataInput.readInt();
				samplingTime           = dataInput.readInt();
				samplesCount           = dataInput.readInt();
				dataStream             = new ByteArrayOutputStream( dataInput.readInt() ); // Uncompressed data size
				savedWithCompression   = DataCompression.values()[ dataInput.read() ];
				
				int extraHeaderBytes = 0;  // Extra header bytes over the SmpdVer.minHeaderSize
				
				// Version 1.1 additions
				if ( smpdVer.compareTo( SmpdVer.V11 ) <= 0 ) {
					final int appNameLength = dataInput.read();
					extraHeaderBytes += appNameLength;
					final byte[] buffer = new byte[ appNameLength ];
					dataInput.read( buffer );
					savedWithApp = new String( buffer, Consts.UTF8 );
				}
				
				if ( headerLength != smpdVer.minHeaderSize + extraHeaderBytes ) // Skip custom header bytes
					dataInput.skipBytes( headerLength - ( smpdVer.minHeaderSize + extraHeaderBytes ) );
				
				// Samples
				InputStream inputStream = null;
				switch ( savedWithCompression ) {
				case NO_COMPRESSION :
					inputStream = dataInput;
					break;
				case DEFLATE :
					inputStream = new InflaterInputStream( dataInput );
					break;
				case BZIP2 :
					inputStream = new CBZip2InputStream( dataInput );
					break;
				}
				
				g2 = createAndInitGraphics();
				
				// available() cannot be used here:
				// InflaterInputStream might return >0 even if there are no more, CBZip2InputStream might return 0 even if there are more...
				final int samplesCount_final = samplesCount; // Handling the samples increments samplesCount, so we need to store/restore its value!
				int samplesCount_ = samplesCount;
				Point location = null, lastLocation = null;
				int elapsedTime = 0;
				while ( samplesCount_-- > 0 ) {
					// InflaterInputStream might return available()>0 even if there are no more...
					final int dt = SmpdUtil.readEncodedValue( inputStream );
					if ( dt == Integer.MAX_VALUE )
						break;
					elapsedTime += dt;
					final int dx = SmpdUtil.readEncodedValue( inputStream );
					final int dy = SmpdUtil.readEncodedValue( inputStream );
					if ( location == null ) {
						location = new Point( dx, dy );
						handleFirstSample( dt, location );
						lastLocation = new Point( location );
					}
					else {
						location.x += dx;
						location.y += dy;
						handleSample( elapsedTime, dt, location, lastLocation, g2 );
						lastLocation.x = location.x;
						lastLocation.y = location.y;
					}
				}
				samplesCount = samplesCount_final;
				
			} catch ( final Exception e ) {
				e.printStackTrace();
				throw new RuntimeException();
			} finally {
				if ( g2 != null )
					g2.dispose();
			}
			
			requestToCancel(); // The "requestedToCancel" state is used in many places...
			
			sourceFileName = smpdFile.getAbsolutePath();
			setRecorderFrameRefreshRate( RECORDER_REFRESH_RATES[ Settings.getInt( Settings.KEY_MOUSE_PRINT_REFRESH_RATE ) ] );
			loaded = true;
		}
		
		/**
		 * This method contains the cycle which records mouse movements.
		 */
		@Override
		public void run() {
			final Graphics2D g2 = createAndInitGraphics();
			
			startTime = new Date();
			final long startTimeLong = startTime.getTime();
			RecorderFrame recorderFrame;
			if ( ( recorderFrame = MousePrintRecorder.recorderFrame ) != null )
				recorderFrame.updateMousePrint();
			
			Point lastLocation = null, location;
			long lastTime = startTimeLong, time;
			long nextRecorderFrameRefreshTime = lastTime + recorderFrameRefreshPeriod;
			boolean mousePrintChanged = false;
			while ( !requestedToCancel ) {
				location = getMouseLocation();
				time     = System.currentTimeMillis();
				// TODO consider not pouring ink when workstation is locked
				if ( location != null ) {
					if ( lastLocation == null ) {
						// This is the first point/sample
						handleFirstSample( (int) ( time - lastTime ), location );
						lastLocation      = location;
						lastTime          = time;
						mousePrintChanged = true;
					}
					else {
						if ( !lastLocation.equals( location ) ) {
							handleSample( (int) ( time - startTimeLong ), (int) ( time - lastTime ), location, lastLocation, g2 );
							lastLocation      = location;
							lastTime          = time;
							mousePrintChanged = true;
						}
					}
				}
				if ( mousePrintChanged && time > nextRecorderFrameRefreshTime && ( recorderFrame = MousePrintRecorder.recorderFrame ) != null ) {
					nextRecorderFrameRefreshTime = time + recorderFrameRefreshPeriod;
					mousePrintChanged            = false;
					recorderFrame.updateMousePrint();
				}
				try {
					Thread.sleep( samplingTime );
				} catch ( final InterruptedException ie ) {
					ie.printStackTrace();
				}
			}
			
			// The last position of the mouse will not be outputted, but that's not a problem...
			// Maybe it's even good: if at the end the player stops playing/moving his/her mouse, it's unnecessary to have a large blob in the image  
			endTime = new Date();
			recorderFrame = MousePrintRecorder.recorderFrame;
			if ( recorderFrame != null )
				recorderFrame.updateMousePrint();
			g2.dispose();
		}
		
		/**
		 * Creates and initalizes the graphics of the buffer.
		 * @return the initialized graphics of the buffer
		 */
		private Graphics2D createAndInitGraphics() {
			final Graphics2D g2 = buffer.createGraphics();
			
			g2.setColor( backgroundColor );
			g2.fillRect( 0, 0, buffer.getWidth(), buffer.getHeight() );
			g2.setColor( color );
			
			if ( useAntialiasing )
				g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			
			return g2;
		}
		
		/**
		 * Returns the current location of the mouse cursor.<br>
		 * On Windows if the workstation is locked (the login screen is displayed), the mouse location is not available
		 * and <code>null</code> is returned. Also if a mouse is not present, <code>null</code> is returned.
		 * @return the current location of the mouse cursor.
		 */
		private Point getMouseLocation() {
			final PointerInfo pointerInfo = MouseInfo.getPointerInfo();
			if ( pointerInfo == null ) // Mouse not present?
				return null;
			
			final Point location = pointerInfo.getLocation();
			
			// Some big random number is returned if the workstation is locked on Windows
			if ( location.x < -20000 || location.x > 20000 || location.y < -20000 || location.y > 20000 )
				return null;
			
			return location;
		}
		
		/**
		 * Handles the first sample.
		 * @param dt       delta time since the recording start time
		 * @param location location of the mouse
		 */
		private void handleFirstSample( final int dt, final Point location ) {
			// Simply write out the sample (if binary data has to be collected)
			if ( dataStream != null )
				writeSample( dt, location.x, location.y );
		}
		
		/**
		 * Handle a (non-first) sample.
		 * @param elapsedTime  elapsed time since the start of recording
		 * @param dt           delta time since the recording start time
		 * @param location     location of the mouse
		 * @param lastLocation last location of the mouse (location in the previous sample)
		 * @param g2           reference to the graphics context in which to paint
		 */
		private void handleSample( final int elapsedTime, final int dt, final Point location, final Point lastLocation, final Graphics2D g2 ) {
			final int dx = location.x - lastLocation.x;
			final int dy = location.y - lastLocation.y;
			
			if ( dataStream != null ) 
				writeSample( dt, dx, dy );
			
			// Check if the movement was horizontal or vertical
			if ( dx == 0 )
				totalMouseDistance += dy < 0 ? -dy : dy;
			else if ( dy == 0 )
				totalMouseDistance += dx < 0 ? -dx : dx;
			else
				totalMouseDistance += Math.sqrt( dx * dx + dy * dy );
			
			// If mouse is idle, "pour ink" on the buffer based on dt
			if ( dt > pourInkMouseIdleTime && elapsedTime > mouseWarmupTime ) {
				// Area is the number of pixels to pour: T = flow_rate * dt = r*r*PI = d*d*PI/4
				final int d = (int) Math.sqrt( 0.004 / Math.PI * ( dt - pourInkMouseIdleTime ) * inkFlowRate ); // flow rate is in pixel/sec, dt is in ms!
				g2.setColor( shadowColor );
				g2.fillOval( lastLocation.x - d, lastLocation.y - d, d << 1, d << 1 );
				g2.setColor( color );
				g2.fillOval( lastLocation.x - ( d >> 1 ), lastLocation.y - ( d >> 1 ), d, d );
			}
			
			// Draw movement
			g2.drawLine( lastLocation.x, lastLocation.y, location.x, location.y );
		}
		
		/**
		 * Writes out a sample.
		 * @param dt delta time since the last entry
		 * @param dx delta x coordinate since the last entry
		 * @param dy delta x coordinate since the last entry
		 */
		private void writeSample( final int dt, final int dx, final int dy ) {
			try {
				SmpdUtil.writeEncodedValue( dt, dataStream );
				SmpdUtil.writeEncodedValue( dx, dataStream );
				SmpdUtil.writeEncodedValue( dy, dataStream );
				samplesCount++;
			} catch ( final IOException ie ) {
				// Never to happen because we write into a ByteArrayOutputStream
				// If we end up here it only might be internal error: out of memory or something
				throw new RuntimeException( ie );
			}
		}
		
		/**
		 * Saves the mouse print.
		 */
		public synchronized void save() {
			final File targetFolder = new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_MOUSE_PRINT_OUTPUT ) );
			if ( !targetFolder.exists() )
				if ( !targetFolder.mkdirs() ) {
					System.out.println( "Failed to create mouse print output folder: " + targetFolder.getAbsolutePath() );
					return;
				}
			
			final String fileName = new SimpleDateFormat( "yy-MM-dd HH-mm-ss" ).format( new Date() );
			
			if ( whatToSave.saveBinaryData ) {
				try {
					savedWithCompression = DataCompression.values()[ Settings.getInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_DATA_COMPRESSION ) ];
				} catch ( final IllegalArgumentException iae ) {
					savedWithCompression = DataCompression.values()[ Settings.getDefaultInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_DATA_COMPRESSION ) ];
				}
				final File smpdFile = new File( targetFolder, fileName + "." + SmpdUtil.SMPD_FILE_EXTENSION );
				try ( final DataOutputStream dataOutput = new DataOutputStream( new FileOutputStream( smpdFile ) ) ) {
					savedWithApp = Consts.APPLICATION_NAME + "/" + Consts.APPLICATION_VERSION
							+ " (" + System.getProperty( "os.name" ) + " " + System.getProperty( "os.version" ) + "; " + System.getProperty( "os.arch" ) + ")"
							+ " Java/" + System.getProperty( "java.version" );
					// AppName bytes must be less than 256. If greater, cut of last characters.
					// (Must trim the string instead of the byte array else it might result in an invalid UTF-8 byte array.)
					byte[] appNameBytes;
					while ( ( appNameBytes = savedWithApp.getBytes( Consts.UTF8 ) ).length > 255 )
						savedWithApp = savedWithApp.substring( 0, savedWithApp.length() - 1 ); // Cut off last char
					final int extraBytes = appNameBytes.length; // Extra bytes over SmpdVer.minHeaderSize
					dataOutput.write     ( SmpdUtil.SMPD_MAGIC );
					dataOutput.writeShort( SmpdVer.V11.binaryValue );
					dataOutput.writeInt  ( SmpdVer.V11.minHeaderSize + extraBytes );
					// Header
					dataOutput.writeLong ( startTime.getTime() );
					dataOutput.writeLong ( endTime  .getTime() );
					dataOutput.writeInt  ( buffer.getWidth () );
					dataOutput.writeInt  ( buffer.getHeight() );
					dataOutput.writeInt  ( screenResolution );
					dataOutput.writeInt  ( (int) samplingTime );
					dataOutput.writeInt  ( samplesCount );
					dataOutput.writeInt  ( dataStream.size() );
					dataOutput.write     ( savedWithCompression.ordinal() );
					// Version 1.1 additions
					// Save application name that created (creates) the SMPD file
					// 1 byte length, followed by the UTF-8 bytes of the app name; ADD this to the header size (above!)
					dataOutput.write( appNameBytes.length );
					dataOutput.write( appNameBytes );
					// Samples
					switch ( savedWithCompression ) {
					case NO_COMPRESSION :
						dataStream.writeTo( dataOutput );
						break;
					case DEFLATE :
						final DeflaterOutputStream deflaterOutput = new DeflaterOutputStream( dataOutput );
						dataStream.writeTo( deflaterOutput );
						deflaterOutput.finish();
						break;
					case BZIP2 :
						final CBZip2OutputStream bzip2Output = new CBZip2OutputStream( dataOutput );
						dataStream.writeTo( bzip2Output );
						bzip2Output.finish();
						break;
					}
					dataOutput.flush();
					System.out.println( "Successfully saved mouse print binary data to: " + smpdFile.getAbsolutePath() );
				} catch ( final IOException ie ) {
					saved = false;
					System.out.println( "Failed to save mouse print image: " + smpdFile.getAbsolutePath() );
					ie.printStackTrace();
				}
				sourceFileName = smpdFile.getAbsolutePath();
				saved = true;
			}
			
			if ( whatToSave.saveImage ) {
				final String imageFormat = OUTPUT_FORMATS[ Settings.getInt( Settings.KEY_SETTINGS_MISC_MOUSE_PRINT_IMAGE_OUTPUT_FORMAT ) ];
				final File   imageFile   = new File( targetFolder, fileName + "." + imageFormat.toLowerCase() );
				try {
					ImageIO.write( buffer, imageFormat, imageFile );
					System.out.println( "Successfully saved mouse print image to: " + imageFile.getAbsolutePath() );
				} catch ( final IOException ie ) {
					if ( saved == null ) // If binary data has to be saved, it's more important...
						saved = false;
					System.out.println( "Failed to save mouse print image: " + imageFile.getAbsolutePath() );
					ie.printStackTrace();
				}
				if ( saved == null ) // If binary data has to be saved, it's more important...
					saved = true;
			}
			
			updateRecorderFrame();
		}
		
		/**
		 * Sets the recorder frame refresh rate.
		 * @param fps recorder frame refresh rate to be set
		 */
		public void setRecorderFrameRefreshRate( final int fps ) {
			recorderFrameRefreshPeriod = 1000 / fps;
		}
		
	}
	
	/**
	 * Frame of the recorder (UI).
	 * @author Andras Belicza
	 */
	@SuppressWarnings("serial")
	private static class RecorderFrame extends JFrame {
		
		/** Max zoom value. */
		private static final int MAX_ZOOM = 3;
		
		/** Label dictionary for the zoom slider. */
		private static final Dictionary< Integer, JComponent > LABEL_DICTIONARY = new Hashtable< Integer, JComponent >();
		static {
			for ( int i = 0; i <= MAX_ZOOM; i++ )
				LABEL_DICTIONARY.put( i, new JLabel( i == 0 ?  "1x" : "1/" + ( 1 << i ) + "x" ) );
		}
		
		/** Reference to the start/stop recording button.          */
		private final JButton startStopRecordingButton = new JButton();
		/** Reference to the save mouse print button.              */
		private final JButton saveMousePrintButton     = new JButton( Icons.DISK );
		/** Reference to the load mouse print button.              */
		private final JButton loadMousePrintButton     = new JButton( Icons.FOLDER_OPEN );
		/** Reference to the store mouse print button.             */
		private final JButton storeMousePrintButton    = new JButton( Icons.SERVER_NETWORK );
		/** Reference to the canvas that displays the mouse print. */
		private final JPanel  mousePrintCanvas;
		/** Status label.                                          */
		private final JLabel  statusLabel              = new JLabel();
		
		/** Reference to the info table.                           */
		private final JTable  infoTable                = GuiUtils.createNonEditableTable();
		
		/**
		 * Creates a new RecorderFrame.
		 */
		public RecorderFrame() {
			super( Language.getText( "mousePrintRecorder.title", Consts.APPLICATION_NAME ) );
			setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
			addWindowListener( new WindowAdapter() {
				@Override
				public void windowClosing( final WindowEvent event ) {
					synchronized ( MousePrintRecorder.class ) {
						recorderFrame.dispose();
						recorderFrame = null;
					}
				}
			} );
			
			final Box northBox = Box.createVerticalBox();
			Box controlBox = Box.createHorizontalBox();
			startStopRecordingButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					if ( recorder == null || recorder.isCancelRequested() )
						startRecording();
					else
						stopRecording();
				}
			} );
			controlBox.add( startStopRecordingButton );
			controlBox.add( Box.createHorizontalStrut( 5 ) );
			GuiUtils.updateButtonText( saveMousePrintButton, "mousePrintRecorder.saveMousePrintButton" );
			saveMousePrintButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					recorder.save();
				}
			} );
			controlBox.add( saveMousePrintButton );
			controlBox.add( Box.createHorizontalStrut( 5 ) );
			GuiUtils.updateButtonText( storeMousePrintButton, "mousePrintRecorder.storeMousePrintButton" );
			storeMousePrintButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					storeMousePrints( new File[] { new File( recorder.sourceFileName ) } );
				}
			} );
			controlBox.add( storeMousePrintButton );
			controlBox.add( Box.createHorizontalStrut( 5 ) );
			GuiUtils.updateButtonText( loadMousePrintButton, "mousePrintRecorder.loadMousePrintButton" );
			loadMousePrintButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					Recorder recorder  = MousePrintRecorder.recorder;
					boolean  canLoad = recorder == null || recorder.isCancelRequested();
					if ( canLoad ) {
						final JFileChooser fileChooser = new JFileChooser( Settings.getString( Settings.KEY_SETTINGS_FOLDER_MOUSE_PRINT_OUTPUT ) );
						fileChooser.setDialogTitle( Language.getText( "mousePrintRecorder.loadMousePrintTitle" ) );
						fileChooser.setFileFilter( GuiUtils.MOUSE_PRINT_DATA_FILE_FILTER );
						fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
						if ( fileChooser.showOpenDialog( RecorderFrame.this ) == JFileChooser.APPROVE_OPTION ) {
							final File smpdFile = fileChooser.getSelectedFile();
							// Check the recorder again...
							boolean failedToLoad = false;
							synchronized ( MousePrintRecorder.class ) {
								recorder = MousePrintRecorder.recorder;
								if ( recorder == null || recorder.isCancelRequested() ) {
									try {
										MousePrintRecorder.recorder = new Recorder( smpdFile );
									} catch ( final Exception e ) {
										failedToLoad = true;
									}
								}
							}
							// The error message is moved out of the synchronized block...
							if ( failedToLoad )
								GuiUtils.showErrorDialog( Language.getText( "mousePrintRecorder.failedToLoadMousePrint" ), RecorderFrame.this );
							else
								updateRecorderFrame();
						}
					}
				}
			} );
			controlBox.add( loadMousePrintButton );
			northBox.add( GuiUtils.wrapInPanel( controlBox ) );
			controlBox = Box.createHorizontalBox();
			final JButton selectMousePrintsToStoreButton = new JButton( Icons.SERVER_NETWORK );
			GuiUtils.updateButtonText( selectMousePrintsToStoreButton, "mousePrintRecorder.selectMousePrintsToStoreButton" );
			selectMousePrintsToStoreButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					final JFileChooser fileChooser = new JFileChooser( Settings.getString( Settings.KEY_SETTINGS_FOLDER_MOUSE_PRINT_OUTPUT ) );
					fileChooser.setDialogTitle( Language.getText( "mousePrintRecorder.selectMousePrintsToStore" ) );
					fileChooser.setMultiSelectionEnabled( true );
					fileChooser.setFileFilter( GuiUtils.MOUSE_PRINT_DATA_FILE_FILTER );
					fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
					if ( fileChooser.showOpenDialog( recorderFrame ) == JFileChooser.APPROVE_OPTION )
						storeMousePrints( fileChooser.getSelectedFiles() );
				}
			} );
			controlBox.add( selectMousePrintsToStoreButton );
			controlBox.add( Box.createHorizontalStrut( 15 ) );
			controlBox.add( MiscSettingsDialog.createLinkLabelToSettings( SettingsTab.MOUSE_PRINT, this ) );
			controlBox.add( Box.createHorizontalStrut( 15 ) );
			controlBox.add( new JLabel( Language.getText( "mousePrintRecorder.refreshRate" ) ) );
			final JComboBox< Integer > refreshRateComboBox = GuiUtils.createComboBox( Recorder.RECORDER_REFRESH_RATES, Settings.KEY_MOUSE_PRINT_REFRESH_RATE );
			refreshRateComboBox.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					final Recorder recorder = MousePrintRecorder.recorder;
					if ( recorder != null )
						recorder.setRecorderFrameRefreshRate( (Integer) refreshRateComboBox.getSelectedItem() );
				}
			} );
			controlBox.add( refreshRateComboBox );
			controlBox.add( new JLabel( Language.getText( "mousePrintRecorder.fps" ) ) );
			northBox.add( GuiUtils.wrapInPanel( controlBox ) );
			updateControls();
			GuiUtils.changeFontToBold( statusLabel );
			northBox.add( GuiUtils.wrapInPanel( statusLabel ) );
			getContentPane().add( GuiUtils.wrapInPanel( northBox ), BorderLayout.NORTH );
			
			final JTabbedPane tabbedPane = new JTabbedPane();
			final Box imageControlBox = Box.createHorizontalBox();
			final JPanel imagePanel = new JPanel( new BorderLayout() );
			imageControlBox.add( new JLabel( Language.getText( "mousePrintRecorder.tab.image.zoom" ) ) );
			final JSlider zoomSlider = GuiUtils.createSlider( Settings.KEY_MOUSE_PRINT_IMAGE_ZOOM, 0, MAX_ZOOM );
			zoomSlider.setPaintLabels( true );
			zoomSlider.setPaintTicks( true );
			zoomSlider.setSnapToTicks( true );
			zoomSlider.setMajorTickSpacing( 1 );
			zoomSlider.setLabelTable( LABEL_DICTIONARY );
			zoomSlider.setPreferredSize( new Dimension( 150, zoomSlider.getPreferredSize().height ) );
			zoomSlider.addChangeListener( new ChangeListener() {
				@Override
				public void stateChanged( final ChangeEvent event ) {
					if ( zoomSlider.getValueIsAdjusting() )
						return;
					mousePrintCanvas.invalidate();
					getContentPane().validate();
					getContentPane().repaint();
				}
			} );
			imageControlBox.add( zoomSlider );
			imagePanel.add( GuiUtils.wrapInPanel( imageControlBox ), BorderLayout.NORTH );
			mousePrintCanvas = new JPanel( new BorderLayout() ) {
				/**
				 * I override paint() instead of paintComponent() because I want control over painting optional children.
				 */
				@Override
				public void paint( final Graphics graphics ) {
					final Recorder recorder = MousePrintRecorder.recorder;
					
					if ( recorder == null )
						super.paint( graphics );
					else {
						final Graphics2D g2 = (Graphics2D) graphics;
						g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
						
						final int width   = recorder.buffer.getWidth();
						final int height  = recorder.buffer.getHeight();
						final int zoom    = zoomSlider.getValue();
						final int zwidth  = width  >> zoom;
						final int zheight = height >> zoom;
						// To display the mouse print in the middle of the frame
						int shiftX = 0;
						int shiftY = 0;
						if ( zwidth < getWidth() || zheight < getHeight() ) {
							g2.setColor( getBackground() );
							g2.fillRect( 0, 0, getWidth(), getHeight() );
							shiftX = ( getWidth () - zwidth  ) >> 1;
							shiftY = ( getHeight() - zheight ) >> 1;
						}
						g2.drawImage( recorder.buffer, shiftX, shiftY, shiftX + zwidth, shiftY + zheight, 0, 0, width, height, null );
					}
				}
				@Override
				public Dimension getPreferredSize() {
					final Recorder recorder = MousePrintRecorder.recorder;
					final int zoom = zoomSlider.getValue();
					return recorder == null ? super.getPreferredSize() : new Dimension( recorder.buffer.getWidth() >> zoom, recorder.buffer.getHeight() >> zoom );
				}
			};
			mousePrintCanvas.add( new JLabel( Language.getText( "mousePrintRecorder.noDisplayableMousePrint" ), JLabel.CENTER ) );
			GuiUtils.makeComponentDragScrollable( mousePrintCanvas );
			imagePanel.add( new JScrollPane( mousePrintCanvas ), BorderLayout.CENTER );
			infoTable.getTableHeader().setReorderingAllowed( false );
			infoTable.setShowVerticalLines( true );
			GuiUtils.addNewTab( Language.getText( "mousePrintRecorder.tab.image.title" ), Icons.IMAGE, false, tabbedPane, imagePanel, null );
			( (DefaultTableModel) infoTable.getModel() ).setDataVector(
				new Object[][] {
					{ Language.getText( "mousePrintRecorder.tab.info.fileName"             ), null },
					{ Language.getText( "mousePrintRecorder.tab.info.fileSize"             ), null },
					{ Language.getText( "mousePrintRecorder.tab.info.savedWithApp"         ), null },
					{ Language.getText( "mousePrintRecorder.tab.info.recordingLength"      ), null },
					{ Language.getText( "mousePrintRecorder.tab.info.recordingStartTime"   ), null },
					{ Language.getText( "mousePrintRecorder.tab.info.recordingEndTime"     ), null },
					{ Language.getText( "mousePrintRecorder.tab.info.imageWidth"           ), null },
					{ Language.getText( "mousePrintRecorder.tab.info.imageHeight"          ), null },
					{ Language.getText( "mousePrintRecorder.tab.info.imageResolution"      ), null },
					{ null                                                                  , null },
					{ Language.getText( "mousePrintRecorder.tab.info.samplingTime"         ), null },
					{ Language.getText( "mousePrintRecorder.tab.info.totalMouseDistance"   ), null },
					{ null                                                                  , null },
					{ null                                                                  , null },
					{ Language.getText( "mousePrintRecorder.tab.info.averageMouseSpeed"    ), null },
					{ null                                                                  , null },
					{ null                                                                  , null },
					{ Language.getText( "mousePrintRecorder.tab.info.numberOfSamples"      ), null },
					{ Language.getText( "mousePrintRecorder.tab.info.uncompressedDataSize" ), null },
					{ Language.getText( "mousePrintRecorder.tab.info.savedWithCompression" ), null },
				}, new Object[] { Language.getText( "mousePrintRecorder.tab.info.property" ), Language.getText( "mousePrintRecorder.tab.info.value" ) } );
			GuiUtils.addNewTab( Language.getText( "mousePrintRecorder.tab.info.title" ), Icons.INFORMATION_BALLOON, false, tabbedPane, new JScrollPane( infoTable ), null );
			getContentPane().add( tabbedPane, BorderLayout.CENTER );
			
			GuiUtils.maximizeWindowWithMargin( this, 5, new Dimension( 1000, 850 ) );
			
			setVisible( true );
		}
		
		/**
		 * Updates the status and control buttons based on whether a recording is in progress.
		 */
		protected void updateControls() {
			final Recorder recorder  = MousePrintRecorder.recorder;
			final boolean  startable = recorder == null || recorder.isCancelRequested();
			
			setIconImage( isRecording() ? Icons.FINGERPRINT_RECOGNITION.getImage() : Icons.FINGERPRINT.getImage() );
			
			GuiUtils.updateButtonText( startStopRecordingButton, startable ? "mousePrintRecorder.startRecordingButton" : "mousePrintRecorder.stopRecordingButton" );
			startStopRecordingButton.setIcon( startable ? Icons.CONTROL_RECORD : Icons.CONTROL_STOP_SQUARE );
			startStopRecordingButton.invalidate();
			saveMousePrintButton .setEnabled( recorder != null && startable );
			storeMousePrintButton.setEnabled( recorder != null && startable && recorder.sourceFileName != null );
			loadMousePrintButton .setEnabled( startable );
			if ( mousePrintCanvas != null )
				mousePrintCanvas.invalidate();
			
			if ( recorder == null )
				statusLabel.setText( Language.getText( "mousePrintRecorder.noRecordingInProgress" ) );
			else {
				if ( recorder.saved != null ) {
					statusLabel.setText( Language.getText( recorder.saved ? "mousePrintRecorder.recordingSaved" : "mousePrintRecorder.recordingSavedFailed" ) );
				}
				else if ( recorder.loaded )
					statusLabel.setText( Language.getText( "mousePrintRecorder.recordingLoaded" ) );
				else if ( startable )
					statusLabel.setText( Language.getText( "mousePrintRecorder.recordingStopped" ) );
				else
					statusLabel.setText( Language.getText( "mousePrintRecorder.recordingInProgress" ) );
			}
		}
		
		/**
		 * Updates the mouse print image and the info labels.
		 */
		protected void updateMousePrint() {
			mousePrintCanvas.repaint();
			final Recorder recorder = MousePrintRecorder.recorder;
			if ( recorder == null || recorder.startTime == null )
				return;
			
			int row = 0;
			final double dt = ( ( recorder.endTime == null ? new Date() : recorder.endTime ).getTime() - recorder.startTime.getTime() ) / 1000.0;
			infoTable.setValueAt( recorder.sourceFileName, row++, 1 );
			infoTable.setValueAt( recorder.sourceFileName == null ? null : Language.getText( "mousePrintRecorder.tab.info.fileSizeValue", String.format( Locale.ENGLISH, "%,d", new File( recorder.sourceFileName ).length() ) ), row++, 1 );
			infoTable.setValueAt( recorder.savedWithApp, row++, 1 );
			infoTable.setValueAt( GeneralUtils.formatLongSeconds( (long) dt ), row++, 1 );
			infoTable.setValueAt( Language.formatDateTime( recorder.startTime ), row++, 1 );
			infoTable.setValueAt( recorder.endTime == null ? null : Language.formatDateTime( recorder.endTime ), row++, 1 );
			infoTable.setValueAt( Language.getText( "mousePrintRecorder.tab.info.imageWidthValue", recorder.buffer.getWidth() ), row++, 1 );
			infoTable.setValueAt( Language.getText( "mousePrintRecorder.tab.info.imageHeightValue", recorder.buffer.getHeight() ), row++, 1 );
			infoTable.setValueAt( Language.getText( "mousePrintRecorder.tab.info.imageResolutionValueInch", recorder.screenResolution ), row++, 1 );
			infoTable.setValueAt( Language.getText( "mousePrintRecorder.tab.info.imageResolutionValueCm", recorder.screenResolution*100/254 ), row++, 1 );
			infoTable.setValueAt( Language.getText( "mousePrintRecorder.tab.info.samplingTimeValue", recorder.samplingTime ), row++, 1 );
			final double totalFoots  = recorder.screenResolution == 0 ? 0 : recorder.totalMouseDistance / 12 / recorder.screenResolution;
			final double totalMeters = recorder.screenResolution == 0 ? 0 : 0.0254 * recorder.totalMouseDistance / recorder.screenResolution;
			infoTable.setValueAt( Language.getText( "mousePrintRecorder.tab.info.totalMouseDistanceValuePixel", String.format( Locale.ENGLISH, "%,d", (int) recorder.totalMouseDistance ) ), row++, 1 );
			infoTable.setValueAt( Language.getText( "mousePrintRecorder.tab.info.totalMouseDistanceValueFoot", formatNumber( totalFoots ) ), row++, 1 );
			infoTable.setValueAt( Language.getText( "mousePrintRecorder.tab.info.totalMouseDistanceValueMeter", formatNumber( totalMeters ) ), row++, 1 );
			infoTable.setValueAt( Language.getText( "mousePrintRecorder.tab.info.avgMouseMovementSpeedValuePixel", formatNumber( dt == 0 ? 0 : recorder.totalMouseDistance / dt ) ), row++, 1 );
			infoTable.setValueAt( Language.getText( "mousePrintRecorder.tab.info.avgMouseMovementSpeedValueInch", formatNumber( dt == 0 ? 0 : totalFoots * 12 / dt ) ), row++, 1 );
			infoTable.setValueAt( Language.getText( "mousePrintRecorder.tab.info.avgMouseMovementSpeedValueCm", formatNumber( dt == 0 ? 0 : totalMeters * 100 / dt ) ), row++, 1 );
			infoTable.setValueAt( String.format( Locale.ENGLISH, "%,d", recorder.samplesCount ), row++, 1 );
			infoTable.setValueAt( recorder.dataStream == null ? null : Language.getText( "mousePrintRecorder.tab.info.uncompressedDataSizeValue", String.format( Locale.ENGLISH, "%,d", recorder.dataStream.size() ) ), row++, 1 );
			infoTable.setValueAt( recorder.savedWithCompression == null ? null : recorder.savedWithCompression.stringValue, row++, 1 );
		}
		
		/**
		 * Formats a number in 2 digit format.
		 * @param n number to be formatted
		 * @return the formatted number as string
		 */
		private String formatNumber( final double n ) {
			return String.format( Locale.US, "%,.2f", n );
		}
		
	}
	
	/** Reference to the current recorder. */
	private static volatile Recorder      recorder;
	/** Reference to the recorder frame.   */
	private static volatile RecorderFrame recorderFrame;
	
	/**
	 * No need to instantiate this class.
	 */
	private MousePrintRecorder() {
	}
	
	/**
	 * Tells if a recording is in progress.
	 * @return true if a recording is in progress; false otherwise
	 */
	public static synchronized boolean isRecording() {
		return recorder != null && !recorder.isCancelRequested();
	}
	
	/**
	 * Tells if a recording is present.
	 * <p>Returns true if a recording is present either if it is still in progress or if it is stopped.</p> 
	 * @return true if a recording is present; false otherwise
	 */
	public static synchronized boolean isRecordingPresent() {
		return recorder != null;
	}
	
	/**
	 * Starts recording the mouse print.
	 */
	public static synchronized void startRecording() {
		if ( recorder != null && !recorder.isCancelRequested() )
			return;
		
		recorder = new Recorder();
		recorder.start();
		MainFrame.INSTANCE.setMousePrintRecorderStatus( true );
		updateRecorderFrame();
	}
	
	/**
	 * Stops recording the mouse print.
	 */
	public static synchronized void stopRecording() {
		if ( recorder == null || recorder.isCancelRequested() )
			return;
		
		recorder.shutdown();
		MainFrame.INSTANCE.setMousePrintRecorderStatus( false );
		updateRecorderFrame();
	}
	
	/**
	 * Called when a game started.
	 */
	public static synchronized void onGameStart() {
		if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_SAVE_MOUSE_PRINTS ) ) 
			startRecording();
	}
	
	/**
	 * Called when a game ended.
	 */
	public static synchronized void onGameEnd() {
		if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_SAVE_MOUSE_PRINTS ) ) { 
			stopRecording();
			if ( recorder != null ) {
				recorder.save();
			
    			if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_STORE_MOUSE_PRINTS ) ) {
    				final String authorizationKey = Settings.getString( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY );
    				if ( authorizationKey.length() == 0 )
    					System.out.println( "Failed to store mouse print in the Sc2gears Database: no Authorization key is set: " + recorder.sourceFileName );
    				else
    					new NormalThread( "Mouse print auto-storer" ) {
    						@Override
    						public void run() {
    							try {
    								MainFrame.registerBackgroundJob();
    								GeneralUtils.storeMousePrint( new File( recorder.sourceFileName ), authorizationKey );
    							} finally {
    								MainFrame.removeBackgroundJob();
    							}
    						}
    					}.start();
    			}
			}
		}
	}
	
	/**
	 * Saves the recording.
	 * <p>The recording can only be saved if a recording is not in progress.
	 * Does nothing if a recording is not present or a recording is in progress.</p>
	 */
	public static synchronized void saveRecording() {
		if ( recorder != null && !recorder.isCancelRequested() )
			recorder.save();
	}
	
	/**
	 * Updates the recorder frame to reflect the state of the recorder.
	 */
	private static void updateRecorderFrame() {
		final RecorderFrame recorderFrame = MousePrintRecorder.recorderFrame;
		if ( recorderFrame != null ) {
			recorderFrame.updateControls();
			recorderFrame.getContentPane().validate();
			recorderFrame.getContentPane().repaint();
			recorderFrame.updateMousePrint();
		}
	}
	
	/**
	 * Displays the recorder frame which can be used to manually start/stop the recording and to display the recorded mouse print.
	 */
	public static synchronized void showFrame() {
		final RecorderFrame recorderFrame = MousePrintRecorder.recorderFrame;
		
		if ( recorderFrame == null ) {
			MousePrintRecorder.recorderFrame = new RecorderFrame();
			MousePrintRecorder.recorderFrame.updateMousePrint();
		}
		else {
			if ( recorderFrame.getExtendedState() == JFrame.ICONIFIED )
				recorderFrame.setExtendedState( JFrame.NORMAL );
			recorderFrame.toFront();
		}
	}
	
	/**
	 * Returns a reference to the recorder frame.
	 * @return a reference to the recorder frame
	 */
	public static synchronized JFrame getRecorderFrame() {
		return recorderFrame;
	}
	
	/**
	 * Stores the specified mouse prints in the Sc2gears Database.
	 * @param files mouse print files to store
	 */
	private static void storeMousePrints( final File[] files ) {
		final String authorizationKey = GeneralUtils.checkKeyBeforeStoringOrDownloading( recorderFrame );
		if ( authorizationKey == null )
			return;
		
		final ProgressDialog progressDialog = new ProgressDialog( "mousePrintRecorder.storingMousePrints", Icons.SERVER_NETWORK, files.length );
		new NormalThread( "Mouse print storer" ) {
			@Override
			public void run() {
				for ( final File file : files ) {
					if ( progressDialog.isAborted() )
						break;
					
					if ( !GeneralUtils.storeMousePrint( file, authorizationKey ) )
						progressDialog.incrementFailed();
					
					progressDialog.incrementProcessed();
					progressDialog.updateProgressBar();
				}
				
				progressDialog.taskFinished();
			}
		}.start();
	}
	
}
