package com.revolsys.gis.cs;

import java.io.Serializable;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import com.revolsys.gis.cs.projection.CoordinatesProjection;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;

public interface CoordinateSystem extends Serializable {
  CoordinateSystem clone();

  Area getArea();

  BoundingBox getAreaBoundingBox();

  Authority getAuthority();

  List<Axis> getAxis();

  CoordinatesProjection getCoordinatesProjection();

  GeometryFactory getGeometryFactory();

  int getId();

  Unit<Length> getLengthUnit();

  String getName();

  <Q extends Quantity> Unit<Q> getUnit();

  boolean isDeprecated();
}
