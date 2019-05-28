package com.ssdtech.model;
import java.util.Date;


public class UserInfo {
	
	    private long id;
	 
	    private String msisdn = "";
	    private String subscriptionGroupID = "";
	    private String status = "";
	    private Date registrationDate;
	    private String downgradedSubscriptionGroupID = "";    
	    private Date chargingDueDate;
	    private Date nextNotificationDate;
	    private Date nextRenewalDate;
	    private String bNI;
	    private String serviceName;
	    private String originalSubscriptionGroupID;
	    private Date renewRequestTime;
	    private String aquisitionChannel;
	    private String autorenew;
	    private String consent;
	    private Date ConsentDeadline;
	    private String Remarks;
	    public int pickerNo;
	    
	    public Date getNextRenewalDate() {
			return nextRenewalDate;
		}


		public void setNextRenewalDate(Date nextRenewalDate) {
			this.nextRenewalDate = nextRenewalDate;
		}	    

		public UserInfo()
		{
			//
			// TODO: Add constructor logic here
			//
		}


		public long getId() {
			return id;
		}


		public void setId(long id) {
			this.id = id;
		}


		public String getMsisdn() {
			return msisdn;
		}


		public void setMsisdn(String msisdn) {
			this.msisdn = msisdn;
		}


		public String getSubscriptionGroupID() {
			return subscriptionGroupID;
		}


		public void setSubscriptionGroupID(String subscriptionGroupID) {
			this.subscriptionGroupID = subscriptionGroupID;
		}


		public String getStatus() {
			return status;
		}


		public void setStatus(String status) {
			this.status = status;
		}


		public Date getRegistrationDate() {
			return registrationDate;
		}


		public void setRegistrationDate(Date registrationDate) {
			this.registrationDate = registrationDate;
		}


		public String getDowngradedSubscriptionGroupID() {
			return downgradedSubscriptionGroupID;
		}


		public void setDowngradedSubscriptionGroupID(
				String downgradedSubscriptionGroupID) {
			this.downgradedSubscriptionGroupID = downgradedSubscriptionGroupID;
		}


		public Date getChargingDueDate() {
			return chargingDueDate;
		}


		public void setChargingDueDate(Date chargingDueDate) {
			this.chargingDueDate = chargingDueDate;
		}
		
		public Date getNextNotificationDate() {
			return nextNotificationDate;
		}


		public void setNextNotificationDate(Date nextNotificationDate) {
			this.nextNotificationDate = nextNotificationDate;
		}


		public String getbNI() {
			return bNI;
		}


		public void setbNI(String bNI) {
			this.bNI = bNI;
		}


		public String getServiceName() {
			return serviceName;
		}


		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}


		public String getOriginalSubscriptionGroupID() {
			return originalSubscriptionGroupID;
		}


		public void setOriginalSubscriptionGroupID(String originalSubscriptionGroupID) {
			this.originalSubscriptionGroupID = originalSubscriptionGroupID;
		}
		
		public Date getRenewRequestTime() {
			return renewRequestTime;
		}


		public void setRenewRequestTime(Date renewRequestTime) {
			this.renewRequestTime = renewRequestTime;
		}


		public String getAquisitionChannel() {
			return aquisitionChannel;
		}


		public void setAquisitionChannel(String aquisitionChannel) {
			this.aquisitionChannel = aquisitionChannel;
		}
		
		public String getautorenew() {
			return autorenew;
		}


		public void setautorenew(String autorenew) {
			this.autorenew = autorenew;
		}
		
		public String getconsent() {
			return consent;
		}


		public void setconsent(String consent) {
			this.consent = consent;
		}
		
		public Date getConsentDeadline() {
			return ConsentDeadline;
		}


		public void setConsentDeadline(Date ConsentDeadline) {
			this.ConsentDeadline = ConsentDeadline;
		}
		
		public String getRemarks() {
			return Remarks;
		}


		public void setRemarks(String Remarks) {
			this.Remarks = Remarks;
		}

		
}
