package com.optum.itl;


public class UserProfile {

  private int profileId;
  private String userId;
  private String userGroup;
  private String userName;
  private String emailAddress;
  private String status;
  private java.sql.Timestamp createTimestamp;
  private java.sql.Timestamp updateTimestamp;
  private String updateUserId;
  private int allowedVmTotal;
  private int currentVmTotal;


  public int getProfileId() {
    return profileId;
  }

  public void setProfileId(int profileId) {
    this.profileId = profileId;
  }


  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }


  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }


  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }


  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }


  public java.sql.Timestamp getCreateTimestamp() {
    return createTimestamp;
  }

  public void setCreateTimestamp(java.sql.Timestamp createTimestamp) {
    this.createTimestamp = createTimestamp;
  }


  public java.sql.Timestamp getUpdateTimestamp() {
    return updateTimestamp;
  }

  public void setUpdateTimestamp(java.sql.Timestamp updateTimestamp) {
    this.updateTimestamp = updateTimestamp;
  }


  public String getUpdateUserId() {
    return updateUserId;
  }

  public void setUpdateUserId(String updateUserId) {
    this.updateUserId = updateUserId;
  }


  public String getUserGroup() {
    return userGroup;
  }

  public void setUserGroup(String userGroup) {
    this.userGroup = userGroup;
  }


  public int getAllowedVmTotal() {
    return allowedVmTotal;
  }

  public void setAllowedVmTotal(int allowedVmTotal) {
    this.allowedVmTotal = allowedVmTotal;
  }


  public int getCurrentVmTotal() {
    return currentVmTotal;
  }

  public void setCurrentVmTotal(int currentVmTotal) {
    this.currentVmTotal = currentVmTotal;
  }

}
