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

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * An {@link AsyncCallback} adapter which handles the animated loading gif feature of an info panel to display to the user
 * that the call is in progress.
 * 
 * @param <T> return value type included in the {@link RpcResult}
 * 
 * @author Andras Belicza
 */
public abstract class AsyncCallbackAdapter< T > implements AsyncCallback< RpcResult< T > > {
	
	/** Reference to the info panel to update on success or on failure. */
	private final InfoPanel infoPanel;
	
	/**
	 * Creates a new AsyncCallbackAdapter.<br>
	 * Initially sets the "work-in-progress" state to true.
	 * @param infoPanel reference to the info panel to update on success or on failure
	 */
	public AsyncCallbackAdapter( final InfoPanel infoPanel ) {
		this( infoPanel, true );
	}
	
	/**
	 * Creates a new AsyncCallbackAdapter.
	 * @param infoPanel reference to the info panel to update on success or on failure
	 * @param loading the "work-in-progress" state
	 */
	public AsyncCallbackAdapter( final InfoPanel infoPanel, final boolean loading ) {
		this.infoPanel = infoPanel;
		infoPanel.setLoading( loading );
	}
	
	/**
	 * Sets the "work-in-progress" state.
	 * @param loading the "work-in-progress" state
	 */
	public void setLoading( final boolean loading ) {
		infoPanel.setLoading( loading );
	}
	
	@Override
	public final void onSuccess( final RpcResult< T > result ) {
		infoPanel.setLoading( false );
		
		if ( result.getErrorMsg() != null )
			infoPanel.setErrorMessage( result.getErrorMsg() );
		else if ( result.getInfoMsg() != null )
			infoPanel.setInfoMessage( result.getInfoMsg() );
		
		customOnSuccess( result );
		customOnEnd();
	}
	
	@Override
	public final void onFailure( final Throwable caught ) {
		infoPanel.setLoading( false );
		customOnFailure( caught );
		customOnEnd();
	}
	
	/**
	 * Custom onSuccess() method.
	 * @see AsyncCallback#onSuccess(T)
	 */
	public abstract void customOnSuccess( final RpcResult< T > result );
	
	/**
	 * Custom onSuccess() method.<br>
	 * This default implementation sets {@link Throwable#getMessage()} as the error message at the {@link #infoPanel}.
	 * @see AsyncCallback#onFailure(Throwable)
	 */
	public void customOnFailure( final Throwable caught ) {
		infoPanel.setErrorMessage( "Error: " + caught.getMessage() );
	}
	
	/**
	 * Custom onEnd() method.<br>
	 * This method is called at the end of both {@link #onSuccess(T)} and {@link #onFailure(Throwable)}.<br>
	 * This default implementation does nothing.
	 */
	public void customOnEnd() {
	}
	
}
