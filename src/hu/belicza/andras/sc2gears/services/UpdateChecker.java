/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.services;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.Sc2gears;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.sound.Sounds;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.NormalThread;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Update checker.<br>
 * 
 * Checks for updates and notifies the user if there is new version.<br>
 * If no new version of the software is available, checks and updates the current language file if a newer version is available,
 * and notifies the user by playing a voice.
 * 
 * @author Andras Belicza
 */
public class UpdateChecker extends NormalThread {
	
	/** Reference to the check updates menu item (it has to be updated). */
	private final JMenuItem checkUpdatesMenuItem;
	
	/**
	 * Creates a new UpdateChecker and starts it.
	 * @param checkUpdatesMenuItem reference to the check updates menu item (it has to be updated)
	 */
	public UpdateChecker( final JMenuItem checkUpdatesMenuItem ) {
		super( "Update checker" );
		this.checkUpdatesMenuItem = checkUpdatesMenuItem;
		checkUpdatesMenuItem.setEnabled( false );
		GuiUtils.updateButtonText( checkUpdatesMenuItem, "menu.help.checkUpdatesChecking" );
		start();
	}
	
	/**
	 * Update check is performed in a new thread to not block the Swing's event dispatching thread.
	 */
	@Override
	public void run() {
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments( true );
			final Document latestVersionDocument = factory.newDocumentBuilder().parse( new URL( Consts.URL_LATEST_VERSION ).toURI().toString() );
			final Element rootElement = latestVersionDocument.getDocumentElement();
			
			final String latestVersion = ( (Element) rootElement.getElementsByTagName( "latestVersion" ).item( 0 ) ).getTextContent();
			if ( !Consts.APPLICATION_VERSION.equals( latestVersion ) ) {
				// New version detected
				if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
					Sounds.playSoundSample( Sounds.SAMPLE_UPDATES_AVAILABLE, false );
				final String installButtonText = Language.getText( "updatesAvailable.updateButton" );
				final int answer = JOptionPane.showOptionDialog( MainFrame.INSTANCE,
						new Object[] { Language.getText( "updatesAvailable.newVersionAvailable", latestVersion ), GeneralUtils.createLinkLabel( Language.getText( "updatesAvailable.viewVersionHistory" ), Consts.URL_VERSION_HISTORY ), " ", Language.getText( "updatesAvailable.whatToDo" ) },
						Language.getText( "updatesAvailable.title" ), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
						new Object[] { installButtonText, Language.getText( "updatesAvailable.dontUpdateButton" ) }, installButtonText );
				
				if ( answer == 0 ) {
					// Post-update must be complete before starting another update
					if ( !Sc2gears.checkAndPerformPostUpdate() ) {
						GuiUtils.showErrorDialog( new String[] { Language.getText( "updatesAvailable.updaterCannotBeStarted", Consts.UPDATER_NAME ), Language.getText( "updatesAvailable.restart", Consts.APPLICATION_NAME ) } );
						return;
					}
					
					final List< String > updaterParamList = new ArrayList< String >();
					updaterParamList.add( latestVersion );
					
					updaterParamList.add( rootElement.getElementsByTagName( "requiredMinUpdaterVer" ).item( 0 ).getTextContent() );
					updaterParamList.add( rootElement.getElementsByTagName( "archiveSha256"         ).item( 0 ).getTextContent() );
					
					final NodeList archiveUrlTags = rootElement.getElementsByTagName( "archiveUrl" );
					for ( int i = 0; i < archiveUrlTags.getLength(); i++ )
						updaterParamList.add( archiveUrlTags.item( i ).getTextContent() );
					
					MainFrame.INSTANCE.exit( true, updaterParamList.toArray( new String[ updaterParamList.size() ] ) );
				}
			}
			else {
				// Application version is OK. Check if there is a newer language file
				final Element  languagesElement = (Element) rootElement.getElementsByTagName( "languages" ).item( 0 );
				if ( languagesElement == null )
					return;
				final String   currentLanguage  = Settings.getString( Settings.KEY_SETTINGS_LANGUAGE );
				final NodeList languageNodeList = languagesElement.getElementsByTagName( "language" );
				final int      languagesCount   = languageNodeList.getLength();
				
				boolean languageFileUpdated = false;
				for ( int i = 0; i < languagesCount; i++ ) {
					final Element language = (Element) languageNodeList.item( i );
					
					boolean newLanguageAvailable = currentLanguage.equals( language.getAttribute( "name" ) ) && ( !Language.getLanguageFileVersion().equals( language.getAttribute( "version" ) ) || !Language.getLanguageFileSubversion().equals( language.getAttribute( "subversion" ) ) );
					if ( !Settings.DEFAULT_APP_LANGUAGE.equals( currentLanguage ) ) // The default language has to be checked and updated no matter what the selected language is
						newLanguageAvailable |= Settings.DEFAULT_APP_LANGUAGE.equals( language.getAttribute( "name" ) ) && ( !Language.getDefaultLanguageFileVersion().equals( language.getAttribute( "version" ) ) || !Language.getDefaultLanguageFileSubversion().equals( language.getAttribute( "subversion" ) ) );
					
					if ( newLanguageAvailable ) {
						System.out.println( "A new language file is available: " + language.getAttribute( "version" ) + " (" + language.getAttribute( "subversion" ) + ") (" + language.getAttribute( "name" ) + ")" );
						System.out.println( "Updating language file..." );
						final File tempLanguageFile = new File( Consts.FOLDER_LANGUAGES, language.getAttribute( "name" ) + ".xml.temp" );
						if ( GeneralUtils.downloadUrl( languagesElement.getAttribute( "baseUrl" ) + language.getAttribute( "file" ), tempLanguageFile ) ) {
							if ( GeneralUtils.calculateFileMd5( tempLanguageFile ).equals( language.getAttribute( "md5" ) ) ) {
								final File languageFile = new File( Consts.FOLDER_LANGUAGES, language.getAttribute( "name" ) + ".xml" );
								if ( languageFile.delete() ) {
									if ( tempLanguageFile.renameTo( languageFile ) ) {
										System.out.println( "Language file updated." );
										languageFileUpdated |= true;
									}
									else
										System.out.println( "Failed to update the language file (failed to rename " + tempLanguageFile.getAbsolutePath() + " to " + languageFile.getAbsolutePath() + ")!" );
								}
								else
									System.out.println( "Failed to update the language file (failed to delete " + languageFile.getAbsolutePath() + ")!" );
							}
							else
								System.out.println( "Failed to update the language file (MD5 check failed)!" );
						}
						else
							System.out.println( "Failed to update the language file (failed to download the new language file)!" );
					}
				}
				if ( languageFileUpdated ) {
					System.out.println( "Please restart Sc2gears." );
					if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
						Sounds.playSoundSample( Sounds.SAMPLE_LANGUAGE_UPDATED, false );
				}
			}
		} catch ( final Exception e ) {
			e.printStackTrace();
		} finally {
			GuiUtils.updateButtonText( checkUpdatesMenuItem, "menu.help.checkUpdates" );
			checkUpdatesMenuItem.setEnabled( true );
		}
	}
	
}
