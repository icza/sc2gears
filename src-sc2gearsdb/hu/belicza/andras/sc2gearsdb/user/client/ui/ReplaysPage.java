/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.user.client.ui;

import static hu.belicza.andras.sc2gearsdb.common.client.ClientUtils.DATE_FORMAT;
import static hu.belicza.andras.sc2gearsdb.common.client.ClientUtils.NUMBER_FORMAT;
import hu.belicza.andras.sc2gearsdb.common.client.AsyncCallbackAdapter;
import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.ImageButton;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.EntityListResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PageInfo;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PagingTable;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PagingTableConfig;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PagingTableHandler;
import hu.belicza.andras.sc2gearsdb.user.client.Permission;
import hu.belicza.andras.sc2gearsdb.user.client.User;
import hu.belicza.andras.sc2gearsdb.user.client.beans.ReplayFilters;
import hu.belicza.andras.sc2gearsdb.user.client.beans.ReplayFullInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.ReplayInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * Replays page.
 * 
 * @author Andras Belicza
 */
public class ReplaysPage extends UserPage {
	
	private static final Map< Character, String > LEAGUE_LETTER_IMAGE_MAP = new HashMap< Character, String >();
	static {
		LEAGUE_LETTER_IMAGE_MAP.put( 'R', "../dbpackages/GRANDMASTER-1.png" );
		LEAGUE_LETTER_IMAGE_MAP.put( 'M', "../dbpackages/MASTER-1.png"      );
		LEAGUE_LETTER_IMAGE_MAP.put( 'D', "../dbpackages/DIAMOND-1.png"     );
		LEAGUE_LETTER_IMAGE_MAP.put( 'P', "../dbpackages/PLATINUM-1.png"    );
		LEAGUE_LETTER_IMAGE_MAP.put( 'G', "../dbpackages/GOLD-1.png"        );
		LEAGUE_LETTER_IMAGE_MAP.put( 'S', "../dbpackages/SILVER-1.png"      );
		LEAGUE_LETTER_IMAGE_MAP.put( 'B', "../dbpackages/BRONZE-1.png"      );
		LEAGUE_LETTER_IMAGE_MAP.put( 'U', "../dbpackages/UNRANKED-1.png"    );
		LEAGUE_LETTER_IMAGE_MAP.put( '-', "../sc2/profile.png"              );
	}
	
	private int labelsCount;
	
	/**
	 * Creates a new ReplaysPage.
	 */
	public ReplaysPage() {
		super( "Replays", "user/replays" );
	}
	
	@Override
	public void buildGUI() {
		if ( !checkPagePermission( Permission.VIEW_REPLAYS ) )
			return;
		
		labelsCount = ( sharedAccountUserInfo == null ? userInfo : sharedAccountUserInfo ).getLabelNames() == null
				? 0 : ( sharedAccountUserInfo == null ? userInfo : sharedAccountUserInfo ).getLabelNames().size();
		
		@SuppressWarnings( "unchecked" )
		final PagingTable< ReplayInfo >[] pagingTableReference = new PagingTable[ 0 ];
		
		final FlexTable filtersTable = new FlexTable();
		DOM.setStyleAttribute( filtersTable.getElement(), "border", "1px dashed #888888" );
		filtersTable.setCellSpacing( 3 );
		filtersTable.setCellPadding( 0 );
		
		int row = 0, col = 0;
		filtersTable.setWidget( row, col++, new Label( "Labels:" ) );
		filtersTable.getCellFormatter().setHorizontalAlignment( row, col-1, HasHorizontalAlignment.ALIGN_LEFT );
		final HorizontalPanel labelsPanel = new HorizontalPanel();
		labelsPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		final CheckBox[] labelCheckBoxes = new CheckBox[ labelsCount ]; 
		for ( int i = 0; i < labelsCount; i++ ) {
			labelsPanel.add( labelCheckBoxes[ i ] = new CheckBox( getLabelName( i ) ) );
			setLabelStyle( labelCheckBoxes[ i ], i );
			labelsPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
		}
		filtersTable.setWidget( row, col++, labelsPanel );
		filtersTable.getFlexCellFormatter().setColSpan( row, col-1, 9 );
		final Button renameLabelsButton = new Button( "Rename labels", new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				if ( !checkPermission( Permission.RENAME_LABELS ) )
					return; // Just to be sure...
				
				final DialogBox dialogBox = new DialogBox( true );
				dialogBox.setText( "Rename labels" );
				dialogBox.setGlassEnabled( true );
				final VerticalPanel content = new VerticalPanel();
				content.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
				content.add( ClientUtils.createVerticalEmptyWidget( 5 ) );
				content.add( new Label( "Tip: Clear a name before save to restore its default name." ) );
				content.add( ClientUtils.createVerticalEmptyWidget( 5 ) );
				final HorizontalPanel labelsRow = new HorizontalPanel();
				final TextBox[] labelEditTextBoxes = new TextBox[ labelsCount ];
				for ( int i = 0; i < labelEditTextBoxes.length; i++ ) {
					labelEditTextBoxes[ i ] = new TextBox();
					setLabelStyle( labelEditTextBoxes[ i ], i );
					labelEditTextBoxes[ i ].setText( getLabelName( i ) );
					labelEditTextBoxes[ i ].setWidth( "60px" );
					labelsRow.add( labelEditTextBoxes[ i ] );
					labelsRow.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
				}
				content.add( labelsRow );
				content.add( ClientUtils.createVerticalEmptyWidget( 5 ) );
				
				final HorizontalPanel buttonsPanel = new HorizontalPanel();
				final Button saveLabelNamesButton = new Button( "Save", new ClickHandler() {
					@Override
					public void onClick( final ClickEvent event ) {
						final List< String > editedLabelNames = new ArrayList< String >( labelEditTextBoxes.length );
						for ( int i = 0; i < labelEditTextBoxes.length; i++ )
							editedLabelNames.add( labelEditTextBoxes[ i ].getText() );
						dialogBox.hide();
						SERVICE_ASYNC.saveLabelNames( getSharedAccount(), editedLabelNames, new AsyncCallbackAdapter< List< String > >( infoPanel ) {
							@Override
							public void customOnSuccess( final RpcResult< List< String > > rpcResult ) {
								if ( rpcResult.getErrorMsg() != null )
									return;
								final List< String > result = rpcResult.getValue();
								( sharedAccountUserInfo == null ? userInfo : sharedAccountUserInfo ).setLabelNames( result );
								for ( int i = 0; i < labelCheckBoxes.length; i++ )
									labelCheckBoxes[ i ].setText( getLabelName( i ) );
								// Refresh table
								if ( pagingTableReference[ 0 ] != null )
									pagingTableReference[ 0 ].reloadCurrentPage();
							}
						} );
					}
				} );
				saveLabelNamesButton.addStyleName( "saveButton" );
				buttonsPanel.add( saveLabelNamesButton );
				buttonsPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
				buttonsPanel.add( ClientUtils.createDialogCloseButton( dialogBox, "Cancel" ) );
				content.add( buttonsPanel );
				content.add( ClientUtils.createVerticalEmptyWidget( 5 ) );
				dialogBox.setWidget( content );
				dialogBox.center();
			}
		} );
		if ( !checkPermission( Permission.RENAME_LABELS ) )
			renameLabelsButton.setEnabled( false );
		ClientUtils.styleSmallButton( renameLabelsButton );
		filtersTable.setWidget( row, col++, renameLabelsButton );
		filtersTable.getFlexCellFormatter().setColSpan( row, col-1, 2 );
		
		final String FIELD_WIDTH = "85px";
		row++; col = 0;
		filtersTable.setWidget( row, col++, new Label( "Map:" ) );
		filtersTable.getCellFormatter().setHorizontalAlignment( row, col-1, HasHorizontalAlignment.ALIGN_LEFT );
		final TextBox mapTextBox = new TextBox();
		mapTextBox.setWidth( FIELD_WIDTH );
		filtersTable.setWidget( row, col++, mapTextBox );
		filtersTable.setWidget( row, col++, new Label( "Players:" ) );
		filtersTable.getCellFormatter().setHorizontalAlignment( row, col-1, HasHorizontalAlignment.ALIGN_LEFT );
		final TextBox playersTextBox = new TextBox();
		playersTextBox.setTitle( "A comma separated list of player names." );
		playersTextBox.setWidth( FIELD_WIDTH );
		filtersTable.setWidget( row, col++, playersTextBox );
		filtersTable.setWidget( row, col++, new Label( "Type:" ) );
		filtersTable.getCellFormatter().setHorizontalAlignment( row, col-1, HasHorizontalAlignment.ALIGN_LEFT );
		final TextBox typeTextBox = new TextBox();
		typeTextBox.setWidth( FIELD_WIDTH );
		filtersTable.setWidget( row, col++, typeTextBox );
		filtersTable.setWidget( row, col++, new Label( "Format:" ) );
		filtersTable.getCellFormatter().setHorizontalAlignment( row, col-1, HasHorizontalAlignment.ALIGN_LEFT );
		final TextBox formatTextBox = new TextBox();
		formatTextBox.setTitle( "Example: 1v1, 2v2, FFA" );
		formatTextBox.setWidth( FIELD_WIDTH );
		filtersTable.setWidget( row, col++, formatTextBox );
		filtersTable.setWidget( row, col++, new Label( "Match-up:" ) );
		filtersTable.getCellFormatter().setHorizontalAlignment( row, col-1, HasHorizontalAlignment.ALIGN_LEFT );
		final TextBox matchupTextBox = new TextBox();
		matchupTextBox.setTitle( "Example: \"TPvPZ\". Permutations will also be listed (for example \"ZPvPT\"). Also works for FFA games (e.g. \"PvTvZvZ\")" );
		matchupTextBox.setWidth( FIELD_WIDTH );
		filtersTable.setWidget( row, col++, matchupTextBox );
		// Filter control buttons
		final Button applyFiltersButton = new Button( "Apply" );
		ClientUtils.styleSmallButton( applyFiltersButton );
		applyFiltersButton.setHeight( "100%" );
		DOM.setStyleAttribute( applyFiltersButton.getElement(), "minHeight", "50px" ); // Needed for FireFox and IE, else button does not take 100% height!
		filtersTable.setWidget( row, col++, applyFiltersButton );
		filtersTable.getFlexCellFormatter().setRowSpan( row, col-1, 2 );
		final Button clearFiltersButton = new Button( "Clear" );
		ClientUtils.styleSmallButton( clearFiltersButton );
		clearFiltersButton.setHeight( "100%" );
		DOM.setStyleAttribute( clearFiltersButton.getElement(), "minHeight", "50px" ); // Needed for FireFox and IE, else button does not take 100% height!
		filtersTable.setWidget( row, col++, clearFiltersButton );
		filtersTable.getFlexCellFormatter().setRowSpan( row, col-1, 2 );
		row++; col = 0;
		filtersTable.setWidget( row, col++, new Label( "Date From:" ) );
		filtersTable.getCellFormatter().setHorizontalAlignment( row, col-1, HasHorizontalAlignment.ALIGN_LEFT );
		final DateBox fromDateBox = new DateBox();
		fromDateBox.setFormat( new DateBox.DefaultFormat( DATE_FORMAT ) );
		fromDateBox.setWidth( FIELD_WIDTH );
		filtersTable.setWidget( row, col++, fromDateBox );
		filtersTable.setWidget( row, col++, new Label( "Date To:" ) );
		filtersTable.getCellFormatter().setHorizontalAlignment( row, col-1, HasHorizontalAlignment.ALIGN_LEFT );
		final DateBox toDateBox = new DateBox();
		toDateBox.setFormat( fromDateBox.getFormat() );
		toDateBox.setWidth( FIELD_WIDTH );
		filtersTable.setWidget( row, col++, toDateBox );
		filtersTable.setWidget( row, col++, new Label( "Comment:" ) );
		filtersTable.getCellFormatter().setHorizontalAlignment( row, col-1, HasHorizontalAlignment.ALIGN_LEFT );
		final ListBox commentListBox = new ListBox();
		commentListBox.addItem( "" );
		commentListBox.addItem( "Has Comment" );
		commentListBox.addItem( "No Comment" );
		commentListBox.setWidth( FIELD_WIDTH );
		filtersTable.setWidget( row, col++, commentListBox );
		if ( !checkPermission( Permission.VIEW_UPDATE_REP_COMMENTS ) )
			commentListBox.setEnabled( false );
		filtersTable.setWidget( row, col++, new Label( "Gateway:" ) );
		filtersTable.getCellFormatter().setHorizontalAlignment( row, col-1, HasHorizontalAlignment.ALIGN_LEFT );
		final TextBox gatewayTextBox = new TextBox();
		gatewayTextBox.setWidth( FIELD_WIDTH );
		filtersTable.setWidget( row, col++, gatewayTextBox );
		filtersTable.setWidget( row, col++, new Label( "League MU:" ) );
		filtersTable.getCellFormatter().setHorizontalAlignment( row, col-1, HasHorizontalAlignment.ALIGN_LEFT );
		final TextBox leagueMuTextBox = new TextBox();
		leagueMuTextBox.setTitle( "League match-up. Example: \"DGvMP\". Permutations will also be listed (for example \"PMvGD\"). Also works for FFA games (e.g. \"DvGvPvM\")" );
		leagueMuTextBox.setWidth( FIELD_WIDTH );
		filtersTable.setWidget( row, col++, leagueMuTextBox );
		add( filtersTable );
		add( ClientUtils.createVerticalEmptyWidget( 7 ) );
		
		final PagingTableHandler< ReplayInfo> tableHandler = new PagingTableHandler< ReplayInfo >() {
			private ReplayFilters filters;
			private final boolean hasChangeRepLabelsPermission      = checkPermission( Permission.CHANGE_REP_LABELS        );
			private final boolean hasViewUpdateRepCommentPermission = checkPermission( Permission.VIEW_UPDATE_REP_COMMENTS );
			private final boolean displayWinnerIcons                = userInfo.getDisplayWinners() == 1 || userInfo.getDisplayWinners() == 3;
			private final boolean displayWinnerColors               = userInfo.getDisplayWinners() == 2 || userInfo.getDisplayWinners() == 3;
			private final ClickHandler mapClickHandler = new ClickHandler() {
				@Override
				public void onClick( final ClickEvent event ) {
					mapTextBox.setText( ( (Anchor) event.getSource() ).getText() );
					applyFiltersButton.click();
				}
			};
			private final ClickHandler gameTypeClickHandler = new ClickHandler() {
				@Override
				public void onClick( final ClickEvent event ) {
					typeTextBox.setText( ( (Anchor) event.getSource() ).getText() );
					applyFiltersButton.click();
				}
			};
			private final ClickHandler playerClickHandler = new ClickHandler() {
				@Override
				public void onClick( final ClickEvent event ) {
					playersTextBox.setText( ( (Anchor) event.getSource() ).getText() );
					applyFiltersButton.click();
				}
			};
			private final ClickHandler gatewayClickHandler = new ClickHandler() {
				@Override
				public void onClick( final ClickEvent event ) {
					gatewayTextBox.setText( ( (Anchor) event.getSource() ).getText() );
					applyFiltersButton.click();
				}
			};
			private final ClickHandler formatClickHandler = new ClickHandler() {
				@Override
				public void onClick( final ClickEvent event ) {
					formatTextBox.setText( ( (Anchor) event.getSource() ).getText() );
					applyFiltersButton.click();
				}
			};
			private final ClickHandler leagueMuClickHandler = new ClickHandler() {
				@Override
				public void onClick( final ClickEvent event ) {
					leagueMuTextBox.setText( ( (Anchor) event.getSource() ).getText() );
					applyFiltersButton.click();
				}
			};
			private final ClickHandler matchupClickHandler = new ClickHandler() {
				@Override
				public void onClick( final ClickEvent event ) {
					matchupTextBox.setText( ( (Anchor) event.getSource() ).getText() );
					applyFiltersButton.click();
				}
			};
			@Override
			public String getRowStyleClass( final ReplayInfo replayInfo, final int row ) {
				if ( userInfo.getFavoredPlayerList() == null || userInfo.getFavoredPlayerList().isEmpty() )
					return null;
				
				boolean hasLoser = false;
				for ( final String player : replayInfo.getPlayers() ) {
					if ( userInfo.getFavoredPlayerList().contains( player ) ) {
						if ( replayInfo.getWinners().contains( player ) )
							return "winRow" + ( row & 0x01 );
						else // Set hasLoser=true only if winner-looser info is available!
							hasLoser = !replayInfo.getWinners().isEmpty(); // Do not return yet, there might be a winner too on the favored player list (and winners have higher precedence)...
					}
				}
				
				return hasLoser ? "lossRow" + ( row & 0x01 ) : null;
			}
			@Override
			public Widget createCellWidget( final ReplayInfo replayInfo, final int column ) {
				switch ( column ) {
				case 0 : return createReplayLabelsPanel( replayInfo, null );
				case 1 : return ClientUtils.createTimestampWidget( replayInfo.getReplayDate() );
				case 2 : return ClientUtils.createAnchorWithHandler( replayInfo.getGameType(), gameTypeClickHandler );
				case 3 : {
					final HorizontalPanel mapPanel = new HorizontalPanel();
					mapPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
					if ( userInfo.getMapImageSize() > 0 ) {
						// Quick map file name check
						final int lastSlashIndex = replayInfo.getMapFileName() != null && replayInfo.getMapFileName().length() == 75 ? replayInfo.getMapFileName().lastIndexOf( '/' ) : -1;
						if ( lastSlashIndex == 5 ) {
							final String mapImageUrl = "/mapimage/" + replayInfo.getMapFileName().substring( lastSlashIndex + 1 );
							final Image mapImage = new Image( mapImageUrl );
							mapImage.addStyleName( "pointerMouse" );
							mapImage.addStyleName( "autoWidth" ); // Internet Explorer needs this (other browsers keep aspect ratio...)
							mapImage.setHeight( User.MAP_IMAGE_SIZE_VALUES[ userInfo.getMapImageSize() ] );
							mapPanel.add( mapImage );
							// When clicking on image, popup dialog shows it in double size (of the original image size)
							mapImage.addClickHandler( new ClickHandler() {
								@Override
								public void onClick( final ClickEvent event ) {
									final DialogBox dialogBox = new DialogBox( true );
									dialogBox.setText( replayInfo.getMapName() );
									dialogBox.setGlassEnabled( true );
									final Image mapImage2 = new Image( mapImageUrl );
									mapImage2.addStyleName( "pointerMouse" );
									mapImage2.addStyleName( "autoWidth" ); // Internet Explorer needs this (other browsers keep aspect ratio...)
									mapImage2.addLoadHandler( new LoadHandler() {
										@Override
										public void onLoad( final LoadEvent event ) {
											if ( mapImage2.getHeight() > 0 )
												mapImage2.setHeight( ( 2 * mapImage2.getHeight() ) + "px" );
											dialogBox.center();
										}
									} );
									mapImage2.addClickHandler( new ClickHandler() {
										@Override
										public void onClick( final ClickEvent event ) {
											dialogBox.hide();
										}
									} );
									dialogBox.setWidget( mapImage2 );
									dialogBox.center();
								}
							} );
							mapPanel.add( ClientUtils.createHorizontalEmptyWidget( 2 ) );
						}
					}
					mapPanel.add( ClientUtils.createAnchorWithHandler( replayInfo.getMapName(), mapClickHandler ) );
					return mapPanel;
				}
				case 4 : return ClientUtils.createAnchorWithHandler( replayInfo.getLeagueMatchup(), leagueMuClickHandler );
				case 5 : return ClientUtils.createAnchorWithHandler( replayInfo.getRaceMatchup(), matchupClickHandler );
				case 6 : return new Label( ClientUtils.formatSeconds( userInfo.isConvertToRealTime() ? replayInfo.getGameLength() * 26 / 36 : replayInfo.getGameLength() ) );
				case 7 : return createPlayersInfoPanel( replayInfo );
				case 8 : {
					final HorizontalPanel actionsPanel = new HorizontalPanel();
					final Button detailsButton = new ImageButton( "edit-column.png", "Details" );
					detailsButton.addClickHandler( new ClickHandler() {
						@Override
						public void onClick( final ClickEvent event ) {
							SERVICE_ASYNC.getReplayFullInfo( getSharedAccount(), replayInfo.getSha1(), new AsyncCallbackAdapter< ReplayFullInfo >( infoPanel ) {
								@Override
								public void customOnSuccess( final RpcResult< ReplayFullInfo > rpcResult ) {
									final ReplayFullInfo replayFullInfo = rpcResult.getValue();
									ClientUtils.displayDetailsDialog( "Replay details", new Object[][] {
										{ "File name:"         , replayFullInfo.getFileName() },
										{ "Uploaded at:"       , replayFullInfo.getUploadedDate() },
										{ "File size:"         , NUMBER_FORMAT.format( replayFullInfo.getFileSize() ) + " bytes" },
										{ "File last modified:", replayFullInfo.getFileLastModified() },
										{ "File SHA-1:"        , replayFullInfo.getSha1() },
										{ "Labels:"            , createReplayLabelsPanel( replayInfo, null ) },
										{ "Version:"           , replayFullInfo.getVersion() },
										{ "Replay date:"       , replayFullInfo.getReplayDate() },
										{ "Game length:"       , ClientUtils.formatSeconds( userInfo.isConvertToRealTime() ? replayInfo.getGameLength() * 26 / 36 : replayInfo.getGameLength() ) },
										{ "Gateway:"           , ClientUtils.createAnchorWithHandler( replayFullInfo.getGateway(), gatewayClickHandler ) },
										{ "Game type:"         , ClientUtils.createAnchorWithHandler( replayFullInfo.getGameType(), gameTypeClickHandler ) },
										{ "Format:"            , ClientUtils.createAnchorWithHandler( replayFullInfo.getFormat(), formatClickHandler ) },
										{ "League match-up:"   , ClientUtils.createAnchorWithHandler( replayFullInfo.getLeagueMatchup(), leagueMuClickHandler ) },
										{ "Race match-up:"     , ClientUtils.createAnchorWithHandler( replayFullInfo.getRaceMatchup(), matchupClickHandler ) },
										{ "Map name:"          , ClientUtils.createAnchorWithHandler( replayFullInfo.getMapName(), mapClickHandler ) },
										{ "Map file name:"     , replayFullInfo.getMapFileName() },
										{ "Players:"           , createPlayersInfoPanel( replayInfo ) }
									} );
								}
							} );
						}
					} );
					actionsPanel.add( detailsButton );
					actionsPanel.add( ClientUtils.createHorizontalEmptyWidget( 3 ) );
					if ( hasViewUpdateRepCommentPermission ) {
						final String toolTip = replayInfo.getComment() == null || replayInfo.getComment().isEmpty() ? "Add comment" : " Edit comment:\n" + replayInfo.getComment();
						final ImageButton editCommentButton = new ImageButton( replayInfo.getComment() == null || replayInfo.getComment().isEmpty() ? "balloon--plus.png" : "balloon-buzz.png", toolTip );
						editCommentButton.addClickHandler( new ClickHandler() {
							@Override
							public void onClick( final ClickEvent event ) {
								final DialogBox dialogBox = new DialogBox( true );
								dialogBox.setText( "Edit comment" );
								dialogBox.setGlassEnabled( true );
								final VerticalPanel content = new VerticalPanel();
								content.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
								content.add( ClientUtils.createVerticalEmptyWidget( 5 ) );
								final HorizontalPanel charsPanel = new HorizontalPanel();
								charsPanel.add( new Label( "Max 500 characters. Remaining: " ) );
								charsPanel.add( ClientUtils.createHorizontalEmptyWidget( 4 ) );
								final Label remainingCharsLabel = new Label();
								charsPanel.add( remainingCharsLabel );
								content.add( charsPanel );
								final TextArea commentTextArea = new TextArea();
								commentTextArea.setSize( "600px", "300px" );
								final KeyUpHandler commentKeyUpHandler = new KeyUpHandler() {
									@Override
									public void onKeyUp( final KeyUpEvent event ) {
										remainingCharsLabel.setText( Integer.toString( 500 - commentTextArea.getText().length() ) );
									}
								};
								commentTextArea.addKeyUpHandler( commentKeyUpHandler );
								commentTextArea.setText( replayInfo.getComment() );
								commentKeyUpHandler.onKeyUp( null ); // Initialize remaining characters
								content.add( commentTextArea );
								content.add( ClientUtils.createVerticalEmptyWidget( 5 ) );
								final HorizontalPanel buttonsPanel = new HorizontalPanel();
								final Button saveCommentButton = new Button( "Save", new ClickHandler() {
									@Override
									public void onClick( final ClickEvent event ) {
										dialogBox.hide();
										final String comment = commentTextArea.getText().length() > 500 ? commentTextArea.getText().substring( 0, 500 ) : commentTextArea.getText();
										SERVICE_ASYNC.saveReplayComment( getSharedAccount(), replayInfo.getSha1(), comment, new AsyncCallbackAdapter< Void >( infoPanel ) {
											@Override
											public void customOnSuccess( final RpcResult< Void > rpcResult ) {
												if ( rpcResult.getErrorMsg() == null ) {
													replayInfo.setComment( comment );
													editCommentButton.setTitle( replayInfo.getComment() == null || replayInfo.getComment().isEmpty() ? "Add comment" : " Edit comment:\n" + replayInfo.getComment() );
													editCommentButton.setImage( replayInfo.getComment() == null || replayInfo.getComment().isEmpty() ? "balloon--plus.png" : "balloon-buzz.png" );
												}
											}
										} );
									}
								} );
								saveCommentButton.addStyleName( "saveButton" );
								buttonsPanel.add( saveCommentButton );
								buttonsPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
	    						buttonsPanel.add( ClientUtils.createDialogCloseButton( dialogBox, "Cancel" ) );
								content.add( buttonsPanel );
								content.add( ClientUtils.createVerticalEmptyWidget( 5 ) );
								dialogBox.setWidget( content );
								dialogBox.center();
								commentTextArea.setFocus( true );
							}
						} );
						actionsPanel.add( editCommentButton );
						actionsPanel.add( ClientUtils.createHorizontalEmptyWidget( 3 ) );
					}
					actionsPanel.add( ClientUtils.createFileDownloadButton( userInfo, userInfo.getReplayFileType(), replayInfo.getSha1(), replayInfo.getFileName(), getSharedAccount() ) );
					return actionsPanel;
				}
				default : throw new RuntimeException( "Unhandled column: " + column );
				}
			}
			@Override
			public String getCursorNamespace() {
				filters = new ReplayFilters();
				final List< Integer > labels = new ArrayList< Integer >( 4 );
				for ( int i = 0; i < labelCheckBoxes.length; i++ )
					if ( labelCheckBoxes[ i ].getValue() )
						labels.add( i );
				if ( !labels.isEmpty() ) filters.setLabels( labels );
				String s;
				if ( !( s = mapTextBox     .getText() ).isEmpty() ) filters.setMapName ( s );
				if ( !( s = playersTextBox .getText() ).isEmpty() ) filters.setPlayers ( s );
				if ( !( s = typeTextBox    .getText() ).isEmpty() ) filters.setGameType( s );
				if ( !( s = formatTextBox  .getText() ).isEmpty() ) filters.setFormat  ( s );
				if ( !( s = leagueMuTextBox.getText() ).isEmpty() ) filters.setLeagueMu( s );
				if ( !( s = matchupTextBox .getText() ).isEmpty() ) filters.setMatchup ( s );
				if ( commentListBox.getSelectedIndex() > 0 ) filters.setHasComment( commentListBox.getSelectedIndex() == 1 ? Boolean.TRUE : Boolean.FALSE );
				if ( !( s = gatewayTextBox.getText() ).isEmpty() ) filters.setGateway ( s );
				filters.setFromDate( fromDateBox.getValue() );
				filters.setToDate  ( toDateBox  .getValue() );
				return filters.toString();
			}
			@Override
			public void getEntityListResult( final PageInfo pageInfo, final AsyncCallback< RpcResult< EntityListResult< ReplayInfo > > > callback ) {
				SERVICE_ASYNC.getReplayInfoList( getSharedAccount(), pageInfo, filters, callback );
			}
			@Override
			public void deleteEntityList( final List< ReplayInfo > entityList, final AsyncCallback< RpcResult< Integer > > callback ) {
				final List< String > sha1List = new ArrayList< String >( entityList.size() );
				for ( final ReplayInfo replayInfo : entityList )
					sha1List.add( replayInfo.getSha1() );
				SERVICE_ASYNC.deleteFileList( getSharedAccount(), userInfo.getReplayFileType(), sha1List, callback );
			}
			@Override
			public void downloadEntityList( final List< ReplayInfo > entityList ) {
				final StringBuilder sha1ListBuilder = new StringBuilder();
				for ( final ReplayInfo replayInfo : entityList ) {
					if ( sha1ListBuilder.length() > 0 )
						sha1ListBuilder.append( ',' );
					sha1ListBuilder.append( replayInfo.getSha1() );
				}
				
				ClientUtils.initiateBatchDownload( userInfo, userInfo.getReplayFileType(), sha1ListBuilder.toString(), getSharedAccount() );
			}
			/**
			 * Returns a panel which contains the players of the specified replay.
			 * @param replayInfo replay info to return a players info panel for
			 * @return a panel which contains the players of the specified replay
			 */
			private FlowPanel createPlayersInfoPanel( final ReplayInfo replayInfo ) {
				final FlowPanel playersPanel = new FlowPanel();
				final List< String  > players     = replayInfo.getPlayers();
				final List< Integer > playerTeams = replayInfo.getPlayerTeams().size() < players.size() ? null : replayInfo.getPlayerTeams();
				final String leagues = replayInfo.getLeagueMatchup() == null ? null : replayInfo.getLeagueMatchup().replace( "v", "" );
				int team, lastTeam = playerTeams != null && playerTeams.size() > 0 ? playerTeams.get( 0 ).intValue() : -1;
				for ( int i = 0; i < players.size(); i++ ) {
					if ( i > 0 ) {
						if ( playerTeams != null && ( team = playerTeams.get( i ).intValue() ) != lastTeam ) {
							lastTeam = team;
							playersPanel.add( ClientUtils.styledWidget( new InlineLabel( " VS. " ), "strong" ) );
						}
						else
							playersPanel.add( new InlineLabel( ", " ) );
					}
					final String player = players.get( i );
					Character leagueLetter = leagues == null || i >= leagues.length() ? '-' : leagues.charAt( i );
					if ( !LEAGUE_LETTER_IMAGE_MAP.containsKey( leagueLetter ) )
						leagueLetter = '-';
					final Button openPlayerProfileButton = new ImageButton( LEAGUE_LETTER_IMAGE_MAP.get( leagueLetter ), "Open " + player + "'s Battle.net profile" );
					if ( leagueLetter != '-' )
						DOM.setStyleAttribute( openPlayerProfileButton.getElement(), "backgroundPosition", "-6px -5px" ); // League icons are size of 27x27 but we display it as 16x16
					DOM.setStyleAttribute( openPlayerProfileButton.getElement(), "verticalAlign", "top" ); // Without this it's shifted in Firefox...
					final int i_ = i;
					openPlayerProfileButton.addClickHandler( new ClickHandler() {
						@Override
						public void onClick( final ClickEvent event ) {
							if ( replayInfo.getProfileUrlList() == null ) {
								// We have to make an async call to parse the replay.
								// We will only have the profile URL when the async call returns.
								// "User action" ends when onClick() ends, and opening a window after that will be popup-blocked.
								// So we create the window now, and set it's URL later.
								final JavaScriptObject newWindow = ClientUtils.createNewWindow( "/bnet_profile_loader.html?player=" + URL.encodeQueryString( player ), "_blank", "" );
								SERVICE_ASYNC.getProfileUrlList( getSharedAccount(), replayInfo.getSha1(), new AsyncCallbackAdapter< List< String > >( infoPanel ) {
									@Override
									public void customOnSuccess( final RpcResult< List< String > > rpcResult ) {
										final List< String > result = rpcResult.getValue();
										if ( result == null ) {
											ClientUtils.closeWindow( newWindow ); // Window is already opened, have to close it.
											return;
										}
										replayInfo.setProfileUrlList( result );
										openProfile( newWindow );
									}
								} );
							}
							else
								openProfile( null );
						}
						private void openProfile( final JavaScriptObject window ) {
							final String profileUrl = replayInfo.getProfileUrlList().get( i_ );
							if ( profileUrl.isEmpty() ) {
								infoPanel.setErrorMessage( player + " does not have a Battle.net profile." );
								if ( window != null )
									ClientUtils.closeWindow( window ); // Window is already opened, have to close it.
							}
							else {
								if ( window == null )
									Window.open( profileUrl, "_blank", null );
								else
									ClientUtils.setWindowLocation( window, profileUrl );
							}
						}
					} );
					playersPanel.add( openPlayerProfileButton );
					final Anchor playerAnchor = ClientUtils.createAnchorWithHandler( player, playerClickHandler );
					if ( ( displayWinnerIcons || displayWinnerColors ) && !replayInfo.getWinners().isEmpty() ) {
						final boolean winner = replayInfo.getWinners().contains( player );
						if ( displayWinnerIcons )
							ClientUtils.setWidgetIcon( playerAnchor, winner ? "sc2/win.png" : "sc2/loss.png", null, null );
						if ( displayWinnerColors )
							DOM.setStyleAttribute( playerAnchor.getElement(), "color", winner ? "green" : "red" );
					}
					playersPanel.add( playerAnchor );
				}
				
				return playersPanel;
			}
			/**
			 * Returns a panel which contains the labels of the specified replay.
			 * @param replayInfo replay info to return a players info panel for
			 * @param labelsPanel if provided, labels will be put into this panel after a clear()
			 * @return a panel which contains the labels of the specified replay
			 */
			private FlowPanel createReplayLabelsPanel( final ReplayInfo replayInfo, FlowPanel labelsPanel ) {
				if ( labelsPanel == null )
					labelsPanel = new FlowPanel();
				else
					labelsPanel.clear();
				final FlowPanel labelsPanel_ = labelsPanel;
				
				final List< Integer > labels = replayInfo.getLabels();
				if ( labels != null && !labels.isEmpty() ) {
					for ( int i = 0; i < labels.size(); i++ ) {
						if ( i > 0 )
							labelsPanel.add( new InlineLabel( ", " ) );
						final int label = labels.get( i );
						final Anchor labelAnchor = new Anchor( getLabelName( label ) );
						setLabelStyle( labelAnchor, label );
						labelAnchor.addClickHandler( new ClickHandler() {
							@Override
							public void onClick( final ClickEvent event ) {
								labelCheckBoxes[ label ].setValue( Boolean.TRUE );
								applyFiltersButton.click();
							}
						} );
						labelsPanel.add( labelAnchor );
					}
				}
				
				if ( !hasChangeRepLabelsPermission )
					return labelsPanel;
				
				final Button setLabelsButton = new ImageButton( "tag-label.png", "Set labels" );
				DOM.setStyleAttribute( setLabelsButton.getElement(), "marginLeft", "5px" );
				setLabelsButton.addClickHandler( new ClickHandler() {
					@Override
					public void onClick( final ClickEvent event ) {
						final DialogBox dialogBox = new DialogBox( true );
						dialogBox.setText( "Set labels" );
						dialogBox.setGlassEnabled( true );
						final VerticalPanel content = new VerticalPanel();
						content.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
						content.add( ClientUtils.createVerticalEmptyWidget( 7 ) );
						final HorizontalPanel labelsRow = new HorizontalPanel();
						final CheckBox[] labelEditCheckBoxes = new CheckBox[ labelsCount ];
						for ( int i = 0; i < labelEditCheckBoxes.length; i++ ) {
							labelEditCheckBoxes[ i ] = new CheckBox( getLabelName( i ) );
							setLabelStyle( labelEditCheckBoxes[ i ], i );
							if ( replayInfo.getLabels() != null && replayInfo.getLabels().contains( i ) )
								labelEditCheckBoxes[ i ].setValue( Boolean.TRUE );
							labelsRow.add( labelEditCheckBoxes[ i ] );
							labelsRow.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
						}
						content.add( labelsRow );
						content.add( ClientUtils.createVerticalEmptyWidget( 8 ) );
						final HorizontalPanel buttonsPanel = new HorizontalPanel();
						final Button saveLabelsButton = new Button( "Save", new ClickHandler() {
							@Override
							public void onClick( final ClickEvent event ) {
								final List< Integer > editedLabels = new ArrayList< Integer >();
								for ( int i = 0; i < labelEditCheckBoxes.length; i++ )
									if ( labelEditCheckBoxes[ i ].getValue() )
										editedLabels.add( i );
								dialogBox.hide();
								SERVICE_ASYNC.saveReplayLabels( getSharedAccount(), replayInfo.getSha1(), editedLabels, new AsyncCallbackAdapter< Void >( infoPanel ) {
									@Override
									public void customOnSuccess( final RpcResult< Void > rpcResult ) {
										if ( rpcResult.getErrorMsg() == null ) {
											replayInfo.setLabels( editedLabels );
											createReplayLabelsPanel( replayInfo, labelsPanel_ );
										}
									}
								} );
							}
						} );
						saveLabelsButton.addStyleName( "saveButton" );
						buttonsPanel.add( saveLabelsButton );
						buttonsPanel.add( ClientUtils.createHorizontalEmptyWidget( 5 ) );
						buttonsPanel.add( ClientUtils.createDialogCloseButton( dialogBox, "Cancel" ) );
						content.add( buttonsPanel );
						content.add( ClientUtils.createVerticalEmptyWidget( 5 ) );
						dialogBox.setWidget( content );
						dialogBox.center();
					}
				} );
				labelsPanel.add( setLabelsButton );
				
				return labelsPanel;
			}
		};
		
		final PagingTableConfig< ReplayInfo > tableConfig = new PagingTableConfig< ReplayInfo >();
		tableConfig.setInfoPanel       ( infoPanel );
		tableConfig.setEntityName      ( "replay" );
		tableConfig.setEntityNamePlural( "replays" );
		tableConfig.setColumnLabels    ( "Labels", "Date", "Type", "Map", "Leagues", "Match-up", "Length", "Players", "Actions" );
		tableConfig.setColumnHorizontalAlignments( HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_CENTER, HasHorizontalAlignment.ALIGN_CENTER, HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_LEFT );
		tableConfig.setTableHandler    ( tableHandler );
		tableConfig.setDeleteEnabled   ( checkPermission( Permission.DELETE_REPLAYS ) );
		
		final PagingTable< ReplayInfo > pagingTable = pagingTableReference[ 0 ] = new PagingTable< ReplayInfo >( tableConfig );
		
		add( pagingTable );
		
		final ClickHandler applyFiltersClickHandler = new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				pagingTable.reloadFirstPage();
			}
		};
		applyFiltersButton.addClickHandler( applyFiltersClickHandler );
		clearFiltersButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				for ( final CheckBox labelCheckBox : labelCheckBoxes )
					labelCheckBox.setValue( Boolean.FALSE );
				mapTextBox     .setText ( null );
				playersTextBox .setText ( null );
				typeTextBox    .setText ( null );
				formatTextBox  .setText ( null );
				leagueMuTextBox.setText ( null );
				matchupTextBox .setText ( null );
				commentListBox .setSelectedIndex( 0 );
				gatewayTextBox .setText ( null );
				fromDateBox    .setValue( null );
				toDateBox      .setValue( null );
				pagingTable.reloadFirstPage();
			}
		} );
		
		final KeyPressHandler enterHandler = new KeyPressHandler() {
			@Override
			public void onKeyPress( final KeyPressEvent event ) {
				if ( event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER )
					applyFiltersButton.click();
			}
		};
		
		for ( final CheckBox labelCheckBox : labelCheckBoxes )
			labelCheckBox.addClickHandler( applyFiltersClickHandler );
		mapTextBox     .addKeyPressHandler( enterHandler );
		playersTextBox .addKeyPressHandler( enterHandler );
		typeTextBox    .addKeyPressHandler( enterHandler );
		formatTextBox  .addKeyPressHandler( enterHandler );
		leagueMuTextBox.addKeyPressHandler( enterHandler );
		matchupTextBox .addKeyPressHandler( enterHandler );
		gatewayTextBox .addKeyPressHandler( enterHandler );
		commentListBox .addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange( final ChangeEvent event ) {
				applyFiltersButton.click();
			}
		} );
		final ValueChangeHandler< Date > dateBoxChangeHandler = new ValueChangeHandler< Date >() {
			@Override
			public void onValueChange( final ValueChangeEvent< Date > event ) {
				applyFiltersButton.click();
			}
		}; 
		fromDateBox.addValueChangeHandler( dateBoxChangeHandler );
		toDateBox  .addValueChangeHandler( dateBoxChangeHandler );
	}
	
	/**
	 * Returns the name of a label.
	 * @param label label whose name to return
	 * @return the name of a label
	 */
	private String getLabelName( final int label ) {
		return label < labelsCount ? ( sharedAccountUserInfo == null ? userInfo : sharedAccountUserInfo ).getLabelNames().get( label ) : "Label" + ( label + 1 );
	}
	
	/**
	 * Sets the label style of a label widget.
	 * @param labelWidget label widget to style
	 * @param label       label whose style to set
	 */
	private void setLabelStyle( final Widget labelWidget, final int label ) {
		if ( label < userInfo.getLabelColors  ().length )
			DOM.setStyleAttribute( labelWidget.getElement(), "color"     , '#' + userInfo.getLabelColors  ()[ label ] );
		if ( label < userInfo.getLabelBgColors().length )
			DOM.setStyleAttribute( labelWidget.getElement(), "background", '#' + userInfo.getLabelBgColors()[ label ] );
	}
	
}
