/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.services.streaming;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.services.BaseHttpClientHandler;
import hu.belicza.andras.sc2gears.services.HttpServer;
import hu.belicza.andras.sc2gears.services.HttpServer.HttpClientHandlerFactory;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.ControlledThread;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.MessageException;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Private Video Streaming.
 * 
 * TODO New ideas:
 * 	-black list (allow/block IP ranges)
 * 	-Viewers (viewing since, status=>no frame dl for let's say 10 sec)
 *  -Color viewiers table: active => green, inactive => red
 * 	-Overlays (custom texts, pictures, )
 * 	-use the AVIWriter to send the video to the clients, embed with HTML video tag?
 * 
 * 	-AVI writer soruce: http://rsb.info.nih.gov/ij/developer/source/
 * 		Another: http://www.randelshofer.ch/blog/2008/08/writing-avi-videos-in-pure-java/
 * 
 * @author Andras Belicza
 */
public class PrivateVideoStreaming {
	
	/** Streamer version. */
	public static final String VERSION = "1.3";
	
	/** Tells if windows native screen capture method is supported. */
	private static final boolean windowsNativeMethodSupported = GeneralUtils.isWindows();
	
	/** HTML header data of the streaming web pages. */
	private static final byte[] STREAMING_PAGE_HEADER_DATA;
	/** HTML body data of the stream viewer page.   */
	private static final byte[] STREAMING_PAGE_STREAM_VIEWER_DATA;
	/** HTML body data of the password form page. */
	private static final byte[] STREAMING_PAGE_PASSWORD_FORM_DATA;
	/** HTML footer data of the streaming web pages. */
	private static final byte[] STREAMING_PAGE_FOOTER_DATA;
	static {
		try {
			final List< byte[] > contentList = new ArrayList< byte[] >();
			
			final ByteArrayOutputStream content = new ByteArrayOutputStream();
			final byte[]                buffer  = new byte[ 4192 ];
			
			for ( final String resourceName : new String[] { "header.html", "streamviewer.html", "passwordform.html", "footer.html" } ) {
				content.reset();
				
				try ( final InputStream input = PrivateVideoStreaming.class.getResourceAsStream( resourceName ) ) {
					int bytesRead;
					while ( ( bytesRead = input.read( buffer ) ) > 0 )
						content.write( buffer, 0, bytesRead );
					
					contentList.add( content.toByteArray() );
				}
			}
			
			int i = 0;
			STREAMING_PAGE_HEADER_DATA        = contentList.get( i++ );
			STREAMING_PAGE_STREAM_VIEWER_DATA = contentList.get( i++ );
			STREAMING_PAGE_PASSWORD_FORM_DATA = contentList.get( i++ );
			STREAMING_PAGE_FOOTER_DATA        = contentList.get( i++ );
			
		} catch ( final IOException ie ) {
			ie.printStackTrace();
			throw new RuntimeException( "Failed to load resources!", ie );
		}
	}
	
	/** Header keys of the viewers table. */
	private static final String[] VIEWERS_HEADER_KEYS = new String[] {
		"privateVideoStreaming.stats.viewers.viewerIp",
		"privateVideoStreaming.stats.viewers.framesSent",
		"privateVideoStreaming.stats.viewers.bandwidth",
		"privateVideoStreaming.stats.viewers.badPassword"
	};
	/** Header names of the viewers table. */
	private static final String[] VIEWERS_HEADER_NAMES = new String[ VIEWERS_HEADER_KEYS.length ];
	static {
		for ( int i = 0; i < VIEWERS_HEADER_KEYS.length; i++ )
			VIEWERS_HEADER_NAMES[ i ] = Language.getText( VIEWERS_HEADER_KEYS[ i ] );
	}
	
	/**
	 * Screen area to stream.
	 * @author Andras Belicza
	 */
	public static enum ScreenAreaToStream {
		FULL_SCREEN( "privateVideoStreaming.params.screenAreaToStream.fullScreen" ),
		CUSTOM_AREA( "privateVideoStreaming.params.screenAreaToStream.customArea" );
		
		/** Cache of the string value. */
		public final String stringValue;
		
		/**
		 * Creates a new ScreenAreaToStream.
		 * @param textKey key of the text representation
		 */
		private ScreenAreaToStream( final String textKey ) {
			stringValue = Language.getText( textKey );
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Output video size.
	 * @author Andras Belicza
	 */
	public static enum OutputVideoSize {
		ORIGINAL_SIZE           ( "privateVideoStreaming.params.outputVideoSize.originalSize"         ,    0,    0 ),
		RESIZE_KEEP_ASPECT_RATIO( "privateVideoStreaming.params.outputVideoSize.resizeKeepAspectRatio",    0,    0 ),
		RESIZE_DISTORTED        ( "privateVideoStreaming.params.outputVideoSize.resizeDistorted"      ,    0,    0 ),
		FULL_HD                 ( "privateVideoStreaming.params.outputVideoSize.fullHd"               , 1920, 1080 ),
		HD                      ( "privateVideoStreaming.params.outputVideoSize.hd"                   , 1280,  720 ),
		STANDARD                ( "privateVideoStreaming.params.outputVideoSize.standard"             ,  854,  480 ),
		LOWD_360                ( "privateVideoStreaming.params.outputVideoSize.lowd360"              ,  640,  360 ),
		LOWD_240                ( "privateVideoStreaming.params.outputVideoSize.lowd240"              ,  320,  240 ),
		DVD                     ( "privateVideoStreaming.params.outputVideoSize.dvd"                  ,  720,  576 ),
		SXGA                    ( "privateVideoStreaming.params.outputVideoSize.sxga"                 , 1280, 1024 ),
		XGA                     ( "privateVideoStreaming.params.outputVideoSize.xga"                  , 1024,  768 ),
		VGA                     ( "privateVideoStreaming.params.outputVideoSize.vga"                  ,  640,  480 ),
		CGA                     ( "privateVideoStreaming.params.outputVideoSize.cga"                  ,  320,  200 );
		
		/** Cache of the string value. */
		public final String stringValue;
		/** Video width.               */
		public final int    width;
		/** Video height.              */
		public final int    height;
		
		/**
		 * Creates a new OutputVideoSize.
		 * @param textKey key of the text representation
		 * @param width video width
		 * @param height video height
		 */
		private OutputVideoSize( final String textKey, final int width, final int height ) {
			stringValue = Language.getText( textKey );
			this.width  = width;
			this.height = height;
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Screen capture method.
	 * @author Andras Belicza
	 */
	public static enum ScreenCaptureMethod {
		STANDARD_JAVA ( "privateVideoStreaming.params.screenCaptureMethod.standardJava"  ),
		WINDOWS_NATIVE( "privateVideoStreaming.params.screenCaptureMethod.windowsNative" );
		
		/** Cache of the string value. */
		public final String stringValue;
		
		/**
		 * Creates a new ScreenCaptureMethod.
		 * @param textKey key of the text representation
		 */
		private ScreenCaptureMethod( final String textKey ) {
			stringValue = Language.getText( textKey );
		}
		
		/**
		 * Returns a {@link ScreenshotProducer} for this screen capture method.
		 * @param screenArea screen area to create screenshot from
		 * @return a {@link ScreenshotProducer} for this screenshot method
		 */
		public ScreenshotProducer getScreenshotProducer( final Rectangle screenArea ) {
			switch ( this ) {
			case STANDARD_JAVA :
				return new ScreenshotProducer( screenArea ) {
					private final Robot robot;
					{
						try {
							robot = new Robot();
						} catch ( final AWTException ae ) {
							throw new MessageException( Language.getText( "privateVideoStreaming.errors.robotCreation" ), ae );
						}
					}
					@Override
					public BufferedImage getScreenshot() {
						return robot.createScreenCapture( screenArea );
					}
				};
			case WINDOWS_NATIVE :
				return new JNAScreenshot( screenArea );
			default :
				throw new MessageException( "Selected screen capture method not implemented!" );
			}
		}
		
		@Override
		public String toString() {
			return stringValue;
		};
	}
	
	/**
	 * Statistics of a stream viewer.
	 * 
	 * @author Andras Belicza
	 */
	private static class ViewerStats implements Comparable< ViewerStats > {
		/** IP address of the viewer.            */
		public final String  ip;
		/** Frames transmitted.                  */
		public volatile int  framesTransmitted;
		/** Total outgoing bandwidth.            */
		public volatile long totalOutBandwidth;
		/** Bad password attempts.               */
		public volatile int  badPasswordAttempts;
		
		/**
		 * Creates a new ViewerStats.
		 */
		public ViewerStats( final String ip ) {
			this.ip = ip;
		}
		
		/**
		 * Registers a bad password attempt.
		 */
		public synchronized void registerBadPasswordAttempt() {
			badPasswordAttempts++;
		}
		
		/**
		 * Registers a new frame being transmitted to the viewer.
		 * @param size size of the frame in bytes
		 */
		public synchronized void registerFrameTransmitted( final long size ) {
			framesTransmitted++;
			totalOutBandwidth += size;
		}
		
		@Override
		public int compareTo( final ViewerStats vs ) {
			return ip.compareTo( vs.ip );
		}
		
	}
	
	/**
	 * The streamer thread.
	 * @author Andras Belicza
	 */
	private static class Streamer extends ControlledThread {
		
		private static final String IMAGE_FORMAT = "jpg";
		
		/** Streaming start time.               */
		public           Date                  startTime;
		/** Streaming end time.                 */
		public volatile  Date                  endTime;
		
		/** Screenshot producer.                */
		private final    ScreenshotProducer    screenshotProducer;
		/** Image writer instance.              */
		private final    ImageWriter           imageWriter; 
		/** Image write parameters.             */
		private final    ImageWriteParam       writeParams;
		
		/** Input (streamed) screen area.       */
		private final    Rectangle             inputArea;
		
		/** Output image dimension.             */
		private final    Dimension             outputSize;
		/** Outgoing buffered image.            */
		private final    BufferedImage         outBufferedImage;
		/** Outgoing buffered image.            */
		private final    Graphics2D            outBufferedImageG2;
		/** Output stream buffer used to output the compressed image. */
		private final    ByteArrayOutputStream outImageDataBuffer = new ByteArrayOutputStream( 128*1024 );
		
		/** The last screenshot image data.     */
		private volatile byte[]                lastScreenshotData;
		
		/** Host HTML URL (page to view the stream in a browser). */
		private final    String                hostHtmlUrl;
		
		/** Delay time between samples (1/FPS). */
		private final long                     delayTime;      
		
		/** AVI video writer. */
		public final AviWriter aviWriter;
		
		/** HTTP server listening for clients. */
		private HttpServer httpServer;
		
		// Calculated statistics
		/** Frames captured.          */
		public volatile int        framesCaptured;
		/** Total frames size.        */
		public volatile long       totalFrameSize;
		/** Frames transmitted.       */
		public final AtomicInteger framesTransmitted = new AtomicInteger();
		/** Total outgoing bandwidth. */
		public final AtomicLong    totalOutBandwidth = new AtomicLong();
		
		/** Viewer stats mapped from viewer IP address. */
		public final Map< String, ViewerStats > ipViewerStatsMap = new HashMap< String, ViewerStats >();
		
		/**
		 * Creates a new Streamer.
		 */
		public Streamer() {
			super( "Streamer" );
			
			switch ( ScreenAreaToStream.values()[ Settings.getInt( Settings.KEY_PRIVATE_STREAMING_SCREEN_AREA_TO_STREAM ) ] ) {
			case FULL_SCREEN :
				inputArea = new Rectangle( Toolkit.getDefaultToolkit().getScreenSize() );
				break;
			case CUSTOM_AREA : {
				final int x1     = Settings.getInt( Settings.KEY_PRIVATE_STREAMING_CUSTOM_AREA_LEFT );
				final int y1     = Settings.getInt( Settings.KEY_PRIVATE_STREAMING_CUSTOM_AREA_TOP  );
				final int width  = Settings.getInt( Settings.KEY_PRIVATE_STREAMING_CUSTOM_AREA_RIGHT  ) - x1 + 1;
				final int height = Settings.getInt( Settings.KEY_PRIVATE_STREAMING_CUSTOM_AREA_BOTTOM ) - y1 + 1;
				if ( width < 1 || height < 1 )
					throw new MessageException( Language.getText( "privateVideoStreaming.errors.invalidCustomArea" ) );
				inputArea = new Rectangle( x1, y1, width, height );
				break;
			}
			default : throw new MessageException( "Selected Screen area to stream mode not implemented!" );
			}
			
			screenshotProducer = ScreenCaptureMethod.values()[ Settings.getInt( Settings.KEY_PRIVATE_STREAMING_SCREEN_CAPTURE_METHOD ) ].getScreenshotProducer( inputArea );
			
			final OutputVideoSize outputVideoSize = OutputVideoSize.values()[ Settings.getInt( Settings.KEY_PRIVATE_STREAMING_OUTPUT_VIDEO_SIZE ) ];
			if ( outputVideoSize == OutputVideoSize.ORIGINAL_SIZE ) {
				outputSize = inputArea.getSize();
			}
			else {
				int resizeWidth  = outputVideoSize.width  > 0 ? outputVideoSize.width  : Settings.getInt( Settings.KEY_PRIVATE_STREAMING_CUSTOM_RESIZE_WIDTH  );
				int resizeHeight = outputVideoSize.height > 0 ? outputVideoSize.height : Settings.getInt( Settings.KEY_PRIVATE_STREAMING_CUSTOM_RESIZE_HEIGHT );
				if ( resizeWidth < 1 || resizeHeight < 1 )
					throw new MessageException( Language.getText( "privateVideoStreaming.errors.invalidResizeDimension" ) );
				if ( outputVideoSize != OutputVideoSize.RESIZE_DISTORTED ) {
					final double zoomX = inputArea.getWidth () / resizeWidth ;
					final double zoomY = inputArea.getHeight() / resizeHeight;
					if ( zoomX > zoomY )
						resizeHeight = (int) ( inputArea.getHeight() / zoomX );
					else
						resizeWidth  = (int) ( inputArea.getWidth () / zoomY );
					if ( resizeWidth < 1 || resizeHeight < 1 )
						throw new MessageException( Language.getText( "privateVideoStreaming.errors.invalidResizeDimension" ) );
				}
				outputSize = new Dimension( resizeWidth, resizeHeight );
			}
			
			delayTime = 1000 / Settings.getInt( Settings.KEY_PRIVATE_STREAMING_REFRESH_RATE_FPS );
			
			final String streamPassword = Settings.getString( Settings.KEY_PRIVATE_STREAMING_STREAM_PASSWORD ).trim();
			try {
				if ( !streamPassword.equals( URLEncoder.encode( streamPassword, "UTF-8" ) ) )
					throw new MessageException( Language.getText( "privateVideoStreaming.errors.invalidStreamPassword" ) );
			} catch ( final UnsupportedEncodingException uee ) {
				// Never to happen...
			}
			
			final Iterator< ImageWriter > imageWriterIterator = ImageIO.getImageWritersByFormatName( IMAGE_FORMAT );
			if ( !imageWriterIterator.hasNext() )
				throw new MessageException( Language.getText( "privateVideoStreaming.errors.missingImageHandler" ) );
			
			try {
				final int port = Settings.getInt( Settings.KEY_PRIVATE_STREAMING_SERVER_PORT );
				
				final String baseHostHtmlResource = "/stream/";
				final String hostHtmlResource     = baseHostHtmlResource + ( streamPassword.isEmpty() ? "" : streamPassword + "/" );
				
				hostHtmlUrl = "http://" + InetAddress.getLocalHost().getHostAddress() + ( port == 80 ? "" : ":" + port ) + hostHtmlResource;
				
				httpServer = new HttpServer( port, new HttpClientHandlerFactory() {
					@Override
					public BaseHttpClientHandler createHttpClientHandler( final Socket socket ) {
						return new BaseHttpClientHandler( socket ) {
							final String cycleCheckUrlStart = hostHtmlResource + "cycle?";
							final String videoDataUrlStart  = hostHtmlResource + "videoData?";
							@Override
							protected void handleResource( final String resource ) throws IOException {
								// Order: frequency order, descending
								if ( resource.startsWith( videoDataUrlStart ) ) {
									
									final byte[] screenCaptureData = lastScreenshotData;
									getViewerStats().registerFrameTransmitted( screenCaptureData.length );
									initResponse( HttpResponseCode.OK );
									printHeaderField( "Content-type", "image/jpeg" );
									printHeaderField( "Content-length", Integer.toString( screenCaptureData.length ) );
									closeHeader();
									output.write( screenCaptureData );
									framesTransmitted.incrementAndGet();
									totalOutBandwidth.addAndGet( screenCaptureData.length );
									
								}
								else if ( resource.startsWith( cycleCheckUrlStart ) ) {
									
									initResponse( HttpResponseCode.OK );
									printHeaderField( "Content-type", "text/html" );
									closeHeader();
									print( "<html><body><script>parent.checkCycle(", startTime.getTime(), ");</script></body></html>" );
									
								}
								else if ( hostHtmlResource.equals( resource ) ) {
									
									initResponse( HttpResponseCode.OK );
									printHeaderField( "Content-type", "text/html" );
									closeHeader();
									output.write( STREAMING_PAGE_HEADER_DATA );
									// Pass variables
									final String streamName        = Settings.getString( Settings.KEY_PRIVATE_STREAMING_STREAM_NAME        ).replace( "\"", "\\\"" );
									final String streamDescription = Settings.getString( Settings.KEY_PRIVATE_STREAMING_STREAM_DESCRIPTION ).replace( "\"", "\\\"" ).replace( "\n", "\\n" );
									print( "<script>",
										"streamName=\"", streamName,
										"\",streamDescription=\"", streamDescription,
										"\",cycle=", startTime.getTime(),
										",origWidth=", outputSize.width,
										",origHeight=", outputSize.height,
										",videoUrl=\"", videoDataUrlStart,
										"\",delayTime=", delayTime,
										",cycleCheckUrl=\"", cycleCheckUrlStart,
										"\",version=\"", VERSION,
										"\";</script>" );
									output.write( STREAMING_PAGE_STREAM_VIEWER_DATA   );
									output.write( STREAMING_PAGE_FOOTER_DATA );
									
								}
								else if ( resource.startsWith( baseHostHtmlResource ) ) {
									
									// Stream password (end of URL) does not match
									initResponse( HttpResponseCode.OK );
									printHeaderField( "Content-type", "text/html" );
									closeHeader();
									output.write( STREAMING_PAGE_HEADER_DATA );
									if ( streamPassword.isEmpty() )
										print( "<script>window.location=\"", hostHtmlResource, "\";</script>" );
									else {
										// Display password form
										final String prevPassword = resource.length() == baseHostHtmlResource.length() ? ""
											: resource.substring( baseHostHtmlResource.length(), resource.endsWith( "/" ) ? resource.length()-1 : resource.length() ).replace( "\"", "\\\"" );
										if ( !prevPassword.isEmpty() ) // We require password, the client provided a password and they do not match!
											getViewerStats().registerBadPasswordAttempt();
										print( "<script>",
											"baseUrl=\"", baseHostHtmlResource,
											"\",version=\"", VERSION,
											"\",prevPassword=\"", prevPassword,
											"\";</script>" );
										output.write( STREAMING_PAGE_PASSWORD_FORM_DATA );
									}
									output.write( STREAMING_PAGE_FOOTER_DATA   );
									
								} else if ( "/favicon.ico".equals( resource ) ) {
									
									initResponse( HttpResponseCode.OK );
									printHeaderField( "Content-type", "image/x-icon" );
									closeHeader();
									try ( final InputStream faviconStream = PrivateVideoStreaming.class.getResourceAsStream( "favicon.ico" ) ) {
										int data;
										while ( ( data = faviconStream.read() ) != -1 )
											output.write( data );
									}
									
								} else {
									sendError( HttpResponseCode.NOT_FOUND );
								}
							}
							private ViewerStats getViewerStats() {
								final String ip = socket.getInetAddress().getHostAddress();
								synchronized ( httpServer ) {
									ViewerStats vs = ipViewerStatsMap.get( ip );
									if ( vs == null )
										ipViewerStatsMap.put( ip, vs = new ViewerStats( ip ) );
									return vs;
								}
							}
						};
					}
				} );
			} catch ( final IOException ie ) {
				ie.printStackTrace();
				throw new MessageException( Language.getText( "privateVideoStreaming.errors.httpServerStartFailed" ), ie );
			}
			
			// TODO new diagnostic test: check save video folder, also check if free space on that drive (less than 100 MB should give FAIL, less than 1 GB should give warning)
			// TODO new diagnostic test: check the server port
			
			if ( Settings.getBoolean( Settings.KEY_PRIVATE_STREAMING_SAVE_VIDEO_AS_AVI ) ) {
				final File saveVideoFolder = new File( Settings.getString( Settings.KEY_PRIVATE_STREAMING_SAVE_VIDEO_FOLDER ) );
				if ( saveVideoFolder.exists() && saveVideoFolder.isFile() )
					throw new MessageException( Language.getText( "privateVideoStreaming.errors.saveVideoFolderIsAFile", saveVideoFolder.getAbsolutePath() ) );
				if ( !saveVideoFolder.exists() && !saveVideoFolder.mkdirs() )
					throw new MessageException( Language.getText( "privateVideoStreaming.errors.couldNotCreateSaveVideoFolder", saveVideoFolder.getAbsolutePath() ) );
				
				final File aviFile = new File( saveVideoFolder, "stream " + new SimpleDateFormat( "yy-MM-dd HH-mm-ss" ).format( new Date() ) + ".avi" );
				try {
					aviWriter = new AviWriter( aviFile, outputSize.width, outputSize.height, Settings.getInt( Settings.KEY_PRIVATE_STREAMING_FIX_AVI_FPS ) );
				} catch ( final IOException ie ) {
					httpServer.requestToCancel(); // HTTP server is already created...
					throw new MessageException( Language.getText( "privateVideoStreaming.errors.cannotWriteAviFile", aviFile.getAbsolutePath() ), ie );
				}
			}
			else
				aviWriter = null;
			
			imageWriter = imageWriterIterator.next();
			
			writeParams = imageWriter.getDefaultWriteParam();
			writeParams.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );
			writeParams.setCompressionQuality( Settings.getInt( Settings.KEY_PRIVATE_STREAMING_IMAGE_QUALITY ) / 100.0f );
			
			imageWriter.setOutput( new MemoryCacheImageOutputStream( outImageDataBuffer ) );
			
			outBufferedImage   = new BufferedImage( outputSize.width, outputSize.height, BufferedImage.TYPE_3BYTE_BGR );
			outBufferedImageG2 = outBufferedImage.createGraphics();
		}
		
		@Override
		public void run() {
			final ControlledThread aviWriteTimer = aviWriter == null ? null : new ControlledThread( "AVI write timer" ) {
				@Override
				public void run() {
					// Wait the first frame
					while ( lastScreenshotData == null && !requestedToCancel )
	                    try {
	                        Thread.sleep( 1 );
                        } catch ( final InterruptedException ie ) {
	                        ie.printStackTrace();
                        }
					final long nsBetweenFrames = 1000000000L / Settings.getInt( Settings.KEY_PRIVATE_STREAMING_FIX_AVI_FPS );
					while ( !requestedToCancel ) {
						long timeInfo = System.nanoTime();
						try {
	                        aviWriter.writeFrame( lastScreenshotData );
                        } catch ( final IOException ie ) {
	                        // TODO stop writing?
                        }
						try {
							// Exact timing: remove the write time from the time between frames..
							timeInfo = System.nanoTime() - timeInfo;
							Thread.sleep( timeInfo >= nsBetweenFrames ? 1 : ( nsBetweenFrames - timeInfo ) / 1000000L ); // Sleep at least 1 ms...
						} catch ( final InterruptedException ie ) {
							ie.printStackTrace();
						}
					}
				}
			};
			if ( aviWriteTimer != null )
				aviWriteTimer.start();
			httpServer.start();
			
			startTime = new Date();
			
			StreamerFrame streamerFrame;
			if ( ( streamerFrame = PrivateVideoStreaming.streamerFrame ) != null ) 
				streamerFrame.updateState();
			
			// Prepare mouse cursor image
			// TODO
			/*final BufferedImage mouseCursorImage = new BufferedImage( 16 * outputSize.width / inputArea.width, 16 * outputSize.height / inputArea.height, BufferedImage.TYPE_INT_ARGB );
			{
				final Graphics2D cursorG2 = mouseCursorImage.createGraphics();
				cursorG2.drawImage( Icons.CURSOR.getImage(), 0, 0, mouseCursorImage.getWidth(), mouseCursorImage.getHeight(), null );
				cursorG2.dispose();
			}*/
			
			final IIOImage iioOutBufferedImage = new IIOImage( outBufferedImage, null, null );
			long lastStatUpdateNs = System.nanoTime();
			while ( !requestedToCancel ) {
				
				long timeInfo = System.nanoTime();
				
				final BufferedImage screenshot = screenshotProducer.getScreenshot();
				outBufferedImageG2.drawImage( screenshot, 0, 0, outputSize.width, outputSize.height, null );
				// Draw mouse cursor
				// TODO translate coordinates (scale, move regarding to custom input area)
				/*final PointerInfo pointerInfo = MouseInfo.getPointerInfo();
				if ( pointerInfo != null )
					outBufferedImageG2.drawImage( mouseCursorImage, pointerInfo.getLocation().x, pointerInfo.getLocation().y, null );*/
				
				outImageDataBuffer.reset();
				try {
					imageWriter.write( null, iioOutBufferedImage, writeParams );
					lastScreenshotData = outImageDataBuffer.toByteArray();
					
					framesCaptured++;
					totalFrameSize += lastScreenshotData.length;
				} catch ( final IOException ie ) {
					ie.printStackTrace();
				}
				
				if ( ( streamerFrame = PrivateVideoStreaming.streamerFrame ) != null && timeInfo - lastStatUpdateNs > 500000000 ) { 
					streamerFrame.updateStats();
					lastStatUpdateNs = timeInfo;
				}
				
				try {
					// Image capture and processing takes time, remove that time from the frame delay time:
					timeInfo = ( System.nanoTime() - timeInfo ) / 1000000L;
					Thread.sleep( timeInfo >= delayTime ? 1 : delayTime - timeInfo ); // Sleep at least 1 ms...
				} catch ( final InterruptedException ie ) {
					ie.printStackTrace();
				}
			}
			
			if ( aviWriteTimer != null ) {
				aviWriteTimer.requestToCancel();
				try {
					aviWriteTimer.join();
				} catch ( final InterruptedException ie ) {
					ie.printStackTrace();
				}
				try {
					aviWriter.close();
				} catch ( final IOException ie ) {
					// TODO
					System.out.println( "Failed to properly close the AVI file, the recording will be corrupted!" );
					ie.printStackTrace();
				}
			}
			httpServer.requestToCancel();
			
			screenshotProducer.close();
			
			imageWriter.dispose();
			outBufferedImageG2.dispose();
			
			try {
				httpServer.join();
			} catch ( final InterruptedException ie ) {
				ie.printStackTrace();
			}
		}
	}
	
	/**
	 * Frame of the streamer (UI).
	 * @author Andras Belicza
	 */
	@SuppressWarnings("serial")
	private static class StreamerFrame extends JFrame {
		
		/** Reference to the start/stop streaming button. */
		private final JButton    startStopStreamingButton  = new JButton();
		/** Label to display streaming status.            */
		private final JLabel     statusLabel               = new JLabel();
		
		/** Text field to display the stream URL.         */
		private final JTextField streamUrlTextField        = new JTextField();
		/** Button to copy the stream URL to clipboard.   */
		private final JButton    copyUrlButton             = new JButton( Icons.CLIPBOARD_SIGN );
		/** Button to open the stream in a browser.       */
		private final JButton    openStreamInBrowserButton = new JButton( Icons.APPLICATION_BROWSER );
		
		/** Reference to the params box.                  */
		private final Box        paramsBox                 = Box.createVerticalBox();
		/** Reference to the stats table.                 */
		private final JTable     statsTable                = GuiUtils.createNonEditableTable();
		/** Reference to the viewers table.               */
		private final JTable     viewersTable              = GuiUtils.createNonEditableTable();
		
		/** Reference to the listener which enables custom area param components only if applicable.   */
		private final ActionListener customAreaComponentsEnablerListener;
		/** Reference to the listener which enables custom resize param components only if applicable. */
		private final ActionListener customResizeComponentsEnablerListener;
		
		/**
		 * Creates a new RecorderFrame.
		 */
		public StreamerFrame() {
			super( Language.getText( "privateVideoStreaming.title", Consts.APPLICATION_NAME ) );
			setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
			addWindowListener( new WindowAdapter() {
				@Override
				public void windowClosing( final WindowEvent event ) {
					synchronized ( PrivateVideoStreaming.class ) {
						streamerFrame.dispose();
						streamerFrame = null;
					}
				}
			} );
			
			final Box northBox = Box.createVerticalBox();
			startStopStreamingButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					if ( streamer == null || streamer.isCancelRequested() )
						startStreaming();
					else
						stopStreaming();
				}
			} );
			JPanel wrapper = new JPanel();
			wrapper.add( startStopStreamingButton );
			wrapper.add( GuiUtils.wrapInPanel( GeneralUtils.createLinkLabel( Language.getText( "privateVideoStreaming.moreAboutPrivateVideoStreaming" ), Consts.URL_PRIVATE_VIDEO_STREAMING ) ) );
			northBox.add( wrapper );
			GuiUtils.changeFontToBold( statusLabel );
			northBox.add( GuiUtils.wrapInPanel( statusLabel ) );
			Box row = Box.createHorizontalBox();
			row.setBorder( BorderFactory.createEmptyBorder( 5, 10, 5, 10 ) );
			row.add( new JLabel( Language.getText( "privateVideoStreaming.privateStreamUrl" ) ) );
			streamUrlTextField.setEditable( false );
			row.add( streamUrlTextField );
			GuiUtils.updateButtonText( copyUrlButton, "privateVideoStreaming.copyUrlButton" );
			copyUrlButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.copyToClipboard( streamUrlTextField.getText() );
				}
			} );
			row.add( copyUrlButton );
			GuiUtils.updateButtonText( openStreamInBrowserButton, "privateVideoStreaming.openStreamInBrowserButton" );
			openStreamInBrowserButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					GeneralUtils.showURLInBrowser( streamUrlTextField.getText() );
				}
			} );
			row.add( openStreamInBrowserButton );
			northBox.add( row );
			wrapper = new JPanel();
			wrapper.add( GuiUtils.changeFontToItalic( new JLabel( Language.getText( "privateVideoStreaming.externalIpNote" ) ) ) );
			wrapper.add( Box.createHorizontalStrut( 5 ) );
			wrapper.add( GuiUtils.changeFontToItalic( GeneralUtils.createLinkLabel( Language.getText( "privateVideoStreaming.checkYourExternalIp" ), "https://pubgoods.appspot.com/location" ) ) );
			northBox.add( wrapper );
			getContentPane().add( GuiUtils.wrapInBorderPanel( northBox ), BorderLayout.NORTH );
			
			paramsBox.setBorder( BorderFactory.createTitledBorder( Language.getText( "privateVideoStreaming.params.title" ) ) );
			final Box firstRow = row = Box.createHorizontalBox();;
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.streamName" ) ) );
			row.add( GuiUtils.createTextField( Settings.KEY_PRIVATE_STREAMING_STREAM_NAME ) );
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.streamPassword" ) ) );
			row.add( GuiUtils.createTextField( Settings.KEY_PRIVATE_STREAMING_STREAM_PASSWORD ) );
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.streamPasswordDescription" ) ) );
			// First row will be added at the end...
			row = Box.createHorizontalBox();
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.streamDescription" ) ) );
			final JTextArea streamDescriptionTextArea = GuiUtils.createTextArea( Settings.KEY_PRIVATE_STREAMING_STREAM_DESCRIPTION );
			streamDescriptionTextArea.setRows( 2 );
			streamDescriptionTextArea.setColumns( 2 );
			row.add( new JScrollPane( streamDescriptionTextArea ) );
			paramsBox.add( row );
			row = Box.createHorizontalBox();
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.screenCaptureMethod" ) ) );
			final JComboBox< ScreenCaptureMethod > screenCaptureMethodComboBox = GuiUtils.createComboBox( windowsNativeMethodSupported ? ScreenCaptureMethod.values() : new ScreenCaptureMethod[] { ScreenCaptureMethod.STANDARD_JAVA }, Settings.KEY_PRIVATE_STREAMING_SCREEN_CAPTURE_METHOD );
			row.add( screenCaptureMethodComboBox );
			paramsBox.add( row );
			final Box screenAreaToStreamParamsBox = row = Box.createHorizontalBox();
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.screenAreaToStream" ) ) );
			final JComboBox< ScreenAreaToStream > streamScreenAreaComboBox = GuiUtils.createComboBox( ScreenAreaToStream.values(), Settings.KEY_PRIVATE_STREAMING_SCREEN_AREA_TO_STREAM );
			row.add( streamScreenAreaComboBox );
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.customAreaLeft" ) ) );
			JSpinner sp;
			row.add( sp = GuiUtils.createSpinner( Settings.KEY_PRIVATE_STREAMING_CUSTOM_AREA_LEFT  , -5000, 5000, 1 ) );
			sp.setPreferredSize( new Dimension( 80, sp.getPreferredSize().height ) );
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.customAreaTop" ) ) );
			row.add( sp = GuiUtils.createSpinner( Settings.KEY_PRIVATE_STREAMING_CUSTOM_AREA_TOP   , -5000, 5000, 1 ) );
			sp.setPreferredSize( new Dimension( 80, sp.getPreferredSize().height ) );
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.customAreaRight" ) ) );
			row.add( sp = GuiUtils.createSpinner( Settings.KEY_PRIVATE_STREAMING_CUSTOM_AREA_RIGHT , -5000, 5000, 1 ) );
			sp.setPreferredSize( new Dimension( 80, sp.getPreferredSize().height ) );
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.customAreaBottom" ) ) );
			row.add( sp = GuiUtils.createSpinner( Settings.KEY_PRIVATE_STREAMING_CUSTOM_AREA_BOTTOM, -5000, 5000, 1 ) );
			sp.setPreferredSize( new Dimension( 80, sp.getPreferredSize().height ) );
			streamScreenAreaComboBox.addActionListener( customAreaComponentsEnablerListener = new ActionListener() {
				{ actionPerformed( null ); } // Set initial enabled status
				@Override
				public void actionPerformed( final ActionEvent event ) {
					for ( int i = screenAreaToStreamParamsBox.getComponentCount() - 1; i >= 2; i-- )
						screenAreaToStreamParamsBox.getComponent( i ).setEnabled( streamScreenAreaComboBox.getSelectedIndex() > 0 );
				}
			} );
			paramsBox.add( row );
			final Box outputVideoSizeParamsBox = row = Box.createHorizontalBox();
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.outputVideoSize" ) ) );
			final JComboBox< OutputVideoSize > outputVideoSizeComboBox = GuiUtils.createComboBox( OutputVideoSize.values(), Settings.KEY_PRIVATE_STREAMING_OUTPUT_VIDEO_SIZE );
			outputVideoSizeComboBox.setMaximumRowCount( outputVideoSizeComboBox.getItemCount() );
			row.add( outputVideoSizeComboBox );
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.customResizeWidth" ) ) );
			row.add( sp = GuiUtils.createSpinner( Settings.KEY_PRIVATE_STREAMING_CUSTOM_RESIZE_WIDTH , 1, 5000, 1 ) );
			sp.setPreferredSize( new Dimension( 80, sp.getPreferredSize().height ) );
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.customResizeHeight" ) ) );
			row.add( sp = GuiUtils.createSpinner( Settings.KEY_PRIVATE_STREAMING_CUSTOM_RESIZE_HEIGHT, 1, 5000, 1 ) );
			sp.setPreferredSize( new Dimension( 80, sp.getPreferredSize().height ) );
			for ( int i = 0; i < 4; i++ ) row.add( new JLabel() );
			outputVideoSizeComboBox.addActionListener( customResizeComponentsEnablerListener = new ActionListener() {
				{ actionPerformed( null ); } // Set initial enabled status
				@Override
				public void actionPerformed( final ActionEvent event ) {
					final OutputVideoSize outputVideoSize = OutputVideoSize.values()[ outputVideoSizeComboBox.getSelectedIndex() ];
					final boolean enable = outputVideoSize == OutputVideoSize.RESIZE_KEEP_ASPECT_RATIO || outputVideoSize == OutputVideoSize.RESIZE_DISTORTED;
					for ( int i = outputVideoSizeParamsBox.getComponentCount() - 1; i >= 2; i-- )
						outputVideoSizeParamsBox.getComponent( i ).setEnabled( enable );
				}
			} );
			paramsBox.add( row );
			row = Box.createHorizontalBox();
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.refreshRateFps" ) ) );
			row.add( sp = GuiUtils.createSpinner( Settings.KEY_PRIVATE_STREAMING_REFRESH_RATE_FPS, 1, 30, 1 ) );
			sp.setPreferredSize( new Dimension( 80, sp.getPreferredSize().height ) );
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.serverPort" ) ) );
			row.add( sp = GuiUtils.createSpinner( Settings.KEY_PRIVATE_STREAMING_SERVER_PORT, 1, 65535, 1 ) );
			sp.setPreferredSize( new Dimension( 80, sp.getPreferredSize().height ) );
			for ( int i = 0; i < 6; i++ ) row.add( new JLabel() );
			paramsBox.add( row );
			row = Box.createHorizontalBox();
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.imageQuality" ) ) );
			final JSlider qualitySlider = GuiUtils.createSlider( Settings.KEY_PRIVATE_STREAMING_IMAGE_QUALITY, 0, 100 );
			qualitySlider.setToolTipText( Language.getText( "privateVideoStreaming.params.imageQualityToolTip" ) );
			qualitySlider.setSnapToTicks( true );
			qualitySlider.setPaintLabels( true );
			qualitySlider.setPaintTicks ( true );
			qualitySlider.setMajorTickSpacing( 10 );
			qualitySlider.setMinorTickSpacing( 1 );
			row.add( qualitySlider );
			paramsBox.add( row );
			GuiUtils.alignBox( paramsBox, 10 );
			paramsBox.add( firstRow, 0 );
			GuiUtils.alignBox( paramsBox, 1 );
			paramsBox.add( Box.createVerticalStrut( 5 ) );
			paramsBox.add( new JSeparator() );
			paramsBox.add( Box.createVerticalStrut( 5 ) );
			row = Box.createHorizontalBox();
			row.add( GuiUtils.createCheckBox( "privateVideoStreaming.params.saveStreamVideoAsAviFolder", Settings.KEY_PRIVATE_STREAMING_SAVE_VIDEO_AS_AVI ) );
			final JTextField saveVideoFolderTextField = GuiUtils.createTextField( Settings.KEY_PRIVATE_STREAMING_SAVE_VIDEO_FOLDER );
			saveVideoFolderTextField.setColumns( 10 );
			row.add( saveVideoFolderTextField );
			final JButton chooseFolderButton = new JButton( Icons.FOLDERS );
			GuiUtils.updateButtonText( chooseFolderButton, "sc2gearsDatabaseDownloader.chooseFolderButton" );
			chooseFolderButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( final ActionEvent event ) {
					final JFileChooser fileChooser = new JFileChooser( saveVideoFolderTextField.getText() );
					fileChooser.setDialogTitle( Language.getText( "sc2gearsDatabaseDownloader.selectTargetFolder" ) );
					fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
					if ( fileChooser.showOpenDialog( StreamerFrame.this ) == JFileChooser.APPROVE_OPTION )
						saveVideoFolderTextField.setText( fileChooser.getSelectedFile().getAbsolutePath() );
				}
			} );
			row.add( chooseFolderButton );
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.fixAviFps" ) ) );
			row.add( sp = GuiUtils.createSpinner( Settings.KEY_PRIVATE_STREAMING_FIX_AVI_FPS, 1, 30, 1 ) );
			sp.setPreferredSize( new Dimension( 80, sp.getPreferredSize().height ) );
			row.add( new JLabel( Language.getText( "privateVideoStreaming.params.streamingFpsIsVarying" ) ) );
			paramsBox.add( row );
			updateState();
			getContentPane().add( GuiUtils.wrapInPanel( paramsBox ), BorderLayout.CENTER );
			
			// TODO we have average stats, how about current or "live" stats?
			// Current FPS, Current outgoing bandwidth
			final JPanel tablesPanel = new JPanel( new GridLayout( 1, 2 ) );
			tablesPanel.setBorder( BorderFactory.createTitledBorder( Language.getText( "privateVideoStreaming.stats.title" ) ) );
			statsTable.setShowVerticalLines( true );
			( (DefaultTableModel) statsTable.getModel() ).setDataVector(
				new Object[][] {
					{ Language.getText( "privateVideoStreaming.stats.streamingTime"          ), null },
					{ Language.getText( "privateVideoStreaming.stats.framesCaptured"         ), null },
					{ Language.getText( "privateVideoStreaming.stats.avgRefreshRate"         ), null },
					{ Language.getText( "privateVideoStreaming.stats.avgFrameSize"           ), null },
					{ Language.getText( "privateVideoStreaming.stats.avgVideoSize"           ), null },
					{ Language.getText( "privateVideoStreaming.stats.totalFrameSize"         ), null },
					{ Language.getText( "privateVideoStreaming.stats.framesTransmitted"      ), null },
					{ Language.getText( "privateVideoStreaming.stats.totalOutgoingBandwidth" ), null },
					{ Language.getText( "privateVideoStreaming.stats.savedAviFileSize"       ), null }
				}, new Object[] { "Property", "Value" } );
			tablesPanel.add( statsTable );
			final JScrollPane viewersScrollPane = new JScrollPane( viewersTable );
			viewersScrollPane.setPreferredSize( statsTable.getPreferredSize() );
			( (DefaultTableModel) viewersTable.getModel() ).setColumnIdentifiers( VIEWERS_HEADER_NAMES );
			tablesPanel.add( viewersScrollPane );
			getContentPane().add( tablesPanel, BorderLayout.SOUTH );
			
			
			SwingUtilities.invokeLater( new Runnable() {
				@Override
				public void run() {
					setVisible( true );
					pack();
					setLocationRelativeTo( null );
				}
			} );
		}
		
		protected void updateState() {
			final Streamer streamer = PrivateVideoStreaming.streamer;
			final boolean  startable = streamer == null || streamer.isCancelRequested();
			
			setIconImage( isStreaming() ? Icons.MONITOR_CAST.getImage() : Icons.MONITOR_MEDIUM.getImage() );
			
			GuiUtils.updateButtonText( startStopStreamingButton, startable ? "privateVideoStreaming.startStreamingButton" : "privateVideoStreaming.stopStreamingButton" );
			startStopStreamingButton.setIcon( startable ? Icons.MONITOR_CAST : Icons.MONITOR_MEDIUM );
			startStopStreamingButton.invalidate();
			
			boolean streamInProgress = false;
			if ( streamer == null ) {
				statusLabel.setForeground( streamerStartingError == null ? null : Color.RED );
				if ( streamerStartingError == null )
					statusLabel.setText( Language.getText( "privateVideoStreaming.noStreamingInProgress" ) );
				else
					statusLabel.setText( streamerStartingError );
			}
			else {
				statusLabel.setForeground( null );
				if ( startable )
					statusLabel.setText( Language.getText( "privateVideoStreaming.streamingStopped" ) );
				else {
					statusLabel.setText( Language.getText( "privateVideoStreaming.streamingInProgress" ) );
					streamInProgress = true;
				}
			}
			
			streamUrlTextField       .setText   ( streamInProgress ? streamer.hostHtmlUrl : "" );
			streamUrlTextField       .setEnabled( streamInProgress );
			copyUrlButton            .setEnabled( streamInProgress );
			openStreamInBrowserButton.setEnabled( streamInProgress );
			
			GuiUtils.setComponentTreeEnabled( paramsBox, startable );
			
			if ( startable ) {
				// Custom screen area components are not always enabled:
				customAreaComponentsEnablerListener  .actionPerformed( null );
				// Custom resize components are not always enabled:
				customResizeComponentsEnablerListener.actionPerformed( null );
			}
		}
		
		@SuppressWarnings( "unchecked" )
		protected void updateStats() {
			final Streamer streamer = PrivateVideoStreaming.streamer;
			if ( streamer == null || streamer.startTime == null )
				return;
			
			int row = 0;
			
			final long dtMs = ( streamer.endTime == null ? new Date() : streamer.endTime ).getTime() - streamer.startTime.getTime();
			final long dt = dtMs / 1000;
			statsTable.setValueAt( GeneralUtils.formatLongSeconds( dt ), row++, 1 );
			statsTable.setValueAt( String.format( Locale.ENGLISH, "%,d", streamer.framesCaptured ), row++, 1 );
			statsTable.setValueAt( Language.getText( "privateVideoStreaming.stats.fps", String.format( Locale.ENGLISH, "%.2f", dtMs == 0 ? 0.0 : 1000.0 * streamer.framesCaptured / dtMs ) ), row++, 1 );
			statsTable.setValueAt( getFormattedValue( "privateVideoStreaming.stats.sizeValue", streamer.framesCaptured == 0 ? 0l : streamer.totalFrameSize / streamer.framesCaptured ), row++, 1 );
			statsTable.setValueAt( getFormattedValue( "privateVideoStreaming.stats.speedValue", dtMs == 0 ? 0l : 1000L * streamer.totalFrameSize / dtMs ), row++, 1 );
			statsTable.setValueAt( getFormattedValue( "privateVideoStreaming.stats.sizeValue", streamer.totalFrameSize ), row++, 1 );
			statsTable.setValueAt( String.format( Locale.ENGLISH, "%,d", streamer.framesTransmitted.intValue() ), row++, 1 );
			statsTable.setValueAt( getFormattedValue( "privateVideoStreaming.stats.sizeValue", streamer.totalOutBandwidth.longValue() ), row++, 1 );
			statsTable.setValueAt( streamer.aviWriter == null ? "" : getFormattedValue( "privateVideoStreaming.stats.sizeValue", streamer.aviWriter.getAviFileSize() ), row++, 1 );
			
			final List< ViewerStats > viewerStatsList = new ArrayList< ViewerStats >( streamer.ipViewerStatsMap.values() );
			Collections.sort( viewerStatsList );
			boolean rowCountChanged = viewersTable.getRowCount() != viewerStatsList.size();
			final Vector< Object > dataVector;
			if ( rowCountChanged ) {
				// Rebuild table data
				dataVector = new Vector< Object >( viewerStatsList.size() );
				for ( int i = 0; i < viewerStatsList.size(); i++ ) {
					final Vector< Object > rowVector = new Vector< Object >( VIEWERS_HEADER_NAMES.length );
					rowVector.add( viewerStatsList.get( i ).ip );
					for ( int j = 1; j < VIEWERS_HEADER_NAMES.length; j++ )
						rowVector.add( "" );
					dataVector.add( rowVector );
				}
			}
			else 
				dataVector = ( (DefaultTableModel) viewersTable.getModel() ).getDataVector();
			
			for ( int i = 0; i < viewerStatsList.size(); i++ ) {
				final ViewerStats      viewerStats = viewerStatsList.get( i );
				final Vector< Object > rowVector   = (Vector< Object >) dataVector.get( i );
				// IP doesn't change...
				rowVector.set( 1, String.format( Locale.ENGLISH, "%,d", viewerStats.framesTransmitted ) );
				rowVector.set( 2, getFormattedValue( "privateVideoStreaming.stats.sizeValue", viewerStats.totalOutBandwidth ) );
				rowVector.set( 3, String.format( Locale.ENGLISH, "%,d", viewerStats.badPasswordAttempts ) );
			}
			
			if ( rowCountChanged )
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						( (DefaultTableModel) viewersTable.getModel() ).setDataVector( dataVector, new Vector< Object >( Arrays.asList( VIEWERS_HEADER_NAMES ) ) );
					}
				} );
			else
				viewersTable.repaint(); // Use repaint() instead of model.fireTableDataChanged(), because number of rows did not change, and repaint() keeps the selection
		}
		
		private String getFormattedValue( final String textKey, final long size ) {
			if ( size < 1000 )
				return Language.getText( textKey, String.format( Locale.ENGLISH, "%d", size ) );
			float sizef = size / 1024f;
			if ( sizef < 1000f )
				return Language.getText( textKey + "KB", String.format( Locale.ENGLISH, "%.2f", sizef ) );
			sizef = sizef / 1024f;
			if ( sizef < 1024f )
				return Language.getText( textKey + "MB", String.format( Locale.ENGLISH, "%.2f", sizef ) );
			sizef = sizef / 1024f;
			return Language.getText( textKey + "GB", String.format( Locale.ENGLISH, "%,.2f", sizef ) );
		}
	}
	
	/** Reference to the streamer.                    */
	private static volatile Streamer      streamer;
	/** Error message if starting the streamer fails. */
	private static volatile String        streamerStartingError;
	/** Reference to the streamer frame.              */
	private static volatile StreamerFrame streamerFrame;
	
	/**
	 * Tells if streaming is in progress.
	 * @return true if streaming is in progress; false otherwise
	 */
	public static synchronized boolean isStreaming() {
		return streamer != null && !streamer.isCancelRequested();
	}
	
	/**
	 * Tells if a streaming is present.
	 * <p>Returns true if streaming is present either if it is still in progress or if it is stopped.</p> 
	 * @return true if a streaming is present; false otherwise
	 */
	public static synchronized boolean isStreamingPresent() {
		return streamer != null;
	}
	
	/**
	 * Starts streaming.
	 * @return <code>null</code> if streaming is already in progress, <code>Boolean.TRUE</code> if streaming was started successfully, <code>Boolean.FALSE</code> otherwise 
	 */
	public static synchronized Boolean startStreaming() {
		if ( streamer != null && !streamer.isCancelRequested() )
			return null;
		
		System.out.println( Language.formatDateTime( new Date() ) + " - Starting private stream..." );
		
		try {
			streamer = null;
			streamer = new Streamer();
			streamer.start();
			MainFrame.INSTANCE.setPrivateStreamingStatus( true );
			return Boolean.TRUE;
		} catch ( final Exception e ) {
			e.printStackTrace();
			if ( e instanceof MessageException )
				streamerStartingError = e.getMessage();
			else
				streamerStartingError = Language.getText( "privateVideoStreaming.errors.general" );
			return Boolean.FALSE;
		} finally {
			updateStreamerFrame();
		}
	}
	
	/**
	 * Stops streaming.
	 */
	public static synchronized void stopStreaming() {
		if ( streamer == null || streamer.isCancelRequested() )
			return;
		
		System.out.println( Language.formatDateTime( new Date() ) + " - Stopping private stream." );
		
		streamer.shutdown();
		MainFrame.INSTANCE.setPrivateStreamingStatus( false );
		updateStreamerFrame();
	}
	
	/**
	 * Updates the streamer frame to reflect the state of the streamer.
	 */
	private static void updateStreamerFrame() {
		final StreamerFrame streamerFrame = PrivateVideoStreaming.streamerFrame;
		if ( streamerFrame != null ) {
			streamerFrame.updateState();
			streamerFrame.getContentPane().validate();
			streamerFrame.getContentPane().repaint();
			streamerFrame.updateStats();
		}
	}
	
	/**
	 * Displays the streamer frame which can be used to manually start/stop the streaming and to display state and statistics.
	 */
	public static synchronized void showFrame() {
		final StreamerFrame streamerFrame = PrivateVideoStreaming.streamerFrame;
		
		if ( streamerFrame == null ) {
			PrivateVideoStreaming.streamerFrame = new StreamerFrame();
			PrivateVideoStreaming.streamerFrame.updateStats();
		}
		else {
			if ( streamerFrame.getExtendedState() == JFrame.ICONIFIED )
				streamerFrame.setExtendedState( JFrame.NORMAL );
			streamerFrame.toFront();
		}
	}
	
	/**
	 * Returns a reference to the streamer frame.
	 * @return a reference to the streamer frame
	 */
	public static synchronized JFrame getStreamerFrame() {
		return streamerFrame;
	}
	
}
