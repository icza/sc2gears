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
import hu.belicza.andras.sc2gears.services.streaming.PrivateVideoStreaming;
import hu.belicza.andras.sc2gears.util.ControlledThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple HTTP server implementation.
 * 
 * @author Andras Belicza
 */
public class HttpServer extends ControlledThread {
	
	/** Server ID string to send in response headers. */
	public static final String SERVER_ID_STRING = Consts.APPLICATION_NAME + "/" + Consts.APPLICATION_VERSION + " " + "Private Video Streaming" + "/" + PrivateVideoStreaming.VERSION;
	
	/**
	 * Factory class to create HTTP client handlers.
	 * 
	 * @author Andras Belicza
	 */
	public static interface HttpClientHandlerFactory {
		/**
		 * Creates a new HTTP client handler.
		 * @param socket socket of the HTTP client to handle
		 */
		BaseHttpClientHandler createHttpClientHandler( Socket socket );
	}
	
	/** HTTP client handler factory. */
	private final HttpClientHandlerFactory httpClientHandlerFactory;
	/** The server socket.           */
	private final ServerSocket             ss;
	
	/** Executor service to serve clients using a thread pool. */
	private final ExecutorService          executorService;
	
	/**
	 * Creates a new HttpServer.
	 * 
	 * @param port                     port to listen on
	 * @param httpClientHandlerFactory HTTP client handler factory
	 * @throws IOException thrown if I/O error occurs when creating the server socket
	 */
	public HttpServer( final int port, final HttpClientHandlerFactory httpClientHandlerFactory ) throws IOException {
		super( "HTTP Server" );
		
		this.httpClientHandlerFactory = httpClientHandlerFactory;
		
		ss = new ServerSocket( port );
		
		executorService = Executors.newCachedThreadPool();
	}
	
	/**
	 * The new thread that waits for new clients.
	 */
	@Override
	public void run() {
		while ( !requestedToCancel )
			try {
				executorService.execute( httpClientHandlerFactory.createHttpClientHandler( ss.accept() ) );
			} catch ( final IOException ie ) {
				if ( !requestedToCancel )
					ie.printStackTrace();
			}
	}
	
	@Override
	public void requestToCancel() {
		super.requestToCancel();
		
		try {
			ss.close();
		} catch ( final IOException ie ) {
		}
		
		executorService.shutdown();
	}
	
}
