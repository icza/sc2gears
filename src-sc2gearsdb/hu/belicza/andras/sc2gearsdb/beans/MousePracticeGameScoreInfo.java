/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.beans;

import java.io.Serializable;

/**
 * Mouse practice game score bean class.
 * 
 * @author Andras Belicza
 */
public class MousePracticeGameScoreInfo implements Serializable {
	
    private static final long serialVersionUID = -6852747555441577134L;
    
	private String userName;
	private int    score;
	private float  accuracy;
	private int    hits;
	private Long   randomSeed;
	private int    personRank;
	
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
	
    public float getAccuracy() {
    	return accuracy;
    }
	
    public void setAccuracy( float accuracy ) {
    	this.accuracy = accuracy;
    }
	
    public int getHits() {
    	return hits;
    }
	
    public void setHits( int hits ) {
    	this.hits = hits;
    }
	
    public Long getRandomSeed() {
    	return randomSeed;
    }
	
    public void setRandomSeed( Long randomSeed ) {
    	this.randomSeed = randomSeed;
    }

	public void setPersonRank( int personRank ) {
	    this.personRank = personRank;
    }

	public int getPersonRank() {
	    return personRank;
    }
	
}
