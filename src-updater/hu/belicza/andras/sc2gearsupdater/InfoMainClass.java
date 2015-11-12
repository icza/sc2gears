/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsupdater;

import hu.belicza.andras.sc2gears.shared.SharedConsts;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * An info main class registered as the main class of the jar to display proper usage.
 * 
 * @author Andras Belicza
 */
public class InfoMainClass {
	
	/**
	 * Entry point of the program.
	 * 
	 * <p>Displays an info message about the proper usage.</p>
	 * 
	 * @param arguments used to take arguments from the running environment - not used here
	 */
	public static void main( final String[] arguments ) {
		setLaf();
		
		JOptionPane.showMessageDialog( null,
				new String[] {
					SharedConsts.UPDATER_NAME + " is handled by " + SharedConsts.APPLICATION_NAME + " internally.",
					"You do not need to run it."
				},
				SharedConsts.UPDATER_NAME, JOptionPane.WARNING_MESSAGE );
	}
	
	/**
	 * Sets the Look and Feel used by the Updater.
	 */
	public static void setLaf() {
		// This has to be done in the EDT!!!
		if ( SwingUtilities.isEventDispatchThread() )
			setLafInEDT();
		else
			try {
				SwingUtilities.invokeAndWait( new Runnable() {
					@Override
					public void run() {
						setLafInEDT();
					}
				} );
			} catch ( final Exception e ) {
			}
	}
	
	/**
	 * Sets the Look and Feel used by the Updater.
	 */
	private static void setLafInEDT() {
		for ( final LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels() )
			if ( "Nimbus".equals( lookAndFeelInfo.getName() ) ) {
				try {
					UIManager.setLookAndFeel( lookAndFeelInfo.getClassName() );
				} catch ( final Exception e ) {
				}
				break;
			}
	}
	
}
