package com.revolsys.gis.wkt;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class WktParser {

  private final GeometryFactory[] geometryFactories = new GeometryFactory[5];

  public WktParser(final GeometryFactory geometryFactory) {
    for (int i = 2; i < 5; i++) {
      geometryFactories[i] = new GeometryFactory(geometryFactory, i);
    }

  }

  private int getNumAxis(final StringBuffer text) {
    skipWhitespace(text);
    final char c = text.charAt(0);
    switch (c) {
      case '(':
      case 'E':
        return 2;
      case 'M':
        text.delete(0, 1);
        return 4;
      case 'Z':
        if (text.charAt(1) == 'M') {
          text.delete(0, 2);
          return 4;
        } else {
          text.delete(0, 1);
          return 3;
        }
      default:
        throw new IllegalArgumentException(
          "Expecting Z, M, ZM, (, or EMPTY not: " + text);
    }
  }

  private boolean hasText(final StringBuffer text, final String expected) {
    skipWhitespace(text);
    final int length = expected.length();
    final CharSequence subText = text.subSequence(0, length);
    if (subText.equals(expected)) {
      text.delete(0, length);
      return true;
    } else {
      return false;
    }
  }

  private boolean isEmpty(final StringBuffer text) {
    if (hasText(text, "EMPTY")) {
      skipWhitespace(text);
      if (text.length() > 0) {
        throw new IllegalArgumentException(
          "Unexpected text at the end of an empty geometry: " + text);
      }
      return true;
    } else {
      return false;
    }
  }

  private CoordinatesList parseCoordinates(final StringBuffer text,
    final int numAxis) {
    char c = text.charAt(0);
    if (c == '(') {
      text.delete(0, 1);
      final List<Double> coordinates = new ArrayList<Double>();
      int i = 0;
      boolean finished = false;
      while (!finished) {
        final Double number = parseDouble(text);
        c = text.charAt(0);
        if (number == null) {
          if (c == ')') {
            finished = true;
          } else {
            throw new IllegalArgumentException(
              "Expecting end of coordinates ')' not" + text);
          }
        } else if (c == ',' || c == ')') {
          i++;
          final int coordinateCount = i % numAxis;
          if (coordinateCount == 0) {
            coordinates.add(number);
          } else {
            throw new IllegalArgumentException(
              "Not enough coordinates, vertex must have " + numAxis
                + " coordinates not " + coordinateCount);
          }
          if (c == ')') {
            finished = true;
          } else {
            text.delete(0, 1);
          }
        } else {
          i++;
          final int coordinateCount = i % numAxis;
          if (coordinateCount == 0) {
            throw new IllegalArgumentException(
              "Too many coordinates, vertex must have " + numAxis
                + " coordinates not " + coordinateCount);
          } else {
            coordinates.add(number);
          }
        }
      }
      text.delete(0, 1);
      return new DoubleCoordinatesList(numAxis, coordinates);
    } else {
      throw new IllegalArgumentException(
        "Expecting start of coordinates '(' not: " + text);
    }
  }

  private Double parseDouble(final StringBuffer text) {
    skipWhitespace(text);
    int i = 0;
    for (; i < text.length(); i++) {
      final char c = text.charAt(i);
      if (Character.isWhitespace(c) || c == ',' || c == ')') {
        break;
      }
    }
    final String numberText = text.substring(0, i);
    text.delete(0, i);
    if (numberText.length() == 0) {
      return null;
    } else {
      return new Double(numberText);
    }

  }

  public Geometry parseGeometry(final String value) {
    if (StringUtils.hasLength(value)) {
      final StringBuffer text = new StringBuffer(value);

      if (hasText(text, "POINT")) {
        return parsePoint(text);
      } else if (hasText(text, "LINESTRING")) {
        return parseLineString(text);
      } else if (hasText(text, "POLYGON")) {
        return parsePolygon(text);
      } else if (hasText(text, "MULTIPOINT")) {
        return parseMultiPoint(text);
      } else if (hasText(text, "MULTILINESTRING")) {
        return parseMultiLineString(text);
      } else if (hasText(text, "MULTIPOLYGON")) {
        return parseMultiPolygon(text);
      } else {
        throw new IllegalArgumentException("Unknown geometry type " + text);
      }
    } else {
      return null;
    }
  }

  private LineString parseLineString(final StringBuffer text) {
    final int numAxis = getNumAxis(text);

    final CoordinatesList points;
    if (isEmpty(text)) {
      points = new DoubleCoordinatesList(0, numAxis);
    } else {
      points = parseCoordinates(text, numAxis);
    }
    final GeometryFactory geometryFactory = geometryFactories[numAxis];
    return geometryFactory.createLineString(points);
  }

  private MultiLineString parseMultiLineString(final StringBuffer text) {
    final int numAxis = getNumAxis(text);

    final List<CoordinatesList> lines;
    if (isEmpty(text)) {
      lines = new ArrayList<CoordinatesList>();
    } else {
      lines = parseParts(text, numAxis);
    }
    final GeometryFactory geometryFactory = geometryFactories[numAxis];
    return geometryFactory.createMultiLineString(lines);
  }

  private MultiPoint parseMultiPoint(final StringBuffer text) {
    final int numAxis = getNumAxis(text);

    final List<CoordinatesList> pointsList;
    if (isEmpty(text)) {
      pointsList = new ArrayList<CoordinatesList>();
    } else {
      pointsList = parseParts(text, numAxis);
    }
    final GeometryFactory geometryFactory = geometryFactories[numAxis];
    return geometryFactory.createMultiPoint(pointsList);
  }

  private MultiPolygon parseMultiPolygon(final StringBuffer text) {
    final int numAxis = getNumAxis(text);

    final List<List<CoordinatesList>> polygons;
    if (isEmpty(text)) {
      polygons = new ArrayList<List<CoordinatesList>>();
    } else {
      polygons = parsePartsList(text, numAxis);
    }
    final GeometryFactory geometryFactory = geometryFactories[numAxis];
    return geometryFactory.createMultiPolygon(polygons);
  }

  private List<CoordinatesList> parseParts(final StringBuffer text,
    final int numAxis) {
    final List<CoordinatesList> parts = new ArrayList<CoordinatesList>();
    final char firstChar = text.charAt(0);
    switch (firstChar) {
      case '(':
        do {
          text.delete(0, 1);
          final CoordinatesList coordinates = parseCoordinates(text, numAxis);
          parts.add(coordinates);
        } while (text.charAt(0) == ',');
        if (text.charAt(0) == ')') {
          text.delete(0, 1);
        } else {
          throw new IllegalArgumentException("Expecting ) not" + text);
        }
      break;
      case ')':
        text.delete(0, 2);
      break;

      default:
        throw new IllegalArgumentException("Expecting ( not" + text);
    }
    return parts;
  }

  private List<List<CoordinatesList>> parsePartsList(final StringBuffer text,
    final int numAxis) {
    final List<List<CoordinatesList>> partsList = new ArrayList<List<CoordinatesList>>();
    final char firstChar = text.charAt(0);
    switch (firstChar) {
      case '(':
        do {
          text.delete(0, 1);
          final List<CoordinatesList> parts = parseParts(text, numAxis);
          partsList.add(parts);
        } while (text.charAt(0) == ',');
        if (text.charAt(0) == ')') {
          text.delete(0, 1);
        } else {
          throw new IllegalArgumentException("Expecting ) not" + text);
        }
      break;
      case ')':
        text.delete(0, 2);
      break;

      default:
        throw new IllegalArgumentException("Expecting ( not" + text);
    }
    return partsList;
  }

  private Point parsePoint(final StringBuffer text) {
    final int numAxis = getNumAxis(text);
    final CoordinatesList points;
    if (isEmpty(text)) {
      points = new DoubleCoordinatesList(0, numAxis);
    } else {
      points = parseCoordinates(text, numAxis);
      if (points.size() > 1) {
        throw new IllegalArgumentException("Points may only have 1 vertex");
      }
    }
    final GeometryFactory geometryFactory = geometryFactories[numAxis];
    return geometryFactory.createPoint(points);
  }

  private Polygon parsePolygon(final StringBuffer text) {
    final int numAxis = getNumAxis(text);

    final List<CoordinatesList> parts;
    if (isEmpty(text)) {
      parts = new ArrayList<CoordinatesList>();
    } else {
      parts = parseParts(text, numAxis);
    }
    final GeometryFactory geometryFactory = geometryFactories[numAxis];
    return geometryFactory.createPolygon(parts);
  }

  private void skipWhitespace(final StringBuffer text) {
    for (int i = 0; i < text.length(); i++) {
      final char c = text.charAt(i);
      if (!Character.isWhitespace(c)) {
        if (i > 0) {
          text.delete(0, i);
        }
        return;
      }
    }
  }

}