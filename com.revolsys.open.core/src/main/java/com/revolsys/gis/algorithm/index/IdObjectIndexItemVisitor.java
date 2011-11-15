package com.revolsys.gis.algorithm.index;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.graph.AbstractItemVisitor;
import com.vividsolutions.jts.geom.Envelope;

public final class IdObjectIndexItemVisitor<T> extends
  AbstractItemVisitor<Integer> {
  private IdObjectIndex<T> index;

  private final Envelope envelope;

  private final Visitor<T> visitor;

  public IdObjectIndexItemVisitor(IdObjectIndex<T> index,
    Envelope envelope, Visitor<T> visitor) {
    this.index = index;
    this.envelope = envelope;
    this.visitor = visitor;
  }

  public boolean visit(final Integer id) {
    final T object = index.getObject(id);
    Envelope e = index.getEnvelope(object);
    if (e.intersects(envelope)) {
      visitor.visit(object);
    }
    return true;
  }
}