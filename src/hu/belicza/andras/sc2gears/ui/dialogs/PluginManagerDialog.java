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
import hu.belicza.andras.sc2gears.services.plugins.PluginControl;
import hu.belicza.andras.sc2gears.services.plugins.PluginManager;
import hu.belicza.andras.sc2gears.services.plugins.PluginControl.Status;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.components.BaseLabelListCellRenderer;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gears.util.Task;
import hu.belicza.andras.sc2gearspluginapi.Configurable;
import hu.belicza.andras.sc2gearspluginapi.PluginDescriptor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 * Plugin manager dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class PluginManagerDialog extends BaseDialog {
	
	/**
	 * Creates a new PluginManagerDialog.
	 */
	public PluginManagerDialog() {
		this( null );
	}
	
	/**
	 * Creates a new PluginManagerDialog.
	 * @param pluginToSelect plugin to select
	 */
	public PluginManagerDialog( final PluginControl pluginToSelect ) {
		super( "pluginManager.title", Icons.PUZZLE );
		
		setModal( false );
		
		final JPanel northPanel = new JPanel( new BorderLayout() );
		northPanel.setBorder( BorderFactory.createEmptyBorder( 5, 10, 10, 10 ) );
		final JPanel globalControlPanel = new JPanel();
		final JButton enableAllPluginsButton = new JButton( Icons.PLUG_CONNECT );
		GuiUtils.updateButtonText( enableAllPluginsButton, "pluginManager.enableAllPluginsButton" );
		globalControlPanel.add( enableAllPluginsButton );
		final JButton disableAllPluginsButton = new JButton( Icons.PLUG_DISCCONNECT );
		GuiUtils.updateButtonText( disableAllPluginsButton, "pluginManager.disableAllPluginsButton" );
		globalControlPanel.add( disableAllPluginsButton );
		northPanel.add( globalControlPanel, BorderLayout.CENTER );
		final JPanel infoPanel = new JPanel();
		final JCheckBox allowIncompatiblePlugins = GuiUtils.createCheckBox( "pluginManager.allowIncompatiblePlugins", Settings.KEY_PLUGIN_MANAGER_ALLOW_INCOMPATIBLE_PLUGINS );
		infoPanel.add( allowIncompatiblePlugins );
		infoPanel.add( Box.createHorizontalStrut( 20 ) );
		infoPanel.add( GeneralUtils.createLinkLabel( Language.getText( "pluginManager.moreAboutPluginInterface" ), Consts.URL_PLUGIN_INTERFACE ) );
		northPanel.add( infoPanel, BorderLayout.SOUTH );
		getContentPane().add( northPanel, BorderLayout.NORTH );
		
		final JPanel pluginsPanel = new JPanel( new BorderLayout() );
		pluginsPanel.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 10 ) );
		
		final JPanel pluginsListAndDetailsPanel = new JPanel( new BorderLayout() );
		final Vector< PluginControl > pluginControlVector = PluginManager.getPluginControlVector();
		final JList< PluginControl > pluginsList = new JList<>( pluginControlVector );
		if ( pluginToSelect != null )
			pluginsList.setSelectedValue( pluginToSelect, true );
		pluginsList.setOpaque( false );
		pluginsList.setCellRenderer( new BaseLabelListCellRenderer< PluginControl >( 2, pluginsList ) {
			@Override
			public Icon getIcon( final PluginControl value ) {
				return value.status.icon;
			}
		} );
		pluginsList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		JScrollPane scrollPane = new JScrollPane( pluginsList );
		scrollPane.setBorder( BorderFactory.createTitledBorder( Language.getText( "pluginManager.installedPlugins" ) ) );
		scrollPane.setPreferredSize( new Dimension( 250, 10 ) );
		pluginsListAndDetailsPanel.add( scrollPane, BorderLayout.CENTER );
		final JPanel detailsPanel = new JPanel( new BorderLayout() );
		detailsPanel.setBorder( BorderFactory.createTitledBorder( Language.getText( "pluginManager.pluginDetails" ) ) );
		final JTable detailsTable = new JTable() {
			@Override
			public boolean isCellEditable( final int row, final int column ) {
				return false;
			}
			@Override
			public String getToolTipText( final MouseEvent event ) {
				// Tool tip for the home page link cell
				if ( pluginsList.getSelectedIndex() >= 0 && isEventOnHomePageLinkCell( this, event ) )
					return Language.getText( "pluginManager.details.visitPluginHomePageToolTip" );
				
				return super.getToolTipText( event );
			}
		};
		final MouseAdapter detailsTableMouseAdapter = new MouseAdapter() {
			@Override
			public void mouseMoved( final MouseEvent event ) {
				// Hand cursor for the home page link cell
				final PluginControl pluginControl = (PluginControl) pluginsList.getSelectedValue();
				final boolean hasLink = pluginControl != null && !pluginControl.pluginDescriptor.getHomePage().isEmpty();
				detailsTable.setCursor( hasLink && isEventOnHomePageLinkCell( detailsTable, event ) ? Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) : null );
			}
			@Override
			public void mousePressed( final MouseEvent event  ) {
				// Open home page if clicked on the Hand cursor for the home page link cell
				final PluginControl pluginControl = (PluginControl) pluginsList.getSelectedValue();
				if ( pluginControl != null && !pluginControl.pluginDescriptor.getHomePage().isEmpty()
					&& isEventOnHomePageLinkCell( detailsTable, event ) )
					GeneralUtils.showURLInBrowser( pluginControl.pluginDescriptor.getHomePage() );
			}
		};
		detailsTable.addMouseListener      ( detailsTableMouseAdapter );
		detailsTable.addMouseMotionListener( detailsTableMouseAdapter );
		final Object[][] detailsData = new Object[][] {
			// Revise isEventOnHomePageCell() if new rows/columns are added!
			{ Language.getText( "pluginManager.details.name"        ), null },
			{ Language.getText( "pluginManager.details.author"      ), null },
			{ Language.getText( "pluginManager.details.email"       ), null },
			{ Language.getText( "pluginManager.details.version"     ), null },
			{ Language.getText( "pluginManager.details.releaseDate" ), null },
			{ Language.getText( "pluginManager.details.apiVersion"  ), null },
			{ Language.getText( "pluginManager.details.homePage"    ), null }
		};
		( (DefaultTableModel) detailsTable.getModel() ).setDataVector( detailsData, new Object[] { "Property", "Value" } );
		GuiUtils.packTable( detailsTable, new int[] { 0 } );
		detailsPanel.add( new JScrollPane( GuiUtils.wrapInBorderPanel( detailsTable ) ), BorderLayout.CENTER );
		final JPanel pluginControlAndStatusPanel = new JPanel( new BorderLayout() );
		final JPanel pluginCompatibilityInfoWrapper = new JPanel( new FlowLayout( FlowLayout.CENTER, 8, 8 ) );
		final JLabel pluginCompatibilityInfoLabel = new JLabel();
		GuiUtils.changeFontToBold( pluginCompatibilityInfoLabel );
		pluginCompatibilityInfoWrapper.add( pluginCompatibilityInfoLabel );
		pluginControlAndStatusPanel.add( pluginCompatibilityInfoWrapper, BorderLayout.NORTH );
		final JPanel pluginControlPanel = new JPanel();
		final JButton enablePluginButton = new JButton( Icons.PLUG_CONNECT );
		GuiUtils.updateButtonText( enablePluginButton, "pluginManager.details.enablePluginButton" );
		pluginControlPanel.add( enablePluginButton );
		final JButton disablePluginButton = new JButton( Icons.PLUG_DISCCONNECT );
		GuiUtils.updateButtonText( disablePluginButton, "pluginManager.details.disablePluginButton" );
		pluginControlPanel.add( disablePluginButton );
		final Holder< JButton > configurePluginButtonHolder = new Holder< JButton >();
		final JButton configurePluginButton = new JButton( Icons.WRENCH ) {
			// Override paint so the background will be "forced" even on native platform LaFs
			@Override
			public void paint( final Graphics graphics ) {
				if ( configurePluginButtonHolder.value.getBackground() != null ) {
					graphics.setColor( configurePluginButtonHolder.value.getBackground() );
					graphics.fillRect( 0, 0, configurePluginButtonHolder.value.getSize().width, configurePluginButtonHolder.value.getSize().height );
				}
				super.paint( graphics );
			}
		};
		configurePluginButtonHolder.value = configurePluginButton;
		GuiUtils.updateButtonText( configurePluginButton, "pluginManager.details.configurePluginButton" );
		pluginControlPanel.add( configurePluginButton );
		pluginControlAndStatusPanel.add( pluginControlPanel, BorderLayout.CENTER );
		final JPanel pluginStatusPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 10, 10 ) );
		final JLabel pluginStatusLabel = new JLabel();
		pluginStatusLabel.setFont( pluginStatusLabel.getFont().deriveFont( Font.BOLD, pluginStatusLabel.getFont().getSize() + 3f ) );
		pluginStatusPanel.add( pluginStatusLabel );
		final JLabel errorDetailsLinkLabel = GuiUtils.createErrorDetailsLink();
		pluginStatusPanel.add( errorDetailsLinkLabel );
		pluginControlAndStatusPanel.add( pluginStatusPanel, BorderLayout.SOUTH );
		detailsPanel.add( pluginControlAndStatusPanel, BorderLayout.SOUTH );
		pluginsListAndDetailsPanel.add( detailsPanel, BorderLayout.EAST );
		pluginsPanel.add( pluginsListAndDetailsPanel, BorderLayout.NORTH );
		
		final JEditorPane pluginDescriptionPane = GuiUtils.createEditorPane();
		pluginDescriptionPane.setPreferredSize( new Dimension( 10, 150 ) );
		pluginDescriptionPane.setContentType( "text/html" ); // To get rid of the scroll bar that appears when resized
		scrollPane = new JScrollPane( pluginDescriptionPane );
		scrollPane.setBorder( BorderFactory.createTitledBorder( Language.getText( "pluginManager.pluginDescription" ) ) );
		pluginsPanel.add( scrollPane, BorderLayout.CENTER );
		
		final Task< Boolean > configurePluginButtonUpdaterTask = new Task< Boolean >() {
			private Timer   blinkTimer;
			private boolean inverted;
			@Override
			public synchronized void execute( final Boolean actionIsRequired ) {
				if ( blinkTimer != null ) {
					if ( actionIsRequired )
						inverted = !inverted;
					blinkTimer.cancel();
					blinkTimer = null;
				}
				else
					if ( actionIsRequired )
						inverted = false;
				
				configurePluginButton.setBackground( actionIsRequired ? ( inverted ? Color.YELLOW : Color.RED    ) : null );
				configurePluginButton.setForeground( actionIsRequired ? ( inverted ? Color.RED    : Color.YELLOW ) : null );
				configurePluginButton.setContentAreaFilled( !actionIsRequired );
				configurePluginButton.setToolTipText( actionIsRequired ? Language.getText( "pluginManager.details.configurePluginButtonActionRequiredToolTip" ) : null );
				
				if ( actionIsRequired && isDisplayable() ) // Stop blinking if dialog is closed..
					( blinkTimer = new Timer() ).schedule( new TimerTask() {
						@Override
						public void run() {
							execute( true );
						}
					}, inverted ? 300 : 500 );
			}
		};
		
		final ListSelectionListener pluginsListSelectionListener = new ListSelectionListener() {
			@Override
			public void valueChanged( final ListSelectionEvent event ) {
				if ( event != null && event.getValueIsAdjusting() )
					return;
				final PluginControl pluginControl = (PluginControl) pluginsList.getSelectedValue();
				if ( pluginControl == null ) {
					pluginCompatibilityInfoLabel.setIcon( null );
					pluginCompatibilityInfoLabel.setText( " " );
					pluginCompatibilityInfoLabel.setForeground( null );
					enablePluginButton   .setEnabled( false );
					disablePluginButton  .setEnabled( false );
					configurePluginButton.setEnabled( false );
					configurePluginButtonUpdaterTask.execute( Boolean.FALSE );
					pluginStatusLabel.setText( Language.getText( "pluginManager.details.selectPlugin" ) );
					pluginStatusLabel.setIcon( null );
					pluginStatusLabel.setForeground( null );
					errorDetailsLinkLabel.setVisible( false );
					for ( int i = 0; i < detailsData.length; i++ )
						detailsData[ i ][ 1 ] = null;
					pluginDescriptionPane.setContentType( "text/html" ); // To get rid of the scroll bar that appears when resized
					pluginDescriptionPane.setText( null );
				}
				else {
					pluginCompatibilityInfoLabel.setIcon( pluginControl.isCompatible ? Icons.TICK_SHIELD : Icons.CROSS_SHIELD );
					pluginCompatibilityInfoLabel.setText( Language.getText( pluginControl.isCompatible ? "pluginManager.details.pluginIsCompatible" : "pluginManager.details.pluginIsIncompatible" ) );
					pluginCompatibilityInfoLabel.setForeground( pluginControl.isCompatible ? new Color( 0, 150, 0 ) : Color.RED );
					enablePluginButton   .setEnabled(  pluginControl.isDisabled() );
					disablePluginButton  .setEnabled( !pluginControl.isDisabled() );
					configurePluginButton.setEnabled( !pluginControl.isDisabled() && pluginControl.plugin instanceof Configurable );
					configurePluginButtonUpdaterTask.execute( pluginControl.plugin instanceof Configurable && ( (Configurable) pluginControl.plugin ).isActionRequired() );
					pluginStatusLabel.setIcon( pluginControl.status.icon );
					pluginStatusLabel.setText( pluginControl.status.stringValue );
					pluginStatusLabel.setForeground( pluginControl.status == Status.ENABLED_RUNNING ? new Color( 0, 150, 0 )
						: pluginControl.isError() ? Color.RED : null );
					errorDetailsLinkLabel.setVisible( pluginControl.isError() );
					final PluginDescriptor pluginDescriptor = pluginControl.pluginDescriptor;
					int row = 0;
					detailsData[ row++ ][ 1 ] = pluginDescriptor.getName();
					detailsData[ row++ ][ 1 ] = Language.formatPersonName( pluginDescriptor.getAuthorFirstName(), pluginDescriptor.getAuthorLastName() );
					detailsData[ row++ ][ 1 ] = pluginDescriptor.getAuthorEmail();
					detailsData[ row++ ][ 1 ] = pluginDescriptor.getVersion();
					detailsData[ row++ ][ 1 ] = Language.formatDate( pluginDescriptor.getReleaseDate() );
					detailsData[ row++ ][ 1 ] = pluginDescriptor.getApiVersion();
					detailsData[ row++ ][ 1 ] = "<html><a href='#'>" + pluginDescriptor.getHomePage() + "</a></html>";
					pluginDescriptionPane.setContentType( pluginDescriptor.isHtmlDescription() ? "text/html" : "text/plain" );
					pluginDescriptionPane.setText( pluginDescriptor.getDescription() );
					pluginDescriptionPane.setCaretPosition( 0 );
				}
				( (DefaultTableModel) detailsTable.getModel() ).setDataVector( detailsData, new Object[] { "Property", "Value" } );
				GuiUtils.packTable( detailsTable, new int[] { 0 } );
			}
		};
		pluginsList.addListSelectionListener( pluginsListSelectionListener );
		// Init component states and values
		pluginsListSelectionListener.valueChanged( null );
		
		final ActionListener enablerDisablerListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				if ( event.getSource() == enableAllPluginsButton ) {
					for ( final PluginControl pluginControl : pluginControlVector ) {
						pluginControl.startPlugin();
					}
				} else if ( event.getSource() == disableAllPluginsButton ) {
					for ( final PluginControl pluginControl : pluginControlVector ) {
						pluginControl.disposePlugin();
						pluginControl.status = pluginControl.isCompatible ? Status.DISABLED : Status.DISABLED_INCOMPATIBLE;
					}
				} else if ( event.getSource() == enablePluginButton ) {
					( (PluginControl) pluginsList.getSelectedValue() ).startPlugin();
				} else if ( event.getSource() == disablePluginButton ) {
					final PluginControl pluginControl = ( (PluginControl) pluginsList.getSelectedValue() );
					pluginControl.disposePlugin();
					pluginControl.status = pluginControl.isCompatible ? Status.DISABLED : Status.DISABLED_INCOMPATIBLE;
				}
				
				// Store the set of new enabled plugins
				final Set< String > enabledPluginSet = new HashSet< String >();
				for ( final PluginControl pluginControl : pluginControlVector )
					if ( !pluginControl.isDisabled() )
						enabledPluginSet.add( pluginControl.pluginDescriptor.getMainClass() );
				Settings.setEnabledPluginSet( enabledPluginSet );
				
				// Refresh icons in plugin list
				pluginsList.repaint();
				// Refresh plugin status
				pluginsListSelectionListener.valueChanged( null );
			}
		};
		enableAllPluginsButton .addActionListener( enablerDisablerListener );
		disableAllPluginsButton.addActionListener( enablerDisablerListener );
		enablePluginButton     .addActionListener( enablerDisablerListener );
		disablePluginButton    .addActionListener( enablerDisablerListener );
		configurePluginButton  .addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				// It's safe to cast because if the plugin is not "Configurable", the button is disabled and we won't end up here
				final PluginControl pluginControl = (PluginControl) pluginsList.getSelectedValue();
				new PluginSettingsDialog( PluginManagerDialog.this, pluginControl, new Runnable() {
					@Override
					public void run() {
						configurePluginButtonUpdaterTask.execute( ( (Configurable) pluginControl.plugin ).isActionRequired() );
					}
				} );
			}
		} );
		
		getContentPane().add( pluginsPanel, BorderLayout.CENTER );
		
		final JButton closeButton = createCloseButton( "button.close" );
		final JPanel closePanel = GuiUtils.wrapInPanel( closeButton );
		closePanel.setBorder( BorderFactory.createEmptyBorder( 2, 10, 5, 10 ) );
		getContentPane().add( closePanel, BorderLayout.SOUTH );
		
		packAndShow( closeButton, false );
	}
	
	/**
	 * Tells if the specified event happened on the home page link cell.
	 * @param detailsTable reference to the details table
	 * @param event        event to examine
	 * @return true if the event happened on the home page link cell; false otherwise
	 */
	private static boolean isEventOnHomePageLinkCell( final JTable detailsTable, final MouseEvent event ) {
		final Point point = event.getPoint();
		return detailsTable.rowAtPoint( point ) == 6 && detailsTable.columnAtPoint( point ) == 1;
	}
	
}
