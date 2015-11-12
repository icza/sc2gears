/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gears.ui.dialogs;

import hu.belicza.andras.sc2gears.Consts;
import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.sc2replay.EapmUtils;
import hu.belicza.andras.sc2gears.sc2replay.ReplayFactory;
import hu.belicza.andras.sc2gears.services.plugins.GeneralServicesImpl;
import hu.belicza.andras.sc2gears.services.plugins.PluginServicesImpl;
import hu.belicza.andras.sc2gears.services.streaming.PrivateVideoStreaming;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.sound.Sounds;
import hu.belicza.andras.sc2gears.sound.Sounds.VoiceDescription;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.ui.mousepracticegame.MousePracticeGameFrame;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gears.util.ProfileCache;
import hu.belicza.andras.sc2gears.util.ReplayCache;
import hu.belicza.andras.sc2gears.util.TemplateEngine;
import hu.belicza.andras.sc2gearspluginapi.Plugin;
import hu.belicza.andras.smpd.SmpdUtil;
import hu.belicza.andras.smpd.SmpdUtil.SmpdVer;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * About information dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class AboutDialog extends BaseDialog {
	
	/**
	 * Creates a new AboutDialog.
	 */
	public AboutDialog() {
		super( "about.title", new Object[] { Consts.APPLICATION_NAME } );
		
		final Box box = Box.createVerticalBox();
		box.setBorder( BorderFactory.createEmptyBorder( 15, 20, 15, 10 ) );
		
		box.add( GuiUtils.wrapInPanel( GeneralUtils.createAnimatedLogoLabel() ) );
		
		box.add( Box.createVerticalStrut( 10 ) );
		
		box.add( GuiUtils.wrapInPanel( new JLabel( Language.getText( "about.usedIcons", Consts.APPLICATION_NAME + "™" ) ) ) );
		box.add( Box.createVerticalStrut( 5 ) );
		box.add( GuiUtils.wrapInPanel( new JLabel( Language.getText( "about.sc2ImagesCopyrightCompany", "© Blizzard Entertainment, Inc." ) ) ) );
		box.add( Box.createVerticalStrut( 15 ) );
		box.add( new JPanel( new BorderLayout() ) );
		box.add( GuiUtils.wrapInPanel( GuiUtils.changeFontToBold( new JLabel( Language.getText( "about.elapsedTimeSinceStart", Consts.APPLICATION_NAME, GeneralUtils.formatLongSeconds( ( System.currentTimeMillis() - MainFrame.APPLICATION_START_TIME.getTime() ) / 1000 ) ) ) ) ) );
		box.add( new JPanel( new BorderLayout() ) );
		box.add( Box.createVerticalStrut( 15 ) );
		box.add( GuiUtils.wrapInPanel( new JLabel( Language.getText( "welcome.thankYou", Consts.APPLICATION_NAME + "™" ) ) ) );
		box.add( Box.createVerticalStrut( 10 ) );
		final JLabel copyrightLabel = new JLabel( "Copyright © " + Language.formatPersonName( Consts.AUTHOR_FIRST_NAME, Consts.AUTHOR_LAST_NAME ) + ", 2010-2014" );
		GuiUtils.changeFontToItalic( copyrightLabel );
		box.add( GuiUtils.wrapInPanel( copyrightLabel ) );
		box.add( Box.createVerticalStrut( 10 ) );
		box.add( GuiUtils.wrapInPanel( new JLabel( Consts.APPLICATION_NAME + " is a trademark of " + Language.formatPersonName( Consts.AUTHOR_FIRST_NAME, Consts.AUTHOR_LAST_NAME ) + "." ) ) );
		//box.add( new JSeparator() );
		box.add( Box.createVerticalStrut( 10 ) );
		
		final JLabel visitScelightLabel = GeneralUtils.createLinkLabel( "Visit Scelight™, the successor to " + Consts.APPLICATION_NAME, Consts.URL_SCELIGHT_HOME_PAGE );
		visitScelightLabel.setIcon( Icons.SCELIGHT );
		box.add( GuiUtils.wrapInPanel( visitScelightLabel ) );
		box.add( Box.createVerticalStrut( 10 ) );
		
		final JButton okButton = createCloseButton( "button.ok" );
		box.add( GuiUtils.wrapInPanel( okButton ) );
		
		getContentPane().add( box, BorderLayout.WEST );
		
		
		final VoiceDescription currentVoiceDesc = Sounds.getVoiceDescription( Settings.getString( Settings.KEY_SETTINGS_VOICE ) );
		final JTable infoTable = GuiUtils.createNonEditableTable();
		( (DefaultTableModel) infoTable.getModel() ).setDataVector(
			new Object[][] {
				{ Language.getText( "about.author"                       ), Language.formatPersonName( Consts.AUTHOR_FIRST_NAME, Consts.AUTHOR_LAST_NAME ) },
				{ Language.getText( "about.email"                        ), Consts.AUTHOR_EMAIL },
				{ Language.getText( "about.version"                      ), Consts.APPLICATION_VERSION },
				{ Language.getText( "about.releasedOn"                   ), Language.formatDate( Consts.APPLICATION_RELEASE_DATE ) },
				{ Language.getText( "about.currentLanguage"              ), Language.getLanguageName() },
				{ Language.getText( "about.currentTranslator"            ), Language.getTranslatorName() },
				{ Language.getText( "about.languageFileVersion"          ), Language.getLanguageFileVersion() + " (" + Language.getLanguageFileSubversion() + ")" },
				{ Language.getText( "about.currentVoice"                 ), currentVoiceDesc.displayName },
				{ Language.getText( "about.authorOfCurrentVoice"         ), Language.formatPersonName( currentVoiceDesc.authorFirstName, currentVoiceDesc.authorLastName ) },
				{ Language.getText( "about.updaterVersion", Consts.UPDATER_NAME ), Consts.UPDATER_VERSION },
				{ Language.getText( "about.replayParserVersion"          ), ReplayFactory.VERSION },
				{ Language.getText( "about.replayCacheVersion"           ), ReplayCache.CACHE_VERSION },
				{ Language.getText( "about.profileCacheVersion"          ), ProfileCache.CACHE_VERSION },
				{ Language.getText( "about.nameTemplateEngineVersion"    ), TemplateEngine.ENGINE_VERSION },
				{ Language.getText( "about.pluginApiVersion"             ), Plugin.API_VERSION },
				{ Language.getText( "about.pluginServicesImplVersion"    ), PluginServicesImpl.IMPL_VERSION },
				{ Language.getText( "about.generalServicesImplVersion"   ), GeneralServicesImpl.IMPL_VERSION },
				{ Language.getText( "about.eapmAlgorithmVersion"         ), EapmUtils.ALGORITHM_VERSION },
				{ Language.getText( "about.mousePracticeGameVersion"     ), MousePracticeGameFrame.GAME_VERSION },
				{ Language.getText( "about.smpdFormatVersion"            ), SmpdUtil.getVersionString( SmpdVer.V11.binaryValue ) },
				{ Language.getText( "about.privateVideoStreamingVersion" ), PrivateVideoStreaming.VERSION }
			}, new Object[] { "Property", "Value" } );
		GuiUtils.packTable( infoTable );
		final JPanel tableWrapper = new JPanel( new BorderLayout() );
		tableWrapper.add( infoTable );
		tableWrapper.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 15, 15, 15, 15 ), BorderFactory.createEtchedBorder() ) );
		getContentPane().add( tableWrapper, BorderLayout.CENTER );
		
		if ( Settings.getBoolean( Settings.KEY_SETTINGS_ENABLE_VOICE_NOTIFICATIONS ) )
			Sounds.playSoundSample( Sounds.SAMPLE_THANK_YOU, false );
		
		packAndShow( okButton, false );
	}
	
}
