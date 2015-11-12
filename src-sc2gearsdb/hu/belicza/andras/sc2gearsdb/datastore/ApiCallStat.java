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

import hu.belicza.andras.sc2gearsdb.ParsingServletApi;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Key;

/**
 * A class that stores statistics for API calls.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>"v"</code> property.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class ApiCallStat extends DataStoreObject {
	
	/** Value of the day property for the total stats. */
	public static final String DAY_TOTAL   = "TOTAL";
	/** Day format used in the {@link #day} property.  */
	public static final String DAY_PATTERN = "yyyy-MM-dd";
	
	/** Key of the owner API account. */
	@Persistent
	private Key ownerKey;
	
	/** Day to which the stats apply. Format: {@link #DAY_PATTERN}<br>
	 * There is a total stat per API account where the value of this property is {@link #DAY_TOTAL}. */
	@Persistent
	private String day;
	
	/** Number of API calls. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long calls;
	
	/** Number of used ops. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long usedOps;
	
	/** Execution time of API calls in milliseconds. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long execTime;
	
	/** Number of denied API calls (due to no available Ops). */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long deniedCalls;
	
	/** Number of API calls returning an error. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long errors;
	
	
	/** Number of Info op calls. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long infoCalls;
	
	/** Number of Map info op calls. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long mapInfoCalls;
	
	/** Number of Parse rep op calls. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long parseRepCalls;
	
	/** Profile info op calls. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long profInfoCalls;
	
	/** Execution time of Info op calls in milliseconds. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long infoExecTime;
	
	/** Execution time of Map info op calls in milliseconds. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long mapInfoExecTime;
	
	/** Execution time of Parse rep op calls in milliseconds. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long parseRepExecTime;
	
	/** Execution time of Profile info op calls in milliseconds. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long profInfoExecTime;
	
	/**
	 * Creates a new ApiAccount.
	 * @param ownerKey key of the API account
	 * @param day      day to which the stats apply
	 */
	public ApiCallStat( final Key ownerKey, final String day ) {
		this.ownerKey = ownerKey;
		this.day      = day;
		setV( 1 );
	}
	
	/**
	 * Registers an API call.
	 * @param opsCharged number of ops to charge
	 * @param execTime   execution time in milliseconds
	 * @param denied     tells if the API call was denied due to no available Ops
	 * @param error      tells if the API call returned an error
	 * @param operation  operation of the call
	 */
	public void registerApiCall( final long opsCharged, final long execTime, final boolean denied, final boolean error, final String operation ) {
		calls++;
		this.usedOps  += opsCharged;
		this.execTime += execTime;
		if ( denied )
			deniedCalls++;
		if ( error )
			errors++;
		
		if ( ParsingServletApi.OPERATION_INFO.equals( operation ) ) {
			infoCalls++;
			infoExecTime += execTime;
		} else if ( ParsingServletApi.OPERATION_MAP_INFO.equals( operation ) ) {
			mapInfoCalls++;
			mapInfoExecTime += execTime;
		} else if ( ParsingServletApi.OPERATION_PARSE_REPLAY.equals( operation ) ) {
			parseRepCalls++;
			parseRepExecTime += execTime;
		} else if ( ParsingServletApi.OPERATION_PROFILE_INFO.equals( operation ) ) {
			profInfoCalls++;
			profInfoExecTime += execTime;
		}
	}
	
	/**
	 * Returns the average API call execution time in milliseconds.
	 * @return the average API call execution time in milliseconds
	 */
	public long getAvgExecTime() {
		return calls == 0 ? 0 : execTime / calls;
	}
	
	/**
	 * Returns the average Info call execution time in milliseconds.
	 * @return the average Info call execution time in milliseconds
	 */
	public long getAvgInfoExecTime() {
		return infoCalls == 0 ? 0 : infoExecTime / infoCalls;
	}
	
	/**
	 * Returns the average Map info call execution time in milliseconds.
	 * @return the average Map info call execution time in milliseconds
	 */
	public long getAvgMapInfoExecTime() {
		return mapInfoCalls == 0 ? 0 : mapInfoExecTime / mapInfoCalls;
	}
	
	/**
	 * Returns the average Parse rep call execution time in milliseconds.
	 * @return the average Parse rep call execution time in milliseconds
	 */
	public long getAvgParseRepExecTime() {
		return parseRepCalls == 0 ? 0 : parseRepExecTime / parseRepCalls;
	}
	
	/**
	 * Returns the average Profile info call execution time in milliseconds.
	 * @return the average Profile info call execution time in milliseconds
	 */
	public long getAvgProfInfoExecTime() {
		return profInfoCalls == 0 ? 0 : profInfoExecTime / profInfoCalls;
	}
	
	public Key getOwnerKey() {
		return ownerKey;
	}
	
	public void setOwnerKey( Key ownerKey ) {
		this.ownerKey = ownerKey;
	}
	
	public String getDay() {
		return day;
	}
	
	public void setDay( String day ) {
		this.day = day;
	}
	
	public long getCalls() {
		return calls;
	}
	
	public void setCalls( long calls ) {
		this.calls = calls;
	}
	
	public long getUsedOps() {
		return usedOps;
	}
	
	public void setUsedOps( long usedOps ) {
		this.usedOps = usedOps;
	}
	
	public long getExecTime() {
		return execTime;
	}

	public void setExecTime( long execTime ) {
		this.execTime = execTime;
	}

	public void setDeniedCalls( long deniedCalls ) {
		this.deniedCalls = deniedCalls;
	}

	public long getDeniedCalls() {
		return deniedCalls;
	}

	public void setErrors( long errors ) {
		this.errors = errors;
	}

	public long getErrors() {
		return errors;
	}

	public void setInfoCalls( long infoCalls ) {
		this.infoCalls = infoCalls;
	}

	public long getInfoCalls() {
		return infoCalls;
	}

	public void setMapInfoCalls( long mapInfoCalls ) {
		this.mapInfoCalls = mapInfoCalls;
	}

	public long getMapInfoCalls() {
		return mapInfoCalls;
	}

	public void setParseRepCalls( long parseRepCalls ) {
		this.parseRepCalls = parseRepCalls;
	}

	public long getParseRepCalls() {
		return parseRepCalls;
	}

	public void setProfInfoCalls( long profInfoCalls ) {
	    this.profInfoCalls = profInfoCalls;
    }

	public long getProfInfoCalls() {
	    return profInfoCalls;
    }
	
	public void setInfoExecTime( long infoExecTime ) {
	    this.infoExecTime = infoExecTime;
    }

	public long getInfoExecTime() {
	    return infoExecTime;
    }

	public void setMapInfoExecTime( long mapInfoExecTime ) {
	    this.mapInfoExecTime = mapInfoExecTime;
    }

	public long getMapInfoExecTime() {
	    return mapInfoExecTime;
    }

	public void setParseRepExecTime( long parseRepExecTime ) {
	    this.parseRepExecTime = parseRepExecTime;
    }

	public long getParseRepExecTime() {
	    return parseRepExecTime;
    }

	public void setProfInfoExecTime( long profInfoExecTime ) {
	    this.profInfoExecTime = profInfoExecTime;
    }

	public long getProfInfoExecTime() {
	    return profInfoExecTime;
    }

}
