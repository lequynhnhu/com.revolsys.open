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
package com.revolsys.jts.densify;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.segment.Segment;

/**
 * Densifies a {@link Geometry} by inserting extra vertices along the line segments
 * contained in the geometry.
 * All segments in the created densified geometry will be no longer than
 * than the given distance tolerance.
 * Densified polygonal geometries are guaranteed to be topologically correct.
 * The coordinates created during densification respect the input geometry's
 * scale.
 * <p>
 * <b>Note:</b> At some future point this class will
 * offer a variety of densification strategies.
 *
 * @author Martin Davis
 */
public class Densifier {

  private static GeometryCollection densify(
    final GeometryCollection geometryCollection, final double distanceTolerance) {
    final List<Geometry> geometries = new ArrayList<>();
    for (final Geometry part : geometryCollection.geometries()) {
      final Geometry newGeometry = densify(part, distanceTolerance);
      geometries.add(newGeometry);
    }
    final GeometryCollection newGeometry = geometryCollection.getGeometryFactory()
        .geometryCollection(geometries);
    return newGeometry;
  }

  private static LinearRing densify(final LinearRing line,
    final double distanceTolerance) {
    final List<Point> points = densifyPoints(line, distanceTolerance);
    final GeometryFactory geometryFactory = line.getGeometryFactory();
    return geometryFactory.linearRing(points);
  }

  private static LineString densify(final LineString line,
    final double distanceTolerance) {
    final List<Point> points = densifyPoints(line, distanceTolerance);
    final GeometryFactory geometryFactory = line.getGeometryFactory();
    return geometryFactory.lineString(points);
  }

  private static MultiLineString densify(final MultiLineString multiLineString,
    final double distanceTolerance) {
    final List<LineString> lines = new ArrayList<>();
    for (final LineString line : multiLineString.getLineStrings()) {
      final LineString newLine = densify(line, distanceTolerance);
      lines.add(newLine);
    }
    final GeometryFactory geometryFactory = multiLineString.getGeometryFactory();
    return geometryFactory.multiLineString(lines);
  }

  private static MultiPolygon densify(final MultiPolygon multiPolygon,
    final double distanceTolerance) {
    final List<Polygon> polygons = new ArrayList<>();
    for (final Polygon polygon : multiPolygon.getPolygons()) {
      final Polygon newPolygon = densify(polygon, distanceTolerance);
      polygons.add(newPolygon);
    }
    final GeometryFactory geometryFactory = multiPolygon.getGeometryFactory();
    final MultiPolygon newMultiPolygon = geometryFactory.multiPolygon(polygons);
    return (MultiPolygon)newMultiPolygon.buffer(0);
  }

  private static Polygon densify(final Polygon polygon,
    final double distanceTolerance) {
    // Attempt to fix invalid geometries
    final GeometryFactory geometryFactory = polygon.getGeometryFactory();
    final List<LinearRing> rings = new ArrayList<>();
    for (final LinearRing ring : polygon.rings()) {
      final LinearRing newRing = densify(ring, distanceTolerance);
      rings.add(newRing);
    }
    final Polygon newPolygon = geometryFactory.polygon(rings);
    return (Polygon)newPolygon.buffer(0);
  }

  /**
   * Densifies a geometry using a given distance tolerance,
   * and respecting the input geometry's scale.
   *
   * @param geometry the geometry to densify
   * @param distanceTolerance the distance tolerance to densify
   * @return the densified geometry
   */
  @SuppressWarnings("unchecked")
  public static <V extends Geometry> V densify(final V geometry,
    final double distanceTolerance) {
    if (distanceTolerance <= 0.0) {
      throw new IllegalArgumentException("Tolerance must be positive");
    } else if (geometry == null || geometry.isEmpty()) {
      return geometry;
    } else {
      if (geometry instanceof Point) {
        return geometry;
      } else if (geometry instanceof LinearRing) {
        return (V)densify((LinearRing)geometry, distanceTolerance);
      } else if (geometry instanceof LineString) {
        return (V)densify((LineString)geometry, distanceTolerance);
      } else if (geometry instanceof Polygon) {
        return (V)densify((Polygon)geometry, distanceTolerance);
      } else if (geometry instanceof MultiPoint) {
        return geometry;
      } else if (geometry instanceof MultiLineString) {
        return (V)densify((MultiLineString)geometry, distanceTolerance);
      } else if (geometry instanceof MultiPolygon) {
        return (V)densify((MultiPolygon)geometry, distanceTolerance);
      } else if (geometry instanceof GeometryCollection) {
        return (V)densify((GeometryCollection)geometry, distanceTolerance);
      } else {
        throw new UnsupportedOperationException("Unknown geometry type "
            + geometry.getClass());
      }
    }
  }

  /**
   * Densifies a coordinate sequence.
   *
   * @param pts
   * @param distanceTolerance
   * @return the densified coordinate sequence
   */
  private static List<Point> densifyPoints(final LineString line,
    final double distanceTolerance) {
    final List<Point> points = new ArrayList<Point>();

    for (final Segment segment : line.segments()) {
      if (points.isEmpty()) {
        points.add(segment.getPoint(0).clonePoint());
      }
      final double length = segment.getLength();
      if (length > 0) {
        final int densifiedSegCount = (int)(length / distanceTolerance) + 1;
        if (densifiedSegCount > 1) {
          final double densifiedSegLen = length / densifiedSegCount;
          for (int j = 1; j < densifiedSegCount; j++) {
            final double segFract = j * densifiedSegLen / length;
            final Point point = segment.pointAlong(segFract);
            if (!segment.isEndPoint(point)) {
              points.add(point.clonePoint());
            }
          }
        }
        points.add(segment.getPoint(1).clonePoint());
      }
    }
    return points;
  }

}
