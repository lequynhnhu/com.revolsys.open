package com.revolsys.io.json;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.io.Resource;

import com.revolsys.io.AbstractMapReaderFactory;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.io.Reader;

public class JsonMapIoFactory extends AbstractMapReaderFactory implements
  MapWriterFactory {
  public static Map<String, String> toMap(final String string) {
    final Map<String, Object> map = toObjectMap(string);
    if (map.isEmpty()) {
      return Collections.emptyMap();
    } else {
      final Map<String, String> stringMap = new LinkedHashMap<String, String>();
      for (final Entry<String, Object> entry : map.entrySet()) {
        final String key = entry.getKey();
        final Object value = entry.getValue();
        if (value == null) {
          stringMap.put(key, null);
        } else {
          stringMap.put(key, value.toString());
        }
      }
      return stringMap;
    }
  }

  public static Map<String, Object> toObjectMap(final String string) {
    final StringReader reader = new StringReader(string);
    final Reader<Map<String, Object>> mapReader = new JsonMapReader(reader);
    for (final Map<String, Object> map : mapReader) {
      return map;
    }
    return Collections.emptyMap();
  }

  public static String toString(final Map<String, ? extends Object> map) {
    final StringWriter writer = new StringWriter();
    final JsonMapWriter mapWriter = new JsonMapWriter(writer);
    mapWriter.write(map);
    mapWriter.close();
    return writer.toString();
  }

  public JsonMapIoFactory() {
    super("JSON");
    addMediaTypeAndFileExtension("application/json", "json");
  }

  public Reader<Map<String, Object>> createMapReader(final Resource resource) {
    try {
      return new JsonMapReader(resource.getInputStream());
    } catch (final IOException e) {
      throw new RuntimeException("Unable to open " + resource, e);
    }
  }

  public MapWriter getWriter(final Writer out) {
    return new JsonMapWriter(out);
  }

  public boolean isCustomAttributionSupported() {
    return true;
  }

  public boolean isGeometrySupported() {
    return true;
  }

}