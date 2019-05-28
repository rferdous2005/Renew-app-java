package com.ssdtech.main;

import com.ssdtech.model.*;
import com.ssdtech.utils.LogWrapper;
import com.ssdtech.utils.SocketWrapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
//import redis.clients.jedis.Jedis;

public class Subscription extends Thread {
    public final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:m:ss");
    String responseline;
    private DatabaseConnection dbConn = null;
    private Statement statement = null;
    private ResultSet resultSet = null;
    String RenewAdditionalCondition = "1=1";
    RenewUserList usersList;
    BlockingQueue<Notification> notificationQueue;
    private Hashtable<String, SubscriptionGroup> subgroupHashtable;
    private Hashtable<String, SubscriptionGroup_ext> subgroup_extHashtable;
    private String regex = "\\d+";
    private int batchSize;
    public static final Integer accessLock = 5;  // for exclusive lock on RenewUserList
    public String checkerName;
    public PrintWriter replaceChecker;
    public FileWriter appendChecker;
    public Scanner readFile;
    private int threadNumber;
    //Jedis redis;
    // Log wrapper declaration
    private LogWrapper logWrapper;

    // StateWiseMsg mapping declaration
    private Hashtable<String, ArrayList<StateWiseMessage>> stateWiseMessages;

    // Global Calendar object
    private Calendar calendar = Calendar.getInstance(AppConstant.malaysianTimeZone);

    // Asynchronous Post renewal notification sending service
    private final ExecutorService asyncNotificationService = Executors.newFixedThreadPool(1);
    private Future<Boolean> futureNotificationSender = null;

    // Socket communication object
    private SocketWrapper socketWrapper;

    // Shutdown service
    private ShutDownService shutdownService;
    private boolean isShutdown;

    private SynchronizedFileWriter synchronizedFileWriter;

    public Subscription(int threadNo, GlobalDatabaseData databaseData, ShutDownService shutdownService, RenewUserList q, BlockingQueue<Notification> bqn) throws FileNotFoundException {
        //this.redis = new Jedis(AppConstant.RedisHost, AppConstant.RedisPort);
        dbConn = new DatabaseConnection();
        threadNumber = threadNo;
        // Initialize logWrapper
        this.logWrapper = new LogWrapper(Subscription.class.getName());
        this.checkerName = "checker_files/renew_checker_"+threadNo+".txt";
        this.usersList = q;
        this.notificationQueue = bqn;
        // Get SubscriptionGroup, SubscriptionGroup_ext tables
        this.subgroupHashtable = databaseData.getSubscriptionGroups();
        this.subgroup_extHashtable = databaseData.getSubscriptionGroupExts();
        this.stateWiseMessages = databaseData.getStateWiseMessages();

        // Set ShutDownService
        this.shutdownService = shutdownService;
        this.isShutdown = false;

        this.synchronizedFileWriter = SynchronizedFileWriter.getInstance();

        this.logWrapper.info("Subscription...");

        // If Charging interface is CGW, then initialize Socket communication object.
        if (AppConstant.ChargingInterface.toUpperCase().equals("CGW")) {
            this.socketWrapper = new SocketWrapper(new String[]{AppConstant.PrimaryHost, AppConstant.SecondaryHost},
                    new int[]{AppConstant.PrimaryPort, AppConstant.SecondaryPort},
                    AppConstant.sleepTime * 1000);
        }
    }


    @Override
    public void run() {
        super.run();
        this.ChangeToRenewal();
        dbConn.closeDBConnection();
    }

    public boolean checkShutdownAndCleanResources() {
        this.isShutdown = this.shutdownService.isShutdown();
        if(this.isShutdown) {
            this.dbConn.closeDBConnection();
            this.socketWrapper.cleanResources();
            try {
                this.futureNotificationSender.get();
            } catch(Exception exp) {}
            this.asyncNotificationService.shutdownNow();
        }
        return this.isShutdown;
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        return writer.toString();
    }

    private boolean CallHostChangingNotificationUrl() {
        String url = AppConstant.HostChangingNotificationUrl;
        String Msg = "Failed to connect with Host:  "
                + AppConstant.SecondaryHost + " Port: "
                + AppConstant.SecondaryPort;

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
            StringBuilder buffer = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    urlconninstr, "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }

            in.close();
            this.logWrapper.debug(url + " : " + buffer.toString());

        } catch (Exception exp) {
            this.logWrapper.error(getStackTrace(exp));
            return false;
        }
        return true;
    }

    private String GetDateString(Date date) throws ParseException {
        return FORMATTER.format(date);
    }

    private String WebServiceMessage(String url) {
        try {
            URL serverUrl = null;
            HttpURLConnection conn = null;
            InputStream urlconninstr;
            serverUrl = new URL(url);
            conn = (HttpURLConnection) serverUrl.openConnection();
            conn.setDoOutput(false);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(AppConstant.HTTP_CONNECTION_TIMEOUT);
            conn.connect();
            this.logWrapper.debug(url);
            urlconninstr = conn.getInputStream();
            StringBuilder buffer = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    urlconninstr, "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            in.close();
            urlconninstr.close();
            this.logWrapper.debug(buffer.toString());
            return buffer.toString();
        } catch (Exception e) {
            this.logWrapper.error(getStackTrace(e));
            return AppConstant.CGW_CONNECTION_ERROR;
        }
    }

    private String GetChargingParameterIntegrated(String subscriptionGroupID, String msisdn, String ServiceIDSuffix, String aquisitionChannel ){
        SubscriptionGroup currentSubscriptionGroup;
        currentSubscriptionGroup = (SubscriptionGroup) subgroupHashtable
                .get(subscriptionGroupID.toLowerCase());

        int tempServiceduration = currentSubscriptionGroup.getServiceDuration();
        if(AppConstant.NumericMsisdn == 0 && msisdn.startsWith("+"))
        {
            msisdn = msisdn.substring(1);
        }
        String bno = currentSubscriptionGroup.getBNI();
        if(bno == null)
            bno = "";

        String service_name = currentSubscriptionGroup.getRemarks();
        if(service_name == null || service_name.equals(""))
            service_name = "NA";

        String ServiceID = currentSubscriptionGroup.getServiceID();
        if(ServiceID == null || ServiceID.equals(""))
            ServiceID = "NA";
        ServiceID = ServiceID + ServiceIDSuffix;

        String transactionid = createTransactionId();
        return msisdn+"|"+bno+"|0|"+ServiceID+"|"+transactionid+"|0|0|"+service_name+"|"+subscriptionGroupID+"|"+aquisitionChannel;
    }

    public String createTransactionId() {
        String[] currentDateParts = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
                .format(new Date())
                .split("-");

        long totalNumberOfSeconds = getTotalNumberOfSeconds(Integer.parseInt(currentDateParts[3]),
                Integer.parseInt(currentDateParts[4]),
                Integer.parseInt(currentDateParts[5]));
        int totalNumberOfDaysPassed = getTotalNumberOfDaysPassed(Integer.parseInt(currentDateParts[0]),
                Integer.parseInt(currentDateParts[1]),
                Integer.parseInt(currentDateParts[2]));

        StringBuilder transactionIdBuilder = new StringBuilder();
        transactionIdBuilder.append(currentDateParts[0].substring(2)); // Last two digits of the current year.
        transactionIdBuilder.append(String.format("%03d", totalNumberOfDaysPassed)); // Total number of days passed including current day.
        transactionIdBuilder.append(String.format("%05d", totalNumberOfSeconds)); // Total number of seconds passed in the current day.
        transactionIdBuilder.append(Math.round(Math.random() * 899 + 100)); // 3 digits random number.
        transactionIdBuilder.append(Math.round(Math.random() * 899 + 100)); // Another 3 digits random number.

        return transactionIdBuilder.toString();
    }

    public long getTotalNumberOfSeconds(int hours, int minutes, int seconds) {
        int HOUR_TO_MINUTE_CONVERTER = 60;
        int MINUTE_TO_SECOND_CONVERTER = 60;
        return (hours * HOUR_TO_MINUTE_CONVERTER + minutes) * MINUTE_TO_SECOND_CONVERTER + seconds;
    }

    public int getTotalNumberOfDaysPassed(int year, int month, int daysInMonth) {
        int[] aggregateDays = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
        int additionalDay = isLeapYear(year) ? 1 : 0;
        return aggregateDays[month - 1] + daysInMonth + additionalDay ;
    }

    public boolean isLeapYear(int year) {
        if((year % 400) == 0) {
            return true;
        } else if((year % 100) == 0) {
            return false;
        } else if((year % 4) == 0) {
            return true;
        } else {
            return false;
        }
    }

    private void ChangeToRenewal() {
        UserInfo userinfoTemp = null;
        try {
            String appid = AppConstant.ApplicationId;
            String apppass = AppConstant.ApplicationPassword;
            String commandid = AppConstant.CommandId;
            List<Log> logWrite = new ArrayList<Log>();
            while ( true ) {
                while(SubscriberPick.isRunnable()) {
                    String ChargingRequestString = null;
                    String ResponseMessage = null;
                    String SessionId = null;
                    String ResultCode = null;
                    String subscriptionGroupID = null;
                    int serviceDuration = 0;
                    int renewNotificationDays = 0;
                    String allowDowngrade = null;
                    String downGradeTo = null;
                    int gracePeriod = 0;
                    int retryRenewalPeriod = 0;
                    int InGracePeriodSuffix = 0;
                    SubscriptionGroup currentSubscriptionGroup;
                    SubscriptionGroup_ext currentSubscriptionGroup_ext;

                    try {
                        userinfoTemp = this.usersList.getUser();
                    } catch (InterruptedException e) {
                        this.logWrapper.error(e);
                    }
                    // notify pick thread before getting shut down
                    if (shutdownService.isShutdown()) {
                        this.usersList.notifyAllPickers();
                        this.logWrapper.info("Shutting down Subscription thread " + this.threadNumber);
                        break;
                    }
                    if (userinfoTemp == null) continue;
                    /*
                    String deregKey = "deregistered_"+userinfoTemp.getId();
                    try {
                        if(this.redis.exists(deregKey))
                            continue;
                    } catch(Exception e) {
                        if(this.isDeregisteredMysql(userinfoTemp.getId()))
                            continue;
                    }*/
                    String check_key = userinfoTemp.getId()+" | "+userinfoTemp.getMsisdn()+" ## ";
                    this.appendChecker = new FileWriter(this.checkerName, true);
                    this.appendChecker.write(check_key);
                    this.appendChecker.close();
                    String tempStatus = userinfoTemp.getStatus();
                    subscriptionGroupID = userinfoTemp
                            .getOriginalSubscriptionGroupID();
                    while (true) {

                        currentSubscriptionGroup = (SubscriptionGroup) subgroupHashtable
                                .get(subscriptionGroupID.toLowerCase());
                        currentSubscriptionGroup_ext = (SubscriptionGroup_ext) subgroup_extHashtable
                                .get(subscriptionGroupID.toLowerCase());
                        allowDowngrade = currentSubscriptionGroup
                                .getAllowDowngrade();
                        downGradeTo = currentSubscriptionGroup.getDownGradeTo();
                        serviceDuration = currentSubscriptionGroup
                                .getServiceDuration();
                        renewNotificationDays = currentSubscriptionGroup
                                .getRenewNotificationDays();
                        if (currentSubscriptionGroup_ext == null)
                            InGracePeriodSuffix = 0;
                        else
                            InGracePeriodSuffix = currentSubscriptionGroup_ext
                                    .getInGracePeriodSuffix();
                        if (AppConstant.ChargingInterface.toUpperCase().equals(
                                "CGW")) {
                            String cmdparam = null;
                            ResponseMessage = null;

                            String tempServiceIDSuffix;
                            if ((tempStatus.toUpperCase()
                                    .equals("INGRACEPERIOD"))
                                    && (InGracePeriodSuffix == 1)) {
                                tempServiceIDSuffix = (AppConstant.ServiceIDSuffix)
                                        + tempStatus;
                            } else
                                tempServiceIDSuffix = AppConstant.ServiceIDSuffix;

                            cmdparam = GetChargingParameterIntegrated(subscriptionGroupID,
                                    userinfoTemp.getMsisdn(),
                                    tempServiceIDSuffix,
                                    userinfoTemp.getAquisitionChannel());

                            ChargingRequestString = "appid=" + appid + "&apppass="
                                    + apppass + "&cmdid=" + commandid
                                    + "&cmdparam=" + cmdparam;

                            ResponseMessage = this.socketWrapper.getResponse(ChargingRequestString);// "12345 2002 101 0 0"

                            this.logWrapper.warn(ChargingRequestString + " : "
                                    + ResponseMessage);
                        } else if (AppConstant.ChargingInterface.toUpperCase()
                                .equals("WEBSERVICE")) {
                            String msisdn = userinfoTemp.getMsisdn();
                            if(AppConstant.NumericMsisdn == 0 && msisdn.startsWith("+"))
                            {
                                msisdn = msisdn.substring(1);
                            }
                            String url = String.format(AppConstant.ChargingUrl,
                                    msisdn,
                                    currentSubscriptionGroup.getServiceID(),
                                    currentSubscriptionGroup.getSubscriptionGroupID(),
                                    currentSubscriptionGroup.getServiceDuration(),
                                    currentSubscriptionGroup.getCpID());
                            ResponseMessage = WebServiceMessage(url);
                            this.logWrapper.debug(url + " : " + ResponseMessage);
                        } else if (AppConstant.ChargingInterface.toUpperCase()
                                .equals("DELAYEDCHARGING")) {
                            break;
                        }
                        String[] ResponseMessageParts = ResponseMessage.split(" ");
                        if (((AppConstant.ChargingInterface.toUpperCase()
                                .equals("CGW")) && (ResponseMessageParts.length == 5))
                                || ((AppConstant.ChargingInterface.toUpperCase()
                                .equals("WEBSERVICE")) && (ResponseMessageParts.length == 2))) {
                            SessionId = ResponseMessageParts[0];
                            ResultCode = ResponseMessageParts[1];

                        } else {
                            continue;
                        }
                        if (AppConstant.ChargingInterface.toUpperCase().equals(
                                "DELAYEDCHARGING")) {
                            break;
                        } else if (ResultCode.equals(AppConstant.CGW_SUCCESS_CODE)) {
                            break;
                        } else {
                            if (allowDowngrade.toUpperCase().equals("YES"))
                                subscriptionGroupID = downGradeTo;
                            else
                                break;
                        }
                    }
                    List Temp_FailedCodesArray = Arrays
                            .asList(AppConstant.FailedCodesArray);
                    if (ResultCode.equals(AppConstant.CGW_SUCCESS_CODE)
                            && AppConstant.ChargingInterface.toUpperCase().equals(
                            "DELAYEDCHARGING")) {

                    } else if (ResultCode.equals(AppConstant.CGW_SUCCESS_CODE)) {
                        String updatedStatus = "Registered", consent;
                        long difference;

                        Date updatedNextRenewalDate = new Date();
                        if (userinfoTemp.getStatus().toUpperCase()
                                .equals("RENEWALFAILED")) {
                            difference = (long) ((updatedNextRenewalDate.getTime()) - (userinfoTemp
                                    .getNextRenewalDate().getTime()));
                            difference = (long) TimeUnit.DAYS.convert(difference,
                                    TimeUnit.MILLISECONDS);
                            SubscriptionGroup subscriptionGroupNew;
                            subscriptionGroupNew = (SubscriptionGroup) subgroupHashtable
                                    .get(userinfoTemp.getSubscriptionGroupID()
                                            .toLowerCase());
                            if (difference < 0)
                                difference = 0;

                            if (difference < subscriptionGroupNew
                                    .getMaxRenewalDay()) {
                                updatedNextRenewalDate = Dateadd("day",
                                        serviceDuration,
                                        userinfoTemp.getNextRenewalDate());
                            } else {
                                updatedNextRenewalDate = Dateadd("day",
                                        serviceDuration, updatedNextRenewalDate);

                            }

                        } else if (userinfoTemp.getStatus().toUpperCase()
                                .equals("INGRACEPERIOD")) {
                            // updatedNextRenewalDate =
                            // Dateadd("day",serviceDuration,
                            // updatedNextRenewalDate);
                            difference = (long) ((updatedNextRenewalDate.getTime()) - (userinfoTemp
                                    .getNextRenewalDate().getTime()));
                            difference = (long) TimeUnit.DAYS.convert(difference,
                                    TimeUnit.MILLISECONDS);
                            SubscriptionGroup subscriptionGroupNew;
                            subscriptionGroupNew = (SubscriptionGroup) subgroupHashtable
                                    .get(userinfoTemp.getSubscriptionGroupID()
                                            .toLowerCase());
                            if (difference < 0)
                                difference = 0;

                            if (difference < subscriptionGroupNew
                                    .getMaxGracePeriod()) {
                                updatedNextRenewalDate = Dateadd("day",
                                        serviceDuration,
                                        userinfoTemp.getNextRenewalDate());
                            } else {
                                updatedNextRenewalDate = Dateadd("day",
                                        serviceDuration, updatedNextRenewalDate);

                            }

                        } else {
                            if (AppConstant.BackwardChargingForDelayedRegisteredEnable == 1) {
                                updatedNextRenewalDate = Dateadd("day",
                                        serviceDuration,
                                        userinfoTemp.getNextRenewalDate());
                            } else {
                                updatedNextRenewalDate = Dateadd("day",
                                        serviceDuration,
                                        updatedNextRenewalDate);
                            }
                        }

                        Date updatedChargingDueDate = updatedNextRenewalDate;
                        Date nextNotificationDate = Dateadd("day",
                                -renewNotificationDays, updatedNextRenewalDate);
                        // update userinfoTemp
                        if (userinfoTemp.getautorenew().toUpperCase().equals("N")) {
                            userinfoTemp.setconsent("N");
                        }
                        String beforeUpdate = userinfoTemp.getStatus();
                        userinfoTemp.setStatus(updatedStatus);
                        userinfoTemp.setNextRenewalDate(updatedNextRenewalDate);
                        userinfoTemp.setChargingDueDate(updatedChargingDueDate);
                        userinfoTemp.setNextNotificationDate(nextNotificationDate);
                        userinfoTemp.setSubscriptionGroupID(subscriptionGroupID);
                        consent = userinfoTemp.getconsent().toUpperCase();

                        String QueryString = "update subscriberservices  set status = '"
                                + updatedStatus
                                + "' ,NextRenewaldate='"
                                + FORMATTER.format(updatedNextRenewalDate)
                                + "' ,ChargingDueDate='"
                                + FORMATTER.format(updatedChargingDueDate)
                                + "' ,NextNotificationDate='"
                                + FORMATTER.format(nextNotificationDate)
                                + "' , LastUpdate= NOW()"
                                + " , consent='"
                                + consent
                                + "', SubscriptionGroupID='"
                                + subscriptionGroupID
                                + "' where id =" + userinfoTemp.getId()
                                + " AND status<>'Deregistered'";
                        this.logWrapper.debug(QueryString);
                        int stime = AppConstant.TimeIntervalForAttempt;
                        for (int attempt = 0; attempt <= AppConstant.NumberOfAttempt; attempt++) {
                            try {
                                statement = dbConn.getConnection().createStatement();
                                int row = statement.executeUpdate(QueryString);
                                statement.close();
                                if(row < 1) this.logWrapper.info(userinfoTemp.getId() + " |No update. Deregistered when inside q| "+ FORMATTER.format(updatedChargingDueDate));
                                else this.logWrapper.info(userinfoTemp.getId() + "| Success | " + updatedStatus + " | " + FORMATTER.format(updatedChargingDueDate));
                                break;
                            } catch (Exception exp) {
                                this.logWrapper.error(exp);
                            }
                            Thread.sleep(stime);
                            stime *= 2;
                        }
                        Notification temp = new Notification(
                                userinfoTemp.getMsisdn(), subscriptionGroupID,
                                beforeUpdate, updatedStatus,
                                userinfoTemp.getNextRenewalDate(),
                                userinfoTemp.getNextRenewalDate());
                        this.addNotificationIfEligible(temp);
                        logWrite.add(new Log(commandid, userinfoTemp.getMsisdn(),
                                userinfoTemp.getbNI(), subscriptionGroupID,
                                SessionId, ResultCode, beforeUpdate, updatedStatus));
                    } else if (ResultCode.equals(AppConstant.DeRegResultCode)) {

                        int flag = makeDeregister(userinfoTemp, true);
                        if (flag == 0)
                            continue;
                    } else if (AppConstant.FailedCodesArray.length == 0
                            || Temp_FailedCodesArray.contains(ResultCode)) {

                        String QueryString;
                        int NextRetryPeriod = 0;

                        String updatedStatus = null;

                        Date currentSystemDate = new Date();

                        Date updatedChargingDueDate = currentSystemDate;
                        String deregistrationDate = "NULL";
                        gracePeriod = 0;
                        retryRenewalPeriod = 0;
                        SubscriptionGroup subscriptionGroup;
                        subscriptionGroup = (SubscriptionGroup) subgroupHashtable
                                .get(userinfoTemp.getSubscriptionGroupID()
                                        .toLowerCase());
                        gracePeriod = subscriptionGroup.GracePeriod;
                        retryRenewalPeriod = subscriptionGroup.RetryRenewalPeriod;
                        if (!(subscriptionGroup.NextRetryPeriod.equals("-1"))) {
                            NextRetryPeriod = getNextRetryPeriod(subscriptionGroup.NextRetryPeriod);
                        }


                        boolean checkRetryRenewalPeriod = true;
                        boolean makeDeRegistered = true;
                        Date ExpiraryDate, nextChargingDueDate;
                        if (!(subscriptionGroup.NextRetryPeriod.equals("-1"))) {
                            if (NextRetryPeriod < 1440) {
                                ExpiraryDate = Dateadd("min", NextRetryPeriod,
                                        trim(userinfoTemp.getChargingDueDate()));
                            } else {
                                ExpiraryDate = Dateadd("min", NextRetryPeriod,
                                        trim(currentSystemDate));
                            }
                        } else {
                            ExpiraryDate = Dateadd("min", 1440,
                                    userinfoTemp.getChargingDueDate());
                        }

                        if (gracePeriod > 0) {
                            Date maxValidityWithGracePeriod = Dateadd("day",
                                    gracePeriod, userinfoTemp.getNextRenewalDate());
                            if (!(subscriptionGroup.NextRetryPeriod.equals("-1"))) {
                                nextChargingDueDate = Dateadd("min",
                                        NextRetryPeriod,
                                        trim(currentSystemDate));
                            } else {
                                nextChargingDueDate = Dateadd("min", 1440,
                                        currentSystemDate);
                            }
                            if (maxValidityWithGracePeriod
                                    .compareTo(nextChargingDueDate) >= 0) {
                                updatedStatus = "InGracePeriod";
                                ExpiraryDate = Dateadd("day", gracePeriod,
                                        userinfoTemp.getNextRenewalDate());
                                updatedChargingDueDate = nextChargingDueDate;
                                checkRetryRenewalPeriod = false;
                                makeDeRegistered = false;
                            }
                        }
                        Date nextChargingDue;
                        if (checkRetryRenewalPeriod && retryRenewalPeriod > 0) {
                            Date maxValidityWithRetryPeriod = Dateadd("day",
                                    gracePeriod + retryRenewalPeriod,
                                    userinfoTemp.getNextRenewalDate());
                            if (!(subscriptionGroup.NextRetryPeriod.equals("-1"))) {
                                nextChargingDue = Dateadd("min", NextRetryPeriod,
                                        trim(currentSystemDate));
                            } else {
                                nextChargingDue = Dateadd("min", 1440,
                                        currentSystemDate);
                            }
                            if (maxValidityWithRetryPeriod
                                    .compareTo(nextChargingDue) >= 0) {
                                updatedStatus = "RenewalFailed";
                                ExpiraryDate = nextChargingDue;
                                updatedChargingDueDate = nextChargingDue;
                                makeDeRegistered = false;
                            }
                        }
                        String Dereg_Channel_state = "";
                        if (makeDeRegistered) {
                            Dereg_Channel_state = "Subscription";
                            updatedStatus = "Deregistered";

                            deregistrationDate = "NOW()";

                        }
                        QueryString = "update  subscriberservices  set status = '"
                                + updatedStatus + "', ChargingDueDate='"
                                + FORMATTER.format(updatedChargingDueDate)
                                + "' ,DeregistrationDate=" + deregistrationDate
                                + " ,LastUpdate = NOW() ,Dereg_Channel='"
                                + Dereg_Channel_state + "' where id ="
                                + userinfoTemp.getId() +" AND status<>'Deregistered'";
                        this.logWrapper.debug(QueryString);
                        try {
                            statement = dbConn.getConnection().createStatement();
                            int r = statement.executeUpdate(QueryString);
                            if(r < 1) this.logWrapper.info(userinfoTemp.getId()+"| No update. Deregistered inside q | ");
                        } catch (Exception exp) {
                            //undoTakingToProcessingState(userinfoTemp);
                            this.logWrapper.error(getStackTrace(exp));
                            continue;
                        }
                        statement.close();
                        Notification temp = new Notification(
                                userinfoTemp.getMsisdn(), subscriptionGroupID,
                                userinfoTemp.getStatus(), updatedStatus,
                                updatedChargingDueDate, ExpiraryDate);
                        this.addNotificationIfEligible(temp);
                        logWrite.add(new Log(commandid, userinfoTemp.getMsisdn(),
                                userinfoTemp.getbNI(), subscriptionGroupID,
                                SessionId, ResultCode, userinfoTemp.getStatus(),
                                updatedStatus));
                    } else {
                        this.logWrapper.error(" Invalid Result Code: " + ResultCode + " "
                                + userinfoTemp.getMsisdn() + " "
                                + userinfoTemp.getSubscriptionGroupID() + " "
                                + userinfoTemp.getServiceName() + " "
                                + userinfoTemp.getStatus());
                        //undoTakingToProcessingState(userinfoTemp);
                    }
                    this.usersList.balance[userinfoTemp.pickerNo].decrementAndGet();
                    this.readFile = new Scanner(new File(this.checkerName));
                    String toWrite = this.readFile.useDelimiter("\\A").next().replace(check_key, "");
                    this.readFile.close();

                    this.replaceChecker = new PrintWriter(this.checkerName);
                    this.replaceChecker.print(toWrite);
                    this.replaceChecker.close();
                    if (AppConstant.LogEnable == 1)
                        logGeneration(logWrite);
                    logWrite.clear();
                }
                if (shutdownService.isShutdown()) {
                    this.usersList.notifyAllPickers();
                    this.logWrapper.info("Shutting down Subscription thread " + this.threadNumber);
                    break;
                }
            }
        } catch (Exception exp) {
            this.logWrapper.error(getStackTrace(exp));
        }
    }

    public boolean isDeregisteredMysql(long userid)
    {
        String q = "select * from subscriberservices where Status='Deregistered' and id="+userid;
        boolean retValue;
        try {
            statement = dbConn.getConnection().createStatement();
            resultSet = statement.executeQuery(q);
            if(resultSet.next())
            {
                retValue = true;
            }
            else
            {
                retValue = false;
            }
            statement.close();
            resultSet.close();
        } catch (Exception exp) {
            this.logWrapper.error(getStackTrace(exp));
            retValue = true;
        }
        return retValue;
    }

    private void addNotificationIfEligible(Notification notification) {
        String SubscriptionGroupID = notification.getSubscriptionGroupID();
        String FromState = notification.getFromState();
        String ToState = notification.getToState();

        String key = FromState + "|" + ToState + "|" + SubscriptionGroupID;
        key = key.toLowerCase();

        if(this.stateWiseMessages.get(key) != null) {
            try {
                this.notificationQueue.put(notification);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private int getNextRetryPeriod(String nextRetryPeriod) {
        String[] allPeriods = nextRetryPeriod.split(",");
        String hr, min;
        int noOfPeriods, getFlag = 0, i, totalMinute;
        noOfPeriods = allPeriods.length;
        Date currentDate = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("mm");
        min = ft.format(currentDate);

        ft = new SimpleDateFormat("HH");
        hr = ft.format(currentDate);

        totalMinute = Integer.valueOf(min) + (Integer.valueOf(hr) * 60);

        for (i = 0; i < noOfPeriods; i++) {
            if (totalMinute < Integer.valueOf(allPeriods[i])) {
                getFlag = 1;
                break;
            }
        }

        if (getFlag == 1)
            return Integer.valueOf(allPeriods[i]);
        else
            return (Integer.valueOf(allPeriods[0]) + 1440);
    }

    private int makeDeregister(UserInfo userinfoTemp, boolean renewaltried) {
        String updatedStatus = "Deregistered";
        String Dereg_Channel_state = "Subscription";

        Date deregistrationDate = new Date();
        String QueryString = "update  subscriberservices  set status = '"
                + updatedStatus + "' ,DeregistrationDate='"
                + FORMATTER.format(deregistrationDate) + "' ,LastUpdate=NOW(), Dereg_Channel='"
                + Dereg_Channel_state + "' where id =" + userinfoTemp.getId();
        this.logWrapper.info(QueryString);

        try {
            statement = dbConn.getConnection().createStatement();
            statement.executeUpdate(QueryString);
            statement.close();
            return 1;

        } catch (Exception exp) {
            this.logWrapper.error(getStackTrace(exp));
            return 0;
        }

    }

    private void logGeneration(List<Log> logWriteMessage) {
        for (int i = 0; i < logWriteMessage.size(); i++) {
            String message = "";
            if (AppConstant.ChargingInterface.toUpperCase().equals("CGW"))
                message = "Message[" + logWriteMessage.get(i).commandId + ","
                        + logWriteMessage.get(i).aNI + ","
                        + logWriteMessage.get(i).bNI + ","
                        + logWriteMessage.get(i).subscriptionGroupID + "]";
            else
                message = "Message[" + logWriteMessage.get(i).aNI + ","
                        + logWriteMessage.get(i).subscriptionGroupID + "]";
            String response = "Response[" + logWriteMessage.get(i).sessionId
                    + "," + logWriteMessage.get(i).resultCode + "]";
            String status = "Status[" + logWriteMessage.get(i).currentStatus
                    + " to " + logWriteMessage.get(i).updatedStatus + "]";
            String logMessage = message + " " + response + " " + status;
            this.logWrapper.info(logMessage);
        }
    }


    private Date Dateadd(String opt, int Interval, Date frmDate) {
        this.calendar.setTime(frmDate);

        if (opt.equals("month")) {
            calendar.add(Calendar.MONTH, Interval);
        } else if (opt.equals("day")) {
            calendar.add(Calendar.DATE, Interval);
        } else if (opt.equals("min")) {
            calendar.add(Calendar.MINUTE, Interval);
        } else if (opt.equals("sec")) {
            calendar.add(Calendar.SECOND, Interval);
        }

        return this.calendar.getTime();
    }

    private Date trim(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTime();
    }
}


