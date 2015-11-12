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
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.mousepracticegame.Model.Disc;
import hu.belicza.andras.sc2gears.ui.mousepracticegame.Model.FloatingText;
import hu.belicza.andras.sc2gears.util.ControlledThread;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.PlayerColor;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.SwingUtilities;

/**
 * Controller of the Mouse practice game.
 * 
 * @author Andras Belicza
 */
class Controller extends ControlledThread {
	
	private final float FLOATING_TEXT_SPEED;  // pixel/iteration
	private final int   MAX_FLOATING_TEXT_AGE = 2000;
	
	/** Reference to the game view.             */
	private final View    view;
	/** Tells if custom rules is to be enabled. */
	public  final boolean enableCustomRules;
	/** Reference to the game model.            */
	private final Model   model = new Model();
	/** Game rules.                             */
	private final Rules   rules;
	
	/** Task to be executed on end. It will be invoked on the AWT event dispatching thread. */
	private final Runnable onEndTask;
	
	/** Mouse adapter handling mouse events on the view. */
	private final MouseAdapter mouseAdapter = new MouseAdapter() {
		@Override
		public void mousePressed( final MouseEvent event ) {
			if ( event.getButton() != GuiUtils.MOUSE_BUTTON_LEFT && event.getButton() != GuiUtils.MOUSE_BUTTON_RIGHT )
				return;
			
			final int x = event.getX();
			final int y = event.getY();
			
			// We're accessing and modifying the model from another thread, synchronize it!
			synchronized ( model ) {
				model.accuracyCount++;
				boolean hitDisc = false;
				for ( int i = model.discList.size() - 1; i >= 0; i-- ) { // Downward so we can easily remove
					final Disc disc = model.discList.get( i );
					// Distance square
					final float d2 = ( x - disc.x ) * ( x - disc.x ) + ( y - disc.y ) * ( y - disc.y );
					if ( d2 < disc.radius * disc.radius ) {
						model.discList.remove( i );
						
						model.hits++;
						final float accuracy = (float) ( 1 - Math.sqrt( d2 ) / disc.radius );
						model.accuracySum += accuracy;
						int discScore = (int) ( accuracy * rules.maxDiscScore );
						if ( event.getButton() == GuiUtils.MOUSE_BUTTON_RIGHT ^ !disc.friendly ) // XOR
							discScore = -discScore;
						model.score += discScore;
						
						final FloatingText floatingText = new FloatingText();
						floatingText.x     = disc.x;
						floatingText.y     = disc.y;
						floatingText.text  = ( discScore > 0 ? "+" : "" ) + discScore;
						floatingText.setInitialColor( discScore < 0 ? PlayerColor.PINK.color : PlayerColor.LIGHT_GREEN.color );
						model.floatingTextList.add( floatingText );
						
						hitDisc = true;
						// Do not "break", discs may overlap
					}
				}
				if ( !hitDisc ) {
					model.floatingTextList.add( createMissedFloatingText( x, y ) );
					// Game over condition
					if ( ++model.missed == rules.maxDiscsMissed ) {
						model.gameOver = true;
						requestToCancel();
					}
					// We have to call repaint here:
					view.repaint();
				}
			}
		}
	};
	
	/**
	 * Creates a new Controller.
	 * @param view reference to the game view
	 * @param onEndTask task to be executed on end; will be invoked on the AWT event dispatching thread
	 */
	public Controller( final View view, final boolean enableCustomRules, final Runnable onEndTask ) {
		super( "Mouse practice game controller" );
		
		this.view              = view;
		this.enableCustomRules = enableCustomRules;
		this.onEndTask         = onEndTask;
		
		if ( enableCustomRules )
			rules = new Rules( Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_FPS ), Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISC_RADIUS ), Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISC_AGE ), Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISC_SCORE ), Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISCS_MISSED ), Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_DISC_SPEED ), Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_FRIENDLY_DISC_PROBABILITY ), Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_INITIAL_DELAY_FOR_NEW_DISC ), Settings.getInt( Settings.KEY_SETTINGS_MOUSE_GAME_NEW_DISC_DELAY_DECREMENT ), Settings.getString( Settings.KEY_SETTINGS_MOUSE_GAME_RANDOM_SEED ), Settings.getBoolean( Settings.KEY_SETTINGS_MOUSE_GAME_PAINT_DISC_CENTER_CROSS ), Settings.getBoolean( Settings.KEY_SETTINGS_MOUSE_GAME_PAINT_MAX_DISC_OUTLINE ) );
		else
			rules = new Rules( Settings.getDefaultInt( Settings.KEY_SETTINGS_MOUSE_GAME_FPS ), Settings.getDefaultInt( Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISC_RADIUS ), Settings.getDefaultInt( Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISC_AGE ), Settings.getDefaultInt( Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISC_SCORE ), Settings.getDefaultInt( Settings.KEY_SETTINGS_MOUSE_GAME_MAX_DISCS_MISSED ), Settings.getDefaultInt( Settings.KEY_SETTINGS_MOUSE_GAME_DISC_SPEED ), Settings.getDefaultInt( Settings.KEY_SETTINGS_MOUSE_GAME_FRIENDLY_DISC_PROBABILITY ), Settings.getDefaultInt( Settings.KEY_SETTINGS_MOUSE_GAME_INITIAL_DELAY_FOR_NEW_DISC ), Settings.getDefaultInt( Settings.KEY_SETTINGS_MOUSE_GAME_NEW_DISC_DELAY_DECREMENT ), Settings.getDefaultString( Settings.KEY_SETTINGS_MOUSE_GAME_RANDOM_SEED ), Settings.getDefaultBoolean( Settings.KEY_SETTINGS_MOUSE_GAME_PAINT_DISC_CENTER_CROSS ), Settings.getDefaultBoolean( Settings.KEY_SETTINGS_MOUSE_GAME_PAINT_MAX_DISC_OUTLINE ) );
		
		FLOATING_TEXT_SPEED = 40f * rules.sleepBetweenIterations / 1000;
	}
	
	@Override
	public void run() {
		view.addMouseListener( mouseAdapter );
		view.setModel( model );
		view.setRules( rules );
		view.repaint();
		
		final Random random          = new Random();
		model.startRandomSeed = rules.randomSeed == null ? random.nextLong() : rules.randomSeed;
		random.setSeed( model.startRandomSeed );
		int          delayForNewDisc = rules.initialDelayForNewDisc;
		int          timeForNewDisc  = delayForNewDisc;
		
		try {
			while ( !requestedToCancel ) {
				
				// Calculate next iteration
				synchronized ( model ) {
					model.ageMs += rules.sleepBetweenIterations;
					
					// Step discs
					for ( int i = model.discList.size() - 1; i >= 0; i-- ) { // Downward so we can easily remove
						final Disc disc = model.discList.get( i );
						if ( ( disc.ageMs += rules.sleepBetweenIterations ) > rules.maxDiscAge ) {
							model.discList.remove( i );
							
							model.floatingTextList.add( createMissedFloatingText( disc.x, disc.y ) );
							// Game over condition
							if ( ++model.missed == rules.maxDiscsMissed ) {
								model.gameOver = true;
								requestToCancel();
							}
						}
						else {
							disc.x += disc.vx;
							disc.y += disc.vy;
							if ( disc.ageMs < ( rules.maxDiscAge >> 1 ) ) 
								disc.radius = ( rules.maxDiscRadius << 1 ) * disc.ageMs / rules.maxDiscAge;
							else
								disc.radius = ( rules.maxDiscRadius << 1 ) * ( rules.maxDiscAge - disc.ageMs ) / rules.maxDiscAge;
						}
					}
					// Step floating texts
					for ( int i = model.floatingTextList.size() - 1; i >= 0; i-- ) { // Downward so we can easily remove
						final FloatingText floatingText = model.floatingTextList.get( i );
						if ( ( floatingText.ageMs += rules.sleepBetweenIterations ) > MAX_FLOATING_TEXT_AGE )
							model.floatingTextList.remove( i );
						else {
							floatingText.y -= FLOATING_TEXT_SPEED;
							floatingText.color = new Color(
								floatingText.red   - floatingText.red   * floatingText.ageMs / MAX_FLOATING_TEXT_AGE,
								floatingText.green - floatingText.green * floatingText.ageMs / MAX_FLOATING_TEXT_AGE,
								floatingText.blue  - floatingText.blue  * floatingText.ageMs / MAX_FLOATING_TEXT_AGE
                            );
						}
					}
					
					// Check if a new disc has to be launched:
					if ( model.ageMs > timeForNewDisc ) {
						delayForNewDisc -= rules.newDiscDelayDecrement;
						timeForNewDisc += delayForNewDisc;
						final Disc disc = new Disc();
						disc.x        = ( rules.maxDiscRadius << 1 ) + random.nextInt( Consts.WIDTH  - ( rules.maxDiscRadius << 2 ) );
						disc.y        = ( rules.maxDiscRadius << 1 ) + random.nextInt( Consts.HEIGHT - ( rules.maxDiscRadius << 2 ) );
						final double vAngle = random.nextInt( 180 ) / Math.PI; // 360 deg => 2PI rad
						disc.vx       = (float) ( Math.cos( vAngle ) * rules.discSpeed );
						disc.vy       = (float) ( Math.sin( vAngle ) * rules.discSpeed );
						disc.friendly = random.nextInt( 100 ) < rules.friendlyDiscProbability;
						model.discList.add( disc );
					}
                }
				
				view.repaint();
				
				try {
					Thread.sleep( rules.sleepBetweenIterations );
				} catch ( final InterruptedException ie ) {
					ie.printStackTrace();
				}
			}
		} finally {
			view.removeMouseListener( mouseAdapter );
			SwingUtilities.invokeLater( onEndTask );
		}
	}
	
	/**
	 * Creates and returns a "MISSED!" floating text.
	 * @param x x coordinate of the returned floating text
	 * @param y y coordinate of the returned floating text
	 * @return a "MISSED!" floating text
	 */
	private static FloatingText createMissedFloatingText( final float x, final float y ) {
		final FloatingText floatingText = new FloatingText();
		
		floatingText.x    = x;
		floatingText.y    = y;
		floatingText.text = Language.getText( "mousePracticeGame.missed2" );
		floatingText.setInitialColor( PlayerColor.PINK.color );
		
		return floatingText;
	}
	
	/**
	 * Returns the model of the game.
	 * @return the model of the game
	 */
	public Model getModel() {
		return model;
	}
	
}
