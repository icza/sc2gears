/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.icons;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.shared.SharedUtils;
import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;
import hu.belicza.andras.sc2gearspluginapi.api.enums.MiscObject;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.AbilityGroup;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Race;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Research;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Unit;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Upgrade;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Icons manager. Responsible to load and serve icons.
 * 
 * <p>Most of these icons come from the <a href='http://p.yusukekamiyamane.com/'>fugue-icons pack</a>.</p>
 * 
 * <p><b>Portrait icon groups (45, 75 and 90 pixel; 0 is the group, it ranges from 0..5):</b><br>
 * http://media.blizzard.com/sc2/portraits/0-45.jpg
 * http://media.blizzard.com/sc2/portraits/0-75.jpg
 * http://media.blizzard.com/sc2/portraits/0-90.jpg
 * 
 * <b>Slicing service:</b> http://www.htmlkit.com/services/is/</p>
 * 
 * <p><b>Individual icons (first 0..5: group, second: icon index 0..35):</b><br>
 * http://media.blizzard.com/sc2/portraits/0-0.jpg</p>
 * 
 * <p><b>Logo editor:</b><br>
 * http://www.lunapic.com/editor/</p>
 * 
 * @author Andras Belicza
 */
public class Icons {
	
	/**
	 * An extended {@link ImageIcon} which stores the resource from which it was created.
	 * @author Andras Belicza
	 */
	@SuppressWarnings( "serial" )
    public static class XImageIcon extends ImageIcon {
		
		/** The resource from which this image icon was created from. */
		public final URL resource;
		
        /**
         * Creates a new XImageIcon.
         * @param resource resource to create the image icon from 
         */
		public XImageIcon( final URL resource ) {
        	super( resource );
        	this.resource = resource;
        }
	}
	
	/**
	 * No need to instantiate this class.
	 */
	private Icons() {
	}
	
	/** Name of the folder containing the flag icons. */
	private static final String FLAG_ICONS_FOLDER_NAME = "flags";
	
	/** Cache of the language flag icons. */
	private static final Map< String, Icon > LANGUAGE_FLAG_ICON_MAP = new HashMap< String, Icon >();
	
	/**
	 * Returns the image icon representing the specified language.
	 * 
	 * <p>The image is loaded from the file named <code>"&lt;language&gt;.png"</code>.
	 * The icon is first looked for in the flags icon folder. If not found there, it will be checked in the languages folder.
	 * If not found there either, an empty icon will be returned.</p>
	 * 
	 * @param language language whose icon to be returned
	 * @return the icon representing the specified language
	 */
    public static Icon getLanguageIcon( final String language ) {
		Icon flagIcon = LANGUAGE_FLAG_ICON_MAP.get( language );
		
		if ( flagIcon == null ) {
			URL iconResourceUrl = Icons.class.getResource( FLAG_ICONS_FOLDER_NAME + '/' + language + ".gif" );
			if ( iconResourceUrl == null )
				try {
					final File flagImageFile = new File( Consts.FOLDER_LANGUAGES + '/' + language + ".gif" );
					iconResourceUrl = flagImageFile.exists() ? flagImageFile.toURI().toURL() : null;
				} catch ( final MalformedURLException mue ) {
					// This should never happen.
				}
			LANGUAGE_FLAG_ICON_MAP.put( language, flagIcon = iconResourceUrl == null ? getNullIcon( 18, 12 ) : new ImageIcon( iconResourceUrl ) );
		}
		
		return flagIcon;
	}
	
	public static final ImageIcon  SC2GEARS                  = SharedUtils.SC2GEARS;
	public static final ImageIcon  SCELIGHT                  = new ImageIcon ( Icons.class.getResource( "my/scelight.png" ) );
	public static final ImageIcon  FORMAT                    = new ImageIcon ( Icons.class.getResource( "my/format.png" ) );
	public static final ImageIcon  SC2LEAGUE                 = new ImageIcon ( Icons.class.getResource( "my/sc2league.png" ) );
	public static final ImageIcon  SC2                       = new ImageIcon ( Icons.class.getResource( "sc2/misc/sc2.png" ) );
	public static final ImageIcon  SC2_EDITOR                = new ImageIcon ( Icons.class.getResource( "sc2/misc/sc2_editor.png" ) );
	public static final ImageIcon  WATCH_REPLAY              = new ImageIcon ( Icons.class.getResource( "sc2/misc/watch_replay.png" ) );
	public static final ImageIcon  PROFILE                   = new ImageIcon ( Icons.class.getResource( "sc2/misc/profile.png" ) );
	public static final ImageIcon  WIN                       = new ImageIcon ( Icons.class.getResource( "sc2/misc/win.png" ) );
	public static final ImageIcon  LOSS                      = new ImageIcon ( Icons.class.getResource( "sc2/misc/loss.png" ) );
	public static final XImageIcon ACHIEVEMENT               = new XImageIcon( Icons.class.getResource( "sc2/misc/achievement.png" ) );
	public static final ImageIcon  SERVER                    = new ImageIcon ( Icons.class.getResource( "sc2/misc/server.png" ) );
	public static final ImageIcon  LADDER                    = new ImageIcon ( Icons.class.getResource( "sc2/misc/ladder.png" ) );
	
	// Fugue icons
	public static final ImageIcon  ALARM_CLOCK               = new ImageIcon ( Icons.class.getResource( "fugue/alarm-clock.png" ) );
	public static final ImageIcon  APPLICATION_BROWSER       = SharedUtils.APPLICATION_BROWSER;
	public static final ImageIcon  APPLICATION_BLUE          = new ImageIcon ( Icons.class.getResource( "fugue/application-blue.png" ) );
	public static final ImageIcon  APPLICATION_DIALOG        = new ImageIcon ( Icons.class.getResource( "fugue/application-dialog.png" ) );
	public static final ImageIcon  APPLICATION_DOCK_180      = new ImageIcon ( Icons.class.getResource( "fugue/application-dock-180.png" ) );
	public static final ImageIcon  APPLICATION_DOCK_TAB      = new ImageIcon ( Icons.class.getResource( "fugue/application-dock-tab.png" ) );
	public static final ImageIcon  APPLICATION_DOCK          = new ImageIcon ( Icons.class.getResource( "fugue/application-dock.png" ) );
	public static final ImageIcon  APPLICATION_RESIZE_FULL   = new ImageIcon ( Icons.class.getResource( "fugue/application-resize-full.png" ) );
	public static final ImageIcon  APPLICATION_RESIZE        = new ImageIcon ( Icons.class.getResource( "fugue/application-resize.png" ) );
	public static final ImageIcon  APPLICATION_RESIZE_ACTUAL = new ImageIcon ( Icons.class.getResource( "fugue/application-resize-actual.png" ) );
	public static final ImageIcon  APPLICATION_SIDEBAR_LIST  = new ImageIcon ( Icons.class.getResource( "fugue/application-sidebar-list.png" ) );
	public static final ImageIcon  APPLICATION_SIDEBAR       = new ImageIcon ( Icons.class.getResource( "fugue/application-sidebar.png" ) );
	public static final ImageIcon  APPLICATION_SMALL_BLUE    = new ImageIcon ( Icons.class.getResource( "fugue/application-small-blue.png" ) );
	public static final ImageIcon  APPLICATION_SPLIT_TILE    = new ImageIcon ( Icons.class.getResource( "fugue/application-split-tile.png" ) );
	public static final ImageIcon  APPLICATION_TILE_VERTICAL = new ImageIcon ( Icons.class.getResource( "fugue/application-tile-vertical.png" ) );
	public static final ImageIcon  APPLICATIONS_STACK        = new ImageIcon ( Icons.class.getResource( "fugue/applications-stack.png" ) );
	public static final ImageIcon  ARROW_090                 = new ImageIcon ( Icons.class.getResource( "fugue/arrow-090.png" ) );
	public static final ImageIcon  ARROW_180                 = new ImageIcon ( Icons.class.getResource( "fugue/arrow-180.png" ) );
	public static final ImageIcon  ARROW_270                 = new ImageIcon ( Icons.class.getResource( "fugue/arrow-270.png" ) );
	public static final ImageIcon  ARROW_CIRCLE_315          = new ImageIcon ( Icons.class.getResource( "fugue/arrow-circle-315.png" ) );
	public static final ImageIcon  ARROW_CIRCLE_DOUBLE       = new ImageIcon ( Icons.class.getResource( "fugue/arrow-circle-double.png" ) );
	public static final ImageIcon  ARROW_CURVE_180           = new ImageIcon ( Icons.class.getResource( "fugue/arrow-curve-180.png" ) );
	public static final ImageIcon  ARROW_STOP_180            = new ImageIcon ( Icons.class.getResource( "fugue/arrow-stop-180.png" ) );
	public static final ImageIcon  ARROW                     = new ImageIcon ( Icons.class.getResource( "fugue/arrow.png" ) );
	public static final ImageIcon  BALLOONS                  = new ImageIcon ( Icons.class.getResource( "fugue/balloons.png" ) );
	public static final ImageIcon  BINOCULAR_ARROW           = new ImageIcon ( Icons.class.getResource( "fugue/binocular--arrow.png" ) );
	public static final ImageIcon  BINOCULAR                 = new ImageIcon ( Icons.class.getResource( "fugue/binocular.png" ) );
	public static final ImageIcon  BLOCK                     = new ImageIcon ( Icons.class.getResource( "fugue/block.png" ) );
	public static final ImageIcon  BLOCK_ARROW               = new ImageIcon ( Icons.class.getResource( "fugue/block--arrow.png" ) );
	public static final ImageIcon  BROOM                     = new ImageIcon ( Icons.class.getResource( "fugue/broom.png" ) );
	public static final ImageIcon  BURN                      = new ImageIcon ( Icons.class.getResource( "fugue/burn.png" ) );
	public static final ImageIcon  CALCULATOR                = new ImageIcon ( Icons.class.getResource( "fugue/calculator.png" ) );
	public static final ImageIcon  CALENDAR_BLUE             = new ImageIcon ( Icons.class.getResource( "fugue/calendar-blue.png" ) );
	public static final ImageIcon  CALENDAR_SELECT_WEEK      = new ImageIcon ( Icons.class.getResource( "fugue/calendar-select-week.png" ) );
	public static final ImageIcon  CALENDAR_SELECT           = new ImageIcon ( Icons.class.getResource( "fugue/calendar-select.png" ) );
	public static final ImageIcon  CARD                      = new ImageIcon ( Icons.class.getResource( "fugue/card.png" ) );
	public static final ImageIcon  CHAIN                     = new ImageIcon ( Icons.class.getResource( "fugue/chain.png" ) );
	public static final ImageIcon  CHART_PLUS                = new ImageIcon ( Icons.class.getResource( "fugue/chart--plus.png" ) );
	public static final ImageIcon  CHART_UP_COLOR            = new ImageIcon ( Icons.class.getResource( "fugue/chart-up-color.png" ) );
	public static final ImageIcon  CHART_UP                  = new ImageIcon ( Icons.class.getResource( "fugue/chart-up.png" ) );
	public static final ImageIcon  CHART                     = new ImageIcon ( Icons.class.getResource( "fugue/chart.png" ) );
	public static final ImageIcon  CLIPBOARD_SIGN            = new ImageIcon ( Icons.class.getResource( "fugue/clipboard-sign.png" ) );
	public static final ImageIcon  CLOCK_MOON_PHASE          = new ImageIcon ( Icons.class.getResource( "fugue/clock-moon-phase.png" ) );
	public static final ImageIcon  CLOCK                     = new ImageIcon ( Icons.class.getResource( "fugue/clock.png" ) );
	public static final ImageIcon  COLOR                     = new ImageIcon ( Icons.class.getResource( "fugue/color.png" ) );
	public static final ImageIcon  COMPILE                   = new ImageIcon ( Icons.class.getResource( "fugue/compile.png" ) );
	public static final ImageIcon  COMPILE_ERROR             = new ImageIcon ( Icons.class.getResource( "fugue/compile-error.png" ) );
	public static final ImageIcon  COMPUTER                  = new ImageIcon ( Icons.class.getResource( "fugue/computer.png" ) );
	public static final ImageIcon  CONTROL_DOUBLE            = new ImageIcon ( Icons.class.getResource( "fugue/control-double.png" ) );
	public static final ImageIcon  CONTROL_DOUBLE_180        = new ImageIcon ( Icons.class.getResource( "fugue/control-double-180.png" ) );
	public static final ImageIcon  CONTROL_PAUSE             = new ImageIcon ( Icons.class.getResource( "fugue/control-pause.png" ) );
	public static final ImageIcon  CONTROL_RECORD            = new ImageIcon ( Icons.class.getResource( "fugue/control-record.png" ) );
	public static final ImageIcon  CONTROL_SKIP_180          = new ImageIcon ( Icons.class.getResource( "fugue/control-skip-180.png" ) );
	public static final ImageIcon  CONTROL_SKIP              = new ImageIcon ( Icons.class.getResource( "fugue/control-skip.png" ) );
	public static final ImageIcon  CONTROL_STOP_SQUARE       = new ImageIcon ( Icons.class.getResource( "fugue/control-stop-square.png" ) );
	public static final ImageIcon  CONTROL                   = new ImageIcon ( Icons.class.getResource( "fugue/control.png" ) );
	public static final ImageIcon  COUNTER_COUNT_UP          = new ImageIcon ( Icons.class.getResource( "fugue/counter-count-up.png" ) );
	public static final ImageIcon  COUNTER_RESET             = new ImageIcon ( Icons.class.getResource( "fugue/counter-reset.png" ) );
	public static final ImageIcon  COUNTER                   = new ImageIcon ( Icons.class.getResource( "fugue/counter.png" ) );
	public static final ImageIcon  CROSS_BUTTON              = new ImageIcon ( Icons.class.getResource( "fugue/cross-button.png" ) );
	public static final ImageIcon  CROSS_OCTAGON             = SharedUtils.CROSS_OCTAGON;
	public static final ImageIcon  CROSS_SHIELD              = new ImageIcon ( Icons.class.getResource( "fugue/cross-shield.png" ) );
	public static final ImageIcon  CROSS_SMALL               = new ImageIcon ( Icons.class.getResource( "fugue/cross-small.png" ) );
	public static final ImageIcon  CROSS                     = new ImageIcon ( Icons.class.getResource( "fugue/cross.png" ) );
	public static final ImageIcon  CROSS_WHITE               = new ImageIcon ( Icons.class.getResource( "fugue/cross-white.png" ) );
	public static final ImageIcon  DOCUMENT_ATTRIBUTE_B      = new ImageIcon ( Icons.class.getResource( "fugue/document-attribute-b.png" ) );
	public static final XImageIcon DOCUMENT_ATTRIBUTE_P      = new XImageIcon( Icons.class.getResource( "fugue/document-attribute-p.png" ) );
	public static final ImageIcon  DOCUMENT_ATTRIBUTE_V      = new ImageIcon ( Icons.class.getResource( "fugue/document-attribute-v.png" ) );
	public static final ImageIcon  DOCUMENT_COPY             = new ImageIcon ( Icons.class.getResource( "fugue/document-copy.png" ) );
	public static final ImageIcon  DOCUMENT_EXPORT           = new ImageIcon ( Icons.class.getResource( "fugue/document-export.png" ) );
	public static final ImageIcon  DOCUMENT_RENAME           = new ImageIcon ( Icons.class.getResource( "fugue/document-rename.png" ) );
	public static final ImageIcon  DOCUMENT_SHARE            = new ImageIcon ( Icons.class.getResource( "fugue/document-share.png" ) );
	public static final ImageIcon  DOCUMENT_ZIPPER           = new ImageIcon ( Icons.class.getResource( "fugue/document-zipper.png" ) );
	public static final ImageIcon  DOCUMENT                  = new ImageIcon ( Icons.class.getResource( "fugue/document.png" ) );
	public static final ImageIcon  DOCUMENTS_STACK           = new ImageIcon ( Icons.class.getResource( "fugue/documents-stack.png" ) );
	public static final ImageIcon  DISK_ARROW                = new ImageIcon ( Icons.class.getResource( "fugue/disk--arrow.png" ) );
	public static final ImageIcon  DISK_SHARE                = new ImageIcon ( Icons.class.getResource( "fugue/disk-share.png" ) );
	public static final ImageIcon  DISK                      = new ImageIcon ( Icons.class.getResource( "fugue/disk.png" ) );
	public static final ImageIcon  DOOR_OPEN_IN              = SharedUtils.DOOR_OPEN_IN;
	public static final ImageIcon  DRIVE_DOWNLOAD            = new ImageIcon ( Icons.class.getResource( "fugue/drive-download.png" ) );
	public static final ImageIcon  DRIVE_UPLOAD              = new ImageIcon ( Icons.class.getResource( "fugue/drive-upload.png" ) );
	public static final ImageIcon  EDIT_COLUMN               = new ImageIcon ( Icons.class.getResource( "fugue/edit-column.png" ) );
	public static final ImageIcon  EDIT_SIZE_DOWN            = new ImageIcon ( Icons.class.getResource( "fugue/edit-size-down.png" ) );
	public static final ImageIcon  EDIT_SIZE_UP              = new ImageIcon ( Icons.class.getResource( "fugue/edit-size-up.png" ) );
	public static final ImageIcon  EQUALIZER                 = new ImageIcon ( Icons.class.getResource( "fugue/equalizer.png" ) );
	public static final ImageIcon  EXCLAMATION_OCTAGON_FRAME = new ImageIcon ( Icons.class.getResource( "fugue/exclamation-octagon-frame.png" ) );
	public static final ImageIcon  EXCLAMATION_SHIELD_FRAME  = new ImageIcon ( Icons.class.getResource( "fugue/exclamation-shield-frame.png" ) );
	public static final ImageIcon  EXCLAMATION               = new ImageIcon ( Icons.class.getResource( "fugue/exclamation.png" ) );
	public static final ImageIcon  FINGERPRINT_RECOGNITION   = new ImageIcon ( Icons.class.getResource( "fugue/fingerprint-recognition.png" ) );
	public static final ImageIcon  FINGERPRINT               = new ImageIcon ( Icons.class.getResource( "fugue/fingerprint.png" ) );
	public static final ImageIcon  FOLDER                    = new ImageIcon ( Icons.class.getResource( "fugue/folder.png" ) );
	public static final ImageIcon  FOLDER_MINUS              = new ImageIcon ( Icons.class.getResource( "fugue/folder--minus.png" ) );
	public static final ImageIcon  FOLDER_BOOKMARK           = new ImageIcon ( Icons.class.getResource( "fugue/folder-bookmark.png" ) );
	public static final ImageIcon  FOLDER_OPEN               = new ImageIcon ( Icons.class.getResource( "fugue/folder-open.png" ) );
	public static final ImageIcon  FOLDER_TREE               = new ImageIcon ( Icons.class.getResource( "fugue/folder-tree.png" ) );
	public static final ImageIcon  FOLDERS_STACK             = new ImageIcon ( Icons.class.getResource( "fugue/folders-stack.png" ) );
	public static final ImageIcon  FOLDERS                   = new ImageIcon ( Icons.class.getResource( "fugue/folders.png" ) );
	public static final ImageIcon  FUNNEL                    = new ImageIcon ( Icons.class.getResource( "fugue/funnel.png" ) );
	public static final ImageIcon  GLOBE_PENCIL              = new ImageIcon ( Icons.class.getResource( "fugue/globe--pencil.png" ) );
	public static final ImageIcon  GLOBE_NETWORK             = new ImageIcon ( Icons.class.getResource( "fugue/globe-network.png" ) );
	public static final ImageIcon  GRID                      = new ImageIcon ( Icons.class.getResource( "fugue/grid.png" ) );
	public static final ImageIcon  HOME_ARROW                = new ImageIcon ( Icons.class.getResource( "fugue/home--arrow.png" ) );
	public static final ImageIcon  HOURGLASS                 = new ImageIcon ( Icons.class.getResource( "fugue/hourglass.png" ) );
	public static final ImageIcon  IMAGE                     = new ImageIcon ( Icons.class.getResource( "fugue/image.png" ) );
	public static final ImageIcon  INFORMATION               = new ImageIcon ( Icons.class.getResource( "fugue/information.png" ) );
	public static final XImageIcon KEYBOARD                  = new XImageIcon( Icons.class.getResource( "fugue/keyboard.png" ) );
	public static final ImageIcon  LICENSE_KEY               = new ImageIcon ( Icons.class.getResource( "fugue/license-key.png" ) );
	public static final ImageIcon  LIGHT_BULB                = new ImageIcon ( Icons.class.getResource( "fugue/light-bulb.png" ) );
	public static final ImageIcon  INFORMATION_BALLOON       = new ImageIcon ( Icons.class.getResource( "fugue/information-balloon.png" ) );
	public static final ImageIcon  LOCALE                    = new ImageIcon ( Icons.class.getResource( "fugue/locale.png" ) );
	public static final ImageIcon  LOCK_UNLOCK               = new ImageIcon ( Icons.class.getResource( "fugue/lock-unlock.png" ) );
	public static final ImageIcon  LOCK                      = new ImageIcon ( Icons.class.getResource( "fugue/lock.png" ) );
	public static final ImageIcon  MAP_PENCIL                = new ImageIcon ( Icons.class.getResource( "fugue/map--pencil.png" ) );
	public static final ImageIcon  MAP                       = new ImageIcon ( Icons.class.getResource( "fugue/map.png" ) );
	public static final ImageIcon  MAPS_STACK                = new ImageIcon ( Icons.class.getResource( "fugue/maps-stack.png" ) );
	public static final ImageIcon  MEMORY                    = new ImageIcon ( Icons.class.getResource( "fugue/memory.png" ) );
	public static final ImageIcon  MICROPHONE                = new ImageIcon ( Icons.class.getResource( "fugue/microphone.png" ) );
	public static final ImageIcon  MINUS_SMALL               = new ImageIcon ( Icons.class.getResource( "fugue/minus-small.png" ) );
	public static final ImageIcon  MINUS                     = new ImageIcon ( Icons.class.getResource( "fugue/minus.png" ) );
	public static final ImageIcon  MONITOR_CAST              = new ImageIcon ( Icons.class.getResource( "fugue/monitor-cast.png" ) );
	public static final ImageIcon  MONITOR_MEDIUM            = new ImageIcon ( Icons.class.getResource( "fugue/monitor-medium.png" ) );
	public static final ImageIcon  MOUSE                     = new ImageIcon ( Icons.class.getResource( "fugue/mouse.png" ) );
	public static final ImageIcon  NA                        = new ImageIcon ( Icons.class.getResource( "fugue/na.png" ) );
	public static final ImageIcon  NEWSPAPER                 = new ImageIcon ( Icons.class.getResource( "fugue/newspaper.png" ) );
	public static final ImageIcon  NOTEBOOK_ARROW            = new ImageIcon ( Icons.class.getResource( "fugue/notebook--arrow.png" ) );
	public static final ImageIcon  PLUG_CONNECT              = new ImageIcon ( Icons.class.getResource( "fugue/plug-connect.png" ) );
	public static final ImageIcon  PLUG_DISCCONNECT          = new ImageIcon ( Icons.class.getResource( "fugue/plug-disconnect.png" ) );
	public static final ImageIcon  PLUS_SMALL                = new ImageIcon ( Icons.class.getResource( "fugue/plus-small.png" ) );
	public static final ImageIcon  PLUS                      = new ImageIcon ( Icons.class.getResource( "fugue/plus.png" ) );
	public static final ImageIcon  PUZZLE                    = new ImageIcon ( Icons.class.getResource( "fugue/puzzle.png" ) );
	public static final ImageIcon  QUESTION                  = new ImageIcon ( Icons.class.getResource( "fugue/question.png" ) );
	public static final ImageIcon  REPORT_EXCLAMATION        = new ImageIcon ( Icons.class.getResource( "fugue/report--exclamation.png" ) );
	public static final ImageIcon  SERVER_NETWORK            = new ImageIcon ( Icons.class.getResource( "fugue/server-network.png" ) );
	public static final ImageIcon  SORT_ALPHABET             = new ImageIcon ( Icons.class.getResource( "fugue/sort-alphabet.png" ) );
	public static final ImageIcon  SPEAKER_VOLUME            = new ImageIcon ( Icons.class.getResource( "fugue/speaker-volume.png" ) );
	public static final ImageIcon  SUM                       = new ImageIcon ( Icons.class.getResource( "fugue/sum.png" ) );
	public static final ImageIcon  SYSTEM_MONITOR            = new ImageIcon ( Icons.class.getResource( "fugue/system-monitor.png" ) );
	public static final ImageIcon  TABLE_DELETE_ROW          = new ImageIcon ( Icons.class.getResource( "fugue/table-delete-row.png" ) );
	public static final ImageIcon  TABLE_EXCEL               = new ImageIcon ( Icons.class.getResource( "fugue/table-excel.png" ) );
	public static final ImageIcon  TABLE_EXPORT              = new ImageIcon ( Icons.class.getResource( "fugue/table-export.png" ) );
	public static final ImageIcon  TABLE_SELECT_ALL          = new ImageIcon ( Icons.class.getResource( "fugue/table-select-all.png" ) );
	public static final ImageIcon  TABLE_SELECT_ROW          = new ImageIcon ( Icons.class.getResource( "fugue/table-select-row.png" ) );
	public static final ImageIcon  TABLE                     = new ImageIcon ( Icons.class.getResource( "fugue/table.png" ) );
	public static final ImageIcon  TAG_CLOUD                 = new ImageIcon ( Icons.class.getResource( "fugue/tag-cloud.png" ) );
	public static final ImageIcon  TAG_HASH                  = new ImageIcon ( Icons.class.getResource( "fugue/tag-hash.png" ) );
	public static final ImageIcon  TICK_SHIELD               = new ImageIcon ( Icons.class.getResource( "fugue/tick-shield.png" ) );
	public static final ImageIcon  TICK                      = new ImageIcon ( Icons.class.getResource( "fugue/tick.png" ) );
	public static final ImageIcon  TOGGLE                    = new ImageIcon ( Icons.class.getResource( "fugue/toggle.png" ) );
	public static final ImageIcon  TOOLBOX                   = new ImageIcon ( Icons.class.getResource( "fugue/toolbox.png" ) );
	public static final ImageIcon  TOGGLE_EXPAND             = new ImageIcon ( Icons.class.getResource( "fugue/toggle-expand.png" ) );
	public static final ImageIcon  UI_COMBO_BOX_BLUE         = new ImageIcon ( Icons.class.getResource( "fugue/ui-combo-box-blue.png" ) );
	public static final ImageIcon  UI_FLOW                   = new ImageIcon ( Icons.class.getResource( "fugue/ui-flow.png" ) );
	public static final ImageIcon  UI_LAYOUT_PANEL           = new ImageIcon ( Icons.class.getResource( "fugue/ui-layout-panel.png" ) );
	public static final ImageIcon  UI_SCROLL_PANE_LIST       = new ImageIcon ( Icons.class.getResource( "fugue/ui-scroll-pane-list.png" ) );
	public static final ImageIcon  USER_ARROW                = new ImageIcon ( Icons.class.getResource( "fugue/user--arrow.png" ) );
	public static final ImageIcon  USER_PLUS                 = new ImageIcon ( Icons.class.getResource( "fugue/user--plus.png" ) );
	public static final ImageIcon  USER_MINUS                = new ImageIcon ( Icons.class.getResource( "fugue/user--minus.png" ) );
	public static final ImageIcon  USER                      = new ImageIcon ( Icons.class.getResource( "fugue/user.png" ) );
	public static final ImageIcon  USERS                     = new ImageIcon ( Icons.class.getResource( "fugue/users.png" ) );
	public static final ImageIcon  WRENCH                    = new ImageIcon ( Icons.class.getResource( "fugue/wrench.png" ) );
	public static final ImageIcon  WRENCH_SCREWDRIVER        = new ImageIcon ( Icons.class.getResource( "fugue/wrench-screwdriver.png" ) );
	
	public static final ImageIcon  CROSS_BIG                 = new ImageIcon ( Icons.class.getResource( "fugue/24/cross.png" ) );
	public static final ImageIcon  EXCLAMATION_BIG           = new ImageIcon ( Icons.class.getResource( "fugue/24/exclamation.png" ) );
	public static final ImageIcon  TICK_BIG                  = new ImageIcon ( Icons.class.getResource( "fugue/24/tick.png" ) );
	
	// Race icons
	public static final XImageIcon RACE_TERRAN               = new XImageIcon( Icons.class.getResource( "sc2/races/terran.png" ) );
	public static final XImageIcon RACE_PROTOSS              = new XImageIcon( Icons.class.getResource( "sc2/races/protoss.png" ) );
	public static final XImageIcon RACE_ZERG                 = new XImageIcon( Icons.class.getResource( "sc2/races/zerg.png" ) );
	public static final XImageIcon RACE_RANDOM               = new XImageIcon( Icons.class.getResource( "sc2/races/random.png" ) );
	public static final ImageIcon  RACE_ANY                  = new ImageIcon ( Icons.class.getResource( "sc2/races/any.png" ) );
	public static final ImageIcon  RACE_ALL                  = new ImageIcon ( Icons.class.getResource( "sc2/races/all.png" ) );
	
	// FamFamFam icons
	public static final ImageIcon  DATABASE_SAVE             = new ImageIcon ( Icons.class.getResource( "famfamfam/database_save.png" ) );
	public static final ImageIcon  GROUP_LINK                = new ImageIcon ( Icons.class.getResource( "famfamfam/group_link.png" ) );
	public static final XImageIcon HTML                      = new XImageIcon( Icons.class.getResource( "famfamfam/html.png" ) );
	
	
	// Other icons
	public static final ImageIcon  HANDICAP                  = new ImageIcon( Icons.class.getResource( "misc/handicap.gif" ) );
	public static final ImageIcon  LOADING                   = new ImageIcon( Icons.class.getResource( "misc/loading.gif" ) );
	public static final ImageIcon  SC2RANKS                  = new ImageIcon( Icons.class.getResource( "misc/sc2ranks.png" ) );
	
	public static final Icon DEFAULT_NULL_ICON = getNullIcon( 16, 16 );
	
	/** Lazy loaded icons of buildings.      */
	private static final Map< Building    , IconHandler > buildingIcons       = new EnumMap< Building    , IconHandler >( Building    .class );
	/** Lazy loaded icons of units.          */
	private static final Map< Unit        , IconHandler > unitIcons           = new EnumMap< Unit        , IconHandler >( Unit        .class );
	/** Lazy loaded icons of upgrades.       */
	private static final Map< Upgrade     , IconHandler > upgradeIcons        = new EnumMap< Upgrade     , IconHandler >( Upgrade     .class );
	/** Lazy loaded icons of researches.     */
	private static final Map< Research    , IconHandler > researchIcons       = new EnumMap< Research    , IconHandler >( Research    .class );
	/** Lazy loaded icons of ability groups. */
	private static final Map< AbilityGroup, IconHandler > abilityGroupIcons   = new EnumMap< AbilityGroup, IconHandler >( AbilityGroup.class );
	/** Lazy loaded icons of misc groups.    */
	private static final Map< MiscObject  , IconHandler > miscObjectIcons     = new EnumMap< MiscObject  , IconHandler >( MiscObject  .class );
	/** Lazy loaded custom enlarged icons.   */
	private static final Map< String      , IconHandler > customEnlargedIcons = new HashMap< String      , IconHandler >();
	
	/** Lazy loaded group images of portraits. */
	private static final BufferedImage[] PORTRAIT_GROUP_IMAGES = new BufferedImage[ 5 ];
	/** Lazy loaded icons of portraits.
	 * Key: portrait_group << 16 | row << 8 | column */
	private static final Map< Integer, Icon > portraitKeyIconMap = new HashMap< Integer, Icon >();
	
	/** Width of the portrait icons.  */
	public static final int PORTRAIT_ICON_WIDTH       = 45;
	/** Height of the portrait icons. */
	public static final int PORTRAIT_ICON_HEIGHT      = 45;
	/** Width of the portrait icons.  */
	public static final int PORTRAIT_HIGH_ICON_WIDTH  = PORTRAIT_ICON_WIDTH  << 1;
	/** Height of the portrait icons. */
	public static final int PORTRAIT_HIGH_ICON_HEIGHT = PORTRAIT_ICON_HEIGHT << 1;
	
	/** Null portrait icon.                          */
	private static final Icon PORTRAIT_NULL_ICON         = getNullIcon( PORTRAIT_ICON_WIDTH, PORTRAIT_ICON_HEIGHT );
	/** Portrait loading icon.                       */
	public  static final Icon PORTRAIT_LOADING_ICON      = getCustomIcon( HOURGLASS, PORTRAIT_ICON_WIDTH, PORTRAIT_ICON_HEIGHT, 2 );
	/** Portrait not available icon.                 */
	public  static final Icon PORTRAIT_NA_ICON           = getCustomIcon( NA, PORTRAIT_ICON_WIDTH, PORTRAIT_ICON_HEIGHT, 2 );
	/** Computer portrait icon.                      */
	public  static final Icon PORTRAIT_COMPUTER_ICON     = getCustomIcon( COMPUTER, PORTRAIT_ICON_WIDTH, PORTRAIT_ICON_HEIGHT, 2 );
	
	/** High-resolution null portrait icon.          */
	private static final Icon PORTRAIT_HIGH_NULL_ICON    = getNullIcon( PORTRAIT_HIGH_ICON_WIDTH, PORTRAIT_HIGH_ICON_HEIGHT );
	/** High-resolution portrait loading icon.       */
	public  static final Icon PORTRAIT_HIGH_LOADING_ICON = getCustomIcon( HOURGLASS, PORTRAIT_HIGH_ICON_WIDTH, PORTRAIT_HIGH_ICON_HEIGHT, 4 );
	/** High-resolution portrait not available icon. */
	public  static final Icon PORTRAIT_HIGH_NA_ICON      = getCustomIcon( NA, PORTRAIT_HIGH_ICON_WIDTH, PORTRAIT_HIGH_ICON_HEIGHT, 4 );
	
	/** League loading icon.       */
	public static final Icon LEAGUE_LOADING_ICON = getCustomIcon( HOURGLASS, 27, 27, 1 );
	/** League not available icon. */
	public static final Icon LEAGUE_NA_ICON      = getCustomIcon( NA, 27, 27, 1 );
	
	/**
	 * Returns the icon of the specified building in the specified size.
	 * @param building building whose icon to be returned
	 * @param size     size in which the icon to be returned
	 * @return the icon of the specified building in the specified size
	 */
	public static Icon getBuildingIcon( final Building building, final IconSize size ) {
		IconHandler iconHandler = buildingIcons.get( building );
		
		if ( iconHandler == null ) {
			// I omit synchronization: it's a rare case, and even it if happens, there are no consequences
			final URL iconResource = Icons.class.getResource( "sc2/buildings/" + building.name() + ".jpg" );
			buildingIcons.put( building, iconHandler = new IconHandler( iconResource == null ? null : new ImageIcon( iconResource ) ) );
		}
		
		return iconHandler.get( size );
	}
	
	/**
	 * Returns the icon of the specified unit in the specified size.
	 * @param unit unit whose icon to be returned
	 * @param size size in which the icon to be returned
	 * @return the icon of the specified unit in the specified size
	 */
	public static Icon getUnitIcon( final Unit unit, final IconSize size ) {
		IconHandler iconHandler = unitIcons.get( unit );
		
		if ( iconHandler == null ) {
			// I omit synchronization: it's a rare case, and even it if happens, there are no consequences
			final URL iconResource = Icons.class.getResource( "sc2/units/" + unit.name() + ".jpg" );
			unitIcons.put( unit, iconHandler = new IconHandler( iconResource == null ? null : new ImageIcon( iconResource ) ) );
		}
		
		return iconHandler.get( size );
	}
	
	/**
	 * Returns the icon of the specified upgrade in the specified size.
	 * @param upgrade upgrade whose icon to be returned
	 * @param size    size in which the icon to be returned
	 * @return the icon of the specified upgrade in the specified size
	 */
	public static Icon getUpgradeIcon( final Upgrade upgrade, final IconSize size ) {
		IconHandler iconHandler = upgradeIcons.get( upgrade );
		
		if ( iconHandler == null ) {
			// I omit synchronization: it's a rare case, and even it if happens, there are no consequences
			final URL iconResource = Icons.class.getResource( "sc2/upgrades/" + upgrade.name() + ".jpg" );
			upgradeIcons.put( upgrade, iconHandler = new IconHandler( iconResource == null ? null : new ImageIcon( iconResource ) ) );
		}
		
		return iconHandler.get( size );
	}
	
	/**
	 * Returns the icon of the specified research in the specified size.
	 * @param research research whose icon to be returned
	 * @param size     size in which the icon to be returned
	 * @return the icon of the specified research in the specified size
	 */
	public static Icon getResearchIcon( final Research research, final IconSize size ) {
		IconHandler iconHandler = researchIcons.get( research );
		
		if ( iconHandler == null ) {
			// I omit synchronization: it's a rare case, and even it if happens, there are no consequences
			final URL iconResource = Icons.class.getResource( "sc2/researches/" + research.name() + ".jpg" );
			researchIcons.put( research, iconHandler = new IconHandler( iconResource == null ? null : new ImageIcon( iconResource ) ) );
		}
		
		return iconHandler.get( size );
	}
	
	/**
	 * Returns the icon of the specified ability group in the specified size.
	 * @param abilityGroup ability group whose icon to be returned, can be <code>null</code>
	 * @param size         size in which the icon to be returned
	 * @return the icon of the specified ability group in the specified size
	 */
	public static Icon getAbilityGroupIcon( final AbilityGroup abilityGroup, final IconSize size ) {
		if ( abilityGroup == null )
			return IconHandler.NULL.get( size );
		
		IconHandler iconHandler = abilityGroupIcons.get( abilityGroup );
		
		if ( iconHandler == null ) {
			// I omit synchronization: it's a rare case, and even it if happens, there are no consequences
			final URL iconResource = Icons.class.getResource( "sc2/abilitygroups/" + abilityGroup.name() + ".jpg" );
			abilityGroupIcons.put( abilityGroup, iconHandler = new IconHandler( iconResource == null ? null : new ImageIcon( iconResource ) ) );
		}
		
		return iconHandler.get( size );
	}
	
	/**
	 * Returns the icon of the specified misc object in the specified size.
	 * @param miscObject misc object whose icon to be returned, can be <code>null</code>
	 * @param size       size in which the icon to be returned
	 * @return the icon of the specified misc object in the specified size
	 */
	public static Icon getMiscObjectIcon( final MiscObject miscObject, final IconSize size ) {
		if ( miscObject == null )
			return IconHandler.NULL.get( size );
		
		IconHandler iconHandler = miscObjectIcons.get( miscObject );
		
		if ( iconHandler == null ) {
			// I omit synchronization: it's a rare case, and even it if happens, there are no consequences
			final URL iconResource = Icons.class.getResource( "sc2/miscobjects/" + miscObject.name() + ".jpg" );
			miscObjectIcons.put( miscObject, iconHandler = new IconHandler( iconResource == null ? null : new ImageIcon( iconResource ) ) );
		}
		
		return iconHandler.get( size );
	}
	
	/**
	 * Returns a custom enlarged icon.
	 * @param name name of the custom enlarged icon to be returned, can be <code>null</code>
	 * @param size size in which the icon to be returned
	 * @return a custom enlarged icon in the specified size
	 */
	public static Icon getCustomEnlargedIcon( final String name, final IconSize size ) {
		if ( name == null )
			return IconHandler.NULL.get( size );
		
		IconHandler iconHandler = customEnlargedIcons.get( name );
		
		if ( iconHandler == null ) {
			// I omit synchronization: it's a rare case, and even it if happens, there are no consequences
			final ImageIcon customIcon = new ImageIcon( Icons.class.getResource( name ) );
			customEnlargedIcons.put( name, iconHandler = new IconHandler( customIcon == null ? null : new ImageIcon( customIcon.getImage().getScaledInstance( 64, 64, Image.SCALE_SMOOTH ) ) ) );
		}
		
		return iconHandler.get( size );
	}
	
	/**
	 * Returns the icon of an entity in the specified size, be it either a building, unit, research, upgrade,
	 * ability group, misc object or a custom enlarged icon name.
	 * 
	 * @param entity entity whose icon to be returned
	 * @param size   size in which the icon to be returned
	 * @return the icon of an entity in the specified size
	 */
	public static Icon getEntityIcon( final Object entity, final IconSize size ) {
		if ( entity instanceof Unit )
			return getUnitIcon( (Unit) entity, size );
		else if ( entity instanceof Building )
			return getBuildingIcon( (Building) entity, size );
		else if ( entity instanceof AbilityGroup )
			return getAbilityGroupIcon( (AbilityGroup) entity, size );
		else if ( entity instanceof Research )
			return getResearchIcon( (Research) entity, size );
		else if ( entity instanceof Upgrade )
			return getUpgradeIcon( (Upgrade) entity, size );
		else if ( entity instanceof MiscObject )
			return getMiscObjectIcon( (MiscObject) entity, size );
		else if ( entity instanceof String )
			return getCustomEnlargedIcon( (String) entity, size );
		
		return IconHandler.NULL.get( size );
	}
	
	/**
	 * Returns the icon of the specified race.
	 * @param race race whose icon to be returned
	 * @return the icon of the specified race
	 */
	public static XImageIcon getRaceIcon( final Race race ) {
		if ( race == null )
			return null;
		switch ( race ) {
		case TERRAN  : return RACE_TERRAN;
		case PROTOSS : return RACE_PROTOSS;
		case ZERG    : return RACE_ZERG;
		case RANDOM  : return RACE_RANDOM;
		default      : return null;
		}
	}
	
	/**
	 * Returns the icon of the specified replay site.
	 * @param replayUploadSiteName name of the replay upload site
	 */
	public static ImageIcon getReplaySiteIcon( final String replayUploadSiteName ) {
		return new ImageIcon( Icons.class.getResource( "replaysites/" + replayUploadSiteName + ".png" ) );
	}
	
	/**
	 * Returns a null icon with the specified size.
	 * @param width  width of the null icon
	 * @param height height of the null icon
	 * @return a null icon with the specified size
	 */
	public static Icon getNullIcon( final int width, final int height ) {
		return new Icon() {
			@Override
			public void paintIcon( final Component c, final Graphics g, final int x, final int y ) {
				// By definition we do nothing
			}
			@Override
			public int getIconWidth() {
				return width;
			}
			@Override
			public int getIconHeight() {
				return height;
			}
		};
	}
	
	/**
	 * Returns an icon with custom size with another icon centered in it.
	 * @param anotherIcon another icon to be centered in it
	 * @param width       width of the icon
	 * @param height      height of the icon
	 * @param zoom        zoom factor of <code>anotherIcon</code> 
	 * @return an icon with custom size with another icon centered in it
	 */
	public static Icon getCustomIcon( final ImageIcon anotherIcon, final int width, final int height, final int zoom ) {
		return new Icon() {
			@Override
			public void paintIcon( final Component c, final Graphics g, final int x, final int y ) {
				if ( zoom == 1 ) {
					g.drawImage( anotherIcon.getImage(), x + ( ( width - anotherIcon.getIconWidth() ) >> 1 ), y + ( ( height - anotherIcon.getIconHeight() ) >> 1 ), null );
				}
				else {
					final int iconWidth  = anotherIcon.getIconWidth () * zoom;
					final int iconHeight = anotherIcon.getIconHeight() * zoom;
					g.drawImage( anotherIcon.getImage(), x + ( ( width - iconWidth ) >> 1 ), y + ( ( height - iconHeight ) >> 1 ), iconWidth, iconHeight, null );
				}
			}
			@Override
			public int getIconWidth() {
				return width;
			}
			@Override
			public int getIconHeight() {
				return height;
			}
		};
	}
	
	/**
	 * Returns the icon of the specified portrait.
	 * @param group  portrait group
	 * @param row    row in the portrait group
	 * @param column column in the portrait group
	 * @return the icon of the specified portrait; or {@link #PORTRAIT_NULL_ICON} if no icon for the specified portrait
	 */
	public static Icon getPortraitIcon( final int group, final int row, final int column ) {
		final Integer portraitKey = group << 16 | row << 8 | column;
		Icon icon = portraitKeyIconMap.get( portraitKey );
		
		if ( icon == null ) {
			if ( group < 0 || group >= PORTRAIT_GROUP_IMAGES.length )
				return PORTRAIT_NULL_ICON;
			if ( PORTRAIT_GROUP_IMAGES[ group ] == null ) {
				final URL resource = Icons.class.getResource( "sc2/portraits/" + group + ".jpg" );
				
				if ( resource == null )
					return PORTRAIT_NULL_ICON;
				
				final ImageIcon portraitGroupIcons = new ImageIcon( resource );
				PORTRAIT_GROUP_IMAGES[ group ] = new BufferedImage( portraitGroupIcons.getIconWidth(), portraitGroupIcons.getIconHeight(), BufferedImage.TYPE_INT_RGB );
				PORTRAIT_GROUP_IMAGES[ group ].createGraphics().drawImage( portraitGroupIcons.getImage(), 0, 0, null );
			}
			
			final int x = column * PORTRAIT_ICON_WIDTH;
			final int y = row    * PORTRAIT_ICON_HEIGHT;
			if ( x < 0 || y < 0 || x + PORTRAIT_ICON_WIDTH > PORTRAIT_GROUP_IMAGES[ group ].getWidth() || y + PORTRAIT_ICON_HEIGHT > PORTRAIT_GROUP_IMAGES[ group ].getHeight() )
				return PORTRAIT_NULL_ICON;
			
			portraitKeyIconMap.put( portraitKey, icon = new ImageIcon( PORTRAIT_GROUP_IMAGES[ group ].getSubimage( x, y, PORTRAIT_ICON_WIDTH, PORTRAIT_ICON_HEIGHT ) ) );
		}
		
		return icon;
	}
	
	/**
	 * Returns the high-resolution icon of the specified portrait.
	 * @param group  portrait group
	 * @param row    row in the portrait group
	 * @param column column in the portrait group
	 * @return the high-resolution icon of the specified portrait; or {@link #PORTRAIT_HIGH_NULL_ICON} if no icon for the specified portrait
	 */
	public static Icon getPortraitHighIcon( final int group, final int row, final int column ) {
		final URL portraitHighResource = Icons.class.getResource( "sc2/portraits-high/" + group + "/" + (row+1) + "-" + (column+1) + ".jpg" );
		return portraitHighResource == null ? PORTRAIT_HIGH_NULL_ICON : new ImageIcon( portraitHighResource );
	}
	
}
