package com.ssdtech.model;

import java.util.Date;

public class Notification {

	public String mSISDN;
    public String subscriptionGroupID;
    public String fromState;
    public String toState;
    public Date NextRenewalDate;
    public Date ExpiryDate;
    public Notification(String MSISDN, String SubscriptionGroupID, String FromState, String ToState,Date NextRenewalDate,Date ExpiryDate )
    {
        this.mSISDN = MSISDN;
        this.subscriptionGroupID = SubscriptionGroupID;
        this.fromState = FromState;
        this.toState = ToState;
        this.NextRenewalDate = NextRenewalDate;
        this.ExpiryDate = ExpiryDate;
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
	 * @return the fromState
	 */
	public String getFromState() {
		return fromState;
	}
	/**
	 * @param fromState the fromState to set
	 */
	public void setFromState(String fromState) {
		this.fromState = fromState;
	}
	/**
	 * @return the toState
	 */
	public String getToState() {
		return toState;
	}
	/**
	 * @param toState the toState to set
	 */
	public void setToState(String toState) {
		this.toState = toState;
	}   
	public Date getNextRenewalDate() {
		return NextRenewalDate;
	}
	
	public void setNextRenewalDate(Date nextrenewaldate) {
		this.NextRenewalDate = nextrenewaldate;
	}  
	
	public Date getExpiryDate() {
		return ExpiryDate;
	}
	
	public void setExpiryDate(Date expirydate) {
		this.ExpiryDate = expirydate;
	}  
}
