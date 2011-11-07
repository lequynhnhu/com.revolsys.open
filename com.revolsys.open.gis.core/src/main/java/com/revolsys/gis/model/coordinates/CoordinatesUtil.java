package com.revolsys.gis.model.coordinates;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.algorithm.HCoordinate;
import com.vividsolutions.jts.algorithm.NotRepresentableException;
import com.vividsolutions.jts.algorithm.RobustDeterminant;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class CoordinatesUtil {

  public static double angle(final Coordinates p1, final Coordinates p2,
    final Coordinates p3) {
    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double x2 = p2.getX();
    final double y2 = p2.getY();
    final double x3 = p3.getX();
    final double y3 = p3.getY();
    return MathUtil.angle(x1, y1, x2, y2, x3, y3);
  }

  public static Coordinates offset(
    final Coordinates coordinate,
    final double angle,
    final double distance) {
    final double newX = coordinate.getX() + distance * Math.cos(angle);
    final double newY = coordinate.getY() + distance * Math.sin(angle);
    final Coordinates newCoordinate = new DoubleCoordinates(newX, newY);
    return newCoordinate;

  }
  public static double getElevation(
    final Coordinates coordinate,
    final Coordinates c0,
    final Coordinates c1) {
    final double fraction = coordinate.distance(c0) / c0.distance(c1);
    final double z = c0.getZ() + (c1.getZ() - c0.getZ()) * (fraction);
    return z;
  }
  
  public static int orientationIndex(Coordinates p1, Coordinates p2,
    Coordinates q) {
    // travelling along p1->p2, turn counter clockwise to get to q return 1,
    // travelling along p1->p2, turn clockwise to get to q return -1,
    // p1, p2 and q are colinear return 0.
    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double x2 = p2.getX();
    final double y2 = p2.getY();
    final double qX = q.getX();
    final double qY = q.getY();
    double dx1 = x2 - x1;
    double dy1 = y2 - y1;
    double dx2 = qX - x2;
    double dy2 = qY - y2;
    return RobustDeterminant.signOfDet2x2(dx1, dy1, dx2, dy2);
  }

  public static Coordinates circumcentre(double x1, double y1, double x2,
    double y2, double x3, double y3) {
    // compute the perpendicular bisector of chord ab
    HCoordinate cab = perpendicularBisector(x1, y1, x2, y2);
    // compute the perpendicular bisector of chord bc
    HCoordinate cbc = perpendicularBisector(x2, y2, x3, y3);
    // compute the intersection of the bisectors (circle radii)
    HCoordinate hcc = new HCoordinate(cab, cbc);
    Coordinates cc = null;
    try {
      cc = new DoubleCoordinates(hcc.getX(), hcc.getY());
    } catch (NotRepresentableException ex) {
      // MD - not sure what we can do to prevent this (robustness problem)
      // Idea - can we condition which edges we choose?
      throw new IllegalStateException(ex.getMessage());
    }
    return cc;
  }

  public static HCoordinate perpendicularBisector(double x1, double y1,
    double x2, double y2) {
    double dx = x2 - x1;
    double dy = y2 - y1;
    HCoordinate l1 = new HCoordinate(x1 + dx / 2.0, y1 + dy / 2.0, 1.0);
    HCoordinate l2 = new HCoordinate(x1 - dy + dx / 2.0, y1 + dx + dy / 2.0,
      1.0);
    return new HCoordinate(l1, l2);
  }

  public static double distance(final Coordinates point1,
    final Coordinates point2) {
    final double x1 = point1.getX();
    final double y1 = point1.getY();
    final double x2 = point2.getX();
    final double y2 = point2.getY();
    return MathUtil.distance(x1, y1, x2, y2);
  }

  public static Coordinates get(final Coordinate coordinate) {
    if (Double.isNaN(coordinate.z)) {
      return new DoubleCoordinates(coordinate.z, coordinate.y);
    } else {
      return new DoubleCoordinates(coordinate.z, coordinate.y, coordinate.z);
    }
  }

  public static Coordinates get(final Geometry geometry) {
    if (geometry.isEmpty()) {
      return null;
    } else {
      final CoordinatesList points = CoordinatesListUtil.get(geometry);
      return points.get(0);
    }
  }

  public static boolean isAcute(final Coordinates point1,
    final Coordinates point2, final Coordinates point3) {
    final double x1 = point1.getX();
    final double y1 = point1.getY();
    final double x2 = point2.getX();
    final double y2 = point2.getY();
    final double x3 = point3.getX();
    final double y3 = point3.getY();

    return MathUtil.isAcute(x1, y1, x2, y2, x3, y3);
  }
}