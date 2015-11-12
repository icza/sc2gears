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
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.ControlledThread;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerColor;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A word cloud generator and visualizer.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class WordCloudDialog extends BaseDialog {
	
	/** Colors used to draw words. */
	private static final Color[] WORD_COLORS = new Color[ 8 ]; // Color.brighter() is "slow", cache the color values
	static {
		final PlayerColor[] playerColors = ReplayConsts.PlayerColor.values();
		for ( int i = 0; i < WORD_COLORS.length; i++ )
			WORD_COLORS[ i ] = playerColors[ i ].color.brighter().brighter();
	}
	
	/**
	 * Holds the attributes of a renderable word.
	 * @author Andras Belicza
	 */
	private static class RenderableWord {
		public int  x1, y1, x2, y2; // We store these coordinates for fast collision detection
		public int  baseLine;
		public Font font;
		
		/**
		 * Moves the top left corner of the box to the specified location
		 * @param x x coordinate of the location to move to
		 * @param y y coordinate of the location to move to
		 */
		public void moveTo( final int x, final int y ) {
			x2 = x - x1 + x2;
			y2 = y - y1 + y2;
			x1 = x;
			y1 = y;
		}
	}
	
	/** List of words (and their frequency) to build the word cloud from. */
	private final List< Pair< String, Integer > > wordList;
	
	/** Array of renderable words.            */
	private transient RenderableWord[] renderableWords;
	/** Reference to the generator thread.    */
	private transient ControlledThread generatorThread;
	/** Tells if generating has been aborted. */
	private transient boolean          aborted;
	
	/**
	 * Creates a new WordCloudDialog.
	 * @param title    sub-title of the dialog
	 * @param owner    the Frame from which the dialog is displayed
	 * @param wordList list of words (and their frequency) to build the word cloud from.
	 */
	public WordCloudDialog( final Frame owner, final String title, final List< Pair< String, Integer > > wordList ) {
		super( owner, new Holder< String>( Language.getText( "wordCloud.title" ) + " - " + title ), Icons.TAG_CLOUD );
		
		this.wordList = wordList;
		completeInit();
	}
	
	/**
	 * Creates a new WordCloudDialog.
	 * @param title    sub-title of the dialog
	 * @param owner    the Dialog from which the dialog is displayed
	 * @param wordList list of words (and their frequency) to build the word cloud from.
	 */
	public WordCloudDialog( final Dialog owner, final String title, final List< Pair< String, Integer > > wordList ) {
		super( owner, new Holder< String>( Language.getText( "wordCloud.title" ) + " - " + title ), Icons.TAG_CLOUD );
		
		this.wordList = wordList;
		completeInit();
	}
	
	/**
	 * Completes the word cloud dialog init.
	 */
	private void completeInit() {
		setModal( false );
		
		Collections.sort( wordList, new Comparator< Pair< String, Integer > >() {
			@Override
			public int compare( final Pair< String, Integer > w1, final Pair< String, Integer > w2 ) {
				return w2.value2.intValue() - w1.value2.intValue(); // Descending order
			}
		} );
		
		final Box northBox = Box.createVerticalBox();
		
		final JPanel settingsPanel = new JPanel();
		settingsPanel.add( new JLabel( Language.getText( "wordCloud.font" ) ) );
		final JComboBox< String > fontComboBox = new JComboBox<>( GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames() );
		fontComboBox.setSelectedItem( Settings.getString( Settings.KEY_WORD_CLOUD_FONT ) );
		fontComboBox.setMaximumRowCount( 13 );
		settingsPanel.add( fontComboBox );
		settingsPanel.add( new JLabel( Language.getText( "wordCloud.minFontSize" ) ) );
		SpinnerNumberModel snm;
		final JSpinner minFontSizeSpinner = new JSpinner( snm = new SpinnerNumberModel( Settings.getInt( Settings.KEY_WORD_CLOUD_MIN_FONT_SIZE ), 1, 500, 1 ) );
		minFontSizeSpinner.setToolTipText( Language.getText( "miscSettings.validRangeAndDefaultToolTip", snm.getMinimum(), snm.getMaximum(), Settings.getDefaultInt( Settings.KEY_WORD_CLOUD_MIN_FONT_SIZE ) ) );
		settingsPanel.add( minFontSizeSpinner );
		settingsPanel.add( new JLabel( Language.getText( "wordCloud.maxFontSize" ) ) );
		final JSpinner maxFontSizeSpinner = new JSpinner( snm = new SpinnerNumberModel( Settings.getInt( Settings.KEY_WORD_CLOUD_MAX_FONT_SIZE ), 1, 500, 1 ) );
		maxFontSizeSpinner.setToolTipText( Language.getText( "miscSettings.validRangeAndDefaultToolTip", snm.getMinimum(), snm.getMaximum(), Settings.getDefaultInt( Settings.KEY_WORD_CLOUD_MAX_FONT_SIZE ) ) );
		settingsPanel.add( maxFontSizeSpinner );
		settingsPanel.add( new JLabel( Language.getText( "wordCloud.maxWords" ) ) );
		final JSpinner maxWordsSpinner = new JSpinner( snm = new SpinnerNumberModel( Settings.getInt( Settings.KEY_WORD_CLOUD_MAX_WORDS ), 1, 2000, 1 ) );
		maxWordsSpinner.setToolTipText( Language.getText( "miscSettings.validRangeAndDefaultToolTip", snm.getMinimum(), snm.getMaximum(), Settings.getDefaultInt( Settings.KEY_WORD_CLOUD_MAX_WORDS ) ) );
		settingsPanel.add( maxWordsSpinner );
		final JCheckBox useColorsCheckBox = new JCheckBox( Language.getText( "wordCloud.useColors" ), Settings.getBoolean( Settings.KEY_WORD_CLOUD_USE_COLORS ) );
		settingsPanel.add( useColorsCheckBox );
		northBox.add( settingsPanel );
		
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 0, 15, 0, 15 ) );
		final JButton regenerateButton = new JButton();
		GuiUtils.updateButtonText( regenerateButton, "wordCloud.regenerateCloudButton" );
		buttonsPanel.add( regenerateButton );
		final JButton restoreDefaultsButton = new JButton();
		GuiUtils.updateButtonText( restoreDefaultsButton, "button.restoreDefaults" );
		buttonsPanel.add( restoreDefaultsButton );
		final JButton closeButton = createCloseButton( "button.close" );
		buttonsPanel.add( closeButton );
		final JButton abortButton = new JButton();
		GuiUtils.updateButtonText( abortButton, "button.abort" );
		buttonsPanel.add( abortButton );
		northBox.add( buttonsPanel );
		
		getContentPane().add( northBox, BorderLayout.NORTH );
		
		final JScrollPane canvasScrollPane = new JScrollPane();
		final JComponent wordCloudCanvas = new JComponent() {
			// Scene boundaries
			int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
			@Override
			public Dimension getPreferredSize() {
				return new Dimension( x2 - x1 + 1, y2 - y1 + 1 );
			}
			@Override
			public void paint( final Graphics g ) {
				if ( getWidth() == 0 || getHeight() == 0 )
					return;
				
				final Graphics2D g2 = (Graphics2D) g;
				g2.setBackground( Color.BLACK );
				g2.clearRect( 0, 0, getWidth(), getHeight() );
				
				if ( renderableWords == null ) {
					g.setColor( Color.WHITE );
					g.setFont( g.getFont().deriveFont( Font.BOLD ) );
					final String message = Language.getText( aborted ? "wordCloud.aborted" : "wordCloud.generatingWordCloud" );
					final FontMetrics fontMetrics = g.getFontMetrics();
					g.drawString( message, ( getWidth() - fontMetrics.stringWidth( message ) ) >> 1, ( getHeight() + fontMetrics.getAscent() ) >> 1 );
					if ( !aborted && generatorThread == null ) {
						scrollToCenter();
						generatorThread = new ControlledThread( "Word cloud generator" ) {
							@Override
							public void run() {
								GuiUtils.setComponentTreeEnabled( northBox, false );
								abortButton.setEnabled( true );
								
								final Font baseFont = new Font( (String) fontComboBox.getSelectedItem(), Font.PLAIN, 10 );
								
								y2 = x2 = y1 = x1 = 0;
								
								final RenderableWord[] renderableWords = new RenderableWord[ Math.min( wordList.size(), (Integer) maxWordsSpinner.getValue() ) ];
								
								final int DISPERSION = 256; // Random.nextInt() is faster if power of 2
								
								if ( renderableWords.length > 0 ) {
									final int   maxFontSize   = (Integer) maxFontSizeSpinner.getValue();
									final int   minFontSize   = (Integer) minFontSizeSpinner.getValue();
									final float deltaFontSize = maxFontSize - minFontSize;
									
									final int   maxValue   = wordList.get( 0 ).value2;
									final int   minValue   = Math.min( wordList.get( renderableWords.length - 1 ).value2, maxValue );
									final float deltaValue = maxValue - minValue;
									
									final int    POINTS_PER_ROUND = 128;
									final Random random = new Random();
									final double deltaB = 10.0 / POINTS_PER_ROUND;                                             // 10 pixel per rounds
									final double deltaA = deltaB * canvasScrollPane.getWidth() / canvasScrollPane.getHeight(); // height cannot be 0
									
									for ( int i = 0; i < renderableWords.length; i++ ) {
										if ( requestedToCancel )
											break;
										
										final RenderableWord rw = renderableWords[ i ] = new RenderableWord();
										final Pair< String, Integer > pair = wordList.get( i );
										
										final float fontSize =  maxValue == minValue ? ( minFontSize + maxFontSize ) >> 1
												: minFontSize + ( pair.value2.intValue() - minValue ) * deltaFontSize / deltaValue;
										
										rw.font = baseFont.deriveFont( fontSize );
										
										final Rectangle2D bounds = g.getFontMetrics( rw.font ).getStringBounds( pair.value1, g );
										final int         shiftX = -( (int) bounds.getWidth () >> 1 ) - ( DISPERSION >> 1 );
										final int         shiftY = -( (int) bounds.getHeight() >> 1 ) - ( DISPERSION >> 1 );
										rw.baseLine = -(int) bounds.getY();
										rw.x2       = (int) bounds.getWidth () - 1;
										rw.y2       = (int) bounds.getHeight() - 1;
										
										// Try to place in increasing spiral line.
										// The spiral is an ellipse whose 'a' and 'b' values come from the component's width and height
										boolean placeable = false;
										RenderableWord rw2;
										for ( double t = 0, a = 0, b = 0; !placeable; t += Math.PI * 2 / POINTS_PER_ROUND, a += deltaA, b += deltaB ) {
											if ( requestedToCancel )
												break;
											
											rw.moveTo( (int) ( a * Math.cos( t ) ) + shiftX + random.nextInt( DISPERSION ), (int) ( b * Math.sin( t ) ) + shiftY + random.nextInt( DISPERSION ) );
											
											placeable = true;
											for ( int j = 0; j < i; j++ ) {
												rw2 = renderableWords[ j ];
												if ( !( rw.x1 > rw2.x2 || rw.y1 > rw2.y2 || rw.x2 < rw2.x1 || rw.y2 < rw2.y1 ) ) {
													placeable = false;
													break;
												}
											}
										}
										
										if ( rw.x1 < x1 ) x1 = rw.x1;
										if ( rw.y1 < y1 ) y1 = rw.y1;
										if ( rw.x2 > x2 ) x2 = rw.x2;
										if ( rw.y2 > y2 ) y2 = rw.y2;
									}
								}
								
								if ( requestedToCancel )  {
									aborted = true;
									y2 = x2 = y1 = x1 = 0;
								}
								else {
									// Empty border:
									x1 -= 10;
									y1 -= 10;
									x2 += 10;
									y2 += 10;
									WordCloudDialog.this.renderableWords = renderableWords;
								}
								generatorThread = null;
								
								// Canvas size might have changed:
								invalidate();
								canvasScrollPane.validate();
								scrollToCenter();
								canvasScrollPane.repaint();
								
								GuiUtils.setComponentTreeEnabled( northBox, true );
								abortButton.setEnabled( false );
							}
						};
						generatorThread.start();
					}
				}
				else {
					g2.translate( Math.max( getWidth(), x2 - x1 + 1 ) >> 1, Math.max( getHeight(), y2 - y1 + 1 ) >> 1 );
					
					final boolean useColors = useColorsCheckBox.isSelected();
					for ( int i = 0; i < renderableWords.length; i++ ) {
						final RenderableWord rw = renderableWords[ i ];
						g2.setColor( useColors ? WORD_COLORS[ i & 0x07 ] : Color.WHITE );
						g2.setFont( rw.font );
						g2.drawString( wordList.get( i ).value1, rw.x1, rw.y1 + rw.baseLine );
					}
				}
			}
			private void scrollToCenter() {
				canvasScrollPane.getHorizontalScrollBar().setValue( ( x2 - x1 + 1 - canvasScrollPane.getVisibleRect().width  ) >> 1 );
				canvasScrollPane.getVerticalScrollBar  ().setValue( ( y2 - y1 + 1 - canvasScrollPane.getVisibleRect().height ) >> 1 );
			}
		};
		canvasScrollPane.setViewportView( wordCloudCanvas );
		getContentPane().add( canvasScrollPane, BorderLayout.CENTER );
		GuiUtils.makeComponentDragScrollable( wordCloudCanvas );
		
		final ActionListener settingsActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				aborted = false;
				if ( event != null ) {
					if ( event.getSource() == fontComboBox ) {
						Settings.set( Settings.KEY_WORD_CLOUD_FONT, fontComboBox.getSelectedItem() );
						renderableWords = null;
					}
					else if ( event.getSource() == useColorsCheckBox )
						Settings.set( Settings.KEY_WORD_CLOUD_USE_COLORS, useColorsCheckBox.isSelected() );
				}
				
				wordCloudCanvas.repaint();
			}
		};
		final ChangeListener settingsChangeListener = new ChangeListener() {
			@Override
			public void stateChanged( final ChangeEvent event ) {
				aborted = false;
				if ( event != null ) {
					if ( event.getSource() == minFontSizeSpinner      )
						Settings.set( Settings.KEY_WORD_CLOUD_MIN_FONT_SIZE, minFontSizeSpinner.getValue() );
					else if ( event.getSource() == maxFontSizeSpinner )
						Settings.set( Settings.KEY_WORD_CLOUD_MAX_FONT_SIZE, maxFontSizeSpinner.getValue() );
					else if ( event.getSource() == maxWordsSpinner    )
						Settings.set( Settings.KEY_WORD_CLOUD_MAX_WORDS    , maxWordsSpinner.getValue() );
				}
				
				renderableWords = null;
				wordCloudCanvas.repaint();
			}
		};
		
		fontComboBox      .addActionListener( settingsActionListener );
		minFontSizeSpinner.addChangeListener( settingsChangeListener );
		maxFontSizeSpinner.addChangeListener( settingsChangeListener );
		maxWordsSpinner   .addChangeListener( settingsChangeListener );
		useColorsCheckBox .addActionListener( settingsActionListener );
		
		regenerateButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				aborted = false;
				renderableWords = null;
				wordCloudCanvas.repaint();
			}
		} );
		restoreDefaultsButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				aborted = false;
				
				fontComboBox.setSelectedItem  ( Settings.getDefaultString ( Settings.KEY_WORD_CLOUD_FONT          ) );
				minFontSizeSpinner.setValue   ( Settings.getDefaultInt    ( Settings.KEY_WORD_CLOUD_MIN_FONT_SIZE ) );
				maxFontSizeSpinner.setValue   ( Settings.getDefaultInt    ( Settings.KEY_WORD_CLOUD_MAX_FONT_SIZE ) );
				maxWordsSpinner   .setValue   ( Settings.getDefaultInt    ( Settings.KEY_WORD_CLOUD_MAX_WORDS     ) );
				useColorsCheckBox .setSelected( Settings.getDefaultBoolean( Settings.KEY_WORD_CLOUD_USE_COLORS    ) );
				
				Settings.set( Settings.KEY_WORD_CLOUD_FONT         , fontComboBox      .getSelectedItem() );
				Settings.set( Settings.KEY_WORD_CLOUD_USE_COLORS   , useColorsCheckBox .isSelected     () );
				Settings.set( Settings.KEY_WORD_CLOUD_MIN_FONT_SIZE, minFontSizeSpinner.getValue       () );
				Settings.set( Settings.KEY_WORD_CLOUD_MAX_FONT_SIZE, maxFontSizeSpinner.getValue       () );
				Settings.set( Settings.KEY_WORD_CLOUD_MAX_WORDS    , maxWordsSpinner   .getValue       () );
			}
		} );
		
		abortButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final ControlledThread gt = generatorThread;
				if ( gt != null )
					gt.requestToCancel();
			}
		} );
		
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing( final WindowEvent event ) {
				final ControlledThread gt = generatorThread;
				if ( gt != null )
					gt.requestToCancel();
			};
		} );
		
		maximizeWithMarginAndShow( 30, null, closeButton, false );
	}
	
}
