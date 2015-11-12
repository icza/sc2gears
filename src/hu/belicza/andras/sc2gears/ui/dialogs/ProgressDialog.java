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
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gearspluginapi.api.ui.IProgressDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * A dialog displaying a progress bar and control buttons to abort/close.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class ProgressDialog extends BaseDialog implements ActionListener, IProgressDialog {
	
	/** Color to display failed subtasks with. */
	private static final Color FAILED_COLOR = new Color( 220, 100, 100 );
	
	/** Stats table about the progress.                    */
	private final JTable       statsTable            = GuiUtils.createNonEditableTable();
	/** Alert when done check box.                         */
	private final JCheckBox    alertWhenDoneCheckBox = GuiUtils.createCheckBox( "progressDialog.alertWhenDone", Settings.KEY_PROGRESS_DIALOG_ALERT_WHEN_DONE );
	/** The progress bar.                                  */
	private final JProgressBar progressBar           = new JProgressBar();
	/** Reference to the info panel.                       */
	private final JPanel       infoPanel             = new JPanel( new BorderLayout() );
	/** Reference to the label to display failed subtasks. */
	private final JLabel       failedLabel           = new JLabel();
	/** Reference to the control button.                   */
	private final JButton      button                = new JButton( Icons.CROSS_OCTAGON );
	
	/** Total number of the subtasks to be performed. */
	private final int total;
	/** Number of processed subtasks.                 */
	private int       processed;
	/** Number of failed subtasks.                    */
	private int       failed;
	
	/** Start time. */
	private long startTime;
	
	/** Tells if the task has been aborted. */
	private volatile boolean aborted;
	/** Tells if the dialog is closeable.   */
	private volatile boolean closeable;
	
	/** Tells if there were failed subtasks before. */
	private boolean wasFailedBefore;
	
	/**
	 * Creates a new ProgressDialog.
	 * @param titleTextKey text key of the dialog title
	 * @param icon         icon of the dialog
	 * @param total        total number of the subtasks to be performed
	 */
	public ProgressDialog( final String titleTextKey, final ImageIcon icon, final int total ) {
		super( titleTextKey, icon );
		
		this.total = total;
		
		completeInit();
	}
	
	/**
	 * Creates a new ProgressDialog.
	 * @param title title of the dialog
	 * @param icon  icon of the dialog
	 * @param total total number of the subtasks to be performed
	 */
	public ProgressDialog( final Holder< String > title, final ImageIcon icon, final int total ) {
		super( title, icon );
		
		this.total = total;
		
		completeInit();
	}
	
	/**
	 * Creates a new ProgressDialog.
	 * @param owner the Frame from which the dialog is displayed
	 * @param title title of the dialog
	 * @param icon  icon of the dialog
	 * @param total total number of the subtasks to be performed
	 */
	public ProgressDialog( final Frame owner, final Holder< String > title, final ImageIcon icon, final int total ) {
		super( owner, title, icon );
		
		this.total = total;
		
		completeInit();
	}
	
	/**
	 * Creates a new ProgressDialog.
	 * @param owner the Dialog from which the dialog is displayed
	 * @param title title of the dialog
	 * @param icon  icon of the dialog
	 * @param total total number of the subtasks to be performed
	 */
	public ProgressDialog( final Dialog owner, final Holder< String > title, final ImageIcon icon, final int total ) {
		super( owner, title, icon );
		
		this.total = total;
		
		completeInit();
	}
	
	/**
	 * Completes the initialization of the dialog.
	 */
	private void completeInit() {
		setModal( false );
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		
		( (JPanel) getContentPane() ).setBorder( BorderFactory.createEmptyBorder( 5, 10, 5, 10 ) );
		
		statsTable.setShowVerticalLines( true );
		( (DefaultTableModel) statsTable.getModel() ).setDataVector(
			new Object[][] {
				{ Language.getText( "progressDialog.elapsedTime"        ), null },
				{ Language.getText( "progressDialog.estRemainingTime"   ), null },
				{ Language.getText( "progressDialog.estTotalTime"       ), null },
				{ Language.getText( "progressDialog.avgProcessingSpeed" ), null }
			}, new Object[] { "Property", "Value" } );
		getContentPane().add( GuiUtils.wrapInBorderPanel( statsTable ), BorderLayout.NORTH );
		
		final Box centerBox = Box.createVerticalBox();
		centerBox.setBorder( BorderFactory.createEmptyBorder( 5, 0, 5, 0 ) );
		progressBar.setStringPainted( true );
		progressBar.setPreferredSize( new Dimension( 450, progressBar.getPreferredSize().height ) );
		progressBar.setMaximum( total );
		centerBox.add( progressBar );
		centerBox.add( Box.createVerticalStrut( 2 ) );
		failedLabel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
		failedLabel.setOpaque( true );
		infoPanel.add( failedLabel, BorderLayout.WEST );
		centerBox.add( infoPanel );
		getContentPane().add( centerBox, BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		GuiUtils.updateButtonText( button, "button.abort" );
		button.addActionListener( this );
		buttonsPanel.add( button );
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		startTime = System.currentTimeMillis();
		updateProgressBar();
		
		MainFrame.registerBackgroundJob();
		
		packAndShow( button, false );
	}
	
	/**
	 * Increments the number of processed subtasks.
	 */
	public void incrementProcessed() {
		processed++;
	}
	
	/**
	 * Increments the number of failed subtasks.
	 */
	public void incrementFailed() {
		failed++;
	}
	
	/**
	 * Returns the number of processed subtasks.
	 * @return the number of processed subtasks
	 */
	public int getProcessed() {
		return processed;
	}
	
	/**
	 * Returns the number of failed subtasks.
	 * @return the number of failed subtasks
	 */
	public int getFailed() {
		return failed;
	}
	
	/**
	 * Returns the total number of subtasks.
	 * @return the total number of subtasks
	 */
	public int getTotal() {
		return total;
	}
	
	/**
	 * Tells if abort has been requested.
	 * @return true if abort has been requested; false otherwise
	 */
	public boolean isAborted() {
		return aborted;
	}
	
	@Override
	public void actionPerformed( final ActionEvent event ) {
		if ( closeable ) {
			dispose();
		}
		else {
			button.setEnabled( false );
			aborted = true;
		}
	}
	
	/**
	 * Updates the progress bar and the info texts.
	 */
	public void updateProgressBar() {
		final long elapsedTime = System.currentTimeMillis() - startTime;
		
		int row = 0;
		statsTable.setValueAt( GeneralUtils.formatLongSeconds( elapsedTime / 1000 ), row++, 1 );
		if ( processed > 0 ) {
			statsTable.setValueAt( GeneralUtils.formatLongSeconds( elapsedTime * ( total - processed ) / processed / 1000 ), row++, 1 );
			statsTable.setValueAt( GeneralUtils.formatLongSeconds( elapsedTime * total / processed / 1000 ), row++, 1 );
			statsTable.setValueAt( Language.getText( "progressDialog.perSec", String.format( Locale.ENGLISH, "%,.1f", processed * 1000f / elapsedTime ) ), row++, 1 );
		}
		
		progressBar.setValue( processed );
		progressBar.setString( Language.getText( "progressDialog.progressBar.status", processed, failed, total, total == 0 ? 100 : 100 * processed / total  ) );
		failedLabel.setText( Language.getText( "progressDialog.progressBar.failed", failed ) );
		
		if ( !wasFailedBefore && failed > 0 ) {
			wasFailedBefore = true;
			failedLabel.setBackground( FAILED_COLOR );
			infoPanel.add( GuiUtils.createErrorDetailsLink(), BorderLayout.EAST );
			pack();
		}
		
		if ( aborted )
			progressBar.setString( Language.getText( "progressDialog.progressBar.aborted" ) + " [" + progressBar.getString() + "]" );
		else if ( closeable )
			progressBar.setString( Language.getText( "progressDialog.progressBar.done"    ) + " [" + progressBar.getString() + "]" );
	}
	
	/**
	 * Registers that the task has been finished.
	 */
	public void taskFinished() {
		MainFrame.removeBackgroundJob();
		setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		closeable = true;
		button.setEnabled( true );
		button.setIcon( null );
		GuiUtils.updateButtonText( button, "button.close" );
		button.requestFocusInWindow();
		updateProgressBar();
		if ( !aborted && alertWhenDoneCheckBox.isSelected() )
			Toolkit.getDefaultToolkit().beep();
	}
	
	/**
	 * Sets a custom message.
	 * @param message custom message to be set
	 */
	public void setCustomMessage( final String message ) {
		infoPanel.add( GuiUtils.wrapInPanel( new JLabel( message ) ), BorderLayout.SOUTH );
		pack();
	}
	
}
