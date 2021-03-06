package com.revolsys.jtstest.function;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.shape.fractal.KochSnowflakeBuilder;
import com.revolsys.jts.shape.fractal.SierpinskiCarpetBuilder;

public class CreateFractalShapeFunctions {

  public static Geometry kochSnowflake(final Geometry g, final int n) {
    final KochSnowflakeBuilder builder = new KochSnowflakeBuilder(
      FunctionsUtil.getFactoryOrDefault(g));
    builder.setExtent(FunctionsUtil.getEnvelopeOrDefault(g));
    builder.setNumPoints(n);
    return builder.getGeometry();
  }

  public static Geometry sierpinskiCarpet(final Geometry g, final int n) {
    final SierpinskiCarpetBuilder builder = new SierpinskiCarpetBuilder(
      FunctionsUtil.getFactoryOrDefault(g));
    builder.setExtent(FunctionsUtil.getEnvelopeOrDefault(g));
    builder.setNumPoints(n);
    return builder.getGeometry();
  }
}
