package com.optum.itl;


public class ConfigProperty {

  private long propertyId;
  private String propertyGroup;
  private String propertyName;
  private String propertyValue;
  private long propertyOrder;


  public long getPropertyId() {
    return propertyId;
  }

  public void setPropertyId(long propertyId) {
    this.propertyId = propertyId;
  }


  public String getPropertyGroup() {
    return propertyGroup;
  }

  public void setPropertyGroup(String propertyGroup) {
    this.propertyGroup = propertyGroup;
  }


  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }


  public String getPropertyValue() {
    return propertyValue;
  }

  public void setPropertyValue(String propertyValue) {
    this.propertyValue = propertyValue;
  }


  public long getPropertyOrder() {
    return propertyOrder;
  }

  public void setPropertyOrder(long propertyOrder) {
    this.propertyOrder = propertyOrder;
  }

}
