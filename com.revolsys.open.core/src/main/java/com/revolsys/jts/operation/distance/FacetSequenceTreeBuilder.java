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

package com.revolsys.jts.operation.distance;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.index.strtree.STRtree;

public class FacetSequenceTreeBuilder {
  public static STRtree build(final Geometry g) {
    final STRtree tree = new STRtree(STR_TREE_NODE_CAPACITY);
    for (final LineString line : g.getGeometryComponents(LineString.class)) {
      int i = 0;
      final int size = line.getVertexCount();
      while (i <= size - 1) {
        int end = i + FACET_SEQUENCE_SIZE + 1;
        // if only one point remains after this section, include it in this
        // section
        if (end >= size - 1) {
          end = size;
        }
        final FacetSequence facetSequence = new LineFacetSequence(line, i, end);
        tree.insert(facetSequence.getEnvelope(), facetSequence);
        i = i + FACET_SEQUENCE_SIZE;
      }
    }
    for (final Point point : g.getGeometries(Point.class)) {
      final PointFacetSequence facetSequence = new PointFacetSequence(point);
      tree.insert(facetSequence.getEnvelope(), facetSequence);
    }
    tree.build();
    return tree;
  }

  // 6 seems to be a good facet sequence size
  private static final int FACET_SEQUENCE_SIZE = 6;

  // Seems to be better to use a minimum node capacity
  private static final int STR_TREE_NODE_CAPACITY = 4;
}
