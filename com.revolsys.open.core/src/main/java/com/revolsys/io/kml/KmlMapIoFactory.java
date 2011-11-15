package com.revolsys.io.kml;

import java.io.Writer;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;

public class KmlMapIoFactory extends AbstractIoFactory implements
  MapWriterFactory {
  public KmlMapIoFactory() {
    super(Kml22Constants.FORMAT_DESCRIPTION);
    addMediaTypeAndFileExtension(Kml22Constants.MEDIA_TYPE,
      Kml22Constants.FILE_EXTENSION);
  }

  public MapWriter getWriter(final Writer out) {
    return new KmlMapWriter(out);
  }

  public boolean isCustomAttributionSupported() {
    return true;
  }

  public boolean isGeometrySupported() {
    return true;
  }

}