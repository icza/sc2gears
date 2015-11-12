/*
 * Project Sc2gears
 * 
 * Copyright (c) 2010 Andras Belicza <iczaaa@gmail.com>
 * 
 * This software is the property of Andras Belicza.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package hu.belicza.andras.sc2gearsdb.admin.client.beans;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class PaymentInfo implements IsSerializable {
	
	private boolean apiPayment;
	private String googleAccount;
	private String paypalAccount;
	private String bankAccount;
	private Float realPayment;
	private Float virtualPayment;
	private Integer apiGrossPayment;
	private Integer apiNetPayment;
	private String comment;
	
	public void setApiPayment( boolean apiPayment ) {
	    this.apiPayment = apiPayment;
    }
	public boolean isApiPayment() {
	    return apiPayment;
    }
	public String getGoogleAccount() {
		return googleAccount;
	}
	public void setGoogleAccount(String googleAccount) {
		this.googleAccount = googleAccount;
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
	public Float getRealPayment() {
		return realPayment;
	}
	public void setRealPayment(Float realPayment) {
		this.realPayment = realPayment;
	}
	public Float getVirtualPayment() {
		return virtualPayment;
	}
	public void setVirtualPayment(Float virtualPayment) {
		this.virtualPayment = virtualPayment;
	}
	public void setApiGrossPayment( Integer apiGrossPayment ) {
	    this.apiGrossPayment = apiGrossPayment;
    }
	public Integer getApiGrossPayment() {
	    return apiGrossPayment;
    }
	public void setApiNetPayment( Integer apiNetPayment ) {
	    this.apiNetPayment = apiNetPayment;
    }
	public Integer getApiNetPayment() {
	    return apiNetPayment;
    }
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

}
