package com.ssdtech.main;

import com.ssdtech.model.GlobalDatabaseData;
import com.ssdtech.model.Notification;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Executable {

	/**
	 * @param args
	 */
	private static Executable instance;
	private ExecutorService threadPool;

	private DatabaseConnection dbConn;
	private Statement statement = null;
	private ResultSet resultSet = null;
	BlockingQueue<Notification> allNotification;
	private List<String> Line = new ArrayList<String>();
	private Map<String, String> map = new HashMap<String, String>();

	Logger logger = Logger.getLogger(Executable.class.getName());

	public static void main(String[] args) {
		Executable app = getInstance();
		app.start();
	}

	private void start() {
		try {
			readConfigFile();
			threadPool = Executors.newFixedThreadPool(AppConstant.NumberOfPreRenewalThreads+ AppConstant.NumberOfRenewalPickThreads
							+ AppConstant.NumberOfRenewalSubscriptionThreads + AppConstant.NumberOfPostRenewalThreads + 10);
			GlobalDatabaseData databaseData = this.loadGlobalDatabaseData();
			ShutDownService shutdownService = ShutDownService.getInstance();
			shutdownService.setExecutorServiceInstance(this.threadPool);
			shutdownService.start();
			RenewUserList usersList = new RenewUserList(databaseData, shutdownService);

			this.allNotification = new ArrayBlockingQueue<Notification>(AppConstant.RenewPickupNo * AppConstant.NumberOfPostRenewalThreads);
			if(AppConstant.NotifyUserEnable == 1) {
				for (int i = 0; i < AppConstant.NumberOfPreRenewalThreads; i++) {
					PreNotificationService preNotify = new PreNotificationService(i, shutdownService);
					this.threadPool.execute(preNotify);
				}
			}
			Thread.sleep(5000);

			for (int i = 0; i < AppConstant.NumberOfRenewalSubscriptionThreads; i++) {
				Subscription sub = new Subscription(i, databaseData, shutdownService, usersList, this.allNotification);
				this.threadPool.execute(sub);
			}

			for (int i = 0; i < AppConstant.NumberOfPostRenewalThreads; i++) {
				PostNotificationService notify = new PostNotificationService(i, this.allNotification, databaseData.getStateWiseMessages(), shutdownService);
				this.threadPool.execute(notify);
			}
		} catch (Exception sqle) {
			Subscription.getStackTrace(sqle);
			sqle.printStackTrace();
		}
	}
	
	private GlobalDatabaseData loadGlobalDatabaseData() {
		GlobalDatabaseData databaseData = new GlobalDatabaseData();
		try {
			Connection connection = new DatabaseConnection().getConnection();
			databaseData.loadSubscriptionGroups(connection);
			databaseData.loadSubscriptionGroup_extTable(connection);
			databaseData.loadStateWiseMessageTable(connection);
			connection.close();
		} catch(SQLException exception) {}
		return databaseData;
	}

	public static Executable getInstance() {
		if (instance == null) {
			instance = new Executable();
		}
		return instance;
	}

	private void readConfigFile() throws FileNotFoundException {
		Configuration cfg = new Configuration();
		AppConstant.sleepTime = Long
				.parseLong(cfg.getProperty("SLEEP_SECONDS"));
		AppConstant.RenewPickupNo = Integer.parseInt(cfg
				.getProperty("RenewPickupNo"));
		AppConstant.NotifyUserPickupNo = Integer.parseInt(cfg
				.getProperty("NotifyUserPickupNo"));
		AppConstant.ChargingInterface = cfg.getProperty("ChargingInterface");
		AppConstant.ChargingUrl = cfg.getProperty("ChargingUrl");
		AppConstant.ServiceIDSuffix = cfg.getProperty("ServiceIDSuffix");
		AppConstant.PrimaryHost = cfg.getProperty("PrimaryHost");
		AppConstant.PrimaryPort = Integer.parseInt(cfg
				.getProperty("PrimaryPort"));
		AppConstant.SecondaryHost = cfg.getProperty("SecondaryHost");
		AppConstant.SecondaryPort = Integer.parseInt(cfg
				.getProperty("SecondaryPort"));
		AppConstant.ApplicationId = cfg.getProperty("ApplicationId");
		AppConstant.ApplicationPassword = cfg
				.getProperty("ApplicationPassword");
		AppConstant.CommandId = cfg.getProperty("CommandId");
		AppConstant.NotifyUserEnable = Integer.parseInt(cfg
				.getProperty("NotifyUserEnable"));
		AppConstant.HTTP_CONNECTION_TIMEOUT = Integer.parseInt(cfg
				.getProperty("HTTP_CONNECTION_TIMEOUT"));
		AppConstant.NumberOfPreRenewalThreads = Integer.parseInt(cfg
				.getProperty("NumberOfPreRenewalThreads"));
		AppConstant.NumberOfPostRenewalThreads = Integer.parseInt(cfg
				.getProperty("NumberOfPostRenewalThreads"));
		AppConstant.NumberOfRenewalPickThreads = Integer.parseInt(cfg
				.getProperty("NumberOfRenewalPickThreads"));
		AppConstant.NumberOfRenewalSubscriptionThreads = Integer.parseInt(cfg
				.getProperty("NumberOfRenewalSubscriptionThreads"));
		AppConstant.TimeSlabEnable = Integer.parseInt(cfg
				.getProperty("TimeSlabEnable"));
		AppConstant.NumericMsisdn = Integer.parseInt(cfg
				.getProperty("NumericMsisdn"));
		AppConstant.SlabStartTime = cfg.getProperty("SlabStartTime");
		AppConstant.SlabEndTime = cfg.getProperty("SlabEndTime");
		String[] str = AppConstant.SlabStartTime.split(":");
		AppConstant.SlabStartHour = Integer.parseInt(str[0]);
		AppConstant.SlabStartMinute = Integer.parseInt(str[1]);
		AppConstant.SlabStartMinute = (AppConstant.SlabStartHour * 60)
				+ AppConstant.SlabStartMinute;
		str = AppConstant.SlabEndTime.split(":");
		AppConstant.SlabEndHour = Integer.parseInt(str[0]);
		AppConstant.SlabEndMinute = Integer.parseInt(str[1]);
		AppConstant.SlabEndMinute = (AppConstant.SlabEndHour * 60)
				+ AppConstant.SlabEndMinute;
		AppConstant.RenewAdditionalCondition = cfg
				.getProperty("RenewAdditionalCondition");
		AppConstant.RenewNotifyAdditionalCondition = cfg
				.getProperty("RenewNotifyAdditionalCondition");
		AppConstant.LogEnable = Integer.parseInt(cfg.getProperty("LogEnable"));
		AppConstant.LogLevel = Integer.parseInt(cfg.getProperty("LogLevel"));

		AppConstant.CGW_SUCCESS_CODE = cfg.getProperty("CGW_SUCCESS_CODE");
		AppConstant.DeRegResultCode = cfg.getProperty("DeRegResultCode");// mamun
		// add
		AppConstant.timeZone = cfg.getProperty("TimeZone");
		AppConstant.malaysianTimeZone = TimeZone
				.getTimeZone(AppConstant.timeZone);
		AppConstant.MaxNotificationMinute = Integer.parseInt(cfg
				.getProperty("MaxNotificationMinute"));
		AppConstant.DelayedChargingSeconds = Integer.parseInt(cfg
				.getProperty("DelayedChargingSeconds"));
		// adding failedcodes
		AppConstant.FailedResultCodes = cfg.getProperty("FailedResultCodes");
		AppConstant.FailedCodesArray = AppConstant.FailedResultCodes.split(",");
		// ProcessingForRenewal
		AppConstant.TimeIntervalForAttempt = Integer.parseInt(cfg.getProperty("TimeIntervalForAttempt"));
		AppConstant.NumberOfAttempt = Integer.parseInt(cfg.getProperty("NumberOfAttempt"));
		AppConstant.ProcessingDueTime = Integer.parseInt(cfg
				.getProperty("ProcessingDueTime"));
		AppConstant.HostChangingNotificationUrl = cfg
				.getProperty("HostChangingNotificationUrl");
		AppConstant.HostChangingNotificationEnable = Integer.parseInt(cfg
				.getProperty("HostChangingNotificationEnable"));
		AppConstant.PackageSwitchingEnable = Integer.parseInt(cfg
				.getProperty("PackageSwitchingEnable"));
		AppConstant.PickOnlyForToday = Integer.parseInt(cfg
				.getProperty("PickOnlyForToday"));
		AppConstant.newNotificationFileCreationInterval =
				Long.parseLong(cfg.getProperty("NewNotificationFileCreationInterval"));
		
		AppConstant.BackwardChargingForDelayedRegisteredEnable =
				Integer.parseInt(cfg.getProperty("BackwardChargingForDelayedRegisteredEnable"));
		
		AppConstant.ShutDownCommandFilePath = cfg.getProperty("ShutDownCommandFilePath");
		AppConstant.TimeoutForGracefulShutdown = Long.parseLong(
				                  cfg.getProperty("TimeoutForGracefulShutdown")
				              );
		AppConstant.TimeIntervalForReadingShutdownCommandFile = Long.parseLong(
				    cfg.getProperty("TimeIntervalForReadingShutdownCommandFile")
				);
		AppConstant.DBConfigPath = cfg.getProperty("DBConfigPath");
		AppConstant.DB_PORT = cfg.getProperty("DB_PORT");
		//AppConstant.RedisHost = cfg.getProperty("RedisHost");
		//AppConstant.RedisPort = Integer.parseInt(cfg.getProperty("RedisPort"));
		AppConstant.DB_Extra_Config = cfg.getProperty("DB_Extra_Config");
		File fFile = new File(AppConstant.DBConfigPath);
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileReader(fFile));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains("->")) {
					String linesplit[] = line.split("->");
					if (linesplit[0].equals("APP_HOST")) {
						AppConstant.dbHostIP = linesplit[1];

					} else if (linesplit[0].equals("APP_USER")) {
						AppConstant.dbUsername = linesplit[1];
					} else if (linesplit[0].equals("APP_PASSWORD")) {
						AppConstant.dbPassword = linesplit[1];
					} else if (linesplit[0].equals("APP_DB")) {
						AppConstant.dbName = linesplit[1];
					} else
						continue;
				}


			}
			File f = new File("checker_files");
			String cur_time = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
			if( f.exists() ) f.renameTo(new File("checker_files_"+cur_time));
			if( !f.mkdir() ) System.exit(1);
		} catch(Exception ex) {

			ex.printStackTrace();
		}
		finally
		{
			scanner.close();
		}
		logger.info("sleepTime :" + AppConstant.sleepTime);
		logger.info("RenewPickupNo :" + AppConstant.RenewPickupNo);
		logger.info("ChargingInterface :" + AppConstant.ChargingInterface);
		logger.info("ChargingUrl :" + AppConstant.ChargingUrl);
		logger.info("PrimaryHost :" + AppConstant.PrimaryHost);
		logger.info("PrimaryPort :" + AppConstant.PrimaryPort);
		logger.info("SecondaryHost :" + AppConstant.SecondaryHost);
		logger.info("SecondaryPort :" + AppConstant.SecondaryPort);
		logger.info("ApplicationId :" + AppConstant.ApplicationId);
		logger.info("ApplicationPassword :" + AppConstant.ApplicationPassword);
		logger.info("CommandId :" + AppConstant.CommandId);
		logger.info("NotifyUserEnable :" + AppConstant.NotifyUserEnable);
		logger.info("HTTP_CONNECTION_TIMEOUT :"
				+ AppConstant.HTTP_CONNECTION_TIMEOUT);
		logger.info("TimeSlabEnable :" + AppConstant.TimeSlabEnable);
		logger.info("SlabStartHour :" + AppConstant.SlabStartHour);
		logger.info("SlabStartMinute :" + AppConstant.SlabStartMinute);
		logger.info("SlabEndHour :" + AppConstant.SlabEndHour);
		logger.info("SlabEndMinute :" + AppConstant.SlabEndMinute);
		logger.info("RenewAdditionalCondition :"
				+ AppConstant.RenewAdditionalCondition);
		logger.info("LogEnable :" + AppConstant.LogEnable);
		logger.info("CGW_SUCCESS_CODE :" + AppConstant.CGW_SUCCESS_CODE);
		logger.info("DeRegResultCode :" + AppConstant.DeRegResultCode);// mamun
		// add
		logger.info("FailedResultCodes :" + AppConstant.FailedResultCodes);
		logger.info("TimeZone :" + AppConstant.malaysianTimeZone);
		logger.info("MaxNotificationMinute :"
				+ AppConstant.MaxNotificationMinute);
		logger.info("DelayedChargingSeconds :"
				+ AppConstant.DelayedChargingSeconds);
		logger.info("RenewNotifyAdditionalCondition :"
				+ AppConstant.RenewNotifyAdditionalCondition);
		logger.info("HostChangingNotificationUrl :"
				+ AppConstant.HostChangingNotificationUrl);
		logger.info("HostChangingNotificationEnable :"
				+ AppConstant.HostChangingNotificationEnable);
		logger.info("PackageSwitchingEnable :" + AppConstant.PackageSwitchingEnable);
		
		logger.info("NewNotificationFileCreationInterval: " 
        		+ AppConstant.newNotificationFileCreationInterval);
		
		logger.info("BackwardChargingForDelayedRegisteredEnable: "
				+ AppConstant.BackwardChargingForDelayedRegisteredEnable);
		
		this.logger.info("ShutDownCommandFilePath : " + AppConstant.ShutDownCommandFilePath);
		try {
			FileReader fileReader = new FileReader(AppConstant.ShutDownCommandFilePath);
			try {
				fileReader.close();
			} catch (IOException e) {}
		} catch (FileNotFoundException e) {
			throw e;
		}
		
		this.logger.info("TimeoutForGracefulShutdown : " + AppConstant.TimeoutForGracefulShutdown);
		this.logger.info("TimeIntervalForReadingShutdownCommandFile : " 
				        + AppConstant.TimeIntervalForReadingShutdownCommandFile);
	}

}
