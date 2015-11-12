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
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.util.Holder;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * Bases of dialogs in Sc2gears.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class BaseDialog extends JDialog {
	
	/** The parent of the dialog (either a {@link Frame} or a {@link Dialog}). */
	protected final Object parent;
	
	/**
	 * Creates a new BaseDialog.<br>
	 * The MainFrame instance will be the owner of the dialog.
	 * 
	 * @param titleTextKey text key of the dialog title
	 * @param icon         optional icon of the dialog
	 */
	public BaseDialog( final String titleTextKey, final ImageIcon icon ) {
		super( MainFrame.INSTANCE, Language.getText( titleTextKey ), true );
		
		parent = MainFrame.INSTANCE;
		
		completeInit( icon );
	}
	
	/**
	 * Creates a new BaseDialog.
	 * @param owner        the Frame from which the dialog is displayed
	 * @param titleTextKey text key of the dialog title
	 * @param icon         optional icon of the dialog
	 */
	public BaseDialog( final Frame owner, final String titleTextKey, final ImageIcon icon ) {
		super( owner, Language.getText( titleTextKey ), true );
		
		parent = owner;
		
		completeInit( icon );
	}
	
	/**
	 * Creates a new BaseDialog.
	 * @param owner        the Dialog from which the dialog is displayed
	 * @param titleTextKey text key of the dialog title
	 * @param icon         optional icon of the dialog
	 */
	public BaseDialog( final Dialog owner, final String titleTextKey, final ImageIcon icon ) {
		super( owner, Language.getText( titleTextKey ) );
		
		parent = owner;
		
		completeInit( icon );
	}
	
	/**
	 * Creates a new BaseDialog.<br>
	 * The MainFrame instance will be the owner of the dialog.
	 * 
	 * @param title title of the dialog
	 * @param icon  optional icon of the dialog
	 */
	public BaseDialog( final Holder< String > title, final ImageIcon icon ) {
		super( MainFrame.INSTANCE, title.value, true );
		
		parent = MainFrame.INSTANCE;
		
		completeInit( icon );
	}
	
	/**
	 * Creates a new BaseDialog.
	 * @param owner the Frame from which the dialog is displayed
	 * @param title title of the dialog
	 * @param icon  optional icon of the dialog
	 */
	public BaseDialog( final Frame owner, final Holder< String > title, final ImageIcon icon ) {
		super( owner, title.value, true );
		
		parent = owner;
		
		completeInit( icon );
	}
	
	/**
	 * Creates a new BaseDialog.
	 * @param owner the Dialog from which the dialog is displayed
	 * @param title title of the dialog
	 * @param icon  optional icon of the dialog
	 */
	public BaseDialog( final Dialog owner, final Holder< String > title, final ImageIcon icon ) {
		super( owner, title.value );
		
		parent = owner;
		
		completeInit( icon );
	}
	
	/**
	 * Creates a new BaseDialog.<br>
	 * The MainFrame instance will be the owner of the dialog.
	 * 
	 * @param titleTextKey text key of the dialog title
	 * @param titleParams  parameters of the title text (to be passed to {@link Language#getText(String, Object...)}
	 */
	public BaseDialog( final String titleTextKey, final Object[] titleParams ) {
		super( MainFrame.INSTANCE, Language.getText( titleTextKey, titleParams ), true );
		
		parent = MainFrame.INSTANCE;
		
		completeInit( null );
	}
	
	/**
	 * Creates a new BaseDialog.<br>
	 * The MainFrame instance will be the owner of the dialog.
	 * 
	 * @param titleTextKey text key of the dialog title
	 * @param titleParams  parameters of the title text (to be passed to {@link Language#getText(String, Object...)}
	 * @param icon         optional icon of the dialog
	 */
	public BaseDialog( final String titleTextKey, final Object[] titleParams, final ImageIcon icon ) {
		super( MainFrame.INSTANCE, Language.getText( titleTextKey, titleParams ), true );
		
		parent = MainFrame.INSTANCE;
		
		completeInit( icon );
	}
	
	/**
	 * Completes the dialog init.
	 * @param icon optional icon of the dialog
	 */
	private void completeInit( final ImageIcon icon ) {
		if ( icon != null )
			setIconImage( icon.getImage() );
		
		setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
	}
	
	/**
	 * Packs the dialog, centers it to the parent and makes it visible.
	 * @param toBeFocused       component to be focused
	 * @param requestFocusLater tells if focus request should be gained by a {@link SwingUtilities#invokeLater(Runnable)} call
	 */
	protected void packAndShow( final JComponent toBeFocused, final boolean requestFocusLater ) {
		pack();
		
		GuiUtils.centerWindowToWindow( this, (Window) parent );
		
		requestFocusAndShow( toBeFocused, requestFocusLater );
	}
	
	/**
	 * Maximizes the dialog with margin, and makes it visible.
	 * @param margin            margin to leave around the dialog
	 * @param maxSize           optional parameter defining a maximum size
	 * @param toBeFocused       component to be focused
	 * @param requestFocusLater tells if focus request should be gained by a {@link SwingUtilities#invokeLater(Runnable)} call
	 */
	protected void maximizeWithMarginAndShow( final int margin, final Dimension maxSize, final JComponent toBeFocused, final boolean requestFocusLater ) {
		GuiUtils.maximizeWindowWithMargin( this, margin, maxSize );
		
		requestFocusAndShow( toBeFocused, requestFocusLater );
	}
	
	/**
	 * Requests the focus for the specified component and makes the dialog visible.
	 * Packs the dialog, centers it to the parent and makes it visible.
	 * @param toBeFocused       component to be focused
	 * @param requestFocusLater tells if focus request should be gained by a {@link SwingUtilities#invokeLater(Runnable)} call
	 */
	protected void requestFocusAndShow( final JComponent toBeFocused, final boolean requestFocusLater ) {
		if ( requestFocusLater )
			SwingUtilities.invokeLater( new Runnable() {
				@Override
				public void run() {
					toBeFocused.requestFocusInWindow();
				}
			} );
		else
			toBeFocused.requestFocusInWindow();
		
		setVisible( true );
	}
	
	/**
	 * Creates a close button which disposes the dialog when clicked.
	 * @param textKey text key of the button
	 * @return a close button which disposes the dialog when clicked
	 */
	protected JButton createCloseButton( final String textKey ) {
		return GuiUtils.createCloseButton( this, textKey );
	}
	
}
