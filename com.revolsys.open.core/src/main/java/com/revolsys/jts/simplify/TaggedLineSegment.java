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

package com.revolsys.jts.simplify;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.LineSegmentDouble;
import com.revolsys.jts.geom.segment.LineSegmentDoubleGF;

/**
 * A {@link LineSegmentDouble} which is tagged with its location in a parent {@link Geometry}.
 * Used to index the segments in a geometry and recover the segment locations
 * from the index.
 */
class TaggedLineSegment extends LineSegmentDouble {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final Geometry parent;

  private final int index;

  public TaggedLineSegment(final Geometry parent, final int index,
    final int axisCount, final double... coordinates) {
    super(axisCount, coordinates);
    this.parent = parent;
    this.index = index;
  }

  public TaggedLineSegment(final Point p0, final Point p1) {
    this(p0, p1, null, -1);
  }

  public TaggedLineSegment(final Point p0, final Point p1,
    final Geometry parent, final int index) {
    super(p0, p1);
    this.parent = parent;
    this.index = index;
  }

  @Override
  protected LineSegment createLineSegment(
    final GeometryFactory geometryFactory, final int axisCount,
    final double... coordinates) {
    return new LineSegmentDoubleGF(this.parent.getGeometryFactory(), axisCount,
      coordinates);
  }

  public int getIndex() {
    return this.index;
  }

  public Geometry getParent() {
    return this.parent;
  }
}
