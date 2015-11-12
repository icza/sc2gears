package hu.belicza.andras.sc2gearspluginapi.api.listener;

import java.io.File;

import hu.belicza.andras.sc2gearspluginapi.api.GeneralUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.ui.IDownloader;

/**
 * A callback to be notified when the download finishes.
 * 
 * @author Andras Belicza
 * 
 * @see IDownloader
 * @see GeneralUtilsApi#getDownloader(String, File, boolean, DownloaderCallback)
 */
public interface DownloaderCallback {
	
	/**
	 * Called when the download process finishes.
	 * @param success true if download was successful; false otherwise
	 */
	void downloadFinished( boolean success );
	
}
