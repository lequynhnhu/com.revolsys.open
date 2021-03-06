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
package com.revolsys.jts.algorithm;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.util.MathUtil;

/**
 * Computes a point in the interior of an linear geometry.
 * <h2>Algorithm</h2>
 * <ul>
 * <li>Find an interior vertex which is closest to
 * the centroid of the linestring.
 * <li>If there is no interior vertex, find the endpoint which is
 * closest to the centroid.
 * </ul>
 *
 * @version 1.7
 */
public class InteriorPointLine {

  private final Point centroid;

  private double minDistance = Double.MAX_VALUE;

  private Point interiorPoint = null;

  public InteriorPointLine(final Geometry g) {
    this.centroid = g.getCentroid();
    addInterior(g);
    if (this.interiorPoint == null) {
      addEndpoints(g);
    }
  }

  private void add(final double x, final double y) {
    final double dist = MathUtil.distance(this.centroid.getX(), this.centroid.getY(), x,
      y);
    if (dist < this.minDistance) {
      this.interiorPoint = new PointDouble(x, y);
      this.minDistance = dist;
    }
  }

  /**
   * Tests the endpoint vertices
   * defined by a linear Geometry for the best inside point.
   * If a Geometry is not of dimension 1 it is not tested.
   * @param geometry the geometry to add
   */
  private void addEndpoints(final Geometry geometry) {
    if (geometry instanceof LineString) {
      addEndpoints((LineString)geometry);
    } else if (geometry instanceof GeometryCollection) {
      for (final Geometry part : geometry.geometries()) {
        addEndpoints(part);
      }
    }
  }

  private void addEndpoints(final LineString line) {
    add(line.getX(0), line.getY(0));
    add(line.getX(-1), line.getY(-1));
  }

  /**
   * Tests the interior vertices (if any)
   * defined by a linear Geometry for the best inside point.
   * If a Geometry is not of dimension 1 it is not tested.
   * @param geometry the geometry to add
   */
  private void addInterior(final Geometry geometry) {
    if (geometry instanceof LineString) {
      addInterior((LineString)geometry);
    } else if (geometry instanceof GeometryCollection) {
      final GeometryCollection gc = (GeometryCollection)geometry;
      for (int i = 0; i < gc.getGeometryCount(); i++) {
        addInterior(gc.getGeometry(i));
      }
    }
  }

  private void addInterior(final LineString line) {
    for (int i = 1; i < line.getVertexCount() - 1; i++) {
      final double x = line.getX(i);
      final double y = line.getY(i);
      add(x, y);
    }
  }

  public Point getInteriorPoint() {
    return this.interiorPoint;
  }

}
