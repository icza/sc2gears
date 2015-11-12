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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * RPC result type.
 * 
 * @param <T> return value type produced by the RPC
 * 
 * @author Andras Belicza
 */
public class RpcResult< T > implements IsSerializable {
	
	/** Error message provided by the RPC call. */
	private String errorMsg;
	/** Info message provided by the RPC call.  */
	private String infoMsg;
	/** Return value produced by the RPC.       */
	private T      value;
	
	public static < T > RpcResult< T > createErrorResult( final String errorMsg ) {
		final RpcResult< T > rpcResult = new RpcResult< T >();
		rpcResult.setErrorMsg( errorMsg );
		return rpcResult;
	}
	
	public static < T > RpcResult< T > createNotLoggedInErrorResult() {
		return createErrorResult( "You are not logged in!" );
	}
	
	public static < T > RpcResult< T > createNoPermissionErrorResult() {
		return createErrorResult( "You have no permission!" );
	}
	
	public static < T > RpcResult< T > createInfoResult( final String infoMesg ) {
		final RpcResult< T > rpcResult = new RpcResult< T >();
		rpcResult.setInfoMsg( infoMesg );
		return rpcResult;
	}
	
	/**
	 * Creates a new RpcResult.
	 */
	public RpcResult() {
	}
	
	/**
	 * Creates a new RpcResult.
	 * @param value return value produced by the RPC
	 */
	public RpcResult( final T value ) {
		this.setValue( value );
	}

	public void setErrorMsg( String errorMsg ) {
		this.errorMsg = errorMsg;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setInfoMsg( String infoMsg ) {
		this.infoMsg = infoMsg;
	}

	public String getInfoMsg() {
		return infoMsg;
	}

	public void setValue( T value ) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}
	
}
