package com.revolsys.gis.algorithm;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * Represents a location along a {@link LineString}.
 */
public class LineStringLocation implements Comparable<LineStringLocation> {

  public static int compareLocationValues(
    final int segmentIndex0,
    final double segmentFraction0,
    final int segmentIndex1,
    final double segmentFraction1) {
    // compare segments
    if (segmentIndex0 < segmentIndex1) {
      return -1;
    }
    if (segmentIndex0 > segmentIndex1) {
      return 1;
    }
    // same segment, so compare segment fraction
    if (segmentFraction0 < segmentFraction1) {
      return -1;
    }
    if (segmentFraction0 > segmentFraction1) {
      return 1;
    }
    // same location
    return 0;
  }

  /**
   * Computes the location of a point a given length along a line segment. If
   * the length exceeds the length of the line segment the last point of the
   * segment is returned. If the length is negative the first point of the
   * segment is returned.
   * 
   * @param p0 the first point of the line segment
   * @param p1 the last point of the line segment
   * @param length the length to the desired point
   * @return the {@link Coordinate} of the desired point
   */
  public static Coordinate pointAlongSegmentByFraction(
    final Coordinate p0,
    final Coordinate p1,
    final double frac) {
    if (frac <= 0.0) {
      return p0;
    }
    if (frac >= 1.0) {
      return p1;
    }
    final double x = (p1.x - p0.x) * frac + p0.x;
    final double y = (p1.y - p0.y) * frac + p0.y;
    return new Coordinate(x, y);
  }

  private final LineString line;

  private double segmentFraction;

  private int segmentIndex;

  public LineStringLocation(
    final LineString line,
    final int segmentIndex,
    final double segmentFraction) {
    this.line = line;
    this.segmentIndex = segmentIndex;
    this.segmentFraction = segmentFraction;
    normalize();
  }

  @Override
  public Object clone() {
    return new LineStringLocation(line, segmentIndex, segmentFraction);
  }

  /**
   * Compares this object with the specified object for order.
   * 
   * @param o the <code>LineStringLocation</code> with which this
   *          <code>Coordinate</code> is being compared
   * @return a negative integer, zero, or a positive integer as this
   *         <code>LineStringLocation</code> is less than, equal to, or greater
   *         than the specified <code>LineStringLocation</code>
   */
  public int compareTo(
    final LineStringLocation other) {
    // compare segments
    if (segmentIndex < other.segmentIndex) {
      return -1;
    }
    if (segmentIndex > other.segmentIndex) {
      return 1;
    }
    // same segment, so compare segment fraction
    if (segmentFraction < other.segmentFraction) {
      return -1;
    }
    if (segmentFraction > other.segmentFraction) {
      return 1;
    }
    // same location
    return 0;
  }

  public Coordinate getCoordinate() {
    final Coordinate p0 = line.getCoordinateN(segmentIndex);
    final Coordinate p1 = line.getCoordinateN(segmentIndex + 1);
    return pointAlongSegmentByFraction(p0, p1, segmentFraction);
  }

  public LineString getLine() {
    return line;
  }

  public double getSegmentFraction() {
    return segmentFraction;
  }

  public int getSegmentIndex() {
    return segmentIndex;
  }

  public boolean isFirst() {
    return segmentIndex == 0 && segmentFraction == 0.0;
  }

  public boolean isLast() {
    return segmentIndex == line.getNumPoints() - 1 && segmentFraction == 1.0;
  }

  public boolean isVertex() {
    return segmentFraction <= 0.0 || segmentFraction >= 1.0;
  }

  /**
   * Ensures the values in this object are valid
   */
  private void normalize() {
    if (segmentFraction < 0.0) {
      segmentFraction = 0.0;
    }
    if (segmentFraction > 1.0) {
      segmentFraction = 1.0;
    }

    if (segmentIndex < 0) {
      segmentIndex = 0;
      segmentFraction = 0.0;
    } else if (segmentIndex >= line.getNumPoints()) {
      segmentIndex = line.getNumPoints() - 1;
      segmentFraction = 1.0;
    }
  }
}