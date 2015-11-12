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

import hu.belicza.andras.sc2gearsdb.admin.client.beans.AccountInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.DlStatInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.MiscFunctionInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.NewAccountSuggestion;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.ActivityInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.ApiCallStatInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.FileStatInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.PaymentInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.UserInfo;
import hu.belicza.andras.sc2gearsdb.admin.client.beans.VisitInfo;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 * 
 * @author Andras Belicza
 * 
 * @see AdminServiceAsync
 */
@RemoteServiceRelativePath("adminsrv")
public interface AdminService extends RemoteService {
	
	/**
	 * Returns the user info.
	 * @return the user info
	 */
	RpcResult< UserInfo > getUserInfo();
	
	/**
	 * Returns a list of new account suggestions.
	 * @return a list of new account suggestions
	 */
	RpcResult< List< NewAccountSuggestion > > getNewAccountSuggestionList();
	
	/**
	 * Creates an account.
	 * @param accountInfo info required to create the account
	 */
	RpcResult< Void > createAccount( AccountInfo accountInfo );

	/**
	 * Registers a payment
	 * @param paymentInfo info required to register a payment
	 */
	RpcResult< Void > registerPayment( PaymentInfo paymentInfo );
	
	/**
	 * Returns activity info for the last x minutes.
	 * @param minutes minutes to calculate and return activity info for
	 * @return activity info for the last x minutes
	 */
	RpcResult< ActivityInfo > getActivityInfo( int minutes );
	
	/**
	 * Visit type.
	 * @author Andras Belicza
	 */
	public enum VisitType {
		/** Visit.     */
		VISIT    ( "Visit"     ),
		/** API Visit. */
		API_VISIT( "API Visit" );
		
		/** String representation. */
		public final String stringValue;
		
        /**
         * Creates a new VisitType.
         */
        private VisitType( final String stringValue ) {
        	this.stringValue = stringValue;
        }
        
        @Override
        public String toString() {
            return stringValue;
        }
	}
	
	/**
	 * Returns a sorted list of the visits for the last x hours.
	 * @param type       visit type to return
	 * @param hours      hours to query and return the visits for
	 * @param hasAccount optional, if provided, visits will be filtered according to this
	 * @return a sorted list of the visits for the last x hours
	 */
	RpcResult< List< VisitInfo > > getVisitInfoList( VisitType type, int hours, Boolean hasAccount );
	
	/**
	 * Returns a sorted list of the file stat info.
	 * @param googleAccount  optional google account filter
	 * @param activeLastMins optional active in last minutes filter
	 * @param minStoredBytes optional min stored bytes filter
	 * @return a sorted list of the file stat info
	 */
	RpcResult< List< FileStatInfo > > getFileStatInfoList( String googleAccount, Integer activeLastMins, Long minStoredBytes );
	
	/**
	 * Recalculates the file info stats of the specified user.
	 * @param googleAccount google account whose stats to be recalculated
	 */
	RpcResult< Void > recalculateFileInfoStats( String googleAccount );
	
	/**
	 * Download stat type.
	 * @author Andras Belicza
	 */
	public enum DlStatType {
		/** General files.                */
		GENERAL         ( "General"          ),
		/** General files, extended list. */
		GENERAL_EXTENDED( "General Extended" ),
		/** Sc2gears starts.              */
		SC2GEARS_STARTS ( "Sc2gears starts"  ),
		/** Latest files.                 */
		LATESTS         ( "Latest"           ),
		/** Release files.                */
		RELEASES        ( "Releases"         ),
		/** Language files.               */
		LANG_FILES      ( "Lang files"       ),
		/** All files.                    */
		ALL             ( "All"              );
		
		/** String representation. */
		public final String stringValue;
		
        /**
         * Creates a new DlStatType.
         */
        private DlStatType( final String stringValue ) {
        	this.stringValue = stringValue;
        }
        
        @Override
        public String toString() {
            return stringValue;
        }
	}
	
	/**
	 * Returns a download stat info list specified by the type.
	 * @param type type specifying what dl stats to query and return
	 * @return a download stat info list specified by the type
	 */
	RpcResult< List< DlStatInfo > > getDlStatInfoList( DlStatType type );
	
	/**
	 * Returns the API activity for the specified date period.
	 * @param firstDay first day to return API call stats for
	 * @param lastDay  last day to return API call stats for
	 * @return a sorted list of the API call stat info
	 */
	RpcResult< List< ApiCallStatInfo > > getApiActivity( String firstDay, String lastDay );
	
	/**
	 * Returns a sorted list of the API call stat stat info.
	 * @param googleAccount optional google account filter
	 * @return a sorted list of the API call stat info
	 */
	RpcResult< List< ApiCallStatInfo > > getApiCallStatInfoList( String googleAccount );
	
	/**
	 * Returns the list of the available misc function info.
	 * @return the list of the available misc function info
	 */
	RpcResult< List< MiscFunctionInfo > > getMiscFunctionInfoList();
	
	/**
	 * Executes the specified misc function.
	 * @param autoTx       tells if a persistence manager with auto-create transaction should be used
	 * @param functionName name of the function to execute
	 * @param params       optional input parameters for the misc function 
	 * @return the result of the executed function
	 */
	RpcResult< String > executeMiscFunction( boolean autoTx, String functionName, String[] params );
	
}
