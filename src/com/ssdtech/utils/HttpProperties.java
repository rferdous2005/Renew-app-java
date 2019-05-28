package com.ssdtech.utils;

public class HttpProperties {
    private String requestMethod;
    private int timeout;
    private String encoding;
    private boolean doInput;
    private boolean doOutput;
    private String url;
    private String httpMessage;
    
    public HttpProperties() {
    	this.requestMethod = "INVALID";
    	this.timeout = -1;
    	this.encoding = "DUMMY";
    	this.doInput = true;
    	this.doOutput = true;
    }
    
    public HttpProperties setRequestMethod(String requestMethod) {
    	this.requestMethod = requestMethod;
    	return this;
    }
    
    public HttpProperties setTimeout(int timeout) {
    	this.timeout = timeout;
    	return this;
    }
    
    public HttpProperties setEncoding(String encoding) {
    	this.encoding = encoding;
    	return this;
    }
    
    public HttpProperties setDoInput(boolean doInput) {
    	this.doInput = doInput;
    	return this;
    }
    
    public HttpProperties setDoOutput(boolean doOutput) {
    	this.doOutput = doOutput;
    	return this;
    }
    
    public HttpProperties setURL(String url) {
    	this.url = url;
    	return this;
    }
    
    public HttpProperties setHttpMessage(String httpMessage) {
    	this.httpMessage = httpMessage;
    	return this;
    }
    
    public String getRequestMethod() {
    	return this.requestMethod;
    }
    
    public int getTimeout() {
    	return this.timeout;
    }
    
    public String getEncoding() {
    	return this.encoding;
    }
    
    public boolean getDoInput() {
    	return this.doInput;
    }
    
    public boolean getDoOuput() {
    	return this.doOutput;
    }
    
    public String getURL() {
    	return this.url;
    }
    
    public String getHttpMessage() {
    	return this.httpMessage;
    }
}

