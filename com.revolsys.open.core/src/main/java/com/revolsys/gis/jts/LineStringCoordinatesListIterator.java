package com.revolsys.gis.jts;

import java.util.Iterator;

import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;

public class LineStringCoordinatesListIterator implements Iterator<LineString>,
  Iterable<LineString> {
  private final com.revolsys.jts.geom.GeometryFactory factory;

  private int index = 0;

  private final CoordinatesList points;

  public LineStringCoordinatesListIterator(final com.revolsys.jts.geom.GeometryFactory factory,
    final CoordinatesList points) {
    this.factory = factory;
    this.points = points;
  }

  public LineStringCoordinatesListIterator(final LineString line) {
    this(GeometryFactory.getFactory(line), CoordinatesListUtil.get(line));
  }

  @Override
  public boolean hasNext() {
    return index < points.size() - 1;
  }

  @Override
  public Iterator<LineString> iterator() {
    return this;
  }

  @Override
  public LineString next() {
    final LineString lineString = factory.createLineString(points.subList(
      index, 2));
    index++;
    return lineString;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove not supported");

  }
}
