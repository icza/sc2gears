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

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of {@link UserService}.
 * 
 * @author Andras Belicza
 * 
 * @see UserService
 */
public interface UserServiceAsync {
	
	void getUserInfo( String sharedAccount, AsyncCallback< RpcResult< UserInfo > > callback );
	
	void register( FreeAccountInfo freeAccountInfo, AsyncCallback< RpcResult< Void > > callback );
	
	void getStorageQuotaStatus( String sharedAccount, AsyncCallback< RpcResult< QuotaStatus > > callback );
	
	void getReplayInfoList( String sharedAccount, PageInfo pageInfo, ReplayFilters filters, AsyncCallback< RpcResult< EntityListResult< ReplayInfo > > > callback );
	
	void getReplayFullInfo( String sharedAccount, String sha1, AsyncCallback< RpcResult< ReplayFullInfo > > callback );
	
	void saveReplayLabels( String sharedAccount, String sha1, List<Integer> labels, AsyncCallback< RpcResult< Void > > callback );
	
	void saveLabelNames( String sharedAccount, List< String > labelNames, AsyncCallback< RpcResult< List< String > > > callback );
	
	void saveReplayComment( String sharedAccount, String sha1, String comment, AsyncCallback< RpcResult< Void > > callback );
	
	void getProfileUrlList( String sharedAccount, String sha1, AsyncCallback< RpcResult< List< String > > > callback );
	
	void getMousePrintInfoList( String sharedAccount, PageInfo pageInfo, MousePrintFilters filters, AsyncCallback< RpcResult< EntityListResult< MousePrintInfo > > > callback );
	
	void getMousePrintFullInfo( String sharedAccount, String sha1, AsyncCallback< RpcResult< MousePrintFullInfo > > callback );
	
	void getOtherFileInfoList( String sharedAccount, PageInfo pageInfo, OtherFileFilters filters, AsyncCallback< RpcResult< EntityListResult< OtherFileInfo > > > callback );
	
	void getFileStatInfo( String sharedAccount, AsyncCallback< RpcResult< FileStatInfo > > callback );
	
	void recalcFileStats( String sharedAccount, AsyncCallback< RpcResult< Void > > callback );
	
	void getPaymentsInfo( String sharedAccount, AsyncCallback< RpcResult< PaymentsInfo > > callback );
	
	void getAuthorizationKey( String sharedAccount, AsyncCallback< RpcResult< String > > callback );
	
	void generateNewAuthorizationKey( String sharedAccount, AsyncCallback< RpcResult< String > > callback );
	
	void deleteFileList( String sharedAccount, String fileTypeString, List< String > sha1List, AsyncCallback< RpcResult< Integer > > callback );
	
	void getSettings( String sharedAccount, AsyncCallback< RpcResult< SettingsInfo > > callback );
	
	void saveSettings( String sharedAccount, SettingsInfo settings, AsyncCallback< RpcResult< Void > > callback );
	
}
