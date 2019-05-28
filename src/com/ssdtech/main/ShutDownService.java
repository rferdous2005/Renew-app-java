package com.ssdtech.main;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutDownService extends Thread {
	private volatile static ShutDownService shutDownService;
	
	private static final int SHUTDOWN_COMMAND = 1;
	private static final int ASCII_VALUE_FOR_ZERO = 48;
	private volatile int currentShutdownCommand;
	private ExecutorService notificationServicesPool;
	
	public static ShutDownService getInstance() {
		if(shutDownService == null) {
    		synchronized(ShutDownService.class) {
    			if(shutDownService == null) {
    				shutDownService = new ShutDownService();
    			}
    		}
    	}
    	return shutDownService;
	}
	
	public void setExecutorServiceInstance(ExecutorService notificationServicesPool) {
		this.notificationServicesPool = notificationServicesPool;
	}
	
	private ShutDownService() {
		this.currentShutdownCommand = 0;
	}
	
	@Override
	public void run() {
	    while(true) {
	    	try {
	    		Thread.sleep(AppConstant.TimeIntervalForReadingShutdownCommandFile);
	    	} catch(InterruptedException exception) {}
	    	
		    this.updateCurrentShutdownCommand();
			if(isShutdown()) {
				this.cleanShutdownCommandFile();
				break;
			}
	    }
	    
		this.notificationServicesPool.shutdown();
		try {
			this.notificationServicesPool.awaitTermination(AppConstant.TimeoutForGracefulShutdown, 
					                                       TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isShutdown() {
		return (this.currentShutdownCommand == SHUTDOWN_COMMAND);
	}
	
	private void updateCurrentShutdownCommand() {
		this.currentShutdownCommand = this.getShutdownCommand();
	}
	
	private int getShutdownCommand() {
		int command = -1; // -1 represents INVALID command.
		try {
			FileReader fileReader = new FileReader(AppConstant.ShutDownCommandFilePath);
			try {
				command = fileReader.read() - ASCII_VALUE_FOR_ZERO;
			} catch (IOException e) {
				try {
					fileReader.close();
				} catch (IOException e1) {}
			}
		} catch (FileNotFoundException e) {}
		
		return command;
	}
	
	private void cleanShutdownCommandFile() {
		try {
			FileWriter fileWriter = new FileWriter(AppConstant.ShutDownCommandFilePath);
			fileWriter.write("0");
			fileWriter.close();
		} catch(FileNotFoundException e) {}
		  catch(IOException e) {}
		
	}
}