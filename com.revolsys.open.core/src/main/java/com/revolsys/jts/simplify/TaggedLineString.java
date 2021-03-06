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

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.Segment;

/**
 * Represents a {@link LineString} which can be modified to a simplified shape.
 * This class provides an attribute which specifies the minimum allowable length
 * for the modified result.
 *
 * @version 1.7
 */
class TaggedLineString {

  private static Point[] extractCoordinates(final List<LineSegment> segs) {
    final Point[] pts = new Point[segs.size() + 1];
    LineSegment seg = null;
    for (int i = 0; i < segs.size(); i++) {
      seg = segs.get(i);
      pts[i] = seg.getP0();
    }
    // add last point
    pts[pts.length - 1] = seg.getP1();
    return pts;
  }

  private final LineString parentLine;

  private TaggedLineSegment[] segs;

  private final List resultSegs = new ArrayList();

  private final int minimumSize;

  public TaggedLineString(final LineString parentLine) {
    this(parentLine, 2);
  }

  public TaggedLineString(final LineString parentLine, final int minimumSize) {
    this.parentLine = parentLine;
    this.minimumSize = minimumSize;
    init();
  }

  public void addToResult(final LineSegment seg) {
    this.resultSegs.add(seg);
  }

  public LinearRing asLinearRing() {
    return this.parentLine.getGeometryFactory().linearRing(
      extractCoordinates(this.resultSegs));
  }

  public LineString asLineString() {
    return this.parentLine.getGeometryFactory().lineString(
      extractCoordinates(this.resultSegs));
  }

  public int getMinimumSize() {
    return this.minimumSize;
  }

  public LineString getParent() {
    return this.parentLine;
  }

  public Point[] getResultCoordinates() {
    return extractCoordinates(this.resultSegs);
  }

  public int getResultSize() {
    final int resultSegsSize = this.resultSegs.size();
    return resultSegsSize == 0 ? 0 : resultSegsSize + 1;
  }

  public TaggedLineSegment getSegment(final int i) {
    return this.segs[i];
  }

  public TaggedLineSegment[] getSegments() {
    return this.segs;
  }

  private void init() {
    this.segs = new TaggedLineSegment[this.parentLine.getVertexCount() - 1];
    int i = 0;
    for (final Segment segment : this.parentLine.segments()) {
      final TaggedLineSegment seg = new TaggedLineSegment(segment.getPoint(0)
        .clonePoint(), segment.getPoint(1).clonePoint(),
        this.parentLine, i);
      this.segs[i] = seg;
      i++;
    }
  }

}
