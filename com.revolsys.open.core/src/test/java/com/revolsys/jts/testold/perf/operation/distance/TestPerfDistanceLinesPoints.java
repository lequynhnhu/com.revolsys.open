package com.revolsys.jts.testold.perf.operation.distance;

import java.util.List;

import com.revolsys.jts.densify.Densifier;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.io.WKTFileReader;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.operation.distance.DistanceOp;
import com.revolsys.jts.operation.distance.IndexedFacetDistance;
import com.revolsys.jts.testold.algorithm.InteriorPointTest;
import com.revolsys.jts.util.Stopwatch;

/**
 * Tests performance of {@link IndexedFacetDistance} versus standard
 * {@link DistanceOp}
 * using a grid of points to a target set of lines
 *
 * @author Martin Davis
 *
 */
public class TestPerfDistanceLinesPoints {
  public static void main(final String[] args) {
    final TestPerfDistanceLinesPoints test = new TestPerfDistanceLinesPoints();
    try {
      test.test();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  static final boolean USE_INDEXED_DIST = true;

  static GeometryFactory geomFact = GeometryFactory.floating3();

  static final int MAX_ITER = 1;

  static final int NUM_TARGET_ITEMS = 4000;

  static final double EXTENT = 1000;

  static final int NUM_PTS_SIDE = 100;

  boolean verbose = true;

  public TestPerfDistanceLinesPoints() {
  }

  void computeDistance(final Geometry[] pts, final Geometry geom) {
    IndexedFacetDistance bbd = null;
    if (USE_INDEXED_DIST) {
      bbd = new IndexedFacetDistance(geom);
    }
    for (final Geometry pt : pts) {
      if (USE_INDEXED_DIST) {
        final double dist = bbd.getDistance(pt);
        // double dist = bbd.getDistanceWithin(pts[i].getCoordinate(), 100000);
      } else {
        final double dist = geom.distance(pt);
      }
    }
  }

  Geometry createDiagonalCircles(final double extent, final int nSegs) {
    final Polygon[] circles = new Polygon[nSegs];
    final double inc = extent / nSegs;
    for (int i = 0; i < nSegs; i++) {
      final double ord = i * inc;
      final Point p = new PointDouble(ord, ord, Point.NULL_ORDINATE);
      final Geometry pt = geomFact.point(p);
      circles[i] = (Polygon)pt.buffer(inc / 2);
    }
    return geomFact.multiPolygon(circles);

  }

  Geometry createDiagonalLine(final double extent, final int nSegs) {
    final Point[] pts = new Point[nSegs + 1];
    pts[0] = new PointDouble((double)0, 0, Point.NULL_ORDINATE);
    final double inc = extent / nSegs;
    for (int i = 1; i <= nSegs; i++) {
      final double ord = i * inc;
      pts[i] = new PointDouble(ord, ord, Point.NULL_ORDINATE);
    }
    return geomFact.lineString(pts);
  }

  Geometry createLine(final double extent, final int nSegs) {
    final Point[] pts = new Point[] {
      new PointDouble((double)0, 0, Point.NULL_ORDINATE),
      new PointDouble((double)0, extent, Point.NULL_ORDINATE),
      new PointDouble(extent, extent, Point.NULL_ORDINATE),
      new PointDouble(extent, 0, Point.NULL_ORDINATE)

    };
    final Geometry outline = geomFact.lineString(pts);
    final double inc = extent / nSegs;
    return Densifier.densify(outline, inc);

  }

  Geometry[] createPoints(final BoundingBox extent, final int nPtsSide) {
    final Geometry[] pts = new Geometry[nPtsSide * nPtsSide];
    int index = 0;
    final double xinc = extent.getWidth() / nPtsSide;
    final double yinc = extent.getHeight() / nPtsSide;
    for (int i = 0; i < nPtsSide; i++) {
      for (int j = 0; j < nPtsSide; j++) {
        pts[index++] = geomFact.point(new PointDouble(extent.getMinX() + i
          * xinc, extent.getMinY() + j * yinc, Point.NULL_ORDINATE));
      }
    }
    return pts;
  }

  Geometry loadData(final String file) throws Exception {
    final List geoms = InteriorPointTest.getTestGeometries(file);
    return geomFact.buildGeometry(geoms);
  }

  List loadWKT(final String filename) throws Exception {
    final WKTReader rdr = new WKTReader();
    final WKTFileReader fileRdr = new WKTFileReader(filename, rdr);
    return fileRdr.read();
  }

  public void test() throws Exception {

    // test(200);
    // if (true) return;

    // test(5000);
    // test(8001);

    // test(50);
    test(100);
    test(200);
    test(500);
    test(1000);
    // test(5000);
    // test(10000);
    // test(50000);
    // test(100000);
  }

  public void test(final Geometry[] pts, final Geometry target) {
    if (this.verbose) {
      // System.out.println("Query points = " + pts.length
      // + "     Target points = " + target.getVertexCount());
      // if (! verbose) System.out.print(num + ", ");
    }

    final Stopwatch sw = new Stopwatch();
    final double dist = 0.0;
    for (int i = 0; i < MAX_ITER; i++) {
      computeDistance(pts, target);
    }
    if (!this.verbose) {
      // System.out.println(sw.getTimeString());
    }
    if (this.verbose) {
      final String name = USE_INDEXED_DIST ? "IndexedFacetDistance"
        : "Distance";
      // System.out.println(name + " - Run time: " + sw.getTimeString());
      // System.out.println("       (Distance = " + dist + ")\n");
      // System.out.println();
    }
  }

  public void test(final int num) throws Exception {
    // Geometry lines = createLine(EXTENT, num);
    final Geometry target = createDiagonalCircles(EXTENT, NUM_TARGET_ITEMS);
    final Geometry[] pts = createPoints(target.getBoundingBox(), num);

    test(pts, target);
  }

}
