/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssdtech.main;

/**
 *
 * @author sarwar
 */
import java.io.FileInputStream;
import java.util.Properties;

public class Configuration 
{   
   Properties prop = new Properties();
   
   public Configuration()
   {	
    	try {
    		
    		   prop.load(new FileInputStream("config.cfg"));
    	    } 
    	catch (Exception ex)
    	{
    		ex.printStackTrace();
        }
   }
 
   public String getProperty(String key)
   {
	String value = this.prop.getProperty(key);	
	return value;
   }
}
