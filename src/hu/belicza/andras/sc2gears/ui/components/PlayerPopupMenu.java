/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.components;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.PlayerId;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.dialogs.PlayerProfileDialog;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.ObjectRegistry;
import hu.belicza.andras.sc2gearspluginapi.api.listener.PlayerPopupMenuItemListener;
import hu.belicza.andras.sc2gearspluginapi.api.listener.PlayerPopupMenuItemListener.PlayerInfo;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerId;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.BnetLanguage;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Gateway;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerType;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Player popup menu.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class PlayerPopupMenu extends JPopupMenu implements ActionListener {
	
	/**
	 * Custom player menu item specification.
	 * @author Andras Belicza
	 */
	private static class CustomPlayerMenuItemSpec extends CustomMenuItemSpec {
		
		/** Listener to be called when action is performed. */
		public final PlayerPopupMenuItemListener listener;
		
		/**
		 * Creates a new CustomPlayerMenuItemSpec.
		 * @param text     text of the custom menu item spec
		 * @param icon     optional icon of the custom menu item spec
		 * @param listener listener to be called when action is performed
		 */
		public CustomPlayerMenuItemSpec( final String text, final Icon icon, final PlayerPopupMenuItemListener listener ) {
			super( text, icon );
			this.listener = listener;
		}
		
	}
	
	/**
	 * A {@link PlayerInfo} implementation.
	 * @author Andras Belicza
	 */
	private static class PlayerInfoImpl implements PlayerInfo {
		
		private IPlayerId playerId;
		private final PlayerType playerType;
		
		/**
		 * Creates a new PlayerInfoImpl.
		 * @param playerId   reference to the player id
		 * @param playerType type of player
		 */
		public PlayerInfoImpl( final IPlayerId playerId, final PlayerType playerType ) {
			this.playerId  = playerId;
			this.playerType = playerType;
		}
		
		@Override
		public IPlayerId getPlayerId() {
			return playerId;
		}
		
		@Override
		public PlayerType getPlayerType() {
			return playerType;
		}
		
	}
	
	/** Custom player menu item specifications registry. */
	private static final ObjectRegistry< CustomPlayerMenuItemSpec > customMenuItemRegistry = new ObjectRegistry< CustomPlayerMenuItemSpec >();
	
	/**
	 * Adds a new player popup menu item.
	 * @param text     text of the new menu item
	 * @param icon     optional icon of the new menu item
	 * @param listener listener to be called when the menu item is activated
	 * @return a handler that can be used to remove the registered menu item
	 */
	public static Integer addPlayerPopupMenuItem( final String text, final Icon icon, final PlayerPopupMenuItemListener listener ) {
		final CustomPlayerMenuItemSpec customPlayerMenuItemSpec = new CustomPlayerMenuItemSpec( text, icon, listener );
		
		customMenuItemRegistry.add( customPlayerMenuItemSpec );
		
		return customPlayerMenuItemSpec.handler;
	}
	
	/**
	 * Removes a player popup menu item specified by its handler.
	 * @param handler handler of the popup menu item to be removed
	 */
	public static void removePlayerPopupMenuItem( final Integer handler ) {
		synchronized ( customMenuItemRegistry ) {
			for ( final CustomPlayerMenuItemSpec customPlayerMenuItemSpec : customMenuItemRegistry )
				if ( customPlayerMenuItemSpec.handler.equals( handler ) ) {
					customMenuItemRegistry.remove( customPlayerMenuItemSpec );
					break;
				}
		}
	}
	
	/** Identifier of the player.        */
	private final PlayerId   playerId;
	/** Type of the player.              */
	private final PlayerType playerType;
	
	/** Show profile info in pop-up item.              */
	private final JMenuItem showProfileInfoInPopupMenuItem      = new JMenuItem( Language.getText( "module.repAnalyzer.tab.charts.playerMenu.showProfileInfoInPopup" ), Icons.PROFILE );
	/** View Sc2ranks.com profile menu item.           */
	private final JMenuItem viewSc2ranksProfileMenuItem         = new JMenuItem( Language.getText( "module.repAnalyzer.tab.charts.playerMenu.viewSc2ranksProfile" ), Icons.SC2RANKS );
	/** View character profile menu item.              */
	private final JMenuItem viewCharacterProfileMenuItem        = new JMenuItem( Language.getText( "module.repAnalyzer.tab.charts.playerMenu.viewCharacterProfile" ), Icons.PROFILE );
	/** Add to the favored player list menu item.      */
	private final JMenuItem addToFavoredPlayerListMenuItem      = new JMenuItem( Language.getText( "module.repAnalyzer.tab.charts.playerMenu.addToFavoredPlayerList" ), Icons.USER_PLUS );
	/** Remove from the favored player list menu item. */
	private final JMenuItem removeFromFavoredPlayerListMenuItem = new JMenuItem( Language.getText( "module.repAnalyzer.tab.charts.playerMenu.removeFromFavoredPlayerList" ), Icons.USER_MINUS );
	/** Copy the full name to the clipboard menu item. */
	private final JMenuItem copyFullNameMenuItem;
	
	/**
	 * Creates a new PlayerPopupMenu.
	 * 
	 * @param playerId   identifier of the player
	 * @param playerType player type
	 */
	public PlayerPopupMenu( final PlayerId playerId, final PlayerType playerType ) {
		this.playerId   = playerId;
		this.playerType = playerType;
		
		// GUI:
		
		final boolean noProfile = playerId.gateway == Gateway.UNKNOWN || playerId.gateway == Gateway.PUBLIC_TEST || playerType != PlayerType.HUMAN || playerId.battleNetId == 0;
		
		if ( noProfile )
			showProfileInfoInPopupMenuItem.setEnabled( false );
		showProfileInfoInPopupMenuItem.addActionListener( this );
		add( showProfileInfoInPopupMenuItem );
		
		addSeparator();
		
		if ( noProfile )
			viewSc2ranksProfileMenuItem.setEnabled( false );
		viewSc2ranksProfileMenuItem.addActionListener( this );
		add( viewSc2ranksProfileMenuItem );
		
		if ( noProfile )
			viewCharacterProfileMenuItem.setEnabled( false );
		viewCharacterProfileMenuItem.addActionListener( this );
		add( viewCharacterProfileMenuItem );
		
		final JMenu viewCharacterProfileInLanguageMenu = new JMenu( Language.getText( "module.repAnalyzer.tab.charts.playerMenu.viewCharacterProfileInLanguage" ) );
		viewCharacterProfileInLanguageMenu.setIcon( Icons.PROFILE );
		if ( noProfile )
			viewCharacterProfileInLanguageMenu.setEnabled( false );
		else {
			final List< BnetLanguage > availableBnetLanguageList = new ArrayList< BnetLanguage >( playerId.gateway.availableLanguageSet );
			Collections.sort( availableBnetLanguageList );
			for ( final BnetLanguage bnetLanguage : availableBnetLanguageList ) {
				final JMenuItem viewCharacterProfileInLanguageMenuItem = new JMenuItem( bnetLanguage.stringValue, Icons.getLanguageIcon( Language.getDefaultText( bnetLanguage.textKey ) ) );
				viewCharacterProfileInLanguageMenuItem.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						GeneralUtils.showURLInBrowser( playerId.getBattleNetProfileUrl( bnetLanguage ) );
					}
				} );
				viewCharacterProfileInLanguageMenu.add( viewCharacterProfileInLanguageMenuItem );
			}
		}
		add( viewCharacterProfileInLanguageMenu );
		
		addSeparator();
		
		final List< String > favoredPlayerList = GeneralUtils.getFavoredPlayerList();
		final boolean playerIsOnTheFavoredList = favoredPlayerList.contains( playerId.name );
		
		addToFavoredPlayerListMenuItem.setEnabled( !playerIsOnTheFavoredList );
		addToFavoredPlayerListMenuItem.addActionListener( this );
		add( addToFavoredPlayerListMenuItem );
		
		final JMenu insertToTheFavoredListBeforePlayerMenu = new JMenu( Language.getText( "module.repAnalyzer.tab.charts.playerMenu.insertToFavoredPlayerList" ) );
		insertToTheFavoredListBeforePlayerMenu.setIcon( Icons.USER_PLUS );
		insertToTheFavoredListBeforePlayerMenu.setEnabled( !playerIsOnTheFavoredList );
		if ( !playerIsOnTheFavoredList ) {
			for ( final String favoredPlayerName : favoredPlayerList ) {
				final JMenuItem playerMenuItem = new JMenuItem( favoredPlayerName );
				playerMenuItem.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed( final ActionEvent event ) {
						final List< String > favoredPlayerList_ = GeneralUtils.getFavoredPlayerList();
						final int insertIndex = favoredPlayerList_.indexOf( favoredPlayerName );
						if ( insertIndex >= 0 ) {
							favoredPlayerList.add( insertIndex, playerId.name );
							Settings.set( Settings.KEY_SETTINGS_MISC_FAVORED_PLAYER_LIST, getFavoredPlayerListString( favoredPlayerList ) );
						}
					}
				} );
				insertToTheFavoredListBeforePlayerMenu.add( playerMenuItem );
			}
		}
		add( insertToTheFavoredListBeforePlayerMenu );
		
		removeFromFavoredPlayerListMenuItem.setEnabled( playerIsOnTheFavoredList );
		removeFromFavoredPlayerListMenuItem.addActionListener( this );
		add( removeFromFavoredPlayerListMenuItem );
		
		addSeparator();
		
		copyFullNameMenuItem = new JMenuItem( Language.getText( "module.repAnalyzer.tab.charts.playerMenu.copyFullName", playerId.getFullName() ), Icons.CLIPBOARD_SIGN );
		copyFullNameMenuItem.addActionListener( this );
		add( copyFullNameMenuItem );
		
		synchronized ( customMenuItemRegistry ) {
			if ( !customMenuItemRegistry.isEmpty() ) {
				addSeparator();
				for ( final CustomPlayerMenuItemSpec customPlayerMenuItemSpec : customMenuItemRegistry ) {
					final JMenuItem customMenuItem = new JMenuItem( customPlayerMenuItemSpec.text, customPlayerMenuItemSpec.icon );
					customMenuItem.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed( final ActionEvent event ) {
							customPlayerMenuItemSpec.listener.actionPerformed( new PlayerInfoImpl( playerId, playerType ), customPlayerMenuItemSpec.handler );
						}
					} );
					add( customMenuItem );
				}
			}
		}
	}
	
	@Override
	public void actionPerformed( final ActionEvent event ) {
		if ( event.getSource() == showProfileInfoInPopupMenuItem ) {
			
			new PlayerProfileDialog( playerId, playerType );
			
		} else if ( event.getSource() == viewCharacterProfileMenuItem ) {
			
			GeneralUtils.showURLInBrowser( playerId.getBattleNetProfileUrl( BnetLanguage.values()[ Settings.getInt( Settings.KEY_SETTINGS_MISC_PREFERRED_BNET_LANGUAGE ) ] ) );
				
		} else if ( event.getSource() == viewSc2ranksProfileMenuItem ) {
			
			GeneralUtils.showURLInBrowser( playerId.getSc2ranksProfileUrl() );
			
		} else if ( event.getSource() == addToFavoredPlayerListMenuItem ) {
			
			final List< String > favoredPlayerList = GeneralUtils.getFavoredPlayerList();
			favoredPlayerList.add( playerId.name );
			Settings.set( Settings.KEY_SETTINGS_MISC_FAVORED_PLAYER_LIST, getFavoredPlayerListString( favoredPlayerList ) );
			
		} else if ( event.getSource() == removeFromFavoredPlayerListMenuItem ) {
			
			final List< String > favoredPlayerList = GeneralUtils.getFavoredPlayerList();
			favoredPlayerList.remove( playerId.name );
			Settings.set( Settings.KEY_SETTINGS_MISC_FAVORED_PLAYER_LIST, getFavoredPlayerListString( favoredPlayerList ) );
			
		} else if ( event.getSource() == copyFullNameMenuItem ) {
			
			GeneralUtils.copyToClipboard( playerId.getFullName() );
			
		}
	}
	
	/**
	 * Converts and returns the favored player list as a string which will be a comma separated list of the favored players.
	 * @param favoredPlayerList favored player list to be converted
	 * @return the favored player list as a string which will be a comma separated list of the favored players.
	 */
	private static String getFavoredPlayerListString( List< String > favoredPlayerList ) {
		final StringBuilder builder = new StringBuilder();
		
		for ( final String playerName : favoredPlayerList ) {
			if ( builder.length() > 0 )
				builder.append( ", " );
			builder.append( playerName );
		}
		
		return builder.toString();
	}
	
}
