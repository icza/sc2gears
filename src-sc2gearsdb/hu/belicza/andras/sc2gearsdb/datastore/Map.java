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

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Blob;

/**
 * Class representing a StarCraft II map.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>v</code> property.
 * 			<br>Made {@link #fsize}, {@link #fsource}, {@link #size}, {@link #width}, {@link #height},
 * 				{@link #mwidth}, {@link #mheight}, {@link #mboundaries} properties unindexed.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class Map extends DataStoreObject {
	
	public static final int STATUS_READY         =    0;
	public static final int STATUS_PROCESSING    =    1;
	public static final int STATUS_DL_ERROR      = 1000;
	public static final int STATUS_PARSING_ERROR = 1001;
	
	/** Map file name (it's the SHA-256 digest of the content of the map plus ".s2ma"). */
	@Persistent
	private String fname;
	
	/** Status of the map.
	 * The status is stored so that if a map is not available or we can't parse it,
	 * we will not attempt to process it (again) each time it is requested... */
	@Persistent
	private int status;
	
	/** Size of the map file in bytes. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int fsize;
	
	/** Map file source (where it was downloaded from). */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String fsource;
	
	/** Size of the map image in bytes. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int size;
	
	/** Width of the map image in pixels. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int width;
	
	/** Height of the map image in pixels. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int height;
	
	/** Image data of the map in JPEG format. */
	@Persistent
	private Blob image;
	
	/** Name of the map. */
	@Persistent
	private String name;
	
	/** Width of the map. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int mwidth;
	
	/** Height of the map. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private int mheight;
	
	/** Boundaries map in format of: <code>"boundary_left,boundary_bottom,boundary_right,boundary_top"</code>. */
	@Persistent
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private String mboundaries;
	
    /**
     * Creates a new Map.
     * @param fname map file name
     */
    public Map( final String fname ) {
    	this.fname = fname;
    	status     = STATUS_PROCESSING;
    	setV( 1 );
    }

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getFsize() {
		return fsize;
	}

	public void setFsize(int fsize) {
		this.fsize = fsize;
	}

	public String getFsource() {
		return fsource;
	}

	public void setFsource(String fsource) {
		this.fsource = fsource;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Blob getImage() {
		return image;
	}

	public void setImage(Blob image) {
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMwidth(int mwidth) {
		this.mwidth = mwidth;
	}

	public int getMwidth() {
		return mwidth;
	}

	public void setMheight(int mheight) {
		this.mheight = mheight;
	}

	public int getMheight() {
		return mheight;
	}

	public void setMboundaries(String mboundaries) {
		this.mboundaries = mboundaries;
	}

	public String getMboundaries() {
		return mboundaries;
	}
	
}
