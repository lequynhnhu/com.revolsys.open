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

package com.revolsys.jts.operation.overlay.validate;

import com.revolsys.jts.algorithm.PointLocator;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.Polygon;

/**
 * Finds the most likely {@link Location} of a point relative to
 * the polygonal components of a geometry, using a tolerance value.
 * If a point is not clearly in the Interior or Exterior,
 * it is considered to be on the Boundary.
 * In other words, if the point is within the tolerance of the Boundary,
 * it is considered to be on the Boundary; otherwise, 
 * whether it is Interior or Exterior is determined directly.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class FuzzyPointLocator {
  private final Geometry g;

  private final double boundaryDistanceTolerance;

  private final MultiLineString linework;

  private final PointLocator ptLocator = new PointLocator();

  private final LineSegment seg = new LineSegment();

  public FuzzyPointLocator(final Geometry g,
    final double boundaryDistanceTolerance) {
    this.g = g;
    this.boundaryDistanceTolerance = boundaryDistanceTolerance;
    linework = extractLinework(g);
  }

  /**
   * Extracts linework for polygonal components.
   * 
   * @param geometry the geometry from which to extract
   * @return a lineal geometry containing the extracted linework
   */
  private MultiLineString extractLinework(final Geometry geometry) {
    final GeometryFactory geometryFactory = geometry.getGeometryFactory();
    if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      return geometryFactory.createMultiLineString(polygon.getRings());
    } else {
      return geometryFactory.createMultiLineString();
    }
  }

  public Location getLocation(final Coordinates pt) {
    if (isWithinToleranceOfBoundary(pt)) {
      return Location.BOUNDARY;
      /*
       * double dist = linework.distance(point); // if point is close to
       * boundary, it is considered to be on the boundary if (dist < tolerance)
       * return Location.BOUNDARY;
       */
    }

    // now we know point must be clearly inside or outside geometry, so return
    // actual location value
    return ptLocator.locate(pt, g);
  }

  private boolean isWithinToleranceOfBoundary(final Coordinates pt) {
    for (int i = 0; i < linework.getGeometryCount(); i++) {
      final LineString line = (LineString)linework.getGeometry(i);
      final CoordinatesList seq = line.getCoordinatesList();
      for (int j = 0; j < seq.size() - 1; j++) {
        seq.getCoordinate(j, seg.getP0());
        seq.getCoordinate(j + 1, seg.getP1());
        final double dist = seg.distance(pt);
        if (dist <= boundaryDistanceTolerance) {
          return true;
        }
      }
    }
    return false;
  }
}
