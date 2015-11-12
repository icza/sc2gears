/*
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

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;

/**
 * Utility class to batch update multiple entities.
 * 
 * @author Andras Belicza
 */
public class EntityBatchUpdater {
	
	/**
	 * Entity processor: defines a custom job on a single {@link Entity}.
	 * 
	 * @author Andras Belicza
	 */
	public static interface EntityProcessor {
		/**
		 * Processes the entity.
		 * @param e entity to be processed
		 */
		void processEntity( Entity e );
	}
	
	/** Entity query. */
	private Query   query;
	
	/** Maximum number of entities to update.    */
	private int     maxUpdates;
	/** Number of entities to query in one step. */
	private Integer batchSize;
	
	/** Tells if entities must be saved automatically after processed. */
	private boolean autoSave = true;;
	
	/** Optional new version value to set to the <code>v</code> property. */
	private Integer newVersionToSet;
	
	/** Optional array of properties to make unindexed. */
	private String[] makePropertiesUnindexed;
	
	/** Entity processor performing custom task on individual entities. */
	private EntityProcessor entityProcessor;
	
	/** Number of processed entities. */
	private int processedCount;
	
	
	/**
	 * Creates a new EntityBatchUpdater.
	 * @param entityName      entity name to update
	 * @param entityProcessor entity processor to perform custom task on individual entities 
	 */
	public EntityBatchUpdater( final Query query ) {
		this( query, null );
	}
	
	/**
	 * Creates a new EntityBatchUpdater.
	 * @param entityName      entity name to update
	 * @param entityProcessor optional entity processor to perform custom task on individual entities 
	 */
	public EntityBatchUpdater( final Query query, final EntityProcessor entityProcessor ) {
		this.query           = query;
		this.entityProcessor = entityProcessor;
	}
	
	/**
	 * Sets the maximum number of entities to update.
	 * @param maxUpdates max number of entities to update
	 */
	public void setMaxUpdates( final int maxUpdates ) {
		this.maxUpdates = maxUpdates;
	}
	
	/**
	 * Returns the maximum number of entities to update.
	 * @return the maximum number of entities to update
	 */
	public int getMaxUpdates() {
		return maxUpdates;
	}
	
	/**
	 * Sets the number of entities to query in one step.
	 * @param batchSize the number of entities to query in one step
	 */
	public void setBatchSize( final Integer batchSize ) {
		this.batchSize = batchSize;
	}
	
	/**
	 * Returns the number of entities to query in one step.
	 * Returns <code>null</code> if it has not been set manually.
	 * @return the number of entities to query in one step
	 */
	public Integer getBatchSize() {
		return batchSize;
	}
	
	/**
	 * Sets the auto-save property.
	 * @param autoSave auto save value to be set
	 */
	public void setAutoSave( final boolean autoSave ) {
		this.autoSave = autoSave;
	}
	
	/**
	 * Returns the auto-save property which tells if entities must be saved automatically after processed.
	 * @return true if entities must be saved automatically after processed; false otherwise
	 */
	public boolean isAutoSave() {
		return autoSave;
	}
	
	/**
	 * Sets the new version value to be set to the <code>v</code> property.<br>
	 * The default <code>null</code> value means not to change the <code>v</code> property.
	 * @param newVersionToSet new version value to set
	 */
	public void setNewVersionToSet( final Integer newVersionToSet ) {
		this.newVersionToSet = newVersionToSet;
	}
	
	/**
	 * Returns the new version value to be set to the <code>v</code> property.
	 * @return the new version value to be set to the <code>v</code> property
	 */
	public Integer getNewVersionToSet() {
		return newVersionToSet;
	}
	
	/**
	 * Sets the array of properties to make unindexed.<br>
	 * Properties are made unindexed by setting their actual values ({@link Entity#getProperty(String)})
	 * with ({@link Entity#setUnindexedProperty(String, Object)}).<br>
	 * The default <code>null</code> value means not to make any properties unindexed.
	 * @param makePropertiesUnindexed properties to make unindexed 
	 */
	public void setMakePropertiesUnindexed( final String... makePropertiesUnindexed ) {
		this.makePropertiesUnindexed = makePropertiesUnindexed;
	}
	
	/**
	 * Returns the array of properties to make unindexed.
	 * @return the array of properties to make unindexed
	 */
	public String[] getMakePropertiesUnindexed() {
		return makePropertiesUnindexed;
	}
	
	/**
	 * Processes the entities meeting the criteria defined by the query filter.
	 * @return the string representation of the updated count
	 */
	public String processEntities() {
		if ( maxUpdates <= 0 )
			return getProcessedCountString();
		
		if ( batchSize == null )
			batchSize = 500;
		if ( maxUpdates < batchSize )
			batchSize = maxUpdates;
		
		final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		
		final FetchOptions fetchOptions = FetchOptions.Builder.withLimit( batchSize );
		
		Cursor cursor = null;
		while ( true ) {
			if ( cursor != null )
				fetchOptions.startCursor( cursor );
			
			final QueryResultList< Entity > resultList = ds.prepare( query ).asQueryResultList( fetchOptions );
			
			for ( final Entity e : resultList ) {
				if ( entityProcessor != null )
					entityProcessor.processEntity( e );
				
				if ( makePropertiesUnindexed != null )
					for ( final String propertyName : makePropertiesUnindexed )
						e.setUnindexedProperty( propertyName, e.getProperty( propertyName ) );
				
				if ( newVersionToSet != null )
					e.setProperty( "v", newVersionToSet );
				
				if ( autoSave )
					ds.put( e );
				
				processedCount++;
			}
			
			if ( resultList.size() < batchSize || processedCount >= maxUpdates )
				return getProcessedCountString();
			
			cursor = resultList.getCursor();
		}
	}
	
	/**
	 * Returns the number of updated entities.
	 * @return the number of updated entities
	 */
	public int getProcessedCount() {
		return processedCount;
	}
	
	/**
	 * Returns the string representation of the updated count.
	 * @return the string representation of the updated count
	 */
	public String getProcessedCountString() {
		return "Processed " + query.getKind() + " count: " + processedCount;
	}
	
}
