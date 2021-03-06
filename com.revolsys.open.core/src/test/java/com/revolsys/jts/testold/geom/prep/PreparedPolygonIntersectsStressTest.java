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
package com.revolsys.jts.testold.geom.prep;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.geom.prep.PreparedPolygon;
import com.revolsys.jts.geom.util.SineStarFactory;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.util.GeometricShapeFactory;

/**
 * Stress tests {@link PreparedPolygon#intersects(Geometry)}
 * to confirm it finds intersections correctly.
 *
 * @author Martin Davis
 *
 */
public class PreparedPolygonIntersectsStressTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(PreparedPolygonIntersectsStressTest.class);
  }

  static final int MAX_ITER = 10000;

  private static final GeometryFactory fact = GeometryFactory.floating(0, 2);

  private static WKTReader wktRdr = new WKTReader(fact);

  boolean testFailed = false;

  public PreparedPolygonIntersectsStressTest(final String name) {
    super(name);
  }

  Geometry createCircle(final Point origin, final double size, final int nPts) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    final Geometry circle = gsf.createCircle();
    // Polygon gRect = gsf.createRectangle();
    // Geometry g = gRect.getExteriorRing();
    return circle;
  }

  Geometry createSineStar(final Point origin, final double size, final int nPts) {
    final SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    gsf.setArmLengthRatio(0.1);
    gsf.setNumArms(20);
    final Geometry poly = gsf.createSineStar();
    return poly;
  }

  LineString createTestLine(final BoundingBox env, final double size,
    final int nPts) {
    final double width = env.getWidth();
    final double xOffset = width * Math.random();
    final double yOffset = env.getHeight() * Math.random();
    final Point basePt = new PointDouble(env.getMinX() + xOffset, env.getMinY()
      + yOffset, Point.NULL_ORDINATE);
    final LineString line = createTestLine(basePt, size, nPts);
    return line;
  }

  LineString createTestLine(final Point base, final double size, final int nPts) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(base);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    final Geometry circle = gsf.createCircle();
    // System.out.println(circle);
    return (LineString)circle.getBoundary();
  }

  public void run(final int nPts) {
    // Geometry poly = createCircle(new PointDouble((double)0, 0), 100, nPts);
    final Geometry poly = createSineStar(new PointDouble((double)0, 0,
      Point.NULL_ORDINATE), 100, nPts);
    // System.out.println(poly);
    //
    // System.out.println();
    // System.out.println("Running with " + nPts + " points");
    test(poly);
  }

  public void test() {
    run(1000);
  }

  public void test(final Geometry g) {
    int count = 0;
    while (count < MAX_ITER) {
      count++;
      final LineString line = createTestLine(g.getBoundingBox(), 10, 20);

      // System.out.println("Test # " + count);
      // System.out.println(line);
      testResultsEqual(g, line);
    }
  }

  public void testResultsEqual(final Geometry g, final LineString line) {
    final boolean slowIntersects = g.intersects(line);

    final Geometry prepGeom = g.prepare();

    final boolean fastIntersects = prepGeom.intersects(line);

    if (slowIntersects != fastIntersects) {
      // System.out.println(line);
      // System.out.println("Slow = " + slowIntersects + ", Fast = "
      // + fastIntersects);
      throw new RuntimeException("Different results found for intersects() !");
    }
  }
}
