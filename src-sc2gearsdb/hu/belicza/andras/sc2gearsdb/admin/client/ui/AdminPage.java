/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.admin.client.ui;

import hu.belicza.andras.sc2gearsdb.admin.client.Admin;
import hu.belicza.andras.sc2gearsdb.admin.client.AdminServiceAsync;
import hu.belicza.andras.sc2gearsdb.common.client.Page;

/**
 * An admin page. Includes common things for admin pages.
 * 
 * @author Andras Belicza
 */
public abstract class AdminPage extends Page {
	
	/** Shortcut to the admin service async. */
	protected static final AdminServiceAsync SERVICE_ASYNC = Admin.SERVICE_ASYNC;
	
    /**
     * Creates a new AdminPage.
     * @param title    displayable title of the page; can be <code>null</code>
     * @param pageName page name to be logged
     */
    public AdminPage( final String title, final String pageName ) {
		super( title, pageName );
    }
	
}
