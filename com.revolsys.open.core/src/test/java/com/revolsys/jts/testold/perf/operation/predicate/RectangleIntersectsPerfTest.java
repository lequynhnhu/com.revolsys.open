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
package com.revolsys.jts.testold.perf.operation.predicate;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.geom.util.SineStarFactory;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.io.WKTWriter;
import com.revolsys.jts.precision.GeometryPrecisionReducer;
import com.revolsys.jts.util.GeometricShapeFactory;
import com.revolsys.jts.util.Stopwatch;

public class RectangleIntersectsPerfTest {
  static final int MAX_ITER = 10;

  static final int NUM_AOI_PTS = 2000;

  static final int NUM_LINES = 5000;

  static final int NUM_LINE_PTS = 1000;

  static PrecisionModel pm = new PrecisionModel();

  static GeometryFactory fact = new GeometryFactory(pm, 0);

  static WKTReader wktRdr = new WKTReader(fact);

  static WKTWriter wktWriter = new WKTWriter();

  public static void main(final String[] args) {
    final RectangleIntersectsPerfTest test = new RectangleIntersectsPerfTest();
    test.test();
  }

  Stopwatch sw = new Stopwatch();

  boolean testFailed = false;

  public RectangleIntersectsPerfTest() {
  }

  Geometry createRectangle(final Coordinates origin, final double size) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(4);
    final Geometry g = gsf.createRectangle();
    // Polygon gRect = gsf.createRectangle();
    // Geometry g = gRect.getExteriorRing();
    return g;
  }

  /**
   * Creates a set of rectangular Polygons which 
   * cover the given envelope.
   * The rectangles   
   * At least nRect rectangles are created.
   * 
   * @param env
   * @param nRect
   * @param rectSize
   * @return
   */
  Geometry[] createRectangles(final Envelope env, final int nRect,
    final double rectSize) {
    final int nSide = 1 + (int)Math.sqrt(nRect);
    final double dx = env.getWidth() / nSide;
    final double dy = env.getHeight() / nSide;

    final List rectList = new ArrayList();
    for (int i = 0; i < nSide; i++) {
      for (int j = 0; j < nSide; j++) {
        final double baseX = env.getMinX() + i * dx;
        final double baseY = env.getMinY() + j * dy;
        final Envelope envRect = new Envelope(baseX, baseX + dx, baseY, baseY
          + dy);
        final Geometry rect = fact.toGeometry(envRect);
        rectList.add(rect);
      }
    }
    return GeometryFactory.toGeometryArray(rectList);
  }

  Geometry createSineStar(final Coordinates origin, final double size,
    final int nPts) {
    final SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    gsf.setArmLengthRatio(2);
    gsf.setNumArms(20);
    final Geometry poly = gsf.createSineStar();
    return poly;
  }

  public void test() {
    // test(5);
    // test(10);
    test(500);
    // test(1000);
    // test(2000);
    test(100000);
    /*
     * test(100); test(1000); test(2000); test(4000); test(8000);
     */
  }

  void test(final Geometry[] rect, final Geometry g) {
    System.out.println("Target # pts: " + g.getVertexCount()
      + "  -- # Rectangles: " + rect.length);

    final int maxCount = MAX_ITER;
    final Stopwatch sw = new Stopwatch();
    final int count = 0;
    for (int i = 0; i < MAX_ITER; i++) {
      for (final Geometry element : rect) {
        // rect[j].relate(g);
        element.intersects(g);
      }
    }
    System.out.println("Finished in " + sw.getTimeString());
    System.out.println();
  }

  void test(final int nPts) {
    final double size = 100;
    final Coordinates origin = new Coordinate((double)0, 0, Coordinates.NULL_ORDINATE);
    final Geometry sinePoly = createSineStar(origin, size, nPts).getBoundary();
    /**
     * Make the geometry "crinkly" by rounding off the points.
     * This defeats the  MonotoneChain optimization in the full relate
     * algorithm, and provides a more realistic test.
     */
    final Geometry sinePolyCrinkly = GeometryPrecisionReducer.reduce(sinePoly,
      new PrecisionModel(size / 10));
    final Geometry target = sinePolyCrinkly;

    final Geometry rect = createRectangle(origin, 5);
    // System.out.println(target);
    // System.out.println("Running with " + nPts + " points");
    testRectangles(target, 100, 5);
  }

  void testRectangles(final Geometry target, final int nRect,
    final double rectSize) {
    final Geometry[] rects = createRectangles(target.getEnvelopeInternal(),
      nRect, rectSize);
    test(rects, target);
  }

}