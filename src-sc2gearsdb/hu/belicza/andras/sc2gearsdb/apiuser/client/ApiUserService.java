/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.apiuser.client;

import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.ApiCallStatFilters;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.ApiCallStatInfo;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.ApiUserInfo;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.QuotaAndPaymentsInfo;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.SettingsInfo;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.EntityListResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PageInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 * 
 * @author Andras Belicza
 * 
 * @see ApiUserServiceAsync
 */
@RemoteServiceRelativePath("apiusersrv")
public interface ApiUserService extends RemoteService {
	
	/**
	 * Returns the API user info.
	 * @param sharedApiAccount Google account to access in case we're viewing someone else's account; <code>null</code> otherwise
	 * @return the API user info
	 */
	RpcResult< ApiUserInfo > getApiUserInfo( String sharedApiAccount );
	
	/**
	 * Returns the list of API call stats.
	 * @param sharedApiAccount Google account to access in case we're viewing someone else's account; <code>null</code> otherwise
	 * @param pageInfo page info to return
	 * @param filters  API call stat filters to be applied
	 * @return the list result of API call stats
	 */
	RpcResult< EntityListResult< ApiCallStatInfo > > getApiCallStatList( String sharedApiAccount, PageInfo pageInfo, ApiCallStatFilters filters );
	
	/**
	 * Returns the API user quota info.
	 * @param sharedApiAccount Google account to access in case we're viewing someone else's account; <code>null</code> otherwise
	 * @return the API user quota info
	 */
	RpcResult< QuotaAndPaymentsInfo > getQuotaAndPaymentsInfo( String sharedApiAccount );
	
	/**
	 * Returns the API key of the logged in API user.
	 * @param sharedApiAccount Google account to access in case we're viewing someone else's account; <code>null</code> otherwise
	 * @return the API key of the logged in API user or <code>null</code> if some errors occurred
	 */
	RpcResult< String > getApiKey( String sharedApiAccount );
	
	/**
	 * Generates a new API key.
	 * @param sharedApiAccount Google account to access in case we're viewing someone else's account; <code>null</code> otherwise
	 * @return the new API key or <code>null</code> if some error occurred
	 */
	RpcResult< String > generateNewApiKey( String sharedApiAccount );
	
	/**
	 * Returns the settings of the API user.
	 * @param sharedApiAccount Google account to access in case we're viewing someone else's account; <code>null</code> otherwise
	 * @return the settings of the API user or <code>null</code> if some errors occurred
	 */
	RpcResult< SettingsInfo > getSettings( String sharedApiAccount );
	
	/**
	 * Saves the settings.
	 * @param sharedApiAccount Google account to access in case we're viewing someone else's account; <code>null</code> otherwise
	 * @param settingsInfo settings to save
	 */
	RpcResult< Void > saveSettings( String sharedApiAccount, SettingsInfo settingsInfo );
	
}
