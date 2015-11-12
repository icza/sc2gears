/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.dialogs;

import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.HEADER_X_FILE_DATE;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.HEADER_X_FILE_NAME;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.OPERATION_DOWNLOAD;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.OPERATION_RETRIEVE_FILE_LIST;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_AFTER_DATE;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_BEFORE_DATE;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_FILE_TYPE;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PARAM_SHA1;
import static hu.belicza.andras.sc2gearsdbapi.FileServletApi.PROTOCOL_VERSION_1;
import static hu.belicza.andras.sc2gearsdbapi.ServletApi.PARAM_AUTHORIZATION_KEY;
import static hu.belicza.andras.sc2gearsdbapi.ServletApi.PARAM_OPERATION;
import static hu.belicza.andras.sc2gearsdbapi.ServletApi.PARAM_PROTOCOL_VERSION;
import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.components.BaseLabelListCellRenderer;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.HttpPost;
import hu.belicza.andras.sc2gears.util.NormalThread;
import hu.belicza.andras.sc2gearsdbapi.FileServletApi.FileType;
import hu.belicza.andras.sc2gearspluginapi.api.httpost.FileProvider;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Tool to download multiple files from the Sc2gears Database.
 *
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class Sc2gearsDatabaseDownloaderDialog extends BaseDialog {
	
	/** Number of max retries if an Sc2gears Database download fails. */
	private static final int MAX_SC2GEARS_DATABASE_DOWNLOAD_RETRIES = 3;
	
	/** Tells if the "Cancel" button has been pressed. */
	private volatile boolean cancelPressed;
	
	/**
	 * Creates a new DiagnosticTool.
	 */
	public Sc2gearsDatabaseDownloaderDialog() {
		super( "sc2gearsDatabaseDownloader.title", Icons.SERVER_NETWORK );
		
		final Box box = Box.createVerticalBox();
		box.setBorder( BorderFactory.createEmptyBorder( 15, 15, 10, 15 ) );
		
		Box row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "sc2gearsDatabaseDownloader.selectFileType" ) ) );
		final JComboBox< FileType > fileTypeComboBox = GuiUtils.createComboBox( new Vector< FileType >( Arrays.asList( FileType.SC2REPLAY, FileType.MOUSE_PRINT, FileType.OTHER ) ), Settings.KEY_SC2GEARS_DATABASE_DOWNLOADER_FILE_TYPE, false );
		fileTypeComboBox.setMaximumRowCount( fileTypeComboBox.getModel().getSize() ); // Display all file types
		fileTypeComboBox.setRenderer( new BaseLabelListCellRenderer< FileType >( 2, fileTypeComboBox ) {
			@Override
			public Icon getIcon( final FileType value ) {
				switch ( value ) {
				case SC2REPLAY   : return Icons.SC2;
				case MOUSE_PRINT : return Icons.FINGERPRINT;
				case OTHER       : return Icons.DOCUMENT;
				default          : return null;
				}
			}
		} );
		row.add( fileTypeComboBox );
		row.add( new JLabel() );
		box.add( row );
		
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "sc2gearsDatabaseDownloader.onlyAfterDate" ) ) );
		final GregorianCalendar calendar = new GregorianCalendar();
		calendar.add( Calendar.MONTH, -1 );
		final JTextField afterDateTextField = new JTextField( Language.formatDate( calendar.getTime() ), 30 );
		row.add( afterDateTextField );
		row.add( new JLabel() );
		box.add( row );
		
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "sc2gearsDatabaseDownloader.onlyBeforeDate" ) ) );
		final JTextField beforeDateTextField = new JTextField( Language.formatDate( new Date() ), 30 );
		row.add( beforeDateTextField );
		row.add( new JLabel() );
		box.add( row );
		
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "sc2gearsDatabaseDownloader.targetFolder" ) ) );
		final JTextField targetFolderTextField = new JTextField( Settings.getString( Settings.KEY_SC2GEARS_DATABASE_DOWNLOADER_TARGET_FOLDER ), 30 );
		row.add( targetFolderTextField );
		final JButton chooseFolderButton = new JButton( Icons.FOLDERS );
		GuiUtils.updateButtonText( chooseFolderButton, "sc2gearsDatabaseDownloader.chooseFolderButton" );
		chooseFolderButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( targetFolderTextField.getText() );
				fileChooser.setDialogTitle( Language.getText( "sc2gearsDatabaseDownloader.selectTargetFolder" ) );
				fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
				if ( fileChooser.showOpenDialog( Sc2gearsDatabaseDownloaderDialog.this ) == JFileChooser.APPROVE_OPTION )
					targetFolderTextField.setText( fileChooser.getSelectedFile().getAbsolutePath() );
			}
		} );
		row.add( chooseFolderButton );
		box.add( row );
		
		GuiUtils.alignBox( box, 3 );
		getContentPane().add( box, BorderLayout.CENTER );
		
		final Box southBox = Box.createVerticalBox();
		southBox.setBorder( BorderFactory.createEmptyBorder( 0, 15, 10, 15 ) );
		final JLabel statusLabel = new JLabel( " " );
		final JPanel buttonsPanel = new JPanel();
		final JButton downloadFilesButton = new JButton( Icons.DRIVE_DOWNLOAD );
		GuiUtils.updateButtonText( downloadFilesButton, "sc2gearsDatabaseDownloader.downloadFilesButton" );
		downloadFilesButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				String dateString = afterDateTextField.getText();
				Date date = Language.parseDateTime( dateString, true );
				if ( date == null )
					date = Language.parseDate( dateString );
				if ( date == null ) {
					GuiUtils.showErrorDialog( Language.getText( "sc2gearsDatabaseDownloader.invalidDate" ) );
					afterDateTextField.requestFocusInWindow();
					return;
				}
				final Date afterDate = date;
				
				dateString = beforeDateTextField.getText();
				date = Language.parseDateTime( dateString, true );
				if ( date == null )
					date = Language.parseDate( dateString );
				if ( date == null ) {
					GuiUtils.showErrorDialog( Language.getText( "sc2gearsDatabaseDownloader.invalidDate" ) );
					beforeDateTextField.requestFocusInWindow();
					return;
				}
				final Date beforeDate = date;
				
				if ( targetFolderTextField.getText().length() == 0 ) {
					GuiUtils.showErrorDialog( Language.getText( "sc2gearsDatabaseDownloader.missingTargetFolder" ) );
					targetFolderTextField.requestFocusInWindow();
					return;
				}
				Settings.set( Settings.KEY_SC2GEARS_DATABASE_DOWNLOADER_TARGET_FOLDER, targetFolderTextField.getText() );
				
				final String authorizationKey = GeneralUtils.checkKeyBeforeStoringOrDownloading();
				if ( authorizationKey == null )
					return;
				
				final File targetFolder = new File( targetFolderTextField.getText() );
				if ( !targetFolder.exists() )
					if ( !targetFolder.mkdirs() ) {
						GuiUtils.showErrorDialog( Language.getText( "sc2gearsDatabaseDownloader.failedToCreateTargetFolder" ) );
						targetFolderTextField.requestFocusInWindow();
						return;
					}
				
				downloadFilesButton.setEnabled( false );
				GuiUtils.setComponentTreeEnabled( box, false );
				statusLabel.setText( Language.getText( "sc2gearsDatabaseDownloader.retrievingFileList" ) );
				new NormalThread( "Sc2gears Database file downloader" ) {
					final FileType     fileType     = (FileType) fileTypeComboBox.getSelectedItem();
					final FileProvider fileProvider = new FileProvider() {
						@Override
						public File getFile( final HttpURLConnection httpUrlConnection ) {
							String fileName = httpUrlConnection.getHeaderField( HEADER_X_FILE_NAME );
							if ( fileName == null )
								fileName = "new file." + fileType.longName;
							try {
								fileName = URLDecoder.decode( fileName, "UTF-8" );
							} catch ( final UnsupportedEncodingException e ) {
								// Never to happen
								e.printStackTrace();
							}
							return GeneralUtils.generateUniqueName( new File( targetFolder, fileName ) );
						}

						@Override
						public Long getLastModified( final HttpURLConnection httpUrlConnection ) {
							try {
								final String fileDateString = httpUrlConnection.getHeaderField( HEADER_X_FILE_DATE );
								return fileDateString == null ? null : Long.valueOf( fileDateString );
							} catch ( final Exception e ) {
								return null;
							}
						}
					};
					private Map< String, String > paramsMap;
					@Override
					public void run() {
						HttpPost httpPost = null;
						try {
							// I use POST so the authorization key will not appear in the URL, because in case of errors the URL will appear in the log
							paramsMap = new HashMap< String, String >();
							paramsMap.put( PARAM_PROTOCOL_VERSION , PROTOCOL_VERSION_1                    );
							paramsMap.put( PARAM_AUTHORIZATION_KEY, authorizationKey                      );
							paramsMap.put( PARAM_OPERATION        , OPERATION_RETRIEVE_FILE_LIST          );
							paramsMap.put( PARAM_FILE_TYPE        , fileType.longName                     );
							paramsMap.put( PARAM_AFTER_DATE       , Long.toString( afterDate .getTime() ) );
							paramsMap.put( PARAM_BEFORE_DATE      , Long.toString( beforeDate.getTime() ) );
							
							httpPost = new HttpPost( Consts.URL_SC2GEARS_DATABASE_FILE_SERVLET, paramsMap );
							if ( httpPost.connect() )
								if ( httpPost.doPost() ) {
									final List< String > fileInfoList = httpPost.getResponseLines();
									httpPost.close();
									httpPost = null;
									
									if ( fileInfoList == null )
										throw new Exception( "Failed to retrieve file list!" );
									if ( cancelPressed )
										return;
									
									dispose();
									
									paramsMap = new HashMap< String, String >();
									// Same parameters for all file requests
									paramsMap.put( PARAM_PROTOCOL_VERSION , PROTOCOL_VERSION_1 );
									paramsMap.put( PARAM_AUTHORIZATION_KEY, authorizationKey   );
									paramsMap.put( PARAM_OPERATION        , OPERATION_DOWNLOAD );
									paramsMap.put( PARAM_FILE_TYPE        , fileType.longName  );
									
									final ProgressDialog progressDialog = new ProgressDialog( "sc2gearsDatabaseDownloader.downloadingFiles", Icons.SERVER_NETWORK, fileInfoList.size() );
									final byte[]         buffer         = new byte[ 200*1024 ]; // Enough for most replays
									for ( final String fileInfo : fileInfoList ) {
										if ( progressDialog.isAborted() )
											break;
										
										if ( !downloadFile( fileInfo, buffer ) )
											progressDialog.incrementFailed();
										
										progressDialog.incrementProcessed();
										progressDialog.updateProgressBar();
									}
									
									progressDialog.taskFinished();
									
									return;
								}
							
						} catch ( final Exception e ) {
							e.printStackTrace();
						} finally {
							if ( httpPost != null )
								httpPost.close();
						}
						GuiUtils.showErrorDialog( Language.getText( "sc2gearsDatabaseDownloader.failedToRetrieveFileList" ) );
						statusLabel.setText( " " );
						GuiUtils.setComponentTreeEnabled( box, true );
						downloadFilesButton.setEnabled( true );
					}
					private boolean downloadFile( final String sha1, final byte[] buffer ) {
						paramsMap.put( PARAM_SHA1, sha1 );
						
						for ( int retry = 0; retry < MAX_SC2GEARS_DATABASE_DOWNLOAD_RETRIES; retry++ ) {
							if ( retry > 0 )
								System.out.println( "Retrying file download... (" + (retry+1) + ")" );
						
							HttpPost httpPost = null;
							try {
								// I use POST so the authorization key will not appear in the URL, because in case of errors the URL will appear in the log
								httpPost = new HttpPost( Consts.URL_SC2GEARS_DATABASE_FILE_SERVLET, paramsMap );
								if ( httpPost.connect() )
									if ( httpPost.doPost() )
										if ( httpPost.saveAttachmentToFile( fileProvider, buffer ) )
											return true;
								
							} catch ( final Exception e ) {
								e.printStackTrace();
							} finally {
								if ( httpPost != null )
									httpPost.close();
							}
							
							System.out.println( "Failed to download a file from the Sc2gears Database!" );
						}
						
						return false;
					}
				}.start();
			}
		} );
		buttonsPanel.add( downloadFilesButton );
		final JButton cancelButton = createCloseButton( "button.cancel" );
		cancelButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				cancelPressed = true;
			}
		} );
		buttonsPanel.add( cancelButton );
		southBox.add( buttonsPanel );
		southBox.add( GuiUtils.wrapInPanel( statusLabel ) );
		getContentPane().add( southBox, BorderLayout.SOUTH );
		
		// "Pack" and show
		packAndShow( fileTypeComboBox, false );
	}
	
}
