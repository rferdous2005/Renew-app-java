package com.ssdtech.model;

public class Log {
	
	public String commandId;
    public String aNI;
	public String bNI;
    public String subscriptionGroupID;
    public String sessionId;
    public String resultCode;
    public String currentStatus;
    public String updatedStatus;
    public Log(String CommandId, String ANI, String BNI, String SubscriptionGroupID, String SessionId, String ResultCode, String CurrentStatus, String UpdatedStatus)
    {
        this.commandId = CommandId;
        this.aNI = ANI;
        this.bNI = BNI;
        this.subscriptionGroupID = SubscriptionGroupID;
        this.sessionId = SessionId;
        this.resultCode = ResultCode;
        this.currentStatus = CurrentStatus;
        this.updatedStatus = UpdatedStatus;            
    }
    /**
	 * @return the commandId
	 */
	public String getCommandId() {
		return commandId;
	}
	/**
	 * @param commandId the commandId to set
	 */
	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}
	/**
	 * @return the aNI
	 */
	public String getaNI() {
		return aNI;
	}
	/**
	 * @param aNI the aNI to set
	 */
	public void setaNI(String aNI) {
		this.aNI = aNI;
	}
	/**
	 * @return the bNI
	 */
	public String getbNI() {
		return bNI;
	}
	/**
	 * @param bNI the bNI to set
	 */
	public void setbNI(String bNI) {
		this.bNI = bNI;
	}
	/**
	 * @return the subscriptionGroupID
	 */
	public String getSubscriptionGroupID() {
		return subscriptionGroupID;
	}
	/**
	 * @param subscriptionGroupID the subscriptionGroupID to set
	 */
	public void setSubscriptionGroupID(String subscriptionGroupID) {
		this.subscriptionGroupID = subscriptionGroupID;
	}
	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}
	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	/**
	 * @return the resultCode
	 */
	public String getResultCode() {
		return resultCode;
	}
	/**
	 * @param resultCode the resultCode to set
	 */
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	/**
	 * @return the currentStatus
	 */
	public String getCurrentStatus() {
		return currentStatus;
	}
	/**
	 * @param currentStatus the currentStatus to set
	 */
	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}
	/**
	 * @return the updatedStatus
	 */
	public String getUpdatedStatus() {
		return updatedStatus;
	}
	/**
	 * @param updatedStatus the updatedStatus to set
	 */
	public void setUpdatedStatus(String updatedStatus) {
		this.updatedStatus = updatedStatus;
	}

}
