/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/java/com/revolsys/gis/processor/AddDefaultValuesProcess.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2006-04-29 00:28:10Z $
 * $Revision: 112 $

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
package com.revolsys.gis.parallel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractInOutProcess;

public class AddDefaultValuesProcess extends AbstractInOutProcess<DataObject,DataObject> {
  private static final Logger log = Logger.getLogger(AddDefaultValuesProcess.class);

  private Set<String> excludedAttributeNames = new HashSet<String>();

  private DataObjectMetaDataFactory metaDataFactory;

  private String schemaName;

  private final Map<DataObjectMetaData, Map<String, Object>> typeDefaultValues = new HashMap<DataObjectMetaData, Map<String, Object>>();

  private void addDefaultValues(
    final Map<String, Object> defaultValues,
    final DataObjectMetaData type) {
    if (type.getName().getNamespaceURI().equals(schemaName)) {
      defaultValues.putAll(type.getDefaultValues());
    }
  }

  private Map<String, Object> getDefaultValues(
    final DataObjectMetaData type) {
    if (schemaName == null) {
      return type.getDefaultValues();
    } else {
      Map<String, Object> defaultValues = typeDefaultValues.get(type);
      if (defaultValues == null) {
        defaultValues = new HashMap<String, Object>();
        addDefaultValues(defaultValues, type);
        typeDefaultValues.put(type, defaultValues);
      }
      return defaultValues;
    }
  }

  /**
   * Get the list of attribute names that will be excluded from having the
   * default values set.
   * 
   * @return The names of the attributes to exclude.
   */
  public Set<String> getExcludedAttributeNames() {
    return excludedAttributeNames;
  }

  public DataObjectMetaDataFactory getMetaDataFactory() {
    return metaDataFactory;
  }

  /**
   * Get the schema name of the type definitions to get the default values from.
   * 
   * @return The schema name.
   */
  public String getSchemaName() {
    return schemaName;
  }

  private void process(
    final DataObject dataObject) {
    final DataObjectMetaData type = dataObject.getMetaData();

    boolean process = true;
    if (schemaName != null) {
      if (!type.getName().getNamespaceURI().equals(schemaName)) {
        process = false;
      }
    }
    if (process) {
      processDefaultValues(dataObject, getDefaultValues(type));
    }

    for (int i = 0; i < type.getAttributeCount(); i++) {
      final Object value = dataObject.getValue(i);
      if (value instanceof DataObject) {
        process((DataObject)value);
      }
    }
  }

  private void processDefaultValues(
    final DataObject dataObject,
    final Map<String, Object> defaultValues) {
    for (final Iterator<Entry<String, Object>> defaults = defaultValues.entrySet()
      .iterator(); defaults.hasNext();) {
      final Entry<String, Object> defaultValue = defaults.next();
      final String key = defaultValue.getKey();
      final Object value = defaultValue.getValue();
      setDefaultValue(dataObject, key, value);
    }
  }

  @Override
  protected void run(
    final Channel<DataObject> in,
    final Channel<DataObject> out) {
    for (DataObject dataObject = in.read(); dataObject != null; dataObject = in.read()) {
      process(dataObject);
      out.write(dataObject);
    }
  }

  private void setDefaultValue(
    final DataObject dataObject,
    final String key,
    final Object value) {
    final int dotIndex = key.indexOf('.');
    if (dotIndex == -1) {
      if (dataObject.getValue(key) == null
        && !excludedAttributeNames.contains(key)) {
        log.info("Adding attribute " + key + "=" + value);
        dataObject.setValue(key, value);
      }
    } else {
      final String attributeName = key.substring(0, dotIndex);
      NDC.push(" -> " + attributeName);
      try {
        final String subKey = key.substring(dotIndex + 1);
        final Object attributeValue = dataObject.getValue(attributeName);
        if (attributeValue == null) {
          final DataObjectMetaData type = dataObject.getMetaData();
          final int attrIndex = type.getAttributeIndex(attributeName);
          final DataType dataType = type.getAttributeType(attrIndex);
          final Class<?> typeClass = dataType.getJavaClass();
          if (typeClass == DataObject.class) {

            final DataObjectMetaData subClass = metaDataFactory.getMetaData(dataType.getName());
            final DataObject subObject = subClass.createDataObject();
            setDefaultValue(subObject, subKey, value);
            dataObject.setValue(attributeName, subObject);
            process(subObject);
          }
        } else if (attributeValue instanceof DataObject) {
          final DataObject subObject = (DataObject)attributeValue;
          setDefaultValue(subObject, subKey, value);
        } else if (!attributeName.equals(dataObject.getMetaData()
          .getGeometryAttributeName())) {
          log.error("Attribute '" + attributeName + "' must be a DataObject");
        }
      } finally {
        NDC.pop();
      }
    }
  }

  /**
   * Set the list of attribute names that will be excluded from having the
   * default values set.
   * 
   * @param excludedAttributeNames The names of the attributes to exclude.
   */
  public void setExcludedAttributeNames(
    final Set<String> excludedAttributeNames) {
    this.excludedAttributeNames = excludedAttributeNames;
  }

  public void setMetaDataFactory(
    final DataObjectMetaDataFactory metaDataFactory) {
    this.metaDataFactory = metaDataFactory;
  }

  /**
   * Set the schema name of the type definitions to get the default values from.
   * 
   * @param schemaName The schema name.
   */
  public void setSchemaName(
    final String schemaName) {
    this.schemaName = schemaName;
  }

}