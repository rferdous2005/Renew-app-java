package com.ssdtech.model;

import java.util.Date;

public class NotifyUserViaSMSInfo {
	public String id;
    public String mSISDN;
    public String subscriptionGroupID;
    public String originalSubscriptionGroupID;
    public String serviceDuration;
    public String renewNotificationDays;
    public String renewNotificationURL;
    public Date nextNotificationDate;
    public NotifyUserViaSMSInfo(String ID,String MSISDN, String SubscriptionGroupID, String OriginalSubscriptionGroupID, String ServiceDuration, String RenewNotificationDays, String RenewNotificationURL, Date NextNotificationDate)
    {
        this.id = ID;
        this.mSISDN = MSISDN;
        this.subscriptionGroupID = SubscriptionGroupID;
        this.originalSubscriptionGroupID = OriginalSubscriptionGroupID;
        this.serviceDuration = ServiceDuration;
        this.renewNotificationDays = RenewNotificationDays;
        this.renewNotificationURL = RenewNotificationURL;
        this.nextNotificationDate = NextNotificationDate;
    }
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the mSISDN
	 */
	public String getmSISDN() {
		return mSISDN;
	}
	/**
	 * @param mSISDN the mSISDN to set
	 */
	public void setmSISDN(String mSISDN) {
		this.mSISDN = mSISDN;
	}
	/**
	 * @return the subscriptionGroupID
	 */
	public String getSubscriptionGroupID() {
		return subscriptionGroupID;
	}
	/**
	 * @param subscriptionGroupID the subscriptionGroupID to set
	 */
	public void setSubscriptionGroupID(String subscriptionGroupID) {
		this.subscriptionGroupID = subscriptionGroupID;
	}
	/**
	 * @return the originalSubscriptionGroupID
	 */
	public String getOriginalSubscriptionGroupID() {
		return originalSubscriptionGroupID;
	}
	/**
	 * @param originalSubscriptionGroupID the originalSubscriptionGroupID to set
	 */
	public void setOriginalSubscriptionGroupID(String originalSubscriptionGroupID) {
		this.originalSubscriptionGroupID = originalSubscriptionGroupID;
	}
	/**
	 * @return the serviceDuration
	 */
	public String getServiceDuration() {
		return serviceDuration;
	}
	/**
	 * @param serviceDuration the serviceDuration to set
	 */
	public void setServiceDuration(String serviceDuration) {
		this.serviceDuration = serviceDuration;
	}
	/**
	 * @return the renewNotificationDays
	 */
	public String getRenewNotificationDays() {
		return renewNotificationDays;
	}
	/**
	 * @param renewNotificationDays the renewNotificationDays to set
	 */
	public void setRenewNotificationDays(String renewNotificationDays) {
		this.renewNotificationDays = renewNotificationDays;
	}
	/**
	 * @return the renewNotificationURL
	 */
	public String getRenewNotificationURL() {
		return renewNotificationURL;
	}
	/**
	 * @param renewNotificationURL the renewNotificationURL to set
	 */
	public void setRenewNotificationURL(String renewNotificationURL) {
		this.renewNotificationURL = renewNotificationURL;
	}
	/**
	 * @return the nextNotificationDate
	 */
	public Date getNextNotificationDate() {
		return nextNotificationDate;
	}
	/**
	 * @param nextNotificationDate the nextNotificationDate to set
	 */
	public void setNextNotificationDate(Date nextNotificationDate) {
		this.nextNotificationDate = nextNotificationDate;
	}
}
