package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.xml.model.Field;

public class BinaryAttribute extends AbstractFileGdbAttribute {

  public BinaryAttribute(final Field field) {
    super(field.getName(), DataTypes.BASE64_BINARY, field.getLength(),
      field.getRequired() == Boolean.TRUE || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setValue(final Row row, final Object value) {
  }

}