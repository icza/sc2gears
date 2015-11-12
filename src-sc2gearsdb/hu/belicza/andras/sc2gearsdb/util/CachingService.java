/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.util;

import hu.belicza.andras.sc2gearsdb.TopScoresServlet;
import hu.belicza.andras.sc2gearsdb.beans.DownloadStatInfo;
import hu.belicza.andras.sc2gearsdb.beans.MousePracticeGameScoreInfo;
import hu.belicza.andras.sc2gearsdb.datastore.Account;
import hu.belicza.andras.sc2gearsdb.datastore.MousePracticeGameScore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceException;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;

/**
 * Implements datastore caching services based on AppEngine's Memcache service.
 * 
 * @author Andras Belicza
 */
public class CachingService {
	
	private static final Logger LOGGER = Logger.getLogger( CachingService.class.getName() );
	
	/** Key prefix of the user => Account key mapping cache.              */
	private static final String CACHE_KEY_USER_ACCOUNT_KEY_PREFIX     = "user:";
	/** Key prefix of the authorization key => Account key mapping cache. */
	private static final String CACHE_KEY_AUTH_KEY_ACCOUNT_KEY_PREFIX = "authKey:";
	
	/** Key of the Mouse Practice Game Top scores in the memcache.        */
	private static final String CACHE_KEY_MPG_TOP_SCORES              = "mousePracticeGameTopScores";
	
	/** Key of the file name => download stat mapping cache.              */
	private static final String CACHE_KEY_FILE_DOWNLOAD_STAT_PREFIX   = "file:";
	
	/** A single memcache service instance. */
	private static final MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
	
	/**
	 * Returns the Account key associated with the specified user.
	 * @param pm   reference to the persistence manager
	 * @param user user to return the account key for
	 * @return the Account key associated with the specified user; or <code>null</code> if no account is associated with the specified user
	 */
	public static Key getAccountKeyByUser( final PersistenceManager pm, final User user ) {
		final String memcacheKey = CACHE_KEY_USER_ACCOUNT_KEY_PREFIX + user.getEmail();
		final String accountKeyString = (String) memcacheService.get( memcacheKey );
		if ( accountKeyString != null )
			return KeyFactory.stringToKey( accountKeyString );
		
		final Query q = new Query( Account.class.getSimpleName() );
		q.setFilter( new FilterPredicate( "user", FilterOperator.EQUAL, user ) );
		q.setKeysOnly();
		final List< Entity > entityList = DatastoreServiceFactory.getDatastoreService().prepare( q ).asList( FetchOptions.Builder.withDefaults() );
		if ( entityList.isEmpty() )
			return null;
		
		final Key accountKey = entityList.get( 0 ).getKey();
		try {
			memcacheService.put( memcacheKey, KeyFactory.keyToString( accountKey ) );
		}
		catch ( final MemcacheServiceException mse ) {
			LOGGER.log( Level.WARNING, "Failed to put key to memcache: " + memcacheKey, mse );
			// Ignore memcache errors, do not prevent serving user request
		}
		
		return accountKey;
	}
	
    /**
     * Removes the user => account key mapping for the user specified by his/her email.
     * @param email user email whose mapping to remove
     * @return true if user was in the cache and was removed; false otherwise
     */
	public static boolean removeUserAccountKey( final String email ) {
    	return memcacheService.delete( CACHE_KEY_USER_ACCOUNT_KEY_PREFIX + email );
	}
	
	/**
	 * Returns the Account key associated with the specified authorization key.
	 * @param pm               reference to the persistence manager
	 * @param authorizationKey authorization key to return the account key for
	 * @return the Account key associated with the specified authorization key; or <code>null</code> if the authorization key is invalid
	 */
	public static Key getAccountKeyByAuthKey( final PersistenceManager pm, final String authorizationKey ) {
		final String memcacheKey = CACHE_KEY_AUTH_KEY_ACCOUNT_KEY_PREFIX + authorizationKey;
		final String accountKeyString = (String) memcacheService.get( memcacheKey );
		if ( accountKeyString != null )
			return KeyFactory.stringToKey( accountKeyString );
		
		final Query q = new Query( Account.class.getSimpleName() );
		q.setFilter( new FilterPredicate( "authorizationKey", FilterOperator.EQUAL, authorizationKey ) );
		q.setKeysOnly();
		final List< Entity > entityList = DatastoreServiceFactory.getDatastoreService().prepare( q ).asList( FetchOptions.Builder.withDefaults() );
		if ( entityList.isEmpty() )
			return null;
		
		final Key accountKey = entityList.get( 0 ).getKey();
		try {
			memcacheService.put( memcacheKey, KeyFactory.keyToString( accountKey ) );
		}
		catch ( final MemcacheServiceException mse ) {
			LOGGER.log( Level.WARNING, "Failed to put key to memcache: " + memcacheKey, mse );
			// Ignore memcache errors, do not prevent serving user request
		}
		
		return accountKey;
	}
	
    /**
     * Removes the authorization key => account key mapping for the specified authorizaion key.
     * @param authorizationKey authorization key whose mapping to remove
     */
	public static void removeAuthorizationKeyAccountKey( final String authorizationKey ) {
    	memcacheService.delete( CACHE_KEY_AUTH_KEY_ACCOUNT_KEY_PREFIX + authorizationKey );
	}
	
	/**
	 * Returns the Mouse Practice Game Top scores.
	 * @return the Mouse Practice Game Top scores
	 */
    public static List< MousePracticeGameScoreInfo > getMousePracticeTopScores() {
    	@SuppressWarnings( "unchecked" )
		List< MousePracticeGameScoreInfo > scoreList = (List< MousePracticeGameScoreInfo >) memcacheService.get( CACHE_KEY_MPG_TOP_SCORES );
		
		if ( scoreList == null ) {
			PersistenceManager pm = null;
			try {
				pm = PMF.get().getPersistenceManager();
				
				final List< MousePracticeGameScore > mousePracticeGameScoreList = new JQBuilder<>( pm, MousePracticeGameScore.class )
						.desc( "score" ).range( 0, TopScoresServlet.TOP_SCORE_TABLE_SIZE ).get();
				
				scoreList = new ArrayList< MousePracticeGameScoreInfo >( mousePracticeGameScoreList.size() );
				final Map< String, Integer > userNamePersonRankMap = new HashMap< String, Integer >();
				int personRankCounter = 0;
				
				for ( final MousePracticeGameScore mousePracticeGameScore : mousePracticeGameScoreList ) {
					final MousePracticeGameScoreInfo score = new MousePracticeGameScoreInfo();
					score.setUserName  ( mousePracticeGameScore.getUserName  () );
					score.setScore     ( mousePracticeGameScore.getScore     () );
					score.setAccuracy  ( mousePracticeGameScore.getAccuracy  () );
					score.setHits      ( mousePracticeGameScore.getHits      () );
					score.setRandomSeed( mousePracticeGameScore.getRandomSeed() );
					
					Integer personRank = userNamePersonRankMap.get( mousePracticeGameScore.getUserName() );
					if ( personRank == null )
						userNamePersonRankMap.put( mousePracticeGameScore.getUserName(), personRank = ++personRankCounter );
					score.setPersonRank( personRank );
					
					scoreList.add( score );
				}
			} finally {
				if ( pm != null )
					pm.close();
			}
			memcacheService.put( CACHE_KEY_MPG_TOP_SCORES, scoreList );
		}
		
		return scoreList;
	}
	
    /**
     * Removes the Mouse Practice Game Top Scores.
     */
    public static void removeMousePracticeTopScores() {
    	memcacheService.delete( CACHE_KEY_MPG_TOP_SCORES );
    }
    
	/**
	 * Returns the download stat info for the specified file.
	 * @param fileName file name to return download stat info for
	 * @return the download stat info for the file name
	 */
	public static DownloadStatInfo getDownloadStatByFile( final String fileName ) {
		return (DownloadStatInfo) memcacheService.get( CACHE_KEY_FILE_DOWNLOAD_STAT_PREFIX + fileName );
	}
	
	/**
	 * Caches the download stat info for the specified file.
	 * @param fileName         file name to cache the download stat info for
	 * @param downloadStatInfo download stat info to cache
	 */
	public static void putFileDownloadStat( final String fileName, final DownloadStatInfo downloadStatInfo ) {
		memcacheService.put( CACHE_KEY_FILE_DOWNLOAD_STAT_PREFIX + fileName, downloadStatInfo );
	}
	
}
