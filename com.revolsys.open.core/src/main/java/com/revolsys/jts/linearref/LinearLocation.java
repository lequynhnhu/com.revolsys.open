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

package com.revolsys.jts.linearref;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;

/**
 * Represents a location along a {@link LineString} or {@link MultiLineString}.
 * The referenced geometry is not maintained within
 * this location, but must be provided for operations which require it.
 * Various methods are provided to manipulate the location value
 * and query the geometry it references.
 */
public class LinearLocation implements Comparable {
  /**
  *  Compares two sets of location values for order.
  *
  * @param componentIndex0 a component index
  * @param segmentIndex0 a segment index
  * @param segmentFraction0 a segment fraction
  * @param componentIndex1 another component index
  * @param segmentIndex1 another segment index
  * @param segmentFraction1 another segment fraction
  *@return    a negative integer, zero, or a positive integer
  *      as the first set of location values
  *      is less than, equal to, or greater than the second set of locationValues
  */
  public static int compareLocationValues(final int componentIndex0,
    final int segmentIndex0, final double segmentFraction0,
    final int componentIndex1, final int segmentIndex1,
    final double segmentFraction1) {
    // compare component indices
    if (componentIndex0 < componentIndex1) {
      return -1;
    }
    if (componentIndex0 > componentIndex1) {
      return 1;
    }
    // compare segments
    if (segmentIndex0 < segmentIndex1) {
      return -1;
    }
    if (segmentIndex0 > segmentIndex1) {
      return 1;
    }
    // same segment, so compare segment fraction
    if (segmentFraction0 < segmentFraction1) {
      return -1;
    }
    if (segmentFraction0 > segmentFraction1) {
      return 1;
    }
    // same location
    return 0;
  }

  /**
    * Gets a location which refers to the end of a linear {@link Geometry}.
    * @param linear the linear geometry
    * @return a new <tt>LinearLocation</tt>
    */
  public static LinearLocation getEndLocation(final Geometry linear) {
    // assert: linear is LineString or MultiLineString
    final LinearLocation loc = new LinearLocation();
    loc.setToEnd(linear);
    return loc;
  }

  /**
   * Computes the {@link Coordinates} of a point a given fraction
   * along the line segment <tt>(p0, p1)</tt>.
   * If the fraction is greater than 1.0 the last
   * point of the segment is returned.
   * If the fraction is less than or equal to 0.0 the first point
   * of the segment is returned.
   * The Z ordinate is interpolated from the Z-ordinates of the given points,
   * if they are specified.
   *
   * @param p0 the first point of the line segment
   * @param p1 the last point of the line segment
   * @param frac the length to the desired point
   * @return the <tt>Coordinate</tt> of the desired point
   */
  public static Coordinates pointAlongSegmentByFraction(final Coordinates p0,
    final Coordinates p1, final double frac) {
    if (frac <= 0.0) {
      return p0;
    }
    if (frac >= 1.0) {
      return p1;
    }

    final double x = (p1.getX() - p0.getX()) * frac + p0.getX();
    final double y = (p1.getY() - p0.getY()) * frac + p0.getY();
    // interpolate Z value. If either input Z is NaN, result z will be NaN as
    // well.
    final double z = (p1.getZ() - p0.getZ()) * frac + p0.getZ();
    return new Coordinate(x, y, z);
  }

  private int componentIndex = 0;

  private int segmentIndex = 0;

  private double segmentFraction = 0.0;

  /**
   * Creates a location referring to the start of a linear geometry
   */
  public LinearLocation() {
  }

  public LinearLocation(final int segmentIndex, final double segmentFraction) {
    this(0, segmentIndex, segmentFraction);
  }

  public LinearLocation(final int componentIndex, final int segmentIndex,
    final double segmentFraction) {
    this.componentIndex = componentIndex;
    this.segmentIndex = segmentIndex;
    this.segmentFraction = segmentFraction;
    normalize();
  }

  private LinearLocation(final int componentIndex, final int segmentIndex,
    final double segmentFraction, final boolean doNormalize) {
    this.componentIndex = componentIndex;
    this.segmentIndex = segmentIndex;
    this.segmentFraction = segmentFraction;
    if (doNormalize) {
      normalize();
    }
  }

  /**
   * Creates a new location equal to a given one.
   * 
   * @param loc a LinearLocation
   */
  public LinearLocation(final LinearLocation loc) {
    this.componentIndex = loc.componentIndex;
    this.segmentIndex = loc.segmentIndex;
    this.segmentFraction = loc.segmentFraction;
  }

  /**
   * Ensures the indexes are valid for a given linear {@link Geometry}.
   *
   * @param linear a linear geometry
   */
  public void clamp(final Geometry linear) {
    if (componentIndex >= linear.getNumGeometries()) {
      setToEnd(linear);
      return;
    }
    if (segmentIndex >= linear.getVertexCount()) {
      final LineString line = (LineString)linear.getGeometry(componentIndex);
      segmentIndex = line.getVertexCount() - 1;
      segmentFraction = 1.0;
    }
  }

  /**
   * Copies this location
   *
   * @return a copy of this location
   */
  @Override
  public Object clone() {
    return new LinearLocation(componentIndex, segmentIndex, segmentFraction);
  }

  /**
   *  Compares this object with the specified index values for order.
   *
   * @param componentIndex1 a component index
   * @param segmentIndex1 a segment index
   * @param segmentFraction1 a segment fraction
   * @return    a negative integer, zero, or a positive integer as this <code>LineStringLocation</code>
   *      is less than, equal to, or greater than the specified locationValues
   */
  public int compareLocationValues(final int componentIndex1,
    final int segmentIndex1, final double segmentFraction1) {
    // compare component indices
    if (componentIndex < componentIndex1) {
      return -1;
    }
    if (componentIndex > componentIndex1) {
      return 1;
    }
    // compare segments
    if (segmentIndex < segmentIndex1) {
      return -1;
    }
    if (segmentIndex > segmentIndex1) {
      return 1;
    }
    // same segment, so compare segment fraction
    if (segmentFraction < segmentFraction1) {
      return -1;
    }
    if (segmentFraction > segmentFraction1) {
      return 1;
    }
    // same location
    return 0;
  }

  /**
   *  Compares this object with the specified object for order.
   *
   *@param  o  the <code>LineStringLocation</code> with which this <code>Coordinate</code>
   *      is being compared
   *@return    a negative integer, zero, or a positive integer as this <code>LineStringLocation</code>
   *      is less than, equal to, or greater than the specified <code>LineStringLocation</code>
   */
  @Override
  public int compareTo(final Object o) {
    final LinearLocation other = (LinearLocation)o;
    // compare component indices
    if (componentIndex < other.componentIndex) {
      return -1;
    }
    if (componentIndex > other.componentIndex) {
      return 1;
    }
    // compare segments
    if (segmentIndex < other.segmentIndex) {
      return -1;
    }
    if (segmentIndex > other.segmentIndex) {
      return 1;
    }
    // same segment, so compare segment fraction
    if (segmentFraction < other.segmentFraction) {
      return -1;
    }
    if (segmentFraction > other.segmentFraction) {
      return 1;
    }
    // same location
    return 0;
  }

  /**
   * Gets the component index for this location.
   *
   * @return the component index
   */
  public int getComponentIndex() {
    return componentIndex;
  }

  /**
   * Gets the {@link Coordinates} along the
   * given linear {@link Geometry} which is
   * referenced by this location.
   *
   * @param linearGeom the linear geometry referenced by this location
   * @return the <tt>Coordinate</tt> at the location
   */
  public Coordinates getCoordinate(final Geometry linearGeom) {
    final LineString lineComp = (LineString)linearGeom.getGeometry(componentIndex);
    final Coordinates p0 = lineComp.getCoordinate(segmentIndex);
    if (segmentIndex >= lineComp.getVertexCount() - 1) {
      return p0;
    }
    final Coordinates p1 = lineComp.getCoordinate(segmentIndex + 1);
    return pointAlongSegmentByFraction(p0, p1, segmentFraction);
  }

  /**
   * Gets a {@link LineSegment} representing the segment of the 
   * given linear {@link Geometry} which contains this location.
   *
   * @param linearGeom a linear geometry
   * @return the <tt>LineSegment</tt> containing the location
   */
  public LineSegment getSegment(final Geometry linearGeom) {
    final LineString lineComp = (LineString)linearGeom.getGeometry(componentIndex);
    final Coordinates p0 = lineComp.getCoordinate(segmentIndex);
    // check for endpoint - return last segment of the line if so
    if (segmentIndex >= lineComp.getVertexCount() - 1) {
      final Coordinates prev = lineComp.getCoordinate(lineComp.getVertexCount() - 2);
      return new LineSegment(prev, p0);
    }
    final Coordinates p1 = lineComp.getCoordinate(segmentIndex + 1);
    return new LineSegment(p0, p1);
  }

  /**
   * Gets the segment fraction for this location
   *
   * @return the segment fraction
   */
  public double getSegmentFraction() {
    return segmentFraction;
  }

  /**
   * Gets the segment index for this location
   *
   * @return the segment index
   */
  public int getSegmentIndex() {
    return segmentIndex;
  }

  /**
   * Gets the length of the segment in the given
   * Geometry containing this location.
   *
   * @param linearGeom a linear geometry
   * @return the length of the segment
   */
  public double getSegmentLength(final Geometry linearGeom) {
    final LineString lineComp = (LineString)linearGeom.getGeometry(componentIndex);

    // ensure segment index is valid
    int segIndex = segmentIndex;
    if (segmentIndex >= lineComp.getVertexCount() - 1) {
      segIndex = lineComp.getVertexCount() - 2;
    }

    final Coordinates p0 = lineComp.getCoordinate(segIndex);
    final Coordinates p1 = lineComp.getCoordinate(segIndex + 1);
    return p0.distance(p1);
  }

  /**
   * Tests whether this location is an endpoint of
   * the linear component it refers to.
   * 
   * @param linearGeom the linear geometry referenced by this location
   * @return true if the location is a component endpoint
   */
  public boolean isEndpoint(final Geometry linearGeom) {
    final LineString lineComp = (LineString)linearGeom.getGeometry(componentIndex);
    // check for endpoint
    final int nseg = lineComp.getVertexCount() - 1;
    return segmentIndex >= nseg
      || (segmentIndex == nseg && segmentFraction >= 1.0);
  }

  /**
   * Tests whether two locations
   * are on the same segment in the parent {@link Geometry}.
   * 
   * @param loc a location on the same geometry
   * @return true if the locations are on the same segment of the parent geometry
   */
  public boolean isOnSameSegment(final LinearLocation loc) {
    if (componentIndex != loc.componentIndex) {
      return false;
    }
    if (segmentIndex == loc.segmentIndex) {
      return true;
    }
    if (loc.segmentIndex - segmentIndex == 1 && loc.segmentFraction == 0.0) {
      return true;
    }
    if (segmentIndex - loc.segmentIndex == 1 && segmentFraction == 0.0) {
      return true;
    }
    return false;
  }

  /**
   * Tests whether this location refers to a valid
   * location on the given linear {@link Geometry}.
   *
   * @param linearGeom a linear geometry
   * @return true if this location is valid
   */
  public boolean isValid(final Geometry linearGeom) {
    if (componentIndex < 0 || componentIndex >= linearGeom.getNumGeometries()) {
      return false;
    }

    final LineString lineComp = (LineString)linearGeom.getGeometry(componentIndex);
    if (segmentIndex < 0 || segmentIndex > lineComp.getVertexCount()) {
      return false;
    }
    if (segmentIndex == lineComp.getVertexCount() && segmentFraction != 0.0) {
      return false;
    }

    if (segmentFraction < 0.0 || segmentFraction > 1.0) {
      return false;
    }
    return true;
  }

  /**
   * Tests whether this location refers to a vertex
   *
   * @return true if the location is a vertex
   */
  public boolean isVertex() {
    return segmentFraction <= 0.0 || segmentFraction >= 1.0;
  }

  /**
   * Ensures the individual values are locally valid.
   * Does <b>not</b> ensure that the indexes are valid for
   * a particular linear geometry.
   *
   * @see clamp
   */
  private void normalize() {
    if (segmentFraction < 0.0) {
      segmentFraction = 0.0;
    }
    if (segmentFraction > 1.0) {
      segmentFraction = 1.0;
    }

    if (componentIndex < 0) {
      componentIndex = 0;
      segmentIndex = 0;
      segmentFraction = 0.0;
    }
    if (segmentIndex < 0) {
      segmentIndex = 0;
      segmentFraction = 0.0;
    }
    if (segmentFraction == 1.0) {
      segmentFraction = 0.0;
      segmentIndex += 1;
    }
  }

  /**
   * Sets the value of this location to
   * refer to the end of a linear geometry.
   *
   * @param linear the linear geometry to use to set the end
   */
  public void setToEnd(final Geometry linear) {
    componentIndex = linear.getNumGeometries() - 1;
    final LineString lastLine = (LineString)linear.getGeometry(componentIndex);
    segmentIndex = lastLine.getVertexCount() - 1;
    segmentFraction = 1.0;
  }

  /**
   * Snaps the value of this location to
   * the nearest vertex on the given linear {@link Geometry},
   * if the vertex is closer than <tt>minDistance</tt>.
   *
   * @param linearGeom a linear geometry
   * @param minDistance the minimum allowable distance to a vertex
   */
  public void snapToVertex(final Geometry linearGeom, final double minDistance) {
    if (segmentFraction <= 0.0 || segmentFraction >= 1.0) {
      return;
    }
    final double segLen = getSegmentLength(linearGeom);
    final double lenToStart = segmentFraction * segLen;
    final double lenToEnd = segLen - lenToStart;
    if (lenToStart <= lenToEnd && lenToStart < minDistance) {
      segmentFraction = 0.0;
    } else if (lenToEnd <= lenToStart && lenToEnd < minDistance) {
      segmentFraction = 1.0;
    }
  }

  /**
   * Converts a linear location to the lowest equivalent location index.
   * The lowest index has the lowest possible component and segment indices.
   * <p>
   * Specifically:
   * <ul>
   * <li>if the location point is an endpoint, a location value is returned as (nseg-1, 1.0)
   * <li>if the location point is ambiguous (i.e. an endpoint and a startpoint), the lowest endpoint location is returned
   * </ul>
   * If the location index is already the lowest possible value, the original location is returned.
   * 
   * @param linearGeom the linear geometry referenced by this location
   * @return the lowest equivalent location
   */
  public LinearLocation toLowest(final Geometry linearGeom) {
    // TODO: compute lowest component index
    final LineString lineComp = (LineString)linearGeom.getGeometry(componentIndex);
    final int nseg = lineComp.getVertexCount() - 1;
    // if not an endpoint can be returned directly
    if (segmentIndex < nseg) {
      return this;
    }
    return new LinearLocation(componentIndex, nseg, 1.0, false);
  }

  @Override
  public String toString() {
    return "LinearLoc[" + componentIndex + ", " + segmentIndex + ", "
      + segmentFraction + "]";
  }
}