package com.revolsys.io.xbase;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.io.ResourceEndianOutput;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.NonExistingResource;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.DateUtil;
import com.vividsolutions.jts.geom.PrecisionModel;

public class XbaseDataObjectWriter extends AbstractWriter<DataObject> {
  private static final Logger log = Logger.getLogger(XbaseDataObjectWriter.class);

  private final List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

  private final List<String> fieldNames = new ArrayList<String>();

  private final Resource resource;

  private final DataObjectMetaData metaData;

  private int numRecords = 0;

  private ResourceEndianOutput out;

  private boolean useZeroForNull = true;

  private boolean initialized;

  private Map<String, String> shortNames = new HashMap<String, String>();

  private Charset charset = FileUtil.UTF8;

  public XbaseDataObjectWriter(final DataObjectMetaData metaData,
    final Resource resource) {
    this.metaData = metaData;
    this.resource = resource;
  }

  protected int addDbaseField(final String fullName, final DataType dataType,
    final Class<?> typeJavaClass, int length, final int scale) {
    char type = FieldDefinition.NUMBER_TYPE;
    if (typeJavaClass == Boolean.class) {
      type = FieldDefinition.LOGICAL_TYPE;
    } else if (Date.class.isAssignableFrom(typeJavaClass)) {
      type = FieldDefinition.DATE_TYPE;
    } else if (typeJavaClass == Long.class || typeJavaClass == BigInteger.class) {
      length = 18;
    } else if (typeJavaClass == Integer.class) {
      length = 10;
    } else if (typeJavaClass == Short.class) {
      length = 5;
    } else if (typeJavaClass == Byte.class) {
      length = 5;
    } else if (Number.class.isAssignableFrom(typeJavaClass)) {
    } else {
      type = FieldDefinition.CHARACTER_TYPE;
    }
    final FieldDefinition field = addFieldDefinition(fullName, type, length,
      scale);
    return field.getLength();
  }

  protected FieldDefinition addFieldDefinition(final String fullName,
    final char type, int length, int scale) {
    if (type == FieldDefinition.NUMBER_TYPE) {
      if (length < 1) {
        length = 18;
      } else {
        length = Math.min(18, length);
      }
      if (scale < -1) {
        scale = 3;
      }
      scale = Math.min(15, scale);
      scale = Math.min(length - 3, scale);
      scale = Math.max(0, scale);
    } else {
      if (type == FieldDefinition.CHARACTER_TYPE) {
        if (length < 1) {
          length = 254;
        } else {
          length = Math.min(254, length);
        }
      } else if (type == FieldDefinition.LOGICAL_TYPE) {
        length = 1;
      } else if (type == FieldDefinition.DATE_TYPE) {
        length = 8;
      }
      scale = 0;
    }
    String name = this.shortNames.get(fullName);
    if (name == null) {
      name = fullName.toUpperCase();
    }
    if (name.length() > 10) {
      name = name.substring(0, 10);
    }
    int i = 1;
    while (this.fieldNames.contains(name)) {
      final String suffix = String.valueOf(i);
      name = name.substring(0, name.length() - suffix.length()) + i;
      i++;
    }

    final FieldDefinition field = new FieldDefinition(name, fullName, type,
      length, scale);
    this.fieldNames.add(name);
    this.fields.add(field);
    return field;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void close() {
    try {
      if (this.out != null) {
        try {
          this.out.write(0x1a);
          this.out.seek(1);
          final Date now = new Date();
          this.out.write(now.getYear());
          this.out.write(now.getMonth() + 1);
          this.out.write(now.getDate());

          this.out.writeLEInt(this.numRecords);
        } finally {
          try {
            this.out.close();
          } finally {
            this.out = null;
          }
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  public Charset getCharset() {
    return this.charset;
  }

  public DataObjectMetaData getMetaData() {
    return this.metaData;
  }

  public Map<String, String> getShortNames() {
    return this.shortNames;
  }

  protected boolean hasField(final String name) {
    if (StringUtils.hasText(name)) {
      return this.fieldNames.contains(name.toUpperCase());
    } else {
      return false;
    }
  }

  protected void init() throws IOException {
    if (!this.initialized) {
      this.initialized = true;
      if (!(this.resource instanceof NonExistingResource)) {
        final Map<String, String> shortNames = getProperty("shortNames");
        if (shortNames != null) {
          this.shortNames = shortNames;
        }
        this.out = new ResourceEndianOutput(this.resource);
        writeHeader();
      }
      final Resource codePageResource = SpringUtil.getResourceWithExtension(
        this.resource, "cpg");
      if (!(codePageResource instanceof NonExistingResource)) {
        final PrintWriter writer = SpringUtil.getPrintWriter(codePageResource);
        try {
          writer.print(this.charset.name());
        } finally {
          writer.close();
        }
      }
    }
  }

  public boolean isUseZeroForNull() {
    return this.useZeroForNull;
  }

  protected void preFirstWrite(final DataObject object) throws IOException {
  }

  public void setCharset(final Charset charset) {
    this.charset = charset;
  }

  public void setShortNames(final Map<String, String> shortNames) {
    this.shortNames = shortNames;
  }

  public void setUseZeroForNull(final boolean useZeroForNull) {
    this.useZeroForNull = useZeroForNull;
  }

  @Override
  public String toString() {
    return this.resource.toString();
  }

  @Override
  public void write(final DataObject object) {
    try {
      if (!this.initialized) {
        init();
        preFirstWrite(object);
      }
      if (this.out != null) {
        this.out.write(' ');
      }
      for (final FieldDefinition field : this.fields) {
        if (!writeField(object, field)) {
          final String attributeName = field.getFullName();
          log.warn("Unable to write attribute '" + attributeName
            + "' with value " + object.getValue(attributeName));
        }
      }
      this.numRecords++;
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected boolean writeField(final DataObject object,
    final FieldDefinition field) throws IOException {
    if (this.out == null) {
      return true;
    } else {
      final String attributeName = field.getFullName();
      final Object value = object.getValue(attributeName);
      final int fieldLength = field.getLength();
      switch (field.getType()) {
        case FieldDefinition.NUMBER_TYPE:
          String numString = "";
          final DecimalFormat numberFormat = field.getNumberFormat();
          if (value == null) {
            if (this.useZeroForNull) {
              numString = numberFormat.format(0);
            }
          } else if (value instanceof Number) {
            Number number = (Number)value;
            final int decimalPlaces = field.getDecimalPlaces();
            if (decimalPlaces >= 0) {
              if (number instanceof BigDecimal) {
                final BigDecimal bigDecimal = new BigDecimal(number.toString());
                number = bigDecimal.setScale(decimalPlaces,
                  RoundingMode.HALF_UP);
              } else if (number instanceof Double || number instanceof Float) {
                final double doubleValue = number.doubleValue();
                final PrecisionModel precisionModel = field.getPrecisionModel();
                number = precisionModel.makePrecise(doubleValue);
              }
            }
            numString = numberFormat.format(number);
          } else {
            throw new IllegalArgumentException("Not a number " + attributeName
              + "=" + value);
          }
          final int numLength = numString.length();
          if (numLength > fieldLength) {
            for (int i = 0; i < fieldLength; i++) {
              this.out.write('9');
            }
          } else {
            for (int i = numLength; i < fieldLength; i++) {
              this.out.write(' ');
            }
            this.out.writeBytes(numString);
          }
          return true;
        case FieldDefinition.FLOAT_TYPE:
          String floatString = "";
          if (value != null) {
            floatString = value.toString();
          }
          final int floatLength = floatString.length();
          if (floatLength > fieldLength) {
            for (int i = 0; i < fieldLength; i++) {
              this.out.write('9');
            }
          } else {
            for (int i = floatLength; i < fieldLength; i++) {
              this.out.write(' ');
            }
            this.out.writeBytes(floatString);
          }
          return true;

        case FieldDefinition.CHARACTER_TYPE:
          String string = "";
          if (value != null) {
            string = StringConverterRegistry.toString(value);
          }
          final byte[] bytes = string.getBytes(this.charset);
          if (bytes.length >= fieldLength) {
            this.out.write(bytes, 0, fieldLength);
          } else {
            this.out.write(bytes);
            for (int i = bytes.length; i < fieldLength; i++) {
              this.out.write(' ');
            }
          }
          return true;

        case FieldDefinition.DATE_TYPE:
          if (value instanceof Date) {
            final Date date = (Date)value;
            final String dateString = DateUtil.format("yyyyMMdd", date);
            this.out.writeBytes(dateString);

          } else if (value == null) {
            this.out.writeBytes("        ");
          } else {
            this.out.writeBytes(value.toString().substring(0, 8));
          }
          return true;

        case FieldDefinition.LOGICAL_TYPE:
          boolean logical = false;
          if (value instanceof Boolean) {
            final Boolean boolVal = (Boolean)value;
            logical = boolVal.booleanValue();
          } else if (value != null) {
            logical = Boolean.valueOf(value.toString());
          }
          if (logical) {
            this.out.write('T');
          } else {
            this.out.write('F');
          }
          return true;

        default:
          return false;
      }
    }
  }

  @SuppressWarnings("deprecation")
  private void writeHeader() throws IOException {
    if (this.out != null) {
      int recordLength = 1;

      this.fields.clear();
      int numFields = 0;
      for (final String name : this.metaData.getAttributeNames()) {
        final int index = this.metaData.getAttributeIndex(name);
        final int length = this.metaData.getAttributeLength(index);
        final int scale = this.metaData.getAttributeScale(index);
        final DataType attributeType = this.metaData.getAttributeType(index);
        final Class<?> typeJavaClass = attributeType.getJavaClass();
        final int fieldLength = addDbaseField(name, attributeType,
          typeJavaClass, length, scale);
        if (fieldLength > 0) {
          recordLength += fieldLength;
          numFields++;
        }
      }
      this.out.write(0x03);
      final Date now = new Date();
      this.out.write(now.getYear());
      this.out.write(now.getMonth() + 1);
      this.out.write(now.getDate());
      // Write 0 as the number of records, come back and update this when closed
      this.out.writeLEInt(0);
      final short headerLength = (short)(33 + numFields * 32);

      this.out.writeLEShort(headerLength);
      this.out.writeLEShort((short)recordLength);
      this.out.writeLEShort((short)0);
      this.out.write(0);
      this.out.write(0);
      this.out.writeLEInt(0);
      this.out.writeLEInt(0);
      this.out.writeLEInt(0);
      this.out.write(0);
      this.out.write(1);
      this.out.writeLEShort((short)0);
      int offset = 1;
      for (final FieldDefinition field : this.fields) {
        if (field.getDataType() != DataTypes.OBJECT) {
          String name = field.getName();
          if (name.length() > 10) {
            name = name.substring(0, 10);
          }
          final int length = field.getLength();
          int decimalPlaces = field.getDecimalPlaces();
          if (decimalPlaces < 0) {
            decimalPlaces = 0;
          } else if (decimalPlaces > 15) {
            decimalPlaces = Math.min(length, 15);
          } else if (decimalPlaces > length) {
            decimalPlaces = Math.min(length, 15);
          }
          this.out.writeBytes(name);
          final int numPad = 11 - name.length();
          for (int i = 0; i < numPad; i++) {
            this.out.write(0);
          }
          this.out.write(field.getType());
          this.out.writeLEInt(0);
          this.out.write(length);
          this.out.write(decimalPlaces);
          this.out.writeLEShort((short)0);
          this.out.write(0);
          this.out.writeLEShort((short)0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          offset += length;
        }
      }
      this.out.write(0x0d);
    }
  }
}
