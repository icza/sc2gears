/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears;

import hu.belicza.andras.sc2gears.shared.SharedConsts;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
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
		// Set Look and Feel: from the EDT!
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				@Override
				public void run() {
					for ( final LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels() )
						if ( "Nimbus".equals( lookAndFeelInfo.getName() ) ) {
							try {
								UIManager.setLookAndFeel( lookAndFeelInfo.getClassName() );
							} catch ( final Exception e ) {
							}
							break;
						}
				}
			} );
		} catch ( final Exception e ) {
		}
		
		final Box starterSriptsInfoBox = Box.createVerticalBox();
		starterSriptsInfoBox.add( createStarterScriptsInfoBoxRow( "Windows:" , SharedConsts.EXECUTABLE_NAME_WIN  ) );
		starterSriptsInfoBox.add( createStarterScriptsInfoBoxRow( "MAC OS-X:", SharedConsts.EXECUTABLE_NAME_OS_X ) );
		starterSriptsInfoBox.add( createStarterScriptsInfoBoxRow( "Linux:"   , SharedConsts.EXECUTABLE_NAME_UNIX ) );
		
		JOptionPane.showMessageDialog( null,
				new Object[] {
					"Please run the proper script to start " + SharedConsts.APPLICATION_NAME + "!",
					" ",
					starterSriptsInfoBox
				},
				SharedConsts.APPLICATION_NAME, JOptionPane.WARNING_MESSAGE );
	}
	
	/**
	 * Creates a row for the starter scripts info box.
	 * @param osName         OS name
	 * @param executableName executable name
	 * @return a row for the starter scripts info box
	 */
	private static Box createStarterScriptsInfoBoxRow( final String osName, final String executableName ) {
		final Box row = Box.createHorizontalBox();
		
		final JLabel osNameLabel = new JLabel( osName );
		osNameLabel.setPreferredSize( new Dimension( 80, 0 ) );
		row.add( osNameLabel );
		row.add( new JLabel( executableName ) );
		row.add( Box.createHorizontalGlue() );
		
		return row;
	}
	
}
