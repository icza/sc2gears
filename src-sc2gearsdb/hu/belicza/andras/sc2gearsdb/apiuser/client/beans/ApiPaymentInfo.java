/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.apiuser.client.beans;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class ApiPaymentInfo implements IsSerializable {
	
	private Date date;
	private int grossPayment;
	private int netPayment;
	private long paidOps;
	
	/** Either a PayPal account or a Bank account. */
	private String paymentSender;
	
	public Date getDate() {
		return date;
	}
	
	public void setDate( Date date ) {
		this.date = date == null ? null : new Date( date.getTime() );
	}

	public void setGrossPayment( int grossPayment ) {
		this.grossPayment = grossPayment;
	}

	public int getGrossPayment() {
		return grossPayment;
	}

	public void setNetPayment( int netPayment ) {
		this.netPayment = netPayment;
	}

	public int getNetPayment() {
		return netPayment;
	}

	public void setPaidOps( long paidOps ) {
		this.paidOps = paidOps;
	}

	public long getPaidOps() {
		return paidOps;
	}

	public String getPaymentSender() {
		return paymentSender;
	}

	public void setPaymentSender(String paymentSender) {
		this.paymentSender = paymentSender;
	}
	
}
