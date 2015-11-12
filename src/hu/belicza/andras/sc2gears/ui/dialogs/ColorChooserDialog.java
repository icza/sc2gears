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

import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;

/**
 * A color chooser dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class ColorChooserDialog extends BaseDialog {
	
	/** The selected color. */
	private Color color;
	
	/**
	 * Creates a new ColorChooserDialog.
	 * @param parent       parent of the dialog
	 * @param initialColor optional initial color to be selected
	 */
	public ColorChooserDialog( final Frame parent, final Color initialColor ) {
		super( parent, "general.colorChooser.title", Icons.COLOR );
		
		completeInit( initialColor );
	}
	
	/**
	 * Creates a new ColorChooserDialog.
	 * @param parent       parent of the dialog
	 * @param initialColor optional initial color to be selected
	 */
	public ColorChooserDialog( final Dialog parent, final Color initialColor ) {
		super( parent, "general.colorChooser.title", Icons.COLOR );
		
		completeInit( initialColor );
	}
	
	/**
	 * Completes the initialization and shows the dialog.
	 * @param initialColor optional initial color to be selected
	 */
	private void completeInit( final Color initialColor ) {
		setModal( true );
		
		final JColorChooser colorChooser = initialColor == null ? new JColorChooser() : new JColorChooser( initialColor );
		getContentPane().add( colorChooser, BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		final JButton okButton = new JButton();
		GuiUtils.updateButtonText( okButton, "button.ok" );
		okButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				color = colorChooser.getColor();
				dispose();
			}
		} );
		buttonsPanel.add( okButton );
		buttonsPanel.add( createCloseButton( "button.cancel" ) );
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		packAndShow( okButton, false );
	}
	
	/**
	 * Returns the selected color.
	 * @return the selected color or <code>null</code> if no color was selected (cancel was pressed)
	 */
	public Color getColor() {
		return color;
	}
	
}
