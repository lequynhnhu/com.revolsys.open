

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
package com.revolsys.jts.operation.valid;

import java.util.Iterator;

import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geomgraph.GeometryGraph;
import com.revolsys.jts.geomgraph.index.SegmentIntersector;
import com.revolsys.jts.operation.relate.EdgeEndBundle;
import com.revolsys.jts.operation.relate.RelateNode;
import com.revolsys.jts.operation.relate.RelateNodeGraph;

/**
 * Checks that a {@link GeometryGraph} representing an area
 * (a {@link Polygon} or {@link MultiPolygon} )
 * has consistent semantics for area geometries.
 * This check is required for any reasonable polygonal model
 * (including the OGC-SFS model, as well as models which allow ring self-intersection at single points)
 * <p>
 * Checks include:
 * <ul>
 * <li>test for rings which properly intersect
 * (but not for ring self-intersection, or intersections at vertices)
 * <li>test for consistent labelling at all node points
 * (this detects vertex intersections with invalid topology,
 * i.e. where the exterior side of an edge lies in the interior of the area)
 * <li>test for duplicate rings
 * </ul>
 * If an inconsistency is found the location of the problem
 * is recorded and is available to the caller.
 *
 * @version 1.7
 */
public class ConsistentAreaTester {

  private final LineIntersector li = new RobustLineIntersector();
  private final GeometryGraph geomGraph;
  private final RelateNodeGraph nodeGraph = new RelateNodeGraph();

  // the intersection point found (if any)
  private Point invalidPoint;

  /**
   * Creates a new tester for consistent areas.
   *
   * @param geomGraph the topology graph of the area geometry
   */
  public ConsistentAreaTester(final GeometryGraph geomGraph)
  {
    this.geomGraph = geomGraph;
  }

  /**
   * @return the intersection point, or <code>null</code> if none was found
   */
  public Point getInvalidPoint() { return this.invalidPoint; }

  /**
   * Checks for two duplicate rings in an area.
   * Duplicate rings are rings that are topologically equal
   * (that is, which have the same sequence of points up to point order).
   * If the area is topologically consistent (determined by calling the
   * <code>isNodeConsistentArea</code>,
   * duplicate rings can be found by checking for EdgeBundles which contain
   * more than one EdgeEnd.
   * (This is because topologically consistent areas cannot have two rings sharing
   * the same line segment, unless the rings are equal).
   * The start point of one of the equal rings will be placed in
   * invalidPoint.
   *
   * @return true if this area Geometry is topologically consistent but has two duplicate rings
   */
  public boolean hasDuplicateRings()
  {
    for (final Iterator nodeIt = this.nodeGraph.getNodeIterator(); nodeIt.hasNext(); ) {
      final RelateNode node = (RelateNode) nodeIt.next();
      for (final Iterator i = node.getEdges().iterator(); i.hasNext(); ) {
        final EdgeEndBundle eeb = (EdgeEndBundle) i.next();
        if (eeb.getEdgeEnds().size() > 1) {
          this.invalidPoint = eeb.getEdge().getCoordinate(0);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Check all nodes to see if their labels are consistent with area topology.
   *
   * @return <code>true</code> if this area has a consistent node labelling
   */
  public boolean isNodeConsistentArea()
  {
    /**
     * To fully check validity, it is necessary to
     * compute ALL intersections, including self-intersections within a single edge.
     */
    final SegmentIntersector intersector = this.geomGraph.computeSelfNodes(this.li, true);
    if (intersector.hasProperIntersection()) {
      this.invalidPoint = intersector.getProperIntersectionPoint();
      return false;
    }

    this.nodeGraph.build(this.geomGraph);

    return isNodeEdgeAreaLabelsConsistent();
  }

  /**
   * Check all nodes to see if their labels are consistent.
   * If any are not, return false
   *
   * @return <code>true</code> if the edge area labels are consistent at this node
   */
  private boolean isNodeEdgeAreaLabelsConsistent()
  {
    for (final Iterator nodeIt = this.nodeGraph.getNodeIterator(); nodeIt.hasNext(); ) {
      final RelateNode node = (RelateNode) nodeIt.next();
      if (! node.getEdges().isAreaLabelsConsistent(this.geomGraph)) {
        this.invalidPoint = node.getCoordinate().clone();
        return false;
      }
    }
    return true;
  }



}
