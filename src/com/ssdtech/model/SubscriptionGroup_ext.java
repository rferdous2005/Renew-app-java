package com.ssdtech.model;

public class SubscriptionGroup_ext {

	public String SubscriptionGroupID;
	public int InGracePeriodSuffix;
	public String User;
	public String Subid;
	
	

	public String getSubscriptionGroupID() {
		return SubscriptionGroupID;
	}
	
	public void setSubscriptionGroupID(String subscriptionGroupID) {
		SubscriptionGroupID = subscriptionGroupID;
	}
	
	public int getInGracePeriodSuffix() {
		return InGracePeriodSuffix;
	}
	
	public void setInGracePeriodSuffix(int inGracePeriodSuffix) {
		this.InGracePeriodSuffix = inGracePeriodSuffix;
	}
	
	public String getuser() {
		return User;
	}
	
	public void setuser(String user) {
		User = user;
	}
	
	public String getsubid()
	{
		return Subid ; 
	}
	public void setsubid(String subID)
	{
		Subid=subID;
	}

	
}
