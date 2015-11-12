/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.impl;

import hu.belicza.andras.sc2gears.language.Language;
import hu.belicza.andras.sc2gears.ui.icons.Icons;
import hu.belicza.andras.sc2gears.util.GeneralUtils;
import hu.belicza.andras.sc2gearspluginapi.api.listener.DiagnosticTestFactory;

import java.io.File;
import java.io.IOException;

import javax.swing.Icon;

/**
 * An abstract diagnostic test.
 * 
 * <p>The test logic must be implemented by overriding the {@link #execute()} method.</p>
 * 
 * <p>Contains helper methods which perform common test tasks and set the {@link #result} and {@link #details} attributes;
 * for example {@link #checkFileExist(File)}, {@link #checkFolderWritePermission(File)}, {@link #checkFreeSpace(File)}.</p>
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see DiagnosticTestFactory#createDiagnosticTest()
 */
public abstract class DiagnosticTest {
	
	/**
	 * Result of the execution of a diagnostic test.
	 * @author Andras Belicza
	 */
	public static enum Result {
		
		/** Error. Indicates that the test could not be executed. */
		ERROR  ( "diagnosticTool.result.error"  , Icons.BURN       , Icons.BURN            ),
		/** Fail. Indicates that the test failed.                 */
		FAIL   ( "diagnosticTool.result.fail"   , Icons.CROSS      , Icons.CROSS_BIG       ),
		/** Success with warnings.                                */
		WARNING( "diagnosticTool.result.warning", Icons.EXCLAMATION, Icons.EXCLAMATION_BIG ),
		/** Success.                                              */
		OK     ( "diagnosticTool.result.ok"     , Icons.TICK       , Icons.TICK_BIG        );
		
		/** Cache of the string value.           */
		public final String stringValue;
		/** Icon associated with the result.     */
		public final Icon icon;
		/** Big icon associated with the result. */
		public final Icon iconBig;
		
		/**
		 * Creates a new Result.
		 * @param textKey text key of the string value of the result
		 * @param icon    associated with the result
		 * @param iconBig big icon associated with the result
		 */
		private Result( final String textKey, final Icon icon, final Icon iconBig ) {
			this.stringValue = Language.getText( textKey );
			this.icon        = icon;
			this.iconBig     = iconBig;
		}
		
	}
	
	/** Free space quota in MB used by the {@link #checkFreeSpace(File)} method. Default value is 10 MB. */
	protected long freeSpaceQuotaMb = 10;
	
	/** Name of the test.                    */
	public final String name;
	/** Result of the execution of the test. */
	public Result       result;
	/** Details of the result.               */
	public String       details;
	
	/**
	 * Creates a new DiagnosticTest.
	 * @param name name of the diagnostic test
	 */
	public DiagnosticTest( final String name ) {
		this.name = name;
	}
	
	/**
	 * Executes the test. This method must be overridden to implement the test.
	 * 
	 * <p>Implementation is responsible to properly set the {@link #result} and the {@link #details} attributes
	 * to reflect the result of the test.<br>
	 * If the {@link #result} attribute is not set in this method, it will be treated as internal error ({@link Result#ERROR}).</p>
	 * 
	 * <p>The implementation should not throw any exceptions, errors should be indicated 
	 * by setting the {@link #result} attribute to {@link Result#ERROR}.
	 * Any thrown exception will be caught and treated as internal error ({@link Result#ERROR}).</p>
	 */
	public abstract void execute();
	
	/**
	 * Checks if the specified file exists.<br>
	 * If not, result is set to {@link Result#FAIL}, and a proper details message is set.
	 * @param file file to check
	 * @return true if the file exists; false otherwise
	 */
	protected boolean checkFileExist( final File file ) {
		if ( file.exists() )
			return true;
		
		result  = Result.FAIL;
		details = Language.getText( "diagnosticTool.wasNotFound", file.getAbsolutePath() );
		
		return false;
	}
	
	/**
	 * Checks if a user has write permission in a folder.<br>
	 * If not, result is set to {@link Result#FAIL}, and a proper details message is set.
	 * @param folder folder to test
	 * @return true if the user can write in the specified folder; false otherwise
	 */
	protected boolean checkFolderWritePermission( final File folder ) {
		final File testFile = GeneralUtils.generateUniqueName( new File( folder, "test" + (int) ( Math.random() * Integer.MAX_VALUE ) + ".txt" ) );
		try {
			if ( testFile.createNewFile() )
				return true;
		} catch ( final IOException ie ) {
		} finally {
			testFile.delete();
		}
		
		result  = Result.FAIL;
		details = Language.getText( "diagnosticTool.noPermissionInFolder", folder.getAbsolutePath() );
		
		return false;
	}
	
	/**
	 * Checks the free space on the partition specified by a folder.<br>
	 * If the free space is less than {@link #freeSpaceQuotaMb}, result is set to {@link Result#WARNING}, and a proper details message is set.
	 * @param folder folder whose partition to check
	 * @return true if free space is greater than a specified quota; false otherwise
	 */
	protected boolean checkFreeSpace( final File folder ) {
		if ( folder.getFreeSpace() > freeSpaceQuotaMb )
			return true;
		
		result  = Result.WARNING;
		details = Language.getText( "diagnosticTool.freeSpaceWarning", freeSpaceQuotaMb );
		
		return false;
	}
	
}

