/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.common.client.beans;

import java.util.Map;

/**
 * Interface for a download parameters provider.
 * 
 * @author Andras Belicza
 */
public interface DownloadParamsProvider {
	
	Map< String, String > getDownloadParameterMap();
	
	Map< String, String > getBatchDownloadParameterMap();
	
	String getFileTypeParamName();
	
	String getSha1ParamName();
	
	String getSha1ListParamName();
	
	String getSharedAccountParamName();
	
}
