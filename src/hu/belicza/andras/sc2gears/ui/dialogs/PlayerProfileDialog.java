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
import hu.belicza.andras.sc2gears.sc2replay.model.Details.PlayerId;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.components.TableBox;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.ProfileCache;
import hu.belicza.andras.sc2gearspluginapi.api.enums.League;
import hu.belicza.andras.sc2gearspluginapi.api.listener.CustomPortraitListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ProfileListener;
import hu.belicza.andras.sc2gearspluginapi.api.profile.IProfile;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BnetLanguage;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerType;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Region;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;
import hu.belicza.andras.util.BnetUtils.Profile;
import hu.belicza.andras.util.BnetUtils.Profile.BestTeamRank;
import hu.belicza.andras.util.BnetUtils.Profile.TeamRank;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Player profile dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class PlayerProfileDialog extends BaseDialog {
	
	/** Header keys of the all leagues table. */
	private static final String[] ALL_LEAGUES_HEADER_KEYS = new String[] {
		"module.repAnalyzer.tab.charts.players.portraitToolTip.format",
		"module.repAnalyzer.tab.charts.players.portraitToolTip.league",
		"module.repAnalyzer.tab.charts.players.portraitToolTip.rank",
		"module.repAnalyzer.tab.charts.players.portraitToolTip.teamMembers"
	};
	private static final Set< Integer > DEFAULT_DESCENDING_SORTING_COLUMN_SET = new HashSet< Integer >();
	
	/** Column model index of the league column. */
	private static final int COLUMN_LEAGUE;
	/** Column model index of the format column. */
	private static final int COLUMN_FORMAT;
	/** Header names of the all leagues table.  */
	private static final Vector< String > ALL_LEAGUES_HEADER_NAME_VECTOR = new Vector< String >( ALL_LEAGUES_HEADER_KEYS.length );
	static {
		final Set< String > DEFAULT_DESCENDING_SORTING_COLUMN_KEY_SET = new HashSet< String >();
		Collections.addAll( DEFAULT_DESCENDING_SORTING_COLUMN_KEY_SET, 
				"module.repAnalyzer.tab.charts.players.portraitToolTip.league"
			);
		
		int COLUMN_LEAGUE_ = 0;
		int COLUMN_FORMAT_ = 0;
		for ( int i = 0; i < ALL_LEAGUES_HEADER_KEYS.length; i++ ) {
			final String headerKey = ALL_LEAGUES_HEADER_KEYS[ i ];
			ALL_LEAGUES_HEADER_NAME_VECTOR.addElement( Language.getText( headerKey ) );
			if ( "module.repAnalyzer.tab.charts.players.portraitToolTip.league".equals( headerKey ) )
				COLUMN_LEAGUE_ = i;
			else if ( "module.repAnalyzer.tab.charts.players.portraitToolTip.format".equals( headerKey ) )
				COLUMN_FORMAT_ = i;
			
			if ( DEFAULT_DESCENDING_SORTING_COLUMN_KEY_SET.contains( headerKey ) )
				DEFAULT_DESCENDING_SORTING_COLUMN_SET.add( i );
		}
		COLUMN_LEAGUE = COLUMN_LEAGUE_;
		COLUMN_FORMAT = COLUMN_FORMAT_;
	}
	
	/**
	 * Creates a new ProfileInfoDialog.
	 * 
	 * @param playerId   player identifier
	 * @param playerType player type
	 */
	public PlayerProfileDialog( final PlayerId playerId, final PlayerType playerType ) {
		super( "playerProfile.title", Icons.PROFILE );
		
		setModal( false );
		
		final Box box = Box.createVerticalBox();
		box.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		
		// Top panel
		final JPanel topPanel = new JPanel( new BorderLayout() );
		// Portrait
		JPanel wrapper = new JPanel();
		final JLabel portraitLabel = new JLabel( playerType == PlayerType.COMPUTER ? Icons.PORTRAIT_COMPUTER_ICON : Icons.PORTRAIT_HIGH_LOADING_ICON );
		portraitLabel.setBorder( BorderFactory.createRaisedBevelBorder() );
		wrapper.add( portraitLabel );
		// Start downloading custom portrait now (if defined), it's independent from profile info
		final Boolean customPortraitReady = ProfileCache.queryCustomPortrait( playerId, true, new CustomPortraitListener() {
			@Override
			public void customPortraitReady( final ImageIcon customPortrait ) {
				portraitLabel.setIcon( customPortrait == null ? Icons.PORTRAIT_HIGH_NA_ICON : customPortrait );
			}
		} );
		Box box2 = Box.createVerticalBox();
		final JLabel playerNameLabel = GeneralUtils.createLinkLabel( playerId.name, playerId.getBattleNetProfileUrl( BnetLanguage.values()[ Settings.getInt( Settings.KEY_SETTINGS_MISC_PREFERRED_BNET_LANGUAGE ) ] ) );
		playerNameLabel.setIcon( null );
		playerNameLabel.setFont( playerNameLabel.getFont().deriveFont( Font.BOLD, 24 ) );
		box2.add( playerNameLabel );
		final JLabel achievementLabel = new JLabel( " ", Icons.ACHIEVEMENT, JLabel.LEFT ); // I specify an initial string (space) because the font height is bigger than the icon size, so this way it will take up its final height
		achievementLabel.setFont( achievementLabel.getFont().deriveFont( Font.BOLD, 20 ) );
		box2.add( achievementLabel );
		// gateway, region
		box2.add( Box.createVerticalStrut( 4 ) );
		final Box gatewayBox = Box.createVerticalBox();
		gatewayBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		Box box3 = Box.createHorizontalBox();
		box3.add( new JLabel( Language.getText( "playerProfile.gateway" ) ) );
		box3.add( Box.createHorizontalStrut( 4 ) );
		box3.add( new JLabel( playerId.gateway.stringValue ) );
		box3.add( Box.createGlue() );
		gatewayBox.add( box3 );
		box3 = Box.createHorizontalBox();
		box3.add( new JLabel( Language.getText( "playerProfile.region" ) ) );
		box3.add( Box.createHorizontalStrut( 4 ) );
		box3.add( new JLabel( Region.getFromGatewayAndSubId( playerId.gateway, playerId.battleNetSubId ).stringValue ) );
		box3.add( Box.createGlue() );
		gatewayBox.add( box3 );
		box2.add( gatewayBox );
		GuiUtils.alignBox( gatewayBox, 2 );
		wrapper.add( box2 );
		topPanel.add( wrapper, BorderLayout.WEST );
		// Custom portraits link
		final JLabel customPortraitsLinkLabel = GeneralUtils.createLinkLabel( Language.getText( "playerProfile.customPortraits" ), Consts.URL_CUSTOM_PORTRAITS );
		customPortraitsLinkLabel.setHorizontalAlignment( JLabel.CENTER );
		GuiUtils.changeFontToItalic( customPortraitsLinkLabel );
		topPanel.add( customPortraitsLinkLabel, BorderLayout.CENTER );
		// Timestamp info
		wrapper = new JPanel( new GridLayout( 2, 2, 5, 0 ) );
		wrapper.add( GuiUtils.changeFontToItalic( new JLabel( Language.getText( "playerProfile.updatedAt" ), JLabel.RIGHT ) ) );
		final JLabel updatedAtLabel = new JLabel( Icons.HOURGLASS );
		GuiUtils.changeFontToItalic( updatedAtLabel );
		wrapper.add( updatedAtLabel );
		wrapper.add( new JLabel() );
		final JLabel updateNowLabel = GeneralUtils.createLinkStyledLabel( Language.getText( "playerProfile.updateNow" ) );
		updateNowLabel.setEnabled( false );
		final String updatingProfileText = Language.getText( "playerProfile.updating" );
		final String updateNowLabelText  = updateNowLabel.getText(); // HTML text
		updateNowLabel.setText( updatingProfileText );
		updateNowLabel.setHorizontalAlignment( JLabel.RIGHT );
		GuiUtils.changeFontToItalic( updateNowLabel );
		wrapper.add( updateNowLabel );
		topPanel.add( GuiUtils.wrapInPanel( wrapper ), BorderLayout.EAST );
		box.add( topPanel );
		
		final JPanel bestLeaguesAndInfoPanel = new JPanel( new BorderLayout() );
		// Best leagues
		wrapper = new JPanel( new GridLayout( 5, 5 ) );
		wrapper.setBorder( BorderFactory.createTitledBorder( Language.getText( "playerProfile.currentBestLeagues" ) ) );
		final JLabel[][] labelss = new JLabel[ 5 ][];
		labelss[ 0 ] = new JLabel[] {
			new JLabel( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.league" ), JLabel.CENTER ),
			new JLabel( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.format" ), JLabel.CENTER ),
			new JLabel( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.rank"   ), JLabel.CENTER ),
			new JLabel( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.games"  ), JLabel.CENTER ),
			new JLabel( Language.getText( "module.repAnalyzer.tab.charts.players.portraitToolTip.wins"   ), JLabel.CENTER )
		};
		for ( int i = 0; i < labelss[ 0 ].length; i++ )
			GuiUtils.changeFontToBold( labelss[ 0 ][ i ] );
		for ( int i = 1; i < labelss.length; i++ )
			labelss[ i ] = new JLabel[] { new JLabel( Icons.LEAGUE_LOADING_ICON ), new JLabel( Icons.HOURGLASS ), new JLabel( Icons.HOURGLASS ), new JLabel( Icons.HOURGLASS ), new JLabel( Icons.HOURGLASS ) };
		for ( final JLabel[] row : labelss )
			for ( final JLabel label : row )
				wrapper.add( label );
		box.add( wrapper );
		bestLeaguesAndInfoPanel.add( wrapper, BorderLayout.CENTER );
		
		final JPanel infoWrapper = new JPanel( new BorderLayout() );
		wrapper = new JPanel( new GridLayout( 10, 1, 0, 4 ) );
		wrapper.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		JLabel l = new JLabel( Language.getText( "playerProfile.allGames" ), JLabel.CENTER );
		l.setFont( l.getFont().deriveFont( 14f ) );
		wrapper.add( l );
		final JLabel allGamesLabel = new JLabel( Icons.HOURGLASS, JLabel.CENTER );
		allGamesLabel.setFont( allGamesLabel.getFont().deriveFont( Font.BOLD, 16 ) );
		wrapper.add( allGamesLabel );
		l = new JLabel( Language.getText( "playerProfile.gamesThisSeason" ), JLabel.CENTER );
		l.setFont( l.getFont().deriveFont( 14f ) );
		wrapper.add( l );
		final JLabel gamesThisSeasonLabel = new JLabel( Icons.HOURGLASS, JLabel.CENTER );
		gamesThisSeasonLabel.setFont( gamesThisSeasonLabel.getFont().deriveFont( Font.BOLD, 16 ) );
		wrapper.add( gamesThisSeasonLabel );
		l = new JLabel( Language.getText( "playerProfile.terranWins" ), Icons.getRaceIcon( Race.TERRAN ), JLabel.CENTER );
		l.setFont( l.getFont().deriveFont( 14f ) );
		wrapper.add( l );
		final JLabel terranWinsLabel = new JLabel( Icons.HOURGLASS, JLabel.CENTER );
		terranWinsLabel.setFont( terranWinsLabel.getFont().deriveFont( Font.BOLD, 16 ) );
		wrapper.add( terranWinsLabel );
		l = new JLabel( Language.getText( "playerProfile.zergWins" ), Icons.getRaceIcon( Race.ZERG ), JLabel.CENTER );
		l.setFont( l.getFont().deriveFont( 14f ) );
		wrapper.add( l );
		final JLabel zergWinsLabel = new JLabel( Icons.HOURGLASS, JLabel.CENTER );
		zergWinsLabel.setFont( zergWinsLabel.getFont().deriveFont( Font.BOLD, 16 ) );
		wrapper.add( zergWinsLabel );
		l = new JLabel( Language.getText( "playerProfile.protossWins" ), Icons.getRaceIcon( Race.PROTOSS ), JLabel.CENTER );
		l.setFont( l.getFont().deriveFont( 14f ) );
		wrapper.add( l );
		final JLabel protossWinsLabel = new JLabel( Icons.HOURGLASS, JLabel.CENTER );
		protossWinsLabel.setFont( protossWinsLabel.getFont().deriveFont( Font.BOLD, 16 ) );
		wrapper.add( protossWinsLabel );
		infoWrapper.add( GuiUtils.wrapInPanel( wrapper ), BorderLayout.WEST );
		wrapper = new JPanel( new GridLayout( 7, 1 ) );
		wrapper.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		wrapper.add( new JLabel() );
		l = new JLabel( Language.getText( "playerProfile.highestSoloFinish" ), JLabel.CENTER );
		l.setFont( l.getFont().deriveFont( 14f ) );
		wrapper.add( l );
		final JLabel highestSoloFinishLeagueLabel = new JLabel( Icons.HOURGLASS, JLabel.CENTER );
		highestSoloFinishLeagueLabel.setFont( highestSoloFinishLeagueLabel.getFont().deriveFont( Font.BOLD, 16 ) );
		wrapper.add( highestSoloFinishLeagueLabel );
		wrapper.add( new JLabel() );
		l = new JLabel( Language.getText( "playerProfile.highestTeamFinish" ), JLabel.CENTER );
		l.setFont( l.getFont().deriveFont( 14f ) );
		wrapper.add( l );
		final JLabel highestTeamFinishLeagueLabel = new JLabel( Icons.HOURGLASS, JLabel.CENTER );
		highestTeamFinishLeagueLabel.setFont( highestTeamFinishLeagueLabel.getFont().deriveFont( Font.BOLD, 16 ) );
		wrapper.add( highestTeamFinishLeagueLabel );
		wrapper.add( new JLabel() );
		infoWrapper.add( GuiUtils.wrapInPanel( wrapper ), BorderLayout.EAST );
		bestLeaguesAndInfoPanel.add( infoWrapper, BorderLayout.EAST );
		box.add( bestLeaguesAndInfoPanel );
		
		getContentPane().add( box, BorderLayout.NORTH );
		
		// All leagues
		wrapper = new JPanel( new BorderLayout() );
		wrapper.setBorder( BorderFactory.createTitledBorder( Language.getText( "playerProfile.allLeagues" ) ) );
		final DefaultTableCellRenderer customTableCellRenderer = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column ) {
				super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
				final int columnModelIndex = table.convertColumnIndexToModel( column );
				if ( columnModelIndex == COLUMN_LEAGUE ) {
					@SuppressWarnings("unchecked")
					final Pair< League, Integer > leagueRank = (Pair< League, Integer >) value;
					setIcon( leagueRank.value1.getIconForRank( leagueRank.value2 ) );
					setText( leagueRank.value1.stringValue );
				} else
					setIcon( null );
				return this;
			};
		};
		final JTable allLeaguesTable = new JTable() {
			@Override
			public TableCellRenderer getCellRenderer( final int row, final int column ) {
				return customTableCellRenderer;
			}
			@Override
			public boolean isCellEditable( final int row, final int column ) {
				return false;
			}
		};
		// To have a proper sorting, we need a table model which returns proper classes for columns
		allLeaguesTable.setModel( new DefaultTableModel() {
			@Override
			public Class<?> getColumnClass( final int columnIndex ) {
				int maxTries = getRowCount();
				if ( maxTries == 0 )
					return super.getColumnClass( columnIndex );
				else {
					if ( maxTries > 10 )
						maxTries = 10;
					for ( int i = 0; i < maxTries; i++ ) {
						final Object value = getValueAt( i, columnIndex );
						if ( value != null )
							return value.getClass();
					}
					return Object.class;
				}
			}
		} );
		allLeaguesTable.setRowHeight( League.GRANDMASTER.getIconForRank( 1 ).getIconHeight() );
		( (DefaultTableModel) allLeaguesTable.getModel() ).setColumnIdentifiers( ALL_LEAGUES_HEADER_NAME_VECTOR );
		allLeaguesTable.setAutoCreateRowSorter( true );
		allLeaguesTable.setPreferredScrollableViewportSize( new Dimension( 650, 220 ) );
		// I want default descending sorting in some columns, and default ascending sorting in others
		allLeaguesTable.setRowSorter( new TableRowSorter< TableModel >( allLeaguesTable.getModel() ) {
			@Override
			public void toggleSortOrder( int column ) {
				if ( DEFAULT_DESCENDING_SORTING_COLUMN_SET.contains( column ) ) {
					final List< SortKey > sortKeys = new ArrayList< SortKey >( getSortKeys() );
					if ( sortKeys.isEmpty() || sortKeys.get( 0 ).getColumn() != column ) {
						sortKeys.add( 0, new SortKey( column, SortOrder.DESCENDING ) );
						if ( sortKeys.size() > getMaxSortKeys() )
							sortKeys.remove( getMaxSortKeys() );
						setSortKeys( sortKeys );
						return;
					}
				}
				super.toggleSortOrder( column );
			};
		} );
		
		final TableBox tableBox = new TableBox( allLeaguesTable, getLayeredPane(), null );
		// Custom word cloud label...
		tableBox.getFilterComponentsWrapper().add( Box.createHorizontalStrut( 3 ) );
		final JLabel wordCloudLabel = GeneralUtils.createLinkStyledLabel( Language.getText( "general.tableFilter.wordCloud" ) );
		wordCloudLabel.setIcon( Icons.TAG_CLOUD );
		wordCloudLabel.addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				final TableModel model = allLeaguesTable.getModel();
				final int rowCount = model.getRowCount();
				final List< Pair< String, Integer > > wordList = new ArrayList< Pair< String,Integer > >( rowCount );
				for ( int i = 0; i < rowCount; i++ ) {
					@SuppressWarnings("unchecked")
					final Pair< League, Integer > leaguePair = (Pair< League, Integer >) model.getValueAt( i, COLUMN_LEAGUE );
					wordList.add( new Pair< String, Integer >( model.getValueAt( i, COLUMN_FORMAT ) + " " + leaguePair.value1.stringValue, ( leaguePair.value1 == League.GRANDMASTER ? 200 : 100 ) - leaguePair.value2 ) );
				}
				
				new WordCloudDialog( PlayerProfileDialog.this, Language.getText( "playerProfile.allLeagues" ) + " - " + playerId.name, wordList );
			}
		} );
		tableBox.getFilterComponentsWrapper().add( wordCloudLabel );
		wrapper.add( tableBox, BorderLayout.CENTER );
		getContentPane().add( wrapper, BorderLayout.CENTER );
		
		final JButton closeButton = createCloseButton( "button.close" );
		getContentPane().add( GuiUtils.wrapInPanel( closeButton ), BorderLayout.SOUTH );
		
		final MouseListener queryProfileListener = new MouseAdapter() {
			@Override
			public void mouseClicked( final MouseEvent event ) {
				if ( event != null ) {
					if ( updateNowLabel.isEnabled() ) {
						updateNowLabel.setEnabled( false );
						updateNowLabel.setText( updatingProfileText );
					}
					else
						return;
				}
				
				ProfileCache.queryProfile( playerId, new ProfileListener() {
					@Override
					public void profileReady( final IProfile profile_, final boolean isAnotherRetrievingInProgress ) {
						final Profile profile = (Profile) profile_;
						if ( profile == null ) {
							updatedAtLabel.setIcon( Icons.NA );
						}
						else {
							updatedAtLabel.setIcon( null );
							updatedAtLabel.setText( Language.formatDateTime( profile.updatedAt ) );
						}
						
						if ( customPortraitReady == null )
							portraitLabel.setIcon( profile == null ? Icons.PORTRAIT_HIGH_NA_ICON : Icons.getPortraitHighIcon( profile.portraitGroup, profile.portraitRow, profile.portraitColumn ) );
						if ( profile == null ) {
							achievementLabel    .setIcon( Icons.NA );
							gamesThisSeasonLabel.setIcon( Icons.NA );
							allGamesLabel       .setIcon( Icons.NA );
							terranWinsLabel     .setIcon( Icons.NA );
							zergWinsLabel       .setIcon( Icons.NA );
							protossWinsLabel    .setIcon( Icons.NA );
							highestSoloFinishLeagueLabel.setIcon( Icons.NA );
							highestTeamFinishLeagueLabel.setIcon( Icons.NA );
						}
						else {
							achievementLabel    .setText( Integer.toString( profile.achievementPoints  ) );
							achievementLabel    .setIcon( Icons.ACHIEVEMENT );
							gamesThisSeasonLabel.setText( Integer.toString( profile.gamesThisSeason  ) );
							gamesThisSeasonLabel.setIcon( null );
							allGamesLabel       .setText( Integer.toString( profile.totalCareerGames ) );
							allGamesLabel       .setIcon( null );
							terranWinsLabel     .setText( Integer.toString( profile.terranWins ) );
							terranWinsLabel     .setIcon( null );
							zergWinsLabel       .setText( Integer.toString( profile.zergWins ) );
							zergWinsLabel       .setIcon( null );
							protossWinsLabel    .setText( Integer.toString( profile.protossWins ) );
							protossWinsLabel    .setIcon( null );
							highestSoloFinishLeagueLabel.setIcon( profile.highestSoloFinishLeague.getIconForRank( 200 ) );
							if ( profile.highestSoloFinishTimes > 0 )
								highestSoloFinishLeagueLabel.setText( "x" + profile.highestSoloFinishTimes );
							highestTeamFinishLeagueLabel.setIcon( profile.highestTeamFinishLeague.getIconForRank( 200 ) );
							if ( profile.highestTeamFinishTimes > 0 )
								highestTeamFinishLeagueLabel.setText( "x" + profile.highestTeamFinishTimes );
						}
						
						for ( int i = 0; i < 4; i++ ) {
							final JLabel[] row = labelss[ i+1 ];
							if ( profile == null || profile.bestRanks == null ) {
								if ( !isAnotherRetrievingInProgress ) {
									row[ 0 ].setIcon( Icons.LEAGUE_NA_ICON );
									for ( int j = 1; j < row.length; j++ )
										row[ j ].setIcon( Icons.NA );
								}
							}
							else {
								int j = 0;
								final BestTeamRank bestRank = profile.bestRanks[ i ];
								row[ j   ].setIcon( bestRank == null ? League.UNRANKED.getIconForRank( 0 ) : bestRank.league.getIconForClass( bestRank.leagueClass ) );
								row[ j++ ].setToolTipText( bestRank == null ? League.UNRANKED.stringValue : Language.getText( "playerProfile.currentBestLeagueIconToolTip", bestRank.getTeamMembersString() ) );
								row[ j   ].setIcon( null );
								row[ j++ ].setText( (i+1) + "v" + (i+1) );
								row[ j   ].setIcon( bestRank != null && bestRank.divisionRank == 0 ? Icons.NA : null );
								row[ j++ ].setText( bestRank == null ? "-" : bestRank.divisionRank == 0 ? null : Integer.toString( bestRank.divisionRank ) );
								row[ j   ].setIcon( null );
								row[ j++ ].setText( bestRank == null ? "-" : bestRank.games + "  (Σ " + bestRank.gamesOfFormat + ")" );
								row[ j   ].setIcon( null );
								row[ j++ ].setText( bestRank == null ? "-" : Integer.toString( bestRank.wins ) );
							}
						}
						
						final Vector< Vector< Object > > allLeagueVector = new Vector< Vector< Object > >();
						if ( profile != null )
							for ( int bracket = 0; bracket < profile.allRankss.length; bracket++ ) {
								final TeamRank[] teamRanks = profile.allRankss[ bracket ];
								if ( teamRanks == null )
									continue;
								for ( int teamRankCounter = 0; teamRankCounter < teamRanks.length; teamRankCounter++ ) {
									final TeamRank teamRank = teamRanks[ teamRankCounter ];
									final Vector< Object > leagueVector = new Vector< Object >();
									leagueVector.add( (bracket+1) + "v" + (bracket+1) );
									leagueVector.add( new Pair< League, Integer >( teamRank.league, teamRank.divisionRank ) {
										public String toString() { return teamRank.league.toString(); }; // Override toString() so copying the table will contain meaningful text
									} );
									leagueVector.add( teamRank.divisionRank );
									leagueVector.add( teamRank.getTeamMembersString() );
									allLeagueVector.add( leagueVector );
								}
							}
						
						if ( !isAnotherRetrievingInProgress ) {
							updateNowLabel.setText( updateNowLabelText );
							updateNowLabel.setEnabled( true );
						}
						
						if ( event == null )
							GuiUtils.centerWindowToWindow( PlayerProfileDialog.this, MainFrame.INSTANCE );
						
						// We have to refresh the table "later" to make sure the UI update will not happen while we refresh it!
						SwingUtilities.invokeLater( new Runnable() {
							@Override
							public void run() {
								( (DefaultTableModel) allLeaguesTable.getModel() ).setDataVector( allLeagueVector, ALL_LEAGUES_HEADER_NAME_VECTOR );
								GuiUtils.packTable( allLeaguesTable );
								// The comparator has to be set here, else it is ignored (bug?) 
								// Unique comparator to sort the league column
								( (TableRowSorter< ? extends TableModel >) allLeaguesTable.getRowSorter() ).setComparator( COLUMN_LEAGUE, new Comparator< Pair< League, Integer > >() {
									@Override
									public int compare( final Pair< League, Integer > p1, final Pair< League, Integer > p2 ) {
										final int leagueComp =  p2.value1.compareTo( p1.value1 );
										if ( leagueComp != 0 )
											return leagueComp;
										return p2.value2 - p1.value2;
									}
								} );
							}
						} );
					}
				}, true, event != null );
			}
		};
		
		queryProfileListener.mouseClicked( null );
		
		updateNowLabel.addMouseListener( queryProfileListener );
		
		packAndShow( closeButton, false );
	}
	
}
