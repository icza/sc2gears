/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.user.client.beans;

import hu.belicza.andras.sc2gearsdb.common.client.beans.DateRangeFilters;

import java.util.List;

/**
 * Replay filters.
 * 
 * @author Andras Belicza
 */
public class ReplayFilters extends DateRangeFilters {
	
	private List< Integer > labels;
	private String mapName;
	private String players;
	private String gameType;
	private String format;
	private String leagueMu;
	private String matchup;
	private Boolean hasComment;
	private String gateway;
	
	@Override
	public String toString() {
	    final StringBuilder builder = new StringBuilder();
		
	    if ( labels != null )
	    	builder.append( ", labels: " ).append( labels );
		if ( mapName != null )
	    	builder.append( ", mapName: " ).append( mapName );
		if ( players != null )
	    	builder.append( ", players: " ).append( players );
		if ( gameType != null )
	    	builder.append( ", gameType: " ).append( gameType );
		if ( format != null )
	    	builder.append( ", format: " ).append( format );
		if ( leagueMu != null )
	    	builder.append( ", league mu: " ).append( leagueMu );
		if ( matchup != null )
	    	builder.append( ", matchup: " ).append( matchup );
		if ( hasComment != null )
	    	builder.append( ", hasComment: " ).append( hasComment );
		if ( gateway != null )
	    	builder.append( ", gateway: " ).append( gateway );
		
		final String superToString = super.toString();
		if ( !superToString.isEmpty() )
			builder.append( ", " ).append( superToString );
		
		return builder.length() > 0 ? builder.substring( 2 ) : "";
	}
	
    public List< Integer > getLabels() {
    	return labels;
    }
	
    public void setLabels( List< Integer > labels ) {
    	this.labels = labels;
    }
	
    public String getMapName() {
    	return mapName;
    }
	
    public void setMapName( String mapName ) {
    	this.mapName = mapName;
    }
	
    public String getPlayers() {
    	return players;
    }
	
    public void setPlayers( String players ) {
    	this.players = players;
    }
	
    public String getGameType() {
    	return gameType;
    }
	
    public void setGameType( String gameType ) {
    	this.gameType = gameType;
    }
	
    public String getFormat() {
    	return format;
    }
	
    public void setFormat( String format ) {
    	this.format = format;
    }
	
	public void setLeagueMu( String leagueMu ) {
	    this.leagueMu = leagueMu;
    }

	public String getLeagueMu() {
	    return leagueMu;
    }

	public void setMatchup( String matchup ) {
	    this.matchup = matchup;
    }

	public String getMatchup() {
	    return matchup;
    }

	public void setHasComment( Boolean hasComment ) {
	    this.hasComment = hasComment;
    }

	public Boolean getHasComment() {
	    return hasComment;
    }

    public String getGateway() {
    	return gateway;
    }
	
    public void setGateway( String gateway ) {
    	this.gateway = gateway;
    }

}
