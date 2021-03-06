package com.revolsys.data.query;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.record.schema.RecordStore;

public class LeftUnaryCondition extends Condition {

  private QueryValue value;

  private final String operator;

  public LeftUnaryCondition(final String operator, final QueryValue value) {
    this.operator = operator;
    this.value = value;
  }

  @Override
  public void appendDefaultSql(final Query query,
    final RecordStore recordStore, final StringBuilder buffer) {
    buffer.append(this.operator);
    buffer.append(" ");
    this.value.appendSql(query, recordStore, buffer);
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return this.value.appendParameters(index, statement);
  }

  @Override
  public LeftUnaryCondition clone() {
    final LeftUnaryCondition clone = (LeftUnaryCondition)super.clone();
    clone.value = this.value.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof LeftUnaryCondition) {
      final LeftUnaryCondition value = (LeftUnaryCondition)obj;
      if (EqualsRegistry.equal(value.getQueryValue(), this.getQueryValue())) {
        if (EqualsRegistry.equal(value.getOperator(), this.getOperator())) {
          return true;
        }
      }
    }
    return false;
  }

  public String getOperator() {
    return this.operator;
  }

  @SuppressWarnings("unchecked")
  public <V extends QueryValue> V getQueryValue() {
    return (V)this.value;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Collections.singletonList(this.value);
  }

  @Override
  public String toString() {
    return this.operator + " " + this.value;
  }
}
