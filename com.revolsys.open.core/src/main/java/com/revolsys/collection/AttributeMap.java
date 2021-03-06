package com.revolsys.collection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class AttributeMap extends LinkedHashMap<String, Object> {
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(AttributeMap.class);

  public AttributeMap() {
  }

  public AttributeMap(final int initialCapacity) {
    super(initialCapacity);
  }

  public AttributeMap(final int initialCapacity, final float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public AttributeMap(final int initialCapacity, final float loadFactor,
    final boolean accessOrder) {
    super(initialCapacity, loadFactor, accessOrder);
  }

  public AttributeMap(final Map<? extends String, ? extends Object> m) {
    super(m);
  }

  public Map<String, Object> getFields() {
    return this;
  }

  public void setAttributes(final Map<String, Object> attributes) {
    clear();
    putAll(attributes);
  }

  public void setProperties(final Resource resource) {
    final Properties properties = new Properties();
    try {
      properties.load(resource.getInputStream());
      setProps(properties);
    } catch (final Throwable e) {
      LOG.warn("Cannot load properties from " + resource, e);
    }
  }

  public void setPropertyResources(final Resource[] resources) {
    final Properties properties = new Properties();
    for (final Resource resource : resources) {
      try {
        properties.load(resource.getInputStream());
      } catch (final Throwable e) {
        LOG.warn("Cannot load properties from " + resource, e);
      }
    }
    setProps(properties);
  }

  public void setProps(final Properties properties) {
    for (final Entry<Object, Object> entry : properties.entrySet()) {
      final String key = (String)entry.getKey();
      final Object value = entry.getValue();
      put(key, value);
    }
  }
}
