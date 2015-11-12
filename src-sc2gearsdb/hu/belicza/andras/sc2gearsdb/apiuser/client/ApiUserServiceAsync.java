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

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of {@link ApiUserService}.
 * 
 * @author Andras Belicza
 * 
 * @see ApiUserService
 */
public interface ApiUserServiceAsync {
	
	void getApiUserInfo( String sharedApiAccount, AsyncCallback< RpcResult< ApiUserInfo > > callback );
	
	void getApiCallStatList( String sharedApiAccount, PageInfo pageInfo, ApiCallStatFilters filters, AsyncCallback< RpcResult< EntityListResult< ApiCallStatInfo > > > callback );
	
	void getQuotaAndPaymentsInfo( String sharedApiAccount, AsyncCallback< RpcResult< QuotaAndPaymentsInfo > > callback );
	
	void getApiKey( String sharedApiAccount, AsyncCallback< RpcResult< String > > callback );
	
	void generateNewApiKey( String sharedApiAccount, AsyncCallback< RpcResult< String > > callback );
	
	void getSettings( String sharedApiAccount, AsyncCallback< RpcResult< SettingsInfo > > callback );
	
	void saveSettings( String sharedApiAccount, SettingsInfo settings, AsyncCallback< RpcResult< Void > > callback );
	
}
