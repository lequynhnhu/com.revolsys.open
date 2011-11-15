package com.revolsys.gis.data.model;

public class GlobalIdProperty extends AbstractDataObjectMetaDataProperty {
  static final String PROPERTY_NAME = "http://revolsys.com/gis/globalId";

  public static GlobalIdProperty getProperty(final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    return getProperty(metaData);
  }

  public static GlobalIdProperty getProperty(final DataObjectMetaData metaData) {
    return metaData.getProperty(PROPERTY_NAME);
  }

  private String attributeName;

  public GlobalIdProperty() {
  }

  public GlobalIdProperty(final String attributeName) {
    this.attributeName = attributeName;
  }

  @Override
  public GlobalIdProperty clone() {
    return (GlobalIdProperty)super.clone();
  }

  public String getAttributeName() {
    return attributeName;
  }

  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  @Override
  public void setMetaData(DataObjectMetaData metaData) {
    if (attributeName == null) {
      attributeName = metaData.getIdAttributeName();
    }
    super.setMetaData(metaData);
  }
  
  public void setAttributeName(final String attributeName) {
    this.attributeName = attributeName;
  }

}