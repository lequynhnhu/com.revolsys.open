package com.revolsys.io.esri.gdb.xml.type;

import com.revolsys.data.types.DataType;
import com.revolsys.io.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.io.xml.XsiConstants;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

public class XmlGeometryFieldType extends AbstractEsriGeodatabaseXmlFieldType {
  public XmlGeometryFieldType(final FieldType esriFieldType,
    final DataType dataType) {
    super(dataType, "xs:" + dataType.getName(), esriFieldType);
  }

  @Override
  public int getFixedLength() {
    return 0;
  }

  @Override
  protected String getType(final Object value) {
    if (value instanceof Point) {
      return POINT_N_TYPE;
    } else if (value instanceof LineString || value instanceof MultiLineString) {
      return POLYLINE_N_TYPE;
    } else if (value instanceof Polygon) {
      return POLYGON_N_TYPE;
    }
    return null;
  }

  private void writeLineString(final XmlWriter out, final LineString line) {
    final boolean hasZ = line.getAxisCount() > 2;
    out.element(HAS_ID, false);
    out.element(HAS_Z, hasZ);
    out.element(HAS_M, false);

    out.startTag(PATH_ARRAY);
    out.attribute(XsiConstants.TYPE, PATH_ARRAY_TYPE);

    writePath(out, line, hasZ);

    out.endTag(PATH_ARRAY);
  }

  private void writeMultiLineString(final XmlWriter out,
    final MultiLineString multiLine) {
    final boolean hasZ;
    if (multiLine.isEmpty()) {
      hasZ = false;
    } else {
      final LineString points = (LineString)multiLine.getGeometry(0);
      hasZ = points.getAxisCount() > 2;
    }
    out.element(HAS_ID, false);
    out.element(HAS_Z, hasZ);
    out.element(HAS_M, false);

    out.startTag(PATH_ARRAY);
    out.attribute(XsiConstants.TYPE, PATH_ARRAY_TYPE);
    for (int i = 0; i < multiLine.getGeometryCount(); i++) {
      final LineString line = (LineString)multiLine.getGeometry(i);
      writePath(out, line, hasZ);
    }
    out.endTag(PATH_ARRAY);
  }

  public void writePath(final XmlWriter out, final LineString line,
    final boolean hasZ) {
    out.startTag(PATH);
    out.attribute(XsiConstants.TYPE, PATH_TYPE);

    writePointArray(out, line, hasZ);

    out.endTag(PATH);
  }

  private void writePoint(final XmlWriter out, final Point point) {
    final boolean hasZ = point.getAxisCount() > 2;
    writePoint(out, point, hasZ);
  }

  public void writePoint(final XmlWriter out, final Point coordinates,
    final boolean hasZ) {
    out.element(X, coordinates.getX());
    out.element(Y, coordinates.getY());
    if (hasZ) {
      out.element(Z, coordinates.getZ());
    }
  }

  public void writePointArray(final XmlWriter out, final LineString line,
    final boolean hasZ) {
    out.startTag(POINT_ARRAY);
    out.attribute(XsiConstants.TYPE, POINT_ARRAY_TYPE);

    for (final Point point : line.vertices()) {
      out.startTag(POINT);
      out.attribute(XsiConstants.TYPE, POINT_N_TYPE);
      writePoint(out, point, hasZ);
      out.endTag(POINT);
    }

    out.endTag(POINT_ARRAY);
  }

  private void writePolygon(final XmlWriter out, final Polygon polygon) {
    final boolean hasZ;
    final LineString exteriorRing = polygon.getExteriorRing();
    final LineString points = exteriorRing;
    hasZ = points.getAxisCount() > 2;
    out.element(HAS_ID, false);
    out.element(HAS_Z, hasZ);
    out.element(HAS_M, false);

    out.startTag(RING_ARRAY);
    out.attribute(XsiConstants.TYPE, RING_ARRAY_TYPE);

    writeRing(out, exteriorRing, hasZ);

    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final LineString interiorRing = polygon.getInteriorRing(i);
      writeRing(out, interiorRing, hasZ);
    }

    out.endTag(RING_ARRAY);
  }

  private void writeRing(final XmlWriter out, final LineString line,
    final boolean hasZ) {
    out.startTag(RING);
    out.attribute(XsiConstants.TYPE, RING_TYPE);

    writePointArray(out, line, hasZ);

    out.endTag(RING);
  }

  @Override
  protected void writeValueText(final XmlWriter out, final Object value) {
    if (value instanceof Point) {
      final Point point = (Point)value;
      writePoint(out, point);
    } else if (value instanceof LineString) {
      final LineString line = (LineString)value;
      writeLineString(out, line);
    } else if (value instanceof Polygon) {
      final Polygon polygon = (Polygon)value;
      writePolygon(out, polygon);
    } else if (value instanceof MultiLineString) {
      final MultiLineString multiLine = (MultiLineString)value;
      writeMultiLineString(out, multiLine);
    }
  }
}
