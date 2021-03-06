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
package com.revolsys.jts.geomgraph;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.TopologyException;
import com.revolsys.jts.util.Assert;

/**
 * @version 1.7
 */
public abstract class EdgeRing {

  protected DirectedEdge startDe; // the directed edge which starts the list of
  // edges for this EdgeRing

  private int maxNodeDegree = -1;

  private final List<DirectedEdge> edges = new ArrayList<>(); // the
  // DirectedEdges
  // making up

  // this EdgeRing

  private final List<Point> pts = new ArrayList<>();

  private final Label label = new Label(Location.NONE); // label stores the
  // locations of each
  // geometry on the face
  // surrounded by this
  // ring

  private LinearRing ring; // the ring created for this EdgeRing

  private boolean isHole;

  private EdgeRing shell; // if non-null, the ring is a hole and this EdgeRing
  // is its containing shell

  private final List<EdgeRing> holes = new ArrayList<EdgeRing>(); // a list of
  // EdgeRings
  // which

  // are holes in this EdgeRing

  protected GeometryFactory geometryFactory;

  public EdgeRing(final DirectedEdge start,
    final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    computePoints(start);
    computeRing();
  }

  public void addHole(final EdgeRing ring) {
    this.holes.add(ring);
  }

  protected void addPoints(final Edge edge, final boolean isForward,
    final boolean isFirstEdge) {
    final int numPoints = edge.getNumPoints();
    if (isForward) {
      int startIndex = 1;
      if (isFirstEdge) {
        startIndex = 0;
      }
      for (int i = startIndex; i < numPoints; i++) {
        this.pts.add(edge.getCoordinate(i));
      }
    } else { // is backward
      int startIndex = numPoints - 2;
      if (isFirstEdge) {
        startIndex = numPoints - 1;
      }
      for (int i = startIndex; i >= 0; i--) {
        this.pts.add(edge.getCoordinate(i));
      }
    }
  }

  private void computeMaxNodeDegree() {
    this.maxNodeDegree = 0;
    DirectedEdge de = this.startDe;
    do {
      final Node node = de.getNode();
      final int degree = ((DirectedEdgeStar)node.getEdges()).getOutgoingDegree(this);
      if (degree > this.maxNodeDegree) {
        this.maxNodeDegree = degree;
      }
      de = getNext(de);
    } while (de != this.startDe);
    this.maxNodeDegree *= 2;
  }

  /**
   * Collect all the points from the DirectedEdges of this ring into a contiguous list
   */
  protected void computePoints(final DirectedEdge start) {
    this.startDe = start;
    DirectedEdge de = start;
    boolean isFirstEdge = true;
    do {
      // Assert.isTrue(de != null, "found null Directed Edge");
      if (de == null) {
        throw new TopologyException("Found null DirectedEdge");
      }
      if (de.getEdgeRing() == this) {
        throw new TopologyException(
          "Directed Edge visited twice during ring-building at "
              + de.getCoordinate());
      }

      this.edges.add(de);
      final Label label = de.getLabel();
      Assert.isTrue(label.isArea());
      mergeLabel(label);
      addPoints(de.getEdge(), de.isForward(), isFirstEdge);
      isFirstEdge = false;
      setEdgeRing(de, this);
      de = getNext(de);
    } while (de != this.startDe);
  }

  /**
   * Compute a LinearRing from the point list previously collected.
   * Test if the ring is a hole (i.e. if it is CCW) and set the hole flag
   * accordingly.
   */
  public void computeRing() {
    if (this.ring == null) {
      this.ring = this.geometryFactory.linearRing(this.pts);
      this.isHole = this.ring.isCounterClockwise();
    }
  }

  /**
   * This method will cause the ring to be computed.
   * It will also check any holes, if they have been assigned.
   */
  public boolean containsPoint(final Point p) {
    final LinearRing shell = getLinearRing();
    final BoundingBox env = shell.getBoundingBox();
    if (!env.covers(p)) {
      return false;
    }
    if (!CGAlgorithms.isPointInRing(p, shell)) {
      return false;
    }

    for (final EdgeRing hole : this.holes) {
      if (hole.containsPoint(p)) {
        return false;
      }
    }
    return true;
  }

  public Point getCoordinate(final int i) {
    return this.pts.get(i);
  }

  /**
   * Returns the list of DirectedEdges that make up this EdgeRing
   */
  public List<DirectedEdge> getEdges() {
    return this.edges;
  }

  public Label getLabel() {
    return this.label;
  }

  public LinearRing getLinearRing() {
    return this.ring;
  }

  public int getMaxNodeDegree() {
    if (this.maxNodeDegree < 0) {
      computeMaxNodeDegree();
    }
    return this.maxNodeDegree;
  }

  abstract public DirectedEdge getNext(DirectedEdge de);

  public EdgeRing getShell() {
    return this.shell;
  }

  public boolean isHole() {
    // computePoints();
    return this.isHole;
  }

  public boolean isIsolated() {
    return this.label.getGeometryCount() == 1;
  }

  public boolean isShell() {
    return this.shell == null;
  }

  protected void mergeLabel(final Label deLabel) {
    mergeLabel(deLabel, 0);
    mergeLabel(deLabel, 1);
  }

  /**
   * Merge the RHS label from a DirectedEdge into the label for this EdgeRing.
   * The DirectedEdge label may be null.  This is acceptable - it results
   * from a node which is NOT an intersection node between the Geometries
   * (e.g. the end node of a LinearRing).  In this case the DirectedEdge label
   * does not contribute any information to the overall labelling, and is simply skipped.
   */
  protected void mergeLabel(final Label deLabel, final int geomIndex) {
    final Location loc = deLabel.getLocation(geomIndex, Position.RIGHT);
    // no information to be had from this label
    if (loc == Location.NONE) {
      return;
    }
    // if there is no current RHS value, set it
    if (this.label.getLocation(geomIndex) == Location.NONE) {
      this.label.setLocation(geomIndex, loc);
      return;
    }
  }

  abstract public void setEdgeRing(DirectedEdge de, EdgeRing er);

  public void setInResult() {
    DirectedEdge de = this.startDe;
    do {
      de.getEdge().setInResult(true);
      de = de.getNext();
    } while (de != this.startDe);
  }

  public void setShell(final EdgeRing shell) {
    this.shell = shell;
    if (shell != null) {
      shell.addHole(this);
    }
  }

  public Polygon toPolygon(final GeometryFactory geometryFactory) {
    final List<LinearRing> rings = new ArrayList<>();
    rings.add(getLinearRing());
    for (int i = 0; i < this.holes.size(); i++) {
      final LinearRing ring = this.holes.get(i).getLinearRing();
      rings.add(ring);
    }
    final Polygon poly = geometryFactory.polygon(rings);
    return poly;
  }

  @Override
  public String toString() {
    if (this.ring == null) {
      return this.pts.toString();
    } else {
      return this.ring.toString();
    }
  }

}
