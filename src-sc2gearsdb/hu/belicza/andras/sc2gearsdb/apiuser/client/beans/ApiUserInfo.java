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
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class ApiUserInfo implements IsSerializable {
	
	private String userNickname;
	private String userName;
	private boolean hasApiAccount;
	private String loginUrl;
	private String logoutUrl;
	private Date  lastVisit;
	private String repParserEngineVer;
	
	private boolean admin;
	
	/** Google account to access in case we're viewing someone else's account; <code>null</code> otherwise. */
	private String sharedApiAccount;
	
	/** List of shared API accounts. The first element is always us. */
	private List< String > sharedAccounts;
	
	public void setUserNickname( String userNickname ) {
	    this.userNickname = userNickname;
    }
	public String getUserNickname() {
	    return userNickname;
    }
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public void setHasApiAccount(boolean hasApiAccount) {
		this.hasApiAccount = hasApiAccount;
	}
	public boolean isHasApiAccount() {
		return hasApiAccount;
	}
	public String getLoginUrl() {
		return loginUrl;
	}
	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}
	public String getLogoutUrl() {
		return logoutUrl;
	}
	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}
	public void setLastVisit(Date lastVisit) {
		this.lastVisit = new Date( lastVisit.getTime() );
	}
	public Date getLastVisit() {
		return lastVisit;
	}
	public String getRepParserEngineVer() {
		return repParserEngineVer;
	}
	public void setRepParserEngineVer(String repParserEngineVer) {
		this.repParserEngineVer = repParserEngineVer;
	}
	public String getSharedApiAccount() {
		return sharedApiAccount;
	}
	public void setSharedApiAccount(String sharedApiAccount) {
		this.sharedApiAccount = sharedApiAccount;
	}
	public boolean isAdmin() {
		return admin;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	public List< String > getSharedAccounts() {
		return sharedAccounts;
	}
	public void setSharedAccounts(List< String > sharedAccounts) {
		this.sharedAccounts = sharedAccounts;
	}
	
}
