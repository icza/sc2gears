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

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.sc2replay.model.GameEvents.Action;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.dialogs.ProgressDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.ReplayRenameDialog;
import hu.belicza.andras.sc2gears.ui.dialogs.ShareReplaysDialog;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.NormalThread;
import hu.belicza.andras.sc2gears.util.ObjectRegistry;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayOpCallback;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayOpsPopupMenuItemListener;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * Replay operations popup menu.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class ReplayOperationsPopupMenu extends JPopupMenu implements ActionListener {
	
	/**
	 * Custom replay ops menu item specification.
	 * @author Andras Belicza
	 */
	private static class CustomReplayOpsMenuItemSpec extends CustomMenuItemSpec {
		
		/** Listener to be called when action is performed. */
		public final ReplayOpsPopupMenuItemListener listener;
		
		/**
		 * Creates a new CustomReplayOpsMenuItemSpec.
		 * @param text     text of the custom menu item spec
		 * @param icon     optional icon of the custom menu item spec
		 * @param listener listener to be called when action is performed
		 */
		public CustomReplayOpsMenuItemSpec( final String text, final Icon icon, final ReplayOpsPopupMenuItemListener listener ) {
			super( text, icon );
			this.listener = listener;
		}
		
	}
	
	/** Custom replay ops menu item specifications registry. */
	private static final ObjectRegistry< CustomReplayOpsMenuItemSpec > customMenuItemRegistry = new ObjectRegistry< CustomReplayOpsMenuItemSpec >();
	
	/**
	 * Adds a new replay ops popup menu item.
	 * @param text     text of the new menu item
	 * @param icon     optional icon of the new menu item
	 * @param listener listener to be called when the menu item is activated
	 * @return a handler that can be used to remove the registered menu item
	 */
	public static Integer addReplayOpsPopupMenuItem( final String text, final Icon icon, final ReplayOpsPopupMenuItemListener listener ) {
		final CustomReplayOpsMenuItemSpec customReplayOpsMenuItemSpec = new CustomReplayOpsMenuItemSpec( text, icon, listener );
		
		customMenuItemRegistry.add( customReplayOpsMenuItemSpec );
		
		return customReplayOpsMenuItemSpec.handler;
	}
	
	/**
	 * Removes a replay ops popup menu item specified by its handler.
	 * @param handler handler of the popup menu item to be removed
	 */
	public static void removeReplayOpsPopupMenuItem( final Integer handler ) {
		synchronized ( customMenuItemRegistry ) {
			for ( final CustomReplayOpsMenuItemSpec customReplayOpsMenuItemSpec : customMenuItemRegistry )
				if ( customReplayOpsMenuItemSpec.handler.equals( handler ) ) {
					customMenuItemRegistry.remove( customReplayOpsMenuItemSpec );
					break;
				}
		}
	}
	
	/** Open in analyzer menu item.              */
	private final JMenuItem openInAnalyzerMenuItem     = new JMenuItem( Language.getText( "replayops.openInAnalyzer"     ), Icons.CHART );
	/** Open in multi-replay analysis menu item. */
	private final JMenuItem openInMultiRepAnalMenuItem = new JMenuItem( Language.getText( "replayops.openInMultiRepAnal" ), Icons.CHART_UP_COLOR );
	/** Watch replay menu item.                  */
	private final JMenuItem watchReplayMenuItem        = new JMenuItem( Language.getText( "replayops.watchReplay"        ), Icons.WATCH_REPLAY );
	/** Copy replays menu item.                  */
	private final JMenuItem copyReplaysMenuItem        = new JMenuItem( Language.getText( "replayops.copyReplays"        ), Icons.DOCUMENT_COPY );
	/** Move replays menu item.                  */
	private final JMenuItem moveReplaysMenuItem        = new JMenuItem( Language.getText( "replayops.moveReplays"        ), Icons.DOCUMENT_EXPORT );
	/** Pack replays menu item.                  */
	private final JMenuItem packReplaysMenuItem        = new JMenuItem( Language.getText( "replayops.packReplays"        ), Icons.DOCUMENT_ZIPPER );
	/** Rename replays menu item.                */
	private final JMenuItem renameReplaysMenuItem      = new JMenuItem( Language.getText( "replayops.renameReplays"      ), Icons.DOCUMENT_RENAME );
	/** Delete replays menu item.                */
	public  final JMenuItem deleteReplaysMenuItem      = new JMenuItem( Language.getText( "replayops.deleteReplays"      ), Icons.CROSS );
	/** Open replays' folder menu item.          */
	private final JMenuItem openReplaysFolderMenuItem  = new JMenuItem( Language.getText( "replayops.openReplaysFolder"  ), Icons.FOLDER_OPEN );
	/** Store replay menu item.                  */
	private final JMenuItem storeReplaysMenuItem       = new JMenuItem( Language.getText( "replayops.storeReplays"       ), Icons.SERVER_NETWORK );
	/** Share replay menu item.                  */
	private final JMenuItem shareReplayMenuItem        = new JMenuItem( Language.getText( "replayops.shareReplays"       ), Icons.DOCUMENT_SHARE );
	/** Export actions menu item.                */
	private final JMenuItem exportActionsMenuItem      = new JMenuItem( Language.getText( "replayops.exportActions"      ), Icons.NOTEBOOK_ARROW );
	
	/** The files to operate on. */
	private final File[] files;
	
	/** Optional callback that has to be called upon file changes. */
	private final ReplayOpCallback replayOpCallback;
	
	/**
	 * Creates a new ReplayOperationsPopupMenu.
	 * @param files            files to operate on
	 * @param replayOpCallback optional callback that can be used to get notified of file changes
	 */
	public ReplayOperationsPopupMenu( final File[] files, final ReplayOpCallback replayOpCallback ) {
		this.files = files;
		this.replayOpCallback = replayOpCallback;
		
		final JPanel infoPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 10, 1 ) );
		infoPanel.add( GuiUtils.changeFontToBold( new JLabel( Language.getText( "replayops.selectedReplays", files.length ) ) ) );
		add( infoPanel );
		
		openInAnalyzerMenuItem.addActionListener( this );
		add( openInAnalyzerMenuItem );
		openInMultiRepAnalMenuItem.addActionListener( this );
		add( openInMultiRepAnalMenuItem );
		watchReplayMenuItem.addActionListener( this );
		add( watchReplayMenuItem );
		addSeparator();
		copyReplaysMenuItem.addActionListener( this );
		add( copyReplaysMenuItem   );
		moveReplaysMenuItem.addActionListener( this );
		add( moveReplaysMenuItem   );
		packReplaysMenuItem.addActionListener( this );
		add( packReplaysMenuItem   );
		renameReplaysMenuItem.addActionListener( this );
		add( renameReplaysMenuItem );
		deleteReplaysMenuItem.addActionListener( this );
		add( deleteReplaysMenuItem );
		addSeparator();
		openReplaysFolderMenuItem.setEnabled( Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported( Desktop.Action.OPEN ) );
		openReplaysFolderMenuItem.addActionListener( this );
		add( openReplaysFolderMenuItem );
		addSeparator();
		storeReplaysMenuItem.addActionListener( this );
		add( storeReplaysMenuItem );
		shareReplayMenuItem.addActionListener( this );
		add( shareReplayMenuItem );
		addSeparator();
		exportActionsMenuItem.addActionListener( this );
		add( exportActionsMenuItem );
		
		synchronized ( customMenuItemRegistry ) {
			if ( !customMenuItemRegistry.isEmpty() ) {
				addSeparator();
				for ( final CustomReplayOpsMenuItemSpec customReplayOpsMenuItemSpec : customMenuItemRegistry ) {
					final JMenuItem customMenuItem = new JMenuItem( customReplayOpsMenuItemSpec.text, customReplayOpsMenuItemSpec.icon );
					customMenuItem.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed( final ActionEvent event ) {
							customReplayOpsMenuItemSpec.listener.actionPerformed( files, replayOpCallback, customReplayOpsMenuItemSpec.handler );
						}
					} );
					add( customMenuItem );
				}
			}
		}
	}
	
	@Override
	public void actionPerformed( final ActionEvent event ) {
		if ( event.getSource() == openInAnalyzerMenuItem ) {
			
			final int maxReplaysToOpen = Settings.getInt( Settings.KEY_SETTINGS_MAX_REPLAYS_TO_OPEN_FOR_OPEN_IN_ANALYZER );
			for ( int i = 0; i < maxReplaysToOpen && i < files.length; i++ )
				MainFrame.INSTANCE.openReplayFile( files[ i ] );
			
		} else if ( event.getSource() == openInMultiRepAnalMenuItem ) {
			
			MainFrame.INSTANCE.openReplaysInMultiRepAnalysis( files );
			
		} else if ( event.getSource() == watchReplayMenuItem ) {
			
			GeneralUtils.launchReplay( files[ 0 ] );
			
		} else if ( event.getSource() == copyReplaysMenuItem || event.getSource() == moveReplaysMenuItem || event.getSource() == deleteReplaysMenuItem ) {
			
			final boolean copy         = event.getSource() == copyReplaysMenuItem;
			final boolean move         = event.getSource() == moveReplaysMenuItem;
			final boolean delete       = event.getSource() == deleteReplaysMenuItem;
			final boolean copyOrMove   = copy || move;
			final boolean moveOrDelete = move || delete;
			
			final File targetFolder;
			if ( copyOrMove ) {
				// Copy and move requires a target folder
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
				fileChooser.setDialogTitle( Language.getText( copy ? "replayops.selectCopyTargetFolder" : "replayops.selectMoveTargetFolder" ) );
				fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
				if ( fileChooser.showOpenDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION  )
					targetFolder = fileChooser.getSelectedFile();
				else
					return;
			}
			else {
				targetFolder = null;
				// Delete "requires" confirmation
				if ( GuiUtils.showConfirmDialog( Language.getText( "replayops.deleteConfirmation", files.length ), true ) != 0 )
					return;
			}
			
			final ProgressDialog progressDialog = new ProgressDialog( copy ? "replayops.copyingReplays" : move ? "replayops.movingReplays" : "replayops.deletingReplays", copy ? Icons.DOCUMENT_COPY : move ? Icons.DOCUMENT_EXPORT : Icons.CROSS, files.length );
			new NormalThread( copy ? "Replay copier" : move ? "Replay mover" : "Replay deleter" ) {
				@Override
				public void run() {
					final byte[] buffer = delete ? null : new byte[ 16384 ]; // Sc2 replays are big; no buffer is needed if we want to delete
					int copiedCounter  = 0;
					int deletedCounter = 0;
					for ( int i = 0; i < files.length; i++ ) {
						final File file = files[ i ];
						
						if ( copyOrMove ) {
							if ( !GeneralUtils.copyFile( file, targetFolder, buffer, null ) ) {
								progressDialog.incrementFailed();
								progressDialog.incrementProcessed();
								progressDialog.updateProgressBar();
								continue; // If copy part of move fails, we cannot delete it (if it was copy, we wouldn't want to delete anyway)
							}
							copiedCounter++;
						}
						
						if ( moveOrDelete ) {
							if ( file.delete() ) {
								if ( !move && replayOpCallback != null )
									replayOpCallback.replayDeleted( file, i );
								deletedCounter++;
							}
							else {
								if ( delete ) // If it's a move and copy succeeded, we take it as not a fail
									progressDialog.incrementFailed();
							}
							if ( move && replayOpCallback != null ) 
								replayOpCallback.replayMoved( file, targetFolder, i ); // We call move even if delete failed (by move the user probably wants the list to point to the new files)
						}
						
						progressDialog.incrementProcessed();
						progressDialog.updateProgressBar();
					}
					if ( replayOpCallback != null )
						replayOpCallback.moveRenameDeleteEnded();
					
					progressDialog.taskFinished();
					if ( move && deletedCounter < copiedCounter )
						progressDialog.setCustomMessage( Language.getText( "replayops.moveReplaysResult", copiedCounter, copiedCounter - deletedCounter ) );
				}
			}.start();
			
		} else if ( event.getSource() == packReplaysMenuItem ) {
			
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle( Language.getText( "replayops.selectFileToPackTo" ) );
			fileChooser.setFileFilter( GuiUtils.ZIP_FILE_FILTER  );
			fileChooser.setFileView( GuiUtils.SC2GEARS_FILE_VIEW );
			final JCheckBox includePathInZipCheckBox = GuiUtils.createCheckBox( "replayops.includePathInZip" , Settings.KEY_REP_SEARCH_RESULTS_INCLUDE_PATH_IN_ZIP );
			fileChooser.setAccessory( includePathInZipCheckBox );
			if ( fileChooser.showSaveDialog( MainFrame.INSTANCE ) == JFileChooser.APPROVE_OPTION  ) {
				final ProgressDialog progressDialog = new ProgressDialog( "replayops.packingReplays", Icons.DOCUMENT_ZIPPER, files.length );
				new NormalThread( "Replay packer" ) {
					@Override
					public void run() {
						File zipFile = fileChooser.getSelectedFile();
						// Append the extension if not provided
						if ( !GuiUtils.ZIP_FILE_FILTER.accept( zipFile ) )
							zipFile = new File( zipFile.getAbsolutePath() + ".zip" );
						
						try ( final ZipOutputStream output = new ZipOutputStream( new FileOutputStream( zipFile ) ) ) {
							
							final boolean includePath = includePathInZipCheckBox.isSelected();
							final byte[] buffer = new byte[ 16384 ]; // SC2 replays are big
							for ( final File file : files ) {
								if ( progressDialog.isAborted() )
									break;
								
								FileInputStream input  = null;
								try {
									// I add zip entry in the try-catch block because adding the same entry raises exception
									final ZipEntry zipEntry = new ZipEntry( includePath ? file.getAbsolutePath() : file.getName() );
									zipEntry.setTime( file.lastModified() );
									output.putNextEntry( zipEntry );
									
									input = new FileInputStream( file );
									int readBytes;
									while ( ( readBytes = input.read( buffer ) ) > 0 )
										output.write( buffer, 0, readBytes );
								} catch ( final Exception e ) {
									progressDialog.incrementFailed();
									e.printStackTrace();
								} finally {
									if ( input != null ) try { input.close(); } catch ( final IOException ie ) {}
								}
								
								output.closeEntry();
								progressDialog.incrementProcessed();
								progressDialog.updateProgressBar();
							}
							
							output.finish();
							output.flush();
						} catch ( final Exception e ) {
							e.printStackTrace();
						}
						
						progressDialog.taskFinished();
					}
				}.start();
			}
			
		} else if ( event.getSource() == renameReplaysMenuItem ) {
			
			new ReplayRenameDialog( replayOpCallback, files );
			
		} else if ( event.getSource() == openReplaysFolderMenuItem ) {
			
			try {
				if ( GeneralUtils.isWindows() ) {
					// On Windows we have a way to not just open but also select the replay file; source:
					//     http://stackoverflow.com/questions/7357969/how-to-use-java-code-to-open-windows-file-explorer-and-highlight-the-specified-f
					//     http://support.microsoft.com/kb/152457
					// It should be "explorer.exe /select,c:\dir\filename.ext", but if (double) spaces are in the file name, it doesn't work,
					// So I use it this way (which also works): "explorer.exe" "/select," "c:\dir\filename.ext"
					new ProcessBuilder( "explorer.exe", "/select,", files[ 0 ].getAbsolutePath() ).start();
				}
				else
					Desktop.getDesktop().open( files[ 0 ].getParentFile() );
			} catch ( final IOException ie ) {
				ie.printStackTrace();
			}
			
		} else if ( event.getSource() == storeReplaysMenuItem ) {
			
			final String authorizationKey = GeneralUtils.checkKeyBeforeStoringOrDownloading();
			if ( authorizationKey == null )
				return;
			
			final ProgressDialog progressDialog = new ProgressDialog( "replayops.storingReplays", Icons.SERVER_NETWORK, files.length );
			new NormalThread( "Replay storer" ) {
				@Override
				public void run() {
					for ( final File file : files ) {
						if ( progressDialog.isAborted() )
							break;
						
						if ( !GeneralUtils.storeReplay( file, authorizationKey ) )
							progressDialog.incrementFailed();
						
						progressDialog.incrementProcessed();
						progressDialog.updateProgressBar();
					}
					
					progressDialog.taskFinished();
				}
			}.start();
			
		} else if ( event.getSource() == shareReplayMenuItem ) {
			
			new ShareReplaysDialog( files );
			
		} else if ( event.getSource() == exportActionsMenuItem ) {
			
			final ProgressDialog progressDialog = new ProgressDialog( "replayops.exportingActions", Icons.NOTEBOOK_ARROW, files.length );
			new NormalThread( "Actions exporter" ) {
				@Override
				public void run() {
					for ( final File file : files ) {
						if ( progressDialog.isAborted() )
							break;
						
						final Replay replay = ReplayFactory.parseReplay( file.getAbsolutePath(), ReplayFactory.GENERAL_DATA_CONTENT );
						if ( replay == null )
							progressDialog.incrementFailed();
						else {
							final File targetFile = GeneralUtils.generateUniqueName( new File( file.getParent(), GeneralUtils.getFileNameWithoutExt( file ) + ".txt" ) );
							PrintWriter output = null;
							try {
								output = new PrintWriter( new OutputStreamWriter( new FileOutputStream( targetFile ), Consts.UTF8 ), false );
								for ( final Action action : replay.gameEvents.actions )
									output.println( action.toString() );
								output.flush();
							} catch ( final Exception e ) {
								progressDialog.incrementFailed();
								e.printStackTrace();
							} finally {
								if ( output != null )
									output.close();
							}
						}
						progressDialog.incrementProcessed();
						progressDialog.updateProgressBar();
					}
					
					progressDialog.taskFinished();
				}
			}.start();
		}
	}
	
}
