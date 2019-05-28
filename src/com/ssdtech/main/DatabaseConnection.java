package com.ssdtech.main;

import com.ssdtech.utils.LogWrapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection
{
	public   Connection connection;
	public static String HOSTURL;

	public LogWrapper logWrapper = new LogWrapper(this.getClass().getName());
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
	
	public void closeDBConnection() {
		try {
			this.connection.close();
		} catch (SQLException e) {}
	}
}	