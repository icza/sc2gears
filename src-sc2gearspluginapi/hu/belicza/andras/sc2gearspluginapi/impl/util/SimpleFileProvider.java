/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.impl.util;

import java.io.File;
import java.net.HttpURLConnection;

import hu.belicza.andras.sc2gearspluginapi.api.httpost.FileProvider;

/**
 * A simple file provider which takes the provided file name or the file itself and last modified date as constructor arguments.
 * 
 * @since "2.2"
 * 
 * @author Andras Belicza
 * 
 * @see FileProvider
 */
public class SimpleFileProvider implements FileProvider {
	
	/** The provided file.                       */
	protected File file;
	/** Last modified date of the provided file. */
	protected Long lastModified;
	
	/**
	 * Creates a new SimpleFileProvider.
	 * @param fileName     name (and path) of the provided file 
	 * @param lastModified last modified date of the provided file
	 */
	public SimpleFileProvider( final String fileName, final Long lastModified ) {
		this( new File( fileName ), lastModified );
	}
	
	/**
	 * Creates a new SimpleFileProvider.
	 * @param file         the provided file 
	 * @param lastModified last modified date of the provided file
	 */
	public SimpleFileProvider( final File file, final Long lastModified ) {
		this.file         = file;
		this.lastModified = lastModified;
	}
	
	@Override
	public File getFile( final HttpURLConnection httpUrlConnection ) {
		return file;
	}
	
	@Override
	public Long getLastModified( final HttpURLConnection httpUrlConnection ) {
		return lastModified;
	}
	
}
