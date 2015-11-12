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

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Proxy configuration dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class ProxyConfigDialog extends BaseDialog {
	
	/**
	 * Proxy configuration.
	 * @author Andras Belicza
	 */
	public static class ProxyConfig {
		/** HTTP Proxy host.  */
		public String httpProxyHost;
		/** HTTP Proxy port.  */
		public int    httpProxyPort;
		
		/** HTTPS Proxy host. */
		public String httpsProxyHost;
		/** HTTPS Proxy port. */
		public int    httpsProxyPort;
		
		/** SOCKS Proxy host. */
		public String socksProxyHost;
		/** SOCKS Proxy port. */
		public int    socksProxyPort;
		
		/** Tells if the Updater also have to use Proxy. */
		public boolean useProxyWhenDownloadingUpdates;
		
		/**
		 * Creates a new ProxyConfig, initialized from the {@link Settings}.
		 */
		public ProxyConfig() {
			httpProxyHost  = Settings.getString( Settings.KEY_SETTINGS_MISC_HTTP_PROXY_HOST  );
			httpProxyPort  = Settings.getInt   ( Settings.KEY_SETTINGS_MISC_HTTP_PROXY_PORT  );
			
			httpsProxyHost = Settings.getString( Settings.KEY_SETTINGS_MISC_HTTPS_PROXY_HOST );
			httpsProxyPort = Settings.getInt   ( Settings.KEY_SETTINGS_MISC_HTTPS_PROXY_PORT );
			
			socksProxyHost = Settings.getString( Settings.KEY_SETTINGS_MISC_SOCKS_PROXY_HOST );
			socksProxyPort = Settings.getInt   ( Settings.KEY_SETTINGS_MISC_SOCKS_PROXY_PORT );
			
			useProxyWhenDownloadingUpdates = Settings.getBoolean( Settings.KEY_SETTINGS_MISC_USE_PROXY_WHEN_DOWNLOADING_UPDATES );
		}
		
		/**
		 * Sets the proxy config to the {@link Settings}.
		 */
		public void storeSettings() {
			Settings.set( Settings.KEY_SETTINGS_MISC_HTTP_PROXY_HOST, httpProxyHost  );
			Settings.set( Settings.KEY_SETTINGS_MISC_HTTP_PROXY_PORT, httpProxyPort  );
			
			Settings.set( Settings.KEY_SETTINGS_MISC_HTTPS_PROXY_HOST, httpsProxyHost );
			Settings.set( Settings.KEY_SETTINGS_MISC_HTTPS_PROXY_PORT, httpsProxyPort );
			
			Settings.set( Settings.KEY_SETTINGS_MISC_SOCKS_PROXY_HOST, socksProxyHost );
			Settings.set( Settings.KEY_SETTINGS_MISC_SOCKS_PROXY_PORT, socksProxyPort );
			
			Settings.set( Settings.KEY_SETTINGS_MISC_USE_PROXY_WHEN_DOWNLOADING_UPDATES, useProxyWhenDownloadingUpdates );
		}
	}
	
	/**
	 * Creates a new TipsDialog.
	 */
	public ProxyConfigDialog( final ProxyConfig proxyConfig ) {
		super( "proxyConfig.title", Icons.GLOBE_NETWORK);
		
		final JPanel northPanel = new JPanel( new BorderLayout() );
		
		final Box infoBox = Box.createVerticalBox();
		infoBox.setBorder( BorderFactory.createEmptyBorder( 15, 15, 0, 15 ) );
		infoBox.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 15, 15, 0, 15 ), BorderFactory.createTitledBorder( "Sc2gears Proxy usage:" ) ) );
		Box row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "proxyConfig.httpProxy" ) ) );
		row.add( Box.createHorizontalGlue() );
		row.add( Box.createHorizontalStrut( 5 ) );
		row.add( new JLabel( Language.getText( "proxyConfig.httpProxyUsage" ) ) );
		infoBox.add( row );
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "proxyConfig.httpsProxy" ) ) );
		row.add( Box.createHorizontalGlue() );
		row.add( Box.createHorizontalStrut( 5 ) );
		row.add( new JLabel( Language.getText( "proxyConfig.httpsProxyUsage" ) ) );
		infoBox.add( row );
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "proxyConfig.socksProxy" ) ) );
		row.add( Box.createHorizontalGlue() );
		row.add( Box.createHorizontalStrut( 5 ) );
		row.add( new JLabel( Language.getText( "proxyConfig.socksProxyUsage" ) ) );
		infoBox.add( row );
		GuiUtils.alignBox( infoBox, 4 );
		northPanel.add( infoBox, BorderLayout.CENTER );
		
		northPanel.add( GuiUtils.wrapInPanel( GeneralUtils.createLinkLabel( Language.getText( "proxyConfig.moreAboutTheProxies" ), "http://en.wikipedia.org/wiki/Proxy_server" ) ), BorderLayout.SOUTH );
		
		getContentPane().add( northPanel, BorderLayout.NORTH );
		
		final Box box = Box.createVerticalBox();
		box.setBorder( BorderFactory.createEmptyBorder( 15, 15, 15, 15 ) );
		
		// HTTP proxy
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "proxyConfig.httpProxyHost" ) ) );
		final JTextField httpProxyHostTextField = new JTextField( proxyConfig.httpProxyHost, 20 );
		row.add( httpProxyHostTextField );
		row.add( new JLabel( Language.getText( "proxyConfig.port" ) ) );
		final JSpinner httpProxyPortSpinner = new JSpinner( new SpinnerNumberModel( proxyConfig.httpProxyPort, 0, 65535, 1 ) );
		httpProxyPortSpinner.setToolTipText( Language.getText( "miscSettings.validRangeAndDefaultToolTip", 0, 65535, Settings.getDefaultInt( Settings.KEY_SETTINGS_MISC_HTTP_PROXY_PORT ) ) );
		row.add( httpProxyPortSpinner );
		box.add( row );
		
		// HTTPS proxy
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "proxyConfig.httpsProxyHost" ) ) );
		final JTextField httpsProxyHostTextField = new JTextField( proxyConfig.httpsProxyHost, 20 );
		row.add( httpsProxyHostTextField );
		row.add( new JLabel( Language.getText( "proxyConfig.port" ) ) );
		final JSpinner httpsProxyPortSpinner = new JSpinner( new SpinnerNumberModel( proxyConfig.httpsProxyPort, 0, 65535, 1 ) );
		httpsProxyPortSpinner.setToolTipText( Language.getText( "miscSettings.validRangeAndDefaultToolTip", 0, 65535, Settings.getDefaultInt( Settings.KEY_SETTINGS_MISC_HTTPS_PROXY_PORT ) ) );
		row.add( httpsProxyPortSpinner );
		box.add( row );
		
		// SOCKS proxy
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "proxyConfig.socksProxyHost" ) ) );
		final JTextField socksProxyHostTextField = new JTextField( proxyConfig.socksProxyHost, 20 );
		row.add( socksProxyHostTextField );
		row.add( new JLabel( Language.getText( "proxyConfig.port" ) ) );
		final JSpinner socksProxyPortSpinner = new JSpinner( new SpinnerNumberModel( proxyConfig.socksProxyPort, 0, 65535, 1 ) );
		socksProxyPortSpinner.setToolTipText( Language.getText( "miscSettings.validRangeAndDefaultToolTip", 0, 65535, Settings.getDefaultInt( Settings.KEY_SETTINGS_MISC_SOCKS_PROXY_PORT ) ) );
		row.add( socksProxyPortSpinner );
		box.add( row );
		
		GuiUtils.alignBox( box, 4 );
		
		box.add( Box.createVerticalStrut( 12 ) );
		final JCheckBox useProxyWhenDownloadingUpdatesCheckBox = new JCheckBox( Language.getText( "proxyConfig.useProxyWhenDownloadingUpdates" ), proxyConfig.useProxyWhenDownloadingUpdates );
		box.add( GuiUtils.wrapInPanelLeftAligned( useProxyWhenDownloadingUpdatesCheckBox ) );
		
		getContentPane().add( box, BorderLayout.CENTER );
		
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 0, 15, 10, 15 ) );
		final JButton okButton = new JButton();
		GuiUtils.updateButtonText( okButton, "button.ok" );
		okButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				proxyConfig.httpProxyHost  = httpProxyHostTextField.getText().trim().replace( "\"", "" );
				proxyConfig.httpProxyPort  = (Integer) httpProxyPortSpinner.getValue();
				
				proxyConfig.httpsProxyHost = httpsProxyHostTextField.getText().trim().replace( "\"", "" );
				proxyConfig.httpsProxyPort = (Integer) httpsProxyPortSpinner.getValue();
				
				proxyConfig.socksProxyHost = socksProxyHostTextField.getText().trim().replace( "\"", "" );
				proxyConfig.socksProxyPort = (Integer) socksProxyPortSpinner.getValue();
				
				proxyConfig.useProxyWhenDownloadingUpdates = useProxyWhenDownloadingUpdatesCheckBox.isSelected();
				
				dispose();
			}
		} );
		buttonsPanel.add( okButton );
		final JButton cancelButton = createCloseButton( "button.cancel" );
		buttonsPanel.add( cancelButton );
		final JButton restoreDefaultsButton = new JButton();
		GuiUtils.updateButtonText( restoreDefaultsButton, "button.restoreDefaults" );
		restoreDefaultsButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				httpProxyHostTextField .setText ( Settings.getDefaultString( Settings.KEY_SETTINGS_MISC_HTTP_PROXY_HOST   ) );
				httpProxyPortSpinner   .setValue( Settings.getDefaultInt   ( Settings.KEY_SETTINGS_MISC_HTTP_PROXY_PORT   ) );
				
				httpsProxyHostTextField.setText ( Settings.getDefaultString( Settings.KEY_SETTINGS_MISC_HTTPS_PROXY_HOST  ) );
				httpsProxyPortSpinner  .setValue( Settings.getDefaultInt   ( Settings.KEY_SETTINGS_MISC_HTTPS_PROXY_PORT  ) );
				
				socksProxyHostTextField.setText ( Settings.getDefaultString( Settings.KEY_SETTINGS_MISC_SOCKS_PROXY_HOST  ) );
				socksProxyPortSpinner  .setValue( Settings.getDefaultInt   ( Settings.KEY_SETTINGS_MISC_SOCKS_PROXY_PORT  ) );
				
				useProxyWhenDownloadingUpdatesCheckBox.setSelected( Settings.getDefaultBoolean( Settings.KEY_SETTINGS_MISC_USE_PROXY_WHEN_DOWNLOADING_UPDATES ) );
			}
		} );
		buttonsPanel.add( restoreDefaultsButton );
		getContentPane().add( buttonsPanel, BorderLayout.SOUTH );
		
		packAndShow( cancelButton, false );
	}
	
}
