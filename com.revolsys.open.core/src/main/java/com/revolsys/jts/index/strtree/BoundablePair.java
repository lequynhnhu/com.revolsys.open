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
package com.revolsys.jts.index.strtree;

import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.util.PriorityQueue;

/**
 * A pair of {@link Boundable}s, whose leaf items
 * support a distance metric between them.
 * Used to compute the distance between the members,
 * and to expand a member relative to the other
 * in order to produce new branches of the
 * Branch-and-Bound evaluation tree.
 * Provides an ordering based on the distance between the members,
 * which allows building a priority queue by minimum distance.
 *
 * @author Martin Davis
 *
 */
class BoundablePair
implements Comparable
{
  private static double area(final Boundable b)
  {
    return ((BoundingBox) b.getBounds()).getArea();
  }
  public static boolean isComposite(final Object item)
  {
    return item instanceof AbstractNode;
  }
  private final Boundable boundable1;
  private final Boundable boundable2;

  private final double distance;

  private final ItemDistance itemDistance;
  //private double maxDistance = -1.0;

  public BoundablePair(final Boundable boundable1, final Boundable boundable2, final ItemDistance itemDistance)
  {
    this.boundable1 = boundable1;
    this.boundable2 = boundable2;
    this.itemDistance = itemDistance;
    this.distance = distance();
  }


  /*
  public double getMaximumDistance()
  {
  	if (maxDistance < 0.0)
  		maxDistance = maxDistance();
  	return maxDistance;
  }
   */

  /*
  private double maxDistance()
  {
    return maximumDistance(
        (BoundingBoxDoubleGf) boundable1.getBounds(),
        (BoundingBoxDoubleGf) boundable2.getBounds());
  }

  private static double maximumDistance(BoundingBoxDoubleGf env1, BoundingBoxDoubleGf env2)
  {
  	double minx = Math.min(env1.getMinX(), env2.getMinX());
  	double miny = Math.min(env1.getMinY(), env2.getMinY());
  	double maxx = Math.max(env1.getMaxX(), env2.getMaxX());
  	double maxy = Math.max(env1.getMaxY(), env2.getMaxY());
    Point min = new PointDouble((double)minx, miny);
    Point max = new PointDouble((double)maxx, maxy);
    return min.distance(max);
  }
   */

  /**
   * Compares two pairs based on their minimum distances
   */
  @Override
  public int compareTo(final Object o)
  {
    final BoundablePair nd = (BoundablePair) o;
    if (this.distance < nd.distance) {
      return -1;
    }
    if (this.distance > nd.distance) {
      return 1;
    }
    return 0;
  }

  /**
   * Computes the distance between the {@link Boundable}s in this pair.
   * The boundables are either composites or leaves.
   * If either is composite, the distance is computed as the minimum distance
   * between the bounds.
   * If both are leaves, the distance is computed by {@link #itemDistance(ItemBoundable, ItemBoundable)}.
   *
   * @return
   */
  private double distance()
  {
    // if items, compute exact distance
    if (isLeaves()) {
      return this.itemDistance.distance((ItemBoundable) this.boundable1,
        (ItemBoundable) this.boundable2);
    }
    // otherwise compute distance between bounds of boundables
    return ((BoundingBox) this.boundable1.getBounds()).distance(
      (BoundingBoxDoubleGf) this.boundable2.getBounds());
  }

  private void expand(final Boundable bndComposite, final Boundable bndOther,
    final PriorityQueue priQ, final double minDistance)
  {
    final List children = ((AbstractNode) bndComposite).getChildBoundables();
    for (final Iterator i = children.iterator(); i.hasNext(); ) {
      final Boundable child = (Boundable) i.next();
      final BoundablePair bp = new BoundablePair(child, bndOther, this.itemDistance);
      // only add to queue if this pair might contain the closest points
      // MD - it's actually faster to construct the object rather than called distance(child, bndOther)!
      if (bp.getDistance() < minDistance) {
        priQ.add(bp);
      }
    }
  }

  /**
   * For a pair which is not a leaf
   * (i.e. has at least one composite boundable)
   * computes a list of new pairs
   * from the expansion of the larger boundable.
   *
   */
  public void expandToQueue(final PriorityQueue priQ, final double minDistance)
  {
    final boolean isComp1 = isComposite(this.boundable1);
    final boolean isComp2 = isComposite(this.boundable2);

    /**
     * HEURISTIC: If both boundable are composite,
     * choose the one with largest area to expand.
     * Otherwise, simply expand whichever is composite.
     */
    if (isComp1 && isComp2) {
      if (area(this.boundable1) > area(this.boundable2)) {
        expand(this.boundable1, this.boundable2, priQ, minDistance);
        return;
      }
      else {
        expand(this.boundable2, this.boundable1, priQ, minDistance);
        return;
      }
    }
    else if (isComp1) {
      expand(this.boundable1, this.boundable2, priQ, minDistance);
      return;
    }
    else if (isComp2) {
      expand(this.boundable2, this.boundable1, priQ, minDistance);
      return;
    }

    throw new IllegalArgumentException("neither boundable is composite");
  }

  /**
   * Gets one of the member {@link Boundable}s in the pair
   * (indexed by [0, 1]).
   *
   * @param i the index of the member to return (0 or 1)
   * @return the chosen member
   */
  public Boundable getBoundable(final int i)
  {
    if (i == 0) {
      return this.boundable1;
    }
    return this.boundable2;
  }

  /**
   * Gets the minimum possible distance between the Boundables in
   * this pair.
   * If the members are both items, this will be the
   * exact distance between them.
   * Otherwise, this distance will be a lower bound on
   * the distances between the items in the members.
   *
   * @return the exact or lower bound distance for this pair
   */
  public double getDistance() { return this.distance; }

  /**
   * Tests if both elements of the pair are leaf nodes
   *
   * @return true if both pair elements are leaf nodes
   */
  public boolean isLeaves()
  {
    return ! (isComposite(this.boundable1) || isComposite(this.boundable2));
  }
}
