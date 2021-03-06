package com.revolsys.data.query.functions;

import java.util.List;
import java.util.Map;

import com.revolsys.data.query.QueryValue;
import com.revolsys.util.Property;

public class GetMapValue extends Function {

  public GetMapValue(final List<QueryValue> parameters) {
    super("get_map_value", parameters);
  }

  @Override
  public <V> V getValue(final Map<String, Object> record) {
    final Map<String, ?> map = getParameterValue(0, record);
    final String key = getParameterStringValue(1, record);
    if (map == null || !Property.hasValue(key)) {
      return null;
    } else {
      final V value = Property.get(map, key);
      return value;
    }
  }

}
