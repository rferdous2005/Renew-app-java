package com.ssdtech.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

public class GlobalDatabaseData {
    private Hashtable<String, SubscriptionGroup> subscriptionGroups;
    private Hashtable<String, SubscriptionGroup_ext> subscriptionGroupExtensions;
    private Hashtable<String, ArrayList<StateWiseMessage>> stateWiseMessages;
    
    public void loadSubscriptionGroups(Connection dbConnection) {
    	this.subscriptionGroups = new Hashtable<String, SubscriptionGroup>();
    	System.out.println("Loading Subscription groups");
    	
    	String query = "SELECT "
    			     + "AllowDowngrade, DownGradeTo, FreeServicePeriod, GracePeriod, "
    			     + "OriginalSubscriptionGroupID, RenewNotificationURL, SubscriptionGroupID, "
    			     + "RenewNotificationDays, RetryRenewalIntervalMinutes, RetryRenewalPeriod, "
    			     + "ServiceDuration, MaxRenewalDay, MaxGracePeriod, NextRetryPeriod, "
    			     + "ServiceID, BNI, Remarks, cpid "
    			     + "FROM subscriptiongroup";
		try {
			Statement statement = dbConnection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				SubscriptionGroup sg = new SubscriptionGroup();
				sg.AllowDowngrade = resultSet.getString("AllowDowngrade");
				if (sg.AllowDowngrade == null)
					sg.AllowDowngrade = "";
				sg.DownGradeTo = resultSet.getString("DownGradeTo");
				if (sg.DownGradeTo == null)
					sg.DownGradeTo = "";
				
				sg.FreeServicePeriod = resultSet.getInt("FreeServicePeriod");
				sg.GracePeriod = resultSet.getInt("GracePeriod");
				
				sg.OriginalSubscriptionGroupID = resultSet
						.getString("OriginalSubscriptionGroupID");
				sg.RenewNotificationURL = resultSet
						.getString("RenewNotificationURL");
				
				String tempsubid = resultSet.getString("SubscriptionGroupID");
				sg.SubscriptionGroupID = tempsubid;
				sg.subid = tempsubid.toLowerCase();
				
				sg.RenewNotificationDays = resultSet.getInt("RenewNotificationDays");
				sg.RetryRenewalIntervalMinutes = resultSet.getInt("RetryRenewalIntervalMinutes");
				sg.RetryRenewalPeriod = resultSet.getInt("RetryRenewalPeriod");
				sg.ServiceDuration = resultSet.getInt("ServiceDuration");
				sg.MaxRenewalDay = resultSet.getInt("MaxRenewalDay");
				sg.MaxGracePeriod = resultSet.getInt("MaxGracePeriod");
				sg.NextRetryPeriod = resultSet.getString("NextRetryPeriod");
				if (sg.NextRetryPeriod == null)
					sg.NextRetryPeriod = "";
				
				sg.ServiceID = resultSet.getString("ServiceID");
				if (sg.ServiceID == null)
					sg.ServiceID = "";
				
				sg.BNI = resultSet.getString("BNI");
				if (sg.BNI == null)
					sg.BNI = "";
				sg.setCpID(resultSet.getString("cpid"));
				sg.Remarks = resultSet.getString("Remarks");
				if (sg.Remarks == null)
					sg.Remarks = "";

				this.subscriptionGroups.put(sg.subid, sg);
			}
			resultSet.close();
			statement.close();
		} catch (Exception ex) {
		}
    }
    
    public void loadSubscriptionGroup_extTable(Connection connection) {
		this.subscriptionGroupExtensions = new Hashtable<String, SubscriptionGroup_ext>();
		System.out.println("Loading Subscription_ext table");
		String query = "select * from subscriptiongroup_ext";
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				SubscriptionGroup_ext sg1 = new SubscriptionGroup_ext();

				sg1.InGracePeriodSuffix = resultSet.getInt("InGracePeriodSuffix");
				sg1.User = resultSet.getString("user");

				String tempsubid = resultSet.getString("SubscriptionGroupID");
				sg1.SubscriptionGroupID = tempsubid;
				sg1.Subid = tempsubid.toLowerCase();

				this.subscriptionGroupExtensions.put(sg1.Subid, sg1);
			}
			resultSet.close();
			statement.close();
		} catch (Exception ex) {
		}
	}
    
    public void loadStateWiseMessageTable(Connection connection) {
    	this.stateWiseMessages = new Hashtable<String, ArrayList<StateWiseMessage>>();
    	System.out.println("Loading StateWiseMsg table");
    	String query = "SELECT FromState, ToState, SubscriptionGroupID, URL, Msg "
    			     + "FROM statewisemsg WHERE NotificationStatus='Active'";
    	
    	try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				String fromState = resultSet.getString("FromState");
				String toState = resultSet.getString("ToState");
				String subscriptionGroupID = resultSet.getString("SubscriptionGroupID");
				
				if(fromState == null || toState == null || subscriptionGroupID == null) {
					continue;
				}
				
				String url = resultSet.getString("URL");
				if(url == null || url.trim().length() == 0) {
					continue;
				}
				
				String message = resultSet.getString("Msg");
				if(message == null) {
					message = "";
				}
				
				String key = fromState + "|" + toState + "|" + subscriptionGroupID;
				key = key.toLowerCase();
				
				StateWiseMessage stateWiseMessage = new StateWiseMessage(url, message);
				ArrayList<StateWiseMessage> value = this.stateWiseMessages.get(key);
				if(value == null) {
					value = new ArrayList<StateWiseMessage>();
				}
				
				value.add(stateWiseMessage);
				this.stateWiseMessages.put(key, value);
			}
			resultSet.close();
			statement.close();
		} catch (Exception ex) {}
    }

    
    public Hashtable<String, SubscriptionGroup> getSubscriptionGroups() {
    	return this.subscriptionGroups;
    }
    
    public Hashtable<String, SubscriptionGroup_ext> getSubscriptionGroupExts() {
    	return this.subscriptionGroupExtensions;
    }
    
    public Hashtable<String, ArrayList<StateWiseMessage>> getStateWiseMessages() {
    	return this.stateWiseMessages;
    }
}
