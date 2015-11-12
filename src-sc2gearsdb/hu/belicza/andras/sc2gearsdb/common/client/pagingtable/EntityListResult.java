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

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Result of getting the entity list.
 * 
 * @param <T> type of the entity displayed in the table
 * 
 * @author Andras Belicza
 */
public class EntityListResult< T > implements IsSerializable {
	
	/** Entity list. */
	private List< T > entityList;
	
	/** Cursor string for getting the entities of the <b>next</b> page. */
	private String cursorString;
	
    /**
     * Creates a new EntityListResult.
     * Default no-args constructor for GWT.
     */
    public EntityListResult() {
    }
    
    /**
     * Creates a new EntityListResult.
     * @param entityList   entity list
     * @param cursorString cursor string for getting the entities of the <b>next</b> page
     */
    public EntityListResult( final List< T > entityList, final String cursorString ) {
    	this.entityList   = entityList;
    	this.cursorString = cursorString;
    }
	
	public void setEntityList( List< T > entityList ) {
	    this.entityList = entityList;
    }

	public List< T > getEntityList() {
	    return entityList;
    }

	public void setCursorString( String cursorString ) {
	    this.cursorString = cursorString;
    }

	public String getCursorString() {
	    return cursorString;
    }
	
}
