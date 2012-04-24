package com.revolsys.io.json;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import org.springframework.core.io.Resource;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.spring.SpringUtil;

public class JsonSchemaWriter {
  private final JsonMapWriter writer;

  public JsonSchemaWriter(final Resource resource) {
    final Writer out = SpringUtil.getWriter(resource);
    writer = new JsonMapWriter(out, true);
    writer.setListRoot(true);
  }

  @PreDestroy
  public void close() {
    writer.close();
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  private Collection<Object> toJsonList(final Collection<?> collection) {
    final List<Object> list = new ArrayList<Object>();
    for (final Object object : collection) {
      final Object jsonValue = toJsonValue(object);
      list.add(jsonValue);
    }
    return list;
  }

  public Map<String, Object> toJsonMap(final Map<String, ?> map) {
    final Map<String, Object> jsonMap = new LinkedHashMap<String, Object>();
    for (final Entry<String, ?> entry : map.entrySet()) {
      final String name = entry.getKey();
      final Object value = entry.getValue();
      final Object jsonValue = toJsonValue(value);
      if (jsonValue != null) {
        jsonMap.put(name, jsonValue);
      }
    }
    return jsonMap;
  }

  public Object toJsonValue(Object value) {
    Object jsonValue = null;
    if (value == null) {
      jsonValue = null;
    } else if (value instanceof Number) {
      jsonValue = value;
    } else if (value instanceof Boolean) {
      jsonValue = value;
    } else if (value instanceof CharSequence) {
      jsonValue = value;
    } else if (value instanceof Map) {
      final Map<String, Object> objectMap = (Map<String, Object>)value;
      value = toJsonMap(objectMap);
    } else if (value instanceof Collection) {
      final Collection<?> collection = (Collection<?>)value;
      value = toJsonList(collection);
    } else {
      StringConverter<?> converter = StringConverterRegistry.INSTANCE.getConverter(value);
      if (converter != null) {
        jsonValue = converter.toString(value);
      }
    }
    return jsonValue;
  }

  public void write(final DataObjectMetaData metaData) {
    final Map<String, Object> metaDataMap = new LinkedHashMap<String, Object>();
    metaDataMap.put("name", metaData.getName());

    final List<Map<String, Object>> fields = new ArrayList<Map<String, Object>>();
    metaDataMap.put("fields", fields);
    for (final Attribute attribute : metaData.getAttributes()) {
      final Map<String, Object> field = new LinkedHashMap<String, Object>();
      final String name = attribute.getName();
      field.put("name", name);
      final DataType dataType = attribute.getType();
      final QName dataTypeName = dataType.getName();
      field.put("type", dataTypeName);
      final int length = attribute.getLength();
      if (length > 0) {
        field.put("length", length);
      }
      final int scale = attribute.getScale();
      if (scale > 0) {
        field.put("scale", scale);
      }
      final boolean required = attribute.isRequired();
      field.put("required", required);
      final Map<String, ?> attributeProperties = attribute.getProperties();
      final Map<String, Object> fieldProperties = toJsonMap(attributeProperties);
      if (!fieldProperties.isEmpty()) {
        field.put("properties", fieldProperties);
      }
      fields.add(field);
    }

    writer.write(metaDataMap);
  }
}