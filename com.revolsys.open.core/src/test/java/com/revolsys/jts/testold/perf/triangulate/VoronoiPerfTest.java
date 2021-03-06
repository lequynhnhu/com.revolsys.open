package com.revolsys.jts.testold.perf.triangulate;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.triangulate.DelaunayTriangulationBuilder;
import com.revolsys.jts.util.Stopwatch;

public class VoronoiPerfTest {
  public static void main(final String args[]) {
    final VoronoiPerfTest test = new VoronoiPerfTest();
    test.run();
  }

  final static GeometryFactory geomFact = GeometryFactory.floating3();

  final static double SIDE_LEN = 10.0;

  List randomPoints(final int nPts) {
    final List pts = new ArrayList();

    final int nSide = (int)Math.sqrt(nPts) + 1;

    for (int i = 0; i < nSide; i++) {
      for (int j = 0; j < nSide; j++) {
        final double x = i * SIDE_LEN + SIDE_LEN * Math.random();
        final double y = j * SIDE_LEN + SIDE_LEN * Math.random();
        pts.add(new PointDouble(x, y, Point.NULL_ORDINATE));
      }
    }
    return pts;
  }

  public void run() {
    run(10);
    run(100);
    run(1000);
    run(10000);
    run(100000);
    run(1000000);
  }

  public void run(final int nPts) {
    final List pts = randomPoints(nPts);
    final Stopwatch sw = new Stopwatch();
    final DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.setSites(pts);

    final Geometry g = builder.getEdges(geomFact);
    //  System.out.println("# pts: " + pts.size() + "  --  " + sw.getTimeString());
    // System.out.println(g);
  }
}
