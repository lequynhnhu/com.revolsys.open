package com.revolsys.io.ecsv.type;

import java.io.PrintWriter;

import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.data.model.types.SimpleDataType;

public class GeometryFactoryFieldType extends AbstractEcsvFieldType {
  private static final DataType DATA_TYPE = new SimpleDataType(
    "GeometryFactory", GeometryFactory.class);

  static {
    DataTypes.register(DATA_TYPE);
  }

  public GeometryFactoryFieldType() {
    super(DATA_TYPE);
  }

  public Object parseValue(final String text) {
    if (StringUtils.hasLength(text)) {
      String[] values = text.split(",");
      int srid = Integer.parseInt(values[0]);
      double scaleXy = Double.parseDouble(values[1]);
      if (values.length == 2) {
        GeometryFactory geometryFactory = new GeometryFactory(srid, scaleXy);
        return geometryFactory;
      } else {
        double scaleZ = Double.parseDouble(values[2]);
        GeometryFactory geometryFactory = new GeometryFactory(srid, scaleXy,
          scaleZ);
        return geometryFactory;
      }
    } else {
      return null;
    }
  }

  public void writeValue(final PrintWriter out, final Object value) {
    if (value instanceof GeometryFactory) {
      GeometryFactory geometryFactory = (GeometryFactory)value;
      out.print('"');
      final int srid = geometryFactory.getSRID();
      out.print(srid);
      final double scaleXY = geometryFactory.getScaleXY();
      out.print(',');
      out.print(scaleXY);
      if (geometryFactory.hasZ() || geometryFactory.hasM()) {
        final double scaleZ = geometryFactory.getScaleZ();
        out.print(',');
        out.print(scaleZ);
      }
      if (geometryFactory.hasM()) {
        final double scaleM = geometryFactory.getScaleZ();
        out.print(',');
        out.print(scaleM);
      }
      out.print('"');
    }
  }

}