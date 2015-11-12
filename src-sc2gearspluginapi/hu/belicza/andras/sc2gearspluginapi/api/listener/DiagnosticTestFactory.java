/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearspluginapi.api.listener;

import hu.belicza.andras.sc2gearspluginapi.api.CallbackApi;
import hu.belicza.andras.sc2gearspluginapi.impl.DiagnosticTest;

/**
 * A factory interface to create diagnostic tests.
 * 
 * @since "2.0"
 * 
 * @author Andras Belicza
 * 
 * @see CallbackApi#addDiagnosticTestFactory(DiagnosticTestFactory)
 * @see CallbackApi#removeDiagnosticTestFactory(DiagnosticTestFactory)
 */
public interface DiagnosticTestFactory {
	
	/**
	 * Creates and returns a diagnostic test.
	 * 
	 * <p>Implementation cannot return the same diagnostic test instance for multiple calls,
	 * a new instance has to be returned each time the method is called.</p>
	 * 
	 * @return the created diagnostic test
	 * @see DiagnosticTest
	 */
	DiagnosticTest createDiagnosticTest();
	
}
