package com.revolsys.jts.testold.perf.operation.union;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.util.GeometricShapeFactory;

public class PolygonUnionPerfTest {

  public static void main(final String[] args) {
    final PolygonUnionPerfTest test = new PolygonUnionPerfTest();

    // test.test();
    test.testRampItems();

  }

  static final int MAX_ITER = 1;

  private static final GeometryFactory geometryFactory = GeometryFactory.floating(
    0, 2);

  static WKTReader wktRdr = new WKTReader(geometryFactory);

  GeometryFactory factory = GeometryFactory.floating3();

  boolean testFailed = false;

  public PolygonUnionPerfTest() {
  }

  Geometry createPoly(final Point base, final double size, final int nPts) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory(this.factory);
    gsf.setCentre(base);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);

    final Geometry poly = gsf.createCircle();
    // Geometry poly = gsf.createRectangle();

    // System.out.println(circle);
    return poly;
  }

  /**
   * Creates a grid of circles with a small percentage of overlap
   * in both directions.
   * This approximated likely real-world cases well,
   * and seems to produce
   * close to worst-case performance for the Iterated algorithm.
   *
   * Sample times:
   * 1000 items/100 pts - Cascaded: 2718 ms, Iterated 150 s
   *
   * @param nItems
   * @param size
   * @param nPts
   * @return
   */
  List createPolys(final int nItems, final double size, final int nPts) {

    // between 0 and 1
    final double overlapPct = 0.2;

    final int nCells = (int)Math.sqrt(nItems);

    final List geoms = new ArrayList();
    // double width = env.getWidth();
    final double width = nCells * (1 - overlapPct) * size;

    // this results in many final polys
    final double height = nCells * 2 * size;

    // this results in a single final polygon
    // double height = width;

    final double xInc = width / nCells;
    final double yInc = height / nCells;
    for (int i = 0; i < nCells; i++) {
      for (int j = 0; j < nCells; j++) {
        final Point base = new PointDouble(i * xInc, j * yInc,
          Point.NULL_ORDINATE);
        final Geometry poly = createPoly(base, size, nPts);
        geoms.add(poly);
        // System.out.println(poly);
      }
    }
    return geoms;
  }

  public void test() {
    // test(5, 100, 10.0);
    test(1000, 100, 10.0);
  }

  public void test(final int nItems, final int nPts, final double size) {
    //  System.out.println("---------------------------------------------------------");
    //  System.out.println("# pts/item: " + nPts);

    final List polys = createPolys(nItems, size, nPts);

    // System.out.println();
    // System.out.println("Running with " + nPts + " points");

    final UnionPerfTester tester = new UnionPerfTester(polys);
    tester.runAll();
  }

  public void testRampItems() {
    final int nPts = 1000;

    test(5, nPts, 10.0);
    test(5, nPts, 10.0);
    test(25, nPts, 10.0);
    test(50, nPts, 10.0);
    test(100, nPts, 10.0);
    test(200, nPts, 10.0);
    test(400, nPts, 10.0);
    test(500, nPts, 10.0);
    test(1000, nPts, 10.0);
    test(2000, nPts, 10.0);
    test(4000, nPts, 10.0);
  }

}
