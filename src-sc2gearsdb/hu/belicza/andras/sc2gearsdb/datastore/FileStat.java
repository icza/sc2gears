/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.datastore;

import java.util.Date;

import hu.belicza.andras.sc2gearsdbapi.FileServletApi.FileType;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Key;

/**
 * A class that stores file statistics for an account.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>"v"</code> property.
 * 			<br>Added <code>recalced</code> property.
 * 		<li>Made {@link #repCount}, {@link #repStorage}, {@link #repInbw}, {@link #repOutbw}, 
 * 			{@link #smpdCount}, {@link #smpdStorage}, {@link #smpdInbw}, {@link #smpdOutbw},
 * 			{@link #otherCount}, {@link #otherStorage}, {@link #otherInbw}, {@link #otherOutbw} properties unindexed.
 * 		<li>Made {@link #count}, {@link #inbw}, {@link #outbw} properties unindexed.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class FileStat extends DataStoreObject {
	
	/** Key of the owner account. */
	@Persistent
	private Key ownerKey;
	
	/** Number of files of all file types. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int count;
	
	/** Total storage of all file types (bytes). */
	@Persistent
	private long storage;
	
	/** Total incoming bandwidth used by all file types (bytes). */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long inbw;
	
	/** Total outgoing bandwidth used by all file types (bytes). */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long outbw;
	
	
	/** Number of rep files. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int repCount;
	
	/** Total storage of rep files (bytes). */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long repStorage;
	
	/** Total incoming bandwidth used by rep files (bytes). */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long repInbw;
	
	/** Total outgoing bandwidth used by rep files (bytes). */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long repOutbw;
	
	
	/** Number of smpd files. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int smpdCount;
	
	/** Total storage of smpd files (bytes). */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long smpdStorage;
	
	/** Total incoming bandwidth used by smpd files (bytes). */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long smpdInbw;
	
	/** Total outgoing bandwidth used by smpd files (bytes). */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long smpdOutbw;
	
	
	/** Number of other files. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int otherCount;
	
	/** Total storage of other files (bytes). */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long otherStorage;
	
	/** Total incoming bandwidth used other files (bytes). */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long otherInbw;
	
	/** Total outgoing bandwidth used by other files (bytes). */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private long otherOutbw;
	
	/** Last updated time.<br>
	 * This property was added at and initialized with '2012-06-25 09:57:47.590000 UTC'. */
	@Persistent
    private Date updated;
	
	/** Last recalculated time. */
	// No need to make this property unindexed as this property only changes when the user (or an admin) triggers a recalculation which is very rare!
	@Persistent
	private Date recalced;
	
	/**
	 * Creates a new FileStat.
	 * @param ownerKey key of the owner account
	 */
	public FileStat( final Key ownerKey ) {
		this.ownerKey = ownerKey;
		setV( 3 );
		update();
	}
	
	public void setOwnerKey(Key ownerKey) {
		this.ownerKey = ownerKey;
	}
	
	public Key getOwnerKey() {
		return ownerKey;
	}
	
	/**
	 * Updates the <code>updated</code> property.
	 */
	public void update() {
		updated = new Date();
	}
	
	/**
	 * Registers a new file.
	 * @param fileType type of file to register
	 * @param size     size of the file
	 */
	public void registerFile( final FileType fileType, final long size ) {
		count++;
		storage += size;
		
		switch ( fileType ) {
		case SC2REPLAY :
			repCount++;
			repStorage += size;
			break;
		case MOUSE_PRINT :
			smpdCount++;
			smpdStorage += size;
			break;
		case OTHER :
			otherCount++;
			otherStorage += size;
			break;
		}
		
		update();
	}
	
	/**
	 * Deregisters a file.
	 * @param fileType type of file to deregister
	 * @param size     size of the file
	 */
	public void deregisterFile( final FileType fileType, final long size ) {
		count--;
		storage -= size;
		
		switch ( fileType ) {
		case SC2REPLAY :
			repCount--;
			repStorage -= size;
			break;
		case MOUSE_PRINT :
			smpdCount--;
			smpdStorage -= size;
			break;
		case OTHER :
			otherCount--;
			otherStorage -= size;
			break;
		}
		
		update();
	}
	
	/**
	 * Increases the incoming bandwidth for the specified file type with the specified amount.
	 * @param fileType file type whose incoming bandwidth to increase
	 * @param amount   amount to increase the incoming bandwidth with
	 */
	public void increaseInbw( final FileType fileType, final long amount ) {
		inbw += amount;
		
		switch ( fileType ) {
		case SC2REPLAY :
			repInbw += amount;
			break;
		case MOUSE_PRINT :
			smpdInbw += amount;
			break;
		case OTHER :
			otherInbw += amount;
			break;
		}
		
		update();
	}
	
	/**
	 * Increases the outgoing bandwidth for the specified file type with the specified amount.
	 * @param fileType file type whose outgoing bandwidth to increase
	 * @param amount   amount to increase the outgoing bandwidth with
	 */
	public void increaseOutbw( final FileType fileType, final long amount ) {
		outbw += amount;
		
		switch ( fileType ) {
		case SC2REPLAY :
			repOutbw += amount;
			break;
		case MOUSE_PRINT :
			smpdOutbw += amount;
			break;
		case OTHER :
			otherOutbw += amount;
			break;
		}
		
		update();
	}
	
	/**
	 * Returns the count of the specified file type.
	 * @return the count of the specified file type
	 */
	public int getCount( final FileType fileType ) {
		switch ( fileType ) {
		case ALL         :
			return count;
		case SC2REPLAY   :
			return repCount;
		case MOUSE_PRINT :
			return smpdCount;
		case OTHER       :
			return otherCount;
		}
		
		return 0;
	}
	
	/**
	 * Returns the storage of the specified file type.
	 * @return the storage of the specified file type
	 */
	public long getStorage( final FileType fileType ) {
		switch ( fileType ) {
		case ALL         :
			return storage;
		case SC2REPLAY   :
			return repStorage;
		case MOUSE_PRINT :
			return smpdStorage;
		case OTHER       :
			return otherStorage;
		}
		
		return 0;
	}
	
    public int getCount() {
    	return count;
    }

    public void setCount( int count ) {
    	this.count = count;
    }

    public long getStorage() {
    	return storage;
    }

    public void setStorage( long storage ) {
    	this.storage = storage;
    }
	
    public long getInbw() {
    	return inbw;
    }
	
    public void setInbw( long inbw ) {
    	this.inbw = inbw;
    }

    public long getOutbw() {
    	return outbw;
    }

    public void setOutbw( long outbw ) {
    	this.outbw = outbw;
    }

    public int getRepCount() {
    	return repCount;
    }

    public void setRepCount( int repCount ) {
    	this.repCount = repCount;
    }
	
    public long getRepStorage() {
    	return repStorage;
    }
	
    public void setRepStorage( long repStorage ) {
    	this.repStorage = repStorage;
    }
	
    public long getRepInbw() {
    	return repInbw;
    }
	
    public void setRepInbw( long repInbw ) {
    	this.repInbw = repInbw;
    }
	
    public long getRepOutbw() {
    	return repOutbw;
    }
	
    public void setRepOutbw( long repOutbw ) {
    	this.repOutbw = repOutbw;
    }

    public int getSmpdCount() {
    	return smpdCount;
    }
	
    public void setSmpdCount( int smpdCount ) {
    	this.smpdCount = smpdCount;
    }
	
    public long getSmpdStorage() {
    	return smpdStorage;
    }

    public void setSmpdStorage( long smpdStorage ) {
    	this.smpdStorage = smpdStorage;
    }
	
    public long getSmpdInbw() {
    	return smpdInbw;
    }
	
    public void setSmpdInbw( long smpdInbw ) {
    	this.smpdInbw = smpdInbw;
    }

    public long getSmpdOutbw() {
    	return smpdOutbw;
    }
	
    public void setSmpdOutbw( long smpdOutbw ) {
    	this.smpdOutbw = smpdOutbw;
    }

    public int getOtherCount() {
    	return otherCount;
    }
	
    public void setOtherCount( int otherCount ) {
    	this.otherCount = otherCount;
    }
	
    public long getOtherStorage() {
    	return otherStorage;
    }
	
    public void setOtherStorage( long otherStorage ) {
    	this.otherStorage = otherStorage;
    }

    public long getOtherInbw() {
    	return otherInbw;
    }
	
    public void setOtherInbw( long otherInbw ) {
    	this.otherInbw = otherInbw;
    }
	
    public long getOtherOutbw() {
    	return otherOutbw;
    }

    public void setOtherOutbw( long otherOutbw ) {
    	this.otherOutbw = otherOutbw;
    }

	public void setUpdated( Date updated ) {
	    this.updated = updated;
    }

	public Date getUpdated() {
	    return updated;
    }

	public void setRecalced( Date recalced ) {
	    this.recalced = recalced;
    }

	public Date getRecalced() {
	    return recalced;
    }

}
