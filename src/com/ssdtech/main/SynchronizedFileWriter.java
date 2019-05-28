package com.ssdtech.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SynchronizedFileWriter {
    private volatile static SynchronizedFileWriter syncFileWriter;
    private static final int SECONDS_TO_MILLISECONDS_CONV = 1000;
    private static final String notificationFileParentDirectory = 
    		"./notification";
    private static final String notificationFilePathNameFormat = 
    		"./notification/notification_history_%s.txt";
    private static final DateFormat FORMATTER = 
    		new SimpleDateFormat("yyyy_MM_dd_HH_m_ss");
    private long fileCreationIntervalInMilli;
    public static FileOutputStream processing=null;
	public static FileOutputStream travelCGW=null;
	public static FileOutputStream lastUpdate=null;
	public static FileOutputStream total=null;
    private SynchronizedFileWriter() {
    	super();
    	File parentDirectory = new File(notificationFileParentDirectory);
    	if(!parentDirectory.exists()) {
    		parentDirectory.mkdir();
    	}
    	
    	this.fileCreationIntervalInMilli = 
    			AppConstant.newNotificationFileCreationInterval * SECONDS_TO_MILLISECONDS_CONV;
    }
    
    public synchronized void writeToFile(String notificationContent) 
    		throws FileNotFoundException, IOException {
    	long trimmedTime = (System.currentTimeMillis() / this.fileCreationIntervalInMilli) 
    									* this.fileCreationIntervalInMilli;
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeZone(AppConstant.malaysianTimeZone);
    	calendar.setTimeInMillis(trimmedTime);
    	
    	String fileName = String.format(notificationFilePathNameFormat, FORMATTER.format(calendar.getTime()));
    	FileOutputStream out = new FileOutputStream(fileName, true);
    	out.write(notificationContent.getBytes());
    	out.write("\r\n".getBytes());
    	out.flush();
    	out.close();
    }
    
    public static SynchronizedFileWriter getInstance() {
    	if(syncFileWriter == null) {
    		synchronized(SynchronizedFileWriter.class) {
    			if(syncFileWriter == null) {
    				syncFileWriter = new SynchronizedFileWriter();
    			}
    		}
    	}
    	return syncFileWriter;
    }

    public void timingStatistics(String msg, int n) throws IOException {
    	if(n == 1) {
			if (processing == null) {
				processing = new FileOutputStream("./processing.txt", true);
			}
			synchronized (processing) {
				processing.write( msg.getBytes());
				processing.write("\r\n".getBytes());
				processing.flush();
			}
		}
		else if(n == 2) {
			if (travelCGW == null) {
				travelCGW = new FileOutputStream("./CGW_travel.txt", true);
			}
			synchronized (travelCGW) {
				travelCGW.write(msg.getBytes());
				travelCGW.write("\r\n".getBytes());
				travelCGW.flush();
			}
		}
		else if(n == 3) {
			if (lastUpdate == null) {
				lastUpdate = new FileOutputStream("./last_update.txt", true);
			}
			synchronized (lastUpdate) {
				lastUpdate.write(msg.getBytes());
				lastUpdate.write("\r\n".getBytes());
				lastUpdate.flush();
			}
		}
		else if(n == 4) {
			if (total == null) {
				total = new FileOutputStream("./total.txt", true);
			}
			synchronized (total) {
				total.write(msg.getBytes());
				total.write("\r\n".getBytes());
				total.flush();
			}
		}
    }
}
