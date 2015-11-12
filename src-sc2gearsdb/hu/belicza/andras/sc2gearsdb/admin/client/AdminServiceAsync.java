/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.admin.client;

import hu.belicza.andras.sc2gearsdb.admin.client.AdminService.DlStatType;
import hu.belicza.andras.sc2gearsdb.admin.client.AdminService.VisitType;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.AccountInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.ActivityInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.ApiCallStatInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.DlStatInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.FileStatInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.MiscFunctionInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.NewAccountSuggestion;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.PaymentInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.UserInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.VisitInfo;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>AdminService</code>.
 * 
 * @author Andras Belicza
 * 
 * @see AdminService
 */
public interface AdminServiceAsync {
	
	void getUserInfo( AsyncCallback< RpcResult< UserInfo > > callback );
	
	void getNewAccountSuggestionList( AsyncCallback< RpcResult< List< NewAccountSuggestion > > > callback );
	
	void createAccount( AccountInfo accountInfo, AsyncCallback< RpcResult< Void > > callback );
	
	void registerPayment( PaymentInfo paymentInfo, AsyncCallback< RpcResult< Void > > callback );
	
	void getActivityInfo( int minutes, AsyncCallback< RpcResult< ActivityInfo > > callback );
	
	void getVisitInfoList( VisitType type, int hours, Boolean hasAccount, AsyncCallback< RpcResult< List< VisitInfo > > > callback );
	
	void getFileStatInfoList( String googleAccount, Integer activeLastMins, Long minStoredBytes, AsyncCallback< RpcResult< List< FileStatInfo > > > callback );
	
	void recalculateFileInfoStats( String googleAccount, AsyncCallback< RpcResult< Void > > callback );
	
	void getDlStatInfoList( DlStatType type, AsyncCallback< RpcResult< List< DlStatInfo > > > callback );
	
	void getApiActivity( String firstDay, String lastDay, AsyncCallback< RpcResult< List< ApiCallStatInfo > > > callback );
	
	void getApiCallStatInfoList( String googleAccount, AsyncCallback< RpcResult< List< ApiCallStatInfo > > > callback );
	
	void getMiscFunctionInfoList( AsyncCallback< RpcResult< List< MiscFunctionInfo > > > callback );
	
	void executeMiscFunction( boolean autoTx, String functionName, String[] params, AsyncCallback< RpcResult< String > > callback );
	
}
