package com.revolsys.data.record.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.data.equals.RecordEquals;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;

public class EqualIgnoreAttributes extends AbstractRecordDefinitionProperty {
  public static EqualIgnoreAttributes getProperty(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static EqualIgnoreAttributes getProperty(
    final RecordDefinition recordDefinition) {
    EqualIgnoreAttributes property = recordDefinition.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new EqualIgnoreAttributes();
      property.setRecordDefinition(recordDefinition);
    }
    return property;
  }

  public static final String PROPERTY_NAME = EqualIgnoreAttributes.class.getName()
      + ".propertyName";

  private Set<String> fieldNames = new LinkedHashSet<String>();

  public EqualIgnoreAttributes() {
  }

  public EqualIgnoreAttributes(final Collection<String> fieldNames) {
    this.fieldNames.addAll(fieldNames);
  }

  public EqualIgnoreAttributes(final String... fieldNames) {
    this(Arrays.asList(fieldNames));
  }

  public void addFieldNames(final Collection<String> fieldNames) {
    this.fieldNames.addAll(fieldNames);
  }

  public void addFieldNames(final String... fieldNames) {
    addFieldNames(Arrays.asList(fieldNames));
  }

  public Set<String> getFieldNames() {
    return this.fieldNames;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public boolean isFieldIgnored(final String fieldName) {
    return this.fieldNames.contains(fieldName);
  }

  public void setFieldNames(final Collection<String> fieldNames) {
    setFieldNames(new LinkedHashSet<String>(fieldNames));
  }

  public void setFieldNames(final Set<String> fieldNames) {
    this.fieldNames = fieldNames;
  }

  public void setFieldNames(final String... fieldNames) {
    setFieldNames(Arrays.asList(fieldNames));
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    super.setRecordDefinition(recordDefinition);
    if (this.fieldNames.contains(RecordEquals.EXCLUDE_ID)) {
      final String idFieldName = recordDefinition.getIdFieldName();
      this.fieldNames.add(idFieldName);
    }
    if (this.fieldNames.contains(RecordEquals.EXCLUDE_GEOMETRY)) {
      final String geometryFieldName = recordDefinition.getGeometryFieldName();
      this.fieldNames.add(geometryFieldName);
    }
  }

  @Override
  public String toString() {
    return "EqualIgnore " + this.fieldNames;
  }
}
