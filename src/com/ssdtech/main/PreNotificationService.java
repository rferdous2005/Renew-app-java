package com.ssdtech.main;

import com.ssdtech.model.NotifyUserViaSMSInfo;
import com.ssdtech.utils.HttpProcessor;
import com.ssdtech.utils.HttpProperties;
import com.ssdtech.utils.LogWrapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PreNotificationService extends Thread {
	private volatile boolean isShutdown = false;
	
	private final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:m:ss");
	private int threadNumber;
	private DatabaseHandler databaseHandler;
	private LogWrapper logWrapper;
	private ShutDownService shutdownService;
	
	private long sleepTimeThreshold = AppConstant.sleepTime * 16; 
	
	public PreNotificationService(int threadNumber, ShutDownService shutdownService) {
		this.threadNumber = threadNumber;
		this.shutdownService = shutdownService;
		this.databaseHandler = new DatabaseHandler(threadNumber);
		this.logWrapper = new LogWrapper(PreNotificationService.class.getName());
	}
	
	@Override
	public void run() {
		while (true) {
			long sleepTime = AppConstant.sleepTime;
			while (this.isRunnable()) {
				this.isShutdown = this.shutdownService.isShutdown();
				if(this.isShutdown) {
					System.out.println("Shutting down : PreNotification Thread " + this.threadNumber);
					return;
				}
				int totalNotifiedUsers = NotifyUserViaSMS();

				if(totalNotifiedUsers == 0) {
					sleepTime = sleepTime * 2;
					if(sleepTime > this.sleepTimeThreshold) {
						sleepTime = this.sleepTimeThreshold;
					}
				} else {
					sleepTime = AppConstant.sleepTime;
				}
				
				try {
					this.logWrapper.info("Application is sleeping...");
					Thread.sleep((sleepTime * 1000));
					this.logWrapper.info("Application is Wake Up...");
				} catch (Exception ex) {
					this.logWrapper.error("Subscription Thread awakened prematurely");
					this.logWrapper.error("", ex.fillInStackTrace());
				}
			}
			
			try {
				Thread.sleep((AppConstant.sleepTime * 1000));
			} catch (Exception ex) {
				this.logWrapper.error("Subscription Thread awakened prematurely");
				this.logWrapper.error("", ex.fillInStackTrace());
			}
		}
	}
	
	private boolean isRunnable() {
		if (AppConstant.TimeSlabEnable == 1) {
			Date getDate = new Date();
			Calendar now = Calendar.getInstance(AppConstant.malaysianTimeZone);
			now.setTime(getDate);
			long time = getDate.getTime();
			now.setTimeInMillis(time);
			int hour = now.get(Calendar.HOUR_OF_DAY);
			int minute = now.get(Calendar.MINUTE);
			minute = (hour * 60) + minute;

			if (minute >= AppConstant.SlabStartMinute
					&& minute <= AppConstant.SlabEndMinute)
				return true;
			else
				return false;
		}
		return true;
	}
	
	private int NotifyUserViaSMS() {
		List<NotifyUserViaSMSInfo> notificableUserList =
				this.databaseHandler.getNotificableUserInformation();
		if(notificableUserList.size() > 0) {
			this.processNotifyUserList(notificableUserList);
		}
		
		return notificableUserList.size();
	}

	private void processNotifyUserList(List<NotifyUserViaSMSInfo> notifyUserList) {
		for (int i = 0; i < notifyUserList.size(); i++) {
			NotifyUserViaSMSInfo notifyInfo = notifyUserList.get(i);
			
			boolean isSendSMSSuccessfully = callRenewNotificationURL(
												notifyInfo.getmSISDN(), 
												notifyInfo.getOriginalSubscriptionGroupID(),
												notifyInfo.getRenewNotificationURL()
											);
			if (isSendSMSSuccessfully) {
				int serviceDuration = 1;
				try {
					serviceDuration = Integer.valueOf(notifyInfo.getServiceDuration());
				} catch (Exception exp) {
					this.logWrapper.error("", exp.fillInStackTrace());
				}
				
				Date updatedNextNotificationDate = dateAdd(Calendar.DATE, 
														   serviceDuration, 
														   notifyInfo.getNextNotificationDate()
														 );
				this.databaseHandler.updateSubscriberNextNotificationDate(
						notifyInfo.getId(), 
						this.FORMATTER.format(updatedNextNotificationDate)
					);
				this.logWrapper.info(notifyInfo.getId() +" | " +notifyInfo.getmSISDN());
			}
		}
	}

	private boolean callRenewNotificationURL(String MSISDN,	String SubscriptionGroupID, String url) {
		String sendingStatus = "FAILED";
		boolean isSuccess = false;
		int statusCode = -1;
		
		try {
			if (url.contains("?")) {
				url += "&MSISDN=" + MSISDN + "&ServiceID=" + SubscriptionGroupID;
			} else {
				url += "?MSISDN=" + MSISDN + "&ServiceID=" + SubscriptionGroupID;
			}
			
			HttpProperties httpProperties = new HttpProperties();
			httpProperties.setDoOutput(false)
			              .setRequestMethod("GET")
			              .setTimeout(AppConstant.HTTP_CONNECTION_TIMEOUT)
			              .setEncoding("UTF-8")
			              .setURL(url);
			HttpProcessor httpProcessor = new HttpProcessor(httpProperties);
			String response = httpProcessor.process();
			statusCode = httpProcessor.getStatusCode();
			
			this.logWrapper.debug(url + " : " + response);
			
			if(statusCode >= 200 && statusCode < 300) {
				isSuccess = true;
				sendingStatus = "SUCCESS";
			}
		} catch(Exception exp) {
			this.logWrapper.error("", exp.fillInStackTrace());
		}
		
		this.databaseHandler.entryNotificationHistory(MSISDN, SubscriptionGroupID, 
													  "Registered", "Registered",
													  url, sendingStatus,
													  statusCode, "PRERENEWAL");
		
	    return isSuccess;
	}
	
	private Date dateAdd(int timeUnit, int interval, Date fromDate) {
		Calendar calendar = Calendar.getInstance(AppConstant.malaysianTimeZone);
		calendar.setTime(fromDate);
		calendar.add(timeUnit, interval);
		return calendar.getTime();
	}
}
