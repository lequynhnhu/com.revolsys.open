package com.revolsys.data.filter;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.filter.Filter;

/**
 * Filter Records by the type (Java class) of the fieldName value.
 *
 * @author Paul Austin
 */
public class AttributeValueClassFilter implements Filter<Record> {
  /** The fieldName name, or path to match. */
  private String fieldName;

  /** The type to match. */
  private Class<?> type = Object.class;

  /**
   * Match the fieldName on the data object with the required value.
   *
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  @Override
  public boolean accept(final Record object) {
    final Object propertyValue = RecordUtil.getFieldByPath(object,
      this.fieldName);
    return this.type.isInstance(propertyValue);
  }

  /**
   * Get the fieldName name, or path to match.
   *
   * @return The fieldName name, or path to match.
   */
  public String getFieldName() {
    return this.fieldName;
  }

  /**
   * Get the type to match.
   *
   * @return The type to match.
   */
  public String getType() {
    return this.type.getName();
  }

  /**
   * Set the fieldName name, or path to match.
   *
   * @param fieldName The fieldName name, or path to match.
   */
  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * Set the type to match.
   *
   * @param type The type to match.
   */
  public void setType(final String type) {
    try {
      this.type = Class.forName(type);
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    return this.fieldName + " type " + this.type;
  }
}
