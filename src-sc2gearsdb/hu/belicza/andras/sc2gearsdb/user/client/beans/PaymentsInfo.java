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

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class PaymentsInfo implements IsSerializable {
	
	private List< PaymentInfo > paymentInfoList;
	private List< DbPackageInfo> dbPackageInfoList;
	private int currentDbPackageInfoIdx;
	private int firstBuyableDbPackageInfoIdx;

	public void setPaymentInfoList( List< PaymentInfo > paymentInfoList ) {
	    this.paymentInfoList = paymentInfoList;
    }

	public List< PaymentInfo > getPaymentInfoList() {
	    return paymentInfoList;
    }

	public void setDbPackageInfoList( List< DbPackageInfo> dbPackageInfoList ) {
	    this.dbPackageInfoList = dbPackageInfoList;
    }

	public List< DbPackageInfo> getDbPackageInfoList() {
	    return dbPackageInfoList;
    }

	public void setCurrentDbPackageInfoIdx( int currentDbPackageInfoIdx ) {
	    this.currentDbPackageInfoIdx = currentDbPackageInfoIdx;
    }

	public int getCurrentDbPackageInfoIdx() {
	    return currentDbPackageInfoIdx;
    }
	
	public void setFirstBuyableDbPackageInfoIdx( int firstBuyableDbPackageInfoIdx ) {
	    this.firstBuyableDbPackageInfoIdx = firstBuyableDbPackageInfoIdx;
    }

	public int getFirstBuyableDbPackageInfoIdx() {
	    return firstBuyableDbPackageInfoIdx;
    }

}
