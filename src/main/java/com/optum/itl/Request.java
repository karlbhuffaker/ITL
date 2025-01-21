package com.optum.itl;

public class Request {

  private int requestId;
  private String requestType;
  private String userId;
  private String userGroup;
  private String status;
  private java.sql.Timestamp submitTimestamp;
  private java.sql.Timestamp startTimestamp;
  private java.sql.Timestamp endTimestamp;
  private String template;
  private String vmName;
  private String os;
  private int requestedCpu;
  private int requestedMemory;
  private int requestedStorage;
  private String productInstall;
  private String db;
  private String productUpgrade;
  private String kb;
  private String feLcd;
  private String peLcd;
  private String ilogSystemRules;


  public int getRequestId() {
    return requestId;
  }

  public void setRequestId(int requestId) {
    this.requestId = requestId;
  }


  public String getRequestType() {
    return requestType;
  }

  public void setRequestType(String requestType) {
    this.requestType = requestType;


  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }


  public java.sql.Timestamp getSubmitTimestamp() {
    return submitTimestamp;
  }

  public void setSubmitTimestamp(java.sql.Timestamp submitTimestamp) {
    this.submitTimestamp = submitTimestamp;
  }


  public java.sql.Timestamp getStartTimestamp() {
    return startTimestamp;
  }

  public void setStartTimestamp(java.sql.Timestamp startTimestamp) {
    this.startTimestamp = startTimestamp;
  }


  public java.sql.Timestamp getEndTimestamp() {
    return endTimestamp;
  }

  public void setEndTimestamp(java.sql.Timestamp endTimestamp) {
    this.endTimestamp = endTimestamp;
  }


  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public String getVmName() {
    return vmName;
  }

  public void setVmName(String vmName) {
    this.vmName = vmName;
  }

  public String getUserGroup() {
    return userGroup;
  }

  public void setUserGroup(String userGroup) { this.userGroup = userGroup; }

  public String getOs() {
    return os;
  }

  public void setOs(String os) {
    this.os = os;
  }

  public int getRequestedCpu() {
    return requestedCpu;
  }

  public void setRequestedCpu(int requestedCpu) {
    this.requestedCpu = requestedCpu;
  }


  public int getRequestedMemory() {
    return requestedMemory;
  }

  public void setRequestedMemory(int requestedMemory) {
    this.requestedMemory = requestedMemory;
  }


  public int getRequestedStorage() {
    return requestedStorage;
  }

  public void setRequestedStorage(int requestedStorage) {
    this.requestedStorage = requestedStorage;
  }


  public String getProductInstall() {
    return productInstall;
  }

  public void setProductInstall(String productInstall) {
    this.productInstall = productInstall;
  }


  public String getDb() {
    return db;
  }

  public void setDb(String db) {
    this.db = db;
  }


  public String getProductUpgrade() {
    return productUpgrade;
  }

  public void setProductUpgrade(String productUpgrade) {
    this.productUpgrade = productUpgrade;
  }


  public String getKb() {
    return kb;
  }

  public void setKb(String kb) {
    this.kb = kb;
  }


  public String getFeLcd() {
    return feLcd;
  }

  public void setFeLcd(String feLcd) {
    this.feLcd = feLcd;
  }


  public String getPeLcd() {
    return peLcd;
  }

  public void setPeLcd(String peLcd) {
    this.peLcd = peLcd;
  }


  public String getIlogSystemRules() {
    return ilogSystemRules;
  }

  public void setIlogSystemRules(String ilogSystemRules) {
    this.ilogSystemRules = ilogSystemRules;
  }

}
