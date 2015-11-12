/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.datastore;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Key;

/**
 * Top score table of the Mouse practice game.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>"v"</code> property.
 * 			<br>Made <code>userAgent</code>, <code>country</code> properties unindexed (as part of {@link ClientTrackedObject}).  
 * 			<br>Made {@link #userName}, {@link #accuracy}, {@link #hits}, {@link #gameLength}, {@link #gameVersion}, {@link #randomSeed} properties unindexed.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class MousePracticeGameScore extends ClientTrackedObject {
	
	/** Key of the account. */
	@Persistent
	private Key accountKey;
	
	/** User name to appear in the top score table. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String userName;
	
	/** Score. */
	@Persistent
	private int score;
	
	/** Accuracy. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private float accuracy;
	
	/** Hits. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int hits;
	
	/** Game length in milliseconds. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int gameLength;
	
	/** Version of the game. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String gameVersion;
	
	/** Start random seed of the game. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private Long randomSeed;
	
    /**
     * Creates a new MousePracticeGameScore.
     */
    public MousePracticeGameScore() {
    	setV( 1 );
    }
	
    public Key getAccountKey() {
    	return accountKey;
    }
	
    public void setAccountKey( Key accountKey ) {
    	this.accountKey = accountKey;
    }
	
    public String getUserName() {
    	return userName;
    }
	
    public void setUserName( String userName ) {
    	this.userName = userName;
    }
	
    public int getScore() {
    	return score;
    }
	
    public void setScore( int score ) {
    	this.score = score;
    }
	
	public void setAccuracy( float accuracy ) {
	    this.accuracy = accuracy;
    }

	public float getAccuracy() {
	    return accuracy;
    }
	
    public int getHits() {
    	return hits;
    }
	
    public void setHits( int hits ) {
    	this.hits = hits;
    }
	
    public int getGameLength() {
    	return gameLength;
    }
	
    public void setGameLength( int gameLength ) {
    	this.gameLength = gameLength;
    }
	
    public String getGameVersion() {
    	return gameVersion;
    }
	
    public void setGameVersion( String gameVersion ) {
    	this.gameVersion = gameVersion;
    }

	public void setRandomSeed( Long randomSeed ) {
	    this.randomSeed = randomSeed;
    }

	public Long getRandomSeed() {
	    return randomSeed;
    }

}
