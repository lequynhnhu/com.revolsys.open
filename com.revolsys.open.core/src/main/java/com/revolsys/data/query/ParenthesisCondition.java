package com.revolsys.data.query;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.record.schema.RecordStore;

public class ParenthesisCondition extends Condition {

  private Condition condition;

  public ParenthesisCondition(final Condition condition) {
    this.condition = condition;
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    return this.condition.accept(record);
  }

  @Override
  public void appendDefaultSql(final Query query,
    final RecordStore recordStore, final StringBuilder buffer) {
    buffer.append("(");
    this.condition.appendSql(query, recordStore, buffer);
    buffer.append(")");
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return this.condition.appendParameters(index, statement);
  }

  @Override
  public ParenthesisCondition clone() {
    final ParenthesisCondition clone = (ParenthesisCondition)super.clone();
    clone.condition = this.condition.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ParenthesisCondition) {
      final ParenthesisCondition condition = (ParenthesisCondition)obj;
      if (EqualsRegistry.equal(condition.getCondition(), this.getCondition())) {
        return true;
      }
    }
    return false;
  }

  public Condition getCondition() {
    return this.condition;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Collections.<QueryValue> singletonList(this.condition);
  }

  @Override
  public <V> V getValue(final Map<String, Object> record) {
    return this.condition.getValue(record);
  }

  @Override
  public String toString() {
    return "(" + getCondition() + ")";
  }
}
