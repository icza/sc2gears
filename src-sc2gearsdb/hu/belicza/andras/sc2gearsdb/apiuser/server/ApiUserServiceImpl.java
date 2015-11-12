/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.apiuser.server;

import static hu.belicza.andras.sc2gearsdb.common.client.CommonApi.MAX_PAGE_SIZE;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gearsdb.TaskServlet;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.ApiCallStatFilters;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.ApiCallStatInfo;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.ApiPaymentInfo;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.ApiUserInfo;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.QuotaAndPaymentsInfo;
import hu.belicza.andras.sc2gearsdb.apiuser.client.beans.SettingsInfo;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.EntityListResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PageInfo;
import hu.belicza.andras.sc2gearsdb.datastore.ApiAccount;
import hu.belicza.andras.sc2gearsdb.datastore.ApiCallStat;
import hu.belicza.andras.sc2gearsdb.datastore.ApiPayment;
import hu.belicza.andras.sc2gearsdb.datastore.ApiVisit;
import hu.belicza.andras.sc2gearsdb.datastore.Event;
import hu.belicza.andras.sc2gearsdb.datastore.Event.Type;
import hu.belicza.andras.sc2gearsdb.util.JQBuilder;
import hu.belicza.andras.sc2gearsdb.util.PMF;
import hu.belicza.andras.sc2gearsdb.util.ServerUtils;
import hu.belicza.andras.sc2gearsdbapi.ServletApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Server side of the API user module.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class ApiUserServiceImpl extends RemoteServiceServlet implements hu.belicza.andras.sc2gearsdb.apiuser.client.ApiUserService {
	
	/** File name to appear for the API visit stats in the download stats. */
	public static final String API_VISIT_STATS_FILE_NAME = "%API_VISITS%";
	
	private static final Logger LOGGER = Logger.getLogger( ApiUserServiceImpl.class.getName() );
	
	/**
	 * Returns the accessed API account.
	 * @param pm               reference to the persistence manager
	 * @param sharedApiAccount Google account to access in case we're viewing someone else's account; <code>null</code> otherwise
	 * @param user             the logged in user
	 * @return the list of the accessed API account
	 */
	private final ApiAccount getApiAccount( final PersistenceManager pm, final String sharedApiAccount, final User user ) {
		final List< ApiAccount > apiAccountList = new JQBuilder<>( pm, ApiAccount.class ).filter( "user==p1", "USER p1" )
				.get( sharedApiAccount == null ? user : new User( sharedApiAccount, "gmail.com" ) );
		
		return apiAccountList.isEmpty() ? null : apiAccountList.get( 0 );
	}
	
	@Override
	public RpcResult< ApiUserInfo > getApiUserInfo( final String sharedApiAccount ) {
		LOGGER.fine( "sharedApiAccount: " + sharedApiAccount );
		
		final ApiUserInfo apiUserInfo = new ApiUserInfo();
		
		final UserService userService = UserServiceFactory.getUserService();
		final User        user        = userService.getCurrentUser();
		
		if ( user == null )
			apiUserInfo.setLoginUrl( userService.createLoginURL( "/ApiUser.html" ) );
		else {
			
			apiUserInfo.setUserNickname( user.getNickname() );
			apiUserInfo.setUserName    ( user.getNickname() );
			apiUserInfo.setLogoutUrl   ( userService.createLogoutURL( "/ApiUser.html" ) );
			
			PersistenceManager pm = null;
			try {
				pm = PMF.get().getPersistenceManager();
				
				final ApiAccount apiAccount = getApiAccount( pm, sharedApiAccount, user );
				
				if ( sharedApiAccount == null ) {
					if ( apiAccount != null ) {
						final List< ApiVisit > apiVisitList = new JQBuilder<>( pm, ApiVisit.class ).filter( "visitorKey==p1", "KEY p1" ).desc( "date" ).range( 0, 1 ).get( apiAccount.getKey() );
						if ( !apiVisitList.isEmpty() )
							apiUserInfo.setLastVisit( apiVisitList.get( 0 ).getDate() );
					}
					
					// Check if visiting user is new (unique)
					final com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query( ApiVisit.class.getSimpleName() );
					q.setFilter( new FilterPredicate( "user", FilterOperator.EQUAL, user ) );
					final boolean isNew = ServerUtils.isQueryResultEmpty( q );
					
					// Log API visit
					final ApiVisit apiVisit = new ApiVisit( user );
					apiVisit.fillTracking( getThreadLocalRequest() );
					if ( apiAccount != null )
						apiVisit.setVisitorKey( apiAccount.getKey() );
					pm.makePersistent( apiVisit );
					
					// Update API visit stats
					TaskServlet.register_updateDownloadStat( API_VISIT_STATS_FILE_NAME, getThreadLocalRequest().getHeader( "User-Agent" ), isNew, 1 );
					
					if ( apiAccount != null ) {
						if ( apiAccount.getName() != null && !apiAccount.getName().isEmpty() )
							apiUserInfo.setUserName( apiAccount.getName() );
						apiUserInfo.setAdmin( userService.isUserAdmin() );
						
						final List< String >  sharedAccounts;
						if ( apiUserInfo.isAdmin() ) {
							// Get list of API accounts we have access to
							final List< ApiAccount > sharedAccountList = new JQBuilder<>( pm, ApiAccount.class ).get(); // All (full list)
							sharedAccounts = new ArrayList< String >( sharedAccountList.size() );
							for ( final ApiAccount sharedAccount_ : sharedAccountList ) {
								if ( !user.equals( sharedAccount_.getUser() ) ) // Do not add ourselves, we insert that to the first later
									sharedAccounts.add( sharedAccount_.getUser().getEmail() );
							}
							// Sort by Google account
							Collections.sort( sharedAccounts, String.CASE_INSENSITIVE_ORDER );
						}
						else {
							sharedAccounts = new ArrayList< String >( 1 );
						}
						sharedAccounts.add( 0, apiAccount.getUser().getEmail() ); // Insert owner account to the first of the list
						apiUserInfo.setSharedAccounts( sharedAccounts );
					}
					else {
						// Send and display the Google Account email address:
						apiUserInfo.setUserName( user.getEmail() );
					}
				}
				else {
					// Accessing shared API account...
					apiUserInfo.setSharedApiAccount( sharedApiAccount );
				}
				
				if ( apiAccount != null ) {
					apiUserInfo.setHasApiAccount( true );
					apiUserInfo.setRepParserEngineVer( ReplayFactory.getVersion() );
				}
				
			} finally {
				if ( pm != null )
					pm.close();
			}
		}
		
		return new RpcResult< ApiUserInfo >( apiUserInfo );
	}
	
	@Override
	public RpcResult< EntityListResult< ApiCallStatInfo > > getApiCallStatList( final String sharedApiAccount, final PageInfo pageInfo, final ApiCallStatFilters filters ) {
		final String filtersString = filters.toString();
		LOGGER.fine( "sharedApiAccount: " + sharedApiAccount + ", pageInfo: {" + pageInfo.toString() + "}"
			+ ( filtersString.isEmpty() ? "" :  ", filters: {" + filtersString + "}" ) );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		if ( pageInfo.getLimit() > MAX_PAGE_SIZE ) {
			LOGGER.warning( "Too big limit supplied: " + pageInfo.getLimit() );
			return RpcResult.createErrorResult( "Too big limit supplied: " + pageInfo.getLimit() );
		}
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final ApiAccount apiAccount = getApiAccount( pm, sharedApiAccount, user );
			if ( apiAccount == null )
				return RpcResult.createNoPermissionErrorResult();
			
			final List< Object > paramsList     = new ArrayList< Object >();
			final StringBuilder  filtersBuilder = new StringBuilder( "ownerKey==p1" );
			final StringBuilder  paramsBuilder  = new StringBuilder( "KEY p1" );
			
			paramsList.add( apiAccount.getKey() );
			if ( filters.getFromDay() != null ) {
				paramsList.add( filters.getFromDay() );
				filtersBuilder.append( " && day>=p" ).append( paramsList.size() );
				paramsBuilder.append( ", String p" ).append( paramsList.size() );
			}
			if ( filters.getToDay() != null ) {
				paramsList.add( filters.getToDay() );
				filtersBuilder.append( " && day<=p" ).append( paramsList.size() );
				paramsBuilder.append( ", String p" ).append( paramsList.size() );
			}
			final List< ApiCallStat > apiCallStatList = new JQBuilder<>( pm, ApiCallStat.class ).filter( filtersBuilder.toString(), paramsBuilder.toString() ).desc( "day" ).pageInfo( pageInfo ).get( paramsList.toArray() );
			
			final List< ApiCallStatInfo > apiCallStatInfoList = new ArrayList< ApiCallStatInfo >( apiCallStatList.size() );
			for ( final ApiCallStat apiCallStat : apiCallStatList ) {
				final ApiCallStatInfo apiCallStatInfo = new ApiCallStatInfo();
				
				apiCallStatInfo.setDay             ( apiCallStat.getDay             () );
				apiCallStatInfo.setCalls           ( apiCallStat.getCalls           () );
				apiCallStatInfo.setUsedOps         ( apiCallStat.getUsedOps         () );
				apiCallStatInfo.setExecTime        ( apiCallStat.getExecTime        () );
				apiCallStatInfo.setDeniedCalls     ( apiCallStat.getDeniedCalls     () );
				apiCallStatInfo.setErrors          ( apiCallStat.getErrors          () );
				
				apiCallStatInfo.setInfoCalls       ( apiCallStat.getInfoCalls       () );
				apiCallStatInfo.setInfoExecTime    ( apiCallStat.getInfoExecTime    () );
				apiCallStatInfo.setMapInfoCalls    ( apiCallStat.getMapInfoCalls    () );
				apiCallStatInfo.setMapInfoExecTime ( apiCallStat.getMapInfoExecTime () );
				apiCallStatInfo.setParseRepCalls   ( apiCallStat.getParseRepCalls   () );
				apiCallStatInfo.setParseRepExecTime( apiCallStat.getParseRepExecTime() );
				apiCallStatInfo.setProfInfoCalls   ( apiCallStat.getProfInfoCalls   () );
				apiCallStatInfo.setProfInfoExecTime( apiCallStat.getProfInfoExecTime() );
				
				apiCallStatInfoList.add( apiCallStatInfo );
			}
			
			return new RpcResult< EntityListResult< ApiCallStatInfo > >( new EntityListResult< ApiCallStatInfo >( apiCallStatInfoList, JDOCursorHelper.getCursor( apiCallStatList ).toWebSafeString() ) );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< QuotaAndPaymentsInfo > getQuotaAndPaymentsInfo( final String sharedApiAccount ) {
		LOGGER.fine( "sharedApiAccount: " + sharedApiAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final ApiAccount apiAccount = getApiAccount( pm, sharedApiAccount, user );
			if ( apiAccount == null )
				return RpcResult.createNoPermissionErrorResult();
			
			final List< ApiCallStat > apiCallStatList = new JQBuilder<>( pm, ApiCallStat.class ).filter( "ownerKey==p1 && day==p2", "KEY p1, String p2" ).get( apiAccount.getKey(), ApiCallStat.DAY_TOTAL );
			
			final List< ApiPayment > apiPaymentList = new JQBuilder<>( pm, ApiPayment.class ).filter( "apiAccountKey==p1", "KEY p1" ).desc( "date" ).get( apiAccount.getKey() );
			
			final QuotaAndPaymentsInfo quotaAndPaymentsInfo = new QuotaAndPaymentsInfo();
			quotaAndPaymentsInfo.setPaidOps( apiAccount.getPaidOps() );
			quotaAndPaymentsInfo.setUsedOps( apiCallStatList.isEmpty() ? 0 : apiCallStatList.get( 0 ).getUsedOps() );
			final List< ApiPaymentInfo > apiPaymentInfoList = new ArrayList< ApiPaymentInfo >( apiPaymentList.size() );
			for ( final ApiPayment apiPayment : apiPaymentList ) {
				final ApiPaymentInfo apiPaymentInfo = new ApiPaymentInfo();
				apiPaymentInfo.setDate         ( apiPayment.getDate         () );
				apiPaymentInfo.setPaymentSender( apiPayment.getPaypalAccount() == null || apiPayment.getPaypalAccount().isEmpty() ? apiPayment.getBankAccount() : apiPayment.getPaypalAccount() );
				apiPaymentInfo.setGrossPayment ( apiPayment.getGrossPayment () );
				apiPaymentInfo.setNetPayment   ( apiPayment.getNetPayment   () );
				apiPaymentInfo.setPaidOps      ( apiPayment.getPaidOps      () );
				apiPaymentInfoList.add( apiPaymentInfo );
			}
			quotaAndPaymentsInfo.setApiPaymentInfoList( apiPaymentInfoList );
			
			return new RpcResult< QuotaAndPaymentsInfo >( quotaAndPaymentsInfo );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< String > getApiKey( final String sharedApiAccount ) {
		LOGGER.fine( "sharedApiAccount: " + sharedApiAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final ApiAccount apiAccount = getApiAccount( pm, sharedApiAccount, user );
			if ( apiAccount == null )
				return RpcResult.createNoPermissionErrorResult();
			else
				return new RpcResult< String >( apiAccount.getApiKey() );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< String > generateNewApiKey( final String sharedApiAccount ) {
		LOGGER.fine( "sharedApiAccount: " + sharedApiAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final ApiAccount apiAccount = getApiAccount( pm, sharedApiAccount, user );
			if ( apiAccount == null )
				return RpcResult.createNoPermissionErrorResult();
			
			final String newApiKey = ServerUtils.generateRandomStringKey();
			
			// Check if new API key is unique:
			final List< ApiAccount > apiAccountList2 = new JQBuilder<>( pm, ApiAccount.class ).filter( "apiKey==p1", "String p1" ).get( newApiKey );
			if ( !apiAccountList2.isEmpty() )
				return RpcResult.createErrorResult( "Error generating new API key!" ); // This API key is in use!!!
			
			pm.makePersistent( new Event( apiAccount.getKey(), Type.CHANGE_API_KEY, "Old key: " + apiAccount.getApiKey() ) );
			
			apiAccount.setApiKey( newApiKey );
			
			return new RpcResult< String >( newApiKey );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< SettingsInfo > getSettings( final String sharedApiAccount ) {
		LOGGER.fine( "sharedApiAccount: " + sharedApiAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final ApiAccount apiAccount = getApiAccount( pm, sharedApiAccount, user );
			if ( apiAccount == null )
				return RpcResult.createNoPermissionErrorResult();
			
			final SettingsInfo settingsInfo = new SettingsInfo();
			settingsInfo.setGoogleAccount       ( apiAccount.getUser                ().getEmail() );
			settingsInfo.setContactEmail        ( apiAccount.getContactEmail        ()            );
			settingsInfo.setUserName            ( apiAccount.getName                ()            );
			settingsInfo.setNotificationAvailOps( apiAccount.getNotificationAvailOps()            );
			return new RpcResult< SettingsInfo >( settingsInfo );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< Void > saveSettings( final String sharedApiAccount, final SettingsInfo settingsInfo ) {
		LOGGER.fine( "sharedApiAccount: " + sharedApiAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final ApiAccount apiAccount = getApiAccount( pm, sharedApiAccount, user );
			if ( apiAccount == null )
				return RpcResult.createNoPermissionErrorResult();
			
			if ( settingsInfo.getContactEmail() != null && !settingsInfo.getContactEmail().isEmpty() )
				if ( !ServerUtils.isEmailValid( settingsInfo.getContactEmail() ) )
					return RpcResult.createErrorResult( "Invalid contact email!" );
			
			if ( settingsInfo.getNotificationAvailOps() < 0 )
				return RpcResult.createErrorResult( "Invalid notification available ops! (Must be equal to or greater than 0!)" );
			
			apiAccount.setContactEmail        ( settingsInfo.getContactEmail() == null || settingsInfo.getContactEmail().isEmpty() ? null : ServletApi.trimStringLength( settingsInfo.getContactEmail(), 500 ) );
			apiAccount.setName                ( settingsInfo.getUserName    () == null || settingsInfo.getUserName    ().isEmpty() ? null : ServletApi.trimStringLength( settingsInfo.getUserName    (), 500 ) );
			apiAccount.setNotificationAvailOps( settingsInfo.getNotificationAvailOps() );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
		
		return RpcResult.createInfoResult( "Settings saved successfully." );
	}
	
}
