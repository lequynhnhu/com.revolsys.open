package com.revolsys.io.kml;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.io.StringBuilderWriter;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;

public class KmlXmlWriter extends XmlWriter implements Kml22Constants {
  public static void append(final StringBuilder buffer, final Geometry geometry) {
    final KmlXmlWriter writer = new KmlXmlWriter(
      new StringBuilderWriter(buffer), false);

    writer.writeGeometry(geometry, 2);
    writer.close();
  }

  public static long getLookAtRange(final BoundingBox boundingBox) {
    if (boundingBox.isEmpty()) {
      return 1000;
    } else {
      final double minX = boundingBox.getMinX();
      final double maxX = boundingBox.getMaxX();
      final double centreX = boundingBox.getCentreX();

      final double minY = boundingBox.getMinY();
      final double maxY = boundingBox.getMaxY();
      final double centreY = boundingBox.getCentreY();

      double maxMetres = 0;

      for (final double y : new double[] {
        minY, centreY, maxY
      }) {
        final double widthMetres = GeographicCoordinateSystem.distanceMetres(
          minX, y, maxX, y);
        if (widthMetres > maxMetres) {
          maxMetres = widthMetres;
        }
      }
      for (final double x : new double[] {
        minX, centreX, maxX
      }) {
        final double heightMetres = GeographicCoordinateSystem.distanceMetres(
          x, minY, x, maxY);
        if (heightMetres > maxMetres) {
          maxMetres = heightMetres;
        }
      }
      if (maxMetres == 0) {
        return 1000;
      } else {
        final double lookAtScale = 1.2;
        final double lookAtRange = maxMetres / 2 / Math.tan(Math.toRadians(25))
            * lookAtScale;
        return (long)Math.ceil(lookAtRange);
      }
    }
  }

  public KmlXmlWriter(final OutputStream out) {
    super(out);
  }

  public KmlXmlWriter(final OutputStream out, final boolean useNamespaces) {
    super(out, useNamespaces);
  }

  public KmlXmlWriter(final Writer out) {
    super(out);
  }

  public KmlXmlWriter(final Writer out, final boolean useNamespaces) {
    super(out, useNamespaces);
  }

  public void write(final LineString points) {
    startTag(Kml22Constants.COORDINATES);
    final int vertexCount = points.getVertexCount();
    final int axisCount = points.getAxisCount();
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      if (vertexIndex > 0) {
        write(' ');
      }
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          write(',');
        }
        write(String.valueOf(points.getCoordinate(vertexIndex, axisIndex)));
      }
    }
    endTag();
  }

  public void writeCoordinates(final Point point) {
    startTag(Kml22Constants.COORDINATES);
    if (point != null && !point.isEmpty()) {
      for (int axisIndex = 0; axisIndex < point.getAxisCount(); axisIndex++) {
        if (axisIndex > 0) {
          write(',');
        }
        write(String.valueOf(point.getCoordinate(axisIndex)));
      }
    }
    endTag(Kml22Constants.COORDINATES);
  }

  public void writeData(final String name, final Object value) {
    if (value != null) {
      startTag(DATA);
      attribute(NAME, name);
      element(VALUE, value);
      endTag();
    }
  }

  public void writeExtendedData(final Map<String, ? extends Object> data) {
    boolean hasValues = false;
    for (final Entry<String, ? extends Object> entry : data.entrySet()) {
      final String fieldName = entry.getKey();
      final Object value = entry.getValue();
      if (!(value instanceof Geometry)) {
        if (value != null) {
          final String stringValue = value.toString();
          if (Property.hasValue(stringValue)) {
            if (!hasValues) {
              hasValues = true;
              startTag(EXTENDED_DATA);
            }
            startTag(DATA);
            attribute(NAME, fieldName);
            element(VALUE, value);
            endTag(DATA);
          }
        }
      }
    }
    if (hasValues) {
      endTag(EXTENDED_DATA);
    }
  }

  public void writeGeometry(final Geometry geometry, final int axisCount) {
    if (geometry != null) {
      final int numGeometries = geometry.getGeometryCount();
      if (numGeometries > 1) {
        startTag(Kml22Constants.MULTI_GEOMETRY);
        for (int i = 0; i < numGeometries; i++) {
          writeGeometry(geometry.getGeometry(i), axisCount);
        }
        endTag();
      } else {
        final Geometry geoGraphicsGeom = geometry.convert(GeometryFactory.floating(
          Kml22Constants.COORDINATE_SYSTEM_ID, axisCount));
        if (geoGraphicsGeom instanceof Point) {
          final Point point = (Point)geoGraphicsGeom;
          writePoint(point);
        } else if (geoGraphicsGeom instanceof LinearRing) {
          final LinearRing line = (LinearRing)geoGraphicsGeom;
          writeLinearRing(line);
        } else if (geoGraphicsGeom instanceof LineString) {
          final LineString line = (LineString)geoGraphicsGeom;
          writeLineString(line);
        } else if (geoGraphicsGeom instanceof Polygon) {
          final Polygon polygon = (Polygon)geoGraphicsGeom;
          writePolygon(polygon);
        } else if (geoGraphicsGeom instanceof GeometryCollection) {
          final GeometryCollection collection = (GeometryCollection)geoGraphicsGeom;
          writeMultiGeometry(collection, axisCount);
        }
      }
    }
  }

  public void writeLatLonBox(final com.revolsys.jts.geom.BoundingBox envelope) {
    startTag(LAT_LON_BOX);
    element(NORTH, envelope.getMaxY());
    element(SOUTH, envelope.getMinY());
    element(WEST, envelope.getMinX());
    element(EAST, envelope.getMaxX());
    endTag();

  }

  public void writeLinearRing(final LineString ring) {
    startTag(Kml22Constants.LINEAR_RING);
    final LineString coordinateSequence = ring;
    write(coordinateSequence);
    endTag();

  }

  public void writeLineString(final LineString line) {
    startTag(Kml22Constants.LINE_STRING);
    final LineString coordinateSequence = line;
    write(coordinateSequence);
    endTag();
  }

  public void writeMultiGeometry(final GeometryCollection collection,
    final int axisCount) {
    startTag(Kml22Constants.MULTI_GEOMETRY);
    for (int i = 0; i < collection.getGeometryCount(); i++) {
      final Geometry geometry = collection.getGeometry(i);
      writeGeometry(geometry, axisCount);
    }
    endTag(Kml22Constants.MULTI_GEOMETRY);

  }

  public void writeNetworkLink(
    final com.revolsys.jts.geom.BoundingBox envelope, final String name,
    final Integer minLod, final Integer maxLod, final String href) {

    startTag(NETWORK_LINK);
    if (name != null) {
      element(NAME, name);
    }
    writeRegion(envelope, minLod, maxLod);
    startTag(LINK);
    element(HREF, href);
    element(VIEW_REFRESH_MODE, "onRegion");

    endTag();
    endTag();

  }

  public void writePlacemark(final Geometry geometry, final String name,
    final String styleUrl) {
    startTag(PLACEMARK);
    if (name != null) {
      element(NAME, name);
    }
    if (styleUrl != null) {
      element(STYLE_URL, styleUrl);
    }
    writeGeometry(geometry, 2);

    endTag();
  }

  public void writePlacemarkLineString(
    final com.revolsys.jts.geom.BoundingBox envelope, final String name,
    final String styleUrl) {

    startTag(PLACEMARK);
    if (name != null) {
      element(NAME, name);
    }
    if (styleUrl != null) {
      element(STYLE_URL, styleUrl);
    }
    startTag(LINE_STRING);
    startTag(COORDINATES);
    final double maxY = envelope.getMaxY();
    final double minY = envelope.getMinY();
    final double maxX = envelope.getMaxX();
    final double minX = envelope.getMinX();
    write(Double.toString(minX));
    write(',');
    write(Double.toString(minY));
    write(' ');
    write(Double.toString(maxX));
    write(',');
    write(Double.toString(minY));
    write(' ');
    write(Double.toString(maxX));
    write(',');
    write(Double.toString(maxY));
    write(' ');
    write(Double.toString(minX));
    write(',');
    write(Double.toString(maxY));
    write(' ');
    write(Double.toString(minX));
    write(',');
    write(Double.toString(minY));

    endTag();
    endTag();
    endTag();

  }

  public void writePlacemarkLineString(final LineString lineString,
    final String name, final String styleUrl) {

    startTag(PLACEMARK);
    if (name != null) {
      element(NAME, name);
    }
    if (styleUrl != null) {
      element(STYLE_URL, styleUrl);
    }
    writeLineString(lineString);

    endTag();

  }

  public void writePlacemarkLineString(final Polygon polygon,
    final String name, final String styleUrl) {

    startTag(PLACEMARK);
    if (name != null) {
      element(NAME, name);
    }
    if (styleUrl != null) {
      element(STYLE_URL, styleUrl);
    }
    final LineString exteriorRing = polygon.getExteriorRing();
    writeLineString(exteriorRing);

    endTag();

  }

  public void writePlacemarkPoint(final BoundingBox boundingBox,
    final String name, final String styleUrl) {

    startTag(PLACEMARK);
    if (name != null) {
      element(NAME, name);
    }
    if (styleUrl != null) {
      element(STYLE_URL, styleUrl);
    }
    startTag(POINT);
    startTag(COORDINATES);
    final double x = boundingBox.getCentreX();
    final double y = boundingBox.getCentreY();
    write(MathUtil.toString(x));
    write(',');
    write(MathUtil.toString(y));

    endTag();
    endTag();
    endTag();
  }

  public void writePlacemarkPolygon(final Polygon polygon, final String name,
    final String styleUrl) {

    startTag(PLACEMARK);
    if (name != null) {
      element(NAME, name);
    }
    if (styleUrl != null) {
      element(STYLE_URL, styleUrl);
    }
    writePolygon(polygon);

    endTag();

  }

  public void writePoint(final Point point) {
    startTag(Kml22Constants.POINT);
    writeCoordinates(point);
    endTag(Kml22Constants.POINT);
  }

  public void writePolygon(final Polygon polygon) {
    startTag(Kml22Constants.POLYGON);
    if (!polygon.isEmpty()) {
      startTag(Kml22Constants.OUTER_BOUNDARY_IS);
      writeLinearRing(polygon.getExteriorRing());
      endTag();
      for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
        startTag(Kml22Constants.INNER_BOUNDARY_IS);
        final LineString ring = polygon.getInteriorRing(i);
        writeLinearRing(ring);
        endTag();
      }
    }
    endTag();
  }

  public void writeRegion(final com.revolsys.jts.geom.BoundingBox envelope,
    final Integer minLod, final Integer maxLod) {
    startTag(REGION);

    startTag(LAT_LON_ALT_BOX);
    element(NORTH, envelope.getMaxY());
    element(SOUTH, envelope.getMinY());
    element(EAST, envelope.getMaxX());
    element(WEST, envelope.getMinX());
    endTag();
    if (minLod != null || maxLod != null) {
      startTag(LOD);
      if (minLod != null) {
        element(MIN_LOD_PIXELS, minLod);
        element(MAX_LOD_PIXELS, maxLod);
      }
      endTag();
    }

    endTag();
  }

  public void writeWmsGroundOverlay(
    final com.revolsys.jts.geom.BoundingBox envelope, final String baseUrl,
    final String name) {

    startTag(GROUND_OVERLAY);
    if (name != null) {
      element(NAME, name);
    }
    writeLatLonBox(envelope);
    startTag(ICON);
    final Map<String, String> parameters = Collections.singletonMap("BBOX",
      envelope.getMinX() + "," + envelope.getMinY() + "," + envelope.getMaxX()
      + "," + envelope.getMaxY());
    element(HREF, UrlUtil.getUrl(baseUrl, parameters));

    endTag();
    endTag();

  }

}
