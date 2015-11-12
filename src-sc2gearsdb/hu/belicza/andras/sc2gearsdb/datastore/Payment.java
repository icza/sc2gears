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
 * Class representing a payment.
 * 
 * <p><b>Class format version history:</b>
 * <ol>
 * 		<li>Added the class format version <code>"v"</code> property.
 * </ol></p>
 * 
 * @author Andras Belicza
 */
@PersistenceCapable
public class Payment extends DataStoreObject {
	
	/** Key of the account. */
	@Persistent
	private Key accountKey;
	
	/** PayPal account if the payment was done via PayPal. */
	@Persistent
	private String paypalAccount;
	
	/** Bank account if the payment was done via bank transfer. */
	@Persistent
	private String bankAccount;
	
	/** Real payment made by the user in USD. */
	@Persistent
	private float realPayment;
	
	/** Virtual payment to be taken into account in USD. */
	@Persistent
	private float virtualPayment;
	
	/** Comment to the payment. */
	@Persistent
	private String comment;
	
	/**
	 * Creates a new Account.
	 * @param accountKey key of the account
	 */
	public Payment( final Key accountKey ) {
		this.accountKey = accountKey;
		setV( 1 );
	}
	
	public Key getAccountKey() {
		return accountKey;
	}

	public void setAccountKey(Key accountKey) {
		this.accountKey = accountKey;
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

	public float getRealPayment() {
		return realPayment;
	}

	public void setRealPayment(float realPayment) {
		this.realPayment = realPayment;
	}

	public float getVirtualPayment() {
		return virtualPayment;
	}

	public void setVirtualPayment(float virtualPayment) {
		this.virtualPayment = virtualPayment;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
