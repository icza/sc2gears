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

import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.EntityListResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PageInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.FileStatInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.FreeAccountInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.MousePrintFilters;
import hu.belicza.andras.sc2gearsdb.user.client.beans.MousePrintFullInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.MousePrintInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.OtherFileFilters;
import hu.belicza.andras.sc2gearsdb.user.client.beans.OtherFileInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.PaymentsInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.QuotaStatus;
import hu.belicza.andras.sc2gearsdb.user.client.beans.ReplayFilters;
import hu.belicza.andras.sc2gearsdb.user.client.beans.ReplayFullInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.ReplayInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.SettingsInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.UserInfo;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 * 
 * @author Andras Belicza
 * 
 * @see UserServiceAsync
 */
@RemoteServiceRelativePath("usersrv")
public interface UserService extends RemoteService {
	
	/**
	 * Returns the user info.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @return the user info
	 */
	RpcResult< UserInfo > getUserInfo( String sharedAccount );
	
	/**
	 * Registers a free account.
	 * @param freeAccountInfo info of the free account to be registered
	 */
	RpcResult< Void > register( FreeAccountInfo freeAccountInfo );
	
	/**
	 * Returns the storage quota status.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @return the storage quota status
	 */
	RpcResult< QuotaStatus > getStorageQuotaStatus( String sharedAccount );
	
	/**
	 * Returns the list of replay info specified by the parameters.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @param pageInfo      page info to return
	 * @param filters       replay filters to be applied
	 * @return the list result of replay info specified by the parameters
	 */
	RpcResult< EntityListResult< ReplayInfo > > getReplayInfoList( String sharedAccount, PageInfo pageInfo, ReplayFilters filters );
	
	/**
	 * Returns all information about a replay.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @param sha1 SHA-1 of the replay
	 * @return all information about a replay
	 */
	RpcResult< ReplayFullInfo > getReplayFullInfo( String sharedAccount, String sha1 );
	
	/**
	 * Saves the labels of a replay.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @param sha1   SHA-1 of the replay
	 * @param labels labels to save for the replay
	 */
	RpcResult< Void > saveReplayLabels( String sharedAccount, String sha1, List< Integer > labels );
	
	/**
	 * Saves the comment of a replay.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @param sha1   SHA-1 of the replay
	 * @param comment comment of the replay to save
	 */
	RpcResult< Void > saveReplayComment( String sharedAccount, String sha1, String comment );
	
	/**
	 * Saves the new names of the labels.<br>
	 * If a name in the list is the empty string (""), the default label name will be saved instead.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @param labelNames new names of labels to save
	 * @return the new label name list if save was successful or <code>null</code> if some errors occurred
	 */
	RpcResult< List< String > > saveLabelNames( String sharedAccount, List< String > labelNames );
	
	/**
	 * Returns the profile URLs of the players of the specified replay.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @param sha1          SHA-1 of the replay
	 * @return the list of profile URLs or <code>null</code> if some errors occurred
	 */
	RpcResult< List< String > > getProfileUrlList( String sharedAccount, String sha1 );
	
	/**
	 * Returns the list of mouse print info specified by the parameters.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @param pageInfo      page info to return
	 * @param filters       mouse print filters to be applied
	 * @return the list result of mouse print info specified by the parameters
	 */
	RpcResult< EntityListResult< MousePrintInfo > > getMousePrintInfoList( String sharedAccount, PageInfo pageInfo, MousePrintFilters filters );
	
	/**
	 * Returns all information about a mouse print.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @param sha1 SHA-1 of the mouse print
	 * @return all information about a mouse print
	 */
	RpcResult< MousePrintFullInfo > getMousePrintFullInfo( String sharedAccount, String sha1 );
	
	/**
	 * Returns the list of other file info specified by the parameters.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @param pageInfo      page info to return
	 * @param filters       other file filters to be applied
	 * @return the list result of other file info specified by the parameters
	 */
	RpcResult< EntityListResult< OtherFileInfo > > getOtherFileInfoList( String sharedAccount, PageInfo pageInfo, OtherFileFilters filters );
	
	/**
	 * Returns the File stat info of the user.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @return the File stat info of the user
	 */
	RpcResult< FileStatInfo > getFileStatInfo( String sharedAccount );
	
	/**
	 * Triggers a file stats recalculation.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 */
	RpcResult< Void > recalcFileStats( String sharedAccount );
	
	/**
	 * Returns the payments info of the user.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @return the payments info of the user
	 */
	RpcResult< PaymentsInfo > getPaymentsInfo( String sharedAccount );
	
	/**
	 * Returns the authorization key of the logged in user.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @return the authorization key of the logged in user or <code>null</code> if some errors occurred
	 */
	RpcResult< String > getAuthorizationKey( String sharedAccount );
	
	/**
	 * Generates a new authorization key.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @return the new authorization key or <code>null</code> if some error occurred
	 */
	RpcResult< String > generateNewAuthorizationKey( String sharedAccount );
	
	/**
	 * Deletes a list of files.
	 * @param fileTypeString type of the files to delete
	 * @param sha1List       SHA-1 list of files to delete
	 * @return the number of successfully deleted files or <code>null</code> if some errors occurred
	 */
	RpcResult< Integer > deleteFileList( String sharedAccount, String fileTypeString, List< String > sha1List );
	
	/**
	 * Returns the settings of the user.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @return the settings of the user or <code>null</code> if some errors occurred
	 */
	RpcResult< SettingsInfo > getSettings( String sharedAccount );
	
	/**
	 * Saves the settings.
	 * @param sharedAccount Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @param settingsInfo settings to save
	 */
	RpcResult< Void > saveSettings( String sharedAccount, SettingsInfo settingsInfo );
	
}
