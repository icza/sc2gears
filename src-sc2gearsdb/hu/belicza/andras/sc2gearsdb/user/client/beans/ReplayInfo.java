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

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class ReplayInfo implements IsSerializable {
	
	private List< Integer > labels;
	private String version;
	private Date replayDate;
	private Integer gameLength;
	private String gateway;
	private String gameType;
	private String mapName;
	private String mapFileName;
	private String format;
	private String leagueMatchup;
	private String raceMatchup;
	private List< String > players;
	private List< Integer> playerTeams;
	private List< String > winners;
	private String sha1;
	private String fileName;
	private String comment;
	
	private List< String > profileUrlList;
	
    public List< Integer > getLabels() {
    	return labels;
    }
	
    public void setLabels( List< Integer > labels ) {
    	this.labels = labels;
    }
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Date getReplayDate() {
		return replayDate;
	}
	public void setReplayDate(Date replayDate) {
		this.replayDate = replayDate == null ? null : new Date( replayDate.getTime() );
	}
	public Integer getGameLength() {
		return gameLength;
	}
	public void setGameLength(Integer gameLength) {
		this.gameLength = gameLength;
	}
	public String getGateway() {
		return gateway;
	}
	public void setGateway(String gateway) {
		this.gateway = gateway;
	}
	public void setGameType(String gameType) {
		this.gameType = gameType;
	}
	public String getGameType() {
		return gameType;
	}
	public String getMapName() {
		return mapName;
	}
	public void setMapName(String mapName) {
		this.mapName = mapName;
	}
	public String getMapFileName() {
		return mapFileName;
	}
	public void setMapFileName(String mapFileName) {
		this.mapFileName = mapFileName;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getLeagueMatchup() {
		return leagueMatchup;
	}
	public void setLeagueMatchup(String leagueMatchup) {
		this.leagueMatchup = leagueMatchup;
	}
	public String getRaceMatchup() {
		return raceMatchup;
	}
	public void setRaceMatchup(String raceMatchup) {
		this.raceMatchup = raceMatchup;
	}
	public List<String> getPlayers() {
		return players;
	}
	public void setPlayers(List<String> players) {
		this.players = players;
	}
	public void setPlayerTeams(List< Integer> playerTeams) {
		this.playerTeams = playerTeams;
	}
	public List< Integer> getPlayerTeams() {
		return playerTeams;
	}
	public List<String> getWinners() {
		return winners;
	}
	public void setWinners(List<String> winners) {
		this.winners = winners;
	}
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}
	public String getSha1() {
		return sha1;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getComment() {
		return comment;
	}

	public void setProfileUrlList( List< String > profileUrlList ) {
	    this.profileUrlList = profileUrlList;
    }

	public List< String > getProfileUrlList() {
	    return profileUrlList;
    }
	
}
