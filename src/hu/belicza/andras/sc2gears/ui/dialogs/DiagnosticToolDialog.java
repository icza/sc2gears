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
import hu.belicza.andras.sc2gears.services.Sc2RegMonitor;
import hu.belicza.andras.sc2gears.services.plugins.PluginControl;
import hu.belicza.andras.sc2gears.services.plugins.PluginManager;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.NormalThread;
import hu.belicza.andras.sc2gears.util.ObjectRegistry;
import hu.belicza.andras.sc2gears.util.TemplateEngine;
import hu.belicza.andras.sc2gearspluginapi.api.listener.DiagnosticTestFactory;
import hu.belicza.andras.sc2gearspluginapi.impl.DiagnosticTest;
import hu.belicza.andras.sc2gearspluginapi.impl.DiagnosticTest.Result;
import hu.belicza.andras.sc2gearspluginapi.impl.util.IntHolder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Diagnostic tool to test the settings of Sc2gears.
 *
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class DiagnosticToolDialog extends BaseDialog {
	
	/** Registry to handle diagnostic test factories. */
	private static final ObjectRegistry< DiagnosticTestFactory > diagnosticTestFactoryRegistry = new ObjectRegistry< DiagnosticTestFactory >();
	
	/**
	 * Adds a {@link DiagnosticTestFactory}.
	 * @param diagnosticTestFactory diagnostic test factory to be added
	 */
	public static void addDiagnosticTestFactory( final DiagnosticTestFactory diagnosticTestFactory ) {
		diagnosticTestFactoryRegistry.add( diagnosticTestFactory );
	}
	
	/**
	 * Removes a {@link DiagnosticTestFactory}.
	 * @param diagnosticTestFactory diagnostic task to be removed
	 */
	public static void removeDiagnosticTestFactory( final DiagnosticTestFactory diagnosticTestFactory ) {
		diagnosticTestFactoryRegistry.remove( diagnosticTestFactory );
	}
	
	/** Log text area. */
	private final JTextArea detailsArea = new JTextArea();
	
	/**
	 * Creates a new DiagnosticTool.
	 */
	public DiagnosticToolDialog() {
		super( "diagnosticTool.title", Icons.SYSTEM_MONITOR );
		
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		
		final JLabel statusLabel = new JLabel( Language.getText( "diagnosticTool.testsAreRunning" ) );
		GuiUtils.changeFontToItalic( statusLabel );
		statusLabel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 15, 0 ) );
		getContentPane().add( statusLabel, BorderLayout.NORTH );
		
		final JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted( true );
		progressBar.setString( "" );
		getContentPane().add( progressBar, BorderLayout.CENTER );
		( (JPanel) getContentPane() ).setBorder( BorderFactory.createEmptyBorder( 15, 15, 15, 15 ) );
		
		new NormalThread( "Diagnostic test runner" ) {
			@Override
			public void run() {
				final List< DiagnosticTest > diagnosticTestList = createDiagnosticTestList();
				final int testsCount = diagnosticTestList.size();
				
				progressBar.setMaximum( testsCount );
				
				// Build diagnostic table
				final String[] columnNameTextKeys = { "diagnosticTool.header.testName", "diagnosticTool.header.result", "diagnosticTool.header.details" };
				final Vector< String > columnNameVector = new Vector< String >( columnNameTextKeys.length );
				columnNameVector.add( "#" );
				for ( final String columnNameTextKey : columnNameTextKeys )
					columnNameVector.add( Language.getText( columnNameTextKey ) );
				final JTable testTable = GuiUtils.createNonEditableTable();
				final Vector< Object > dataVector = new Vector< Object >();
				
				Result worstResult = Result.OK;
				final Map< Result, IntHolder > resultCounterMap = new EnumMap< Result, IntHolder>( Result.class );
				IntHolder counter;
				for ( int i = 0; i < testsCount; i++ ) {
					progressBar.setValue( i );
					progressBar.setString( i + " / " + testsCount + " (" + ( 100 * i / testsCount ) + "%)" );
					
					final DiagnosticTest diagnosticTest = diagnosticTestList.get( i );
					
					try {
						diagnosticTest.execute();
						if ( diagnosticTest.result == null )
							throw new RuntimeException( "Result cannot be null!" );
					} catch ( final Throwable t ) {
						diagnosticTest.result  = Result.ERROR;
						diagnosticTest.details = Language.getText( "diagnosticTool.testExecutionError" );
					}
					
					if ( diagnosticTest.result.ordinal() < worstResult.ordinal() )
						worstResult = diagnosticTest.result;
					if ( ( counter = resultCounterMap.get( diagnosticTest.result ) ) == null )
						resultCounterMap.put( diagnosticTest.result, new IntHolder( 1 ) );
					else
						counter.value++;
					
					final Vector< Object > rowVector = new Vector< Object >();
					rowVector.add( i + 1 );
					rowVector.add( diagnosticTest.name );
					rowVector.add( diagnosticTest.result );
					rowVector.add( diagnosticTest.details );
					dataVector.add( rowVector );
				}
				
				final Result worstResult_ = worstResult;
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						setVisible( false );
						setDefaultCloseOperation( DISPOSE_ON_CLOSE );
						( (JPanel) getContentPane() ).setBorder( null );
						
						getContentPane().removeAll();
						
						final Box northBox = Box.createVerticalBox();
						northBox.setBorder( BorderFactory.createEmptyBorder( 15, 15, 0, 15 ) );
						
						final Box summaryBox = Box.createHorizontalBox();
						final JLabel summaryLabel = new JLabel( Language.getText( "diagnosticTool.summary" ) );
						GuiUtils.changeFontToBold( summaryLabel );
						summaryLabel.setFont( summaryLabel.getFont().deriveFont( 24f ) );
						summaryBox.add( summaryLabel );
						summaryBox.add( Box.createHorizontalStrut( 10 ) );
						final JLabel summaryResultLabel = new JLabel();
						GuiUtils.changeFontToBold( summaryResultLabel );
						summaryResultLabel.setFont( summaryResultLabel.getFont().deriveFont( 24f ) );
						summaryBox.add( summaryResultLabel );
						northBox.add( summaryBox );
						
						northBox.add( Box.createVerticalStrut( 5 ) );
						final Box summaryDetailsBox = Box.createHorizontalBox();
						northBox.add( summaryDetailsBox );
						
						getContentPane().add( northBox, BorderLayout.NORTH );
						
						final JPanel centerPanel = new JPanel( new BorderLayout() );
						centerPanel.setBorder( BorderFactory.createEmptyBorder( 15, 15, 15, 15 ) );
						
						summaryResultLabel.setIcon( worstResult_.iconBig );
						summaryResultLabel.setText( worstResult_.stringValue );
						final Result[] results = Result.values();
						IntHolder counter;
						for ( int i = results.length - 1; i >= 0; i-- )
							if ( ( counter = resultCounterMap.get( results[ i ] ) ) != null ) {
								if ( summaryDetailsBox.getComponentCount() > 0 )
									summaryDetailsBox.add( Box.createHorizontalStrut( 15 ) );
								final JLabel label = new JLabel( results[ i ].stringValue
										+ ":  " + counter.value + " / " + testsCount + "  ("
										+ ( counter.value * 100 / testsCount ) + "%)", results[ i ].icon, JLabel.LEFT );
								summaryDetailsBox.add( label );
							}
						
						testTable.setModel( new DefaultTableModel( dataVector, columnNameVector ) {
							public Class< ? > getColumnClass( final int columnIndex ) {
								if ( columnIndex == 2 )
									return Result.class;
								else
									return super.getColumnClass( columnIndex );
							};
						} );
						testTable.setDefaultRenderer( Result.class, new DefaultTableCellRenderer() {
							@Override
							public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column ) {
								final JLabel renderer = (JLabel) super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
								
								final Result result = (Result) value;
								renderer.setText( result.stringValue );
								renderer.setIcon( result.icon );
								
								return renderer;
							}
						} );
						testTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
						testTable.setPreferredScrollableViewportSize( new Dimension( 1000, 300 ) );
						testTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
							@Override
							public void valueChanged( final ListSelectionEvent event ) {
								final int selectedRow = testTable.getSelectedRow();
								if ( selectedRow < 0 ) {
									detailsArea.setText( "" );
								}
								else {
									final DiagnosticTest diagnosticTest = diagnosticTestList.get( selectedRow );
									detailsArea.setText(
													 columnNameVector.get( 0 ) + ": " + ( selectedRow + 1 )
											+ "\n" + columnNameVector.get( 1 ) + ": " + diagnosticTest.name
											+ "\n" + columnNameVector.get( 2 ) + ": " + diagnosticTest.result.stringValue
											+ "\n" + columnNameVector.get( 3 ) + ": " + ( diagnosticTest.details == null ? "" : diagnosticTest.details ) );
									detailsArea.setCaretPosition( 0 );
								}
							}
						} );
						GuiUtils.packTable( testTable, new int[] { 0, 1, 2 } );
						centerPanel.add( new JScrollPane( testTable ), BorderLayout.CENTER );
						
						// Diagnostic log text area
						detailsArea.setEditable( false );
						detailsArea.setLineWrap( true );
						detailsArea.setRows( 7 );
						final JScrollPane scrollPane = new JScrollPane( detailsArea );
						scrollPane.setBorder( BorderFactory.createTitledBorder( Language.getText( "diagnosticTool.detailsTitle" ) ) );
						centerPanel.add( scrollPane, BorderLayout.SOUTH );
						
						getContentPane().add( centerPanel, BorderLayout.CENTER );
						
						final JPanel buttonsPanel = new JPanel();
						buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 0, 15, 10, 15 ) );
						final JButton closeButton = createCloseButton( "button.close" );
						buttonsPanel.add( closeButton );
						getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
						
						// "Pack" and show
						packAndShow( closeButton, false );
					}
				} );
				
			}
		}.start();
		
		// "Pack" and show
		pack();
		GuiUtils.centerWindowToWindow( this, (Window) parent );
		setVisible( true );
	}
	
	/**
	 * Creates and returns a list of diagnostic tests.
	 * @return a list of diagnostic tests
	 */
	private List< DiagnosticTest > createDiagnosticTestList() {
		final List< DiagnosticTest > diagnosticTestList = new ArrayList< DiagnosticTest >();
		
		// Check memory for Sc2gears
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkMemory.name", Consts.APPLICATION_NAME ) ) {
			@Override
			public void execute() {
				final Runtime runtime = Runtime.getRuntime();
				final long allocated   = runtime.totalMemory();
				final long free        = runtime.freeMemory();
				final long max         = runtime.maxMemory();
				final long totalFreeMB = ( free + ( max - allocated ) ) >> 20;
				
				final int failTotalFreeMBLimit    = 10;
				final int warningTotalFreeMBLimit = 50;
				
				if ( totalFreeMB < failTotalFreeMBLimit ) {
					result  = Result.FAIL;
					details = Language.getText( "diagnosticTool.test.checkMemory.fail", failTotalFreeMBLimit, Consts.APPLICATION_NAME );
				}
				else if ( totalFreeMB < warningTotalFreeMBLimit ) {
					result  = Result.WARNING;
					details = Language.getText( "diagnosticTool.test.checkMemory.warning", warningTotalFreeMBLimit, Consts.APPLICATION_NAME );
				}
				else {
					result  = Result.OK;
				}
				final String memoryValuesString = Language.getText( "diagnosticTool.test.checkMemory.memoryValues", allocated >> 20, free >> 20, max >> 20, totalFreeMB );
				details = details == null ? memoryValuesString : details + " (" + memoryValuesString + ")";
			}
		} );
		
		// Check Java
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkJavaVersion.name" ) ) {
			@Override
			public void execute() {
				final String javaVersion = System.getProperty( "java.version" );
				final int major, minor;
				try {
					final int dotIndex = javaVersion.indexOf( '.' );
					major = Integer.parseInt( javaVersion.substring( 0, dotIndex ) );
					minor = Integer.parseInt( javaVersion.substring( dotIndex + 1, javaVersion.indexOf( '.', dotIndex + 1 ) ) );
				} catch ( final Exception e ) {
					result  = Result.ERROR;
					details = Language.getText( "diagnosticTool.test.checkJavaVersion.error" );
					return;
				}
				
				final String detectedJavaVersion = javaVersion + " - " + System.getProperty( "java.vendor" );
				// Required Java version: 7.0 (1.7.0) or newer
				if ( major >= 7 || major == 1 && minor >= 7 || major >= 2  ) {
					result  = Result.OK;
					details = detectedJavaVersion;
				}
				else {
					result  = Result.FAIL;
					details = Language.getText( "diagnosticTool.test.checkJavaVersion.fail2", "7.0 (1.7.0)", detectedJavaVersion );
				}
			}
		} );
		
		// Check Auto-check updates setting
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkAutoCheckUpdatesSetting.name" ) ) {
			@Override
			public void execute() {
				if ( Settings.getBoolean( Settings.KEY_SETTINGS_CHECK_UPDATES_ON_STARTUP ) ) {
					result  = Result.OK;
					details = Language.getText( "diagnosticTool.enabled" );
				}
				else {
					result  = Result.WARNING;
					details = Language.getText( "diagnosticTool.test.checkAutoCheckUpdatesSetting.warning", Consts.APPLICATION_NAME );
				}
			}
		} );
		
		// Check Sc2gears User Content folders
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkUserContentFolders.name", Consts.APPLICATION_NAME ) ) {
			@Override
			public void execute() {
				for ( final String folder : Consts.USER_CONTENT_FOLDERS ) {
					final File file = new File( folder );
					if ( !checkFileExist( file ) )
						break;
					if ( !checkFolderWritePermission( file ) )
						break;
				}
				
				if ( result == null )
					if ( checkFreeSpace( new File( Consts.USER_CONTENT_FOLDERS[ 0 ] ) ) )
						result = Result.OK;
			}
		} );
		
		// Check SC2 install folder
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkSc2InstallFolder.name" ) ) {
			@Override
			public void execute() {
				final File launcherFile = new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_SC2_INSTALLATION ), GeneralUtils.isMac() ? "StarCraft II.app/Contents/MacOS/StarCraft II" : "StarCraft II.exe" );
				
				if ( checkFileExist( launcherFile ) )
					result = Result.OK;
			}
		} );
		
		// Check SC2 auto-replay folder
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkSc2AutoRepFolder.name" ) ) {
			@Override
			public void execute() {
				final List< File > autoRepFolderList = GeneralUtils.getAutoRepFolderList();
				
				int existsCount = 0;
				details = "";
				for ( final File autoRepFolder : autoRepFolderList )
					if ( autoRepFolder.exists() )
						existsCount++;
					else {
						if ( details.length() > 0 )
							details += ", ";
						details += '"' + autoRepFolder.getAbsolutePath() + '"';
					}
				
				result = existsCount == 0 ? Result.FAIL : existsCount == autoRepFolderList.size() ? Result.OK : Result.WARNING;
				if ( result == Result.FAIL )
					details = Language.getText( "diagnosticTool.test.checkSc2AutoRepFolder.fail" ) + " (" + details + ")";
				else if ( result == Result.WARNING )
					details = Language.getText( "diagnosticTool.test.checkSc2AutoRepFolder.warning" ) + " (" + details + ")";
			}
		} );
		
		// Check SC2 maps folder
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkSc2MapsFolder.name" ) ) {
			@Override
			public void execute() {
				final File mapsFolder = new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_SC2_MAPS ) ); 
				
				if ( checkFileExist( mapsFolder ) )
					result = Result.OK;
			}
		} );
		
		// Check replay auto-save folder
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkReplayAutoSaveFolder.name" ) ) {
			@Override
			public void execute() {
				final File replayAutoSaveFolder = new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_REPLAY_AUTO_SAVE ) );
				
				if ( checkFileExist( replayAutoSaveFolder ) )
					if ( checkFolderWritePermission( replayAutoSaveFolder ) )
						if ( checkFreeSpace( replayAutoSaveFolder ) )
							result = Result.OK;
			}
		} );
		
		// Check mouse print output folder
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkMousePrintOutputFolder.name" ) ) {
			@Override
			public void execute() {
				final File mousePrintOutputFolder = new File( Settings.getString( Settings.KEY_SETTINGS_FOLDER_MOUSE_PRINT_OUTPUT ) );
				
				if ( checkFileExist( mousePrintOutputFolder ) )
					if ( checkFolderWritePermission( mousePrintOutputFolder ) )
						if ( checkFreeSpace( mousePrintOutputFolder ) )
							result = Result.OK;
			}
		} );
		
		// Check replay auto-save name template
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkReplayAutoSaveNameTemplate.name" ) ) {
			@Override
			public void execute() {
				try {
					new TemplateEngine( Settings.getString( Settings.KEY_SETTINGS_MISC_REP_AUTO_SAVE_NAME_TEMPLATE ) );
					result = Result.OK;
				} catch ( final Exception e ) {
					result  = Result.WARNING;
					details = Language.getText( "diagnosticTool.test.checkReplayAutoSaveNameTemplate.warning" );
				}
			}
		} );
		
		// Check internal replay cache setting
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkInternalReplayCacheSetting.name" ) ) {
			@Override
			public void execute() {
				if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_CACHE_PREPROCESSED_REPLAYS ) ) {
					result  = Result.OK;
					details = Language.getText( "diagnosticTool.enabled" );
				}
				else {
					result  = Result.WARNING;
					details = Language.getText( "diagnosticTool.test.checkInternalReplayCacheSetting.warning" );
				}
			}
		} );
		
		// Check replay auto-save setting
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkReplayAutoSaveSetting.name" ) ) {
			@Override
			public void execute() {
				if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_REPLAY_AUTO_SAVE ) ) {
					result  = Result.OK;
					details = Language.getText( "diagnosticTool.enabled" );
				}
				else {
					result  = Result.WARNING;
					details = Language.getText( "diagnosticTool.test.checkReplayAutoSaveSetting.warning" );
				}
			}
		} );
		
		// Check mouse print auto-save (along with APM Alert)
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkMousePrintAutoSave.name" ) ) {
			@Override
			public void execute() {
				if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_SAVE_MOUSE_PRINTS ) ) {
					if ( Sc2RegMonitor.supported ) {
						result  = Result.OK;
						details = Language.getText( "diagnosticTool.enabled" );
					}
					else {
						result  = Result.WARNING;
						details = Language.getText( "diagnosticTool.test.checkMousePrintAutoSave.warning#2" );
					}
				}
				else {
					if ( Sc2RegMonitor.supported ) {
						result  = Result.WARNING;
						details = Language.getText( "diagnosticTool.test.checkMousePrintAutoSave.disabled" );
					}
					else {
						result  = Result.OK;
						details = Language.getText( "diagnosticTool.disabled" );
					}
				}
			}
		} );
		
		// Check replay auto-store setting (along with replay auto-save)
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkReplayAutoStoreSetting.name" ) ) {
			@Override
			public void execute() {
				if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_AUTO_STORE_NEW_REPLAYS ) ) {
					if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_REPLAY_AUTO_SAVE ) ) {
						result  = Result.OK;
						details = Language.getText( "diagnosticTool.test.checkReplayAutoStoreSetting.ok" );
					}
					else {
						result  = Result.FAIL;
						details = Language.getText( "diagnosticTool.test.checkReplayAutoStoreSetting.fail" );
					}
				}
				else {
					if ( Settings.getString( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY ).length() > 0 ) {
						result  = Result.WARNING;
						details = Language.getText( "diagnosticTool.test.checkReplayAutoStoreSetting.warning" );
					}
					else {
						result  = Result.OK;
						details = Language.getText( "diagnosticTool.disabled" );
					}
				}
			}
		} );
		
		// Check mouse print auto-store setting (along with mouse print auto-save)
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkMousePrintAutoStoreSetting.name" ) ) {
			@Override
			public void execute() {
				if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_STORE_MOUSE_PRINTS ) ) {
					if ( Settings.getBoolean( Settings.KEY_SETTINGS_MISC_SAVE_MOUSE_PRINTS ) ) {
						result  = Result.OK;
						details = Language.getText( "diagnosticTool.test.checkMousePrintAutoStoreSetting.ok" );
					}
					else {
						result  = Result.FAIL;
						details = Language.getText( "diagnosticTool.test.checkMousePrintAutoStoreSetting.fail" );
					}
				}
				else {
					if ( Settings.getString( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY ).length() > 0 ) {
						result  = Result.WARNING;
						details = Language.getText( "diagnosticTool.test.checkMousePrintAutoStoreSetting.warning" );
					}
					else {
						result  = Result.OK;
						details = Language.getText( "diagnosticTool.disabled" );
					}
				}
			}
		} );
		
		// Check Authorization key
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkAuthorizationKey.name" ) ) {
			@Override
			public void execute() {
				final String authorizationKey = Settings.getString( Settings.KEY_SETTINGS_MISC_AUTHORIZATION_KEY );
				if ( authorizationKey.length() == 0 ) {
					result  = Result.OK;
					details = Language.getText( "diagnosticTool.test.checkAuthorizationKey.notSet" );
				} else {
					final Boolean valid = GeneralUtils.checkAuthorizationKey( authorizationKey );
					if ( valid == null ) {
						result  = Result.WARNING;
						details = Language.getText( "diagnosticTool.test.checkAuthorizationKey.warning" );
					} else if ( valid.booleanValue() ) {
						result  = Result.OK;
						details = Language.getText( "diagnosticTool.test.checkAuthorizationKey.ok" );
					} else {
						result  = Result.FAIL;
						details = Language.getText( "diagnosticTool.test.checkAuthorizationKey.fail" );
					}
				}
			}
		} );
		
		// Check plugins
		diagnosticTestList.add( new DiagnosticTest( Language.getText( "diagnosticTool.test.checkPlugins.name" ) ) {
			@Override
			public void execute() {
				int enabledCounter = 0;
				int failedCounter  = 0;
				for ( final PluginControl pluginControl : PluginManager.getPluginControlVector() ) {
					if ( !pluginControl.isDisabled() ) {
						enabledCounter++;
						if ( pluginControl.isError() )
							failedCounter++;
					}
				}
				
				if ( failedCounter == 0 ) {
					result  = Result.OK;
					details = enabledCounter == 0 ? Language.getText( "diagnosticTool.test.checkPlugins.noEnabledPlugins" )
							: Language.getText( "diagnosticTool.test.checkPlugins.ok", enabledCounter );
				}
				else {
					result  = Result.FAIL;
					details = Language.getText( "diagnosticTool.test.checkPlugins.fail", failedCounter, enabledCounter );
				}
			}
		} );
		
		// Add external diagnostic tests
		synchronized ( diagnosticTestFactoryRegistry ) {
			for ( final DiagnosticTestFactory diagnosticTestFactory : diagnosticTestFactoryRegistry )
				diagnosticTestList.add( diagnosticTestFactory.createDiagnosticTest() );
		}
		
		return diagnosticTestList;
	}
	
}
