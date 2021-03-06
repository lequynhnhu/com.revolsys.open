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

import com.revolsys.jts.algorithm.BoundaryNodeRule;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.operation.IsSimpleOp;

/**
 * Tests {@link IsSimpleOp} with different {@link BoundaryNodeRule}s.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class IsSimpleTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(IsSimpleTest.class);
  }

  private static final double TOLERANCE = 0.00005;

  private final GeometryFactory fact = GeometryFactory.floating3();

  private final WKTReader rdr = new WKTReader(this.fact);

  public IsSimpleTest(final String name) {
    super(name);
  }

  private void runIsSimpleTest(final String wkt, final boolean expectedResult)
      throws ParseException {
    runIsSimpleTest(wkt, expectedResult, null);
  }

  private void runIsSimpleTest(final String wkt, final boolean expectedResult,
    final Point expectedLocation) throws ParseException {
    final Geometry g = this.rdr.read(wkt);
    final IsSimpleOp op = new IsSimpleOp(g);
    boolean isSimple = false;
    isSimple = op.isSimple();
    final Point nonSimpleLoc = op.getNonSimpleLocation();

    // if geom is not simple, should have a valid location
    assertTrue(isSimple || nonSimpleLoc != null);

    assertTrue(expectedResult == isSimple);

    if (!isSimple && expectedLocation != null) {
      assertTrue(expectedLocation.distance(nonSimpleLoc) < TOLERANCE);
    }
  }

  /**
   * 2 LineStrings touching at an endpoint
   * @throws Exception
   */
  public void test2TouchAtEndpoint() throws Exception {
    final String a = "MULTILINESTRING((0 1, 1 1, 2 1), (0 0, 1 0, 2 1))";
    runIsSimpleTest(a, true, new PointDouble((double)2, 1, Point.NULL_ORDINATE));
  }

  /**
   * 3 LineStrings touching at an endpoint.
   *
   * @throws Exception
   */
  public void test3TouchAtEndpoint() throws Exception {
    final String a = "MULTILINESTRING ((0 1, 1 1, 2 1),   (0 0, 1 0, 2 1),  (0 2, 1 2, 2 1))";

    // rings are simple under all rules
    runIsSimpleTest(a, true, new PointDouble((double)2, 1, Point.NULL_ORDINATE));
  }

  public void testCross() throws Exception {
    final String a = "MULTILINESTRING ((20 120, 120 20), (20 20, 120 120))";
    runIsSimpleTest(a, false, new PointDouble((double)70, 70,
      Point.NULL_ORDINATE));
  }

  public void testMultiLineStringWithRingTouchAtEndpoint() throws Exception {
    final String a = "MULTILINESTRING ((100 100, 20 20, 200 20, 100 100), (100 200, 100 100))";

    // under Mod-2, the ring has no boundary, so the line intersects the
    // interior ==> not simple
    runIsSimpleTest(a, false, new PointDouble((double)100, 100,
      Point.NULL_ORDINATE));
  }

  public void testRing() throws Exception {
    final String a = "LINESTRING (100 100, 20 20, 200 20, 100 100)";

    // rings are simple under all rules
    runIsSimpleTest(a, true);
  }

}
