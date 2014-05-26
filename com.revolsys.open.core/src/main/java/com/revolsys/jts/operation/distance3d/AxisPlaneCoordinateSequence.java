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

package com.revolsys.jts.operation.distance3d;

import com.revolsys.gis.model.coordinates.list.AbstractCoordinatesList;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;

/**
 * A PointList wrapper which 
 * projects 3D coordinates into one of the
 * three Cartesian axis planes,
 * using the standard orthonormal projection
 * (i.e. simply selecting the appropriate ordinates into the XY ordinates).
 * The projected data is represented as 2D coordinates.
 * 
 * @author mdavis
 *
 */
public class AxisPlaneCoordinateSequence extends AbstractCoordinatesList {

  private static final int[] XY_INDEX = new int[] {
    0, 1
  };

  private static final int[] XZ_INDEX = new int[] {
    0, 2
  };

  private static final int[] YZ_INDEX = new int[] {
    1, 2
  };

  /**
   * Creates a wrapper projecting to the XY plane.
   * 
   * @param seq the sequence to be projected
   * @return a sequence which projects coordinates
   */
  public static PointList projectToXY(final PointList seq) {
    /**
     * This is just a no-op, but return a wrapper
     * to allow better testing
     */
    return new AxisPlaneCoordinateSequence(seq, XY_INDEX);
  }

  /**
   * Creates a wrapper projecting to the XZ plane.
   * 
   * @param seq the sequence to be projected
   * @return a sequence which projects coordinates
   */
  public static PointList projectToXZ(final PointList seq) {
    return new AxisPlaneCoordinateSequence(seq, XZ_INDEX);
  }

  /**
   * Creates a wrapper projecting to the YZ plane.
   * 
   * @param seq the sequence to be projected
   * @return a sequence which projects coordinates
   */
  public static PointList projectToYZ(final PointList seq) {
    return new AxisPlaneCoordinateSequence(seq, YZ_INDEX);
  }

  private final PointList seq;

  private final int[] indexMap;

  private AxisPlaneCoordinateSequence(final PointList seq,
    final int[] indexMap) {
    this.seq = seq;
    this.indexMap = indexMap;
  }

  @Override
  public AxisPlaneCoordinateSequence clone() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Point get(final int i) {
    return new PointDouble(getX(i), getY(i), getZ(i));
  }

  @Override
  public int getAxisCount() {
    return 2;
  }

  @Override
  public double getValue(final int index, final int ordinateIndex) {
    // Z ord is always 0
    if (ordinateIndex > 1) {
      return 0;
    }
    return seq.getValue(index, indexMap[ordinateIndex]);
  }

  @Override
  public double getX(final int index) {
    return getValue(index, X);
  }

  @Override
  public double getY(final int index) {
    return getValue(index, Y);
  }

  @Override
  public double getZ(final int index) {
    return getValue(index, Z);
  }

  @Override
  public int size() {
    return seq.size();
  }

  @Override
  public Point[] toCoordinateArray() {
    throw new UnsupportedOperationException();
  }

}