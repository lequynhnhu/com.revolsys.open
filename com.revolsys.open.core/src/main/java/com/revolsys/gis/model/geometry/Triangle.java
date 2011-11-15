package com.revolsys.gis.model.geometry;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;

public class Triangle extends DoubleCoordinatesList {
  private static final long serialVersionUID = -4513931832875328029L;

  public static Triangle createClockwiseTriangle(final Coordinates c0,
    final Coordinates c1, final Coordinates c2) {
    try {
      if (CoordinatesUtil.orientationIndex(c0, c1, c2) == CGAlgorithms.CLOCKWISE) {
        return new Triangle(c0, c1, c2);
      } else {
        return new Triangle(c0, c2, c1);
      }
    } catch (final IllegalStateException e) {
      throw e;
    }

  }

  public static Coordinates circumCentre(final CoordinatesList points) {
    final double x1 = points.getX(0);
    final double y1 = points.getY(0);
    final double x2 = points.getX(1);
    final double y2 = points.getY(1);
    final double x3 = points.getX(2);
    final double y3 = points.getY(2);
    return CoordinatesUtil.circumcentre(x1, y1, x2, y2, x3, y3);
  }

  private Circle circumcircle;

  private Envelope envelope = new Envelope();

  private GeometryFactory geometryFactory = new GeometryFactory(0, 1);

  public Triangle() {
    super(3, 2);
  }

  public Triangle(final Coordinates... points) {
    super((byte)3, points);
    init();
  }

  public Triangle(final CoordinatesList points) {
    super(points);
    init();
  }

  public Triangle(final int numAxis) {
    super(3, numAxis);
  }

  public Triangle(final int numAxis, final CoordinatesList points) {
    super(points, numAxis);
    init();
  }

  public Triangle(final int numAxis, final double... coordinates) {
    super(numAxis, coordinates);
    init();
  }

  public Triangle(final int numAxis, final List<? extends Number> coordinates) {
    super(numAxis, coordinates);
    init();
  }

  private void addIntersection(final Set<Coordinates> coordinates,
    final Coordinates line1Start, final Coordinates line1End,
    final Coordinates line2Start, final Coordinates line2End) {
    final List<Coordinates> intersections = LineSegmentUtil.intersection(
      geometryFactory, line1Start, line1End, line2Start, line2End);
    coordinates.addAll(intersections);
  }

  /**
   * Returns true if the coordinate lies inside or on the edge of the Triangle.
   * 
   * @param coordinate The coordinate.
   * @return True if the coordinate lies inside or on the edge of the Triangle.
   */
  public boolean contains(final Coordinates coordinate) {
    final int triangleOrientation = CoordinatesUtil.orientationIndex(getP0(),
      getP1(), getP2());
    final int p0p1Orientation = CoordinatesUtil.orientationIndex(getP0(),
      getP1(), coordinate);
    if (p0p1Orientation != triangleOrientation
      && p0p1Orientation != CGAlgorithms.COLLINEAR) {
      return false;
    }
    final int p1p2Orientation = CoordinatesUtil.orientationIndex(getP1(),
      getP2(), coordinate);
    if (p1p2Orientation != triangleOrientation
      && p1p2Orientation != CGAlgorithms.COLLINEAR) {
      return false;
    }
    final int p2p0Orientation = CoordinatesUtil.orientationIndex(getP2(),
      getP0(), coordinate);
    if (p2p0Orientation != triangleOrientation
      && p2p0Orientation != CGAlgorithms.COLLINEAR) {
      return false;
    }
    return true;
  }

  private void createCircumcircle() {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    final double x3 = getX(2);
    final double y3 = getY(2);
    final double angleB = MathUtil.angle(x1, y1, x2, y2, x3, y3);

    final double radius = getP0().distance(getP2()) / Math.sin(angleB) * 0.5;
    final Coordinates coordinate = getCircumcentre();
    circumcircle = new Circle(coordinate, radius);
  }

  public boolean equals(final Triangle triangle) {
    final HashSet<Coordinates> coords = new HashSet<Coordinates>();
    coords.add(triangle.getP0());
    coords.add(triangle.getP1());
    coords.add(triangle.getP2());
    coords.add(getP0());
    coords.add(getP1());
    coords.add(getP2());
    return coords.size() == 3;
  }

  /**
   * Computes the circumcentre of a triangle. The circumcentre is the centre of
   * the circumcircle, the smallest circle which encloses the triangle.
   * 
   * @return The circumcentre of the triangle.
   */
  public Coordinates getCircumcentre() {
    return circumCentre(this);
  }

  /**
   * Computes the circumcircle of a triangle. The circumcircle is the smallest
   * circle which encloses the triangle.
   * 
   * @return The circumcircle of the triangle.
   */
  public Circle getCircumcircle() {
    return circumcircle;
  }

  /**
   * Get the envelope of the Triangle.
   * 
   * @return The envelope.
   */
  public Envelope getEnvelopeInternal() {
    return envelope;
  }

  public Coordinates getP0() {
    return get(0);
  }

  public Coordinates getP1() {
    return get(1);
  }

  public Coordinates getP2() {
    return get(2);
  }

  public Polygon getPolygon() {
    final LinearRing shell = geometryFactory.createLinearRing(new DoubleCoordinatesList(
      getNumAxis(), getP0(), getP1(), getP2(), getP0()));
    return geometryFactory.createPolygon(shell, null);
  }

  public void init() {
    if (size() > 3) {
      throw new IllegalArgumentException(
        "A traingle must have exeactly 3 points not " + size());
    }
    createCircumcircle();
    for (int i = 0; i < 3; i++) {
      double x = getX(i);
      double y = getY(i);
      envelope.expandToInclude(x, y);
    }
  }

  public LineSegment intersection(final LineSegment line) {
    final Coordinates lc0 = line.get(0);
    final Coordinates lc1 = line.get(1);
    final boolean lc0Contains = contains(lc0);
    final boolean lc1Contains = contains(lc1);
    if (lc0Contains && lc1Contains) {
      return line;
    } else {
      final Set<Coordinates> coordinates = new HashSet<Coordinates>();
      addIntersection(coordinates, lc0, lc1, getP0(), getP1());
      addIntersection(coordinates, lc0, lc1, getP1(), getP2());
      addIntersection(coordinates, lc0, lc1, getP2(), getP0());

      final Iterator<Coordinates> coordIterator = coordinates.iterator();
      if (coordIterator.hasNext()) {
        final Coordinates c1 = coordIterator.next();
        if (coordIterator.hasNext()) {
          final Coordinates c2 = coordIterator.next();
          if (coordIterator.hasNext()) {
            // TODO Too many intersect
          }
          return new LineSegment(c1, c2);
        } else {
          return new LineSegment(c1, c1);
        }
      } else {
        return null;
      }
    }
  }

  @Override
  public String toString() {
    return new WKTWriter(3).write(getPolygon());
  }

  public boolean intersectsCircumCircle(Coordinates point) {
    return circumcircle.contains(point);
  }
}