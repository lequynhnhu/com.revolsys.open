package com.revolsys.gis.cs.projection;

import com.revolsys.gis.model.coordinates.Coordinates;

public class InverseOperation implements CoordinatesOperation {
  private final CoordinatesProjection projection;

  public InverseOperation(
    final CoordinatesProjection projection) {
    this.projection = projection;
  }

  public void perform(
    final Coordinates from,
    final Coordinates to) {
    projection.inverse(from, to);
  }
}