/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.admin.server;

import static hu.belicza.andras.sc2gearsdb.FileServlet.FILE_TYPE_CLASS_MAP;
import hu.belicza.andras.sc2gearsdb.DownloadServlet;
import hu.belicza.andras.sc2gearsdb.TaskServlet;
import hu.belicza.andras.sc2gearsdb.admin.client.AdminService;
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
import hu.belicza.andras.sc2gearsdb.admin.server.EntityBatchUpdater.EntityProcessor;
import hu.belicza.andras.sc2gearsdb.apiuser.server.ApiUserServiceImpl;
import hu.belicza.andras.sc2gearsdb.beans.DownloadStatInfo;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.common.server.CommonUtils.DbPackage;
import hu.belicza.andras.sc2gearsdb.datastore.Account;
import hu.belicza.andras.sc2gearsdb.datastore.ApiAccount;
import hu.belicza.andras.sc2gearsdb.datastore.ApiCallStat;
import hu.belicza.andras.sc2gearsdb.datastore.ApiPayment;
import hu.belicza.andras.sc2gearsdb.datastore.ApiVisit;
import hu.belicza.andras.sc2gearsdb.datastore.BaseVisit;
import hu.belicza.andras.sc2gearsdb.datastore.D;
import hu.belicza.andras.sc2gearsdb.datastore.DownloadStat;
import hu.belicza.andras.sc2gearsdb.datastore.Event;
import hu.belicza.andras.sc2gearsdb.datastore.Event.Type;
import hu.belicza.andras.sc2gearsdb.datastore.FileMetaData;
import hu.belicza.andras.sc2gearsdb.datastore.FileStat;
import hu.belicza.andras.sc2gearsdb.datastore.MousePracticeGameScore;
import hu.belicza.andras.sc2gearsdb.datastore.OtherFile;
import hu.belicza.andras.sc2gearsdb.datastore.Payment;
import hu.belicza.andras.sc2gearsdb.datastore.Rep;
import hu.belicza.andras.sc2gearsdb.datastore.RepComment;
import hu.belicza.andras.sc2gearsdb.datastore.RepProfile;
import hu.belicza.andras.sc2gearsdb.datastore.RepRate;
import hu.belicza.andras.sc2gearsdb.datastore.Smpd;
import hu.belicza.andras.sc2gearsdb.datastore.Visit;
import hu.belicza.andras.sc2gearsdb.user.server.UserServiceImpl;
import hu.belicza.andras.sc2gearsdb.util.CachingService;
import hu.belicza.andras.sc2gearsdb.util.JQBuilder;
import hu.belicza.andras.sc2gearsdb.util.PMF;
import hu.belicza.andras.sc2gearsdb.util.ServerUtils;
import hu.belicza.andras.sc2gearsdbapi.FileServletApi.FileType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Server side of the admin module.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class AdminServiceImpl extends RemoteServiceServlet implements AdminService {
	
	private static final Logger LOGGER = Logger.getLogger( AdminServiceImpl.class.getName() );
	
	@Override
	public RpcResult< UserInfo > getUserInfo() {
		LOGGER.fine( "" );
		
		final UserInfo userInfo = new UserInfo();
		
		final UserService userService = UserServiceFactory.getUserService();
		final User        user        = userService.getCurrentUser();
		
		if ( user == null )
			userInfo.setLoginUrl( userService.createLoginURL( "/Admin.html" ) );
		else {
			userInfo.setAdmin( userService.isUserAdmin() );
			userInfo.setUserName( user.getNickname() );
			userInfo.setLogoutUrl( userService.createLogoutURL( "/Admin.html" ) );
		}
		
		return new RpcResult< UserInfo >( userInfo );
	}
	
	@Override
	public RpcResult< List< NewAccountSuggestion > > getNewAccountSuggestionList() {
		LOGGER.fine( "" );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		if ( !userService.isUserAdmin() )
			return RpcResult.createNoPermissionErrorResult();
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			final List< Visit > visitList = new JQBuilder<>( pm, Visit.class ).filter( "visitorKey==null", null ).desc( "date" ).range( 0, 20 ).get();
			
			final List< NewAccountSuggestion > newAccSuggestionList = new ArrayList< NewAccountSuggestion >( visitList.size() + 1 );
			if ( !visitList.isEmpty() )
				newAccSuggestionList.add( new NewAccountSuggestion() );
			
			for ( final Visit visit : visitList ) {
				final NewAccountSuggestion newAccSuggestion = new NewAccountSuggestion();
				
				newAccSuggestion.setGoogleAccount( visit.getUser().getEmail()                              );
				newAccSuggestion.setCountryCode  ( visit.getCountryCode()                                  );
				newAccSuggestion.setCountryName  ( ServerUtils.countryCodeToName( visit.getCountryCode() ) );
				
				newAccSuggestionList.add( newAccSuggestion );
			}
			
			return new RpcResult< List<NewAccountSuggestion> >( newAccSuggestionList );
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public RpcResult< Void > createAccount( final AccountInfo accountInfo ) {
		LOGGER.fine( "For Google account: " + accountInfo.getGoogleAccount() );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		if ( !userService.isUserAdmin() )
			return RpcResult.createNoPermissionErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			// Perform checks
			if ( accountInfo.getGoogleAccount() == null || accountInfo.getGoogleAccount().isEmpty() )
				return RpcResult.createErrorResult( "Google account is required!" );
			
			if ( !ServerUtils.isEmailValid( accountInfo.getGoogleAccount() ) )
				return RpcResult.createErrorResult( "Invalid Google account!" );
			if ( accountInfo.getContactEmail() != null && !accountInfo.getContactEmail().isEmpty() )
				if ( !ServerUtils.isEmailValid( accountInfo.getContactEmail() ) )
					return RpcResult.createErrorResult( "Invalid contact email!" );
			
			final User newUser = new User( accountInfo.getGoogleAccount(), "gmail.com" );
			if ( accountInfo.isApiAccount() ) {
				if ( !( (List< ? >) pm.newQuery( "select key from " + ApiAccount.class.getName() + " where user==:1" ).execute( newUser ) ).isEmpty() )
					return RpcResult.createErrorResult( "There is already an API account with this Google account!" );
				
				// Create and save API account
				final ApiAccount apiAccount = new ApiAccount( newUser );
				apiAccount.setApiKey( ServerUtils.generateRandomStringKey() );
				// We have to make sure the API key is unique:
				if ( !( (List< Key >) pm.newQuery( "select key from " + ApiAccount.class.getName() + " where apiKey==:1" ).execute( apiAccount.getApiKey() ) ).isEmpty() ) {
					// This will (likely) never happen, but just in case this will ensure that the same key will not be associated with multiple API accounts.
					throw new RuntimeException( "Failed to save new API key, please try again." );
				}
				if ( accountInfo.getContactEmail() != null && !accountInfo.getContactEmail().isEmpty() )
					apiAccount.setContactEmail( accountInfo.getContactEmail() );
				if ( accountInfo.getName() != null && !accountInfo.getName().isEmpty() )
					apiAccount.setName( accountInfo.getName() );
				if ( accountInfo.getCountry() != null && !accountInfo.getCountry().isEmpty() )
					apiAccount.setCountry( accountInfo.getCountry() );
				if ( accountInfo.getComment() != null )
					apiAccount.setComment( accountInfo.getComment() );
				apiAccount.setNotificationAvailOps( 2000 ); // Default notification available Ops
				pm.makePersistent( apiAccount );
				
				// Notification Email will be sent when API payment is registered 
			}
			else {
				if ( !( (List< ? >) pm.newQuery( "select key from " + Account.class.getName() + " where user==:1" ).execute( newUser ) ).isEmpty() )
					return RpcResult.createErrorResult( "There is already an account with this Google account!" );
				
				// Create and save account
				final Account account = new Account( newUser );
				ServerUtils.initializeNewAccount( pm, account );
				if ( accountInfo.getContactEmail() != null && !accountInfo.getContactEmail().isEmpty() )
					account.setContactEmail( accountInfo.getContactEmail() );
				if ( accountInfo.getName() != null && !accountInfo.getName().isEmpty() )
					account.setName( accountInfo.getName() );
				if ( accountInfo.getCountry() != null && !accountInfo.getCountry().isEmpty() )
					account.setCountry( accountInfo.getCountry() );
				if ( accountInfo.getComment() != null && !accountInfo.getComment().isEmpty())
					account.setComment( accountInfo.getComment() );
				pm.makePersistent( account );
				
				if ( accountInfo.isFreeAccount() ) {
					// Email will be sent by the TaskServlet:
					TaskServlet.register_updatePackageTask( account.getKey() );
				}
				// Else notification Email will be sent when payment is registered 
			}
			
		} finally {
			if ( pm != null )
				pm.close();
		}
		
		return RpcResult.createInfoResult( ( accountInfo.isApiAccount() ? "New API" : "New " ) + "Account created successfully." );
	}
	
	@Override
	public RpcResult< Void > registerPayment( final PaymentInfo paymentInfo ) {
		LOGGER.fine( "For Google account: " + paymentInfo.getGoogleAccount() );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		if ( !userService.isUserAdmin() )
			return RpcResult.createNoPermissionErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			// Perform checks
			if ( paymentInfo.getGoogleAccount() == null || paymentInfo.getGoogleAccount().isEmpty() )
				return RpcResult.createErrorResult( "Google account is required!" );
			
			if ( paymentInfo.isApiPayment() ) {
				final List< ApiAccount > apiAccountList = new JQBuilder<>( pm, ApiAccount.class ).filter( "user==p1", "USER p1" ).get( new User( paymentInfo.getGoogleAccount(), "gmail.com" ) );
				if ( apiAccountList.isEmpty() )
					return RpcResult.createErrorResult( "No API account found for the specified Google account!" );
				
				if ( paymentInfo.getApiGrossPayment() == null )
					return RpcResult.createErrorResult( "API Gross Payment is required!" );
				else
					if ( paymentInfo.getApiGrossPayment() < 0 )
						return RpcResult.createErrorResult( "Invalid API Gross Payment!" );
				if ( paymentInfo.getApiNetPayment() == null )
					return RpcResult.createErrorResult( "API Net Payment is required!" );
				else
					if ( paymentInfo.getApiNetPayment() < 0 )
						return RpcResult.createErrorResult( "Invalid API Net Payment!" );
				
				final ApiAccount apiAccount = apiAccountList.get( 0 );
				
				// Create and save API payment
				final ApiPayment apiPayment = new ApiPayment( apiAccount.getKey() );
				apiPayment.setPaypalAccount( paymentInfo.getPaypalAccount  () );
				apiPayment.setBankAccount  ( paymentInfo.getBankAccount    () );
				apiPayment.setGrossPayment ( paymentInfo.getApiGrossPayment() );
				apiPayment.setNetPayment   ( paymentInfo.getApiNetPayment  () );
				apiPayment.setPaidOps      ( apiPayment.getNetPayment() * 100 ); // 1 USD = 10,000 Ops => 1 cent = 100 Ops
				if ( paymentInfo.getComment() != null )
					apiPayment.setComment( paymentInfo.getComment() );
				pm.makePersistent( apiPayment );
				
				if ( apiAccount.getPaidOps() == 0l ) {
					// API Account registration, first API payment
					final String body = ServerUtils.concatenateLines(
							"Hi " + apiAccount.getAddressedBy() + ",", null,
							"Your Sc2gears Database API account is ready.", null,
							"This is your API key:", null,
							apiAccount.getApiKey(), null,
							"You can view your API account here:",
							"https://sciigears.appspot.com/ApiUser.html", null,
							"Should you have any questions, hit \"Reply\" to this email.", null,
							"Regards,",
							"   Andras Belicza" );
					if ( !ServerUtils.sendEmail( apiAccount, "API Account info", body ) )
						ServerUtils.sendEmailToAdmin( "Failed to notify client",
								ServerUtils.concatenateLines( "API Payment registered successfully, but failed to send email notification about API account creation!", "Google account: " + apiAccount.getUser().getEmail() ) );
				}
				else {
					// Non-first API payment
					final String body = ServerUtils.concatenateLines(
							"Hi " + apiAccount.getAddressedBy() + ",", null,
							"This email is sent to notify you that your API payment has been registered.", null,
							"You can view your API account here:",
							"https://sciigears.appspot.com/ApiUser.html", null,
							"Should you have any questions, hit \"Reply\" to this email.", null,
							"Regards,",
							"   Andras Belicza" );
					if ( !ServerUtils.sendEmail( apiAccount, "Successful API Payment registration", body ) )
						ServerUtils.sendEmailToAdmin( "Failed to notify client",
								ServerUtils.concatenateLines( "API Payment registered successfully, but failed to send email notification about API payment registration!", "Google account: " + apiAccount.getUser().getEmail() ) );
				}
				apiAccount.increasePaidOps( apiPayment.getPaidOps() );
			}
			else {
				final Key accountKey = CachingService.getAccountKeyByUser( pm, new User( paymentInfo.getGoogleAccount(), "gmail.com" ) );
				if ( accountKey == null )
					return RpcResult.createErrorResult( "No account found for the specified Google account!" );
				if ( paymentInfo.getRealPayment() == null )
					return RpcResult.createErrorResult( "Real Payment is required!" );
				else
					if ( paymentInfo.getRealPayment().floatValue() < 0f )
						return RpcResult.createErrorResult( "Invalid Real Payment!" );
				if ( paymentInfo.getVirtualPayment() == null )
					paymentInfo.setVirtualPayment( paymentInfo.getRealPayment() );
				else
					if ( paymentInfo.getVirtualPayment().floatValue() < 0f )
						return RpcResult.createErrorResult( "Invalid Virtual Payment!" );
				
				// Create and save payment
				final Payment payment = new Payment( accountKey );
				payment.setPaypalAccount ( paymentInfo.getPaypalAccount () );
				payment.setBankAccount   ( paymentInfo.getBankAccount   () );
				payment.setRealPayment   ( paymentInfo.getRealPayment   () );
				payment.setVirtualPayment( paymentInfo.getVirtualPayment() );
				if ( paymentInfo.getComment() != null )
					payment.setComment( paymentInfo.getComment() );
				pm.makePersistent( payment );
				
				// Email will be sent by the TaskServlet:
				TaskServlet.register_updatePackageTask( accountKey );
			}
			
		} finally {
			if ( pm != null )
				pm.close();
		}
		
		return RpcResult.createInfoResult( ( paymentInfo.isApiPayment() ? "API " : "" ) + "Payment registered successfully." );
	}
	
	@Override
	public RpcResult< ActivityInfo > getActivityInfo( final int minutes ) {
		LOGGER.fine( "minutes: " + minutes );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		if ( !userService.isUserAdmin() )
			return RpcResult.createNoPermissionErrorResult();
		
		if ( minutes < 1 || minutes > 10080 )
			return RpcResult.createErrorResult( "Invalid minutes (must be between 1 and 10080)!" );
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final ActivityInfo activityInfo = new ActivityInfo();
			
			final Date fromDate = new Date( System.currentTimeMillis() - minutes * 60l * 1000l );
			
			// New entity and download counts
			final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
			activityInfo.setVisitsCount                 ( ServerUtils.countNewEntities( ds, Visit                 .class, fromDate ) );
			activityInfo.setAccountsCount               ( ServerUtils.countNewEntities( ds, Account               .class, fromDate ) );
			activityInfo.setMousePracticeGameScoresCount( ServerUtils.countNewEntities( ds, MousePracticeGameScore.class, fromDate ) );
			activityInfo.setMapsCount                   ( ServerUtils.countNewEntities( ds, hu.belicza.andras.sc2gearsdb.datastore.Map.class, fromDate ) );
			activityInfo.setRepProfilesCount            ( ServerUtils.countNewEntities( ds, RepProfile            .class, fromDate ) );
			activityInfo.setRepCommentsCount            ( ServerUtils.countNewEntities( ds, RepComment            .class, fromDate ) );
			activityInfo.setRepRatesCount               ( ServerUtils.countNewEntities( ds, RepRate               .class, fromDate ) );
			activityInfo.setEventsCount                 ( ServerUtils.countNewEntities( ds, Event                 .class, fromDate ) );
			final Query q = new Query( D.class.getSimpleName() );
			q.setFilter( CompositeFilterOperator.and(
				new Query.FilterPredicate( "f", FilterOperator.EQUAL, DownloadServlet.LATEST_RELEASE_FILE_NAME ),
				new Query.FilterPredicate( "d", FilterOperator.GREATER_THAN, fromDate ) ) );
			activityInfo.setReleaseDownloadsCount       ( ServerUtils.countEntities   ( ds, q ) );
			
			// JDO version:
			/*activityInfo.setVisitsCount                 ( ServerUtils.countNewEntities( pm, Visit                 .class, fromDate ) );
			activityInfo.setAccountsCount               ( ServerUtils.countNewEntities( pm, Account               .class, fromDate ) );
			activityInfo.setMousePracticeGameScoresCount( ServerUtils.countNewEntities( pm, MousePracticeGameScore.class, fromDate ) );
			activityInfo.setMapsCount                   ( ServerUtils.countNewEntities( pm, hu.belicza.andras.sc2gearsdb.datastore.Map.class, fromDate ) );
			activityInfo.setRepProfilesCount            ( ServerUtils.countNewEntities( pm, RepProfile            .class, fromDate ) );
			activityInfo.setRepCommentsCount            ( ServerUtils.countNewEntities( pm, RepComment            .class, fromDate ) );
			activityInfo.setRepRatesCount               ( ServerUtils.countNewEntities( pm, RepRate               .class, fromDate ) );
			activityInfo.setReleaseDownloadsCount       ( ServerUtils.countEntities   ( pm.newQuery( "select k from " + D.class.getName() + " where f==:1 && d>:2" ), DownloadServlet.LATEST_RELEASE_FILE_NAME, fromDate ) );
			activityInfo.setEventsCount                 ( ServerUtils.countNewEntities( pm, Event                 .class, fromDate ) );*/

			
			// Latest file type stat info list:
			final Map< Key, FileStatInfo > accountKeyFileStatInfoMap = new HashMap< Key, FileStatInfo >();
			
			// Round 1: Gather SC2Replays
			JQBuilder< ? extends FileMetaData > qb = new JQBuilder<>( pm, Rep.class ).filter( "date>p1", "DATE p1" ).range( 0, 1000 );
			while ( true ) {
				final List< ? extends FileMetaData > fileMetaDataList = qb.get( fromDate );
				for ( final FileMetaData fileMetaData : fileMetaDataList ) {
					FileStatInfo fileStatInfo = accountKeyFileStatInfoMap.get( fileMetaData.getOwnerk() );
					if ( fileStatInfo == null )
						accountKeyFileStatInfoMap.put( fileMetaData.getOwnerk(), fileStatInfo = ServerUtils.createFileStatInfoForAccount( pm.getObjectById( Account.class, fileMetaData.getOwnerk() ) ) );
					fileStatInfo.integrateRep( fileMetaData.getSize(), fileMetaData.getDate() );
				}
				
				if ( fileMetaDataList.size() < 1000 )
					break;
				
				qb.cursor( fileMetaDataList );
			}
			
			// Round 2: Gather Mouse prints
			qb = new JQBuilder<>( pm, Smpd.class ).filter( "date>p1", "DATE p1" ).range( 0, 1000 );
			while ( true ) {
				final List< ? extends FileMetaData > fileMetaDataList = qb.get( fromDate );
				for ( final FileMetaData fileMetaData : fileMetaDataList ) {
					FileStatInfo fileStatInfo = accountKeyFileStatInfoMap.get( fileMetaData.getOwnerk() );
					if ( fileStatInfo == null )
						accountKeyFileStatInfoMap.put( fileMetaData.getOwnerk(), fileStatInfo = ServerUtils.createFileStatInfoForAccount( pm.getObjectById( Account.class, fileMetaData.getOwnerk() ) ) );
					fileStatInfo.integrateSmpd( fileMetaData.getSize(), fileMetaData.getDate() );
				}
				
				if ( fileMetaDataList.size() < 1000 )
					break;
				
				qb.cursor( fileMetaDataList );
			}
			
			// Round 3: Gather Other files
			qb = new JQBuilder<>( pm, OtherFile.class ).filter( "date>p1", "DATE p1" ).range( 0, 1000 );
			while ( true ) {
				final List< ? extends FileMetaData > fileMetaDataList = qb.get( fromDate );
				for ( final FileMetaData fileMetaData : fileMetaDataList ) {
					FileStatInfo fileStatInfo = accountKeyFileStatInfoMap.get( fileMetaData.getOwnerk() );
					if ( fileStatInfo == null )
						accountKeyFileStatInfoMap.put( fileMetaData.getOwnerk(), fileStatInfo = ServerUtils.createFileStatInfoForAccount( pm.getObjectById( Account.class, fileMetaData.getOwnerk() ) ) );
					fileStatInfo.integrateOther( fileMetaData.getSize(), fileMetaData.getDate() );
				}
				
				if ( fileMetaDataList.size() < 1000 )
					break;
				
				qb.cursor( fileMetaDataList );
			}
			
			// Create file stat info list
			final List< FileStatInfo > fileStatInfoList = new ArrayList< FileStatInfo >( accountKeyFileStatInfoMap.size() );
			fileStatInfoList.addAll( accountKeyFileStatInfoMap.values() );
			
			Collections.sort( fileStatInfoList, new Comparator< FileStatInfo >() {
				@Override
				public int compare( final FileStatInfo i1, final FileStatInfo i2 ) {
					return new Long( i2.getAllStorage() ).compareTo( i1.getAllStorage() );
				}
			} );
			
			activityInfo.setLatestFileStatInfoList( fileStatInfoList );
			
			return new RpcResult< ActivityInfo >( activityInfo );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< List< VisitInfo > > getVisitInfoList( final VisitType type, final int hours, final Boolean hasAccount ) {
		LOGGER.fine( "Type: " + type.name() + ", hours: " + hours + ", has account: " + hasAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		if ( !userService.isUserAdmin() )
			return RpcResult.createNoPermissionErrorResult();
		
		if ( hours < 1 || hours > 336 )
			return RpcResult.createErrorResult( "Invalid hours (must be between 1 and 336)!" );
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Date fromDate = new Date( System.currentTimeMillis() - hours * 60L * 60 * 1000l );
			
			final List< VisitInfo > visitInfoList = new ArrayList< VisitInfo >();
			
			// "visitorKey==null" filter is OK, but can't append "visitorKey!=null" inequality filter
			// because that would require to sort by it (but we want to sort by date), I'll handle that manually.
			final JQBuilder< ? extends BaseVisit > q = new JQBuilder<>( pm, type == VisitType.VISIT ? Visit.class : ApiVisit.class ).desc( "date" ).range( 0, 1000 )
					.filter( hasAccount == null || hasAccount ? "date>p1" : "date>p1 && visitorKey==null", "DATE p1" );
			
			while ( true ) {
				final List< ? extends BaseVisit > visitList = q.get( fromDate );
				for ( final BaseVisit visit : visitList ) {
					if ( Boolean.TRUE.equals( hasAccount ) && visit.getVisitorKey() == null )
						continue;
					
					final VisitInfo visitInfo = new VisitInfo();
					
					visitInfo.setDate         ( visit.getDate()               );
					visitInfo.setGoogleAccount( visit.getUser().getEmail()    );
					visitInfo.setLocation     ( visit.getCountry()            );
					visitInfo.setIp           ( visit.getIp()                 );
					visitInfo.setHasAccount   ( visit.getVisitorKey() != null );
					visitInfo.setUserAgent    ( visit.getUserAgent()          );
					
					visitInfoList.add( visitInfo );
				}
				
				if ( visitList.size() < 1000 )
					break;
				
				q.cursor( visitList );
			}
			
			return new RpcResult< List< VisitInfo > >( visitInfoList );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< List< FileStatInfo > > getFileStatInfoList( final String googleAccount, final Integer activeLastMins, final Long minStoredBytes ) {
		LOGGER.fine( googleAccount == null ? "" : "For Google account: " + googleAccount + ", active last mins: " + activeLastMins + ", min stored bytes: " + minStoredBytes );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		if ( !userService.isUserAdmin() )
			return RpcResult.createNoPermissionErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final JQBuilder< FileStat > q = new JQBuilder<>( pm, FileStat.class ).range( 0, 1000 );
			
			final Object param;
			
			if ( googleAccount != null ) {
				final Key accountKey = CachingService.getAccountKeyByUser( pm, new User( googleAccount, "gmail.com" ) );
				if ( accountKey == null )
					return new RpcResult< List< FileStatInfo > >( new ArrayList< FileStatInfo >( 0 ) );
				
				param = accountKey;
				q.filter( "ownerKey==p1", "KEY p1" );
			}
			else if ( activeLastMins != null ) {
				q.filter( "updated>p1", "DATE p1" );
				param = new Date( System.currentTimeMillis() - activeLastMins * 60000L );
			}
			else if ( minStoredBytes != null ) {
				q.filter( "storage>p1", "Long p1" );
				param = minStoredBytes;
			}
			else {
				// No filter
				param = null;
			}
			
			final boolean manualSorting = googleAccount != null || activeLastMins != null;
			
			if ( !manualSorting )
				q.desc( "storage" );
			
			final List< FileStatInfo > fileStatInfoList = new ArrayList< FileStatInfo >();
			
			while ( true ) {
				final List< FileStat > fileStatList = q.get( param );
				for ( final FileStat fileStat : fileStatList ) {
					final FileStatInfo fileStatInfo = ServerUtils.createFileStatInfoForAccount( pm.getObjectById( Account.class, fileStat.getOwnerKey() ) );
					
					final DbPackage dbPackage = DbPackage.getFromStorage( fileStatInfo.getPaidStorage() );
					fileStatInfo.setDbPackageIcon( ServerUtils.getDbPackageIcon( dbPackage, fileStatInfo.getPaidStorage(), fileStat.getStorage() ) );
					
					fileStatInfo.setAllCount    ( fileStat.getCount       () );
					fileStatInfo.setAllStorage  ( fileStat.getStorage     () );
					
					fileStatInfo.setRepCount    ( fileStat.getRepCount    () );
					fileStatInfo.setRepStorage  ( fileStat.getRepStorage  () );
					
					fileStatInfo.setSmpdCount   ( fileStat.getSmpdCount   () );
					fileStatInfo.setSmpdStorage ( fileStat.getSmpdStorage () );
					
					fileStatInfo.setOtherCount  ( fileStat.getOtherCount  () );
					fileStatInfo.setOtherStorage( fileStat.getOtherStorage() );
					
					fileStatInfo.setUpdated     ( fileStat.getUpdated     () );
					
					fileStatInfoList.add( fileStatInfo );
				}
				
				if ( fileStatList.size() < 1000 )
					break;
				
				q.cursor( fileStatList );
			}
			
			// Sort result (storage desc)
			if ( manualSorting ) {
				Collections.sort( fileStatInfoList, new Comparator< FileStatInfo >() {
					@Override
                    public int compare( final FileStatInfo f1, final FileStatInfo f2 ) {
						final long d = f2.getAllStorage() - f1.getAllStorage();
	                    return d > 0 ? 1 : d < 0 ? -1 : 0;
                    }
				} );
			}
			
			return new RpcResult< List< FileStatInfo > >( fileStatInfoList );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< Void > recalculateFileInfoStats( final String googleAccount ) {
		LOGGER.fine( "For Google account: " + googleAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		if ( !userService.isUserAdmin() )
			return RpcResult.createNoPermissionErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Key accountKey = CachingService.getAccountKeyByUser( pm, new User( googleAccount, "gmail.com" ) );
			if ( accountKey == null )
				return RpcResult.createErrorResult( "Invalid Google account!" );
			
			TaskServlet.register_recalcFileStatsTask( accountKey );
			
			pm.makePersistent( new Event( accountKey, Type.FILE_STATS_RECALC_TRIGGERED, "By admin: " + user.getEmail() ) );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
		
		return RpcResult.createInfoResult( "File stats recalculation has been kicked-off..." );
	}
	
	@Override
	public RpcResult< List< DlStatInfo > > getDlStatInfoList( final DlStatType type ) {
		LOGGER.fine( "type: " + type.name() );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		if ( !userService.isUserAdmin() )
			return RpcResult.createNoPermissionErrorResult();
		
		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();
			
			final List< DlStatInfo > dlStatInfoList = new ArrayList< DlStatInfo >();
			
			JQBuilder< DownloadStat > q = new JQBuilder<>( pm, DownloadStat.class );
			
			// TODO later add type for "GENERAL_EXTENDED_BY_MONTH" - returning all months
			switch ( type ) {
			case GENERAL :
			case GENERAL_EXTENDED : {
				q.filter( "file==p1", "String p1" );
				final List< String > fileNameList = new ArrayList< String >();
				if ( type == DlStatType.GENERAL ) {
					fileNameList.add( DownloadServlet.LATEST_RELEASE_FILE_NAME );
					fileNameList.add( UserServiceImpl.VISIT_STATS_FILE_NAME );
					fileNameList.add( "custom_portraits.xml" );
				}
				else {
					fileNameList.add( DownloadServlet.LATEST_RELEASE_FILE_NAME );
					fileNameList.add( UserServiceImpl.VISIT_STATS_FILE_NAME );
					fileNameList.add( ApiUserServiceImpl.API_VISIT_STATS_FILE_NAME );
					fileNameList.add( DownloadServlet.LATEST_PLUGIN_API_RELEASE_FILE_NAME );
					fileNameList.add( "custom_portraits.xml" );
					fileNameList.add( "latest_version.xml" );
					fileNameList.add( "start_page.html" );
					fileNameList.add( "start_page_all_sc2gears_news.html" );
				}
				
				for ( final String fileName : fileNameList ) {
					// TODO these can be optimized, example: "file>='custom_portraits.xml@12-12-15' && file<='custom_portraits.xml@12-12-17'"
					// Profit if more than 2 days are displayed!
					if ( DownloadServlet.DAILY_STATS_FILE_NAME_SET.contains( fileName ) ) {
						final Calendar cal = Calendar.getInstance();
						// 2 days: today and yesterday
						for ( int i = 0; i < 2; i++ ) {
							final String loggedFileName = ServerUtils.appendDayToFileName( fileName, cal.getTime() );
							final List< DownloadStat > downloadStatList = q.get( loggedFileName );
							if ( !downloadStatList.isEmpty() )
								dlStatInfoList.add( ServerUtils.convertDownloadStatToDlInfo( downloadStatList.get( 0 ) ) );
							cal.add( Calendar.DATE, -1 );
						}
					}
					else {
						final List< DownloadStat > downloadStatList = q.get( fileName );
						if ( !downloadStatList.isEmpty() )
							dlStatInfoList.add( ServerUtils.convertDownloadStatToDlInfo( downloadStatList.get( 0 ) ) );
					}
				}
				q = null;
				break;
			}
			case SC2GEARS_STARTS :
				q.filter( "file>='custom_portraits.xml@D' && file<='custom_portraits.xml@D9'", null ).desc( "file" ).range( 0, 31 );
				break;
			case LATESTS :
				q.desc( "date" ).range( 0, 20 );
				break;
			case RELEASES :
				q.filter( "file>='Sc2gears-0' && file<='Sc2gears-9'", null );
				break;
			case LANG_FILES :
				q.filter( "file>='lang/0' && file<='lang/9'", null );
				break;
			case ALL :
				break;
			}
			
			if ( q != null ) {
				final List< DownloadStat > downloadStatList = q.get();
				
				for ( final DownloadStat downloadStat : downloadStatList )
					dlStatInfoList.add( ServerUtils.convertDownloadStatToDlInfo( downloadStat ) );
				
				// Sort by date desc
				Collections.sort( dlStatInfoList, new Comparator< DlStatInfo >() {
					@Override
					public int compare( final DlStatInfo d1, final DlStatInfo d2 ) {
						return d2.getDate().compareTo( d1.getDate() );
					}
				} );
			}
			
			return new RpcResult< List<DlStatInfo> >( dlStatInfoList );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< List< ApiCallStatInfo > > getApiActivity( final String firstDay, final String lastDay ) {
		LOGGER.fine( "First day: " + firstDay + ", last day: " + lastDay );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		if ( !userService.isUserAdmin() )
			return RpcResult.createNoPermissionErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			// To keep track of total
			final ApiCallStatInfo totalApiCallStatInfo = new ApiCallStatInfo();
			totalApiCallStatInfo.setGoogleAccount( "TOTAL: ∑ ALL" );
			
			final Map< Key, ApiCallStatInfo > apiAccountKeyApiCallStatInfoMap  = new HashMap< Key, ApiCallStatInfo >();
			
			final JQBuilder< ApiCallStat > q = new JQBuilder<>( pm, ApiCallStat.class ).filter( "day>=p1 && day<=p2", "String p1, String p2" ).range( 0, 1000 );
			
			while ( true ) {
				final List< ApiCallStat > apiCallStatList = q.get( firstDay, lastDay );
				
				for ( final ApiCallStat apiCallStat : apiCallStatList ) {
					ApiCallStatInfo apiCallStatInfo = apiAccountKeyApiCallStatInfoMap.get( apiCallStat.getOwnerKey() );
					if ( apiCallStatInfo == null ) {
						apiAccountKeyApiCallStatInfoMap.put( apiCallStat.getOwnerKey(), apiCallStatInfo = new ApiCallStatInfo() );
						final ApiAccount apiAccount = pm.getObjectById( ApiAccount.class, apiCallStat.getOwnerKey() );
						apiCallStatInfo.setGoogleAccount( apiAccount.getUser().getEmail() );
						apiCallStatInfo.setPaidOps      ( apiAccount.getPaidOps        () );
						// Integrate paid Ops into totals, ONCE only per API account
						totalApiCallStatInfo.setPaidOps( totalApiCallStatInfo.getPaidOps() + apiCallStatInfo.getPaidOps() );
					}
					
					ServerUtils.integrateApiCallStatIntoInfo( apiCallStatInfo, apiCallStat );
					
					// Keep track of totals
					ServerUtils.integrateApiCallStatIntoInfo( totalApiCallStatInfo, apiCallStat );
				}
				
				if ( apiCallStatList.size() < 1000 )
					break;
				
				q.cursor( apiCallStatList );
			}
			
			final List< ApiCallStatInfo > apiCallStatInfoList = new ArrayList< ApiCallStatInfo >( apiAccountKeyApiCallStatInfoMap.size() + 1 );
			// First add the total info record (sorting will not move it even if only 1 stat record which will have the same used ops)
			apiCallStatInfoList.add( totalApiCallStatInfo );
			apiCallStatInfoList.addAll( apiAccountKeyApiCallStatInfoMap.values() );
			
			Collections.sort( apiCallStatInfoList, new Comparator< ApiCallStatInfo >() {
				@Override
				public int compare( final ApiCallStatInfo i1, final ApiCallStatInfo i2 ) {
					return new Long( i2.getUsedOps() ).compareTo( i1.getUsedOps() );
				}
			} );
			
			return new RpcResult< List<ApiCallStatInfo> >( apiCallStatInfoList );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< List< ApiCallStatInfo > > getApiCallStatInfoList( final String googleAccount ) {
		LOGGER.fine( "For Google account: " + googleAccount);
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		if ( !userService.isUserAdmin() )
			return RpcResult.createNoPermissionErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final JQBuilder< ApiCallStat > q = new JQBuilder<>( pm, ApiCallStat.class ).range( 0, 1000 );
			
			final Object[] queryParams;
			if ( googleAccount != null && !googleAccount.isEmpty() ) {
				@SuppressWarnings( "unchecked" )
				final List< Key > apiAccountKey = (List< Key >) pm.newQuery( "select key from " + ApiAccount.class.getName() + " where user==:1" ).execute( new User( googleAccount, "gmail.com" ) );
				if ( apiAccountKey.isEmpty() )
					return new RpcResult< List<ApiCallStatInfo> >( new ArrayList< ApiCallStatInfo >( 0 ) );
				
				q.filter( "ownerKey==p1 && day==p2", "KEY p1, String p2" );
				queryParams = new Object[] { apiAccountKey.get( 0 ), ApiCallStat.DAY_TOTAL };
			}
			else {
				q.filter( "day==p1", "String p1" );
				queryParams = new Object[] { ApiCallStat.DAY_TOTAL };
			}
			
			// To keep track of total
			final ApiCallStatInfo totalApiCallStatInfo = new ApiCallStatInfo();
			totalApiCallStatInfo.setGoogleAccount( "TOTAL: ∑ ALL" );
			
			final List< ApiCallStatInfo > apiCallStatInfoList = new ArrayList< ApiCallStatInfo >();
			// First add the total info record
			apiCallStatInfoList.add( totalApiCallStatInfo );
			
			while ( true ) {
				final List< ApiCallStat > apiCallStatList = q.get( queryParams );
				
				for ( final ApiCallStat apiCallStat : apiCallStatList ) {
					final ApiCallStatInfo info = new ApiCallStatInfo();
					
					final ApiAccount apiAccount  = pm.getObjectById( ApiAccount.class, apiCallStat.getOwnerKey() );
					info.setGoogleAccount   ( apiAccount .getUser().getEmail () );
					info.setPaidOps         ( apiAccount .getPaidOps         () );
					ServerUtils.integrateApiCallStatIntoInfo( info, apiCallStat );
					apiCallStatInfoList.add( info );
					
					// Keep track of totals
					totalApiCallStatInfo.integrateApiCallStat( info );
					totalApiCallStatInfo.setPaidOps( totalApiCallStatInfo.getPaidOps() + info.getPaidOps() );
				}
				
				if ( apiCallStatList.size() < 1000 )
					break;
				
				q.cursor( apiCallStatList );
			}
			
			Collections.sort( apiCallStatInfoList, new Comparator< ApiCallStatInfo >() {
				@Override
				public int compare( final ApiCallStatInfo i1, final ApiCallStatInfo i2 ) {
					return new Long( i2.getUsedOps() ).compareTo( i1.getUsedOps() );
				}
			} );
			
			return new RpcResult< List<ApiCallStatInfo> >( apiCallStatInfoList );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	
	// =======================================================================================================================================
	// ================================== M I S C  F U N C T I O N S =========================================================================
	// =======================================================================================================================================
	
	/** Static map of the misc functions and their implementations. */
	private static final Map< String, DatastoreTask > miscFunctionMap = new HashMap< String, DatastoreTask >();
	static {
		int pos = 1;
		
		miscFunctionMap.put( pos++ + ". Check 'UNUSED' accounts", new DatastoreTask() {
			@Override
			public String[] getParamNames() {
			    return new String[] { "Max updates", "Comment to query" };
			}
			@Override
			public String execute( final HttpServletRequest request, final PersistenceManager pm, final String[] params ) {
				int maxUpdates = params[ 0 ].isEmpty() ? Integer.MAX_VALUE : Integer.parseInt( params[ 0 ] );
				final String commentToQuery = params[ 1 ].isEmpty() ? "UNUSED" : params[ 1 ];
				String becameUsed = "";
				int becameUsedCount = 0;
				@SuppressWarnings("unchecked")
				final List< Key > accountKeyList = (List< Key >) pm.newQuery( "select key from " + Account.class.getName() + " where comment==:1" ).execute( commentToQuery );
				for ( final Key accountKey : accountKeyList ) {
					@SuppressWarnings("unchecked")
					final List< Key > keyList = (List< Key >) pm.newQuery( "select key from " + FileStat.class.getName() + " where ownerKey==:1" ).execute( accountKey );
					if ( !keyList.isEmpty() ) {
						final Account account = pm.getObjectById( Account.class, accountKey );
						account.setComment( null );
						becameUsed += ( becameUsed.isEmpty() ? "" : ", " ) + account.getUser().getEmail();
						becameUsedCount++;
						if ( --maxUpdates <= 0 )
							break;
					}
				}
				return "UNUSED: " + accountKeyList.size() + ( becameUsed.isEmpty() ? ", all remained unused." : ", became used: " + becameUsedCount + "; " + becameUsed );
			}
		} );
		
		miscFunctionMap.put( pos++ + ". Calculate Monthly DL stats from Daily stats", new DatastoreTask() {
			@Override
			public String[] getParamNames() {
			    return new String[] { "Month ('yy-MM')" };
			}
            @Override
			public String execute( final HttpServletRequest request, final PersistenceManager pm, final String[] params ) {
            	// TODO parse month, check if that month is over!
            	// See ServerUtils.appendDayToFileName(), ServerUtils.appendMonthToFileName()
    			String ret = "";
            	DownloadStatInfo downloadStatInfo = CachingService.getDownloadStatByFile( "custom_portraits.xml@D12-12-13" );
            	if ( downloadStatInfo != null ) {
            		ret += "; cust_portr: " + downloadStatInfo.getCount() + ", " + downloadStatInfo.getJavaClient() + ", "
            				+ downloadStatInfo.getUnique() + ", " + downloadStatInfo.getPersistingCycleCounter();
            	}
            	downloadStatInfo = CachingService.getDownloadStatByFile( "latest_version.xml@D12-12-13" );
            	if ( downloadStatInfo != null ) {
            		ret += "; lat_ver: " + downloadStatInfo.getCount() + ", " + downloadStatInfo.getJavaClient() + ", "
            				+ downloadStatInfo.getUnique() + ", " + downloadStatInfo.getPersistingCycleCounter();
            	}
            	downloadStatInfo = CachingService.getDownloadStatByFile( "start_page.html@D12-12-13" );
            	if ( downloadStatInfo != null ) {
            		ret += "; start_page: " + downloadStatInfo.getCount() + ", " + downloadStatInfo.getJavaClient() + ", "
            				+ downloadStatInfo.getUnique() + ", " + downloadStatInfo.getPersistingCycleCounter();
            	}
            	
            	final Set< String > fileNameSet = DownloadServlet.DAILY_STATS_FILE_NAME_SET;
            	fileNameSet.size();
            	
            	return "TODO Not yet implemented!" + " " + ret;
			}
		} );
		
		miscFunctionMap.put( pos++ + ". Change Google account", new DatastoreTask() {
			@Override
			public String[] getParamNames() {
			    return new String[] { "Google account", "New Google account" };
			}
            @Override
			public String execute( final HttpServletRequest request, final PersistenceManager pm, final String[] params ) {
				if ( !ServerUtils.isEmailValid( params[ 1 ] ) )
					return "Invalid new Google account email!";
				if ( params[ 1 ].equals( params[ 0 ] ) )
					return "Old and new Google accounts must be different!";
				
				final JQBuilder< Account > q = new JQBuilder<>( pm, Account.class ).filter( "user==p1", "USER p1" );
				
				final List< Account > accountList = q.get( new User( params[ 0 ], "gmail.com" ) );
				if ( accountList.isEmpty() )
					return "No account found for the specified Google account!";
				
				// Check if new account is not yet in use
				final User newUser = new User( params[ 1 ], "gmail.com" );
				final List< Account > account2List = q.get( newUser );
				if ( !account2List.isEmpty() )
					return "New Google account is already in use!";
				
				final Account account = accountList.get( 0 );
				pm.makePersistent( new Event( account.getKey(), Type.CHANGE_GOOGLE_ACCOUNT, "Old Google acc: " + account.getUser().getEmail() ) );
				account.setUser( newUser );
				
				final boolean removedFromCache = CachingService.removeUserAccountKey( params[ 0 ] );
				
				return "Google account changed successfully. ("
					+ ( removedFromCache ? params[ 0 ] + " removed from memache)" : params[ 0 ] + " was not cached)" );
			}
		} );
		
		miscFunctionMap.put( pos++ + ". ENQUEUE v3 Files to upgrade to v4", new DatastoreTask() {
			@Override
			public String[] getParamNames() {
			    return new String[] { "Type ['SC2Replay','smpd','Other']", "Max count" };
			}
			@Override
			public String execute( final HttpServletRequest request, final PersistenceManager pm, final String[] params ) {
				final FileType fileType = FileType.fromString( params[ 0 ] );
				if ( fileType == null )
					return "Invalid type!";
				
				final Map< String, String > paramMap = new HashMap< String, String >();
				paramMap.put( "fileType", params[ 0 ] );
				
				final Query q = new Query( FILE_TYPE_CLASS_MAP.get( fileType ).getSimpleName() );
				q.setFilter( new FilterPredicate( "v", FilterOperator.EQUAL, 3 ) );
				q.setKeysOnly();
				final EntityBatchUpdater batchUpdater = new EntityBatchUpdater( q, new EntityProcessor() {
					@Override
                    public void processEntity( final Entity e ) {
    					paramMap.put( "key", KeyFactory.keyToString( e.getKey() ) );
    					TaskServlet.register_customTask( paramMap );
                    }
				} );
				batchUpdater.setMaxUpdates( Integer.parseInt( params[ 1 ] ) );
				batchUpdater.setAutoSave( false ); // We do not modify entities here...
				
				batchUpdater.processEntities();
				return "Enqueued " + batchUpdater.getProcessedCount();
			}
		} );
		
		miscFunctionMap.put( pos++ + ". Upgrade v2 FileStat to v3", new DatastoreTask() {
			@Override
			public String[] getParamNames() {
			    return new String[] { "Max count" };
			}
			@Override
			public String execute( final HttpServletRequest request, final PersistenceManager pm, final String[] params ) {
				final Query q = new Query( FileStat.class.getSimpleName() );
				q.setFilter( new FilterPredicate( "v", FilterOperator.EQUAL, 2 ) );
				final EntityBatchUpdater batchUpdater = new EntityBatchUpdater( q, new EntityProcessor() {
					@Override
                    public void processEntity( final Entity e ) {
						// Custom task may go here...
                    }
				} );
				batchUpdater.setMaxUpdates( Integer.parseInt( params[ 0 ] ) );
				batchUpdater.setMakePropertiesUnindexed( "count", "inbw", "outbw" );
				batchUpdater.setNewVersionToSet( 3 );
				
				return batchUpdater.processEntities();
			}
		} );
		
		miscFunctionMap.put( pos++ + ". Upgrade D: make ['u', 'c'] unindexed", new DatastoreTask() {
			@Override
			public String[] getParamNames() {
			    return new String[] { "Max count" };
			}
			@Override
			public String execute( final HttpServletRequest request, final PersistenceManager pm, final String[] params ) {
				final Query q = new Query( D.class.getSimpleName() );
				q.addSort( "u" );
				final EntityBatchUpdater batchUpdater = new EntityBatchUpdater( q );
				batchUpdater.setMaxUpdates( Integer.parseInt( params[ 0 ] ) );
				batchUpdater.setMakePropertiesUnindexed( "c", "u" );
				
				return batchUpdater.processEntities();
			}
		} );
		
		miscFunctionMap.put( pos++ + ". Change 10MB paid storage to 5MB ", new DatastoreTask() {
			@Override
			public String[] getParamNames() {
			    return new String[] { "Max updates" };
			}
			@Override
			public String execute( final HttpServletRequest request, final PersistenceManager pm, final String[] params ) {
				int maxUpdates = params[ 0 ].isEmpty() ? Integer.MAX_VALUE : Integer.parseInt( params[ 0 ] );
				String changed = "";
				int changedCount = 0;
				final javax.jdo.Query query = pm.newQuery( "select key from " + Account.class.getName() + " where paidStorage==:1" );
				query.setRange( 0,  maxUpdates + 1 );
				@SuppressWarnings("unchecked")
				final List< Key > accountKeyList = (List< Key >) query.execute( 10_000_000 );
				for ( final Key accountKey : accountKeyList ) {
						final Account account = pm.getObjectById( Account.class, accountKey );
						account.setPaidStorage( 5_000_000 );
						changed += ( changed.isEmpty() ? "" : ", " ) + account.getUser().getEmail();
						changedCount++;
						if ( --maxUpdates <= 0 )
							break;
				}
				return "At least: " + accountKeyList.size() + ( changed.isEmpty() ? ", all remained unchanged." : ", changed: " + changedCount + "; " + changed );
			}
		} );
		
	}
	
	@Override
	public RpcResult< List< MiscFunctionInfo > > getMiscFunctionInfoList() {
		LOGGER.fine( "" );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		if ( !userService.isUserAdmin() )
			return RpcResult.createNoPermissionErrorResult();
		
		final List< MiscFunctionInfo > miscFunctionInfoList = new ArrayList< MiscFunctionInfo >( miscFunctionMap.size() );
		
		for ( final Entry< String, DatastoreTask > entry : miscFunctionMap.entrySet() )
			miscFunctionInfoList.add( new MiscFunctionInfo( entry.getKey(), entry.getValue().getParamNames() ) );
		
		Collections.sort( miscFunctionInfoList, new Comparator< MiscFunctionInfo >() {
			@Override
            public int compare( final MiscFunctionInfo i1, final MiscFunctionInfo i2 ) {
	            return i1.getName().compareTo( i2.getName() );
            }
		} );
		
		return new RpcResult< List< MiscFunctionInfo > >( miscFunctionInfoList );
	}
	
	@Override
	public RpcResult< String > executeMiscFunction( final boolean autoTx, final String functionName, final String[] params ) {
		final StringBuilder paramsBuilder = new StringBuilder( "[" );
		for ( final String param : params ) {
			if ( paramsBuilder.length() > 1 )
				paramsBuilder.append( ", " );
			paramsBuilder.append( param );
		}
		paramsBuilder.append( ']' );
		LOGGER.fine( "Auto Tx: " + autoTx + ", Function name: " + functionName + ", params: " + paramsBuilder );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		if ( !userService.isUserAdmin() )
			return RpcResult.createNoPermissionErrorResult();
		
		final DatastoreTask miscFunction = miscFunctionMap.get( functionName );
		if ( miscFunction == null ) {
			LOGGER.warning( "Invalid function name!" );
			return RpcResult.createErrorResult( "Invalid function name!" );
		}
		
		PersistenceManager pm = null;
		try {
			
			pm = ( autoTx ? PMF.getAutoTx() : PMF.getNoAutoTx() ).getPersistenceManager();
			
			final long start = System.nanoTime();
			String result = miscFunction.execute( getThreadLocalRequest(), pm, params );
			final long end = System.nanoTime();
			
			return new RpcResult< String >( "[" + ServerUtils.DECIMAL_FORMAT.format( ( end - start ) / 1000000l ) + " ms] Execution result: " + result );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
}
