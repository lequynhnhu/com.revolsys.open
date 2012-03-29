package com.revolsys.gis.algorithm.index.visitor;

import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.index.ItemVisitor;

public class LineSegmentIntersectionVisitor implements ItemVisitor {

  private final Set<Coordinates> intersections = new LinkedHashSet<Coordinates>();

  private final LineSegment querySeg;

  public LineSegmentIntersectionVisitor(final LineSegment querySeg) {
    this.querySeg = querySeg;
  }

  public Set<Coordinates> getIntersections() {
    return intersections;
  }

  public void visitItem(final Object item) {
    final LineSegment segment = (LineSegment)item;
    if (segment.getEnvelope().intersects(querySeg.getEnvelope())) {
      final Coordinates intersection = querySeg.intersection(segment);
      if (intersection != null) {
        intersections.add(intersection);
      }
    }

  }
}