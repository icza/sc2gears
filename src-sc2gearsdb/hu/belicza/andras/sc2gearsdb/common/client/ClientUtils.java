/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.common.client;

import hu.belicza.andras.sc2gearsdb.common.client.beans.DownloadParamsProvider;

import java.util.Date;
import java.util.Map.Entry;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * General utilities for the client sides of GWT modules.
 * 
 * @author Andras Belicza
 */
public class ClientUtils {
	
	public static final DateTimeFormat DATE_TIME_FORMAT      = DateTimeFormat.getFormat( "yyyy-MM-dd HH:mm:ss" );
	public static final DateTimeFormat HTML_TIMESTAMP_FORMAT = DateTimeFormat.getFormat( "'<b>'yyyy-MM-dd'</b>' HH:mm:ss" );
	public static final NumberFormat   NUMBER_FORMAT         = NumberFormat  .getFormat( "#,###" );
	public static final DateTimeFormat DATE_FORMAT           = DateTimeFormat.getFormat( "yyyy-MM-dd" );
	
	/**
	 * Adds a style name to a widget and returns the widget
	 * @param widget    widget to add the style name to
	 * @param styleName style name to be added
	 * @param <T> type of the widget
	 * @return the widget
	 */
	public static < T extends Widget > T styledWidget( final T widget, final String styleName ) {
		widget.addStyleName( styleName );
		return widget;
	}
	
	/**
	 * Creates an empty widget with a fixed vertical height.
	 * @param height height of the widget to create
	 * @return an empty widget with a fixed vertical height
	 */
	public static Widget createVerticalEmptyWidget( final int height ) {
		final Label l = new Label();
		l.setHeight( height + "px" );
		return l;
	}
	
	/**
	 * Creates an empty widget with a fixed horizontal width.
	 * @param width width of the widget to create
	 * @return an empty widget with a fixed horizontal width
	 */
	public static Widget createHorizontalEmptyWidget( final int width ) {
		final Label l = new Label();
		l.setWidth( width + "px" );
		return l;
	}
	
	/**
	 * Creates a file download button.
	 * @param downloadParamsProvider download parameters provider
	 * @param fileType               type of file to create a download button for
	 * @param sha1                   SHA-1 of the file
	 * @param fileName               name of the file
	 * @param sharedAccount          Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 * @return the created file download button
	 */
	public static Button createFileDownloadButton( final DownloadParamsProvider downloadParamsProvider, final String fileType, final String sha1, final String fileName, final String sharedAccount ) {
		final Button downloadButton = new ImageButton( "drive-download.png", "Download \"" + fileName + "\"" );
		
		downloadButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				final FormPanel formPanel = new FormPanel();
				formPanel.setAction( "/file/" + URL.encode( fileName ).replace( "#", "%23" ) ); // Browsers break file name from the end of the URL if '#' is found...
				formPanel.setMethod( FormPanel.METHOD_POST );
				
				final VerticalPanel formContentPanel = new VerticalPanel();
				Hidden hidden;
				for ( final Entry< String, String > entry : downloadParamsProvider.getDownloadParameterMap().entrySet() ) {
					hidden = new Hidden( entry.getKey(), entry.getValue() );
					formContentPanel.add( hidden );
				}
				hidden = new Hidden( downloadParamsProvider.getFileTypeParamName(), fileType ); formContentPanel.add( hidden );
				hidden = new Hidden( downloadParamsProvider.getSha1ParamName    (), sha1     ); formContentPanel.add( hidden );
				if ( sharedAccount != null ) {
					hidden = new Hidden( downloadParamsProvider.getSharedAccountParamName(), sharedAccount ); formContentPanel.add( hidden );
				}
				formPanel.setWidget( formContentPanel );
				
				// We have to add it to the root panel, else download will not work in FireFox and in Internet Explorer
				RootPanel.get().add( formPanel );
				formPanel.addSubmitCompleteHandler( new SubmitCompleteHandler() {
					@Override
					public void onSubmitComplete( final SubmitCompleteEvent event ) {
						RootPanel.get().remove( formPanel );
					}
				} );
				// Have to submit "deferred", else download will not work in FireFox (form is submitted, but response is not processed => file not saved)
				Scheduler.get().scheduleDeferred( new ScheduledCommand() {
					@Override
					public void execute() {
						formPanel.submit();
					}
				} );
			}
		} );
		
		return downloadButton;
	}
	
	/**
	 * Initiates a batch file download.
	 * @param downloadParamsProvider download parameters provider
	 * @param fileType               type of files to download
	 * @param sha1List               a comma separated list of SHA-1 of the files to download
	 * @param sharedAccount          Google account to access in case we're viewing a shared account; <code>null</code> otherwise
	 */
	public static void initiateBatchDownload( final DownloadParamsProvider downloadParamsProvider, final String fileType, final String sha1List, final String sharedAccount ) {
		final FormPanel formPanel = new FormPanel();
		
		formPanel.setAction( "/file/" + URL.encode( fileType + "_pack.zip" ) );
		formPanel.setMethod( FormPanel.METHOD_POST );
		
		final VerticalPanel formContentPanel = new VerticalPanel();
		Hidden hidden;
		for ( final Entry< String, String > entry : downloadParamsProvider.getBatchDownloadParameterMap().entrySet() ) {
			hidden = new Hidden( entry.getKey(), entry.getValue() );
			formContentPanel.add( hidden );
		}
		hidden = new Hidden( downloadParamsProvider.getFileTypeParamName(), fileType ); formContentPanel.add( hidden );
		hidden = new Hidden( downloadParamsProvider.getSha1ListParamName(), sha1List ); formContentPanel.add( hidden );
		if ( sharedAccount != null ) {
			hidden = new Hidden( downloadParamsProvider.getSharedAccountParamName(), sharedAccount ); formContentPanel.add( hidden );
		}
		formPanel.setWidget( formContentPanel );
		
		// We have to add it to the root panel, else download will not work in FireFox and in Internet Explorer
		RootPanel.get().add( formPanel );
		formPanel.addSubmitCompleteHandler( new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				RootPanel.get().remove( formPanel );
			}
		} );
		
		// Have to submit "deferred", else download will not work in FireFox (form is submitted, but response is not processed => file not saved)
		Scheduler.get().scheduleDeferred( new ScheduledCommand() {
			@Override
			public void execute() {
				formPanel.submit();
			}
		} );
	}
	
	/**
	 * Formats the specified amount of seconds to a short format.
	 * @param seconds seconds to be formatted
	 * @return the formatted seconds in short format
	 */
	public static String formatSeconds( int seconds ) {
		int hour = 0, min = 0;
		
		if ( seconds >= 3600 ) {
			hour = seconds / 3600;
			seconds  = seconds % 3600;
		}
		if ( seconds >= 60 ) {
			min = seconds / 60;
			seconds = seconds % 60;
		}
		
		return hour > 0 ? hour + ( min < 10 ? ":0" : ":" ) + min + ( seconds < 10 ? ":0" : ":" ) + seconds
				: min + ( seconds < 10 ? ":0" : ":" ) + seconds;
	}
	
	/**
	 * Displays a Details dialog box.<br>
	 * Renders a table with 2 columns: name and value pairs.
	 * @param caption title of the dialog
	 * @param values  values to be displayed; each element is an array (name-value pair) which defines a row
	 */
	public static void displayDetailsDialog( final String caption, final Object[][] values ) {
		final DialogBox dialogBox = new DialogBox( true );
		dialogBox.setText( caption );
		dialogBox.setGlassEnabled( true );
		
		final VerticalPanel content = new VerticalPanel();
		content.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		
		final FlexTable table = new FlexTable();
		table.setBorderWidth( 1 );
		table.setCellSpacing( 0 );
		table.setCellPadding( 3 );
		
		
		final CellFormatter cellFormatter = table.getCellFormatter();
		for ( int i = 0; i < values.length; i++ ) {
			// Name
			table.setWidget( i, 0, new Label( values[ i ][ 0 ].toString() ) );
			cellFormatter.addStyleName( i, 0, "headerRow" );
			
			final Object value = values[ i ] [ 1 ];
			
			if ( value == null )
				table.setWidget( i, 1, new Label() );
			else if ( value instanceof Widget ) 
				table.setWidget( i, 1, (Widget) value );
			else if ( value instanceof Date )
				table.setWidget( i, 1, createTimestampWidget( (Date) value ) );
			else {
				String stringValue;
				if ( value instanceof String )
					stringValue = (String) value;
				else if ( value instanceof Number )
					stringValue = NUMBER_FORMAT   .format( (Number) value );
				else
					stringValue = value.toString();
				table.setWidget( i, 1, new Label( stringValue ) );
			}
			cellFormatter.addStyleName( i, 1, "row" + ( i & 0x01 ) );
			cellFormatter.setHorizontalAlignment( i, 1, HasHorizontalAlignment.ALIGN_LEFT );
		}
		
		content.add( table );
		
		content.add( createVerticalEmptyWidget( 8 ) );
		content.add( ClientUtils.createDialogCloseButton( dialogBox, "Close" ) );
		content.add( createVerticalEmptyWidget( 8 ) );
		
		dialogBox.setWidget( content );
		
		dialogBox.center();
	}
	
	/**
	 * Sets common style for the specified button to be <i>small<i>.
	 * @param button button to be styled
	 */
	public static void styleSmallButton( final Button button ) {
		DOM.setStyleAttribute( button.getElement(), "fontSize", "85%" );
	}
	
	/**
	 * Sets the font size style attribute of the specified widget.
	 * @param widget   widget whose font size to be set
	 * @param fontSize font size to be set, for example <code>"85%"</code>
	 * @param <T> type of the widget
	 * @return the widget
	 */
	public static < T extends Widget > T setWidgetFontSize( final T widget, final String fontSize ) {
		DOM.setStyleAttribute( widget.getElement(), "fontSize", fontSize );
		return widget;
	}
	
	/**
	 * Sets the horizontal alignment of all cells of the specified table.
	 * @param table      table whose cells to be aligned
	 * @param hAlignment horizontal alignment to be set
	 */
	public static void alignTableCells( final HTMLTable table, final HorizontalAlignmentConstant hAlignment ) {
		final CellFormatter cellFormatter = table.getCellFormatter();
		
		for ( int i = table.getRowCount() - 1; i >= 0; i-- )
			for ( int j = table.getCellCount( i ) - 1; j >= 0; j-- )
				cellFormatter.setHorizontalAlignment( i, j, hAlignment );
	}
	
	/**
	 * Adds a {@link KeyPressHandler} to the specified widget which calls {@link Button#click()} on <code>targetButton</code>
	 * when the Enter key is pressed.
	 * @param widget       widget to add the key handler to
	 * @param targetButton target button to activate when the enter key is pressed
	 */
	public static void addEnterTarget( final HasKeyPressHandlers widget, final Button targetButton ) {
		widget.addKeyPressHandler( new KeyPressHandler() {
			@Override
			public void onKeyPress( final KeyPressEvent event ) {
				if ( event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER )
					targetButton.click();
			}
		} );
	}
	
	/**
	 * Creates and returns a {@link Button} which when activated closes the specified dialog box.
	 * @param dialogBox  dialog box to close when the close button is activated
	 * @param buttonText text of the close button
	 * @return a button which when activated closes the specified dialog box
	 */
	public static Button createDialogCloseButton( final DialogBox dialogBox, final String buttonText ) {
		return new Button( buttonText, new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				dialogBox.hide();
			}
		} );
	}
	
	/**
	 * Returns a widget which displays a formatted time stamp.
	 * @param date time stamp to be displayed
	 * @return a widget which displays a formatted time stamp
	 */
	public static Widget createTimestampWidget( final Date date ) {
		return new HTML( HTML_TIMESTAMP_FORMAT.format( date ) );
	}
	
	/**
	 * Returns a widget which displays a formatted time stamp.
	 * @param text text to prepend to the formatted time stamp
	 * @param date time stamp to be displayed
	 * @return a widget which displays a formatted time stamp
	 */
	public static Widget createTimestampWidget( final String text, final Date date ) {
		return new HTML( text + HTML_TIMESTAMP_FORMAT.format( date ) );
	}
	
	/**
	 * Returns a widget which indicates that the logged in user is being checked.
	 * @return a widget which indicates that the logged in user is being checked
	 */
	public static Widget createCheckingUserWidget() {
		final HorizontalPanel panel = new HorizontalPanel();
		panel.setHeight( "20px" );
		
		panel.add( new Image( "/images/loading.gif" ) );
		panel.add( createHorizontalEmptyWidget( 3 ) );
		panel.add( styledWidget( new Label( "Checking user..." ), "note" ) );
		
		return panel;
	}
	
	/**
	 * Creates and adds a logout link to the Root panel, at the top right absolute position.
	 * @param text      text of the logout link
	 * @param logoutUrl logout URL
	 * @return the logout link
	 */
	public static Anchor createAndSetupLogoutLink( final String text, final String logoutUrl ) {
		final Anchor logoutLink = new Anchor( text, logoutUrl );
		
		// Absolute position to the top right
		DOM.setStyleAttribute( logoutLink.getElement(), "position", "absolute" );
		DOM.setStyleAttribute( logoutLink.getElement(), "top"     , "2px"      );
		DOM.setStyleAttribute( logoutLink.getElement(), "right"   , "4px"      );
		
		RootPanel.get().add( logoutLink );
		
		return logoutLink;
	}
	
	/**
	 * Creates a new {@link Anchor} and adds a {@link ClickHandler} to it.
	 * @param text    text of the anchor
	 * @param handler click handler to be added
	 * @return the created anchor
	 */
	public static Anchor createAnchorWithHandler( final String text, final ClickHandler handler ) {
		final Anchor a = new Anchor( text );
		a.addClickHandler( handler );
		return a;
	}
	
	/**
	 * Sets an icon to the specified widget.<br>
	 * The widget will be set as a background image on the left side.
	 * @param widget    widget to set the icon for
	 * @param imageName name of the icon image file, relative to the <code>/images/</code> folder
	 * @param width     optional icon width; 18 pixel is used if not provided
	 * @param color     optional background color to be set for the widget; use <code>"transparent"</code> for no background
	 */
	public static void setWidgetIcon( final Widget widget, final String imageName, final Integer width, final String color ) {
		DOM.setStyleAttribute( widget.getElement(), "background", ( color == null ? "" : color ) + " url('/images/" + imageName + "') no-repeat 1px center" );
		DOM.setStyleAttribute( widget.getElement(), "paddingLeft", width == null ? "18px" : width + "px" );
	}
	
	/**
	 * Creates a new window and returns a reference to it.
	 * @param url      url to be passed to it
	 * @param name     name to be passed to it
	 * @param features features to be passed to it
	 * @return a reference to the new window
	 */
	public static native JavaScriptObject createNewWindow( final String url, final String name, final String features ) /*-{
		return $wnd.open( url, name, features );
	}-*/;
	
	/**
	 * Sets the location of a window
	 * @param window   window whose location to be set
	 * @param location location to be set 
	 */
	public static native void setWindowLocation( final JavaScriptObject window, final String location ) /*-{
		window.location = location;
	}-*/;
	
	/**
	 * Closes a window.
	 * @param window window to be closed
	 */
	public static native void closeWindow( final JavaScriptObject window ) /*-{
		window.close();
	}-*/;
	
	/**
	 * Tracks a Google Analytics page view.<br>
	 * Google Analytics tracking code must be included in the host html.<br>
	 * Asynchronous implementation. Synchronous would be:
	 * <pre>$wnd.pageTracker._trackPageview(pageName);</pre>
	 * @param pageName page name to be reported
	 */
	public static native void trackAnalyticsPageView( final String pageName ) /*-{
		$wnd._gaq.push(['_trackPageview', pageName]);
	}-*/;
	
}
