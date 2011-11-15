package com.revolsys.gis.algorithm.index;

import com.revolsys.collection.Visitor;
import com.vividsolutions.jts.geom.Envelope;

public final class IdObjectIndexEnvelopeVisitor<T> implements Visitor<Integer> {
  private IdObjectIndex<T> index;

  private final Envelope envelope;

  private final Visitor<T> visitor;

  public IdObjectIndexEnvelopeVisitor(IdObjectIndex<T> index,
    Envelope envelope, Visitor<T> visitor) {
    this.index = index;
    this.envelope = envelope;
    this.visitor = visitor;
  }

  public boolean visit(final Integer id) {
    final T object = index.getObject(id);
    Envelope e = index.getEnvelope(object);
    if (e.intersects(envelope)) {
      if (!visitor.visit(object)) {
        return false;
      }
    }
    return true;
  }
}