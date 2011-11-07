package com.revolsys.gis.format.saif.io.util;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class PointConverter implements OsnConverter {
  private String geometryClass = "Point";

  private final GeometryFactory geometryFactory;

  private final CoordinatesPrecisionModel precisionModel;

  public PointConverter(
    final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    precisionModel = geometryFactory.getCoordinatesPrecisionModel();
  }

  public PointConverter(
    final GeometryFactory geometryFactory,
    final String geometryClass) {
    this.geometryFactory = geometryFactory;
    precisionModel = geometryFactory.getCoordinatesPrecisionModel();
    this.geometryClass = geometryClass;
  }

  public Object read(
    final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<String, Object>();
    values.put("type", geometryClass);
    Geometry geometry = null;

    String attributeName = iterator.nextAttributeName();
    while (attributeName != null) {
      if (attributeName.equals("coords")) {
        Coordinates coordinate = null;
        final QName coordTypeName = iterator.nextObjectName();
        if (coordTypeName.equals(new QName("Coord3D"))) {
          final double x = iterator.nextDoubleAttribute("c1");
          final double y = iterator.nextDoubleAttribute("c2");
          double z = iterator.nextDoubleAttribute("c3");
          if (z == 2147483648.0) {
            z = 0;
          }
          coordinate = new DoubleCoordinates(x, y, z);
        } else if (coordTypeName.equals(new QName("Coord2D"))) {
          final double x = iterator.nextDoubleAttribute("c1");
          final double y = iterator.nextDoubleAttribute("c2");
          coordinate = new DoubleCoordinates(x, y);
        } else {
          iterator.throwParseError("Expecting Coord2D or Coord3D");
        }
        iterator.nextEndObject();
        geometry = geometryFactory.createPoint(coordinate);
      } else {
        readAttribute(iterator, attributeName, values);
      }
      attributeName = iterator.nextAttributeName();
    }
    if (!values.isEmpty()) {
      geometry.setUserData(values);
    }
    return geometry;
  }

  protected void readAttribute(
    final OsnIterator iterator,
    final String attributeName,
    final Map<String, Object> values) {
    iterator.next();
    values.put(attributeName, iterator.getValue());
  }

  public void write(
    final OsnSerializer serializer,
    final Object object)
    throws IOException {
    if (object instanceof Point) {
      final Point point = (Point)object;
      final CoordinatesList points = CoordinatesListUtil.get(point);

      int numAxis = points.getNumAxis();
      final double x = points.getX(0);
      final double y = points.getY(0);
      final double z = points.getZ(0);
      serializer.startObject(geometryClass);
      serializer.attributeName("coords");
      if (numAxis == 2) {
        serializer.startObject("Coord2D");
        serializer.attribute("c1", x, true);
        serializer.attribute("c2", y, false);
      } else {
        serializer.startObject("Coord3D");
        serializer.attribute("c1", x, true);
        serializer.attribute("c2", y, true);
        if (Double.isNaN(z)) {
          serializer.attribute("c3", 0, false);
        } else {
          serializer.attribute("c3", z, false);
        }
      }
      serializer.endObject();
      serializer.endAttribute();

      final Map<String, Object> values = JtsGeometryUtil.getGeometryProperties(point);
      writeAttributes(serializer, values);
      serializer.endObject();
    }
  }

  protected void writeAttribute(
    final OsnSerializer serializer,
    final Map<String, Object> values,
    final String name)
    throws IOException {
    final Object value = values.get(name);
    if (value != null) {
      serializer.endLine();
      serializer.attribute(name, value, false);
    }

  }

  protected void writeAttributes(
    final OsnSerializer serializer,
    final Map<String, Object> values)
    throws IOException {
    writeEnumAttribute(serializer, values, "qualifier");
  }

  protected void writeEnumAttribute(
    final OsnSerializer serializer,
    final Map<String, Object> values,
    final String name)
    throws IOException {
    final String value = (String)values.get(name);
    if (value != null) {
      serializer.endLine();
      serializer.attributeEnum(name, value, false);
    }
  }

}