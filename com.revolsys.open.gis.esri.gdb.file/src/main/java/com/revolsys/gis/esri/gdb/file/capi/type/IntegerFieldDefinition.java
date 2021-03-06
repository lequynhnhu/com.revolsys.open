package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.esri.gdb.xml.model.Field;
import com.revolsys.util.Property;

public class IntegerFieldDefinition extends AbstractFileGdbFieldDefinition {
  public IntegerFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.INT,
      BooleanStringConverter.getBoolean(field.getRequired())
      || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 11;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    final CapiFileGdbRecordStore recordStore = getRecordStore();
    if (recordStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (recordStore) {
        final Integer number = row.getInteger(name);
        return number;
      }
    }
  }

  @Override
  public Object setValue(final Record object, final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        getRecordStore().setNull(row, name);
      }
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      final int intValue = number.intValue();
      synchronized (getRecordStore()) {
        row.setInteger(name, intValue);
      }
      return intValue;
    } else {
      final String string = value.toString().trim();
      if (Property.hasValue(string)) {
        final int intValue = Integer.parseInt(string);
        synchronized (getRecordStore()) {
          row.setInteger(name, intValue);
        }
        return intValue;
      } else {
        if (isRequired()) {
          throw new IllegalArgumentException(name
            + " is required and cannot be null");
        } else {
          getRecordStore().setNull(row, name);
          return null;
        }
      }
    }
  }

}
