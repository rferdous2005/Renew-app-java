package com.ssdtech.main;

import java.util.TimeZone;

public class AppConstant {
	public static String PrimaryHost, SecondaryHost;
	public static int PrimaryPort, SecondaryPort;
	public static String ApplicationId;
	public static String ApplicationPassword;
	public static String CommandId;
	public static int TransactionPrefix;
	public static int SleepPeriod;
	public static int LogEnable;
	public static int LogLevel;
	public static String ChargingInterface;
	public static String ChargingUrl;
	// public static int ServiceIDSuffix;
	public static String ServiceIDSuffix;
	public static String RenewAdditionalCondition;
	public static int RenewPickupNo;
	public static int NotifyUserEnable = 1;
	public static String ServiceUri;
	public static Long sleepTime;
	public static String CGW_SUCCESS_CODE = "1";
	public static String DeRegResultCode;// mamun add
	public static int HTTP_CONNECTION_TIMEOUT = 4000;
	public static int TimeSlabEnable = 1;
	public static String SlabStartTime;
	public static String SlabEndTime;
	public static int SlabStartHour = 1;
	public static int SlabEndHour = 1;
	public static int SlabStartMinute = 1;
	public static int SlabEndMinute = 1;
	public final static String CGW_CONNECTION_ERROR = "ERROR";
	public static String timeZone = "Asia/Kuala_Lumpur";
	public static TimeZone malaysianTimeZone = TimeZone
			.getTimeZone("Asia/Kuala_Lumpur");
	public static int MaxNotificationMinute = 1;
	public static int DelayedChargingSeconds = 30;
	public static int NumericMsisdn;
	// ading faildresultcodes
	public static String FailedResultCodes;
	public static String[] FailedCodesArray;
	public static int TimeIntervalForAttempt;
	public static int NumberOfAttempt;
	public static int ProcessingDueTime;
	public static String RenewNotifyAdditionalCondition;
	public static String HostChangingNotificationUrl;
	public static int HostChangingNotificationEnable = 1;
	public static int PackageSwitchingEnable  = 0;
	//New notification history file creation interval in seconds.
	public static long newNotificationFileCreationInterval;
	
	public static int BackwardChargingForDelayedRegisteredEnable;
	public static int PickOnlyForToday;
	public static String ShutDownCommandFilePath;
	public static long TimeoutForGracefulShutdown;
	public static long TimeIntervalForReadingShutdownCommandFile;
	public static int NumberOfPreRenewalThreads = 20;
	public static int NumberOfRenewalPickThreads = 100;
	public static int NumberOfRenewalSubscriptionThreads = 100;
	public static int NumberOfPostRenewalThreads = 20;
	public static int NotifyUserPickupNo = 1000;
	public static String DBConfigPath;
	public static String DB_PORT;
	public static String dbHostIP;
	public static String dbUsername;
	public static String dbPassword;
	public static String dbName;
	public static String RedisHost;
	public static int RedisPort;
	public static String DB_Extra_Config;
}
