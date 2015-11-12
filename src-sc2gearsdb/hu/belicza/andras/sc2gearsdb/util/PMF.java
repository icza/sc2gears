/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.util;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * Persistence Manager Factory utility class.
 * 
 * @author Andras Belicza
 */
public class PMF {
	
	/** Single instance of the Persistence Manager Factory which automatically creates transactions.         */
	private static final PersistenceManagerFactory pmfAutoTxInstance   = JDOHelper.getPersistenceManagerFactory( "transactions-optional-auto"   );
	
	/** Single instance of the Persistence Manager Factory which does not automatically create transactions. */
	private static final PersistenceManagerFactory pmfNoAutoTxInstance = JDOHelper.getPersistenceManagerFactory( "transactions-optional-noauto" );
	
	/**
	 * No need to instantiate this class.
	 */
	private PMF() {
	}
	
	/**
	 * Returns the single Persistence Manager Factory instance which automatically creates transactions.
	 * @return the single Persistence Manager Factory instance
	 */
	public static PersistenceManagerFactory get() {
		return pmfAutoTxInstance;
	}
	
	/**
	 * Returns the single Persistence Manager Factory instance which automatically creates transactions.
	 * @return the single Persistence Manager Factory instance
	 */
	public static PersistenceManagerFactory getAutoTx() {
		return pmfAutoTxInstance;
	}
	
	/**
	 * Returns the single Persistence Manager Factory instance which does not automatically create transactions.
	 * @return the single Persistence Manager Factory instance
	 */
	public static PersistenceManagerFactory getNoAutoTx() {
		return pmfNoAutoTxInstance;
	}
	
}
