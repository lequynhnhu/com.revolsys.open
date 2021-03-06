package com.revolsys.gis.algorithm.index;

import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.visitor.CreateListVisitor;

public abstract class AbstractPointSpatialIndex<T> implements
PointSpatialIndex<T> {

  @Override
  public List<T> find(final BoundingBox envelope) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    visit(envelope, visitor);
    return visitor.getList();
  }

  @Override
  public List<T> findAll() {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    visit(visitor);
    return visitor.getList();
  }

  @Override
  public Iterator<T> iterator() {
    return findAll().iterator();
  }

}
