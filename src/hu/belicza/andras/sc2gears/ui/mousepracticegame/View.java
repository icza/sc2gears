/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.mousepracticegame;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.mousepracticegame.Model.Disc;
import hu.belicza.andras.sc2gears.ui.mousepracticegame.Model.FloatingText;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerColor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.JComponent;

/**
 * View of the Mouse practice game.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
class View extends JComponent {
	
	/** Cache of the Score string.    */
	private final String scoreString    = Language.getText( "mousePracticeGame.score"    );
	/** Cache of the Accuracy string. */
	private final String accuracyString = Language.getText( "mousePracticeGame.accuracy" );
	/** Cache of the Hits string.     */
	private final String hitsString     = Language.getText( "mousePracticeGame.hits"     );
	/** Cache of the Missed string.   */
	private final String missedString   = Language.getText( "mousePracticeGame.missed"   );
	
	/** Double stroke. */
	private final Stroke DOUBLE_STROKE = new BasicStroke( 2 ) ;
	
	/** Reference to the game model. */
	private volatile Model model_;
	
	/** Reference to the game model we're currently painting (doesn't change). */
	private Model model;
	/** Reference to the game rules.                                           */
	private Rules rules;
	
	/**
	 * Creates a new View.
	 */
    public View() {
		setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ) );
    }
	
	/**
	 * Sets the game model.
	 * @param model model to be set
	 */
	public void setModel( final Model model ) {
		model_ = model;
	}
	
	/**
	 * Sets the game rules.
	 * @param rules rules to be set
	 */
	public void setRules( final Rules rules ) {
		this.rules = rules;
	}
	
	@Override
	public void paint( final Graphics g ) {
		final Graphics2D g2 = (Graphics2D) g;
		g2.setBackground( Color.BLACK );
		g2.clearRect( 0, 0, Consts.WIDTH, Consts.HEIGHT );
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		
		model = model_;
		if ( model == null ) {
			paintInfo( g2 );
		}
		else {
			// Clone the model so we can concurrently paint it and have the controller modify it
			model = model_.clone();
			final boolean colorBind = Settings.getBoolean( Settings.KEY_MOUSE_PRACTICE_COLOR_BLIND );
			
			// Draw objects of the model
			for ( final Disc disc : model.discList ) {
				if ( rules.paintMaxDiscOutline ) {
    				g2.setColor( Color.GRAY );
    				g2.drawOval( (int) ( disc.x - rules.maxDiscRadius ), (int) ( disc.y - rules.maxDiscRadius ), ( rules.maxDiscRadius << 1 ) + 1, ( rules.maxDiscRadius << 1 ) + 1 );
				}
				
				g2.setColor( disc.friendly ? PlayerColor.GREEN.color : PlayerColor.RED.color );
				if ( disc.friendly || !colorBind )
					g2.fillOval( (int) ( disc.x - disc.radius ), (int) ( disc.y - disc.radius ), (int) ( disc.radius*2 ) + 1, (int) ( disc.radius*2 ) + 1 );
				else {
					final Stroke storedStroke = g2.getStroke();
					g2.setStroke( DOUBLE_STROKE );
					g2.drawOval( (int) ( disc.x - disc.radius ), (int) ( disc.y - disc.radius ), (int) ( disc.radius*2 ) + 1, (int) ( disc.radius*2 ) + 1 );
					g2.setStroke( storedStroke );
				}
				
				if ( rules.paintDiscCenterCross && disc.radius > 1 ) {
    				g2.setColor( Color.WHITE );
    				g2.drawLine( (int) ( disc.x - disc.radius/2 ), (int) disc.y, (int) ( disc.x + disc.radius/2 ), (int) disc.y );
    				g2.drawLine( (int) disc.x, (int) ( disc.y - disc.radius/2 ), (int) disc.x, (int) ( disc.y + disc.radius/2 ) );
				}
			}
			
			final FontMetrics fontMetrics = g2.getFontMetrics();
			final int shiftY = fontMetrics.getAscent() >> 1;
			for ( final FloatingText floatingText : model.floatingTextList ) {
				g2.setColor( floatingText.color );
				g2.drawString( floatingText.text, floatingText.x - ( fontMetrics.stringWidth( floatingText.text ) >> 1 ), floatingText.y + shiftY );
			}
			
			// Draw texts
			paintTexts( g2 );
		}
	}
	
	/**
	 * Paints info message about the game.
	 * @param g2 graphics context in which to paint
	 */
	private void paintInfo( final Graphics2D g2 ) {
		g2.setFont( g2.getFont().deriveFont( Font.BOLD, 22 ) );
		final FontMetrics fontMetrics = g2.getFontMetrics();
		final int textsCount = 5;
		final int lineHeight = fontMetrics.getHeight() << 1;
		int y = ( ( Consts.HEIGHT - textsCount * lineHeight ) >> 1 ) + fontMetrics.getAscent();
		
		g2.setColor( Color.LIGHT_GRAY );
		for ( int i = 1; i <= textsCount; i++, y += lineHeight ) {
			final String text = Language.getText( "mousePracticeGame.info" + i );
			g2.drawString( text, ( Consts.WIDTH - fontMetrics.stringWidth( text ) ) >> 1, y );
		}
	}
	
	/**
	 * Paints the texts of the game.
	 * @param g2 graphics context in which to paint
	 */
	private void paintTexts( final Graphics2D g2 ) {
		g2.setFont( g2.getFont().deriveFont( Font.BOLD ) );
		FontMetrics fontMetrics = g2.getFontMetrics();
		final int fontHeight = fontMetrics.getHeight();
		
		g2.setColor( Color.LIGHT_GRAY );
		g2.drawString( scoreString   , 5, fontHeight      );
		g2.drawString( accuracyString, 5, fontHeight << 1 );
		final int dataX = 10 + Math.max( fontMetrics.stringWidth( scoreString ), fontMetrics.stringWidth( accuracyString ) );
		g2.drawString( Integer.toString( model.score ), dataX, fontHeight );
		g2.drawString( ( (int) ( 100 * model.accuracySum / model.accuracyCount ) ) + "%", dataX, fontHeight << 1);
		
		final int roomForHits  = fontMetrics.stringWidth( "999999" ) + 5;
		final int rightColumnX = Consts.WIDTH - 5 - roomForHits - Math.max( fontMetrics.stringWidth( hitsString ), fontMetrics.stringWidth( missedString ) );
		g2.drawString( hitsString  , rightColumnX, fontHeight      );
		g2.drawString( missedString, rightColumnX, fontHeight << 1 );
		g2.drawString( Integer.toString( model.hits   ), Consts.WIDTH - roomForHits, fontHeight      );
		g2.drawString( Integer.toString( model.missed ), Consts.WIDTH - roomForHits, fontHeight << 1 );
		
		if ( model.gameOver ) {
			g2.setColor( Color.LIGHT_GRAY );
			g2.setFont( g2.getFont().deriveFont( 50f ) );
			fontMetrics = g2.getFontMetrics();
			final String gameOverText = Language.getText( "mousePracticeGame.gameOver" );
			g2.drawString( gameOverText, ( Consts.WIDTH - fontMetrics.stringWidth( gameOverText ) ) >> 1, ( Consts.HEIGHT - fontMetrics.getHeight() ) >> 1 );
		}
	}
	
	@Override
	public Dimension getPreferredSize() {
		return Consts.SIZE;
	}
	@Override
	public Dimension getMinimumSize() {
		return Consts.SIZE;
	}
	@Override
	public Dimension getMaximumSize() {
		return Consts.SIZE;
	}
	
}
