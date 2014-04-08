package com.revolsys.io.saif.util;

import java.io.IOException;
import java.util.Map;

import com.revolsys.io.saif.SaifConstants;

public class AlignedPointConverter extends PointConverter {
  public AlignedPointConverter(final com.revolsys.jts.geom.GeometryFactory geometryFactory) {
    super(geometryFactory, SaifConstants.ALIGNED_POINT);
  }

  /**
   * north, directionIndicator are handled by the default handling
   */
  @Override
  protected void readAttribute(final OsnIterator iterator,
    final String attributeName, final Map<String, Object> values) {
    if (attributeName.equals("alignment")) {
      values.put("alignment", new Double(iterator.nextDoubleValue()));
    } else {
      super.readAttribute(iterator, attributeName, values);
    }
  }

  @Override
  protected void writeAttributes(final OsnSerializer serializer,
    final Map<String, Object> values) throws IOException {
    writeAttribute(serializer, values, "alignment");
    writeAttribute(serializer, values, "directionIndicator");
    writeEnumAttribute(serializer, values, "north");
    writeEnumAttribute(serializer, values, "qualifier");
  }

}
