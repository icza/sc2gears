/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.user.client.beans;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class PaymentInfo implements IsSerializable {
	
	/** Payment registration date+time.            */
	private Date date;
	/** Either a PayPal account or a Bank account. */
	private String paymentSender;
	/** Virtual payment in USD.                    */
	private float payment;
	
	public void setDate( Date date ) {
	    this.date = date == null ? null : new Date( date.getTime() );
    }
	public Date getDate() {
	    return date;
    }
	public void setPaymentSender( String paymentSender ) {
	    this.paymentSender = paymentSender;
    }
	public String getPaymentSender() {
	    return paymentSender;
    }
	public void setPayment( float payment ) {
	    this.payment = payment;
    }
	public float getPayment() {
	    return payment;
    }
	
}
