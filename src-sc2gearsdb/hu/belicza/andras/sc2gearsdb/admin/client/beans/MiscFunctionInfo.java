/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.admin.client.beans;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class MiscFunctionInfo implements IsSerializable {
	
	private String name;
	
	private String[] paramNames;
	
    /**
     * Creates a new MiscFunctionInfo.
     * Default no-args constructor for GWT.
     */
    public MiscFunctionInfo() {
    }
	
    /**
     * Creates a new MiscFunctionInfo.
     */
    public MiscFunctionInfo( final String name, final String[] paramNames ) {
    	this.name       = name;
    	this.paramNames = paramNames;
    }
	
	public void setName( String name ) {
	    this.name = name;
    }

	public String getName() {
	    return name;
    }

	public void setParamNames( String[] paramNames ) {
	    this.paramNames = paramNames;
    }

	public String[] getParamNames() {
	    return paramNames;
    }
	
}
