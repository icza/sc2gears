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

import hu.belicza.andras.sc2gearsdb.FileServlet;
import hu.belicza.andras.sc2gearsdb.util.ServerUtils;

import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Key;

/**
 * Class representing a replay.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>v</code> property.
 * 			<br>Made {@link #teams} property unindexed.
 * 		<li>Added <code>blobKey</code>, <code>content</code> properties (as part of {@link FileMetaData}) to dispose of the <code>File</code> type.
 * 		<li>Made <code>fname</code>, <code>lastmod</code>, <code>size</code> properties (as part of {@link FileMetaData}) unindexed.
 * 			<br>Made {@link #length}, {@link #winners}, {@link #comment} properties unindexed.
 * 			<br>Added {@link #normu}, {@link #hasCom} properties.
 * 		<li>File <code>content</code> is now stored locally (as part of {@link FileMetaData}) if size is less than a specified limit (details at {@link FileServlet}).
 * 			<br>Made <code>blobKey</code>, <code>content</code> properties (as part of {@link FileMetaData}) unindexed.
 * 			<br>Made {@link #matchup} property unindexed.
 * 		<li>Added unindexed property {@link #leagues}.
 * 			<br>Added indexed property {@link #norlm}.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class Rep extends FileMetaData {
	
	/** Version of the replay. */
	@Persistent
	private String ver;
	
	/** Date of the replay. */
	@Persistent
	private Date repd;
	
	/** Game length of the replay in seconds. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private Integer length;
	
	/** Gateway of the replay. */
	@Persistent
	private String gw;
	
	/** Game type of the replay. */
	@Persistent
	private String type;
	
	/** Map name of the replay. */
	@Persistent
	private String map;
	
	/** Map file name of the replay. */
	@Persistent
	private String mapf;
	
	/** Format of the replay. */
	@Persistent
	private String format;
	
	/** League match-up of the replay.
	 * Available only from replay version 2.0. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String leagues;
	
	/** Normalized league match-up form. For details see {@link ServerUtils#normalizeMatchupString(String)}.
	 * Available only from replay version 2.0. */
	@Persistent
	private String norlm;
	
	/** Race match-up of the replay. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String matchup;
	
	/** Normalized match-up form. For details see {@link ServerUtils#normalizeMatchupString(String)}. */
	@Persistent
	private String normu;
	
	/** Players of the replay. */
	@Persistent
	private List< String > players;
	
	/** Teams of players of the replay. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private List< Integer > teams;
	
	/** Winners of the replay. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private List< String > winners;
	
	/** Labels of the replay. */
	@Persistent
	private List< Integer > labels;
	
	/** Comment of the replay. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String comment;
	
	/** Tells if the replay has a private comment (in the {@link #comment} property).<br>
	 * While the {@link #comment} property is unindexed, this property is indexed.<br>
	 * The reason for this is so that users can filter replays where they have/don't have private comments. */
	@Persistent
	private boolean hasCom;
	
	/**
	 * Creates a new Rep.
	 * @param ownerk  key of the owner account
	 * @param fname   name of the replay
	 * @param lastmod last modified date of the file
	 * @param sha1    SHA-1 digest of the content of the replay
	 * @param size    size of the file
	 */
	public Rep( final Key ownerk, final String fname, final Date lastmod, final String sha1, final long size ) {
		super( ownerk, fname, lastmod, sha1, size );
		setV( 5 );
	}
	
    public String getVer() {
    	return ver;
    }
	
    public void setVer( String ver ) {
    	this.ver = ver;
    }
	
    public Date getRepd() {
    	return repd;
    }
	
    public void setRepd( Date repd ) {
    	this.repd = repd;
    }
	
    public Integer getLength() {
    	return length;
    }
	
    public void setLength( Integer length ) {
    	this.length = length;
    }
	
    public String getGw() {
    	return gw;
    }
	
    public void setGw( String gw ) {
    	this.gw = gw;
    }
	
    public String getType() {
    	return type;
    }
	
    public void setType( String type ) {
    	this.type = type;
    }
	
    public String getMap() {
    	return map;
    }
	
    public void setMap( String map ) {
    	this.map = map;
    }
	
    public String getMapf() {
    	return mapf;
    }
	
    public void setMapf( String mapf ) {
    	this.mapf = mapf;
    }
	
    public String getFormat() {
    	return format;
    }
	
    public void setFormat( String format ) {
    	this.format = format;
    }
	
    public String getLeagues() {
    	return leagues;
    }
	
    public void setLeagues( String leagues) {
    	this.leagues = leagues;
    	setNorlm( ServerUtils.normalizeMatchupString( leagues ) );
    }
	
	public void setNorlm( String norlm ) {
	    this.norlm = norlm;
    }

	public String getNorlm() {
	    return norlm;
    }

    public String getMatchup() {
    	return matchup;
    }
	
    public void setMatchup( String matchup ) {
    	this.matchup = matchup;
    	setNormu( ServerUtils.normalizeMatchupString( matchup ) );
    }
	
	public void setNormu( String normu ) {
	    this.normu = normu;
    }

	public String getNormu() {
	    return normu;
    }

    public List< String > getPlayers() {
    	return players;
    }
	
    public void setPlayers( List< String > players ) {
    	this.players = players;
    }
	
    public List< Integer > getTeams() {
    	return teams;
    }
	
    public void setTeams( List< Integer > teams ) {
    	this.teams = teams;
    }
	
    public List< String > getWinners() {
    	return winners;
    }
	
    public void setWinners( List< String > winners ) {
    	this.winners = winners;
    }
	
    public List< Integer > getLabels() {
    	return labels;
    }

    public void setLabels( List< Integer > labels ) {
    	this.labels = labels;
    }
	
    public String getComment() {
    	return comment;
    }
	
    public void setComment( String comment ) {
    	this.comment = comment;
    	setHasCom( comment != null );
    }

	public void setHasCom( Boolean hasCom ) {
	    this.hasCom = hasCom;
    }

	public Boolean getHasCom() {
	    return hasCom;
    }

}
