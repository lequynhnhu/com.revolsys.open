package com.revolsys.data.query;

import java.sql.PreparedStatement;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;

public class Column extends QueryValue {

  private final String name;

  private FieldDefinition attribute;

  public Column(final FieldDefinition attribute) {
    this.name = attribute.getName();
    this.attribute = attribute;
  }

  public Column(final String name) {
    this.name = name;
  }

  @Override
  public void appendDefaultSql(final Query query,
    final RecordStore recordStore, final StringBuilder buffer) {
    buffer.append(toString());
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return index;
  }

  @Override
  public Column clone() {
    return new Column(this.name);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Column) {
      final Column value = (Column)obj;
      return EqualsRegistry.equal(value.getName(), this.getName());
    } else {
      return false;
    }
  }

  public FieldDefinition getField() {
    return this.attribute;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String getStringValue(final Map<String, Object> record) {
    final Object value = getValue(record);
    if (this.attribute == null) {
      return StringConverterRegistry.toString(value);
    } else {
      final Class<?> typeClass = this.attribute.getTypeClass();
      return StringConverterRegistry.toString(typeClass, value);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final Map<String, Object> record) {
    final String name = getName();
    return (V)record.get(name);
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.attribute = recordDefinition.getField(getName());
  }

  @Override
  public String toString() {
    if (this.name.matches("([A-Z][_A-Z1-9]*\\.)?[A-Z][_A-Z1-9]*")) {
      return this.name;
    } else {
      return "\"" + this.name + "\"";
    }
  }
}
