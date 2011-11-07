/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/model/schema/impl/DataObjectMetaDataImpl.java $
 * $Author:paul.austin@revolsys.com $
 * $Date:2007-06-09 09:28:28 -0700 (Sat, 09 Jun 2007) $
 * $Revision:265 $

 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.gis.data.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.WeakUuidObjectMap;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.types.DataType;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectMetaDataImpl implements DataObjectMetaData,
  Comparable<DataObjectMetaData>, Cloneable {
  private final Map<String, Integer> attributeIdMap = new HashMap<String, Integer>();

  private final Map<String, Attribute> attributeMap = new HashMap<String, Attribute>();

  private final List<String> attributeNames = new ArrayList<String>();

  private final List<Attribute> attributes = new ArrayList<Attribute>();

  private DataObjectFactory dataObjectFactory = new ArrayDataObjectFactory();

  private DataObjectMetaDataFactory dataObjectMetaDataFactory;

  private DataObjectStore dataObjectStore;

  private final Map<String, Object> defaultValues = new HashMap<String, Object>();

  /** The index of the primary geometry attribute. */
  private int geometryAttributeIndex = -1;

  private final UUID uuid = UUID.randomUUID();

  private final List<Integer> geometryAttributeIndexes = new ArrayList<Integer>();

  private final List<String> geometryAttributeNames = new ArrayList<String>();

  /** The index of the ID attribute. */
  private int idAttributeIndex = -1;

  /** The name of the data type. */
  private QName name;

  /** The meta data properties of the data type. */
  private final Map<String, Object> properties = new HashMap<String, Object>();

  private final Map<String, Collection<Object>> restrictions = new HashMap<String, Collection<Object>>();

  protected DataObjectStoreSchema schema;

  private final List<DataObjectMetaData> superClasses = new ArrayList<DataObjectMetaData>();

  public DataObjectMetaDataImpl(final DataObjectMetaData metaData) {
    this(metaData.getName(), metaData.getProperties(), metaData.getAttributes());
    setIdAttributeIndex(metaData.getIdAttributeIndex());
    WeakUuidObjectMap.putObject(uuid, this);
  }

  public DataObjectMetaDataImpl(final DataObjectStore dataObjectStore,
    final DataObjectStoreSchema schema, final DataObjectMetaData metaData) {
    this(metaData);
    this.dataObjectStore = dataObjectStore;
    this.dataObjectFactory = dataObjectStore.getDataObjectFactory();
    this.schema = schema;
    WeakUuidObjectMap.putObject(uuid, this);
  }

  public DataObjectMetaDataImpl(final DataObjectStore dataObjectStore,
    final DataObjectStoreSchema schema, final QName typeName) {
    this(typeName);
    this.dataObjectStore = dataObjectStore;
    this.dataObjectFactory = dataObjectStore.getDataObjectFactory();
    this.schema = schema;
    WeakUuidObjectMap.putObject(uuid, this);
  }

  public DataObjectMetaDataImpl(final QName name) {
    this.name = name;
    WeakUuidObjectMap.putObject(uuid, this);
  }

  public DataObjectMetaDataImpl(final QName name, final Attribute... attributes) {
    this(name, null, attributes);
  }

  public DataObjectMetaDataImpl(final QName name,
    final List<Attribute> attributes) {
    this(name, null, attributes);
  }

  public DataObjectMetaDataImpl(final QName name,
    final Map<String, Object> properties, final Attribute... attributes) {
    this(name, properties, Arrays.asList(attributes));
  }

  public DataObjectMetaDataImpl(final QName name,
    final Map<String, Object> properties, final List<Attribute> attributes) {
    this.name = name;
    for (final Attribute attribute : attributes) {
      addAttribute(attribute.clone());
    }
    cloneProperties(properties);
    WeakUuidObjectMap.putObject(uuid, this);
  }

  public void addAttribute(final Attribute attribute) {
    final int index = attributeNames.size();
    final String name = attribute.getName();
    String lowerName;
    if (name == null) {
      lowerName = null;
    } else {
      lowerName = name.toLowerCase();

    }
    attributeNames.add(name);
    attributes.add(attribute);
    attributeMap.put(lowerName, attribute);
    attributeIdMap.put(lowerName, attributeIdMap.size());
    final DataType dataType = attribute.getType();
    if (dataType == null) {
      LoggerFactory.getLogger(getClass()).debug(attribute.toString());
    } else {
      final Class<?> dataClass = dataType.getJavaClass();
      if (Geometry.class.isAssignableFrom(dataClass)) {
        geometryAttributeIndexes.add(index);
        geometryAttributeNames.add(name);
        if (geometryAttributeIndex == -1) {
          geometryAttributeIndex = index;
        }
      }
    }
    attribute.setIndex(index);
  }

  public Attribute addAttribute(final String name, final DataType type,
    final boolean required) {
    final Attribute attribute = new Attribute(name, type, required);
    addAttribute(attribute);
    return attribute;
  }

  public Attribute addAttribute(final String name, final DataType type,
    final int length, final boolean required) {
    final Attribute attribute = new Attribute(name, type, length, required);
    addAttribute(attribute);
    return attribute;
  }

  public Attribute addAttribute(final String name, final DataType type,
    final int length, final int scale, final boolean required) {
    final Attribute attribute = new Attribute(name, type, length, scale,
      required);
    addAttribute(attribute);
    return attribute;
  }

  public void addDefaultValue(final String attributeName,
    final Object defaultValue) {
    defaultValues.put(attributeName, defaultValue);
  }

  public void addRestriction(final String attributePath,
    final Collection<Object> values) {
    restrictions.put(attributePath, values);
  }

  public void addSuperClass(final DataObjectMetaData superClass) {
    if (!superClasses.contains(superClass)) {
      superClasses.add(superClass);
    }
  }

  @Override
  public DataObjectMetaDataImpl clone() {
    final DataObjectMetaDataImpl clone = new DataObjectMetaDataImpl(name,
      properties, attributes);
    clone.setIdAttributeIndex(idAttributeIndex);
    clone.setProperties(getProperties());
    return clone;
  }

  public void cloneProperties(final Map<String, Object> properties) {
    if (properties != null) {
      for (final Entry<String, Object> property : properties.entrySet()) {
        final String propertyName = property.getKey();
        if (property instanceof DataObjectMetaDataProperty) {
          DataObjectMetaDataProperty metaDataProperty = (DataObjectMetaDataProperty)property;
          metaDataProperty = metaDataProperty.clone();
          metaDataProperty.setMetaData(this);
          this.properties.put(propertyName, metaDataProperty);
        } else {
          this.properties.put(propertyName, property);
        }
      }
      this.properties.putAll(properties);
    }
  }

  public int compareTo(final DataObjectMetaData other) {
    if (name == null) {
      return 1;
    }
    return name.toString().compareTo(other.getName().toString());
  }

  public DataObject createDataObject() {
    return dataObjectFactory.createDataObject(this);
  }

  public void delete(final DataObject dataObject) {
    if (dataObjectStore == null) {
      throw new UnsupportedOperationException();
    } else {
      dataObjectStore.delete(dataObject);
    }
  }

  @Override
  public boolean equals(final Object other) {
    return compareTo((DataObjectMetaData)other) == 0;
  }

  public Attribute getAttribute(final CharSequence name) {
    final String lowerName = name.toString().toLowerCase();
    return attributeMap.get(lowerName);
  }

  public Attribute getAttribute(final int i) {
    return attributes.get(i);
  }

  public int getAttributeCount() {
    return attributes.size();
  }

  public int getAttributeIndex(final CharSequence name) {
    if (name == null) {
      return -1;
    } else {
      final String lowerName = name.toString().toLowerCase();
      final Integer attributeId = attributeIdMap.get(lowerName);
      if (attributeId == null) {
        return -1;
      } else {
        return attributeId;
      }
    }
  }

  public int getAttributeLength(final int i) {
    try {
      final Attribute attribute = attributes.get(i);
      return attribute.getLength();
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw e;
    }
  }

  public String getAttributeName(final int i) {
    try {
      if (i == -1) {
        return null;
      } else {
        final Attribute attribute = attributes.get(i);
        return attribute.getName();
      }
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw e;
    }
  }

  public List<String> getAttributeNames() {
    return attributeNames;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public int getAttributeScale(final int i) {
    final Attribute attribute = attributes.get(i);
    return attribute.getScale();
  }

  public DataType getAttributeType(final CharSequence name) {
    final int index = getAttributeIndex(name);
    if (index == -1) {
      return null;
    } else {
      return getAttributeType(index);
    }
  }

  public DataType getAttributeType(final int i) {
    final Attribute attribute = attributes.get(i);
    return attribute.getType();
  }

  public DataObjectFactory getDataObjectFactory() {
    return dataObjectFactory;
  }

  public DataObjectMetaDataFactory getDataObjectMetaDataFactory() {
    if (dataObjectMetaDataFactory == null) {
      return dataObjectStore;
    } else {
      return dataObjectMetaDataFactory;
    }
  }

  public DataObjectStore getDataObjectStore() {
    return dataObjectStore;
  }

  public Object getDefaultValue(final String attributeName) {
    return defaultValues.get(attributeName);
  }

  public Map<String, Object> getDefaultValues() {
    return defaultValues;
  }

  public Attribute getGeometryAttribute() {
    if (geometryAttributeIndex == -1) {
      return null;
    } else {
      return attributes.get(geometryAttributeIndex);
    }
  }

  public int getGeometryAttributeIndex() {
    return geometryAttributeIndex;
  }

  public List<Integer> getGeometryAttributeIndexes() {
    return geometryAttributeIndexes;
  }

  public String getGeometryAttributeName() {
    return getAttributeName(geometryAttributeIndex);
  }

  public List<String> getGeometryAttributeNames() {
    return geometryAttributeNames;
  }

  public GeometryFactory getGeometryFactory() {
    final Attribute geometryAttribute = getGeometryAttribute();
    if (geometryAttribute == null) {
      return null;
    } else {
      final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
      return geometryFactory;
    }
  }

  public Attribute getIdAttribute() {
    if (idAttributeIndex >= 0) {
      return attributes.get(idAttributeIndex);
    } else {
      return null;
    }
  }

  public int getIdAttributeIndex() {
    return idAttributeIndex;
  }

  public String getIdAttributeName() {
    return getAttributeName(idAttributeIndex);
  }

  public QName getName() {
    return name;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  @SuppressWarnings("unchecked")
  public <V> V getProperty(final String name) {
    return (V)properties.get(name);
  }

  public Map<String, Collection<Object>> getRestrictions() {
    return restrictions;
  }

  public DataObjectStoreSchema getSchema() {
    return schema;
  }

  public UUID getUuid() {
    return uuid;
  }

  public boolean hasAttribute(final CharSequence name) {
    final String lowerName = name.toString().toLowerCase();
    return attributeMap.containsKey(lowerName);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  public boolean isAttributeRequired(final int i) {
    final Attribute attribute = attributes.get(i);
    return attribute.isRequired();
  }

  public boolean isInstanceOf(final DataObjectMetaData classDefinition) {
    if (classDefinition == null) {
      return false;
    }
    if (equals(classDefinition)) {
      return true;
    }
    for (final DataObjectMetaData superClass : superClasses) {
      if (superClass.isInstanceOf(classDefinition)) {
        return true;
      }
    }
    return false;
  }

  private void readObject(final ObjectInputStream ois)
    throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    WeakUuidObjectMap.putObject(uuid, this);
  }

  public void replaceAttribute(final Attribute attribute,
    final Attribute newAttribute) {
    final String name = attribute.getName();
    final String lowerName = name.toLowerCase();
    final String newName = newAttribute.getName();
    if (attributes.contains(attribute) && name.equals(newName)) {
      final int index = attribute.getIndex();
      attributes.set(index, newAttribute);
      attributeMap.put(lowerName, newAttribute);
      newAttribute.setIndex(index);
    } else {
      addAttribute(newAttribute);
    }
  }

  public void setDataObjectMetaDataFactory(
    final DataObjectMetaDataFactory dataObjectMetaDataFactory) {
    this.dataObjectMetaDataFactory = dataObjectMetaDataFactory;
  }

  /**
   * @param geometryAttributeIndex the geometryAttributeIndex to set
   */
  public void setGeometryAttributeIndex(final int geometryAttributeIndex) {
    this.geometryAttributeIndex = geometryAttributeIndex;
  }

  public void setGeometryAttributeName(final String name) {
    final int id = getAttributeIndex(name);
    setGeometryAttributeIndex(id);
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    final Attribute geometryAttribute = getGeometryAttribute();
    if (geometryAttribute != null) {
      geometryAttribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
        geometryFactory);
    }
  }

  /**
   * @param idAttributeIndex the idAttributeIndex to set
   */
  public void setIdAttributeIndex(final int idAttributeIndex) {
    this.idAttributeIndex = idAttributeIndex;
  }

  public void setIdAttributeName(final String name) {
    final int id = getAttributeIndex(name);
    setIdAttributeIndex(id);
  }

  public void setName(final QName name) {
    this.name = name;
  }

  public void setProperties(final Map<String, Object> properties) {
    if (properties != null) {
      for (final Entry<String, Object> entry : properties.entrySet()) {
        final String key = entry.getKey();
        final Object value = entry.getValue();
        if (value instanceof DataObjectMetaDataProperty) {
          final DataObjectMetaDataProperty property = (DataObjectMetaDataProperty)value;
          final DataObjectMetaDataProperty clonedProperty = property.clone();
          clonedProperty.setMetaData(this);
        } else {
          setProperty(key, value);
        }
      }
    }

  }

  public void setProperty(final String name, final Object value) {
    properties.put(name, value);
  }

  @Override
  public String toString() {
    return name.toString();
  }
}