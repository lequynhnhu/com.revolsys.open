package com.revolsys.io.wkt;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.data.io.RecordIterator;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.data.record.property.FieldProperties;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;

public class WktRecordIterator extends AbstractIterator<Record>
  implements RecordIterator {

  private RecordFactory factory;

  private BufferedReader in;

  private WktParser wktParser;

  private RecordDefinition recordDefinition;

  public WktRecordIterator(final RecordFactory factory,
    final Resource resource) throws IOException {
    this.factory = factory;
    this.in = new BufferedReader(
      FileUtil.createUtf8Reader(resource.getInputStream()));
    this.recordDefinition = RecordUtil.createGeometryRecordDefinition();
  }

  @Override
  protected void doClose() {
    FileUtil.closeSilent(in);
    factory = null;
    in = null;
    wktParser = null;
    recordDefinition = null;
  }

  @Override
  protected void doInit() {
    GeometryFactory geometryFactory;
    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    if (geometryField == null) {
      geometryFactory = GeometryFactory.floating3();
    } else {
      geometryFactory = geometryField.getProperty(FieldProperties.GEOMETRY_FACTORY);
      if (geometryFactory == null) {
        geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
        if (geometryFactory == null) {
          geometryFactory = GeometryFactory.floating3();
        }
        geometryField.setProperty(FieldProperties.GEOMETRY_FACTORY,
          geometryFactory);
      }
    }
    wktParser = new WktParser(geometryFactory);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return recordDefinition;
  }

  @Override
  protected Record getNext() {
    try {
      final String wkt = in.readLine();
      final Geometry geometry = wktParser.parseGeometry(wkt);
      if (geometry == null) {
        throw new NoSuchElementException();
      } else {
        final Record object = factory.createRecord(getRecordDefinition());
        object.setGeometryValue(geometry);
        return object;
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error reading geometry ", e);
    }

  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}