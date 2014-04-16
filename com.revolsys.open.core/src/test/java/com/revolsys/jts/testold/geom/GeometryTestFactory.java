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
package com.revolsys.jts.testold.geom;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygon;

/**
 * @version 1.7
 */
public class GeometryTestFactory {

  public static Coordinates[] createBox(final double minx, final double miny,
    final int nSide, final double segLen) {
    int i;
    int ipt = 0;
    final Coordinates[] pts = new Coordinates[4 * nSide + 1];

    final double maxx = minx + nSide * segLen;
    final double maxy = miny + nSide * segLen;

    for (i = 0; i < nSide; i++) {
      final double x = minx + i * segLen;
      final double y = miny;
      pts[ipt++] = new Coordinate(x, y, Coordinates.NULL_ORDINATE);
    }
    for (i = 0; i < nSide; i++) {
      final double x = maxx;
      final double y = miny + i * segLen;
      pts[ipt++] = new Coordinate(x, y, Coordinates.NULL_ORDINATE);
    }
    for (i = 0; i < nSide; i++) {
      final double x = maxx - i * segLen;
      final double y = maxy;
      pts[ipt++] = new Coordinate(x, y, Coordinates.NULL_ORDINATE);
    }
    for (i = 0; i < nSide; i++) {
      final double x = minx;
      final double y = maxy - i * segLen;
      pts[ipt++] = new Coordinate(x, y, Coordinates.NULL_ORDINATE);
    }
    pts[ipt++] = new Coordinate(pts[0]);

    return pts;
  }

  public static Polygon createBox(final GeometryFactory fact,
    final double minx, final double miny, final int nSide, final double segLen) {
    final Coordinates[] pts = createBox(minx, minx, nSide, segLen);
    final LinearRing ring = fact.linearRing(pts);
    final Polygon poly = fact.createPolygon(ring, null);
    return poly;
  }

  /**
   * Creates a circle
   * @param basex the centre x coord
   * @param basey the centre y coord
   * @param size the size of the envelope of the star
   * @param nPts the number of points in the star
   */
  public static Coordinates[] createCircle(final double basex,
    final double basey, final double size, final int nPts) {
    final Coordinates[] pts = new Coordinates[nPts + 1];

    int iPt = 0;
    final double len = size / 2.0;

    for (int i = 0; i < nPts; i++) {
      final double ang = i * (2 * Math.PI / nPts);
      final double x = len * Math.cos(ang) + basex;
      final double y = len * Math.sin(ang) + basey;
      final Coordinates pt = new Coordinate(x, y, Coordinates.NULL_ORDINATE);
      pts[iPt++] = pt;
    }
    pts[iPt] = pts[0];
    return pts;
  }

  public static Polygon createCircle(final GeometryFactory fact,
    final double basex, final double basey, final double size, final int nPts) {
    final Coordinates[] pts = createCircle(basex, basey, size, nPts);
    final LinearRing ring = fact.linearRing(pts);
    final Polygon poly = fact.createPolygon(ring, null);
    return poly;
  }

  /**
   * Creates a star from a "circular" sine wave
   * @param basex the centre x coord
   * @param basey the centre y coord
   * @param size the size of the envelope of the star
   * @param armLen the length of an arm of the star
   * @param nArms the number of arms of the star
   * @param nPts the number of points in the star
   */
  public static Coordinates[] createSineStar(final double basex,
    final double basey, final double size, final double armLen,
    final int nArms, final int nPts) {
    double armBaseLen = size / 2 - armLen;
    if (armBaseLen < 0) {
      armBaseLen = 0.5;
    }

    final double angInc = 2 * Math.PI / nArms;
    int nArmPt = nPts / nArms;
    if (nArmPt < 5) {
      nArmPt = 5;
    }

    final int nPts2 = nArmPt * nArms;
    final Coordinates[] pts = new Coordinates[nPts2 + 1];

    int iPt = 0;
    double starAng = 0.0;

    for (int iArm = 0; iArm < nArms; iArm++) {
      for (int iArmPt = 0; iArmPt < nArmPt; iArmPt++) {
        final double ang = iArmPt * (2 * Math.PI / nArmPt);
        final double len = armLen * (1 - Math.cos(ang) / 2) + armBaseLen;
        final double x = len * Math.cos(starAng + iArmPt * angInc / nArmPt)
          + basex;
        final double y = len * Math.sin(starAng + iArmPt * angInc / nArmPt)
          + basey;
        final Coordinates pt = new Coordinate(x, y, Coordinates.NULL_ORDINATE);
        pts[iPt++] = pt;
      }
      starAng += angInc;
    }
    pts[iPt] = pts[0];
    return pts;
  }

  public static Polygon createSineStar(final GeometryFactory fact,
    final double basex, final double basey, final double size,
    final double armLen, final int nArms, final int nPts) {
    final Coordinates[] pts = createSineStar(basex, basey, size, armLen, nArms,
      nPts);
    final LinearRing ring = fact.linearRing(pts);
    final Polygon poly = fact.createPolygon(ring, null);
    return poly;
  }

}