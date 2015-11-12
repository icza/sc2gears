/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.buildorderstableplugin;

import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.PluginDescriptor;
import hu.belicza.andras.sc2gearspluginapi.PluginServices;
import hu.belicza.andras.sc2gearspluginapi.api.GuiUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.IconsApi;
import hu.belicza.andras.sc2gearspluginapi.api.ReplayUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.ReplayFactoryApi.ReplayContent;
import hu.belicza.andras.sc2gearspluginapi.api.enums.IconSize;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayOpCallback;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayOpsPopupMenuItemListener;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IReplay;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.Building;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IBuildAction;
import hu.belicza.andras.sc2gearspluginapi.api.ui.ITableBox;
import hu.belicza.andras.sc2gearspluginapi.impl.BasePlugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * A demonstration plugin that adds a new <b>"Show Build Orders Table"</b> replay operations menu item
 * which opens the Build orders table of the selected replay in a dialog.
 * 
 * @author Andras Belicza
 */
public class BuildOrdersTablePlugin extends BasePlugin {
	
	/** Hander of the new replay ops popup menu item. */
	private Integer showBoTableItemHandler;
	
	@Override
	public void init( final PluginDescriptor pluginDescriptor, final PluginServices pluginServices, final GeneralServices generalServices ) {
		// Call the init() implementation of the BasePlugin:
		super.init( pluginDescriptor, pluginServices, generalServices );
		
		// Register the new replay ops popup menu item:
		final ImageIcon boIcon = new ImageIcon( getClass().getResource( "block.png" ) );
		
		showBoTableItemHandler = generalServices.getCallbackApi().addReplayOpsPopupMenuItem( generalServices.getLanguageApi().getText( "botplugin.repOpsMenuItem" ), boIcon, new ReplayOpsPopupMenuItemListener() {
			@Override
			public void actionPerformed( final File[] files, final ReplayOpCallback replayOpCallback, final Integer handler ) {
				// Only handle the first file:
				final IReplay replay  = generalServices.getReplayFactoryApi().parseReplay( files[ 0 ].getAbsolutePath(), EnumSet.of( ReplayContent.GAME_EVENTS ) );
				
				if ( replay != null ) {
					final GuiUtilsApi guiUtils = generalServices.getGuiUtilsApi();
					
					// Create the dialog
					final JDialog dialog = new JDialog( guiUtils.getMainFrame(), generalServices.getLanguageApi().getText( "botplugin.title" ) + " - " + files[ 0 ].getName() );
					dialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
					dialog.setIconImage( boIcon.getImage() );
					
					// Build and add the Build orders table
					final JTable table = buildBoTable( replay );
					generalServices.getGuiUtilsApi().packTable( table );
					final ITableBox tableBox = generalServices.getGuiUtilsApi().createTableBox( table, dialog.getLayeredPane() );
					tableBox.getComponent().setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
					dialog.add( tableBox.getComponent() );
					
					// Close button
					final JPanel buttonsPanel = new JPanel();
					buttonsPanel.add( generalServices.getGuiUtilsApi().createCloseButton( dialog ) );
					dialog.add( buttonsPanel, BorderLayout.SOUTH );
					
					// And now show the Build orders table dialog
					dialog.setSize( 900, 600 );
					guiUtils.centerWindowToWindow( dialog, guiUtils.getMainFrame() );
					dialog.setVisible( true );
				}
			}
		} );
	}
	
	/**
	 * Builds the Build orders table.
	 * @param replay replay to build the Build orders table from
	 */
	@SuppressWarnings("serial")
	private JTable buildBoTable( final IReplay replay ) {
		
		// Rearrange players in respect to the user's favored player list
		replay.rearrangePlayers( generalServices.getInfoApi().getFavoredPlayerList() );
		final int[] teamOrderPlayerIndices = replay.getTeamOrderPlayerIndices();
		final int[] playerColumnIndices    = new int[ teamOrderPlayerIndices.length ];
		for ( int i = 0; i < teamOrderPlayerIndices.length; i++ )
			playerColumnIndices[ teamOrderPlayerIndices[ i ] ] = i + 1;
		
		final IconsApi icons = generalServices.getIconsApi();
		
		final GameSpeed      converterGameSpeed = replay.getConverterGameSpeed();
		final ReplayUtilsApi replayUtils        = generalServices.getReplayUtilsApi();
		
		// Custom cell renderer to display building icons
		final TableCellRenderer customRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column ) {
				super.getTableCellRendererComponent( table, value instanceof Integer ? replayUtils.formatFramesShort( (Integer) value, converterGameSpeed ) : value, isSelected, hasFocus, row, column );
				
				if ( value instanceof Building )
					setIcon( icons.getBuildingIcon( (Building) value, IconSize.MEDIUM ) );
				else
					setIcon( null );
				
				return this;
			}
		};
		final JTable table = new JTable() {
			@Override
			public boolean isCellEditable( final int row, final int column ) {
				return false;
			}
			@Override
			public TableCellRenderer getDefaultRenderer( final Class<?> columnClass ) {
				return customRenderer;
			}
		};
		final TableCellRenderer defaultInternalHeaderRenderer = table.getTableHeader().getDefaultRenderer();
		// Custom header cell renderer to display race icons
		table.getTableHeader().setDefaultRenderer( new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column ) {
				Component tableCellRendererComponent = defaultInternalHeaderRenderer.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
				final int modelColumn = table.convertColumnIndexToModel( column );
				if ( modelColumn > 0 )
					( (DefaultTableCellRenderer) tableCellRendererComponent ).setIcon( icons.getRaceIcon( replay.getPlayers()[ teamOrderPlayerIndices[ modelColumn - 1 ] ].getFinalRace() ) );
				return tableCellRendererComponent;
			};
		} );
		table.setRowHeight( icons.getBuildingIcon( Building.NEXUS, IconSize.MEDIUM ).getIconHeight() );
		table.setAutoCreateRowSorter( true );
		
		// Table header
		final int columns = replay.getPlayers().length + 1;
		final Vector< Object > headerVector = new Vector< Object >( columns );
		headerVector.add( generalServices.getLanguageApi().getText( "botplugin.time" ) );
		for ( final int playerIndex : teamOrderPlayerIndices )
			headerVector.add( replay.getPlayers()[ playerIndex ].getPlayerId().getName() );
		
		// Build actions
		final Vector< Vector< Object > > dataVector = new Vector< Vector< Object > >();
		
		Vector< Object > row     = null;  // Current row being constructed.
		String           rowTime = null;  // Time of the current row
		
		for ( final IAction action : replay.getGameEvents().getActions() )
			if ( action instanceof IBuildAction ) {
				final String newRowTime = replayUtils.formatFramesShort( action.getFrame(), converterGameSpeed );
				if ( !newRowTime.equals( rowTime ) ) {
					// Time is different, create a new row
					if ( row != null )
						dataVector.add( row );
					rowTime = newRowTime;
					row = createNewRow( columns, action.getFrame() );
				}
				
				if ( row.get( playerColumnIndices[ action.getPlayer() ] ) != null ) {
					// Cell is already occupied/taken, create a new row:
					dataVector.add( row );
					row = createNewRow( columns, action.getFrame() );
				}
				row.set( playerColumnIndices[ action.getPlayer() ] , ( (IBuildAction) action ).getBuilding() );
			}
		
		// Last row has to be added
		if ( row != null )
			dataVector.add( row );
		
		// Set the data vector to the table
		( (DefaultTableModel) table.getModel() ).setDataVector( dataVector, headerVector );
		( (TableRowSorter< ? extends TableModel >) table.getRowSorter() ).setComparator( 0, new Comparator< Integer >() {
			@Override
			public int compare( final Integer frame1, final Integer frame2 ) {
				return frame1.compareTo( frame2 );
			}
		} );
		
		return table;
	}
	
	/**
	 * Creates and initializes a new Build orders table row.
	 * @param columns number of table columns
	 * @param frame frame of the row
	 * @return a new and initialized row
	 */
	private static Vector< Object > createNewRow( final int columns, final int frame ) {
		final Vector< Object > row = new Vector< Object >( columns );
		row.add( frame );
		
		for ( int i = 1; i < columns; i++ )
			row.add( null );
		
		return row;
	}
	
	@Override
	public void destroy() {
		// Remove the registered replay ops popup menu item
		generalServices.getCallbackApi().removeReplayOpsPopupMenuItem( showBoTableItemHandler );
	}
	
}
