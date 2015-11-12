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

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.components.TableBox;
import hu.belicza.andras.sc2gears.ui.icons.Icons;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Keyboard shortcuts dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class KeyboardShortcutsDialog extends BaseDialog {
	
	/**
	 * Creates a new TipsDialog.
	 */
	public KeyboardShortcutsDialog() {
		super( "keyboardShortcuts.title", Icons.KEYBOARD );
		
		final String CTRL  = Language.getText( "keyboardShortcuts.key.ctrl"  ) + '+';
		final String SHIFT = Language.getText( "keyboardShortcuts.key.shift" ) + '+';
		final String ALT   = Language.getText( "keyboardShortcuts.key.alt"   ) + '+';
		
		final String NUMBER      = Language.getText( "keyboardShortcuts.key.number"      );
		final String FUNCION_KEY = Language.getText( "keyboardShortcuts.key.functionKey" );
		
		final JTable table = GuiUtils.createNonEditableTable();
		table.setAutoCreateRowSorter( true );
		( (DefaultTableModel) table.getModel() ).setDataVector( createData( 
				new String[][] { 
					{ "global",
					        "enableDisableReplayAutoSave"   , CTRL + ALT + "R" },
					{ null, "enableDisableApmAlert"         , CTRL + ALT + "A" },
					{ null, "startStopMousePrintRecording"  , CTRL + ALT + "M" },
					{ null, "switchToSc2gears"              , CTRL + ALT + "T" },
					{ null, "showHideLastGameInfoDialog"    , CTRL + ALT + "I" },
					{ null, "showHideApmDisplayDialog"      , CTRL + ALT + "U" },
					{ "general",
					        "openReplay"                    , CTRL + "O" },
					{ null, "openLastReplay"                , CTRL + SHIFT+ "O" },
					{ null, "exit"                          , ALT + "X" },
					{ null, "miscSettings"                  , CTRL + "P" },
					{ null, "filterTableRows"               , CTRL + "F" },
					{ null, "filterOutTableRows"            , CTRL + "T" },
					{ null, "openTool"                      , CTRL + SHIFT + FUNCION_KEY },
					{ null, "fullScreen"                    , "F11" },
					{ null, "minimieToTray"                 , "F9" },
					{ null, "tileAllWindows"                , "F2" },
					{ null, "cascadeAllWindows"             , "F3" },
					{ null, "tileVisibleWindows"            , CTRL + "F2" },
					{ null, "cascadeVisibleWindows"         , CTRL + "F3" },
					{ null, "visitHomePage"                 , CTRL + "F1" },
					{ null, "showStartPage"                 , "F1" },
					{ null, "closeInternalWindow"           , CTRL + "F4" },
					{ null, "restoreInternalWindowSize"     , CTRL + "F5" },
					{ null, "cycleThroughTabs"              , CTRL + "TAB" },
					{ null, "cycleThroughInternalWindows"   , CTRL + "TAB" },
					{ null, "cycleThroughInternalWindows"   , CTRL + "F6" },
					{ null, "switchNavTreeIntWindows"       , "F6" },
					{ null, "focusNavTreeIntWindowsSplitter", "F8" },
					{ null, "windowContextMenu"             , CTRL + "SPACE" },
					{ null, "mainMenu"                      , "F10" },
					{ null, "closeTab"                      , CTRL + "W" },
					{ "startPage",
				            "refreshContent"                , "F5" },
					{ "replayAnalyzer",
					        "selectChart"                   , CTRL + NUMBER },
					{ null, "openCloseOverlayChart"         , CTRL + SHIFT + NUMBER },
					{ null, "zoomIn"                        , CTRL + "I" },
					{ null, "zoomOut"                       , CTRL + "U" },
					{ null, "gridOnOff"                     , CTRL + "G" },
					{ null, "openGridSettings"              , CTRL + SHIFT + "G" },
					{ null, "playPause"                     , CTRL + "W" },
					{ null, "jumpBackward"                  , CTRL + "Q" },
					{ null, "jumpForward"                   , CTRL + "E" },
					{ null, "jumpToBeginning"               , CTRL + SHIFT + "Q" },
					{ null, "jumpToEnd"                     , CTRL + SHIFT + "E" },
					{ null, "slowDown"                      , CTRL + "R" },
					{ null, "speedUp"                       , CTRL + SHIFT + "R" },
					{ null, "jumpToFrame"                   , CTRL + "J" },
					{ null, "searchText"                    , CTRL + "S" },
					{ null, "filterActions"                 , CTRL + "F" },
					{ null, "filterOutActions"              , CTRL + "T" },
					{ "multiRepAnalysis",
					        "selectChart"                   , CTRL + NUMBER },
					{ "onTopApmDisplay",
						    "increaseFontSize"              , "+" },
					{ null, "decreaseFontSize"              , "-" }
				} ), new Object[] { Language.getText( "keyboardShortcuts.table.header.context" ), Language.getText( "keyboardShortcuts.table.header.function" ), Language.getText( "keyboardShortcuts.table.header.shortcut" ),  } );
		table.setPreferredScrollableViewportSize( new Dimension( 900, 500 ) );
		GuiUtils.packTable( table );
		final TableBox tableBox = new TableBox( table, getLayeredPane(), null );
		tableBox.setBorder( BorderFactory.createEmptyBorder( 15, 15, 10, 15 ) );
		getContentPane().add( tableBox, BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 0, 15, 10, 15 ) );
		final JButton closeButton = createCloseButton( "button.close" );
		buttonsPanel.add( closeButton );
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		packAndShow( closeButton, false );
	}
	
	/**
	 * Creates the data for the shortcuts table.
	 * @param input input for the data creation; elements: arrays, where elements: 0: context key postfix, 1: function key postfix, 2: shortcut<br>
	 * 		if context key postfix is null, the last context will be used 
	 * @return the data for the shortcuts table
	 */
	private static Object[][] createData( final String[][] input ) {
		final Object[][] data = new Object[ input.length ][ input[ 0 ].length ];
		
		String context = null;
		for ( int i = 0; i < input.length; i++ ) {
			final String[] inputRow = input[ i ];
			final Object[] dataRow  = data [ i ];
			
			for ( int j = 0; j < inputRow.length; j++ ) {
				if ( inputRow[ 0 ] != null )
					context = Language.getText( "keyboardShortcuts.context." + inputRow[ 0 ] );
				dataRow[ 0 ] = context;
				dataRow[ 1 ] = Language.getText( "keyboardShortcuts.function." + inputRow[ 1 ] );
				dataRow[ 2 ] = inputRow[ 2 ];
			}
		}
		
		return data;
	}
	
}
