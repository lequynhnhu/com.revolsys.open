package com.revolsys.jts.testold.perf.geom.prep;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.geom.util.SineStarFactory;
import com.revolsys.jts.testold.perf.ThreadTestCase;
import com.revolsys.jts.testold.perf.ThreadTestRunner;

/**
 * Tests for race conditons in the Geometry classes.
 *
 * @author Martin Davis
 *
 */
public class PreparedGeometryThreadSafeTest extends ThreadTestCase {
  public static void main(final String[] args) {
    ThreadTestRunner.run(new PreparedGeometryThreadSafeTest());
  }

  int nPts = 1000;

  GeometryFactory factory = GeometryFactory.fixed(0, 1.0);

  protected Geometry pg;

  protected Geometry g;

  public PreparedGeometryThreadSafeTest() {

  }

  Geometry createSineStar(final Point origin, final double size, final int nPts) {
    final SineStarFactory gsf = new SineStarFactory(this.factory);
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    gsf.setArmLengthRatio(0.1);
    gsf.setNumArms(20);
    final Geometry poly = gsf.createSineStar();
    return poly;
  }

  @Override
  public Runnable getRunnable(final int threadIndex) {
    return new Runnable() {

      @Override
      public void run() {
        while (true) {
          // System.out.println(threadIndex);
          PreparedGeometryThreadSafeTest.this.pg.intersects(PreparedGeometryThreadSafeTest.this.g);
        }
      }

    };
  }

  @Override
  public void setup() {
    final Geometry sinePoly = createSineStar(new PointDouble((double)0, 0,
      Point.NULL_ORDINATE), 100000.0, this.nPts);
    this.pg = sinePoly.prepare();
    this.g = createSineStar(
      new PointDouble((double)10, 10, Point.NULL_ORDINATE), 100000.0, 100);
  }
}
