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

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class QuotaAndPaymentsInfo implements IsSerializable {
	
	private long paidOps;
	private long usedOps;
	private List< ApiPaymentInfo > apiPaymentInfoList;
	
	public void setPaidOps( long paidOps ) {
		this.paidOps = paidOps;
	}
	
	public long getPaidOps() {
		return paidOps;
	}
	
	public void setUsedOps( long usedOps ) {
		this.usedOps = usedOps;
	}
	
	public long getUsedOps() {
		return usedOps;
	}

	public void setApiPaymentInfoList( List< ApiPaymentInfo > apiPaymentInfoList ) {
		this.apiPaymentInfoList = apiPaymentInfoList;
	}

	public List< ApiPaymentInfo > getApiPaymentInfoList() {
		return apiPaymentInfoList;
	}
	
}
