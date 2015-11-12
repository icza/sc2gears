/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.user.client;

import java.util.Set;

/**
 * Controllable permissions in the Sc2gears Database User module.
 * 
 * @author Andras Belicza
 */
public enum Permission {
	
	VIEW_REPLAYS            ( 0x0001L, "Replays"          , "View Replays"                                                         ),
	CHANGE_REP_LABELS       ( 0x0002L, "Replays"          , "Change Replay Labels"                                                 ),
	VIEW_UPDATE_REP_COMMENTS( 0x0004L, "Replays"          , "View/Update Private Replay Comments"                                  ),
	RENAME_LABELS           ( 0x0008L, "Replays"          , "Rename Labels"                                                        ),
	DELETE_REPLAYS          ( 0x0010L, "Replays"          , "Delete Replays"                                                       ),
	
	VIEW_MOUSE_PRINTS       ( 0x0020L, "Mouse Prints"     , "View Mouse Prints"                                                    ),
	DELETE_MOUSE_PRINTS     ( 0x0040L, "Mouse Prints"     , "Delete Mouse Prints"                                                  ),
	
	VIEW_OTHER_FILES        ( 0x0080L, "Other Files"      , "View Other Files (stored settings include your Authorization key!)"   ),
	DELETE_OTHER_FILES      ( 0x0100L, "Other Files"      , "Delete Other Files"                                                   ),
	
	VIEW_QUOTA              ( 0x0200L, "Quota"            , "View Quota"                                                           ),
	
	VIEW_PAYMENTS           ( 0x4000L, "Payments"         , "View Payments"                                                        ),
	
	VIEW_AUTHORIZATION_KEY  ( 0x0400L, "Authorization Key", "View Authorization Key (implicit FULL access from Sc2gears clients!)" ),
	CHANGE_AUTHORIZATION_KEY( 0x0800L, "Authorization Key", "Change Authorization Key"                                             ),
	
	VIEW_SETTINGS           ( 0x1000L, "Settings"         , "View Settings"                                                        ),
	CHANGE_SETTINGS         ( 0x2000L, "Settings"         , "Change Settings (includes changing account sharing and permissions!)" );
	
	/** Visual group when displaying the permissions. */
	public final String group;
	/** Bit mask of this permission.                  */
	public final long   mask;
	/** Display name of the permission.               */
	public final String displayName;
	
	/**
	 * Creates a new Permission.
	 * @param group       visual group when displaying the permissions
	 * @param displayName display name of the permission
	 */
	private Permission( final long mask, final String group, final String displayName ) {
		this.group       = group;
		this.mask        = mask;
		this.displayName = displayName;
	}
	
	/**
	 * Tests if the specified <code>permissions</code> mask contains this permission.
	 * @param permissions permissions mask to test if we're included
	 * @return true if this permission is included in the specified <code>permissions</code> mask
	 */
	public boolean contained( final long permissions ) {
		return ( permissions & mask ) != 0;
	}
	
	/**
	 * Adds this permission to the the specified <code>permissions</code> mask.
	 * @param permissions permissions mask to add this permission to
	 * @return a modified <code>permissions</code> mask which includes this permission
	 */
	public long addTo( final long permissions ) {
		return permissions | mask;
	}
	
	/**
	 * Returns the permissions mask containing all permissions of <code>permissionSet</code>. 
	 * @param permissionSet set of permissions to be contained in the returned mask
	 * @return a permissions mask containing all of <code>permissionSet</code>
	 */
	public static long permissionsOf( final Set< Permission > permissionSet ) {
		long permissions = 0;
		
		for ( Permission permission : permissionSet )
			permissions = permission.addTo( permissions );
		
		return permissions;
	}
	
}
