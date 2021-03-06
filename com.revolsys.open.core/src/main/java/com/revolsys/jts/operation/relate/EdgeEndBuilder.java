
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
package com.revolsys.jts.operation.relate;

/**
 * An EdgeEndBuilder creates EdgeEnds for all the "split edges"
 * created by the
 * intersections determined for an Edge.
 *
 * @version 1.7
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geomgraph.Edge;
import com.revolsys.jts.geomgraph.EdgeEnd;
import com.revolsys.jts.geomgraph.EdgeIntersection;
import com.revolsys.jts.geomgraph.EdgeIntersectionList;
import com.revolsys.jts.geomgraph.Label;

/**
 * Computes the {@link EdgeEnd}s which arise from a noded {@link Edge}.
 *
 * @version 1.7
 */
public class EdgeEndBuilder {

  public EdgeEndBuilder() {
  }

  /**
   * Creates stub edges for all the intersections in this
   * Edge (if any) and inserts them into the graph.
   */
  public void computeEdgeEnds(final Edge edge, final List l) {
    final EdgeIntersectionList eiList = edge.getEdgeIntersectionList();
    // Debug.print(eiList);
    // ensure that the list has entries for the first and last point of the edge
    eiList.addEndpoints();

    final Iterator it = eiList.iterator();
    EdgeIntersection eiPrev = null;
    EdgeIntersection eiCurr = null;
    // no intersections, so there is nothing to do
    if (!it.hasNext()) {
      return;
    }
    EdgeIntersection eiNext = (EdgeIntersection)it.next();
    do {
      eiPrev = eiCurr;
      eiCurr = eiNext;
      eiNext = null;
      if (it.hasNext()) {
        eiNext = (EdgeIntersection)it.next();
      }

      if (eiCurr != null) {
        createEdgeEndForPrev(edge, l, eiCurr, eiPrev);
        createEdgeEndForNext(edge, l, eiCurr, eiNext);
      }

    } while (eiCurr != null);

  }

  public List computeEdgeEnds(final Iterator edges) {
    final List l = new ArrayList();
    for (final Iterator i = edges; i.hasNext();) {
      final Edge e = (Edge)i.next();
      computeEdgeEnds(e, l);
    }
    return l;
  }

  /**
   * Create a StubEdge for the edge after the intersection eiCurr.
   * The next intersection is provided
   * in case it is the endpoint for the stub edge.
   * Otherwise, the next point from the parent edge will be the endpoint.
   * <br>
   * eiCurr will always be an EdgeIntersection, but eiNext may be null.
   */
  void createEdgeEndForNext(final Edge edge, final List l,
    final EdgeIntersection eiCurr, final EdgeIntersection eiNext) {

    final int iNext = eiCurr.segmentIndex + 1;
    // if there is no next edge there is nothing to do
    if (iNext >= edge.getNumPoints() && eiNext == null) {
      return;
    }

    Point pNext = edge.getCoordinate(iNext);

    // if the next intersection is in the same segment as the current, use it as
    // the endpoint
    if (eiNext != null && eiNext.segmentIndex == eiCurr.segmentIndex) {
      pNext = eiNext.coord;
    }

    final EdgeEnd e = new EdgeEnd(edge, eiCurr.coord, pNext, new Label(
      edge.getLabel()));
    // Debug.println(e);
    l.add(e);
  }

  /**
   * Create a EdgeStub for the edge before the intersection eiCurr.
   * The previous intersection is provided
   * in case it is the endpoint for the stub edge.
   * Otherwise, the previous point from the parent edge will be the endpoint.
   * <br>
   * eiCurr will always be an EdgeIntersection, but eiPrev may be null.
   */
  void createEdgeEndForPrev(final Edge edge, final List l,
    final EdgeIntersection eiCurr, final EdgeIntersection eiPrev) {

    int iPrev = eiCurr.segmentIndex;
    if (eiCurr.dist == 0.0) {
      // if at the start of the edge there is no previous edge
      if (iPrev == 0) {
        return;
      }
      iPrev--;
    }
    Point pPrev = edge.getCoordinate(iPrev);
    // if prev intersection is past the previous vertex, use it instead
    if (eiPrev != null && eiPrev.segmentIndex >= iPrev) {
      pPrev = eiPrev.coord;
    }

    final Label label = new Label(edge.getLabel());
    // since edgeStub is oriented opposite to it's parent edge, have to flip
    // sides for edge label
    label.flip();
    final EdgeEnd e = new EdgeEnd(edge, eiCurr.coord, pPrev, label);
    // e.print(System.out); System.out.println();
    l.add(e);
  }

}
