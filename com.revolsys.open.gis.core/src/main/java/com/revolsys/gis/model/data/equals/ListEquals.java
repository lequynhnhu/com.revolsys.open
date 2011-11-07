package com.revolsys.gis.model.data.equals;

import java.util.Collection;
import java.util.List;

public class ListEquals implements Equals<List<?>> {
  private EqualsRegistry equalsRegistry;

  public boolean equals(
    final List<?> list1,
    final List<?> list2,
    final Collection<String> exclude) {
    if (list1 == null) {
      return list2 == null;
    } else if (list2 == null) {
      return false;
    } else if (list1.size() != list2.size()) {
      return false;
    } else {
      for (int i = 0; i < list1.size(); i++) {
        Object value1 = list1.get(i);
        Object value2 = list2.get(i);
        if (!equalsRegistry.equals(value1, value2, exclude)) {
          return false;
        }
      }
    }
    return true;
  }

  public void setEqualsRegistry(
    final EqualsRegistry equalsRegistry) {
    this.equalsRegistry = equalsRegistry;
  }
}