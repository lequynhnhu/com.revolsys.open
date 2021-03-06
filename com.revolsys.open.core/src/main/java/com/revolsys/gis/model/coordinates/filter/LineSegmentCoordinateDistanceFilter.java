package com.revolsys.gis.model.coordinates.filter;

import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.Segment;

public class LineSegmentCoordinateDistanceFilter implements Filter<Segment> {

  private final double maxDistance;

  private final Point point;

  public LineSegmentCoordinateDistanceFilter(final Point point,
    final double maxDistance) {
    this.point = point;
    this.maxDistance = maxDistance;
  }

  @Override
  public boolean accept(final Segment lineSegment) {
    final double distance = lineSegment.distance(this.point);
    if (distance < this.maxDistance) {
      return true;
    } else {
      return false;
    }
  }

}
