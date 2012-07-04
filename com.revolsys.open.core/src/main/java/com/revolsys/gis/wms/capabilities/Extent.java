package com.revolsys.gis.wms.capabilities;

public class Extent {
  private String name;

  private String defaultValue;

  private boolean nearestValue = false;

  private boolean multipleValues = false;

  private boolean current = false;

  public String getDefaultValue() {
    return defaultValue;
  }

  public String getName() {
    return name;
  }

  public boolean isCurrent() {
    return current;
  }

  public boolean isMultipleValues() {
    return multipleValues;
  }

  public boolean isNearestValue() {
    return nearestValue;
  }

  public void setCurrent(final boolean current) {
    this.current = current;
  }

  public void setDefaultValue(final String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void setMultipleValues(final boolean multipleValues) {
    this.multipleValues = multipleValues;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setNearestValue(final boolean nearestValue) {
    this.nearestValue = nearestValue;
  }

}