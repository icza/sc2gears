/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.apiuser.client.beans;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class ApiCallStatInfo implements IsSerializable {
	
	private String day;
	private long calls;
	private long usedOps;
	private long execTime;
	private long deniedCalls;
	private long errors;
	
	private long infoCalls;
	private long mapInfoCalls;
	private long parseRepCalls;
	private long profInfoCalls;
	private long infoExecTime;
	private long mapInfoExecTime;
	private long parseRepExecTime;
	private long profInfoExecTime;
	
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
	
	public void setDay( String day ) {
		this.day = day;
	}
	
	public String getDay() {
		return day;
	}
	
	public void setCalls( long calls ) {
		this.calls = calls;
	}
	
	public long getCalls() {
		return calls;
	}
	
	public void setUsedOps( long usedOps ) {
		this.usedOps = usedOps;
	}
	
	public long getUsedOps() {
		return usedOps;
	}
	
	public void setExecTime( long execTime ) {
	    this.execTime = execTime;
    }

	public long getExecTime() {
	    return execTime;
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
