package com.revolsys.io.geojson;

import java.io.BufferedWriter;
import java.io.Writer;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.io.json.JsonWriter;
import com.revolsys.util.Property;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeoJsonDataObjectWriter extends AbstractWriter<DataObject>
implements GeoJsonConstants {

  boolean initialized = false;

  private int srid = -1;

  /** The writer */
  private JsonWriter out;

  private boolean singleObject;

  private boolean writeNulls;

  public GeoJsonDataObjectWriter(final Writer out) {
    this.out = new JsonWriter(new BufferedWriter(out));
    this.out.setIndent(true);
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    if (this.out != null) {
      try {
        writeFooter();
      } finally {
        this.out.close();
        this.out = null;
      }
    }
  }

  private void coordinate(final CoordinatesList coordinates, final int i) {
    final double x = coordinates.getX(i);
    this.out.print('[');
    this.out.value(x);

    final double y = coordinates.getY(i);
    this.out.print(',');
    this.out.value(y);

    final double z = coordinates.getZ(i);
    if (!Double.isNaN(z)) {
      this.out.print(',');
      this.out.value(z);
    }
    this.out.print(']');
  }

  private void coordinates(final CoordinatesList coordinates) {
    this.out.startList();
    this.out.indent();
    coordinate(coordinates, 0);
    for (int i = 1; i < coordinates.size(); i++) {
      this.out.endAttribute();
      this.out.indent();
      coordinate(coordinates, i);
    }
    this.out.endList();
  }

  private void coordinates(final LineString line) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(line);
    coordinates(coordinates);
  }

  public void coordinates(final Point point) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(point);
    coordinate(coordinates, 0);
  }

  public void coordinates(final Polygon polygon) {
    this.out.startList();
    this.out.indent();

    final LineString exteriorRing = polygon.getExteriorRing();
    coordinates(exteriorRing);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final LineString interiorRing = polygon.getInteriorRingN(i);
      this.out.endAttribute();
      coordinates(interiorRing);
    }

    this.out.endList();
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  private void geometry(final Geometry geometry) {
    this.out.startObject();
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      point(point);
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      line(line);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      polygon(polygon);
    } else if (geometry instanceof MultiPoint) {
      final MultiPoint multiPoint = (MultiPoint)geometry;
      multiPoint(multiPoint);
    } else if (geometry instanceof MultiLineString) {
      final MultiLineString multiLine = (MultiLineString)geometry;
      multiLineString(multiLine);
    } else if (geometry instanceof MultiPolygon) {
      final MultiPolygon multiPolygon = (MultiPolygon)geometry;
      multiPolygon(multiPolygon);
    }
    this.out.endObject();
  }

  public boolean isWriteNulls() {
    return this.writeNulls;
  }

  private void line(final LineString line) {
    type(LINE_STRING);
    this.out.endAttribute();
    this.out.label(COORDINATES);
    coordinates(line);
  }

  private void multiLineString(final MultiLineString multiLineString) {
    type(MULTI_LINE_STRING);

    this.out.endAttribute();
    this.out.label(COORDINATES);
    this.out.startList();
    this.out.indent();
    final int numGeometries = multiLineString.getNumGeometries();
    if (numGeometries > 0) {
      coordinates((LineString)multiLineString.getGeometryN(0));
      for (int i = 1; i < numGeometries; i++) {
        final LineString lineString = (LineString)multiLineString.getGeometryN(i);
        this.out.endAttribute();
        coordinates(lineString);
      }
    }
    this.out.endList();
  }

  private void multiPoint(final MultiPoint multiPoint) {
    type(MULTI_POINT);

    this.out.endAttribute();
    this.out.label(COORDINATES);
    this.out.startList();
    this.out.indent();
    final int numGeometries = multiPoint.getNumGeometries();
    if (numGeometries > 0) {
      coordinates((Point)multiPoint.getGeometryN(0));
      for (int i = 1; i < numGeometries; i++) {
        final Point point = (Point)multiPoint.getGeometryN(i);
        this.out.endAttribute();
        coordinates(point);
      }
    }
    this.out.endList();
  }

  private void multiPolygon(final MultiPolygon multiPolygon) {
    type(MULTI_POLYGON);

    this.out.endAttribute();
    this.out.label(COORDINATES);
    this.out.startList();
    this.out.indent();
    final int numGeometries = multiPolygon.getNumGeometries();
    if (numGeometries > 0) {
      coordinates((Polygon)multiPolygon.getGeometryN(0));
      for (int i = 1; i < numGeometries; i++) {
        final Polygon polygon = (Polygon)multiPolygon.getGeometryN(i);
        this.out.endAttribute();
        coordinates(polygon);
      }
    }
    this.out.endList();
  }

  private void point(final Point point) {
    type(POINT);
    this.out.endAttribute();
    this.out.label(COORDINATES);
    coordinates(point);
  }

  private void polygon(final Polygon polygon) {
    type(POLYGON);

    this.out.endAttribute();
    this.out.label(COORDINATES);
    coordinates(polygon);
  }

  @Override
  public void setProperty(final String name, final Object value) {
    super.setProperty(name, value);
    if (name.equals(IoConstants.INDENT_PROPERTY)) {
      this.out.setIndent((Boolean)value);
    } else if (IoConstants.WRITE_NULLS_PROPERTY.equals(name)) {
      this.writeNulls = BooleanStringConverter.isTrue(value);
    }
  }

  private void srid(final int srid) {
    final String urn = URN_OGC_DEF_CRS_EPSG + srid;
    this.out.label(CRS);
    this.out.startObject();
    type(NAME);
    this.out.endAttribute();
    this.out.label(PROPERTIES);
    this.out.startObject();
    this.out.label(NAME);
    this.out.value(urn);
    this.out.endObject();
    this.out.endObject();
  }

  private void type(final String type) {
    this.out.label(TYPE);
    this.out.value(type);
  }

  @Override
  public void write(final DataObject object) {
    if (this.initialized) {
      this.out.endAttribute();
    } else {
      writeHeader();
      this.initialized = true;
    }
    this.out.startObject();
    type(FEATURE);
    final Geometry mainGeometry = object.getGeometryValue();
    writeSrid(mainGeometry);
    final DataObjectMetaData metaData = object.getMetaData();
    final int geometryIndex = metaData.getGeometryAttributeIndex();
    boolean geometryWritten = false;
    this.out.endAttribute();
    this.out.label(GEOMETRY);
    if (mainGeometry != null) {
      geometryWritten = true;
      geometry(mainGeometry);
    }
    if (!geometryWritten) {
      this.out.value(null);
    }
    this.out.endAttribute();
    this.out.label(PROPERTIES);
    this.out.startObject();
    final int numAttributes = metaData.getAttributeCount();
    boolean hasValue = false;
    for (int i = 0; i < numAttributes; i++) {
      if (i != geometryIndex) {
        final Object value = object.getValue(i);
        if (Property.hasValue(value) || this.writeNulls) {
          if (hasValue) {
            this.out.endAttribute();
          } else {
            hasValue = true;
          }
          final String name = metaData.getAttributeName(i);
          this.out.label(name);
          if (value instanceof Geometry) {
            final Geometry geometry = (Geometry)value;
            geometry(geometry);
          } else {
            this.out.value(value);
          }
        }
      }
    }
    this.out.endObject();
    this.out.endObject();
  }

  private void writeFooter() {
    if (!this.singleObject) {
      this.out.endList();
      this.out.endObject();
    }
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      this.out.print(");");
    }
  }

  private void writeHeader() {
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      this.out.print(callback);
      this.out.print('(');
    }
    this.singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (!this.singleObject) {
      this.out.startObject();
      type(FEATURE_COLLECTION);
      this.srid = writeSrid();
      this.out.endAttribute();
      this.out.label(FEATURES);
      this.out.startList();
    }
  }

  private int writeSrid() {
    final GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    return writeSrid(geometryFactory);
  }

  private void writeSrid(final Geometry geometry) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
      writeSrid(geometryFactory);
    }
  }

  protected int writeSrid(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      final int srid = geometryFactory.getSRID();
      if (srid != 0 && srid != this.srid) {
        this.out.endAttribute();
        srid(srid);
        return srid;
      }
    }
    return -1;
  }
}
