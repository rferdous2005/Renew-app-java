package com.ssdtech.main;

import com.ssdtech.model.Notification;
import com.ssdtech.model.StateWiseMessage;
import com.ssdtech.utils.LogWrapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

public class PostNotificationService extends Thread {
	private final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:m:ss");
	
	private int threadNumber;
    private BlockingQueue<Notification> notificationQueue;
    private Hashtable<String, ArrayList<StateWiseMessage>> stateWiseMessages;
    private SynchronizedFileWriter synchronizedFileWriter;
    ShutDownService shutDown;
    private LogWrapper logWrapper;
	
	public PostNotificationService(int threadNumber,
                                   BlockingQueue<Notification> notificationQueue,
                                   Hashtable<String, ArrayList<StateWiseMessage>> stateWiseMessages, ShutDownService sd) {
		this.threadNumber = threadNumber;
		this.notificationQueue = notificationQueue;
		this.stateWiseMessages = stateWiseMessages;
		this.synchronizedFileWriter = SynchronizedFileWriter.getInstance();
		this.shutDown = sd;
		this.logWrapper = new LogWrapper(PostNotificationService.class.getName());
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				Notification notification = this.notificationQueue.take();
				
				this.sendNotification(notification);

			} catch (InterruptedException e) {
				this.logWrapper.error(e);
			} catch (Exception e) {
				this.logWrapper.error(e);
			}
			if(this.notificationQueue.isEmpty() && this.shutDown.isShutdown()) {
				this.logWrapper.info("Shutting down PostNotification Thread " + threadNumber);
				break;
			}
		}

	}
	
	private void sendNotification(Notification notification) {
		String MSISDN = notification.getmSISDN();
		String SubscriptionGroupID = notification.getSubscriptionGroupID();
		String FromState = notification.getFromState();
		String ToState = notification.getToState();
		Date NextRenewalDate = notification.getNextRenewalDate();
		Date ExpiraryDate = notification.getExpiryDate();
		
		String key = FromState + "|" + ToState + "|" + SubscriptionGroupID;
		key = key.toLowerCase();
		
		ArrayList<StateWiseMessage> stateMessages = this.stateWiseMessages.get(key);
		Iterator<StateWiseMessage> iterator = stateMessages.iterator();
		
		while(iterator.hasNext()) {
			StateWiseMessage stateWiseMessage = iterator.next();
			this.CallURL(stateWiseMessage.getMessage(), MSISDN, SubscriptionGroupID, 
					     stateWiseMessage.getURL(), FromState, ToState,
					     FORMATTER.format(NextRenewalDate), FORMATTER.format(ExpiraryDate));

			this.logWrapper.info(notification.getmSISDN()+" | "+FromState+" | "+ToState);
		}
	}
	
	private boolean CallURL(String Msg, String MSISDN,
			String SubscriptionGroupID, String url, String fromState,
			String toState, String nextRenewalDate, String expiraryDate) {
		int statuscode = 0;
		try {

			Msg = java.net.URLEncoder.encode(Msg, "UTF-8");
			String EncodedNextRenewalDate = java.net.URLEncoder.encode(
					nextRenewalDate, "UTF-8");
			String EncodedExpiraryDate = java.net.URLEncoder.encode(
					expiraryDate, "UTF-8");
			if (url.contains("?"))
				url += "&Msg=" + Msg + "&MSISDN=" + MSISDN + "&ServiceName="
						+ SubscriptionGroupID + "&FromState=" + fromState
						+ "&ToState=" + toState + "&NextRenewalDate="
						+ EncodedNextRenewalDate + "&ExpiraryDate="
						+ EncodedExpiraryDate;
			else
				url += "?Msg=" + Msg + "&MSISDN=" + MSISDN + "&ServiceName="
						+ SubscriptionGroupID + "&FromState=" + fromState
						+ "&ToState=" + toState + "&NextRenewalDate="
						+ EncodedNextRenewalDate + "&ExpiraryDate="
						+ EncodedExpiraryDate;

			URL serverUrl = null;
			HttpURLConnection conn = null;
			InputStream urlconninstr;
			serverUrl = new URL(url);
			conn = (HttpURLConnection) serverUrl.openConnection();
			conn.setDoOutput(false);
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(AppConstant.HTTP_CONNECTION_TIMEOUT);
			conn.connect();
			statuscode = conn.getResponseCode();

			urlconninstr = conn.getInputStream();
			StringBuilder buffer = new StringBuilder();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					urlconninstr, "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				buffer.append(line);
			}
			in.close();
			urlconninstr.close();
			conn.disconnect();
			this.entryNotificationHistory(MSISDN, SubscriptionGroupID, 
					                        fromState, toState, url, "SUCCESS", 
					                        statuscode, "POSTRENEWAL");
			logWrapper.debug(url + " : " + buffer.toString());
		} catch (Exception exp) {
			this.entryNotificationHistory(MSISDN, SubscriptionGroupID, 
											fromState, toState, url, "FAILED", 
											statuscode, "POSTRENEWAL");
			logWrapper.error(exp.toString());
			return false;
		}
		return true;
	}
	
	public void entryNotificationHistory(
			String userid, String subscriptiongroupid, String fromstate, 
			String tostate, String url, String status, int statuscode, String type) {
		String notificationContent = fromstate + "|" + tostate + "|" + url + "|" + subscriptiongroupid 
                + "|" + status + "|" + statuscode + "|" + type + "|" + userid
             + "|" + FORMATTER.format(new Date());
		try {
			this.synchronizedFileWriter.writeToFile(notificationContent);
		} catch(FileNotFoundException exception) {
			this.logWrapper.error(exception.toString());
		} catch(IOException exception) {
			this.logWrapper.error(exception.toString());
		}
	}
}
