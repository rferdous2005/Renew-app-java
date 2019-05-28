package com.ssdtech.utils;

import com.ssdtech.main.AppConstant;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class SocketWrapper {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintStream out;
    
    private String[] addresses;
    private int[] ports;
    private int currentPeerInfoIndex;
    private int previousPeerInfoIndex;
    
    private long sleepTime;
    
    private LogWrapper logWrapper;
    
    public SocketWrapper(String[] addresses, int[] ports, long sleepTime) {
        this.addresses = addresses;
        this.ports = ports;
        
        this.currentPeerInfoIndex = 0;
        this.previousPeerInfoIndex = 0;
        
        this.sleepTime = sleepTime;
        this.logWrapper = new LogWrapper(SocketWrapper.class.getName());
        
        this.establishSocketChannel();
    }
    
    public String getResponse(String request) {
    	try {
			this.out.print(request);

			char[] buf = new char[1000];
			this.in.read(buf, 0, buf.length);
			
			return String.valueOf(buf);
		} catch (Exception exception) {
			this.storeLog(exception);
			this.establishSocketChannel();
			return AppConstant.CGW_CONNECTION_ERROR;
		}
    }
    
    public void establishSocketChannel() {
    	this.cleanResources();
    	this.createSocketObject();
    	try {
    		this.setInputStream();
    		this.setOutputStream();
    	} catch(IOException exception) {
    		this.storeLog(exception);
    		this.sleep();
    		this.establishSocketChannel();
    	}
    	
    	if (AppConstant.LogEnable == 1) {
			logWrapper.warn("ConnectedHost :" + this.getCurrentAddress());
			logWrapper.warn("ConnectedPort :" + this.getCurrentPort());
		}
    	
    	if(AppConstant.HostChangingNotificationEnable == 1) {
    		if(this.currentPeerInfoIndex != this.previousPeerInfoIndex) {
    			this.callHostChangingNotificationUrl();
    		}
    	}
    }
    
    public void createSocketObject() {
    	try {
			this.clientSocket = new Socket(this.addresses[this.currentPeerInfoIndex],
					                       this.ports[this.currentPeerInfoIndex]);
		} catch(Exception exception) {
			this.sleep();
			this.changeToNextAddressAndPort();
			this.createSocketObject();
		}
    }
    
    public void setInputStream() throws IOException {
    	this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
    }
    
    public void setOutputStream() throws IOException {
    	this.out = new PrintStream(clientSocket.getOutputStream(), true);
    }
    
    public void changeToNextAddressAndPort() {
    	this.previousPeerInfoIndex = this.currentPeerInfoIndex;
    	this.currentPeerInfoIndex = (this.currentPeerInfoIndex + 1) % this.addresses.length;
    }
    
    public void cleanResources() {
    	try {
    		if(this.in != null) {
	    		this.in.close();
	    	}
    	} catch(IOException exception) {
    		this.storeLog(exception);
    	}
    	
    	if(this.out != null) {
    		this.out.close();
    	}
    	
    	try {
    		if(this.clientSocket != null) {
	    		this.clientSocket.close();
	    	}
    	} catch(IOException exception) {
    		this.storeLog(exception);
    	}
    	
    	this.clientSocket = null;
    	this.in = null;
    	this.out = null;
    }
    
    private void sleep() {
    	try {
    		Thread.sleep(this.sleepTime);
    	} catch(InterruptedException exception) {
    		this.storeLog(exception);
    	}
    }
	
	public Socket getClientSocket() {
		return this.clientSocket;
	}
	
	public String getCurrentAddress() {
		return this.addresses[this.currentPeerInfoIndex];
	}
	
	public int getCurrentPort() {
		return this.ports[this.currentPeerInfoIndex];
	}
	
	public int getCurrentPeerInfoIndex() {
		return this.currentPeerInfoIndex;
	}
	
	private void storeLog(Throwable t) {
		if(AppConstant.LogEnable == 1) {
			logWrapper.error("", t);
		} else {
			t.printStackTrace();
		}
	}
	
	private boolean callHostChangingNotificationUrl() {
		String url = AppConstant.HostChangingNotificationUrl;
		String Msg = "Failed to connect with Host:  "
				+ this.addresses[this.previousPeerInfoIndex] + " Port: "
				+ this.ports[this.previousPeerInfoIndex];

		int statuscode;
		URL serverUrl = null;
		HttpURLConnection conn = null;
		InputStream urlconninstr;
		try {
			Msg = java.net.URLEncoder.encode(Msg, "UTF-8");
			if (url.contains("?"))
				url += "&Msg=" + Msg;
			else
				url += "?Msg=" + Msg;
			serverUrl = new URL(url);
			conn = (HttpURLConnection) serverUrl.openConnection();
			conn.setDoOutput(false);
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(AppConstant.HTTP_CONNECTION_TIMEOUT);
			conn.connect();
			statuscode = conn.getResponseCode();
			urlconninstr = conn.getInputStream();
			StringBuffer buffer = new StringBuffer();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					urlconninstr, "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				buffer.append(line);
			}

			in.close();
			logWrapper.debug(url + " : " + buffer.toString());
		} catch(Exception exp) {
			if(AppConstant.LogEnable == 1) {
				logWrapper.error("", exp);
			} else {
				exp.printStackTrace();
			}
			return false;
		}
		return true;
	}
}
