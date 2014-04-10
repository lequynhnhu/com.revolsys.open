package com.revolsys.gis.model.geometry;

import java.util.List;

import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.revolsys.io.ObjectWithProperties;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.IntersectionMatrix;

public interface Geometry extends Cloneable, ObjectWithProperties {

  Geometry buffer(double distance);

  Geometry buffer(double distance, int quadrantSegments);

  Geometry buffer(double distance, int quadrantSegments, int endCapStyle);

  Object clone();

  <G extends Geometry> G cloneGeometry();

  boolean contains(Geometry geometry);

  boolean coveredBy(Geometry geometry);

  boolean covers(Geometry geometry);

  boolean crosses(Geometry geometry);

  boolean disjoint(Geometry geometry);

  double distance(Geometry g);

  double getArea();

  int getBoundaryDimension();

  BoundingBox getBoundingBox();

  List<CoordinatesList> getCoordinatesLists();

  int getDimension();

  Point getFirstPoint();

  <G extends Geometry> List<G> getGeometries();

  <G extends Geometry> G getGeometry(int i);

  int getGeometryCount();

  <F extends GeometryFactoryI> F getGeometryFactory();

  double getLength();

  byte getNumAxis();

  int getSrid();

  Geometry intersection(Geometry geometry);

  boolean intersects(Geometry geometry);

  boolean isEmpty();

  boolean isValid();

  boolean overlaps(Geometry geometry);

  IntersectionMatrix relate(Geometry geometry);

  boolean relate(Geometry geometry, String intersectionPattern);

  boolean touches(Geometry geometry);

  boolean within(Geometry geometry);
}
