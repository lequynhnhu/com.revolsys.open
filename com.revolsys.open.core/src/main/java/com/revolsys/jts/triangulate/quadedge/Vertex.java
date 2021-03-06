/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.revolsys.jts.triangulate.quadedge;

import com.revolsys.jts.algorithm.HCoordinate;
import com.revolsys.jts.algorithm.NotRepresentableException;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;

/**
 * Models a site (node) in a {@link QuadEdgeSubdivision}.
 * The sites can be points on a line string representing a
 * linear site.
 * <p>
 * The vertex can be considered as a vector with a norm, length, inner product, cross
 * product, etc. Additionally, point relations (e.g., is a point to the left of a line, the circle
 * defined by this point and two others, etc.) are also defined in this class.
 * <p>
 * It is common to want to attach user-defined data to
 * the vertices of a subdivision.
 * One way to do this is to subclass <tt>Vertex</tt>
 * to carry any desired information.
 *
 * @author David Skea
 * @author Martin Davis
 */
public class Vertex {
  /**
   * Computes the interpolated Z-value for a point p lying on the segment p0-p1
   *
   * @param p
   * @param p0
   * @param p1
   * @return the interpolated Z value
   */
  public static double interpolateZ(final Point p, final Point p0,
    final Point p1) {
    final double segLen = p0.distance(p1);
    final double ptLen = p.distance(p0);
    final double dz = p1.getZ() - p0.getZ();
    final double pz = p0.getZ() + dz * (ptLen / segLen);
    return pz;
  }

  /**
   * Interpolates the Z-value (height) of a point enclosed in a triangle
   * whose vertices all have Z values.
   * The containing triangle must not be degenerate
   * (in other words, the three vertices must enclose a
   * non-zero area).
   *
   * @param p the point to interpolate the Z value of
   * @param v0 a vertex of a triangle containing the p
   * @param v1 a vertex of a triangle containing the p
   * @param v2 a vertex of a triangle containing the p
   * @return the interpolated Z-value (height) of the point
   */
  public static double interpolateZ(final Point p, final Point v0,
    final Point v1, final Point v2) {
    final double x0 = v0.getX();
    final double y0 = v0.getY();
    final double a = v1.getX() - x0;
    final double b = v2.getX() - x0;
    final double c = v1.getY() - y0;
    final double d = v2.getY() - y0;
    final double det = a * d - b * c;
    final double dx = p.getX() - x0;
    final double dy = p.getY() - y0;
    final double t = (d * dx - b * dy) / det;
    final double u = (-c * dx + a * dy) / det;
    final double z = v0.getZ() + t * (v1.getZ() - v0.getZ()) + u
        * (v2.getZ() - v0.getZ());
    return z;
  }

  public static final int LEFT = 0;

  public static final int RIGHT = 1;

  public static final int BEYOND = 2;

  public static final int BEHIND = 3;

  public static final int BETWEEN = 4;

  public static final int ORIGIN = 5;

  public static final int DESTINATION = 6;

  private final Point p;

  // private int edgeNumber = -1;

  public Vertex(final double _x, final double _y) {
    this.p = new PointDouble(_x, _y, Point.NULL_ORDINATE);
  }

  public Vertex(final double _x, final double _y, final double _z) {
    this.p = new PointDouble(_x, _y, _z);
  }

  public Vertex(final Point _p) {
    this.p = new PointDouble(_p);
  }

  private HCoordinate bisector(final Vertex a, final Vertex b) {
    // returns the perpendicular bisector of the line segment ab
    final double dx = b.getX() - a.getX();
    final double dy = b.getY() - a.getY();
    final HCoordinate l1 = new HCoordinate(a.getX() + dx / 2.0, a.getY() + dy
      / 2.0, 1.0);
    final HCoordinate l2 = new HCoordinate(a.getX() - dy + dx / 2.0, a.getY()
      + dx + dy / 2.0, 1.0);
    return new HCoordinate(l1, l2);
  }

  /**
   * Computes the centre of the circumcircle of this vertex and two others.
   *
   * @param b
   * @param c
   * @return the Point which is the circumcircle of the 3 points.
   */
  public Vertex circleCenter(final Vertex b, final Vertex c) {
    final Vertex a = new Vertex(this.getX(), this.getY());
    // compute the perpendicular bisector of cord ab
    final HCoordinate cab = bisector(a, b);
    // compute the perpendicular bisector of cord bc
    final HCoordinate cbc = bisector(b, c);
    // compute the intersection of the bisectors (circle radii)
    final HCoordinate hcc = new HCoordinate(cab, cbc);
    Vertex cc = null;
    try {
      cc = new Vertex(hcc.getX(), hcc.getY());
    } catch (final NotRepresentableException nre) {
      System.err.println("a: " + a + "  b: " + b + "  c: " + c);
      System.err.println(nre);
    }
    return cc;
  }

  /**
   * Computes the value of the ratio of the circumradius to shortest edge. If smaller than some
   * given tolerance B, the associated triangle is considered skinny. For an equal lateral
   * triangle this value is 0.57735. The ratio is related to the minimum triangle angle theta by:
   * circumRadius/shortestEdge = 1/(2sin(theta)).
   *
   * @param b second vertex of the triangle
   * @param c third vertex of the triangle
   * @return ratio of circumradius to shortest edge.
   */
  public double circumRadiusRatio(final Vertex b, final Vertex c) {
    final Vertex x = this.circleCenter(b, c);
    final double radius = distance(x, b);
    double edgeLength = distance(this, b);
    double el = distance(b, c);
    if (el < edgeLength) {
      edgeLength = el;
    }
    el = distance(c, this);
    if (el < edgeLength) {
      edgeLength = el;
    }
    return radius / edgeLength;
  }

  public int classify(final Vertex p0, final Vertex p1) {
    final Vertex p2 = this;
    final Vertex a = p1.sub(p0);
    final Vertex b = p2.sub(p0);
    final double sa = a.crossProduct(b);
    if (sa > 0.0) {
      return LEFT;
    }
    if (sa < 0.0) {
      return RIGHT;
    }
    if (a.getX() * b.getX() < 0.0 || a.getY() * b.getY() < 0.0) {
      return BEHIND;
    }
    if (a.magn() < b.magn()) {
      return BEYOND;
    }
    if (p0.equals(p2)) {
      return ORIGIN;
    }
    if (p1.equals(p2)) {
      return DESTINATION;
    }
    return BETWEEN;
  }

  /* returns k X v (cross product). this is a vector perpendicular to v */
  Vertex cross() {
    return new Vertex(this.p.getY(), -this.p.getX());
  }

  /**
   * Computes the cross product k = u X v.
   *
   * @param v a vertex
   * @return returns the magnitude of u X v
   */
  double crossProduct(final Vertex v) {
    return this.p.getX() * v.getY() - this.p.getY() * v.getX();
  }

  private double distance(final Vertex v1, final Vertex v2) {
    return Math.sqrt(Math.pow(v2.getX() - v1.getX(), 2.0)
      + Math.pow(v2.getY() - v1.getY(), 2.0));
  }

  /**
   * Computes the inner or dot product
   *
   * @param v a vertex
   * @return returns the dot product u.v
   */
  double dot(final Vertex v) {
    return this.p.getX() * v.getX() + this.p.getY() * v.getY();
  }

  public boolean equals(final Vertex _x) {
    if (this.p.getX() == _x.getX() && this.p.getY() == _x.getY()) {
      return true;
    } else {
      return false;
    }
  }

  public boolean equals(final Vertex _x, final double tolerance) {
    if (this.p.distance(_x.getCoordinate()) < tolerance) {
      return true;
    } else {
      return false;
    }
  }

  public Point getCoordinate() {
    return this.p;
  }

  public double getX() {
    return this.p.getX();
  }

  public double getY() {
    return this.p.getY();
  }

  public double getZ() {
    return this.p.getZ();
  }

  /** ************************************************************* */
  /***********************************************************************************************
   * Geometric primitives /
   **********************************************************************************************/

  /**
   * For this vertex enclosed in a triangle defined by three vertices v0, v1 and v2, interpolate
   * a z value from the surrounding vertices.
   */
  public double interpolateZValue(final Vertex v0, final Vertex v1,
    final Vertex v2) {
    final double x0 = v0.getX();
    final double y0 = v0.getY();
    final double a = v1.getX() - x0;
    final double b = v2.getX() - x0;
    final double c = v1.getY() - y0;
    final double d = v2.getY() - y0;
    final double det = a * d - b * c;
    final double dx = this.getX() - x0;
    final double dy = this.getY() - y0;
    final double t = (d * dx - b * dy) / det;
    final double u = (-c * dx + a * dy) / det;
    final double z = v0.getZ() + t * (v1.getZ() - v0.getZ()) + u
        * (v2.getZ() - v0.getZ());
    return z;
  }

  /**
   * Tests whether the triangle formed by this vertex and two
   * other vertices is in CCW orientation.
   *
   * @param b a vertex
   * @param c a vertex
   * @returns true if the triangle is oriented CCW
   */
  public final boolean isCCW(final Vertex b, final Vertex c) {
    /*
     * // test code used to check for robustness of triArea boolean isCCW =
     * (b.p.x - p.x) * (c.p.y - p.y) - (b.p.y - p.y) * (c.p.x - p.x) > 0;
     * //boolean isCCW = triArea(this, b, c) > 0; boolean isCCWRobust =
     * CGAlgorithms.orientationIndex(p, b.p, c.p) ==
     * CGAlgorithms.COUNTERCLOCKWISE; if (isCCWRobust != isCCW)
     * System.out.println("CCW failure"); //
     */

    // is equal to the signed area of the triangle

    return (b.p.getX() - this.p.getX()) * (c.p.getY() - this.p.getY())
        - (b.p.getY() - this.p.getY()) * (c.p.getX() - this.p.getX()) > 0;

        // original rolled code
        // boolean isCCW = triArea(this, b, c) > 0;
        // return isCCW;

  }

  /**
   * Tests if the vertex is inside the circle defined by
   * the triangle with vertices a, b, c (oriented counter-clockwise).
   *
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @return true if this vertex is in the circumcircle of (a,b,c)
   */
  public boolean isInCircle(final Vertex a, final Vertex b, final Vertex c) {
    return TrianglePredicate.isInCircleRobust(a.p, b.p, c.p, this.p);
    // non-robust - best to not use
    // return TrianglePredicate.isInCircle(a.p, b.p, c.p, this.p);
  }

  public final boolean leftOf(final QuadEdge e) {
    return isCCW(e.orig(), e.dest());
  }

  /* magnitude of vector */
  double magn() {
    return Math.sqrt(this.p.getX() * this.p.getX() + this.p.getY() * this.p.getY());
  }

  /**
   * returns a new vertex that is mid-way between this vertex and another end point.
   *
   * @param a the other end point.
   * @return the point mid-way between this and that.
   */
  public Vertex midPoint(final Vertex a) {
    final double xm = (this.p.getX() + a.getX()) / 2.0;
    final double ym = (this.p.getY() + a.getY()) / 2.0;
    final double zm = (this.p.getZ() + a.getZ()) / 2.0;
    return new Vertex(xm, ym, zm);
  }

  public final boolean rightOf(final QuadEdge e) {
    return isCCW(e.dest(), e.orig());
  }

  /* and subtraction */
  Vertex sub(final Vertex v) {
    return new Vertex(this.p.getX() - v.getX(), this.p.getY() - v.getY());
  }

  /* Vector addition */
  Vertex sum(final Vertex v) {
    return new Vertex(this.p.getX() + v.getX(), this.p.getY() + v.getY());
  }

  /**
   * Computes the scalar product c(v)
   *
   * @param v a vertex
   * @return returns the scaled vector
   */
  Vertex times(final double c) {
    return new Vertex(c * this.p.getX(), c * this.p.getY());
  }

  @Override
  public String toString() {
    return "POINT (" + this.p.getX() + " " + this.p.getY() + ")";
  }

}
