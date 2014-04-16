package com.revolsys.jts.dissolve;

import com.revolsys.jts.edgegraph.EdgeGraph;
import com.revolsys.jts.edgegraph.HalfEdge;
import com.revolsys.jts.geom.Coordinates;

/**
 * A graph containing {@link DissolveHalfEdge}s.
 * 
 * @author Martin Davis
 *
 */
class DissolveEdgeGraph extends EdgeGraph {
  protected HalfEdge createEdge(final Coordinates p0) {
    return new DissolveHalfEdge(p0);
  }

}