/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.user.server;

import static hu.belicza.andras.sc2gearsdb.FileServlet.FILE_TYPE_CLASS_MAP;
import static hu.belicza.andras.sc2gearsdb.common.client.CommonApi.MAX_PAGE_SIZE;
import hu.belicza.andras.mpq.InvalidMpqArchiveException;
import hu.belicza.andras.mpq.MpqParser;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.sc2replay.model.Details.Player;
import hu.belicza.andras.sc2gears.sc2replay.model.Replay;
import hu.belicza.andras.sc2gearsdb.Consts;
import hu.belicza.andras.sc2gearsdb.TaskServlet;
import hu.belicza.andras.sc2gearsdb.common.client.RpcResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.EntityListResult;
import hu.belicza.andras.sc2gearsdb.common.client.pagingtable.PageInfo;
import hu.belicza.andras.sc2gearsdb.common.server.CommonUtils.DbPackage;
import hu.belicza.andras.sc2gearsdb.datastore.Account;
import hu.belicza.andras.sc2gearsdb.datastore.Event;
import hu.belicza.andras.sc2gearsdb.datastore.Event.Type;
import hu.belicza.andras.sc2gearsdb.datastore.FileMetaData;
import hu.belicza.andras.sc2gearsdb.datastore.FileStat;
import hu.belicza.andras.sc2gearsdb.datastore.OtherFile;
import hu.belicza.andras.sc2gearsdb.datastore.Payment;
import hu.belicza.andras.sc2gearsdb.datastore.Rep;
import hu.belicza.andras.sc2gearsdb.datastore.Smpd;
import hu.belicza.andras.sc2gearsdb.datastore.Visit;
import hu.belicza.andras.sc2gearsdb.user.client.Permission;
import hu.belicza.andras.sc2gearsdb.user.client.beans.DbPackageInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.FileStatInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.FreeAccountInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.MousePrintFilters;
import hu.belicza.andras.sc2gearsdb.user.client.beans.MousePrintFullInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.MousePrintInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.OtherFileFilters;
import hu.belicza.andras.sc2gearsdb.user.client.beans.OtherFileInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.PaymentInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.PaymentsInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.QuotaStatus;
import hu.belicza.andras.sc2gearsdb.user.client.beans.ReplayFilters;
import hu.belicza.andras.sc2gearsdb.user.client.beans.ReplayFullInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.ReplayInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.SettingsInfo;
import hu.belicza.andras.sc2gearsdb.user.client.beans.UserInfo;
import hu.belicza.andras.sc2gearsdb.util.ByteArrayMpqDataInput;
import hu.belicza.andras.sc2gearsdb.util.CachingService;
import hu.belicza.andras.sc2gearsdb.util.JQBuilder;
import hu.belicza.andras.sc2gearsdb.util.PMF;
import hu.belicza.andras.sc2gearsdb.util.ServerUtils;
import hu.belicza.andras.sc2gearsdbapi.FileServletApi;
import hu.belicza.andras.sc2gearsdbapi.FileServletApi.FileType;
import hu.belicza.andras.sc2gearsdbapi.InfoServletApi;
import hu.belicza.andras.sc2gearsdbapi.ServletApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Server side of the user module.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class UserServiceImpl extends RemoteServiceServlet implements hu.belicza.andras.sc2gearsdb.user.client.UserService {
	
	/** File name to appear for the visit stats in the download stats. */
	public static final String VISIT_STATS_FILE_NAME = "%VISITS%";
	
	private static final Logger LOGGER = Logger.getLogger( UserServiceImpl.class.getName() );
	
    @Override
	public RpcResult< UserInfo > getUserInfo( final String sharedAccount ) {
		LOGGER.fine( sharedAccount == null ? "" : "Shared account: " + sharedAccount );
		
		final UserInfo userInfo = new UserInfo();
		
		final UserService userService = UserServiceFactory.getUserService();
		final User        user        = userService.getCurrentUser();
		
		if ( user == null )
			userInfo.setLoginUrl( userService.createLoginURL( "/User.html" ) );
		else {
			userInfo.setUserNickname( user.getNickname() );
			userInfo.setUserName    ( user.getNickname() );
			userInfo.setLogoutUrl   ( userService.createLogoutURL( "/User.html" ) );
			
			PersistenceManager pm = null;
			try {
				pm = PMF.get().getPersistenceManager();
				
				final Account account = ServerUtils.getAccount( pm, sharedAccount, user );
				
				if ( sharedAccount == null ) {
					// Accessing own account...
					if ( account != null ) {
						final List< Visit > visitList = new JQBuilder<>( pm, Visit.class ).filter( "visitorKey==p1", "KEY p1" ).desc( "date" ).range( 0, 1 ).get( account.getKey() );
						if ( !visitList.isEmpty() )
							userInfo.setLastVisit( visitList.get( 0 ).getDate() );
					}
					
					// Check if visiting user is new (unique)
					final Query q = new Query( Visit.class.getSimpleName() );
					q.setFilter( new FilterPredicate( "user", FilterOperator.EQUAL, user ) );
					final boolean isNew = ServerUtils.isQueryResultEmpty( q );
					
					// Log visit
					final Visit visit = new Visit( user );
					visit.fillTracking( getThreadLocalRequest() );
					if ( account != null )
						visit.setVisitorKey( account.getKey() );
					pm.makePersistent( visit );
					
					// Update visit stats
					TaskServlet.register_updateDownloadStat( VISIT_STATS_FILE_NAME, getThreadLocalRequest().getHeader( "User-Agent" ), isNew, 1 );
					
					if ( account != null ) {
						if ( account.getName() != null && !account.getName().isEmpty() )
							userInfo.setUserName( account.getName() );
						
						userInfo.setHasAccount( true );
						
						final Map< String, String > downloadParameterMap = new HashMap< String, String >();
						downloadParameterMap.put( FileServletApi.PARAM_PROTOCOL_VERSION, FileServletApi.PROTOCOL_VERSION_1 );
						downloadParameterMap.put( FileServletApi.PARAM_OPERATION       , FileServletApi.OPERATION_DOWNLOAD );
						userInfo.setDownloadParameterMap( downloadParameterMap );
						
						final Map< String, String > batchDownloadParameterMap = new HashMap< String, String >();
						batchDownloadParameterMap.put( FileServletApi.PARAM_PROTOCOL_VERSION, FileServletApi.PROTOCOL_VERSION_1 );
						batchDownloadParameterMap.put( FileServletApi.PARAM_OPERATION       , FileServletApi.OPERATION_BATCH_DOWNLOAD );
						userInfo.setBatchDownloadParameterMap( batchDownloadParameterMap );
						
						userInfo.setFileTypeParamName     ( FileServletApi.PARAM_FILE_TYPE      );
						userInfo.setSha1ParamName         ( FileServletApi.PARAM_SHA1           );
						userInfo.setSha1ListParamName     ( FileServletApi.PARAM_SHA1_LIST      );
						userInfo.setSharedAccountParamName( FileServletApi.PARAM_SHARED_ACCOUNT );
						userInfo.setReplayFileType        ( FileType.SC2REPLAY  .longName       );
						userInfo.setMousePrintFileType    ( FileType.MOUSE_PRINT.longName       );
						userInfo.setOtherFileType         ( FileType.OTHER      .longName       );
						
						userInfo.setLabelNames   ( ServerUtils.getLabelNames( account )  );
						userInfo.setLabelColors  ( Consts.DEFAULT_REPLAY_LABEL_COLORS    );
						userInfo.setLabelBgColors( Consts.DEFAULT_REPLAY_LABEL_BG_COLORS );
						
						userInfo.setConvertToRealTime     ( account.isConvertToRealTime      () );
						userInfo.setMapImageSize          ( account.getMapImageSize          () );
						userInfo.setDisplayWinners        ( account.getDisplayWinners        () );
						userInfo.setFavoredPlayerList( ServerUtils.cloneList( account.getFavoredPlayers() ) );
						
						userInfo.setFreeAccount( DbPackage.FREE == DbPackage.getFromStorage( account.getPaidStorage() ) );
						
						userInfo.setAdmin( userService.isUserAdmin() );
						
						// Get list of accounts we have been granted access to
						final List< Account > sharedAccountList = new JQBuilder<>( pm, Account.class ).filter( "grantedUsers==p1", "USER p1" ).get( user );
						final List< String >  sharedAccounts    = new ArrayList< String >( sharedAccountList.size() + 1 );
						for ( final Account sharedAccount_ : sharedAccountList )
							sharedAccounts.add( sharedAccount_.getUser().getEmail() );
						// Sort by Google account
						Collections.sort( sharedAccounts, String.CASE_INSENSITIVE_ORDER );
						sharedAccounts.add( 0, account.getUser().getEmail() ); // Insert owner account to the first of the list
						userInfo.setSharedAccounts( sharedAccounts );
					}
					else {
						// Send and display the Google Account email address:
						userInfo.setUserName( user.getEmail() );
					}
				}
				else {
					// Accessing shared account...
					userInfo.setSharedAccount( sharedAccount );
					if ( account != null ) {
						// We have access to this account:
						userInfo.setHasAccount        ( true                                         );
						userInfo.setLabelNames        ( ServerUtils.getLabelNames( account )         );
						userInfo.setGrantedPermissions( account.getGrantedPermissionsForUser( user ) );
					}
				}
				
				if ( account != null && ( sharedAccount == null || account.isPermissionGranted( user, Permission.VIEW_QUOTA ) ) ) {
					final QuotaStatus quotaStatus = new QuotaStatus();
					quotaStatus.setPaidStorage( account.getPaidStorage() );
					quotaStatus.setNotificationQuotaLevel( account.getNotificationQuotaLevel() );
					final List< FileStat > fileStatList = new JQBuilder<>( pm, FileStat.class ).filter( "ownerKey==p1", "KEY p1" ).get( account.getKey() );
					if ( !fileStatList.isEmpty() )
						quotaStatus.setUsedStorage( fileStatList.get( 0 ).getStorage() );
					userInfo.setQuotaStatus( quotaStatus );
				}
				
			} finally {
				if ( pm != null )
					pm.close();
			}
		}
		
		return new RpcResult< UserInfo >( userInfo );
	}
	
    @Override
    public RpcResult< Void > register( final FreeAccountInfo freeAccountInfo ) {
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		if ( freeAccountInfo.getGoogleAccount() == null || freeAccountInfo.getGoogleAccount().isEmpty() )
			return RpcResult.createErrorResult( "Google account is required! Refresh the page!" );
		
		if ( !ServerUtils.isEmailValid( freeAccountInfo.getGoogleAccount() ) )
			return RpcResult.createErrorResult( "Invalid Google account! Refresh the page!" );
		
		if ( !user.getEmail().equals( freeAccountInfo.getGoogleAccount() ) )
			return RpcResult.createErrorResult( "Google account does not match the user you're logged in with! Refresh the page!" );
		
		if ( freeAccountInfo.getContactEmail() != null && freeAccountInfo.getContactEmail().length() > 500 )
			return RpcResult.createErrorResult( "Invalid contact email, cannot be longer than 500 characters!" );
		
		if ( freeAccountInfo.getContactEmail() != null && !freeAccountInfo.getContactEmail().isEmpty() )
			if ( !ServerUtils.isEmailValid( freeAccountInfo.getContactEmail() ) )
				return RpcResult.createErrorResult( "Invalid contact email!" );
		
		if ( freeAccountInfo.getName() != null && freeAccountInfo.getName().length() > 500 )
			return RpcResult.createErrorResult( "Invalid name, cannot be longer than 500 characters!" );
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			if ( !( (List< ? >) pm.newQuery( "select key from " + Account.class.getName() + " where user==:1" ).execute( user ) ).isEmpty() )
				return RpcResult.createErrorResult( "There is already an account with this Google account! Refresh the page!" );
			
			// REGISTRATION IS DISABLED.
			if ( true )
				return RpcResult.createErrorResult( "Registration is disabled!" );
			
			// Create and save account
			final Account account = new Account( user );
			ServerUtils.initializeNewAccount( pm, account );
			if ( freeAccountInfo.getName() != null && !freeAccountInfo.getName().isEmpty() )
				account.setName( freeAccountInfo.getName() );
			if ( freeAccountInfo.getContactEmail() != null && !freeAccountInfo.getContactEmail().isEmpty() )
				account.setContactEmail( freeAccountInfo.getContactEmail() );
			account.setCountry( ServerUtils.countryCodeToName( getThreadLocalRequest().getHeader( "X-AppEngine-Country" ) ) );
			pm.makePersistent( account );
			
			// Email will be sent by the TaskServlet:
			TaskServlet.register_updatePackageTask( account.getKey() );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
		
	    return new RpcResult< Void >();
    }
	
	@Override
	public RpcResult< QuotaStatus > getStorageQuotaStatus( final String sharedAccount ) {
		LOGGER.fine( sharedAccount == null ? "" : "Shared account: " + sharedAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Account account = ServerUtils.getAccount( pm, sharedAccount, user );
			if ( account == null )
				return RpcResult.createNoPermissionErrorResult();
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.VIEW_QUOTA ) )
				return RpcResult.createNoPermissionErrorResult();
			
			final QuotaStatus quotaStatus = new QuotaStatus();
			
			quotaStatus.setPaidStorage( account.getPaidStorage() );
			quotaStatus.setNotificationQuotaLevel( account.getNotificationQuotaLevel() );
			
			final List< FileStat > fileStatList = new JQBuilder<>( pm, FileStat.class ).filter( "ownerKey==p1", "KEY p1" ).get( account.getKey() );
			if ( !fileStatList.isEmpty() )
				quotaStatus.setUsedStorage( fileStatList.get( 0 ).getStorage() );
			
			return new RpcResult< QuotaStatus >( quotaStatus );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< EntityListResult< ReplayInfo > > getReplayInfoList( final String sharedAccount, final PageInfo pageInfo, final ReplayFilters filters ) {
		final String filtersString = filters.toString();
		LOGGER.fine( ( sharedAccount == null ? "" : "Shared account: " + sharedAccount + ", " ) + "pageInfo: {" + pageInfo.toString() + "}"
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
			
			final Key accountKey;
			final boolean hasViewUpdateRepCommentPermission;
			
			final User accountUser = sharedAccount == null ? user : new User( sharedAccount, "gmail.com" );
			// If we're accessing our own account, we can use the CachingService, keys are cached
			if ( sharedAccount == null || ServerUtils.ADMIN_EMAIL_STRING.equals( user.getEmail() )) {
				accountKey                        = CachingService.getAccountKeyByUser( pm, accountUser );
				hasViewUpdateRepCommentPermission = true;
			}
			else {
				// Else we need to fetch the whole account to check the required permission
				final List< Account > accountList = new JQBuilder<>( pm, Account.class ).filter( "user==p1 && grantedUsers==p2", "USER p1, USER p2" ).get( accountUser, user );
				accountKey                        =  accountList.isEmpty() || !accountList.get( 0 ).isPermissionGranted( user, Permission.VIEW_REPLAYS ) ? null : accountList.get( 0 ).getKey();
				hasViewUpdateRepCommentPermission = !accountList.isEmpty() &&  accountList.get( 0 ).isPermissionGranted( user, Permission.VIEW_UPDATE_REP_COMMENTS );
			}
			if ( accountKey == null )
				return RpcResult.createNoPermissionErrorResult();
			
			final List< Object > paramsList     = new ArrayList< Object >();
			final StringBuilder  filtersBuilder = new StringBuilder( "ownerk==p1" );
			final StringBuilder  paramsBuilder  = new StringBuilder( "KEY p1" );
			paramsList.add( accountKey );
			if ( filters.getLabels() != null ) {
				for ( final Integer label : filters.getLabels() ) {
					paramsList.add( label );
					filtersBuilder.append( " && labels==p" ).append( paramsList.size() );
					paramsBuilder.append( ", Integer p" ).append( paramsList.size() );
				}
			}
			if ( filters.getMapName() != null ) {
				paramsList.add( filters.getMapName() );
				filtersBuilder.append( " && map==p" ).append( paramsList.size() );
				paramsBuilder.append( ", String p" ).append( paramsList.size() );
			}
			if ( filters.getPlayers() != null ) {
				final StringTokenizer playerTokenizer = new StringTokenizer( filters.getPlayers(), "," );
				while ( playerTokenizer.hasMoreTokens() ) {
					paramsList.add( playerTokenizer.nextToken().trim() );
					filtersBuilder.append( " && players==p" ).append( paramsList.size() );
					paramsBuilder.append( ", String p" ).append( paramsList.size() );
				}
			}
			if ( filters.getGameType() != null ) {
				paramsList.add( filters.getGameType() );
				filtersBuilder.append( " && type==p" ).append( paramsList.size() );
				paramsBuilder.append( ", String p" ).append( paramsList.size() );
			}
			if ( filters.getFormat() != null ) {
				paramsList.add( filters.getFormat() );
				filtersBuilder.append( " && format==p" ).append( paramsList.size() );
				paramsBuilder.append( ", String p" ).append( paramsList.size() );
			}
			if ( filters.getLeagueMu() != null ) {
				paramsList.add( ServerUtils.normalizeMatchupString( filters.getLeagueMu() ) );
				filtersBuilder.append( " && norlm==p" ).append( paramsList.size() );
				paramsBuilder.append( ", String p" ).append( paramsList.size() );
			}
			if ( filters.getMatchup() != null ) {
				paramsList.add( ServerUtils.normalizeMatchupString( filters.getMatchup() ) );
				filtersBuilder.append( " && normu==p" ).append( paramsList.size() );
				paramsBuilder.append( ", String p" ).append( paramsList.size() );
			}
			if ( hasViewUpdateRepCommentPermission ) {
				// This filter can only be used if the user has the necessary permission.
    			if ( filters.getHasComment() != null ) {
    				paramsList.add( filters.getHasComment() );
    				filtersBuilder.append( " && hasCom==p" ).append( paramsList.size() );
					paramsBuilder.append( ", Boolean p" ).append( paramsList.size() );
    			}
			}
			if ( filters.getGateway() != null ) {
				paramsList.add( filters.getGateway() );
				filtersBuilder.append( " && gw==p" ).append( paramsList.size() );
				paramsBuilder.append( ", String p" ).append( paramsList.size() );
			}
			if ( filters.getFromDate() != null ) {
				paramsList.add( filters.getFromDate() );
				filtersBuilder.append( " && repd>p" ).append( paramsList.size() );
				paramsBuilder.append( ", DATE p" ).append( paramsList.size() );
			}
			if ( filters.getToDate() != null ) {
				filters.getToDate().setTime( filters.getToDate().getTime() + 24L*60*60*1000 ); // Add one day so replays from this day will be included
				paramsList.add( filters.getToDate() );
				filtersBuilder.append( " && repd<p" ).append( paramsList.size() );
				paramsBuilder.append( ", DATE p" ).append( paramsList.size() );
			}
			final List< Rep > replayList = new JQBuilder<>( pm, Rep.class ).filter( filtersBuilder.toString(), paramsBuilder.toString() ).desc( "repd" ).pageInfo( pageInfo ).get( paramsList.toArray() );
			
			final List< ReplayInfo > replayInfoList = new ArrayList< ReplayInfo >( replayList.size() );
			for ( final Rep replay : replayList ) {
				final ReplayInfo replayInfo = new ReplayInfo();
				ServerUtils.copyReplayToReplayInfo( replay, replayInfo );
				if ( hasViewUpdateRepCommentPermission )
					replayInfo.setComment( replay.getComment() );
				replayInfoList.add( replayInfo );
			}
			
			return new RpcResult< EntityListResult< ReplayInfo > >( new EntityListResult< ReplayInfo >( replayInfoList, JDOCursorHelper.getCursor( replayList ).toWebSafeString() ) );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< ReplayFullInfo > getReplayFullInfo( final String sharedAccount, final String sha1 ) {
		LOGGER.fine( ( sharedAccount == null ? "" : "Shared account: " + sharedAccount + ", " ) + "sha1: " + sha1 );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Key accountKey = ServerUtils.getAccountKey( pm, sharedAccount, user, Permission.VIEW_REPLAYS );
			if ( accountKey == null )
				return RpcResult.createNoPermissionErrorResult();
			
			final List< Rep > replayList = new JQBuilder<>( pm, Rep.class ).filter( "ownerk==p1 && sha1==p2", "KEY p1, String p2" ).get( accountKey, sha1 );
			if ( replayList.isEmpty() )
				return RpcResult.createNoPermissionErrorResult();
			
			final Rep replay = replayList.get( 0 );
			
			final ReplayFullInfo replayFullInfo = new ReplayFullInfo();
			
			ServerUtils.copyReplayToReplayInfo( replay, replayFullInfo );
			
			replayFullInfo.setFormat          ( replay.getFormat () );
			replayFullInfo.setUploadedDate    ( replay.getDate   () );
			replayFullInfo.setFileSize        ( replay.getSize   () );
			replayFullInfo.setFileLastModified( replay.getLastmod() );
			replayFullInfo.setFileSize        ( replay.getSize   () );
			
			return new RpcResult< ReplayFullInfo >( replayFullInfo );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< Void > saveReplayLabels( final String sharedAccount, final String sha1, final List< Integer > labels ) {
		LOGGER.fine( ( sharedAccount == null ? "" : "Shared account: " + sharedAccount + ", " ) + "sha1: " + sha1 );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Key accountKey = ServerUtils.getAccountKey( pm, sharedAccount, user, Permission.CHANGE_REP_LABELS );
			if ( accountKey == null )
				return RpcResult.createNoPermissionErrorResult();
			
			final List< Rep > replayList = new JQBuilder<>( pm, Rep.class ).filter( "ownerk==p1 && sha1==p2", "KEY p1, String p2" ).get( accountKey, sha1 );
			if ( replayList.isEmpty() )
				return RpcResult.createNoPermissionErrorResult();
			
			final Rep replay = replayList.get( 0 );
			replay.setLabels( labels );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
		
		return RpcResult.createInfoResult( "Labels saved." );
	}
	
	@Override
	public RpcResult< Void > saveReplayComment( final String sharedAccount, final String sha1, final String comment ) {
		LOGGER.fine( ( sharedAccount == null ? "" : "Shared account: " + sharedAccount + ", " ) + "sha1: " + sha1 );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Key accountKey = ServerUtils.getAccountKey( pm, sharedAccount, user, Permission.VIEW_UPDATE_REP_COMMENTS );
			if ( accountKey == null )
				return RpcResult.createNoPermissionErrorResult();
			
			final List< Rep > replayList = new JQBuilder<>( pm, Rep.class ).filter( "ownerk==p1 && sha1==p2", "KEY p1, String p2" ).get( accountKey, sha1 );
			if ( replayList.isEmpty() )
				return RpcResult.createNoPermissionErrorResult();
			
			final Rep replay = replayList.get( 0 );
			replay.setComment( comment == null || comment.isEmpty() ? null : InfoServletApi.trimPrivateRepComment( comment ) );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
		
		return RpcResult.createInfoResult( "Comment saved." );
	}
	
	@Override
	public RpcResult< List< String > > saveLabelNames( final String sharedAccount, final List< String > labelNames ) {
		LOGGER.fine( sharedAccount == null ? "" : "Shared account: " + sharedAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Account account = ServerUtils.getAccount( pm, sharedAccount, user );
			if ( account == null )
				return RpcResult.createNoPermissionErrorResult();
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.RENAME_LABELS ) )
				return RpcResult.createNoPermissionErrorResult();
			
			// Restore default names for empty strings
			final List< String > checkedNames = new ArrayList< String >( Consts.DEFAULT_REPLAY_LABEL_LIST.size() );
			for ( int i = 0; i < Consts.DEFAULT_REPLAY_LABEL_LIST.size(); i++ )
				checkedNames.add( i >= labelNames.size() || Consts.DEFAULT_REPLAY_LABEL_LIST.get( i ).equals( labelNames.get( i ) ) ? "" : ServletApi.trimStringLength( labelNames.get( i ), 500 ) );
			
			account.setLabelNames( checkedNames );
			
			final RpcResult< List< String > > rpcResult = new RpcResult< List< String > >( ServerUtils.getLabelNames( account ) );
			rpcResult.setInfoMsg( "Label names saved." );
			
			return rpcResult;
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	public RpcResult< List< String > > getProfileUrlList( String sharedAccount, String sha1 ) {
		LOGGER.fine( ( sharedAccount == null ? "" : "Shared account: " + sharedAccount + ", " ) + "sha1: " + sha1 );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Key accountKey = ServerUtils.getAccountKey( pm, sharedAccount, user, Permission.VIEW_REPLAYS );
			if ( accountKey == null )
				return RpcResult.createNoPermissionErrorResult();
			
			// Check if the specified file is owned by the account
			final List< Rep > replayList = new JQBuilder<>( pm, Rep.class ).filter( "ownerk==p1 && sha1==p2", "KEY p1, String p2" ).get( accountKey, sha1 );
			if ( replayList.isEmpty() )
				return RpcResult.createNoPermissionErrorResult();
			
			byte[] content = null;
			try {
				content = ServerUtils.getFileContent( replayList.get( 0 ), null );
				if ( content == null ) {
					LOGGER.warning( "File not found!" );
					return RpcResult.createErrorResult( "Replay not found!" );
				}
			} catch ( final IOException ie ) {
				LOGGER.log( Level.SEVERE, "Some error occured getting the file from the Blobstore!", ie );
				return RpcResult.createErrorResult( "Internal error accessing the replay!" );
			}
			
			MpqParser mpqParser;
            try {
	            mpqParser = new MpqParser( new ByteArrayMpqDataInput( content ) );
            } catch ( final InvalidMpqArchiveException imae ) {
				LOGGER.log( Level.WARNING, "Invalid SC2Replay file!", imae );
				return RpcResult.createErrorResult( "Invalid SC2Replay file!" );
            }
			
			final Replay replay = ReplayFactory.parseReplay( "replayFromBlobstore.SC2Replay", mpqParser, ReplayFactory.GENERAL_INFO_CONTENT );
			if ( replay == null ) {
				LOGGER.info( "Error parsing the replay!" );
				return RpcResult.createErrorResult( "Error parsing the replay!" );
			}
			
			final List< String > profileUrlList = new ArrayList< String >();
			for ( final String playerName : replayList.get( 0 ).getPlayers() ) {
				boolean foundMatch = false;
				for ( final Player p : replay.details.players ) {
					if ( p.playerId.name.equals( playerName ) ) {
						foundMatch = true;
						profileUrlList.add( p.playerId.getBattleNetProfileUrl( null ) );
						break;
					}
				}
				if ( !foundMatch )
					profileUrlList.add( "" ); // Add an empty string to keep proper player indices
			}
			
			return new RpcResult< List< String > >( profileUrlList );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< EntityListResult< MousePrintInfo > > getMousePrintInfoList( final String sharedAccount, final PageInfo pageInfo, final MousePrintFilters filters ) {
		final String filtersString = filters.toString();
		LOGGER.fine( ( sharedAccount == null ? "" : "Shared account: " + sharedAccount + ", " ) + "pageInfo: {" + pageInfo.toString() + "}"
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
			
			final Key accountKey = ServerUtils.getAccountKey( pm, sharedAccount, user, Permission.VIEW_MOUSE_PRINTS );
			if ( accountKey == null )
				return RpcResult.createNoPermissionErrorResult();
			
			final List< Object > paramsList     = new ArrayList< Object >();
			final StringBuilder  filtersBuilder = new StringBuilder( "ownerk==p1" );
			final StringBuilder  paramsBuilder  = new StringBuilder( "KEY p1" );
			
			paramsList.add( accountKey );
			if ( filters.getFromDate() != null ) {
				paramsList.add( filters.getFromDate() );
				filtersBuilder.append( " && end>p" ).append( paramsList.size() );
				paramsBuilder.append( ", DATE p" ).append( paramsList.size() );
			}
			if ( filters.getToDate() != null ) {
				filters.getToDate().setTime( filters.getToDate().getTime() + 24L*60*60*1000 ); // Add one day so replays from this day will be included
				paramsList.add( filters.getToDate() );
				filtersBuilder.append( " && end<p" ).append( paramsList.size() );
				paramsBuilder.append( ", DATE p" ).append( paramsList.size() );
			}
			final List< Smpd > mousePrintList = new JQBuilder<>( pm, Smpd.class ).filter( filtersBuilder.toString(), paramsBuilder.toString() ).desc( "end" ).pageInfo( pageInfo ).get( paramsList.toArray() );
			
			final List< MousePrintInfo > mousePrintInfoList = new ArrayList< MousePrintInfo >( mousePrintList.size() );
			for ( final Smpd mousePrint : mousePrintList ) {
				final MousePrintInfo mousePrintInfo = new MousePrintInfo();
				
				mousePrintInfo.setRecordingStart( mousePrint.getStart () );
				mousePrintInfo.setRecordingEnd  ( mousePrint.getEnd   () );
				mousePrintInfo.setScreenWidth   ( mousePrint.getWidth () );
				mousePrintInfo.setScreenHeight  ( mousePrint.getHeight() );
				mousePrintInfo.setFileSize      ( mousePrint.getSize  () );
				mousePrintInfo.setSamplesCount  ( mousePrint.getCount () );
				mousePrintInfo.setSha1          ( mousePrint.getSha1  () );
				mousePrintInfo.setFileName      ( mousePrint.getFname () );
				
				mousePrintInfoList.add( mousePrintInfo );
			}
			
			return new RpcResult< EntityListResult< MousePrintInfo > >( new EntityListResult< MousePrintInfo >( mousePrintInfoList, JDOCursorHelper.getCursor( mousePrintList ).toWebSafeString() ) );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< MousePrintFullInfo > getMousePrintFullInfo( final String sharedAccount, final String sha1 ) {
		LOGGER.fine( ( sharedAccount == null ? "" : "Shared account: " + sharedAccount + ", " ) + "sha1: " + sha1 );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Key accountKey = ServerUtils.getAccountKey( pm, sharedAccount, user, Permission.VIEW_MOUSE_PRINTS );
			if ( accountKey == null )
				return RpcResult.createNoPermissionErrorResult();
			
			final List< Smpd > mousePrintList = new JQBuilder<>( pm, Smpd.class ).filter( "ownerk==p1 && sha1==p2", "KEY p1, String p2" ).get( accountKey, sha1 );
			if ( mousePrintList.isEmpty() )
				return RpcResult.createNoPermissionErrorResult();
			
			final Smpd mousePrint = mousePrintList.get( 0 );
			
			final MousePrintFullInfo mousePrintFullInfo = new MousePrintFullInfo();
			
			mousePrintFullInfo.setRecordingStart( mousePrint.getStart () );
			mousePrintFullInfo.setRecordingEnd  ( mousePrint.getEnd   () );
			mousePrintFullInfo.setScreenWidth   ( mousePrint.getWidth () );
			mousePrintFullInfo.setScreenHeight  ( mousePrint.getHeight() );
			mousePrintFullInfo.setFileSize      ( mousePrint.getSize  () );
			mousePrintFullInfo.setSamplesCount  ( mousePrint.getCount () );
			mousePrintFullInfo.setSha1          ( mousePrint.getSha1  () );
			mousePrintFullInfo.setFileName      ( mousePrint.getFname () );
			
			mousePrintFullInfo.setUploadedDate        ( mousePrint.getDate   () );
			mousePrintFullInfo.setSamplingTime        ( mousePrint.getTime   () );
			mousePrintFullInfo.setFileLastModified    ( mousePrint.getLastmod() );
			mousePrintFullInfo.setVersion             ( mousePrint.getVer    () );
			mousePrintFullInfo.setScreenResolution    ( mousePrint.getRes    () );
			mousePrintFullInfo.setUncompressedDataSize( mousePrint.getUdsize () );
			mousePrintFullInfo.setSavedWithCompression( mousePrint.getCompr  () );
			
			return new RpcResult< MousePrintFullInfo >( mousePrintFullInfo );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< EntityListResult< OtherFileInfo > > getOtherFileInfoList( final String sharedAccount, final PageInfo pageInfo, final OtherFileFilters filters ) {
		final String filtersString = filters.toString();
		LOGGER.fine( ( sharedAccount == null ? "" : "Shared account: " + sharedAccount + ", " ) + "pageInfo: {" + pageInfo.toString() + "}"
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
			
			final Key accountKey = ServerUtils.getAccountKey( pm, sharedAccount, user, Permission.VIEW_OTHER_FILES );
			if ( accountKey == null )
				return RpcResult.createNoPermissionErrorResult();
			
			final List< Object > paramsList     = new ArrayList< Object >();
			final StringBuilder  filtersBuilder = new StringBuilder( "ownerk==p1" );
			final StringBuilder  paramsBuilder  = new StringBuilder( "KEY p1" );
			
			paramsList.add( accountKey );
			if ( filters.getExtension() != null ) {
				paramsList.add( filters.getExtension() );
				filtersBuilder.append( " && ext==p" ).append( paramsList.size() );
				paramsBuilder.append( ", String p" ).append( paramsList.size() );
			}
			if ( filters.getFromDate() != null ) {
				paramsList.add( filters.getFromDate() );
				filtersBuilder.append( " && date>p" ).append( paramsList.size() );
				paramsBuilder.append( ", DATE p" ).append( paramsList.size() );
			}
			if ( filters.getToDate() != null ) {
				filters.getToDate().setTime( filters.getToDate().getTime() + 24L*60*60*1000 ); // Add one day so replays from this day will be included
				paramsList.add( filters.getToDate() );
				filtersBuilder.append( " && date<p" ).append( paramsList.size() );
				paramsBuilder.append( ", DATE p" ).append( paramsList.size() );
			}
			final List< OtherFile > otherFileList = new JQBuilder<>( pm, OtherFile.class ).filter( filtersBuilder.toString(), paramsBuilder.toString() ).desc( "date" ).pageInfo( pageInfo ).get( paramsList.toArray() );
			
			final List< OtherFileInfo > otherFileInfoList = new ArrayList< OtherFileInfo >( otherFileList.size() );
			for ( final OtherFile otherFile : otherFileList ) {
				final OtherFileInfo otherFileInfo = new OtherFileInfo();
				
				otherFileInfo.setUploaded    ( otherFile.getDate   () );
				otherFileInfo.setFileName    ( otherFile.getFname  () );
				otherFileInfo.setSize        ( otherFile.getSize   () );
				otherFileInfo.setLastModified( otherFile.getLastmod() );
				otherFileInfo.setComment     ( otherFile.getComment() );
				otherFileInfo.setSha1        ( otherFile.getSha1   () );
				
				otherFileInfoList.add( otherFileInfo );
			}
			
			return new RpcResult< EntityListResult< OtherFileInfo > >( new EntityListResult< OtherFileInfo >( otherFileInfoList, JDOCursorHelper.getCursor( otherFileList ).toWebSafeString() ) );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< FileStatInfo > getFileStatInfo( final String sharedAccount ) {
		LOGGER.fine( sharedAccount == null ? "" : "Shared account: " + sharedAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Account account = ServerUtils.getAccount( pm, sharedAccount, user );
			if ( account == null )
				return RpcResult.createNoPermissionErrorResult();
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.VIEW_QUOTA ) )
				return RpcResult.createNoPermissionErrorResult();
			
			final FileStatInfo fileStatInfo = new FileStatInfo();
			
			// File stats
			final List< FileStat > fileStatList = new JQBuilder<>( pm, FileStat.class ).filter( "ownerKey==p1", "KEY p1" ).get( account.getKey() );
			final FileStat fileStat = fileStatList.isEmpty() ? null : fileStatList.get( 0 );
			if ( fileStat != null ) {
				fileStatInfo.setHasFileStat ( true                       );
				fileStatInfo.setAllCount    ( fileStat.getCount       () );
				fileStatInfo.setAllStorage  ( fileStat.getStorage     () );
				fileStatInfo.setRepCount    ( fileStat.getRepCount    () );
				fileStatInfo.setRepStorage  ( fileStat.getRepStorage  () );
				fileStatInfo.setSmpdCount   ( fileStat.getSmpdCount   () );
				fileStatInfo.setSmpdStorage ( fileStat.getSmpdStorage () );
				fileStatInfo.setOtherCount  ( fileStat.getOtherCount  () );
				fileStatInfo.setOtherStorage( fileStat.getOtherStorage() );
				fileStatInfo.setRecalced    ( fileStat.getRecalced    () );
			}
			
			fileStatInfo.setPaidStorage( account.getPaidStorage() );
			final DbPackage dbPackage = DbPackage.getFromStorage( account.getPaidStorage() );
			if ( dbPackage != null ) {
				fileStatInfo.setDbPackageName( dbPackage.name );
				fileStatInfo.setDbPackageIcon( ServerUtils.getDbPackageIcon( dbPackage, account.getPaidStorage(), fileStatInfo.getAllStorage() ) );
			}
			
			return new RpcResult< FileStatInfo >( fileStatInfo );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< Void > recalcFileStats( final String sharedAccount ) {
		LOGGER.fine( sharedAccount == null ? "" : "Shared account: " + sharedAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Key accountKey = ServerUtils.getAccountKey( pm, sharedAccount, user, Permission.VIEW_QUOTA );
			if ( accountKey == null )
				return RpcResult.createNoPermissionErrorResult();
			
			final Event lastRecalcEvent = ServerUtils.getLastEvent( accountKey, Type.FILE_STATS_RECALC_TRIGGERED );
			if ( lastRecalcEvent != null && System.currentTimeMillis() - lastRecalcEvent.getDate().getTime() < 24L*60*60*1000 ) {
				final int hours = (int) ( ( System.currentTimeMillis() - lastRecalcEvent.getDate().getTime() ) / (1000L*60*60) );
				return RpcResult.createErrorResult( "Recalculation can only be requested once per 24 hours"
					+ " (last was " + hours + ( hours == 1 ? " hour ago)!" : " hours ago)!" ) );
			}
			
			TaskServlet.register_recalcFileStatsTask( accountKey );
			
			pm.makePersistent( new Event( accountKey, Type.FILE_STATS_RECALC_TRIGGERED ) );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
		
		return RpcResult.createInfoResult( "File stats recalculation has been kicked-off, check back in a short time..." );
	}
	
	@Override
	public RpcResult< PaymentsInfo > getPaymentsInfo( String sharedAccount ) {
		LOGGER.fine( sharedAccount == null ? "" : "Shared account: " + sharedAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Account account = ServerUtils.getAccount( pm, sharedAccount, user );
			if ( account == null )
				return RpcResult.createNoPermissionErrorResult();
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.VIEW_PAYMENTS ) )
				return RpcResult.createNoPermissionErrorResult();
			
			final PaymentsInfo paymentsInfo = new PaymentsInfo();
			
			// Payments
			final List< Payment > paymentList = new JQBuilder<>( pm, Payment.class ).filter( "accountKey==p1", "KEY p1" ).get( account.getKey() );
			final List< PaymentInfo > paymentInfoList = new ArrayList< PaymentInfo >( paymentList.size() );
			for ( final Payment payment : paymentList ) {
    			final PaymentInfo paymentInfo = new PaymentInfo();
    			paymentInfo.setDate         ( payment.getDate() );
    			paymentInfo.setPaymentSender( payment.getPaypalAccount() == null || payment.getPaypalAccount().isEmpty() ? payment.getBankAccount() : payment.getPaypalAccount() );
    			paymentInfo.setPayment      ( payment.getVirtualPayment() );
    			paymentInfoList.add( paymentInfo );
			}
			// Sort payments by date
			Collections.sort( paymentInfoList, new Comparator< PaymentInfo >() {
				@Override
                public int compare( final PaymentInfo p1, final PaymentInfo p2 ) {
	                return p2.getDate().compareTo( p1.getDate() );
                }
			} );
			paymentsInfo.setPaymentInfoList( paymentInfoList );
			
			// DB package info list
			final List< DbPackageInfo > dbPackageInfoList = new ArrayList< DbPackageInfo >();
			int idx = 0;
			for ( final DbPackage dbPackage : DbPackage.values() ) {
				if ( dbPackage.buyable || dbPackage.storage == account.getPaidStorage() ) {
        			final DbPackageInfo dbPackageInfo = new DbPackageInfo();
        			dbPackageInfo.setDbPackageIcon( ServerUtils.getDbPackageIcon( dbPackage, 0, 0 ) );
        			dbPackageInfo.setDbPackageName( dbPackage.name );
        			dbPackageInfo.setPriceUSD     ( dbPackage.priceUSD );
        			dbPackageInfo.setStorage      ( dbPackage.storage );
        			dbPackageInfo.setNumOfReplays ( (int) ( dbPackage.storage / 50000 ) ); // Estimate avg replay size with 50,000 bytes
        			dbPackageInfoList.add( dbPackageInfo );
        			if ( dbPackage.storage == account.getPaidStorage() )
        				paymentsInfo.setCurrentDbPackageInfoIdx( idx );
        			if ( dbPackage.storage > account.getPaidStorage() && paymentsInfo.getFirstBuyableDbPackageInfoIdx() == 0 )
        				paymentsInfo.setFirstBuyableDbPackageInfoIdx( idx );
        			else
        				idx++;
				}
			}
			paymentsInfo.setDbPackageInfoList( dbPackageInfoList );
			
			return new RpcResult< PaymentsInfo >( paymentsInfo );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< String > getAuthorizationKey( final String sharedAccount ) {
		LOGGER.fine( sharedAccount == null ? "" : "Shared account: " + sharedAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Account account = ServerUtils.getAccount( pm, sharedAccount, user );
			
			if ( account == null )
				return RpcResult.createNoPermissionErrorResult();
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.VIEW_AUTHORIZATION_KEY ) )
				return RpcResult.createNoPermissionErrorResult();
			
			return new RpcResult< String >( account.getAuthorizationKey() );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< String > generateNewAuthorizationKey( final String sharedAccount ) {
		LOGGER.fine( sharedAccount == null ? "" : "Shared account: " + sharedAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Account account = ServerUtils.getAccount( pm, sharedAccount, user );
			
			if ( account == null )
				return RpcResult.createNoPermissionErrorResult();
			
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.CHANGE_AUTHORIZATION_KEY ) )
				return RpcResult.createNoPermissionErrorResult();
			
			CachingService.removeAuthorizationKeyAccountKey( account.getAuthorizationKey() );
			final String newAuthorizationKey = ServerUtils.generateRandomStringKey();
			
			// Check if new authorization key is unique:
			final List< Account > accountList2 = new JQBuilder<>( pm, Account.class ).filter( "authorizationKey==p1", "String p1" ).get( newAuthorizationKey );
			if ( !accountList2.isEmpty() )
				return RpcResult.createErrorResult( "Error generating new key!" ); // This authorization key is in use!!!
			
			pm.makePersistent( new Event( account.getKey(), Type.CHANGE_AUTHORIZATION_KEY, "Old key: " + account.getAuthorizationKey() ) );
			
			account.setAuthorizationKey( newAuthorizationKey );
			
			return new RpcResult< String >( newAuthorizationKey );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< Integer > deleteFileList( final String sharedAccount, final String fileTypeString, final List< String > sha1List ) {
		LOGGER.fine( ( sharedAccount == null ? "" : "Shared account: " + sharedAccount + ", " ) + "File type: " + fileTypeString + ", SHA-1 list size: " + sha1List.size() );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		int deletedCount = 0;
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final FileType fileType = FileType.fromString( fileTypeString );
			
			final Permission requiredPermission;
			switch ( fileType ) {
			case SC2REPLAY   : requiredPermission = Permission.DELETE_REPLAYS     ; break;
			case MOUSE_PRINT : requiredPermission = Permission.DELETE_MOUSE_PRINTS; break;
			case OTHER       : requiredPermission = Permission.DELETE_OTHER_FILES ; break;
			default          : return RpcResult.createErrorResult( "Invalid file type!" );
			}
			
			final Key accountKey = ServerUtils.getAccountKey( pm, sharedAccount, user, requiredPermission );
			if ( accountKey == null )
				return RpcResult.createNoPermissionErrorResult();
			
			// TODO defer the rest to the task queue, that way page size of 200 can be brought back again!
			// We're also getting frequent internal errors, because even deleting 100 files does not fit into the 1 min deadline!
			// TODO consider adding each file separately: 1 task for each SHA1!
			
			final JQBuilder< ? extends FileMetaData > q = new JQBuilder<>( pm, FILE_TYPE_CLASS_MAP.get( fileType ) ).filter( "ownerk==p1 && sha1==p2", "KEY p1, String p2" );
			
			for ( final String sha1 : sha1List ) {
				// 1. Load entity (based on fileType)
				final List< ? extends FileMetaData > fileMetaDataList = q.get( accountKey, sha1 );
				if ( fileMetaDataList.isEmpty() ) // File has already been deleted?
					continue;
				
				final FileMetaData fileMetaData = fileMetaDataList.get( 0 );
				final long size = fileMetaData.getSize();
				
				// 2. Delete file (content)
				if ( fileMetaData.getBlobKey() != null )
					BlobstoreServiceFactory.getBlobstoreService().delete( fileMetaData.getBlobKey() );
				
				// 3. Delete entity
				pm.deletePersistent( fileMetaData );
				
				deletedCount++;
				
				// Schedule file stats update
				TaskServlet.register_updateFileDelStatsTask( accountKey, size, fileTypeString );
			}
			
		} finally {
			if ( pm != null )
				pm.close();
		}
		
		return new RpcResult< Integer >( deletedCount );
	}
	
	@Override
	public RpcResult< SettingsInfo > getSettings( final String sharedAccount ) {
		LOGGER.fine( sharedAccount == null ? "" : "Shared account: " + sharedAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Account account = ServerUtils.getAccount( pm, sharedAccount, user );
			
			if ( account == null )
				return RpcResult.createNoPermissionErrorResult();
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.VIEW_SETTINGS ) )
				return RpcResult.createNoPermissionErrorResult();
			
			final SettingsInfo settingsInfo = new SettingsInfo();
			
			settingsInfo.setGoogleAccount         ( account.getUser().getEmail       () );
			settingsInfo.setContactEmail          ( account.getContactEmail          () );
			settingsInfo.setUserName              ( account.getName                  () );
			settingsInfo.setNotificationQuotaLevel( account.getNotificationQuotaLevel() );
			settingsInfo.setConvertToRealTime     ( account.isConvertToRealTime      () );
			settingsInfo.setMapImageSize          ( account.getMapImageSize          () );
			settingsInfo.setDisplayWinners        ( account.getDisplayWinners        () );
			settingsInfo.setFavoredPlayerList     ( ServerUtils.cloneList( account.getFavoredPlayers() ) );
			
			if ( account.getGrantedUsers() == null )
				settingsInfo.setGrantedUsers( new ArrayList< String >() );
			else {
				final List< String > grantedUsers = new ArrayList< String >( account.getGrantedUsers().size() );
				for ( final User grantedUser : account.getGrantedUsers() )
					grantedUsers.add( grantedUser.getEmail() );
				settingsInfo.setGrantedUsers( grantedUsers );
			}
			if ( account.getGrantedPermissions() == null )
				settingsInfo.setGrantedPermissions( new ArrayList< Long >() );
			else
				settingsInfo.setGrantedPermissions( ServerUtils.cloneList( account.getGrantedPermissions() ) );
			
			return new RpcResult< SettingsInfo >( settingsInfo );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
	}
	
	@Override
	public RpcResult< Void > saveSettings( final String sharedAccount, final SettingsInfo settingsInfo ) {
		LOGGER.fine( sharedAccount == null ? "" : "Shared account: " + sharedAccount );
		
		final UserService userService = UserServiceFactory.getUserService();
		final User user = userService.getCurrentUser();
		if ( user == null )
			return RpcResult.createNotLoggedInErrorResult();
		
		PersistenceManager pm = null;
		try {
			
			pm = PMF.get().getPersistenceManager();
			
			final Account account = ServerUtils.getAccount( pm, sharedAccount, user );
			
			if ( account == null )
				return RpcResult.createNoPermissionErrorResult();
			if ( sharedAccount != null && !account.isPermissionGranted( user, Permission.CHANGE_SETTINGS ) )
				return RpcResult.createNoPermissionErrorResult();
			
			if ( settingsInfo.getContactEmail() != null && !settingsInfo.getContactEmail().isEmpty() )
				if ( !ServerUtils.isEmailValid( settingsInfo.getContactEmail() ) )
					return RpcResult.createErrorResult( "Invalid contact email!" );
			
			if ( settingsInfo.getNotificationQuotaLevel() < 50 || settingsInfo.getNotificationQuotaLevel() > 100 )
				return RpcResult.createErrorResult( "Invalid notification quota level! (Must be between 50 and 100!)" );
			
			account.setContactEmail          ( settingsInfo.getContactEmail() == null || settingsInfo.getContactEmail().isEmpty() ? null : ServletApi.trimStringLength( settingsInfo.getContactEmail(), 500 ) );
			account.setName                  ( settingsInfo.getUserName    () == null || settingsInfo.getUserName    ().isEmpty() ? null : ServletApi.trimStringLength( settingsInfo.getUserName    (), 500 ) );
			account.setNotificationQuotaLevel( settingsInfo.getNotificationQuotaLevel() );
			account.setConvertToRealTime     ( settingsInfo.isConvertToRealTime      () );
			account.setMapImageSize          ( settingsInfo.getMapImageSize          () );
			account.setDisplayWinners        ( settingsInfo.getDisplayWinners        () );
			account.setFavoredPlayers        ( settingsInfo.getFavoredPlayerList     () );
			
			if ( settingsInfo.getGrantedUsers().size() != settingsInfo.getGrantedPermissions().size() )
				return RpcResult.createErrorResult( "Bad request!" );
			
			if ( settingsInfo.getGrantedUsers().size() > 100 )
				return RpcResult.createErrorResult( "Maximum 100 granted users allowed!" );
			
			// Sort granted users by their Google account
			final List< Object[] > grantedPairs = new ArrayList< Object[] >( settingsInfo.getGrantedUsers().size() );
			final Set< String > grantedUserSet = new HashSet< String >( settingsInfo.getGrantedUsers().size() );
			for ( int i = 0; i < settingsInfo.getGrantedUsers().size(); i++ ) {
				final String grantedGoogleAccount = settingsInfo.getGrantedUsers().get( i );
				// Do not allow duplicates:
				if ( !grantedUserSet.add( grantedGoogleAccount ) )
					return RpcResult.createErrorResult( "Duplicate granted Google account: " + grantedGoogleAccount );
				grantedPairs.add( new Object[] { grantedGoogleAccount, settingsInfo.getGrantedPermissions().get( i ) } );
			}
			Collections.sort( grantedPairs, new Comparator< Object[] >() {
				@Override
                public int compare( final Object[] p1, final Object[] p2 ) {
	                return String.CASE_INSENSITIVE_ORDER.compare( (String) p1[ 0 ], (String) p2[ 0 ] );
                }
			} );
			
			final List< User > grantedUsers       = new ArrayList< User >( grantedPairs.size() );
			final List< Long > grantedPermissions = new ArrayList< Long >( grantedPairs.size() );
			for ( final Object[] grantedPair : grantedPairs ) {
				if ( !ServerUtils.isEmailValid( (String) grantedPair[ 0 ] ) )
					return RpcResult.createErrorResult( "Invalid Google account: " + (String) grantedPair[ 0 ] );
				grantedUsers.add( new User( (String) grantedPair[ 0 ], "gmail.com" ) );
				grantedPermissions.add( (Long) grantedPair[ 1 ] );
			}
			account.setGrantedUsers      ( grantedUsers       );
			account.setGrantedPermissions( grantedPermissions );
			
		} finally {
			if ( pm != null )
				pm.close();
		}
		
		return RpcResult.createInfoResult( "Settings saved successfully." );
	}
	
}
