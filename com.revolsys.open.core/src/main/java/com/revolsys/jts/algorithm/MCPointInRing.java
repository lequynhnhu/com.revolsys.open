
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
package com.revolsys.jts.algorithm;

import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.algorithm.locate.IndexedPointInAreaLocator;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateArrays;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.index.bintree.Bintree;
import com.revolsys.jts.index.bintree.Interval;
import com.revolsys.jts.index.chain.MonotoneChain;
import com.revolsys.jts.index.chain.MonotoneChainBuilder;
import com.revolsys.jts.index.chain.MonotoneChainSelectAction;

/**
 * Implements {@link PointInRing}
 * using {@link MonotoneChain}s and a {@link Bintree} index to
 * increase performance.
 *
 * @version 1.7
 * 
 * @see IndexedPointInAreaLocator for more general functionality
 */
public class MCPointInRing   implements PointInRing {

  class MCSelecter extends MonotoneChainSelectAction
  {
    Coordinates p;

    public MCSelecter(Coordinates p)
    {
      this.p = p;
    }

    public void select(LineSegment ls)
    {
      testLineSegment(p, ls);
    }
  }

  private LinearRing ring;
  private Bintree tree;
  private int crossings = 0;  // number of segment/ray crossings

  public MCPointInRing(LinearRing ring)
  {
    this.ring = ring;
    buildIndex();
  }

  private void buildIndex()
  {
    //Envelope env = ring.getEnvelopeInternal();
    tree = new Bintree();

    Coordinates[] pts = CoordinateArrays.removeRepeatedPoints(ring.getCoordinateArray());
    List mcList = MonotoneChainBuilder.getChains(pts);

    for (int i = 0; i < mcList.size(); i++) {
      MonotoneChain mc = (MonotoneChain) mcList.get(i);
      Envelope mcEnv = mc.getEnvelope();
      interval.min = mcEnv.getMinY();
      interval.max = mcEnv.getMaxY();
      tree.insert(interval, mc);
    }
  }

  private Interval interval = new Interval();

  public boolean isInside(Coordinates pt)
  {
    crossings = 0;

    // test all segments intersected by ray from pt in positive x direction
    Envelope rayEnv = new Envelope(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, pt.getY(), pt.getY());

    interval.min = pt.getY();
    interval.max = pt.getY();
    List segs = tree.query(interval);
//System.out.println("query size = " + segs.size());

    MCSelecter mcSelecter = new MCSelecter(pt);
    for (Iterator i = segs.iterator(); i.hasNext(); ) {
      MonotoneChain mc = (MonotoneChain) i.next();
      testMonotoneChain(rayEnv, mcSelecter, mc);
    }

    /*
     *  p is inside if number of crossings is odd.
     */
    if ((crossings % 2) == 1) {
      return true;
    }
    return false;
  }


  private void testMonotoneChain(Envelope rayEnv, MCSelecter mcSelecter, MonotoneChain mc)
  {
    mc.select(rayEnv, mcSelecter);
  }

  private void testLineSegment(Coordinates p, LineSegment seg) {
    double xInt;  // x intersection of segment with ray
    double x1;    // translated coordinates
    double y1;
    double x2;
    double y2;

    /*
     *  Test if segment crosses ray from test point in positive x direction.
     */
    Coordinates p1 = seg.getP0();
    Coordinates p2 = seg.getP1();
    x1 = p1.getX() - p.getX();
    y1 = p1.getY() - p.getY();
    x2 = p2.getX() - p.getX();
    y2 = p2.getY() - p.getY();

    if (((y1 > 0) && (y2 <= 0)) ||
        ((y2 > 0) && (y1 <= 0))) {
        /*
         *  segment straddles x axis, so compute intersection.
         */
      xInt = RobustDeterminant.signOfDet2x2(x1, y1, x2, y2) / (y2 - y1);
        //xsave = xInt;
        /*
         *  crosses ray if strictly positive intersection.
         */
      if (0.0 < xInt) {
        crossings++;
      }
    }
  }

}