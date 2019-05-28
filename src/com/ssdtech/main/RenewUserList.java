package com.ssdtech.main;

import com.ssdtech.model.GlobalDatabaseData;
import com.ssdtech.model.UserInfo;
import com.ssdtech.utils.LogWrapper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Raiyan on 06-Nov-17.
 */
public class RenewUserList {
    public final ArrayBlockingQueue<UserInfo>[] disjointLists;
    int head, toNotify;
    AtomicInteger[] balance;
    public final GlobalDatabaseData databaseData;
    SubscriberPick[] pickers;
    ShutDownService shutDownService;
    LogWrapper logWrapper;
    public RenewUserList(GlobalDatabaseData globalDB, ShutDownService ss)
    {
        this.logWrapper = new LogWrapper(this.getClass().getName());
        this.shutDownService = ss;
        this.balance = new AtomicInteger[AppConstant.NumberOfRenewalPickThreads];
        this.pickers = new SubscriberPick[AppConstant.NumberOfRenewalPickThreads];
        this.databaseData = globalDB;
        this.head = 0;
        this.disjointLists = new ArrayBlockingQueue[AppConstant.NumberOfRenewalPickThreads];

        for(int i=0; i< AppConstant.NumberOfRenewalPickThreads; i++)
        {
            this.balance[i] = new AtomicInteger();
            this.disjointLists[i] = new ArrayBlockingQueue<UserInfo>(AppConstant.RenewPickupNo);
            this.pickers[i] = new SubscriberPick(i, globalDB, this, this.shutDownService);
            this.pickers[i].setPriority(8);
            this.pickers[i].start();
        }
    }

    public synchronized UserInfo getUser() throws InterruptedException {
        if(this.balance[this.toNotify].get() == 0)
        {
            synchronized (this.pickers[this.toNotify])
            {
                this.pickers[this.toNotify].notify();
            }
        }
        if (this.disjointLists[head].isEmpty()) {
            this.toNotify = this.head;
            int newHead = (this.head+1)%AppConstant.NumberOfRenewalPickThreads;
            while(this.pickers[newHead].isBusy)
            {
                Thread.sleep(10);
            }
            this.head = newHead;
            this.logWrapper.info("head: "+ this.head +" -> "+(this.head+1)%AppConstant.NumberOfRenewalPickThreads);
            return null;
        }
        this.logWrapper.info("take from list no: "+this.head);
        return this.disjointLists[this.head].take();
    }

    public void insertUser(int threadNo, UserInfo user) throws InterruptedException {
        this.logWrapper.info("insert in list no: "+threadNo+":: userid: "+user.getId());
        user.pickerNo = threadNo;
        this.disjointLists[threadNo].put(user);
    }

    public void notifyAllPickers()
    {
        int i;
        for(i=0; i < AppConstant.NumberOfRenewalPickThreads; i++)
        {
            synchronized (this.pickers[i]) {
                this.pickers[i].notify();
            }
        }
    }
}
