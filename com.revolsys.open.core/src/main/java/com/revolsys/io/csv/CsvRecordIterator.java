package com.revolsys.io.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.io.RecordIterator;
import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.io.FileUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Property;

public class CsvRecordIterator extends AbstractIterator<Record> implements
RecordIterator {

  private final char fieldSeparator;

  private String pointXAttributeName;

  private String pointYAttributeName;

  private String geometryColumnName;

  private DataType geometryType = DataTypes.GEOMETRY;

  private GeometryFactory geometryFactory = GeometryFactory.floating3();

  private RecordFactory recordFactory;

  private BufferedReader in;

  private RecordDefinition recordDefinition;

  private Resource resource;

  private boolean hasPointFields;

  public CsvRecordIterator(final Resource resource) {
    this(resource, new ArrayRecordFactory(), CsvConstants.FIELD_SEPARATOR);
  }

  public CsvRecordIterator(final Resource resource, final char fieldSeparator) {
    this(resource, new ArrayRecordFactory(), fieldSeparator);
  }

  public CsvRecordIterator(final Resource resource,
    final RecordFactory recordFactory) {
    this(resource, recordFactory, CsvConstants.FIELD_SEPARATOR);
  }

  public CsvRecordIterator(final Resource resource,
    final RecordFactory recordFactory, final char fieldSeparator) {
    this.resource = resource;
    this.recordFactory = recordFactory;
    this.fieldSeparator = fieldSeparator;
  }

  private void createRecordDefinition(final String[] fieldNames)
      throws IOException {
    this.hasPointFields = Property.hasValue(this.pointXAttributeName)
        && Property.hasValue(this.pointYAttributeName);
    if (this.hasPointFields) {
      this.geometryType = DataTypes.POINT;
    } else {
      this.pointXAttributeName = null;
      this.pointYAttributeName = null;
    }
    final List<Attribute> attributes = new ArrayList<>();
    Attribute geometryAttribute = null;
    for (final String name : fieldNames) {
      DataType type = DataTypes.STRING;
      if (name.equalsIgnoreCase(this.geometryColumnName)) {
        type = this.geometryType;
      }
      final Attribute attribute = new Attribute(name, type, false);
      if (name.equalsIgnoreCase(this.geometryColumnName)) {
        geometryAttribute = attribute;
      }
      attributes.add(attribute);
    }
    if (this.hasPointFields) {
      if (geometryAttribute == null) {
        geometryAttribute = new Attribute(this.geometryColumnName,
          this.geometryType, true);
        attributes.add(geometryAttribute);
      }
    }
    if (geometryAttribute != null) {
      geometryAttribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
        this.geometryFactory);
    }
    final RecordStoreSchema schema = getProperty("schema");
    String typePath = getProperty("typePath");
    if (!Property.hasValue(typePath)) {
      typePath = "/" + FileUtil.getBaseName(this.resource.getFilename());
      String schemaPath = getProperty("schemaPath");
      if (Property.hasValue(schemaPath)) {
        if (!schemaPath.startsWith("/")) {
          schemaPath = "/" + schemaPath;
        }
        typePath = schemaPath + typePath;
      }
    }
    this.recordDefinition = new RecordDefinitionImpl(schema, typePath,
      getProperties(), attributes);
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  protected void doClose() {
    FileUtil.closeSilent(this.in);
    this.recordFactory = null;
    this.geometryFactory = null;
    this.in = null;
    this.recordDefinition = null;
    this.resource = null;
  }

  @Override
  protected void doInit() {
    try {
      this.pointXAttributeName = getProperty("pointXAttributeName");
      this.pointYAttributeName = getProperty("pointYAttributeName");
      this.geometryColumnName = getProperty("geometryColumnName", "GEOMETRY");

      this.geometryFactory = getProperty("geometryFactory");
      if (this.geometryFactory == null) {
        final Integer geometrySrid = Property.getInteger(this, "geometrySrid");
        if (geometrySrid == null) {
          this.geometryFactory = EsriCoordinateSystems.getGeometryFactory(this.resource);
        } else {
          this.geometryFactory = GeometryFactory.floating3(geometrySrid);
        }
      }
      if (this.geometryFactory == null) {
        this.geometryFactory = GeometryFactory.floating3();
      }
      final DataType geometryType = DataTypes.getType((String)getProperty("geometryType"));
      if (Geometry.class.isAssignableFrom(geometryType.getJavaClass())) {
        this.geometryType = geometryType;
      }

      this.in = new BufferedReader(
        FileUtil.createUtf8Reader(this.resource.getInputStream()));
      final String[] line = readNextRecord();
      createRecordDefinition(line);
    } catch (final IOException e) {
      ExceptionUtil.log(getClass(), "Unable to open " + this.resource, e);
    } catch (final NoSuchElementException e) {
    }
  }

  @Override
  protected Record getNext() {
    try {
      final String[] record = readNextRecord();
      if (record != null && record.length > 0) {
        return parseRecord(record);
      } else {
        throw new NoSuchElementException();
      }
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Reads the next line from the file.
   *
   * @return the next line from the file without trailing newline
   * @throws IOException if bad things happen during the read
   */
  private String getNextLine() throws IOException {
    final BufferedReader in = this.in;
    if (in == null) {
      throw new NoSuchElementException();
    } else {
      final String nextLine = this.in.readLine();
      if (nextLine == null) {
        throw new NoSuchElementException();
      }
      return nextLine;
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  /**
   * Parses an incoming String and returns an array of elements.
   *
   * @param nextLine the string to parse
   * @return the comma-tokenized list of elements, or null if nextLine is null
   * @throws IOException if bad things happen during the read
   */
  private String[] parseLine(final String nextLine, final boolean readLine)
      throws IOException {
    String line = nextLine;
    if (line.length() == 0) {
      return new String[0];
    } else {

      final List<String> fields = new ArrayList<String>();
      StringBuffer sb = new StringBuffer();
      boolean inQuotes = false;
      boolean hadQuotes = false;
      do {
        if (inQuotes && readLine) {
          sb.append("\n");
          line = getNextLine();
          if (line == null) {
            break;
          }
        }
        for (int i = 0; i < line.length(); i++) {
          final char c = line.charAt(i);
          if (c == CsvConstants.QUOTE_CHARACTER) {
            hadQuotes = true;
            if (inQuotes && line.length() > i + 1
                && line.charAt(i + 1) == CsvConstants.QUOTE_CHARACTER) {
              sb.append(line.charAt(i + 1));
              i++;
            } else {
              inQuotes = !inQuotes;
              if (i > 2 && line.charAt(i - 1) != this.fieldSeparator
                  && line.length() > i + 1
                  && line.charAt(i + 1) != this.fieldSeparator) {
                sb.append(c);
              }
            }
          } else if (c == this.fieldSeparator && !inQuotes) {
            hadQuotes = false;
            if (hadQuotes || sb.length() > 0) {
              fields.add(sb.toString());
            } else {
              fields.add(null);
            }
            sb = new StringBuffer();
          } else {
            sb.append(c);
          }
        }
      } while (inQuotes);
      if (sb.length() > 0 || fields.size() > 0) {
        if (hadQuotes || sb.length() > 0) {
          fields.add(sb.toString());
        } else {
          fields.add(null);
        }
      }
      return fields.toArray(new String[0]);
    }
  }

  /**
   * Parse a record containing an array of String values into a Record with
   * the strings converted to the objects based on the attribute data type.
   *
   * @param record The record.
   * @return The Record.
   */
  private Record parseRecord(final String[] record) {
    final Record object = this.recordFactory.createRecord(this.recordDefinition);
    for (int i = 0; i < this.recordDefinition.getAttributeCount(); i++) {
      String value = null;
      if (i < record.length) {
        value = record[i];
        if (value != null) {
          final DataType dataType = this.recordDefinition.getAttributeType(i);
          final Object convertedValue = StringConverterRegistry.toObject(
            dataType, value);
          object.setValue(i, convertedValue);
        }
      }
    }
    if (this.hasPointFields) {
      final Double x = CollectionUtil.getDouble(object,
        this.pointXAttributeName);
      final Double y = CollectionUtil.getDouble(object,
        this.pointYAttributeName);
      if (x != null && y != null) {
        final Geometry geometry = this.geometryFactory.point(x, y);
        object.setGeometryValue(geometry);
      }
    }
    return object;
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   *
   * @return a string array with each comma-separated element as a separate
   *         entry.
   * @throws IOException if bad things happen during the read
   */
  private String[] readNextRecord() throws IOException {
    final String nextLine = getNextLine();
    return parseLine(nextLine, true);
  }
}