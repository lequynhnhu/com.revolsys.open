package com.revolsys.jts.operation.valid;

import com.revolsys.jts.geom.vertex.Vertex;

public class VertexCoordinateError extends VertexError {
  private final int axisIndex;

  public VertexCoordinateError(final String message, final Vertex vertex,
    final int axisIndex) {
    super(message, vertex);
    this.axisIndex = axisIndex;
  }

  public int getAxisIndex() {
    return this.axisIndex;
  }

  public double getCoordinate() {
    final Vertex vertex = getVertex();
    if (vertex == null) {
      return Double.NaN;
    } else {
      return vertex.getCoordinate(this.axisIndex);
    }
  }
}
