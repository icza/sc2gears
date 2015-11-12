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
import hu.belicza.andras.sc2gears.util.GeneralUtils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * Rename navigation tree item dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class RenameItemDialog extends BaseDialog {
	
	/**
	 * Creates a new RenameItemDialog.
	 * @param node node to be renamed
	 * @param file file associated with the node
	 */
	public RenameItemDialog( final DefaultMutableTreeNode node, final File file ) {
		super( "navigationTree.popup.renameDialog.title", (ImageIcon) null );
		
		final Box box = Box.createVerticalBox();
		box.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		final String currentName = node.getUserObject().toString();
		box.add( new JLabel( Language.getText( "navigationTree.popup.renameDialog.enterNewNameFor", currentName ) ) );
		
		final JTextField newNameTextField = new JTextField( currentName );
		newNameTextField.setSelectionStart( 0 );
		newNameTextField.setSelectionEnd( currentName.length() );
		box.add( newNameTextField );
		
		getContentPane().add( box, BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		final JButton renameButton = new JButton();
		GuiUtils.updateButtonText( renameButton, "navigationTree.popup.renameDialog.renameButton" );
		renameButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				dispose();
				if ( !file.renameTo( new File( file.getParent(), newNameTextField.getText() + GeneralUtils.getFileExtension( file ) ) ) )
					GuiUtils.showErrorDialog( Language.getText( "navigationTree.popup.renameDialog.failedToRename", currentName, newNameTextField.getText() ) );
				else
					MainFrame.INSTANCE.refreshNavigationTree();
			}
		} );
		buttonsPanel.add( renameButton );
		final JButton cancelButton = createCloseButton( "button.cancel" );
		buttonsPanel.add( cancelButton );
		getContentPane().add( GuiUtils.wrapInPanel( buttonsPanel ), BorderLayout.SOUTH );
		
		packAndShow( newNameTextField, false );
	}
	
}
