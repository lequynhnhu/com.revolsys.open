package com.revolsys.gis.web.rest.converter;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.revolsys.data.io.ListRecordReader;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.io.RecordReaderFactory;
import com.revolsys.data.io.RecordWriterFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.ui.web.rest.converter.AbstractHttpMessageConverter;
import com.revolsys.ui.web.utils.HttpServletUtils;

public class RecordHttpMessageConverter extends
AbstractHttpMessageConverter<Record> {

  private final RecordReaderHttpMessageConverter readerConverter = new RecordReaderHttpMessageConverter();

  public RecordHttpMessageConverter() {
    super(Record.class, IoFactoryRegistry.getInstance().getMediaTypes(
      RecordReaderFactory.class), IoFactoryRegistry.getInstance()
      .getMediaTypes(RecordWriterFactory.class));
  }

  public GeometryFactory getGeometryFactory() {
    return this.readerConverter.getGeometryFactory();
  }

  public List<String> getRequestAttributeNames() {
    return this.readerConverter.getRequestAttributeNames();
  }

  @Override
  public Record read(final Class<? extends Record> clazz,
    final HttpInputMessage inputMessage) throws IOException,
    HttpMessageNotReadableException {
    final RecordReader reader = this.readerConverter.read(RecordReader.class,
      inputMessage);
    try {
      for (final Record record : reader) {
        return record;
      }
      return null;
    } finally {
      reader.close();
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.readerConverter.setGeometryFactory(geometryFactory);
  }

  public void setRequestAttributeNames(final List<String> requestAttributeNames) {
    this.readerConverter.setRequestAttributeNames(requestAttributeNames);
  }

  @Override
  public void write(final Record record, final MediaType mediaType,
    final HttpOutputMessage outputMessage) throws IOException,
    HttpMessageNotWritableException {
    if (!HttpServletUtils.getResponse().isCommitted()) {
      if (record != null) {
        final RecordDefinition recordDefinition = record.getRecordDefinition();
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        requestAttributes.setAttribute(IoConstants.SINGLE_OBJECT_PROPERTY,
          true, RequestAttributes.SCOPE_REQUEST);
        final ListRecordReader reader = new ListRecordReader(recordDefinition,
          record);
        this.readerConverter.write(reader, mediaType, outputMessage);
      }
    }
  }
}
