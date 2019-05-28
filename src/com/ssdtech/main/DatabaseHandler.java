package com.ssdtech.main;

import com.ssdtech.model.NotifyUserViaSMSInfo;
import com.ssdtech.utils.LogWrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class DatabaseHandler {
	public   Connection connection;
	public static String HOSTURL;
	private int threadNumber;
	
	// Notification history writer.
    private SynchronizedFileWriter synchronizedFileWriter;
    private final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:m:ss");
	
	private LogWrapper logWrapper;
	
	public DatabaseHandler(int threadNumber) {
		this.threadNumber = threadNumber;
		// Initialize notification history writer.
		this.synchronizedFileWriter = SynchronizedFileWriter.getInstance();
		
		this.logWrapper = new LogWrapper(DatabaseHandler.class.getName());
	}

	public  Connection getConnection() throws SQLException
	{
		HOSTURL = "jdbc:mysql://"+AppConstant.dbHostIP+":"+AppConstant.DB_PORT+"/"+AppConstant.dbName+"?"+AppConstant.DB_Extra_Config;
		try {
			if(this.connection == null)
			{
				Class.forName ("com.mysql.jdbc.Driver").newInstance();
				DriverManager.setLoginTimeout(100000);
				this.connection =  DriverManager.getConnection(HOSTURL,AppConstant.dbUsername,AppConstant.dbPassword);
			}
			else if(!this.connection.isValid(5))		// will check for 5 seconds
			{
				this.connection.close();
				Class.forName ("com.mysql.jdbc.Driver").newInstance();
				DriverManager.setLoginTimeout(100000);
				this.connection =  DriverManager.getConnection(HOSTURL,AppConstant.dbUsername,AppConstant.dbPassword);
			}
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your MySQL JDBC Driver?");
			this.logWrapper.error(e);
		} catch (SQLException e) {
			this.logWrapper.error(e);
		} catch (Exception e) {
			this.logWrapper.error(e);
		}
		return this.connection;
	}
	
	public List<NotifyUserViaSMSInfo> getNotificableUserInformation() {
		List<NotifyUserViaSMSInfo> notifyUserList =
				new ArrayList<NotifyUserViaSMSInfo>(AppConstant.NotifyUserPickupNo);
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			String notificableUserQuery = 
					"SELECT "
					+ "ss.id,ss.msisdn,ss.SubscriptionGroupID,ss.isgift,"
					+ "sg.OriginalSubscriptionGroupID,"
						+ "(SELECT "
						+ "ServiceDuration "
						+ "FROM subscriptiongroup "
						+ "WHERE SubscriptionGroupID=sg.OriginalSubscriptionGroupID) "
					+ "AS ServiceDuration,"
					+ "sg.RenewNotificationDays,sg.RenewNotificationURL,"
					+ "sg.RenewGiftingNotificationURL,ss.NextNotificationDate "
					+ "FROM subscriberservices ss "
					+ "JOIN "
					+ "subscriptiongroup sg "
					+ "ON sg.SubscriptionGroupID=ss.SubscriptionGroupID "
					+ "WHERE ss.id% " + AppConstant.NumberOfPreRenewalThreads + " = " + this.threadNumber + " AND "
					+ AppConstant.RenewNotifyAdditionalCondition
					+ " AND "
					+ "ss.Status='Registered' AND "
					+ "sg.RenewNotificationDays>0 AND ss.NextNotificationDate<=NOW() "
					+ "LIMIT 0," + AppConstant.NotifyUserPickupNo;

			statement = getConnection().createStatement();
			resultSet = statement.executeQuery(notificableUserQuery);
			
			this.logWrapper.info("Pre renewal -- Thread number : " + this.threadNumber 
					            + "; Query : " + notificableUserQuery);
			
			while (resultSet.next()) {
				String id = String.valueOf(resultSet.getString("id"));
				String mSISDN = String.valueOf(resultSet.getString("msisdn"));
				String subscriptionGroupID = String.valueOf(
							resultSet.getString("SubscriptionGroupID")
						);
				String isgift = String.valueOf(resultSet.getString("isgift"));
				String originalSubscriptionGroupID = String.valueOf(
							resultSet.getString("OriginalSubscriptionGroupID")
						);
				String serviceDuration = String.valueOf(
							resultSet.getString("ServiceDuration")
						);
				String renewNotificationDays = String.valueOf(
							resultSet.getString("RenewNotificationDays")
						);
				String renewNotificationURL = String.valueOf(
							resultSet.getString("RenewNotificationURL")
						);
				String renewGiftingNotificationURL = String.valueOf(
							resultSet.getString("RenewGiftingNotificationURL")
						);
				Date nextNotificationDate = resultSet.getDate("NextNotificationDate");
				
				String notifyURL;
				if (isgift.toUpperCase().equals("Y")) {
					notifyURL = renewGiftingNotificationURL;
				} else {
					notifyURL = renewNotificationURL;
				}
				
				if (!notifyURL.equals("null") && notifyURL != null
						&& !notifyURL.isEmpty() && notifyURL.trim() != "") {
					NotifyUserViaSMSInfo temp = new NotifyUserViaSMSInfo(id,
							mSISDN, subscriptionGroupID,
							originalSubscriptionGroupID, serviceDuration,
							renewNotificationDays, notifyURL,
							nextNotificationDate);
					notifyUserList.add(temp);
				}
			}

			cleanResources(statement, resultSet);
		} catch (Exception exp) {
			this.logWrapper.error(exp);
			cleanResources(statement, resultSet);
		}
		
		return notifyUserList;
	}
	
	public void updateSubscriberNextNotificationDate(String subscriberId, String nextNotificationDate) {
		String updateSubscriberQuery = "UPDATE subscriberservices "
				                     + "SET "
				                     + "nextnotificationdate='" + nextNotificationDate + "', LastUpdate=NOW() "
				                     + "WHERE id=" + subscriberId;
		Statement statement = null;
		try {
			statement = getConnection().createStatement();
			statement.executeUpdate(updateSubscriberQuery);
			cleanResources(statement, null);
		} catch(Exception exception) {
			this.logWrapper.error(exception);
			cleanResources(statement, null);
		}
	}
	
	public void entryNotificationHistory(
			String userid, String subscriptiongroupid, String fromstate, 
			String tostate, String url, String status, int statuscode, String type) {
		String notificationContent = fromstate + "|" + tostate + "|" + url + "|" + subscriptiongroupid
				+ "|" + status + "|" + statuscode + "|" + type + "|" + userid
				+ "|" + FORMATTER.format(new Date());
		try {
			this.synchronizedFileWriter.writeToFile(notificationContent);
		} catch (FileNotFoundException exception) {
			if (AppConstant.LogEnable == 1) {
				logWrapper.error(exception);
			} else {
				exception.printStackTrace();
			}
		} catch (IOException exception) {
			if (AppConstant.LogEnable == 1) {
				logWrapper.error(exception);
			} else {
				exception.printStackTrace();
			}
		}
	}
	
	private void cleanResources(Statement statement, ResultSet resultSet) {
		if(resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				this.logWrapper.error(e);
			}
		}
		
		if(statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				this.logWrapper.error(e);
			}
		}
	}
}	
