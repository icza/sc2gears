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

import hu.belicza.andras.sc2gearsdb.common.client.beans.DownloadParamsProvider;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Andras Belicza
 */
public class UserInfo implements DownloadParamsProvider, IsSerializable {
	
	private String userNickname;
	private String userName;
	private boolean hasAccount;
	private String loginUrl;
	private String logoutUrl;
	private Date  lastVisit;
	
	private Map< String, String > downloadParameterMap;
	private Map< String, String > batchDownloadParameterMap;
	
	private String fileTypeParamName;
	private String sha1ParamName;
	private String sha1ListParamName;
	private String sharedAccountParamName;
	
	private String replayFileType;
	private String mousePrintFileType;
	private String otherFileType;
	
	private List< String > labelNames;
	private String[] labelColors;
	private String[] labelBgColors;
	
	private boolean convertToRealTime;
	private int mapImageSize;
	private int displayWinners;
	private List< String > favoredPlayerList;
	private boolean freeAccount;
	
	private QuotaStatus quotaStatus;
	
	private boolean admin;
	
	/** List of accounts who granted us access to their accounts. The first element is always us. */
	private List< String > sharedAccounts;
	
	/** Google account of the Database account in case we're viewing a shared account. */
	private String sharedAccount;
	/** Granted permissions in case we're viewing a shared account. */
	private long grantedPermissions;
	
	
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
	public void setHasAccount(boolean hasAccount) {
		this.hasAccount = hasAccount;
	}
	public boolean isHasAccount() {
		return hasAccount;
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
		this.lastVisit = lastVisit == null ? null : new Date( lastVisit.getTime() );
	}
	public Date getLastVisit() {
		return lastVisit;
	}
	public void setDownloadParameterMap(Map< String, String > downloadParameterMap) {
		this.downloadParameterMap = downloadParameterMap;
	}
	public Map< String, String > getDownloadParameterMap() {
		return downloadParameterMap;
	}
	public void setBatchDownloadParameterMap(
			Map< String, String > batchDownloadParameterMap) {
		this.batchDownloadParameterMap = batchDownloadParameterMap;
	}
	public Map< String, String > getBatchDownloadParameterMap() {
		return batchDownloadParameterMap;
	}
	public void setFileTypeParamName(String fileTypeParamName) {
		this.fileTypeParamName = fileTypeParamName;
	}
	public String getFileTypeParamName() {
		return fileTypeParamName;
	}
	public void setReplayFileType(String replayFileType) {
		this.replayFileType = replayFileType;
	}
	public void setSha1ParamName(String sha1ParamName) {
		this.sha1ParamName = sha1ParamName;
	}
	public String getSha1ParamName() {
		return sha1ParamName;
	}
	public void setSha1ListParamName(String sha1ListParamName) {
		this.sha1ListParamName = sha1ListParamName;
	}
	public String getSha1ListParamName() {
		return sha1ListParamName;
	}
	public void setSharedAccountParamName(String sharedAccountParamName) {
		this.sharedAccountParamName = sharedAccountParamName;
	}
	public String getSharedAccountParamName() {
		return sharedAccountParamName;
	}
	public String getReplayFileType() {
		return replayFileType;
	}
	public void setMousePrintFileType(String mousePrintFileType) {
		this.mousePrintFileType = mousePrintFileType;
	}
	public String getMousePrintFileType() {
		return mousePrintFileType;
	}
	public void setOtherFileType(String otherFileType) {
		this.otherFileType = otherFileType;
	}
	public String getOtherFileType() {
		return otherFileType;
	}
	public void setLabelNames( List< String > labelNamess ) {
		this.labelNames = labelNamess;
	}
	public List< String > getLabelNames() {
		return labelNames;
	}
	public void setLabelColors( String[] labelColors ) {
		this.labelColors = labelColors;
	}
	public String[] getLabelColors() {
		return labelColors;
	}
	public void setLabelBgColors( String[] labelBgColors ) {
		this.labelBgColors = labelBgColors;
	}
	public String[] getLabelBgColors() {
		return labelBgColors;
	}
	public void setConvertToRealTime( boolean convertToRealTime ) {
		this.convertToRealTime = convertToRealTime;
	}
	public boolean isConvertToRealTime() {
		return convertToRealTime;
	}
	public void setMapImageSize( int mapImageSize ) {
		this.mapImageSize = mapImageSize;
	}
	public int getMapImageSize() {
		return mapImageSize;
	}
	public void setFreeAccount( boolean freeAccount ) {
		this.freeAccount = freeAccount;
	}
	public boolean isFreeAccount() {
		return freeAccount;
	}
	public void setSharedAccounts( List< String > sharedAccounts ) {
		this.sharedAccounts = sharedAccounts;
	}
	public List< String > getSharedAccounts() {
		return sharedAccounts;
	}
	public void setSharedAccount(String sharedAccount) {
		this.sharedAccount = sharedAccount;
	}
	public String getSharedAccount() {
		return sharedAccount;
	}
	public void setGrantedPermissions(long grantedPermissions) {
		this.grantedPermissions = grantedPermissions;
	}
	public long getGrantedPermissions() {
		return grantedPermissions;
	}
	public void setAdmin( boolean admin ) {
	    this.admin = admin;
    }
	public boolean isAdmin() {
	    return admin;
    }
	public void setFavoredPlayerList( List< String > favoredPlayerList ) {
	    this.favoredPlayerList = favoredPlayerList;
    }
	public List< String > getFavoredPlayerList() {
	    return favoredPlayerList;
    }
	public void setDisplayWinners( int displayWinners ) {
	    this.displayWinners = displayWinners;
    }
	public int getDisplayWinners() {
	    return displayWinners;
    }
	public void setQuotaStatus( QuotaStatus quotaStatus ) {
	    this.quotaStatus = quotaStatus;
    }
	public QuotaStatus getQuotaStatus() {
	    return quotaStatus;
    }
	
}
