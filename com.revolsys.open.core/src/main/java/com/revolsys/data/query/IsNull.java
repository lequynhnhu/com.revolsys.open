package com.revolsys.data.query;

import java.util.Map;

public class IsNull extends RightUnaryCondition {

  public IsNull(final QueryValue value) {
    super(value, "IS NULL");
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final QueryValue queryValue = getValue();
    final Object value = queryValue.getValue(record);
    return value == null;
  }
}
