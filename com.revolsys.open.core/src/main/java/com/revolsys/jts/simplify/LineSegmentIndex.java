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

import com.revolsys.collection.Visitor;
import com.revolsys.gis.algorithm.index.quadtree.QuadTree;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.LineSegmentDouble;
import com.revolsys.jts.util.BoundingBoxUtil;

/**
 * An spatial index on a set of {@link LineSegmentDouble}s.
 * Supports adding and removing items.
 *
 * @author Martin Davis
 *
 */
@Deprecated
class LineSegmentIndex {
  private final QuadTree<LineSegment> index = new QuadTree<>();

  public LineSegmentIndex() {
  }

  public void add(final LineSegment seg) {
    this.index.insert(new BoundingBoxDoubleGf(seg.getP0(), seg.getP1()), seg);
  }

  public void add(final TaggedLineString line) {
    final TaggedLineSegment[] segs = line.getSegments();
    for (final TaggedLineSegment seg : segs) {
      add(seg);
    }
  }

  public List query(final LineSegment querySeg) {
    final BoundingBoxDoubleGf env = new BoundingBoxDoubleGf(querySeg.getP0(), querySeg.getP1());

    final LineSegmentVisitor visitor = new LineSegmentVisitor(querySeg);
    this.index.visit(env, visitor);
    final List itemsFound = visitor.getItems();

    // List listQueryItems = index.query(env);
    // System.out.println("visitor size = " + itemsFound.size()
    // + "  query size = " + listQueryItems.size());
    // List itemsFound = index.query(env);

    return itemsFound;
  }

  public void remove(final LineSegment seg) {
    this.index.remove(new BoundingBoxDoubleGf(seg.getP0(), seg.getP1()), seg);
  }
}

/**
 * ItemVisitor subclass to reduce volume of query results.
 */
class LineSegmentVisitor implements Visitor<LineSegment> {
  // MD - only seems to make about a 10% difference in overall time.

  private final LineSegment querySeg;

  private final ArrayList<LineSegment> items = new ArrayList<>();

  public LineSegmentVisitor(final LineSegment querySeg) {
    this.querySeg = querySeg;
  }

  public List<LineSegment> getItems() {
    return this.items;
  }

  @Override
  public boolean visit(final LineSegment seg) {
    if (BoundingBoxUtil.intersects(seg.getP0(), seg.getP1(), this.querySeg.getP0(),
      this.querySeg.getP1())) {
      this.items.add(seg);
    }
    return true;
  }
}
