package com.ssdtech.main;

import com.ssdtech.model.GlobalDatabaseData;
import com.ssdtech.model.SubscriptionGroup;
import com.ssdtech.model.UserInfo;
import com.ssdtech.utils.LogWrapper;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

/**
 * Created by Raiyan on 07-Jun-17.
 */
public class SubscriberPick extends Thread {
    public final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:m:ss");
    boolean isBusy=false;
    private SynchronizedFileWriter fileWriter;
    ShutDownService sd;
    private DatabaseConnection db;
    RenewUserList sharedQueue;
    private Statement statement = null;
    private ResultSet resultSet = null;
    private Hashtable<String, SubscriptionGroup> subgroupHashtable;
    public int threadNo;
    private LogWrapper logWrapper;

    public SubscriberPick(int tno, GlobalDatabaseData databaseData, RenewUserList bq, ShutDownService ss)
    {
        sharedQueue = bq;
        db = new DatabaseConnection();
        logWrapper = new LogWrapper(SubscriberPick.class.getName());
        threadNo = tno;
        subgroupHashtable = databaseData.getSubscriptionGroups();
        this.sd = ss;
    }

    @Override
    public void run()
    {
        while(true)
        {
            if(this.sd.isShutdown())
            {
                break;
            }
            while(this.isRunnable()) {
                if(this.sd.isShutdown())
                {
                    this.logWrapper.info("Shutting down SubscriberPick thread "+ this.threadNo);
                    break;
                }

                try {
                    this.pickSubscribers();
                } catch (InterruptedException e) {
                    this.logWrapper.error(e);
                } catch (Exception e) {
                    this.logWrapper.error(e);
                }

                if(this.sd.isShutdown())
                {
                    this.logWrapper.info("Shutting down SubscriberPick thread "+ this.threadNo);
                    break;
                }
                try {
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    this.logWrapper.error(e);
                } catch (Exception e) {
                    this.logWrapper.error(e);
                }
                try {
                    Thread.sleep(AppConstant.sleepTime * 1000);
                } catch (InterruptedException e) {
                    this.logWrapper.error(e);
                }
            }
            if(this.sd.isShutdown())
            {
                break;
            }
            try {
                Thread.sleep(AppConstant.sleepTime * 1000);
            } catch (InterruptedException e) {
                this.logWrapper.error(e);
            }
        }
    }

    public static boolean isRunnable() {
        if (AppConstant.TimeSlabEnable == 1) {
            Date getDate = new Date();
            Calendar now = Calendar.getInstance(AppConstant.malaysianTimeZone);
            now.setTime(getDate);
            long time = getDate.getTime();
            now.setTimeInMillis(time);
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);
            minute = (hour * 60) + minute;
            // this.logWrapper.debug(now.getTime()+ " Application is Checking.......");
            if (minute >= AppConstant.SlabStartMinute
                    && minute <= AppConstant.SlabEndMinute)
                return true;
            else
                return false;
        }
        return true;
    }
    private void pickSubscribers() throws InterruptedException {
        this.isBusy = true;
        String addCondition;
        if(AppConstant.PickOnlyForToday == 1)
        {
            addCondition = " AND ChargingDueDate >= DATE_ADD(DATE(NOW()),INTERVAL 0 second) ";
        }
        else
        {
            addCondition = "";
        }
        String subscriberPickUpQuery = "SELECT "
                + "ss.id,ss.msisdn,ss.SubscriptionGroupID,ss.Status,"
                + "ss.RegistrationDate,ss.DowngradedSubscriptionGroupID,"
                + "ss.NextRenewalDate,ss.ChargingDueDate, ss.RenewRequestTime,"
                + "ss.Channel,ss.autorenew,ss.consent,ss.ConsentDeadline "
                + "FROM subscriberservices ss "
                + " WHERE "
                +" ss.id%"
                + AppConstant.NumberOfRenewalPickThreads
                + "="
                + threadNo
                + " AND "
                + "(ChargingDueDate <=NOW() "+addCondition+")"
                + " AND (ss.Status='Downgraded' OR ss.Status='Registered' OR ss.Status='InGracePeriod' OR ss.Status='RenewalFailed' OR ss.Status='WaitingForRenewal')"
                + " AND " + AppConstant.RenewAdditionalCondition
                + " LIMIT " + AppConstant.RenewPickupNo;

        UserInfo userinfo = null;
        try {
            statement = db.getConnection().createStatement();
            resultSet = statement.executeQuery(subscriberPickUpQuery);
            this.logWrapper.info(subscriberPickUpQuery);
            while (resultSet.next()) {
                String subscriptionGroupId = resultSet.getString("SubscriptionGroupID");
                if(subscriptionGroupId == null) {
                    continue;
                }
                SubscriptionGroup subscriptionGroup = this.subgroupHashtable.get(subscriptionGroupId.toLowerCase());
                if(subscriptionGroup == null) {
                    continue;
                }
                userinfo = new UserInfo();
                userinfo.setId(resultSet.getLong("id"));
                userinfo.setMsisdn(resultSet.getString("msisdn"));
                userinfo.setSubscriptionGroupID(subscriptionGroupId);
                //userinfo.setOriginalSubscriptionGroupID(resultSet
                //		.getString("OriginalSubscriptionGroupID"));

                userinfo.setOriginalSubscriptionGroupID(subscriptionGroup.getOriginalSubscriptionGroupID());
                userinfo.setbNI(subscriptionGroup.getBNI());
                userinfo.setServiceName(subscriptionGroup.getRemarks());

                if (userinfo.getOriginalSubscriptionGroupID() == null
                        || userinfo.getOriginalSubscriptionGroupID().trim() == ""
                        || userinfo.getOriginalSubscriptionGroupID().isEmpty())
                    userinfo.setOriginalSubscriptionGroupID(userinfo
                            .getSubscriptionGroupID());

                userinfo.setStatus(resultSet.getString("Status"));
                userinfo.setRegistrationDate(resultSet.getTimestamp("RegistrationDate"));
                userinfo.setDowngradedSubscriptionGroupID(resultSet.getString("DowngradedSubscriptionGroupID"));
                userinfo.setNextRenewalDate(resultSet.getTimestamp("NextRenewalDate"));
                userinfo.setChargingDueDate(resultSet.getTimestamp("ChargingDueDate"));

                //userinfo.setbNI(resultSet.getString("BNI"));
                //userinfo.setServiceName(resultSet.getString("ServiceName"));

                userinfo.setAquisitionChannel(resultSet.getString("Channel"));
                userinfo.setautorenew(resultSet.getString("autorenew"));
                userinfo.setconsent(resultSet.getString("consent"));
                userinfo.setConsentDeadline(resultSet.getTimestamp("ConsentDeadline"));

                if (AppConstant.ChargingInterface.toUpperCase().equals("DELAYEDCHARGING")) {
                    userinfo.setRenewRequestTime(resultSet.getTimestamp("RenewRequestTime"));
                }

                String autorenewvalue = resultSet.getString("autorenew"),
                        consentvalue = resultSet.getString("consent");
                Date consentdeadline = resultSet.getTimestamp("ConsentDeadline");
                Date currentDate = new Date();
                if (((autorenewvalue.toUpperCase().equals("Y")))
                        || ((autorenewvalue.toUpperCase().equals("N")) && (consentdeadline
                        .after(currentDate)))
                        || ((autorenewvalue.toUpperCase().equals("N"))
                        && (consentdeadline.before(currentDate)) && (consentvalue
                        .toUpperCase().equals("Y")))) {
                    sharedQueue.balance[this.threadNo].incrementAndGet();
                    sharedQueue.insertUser(this.threadNo,userinfo);
                }

                else {
                    makeDeregister(userinfo, false);
                }
            }
            resultSet.close();
            statement.close();
            this.isBusy = false;
        } catch (Exception exp) {
            this.logWrapper.error(subscriberPickUpQuery + ": " + getStackTrace(exp));

            if(resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    this.logWrapper.error(getStackTrace(e));
                }
            }

            if(statement != null) {
                try {
                    statement.close();
                } catch(SQLException e) {
                    this.logWrapper.error(getStackTrace(e));
                }
            }
        }
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        return writer.toString();
    }

    private int makeDeregister(UserInfo userinfoTemp, boolean renewaltried) {
        String updatedStatus = "Deregistered";
        String Dereg_Channel_state = "Subscription";

        Date deregistrationDate = new Date();
        String QueryString = "update  subscriberservices  set status = '"
                + updatedStatus + "' ,DeregistrationDate='"
                + FORMATTER.format(deregistrationDate) + "' ,Dereg_Channel='"
                + Dereg_Channel_state + "' where id =" + userinfoTemp.getId();
        this.logWrapper.info(QueryString);

        try {
            statement = db.getConnection().createStatement();
            statement.executeUpdate(QueryString);
            statement.close();
            return 1;

        } catch (Exception exp) {
            this.logWrapper.error(getStackTrace(exp));
            return 0;
        }

    }

}
