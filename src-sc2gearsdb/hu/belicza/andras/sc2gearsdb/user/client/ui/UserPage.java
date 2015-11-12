/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.user.client.ui;

import com.google.gwt.user.client.ui.Label;

import hu.belicza.andras.sc2gearsdb.common.client.ClientUtils;
import hu.belicza.andras.sc2gearsdb.common.client.Page;
import hu.belicza.andras.sc2gearsdb.user.client.Permission;
import hu.belicza.andras.sc2gearsdb.user.client.User;
import hu.belicza.andras.sc2gearsdb.user.client.UserServiceAsync;
import hu.belicza.andras.sc2gearsdb.user.client.beans.UserInfo;

/**
 * A user page. Includes common things for user pages.
 * 
 * @author Andras Belicza
 */
public abstract class UserPage extends Page {
	
	/** Shortcut to the user service async. */
	protected static final UserServiceAsync SERVICE_ASYNC = User.SERVICE_ASYNC;
	
	/** Shortcut to the user info. */
	protected final UserInfo userInfo = User.userInfo;
	
	/** Shortcut to the shared account user info. */
	protected final UserInfo sharedAccountUserInfo = User.sharedAccountUserInfo;
	
	/**
	 * Creates a new UserPage.
	 * @param title    displayable title of the page; can be <code>null</code>
	 * @param pageName page name to be logged
	 */
	public UserPage( final String title, final String pageName ) {
		super( title, pageName );
	}
	
	/**
	 * Returns the shared account we're currently viewing.
	 * @return the shared account we're currently viewing or <code>null</code> if we're viewing our own account
	 */
	protected String getSharedAccount() {
		return sharedAccountUserInfo == null ? null : sharedAccountUserInfo.getSharedAccount();
	}
	
	/**
	 * Checks if we have the specified permission to the database account we're currently viewing.
	 * @param permission permission to be checked
	 * @return true if we have the specified permission to the database account we're currently viewing; false otherwise
	 */
	protected boolean checkPermission( final Permission permission ) {
		return sharedAccountUserInfo == null || permission.contained( sharedAccountUserInfo.getGrantedPermissions() );
	}
	
	/**
	 * Checks if we have the specified permission to the database account we're currently viewing.
	 * If not, a message will be displayed stating that the user has no permission to view this page.
	 * @param permission permission to be checked
	 * @return true if we have the specified permission to the database account we're currently viewing; false otherwise
	 */
	protected boolean checkPagePermission( final Permission permission ) {
		if ( checkPermission( permission ) )
			return true;
		
		add( ClientUtils.createVerticalEmptyWidget( 30 ) );
		add( new Label( "You have no permission to view this page!" ) );
		
		return false;
	}
	
}
