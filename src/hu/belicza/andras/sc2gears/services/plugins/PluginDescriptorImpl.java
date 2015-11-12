/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.services.plugins;

import hu.belicza.andras.sc2gearspluginapi.PluginDescriptor;

import java.io.File;
import java.util.Date;

/**
 * Plugin descriptor.
 * 
 * @version 1.0
 * 
 * @author Andras Belicza
 */
public class PluginDescriptorImpl implements PluginDescriptor {
	
	/** The plugin folder (where the plugin files are). */
	private final File folder;
	
	/** The libraries (*.jar files) that were found and loaded from the plugin folder. */
	private File[] libs;
	
	/** Name of the plugin. */
	private String  name;
	/** First name of the author of the plugin.     */
	private String  authorFirstName;
	/** Last name of the author of the plugin.      */
	private String  authorLastName;
	/** Email of the author of the plugin.          */
	private String  authorEmail;
	/** Version of the plugin.                      */
	private String  version;
	/** Release date of the plugin.                 */
	private Date    releaseDate;
	/** API version implemented/used by the plugin. */
	private String  apiVersion;
	/** Home page of the plugin.                    */
	private String  homePage;
	/** Tells if the description is HTML.           */
	private boolean isHtmlDescription;
	/** Description of the plugin.                  */
	private String  description;
	/** Main class of the plugin.                   */
	private String  mainClass;
	
	/**
	 * Creates a new PluginDescriptorImpl.
	 * @param folder plugin folder (where the plugin files are)
	 */
	public PluginDescriptorImpl( final File folder ) {
		this.folder = folder;
	}
	
	@Override
	public File getPluginFolder() {
		return folder;
	}
	
	/**
	 * Sets the plugin libs.
	 * @param libs plugin libs to be set
	 */
	public void setPluginLibs( final File[] libs ) {
		this.libs = libs;
	}
	
	@Override
	public File[] getPluginLibs() {
		return libs.clone();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of the plugin.
	 * @param name name of the plugin to be set
	 */
	public void setName( final String name ) {
		this.name = name;
	}
	
	@Override
	public String getAuthorFirstName() {
		return authorFirstName;
	}
	
	/**
	 * Sets the first name of the author of the plugin.
	 * @param authorFirstName first name of the author of the plugin to be set
	 */
	public void setAuthorFirstName( final String authorFirstName ) {
		this.authorFirstName = authorFirstName;
	}
	
	@Override
	public String getAuthorLastName() {
		return authorLastName;
	}
	
	/**
	 * Sets the last name of the author of the plugin.
	 * @param authorLastName last name of the author of the plugin to be set
	 */
	public void setAuthorLastName( final String authorLastName) {
		this.authorLastName = authorLastName;
	}
	
	@Override
	public String getAuthorEmail() {
		return authorEmail;
	}
	
	/**
	 * Sets the email of the author of the plugin.
	 * @param authorEmail email of the author of the plugin to be set
	 */
	public void setAuthorEmail( final String authorEmail ) {
		this.authorEmail = authorEmail;
	}
	
	@Override
	public String getVersion() {
		return version;
	}
	
	/**
	 * Sets the version of the plugin.
	 * @param version version of the plugin to be set
	 */
	public void setVersion( final String version ) {
		this.version = version;
	}
	
	@Override
	public Date getReleaseDate() {
		return (Date) releaseDate.clone();
	}
	
	/**
	 * Sets the release date of the plugin.
	 * @param releaseDate releaseDate of the plugin to be set
	 */
	public void setReleaseDate( final Date releaseDate ) {
		this.releaseDate = releaseDate;
	}
	
	@Override
	public String getHomePage() {
		return homePage;
	}
	
	/**
	 * Sets the home page of the plugin.
	 * @param homePage homePage of the plugin to be set
	 */
	public void setHomePage( final String homePage ) {
		this.homePage = homePage;
	}
	
	@Override
	public boolean isHtmlDescription() {
		return isHtmlDescription;
	}
	
	/**
	 * Sets whether the description is HTML.
	 * @param isHtmlDescription HTML description property to be set
	 */
	public void setIsHtmlDescription( final boolean isHtmlDescription ) {
		this.isHtmlDescription = isHtmlDescription;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description of the plugin.
	 * @param description description of the plugin to be set
	 */
	public void setDescription( final String description ) {
		this.description = description;
	}
	
	@Override
	public String getMainClass() {
		return mainClass;
	}
	
	/**
	 * Sets the main class of the plugin.
	 * @param mainClass mainClass of the plugin to be set
	 */
	public void setMainClass( final String mainClass ) {
		this.mainClass = mainClass;
	}
	
	/**
	 * Sets the API version implemented/used by the plugin.
	 * @param apiVersion the API version implemented/used by the plugin to be set
	 */
	public void setApiVersion( final String apiVersion ) {
		this.apiVersion = apiVersion;
	}
	
	@Override
	public String getApiVersion() {
		return apiVersion;
	}
	
}
