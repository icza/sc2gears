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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Model of the Mouse practice game.
 * 
 * <p><i>This model can only by modified if the reference to this object is acquired as the synchronization lock!</i>
 * This lock is also used when the model is cloned (inside {@link #clone()}).</p>
 * 
 * @author Andras Belicza
 */
class Model implements Cloneable {
	
	/**
	 * Represents a live object which has a location and an age. 
	 * @author Andras Belicza
	 */
	public static class LiveObject implements Cloneable {
		/** X coordinate of the object's location. */
		public float  x;
		/** Y coordinate of the object's location. */
		public float  y;
		/** Age of the object in milliseconds.     */
		public int    ageMs;
	}
	
	/**
	 * Represents a disc.
	 * @author Andras Belicza
	 */
	public static class Disc extends LiveObject {
		/** Radius of the disc.                     */
		public float   radius;
		/** X component of the of the disc's speed. */
		public float   vx;
		/** Y component of the of the disc's speed. */
		public float   vy;
		/** Tells if this disc is friendly.         */
		public boolean friendly;
		
		@Override
		public Disc clone() {
			try {
				return (Disc) super.clone();
			} catch ( final CloneNotSupportedException cnse ) {
				throw new RuntimeException( cnse );
			}
		}
	}
	
	/**
	 * Represents a floating text.
	 * @author Andras Belicza
	 */
	public static class FloatingText extends LiveObject {
		/** The text.                                             */
		public String text;
		/** The Red component of the initial color of the text.   */
		public int    red;
		/** The Green component of the initial color of the text. */
		public int    green;
		/** The Blue component of the initial color of the text.  */
		public int    blue;
		/** The color of the text.                                */
		public Color  color;
		
		/**
		 * Sets the initial color of the text.
		 * @param color initial color to be set
		 */
		public void setInitialColor( final Color color ) {
			this.color = color;
			red   = color.getRed();
			green = color.getGreen();
			blue  = color.getBlue();
		}
		
		@Override
		public FloatingText clone() {
			try {
				return (FloatingText) super.clone();
			} catch ( final CloneNotSupportedException cnse ) {
				throw new RuntimeException( cnse );
			}
		}
	}
	
	/** Saved value of the start random seed (so the game can be "replayed"). */
	public long startRandomSeed;
	
	/** Elapsed time in the game.      */
	public int   ageMs;
	/** Current score.                 */
	public int   score;
	/** Number of discs hit.           */
	public int   hits;
	/** Number of missed discs.        */
	public int   missed;
	/** Hits counting toward accuracy. */
	public float accuracyCount;
	/** Sum of the hits accuracy.      */
	public float accuracySum;
	
	/** Tells if game is over.    */
	public boolean gameOver;
	
	/** List of discs. */
	public List< Disc         > discList         = new ArrayList< Disc         >();
	/** List of discs. */
	public List< FloatingText > floatingTextList = new ArrayList< FloatingText >();
	
	@Override
	public synchronized Model clone() {
		// Method is synchronized assuring that the model is not modified while we clone it!
		try {
			final Model clonedModel = (Model) super.clone();
			
			clonedModel.discList         = new ArrayList< Disc         >( discList.size()         );
			for ( final Disc disc : discList )
				clonedModel.discList.add( disc.clone() );
			clonedModel.floatingTextList = new ArrayList< FloatingText >( floatingTextList.size() );
			for ( final FloatingText floatingText : floatingTextList )
				clonedModel.floatingTextList.add( floatingText.clone() );
			
			return clonedModel;
		} catch ( final CloneNotSupportedException cnse ) {
			// Never to happen
			throw new RuntimeException( cnse );
		}
	}
	
}
