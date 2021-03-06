package com.revolsys.data.filter;

import java.util.Arrays;
import java.util.Collection;

import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.filter.Filter;

public class AttributesEqualOrNullFilter implements Filter<Record> {
  public static boolean accept(final Record object1, final Record object2,
    final Collection<String> fieldNames) {
    for (final String fieldName : fieldNames) {
      final Object value1 = RecordUtil.getFieldByPath(object1, fieldName);
      final Object value2 = RecordUtil.getFieldByPath(object2, fieldName);

      if (value1 != null && value2 != null
          && !EqualsInstance.INSTANCE.equals(value1, value2)) {
        return false;
      }
    }
    return true;
  }

  public static boolean accept(final Record object1, final Record object2,
    final String... fieldNames) {
    return accept(object1, object2, Arrays.asList(fieldNames));
  }

  private final Collection<String> fieldNames;

  private final Record object;

  public AttributesEqualOrNullFilter(final Record object,
    final Collection<String> fieldNames) {
    this.fieldNames = fieldNames;
    this.object = object;
  }

  public AttributesEqualOrNullFilter(final Record object,
    final String... fieldNames) {
    this(object, Arrays.asList(fieldNames));
  }

  @Override
  public boolean accept(final Record object) {
    return accept(this.object, object, this.fieldNames);
  }

  @Override
  public String toString() {
    return "AttributeEquals" + this.fieldNames;
  }

}
