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
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.GameSpeed;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 * Game time - Real time dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class GameTimeRealTimeConverterDialog extends BaseDialog {
	
	/**
	 * Creates a new GameTimeRealTimeConverterDialog.
	 */
	public GameTimeRealTimeConverterDialog() {
		super( "gameTimeRealTimeConverter.title", Icons.CALCULATOR );
		
		setModal( false );
		
		final Box box = Box.createVerticalBox();
		box.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		final Vector< GameSpeed > gameSpeedVector = new Vector< GameSpeed >( EnumSet.complementOf( EnumSet.of( GameSpeed.UNKNOWN ) ) );
		Collections.sort( gameSpeedVector, Collections.reverseOrder() );
		final JComboBox< GameSpeed > gameSpeedComboBox = new JComboBox<>( gameSpeedVector );
		
		final JSpinner   gameTimeApmSpinner = new JSpinner( new SpinnerNumberModel( 100, 0, Integer.MAX_VALUE, 1 ) );
		final JSpinner   realTimeApmSpinner = new JSpinner( new SpinnerNumberModel( 100, 0, Integer.MAX_VALUE, 1 ) );
		final JTextField gameTimeTextField  = new JTextField( "00:01:00", 10 );
		final JTextField realTimeTextField  = new JTextField( 10 );
		
		final JButton toRealApmButton  = new JButton( Icons.ARROW );
		final JButton toGameApmButton  = new JButton( Icons.ARROW_180 );
		final JButton toRealTimeButton = new JButton( Icons.ARROW );
		final JButton toGameTimeButton = new JButton( Icons.ARROW_180 );
		
		final Holder< JButton > lastApmConverterButtonHolder  = new Holder< JButton >( toRealApmButton  );
		final Holder< JButton > lastTimeConverterButtonHolder = new Holder< JButton >( toRealTimeButton );
		
		final ActionListener converterActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final GameSpeed gameSpeed = (GameSpeed) gameSpeedComboBox.getSelectedItem();
				if ( event.getSource() == toRealApmButton ) {
					
					realTimeApmSpinner.setValue( gameSpeed.convertToGameTime( (Integer) gameTimeApmSpinner.getValue() ) );
					if ( (Integer) realTimeApmSpinner.getValue() < 0 )
						realTimeApmSpinner.setValue( 0 );
					lastApmConverterButtonHolder.value = toRealApmButton;
					
				} else if ( event.getSource() == toGameApmButton ) {
					
					gameTimeApmSpinner.setValue( gameSpeed.convertToRealTime( (Integer) realTimeApmSpinner.getValue() ) );
					if ( (Integer) gameTimeApmSpinner.getValue() < 0 )
						gameTimeApmSpinner.setValue( 0 );
					lastApmConverterButtonHolder.value = toGameApmButton;
					
				} else if ( event.getSource() == toRealTimeButton ) {
					
					final Integer seconds = GeneralUtils.parseSeconds( gameTimeTextField.getText() );
					realTimeTextField.setText( seconds == null ? "" : ReplayUtils.formatMs( seconds * 1000, gameSpeed ) );
					lastTimeConverterButtonHolder.value = toRealTimeButton;
					
				} else if ( event.getSource() == toGameTimeButton ) {
					
					final Integer seconds = GeneralUtils.parseSeconds( realTimeTextField.getText() );
					gameTimeTextField.setText( seconds == null ? "" : ReplayUtils.formatMs( gameSpeed.convertToGameTime( seconds ) * 1000, GameSpeed.NORMAL ) );
					lastTimeConverterButtonHolder.value = toGameTimeButton;
					
				}
			}
		};
		
		toRealApmButton .addActionListener( converterActionListener );
		toGameApmButton .addActionListener( converterActionListener );
		toRealTimeButton.addActionListener( converterActionListener );
		toGameTimeButton.addActionListener( converterActionListener );
		
		final JPanel panel = new JPanel();
		panel.add( new JLabel( Language.getText( "gameTimeRealTimeConverter.gameSpeed" ) ) );
		gameSpeedComboBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				lastApmConverterButtonHolder .value.doClick( 0 );
				lastTimeConverterButtonHolder.value.doClick( 0 );
			}
		} );
		gameSpeedComboBox.setSelectedIndex( 0 );
		panel.add( gameSpeedComboBox );
		box.add( panel );
		
		// APM conversion
		final Box apmConverterBox = Box.createHorizontalBox();
		apmConverterBox.setBorder( BorderFactory.createTitledBorder( Language.getText( "gameTimeRealTimeConverter.apmConverter" ) ) );
		apmConverterBox.add( new JLabel( Language.getText( "gameTimeRealTimeConverter.ingameApm" ) ) );
		apmConverterBox.add( gameTimeApmSpinner );
		apmConverterBox.add( Box.createHorizontalStrut( 20 ) );
		// APM converter buttons
		GuiUtils.updateButtonText( toRealApmButton, "gameTimeRealTimeConverter.toRealApmButton" );
		apmConverterBox.add( toRealApmButton );
		GuiUtils.updateButtonText( toGameApmButton, "gameTimeRealTimeConverter.toGameApmButton" );
		toGameApmButton.setHorizontalTextPosition( SwingConstants.LEFT );
		apmConverterBox.add( toGameApmButton );
		apmConverterBox.add( Box.createHorizontalStrut( 20 ) );
		apmConverterBox.add( new JLabel( Language.getText( "gameTimeRealTimeConverter.realApm" ) ) );
		apmConverterBox.add( realTimeApmSpinner );
		box.add( apmConverterBox );
		
		// Time conversion
		final Box timeConverterBox = Box.createHorizontalBox();
		timeConverterBox.setBorder( BorderFactory.createTitledBorder( Language.getText( "gameTimeRealTimeConverter.timeConverter" ) ) );
		timeConverterBox.add( new JLabel( Language.getText( "gameTimeRealTimeConverter.ingameTime" ) ) );
		gameTimeTextField.setToolTipText( Language.getText( "module.repSearch.tab.filters.timeFormatToolTip" ) );
		timeConverterBox.add( gameTimeTextField );
		timeConverterBox.add( Box.createHorizontalStrut( 20 ) );
		// Time converter buttons
		GuiUtils.updateButtonText( toRealTimeButton, "gameTimeRealTimeConverter.toRealTimeButton" );
		timeConverterBox.add( toRealTimeButton );
		GuiUtils.updateButtonText( toGameTimeButton, "gameTimeRealTimeConverter.toGameTimeButton" );
		toGameTimeButton.setHorizontalTextPosition( SwingConstants.LEFT );
		timeConverterBox.add( toGameTimeButton );
		timeConverterBox.add( Box.createHorizontalStrut( 20 ) );
		timeConverterBox.add( new JLabel( Language.getText( "gameTimeRealTimeConverter.realTime" ) ) );
		realTimeTextField.setToolTipText( Language.getText( "module.repSearch.tab.filters.timeFormatToolTip" ) );
		timeConverterBox.add( realTimeTextField );
		box.add( timeConverterBox );
		
		// Create a "form-style" (same width for all columns)
		final Box[] formBoxes = { apmConverterBox, timeConverterBox };
		for ( int column = timeConverterBox.getComponentCount() - 1; column >= 0; column-- ) {
			// Find max column width
			int maxWidth = 0;
			for ( final Box formBox : formBoxes )
				maxWidth = Math.max( formBox.getComponent( column ).getPreferredSize().width, maxWidth );
			
			// Set same width for all labels
			for ( final Box formBox : formBoxes )
				formBox.getComponent( column ).setPreferredSize( new Dimension( maxWidth, formBox.getComponent( column ).getPreferredSize().height ) );
		}
		
		final JButton closeButton = createCloseButton( "button.close" );
		box.add( GuiUtils.wrapInPanel( closeButton ) );
		
		getContentPane().add( box );
		
		packAndShow( closeButton, false );
	}
	
}
