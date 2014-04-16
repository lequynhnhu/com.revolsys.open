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
package com.revolsys.jts.algorithm;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;

/**
 * Non-robust versions of various fundamental Computational Geometric algorithms,
 * <b>FOR TESTING PURPOSES ONLY!</b>.
 * The non-robustness is due to rounding error in floating point computation.
 *
 * @version 1.7
 */
public class NonRobustCGAlgorithms extends CGAlgorithms {
  /**
   * Computes the orientation of a point q to the directed line segment p1-p2.
   * The orientation of a point relative to a directed line segment indicates
   * which way you turn to get to q after travelling from p1 to p2.
   * 
   * @return 1 if q is counter-clockwise from p1-p2
   * @return -1 if q is clockwise from p1-p2
   * @return 0 if q is collinear with p1-p2
   */
  public static int computeOrientation(final Coordinates p1,
    final Coordinates p2, final Coordinates q) {
    return orientationIndex(p1, p2, q);
  }

  /**
   * Computes the distance from a line segment AB to a line segment CD
   * 
   * Note: NON-ROBUST!
   * 
   * @param A
   *          a point of one line
   * @param B
   *          the second point of (must be different to A)
   * @param C
   *          one point of the line
   * @param D
   *          another point of the line (must be different to A)
   */
  public static double distanceLineLine(final Coordinates A, final Coordinates B,
    final Coordinates C, final Coordinates D) {
    // check for zero-length segments
    if (A.equals(B)) {
      return distancePointLine(A, C, D);
    }
    if (C.equals(D)) {
      return distancePointLine(D, A, B);
    }

    // AB and CD are line segments
    /*
     * from comp.graphics.algo Solving the above for r and s yields
     * (Ay-Cy)(Dx-Cx)-(Ax-Cx)(Dy-Cy) r = ----------------------------- (eqn 1)
     * (Bx-Ax)(Dy-Cy)-(By-Ay)(Dx-Cx) (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay) s =
     * ----------------------------- (eqn 2) (Bx-Ax)(Dy-Cy)-(By-Ay)(Dx-Cx) Let P
     * be the position vector of the intersection point, then P=A+r(B-A) or
     * Px=Ax+r(Bx-Ax) Py=Ay+r(By-Ay) By examining the values of r & s, you can
     * also determine some other limiting conditions: If 0<=r<=1 & 0<=s<=1,
     * intersection exists r<0 or r>1 or s<0 or s>1 line segments do not
     * intersect If the denominator in eqn 1 is zero, AB & CD are parallel If
     * the numerator in eqn 1 is also zero, AB & CD are collinear.
     */
    final double r_top = (A.getY() - C.getY()) * (D.getX() - C.getX())
      - (A.getX() - C.getX()) * (D.getY() - C.getY());
    final double r_bot = (B.getX() - A.getX()) * (D.getY() - C.getY())
      - (B.getY() - A.getY()) * (D.getX() - C.getX());

    final double s_top = (A.getY() - C.getY()) * (B.getX() - A.getX())
      - (A.getX() - C.getX()) * (B.getY() - A.getY());
    final double s_bot = (B.getX() - A.getX()) * (D.getY() - C.getY())
      - (B.getY() - A.getY()) * (D.getX() - C.getX());

    if ((r_bot == 0) || (s_bot == 0)) {
      return Math.min(
        distancePointLine(A, C, D),
        Math.min(distancePointLine(B, C, D),
          Math.min(distancePointLine(C, A, B), distancePointLine(D, A, B))));

    }
    final double s = s_top / s_bot;
    final double r = r_top / r_bot;

    if ((r < 0) || (r > 1) || (s < 0) || (s > 1)) {
      // no intersection
      return Math.min(
        distancePointLine(A, C, D),
        Math.min(distancePointLine(B, C, D),
          Math.min(distancePointLine(C, A, B), distancePointLine(D, A, B))));
    }
    return 0.0; // intersection exists
  }

  /**
   * Computes whether a ring defined by an array of {@link Coordinates} is
   * oriented counter-clockwise.
   * <p>
   * This will handle coordinate lists which contain repeated points.
   *
   * @param ring an array of coordinates forming a ring
   * @return <code>true</code> if the ring is oriented counter-clockwise.
   * @throws IllegalArgumentException if the ring is degenerate (does not contain 3 different points)
   */
  public static boolean isCCW(final Coordinates[] ring) {
    // # of points without closing endpoint
    final int nPts = ring.length - 1;

    // check that this is a valid ring - if not, simply return a dummy value
    if (nPts < 4) {
      return false;
    }

    // algorithm to check if a Ring is stored in CCW order
    // find highest point
    Coordinates hip = ring[0];
    int hii = 0;
    for (int i = 1; i <= nPts; i++) {
      final Coordinates p = ring[i];
      if (p.getY() > hip.getY()) {
        hip = p;
        hii = i;
      }
    }

    // find different point before highest point
    int iPrev = hii;
    do {
      iPrev = (iPrev - 1) % nPts;
    } while (ring[iPrev].equals(hip) && iPrev != hii);

    // find different point after highest point
    int iNext = hii;
    do {
      iNext = (iNext + 1) % nPts;
    } while (ring[iNext].equals(hip) && iNext != hii);

    final Coordinates prev = ring[iPrev];
    final Coordinates next = ring[iNext];

    if (prev.equals(hip) || next.equals(hip) || prev.equals(next)) {
      throw new IllegalArgumentException(
        "degenerate ring (does not contain 3 different points)");
    }

    // translate so that hip is at the origin.
    // This will not affect the area calculation, and will avoid
    // finite-accuracy errors (i.e very small vectors with very large
    // coordinates)
    // This also simplifies the discriminant calculation.
    final double prev2x = prev.getX() - hip.getX();
    final double prev2y = prev.getY() - hip.getY();
    final double next2x = next.getX() - hip.getX();
    final double next2y = next.getY() - hip.getY();
    // compute cross-product of vectors hip->next and hip->prev
    // (e.g. area of parallelogram they enclose)
    final double disc = next2x * prev2y - next2y * prev2x;
    /*
     * If disc is exactly 0, lines are collinear. There are two possible cases:
     * (1) the lines lie along the x axis in opposite directions (2) the line
     * lie on top of one another (2) should never happen, so we're going to
     * ignore it! (Might want to assert this) (1) is handled by checking if next
     * is left of prev ==> CCW
     */
    if (disc == 0.0) {
      // poly is CCW if prev x is right of next x
      return (prev.getX() > next.getX());
    } else {
      // if area is positive, points are ordered CCW
      return (disc > 0.0);
    }
  }

  /**
   * Computes whether a ring defined by an array of {@link Coordinates} is
   * oriented counter-clockwise.
   * <p>
   * This will handle coordinate lists which contain repeated points.
   *
   * @param ring an array of coordinates forming a ring
   * @return <code>true</code> if the ring is oriented counter-clockwise.
   * @throws IllegalArgumentException if the ring is degenerate (does not contain 3 different points)
   */
  public static boolean isPointInRing(final Coordinates p,
    final Coordinates[] ring) {
    int i, i1; // point index; i1 = i-1 mod n
    double xInt; // x intersection of e with ray
    int crossings = 0; // number of edge/ray crossings
    double x1, y1, x2, y2;
    final int nPts = ring.length;

    /*
     * For each line edge l = (i-1, i), see if it crosses ray from test point in
     * positive x direction.
     */
    for (i = 1; i < nPts; i++) {
      i1 = i - 1;
      final Coordinates p1 = ring[i];
      final Coordinates p2 = ring[i1];
      x1 = p1.getX() - p.getX();
      y1 = p1.getY() - p.getY();
      x2 = p2.getX() - p.getX();
      y2 = p2.getY() - p.getY();

      if (((y1 > 0) && (y2 <= 0)) || ((y2 > 0) && (y1 <= 0))) {
        /* e straddles x axis, so compute intersection. */
        xInt = (x1 * y2 - x2 * y1) / (y2 - y1);
        // xsave = xInt;
        /* crosses ray if strictly positive intersection. */
        if (0.0 < xInt) {
          crossings++;
        }
      }
    }
    /* p is inside if an odd number of crossings. */
    if ((crossings % 2) == 1) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns the index of the direction of the point <code>q</code> relative to
   * a vector specified by <code>p1-p2</code>.
   * 
   * @param p1
   *          the origin point of the vector
   * @param p2
   *          the final point of the vector
   * @param q
   *          the point to compute the direction to
   * 
   * @return 1 if q is counter-clockwise (left) from p1-p2
   * @return -1 if q is clockwise (right) from p1-p2
   * @return 0 if q is collinear with p1-p2
   */
  public static int orientationIndex(final Coordinates p1,
    final Coordinates p2, final Coordinates q) {
    final double dx1 = p2.getX() - p1.getX();
    final double dy1 = p2.getY() - p1.getY();
    final double dx2 = q.getX() - p2.getX();
    final double dy2 = q.getY() - p2.getY();
    final double det = dx1 * dy2 - dx2 * dy1;
    if (det > 0.0) {
      return 1;
    }
    if (det < 0.0) {
      return -1;
    }
    return 0;
  }

  public NonRobustCGAlgorithms() {
  }

}