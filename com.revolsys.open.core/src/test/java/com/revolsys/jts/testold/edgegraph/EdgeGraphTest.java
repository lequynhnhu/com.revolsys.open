package com.revolsys.jts.testold.edgegraph;

import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.edgegraph.EdgeGraph;
import com.revolsys.jts.edgegraph.EdgeGraphBuilder;
import com.revolsys.jts.edgegraph.HalfEdge;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.testold.junit.GeometryUtils;

public class EdgeGraphTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(EdgeGraphTest.class);
  }

  public EdgeGraphTest(final String name) {
    super(name);
  }

  private EdgeGraph build(final String wkt) throws ParseException {
    return build(new String[] {
      wkt
    });
  }

  private EdgeGraph build(final String[] wkt) throws ParseException {
    final List geoms = GeometryUtils.readWKT(wkt);
    return EdgeGraphBuilder.build(geoms);
  }

  private void checkEdge(final EdgeGraph graph, final Point p0,
    final Point p1) {
    final HalfEdge e = graph.findEdge(p0, p1);
    assertNotNull(e);
  }

  private void checkEdgeRing(final EdgeGraph graph, final Point p,
    final Point[] dest) {
    final HalfEdge e = graph.findEdge(p, dest[0]);
    HalfEdge onext = e;
    int i = 0;
    do {
      assertTrue(onext.dest().equals(2,dest[i++]));
      onext = onext.oNext();
    } while (onext != e);

  }

  public void testNode() throws Exception {
    final EdgeGraph graph = build("MULTILINESTRING((0 0, 1 0), (0 0, 0 1), (0 0, -1 0))");
    checkEdgeRing(graph, new PointDouble((double)0, 0, Point.NULL_ORDINATE), new Point[] {
      new PointDouble((double)1, 0, Point.NULL_ORDINATE), new PointDouble((double)0, 1, Point.NULL_ORDINATE), new PointDouble((double)-1, 0, Point.NULL_ORDINATE)
    });
    checkEdge(graph, new PointDouble((double)0, 0, Point.NULL_ORDINATE), new PointDouble((double)1, 0, Point.NULL_ORDINATE));
  }

}
