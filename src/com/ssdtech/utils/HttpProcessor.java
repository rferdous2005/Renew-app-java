package com.ssdtech.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpProcessor {
	private String INVALID_HTTP_REQUEST_TYPE = "INVALID";
	private int INVALID_HTTP_CONNECTION_TIMEOUT = -1;
	private String DEFAULT_ENCODING = "DUMMY";
	private HttpProperties httpProperties;
	private HttpURLConnection httpUrlConnection;
	private int statusCode;
	
	private LogWrapper logWrapper;
	
	public HttpProcessor(HttpProperties httpProperties) {
		this.httpProperties = httpProperties;
		this.statusCode = -1;
		this.logWrapper = new LogWrapper(HttpProcessor.class.getName());
	}
	
	public String process() throws IOException, UnsupportedEncodingException, MalformedURLException, Exception {
		setHttpUrlConnectionInstance();
		this.httpUrlConnection.connect();
		if(this.httpProperties.getRequestMethod().equals("POST")) {
			OutputStream out = null;
			try {
				out = this.httpUrlConnection.getOutputStream();
				sendHttpPostRequest(out);
			} catch(IOException exception) {
				this.logWrapper.error("", exception.fillInStackTrace());
				this.closeResources(this.httpUrlConnection, null, out);
				throw exception;
			}
		}
		
		String response = "";
		BufferedReader in = null;
		try {
			in = this.getInputChannel();
			response = this.getHttpResponse(in);
		} catch(Exception exception) {
			this.logWrapper.error("", exception.fillInStackTrace());
			this.closeResources(this.httpUrlConnection, in, null);
			throw exception;
		}
		this.httpUrlConnection.disconnect();
		return response;
	}
	
	public int getStatusCode() {
		return this.statusCode;
	}
	
	public void closeResources(HttpURLConnection httpUrlConnection,
			                    BufferedReader in, OutputStream out) {
		if(in != null) {
			try {
				in.close();
			} catch(IOException exp) {
				this.logWrapper.error("", exp.fillInStackTrace());
			}
		}
		
		if(out != null) {
			try {
				out.close();
			} catch(IOException exp) {
				this.logWrapper.error("", exp.fillInStackTrace());
			}
		}
		
		if(httpUrlConnection != null) {
			httpUrlConnection.disconnect();
		}
	}
	
	private void setHttpUrlConnectionInstance() 
			throws MalformedURLException, IOException {
		this.httpUrlConnection = (HttpURLConnection) new URL(this.httpProperties.getURL()).openConnection();
		if(this.httpProperties.getRequestMethod().equals(INVALID_HTTP_REQUEST_TYPE)) {
			throw new IOException();
		}
		this.httpUrlConnection.setRequestMethod(this.httpProperties.getRequestMethod());
		if(this.httpProperties.getTimeout() != INVALID_HTTP_CONNECTION_TIMEOUT) {
			this.httpUrlConnection.setConnectTimeout(this.httpProperties.getTimeout());
		}
		this.httpUrlConnection.setDoInput(this.httpProperties.getDoInput());
		this.httpUrlConnection.setDoOutput(this.httpProperties.getDoOuput());
	}
	
	private void sendHttpPostRequest(OutputStream out) throws IOException {
		out.write(this.httpProperties.getHttpMessage().getBytes());
        out.flush();
        out.close();
	}
	
	private BufferedReader getInputChannel() throws IOException {
		BufferedReader in = null;
		try {
			this.statusCode = this.httpUrlConnection.getResponseCode();
			try {
				in = new BufferedReader(getHttpInReaderStream(this.httpUrlConnection.getInputStream()));
			} catch(IOException exception) {
				try {
					in = new BufferedReader(getHttpInReaderStream(this.httpUrlConnection.getErrorStream()));
				} catch(UnsupportedEncodingException cException) {
					throw cException;
				}
			} 
		} catch(IOException exception) {
			throw exception;
		}
		
		return in;
	}
	
	private String getHttpResponse(BufferedReader in) throws IOException {
		StringBuffer response = new StringBuffer();
		int c;
	    while ( (c = in.read()) != -1) {
	        response.append((char) c);
	    }
		in.close();
		return response.toString();
	}
	
	private InputStreamReader getHttpInReaderStream(InputStream in) throws UnsupportedEncodingException {
        if(!this.httpProperties.getEncoding().equals(DEFAULT_ENCODING)) {
        	return new InputStreamReader(in, this.httpProperties.getEncoding());
        } else {
        	return new InputStreamReader(in);
        }
	}
}

