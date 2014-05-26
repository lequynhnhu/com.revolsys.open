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
package com.revolsys.jts.geom.prep;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.algorithm.PointLocator;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.vertex.Vertex;

/**
 * A base class for {@link PreparedGeometry} subclasses.
 * Contains default implementations for methods, which simply delegate
 * to the equivalent {@link Geometry} methods.
 * This class may be used as a "no-op" class for Geometry types
 * which do not have a corresponding {@link PreparedGeometry} implementation.
 * 
 * @author Martin Davis
 *
 */
class BasicPreparedGeometry implements PreparedGeometry {
  private final Geometry geometry;

  public BasicPreparedGeometry(final Geometry geometry) {
    this.geometry = geometry;
  }

  /**
   * Default implementation.
   */
  @Override
  public boolean contains(final Geometry geometry) {
    return this.geometry.contains(geometry);
  }

  /**
   * Default implementation.
   */
  @Override
  public boolean containsProperly(final Geometry geometry) {
    // since raw relate is used, provide some optimizations

    // short-circuit test
    if (!this.geometry.getBoundingBox().covers(geometry.getBoundingBox())) {
      return false;
    }

    // otherwise, compute using relate mask
    return this.geometry.relate(geometry, "T**FF*FF*");
  }

  /**
   * Default implementation.
   */
  @Override
  public boolean coveredBy(final Geometry geometry) {
    return this.geometry.coveredBy(geometry);
  }

  /**
   * Default implementation.
   */
  @Override
  public boolean covers(final Geometry geometry) {
    return this.geometry.covers(geometry);
  }

  /**
   * Default implementation.
   */
  @Override
  public boolean crosses(final Geometry geometry) {
    return this.geometry.crosses(geometry);
  }

  /**
   * Standard implementation for all geometries.
   * Supports {@link GeometryCollection}s as input.
   */
  @Override
  public boolean disjoint(final Geometry geometry) {
    return !intersects(geometry);
  }

  /**
   * Determines whether the envelope of 
   * this geometry covers the Geometry  geometry.
   * 
   *  
   * @param g a Geometry
   * @return true if g is contained in this envelope
   */
  protected boolean envelopeCovers(final Geometry geometry) {
    if (!this.geometry.getBoundingBox().covers(geometry.getBoundingBox())) {
      return false;
    }
    return true;
  }

  /**
   * Determines whether a Geometry g interacts with 
   * this geometry by testing the geometry envelopes.
   *  
   * @param g a Geometry
   * @return true if the envelopes intersect
   */
  protected boolean envelopesIntersect(final Geometry geometry) {
    if (!this.geometry.getBoundingBox().intersects(geometry.getBoundingBox())) {
      return false;
    }
    return true;
  }

  @Override
  public Geometry getGeometry() {
    return geometry;
  }

  /**
   * Gets the list of representative points for this geometry.
   * One vertex is included for every component of the geometry
   * (i.e. including one for every ring of polygonal geometries).
   * 
   * Do not modify the returned list!
   * 
   * @return a List of Coordinate
   */
  public List<Point> getRepresentativePoints() {
    final List<Point> points = new ArrayList<Point>();
    for (final Vertex vertex : geometry.vertices()) {
      points.add(vertex.cloneCoordinates());
    }
    return points;
  }

  /**
   * Default implementation.
   */
  @Override
  public boolean intersects(final Geometry geometry) {
    return geometry.intersects(geometry);
  }

  /**
   * Tests whether any representative of the target geometry 
   * intersects the test geometry.
   * This is useful in A/A, A/L, A/P, L/P, and P/P cases.
   * 
   * @param geom the test geometry
   * @param repPts the representative points of the target geometry
   * @return true if any component intersects the areal test geometry
   */
  public boolean isAnyTargetComponentInTest(final Geometry testGeom) {
    final PointLocator locator = new PointLocator();
    for (final Vertex vertex : geometry.vertices()) {
      if (locator.intersects(vertex, testGeom)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Default implementation.
   */
  @Override
  public boolean overlaps(final Geometry geometry) {
    return geometry.overlaps(geometry);
  }

  @Override
  public String toString() {
    return geometry.toString();
  }

  /**
   * Default implementation.
   */
  @Override
  public boolean touches(final Geometry geometry) {
    return geometry.touches(geometry);
  }

  /**
   * Default implementation.
   */
  @Override
  public boolean within(final Geometry geometry) {
    return geometry.within(geometry);
  }
}