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
package com.revolsys.jts.testold.operation;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.operation.distance.DistanceOp;

/**
 * @version 1.7
 */
public class DistanceTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(DistanceTest.class);
  }

  private final GeometryFactory geometryFactory = GeometryFactory.fixed(0,
    1.0);

  WKTReader reader = new WKTReader(this.geometryFactory);

  public DistanceTest(final String name) {
    super(name);
  }

  private void doNearestPointsTest(final String wkt0, final String wkt1,
    final double distance, final Point p0, final Point p1)
        throws ParseException {
    final Geometry geometry1 = new WKTReader().read(wkt0);
    final Geometry geometry2 = new WKTReader().read(wkt1);
    final DistanceOp op = new DistanceOp(geometry1, geometry2);
    final double tolerance = 1E-10;
    final Point[] nearestPoints = op.nearestPoints();
    final Point nearestPoint1 = nearestPoints[0];
    final Point nearestPoint2 = nearestPoints[1];
    final double p1p2Distance = nearestPoint1.distance(nearestPoint2);
    assertEquals(distance, p1p2Distance, tolerance);
    assertEquals(p0.getX(), nearestPoint1.getX(), tolerance);
    assertEquals(p0.getY(), nearestPoint1.getY(), tolerance);
    assertEquals(p1.getX(), nearestPoint2.getX(), tolerance);
    assertEquals(p1.getY(), nearestPoint2.getY(), tolerance);
  }

  public void testClosestPoints1() throws Exception {
    final String wkt1 = "POLYGON ((200 180, 60 140, 60 260, 200 180))";
    final String wkt2 = "POINT (140 280)";
    final Point p1 = new PointDouble(111.6923076923077,
      230.46153846153845);
    final Point p2 = new PointDouble(140.0, 280);
    doNearestPointsTest(wkt1, wkt2, 57.05597791103589, p1, p2);
  }

  public void testClosestPoints2() throws Exception {
    doNearestPointsTest("POLYGON ((200 180, 60 140, 60 260, 200 180))",
      "MULTIPOINT ((140 280), (140 320))", 57.05597791103589,
      new PointDouble(111.6923076923077, 230.46153846153845,
        Point.NULL_ORDINATE), new PointDouble((double)140, 280,
          Point.NULL_ORDINATE));
  }

  public void testClosestPoints3() throws Exception {
    doNearestPointsTest(
      "LINESTRING (100 100, 200 100, 200 200, 100 200, 100 100)",
      "POINT (10 10)", 127.27922061357856, new PointDouble((double)100,
        100, Point.NULL_ORDINATE), new PointDouble((double)10, 10,
          Point.NULL_ORDINATE));
  }

  public void testClosestPoints4() throws Exception {
    doNearestPointsTest("LINESTRING (100 100, 200 200)",
      "LINESTRING (100 200, 200 100)", 0.0,
      new PointDouble(150.0, 150.0), new PointDouble(150.0, 150.0));
  }

  public void testClosestPoints5() throws Exception {
    doNearestPointsTest("LINESTRING (100 100, 200 200)",
      "LINESTRING (150 121, 200 0)", 20.506096654409877, new PointDouble(
        135.5, 135.5, Point.NULL_ORDINATE), new PointDouble((double)150,
          121, Point.NULL_ORDINATE));
  }

  public void testClosestPoints6() throws Exception {
    final PointDouble p1 = new PointDouble(139.4956500724988,
      206.78661188980183);
    final PointDouble p2 = new PointDouble(153.0, 204);
    final String wkt1 = "POLYGON ((76 185, 125 283, 331 276, 324 122, 177 70, 184 155, 69 123, 76 185), (267 237, 148 248, 135 185, 223 189, 251 151, 286 183, 267 237))";
    final String wkt2 = "LINESTRING (153 204, 185 224, 209 207, 238 222, 254 186)";
    doNearestPointsTest(wkt1, wkt2, 13.788860460124573, p1, p2);
  }

  public void testClosestPoints7() throws Exception {
    doNearestPointsTest(
      "POLYGON ((76 185, 125 283, 331 276, 324 122, 177 70, 184 155, 69 123, 76 185), (267 237, 148 248, 135 185, 223 189, 251 151, 286 183, 267 237))",
      "LINESTRING (120 215, 185 224, 209 207, 238 222, 254 186)", 0.0,
      new PointDouble((double)120, 215, Point.NULL_ORDINATE),
      new PointDouble((double)120, 215, Point.NULL_ORDINATE));
  }

  public void testDisjointCollinearSegments() throws Exception {
    final Geometry g1 = this.reader.read("LINESTRING (0.0 0.0, 9.9 1.4)");
    final Geometry g2 = this.reader.read("LINESTRING (11.88 1.68, 21.78 3.08)");
    assertEquals(2.23606, g1.distance(g2), 0.0001);
  }

  public void testEmpty() throws Exception {
    final Geometry g1 = this.reader.read("POINT (0 0)");
    final Geometry g2 = this.reader.read("POLYGON EMPTY");
    assertEquals(0.0, g1.distance(g2), 0.0);
  }

  public void testEverything() throws Exception {
    final Geometry g1 = this.reader.read("POLYGON ((40 320, 200 380, 320 80, 40 40, 40 320),  (180 280, 80 280, 100 100, 220 140, 180 280))");
    Geometry g2 = this.reader.read("POLYGON ((160 240, 120 240, 120 160, 160 140, 160 240))");
    assertEquals(18.97366596, g1.distance(g2), 1E-5);

    g2 = this.reader.read("POLYGON ((160 240, 120 240, 120 160, 180 100, 160 240))");
    assertEquals(0.0, g1.distance(g2), 1E-5);

    final LineString l1 = (LineString)this.reader.read("LINESTRING(10 10, 20 20, 30 40)");
    final LineString l2 = (LineString)this.reader.read("LINESTRING(10 10, 20 20, 30 40)");
    assertEquals(0.0, l1.distance(l2), 1E-5);
  }
}
