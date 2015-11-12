/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.datastore;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Key;

/**
 * Class representing an API payment.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>"v"</code> property.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class ApiPayment extends DataStoreObject {
	
	/** Key of the API account. */
	@Persistent
	private Key apiAccountKey;
	
	/** PayPal account if the payment was done via PayPal. */
	@Persistent
	private String paypalAccount;
	
	/** Bank account if the payment was done via bank transfer. */
	@Persistent
	private String bankAccount;
	
	/** Net payment made by the user in US cent. */
	@Persistent
	private int netPayment;
	
	/** Gross payment made by the user in US cent. */
	@Persistent
	private int grossPayment;
	
	/** Ops purchased by this payment. */
	@Persistent
	private long paidOps;
	
	/** Comment to the payment. */
	@Persistent
	private String comment;
	
	/**
	 * Creates a new ApiAccount.
	 * @param apiAccountKey key of the API account
	 */
	public ApiPayment( final Key apiAccountKey ) {
		this.apiAccountKey = apiAccountKey;
		setV( 1 );
	}

	public Key getApiAccountKey() {
		return apiAccountKey;
	}

	public void setApiAccountKey(Key apiAccountKey) {
		this.apiAccountKey = apiAccountKey;
	}

	public String getPaypalAccount() {
		return paypalAccount;
	}

	public void setPaypalAccount(String paypalAccount) {
		this.paypalAccount = paypalAccount;
	}

	public String getBankAccount() {
		return bankAccount;
	}

	public void setBankAccount(String bankAccount) {
		this.bankAccount = bankAccount;
	}

	public void setNetPayment( int netPayment ) {
		this.netPayment = netPayment;
	}

	public int getNetPayment() {
		return netPayment;
	}

	public void setGrossPayment( int grossPayment ) {
		this.grossPayment = grossPayment;
	}

	public int getGrossPayment() {
		return grossPayment;
	}

	public void setPaidOps( long paidOps ) {
		this.paidOps = paidOps;
	}

	public long getPaidOps() {
		return paidOps;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
