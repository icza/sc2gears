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

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.components.BaseLabelListCellRenderer;
import hu.belicza.andras.sc2gears.ui.components.CustomComboBoxModel;
import hu.belicza.andras.sc2gears.ui.components.ReplayInfoBox;
import hu.belicza.andras.sc2gears.ui.dialogs.MiscSettingsDialog.SettingsTab;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.Base64;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gears.util.HttpPost;
import hu.belicza.andras.sc2gears.util.NormalThread;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Share replays dialog. Offers the user to upload a replay to different sites.
 * 
 * <p>Replay upload specification can be found <a href="http://sites.google.com/site/sc2gears/features/replay-sharing">here</a>.</p>
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class ShareReplaysDialog extends BaseDialog {
	
	/** Value of the request version parameter. */
	private static final String PARAM_VALUE_REQUEST_VERSION = "1.0";
	
	/** Name of the request version parameter. */
	private static final String PARAM_NAME_REQUEST_VERSION = "requestVersion";
	/** Name of the user name parameter.       */
	private static final String PARAM_NAME_USER_NAME       = "userName";
	/** Name of the password parameter.        */
	private static final String PARAM_NAME_PASSWORD        = "password";
	/** Name of the description parameter.     */
	private static final String PARAM_NAME_DESCRIPTION     = "description";
	/** Name of the file name parameter.       */
	private static final String PARAM_NAME_FILE_NAME       = "fileName";
	/** Name of the file size parameter.       */
	private static final String PARAM_NAME_FILE_SIZE       = "fileSize";
	/** Name of the file MD5 parameter.        */
	private static final String PARAM_NAME_FILE_MD5        = "fileMd5";
	/** Name of the file content parameter.    */
	private static final String PARAM_NAME_FILE_CONTENT    = "fileContent";
	
	/** Null icon for custom replay upload sites. */
	private static final Icon NULL_ICON;
	
	/** Static replay upload site list. */
	public static final List< ReplayUploadSite > STATIC_REPLAY_UPLOAD_SITE_LIST = new ArrayList< ReplayUploadSite >();
	static {
		STATIC_REPLAY_UPLOAD_SITE_LIST.add( new ReplayUploadSite( "drop.sc"           , "http://drop.sc/"               , "http://drop.sc/api/upload?source=sc2gears"                                          , Icons.getReplaySiteIcon( "DROP_SC"            ) ) );
		STATIC_REPLAY_UPLOAD_SITE_LIST.add( new ReplayUploadSite( "ReplayFu.com"      , "http://replayfu.com/"          , "http://replayfu.com/api/upload?key=49b752508fcccf95af720b162dfba9a0&requestType=xml", Icons.getReplaySiteIcon( "REPLAYFU_COM"       ) ) );
		STATIC_REPLAY_UPLOAD_SITE_LIST.add( new ReplayUploadSite( "sc2bc.com"         , "http://sc2bc.com/"             , "http://sc2bc.com/upload/sc2gears"                                                   , Icons.getReplaySiteIcon( "SC2BC_COM"          ) ) );
		STATIC_REPLAY_UPLOAD_SITE_LIST.add( new ReplayUploadSite( "SCTemple"          , "http://www.sctemple.com/"      , "http://www.sctemple.com/sc2gears/"                                                  , Icons.getReplaySiteIcon( "SCTEMPLE_COM"       ) ) );
		STATIC_REPLAY_UPLOAD_SITE_LIST.add( new ReplayUploadSite( "TopReplays.com"    , "http://topreplays.com/"        , "http://topreplays.com/Replays/InsertFromSc2Gears"                                   , Icons.getReplaySiteIcon( "TOPREPLAYS_COM"     ) ) );
		STATIC_REPLAY_UPLOAD_SITE_LIST.add( new ReplayUploadSite( "StatCraft"         , "http://www.statcraft.net/"     , "http://www.statcraft.net/batch/upload"                                              , Icons.getReplaySiteIcon( "STATCRAFT_NET"      ) ) );
		STATIC_REPLAY_UPLOAD_SITE_LIST.add( new ReplayUploadSite( "StarcraftTools.net", "http://www.starcrafttools.net/", "http://www.starcrafttools.net/sc2gears_up.php"                                      , Icons.getReplaySiteIcon( "STARCRAFTTOOLS_NET" ) ) );
		STATIC_REPLAY_UPLOAD_SITE_LIST.add( new ReplayUploadSite( "ggtracker"         , "http://ggtracker.com/"         , "http://ggtracker.com/api/upload"                                                    , Icons.getReplaySiteIcon( "GGTRACKER_COM"      ) ) );
		
		NULL_ICON = Icons.getNullIcon( STATIC_REPLAY_UPLOAD_SITE_LIST.get( 0 ).icon.getIconWidth(), STATIC_REPLAY_UPLOAD_SITE_LIST.get( 0 ).icon.getIconWidth() );
	}
	
	/**
	 * Replay upload site.
	 * @author Andras Belicza
	 */
	public static class ReplayUploadSite {
		
		/** Name of the replay upload site to display. */
		public String displayName;
		/** Home page of the replay upload site.       */
		public String homePage;
		/** URL to post the replay to.                 */
		public String uploadUrl;
		
		/** Icon of the replay site. */
		private final Icon icon;
		
		/**
		 * Creates a new ReplayUploadSite.
		 * @param displayName name of the replay upload site to display
		 * @param homePage    home page of the replay upload site
		 * @param uploadUrl   URL to post the replay to
		 */
		private ReplayUploadSite( final String displayName, final String homePage, final String uploadUrl, final Icon icon ) {
			this.displayName = displayName;
			this.homePage    = homePage;
			this.uploadUrl   = uploadUrl;
			this.icon        = icon;
		}
		
		/**
		 * Creates a new ReplayUploadSite.
		 */
		public ReplayUploadSite() {
			icon = null;
		}
		
		@Override
		public String toString() {
			return displayName;
		}
		
	}
	
	/**
	 * Creates a new ShareReplayDialog.
	 * @param replayFiles_ optional replay files to be shared; if missing (null), a file chooser dialog will be displayed to select replays
	 */
	public ShareReplaysDialog( File[] replayFiles_ ) {
		super( "shareReplay.title", Icons.DOCUMENT_SHARE );
		setModal( false );
		
		if ( replayFiles_ == null ) {
			final JFileChooser fileChooser = new JFileChooser( GeneralUtils.getDefaultReplayFolder() );
			fileChooser.setDialogTitle( Language.getText( "shareReplay.openTitle" ) );
			fileChooser.setFileFilter( GuiUtils.SC2_REPLAY_FILTER );
			fileChooser.setMultiSelectionEnabled( true );
			fileChooser.setAccessory( GuiUtils.createReplayFilePreviewAccessory( fileChooser ) );
			fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
			if ( fileChooser.showOpenDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION )
				replayFiles_ = fileChooser.getSelectedFiles();
			else {
				dispose();
				return;
			}
		}
		
		final File[] replayFiles = replayFiles_;
		
		final Box box = Box.createVerticalBox();
		box.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		JLabel infoLabel = new JLabel( Language.getText( "shareReplay.info1", Consts.APPLICATION_NAME ) );
		GuiUtils.changeFontToBold( infoLabel );
		box.add( GuiUtils.wrapInPanelLeftAligned( infoLabel ) );
		infoLabel = new JLabel( Language.getText( "shareReplay.info2" ) ) ;
		GuiUtils.changeFontToBold( infoLabel );
		JPanel wrapper = GuiUtils.wrapInPanelLeftAligned( infoLabel );
		box.add( wrapper );
		
		final JLabel infoLinkLabel = GeneralUtils.createLinkLabel( Language.getText( "shareReplay.wantYourSiteListed" ), Consts.URL_REPLAY_SHARING );
		wrapper = GuiUtils.createRightAlignedInfoWrapperPanel( infoLinkLabel, 5 );
		box.add( wrapper );
		box.add( new JSeparator() );
		
		box.add( Box.createVerticalStrut( 10 ) );
		
		Box row = Box.createHorizontalBox();
		
		row.add( new JLabel( Language.getText( "shareReplay.replayToShare" ) ) );
		final JTextField replayPathTextField = new JTextField( replayFiles[ 0 ].getAbsolutePath(), 35 );
		replayPathTextField.setCaretPosition( 0 );
		replayPathTextField.setEditable( false );
		row.add( replayPathTextField );
		box.add( row );
		
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "shareReplay.replayNameToReport" ) ) );
		final JTextField replayNameToReportTextField = new JTextField( replayFiles[ 0 ].getName(), 35 );
		replayNameToReportTextField.setCaretPosition( 0 );
		row.add( replayNameToReportTextField );
		box.add( row );
		
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "shareReplay.replayDescription" ) ) );
		final JTextArea replayDescriptionTextArea = new JTextArea();
		replayDescriptionTextArea.setRows( 7 );
		row.add( new JScrollPane( replayDescriptionTextArea ) );
		box.add( row );
		
		final JLabel  visitReplaySiteLinkLabel      = GeneralUtils.createLinkStyledLabel( Language.getText( "shareReplay.visitThisReplaySite" ) );
		visitReplaySiteLinkLabel.setIcon( Icons.APPLICATION_BROWSER );
		final String  visitReplaySiteLinkActiveText = visitReplaySiteLinkLabel.getText(); // HTML text 
		final JButton uploadButton                  = new JButton( Icons.DRIVE_UPLOAD );
		final JButton uploadAllButton               = new JButton( Icons.DRIVE_UPLOAD );
		
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "shareReplay.chooseReplaySite" ) ) );
		final Vector< Object > replayUpoadSiteVector = new Vector< Object >();
		constructReplayUploadSiteVector( replayUpoadSiteVector );
		final JComboBox< Object > replaySiteComboBox = GuiUtils.createComboBox( replayUpoadSiteVector, Settings.KEY_SHARE_REP_REPLAY_SITE, false );
		replaySiteComboBox.setMaximumRowCount( 15 );
		GuiUtils.addEditListLinkToComboBoxPopup( replaySiteComboBox, new Runnable() {
			@Override
			public void run() {
				new MiscSettingsDialog( SettingsTab.CUSTOM_REPLAY_SITES );
				replayUpoadSiteVector.clear();
				// Refresh the list in the combo box. The vector reference is shared, so the following is enough:
				constructReplayUploadSiteVector( replayUpoadSiteVector );
				if ( replaySiteComboBox.getModel() instanceof CustomComboBoxModel )
					( (CustomComboBoxModel< Object >) replaySiteComboBox.getModel() ).fireContentsChanged( replaySiteComboBox );
				// TODO: revise!
				// The misc settings dialog constructs new custom replay site objects, so the getSelectedIndex() will return -1
				// if a custom one was selected before the edit.
				// One possible solution/workaround: always set the 0th element after edit...
				replaySiteComboBox.setSelectedIndex( 0 );
			}
		} );
		replaySiteComboBox.setRenderer( new BaseLabelListCellRenderer< Object >() {
			@Override
			public Icon getIcon( final Object value ) {
				final Icon icon = value instanceof ReplayUploadSite ? ( (ReplayUploadSite) value ).icon : null;
				return icon == null ? NULL_ICON : icon;
			}
		} );
		final ActionListener uploadButtonEnabledListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final boolean replaySiteIsSelected = replaySiteComboBox.getSelectedIndex() != 0;
				uploadButton   .setEnabled( replaySiteIsSelected );
				uploadAllButton.setEnabled( replaySiteIsSelected && replayFiles.length > 1 );
				visitReplaySiteLinkLabel.setText( replaySiteIsSelected ? visitReplaySiteLinkActiveText : Language.getText( "shareReplay.visitThisReplaySite" ) );
				visitReplaySiteLinkLabel.setEnabled( replaySiteIsSelected );
				visitReplaySiteLinkLabel.setToolTipText( replaySiteIsSelected ? ( (ReplayUploadSite) replaySiteComboBox.getSelectedItem() ).homePage : null );
			}
		};
		uploadButtonEnabledListener.actionPerformed( null );
		replaySiteComboBox.addActionListener( uploadButtonEnabledListener );
		row.add( replaySiteComboBox );
		box.add( row );
		
		visitReplaySiteLinkLabel.addMouseListener( new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				if ( replaySiteComboBox.getSelectedIndex() != 0 ) {
					final ReplayUploadSite replayUploadSite = (ReplayUploadSite) replaySiteComboBox.getSelectedItem();
					GeneralUtils.showURLInBrowser( replayUploadSite.homePage );
				}
			};
		} );
		box.add( GuiUtils.createRightAlignedInfoWrapperPanel( visitReplaySiteLinkLabel, 0 ) );
		
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "shareReplay.userName" ) ) );
		final JComboBox< String > userNameComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.REP_SHARE_USER_NAME );
		userNameComboBox.setSelectedItem( Settings.getString( Settings.KEY_SHARE_REP_USER_NAME ) );
		row.add( userNameComboBox );
		box.add( row );
		
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "shareReplay.password" ) ) );
		final JPasswordField passwordField = new JPasswordField();
		row.add( passwordField );
		box.add( row );
		
		box.add( Box.createVerticalStrut( 10 ) );
		infoLabel = new JLabel( Language.getText( "shareReplay.info3", Consts.APPLICATION_NAME ) );
		GuiUtils.changeFontToItalic( infoLabel );
		box.add( GuiUtils.wrapInPanelLeftAligned( infoLabel ) );
		infoLabel = new JLabel( Language.getText( "shareReplay.info4" ) ) ;
		GuiUtils.changeFontToItalic( infoLabel );
		box.add( GuiUtils.wrapInPanelLeftAligned( infoLabel ) );
		
		GuiUtils.alignBox( box, 2 );
		
		getContentPane().add( new JScrollPane( GuiUtils.wrapInPanel( box ) ), BorderLayout.CENTER );
		
		final ReplayInfoBox replayInfoBox = new ReplayInfoBox( replayFiles[ 0 ] );
		replayInfoBox.setBorder( BorderFactory.createEmptyBorder( 10, 0, 0, 10 ) );
		getContentPane().add( replayInfoBox, BorderLayout.EAST );
		
		final IntHolder replayIndexHolder = new IntHolder();
		
		final JSlider progressSlider = replayFiles.length > 1 ? new JSlider( 1, replayFiles.length, 1 ) : null;
		if ( progressSlider != null ) {
			progressSlider.setSnapToTicks( true );
			progressSlider.setPaintLabels( true );
			progressSlider.setPaintTicks ( true );
			progressSlider.setMajorTickSpacing( ( replayFiles.length - 1 ) / ( replayFiles.length < 1000 ? 40 : 25 ) + 1 );
			progressSlider.setMinorTickSpacing( 1 );
		}
		
		// Updater task for cases when sharing multiple replays
		final Runnable updateRepInfoAndControlsTask = new Runnable() {
			@Override
            public void run() {
				// Update fields with the new replay
				replayPathTextField        .setText( replayFiles[ replayIndexHolder.value ].getAbsolutePath() );
				replayPathTextField        .setCaretPosition( 0 );
				replayNameToReportTextField.setText( replayFiles[ replayIndexHolder.value ].getName        () );
				replayNameToReportTextField.setCaretPosition( 0 );
				replayInfoBox              .setReplayFile( replayFiles[ replayIndexHolder.value ]             );
				GuiUtils.updateButtonText( uploadButton   , "shareReplay.uploadButton2"           , replayIndexHolder.value + 1                  );
				GuiUtils.updateButtonText( uploadAllButton, "shareReplay.uploadAllRemainingButton", replayFiles.length - replayIndexHolder.value );
				progressSlider.setValue( replayIndexHolder.value+1 );
            }
		};
		
		if ( progressSlider != null )
			progressSlider.addChangeListener( new ChangeListener() {
				@Override
				public void stateChanged( final ChangeEvent event ) {
					replayIndexHolder.value = progressSlider.getValue() - 1;
					updateRepInfoAndControlsTask.run();
				}
			} );
		
		final JPanel southPanel = new JPanel( new BorderLayout() );
		
		final JPanel buttonsAndProgressSliderPanel = new JPanel( new BorderLayout() );
		buttonsAndProgressSliderPanel.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 10 ) );
		
		final JPanel buttonsPanel = new JPanel();
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		final JButton         closeButton      = createCloseButton( "button.close" );
		final JTextArea       progressTextArea = new JTextArea();
		final ReplayLinkPanel replayLinkPanel  = new ReplayLinkPanel();
		
		if ( replayFiles.length == 1 )
			GuiUtils.updateButtonText( uploadButton, "shareReplay.uploadButton" );
		else
			GuiUtils.updateButtonText( uploadButton, "shareReplay.uploadButton2", replayIndexHolder.value + 1 );
		GuiUtils.updateButtonText( uploadAllButton, "shareReplay.uploadAllRemainingButton", replayFiles.length );
		final ActionListener uploadActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final String userName = userNameComboBox.getSelectedItem().toString();
				Settings.set( Settings.KEY_SHARE_REP_USER_NAME, userName );
				
				ShareReplaysDialog.this.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
				final JComponent[] componentList = new JComponent[] { replayNameToReportTextField, replayDescriptionTextArea, replaySiteComboBox, userNameComboBox, passwordField, uploadButton, uploadAllButton, closeButton, progressSlider };
				for ( final JComponent component : componentList )
					if ( component != null )
						component.setEnabled( false );
				
				final ReplayUploadSite replayUploadSite = (ReplayUploadSite) replaySiteComboBox.getSelectedItem();
				
				final String reportedName = replayNameToReportTextField.getText();
				final String description  = replayDescriptionTextArea.getText();
				final String password     = new String( passwordField.getPassword() ); // Password should be handled differently (with only a char array...)
				
				new NormalThread( "Replay uploader" ) {
					private ProgressDialog progressDialog;
					@Override
					public void run() {
						int repsToUpload = event.getSource() == uploadButton ? 1 : replayFiles.length - replayIndexHolder.value;
						final int repsToUpload_ = repsToUpload;
						progressDialog = repsToUpload > 1 ? new ProgressDialog( ShareReplaysDialog.this, new Holder< String >( Language.getText( "shareReplay.uploadingReplays" ) ), Icons.DRIVE_UPLOAD, repsToUpload ) : null;
						
						if ( repsToUpload_ > 1 ) {
    						GuiUtils.appendNewLine( progressTextArea, "" ); // An empty line
    						GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.batchUploading", repsToUpload_ ) );
						}
						
						try {
    						for ( ; repsToUpload > 0; repsToUpload-- ) {
    							if ( progressDialog != null && progressDialog.isAborted() ) {
            						GuiUtils.appendNewLine( progressTextArea, "" ); // An empty line
    								GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.batchUploadingAborted" ) );
    								break;
    							}
    							
    							final File replayFile = replayFiles[ replayIndexHolder.value ];
    							
        						GuiUtils.appendNewLine( progressTextArea, "" ); // An empty line
        						GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.preparingForUpload", replayUploadSite.displayName ) );
        						
        						HttpPost httpPost = null;
        						try {
        							final String fileMd5    = GeneralUtils.calculateFileMd5( replayFile );
        							if ( fileMd5 == null || fileMd5.length() == 0 ) {
        								System.err.println( "MD5 could not be calculated (" + replayFile + ")!" );
        								GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.preparationFailed" ) );
                						proceedToNextReplay( true );
                						continue;
        							}
        							final String fileBase64 = Base64.encodeFile( replayFile );
        							if ( fileBase64 == null ) {
        								System.err.println( "Base64 encoding could not be performed (" + replayFile + ")!" );
        								GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.preparationFailed" ) );
                						proceedToNextReplay( true );
                						continue;
        							}
        							
        							final Map< String, String > paramsMap = new HashMap< String, String >();
        							paramsMap.put( PARAM_NAME_REQUEST_VERSION, PARAM_VALUE_REQUEST_VERSION );
        							paramsMap.put( PARAM_NAME_FILE_NAME      , reportedName );
        							paramsMap.put( PARAM_NAME_FILE_SIZE      , Long.toString( replayFile.length() ) );
        							paramsMap.put( PARAM_NAME_DESCRIPTION    , description );
        							paramsMap.put( PARAM_NAME_USER_NAME      , userName );
        							paramsMap.put( PARAM_NAME_PASSWORD       , password );
        							paramsMap.put( PARAM_NAME_FILE_MD5       , fileMd5 );
        							paramsMap.put( PARAM_NAME_FILE_CONTENT   , fileBase64 );
        							
        							httpPost = new HttpPost( replayUploadSite.uploadUrl, paramsMap );
        							
        							GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.connecting", replayUploadSite.displayName ) );
        							if ( httpPost.connect() )
        								GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.connectionEstabilished" ) );
        							else {
        								GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.failedToConnect" ) );
                						proceedToNextReplay( true );
                						continue;
        							}
        							GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.sendingReplay" ) );
        							if ( httpPost.doPost() )
        								GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.replaySentSuccessfully" ) );
        							else {
        								GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.failedToSendReplay" ) );
                						proceedToNextReplay( true );
                						continue;
        							}
        							GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.waitingServerResponse" ) );
        							final String response = httpPost.getResponse();
        							if ( response == null ) {
        								GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.failedToParseServerResponse" ) );
                						proceedToNextReplay( true );
                						continue;
        							}
        							
        							try {
        								// Example response:
        								/*
        								 * <?xml version="1.0" encoding="UTF-8"?>
        								 * <uploadResult docVersion="1.0">
        								 *     <errorCode>0</errorCode>
        								 *     <message>Upload OK.</message>
        								 *     <replayUrl>http://some.host.com/replay?id=1234</replayUrl>
        								 * </uploadResult>
        								 */
        								final Document responseDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new ByteArrayInputStream( response.getBytes( Consts.UTF8 ) ) );
        								final Element  docElement       = responseDocument.getDocumentElement();
        								final int      errorCode        = Integer.parseInt( ( (Element) docElement.getElementsByTagName( "errorCode" ).item( 0 ) ).getTextContent().trim() );
        								if ( errorCode == 0 ) {
        									final String replayUrl = ( (Element) docElement.getElementsByTagName( "replayUrl" ).item( 0 ) ).getTextContent().trim();
        									GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.uploadSucceeded" ) );
        									System.out.println( "Uploaded replay \"" + replayFile.getAbsolutePath() + "\" to site " + replayUploadSite.displayName + ", replay URL: " + replayUrl );
        									// All went well:
        									replayLinkPanel.setUrl( replayUrl, true, true );
        									
        									// Save shared rep info to file
        									MySharedReplaysDialog.saveSharedRepInfo( replayUploadSite.displayName, userName, reportedName, description, replayUrl );
        									
        	        						proceedToNextReplay( false );
        								}
        								else {
        									final String message = ( (Element) docElement.getElementsByTagName( "message" ).item( 0 ) ).getTextContent().trim();
        									GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.uploadFailed" ) );
        									GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.errorCode", errorCode ) );
        									GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.errorMessage", message ) );
        									
        	        						proceedToNextReplay( true );
        								}
        							} catch ( final Exception e ) {
        								GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.failedToParseServerResponse" ) );
        								e.printStackTrace();
                						proceedToNextReplay( true );
        							}
        						} finally {
        							if ( httpPost != null )
        								httpPost.close();
        						}
        						
    						}
    						
    						if ( progressDialog != null )
    							progressDialog.taskFinished();
						} finally {
							ShareReplaysDialog.this.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
							for ( final JComponent component : componentList )
								if ( component != null )
									component.setEnabled( true );
						}
						
						if ( repsToUpload_ > 1 && !progressDialog.isAborted() ) {
    						GuiUtils.appendNewLine( progressTextArea, "" ); // An empty line
    						GuiUtils.appendNewLineWithTimestamp( progressTextArea, Language.getText( "shareReplay.uploadProcess.batchUploadingFinished", repsToUpload_ ) );
						}
					}
					private void proceedToNextReplay( final boolean failed ) {
						if ( progressDialog != null ) {
    						if ( failed )
    							progressDialog.incrementFailed();
    						progressDialog.incrementProcessed();
    						progressDialog.updateProgressBar();
						}
						
						if ( ++replayIndexHolder.value == replayFiles.length )
							replayIndexHolder.value = 0;
						
						if ( replayFiles.length > 1 )
							updateRepInfoAndControlsTask.run();
					}
				}.start();
			}
		};
		uploadButton   .addActionListener( uploadActionListener );		
		uploadAllButton.addActionListener( uploadActionListener );		
		buttonsPanel.add( uploadButton   );
		buttonsPanel.add( uploadAllButton );
		
		buttonsPanel.add( closeButton );
		
		buttonsAndProgressSliderPanel.add( buttonsPanel, BorderLayout.NORTH );
		
		if ( progressSlider != null )
			buttonsAndProgressSliderPanel.add( progressSlider, BorderLayout.CENTER );
		
		southPanel.add( buttonsAndProgressSliderPanel, BorderLayout.NORTH );
		
		progressTextArea.setRows( 10 );
		progressTextArea.setEditable( false );
		final JScrollPane progressScrollPane = new JScrollPane( progressTextArea );
		progressScrollPane.setBorder( BorderFactory.createTitledBorder( Language.getText( "shareReplay.uploadProgressTitle" ) ) );
		southPanel.add( progressScrollPane, BorderLayout.CENTER );
		
		southPanel.add( replayLinkPanel, BorderLayout.SOUTH );
		
		getContentPane().add( southPanel, BorderLayout.SOUTH );
		
		packAndShow( replayDescriptionTextArea, false );
		
		// This dialog is quite height (default height is 861 pixels!), so maximize its size if its bigger than the screen insets...
		GuiUtils.maximizeWindowWithMargin( this, 0, getSize() );
	}
	
	/**
	 * Constructs the replay upload site vector.
	 * @param replayUpoadSiteVector vector to be used
	 */
	private static void constructReplayUploadSiteVector( final Vector< Object > replayUpoadSiteVector ) {
		replayUpoadSiteVector.add( " " );
		replayUpoadSiteVector.addAll( STATIC_REPLAY_UPLOAD_SITE_LIST );
		if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_CUSTOM_REPLAY_SITES_ACKNOWLEDGED ) )
			replayUpoadSiteVector.addAll( Settings.getCustomReplayUploadSiteList() );
	}
	
}
