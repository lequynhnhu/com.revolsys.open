package com.revolsys.gis.data.visitor;

import com.revolsys.collection.Visitor;

public class SingleObjectVisitor<T> implements Visitor<T> {
  private T object;

  public T getObject() {
    return object;
  }

  public boolean visit(
    final T object) {
    if (this.object == null) {
      this.object = object;
    }
    return false;
  }
}