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
package com.revolsys.jtstest.function;

import com.revolsys.jts.algorithm.MinimumBoundingCircle;
import com.revolsys.jts.algorithm.MinimumDiameter;
import com.revolsys.jts.densify.Densifier;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.OctagonalEnvelope;

public class ConstructionFunctions {
  public static Geometry boundary(final Geometry g) {
    return g.getBoundary();
  }

  public static Geometry centroid(final Geometry g) {
    return g.getCentroid();
  }

  public static Geometry convexHull(final Geometry g) {
    return g.convexHull();
  }

  public static Geometry densify(final Geometry g, final double distance) {
    return Densifier.densify(g, distance);
  }

  public static Geometry interiorPoint(final Geometry g) {
    return g.getInteriorPoint();
  }

  public static double maximumDiameter(final Geometry g) {
    return 2 * (new MinimumBoundingCircle(g)).getRadius();
  }

  public static Geometry minimumBoundingCircle(final Geometry g) {
    return (new MinimumBoundingCircle(g)).getCircle();
  }

  public static Geometry minimumBoundingCirclePoints(final Geometry g) {
    return g.getGeometryFactory().lineString(
      (new MinimumBoundingCircle(g)).getExtremalPoints());
  }

  public static double minimumDiameter(final Geometry g) {
    return (new MinimumDiameter(g)).getDiameter().getLength();
  }

  public static Geometry minimumDiameterLine(final Geometry g) {
    return (new MinimumDiameter(g)).getDiameter();
  }

  public static Geometry minimumRectangle(final Geometry g) {
    return (new MinimumDiameter(g)).getMinimumRectangle();
  }

  public static Geometry octagonalEnvelope(final Geometry g) {
    final OctagonalEnvelope octEnv = new OctagonalEnvelope(g);
    return octEnv.toGeometry(g.getGeometryFactory());
  }

}