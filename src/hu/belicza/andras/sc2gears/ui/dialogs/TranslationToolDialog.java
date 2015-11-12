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
import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.settings.Settings.PredefinedList;
import hu.belicza.andras.sc2gears.ui.GuiUtils;
import hu.belicza.andras.sc2gears.ui.components.BaseLabelListCellRenderer;
import hu.belicza.andras.sc2gears.ui.components.TableBox;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.Holder;
import hu.belicza.andras.sc2gears.util.Task;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Translation tool dialog.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class TranslationToolDialog extends BaseDialog {
	
	/** Header keys of the texts table. */
	private static final String[] TEXTS_HEADER_KEYS = new String[] {
		"translationTool.tab.texts.table.header.textKeyContext",
		"translationTool.tab.texts.table.header.originalText",
		"translationTool.tab.texts.table.header.translation"
	};
	/** Column model index of the text key column.      */
	private static final int TEXT_KEY_COLUMN;
	/** Column model index of the original text column. */
	private static final int ORIGINAL_TEXT_COLUMN;
	/** Column model index of the translation column.   */
	private static final int TRANSLATION_COLUMN;
	/** Header names of the texts table.                */
	private static final Vector< String > TEXTS_HEADER_NAME_VECTOR = new Vector< String >( TEXTS_HEADER_KEYS.length );
	static {
		int TEXT_KEY_COLUMN_      = 0;
		int ORIGINAL_TEXT_COLUMN_ = 0;
		int TRANSLATION_COLUMN_   = 0;
		
		for ( int i = 0; i < TEXTS_HEADER_KEYS.length; i++ ) {
			final String headerKey = TEXTS_HEADER_KEYS[ i ];
			TEXTS_HEADER_NAME_VECTOR.addElement( Language.getText( headerKey ) );
			if ( "translationTool.tab.texts.table.header.textKeyContext".equals( headerKey ) )
				TEXT_KEY_COLUMN_ = i;
			else if ( "translationTool.tab.texts.table.header.originalText".equals( headerKey ) )
				ORIGINAL_TEXT_COLUMN_ = i;
			else if ( "translationTool.tab.texts.table.header.translation".equals( headerKey ) )
				TRANSLATION_COLUMN_ = i;
		}
		
		TEXT_KEY_COLUMN      = TEXT_KEY_COLUMN_;
		ORIGINAL_TEXT_COLUMN = ORIGINAL_TEXT_COLUMN_;
		TRANSLATION_COLUMN   = TRANSLATION_COLUMN_;
	}
	
	/** Total number of texts. */
	private final int TOTAL_TEXTS_COUNT = Language.DEFAULT_LANGUAGE.textMap.size();
	
	/**
	 * Creates a new TranslationToolDialog.
	 */
	public TranslationToolDialog() {
		super( "translationTool.title", Icons.LOCALE );
		
		final JPanel northPanel = new JPanel( new BorderLayout() );
		northPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 0, 10 ) );
		
		final Box languageChooserBox = Box.createVerticalBox();
		languageChooserBox.setBorder( BorderFactory.createEmptyBorder( 10, 0, 15, 0 ) );
		Box row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "translationTool.chooseLanguageToEdit" ) ) );
		final JComboBox< String > editedLanguageComboBox = new JComboBox<>();
		final String EMPTY_LANGUAGE = " ";
		final Runnable rebuildEditedLanguageComboBoxTask = new Runnable() {
			@Override
			public void run() {
				editedLanguageComboBox.removeAllItems();
				editedLanguageComboBox.addItem( EMPTY_LANGUAGE );
				
				for ( final String language : Language.getAvailableLanguages() )
					if ( !Settings.DEFAULT_APP_LANGUAGE.equals( language ) ) 
						editedLanguageComboBox.addItem( language );
				
				editedLanguageComboBox.setMaximumRowCount( editedLanguageComboBox.getItemCount() );
			}
		};
		rebuildEditedLanguageComboBoxTask.run();
		editedLanguageComboBox.setSelectedIndex( 0 );
		editedLanguageComboBox.setRenderer( new BaseLabelListCellRenderer< String >( 2 ) {
			@Override
			public Icon getIcon( final String value ) {
				return EMPTY_LANGUAGE.equals( value ) ? null : Icons.getLanguageIcon( value );
			}
		} );
		row.add( editedLanguageComboBox );
		languageChooserBox.add( row );
		languageChooserBox.add( Box.createVerticalStrut( 7 ) );
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "translationTool.orCreateNewTranslationWithName" ) ) );
		final JTextField newLanguageNameTextField = new JTextField( 10 );
		row.add( newLanguageNameTextField );
		final JButton createNewLanguageButton = new JButton();
		GuiUtils.updateButtonText( createNewLanguageButton, "translationTool.createButton" );
		row.add( createNewLanguageButton );
		languageChooserBox.add( row );
		GuiUtils.alignBox( languageChooserBox, 1 );
		
		languageChooserBox.add( Box.createVerticalStrut( 20 ) );
		final JPanel buttonsPanel = new JPanel( new GridLayout( 1, 2 ) );
		final JButton saveButton = new JButton( Icons.DISK );
		GuiUtils.updateButtonText( saveButton, "translationTool.saveChangesButton" );
		buttonsPanel.add( saveButton );
		final JButton closeButton = createCloseButton( "button.close" );
		buttonsPanel.add( closeButton );
		languageChooserBox.add( buttonsPanel );
		
		northPanel.add( GuiUtils.wrapInPanel( languageChooserBox ), BorderLayout.WEST );
		
		final JEditorPane notesEditorPane = new JEditorPane();
		notesEditorPane.setContentType( "text/html" );
		notesEditorPane.setText( "<html>"
				+ "<b>NOTES</b><br><br>"
				+ "With this Translation tool you can translate " + Consts.APPLICATION_NAME + " into other languages or edit any existing translations. If you create a new translation or update an existing one, please send the language file to me via email so I can include it in the next release.<br>"
				+ "Each translation is stored in its own file. Language files are saved in the <i>\"" + Consts.FOLDER_LANGUAGES + "\"</i> folder inside " + Consts.APPLICATION_NAME + ".<br>"
				+ "Official language files appear in the application with a country flag. If you send me language files that I approve, I will add and associate the proper country flag for the language.<br>"
				+ "Not all texts have to be translated, but of course the more the better. If a language file is incomplete, the original English version will be displayed for the missing texts.<br>"
				+ "<br><i>Warning! If you update " + Consts.APPLICATION_NAME + ", the Updater will overwrite existing language files! Be sure to keep a copy of the language file you edit!</i><br><br><hr>"
				+ "<b>PARAMETERS</b><br><br>"
				+ "The values of texts may contain parameters which will be substituted by values specified by the program.<br>"
				+ "The places and order of parameters are indicated with <span style='color:green'>$x</span> where <span style='color:green'>x</span> is the number identifier of the parameter (starting from 0).<br>"
				+ "For example the following text: <span style='color:green'>Hello $1, this is a $0 day! You have $2$3.</span><br>"
				+ "with the parameters: <span style='color:green'>\"fine\", \"Mr. Hunter\", '$', 10</span><br>"
				+ "will result in: <span style='color:green'>Hello Mr. Hunter, this is a fine day! You have $10.<span style='color:green'><br><br><hr>"
				+ "<b>HOTKEYS / MNEMONICS</b><br><br>"
				+ "Menus and buttons may have hotkeys / mnemonics. The hotkey character can be marked with an _ (underscore) sign before the intended character.<br>"
				+ "The hotkey indicators will be removed when displayed to the user.<br>"
				+ "The hotkeys are optional. For example if we want a <span style='color:green'>\"Visit home page\"</span> button to have the ALT+M hotkey, the following text is to be specified: <span style='color:green'>\"Visit ho_me page\"</span><br><br><hr>"
				+ "<b>ICONS</b><br><br>"
				+ "Some texts appear with icons in the texts table. The following icons are defined:<ul>"
				+ "<li><img border=0 src=\"" + Icons.HTML.resource + "\">&nbsp;This icon indicates that the text is specified as HTML text. This is the case if the text starts with <i>\"&lt;html&gt;\"</i>; and in this case it has to end with <i>\"&lt;/html&gt;\"</i>."
				+ "<li><img border=0 src=\"" + Icons.KEYBOARD.resource + "\">&nbsp;This icon indicates that the text contains a hotkey / mnemonic marker."
				+ "<li><img border=0 src=\"" + Icons.DOCUMENT_ATTRIBUTE_P.resource + "\">&nbsp;This icon indicates that the text contains parameters."
				+ "</ul>"
				+ "</html>" );
		notesEditorPane.setEditable( false );
		JScrollPane scrollPane = new JScrollPane( notesEditorPane );
		scrollPane.setPreferredSize( new Dimension( 10, 120 ) );
		northPanel.add( scrollPane, BorderLayout.CENTER );
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				notesEditorPane.scrollRectToVisible( new Rectangle( 0, 0, 1, 1 ) );
			}
		} );
		
		getContentPane().add( northPanel, BorderLayout.NORTH );
		
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
		
		final JPanel generalInfoPanel = new JPanel( new BorderLayout() );
		final Box box = Box.createVerticalBox();
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "translationTool.tab.generalInfo.languageFileVersion" ) ) );
		final JTextField languageFileVersionTextField = new JTextField( 20 );
		row.add( languageFileVersionTextField );
		final JButton setCurrentVersionButton = new JButton();
		GuiUtils.updateButtonText( setCurrentVersionButton, "translationTool.tab.generalInfo.setCurrentVersionButton" );
		row.add( setCurrentVersionButton );
		box.add( row );
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "translationTool.tab.generalInfo.languageFileSubversion" ) ) );
		final JTextField languageFileSubversionTextField = new JTextField( 20 );
		row.add( languageFileSubversionTextField );
		row.add( new JLabel() );
		setCurrentVersionButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				languageFileVersionTextField   .setText( Consts.APPLICATION_LANGUAGE_VERSION );
				languageFileSubversionTextField.setText( "1" );
			}
		} );
		box.add( row );
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "translationTool.tab.generalInfo.translatorFirstName" ) ) );
		final JTextField translatorFirstNameTextField = new JTextField( 20 );
		row.add( translatorFirstNameTextField );
		row.add( new JLabel() );
		box.add( row );
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "translationTool.tab.generalInfo.translatorLastName" ) ) );
		final JTextField translatorLastNameTextField = new JTextField( 20 );
		row.add( translatorLastNameTextField );
		row.add( new JLabel() );
		box.add( row );
		// Date/time formats
		final String dateTimeFormatToolTip = Language.getText( "miscSettings.customDateTimeFormatToolTip" );
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "translationTool.tab.generalInfo.dateFormat" ) ) );
		final JComboBox< String > dateFormatTextComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.CUSTOM_DATE_FORMAT, false );
		dateFormatTextComboBox.setToolTipText( dateTimeFormatToolTip );
		row.add( dateFormatTextComboBox );
		final JPanel dateControlPanel = new JPanel( new BorderLayout() );
		final JButton testDateButton = new JButton( Language.getText( "miscSettings.testFormatButton" ) );
		dateControlPanel.add( testDateButton, BorderLayout.CENTER );
		dateControlPanel.add( GuiUtils.createDateTimeFormatHelpLinkLabel(), BorderLayout.EAST );
		row.add( dateControlPanel );
		box.add( row );
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "translationTool.tab.generalInfo.timeFormat" ) ) );
		final JComboBox< String > timeFormatTextComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.CUSTOM_TIME_FORMAT, false );
		timeFormatTextComboBox.setToolTipText( dateTimeFormatToolTip );
		row.add( timeFormatTextComboBox );
		final JPanel timeControlPanel = new JPanel( new BorderLayout() );
		final JButton testTimeButton = new JButton( Language.getText( "miscSettings.testFormatButton" ) );
		timeControlPanel.add( testTimeButton, BorderLayout.CENTER );
		timeControlPanel.add( GuiUtils.createDateTimeFormatHelpLinkLabel(), BorderLayout.EAST );
		row.add( timeControlPanel );
		box.add( row );
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "translationTool.tab.generalInfo.dateTimeFormat" ) ) );
		final JComboBox< String > dateTimeFormatTextComboBox = GuiUtils.createPredefinedListComboBox( PredefinedList.CUSTOM_DATE_TIME_FORMAT, false );
		dateTimeFormatTextComboBox.setToolTipText( dateTimeFormatToolTip );
		row.add( dateTimeFormatTextComboBox );
		final JPanel dateTimeControlPanel = new JPanel( new BorderLayout() );
		final JButton testDateTimeButton = new JButton( Language.getText( "miscSettings.testFormatButton" ) );
		dateTimeControlPanel.add( testDateTimeButton, BorderLayout.CENTER );
		dateTimeControlPanel.add( GuiUtils.createDateTimeFormatHelpLinkLabel(), BorderLayout.EAST );
		row.add( dateTimeControlPanel );
		final ActionListener testDateTimeActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final String pattern = ( event.getSource() == testDateButton ? dateFormatTextComboBox
						: event.getSource() == testTimeButton ? timeFormatTextComboBox : dateTimeFormatTextComboBox ).getSelectedItem().toString();
				try {
					final String currentTime = new SimpleDateFormat( pattern ).format( new Date() );
					GuiUtils.showInfoDialog( new Object[] { Language.getText( "miscSettings.dateTimeFormatValid" ), " ", Language.getText( "miscSettings.currentDateTimeWithFormat" ), currentTime } );
				} catch ( IllegalArgumentException iae ) {
					iae.printStackTrace();
					GuiUtils.showErrorDialog( Language.getText( "miscSettings.dateTimeFormatInvalid" ) );
				}
			}
		};
		testDateButton    .addActionListener( testDateTimeActionListener );
		testTimeButton    .addActionListener( testDateTimeActionListener );
		testDateTimeButton.addActionListener( testDateTimeActionListener );
		box.add( row );
		row = Box.createHorizontalBox();
		row.add( new JLabel( Language.getText( "translationTool.tab.generalInfo.personNameFormat" ) ) );
		final JComboBox< String > personNameFormatComboBox = new JComboBox<>( new String[] { Language.getText( "translationTool.tab.generalInfo.personNameFormat.firstNameLastName" ), Language.getText( "translationTool.tab.generalInfo.personNameFormat.lastNameFirstName" ) } );
		row.add( personNameFormatComboBox );
		row.add( new JLabel() );
		box.add( row );
		GuiUtils.alignBox( box, 3 );
		generalInfoPanel.add( new JScrollPane( GuiUtils.wrapInPanel( box ) ), BorderLayout.CENTER );
		GuiUtils.addNewTab( Language.getText( "translationTool.tab.generalInfo.title" ), Icons.INFORMATION_BALLOON, false, tabbedPane, generalInfoPanel, null );
		
		final JPanel textsPanel = new JPanel( new BorderLayout() );
		final JProgressBar progressBar = new JProgressBar( 0, Language.DEFAULT_LANGUAGE.textMap.size() );
		progressBar.setStringPainted( true );
		textsPanel.add( progressBar, BorderLayout.NORTH );
		final JTable textsTable = new JTable() {
			// Custom cell renderer because we want icons and html source to be rendered...
			final DefaultTableCellRenderer customCellRenderer = new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column ) {
					super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
					Icon icon = null;
					if ( value != null && ( (String) value ).startsWith( "<html>" ) ) {
						icon = Icons.HTML;
						setText( ' ' + (String) value );
					}
					if ( value != null && ( (String) value ).indexOf( '$' ) >= 0 )
						icon = icon == null ? Icons.DOCUMENT_ATTRIBUTE_P : GuiUtils.concatenateIcons( icon, Icons.DOCUMENT_ATTRIBUTE_P );
					if ( value != null && ( (String) value ).indexOf( '_' ) >= 0 )
						icon = icon == null ? Icons.KEYBOARD : GuiUtils.concatenateIcons( icon, Icons.KEYBOARD );
					setIcon( icon );
					return this;
				}
			};
			@Override
			public boolean isCellEditable( final int row, final int column ) {
				return column == TRANSLATION_COLUMN;
			}
			int tipCounter = 0; // To generate unique tips so it will follow the mouse cursor
			@Override
			public String getToolTipText( final MouseEvent event ) {
				if ( columnAtPoint( event.getPoint() ) == TRANSLATION_COLUMN )
					return Language.getText( "translationTool.tab.texts.table.toolTip" ) + ( ( tipCounter++ & 0x01 ) == 1 ? " " : " " );
				
				return super.getToolTipText( event );
			}
			@Override
			public TableCellRenderer getCellRenderer( final int row, final int column ) {
				return customCellRenderer;
			}
		};
		textsTable.setAutoCreateRowSorter( true );
		textsTable.setColumnSelectionAllowed( true );
		textsTable.setShowVerticalLines( true );
		textsTable.setPreferredScrollableViewportSize( new Dimension( 850, 350 ) );
		textsTable.getTableHeader().setReorderingAllowed( false );
		( (DefaultTableModel) textsTable.getModel() ).setDataVector( new Vector< Vector< String > >(), TEXTS_HEADER_NAME_VECTOR );
		// Start editing for 1 click:
		( (DefaultCellEditor) textsTable.getDefaultEditor( textsTable.getColumnClass( TRANSLATION_COLUMN ) ) ).setClickCountToStart( 1 );
		// Gain focus and cursor when editing started due to typing
		textsTable.setSurrendersFocusOnKeystroke( true );
		final TableBox tableBox = new TableBox( textsTable, getLayeredPane(), null );
		tableBox.getFilterComponentsWrapper().add( Box.createHorizontalStrut( 10 ) );
		final JCheckBox showOnlyUntranslatedCheckBox = new JCheckBox( Language.getText( "translationTool.tab.texts.showOnlyUntranslated" ) );
		showOnlyUntranslatedCheckBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				tableBox.fireAdditionalRowFilterChanged();
			}
		} );
		tableBox.getFilterComponentsWrapper().add( showOnlyUntranslatedCheckBox );
		tableBox.getFilterComponentsWrapper().add( Box.createHorizontalStrut( 5 ) );
		final JButton previousButton = new JButton( Icons.ARROW_180 );
		GuiUtils.updateButtonText( previousButton, "translationTool.tab.texts.previousButton" );
		tableBox.getFilterComponentsWrapper().add( previousButton );
		previousButton.setToolTipText( Language.getText( "translationTool.tab.texts.previousButtonToolTip" ) );
		final JButton nextButton = new JButton( Icons.ARROW );
		nextButton.setHorizontalTextPosition( SwingConstants.LEFT );
		GuiUtils.updateButtonText( nextButton, "translationTool.tab.texts.nextButton" );
		nextButton.setToolTipText( Language.getText( "translationTool.tab.texts.nextButtonToolTip" ) );
		tableBox.getFilterComponentsWrapper().add( nextButton );
		final ActionListener prevNextActionListener = new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final int direction = event.getSource() == previousButton ? -1 : 1;
				// Visible rows count
				final int rowsCount = textsTable.getRowCount();
				if ( rowsCount == 0 )
					return; // No visible rows
				
				if ( textsTable.isEditing() )
					textsTable.getCellEditor().stopCellEditing();
				
				int start = textsTable.getSelectedRow();
				if ( start < 0 )
					start = 0;
				int i = start;
				do {
					i += direction;
					if ( i == rowsCount )
						i = 0;
					if ( i < 0 )
						i = rowsCount - 1;
					final String translation = (String) textsTable.getValueAt( i, TRANSLATION_COLUMN );
					if ( translation == null || translation.length() == 0 ) {
						// select row (and column)
						textsTable.getSelectionModel().setSelectionInterval( i, i );
						textsTable.setColumnSelectionInterval( TRANSLATION_COLUMN, TRANSLATION_COLUMN );
						textsTable.scrollRectToVisible( textsTable.getCellRect( i, TRANSLATION_COLUMN, true ) );
						break;
					}
				} while ( i != start );
				final String translation = (String) textsTable.getValueAt( i, TRANSLATION_COLUMN );
				if ( i == start && translation != null && translation.length() > 0 )
					GuiUtils.showInfoDialog( Language.getText( "translationTool.tab.texts.allDisplayedTextsAreTranslated" ) );
				textsTable.requestFocusInWindow();
			}
		};
		previousButton.addActionListener( prevNextActionListener );
		nextButton    .addActionListener( prevNextActionListener );
		tableBox.getFilterComponentsWrapper().add( new JLabel( "<html></html>" ) );
		textsPanel.add( tableBox, BorderLayout.CENTER );
		final Box southBox = Box.createVerticalBox();
		final JPanel previewPanel = new JPanel( new GridLayout( 1, 2 ) );
		final JEditorPane originalTextPreviewPane = new JEditorPane();
		originalTextPreviewPane.setEditable( false );
		scrollPane = new JScrollPane( originalTextPreviewPane );
		scrollPane.setBorder( BorderFactory.createTitledBorder( Language.getText( "translationTool.tab.texts.originalTextPreview" ) ) );
		scrollPane.setPreferredSize( new Dimension( 10, 140 ) );
		previewPanel.add( scrollPane );
		final JEditorPane translationPreviewPane = new JEditorPane();
		translationPreviewPane.setEditable( false );
		scrollPane = new JScrollPane( translationPreviewPane );
		scrollPane.setBorder( BorderFactory.createTitledBorder( Language.getText( "translationTool.tab.texts.translationPreview" ) ) );
		scrollPane.setPreferredSize( new Dimension( 10, 140 ) );
		previewPanel.add( scrollPane );
		southBox.add( previewPanel );
		final JTextArea commentTextArea = new JTextArea( 2, 1 );
		commentTextArea.setEditable( false );
		scrollPane = new JScrollPane( commentTextArea );
		scrollPane.setBorder( BorderFactory.createTitledBorder( Language.getText( "translationTool.tab.texts.comments" ) ) );
		southBox.add( scrollPane );
		textsPanel.add( southBox, BorderLayout.SOUTH );
		GuiUtils.addNewTab( Language.getText( "translationTool.tab.texts.title" ), Icons.BALLOONS, false, tabbedPane, textsPanel, null );
		
		getContentPane().add( tabbedPane, BorderLayout.CENTER );
		
		final Runnable updateProgressBarTask = new Runnable() {
			@Override
			public void run() {
				@SuppressWarnings( "unchecked" )
				final Vector< Vector< String > > dataVector = ( (DefaultTableModel) textsTable.getModel() ).getDataVector();
				
				int translatedCount = 0;
				for ( final Vector< String > row : dataVector ) {
					final String translation = row.get( TRANSLATION_COLUMN );
					if ( translation != null && translation.length() > 0 )
						translatedCount++;
				}
				progressBar.setValue( translatedCount );
				progressBar.setString( Language.getText( "translationTool.tab.texts.translationProgress", translatedCount, TOTAL_TEXTS_COUNT, 100 * translatedCount / TOTAL_TEXTS_COUNT ) );
			}
		};
		
		final Task< Holder< String > > updateTranslationPreviewTask = new Task< Holder< String > >() {
			/**
			 * @param translationHolder if provided, it will be used as the translation; else the translation from the table will be read (from the selected row)
			 */
			@Override
            public void execute( final Holder< String > translationHolder ) {
				int selectedRow = -1;
				if ( translationHolder != null || ( selectedRow = textsTable.getSelectedRow() ) >= 0 ) {
					String translation = translationHolder == null ? (String) textsTable.getValueAt( selectedRow, TRANSLATION_COLUMN ) : translationHolder.value;
					
					// If text has a mnemonic, show it as HTML text where the mnemonic is underlined:
					int underScoreIndex;
					if ( translation != null && ( underScoreIndex = translation.indexOf( '_' ) ) >= 0 && underScoreIndex < translation.length() - 1 )
						translation = "<html>" + translation.substring( 0, underScoreIndex ) + "<u>" + translation.charAt( underScoreIndex + 1 ) + "</u>" + translation.substring( underScoreIndex + 2 ) + "</html>";
					
					translationPreviewPane.setContentType( translation != null && translation.startsWith( "<html>" ) ? "text/html" : "text/plain" );
					translationPreviewPane.setText( translation  );
					SwingUtilities.invokeLater( new Runnable() {
						@Override
						public void run() {
							translationPreviewPane.scrollRectToVisible( new Rectangle( 0, 0, 1, 1 ) );
						}
					} );
				}
				else {
					translationPreviewPane.setContentType( "text/plain" );
					translationPreviewPane.setText( null );
				}
            }
		};
		
		textsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			@Override
			public void valueChanged( final ListSelectionEvent event ) {
				// Update translation preview
				updateTranslationPreviewTask.execute( null );
				
				// Update original text preview and comments
				final int selectedRow = textsTable.getSelectedRow();
				if ( selectedRow >= 0 ) {
					// Update original text preview
					String originalText = (String) textsTable.getValueAt( selectedRow, ORIGINAL_TEXT_COLUMN );
					
					// If text has a mnemonic, show it as HTML text where the mnemonic is underlined:
					int underScoreIndex;
					if ( originalText != null && ( underScoreIndex = originalText.indexOf( '_' ) ) >= 0 && underScoreIndex < originalText.length() - 1 )
						originalText = "<html>" + originalText.substring( 0, underScoreIndex ) + "<u>" + originalText.charAt( underScoreIndex + 1 ) + "</u>" + originalText.substring( underScoreIndex + 2 ) + "</html>";
					
					originalTextPreviewPane.setContentType( originalText != null && originalText.startsWith( "<html>" ) ? "text/html" : "text/plain" );
					originalTextPreviewPane.setText( originalText );
					SwingUtilities.invokeLater( new Runnable() {
						@Override
						public void run() {
							originalTextPreviewPane.scrollRectToVisible( new Rectangle( 0, 0, 1, 1 ) );
						}
					} );
					
					// Update comments
					commentTextArea.setText( null );
					final String textKey = (String) textsTable.getValueAt( selectedRow, TEXT_KEY_COLUMN );
					// Text group comments:
					boolean firstGroupComment = true;
					int dotIndex = 0;
					while ( ( dotIndex = textKey.indexOf( '.', dotIndex ) ) >= 0 ) {
						final String groupComment = Language.DEFAULT_LANGUAGE.textGroupCommentsMap.get( textKey.substring( 0, dotIndex ) );
						if ( groupComment != null ) {
    						if ( firstGroupComment )
    							firstGroupComment = false;
    						else
    							commentTextArea.append( " => " );
							commentTextArea.append( groupComment );
						}
						dotIndex++;
					}
					if ( !firstGroupComment )
						commentTextArea.append( "\n" );
					// Text comment
					commentTextArea.append( Language.DEFAULT_LANGUAGE.textCommentsMap.get( textKey ) ); // TextArea.append() ommits nulls
					commentTextArea.setCaretPosition( 0 );
				}
				else {
					originalTextPreviewPane.setContentType( "text/plain" );
					originalTextPreviewPane.setText( null );
					
					commentTextArea.setText( null );
				}
			}
		} );
		
		// Show real time preview while editing:
		( (JTextField) ( (DefaultCellEditor) textsTable.getDefaultEditor( textsTable.getColumnClass( TRANSLATION_COLUMN ) ) ).getComponent() ).getDocument().addDocumentListener( new DocumentListener() {
			@Override
			public void removeUpdate( final DocumentEvent event ) {
				changedUpdate( event );
			}
			@Override
			public void insertUpdate( final DocumentEvent event ) {
				changedUpdate( event );
			}
			@Override
			public void changedUpdate( final DocumentEvent event ) {
				try {
					final String translation = event.getDocument().getText( 0, event.getDocument().getLength() );
					updateTranslationPreviewTask.execute( new Holder< String >( translation ) );
				} catch ( final BadLocationException ble ) {
					ble.printStackTrace();
				}
			}
		} );
		
		textsTable.getModel().addTableModelListener( new TableModelListener() {
			@Override
			public void tableChanged( final TableModelEvent event ) {
				// event.getColumn() returns the column model index
				if ( event.getColumn() >= 0 && event.getColumn() == TRANSLATION_COLUMN ) {
					updateProgressBarTask.run();
					if ( showOnlyUntranslatedCheckBox.isSelected() ) {
						// If only untranslated texts are displayed, then after editing the current row will be hidden,
						// so the selected row have to be moved up by 1
						// (If the entered text will be empty, then this will have a side effect of not changing the selected row
						// but that's not really a problem, it's even the intended operation.)
						final int newSelectedRow = Math.max( 0, textsTable.getEditingRow() );
						tableBox.fireAdditionalRowFilterChanged();
						textsTable.getSelectionModel().setSelectionInterval( newSelectedRow, newSelectedRow );
					}
				}
			}
		} );
		
		editedLanguageComboBox.addActionListener( new ActionListener() {
			{ actionPerformed( null ); } // Initialize
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final String selectedLanguage = (String) editedLanguageComboBox.getSelectedItem();
				if ( selectedLanguage == null )
					return;
				// selectedLanguage can be null when the combo box is being rebuilt...
				final boolean isLanguageSelected = selectedLanguage != null && !EMPTY_LANGUAGE.equals( selectedLanguage );
				
				if ( isLanguageSelected ) {
					final Language language = Language.loadLanguage( selectedLanguage );
					if ( language == null ) {
						GuiUtils.showErrorDialog( Language.getText( "translationTool.failedToLoadLanguage", selectedLanguage ) );
						editedLanguageComboBox.setSelectedIndex( 0 );
						GuiUtils.setComponentTreeEnabled( tabbedPane, false );
						saveButton.setEnabled( false );
					}
					else {
						languageFileVersionTextField   .setText( language.languageFileVersion    );
						languageFileSubversionTextField.setText( language.languageFileSubversion );
						translatorFirstNameTextField   .setText( language.translatorFirstName    );
						translatorLastNameTextField    .setText( language.translatorLastName     );
						dateFormatTextComboBox         .setSelectedItem( language.defaultDateFormatPattern     );
						timeFormatTextComboBox         .setSelectedItem( language.defaultTimeFormatPattern     );
						dateTimeFormatTextComboBox     .setSelectedItem( language.defaultDateTimeFormatPattern );
						personNameFormatComboBox       .setSelectedIndex( language.personNameFormatFirstNameFirst ? 0 : 1 );
						
						final Vector< Vector< String > > dataVector      = new Vector< Vector<String> >( Language.DEFAULT_LANGUAGE.textMap.size() );
						final Map< String, String >      languageTextMap = language.textMap;
						for ( final Entry< String, String > textEntry : Language.DEFAULT_LANGUAGE.textMap.entrySet() ) {
							final Vector< String > row = new Vector< String >( 3 );
							
							final String textKey = textEntry.getKey();
							row.add( textKey );
							row.add( textEntry.getValue() );
							row.add( languageTextMap.get( textKey ) );
							
							dataVector.add( row );
						}
						
						( (DefaultTableModel) textsTable.getModel() ).setDataVector( dataVector, TEXTS_HEADER_NAME_VECTOR );
						textsTable.getRowSorter().setSortKeys( Arrays.asList( new SortKey( 0, SortOrder.ASCENDING ) ) );
						tableBox.setAdditionalRowFilter( new RowFilter< TableModel, Integer >() {
							@Override
							public boolean include( final Entry< ? extends TableModel, ? extends Integer > entry ) {
								if ( showOnlyUntranslatedCheckBox.isSelected() ) {
									final String translation = dataVector.get( entry.getIdentifier() ).get( TRANSLATION_COLUMN );
									return translation == null || translation.length() == 0;
								}
								else
									return true;
							}
						} );
						
						updateProgressBarTask.run();
						
						GuiUtils.setComponentTreeEnabled( tabbedPane, true );
						saveButton.setEnabled( true );
					}
				}
				else {
					GuiUtils.setComponentTreeEnabled( tabbedPane, false );
					saveButton.setEnabled( false );
				}
			}
		} );
		
		final ActionListener saveActionListener = new ActionListener() {
			/**
			 * If <code>event</code> is <code>null</code> a new language file will be saved!
			 */
			@Override
			public void actionPerformed( final ActionEvent event ) {
				if ( textsTable.isEditing() )
					textsTable.getCellEditor().stopCellEditing();
				
				try {
					final Document document    = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
					final Element  rootElement = document.createElement( "language" );
					rootElement.setAttribute( Language.VERSION_ATTRIBUTE_NAME              , event == null ? Consts.APPLICATION_LANGUAGE_VERSION : languageFileVersionTextField.getText() );
					rootElement.setAttribute( Language.SUBVERSION_ATTRIBUTE_NAME           , event == null ? "1" : languageFileSubversionTextField.getText() );
					rootElement.setAttribute( Language.TRANSLATOR_FIRST_NAME_ATTRIBUTE_NAME, event == null ? ""  : translatorFirstNameTextField   .getText() );
					rootElement.setAttribute( Language.TRANSLATOR_LAST_NAME_ATTRIBUTE_NAME , event == null ? ""  : translatorLastNameTextField    .getText() );
					
					final Element dateFormatElement = document.createElement( Language.DATE_FORMAT_TAG_NAME );
					dateFormatElement.setTextContent( event == null ? Language.DEFAULT_LANGUAGE.defaultDateFormatPattern : (String) dateFormatTextComboBox.getSelectedItem() );
					rootElement.appendChild( dateFormatElement );
					
					final Element timeFormatElement = document.createElement( Language.TIME_FORMAT_TAG_NAME );
					timeFormatElement.setTextContent( event == null ? Language.DEFAULT_LANGUAGE.defaultTimeFormatPattern : (String) timeFormatTextComboBox.getSelectedItem() );
					rootElement.appendChild( timeFormatElement );
					
					final Element dateTimeFormatElement = document.createElement( Language.DATE_TIME_FORMAT_TAG_NAME );
					dateTimeFormatElement.setTextContent( event == null ? Language.DEFAULT_LANGUAGE.defaultDateTimeFormatPattern : (String) dateTimeFormatTextComboBox.getSelectedItem() );
					rootElement.appendChild( dateTimeFormatElement );
					
					final Element personNameFormatElement = document.createElement( Language.PERSON_NAME_FORMAT_TAG_NAME );
					personNameFormatElement.setTextContent( event == null ? ( Language.DEFAULT_LANGUAGE.personNameFormatFirstNameFirst ? Language.PERSON_NAME_FORMAT_FIRST_NAME_LAST_NAME : Language.PERSON_NAME_FORMAT_LAST_NAME_FISRT_NAME ) : personNameFormatComboBox.getSelectedIndex() == 0 ? Language.PERSON_NAME_FORMAT_FIRST_NAME_LAST_NAME : Language.PERSON_NAME_FORMAT_LAST_NAME_FISRT_NAME );
					rootElement.appendChild( personNameFormatElement );
					
					if ( event != null ) {
						// Now add the texts.
						// First create the text groups
						// Create groups in the order of their node counts (for example "menu.file" has 2 nodes)
						final List< String > textGroupList = new ArrayList< String >( Language.DEFAULT_LANGUAGE.textGroupCommentsMap.keySet() );
						Collections.sort( textGroupList, new Comparator< String >() {
							@Override
							public int compare( final String g1, final String g2 ) {
								int c1 = 0;
								for ( int i = g1.length() - 2; i > 0; i-- ) // Cannot start or end with '.'
									if ( g1.charAt( i ) == '.' )
										c1++;
								int c2 = 0;
								for ( int i = g2.length() - 2; i > 0; i-- ) // Cannot start or end with '.'
									if ( g2.charAt( i ) == '.' )
										c2++;
								return c1 - c2;
							}
						} );
						final Map< String, Element > textGroupElementMap = new HashMap< String, Element >( textGroupList.size() ); // Store the elements mapped from text group keys
						for ( final String textGroup : textGroupList ) {
							// Check if there is a parent group
							Element parentElement = null;
							String  relativeKey   = null;
							int dotIndex = textGroup.length() - 1; // The last node will be cut off (the parent cannot have the same key, do not check it)
							while ( ( dotIndex = textGroup.lastIndexOf( '.', dotIndex ) ) >= 0 ) {
								parentElement = textGroupElementMap.get( textGroup.substring( 0, dotIndex ) );
								if ( parentElement != null ) {
									relativeKey = textGroup.substring( dotIndex + 1 );
									break;
								}
								dotIndex--;
							}
							
							final Element groupElement = document.createElement( Language.TEXT_GROUP_TAG_NAME );
							groupElement.setAttribute( Language.KEY_ATTRIBUTE_NAME, parentElement == null ? textGroup : relativeKey );
							( parentElement == null ? rootElement : parentElement ).appendChild( groupElement );
							
							textGroupElementMap.put( textGroup, groupElement );
						}
						
						// And finally create text elements
						@SuppressWarnings( "unchecked" )
						final Vector< Vector< String > > dataVector = ( (DefaultTableModel) textsTable.getModel() ).getDataVector();
						for ( final Vector< String > row : dataVector ) {
							final String translation = row.get( TRANSLATION_COLUMN );
							if ( translation == null || translation.length() == 0 )
								continue;
							
							final String key = row.get( TEXT_KEY_COLUMN );
							
							// Find "closest" parent text group
							Element parentElement = null;
							String  relativeKey   = null;
							int dotIndex = key.length() - 1; // The last node will be cut off (do not check the text key itself as a group key)
							while ( ( dotIndex = key.lastIndexOf( '.', dotIndex ) ) >= 0 ) {
								parentElement = textGroupElementMap.get( key.substring( 0, dotIndex ) );
								if ( parentElement != null ) {
									relativeKey = key.substring( dotIndex + 1 );
									break;
								}
								dotIndex--;
							}
							
							
							final Element textElement = document.createElement( Language.TEXT_TAG_NAME );
							textElement.setAttribute( Language.KEY_ATTRIBUTE_NAME, parentElement == null ? key : relativeKey );
							textElement.setTextContent( translation );
							( parentElement == null ? rootElement : parentElement ).appendChild( textElement );
						}
					}
					
					document.appendChild( rootElement );
					
					final Transformer transformer = TransformerFactory.newInstance().newTransformer();
					transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
					transformer.transform( new DOMSource( document ), new StreamResult( new FileOutputStream( Language.getLanguageFile( event == null ? newLanguageNameTextField.getText() : (String) editedLanguageComboBox.getSelectedItem() ) ) ) );
					
					if ( event != null )
						GuiUtils.showInfoDialog( Language.getText( "translationTool.changesSavedSuccessfully" ) );
					
				} catch ( final Exception e ) {
					e.printStackTrace();
					GuiUtils.showErrorDialog( Language.getText( "translationTool.failedToSaveTranslation" ) );
				}
			}
		};
		saveButton.addActionListener( saveActionListener );
		
		createNewLanguageButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				final String newLanguageName = newLanguageNameTextField.getText();
				if ( newLanguageName.length() == 0 )
					return;
				
				if ( newLanguageName.indexOf( ' ' ) >= 0 ) {
					GuiUtils.showErrorDialog( Language.getText( "translationTool.doNotUseSpacesInLanguageName" ) );
					return;
				}
				
				if ( Language.getLanguageFile( newLanguageName ).exists() ) {
					GuiUtils.showErrorDialog( Language.getText( "translationTool.languageAlreadyExists", newLanguageName ) );
					return;
				}
				
				saveActionListener.actionPerformed( null );
				
				rebuildEditedLanguageComboBoxTask.run();
				editedLanguageComboBox.setSelectedItem( newLanguageName );
				
				tabbedPane.setSelectedIndex( 0 );
				translatorFirstNameTextField.requestFocusInWindow();
			}
		} );
		
		maximizeWithMarginAndShow( 30, null, editedLanguageComboBox, true );
	}
	
}
