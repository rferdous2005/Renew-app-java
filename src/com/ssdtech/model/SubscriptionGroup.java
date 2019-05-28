package com.ssdtech.model;

public class SubscriptionGroup {
	public String SubscriptionGroupID;
	public String subid;
	public int ServiceDuration;
	public int RenewNotificationDays;
	public int GracePeriod;
	public String AllowDowngrade;
	public String DownGradeTo;
	public int FreeServicePeriod;
	public String OriginalSubscriptionGroupID;
	public int RetryRenewalPeriod;
	public int RetryRenewalIntervalMinutes;
	public String RenewNotificationURL;
	public int MaxRenewalDay;
	public int MaxGracePeriod;
	public String NextRetryPeriod;
	public String ServiceID;
	public String BNI;
	public String Remarks;
	public String cpID;


	/**
	 * @return the subscriptionGroupID
	 */
	public String getSubscriptionGroupID() {
		return SubscriptionGroupID;
	}
	/**
	 * @param subscriptionGroupID the subscriptionGroupID to set
	 */
	public void setSubscriptionGroupID(String subscriptionGroupID) {
		SubscriptionGroupID = subscriptionGroupID;
	}
	/**
	 * @return the serviceDuration
	 */
	public int getServiceDuration() {
		return ServiceDuration;
	}
	/**
	 * @param serviceDuration the serviceDuration to set
	 */
	public void setServiceDuration(int serviceDuration) {
		ServiceDuration = serviceDuration;
	}
	/**
	 * @return the renewNotificationDays
	 */
	public int getRenewNotificationDays() {
		return RenewNotificationDays;
	}
	/**
	 * @param renewNotificationDays the renewNotificationDays to set
	 */
	public void setRenewNotificationDays(int renewNotificationDays) {
		RenewNotificationDays = renewNotificationDays;
	}
	/**
	 * @return the gracePeriod
	 */
	public int getGracePeriod() {
		return GracePeriod;
	}
	/**
	 * @param gracePeriod the gracePeriod to set
	 */
	public void setGracePeriod(int gracePeriod) {
		GracePeriod = gracePeriod;
	}
	/**
	 * @return the allowDowngrade
	 */
	public String getAllowDowngrade() {
		return AllowDowngrade;
	}
	/**
	 * @param allowDowngrade the allowDowngrade to set
	 */
	public void setAllowDowngrade(String allowDowngrade) {
		AllowDowngrade = allowDowngrade;
	}
	/**
	 * @return the downGradeTo
	 */
	public String getDownGradeTo() {
		return DownGradeTo;
	}
	/**
	 * @param downGradeTo the downGradeTo to set
	 */
	public void setDownGradeTo(String downGradeTo) {
		DownGradeTo = downGradeTo;
	}
	/**
	 * @return the freeServicePeriod
	 */
	public int getFreeServicePeriod() {
		return FreeServicePeriod;
	}
	/**
	 * @param freeServicePeriod the freeServicePeriod to set
	 */
	public void setFreeServicePeriod(int freeServicePeriod) {
		FreeServicePeriod = freeServicePeriod;
	}
	/**
	 * @return the originalSubscriptionGroupID
	 */
	public String getOriginalSubscriptionGroupID() {
		return OriginalSubscriptionGroupID;
	}
	/**
	 * @param originalSubscriptionGroupID the originalSubscriptionGroupID to set
	 */
	public void setOriginalSubscriptionGroupID(String originalSubscriptionGroupID) {
		OriginalSubscriptionGroupID = originalSubscriptionGroupID;
	}
	/**
	 * @return the retryRenewalPeriod
	 */
	public int getRetryRenewalPeriod() {
		return RetryRenewalPeriod;
	}
	/**
	 * @param retryRenewalPeriod the retryRenewalPeriod to set
	 */
	public void setRetryRenewalPeriod(int retryRenewalPeriod) {
		RetryRenewalPeriod = retryRenewalPeriod;
	}
	/**
	 * @return the retryRenewalIntervalMinutes
	 */
	public int getRetryRenewalIntervalMinutes() {
		return RetryRenewalIntervalMinutes;
	}
	/**
	 * @param retryRenewalIntervalMinutes the retryRenewalIntervalMinutes to set
	 */
	public void setRetryRenewalIntervalMinutes(int retryRenewalIntervalMinutes) {
		RetryRenewalIntervalMinutes = retryRenewalIntervalMinutes;
	}
	/**
	 * @return the renewNotificationURL
	 */
	public String getRenewNotificationURL() {
		return RenewNotificationURL;
	}
	/**
	 * @param renewNotificationURL the renewNotificationURL to set
	 */
	public void setRenewNotificationURL(String renewNotificationURL) {
		RenewNotificationURL = renewNotificationURL;
	}
	public String getsubid()
	{
		return subid ; 
	}
	public void setsubid(String SubID)
	{
		subid=SubID;
	}
	
	/**
	 * @return the MaxRenewalDate
	 */
	public int getMaxRenewalDay() {
		return MaxRenewalDay;
	}
	/**
	 * @param MaxRenewalDate the MaxRenewalDate to set
	 */
	public void setMaxRenewalDay(int maxRenewalDay) {
		MaxRenewalDay = maxRenewalDay;
	}
	
	
	/**
	 * @return the MaxGracePeriod
	 */
	public int getMaxGracePeriod() {
		return MaxGracePeriod;
	}
	/**
	 * @param MaxGracePeriod the MaxGracePeriod to set
	 */
	public void setMaxGracePeriod(int maxGracePeriod) {
		MaxGracePeriod = maxGracePeriod;
	}
	
	
	/**
	 * @return the NextRetryPeriod
	 */
	public String getNextRetryPeriod() {
		return NextRetryPeriod;
	}
	/**
	 * @param MaxRenewalDate the NextRetryPeriod to set
	 */
	public void setNextRetryPeriod(String nextretryperiod) {
		NextRetryPeriod = nextretryperiod;
	}
	
	
	public String getServiceID() {
		return ServiceID;
	}
	
	public void setServiceID(String ServiceID) {
		ServiceID = ServiceID;
	}
	
	public String getBNI() {
		return BNI;
	}
	
	public void setBNI(String BNI) {
		BNI = BNI;
	}
	
	public String getRemarks() {
		return Remarks;
	}
	
	public void setRemarks(String Remarks) {
		Remarks = Remarks;
	}

	public String getCpID() {
		return this.cpID;
	}

	public void setCpID(String cpID) {
		this.cpID = cpID == null ? "" : cpID;
	}
}
