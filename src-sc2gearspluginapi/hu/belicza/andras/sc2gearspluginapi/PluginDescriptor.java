package hu.belicza.andras.sc2gearspluginapi;

import java.io.File;
import java.util.Date;

/**
 * Plugin descriptor: provides meta-data about a plugin.
 * 
 * @version 1.0
 * 
 * @author Andras Belicza
 */
public interface PluginDescriptor {
	
	/**
	 * Returns the plugin folder (where the plugin files are).
	 * @return the plugin folder (where the plugin files are)
	 */
	File getPluginFolder();
	
	/**
	 * Returns the libraries (*.jar files) that were found and loaded from the plugin folder.
	 * @return the libraries (*.jar files) that were found and loaded from the plugin folder
	 */
	File[] getPluginLibs();
	
	/**
	 * Returns the name of the plugin.<br>
	 * This property is taken from the plugin descriptor XML (tag name: <code>"name"</code>).
	 * @return the name of the plugin
	 */
	String getName();
	
	/**
	 * Returns the first name of the author of the plugin.<br>
	 * This property is taken from the plugin descriptor XML (tag name: <code>"authorFirstName"</code>).
	 * @return the first name of the author of the plugin
	 */
	String getAuthorFirstName();
	
	/**
	 * Returns the last name of the author of the plugin.<br>
	 * This property is taken from the plugin descriptor XML (tag name: <code>"authorLastName"</code>).
	 * @return the last name of the author of the plugin
	 */
	String getAuthorLastName();
	
	/**
	 * Returns the email of the author of the plugin.<br>
	 * This property is taken from the plugin descriptor XML (tag name: <code>"authorEmail"</code>).
	 * @return the email of the author of the plugin
	 */
	String getAuthorEmail();
	
	/**
	 * Returns the version of the plugin.<br>
	 * This property is taken from the plugin descriptor XML (tag name: <code>"version"</code>).
	 * @return the version of the plugin
	 */
	String getVersion();
	
	/**
	 * Returns the release date of the plugin.<br>
	 * This property is taken from the plugin descriptor XML (tag name: <code>"releaseDate"</code>).
	 * @return the date of the plugin
	 */
	Date getReleaseDate();
	
	/**
	 * Returns the API version implemented/used by the plugin.<br>
	 * This property is taken from the plugin descriptor XML (tag name: <code>"apiVersion"</code>).
	 * @return the API version implemented/used by the plugin
	 */
	String getApiVersion();
	
	/**
	 * Returns the home page of the plugin.<br>
	 * This property is taken from the plugin descriptor XML (tag name: <code>"homePage"</code>).
	 * @return the home page of the plugin
	 */
	String getHomePage();
	
	/**
	 * Tells if the description is HTML.
	 * This property is taken from the plugin descriptor XML (tag name: <code>"description"</code>, attribute: <code>"isHtml"</code>).
	 * @return true if the description is HTML; false if it is plain text
	 */
	boolean isHtmlDescription();
	
	/**
	 * Returns the description of the plugin.<br>
	 * This property is taken from the plugin descriptor XML (tag name: <code>"description"</code>).
	 * @return the description of the plugin
	 */
	String getDescription();
	
	/**
	 * Returns the main class of the plugin.<br>
	 * This property is taken from the plugin descriptor XML (tag name: <code>"mainClass"</code>).
	 * @return the main class of the plugin
	 */
	String getMainClass();
	
}
