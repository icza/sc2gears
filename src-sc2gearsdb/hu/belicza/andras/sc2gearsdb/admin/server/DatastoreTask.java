/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.admin.server;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;

/**
 * Defines a datastore task which may operate using a {@link PersistenceManager}.
 * 
 * @author Andras Belicza
 */
interface DatastoreTask {
	
	/**
	 * Executes the datastore task
	 * @param request reference to the HTTP servlet request
	 * @param pm      reference to the persistence manager
	 * @param params  optional input parameters 
	 * @return the result of the execution
	 */
	String execute( HttpServletRequest request, PersistenceManager pm, String[] params );
	
	/**
	 * Returns the names of the parameters.
	 * @return the names of the parameters
	 */
	String[] getParamNames();
	
}
