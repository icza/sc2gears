/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.apiuser.client.ui;

import hu.belicza.andras.sc2gearsdb.apiuser.client.ApiUser;
import hu.belicza.andras.sc2gearsdb.apiuser.client.ApiUserServiceAsync;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.ApiUserInfo;
import hu.belicza.andras.sc2gearsdb.common.client.Page;

/**
 * An API user page. Includes common things for API user pages.
 * 
 * @author Andras Belicza
 */
public abstract class ApiUserPage extends Page {
	
	/** Shortcut to the user service async. */
	protected static final ApiUserServiceAsync SERVICE_ASYNC = ApiUser.SERVICE_ASYNC;
	
	/** Shortcut to the API user info. */
	protected final ApiUserInfo apiUserInfo = ApiUser.apiUserInfo;
	
	/** Shortcut to the shared account user info. */
	protected final ApiUserInfo sharedApiAccountUserInfo = ApiUser.sharedAccountApiUserInfo;
	
    /**
     * Creates a new ApiUserPage.
     * @param title    displayable title of the page; can be <code>null</code>
     * @param pageName page name to be logged
     */
    public ApiUserPage( final String title, final String pageName ) {
		super( title, pageName );
    }
	
	/**
	 * Returns the shared account we're currently viewing.
	 * @return the shared account we're currently viewing or <code>null</code> if we're viewing our own account
	 */
	protected String getSharedApiAccount() {
		return sharedApiAccountUserInfo == null ? null : sharedApiAccountUserInfo.getSharedApiAccount();
	}
	
}
