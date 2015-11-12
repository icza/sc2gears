/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.common.client.pagingtable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info specifying a page of the paging table.
 * 
 * @author Andras Belicza
 */
public class PageInfo implements IsSerializable {
	
	/** Offset of the first row. */
	private int offset;
	
	/** Number of rows on the page. */
	private int limit;
	
	/** Cursor string for getting the entities of <b>this</b> page. */
	private String cursorString;
	
    /**
     * Creates a new PageInfo.
     * Default no-args constructor for GWT.
     */
    public PageInfo() {
    }
    
    /**
     * Creates a new PageInfo.
     * @param offset       offset of the first row
     * @param limit        number of rows on the page
     * @param cursorString cursor string for getting the entities of <b>this</b> page
     */
	public PageInfo( final int offset, final int limit, final String cursorString ) {
		this.offset       = offset;
		this.limit        = limit;
		this.cursorString = cursorString;
    }
	
	@Override
	public String toString() {
	    return "offset: "         + offset
	    	 + ", limit: "        + limit
	    	 + ", cursorString: " + cursorString;
	}
	
	public void setOffset( int offset ) {
	    this.offset = offset;
    }

	public int getOffset() {
	    return offset;
    }

	public void setLimit( int limit ) {
	    this.limit = limit;
    }

	public int getLimit() {
	    return limit;
    }

	public void setCursorString( String cursorString ) {
	    this.cursorString = cursorString;
    }

	public String getCursorString() {
	    return cursorString;
    }
	
}
