/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.moduls.replayanal;

import static hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.FRAME_BITS_IN_SECOND;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.ReplayUtils;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.ControlledThread;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gears.util.Task;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * An animator that animates the replay in a new thread.
 * 
 * @author Andras Belicza
 */
public class Animator implements Runnable {
	
	public enum Speed {
		QUARTER   ( "x1/4", -2 ),
		HALF      ( "x1/2", -1 ),
		ONE       ( "x1"  , 0  ),
		TWO       ( "x2"  , 1  ),
		FOUR      ( "x4"  , 2  ),
		EIGHT     ( "x8"  , 3  ),
		SIXTEEN   ( "x16" , 4  ),
		THIRTY_TWO( "x32" , 5  );
		
		/** String value of the FPS value.                 */
		public  final String stringValue;
		/** Number of bit shift associated with the speed. */
		private final int    bitShift;
		
		/**
		 * Creates a new FPS.
		 * @param stringValue string value of the FPS
		 * @param bitShift    number of bit shift associated with the speed
		 */
		private Speed( final String stringValue, final int bitShift ) {
			this.stringValue = stringValue;
			this.bitShift    = bitShift;
		}
		
		/**
		 * Returns the elapsed play time between frames.
		 * @param fps fps value to base on
		 * @return the elapsed time between frames
		 */
		public int getPlayTimeBetweenFrames( final int delayMsBetweenFrames ) {
			return bitShift >= 0 ? delayMsBetweenFrames << bitShift : delayMsBetweenFrames >> (-bitShift);
		}
		
		@Override
		public String toString() {
			return stringValue;
		}
	}
	
	/** The UI component of the control interface.                                */
	private final JComponent   controlUIComponent;
	/** Progress bar to indicate the current time in the replay and the progress. */
	private final JProgressBar currentTimeProgressBar = new JProgressBar();
	
	/** Reference to the replay.                                             */
	private final Replay               replay;
	/** Reference to the chart canvas repainter listener.                    */
	private final ActionListener       chartCanvasRepainterListener;
	/** Reference to the holder of the chart params.                         */
	private final Holder< ChartParams > chartParamsHolder;
	/** Reference to the holder of the marker X.                             */
	private final Holder< Integer     > markerXHolder;
	/** Reference to the holder of the marker frame.     */
	private final Holder< Integer     > markerFrameHolder;
	/** Reference to the task which synchronizes the action list to a frame. */
	private final Task  < Integer     > syncActionListToFrameTask;
	
	/** The current time in the replay in ms. */
	private volatile int currentTimeMs;
	
	/** Reference to the Play/Pause label. */
	private final JLabel playPauseLabel = GuiUtils.createIconLabelButton( Icons.CONTROL, "module.repAnalyzer.tab.charts.animate.playToolTip" );
	
	/** Reference to the animator thread. */
	private ControlledThread animatorThread;
	
	/**
	 * Creates a new Animator.
	 * @param replay                       reference to the replay
	 * @param chartCanvasRepainterListener reference to the chart canvas repaint listener
	 * @param layeredPane                  reference to the layered pane
	 * @param chartParamsHolder            reference to the holder of the chart params
	 * @param markerFrameHolder            reference to the holder of the marker frame
	 * @param markerXHolder                reference to the holder of the marker X
	 * @param syncActionListToFrameTask    reference to the task which synchronizes the action list to a frame
	 */
	public Animator( final Replay replay, final ActionListener chartCanvasRepainterListener, final JLayeredPane layeredPane, final Holder< ChartParams > chartParamsHolder, final Holder< Integer > markerXHolder, final Holder< Integer > markerFrameHolder, final Task< Integer > syncActionListToFrameTask ) {
		this.replay                       = replay;
		this.chartCanvasRepainterListener = chartCanvasRepainterListener;
		this.chartParamsHolder            = chartParamsHolder;
		this.markerXHolder                = markerXHolder;
		this.markerFrameHolder            = markerFrameHolder;
		this.syncActionListToFrameTask    = syncActionListToFrameTask;
		
		controlUIComponent = buildControlUI( layeredPane );
		setCurrentTime( 0 );
	}
	
	/**
	 * Builds the UI of the control interface.
	 * @param layeredPane reference to the layered pane
	 */
	@SuppressWarnings("serial")
	private JComponent buildControlUI( final JLayeredPane layeredPane ) {
		final Box box = Box.createHorizontalBox();
		box.add( Box.createHorizontalStrut( 2 ) );
		
		box.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.animate.animate" ) ) );
		box.add( Box.createHorizontalStrut( 3 ) );
		final MouseListener playPauseListener = new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				handlePlayPause();
			}
		};
		playPauseLabel.addMouseListener( playPauseListener );
		box.add( playPauseLabel );
		box.add( Box.createHorizontalStrut( 3 ) );
		final JLabel jumpToBeginningLabel = GuiUtils.createIconLabelButton( Icons.CONTROL_SKIP_180, "module.repAnalyzer.tab.charts.animate.jumpToBeginningToolTip" );
		final MouseListener jumpToBeginningListener = new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				setCurrentTime( 0 );
				syncChartToCurrentFrame();
			}
		};
		jumpToBeginningLabel.addMouseListener( jumpToBeginningListener );
		box.add( jumpToBeginningLabel );
		
		final JLabel jumpBackwardLabel = GuiUtils.createIconLabelButton( Icons.CONTROL_DOUBLE_180, "module.repAnalyzer.tab.charts.animate.jumpBackwardToolTip" );
		final MouseListener jumpBackwardListener = new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				// +1 to get rid of rounding problems (e.g. 60 seconds remain 60 and not 59)
				jumpTime( Settings.getInt( Settings.KEY_SETTINGS_MISC_ANIMATOR_JUMP_TIME ) * -1000 - 1 );
			}
		};
		jumpBackwardLabel.addMouseListener( jumpBackwardListener );
		box.add( jumpBackwardLabel );
		
		currentTimeProgressBar.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		currentTimeProgressBar.addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				setCurrentTime( replay.gameLength * 500 * event.getX() / currentTimeProgressBar.getWidth() );
				syncChartToCurrentFrame();
			}
		} );
		currentTimeProgressBar.setMaximum( replay.converterGameSpeed.convertToRealTime( replay.gameLength * 500 ) );
		currentTimeProgressBar.setStringPainted( true );
		box.add( currentTimeProgressBar );
		
		final JLabel jumpForwardLabel = GuiUtils.createIconLabelButton( Icons.CONTROL_DOUBLE, "module.repAnalyzer.tab.charts.animate.jumpForwardToolTip" );
		final MouseListener jumpForwardListener = new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				// +1 to get rid of rounding problems (e.g. 60 seconds remain 60 and not 59)
				jumpTime( Settings.getInt( Settings.KEY_SETTINGS_MISC_ANIMATOR_JUMP_TIME ) * 1000 + 1 );
			}
		};
		jumpForwardLabel.addMouseListener( jumpForwardListener );
		box.add( jumpForwardLabel );
		
		final JLabel jumpToEndLabel = GuiUtils.createIconLabelButton( Icons.CONTROL_SKIP, "module.repAnalyzer.tab.charts.animate.jumpToEndToolTip" );
		final MouseListener jumpToEndListener = new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				setCurrentFrame( replay.gameEvents.actions.length > 0 ? replay.gameEvents.actions[ replay.gameEvents.actions.length - 1 ].frame : 0 );
				syncChartToCurrentFrame();
			}
		};
		jumpToEndLabel.addMouseListener( jumpToEndListener );
		box.add( jumpToEndLabel );
		box.add( Box.createHorizontalStrut( 8 ) );
		
		box.add( new JLabel( Language.getText( "module.repAnalyzer.tab.charts.animate.speed" ) ) );
		box.add( Box.createHorizontalStrut( 2 ) );
		
		final JLabel slowDownLabel = GuiUtils.createIconLabelButton( Icons.MINUS_SMALL, "module.repAnalyzer.tab.charts.animate.slowDownToolTip" );
		final JLabel speedUpLabel  = GuiUtils.createIconLabelButton( Icons.PLUS_SMALL, "module.repAnalyzer.tab.charts.animate.speedUpToolTip" );
		final JLabel speedLabel    = new JLabel();
		
		final Task< Integer > updateSpeedLabelsTask = new Task< Integer >() {
			@Override
			public void execute( final Integer speed ) {
				speedLabel   .setText( Speed.values()[ speed ].stringValue );
				slowDownLabel.setEnabled( speed > 0                          );
				speedUpLabel .setEnabled( speed < Speed.values().length - 1  );
			}
		};
		updateSpeedLabelsTask.execute( Settings.getInt( Settings.KEY_SETTINGS_MISC_ANIMATOR_SPEED ) );
		
		final MouseListener slowDownListener = new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				final int speed = Settings.getInt( Settings.KEY_SETTINGS_MISC_ANIMATOR_SPEED );
				if ( speed > 0 ) {
					Settings.set( Settings.KEY_SETTINGS_MISC_ANIMATOR_SPEED, speed - 1 );
					updateSpeedLabelsTask.execute( speed - 1 );
				}
			}
		};
		slowDownLabel.addMouseListener( slowDownListener );
		box.add( slowDownLabel );
		
		box.add( speedLabel );
		
		final MouseListener speedUpListener = new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				final int speed = Settings.getInt( Settings.KEY_SETTINGS_MISC_ANIMATOR_SPEED );
				if ( speed < Speed.values().length - 1 ) {
					Settings.set( Settings.KEY_SETTINGS_MISC_ANIMATOR_SPEED, speed + 1 );
					updateSpeedLabelsTask.execute( speed + 1 );
				}
			}
		};
		speedUpLabel.addMouseListener( speedUpListener );
		box.add( speedUpLabel );
		
		// Register hotkeys for the animator actions
		final InputMap  inputMap  = layeredPane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		final ActionMap actionMap = layeredPane.getActionMap();
		Object actionKey;
		// Register hotkey for jump to the beginning
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_Q, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				jumpToBeginningListener.mousePressed( null );
			}
		} );
		// Register hotkey for jump backward
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_Q, InputEvent.CTRL_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				jumpBackwardListener.mousePressed( null );
			}
		} );
		// Register hotkey for Play/Pause
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_W, InputEvent.CTRL_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				playPauseListener.mousePressed( null );
			}
		} );
		// Register hotkey for jump forward
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_E, InputEvent.CTRL_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				jumpForwardListener.mousePressed( null );
			}
		} );
		// Register hotkey for jump to the end
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_E, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				jumpToEndListener.mousePressed( null );
			}
		} );
		// Register hotkey for slow down
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_R, InputEvent.CTRL_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				slowDownListener.mousePressed( null );
			}
		} );
		// Register hotkey for speed up
		inputMap .put( KeyStroke.getKeyStroke( KeyEvent.VK_R, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK ), actionKey = new Object() );
		actionMap.put( actionKey, new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				speedUpListener.mousePressed( null );
			}
		} );
		
		return box;
	}
	
	private void handlePlayPause() {
		if ( animatorThread == null ) {
			playPauseLabel.setIcon( Icons.CONTROL_PAUSE );
			playPauseLabel.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.animate.pauseToolTip" ) );
			animatorThread = new ControlledThread( "Animator" ) {
				@Override
				public void run() {
					// If we're at the end, first rewind:
					final int maxTime = replay.gameEvents.actions.length > 0 ? ( replay.gameEvents.actions[ replay.gameEvents.actions.length - 1 ].frame * 125 ) >> ( FRAME_BITS_IN_SECOND - 3 ) : 0;
					if ( currentTimeMs >= maxTime )
						setCurrentTime( 0 );
					
					while ( !requestedToCancel )
						try {
							// It has to be "invoked" later, else many rendering bugs occur on the whole charts tab!
							SwingUtilities.invokeLater( Animator.this );
							sleep( 1000 / Settings.getInt( Settings.KEY_SETTINGS_MISC_ANIMATOR_FPS ) );
						} catch ( final InterruptedException e ) {
							e.printStackTrace();
						}
				}
			};
			animatorThread.start();
		}
		else {
			animatorThread.requestToCancel();
			animatorThread = null;
			playPauseLabel.setIcon( Icons.CONTROL );
			playPauseLabel.setToolTipText( Language.getText( "module.repAnalyzer.tab.charts.animate.playToolTip" ) );
		}
	}
	
	/**
	 * Returns the control UI component of the animator.
	 * @return the control UI component of the animator
	 */
	public JComponent getControlUIComponent() {
		return controlUIComponent;
	}
	
	/**
	 * Sets the current time in the replay.
	 */
	public void setCurrentFrame( final int frame ) {
		setCurrentTime( ( frame * 125 ) >> ( FRAME_BITS_IN_SECOND - 3 ) );
	}
	
	/**
	 * Sets the current time in the replay.
	 */
	private void setCurrentTime( final int currentTimeMs ) {
		this.currentTimeMs = currentTimeMs;
		currentTimeProgressBar.setValue( replay.converterGameSpeed.convertToRealTime( currentTimeMs ) );
		currentTimeProgressBar.setString( ReplayUtils.formatMs( currentTimeMs, replay.converterGameSpeed ) );
	}
	
	/**
	 * Synchronizes the chart to the current frame.
	 */
	private void syncChartToCurrentFrame() {
		final int frame = ( currentTimeMs << ( FRAME_BITS_IN_SECOND - 3 ) ) / 125;
		
		markerXHolder    .value = chartParamsHolder.value.frameToX( frame );
		markerFrameHolder.value = null;
		
		chartCanvasRepainterListener.actionPerformed( null );
		
		syncActionListToFrameTask.execute( frame );
	}
	
	/**
	 * Jumps the specified amount of time, can be negative.
	 * @param msToJump ms to jump, can be negative
	 * @return true if the end of replay has been reached
	 */
	private boolean jumpTime( final int msToJump ) {
		int newTime = currentTimeMs + replay.converterGameSpeed.convertToGameTime( msToJump );
		
		final boolean eorReached;
		if ( msToJump > 0 ) {
			final int maxTime = replay.gameEvents.actions.length > 0 ? ( replay.gameEvents.actions[ replay.gameEvents.actions.length - 1 ].frame * 125 ) >> ( FRAME_BITS_IN_SECOND - 3 ) : 0;
			if ( eorReached = newTime > maxTime )
				newTime = maxTime;
		}
		else {
			eorReached = false;
			if ( newTime < 0 )
				newTime = 0;
		}
		
		setCurrentTime( newTime );
		syncChartToCurrentFrame();
		
		return eorReached;
	}
	
	/**
	 * This method is called from the Animator thread, this will handle the the animation.
	 */
	@Override
	public void run() {
		if ( jumpTime( Speed.values()[ Settings.getInt( Settings.KEY_SETTINGS_MISC_ANIMATOR_SPEED ) ].getPlayTimeBetweenFrames( 1000 / Settings.getInt( Settings.KEY_SETTINGS_MISC_ANIMATOR_FPS ) ) ) )
			handlePlayPause();
	}
	
	/**
	 * Cancels the animator thread if it is running.
	 */
	public void requestToCancel() {
		if ( animatorThread != null )
			animatorThread.requestToCancel();		
	}
	
}
