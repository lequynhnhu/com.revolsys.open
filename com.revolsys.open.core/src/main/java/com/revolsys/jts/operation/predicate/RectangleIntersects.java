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

package com.revolsys.jts.operation.predicate;

import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.algorithm.RectangleLineIntersector;
import com.revolsys.jts.algorithm.locate.SimplePointInAreaLocator;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.util.ShortCircuitedGeometryVisitor;

/**
 * Tests whether it can be concluded that a rectangle intersects a geometry,
 * based on the relationship of the envelope(s) of the geometry.
 *
 * @author Martin Davis
 * @version 1.7
 */
class EnvelopeIntersectsVisitor extends ShortCircuitedGeometryVisitor {
  private final BoundingBox rectEnv;

  private boolean intersects = false;

  public EnvelopeIntersectsVisitor(final BoundingBox rectEnv) {
    this.rectEnv = rectEnv;
  }

  /**
   * Reports whether it can be concluded that an intersection occurs,
   * or whether further testing is required.
   *
   * @return true if an intersection must occur
   * or false if no conclusion about intersection can be made
   */
  public boolean intersects() {
    return this.intersects;
  }

  @Override
  protected boolean isDone() {
    return this.intersects == true;
  }

  @Override
  protected void visit(final Geometry element) {
    final BoundingBox elementEnv = element.getBoundingBox();

    // disjoint => no intersection
    if (!this.rectEnv.intersects(elementEnv)) {
      return;
    }
    // rectangle contains target env => must intersect
    if (this.rectEnv.covers(elementEnv)) {
      this.intersects = true;
      return;
    }
    /**
     * Since the envelopes intersect and the test element is connected, if the
     * test envelope is completely bisected by an edge of the rectangle the
     * element and the rectangle must touch (This is basically an application of
     * the Jordan Curve Theorem). The alternative situation is that the test
     * envelope is "on a corner" of the rectangle envelope, i.e. is not
     * completely bisected. In this case it is not possible to make a conclusion
     * about the presence of an intersection.
     */
    if (elementEnv.getMinX() >= this.rectEnv.getMinX()
        && elementEnv.getMaxX() <= this.rectEnv.getMaxX()) {
      this.intersects = true;
      return;
    }
    if (elementEnv.getMinY() >= this.rectEnv.getMinY()
        && elementEnv.getMaxY() <= this.rectEnv.getMaxY()) {
      this.intersects = true;
      return;
    }
  }
}

/**
 * A visitor which tests whether it can be
 * concluded that a geometry contains a vertex of
 * a query geometry.
 *
 * @author Martin Davis
 * @version 1.7
 */
class GeometryContainsPointVisitor extends ShortCircuitedGeometryVisitor {
  private final LineString rectSeq;

  private final BoundingBox rectEnv;

  private boolean containsPoint = false;

  public GeometryContainsPointVisitor(final Polygon rectangle) {
    this.rectSeq = rectangle.getExteriorRing();
    this.rectEnv = rectangle.getBoundingBox();
  }

  /**
   * Reports whether it can be concluded that a corner point of the rectangle is
   * contained in the geometry, or whether further testing is required.
   *
   * @return true if a corner point is contained
   * or false if no conclusion about intersection can be made
   */
  public boolean containsPoint() {
    return this.containsPoint;
  }

  @Override
  protected boolean isDone() {
    return this.containsPoint == true;
  }

  @Override
  protected void visit(final Geometry geom) {
    // if test geometry is not polygonal this check is not needed
    if (!(geom instanceof Polygon)) {
      return;
    }

    // skip if envelopes do not intersect
    final BoundingBox elementEnv = geom.getBoundingBox();
    if (!this.rectEnv.intersects(elementEnv)) {
      return;
    }

    // test each corner of rectangle for inclusion
    for (int i = 0; i < 4; i++) {
      final Point rectPt = this.rectSeq.getPoint(i);
      if (!elementEnv.covers(rectPt)) {
        continue;
      }
      // check rect point in poly (rect is known not to touch polygon at this
      // point)
      if (SimplePointInAreaLocator.containsPointInPolygon(rectPt, (Polygon)geom)) {
        this.containsPoint = true;
        return;
      }
    }
  }
}

/**
 * Implementation of the <tt>intersects</tt> spatial predicate
 * optimized for the case where one {@link Geometry} is a rectangle.
 * This class works for all
 * input geometries, including {@link GeometryCollection}s.
 * <p>
 * As a further optimization,
 * this class can be used in batch style
 * to test many geometries
 * against a single rectangle.
 *
 * @version 1.7
 */
public class RectangleIntersects {
  /**
   * Tests whether a rectangle intersects a given geometry.
   *
   * @param rectangle
   *          a rectangular Polygon
   * @param b
   *          a Geometry of any type
   * @return true if the geometries intersect
   */
  public static boolean intersects(final Polygon rectangle, final Geometry b) {
    final RectangleIntersects rp = new RectangleIntersects(rectangle);
    return rp.intersects(b);
  }

  private final Polygon rectangle;

  private final BoundingBox rectEnv;

  /**
   * Create a new intersects computer for a rectangle.
   *
   * @param rectangle
   *          a rectangular Polygon
   */
  public RectangleIntersects(final Polygon rectangle) {
    this.rectangle = rectangle;
    this.rectEnv = rectangle.getBoundingBox();
  }

  /**
   * Tests whether the given Geometry intersects
   * the query rectangle.
   *
   * @param geom the Geometry to test (may be of any type)
   * @return true if the geometry intersects the query rectangle
   */
  public boolean intersects(final Geometry geom) {
    if (!this.rectEnv.intersects(geom.getBoundingBox())) {
      return false;
    }

    /**
     * Test if rectangle envelope intersects any component envelope.
     * This handles Point components as well
     */
    final EnvelopeIntersectsVisitor visitor = new EnvelopeIntersectsVisitor(
      this.rectEnv);
    visitor.applyTo(geom);
    if (visitor.intersects()) {
      return true;
    }

    /**
     * Test if any rectangle vertex is contained in the target geometry
     */
    final GeometryContainsPointVisitor ecpVisitor = new GeometryContainsPointVisitor(
      this.rectangle);
    ecpVisitor.applyTo(geom);
    if (ecpVisitor.containsPoint()) {
      return true;
    }

    /**
     * Test if any target geometry line segment intersects the rectangle
     */
    final RectangleIntersectsSegmentVisitor riVisitor = new RectangleIntersectsSegmentVisitor(
      this.rectangle);
    riVisitor.applyTo(geom);
    if (riVisitor.intersects()) {
      return true;
    }

    return false;
  }
}

/**
 * A visitor to test for intersection between the query
 * rectangle and the line segments of the geometry.
 *
 * @author Martin Davis
 *
 */
class RectangleIntersectsSegmentVisitor extends ShortCircuitedGeometryVisitor {
  private final BoundingBox rectEnv;

  private final RectangleLineIntersector rectIntersector;

  private boolean hasIntersection = false;

  /**
   * Creates a visitor for checking rectangle intersection
   * with segments
   *
   * @param rectangle the query rectangle
   */
  public RectangleIntersectsSegmentVisitor(final Polygon rectangle) {
    this.rectEnv = rectangle.getBoundingBox();
    this.rectIntersector = new RectangleLineIntersector(this.rectEnv);
  }

  private void checkIntersectionWithLineStrings(final List lines) {
    for (final Iterator i = lines.iterator(); i.hasNext();) {
      final LineString testLine = (LineString)i.next();
      checkIntersectionWithSegments(testLine);
      if (this.hasIntersection) {
        return;
      }
    }
  }

  private void checkIntersectionWithSegments(final LineString testLine) {
    final LineString seq1 = testLine;
    for (int j = 1; j < seq1.getVertexCount(); j++) {
      final Point p0 = seq1.getPoint(j - 1);
      final Point p1 = seq1.getPoint(j);

      if (this.rectIntersector.intersects(p0, p1)) {
        this.hasIntersection = true;
        return;
      }
    }
  }

  /**
   * Reports whether any segment intersection exists.
   *
   * @return true if a segment intersection exists
   * or false if no segment intersection exists
   */
  public boolean intersects() {
    return this.hasIntersection;
  }

  @Override
  protected boolean isDone() {
    return this.hasIntersection == true;
  }

  @Override
  protected void visit(final Geometry geom) {
    /**
     * It may be the case that the rectangle and the
     * envelope of the geometry component are disjoint,
     * so it is worth checking this simple condition.
     */
    final BoundingBox elementEnv = geom.getBoundingBox();
    if (!this.rectEnv.intersects(elementEnv)) {
      return;
    }

    // check segment intersections
    // get all lines from geometry component
    // (there may be more than one if it's a multi-ring polygon)
    final List lines = geom.getGeometryComponents(LineString.class);
    checkIntersectionWithLineStrings(lines);
  }
}
