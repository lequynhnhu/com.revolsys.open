package com.revolsys.swing.map.layer.record;

import com.revolsys.data.record.Record;

public interface LayerRecord extends Record {
  void cancelChanges();

  void clearChanges();

  void firePropertyChange(String propertyName, Object oldValue, Object newValue);

  AbstractRecordLayer getLayer();

  <T> T getOriginalValue(String attributeName);

  boolean isDeletable();

  boolean isDeleted();

  boolean isGeometryEditable();

  boolean isModified(int attributeIndex);

  boolean isModified(String name);

  boolean isSame(Record record);

  void postSaveChanges();

  LayerRecord revertChanges();

  void revertEmptyFields();
}