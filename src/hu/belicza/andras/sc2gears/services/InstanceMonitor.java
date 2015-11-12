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

import hu.belicza.andras.sc2gears.settings.Settings;
import hu.belicza.andras.sc2gears.ui.MainFrame;
import hu.belicza.andras.sc2gears.util.NormalThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class is responsible for allowing only one instance if it is desired,
 * and to pass the arguments to the running instance if we're not the first one.
 * 
 * @author Andras Belicza
 */
public class InstanceMonitor extends NormalThread {
	
	/** Reference to the server socket used to force and check the only instance. */
	private final ServerSocket serverSocket;
	
	/**
	 * Creates a new InstanceListener and starts it.
	 * @param serverSocket reference to the server socket used to force and check the only instance
	 */
	private InstanceMonitor( final ServerSocket serverSocket ) {
		super( "Instance listener" );
		this.serverSocket = serverSocket;
		start();
	}
	
	/**
	 * If multiple instances are allowed, this method does nothing.
	 * If not and one instance is already running, brings it to front and passes the arguments. Terminates after that.
	 * If multiple instances are not allowed and none is running, takes the place of THE instance.
	 * 
	 * @param arguments arguments of the application
	 */
	public static void checkRunningInstance( final String[] arguments ) {
		if ( Settings.getBoolean( Settings.KEY_SETTINGS_ALLOW_ONLY_ONE_INSTANCE ) ) {
			final int PORT = 37894;
			try {
				// Check if there is already an instance running
				// We do this by trying to create a sever socket.
				// Reason: creating a server socket if another instance already took it fails much faster
				// than trying to connect to a non-listening port...
				final ServerSocket serverSocket = new ServerSocket( PORT, 0, InetAddress.getByName( "localhost" ) );
				
				// If server socket is created: there is no instance yet, we will be THE instance
				new InstanceMonitor( serverSocket );
				
			} catch ( final Exception e ) {
				// There is already a running instance, connect to it
				Socket      socket = null;
				PrintWriter output = null; 
				boolean argumentsPassed = false;
				try {
					socket = new Socket( "localhost", PORT );
					output = new PrintWriter( socket.getOutputStream(), true );
					output.println( arguments.length );
					
					for ( final String argument : arguments )
						output.println( new File( argument ).getAbsolutePath() );
					
					argumentsPassed = true;
				} catch ( final Exception e2 ) {
					e2.printStackTrace();
				} finally {
					if ( output != null )
						output.close();
					if ( socket != null )
						try { socket.close(); } catch ( final Exception e2 ) {}
				}
				
				if ( argumentsPassed )
					System.exit( 0 );
			}
		}
	}
	
	/**
	 * Continues to listen for potential new instances on the server port, and handles the passed arguments. 
	 */
	@Override
	public void run() {
		while ( true ) {
			Socket         inSocket = null;
			BufferedReader input    = null;
			
			try {
				inSocket = serverSocket.accept();
				
				MainFrame.INSTANCE.restoreMainFrame(); // Restores window even if minimized to system tray
				
				input = new BufferedReader( new InputStreamReader( inSocket.getInputStream() ) );
				final int argumentsCount = Integer.parseInt( input.readLine() );
				if ( argumentsCount > 0 ) {
					final String[] inArguments = new String[ argumentsCount ];
					for ( int i = 0; i < argumentsCount; i++ )
						inArguments[ i ] = input.readLine();
					MainFrame.INSTANCE.openArguments( inArguments );
				}
			} catch ( final Exception e ) {
				e.printStackTrace();
			} finally {
				if ( input != null )
					try { input.close(); } catch ( final Exception e ) {}
				if ( inSocket != null )
					try { inSocket.close(); } catch ( final Exception e ) {}
			}
		}
	}
	
}
