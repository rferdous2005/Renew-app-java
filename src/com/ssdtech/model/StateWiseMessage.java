package com.ssdtech.model;

public class StateWiseMessage {
    private final String url;
    private final String message;
    
    public StateWiseMessage(String url, String message) {
    	this.url = url;
    	this.message = message;
    }
    
    public String getURL() {
    	return this.url;
    }
    
    public String getMessage() {
    	return this.message;
    }
}
