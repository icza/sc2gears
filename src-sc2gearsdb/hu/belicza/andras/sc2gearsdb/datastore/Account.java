/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.datastore;

import hu.belicza.andras.sc2gearsdb.Consts;
import hu.belicza.andras.sc2gearsdb.user.client.Permission;
import hu.belicza.andras.sc2gearsdb.util.ServerUtils;

import java.util.List;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.users.User;

/**
 * Class representing an account.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>"v"</code> property.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class Account extends BaseAccount {
	
	/** Authorization key used to upload replays. */
	@Persistent
	private String authorizationKey;
	
	/** Storage the user paid for (bytes).*/
	@Persistent
	private long paidStorage;
	
	/** Custom label names of the account.<br>
	 * Where this list contains an empty string, the default label name is to be used (instead of the empty string).
	 * Default label names: {@link Consts#DEFAULT_REPLAY_LABEL_LIST}
	 */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private List< String > labelNames;
	
	/** Notification storage quota level in percent. */
	@Persistent
	private int notificationQuotaLevel;
	
	/** User preference whether to convert times to real-time. */
	@Persistent
	private boolean convertToRealTime;
	
	/** Displayable map size in the Replays list. */
	@Persistent
	private int mapImageSize;
	
	/** User preference how to indicate replay winners. */
	@Persistent
	private int displayWinners;
	
	/** List of favored players. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private List< String > favoredPlayers;
	
	/** List of Google account users who have been granted access to this account. */
	@Persistent
	private List< User > grantedUsers;
	
	/** List of granted permissions. See {@link Permission}. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private List< Long > grantedPermissions;
	
	/**
	 * Creates a new Account.
	 * @param user user associated with this account
	 */
	public Account( final User user ) {
		super( user );
		setV( 1 );
	}
	
	/**
	 * Returns the granted permissions for the specified user.
	 * Returns <code>0L</code> if this account does not share access or the specified user is not granted any permissions.
	 * @param user user whose granted permissions to return
	 * @return the granted permissions for the specified user
	 */
	public long getGrantedPermissionsForUser( final User user ) {
		if ( ServerUtils.ADMIN_EMAIL_STRING.equals( user.getEmail() ) )
			return Long.MAX_VALUE;
		
		if ( grantedUsers == null ) {
			return 0;
		}
		
		for ( int i = grantedUsers.size() - 1; i >= 0; i-- )
			if ( user.equals( grantedUsers.get( i ) ) )
				return grantedPermissions.get( i );
		
		return 0;
	}
	
	/**
	 * Tells if the specified permission is granted to the specified user.
	 * @param user       user whose granted permissions to check
	 * @param permission permission to check if it's granted
	 * @return true if the specified permission is granted to the specified user; false otherwise
	 */
	public boolean isPermissionGranted( final User user, final Permission permission ) {
		return permission.contained( getGrantedPermissionsForUser( user ) );
	}
	
	public String getAuthorizationKey() {
		return authorizationKey;
	}

	public void setAuthorizationKey(String authorizationKey) {
		this.authorizationKey = authorizationKey;
	}

	public long getPaidStorage() {
		return paidStorage;
	}

	public void setPaidStorage(long paidStorage) {
		this.paidStorage = paidStorage;
	}
	
	public void setLabelNames( List< String > labelNames ) {
	    this.labelNames = labelNames;
    }
	
	public List< String > getLabelNames() {
	    return labelNames;
    }

	public void setNotificationQuotaLevel( int notificationQuotaLevel ) {
	    this.notificationQuotaLevel = notificationQuotaLevel;
    }

	public int getNotificationQuotaLevel() {
	    return notificationQuotaLevel;
    }

	public void setConvertToRealTime( boolean convertToRealTime ) {
	    this.convertToRealTime = convertToRealTime;
    }

	public boolean isConvertToRealTime() {
	    return convertToRealTime;
    }

	public void setMapImageSize( int mapImageSize ) {
	    this.mapImageSize = mapImageSize;
    }

	public int getMapImageSize() {
	    return mapImageSize;
    }

	public void setFavoredPlayers( List< String > favoredPlayers ) {
	    this.favoredPlayers = favoredPlayers;
    }

	public List< String > getFavoredPlayers() {
	    return favoredPlayers;
    }

	public void setGrantedUsers( List< User > grantedUsers ) {
	    this.grantedUsers = grantedUsers;
    }

	public List< User > getGrantedUsers() {
	    return grantedUsers;
    }

	public void setGrantedPermissions( List< Long > grantedPermissions ) {
	    this.grantedPermissions = grantedPermissions;
    }

	public List< Long > getGrantedPermissions() {
	    return grantedPermissions;
    }

	public void setDisplayWinners( int displayWinners ) {
	    this.displayWinners = displayWinners;
    }

	public int getDisplayWinners() {
	    return displayWinners;
    }

}
