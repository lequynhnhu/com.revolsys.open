package com.revolsys.gis.cs;

import junit.framework.Assert;

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
import com.revolsys.jts.geom.impl.LineStringDouble;

public class GeometryFactoryTest {
  public static void assertCopyGeometry(final Geometry geometry,
    final LineString... pointsList) {
    // assertCoordinatesListEqual(geometry, pointsList);
    final Geometry copy = geometry.copy(GEOMETRY_FACTORY);
    final Class<? extends Geometry> geometryClass = geometry.getClass();
    Assert.assertEquals("Geometry class", geometryClass, copy.getClass());
    Assert.assertEquals("Geometry", geometry, copy);
    // assertCoordinatesListEqual(copy, pointsList);

    final Geometry copy2 = GEOMETRY_FACTORY.geometry(geometryClass, geometry);
    Assert.assertEquals("Geometry class", geometryClass, copy2.getClass());
    Assert.assertEquals("Geometry", geometry, copy2);
    // assertCoordinatesListEqual(copy2, pointsList);
    assertCreateGeometryCollection(geometry, pointsList);
  }

  // public static void assertCoordinatesListEqual(final Geometry geometry,
  // final LineString... pointsList) {
  // System.out.println(geometry);
  // final List<LineString> geometryPointsList = getAll(geometry);
  // Assert.assertEquals("Number of coordinates Lists", pointsList.length,
  // geometryPointsList.size());
  // for (int i = 0; i < pointsList.length; i++) {
  // final LineString points = pointsList[i];
  // final LineString geometryPoints = geometryPointsList.get(i);
  // Assert.assertEquals("Coordinates not equal", points, geometryPoints);
  // }
  // }

  public static void assertCreateGeometryCollection(final Geometry geometry,
    final LineString... pointsList) {
    if (geometry instanceof GeometryCollection) {
      if (geometry.getGeometryCount() == 1) {
        final Geometry part = geometry.getGeometry(0);
        final Class<? extends Geometry> geometryClass = geometry.getClass();

        final Geometry copy2 = GEOMETRY_FACTORY.geometry(geometryClass, part);
        Assert.assertEquals("Geometry class", geometryClass, copy2.getClass());
        Assert.assertEquals("Geometry", geometry, copy2);
        // assertCoordinatesListEqual(copy2, pointsList);
      }
    } else if (!(geometry instanceof LinearRing)) {
      final GeometryCollection collection = GEOMETRY_FACTORY.geometryCollection(geometry);
      final Geometry copy = collection.getGeometry(0);
      final Class<? extends Geometry> geometryClass = geometry.getClass();
      Assert.assertEquals("Geometry class", geometryClass, copy.getClass());
      Assert.assertEquals("Geometry", geometry, copy);
      // assertCoordinatesListEqual(collection, pointsList);

      final Geometry copy2 = GEOMETRY_FACTORY.geometry(geometryClass,
        collection);
      Assert.assertEquals("Geometry class", geometryClass, copy2.getClass());
      Assert.assertEquals("Geometry", geometry, copy2);
      // assertCoordinatesListEqual(copy2, pointsList);
    }

  }

  public static void main(final String[] args) {
    testCreateGeometry();
  }

  private static void testCreateGeometry() {
    final LineString pointPoints = new LineStringDouble(2, 0.0, 0);
    final LineString point2Points = new LineStringDouble(2, 20.0, 20);
    final LineString ringPoints = new LineStringDouble(2, 0.0, 0, 0, 100, 100,
      100, 100, 0, 0, 0);
    final LineString ring2Points = new LineStringDouble(2, 20.0, 20, 20, 80,
      80, 80, 80, 20, 20, 20);
    final LineString ring3Points = new LineStringDouble(2, 120.0, 120, 120,
      180, 180, 180, 180, 120, 120, 120);

    final Point point = GEOMETRY_FACTORY.point(pointPoints);
    assertCopyGeometry(point, pointPoints);

    final LineString line = GEOMETRY_FACTORY.lineString(ringPoints);
    assertCopyGeometry(line, ringPoints);

    final LinearRing linearRing = GEOMETRY_FACTORY.linearRing(ringPoints);
    assertCopyGeometry(linearRing, ringPoints);

    final Polygon polygon = GEOMETRY_FACTORY.polygon(ringPoints);
    assertCopyGeometry(polygon, ringPoints);

    final Polygon polygon2 = GEOMETRY_FACTORY.polygon(ringPoints, ring2Points);
    assertCopyGeometry(polygon2, ringPoints, ring2Points);

    final MultiPoint multiPoint = GEOMETRY_FACTORY.multiPoint(pointPoints);
    assertCopyGeometry(multiPoint, pointPoints);

    final MultiPoint multiPoint2 = GEOMETRY_FACTORY.multiPoint(pointPoints,
      point2Points);
    assertCopyGeometry(multiPoint2, pointPoints, point2Points);

    final MultiLineString multiLineString = GEOMETRY_FACTORY.multiLineString(ringPoints);
    assertCopyGeometry(multiLineString, ringPoints);

    final MultiLineString multiLineString2 = GEOMETRY_FACTORY.multiLineString(
      ringPoints, ring2Points);
    assertCopyGeometry(multiLineString2, ringPoints, ring2Points);

    final MultiPolygon multiPolygon = GEOMETRY_FACTORY.multiPolygon(ringPoints);
    assertCopyGeometry(multiPolygon, ringPoints);

    final MultiPolygon multiPolygon2 = GEOMETRY_FACTORY.multiPolygon(
      ringPoints, ring3Points);
    assertCopyGeometry(multiPolygon2, ringPoints, ring3Points);

  }

  private static GeometryFactory GEOMETRY_FACTORY = GeometryFactory.fixed(3857,
    1.0);
}
