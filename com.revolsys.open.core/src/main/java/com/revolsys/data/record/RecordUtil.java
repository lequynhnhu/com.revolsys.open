package com.revolsys.data.record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.comparator.StringNumberComparator;
import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.CompareUtil;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public final class RecordUtil {

  public static int compareNullFirst(final Record record1, final Record record2,
    final String fieldName) {
    final Object value1 = getValue(record1, fieldName);
    final Object value2 = getValue(record2, fieldName);
    if (value1 == value2) {
      return 0;
    } else {
      if (value1 == null) {
        return -1;
      } else if (value2 == null) {
        return 1;
      } else {
        return CompareUtil.compare(value1, value2);
      }
    }
  }

  public static int compareNullFirst(final Record record1, final Record record2,
    final String... fieldNames) {
    for (final String fieldName : fieldNames) {
      final Object value1 = getValue(record1, fieldName);
      final Object value2 = getValue(record2, fieldName);
      if (value1 != value2) {
        if (value1 == null) {
          return -1;
        } else if (value2 == null) {
          return 1;
        } else {
          final int compare = CompareUtil.compare(value1, value2);
          if (compare != 0) {
            return compare;
          }
        }
      }
    }
    return 0;
  }

  public static int compareNullLast(final Record record1, final Record record2,
    final String fieldName) {
    final Object value1 = getValue(record1, fieldName);
    final Object value2 = getValue(record2, fieldName);
    if (value1 == value2) {
      return 0;
    } else {
      if (value1 == null) {
        return 1;
      } else if (value2 == null) {
        return -1;
      } else {
        return CompareUtil.compare(value1, value2);
      }
    }
  }

  public static int compareNullLast(final Record record1, final Record record2,
    final String... fieldNames) {
    for (final String fieldName : fieldNames) {
      final Object value1 = getValue(record1, fieldName);
      final Object value2 = getValue(record2, fieldName);
      if (value1 != value2) {
        if (value1 == null) {
          return 1;
        } else if (value2 == null) {
          return -1;
        } else {
          final int compare = CompareUtil.compare(value1, value2);
          if (compare != 0) {
            return compare;
          }
        }
      }
    }
    return 0;
  }

  public static Record copy(final RecordDefinition recordDefinition, final Record object) {
    final Record copy = new ArrayRecord(recordDefinition);
    copy.setValues(object);
    return copy;
  }

  /**
   * Create a copy of the data object replacing the geometry with the new
   * geometry. If the existing geometry on the object has user data it will be
   * cloned to the new geometry.
   *
   * @param object The object to copy.
   * @param geometry The new geometry.
   * @return The copied object.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Record> T copy(final T object, final Geometry geometry) {
    final Geometry oldGeometry = object.getGeometryValue();
    final T newObject = (T)object.clone();
    newObject.setGeometryValue(geometry);
    GeometryProperties.copyUserData(oldGeometry, geometry);
    return newObject;
  }

  public static RecordDefinition createGeometryRecordDefinition() {
    final FieldDefinition geometryField = new FieldDefinition("geometry", DataTypes.GEOMETRY, true);
    return new RecordDefinitionImpl("Feature", geometryField);
  }

  public static <D extends Record> List<D> filter(final Collection<D> objects,
    final Geometry geometry, final double maxDistance) {
    final List<D> results = new ArrayList<D>();
    for (final D object : objects) {
      final Geometry objectGeometry = object.getGeometryValue();
      final double distance = objectGeometry.distance(geometry);
      if (distance < maxDistance) {
        results.add(object);
      }
    }
    return results;
  }

  public static boolean getBoolean(final Record object, final String fieldName) {
    if (object == null) {
      return false;
    } else {
      final Object value = getValue(object, fieldName);
      if (value == null) {
        return false;
      } else if (value instanceof Boolean) {
        final Boolean booleanValue = (Boolean)value;
        return booleanValue;
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        return number.intValue() == 1;
      } else {
        final String stringValue = value.toString();
        if (stringValue.equals("Y") || stringValue.equals("1") || Boolean.parseBoolean(stringValue)) {
          return true;
        } else {
          return false;
        }
      }
    }
  }

  public static Double getDouble(final Record object, final int attributeIndex) {
    final Number value = object.getValue(attributeIndex);
    if (value == null) {
      return null;
    } else if (value instanceof Double) {
      return (Double)value;
    } else {
      return value.doubleValue();
    }
  }

  public static Double getDouble(final Record object, final String fieldName) {
    final Number value = object.getValue(fieldName);
    if (value == null) {
      return null;
    } else if (value instanceof Double) {
      return (Double)value;
    } else {
      return value.doubleValue();
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T getFieldByPath(final Record object, final String path) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();

    final String[] propertyPath = path.split("\\.");
    Object propertyValue = object;
    for (int i = 0; i < propertyPath.length && propertyValue != null; i++) {
      final String propertyName = propertyPath[i];
      if (propertyValue instanceof Record) {
        final Record record = (Record)propertyValue;

        if (record.hasField(propertyName)) {
          propertyValue = getValue(record, propertyName);
          if (propertyValue == null) {
            return null;
          } else if (i + 1 < propertyPath.length) {
            final CodeTable codeTable = recordDefinition.getCodeTableByColumn(propertyName);
            if (codeTable != null) {
              propertyValue = codeTable.getMap(SingleIdentifier.create(propertyValue));
            }
          }
        } else {
          return null;
        }
      } else if (propertyValue instanceof Geometry) {
        final Geometry geometry = (Geometry)propertyValue;
        propertyValue = GeometryProperties.getGeometryProperty(geometry, propertyName);
      } else if (propertyValue instanceof Map) {
        final Map<String, Object> map = (Map<String, Object>)propertyValue;
        propertyValue = map.get(propertyName);
        if (propertyValue == null) {
          return null;
        } else if (i + 1 < propertyPath.length) {
          final CodeTable codeTable = recordDefinition.getCodeTableByColumn(propertyName);
          if (codeTable != null) {
            propertyValue = codeTable.getMap(SingleIdentifier.create(propertyValue));
          }
        }
      } else {
        try {
          propertyValue = JavaBeanUtil.getProperty(propertyValue, propertyName);
        } catch (final IllegalArgumentException e) {
          throw new IllegalArgumentException("Path does not exist " + path, e);
        }
      }
    }
    return (T)propertyValue;
  }

  public static Set<Identifier> getIdentifiers(final Collection<? extends Record> records) {
    final Set<Identifier> identifiers = new TreeSet<>();
    for (final Record record : records) {
      final Identifier identifier = record.getIdentifier();
      if (identifier != null) {
        identifiers.add(identifier);
      }
    }
    return identifiers;
  }

  public static Integer getInteger(final Record object, final String fieldName) {
    if (object == null) {
      return null;
    } else {
      final Number value = object.getValue(fieldName);
      if (value == null) {
        return null;
      } else if (value instanceof Integer) {
        return (Integer)value;
      } else {
        return value.intValue();
      }
    }
  }

  public static Integer getInteger(final Record object, final String fieldName,
    final Integer defaultValue) {
    if (object == null) {
      return null;
    } else {
      final Number value = object.getValue(fieldName);
      if (value == null) {
        return defaultValue;
      } else if (value instanceof Integer) {
        return (Integer)value;
      } else {
        return value.intValue();
      }
    }
  }

  public static Long getLong(final Record object, final String fieldName) {
    final Number value = object.getValue(fieldName);
    if (value == null) {
      return null;
    } else if (value instanceof Long) {
      return (Long)value;
    } else {
      return value.longValue();
    }
  }

  public static Record getObject(final RecordDefinition recordDefinition,
    final Map<String, Object> values) {
    final Record object = new ArrayRecord(recordDefinition);
    for (final Entry<String, Object> entry : values.entrySet()) {
      final String name = entry.getKey();
      final FieldDefinition attribute = recordDefinition.getField(name);
      if (attribute != null) {
        final Object value = entry.getValue();
        if (value != null) {
          final DataType dataType = attribute.getType();
          @SuppressWarnings("unchecked")
          final Class<Object> dataTypeClass = (Class<Object>)dataType.getJavaClass();
          if (dataTypeClass.isAssignableFrom(value.getClass())) {
            object.setValue(name, value);
          } else {
            final StringConverter<Object> converter = StringConverterRegistry.getInstance()
              .getConverter(dataTypeClass);
            if (converter == null) {
              object.setValue(name, value);
            } else {
              final Object convertedValue = converter.toObject(value);
              object.setValue(name, convertedValue);
            }
          }
        }
      }
    }
    return object;
  }

  public static List<Record> getObjects(final RecordDefinition recordDefinition,
    final Collection<? extends Map<String, Object>> list) {
    final List<Record> objects = new ArrayList<Record>();
    for (final Map<String, Object> map : list) {
      final Record object = getObject(recordDefinition, map);
      objects.add(object);
    }
    return objects;
  }

  private static Object getValue(final Record record, final String fieldName) {
    if (record == null || !Property.hasValue(fieldName)) {
      return null;
    } else {
      return record.getValue(fieldName);
    }
  }

  public static void mergeStringListValue(final Map<String, Object> object, final Record object1,
    final Record object2, final String fieldName, final String separator) {
    final String value1 = object1.getString(fieldName);
    final String value2 = object2.getString(fieldName);
    Object value;
    if (!Property.hasValue(value1)) {
      value = value2;
    } else if (!Property.hasValue(value2)) {
      value = value1;
    } else if (EqualsRegistry.equal(value1, value2)) {
      value = value1;
    } else {
      final Set<String> values = new TreeSet<>(new StringNumberComparator());
      values.addAll(CollectionUtil.split(value1, ","));
      values.addAll(CollectionUtil.split(value2, ","));
      value = CollectionUtil.toString(values);
    }
    object.put(fieldName, value);
  }

  public static void mergeValue(final Map<String, Object> object, final Record object1,
    final Record object2, final String fieldName, final String separator) {
    final String value1 = object1.getString(fieldName);
    final String value2 = object2.getString(fieldName);
    Object value;
    if (!Property.hasValue(value1)) {
      value = value2;
    } else if (!Property.hasValue(value2)) {
      value = value1;
    } else if (EqualsRegistry.equal(value1, value2)) {
      value = value1;
    } else {
      value = value1 + separator + value2;
    }
    object.put(fieldName, value);
  }

  public static void setValues(final Record target, final Record source,
    final Collection<String> fieldNames, final Collection<String> ignoreFieldNames) {
    for (final String fieldName : fieldNames) {
      if (!ignoreFieldNames.contains(fieldName)) {
        final Object oldValue = getValue(target, fieldName);
        Object newValue = getValue(source, fieldName);
        if (!EqualsInstance.INSTANCE.equals(oldValue, newValue)) {
          newValue = JavaBeanUtil.clone(newValue);
          target.setValue(fieldName, newValue);
        }
      }
    }
  }

  public static Geometry unionGeometry(final Collection<?> objects) {
    Geometry union = null;
    for (final Object object : objects) {
      final Geometry geometry = unionGeometry(object);
      if (geometry != null) {
        union = geometry.union(union);
      }
    }
    return union;
  }

  public static Geometry unionGeometry(final Map<?, ?> map) {
    Geometry union = null;
    for (final Entry<?, ?> entry : map.entrySet()) {
      final Object key = entry.getKey();
      final Geometry keyGeometry = unionGeometry(key);
      if (keyGeometry != null) {
        union = keyGeometry.union(union);
      }
      final Object value = entry.getValue();
      final Geometry valueGeometry = unionGeometry(value);
      if (valueGeometry != null) {
        union = valueGeometry.union(union);
      }
    }
    return union;
  }

  public static Geometry unionGeometry(final Object object) {
    if (object instanceof Geometry) {
      final Geometry geometry = (Geometry)object;
      return geometry;
    } else if (object instanceof Record) {
      final Record record = (Record)object;
      return record.getGeometryValue();
    } else if (object instanceof Collection) {
      final Collection<?> objects = (Collection<?>)object;
      return unionGeometry(objects);
    } else if (object instanceof Map) {
      final Map<?, ?> map = (Map)object;
      return unionGeometry(map);
    } else {
      return null;
    }
  }

  private RecordUtil() {
  }

}
