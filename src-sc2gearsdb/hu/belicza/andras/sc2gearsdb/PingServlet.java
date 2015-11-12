/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A ping servlet.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class PingServlet extends BaseServlet {
	
	static {
		ParsingServlet.loadResources();
	}
	
	/*
	 * doGet() is more common in case of this servlet, so doGet() calls doPost().
	 */
	@Override
	protected void doPost( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		doGet( request, response );
	}
	
	@Override
	protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		response.setContentType( "text/html" );
		// Set no-cache
		setNoCache( response );
		
		final PrintWriter out = response.getWriter();
		out.println( "<html><body>" );
		out.println( "<h3>Test! It works!</h3>" );
		out.println( "You requested:<br><code>" );
		out.println( request.getRequestURL() );
		out.println( "</code></body></html>" );
	}
	
}
